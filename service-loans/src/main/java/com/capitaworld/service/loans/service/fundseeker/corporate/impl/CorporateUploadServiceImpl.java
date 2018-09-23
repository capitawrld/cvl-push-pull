package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.capitaworld.service.dms.client.DMSClient;
import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.model.DocumentRequest;
import com.capitaworld.service.dms.model.DocumentResponse;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.ddr.DDRFormDetails;
import com.capitaworld.service.loans.model.common.DocumentUploadFlagRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.ddr.DDRFormDetailsRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateUploadService;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.fundseeker.retail.impl.CoApplicantServiceImpl;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;

@Service
@Transactional
public class CorporateUploadServiceImpl implements CorporateUploadService {

	private static final Logger logger = LoggerFactory.getLogger(CorporateUploadServiceImpl.class);

	// @Autowired
	// private Environment environment;

	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	CoApplicantService coApplicantService;
	
	@Autowired
	GuarantorService guarantorService;
	
	@Autowired
	private DDRFormDetailsRepository ddrFormDetailsRepository;

	@SuppressWarnings("unchecked")
	@Override
	public DocumentResponse uploadProfile(Long applicantId, Long mappingId, String fileName, String userType,
			MultipartFile multipartFile) throws Exception {
		try {
			JSONObject jsonObj = new JSONObject();

			if (CommonUtils.UploadUserType.UERT_TYPE_APPLICANT.equalsIgnoreCase(userType)) {
				jsonObj.put("applicationId", applicantId);
			} else if (CommonUtils.UploadUserType.UERT_TYPE_CO_APPLICANT.equalsIgnoreCase(userType)) {
				// here we have set same applicant variable because when
				// requested user is co-applicant then it "coApplicantId" will
				// be considered and same as for "guarantors".
				jsonObj.put("coApplicantId", applicantId);
			} else if (CommonUtils.UploadUserType.UERT_TYPE_GUARANTOR.equalsIgnoreCase(userType)) {
				// here we have set same applicant variable because when
				// requested user is co-applicant then it "coApplicantId" will
				// be considered and same as for "guarantors".
				jsonObj.put("guarantorId", applicantId);
			}

			jsonObj.put("productDocumentMappingId", mappingId);
			jsonObj.put("userType", userType);
			jsonObj.put("originalFileName", fileName);
			DocumentResponse documentResponse = dmsClient.productImage(jsonObj.toString(), multipartFile);
			return documentResponse;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			logger.error("Error while uploading Profile Document");
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public DocumentResponse getProfilePic(Long applicantId, Long mappingId, String userType) throws Exception {
		try {
			DocumentRequest docRequest = new DocumentRequest();
			if (CommonUtils.UploadUserType.UERT_TYPE_APPLICANT.equalsIgnoreCase(userType)) {
				docRequest.setApplicationId(applicantId);
			} else if (CommonUtils.UploadUserType.UERT_TYPE_CO_APPLICANT.equalsIgnoreCase(userType)) {
				// here we have set same applicant variable because when
				// requested user is co-applicant then it "coApplicantId" will
				// be considered and same as for "guarantors".
				docRequest.setCoApplicantId(applicantId);
			} else if (CommonUtils.UploadUserType.UERT_TYPE_GUARANTOR.equalsIgnoreCase(userType)) {
				// here we have set same applicant variable because when
				// requested user is co-applicant then it "coApplicantId" will
				// be considered and same as for "guarantors".
				docRequest.setGuarantorId(applicantId);
			}
			docRequest.setProductDocumentMappingId(mappingId);
			docRequest.setUserType(userType);
			return dmsClient.listProductDocument(docRequest);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			logger.error("Error while getting Profile Document");
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public DocumentResponse uploadOtherDoc(String documentRequestString, MultipartFile multipartFiles, Long userId)
			throws Exception {
		
		
		try {
			DocumentResponse response = dmsClient.uploadFile(documentRequestString, multipartFiles);
			if(!CommonUtils.isObjectNullOrEmpty(response) && response.getStatus() == HttpStatus.OK.value()) {
				DocumentRequest request = MultipleJSONObjectHelper.getObjectFromString(documentRequestString, DocumentRequest.class);
				request.setUserId(userId);
				try{
					logger.info("saving Upload FLag");
				Long resp = saveDocumentFLag( request);
				if(resp == 0L){
					logger.error("Error while saving Upload FLag");
					throw new Exception("Error while saving Upload FLag");
				}
				
				}
				catch (Exception e) {
					e.printStackTrace();
					logger.error("Error while saving Upload FLag");
					throw new Exception("Error while saving Upload FLag");
				}	
			}
			return response;
		} catch (DocumentException e) {
			logger.error("Error while uploading Corporate Other Documents");
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public DocumentResponse getOtherDoc(DocumentRequest documentRequest) throws Exception {
		try {
			return dmsClient.listProductDocument(documentRequest);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			logger.error("Error while getting Corporate Other Documents");
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	// New UBI Requirement
		@Override
		public Map<String, Map<String, Object>> getOtherDocReport(Long applicationId) throws Exception {
			try {
				
				Long userId = loanApplicationRepository.getUserIdByApplicationId(applicationId);
				List<Long> proIdList = new ArrayList<>();
				List<Long> co_app_proIdList = new ArrayList<>();
				List<Long> gua_proIdList = new ArrayList<>();
				Long applicantArray[] = { 55L, 56L, 61L, 63L, 64L, 65L, 243L, 248L };
				Long co_appArray[] = { 57L, 58L, 69L, 71L, 72L, 73L, 254L, 259L };
				Long guarantorArray[] = { 59L, 60L, 77L, 79L, 80L, 81L, 264L, 269L };
				proIdList.addAll((List<Long>) Arrays.asList(applicantArray));
				co_app_proIdList.addAll((List<Long>) Arrays.asList(co_appArray));
				gua_proIdList.addAll((List<Long>) Arrays.asList(guarantorArray));
				Map<String, Map<String, Object>> maps = new HashMap<String, Map<String,Object>>();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("identity_proof", false);
				map.put("bank_statement", false);
				map.put("itr", false);
				map.put("audited_annual_report", false);
				map.put("address_proof", false);
				for (Long id : proIdList) {
					DocumentRequest documentRequest = new DocumentRequest();
					documentRequest.setApplicationId(applicationId);
					documentRequest.setProductDocumentMappingId(id);
					documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
					dmsClient.listProductDocument(documentRequest);
								
					if (dmsClient.listProductDocument(documentRequest).getDataList().size() > 0) {
						if(id.equals(55L) || id.equals(56L)){
							map.put("identity_proof", true);
						}
						else if(id.equals(61L)){
							map.put("bank_statement", true);
						}
						else if(id.equals(63L) || id.equals(243L) || id.equals(248L)){
							map.put("itr", true);
						}
						else if(id.equals(64L)){
							map.put("audited_annual_report", true);
						}
						else if(id.equals(65L)){
							map.put("address_proof", true);
						}
					}
				}
				maps.put("app",map);
				List<Long> ids = coApplicantService.getCoAppIds(userId, applicationId);
				
				int i =1;
				
				for (Long coappid : ids) {
					Map<String, Object> mapCoApp = new HashMap<String, Object>();
					mapCoApp.put("identity_proof", false);
					mapCoApp.put("bank_statement", false);
					mapCoApp.put("itr", false);
					mapCoApp.put("audited_annual_report", false);
					mapCoApp.put("address_proof", false);
					for (Long id : co_app_proIdList) {
						DocumentRequest documentRequest = new DocumentRequest();
						documentRequest.setCoApplicantId(coappid);
						documentRequest.setProductDocumentMappingId(id);
						documentRequest.setUserType(DocumentAlias.UERT_TYPE_CO_APPLICANT);
						dmsClient.listProductDocument(documentRequest);
								
						if (dmsClient.listProductDocument(documentRequest).getDataList().size() > 0) {
							if(id.equals(57L) || id.equals(58L)){
								mapCoApp.put("identity_proof", true);
							}
							else if(id.equals(69L)){
								mapCoApp.put("bank_statement", true);
							}
							else if(id.equals(71L) || id.equals(254L) || id.equals(259L)){
								mapCoApp.put("itr", true);
							}
							else if(id.equals(72L)){
								mapCoApp.put("audited_annual_report", true);
							}
							else if(id.equals(73L)){
								mapCoApp.put("address_proof", true);
							}
						} 
					}
					maps.put("coApp "+i,mapCoApp);
					i++;
				}
				
				
				
				List<Long> gua_ids = guarantorService.getGuarantorIds(userId, applicationId);
				
				int j = 1;
				for (Long guaId : gua_ids) {
					Map<String, Object> mapGua = new HashMap<String, Object>();
						mapGua.put("identity_proof", false);
						mapGua.put("bank_statement", false);
						mapGua.put("itr", false);
						mapGua.put("audited_annual_report", false);
						mapGua.put("address_proof", false);
				for (Long id : gua_proIdList) {
					DocumentRequest documentRequest = new DocumentRequest();
					documentRequest.setGuarantorId(guaId);
					documentRequest.setProductDocumentMappingId(id);
					documentRequest.setUserType(DocumentAlias.UERT_TYPE_GUARANTOR);
					dmsClient.listProductDocument(documentRequest);
						
					if (dmsClient.listProductDocument(documentRequest).getDataList().size() > 0) {
						if(id.equals(59L) || id.equals(60L)){
							mapGua.put("identity_proof", true);
						}
						else if(id.equals(77L)){
							mapGua.put("bank_statement", true);
						}
						else if(id.equals(79L) || id.equals(264L) || id.equals(269L)){
							mapGua.put("itr", true);
						}
						else if(id.equals(80L)){
							mapGua.put("audited_annual_report", true);
						}
						else if(id.equals(81L)){
							mapGua.put("address_proof", true);
						}
					} 
				}
				maps.put("Guar "+j, mapGua);
				j++;
				}
				
				return maps;
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				logger.error("Error while getting Corporate Other Documents");
				throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
			}

		}
		// end New UBI Requirement
		
	@Override
	public void updateLoanApplicationFlag(Long applicantId, Long userId, int tabType, Boolean isFilled,
			String filledCount) throws Exception {
		logger.info("In updateLoanApplicationFlag service method");
		logger.info("appId----------->" + applicantId + "------userId------->" + userId + 
				"---------tabtype------->"+tabType + "--------isFilled------->" + isFilled +
				"----------FileCount----------"+filledCount);
		try {
			switch (tabType) {
			case CommonUtils.TabType.PRIMARY_UPLOAD:
				loanApplicationRepository.setIsPrimaryUploadMandatoryFilled(applicantId, userId, isFilled);
				loanApplicationRepository.setPrimaryFilledCount(applicantId, userId, filledCount);
				break;
			case CommonUtils.TabType.FINAL_UPLOAD:
				logger.info("Before setIsFinalUploadMandatoryFilled");
				loanApplicationRepository.setIsFinalUploadMandatoryFilled(applicantId, userId, isFilled);
				logger.info("After setIsFinalUploadMandatoryFilled");
				logger.info("Before setFinalFilledCount");
				loanApplicationRepository.setFinalFilledCount(applicantId, userId, filledCount);
				logger.info("After setFinalFilledCount");
				break;
			case CommonUtils.TabType.FINAL_DPR_UPLOAD:
				loanApplicationRepository.setIsFinalDprMandatoryFilled(applicantId, userId, isFilled);
				loanApplicationRepository.setFinalFilledCount(applicantId, userId, filledCount);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error while updating Flag to loan_application_master for upload");
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	
	public Long saveDocumentFLag(DocumentRequest documentUploadFlagRequest) throws Exception {
//		DDRFormDetailsRequest
		try{
		DDRFormDetails dDRFormDetails = ddrFormDetailsRepository.getByAppIdAndIsActive(documentUploadFlagRequest.getApplicationId());
		if(CommonUtils.isObjectNullOrEmpty(dDRFormDetails)){
			dDRFormDetails = new DDRFormDetails();
			dDRFormDetails.setApplicationId(documentUploadFlagRequest.getApplicationId());
			dDRFormDetails.setUserId(documentUploadFlagRequest.getUserId());
			dDRFormDetails.setCreatedBy(documentUploadFlagRequest.getUserId());
			dDRFormDetails.setCreatedDate(new Date());
			dDRFormDetails.setModifyBy(documentUploadFlagRequest.getUserId());
			dDRFormDetails.setModifyDate(new Date());
			dDRFormDetails.setIsActive(true);
		}
		
		final int switchCase = documentUploadFlagRequest.getProductDocumentMappingId().intValue();
		switch (switchCase) {
		// Working Capital

		case 501:// WC
		case 502:// TL
		case 503:// USL
		case 504:// WCTL
			dDRFormDetails.setFieldAuditReport("Yes");
			break;
		
		case 9:// WC
		case 36:// TL
		case 276:// USL
		case 375:// WCTL
			dDRFormDetails.setAuditedFinancialsForLast3years("Yes");
			break;
		
		case 280:// WC
		case 13:// TL
		case 40:// USL
		case 379:// WCTL
			dDRFormDetails.setProvisionalFinancialsForCurrentYear("Yes");
			break;
		
		case 11:// WC
		case 38:// TL
		case 278:// USL
		case 377:// WCTL
			dDRFormDetails.setItrForLast3years("Yes");
			break;
		
		case 10:// WC
		case 37:// TL
		case 277:// USL
		case 376:// WCTL
			dDRFormDetails.setSanctionLetter("Yes");
			break;
		
		case 505:// WC
		case 506:// TL
		case 507:// USL
		case 508:// WCTL
			dDRFormDetails.setBankStatementOfLast12months("Yes");
			break;
		
		case 308:// WC
		case 309:// TL
		case 310:// USL
		case 399:// WCTL
			dDRFormDetails.setDebtorsList("Yes");
			break;
		
		case 509:// WC
		case 510:// TL
		case 511:// USL
		case 512:// WCTL
			dDRFormDetails.setFinancialFigures("Yes");
			break;
		
		case 18:// WC
		case 45:// TL
		case 311:// USL
		case 384:// WCTL
			dDRFormDetails.setMoaOfTheCompany("Yes");
			break;
		
		case 3:// WC
		case 30:// TL
		case 283:// USL
		case 369:// WCTL
			dDRFormDetails.setPanCardOfTheCompany("Yes");
			break;
		
		case 305:// WC
		case 306:// TL
		case 307:// USL
		case 398:// WCTL
			dDRFormDetails.setResolutionAndForm32forAdditionOfDirector("Yes");
			break;
		
		case 513:// WC
		case 514:// TL
		case 515:// USL
		case 516:// WCTL
			dDRFormDetails.setCentralSalesTaxRegistrationOfCompany("Yes");
			break;
		
		case 517:// WC
		case 518:// TL
		case 519:// USL
		case 520:// WCTL
			dDRFormDetails.setCentralExciseRegistrationOfCompany("Yes");
			break;
		
		case 521:// WC
		case 522:// TL
		case 523:// USL
		case 524:// WCTL
			dDRFormDetails.setVatRegistrationOfCompany("Yes");
			break;
		
		case 315:// WC
		case 316:// TL
		case 317:// USL
		case 401:// WCTL
			dDRFormDetails.setLetterOfIntentFromFundProviders("Yes");
			break;
		
		case 14:// WC
		case 41:// TL
		case 284:// USL
		case 397:// WCTL
			dDRFormDetails.setPanCardAndResidenceAddProofOfDirectors("Yes");
			break;
		
		case 12:// WC
		case 39:// TL
		case 279:// USL
		case 378:// WCTL
			dDRFormDetails.setCaCertifiedNetworthStatement("Yes");
			break;
		
		case 297:// WC
		case 298:// TL
		case 299:// USL
		case 396:// WCTL
			dDRFormDetails.setIrrOfAllDirectorsForLast2years("Yes");
			break;
		
		case 525:// WC
		case 526:// TL
		case 527:// USL
		case 528:// WCTL
			dDRFormDetails.setListOfDirectors("Yes");
			break;
		
		case 15:// WC
		case 42:// TL
		case 285:// USL
		case 381:// WCTL
			dDRFormDetails.setListOfShareholdersAndShareHoldingPatter("Yes");
			break;
		
		case 535:// WC
		case 536:// TL
		case 537:// USL
		case 538:// WCTL
			dDRFormDetails.setProfilePicCompany("Yes");
			break;
		
		case 539:// WC
		case 540:// TL
		case 541:// USL
		case 542:// WCTL
			dDRFormDetails.setSiteOrPromotorsPhotos("Yes");
			break;

		default:
			break;
		}
		dDRFormDetails.setModifyBy(documentUploadFlagRequest.getUserId());
		dDRFormDetails.setModifyDate(new Date());
		ddrFormDetailsRepository.save(dDRFormDetails);
		return 1L;
		}
		catch (Exception e) {
			return 0L;
		}
	}
	
	@Override
	public DocumentResponse listOfDocumentByMultiProDocMapId(DocumentRequest documentRequest) throws DocumentException {
		return dmsClient.listProDocByMultiProMapId(documentRequest);
	}


}
