package com.capitaworld.service.loans.controller.fundprovider;

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

import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.retail.LapParameterRequest;
import com.capitaworld.service.loans.service.fundprovider.LapLoanParameterService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/lap_parameter")
public class LapLoanParameterController {

	private static final Logger logger = LoggerFactory.getLogger(LapLoanParameterController.class.getName());

	private static final String LITERAL_START = "start";

	@Autowired
	private LapLoanParameterService lapLoanParameterService;

	/*@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public String getPing() {
		logger.info("Ping success");
		return "Ping Succeed";
	}*/

	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody LapParameterRequest  lapParameterRequest,HttpServletRequest request) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, LITERAL_START);
		if (lapParameterRequest == null) {
			logger.warn("lapParameterRequest Object can not be empty ==>", lapParameterRequest);
			CommonDocumentUtils.endHook(logger, LITERAL_START);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}

		if(lapParameterRequest.getId()==null)
		{
			logger.warn("lapParameterRequest id can not be empty ==>", lapParameterRequest);
			CommonDocumentUtils.endHook(logger, LITERAL_START);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}
		
		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		if(userId==null)
		{
			logger.warn("userId  id can not be empty ==>", userId);
			CommonDocumentUtils.endHook(logger, LITERAL_START);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}
		lapParameterRequest.setUserId(userId);
		
		boolean response = lapLoanParameterService.saveOrUpdate(lapParameterRequest);
		if (response) {
			CommonDocumentUtils.endHook(logger, LITERAL_START);
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);
		} else {
			CommonDocumentUtils.endHook(logger, LITERAL_START);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> get(@PathVariable("id") Long id) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, "get");
		try {
			if (id == null) {
				logger.warn("ID Require to get lap loan parameter ==>" + id);
				CommonDocumentUtils.endHook(logger, "get");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			LapParameterRequest parameterRequest= lapLoanParameterService.getLapParameterRequest(id);
			if (parameterRequest != null) {
				LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
				loansResponse.setData(parameterRequest);
				CommonDocumentUtils.endHook(logger, "get");
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "get");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while getting lap Loan Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

}
