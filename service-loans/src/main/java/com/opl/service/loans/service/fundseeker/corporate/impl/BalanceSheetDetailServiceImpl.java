package com.opl.service.loans.service.fundseeker.corporate.impl;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opl.service.loans.domain.fundseeker.corporate.BalanceSheetDetail;
import com.opl.service.loans.repository.fundseeker.corporate.BalanceSheetDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.service.fundseeker.corporate.BalanceSheetDetailService;
import com.opl.service.loans.utils.bs.BalanceSheetExcelReader;

@Service
public class BalanceSheetDetailServiceImpl implements BalanceSheetDetailService{

	@Autowired
	BalanceSheetDetailRepository balanceSheetDetailRepository;
	
	@Autowired
	LoanApplicationRepository loanApplicationRepository;
	
	@Override
	public void saveOrUpdate(BalanceSheetDetail balanceSheetDetail) {
		balanceSheetDetailRepository.save(balanceSheetDetail);
	}

	@Override
	public void readBalanceSheetDetails(Long applicationId, Long storageDetailsId,
			XSSFSheet sheet) {
		BalanceSheetExcelReader.run(storageDetailsId, sheet, loanApplicationRepository.findOne(applicationId),
				balanceSheetDetailRepository);
		
	}

	@Override
	public void inActiveBalanceSheetDetail(Long storageDetailsId) {
		balanceSheetDetailRepository.inActiveBalanceSheetDetail(storageDetailsId);
	}

}
