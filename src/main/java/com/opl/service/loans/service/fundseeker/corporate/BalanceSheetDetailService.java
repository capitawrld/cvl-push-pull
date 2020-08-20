package com.opl.service.loans.service.fundseeker.corporate;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.opl.service.loans.domain.fundseeker.corporate.BalanceSheetDetail;

public interface BalanceSheetDetailService {

	public void saveOrUpdate(BalanceSheetDetail balanceSheetDetail);
	
	public void readBalanceSheetDetails(Long applicationId,Long storageDetailsId,XSSFSheet sheet);
	
	public void inActiveBalanceSheetDetail(Long storageDetailsId);
}
