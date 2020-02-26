
package com.capitaworld.service.loans.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.capitaworld.connect.api.ConnectRequest;
import com.capitaworld.service.loans.config.AsyncComponent;
import com.capitaworld.service.loans.model.FundProviderProposalDetails;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.ProposalDetailsAdminRequest;
import com.capitaworld.service.loans.model.common.ReportRequest;
import com.capitaworld.service.loans.service.ProposalService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.matchengine.model.DisbursementDetailsModel;
import com.capitaworld.service.matchengine.model.DisbursementRequestModel;
import com.capitaworld.service.matchengine.model.ProposalCountResponse;
import com.capitaworld.service.matchengine.model.ProposalMappingRequest;
import com.capitaworld.service.matchengine.model.ProposalMappingResponse;
import com.capitaworld.service.notification.model.SchedulerDataMultipleBankRequest;
import com.capitaworld.service.users.model.UsersRequest;


@RestController
@RequestMapping("/proposal")
public class ProposalController {
	
	private static final Logger logger = LoggerFactory.getLogger(ProposalController.class);

	private static final String REQUEST_GET_SIZE = "request.getSize()::";
	private static final String REQUEST_GET_PAGE_INDEX = "request.getPageIndex()::";
	private static final String BAD_REQUEST_MSG = "Bad Request !!";
	private static final String REQUEST_PARAMETER_NULL_OR_EMPTY = "Request parameter null or empty !!";

	@Autowired
	ProposalService proposalService;
	
	@Autowired
	private AsyncComponent asyncComponent;
	
//	@Autowired
//	private UsersClient userClient;
	
	@RequestMapping(value = "/fundproviderProposal", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> fundproviderProposal(@RequestBody ProposalMappingRequest request,HttpServletRequest httpRequest,@RequestParam(value = "clientId", required = false) Long clientId) {
		
		// request must not be null
		logger.info(REQUEST_GET_PAGE_INDEX+request.getPageIndex());
		logger.info(REQUEST_GET_SIZE+request.getSize());
		
		Long userId = null;
		if (CommonDocumentUtils.isThisClientApplication(httpRequest) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
		} else {
			userId = (Long) httpRequest.getAttribute(CommonUtils.USER_ID);
		}
		request.setUserId(userId);
		List proposalDetailsList=proposalService.fundproviderProposalByProposalId(request);
		logger.info("proposalDetailsList"+proposalDetailsList);

		LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
		loansResponse.setListData(proposalDetailsList);
		return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
	}


	@RequestMapping(value = "/basicInfoToSearch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> basicInfoToSearch(@RequestBody ProposalMappingRequest request,HttpServletRequest httpRequest,@RequestParam(value = "clientId", required = false) Long clientId) {

		// request must not be null
		logger.info(REQUEST_GET_PAGE_INDEX+request.getPageIndex());
		logger.info(REQUEST_GET_SIZE+request.getSize());

		Long userId = null;
		Long userOrgId = null;

		if (CommonDocumentUtils.isThisClientApplication(httpRequest) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
		} else {
			userId = (Long) httpRequest.getAttribute(CommonUtils.USER_ID);
		}
		userOrgId = (Long) httpRequest.getAttribute(CommonUtils.USER_ORG_ID);
		request.setUserId(userId);
		request.setUserOrgId(userOrgId);
		List proposalDetailsList=proposalService.basicInfoForSearch(request);
		LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
		loansResponse.setListData(proposalDetailsList);
		return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
	}
	
	
	
	@RequestMapping(value = "/fundseekerProposal", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<FundProviderProposalDetails>> fundseekerProposal(@RequestBody ProposalMappingRequest request,HttpServletRequest httpRequest,@RequestParam(value = "clientId", required = false) Long clientId) {
		
		// request must not be null
		Long userId = null;
		if (CommonDocumentUtils.isThisClientApplication(httpRequest)) {
			userId = clientId;
		} else {
			userId = (Long) httpRequest.getAttribute(CommonUtils.USER_ID);
		}
		List<FundProviderProposalDetails> proposalDetailsList=proposalService.fundseekerProposal(request, userId);
		return new ResponseEntity<List<FundProviderProposalDetails>>(proposalDetailsList,HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/getOfflineProposals", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getOfflineProposals(@RequestBody ProposalMappingRequest request) {
		
		if(CommonUtils.isObjectNullOrEmpty(request.getApplicationId())) {
			logger.info("getOfflineProposals " + BAD_REQUEST_MSG);
			return new ResponseEntity<>(new LoansResponse(REQUEST_PARAMETER_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			return new ResponseEntity<>(new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value(),proposalService.getOfflineProposalList(request.getApplicationId())), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<>(new LoansResponse(e.getMessage()) , HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@RequestMapping(value = "/saveDisbursementDetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalMappingResponse> saveDisbursementDetails(@RequestBody DisbursementDetailsModel request, HttpServletRequest httpRequest, @RequestParam(value = "clientId", required = false) Long clientId) {
		
		// request must not be null
		Long userId = null;
		if (CommonDocumentUtils.isThisClientApplication(httpRequest) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
		} else {
			userId = (Long) httpRequest.getAttribute(CommonUtils.USER_ID);
		}
		request.setUserId(userId);
		return new ResponseEntity<ProposalMappingResponse>(proposalService.saveDisbursementDetails(request, userId),HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/count/fundprovider", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalCountResponse> fundProviderProposalCount(@RequestBody ProposalMappingRequest request,HttpServletRequest httpServletRequest,@RequestParam(value = "clientId", required = false) Long clientId,@RequestParam(value = "clientUserType", required = false) Long clientUserType) {
		Long userId = null;
		Long userType = null;
		if (CommonDocumentUtils.isThisClientApplication(httpServletRequest) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
			userType = clientUserType;
		} else {
			userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
			userType = Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_TYPE).toString());
		}
		request.setUserId(userId);
		request.setUserType(userType);

		return new ResponseEntity<ProposalCountResponse>(proposalService.fundProviderProposalCount(request),HttpStatus.OK);
	}
	
	@RequestMapping(value = "/count/fundseeker", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalCountResponse> fundSeekerProposalCount(@RequestBody ProposalMappingRequest request) {
		return new ResponseEntity<ProposalCountResponse>(proposalService.fundSeekerProposalCount(request),HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalMappingResponse> get(@RequestBody ProposalMappingRequest request,HttpServletRequest httpServletRequest,@RequestParam(value = "clientId", required = false) Long clientId,@RequestParam(value = "clientUserType", required = false) Long clientUserType) {
		
		Long userId = null;
		Long userType = null;
		if (CommonDocumentUtils.isThisClientApplication(httpServletRequest)  && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
			userType = clientUserType;
		} else {
			userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
			userType = Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_TYPE).toString());
		}
		if(!CommonUtils.isObjectNullOrEmpty(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID))) {
			request.setUserOrgId(Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID).toString()));	
		}
		request.setUserType(userType);
		request.setUserId(userId);
		ProposalMappingResponse response = proposalService.
				get(request);
		response.setUserType(userType.longValue());
		return new ResponseEntity<ProposalMappingResponse>(response,HttpStatus.OK);
	}
	
	
	
	
	//Arun's Code
	
	@RequestMapping(value = "/getByProposalId", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalMappingResponse> getByProposalId(@RequestBody ProposalMappingRequest request,HttpServletRequest httpServletRequest,@RequestParam(value = "clientId", required = false) Long clientId,@RequestParam(value = "clientUserType", required = false) Long clientUserType) {
		
		Long userId = null;
		Long userType = null;
		if (CommonDocumentUtils.isThisClientApplication(httpServletRequest)  && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
			userType = clientUserType;
		} else {
			userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
			userType = Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_TYPE).toString());
		}
		if(!CommonUtils.isObjectNullOrEmpty(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID))) {
			request.setUserOrgId(Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID).toString()));	
		}
		request.setUserType(userType);
		request.setUserId(userId);
		ProposalMappingResponse response = proposalService.getProposalId(request);
		response.setUserType(userType.longValue());
		return new ResponseEntity<ProposalMappingResponse>(response,HttpStatus.OK);
	}
	
	
	
	
	
	
	

	@RequestMapping(value = "/getSanctionProposalByApplicationId", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalMappingResponse> getSanctionProposalByApplicationId(@RequestBody ProposalMappingRequest request,HttpServletRequest httpServletRequest,@RequestParam(value = "clientId", required = false) Long clientId,@RequestParam(value = "clientUserType", required = false) Long clientUserType) {


		if(!CommonUtils.isObjectNullOrEmpty(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID))) {
			request.setUserOrgId(Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID).toString()));
		}
		ProposalMappingResponse response = proposalService.
				getSanctionProposalByApplicationId(request.getApplicationId(),request.getUserOrgId());
		return new ResponseEntity<ProposalMappingResponse>(response,HttpStatus.OK);
	}

	@RequestMapping(value = "/changeStatus", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalMappingResponse> changeStatus(@RequestBody ProposalMappingRequest request,HttpServletRequest httpServletRequest,@RequestParam(value = "clientId", required = false) Long clientId,@RequestParam(value = "clientUserType", required = false) Long clientUserType) {
		Long userId = null;
		Long userType = null;
		if (CommonDocumentUtils.isThisClientApplication(httpServletRequest) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
			userType = clientUserType;
		} else {
			userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
			userType = Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_TYPE).toString());
		}
		
		if(!CommonUtils.isObjectNullOrEmpty(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID))) {
			request.setUserOrgId(Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID).toString()));	
		}
		request.setLastActionPerformedBy(userType);
		request.setUserId(userId);
		request.setClientId(clientId);
		return new ResponseEntity<ProposalMappingResponse>(proposalService.changeStatus(request),HttpStatus.OK);
	}
	
	@RequestMapping(value = "/sendRequest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalMappingResponse> sendRequest(@RequestBody ProposalMappingRequest request,HttpServletRequest httpServletRequest,@RequestParam(value = "clientId", required = false) Long clientId,@RequestParam(value = "clientUserType", required = false) Long clientUserType) {
		Long userId = null;
		Long userType = null;
		Integer loginUserType = ((Integer) httpServletRequest.getAttribute(CommonUtils.USER_TYPE));
		if (CommonDocumentUtils.isThisClientApplication(httpServletRequest)  && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
			userType = clientUserType;
		} else {
			userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
			userType = Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_TYPE).toString());
		}
		request.setUserId(userId);
		request.setUserType(userType.longValue());
		ProposalMappingResponse response = proposalService.sendRequest(request);
		if(response.getStatus() == HttpStatus.OK.value() && CommonUtils.UserType.FUND_PROVIDER == loginUserType ) {
				logger.info("ProposalController, FP send request to fund seeker and sent mail");
				if(!CommonUtils.isObjectNullOrEmpty(request.getFpProductId())) {
					asyncComponent.sentMailWhenFPSentFSDirectREquest(userId,request.getFpProductId(),request.getApplicationId());	
				} else {
					logger.info("ProposalController, FP ProductId or application id null or empty");	
				}
		}
		return new ResponseEntity<ProposalMappingResponse>(response,HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "/listfundseekerproposal", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalMappingResponse> listOfFundSeekerProposal(@RequestBody ProposalMappingRequest request) {
		return new ResponseEntity<ProposalMappingResponse>(proposalService.listOfFundSeekerProposal(request),HttpStatus.OK);
	}
	
	@RequestMapping(value = "/connections", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> connections(@RequestBody ProposalMappingRequest request,HttpServletRequest httpServletRequest,@RequestParam(value = "clientUserType", required = false) Long clientUserType) {
		try {
			Long userType = null;
			if (CommonDocumentUtils.isThisClientApplication(httpServletRequest) && !CommonUtils.isObjectNullOrEmpty(clientUserType)) {
				userType = clientUserType;
			} else {
				userType = Long.valueOf(httpServletRequest.getAttribute(CommonUtils.USER_TYPE).toString());
			}
			request.setUserType(userType);
			
			
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(proposalService.getConectionList(request));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@RequestMapping(value = "/getPendingProposalCount", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> connections(@RequestBody Long applicationId,HttpServletRequest httpServletRequest,@RequestParam(value = "clientUserType", required = false) Long clientUserType) {
		try {
			
			
			
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(proposalService.getPendingProposalCount(applicationId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	/**
	 * AXIS BANK CHANGES (UPDATE ASSIGN BY AND ASSIGNTO FOR HO TO BO STEP)
	 * @param request
	 * @param httpServletRequest
	 * @param clientUserType
	 * @return
	 * requestJson :- {fpProductId : 20,branchId : 2}
	 */
	@RequestMapping(value = "/updateAssignDetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateAssignDetails(@RequestBody ProposalMappingRequest request,HttpServletRequest httpServletRequest,@RequestParam(value = "clientId", required = false) Long clientId) {
		logger.info("Enter in update assign details for axis bank flow");
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(httpServletRequest)  && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId  = clientId;				
			} else {
				userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
			}
			if(CommonUtils.isObjectNullOrEmpty(request.getFpProductId()) || CommonUtils.isObjectNullOrEmpty(request.getBranchId())) {
				logger.info("Fp Product id or Branch id null or empty !!");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(REQUEST_PARAMETER_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			
			request.setUserId(userId);
			ProposalMappingResponse updateAssignDetails = proposalService.updateAssignDetails(request);
			logger.info("Successfully updated assign details");
			return new ResponseEntity<LoansResponse>(new LoansResponse(updateAssignDetails.getMessage(), HttpStatus.OK.value()), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Throw Exception while update assign details : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/fundproviderProposalByAssignBy", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> fundproviderProposalByAssignBy(@RequestBody ProposalMappingRequest request,HttpServletRequest httpRequest,@RequestParam(value = "clientId", required = false) Long clientId) {
		
		// request must not be null
		logger.info(REQUEST_GET_PAGE_INDEX+request.getPageIndex());
		logger.info(REQUEST_GET_SIZE+request.getSize());
		
		Long userId = null;
		if (CommonDocumentUtils.isThisClientApplication(httpRequest) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
		} else {
			userId = (Long) httpRequest.getAttribute(CommonUtils.USER_ID);
		}
		request.setUserId(userId);
		
		if(CommonUtils.isObjectNullOrEmpty(request.getFpProductId())) {
			logger.info("Fp Product id null or empty !!");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(REQUEST_PARAMETER_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST.value()),
					HttpStatus.OK);
		}
		logger.info("User id ------------------>" + userId + "----------------------------" + request.getFpProductId());
		List proposalDetailsList=proposalService.fundproviderProposalByAssignBy(request);
		
		LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
		loansResponse.setListData(proposalDetailsList);
		CommonDocumentUtils.endHook(logger, "fundproviderProposalByAssignBy");
		return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		
	}

	@RequestMapping(value = "/checkAvailabilityForBankSelection", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> checkAvailabilityForBankSelection(@RequestBody ConnectRequest request) {

		if(CommonUtils.isObjectNullOrEmpty(request.getApplicationId())) {
			logger.info("Bad Request !!");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Request parameter null or empty !!", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}

		LoansResponse response = new LoansResponse("Data Found.", HttpStatus.OK.value(), proposalService.checkAvailabilityForBankSelection(request.getApplicationId(), request.getBusinessTypeId()));
		return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/checkFpMakerAccess", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> checkFpMakerAccess(@RequestBody UsersRequest userRequest, HttpServletRequest httpServletRequest) {

		Long userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
		Long userOrgId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID);
		userRequest.setId(userId);
		userRequest.setApplicationId(Long.parseLong(CommonUtils.decode(userRequest.getApplicationIdString())));


		LoansResponse loansResponse=proposalService.checkMinMaxAmount(userRequest,userOrgId);
		return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getProposalByOrgId", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getProposalByOrgId(@RequestBody ProposalDetailsAdminRequest request, HttpServletRequest httpServletRequest) {
		
		Long userOrgId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID);
		Long userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
		
		if(CommonUtils.isObjectNullOrEmpty(userOrgId) || CommonUtils.isObjectNullOrEmpty(request.getFromDate()) || CommonUtils.isObjectNullOrEmpty(request.getToDate()) || CommonUtils.isObjectNullOrEmpty(userId)) {
			logger.info(BAD_REQUEST_MSG);
			return new ResponseEntity<LoansResponse>(new LoansResponse(REQUEST_PARAMETER_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		
		List<ProposalDetailsAdminRequest> dataList = proposalService.getProposalsByOrgId(userOrgId, request, userId);
		
		LoansResponse response = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
		response.setData(dataList);
		
		return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getHomeCounterDetail", method = RequestMethod.GET)
	public ResponseEntity<LoansResponse> getHomeCounter() {
		
		try {
		
		LoansResponse response = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
		Object obj = proposalService.getHomeCounterDetail();
		response.setData(obj);
		return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(e.getMessage()) , HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@RequestMapping(value = "/searchProposals", method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> searchProposals(@RequestBody ReportRequest reportRequest ,HttpServletRequest httpServletRequest) {

		Long userOrgId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID);
		Long userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
		Long businessTypeId = reportRequest.getBusinessTypeId();
		logger.info("========>>>>>>>>>>>>>>>>>>>>>>|||||||||||||||| businessTypeId ||||||||||{}",businessTypeId);
		if(CommonUtils.isObjectNullOrEmpty(userOrgId) || CommonUtils.isObjectNullOrEmpty(userId) || CommonUtils.isObjectNullOrEmpty(reportRequest.getValue())|| CommonUtils.isObjectNullOrEmpty(businessTypeId)) {
			logger.info(BAD_REQUEST_MSG);
			return new ResponseEntity<LoansResponse>(new LoansResponse(REQUEST_PARAMETER_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		if(CommonUtils.isObjectNullOrEmpty(reportRequest.getNumber())) {
			reportRequest.setNumber(10);
		}
		try {
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value(),proposalService.searchProposalByAppCode(userId, userOrgId, reportRequest,businessTypeId)), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(e.getMessage()) , HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/fpDashboardProposalCont/{businessTypeId}", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> fpDashboardProposalCont(@PathVariable("businessTypeId") Long businessTypeId,HttpServletRequest httpServletRequest) {

		Long userOrgId = Long.parseLong(httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID).toString());
		Long userId = Long.parseLong(httpServletRequest.getAttribute(CommonUtils.USER_ID).toString());
		if(CommonUtils.isObjectNullOrEmpty(userOrgId) || CommonUtils.isObjectNullOrEmpty(userId) || CommonUtils.isObjectNullOrEmpty(businessTypeId)) {
			logger.info(BAD_REQUEST_MSG);
			return new ResponseEntity<LoansResponse>(new LoansResponse(REQUEST_PARAMETER_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value(),proposalService.getFpDashBoardCount(userId, userOrgId,businessTypeId)), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(e.getMessage()) , HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/update_Status", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateStatus(@RequestBody ProposalMappingRequest mappingRequest, HttpServletRequest httpServletRequest) {

		Long userOrgId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ORG_ID);
		Long userId = (Long) httpServletRequest.getAttribute(CommonUtils.USER_ID);
		if(CommonUtils.isObjectNullOrEmpty(userOrgId) || CommonUtils.isObjectNullOrEmpty(userId)) {
			logger.info(BAD_REQUEST_MSG);
			return new ResponseEntity<LoansResponse>(new LoansResponse(REQUEST_PARAMETER_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}

		if(CommonUtils.isObjectNullOrEmpty(mappingRequest.getApplicationId()) || CommonUtils.isObjectNullOrEmpty(mappingRequest.getProposalStatusId())
				 || CommonUtils.isObjectNullOrEmpty(mappingRequest.getFpProductId())) {
			logger.info(BAD_REQUEST_MSG);
			return new ResponseEntity<LoansResponse>(new LoansResponse(REQUEST_PARAMETER_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value(),proposalService.updateStatus(mappingRequest.getApplicationId(), mappingRequest.getFpProductId(), mappingRequest.getProposalStatusId(),mappingRequest.getReason())), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(e.getMessage()) , HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/inprincipleDataMulipleBank", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> multiplbankInprincipleRecord(HttpServletRequest httpServletRequest) {
		try {
			List<SchedulerDataMultipleBankRequest> schedulerDataMultipleBankRequestList = proposalService.getApplicationListForMultipleBank();

			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setListData(schedulerDataMultipleBankRequestList);
			loansResponse.setStatus(HttpStatus.OK.value());
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(e.getMessage()) , HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@GetMapping(value = "/getDayDiffrenceForMultipleBank/{loanType}",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getDayDiffrenceForMultipleBank(@PathVariable("loanType") Integer loanType) {
		try {
			if(loanType == null) {
				return new ResponseEntity<LoansResponse>(new LoansResponse("loanType can not be null") , HttpStatus.INTERNAL_SERVER_ERROR);
			}
			String dayDiff = proposalService.getDayDiffrenceForInprinciple(loanType);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			if(!CommonUtils.isObjectNullOrEmpty(dayDiff)) {
				loansResponse.setData(dayDiff);
				loansResponse.setStatus(HttpStatus.OK.value());
			}else{
				loansResponse.setMessage("diffrence not found"); 
				loansResponse.setStatus(HttpStatus.OK.value());
			}
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(e.getMessage()) , HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getDisbursementRequestDetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProposalMappingResponse> getDisbursementRequestDetails(@RequestBody DisbursementRequestModel request, HttpServletRequest httpRequest, @RequestParam(value = "clientId", required = false) Long clientId) {

		// request must not be null
		Long userId = null;
		if (CommonDocumentUtils.isThisClientApplication(httpRequest) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
			userId = clientId;
		} else {
			userId = (Long) httpRequest.getAttribute(CommonUtils.USER_ID);
		}
		request.setUserId(userId);
		return new ResponseEntity<ProposalMappingResponse>(proposalService.getDisbursementRequestDetails(request),HttpStatus.OK);

	}

	/*@RequestPart("uploadRequest") String uploadRequestString,
														@RequestPart("file") MultipartFile[] multipartFiles, HttpServletRequest request)*/

	@RequestMapping(value = "/saveDisbursementRequestDetails", method = RequestMethod.POST)
	public ResponseEntity<ProposalMappingResponse> saveDisbursementRequestDetails(@RequestPart("uploadRequest") String uploadRequestString,
																				  @RequestPart("file") MultipartFile[] multipartFiles, HttpServletRequest httpRequest) {
	//public ResponseEntity<ProposalMappingResponse> saveDisbursementRequestDetails(@RequestBody DisbursementRequestModel request, HttpServletRequest httpRequest, @RequestParam(value = "clientId", required = false) Long clientId) {

		try {
			for (MultipartFile multipartFile : multipartFiles) {

				String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());

				if (!"pdf".equalsIgnoreCase(extension)) {
					logger.warn("File format is not PDF...!");
					return new ResponseEntity<ProposalMappingResponse>(new ProposalMappingResponse(CommonUtils.WRONG_FILE_FORMAT, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
				}
			}
			ProposalMappingResponse saved = proposalService.saveDisbursementRequestDetails(multipartFiles, uploadRequestString);
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<ProposalMappingResponse>(saved, HttpStatus.OK);
		} catch (Exception e) {
			logger.warn("File format is not PDF...!");
			return new ResponseEntity<ProposalMappingResponse>(new ProposalMappingResponse(CommonUtils.WRONG_FILE_FORMAT, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
	}
}
