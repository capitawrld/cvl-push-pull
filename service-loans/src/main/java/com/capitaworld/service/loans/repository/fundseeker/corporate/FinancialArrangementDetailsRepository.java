package com.capitaworld.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitaworld.service.loans.domain.fundseeker.corporate.FinancialArrangementsDetail;

/**
 * @author Sanket
 *
 */
public interface FinancialArrangementDetailsRepository extends JpaRepository<FinancialArrangementsDetail, Long> {

	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id  and o.applicationId.userId =:userId and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailFromAppId(@Param("id")Long id, @Param("userId") Long userId);
	
	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailFromAppId(@Param("id")Long id);

	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.isManuallyAdded = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> getManuallyAddedFinancialDetail(@Param("id")Long id);

	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id and o.applicationProposalMapping.proposalId=:proposalId and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailFromAppIdAndProposalId(@Param("id")Long id,@Param("proposalId")Long proposalId);

	@Modifying
	@Query("update FinancialArrangementsDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true and pm.directorBackgroundDetail IS NULL")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId);

	@Modifying
	@Query("update FinancialArrangementsDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isManuallyAdded = true and pm.isActive = true and pm.directorBackgroundDetail IS NULL")
	public int inActiveManuallyAddedLoans(@Param("userId") Long userId,@Param("applicationId") Long applicationId);

	@Modifying
	@Query("update FinancialArrangementsDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true and pm.directorBackgroundDetail.id =:directorId")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId,@Param("directorId") Long directorId);
	
	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and o.applicationProposalMapping IS NULL")
	public Double getTotalEmiByApplicationId(@Param("id")Long id);
	
	@Query("select sum(o.amount) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and LOWER(o.loanType) IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0 and o.applicationProposalMapping IS NULL")
	public Double getExistingLimits(@Param("id")Long id,@Param("loanType") List<String> loanType);

	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and o.isManuallyAdded = true and o.applicationProposalMapping IS NULL")
	public Double getTotalEmiByApplicationIdForUniformProduct(@Param("id")Long id);

	@Query("select sum(o.amount) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0 and o.isManuallyAdded = true and o.applicationProposalMapping IS NULL")
	public Double getExistingLimitsForUniformProduct(@Param("id")Long id);

	public FinancialArrangementsDetail findByIdAndIsActive(Long id,Boolean isActive);

	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id  and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailByAppId(@Param("id")Long id);

	@Query("select o from FinancialArrangementsDetail o where o.directorBackgroundDetail.id =:id and o.isActive =:isActive")
	public FinancialArrangementsDetail findByDirectorIdAndIsActive(@Param("id")Long id, @Param("isActive")Boolean isActive);

//	@Query("select o from FinancialArrangementsDetail o where o.directorBackgroundDetail.id =:directorId and o.applicationId.id =:applicationId and o.isActive =:true")
//	public List<FinancialArrangementsDetail> listFinancialListForPartner(@Param("directorId")Long directorId, @Param("applicationId")Long applicationId, @Param("isActive")Boolean isActive);
	
	public List<FinancialArrangementsDetail> findByDirectorBackgroundDetailIdAndApplicationIdIdAndIsActive(Long dirId,Long appId,Boolean isActive);

	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:applicationId and o.directorBackgroundDetail.id =:directorId and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public Double getTotalEmiByApplicationIdAndDirectorId(@Param("applicationId")Long applicationId,@Param("directorId")Long directorId);

	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:applicationId and o.directorBackgroundDetail IS NOT NULL and o.isActive = true and LOWER(o.loanType) NOT IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0 and o.directorBackgroundDetail IS NULL")
	public Double getTotalEmiOfAllDirByApplicationId(@Param("applicationId")Long applicationId,@Param("loanType") List<String> loanType);

	@Query("select o from FinancialArrangementsDetail o where o.applicationProposalMapping.proposalId =:proposalId and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailFromProposalId(@Param("proposalId")Long proposalId);
}
