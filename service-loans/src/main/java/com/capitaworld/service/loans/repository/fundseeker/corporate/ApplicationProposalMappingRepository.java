package com.capitaworld.service.loans.repository.fundseeker.corporate;

import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

public interface ApplicationProposalMappingRepository extends JpaRepository<ApplicationProposalMapping, Long> {

    @Query("select count(proposalId) from ApplicationProposalMapping apm where apm.proposalId =:proposalId and apm.isFinalLocked=1 and apm.isActive = true")
    Long checkFinalDetailIsLocked(@Param("proposalId") Long proposalId);

    @Query("select count(proposalId) from ApplicationProposalMapping apm where apm.proposalId =:proposalId and apm.isPrimaryLocked=1 and apm.isActive = true")
    Long checkPrimaryDetailIsLocked(@Param("proposalId") Long proposalId);
    
    @Query("from ApplicationProposalMapping lm where lm.applicationId =:applicationId and lm.orgId =:orgId and lm.isActive = true")
	public ApplicationProposalMapping getByApplicationIdAndOrgId(@Param("applicationId") Long applicationId, @Param("orgId") Long orgId);

    @Modifying
    @Query("update ApplicationProposalMapping apm set apm.isFinalUploadFilled =:isFinalUploadFilled,apm.modifiedDate = NOW() where apm.proposalId=:proposalId AND apm.applicationId =:applicationId AND apm.isActive = true")
    public int setIsFinalUploadMandatoryFilled(@Param("proposalId") Long proposalId,
                                               @Param("applicationId") Long applicationId,
                                               @Param("isFinalUploadFilled") Boolean isFinalUploadFilled);


    @Modifying
    @Query("update ApplicationProposalMapping apm set apm.isFinalDprUploadFilled =:isFinalDprUploadFilled,apm.modifiedDate = NOW() where apm.proposalId=:proposalId and apm.id =:id and apm.isActive = true")
    public int setIsFinalDprMandatoryFilled(@Param("proposalId") Long proposalId,
                                            @Param("id") Long id,
                                            @Param("isFinalDprUploadFilled") Boolean isFinalDprUploadFilled);


    @Query("select tenure from ApplicationProposalMapping where proposalId =:proposalId")
    public Double getTenure(@Param("proposalId") Long proposalId);

    @Query("from ApplicationProposalMapping apm where apm.proposalId=:proposalId and apm.applicationId=:applicationId and apm.isActive = true order by apm.proposalId")
    public ApplicationProposalMapping getByProposalIdAndApplicationId(@Param("proposalId") Long proposalId, @Param("applicationId") Long applicationId);

    @Query(value = "select * from application_proposal_mapping lm where lm.application_id =:applicationId and lm.is_active = true order by lm.proposal_id desc limit 1",nativeQuery = true)
    public ApplicationProposalMapping getByApplicationId(@Param("applicationId") Long applicationId);

    //fp-maker - new proposal - pagination
    @Query(value = "select lm.proposal_id from application_proposal_mapping lm inner join proposal_details pd on pd.id=lm.proposal_id where pd.branch_id=:branchId and lm.status =:id and lm.org_id=:npOrgId and (lm.payment_status=:paymentStatus or lm.payment_status='ByPass') and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
    public List<BigInteger> getFPProposalsByApplicationStatusAndNpOrgIdForPagination(Pageable pageable, @Param("id") Long applicationStatusId, @Param("npOrgId")Long npOrgId, @Param("paymentStatus")String paymentStatus, @Param("branchId") Long branchId);

    //fp-maker - new proposal - count
    @Query(value = "select lm.proposal_id from application_proposal_mapping lm inner join proposal_details pd on pd.id=lm.proposal_id where pd.branch_id=:branchId and lm.status =:id and lm.org_id=:npOrgId and (lm.payment_status=:paymentStatus or lm.payment_status='ByPass') and pd.is_active=true and lm.is_active = true",nativeQuery = true)
    public List<BigInteger> getFPMakerNewProposalCount(@Param("id") Long applicationStatusId, @Param("npOrgId")Long npOrgId, @Param("paymentStatus")String paymentStatus, @Param("branchId") Long branchId);

    //fp-maker-pending tab - pagination
    @Query(value = "select lm.proposal_id from application_proposal_mapping lm inner join proposal_details pd on pd.id=lm.proposal_id where pd.branch_id=:branchId and (lm.status =:id or lm.status =:revertedId or lm.status =:submitId) and lm.fp_maker_id=:npUserId and pd.is_active=true and  lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
    public List<BigInteger> getFPAssignedTabPropsByNPUserIdForPagination(Pageable pageable,@Param("id") Long applicationStatusAssignId, @Param("revertedId") Long applicationRevertedStatusId, @Param("submitId") Long applicationSubmitStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

    //fp-maker-pending tab - count
    @Query(value = "select lm.proposal_id from application_proposal_mapping lm inner join proposal_details pd on pd.id=lm.proposal_id where pd.branch_id=:branchId and (lm.status =:id or lm.status =:revertedId or lm.status =:submitId) and lm.fp_maker_id=:npUserId and pd.is_active=true and lm.is_active = true",nativeQuery = true)
    public List<BigInteger> getFPAssignedTabPropsByNPUserIdCount(@Param("id") Long applicationStatusAssignId, @Param("revertedId") Long applicationRevertedStatusId, @Param("submitId") Long applicationSubmitStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

    //fp-maker-assigned to checker - pagination
    @Query(value = "select lm.proposal_id from application_proposal_mapping lm inner join proposal_details pd on pd.id=lm.proposal_id where pd.branch_id=:branchId and (lm.status >=:id or lm.status=5) and lm.fp_maker_id=:npUserId and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
    public List<BigInteger> getFPAssignedProposalsByNPUserIdForPagination(Pageable pageable, @Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

    //fp - maker-assigned to checker - count
    @Query(value = "select lm.proposal_id from application_proposal_mapping lm inner join proposal_details pd on pd.id=lm.proposal_id where pd.branch_id=:branchId and (lm.status >=:id or lm.status=5) and lm.fp_maker_id=:npUserId and pd.is_active=true and lm.is_active = true ",nativeQuery = true)
    public List<BigInteger> getFPMakerAssignedAndAssginedToCheckerCount(@Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

    //fp - maker - all other proposals - pagination
    @Query(value = "select lm.proposal_id from application_proposal_mapping lm inner join proposal_details pd on pd.id=lm.proposal_id where pd.branch_id=:branchId and lm.status >=:id and lm.fp_maker_id!=:npUserId and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
    public List<BigInteger> getFPProposalsWithOthersForPagination(Pageable pageable, @Param("id") Long applicationStatusId, @Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

    //fp - maker - all other proposals - count
    @Query(value = "select lm.proposal_id from application_proposal_mapping lm inner join proposal_details pd on pd.id=lm.proposal_id where pd.branch_id=:branchId and lm.status >=:id and lm.fp_maker_id!=:npUserId and pd.is_active=true and lm.is_active = true ",nativeQuery = true)
    public List<BigInteger> getFPProposalsWithOthersCount(@Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

    @Modifying
    @Query("update ApplicationProposalMapping apm set apm.isFinalMcqFilled =:isFinalMcqFilled,apm.modifiedDate = NOW(),apm.modifiedBy =:userId where apm.proposalId =:proposalId and apm.userId =:userId and apm.isActive = true")
    public int setIsFinalMcqMandatoryFilled(@Param("proposalId") Long proposalId, @Param("userId") Long userId,
                                            @Param("isFinalMcqFilled") Boolean isFinalMcqFilled);

}
