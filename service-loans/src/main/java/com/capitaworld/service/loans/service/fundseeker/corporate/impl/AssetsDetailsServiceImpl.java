package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.io.FileInputStream;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.AssetsDetails;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AssetsDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.AssetsDetailsService;
import com.capitaworld.service.loans.utils.cma.AssetsDetailsExcelReader;
import com.capitaworld.service.loans.utils.cma.LiabilitiesDetailsExcelReader;

@Service
public class AssetsDetailsServiceImpl implements AssetsDetailsService{

	@Autowired
	AssetsDetailsRepository assetsDetailsRepository; 
	
	@Autowired
	LoanApplicationRepository LoanApplicationRepository;
	
	@Override
	public void saveOrUpdate(AssetsDetails assetsDetails) {
		// TODO Auto-generated method stub
		assetsDetailsRepository.save(assetsDetails);	
	}

	@Override
	public void readAssetsDetails(Long applicationId,Long storageDetailsId,FileInputStream file, XSSFSheet sheet) {
		// TODO Auto-generated method stub
	      AssetsDetailsExcelReader.run(storageDetailsId,sheet, new LoanApplicationMaster(applicationId), assetsDetailsRepository);
	       
	}

	@Override
	public void inActiveAssetsDetails(Long storageDetailsId) {
		// TODO Auto-generated method stub
		assetsDetailsRepository.inActiveAssetsDetails(storageDetailsId);
		
	}

}
