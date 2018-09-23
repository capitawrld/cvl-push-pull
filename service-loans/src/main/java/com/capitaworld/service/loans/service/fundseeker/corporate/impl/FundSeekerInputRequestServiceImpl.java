package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.capitaworld.connect.api.ConnectAuditErrorCode;
import com.capitaworld.connect.api.ConnectLogAuditRequest;
import com.capitaworld.connect.api.ConnectStage;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.connect.api.ConnectResponse;
import com.capitaworld.connect.client.ConnectClient;
import com.capitaworld.service.analyzer.client.AnalyzerClient;
import com.capitaworld.service.analyzer.model.common.ReportRequest;
import com.capitaworld.service.fraudanalytics.client.FraudAnalyticsClient;
import com.capitaworld.service.fraudanalytics.model.AnalyticsRequest;
import com.capitaworld.service.fraudanalytics.model.AnalyticsResponse;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.DirectorBackgroundDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.DirectorPersonalDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FinancialArrangementsDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.capitaworld.service.loans.model.Address;
import com.capitaworld.service.loans.model.DirectorBackgroundDetailRequest;
import com.capitaworld.service.loans.model.DirectorPersonalDetailRequest;
import com.capitaworld.service.loans.model.FinancialArrangementsDetailRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.NTBRequest;
import com.capitaworld.service.loans.model.common.HunterRequestDataResponse;
import com.capitaworld.service.loans.model.corporate.FundSeekerInputRequestResponse;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.DirectorBackgroundDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.DirectorPersonalDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FinancialArrangementDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SubSectorRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.service.fundseeker.corporate.FundSeekerInputRequestService;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.utils.CommonUtils;

@Service
@Transactional
public class FundSeekerInputRequestServiceImpl implements FundSeekerInputRequestService {

	private static final Logger logger = LoggerFactory.getLogger(FundSeekerInputRequestServiceImpl.class);

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;
  
	@Autowired
	private PrimaryCorporateDetailRepository primaryCorporateDetailRepository;

	@Autowired
	private FinancialArrangementDetailsRepository financialArrangementDetailsRepository;

	@Autowired
	private DirectorBackgroundDetailsRepository directorBackgroundDetailsRepository;

	@Autowired
	private ConnectClient connectClient;

	@Autowired
	private AnalyzerClient analyzerClient;

	@Autowired
	private CorporateApplicantService corporateApplicantService;

	@Autowired
	private LoanApplicationService loanApplicationService;

	@Autowired
	private FraudAnalyticsClient fraudAnalyticsClient;
	
	@Autowired
	private IndustrySectorRepository industrySectorRepository;
	    
	@Autowired
	private SubSectorRepository subSectorRepository; 
	
	@Autowired
	private Environment environment;

	@Autowired
	private DirectorPersonalDetailRepository directorPersonalDetailRepository;

	@Override
	public boolean saveOrUpdate(FundSeekerInputRequestResponse fundSeekerInputRequest) throws Exception {
		try {
			logger.info("getting corporateApplicantDetail from applicationId::"
					+ fundSeekerInputRequest.getApplicationId());
			CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
					.findOneByApplicationIdId(fundSeekerInputRequest.getApplicationId());
			if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail)) {
				logger.info("corporateApplicantDetail is null created new object");
				corporateApplicantDetail = new CorporateApplicantDetail();
				BeanUtils.copyProperties(fundSeekerInputRequest, corporateApplicantDetail, "secondAddress", "sameAs","organisationName","constitutionId",
						"creditRatingId", "contLiabilityFyAmt", "contLiabilitySyAmt", "contLiabilityTyAmt",
						" contLiabilityYear", "notApplicable", "aboutUs", "id", "isActive");
				corporateApplicantDetail
						.setApplicationId(new LoanApplicationMaster(fundSeekerInputRequest.getApplicationId()));
				corporateApplicantDetail.setCreatedBy(fundSeekerInputRequest.getUserId());
				corporateApplicantDetail.setCreatedDate(new Date());
				corporateApplicantDetail.setIsActive(true);
			} else {
				BeanUtils.copyProperties(fundSeekerInputRequest, corporateApplicantDetail, "secondAddress", "sameAs","organisationName","constitutionId",
						"creditRatingId", "contLiabilityFyAmt", "contLiabilitySyAmt", "contLiabilityTyAmt",
						" contLiabilityYear", "notApplicable", "aboutUs", "id");
				corporateApplicantDetail.setModifiedBy(fundSeekerInputRequest.getUserId());
				corporateApplicantDetail.setModifiedDate(new Date());
			}

			corporateApplicantDetailRepository.save(corporateApplicantDetail);

			// ----INDUSTRY SECTOR SUBSECTOR SAVE START
			// industry data save
			corporateApplicantService.saveIndustry(corporateApplicantDetail.getApplicationId().getId(),
					fundSeekerInputRequest.getIndustrylist());
			// Sector data save
			corporateApplicantService.saveSector(corporateApplicantDetail.getApplicationId().getId(),
					fundSeekerInputRequest.getSectorlist());
			// sub sector save
			corporateApplicantService.saveSubSector(corporateApplicantDetail.getApplicationId().getId(),
					fundSeekerInputRequest.getSubsectors());
			// ----INDUSTRY SECTOR SUBSECTOR SAVE END

			logger.info(
					"getting primaryCorporateDetail from applicationId::" + fundSeekerInputRequest.getApplicationId());
			PrimaryCorporateDetail primaryCorporateDetail = primaryCorporateDetailRepository
					.findOneByApplicationIdId(fundSeekerInputRequest.getApplicationId());
			if (CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail)) {
				logger.info("primaryCorporateDetail is null created new object");
				primaryCorporateDetail = new PrimaryCorporateDetail();
			}
			BeanUtils.copyProperties(fundSeekerInputRequest, primaryCorporateDetail);
			primaryCorporateDetail.setAmount(fundSeekerInputRequest.getLoanAmount());

			primaryCorporateDetail.setIsApplicantDetailsFilled(true);
			primaryCorporateDetail.setIsApplicantPrimaryFilled(true);
			primaryCorporateDetail.setApplicationId(new LoanApplicationMaster(fundSeekerInputRequest.getApplicationId()));
			logger.info("Save in LoanAppMaster with BusinessType ==>"+fundSeekerInputRequest.getBusinessTypeId());
			primaryCorporateDetail.setBusinessTypeId(fundSeekerInputRequest.getBusinessTypeId());
			primaryCorporateDetail.setModifiedBy(fundSeekerInputRequest.getUserId());
			primaryCorporateDetail.setModifiedDate(new Date());
			primaryCorporateDetail.setIsActive(true);
			primaryCorporateDetailRepository.saveAndFlush(primaryCorporateDetail);

			List<FinancialArrangementsDetailRequest> financialArrangementsDetailRequestsList = fundSeekerInputRequest
					.getFinancialArrangementsDetailRequestsList();
			if(!CommonUtils.isListNullOrEmpty(financialArrangementsDetailRequestsList)) {
				logger.info("Financial Arrangements Detail List Null Or Empty ------------->");
				for (FinancialArrangementsDetailRequest reqObj : financialArrangementsDetailRequestsList) {
					FinancialArrangementsDetail saveFinObj = null;
					if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
						saveFinObj = financialArrangementDetailsRepository.findByIdAndIsActive(reqObj.getId(), true);
					}
					if (CommonUtils.isObjectNullOrEmpty(saveFinObj)) {
						saveFinObj = new FinancialArrangementsDetail();
						BeanUtils.copyProperties(reqObj, saveFinObj, "id", "createdBy", "createdDate", "modifiedBy",
								"modifiedDate", "isActive");

						saveFinObj.setApplicationId(new LoanApplicationMaster(fundSeekerInputRequest.getApplicationId()));
						saveFinObj.setCreatedBy(fundSeekerInputRequest.getUserId());
						saveFinObj.setCreatedDate(new Date());
						saveFinObj.setIsActive(true);
					} else {
						BeanUtils.copyProperties(reqObj, saveFinObj, "id", "createdBy", "createdDate", "modifiedBy",
								"modifiedDate");
						saveFinObj.setModifiedBy(fundSeekerInputRequest.getUserId());
						saveFinObj.setModifiedDate(new Date());
					}
					financialArrangementDetailsRepository.save(saveFinObj);
				}
			}

			return true;

		} catch (Exception e) {
			logger.info("Throw Exception while save and update Fundseeker input request !!");
			e.printStackTrace();
			throw new Exception();
		}
	}

	@Override
	public ResponseEntity<LoansResponse> saveOrUpdateDirectorDetail(
			FundSeekerInputRequestResponse fundSeekerInputRequest) {
		String msg = null;
		try {
			// ==== Applicant Address

			logger.info("Enter in save directors details ---------------------------------------->"
					+ fundSeekerInputRequest.getApplicationId());
			CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
					.findOneByApplicationIdId(fundSeekerInputRequest.getApplicationId());
			if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail)) {
				logger.info("corporateApplicantDetail is null created new object");
				corporateApplicantDetail = new CorporateApplicantDetail();
				BeanUtils.copyProperties(fundSeekerInputRequest, corporateApplicantDetail, "aadhar", "secondAddress",
						"sameAs", "creditRatingId", "contLiabilityFyAmt", "contLiabilitySyAmt", "contLiabilityTyAmt",
						" contLiabilityYear", "notApplicable", "aboutUs", "id", "isActive");
				corporateApplicantDetail
						.setApplicationId(new LoanApplicationMaster(fundSeekerInputRequest.getApplicationId()));
				corporateApplicantDetail.setCreatedBy(fundSeekerInputRequest.getUserId());
				corporateApplicantDetail.setCreatedDate(new Date());
				corporateApplicantDetail.setIsActive(true);
			} else {
				logger.info("constitution id  ------------------------------------------>"
						+ corporateApplicantDetail.getConstitutionId());
				CorporateApplicantDetail copyObj = corporateApplicantDetail;
				BeanUtils.copyProperties(fundSeekerInputRequest, corporateApplicantDetail, "aadhar", "secondAddress",
						"sameAs", "creditRatingId", "contLiabilityFyAmt", "contLiabilitySyAmt", "contLiabilityTyAmt",
						" contLiabilityYear", "notApplicable", "aboutUs", "id", "constitutionId");
				logger.info(
						"Before save constitution id ---------------> " + fundSeekerInputRequest.getKeyVericalFunding()
								+ "---------------in DB------------->" + copyObj.getConstitutionId());
				corporateApplicantDetail.setKeyVericalFunding(
						!CommonUtils.isObjectNullOrEmpty(fundSeekerInputRequest.getKeyVericalFunding())
								? fundSeekerInputRequest.getKeyVericalFunding()
								: copyObj.getKeyVericalFunding());
				corporateApplicantDetail.setKeyVerticalSector(
						!CommonUtils.isObjectNullOrEmpty(fundSeekerInputRequest.getKeyVerticalSector())
								? fundSeekerInputRequest.getKeyVerticalSector()
								: copyObj.getKeyVerticalSector());
				corporateApplicantDetail.setKeyVerticalSubsector(
						!CommonUtils.isObjectNullOrEmpty(fundSeekerInputRequest.getKeyVerticalSubsector())
								? fundSeekerInputRequest.getKeyVerticalSubsector()
								: copyObj.getKeyVerticalSubsector());
				corporateApplicantDetail.setOrganisationName(
						!CommonUtils.isObjectNullOrEmpty(fundSeekerInputRequest.getOrganisationName())
								? fundSeekerInputRequest.getOrganisationName()
								: copyObj.getOrganisationName());
				corporateApplicantDetail.setAadhar(!CommonUtils.isObjectNullOrEmpty(fundSeekerInputRequest.getAadhar())
						? fundSeekerInputRequest.getAadhar()
						: copyObj.getAadhar());
				corporateApplicantDetail.setMsmeRegistrationNumber(
						!CommonUtils.isObjectNullOrEmpty(fundSeekerInputRequest.getMsmeRegistrationNumber())
								? fundSeekerInputRequest.getMsmeRegistrationNumber()
								: copyObj.getMsmeRegistrationNumber());
				corporateApplicantDetail
						.setConstitutionId(!CommonUtils.isObjectNullOrEmpty(fundSeekerInputRequest.getConstitutionId())
								? fundSeekerInputRequest.getConstitutionId()
								: copyObj.getConstitutionId());

				corporateApplicantDetail.setModifiedBy(fundSeekerInputRequest.getUserId());
				corporateApplicantDetail.setModifiedDate(new Date());
			}
			copyAddressFromRequestToDomain(fundSeekerInputRequest, corporateApplicantDetail);

			logger.info("Just Before Save ------------------------------------->" + corporateApplicantDetail.getConstitutionId());
			corporateApplicantDetailRepository.save(corporateApplicantDetail);
			// ==== Director details
			List<DirectorBackgroundDetailRequest> directorBackgroundDetailRequestList = fundSeekerInputRequest.getDirectorBackgroundDetailRequestsList();

			try {
				for (DirectorBackgroundDetailRequest reqObj : directorBackgroundDetailRequestList) {
					DirectorBackgroundDetail saveDirObj = null;
					if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
						saveDirObj = directorBackgroundDetailsRepository.findByIdAndIsActive(reqObj.getId(), true);
						logger.info("Old Object Retrived For Director saveDirObj.getId()==========================>{}",
								saveDirObj.getId());
						BeanUtils.copyProperties(reqObj, saveDirObj, "id", "createdBy", "createdDate", "modifiedBy",
								"modifiedDate");
						saveDirObj.setModifiedBy(fundSeekerInputRequest.getUserId());
						saveDirObj.setModifiedDate(new Date());
					} else {
						logger.info("New Object Created for Director");
						saveDirObj = new DirectorBackgroundDetail();
						BeanUtils.copyProperties(reqObj, saveDirObj, "id", "createdBy", "createdDate", "modifiedBy",
								"modifiedDate", "isActive");
						saveDirObj
								.setApplicationId(new LoanApplicationMaster(fundSeekerInputRequest.getApplicationId()));
						saveDirObj.setCreatedBy(fundSeekerInputRequest.getUserId());
						saveDirObj.setCreatedDate(new Date());
						saveDirObj.setIsActive(true);
					}
					if(!CommonUtils.isObjectNullOrEmpty(reqObj.getIsMainDirector()) && (reqObj.getIsMainDirector())){
						DirectorPersonalDetailRequest directorPersonalDetailRequest = reqObj.getDirectorPersonalDetailRequest();
						DirectorPersonalDetail directorPersonalDetail = null;
						if(directorPersonalDetailRequest.getId() != null){
							directorPersonalDetail = directorPersonalDetailRepository.findOne(directorPersonalDetailRequest.getId());
						}else{
							directorPersonalDetail = new DirectorPersonalDetail();
							directorPersonalDetail.setCreatedBy(fundSeekerInputRequest.getUserId());
							directorPersonalDetail.setCreatedDate(new Date());
						}
						BeanUtils.copyProperties(directorPersonalDetailRequest,directorPersonalDetail);
						directorPersonalDetail.setModifiedBy(fundSeekerInputRequest.getUserId());
						directorPersonalDetail.setModifiedDate(new Date());
						DirectorPersonalDetail directorPersonalDetailTemp=directorPersonalDetailRepository.save(directorPersonalDetail);
						logger.info("employment detail saved successfully");
						saveDirObj.setDirectorPersonalDetail(directorPersonalDetailTemp);
					}else{
						saveDirObj.setDirectorPersonalDetail(null);
					}
					directorBackgroundDetailsRepository.save(saveDirObj);
				}
			} catch (Exception e) {
				logger.info(
						"Directors ===============> Throw Exception While Save Director Background Details -------->");
				e.printStackTrace();
			}

			LoansResponse res = new LoansResponse("director detail successfully saved", HttpStatus.OK.value());
			res.setFlag(true);
			logger.info("director detail successfully saved");
			msg = "director detail successfully saved";
			return new ResponseEntity<LoansResponse>(res, HttpStatus.OK);

		} catch (Exception e) {
			LoansResponse res = new LoansResponse("error while saving director detail",
					HttpStatus.INTERNAL_SERVER_ERROR.value());
			msg="";
			logger.error("error while saving director detail");
			e.printStackTrace();

			return new ResponseEntity<LoansResponse>(res, HttpStatus.OK);
		}finally {
			try {
				connectClient.saveAuditLog(new ConnectLogAuditRequest(fundSeekerInputRequest.getApplicationId(), ConnectStage.DIRECTOR_BACKGROUND.getId(),fundSeekerInputRequest.getUserId(),msg, ConnectAuditErrorCode.DIRECTOR_SUBMIT.toString(),CommonUtils.BusinessType.EXISTING_BUSINESS.getId()));
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public ResponseEntity<LoansResponse> get(FundSeekerInputRequestResponse fsInputReq) {

		FundSeekerInputRequestResponse fsInputRes = new FundSeekerInputRequestResponse();
		fsInputRes.setApplicationId(fsInputReq.getApplicationId());

		try {
			CorporateApplicantDetail corpApplicantDetail = corporateApplicantDetailRepository
					.findOneByApplicationIdId(fsInputReq.getApplicationId());
			if (CommonUtils.isObjectNullOrEmpty(corpApplicantDetail)) {
				logger.info("Data not found for given applicationid");
				fsInputRes.setFinancialArrangementsDetailRequestsList(Collections.emptyList());
				return new ResponseEntity<LoansResponse>(new LoansResponse("Data not found for given applicationid",
						HttpStatus.BAD_REQUEST.value(), fsInputRes), HttpStatus.OK);
			}

			BeanUtils.copyProperties(corpApplicantDetail, fsInputRes);

			PrimaryCorporateDetail primaryCorporateDetail = primaryCorporateDetailRepository
					.findOneByApplicationIdId(fsInputReq.getApplicationId());
			if (!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail)) {
				BeanUtils.copyProperties(primaryCorporateDetail, fsInputRes);
			}

			List<FinancialArrangementsDetail> finArngDetailList = financialArrangementDetailsRepository
					.listSecurityCorporateDetailByAppId(fsInputReq.getApplicationId());
			
//			if(CommonUtils.isListNullOrEmpty(finArngDetailList)) {
//				if(!CommonUtils.isObjectNullOrEmpty(corpApplicantDetail.getPanNo())) {
//					if(corpApplicantDetail.getPanNo().charAt(3) == 'P' || corpApplicantDetail.getPanNo().charAt(3) == 'p') {
//						DirectorBackgroundDetail backgroundDetail = directorBackgroundDetailsRepository.findByApplicationIdIdAndPanNoAndIsActive(fsInputReq.getApplicationId(), corpApplicantDetail.getPanNo().toUpperCase(), true);
//						if(!CommonUtils.isObjectNullOrEmpty(backgroundDetail) && !CommonUtils.isObjectNullOrEmpty(backgroundDetail.getId())) {
//							finArngDetailList = financialArrangementDetailsRepository.findByDirectorBackgroundDetailIdAndApplicationIdIdAndIsActive(backgroundDetail.getId(), fsInputReq.getApplicationId(), true);
//						}else {
//							logger.info("Director Not Found for Application Id====>{} and Pan No==========>{}",fsInputReq.getApplicationId(), corpApplicantDetail.getPanNo());
//						}
//					}else {
//						logger.info("No Current Financial Loans for Pan No======>{}",corpApplicantDetail.getPanNo());	
//					}	
//				}else {
//					logger.info("Pan No is Blank from Corporate Profile");				
//				}
//			}

			List<FinancialArrangementsDetailRequest> finArrngDetailResList = new ArrayList<FinancialArrangementsDetailRequest>(
					finArngDetailList.size());

			FinancialArrangementsDetailRequest finArrngDetailReq = null;
			for (FinancialArrangementsDetail finArrngDetail : finArngDetailList) {
				finArrngDetailReq = new FinancialArrangementsDetailRequest();
				BeanUtils.copyProperties(finArrngDetail, finArrngDetailReq);
				if(!CommonUtils.isObjectNullOrEmpty(finArrngDetail.getDirectorBackgroundDetail())) {
					finArrngDetailReq.setDirectorId(finArrngDetail.getDirectorBackgroundDetail().getId());					
				}
				finArrngDetailResList.add(finArrngDetailReq);
			}
			fsInputRes.setFinancialArrangementsDetailRequestsList(finArrngDetailResList);
			
			List<Long> industryList = industrySectorRepository.getIndustryByApplicationId(fsInputReq.getApplicationId());
			logger.info("TOTAL INDUSTRY FOUND ------------->" + industryList.size() + "------------By APP Id -----------> " + fsInputReq.getApplicationId());
			fsInputRes.setIndustrylist(industryList);
            
			List<Long> sectorList = industrySectorRepository.getSectorByApplicationId(fsInputReq.getApplicationId());
			logger.info("TOTAL SECTOR FOUND ------------->" + sectorList.size() + "------------By APP Id -----------> " + fsInputReq.getApplicationId());
			fsInputRes.setSectorlist(sectorList);
            
            List<Long> subSectorList = subSectorRepository.getSubSectorByApplicationId(fsInputReq.getApplicationId());
			logger.info("TOTAL SUB SECTOR FOUND ------------->" + subSectorList.size() + "------------By APP Id -----------> " + fsInputReq.getApplicationId());
			fsInputRes.setSubsectors(subSectorList);

			return new ResponseEntity<LoansResponse>(
					new LoansResponse("One form data successfully fetched", HttpStatus.OK.value(), fsInputRes),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while fetching one form data");
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(new LoansResponse("Error while fetching one form input data",
					HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<LoansResponse> getDirectorDetail(FundSeekerInputRequestResponse fundSeekerInputRequest) {

		FundSeekerInputRequestResponse fundSeekerInputResponse = new FundSeekerInputRequestResponse();
		fundSeekerInputResponse.setApplicationId(fundSeekerInputRequest.getApplicationId());
		try {
			// === Applicant Address
			CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
					.findOneByApplicationIdId(fundSeekerInputRequest.getApplicationId());
			if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail)) {
				fundSeekerInputResponse.setDirectorBackgroundDetailRequestsList(Collections.emptyList());
				logger.info("Data not found for given applicationid");
				return new ResponseEntity<LoansResponse>(new LoansResponse("Data not found for given applicationid",
						HttpStatus.BAD_REQUEST.value(), fundSeekerInputResponse), HttpStatus.OK);
			}

			BeanUtils.copyProperties(corporateApplicantDetail, fundSeekerInputResponse);
			copyAddressFromDomainToRequest(corporateApplicantDetail, fundSeekerInputResponse);
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getConstitutionId()) && corporateApplicantDetail.getConstitutionId()==7){
				ReportRequest reportRequest = new ReportRequest();
				reportRequest.setApplicationId(fundSeekerInputRequest.getApplicationId());
				try {
					String orgName = analyzerClient.getOrgNameByAppId(reportRequest);
					fundSeekerInputResponse.setOrganisationName(orgName);
					logger.info("Fetched Organisation Name from Bank Statement ==>"+orgName);
				} catch (Exception e) {
					e.printStackTrace();
					logger.info("Error while getting perfios data");
				}
			}
			// === Director
			List<DirectorBackgroundDetail> directorBackgroundDetailList = directorBackgroundDetailsRepository
					.listPromotorBackgroundFromAppId(fundSeekerInputRequest.getApplicationId());

			List<DirectorBackgroundDetailRequest> directorBackgroundDetailRequestList = new ArrayList<DirectorBackgroundDetailRequest>(
					directorBackgroundDetailList.size());

			DirectorBackgroundDetailRequest directorBackgroundDetailRequest = null;
			for (DirectorBackgroundDetail directorBackgroundDetail : directorBackgroundDetailList) {

				directorBackgroundDetailRequest = new DirectorBackgroundDetailRequest();
				BeanUtils.copyProperties(directorBackgroundDetail, directorBackgroundDetailRequest);
				if(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetail.getIsMainDirector()) && (directorBackgroundDetail.getIsMainDirector()) && !CommonUtils.isObjectNullOrEmpty(directorBackgroundDetail.getDirectorPersonalDetail())){
					DirectorPersonalDetailRequest directorPersonalDetailRequest = new DirectorPersonalDetailRequest();
					BeanUtils.copyProperties(directorBackgroundDetail.getDirectorPersonalDetail(), directorPersonalDetailRequest);
					directorBackgroundDetailRequest.setDirectorPersonalDetailRequest(directorPersonalDetailRequest);
				}
				directorBackgroundDetailRequestList.add(directorBackgroundDetailRequest);
			}
			fundSeekerInputResponse.setDirectorBackgroundDetailRequestsList(directorBackgroundDetailRequestList);

			logger.info("director detail successfully fetched");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Director detail successfully fetched",
					HttpStatus.OK.value(), fundSeekerInputResponse), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("error while fetching director detail");
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse("Error while fetching director detail", HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@Override
	public LoansResponse callMatchEngineClient(Long applicationId, Long userId, Integer businessTypeId) {
		ConnectResponse postOneForm;
		try {
			postOneForm = connectClient.postOneForm(applicationId, userId, businessTypeId);
			if (postOneForm != null) {
				logger.info("postOneForm=======================>Client Connect Response=============>{}",
						postOneForm.toString());
				if (!postOneForm.getProceed().booleanValue()) {
					return new LoansResponse("Not Eligibile from Matchengine", HttpStatus.BAD_REQUEST.value());
				} else {
					return new LoansResponse("Successfully Matched", HttpStatus.OK.value());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Error while Calling Matchengine after Oneform Submit=============");
		}
		return new LoansResponse("Something went wrong while Checking your Eligibility",
				HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	private static void copyAddressFromRequestToDomain(FundSeekerInputRequestResponse from,
			CorporateApplicantDetail to) {
		// Setting Regsiterd Address
		if (from.getFirstAddress() != null) {
			to.setRegisteredPremiseNumber(from.getFirstAddress().getPremiseNumber());
			to.setRegisteredLandMark(from.getFirstAddress().getLandMark());
			to.setRegisteredStreetName(from.getFirstAddress().getStreetName());
			to.setRegisteredPincode(from.getFirstAddress().getPincode());
			to.setRegisteredCityId(from.getFirstAddress().getCityId());
			to.setRegisteredStateId(from.getFirstAddress().getStateId());
			to.setRegisteredCountryId(from.getFirstAddress().getCountryId());
			to.setRegisteredDistMappingId(from.getFirstAddress().getDistrictMappingId());
		}

		/*
		 * // Setting Administrative Address if (from.getSameAs() != null &&
		 * from.getSameAs().booleanValue()) { if (from.getFirstAddress() != null) {
		 * to.setAdministrativePremiseNumber(from.getFirstAddress().getPremiseNumber());
		 * to.setAdministrativeLandMark(from.getFirstAddress().getLandMark());
		 * to.setAdministrativeStreetName(from.getFirstAddress().getStreetName());
		 * to.setAdministrativePincode(from.getFirstAddress().getPincode());
		 * to.setAdministrativeCityId(from.getFirstAddress().getCityId());
		 * to.setAdministrativeStateId(from.getFirstAddress().getStateId());
		 * to.setAdministrativeCountryId(from.getFirstAddress().getCountryId()); } }
		 * else { if (from.getSecondAddress() != null) {
		 * to.setAdministrativePremiseNumber(from.getSecondAddress().getPremiseNumber())
		 * ; to.setAdministrativeLandMark(from.getSecondAddress().getLandMark());
		 * to.setAdministrativeStreetName(from.getSecondAddress().getStreetName());
		 * to.setAdministrativePincode(from.getSecondAddress().getPincode());
		 * to.setAdministrativeCityId(from.getSecondAddress().getCityId());
		 * to.setAdministrativeStateId(from.getSecondAddress().getStateId());
		 * to.setAdministrativeCountryId(from.getSecondAddress().getCountryId()); } }
		 */
	}

	private static void copyAddressFromDomainToRequest(CorporateApplicantDetail from,
			FundSeekerInputRequestResponse to) {
		// Setting Regsiterd Address
		Address address = new Address();

		address.setPremiseNumber(from.getRegisteredPremiseNumber());
		address.setLandMark(from.getRegisteredLandMark());
		address.setStreetName(from.getRegisteredStreetName());
		address.setPincode(from.getRegisteredPincode());
		address.setCityId(from.getRegisteredCityId());
		address.setStateId(from.getRegisteredStateId());
		address.setCountryId(from.getRegisteredCountryId());
		address.setDistrictMappingId(from.getRegisteredDistMappingId());
		to.setFirstAddress(address);
		/*
		 * if (from.getSameAs() != null && from.getSameAs()) {
		 * to.setSecondAddress(address); } else { address = new Address();
		 * address.setPremiseNumber(from.getAdministrativePremiseNumber());
		 * address.setLandMark(from.getAdministrativeLandMark());
		 * address.setStreetName(from.getAdministrativeStreetName());
		 * address.setPincode(from.getAdministrativePincode());
		 * address.setCityId(from.getAdministrativeCityId());
		 * address.setStateId(from.getAdministrativeStateId());
		 * address.setCountryId(from.getAdministrativeCountryId());
		 * to.setSecondAddress(address);
		 * 
		 * }
		 */

		// Setting Administrative Address
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.capitaworld.service.loans.service.fundseeker.corporate.
	 * FundSeekerInputRequestService#invokeFraudAnalytics(com.capitaworld.service.
	 * loans.model.corporate.FundSeekerInputRequestResponse)
	 */
	@Override
	public LoansResponse invokeFraudAnalytics(FundSeekerInputRequestResponse fundSeekerInputRequestResponse)
			throws Exception {
		
		try {
			logger.info("Start invokeFraudAnalytics()");
			LoansResponse res = new LoansResponse();
			if("Y".equals(String.valueOf(environment.getRequiredProperty("cw.call.service_fraudanalytics")))) {
				Boolean isNTB = false;
				HunterRequestDataResponse hunterRequestDataResponse = null;
				if(fundSeekerInputRequestResponse.getBusinessTypeId()!=null && fundSeekerInputRequestResponse.getBusinessTypeId() == 2) {// FOR NTB ONLY
					isNTB = true;
					hunterRequestDataResponse = loanApplicationService
							.getDataForHunterForNTB(fundSeekerInputRequestResponse.getApplicationId());
				}
				else {
			hunterRequestDataResponse = loanApplicationService
					.getDataForHunter(fundSeekerInputRequestResponse.getApplicationId());
				}
			AnalyticsRequest request = new AnalyticsRequest();
			request.setApplicationId(fundSeekerInputRequestResponse.getApplicationId());
			request.setUserId(fundSeekerInputRequestResponse.getUserId());
			request.setData(hunterRequestDataResponse);
			request.setIsNtb(isNTB);
			res.setMessage("Oneform Saved Successfully");
			res.setStatus(HttpStatus.OK.value());
			AnalyticsResponse response = fraudAnalyticsClient.callHunterIIAPI(request);
			if (response != null) {
				
				Boolean resp = false;
				if(response.getData()!=null) {
					resp = Boolean.valueOf(response.getData().toString());
				}
				res.setData(resp);
				if(resp) {
					res.setStatus(HttpStatus.OK.value());
					res.setMessage("Oneform Saved Successfully");
				}
				else {
					res.setStatus(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS.value());
				res.setMessage(CommonUtils.HUNTER_INELIGIBLE_MESSAGE);
				}
			}
			
			logger.info("End invokeFraudAnalytics() with resp : "+res.getData());
			return res;
			}
			else {
				logger.info("End invokeFraudAnalytics() Skiping Fraud Analytics call");
				   logger.info("FUNDSEEKER INPUT SAVED SUCCESSFULLY");
	                return new LoansResponse("Oneform Saved Successfully", HttpStatus.OK.value());
	                      
			}
		} catch (Exception e) {
			logger.info("End invokeFraudAnalytics() Error in Fraud Analytics call");
			e.printStackTrace();
			//throw new Exception();
			logger.info("End invokeFraudAnalytics() ERROR IN FRAUD ANALYTICS CALL");
			 return new LoansResponse("Oneform Saved Successfully", HttpStatus.OK.value());
		}
	}

	@Override
	public LoansResponse postDirectorBackground(NTBRequest ntbRequest) {
		logger.info("Start postDirectorBackground()");
		try {
			ConnectResponse connectResponse = connectClient.postDirectorBackground(ntbRequest.getApplicationId(),
					ntbRequest.getUserId(), ntbRequest.getBusineeTypeId(), ntbRequest.getDirectorId());
			if (connectResponse == null) {
				return new LoansResponse(
						"Something goes wrong with the internal server. Please try again after sometime.",
						HttpStatus.BAD_REQUEST.value());
			}
			logger.info("End postDirectorBackground()");
			if (!connectResponse.getProceed().booleanValue()) {
				return new LoansResponse(connectResponse.getMessage(), HttpStatus.BAD_REQUEST.value());
			} else {
				return new LoansResponse("Success", HttpStatus.OK.value());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
