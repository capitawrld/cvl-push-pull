package com.capitaworld.service.loans.controller.fundseeker.retail;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.capitaworld.api.reports.ReportRequest;
import com.capitaworld.client.reports.ReportsClient;
import com.capitaworld.service.dms.client.DMSClient;
import com.capitaworld.service.dms.model.DocumentResponse;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.service.fundseeker.retail.PLCamReportService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.DDRMultipart;

@RestController
@RequestMapping("/cam/pl")
public class PLCamReportController {

	@Autowired
	private PLCamReportService plCamReportService; 
	
	@Autowired
	private ReportsClient reportsClient;

	@Autowired
	private DMSClient dmsClient;

	private static final Logger logger = LoggerFactory.getLogger(PLCamReportController.class);

	@RequestMapping(value = "/getPrimaryPlCamData/{applicationId}/{productMappingId}/{proposalId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPrimaryPlCamDataByProposalId(@PathVariable(value = "proposalId") Long proposalId, @PathVariable(value = "applicationId") Long applicationId, @PathVariable(value = "productMappingId") Long productId, HttpServletRequest request) {
		
		if (CommonUtils.isObjectNullOrEmpty(applicationId) || CommonUtils.isObjectNullOrEmpty(productId) || CommonUtils.isObjectNullOrEmpty(proposalId)) {
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId + productId + proposalId);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),HttpStatus.OK);
		}
		try {
			Map<String, Object> response = plCamReportService.getCamReportDetailsByProposalId(applicationId, productId,proposalId, false);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("PLCAMPRIMARY");
			reportRequest.setType("PLCAMPRIMARY");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			MultipartFile multipartFile = new DDRMultipart(byteArr);
			JSONObject jsonObj = new JSONObject();

			jsonObj.put("applicationId", applicationId);
			jsonObj.put("productDocumentMappingId", 355L);
			jsonObj.put("userType", CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
			jsonObj.put("originalFileName", "PLCAMPRIMARYREPORT" + applicationId + ".pdf");

			DocumentResponse documentResponse = dmsClient.uploadFile(jsonObj.toString(), multipartFile);
			if (documentResponse.getStatus() == 200) {
				logger.info(""+documentResponse);
				return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), "success", documentResponse.getData(), response),HttpStatus.OK);
			} else {
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while getting MAP Details==>", e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getFinalPlCamData/{applicationId}/{productMappingId}/{proposalId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getFinalDataMap(@PathVariable(value = "proposalId") Long proposalId, @PathVariable(value = "applicationId") Long applicationId, @PathVariable(value = "productMappingId") Long productId, HttpServletRequest request) {
		
		if (CommonUtils.isObjectNullOrEmpty(applicationId) || CommonUtils.isObjectNullOrEmpty(productId)) {
			logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId + productId);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()),HttpStatus.OK);
		}
		try {
			Map<String, Object> response = plCamReportService.getCamReportDetailsByProposalId(applicationId, productId,proposalId, true);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("PLCAMFINAL");
			reportRequest.setType("PLCAMFINAL");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			MultipartFile multipartFile = new DDRMultipart(byteArr);
			JSONObject jsonObj = new JSONObject();

			jsonObj.put("applicationId", applicationId);
			jsonObj.put("productDocumentMappingId", 362L);
			jsonObj.put("userType", CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
			jsonObj.put("originalFileName", "PLCAMFINALREPORT" + applicationId + ".pdf");

			DocumentResponse documentResponse = dmsClient.uploadFile(jsonObj.toString(), multipartFile);
			if (documentResponse.getStatus() == 200) {
				logger.info(""+documentResponse);
				return new ResponseEntity<LoansResponse>(new LoansResponse(HttpStatus.OK.value(), "success", documentResponse.getData(), response),HttpStatus.OK);
			} else {
				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while getting MAP Details==>", e);
			return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * cam generate for gateway
	 * @return  byte[]
	 * */

	@GetMapping(value = "/getPlPrimaryDataInByteArray/{applicationId}/{productMappingId}/{proposalId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getPlPrimaryDataInByteArray(@PathVariable(value = "applicationId") Long applicationId,@PathVariable(value = "productMappingId") Long productId, 
			@PathVariable(value = "proposalId") Long proposalId)  {

		if (CommonUtils.isObjectNullOrEmpty(applicationId)||CommonUtils.isObjectNullOrEmpty(productId)||CommonUtils.isObjectListNull(proposalId)) {
				logger.warn(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, applicationId + productId + proposalId);

				return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_DATA_OR_REQUESTED_DATA_NOT_FOUND, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
		}
		try {
			Map<String,Object> response = plCamReportService.getCamReportDetailsByProposalId(applicationId,productId,proposalId,false);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setParams(response);
			reportRequest.setTemplate("PLCAMPRIMARY");
			reportRequest.setType("PLCAMPRIMARY");
			byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
			if(byteArr != null){
				return new ResponseEntity<LoansResponse>(new LoansResponse("Success",HttpStatus.OK.value(), byteArr),HttpStatus.OK);
			}else{
				 return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error("Error while getting PL MAP Details==>", e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
}
