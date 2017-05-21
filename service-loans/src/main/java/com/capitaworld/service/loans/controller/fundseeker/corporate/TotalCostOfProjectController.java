package com.capitaworld.service.loans.controller.fundseeker.corporate;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.corporate.TotalCostOfProjectRequest;
import com.capitaworld.service.loans.service.fundseeker.corporate.TotalCostOfProjectService;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/total_cost_of_project")
public class TotalCostOfProjectController {
	
	private static final Logger logger = LoggerFactory.getLogger(TotalCostOfProjectController.class);

	@Autowired
	private TotalCostOfProjectService totalCostOfProjectService; 

	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public String getPing() {
		logger.info("Ping success");
		return "Ping Succeed";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody FrameRequest frameRequest, HttpServletRequest request) {
		// request must not be null
		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

		if (frameRequest == null) {
			logger.warn("frameRequest must not be empty ==>" + frameRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		// application id and user id must not be null
		if (frameRequest.getApplicationId() == null) {
			logger.warn("application id and user id must not be null ==>" + frameRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
		try {
			frameRequest.setUserId(userId);
			totalCostOfProjectService.saveOrUpdate(frameRequest);
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving Total Cost Of Project Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/getList/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getList(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request) {
		// request must not be null
		try {
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if (applicationId == null) {
				logger.warn("ID Require to get Total Cost Of Project ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			List<TotalCostOfProjectRequest> response = totalCostOfProjectService.getCostOfProjectDetailList(applicationId, userId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(response);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Achievement Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
