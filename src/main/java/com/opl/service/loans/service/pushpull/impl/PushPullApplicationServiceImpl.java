package com.opl.service.loans.service.pushpull.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.auth.model.UserRequest;
import com.opl.mudra.api.gst.exception.GstException;
import com.opl.mudra.api.gst.model.GstResponse;
import com.opl.mudra.api.gst.model.yuva.request.GSTR1Request;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.notification.utils.NotificationAlias;
import com.opl.mudra.api.notification.utils.NotificationConstants;
import com.opl.mudra.api.notification.utils.NotificationMasterAlias;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.gst.GstClient;
import com.opl.mudra.client.users.UsersClient;
import com.opl.profile.api.model.CommonResponse;
import com.opl.profile.api.model.ProfileRequest;
import com.opl.profile.api.utils.CommonUtility;
import com.opl.profile.api.utils.MultipleJSONObjectHelper;
import com.opl.profile.client.ProfileClient;
import com.opl.service.loans.config.FPAsyncComponent;
import com.opl.service.loans.domain.TataMotorsLoanDetails;
import com.opl.service.loans.domain.TataMotorsReqResDetails;
import com.opl.service.loans.model.pushpull.PushPullRequest;
import com.opl.service.loans.model.pushpull.Result;
import com.opl.service.loans.model.pushpull.TmlRootRequest;
import com.opl.service.loans.repository.common.LoanRepository;
import com.opl.service.loans.repository.common.TataMotorsLoanDetailsRepository;
import com.opl.service.loans.repository.common.TataMotorsReqResDetailsRepository;
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
	private TataMotorsLoanDetailsRepository tataMotorsLoanDetailsRepository;
	
	@Autowired
	private TataMotorsReqResDetailsRepository tataMotorsReqResDetailsRepository ;
	
	@Autowired
	private GstClient gstClient;
		
	@Value("${dfs.user.default.password}")
	private String password;
	
	@Value("${tatmotors.user.default.url}")
	private String tataMotorsUrl;
	
	@Autowired
	private FPAsyncComponent asyncComp;
	
	private Long fpUserType = 2L;
	
	private static final Logger logger = LoggerFactory.getLogger(PushPullApplicationServiceImpl.class.getName());
	private static final String PROFILE_EXISTS = "Invalid request, Profile with same pan already exists!";
	private static final Integer REGULAR = 1;
	private static final Integer COMPOSITE = 2;
	private static final Integer GST_NOT_APPLICABLE = 3;

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
		
			sendNotification(pushPullRequest, userResponse);
			
			sendSMSNotification(pushPullRequest,userResponse);
			
			return userResponse;
		}catch(Exception e) {
			logger.error("Error While saveUsersData: ", e);
			return null;
		}
	}

	/*
	 * private void sentSMS(Map<String, Object> mailParameters ,PushPullRequest
	 * pushPullRequest, UserResponse userResponse) throws NotificationException {
	 * String toMobile = null; if(pushPullRequest.getMobile() != null ) { toMobile =
	 * pushPullRequest.getMobile(); mailParameters.put("url",
	 * "www.psbloansin59minutes.com"); asyncComp.sendSMSNotification(toMobile
	 * ,userResponse.getId().toString() ,mailParameters
	 * ,NotificationAlias.SMS_WELCOME_CAPITAWORLD ,null , 16 ,25 ,
	 * NotificationMasterAlias.EMAIL_FS_SIGNUP_COMPLETE.getMasterId()); } }
	 */

	private void sendNotification(PushPullRequest pushPullRequest, UserResponse userResponse) {
		try {
			if (!CommonUtils.isObjectNullOrEmpty(pushPullRequest)
					&& !CommonUtils.isObjectNullOrEmpty(pushPullRequest.getEmail())
					&& !CommonUtils.isObjectNullOrEmpty(userResponse)
					&& !CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
	
				LinkedHashMap userMap = (LinkedHashMap) userResponse.getData();
					Map<String, Object> mailParameters = new HashMap<>();{
						mailParameters.put("email", pushPullRequest.getEmail());
						mailParameters.put("password", password);
						asyncComp.createNotificationForEmail(pushPullRequest.getEmail(),
								userResponse.getId() != null ? userResponse.getId().toString() : "123", mailParameters,
								NotificationAlias.DFS_WELCOME_EMAIL_FOR_DEALER, null, null, null, null, null,
								NotificationMasterAlias.DFS_WELCOME_EMAIL_FOR_EXISITNG_USER_DEALER.getMasterId());
					}
			logger.info("Mail Sent Successfully ");
			
		}
		}catch(Exception e) {
			logger.error("Error While Sending Mail Notification: ", e);
			
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
		profileRequest.setGstTypeId(pushPullRequest.getGstTypeId() == null ? null : pushPullRequest.getGstTypeId());
		
		com.opl.profile.api.model.CommonResponse profileResponse = profileClient.createProfile(profileRequest);
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
	
	private CommonResponse saveProfileData(Long id, UserResponse userResponse, PushPullRequest pushPullRequest) {
		ProfileRequest profileRequest=new ProfileRequest();
		profileRequest.setId(pushPullRequest.getId() == null ? null : pushPullRequest.getId());
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
		profileRequest.setGstTypeId(pushPullRequest.getGstTypeId() == null ? null : pushPullRequest.getGstTypeId());
		
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
	
	
	private void sendSMSNotification(PushPullRequest pushPullRequest, UserResponse userResponse) {
		try {
			if (!CommonUtils.isObjectNullOrEmpty(pushPullRequest)
					&& !CommonUtils.isObjectNullOrEmpty(pushPullRequest.getEmail())
					&& !CommonUtils.isObjectNullOrEmpty(userResponse)
					&& !CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
	
				LinkedHashMap userMap = (LinkedHashMap) userResponse.getData();
					Map<String, Object> mailParameters = new HashMap<>();{
						mailParameters.put("email", pushPullRequest.getEmail());
						mailParameters.put("password", password);
						asyncComp.sendSMSNotification(
								userResponse.getId() != null ? userResponse.getId().toString() : "123",
								mailParameters,
								NotificationAlias.DFS_WELCOME_EMAIL_FOR_DEALER,
								NotificationConstants.NotificationProperty.DomainValue.MSME.getId(),
								2,
								16L,
								NotificationMasterAlias.DFS_WELCOME_EMAIL_FOR_EXISITNG_USER_DEALER.getMasterId(),
								pushPullRequest.getMobile());
					}
			logger.info("SMS Sent Successfully ");
			
		}
		}catch(Exception e) {
			logger.error("Error While Sending SMS Notification: ", e);
			
		}
	}

	@Override
	public LoansResponse saveTataMotorsLoanDetails(TmlRootRequest tmlRootRequest) {
		logger.info("tmlRootRequest request :{}",tmlRootRequest.toString());
		
		TataMotorsLoanDetails tataMotorsLoanDetails = null;
		if(tmlRootRequest.getResult()!=null) {
		for (Result result : tmlRootRequest.getResult()) {
			
			long mobileNumberExist = tataMotorsLoanDetailsRepository.countByMobileNo(result.getMobileNo());
			
			if(mobileNumberExist > 0) {
				logger.info("Mobile Number is already Exist");
			}else {
				tataMotorsLoanDetails = new TataMotorsLoanDetails();
				BeanUtils.copyProperties(result, tataMotorsLoanDetails);
				tataMotorsLoanDetails.setOffset(tmlRootRequest.getOffset());
				tataMotorsLoanDetails.setIsActive(true);
				tataMotorsLoanDetailsRepository.save(tataMotorsLoanDetails);
				
				tataMotorsLoanDetails = tataMotorsLoanDetailsRepository.findByMobileNo(result.getMobileNo());
				try {
					if (!CommonUtils.isObjectNullOrEmpty(result)) {
						
						Map<String, Object> mailParameters = new HashMap<>();
						{
							mailParameters.put("mobile", result.getMobileNo());
							mailParameters.put("password", password);
							mailParameters.put("url", tataMotorsUrl + CommonUtility.encode(result.getMobileNo()));
							asyncComp.sendSMSNotification(tataMotorsLoanDetails.getId() != null ? tataMotorsLoanDetails.getId().toString() : "123", mailParameters, null, null, null, null,
									NotificationMasterAlias.SMS_TO_TATA_MOTORS_BORROWER_FOR_SIGN_UP_URL.getMasterId(),
									result.getMobileNo());
						}
						logger.info("SMS Sent Successfully ");

					}
				} catch (Exception e) {
					logger.error("Error While Sending SMS Notification: ", e);

				}
			}
		}
	}
		return null;
	}

	@Override
	public LoansResponse saveTataMotorsReqResDetails(TmlRootRequest tmlRootRequest) {
		
		TataMotorsReqResDetails reqResDetails = new TataMotorsReqResDetails();
		reqResDetails.setResponse(tmlRootRequest.getResponseBody());
		reqResDetails.setIsActive(true);
		reqResDetails.setRequest(tmlRootRequest.getRequest().toString());
		reqResDetails.setCreatedDate(new Date());
		reqResDetails.setModifiedDate(new Date());
		tataMotorsReqResDetailsRepository.save(reqResDetails);
		
		return null;
	}

	@Override
	public TataMotorsLoanDetails getDataBYEmail(Long userId) {
		// TODO Auto-generated method stub
		try {
		UserResponse userResponseForName = userClient.getUserBasicDetails(userId);
		UsersRequest uResponse = MultipleJSONObjectHelper.getObjectFromMap((Map<Object, Object>) userResponseForName.getData(), com.opl.mudra.api.user.model.UsersRequest.class);
		TataMotorsLoanDetails details=tataMotorsLoanDetailsRepository.findByMobileNo(uResponse.getMobile());
		return details;
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("error while get user data by email");
			return null;
		}
	}

	@Override
	public LoansResponse createProfileForTmlUsers(UsersRequest usersRequest) {
		
		PushPullRequest pushPullRequest = new PushPullRequest();
		TataMotorsLoanDetails tataMotorsLoanDetails =tataMotorsLoanDetailsRepository.findByMobileNo(usersRequest.getMobile());
		
		UserResponse response = new UserResponse();
		if(tataMotorsLoanDetails != null) {
			List<Object[]> userResponse = tataMotorsLoanDetailsRepository.getUserByMobileNo(usersRequest.getMobile());
			Long id = null;
			for (Object[] object : userResponse) {
				id = Long.parseLong(String.valueOf(object[0]));
				 response.setId(id);
			}
			pushPullRequest.setUsername(tataMotorsLoanDetails.getFirstName());
			pushPullRequest.setPan(tataMotorsLoanDetails.getPanNoCompany());
			com.opl.profile.api.model.CommonResponse profileResponse = createProfileData(id, response, pushPullRequest);
			if(CommonUtils.isObjectNullOrEmpty(profileResponse) || (!CommonUtils.isObjectNullOrEmpty(profileResponse) && CommonUtils.isObjectNullOrEmpty(profileResponse.getData()))) {
				return null;
			}
			
			Long profileId = Long.parseLong(profileResponse.getData().toString());
			
			GSTR1Request gstRequest = new GSTR1Request();
			gstRequest.setUserId(id);
			gstRequest.setPan(pushPullRequest.getPan());
			//gstRequest.setApplicationId(connectResponse.getApplicationId());
			gstRequest.setProfileId(profileId);
			try {
				GstResponse createGstProfileMappingApplication = gstClient.createGstProfileMappingApplication(gstRequest);
				logger.error("get gst data");
				 if (createGstProfileMappingApplication.getStatusCd().equals("914")) {
			          pushPullRequest.setGstTypeId(GST_NOT_APPLICABLE);
			        } else if (createGstProfileMappingApplication.getStatusCd().equals("907")) {
			        	pushPullRequest.setGstTypeId(COMPOSITE);
			        } else {
			        	pushPullRequest.setGstTypeId(REGULAR);
			        }
			} catch (GstException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pushPullRequest.setId(profileId);
			com.opl.profile.api.model.CommonResponse saveProfileResponse = saveProfileData(id, response, pushPullRequest);
			if(CommonUtils.isObjectNullOrEmpty(saveProfileResponse) || (!CommonUtils.isObjectNullOrEmpty(saveProfileResponse) && CommonUtils.isObjectNullOrEmpty(saveProfileResponse.getData()))) {
				return null;
			}
			
			return new LoansResponse(CommonUtils.SUCCESS, HttpStatus.OK.value(), HttpStatus.OK);
		}
		return new LoansResponse("User is not registered TML", HttpStatus.OK.value(), "User is not registered TML");
	}

}
