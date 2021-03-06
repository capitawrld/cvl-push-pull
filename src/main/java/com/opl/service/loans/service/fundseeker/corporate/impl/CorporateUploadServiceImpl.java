package com.opl.service.loans.service.fundseeker.corporate.impl;

import java.io.IOException;
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

import com.opl.mudra.api.dms.exception.DocumentException;
import com.opl.mudra.api.dms.model.DocumentRequest;
import com.opl.mudra.api.dms.model.DocumentResponse;
import com.opl.mudra.api.dms.utils.DocumentAlias;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.client.dms.DMSClient;
import com.opl.service.loans.domain.fundseeker.ddr.DDRFormDetails;
import com.opl.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.ddr.DDRFormDetailsRepository;
import com.opl.service.loans.service.fundseeker.corporate.CorporateUploadService;

@Service
@Transactional
public class CorporateUploadServiceImpl implements CorporateUploadService {

	private static final Logger logger = LoggerFactory.getLogger(CorporateUploadServiceImpl.class);

	private static final String ERROR_WHILE_SAVING_UPLOAD_FLAG_MSG = "Error while saving Upload FLag";
	private static final String IDENTITY_PROOF = "identity_proof";
	private static final String ADDRESS_PROOF = "address_proof";
	private static final String BANK_STATEMENT = "bank_statement";
	private static final String AUDITED_ANNUAL_REPORT = "audited_annual_report";

	// @Autowired
	// private Environment environment;

	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private ApplicationProposalMappingRepository applicationProposalMappingRepository;

	@Autowired
	private DDRFormDetailsRepository ddrFormDetailsRepository;

	@SuppressWarnings("unchecked")
	@Override
	public DocumentResponse uploadProfile(Long applicantId, Long mappingId, String fileName, String userType,
			MultipartFile multipartFile) throws LoansException {
		try {
			JSONObject jsonObj = new JSONObject();
			if (CommonUtils.UploadUserType.UERT_TYPE_APPLICANT.equalsIgnoreCase(userType)) {
				jsonObj.put("applicationId", applicantId);
			}
			jsonObj.put("productDocumentMappingId", mappingId);
			jsonObj.put("userType", userType);
			jsonObj.put("originalFileName", fileName);
			return dmsClient.productImage(jsonObj.toString(), multipartFile);
		} catch (DocumentException e) {
			logger.error("Error while uploading Profile Document : ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public DocumentResponse getProfilePicByProposalId(Long proposalId,Long applicantId, Long mappingId, String userType) throws Exception {
		try {
			DocumentRequest docRequest = new DocumentRequest();
			if (CommonUtils.UploadUserType.UERT_TYPE_APPLICANT.equalsIgnoreCase(userType)) {
				docRequest.setApplicationId(applicantId);
			}
			docRequest.setProductDocumentMappingId(mappingId);
			docRequest.setUserType(userType);
			docRequest.setProposalMappingId(proposalId);
			return dmsClient.listProductDocumentByProposalId(docRequest);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			logger.error("Error while getting Profile Document");
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public DocumentResponse getProfilePic(Long applicantId, Long mappingId, String userType) throws LoansException {
		try {
			DocumentRequest docRequest = new DocumentRequest();
			if (CommonUtils.UploadUserType.UERT_TYPE_APPLICANT.equalsIgnoreCase(userType)) {
				docRequest.setApplicationId(applicantId);
			}
			docRequest.setProductDocumentMappingId(mappingId);
			docRequest.setUserType(userType);
			return dmsClient.listProductDocument(docRequest);
		} catch (DocumentException e) {
			logger.error("Error while getting Profile Document : ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public DocumentResponse uploadOtherDocByProposalId(String documentRequestString, MultipartFile multipartFiles, Long userId)
			throws LoansException {

		try {
			DocumentResponse response = dmsClient.uploadFileByProposalId(documentRequestString, multipartFiles);
			if(!CommonUtils.isObjectNullOrEmpty(response) && response.getStatus() == HttpStatus.OK.value()) {
				DocumentRequest request = MultipleJSONObjectHelper.getObjectFromString(documentRequestString, DocumentRequest.class);
				request.setUserId(userId);
				try{
					logger.info("saving Upload FLag");
					Long resp = saveDocumentFLagByProposalId( request);
					if(resp == 0L){
						logger.error(ERROR_WHILE_SAVING_UPLOAD_FLAG_MSG);
						throw new LoansException(ERROR_WHILE_SAVING_UPLOAD_FLAG_MSG);
					}
				}
				catch (Exception e) {
					logger.error("Error while saving Upload FLag : ",e);
					throw new LoansException(ERROR_WHILE_SAVING_UPLOAD_FLAG_MSG);
				}
			}
			return response;
		} catch (Exception e) {
			logger.error("Error while uploading Corporate Other Documents : ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public DocumentResponse uploadOtherDoc(String documentRequestString, MultipartFile multipartFiles, Long userId)
			throws LoansException {


		try {
			DocumentResponse response = dmsClient.uploadFile(documentRequestString, multipartFiles);
			if(!CommonUtils.isObjectNullOrEmpty(response) && response.getStatus() == HttpStatus.OK.value()) {
				DocumentRequest request = MultipleJSONObjectHelper.getObjectFromString(documentRequestString, DocumentRequest.class);
				request.setUserId(userId);
				try{
					logger.info("saving Upload FLag");
				Long resp = saveDocumentFLag( request);
				if(resp == 0L){
					logger.error(ERROR_WHILE_SAVING_UPLOAD_FLAG_MSG);
					throw new Exception(ERROR_WHILE_SAVING_UPLOAD_FLAG_MSG);
				}

				}
				catch (Exception e) {
					logger.error("Error while saving Upload FLag : ",e);
					throw new LoansException(ERROR_WHILE_SAVING_UPLOAD_FLAG_MSG);
				}
			}
			return response;
		} catch (DocumentException | IOException e) {
			logger.error("Error while uploading Corporate Other Documents");
			e.printStackTrace();
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public DocumentResponse getOtherDocByProposalId(DocumentRequest documentRequest) throws LoansException {
		try {
			return dmsClient.listProductDocumentByProposalId(documentRequest);
		} catch (DocumentException e) {
			logger.error("Error while getting Corporate Other Documents : ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public DocumentResponse getOtherDoc(DocumentRequest documentRequest) throws Exception {
		try {
			return dmsClient.listProductDocument(documentRequest);
		} catch (DocumentException e) {
			logger.error("Error while getting Corporate Other Documents : ",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	// New UBI Requirement
		@Override
		public Map<String, Map<String, Object>> getOtherDocReport(Long applicationId) throws LoansException {
			try {
				
				Long userId = loanApplicationRepository.getUserIdByApplicationId(applicationId);
				List<Long> proIdList = new ArrayList<>();
				List<Long> co_app_proIdList = new ArrayList<>();
				List<Long> gua_proIdList = new ArrayList<>();
				Long[] applicantArray = { 55L, 56L, 61L, 63L, 64L, 65L, 243L, 248L };
				Long[] co_appArray = { 57L, 58L, 69L, 71L, 72L, 73L, 254L, 259L };
				Long[] guarantorArray = { 59L, 60L, 77L, 79L, 80L, 81L, 264L, 269L };
				proIdList.addAll((List<Long>) Arrays.asList(applicantArray));
				co_app_proIdList.addAll((List<Long>) Arrays.asList(co_appArray));
				gua_proIdList.addAll((List<Long>) Arrays.asList(guarantorArray));
				Map<String, Map<String, Object>> maps = new HashMap<String, Map<String,Object>>();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(IDENTITY_PROOF, false);
				map.put(BANK_STATEMENT, false);
				map.put("itr", false);
				map.put(AUDITED_ANNUAL_REPORT, false);
				map.put(ADDRESS_PROOF, false);
				for (Long id : proIdList) {
					DocumentRequest documentRequest = new DocumentRequest();
					documentRequest.setApplicationId(applicationId);
					documentRequest.setProductDocumentMappingId(id);
					documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
					dmsClient.listProductDocument(documentRequest);
								
					if (dmsClient.listProductDocument(documentRequest).getDataList().size() > 0) {
						if(id.equals(55L) || id.equals(56L)){
							map.put(IDENTITY_PROOF, true);
						}
						else if(id.equals(61L)){
							map.put(BANK_STATEMENT, true);
						}
						else if(id.equals(63L) || id.equals(243L) || id.equals(248L)){
							map.put("itr", true);
						}
						else if(id.equals(64L)){
							map.put(AUDITED_ANNUAL_REPORT, true);
						}
						else if(id.equals(65L)){
							map.put(ADDRESS_PROOF, true);
						}
					}
				}
				maps.put("app",map);
				return maps;
			} catch (DocumentException e) {
				logger.error("Error while getting Corporate Other Documents : ",e);
				throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
			}

		}
		// end New UBI Requirement

	@Override
	public void updateLoanApplicationFlagByProposalId(Long proposalId,Long applicantId, Long userId, int tabType, Boolean isFilled,
										  String filledCount) throws LoansException {
		logger.info("In updateLoanApplicationFlag service method");
		logger.info("appId----------->" + applicantId + "-----------Proposal Id---------->"+ proposalId + "------userId------->" + userId +
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
					applicationProposalMappingRepository.setIsFinalUploadMandatoryFilled(proposalId,applicantId,isFilled);
					logger.info("After setIsFinalUploadMandatoryFilled");
					break;
				case CommonUtils.TabType.FINAL_DPR_UPLOAD:
					applicationProposalMappingRepository.setIsFinalDprMandatoryFilled(proposalId,applicantId,isFilled);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error while updating Flag to loan_application_master for upload");
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public void updateLoanApplicationFlag(Long applicantId, Long userId, int tabType, Boolean isFilled,
			String filledCount) throws LoansException {
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
			logger.error("Error while updating Flag to loan_application_master for upload : ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	public Long saveDocumentFLagByProposalId(DocumentRequest documentUploadFlagRequest) throws Exception {
//		DDRFormDetailsRequest
		try{
			DDRFormDetails dDRFormDetails = ddrFormDetailsRepository.getByProposaMappingIdAndApplicationId(documentUploadFlagRequest.getProposalMappingId(),documentUploadFlagRequest.getApplicationId());
			if(CommonUtils.isObjectNullOrEmpty(dDRFormDetails)){
				dDRFormDetails = new DDRFormDetails();
				dDRFormDetails.setApplicationId(documentUploadFlagRequest.getApplicationId());
				dDRFormDetails.setProposalMappingId(documentUploadFlagRequest.getProposalMappingId());
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

	public Long saveDocumentFLag(DocumentRequest documentUploadFlagRequest) throws Exception {
//		DDRFormDetailsRequest
		try{
		DDRFormDetails dDRFormDetails = ddrFormDetailsRepository.getByProposaMappingIdAndApplicationId(documentUploadFlagRequest.getProposalMappingId(),documentUploadFlagRequest.getApplicationId());
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
