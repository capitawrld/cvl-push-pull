package com.opl.service.loans.controller.fundseeker.corporate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opl.mudra.api.loans.model.ExcelRequest;
import com.opl.mudra.api.loans.model.ExcelResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.service.fundseeker.corporate.AssetsDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.BalanceSheetDetailService;
import com.opl.service.loans.service.fundseeker.corporate.EntityInformationDetailService;
import com.opl.service.loans.service.fundseeker.corporate.ExcelExtractionService;
import com.opl.service.loans.service.fundseeker.corporate.LiabilitiesDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.ManagementDetailService;
import com.opl.service.loans.service.fundseeker.corporate.OperatingStatementDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.ProfitibilityStatementDetailService;

@RestController
@RequestMapping("/cw_excel")
public class ExcelExtractionController {

	private final Logger log = LoggerFactory.getLogger(ExcelExtractionController.class);

	private static final String REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG = "request parameter is null or empty";

	@Autowired
	ExcelExtractionService excelExtractionService;
	
	@Autowired
	AssetsDetailsService assetsDetailsService;
	
	@Autowired
	LiabilitiesDetailsService liabilitiesDetailsService;
	
	@Autowired
	OperatingStatementDetailsService operatingStatementDetailsService; 
	
	@Autowired
	private EntityInformationDetailService entityInformationDetailService; 
	
	@Autowired
	private ManagementDetailService managementDetailService; 
	
	@Autowired
	BalanceSheetDetailService balanceSheetDetailService;
	
	@Autowired
	ProfitibilityStatementDetailService profitibilityStatementDetailService;
	
	private static final Logger logger = LoggerFactory.getLogger(ExcelExtractionController.class);
	

//	@RequestMapping(value = "/ping", method = RequestMethod.GET)
//	public String getPing() {
//		
//		return "Welcome to Excel Extration Service...";
//	}

	@RequestMapping(value = "/read_cma", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExcelResponse> readCMA(@RequestBody ExcelRequest excelRequest) {
	
		String filePath=excelRequest.getFilePath();
		Long applicationId=excelRequest.getApplicationId();
		Long storageDetailsId=excelRequest.getStorageDetailsId();
		
		
		if(CommonUtils.isObjectNullOrEmpty(filePath) && CommonUtils.isObjectNullOrEmpty(applicationId) && CommonUtils.isObjectNullOrEmpty(storageDetailsId))
		{
			ExcelResponse res= new ExcelResponse(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG, HttpStatus.BAD_REQUEST.value());
			log.error(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		MultipartFile file=null;
		
		try {
			if(!(excelExtractionService.readCMA(applicationId,storageDetailsId,file)))
			{
				ExcelResponse res= new ExcelResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value());
				log.error("Error while reading CMA");
				return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
			}
		}catch (Exception e) {
			ExcelResponse res= new ExcelResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value());
			res.setData(e.getMessage());
			log.error("Error while reading CMA");
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}	
		
		ExcelResponse res= new ExcelResponse("CMA successfully read", HttpStatus.OK.value());
		return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/inactive_cma", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExcelResponse> inActiveCMA(@RequestBody ExcelRequest excelRequest) {
		logger.warn("in inactivate cma");
		Long storageDetailsId=excelRequest.getStorageDetailsId();
		logger.warn("storage is==>"+storageDetailsId);
		
		if(CommonUtils.isObjectNullOrEmpty(storageDetailsId))
		{
			ExcelResponse res= new ExcelResponse(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG, HttpStatus.BAD_REQUEST.value());
			log.error(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		try {
			logger.warn("in try to in activate");
			assetsDetailsService.inActiveAssetsDetails(storageDetailsId);
			liabilitiesDetailsService.inActiveAssetsDetails(storageDetailsId);
			operatingStatementDetailsService.inActiveAssetsDetails(storageDetailsId);
		} catch (Exception e) {
			
			ExcelResponse res= new ExcelResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value());
			log.error("Error while inactive CMA : ",e);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		
		ExcelResponse res= new ExcelResponse("CMA Detailes inActivated", HttpStatus.OK.value());
		return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/read_dpr", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExcelResponse> extractDPR(@RequestBody ExcelRequest excelRequest) {
	
		String filePath=excelRequest.getFilePath();
		Long applicationId=excelRequest.getApplicationId();
		Long storageDetailsId=excelRequest.getStorageDetailsId();
		MultipartFile file=null;
		
		if(CommonUtils.isObjectNullOrEmpty(filePath) || CommonUtils.isObjectNullOrEmpty(applicationId) || CommonUtils.isObjectNullOrEmpty(storageDetailsId))
		{
			ExcelResponse res= new ExcelResponse(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG, HttpStatus.OK.value());
			log.error(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		
		if(!(excelExtractionService.readDPR(applicationId,storageDetailsId,file)))
		{
			ExcelResponse res= new ExcelResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.OK.value());
			log.error("Error while reading DPR");
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		ExcelResponse res= new ExcelResponse("DPR successfully read", HttpStatus.OK.value());
		return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/inactive_dpr", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExcelResponse> inActiveDPR(@RequestBody ExcelRequest excelRequest) {
	
		Long storageDetailsId=excelRequest.getStorageDetailsId();
		
		
		if(CommonUtils.isObjectNullOrEmpty(storageDetailsId))
		{
			ExcelResponse res= new ExcelResponse(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG, HttpStatus.OK.value());
			log.error(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		try {
			entityInformationDetailService.inActiveEntityInformationDetails(storageDetailsId);
			managementDetailService.inActiveManagementDetails(storageDetailsId);
		} catch (Exception e) {
			
			ExcelResponse res= new ExcelResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.OK.value());
			log.error("Error while inactive DPR : ",e);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		
		ExcelResponse res= new ExcelResponse("DPR Detailes inActivated", HttpStatus.OK.value());
		return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		
	}
	
	
	@RequestMapping(value = "/read_bs", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExcelResponse> readBS(@RequestBody ExcelRequest excelRequest) {
	
		String filePath=excelRequest.getFilePath();
		Long applicationId=excelRequest.getApplicationId();
		Long storageDetailsId=excelRequest.getStorageDetailsId();
		MultipartFile file=null;
		
		if(CommonUtils.isObjectNullOrEmpty(filePath) && CommonUtils.isObjectNullOrEmpty(applicationId) && CommonUtils.isObjectNullOrEmpty(storageDetailsId))
		{
			ExcelResponse res= new ExcelResponse(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG, HttpStatus.BAD_REQUEST.value());
			log.error(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		
		if(!(excelExtractionService.readBS(applicationId,storageDetailsId,file)))
		{
			ExcelResponse res= new ExcelResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value());
			log.error("Error while reading BS");
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		ExcelResponse res= new ExcelResponse("BS successfully read", HttpStatus.OK.value());
		return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		
	}
	
	
	@RequestMapping(value = "/inactive_bs", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExcelResponse> inActiveBS(@RequestBody ExcelRequest excelRequest) {
	
		Long storageDetailsId=excelRequest.getStorageDetailsId();
		
		
		if(CommonUtils.isObjectNullOrEmpty(storageDetailsId))
		{
			ExcelResponse res= new ExcelResponse(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG, HttpStatus.BAD_REQUEST.value());
			log.error(REQUEST_PARAMETER_IS_NULL_OR_EMPTY_MSG);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		try {
			balanceSheetDetailService.inActiveBalanceSheetDetail(storageDetailsId);
			profitibilityStatementDetailService.inActiveProfitibilityStatementDetail(storageDetailsId);
		} catch (Exception e) {
			
			ExcelResponse res= new ExcelResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value());
			log.error("Error while inactive BS : ",e);
			return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		}
		
		
		ExcelResponse res= new ExcelResponse("BS Detailes inActivated", HttpStatus.OK.value());
		return new ResponseEntity<ExcelResponse>(res,HttpStatus.OK);
		
	}
	

}
