package com.capitaworld.service.loans.service.fundseeker.corporate;

import com.capitaworld.service.loans.exceptions.ExcelException;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.capitaworld.service.loans.domain.fundseeker.corporate.AssetsDetails;

public interface AssetsDetailsService {

	public void saveOrUpdate(AssetsDetails assetsDetails);
	
	public void readAssetsDetails(Long applicationId,Long storageDetailsId,XSSFSheet sheet) throws ExcelException;
	
	public void inActiveAssetsDetails(Long storageDetailsId);
}
