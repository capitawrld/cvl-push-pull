package com.capitaworld.service.loans.controller.fundseeker.retail;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.retail.OtherCurrentAssetDetailRequest;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.fundseeker.retail.OtherCurrentAssetDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

/**
 * @author Sanket
 *
 */
@RestController
@RequestMapping("/other_current_asset_detail")
public class OtherCurrentAssetDetailController {

	private static final Logger logger = LoggerFactory.getLogger(OtherCurrentAssetDetailController.class);

	@Autowired
	private OtherCurrentAssetDetailService otherCurrentAssetDetailService;
	
	@Autowired
	private RetailApplicantService retailApplicantService;
	
	@Autowired
	private CoApplicantService coApplicantService;
	
	@Autowired
	private GuarantorService guarantorService;
	
	@Autowired
	private LoanApplicationService loanApplicationService;

	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody FrameRequest frameRequest, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

		if (frameRequest == null) {
			logger.warn("frameRequest can not be empty ==>" + frameRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		// application id and user id must not be null
		if (frameRequest.getApplicationId() == null || frameRequest.getApplicantType() == 0) {
			logger.warn("application id, user id and applicant Type must not be null ==>" + frameRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

		try {
			frameRequest.setUserId(userId);
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				frameRequest.setClientId(clientId);
			}
			//Checking Profile is Locked
			Long finalUserId = (CommonUtils.isObjectNullOrEmpty(frameRequest.getClientId()) ? userId
					: frameRequest.getClientId());
			Long applicationId = null;
			if(CommonUtils.ApplicantType.APPLICANT == frameRequest.getApplicantType()){
				applicationId = frameRequest.getApplicationId();
			}else if(CommonUtils.ApplicantType.COAPPLICANT == frameRequest.getApplicantType()){
				applicationId = coApplicantService.getApplicantIdById(frameRequest.getApplicationId());
			}else if(CommonUtils.ApplicantType.GARRANTOR == frameRequest.getApplicantType()){
				applicationId = guarantorService.getApplicantIdById(frameRequest.getApplicationId());
			}
			Boolean primaryLocked = loanApplicationService.isFinalLocked(applicationId, finalUserId);
			if(!CommonUtils.isObjectNullOrEmpty(primaryLocked) && primaryLocked.booleanValue()){
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.APPLICATION_LOCKED_MESSAGE, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			otherCurrentAssetDetailService.saveOrUpdate(frameRequest);
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving Other Current Asset Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/getList/{applicationType}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getList(@PathVariable Long id, @PathVariable int applicationType,
			@RequestParam(value = "clientId", required = false) Long clientId, HttpServletRequest request) {
		// request must not be null
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (id == null) {
				logger.warn("ID Require to get Other Current Asset Details ==>" + id);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			List<OtherCurrentAssetDetailRequest> response = otherCurrentAssetDetailService
					.getOtherCurrentAssetDetailList(id, applicationType);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(response);
			Integer currencyId = null;
			Long applicantIdById = null;
			switch (applicationType) {
			case CommonUtils.ApplicantType.APPLICANT:
				currencyId = retailApplicantService.getCurrency(id, userId);
				break;
			case CommonUtils.ApplicantType.COAPPLICANT:
				applicantIdById = coApplicantService.getApplicantIdById(id);
				currencyId = retailApplicantService.getCurrency(applicantIdById, userId);
				break;
			case CommonUtils.ApplicantType.GARRANTOR:
				applicantIdById = guarantorService.getApplicantIdById(id);
				currencyId = retailApplicantService.getCurrency(applicantIdById, userId);
				break;
			default : break;
			}
			loansResponse.setData(CommonDocumentUtils.getCurrency(currencyId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting Other Current Asset Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
