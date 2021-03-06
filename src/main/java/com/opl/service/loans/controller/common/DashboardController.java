package com.opl.service.loans.controller.common;

import javax.servlet.http.HttpServletRequest;

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

import com.opl.mudra.api.loans.model.DashboardProfileResponse;
import com.opl.mudra.api.loans.model.DataRequest;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.service.loans.service.common.DashboardService;
import com.opl.service.loans.utils.CommonDocumentUtils;

@RestController
@RequestMapping("/fs_dashboard")
public class DashboardController {

	private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@Autowired
	private DashboardService dashboardService;

	@RequestMapping(value = "/profile_detail/{userType}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> profileDetails(@RequestBody DataRequest data, HttpServletRequest request,@PathVariable("userType")Integer userType,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "profileDetails");
			Long userId = null;
			if(CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)){
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			
			if(CommonUtils.isObjectNullOrEmpty(userType) || (userType != CommonUtils.UserType.FUND_SEEKER && userType != CommonUtils.UserType.FUND_PROVIDER)){
				logger.warn("Application Id or UserType must not be Empty");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			
			if (userType == CommonUtils.UserType.FUND_SEEKER && CommonUtils.isObjectNullOrEmpty(data.getId()) ) {
					logger.warn("Application Id must not be Empty");
					return new ResponseEntity<LoansResponse>(
							new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()),
							HttpStatus.OK);
			}
			
			LoansResponse loansResponse = new LoansResponse("Data Found",HttpStatus.OK.value());			
			if(userType == CommonUtils.UserType.FUND_SEEKER){
				//false : Because this method used twice. once from SP also
				DashboardProfileResponse basicProfileInfo = dashboardService.getBasicProfileInfo(data.getId(), userId,false);
				loansResponse.setData(basicProfileInfo);				
			}else{
				UserResponse fpBasicProfileInfo = dashboardService.getFPBasicProfileInfo(userId);
				if(fpBasicProfileInfo!=null && fpBasicProfileInfo.getStatus() == 200){
					loansResponse.setData(fpBasicProfileInfo.getData());					
				}
			}
			CommonDocumentUtils.endHook(logger, "profileDetails");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while saving applicationRequest Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/get_fsfp_count", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFsOrFpCount(@RequestBody DataRequest data, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "getFsOrFpCount");
			
			if(CommonUtils.isObjectNullOrEmpty(data.getValue())){
				logger.warn("UserType must not be Empty");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			data.setId(Long.parseLong(CommonUtils.decode(data.getValue())));
			Integer count = dashboardService.getCount(data.getId().intValue());
			LoansResponse loansResponse = new LoansResponse("Data Found",
					HttpStatus.OK.value());
			loansResponse.setData(count);
			CommonDocumentUtils.endHook(logger, "getFsOrFpCount");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting count of Users==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

}
