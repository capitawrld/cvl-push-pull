package com.capitaworld.service.loans.service.common;

import org.json.simple.JSONObject;

import com.capitaworld.service.loans.model.CMADetailResponse;
import com.capitaworld.service.loans.model.common.HomeLoanEligibilityRequest;
import com.capitaworld.service.loans.model.common.LAPEligibilityRequest;
import com.capitaworld.service.loans.model.common.LoanEligibilility;
import com.capitaworld.service.loans.model.common.PersonalLoanEligibilityRequest;

public interface LoanEligibilityCalculatorService {

	// For Home Loan
	public JSONObject getMinMaxBySalarySlab(HomeLoanEligibilityRequest homeLoanRequest) throws Exception;

	public Integer calculateTenure(LoanEligibilility eligibilility, Integer productId) throws Exception;

	public JSONObject calcHomeLoanAmount(HomeLoanEligibilityRequest homeLoanRequest) throws Exception;

	// For Personal Loan
	public JSONObject calcMinMaxForPersonalLoan(PersonalLoanEligibilityRequest eligibilityRequest) throws Exception;
	
	//For LAP
	
	public JSONObject calcMinMaxForLAP(LAPEligibilityRequest eligibilityRequest) throws Exception;
	
	public JSONObject calcLAPAmount(LAPEligibilityRequest homeLoanRequest) throws Exception;
	
	public CMADetailResponse getCMADetail(Long applicationId ); 
	
}
