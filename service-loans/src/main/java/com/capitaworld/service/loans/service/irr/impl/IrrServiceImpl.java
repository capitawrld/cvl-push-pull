package com.capitaworld.service.loans.service.irr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.capitaworld.service.loans.domain.fundseeker.corporate.*;
import com.capitaworld.service.loans.model.corporate.CorporateFinalInfoRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.*;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateFinalInfoService;
import com.capitaworld.service.loans.service.scoring.ScoringService;
import com.capitaworld.service.loans.service.scoring.impl.ScoringServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.model.retail.PastFinancialEstimatesDetailRequest;
import com.capitaworld.service.loans.service.common.DocumentManagementService;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.service.fundseeker.corporate.PastFinancialEstiamateDetailsService;
import com.capitaworld.service.loans.service.irr.IrrService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.CommonUtils.LoanType;
import com.capitaworld.service.oneform.enums.Denomination;
import com.capitaworld.service.rating.RatingClient;
import com.capitaworld.service.rating.model.FinancialInputRequest;
import com.capitaworld.service.rating.model.IndustryResponse;
import com.capitaworld.service.rating.model.IrrRequest;
import com.capitaworld.service.rating.model.QualitativeInputSheetManuRequest;
import com.capitaworld.service.rating.model.QualitativeInputSheetServRequest;
import com.capitaworld.service.rating.model.QualitativeInputSheetTradRequest;
import com.capitaworld.service.rating.model.RatingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.Calendar;

@Service
@Transactional
public class IrrServiceImpl implements IrrService{
	
	@Autowired
	private DocumentManagementService documentManagementService;
	
	@Autowired
	RatingClient ratingClient;
	
	@Autowired
	OperatingStatementDetailsRepository operatingStatementDetailsRepository;
	
	@Autowired
	LiabilitiesDetailsRepository liabilitiesDetailsRepository;
	
	@Autowired
	AssetsDetailsRepository assetsDetailsRepository;
	
	@Autowired
	CorporateFinalInfoService corporateFinalInfoService;
	
	@Autowired
	ProfitibilityStatementDetailRepository profitibilityStatementDetailRepository;

	@Autowired
	BalanceSheetDetailRepository balanceSheetDetailRepository;

	@Autowired
	CorporateMcqDetailRepository corporateMcqDetailRepository;
	
	/*@Autowired
	FinalWorkingCapitalLoanDetailRepository finalWorkingCapitalLoanDetailRepository;
	
	@Autowired
	FinalUnsecuredLoanDetailRepository finalUnsecuredLoanDetailRepository;*/
	
	@Autowired
	private LoanApplicationRepository loanApplicationRepository;
	
	@Autowired
	private CorporateApplicantService applicantService;
	
	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;
	
	@Autowired
	private PrimaryTermLoanDetailRepository primaryTermLoanDetailRepository;
	
	@Autowired
	private PrimaryWorkingCapitalLoanDetailRepository primaryWorkingCapitalLoanDetailRepository;
	
	@Autowired
	private PrimaryUnsecuredLoanDetailRepository primaryUnsecuredLoanDetailRepository;
	
	@Autowired
	private LoanApplicationService loanApplicationService;

	@Autowired
	private ScoringService scoringService;

	@Autowired
	private CreditRatingCompanyDetailsRepository creditRatingCompanyDetailsRepository;
	
	@Autowired
	private ApplicationProposalMappingRepository applicationProposalMappingRepository;

	private final Logger log = LoggerFactory.getLogger(IrrServiceImpl.class);

	private static final String ERROR_WHILE_GETTING_IRR_ID_FROM_ONE_FORM = "error while getting irr id from one form : ";
	private static final String SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN_AFTER_SOME_TIMES = "Something went wrong please try again after some times";
	private static final String OPERATING_STATEMENT_DETAILS_GET_DEPRECIATION = "operatingStatementDetails.getDepreciation()::";
	private static final String MSG_APP_ID = "App Id::";
	private static final String MSG_CURRENT_YEAR_1 = " currentYear-1::";

	@Override
	public ResponseEntity<RatingResponse> calculateIrrRating(Long appId, Long userId, Long proposalMapId) {
		// TODO Auto-generated method stub
		Integer businessTypeId = null; // get from irr-cw industry mapping
		Double industryRiskScore = 0.0;
		String industry = "";
		IrrRequest irrIndustryRequest = new IrrRequest();

		IrrRequest irrRequest = new IrrRequest();
		LoanApplicationMaster applicationMaster = null;
		ApplicationProposalMapping applicationProposalMapping = null;
		CorporateApplicantDetail corporateApplicantDetail = null;
		try {

			applicationProposalMapping = applicationProposalMappingRepository.findOne(proposalMapId);
			applicationMaster = loanApplicationRepository.findOne(appId);

			Long denom = null;
			if (applicationMaster.getDenominationId() != null) {
				denom = Denomination.getById(applicationMaster.getDenominationId()).getDigit();
			} else {
				denom = 1L;
			}

			userId = applicationMaster.getUserId();
			corporateApplicantDetail = corporateApplicantDetailRepository.getByApplicationAndUserId(userId,
					appId.longValue());

			if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVericalFunding())
					|| CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSector())
					|| CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSubsector())) {
				log.error("error while getting industry,sector,subsector");
				return new ResponseEntity<RatingResponse>(
						new RatingResponse("Select key verticle sector and sub sector in profile section",
								HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			if (applicationProposalMapping != null && (CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getIsFinalLocked())
					|| !(true == applicationProposalMapping.getIsFinalLocked()))) {
				log.info("final section is not locked");
				return new ResponseEntity<RatingResponse>(
						new RatingResponse("Submit your final one form section for MSME score",
								HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Long irrId = null;
			try {

				irrId = loanApplicationService.getIrrByApplicationId(appId);

				if (irrId == null) {
					log.error(ERROR_WHILE_GETTING_IRR_ID_FROM_ONE_FORM);
					return new ResponseEntity<RatingResponse>(new RatingResponse(
							ERROR_WHILE_GETTING_IRR_ID_FROM_ONE_FORM, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
				}

			} catch (Exception e) {
				// TODO: handle exception
				log.error(ERROR_WHILE_GETTING_IRR_ID_FROM_ONE_FORM,e);
				return new ResponseEntity<RatingResponse>(
						new RatingResponse(ERROR_WHILE_GETTING_IRR_ID_FROM_ONE_FORM, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);

			}

			// start getting irr industry and business type
			try {

				irrIndustryRequest.setIrrIndustryId(irrId);
				irrIndustryRequest = ratingClient.getIrrIndustry(irrIndustryRequest);
				IndustryResponse industryResponse = irrIndustryRequest.getIndustryResponse();
				if (CommonUtils.isObjectNullOrEmpty(industryResponse)) {
					log.info("Error while getting irr id from rating");
					return new ResponseEntity<RatingResponse>(
							new RatingResponse("Something went wrong please try again after some times",
									HttpStatus.BAD_REQUEST.value()),
							HttpStatus.OK);
				}

				businessTypeId = industryResponse.getBusinessTypeId();
				industryRiskScore = industryResponse.getScore();
				industry = industryResponse.getIndustry();
			} catch (Exception e) {
				// TODO: handle exception
				log.error("error while getting irr industry detail from rating");
				e.printStackTrace();

				return new ResponseEntity<RatingResponse>(
						new RatingResponse("error while getting irr industry detail from rating",
								HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			// end getting irr industry and business type

			irrRequest.setApplicationId(appId);
			//irrRequest.setProposalMappingId(proposalMapId);
			irrRequest.setCompanyName(corporateApplicantDetail.getOrganisationName());
			irrRequest.setBusinessTypeId(businessTypeId);
			irrRequest.setUserId(userId);

			/*
			 * Boolean isCmaUploaded=isCMAUploaded(appId,applicationMaster.getProductId());
			 * Boolean
			 * isCoActUploaded=isCoActUploaded(appId,applicationMaster.getProductId());
			 */

			Boolean isCmaUploaded = true;
			Boolean isCoActUploaded = false;

			if ((false == isCmaUploaded) && (false == isCoActUploaded)) {
				log.info("cma and coAct are not uploaded.");
				return new ResponseEntity<RatingResponse>(
						new RatingResponse("Upload either of CMA Or Company's Act in final section.",
								HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			if (com.capitaworld.service.rating.utils.CommonUtils.BusinessType.MANUFACTURING == businessTypeId) {
				// ---- Manufacturing
				irrRequest.setQualitativeInputSheetManuRequest(qualitativeInputServiceManu(appId, userId,
						applicationMaster.getProductId(), isCmaUploaded, isCoActUploaded, industryRiskScore, denom, proposalMapId));
			} else if (com.capitaworld.service.rating.utils.CommonUtils.BusinessType.SERVICE == businessTypeId) {
				// ---- Service
				irrRequest.setQualitativeInputSheetServRequest(qualitativeInputServiceService(appId, userId,
						applicationMaster.getProductId(), isCmaUploaded, isCoActUploaded, denom, proposalMapId));
			} else if (com.capitaworld.service.rating.utils.CommonUtils.BusinessType.TRADING == businessTypeId) {
				// ---- Trading
				irrRequest.setQualitativeInputSheetTradRequest(qualitativeInputServiceTrading(appId, userId,
						applicationMaster.getProductId(), isCmaUploaded, isCoActUploaded, denom, proposalMapId));
			}

			// if CMA filled
			if (isCmaUploaded)
				irrRequest.setFinancialInputRequest(cmaIrrMappingService(userId, appId, industry, denom, proposalMapId));

			/*
			 * // if coAct filled if(isCoActUploaded)
			 * irrRequest.setFinancialInputRequest(coActIrrMappingService(userId,appId,
			 * industry,denom));
			 */

		} catch (Exception e) {
			e.printStackTrace();

			log.info("Error while mapping irr request and qualitative input from db");
			return new ResponseEntity<RatingResponse>(
					new RatingResponse("Something went wrong please try again after some times",
							HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}

		RatingResponse ratingResponse = new RatingResponse();
		try {

			ratingResponse = ratingClient.calculateIrrRating(irrRequest);
			log.info("rating respo->" + ratingResponse.toString());
			// ratingResponse.setData(irrRequest);
			ratingResponse.setBusinessTypeId(businessTypeId);

			return new ResponseEntity<RatingResponse>(
					new RatingResponse(ratingResponse, "Irr rating generated", HttpStatus.OK.value()), HttpStatus.OK);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

			log.info("Error while callling rating client");
			return new ResponseEntity<RatingResponse>(
					new RatingResponse("Something went wrong please try again after some times",
							HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}
	}
	
	private Boolean isCMAUploaded(Long appId, Integer productId) {
		
		try{
			
			LoanType type = CommonUtils.LoanType.getType(productId);
			switch (type) {
			case WORKING_CAPITAL:
				return  (documentManagementService.getDocumentDetails(appId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.WC_CMA)).size()>0?true:false);
			case TERM_LOAN:
				return  (documentManagementService.getDocumentDetails(appId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.TL_CMA)).size()>0?true:false);
			case UNSECURED_LOAN :
				return  (documentManagementService.getDocumentDetails(appId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_CMA)).size()>0?true:false);
			default : break;
			}
		}catch(DocumentException e){
			log.error("Exception in isCMAUploaded : ",e);
			return false;
		}
		return false;
	}
	
	private Boolean isCoActUploaded(Long appId, Integer productId) {
		
		try{
			
			LoanType type = CommonUtils.LoanType.getType(productId);
			switch (type) {
			case WORKING_CAPITAL:
				return  (documentManagementService.getDocumentDetails(appId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.WC_COMPANY_ACT)).size()>0?true:false);
			case TERM_LOAN:
				return  (documentManagementService.getDocumentDetails(appId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.TL_COMPANY_ACT)).size()>0?true:false);
			case UNSECURED_LOAN :
				return  (documentManagementService.getDocumentDetails(appId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_COMPANY_ACT)).size()>0?true:false);
			default : break;
			}
		}catch(DocumentException e){
			log.error("Exception in isCoActUploaded : ",e);
			return false;
		}
		return false;
	}
	
	
	
	@Override
	public FinancialInputRequest cmaIrrMappingService(Long userId, Long aplicationId,String industry,Long denom, Long proposalMapId) throws Exception {
		// TODO Auto-generated method stub
		//JSONObject jSONObject = new JSONObject();
		log.info("APPLICATION ID:::"+aplicationId);
		log.info("DENO::"+denom);
		IrrRequest irrRequest = new IrrRequest();
		FinancialInputRequest financialInputRequest = new FinancialInputRequest();
		OperatingStatementDetails operatingStatementDetails = new OperatingStatementDetails();
		LiabilitiesDetails liabilitiesDetails = new LiabilitiesDetails();
		AssetsDetails assetsDetails = new AssetsDetails();
		CorporateFinalInfoRequest  corporateFinalInfoRequest = new CorporateFinalInfoRequest();
		corporateFinalInfoRequest = corporateFinalInfoService.get(userId ,aplicationId);
		
		//---SHARE FACE VALUE SET-----
        Double shareFaceVal=1.00;

		CorporateApplicantDetail corporateApplicantDetail=corporateApplicantDetailRepository.findOneByApplicationIdId(aplicationId);
		if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getSharePriceFace()))
            shareFaceVal=corporateApplicantDetail.getSharePriceFace();

		financialInputRequest.setShareFaceValue(shareFaceVal);

		LoanApplicationMaster applicationMaster = null;
		/*applicationMaster = loanApplicationRepository.findOne(aplicationId);
		LoanType type = CommonUtils.LoanType.getType(applicationMaster.getProductId());
		switch (type) {
		case WORKING_CAPITAL:
			PrimaryWorkingCapitalLoanDetail primaryWorkingCapitalLoanDetail = null;
			primaryWorkingCapitalLoanDetail = primaryWorkingCapitalLoanDetailRepository.findOne(aplicationId);
			financialInputRequest.setShareFaceValue(primaryWorkingCapitalLoanDetail.getSharePriceFace() * denom);
			break;
		case TERM_LOAN:
			PrimaryTermLoanDetail primaryTermLoanDetail = null;
			primaryTermLoanDetail = primaryTermLoanDetailRepository.findOne(aplicationId);
			financialInputRequest.setShareFaceValue(primaryTermLoanDetail.getSharePriceFace() * denom);
			break;
		case UNSECURED_LOAN :
			PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetail = null;
			primaryUnsecuredLoanDetail = primaryUnsecuredLoanDetailRepository.findOne(aplicationId);
			financialInputRequest.setShareFaceValue(primaryUnsecuredLoanDetail.getSharePriceFace() * denom);
			break;
		}	*/
	
		// set industry
		financialInputRequest.setIndustryName(industry);
		
		financialInputRequest.setNoOfMonthTy(12.0);
		financialInputRequest.setNoOfMonthSy(12.0);
		financialInputRequest.setNoOfMonthFy(12.0);
		// -------------------------------------------------------THIRD year data-------------------------------------------------------------------------
		//========= ==========================================OPERATINGSTATEMENT DETAIL 3 YR========================================================
		int currentYear = scoringService.getFinYear(aplicationId);

		financialInputRequest.setYear(currentYear-1);

		financialInputRequest.setRatioAnalysisFyFullDate("31-March-"+(currentYear-1));
		
		try {
			
			operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByProposal(proposalMapId, currentYear-1+"");
			if(operatingStatementDetails != null) {
				
				log.info(MSG_APP_ID + aplicationId + MSG_CURRENT_YEAR_1 + (currentYear-1) + OPERATING_STATEMENT_DETAILS_GET_DEPRECIATION+operatingStatementDetails.getDepreciation());

				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails)){
					operatingStatementDetails = new OperatingStatementDetails();
				}
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDomesticSales()))
					operatingStatementDetails.setDomesticSales(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDomesticSales()))
					operatingStatementDetails.setExportSales(0.0);		
				financialInputRequest.setGrossSalesFy((operatingStatementDetails.getDomesticSales()+operatingStatementDetails.getExportSales()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getLessExciseDuty()))
					operatingStatementDetails.setLessExciseDuty(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductOtherItems()))
					operatingStatementDetails.setDeductOtherItems(0.0);	
				financialInputRequest.setLessExciseDuityFy((operatingStatementDetails.getLessExciseDuty()+operatingStatementDetails.getDeductOtherItems()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOperatingStock()))
					operatingStatementDetails.setAddOperatingStock(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductStockInProcess()))
					operatingStatementDetails.setDeductStockInProcess(0.0);	
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOperatingStockFg()))
					operatingStatementDetails.setAddOperatingStockFg(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductClStockFg()))
					operatingStatementDetails.setDeductClStockFg(0.0);	
				financialInputRequest.setIncreaseDecreaseStockFy(((operatingStatementDetails.getAddOperatingStock()-operatingStatementDetails.getDeductStockInProcess()) + (operatingStatementDetails.getAddOperatingStockFg()-operatingStatementDetails.getDeductClStockFg())) * denom);
				
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getRawMaterials()))
					operatingStatementDetails.setRawMaterials(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherSpares()))
					operatingStatementDetails.setOtherSpares(0.0);	
				financialInputRequest.setRawMaterialConsumedFy((operatingStatementDetails.getRawMaterials()+operatingStatementDetails.getOtherSpares()) * denom);
				
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getPowerAndFuel()))
					operatingStatementDetails.setPowerAndFuel(0.0);
				financialInputRequest.setPowerAndFuelCostFy(operatingStatementDetails.getPowerAndFuel()  * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDirectLabour()))
					operatingStatementDetails.setDirectLabour(0.0);
				financialInputRequest.setEmployeeCostFy(operatingStatementDetails.getDirectLabour() * denom);

				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getSellingGenlAdmnExpenses()))
					operatingStatementDetails.setSellingGenlAdmnExpenses(0.0);
				financialInputRequest.setGeneralAndAdminExpeFy(operatingStatementDetails.getSellingGenlAdmnExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getSellingAndDistributionExpenses()))
					operatingStatementDetails.setSellingAndDistributionExpenses(0.0);
				financialInputRequest.setSellingAndDistriExpeFy(operatingStatementDetails.getSellingAndDistributionExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getExpensesAmortised()))
					operatingStatementDetails.setExpensesAmortised(0.0);
				financialInputRequest.setLessExpeCapitaFy(operatingStatementDetails.getExpensesAmortised() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherMfgExpenses()))
					operatingStatementDetails.setOtherMfgExpenses(0.0);
				financialInputRequest.setMiscelExpeFy(operatingStatementDetails.getOtherMfgExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOtherRevenueIncome()))
					operatingStatementDetails.setAddOtherRevenueIncome(0.0);
				financialInputRequest.setOtherIncomeFy(operatingStatementDetails.getAddOtherRevenueIncome() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getInterest()))
					operatingStatementDetails.setInterest(0.0);
				financialInputRequest.setInterestFy(operatingStatementDetails.getInterest() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDepreciation()))
					operatingStatementDetails.setDepreciation(0.0);
				financialInputRequest.setDepriciationFy(operatingStatementDetails.getDepreciation() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getNetofNonOpIncomeOrExpenses()))
					operatingStatementDetails.setNetofNonOpIncomeOrExpenses(0.0);
				financialInputRequest.setExceptionalIncomeFy(operatingStatementDetails.getNetofNonOpIncomeOrExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getProvisionForTaxes()))
					operatingStatementDetails.setProvisionForTaxes(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getProvisionForDeferredTax()))
					operatingStatementDetails.setProvisionForDeferredTax(0.0);
				financialInputRequest.setProvisionForTaxFy((operatingStatementDetails.getProvisionForTaxes() + operatingStatementDetails.getProvisionForDeferredTax()) * denom);
		        
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherIncomeNeedTocCheckOp()))
					operatingStatementDetails.setOtherIncomeNeedTocCheckOp(0.0);
				financialInputRequest.setOtherIncomeNeedTocCheckOpFy(operatingStatementDetails.getOtherIncomeNeedTocCheckOp() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getEquityDeividendPaidAmt()))
					operatingStatementDetails.setEquityDeividendPaidAmt(0.0);
				financialInputRequest.setDividendPayOutFy(operatingStatementDetails.getEquityDeividendPaidAmt() * denom);
				
			}else {
				log.error("first year os is null ");
			}
			
			
		} catch (Exception e) {
			log.error("error while calculate first year financial data OS : ",e);
		}
		

		
		
		//========= ===============================================LIABILITIES DETAIL 3 YR==================================================================
		
		try {
			liabilitiesDetails = liabilitiesDetailsRepository.getLiabilitiesDetailByProposal(proposalMapId, currentYear-1+"");
			if(liabilitiesDetails != null) {
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails)){
					liabilitiesDetails = new LiabilitiesDetails();
				}
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getPreferencesShares()))
					liabilitiesDetails.setPreferencesShares(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOrdinarySharesCapital()))
					liabilitiesDetails.setOrdinarySharesCapital(0.0);
				financialInputRequest.setShareCapitalFy((liabilitiesDetails.getPreferencesShares() + liabilitiesDetails.getOrdinarySharesCapital()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getShareWarrentsOutstanding()))
					liabilitiesDetails.setShareWarrentsOutstanding(0.0);
				financialInputRequest.setShareWarrantOutstandingsFy((liabilitiesDetails.getShareWarrentsOutstanding()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getRevaluationReservse()))
					liabilitiesDetails.setRevaluationReservse(0.0);
				financialInputRequest.setRevalationReserveFy((liabilitiesDetails.getRevaluationReservse()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getGeneralReserve()))
					liabilitiesDetails.setGeneralReserve(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherReservse()))
					liabilitiesDetails.setOtherReservse(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSurplusOrDeficit()))
					liabilitiesDetails.setSurplusOrDeficit(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOthers()))
					liabilitiesDetails.setOthers(0.0);
				financialInputRequest.setOtherReserveAndSurplusFy((liabilitiesDetails.getGeneralReserve() + liabilitiesDetails.getOtherReservse() + liabilitiesDetails.getSurplusOrDeficit() + liabilitiesDetails.getOthers()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getMinorityInterest()))
					liabilitiesDetails.setMinorityInterest(0.0);
				financialInputRequest.setMinorityInterestFy(liabilitiesDetails.getMinorityInterest() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermLiabilitiesSecured()))
					liabilitiesDetails.setTermLiabilitiesSecured(0.0);
				financialInputRequest.setSecuredLoansFy(liabilitiesDetails.getTermLiabilitiesSecured() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclUnsecuredLoansFromPromoters()))
					liabilitiesDetails.setOtherNclUnsecuredLoansFromPromoters(0.0);
				financialInputRequest.setUnsecuredLoansPromotersFy(liabilitiesDetails.getOtherNclUnsecuredLoansFromPromoters() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclUnsecuredLoansFromOther()))
					liabilitiesDetails.setOtherNclUnsecuredLoansFromOther(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermLiabilitiesUnsecured()))
					liabilitiesDetails.setTermLiabilitiesUnsecured(0.0);
				financialInputRequest.setUnsecuredLoansOthersFy((liabilitiesDetails.getOtherNclUnsecuredLoansFromOther() + liabilitiesDetails.getTermLiabilitiesUnsecured()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSubTotalA()))
					liabilitiesDetails.setSubTotalA(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getShortTermBorrowingFromOthers()))
					liabilitiesDetails.setShortTermBorrowingFromOthers(0.0);
				financialInputRequest.setOtherBorrowingFy((liabilitiesDetails.getSubTotalA() + liabilitiesDetails.getShortTermBorrowingFromOthers()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDeferredTaxLiability()))
					liabilitiesDetails.setDeferredTaxLiability(0.0);
				financialInputRequest.setDeferredTaxLiablitiesFy(liabilitiesDetails.getDeferredTaxLiability() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclOthers()))
					liabilitiesDetails.setOtherNclOthers(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDeferredPaymentsCredits()))
					liabilitiesDetails.setDeferredPaymentsCredits(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermDeposits()))
					liabilitiesDetails.setTermDeposits(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDebentures()))
					liabilitiesDetails.setDebentures(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherTermLiabilies()))
					liabilitiesDetails.setOtherTermLiabilies(0.0);
				financialInputRequest.setOtherLongTermLiablitiesFy((liabilitiesDetails.getOtherNclOthers() + liabilitiesDetails.getDeferredPaymentsCredits() + liabilitiesDetails.getTermDeposits() + liabilitiesDetails.getDebentures() + liabilitiesDetails.getOtherTermLiabilies()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclLongTermProvisions()))
					liabilitiesDetails.setOtherNclLongTermProvisions(0.0);
				financialInputRequest.setLongTermProvisionFy(liabilitiesDetails.getOtherNclLongTermProvisions() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSundryCreditors()))
					liabilitiesDetails.setSundryCreditors(0.0);
				financialInputRequest.setTradePayablesFy(liabilitiesDetails.getSundryCreditors() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getAdvancePaymentsFromCustomers()))
					liabilitiesDetails.setAdvancePaymentsFromCustomers(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDividendPayable()))
					liabilitiesDetails.setDividendPayable(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherStatutoryLiability()))
					liabilitiesDetails.setOtherStatutoryLiability(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherCurrentLiability()))
					liabilitiesDetails.setOtherCurrentLiability(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans()))
					liabilitiesDetails.setDepositsOrInstalmentsOfTermLoans(0.0);
				financialInputRequest.setOtherCurruntLiablitiesFy((liabilitiesDetails.getAdvancePaymentsFromCustomers() + liabilitiesDetails.getDividendPayable() + liabilitiesDetails.getOtherStatutoryLiability() + liabilitiesDetails.getOtherCurrentLiability() + liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherIncomeNeedTocCheckLia()))
						liabilitiesDetails.setOtherIncomeNeedTocCheckLia(0.0);
				financialInputRequest.setOtherIncomeNeedTocCheckLiaFy(liabilitiesDetails.getOtherIncomeNeedTocCheckLia() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getProvisionalForTaxation()))
					liabilitiesDetails.setProvisionalForTaxation(0.0);
				financialInputRequest.setShortTermProvisionFy(liabilitiesDetails.getProvisionalForTaxation() * denom);
				
				
			}else {
				
				log.error("first year liability data is null");
				
			}
		} catch (Exception e) {
				log.error("Error while calculate data of first year liability data");
		}
		
		
		
		//========= ===============================================ASSET DETAIL 3 YR==================================================================
		
		
		try {
			
			assetsDetails = assetsDetailsRepository.getAssetsDetailByProposal(proposalMapId, currentYear-1+"");
			
			if(assetsDetails != null ) {
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails)){
					assetsDetails = new AssetsDetails();
				}
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getGrossBlock()))
					assetsDetails.setGrossBlock(0.0);
				financialInputRequest.setGrossBlockFy(assetsDetails.getGrossBlock() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getDepreciationToDate()))
					assetsDetails.setDepreciationToDate(0.0);
				financialInputRequest.setLessAccumulatedDepreFy(assetsDetails.getDepreciationToDate() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getImpairmentAsset()))
					assetsDetails.setImpairmentAsset(0.0);
				financialInputRequest.setImpairmentofAssetFy(assetsDetails.getImpairmentAsset() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherNcaOtherCapitalWorkInprogress()))
					assetsDetails.setOtherNcaOtherCapitalWorkInprogress(0.0);
				financialInputRequest.setCapitalWorkInProgressFy(assetsDetails.getOtherNcaOtherCapitalWorkInprogress() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getIntangibleAssets()))
					assetsDetails.setIntangibleAssets(0.0);
				financialInputRequest.setIntengibleAssetsFy(assetsDetails.getIntangibleAssets() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersPreOperativeExpensesPending()))
					assetsDetails.setOthersPreOperativeExpensesPending(0.0);
				financialInputRequest.setPreOperativeExpeFy(assetsDetails.getOthersPreOperativeExpensesPending() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersAssetsInTransit()))
					assetsDetails.setOthersAssetsInTransit(0.0);
				financialInputRequest.setAssetInTransitFy(assetsDetails.getOthersAssetsInTransit() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInvestmentsInSubsidiary()))
					assetsDetails.setInvestmentsInSubsidiary(0.0);
				financialInputRequest.setInvestmentInSubsidiariesFy(assetsDetails.getInvestmentsInSubsidiary() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersOther()))
					assetsDetails.setOthersOther(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getDeferredReceviables()))
					assetsDetails.setDeferredReceviables(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthers()))
					assetsDetails.setOthers(0.0);
				financialInputRequest.setOtherInvestmentFy((assetsDetails.getOthersOther() + assetsDetails.getDeferredReceviables() + assetsDetails.getOthers()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvanceToSuppliersCapitalGoods()))
					assetsDetails.setAdvanceToSuppliersCapitalGoods(0.0);
				financialInputRequest.setLongTermLoansAndAdvaFy(assetsDetails.getAdvanceToSuppliersCapitalGoods() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getNonConsumableStoreAndSpares()))
					assetsDetails.setNonConsumableStoreAndSpares(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherNonCurrentAssets()))
					assetsDetails.setOtherNonCurrentAssets(0.0);
				financialInputRequest.setOtheNonCurruntAssetFy((assetsDetails.getNonConsumableStoreAndSpares() + assetsDetails.getOtherNonCurrentAssets()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInventory()))
					assetsDetails.setInventory(0.0);
				financialInputRequest.setInventoriesFy(assetsDetails.getInventory() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getReceivableOtherThanDefferred()))
					assetsDetails.setReceivableOtherThanDefferred(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getExportReceivables()))
					assetsDetails.setExportReceivables(0.0);
				financialInputRequest.setSundryDebtorsFy((assetsDetails.getReceivableOtherThanDefferred() + assetsDetails.getExportReceivables()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getCashAndBankBalance()))
					assetsDetails.setCashAndBankBalance(0.0);
				financialInputRequest.setCashAndBankFy(assetsDetails.getCashAndBankBalance() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInvestments()))
					assetsDetails.setInvestments(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInstalmentsDeferred()))
					assetsDetails.setInstalmentsDeferred(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherCurrentAssets()))
					assetsDetails.setOtherCurrentAssets(0.0);
				financialInputRequest.setOtherCurruntAssetFy((assetsDetails.getInvestments() + assetsDetails.getInstalmentsDeferred() + assetsDetails.getOtherCurrentAssets()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvanceToSupplierRawMaterials()))
					assetsDetails.setAdvanceToSupplierRawMaterials(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvancePaymentTaxes()))
					assetsDetails.setAdvancePaymentTaxes(0.0);
				financialInputRequest.setShortTermLoansAdvancesFy((assetsDetails.getAdvanceToSupplierRawMaterials() + assetsDetails.getAdvancePaymentTaxes()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherIncomeNeedTocCheckAsset()))
					assetsDetails.setOtherIncomeNeedTocCheckAsset(0.0);
				financialInputRequest.setOtherIncomeNeedTocCheckAssetFy(assetsDetails.getOtherIncomeNeedTocCheckAsset() * denom);
				
				// -----CONTIGENT LIABILITIES
				if(corporateFinalInfoRequest == null)
					financialInputRequest.setContingentLiablitiesFy(null);
				else
					financialInputRequest.setContingentLiablitiesFy(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilityFyAmt()) ? 0.0 : (corporateFinalInfoRequest.getContLiabilityFyAmt()* denom));
				
				
			}else {
				
				log.error("first year asset details is null");
				
			}
			
		} catch (Exception e) {
			log.error("error while getting first year asset details : ",e);
		}
		
		
		//----------------------------------------------------------------SECOND YEAR DATA---------------------------------------------------------------------
		//========= ================================================OPERATINGSTATEMENT DETAIL 2 YR=========================================================
		operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByProposal(proposalMapId, currentYear-2+"");
			
		
		try {
			
			if(operatingStatementDetails != null) {
				
				log.info(MSG_APP_ID+aplicationId + MSG_CURRENT_YEAR_1+(currentYear-1) + OPERATING_STATEMENT_DETAILS_GET_DEPRECIATION+operatingStatementDetails.getDepreciation() );
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails)){
					operatingStatementDetails = new OperatingStatementDetails();
				}
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDomesticSales()))
					operatingStatementDetails.setDomesticSales(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDomesticSales()))
					operatingStatementDetails.setExportSales(0.0);		
				financialInputRequest.setGrossSalesSy((operatingStatementDetails.getDomesticSales()+operatingStatementDetails.getExportSales()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getLessExciseDuty()))
					operatingStatementDetails.setLessExciseDuty(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductOtherItems()))
					operatingStatementDetails.setDeductOtherItems(0.0);	
				financialInputRequest.setLessExciseDuitySy((operatingStatementDetails.getLessExciseDuty()+operatingStatementDetails.getDeductOtherItems()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOperatingStock()))
					operatingStatementDetails.setAddOperatingStock(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductStockInProcess()))
					operatingStatementDetails.setDeductStockInProcess(0.0);	
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOperatingStockFg()))
					operatingStatementDetails.setAddOperatingStockFg(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductClStockFg()))
					operatingStatementDetails.setDeductClStockFg(0.0);	
				financialInputRequest.setIncreaseDecreaseStockSy(((operatingStatementDetails.getAddOperatingStock()-operatingStatementDetails.getDeductStockInProcess()) + (operatingStatementDetails.getAddOperatingStockFg()-operatingStatementDetails.getDeductClStockFg())) * denom);
				
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getRawMaterials()))
					operatingStatementDetails.setRawMaterials(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherSpares()))
					operatingStatementDetails.setOtherSpares(0.0);	
				financialInputRequest.setRawMaterialConsumedSy((operatingStatementDetails.getRawMaterials()+operatingStatementDetails.getOtherSpares()) * denom);
				
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getPowerAndFuel()))
					operatingStatementDetails.setPowerAndFuel(0.0);
				financialInputRequest.setPowerAndFuelCostSy(operatingStatementDetails.getPowerAndFuel() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDirectLabour()))
					operatingStatementDetails.setDirectLabour(0.0);
				financialInputRequest.setEmployeeCostSy(operatingStatementDetails.getDirectLabour() * denom);

				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getSellingGenlAdmnExpenses()))
					operatingStatementDetails.setSellingGenlAdmnExpenses(0.0);
				financialInputRequest.setGeneralAndAdminExpeSy(operatingStatementDetails.getSellingGenlAdmnExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getSellingAndDistributionExpenses()))
					operatingStatementDetails.setSellingAndDistributionExpenses(0.0);
				financialInputRequest.setSellingAndDistriExpeSy(operatingStatementDetails.getSellingAndDistributionExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getExpensesAmortised()))
					operatingStatementDetails.setExpensesAmortised(0.0);
				financialInputRequest.setLessExpeCapitaSy(operatingStatementDetails.getExpensesAmortised() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherMfgExpenses()))
					operatingStatementDetails.setOtherMfgExpenses(0.0);
				financialInputRequest.setMiscelExpeSy(operatingStatementDetails.getOtherMfgExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOtherRevenueIncome()))
					operatingStatementDetails.setAddOtherRevenueIncome(0.0);
				financialInputRequest.setOtherIncomeSy(operatingStatementDetails.getAddOtherRevenueIncome() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getInterest()))
					operatingStatementDetails.setInterest(0.0);
				financialInputRequest.setInterestSy(operatingStatementDetails.getInterest() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDepreciation()))
					operatingStatementDetails.setDepreciation(0.0);
				financialInputRequest.setDepriciationSy(operatingStatementDetails.getDepreciation() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getNetofNonOpIncomeOrExpenses()))
					operatingStatementDetails.setNetofNonOpIncomeOrExpenses(0.0);
				financialInputRequest.setExceptionalIncomeSy(operatingStatementDetails.getNetofNonOpIncomeOrExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getProvisionForTaxes()))
					operatingStatementDetails.setProvisionForTaxes(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getProvisionForDeferredTax()))
					operatingStatementDetails.setProvisionForDeferredTax(0.0);
				financialInputRequest.setProvisionForTaxSy((operatingStatementDetails.getProvisionForTaxes() + operatingStatementDetails.getProvisionForDeferredTax()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherIncomeNeedTocCheckOp()))
					operatingStatementDetails.setOtherIncomeNeedTocCheckOp(0.0);
				financialInputRequest.setOtherIncomeNeedTocCheckOpSy(operatingStatementDetails.getOtherIncomeNeedTocCheckOp() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getEquityDeividendPaidAmt()))
					operatingStatementDetails.setEquityDeividendPaidAmt(0.0);
				financialInputRequest.setDividendPayOutSy(operatingStatementDetails.getEquityDeividendPaidAmt() * denom);		
				
			}else {
				
				log.error("second year data is null of operating Statement");
				
			}
			
		} catch (Exception e) {
				log.error("error while fetching second year operating data : ",e);
		}
		
		

		//========= ===============================================LIABILITIES DETAIL 2 YR==================================================================
		
		
		try {
			
			liabilitiesDetails = liabilitiesDetailsRepository.getLiabilitiesDetailByProposal(proposalMapId, currentYear-2+"");
			
			if(liabilitiesDetails != null) {
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails)){
					liabilitiesDetails = new LiabilitiesDetails();
				}
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getPreferencesShares()))
					liabilitiesDetails.setPreferencesShares(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOrdinarySharesCapital()))
					liabilitiesDetails.setOrdinarySharesCapital(0.0);
				financialInputRequest.setShareCapitalSy((liabilitiesDetails.getPreferencesShares() + liabilitiesDetails.getOrdinarySharesCapital()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getShareWarrentsOutstanding()))
					liabilitiesDetails.setShareWarrentsOutstanding(0.0);
				financialInputRequest.setShareWarrantOutstandingsSy(liabilitiesDetails.getShareWarrentsOutstanding() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getRevaluationReservse()))
					liabilitiesDetails.setRevaluationReservse(0.0);
				financialInputRequest.setRevalationReserveSy(liabilitiesDetails.getRevaluationReservse() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getGeneralReserve()))
					liabilitiesDetails.setGeneralReserve(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherReservse()))
					liabilitiesDetails.setOtherReservse(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSurplusOrDeficit()))
					liabilitiesDetails.setSurplusOrDeficit(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOthers()))
					liabilitiesDetails.setOthers(0.0);
				financialInputRequest.setOtherReserveAndSurplusSy((liabilitiesDetails.getGeneralReserve() + liabilitiesDetails.getOtherReservse() + liabilitiesDetails.getSurplusOrDeficit() + liabilitiesDetails.getOthers()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getMinorityInterest()))
					liabilitiesDetails.setMinorityInterest(0.0);
				financialInputRequest.setMinorityInterestSy(liabilitiesDetails.getMinorityInterest() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermLiabilitiesSecured()))
					liabilitiesDetails.setTermLiabilitiesSecured(0.0);
				financialInputRequest.setSecuredLoansSy(liabilitiesDetails.getTermLiabilitiesSecured() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclUnsecuredLoansFromPromoters()))
					liabilitiesDetails.setOtherNclUnsecuredLoansFromPromoters(0.0);
				financialInputRequest.setUnsecuredLoansPromotersSy(liabilitiesDetails.getOtherNclUnsecuredLoansFromPromoters() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclUnsecuredLoansFromOther()))
					liabilitiesDetails.setOtherNclUnsecuredLoansFromOther(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermLiabilitiesUnsecured()))
					liabilitiesDetails.setTermLiabilitiesUnsecured(0.0);
				financialInputRequest.setUnsecuredLoansOthersSy((liabilitiesDetails.getOtherNclUnsecuredLoansFromOther() + liabilitiesDetails.getTermLiabilitiesUnsecured()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSubTotalA()))
					liabilitiesDetails.setSubTotalA(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getShortTermBorrowingFromOthers()))
					liabilitiesDetails.setShortTermBorrowingFromOthers(0.0);
				financialInputRequest.setOtherBorrowingSy((liabilitiesDetails.getSubTotalA() + liabilitiesDetails.getShortTermBorrowingFromOthers()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDeferredTaxLiability()))
					liabilitiesDetails.setDeferredTaxLiability(0.0);
				financialInputRequest.setDeferredTaxLiablitiesSy(liabilitiesDetails.getDeferredTaxLiability() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclOthers()))
					liabilitiesDetails.setOtherNclOthers(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDeferredPaymentsCredits()))
					liabilitiesDetails.setDeferredPaymentsCredits(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermDeposits()))
					liabilitiesDetails.setTermDeposits(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDebentures()))
					liabilitiesDetails.setDebentures(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherTermLiabilies()))
					liabilitiesDetails.setOtherTermLiabilies(0.0);
				financialInputRequest.setOtherLongTermLiablitiesSy((liabilitiesDetails.getOtherNclOthers() + liabilitiesDetails.getDeferredPaymentsCredits() + liabilitiesDetails.getTermDeposits() + liabilitiesDetails.getDebentures() + liabilitiesDetails.getOtherTermLiabilies()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclLongTermProvisions()))
					liabilitiesDetails.setOtherNclLongTermProvisions(0.0);
				financialInputRequest.setLongTermProvisionSy(liabilitiesDetails.getOtherNclLongTermProvisions() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSundryCreditors()))
					liabilitiesDetails.setSundryCreditors(0.0);
				financialInputRequest.setTradePayablesSy(liabilitiesDetails.getSundryCreditors() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getAdvancePaymentsFromCustomers()))
					liabilitiesDetails.setAdvancePaymentsFromCustomers(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDividendPayable()))
					liabilitiesDetails.setDividendPayable(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherStatutoryLiability()))
					liabilitiesDetails.setOtherStatutoryLiability(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherCurrentLiability()))
					liabilitiesDetails.setOtherCurrentLiability(0.0);
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans()))
					liabilitiesDetails.setDepositsOrInstalmentsOfTermLoans(0.0);
				financialInputRequest.setOtherCurruntLiablitiesSy((liabilitiesDetails.getAdvancePaymentsFromCustomers() + liabilitiesDetails.getDividendPayable() + liabilitiesDetails.getOtherStatutoryLiability() + liabilitiesDetails.getOtherCurrentLiability() + liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherIncomeNeedTocCheckLia()))
					liabilitiesDetails.setOtherIncomeNeedTocCheckLia(0.0);
				financialInputRequest.setOtherIncomeNeedTocCheckLiaSy(liabilitiesDetails.getOtherIncomeNeedTocCheckLia() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getProvisionalForTaxation()))
					liabilitiesDetails.setProvisionalForTaxation(0.0);
				financialInputRequest.setShortTermProvisionSy(liabilitiesDetails.getProvisionalForTaxation() * denom);
				
			}else {
				
				log.error("2nd year liability data is null");
				
			}
			
		} catch (Exception e) {
			log.error("error while fetching 2nd year liability data : ",e);
		}
		
		
		
		//========= ===============================================ASSET DETAIL 2 YR==================================================================
		
		
		try {
			
			assetsDetails = assetsDetailsRepository.getAssetsDetailByProposal(proposalMapId, currentYear-2+"");
			
			if(assetsDetails != null) {
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails)){
					assetsDetails = new AssetsDetails();
				}
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getGrossBlock()))
					assetsDetails.setGrossBlock(0.0);
				financialInputRequest.setGrossBlockSy(assetsDetails.getGrossBlock() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getDepreciationToDate()))
					assetsDetails.setDepreciationToDate(0.0);
				financialInputRequest.setLessAccumulatedDepreSy(assetsDetails.getDepreciationToDate() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getImpairmentAsset()))
					assetsDetails.setImpairmentAsset(0.0);
				financialInputRequest.setImpairmentofAssetSy(assetsDetails.getImpairmentAsset() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherNcaOtherCapitalWorkInprogress()))
					assetsDetails.setOtherNcaOtherCapitalWorkInprogress(0.0);
				financialInputRequest.setCapitalWorkInProgressSy(assetsDetails.getOtherNcaOtherCapitalWorkInprogress() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getIntangibleAssets()))
					assetsDetails.setIntangibleAssets(0.0);
				financialInputRequest.setIntengibleAssetsSy(assetsDetails.getIntangibleAssets() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersPreOperativeExpensesPending()))
					assetsDetails.setOthersPreOperativeExpensesPending(0.0);
				financialInputRequest.setPreOperativeExpeSy(assetsDetails.getOthersPreOperativeExpensesPending() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersAssetsInTransit()))
					assetsDetails.setOthersAssetsInTransit(0.0);
				financialInputRequest.setAssetInTransitSy(assetsDetails.getOthersAssetsInTransit() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInvestmentsInSubsidiary()))
					assetsDetails.setInvestmentsInSubsidiary(0.0);
				financialInputRequest.setInvestmentInSubsidiariesSy(assetsDetails.getInvestmentsInSubsidiary() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersOther()))
					assetsDetails.setOthersOther(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getDeferredReceviables()))
					assetsDetails.setDeferredReceviables(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthers()))
					assetsDetails.setOthers(0.0);
				financialInputRequest.setOtherInvestmentSy((assetsDetails.getOthersOther() + assetsDetails.getDeferredReceviables() + assetsDetails.getOthers()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvanceToSuppliersCapitalGoods()))
					assetsDetails.setAdvanceToSuppliersCapitalGoods(0.0);
				financialInputRequest.setLongTermLoansAndAdvaSy(assetsDetails.getAdvanceToSuppliersCapitalGoods() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getNonConsumableStoreAndSpares()))
					assetsDetails.setNonConsumableStoreAndSpares(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherNonCurrentAssets()))
					assetsDetails.setOtherNonCurrentAssets(0.0);
				financialInputRequest.setOtheNonCurruntAssetSy((assetsDetails.getNonConsumableStoreAndSpares() + assetsDetails.getOtherNonCurrentAssets()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInventory()))
					assetsDetails.setInventory(0.0);
				financialInputRequest.setInventoriesSy(assetsDetails.getInventory() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getReceivableOtherThanDefferred()))
					assetsDetails.setReceivableOtherThanDefferred(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getExportReceivables()))
					assetsDetails.setExportReceivables(0.0);
				financialInputRequest.setSundryDebtorsSy((assetsDetails.getReceivableOtherThanDefferred() + assetsDetails.getExportReceivables()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getCashAndBankBalance()))
					assetsDetails.setCashAndBankBalance(0.0);
				financialInputRequest.setCashAndBankSy(assetsDetails.getCashAndBankBalance() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInvestments()))
					assetsDetails.setInvestments(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInstalmentsDeferred()))
					assetsDetails.setInstalmentsDeferred(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherCurrentAssets()))
					assetsDetails.setOtherCurrentAssets(0.0);
				financialInputRequest.setOtherCurruntAssetSy((assetsDetails.getInvestments() + assetsDetails.getInstalmentsDeferred() + assetsDetails.getOtherCurrentAssets()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvanceToSupplierRawMaterials()))
					assetsDetails.setAdvanceToSupplierRawMaterials(0.0);
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvancePaymentTaxes()))
					assetsDetails.setAdvancePaymentTaxes(0.0);
				financialInputRequest.setShortTermLoansAdvancesSy((assetsDetails.getAdvanceToSupplierRawMaterials() + assetsDetails.getAdvancePaymentTaxes()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherIncomeNeedTocCheckAsset()))
					assetsDetails.setOtherIncomeNeedTocCheckAsset(0.0);
				financialInputRequest.setOtherIncomeNeedTocCheckAssetSy(assetsDetails.getOtherIncomeNeedTocCheckAsset() * denom);
				// -----CONTIGENT LIABILITIES
				if(corporateFinalInfoRequest == null)
				{
					financialInputRequest.setContingentLiablitiesSy(null);
				}
				else {
					financialInputRequest.setContingentLiablitiesSy(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilitySyAmt()) ? 0.0 : (corporateFinalInfoRequest.getContLiabilitySyAmt() * denom));
				}
				
			}else {
				log.error("2nd year asset data is null");
			}
			
		} catch (Exception e) {
			log.error("error while fetching 2nd year data asset : ",e);
		}
		
		
		
		
		// ----------------------------------------FIRST YEAR DATA---------------------------------------------------------------------------------------
		//========= ==========================================OPERATINGSTATEMENT DETAIL 1 YR========================================================
		
		
		
		try {
			
			operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByProposal(proposalMapId, currentYear-3+"");
			
			if(operatingStatementDetails != null) {
				
				log.info(MSG_APP_ID+ aplicationId + MSG_CURRENT_YEAR_1 +(currentYear-1));
				if(operatingStatementDetails!=null) {
				log.info(OPERATING_STATEMENT_DETAILS_GET_DEPRECIATION+operatingStatementDetails.getDepreciation());
				}else {
					log.info("operatingStatementDetails is:: NULL");
				}
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails)){
					operatingStatementDetails = new OperatingStatementDetails();
					operatingStatementDetails.setDepreciation(0.0);
				}
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDomesticSales()))
					operatingStatementDetails.setDomesticSales(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDomesticSales()))
					operatingStatementDetails.setExportSales(0.0);		
				financialInputRequest.setGrossSalesTy((operatingStatementDetails.getDomesticSales()+operatingStatementDetails.getExportSales()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getLessExciseDuty()))
					operatingStatementDetails.setLessExciseDuty(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductOtherItems()))
					operatingStatementDetails.setDeductOtherItems(0.0);	
				financialInputRequest.setLessExciseDuityTy((operatingStatementDetails.getLessExciseDuty()+operatingStatementDetails.getDeductOtherItems()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOperatingStock()))
					operatingStatementDetails.setAddOperatingStock(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductStockInProcess()))
					operatingStatementDetails.setDeductStockInProcess(0.0);	
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOperatingStockFg()))
					operatingStatementDetails.setAddOperatingStockFg(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDeductClStockFg()))
					operatingStatementDetails.setDeductClStockFg(0.0);	
				financialInputRequest.setIncreaseDecreaseStockTy(((operatingStatementDetails.getAddOperatingStock()-operatingStatementDetails.getDeductStockInProcess()) + (operatingStatementDetails.getAddOperatingStockFg()-operatingStatementDetails.getDeductClStockFg())) * denom);
				
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getRawMaterials()))
					operatingStatementDetails.setRawMaterials(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherSpares()))
					operatingStatementDetails.setOtherSpares(0.0);	
				financialInputRequest.setRawMaterialConsumedTy((operatingStatementDetails.getRawMaterials()+operatingStatementDetails.getOtherSpares()) * denom);
				
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getPowerAndFuel()))
					operatingStatementDetails.setPowerAndFuel(0.0);
				financialInputRequest.setPowerAndFuelCostTy(operatingStatementDetails.getPowerAndFuel() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDirectLabour()))
					operatingStatementDetails.setDirectLabour(0.0);
				financialInputRequest.setEmployeeCostTy(operatingStatementDetails.getDirectLabour() * denom);

				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getSellingGenlAdmnExpenses()))
					operatingStatementDetails.setSellingGenlAdmnExpenses(0.0);
				financialInputRequest.setGeneralAndAdminExpeTy(operatingStatementDetails.getSellingGenlAdmnExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getSellingAndDistributionExpenses()))
					operatingStatementDetails.setSellingAndDistributionExpenses(0.0);
				financialInputRequest.setSellingAndDistriExpeTy(operatingStatementDetails.getSellingAndDistributionExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getExpensesAmortised()))
					operatingStatementDetails.setExpensesAmortised(0.0);
				financialInputRequest.setLessExpeCapitaTy(operatingStatementDetails.getExpensesAmortised() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherMfgExpenses()))
					operatingStatementDetails.setOtherMfgExpenses(0.0);
				financialInputRequest.setMiscelExpeTy(operatingStatementDetails.getOtherMfgExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getAddOtherRevenueIncome()))
					operatingStatementDetails.setAddOtherRevenueIncome(0.0);
				financialInputRequest.setOtherIncomeTy(operatingStatementDetails.getAddOtherRevenueIncome() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getInterest()))
					operatingStatementDetails.setInterest(0.0);
				financialInputRequest.setInterestTy(operatingStatementDetails.getInterest() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getDepreciation()))
					operatingStatementDetails.setDepreciation(0.0);
				financialInputRequest.setDepriciationTy(operatingStatementDetails.getDepreciation() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getNetofNonOpIncomeOrExpenses()))
					operatingStatementDetails.setNetofNonOpIncomeOrExpenses(0.0);
				financialInputRequest.setExceptionalIncomeTy(operatingStatementDetails.getNetofNonOpIncomeOrExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getProvisionForTaxes()))
					operatingStatementDetails.setProvisionForTaxes(0.0);
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getProvisionForDeferredTax()))
					operatingStatementDetails.setProvisionForDeferredTax(0.0);
				financialInputRequest.setProvisionForTaxTy((operatingStatementDetails.getProvisionForTaxes() + operatingStatementDetails.getProvisionForDeferredTax()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getOtherIncomeNeedTocCheckOp()))
					operatingStatementDetails.setOtherIncomeNeedTocCheckOp(0.0);
				financialInputRequest.setOtherIncomeNeedTocCheckOpTy(operatingStatementDetails.getOtherIncomeNeedTocCheckOp() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getEquityDeividendPaidAmt()))
					operatingStatementDetails.setEquityDeividendPaidAmt(0.0);
				financialInputRequest.setDividendPayOutTy(operatingStatementDetails.getEquityDeividendPaidAmt() * denom);
				
				
			}else {
				
				log.error("os data is null");
				
			}
			
			
		} catch (Exception e) {
			log.error("error while fetching os data : ",e);
		}
		
		
		//========= ===============================================LIABILITIES DETAIL 1 YR==================================================================
				
				
				try {
					
					liabilitiesDetails = liabilitiesDetailsRepository.getLiabilitiesDetailByProposal(proposalMapId, currentYear-3+"");
					
					if(liabilitiesDetails != null) {
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails)){
							liabilitiesDetails = new LiabilitiesDetails();
						}
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getPreferencesShares()))
							liabilitiesDetails.setPreferencesShares(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOrdinarySharesCapital()))
							liabilitiesDetails.setOrdinarySharesCapital(0.0);
						financialInputRequest.setShareCapitalTy((liabilitiesDetails.getPreferencesShares() + liabilitiesDetails.getOrdinarySharesCapital()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getShareWarrentsOutstanding()))
							liabilitiesDetails.setShareWarrentsOutstanding(0.0);
						financialInputRequest.setShareWarrantOutstandingsTy(liabilitiesDetails.getShareWarrentsOutstanding() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getRevaluationReservse()))
							liabilitiesDetails.setRevaluationReservse(0.0);
						financialInputRequest.setRevalationReserveTy(liabilitiesDetails.getRevaluationReservse() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getGeneralReserve()))
							liabilitiesDetails.setGeneralReserve(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherReservse()))
							liabilitiesDetails.setOtherReservse(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSurplusOrDeficit()))
							liabilitiesDetails.setSurplusOrDeficit(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOthers()))
							liabilitiesDetails.setOthers(0.0);
						financialInputRequest.setOtherReserveAndSurplusTy((liabilitiesDetails.getGeneralReserve() + liabilitiesDetails.getOtherReservse() + liabilitiesDetails.getSurplusOrDeficit() + liabilitiesDetails.getOthers()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getMinorityInterest()))
							liabilitiesDetails.setMinorityInterest(0.0);
						financialInputRequest.setMinorityInterestTy(liabilitiesDetails.getMinorityInterest() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermLiabilitiesSecured()))
							liabilitiesDetails.setTermLiabilitiesSecured(0.0);
						financialInputRequest.setSecuredLoansTy(liabilitiesDetails.getTermLiabilitiesSecured() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclUnsecuredLoansFromPromoters()))
							liabilitiesDetails.setOtherNclUnsecuredLoansFromPromoters(0.0);
						financialInputRequest.setUnsecuredLoansPromotersTy(liabilitiesDetails.getOtherNclUnsecuredLoansFromPromoters() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclUnsecuredLoansFromOther()))
							liabilitiesDetails.setOtherNclUnsecuredLoansFromOther(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermLiabilitiesUnsecured()))
							liabilitiesDetails.setTermLiabilitiesUnsecured(0.0);
						financialInputRequest.setUnsecuredLoansOthersTy((liabilitiesDetails.getOtherNclUnsecuredLoansFromOther() + liabilitiesDetails.getTermLiabilitiesUnsecured()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSubTotalA()))
							liabilitiesDetails.setSubTotalA(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getShortTermBorrowingFromOthers()))
							liabilitiesDetails.setShortTermBorrowingFromOthers(0.0);
						financialInputRequest.setOtherBorrowingTy((liabilitiesDetails.getSubTotalA() + liabilitiesDetails.getShortTermBorrowingFromOthers()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDeferredTaxLiability()))
							liabilitiesDetails.setDeferredTaxLiability(0.0);
						financialInputRequest.setDeferredTaxLiablitiesTy(liabilitiesDetails.getDeferredTaxLiability() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclOthers()))
							liabilitiesDetails.setOtherNclOthers(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDeferredPaymentsCredits()))
							liabilitiesDetails.setDeferredPaymentsCredits(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getTermDeposits()))
							liabilitiesDetails.setTermDeposits(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDebentures()))
							liabilitiesDetails.setDebentures(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherTermLiabilies()))
							liabilitiesDetails.setOtherTermLiabilies(0.0);
						financialInputRequest.setOtherLongTermLiablitiesTy((liabilitiesDetails.getOtherNclOthers() + liabilitiesDetails.getDeferredPaymentsCredits() + liabilitiesDetails.getTermDeposits() + liabilitiesDetails.getDebentures() + liabilitiesDetails.getOtherTermLiabilies()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherNclLongTermProvisions()))
							liabilitiesDetails.setOtherNclLongTermProvisions(0.0);
						financialInputRequest.setLongTermProvisionTy(liabilitiesDetails.getOtherNclLongTermProvisions() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getSundryCreditors()))
							liabilitiesDetails.setSundryCreditors(0.0);
						financialInputRequest.setTradePayablesTy(liabilitiesDetails.getSundryCreditors() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getAdvancePaymentsFromCustomers()))
							liabilitiesDetails.setAdvancePaymentsFromCustomers(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDividendPayable()))
							liabilitiesDetails.setDividendPayable(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherStatutoryLiability()))
							liabilitiesDetails.setOtherStatutoryLiability(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherCurrentLiability()))
							liabilitiesDetails.setOtherCurrentLiability(0.0);
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans()))
							liabilitiesDetails.setDepositsOrInstalmentsOfTermLoans(0.0);
						financialInputRequest.setOtherCurruntLiablitiesTy((liabilitiesDetails.getAdvancePaymentsFromCustomers() + liabilitiesDetails.getDividendPayable() + liabilitiesDetails.getOtherStatutoryLiability() + liabilitiesDetails.getOtherCurrentLiability() + liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getOtherIncomeNeedTocCheckLia()))
							liabilitiesDetails.setOtherIncomeNeedTocCheckLia(0.0);
						financialInputRequest.setOtherIncomeNeedTocCheckLiaTy(liabilitiesDetails.getOtherIncomeNeedTocCheckLia() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails.getProvisionalForTaxation()))
							liabilitiesDetails.setProvisionalForTaxation(0.0);
						financialInputRequest.setShortTermProvisionTy(liabilitiesDetails.getProvisionalForTaxation() * denom);
						
					}else {
						log.error("liability data is null");
					}
					
				} catch (Exception e) {
					log.error("error while fetching liability data : ",e);
				}
				
				
				
				
				//========= ===============================================ASSET DETAIL 1 YR==================================================================
				
				
				try {
					
					assetsDetails = assetsDetailsRepository.getAssetsDetailByProposal(proposalMapId, currentYear-3+"");
					
					if(assetsDetails != null) {
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails)){
							assetsDetails = new AssetsDetails();
						}
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getGrossBlock()))
							assetsDetails.setGrossBlock(0.0);
						financialInputRequest.setGrossBlockTy(assetsDetails.getGrossBlock() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getDepreciationToDate()))
							assetsDetails.setDepreciationToDate(0.0);
						financialInputRequest.setLessAccumulatedDepreTy(assetsDetails.getDepreciationToDate() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getImpairmentAsset()))
							assetsDetails.setImpairmentAsset(0.0);
						financialInputRequest.setImpairmentofAssetTy(assetsDetails.getImpairmentAsset() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherNcaOtherCapitalWorkInprogress()))
							assetsDetails.setOtherNcaOtherCapitalWorkInprogress(0.0);
						financialInputRequest.setCapitalWorkInProgressTy(assetsDetails.getOtherNcaOtherCapitalWorkInprogress() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getIntangibleAssets()))
							assetsDetails.setIntangibleAssets(0.0);
						financialInputRequest.setIntengibleAssetsTy(assetsDetails.getIntangibleAssets() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersPreOperativeExpensesPending()))
							assetsDetails.setOthersPreOperativeExpensesPending(0.0);
						financialInputRequest.setPreOperativeExpeTy(assetsDetails.getOthersPreOperativeExpensesPending() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersAssetsInTransit()))
							assetsDetails.setOthersAssetsInTransit(0.0);
						financialInputRequest.setAssetInTransitTy(assetsDetails.getOthersAssetsInTransit() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInvestmentsInSubsidiary()))
							assetsDetails.setInvestmentsInSubsidiary(0.0);
						financialInputRequest.setInvestmentInSubsidiariesTy(assetsDetails.getInvestmentsInSubsidiary() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthersOther()))
							assetsDetails.setOthersOther(0.0);
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getDeferredReceviables()))
							assetsDetails.setDeferredReceviables(0.0);
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOthers()))
							assetsDetails.setOthers(0.0);
						financialInputRequest.setOtherInvestmentTy((assetsDetails.getOthersOther() + assetsDetails.getDeferredReceviables() + assetsDetails.getOthers()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvanceToSuppliersCapitalGoods()))
							assetsDetails.setAdvanceToSuppliersCapitalGoods(0.0);
						financialInputRequest.setLongTermLoansAndAdvaTy(assetsDetails.getAdvanceToSuppliersCapitalGoods() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getNonConsumableStoreAndSpares()))
							assetsDetails.setNonConsumableStoreAndSpares(0.0);
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherNonCurrentAssets()))
							assetsDetails.setOtherNonCurrentAssets(0.0);
						financialInputRequest.setOtheNonCurruntAssetTy((assetsDetails.getNonConsumableStoreAndSpares() + assetsDetails.getOtherNonCurrentAssets()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInventory()))
							assetsDetails.setInventory(0.0);
						financialInputRequest.setInventoriesTy(assetsDetails.getInventory() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getReceivableOtherThanDefferred()))
							assetsDetails.setReceivableOtherThanDefferred(0.0);
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getExportReceivables()))
							assetsDetails.setExportReceivables(0.0);
						financialInputRequest.setSundryDebtorsTy((assetsDetails.getReceivableOtherThanDefferred() + assetsDetails.getExportReceivables()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getCashAndBankBalance()))
							assetsDetails.setCashAndBankBalance(0.0);
						financialInputRequest.setCashAndBankTy(assetsDetails.getCashAndBankBalance() * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInvestments()))
							assetsDetails.setInvestments(0.0);
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getInstalmentsDeferred()))
							assetsDetails.setInstalmentsDeferred(0.0);
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherCurrentAssets()))
							assetsDetails.setOtherCurrentAssets(0.0);
						financialInputRequest.setOtherCurruntAssetTy((assetsDetails.getInvestments() + assetsDetails.getInstalmentsDeferred() + assetsDetails.getOtherCurrentAssets()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvanceToSupplierRawMaterials()))
							assetsDetails.setAdvanceToSupplierRawMaterials(0.0);
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getAdvancePaymentTaxes()))
							assetsDetails.setAdvancePaymentTaxes(0.0);
						financialInputRequest.setShortTermLoansAdvancesTy((assetsDetails.getAdvanceToSupplierRawMaterials() + assetsDetails.getAdvancePaymentTaxes()) * denom);
						
						if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getOtherIncomeNeedTocCheckAsset()))
							assetsDetails.setOtherIncomeNeedTocCheckAsset(0.0);
						financialInputRequest.setOtherIncomeNeedTocCheckAssetTy(assetsDetails.getOtherIncomeNeedTocCheckAsset() * denom);
						
						// -----CONTIGENT LIABILITIES
				if(corporateFinalInfoRequest == null)
					financialInputRequest.setContingentLiablitiestTy(null);
				else
					financialInputRequest.setContingentLiablitiestTy(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilityTyAmt()) ? 0.0 : (corporateFinalInfoRequest.getContLiabilityTyAmt() * denom));
						
					}else {
						log.error("asset details is null");
					}
					
				} catch (Exception e) {
					log.error("error while fetching asset data : ",e);
				}
				
				
				
				
		// FinancialInput Object Set
				if(financialInputRequest != null) {
					
					irrRequest.setFinancialInputRequest(financialInputRequest);
					
				}
		
		//jSONObject.put("irrRequest",irrRequest);

		log.info("financialInputRequest.getDepriciationFy()::"+financialInputRequest.getDepriciationFy());
		log.info("financialInputRequest.getDepriciationSy()::"+financialInputRequest.getDepriciationSy());
		log.info("financialInputRequest.getDepriciationTy()::"+financialInputRequest.getDepriciationTy());

		log.info("financialInputRequest.getPowerAndFuelCostFy()::"+financialInputRequest.getPowerAndFuelCostFy());
		log.info("financialInputRequest.getPowerAndFuelCostSy()::"+financialInputRequest.getPowerAndFuelCostSy());
		log.info("financialInputRequest.getPowerAndFuelCostTy()::"+financialInputRequest.getPowerAndFuelCostTy());

		return financialInputRequest;
	}

	@Override
	public FinancialInputRequest coActIrrMappingService(Long userId, Long aplicationId,String industry,Long denom) throws Exception {

		//JSONObject jSONObject = new JSONObject();
		IrrRequest irrRequest = new IrrRequest();
		FinancialInputRequest financialInputRequest = new FinancialInputRequest();
		ProfitibilityStatementDetail profitibilityStatementDetail = new ProfitibilityStatementDetail();
		BalanceSheetDetail balanceSheetDetail = new BalanceSheetDetail();
		int currentYear = scoringService.getFinYear(aplicationId);
		financialInputRequest.setRatioAnalysisFyFullDate("31-March-"+(currentYear-1));
		CorporateFinalInfoRequest corporateFinalInfoRequest = new CorporateFinalInfoRequest();
		corporateFinalInfoRequest = corporateFinalInfoService.get(userId, aplicationId);
		
		//---SHARE FACE VALUE SET-----
				financialInputRequest.setShareFaceValue(1.00); // ------CAlculation Remained
				/*LoanApplicationMaster applicationMaster = null;
				applicationMaster = loanApplicationRepository.findOne(aplicationId);
				LoanType type = CommonUtils.LoanType.getType(applicationMaster.getProductId());
				switch (type) {
				case WORKING_CAPITAL:
					PrimaryWorkingCapitalLoanDetail primaryWorkingCapitalLoanDetail = null;
					primaryWorkingCapitalLoanDetail = primaryWorkingCapitalLoanDetailRepository.findOne(aplicationId);
					financialInputRequest.setShareFaceValue(primaryWorkingCapitalLoanDetail.getSharePriceFace() * denom);
					break;
				case TERM_LOAN:
					PrimaryTermLoanDetail primaryTermLoanDetail = null;
					primaryTermLoanDetail = primaryTermLoanDetailRepository.findOne(aplicationId);
					financialInputRequest.setShareFaceValue(primaryTermLoanDetail.getSharePriceFace() * denom);
					break;
				case UNSECURED_LOAN :
					PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetail = null;
					primaryUnsecuredLoanDetail = primaryUnsecuredLoanDetailRepository.findOne(aplicationId);
					financialInputRequest.setShareFaceValue(primaryUnsecuredLoanDetail.getSharePriceFace() * denom);
					break;
				}*/

		// set industry
		financialInputRequest.setIndustryName(industry);
				
		financialInputRequest.setNoOfMonthTy(12.0);
		financialInputRequest.setNoOfMonthSy(12.0);
		financialInputRequest.setNoOfMonthFy(12.0);
		// ----------------------------------------THIRD YEAR DATA---------------------------------------------------------------------------------------
		//========= ==========================================PROFITIBILITYSTATEMENTDETAIL DETAIL 3 YR========================================================
		profitibilityStatementDetail = profitibilityStatementDetailRepository.getProfitibilityStatementDetail(aplicationId, currentYear-1+"");
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail)){
			profitibilityStatementDetail = new ProfitibilityStatementDetail();
		}
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getSales()))
			profitibilityStatementDetail.setSales(0.0);
		financialInputRequest.setGrossSalesFy(profitibilityStatementDetail.getSales() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getLessExciseDutyOrVatOrServiceTax()))
			profitibilityStatementDetail.setLessExciseDutyOrVatOrServiceTax(0.0);
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getLessAnyOtherItem()))
			profitibilityStatementDetail.setLessAnyOtherItem(0.0);
		financialInputRequest.setLessExciseDuityFy((profitibilityStatementDetail.getLessExciseDutyOrVatOrServiceTax() + profitibilityStatementDetail.getLessAnyOtherItem()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryFg()))
			profitibilityStatementDetail.setIncreaseOrDecreaseInInventoryFg(0.0);
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryWip()))
			profitibilityStatementDetail.setIncreaseOrDecreaseInInventoryWip(0.0);
		financialInputRequest.setIncreaseDecreaseStockFy((profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryFg() + profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryWip()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getCostRawMaterialConsumed()))
			profitibilityStatementDetail.setCostRawMaterialConsumed(0.0);
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getPurchasesStockTnTrade()))
			profitibilityStatementDetail.setPurchasesStockTnTrade(0.0);
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getStoreAndSpares()))
			profitibilityStatementDetail.setStoreAndSpares(0.0);
		financialInputRequest.setRawMaterialConsumedFy((profitibilityStatementDetail.getCostRawMaterialConsumed() + profitibilityStatementDetail.getPurchasesStockTnTrade()  + profitibilityStatementDetail.getStoreAndSpares()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getPowerAndFuel()))
			profitibilityStatementDetail.setPowerAndFuel(0.0);
		financialInputRequest.setPowerAndFuelCostFy(profitibilityStatementDetail.getPowerAndFuel() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getEmployeeBenefitExpenses()))
			profitibilityStatementDetail.setEmployeeBenefitExpenses(0.0);
		financialInputRequest.setEmployeeCostFy(profitibilityStatementDetail.getEmployeeBenefitExpenses() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getGeneralAdminExpenses()))
			profitibilityStatementDetail.setGeneralAdminExpenses(0.0);
		financialInputRequest.setGeneralAndAdminExpeFy(profitibilityStatementDetail.getGeneralAdminExpenses() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getSellingDistributionExpenses()))
			profitibilityStatementDetail.setSellingDistributionExpenses(0.0);
		financialInputRequest.setSellingAndDistriExpeFy(profitibilityStatementDetail.getSellingDistributionExpenses() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExpensesCapitalised()))
			profitibilityStatementDetail.setExpensesCapitalised(0.0);
		financialInputRequest.setLessExpeCapitaFy(profitibilityStatementDetail.getExpensesCapitalised() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getOtherPlsSpecify())) // ------ others pls specify
			profitibilityStatementDetail.setOtherPlsSpecify(0.0);
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExpensesCapitalised()))
			profitibilityStatementDetail.setExpensesCapitalised(0.0);
		financialInputRequest.setMiscelExpeFy((profitibilityStatementDetail.getOtherPlsSpecify() - profitibilityStatementDetail.getExpensesCapitalised()) * denom); //----------------others pls specify field
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getOtherOperatingRevenue()))
			profitibilityStatementDetail.setOtherOperatingRevenue(0.0);
		financialInputRequest.setOtherIncomeFy(profitibilityStatementDetail.getOtherOperatingRevenue() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getFinanceCost()))
			profitibilityStatementDetail.setFinanceCost(0.0);
		financialInputRequest.setInterestFy(profitibilityStatementDetail.getFinanceCost() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getDepreciationAndAmortisation()))
			profitibilityStatementDetail.setDepreciationAndAmortisation(0.0);
		financialInputRequest.setDepriciationFy(profitibilityStatementDetail.getDepreciationAndAmortisation() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getNonOperatingIncome()))
			profitibilityStatementDetail.setNonOperatingIncome(0.0);
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getNonOperatingExpenses()))
			profitibilityStatementDetail.setNonOperatingExpenses(0.0);
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExtraordinaryItems()))
			profitibilityStatementDetail.setExtraordinaryItems(0.0);
		financialInputRequest.setExceptionalIncomeFy((profitibilityStatementDetail.getNonOperatingIncome() + profitibilityStatementDetail.getNonOperatingExpenses() + profitibilityStatementDetail.getExtraordinaryItems()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getProvisionForTax()))
			profitibilityStatementDetail.setProvisionForTax(0.0);
		financialInputRequest.setProvisionForTaxFy(profitibilityStatementDetail.getProvisionForTax() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getDividend()))
			profitibilityStatementDetail.setDividend(0.0);
		financialInputRequest.setDividendPayOutFy(profitibilityStatementDetail.getDividend() * denom);
		//========= ==========================================LIABILITY(BS) DETAIL 3 YR========================================================
		balanceSheetDetail = balanceSheetDetailRepository.getBalanceSheetDetail(aplicationId, currentYear-1+"");
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail)){
			balanceSheetDetail = new BalanceSheetDetail();
		}
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOrdinaryShareCapital()))
			balanceSheetDetail.setOrdinaryShareCapital(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreferenceShareCapital()))
			balanceSheetDetail.setPreferenceShareCapital(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getShareApplicationPendingAllotment()))
			balanceSheetDetail.setShareApplicationPendingAllotment(0.0);
		financialInputRequest.setShareCapitalFy((balanceSheetDetail.getOrdinaryShareCapital() + balanceSheetDetail.getPreferenceShareCapital() + balanceSheetDetail.getShareApplicationPendingAllotment()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMoneyReceivedAgainstShareWarrants()))
			balanceSheetDetail.setMoneyReceivedAgainstShareWarrants(0.0);
		financialInputRequest.setShareWarrantOutstandingsFy(balanceSheetDetail.getMoneyReceivedAgainstShareWarrants() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getRevaluationReserve()))
			balanceSheetDetail.setRevaluationReserve(0.0);
		financialInputRequest.setRevalationReserveFy(balanceSheetDetail.getRevaluationReserve() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getReservesAndSurplus()))
			balanceSheetDetail.setReservesAndSurplus(0.0);
		financialInputRequest.setOtherReserveAndSurplusFy((balanceSheetDetail.getReservesAndSurplus() - balanceSheetDetail.getRevaluationReserve()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMinorityInterest()))
			balanceSheetDetail.setMinorityInterest(0.0);
		financialInputRequest.setMinorityInterestFy(balanceSheetDetail.getMinorityInterest() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermLoansSecured()))
			balanceSheetDetail.setTermLoansSecured(0.0);
		financialInputRequest.setSecuredLoansFy(balanceSheetDetail.getTermLoansSecured() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermLoansUnsecured()))
			balanceSheetDetail.setTermLoansUnsecured(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromOthers()))
			balanceSheetDetail.setUnsecuredLoansFromOthers(0.0);
		financialInputRequest.setUnsecuredLoansOthersFy((balanceSheetDetail.getTermLoansUnsecured() + balanceSheetDetail.getUnsecuredLoansFromOthers()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getShortTermBorrowings()))
			balanceSheetDetail.setShortTermBorrowings(0.0);
		financialInputRequest.setOtherBorrowingFy(balanceSheetDetail.getShortTermBorrowings() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromPromoters()))
			balanceSheetDetail.setUnsecuredLoansFromPromoters(0.0);
		financialInputRequest.setUnsecuredLoansPromotersFy(balanceSheetDetail.getUnsecuredLoansFromPromoters() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredTaxLiability()))
			balanceSheetDetail.setDeferredTaxLiability(0.0);
		financialInputRequest.setDeferredTaxLiablitiesFy(balanceSheetDetail.getDeferredTaxLiability() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredPaymentCredits()))
			balanceSheetDetail.setDeferredPaymentCredits(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermDeposits()))
			balanceSheetDetail.setTermDeposits(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersPlsSpecify())) // ------ others pls specify 
			balanceSheetDetail.setOthersPlsSpecify(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDebentures()))
			balanceSheetDetail.setDebentures(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromOthers()))
			balanceSheetDetail.setUnsecuredLoansFromOthers(0.0);
		financialInputRequest.setOtherLongTermLiablitiesFy(((balanceSheetDetail.getDeferredPaymentCredits() + balanceSheetDetail.getTermDeposits() + balanceSheetDetail.getOthersPlsSpecify() + balanceSheetDetail.getDebentures()) - balanceSheetDetail.getUnsecuredLoansFromOthers()) * denom); // ------ others
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getLongTermProvisions()))
			balanceSheetDetail.setLongTermProvisions(0.0);
		financialInputRequest.setLongTermProvisionFy(balanceSheetDetail.getLongTermProvisions() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTradePayables()))
			balanceSheetDetail.setTradePayables(0.0);
		financialInputRequest.setTradePayablesFy(balanceSheetDetail.getTradePayables() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAdvanceFromCustomers()))
			balanceSheetDetail.setAdvanceFromCustomers(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDividendPayable()))
			balanceSheetDetail.setDividendPayable(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getStatutoryLiabilityDues()))
			balanceSheetDetail.setStatutoryLiabilityDues(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDepositsAndInstallments()))
			balanceSheetDetail.setDepositsAndInstallments(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersTotals())) // ------ others totals
			balanceSheetDetail.setOthersTotals(0.0);
		financialInputRequest.setOtherCurruntLiablitiesFy((balanceSheetDetail.getAdvanceFromCustomers() + balanceSheetDetail.getDividendPayable() + balanceSheetDetail.getStatutoryLiabilityDues() + balanceSheetDetail.getDepositsAndInstallments() + balanceSheetDetail.getOthersTotals()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getProvisionForTax()))
			balanceSheetDetail.setProvisionForTax(0.0);
		financialInputRequest.setShortTermProvisionFy(balanceSheetDetail.getProvisionForTax() * denom);
		
		//========= ==========================================ASSET(BS) DETAIL 3 YR========================================================
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getGrossFixedAssets()))
			balanceSheetDetail.setGrossFixedAssets(0.0);
		financialInputRequest.setGrossBlockFy(balanceSheetDetail.getGrossFixedAssets() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDepreciationToDate()))
			balanceSheetDetail.setDepreciationToDate(0.0);
		financialInputRequest.setLessAccumulatedDepreFy(balanceSheetDetail.getDepreciationToDate() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getImpairmentsOfAssests()))
			balanceSheetDetail.setImpairmentsOfAssests(0.0);
		financialInputRequest.setImpairmentofAssetFy(balanceSheetDetail.getImpairmentsOfAssests() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCapitalWorkInProgress()))
			balanceSheetDetail.setCapitalWorkInProgress(0.0);
		financialInputRequest.setCapitalWorkInProgressFy(balanceSheetDetail.getCapitalWorkInProgress() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getIntangibleAssets()))
			balanceSheetDetail.setIntangibleAssets(0.0);
		financialInputRequest.setIntengibleAssetsFy(balanceSheetDetail.getIntangibleAssets() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreOperativeExpensesPending()))
			balanceSheetDetail.setPreOperativeExpensesPending(0.0);
		financialInputRequest.setPreOperativeExpeFy(balanceSheetDetail.getPreOperativeExpensesPending() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAssetsInTransit()))
			balanceSheetDetail.setAssetsInTransit(0.0);
		financialInputRequest.setAssetInTransitFy(balanceSheetDetail.getAssetsInTransit() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInSubsidiaries()))
			balanceSheetDetail.setInvestmentInSubsidiaries(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInAssociates()))
			balanceSheetDetail.setInvestmentInAssociates(0.0);
		financialInputRequest.setInvestmentInSubsidiariesFy((balanceSheetDetail.getInvestmentInSubsidiaries() + balanceSheetDetail.getInvestmentInAssociates()) * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInQuoted()))
			balanceSheetDetail.setInvestmentInQuoted(0.0);
		financialInputRequest.setOtherInvestmentFy(balanceSheetDetail.getInvestmentInQuoted() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getLongTermLoansAndAdvance()))
			balanceSheetDetail.setLongTermLoansAndAdvance(0.0);
		financialInputRequest.setLongTermLoansAndAdvaFy(balanceSheetDetail.getLongTermLoansAndAdvance() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCapitalAdvance()))
			balanceSheetDetail.setCapitalAdvance(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersAssetsTransit())) // ------ others assets in transit
			balanceSheetDetail.setOthersAssetsTransit(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreOperativeExpensesPending()))
			balanceSheetDetail.setPreOperativeExpensesPending(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAssetsInTransit()))
			balanceSheetDetail.setAssetsInTransit(0.0);
		financialInputRequest.setOtheNonCurruntAssetFy(((balanceSheetDetail.getCapitalAdvance() + balanceSheetDetail.getOthersAssetsTransit()) - balanceSheetDetail.getPreOperativeExpensesPending() - balanceSheetDetail.getAssetsInTransit()) * denom);// ------ others pls specify
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInventory()))
			balanceSheetDetail.setInventory(0.0);
		financialInputRequest.setInventoriesFy(balanceSheetDetail.getInventory() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTradeReceivables()))
			balanceSheetDetail.setTradeReceivables(0.0);
		financialInputRequest.setSundryDebtorsFy(balanceSheetDetail.getTradeReceivables() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCashAndCashEquivalents()))
			balanceSheetDetail.setCashAndCashEquivalents(0.0);
		financialInputRequest.setCashAndBankFy(balanceSheetDetail.getCashAndCashEquivalents() * denom);
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCurrentInvestments()))
			balanceSheetDetail.setCurrentInvestments(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredTaxAsset()))
			balanceSheetDetail.setDeferredTaxAsset(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMiscExpences()))
			balanceSheetDetail.setMiscExpences(0.0);
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOtherDetails()))// ------ others details
			balanceSheetDetail.setOtherDetails(0.0);
		financialInputRequest.setOtherCurruntAssetFy((balanceSheetDetail.getCurrentInvestments() + balanceSheetDetail.getDeferredTaxAsset() + balanceSheetDetail.getMiscExpences() + balanceSheetDetail.getOtherDetails()) * denom);// ------ others pls specify
		
		if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCashAndCashEquivalents()))
			balanceSheetDetail.setCashAndCashEquivalents(0.0);
		financialInputRequest.setShortTermLoansAdvancesFy(balanceSheetDetail.getShortTermLoansAndAdvances() * denom);
		// -----CONTIGENT LIABILITIES
		if(corporateFinalInfoRequest == null)
			financialInputRequest.setContingentLiablitiesFy(null);
		else
			financialInputRequest.setContingentLiablitiesFy(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilityFyAmt()) ? 0.0 : (corporateFinalInfoRequest.getContLiabilityFyAmt() * denom));
		
		// ----------------------------------------SECOND YEAR DATA---------------------------------------------------------------------------------------
				//========= ==========================================PROFITIBILITYSTATEMENTDETAIL DETAIL 2 YR========================================================
				profitibilityStatementDetail = profitibilityStatementDetailRepository.getProfitibilityStatementDetail(aplicationId, currentYear-2+"");
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail)){
					profitibilityStatementDetail = new ProfitibilityStatementDetail();
				}
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getSales()))
					profitibilityStatementDetail.setSales(0.0);
				financialInputRequest.setGrossSalesSy(profitibilityStatementDetail.getSales() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getLessExciseDutyOrVatOrServiceTax()))
					profitibilityStatementDetail.setLessExciseDutyOrVatOrServiceTax(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getLessAnyOtherItem()))
					profitibilityStatementDetail.setLessAnyOtherItem(0.0);
				financialInputRequest.setLessExciseDuitySy((profitibilityStatementDetail.getLessExciseDutyOrVatOrServiceTax() + profitibilityStatementDetail.getLessAnyOtherItem()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryFg()))
					profitibilityStatementDetail.setIncreaseOrDecreaseInInventoryFg(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryWip()))
					profitibilityStatementDetail.setIncreaseOrDecreaseInInventoryWip(0.0);
				financialInputRequest.setIncreaseDecreaseStockSy((profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryFg() + profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryWip()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getCostRawMaterialConsumed()))
					profitibilityStatementDetail.setCostRawMaterialConsumed(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getPurchasesStockTnTrade()))
					profitibilityStatementDetail.setPurchasesStockTnTrade(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getStoreAndSpares()))
					profitibilityStatementDetail.setStoreAndSpares(0.0);
				financialInputRequest.setRawMaterialConsumedSy((profitibilityStatementDetail.getCostRawMaterialConsumed() + profitibilityStatementDetail.getPurchasesStockTnTrade()  + profitibilityStatementDetail.getStoreAndSpares()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getPowerAndFuel()))
					profitibilityStatementDetail.setPowerAndFuel(0.0);
				financialInputRequest.setPowerAndFuelCostSy(profitibilityStatementDetail.getPowerAndFuel() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getEmployeeBenefitExpenses()))
					profitibilityStatementDetail.setEmployeeBenefitExpenses(0.0);
				financialInputRequest.setEmployeeCostSy(profitibilityStatementDetail.getEmployeeBenefitExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getGeneralAdminExpenses()))
					profitibilityStatementDetail.setGeneralAdminExpenses(0.0);
				financialInputRequest.setGeneralAndAdminExpeSy(profitibilityStatementDetail.getGeneralAdminExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getSellingDistributionExpenses()))
					profitibilityStatementDetail.setSellingDistributionExpenses(0.0);
				financialInputRequest.setSellingAndDistriExpeSy(profitibilityStatementDetail.getSellingDistributionExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExpensesCapitalised()))
					profitibilityStatementDetail.setExpensesCapitalised(0.0);
				financialInputRequest.setLessExpeCapitaSy(profitibilityStatementDetail.getExpensesCapitalised() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getOtherPlsSpecify())) // ------ others pls specify
					profitibilityStatementDetail.setOtherPlsSpecify(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExpensesCapitalised()))
					profitibilityStatementDetail.setExpensesCapitalised(0.0);
				financialInputRequest.setMiscelExpeSy((profitibilityStatementDetail.getOtherPlsSpecify() - profitibilityStatementDetail.getExpensesCapitalised()) * denom); //----------------others pls specify field
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getOtherOperatingRevenue()))
					profitibilityStatementDetail.setOtherOperatingRevenue(0.0);
				financialInputRequest.setOtherIncomeSy(profitibilityStatementDetail.getOtherOperatingRevenue() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getFinanceCost()))
					profitibilityStatementDetail.setFinanceCost(0.0);
				financialInputRequest.setInterestSy(profitibilityStatementDetail.getFinanceCost() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getDepreciationAndAmortisation()))
					profitibilityStatementDetail.setDepreciationAndAmortisation(0.0);
				financialInputRequest.setDepriciationSy(profitibilityStatementDetail.getDepreciationAndAmortisation() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getNonOperatingIncome()))
					profitibilityStatementDetail.setNonOperatingIncome(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getNonOperatingExpenses()))
					profitibilityStatementDetail.setNonOperatingExpenses(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExtraordinaryItems()))
					profitibilityStatementDetail.setExtraordinaryItems(0.0);
				financialInputRequest.setExceptionalIncomeSy((profitibilityStatementDetail.getNonOperatingIncome() + profitibilityStatementDetail.getNonOperatingExpenses() + profitibilityStatementDetail.getExtraordinaryItems()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getProvisionForTax()))
					profitibilityStatementDetail.setProvisionForTax(0.0);
				financialInputRequest.setProvisionForTaxSy(profitibilityStatementDetail.getProvisionForTax() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getDividend()))
					profitibilityStatementDetail.setDividend(0.0);
				financialInputRequest.setDividendPayOutSy(profitibilityStatementDetail.getDividend() * denom);
				//========= ==========================================LIABILITY(BS) DETAIL 2 YR========================================================
				balanceSheetDetail = balanceSheetDetailRepository.getBalanceSheetDetail(aplicationId, currentYear-2+"");
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail)){
					balanceSheetDetail = new BalanceSheetDetail();
				}
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOrdinaryShareCapital()))
					balanceSheetDetail.setOrdinaryShareCapital(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreferenceShareCapital()))
					balanceSheetDetail.setPreferenceShareCapital(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getShareApplicationPendingAllotment()))
					balanceSheetDetail.setShareApplicationPendingAllotment(0.0);
				financialInputRequest.setShareCapitalSy((balanceSheetDetail.getOrdinaryShareCapital() + balanceSheetDetail.getPreferenceShareCapital() + balanceSheetDetail.getShareApplicationPendingAllotment()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMoneyReceivedAgainstShareWarrants()))
					balanceSheetDetail.setMoneyReceivedAgainstShareWarrants(0.0);
				financialInputRequest.setShareWarrantOutstandingsSy(balanceSheetDetail.getMoneyReceivedAgainstShareWarrants() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getRevaluationReserve()))
					balanceSheetDetail.setRevaluationReserve(0.0);
				financialInputRequest.setRevalationReserveSy(balanceSheetDetail.getRevaluationReserve() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getReservesAndSurplus()))
					balanceSheetDetail.setReservesAndSurplus(0.0);
				financialInputRequest.setOtherReserveAndSurplusSy((balanceSheetDetail.getReservesAndSurplus() - balanceSheetDetail.getRevaluationReserve()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMinorityInterest()))
					balanceSheetDetail.setMinorityInterest(0.0);
				financialInputRequest.setMinorityInterestSy(balanceSheetDetail.getMinorityInterest() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermLoansSecured()))
					balanceSheetDetail.setTermLoansSecured(0.0);
				financialInputRequest.setSecuredLoansSy(balanceSheetDetail.getTermLoansSecured() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermLoansUnsecured()))
					balanceSheetDetail.setTermLoansUnsecured(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromOthers()))
					balanceSheetDetail.setUnsecuredLoansFromOthers(0.0);
				financialInputRequest.setUnsecuredLoansOthersSy((balanceSheetDetail.getTermLoansUnsecured() + balanceSheetDetail.getUnsecuredLoansFromOthers()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getShortTermBorrowings()))
					balanceSheetDetail.setShortTermBorrowings(0.0);
				financialInputRequest.setOtherBorrowingSy(balanceSheetDetail.getShortTermBorrowings() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromPromoters()))
					balanceSheetDetail.setUnsecuredLoansFromPromoters(0.0);
				financialInputRequest.setUnsecuredLoansPromotersSy(balanceSheetDetail.getUnsecuredLoansFromPromoters() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredTaxLiability()))
					balanceSheetDetail.setDeferredTaxLiability(0.0);
				financialInputRequest.setDeferredTaxLiablitiesSy(balanceSheetDetail.getDeferredTaxLiability() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredPaymentCredits()))
					balanceSheetDetail.setDeferredPaymentCredits(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermDeposits()))
					balanceSheetDetail.setTermDeposits(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersPlsSpecify())) // ------ others pls specify
					balanceSheetDetail.setOthersPlsSpecify(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDebentures()))
					balanceSheetDetail.setDebentures(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromOthers()))
					balanceSheetDetail.setUnsecuredLoansFromOthers(0.0);
				financialInputRequest.setOtherLongTermLiablitiesSy(((balanceSheetDetail.getDeferredPaymentCredits() + balanceSheetDetail.getTermDeposits() + balanceSheetDetail.getOthersPlsSpecify() + balanceSheetDetail.getDebentures()) - balanceSheetDetail.getUnsecuredLoansFromOthers()) * denom); // ------ others
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getLongTermProvisions()))
					balanceSheetDetail.setLongTermProvisions(0.0);
				financialInputRequest.setLongTermProvisionSy(balanceSheetDetail.getLongTermProvisions() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTradePayables()))
					balanceSheetDetail.setTradePayables(0.0);
				financialInputRequest.setTradePayablesSy(balanceSheetDetail.getTradePayables() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAdvanceFromCustomers()))
					balanceSheetDetail.setAdvanceFromCustomers(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDividendPayable()))
					balanceSheetDetail.setDividendPayable(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getStatutoryLiabilityDues()))
					balanceSheetDetail.setStatutoryLiabilityDues(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDepositsAndInstallments()))
					balanceSheetDetail.setDepositsAndInstallments(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersTotals())) // ------ others pls specifytotals
					balanceSheetDetail.setOthersTotals(0.0);
				financialInputRequest.setOtherCurruntLiablitiesSy((balanceSheetDetail.getAdvanceFromCustomers() + balanceSheetDetail.getDividendPayable() + balanceSheetDetail.getStatutoryLiabilityDues() + balanceSheetDetail.getDepositsAndInstallments() + balanceSheetDetail.getOthersTotals()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getProvisionForTax()))
					balanceSheetDetail.setProvisionForTax(0.0);
				financialInputRequest.setShortTermProvisionSy(balanceSheetDetail.getProvisionForTax() * denom);
				
				//========= ==========================================ASSET(BS) DETAIL 2 YR========================================================
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getGrossFixedAssets()))
					balanceSheetDetail.setGrossFixedAssets(0.0);
				financialInputRequest.setGrossBlockSy(balanceSheetDetail.getGrossFixedAssets() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDepreciationToDate()))
					balanceSheetDetail.setDepreciationToDate(0.0);
				financialInputRequest.setLessAccumulatedDepreSy(balanceSheetDetail.getDepreciationToDate() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getImpairmentsOfAssests()))
					balanceSheetDetail.setImpairmentsOfAssests(0.0);
				financialInputRequest.setImpairmentofAssetSy(balanceSheetDetail.getImpairmentsOfAssests() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCapitalWorkInProgress()))
					balanceSheetDetail.setCapitalWorkInProgress(0.0);
				financialInputRequest.setCapitalWorkInProgressSy(balanceSheetDetail.getCapitalWorkInProgress() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getIntangibleAssets()))
					balanceSheetDetail.setIntangibleAssets(0.0);
				financialInputRequest.setIntengibleAssetsSy(balanceSheetDetail.getIntangibleAssets() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreOperativeExpensesPending()))
					balanceSheetDetail.setPreOperativeExpensesPending(0.0);
				financialInputRequest.setPreOperativeExpeSy(balanceSheetDetail.getPreOperativeExpensesPending() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAssetsInTransit()))
					balanceSheetDetail.setAssetsInTransit(0.0);
				financialInputRequest.setAssetInTransitSy(balanceSheetDetail.getAssetsInTransit() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInSubsidiaries()))
					balanceSheetDetail.setInvestmentInSubsidiaries(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInAssociates()))
					balanceSheetDetail.setInvestmentInAssociates(0.0);
				financialInputRequest.setInvestmentInSubsidiariesSy((balanceSheetDetail.getInvestmentInSubsidiaries() + balanceSheetDetail.getInvestmentInAssociates()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInQuoted()))
					balanceSheetDetail.setInvestmentInQuoted(0.0);
				financialInputRequest.setOtherInvestmentSy(balanceSheetDetail.getInvestmentInQuoted() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getLongTermLoansAndAdvance()))
					balanceSheetDetail.setLongTermLoansAndAdvance(0.0);
				financialInputRequest.setLongTermLoansAndAdvaSy(balanceSheetDetail.getLongTermLoansAndAdvance() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCapitalAdvance()))
					balanceSheetDetail.setCapitalAdvance(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersAssetsTransit())) // ------ others assets in transit
					balanceSheetDetail.setOthersAssetsTransit(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreOperativeExpensesPending()))
					balanceSheetDetail.setPreOperativeExpensesPending(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAssetsInTransit()))
					balanceSheetDetail.setAssetsInTransit(0.0);
				financialInputRequest.setOtheNonCurruntAssetSy(((balanceSheetDetail.getCapitalAdvance() + balanceSheetDetail.getOthersAssetsTransit()) - balanceSheetDetail.getPreOperativeExpensesPending() - balanceSheetDetail.getAssetsInTransit()) * denom);// ------ others totals
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInventory()))
					balanceSheetDetail.setInventory(0.0);
				financialInputRequest.setInventoriesSy(balanceSheetDetail.getInventory() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTradeReceivables()))
					balanceSheetDetail.setTradeReceivables(0.0);
				financialInputRequest.setSundryDebtorsSy(balanceSheetDetail.getTradeReceivables() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCashAndCashEquivalents()))
					balanceSheetDetail.setCashAndCashEquivalents(0.0);
				financialInputRequest.setCashAndBankSy(balanceSheetDetail.getCashAndCashEquivalents() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCurrentInvestments()))
					balanceSheetDetail.setCurrentInvestments(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredTaxAsset()))
					balanceSheetDetail.setDeferredTaxAsset(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMiscExpences()))
					balanceSheetDetail.setMiscExpences(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOtherDetails()))// ------ others details
					balanceSheetDetail.setOtherDetails(0.0);
				financialInputRequest.setOtherCurruntAssetSy((balanceSheetDetail.getCurrentInvestments() + balanceSheetDetail.getDeferredTaxAsset() + balanceSheetDetail.getMiscExpences() + balanceSheetDetail.getOtherDetails()) * denom);// ------ others details
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCashAndCashEquivalents()))
					balanceSheetDetail.setCashAndCashEquivalents(0.0);
				financialInputRequest.setShortTermLoansAdvancesSy(balanceSheetDetail.getShortTermLoansAndAdvances() * denom);
				// -----CONTIGENT LIABILITIES
		if(corporateFinalInfoRequest == null)
			financialInputRequest.setContingentLiablitiesSy(null);
		else
			financialInputRequest.setContingentLiablitiesSy(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilitySyAmt()) ? 0.0 : (corporateFinalInfoRequest.getContLiabilitySyAmt() * denom));
				
				// ----------------------------------------FIRST YEAR DATA---------------------------------------------------------------------------------------
				//========= ==========================================PROFITIBILITYSTATEMENTDETAIL DETAIL 1 YR========================================================
				profitibilityStatementDetail = profitibilityStatementDetailRepository.getProfitibilityStatementDetail(aplicationId, currentYear-3+"");
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail)){
					profitibilityStatementDetail = new ProfitibilityStatementDetail();
				}
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getSales()))
					profitibilityStatementDetail.setSales(0.0);
				financialInputRequest.setGrossSalesTy(profitibilityStatementDetail.getSales() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getLessExciseDutyOrVatOrServiceTax()))
					profitibilityStatementDetail.setLessExciseDutyOrVatOrServiceTax(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getLessAnyOtherItem()))
					profitibilityStatementDetail.setLessAnyOtherItem(0.0);
				financialInputRequest.setLessExciseDuityTy((profitibilityStatementDetail.getLessExciseDutyOrVatOrServiceTax() + profitibilityStatementDetail.getLessAnyOtherItem()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryFg()))
					profitibilityStatementDetail.setIncreaseOrDecreaseInInventoryFg(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryWip()))
					profitibilityStatementDetail.setIncreaseOrDecreaseInInventoryWip(0.0);
				financialInputRequest.setIncreaseDecreaseStockTy((profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryFg() + profitibilityStatementDetail.getIncreaseOrDecreaseInInventoryWip()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getCostRawMaterialConsumed()))
					profitibilityStatementDetail.setCostRawMaterialConsumed(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getPurchasesStockTnTrade()))
					profitibilityStatementDetail.setPurchasesStockTnTrade(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getStoreAndSpares()))
					profitibilityStatementDetail.setStoreAndSpares(0.0);
				financialInputRequest.setRawMaterialConsumedTy((profitibilityStatementDetail.getCostRawMaterialConsumed() + profitibilityStatementDetail.getPurchasesStockTnTrade()  + profitibilityStatementDetail.getStoreAndSpares()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getPowerAndFuel()))
					profitibilityStatementDetail.setPowerAndFuel(0.0);
				financialInputRequest.setPowerAndFuelCostTy(profitibilityStatementDetail.getPowerAndFuel() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getEmployeeBenefitExpenses()))
					profitibilityStatementDetail.setEmployeeBenefitExpenses(0.0);
				financialInputRequest.setEmployeeCostTy(profitibilityStatementDetail.getEmployeeBenefitExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getGeneralAdminExpenses()))
					profitibilityStatementDetail.setGeneralAdminExpenses(0.0);
				financialInputRequest.setGeneralAndAdminExpeTy(profitibilityStatementDetail.getGeneralAdminExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getSellingDistributionExpenses()))
					profitibilityStatementDetail.setSellingDistributionExpenses(0.0);
				financialInputRequest.setSellingAndDistriExpeTy(profitibilityStatementDetail.getSellingDistributionExpenses() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExpensesCapitalised()))
					profitibilityStatementDetail.setExpensesCapitalised(0.0);
				financialInputRequest.setLessExpeCapitaTy(profitibilityStatementDetail.getExpensesCapitalised() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getOtherPlsSpecify())) // ------ others pls specify
					profitibilityStatementDetail.setOtherPlsSpecify(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExpensesCapitalised()))
					profitibilityStatementDetail.setExpensesCapitalised(0.0);
				financialInputRequest.setMiscelExpeTy((profitibilityStatementDetail.getOtherPlsSpecify() - profitibilityStatementDetail.getExpensesCapitalised()) * denom); //----------------others pls specify field
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getOtherOperatingRevenue()))
					profitibilityStatementDetail.setOtherOperatingRevenue(0.0);
				financialInputRequest.setOtherIncomeTy(profitibilityStatementDetail.getOtherOperatingRevenue() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getFinanceCost()))
					profitibilityStatementDetail.setFinanceCost(0.0);
				financialInputRequest.setInterestTy(profitibilityStatementDetail.getFinanceCost() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getDepreciationAndAmortisation()))
					profitibilityStatementDetail.setDepreciationAndAmortisation(0.0);
				financialInputRequest.setDepriciationTy(profitibilityStatementDetail.getDepreciationAndAmortisation() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getNonOperatingIncome()))
					profitibilityStatementDetail.setNonOperatingIncome(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getNonOperatingExpenses()))
					profitibilityStatementDetail.setNonOperatingExpenses(0.0);
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getExtraordinaryItems()))
					profitibilityStatementDetail.setExtraordinaryItems(0.0);
				financialInputRequest.setExceptionalIncomeTy((profitibilityStatementDetail.getNonOperatingIncome() + profitibilityStatementDetail.getNonOperatingExpenses() + profitibilityStatementDetail.getExtraordinaryItems()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getProvisionForTax()))
					profitibilityStatementDetail.setProvisionForTax(0.0);
				financialInputRequest.setProvisionForTaxTy(profitibilityStatementDetail.getProvisionForTax() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail.getDividend()))
					profitibilityStatementDetail.setDividend(0.0);
				financialInputRequest.setDividendPayOutTy(profitibilityStatementDetail.getDividend() * denom);
				//========= ==========================================LIABILITY(BS) DETAIL 1 YR========================================================
				balanceSheetDetail = balanceSheetDetailRepository.getBalanceSheetDetail(aplicationId, currentYear-3+"");
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail)){
					balanceSheetDetail = new BalanceSheetDetail();
				}
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOrdinaryShareCapital()))
					balanceSheetDetail.setOrdinaryShareCapital(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreferenceShareCapital()))
					balanceSheetDetail.setPreferenceShareCapital(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getShareApplicationPendingAllotment()))
					balanceSheetDetail.setShareApplicationPendingAllotment(0.0);
				financialInputRequest.setShareCapitalTy((balanceSheetDetail.getOrdinaryShareCapital() + balanceSheetDetail.getPreferenceShareCapital() + balanceSheetDetail.getShareApplicationPendingAllotment()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMoneyReceivedAgainstShareWarrants()))
					balanceSheetDetail.setMoneyReceivedAgainstShareWarrants(0.0);
				financialInputRequest.setShareWarrantOutstandingsTy(balanceSheetDetail.getMoneyReceivedAgainstShareWarrants() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getRevaluationReserve()))
					balanceSheetDetail.setRevaluationReserve(0.0);
				financialInputRequest.setRevalationReserveTy(balanceSheetDetail.getRevaluationReserve() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getReservesAndSurplus()))
					balanceSheetDetail.setReservesAndSurplus(0.0);
				financialInputRequest.setOtherReserveAndSurplusTy((balanceSheetDetail.getReservesAndSurplus() - balanceSheetDetail.getRevaluationReserve()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMinorityInterest()))
					balanceSheetDetail.setMinorityInterest(0.0);
				financialInputRequest.setMinorityInterestTy(balanceSheetDetail.getMinorityInterest() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermLoansSecured()))
					balanceSheetDetail.setTermLoansSecured(0.0);
				financialInputRequest.setSecuredLoansTy(balanceSheetDetail.getTermLoansSecured() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermLoansUnsecured()))
					balanceSheetDetail.setTermLoansUnsecured(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromOthers()))
					balanceSheetDetail.setUnsecuredLoansFromOthers(0.0);
				financialInputRequest.setUnsecuredLoansOthersTy((balanceSheetDetail.getTermLoansUnsecured() + balanceSheetDetail.getUnsecuredLoansFromOthers()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getShortTermBorrowings()))
					balanceSheetDetail.setShortTermBorrowings(0.0);
				financialInputRequest.setOtherBorrowingTy(balanceSheetDetail.getShortTermBorrowings() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromPromoters()))
					balanceSheetDetail.setUnsecuredLoansFromPromoters(0.0);
				financialInputRequest.setUnsecuredLoansPromotersTy(balanceSheetDetail.getUnsecuredLoansFromPromoters() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredTaxLiability()))
					balanceSheetDetail.setDeferredTaxLiability(0.0);
				financialInputRequest.setDeferredTaxLiablitiesTy(balanceSheetDetail.getDeferredTaxLiability() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredPaymentCredits()))
					balanceSheetDetail.setDeferredPaymentCredits(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTermDeposits()))
					balanceSheetDetail.setTermDeposits(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersPlsSpecify())) // ------ others pls specfy
					balanceSheetDetail.setOthersPlsSpecify(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDebentures()))
					balanceSheetDetail.setDebentures(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getUnsecuredLoansFromOthers()))
					balanceSheetDetail.setUnsecuredLoansFromOthers(0.0);
				financialInputRequest.setOtherLongTermLiablitiesTy(((balanceSheetDetail.getDeferredPaymentCredits() + balanceSheetDetail.getTermDeposits() + balanceSheetDetail.getOthersPlsSpecify() + balanceSheetDetail.getDebentures()) - balanceSheetDetail.getUnsecuredLoansFromOthers()) * denom); // ------ others 
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getLongTermProvisions()))
					balanceSheetDetail.setLongTermProvisions(0.0);
				financialInputRequest.setLongTermProvisionTy(balanceSheetDetail.getLongTermProvisions() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTradePayables()))
					balanceSheetDetail.setTradePayables(0.0);
				financialInputRequest.setTradePayablesTy(balanceSheetDetail.getTradePayables() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAdvanceFromCustomers()))
					balanceSheetDetail.setAdvanceFromCustomers(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDividendPayable()))
					balanceSheetDetail.setDividendPayable(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getStatutoryLiabilityDues()))
					balanceSheetDetail.setStatutoryLiabilityDues(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDepositsAndInstallments()))
					balanceSheetDetail.setDepositsAndInstallments(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersTotals())) // ------ others totals
					balanceSheetDetail.setOthersTotals(0.0);
				financialInputRequest.setOtherCurruntLiablitiesTy((balanceSheetDetail.getAdvanceFromCustomers() + balanceSheetDetail.getDividendPayable() + balanceSheetDetail.getStatutoryLiabilityDues() + balanceSheetDetail.getDepositsAndInstallments() +balanceSheetDetail.getOthersTotals()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getProvisionForTax()))
					balanceSheetDetail.setProvisionForTax(0.0);
				financialInputRequest.setShortTermProvisionTy(balanceSheetDetail.getProvisionForTax() * denom);
				
				//========= ==========================================ASSET(BS) DETAIL 1 YR========================================================
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getGrossFixedAssets()))
					balanceSheetDetail.setGrossFixedAssets(0.0);
				financialInputRequest.setGrossBlockTy(balanceSheetDetail.getGrossFixedAssets() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDepreciationToDate()))
					balanceSheetDetail.setDepreciationToDate(0.0);
				financialInputRequest.setLessAccumulatedDepreTy(balanceSheetDetail.getDepreciationToDate() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getImpairmentsOfAssests()))
					balanceSheetDetail.setImpairmentsOfAssests(0.0);
				financialInputRequest.setImpairmentofAssetTy(balanceSheetDetail.getImpairmentsOfAssests() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCapitalWorkInProgress()))
					balanceSheetDetail.setCapitalWorkInProgress(0.0);
				financialInputRequest.setCapitalWorkInProgressTy(balanceSheetDetail.getCapitalWorkInProgress() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getIntangibleAssets()))
					balanceSheetDetail.setIntangibleAssets(0.0);
				financialInputRequest.setIntengibleAssetsTy(balanceSheetDetail.getIntangibleAssets() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreOperativeExpensesPending()))
					balanceSheetDetail.setPreOperativeExpensesPending(0.0);
				financialInputRequest.setPreOperativeExpeTy(balanceSheetDetail.getPreOperativeExpensesPending() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAssetsInTransit()))
					balanceSheetDetail.setAssetsInTransit(0.0);
				financialInputRequest.setAssetInTransitTy(balanceSheetDetail.getAssetsInTransit() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInSubsidiaries()))
					balanceSheetDetail.setInvestmentInSubsidiaries(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInAssociates()))
					balanceSheetDetail.setInvestmentInAssociates(0.0);
				financialInputRequest.setInvestmentInSubsidiariesTy((balanceSheetDetail.getInvestmentInSubsidiaries() + balanceSheetDetail.getInvestmentInAssociates()) * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInvestmentInQuoted()))
					balanceSheetDetail.setInvestmentInQuoted(0.0);
				financialInputRequest.setOtherInvestmentTy(balanceSheetDetail.getInvestmentInQuoted() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getLongTermLoansAndAdvance()))
					balanceSheetDetail.setLongTermLoansAndAdvance(0.0);
				financialInputRequest.setLongTermLoansAndAdvaTy(balanceSheetDetail.getLongTermLoansAndAdvance() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCapitalAdvance()))
					balanceSheetDetail.setCapitalAdvance(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOthersAssetsTransit())) // ------ others assets in transit
					balanceSheetDetail.setOthersAssetsTransit(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getPreOperativeExpensesPending()))
					balanceSheetDetail.setPreOperativeExpensesPending(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getAssetsInTransit()))
					balanceSheetDetail.setAssetsInTransit(0.0);
				financialInputRequest.setOtheNonCurruntAssetTy(((balanceSheetDetail.getCapitalAdvance() + balanceSheetDetail.getOthersAssetsTransit()) - balanceSheetDetail.getPreOperativeExpensesPending() - balanceSheetDetail.getAssetsInTransit()) * denom);// ------ others totals
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getInventory()))
					balanceSheetDetail.setInventory(0.0);
				financialInputRequest.setInventoriesTy(balanceSheetDetail.getInventory() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getTradeReceivables()))
					balanceSheetDetail.setTradeReceivables(0.0);
				financialInputRequest.setSundryDebtorsTy(balanceSheetDetail.getTradeReceivables() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCashAndCashEquivalents()))
					balanceSheetDetail.setCashAndCashEquivalents(0.0);
				financialInputRequest.setCashAndBankTy(balanceSheetDetail.getCashAndCashEquivalents() * denom);
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getCurrentInvestments()))
					balanceSheetDetail.setCurrentInvestments(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getDeferredTaxAsset()))
					balanceSheetDetail.setDeferredTaxAsset(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getMiscExpences()))
					balanceSheetDetail.setMiscExpences(0.0);
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getOtherDetails()))// ------ others details
					balanceSheetDetail.setOtherDetails(0.0);
				financialInputRequest.setOtherCurruntAssetTy((balanceSheetDetail.getCurrentInvestments() + balanceSheetDetail.getDeferredTaxAsset() + balanceSheetDetail.getMiscExpences() + balanceSheetDetail.getOtherDetails()) * denom);// ------ others details
				
				if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getShortTermLoansAndAdvances()))
					balanceSheetDetail.setShortTermLoansAndAdvances(0.0);
				financialInputRequest.setShortTermLoansAdvancesTy(balanceSheetDetail.getShortTermLoansAndAdvances() * denom);
				// -----CONTIGENT LIABILITIES
		if(corporateFinalInfoRequest == null)
			financialInputRequest.setContingentLiablitiestTy(null);
		else
			financialInputRequest.setContingentLiablitiestTy(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilityTyAmt()) ? 0.0 : (corporateFinalInfoRequest.getContLiabilityTyAmt() * denom));
				
		// FinancialInput Object Set		
		return financialInputRequest;
	}

	
	
	
	
	@Override
	public QualitativeInputSheetManuRequest qualitativeInputServiceManu(Long aplicationId, Long userId, Integer productId, Boolean isCmaUploaded, Boolean isCoActUploaded,Double industryRiskScore,Long denom, Long proposalMapId)
			throws Exception {

		QualitativeInputSheetManuRequest qualitativeInputSheetManuRequest = null;

		return setQualitativeInputManu(aplicationId,productId, userId,isCmaUploaded,isCoActUploaded, industryRiskScore, denom, proposalMapId);
		/*LoanType type = CommonUtils.LoanType.getType(productId);
		switch (type) {
		case WORKING_CAPITAL:
			// set 
			return setWCManufacturingQualitativeInput(aplicationId,userId,industryRiskScore,denom);
			//break;
		case TERM_LOAN:
			return setTLManufacturingQualitativeInput(aplicationId,userId,isCmaUploaded,isCoActUploaded,industryRiskScore,denom);
			//break;
		case UNSECURED_LOAN :
			return setUSLManufacturingQualitativeInput(aplicationId,userId,industryRiskScore,denom);
			//break;
				
		}*/
	}

	public QualitativeInputSheetManuRequest setQualitativeInputManu(Long aplicationId,Integer productId,Long userId,Boolean isCmaUploaded, Boolean isCoActUploaded,Double industryRiskScore,Long denom, Long proposalMapId) throws Exception{
		QualitativeInputSheetManuRequest qualitativeInputSheetManuRequest = new QualitativeInputSheetManuRequest();

		CorporateMcqDetail corporateMcqDetail = null;
		corporateMcqDetail = corporateMcqDetailRepository.getByProposalIdAndUserId(proposalMapId);

		qualitativeInputSheetManuRequest.setAccountingQuality(corporateMcqDetail.getAccountingQuality().longValue());
		qualitativeInputSheetManuRequest.setUnhedgedForeignCurrencyExposure(corporateMcqDetail.getUnhedgedForeignCurrency().longValue());
		qualitativeInputSheetManuRequest.setFinancialRestructuringHistory(corporateMcqDetail.getFinancialRestructuringHistory().longValue());
		qualitativeInputSheetManuRequest.setIndustryRiskScore(industryRiskScore); //-----Industry mapping -- Remaining
		qualitativeInputSheetManuRequest.setCustomerQuality(corporateMcqDetail.getCustomerQuality().longValue());
		qualitativeInputSheetManuRequest.setSupplierQuality(corporateMcqDetail.getSupplierQuality().longValue());
		qualitativeInputSheetManuRequest.setOrderBookPosition(corporateMcqDetail.getOrderBookPosition().longValue());
		qualitativeInputSheetManuRequest.setIndustrialEmployeeRelations(corporateMcqDetail.getEmployeeRelations().longValue());
		qualitativeInputSheetManuRequest.setIntegrity(corporateMcqDetail.getIntegrity().longValue());
		qualitativeInputSheetManuRequest.setBusinessCommitment(corporateMcqDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetManuRequest.setManagementCompetence(corporateMcqDetail.getManagementCompetence().longValue());
		qualitativeInputSheetManuRequest.setBusinessExperience(corporateMcqDetail.getBusinessExperience().longValue());
		qualitativeInputSheetManuRequest.setSuccsessionPlanning(corporateMcqDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetManuRequest.setFinancialStrength(corporateMcqDetail.getFinancialSupport().longValue());
		qualitativeInputSheetManuRequest.setAbilityToRaise(corporateMcqDetail.getAbilityToRaiseFunds().longValue());
		qualitativeInputSheetManuRequest.setIntraCompanyConflicts(corporateMcqDetail.getIntraCompany().longValue());
		qualitativeInputSheetManuRequest.setStatusProjectClearances(corporateMcqDetail.getStatusOfProjectClearances().longValue());
		qualitativeInputSheetManuRequest.setStatusFinancialClosure(corporateMcqDetail.getStatusOfFinancialClosure().longValue());
		qualitativeInputSheetManuRequest.setProjectDebtService(corporateMcqDetail.getProjectedDebtService().longValue());
		qualitativeInputSheetManuRequest.setInternalRateReturn(corporateMcqDetail.getInternalRateReturn().longValue());
		qualitativeInputSheetManuRequest.setSensititivityAnalysis(corporateMcqDetail.getSensititivityAnalysis().longValue());
		qualitativeInputSheetManuRequest.setInfrastructureAvailability(corporateMcqDetail.getInfrastructureAvailability().longValue());
		qualitativeInputSheetManuRequest.setConstructionContract(corporateMcqDetail.getConstructionContract().longValue());
		log.info("corporateMcqDetail.getConstructionContract():::::::::::::::::::::"+corporateMcqDetail.getConstructionContract());
		qualitativeInputSheetManuRequest.setDesignTechnologyRisk(corporateMcqDetail.getTechnologyRiskId().longValue());
		qualitativeInputSheetManuRequest.setNumberCheckReturned(corporateMcqDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetManuRequest.setNumberTimesDpLimits(corporateMcqDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetManuRequest.setCumulativeDaysDpLimits(corporateMcqDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetManuRequest.setCompliancesWithSancationed(corporateMcqDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetManuRequest.setSubmissionProgressReport(corporateMcqDetail.getProgressReports().longValue());
		qualitativeInputSheetManuRequest.setDelayInReceiptPrincipal(corporateMcqDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetManuRequest.setDelayInSubmissionAudited(corporateMcqDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetManuRequest.setVarianceInProjectedSales(corporateMcqDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetManuRequest.setNumberOfLcBgIssuedInFavor(corporateMcqDetail.getNumberOfLc().longValue());

		//---Contigent Liabilities set
		CorporateFinalInfoRequest  corporateFinalInfoRequest = new CorporateFinalInfoRequest();
		corporateFinalInfoRequest = corporateFinalInfoService.get(userId ,aplicationId);
		int currentYear = scoringService.getFinYear(aplicationId);
		if(isCmaUploaded) {
			AssetsDetails assetsDetails = new AssetsDetails();
			assetsDetails = assetsDetailsRepository.getAssetsDetails(aplicationId, currentYear-1+"");
			if(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilityFyAmt()))
				qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);//-----formula based
			else if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getTangibleNetWorth()))
				qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);//-----formula based
			else
				qualitativeInputSheetManuRequest.setContingentLiabilities((corporateFinalInfoRequest.getContLiabilityFyAmt() / assetsDetails.getTangibleNetWorth()) * denom);//-----formula based
		}

		Double totalCostEstimate=0.0;
		Double totalAsset=0.0;
		LoanType type = CommonUtils.LoanType.getType(productId);
		switch (type) {
			case TERM_LOAN:
			{
				CorporateApplicantDetail corporateApplicantDetail=corporateApplicantDetailRepository.findOneByApplicationIdId(aplicationId);
				if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getTotalCostOfEstimate()))
					totalCostEstimate=corporateApplicantDetail.getTotalCostOfEstimate();

				int c_Year = Calendar.getInstance().get(Calendar.YEAR);
				AssetsDetails assetsDetails = assetsDetailsRepository.getAssetsDetails(aplicationId, c_Year-1+"");
				if(!CommonUtils.isObjectNullOrEmpty(assetsDetails.getTotalAssets()))
					totalAsset=assetsDetails.getTotalAssets();

				if(totalAsset != 0.0)
					qualitativeInputSheetManuRequest.setProjectSize(totalCostEstimate/totalAsset);//----- formula based
				else
					qualitativeInputSheetManuRequest.setProjectSize(0.0);

				break;

			}
			case  WCTL_LOAN:
			{
				CorporateApplicantDetail corporateApplicantDetail=corporateApplicantDetailRepository.findOneByApplicationIdId(aplicationId);
				if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getTotalCostOfEstimate()))
					totalCostEstimate=corporateApplicantDetail.getTotalCostOfEstimate();

				int c_Year = Calendar.getInstance().get(Calendar.YEAR);
				AssetsDetails assetsDetails = assetsDetailsRepository.getAssetsDetails(aplicationId, c_Year-1+"");
				if(!CommonUtils.isObjectNullOrEmpty(assetsDetails.getTotalAssets()))
					totalAsset=assetsDetails.getTotalAssets();

				if(totalAsset != 0.0)
					qualitativeInputSheetManuRequest.setProjectSize(totalCostEstimate/totalAsset);//----- formula based
				else
					qualitativeInputSheetManuRequest.setProjectSize(0.0);

				break;
			}
			case  WORKING_CAPITAL:
			{
				qualitativeInputSheetManuRequest.setProjectSize(0.0);//----- formula based
				break;
			}

			default : break;
		}

		return qualitativeInputSheetManuRequest;
	}

	/*public QualitativeInputSheetManuRequest setWCManufacturingQualitativeInput(Long aplicationId,Long userId,Double industryRiskScore,Long denom) throws Exception{
		QualitativeInputSheetManuRequest qualitativeInputSheetManuRequest = new QualitativeInputSheetManuRequest();
		List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequest = new ArrayList<PastFinancialEstimatesDetailRequest>();
		pastFinancialEstimatesDetailRequest=pastFinancialEstiamateDetailsService.getPastFinancialEstimateDetailsList(aplicationId);
		
		FinalWorkingCapitalLoanDetail finalWorkingCapitalLoanDetail = null; 
		finalWorkingCapitalLoanDetail = finalWorkingCapitalLoanDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		
		qualitativeInputSheetManuRequest.setAccountingQuality(finalWorkingCapitalLoanDetail.getAccountingQuality().longValue());		
		qualitativeInputSheetManuRequest.setUnhedgedForeignCurrencyExposure(finalWorkingCapitalLoanDetail.getUnhedgedForeignCurrency().longValue());
		qualitativeInputSheetManuRequest.setFinancialRestructuringHistory(finalWorkingCapitalLoanDetail.getFinancialRestructuringHistory().longValue());
		qualitativeInputSheetManuRequest.setIndustryRiskScore(industryRiskScore); //-----Industry mapping -- Remaining
		qualitativeInputSheetManuRequest.setCustomerQuality(finalWorkingCapitalLoanDetail.getCustomerQuality().longValue());
		qualitativeInputSheetManuRequest.setSupplierQuality(finalWorkingCapitalLoanDetail.getSupplierQuality().longValue());
		qualitativeInputSheetManuRequest.setOrderBookPosition(finalWorkingCapitalLoanDetail.getOrderBookPosition().longValue());
		qualitativeInputSheetManuRequest.setIndustrialEmployeeRelations(finalWorkingCapitalLoanDetail.getEmployeeRelations().longValue());
		qualitativeInputSheetManuRequest.setIntegrity(finalWorkingCapitalLoanDetail.getIntegrity().longValue());
		qualitativeInputSheetManuRequest.setBusinessCommitment(finalWorkingCapitalLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetManuRequest.setManagementCompetence(finalWorkingCapitalLoanDetail.getManagementCompetence().longValue());
		qualitativeInputSheetManuRequest.setBusinessExperience(finalWorkingCapitalLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetManuRequest.setSuccsessionPlanning(finalWorkingCapitalLoanDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetManuRequest.setFinancialStrength(finalWorkingCapitalLoanDetail.getFinancialStrength().longValue());
		qualitativeInputSheetManuRequest.setAbilityToRaise(finalWorkingCapitalLoanDetail.getAbilityToRaiseFunds().longValue());
		qualitativeInputSheetManuRequest.setIntraCompanyConflicts(finalWorkingCapitalLoanDetail.getIntraCompany().longValue());		
		qualitativeInputSheetManuRequest.setStatusProjectClearances(finalWorkingCapitalLoanDetail.getStatusOfProjectClearances().longValue());
		qualitativeInputSheetManuRequest.setStatusFinancialClosure(finalWorkingCapitalLoanDetail.getStatusOfFinancialClosure().longValue());
		qualitativeInputSheetManuRequest.setProjectDebtService(finalWorkingCapitalLoanDetail.getProjectedDebtService().longValue());
		qualitativeInputSheetManuRequest.setInternalRateReturn(finalWorkingCapitalLoanDetail.getInternalRateReturn().longValue());
		qualitativeInputSheetManuRequest.setSensititivityAnalysis(finalWorkingCapitalLoanDetail.getSensititivityAnalysis().longValue());
		qualitativeInputSheetManuRequest.setInfrastructureAvailability(finalWorkingCapitalLoanDetail.getInfrastructureAvailability().longValue());
		qualitativeInputSheetManuRequest.setConstructionContract(finalWorkingCapitalLoanDetail.getConstructionContract().longValue());
		qualitativeInputSheetManuRequest.setDesignTechnologyRisk(finalWorkingCapitalLoanDetail.getTechnologyRiskId().longValue());
		qualitativeInputSheetManuRequest.setNumberCheckReturned(finalWorkingCapitalLoanDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetManuRequest.setNumberTimesDpLimits(finalWorkingCapitalLoanDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetManuRequest.setCumulativeDaysDpLimits(finalWorkingCapitalLoanDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetManuRequest.setCompliancesWithSancationed(finalWorkingCapitalLoanDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetManuRequest.setSubmissionProgressReport(finalWorkingCapitalLoanDetail.getProgressReports().longValue());
		qualitativeInputSheetManuRequest.setDelayInReceiptPrincipal(finalWorkingCapitalLoanDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetManuRequest.setDelayInSubmissionAudited(finalWorkingCapitalLoanDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetManuRequest.setVarianceInProjectedSales(finalWorkingCapitalLoanDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetManuRequest.setNumberOfLcBgIssuedInFavor(finalWorkingCapitalLoanDetail.getNumberOfLc().longValue());	
		
		//---Contigent Liabilities set
		if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest))
			qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
		else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability()))
			qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
		else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()))
			qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
		else
			qualitativeInputSheetManuRequest.setContingentLiabilities((pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability() / pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()) *denom);//-----formula based
		//---project size 0.0 WC
		qualitativeInputSheetManuRequest.setProjectSize(0.0);//----- formula based
		
		return qualitativeInputSheetManuRequest;
	}
	
	public QualitativeInputSheetManuRequest setTLManufacturingQualitativeInput(Long aplicationId,Long userId, Boolean isCmaUploaded, Boolean isCoActUploaded,Double industryRiskScore,Long denom) throws Exception{
		QualitativeInputSheetManuRequest qualitativeInputSheetManuRequest = new QualitativeInputSheetManuRequest();
		CorporateFinalInfoRequest corporateFinalInfoRequest = new CorporateFinalInfoRequest();
		corporateFinalInfoRequest = corporateFinalInfoService.get(userId, aplicationId);
		
		FinalTermLoanDetail finalTermLoanDetail = null;
		finalTermLoanDetail = finalTermLoanDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		
		qualitativeInputSheetManuRequest.setAccountingQuality(finalTermLoanDetail.getAccountingQuality().longValue());
		qualitativeInputSheetManuRequest.setUnhedgedForeignCurrencyExposure(finalTermLoanDetail.getUnhedgedForeignCurrency().longValue());
		qualitativeInputSheetManuRequest.setFinancialRestructuringHistory(finalTermLoanDetail.getFinancialRestructuringHistory().longValue());
		qualitativeInputSheetManuRequest.setIndustryRiskScore(industryRiskScore); //-----Industry mapping
		qualitativeInputSheetManuRequest.setCustomerQuality(finalTermLoanDetail.getCustomerQuality().longValue());
		qualitativeInputSheetManuRequest.setSupplierQuality(finalTermLoanDetail.getSupplierQuality().longValue());
		qualitativeInputSheetManuRequest.setOrderBookPosition(finalTermLoanDetail.getOrderBookPosition().longValue());
		qualitativeInputSheetManuRequest.setIndustrialEmployeeRelations(finalTermLoanDetail.getEmployeeRelations().longValue());
		qualitativeInputSheetManuRequest.setIntegrity(finalTermLoanDetail.getIntegrity().longValue());
		qualitativeInputSheetManuRequest.setBusinessCommitment(finalTermLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetManuRequest.setManagementCompetence(finalTermLoanDetail.getManagementCompetence().longValue());
		qualitativeInputSheetManuRequest.setBusinessExperience(finalTermLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetManuRequest.setSuccsessionPlanning(finalTermLoanDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetManuRequest.setFinancialStrength(finalTermLoanDetail.getFinancialStrength().longValue());
		qualitativeInputSheetManuRequest.setAbilityToRaise(finalTermLoanDetail.getAbilityToRaiseFunds().longValue());
		qualitativeInputSheetManuRequest.setIntraCompanyConflicts(finalTermLoanDetail.getIntraCompany().longValue());
		qualitativeInputSheetManuRequest.setStatusProjectClearances(finalTermLoanDetail.getStatusOfProjectClearances().longValue());
		qualitativeInputSheetManuRequest.setStatusFinancialClosure(finalTermLoanDetail.getStatusOfFinancialClosure().longValue());
		qualitativeInputSheetManuRequest.setProjectDebtService(finalTermLoanDetail.getProjectedDebtService().longValue());
		qualitativeInputSheetManuRequest.setInternalRateReturn(finalTermLoanDetail.getInternalRateReturn().longValue());
		qualitativeInputSheetManuRequest.setSensititivityAnalysis(finalTermLoanDetail.getSensititivityAnalysis().longValue());
		qualitativeInputSheetManuRequest.setInfrastructureAvailability(finalTermLoanDetail.getInfrastructureAvailability().longValue());
		qualitativeInputSheetManuRequest.setConstructionContract(finalTermLoanDetail.getConstructionContract().longValue());
		qualitativeInputSheetManuRequest.setDesignTechnologyRisk(finalTermLoanDetail.getTechnologyRiskId().longValue());
		qualitativeInputSheetManuRequest.setNumberCheckReturned(finalTermLoanDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetManuRequest.setNumberTimesDpLimits(finalTermLoanDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetManuRequest.setCumulativeDaysDpLimits(finalTermLoanDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetManuRequest.setCompliancesWithSancationed(finalTermLoanDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetManuRequest.setSubmissionProgressReport(finalTermLoanDetail.getProgressReports().longValue());
		qualitativeInputSheetManuRequest.setDelayInReceiptPrincipal(finalTermLoanDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetManuRequest.setDelayInSubmissionAudited(finalTermLoanDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetManuRequest.setVarianceInProjectedSales(finalTermLoanDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetManuRequest.setNumberOfLcBgIssuedInFavor(finalTermLoanDetail.getNumberOfLc().longValue());		
		
		//---Contigent Liabilities set
			if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest))
				qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
			else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability()))
				qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
			else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()))
				qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
			else
				qualitativeInputSheetManuRequest.setContingentLiabilities((pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability() / pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()) *denom);//-----formula based
		//---project size TL
			PrimaryTermLoanDetail primaryTermLoanDetail = null;
			primaryTermLoanDetail = primaryTermLoanDetailRepository.findOne(aplicationId);			
			int currentYear = scoringService.getFinYear(aplicationId);

			
			if(isCmaUploaded){
				AssetsDetails assetsDetails = new AssetsDetails();
				assetsDetails = assetsDetailsRepository.getAssetsDetails(aplicationId, currentYear-1+"");
				if(CommonUtils.isObjectNullOrEmpty(primaryTermLoanDetail.getTotalCostOfEstimate()))
					qualitativeInputSheetManuRequest.setProjectSize(0.0);
				else if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getTotalAssets()))
					qualitativeInputSheetManuRequest.setProjectSize(0.0);
				else
					qualitativeInputSheetManuRequest.setProjectSize((primaryTermLoanDetail.getTotalCostOfEstimate() / assetsDetails.getTotalAssets()) *denom);//----- formula based
			}else if(isCoActUploaded){
				BalanceSheetDetail balanceSheetDetail = new BalanceSheetDetail();
				balanceSheetDetail = balanceSheetDetailRepository.getBalanceSheetDetail(aplicationId, currentYear-1+"");
				if(CommonUtils.isObjectNullOrEmpty(primaryTermLoanDetail.getTotalCostOfEstimate()))
					qualitativeInputSheetManuRequest.setProjectSize(0.0);
				else if(CommonUtils.isObjectNullOrEmpty(balanceSheetDetail.getGrandTotal()))
					qualitativeInputSheetManuRequest.setProjectSize(0.0);
				else
					qualitativeInputSheetManuRequest.setProjectSize((primaryTermLoanDetail.getTotalCostOfEstimate() / balanceSheetDetail.getGrandTotal()) *denom);//----- formula based
			}
				
		return qualitativeInputSheetManuRequest;
	}
	
	public QualitativeInputSheetManuRequest setUSLManufacturingQualitativeInput(Long aplicationId,Long userId,Double industryRiskScore,Long denom) throws Exception{
		QualitativeInputSheetManuRequest qualitativeInputSheetManuRequest = new QualitativeInputSheetManuRequest();
		List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequest = new ArrayList<PastFinancialEstimatesDetailRequest>();
		pastFinancialEstimatesDetailRequest=pastFinancialEstiamateDetailsService.getPastFinancialEstimateDetailsList(aplicationId);
		
		FinalUnsecureLoanDetail finalUnsecureLoanDetail = null;
		finalUnsecureLoanDetail = finalUnsecuredLoanDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		
		qualitativeInputSheetManuRequest.setAccountingQuality(finalUnsecureLoanDetail.getAccountingQuality().longValue());
		qualitativeInputSheetManuRequest.setUnhedgedForeignCurrencyExposure(finalUnsecureLoanDetail.getUnhedgedForeignCurrency().longValue());
		qualitativeInputSheetManuRequest.setFinancialRestructuringHistory(finalUnsecureLoanDetail.getFinancialRestructuringHistory().longValue());
		qualitativeInputSheetManuRequest.setIndustryRiskScore(industryRiskScore); //-----Industry mapping-- Remaining
		qualitativeInputSheetManuRequest.setCustomerQuality(finalUnsecureLoanDetail.getCustomerQuality().longValue());
		qualitativeInputSheetManuRequest.setSupplierQuality(finalUnsecureLoanDetail.getSupplierQuality().longValue());
		qualitativeInputSheetManuRequest.setOrderBookPosition(finalUnsecureLoanDetail.getOrderBookPosition().longValue());
		qualitativeInputSheetManuRequest.setIndustrialEmployeeRelations(finalUnsecureLoanDetail.getEmployeeRelations().longValue());
		qualitativeInputSheetManuRequest.setIntegrity(finalUnsecureLoanDetail.getIntegrity().longValue());
		qualitativeInputSheetManuRequest.setBusinessCommitment(finalUnsecureLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetManuRequest.setManagementCompetence(finalUnsecureLoanDetail.getManagementCompetence().longValue());
		qualitativeInputSheetManuRequest.setBusinessExperience(finalUnsecureLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetManuRequest.setSuccsessionPlanning(finalUnsecureLoanDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetManuRequest.setFinancialStrength(finalUnsecureLoanDetail.getFinancialStrength().longValue());
		qualitativeInputSheetManuRequest.setAbilityToRaise(finalUnsecureLoanDetail.getAbilityToRaiseFunds().longValue());
		qualitativeInputSheetManuRequest.setIntraCompanyConflicts(finalUnsecureLoanDetail.getIntraCompany().longValue());
		qualitativeInputSheetManuRequest.setStatusProjectClearances(finalUnsecureLoanDetail.getStatusOfProjectClearances().longValue());
		qualitativeInputSheetManuRequest.setStatusFinancialClosure(finalUnsecureLoanDetail.getStatusOfFinancialClosure().longValue());
		qualitativeInputSheetManuRequest.setProjectDebtService(finalUnsecureLoanDetail.getProjectedDebtService().longValue());
		qualitativeInputSheetManuRequest.setInternalRateReturn(finalUnsecureLoanDetail.getInternalRateReturn().longValue());
		qualitativeInputSheetManuRequest.setSensititivityAnalysis(finalUnsecureLoanDetail.getSensititivityAnalysis().longValue());
		qualitativeInputSheetManuRequest.setInfrastructureAvailability(finalUnsecureLoanDetail.getInfrastructureAvailability().longValue());
		qualitativeInputSheetManuRequest.setConstructionContract(finalUnsecureLoanDetail.getConstructionContract().longValue());
		qualitativeInputSheetManuRequest.setDesignTechnologyRisk(finalUnsecureLoanDetail.getTechnologyRiskId().longValue());
		qualitativeInputSheetManuRequest.setNumberCheckReturned(finalUnsecureLoanDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetManuRequest.setNumberTimesDpLimits(finalUnsecureLoanDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetManuRequest.setCumulativeDaysDpLimits(finalUnsecureLoanDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetManuRequest.setCompliancesWithSancationed(finalUnsecureLoanDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetManuRequest.setSubmissionProgressReport(finalUnsecureLoanDetail.getProgressReports().longValue());
		qualitativeInputSheetManuRequest.setDelayInReceiptPrincipal(finalUnsecureLoanDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetManuRequest.setDelayInSubmissionAudited(finalUnsecureLoanDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetManuRequest.setVarianceInProjectedSales(finalUnsecureLoanDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetManuRequest.setNumberOfLcBgIssuedInFavor(finalUnsecureLoanDetail.getNumberOfLc().longValue());	
		
		//---Contigent Liabilities set
		if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest))
				qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
		else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability()))
				qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
		else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()))
				qualitativeInputSheetManuRequest.setContingentLiabilities(0.0);
		else
				qualitativeInputSheetManuRequest.setContingentLiabilities((pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability() / pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()) *denom);//-----formula based
		//---project size 0.0 USL
			qualitativeInputSheetManuRequest.setProjectSize(0.0);//----- formula based
				
		return qualitativeInputSheetManuRequest;
	}
	*/
	
	
	
	@Override
	public QualitativeInputSheetServRequest qualitativeInputServiceService(Long aplicationId,Long userId , Integer productId,Boolean isCmaUploaded, Boolean isCoActUploaded,Long denom, Long proposalMapId)
			throws Exception {

		QualitativeInputSheetServRequest qualitativeInputSheetServRequest = new QualitativeInputSheetServRequest();

		return setServiceQualitativeInput(aplicationId,userId ,isCmaUploaded, isCoActUploaded, denom, proposalMapId);
		/*LoanType type = CommonUtils.LoanType.getType(productId);
		switch (type) {
		case WORKING_CAPITAL:
			return setWCServiceQualitativeInput(aplicationId,userId ,denom);
		case TERM_LOAN:
			return setTLServiceQualitativeInput(aplicationId,userId ,denom);
		case UNSECURED_LOAN :
			return setUSLServiceQualitativeInput(aplicationId,userId ,denom);
		}*/
	}

	public QualitativeInputSheetServRequest setServiceQualitativeInput(Long aplicationId,Long userId,Boolean isCmaUploaded, Boolean isCoActUploaded,Long denom, Long proposalMapId) throws Exception{
		QualitativeInputSheetServRequest qualitativeInputSheetServRequest = new QualitativeInputSheetServRequest();

		CorporateMcqDetail corporateMcqDetail = null;
//		corporateMcqDetail = corporateMcqDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		corporateMcqDetail = corporateMcqDetailRepository.getByProposalIdAndUserId(proposalMapId);
		
		qualitativeInputSheetServRequest.setAccountingQuality(corporateMcqDetail.getAccountingQuality().longValue());
		qualitativeInputSheetServRequest.setCustomerQuality(corporateMcqDetail.getCustomerQuality().longValue());
		qualitativeInputSheetServRequest.setSupplierQuality(corporateMcqDetail.getSupplierQuality().longValue());
		qualitativeInputSheetServRequest.setSustainabilityProductDemand(corporateMcqDetail.getSustainabilityProduct().longValue());
		qualitativeInputSheetServRequest.setProductSeasonality(corporateMcqDetail.getProductSeasonality().longValue());
		qualitativeInputSheetServRequest.setImpactOnOperatingMargins(corporateMcqDetail.getImpactOnOperatingMargins().longValue());
		qualitativeInputSheetServRequest.setEnvironmentImpact(corporateMcqDetail.getEnvironmentalImpact().longValue());
		qualitativeInputSheetServRequest.setIntegrity(corporateMcqDetail.getIntegrity().longValue());
		qualitativeInputSheetServRequest.setBusinessCommitment(corporateMcqDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetServRequest.setManagementCompetence(corporateMcqDetail.getManagementCompetence().longValue());
		qualitativeInputSheetServRequest.setBusinessExperience(corporateMcqDetail.getBusinessExperience().longValue());
		qualitativeInputSheetServRequest.setSuccsessionPlanning(corporateMcqDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetServRequest.setFinancialStrength(corporateMcqDetail.getFinancialStrength().longValue());
		qualitativeInputSheetServRequest.setInternalControl(corporateMcqDetail.getInternalControl().longValue());
		qualitativeInputSheetServRequest.setCreditTrackRecord(corporateMcqDetail.getCreditTrackRecord().longValue());
		qualitativeInputSheetServRequest.setNumberCheckReturned(corporateMcqDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetServRequest.setNumberTimesDpLimits(corporateMcqDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetServRequest.setCumulativeDaysDpLimits(corporateMcqDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetServRequest.setCompliancesWithSancationed(corporateMcqDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetServRequest.setSubmissionProgressReport(corporateMcqDetail.getProgressReports().longValue());
		qualitativeInputSheetServRequest.setDelayInReceiptPrincipal(corporateMcqDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetServRequest.setDelayInSubmissionAudited(corporateMcqDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetServRequest.setVarianceInProjectedSales(corporateMcqDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetServRequest.setNumberOfLcBgIssuedInFavor(corporateMcqDetail.getNumberOfLc().longValue());
		
		//---Contigent Liabilities set
		CorporateFinalInfoRequest  corporateFinalInfoRequest = new CorporateFinalInfoRequest();
		corporateFinalInfoRequest = corporateFinalInfoService.get(userId ,aplicationId);
		int currentYear = scoringService.getFinYear(aplicationId);
		if(isCmaUploaded) {
			AssetsDetails assetsDetails = new AssetsDetails();
			assetsDetails = assetsDetailsRepository.getAssetsDetails(aplicationId, currentYear-1+"");
			if(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilityFyAmt()))
				qualitativeInputSheetServRequest.setContingentLiabilities(0.0);//-----formula based
			else if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getTangibleNetWorth()))
				qualitativeInputSheetServRequest.setContingentLiabilities(0.0);//-----formula based
			else
				qualitativeInputSheetServRequest.setContingentLiabilities((corporateFinalInfoRequest.getContLiabilityFyAmt() / assetsDetails.getTangibleNetWorth()) * denom);//-----formula based
		}
		
		return qualitativeInputSheetServRequest;		
	}
	
	/*public QualitativeInputSheetServRequest setTLServiceQualitativeInput(Long aplicationId,Long userId,Long denom) throws Exception{
		QualitativeInputSheetServRequest qualitativeInputSheetServRequest = new QualitativeInputSheetServRequest();
		List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequest = new ArrayList<PastFinancialEstimatesDetailRequest>();
		pastFinancialEstimatesDetailRequest=pastFinancialEstiamateDetailsService.getPastFinancialEstimateDetailsList(aplicationId);
		
		FinalTermLoanDetail finalTermLoanDetail = null;
		finalTermLoanDetail = finalTermLoanDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		
		qualitativeInputSheetServRequest.setAccountingQuality(finalTermLoanDetail.getAccountingQuality().longValue());
		qualitativeInputSheetServRequest.setCustomerQuality(finalTermLoanDetail.getCustomerQuality().longValue());
		qualitativeInputSheetServRequest.setSupplierQuality(finalTermLoanDetail.getSupplierQuality().longValue());
		qualitativeInputSheetServRequest.setSustainabilityProductDemand(finalTermLoanDetail.getSustainabilityProduct().longValue());
		qualitativeInputSheetServRequest.setProductSeasonality(finalTermLoanDetail.getProductSeasonality().longValue());
		qualitativeInputSheetServRequest.setImpactOnOperatingMargins(finalTermLoanDetail.getImpactOnOperatingMargins().longValue());
		qualitativeInputSheetServRequest.setEnvironmentImpact(finalTermLoanDetail.getEnvironmentalImpact().longValue());
		qualitativeInputSheetServRequest.setIntegrity(finalTermLoanDetail.getIntegrity().longValue());
		qualitativeInputSheetServRequest.setBusinessCommitment(finalTermLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetServRequest.setManagementCompetence(finalTermLoanDetail.getManagementCompetence().longValue());
		qualitativeInputSheetServRequest.setBusinessExperience(finalTermLoanDetail.getBusinessExperience().longValue());
		qualitativeInputSheetServRequest.setSuccsessionPlanning(finalTermLoanDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetServRequest.setFinancialStrength(finalTermLoanDetail.getFinancialStrength().longValue());
		qualitativeInputSheetServRequest.setInternalControl(finalTermLoanDetail.getInternalControl().longValue());
		qualitativeInputSheetServRequest.setCreditTrackRecord(finalTermLoanDetail.getCreditTrackRecord().longValue());
		qualitativeInputSheetServRequest.setNumberCheckReturned(finalTermLoanDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetServRequest.setNumberTimesDpLimits(finalTermLoanDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetServRequest.setCumulativeDaysDpLimits(finalTermLoanDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetServRequest.setCompliancesWithSancationed(finalTermLoanDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetServRequest.setSubmissionProgressReport(finalTermLoanDetail.getProgressReports().longValue());
		qualitativeInputSheetServRequest.setDelayInReceiptPrincipal(finalTermLoanDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetServRequest.setDelayInSubmissionAudited(finalTermLoanDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetServRequest.setVarianceInProjectedSales(finalTermLoanDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetServRequest.setNumberOfLcBgIssuedInFavor(finalTermLoanDetail.getNumberOfLc().longValue());
		
		//---Contigent Liabilities set
		if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest))
			qualitativeInputSheetServRequest.setContingentLiabilities(0.0);
		else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability()))
			qualitativeInputSheetServRequest.setContingentLiabilities(0.0);
		else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()))
			qualitativeInputSheetServRequest.setContingentLiabilities(0.0);
		else
			qualitativeInputSheetServRequest.setContingentLiabilities((pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability() / pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()) *denom);//-----formula based

		return qualitativeInputSheetServRequest;		
	}
	
	public QualitativeInputSheetServRequest setUSLServiceQualitativeInput(Long aplicationId,Long userId,Long denom) throws Exception{
		QualitativeInputSheetServRequest qualitativeInputSheetServRequest = new QualitativeInputSheetServRequest();
		List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequest = new ArrayList<PastFinancialEstimatesDetailRequest>();
		pastFinancialEstimatesDetailRequest=pastFinancialEstiamateDetailsService.getPastFinancialEstimateDetailsList(aplicationId);
		
		FinalUnsecureLoanDetail finalUnsecureLoanDetail = null;
		finalUnsecureLoanDetail = finalUnsecuredLoanDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		
		qualitativeInputSheetServRequest.setAccountingQuality(finalUnsecureLoanDetail.getAccountingQuality().longValue());
		qualitativeInputSheetServRequest.setCustomerQuality(finalUnsecureLoanDetail.getCustomerQuality().longValue());
		qualitativeInputSheetServRequest.setSupplierQuality(finalUnsecureLoanDetail.getSupplierQuality().longValue());
		qualitativeInputSheetServRequest.setSustainabilityProductDemand(finalUnsecureLoanDetail.getSustainabilityProduct().longValue());
		qualitativeInputSheetServRequest.setProductSeasonality(finalUnsecureLoanDetail.getProductSeasonality().longValue());
		qualitativeInputSheetServRequest.setImpactOnOperatingMargins(finalUnsecureLoanDetail.getImpactOnOperatingMargins().longValue());
		qualitativeInputSheetServRequest.setEnvironmentImpact(finalUnsecureLoanDetail.getEnvironmentalImpact().longValue());
		qualitativeInputSheetServRequest.setIntegrity(finalUnsecureLoanDetail.getIntegrity().longValue());
		qualitativeInputSheetServRequest.setBusinessCommitment(finalUnsecureLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetServRequest.setManagementCompetence(finalUnsecureLoanDetail.getManagementCompetence().longValue());
		qualitativeInputSheetServRequest.setBusinessExperience(finalUnsecureLoanDetail.getBusinessExperience().longValue());
		qualitativeInputSheetServRequest.setSuccsessionPlanning(finalUnsecureLoanDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetServRequest.setFinancialStrength(finalUnsecureLoanDetail.getFinancialStrength().longValue());
		qualitativeInputSheetServRequest.setInternalControl(finalUnsecureLoanDetail.getInternalControl().longValue());
		qualitativeInputSheetServRequest.setCreditTrackRecord(finalUnsecureLoanDetail.getCreditTrackRecord().longValue());
		qualitativeInputSheetServRequest.setNumberCheckReturned(finalUnsecureLoanDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetServRequest.setNumberTimesDpLimits(finalUnsecureLoanDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetServRequest.setCumulativeDaysDpLimits(finalUnsecureLoanDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetServRequest.setCompliancesWithSancationed(finalUnsecureLoanDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetServRequest.setSubmissionProgressReport(finalUnsecureLoanDetail.getProgressReports().longValue());
		qualitativeInputSheetServRequest.setDelayInReceiptPrincipal(finalUnsecureLoanDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetServRequest.setDelayInSubmissionAudited(finalUnsecureLoanDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetServRequest.setVarianceInProjectedSales(finalUnsecureLoanDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetServRequest.setNumberOfLcBgIssuedInFavor(finalUnsecureLoanDetail.getNumberOfLc().longValue());
		
		//---Contigent Liabilities set
		if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest))
			qualitativeInputSheetServRequest.setContingentLiabilities(0.0);
		else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability()))
			qualitativeInputSheetServRequest.setContingentLiabilities(0.0);
		else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()))
			qualitativeInputSheetServRequest.setContingentLiabilities(0.0);
		else
			qualitativeInputSheetServRequest.setContingentLiabilities((pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability() / pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()) *denom);//-----formula based

		return qualitativeInputSheetServRequest;		
	}
	*/
	
	
	
	@Override
	public QualitativeInputSheetTradRequest qualitativeInputServiceTrading(Long aplicationId, Long userId, Integer productId,Boolean isCmaUploaded, Boolean isCoActUploaded,Long denom, Long proposalMapId)
			throws Exception {

		QualitativeInputSheetTradRequest qualitativeInputSheetTradRequest = new QualitativeInputSheetTradRequest();

		return setTradingQualitativeInput(aplicationId,userId ,isCmaUploaded, isCoActUploaded, denom, proposalMapId);
		/*LoanType type = CommonUtils.LoanType.getType(productId);
		switch (type) {
		case WORKING_CAPITAL:
			return setWCTradingQualitativeInput(aplicationId,userId, denom);
		case TERM_LOAN:
			return setTLTradingQualitativeInput(aplicationId,userId, denom);
		case UNSECURED_LOAN :
			return setUSLTradingQualitativeInput(aplicationId,userId, denom);
		}*/
	}

	public QualitativeInputSheetTradRequest setTradingQualitativeInput(Long aplicationId,Long userId,Boolean isCmaUploaded, Boolean isCoActUploaded, Long denom, Long proposalMapId) throws Exception{
		QualitativeInputSheetTradRequest qualitativeInputSheetTradRequest = new QualitativeInputSheetTradRequest();
		
		CorporateMcqDetail corporateMcqDetail = null;
//		corporateMcqDetail = corporateMcqDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		corporateMcqDetail = corporateMcqDetailRepository.getByProposalIdAndUserId(proposalMapId);
		
		qualitativeInputSheetTradRequest.setAccountingQuality(corporateMcqDetail.getAccountingQuality().longValue());
		qualitativeInputSheetTradRequest.setCustomerQuality(corporateMcqDetail.getCustomerQuality().longValue());
		qualitativeInputSheetTradRequest.setSupplierQuality(corporateMcqDetail.getSupplierQuality().longValue());
		qualitativeInputSheetTradRequest.setSustainabilityProductDemand(corporateMcqDetail.getSustainabilityProduct().longValue());
		qualitativeInputSheetTradRequest.setProductSeasonality(corporateMcqDetail.getProductSeasonality().longValue());
		qualitativeInputSheetTradRequest.setImpactOnOperatingMargins(corporateMcqDetail.getImpactOnOperatingMargins().longValue());
		qualitativeInputSheetTradRequest.setEnvironmentImpact(corporateMcqDetail.getEnvironmentalImpact().longValue());
		qualitativeInputSheetTradRequest.setIntegrity(corporateMcqDetail.getIntegrity().longValue());
		qualitativeInputSheetTradRequest.setBusinessCommitment(corporateMcqDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetTradRequest.setManagementCompetence(corporateMcqDetail.getManagementCompetence().longValue());
		qualitativeInputSheetTradRequest.setBusinessExperience(corporateMcqDetail.getBusinessExperience().longValue());
		qualitativeInputSheetTradRequest.setSuccsessionPlanning(corporateMcqDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetTradRequest.setFinancialStrength(corporateMcqDetail.getFinancialStrength().longValue());
		qualitativeInputSheetTradRequest.setInternalControl(corporateMcqDetail.getInternalControl().longValue());
		qualitativeInputSheetTradRequest.setCreditTrackRecord(corporateMcqDetail.getCreditTrackRecord().longValue());
		qualitativeInputSheetTradRequest.setNumberCheckReturned(corporateMcqDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetTradRequest.setNumberTimesDpLimits(corporateMcqDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetTradRequest.setCumulativeDaysDpLimits(corporateMcqDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetTradRequest.setCompliancesWithSancationed(corporateMcqDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetTradRequest.setSubmissionProgressReport(corporateMcqDetail.getProgressReports().longValue());
		qualitativeInputSheetTradRequest.setDelayInReceiptPrincipal(corporateMcqDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetTradRequest.setDelayInSubmissionAudited(corporateMcqDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetTradRequest.setVarianceInProjectedSales(corporateMcqDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetTradRequest.setNumberOfLcBgIssuedInFavor(corporateMcqDetail.getNumberOfLc().longValue());
		
		//---Contigent Liabilities set
		CorporateFinalInfoRequest  corporateFinalInfoRequest = new CorporateFinalInfoRequest();
		corporateFinalInfoRequest = corporateFinalInfoService.get(userId ,aplicationId);
		int currentYear = scoringService.getFinYear(aplicationId);
		if(isCmaUploaded) {
			AssetsDetails assetsDetails = new AssetsDetails();
			assetsDetails = assetsDetailsRepository.getAssetsDetails(aplicationId, currentYear-1+"");
			if(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilityFyAmt()))
				qualitativeInputSheetTradRequest.setContingentLiabilities(0.0);//-----formula based
			else if(CommonUtils.isObjectNullOrEmpty(assetsDetails.getTangibleNetWorth()))
				qualitativeInputSheetTradRequest.setContingentLiabilities(0.0);//-----formula based
			else
				qualitativeInputSheetTradRequest.setContingentLiabilities((corporateFinalInfoRequest.getContLiabilityFyAmt() / assetsDetails.getTangibleNetWorth()) * denom);//-----formula based
		}

		return qualitativeInputSheetTradRequest;		
	}

	public List<CreditRatingCompanyDetail> getCompanyDetails(String companyName){
		return creditRatingCompanyDetailsRepository.getCompanyDetail(companyName);
	}

	public List<CreditRatingCompanyDetail> getAllCompanyDetail(){
		return creditRatingCompanyDetailsRepository.findAll();
	}

	/*public QualitativeInputSheetTradRequest setTLTradingQualitativeInput(Long aplicationId,Long userId,Long denom) throws Exception{
		QualitativeInputSheetTradRequest qualitativeInputSheetTradRequest = new QualitativeInputSheetTradRequest();
		List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequest = new ArrayList<PastFinancialEstimatesDetailRequest>();
		pastFinancialEstimatesDetailRequest=pastFinancialEstiamateDetailsService.getPastFinancialEstimateDetailsList(aplicationId);
		
		FinalTermLoanDetail finalTermLoanDetail = null;
		finalTermLoanDetail = finalTermLoanDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		
		qualitativeInputSheetTradRequest.setAccountingQuality(finalTermLoanDetail.getAccountingQuality().longValue());
		qualitativeInputSheetTradRequest.setCustomerQuality(finalTermLoanDetail.getCustomerQuality().longValue());
		qualitativeInputSheetTradRequest.setSupplierQuality(finalTermLoanDetail.getSupplierQuality().longValue());
		qualitativeInputSheetTradRequest.setSustainabilityProductDemand(finalTermLoanDetail.getSustainabilityProduct().longValue());
		qualitativeInputSheetTradRequest.setProductSeasonality(finalTermLoanDetail.getProductSeasonality().longValue());
		qualitativeInputSheetTradRequest.setImpactOnOperatingMargins(finalTermLoanDetail.getImpactOnOperatingMargins().longValue());
		qualitativeInputSheetTradRequest.setEnvironmentImpact(finalTermLoanDetail.getEnvironmentalImpact().longValue());
		qualitativeInputSheetTradRequest.setIntegrity(finalTermLoanDetail.getIntegrity().longValue());
		qualitativeInputSheetTradRequest.setBusinessCommitment(finalTermLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetTradRequest.setManagementCompetence(finalTermLoanDetail.getManagementCompetence().longValue());
		qualitativeInputSheetTradRequest.setBusinessExperience(finalTermLoanDetail.getBusinessExperience().longValue());
		qualitativeInputSheetTradRequest.setSuccsessionPlanning(finalTermLoanDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetTradRequest.setFinancialStrength(finalTermLoanDetail.getFinancialStrength().longValue());
		qualitativeInputSheetTradRequest.setInternalControl(finalTermLoanDetail.getInternalControl().longValue());
		qualitativeInputSheetTradRequest.setCreditTrackRecord(finalTermLoanDetail.getCreditTrackRecord().longValue());
		qualitativeInputSheetTradRequest.setNumberCheckReturned(finalTermLoanDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetTradRequest.setNumberTimesDpLimits(finalTermLoanDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetTradRequest.setCumulativeDaysDpLimits(finalTermLoanDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetTradRequest.setCompliancesWithSancationed(finalTermLoanDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetTradRequest.setSubmissionProgressReport(finalTermLoanDetail.getProgressReports().longValue());
		qualitativeInputSheetTradRequest.setDelayInReceiptPrincipal(finalTermLoanDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetTradRequest.setDelayInSubmissionAudited(finalTermLoanDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetTradRequest.setVarianceInProjectedSales(finalTermLoanDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetTradRequest.setNumberOfLcBgIssuedInFavor(finalTermLoanDetail.getNumberOfLc().longValue());
		
		//---Contigent Liabilities set
				if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest))
					qualitativeInputSheetTradRequest.setContingentLiabilities(0.0);
				else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability()))
					qualitativeInputSheetTradRequest.setContingentLiabilities(0.0);
				else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()))
					qualitativeInputSheetTradRequest.setContingentLiabilities(0.0);
				else
					qualitativeInputSheetTradRequest.setContingentLiabilities((pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability() / pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()) *denom);//-----formula based

		return qualitativeInputSheetTradRequest;		
	}
	
	public QualitativeInputSheetTradRequest setUSLTradingQualitativeInput(Long aplicationId,Long userId,Long denom) throws Exception{
		QualitativeInputSheetTradRequest qualitativeInputSheetTradRequest = new QualitativeInputSheetTradRequest();
		List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequest = new ArrayList<PastFinancialEstimatesDetailRequest>();
		pastFinancialEstimatesDetailRequest=pastFinancialEstiamateDetailsService.getPastFinancialEstimateDetailsList(aplicationId);
		
		FinalUnsecureLoanDetail finalUnsecureLoanDetail = null;
		finalUnsecureLoanDetail = finalUnsecuredLoanDetailRepository.getByApplicationAndUserId(aplicationId,userId);
		
		qualitativeInputSheetTradRequest.setAccountingQuality(finalUnsecureLoanDetail.getAccountingQuality().longValue());
		qualitativeInputSheetTradRequest.setCustomerQuality(finalUnsecureLoanDetail.getCustomerQuality().longValue());
		qualitativeInputSheetTradRequest.setSupplierQuality(finalUnsecureLoanDetail.getSupplierQuality().longValue());
		qualitativeInputSheetTradRequest.setSustainabilityProductDemand(finalUnsecureLoanDetail.getSustainabilityProduct().longValue());
		qualitativeInputSheetTradRequest.setProductSeasonality(finalUnsecureLoanDetail.getProductSeasonality().longValue());
		qualitativeInputSheetTradRequest.setImpactOnOperatingMargins(finalUnsecureLoanDetail.getImpactOnOperatingMargins().longValue());
		qualitativeInputSheetTradRequest.setEnvironmentImpact(finalUnsecureLoanDetail.getEnvironmentalImpact().longValue());
		qualitativeInputSheetTradRequest.setIntegrity(finalUnsecureLoanDetail.getIntegrity().longValue());
		qualitativeInputSheetTradRequest.setBusinessCommitment(finalUnsecureLoanDetail.getBusinessCommitment().longValue());
		qualitativeInputSheetTradRequest.setManagementCompetence(finalUnsecureLoanDetail.getManagementCompetence().longValue());
		qualitativeInputSheetTradRequest.setBusinessExperience(finalUnsecureLoanDetail.getBusinessExperience().longValue());
		qualitativeInputSheetTradRequest.setSuccsessionPlanning(finalUnsecureLoanDetail.getSuccessionPlanning().longValue());
		qualitativeInputSheetTradRequest.setFinancialStrength(finalUnsecureLoanDetail.getFinancialStrength().longValue());
		qualitativeInputSheetTradRequest.setInternalControl(finalUnsecureLoanDetail.getInternalControl().longValue());
		qualitativeInputSheetTradRequest.setCreditTrackRecord(finalUnsecureLoanDetail.getCreditTrackRecord().longValue());
		qualitativeInputSheetTradRequest.setNumberCheckReturned(finalUnsecureLoanDetail.getNumberOfCheques().longValue());
		qualitativeInputSheetTradRequest.setNumberTimesDpLimits(finalUnsecureLoanDetail.getNumberOfTimesDp().longValue());
		qualitativeInputSheetTradRequest.setCumulativeDaysDpLimits(finalUnsecureLoanDetail.getCumulativeNoOfDaysDp().longValue());
		qualitativeInputSheetTradRequest.setCompliancesWithSancationed(finalUnsecureLoanDetail.getComplianceWithSanctioned().longValue());
		qualitativeInputSheetTradRequest.setSubmissionProgressReport(finalUnsecureLoanDetail.getProgressReports().longValue());
		qualitativeInputSheetTradRequest.setDelayInReceiptPrincipal(finalUnsecureLoanDetail.getDelayInReceipt().longValue());
		qualitativeInputSheetTradRequest.setDelayInSubmissionAudited(finalUnsecureLoanDetail.getDelayInSubmission().longValue());
		qualitativeInputSheetTradRequest.setVarianceInProjectedSales(finalUnsecureLoanDetail.getVarianceInProjectedSales().longValue());
		qualitativeInputSheetTradRequest.setNumberOfLcBgIssuedInFavor(finalUnsecureLoanDetail.getNumberOfLc().longValue());
		
		//---Contigent Liabilities set
				if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest))
					qualitativeInputSheetTradRequest.setContingentLiabilities(0.0);
				else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability()))
					qualitativeInputSheetTradRequest.setContingentLiabilities(0.0);
				else if(CommonUtils.isObjectNullOrEmpty(pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()))
					qualitativeInputSheetTradRequest.setContingentLiabilities(0.0);
				else
					qualitativeInputSheetTradRequest.setContingentLiabilities((pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getContingentLiability() / pastFinancialEstimatesDetailRequest.get(pastFinancialEstimatesDetailRequest.size()-1).getNetWorth()) *denom);//-----formula based

		return qualitativeInputSheetTradRequest;		
	}
*/

}
