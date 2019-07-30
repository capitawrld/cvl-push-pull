package com.capitaworld.service.loans.controller.fundseeker;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.model.InEligibleProposalDetailsRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.ProposalDetailsAdminRequest;
import com.capitaworld.service.loans.repository.common.LoanRepository;
import com.capitaworld.service.loans.service.common.IneligibleProposalDetailsService;
import com.capitaworld.service.loans.utils.CommonUtils;

/**
 * Created by KushalCW on 22-09-2018.
 */

@RestController
public class IneligibleProposalDetailsController {

	private static final Logger logger = LoggerFactory.getLogger(IneligibleProposalDetailsController.class);

	@Autowired
	private IneligibleProposalDetailsService ineligibleProposalDetailsService;
	
	@Autowired
	private LoanRepository loanRepository;
	
/**
 * need to change the method sendMailToFsAndBankBranch of applicationId to proposalId
 * */
	@RequestMapping(value = "/save/ineligible/proposal", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(
			@RequestBody InEligibleProposalDetailsRequest inEligibleProposalDetailsRequest,
			HttpServletRequest request) {
		if (CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest)
				|| CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getApplicationId())
				|| CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getUserOrgId())
				|| CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getBranchId())) {
			logger.warn("Requested data can not be empty.Invalid Request. ");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		
		if(CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getUserId())) {
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if (!CommonUtils.isObjectNullOrEmpty(userId)) {
				inEligibleProposalDetailsRequest.setUserId(userId);
			}
		}

		Boolean isCampaignUser = loanRepository.isCampaignUser(inEligibleProposalDetailsRequest.getUserId());
		if(isCampaignUser) {
			InEligibleProposalDetailsRequest proposalDetailsRequest = ineligibleProposalDetailsService.get(inEligibleProposalDetailsRequest.getApplicationId());
			if(proposalDetailsRequest != null && CommonUtils.OfflineApplicationConfig.BankSpecific.ON.equalsIgnoreCase(proposalDetailsRequest.getAddiFields())) {
				
			}
		}
		
		Integer isDetailsSaved = ineligibleProposalDetailsService.save(inEligibleProposalDetailsRequest);
		Integer fsBusinessType = ineligibleProposalDetailsService.getBusinessTypeIdFromApplicationId(inEligibleProposalDetailsRequest.getApplicationId());
		if (isDetailsSaved == 2) {
			Boolean isEligible = false;
			if(!CommonUtils.isObjectNullOrEmpty(fsBusinessType)
					&& fsBusinessType == CommonUtils.BusinessType.EXISTING_BUSINESS.getId()){
				//Trigger mail  to fs and bank branch
				//This email check if the selected bank is (sbi and wc_renewal) or sidbi specific then this email shoot
				isEligible = ineligibleProposalDetailsService.sendMailToFsAndBankBranchForSbiBankSpecific(
						inEligibleProposalDetailsRequest.getApplicationId(),
						inEligibleProposalDetailsRequest.getBranchId(),inEligibleProposalDetailsRequest.getUserOrgId(),false);
			}	
				if(!isEligible) {
					//If users is not from sbi and sidbi specific then this email shoot
					Boolean isSent = ineligibleProposalDetailsService.sendMailToFsAndBankBranch(
							inEligibleProposalDetailsRequest.getApplicationId(),
							inEligibleProposalDetailsRequest.getBranchId(),inEligibleProposalDetailsRequest.getUserOrgId());
					if (isSent) {
						logger.info("Email sent to fs and branch");
					} else {
						logger.info("Error in sending email to fs and branch");
					}
				}
			
			return new ResponseEntity<LoansResponse>(new LoansResponse("Data saved", HttpStatus.OK.value()),
					HttpStatus.OK);
		} else  if (isDetailsSaved == 1) {
			return new ResponseEntity<LoansResponse>(
					new LoansResponse("It seems your proposal is already sanctioned by one of our bank partner. If you did not receive any communication from bank please mail your details at support@psbloansin59minutes.com", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}  else {
			return new ResponseEntity<LoansResponse>(
					new LoansResponse("Data not saved", HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/update/ineligible/status", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> update( @RequestBody InEligibleProposalDetailsRequest inEligibleProposalDetailsRequest, HttpServletRequest request) {
		if (CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest) || CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getApplicationId())
				|| CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getStatus()) ||
				CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getReason())) {
			logger.warn("Requested data can not be empty.Invalid Request. ");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}

		if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			inEligibleProposalDetailsRequest.setUserId(userId);
			inEligibleProposalDetailsRequest.setUserOrgId(userOrgId);
		}

		Boolean isDetailsSaved = ineligibleProposalDetailsService.updateStatus(inEligibleProposalDetailsRequest);
		if (isDetailsSaved) {
			return new ResponseEntity<LoansResponse>(new LoansResponse("Data saved", HttpStatus.OK.value()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<LoansResponse>(
					new LoansResponse("The application has encountered an error, please try again after sometime!!!", HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/getOfflineProposalByOrgId", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getOfflineProposalByOrgId(@RequestBody ProposalDetailsAdminRequest request, HttpServletRequest httpServletRequest) {
		
		Long userOrgId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID);
		Long userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
		
		if(CommonUtils.isObjectNullOrEmpty(userOrgId) || CommonUtils.isObjectNullOrEmpty(request.getFromDate()) || CommonUtils.isObjectNullOrEmpty(request.getToDate()) || CommonUtils.isObjectNullOrEmpty(userId)) {
			logger.info("Bad Request !!");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Request parameter null or empty !!", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		
		List<ProposalDetailsAdminRequest> dataList = ineligibleProposalDetailsService.getOfflineProposals(userOrgId, userId, request);
		
		LoansResponse response = new LoansResponse("Data Found.", HttpStatus.OK.value());
		response.setData(dataList);
		
		return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
	}

	/**
	 * Transfer branch and reason
	 * @param inEligibleProposalDetailsRequest
	 * @param request
	 * @return
	 */
	@PostMapping(value = "/update/ineligible/transferBranch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateTransferBranch( @RequestBody InEligibleProposalDetailsRequest inEligibleProposalDetailsRequest, HttpServletRequest request) {
		if (CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest) || CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getBranchId())
				|| CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getIneligibleProposalId()) ||
				CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getReason())) {
			logger.warn("Requested data can not be empty.Invalid Request. ");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}

		if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			inEligibleProposalDetailsRequest.setUserId(userId);
			inEligibleProposalDetailsRequest.setUserOrgId(userOrgId);
		}

		Boolean isDetailsSaved = ineligibleProposalDetailsService.updateTransferBranchDetail(inEligibleProposalDetailsRequest);
		if (isDetailsSaved) {
			return new ResponseEntity<LoansResponse>(new LoansResponse("Data saved", HttpStatus.OK.value()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<LoansResponse>(
					new LoansResponse("The application has encountered an error, please try again after sometime!!!", HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
		}
	}

	/**
	 * Re open proposal
	 * @param inEligibleProposalDetailsRequest
	 * @param request
	 * @return
	 */
	@PostMapping(value = "/update/ineligible/reOpenProposalDetail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateReOpenProposalDetail(@RequestBody InEligibleProposalDetailsRequest inEligibleProposalDetailsRequest, HttpServletRequest request) {
		if (CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest) || CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getReOpenReason())
				|| CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getIneligibleProposalId())) {
			logger.warn("Requested data can not be empty.Invalid Request. ");
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}

		if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			inEligibleProposalDetailsRequest.setUserId(userId);
			inEligibleProposalDetailsRequest.setUserOrgId(userOrgId);
		}

		Boolean isDetailsSaved = ineligibleProposalDetailsService.updateReOpenProposalDetail(inEligibleProposalDetailsRequest);
		if (isDetailsSaved) {
			return new ResponseEntity<LoansResponse>(new LoansResponse("Data updated", HttpStatus.OK.value()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<LoansResponse>(
					new LoansResponse("The application has encountered an error, please try again after sometime!!!", HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/checkIsExistOfflineProposalByApplicationId/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> checkIsExistOfflineProposalByApplicationId(@PathVariable(value = "applicationId") Long applicationId) {
		LoansResponse response = new LoansResponse("Success.", HttpStatus.OK.value());
		response.setFlag(ineligibleProposalDetailsService.checkIsExistOfflineProposalByApplicationId(applicationId));
		return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "/sendInEligibleForSidbi/{applicationId}")
	public ResponseEntity<LoansResponse> sendInEligibleForSidbi(@PathVariable("applicationId") Long applicationId) {
		logger.info("Enter in sendInEligibleForSidbi");
		try {
			return new ResponseEntity<>(new LoansResponse("Successfully sent mail !!",HttpStatus.OK.value(),ineligibleProposalDetailsService.sendInEligibleForSidbi(applicationId)), HttpStatus.OK);
		} catch (Exception e) {
			logger.warn("Error while sendInEligibleForSidbi",e);
			return new ResponseEntity<>(new LoansResponse("Something went wrong !!",HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
