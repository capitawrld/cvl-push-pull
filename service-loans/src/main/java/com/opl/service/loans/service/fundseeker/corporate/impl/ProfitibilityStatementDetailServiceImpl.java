package com.opl.service.loans.service.fundseeker.corporate.impl;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opl.service.loans.domain.fundseeker.corporate.ProfitibilityStatementDetail;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.corporate.ProfitibilityStatementDetailRepository;
import com.opl.service.loans.service.fundseeker.corporate.ProfitibilityStatementDetailService;
import com.opl.service.loans.utils.bs.ProfitabilityStatementExcelReader;

@Service
public class ProfitibilityStatementDetailServiceImpl implements ProfitibilityStatementDetailService{

	@Autowired
	ProfitibilityStatementDetailRepository profitibilityStatementDetailRepository;
	
	@Autowired
	LoanApplicationRepository loanApplicationRepository;
	
	@Override
	public void saveOrUpdate(ProfitibilityStatementDetail profitibilityStatementDetail) {
		profitibilityStatementDetailRepository.save(profitibilityStatementDetail);
		
	}

	@Override
	public void readProfitibilityStatementDetail(Long applicationId, Long storageDetailsId,
			XSSFSheet sheet) {
		ProfitabilityStatementExcelReader.run(storageDetailsId, sheet, loanApplicationRepository.findOne(applicationId), profitibilityStatementDetailRepository);
	}

	@Override
	public void inActiveProfitibilityStatementDetail(Long storageDetailsId) {
		profitibilityStatementDetailRepository.inActiveProfitibilityStatementDetail(storageDetailsId);
		
	}

}
