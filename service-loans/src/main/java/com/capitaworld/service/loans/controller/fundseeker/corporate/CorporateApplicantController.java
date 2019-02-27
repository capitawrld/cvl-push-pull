package com.capitaworld.service.loans.controller.fundseeker.corporate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
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
import com.capitaworld.service.loans.model.common.LongitudeLatitudeRequest;
import com.capitaworld.service.loans.model.corporate.CorporateApplicantRequest;
import com.capitaworld.service.loans.model.corporate.CorporateCoApplicantRequest;
import com.capitaworld.service.loans.model.corporate.SubSectorListRequest;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/fs_profile")
public class CorporateApplicantController {

	private static final Logger logger = LoggerFactory.getLogger(CorporateApplicantController.class.getName());

	private static final String ERROR_WHILE_GETTING_CORPORATE_APPLICANT_PROFILE_DETAILS_MSG = "Error while getting Corporate Applicant Profile Details==>";
	private static final String APPLICATION_ID_REQUIRE_TO_GET_CORPORATE_PROFILE_DETAILS_FOR_CLIENT_APPLICATION_ID_MSG =  "ApplicationId Require to get Corporate Profile Details for CLient Application Id ==>";

	@Autowired
	private CorporateApplicantService applicantService;

	@Autowired
	private LoanApplicationService loanApplicationService;

	/*
	 * @Autowired private RatingClient ratingClient;
	 */
	
	@RequestMapping(value = "/saveITRMapping", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveITRMappingData(@RequestBody CorporateApplicantRequest applicantRequest){
		
		if (applicantRequest.getApplicationId() == null) {
			logger.warn("Application Id can not be empty ==>", applicantRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			applicantService.saveITRMappingData(applicantRequest);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse("Successfully Saved Data!!", HttpStatus.OK.value()), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
			
	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody CorporateApplicantRequest applicantRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "save");
			// request must not be null
			Long userId = null;

			// Long tempUserId =null;// (Long) request.getAttribute(CommonUtils.USER_ID);
			// if(tempUserId != null){
			// userId =tempUserId;
			// }
			// else if(applicantRequest.getUserId() !=null){
			// userId = applicantRequest.getUserId();
			// }
			// else{
			// userId = null;
			// }

			// ==============

			if ((!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_TYPE)))
					&& (CommonDocumentUtils.isThisClientApplication(request))) {
				applicantRequest.setClientId(clientId);
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			} else {
				if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
					userId = (Long) request.getAttribute(CommonUtils.USER_ID);
				} else if (!CommonUtils.isObjectNullOrEmpty(applicantRequest.getUserId())) {
					userId = applicantRequest.getUserId();
				} else {
					logger.warn("Invalid request.");
					return new ResponseEntity<LoansResponse>(
							new LoansResponse("Invalid request.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
				}
			}

			/*
			 if (CommonUtils.UserType.SERVICE_PROVIDER == ((Integer)
			 request.getAttribute(CommonUtils.USER_TYPE))
			 .intValue()) {
			 applicantRequest.setClientId(clientId);
			 } */

			if (applicantRequest == null) {
				logger.warn("applicantRequest  can not be empty ==>", userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (applicantRequest.getApplicationId() == null) {
				logger.warn("Application Id can not be empty ==>", applicantRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			applicantRequest.setUserId(userId);
			// Checking Profile is Locked
			Long finalUserId = (CommonUtils.isObjectNullOrEmpty(applicantRequest.getClientId()) ? userId
					: applicantRequest.getClientId());
			Boolean primaryLocked = loanApplicationService.isPrimaryLocked(applicantRequest.getApplicationId(),
					finalUserId);
			if (!CommonUtils.isObjectNullOrEmpty(primaryLocked) && primaryLocked.booleanValue()) {
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.APPLICATION_LOCKED_MESSAGE, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			applicantService.save(applicantRequest, userId);
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/get/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> get(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "get");
			Long id = null;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				id = clientId;
			} else {
				id = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (applicationId == null) {
				logger.warn(
						"ApplicationId Require to get Corporate Profile Details. Application Id ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			CorporateApplicantRequest response = applicantService.getCorporateApplicant(id, applicationId);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(response);
			CommonDocumentUtils.endHook(logger, "get");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_CORPORATE_APPLICANT_PROFILE_DETAILS_MSG, e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getSectorListByIndustryList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getSectorListByIndustryList(@RequestBody List<Long> industryIdList,
			HttpServletRequest request) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getSectorListByIndustryList");
			Long id = (Long) request.getAttribute(CommonUtils.USER_ID);
			if (id == null) {
				logger.warn("userId  Require to get sectors Details ==>" + id);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (industryIdList == null) {
				logger.warn("industryIdList  Require to get sectors Details ==>" + industryIdList);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			List<Long> response = applicantService.getSectorListByIndustryId(industryIdList);
			LoansResponse loansResponse;
			if (response == null || response.isEmpty()) {
				loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			} else {
				loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
				loansResponse.setListData(response);
			}
			CommonDocumentUtils.endHook(logger, "getSectorListByIndustryList");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getSubSectorListBySectorList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getSubSectorListBySectorList(@RequestBody List<Long> sectorIdList,
			HttpServletRequest request) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getSubSectorListBySectorList");
			if (sectorIdList == null) {

				logger.warn("sectorIdList  Require to get sectors Details ==>" + sectorIdList);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			List<SubSectorListRequest> response = applicantService.getSubSectorList(sectorIdList);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setListData(response);
			CommonDocumentUtils.endHook(logger, "getSubSectorListBySectorList");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/get_graphs/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getGraphs(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getGraphs");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (applicationId == null) {
				logger.warn("applicationId Require to get Graphs Details ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(applicantService.getGraphs(applicationId, userId));
			CommonDocumentUtils.endHook(logger, "getGraphs");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Graphs Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/save_lat_lon", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveLatLon(@RequestBody LongitudeLatitudeRequest applicantRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "saveLatLon");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (applicantRequest.getId() == null) {
				logger.warn("applicationId Require to Save Lat Lon Details ==>" + applicantRequest.getId());
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Successfull updated", HttpStatus.OK.value());
			loansResponse.setData(applicantService.updateLatLong(applicantRequest, userId));
			CommonDocumentUtils.endHook(logger, "saveLatLon");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while Saving LatLon Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/get_lat_lon/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getLatLon(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getLatLon");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (applicationId == null) {
				logger.warn("applicationId Require to get LatLon Details ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Successfull updated", HttpStatus.OK.value());
			loansResponse.setData(applicantService.getLatLonByApplicationAndUserId(applicationId, userId));
			CommonDocumentUtils.endHook(logger, "getLatLon");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while Getting LatLon Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * @RequestMapping(value = "/isMsmeScoreRequired", method = RequestMethod.POST,
	 * produces = MediaType.APPLICATION_JSON_VALUE, consumes =
	 * MediaType.APPLICATION_JSON_VALUE) public ResponseEntity<LoansResponse>
	 * saveIsMsmeScoreREquired(@RequestBody MsmeScoreRequest request,
	 * HttpServletRequest httpRequest, @RequestParam(value = "clientId", required =
	 * false) Long clientId) { try { Long userId = null; if
	 * (CommonUtils.UserType.SERVICE_PROVIDER == ((Integer)
	 * httpRequest.getAttribute(CommonUtils.USER_TYPE)).intValue()) { userId =
	 * clientId; } else { userId = (Long)
	 * httpRequest.getAttribute(CommonUtils.USER_ID); } if
	 * (CommonUtils.isObjectNullOrEmpty(request)) {
	 * logger.warn("request cannot be empty"); return new
	 * ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_REQUEST,
	 * HttpStatus.BAD_REQUEST.value()), HttpStatus.OK); } else{ RatingResponse
	 * ratingResponse = new RatingResponse(); boolean
	 * response=applicantService.updateIsMsmeScoreRequired(request); if(response) {
	 * CompanyDetails
	 * companyDetails=applicantService.getCompanyDetails(request.getApplicationId(),
	 * userId); ratingResponse=ratingClient.getCompanyDetails(companyDetails);
	 * if(CommonUtils.isObjectNullOrEmpty(ratingResponse.getCompanyDetails())){
	 * ratingResponse=ratingClient.saveCompanyDetails(companyDetails); }
	 * LoansResponse loansResponse = new LoansResponse();
	 * loansResponse.setData(response); return new
	 * ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK); } LoansResponse
	 * loansResponse = new LoansResponse(); loansResponse.setData(response); return
	 * new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK); } } catch
	 * (Exception e) { logger.error("Error while getting msme score==>", e); return
	 * new ResponseEntity<LoansResponse>(new
	 * LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
	 * HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.INTERNAL_SERVER_ERROR);
	 * } }
	 */
/*	@RequestMapping(value = "/getMsmeScoreRequired", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getMsmeScoreRequired(@RequestBody Long applicationId,
			HttpServletRequest httpRequest, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.warn("request cannot be empty");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			} else {
				boolean response = applicantService.getIsMsmeScoreRequired(applicationId);
				LoansResponse loansResponse = new LoansResponse();
				loansResponse.setData(response);
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while getting msme score==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	} */

	@RequestMapping(value = "/get_coapplicants/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getCoApplicants(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			Long userId;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (applicationId == null) {
				logger.warn("Application ID Require to get  Co-Applicant Profile Details. Application ID==>"
						+ applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			List<CorporateCoApplicantRequest> coApplicants = applicantService.getCoApplicants(userId, applicationId);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(coApplicants);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting corporate Co-Applicant Profile Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "${profile}/getCorpCoapIds/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getCoapAndGuarIds(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		logger.info("Enter in getCoapAndGuarIds for retail profile");
		Long userId = null;
		if (CommonDocumentUtils.isThisClientApplication(request)) {
			userId = clientId;
		} else {
			userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		}
		if (applicationId == null) {
			logger.warn("ApplicationId Require to get Coap And Guar Ids. Application Id ==>" + applicationId);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(applicantService.getCoapAndGuarIds(userId, applicationId));
			logger.info("Successfully get Coap And Guar Ids for retail profile");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting CoapAndGuarIds==>",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "get_payment_info/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPaymentInfo(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId,@RequestParam(value = "npUserId", required = false) Long npUserId) {
		logger.info("Enter in getCoapAndGuarIds for retail profile");
		Long userId = null;
		if (CommonDocumentUtils.isThisClientApplication(request)) {
			if(CommonUtils.isObjectNullOrEmpty(clientId) && !CommonUtils.isObjectNullOrEmpty(npUserId)){
				userId = npUserId;	
			}else{
				userId = clientId;
			}
			
		} else {
			userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		}
		if (applicationId == null) {
			logger.warn("ApplicationId Require to get Coap And Guar Ids. Application Id ==>" + applicationId);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(applicantService.getPaymentInfor(userId, applicationId));
			logger.info("Successfully get Paymnt Info");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting Payment Info==>",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/getConstitutionByProposalId/{proposalId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getConstitutionByProposalId(@PathVariable("proposalId") Long proposalId,
																		HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "getPrimary");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (proposalId == null || userId == null) {
				logger.warn("ID and User Id Require to get Primary Working Details ==>" + proposalId + "User ID ==>"
						+ userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid Request", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			CorporateApplicantRequest response = applicantService.getCorporateApplicantByProposalId(userId, proposalId);

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(response.getConstitutionId());
			CommonDocumentUtils.endHook(logger, "getPrimary");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting Primary Term Loan Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/getConstitutionByApplicationId/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getConstitutionByApplicationId(@PathVariable("applicationId") Long applicationId,
													HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "getPrimary");
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
			CorporateApplicantRequest response = applicantService.getCorporateApplicant(userId, applicationId);

			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(response.getConstitutionId());
			CommonDocumentUtils.endHook(logger, "getPrimary");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting Primary Term Loan Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/getApplicationClient/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getCorporateApplicantDetails(@PathVariable("applicationId") Long applicationId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getCorporateApplicantDetails()");
			logger.info("In method getCorporateApplicantDetails()");
			if (applicationId == null) {
				logger.info("AppplicationId is NULL so returnig ()");
				logger.warn(
						APPLICATION_ID_REQUIRE_TO_GET_CORPORATE_PROFILE_DETAILS_FOR_CLIENT_APPLICATION_ID_MSG + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			logger.info("Calling Service IMpl Methdos()");

			CorporateApplicantRequest response = applicantService.getCorporateApplicant(applicationId);
			logger.info("Result retruned");
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			logger.info("Loans Response onject creted");
			loansResponse.setData(response);
			logger.info("Data Ste");
			logger.info("response====================================================>{}",response.toString());
			CommonDocumentUtils.endHook(logger, "get");
			logger.info("Returning okay");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_CORPORATE_APPLICANT_PROFILE_DETAILS_MSG, e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/getApplicationClientForEligibility/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getApplicationClientForEligibility(@PathVariable("applicationId") Long applicationId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "get");
			logger.info("In method getApplicationClientForEligibility()");
			if (applicationId == null) {
				logger.info("AppplicationId is NULL so returnig from getApplicationClientForEligibility ()");
				logger.warn(
						APPLICATION_ID_REQUIRE_TO_GET_CORPORATE_PROFILE_DETAILS_FOR_CLIENT_APPLICATION_ID_MSG + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			logger.info("Calling Service IMpl Methdos() from getApplicationClientForEligibility()");

			CorporateApplicantRequest response = applicantService.getCorporateApplicant(applicationId);
			logger.info("Result retruned from getApplicationClientForEligibility()");
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			logger.info("Loans Response onject creted from getApplicationClientForEligibility()");
			loansResponse.setData(response);
			logger.info("Data Set from getApplicationClientForEligibility()");
			CommonDocumentUtils.endHook(logger, "getApplicationClientForEligibility");
			logger.info("Returning okay");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_CORPORATE_APPLICANT_PROFILE_DETAILS_MSG, e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/getOrgAndPanByAppId/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getOrgAndPanByAppId(@PathVariable("applicationId") Long applicationId) {
		logger.info("Enter in getOrgAndPanByAppId method----------------->" + applicationId);
		try {
			CommonDocumentUtils.startHook(logger, "get");
			if (applicationId == null) {
				logger.warn(
						APPLICATION_ID_REQUIRE_TO_GET_CORPORATE_PROFILE_DETAILS_FOR_CLIENT_APPLICATION_ID_MSG + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			JSONObject response = applicantService.getOrgAndPanByAppId(applicationId);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(response);
			CommonDocumentUtils.endHook(logger, "get");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting OrgAndPanByAppId Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
}
