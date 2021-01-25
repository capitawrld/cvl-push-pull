package com.opl.service.loans.service.pushpull.impl;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.gst.model.GstResponse;
import com.opl.mudra.api.gst.model.yuva.request.GSTR1Request;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.gst.GstClient;
import com.opl.mudra.client.users.UsersClient;
import com.opl.profile.api.model.CommonResponse;
import com.opl.profile.api.model.LoanMappingRequest;
import com.opl.profile.api.model.ProfileRequest;
import com.opl.profile.client.ProfileClient;
import com.opl.service.loans.model.pushpull.PushPullRequest;
import com.opl.service.loans.repository.common.LoanRepository;
import com.opl.service.loans.service.pushpull.PushPullApplicationService;

@Service
@Transactional
public class PushPullApplicationServiceImpl implements PushPullApplicationService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private UsersClient userClient;
	
	@Autowired
	private ProfileClient profileClient;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private GstClient gstClient;
		
	private Long fpUserType = 2L;
	
	private static final Logger logger = LoggerFactory.getLogger(PushPullApplicationServiceImpl.class.getName());
	private static final String PROFILE_EXISTS = "Invalid request, Profile with same pan already exists!";


	@Override
	public LoansResponse saveOrUpdate(PushPullRequest pushPullRequest) throws LoansException {
		LoansResponse loansResponse = new LoansResponse();
		if (!CommonUtils.isObjectNullOrEmpty(pushPullRequest)) {
			Long userid = loanRepository.getUserTypeByEmail(pushPullRequest.getEmail(), pushPullRequest.getMobile());
			if (!CommonUtils.isObjectNullOrEmpty(userid) && userid != 0) {
				loansResponse.setStatus(HttpStatus.CONFLICT.value());
				loansResponse.setMessage("User already enrolled in system");
				return loansResponse;
			}
			else {
				UserResponse userResponse = saveUsersData(pushPullRequest);
			}
		}
		return null;
	}
	
	private UserResponse saveUsersData(PushPullRequest pushPullRequest) {
		try {
			UsersRequest usersRequest = new UsersRequest();
			usersRequest.setEmail(pushPullRequest.getEmail());
			usersRequest.setMobile(pushPullRequest.getMobile());
			usersRequest.setPan(pushPullRequest.getPan());
			usersRequest.setGstin(pushPullRequest.getGstIn());
			usersRequest.setBusinessTypeId(pushPullRequest.getBusinessTypeId());
			UserResponse userResponse = userClient.saveCvlPushPull(usersRequest);
		
			Long id = userResponse.getId();
			com.opl.profile.api.model.CommonResponse profileResponse = createProfileData(id, userResponse ,pushPullRequest);
			if(CommonUtils.isObjectNullOrEmpty(profileResponse) ||
					(!CommonUtils.isObjectNullOrEmpty(profileResponse) && 
							   CommonUtils.isObjectNullOrEmpty(profileResponse.getData()))) {
				return null;
			}
			
			Long profileId = Long.parseLong(profileResponse.getData().toString());
			
			GSTR1Request gstRequest = new GSTR1Request();
			gstRequest.setUserId(userResponse.getId());
			gstRequest.setPan(pushPullRequest.getPan());
			//gstRequest.setApplicationId(connectResponse.getApplicationId());
			gstRequest.setProfileId(profileId);
			GstResponse createGstProfileMappingApplication = gstClient.createGstProfileMappingApplication(gstRequest);
			
			
//			ProfileRequest request = new ProfileRequest();
//			request.setNoGstReasone(pushPullRequest.getGstIn());
//			request.setPanNo("AAGFV5271N");
//			request.setUserId(userResponse.getId());
//			request.setCampaignCode("sbi");
//			request.setGstTypeId(3);
//			request.setCampaignType("sbi");
//			com.opl.profile.api.model.CommonResponse profileLoanMappingResponse = profileClient.saveProfile(request);
		
			return userResponse;
		}catch(Exception e) {
			logger.error("Error While saveUsersData: ", e);
			return null;
		}
	}

	private CommonResponse createProfileData(Long id, UserResponse userResponse, PushPullRequest pushPullRequest) {
		ProfileRequest profileRequest=new ProfileRequest();
		profileRequest.setName(pushPullRequest.getUsername());
		profileRequest.setCampaignCode("sbi");
		profileRequest.setCreatedBy(id);
		profileRequest.setCreatedDate(new Date());
		profileRequest.setModifiedBy(id);
		profileRequest.setModifiedDate(new Date());
		profileRequest.setIsActive(true);
		profileRequest.setPanNo(pushPullRequest.getPan());
		profileRequest.setTypeId(1);
		profileRequest.setUserId(userResponse.getId());
		profileRequest.setGstTypeId(3);
		
		com.opl.profile.api.model.CommonResponse profileResponse = profileClient.saveProfile(profileRequest);
		if(!CommonUtils.isObjectNullOrEmpty(profileResponse) && 
				profileResponse.getStatus().equals(HttpStatus.BAD_REQUEST.value()) && 
				profileResponse.getMessage().equalsIgnoreCase(PROFILE_EXISTS)) {
			com.opl.profile.api.model.CommonResponse profileIdResponse = profileClient.getProfileIdByUserIdAndPanNo(profileRequest);
			if(!CommonUtils.isObjectNullOrEmpty(profileIdResponse) && 
					profileIdResponse.getStatus().equals(HttpStatus.OK.value()) &&
					!CommonUtils.isObjectNullOrEmpty(profileResponse.getData())) {
				Long profileId = Long.parseLong(profileResponse.getData().toString());
				profileResponse.setData(profileId);
			}else {
				return null;
			}
		}
		return profileResponse;

	}

}
