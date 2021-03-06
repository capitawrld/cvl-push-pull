package com.opl.service.loans.service.fundseeker.corporate;

import org.springframework.web.multipart.MultipartFile;

import com.opl.mudra.api.loans.exception.ExcelException;

public interface ExcelExtractionService {
	
	public Boolean readCMA(Long applicationId,Long storageDetailsId,MultipartFile multipartFile) throws Exception;

	public Boolean readCMA(Long applicationId,Long proposalMappingId,Long storageDetailsId,MultipartFile multipartFile) throws ExcelException;

	public Boolean readDPR(Long applicationId, Long storageDetailsId, MultipartFile multipartFile);
	
	public Boolean readBS(Long applicationId,Long storageDetailsId,MultipartFile multipartFile);
	
	public Boolean inActiveCMA(Long storageDetailsId);
	
	public Boolean inActiveDPR(Long storageDetailsId);
	
	public Boolean inActiveBS(Long storageDetailsId);

}
