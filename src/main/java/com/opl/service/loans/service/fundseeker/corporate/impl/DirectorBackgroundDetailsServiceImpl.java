package com.opl.service.loans.service.fundseeker.corporate.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.pennydrop.client.PennydropClient;
import com.opl.mudra.api.connect.ConnectResponse;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.DirectorBackgroundDetailRequest;
import com.opl.mudra.api.loans.model.DirectorPersonalDetailRequest;
import com.opl.mudra.api.loans.model.EmploymentDetailRequest;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.CommonUtils.APIFlags;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.client.connect.ConnectClient;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.DirectorBackgroundDetail;
import com.opl.service.loans.repository.common.CommonRepository;
import com.opl.service.loans.repository.fundseeker.corporate.DirectorBackgroundDetailsRepository;
import com.opl.service.loans.service.fundseeker.corporate.DirectorBackgroundDetailsService;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class DirectorBackgroundDetailsServiceImpl implements DirectorBackgroundDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(DirectorBackgroundDetailsServiceImpl.class);

	@Autowired
	private DirectorBackgroundDetailsRepository directorBackgroundDetailsRepository;
	
	@Autowired
	private ConnectClient connectClient;
	
	@Autowired
	private CommonRepository commonRepository;
	//private static final String SIDBI_AMOUNT = "com.capitaworld.sidbi.amount";
	
//	@Autowired
//	private Environment environment;
	
	@Autowired
	private PennydropClient pennydropClient;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {
		
		if(!CommonUtils.isObjectNullOrEmpty(frameRequest) && !CommonUtils.isObjectNullOrEmpty(frameRequest.getIsFromClient()) && frameRequest.getIsFromClient() ) {
					directorBackgroundDetailsRepository.inActive(frameRequest.getUserId(), frameRequest.getApplicationId());
		}
		
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				DirectorBackgroundDetailRequest directorBackgroundDetailRequest= (DirectorBackgroundDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, DirectorBackgroundDetailRequest.class);
				saveDirectorInfo(directorBackgroundDetailRequest, frameRequest.getApplicationId(), frameRequest.getUserId());
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save directorBackgroundDetail :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}


	@Override
	public boolean saveDirectorInfo(DirectorBackgroundDetailRequest backgroundDetailRequest,Long applicationId,Long userId){

		DirectorBackgroundDetail  directorBackgroundDetail= null;
		if (backgroundDetailRequest.getPanNo() != null && applicationId != null) {
			directorBackgroundDetail = directorBackgroundDetailsRepository.findByApplicationIdIdAndIsActiveIsTrueAndPanNo(applicationId,backgroundDetailRequest.getPanNo());
		} else {
			directorBackgroundDetail = new DirectorBackgroundDetail();
			directorBackgroundDetail.setCreatedBy(userId);
			directorBackgroundDetail.setCreatedDate(new Date());
		}
		BeanUtils.copyProperties(backgroundDetailRequest, directorBackgroundDetail, "applicationId");
		directorBackgroundDetail.setApplicationId(new LoanApplicationMaster(applicationId));
		directorBackgroundDetail.setModifiedBy(userId);
		directorBackgroundDetail.setModifiedDate(new Date());
		directorBackgroundDetailsRepository.save(directorBackgroundDetail);
		return true;
	}

	@Override
	public List<DirectorBackgroundDetailRequest> getDirectorBackgroundDetailList(Long applicationId,Long userId) throws LoansException {
		try {
			List<DirectorBackgroundDetail> directorBackgroundDetails = null;
			if(userId != null) {
				directorBackgroundDetails = directorBackgroundDetailsRepository.listPromotorBackgroundFromAppId(applicationId,userId);	
			}else {
				directorBackgroundDetails = directorBackgroundDetailsRepository.listPromotorBackgroundFromAppId(applicationId);
			}
			List<DirectorBackgroundDetailRequest> directorBackgroundDetailRequests = new ArrayList<DirectorBackgroundDetailRequest>();

			for (DirectorBackgroundDetail detail : directorBackgroundDetails) {
				DirectorBackgroundDetailRequest directorBackgroundDetailRequest = new DirectorBackgroundDetailRequest();
				if(!CommonUtils.isObjectNullOrEmpty(detail.getEmploymentDetail())){
					EmploymentDetailRequest employmentDetailRequest = new EmploymentDetailRequest();
					BeanUtils.copyProperties(detail.getEmploymentDetail(),employmentDetailRequest);
					directorBackgroundDetailRequest.setEmploymentDetailRequest(employmentDetailRequest);
				}
				if(!CommonUtils.isObjectNullOrEmpty(detail.getDirectorPersonalDetail())){
					DirectorPersonalDetailRequest directorPersonalDetailRequest = new DirectorPersonalDetailRequest();
					BeanUtils.copyProperties(detail.getDirectorPersonalDetail(),directorPersonalDetailRequest);
					directorBackgroundDetailRequest.setDirectorPersonalDetailRequest(directorPersonalDetailRequest);
				}
				BeanUtils.copyProperties(detail, directorBackgroundDetailRequest);
				DirectorBackgroundDetailRequest.printFields(directorBackgroundDetailRequest);
				directorBackgroundDetailRequests.add(directorBackgroundDetailRequest);
			}
			
			return directorBackgroundDetailRequests;
		} catch (Exception e) {
			logger.info("Exception  in getdirectorBackgroundDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	
	@Override
	public List<DirectorBackgroundDetailRequest> getDirectorBasicDetailsListForNTB(Long applicationId) throws LoansException {
		try {
			List<DirectorBackgroundDetail> dirBackDetails = directorBackgroundDetailsRepository.listPromotorBackgroundFromAppId(applicationId);
			List<DirectorBackgroundDetailRequest> dirBackDetailReqList = new ArrayList<DirectorBackgroundDetailRequest>();

			DirectorBackgroundDetailRequest dirBackDetailReq = null;
			for (DirectorBackgroundDetail detail : dirBackDetails) {
				dirBackDetailReq = new DirectorBackgroundDetailRequest();
				dirBackDetailReq.setPanNo(detail.getPanNo());
				dirBackDetailReq.setDirectorsName(detail.getDirectorsName());
				dirBackDetailReq.setId(detail.getId());
				dirBackDetailReq.setIsMainDirector(detail.getIsMainDirector());
				dirBackDetailReq.setAmount(commonRepository.getSidbiAmount() != null ? commonRepository.getSidbiAmount() : "1180");
				dirBackDetailReqList.add(dirBackDetailReq);
			}
			return dirBackDetailReqList;
		} catch (Exception e) {
			logger.error("Exception  in getDirectorBasicDetailsListForNTB  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	
	@Override
	public DirectorBackgroundDetailRequest getDirectorBackgroundDetail(Long id){
		try {
			DirectorBackgroundDetail directorBackgroundDetail = directorBackgroundDetailsRepository.findByIdAndIsActive(id, true);
			if(CommonUtils.isObjectNullOrEmpty(directorBackgroundDetail)) {
				logger.warn("Director Background Details not found For Id ==>{}",id);
				return null;
			}
			DirectorBackgroundDetailRequest directorBackgroundDetailRequest = new DirectorBackgroundDetailRequest();
			BeanUtils.copyProperties(directorBackgroundDetail, directorBackgroundDetailRequest);
			DirectorBackgroundDetailRequest.printFields(directorBackgroundDetailRequest);
			return directorBackgroundDetailRequest;
		} catch (Exception e) {
			logger.info("Exception  in getdirectorBackgroundDetail  :-",e);
			return null;
		}
	}

	@Override
	public Boolean updateFlag(Long directorId, Integer apiId, Boolean apiFlag,Long userId) {

		logger.info("Enter in updateFlag()");
		APIFlags apiFlagObj = CommonUtils.APIFlags.fromId(apiId);
		if(apiFlag == null) {
			logger.warn("Invalid Flag===>{}",apiId);
			logger.info("Exit in updateFlag()");
			return false;
		}
		int updatedRows = 0;
		switch (apiFlagObj) {
		case ITR:
			updatedRows = directorBackgroundDetailsRepository.updateITRFlag(userId, directorId, apiFlag);
			break;
		case CIBIL:
			updatedRows = directorBackgroundDetailsRepository.updateCIBILFlag(userId, directorId, apiFlag);
			break;
		case BANK_STATEMENT:
			updatedRows = directorBackgroundDetailsRepository.updateBankStatementFlag(userId, directorId, apiFlag);
			break;
		case ONE_FORM:
			updatedRows = directorBackgroundDetailsRepository.updateOneFormFlag(userId, directorId, apiFlag);
			break;

		default:
			break;
		}
		logger.info("updatedRows====>{}",updatedRows);
		logger.info("Exit in updateFlag()");
		return updatedRows > 0;
	}

	@Override
	public Boolean saveDirectors(Long applicationId, Long userId, Integer noOfDirector) {
		logger.info("Enter in saveDirectors()");
		directorBackgroundDetailsRepository.inActive(userId, applicationId);
		if(noOfDirector <= 0) {
			logger.warn("No Of Director Found Less than or Equal 0");
			return false;
		}
		LoanApplicationMaster loanMs = new LoanApplicationMaster(applicationId);
		for(int i = 0; i < noOfDirector; i++) {
			DirectorBackgroundDetail backgroundDetail = new DirectorBackgroundDetail();
			backgroundDetail.setApplicationId(loanMs);
			backgroundDetail.setIsActive(true);
			backgroundDetail.setCreatedBy(userId);
			backgroundDetail.setCreatedDate(new Date());
			backgroundDetail.setIsItrCompleted(false);
			backgroundDetail.setIsCibilCompleted(false);
			backgroundDetail.setIsBankStatementCompleted(false);
			backgroundDetail.setIsOneFormCompleted(false);
			directorBackgroundDetailsRepository.save(backgroundDetail);
		}
		try {
			ConnectResponse connResponse = connectClient.postMCQNTB(applicationId, userId, CommonUtils.BusinessType.NEW_TO_BUSINESS.getId());
			if(!CommonUtils.isObjectNullOrEmpty(connResponse) && !CommonUtils.isObjectNullOrEmpty(connResponse.getProceed()) && connResponse.getProceed()) {
				logger.info("Connect Response--------------------------------> " + connResponse.toString());
				return true;
			} else {
				logger.info("Connect Response--------------------------------> null");
			}
		} catch (Exception e) {
			logger.error("Throw Exception While Call Connect Client : ",e);
		}
		logger.info("Exit in saveDirectors()");
		return false;
	}


	@Override
	public boolean inactive(Long applicationId, Long userId) {
		int inActive = directorBackgroundDetailsRepository.inActive(userId, applicationId);
		return inActive > 0;
	}

	@Override
	public LoansResponse panVerification(List<DirectorBackgroundDetailRequest> directors) {

		LoansResponse resp = new LoansResponse();
		List<com.opl.api.pennydrop.model.CommonResponse> response = new ArrayList<>();
		
		for (DirectorBackgroundDetailRequest dir : directors) {
			com.opl.api.pennydrop.model.PanVerificationRequest request = new com.opl.api.pennydrop.model.PanVerificationRequest();
			request.setPan(dir.getPanNo());
			request.setDob(dir.getDobString());
			request.setApplicationId(dir.getApplicationId());
			request.setName(dir.getDirectorsName());

			try {
				response.add(pennydropClient.panVerification(request));
				resp.setStatus(HttpStatus.OK.value());
			} catch (Exception e) {
				logger.error("Exception in panVerification :{} ",e);
				resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				resp.setMessage("Some thing went wrong");
				logger.error("Error while Validating Pan For Director = >{}",e);
			}
		}
		resp.setData(response);
		return resp;
	}		

}
