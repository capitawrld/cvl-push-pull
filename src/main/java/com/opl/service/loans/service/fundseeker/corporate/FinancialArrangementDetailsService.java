package com.opl.service.loans.service.fundseeker.corporate;

import java.util.List;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.FinancialArrangementsDetailRequest;
import com.opl.mudra.api.loans.model.FrameRequest;

/**
 * @author Sanket
 *
 */
public interface FinancialArrangementDetailsService {

	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException;

	public List<FinancialArrangementsDetailRequest> getFinancialArrangementDetailsList(Long id,Long userId) throws LoansException;
	
	public List<FinancialArrangementsDetailRequest> getFinancialArrangementDetailsListForProprietorship(Long id,Long userId) throws LoansException;

	public List<FinancialArrangementsDetailRequest> getManuallyAddedFinancialArrangementDetailsList(Long applicationId);

	public List<FinancialArrangementsDetailRequest> getFinancialArrangementDetailsListDirId(Long id,Long dirId) throws LoansException;
	
	public Boolean saveOrUpdate(List<FinancialArrangementsDetailRequest> existingLoanDetailRequest,Long applicationId,Long userId);
	
	public Boolean saveOrUpdate(List<FinancialArrangementsDetailRequest> existingLoanDetailRequest,Long applicationId,Long userId,Long directorId);
	
	public Boolean saveAllExistingLoansByApplicationId(List<FinancialArrangementsDetailRequest> existingLoanDetailRequest, Long applicationId, Long userId);
	
	public FinancialArrangementsDetailRequest getTotalEmiAndSanctionAmountByApplicationId(Long applicationId);

	public Double getTotalOfEmiByApplicationIdAndDirectorId(Long applicationId,Long directorId);

	public Double getTotalEmiOfAllDirByApplicationId(Long applicationId);
	
	public Double getTotalEmiByApplicationIdSoftPing(Long applicationId);
	
	public Double getTotalEmiByApplicationIdSoftPingForCoApplicant(Long applicationId,Long coApplicantId);
	
	public Double getTotalEmiByApplicationIdSoftPing(Long coApplicantId,Long applicationId);

	public Boolean saveOrUpdateManuallyAddedLoans(List<FinancialArrangementsDetailRequest> finArrDetailRequest,Long applicationId,Long userId);

	public FinancialArrangementsDetailRequest getTotalEmiAndSanctionAmountByApplicationIdForUniforProduct(Long applicationId);

}
