
package com.opl.service.loans.controller.pushpull;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.model.pushpull.PushPullRequest;
import com.opl.service.loans.repository.common.LoanRepository;
import com.opl.service.loans.service.pushpull.PushPullApplicationService;

@RestController
@RequestMapping("/loan_application")
public class PushPullCvlController {
	
	private static final int RENEWALLOANTYPE = 2;

	private static final int NEWLOANTYPE = 1;

	private static final Logger logger = LoggerFactory.getLogger(PushPullCvlController.class);

	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private PushPullApplicationService pushPullApplicationService;
	
	@PostMapping(value = "/pushpull", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPushPullData(@RequestBody PushPullRequest pushPullRequest, HttpServletRequest request) {
		try {
			if (!CommonUtils.isObjectNullOrEmpty(pushPullRequest)) {
				Long userid = loanRepository.getUserTypeByEmail(pushPullRequest.getEmail());
				if (!CommonUtils.isObjectNullOrEmpty(userid)) {
					return new ResponseEntity<LoansResponse>(
							new LoansResponse("Data already enrolled in system", HttpStatus.CONFLICT.value(), pushPullRequest), HttpStatus.CONFLICT);
				}
				
				return new ResponseEntity<>(new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			
			LoansResponse loansResponse = new LoansResponse();
			loansResponse  = pushPullApplicationService.saveOrUpdate(pushPullRequest);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);
		}catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
