package com.opl.service.loans.repository.fundseeker;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.ApplicationStatusAudit;


public interface ApplicationStatusAuditRepository extends JpaRepository<ApplicationStatusAudit, Long> {

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.applicationStatusMaster.id =:id and lm.npAssigneeId=:assigneeId and  lm.isActive = true ")
	public List<ApplicationStatusAudit> getApplicationByAssigneeIdBasedOnStatus(@Param("appId")Long applicationId,@Param("id") Long applicationStatusId,@Param("assigneeId") Long assigneeId);

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.applicationId=:proposalId and lm.applicationStatusMaster.id =:id and lm.npAssigneeId=:assigneeId and  lm.isActive = true ")
	public List<ApplicationStatusAudit> getApplicationByAssigneeIdBasedOnStatus(@Param("appId")Long applicationId,@Param("proposalId")Long proposalId,@Param("id") Long applicationStatusId,@Param("assigneeId") Long assigneeId);
	
	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.applicationStatusMaster.id =:id and lm.npUserId=:npUserId and  lm.isActive = true ")
	public List<ApplicationStatusAudit> getApplicationByNpUserIdBasedOnStatus(@Param("appId")Long applicationId,@Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId);

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.proposalId=:proposalId and lm.applicationStatusMaster.id =:id and lm.npUserId=:npUserId and  lm.isActive = true ")
	public List<ApplicationStatusAudit> getApplicationByAndProposalIdNpUserIdBasedOnStatus(@Param("appId")Long applicationId,@Param("proposalId")Long proposalId,@Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId);

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.applicationStatusMaster.id =:id and  lm.isActive = true order by lm.modifiedDate desc")
	public List<ApplicationStatusAudit> getApplicationByUserIdBasedOnStatusForFPMaker(@Param("appId")Long applicationId,@Param("id") Long applicationStatusId);

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.applicationStatusMaster.id =:id and lm.proposalId=:proposalId and lm.isActive = true order by lm.modifiedDate desc")
	public List<ApplicationStatusAudit> getApplicationByUserIdBasedOnStatusForFPMaker(@Param("appId")Long applicationId,@Param("proposalId")Long proposalId,@Param("id") Long applicationStatusId);

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.applicationStatusMaster.id =:id and  lm.isActive = true and lm.proposalId=:proposalId order by lm.modifiedDate desc")
	public List<ApplicationStatusAudit> getApplicationByUserIdAndProposalIdBasedOnStatusForFPMaker(@Param("appId")Long applicationId,@Param("proposalId")Long proposalId,@Param("id") Long applicationStatusId);

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.ddrStatusId =:id and  lm.isActive = true order by lm.modifiedDate desc")
	public List<ApplicationStatusAudit> getApplicationByUserIdBasedOnDDRStatusForFPChecker(@Param("appId")Long applicationId,@Param("id") Long ddrStatusId);

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.ddrStatusId =:id and  lm.isActive = true and lm.proposalId=:proposalId order by lm.modifiedDate desc")
	public List<ApplicationStatusAudit> getApplicationByUserIdAndProposalIdBasedOnDDRStatusForFPChecker(@Param("appId")Long applicationId,@Param("id") Long ddrStatusId,@Param("proposalId")Long proposalId);

	@Query("select lm from ApplicationStatusAudit lm where lm.applicationId=:appId and lm.applicationStatusMaster.id=:id and lm.isActive = true and lm.proposalId=:proposalId order by lm.modifiedDate desc")
	public List<ApplicationStatusAudit> getApplicationByUserIdAndProposalIdBasedOnStatusForFPChecker(@Param("appId")Long applicationId,@Param("id") Long ddrStatusId,@Param("proposalId")Long proposalId);
}
