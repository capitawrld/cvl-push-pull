/**
 * 
 */
package com.capitaworld.service.loans.service.teaser.primaryview.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.api.ekyc.model.EkycResponse;
import com.capitaworld.api.ekyc.model.epf.request.EmployerRequest;
import com.capitaworld.api.eligibility.model.EligibililityRequest;
import com.capitaworld.api.eligibility.model.EligibilityResponse;
import com.capitaworld.cibil.api.model.CibilRequest;
import com.capitaworld.cibil.api.model.CibilScoreLogRequest;
import com.capitaworld.cibil.client.CIBILClient;
import com.capitaworld.client.ekyc.EPFClient;
import com.capitaworld.client.eligibility.EligibilityClient;
import com.capitaworld.connect.api.ConnectStage;
import com.capitaworld.connect.client.ConnectClient;
import com.capitaworld.itr.api.model.ITRBasicDetailsResponse;
import com.capitaworld.itr.api.model.ITRConnectionResponse;
import com.capitaworld.itr.client.ITRClient;
import com.capitaworld.service.analyzer.client.AnalyzerClient;
import com.capitaworld.service.analyzer.model.common.AnalyzerResponse;
import com.capitaworld.service.analyzer.model.common.Data;
import com.capitaworld.service.analyzer.model.common.ReportRequest;
import com.capitaworld.service.dms.client.DMSClient;
import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.model.DocumentRequest;
import com.capitaworld.service.dms.model.DocumentResponse;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.model.PincodeDataResponse;
import com.capitaworld.service.loans.model.retail.BankAccountHeldDetailsRequest;
import com.capitaworld.service.loans.model.retail.FixedDepositsDetailsRequest;
import com.capitaworld.service.loans.model.retail.ObligationDetailRequest;
import com.capitaworld.service.loans.model.retail.OtherCurrentAssetDetailRequest;
import com.capitaworld.service.loans.model.retail.PLRetailApplicantRequest;
import com.capitaworld.service.loans.model.retail.PLRetailApplicantResponse;
import com.capitaworld.service.loans.model.retail.ReferenceRetailDetailsRequest;
import com.capitaworld.service.loans.model.retail.RetailApplicantIncomeRequest;
import com.capitaworld.service.loans.model.retail.RetailFinalInfoRequest;
import com.capitaworld.service.loans.model.teaser.primaryview.PlTeaserViewResponse;
import com.capitaworld.service.loans.repository.common.CommonRepository;
import com.capitaworld.service.loans.repository.fundprovider.ProductMasterRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.capitaworld.service.loans.service.common.PincodeDateService;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateFinalInfoService;
import com.capitaworld.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.capitaworld.service.loans.service.fundseeker.retail.BankAccountHeldDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.FixedDepositsDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.ObligationDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.OtherCurrentAssetDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.PlRetailApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.ReferenceRetailDetailsService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantIncomeService;
import com.capitaworld.service.loans.service.teaser.primaryview.PlTeaserViewService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.matchengine.MatchEngineClient;
import com.capitaworld.service.matchengine.ProposalDetailsClient;
import com.capitaworld.service.matchengine.exception.MatchException;
import com.capitaworld.service.matchengine.model.MatchDisplayResponse;
import com.capitaworld.service.matchengine.model.MatchRequest;
import com.capitaworld.service.matchengine.model.ProposalMappingRequest;
import com.capitaworld.service.matchengine.model.ProposalMappingRequestString;
import com.capitaworld.service.matchengine.model.ProposalMappingResponse;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.CastCategory;
import com.capitaworld.service.oneform.enums.Currency;
import com.capitaworld.service.oneform.enums.DesignationList;
import com.capitaworld.service.oneform.enums.DisabilityType;
import com.capitaworld.service.oneform.enums.EducationStatusRetailMst;
import com.capitaworld.service.oneform.enums.EmploymentStatusRetailMst;
import com.capitaworld.service.oneform.enums.EmploymentWithPL;
import com.capitaworld.service.oneform.enums.Gender;
import com.capitaworld.service.oneform.enums.GetStringFromIdForMasterData;
import com.capitaworld.service.oneform.enums.LoanPurposePL;
import com.capitaworld.service.oneform.enums.LoanType;
import com.capitaworld.service.oneform.enums.MaritalStatusMst;
import com.capitaworld.service.oneform.enums.OccupationNature;
import com.capitaworld.service.oneform.enums.ReligionRetailMst;
import com.capitaworld.service.oneform.enums.ResidenceStatusRetailMst;
import com.capitaworld.service.oneform.enums.ResidentStatusMst;
import com.capitaworld.service.oneform.enums.ResidentialStatus;
import com.capitaworld.service.oneform.enums.SalaryModeMst;
import com.capitaworld.service.oneform.enums.SpouseEmploymentList;
import com.capitaworld.service.oneform.enums.Title;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;
import com.capitaworld.service.oneform.model.SectorIndustryModel;
import com.capitaworld.service.scoring.ScoringClient;
import com.capitaworld.service.scoring.exception.ScoringException;
import com.capitaworld.service.scoring.model.ProposalScoreResponse;
import com.capitaworld.service.scoring.model.ScoringRequest;
import com.capitaworld.service.scoring.model.ScoringResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author nilay.darji
 *
 */

@Service
@Transactional
public class PlTeaserViewServiceImpl implements PlTeaserViewService {

	private static final Logger logger = LoggerFactory.getLogger(PlTeaserViewServiceImpl.class);

	private static final String DISTRICT_ID_IS_NULL_MSG = "District id is null";

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private PrimaryCorporateDetailRepository primaryCorporateRepository;

	@Autowired
	private FinancialArrangementDetailsService financialArrangementDetailsService;
	
	@Autowired
	private CommonRepository commonRepository;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private MatchEngineClient matchEngineClient;
	
	@Autowired
	private CommonRepository commonRepo;

	@Autowired
	private ScoringClient scoringClient;

	@Autowired
	private AnalyzerClient analyzerClient;

	@Autowired
	private EligibilityClient eligibilityClient;
	
	@Autowired
	private PlRetailApplicantService plRetailApplicantService;
	
	@Autowired
	private RetailApplicantIncomeService retailApplicantIncomeService;
	
	@Autowired
	private CorporateFinalInfoService corporateFinalInfoService;
	
	@Autowired
	private PincodeDateService pincodeDateService;
	
	@Autowired
	private CIBILClient cibilClient;
	
	@Autowired
	private BankAccountHeldDetailService bankAccountHeldDetailsService;
	
	@Autowired
	private FixedDepositsDetailService fixedDepositsDetailService;
	
	@Autowired
	private OtherCurrentAssetDetailService otherCurrentAssetDetailsService;
	
	@Autowired
	private ObligationDetailService obligationDetailService;
	
	@Autowired
	private ReferenceRetailDetailsService referenceRetailDetailService;
	
	@Autowired
	private ConnectClient connectClient;
	
	@Autowired
	private ITRClient itrClient;
	
	@Autowired
	private ProposalDetailsClient proposalDetailsClient;
	
	@Autowired
	ApplicationProposalMappingRepository applicationProposalMappingRepository;
	
	@Autowired
	ProductMasterRepository productMasterRepository;
	
	@Autowired
	private EPFClient epfClient;
	
	
	

	@Override
	public PlTeaserViewResponse getPlPrimaryViewDetails(Long toApplicationId, Integer userType, Long userId,
			Long productMappingId, Boolean isFinal) {

		PlTeaserViewResponse plTeaserViewResponse = new PlTeaserViewResponse();

		LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.findOne(toApplicationId);
		Long userid=loanApplicationMaster.getUserId();	
		plTeaserViewResponse.setLoanType(loanApplicationMaster.getProductId() != null ? LoanType.getById(loanApplicationMaster.getProductId()).getValue().toString() : "");
		plTeaserViewResponse.setLoanAmount(loanApplicationMaster.getAmount().longValue());
		plTeaserViewResponse.setTenure(((loanApplicationMaster.getTenure()).toString()) + " Years");
		plTeaserViewResponse.setAppId(toApplicationId);
		

		 // CHANGES FOR DATE OF PROPOSAL(TEASER VIEW)	NEW CODE
			try {
				Object obj = "-";
				Date dateOfProposal = loanApplicationRepository.getModifiedDate(toApplicationId, ConnectStage.RETAIL_COMPLETE.getId());
				if(!CommonUtils.isObjectNullOrEmpty(dateOfProposal)) {
			     plTeaserViewResponse.setDateOfProposal(dateOfProposal);
				}else{
				plTeaserViewResponse.setDateOfProposal(obj);
				}
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			// ENDS HERE===================>


		/* ========= Matches Data ========== */
		if (userType != null && CommonUtils.UserType.FUND_SEEKER != userType ) {
			// TEASER VIEW FROM FP SIDE
				try {
					MatchRequest matchRequest = new MatchRequest();
					matchRequest.setApplicationId(toApplicationId);
					matchRequest.setProductId(productMappingId);
					matchRequest.setBusinessTypeId(loanApplicationMaster.getBusinessTypeId());
					MatchDisplayResponse matchResponse = matchEngineClient.displayMatchesOfRetail(matchRequest);
					plTeaserViewResponse.setMatchesList(matchResponse.getMatchDisplayObjectList());
				} catch (Exception e) {
					logger.error("Error while getting matches data :{} " , e);
				}
		}
		
		
		// basic Details
		
		PLRetailApplicantResponse plRetailApplicantResponse = null;
		try {
			PLRetailApplicantRequest plRetailApplicantRequest = plRetailApplicantService.getProfile(userid, toApplicationId);
			plRetailApplicantResponse = new PLRetailApplicantResponse();
			if(plRetailApplicantRequest != null) {
				
				/*-----  all primary details fetch from db (fs_retail_applicat_detail) and using enum ------*/

				plRetailApplicantResponse.setFullName((plRetailApplicantRequest.getFirstName() != null ? plRetailApplicantRequest.getFirstName() : "") +" "+ (plRetailApplicantRequest.getMiddleName() != null ? plRetailApplicantRequest.getMiddleName() : "") +" "+ (plRetailApplicantRequest.getLastName() != null ?  plRetailApplicantRequest.getLastName() : ""));
				plRetailApplicantResponse.setGender(plRetailApplicantRequest.getGenderId() != null ? Gender.getById(plRetailApplicantRequest.getGenderId()).getValue().toString() : "-");
				plRetailApplicantResponse.setBirthDate(plRetailApplicantRequest.getBirthDate());
				plRetailApplicantResponse.setPan(plRetailApplicantRequest.getPan());
				plRetailApplicantResponse.setAadharNumber(plRetailApplicantRequest.getAadharNumber());
				plRetailApplicantResponse.setMobile(plRetailApplicantRequest.getMobile());
				plRetailApplicantResponse.setEmploymentType(plRetailApplicantRequest.getEmploymentType() != null ? OccupationNature.getById(plRetailApplicantRequest.getEmploymentType()).getValue().toString() : "-");
				plRetailApplicantResponse.setNameOfEmployer(plRetailApplicantRequest.getNameOfEmployer());
				plRetailApplicantResponse.setEmploymentWith(plRetailApplicantRequest.getEmploymentWith() != null ? EmploymentWithPL.getById(plRetailApplicantRequest.getEmploymentWith()).getValue().toString() : "-");
				plRetailApplicantResponse.setEmploymentStatus(plRetailApplicantRequest.getEmploymentStatus() != null ? EmploymentStatusRetailMst.getById(plRetailApplicantRequest.getEmploymentStatus()).getValue().toString() : "-");
				plRetailApplicantResponse.setCurrentJobYear((plRetailApplicantRequest.getCurrentJobYear() !=null ? (plRetailApplicantRequest.getCurrentJobYear() +" year") : "") + " " +(plRetailApplicantRequest.getCurrentJobMonth() != null ? (plRetailApplicantRequest.getCurrentJobMonth() +" months") :  "" )); 
				plRetailApplicantResponse.setTotalExperienceYear((plRetailApplicantRequest.getTotalExperienceYear() !=null ? (plRetailApplicantRequest.getTotalExperienceYear() +" year") : "") + " " + (plRetailApplicantRequest.getTotalExperienceMonth() != null ? (plRetailApplicantRequest.getTotalExperienceMonth() +" months") :  "" ));
				plRetailApplicantResponse.setResidenceType(plRetailApplicantRequest.getResidenceType() != null ? ResidenceStatusRetailMst.getById(plRetailApplicantRequest.getResidenceType()).getValue().toString() : "-");
				plRetailApplicantResponse.setMaritalStatus(plRetailApplicantRequest.getStatusId() != null ? MaritalStatusMst.getById(plRetailApplicantRequest.getStatusId()).getValue().toString() : "-");
				plRetailApplicantResponse.setEducationQualificationString(plRetailApplicantRequest.getEducationQualification() != null ? EducationStatusRetailMst.getById(plRetailApplicantRequest.getEducationQualification()).getValue().toString() : "-");
				plRetailApplicantResponse.setSpouseEmployment(plRetailApplicantRequest.getSpouseEmployment() != null ? SpouseEmploymentList.getById(plRetailApplicantRequest.getSpouseEmployment()).getValue().toString() : "-");
				plRetailApplicantResponse.setDesignation(plRetailApplicantRequest.getDesignation()!= null ? DesignationList.getById(plRetailApplicantRequest.getDesignation()).getValue().toString() : "-");
				plRetailApplicantResponse.setNoOfDependent(plRetailApplicantRequest.getNoOfDependent());
				plRetailApplicantResponse.setResidenceSinceMonthYear((plRetailApplicantRequest.getResidenceSinceYear() !=null ? (plRetailApplicantRequest.getResidenceSinceYear() +" year") : "") + " " +(plRetailApplicantRequest.getResidenceSinceMonth()!= null ? (plRetailApplicantRequest.getResidenceSinceMonth()+" months") :  "" ));
				plRetailApplicantResponse.setSalaryMode(plRetailApplicantRequest.getSalaryMode()!=null ? SalaryModeMst.getById(plRetailApplicantRequest.getSalaryMode()).getValue().toString() : "-");

				/*salary account details*/
				plRetailApplicantResponse.setSalaryAccountBankName(plRetailApplicantRequest.getSalaryBankName());
				plRetailApplicantResponse.setIsOtherSalaryAccBank(plRetailApplicantRequest.getIsOtherSalaryBank()!=null ? plRetailApplicantRequest.getIsOtherSalaryBank() : false);

				if(plRetailApplicantRequest.getSalaryBankYear() !=null && plRetailApplicantRequest.getSalaryBankMonth()!= null) {

					LocalDate since = LocalDate.of(plRetailApplicantRequest.getSalaryBankYear(), plRetailApplicantRequest.getSalaryBankMonth(), 1);
			        LocalDate today = LocalDate.now();

			        Period age = Period.between(since, today);
			        int years = age.getYears();
			        int months = age.getMonths();

					plRetailApplicantResponse.setSalaryAccountBankSince(years+" year "+months+" months");
				}

				//citetailApplicantResponse.setry,State,country
				
				plTeaserViewResponse.setCity(CommonDocumentUtils.getCity(plRetailApplicantRequest.getAddressCity(), oneFormClient));
				plTeaserViewResponse.setState(CommonDocumentUtils.getState(plRetailApplicantRequest.getAddressState(), oneFormClient));
				plTeaserViewResponse.setCountry(CommonDocumentUtils.getCountry(plRetailApplicantRequest.getAddressCountry().longValue(), oneFormClient));
				
				// address
				
				try {
					if(plRetailApplicantRequest.getContactAddress() != null) {
						
						PincodeDataResponse pindata=pincodeDateService.getById(plRetailApplicantRequest.getContactAddress().getDistrictMappingId());
						plTeaserViewResponse.setPresentAddDist(pindata.getDistrictName());
						plTeaserViewResponse.setPresentAddTaluko(pindata.getTaluka());
						pindata.getTaluka();
					}else {
						logger.warn(DISTRICT_ID_IS_NULL_MSG);
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				
				if(plRetailApplicantRequest.getContactAddress() != null){
					
					plTeaserViewResponse.setPresentAdd( (plRetailApplicantRequest.getContactAddress().getPremiseNumber()!=null ? (CommonUtils.commaReplace(plRetailApplicantRequest.getContactAddress().getPremiseNumber())) :"") + (plRetailApplicantRequest.getContactAddress().getStreetName() != null ? (CommonUtils.commaReplace(plRetailApplicantRequest.getContactAddress().getStreetName())) : "") + (plRetailApplicantRequest.getContactAddress().getLandMark() != null ? (CommonUtils.commaReplace(plRetailApplicantRequest.getContactAddress().getLandMark())) : "")+ (plTeaserViewResponse.getPresentAddDist() != null ?(CommonUtils.commaReplace(plTeaserViewResponse.getPresentAddDist())) :"")+ (plTeaserViewResponse.getPresentAddTaluko() != null ? (CommonUtils.commaReplace(plTeaserViewResponse.getPresentAddTaluko())) : "") + (plRetailApplicantRequest.getContactAddress().getPincode() != null ? (plRetailApplicantRequest.getContactAddress().getPincode()) : ""));
				}
				
				/*banking relationship details*/

				
				// loan Details 
				
				plRetailApplicantResponse.setLoanAmountRequired(plRetailApplicantRequest.getLoanAmountRequired());
				plTeaserViewResponse.setPurposeOfLoan(plRetailApplicantRequest.getLoanPurpose() != null ? LoanPurposePL.getById(plRetailApplicantRequest.getLoanPurpose()).getValue().toString() : "NA");
				plRetailApplicantResponse.setTenureRequired(plRetailApplicantRequest.getTenureRequired());
				plRetailApplicantResponse.setRepayment(plRetailApplicantRequest.getRepayment());
				plRetailApplicantResponse.setMonthlyIncome(plRetailApplicantRequest.getMonthlyIncome());
				
				//set existing financial data if not null 
				
				if(plRetailApplicantRequest.getFinancialArrangementsDetailRequestsList() != null) {
					plRetailApplicantResponse.setFinancialArrangementsDetailRequestsList(plRetailApplicantRequest.getFinancialArrangementsDetailRequestsList());	
				}else {
					logger.warn("FinancialArrangementsDetail is null...");
				}
				
				//set credit card data if not null
				if(plRetailApplicantRequest.getCreditCardsDetailRequestList() != null) {
					
					plRetailApplicantResponse.setCreditCardsDetailRequestList(plRetailApplicantRequest.getCreditCardsDetailRequestList());	
				}else {
					logger.warn("CreditCardDetails is null...");
				}
				

				if(plRetailApplicantRequest.getBankingRelationshipList() != null) {
					plRetailApplicantResponse.setBankRelationShipList(plRetailApplicantRequest.getBankingRelationshipList());
				}else {
					logger.warn("bankRelationship  is null...");
				}

				//KEY VERTICAL FUNDING
				List<Long> keyVerticalFundingId = new ArrayList<>();
				if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalFunding()))
					keyVerticalFundingId.add(plRetailApplicantRequest.getKeyVerticalFunding());
				if (!CommonUtils.isListNullOrEmpty(keyVerticalFundingId)) {
					try {
						OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
						List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
						if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
							MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						    plTeaserViewResponse.setKeyVericalFunding(masterResponse.getValue());
						} else {
							logger.warn("key vertical funding is null");
						}

					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
				//KEY VERTICAL SECTOR
				List<Long> keyVerticalSectorId = new ArrayList<>();
				if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalSector()))
					keyVerticalSectorId.add(plRetailApplicantRequest.getKeyVerticalSector());
				try {
					OneFormResponse formResponse = oneFormClient.getIndustrySecByMappingId(plRetailApplicantRequest.getKeyVerticalSector());
					SectorIndustryModel sectorIndustryModel = MultipleJSONObjectHelper.getObjectFromMap((Map) formResponse.getData(), SectorIndustryModel.class);
					OneFormResponse oneFormResponse = oneFormClient.getSectorById(Arrays.asList(sectorIndustryModel.getSectorId()));
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						plTeaserViewResponse.setKeyVericalSector(masterResponse.getValue());
					} else {
						logger.warn("key vertical sector is null");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				//KEY VERTICAL SUBSECTOR
				try {
					if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalSubSector())) {
						OneFormResponse oneFormResponse = oneFormClient.getSubSecNameByMappingId(plRetailApplicantRequest.getKeyVerticalSubSector());
						plTeaserViewResponse.setKeyVericalSubsector(oneFormResponse.getData().toString());
					}
				} catch (Exception e) {
					logger.warn("key vertical subSector is null ");
				}
				
				
				plTeaserViewResponse.setRetailApplicantDetail(plRetailApplicantResponse);
				
			}else {
				logger.warn("retailApplicantDetail is null");
			}
			
		} catch (Exception e) {
			logger.error("error while fetching retailApplicantDetails : ",e);
		}
		
		/*get epfoData*/
		/*EmployerRequest epfReq=new EmployerRequest();
		epfReq.setApplicationId(toApplicationId);
		EkycResponse epfRes=epfClient.getEpfData(epfReq);
		if(epfRes != null && epfRes.getData()!= null) {
			plTeaserViewResponse.setEpfData(epfRes.getData());
		}else {
			logger.info("epfo data is null for===>>"+toApplicationId);
		}*/
		 
		
		//PROPOSAL RESPONSE
				try {
					ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
					proposalMappingRequest.setApplicationId(toApplicationId);
					proposalMappingRequest.setFpProductId(productMappingId);
					ProposalMappingResponse proposalMappingResponse= proposalDetailsClient.getActiveProposalDetails(proposalMappingRequest);
					if(proposalMappingResponse.getData() != null) {
					
						plTeaserViewResponse.setProposalData(proposalMappingResponse.getData());
						
					}else {
						logger.info("proposal data is null");
					}

				}catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				
		//cibil score
		
				try {
					CibilRequest cibilReq=new CibilRequest();
					cibilReq.setPan(plRetailApplicantResponse.getPan());
					cibilReq.setApplicationId(toApplicationId);
					CibilScoreLogRequest cibilScoreByPanCard = cibilClient.getCibilScoreByPanCard(cibilReq);
					plTeaserViewResponse.setCibilScore(cibilScoreByPanCard);
				} catch (Exception e) {
					logger.error("Error While calling Cibil Score By PanCard : ",e);
				}
		
		// Income Details
		
		try {
			
			List<RetailApplicantIncomeRequest> retailApplicantIncomeDetail = retailApplicantIncomeService.getAll(toApplicationId);
			
			if(retailApplicantIncomeDetail != null) {
				plTeaserViewResponse.setRetailApplicantIncomeDetails(retailApplicantIncomeDetail);	
			}else {
				logger.warn("..........::::::::----->>retailApplicantIncomeDetail is null<<-----:::::::::.....");
				
			}
			
			
		} catch (Exception e) {
			logger.error("..........::::::::----->> Error while calling PL Income Details <<-----:::::::::.....",e);
		}
		
		// bank statement data
		ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(toApplicationId);
		reportRequest.setUserId(userId);
		List<Data> datas = new ArrayList<>();
		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			List<HashMap<String, Object>> hashMaps = (List<HashMap<String, Object>>) analyzerResponse.getData();
			if (!CommonUtils.isListNullOrEmpty(hashMaps)) {
				for (HashMap<String, Object> hashMap : hashMaps) {
					Data data = MultipleJSONObjectHelper.getObjectFromMap(hashMap, Data.class);
					datas.add(data);
				}
			}
			plTeaserViewResponse.setBankData(datas);
		
		} catch (Exception e) {
			logger.error("Error while getting perfios data : ",e);
		}

		// SCORING DATA
		ScoringRequest scoringRequest = new ScoringRequest();
		scoringRequest.setApplicationId(toApplicationId);
		scoringRequest.setFpProductId(productMappingId);

		try {

			ScoringResponse scoringResponse = scoringClient.getScore(scoringRequest);
			ProposalScoreResponse proposalScoreResponse = MultipleJSONObjectHelper.getObjectFromMap(
					(LinkedHashMap<String, Object>) scoringResponse.getDataObject(), ProposalScoreResponse.class);

			if (proposalScoreResponse != null){
				logger.info("getObjectFromMap called successfully");
			}

			plTeaserViewResponse.setDataList(scoringResponse.getDataList());
			plTeaserViewResponse.setDataObject(scoringResponse.getDataObject());
			plTeaserViewResponse.setScoringResponseList(scoringResponse.getScoringResponseList());

		} catch (ScoringException | IOException e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}

		// Eligibility Data

		EligibililityRequest eligibilityReq = new EligibililityRequest();
		eligibilityReq.setApplicationId(toApplicationId);
		eligibilityReq.setFpProductMappingId(productMappingId);
		logger.info(" for eligibility appid============>>{}" , toApplicationId);

		try {

			EligibilityResponse eligibilityResp = eligibilityClient.getRetailLoanData(eligibilityReq);

			plTeaserViewResponse.setEligibilityDataObject(eligibilityResp.getData());

		} catch (Exception e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}
		
		try {
			ITRConnectionResponse resNameAsPerITR = itrClient.getIsUploadAndYearDetails(toApplicationId);
			if (resNameAsPerITR != null) {

				plTeaserViewResponse.setNameAsPerItr(resNameAsPerITR.getData() != null ? resNameAsPerITR.getData() : "NA");
			} else {

				logger.warn("-----------:::::::::::::: ItrResponse is null ::::::::::::---------");
			}

		} catch (Exception e) {
			logger.error(":::::::::::---------Error while fetching name as per itr----------:::::::::::",e);
		}
		
		
		// GET DOCUMENTS
				DocumentRequest documentRequest = new DocumentRequest();
				documentRequest.setApplicationId(toApplicationId);
				documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
				documentRequest.setProductDocumentMappingId(DocumentAlias.WORKING_CAPITAL_PROFIEL_PICTURE);
				try {
					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					plTeaserViewResponse.setProfilePic(documentResponse.getDataList());
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				documentRequest.setProductDocumentMappingId(DocumentAlias.WORKING_CAPITAL_BANK_STATEMENT);
				try {
					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					plTeaserViewResponse.setBankStatement(documentResponse.getDataList());
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				documentRequest.setProductDocumentMappingId(DocumentAlias.RETAIL_ITR_PDF);
				try {
					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					plTeaserViewResponse.setIrtPdfReport(documentResponse.getDataList());
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				documentRequest.setProductDocumentMappingId(DocumentAlias.RETAIL_ITR_XML);
				try {
					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					plTeaserViewResponse.setIrtXMLReport(documentResponse.getDataList());
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				documentRequest.setProductDocumentMappingId(DocumentAlias.CIBIL_SOFTPING_CONSUMER);
				try {
					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					plTeaserViewResponse.setCibilReport(documentResponse.getDataList());
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				
		

		// pl final view details filled from here
		if (isFinal) {
			
			try {
				
				RetailFinalInfoRequest retailFinalInfo = plRetailApplicantService.getFinal(userId, toApplicationId);
				
				if(retailFinalInfo != null) {
				
					plTeaserViewResponse.setReligion(retailFinalInfo.getReligion() != null ? ReligionRetailMst.getById(retailFinalInfo.getReligion()).getValue().toString() : "-");
					plTeaserViewResponse.setResidentialStatus(retailFinalInfo.getResidentialStatus() != null ? ResidentialStatus.getById(retailFinalInfo.getResidentialStatus()).getValue().toString() : "-");
					plTeaserViewResponse.setCastCategory(retailFinalInfo.getCastId() != null ? CastCategory.getById(retailFinalInfo.getCastId()).getValue().toString() : "-");
					plTeaserViewResponse.setDiasablityType(retailFinalInfo.getDisabilityType() != null ? DisabilityType.getById(retailFinalInfo.getDisabilityType()).getValue().toString() : "-");
					plTeaserViewResponse.setDdoOrganizationType(retailFinalInfo.getDdoOrganizationType() != null ? EmploymentWithPL.getById(retailFinalInfo.getDdoOrganizationType()).getValue().toString() : "-");
					
					//permanent address
					try {
						if(retailFinalInfo != null && retailFinalInfo.getPermanentAddress().getDistrictMappingId() != null) {
							PincodeDataResponse pindata=pincodeDateService.getById(retailFinalInfo.getPermanentAddress().getDistrictMappingId());
							plTeaserViewResponse.setPermAddDist(pindata.getDistrictName());
							plTeaserViewResponse.setPermAddTaluko(pindata.getTaluka());
							pindata.getTaluka();
						}else {
							logger.warn(DISTRICT_ID_IS_NULL_MSG);
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					
					if(retailFinalInfo.getPermanentAddress() != null){	
						plTeaserViewResponse.setPermAdd( (retailFinalInfo.getPermanentAddress().getPremiseNumber()!=null ? (CommonUtils.commaReplace(retailFinalInfo.getPermanentAddress().getPremiseNumber())) :"") + (retailFinalInfo.getPermanentAddress().getStreetName() != null ? (CommonUtils.commaReplace(retailFinalInfo.getPermanentAddress().getStreetName())) : "") + (retailFinalInfo.getPermanentAddress().getLandMark() != null ? (CommonUtils.commaReplace(retailFinalInfo.getPermanentAddress().getLandMark())) : "")+ (plTeaserViewResponse.getPermAddDist() != null ?(CommonUtils.commaReplace(plTeaserViewResponse.getPermAddDist())) :"")+ (plTeaserViewResponse.getPermAddTaluko() != null ? (CommonUtils.commaReplace(plTeaserViewResponse.getPermAddTaluko())) : "") + (retailFinalInfo.getPermanentAddress().getPincode() != null ? (retailFinalInfo.getPermanentAddress().getPincode()) : ""));
					}
					
					//Office address
					try {
						if(retailFinalInfo != null && retailFinalInfo.getOfficeAddress().getDistrictMappingId() !=null) {
							PincodeDataResponse pindata=pincodeDateService.getById(retailFinalInfo.getOfficeAddress().getDistrictMappingId());
							plTeaserViewResponse.setOffAddDist(pindata.getDistrictName());
							plTeaserViewResponse.setOffAddTaluko(pindata.getTaluka());
							pindata.getTaluka();
						}else {
							logger.warn(DISTRICT_ID_IS_NULL_MSG);
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					
					if(retailFinalInfo.getOfficeAddress() != null){
						plTeaserViewResponse.setOffAdd( (retailFinalInfo.getOfficeAddress().getPremiseNumber()!=null ? (CommonUtils.commaReplace(retailFinalInfo.getOfficeAddress().getPremiseNumber())) :"") + (retailFinalInfo.getOfficeAddress().getStreetName() != null ? (CommonUtils.commaReplace(retailFinalInfo.getOfficeAddress().getStreetName())) : "") + (retailFinalInfo.getOfficeAddress().getLandMark() != null ? (CommonUtils.commaReplace(retailFinalInfo.getOfficeAddress().getLandMark())) : "")+ (plTeaserViewResponse.getOffAddDist() != null ?(CommonUtils.commaReplace(plTeaserViewResponse.getOffAddDist())) :"")+ (plTeaserViewResponse.getOffAddTaluko() != null ? (CommonUtils.commaReplace(plTeaserViewResponse.getOffAddTaluko())) : "") + (retailFinalInfo.getOfficeAddress().getPincode() != null ? (retailFinalInfo.getOfficeAddress().getPincode()) : ""));
					}
					
					
					
					plTeaserViewResponse.setFinalDetails(retailFinalInfo);
					
				}else {
					logger.warn("Retail Final Info is Null....");
				}
				
				
				
				
			} catch (Exception e) {
				logger.error("Error while fetching RetailFinalData : ",e);
			}
			
			
			//BANK ACCOUNT HELD DETAILS
			try {
				List<BankAccountHeldDetailsRequest> bankAccountHeldDetails = bankAccountHeldDetailsService.getExistingLoanDetailList(toApplicationId, 1);
				if(bankAccountHeldDetails != null) {
					
					plTeaserViewResponse.setBankAccountDetails(bankAccountHeldDetails);
					
				}else {
					logger.warn("Bank Held Details is Null....");
				}
				
			} catch (Exception e) {
				logger.error("Error while getting bank account held details : ",e);
			}
			
			//FIXED DEPOSITS DETAILS
			try {
				List<FixedDepositsDetailsRequest> fixedDepositeDetails = fixedDepositsDetailService.getFixedDepositsDetailList(toApplicationId, 1);
					if(fixedDepositeDetails != null) {
					
					plTeaserViewResponse.setFixDepositDetails(fixedDepositeDetails);
					
				}else {
					logger.warn("Fix Deposit Details is Null....");
				}
			} catch (Exception e) {
				logger.error("Error while getting fixed deposite details : ",e);
			}
			
			//OTHER CURRENT ASSEST DETAILS
			try {
				List<OtherCurrentAssetDetailRequest> otherCurrentAssetDetails = otherCurrentAssetDetailsService.getOtherCurrentAssetDetailList(toApplicationId,1);
					
				if(otherCurrentAssetDetails != null) {
					
					plTeaserViewResponse.setOtherCurruntAssetDetail(otherCurrentAssetDetails);
					
				}else {
					logger.warn("Other Currnt Asset Details is Null....");
				}
			} catch (Exception e) {
				logger.error("Error while getting other current asset details : ",e);
			}
			
			//OBLIGATION DETAILS
			try {
				
				List<ObligationDetailRequest> obligationRequest = obligationDetailService.getObligationDetailList(toApplicationId,1);
				
				if(obligationRequest != null) {
					
					plTeaserViewResponse.setObligationDetails(obligationRequest);
					
				}else {
					logger.warn("Obligation Details is Null....");
				}
				
			} catch (Exception e) {
				logger.error("Error while getting obligation details : ",e);
			}
			
			//REFERENCES DETAILS
			try {
				List<ReferenceRetailDetailsRequest> referenceDetails = referenceRetailDetailService.getReferenceRetailDetailList(toApplicationId,1);
				if(referenceDetails !=null) {
					
					plTeaserViewResponse.setReferenceDetails(referenceDetails);
				}else {
					logger.warn("Reference Details is Null...");
				}
			} catch (Exception e) {
				logger.error("Error while getting reference details : ",e);
			}
			
			DocumentRequest finalDocumentRequest = new DocumentRequest();
			documentRequest.setApplicationId(toApplicationId);
			documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
			documentRequest.setProductDocumentMappingId(DocumentAlias.WORKING_CAPITAL_PROFIEL_PICTURE);
			try {
				DocumentResponse documentResponse = dmsClient.listProductDocument(finalDocumentRequest);
				plTeaserViewResponse.setProfilePic(documentResponse.getDataList());
			} catch (DocumentException e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}

		}
		return plTeaserViewResponse;
	}
	
	@Override
	public PlTeaserViewResponse getPlPrimaryViewDetailsByProposalId(Long toApplicationId, Integer userType, Long userId,Long productMappingId, Boolean isFinal, Long proposalId) {

		PlTeaserViewResponse plTeaserViewResponse = new PlTeaserViewResponse();

//		LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.findOne(toApplicationId);
		ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.findOne(proposalId);
		
		Long userid=applicationProposalMapping.getUserId();
		plTeaserViewResponse.setLoanType(applicationProposalMapping.getProductId() != null ? LoanType.getById(applicationProposalMapping.getProductId()).getValue().toString() : "");
		plTeaserViewResponse.setLoanAmount(applicationProposalMapping.getLoanAmount().longValue());
		plTeaserViewResponse.setTenure(((applicationProposalMapping.getTenure()).intValue()) + " Years");
		plTeaserViewResponse.setCurrencyDenomination(applicationProposalMapping.getCurrencyId() != null ? Currency.getById(applicationProposalMapping.getCurrencyId()).getValue().toString() : "-");
		plTeaserViewResponse.setAppId(toApplicationId);
		 

		 // CHANGES FOR DATE OF PROPOSAL(TEASER VIEW)	NEW CODE
			try {
				Object obj = "-";
				Date dateOfProposal = loanApplicationRepository.getInPrincipleDate(toApplicationId);
				if(!CommonUtils.isObjectNullOrEmpty(dateOfProposal)) {
			     plTeaserViewResponse.setDateOfProposal(dateOfProposal);
				}else{
				plTeaserViewResponse.setDateOfProposal(obj);
				}
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			// ENDS HERE===================>


		/* ========= Matches Data ========== */
		if (userType != null && !(CommonUtils.UserType.FUND_SEEKER == userType) ) {
			// TEASER VIEW FROM FP SIDE
				try {
					MatchRequest matchRequest = new MatchRequest();
					matchRequest.setApplicationId(toApplicationId);
					matchRequest.setProductId(productMappingId);
					matchRequest.setProposalId(proposalId);
					matchRequest.setBusinessTypeId(applicationProposalMapping.getBusinessTypeId());
					MatchDisplayResponse matchResponse = matchEngineClient.displayMatchesOfRetail(matchRequest);
					plTeaserViewResponse.setMatchesList(matchResponse.getMatchDisplayObjectList());
				} catch (Exception e) {
					logger.error("Error while getting matches data : " + e);
				}
		}
		
		// Product Name
		
		if(productMappingId != null) {
			String productName = productMasterRepository.getFpProductName(productMappingId);
			if(productName != null) {
				plTeaserViewResponse.setFpProductName(productName);
			}
		}
		
		// basic Details
		
		PLRetailApplicantResponse plRetailApplicantResponse = null;
		try {
			PLRetailApplicantRequest plRetailApplicantRequest = plRetailApplicantService.getProfileByProposalId(userid, toApplicationId, proposalId);
			plRetailApplicantResponse = new PLRetailApplicantResponse();
			if(plRetailApplicantRequest != null) {
				
				/*-----  all primary details fetch from db (fs_retail_applicat_detail) and using enum ------*/

				plRetailApplicantResponse.setFullName((plRetailApplicantRequest.getFirstName() != null ? plRetailApplicantRequest.getFirstName() : "") +" "+ (plRetailApplicantRequest.getMiddleName() != null ? plRetailApplicantRequest.getMiddleName() : "") +" "+ (plRetailApplicantRequest.getLastName() != null ?  plRetailApplicantRequest.getLastName() : ""));
				plRetailApplicantResponse.setGender(plRetailApplicantRequest.getGenderId() != null ? Gender.getById(plRetailApplicantRequest.getGenderId()).getValue().toString() : "-");
				plRetailApplicantResponse.setBirthDate(plRetailApplicantRequest.getBirthDate());
				plRetailApplicantResponse.setPan(plRetailApplicantRequest.getPan());
				plRetailApplicantResponse.setAadharNumber(plRetailApplicantRequest.getAadharNumber());
				plRetailApplicantResponse.setMobile(plRetailApplicantRequest.getMobile());
				plRetailApplicantResponse.setEmploymentType(plRetailApplicantRequest.getEmploymentType() != null ? OccupationNature.getById(plRetailApplicantRequest.getEmploymentType()).getValue().toString() : "-");
				if(ResidenceStatusRetailMst.OWNED.getId() == plRetailApplicantRequest.getResidenceType()) {
					plRetailApplicantResponse.setIsOwnedProp(plRetailApplicantRequest.getIsOwnedProp() != null ? plRetailApplicantRequest.getIsOwnedProp() == true ? "Yes" : "No" : "-");
				}else {
					plRetailApplicantResponse.setIsOwnedProp("-");
				}
				/*plRetailApplicantResponse.setNameOfEmployer(plRetailApplicantRequest.getNameOfEmployer());*/
				
				/*name of emp*/
				//switch as per EmploymentWithPL id
				try {
					switch (plRetailApplicantRequest.getEmploymentWith() != null ? plRetailApplicantRequest.getEmploymentWith() :0) {
					
					case 1://central gov
						plRetailApplicantResponse.setNameOfEmployer(oneFormClient.getMasterTableData(plRetailApplicantRequest.getCentralGovId().longValue(), GetStringFromIdForMasterData.CENTRAL_GOV.getValue()));
						break;
					case 2://state gov
						plRetailApplicantResponse.setNameOfEmployer(oneFormClient.getMasterTableData(plRetailApplicantRequest.getStateGovId().longValue(), GetStringFromIdForMasterData.STATE_GOV.getValue()));
						break;
					case 3://psu
						plRetailApplicantResponse.setNameOfEmployer(oneFormClient.getMasterTableData(plRetailApplicantRequest.getPsuId().longValue(), GetStringFromIdForMasterData.PSU.getValue()));
						break;
					case 4: //company
						plRetailApplicantResponse.setNameOfEmployer(plRetailApplicantRequest.getNameOfEmployer());
						break;
					case 5://educational insitute
						plRetailApplicantResponse.setNameOfEmployer(oneFormClient.getMasterTableData(plRetailApplicantRequest.getEduInstId().longValue(), GetStringFromIdForMasterData.INSITUTE.getValue()));
						break;
					case 8: //bank
						plRetailApplicantResponse.setNameOfEmployer(oneFormClient.getMasterTableData(plRetailApplicantRequest.getBankNameId().longValue(), GetStringFromIdForMasterData.BANK.getValue()));
						break;
					case 9: //Insurance company
						plRetailApplicantResponse.setNameOfEmployer(oneFormClient.getMasterTableData(plRetailApplicantRequest.getInsuranceNameId().longValue(), GetStringFromIdForMasterData.INSURANCE_COMP.getValue()));
						break;

					default:
						break;
					}
				} catch (Exception e) {
					logger.info("exception while calling oneform client"+e);
				}
				
				
				
				plRetailApplicantResponse.setEmploymentWith(plRetailApplicantRequest.getEmploymentWith() != null ? EmploymentWithPL.getById(plRetailApplicantRequest.getEmploymentWith()).getValue().toString() : "-");
				plRetailApplicantResponse.setEmploymentStatus(plRetailApplicantRequest.getEmploymentStatus() != null ? EmploymentStatusRetailMst.getById(plRetailApplicantRequest.getEmploymentStatus()).getValue().toString() : "-");
				plRetailApplicantResponse.setCurrentJobYear((plRetailApplicantRequest.getCurrentJobYear() !=null ? (plRetailApplicantRequest.getCurrentJobYear() +" year") : "") + " " +(plRetailApplicantRequest.getCurrentJobMonth() != null ? (plRetailApplicantRequest.getCurrentJobMonth() +" months") :  "" )); 
				plRetailApplicantResponse.setTotalExperienceYear((plRetailApplicantRequest.getTotalExperienceYear() !=null ? (plRetailApplicantRequest.getTotalExperienceYear() +" year") : "") + " " + (plRetailApplicantRequest.getTotalExperienceMonth() != null ? (plRetailApplicantRequest.getTotalExperienceMonth() +" months") :  "" ));
				plRetailApplicantResponse.setResidenceType(plRetailApplicantRequest.getResidenceType() != null ? ResidenceStatusRetailMst.getById(plRetailApplicantRequest.getResidenceType()).getValue().toString() : "-");
				plRetailApplicantResponse.setMaritalStatus(plRetailApplicantRequest.getStatusId() != null ? MaritalStatusMst.getById(plRetailApplicantRequest.getStatusId()).getValue().toString() : "-");
				plRetailApplicantResponse.setEducationQualificationString(plRetailApplicantRequest.getEducationQualification() != null ? EducationStatusRetailMst.getById(plRetailApplicantRequest.getEducationQualification()).getValue().toString() : "-");
				plRetailApplicantResponse.setSpouseEmployment(plRetailApplicantRequest.getSpouseEmployment() != null ? SpouseEmploymentList.getById(plRetailApplicantRequest.getSpouseEmployment()).getValue().toString() : "-");
				plRetailApplicantResponse.setDesignation(plRetailApplicantRequest.getDesignation()!= null ? DesignationList.getById(plRetailApplicantRequest.getDesignation()).getValue().toString() : "-");
				plRetailApplicantResponse.setNoOfDependent(plRetailApplicantRequest.getNoOfDependent());
				plRetailApplicantResponse.setTitle(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getTitleId()) ? StringEscapeUtils.escapeXml(Title.getById(plRetailApplicantRequest.getTitleId()).getValue()):null);
				plRetailApplicantResponse.setSalaryMode(plRetailApplicantRequest.getSalaryMode()!=null ? SalaryModeMst.getById(plRetailApplicantRequest.getSalaryMode()).getValue().toString() : "-");

				plRetailApplicantResponse.setRetailApplicantIncomeRequestList(plRetailApplicantRequest.getRetailApplicantIncomeRequestList());
				
				
				
				
				/* Addition */
				plRetailApplicantResponse.setCategory(plRetailApplicantRequest.getCategory() != null ? CastCategory.getById(plRetailApplicantRequest.getCategory()).getValue() : "-");
				plRetailApplicantResponse.setEmail(plRetailApplicantRequest.getEmail() != null ? (plRetailApplicantRequest.getEmail()) : "-");
				plRetailApplicantResponse.setFatherName(plRetailApplicantRequest.getFatherName() != null ? (plRetailApplicantRequest.getFatherName()) : " ");
				plRetailApplicantResponse.setResidenceSinceMonthYear((plRetailApplicantRequest.getResidenceSinceYear() !=null ? (plRetailApplicantRequest.getCurrentJobYear() +" year") : "") + " " +(plRetailApplicantRequest.getResidenceSinceMonth()!= null ? (plRetailApplicantRequest.getResidenceSinceMonth()+" months") :  "" ));
				plRetailApplicantResponse.setAnnualIncomeOfSpouse(plRetailApplicantRequest.getAnnualIncomeOfSpouse() != null ? plRetailApplicantRequest.getAnnualIncomeOfSpouse() : null);
				plRetailApplicantResponse.setContactNo((plRetailApplicantRequest.getContactNo() != null ? plRetailApplicantRequest.getContactNo() : " "));
				plRetailApplicantResponse.setResidenceSinceYear(plRetailApplicantRequest.getResidenceSinceYear());
				plRetailApplicantResponse.setNetworth(plRetailApplicantRequest.getNetworth());
				plRetailApplicantResponse.setGrossMonthlyIncome(plRetailApplicantRequest.getGrossMonthlyIncome() != null ? (plRetailApplicantRequest.getGrossMonthlyIncome()) : null);
				plRetailApplicantResponse.setResidenceType(plRetailApplicantRequest.getResidenceType() != null ? ResidenceStatusRetailMst.getById(plRetailApplicantRequest.getResidenceType()).getValue() : "-");
				plRetailApplicantResponse.setNetMonthlyIncome(plRetailApplicantRequest.getMonthlyIncome() != null ? (plRetailApplicantRequest.getMonthlyIncome()) : null);
				plRetailApplicantResponse.setNationality(plRetailApplicantRequest.getResidentialStatus()!=null ? ResidentStatusMst.getById(plRetailApplicantRequest.getResidentialStatus()).getValue().toString() : "-");
				/* Addition */
				
			
				/*salary account details*/
				plRetailApplicantResponse.setSalaryAccountBankName(plRetailApplicantRequest.getSalaryBankName());
				plRetailApplicantResponse.setIsOtherSalaryAccBank(plRetailApplicantRequest.getIsOtherSalaryBank()!=null ? plRetailApplicantRequest.getIsOtherSalaryBank() : false);

				LocalDate today = LocalDate.now();
				if(plRetailApplicantRequest.getSalaryBankYear() !=null && plRetailApplicantRequest.getSalaryBankMonth()!= null) {

//					LocalDate since = LocalDate.of(plRetailApplicantRequest.getSalaryBankYear(), plRetailApplicantRequest.getSalaryBankMonth(), 1);
//
//			        Period age = Period.between(since, today);
//			        int years = age.getYears();
//			        int months = age.getMonths();

					plRetailApplicantResponse.setSalaryAccountBankSince(plRetailApplicantRequest.getSalaryBankYear()+" year "+plRetailApplicantRequest.getSalaryBankMonth()+" months");
				}
				
				if(plRetailApplicantRequest.getResidenceSinceYear() !=null && plRetailApplicantRequest.getResidenceSinceMonth() != null) {
					LocalDate since = LocalDate.of(plRetailApplicantRequest.getResidenceSinceYear(), plRetailApplicantRequest.getResidenceSinceMonth(), 1);

			        Period age = Period.between(since, today);
			        int years = age.getYears();
			        int months = age.getMonths();
			        
			        plRetailApplicantResponse.setResidenceSinceMonthYear(years+" year "+months+" months");
				}

				
				//citetailApplicantResponse.setry,State,country
				plTeaserViewResponse.setCity(CommonDocumentUtils.getCity(plRetailApplicantRequest.getAddressCity(), oneFormClient));
				plTeaserViewResponse.setState(CommonDocumentUtils.getState(plRetailApplicantRequest.getAddressState(), oneFormClient));
				plTeaserViewResponse.setCountry(CommonDocumentUtils.getCountry(plRetailApplicantRequest.getAddressCountry().longValue(), oneFormClient));
				
				// address
				try {
					if(plRetailApplicantRequest.getContactAddress() != null) {
						PincodeDataResponse pindata=pincodeDateService.getById(plRetailApplicantRequest.getContactAddress().getDistrictMappingId());
						plTeaserViewResponse.setPresentAddDist(pindata.getDistrictName());
						plTeaserViewResponse.setPresentAddTaluko(pindata.getTaluka());
						pindata.getTaluka();
					}else {
						logger.warn(DISTRICT_ID_IS_NULL_MSG);
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				
				if(plRetailApplicantRequest.getContactAddress() != null){
					plTeaserViewResponse.setPresentAdd( (plRetailApplicantRequest.getContactAddress().getPremiseNumber()!=null ? (CommonUtils.commaReplace(plRetailApplicantRequest.getContactAddress().getPremiseNumber())) :"") + (plRetailApplicantRequest.getContactAddress().getStreetName() != null ? (CommonUtils.commaReplace(plRetailApplicantRequest.getContactAddress().getStreetName())) : "") + (plRetailApplicantRequest.getContactAddress().getLandMark() != null ? (CommonUtils.commaReplace(plRetailApplicantRequest.getContactAddress().getLandMark())) : "")+ (plTeaserViewResponse.getPresentAddDist() != null ?(CommonUtils.commaReplace(plTeaserViewResponse.getPresentAddDist())) :"")+ (plTeaserViewResponse.getPresentAddTaluko() != null ? (CommonUtils.commaReplace(plTeaserViewResponse.getPresentAddTaluko())) : "") + (plRetailApplicantRequest.getContactAddress().getPincode() != null ? (plRetailApplicantRequest.getContactAddress().getPincode()) : ""));
				}
				
				/*banking relationship details*/

				
				// loan Details 
				plRetailApplicantResponse.setLoanAmountRequired(plRetailApplicantRequest.getLoanAmountRequired());
				if(plRetailApplicantRequest.getLoanPurpose() != null) {
					plTeaserViewResponse.setPurposeOfLoan(plRetailApplicantRequest.getLoanPurpose().equals(LoanPurposePL.OTHERS.getId()) ?  "Other - "+plRetailApplicantRequest.getLoanPurposeOther()  : LoanPurposePL.getById(plRetailApplicantRequest.getLoanPurpose()).getValue().toString());
				}else {
					plTeaserViewResponse.setPurposeOfLoan("NA");
				}
				
				plRetailApplicantResponse.setTenureReq(plRetailApplicantRequest.getTenureRequired()!= null ? plRetailApplicantRequest.getTenureRequired() > 1 ? plRetailApplicantRequest.getTenureRequired() + " Years" : plRetailApplicantRequest.getTenureRequired() + " Year " : "" );
				plRetailApplicantResponse.setRepayment(plRetailApplicantRequest.getRepayment());
				plRetailApplicantResponse.setMonthlyIncome(plRetailApplicantRequest.getMonthlyIncome());
				
				//set existing financial data if not null 
				
				if(plRetailApplicantRequest.getFinancialArrangementsDetailRequestsList() != null) {
					plRetailApplicantResponse.setFinancialArrangementsDetailRequestsList(plRetailApplicantRequest.getFinancialArrangementsDetailRequestsList());	
				}else {
					logger.warn("FinancialArrangementsDetail is null...");
				}
				
				//set credit card data if not null
				if(plRetailApplicantRequest.getCreditCardsDetailRequestList() != null) {
					
					plRetailApplicantResponse.setCreditCardsDetailRequestList(plRetailApplicantRequest.getCreditCardsDetailRequestList());	
				}else {
					logger.warn("CreditCardDetails is null...");
				}
				
				if(plRetailApplicantRequest.getBankingRelationshipList() != null) {
					plRetailApplicantResponse.setBankRelationShipList(plRetailApplicantRequest.getBankingRelationshipList());
				}else {
					logger.warn("bankRelationship  is null...");
				}

				//KEY VERTICAL FUNDING
				List<Long> keyVerticalFundingId = new ArrayList<>();
				if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalFunding()))
					keyVerticalFundingId.add(plRetailApplicantRequest.getKeyVerticalFunding());
				if (!CommonUtils.isListNullOrEmpty(keyVerticalFundingId)) {
					try {
						OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
						List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
						if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
							MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						    plTeaserViewResponse.setKeyVericalFunding(masterResponse.getValue());
						} else {
							logger.warn("key vertical funding is null");
						}

					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
				
				//KEY VERTICAL SECTOR
				List<Long> keyVerticalSectorId = new ArrayList<>();
				if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalSector()))
					keyVerticalSectorId.add(plRetailApplicantRequest.getKeyVerticalSector());
				try {
					OneFormResponse formResponse = oneFormClient.getIndustrySecByMappingId(plRetailApplicantRequest.getKeyVerticalSector());
					SectorIndustryModel sectorIndustryModel = MultipleJSONObjectHelper.getObjectFromMap((Map) formResponse.getData(), SectorIndustryModel.class);
					OneFormResponse oneFormResponse = oneFormClient.getSectorById(Arrays.asList(sectorIndustryModel.getSectorId()));
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						plTeaserViewResponse.setKeyVericalSector(masterResponse.getValue());
					} else {
						logger.warn("key vertical sector is null");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				
				//KEY VERTICAL SUBSECTOR
				try {
					if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalSubSector())) {
						OneFormResponse oneFormResponse = oneFormClient.getSubSecNameByMappingId(plRetailApplicantRequest.getKeyVerticalSubSector());
						plTeaserViewResponse.setKeyVericalSubsector(oneFormResponse.getData().toString());
					}
				} catch (Exception e) {
					logger.warn("key vertical subSector is null ");
				}
				
				
				plTeaserViewResponse.setRetailApplicantDetail(plRetailApplicantResponse);
				
			}else {
				logger.warn("retailApplicantDetail is null");
			}
			
		} catch (Exception e) {
			logger.error("error while fetching retailApplicantDetails : ",e);
		}
		
		/*get epfoData*/
		try {
			EmployerRequest epfReq=new EmployerRequest();
			epfReq.setApplicationId(toApplicationId);
			EkycResponse epfRes=epfClient.getEpfData(epfReq);
			if(epfRes != null && epfRes.getData()!= null) {
				plTeaserViewResponse.setEpfData(epfRes.getData());
			}else {
				logger.info("epfo data is null for===>>"+toApplicationId);
			}
		} catch (Exception e) {
			logger.info("error"+e);
		}
		
		
		//PROPOSAL RESPONSE
		try {
			ObjectMapper mapper = new ObjectMapper();
			ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
			proposalMappingRequest.setApplicationId(toApplicationId);
			proposalMappingRequest.setFpProductId(productMappingId);
			ProposalMappingResponse proposalMappingResponse= proposalDetailsClient.getActiveProposalDetails(proposalMappingRequest);
			ProposalMappingRequestString proposalMappingRequestString = mapper.convertValue(proposalMappingResponse.getData(), ProposalMappingRequestString.class);
			if(proposalMappingRequestString != null) {
				plTeaserViewResponse.setScoringBasedOn(proposalMappingRequestString.getScoringModelBasedOn() != null && proposalMappingRequestString.getScoringModelBasedOn() == 2 ? "REPO" : "MCLR");
				plTeaserViewResponse.setMclrRoi(proposalMappingRequestString.getMclrRoi() != null ? proposalMappingRequestString.getMclrRoi().toString() : "-");
				plTeaserViewResponse.setSpreadRoi(proposalMappingRequestString.getSpreadRoi() != null ? proposalMappingRequestString.getSpreadRoi().toString() : "-");
				 if (!CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString.getMclrRoi()) && !CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString.getSpreadRoi())) {
					 plTeaserViewResponse.setEffectiveRoi(String.valueOf(Double.valueOf(proposalMappingRequestString.getMclrRoi()) + Double.valueOf(proposalMappingRequestString.getSpreadRoi())));		    	
					} else {
						plTeaserViewResponse.setEffectiveRoi(proposalMappingRequestString.getMclrRoi() == null && proposalMappingRequestString.getSpreadRoi() == null ? "-" : proposalMappingRequestString.getMclrRoi() != null ? proposalMappingRequestString.getMclrRoi().toString() : proposalMappingRequestString.getSpreadRoi().toString());				
					}
				 plTeaserViewResponse.setConcessionRoi(proposalMappingRequestString.getConsessionRoi() != null && proposalMappingRequestString.getConsessionRoi() != 0.0 && proposalMappingRequestString.getConsessionRoi() != 0 ? proposalMappingRequestString.getConsessionRoi().toString() : "-");
				 plTeaserViewResponse.setConcessionRoiBased(proposalMappingRequestString.getConcessionBasedOnType() != null ? "- " + proposalMappingRequestString.getConcessionBasedOnType() : "No Concession");
				    if (plTeaserViewResponse.getEffectiveRoi() != null) {
				    	plTeaserViewResponse.setFinalRoi(proposalMappingRequestString.getConsessionRoi() != null ? String.valueOf(Double.valueOf(plTeaserViewResponse.getEffectiveRoi()) - Double.valueOf(proposalMappingRequestString.getConsessionRoi())) : "-" );
					} else {
						plTeaserViewResponse.setFinalRoi("-");
					}			
			if(proposalMappingResponse.getData() != null) {	
				plTeaserViewResponse.setProposalData(proposalMappingResponse.getData());
			}else {
				logger.info("proposal data is null");
			}
		} 
		}catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
			
		//Note for restrict Borrower
		try {
			String note = commonRepository.getNoteForHLCam(toApplicationId);
			plTeaserViewResponse.setNoteOfBorrower(!CommonUtils.isObjectNullOrEmpty(note) ? note : null);
		}catch (Exception e) {
			logger.error("Error/Exception while getting note of borrower....Error==>{}", e);
		}
		
		//cibil score
		try {
			CibilRequest cibilReq=new CibilRequest();
			cibilReq.setPan(plRetailApplicantResponse.getPan());
			cibilReq.setApplicationId(toApplicationId);
			CibilScoreLogRequest cibilScoreByPanCard = cibilClient.getCibilScoreByPanCard(cibilReq);
			plTeaserViewResponse.setCibilScore(cibilScoreByPanCard);
		} catch (Exception e) {
			logger.error("Error While calling Cibil Score By PanCard : ",e);
		}
		
		// Income Details
		try {
			List<RetailApplicantIncomeRequest> retailApplicantIncomeDetail = retailApplicantIncomeService.getAllByProposalId(toApplicationId, proposalId);
			
			if(retailApplicantIncomeDetail != null) {
				plTeaserViewResponse.setRetailApplicantIncomeDetails(retailApplicantIncomeDetail);	
			}else {
				logger.warn("..........::::::::----->>retailApplicantIncomeDetail is null<<-----:::::::::.....");	
			}
		} catch (Exception e) {
			logger.error("..........::::::::----->> Error while calling PL Income Details <<-----:::::::::.....",e);
		}
		
		// bank statement data
		ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(toApplicationId);
		reportRequest.setUserId(userId);
		List<Data> datas = new ArrayList<>();
		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			List<HashMap<String, Object>> hashMaps = (List<HashMap<String, Object>>) analyzerResponse.getData();
			if (!CommonUtils.isListNullOrEmpty(hashMaps)) {
				for (HashMap<String, Object> hashMap : hashMaps) {
					Data data = MultipleJSONObjectHelper.getObjectFromMap(hashMap, Data.class);
					datas.add(data);
				}
			}
			plTeaserViewResponse.setBankData(datas);
		
		} catch (Exception e) {
			logger.error("Error while getting perfios data : ",e);
		}

		// SCORING DATA
		ScoringRequest scoringRequest = new ScoringRequest();
		scoringRequest.setApplicationId(toApplicationId);
		scoringRequest.setFpProductId(productMappingId);

		try {
			ScoringResponse scoringResponse = scoringClient.getScore(scoringRequest);
			ProposalScoreResponse proposalScoreResponse = MultipleJSONObjectHelper.getObjectFromMap(
					(LinkedHashMap<String, Object>) scoringResponse.getDataObject(), ProposalScoreResponse.class);

			if (proposalScoreResponse != null){
				plTeaserViewResponse.setScoringModelName(proposalScoreResponse.getScoringModelName());
				plTeaserViewResponse.setDataList(scoringResponse.getDataList());
				plTeaserViewResponse.setDataObject(scoringResponse.getDataObject());
				plTeaserViewResponse.setScoringResponseList(scoringResponse.getScoringResponseList());
			}
			
		} catch (ScoringException | IOException e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}

		// Eligibility Data
		EligibililityRequest eligibilityReq = new EligibililityRequest();
		eligibilityReq.setApplicationId(toApplicationId);
		eligibilityReq.setFpProductMappingId(productMappingId);
		logger.info(" for eligibility appid============>>{}" , toApplicationId);

		try {
			EligibilityResponse eligibilityResp = eligibilityClient.getRetailLoanData(eligibilityReq);
			plTeaserViewResponse.setEligibilityDataObject(eligibilityResp.getData());
		} catch (Exception e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}
		
		ITRBasicDetailsResponse itrBasicDetailsResponse = null;
		String nameAsPerItr = null;
		try {
			ITRConnectionResponse resNameAsPerITR = itrClient.getIsUploadAndYearDetails(toApplicationId);
			if (resNameAsPerITR != null) {
				itrBasicDetailsResponse = (ITRBasicDetailsResponse)MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)resNameAsPerITR.getData(), ITRBasicDetailsResponse.class);
				nameAsPerItr = itrBasicDetailsResponse.getName();
				plTeaserViewResponse.setNameAsPerItr(resNameAsPerITR.getData() != null ? resNameAsPerITR.getData() : "NA");
			} else {
				logger.warn("-----------:::::::::::::: ItrResponse is null ::::::::::::---------");
			}
		} catch (Exception e) {
			logger.error(":::::::::::---------Error while fetching name as per itr----------:::::::::::",e);
		}

         // for name is edited or not:

        /* if(plRetailApplicantResponse.getFullName().equalsIgnoreCase((String) plTeaserViewResponse.getNameAsPerItr()))
         {
			 plTeaserViewResponse.setIsNameEdited(Boolean.FALSE);
		 }

		 else{
			 plTeaserViewResponse.setIsNameEdited(Boolean.TRUE);
		 }*/
		String fullName = plRetailApplicantResponse.getFullName();
		if(!CommonUtils.isObjectNullOrEmpty(fullName) && fullName.equalsIgnoreCase(nameAsPerItr)) {
			plTeaserViewResponse.setNameEditedByUser("-");
		}else {
			plTeaserViewResponse.setNameEditedByUser(fullName);	
		}
		
		//Note for restrict Borrower
				try {
					String note = commonRepo.getNoteForHLCam(toApplicationId);
					plTeaserViewResponse.setNoteOfBorrower(!CommonUtils.isObjectNullOrEmpty(note) ? note : null);
				}catch (Exception e) {
					logger.error("Error/Exception while getting note of borrower....Error==>{}", e);
				}


		// GET DOCUMENTS
		DocumentRequest documentRequest = new DocumentRequest();
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.WORKING_CAPITAL_PROFIEL_PICTURE);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			plTeaserViewResponse.setProfilePic(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.WORKING_CAPITAL_BANK_STATEMENT);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			plTeaserViewResponse.setBankStatement(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.RETAIL_ITR_PDF);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			plTeaserViewResponse.setIrtPdfReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.RETAIL_ITR_XML);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			plTeaserViewResponse.setIrtXMLReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.CIBIL_SOFTPING_CONSUMER);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			plTeaserViewResponse.setCibilConsumerReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// pl final view details filled from here
		if (isFinal) {	
			try {
				RetailFinalInfoRequest retailFinalInfo = plRetailApplicantService.getFinalByProposalId(userId, toApplicationId, proposalId);
				if(retailFinalInfo != null) {
					plTeaserViewResponse.setReligion(retailFinalInfo.getReligion() != null ? ReligionRetailMst.getById(retailFinalInfo.getReligion()).getValue().toString() : "-");
					plTeaserViewResponse.setResidentialStatus(retailFinalInfo.getResidentialStatus() != null ? ResidentialStatus.getById(retailFinalInfo.getResidentialStatus()).getValue().toString() : "-");
					plTeaserViewResponse.setCastCategory(retailFinalInfo.getCastId() != null ? CastCategory.getById(retailFinalInfo.getCastId()).getValue().toString() : "-");
					plTeaserViewResponse.setDiasablityType(retailFinalInfo.getDisabilityType() != null ? DisabilityType.getById(retailFinalInfo.getDisabilityType()).getValue().toString() : "-");
					plTeaserViewResponse.setDdoOrganizationType(retailFinalInfo.getDdoOrganizationType() != null ? EmploymentWithPL.getById(retailFinalInfo.getDdoOrganizationType()).getValue().toString() : "-");
					
					if(retailFinalInfo.getPreviousJobYear() != null) {
						plTeaserViewResponse.setPreviousJobYear(retailFinalInfo.getPreviousJobYear() +" Years " + retailFinalInfo.getPreviousJobMonth() + " Months");
					}else {
						plTeaserViewResponse.setPreviousJobYear("-");
					}
					
					
					if(retailFinalInfo.getDdoRemainingSerYrs() != null && retailFinalInfo.getDdoRemainingSerMonths() != null) {
						LocalDate today = LocalDate.now();
						LocalDate remainingYears = LocalDate.of(retailFinalInfo.getDdoRemainingSerYrs(), retailFinalInfo.getDdoRemainingSerMonths(), 1);
						Period p = Period.between(today, remainingYears);
						retailFinalInfo.setDdoRemainingSerYrs(p.getYears());
						retailFinalInfo.setDdoRemainingSerMonths(p.getMonths());
					}
					
					//permanent address
					try {
						if(retailFinalInfo != null && retailFinalInfo.getPermanentAddress().getDistrictMappingId() != null) {	
							PincodeDataResponse pindata=pincodeDateService.getById(retailFinalInfo.getPermanentAddress().getDistrictMappingId());
							plTeaserViewResponse.setPermAddDist(pindata.getDistrictName());
							plTeaserViewResponse.setPermAddTaluko(pindata.getTaluka());
							pindata.getTaluka();
						}else {
							logger.warn(DISTRICT_ID_IS_NULL_MSG);
						}

					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					
					if(retailFinalInfo.getPermanentAddress() != null){	
						plTeaserViewResponse.setPermAdd( (retailFinalInfo.getPermanentAddress().getPremiseNumber()!=null ? (CommonUtils.commaReplace(retailFinalInfo.getPermanentAddress().getPremiseNumber())) :"") + (retailFinalInfo.getPermanentAddress().getStreetName() != null ? (CommonUtils.commaReplace(retailFinalInfo.getPermanentAddress().getStreetName())) : "") + (retailFinalInfo.getPermanentAddress().getLandMark() != null ? (CommonUtils.commaReplace(retailFinalInfo.getPermanentAddress().getLandMark())) : "")+ (plTeaserViewResponse.getPermAddDist() != null ?(CommonUtils.commaReplace(plTeaserViewResponse.getPermAddDist())) :"")+ (plTeaserViewResponse.getPermAddTaluko() != null ? (CommonUtils.commaReplace(plTeaserViewResponse.getPermAddTaluko())) : "") + (retailFinalInfo.getPermanentAddress().getPincode() != null ? (retailFinalInfo.getPermanentAddress().getPincode()) : ""));
					}
					
					
					//Office address
					try {
						if(retailFinalInfo != null && retailFinalInfo.getOfficeAddress().getDistrictMappingId() !=null) {	
							PincodeDataResponse pindata=pincodeDateService.getById(retailFinalInfo.getOfficeAddress().getDistrictMappingId());
							plTeaserViewResponse.setOffAddDist(pindata.getDistrictName());
							plTeaserViewResponse.setOffAddTaluko(pindata.getTaluka());
							pindata.getTaluka();
						}else {
							logger.warn(DISTRICT_ID_IS_NULL_MSG);
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					
					if(retailFinalInfo.getOfficeAddress() != null){
						plTeaserViewResponse.setOffAdd( (retailFinalInfo.getOfficeAddress().getPremiseNumber()!=null ? (CommonUtils.commaReplace(retailFinalInfo.getOfficeAddress().getPremiseNumber())) :"") + (retailFinalInfo.getOfficeAddress().getStreetName() != null ? (CommonUtils.commaReplace(retailFinalInfo.getOfficeAddress().getStreetName())) : "") + (retailFinalInfo.getOfficeAddress().getLandMark() != null ? (CommonUtils.commaReplace(retailFinalInfo.getOfficeAddress().getLandMark())) : "")+ (plTeaserViewResponse.getOffAddDist() != null ?(CommonUtils.commaReplace(plTeaserViewResponse.getOffAddDist())) :"")+ (plTeaserViewResponse.getOffAddTaluko() != null ? (CommonUtils.commaReplace(plTeaserViewResponse.getOffAddTaluko())) : "") + (retailFinalInfo.getOfficeAddress().getPincode() != null ? (retailFinalInfo.getOfficeAddress().getPincode()) : ""));
					}
				
					plTeaserViewResponse.setFinalDetails(retailFinalInfo);
				}else {
					logger.warn("Retail Final Info is Null....");
				}	
			} catch (Exception e) {
				logger.error("Error while fetching RetailFinalData : ",e);
			}

			//BANK ACCOUNT HELD DETAILS
			try {
				List<BankAccountHeldDetailsRequest> bankAccountHeldDetails = bankAccountHeldDetailsService.getExistingLoanDetailListByProposalId(proposalId,1);
				if(bankAccountHeldDetails != null) {
					plTeaserViewResponse.setBankAccountDetails(bankAccountHeldDetails);
				}else {
					logger.warn("Bank Held Details is Null....");
				}	
			} catch (Exception e) {
				logger.error("Error while getting bank account held details : ",e);
			}
			
			//FIXED DEPOSITS DETAILS
			try {
				List<FixedDepositsDetailsRequest> fixedDepositeDetails = fixedDepositsDetailService.getFixedDepositsDetailByProposalId(proposalId, 1);
				if(fixedDepositeDetails != null) {
					plTeaserViewResponse.setFixDepositDetails(fixedDepositeDetails);
				}else {
					logger.warn("Fix Deposit Details is Null....");
				}
			} catch (Exception e) {
				logger.error("Error while getting fixed deposite details : ",e);
			}
			
			//OTHER CURRENT ASSEST DETAILS
			try {
				List<OtherCurrentAssetDetailRequest> otherCurrentAssetDetails = otherCurrentAssetDetailsService.getOtherCurrentAssetDetailListByProposalId(proposalId,1);	
				if(otherCurrentAssetDetails != null) {
					plTeaserViewResponse.setOtherCurruntAssetDetail(otherCurrentAssetDetails);	
				}else {
					logger.warn("Other Currnt Asset Details is Null....");
				}
			} catch (Exception e) {
				logger.error("Error while getting other current asset details : ",e);
			}
			
			//OBLIGATION DETAILS
			try {
				List<ObligationDetailRequest> obligationRequest = obligationDetailService.getObligationDetailsFromProposalId(proposalId, 1);
				if(obligationRequest != null) {	
					plTeaserViewResponse.setObligationDetails(obligationRequest);	
				}else {
					logger.warn("Obligation Details is Null....");
				}	
			} catch (Exception e) {
				logger.error("Error while getting obligation details : ",e);
			}
			
			//REFERENCES DETAILS
			try {
				List<ReferenceRetailDetailsRequest> referenceDetails = referenceRetailDetailService.getReferenceRetailDetailListByPropsalId(proposalId,1);
				if(referenceDetails !=null) {	
					plTeaserViewResponse.setReferenceDetails(referenceDetails);
				}else {
					logger.warn("Reference Details is Null...");
				}
			} catch (Exception e) {
				logger.error("Error while getting reference details : ",e);
			}
			
			DocumentRequest finalDocumentRequest = new DocumentRequest();
			documentRequest.setApplicationId(toApplicationId);
			documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
			documentRequest.setProductDocumentMappingId(DocumentAlias.WORKING_CAPITAL_PROFIEL_PICTURE);
			try {
				DocumentResponse documentResponse = dmsClient.listProductDocument(finalDocumentRequest);
				plTeaserViewResponse.setProfilePic(documentResponse.getDataList());
			} catch (DocumentException e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}
		return plTeaserViewResponse;
	}

}
