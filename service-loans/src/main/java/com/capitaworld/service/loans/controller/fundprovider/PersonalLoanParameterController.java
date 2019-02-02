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
import com.capitaworld.service.loans.model.retail.PersonalLoanParameterRequest;
import com.capitaworld.service.loans.service.fundprovider.PersonalLoanParameterService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/pl_parameter")
public class PersonalLoanParameterController {

	private static final Logger logger = LoggerFactory.getLogger(PersonalLoanParameterController.class.getName());
	@Autowired
	private PersonalLoanParameterService personalLoanParameterService;

	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody PersonalLoanParameterRequest  personalLoanParameterRequest,HttpServletRequest request) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, "save");
		if (personalLoanParameterRequest == null) {
			logger.warn("personalLoanParameterRequest Object can not be empty ==>", personalLoanParameterRequest);
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}

		if(personalLoanParameterRequest.getId()==null)
		{
			logger.warn("personalLoanParameterRequest id can not be empty ==>", personalLoanParameterRequest);
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}
		
		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		if(userId==null)
		{
			logger.warn("userId  id can not be empty ==>", userId);
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}
		personalLoanParameterRequest.setUserId(userId);
		
		boolean response = personalLoanParameterService.saveOrUpdate(personalLoanParameterRequest,null);
		if (response) {
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);
		} else {
			CommonDocumentUtils.endHook(logger, "save");
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
				logger.warn("ID Require to get personal loan parameter ==>" + id);
				CommonDocumentUtils.endHook(logger, "get");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			PersonalLoanParameterRequest parameterRequest= personalLoanParameterService.getPersonalLoanParameterRequest(id);
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
			logger.error("Error while getting personal Loan Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
}
