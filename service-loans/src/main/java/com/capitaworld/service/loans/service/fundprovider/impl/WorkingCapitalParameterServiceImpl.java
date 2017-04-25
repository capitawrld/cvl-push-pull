package com.capitaworld.service.loans.service.fundprovider.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capitaworld.service.loans.domain.fundprovider.WorkingCapitalParameter;
import com.capitaworld.service.loans.model.WorkingCapitalParameterRequest;
import com.capitaworld.service.loans.repository.fundprovider.ProductMasterRepository;
import com.capitaworld.service.loans.repository.fundprovider.WorkingCapitalParameterRepository;
import com.capitaworld.service.loans.service.fundprovider.WorkingCapitalParameterService;

@Service
public class WorkingCapitalParameterServiceImpl implements WorkingCapitalParameterService {
	private static final Logger logger = LoggerFactory.getLogger(WorkingCapitalParameterServiceImpl.class.getName());
	@Autowired
	private WorkingCapitalParameterRepository workingCapitalParameterRepository;
	
	@Autowired
	private ProductMasterRepository productMasterRepository;
	@Override
	public boolean saveOrUpdate(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		// TODO Auto-generated method stub
		try { 
			WorkingCapitalParameter workingCapitalParameter= new WorkingCapitalParameter();
			BeanUtils.copyProperties(workingCapitalParameterRequest, workingCapitalParameter);
			if(null!=workingCapitalParameterRequest.getFpProductId())
			workingCapitalParameter.setFpProductId(productMasterRepository.findOne(workingCapitalParameterRequest.getFpProductId()));
			workingCapitalParameter = workingCapitalParameterRepository.save(workingCapitalParameter);
			return true;
			}

		 catch (Exception e) {
			logger.info("Exception  in save workingcapital parameter :-");
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public WorkingCapitalParameterRequest getWorkingCapitalParameter(Long id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
