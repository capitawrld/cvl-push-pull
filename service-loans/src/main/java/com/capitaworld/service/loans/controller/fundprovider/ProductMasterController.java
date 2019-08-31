package com.capitaworld.service.loans.controller.fundprovider;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.model.FpProductDetails;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.ProductDetailsForSp;
import com.capitaworld.service.loans.model.ProductDetailsResponse;
import com.capitaworld.service.loans.model.ProductMasterRequest;
import com.capitaworld.service.loans.model.WorkflowData;
import com.capitaworld.service.loans.model.common.ChatDetails;
import com.capitaworld.service.loans.model.corporate.AddProductRequest;
import com.capitaworld.service.loans.model.corporate.CorporateProduct;
import com.capitaworld.service.loans.model.retail.AgriLoanParameterRequest;
import com.capitaworld.service.loans.model.retail.AutoLoanParameterRequest;
import com.capitaworld.service.loans.model.retail.HomeLoanParameterRequest;
import com.capitaworld.service.loans.model.retail.RetailProduct;
import com.capitaworld.service.loans.service.fundprovider.AgriLoanParameterService;
import com.capitaworld.service.loans.service.fundprovider.ProductMasterService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/product_master")
public class ProductMasterController {

	private static final Logger logger = LoggerFactory.getLogger(ProductMasterController.class);

	private static final String USER_ID_CAN_NOT_BE_EMPTY_MSG = "userId can not be empty ==>{}";
	private static final String CORPORATE_PRODUCT_ID_CAN_NOT_BE_EMPTY_MSG = "corporateProduct id can not be empty ==>{}";
	private static final String USER_ID_REQUIRE_TO_GET_PRODUCT_DETAILS_MSG = "UserId Require to get product Details ==>{}";
	private static final String ERROR_WHILE_GETTING_PRODUCTS_DETAILS_MSG = "Error while getting Products Details ==> {}";
	private static final String ADD_PRODUCT = "addProduct";
	private static final String CLICK_ON_WORK_FLOW_BUTTON = "clickOnWorkFlowButton";
	private static final String GET_ACTIVE_INACTIVE_LIST = "getActiveInActiveList";
	private static final String GET_LIST_BY_USER_TYPE = "getListByUserType";
	private static final String GET_USER_NAME_BY_PRODUCT_ID = "getUserNameByProductId";
	private static final String GET_USER_ID_BY_PRODUCT_ID = "getUserIdByProductId";
	private static final String GET_LIST_BY_USER_ID_LIST = "getListByUserIdList";
	private static final String FP_PRODUCT_DETAILS = "fpProductDetails";
	private static final String GET_FP_DETAILS = "getFpDetails";
	private static final String IS_SELF_VIEW = "isSelfView";
	private static final String CHECK_PARAMETER_IS_FILLED = "checkParameterIsFilled";
	private static final String CHANGE_STATUS = "changeStatus";
	private static final String LAST_ACCESSED_PRODUCT = "lastAccessedProduct";
	private static final String CHANGE_STATUS_WITH_WORKFLOW = "changeStatusWithWorkFlow";

	@Autowired
	private ProductMasterService productMasterService;
	
	@Autowired
	private AgriLoanParameterService agriLoanParameterService;

	@RequestMapping(value = "/addProduct", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> addProduct(@RequestBody AddProductRequest addProductRequest,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		CommonDocumentUtils.startHook(logger, ADD_PRODUCT);
		try {
			// request must not be null

			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);

			if (userId == null) {
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG + userId);
				CommonDocumentUtils.endHook(logger, ADD_PRODUCT);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			if (addProductRequest == null) {
				logger.warn("addProductRequest Object can not be empty ==>" + addProductRequest);
				CommonDocumentUtils.endHook(logger, ADD_PRODUCT);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			addProductRequest.setUserId(userId);
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				addProductRequest.setClientId(clientId);
			}

			Boolean response = productMasterService.saveOrUpdate(addProductRequest,userOrgId);
			if (response) {
				CommonDocumentUtils.endHook(logger, ADD_PRODUCT);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, ADD_PRODUCT);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while saving addProduct Details==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	
	
	@RequestMapping(value = "/clickOnWorkFlowButton", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> clickOnWorkFlowButton(@RequestBody WorkflowData workflowData,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		CommonDocumentUtils.startHook(logger, CLICK_ON_WORK_FLOW_BUTTON);
		try {
			// request must not be null

			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			
			workflowData.setUserId(userId);
			if(CommonUtils.isObjectListNull(workflowData.getActionId(),workflowData.getFpProductId(),workflowData.getJobId(),workflowData.getNextworkflowStep(),workflowData.getWorkflowStep()))
			{
				logger.warn("workflow data can not be null" );
				CommonDocumentUtils.endHook(logger, CLICK_ON_WORK_FLOW_BUTTON);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			if (userId == null) {
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG + userId);
				CommonDocumentUtils.endHook(logger, ADD_PRODUCT);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			

			Boolean response = productMasterService.clickOnWorkFlowButton(workflowData);
			if (response) {
				CommonDocumentUtils.endHook(logger, CLICK_ON_WORK_FLOW_BUTTON);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, CLICK_ON_WORK_FLOW_BUTTON);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while clickOnWorkFlowButton==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/saveCorporate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveCorporate(
			@RequestBody CorporateProduct corporateProduct,HttpServletRequest request) {
		CommonDocumentUtils.startHook(logger, "save");
		try {
			if (corporateProduct == null) {
				logger.warn("corporateProduct Object can not be empty ==>",
						corporateProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			if (corporateProduct.getId() == null) {
				logger.warn(CORPORATE_PRODUCT_ID_CAN_NOT_BE_EMPTY_MSG, corporateProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if(userId==null)
			{
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG, userId);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			corporateProduct.setUserId(userId);
			boolean response = productMasterService.saveCorporate(corporateProduct);
			if (response) {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Error while saving corporateProduct  Parameter==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@RequestMapping(value = "/saveRetail", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveRetail(
			@RequestBody RetailProduct retailProduct,HttpServletRequest request) {
		CommonDocumentUtils.startHook(logger, "save");
		try {
			if (retailProduct == null) {
				logger.warn("retailProduct Object can not be empty ==>",
						retailProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			if (retailProduct.getId() == null) {
				logger.warn("retailProduct id can not be empty ==>", retailProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			if(userId==null)
			{
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG, userId);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			retailProduct.setUserId(userId);
			boolean response = productMasterService.saveRetail(retailProduct);
			if (response) {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Error while saving retailProduct  Parameter==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@RequestMapping(value = "/getList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getList(HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, CommonUtils.GET_LIST);
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId) ) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (userId == null) {
				logger.warn(USER_ID_REQUIRE_TO_GET_PRODUCT_DETAILS_MSG + userId);
				CommonDocumentUtils.endHook(logger, CommonUtils.GET_LIST);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			List<ProductMasterRequest> response = productMasterService.getList(userId,userOrgId);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setListData(response);
			CommonDocumentUtils.endHook(logger, CommonUtils.GET_LIST);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_PRODUCTS_DETAILS_MSG, e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getActiveInActiveList/{businessTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoansResponse> getActiveInActiveList(HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId, @PathVariable(value = "businessTypeId") Long businessTypeId) {
        // request must not be null
		CommonDocumentUtils.startHook(logger, GET_ACTIVE_INACTIVE_LIST);
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId) ) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (userId == null) {
				logger.warn(USER_ID_REQUIRE_TO_GET_PRODUCT_DETAILS_MSG + userId);
				CommonDocumentUtils.endHook(logger, GET_ACTIVE_INACTIVE_LIST);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
            List<ProductMasterRequest> response = productMasterService.getActiveInActiveList(userId, userOrgId, businessTypeId);
            LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setListData(response);
			CommonDocumentUtils.endHook(logger, GET_ACTIVE_INACTIVE_LIST);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Active InActive Products Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/getListByUserType/{userType}/{applicationStage}/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getListByUserType(HttpServletRequest request,
			@PathVariable(value = "userType") String userType,@PathVariable(value = "applicationStage")String applicationStage,@PathVariable(value = "productId")String productId,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, GET_LIST_BY_USER_TYPE);
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}		
			//get org id
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			
			if (userId == null) {
				logger.warn(USER_ID_REQUIRE_TO_GET_PRODUCT_DETAILS_MSG , userId);
				CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_TYPE);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			if (userType == null) {
				logger.warn("userType Require to get product Details ==>{}" , userId);
				CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_TYPE);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			//List<ProductMasterRequest> response = productMasterService.getListByUserType(userId, userType);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setListData(productMasterService.getListByUserType(userId, Integer.parseInt(CommonUtils.decode(userType)),Integer.parseInt(CommonUtils.decode(applicationStage)),userOrgId,Integer.parseInt(CommonUtils.decode(productId))));
			CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_TYPE);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_PRODUCTS_DETAILS_MSG, e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getFPProduct/{id}/{applicationStage}/{role}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFPProduct(HttpServletRequest request,
			@PathVariable(value = "id") Long id,@PathVariable(value = "applicationStage")Integer applicationStage
			,@PathVariable(value = "role")Long role,@RequestParam(value = "clientId", required = false) Long clientId
			) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, "getFPProduct");
		try {
			
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}	
			
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(productMasterService.getProductMasterWithAllData(id,applicationStage,role,userId));
			CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_TYPE);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_PRODUCTS_DETAILS_MSG, e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	
	@RequestMapping(value = "/getUserNameByProductId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUserNameByProductId(@RequestBody Long productId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, GET_USER_NAME_BY_PRODUCT_ID);
		try {

			if (productId == null) {
				logger.warn("productId ID Require to get Details ==>" + productId);
				CommonDocumentUtils.endHook(logger, GET_USER_NAME_BY_PRODUCT_ID);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			Object[] response = productMasterService.getUserDetailsByPrductId(productId);
			LoansResponse loansResponse;
			if (response == null) {
				loansResponse = new LoansResponse(CommonUtils.DATA_NOT_FOUND, HttpStatus.OK.value());
			} else {
				loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
				if (!CommonUtils.isObjectNullOrEmpty(response[1])) {
					loansResponse.setData(response[1]);
				} else {
					loansResponse.setData("NA");
				}
			}
			CommonDocumentUtils.endHook(logger, GET_USER_NAME_BY_PRODUCT_ID);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting user name ==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getUserIdByProductId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUserIdByProductId(@RequestBody Long productId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, GET_USER_ID_BY_PRODUCT_ID);
		try {

			if (productId == null) {
				logger.warn("productId ID Require to get Details ==>" + productId);
				CommonDocumentUtils.endHook(logger, GET_USER_ID_BY_PRODUCT_ID);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			Object[] response = productMasterService.getUserDetailsByPrductId(productId);
			LoansResponse loansResponse;
			if (response == null) {
				loansResponse = new LoansResponse(CommonUtils.DATA_NOT_FOUND, HttpStatus.OK.value());
			} else {
				loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
				if (!CommonUtils.isObjectNullOrEmpty(response[0])) {
					loansResponse.setData(response[0]);
				}

			}
			CommonDocumentUtils.endHook(logger, GET_USER_ID_BY_PRODUCT_ID);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting user name ==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getListByUserIdList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getListByUseIdList(@RequestBody Long userId, HttpServletRequest request) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, GET_LIST_BY_USER_ID_LIST);
		try {

			if (userId == null) {
				logger.warn("UserId Require to get Loan Applications Details ==>" + userId);
				CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_ID_LIST);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			List<ProductDetailsForSp> response = productMasterService.getProductDetailsByUserIdList(userId);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setListData(response);
			CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_ID_LIST);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/productDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProductDetailsResponse> fpProductDetails(
			@RequestParam(value = "clientId", required = false) Long clientId, HttpServletRequest request) {
		CommonDocumentUtils.startHook(logger, FP_PRODUCT_DETAILS);
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}

			if (userId == null) {
				ProductDetailsResponse productDetailsResponse = new ProductDetailsResponse("User id is null or empty",
						HttpStatus.BAD_REQUEST.value());
				logger.error("User id is null or empty");
				CommonDocumentUtils.endHook(logger, FP_PRODUCT_DETAILS);
				return new ResponseEntity<ProductDetailsResponse>(productDetailsResponse, HttpStatus.OK);
			}
			Long userOrgId = null;
			if(!CommonUtils.isObjectNullOrEmpty(request.getAttribute(CommonUtils.USER_ORG_ID))) {
				userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);	
			}

			ProductDetailsResponse productDetailsResponse = productMasterService.getProductDetailsResponse(userId,userOrgId);
			CommonDocumentUtils.endHook(logger, FP_PRODUCT_DETAILS);
			return new ResponseEntity<ProductDetailsResponse>(productDetailsResponse, HttpStatus.OK);

		} catch (Exception e) {
			ProductDetailsResponse productDetailsResponse = new ProductDetailsResponse("Something went wrong",
					HttpStatus.INTERNAL_SERVER_ERROR.value());
			logger.error(CommonUtils.SOMETHING_WENT_WRONG,e);
			return new ResponseEntity<ProductDetailsResponse>(productDetailsResponse, HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/getProductDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFpDetails(@RequestBody Long productMappingId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, GET_FP_DETAILS);
		try {

			if (productMappingId == null) {
				logger.warn("productMappingId  Require to get product Details ==>" + productMappingId);
				CommonDocumentUtils.endHook(logger, GET_FP_DETAILS);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			FpProductDetails response = productMasterService.getProductDetails(productMappingId);
			LoansResponse loansResponse;
			if (response == null) {
				loansResponse = new LoansResponse(CommonUtils.DATA_NOT_FOUND, HttpStatus.OK.value());
			} else {
				loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
				loansResponse.setData(response);
			}
			CommonDocumentUtils.endHook(logger, GET_FP_DETAILS);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting fp  product details ==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/is_self_view/{fpMappingId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> isSelfView(@RequestParam(value = "clientId", required = false) Long clientId,
			@PathVariable("fpMappingId") Long fpMappingId, HttpServletRequest request) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, IS_SELF_VIEW);
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (fpMappingId == null) {
				CommonDocumentUtils.endHook(logger, IS_SELF_VIEW);
				logger.warn("fpMappingId  Require to get product Details ==>" + fpMappingId);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(productMasterService.isSelfView(fpMappingId, userId));
			CommonDocumentUtils.endHook(logger, IS_SELF_VIEW);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while checking self view ==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/checkParameterIsFilled/{fpMappingId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> checkParameterIsFilled(@PathVariable("fpMappingId") Long fpMappingId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, CHECK_PARAMETER_IS_FILLED);
		try {

			if (fpMappingId == null) {
				CommonDocumentUtils.endHook(logger, CHECK_PARAMETER_IS_FILLED);
				logger.warn("fpMappingId  Require to check parameter filled or not==>" + fpMappingId);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(productMasterService.checkParameterIsFilled(fpMappingId));
			CommonDocumentUtils.endHook(logger, CHECK_PARAMETER_IS_FILLED);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while checking fp parameter filled or not ==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/changeStatus/{productMappingId}/{status}/{stage}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> changeStatus(@PathVariable("status") Boolean status,
			@PathVariable("productMappingId") Long productMappingId,
			@PathVariable("stage") Integer stage, HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, CHANGE_STATUS);
		try {

			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (userId == null) {
				logger.warn(USER_ID_REQUIRE_TO_GET_PRODUCT_DETAILS_MSG + userId);
				CommonDocumentUtils.endHook(logger, CommonUtils.GET_LIST);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			if (status == null || productMappingId==null) {
				CommonDocumentUtils.endHook(logger, CHANGE_STATUS);
				logger.warn("productMappingId  and status Require to changeStatus==>" + status);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			LoansResponse loansResponse = new LoansResponse("changeStatus successfully.", HttpStatus.OK.value());
			loansResponse.setMessage(status?"activated":"inactivated");
			loansResponse.setData(productMasterService.changeStatus(productMappingId,status, userId,stage));
			CommonDocumentUtils.endHook(logger, CHANGE_STATUS);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while changeStatus ==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/lastAccessedProduct", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> lastAccessedProduct( HttpServletRequest request,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, LAST_ACCESSED_PRODUCT);
		try {

			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (userId == null) {
				logger.warn("UserId Require to get lastAccessedProduct ==>" + userId);
				CommonDocumentUtils.endHook(logger, LAST_ACCESSED_PRODUCT);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			
			LoansResponse loansResponse = new LoansResponse("last access product detail", HttpStatus.OK.value());
			
			loansResponse.setData(productMasterService.lastAccessedProduct(userId));
			CommonDocumentUtils.endHook(logger, LAST_ACCESSED_PRODUCT);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while lastAccessedProduct ==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getChatListByApplicationId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getChatListByApplicationId(HttpServletRequest request,
			@RequestBody Long mappingId) {
		// request must not be null
		try {
			CommonDocumentUtils.startHook(logger, "getChatListByApplicationId");
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			List<ChatDetails> chatDetailsList = productMasterService.getChatListByFpMappingId(mappingId);
			if (chatDetailsList != null && !chatDetailsList.isEmpty()) {
				loansResponse.setListData(chatDetailsList);
			}
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getChatListByApplicationId==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/get_product/{orgId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getProductsByOrg(@PathVariable("orgId") Long orgId) {
		try {
			logger.info("start getProductsByOrg()");
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setData(productMasterService.getProductByOrgId(orgId));
			logger.info("End getProductsByOrg()");
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while getProductsByOrg==>{}", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/saveMasterFromTemp ", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveMasterFromTemp(HttpServletRequest request,
			@RequestBody Long mappingId) {
		// request must not be null
		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
		
		if (userId == null) {
			logger.warn("UserId Require to saveMasterFromTemp ==>" + userId);
			CommonDocumentUtils.endHook(logger, "saveMasterFromTemp");
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			CommonDocumentUtils.startHook(logger, "saveMasterFromTemp");
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			productMasterService.saveCorporateMasterFromTemp(mappingId);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while saveMasterFromTemp==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/saveCorporateInTemp", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveCorporateInTemp(
			@RequestBody CorporateProduct corporateProduct,HttpServletRequest request) {
		CommonDocumentUtils.startHook(logger, "save");
		try {
			if (corporateProduct == null) {
				logger.warn("corporateProduct Object can not be empty ==>",
						corporateProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			if (corporateProduct.getId() == null) {
				logger.warn(CORPORATE_PRODUCT_ID_CAN_NOT_BE_EMPTY_MSG, corporateProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			if(userId==null)
			{
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG, userId);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			corporateProduct.setUserId(userId);
			corporateProduct.setUserOrgId(userOrgId);
			boolean response = productMasterService.saveCorporateInTemp(corporateProduct);
			if (response) {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Error while saving corporateProduct  Parameter==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@RequestMapping(value = "/saveRetailInTemp", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveRetailInTemp(
			@RequestBody RetailProduct retailProduct,HttpServletRequest request) {
		CommonDocumentUtils.startHook(logger, "save");
		try {
			if (retailProduct == null) {
				logger.warn("retailProduct Object can not be empty ==>",
						retailProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			if (retailProduct.getId() == null) {
				logger.warn(CORPORATE_PRODUCT_ID_CAN_NOT_BE_EMPTY_MSG, retailProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			if(userId==null)
			{
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG, userId);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			retailProduct.setUserId(userId);
			retailProduct.setUserOrgId(userOrgId);
			boolean response = productMasterService.saveRetailInTemp(retailProduct);
			if (response) {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Error while saving saveRetailInTemp  Parameter==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@PostMapping(value = "/saveRetailHomeLoan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveRetailHomeLoan(
			@RequestBody HomeLoanParameterRequest retailProduct,HttpServletRequest request) {
		CommonDocumentUtils.startHook(logger, "save");
		try {
			if (retailProduct.getId() == null) {
				logger.warn(CORPORATE_PRODUCT_ID_CAN_NOT_BE_EMPTY_MSG, retailProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			if(userId == null){
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG, userId);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			retailProduct.setUserId(userId);
			retailProduct.setUserOrgId(userOrgId);
			boolean response = productMasterService.saveRetailInTemp(retailProduct);
			if (response) {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Error while saving saveRetailInTemp  Parameter==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@PostMapping(value = "/saveRetailAutoLoan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveRetailAutoLoan(
			@RequestBody AutoLoanParameterRequest retailProduct,HttpServletRequest request) {
		CommonDocumentUtils.startHook(logger, "save");
		try {
			if (retailProduct.getId() == null) {
				logger.warn(CORPORATE_PRODUCT_ID_CAN_NOT_BE_EMPTY_MSG, retailProduct);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			if(userId == null){
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG, userId);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			retailProduct.setUserId(userId);
			retailProduct.setUserOrgId(userOrgId);
			boolean response = productMasterService.saveRetailInTemp(retailProduct);
			if (response) {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Error while saving saveRetailInTemp  Parameter==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@PostMapping(value = "/saveAgriLoan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> saveAgriLoan(
			@RequestBody AgriLoanParameterRequest agriLoanParameterRequest,HttpServletRequest request) {
		CommonDocumentUtils.startHook(logger, "save");
		try {
			if (agriLoanParameterRequest.getId() == null) {
				logger.warn("Id Must Not be Null", agriLoanParameterRequest);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			if(userId == null){
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG, userId);
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.REQUESTED_DATA_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			agriLoanParameterRequest.setUserId(userId);
			agriLoanParameterRequest.setUserOrgId(userOrgId);
			boolean response = agriLoanParameterService.saveOrUpdateTemp(agriLoanParameterRequest);
			if (response) {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, "save");
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Error while saving Agri Loan Parameter==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@RequestMapping(value = "/getApprovedProductByProductType/{productId}/{businessId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getApprovedProductByProductType(HttpServletRequest request,
			@PathVariable(value = "productId") String productId,@PathVariable(value = "businessId") String businessId,
			@RequestParam(value = "clientId", required = false) Long clientId) {
		// request must not be null
		CommonDocumentUtils.startHook(logger, GET_LIST_BY_USER_TYPE);
		try {
			Long userId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId)) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}		
			//get org id
			Long userOrgId = (Long) request.getAttribute(CommonUtils.USER_ORG_ID);
			
			if (userId == null) {
				logger.warn(USER_ID_REQUIRE_TO_GET_PRODUCT_DETAILS_MSG , userId);
				CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_TYPE);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			if (productId == null) {
				logger.warn("productType Require to get product Details ==>{}" , userId);
				CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_TYPE);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			//List<ProductMasterRequest> response = productMasterService.getListByUserType(userId, userType);
			LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
			loansResponse.setListData(productMasterService.getApprovedListByProductType(userId, Integer.parseInt(CommonUtils.decode(productId)), CommonUtils.isObjectNullOrEmpty(CommonUtils.decode(businessId))?null:Integer.parseInt(CommonUtils.decode(businessId)),userOrgId));
			CommonDocumentUtils.endHook(logger, GET_LIST_BY_USER_TYPE);
			return new ResponseEntity<>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_PRODUCTS_DETAILS_MSG, e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/changeStatusWithWorkFlow", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> changeStatusWithWorkFlow(@RequestBody WorkflowData workflowData,
			HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
		CommonDocumentUtils.startHook(logger, CHANGE_STATUS_WITH_WORKFLOW);
		try {

			Long userId = null;
			Long jobId = null;
			if (CommonDocumentUtils.isThisClientApplication(request) && !CommonUtils.isObjectNullOrEmpty(clientId) ) {
				userId = clientId;
			} else {
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			
			if (userId == null) {
				logger.warn(USER_ID_CAN_NOT_BE_EMPTY_MSG , userId);
				CommonDocumentUtils.endHook(logger, ADD_PRODUCT);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			
			jobId = productMasterService.createJobId(userId);
			workflowData.setUserId(userId);
			workflowData.setJobId(jobId);
			
			if(CommonUtils.isObjectListNull(workflowData.getActionId(), workflowData.getFpProductId(), jobId, workflowData.getStage()))
			{
				logger.warn("workflow data can not be null" );
				CommonDocumentUtils.endHook(logger, CHANGE_STATUS_WITH_WORKFLOW);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			Boolean response = productMasterService.changeStatusWithWorkFlow(workflowData);
			if (response) {
				CommonDocumentUtils.endHook(logger, CHANGE_STATUS_WITH_WORKFLOW);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SUCCESSFULLY_SAVED, HttpStatus.OK.value()), HttpStatus.OK);
			} else {
				CommonDocumentUtils.endHook(logger, CHANGE_STATUS_WITH_WORKFLOW);
				return new ResponseEntity<>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while changeStatusWithWorkFlow==>", e);
			return new ResponseEntity<>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
}
