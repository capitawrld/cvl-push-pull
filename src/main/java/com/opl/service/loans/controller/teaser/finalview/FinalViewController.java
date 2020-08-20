package com.opl.service.loans.controller.teaser.finalview;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.model.teaser.finalview.CorporateFinalViewResponse;
import com.opl.mudra.api.loans.model.teaser.finalview.TermLoanFinalViewResponse;
import com.opl.mudra.api.loans.model.teaser.finalview.UnsecuredLoanFinalViewResponse;
import com.opl.mudra.api.loans.model.teaser.finalview.WorkingCapitalFinalViewResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UserTypeRequest;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.users.UsersClient;
import com.opl.service.loans.service.teaser.finalview.CorporateFinalViewService;
import com.opl.service.loans.service.teaser.finalview.TermLoanFinalViewService;
import com.opl.service.loans.service.teaser.finalview.UnsecuredLoanFinalViewService;
import com.opl.service.loans.service.teaser.finalview.WorkingCapitalFinalService;
import com.opl.service.loans.utils.CommonDocumentUtils;

@RestController
@RequestMapping("/FinalView")
public class FinalViewController {

private static final Logger logger = LoggerFactory.getLogger(FinalViewController.class);
	
	@Autowired
	private WorkingCapitalFinalService wcFinalService;

	@Autowired
	private TermLoanFinalViewService tlFinalViewService;
	
	@Autowired
	private UnsecuredLoanFinalViewService unsecuredLoanFinalViewService;

	@Autowired
	private UsersClient usersClient;
	
	@Autowired
	private CorporateFinalViewService corporateFinalViewService;
	
	
	private static final String WARN_MSG_USER_VERIFICATION_INVALID_REQUEST_CLIENT_ID_IS_NOT_VALID = "user_verification, Invalid Request... Client Id is not valid";
	private static final String ERROR_MSG_USER_VERIFICATION_INVALID_REQUEST_SOMETHING_WENT_WRONG = "user_verification, Invalid Request... Something went wrong : ";
	private static final String MSG_USER_ID = "userId : ";
	private static final String MSG_USER_TYPE = " userType : ";

	@GetMapping(value = "/WorkingCapital/{toApplicationId}")
	public @ResponseBody ResponseEntity<LoansResponse> finalViewWrokingCapital(@PathVariable(value = "toApplicationId") Long toApplicationId,@RequestParam(value = "clientId", required = false) Long clientId,HttpServletRequest request) {
		LoansResponse loansResponse = new LoansResponse();
		//get user id from http servlet request
		Long userId = null;
		Integer userType = null;
		
		if (CommonDocumentUtils.isThisClientApplication(request)) {
			if(!CommonUtils.isObjectNullOrEmpty(clientId)){
				//MEANS FS, FP VIEW
				userId = clientId;
				try {
					UserResponse response = usersClient.getUserTypeByUserId(new UsersRequest(userId));
					if(response != null && response.getData() != null){
						UserTypeRequest req = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>) response.getData(), UserTypeRequest.class);
						userType = req.getId().intValue();
					} else {
						logger.warn(WARN_MSG_USER_VERIFICATION_INVALID_REQUEST_CLIENT_ID_IS_NOT_VALID);
						return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.CLIENT_ID_IS_NOT_VALID,
								HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
					}	
				} catch(Exception e) {
					logger.error(ERROR_MSG_USER_VERIFICATION_INVALID_REQUEST_SOMETHING_WENT_WRONG,e);
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
							HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
				}
			} else {if(CommonUtils.UserType.SERVICE_PROVIDER == userType){
				userType = CommonUtils.UserType.SERVICE_PROVIDER;
				}else if(CommonUtils.UserType.NETWORK_PARTNER == userType){
					userType = CommonUtils.UserType.NETWORK_PARTNER;
					}
			}
			
		} else {
			userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			userType = (Integer) request.getAttribute(CommonUtils.USER_TYPE);
		}

			logger.debug(MSG_USER_ID+userId+MSG_USER_TYPE+userType);

		if(CommonUtils.isObjectNullOrEmpty(toApplicationId)){
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, toApplicationId);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}else {
			WorkingCapitalFinalViewResponse workingCapitalFinalViewResponse = null;
			try {
				workingCapitalFinalViewResponse = wcFinalService.getWorkingCapitalFinalViewDetails(toApplicationId);
				if(!CommonUtils.isObjectNullOrEmpty(workingCapitalFinalViewResponse)){
					loansResponse.setData(workingCapitalFinalViewResponse);
					loansResponse.setMessage("Working Capital Final Details");
					loansResponse.setStatus(HttpStatus.OK.value());
				}else{
					loansResponse.setMessage("No data found for working capital final view");
					loansResponse.setStatus(HttpStatus.OK.value());
				}
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} catch (Exception e) {
				loansResponse.setMessage(CommonUtils.SOMETHING_WENT_WRONG+e.getMessage());
				loansResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
		}
	}

	@GetMapping(value = "/TermLoan/{toApplicationId}")
	public @ResponseBody ResponseEntity<LoansResponse> finalViewTermLoan(@PathVariable(value = "toApplicationId") Long toApplicationId,@RequestParam(value = "clientId", required = false) Long clientId,HttpServletRequest request) {
		LoansResponse loansResponse = new LoansResponse();
		//get user id from http servlet request
		Long userId = null;
		Integer userType = null;
		
		if (CommonDocumentUtils.isThisClientApplication(request)) {
			if(!CommonUtils.isObjectNullOrEmpty(clientId)){
				//MEANS FS, FP VIEW
				userId = clientId;
				try {
					UserResponse response = usersClient.getUserTypeByUserId(new UsersRequest(userId));
					if(response != null && response.getData() != null){
						UserTypeRequest req = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>) response.getData(), UserTypeRequest.class);
						userType = req.getId().intValue();
					} else {
						logger.warn(WARN_MSG_USER_VERIFICATION_INVALID_REQUEST_CLIENT_ID_IS_NOT_VALID);
						return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.CLIENT_ID_IS_NOT_VALID,
								HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
					}	
				} catch(Exception e) {
					logger.error(ERROR_MSG_USER_VERIFICATION_INVALID_REQUEST_SOMETHING_WENT_WRONG,e);
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
							HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
				}
			} else {if(CommonUtils.UserType.SERVICE_PROVIDER == userType){
				userType = CommonUtils.UserType.SERVICE_PROVIDER;
				}else if(CommonUtils.UserType.NETWORK_PARTNER == userType){
					userType = CommonUtils.UserType.NETWORK_PARTNER;
					}
			}
			
		} else {
			userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			userType = (Integer) request.getAttribute(CommonUtils.USER_TYPE);
		}

			logger.debug(MSG_USER_ID+userId+MSG_USER_TYPE+userType);

		if(CommonUtils.isObjectNullOrEmpty(toApplicationId)){
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, toApplicationId);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}else {
			TermLoanFinalViewResponse termLoanFinalViewResponse = null;
			try {
				termLoanFinalViewResponse = tlFinalViewService.getTermLoanFinalViewDetails(toApplicationId);
				if(!CommonUtils.isObjectNullOrEmpty(termLoanFinalViewResponse)){
					loansResponse.setData(termLoanFinalViewResponse);
					loansResponse.setMessage("Term Loan Final Details");
					loansResponse.setStatus(HttpStatus.OK.value());
				}else{
					loansResponse.setMessage("No data found for Term Loan final view");
					loansResponse.setStatus(HttpStatus.OK.value());
				}
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} catch (Exception e) {
				loansResponse.setMessage(CommonUtils.SOMETHING_WENT_WRONG+e.getMessage());
				loansResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
		}
	}
	
	@GetMapping(value = "/UnsecuredLoan/{toApplicationId}")
	public @ResponseBody ResponseEntity<LoansResponse> finalViewUnsecuredLoan(@PathVariable(value = "toApplicationId") Long toApplicationId,@RequestParam(value = "clientId", required = false) Long clientId,HttpServletRequest request) {
		LoansResponse loansResponse = new LoansResponse();
		//get user id from http servlet request
		Long userId = null;
		Integer userType = null;
		
		if (CommonDocumentUtils.isThisClientApplication(request)) {
			if(!CommonUtils.isObjectNullOrEmpty(clientId)){
				//MEANS FS, FP VIEW
				userId = clientId;
				try {
					UserResponse response = usersClient.getUserTypeByUserId(new UsersRequest(userId));
					if(response != null && response.getData() != null){
						UserTypeRequest req = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>) response.getData(), UserTypeRequest.class);
						userType = req.getId().intValue();
					} else {
						logger.warn(WARN_MSG_USER_VERIFICATION_INVALID_REQUEST_CLIENT_ID_IS_NOT_VALID);
						return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.CLIENT_ID_IS_NOT_VALID,
								HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
					}	
				} catch(Exception e) {
					logger.error(ERROR_MSG_USER_VERIFICATION_INVALID_REQUEST_SOMETHING_WENT_WRONG,e);
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
							HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
				}
			} else {if(CommonUtils.UserType.SERVICE_PROVIDER == userType){
				userType = CommonUtils.UserType.SERVICE_PROVIDER;
				}else if(CommonUtils.UserType.NETWORK_PARTNER == userType){
					userType = CommonUtils.UserType.NETWORK_PARTNER;
					}
			}
			
		} else {
			userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			userType = (Integer) request.getAttribute(CommonUtils.USER_TYPE);
		}

			logger.debug(MSG_USER_ID+userId+MSG_USER_TYPE+userType);

		if(CommonUtils.isObjectNullOrEmpty(toApplicationId)){
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, toApplicationId);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}else {
			UnsecuredLoanFinalViewResponse unsecuredLoanFinalViewResponse = null;
			try {
				unsecuredLoanFinalViewResponse = unsecuredLoanFinalViewService.getUnsecuredLoanFinalViewDetails(toApplicationId,userType,userId);
				if(!CommonUtils.isObjectNullOrEmpty(unsecuredLoanFinalViewResponse)){
					loansResponse.setData(unsecuredLoanFinalViewResponse);
					loansResponse.setMessage("Unsecured Loan Final Details");
					loansResponse.setStatus(HttpStatus.OK.value());
				}else{
					loansResponse.setMessage("No data found for Unsecured Loan final view");
					loansResponse.setStatus(HttpStatus.OK.value());
				}
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} catch (Exception e) {
				loansResponse.setMessage(CommonUtils.SOMETHING_WENT_WRONG+e.getMessage());
				loansResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
		}
	}
	
	// COMMON FINAL CORPORATE TEASER VIEW
	@GetMapping(value = "/Corporate/{toApplicationId}/{proposalId}")   // @GetMapping(value = "/Corporate/{toApplicationId}")
	public @ResponseBody ResponseEntity<LoansResponse> primaryViewOfCorporateByProposal(
			@PathVariable(value = "toApplicationId") Long toApplicationId,@PathVariable(value = "proposalId") Long proposalId,
			@RequestParam(value = "clientId", required = false) Long clientId, HttpServletRequest request) {
		logger.info("IN FINAL CORPORATE TEASER VIEW======>" + proposalId);
		LoansResponse loansResponse = new LoansResponse();
		// GET USERID
		Long userId = null;
		Integer userType = null;

		if (CommonDocumentUtils.isThisClientApplication(request)) {
			if (!CommonUtils.isObjectNullOrEmpty(clientId)) {
				// FOR FS,FP UNDER SP OR NP
				userId = clientId;
				try {
					UserResponse response = usersClient.getUserTypeByUserId(new UsersRequest(userId));
					if (response != null && response.getData() != null) {
						UserTypeRequest req = MultipleJSONObjectHelper.getObjectFromMap(
								(LinkedHashMap<String, Object>) response.getData(), UserTypeRequest.class);
						userType = req.getId().intValue();
					} else {
						logger.warn(WARN_MSG_USER_VERIFICATION_INVALID_REQUEST_CLIENT_ID_IS_NOT_VALID);
						return new ResponseEntity<LoansResponse>(
								new LoansResponse(CommonUtils.CLIENT_ID_IS_NOT_VALID, HttpStatus.BAD_REQUEST.value()),
								HttpStatus.OK);
					}
				} catch (Exception e) {
					logger.error(ERROR_MSG_USER_VERIFICATION_INVALID_REQUEST_SOMETHING_WENT_WRONG,e);
					return new ResponseEntity<LoansResponse>(
							new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
							HttpStatus.OK);
				}
			} else {
				if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_TYPE))) {
					userType = (Integer) request.getAttribute(CommonUtils.USER_TYPE);
					}
					if(!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
						userId = ((Long) request.getAttribute(CommonUtils.USER_ID));		}
				/*
				 * if(CommonUtils.UserType.SERVICE_PROVIDER == userType){ userType =
				 * CommonUtils.UserType.SERVICE_PROVIDER; }else
				 * if(CommonUtils.UserType.NETWORK_PARTNER == userType){ userType =
				 * CommonUtils.UserType.NETWORK_PARTNER; }
				 */
			}
		} else {
			userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			userType = (Integer) request.getAttribute(CommonUtils.USER_TYPE);
			}

				logger.debug(MSG_USER_ID+userId+MSG_USER_TYPE+userType);

		if (CommonUtils.isObjectNullOrEmpty(proposalId)) {
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, proposalId);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		} else {
			CorporateFinalViewResponse corporateFinalViewResponse = null;
			try {
				logger.info("Request users details:- toApplicationId,userType,userId is" + proposalId + userType
						+ userId);
				corporateFinalViewResponse = corporateFinalViewService.getCorporateFinalViewDetails(toApplicationId,proposalId,
						userType, userId);
				if (!CommonUtils.isObjectNullOrEmpty(corporateFinalViewResponse)) {
					logger.info("response is" + corporateFinalViewResponse.toString());
					loansResponse.setData(corporateFinalViewResponse);
					loansResponse.setMessage("Corporate Primary Details");
					loansResponse.setStatus(HttpStatus.OK.value());
				} else {
					loansResponse.setMessage("No data found for Corporate final view");
					loansResponse.setStatus(HttpStatus.OK.value());
				}
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} catch (Exception e) {
				loansResponse.setData(corporateFinalViewResponse);
				loansResponse.setMessage(CommonUtils.SOMETHING_WENT_WRONG);
				loansResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
		}
	}
}
