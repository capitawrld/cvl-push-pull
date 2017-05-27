package com.capitaworld.service.loans.controller.fundprovider;

import java.util.List;

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

import com.capitaworld.service.loans.controller.fundseeker.LoanApplicationController;
import com.capitaworld.service.loans.domain.fundprovider.ProductMaster;
import com.capitaworld.service.loans.model.CommonResponse;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.MultipleFpPruductRequest;
import com.capitaworld.service.loans.model.ProductDetailsForSp;
import com.capitaworld.service.loans.model.ProductDetailsResponse;
import com.capitaworld.service.loans.model.ProductMasterRequest;
import com.capitaworld.service.loans.service.fundprovider.ProductMasterService;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/product_master")
public class ProductMasterController {
	private static final Logger logger = LoggerFactory.getLogger(LoanApplicationController.class);
	
	@Autowired
	private ProductMasterService productMasterService;
	
	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public String getPing() {
		logger.info("Ping success");
		return "Ping Succeed";
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> save(@RequestBody MultipleFpPruductRequest multipleFpPruductRequest, HttpServletRequest request,@RequestParam(value = "clientId",required = false) Long clientId) {
		try {
			// request must not be null
			
			Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			multipleFpPruductRequest.setUserId(userId);
			if(request.getAttribute(CommonUtils.USER_TYPE).equals(String.valueOf(CommonUtils.USER_TYPE_SERVICEPROVIDER))){
				multipleFpPruductRequest.setClientId(clientId);
			}
			
			if (userId == null) {
				logger.warn("userId  can not be empty ==>" + userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			if (multipleFpPruductRequest == null) {
				logger.warn("multipleFpPruductRequest Object can not be empty ==>" + multipleFpPruductRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			if(multipleFpPruductRequest.getDataList()==null)
			{
				logger.warn("data list can not be empty ==>" + multipleFpPruductRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Requested data can not be empty.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}
			if(multipleFpPruductRequest.getFpName()==null || multipleFpPruductRequest.getFpName().length()==0)
			{
				logger.warn("fund provider name  can not be empty ==>" + multipleFpPruductRequest);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse("Requested data can not be empty.", HttpStatus.BAD_REQUEST.value()),
						HttpStatus.OK);
			}

			List<CommonResponse> response = productMasterService.saveOrUpdate(multipleFpPruductRequest);
			if (response==null||!response.isEmpty()) {
				LoansResponse loansResponse = new LoansResponse("Data saved.", HttpStatus.OK.value());
				loansResponse.setListData(response);
				return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
			} else {
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while saving multipleFpPruductRequest Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/getList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getList(HttpServletRequest request,@RequestParam(value = "clientId",required = false) Long clientId) {
		// request must not be null
		try {
			Long userId = null;
			if(request.getAttribute(CommonUtils.USER_TYPE).equals(String.valueOf(CommonUtils.USER_TYPE_SERVICEPROVIDER))){
				userId = clientId;
			}else{
				userId = (Long) request.getAttribute(CommonUtils.USER_ID);
			}
			if (userId == null) {
				logger.warn("UserId Require to get product Details ==>" + userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			List<ProductMasterRequest> response = productMasterService.getList(userId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(response);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getUserNameByProductId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUserNameByProductId(@RequestBody Long productId) {
		// request must not be null
		try {

			if (productId == null) {
				logger.warn("productId ID Require to get Details ==>" + productId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			
			ProductMaster response = productMasterService.getUserDetailsByPrductId(productId);
			LoansResponse loansResponse;
			if(response==null)
			{
				 loansResponse = new LoansResponse("Data Not Found.", HttpStatus.OK.value());
			}
			else
			{
			 loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			 System.out.println(loansResponse);
			 loansResponse.setData(response.getFpName());
			}
			
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting user name ==>", e);	
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getUserIdByProductId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getUserIdByProductId(@RequestBody Long productId) {
		// request must not be null
		try {

			if (productId == null) {
				logger.warn("productId ID Require to get Details ==>" + productId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}
			
			ProductMaster response = productMasterService.getUserDetailsByPrductId(productId);
			LoansResponse loansResponse;
			if(response==null)
			{
				 loansResponse = new LoansResponse("Data Not Found.", HttpStatus.OK.value());
			}
			else
			{
			 loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			 System.out.println(loansResponse);
			 loansResponse.setData(response.getUserId());
			}
			
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting user name ==>", e);	
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getListByUserIdList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getListByUseIdList(@RequestBody Long userId,
			HttpServletRequest request) {
		// request must not be null
		try {

			if (userId == null) {
				logger.warn("UserId Require to get Loan Applications Details ==>" + userId);
				return new ResponseEntity<LoansResponse>(
						new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
			}

			List<ProductDetailsForSp> response = productMasterService.getProductDetailsByUserIdList(userId);
			LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
			loansResponse.setListData(response);
			return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error while getting Loan Application Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	@RequestMapping(value = "/productDetails", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProductDetailsResponse> fpProductDetails(HttpServletRequest request ) {
	
		Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
	
			try {
				
				if (userId == null) {
					ProductDetailsResponse productDetailsResponse=new ProductDetailsResponse("User id is null or empty",HttpStatus.BAD_REQUEST.value());
					logger.error("User id is null or empty");
					return new ResponseEntity<ProductDetailsResponse>(productDetailsResponse,HttpStatus.OK);
				}
				
				ProductDetailsResponse productDetailsResponse=productMasterService.getProductDetailsResponse(userId);
				return new ResponseEntity<ProductDetailsResponse>(productDetailsResponse,HttpStatus.OK);
				
				} catch (Exception e) {
					// TODO: handle exception
					ProductDetailsResponse productDetailsResponse=new ProductDetailsResponse("Something went wrong",HttpStatus.INTERNAL_SERVER_ERROR.value());
					logger.error("Something went wrong");
					e.printStackTrace();
					return new ResponseEntity<ProductDetailsResponse>(productDetailsResponse,HttpStatus.OK);
				}
		}
	
}
