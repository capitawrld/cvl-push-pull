package com.opl.service.loans.service.fundprovider.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.domain.fundprovider.FSParameterMapping;
import com.opl.service.loans.repository.fundprovider.FSParameterMappingRepository;
import com.opl.service.loans.service.fundprovider.FSParameterMappingService;

@Service
@Transactional
public class FSParameterMappingServiceImpl implements FSParameterMappingService {

	@Autowired
	
	private FSParameterMappingRepository fsParameterMappingRepository; 
	
	@Override
	public Boolean inactiveAndSave(Long applicationId, Integer type, List<Integer> parameterIds) {
		fsParameterMappingRepository.inactiveMapping(applicationId, type);
		return save(applicationId,type,parameterIds);
	}

	@Override
	public Boolean save(Long applicationId, Integer type, List<Integer> parameterIds) {
		if(CommonUtils.isListNullOrEmpty(parameterIds)) {
			return null;
		}
		FSParameterMapping fsParameterMapping = null;
		for(Integer parameterId : parameterIds) {
			fsParameterMapping = new FSParameterMapping();
			fsParameterMapping.setApplicationId(applicationId);
			fsParameterMapping.setType(type);
			fsParameterMapping.setParameterId(parameterId);
			fsParameterMapping.setIsActive(true);
			fsParameterMappingRepository.save(fsParameterMapping);
		}
		return true;
	}

	@Override
	public List<Integer> getParameters(Long applicationId, Integer type) {
		 return fsParameterMappingRepository.getParametersByApplicationIdAndType(applicationId, type);
	}

}
