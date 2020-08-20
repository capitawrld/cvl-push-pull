package com.opl.service.loans.service.fundprovider;

import java.util.List;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.corporate.WorkingCapitalParameterRequest;

public interface WorkingCapitalParameterService {
	public boolean saveOrUpdate(WorkingCapitalParameterRequest workingCapitalParameterRequest,Long mappingId);
	
	public WorkingCapitalParameterRequest getWorkingCapitalParameter(Long id);
	
	public List<WorkingCapitalParameterRequest> getWorkingCapitalParameterListByUserId(Long id);
	
	public Boolean saveMasterFromTempWc(Long mappingId) throws LoansException;

	/**
	 * @param capitalParameterRequest
	 * @return
	 */
	public Boolean saveOrUpdateTemp(WorkingCapitalParameterRequest capitalParameterRequest);

	WorkingCapitalParameterRequest getWorkingCapitalParameterTemp(Long id,Long role,Long userId);
}
