package com.opl.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.corporate.FinancialArrangementsDetail;

/**
 * @author Sanket
 *
 */
public interface FinancialArrangementDetailsRepository extends JpaRepository<FinancialArrangementsDetail, Long> {

	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id  and o.applicationId.userId =:userId and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailFromAppId(@Param("id")Long id, @Param("userId") Long userId);
	
	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailFromAppId(@Param("id")Long id);
		
	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NOT NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailFromAppIdForProprietorship(@Param("id")Long id);

	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.isManuallyAdded = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> getManuallyAddedFinancialDetail(@Param("id")Long id);

	//@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id and o.applicationProposalMapping.proposalId=:proposalId and o.isActive = true and o.directorBackgroundDetail IS NULL")
	//public List<FinancialArrangementsDetail> listSecurityCorporateDetailFromAppIdAndProposalId(@Param("id")Long id,@Param("proposalId")Long proposalId);

	@Modifying
	@Query("update FinancialArrangementsDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true and pm.directorBackgroundDetail IS NULL")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId);
	
	@Modifying
	@Query("update FinancialArrangementsDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true")
	public int inActiveAllByApplicationId(@Param("userId") Long userId,@Param("applicationId") Long applicationId);

	@Modifying
	@Query("update FinancialArrangementsDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isManuallyAdded = true and pm.isActive = true and pm.directorBackgroundDetail IS NULL")
	public int inActiveManuallyAddedLoans(@Param("userId") Long userId,@Param("applicationId") Long applicationId);

	@Modifying
	@Query("update FinancialArrangementsDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true and pm.directorBackgroundDetail =:directorId")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId,@Param("directorId") Long directorId);
	
	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and o.applicationProposalMapping IS NULL")
	public Double getTotalEmiByApplicationId(@Param("id")Long id);
	
	@Query("select sum(o.amount) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and LOWER(o.loanType) IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0 and o.applicationProposalMapping IS NULL")
	public Double getExistingLimits(@Param("id")Long id,@Param("loanType") List<String> loanType);
	
	
	@Query("select sum(o.collateralSecurityAmount) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and LOWER(o.loanType) IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0 and o.applicationProposalMapping IS NULL")
	public Double getAmountOfCollateralExistingLoan(@Param("id")Long id,@Param("loanType") List<String> loanType);
	
	@Query("select sum(o.outstandingAmount) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and LOWER(o.loanType) IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0 and o.applicationProposalMapping IS NULL" )
	public Double getOutStandingAmount(@Param("id")Long id,@Param("loanType") List<String> loanType);
	

	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and o.isManuallyAdded = true and o.applicationProposalMapping IS NULL")
	public Double getTotalEmiByApplicationIdForUniformProduct(@Param("id")Long id);

	@Query("select sum(o.amount) from FinancialArrangementsDetail o where o.applicationId.id =:id and o.isActive = true and o.directorBackgroundDetail IS NULL and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0 and o.isManuallyAdded = true and o.applicationProposalMapping IS NULL")
	public Double getExistingLimitsForUniformProduct(@Param("id")Long id);

	public FinancialArrangementsDetail findByIdAndIsActive(Long id,Boolean isActive);

	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id  and o.isActive = true and o.directorBackgroundDetail IS NULL")
	public List<FinancialArrangementsDetail> listSecurityCorporateDetailByAppId(@Param("id")Long id);
	
	@Query("select o from FinancialArrangementsDetail o where o.applicationId.id =:id  and o.isActive = true")
	public List<FinancialArrangementsDetail> listAllSecurityCorporateDetailByAppId(@Param("id")Long id);

	@Query("select o from FinancialArrangementsDetail o where o.directorBackgroundDetail =:id and o.isActive =:isActive")
	public FinancialArrangementsDetail findByDirectorIdAndIsActive(@Param("id")Long id, @Param("isActive")Boolean isActive);

//	@Query("select o from FinancialArrangementsDetail o where o.directorBackgroundDetail.id =:directorId and o.applicationId.id =:applicationId and o.isActive =:true")
//	public List<FinancialArrangementsDetail> listFinancialListForPartner(@Param("directorId")Long directorId, @Param("applicationId")Long applicationId, @Param("isActive")Boolean isActive);
	
	public List<FinancialArrangementsDetail> findByDirectorBackgroundDetailAndApplicationIdIdAndIsActive(Long dirId,Long appId,Boolean isActive);

	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:applicationId and o.directorBackgroundDetail =:directorId and o.isActive = true")
	public Double getTotalEmiByApplicationIdAndDirectorId(@Param("applicationId")Long applicationId,@Param("directorId")Long directorId);
	
	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.directorBackgroundDetail =:directorId and o.isActive = true and LOWER(o.loanType) NOT IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0 and o.applicationId.id =:applicationId")
	public Double getTotalEmiByDirectorId(@Param("directorId")Long directorId,@Param("loanType") List<String> loanType,@Param("applicationId")Long applicationId);

	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:applicationId and o.directorBackgroundDetail IS NOT NULL and o.isActive = true and LOWER(o.loanType) NOT IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0")
	public Double getTotalEmiOfAllDirByApplicationId(@Param("applicationId")Long applicationId,@Param("loanType") List<String> loanType);
	

	/*@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:applicationId and o.directorBackgroundDetail IS NULL and o.isActive = true and LOWER(o.loanType) NOT IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0")*/
	@Query(value="select SUM(GREATEST(o.emi,o.bureau_or_calculated_emi)) from fs_corporate_current_financial_arrangements_details o where o.application_id =:applicationId and o.director_id IS NULL and o.is_active = true and LOWER(o.loan_type) NOT IN (:loanType) and ((o.outstanding_amount IS NOT NULL and o.outstanding_amount > 0) or o.is_manually_added IS TRUE)",nativeQuery = true)
	public Double getTotalEmiByApplicationIdSoftPing(@Param("applicationId")Long applicationId,@Param("loanType") List<String> loanType);
	
	@Query("select sum(o.emi) from FinancialArrangementsDetail o where o.applicationId.id =:applicationId and o.directorBackgroundDetail =:coApplicantId and o.isActive = true and LOWER(o.loanType) NOT IN (:loanType) and o.outstandingAmount IS NOT NULL and o.outstandingAmount > 0")
	public Double getTotalEmiByApplicationIdSoftPing(@Param("applicationId")Long applicationId,@Param("loanType") List<String> loanType,@Param("coApplicantId") Long coApplicantId);
	
	@Query("From FinancialArrangementsDetail o where o.directorBackgroundDetail =:coAppId")
	public List<FinancialArrangementsDetail> listCoFinancialByCoAppId(@Param("coAppId")Long id);
	
	@Query(value="SELECT o.id FROM fs_corporate_current_financial_arrangements_details o WHERE o.application_id =:applicationId AND lower(o.`financial_institution_name`) =:financialInstitutionName AND o.director_id IS NULL AND o.is_active = TRUE AND (o.outstanding_amount IS NOT NULL AND o.outstanding_amount > 0)",nativeQuery = true)
	public List<Long> checkExistingLoanWithBank(@Param("applicationId") Long applicationId,@Param("financialInstitutionName") String financialInstitutionName);
	
	@Query(value="SELECT o.id FROM fs_corporate_current_financial_arrangements_details o WHERE o.application_id =:applicationId AND lower(o.`financial_institution_name`) =:financialInstitutionName AND o.director_id =:coAppId AND o.is_active = TRUE AND (o.outstanding_amount IS NOT NULL AND o.outstanding_amount > 0)",nativeQuery = true)
	public List<Long> checkExistingLoanWithBankForCoApp(@Param("applicationId") Long applicationId,@Param("financialInstitutionName") String financialInstitutionName,@Param("coAppId") Long coAppId);
	
	@Query(value="SELECT count(o.id) FROM fs_corporate_current_financial_arrangements_details o WHERE o.id in (:ids) and o.dpd_details IS NOT NULL and o.dpd_details != '[]'",nativeQuery = true)
	public Long checkDpdsWithBankByIds(@Param("ids") List<Long> ids);

	@Query(value="SELECT COUNT(*) FROM fs_corporate_current_financial_arrangements_details o WHERE o.application_id =:id AND o.is_active = TRUE AND o.director_id IS NULL",nativeQuery = true)
	public Long getExistingLoansCount(@Param("id") Long id);
	
}
