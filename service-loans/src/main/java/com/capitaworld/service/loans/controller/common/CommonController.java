package com.capitaworld.service.loans.controller.common;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.common.CGTMSECalcDataResponse;
import com.capitaworld.service.loans.model.common.HunterRequestDataResponse;
import com.capitaworld.service.loans.model.common.LongitudeLatitudeRequest;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.UserLongitudeLatitudeRequest;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UserTypeRequest;
import com.capitaworld.service.users.model.UsersRequest;

@RestController
@RequestMapping("/common")
public class CommonController {

	private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

	@Autowired
	private CorporateApplicantService corporateApplicantService;

	@Autowired
	private UsersClient usersClient;

	@Autowired
	private LoanApplicationService applicationService;

	@RequestMapping(value = "/save_lat_long", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveLatLong(@RequestBody LongitudeLatitudeRequest longLatrequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {

		CommonDocumentUtils.startHook(logger, "save_lat_long");

		if (CommonUtils.isObjectNullOrEmpty(longLatrequest.getLatitude())
				|| CommonUtils.isObjectNullOrEmpty(longLatrequest.getLongitude())
				|| CommonUtils.isObjectNullOrEmpty(longLatrequest.getUserType())) {
			logger.warn("Latitude and longitude and usertype Require to Save Lat Lon Details");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}

		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		Integer userType = ((Integer) request.getAttribute(CommonUtils.USER_TYPE)).intValue();

		Long finalUserId = userId;
		if (CommonDocumentUtils.isThisClientApplication(request)) {
			if (!CommonUtils.isObjectNullOrEmpty(clientId)) {
				finalUserId = clientId;
			}
			longLatrequest.setClientId(clientId);
		}

		if (longLatrequest.getUserType() == CommonUtils.UserType.FUND_SEEKER) {
			try {
				if (CommonUtils.isObjectNullOrEmpty(longLatrequest.getId())) {
					logger.warn("applicationId Require to Save Lat Lon Details");
					return new ResponseEntity<LoansResponse>(
							new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()),
							HttpStatus.OK);
				}
				corporateApplicantService.updateLatLong(longLatrequest, userId);
				logger.warn("successfully save fs long and lat value");
				return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully saved", HttpStatus.OK.value()),
						HttpStatus.OK);
			} catch (Exception e) {
				logger.warn("error while save fs long and lat, applicationId==>" + longLatrequest.getId());
				e.printStackTrace();
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} else {
			if (longLatrequest.getUserType() == CommonUtils.UserType.SERVICE_PROVIDER
					|| longLatrequest.getUserType() == CommonUtils.UserType.FUND_PROVIDER || longLatrequest.getUserType() == CommonUtils.UserType.NETWORK_PARTNER) {
				UserLongitudeLatitudeRequest userRequest = new UserLongitudeLatitudeRequest();
				userRequest.setLongitude(longLatrequest.getLongitude());
				userRequest.setLatitude(longLatrequest.getLatitude());
				userRequest.setUserId(finalUserId);
				userRequest.setUserType(longLatrequest.getUserType());
				try {
					usersClient.saveLongLatValue(userRequest);
					return new ResponseEntity<LoansResponse>(
							new LoansResponse("Successfully save data", HttpStatus.OK.value()), HttpStatus.OK);
				} catch (Exception e) {
					logger.warn("Something went wrong while save long and lat value");
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
							HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
				}

			} else {
				logger.warn("user type invalid when save lat long==>");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
		}

	}

	@RequestMapping(value = "/get_lat_long", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getLatLong(@RequestBody LongitudeLatitudeRequest longLatrequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {

		CommonDocumentUtils.startHook(logger, "get_lat_long");

		if (CommonUtils.isObjectNullOrEmpty(longLatrequest.getUserType())) {
			logger.warn("Usertype Require to Get Lat Lon Details");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}

		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		Integer userType = ((Integer) request.getAttribute(CommonUtils.USER_TYPE)).intValue();

		Long finalUserId = userId;
		if (CommonDocumentUtils.isThisClientApplication(request)) {
			if (!CommonUtils.isObjectNullOrEmpty(clientId)) {
				finalUserId = clientId;
			}
			longLatrequest.setClientId(clientId);
		}

		if (longLatrequest.getUserType() == CommonUtils.UserType.FUND_SEEKER) {
			try {
				if (CommonUtils.isObjectNullOrEmpty(longLatrequest.getId())) {
					logger.warn("applicationId Require to get Lat Lon Details ==>");
					return new ResponseEntity<LoansResponse>(
							new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()),
							HttpStatus.OK);
				}
				LoansResponse response = new LoansResponse("Successfully get data", HttpStatus.OK.value());
				response.setData(
						corporateApplicantService.getLatLonByApplicationAndUserId(longLatrequest.getId(), finalUserId));
				logger.warn("successfully get fs long and lat value");
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			} catch (Exception e) {
				logger.warn("error while get fs long and lat, applicationId==>" + longLatrequest.getId());
				e.printStackTrace();
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} else {
			if (longLatrequest.getUserType() == CommonUtils.UserType.SERVICE_PROVIDER
					|| longLatrequest.getUserType() == CommonUtils.UserType.FUND_PROVIDER || longLatrequest.getUserType() == CommonUtils.UserType.NETWORK_PARTNER) {
				UserLongitudeLatitudeRequest userRequest = new UserLongitudeLatitudeRequest();
				userRequest.setUserId(finalUserId);
				userRequest.setUserType(longLatrequest.getUserType());
				try {
					UserResponse userResponse = usersClient.getLongLatValue(userRequest);
					LoansResponse response = new LoansResponse("Successfully get data", HttpStatus.OK.value());
					response.setData(userResponse.getData());
					logger.warn("successfully get fp and sp long and lat value");
					return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
				} catch (Exception e) {
					logger.warn("Something went wrong while get long and lat value");
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
							HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
				}

			} else {
				logger.warn("user type invalid when get lat long==>");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/user_verification", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponse> userVerification(HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {

		CommonDocumentUtils.startHook(logger, "user_verification");
		Long userId = Long.valueOf(request.getAttribute(CommonUtils.USER_ID).toString());
		Integer userType = (Integer) request.getAttribute(CommonUtils.USER_TYPE);

		if (CommonUtils.isObjectNullOrEmpty(userId) || CommonUtils.isObjectNullOrEmpty(userType)) {
			logger.warn("Invalid Request... UserId or UserType is not found ");
			return new ResponseEntity<UserResponse>(
					new UserResponse("UserId or UserType is not found", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		JSONObject obj = new JSONObject();
		obj.put("userType", userType);

		Integer spUserId = null;
		if (CommonDocumentUtils.isThisClientApplication(request)) {
			// SP LOGIN
			if (!CommonUtils.isObjectNullOrEmpty(clientId)) {
				// MEANS FS, FP VIEW
				userId = clientId;
				try {
					UserResponse response = usersClient.getUserTypeByUserId(new UsersRequest(userId));
					if (response != null && response.getData() != null) {
						UserTypeRequest req = MultipleJSONObjectHelper.getObjectFromMap(
								(LinkedHashMap<String, Object>) response.getData(), UserTypeRequest.class);
						spUserId = req.getId().intValue();
					} else {
						logger.warn("user_verification, Invalid Request... Client Id is not valid");
						return new ResponseEntity<UserResponse>(
								new UserResponse("Client Id is not valid", HttpStatus.BAD_REQUEST.value()),
								HttpStatus.OK);
					}
				} catch (Exception e) {
					logger.warn("user_verification, Invalid Request... Something went wrong");
					e.printStackTrace();
					return new ResponseEntity<UserResponse>(
							new UserResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR.value()),
							HttpStatus.OK);
				}
			} else {
				if (CommonUtils.UserType.SERVICE_PROVIDER == userType){
				spUserId = CommonUtils.UserType.SERVICE_PROVIDER;
				}else if (CommonUtils.UserType.NETWORK_PARTNER == userType){
					spUserId = CommonUtils.UserType.NETWORK_PARTNER;
				}else if (CommonUtils.UserType.FUND_PROVIDER == userType){
					spUserId = CommonUtils.UserType.FUND_PROVIDER;
				}
			}
			userType = spUserId;
		}

		UsersRequest usersRequest = new UsersRequest();
		usersRequest.setId(userId);
		if (userType == CommonUtils.UserType.FUND_SEEKER || userType == CommonUtils.UserType.FUND_PROVIDER) {
			try {
				UserResponse response = usersClient.getLastAccessApplicant(usersRequest);
				obj.put("lastAccessId", response.getId());
				if (!CommonUtils.isObjectNullOrEmpty(response.getId())) {
					obj.put("campaignCode", applicationService.getCampaignCodeByApplicationId(response.getId()));
					obj.put("isProfileAndPrimaryLocked", applicationService.isPrimaryLocked(response.getId(), userId));
					
					
					//SET DDR STATUS ID 
					if (userType == CommonUtils.UserType.FUND_SEEKER) {
						obj.put("ddrStatusId", applicationService.getDDRStatusId(response.getId()));	
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("Error While Get Last access application id");
				return new ResponseEntity<UserResponse>(
						new UserResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} /*
			 * else if(userType == CommonUtils.UserType.FUND_PROVIDER){
			 * List<ProductMasterRequest> productMasterlist =
			 * productMasterService.getList(userId); obj.put("productId",
			 * !CommonUtils.isListNullOrEmpty(productMasterlist) ?
			 * productMasterlist.get(0).getId() : null); }
			 */ else if (userType == CommonUtils.UserType.SERVICE_PROVIDER || userType == CommonUtils.UserType.NETWORK_PARTNER) {
			obj.put("productId", null);
			obj.put("lastAccessAppId", null);
		}
		//CHECK EMAIL VERIFIED OR NOT
		obj.put("emailVerified", false);
		if (userType == CommonUtils.UserType.FUND_SEEKER){
			try {
				logger.info("Call Users Module for check user email verfied or not");
				UserResponse response = usersClient.checkEmailVerified(usersRequest);
				if(!CommonUtils.isObjectNullOrEmpty(response)){
					if(!CommonUtils.isObjectNullOrEmpty(response.getData())){
						obj.put("emailVerified", ((Boolean) response.getData()));
					}
				}
			} catch(Exception e){
				logger.info("Throw exception while check email verified or not");
				e.printStackTrace();
			}
		}
		
		CommonDocumentUtils.endHook(logger, "user_verification");
		return new ResponseEntity<UserResponse>(new UserResponse(obj, "Successfully get data", HttpStatus.OK.value()),
				HttpStatus.OK);
	}

	
	@RequestMapping(value = "/getDataForCGTMSE/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getDataForCGTMSE(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request,@RequestParam(value = "clientId",required = false) Long clientId) {
		// request must not be null
		try {
			
			if (applicationId == null) {
				logger.warn("ID Require to get Recent Profile View Details ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

//			RecentProfileViewDetailResponse response = recentViewService.getLatestRecentViewDetailListByAppId(applicationId,
//					userId);
			
			CGTMSECalcDataResponse response = applicationService.getDataForCGTMSE(applicationId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(response);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Recent Profile View Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	@RequestMapping(value = "/getDataForHunter/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getDataForHunter(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request,@RequestParam(value = "clientId",required = false) Long clientId) {
		// request must not be null
		try {
			logger.info("In getDataForHunter with Application ID : "+applicationId);
			if (applicationId == null) {
				logger.warn("ID Require to getDataForHunter ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

//			RecentProfileViewDetailResponse response = recentViewService.getLatestRecentViewDetailListByAppId(applicationId,
//					userId);
			
			HunterRequestDataResponse response = applicationService.getDataForHunter(applicationId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(response);
			logger.info("End getDataForHunter with Application ID : "+applicationId);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting getDataForHunter==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
