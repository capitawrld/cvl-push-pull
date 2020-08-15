package com.opl.service.loans.service.fundseeker.corporate.impl;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.corporate.ScotAnalysisDetailRepository;
import com.opl.service.loans.service.fundseeker.corporate.ScotService;
import com.opl.service.loans.utils.dpr.DprTenthSheetExcelReader;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class ScotServiceImpl implements ScotService {
	
	@Autowired
	LoanApplicationRepository loanApplicationRepository;
	
	@Autowired
	ScotAnalysisDetailRepository scotAnalysisDetailRepository;

	@Override
	public void readScotDetails(Long applicationId, Long storageDetailsId, XSSFSheet scotSheet) {
		DprTenthSheetExcelReader.run(storageDetailsId, scotSheet,
				loanApplicationRepository.findOne(applicationId), scotAnalysisDetailRepository);
		
	}

	@Override
	public void inActiveScotDetails(Long storageDetailsId) {
		scotAnalysisDetailRepository.inActiveScotDetails(storageDetailsId);
		
	}

}
