package com.opl.service.loans.controller.fundseeker.corporate;

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

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.model.corporate.FinalTermLoanRequest;
import com.opl.mudra.api.loans.model.corporate.PrimaryTermLoanRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.service.fundseeker.corporate.FinalTermLoanService;
import com.opl.service.loans.service.fundseeker.corporate.PrimaryTermLoanService;
import com.opl.service.loans.utils.CommonDocumentUtils;

@RestController
@RequestMapping("/term_loan")
public class TermLoanController {

	private static final Logger logger = LoggerFactory.getLogger(TermLoanController.class);

	@Autowired
	private FinalTermLoanService finalTLService;

	@Autowired
	private PrimaryTermLoanService primaryTLService;

	@RequestMapping(value = "${final}/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody FinalTermLoanRequest termLoanRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId)
			throws LoansException {
		try {
			CommonDocumentUtils.startHook(logger, "save");
			// request must not be null
			
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

			if (userId == null) {
				logger.warn("userId can not be empty ==>" + termLoanRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (termLoanRequest.getApplicationId() == null) {
				logger.warn("Application ID can not be empty ==>" + termLoanRequest.getId());
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Application ID can not be empty.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				termLoanRequest.setClientId(clientId);
			}
			finalTLService.saveOrUpdate(termLoanRequest, userId);
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while saving final information of Term Loan : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "${final}/get/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFinal(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			try {
				CommonDocumentUtils.startHook(logger, "getFinal");
				Long userId;
				if (CommonDocumentUtils.isThisClientApplication(request)) {
					userId = clientId;
				} else {
					userId = (Long) request.getAttribute(CommonUtils.USER_ID);
				}
				if (userId == null || applicationId == null) {
					logger.warn("ID and ApplicationId Require to get Final Term Loan Details. ID==>" + userId
							+ " and ApplicationId==>" + applicationId);
					return new ResponseEntity<LoansResponse>(
							new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
				}
				FinalTermLoanRequest response = finalTLService.get(userId, applicationId);
				LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
				loansResponse.setData(response);
				CommonDocumentUtils.endHook(logger, "getFinal");
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} catch (Exception e) {
				logger.error("Error while getting Final Term Loan Details==>", e);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while getting  final information of Term Loan : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "${primary}/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> savePrimary(@RequestBody PrimaryTermLoanRequest termLoanRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId)
			throws LoansException {
		try {
			CommonDocumentUtils.startHook(logger, "savePrimary");
			// request must not be null
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				termLoanRequest.setClientId(clientId);
			}

			if (userId == null) {
				logger.warn("userId can not be empty ==>" + termLoanRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (termLoanRequest.getId() == null) {
				logger.warn("ID must not be empty ==>" + termLoanRequest.getId());
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("ID must not be empty.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			primaryTLService.saveOrUpdate(termLoanRequest, userId);
			CommonDocumentUtils.endHook(logger, "savePrimary");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving Primary Working Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "${primary}/get/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPrimary(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, CommonUtils.GET_PRIMARY);
			Long userId;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (applicationId == null || userId == null) {
				logger.warn("ID and User Id Require to get Primary Working Details ==>" + applicationId + "User ID ==>"
						+ userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			PrimaryTermLoanRequest response = primaryTLService.get(applicationId, userId);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(response);
			CommonDocumentUtils.endHook(logger, CommonUtils.GET_PRIMARY);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting Primary Term Loan Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	
	@RequestMapping(value = "${primary}/get_client/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPrimaryClient(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {	
			
			if (applicationId == null) {
			logger.warn(
					"Application ID Require to get Primary TermLoan Details ==>" + applicationId);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
			PrimaryTermLoanRequest response = primaryTLService.get(applicationId, null);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(response);
			CommonDocumentUtils.endHook(logger, CommonUtils.GET_PRIMARY);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting Primary Term Loan Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	
	

}
