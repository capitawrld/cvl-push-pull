package com.opl.service.loans.repository.common;

import java.util.List;

import com.opl.service.loans.domain.common.HomeLoanEligibilityCriteria;
import com.opl.service.loans.domain.common.LAPEligibilityCriteria;
import com.opl.service.loans.domain.common.PersonalLoanEligibilityCriteria;

public interface LoanEligibilityCriteriaRepository {

	// For Home Loan
	public HomeLoanEligibilityCriteria getHomeLoanBySalarySlab(Long income, Integer type, Integer bankId);

	public Float getHomeLoanBySV(Long sv,Integer bankId);
	
	public Float getHomeLoanByMV(Long mv, Integer bankId);

	public Object[] getMinMaxRoiForHomeLoan(List<Integer> bankIds);

	// For Personal Loan
	public PersonalLoanEligibilityCriteria getPersonalLoanBySalarySlab(Long income, Integer type, Integer bankId);

	public Object[] getMinMaxRoiForPersonalLoan(List<Integer> bankIds, Integer type);

	// For LAP
	public LAPEligibilityCriteria getLAPBySalarySlab(Long income, Integer type, Integer bankId, Integer propertyType);

	public Object[] getMinMaxRoiForLAP(List<Integer> bankIds, Integer employementType, Integer propertyType);

}
