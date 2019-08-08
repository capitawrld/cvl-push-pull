package com.capitaworld.service.loans.service.fundseeker.corporate;

import com.capitaworld.service.loans.model.api_model.FinancialRequest;
import com.capitaworld.service.loans.model.corporate.CMARequest;

public interface CMAService {

	public void saveCMA(CMARequest cmaRequest);
	
	public CMARequest getCMA(Long applicationId);
	
	public FinancialRequest getFinancialDetailsForBankIntegration(Long applicationId, Long proposalId);
	
}
