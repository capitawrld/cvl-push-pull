package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.OperatingStatementDetails;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.OperatingStatementDetailsRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.OperatingStatementDetailsService;
import com.capitaworld.service.loans.utils.cma.OperatingStatementDetailsExcelReader;

@Service
public class OperatingStatementDetailsServiceImpl implements OperatingStatementDetailsService{

	@Autowired
	OperatingStatementDetailsRepository operatingStatementDetailsRepository;
	
	@Autowired
	LoanApplicationRepository loanApplicationRepository;
	
	@Override
	public void saveOrUpdate(OperatingStatementDetails operatingStatementDetails) {
		// TODO Auto-generated method stub
		operatingStatementDetailsRepository.save(operatingStatementDetails);
		
	}

	@Override
	public void readOperatingStatementDetails(Long applicationId,Long storageDetailsId,FileInputStream file,XSSFSheet sheet){
		
	       OperatingStatementDetailsExcelReader.run(storageDetailsId,sheet, new LoanApplicationMaster(applicationId), operatingStatementDetailsRepository);
	   
	}

	@Override
	public void inActiveAssetsDetails(Long storageDetailsId) {
		// TODO Auto-generated method stub
		operatingStatementDetailsRepository.inActiveAssetsDetails(storageDetailsId);
		
	}

}
