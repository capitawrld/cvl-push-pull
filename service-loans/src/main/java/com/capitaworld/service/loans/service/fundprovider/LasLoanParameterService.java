package com.capitaworld.service.loans.service.fundprovider;

import com.capitaworld.service.loans.model.LasParameterRequest;

public interface LasLoanParameterService {
	public boolean saveOrUpdate(LasParameterRequest lasParameterRequest);
	
	public LasParameterRequest getLasParameterRequest(Long id);
}
