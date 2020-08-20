package com.opl.service.loans.service.fundseeker.corporate.impl;

import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.service.loans.domain.fundseeker.corporate.DprUserDataDetail;
import com.opl.service.loans.repository.fundseeker.corporate.DprUserDataDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.service.fundseeker.corporate.DprUserDataDetailService;
import com.opl.service.loans.utils.dpr.DprThirdSheetExcelReader;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class DprUserDataDetailServiceImpl implements DprUserDataDetailService {
	
	@Autowired
	DprUserDataDetailRepository dprUserDataDetailRepository; 
	
	@Autowired
	LoanApplicationRepository loanApplicationRepository; 

	@Override
	public void readDprUserDataDetails(Long applicationId, Long storageDetailsId,
			XSSFSheet productsSheet, DprUserDataDetail dprUserDataDetail) {
		
		DprThirdSheetExcelReader.run(storageDetailsId, productsSheet,
					loanApplicationRepository.findOne(applicationId), dprUserDataDetail);
		
	}

	@Override
	public void inActiveDprUserDataDetails(Long storageDetailsId) {
		dprUserDataDetailRepository.inActiveDprUserDataDetails(storageDetailsId);
		
	}

	@Override
	public void save(Long storageDetailsId, DprUserDataDetail dprUserDataDetail, Long applicationId) {
		
		dprUserDataDetail.setStorageDetailsId(storageDetailsId);
		dprUserDataDetail.setIsActive(true);
		dprUserDataDetail.setCreatedDate(new Date());
		dprUserDataDetail.setModifiedDate(new Date());
		dprUserDataDetail.setApplicationId(loanApplicationRepository.findOne(applicationId));
		dprUserDataDetailRepository.save(dprUserDataDetail);
		
	}

}
