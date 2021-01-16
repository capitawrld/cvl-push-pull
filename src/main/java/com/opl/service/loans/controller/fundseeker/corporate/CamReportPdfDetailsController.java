package com.opl.service.loans.controller.fundseeker.corporate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opl.mudra.api.dms.model.DocumentResponse;
import com.opl.mudra.api.gst.exception.GstException;
import com.opl.mudra.api.gst.model.GstResponse;
import com.opl.mudra.api.gst.model.yuva.request.GSTR1Request;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.CommonUtils.LoanType;
import com.opl.mudra.api.reports.ReportRequest;
import com.opl.mudra.client.dms.DMSClient;
import com.opl.mudra.client.gst.GstClient;
import com.opl.mudra.client.reports.ReportsClient;
import com.opl.service.loans.service.fundseeker.corporate.CamReportPdfDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.InEligibleProposalCamReportService;
import com.opl.service.loans.service.fundseeker.retail.HLCamReportService;
import com.opl.service.loans.utils.DDRMultipart;

@RestController
@RequestMapping("/cam")
public class CamReportPdfDetailsController {
	
	@Autowired
	private CamReportPdfDetailsService camReportPdfDetailsService;
	
	@Autowired
	private InEligibleProposalCamReportService inEligibleProposalCamReportService;
	
	@Autowired
	private ReportsClient reportsClient;
	
	@Autowired
	private DMSClient dmsClient;
	
	@Autowired
	private HLCamReportService hlCamReportService;
	
	private static final Logger logger = LoggerFactory.getLogger(CamReportPdfDetailsController.class);

	private static final String SUCCESS_LITERAL = "success";
	private static final String PRODUCT_DOCUMENT_MAPPING_ID = "productDocumentMappingId";
	private static final String USER_TYPE = "userType";
	private static final String ORIGINAL_FILE_NAME = "originalFileName";
	private static final String ERROR_WHILE_GETTING_MAP_DETAILS = "Error while getting MAP Details==>";
	private static final String INELIGIBLE_CAM_REPORT = "INELIGIBLECAMREPORT";
	private static final String INELIGIBLE_CAM_REPORT_MUDRA = "CVLMUDRALOANINELIGIBLECAM";

	
	/* MUDRA LOAN PRIMARY CAM REPORT */
	@GetMapping(value = {"/getPrimaryDataMap/{applicationId}/{productMappingId}/{proposalId}" ,"/getPrimaryDataMap/{applicationId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPrimaryDataMap(@PathVariable(value = "applicationId") Long applicationId,@PathVariable(name = "productMappingId" ,required = false) Long productId,
			@PathVariable(name ="proposalId" ,required = false) Long proposalId ,HttpServletRequest request)  {
		
		ReportRequest reportRequest = new ReportRequest();
		if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, + applicationId);
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			Map<String,Object> response = camReportPdfDetailsService.getCamReportPrimaryDetails(applicationId,productId,proposalId,false);
			
			reportRequest.setParams(response);
			reportRequest.setTemplate("CVLMUDRALOANPRIMARYCAM");
			reportRequest.setType("CVLMUDRALOANPRIMARYCAM");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			MultipartFile multipartFile = new DDRMultipart(byteArr);			  
			  JSONObject jsonObj = new JSONObject();
				jsonObj.put(CommonUtils.APPLICATION_ID, applicationId);
				jsonObj.put(PRODUCT_DOCUMENT_MAPPING_ID, 355L);
				jsonObj.put(USER_TYPE, CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
				jsonObj.put(ORIGINAL_FILE_NAME, "CVLMUDRALOANPRIMARYCAM"+proposalId +".pdf");
				
				DocumentResponse  documentResponse  =  dmsClient.uploadFile(jsonObj.toString(), multipartFile);
				if(documentResponse.getStatus() == 200){
					logger.info(""+documentResponse);
					return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), SUCCESS_LITERAL, documentResponse.getData(), response),HttpStatus.OK);
				}else{
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
				}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value(), reportRequest),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * cam generate for gateway
	 * @return  byte[]
	 * */

	@GetMapping(value = "/getPrimaryDataInByteArray/{applicationId}/{productMappingId}/{proposalId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public byte[] getPrimaryDataInByteArray(@PathVariable(value = "applicationId") Long applicationId,@PathVariable(value = "productMappingId") Long productId, 
			@PathVariable(value = "proposalId") Long proposalId)  {

		if (CommonUtils.isObjectNullOrEmpty(applicationId)||CommonUtils.isObjectNullOrEmpty(productId)||CommonUtils.isObjectListNull(proposalId)) {
				logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId + productId + proposalId);
				return null;
		}
		try {

			Map<String,Object> response = camReportPdfDetailsService.getCamReportPrimaryDetails(applicationId,productId,proposalId,false);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("CVLMUDRALOANPRIMARYCAM");
			reportRequest.setType("INPRINCIPLECAM");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			
			if(byteArr != null && byteArr.length > 0){
				try {
					MultipartFile multipartFile = new DDRMultipart(byteArr);			  
					JSONObject jsonObj = new JSONObject();
					jsonObj.put(CommonUtils.APPLICATION_ID, applicationId);
					jsonObj.put(PRODUCT_DOCUMENT_MAPPING_ID, 594L);
					jsonObj.put(USER_TYPE, CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
					jsonObj.put(ORIGINAL_FILE_NAME, "INPRINCIPLECAM"+proposalId+".pdf");
					DocumentResponse  documentResponse  =  dmsClient.uploadFile(jsonObj.toString(), multipartFile);
					logger.info("InPrinciple CAM Uploaded for ==>"+applicationId);
				} catch (Exception e) {
					logger.info("Error while Generated InPrinciple-Letter upload to dms for==>"+applicationId);
					logger.info("Error: "+e);
				}
				
				return byteArr;
//				return new ResponseEntity<LoansResponse>(new LoansResponse(SUCCESS_LITERAL,HttpStatus.OK.value(), byteArr),HttpStatus.OK);
			}else{
				 logger.info("Byte array not generated at inPrinciple time for ==>"+applicationId);
				 return null;
//				 return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return null;
//			return new ResponseEntity<LoansResponse>(
//					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
//					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	
	@GetMapping(value = "/getFinalDataMap/{applicationId}/{productMappingId}/{proposalId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFinalDataMap(@PathVariable(value = "applicationId") Long applicationId,@PathVariable(value = "productMappingId") Long productId,
			@PathVariable(value ="proposalId") Long proposalId,HttpServletRequest request)  {
		
		if (CommonUtils.isObjectNullOrEmpty(applicationId)||CommonUtils.isObjectNullOrEmpty(productId)||CommonUtils.isObjectNullOrEmpty(proposalId)) {
				logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId + productId+proposalId);
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {

			Map<String,Object> response = camReportPdfDetailsService.getCamReportPrimaryDetails(applicationId,productId,proposalId,true);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("CAMREPORTFINALSIDBI");
			reportRequest.setType("CAMREPORTFINALSIDBI");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			MultipartFile multipartFile = new DDRMultipart(byteArr);			  
			  JSONObject jsonObj = new JSONObject();

				jsonObj.put(CommonUtils.APPLICATION_ID, applicationId);
				jsonObj.put(PRODUCT_DOCUMENT_MAPPING_ID, 362L);
				jsonObj.put(USER_TYPE, CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
				jsonObj.put(ORIGINAL_FILE_NAME, "CAMREPORTSIDBI"+applicationId+".pdf");
				
				DocumentResponse  documentResponse  =  dmsClient.uploadFile(jsonObj.toString(), multipartFile);
				if(documentResponse.getStatus() == 200){
				logger.info("DocumentResponse Data==>{}",documentResponse.getData());
				return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), SUCCESS_LITERAL, documentResponse.getData(), response),HttpStatus.OK);
				}
				else{
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
				}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@GetMapping(value = "/getBankStatementAnalysis/{applicationId}/{productMappingId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getBankStatementAnalysis(@PathVariable(value = "applicationId") Long applicationId,@PathVariable(value = "productMappingId") Long productId, HttpServletRequest request)  {
		
		if (CommonUtils.isObjectNullOrEmpty(applicationId)||CommonUtils.isObjectNullOrEmpty(productId)) {
				logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId + productId);
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {

			Map<String,Object> response = camReportPdfDetailsService.getBankStatementAnalysisReport(applicationId,productId);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("BANKSTATEMENTANALYSIS");
			reportRequest.setType("BANKSTATEMENTANALYSIS");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			MultipartFile multipartFile = new DDRMultipart(byteArr);			  
			  JSONObject jsonObj = new JSONObject();

				jsonObj.put(CommonUtils.APPLICATION_ID, applicationId);
				jsonObj.put(PRODUCT_DOCUMENT_MAPPING_ID,553L);
				jsonObj.put(USER_TYPE, CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
				jsonObj.put(ORIGINAL_FILE_NAME, "BANK_STATEMENT_ANALYSIS"+applicationId+".pdf");
				
				DocumentResponse  documentResponse  =  dmsClient.uploadFile(jsonObj.toString(), multipartFile);
				if(documentResponse.getStatus() == 200){
				logger.info("DocumentResponse Data==>{}",documentResponse.getData());
				return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), SUCCESS_LITERAL, documentResponse.getData(), response),HttpStatus.OK);
				}
				else{
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
				}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@GetMapping(value = "/getInEligiblePrimaryDataMap/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPrimaryDataMap(@PathVariable(value = "applicationId") Long applicationId)  {

		if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId);
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {

			Map<String,Object> response = inEligibleProposalCamReportService.getInEligibleCamReport(applicationId);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("CVLMUDRALOANINELIGIBLECAM");
			reportRequest.setType("CVLMUDRALOANINELIGIBLECAM");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			MultipartFile multipartFile = new DDRMultipart(byteArr);
			  JSONObject jsonObj = new JSONObject();


				jsonObj.put(CommonUtils.APPLICATION_ID, applicationId);
				jsonObj.put(PRODUCT_DOCUMENT_MAPPING_ID, 570L);
				jsonObj.put(USER_TYPE, CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
				jsonObj.put(ORIGINAL_FILE_NAME, INELIGIBLE_CAM_REPORT+applicationId+".pdf");

				DocumentResponse  documentResponse  =  dmsClient.uploadFile(jsonObj.toString(), multipartFile);
				if(documentResponse.getStatus() == 200){
				logger.info("DocumentResponse Data==>{}",documentResponse);
				return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), SUCCESS_LITERAL, documentResponse.getData(), response),HttpStatus.OK);
				}else{
					 return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
				}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@GetMapping(value = "/getInEligiblePrimaryDataMapinByteArray/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public byte[] getInEligiblePrimaryDataMapinByteArray(@PathVariable(value = "applicationId") Long applicationId)  {

		if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
				logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId);
				return null;
		}
		try {

			Map<String,Object> response = inEligibleProposalCamReportService.getInEligibleCamReport(applicationId);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("CVLMUDRALOANINELIGIBLECAM");
			reportRequest.setType("CVLMUDRALOANINELIGIBLECAM");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			if(byteArr != null && byteArr.length > 0){
				return byteArr;
			}
				
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
		}
		return null;
	}

	@GetMapping(value = "/getGstDataReport/{panNo}" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getGSTDataReport(@PathVariable(value = "panNo") String panNo, HttpServletResponse  httpServletResponse) {
		logger.info("Into get Gst Data Report ");
		if (CommonUtils.isObjectNullOrEmpty(panNo)) {
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, panNo);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),HttpStatus.OK);
		}
		try {
			Map<String, Object> response = camReportPdfDetailsService.getGstReportData(panNo);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("GSTDATAREPORT");
			reportRequest.setType("GSTDATAREPORT");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);

			if (byteArr != null && byteArr.length > 0) {
				httpServletResponse.setContentType("application/octet-stream");
				httpServletResponse.setHeader("Content-Disposition", String.format("inline; filename=GST\"" + " GSTDataReport.pdf" + "\""));
				httpServletResponse.setContentLength((int) byteArr.length);
				InputStream inputStream = new ByteArrayInputStream(byteArr); 
				FileCopyUtils.copy(inputStream, httpServletResponse.getOutputStream());
				return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), SUCCESS_LITERAL, response),HttpStatus.OK);
			} else {
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Autowired
	private GstClient gstClient;
	
	@GetMapping(value = "/getGstSpecificDataReport/{panNo}/{fpUserId}/{fsUserId}" , produces = MediaType.APPLICATION_JSON_VALUE)
	public byte[] getGstSpecificDataReport(@PathVariable(value = "panNo") String panNo ,@PathVariable(value = "fpUserId") Long fpUserId,@PathVariable(value = "fsUserId") Long fsUserId, HttpServletResponse  httpServletResponse,HttpServletRequest httpReq) {
		logger.info("Into get Gst Specific Data Report with panNo==>{} , fpUserId==>{} and FsUserId==>{}" , panNo , fpUserId ,fsUserId);
		if(CommonUtils.isObjectNullOrEmpty(panNo) || CommonUtils.isObjectNullOrEmpty(fpUserId) || CommonUtils.isObjectNullOrEmpty(fsUserId)) {
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND , panNo);
			return null;
		}
		try {
			List<Map<String, Object>> dataMapList =  (List<Map<String, Object>>) callGstClient(panNo, fpUserId, fsUserId);

			//handle exponential value ussue
			for (Map<String, Object> map : dataMapList) {
				Iterable<Map<String,Object>> values = ((Map) map.get("financialYearWiseData")).values();
				for (Map<String,Object> mp : values)
					mp.computeIfPresent("yearWiseTotalPurchases", (k,v)->BigDecimal.valueOf((Double)v).setScale(1).toString());
			}

			//use data
			Map<String, Object> mapData = new HashMap<String, Object>();
			mapData.put("gstSpecificData", dataMapList);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(mapData);
			reportRequest.setTemplate("GSTSPECIFICDATA");
			reportRequest.setType("GSTSPECIFICDATA");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			
			if (byteArr != null && byteArr.length > 0) {
				return byteArr;
			}else {
				return null;
			}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return null;
		}
	}

	private Object callGstClient(String panNo, Long fpUserId, Long fsUserId) throws GstException {
		GSTR1Request gstr1Request = new GSTR1Request();
		gstr1Request.setPan(panNo);
		gstr1Request.setFpUserId(fpUserId);
		gstr1Request.setFsUserId(fsUserId);
		GstResponse gstResponse = gstClient.gstSpecificDetailCalculation(gstr1Request);
		return gstResponse.getData();
	}
	
	
	//APPLICATION FORM MUDRA LOAN
	@GetMapping(value = {"/getApplicationForm/{applicationId}/{loanTypeId}","/getApplicationForm/{applicationId}/{productMappingId}/{proposalId}","/getApplicationForm/{applicationId}/{productMappingId}/{proposalId}/{loanTypeId}"} , produces = MediaType.APPLICATION_JSON_VALUE)
	public byte[] getApplicationFormReport(@PathVariable(value = "applicationId") Long applicationId ,@PathVariable(name = "productMappingId" , required = false) Long productId, 
			@PathVariable(name = "proposalId" , required = false) Long proposalId ,@PathVariable(name = "loanTypeId" , required = false) Long loanTypeId, HttpServletResponse  httpServletResponse,HttpServletRequest httpReq) {
		
		logger.info("Into get Application Form Report with ApplicationId==>{} ,ProductId==>{} and ProposalId==>{} with LoanTypeId==>{}" , applicationId ,productId ,proposalId ,loanTypeId);
		if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND , applicationId);
			return null;
		}
		
		if(loanTypeId == null) {
			loanTypeId = 1l;
		}
		
		Map<String, Object> response = new HashMap<String, Object>();		
		try {
			ReportRequest reportRequest = null;
			
			if(loanTypeId == LoanType.WORKING_CAPITAL.getValue() || loanTypeId == LoanType.TERM_LOAN.getValue() || loanTypeId == LoanType.WCTL_LOAN.getValue()) {
				logger.info("Fetching Data of Personal Loan by ApplicationId==>{} ProductMappingId==>{} ProposalId==>{}" ,applicationId ,productId, proposalId);
				response = camReportPdfDetailsService.getDetailsForApplicationForm(applicationId, productId, proposalId);
				reportRequest = new ReportRequest();
				reportRequest.setParams(response);
				reportRequest.setTemplate("CVLMUDRALOANAPPLICATIONFORM");
				reportRequest.setType("CVLMUDRALOANAPPLICATIONFORM");
			}
			if(reportRequest != null && !response.isEmpty()) {
				byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
				if (byteArr != null && byteArr.length > 0) {
					return byteArr;
				}
			}else {
				logger.error("Error/Excpetion while fetching data for report for ApplicationId==>{} ,ProductId==>{} and ProposalId==>{} with LoanTypeId==>{}" , applicationId ,productId ,proposalId ,loanTypeId);
			}
		}catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
		}
		return null;
	}
		
	//only to view cam data of All types
	@GetMapping(value = {"/getCamData/{applicationId}/{productMappingId}/{proposalId}","/getCamData/{applicationId}/{productMappingId}/{proposalId}/{camType}","/getOfflineCamData/{applicationId}/{camType}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getCamDataByProposalId(@PathVariable(value = "proposalId" , required = false) Long proposalId, @PathVariable(value = "applicationId") Long applicationId, @PathVariable(value = "productMappingId" , required = false) Long productId,
			@PathVariable(name= "camType" , required = false) String camType, HttpServletRequest request) {
		
		if (CommonUtils.isObjectNullOrEmpty(applicationId)) {
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),HttpStatus.OK);
		}
		try {
			logger.info("In getCamData Method With ApplicationId==>{} ProductMappingId==>{} ProposalId==>{} camType==>{}" , 
					applicationId ,productId ,proposalId  ,camType);
			
			Boolean isFinalView = false;
			
			if("true".equals(camType)) {
				isFinalView = true;
			}
			
			Map<String, Object> response = new HashMap<String, Object>();
			if("Offline".equals(camType)){
					logger.info("Fetching Data of MSME For Offline by ApplicationId==>{}" ,applicationId);
					response = inEligibleProposalCamReportService.getInEligibleCamReport(applicationId);
			}else {
					logger.info("Fetching Data of MSME by ApplicationId==>{} ProductMappingId==>{} ProposalId==>{} with finalView==>{}" ,applicationId ,productId, proposalId, isFinalView);
					response = camReportPdfDetailsService.getCamReportPrimaryDetails(applicationId,productId,proposalId, isFinalView);
			}
			if(response != null && !response.isEmpty()) {
				logger.info("DocumentResponse Data==>{}",response);
				return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), "success", response),HttpStatus.OK);
			} else {
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(value = "/getHLBankStatementAnalysis/{applicationId}/{productMappingId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getHLBankStatementAnalysis(@PathVariable(value = "applicationId") Long applicationId,@PathVariable(value = "productMappingId") Long productId, HttpServletRequest request)  {
		
		if (CommonUtils.isObjectNullOrEmpty(applicationId)||CommonUtils.isObjectNullOrEmpty(productId)) {
				logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId + productId);
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {

			Map<String,Object> response = hlCamReportService.getHLBankStatementAnalysisReport(applicationId, productId);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("HLBANKSTATEMENTANALYSIS");
			reportRequest.setType("HLBANKSTATEMENTANALYSIS");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			MultipartFile multipartFile = new DDRMultipart(byteArr);			  
			  JSONObject jsonObj = new JSONObject();

				jsonObj.put(CommonUtils.APPLICATION_ID, applicationId);
				jsonObj.put(PRODUCT_DOCUMENT_MAPPING_ID,553L);
				jsonObj.put(USER_TYPE, CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
				jsonObj.put(ORIGINAL_FILE_NAME, "HL_BANK_STATEMENT_ANALYSIS"+applicationId+".pdf");
				
				DocumentResponse  documentResponse  =  dmsClient.uploadFile(jsonObj.toString(), multipartFile);
				if(documentResponse.getStatus() == 200){
				logger.info("DocumentResponse Data==>{}",documentResponse.getData());
				return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), SUCCESS_LITERAL, documentResponse.getData(), response),HttpStatus.OK);
				}
				else{
					return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
				}
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
}
