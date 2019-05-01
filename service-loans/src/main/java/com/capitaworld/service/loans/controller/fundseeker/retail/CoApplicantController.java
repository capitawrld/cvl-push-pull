package com.capitaworld.service.loans.controller.fundseeker.retail;

import javax.servlet.http.HttpServletRequest;

import com.capitaworld.service.loans.model.retail.FinalCommonRetailRequestOld;
import com.capitaworld.service.loans.model.retail.HLOneformRequest;
import com.capitaworld.service.loans.model.retail.HLOnefromResponse;
import com.capitaworld.service.loans.model.retail.RetailApplicantRequest;

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
import com.capitaworld.service.loans.model.retail.CoApplicantRequest;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/co_applicant")
public class CoApplicantController {

	private static final Logger logger = LoggerFactory.getLogger(CoApplicantController.class.getName());

	private static final String AND_ID_MSG = " and ID ==>";

	@Autowired
	private CoApplicantService coApplicantService;

	/*@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public String getPing() {
		logger.info("Ping success");
		return "Ping Succeed";
	}*/

	// Primary Portion
	@RequestMapping(value = "${profile}/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody CoApplicantRequest applicantRequest,
			HttpServletRequest request,@RequestParam(value = "clientId",required = false) Long clientId) {
		// request must not be null
		try {
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

			if (applicantRequest == null) {
				logger.warn("applicantRequest can not be empty ==>", applicantRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (applicantRequest.getApplicationId() == null || applicantRequest.getId() == null) {
				logger.warn("Application Id and ID can not be empty ==>", applicantRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);

			}
			if(CommonDocumentUtils.isThisClientApplication(request)){
				applicantRequest.setClientId(clientId);
			}
			coApplicantService.save(applicantRequest, applicantRequest.getApplicationId(), userId);
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "${profile}/get/{applicationId}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> get(@PathVariable("id") Long id,
			@PathVariable("applicationId") Long applicationId, HttpServletRequest request,@RequestParam(value = "clientId",required = false) Long clientId) {
		// request must not be null
		try {
			Long userId = null;
			if(CommonDocumentUtils.isThisClientApplication(request)){
				userId = clientId;
			}else{
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (applicationId == null || id == null) {
				logger.warn("ID and Application ID Require to get CoApplicant Profile Details. Application ID==>"
						+ applicationId + AND_ID_MSG + id);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			CoApplicantRequest response = coApplicantService.get(userId, applicationId, id);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(response);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Co Applicant Profile Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Final Portion
	@RequestMapping(value = "${final}/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveFinal(@RequestBody FinalCommonRetailRequestOld applicantRequest,
			HttpServletRequest request,@RequestParam(value = "clientId",required = false) Long clientId) {
		// request must not be null
		try {
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

			if (applicantRequest == null) {
				logger.warn("applicantRequest can not be empty ==>", applicantRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (applicantRequest.getApplicationId() == null || applicantRequest.getId() == null) {
				logger.warn("ID and Application Id can not be empty Application ID==>"
						+ applicantRequest.getApplicationId() + AND_ID_MSG + applicantRequest.getId());
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);

			}
			
			if(CommonDocumentUtils.isThisClientApplication(request)){
				applicantRequest.setClientId(clientId);
			}
			coApplicantService.saveFinal(applicantRequest, userId);
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "${final}/get/{applicationId}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFinal(@PathVariable("id") Long id,
			@PathVariable("applicationId") Long applicationId, HttpServletRequest request,@RequestParam(value = "clientId",required = false) Long clientId) {
		// request must not be null
		try {
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if(CommonDocumentUtils.isThisClientApplication(request)){
				userId = clientId;
			}else{
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (applicationId == null || id == null) {
				logger.warn("Application ID and IDRequire to get CoApplicant Profile Details. Application ID==>"
						+ applicationId + AND_ID_MSG + id);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			FinalCommonRetailRequestOld response = coApplicantService.getFinal(userId, applicationId, id);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(response);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Co Applicant Profile Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "${profile}/saveITRRes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveITRRes(@RequestBody RetailApplicantRequest applicantRequest) {
		logger.info("Enter in Save Profile Retail Applicant Details From ITR Repsonse");
		try {
			if (applicantRequest.getApplicationId() == null) {
				logger.warn("Application Id can not be empty ==>" + applicantRequest.getApplicationId());
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			coApplicantService.saveITRResponse(applicantRequest);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	

}
