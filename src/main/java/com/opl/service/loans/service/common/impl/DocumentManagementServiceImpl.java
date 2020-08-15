package com.opl.service.loans.service.common.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opl.mudra.api.dms.exception.DocumentException;
import com.opl.mudra.api.dms.model.DocumentRequest;
import com.opl.mudra.api.dms.model.DocumentResponse;
import com.opl.mudra.api.dms.utils.DocumentAlias;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.client.dms.DMSClient;
import com.opl.service.loans.service.common.DocumentManagementService;
import com.opl.service.loans.utils.CommonDocumentUtils;

/**
 * @author Sanket
 *
 */
@Service
public class DocumentManagementServiceImpl implements DocumentManagementService{
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentManagementServiceImpl.class);
	
	@Autowired
	private DMSClient dmsClient;

	 protected static final String DMS_URL = "dmsURL";
	
	 public List<Object> getDocumentDetails(Long id,String userType,Long documentMappingId) throws DocumentException{
		CommonDocumentUtils.startHook(logger, "getDocumentDetails");
	    DocumentRequest documentRequest = new DocumentRequest();
	    switch (userType) {
			case DocumentAlias.UERT_TYPE_APPLICANT:
			 documentRequest.setApplicationId(id);
			break;
			case DocumentAlias.UERT_TYPE_CO_APPLICANT:
				documentRequest.setCoApplicantId(id);
				break;
			case DocumentAlias.UERT_TYPE_GUARANTOR:
				documentRequest.setGuarantorId(id);
				break;
			default:
			break;
		}
	   
	    documentRequest.setUserType(userType);
	    documentRequest.setProductDocumentMappingId(documentMappingId);
	    try {
	        DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
	        CommonDocumentUtils.endHook(logger, "getDocumentDetails");
	        return documentResponse.getDataList();
	    } catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
	        throw new DocumentException(e.getMessage());
	    	}
		}

}
