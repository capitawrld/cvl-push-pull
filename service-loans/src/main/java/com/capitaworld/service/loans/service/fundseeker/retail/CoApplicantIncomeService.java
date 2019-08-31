package com.capitaworld.service.loans.service.fundseeker.retail;

import java.util.List;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.retail.RetailApplicantIncomeRequest;

public interface CoApplicantIncomeService {
	
	public boolean save(RetailApplicantIncomeRequest appIncomeReq) throws LoansException;
	
	public boolean saveAll(List<RetailApplicantIncomeRequest> appIncomeReqList) throws LoansException;
	
	public List<RetailApplicantIncomeRequest> getAll(Long applicationId);
	
	public List<RetailApplicantIncomeRequest> get(Long coApplicantId);

	public List<RetailApplicantIncomeRequest> getAllByCoAppId(Long coAppId);
	
	public Boolean saveOrUpdateIncomeDetailForGrossIncome(FrameRequest frameRequest) throws LoansException;
}
