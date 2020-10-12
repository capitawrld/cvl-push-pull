package com.opl.service.loans.repository.fundseeker.corporate;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.model.LoanApplicationDetailsForSp;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;

public interface LoanApplicationRepository extends JpaRepository<LoanApplicationMaster, Long> {

	
	public LoanApplicationMaster findByIdAndIsActive(Long applicationId,boolean isActive);
	
	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isActive = false,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int inActive(@Param("id") Long id, @Param("userId") Long userId);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isApplicantDetailsFilled =:isApplicantDetailsFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsApplicantProfileMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isApplicantDetailsFilled") Boolean isApplicantDetailsFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isApplicantPrimaryFilled =:isApplicantPrimaryFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsApplicantPrimaryMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isApplicantPrimaryFilled") Boolean isApplicantPrimaryFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isApplicantFinalFilled =:isApplicantFinalFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsApplicantFinalMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isApplicantFinalFilled") Boolean isApplicantFinalFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isCoApp1DetailsFilled =:isCoApp1DetailsFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsCoAppOneProfileMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isCoApp1DetailsFilled") Boolean isCoApp1DetailsFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isCoApp1FinalFilled =:isCoApp1FinalFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsCoAppOneFinalMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isCoApp1FinalFilled") Boolean isCoApp1FinalFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isCoApp2DetailsFilled =:isCoApp2DetailsFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsCoAppTwoProfileMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isCoApp2DetailsFilled") Boolean isCoApp2DetailsFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isCoApp2FinalFilled =:isCoApp2FinalFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsCoAppTwoFinalMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isCoApp2FinalFilled") Boolean isCoApp2FinalFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isGuarantor1DetailsFilled =:isGuarantor1DetailsFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsGuarantorOneProfileMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isGuarantor1DetailsFilled") Boolean isGuarantor1DetailsFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isGuarantor1FinalFilled =:isGuarantor1FinalFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsGuarantorOneFinalMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isGuarantor1FinalFilled") Boolean isGuarantor1FinalFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isGuarantor2DetailsFilled =:isGuarantor2DetailsFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsGuarantorTwoProfileMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isGuarantor2DetailsFilled") Boolean isGuarantor2DetailsFilled);
	
	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isGuarantor2FinalFilled =:isGuarantor2FinalFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsGuarantorTwoFinalMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isGuarantor2FinalFilled") Boolean isGuarantor2FinalFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isPrimaryUploadFilled =:isPrimaryUploadFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsPrimaryUploadMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isPrimaryUploadFilled") Boolean isPrimaryUploadFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isFinalDprUploadFilled =:isFinalDprUploadFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsFinalDprMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isFinalDprUploadFilled") Boolean isFinalDprUploadFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isFinalUploadFilled =:isFinalUploadFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsFinalUploadMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isFinalUploadFilled") Boolean isFinalUploadFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isFinalMcqFilled =:isFinalMcqFilled,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsFinalMcqMandatoryFilled(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isFinalMcqFilled") Boolean isFinalMcqFilled);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isMcqSkipped =:isMcqSkipped,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setIsMcqSkipped(@Param("id") Long id, @Param("userId") Long userId,
			@Param("isMcqSkipped") Boolean isMcqSkipped);

	@Query("from LoanApplicationMaster lm where lm.userId =:userId and lm.isActive = true order by lm.id desc")
	public List<LoanApplicationMaster> getUserLoans(@Param("userId") Long userId);

	@Query("from LoanApplicationMaster lm where lm.id =:id and lm.userId =:userId and lm.isActive = true order by lm.id")
	public LoanApplicationMaster getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
	
	@Query("from LoanApplicationMaster lm where lm.id =:id and lm.userId =:userId order by lm.id")
	public LoanApplicationMaster getByIdAndUserIdForInEligibleCam(@Param("id") Long id, @Param("userId") Long userId);

	@Query("from LoanApplicationMaster lm where lm.id =:id and lm.isActive = true order by lm.id")
	public LoanApplicationMaster getById(@Param("id") Long id);
	
	@Query("select lm.createdDate from LoanApplicationMaster lm where lm.id =:id and lm.isActive = true")
	public Date getCreatedDateById(@Param("id") Long id);

	@Query("select new com.opl.mudra.api.loans.model.LoanApplicationDetailsForSp(lm.id,lm.productId,lm.amount,lm.currencyId,lm.denominationId)  from LoanApplicationMaster lm where lm.userId=:userId and lm.isActive = true")
	public List<LoanApplicationDetailsForSp> getListByUserId(@Param("userId") Long userId);

	@Query("select lm.productId from LoanApplicationMaster lm where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public Integer getProductIdByApplicationId(@Param("id") Long applicationId, @Param("userId") Long userId);
	
	@Query("select lm.productId from LoanApplicationMaster lm where lm.id =:id and lm.isActive = true")
	public Integer getProductIdByApplicationId(@Param("id") Long applicationId);
	
	@Query("select lm.productId from LoanApplicationMaster lm where lm.id =:id and lm.userId =:userId")
	public Integer getProductIdByApplicationIdForSP(@Param("id") Long applicationId, @Param("userId") Long userId);

	@Query("select lm.userId,lm.name from LoanApplicationMaster lm where lm.id =:applicationId")
	public List<Object[]> getUserDetailsByApplicationId(@Param("applicationId") Long applicationId);
	
	@Query("select count(applicationId) from LoanApplicationMaster lm where lm.id =:id and lm.isActive = true")
	public Long checkApplicationIdActive(@Param("id") Long id);

	@Query("select count(id) from LoanApplicationMaster lm where lm.id =:id and lm.isPrimaryLocked=1 and lm.isActive = true")
	public Long checkPrimaryDetailIsLocked(@Param("id") Long applicationId);

	@Query("select count(id) from LoanApplicationMaster lm where lm.id =:id and lm.isFinalLocked=1 and lm.isActive = true")
	public Long checkFinalDetailIsLocked(@Param("id") Long applicationId);

	@Query("select count(id) from LoanApplicationMaster lm where lm.id =:id and lm.isMcqSkipped=1 and lm.isActive = true")
	public Long checkMcqSkipped(@Param("id") Long applicationId);

	@Query("select count(id) from LoanApplicationMaster lm where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public Long isSelfApplicantView(@Param("id") Long applicationId, @Param("userId") Long userId);

	@Query("select lm.currencyId from LoanApplicationMaster lm where lm.id =:applicationId and lm.userId =:userId and lm.isActive = true")
	public Integer getCurrencyId(@Param("applicationId") Long applicationId, @Param("userId") Long userId);
	
	@Query("select lm.currencyId from LoanApplicationMaster lm where lm.id =:applicationId and lm.isActive = true")
	public Integer getCurrencyId(@Param("applicationId") Long applicationId);
	
	@Query("select lm.denominationId from LoanApplicationMaster lm where lm.id =:applicationId and lm.userId =:userId and lm.isActive = true")
	public Integer getDenominationId(@Param("applicationId") Long applicationId, @Param("userId") Long userId);
	
	@Query("select lm.denominationId from LoanApplicationMaster lm where lm.id =:applicationId and lm.isActive = true")
	public Integer getDenominationId(@Param("applicationId") Long applicationId);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.detailsFilledCount =:detailsFilledCount,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setProfileFilledCount(@Param("id") Long id, @Param("userId") Long userId,
			@Param("detailsFilledCount") String detailsFilledCount);
	
	@Modifying
	@Query("update LoanApplicationMaster lm set lm.primaryFilledCount =:primaryFilledCount,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setPrimaryFilledCount(@Param("id") Long id, @Param("userId") Long userId,
			@Param("primaryFilledCount") String primaryFilledCount);
	
	@Modifying
	@Query("update LoanApplicationMaster lm set lm.finalFilledCount =:finalFilledCount,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setFinalFilledCount(@Param("id") Long id, @Param("userId") Long userId,
			@Param("finalFilledCount") String finalFilledCount);
	
	@Query("select lm from LoanApplicationMaster lm where lm.userId IN (:userIds) and lm.isActive = true and (lm.createdDate BETWEEN :fromDate and :toDate)")
	public List<LoanApplicationMaster> getLoanDetailsForAdminPanel(@Param("userIds") List<Long> userIds,@Param("fromDate") Date fromDate,@Param("toDate") Date toDate);

	@Query("select lm from LoanApplicationMaster lm where lm.userId IN (:userIds) and lm.id IN(:appIds) and lm.isActive = true and (lm.createdDate BETWEEN :fromDate and :toDate)")
	public List<LoanApplicationMaster> getLoanDetailsForAdminPanelUbi(@Param("userIds") List<Long> userIds,@Param("appIds") List<Long> appIds,@Param("fromDate") Date fromDate,@Param("toDate") Date toDate);
	
	@Query("select lm from LoanApplicationMaster lm where lm.id =:id and lm.userId =:userId and lm.isActive = true order by lm.id")
	public LoanApplicationMaster getMCACompanyIdByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
	
	@Query("select lm from LoanApplicationMaster lm where lm.id =:id and lm.isActive = true order by lm.id")
	public LoanApplicationMaster getMCACompanyIdById(@Param("id") Long id);
	
	@Query("select lm from LoanApplicationMaster lm where lm.id =:id and lm.isActive = true order by lm.id")
	public LoanApplicationMaster getMcaCin(@Param("id") Long id);

	@Query("select count(lm.id) from LoanApplicationMaster lm where lm.userId =:userId and lm.isActive = true")
	public Long getTotalUserApplication(@Param("userId") Long userId);
	
	@Query("select lm.userId from LoanApplicationMaster lm where lm.id =:applicationId")
	public Long getUserIdByApplicationId(@Param("applicationId") Long applicationId);
	
//	 and lm.isActive = true
	@Query("select count(*) from LoanApplicationMaster lm where lm.userId =:userId and lm.campaignCode =:campaignCode")
	public Long getApplicantCountByCode(@Param("userId") Long userId,@Param("campaignCode") String campaignCode);
	
	@Query("select lm.campaignCode from LoanApplicationMaster lm where lm.id =:applicationId")
	public String getCampaignCodeByApplicationId(@Param("applicationId") Long applicationId);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.eligibleAmnt =:eligibleAmnt,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.userId =:userId and lm.isActive = true")
	public int setEligibleAmount(@Param("id") Long id, @Param("userId") Long userId,
			@Param("eligibleAmnt") Double amount);
	
	//nhbs
	@Query("select lm from LoanApplicationMaster lm where lm.applicationStatusMaster.id =:id and lm.typeOfPayment<>null and lm.npOrgId=null and lm.paymentStatus=:paymentStatus and lm.isActive = true order by lm.modifiedDate desc")
	public List<LoanApplicationMaster> getProposalsByApplicationStatus(@Param("id") Long applicationStatusId,@Param("paymentStatus")String paymentStatus);

	//nhbs-pagination
	@Query("select lm from LoanApplicationMaster lm where lm.applicationStatusMaster.id =:id and lm.typeOfPayment<>null and lm.npOrgId=null and lm.paymentStatus=:paymentStatus and lm.isActive = true order by lm.modifiedDate desc")
	public List<LoanApplicationMaster> getProposalsByApplicationStatusForPagination(Pageable pageable,@Param("id") Long applicationStatusId,@Param("paymentStatus")String paymentStatus);
	
	//to get count of proposal based on application status
	@Query("select count(*) from LoanApplicationMaster lm where lm.applicationStatusMaster.id =:id and lm.typeOfPayment<>null and lm.npOrgId=null and lm.isActive = true ")
	public int getCountOfProposalsByApplicationStatus(@Param("id") Long applicationStatusId);

	//nhbs
	@Query("select lm from LoanApplicationMaster lm where lm.applicationStatusMaster.id =:id and lm.typeOfPayment<>null and lm.npOrgId=:npOrgId and lm.paymentStatus=:paymentStatus and lm.isActive = true order by lm.modifiedDate desc")
	public List<LoanApplicationMaster> getProposalsByApplicationStatusAndNpOrgId(@Param("id") Long applicationStatusId,@Param("npOrgId")Long npOrgId,@Param("paymentStatus")String paymentStatus);

	//nhbs-pagination
	@Query("select lm from LoanApplicationMaster lm where lm.applicationStatusMaster.id =:id and lm.typeOfPayment<>null and lm.npOrgId=:npOrgId and lm.paymentStatus=:paymentStatus and lm.isActive = true order by lm.modifiedDate desc")
	public List<LoanApplicationMaster> getProposalsByApplicationStatusAndNpOrgIdForPagination(Pageable pageable, @Param("id") Long applicationStatusId, @Param("npOrgId")Long npOrgId, @Param("paymentStatus")String paymentStatus);

    //to get count of proposal based on application status and npOrgId
    @Query("select count(*) from LoanApplicationMaster lm where lm.applicationStatusMaster.id =:id and lm.typeOfPayment<>null and lm.npOrgId=:npOrgId and lm.isActive = true ")
    public int getCountOfProposalsByApplicationStatusAndNpOrgId(@Param("id") Long applicationStatusId,@Param("npOrgId")Long npOrgId);

	//nhbs	
	//@Query("select lm from LoanApplicationMaster lm where lm.applicationStatusMaster.id >=:id and lm.npAssigneeId=:assigneeId and  lm.isActive = true ")
	//public List<LoanApplicationMaster> getAssignedProposalsByAssigneeId(@Param("id") Long applicationStatusId,@Param("assigneeId") Long assigneeId);

	//nhbs-pagination
	@Query("select lm from LoanApplicationMaster lm where lm.applicationStatusMaster.id >=:id and lm.npAssigneeId=:assigneeId and  lm.isActive = true order by lm.modifiedDate desc")
	public List<LoanApplicationMaster> getAssignedProposalsByAssigneeIdForPagination(Pageable pageable,@Param("id") Long applicationStatusId,@Param("assigneeId") Long assigneeId);

	//to get count of assigned proposals based on assignee id
	@Query("select count(*) from LoanApplicationMaster lm where lm.applicationStatusMaster.id >=:id and lm.npAssigneeId=:assigneeId and  lm.isActive = true ")
	public int getCountOfAssignedProposalsByAssigneeId(@Param("id") Long applicationStatusId,@Param("assigneeId") Long assigneeId);
	
	//nhbs	
	//@Query("select lm from LoanApplicationMaster lm where lm.npUserId=:npUserId and  lm.isActive = true ")
	//public List<LoanApplicationMaster> getAssignedProposalsByNpUserId(@Param("npUserId") Long npUserId);

	//nhbs-pagination
	@Query("select lm from LoanApplicationMaster lm where lm.npUserId=:npUserId and  lm.isActive = true ")
	public List<LoanApplicationMaster> getAssignedProposalsByNpUserIdForPagination(Pageable pageable,@Param("npUserId") Long npUserId);
	
	//to get count of proposal based on NpUserId
	@Query("select count(*) from LoanApplicationMaster lm where lm.npUserId=:npUserId and  lm.isActive = true ")
	public int getCountOfAssignedProposalsByNpUserId(@Param("npUserId") Long npUserId);
	
	//nhbs
	//@Query("select lm from LoanApplicationMaster lm where lm.ddrStatusId =:id and lm.isActive = true ")
	//public List<LoanApplicationMaster> getProposalsByDdrStatus(@Param("id") Long ddrStatusId);

	//nhbs-pagination
	@Query("select lm from LoanApplicationMaster lm where lm.ddrStatusId =:id and lm.isActive = true ")
	public List<LoanApplicationMaster> getProposalsByDdrStatusForPagination(Pageable pageable,@Param("id") Long ddrStatusId);

	//nhbs
	@Query("select count(*) from LoanApplicationMaster lm where lm.ddrStatusId =:id and lm.isActive = true ")
	public int getCountOfProposalsByDdrStatus(@Param("id") Long ddrStatusId);

    //nhbs
    //@Query("select lm from LoanApplicationMaster lm where lm.ddrStatusId =:id and lm.npOrgId=:npOrgId and lm.isActive = true ")
    //public List<LoanApplicationMaster> getProposalsByDdrStatusAndNpOrgId(@Param("id") Long ddrStatusId,@Param("npOrgId")Long npOrgId);

	//nhbs-pagination
	@Query("select lm from LoanApplicationMaster lm where lm.ddrStatusId =:id and lm.npOrgId=:npOrgId and lm.isActive = true ")
	public List<LoanApplicationMaster> getProposalsByDdrStatusAndNpOrgIdForPagination(Pageable pageable,@Param("id") Long ddrStatusId,@Param("npOrgId")Long npOrgId);

    //nhbs
    @Query("select count(*) from LoanApplicationMaster lm where lm.ddrStatusId =:id and npOrgId=:npOrgId and lm.isActive = true ")
    public int getCountOfProposalsByDdrStatusAndNpOrgId(@Param("id") Long ddrStatusId,@Param("npOrgId")Long npOrgId);
    
	@Query("select lm from LoanApplicationMaster lm where lm.userId =:userId and lm.isActive = true and lm.productId IS NULL and lm.businessTypeId =:businessTypeId")
	public LoanApplicationMaster getCorporateLoan(@Param("userId") Long userId,@Param("businessTypeId") Integer businessTypeId);
	
	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isActive  = false where lm.userId =:userId and lm.isActive = true and lm.productId IS NULL")
	public int inActiveCorporateLoan(@Param("userId") Long userId);

	@Modifying
	@Query("update LoanApplicationMaster lm set lm.isActive = false,lm.modifiedDate = NOW(),lm.modifiedBy =:userId where lm.id =:id and lm.isActive = true")
	public int inActiveApplication(@Param("id") Long id, @Param("userId") Long userId);
	
	@Query("select tenure from LoanApplicationMaster where id =:id")
	public Double getTenure(@Param("id") Long applicationId);

	//fp-maker - new proposal - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and lm.status =:id and lm.np_org_id=:npOrgId and (lm.payment_status=:paymentStatus or lm.payment_status='ByPass') and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
	public List<BigInteger> getFPProposalsByApplicationStatusAndNpOrgIdForPagination(Pageable pageable, @Param("id") Long applicationStatusId, @Param("npOrgId")Long npOrgId, @Param("paymentStatus")String paymentStatus, @Param("branchId") Long branchId);

	//fp-maker - new proposal - count
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and lm.status =:id and lm.np_org_id=:npOrgId and (lm.payment_status=:paymentStatus or lm.payment_status='ByPass') and pd.is_active=true and lm.is_active = true",nativeQuery = true)
	public List<BigInteger> getFPMakerNewProposalCount(@Param("id") Long applicationStatusId, @Param("npOrgId")Long npOrgId, @Param("paymentStatus")String paymentStatus, @Param("branchId") Long branchId);

	//fp-maker-assigned to checker - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and (lm.status >=:id or lm.status=5) and lm.fp_maker_id=:npUserId and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
	public List<BigInteger> getFPAssignedProposalsByNPUserIdForPagination(Pageable pageable, @Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

	//fp - maker-assigned to checker - count
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and (lm.status >=:id or lm.status=5) and lm.fp_maker_id=:npUserId and pd.is_active=true and lm.is_active = true ",nativeQuery = true)
	public List<BigInteger> getFPMakerAssignedAndAssginedToCheckerCount(@Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

	//fp-maker-pending tab - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and (lm.status =:id or lm.status =:revertedId or lm.status =:submitId) and lm.fp_maker_id=:npUserId and pd.is_active=true and  lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
	public List<BigInteger> getFPAssignedTabPropsByNPUserIdForPagination(Pageable pageable,@Param("id") Long applicationStatusAssignId, @Param("revertedId") Long applicationRevertedStatusId, @Param("submitId") Long applicationSubmitStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

	//fp-maker-pending tab - count
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and (lm.status =:id or lm.status =:revertedId or lm.status =:submitId) and lm.fp_maker_id=:npUserId and pd.is_active=true and lm.is_active = true",nativeQuery = true)
	public List<BigInteger> getFPAssignedTabPropsByNPUserIdCount(@Param("id") Long applicationStatusAssignId, @Param("revertedId") Long applicationRevertedStatusId, @Param("submitId") Long applicationSubmitStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

	@Query(value = "select lg.modified_date from connect.connect_log lg where lg.application_id=:applicationId AND lg.stage_id=:stage AND lg.status=:status ORDER BY lg.id desc LIMIT 1", nativeQuery = true)
	Date getInEligibleModifiedDate(@Param("applicationId")  Long applicationId,@Param("stage")  Integer stage,@Param("status")  Integer status);
	
	//fp - maker - all other proposals - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and lm.status >=:id and lm.fp_maker_id!=:npUserId and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
	public List<BigInteger> getFPProposalsWithOthersForPagination(Pageable pageable, @Param("id") Long applicationStatusId, @Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

	//fp - maker - all other proposals - count
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and lm.status >=:id and lm.fp_maker_id!=:npUserId and pd.is_active=true and lm.is_active = true ",nativeQuery = true)
	public List<BigInteger> getFPProposalsWithOthersCount(@Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId);

	//fp - checker - for approved or submitted - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and pd.fp_product_id=:fpProductId and lm.ddr_status_id =:id and lm.np_user_id=:npUserId and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
	public List<BigInteger> getFPAssignedToCheckerProposalsByNPUserIdPagination(Pageable pageable, @Param("id") Long ddrStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId,@Param("fpProductId") Long fpProductId);

	//fp - MFI - checker - for approved or submitted - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm where lm.np_org_id=:npOrgId and lm.product_id=:productId and lm.status =:id and lm.np_org_id=:npOrgId and lm.business_type_id=:businessTypeId and lm.is_active = true order by lm.application_id desc \n#pageable\n",nativeQuery = true)
	public List<BigInteger> getFPAssignedToCheckerProposalsByNPUserOrgIdPagination( @Param("id") Long ddrStatusId,@Param("npOrgId") Long npOrgId, @Param("productId") Long productId,@Param("businessTypeId") Long businessTypeId,Pageable pageable);

	//fp - MFI - checker - for approved or submitted - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm where lm.product_id=:productId and lm.status =:id and lm.business_type_id=:businessTypeId and lm.is_active = true order by lm.application_id desc \n#pageable\n",nativeQuery = true)
	public List<BigInteger> getFPAssignedToCheckerProposalsByNPUserOrgIdPaginationForSidbi( @Param("id") Long ddrStatusId,@Param("productId") Long productId,@Param("businessTypeId") Long businessTypeId,Pageable pageable);

	//fp - MFI - sidbi - for approved or submitted - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm where lm.product_id=:productId and lm.status =:status and lm.business_type_id=:businessTypeId and lm.is_active = true order by lm.modified_date desc",nativeQuery = true)
	public List<BigInteger> getFPAllProposalsByStatusPagination( @Param("status") Long status, @Param("productId") Long productId,@Param("businessTypeId") Long businessTypeId);

	//fp - checker - for approved or submitted - count
	@Query(value = "SELECT lm.application_id FROM fs_loan_application_master lm WHERE np_org_id =:npOrgId AND lm.status=:id AND is_active=TRUE and lm.product_id=:fpProductId and lm.business_type_id =6",nativeQuery = true)
	public List<BigInteger> getFPAssignedToCheckerProposalsCount(@Param("id") Long ddrStatusId,
																 @Param("npOrgId") Long npOrgId,
																 @Param("fpProductId") Long fpProductId);

	//fp - checker - for approved or submitted - count
	@Query(value = "SELECT lm.application_id FROM fs_loan_application_master lm WHERE lm.status=:id AND is_active=TRUE and lm.product_id=:fpProductId and lm.business_type_id =6",nativeQuery = true)
	public List<BigInteger> getFPAssignedToCheckerProposalsCountForSidbi(@Param("id") Long ddrStatusId,
																 @Param("fpProductId") Long fpProductId);

	//fp - checker - for approved or submitted - count
	@Query(value = "SELECT lm.application_id FROM fs_loan_application_master lm WHERE lm.status=:id AND is_active=TRUE and product_id=:fpProductId",nativeQuery = true)
	public List<BigInteger> getFPAssignedToCheckerProposalsCount(@Param("id") Long ddrStatusId,
																 @Param("fpProductId") Long fpProductId);

	//fp - checker - for approved or submitted - count MFI
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.org_id=:orgId and pd.fp_product_id=:fpProductId and lm.ddr_status_id =:id and lm.np_user_id=:npUserId and pd.is_active=true and lm.is_active = true ",nativeQuery = true)
	public List<BigInteger> getFPAssignedToCheckerMFIProposalsCount(@Param("id") Long ddrStatusId,@Param("npUserId") Long npUserId, @Param("orgId") Long orgId,@Param("fpProductId") Long fpProductId);

    //fp - checker - for reverted applications - pagination
    @Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and pd.fp_product_id=:fpProductId and lm.ddr_status_id =:id and lm.np_user_id=:npUserId and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
    public List<BigInteger> getFPAssignedToCheckerReverted(Pageable pageable, @Param("id") Long ddrStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId,@Param("fpProductId") Long fpProductId);

	//fp - checker - for reverted applications - count
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and pd.fp_product_id=:fpProductId and lm.ddr_status_id =:id and lm.np_user_id=:npUserId and pd.is_active=true and lm.is_active = true ",nativeQuery = true)
	public List<BigInteger> getFPAssignedToCheckerRevertedCount(@Param("id") Long ddrStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId,@Param("fpProductId") Long fpProductId);

	//fp - checker - for other applications - pagination
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and pd.fp_product_id=:fpProductId and lm.status >=:id and (lm.np_assignee_id IS NULL or lm.np_assignee_id!=:npUserId) and (lm.np_user_id IS NULL or lm.np_user_id!=:npUserId) and pd.is_active=true and lm.is_active = true order by lm.modified_date desc \n#pageable\n",nativeQuery = true)
	public List<BigInteger> getFPCheckerProposalsWithOthersForPagination(Pageable pageable, @Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId,@Param("fpProductId") Long fpProductId);

	//fp - checker - for other applications - count
	@Query(value = "select lm.application_id from fs_loan_application_master lm inner join proposal_details pd on pd.application_id=lm.application_id where pd.branch_id=:branchId and pd.fp_product_id=:fpProductId and lm.status >=:id and (lm.np_assignee_id IS NULL or lm.np_assignee_id!=:npUserId) and (lm.np_user_id IS NULL or lm.np_user_id!=:npUserId) and pd.is_active=true and lm.is_active = true ",nativeQuery = true)
	public List<BigInteger> getFPCheckerProposalsWithOthersCount(@Param("id") Long applicationStatusId,@Param("npUserId") Long npUserId, @Param("branchId") Long branchId,@Param("fpProductId") Long fpProductId);

	// get list of matched fpProduct based on application id
	@Query(value = "SELECT  score_model_id  FROM  fp_product_master WHERE  fp_product_id IN (select fp_product_id from application_product_audit where application_id=:applicationId and stage_id=:stageId and is_active = true) ",nativeQuery = true)
	public List<BigInteger> getScoringIdListByApplicationIdAndStageId(@Param("applicationId") Long applicationId,@Param("stageId") Long stageId);

	// get list of matched fpProduct based on application id on one form
	@Query(value = "SELECT score_model_id FROM fp_product_master WHERE is_active=1 AND is_parameter_filled=1 AND (product_id =1  or product_id =2 or product_id =16 ) AND (business_type_id IS NULL OR business_type_id=1)",nativeQuery = true)
	public List<BigInteger> getScoringIdListByApplicationIdOnOneForm();
	
	//fwt busynessTypeId by applicationId
	@Query("select lm.businessTypeId from LoanApplicationMaster lm where lm.id =:applicationId and lm.isActive = true ")
	public Integer findOneBusinessTypeIdByIdAndIsActive(@Param("applicationId")  Long applicationId); 

	/*For cam report In-principleDate*/
	@Query(value = "select lg.modified_date from connect.connect_log lg where lg.application_id=:applicationId AND lg.stage_id=:stage ORDER BY lg.id desc LIMIT 1", nativeQuery = true)
	Date getModifiedDate(@Param("applicationId")  Long applicationId,@Param("stage")  Integer stage);
	
	
	/*For cam report In-principleDate for hl pl*/
	@Query(value = "select lg.In_principle_date from connect_mudra.connect_log lg where lg.application_id=:applicationId ORDER BY lg.id desc LIMIT 1", nativeQuery = true)
	Date getInPrincipleDate(@Param("applicationId")  Long applicationId);
	
	@Modifying
   	@Query("update LoanApplicationMaster lm set lm.isPrimaryLocked =:isPrimaryLocked where lm.id =:applicationId and lm.userId =:userId and lm.isActive = true")
   	public int setIsPrimaryLocked(@Param("applicationId") Long applicationId, @Param("userId") Long userId,
   			@Param("isPrimaryLocked") Boolean isPrimaryLocked);

	/*For select on on Loan Type*/
	@Modifying
	@Query(value = "UPDATE connect_mudra.connect_log SET loan_type_id =:loanType, is_coapp_page = false where application_id=:applicationId", nativeQuery = true)
	public int updateLoanType(@Param("applicationId")  Long applicationId,@Param("loanType") Long loanType);

	@Modifying
	@Query(value = "UPDATE fs_loan_application_master lm SET lm.status=:status WHERE lm.application_id=:applicationId",nativeQuery = true)
	public int updateStatus(@Param("applicationId")  Long applicationId,@Param("status") Long status);
	
	@Query(value = "SELECT COUNT(id) FROM `fs_corporate_current_financial_arrangements_details` WHERE (loan_type = 'Overdraft' OR loan_type = 'Cash credit') AND is_active = TRUE AND application_id =:applicationId LIMIT 1", nativeQuery = true)
	public Integer checkAppliedForExisitingLoan(@Param("applicationId") Long applicationId);
	
	@Query(value = "SELECT wc_renewal_status FROM connect_mudra.connect_log WHERE application_id =:applicationId LIMIT 1", nativeQuery = true)
	public Integer checkLoanTypeByApplicationId(@Param("applicationId") Long applicationId);
	
	@Query(value = "SELECT organisation_name FROM users.user_organisation_master WHERE user_org_id =:userOrganisationId LIMIT 1", nativeQuery = true)
	public String getOrganisationNameByOrgId(@Param("userOrganisationId") Long userOrganisationId);
	
	@Modifying
	@Query(value = "UPDATE connect_mudra.connect_log cl SET cl.wc_renewal_status=:wsRenwalStatus WHERE cl.application_id=:applicationId",nativeQuery = true)
	public int updateWcRenewalStatusByApplicationId(@Param("wsRenwalStatus") Integer wsRenwalStatus, @Param("applicationId") Long applicationId);
	
	@Modifying
	@Query(value = "UPDATE fs_loan_application_master cl SET cl.wc_renewal_status=:wsRenwalStatus WHERE cl.application_id=:applicationId",nativeQuery = true)
	public int updateWcRenewalStatusOfLoanApplicationByApplicationId(@Param("wsRenwalStatus") Integer wsRenwalStatus, @Param("applicationId") Long applicationId);
	
	
	@Transactional
	@Modifying
	@Query("UPDATE LoanApplicationMaster lam SET lam.profileMappingId =:profileMappingId , lam.modifiedDate = now() WHERE lam.id =:applicationId AND lam.isActive =:isActive ")
	public Integer updateProfileMappingId(@Param("profileMappingId") Long profileMappingId ,  @Param("applicationId") Long applicationId , @Param("isActive") Boolean isActive) ;
	
	@Query("select lm.profileMappingId from LoanApplicationMaster lm where lm.id =:toApplicationId and lm.isActive = true")
	public Long getProfileMappingId(@Param("toApplicationId") Long toApplicationId);
	
	@Modifying
	@Query("update LoanApplicationMaster lm set lm.applicationCode =:appCode where lm.id =:id")
	public int updateApplicationCode(@Param("id") Long id, @Param("appCode") String appCode);
	
	// NEW PAYMENT
	@Query(value = "SELECT lam.loanCampaignCode FROM LoanApplicationMaster lam WHERE lam.id = :applicationId AND lam.isActive =:isActive")
	public String isFromBankSpecificOrMarketPalce(@Param("applicationId") Long applicationId , @Param("isActive") Boolean isActive );
	
	@Transactional
	@Modifying
	@Query("UPDATE LoanApplicationMaster lam SET lam.paymentStatus =:paymentStatus , lam.modifiedDate = now() WHERE lam.id =:applicationId AND lam.isActive =:isActive ")
	public Integer updatePaymentStatus(@Param("paymentStatus") String paymentStatus ,  @Param("applicationId") Long applicationId , @Param("isActive") Boolean isActive) ;

	@Modifying
	@Query(value = "UPDATE fs_loan_application_master lm SET lm.data_copied_for=:copyId WHERE lm.application_id=:applicationId",nativeQuery = true)
	public int updateCopyId(@Param("applicationId") Long applicationId, @Param("copyId") Long copyId);
	
}
