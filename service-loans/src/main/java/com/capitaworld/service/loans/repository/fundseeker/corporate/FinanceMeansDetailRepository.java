package com.capitaworld.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitaworld.service.loans.domain.fundseeker.corporate.FinanceMeansDetail;

public interface FinanceMeansDetailRepository extends JpaRepository<FinanceMeansDetail, Long> {

	@Query("from FinanceMeansDetail  a where a.applicationId.id=:id and a.applicationId.userId =:userId AND a.isActive=true")
	public List<FinanceMeansDetail> listFinanceMeansFromAppId(@Param("id") Long id, @Param("userId") Long userId);

	@Query("from FinanceMeansDetail  a where a.proposalId.proposalId=:proposalId AND a.isActive=true")
	public List<FinanceMeansDetail> listFinanceMeansFromProposalId(@Param("proposalId") Long proposalId);
	
	@Modifying
	@Query("update FinanceMeansDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId);

	
}
