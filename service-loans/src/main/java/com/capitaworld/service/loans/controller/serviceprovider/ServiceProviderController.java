package com.capitaworld.service.loans.controller.serviceprovider;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.model.SpClientListing;
import com.capitaworld.service.loans.model.SpSysNotifyResponse;
import com.capitaworld.service.loans.model.common.NotificationPageRequest;
import com.capitaworld.service.loans.service.serviceprovider.ServiceProviderFlowService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;


@RestController
@RequestMapping("/sp")
public class ServiceProviderController {

	private static final Logger logger = LoggerFactory.getLogger(ServiceProviderController.class.getName());

	private static final String SERIVCE_PROVIDERS_CLIENT_LIST_MSG = "Serivce provider's client list";
	private static final String SOMETHING_WENT_WRONG_WHILE_FETCHING_SP_CLIENT_COUNT_MSG = "Something went wrong while fetching SP client count..!";

	@Autowired
	ServiceProviderFlowService serviceProviderFlowService;
	
	@RequestMapping(value = "/client/list",method = RequestMethod.POST,consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponse> clientList(@RequestBody UsersRequest usersRequest,HttpServletRequest request){
		if(CommonUtils.isObjectNullOrEmpty(usersRequest) || CommonUtils.isObjectNullOrEmpty(usersRequest.getUserType())){
			return new ResponseEntity<UserResponse>(

					new UserResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}
		try {
			List<SpClientListing> clientList = serviceProviderFlowService.spClientList(Integer.parseInt(usersRequest.getPageIndex().toString()),Integer.parseInt(usersRequest.getSize().toString()),Long.valueOf(request.getAttribute(CommonUtils.USER_ID).toString()), usersRequest.getUserType().getCode());

			if(clientList != null){
				logger.info(SERIVCE_PROVIDERS_CLIENT_LIST_MSG);
				return new ResponseEntity<UserResponse>(
						new UserResponse(clientList,SERIVCE_PROVIDERS_CLIENT_LIST_MSG, HttpStatus.OK.value()),
						HttpStatus.OK);
			}else{
				logger.info(CommonUtils.SOMETHING_WENT_WRONG);
				return new ResponseEntity<UserResponse>(
						new UserResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(CommonUtils.SOMETHING_WENT_WRONG,e);
			return new ResponseEntity<UserResponse>(
					new UserResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		
	}
	

	@RequestMapping(value = "/client/count",method = RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponse> spClientCount(HttpServletRequest request){
		if(CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID).toString())){
			return new ResponseEntity<UserResponse>(
					new UserResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		try {
			JSONObject spClientCount = serviceProviderFlowService.spClientCount(Long.valueOf(request.getAttribute(CommonUtils.USER_ID).toString()));
			if(!spClientCount.isEmpty()){
				logger.info("Serivce provider's client count");
				return new ResponseEntity<UserResponse>(
						new UserResponse(spClientCount,"Serivce provider's client count", HttpStatus.OK.value()),
						HttpStatus.OK);
			}else{
				logger.info(SOMETHING_WENT_WRONG_WHILE_FETCHING_SP_CLIENT_COUNT_MSG);
				return new ResponseEntity<UserResponse>(
						new UserResponse("Something went wrong while fetching SP client count..!-->", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);	
			}
		} catch (Exception e) {
			logger.error(SOMETHING_WENT_WRONG_WHILE_FETCHING_SP_CLIENT_COUNT_MSG,e);
			return new ResponseEntity<UserResponse>(
					new UserResponse(SOMETHING_WENT_WRONG_WHILE_FETCHING_SP_CLIENT_COUNT_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		
	}
	
	@RequestMapping(value = "/client/notifications",method = RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponse> spClientNotifications(HttpServletRequest request){
		if(CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID).toString())){
			return new ResponseEntity<UserResponse>(
					new UserResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		try {
			List<SpSysNotifyResponse> clientotification = serviceProviderFlowService.spClientNotifications(Long.valueOf(request.getAttribute(CommonUtils.USER_ID).toString()));

			if(clientotification != null){
				logger.info(SERIVCE_PROVIDERS_CLIENT_LIST_MSG);
				return new ResponseEntity<UserResponse>(
						new UserResponse(clientotification,SERIVCE_PROVIDERS_CLIENT_LIST_MSG, HttpStatus.OK.value()),
						HttpStatus.OK);
			}else{
				logger.info(CommonUtils.SOMETHING_WENT_WRONG);
				return new ResponseEntity<UserResponse>(
						new UserResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(CommonUtils.SOMETHING_WENT_WRONG,e);
			return new ResponseEntity<UserResponse>(
					new UserResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		
	}
	
	
	@RequestMapping(value = "/client/allNotifications",method = RequestMethod.POST,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponse> spClientAllNotifications(HttpServletRequest request, @RequestBody NotificationPageRequest notificationPageRequest){
		if(CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID).toString())){
			return new ResponseEntity<UserResponse>(
					new UserResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		try {
			List<SpSysNotifyResponse> clientotification = serviceProviderFlowService.spClientAllNotifications(Long.valueOf(request.getAttribute(CommonUtils.USER_ID).toString()),notificationPageRequest);

			if(clientotification != null){
				logger.info(SERIVCE_PROVIDERS_CLIENT_LIST_MSG);
				return new ResponseEntity<UserResponse>(
						new UserResponse(clientotification,SERIVCE_PROVIDERS_CLIENT_LIST_MSG, HttpStatus.OK.value()),
						HttpStatus.OK);
			}else{
				logger.info(CommonUtils.SOMETHING_WENT_WRONG);
				return new ResponseEntity<UserResponse>(
						new UserResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(CommonUtils.SOMETHING_WENT_WRONG,e);
			return new ResponseEntity<UserResponse>(
					new UserResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		
	}
	
	
	@RequestMapping(value = "/client/allNotificationsCount",method = RequestMethod.POST,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponse> spClientAllNotificationsCount(HttpServletRequest request, @RequestBody NotificationPageRequest notificationPageRequest){
		if(CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID).toString())){
			return new ResponseEntity<UserResponse>(
					new UserResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		try {
			Long clientotification = serviceProviderFlowService.spClientAllNotificationsCount(Long.valueOf(request.getAttribute(CommonUtils.USER_ID).toString()),notificationPageRequest);

			if(clientotification != null){
				logger.info(SERIVCE_PROVIDERS_CLIENT_LIST_MSG);
				return new ResponseEntity<UserResponse>(
						new UserResponse(null,clientotification,SERIVCE_PROVIDERS_CLIENT_LIST_MSG, HttpStatus.OK.value()),
						HttpStatus.OK);
			}else{
				logger.info(CommonUtils.SOMETHING_WENT_WRONG);
				return new ResponseEntity<UserResponse>(
						new UserResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(CommonUtils.SOMETHING_WENT_WRONG,e);
			return new ResponseEntity<UserResponse>(
					new UserResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		
	}
	
	}
