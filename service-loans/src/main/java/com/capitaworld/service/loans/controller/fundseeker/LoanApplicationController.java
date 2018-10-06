
package com.capitaworld.service.loans.controller.fundseeker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

import com.capitaworld.service.gateway.model.GatewayRequest;
import com.capitaworld.service.loans.config.AsyncComponent;
import com.capitaworld.service.loans.config.AuditComponentBankToCW;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.LoanApplicationDetailsForSp;
import com.capitaworld.service.loans.model.LoanApplicationRequest;
import com.capitaworld.service.loans.model.LoanDisbursementRequest;
import com.capitaworld.service.loans.model.LoanSanctionRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.PaymentRequest;
import com.capitaworld.service.loans.model.common.AutoFillOneFormDetailRequest;
import com.capitaworld.service.loans.model.common.DisbursementRequest;
import com.capitaworld.service.loans.model.common.EkycRequest;
import com.capitaworld.service.loans.model.mobile.MobileLoanRequest;
import com.capitaworld.service.loans.service.common.AutoFillOneFormDetailService;
import com.capitaworld.service.loans.service.common.FsDetailsForPdfService;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.service.sanction.LoanDisbursementService;
import com.capitaworld.service.loans.service.sanction.LoanSanctionService;
import com.capitaworld.service.loans.service.token.TokenService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonNotificationUtils.NotificationTemplate;
import com.capitaworld.service.loans.utils.CommonUtility;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.matchengine.ProposalDetailsClient;
import com.capitaworld.service.matchengine.model.ProposalMappingRequest;
import com.capitaworld.service.notification.utils.NotificationAlias;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;
import com.capitaworld.sidbi.integration.model.GenerateTokenRequest;
import com.capitaworld.sidbi.integration.model.ProfileReqRes;
import com.capitaworld.sidbi.integration.util.AESEncryptionUtility;

@RestController
@RequestMapping("/loan_application")
public class LoanApplicationController {

	private static final Logger logger = LoggerFactory.getLogger(LoanApplicationController.class);

	@Autowired
	private LoanApplicationService loanApplicationService;

	@Autowired
	private AsyncComponent asyncComponent;

	@Autowired
	private UsersClient usersClient;

	@Autowired
	private FsDetailsForPdfService fsDetailPdfService;

	@Autowired
	private AutoFillOneFormDetailService autoFillOneFormDetailService;

	@Autowired
	private LoanSanctionService loanSanctionService;

	@Autowired
	private LoanDisbursementService loanDisbursementService;

	@Autowired
	private ProposalDetailsClient proposalDetailsClient;

	@Autowired
	private AuditComponentBankToCW auditComponentBankToCW;

	@Autowired
	private TokenService tokenService;

	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public String getPing() {
		logger.info("Ping success");
		return "Ping Succeed";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody FrameRequest commonRequest, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			// request must not be null
			CommonDocumentUtils.startHook(logger, "save");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

			if (userId == null) {
				logger.warn("userId  can not be empty ==>" + userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			commonRequest.setUserId(userId);
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				commonRequest.setClientId(clientId);
			}

			// ==============

			loanApplicationService.saveOrUpdate(commonRequest, userId);
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving applicationRequest Details ==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/saveFromLoanEligibility", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveFromLoanEligibility(@RequestBody FrameRequest commonRequest,
			HttpServletRequest request) {
		try {
			// request must not be null
			CommonDocumentUtils.startHook(logger, "save");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

			if (userId == null) {
				logger.warn("userId  can not be empty ==>" + userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			commonRequest.setUserId(userId);

			loanApplicationService.saveOrUpdateFromLoanEligibilty(commonRequest, userId);
			CommonDocumentUtils.endHook(logger, "save");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while saving applicationRequest Details ==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/get/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> get(@PathVariable("id") Long id, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "get");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (id == null || userId == null) {
				logger.warn("ID And UserId Require to get Loan Application Details. ID==>" + id + " and UserId==>"
						+ userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			LoanApplicationRequest response = loanApplicationService.get(id, userId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(response);
			CommonDocumentUtils.endHook(logger, "get");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getIrrByApplicationId/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getIrrByApplicationId(@PathVariable("id") Long id, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getIrrByApplicationId");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (id == null || userId == null) {
				logger.warn("ID And UserId Require to get Loan Application Details. ID==>" + id + " and UserId==>"
						+ userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			Long response = loanApplicationService.getIrrByApplicationId(id);
			LoansResponse loansResponse = null;
			if (response != null) {
				loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
				loansResponse.setData(response);
				CommonDocumentUtils.endHook(logger, "get");
			}
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getList(HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getList");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (userId == null) {
				logger.warn("UserId Require to get Loan Applications Details ==>" + userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			List<LoanApplicationRequest> response = loanApplicationService.getList(userId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(response);
			CommonDocumentUtils.endHook(logger, "getList");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/delete/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> inActive(@PathVariable("id") Long id, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "inActive");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (userId == null) {
				logger.warn("UserId Require to Inactive Loan Application Details ==>" + id);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Inactivated", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.inActive(id, userId));
			CommonDocumentUtils.endHook(logger, "inActive");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getListByUserIdList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getListByUseIdList(@RequestBody Long userId, HttpServletRequest request) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getListByUseIdList");
			if (userId == null) {
				logger.warn("UserId Require to get Loan Applications Details ==>" + userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			List<LoanApplicationDetailsForSp> response = loanApplicationService.getLoanDetailsByUserIdList(userId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(response);
			CommonDocumentUtils.endHook(logger, "getListByUseIdList");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/set_last_application_access/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> setLastApplicationAccess(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "setLastApplicationAccess");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (applicationId == null) {
				logger.warn("applicationId Require to Set last Access Profile ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			UserResponse userResponse = loanApplicationService.setLastAccessApplication(applicationId, userId);
			if (userResponse != null && userResponse.getStatus() == 200) {
				LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
				loansResponse.setData(userResponse);
				CommonDocumentUtils.endHook(logger, "setLastApplicationAccess");
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "setLastApplicationAccess");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while Setting Last Access profile==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/get_product_by_application/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getProductByApplication(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getProductByApplication");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if (applicationId == null) {
				logger.warn("applicationId Require to get Product Id ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			Integer productId = loanApplicationService.getProductIdByApplicationId(applicationId, userId);
			if (!CommonUtils.isObjectNullOrEmpty(productId)) {
				LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
				loansResponse.setId(productId.longValue());
				logger.info("End getProductByApplication() method");
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} else {
				logger.info("End getProductByApplication() method");
				logger.warn("ProductId not found");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while Getting Product Id by Application Id==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUserIdByApplicationId", method = RequestMethod.POST)
	public ResponseEntity<LoansResponse> getUserIdByApplicationId(HttpServletRequest request,
			@RequestBody Long applicationId) {

		try {
			CommonDocumentUtils.startHook(logger, "getUserIdByApplicationId");
			if (applicationId == null) {
				logger.warn("applicationId Require to get Loan Applications Details ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			LoansResponse loansResponse = new LoansResponse();

			Object[] response = loanApplicationService.getApplicationDetailsById(applicationId);
			if (!CommonUtils.isObjectNullOrEmpty(response[0])) {
				loansResponse.setData(response[0]);
			}
			CommonDocumentUtils.endHook(logger, "getUserIdByApplicationId");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while Getting Product Id by Application Id==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUserNameByApplicationId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUserNameByApplicationId(HttpServletRequest request,
			@RequestBody Long applicationId) {

		try {
			CommonDocumentUtils.startHook(logger, "getUserNameByApplicationId");
			if (applicationId == null) {
				logger.warn("applicationId Require to get Loan Applications Details ==>" + applicationId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			LoansResponse loansResponse = new LoansResponse();

			String nameResponse = loanApplicationService.getFsApplicantName(applicationId);
			loansResponse.setData(nameResponse);
			CommonDocumentUtils.endHook(logger, "getUserNameByApplicationId");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while Getting Product Id by Application Id==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/lock_primary/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> lockPrimary(@PathVariable("applicationId") Long applicationId,
			@RequestParam(value = "clientId", required = false) Long clientId, HttpServletRequest request) {
		try {
			CommonDocumentUtils.startHook(logger, "lockPrimary");
			Long userId = null;
			Integer userType = ((Integer) request.getAttribute(CommonUtils.USER_TYPE)).intValue();
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.warn("applicationId Must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			loanApplicationService.lockPrimary(applicationId, userId, true);
			if (CommonUtils.UserType.FUND_SEEKER == userType) {
				asyncComponent.sentMailWhenUserLogoutWithoutFillingFinalData(userId, applicationId);
			}
			CommonDocumentUtils.endHook(logger, "lockPrimary");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully updated", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while locking primary information==>" + e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/lock_final/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> lockFinal(@PathVariable("applicationId") Long applicationId,
			@RequestParam(value = "clientId", required = false) Long clientId, HttpServletRequest request) {
		try {
			CommonDocumentUtils.startHook(logger, "lockFinal");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.warn("applicationId Must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoanApplicationRequest loanApplicationRequest = loanApplicationService.lockFinal(applicationId, userId,
					true);
			if (loanApplicationRequest.getIsMailSent()) {
				asyncComponent.sendEmailWhenMakerLockFinalDetails(loanApplicationRequest.getNpAssigneeId(),
						loanApplicationRequest.getNpUserId(), loanApplicationRequest.getApplicationCode(),
						loanApplicationRequest.getProductId(), loanApplicationRequest.getName(),
						loanApplicationRequest.getId());
			}
			CommonDocumentUtils.endHook(logger, "lockFinal");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully updated", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while Locking final information==>" + e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/update_final_information_flag/{applicationId}/{flag}/{finalFilledCount}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateFinalInformationFlag(@PathVariable("applicationId") Long applicationId,
			@PathVariable("flag") Boolean flag, @PathVariable("finalFilledCount") String finalFilledCount,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "updateFinalInformationFlag");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.error("Application id must not be null.");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid data or Requested data not found.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			loanApplicationService.updateFinalCommonInformation(applicationId, userId, flag, finalFilledCount);
			CommonDocumentUtils.endHook(logger, "updateFinalInformationFlag");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/primary_profile_filled/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isProfileAndPrimaryFilled(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "isProfileAndPrimaryFilled");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.error("Application id must not be null.");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid data or Requested data not found.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Success Result", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.isProfileAndPrimaryDetailFilled(applicationId, userId));
			CommonDocumentUtils.endHook(logger, "isProfileAndPrimaryFilled");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/primary_locked/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isPrimaryLocked(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "isPrimaryLocked");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.error("Application id must not be null.");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid data or Requested data not found.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Success Result", HttpStatus.OK.value());

			loansResponse.setData(true);

			if (!loanApplicationService.isApplicationIdActive(applicationId)) {
				loansResponse.setData(false);
				loansResponse.setMessage("Requested user is Inactive");
				CommonDocumentUtils.endHook(logger, "isPrimaryLocked");
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
			if (!loanApplicationService.isPrimaryLocked(applicationId, userId)) {
				loansResponse.setData(false);
				loansResponse.setMessage("Requested User has not filled Primary Details");
				CommonDocumentUtils.endHook(logger, "isPrimaryLocked");
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}

			CommonDocumentUtils.endHook(logger, "isPrimaryLocked");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/final_filled/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isFinalFilled(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "isFinalFilled");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.error("Application id must not be null.");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid data or Requested data not found.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Success Result", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.isFinalDetailFilled(applicationId, userId));
			CommonDocumentUtils.endHook(logger, "isFinalFilled");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/final_locked/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isFinalLocked(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "isFinalLocked");
			Long userId = null;
			Integer userType = ((Integer) request.getAttribute(CommonUtils.USER_TYPE)).intValue();
			if ((CommonUtils.UserType.SERVICE_PROVIDER == userType || CommonUtils.UserType.NETWORK_PARTNER == userType
					|| CommonUtils.UserType.FUND_PROVIDER == userType) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.error("Application id must not be null.");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid data or Requested data not found.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Success Result", HttpStatus.OK.value());
			loansResponse.setData(true);
			if (!loanApplicationService.isApplicationIdActive(applicationId)) {
				loansResponse.setData(false);
				loansResponse.setMessage("Requested user is Inactive");
				CommonDocumentUtils.endHook(logger, "isFinalLocked");
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
			if (!loanApplicationService.isFinalLocked(applicationId, userId)) {
				loansResponse.setData(loanApplicationService.isFinalLocked(applicationId, userId));
				loansResponse.setMessage("Requested User has not filled Final Details");
				if (CommonUtils.UserType.FUND_PROVIDER == userType) {
					logger.info("Start Sending Mail To Fs for Fill Final Details When FP Click View More Details");
					asyncComponent.sendMailWhenUserNotCompleteFinalDetails(userId, applicationId);
				}
				CommonDocumentUtils.endHook(logger, "isFinalLocked");
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
			CommonDocumentUtils.endHook(logger, "isFinalLocked");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/primary_final_locked/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isPrimaryAndFinalLocked(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "isPrimaryAndFinalLocked");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.error("Application id must not be null.");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid data or Requested data not found.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			JSONObject json = new JSONObject();
			json.put("isPrimaryLock", loanApplicationService.isPrimaryLocked(applicationId, userId));
			json.put("isFinalLock", loanApplicationService.isFinalLocked(applicationId, userId));
			LoansResponse loansResponse = new LoansResponse("Success Result", HttpStatus.OK.value());
			loansResponse.setData(json);
			CommonDocumentUtils.endHook(logger, "isPrimaryAndFinalLocked");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/get_selfview_primary_locked/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getSelfViewAndPrimaryLocked(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getSelfViewAndPrimaryLocked");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			/*
			 * if (CommonDocumentUtils.isThisClientApplication(request) &&
			 * !CommonUtils.isObjectNullOrEmpty(clientId)) { userId = clientId; } else {
			 * userId = (Long) request.getAttribute(CommonUtils.USER_ID); }
			 */

			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.error("Application id must not be null.");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid data or Requested data not found.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Success Result", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getSelfViewAndPrimaryLocked(applicationId, userId));
			CommonDocumentUtils.endHook(logger, "getSelfViewAndPrimaryLocked");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/currency_denomination/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getCurrencyAndDenomination(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getCurrencyAndDenomination");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (applicationId == null || userId == null) {
				logger.warn("ID And UserId Require to get Primary Working Details ==>" + applicationId
						+ " and UserId ==>" + userId);
				CommonDocumentUtils.endHook(logger, "getCurrencyAndDenomination");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getCurrencyAndDenomination(applicationId, userId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting Primary Working Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/is_allow_to_move_ahead/{applicationId}/{tabType}/{coAppllicantOrGuarantorId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isAllowToMoveAhead(@PathVariable("applicationId") Long applicationId,
			@PathVariable("tabType") Integer tabType,
			@PathVariable("coAppllicantOrGuarantorId") Long coAppllicantOrGuarantorId, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "isAllowToMoveAhead");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (applicationId == null || userId == null) {
				logger.warn("ID And UserId Require to get Primary Working Details ==>" + applicationId
						+ " and UserId ==>" + userId);
				CommonDocumentUtils.endHook(logger, "isAllowToMoveAhead");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.isAllowToMoveAhead(applicationId, userId, tabType,
					coAppllicantOrGuarantorId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getting Primary Working Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getBowlCount/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getBowlCount(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getBowlCount");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (applicationId == null || userId == null) {
				logger.warn("ID And UserId Require to get Bowl Count Details ==>" + applicationId + " and UserId ==>"
						+ userId);
				CommonDocumentUtils.endHook(logger, "getBowlCount");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getBowlCount(applicationId, userId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getBowlCount Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUsersRegisteredLoanDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUsersRegisteredLoanDetails(@RequestBody MobileLoanRequest loanRequest) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getLoanDetailsForSignUpUserList");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getUsersRegisteredLoanDetails(loanRequest));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getLoanDetailsForSignUpUserList==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getLoanDetailsForAdminPanel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getLoanDetailsForAdminPanel(@RequestBody MobileLoanRequest loanRequest) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getLoanDetailsForAdminPanel");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getLoanDetailsForAdminPanel(1, loanRequest));

			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getLoanDetailsForAdminPanel==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getFilledLoanDetailsForAdminPanel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFilledLoanDetailsForAdminPanel(@RequestBody MobileLoanRequest loanRequest) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getFilledLoanDetailsForAdminPanel");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getLoanDetailsForAdminPanel(2, loanRequest));

			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getFilledLoanDetailsForAdminPanel==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUbiReport1ForAdminPanel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUbiReport1ForAdminPanel(@RequestBody MobileLoanRequest loanRequest) {
		try {
			CommonDocumentUtils.startHook(logger, "getUbiReport1ForAdminPanel");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getPostLoginForAdminPanel(loanRequest));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getUbiReport1ForAdminPanel==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUbiReport2ForAdminPanel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUbiReport2ForAdminPanel(@RequestBody MobileLoanRequest loanRequest) {
		try {
			CommonDocumentUtils.startHook(logger, "getUbiReport1ForAdminPanel");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getPostLoginForAdminPanelOfNotEligibility(loanRequest));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getUbiReport1ForAdminPanel==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUbiReport3ForAdminPanel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUbiReport3ForAdminPanel(@RequestBody MobileLoanRequest loanRequest) {
		try {
			CommonDocumentUtils.startHook(logger, "getUbiReport1ForAdminPanel");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getPostLoginForAdminPanelOfEligibility(loanRequest));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getUbiReport1ForAdminPanel==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUbiReport4ForAdminPanel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUbiReport4ForAdminPanel(@RequestBody MobileLoanRequest loanRequest) {
		try {
			CommonDocumentUtils.startHook(logger, "getUbiReport1ForAdminPanel");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(
					loanApplicationService.getPostLoginForAdminPanelOfFinalLockedRejectedByUbi(loanRequest));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getUbiReport1ForAdminPanel==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUbiReport5ForAdminPanel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUbiReport5ForAdminPanel(@RequestBody MobileLoanRequest loanRequest) {
		try {
			CommonDocumentUtils.startHook(logger, "getUbiReport1ForAdminPanel");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getPostLoginForAdminPanelOfApprovedByUbi(loanRequest));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getUbiReport1ForAdminPanel==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getChatListByFpMappingId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getChatListByFpMappingId(HttpServletRequest request,
			@RequestBody Long applicationId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getChatListByFpMappingId");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getChatListByApplicationId(applicationId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getChatListByFpMappingId==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getFpNegativeList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFpNegativeList(HttpServletRequest request,
			@RequestBody Long applicationId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getFpNegativeList");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(loanApplicationService.getFpNegativeList(applicationId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getFpNegativeList==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getDetailsForEkycAuthentication", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getDetailsForEkycAuthentication(HttpServletRequest request,
			@RequestBody EkycRequest ekycRequest) {

		// request must not be null
		try {
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getDetailsForEkycAuthentication(ekycRequest));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Something went wrong", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/getMcaCompanyId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getMcaCompanyId(HttpServletRequest request, @RequestBody Long applicationId,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "getMcaCompanyId");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getMcaCompanyId(applicationId, userId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getMcaCompanyId==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/updateLoanApplication", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateLoanApplication(@RequestBody LoanApplicationRequest loanRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "updateLoanApplication");
			Long userId = null;
			if ((!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_TYPE)))
					&& (CommonUtils.UserType.SERVICE_PROVIDER == Integer
							.parseInt(request.getAttribute(CommonUtils.USER_TYPE).toString())
							|| CommonUtils.UserType.NETWORK_PARTNER == ((Integer) request
									.getAttribute(CommonUtils.USER_TYPE)).intValue()
							|| CommonUtils.UserType.FUND_PROVIDER == ((Integer) request
									.getAttribute(CommonUtils.USER_TYPE)).intValue())) {
				loanRequest.setClientId(clientId);
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			} else {
				if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
					userId = (Long) request.getAttribute(CommonUtils.USER_ID);
				} else if (!CommonUtils.isObjectNullOrEmpty(loanRequest.getUserId())) {
					userId = loanRequest.getClientId();
				} else {
					logger.warn("Invalid request.");
					return new ResponseEntity<LoansResponse>(
							new LoansResponse("Invalid request.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
				}
			}
			loanRequest.setUserId(userId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loanApplicationService.updateLoanApplication(loanRequest);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updateLoanApplication==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/isMca", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isMca(HttpServletRequest request, @RequestBody Long applicationId,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "getMcaCompanyId");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.isMca(applicationId, userId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getMcaCompanyId==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getLoanBasicDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getLoanBasicDetails(@RequestBody LoanApplicationRequest loanRequest) {
		try {
			CommonDocumentUtils.startHook(logger, "getLoanBasicDetails");
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse
					.setData(loanApplicationService.getLoanBasicDetails(loanRequest.getId(), loanRequest.getUserId()));
			CommonDocumentUtils.endHook(logger, "getLoanBasicDetails");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getLoanBasicDetails==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create_loan_from_campaign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> createLoanFromCampaign(HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId,
			@RequestParam(value = "isActive", required = false) Boolean isActive,
			@RequestParam(value = "businessType", required = false) Integer busineeTypeId,
			@RequestBody List<String> campaignCodes) {
		try {
			logger.info("start createLoanFromCampaign()");
			logger.info("Business Type Id====>{}", busineeTypeId);
			logger.info("isActive====>{}", isActive);
			logger.info("clientId====>{}", clientId);
			logger.info("campaignCodes====>{}", campaignCodes.toString());

			boolean isMsmeUserFromGeneric = false;
			try {
				logger.info("Campaign Code=====>{}", campaignCodes);
				if (!CommonUtils.isListNullOrEmpty(campaignCodes)) {
					isMsmeUserFromGeneric = campaignCodes.contains(CommonUtils.CampaignCodes.ALL1MSME.getValue());
					logger.info("codeExist====>{}", isMsmeUserFromGeneric);
					if (isMsmeUserFromGeneric) {
						// In this case
						Long createdId =null;
						if (CommonUtils.BusinessType.NEW_TO_BUSINESS.getId() == busineeTypeId ||
								CommonUtils.BusinessType.EXISTING_BUSINESS.getId() == busineeTypeId) {
							createdId = loanApplicationService.createMsmeLoan(clientId, isActive, busineeTypeId);
						}else if(CommonUtils.BusinessType.RETAIL_PERSONAL_LOAN.getId() == busineeTypeId){
							createdId = loanApplicationService.createRetailLoan(clientId, isActive, busineeTypeId);
						}

						return new ResponseEntity<LoansResponse>(
								new LoansResponse(createdId, "Successfully New Loan Created", HttpStatus.OK.value()),
								HttpStatus.OK);

					}
					// Integer index = campaignCodes
					// .indexOf(CommonUtils.CampaignCodes.ALL1MSME.getValue());
					// logger.info("index==={}=of Code====>{}", index,
					// CommonUtils.CampaignCodes.ALL1MSME.getValue());
					// if (index > -1) {
					// }
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error while Set Campaign Code to LoanApplication Master");
			}

			JSONObject json = new JSONObject();
			CommonDocumentUtils.startHook(logger, "createLoanFromCampaign");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			LoansResponse loansResponse = new LoansResponse("Success", HttpStatus.OK.value());
			Long finalUserId = CommonUtils.isObjectNullOrEmpty(clientId) ? userId : clientId;
			for (String campaignCode : campaignCodes) {
				if (!CommonUtils.CampaignCodes.ALL1MSME.getValue().equals(campaignCode)) {
					boolean campaignCodeExist = loanApplicationService.isCampaignCodeExist(userId, clientId,
							campaignCode);
					if (campaignCodeExist) {
						logger.info("Campaign code is already Exists==>" + campaignCode);
					} else {
						LoanApplicationRequest request2 = loanApplicationService.saveFromCampaign(userId, clientId,
								campaignCode);
						json.put("id", request2.getId());
						json.put("productId", request2.getProductId());
						json.put("hasAlreadyApplied", loanApplicationService.hasAlreadyApplied(finalUserId,
								request2.getId(), request2.getProductId()));
						json.put("isNew", true);
					}
				}
			}

			if (json.isEmpty()) {
				UsersRequest usersRequest = new UsersRequest();
				usersRequest.setId(userId);
				UserResponse response = usersClient.getLastAccessApplicant(usersRequest);
				if (!CommonUtils.isObjectNullOrEmpty(response) && !CommonUtils.isObjectNullOrEmpty(response.getId())) {
					Integer productId = loanApplicationService.getProductIdByApplicationId(response.getId(), userId);
					json.put("id", response.getId());
					json.put("productId", productId);
					json.put("hasAlreadyApplied",
							loanApplicationService.hasAlreadyApplied(finalUserId, response.getId(), productId));
					json.put("isNew", false);
				}
			}
			json.put("msmeGeneric", isMsmeUserFromGeneric);
			loansResponse.setData(json);

			logger.info("end createLoanFromCampaign()");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while creating Loan from Campaign==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/checkUserHasAnyApplication", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> checkUserHasAnyApplication(HttpServletRequest request) {
		try {
			CommonDocumentUtils.startHook(logger, "check User Has Any Application");
			if (CommonUtils.UserType.FUND_SEEKER == ((Integer) request.getAttribute(CommonUtils.USER_TYPE))
					.intValue()) {
				Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
				asyncComponent.sendMailWhenUserHasNoApplication(userId);
			}
			LoansResponse loansResponse = new LoansResponse("Successfully recieved", HttpStatus.OK.value());
			CommonDocumentUtils.endHook(logger, "check User Has Any Application");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while check User Has Any Application==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * This is method call when fund seeker submit primary details and then go to
	 * matches This called from matchesPopupCtrl.js controller
	 * 
	 * @param applicationId
	 * @param request
	 * @param clientId
	 * @author harshit
	 * @return
	 */
	@RequestMapping(value = "/sendMailForFirstTimeUserViewMatches/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> sendMailForFirstTimeUserViewMatches(
			@PathVariable("applicationId") Long applicationId, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "sendMailForFirstTimeUserViewMatches");
			Long userId = null;
			if (CommonUtils.UserType.FUND_SEEKER == ((Integer) request.getAttribute(CommonUtils.USER_TYPE))
					.intValue()) {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			} else {
				logger.info("Logged user not fundseeker in sendMailForFirstTimeUserViewMatches method");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Requested user is not fundseeker", HttpStatus.OK.value()), HttpStatus.OK);
			}
			if (CommonUtils.isObjectNullOrEmpty(applicationId) || CommonUtils.isObjectNullOrEmpty(userId)) {
				logger.warn("ID And UserId Require to send mail for first time user view matches==>" + applicationId
						+ " and UserId ==>" + userId);
				CommonDocumentUtils.endHook(logger, "sendMailForFirstTimeUserViewMatches");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			asyncComponent.sendMailForFirstTimeUserViewMatches(applicationId, userId);
			logger.info("Mail sent successfully while fs go in matches pages first time");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Sent Mail Successfully", HttpStatus.OK.value()),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while sendMailForFirstTimeUserViewMatches Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/isTermLoanLessThanLimit/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isTermLoanLessThanLimit(HttpServletRequest request,
			@PathVariable Long applicationId, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "isTermLoanLessThanLimit");

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.isTermLoanLessThanLimit(applicationId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while isTermLoanLessThanLimit==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/isApplicationEligibleForIrr/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isApplicationEligibleForIrr(HttpServletRequest request,
			@PathVariable Long applicationId, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "isApplicationEligibleForIrr");

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.isApplicationEligibleForIrr(applicationId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while isApplicationEligibleForIrr==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getIndustryIrrByApplication/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getIndustryIrrByApplication(HttpServletRequest request,
			@PathVariable Long applicationId, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "getIndustryIrrByApplication");

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getIndustryIrrByApplication(applicationId));
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getIndustryIrrByApplication==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getFsDetailsInMapResponse/{applicationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFsDetailsInMapResponse(@PathVariable("applicationId") Long applicationId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "getFsDetailsInMapResponse");
			LoansResponse loansResponse = new LoansResponse("Successfully recieved", HttpStatus.OK.value());
			loansResponse.setData(fsDetailPdfService.getHomeLoanDetails(applicationId));
			CommonDocumentUtils.endHook(logger, "check User Has Any Application");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error whilegetFsDetailsInMapResponse==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/set_eligibility_amount", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Integer setEligibilityAmount(@RequestBody LoanApplicationRequest loanRequest) {
		try {
			CommonDocumentUtils.startHook(logger, "setEligibilityAmount");
			return loanApplicationService.setEligibleLoanAmount(loanRequest);
		} catch (Exception e) {
			logger.error("Error while updating Eligibility Amount==>", e);
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = "/getLoanDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getLoanDetails(@RequestBody Long applicationId, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			CommonDocumentUtils.startHook(logger, "getLoanDetails");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getLoanBasicDetails(applicationId, userId));
			CommonDocumentUtils.endHook(logger, "getLoanDetails");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getLoanDetails==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/update_flow/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateFlow(HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId,
			@PathVariable("applicationId") Long applicationId) {
		try {
			logger.info("start updateFlow()");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			loanApplicationService.updateFlow(applicationId, clientId, userId);
			logger.info("end updateFlow()");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Success", HttpStatus.OK.value()),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updating Flow from UBI to Normal==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/save_payment_info", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> savePaymentInfor(@RequestBody PaymentRequest paymentRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			logger.info("start save_payment_info()");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				logger.info("User id from front end===>" + request.getAttribute(CommonUtils.USER_ID));
				if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
					userId = (Long) request.getAttribute(CommonUtils.USER_ID);
					logger.info("User id from browser===>" + userId);

				} else {
					userId = paymentRequest.getUserId();

					logger.info("User id from mobile===>" + userId);
				}
			}

			if (CommonUtils.isObjectNullOrEmpty(paymentRequest.getApplicationId())) {
				logger.info("Application id is null or empty");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid Request, Application Id Null Or Empty",
								HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Object applicationMaster = loanApplicationService.updateLoanApplicationMaster(paymentRequest, userId);
			logger.info("Response========>{}", applicationMaster);

			try {
				if (CommonUtils.PaymentMode.ONLINE.equalsIgnoreCase(paymentRequest.getTypeOfPayment())
						&& paymentRequest.getPurposeCode().equals("NHBS_FEES")) {
					logger.info("Start Sent Mail When FS select Online Payment");
					asyncComponent.sendMailWhenFSSelectOnlinePayment(userId, paymentRequest,
							NotificationTemplate.EMAIL_FS_PAYMENT_ONLINE, NotificationAlias.SYS_FS_PAYMENT_ONLINE);
					logger.info("End Sent Mail When FS select Online Payment");
				} else if (CommonUtils.PaymentMode.CASH.equalsIgnoreCase(paymentRequest.getTypeOfPayment())) {
					logger.info("Start Sent Mail When FS select CASH Payment");
					asyncComponent.sendMailWhenFSSelectOnlinePayment(userId, paymentRequest,
							NotificationTemplate.EMAIL_FS_PAYMENT_CASH_CHEQUE,
							NotificationAlias.SYS_FS_PAYMENT_CASH_CHEQUE);
					logger.info("End Sent Mail When FS select CASH Payment");
				} else if (CommonUtils.PaymentMode.CHEQUE.equalsIgnoreCase(paymentRequest.getTypeOfPayment())) {
					logger.info("Start Sent Mail When FS select CHEQUE Payment");
					asyncComponent.sendMailWhenFSSelectOnlinePayment(userId, paymentRequest,
							NotificationTemplate.EMAIL_FS_PAYMENT_CASH_CHEQUE,
							NotificationAlias.SYS_FS_PAYMENT_CASH_CHEQUE);
					logger.info("End Sent Mail When FS select CHEQUE Payment");
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Throw Exception while send mail when save payment ");
			}

			LoansResponse response = new LoansResponse("Success", HttpStatus.OK.value());
			response.setData(applicationMaster);
			logger.info("end save_payment_info()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while Saving Payment info==>{}", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/save_payment_info_for_mobile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> savePaymentInforForMobile(@RequestBody PaymentRequest paymentRequest) {
		try {
			logger.info("start save_payment_info()");
			Long userId = paymentRequest.getUserId();

			if (CommonUtils.isObjectNullOrEmpty(paymentRequest.getApplicationId())) {
				logger.info("Application id is null or empty");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Invalid Request, Application Id Null Or Empty",
								HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Object applicationMaster = loanApplicationService.updateLoanApplicationMaster(paymentRequest, userId);
			logger.info("Response========>{}", applicationMaster);

			try {
				if (CommonUtils.PaymentMode.ONLINE.equalsIgnoreCase(paymentRequest.getTypeOfPayment())
						&& paymentRequest.getPurposeCode().equals("NHBS_FEES")) {
					logger.info("Start Sent Mail When FS select Online Payment");
					asyncComponent.sendMailWhenFSSelectOnlinePayment(userId, paymentRequest,
							NotificationTemplate.EMAIL_FS_PAYMENT_ONLINE, NotificationAlias.SYS_FS_PAYMENT_ONLINE);
					logger.info("End Sent Mail When FS select Online Payment");
				} else if (CommonUtils.PaymentMode.CASH.equalsIgnoreCase(paymentRequest.getTypeOfPayment())) {
					logger.info("Start Sent Mail When FS select CASH Payment");
					asyncComponent.sendMailWhenFSSelectOnlinePayment(userId, paymentRequest,
							NotificationTemplate.EMAIL_FS_PAYMENT_CASH_CHEQUE,
							NotificationAlias.SYS_FS_PAYMENT_CASH_CHEQUE);
					logger.info("End Sent Mail When FS select CASH Payment");
				} else if (CommonUtils.PaymentMode.CHEQUE.equalsIgnoreCase(paymentRequest.getTypeOfPayment())) {
					logger.info("Start Sent Mail When FS select CHEQUE Payment");
					asyncComponent.sendMailWhenFSSelectOnlinePayment(userId, paymentRequest,
							NotificationTemplate.EMAIL_FS_PAYMENT_CASH_CHEQUE,
							NotificationAlias.SYS_FS_PAYMENT_CASH_CHEQUE);
					logger.info("End Sent Mail When FS select CHEQUE Payment");
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Throw Exception while send mail when save payment ");
			}

			LoansResponse response = new LoansResponse("Success", HttpStatus.OK.value());
			response.setData(applicationMaster);
			logger.info("end save_payment_info()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while Saving Payment info==>{}", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/update_payment_status", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updatePaymentStatus(@RequestBody PaymentRequest paymentRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			logger.info("start updatePaymentStatus()");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			LoansResponse response = new LoansResponse("Success", HttpStatus.OK.value());
			response.setData(loanApplicationService.updateLoanApplicationMasterPaymentStatus(paymentRequest, userId));
			logger.info("end updatePaymentStatus()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updating Payment Status==>{}", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/update_payment_status_sidbi", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updatePaymentStatusForSidbi(@RequestBody PaymentRequest paymentRequest) {
		try {
			logger.info("start updatePaymentStatus from SIDBI");

			LoansResponse response = new LoansResponse("Success", HttpStatus.OK.value());
			response.setData(loanApplicationService.updateLoanApplicationMasterPaymentStatus(paymentRequest,
					paymentRequest.getUserId()));
			logger.info("end updatePaymentStatus from SIDBI()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updating Payment Status from SIDBI==>{}", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/get_payment_status", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPaymentStatus(@RequestBody PaymentRequest paymentRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			logger.info("start getPaymentStatus()");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			GatewayRequest paymentStatus = loanApplicationService.getPaymentStatus(paymentRequest, userId, clientId);
			logger.info("Response========>{}", paymentStatus);
			LoansResponse response = new LoansResponse("Success", HttpStatus.OK.value());
			response.setData(paymentStatus);
			logger.info("end getPaymentStatus()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updating Payment Status==>{}", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/update_ddr_status/{applicationId}/{statusId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateDDRStatus(@PathVariable("applicationId") Long applicationId,
			@PathVariable("statusId") Long statusId, @RequestParam(value = "clientId", required = false) Long clientId,
			HttpServletRequest request) {
		try {
			CommonDocumentUtils.startHook(logger, "updateDDRStatus");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (CommonUtils.isObjectNullOrEmpty(statusId)) {
				logger.warn("statusId(Action Id in Workflow) Must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.warn("applicationId Must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			loanApplicationService.updateDDRStatus(applicationId, userId, clientId, statusId);
			CommonDocumentUtils.endHook(logger, "updateDDRStatus");
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully updated", HttpStatus.OK.value()),
					HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while Locking final information==>" + e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/copy_loan", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> copyOrImportLoan(
			@RequestBody AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			@RequestParam(value = "clientId", required = false) Long clientId, HttpServletRequest request) {
		logger.info("=========== Enter in copyOrImportLoan() ==============");
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (CommonUtils.isObjectListNull(autoFillOneFormDetailRequest.getFromApplicationId(),
					autoFillOneFormDetailRequest.getToApplicationId(), autoFillOneFormDetailRequest.getFromProductId(),
					autoFillOneFormDetailRequest.getToProductId())) {
				logger.warn("All parameter must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			autoFillOneFormDetailService.getAndSaveCorporateAutoFillOneFormDateils(userId,
					autoFillOneFormDetailRequest);
			logger.info("==============Exit from copyOrImportLoan()===============");
			return new ResponseEntity<LoansResponse>(
					new LoansResponse("Successfully loan copied", HttpStatus.OK.value()), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while Copyig Loan Information==>" + e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/get_client/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoanApplicationRequest> getClient(@PathVariable("id") Long id) {
		// request must not be null
		try {
			if (id == null) {
				logger.warn("ID Require to get Loan Application Details. ID==>" + id);
				return null;
			}
			CommonDocumentUtils.endHook(logger, "get");
			return new ResponseEntity<LoanApplicationRequest>(loanApplicationService.getFromClient(id), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details from Client==>", e);
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = "/save_phase1_sidbi", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> savePhese1DataToSidbi(
			@RequestBody LoanApplicationRequest loanApplicationRequest, HttpServletRequest request) {
		// request must not be null
		try {

			logger.info("Start savePhese1DataToSidbi()");
			Long userId = null;
			if (CommonUtils.isObjectNullOrEmpty(loanApplicationRequest.getUserId())) {
				if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
					userId = (Long) request.getAttribute(CommonUtils.USER_ID);
				}
			} else {
				userId = loanApplicationRequest.getUserId();
			}
			if (userId == null) {
				logger.warn("UserId must not be null==>");
				return new ResponseEntity<LoansResponse>(new LoansResponse(
						"Invalid User. Please relogin and try again.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (loanApplicationRequest.getId() == null) {
				logger.warn("applicationId must not be null==>");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (loanApplicationRequest.getNpOrgId() == null) {
				logger.warn("Organization Id must not be null==>");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			logger.info("Save Phase one Application id -------------------->" + loanApplicationRequest.getId());
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			boolean isSavePhase1Data = loanApplicationService.savePhese1DataToSidbi(loanApplicationRequest.getId(),
					userId, loanApplicationRequest.getNpOrgId(), loanApplicationRequest.getFpProductId());
			logger.info("Result in savePhese1DataToSidbi== {}", isSavePhase1Data);
			loansResponse.setData(isSavePhase1Data);
			logger.info("End savePhese1DataToSidbi()");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while Saving Phse1 Details of SIDBI==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/save_phase2_sidbi", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> savePhese2DataToSidbi(
			@RequestBody LoanApplicationRequest loanApplicationRequest, HttpServletRequest request) {
		// request must not be null
		try {

			logger.info("Start savePhese2DataToSidbi()");

			Long userId = null;
			if (CommonUtils.isObjectNullOrEmpty(loanApplicationRequest.getUserId())) {
				if (!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ID))) {
					userId = (Long) request.getAttribute(CommonUtils.USER_ID);
				}
			} else {
				userId = loanApplicationRequest.getUserId();
			}
			if (userId == null) {
				logger.warn("UsrId must not be null==>");
				return new ResponseEntity<LoansResponse>(new LoansResponse(
						"Invalid User. Please relogin and try again.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (loanApplicationRequest.getId() == null) {
				logger.warn("applicationId must not be null==>");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (loanApplicationRequest.getNpOrgId() == null) {
				logger.warn("Organization Id must not be null==>");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			boolean isSavePhase2Data = loanApplicationService.savePhese2DataToSidbi(loanApplicationRequest.getId(),
					userId, loanApplicationRequest.getNpOrgId(), loanApplicationRequest.getFpProductId());
			logger.info("Result in savePhese2DataToSidbi== {}", isSavePhase2Data);
			loansResponse.setData(isSavePhase2Data);
			logger.info("End savePhese2DataToSidbi()");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while Saving Phase2 Details of SIDBI==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/getDisbursementDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getDisbursementDetails(@RequestBody DisbursementRequest disbursementRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			logger.info("start getDisbursementDetails()");
			/*
			 * Long userId = null; if (CommonUtils.UserType.SERVICE_PROVIDER == ((Integer)
			 * request.getAttribute(CommonUtils.USER_TYPE)) .intValue() ||
			 * CommonUtils.UserType.NETWORK_PARTNER == ((Integer)
			 * request.getAttribute(CommonUtils.USER_TYPE)) .intValue()) { userId =
			 * clientId; } else { userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			 * }
			 */

			if (CommonUtils.isObjectListNull(disbursementRequest.getApplicationId(),
					disbursementRequest.getProductMappingId())) {
				logger.warn("All parameter must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse response = new LoansResponse("Success", HttpStatus.OK.value());
			response.setData(loanApplicationService.getDisbursementDetails(disbursementRequest));
			logger.info("end getDisbursementDetails()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getDisbursementDetails", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/getDetailsForSanctionPopup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getDetailsForSanctionPopup(
			@RequestBody DisbursementRequest disbursementRequest, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			logger.info("start getDetailsForApproval()");

			if (CommonUtils.isObjectListNull(disbursementRequest.getApplicationId(),
					disbursementRequest.getProductMappingId())) {
				logger.warn("All parameter must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse response = new LoansResponse("Success", HttpStatus.OK.value());

			response.setData(loanApplicationService.getDetailsForSanction(disbursementRequest));
			logger.info("end getDetailsForApproval()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getDetailsForApproval", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/saveDetailsForSanctionPopup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveDetailsForSanctionPopup(
			@RequestBody LoanSanctionRequest loanSanctionRequest, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			logger.info("start getDetailsForApproval()");
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userType = Long.valueOf(request.getAttribute(CommonUtils.USER_TYPE).toString());
			if (userId == null) {
				logger.warn("UsrId must not be null==>");
				return new ResponseEntity<LoansResponse>(new LoansResponse(
						"Invalid User. Please relogin and try again.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			if (userType == null) {
				logger.warn("userType must not be null==>");
				return new ResponseEntity<LoansResponse>(new LoansResponse(
						"Invalid User. Please relogin and try again.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			loanSanctionRequest.setActionBy(userId.toString());

			if (CommonUtils.isObjectListNull(loanSanctionRequest.getApplicationId(), loanSanctionRequest.getBranch(),
					loanSanctionRequest.getOrgId(), loanSanctionRequest.getRoi(),
					loanSanctionRequest.getSanctionAmount(), loanSanctionRequest.getTenure(),
					loanSanctionRequest.getProcessingFee())) {
				logger.warn("All parameter must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse response = new LoansResponse("Success", HttpStatus.OK.value());

			Boolean result = loanSanctionService.saveSanctionDetailFromPopup(loanSanctionRequest);
			logger.info("result of save sanction detail ---------------------{}", result);
			response.setData(result);
			if (!result) {
				response.setMessage("something went wrong while saving sanctioned details");
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			} else {
				ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
				proposalMappingRequest.setId(loanSanctionRequest.getProposalId());
				proposalMappingRequest.setProposalStatusId(loanSanctionRequest.getProposalStatusId());
				proposalMappingRequest.setLastActionPerformedBy(userType);
				proposalMappingRequest.setUserId(userId);
				proposalDetailsClient.changeStatus(proposalMappingRequest);
			}

			logger.info("end getDetailsForApproval()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getDetailsForApproval", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/updateProductDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateProductDetails(@RequestBody LoanApplicationRequest loanRequest) {
		try {
			logger.info("Enter in update product details service--------------------------------->");
			if (CommonUtils.isObjectListNull(loanRequest.getId(), loanRequest.getProductId())) {
				logger.warn("All parameter must not be null");
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.updateProductDetails(loanRequest));
			CommonDocumentUtils.endHook(logger, "updateProductDetails");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updateProductDetails==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/inactive_application/{id}/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> inActiveApplication(@PathVariable("id") Long id,
			@PathVariable("userId") Long userId) {
		// request must not be null
		try {
			logger.info("Entry inActiveApplication");
			logger.info("Application Require to Inactive Loan Application Details ==>" + id);
			LoansResponse loansResponse = new LoansResponse("Inactivated", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.inActiveApplication(id, userId));
			logger.info("Exit inActiveApplication");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while Inactivating Loan Application Details==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/getFpDetailsByFpProductId/{fpProductId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFpDetailsByFpProductId(@PathVariable("fpProductId") Long fpProductId) {
		try {
			logger.info("Entry getFpDetailsByFpProductId");
			LoansResponse loansResponse = new LoansResponse("Inactivated", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getFpDetailsByFpProductId(fpProductId));
			logger.info("Exit getFpDetailsByFpProductId");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getFpDetailsByFpProductId==>", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/getFpDetailsByFpProductMappindId/{fpProductMappingId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFpDetailsByFpProductMappingId(@PathVariable("fpProductMappingId") Long fpProductMappingId) {
		
		try {
			logger.info("ENTER  IN GET FP DETAILS BY PRODUCT MAPPING ID -------------FP PRODUCT MAPPING ID >>>>>>>>>>>"+fpProductMappingId);
			LoansResponse loansResponse = new LoansResponse("DATA FOUND", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.getFpDetailsByFpProductMappingId(fpProductMappingId));
			logger.info("Exit getFpDetailsByFpProductMappingId");
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

			
		} catch (Exception e) {
			logger.error("Error while getFpDetailsByFpProductMappingId==========>", e.getMessage());
			e.printStackTrace();
			
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/saveLoanSanctionDetail", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<LoansResponse> saveLoanSanctionDetail(@RequestBody String encryptedString,
			HttpServletRequest httpServletRequest) {
		LoansResponse loansResponse = null;
		LoanSanctionRequest loanSanctionRequest = null;
		String reason = null;
		Long orgId = null;
		GenerateTokenRequest generateTokenRequest = null;
		String decrypt = null;
		String tokenString = null;
		try {
			logger.info(
					"=============================checking authorized token in saveLoanSanctionDetail(){} ============================= ");
			tokenString = httpServletRequest.getHeader("token");
			if (CommonUtils.isObjectNullOrEmpty(tokenString)) {
				reason = "Token is null";
				loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.UNAUTHORIZED);
			} else {
				if (CommonUtils.isObjectNullOrEmpty((tokenString = tokenService.checkTokenExpiration(tokenString)))) {
					reason = "Token is Expired ";
					loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.UNAUTHORIZED);
				}
			}
			logger.info("----------------Enter in saveLoanSanctionDetail(){} ---------------- ");

			if (encryptedString != null) {

				try {
					decrypt = AESEncryptionUtility.decrypt(encryptedString);
					loanSanctionRequest = MultipleJSONObjectHelper.getObjectFromString(decrypt,
							LoanSanctionRequest.class);

				} catch (Exception e) {
					e.printStackTrace();
					logger.info(
							"Error while Converting Encrypted Object to LoanSanctionRequest  saveLoanSanctionDetail(){} -------------------------> ",
							e.getMessage());
					loansResponse = new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(),
							HttpStatus.OK);
					loansResponse.setData(false);
					reason = "Error while Converting Encrypted Object to LoanSanctionRequest ====>{} > Msg ==>"
							+ e.getMessage();
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}

				if (!CommonUtils.isObjectListNull(loanSanctionRequest, loanSanctionRequest.getAccountNo(),
						loanSanctionRequest.getApplicationId(), loanSanctionRequest.getBranch(),
						loanSanctionRequest.getRoi(), loanSanctionRequest.getSanctionAmount(),
						loanSanctionRequest.getSanctionDate(), loanSanctionRequest.getTenure(),
						loanSanctionRequest.getUserName(), loanSanctionRequest.getPassword(),
						loanSanctionRequest.getReferenceNo(), loanSanctionRequest.getActionBy())) {
					orgId = auditComponentBankToCW.getOrgIdByCredential(loanSanctionRequest.getUserName(),
							loanSanctionRequest.getPassword());
					if (!CommonUtils.isObjectNullOrEmpty(orgId)) {
						reason = loanSanctionService.sanctionRequestValidation(loanSanctionRequest.getApplicationId(), orgId);
						if ("SUCCESS".equalsIgnoreCase(reason)) {
							logger.info("Success msg while saveLoanSanctionDetail() ----------------> msg " + reason);
							reason = null;
							loansResponse = new LoansResponse("Information Successfully Stored ",
									HttpStatus.OK.value());
							loansResponse.setData(loanSanctionService.saveLoanSanctionDetail(loanSanctionRequest));
							logger.info("Exit saveLoanSanctionDetail() ---------------->  msg ==>"
									+ "Information Successfully Stored ");
							return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
						} else {
							logger.info("Failure msg while saveLoanSanctionDetail() ----------------> msg " + reason);
							loansResponse = new LoansResponse(reason, HttpStatus.BAD_REQUEST.value());
							loansResponse.setData(false);
							logger.info("Exit saveLoanSanctionDetail() ----------------> msg ==>" + reason);
							return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
						}
					} else {
						reason = "Invalid Credentials";
						logger.info("Invalid Credentials while saveLoanSanctionDetail() ----------------> orgId "
								+ orgId + " reason  " + reason);
						loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
						loansResponse.setData(false);
						logger.info("================== Exit saveLoanSanctionDetail() =================");
						return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
					}
				} else {
					logger.info(
							"Null in LoanSanctionRequest while saveLoanSanctionDetail() ----------------> LoanSanctionRequest"
									+ loanSanctionRequest);
					loansResponse = new LoansResponse("Mandatory Fields Must Not be Null",
							HttpStatus.BAD_REQUEST.value(), HttpStatus.OK);
					loansResponse.setData(false);
					reason = "Mandatory Fields Must Not be Null while saveLoanSanctionDetail() ==> LoanSanctionRequest ===> "
							+ loanSanctionRequest;
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

				}
			} else {
				logger.info("Null encryptedString saveLoanSanctionDetail() ---------------->encryptedString "
						+ encryptedString);
				loansResponse = new LoansResponse("Mandatory Fields Must Not be Null", HttpStatus.BAD_REQUEST.value(),
						HttpStatus.OK);
				loansResponse.setData(false);
				reason = "Null encryptedString saveLoanSanctionDetail in saveLoanSanctionDetail()  ====> encryptedString  ====> "
						+ encryptedString;
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("Error while saveLoanSanctionDetail()----------------------> ", e);
			e.printStackTrace();
			loansResponse = new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
					HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.OK);
			loansResponse.setData(false);
			reason = "Error while save SanctionDetail in saveLoanSanctionDetail() ==> Msg " + e.getMessage();
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} finally {
			logger.info("Saving Request to DB ===> ");
			generateTokenRequest = new GenerateTokenRequest();
			generateTokenRequest.setToken(tokenString);
			tokenService.setTokenAsExpired(generateTokenRequest);
			auditComponentBankToCW.saveBankToCWReqRes(decrypt != null ? decrypt : encryptedString,
					loanSanctionRequest != null ? loanSanctionRequest.getApplicationId() : null,
					CommonUtility.ApiType.SANCTION, loansResponse, reason, null  , null);
		}
	}

	@RequestMapping(value = "/saveLoanDisbursementDetail", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<LoansResponse> saveLoanDisbursementDetail(@RequestBody String encryptedString,
			HttpServletRequest httpServletRequest) {
		LoansResponse loansResponse = null;
		String reason = null;
		Long orgId = null;
		String decrypt = null;
		LoanDisbursementRequest loanDisbursementRequest = null;
		GenerateTokenRequest generateTokenRequest = null;
		String tokenString = null;
		try {
			logger.info(
					"=============================checking authorized token in saveLoanDisbursementDetail(){} ============================= ");
			tokenString = httpServletRequest.getHeader("token");
			if (CommonUtils.isObjectNullOrEmpty(tokenString)) {
				reason = "Token is null";
				loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.UNAUTHORIZED);
			} else {
				if (CommonUtils.isObjectNullOrEmpty((tokenString = tokenService.checkTokenExpiration(tokenString)))) {
					reason = "Token is Expired ";
					loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.UNAUTHORIZED);
				}
			}
			logger.info("-----------------------------Enter in saveLoanDisbursementDetail(){} --------------------");
			if (encryptedString != null) {

				try {
					decrypt = AESEncryptionUtility.decrypt(encryptedString);
					loanDisbursementRequest = MultipleJSONObjectHelper.getObjectFromString(decrypt,
							LoanDisbursementRequest.class);

				} catch (Exception e) {
					e.printStackTrace();
					logger.info(
							"Error while Converting Encrypted Object to LoanDisbursementRequest  saveLoanDisbursementDetail(){} -------------------------> ",
							e.getMessage());
					loansResponse = new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(),
							HttpStatus.OK);
					loansResponse.setData(false);
					if (CommonUtils.isObjectNullOrEmpty(decrypt)) {
						reason = "ERROR WHILE DECRYPT ENCRYPTED OBJECT   ====> Msg ===> ";
					} else {
						reason = "error while converting decrypt string to profileReqRes ====> Msg ===> ";
					}
					reason += e.getMessage();
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}
				if (!CommonUtils.isObjectListNull(loanDisbursementRequest, loanDisbursementRequest.getApplicationId(),
						loanDisbursementRequest.getDisbursedAmount(), loanDisbursementRequest.getDisbursementDate(),
						loanDisbursementRequest.getPaymentMode(), loanDisbursementRequest.getReferenceNo(),
						loanDisbursementRequest.getActionBy(), loanDisbursementRequest.getAccountNo())) {
					orgId = auditComponentBankToCW.getOrgIdByCredential(loanDisbursementRequest.getUserName(),
							loanDisbursementRequest.getPassword());
					if (!CommonUtils.isObjectNullOrEmpty(orgId)) {

						loanDisbursementRequest = loanDisbursementService.disbursementRequestValidation(null , loanDisbursementRequest, orgId , CommonUtility.ApiType.DISBURSEMENT);

						if ("SUCCESS".equalsIgnoreCase(loanDisbursementRequest.getReason()) || "First Disbursement".equalsIgnoreCase(loanDisbursementRequest.getReason())) {
							logger.info(
									"Success msg while saveLoanDisbursementDetail() ----------------> msg " + reason);
							loanDisbursementRequest = null;
							loansResponse = new LoansResponse("Information Successfully Stored ",
									HttpStatus.OK.value());
							loansResponse.setData(
									loanDisbursementService.saveLoanDisbursementDetail(loanDisbursementRequest));
							logger.info("Exit saveLoanDisbursementDetail() ---------------->  msg ==>"
									+ "Information Successfully Stored ");
							return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
						} else {
							logger.info(
									"Failure msg while saveLoanDisbursementDetail in saveLoanDisbursementDetail() ----------------> msg "
											+ reason);
							loansResponse = new LoansResponse(loanDisbursementRequest.getReason().split("[\\{}]")[0],
									HttpStatus.BAD_REQUEST.value());
							loansResponse.setData(false);
							logger.info("Exit saveLoanDisbursementDetail() ----------------> msg ==>" + reason);
							return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
						}
					} else {
						reason = "Invalid Credentials";
						logger.info("Invalid Credentials while saveLoanDisbursementDetail() ----------------> orgId "
								+ orgId + " reason  " + reason);
						loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
						loansResponse.setData(false);
						logger.info("================== Exit saveLoanDisbursementDetail() =================");
						return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
					}
				} else {
					logger.info(
							"Null in LoanDisbursementRequest while saveLoanDisbursementDetail() ----------------> LoanDisbursementRequest"
									+ loanDisbursementRequest);
					loansResponse = new LoansResponse("Mandatory Fields Must Not be Null",
							HttpStatus.BAD_REQUEST.value(), HttpStatus.OK);
					loansResponse.setData(false);
					reason = "Mandatory Fields Must Not be Null while saveLoanDisbursementDetail() ===> LoanDisbursementRequest ====> "
							+ loanDisbursementRequest;
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}
			} else {
				logger.info(
						"Null in encryptedString while saveLoanDisbursementDetail() ----------------> encryptedString "
								+ encryptedString);
				loansResponse = new LoansResponse("Mandatory Fields Must Not be Null", HttpStatus.BAD_REQUEST.value(),
						HttpStatus.OK);
				loansResponse.setData(false);
				reason = "Null in encryptedString while saveLoanDisbursementDetail() encryptedString ====>"
						+ encryptedString;
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("Error while saveLoanDisbursementDetail()----------------------> ", e);
			e.printStackTrace();
			loansResponse = new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
					HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.OK);
			loansResponse.setData(false);
			reason = "Error while save DisbursementDetail in  DisbursementDetail() ===> Msg " + e.getMessage();
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} finally {
			logger.info("Saving Request to DB ===> ");
			generateTokenRequest = new GenerateTokenRequest();
			generateTokenRequest.setToken(tokenString);
			tokenService.setTokenAsExpired(generateTokenRequest);
			auditComponentBankToCW.saveBankToCWReqRes(decrypt != null ? decrypt : encryptedString,
					loanDisbursementRequest != null ? loanDisbursementRequest.getApplicationId() : null,
					CommonUtility.ApiType.DISBURSEMENT, loansResponse, reason, orgId , null);
		}
	}

	@RequestMapping(value = "/update_skip_payment_status/{appId}/{orgId}/{fpProductId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateSkipPaymentStatus(@PathVariable("appId") Long appId,
			@PathVariable("orgId") Long orgId, @PathVariable("fpProductId") Long fprProductId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			logger.info("start updateSkipPaymentStatus()");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			LoansResponse response = new LoansResponse("Successfully updated", HttpStatus.OK.value());
			loanApplicationService.updateSkipPayment(userId, appId, orgId, fprProductId);
			logger.info("end updateSkipPaymentStatus()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updating Payment Status==>{}", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/update_skip_payment_whiteLabel/{appId}/{businessTypeId}/{orgId}/{fpProductId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> updateSkipPayment(@PathVariable("appId") Long appId, @PathVariable("businessTypeId") Integer businessTypeId,
			@PathVariable("orgId") Long orgId, @PathVariable("fpProductId") Long fprProductId,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		try {
			logger.info("start updateSkipPaymentForWhiteLabel()");
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			LoansResponse response = new LoansResponse("Successfully updated", HttpStatus.OK.value());
			loanApplicationService.updateSkipPaymentWhiteLabel(userId, appId, businessTypeId, orgId, fprProductId);
			logger.info("end updateSkipPaymentStatus()");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updating Payment Status for WhiteLabel==>{}", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/getProposalDataFromAppId/{appId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getProposalDataFromAppId(@PathVariable("appId") Long appId) {
		try {
			logger.info("start getProposalDataFromAppId()");
			LoanApplicationRequest loanResponse = loanApplicationService.getProposalDataFromApplicationId(appId);
			if (!CommonUtils.isObjectNullOrEmpty(loanResponse)) {
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Successfully get result", HttpStatus.OK.value(), loanResponse),
						HttpStatus.OK);
			}
			return new ResponseEntity<LoansResponse>(new LoansResponse("Data not found !!", HttpStatus.OK.value()),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getProposalDataFromAppId ==>{}", e);
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/saveDetailedInfo", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<LoansResponse> saveDetailedInfo(@RequestBody String encryptedString,
			HttpServletRequest httpServletRequest) {
		GenerateTokenRequest generateTokenRequest = null;
		String decrypt = null;
		LoansResponse loansResponse = null;
		String reason = null;
		Long orgId = null;
		Boolean isSuccess = false;
		ProfileReqRes profileReqRes = null;
		String tokenString = null;
		try {

			logger.info(
					"=============================checking authorized token in  saveDetailedInfo(){} ============================= ");
			tokenString = httpServletRequest.getHeader("token");
			if (CommonUtils.isObjectNullOrEmpty(tokenString)) {
				reason = "Token is null";
				loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.UNAUTHORIZED);
			} else {
				if (CommonUtils.isObjectNullOrEmpty((tokenString = tokenService.checkTokenExpiration(tokenString)))) {
					reason = "Token is Expired ";
					loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.UNAUTHORIZED);
				}
			}
			logger.info("------------------------------ Enter in  saveDetailedInfo(){}------------------------------");
			if (encryptedString != null) {
				try {
					decrypt = AESEncryptionUtility.decrypt(encryptedString);
					profileReqRes = MultipleJSONObjectHelper.getObjectFromString(decrypt, ProfileReqRes.class);
				} catch (Exception e) {
					e.printStackTrace();
					logger.info(
							"Error while Converting Encrypted Object to ProfileReqRes  saveDetailedInfo(){} -------------------------> ",
							e.getMessage());
					loansResponse = new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(),
							HttpStatus.OK);
					loansResponse.setData(isSuccess);
					logger.info("Saving Request to DB ===> ");
					if (CommonUtils.isObjectNullOrEmpty(decrypt)) {
						reason = "ERROR WHILE DECRYPT ENCRYPTED OBJECT   ====> Msg ===> ";
					} else {
						reason = "error while converting decrypt string to profileReqRes ====> Msg ===> ";
					}
					reason += e.getMessage();
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}
				if (!CommonUtils.isObjectListNull(profileReqRes, profileReqRes.getUserName(),
						profileReqRes.getPassword())) {
					orgId = auditComponentBankToCW.getOrgIdByCredential(profileReqRes.getUserName(),
							profileReqRes.getPassword());

					if (!CommonUtils.isObjectNullOrEmpty(orgId)) {

						loanApplicationService.saveDetailedInfo(profileReqRes);
						/*
						 * if("Sucess".equals(loanApplicationService.saveDetailedInfo(profileReqRes))) {
						 */
						logger.info("Success msg while saveDetailedInfo() ----------------> msg " + reason);
						isSuccess = true;
						reason = null;
						loansResponse = new LoansResponse("Sucess", HttpStatus.OK.value());
						loansResponse.setData(isSuccess);
						logger.info("================== Exit saveDetailedInfo() =================");
						return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
						/*
						 * }else { logger.
						 * info("Failed while save profileReqRes in saveDetailedInfo() ----------------> reason  "
						 * + reason) ; loansResponse = new LoansResponse(reason,
						 * HttpStatus.BAD_REQUEST.value()); loansResponse.setData(isSuccess);
						 * logger.info("================== Exit saveDetailedInfo() =================" );
						 * return new ResponseEntity<LoansResponse>(loansResponse ,HttpStatus.OK ); }
						 */
					} else {
						reason = "Invalid Credentials";
						logger.info("Invalid Credentials while saveDetailedInfo() ----------------> orgId " + orgId
								+ " reason  " + reason);
						loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
						loansResponse.setData(isSuccess);
						logger.info("================== Exit saveDetailedInfo() =================");
						return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
					}

				} else {
					logger.info("Null in ProfileReqRes while saveDetailedInfo() ----------------> ProfileReqRes  "
							+ profileReqRes);
					loansResponse = new LoansResponse(
							"mandatory field must not be null (** requestObject and credentials ** ) ",
							HttpStatus.BAD_REQUEST.value());
					loansResponse.setData(isSuccess);
					logger.info("Saving Request to DB ===> ");
					reason = "mandatory filed must not be null (** requestObject and credentials ** ) ===> ProfileReqRes ====> "
							+ profileReqRes;
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}
			} else {
				logger.info("Null in encryptedString while saveDetailedInfo() ----------------> encryptedString "
						+ encryptedString);
				loansResponse = new LoansResponse("request object must not be null", HttpStatus.BAD_REQUEST.value());
				loansResponse.setData(isSuccess);
				logger.info("Saving Request to DB ===> ");
				reason = "request object must not be null , Null in encryptedString  ====> " + encryptedString;
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("Error while saveDetailedInfo()----------------------> ", e);
			e.printStackTrace();
			if (e instanceof LoansException)
				loansResponse = new LoansResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), HttpStatus.OK);
			else
				loansResponse = new LoansResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
						HttpStatus.OK);
			loansResponse.setData(isSuccess);
			reason = "Error while save profileReqRes ===> Msg " + e.getMessage();
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} finally {
			logger.info("Reason ---------------------> ", reason);
			reason += " \n while saveDetailedInfo()";
			generateTokenRequest = new GenerateTokenRequest();
			generateTokenRequest.setToken(tokenString);
			tokenService.setTokenAsExpired(generateTokenRequest);
			auditComponentBankToCW.saveBankToCWReqRes(decrypt != null ? decrypt : encryptedString, null,
					CommonUtility.ApiType.DETAILED_API, loansResponse, reason, orgId , null);
		}
	}

	// For Payment Gateway response through Mobile API
	// ===========================================================================================================================

	@RequestMapping(value = "mobile/successUrl", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public void payUMoneyResponse(@RequestBody String response) {

		try {
			logger.info("Payment Gateway redirect response for Mobile" + response);
			Map<String, Object> map = new HashMap<String, Object>();
			String responseMessageSequence = "MerchantID|txnid|Filler1|Filler2|TxnAmount|BankID|Filler3|Filler4|CurrencyType|ItemCode|Filler5|Filler6|Filler7|txnDate|statusCode|Filler7|Phone|EmailId|firstname|applicationId|productinfo|AdditionalInfo2|AdditionalInfo3|AdditionalInfo4|status|checkSum";
			String responseType = null;
			PaymentRequest paymentRequest = null;
			int flag = 0;
			String decodedresponse = null;
			String responseParams = null;
			try {
				decodedresponse = URLDecoder.decode(response, StandardCharsets.UTF_8.toString());
				responseParams = decodedresponse;
				logger.info("Decoded value=======++++++>>>>" + decodedresponse);
			} catch (UnsupportedEncodingException e) {
				logger.info("Something went wrong while decoding the response!!!");
				e.printStackTrace();
			}

			StringTokenizer a = new StringTokenizer(decodedresponse, "&");

			while (a.hasMoreElements()) {
				String z = a.nextToken();
				StringTokenizer m = new StringTokenizer(z, "=");

				logger.info("Checking whether the Response is from BillDesk or PayUMoney");
				String key;
				String value;
				try {
					key = m.nextToken();
					if ("msg".equals(key)) {
						responseType = "BillDesk";
						logger.info("Response is from BillDesk" + key);
						break;
					}
				} catch (Exception e) {
					logger.info("Token null==================>" + e.getMessage());
					// e.printStackTrace();
					key = null;
				}
				try {
					value = m.nextToken();
				} catch (Exception e) {
					logger.info("Token null==================>" + e.getMessage());
					// e.printStackTrace();
					value = null;
				}
				// map.put(key, value);
			}

			if ("BillDesk".equals(responseType)) {

				StringTokenizer x = new StringTokenizer(decodedresponse, "&");

				while (x.hasMoreElements()) {
					String z = x.nextToken();
					StringTokenizer p = new StringTokenizer(z, "=");
					while (p.hasMoreElements()) {
						String msg = p.nextToken();

						if (!"msg".equals(msg)) {
							// String pipedMessage = msg.replaceAll("%7C", "|");
							logger.info("Piped Message================>" + msg);
							StringTokenizer b = new StringTokenizer(msg, "|");
							StringTokenizer c = new StringTokenizer(responseMessageSequence, "|");
							while (b.hasMoreElements()) {
								while (c.hasMoreElements()) {
									String key = c.nextToken();
									String value = b.nextToken();
									System.out.println(key + "================>" + value);

									map.put(key, value);

								}
							}
							flag = 1;
						}
						if (flag == 1)
							break;
					}
					if (flag == 1)
						break;
				}
				responseParams = responseParams.replaceAll("&", " ");
				responseParams = responseParams.replace("|", " ");
				logger.info("Response Params================>" + responseParams);
				paymentRequest = new PaymentRequest();
				paymentRequest.setApplicationId(Long.valueOf(String.valueOf(map.get("applicationId"))));
				paymentRequest.setUserId(Long.valueOf(String.valueOf(map.get("AdditionalInfo2"))));
				paymentRequest.setPurposeCode(map.get("productinfo").toString());
				paymentRequest.setResponseParams(responseParams);
				paymentRequest.setNameOfEntity(map.get("firstname").toString());
				
				if ("0300".toString().equals(map.get("statusCode"))) {
					paymentRequest.setStatus("Success");
				}
				else {
					paymentRequest.setStatus("Failed");
				}
			}

			else {

				StringTokenizer x = new StringTokenizer(response, "&");
				while (x.hasMoreElements()) {
					String z = x.nextToken();
					StringTokenizer p = new StringTokenizer(z, "=");
					logger.info("Success URL ----------------------------> " + p);
					String key;
					String value;
					try {
						key = p.nextToken();
					} catch (Exception e) {
						logger.info("Token null==================>" + e.getMessage());
						// e.printStackTrace();
						key = null;
					}
					try {
						value = p.nextToken();
					} catch (Exception e) {
						logger.info("Token null==================>" + e.getMessage());
						// e.printStackTrace();
						value = null;
					}
					map.put(key, value);

				}
				responseParams = responseParams.replaceAll("&", " ");
				logger.info("Response Params================>" + responseParams);
				paymentRequest = new PaymentRequest();
				paymentRequest.setApplicationId(Long.valueOf(String.valueOf(map.get("udf1"))));
				paymentRequest.setUserId(Long.valueOf(String.valueOf(map.get("udf2"))));
				paymentRequest.setPurposeCode(map.get("productinfo").toString());
				paymentRequest.setResponseParams(responseParams);
				paymentRequest.setNameOfEntity(map.get("firstname").toString());
				
				if ("success".equals(map.get("status").toString())) {
					paymentRequest.setStatus("Success");
				} else {
					paymentRequest.setStatus("Failed");
				}

			}

			logger.info("AppId==>" + paymentRequest.getApplicationId() + " UserId==>" + paymentRequest.getUserId()
					+ " PuposeCode==>" + paymentRequest.getPurposeCode());

			
			paymentRequest.setTrxnId(map.get("txnid").toString());

			LoansResponse loansResponse = new LoansResponse("Success", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.updateLoanApplicationMasterPaymentStatus(paymentRequest,
					paymentRequest.getUserId()));
			logger.info("end updatePaymentForMobileStatus()");
			// return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updating Payment Status for mobile==>{}", e);
			e.printStackTrace();
			/*
			 * return new ResponseEntity<LoansResponse>( new
			 * LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
			 * HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
			 */ }

	}

	@RequestMapping(value = "mobile/billDesk", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public void billDeskResponse(@RequestBody String response) {

		try {
			logger.info("BillDesk redirect response for Mobile" + response);

			Map<String, Object> map = new HashMap<String, Object>();
			String responseMessageSequence = "MerchantID|txnid|Filler1|Filler2|TxnAmount|BankID|Filler3|Filler4|CurrencyType|ItemCode|Filler5|Filler6|Filler7|txnDate|statusCode|Filler7|Phone|EmailId|firstname|applicationId|productinfo|AdditionalInfo2|AdditionalInfo3|AdditionalInfo4|status|checkSum";
			int flag = 0;

			StringTokenizer x = new StringTokenizer(response, "&");

			while (x.hasMoreElements()) {
				String z = x.nextToken();
				StringTokenizer p = new StringTokenizer(z, "=");
				while (p.hasMoreElements()) {
					String msg = p.nextToken();

					if (!"msg".equals(msg)) {
						String pipedMessage = msg.replaceAll("%7C", "|");
						logger.info("Piped Message================>" + pipedMessage);
						StringTokenizer b = new StringTokenizer(pipedMessage, "|");
						StringTokenizer c = new StringTokenizer(responseMessageSequence, "|");
						while (b.hasMoreElements()) {
							while (c.hasMoreElements()) {
								String key = c.nextToken();
								String value = b.nextToken();
								System.out.println(key + "================>" + value);

								map.put(key, value);

							}
						}
						flag = 1;
					}
					if (flag == 1)
						break;
				}
				if (flag == 1)
					break;
			}

			PaymentRequest paymentRequest = new PaymentRequest();

			paymentRequest.setApplicationId(Long.valueOf(String.valueOf(map.get("udf1"))));
			paymentRequest.setUserId(Long.valueOf(String.valueOf(map.get("udf2"))));
			paymentRequest.setPurposeCode(map.get("productinfo").toString());
			if ("success".equals(map.get("status").toString())) {
				paymentRequest.setStatus("Success");
			} else {
				paymentRequest.setStatus("Failed");
			}
			paymentRequest.setTrxnId(map.get("txnid").toString());

			LoansResponse loansResponse = new LoansResponse("Success", HttpStatus.OK.value());
			loansResponse.setData(loanApplicationService.updateLoanApplicationMasterPaymentStatus(paymentRequest,
					paymentRequest.getUserId()));
			logger.info("end updatePaymentForMobileStatus()");
			// return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while updating Payment Status for mobile==>{}", e);
			e.printStackTrace();
			/*
			 * return new ResponseEntity<LoansResponse>( new
			 * LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
			 * HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
			 */ }

	}

	@RequestMapping(value = "/getToken", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getToken(@RequestBody String encryptedString) {
		GenerateTokenRequest generateTokenRequest = null;
		String reason = null;
		Boolean isSuccess = false;
		Long applicationId = null;
		String token = null;
		String decrypt = null;
		Long orgId = null;
		LoansResponse loansResponse = null;
		try {

			try {
				decrypt = AESEncryptionUtility.decrypt(encryptedString);
				generateTokenRequest = MultipleJSONObjectHelper.getObjectFromString(decrypt,
						GenerateTokenRequest.class);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error while Converting Encrypted Object to GenerateTokenRequest in getToken(){} =====>"
						+ e.getMessage());
				loansResponse = new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(),
						HttpStatus.OK);
				loansResponse.setData(isSuccess);
				logger.info("Saving Request to DB ===> ");
				if (CommonUtils.isObjectNullOrEmpty(decrypt)) {
					reason = "ERROR WHILE DECRYPT ENCRYPTED OBJECT   ====> Msg ===> ";
				} else {
					reason = "error while converting decrypt string to GenerateTokenRequest  ====> Msg ===> ";
				}
				reason += e.getMessage();
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
			if (!CommonUtils.isObjectListNull(generateTokenRequest, generateTokenRequest.getApplicationId(),
					generateTokenRequest.getUserName(), generateTokenRequest.getPassword())) {
				orgId = auditComponentBankToCW.getOrgIdByCredential(generateTokenRequest.getUserName(),
						generateTokenRequest.getPassword());
				if (!CommonUtils.isObjectNullOrEmpty(orgId)) {
					applicationId = generateTokenRequest.getApplicationId();
					token = tokenService.getToken(generateTokenRequest);
					isSuccess = true;
					logger.info("Success msg while getToken() ----------------> msg " + reason);
					loansResponse = new LoansResponse("Information Successfully Stored ", HttpStatus.OK.value());
					loansResponse.setData(token);
					logger.info("Exit getToken() ---------------->  msg ==>" + "Information Successfully Stored ");
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				} else {
					reason = "Invalid Credentials";
					logger.info("Invalid Credentials while getToken() ----------------> orgId " + orgId + " reason  "
							+ reason);
					loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
					loansResponse.setData(isSuccess);
					logger.info("================== Exit getToken() () =================");
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}
			} else {
				reason = "Mandatory fields must not be null in getToken(){}  (** applicationId and credentials ** ) ";
				logger.info("Failure msg while get Token in getToken() ----------------> msg " + reason);
				loansResponse = new LoansResponse(reason.split("[\\{}]")[0], HttpStatus.BAD_REQUEST.value());
				loansResponse.setData(false);
				logger.info("Exit getToken() ----------------> msg ==>" + reason);
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while token Generation  in getToken()----------------------> ", e);
			reason = "Exception while token Generation  in getToken() {} ====> MSg  " + e.getMessage();
			e.printStackTrace();
			loansResponse = new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
					HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.OK);
			loansResponse.setData(false);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} finally {
			auditComponentBankToCW.saveBankToCWReqRes(decrypt != null ? decrypt : encryptedString, applicationId,
					CommonUtility.ApiType.GENERATING_TOKEN, loansResponse, reason, orgId , null);
		}
	}

	@RequestMapping(value = "/setTokenAsExpired", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> setTokenAsExpired(@RequestBody String encryptedString) {
		GenerateTokenRequest generateTokenRequest = null;
		String reason = null;
		Boolean isSuccess = false;
		Long applicationId = null;
		String decrypt = null;
		LoansResponse loansResponse = null;
		Long orgId = null;

		try {
			try {
				decrypt = AESEncryptionUtility.decrypt(encryptedString);
				generateTokenRequest = MultipleJSONObjectHelper.getObjectFromString(decrypt,
						GenerateTokenRequest.class);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(
						"Error while Converting Encrypted Object to GenerateTokenRequest in setTokenAsExpired(){} =====>"
								+ e.getMessage());
				loansResponse = new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(),
						HttpStatus.OK);
				loansResponse.setData(isSuccess);
				logger.info("Saving Request to DB ===> ");
				if (CommonUtils.isObjectNullOrEmpty(decrypt)) {
					reason = "ERROR WHILE DECRYPT ENCRYPTED OBJECT   ====> Msg ===> ";
				} else {
					reason = "error while converting decrypt string to GenerateTokenRequest  ====> Msg ===> ";
				}
				reason += e.getMessage();
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}
			if (!CommonUtils.isObjectListNull(generateTokenRequest, generateTokenRequest.getApplicationId(),
					generateTokenRequest.getUserName(), generateTokenRequest.getPassword())) {
				orgId = auditComponentBankToCW.getOrgIdByCredential(generateTokenRequest.getUserName(),
						generateTokenRequest.getPassword());
				if (!CommonUtils.isObjectNullOrEmpty(orgId)) {
					applicationId = generateTokenRequest.getApplicationId();
					isSuccess = tokenService.setTokenAsExpired(generateTokenRequest);
					logger.info("Success msg while setTokenAsExpired() ----------------> msg " + reason);
					loansResponse = new LoansResponse("Successfully Set Token as Expired  ", HttpStatus.OK.value());
					loansResponse.setData(isSuccess);
					logger.info("Exit setTokenAsExpired() ---------------->  msg ==>"
							+ "Successfully Set Token as Expired  ");
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				} else {
					reason = "Invalid Credentials";
					logger.info("Invalid Credentials while setTokenAsExpired() ----------------> orgId " + orgId
							+ " reason  " + reason);
					loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED.value());
					loansResponse.setData(isSuccess);
					logger.info("================== Exit setTokenAsExpired() () =================");
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}
			} else {
				reason = "Mandatory fields must not be null in setTokenAsExpired(){}  (** applicationId and credentials ** ) ";
				logger.info("Failure msg while expiring token in setTokenAsExpired() ----------------> msg " + reason);
				loansResponse = new LoansResponse(reason.split("[\\{}]")[0], HttpStatus.BAD_REQUEST.value());
				loansResponse.setData(false);
				logger.info("Exit setTokenAsExpired() ----------------> msg ==>" + reason);
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while expiring token  in setTokenAsExpired()----------------------> ", e);
			reason = "Exception while expiring token  in setTokenAsExpired() {} ====> MSg  " + e.getMessage();
			e.printStackTrace();
			loansResponse = new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
					HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.OK);
			loansResponse.setData(false);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} finally {
			auditComponentBankToCW.saveBankToCWReqRes(decrypt != null ? decrypt : encryptedString, applicationId,
					CommonUtility.ApiType.GENERATING_TOKEN, loansResponse, reason, orgId , null);
		}
	}
	
//	loanSanctionService.saveSanctionAndDisbursementDetailsFromBank();

	/*@RequestMapping(value = "/saveLoanSanctionDisbursementDetailFromBank", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<LoansResponse> saveLoanSanctionDisbursementDetailFromBank(@RequestBody String encryptedString) {
		LoansResponse loansResponse = null;
		String sanctionReason = null;
		String disbursementReason = null;
		Long orgId = null;
		String decrypt = null;
		LoanSanctionAndDisbursedRequest loanSanctionAndDisbursedRequest = null;
		GenerateTokenRequest generateTokenRequest = null;
		String tokenString = null;
		try {
			logger.info(
					"=============================Entry saveLoanSanctionDisbursementDetailFromBank(){} ============================= ");
			
			
			 * if(CommonUtils.isObjectNullOrEmpty(tokenString)) { reason = "Token is null";
			 * loansResponse = new LoansResponse(reason, HttpStatus.UNAUTHORIZED .value());
			 * return new ResponseEntity<LoansResponse>(loansResponse,
			 * HttpStatus.UNAUTHORIZED); }else {
			 * if(CommonUtils.isObjectNullOrEmpty((tokenString =
			 * tokenService.checkTokenExpiration(tokenString)))) { reason =
			 * "Token is Expired "; loansResponse = new LoansResponse(reason,
			 * HttpStatus.UNAUTHORIZED .value()); return new
			 * ResponseEntity<LoansResponse>(loansResponse, HttpStatus.UNAUTHORIZED); } }
			 
			// logger.info("-----------------------------Entry
			// saveLoanSanctionDisbursementDetailFromBank(){} --------------------");
			if (encryptedString != null) {
				
				try {
					decrypt = AESEncryptionUtility.decrypt(encryptedString);
					
					loanSanctionAndDisbursedRequest = MultipleJSONObjectHelper.getObjectFromString(decrypt,
							LoanSanctionAndDisbursedRequest.class);
					
				} catch (Exception e) {
					e.printStackTrace();
					logger.info(
							"Error while Converting Encrypted Object to LoanSanctionAndDisbursedRequest  saveLoanSanctionDisbursementDetailFromBank(){} -------------------------> ",
							e.getMessage());
					loansResponse = new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(),
							HttpStatus.OK);
					loansResponse.setData(false);
					if (CommonUtils.isObjectNullOrEmpty(decrypt)) {
						sanctionReason = "ERROR WHILE DECRYPT ENCRYPTED OBJECT   ====> Msg ===> ";
					} else {
						sanctionReason = "error while converting decrypt string to profileReqRes ====> Msg ===> ";
					}
					sanctionReason += e.getMessage();
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}
			
				if (!CommonUtils.isObjectListNull(loanSanctionAndDisbursedRequest,
					loanSanctionAndDisbursedRequest.getApplicationId(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getAccountNo(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getApplicationId(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getBranch(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getRoi(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getSanctionAmount(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getSanctionDate(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getTenure(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getUserName(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getPassword(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getReferenceNo(),
					loanSanctionAndDisbursedRequest.getLoanSanctionRequest().getActionBy())) {
					orgId = auditComponentBankToCW.getOrgIdByCredential(loanSanctionAndDisbursedRequest.getUserName(),
							loanSanctionAndDisbursedRequest.getPassword());
					if (!CommonUtils.isObjectNullOrEmpty(orgId)) {
						sanctionReason = loanSanctionService.requestValidation(loanSanctionAndDisbursedRequest.getApplicationId(), orgId);
						if ("SUCCESS".equalsIgnoreCase(sanctionReason) && loanSanctionService
								.saveLoanSanctionDetail(loanSanctionAndDisbursedRequest.getLoanSanctionRequest())) {
							if(! CommonUtils.isListNullOrEmpty(loanSanctionAndDisbursedRequest.getLoanDisbursementRequestsList())) {
								disbursementReason = loanDisbursementService.bankRequestValidationAndSave(
									loanSanctionAndDisbursedRequest.getLoanDisbursementRequestsList(), orgId , CommonUtility.ApiType.SANCTION_AND_DISBURSEMENT);
							}
						}

						if ("SUCCESS".equalsIgnoreCase(sanctionReason) || "First Disbursement".equalsIgnoreCase(sanctionReason)) {
							logger.info(
									"Success msg while saveLoanSanctionDisbursementDetailFromBank() ----------------> msg "
											+ sanctionReason);
							sanctionReason = null;
							loansResponse = new LoansResponse("Information Successfully Stored ",
									HttpStatus.OK.value());
							if("SUCCESS".equalsIgnoreCase(disbursementReason) || "First Disbursement".equalsIgnoreCase(disbursementReason)){
								loansResponse.setData(CommonUtility.ApiType.SANCTION_AND_DISBURSEMENT);
							}else {
								loansResponse.setData(CommonUtility.ApiType.SANCTION);
							}
							logger.info("Exit saveLoanSanctionDisbursementDetailFromBank() ---------------->  msg ==>"
									+ "Information Successfully Stored ");
							return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
						} else {
							logger.info(
									"Failure msg while save LoanSanctionAndDisbursedRequest in saveLoanSanctionDisbursementDetailFromBank() to CW ----------------> msg "
											+ sanctionReason);
							loansResponse = new LoansResponse(sanctionReason.split("[\\{}]")[0],
									HttpStatus.BAD_REQUEST.value());
							loansResponse.setData(false);
							logger.info("Exit saveLoanDisbursementDetail() ----------------> msg ==>" + sanctionReason);
							return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
						}
					} else {
						sanctionReason = "Invalid Credentials";
						logger.info(
								"Invalid Credentials while saveLoanSanctionDisbursementDetailFromBank() ----------------> orgId "
										+ orgId + " reason  " + sanctionReason);
						loansResponse = new LoansResponse(sanctionReason, HttpStatus.UNAUTHORIZED.value());
						loansResponse.setData(false);
						logger.info("================== Exit saveLoanDisbursementDetail() =================");
						return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
					}
				} else {
					logger.info(
							"Null in LoanSanctionAndDisbursedRequest while saveLoanSanctionDisbursementDetailFromBank() ----------------> LoanDisbursementRequest"
									+ loanSanctionAndDisbursedRequest);
					loansResponse = new LoansResponse("Mandatory Fields Must Not be Null",
							HttpStatus.BAD_REQUEST.value(), HttpStatus.OK);
					loansResponse.setData(false);
					sanctionReason = "Mandatory Fields Must Not be Null while saveLoanSanctionDisbursementDetailFromBank() ===> LoanDisbursementRequest ====> "
							+ loanSanctionAndDisbursedRequest;
					return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
				}
			} else {
				logger.info(
						"Null in encryptedString while saveLoanSanctionDisbursementDetailFromBank() ----------------> encryptedString "
								+ encryptedString);
				loansResponse = new LoansResponse("Mandatory Fields Must Not be Null", HttpStatus.BAD_REQUEST.value(),
						HttpStatus.OK);
				loansResponse.setData(false);
				sanctionReason = "Null in encryptedString while saveLoanSanctionDisbursementDetailFromBank() encryptedString ====>"
						+ encryptedString;
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("Error while saveLoanSanctionDisbursementDetailFromBank()----------------------> ", e);
			e.printStackTrace();
			loansResponse = new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG,
					HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.OK);
			loansResponse.setData(false);
			sanctionReason = "Error while save LoanSanctionAndDisbursedRequest in  saveLoanSanctionDisbursementDetailFromBank() ===> Msg "
					+ e.getMessage();
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
		} finally {
			logger.info("Saving Request to DB ===> ");
			generateTokenRequest = new GenerateTokenRequest();
			generateTokenRequest.setToken(tokenString);
			auditComponentBankToCW.saveBankToCWReqRes(decrypt != null ? decrypt : encryptedString,
					loanSanctionAndDisbursedRequest != null ? loanSanctionAndDisbursedRequest.getApplicationId() : null,
					CommonUtility.ApiType.SANCTION_AND_DISBURSEMENT, loansResponse, sanctionReason, orgId);
		}
	}*/

	
	@RequestMapping(value = "/reverse_api", method = RequestMethod.GET)
	public void reverseAPI() {
		try {
			logger.info("start reverseAPI()");
			loanSanctionService.saveSanctionAndDisbursementDetailsFromBank();
		} catch (Exception e) {
			logger.error("Error while reverseAPI ==>{}", e);
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/ddr_status/{applicationId}", method = RequestMethod.GET)
	public ResponseEntity<LoansResponse> ddrStatus(@PathVariable("applicationId") Long applicationId) {
		try {
			logger.info("ENTER IN GET DDR STATUS ID---------------->" + applicationId);
			return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully get data", HttpStatus.OK.value(), loanApplicationService.getDDRStatusId(applicationId)), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while get ddr status==>");
			e.printStackTrace();
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/getToken/{url}/{langCode}", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getToken(@PathVariable("url") String url , @RequestBody GenerateTokenRequest generateTokenRequest , @PathVariable("langCode") Integer  langCode ) {
		try {
			logger.info("start getToken()");
			String res = loanSanctionService.getToken(url, generateTokenRequest, langCode);
			return res;
		} catch (Exception e) {
			logger.error("Error while reverseAPI ==>{}", e);
			e.printStackTrace();
			return null;
			
		}
	}
	
}
