package com.opl.service.loans.repository.fundseeker.corporate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.corporate.PrimaryWorkingCapitalLoanDetail;

public interface PrimaryWorkingCapitalLoanDetailRepository
		extends JpaRepository<PrimaryWorkingCapitalLoanDetail, Long> {

	@Query("from PrimaryWorkingCapitalLoanDetail pd where pd.applicationId.id =:applicationId and pd.applicationId.userId =:userId and pd.isActive = true")
	public PrimaryWorkingCapitalLoanDetail getByApplicationAndUserId(@Param("applicationId") Long applicationId,
			@Param("userId") Long id);
	
	public PrimaryWorkingCapitalLoanDetail findByApplicationIdIdAndIsActive(Long applicationId,Boolean isActive);
}
