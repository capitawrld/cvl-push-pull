package com.capitaworld.service.loans.controller.sidbi;

import java.util.List;

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

import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.corporate.TotalCostOfProjectRequest;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.service.sidbi.ProjectCostDetailService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/project_cost")
public class ProjectCostDetailController {

	private static final Logger logger = LoggerFactory.getLogger(ProjectCostDetailController.class);

	@Autowired
	private ProjectCostDetailService projectCostDetailService;
	
	@Autowired
	private LoanApplicationService loanApplicationService;

	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody FrameRequest frameRequest, HttpServletRequest request,@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, "save");
		
		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

		if (frameRequest == null) {
			logger.warn("frameRequest must not be empty ==>" + frameRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			frameRequest.setUserId(userId);
			if(CommonDocumentUtils.isThisClientApplication(request)){
				frameRequest.setClientId(clientId);
			}
			
			projectCostDetailService.saveOrUpdate(frameRequest);
			CommonDocumentUtils.endHook(logger, "save");
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
			HttpServletRequest request,@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getList");
			Long userId = null;
			   if(CommonDocumentUtils.isThisClientApplication(request)){
			    userId = clientId;
			   } else {
			    userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			   }
			if (applicationId == null) {
				logger.warn("ID Require to get Total Cost Of Project ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			List<TotalCostOfProjectRequest> response = projectCostDetailService.getCostOfProjectDetailList(applicationId, userId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			JSONObject result = loanApplicationService.getCurrencyAndDenomination(applicationId,userId);
			String data = result.get("currency").toString();
			data = data.concat(" In "+ result.get("denomination").toString());
			loansResponse.setData(data);
			loansResponse.setListData(response);
			CommonDocumentUtils.endHook(logger, "getList");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Achievement Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
