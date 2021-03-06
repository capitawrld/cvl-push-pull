package com.opl.service.loans.service.sidbi;

import java.util.List;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.corporate.TotalCostOfProjectRequest;

public interface MeansOfFinanceDetailService {

	public Boolean saveOrUpdate(FrameRequest  frameRequest) throws LoansException;
	
	public List<TotalCostOfProjectRequest> getMeansOfFinanceList(Long applicationId,Long userId) throws LoansException;

}
