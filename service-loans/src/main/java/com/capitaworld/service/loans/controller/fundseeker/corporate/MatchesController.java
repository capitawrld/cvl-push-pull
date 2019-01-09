package com.capitaworld.service.loans.controller.fundseeker.corporate;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.config.AsyncComponent;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.common.ProposalList;
import com.capitaworld.service.loans.service.fundprovider.ProductMasterService;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.matchengine.MatchEngineClient;
import com.capitaworld.service.matchengine.model.MatchRequest;
import com.capitaworld.service.matchengine.model.MatchResponse;


@RestController
@RequestMapping("/matches")
public class MatchesController {

	private static final Logger logger = LoggerFactory.getLogger(MatchesController.class);

	private static final String MATCH_REQUEST_MUST_NOT_BE_EMPTY_MSG = "matchRequest must not be empty ==>";
	private static final String MATCHES_SUCCESSFULLY_SAVED_MSG = "Matches Successfully Saved";
	private static final String MATCH_FS_CORPORATE = "matchFSCorporate";

	@Autowired
	private MatchEngineClient engineClient;
	
	@Autowired
	private ProductMasterService productMasterService; 

	@Autowired LoanApplicationService loanApplicationService;
	
	@Autowired
	private AsyncComponent asyncComponent;
	
	/*@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public String getPing() {
		logger.info("Ping success");
		return "Ping Succeed";
	}*/

	@RequestMapping(value = "/${corporate}/fundseeker", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> matchFSCorporate(@RequestBody MatchRequest matchRequest,
			HttpServletRequest request,@RequestParam(value = "clientId", required = false) Long clientId) {
		CommonDocumentUtils.startHook(logger, MATCH_FS_CORPORATE);
		Long userId = null;
		Integer userType = (Integer)request.getAttribute(CommonUtils.USER_TYPE);
		   if(CommonDocumentUtils.isThisClientApplication(request)){
		    userId = clientId;
		   } else {
		    userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		   }
		matchRequest.setUserId(userId);
		
		if (matchRequest == null || matchRequest.getApplicationId() == null) {
			logger.warn(MATCH_REQUEST_MUST_NOT_BE_EMPTY_MSG + matchRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			MatchResponse matchResponse = engineClient.calculateMatchesOfCorporateFundSeeker(matchRequest);
			if (matchResponse != null && matchResponse.getStatus() == 200) {
				CommonDocumentUtils.endHook(logger, MATCH_FS_CORPORATE);
				if(CommonUtils.UserType.FUND_SEEKER == userType){
					logger.info("Start Sending Mail To Fs Corporate for Profile and primary fill complete");
					asyncComponent.sendMailWhenUserCompletePrimaryForm(userId,matchRequest.getApplicationId());	
				}
				LoansResponse loansResponse = new LoansResponse(MATCHES_SUCCESSFULLY_SAVED_MSG, HttpStatus.OK.value());
				loansResponse.setData(matchResponse.getData());
				loansResponse.setFlag(matchResponse.getIsUBIMatched());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
			CommonDocumentUtils.endHook(logger, MATCH_FS_CORPORATE);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving Matches for Corporate Fundseeker ==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/${retail}/fundseeker", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> matchFSRetail(@RequestBody MatchRequest matchRequest,
			HttpServletRequest request,@RequestParam(value = "clientId", required = false) Long clientId) {
		CommonDocumentUtils.startHook(logger, "matchFSRetail");
		Long userId = null;
		Integer userType = (Integer)request.getAttribute(CommonUtils.USER_TYPE);
		   if(CommonDocumentUtils.isThisClientApplication(request)){
		    userId = clientId;
		   } else {
		    userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		   }
		matchRequest.setUserId(userId);
		
		if (matchRequest == null || matchRequest.getApplicationId() == null) {
			logger.warn(MATCH_REQUEST_MUST_NOT_BE_EMPTY_MSG + matchRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			MatchResponse matchResponse = engineClient.calculateMatchesOfRetailFundSeeker(matchRequest);
			CommonDocumentUtils.endHook(logger, "matchFSRetail");
			if (matchResponse != null && matchResponse.getStatus() == 200) {
				if(CommonUtils.UserType.FUND_SEEKER == userType){
					logger.info("Start Sending Mail To Fs Retails for Profile and primary fill complete");
					asyncComponent.sendMailWhenUserCompletePrimaryForm(userId,matchRequest.getApplicationId());	
				}
				LoansResponse loansResponse = new LoansResponse(MATCHES_SUCCESSFULLY_SAVED_MSG, HttpStatus.OK.value());
				loansResponse.setData(matchResponse.getData());
				loansResponse.setFlag(matchResponse.getIsUBIMatched());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving Matches for Retail Fundseeker ==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/${corporate}/fundprovider", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> matchFPCorporate(@RequestBody MatchRequest matchRequest,
			HttpServletRequest request,@RequestParam(value = "clientId", required = false) Long clientId) {
		CommonDocumentUtils.startHook(logger, "matchFPCorporate");
		Long userId = null;
		   if(CommonDocumentUtils.isThisClientApplication(request)){
		    userId = clientId;
		   } else {
		    userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		   }
		matchRequest.setUserId(userId);
		
		if (matchRequest == null || matchRequest.getProductId() == null) {
			logger.warn(MATCH_REQUEST_MUST_NOT_BE_EMPTY_MSG + matchRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			MatchResponse matchResponse = engineClient.calculateMatchesOfCorporateFundProvider(matchRequest);
			CommonDocumentUtils.endHook(logger, "matchFPCorporate");
			if (matchResponse != null && matchResponse.getStatus() == 200) {
				
				// update is match field in parameter table
				productMasterService.setIsMatchProduct(matchRequest.getProductId(), matchRequest.getUserId());
				
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(MATCHES_SUCCESSFULLY_SAVED_MSG, HttpStatus.OK.value()), HttpStatus.OK);
			}
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving Matches for Corporate Fundseeker ==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/${retail}/fundprovider", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> matchFPRetail(@RequestBody MatchRequest matchRequest,
			HttpServletRequest request,@RequestParam(value = "clientId", required = false) Long clientId) {
		CommonDocumentUtils.startHook(logger, "matchFPRetail");
		Long userId = null;
		   if(CommonDocumentUtils.isThisClientApplication(request)){
		    userId = clientId;
		   } else {
		    userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		   }
		matchRequest.setUserId(userId);
		
		
		if(!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ORG_ID))) {
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			if(!CommonUtils.isObjectNullOrEmpty(userOrgId)) {
				logger.info("Found User Org Id, So we can't process matches more ! ------------->" + userOrgId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("This is UBI fundprovider, So we can't process matches more !", HttpStatus.OK.value()), HttpStatus.OK);
			}
		}
		
		
		
		if (matchRequest == null || matchRequest.getProductId() == null) {
			logger.warn(MATCH_REQUEST_MUST_NOT_BE_EMPTY_MSG + matchRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			MatchResponse matchResponse = engineClient.calculateMatchesOfRetailFundProvider(matchRequest);
			CommonDocumentUtils.endHook(logger, "matchFPRetail");
			if (matchResponse != null && matchResponse.getStatus() == 200) {
				
				// update is match field in parameter table
				productMasterService.setIsMatchProduct(matchRequest.getProductId(), matchRequest.getUserId());
				
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(MATCHES_SUCCESSFULLY_SAVED_MSG, HttpStatus.OK.value()), HttpStatus.OK);
			}
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving Matches for Retail Fundseeker ==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}
	
	
	@RequestMapping(value = "/saveSuggestionList", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveSuggestionList(@RequestBody ProposalList proposalList,
			HttpServletRequest request,@RequestParam(value = "clientId", required = false) Long clientId) {
		CommonDocumentUtils.startHook(logger, "saveSuggestionList");
		Long userId = null;
		   if(CommonDocumentUtils.isThisClientApplication(request)){
		    userId = clientId;
		   } else {
		    userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		   }
		   proposalList.setUserId(userId);
		
		if (proposalList == null || proposalList.getApplicationId() == null ||CommonUtils.isObjectNullOrEmpty(proposalList.getApplicationId())) {
			logger.warn("proposalList must not be empty ==>" + proposalList);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			
				
				// update is match field in parameter table
				loanApplicationService.saveSuggestionList(proposalList);
				CommonDocumentUtils.endHook(logger, "saveSuggestionList");	
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("saveSuggestionList Successfully Saved", HttpStatus.OK.value()), HttpStatus.OK);
			

		} catch (Exception e) {
			logger.error("Error while saveSuggestionList ==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}
}
