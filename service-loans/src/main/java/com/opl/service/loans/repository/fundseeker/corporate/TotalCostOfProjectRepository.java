package com.opl.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.corporate.TotalCostOfProject;

public interface TotalCostOfProjectRepository extends JpaRepository<TotalCostOfProject, Long> {

	@Query("from TotalCostOfProject  a where a.applicationId.id=:id and a.applicationId.userId =:userId AND a.isActive=true")
	public List<TotalCostOfProject> listCostOfProjectFromAppId(@Param("id") Long id, @Param("userId") Long userId);

	@Query("from TotalCostOfProject  a where a.proposalId.proposalId=:proposalId AND a.isActive=true")
	public List<TotalCostOfProject> listCostOfProjectFromProposalId(@Param("proposalId") Long id);
	
	@Modifying
	@Query("update TotalCostOfProject pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId);

	
}
