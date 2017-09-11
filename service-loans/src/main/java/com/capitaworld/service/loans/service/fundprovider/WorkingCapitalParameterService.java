package com.capitaworld.service.loans.service.fundprovider;

import java.util.List;

import com.capitaworld.service.loans.model.corporate.WorkingCapitalParameterRequest;

public interface WorkingCapitalParameterService {
	public boolean saveOrUpdate(WorkingCapitalParameterRequest workingCapitalParameterRequest);
	
	public WorkingCapitalParameterRequest getWorkingCapitalParameter(Long id);
	
	public List<WorkingCapitalParameterRequest> getWorkingCapitalParameterListByUserId(Long id);
}
