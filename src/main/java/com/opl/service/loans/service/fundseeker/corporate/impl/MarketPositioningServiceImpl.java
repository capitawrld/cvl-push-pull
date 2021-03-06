package com.opl.service.loans.service.fundseeker.corporate.impl;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.service.loans.domain.fundseeker.corporate.DprUserDataDetail;
import com.opl.service.loans.repository.fundseeker.corporate.DriverForFutureGrowthDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.corporate.RevenueAndOrderBookDetailRepository;
import com.opl.service.loans.service.fundseeker.corporate.MarketPositioningService;
import com.opl.service.loans.utils.dpr.DprSixSheetExcelReader;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class MarketPositioningServiceImpl implements MarketPositioningService {

	@Autowired
	LoanApplicationRepository loanApplicationRepository;
	
	@Autowired
	RevenueAndOrderBookDetailRepository revenueAndOrderBookDetailRepository; 
	
	@Autowired
	DriverForFutureGrowthDetailRepository driverForFutureGrowthDetailRepository;
	
	@Override
	public void readMarketPositioningDetails(Long applicationId, Long storageDetailsId,
			XSSFSheet marketScenerioSheet, DprUserDataDetail dprUserDataDetail) {
		DprSixSheetExcelReader.run(storageDetailsId, marketScenerioSheet,
				loanApplicationRepository.findOne(applicationId), revenueAndOrderBookDetailRepository,driverForFutureGrowthDetailRepository,dprUserDataDetail);

	}

	@Override
	public void inActiveMarketPositioningDetails(Long storageDetailsId) {
		revenueAndOrderBookDetailRepository.inActiveRevenueAndOrderBookDetails(storageDetailsId);
		driverForFutureGrowthDetailRepository.inActiveDriverForFutureGrowthDetails(storageDetailsId);
		
	}

}
