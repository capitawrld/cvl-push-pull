
package com.opl.service.loans.controller.pushpull;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.opl.mudra.api.user.model.FpProfileBasicDetailRequest;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.users.UsersClient;
import com.opl.mudra.api.loans.model.AdminPanelLoanDetailsResponse;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.LoanApplicationDetailsForSp;
import com.opl.mudra.api.loans.model.LoanApplicationRequest;
import com.opl.mudra.api.loans.model.LoanDisbursementRequest;
import com.opl.mudra.api.loans.model.LoanPanCheckRequest;
import com.opl.mudra.api.loans.model.LoanSanctionRequest;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.model.PaymentRequest;
import com.opl.mudra.api.loans.model.common.AutoFillOneFormDetailRequest;
import com.opl.mudra.api.loans.model.common.ChatDetails;
import com.opl.mudra.api.loans.model.common.DisbursementRequest;
import com.opl.mudra.api.loans.model.common.EkycRequest;
import com.opl.mudra.api.loans.model.mobile.MobileLoanRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.matchengine.model.ProposalMappingRequest;
import com.opl.mudra.api.payment.model.GatewayResponse;
import com.opl.mudra.client.matchengine.ProposalDetailsClient;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.sidbi.PushPullRequest;
import com.opl.service.loans.repository.common.LoanRepository;
import com.opl.service.loans.service.common.AutoFillOneFormDetailService;
import com.opl.service.loans.service.fundseeker.corporate.ApplicationProposalMappingService;
import com.opl.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.opl.service.loans.service.fundseeker.corporate.PushPullApplicationService;
import com.opl.service.loans.service.sanction.LoanDisbursementService;
import com.opl.service.loans.service.sanction.LoanSanctionService;
import com.opl.service.loans.utils.CommonDocumentUtils;
import com.opl.service.loans.utils.CommonUtility;

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
