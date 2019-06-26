package com.capitaworld.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.fundseeker.corporate.OperatingStatementDetails;

public interface OperatingStatementDetailsRepository  extends JpaRepository<OperatingStatementDetails, Long>{
	
	@Modifying
	@Transactional
	@Query("update OperatingStatementDetails o set o.isActive = false where o.storageDetailsId= :sId")
	public void inActiveAssetsDetails(@Param("sId") Long storageDetailsId);

	@Modifying
	@Transactional
	@Query("update OperatingStatementDetails o set o.isActive = false where o.loanApplicationMaster.id = :applicationId and o.isActive = true")
	public void inActiveAssetsDetailsByAppId(@Param("applicationId") Long applicationId);
	
	@Modifying
	@Transactional
	@Query("update OperatingStatementDetails o set o.isActive = false where o.loanApplicationMaster.id = :applicationId and o.isActive = true and o.applicationProposalMapping.proposalId IS NULL")
	public void inActiveByAppId(@Param("applicationId") Long applicationId);
	
	@Query("from OperatingStatementDetails o where o.loanApplicationMaster.id = :appId and o.year = :yr and o.isActive = true")
	public OperatingStatementDetails getOperatingStatementDetails(@Param("appId") Long applicationId, @Param("yr") String year);

	@Query("from OperatingStatementDetails o where o.loanApplicationMaster.id =:appId and o.applicationProposalMapping.proposalId =:proposalId and o.year = :yr and o.isActive = true")
	public OperatingStatementDetails getOperatingStatementDetails(@Param("appId") Long applicationId,@Param("proposalId") Long proposalId, @Param("yr") String year);

	@Query("from OperatingStatementDetails o where o.applicationProposalMapping.proposalId = :proposalId and o.year = :yr and o.isActive = true")
	public OperatingStatementDetails getOperatingStatementDetailsByProposal(@Param("proposalId") Long proposalId, @Param("yr") String year);

	//@Query(value = "SELECT * FROM fs_corporate_cma_operating_statement_details  o WHERE o.application_id = :applicationId AND o.is_active = true ORDER BY o.year DESC LIMIT 2 4 " , nativeQuery = true)
	//@Query(value = "SELECT o FROM OperatingStatementDetails o WHERE o.loanApplicationMaster.id = :applicationId AND o.isActive = true ORDER BY o.year ASC LIMIT 3 ")
	@Query(value=" SELECT * FROM ( SELECT * FROM fs_corporate_cma_operating_statement_details o WHERE o.application_id = :applicationId  AND o.financial_yearly_statement = 'Audited'  AND o.is_active = TRUE ORDER BY o.year DESC LIMIT 3 ) AS t ORDER BY t.year " , nativeQuery = true )
	public List<OperatingStatementDetails> getByApplicationId(@Param("applicationId") Long applicationId);

	@Query("select o from OperatingStatementDetails o where o.loanApplicationMaster.id = :applicationId and o.applicationProposalMapping.proposalId =:proposalId and o.isActive = true")
	public List<OperatingStatementDetails> getByApplicationIdAndProposalId(@Param("applicationId") Long applicationId,@Param("proposalId") Long proposalId);

	@Query("select o from OperatingStatementDetails o where o.loanApplicationMaster.id = :applicationId and  o.applicationProposalMapping.proposalId = NULL and o.isActive = true")
	public List<OperatingStatementDetails> getByApplicationIdAndProposalIdNULL(@Param("applicationId") Long applicationId);

	@Query("select o from OperatingStatementDetails o where o.loanApplicationMaster.id = :applicationId and o.year = :yr and o.applicationProposalMapping.proposalId = NULL and o.isActive = true")
	public OperatingStatementDetails getByApplicationIdAndYearAndProposalIdNULL(@Param("applicationId") Long applicationId, @Param("yr") String year);

	@Query("select o from OperatingStatementDetails o where o.applicationProposalMapping.proposalId = :proposalId and o.isActive = true")
	public List<OperatingStatementDetails> getByProposalId(@Param("proposalId") Long proposalId);

	public OperatingStatementDetails findByIdAndIsActive(Long id, Boolean isActive);
	
	@Query("select o from OperatingStatementDetails o where o.loanApplicationMaster.id = :applicationId and o.isActive = true  and o.year IN :yearList and o.financialYearlyStatement =:financialYearlyStatement ORDER BY o.year ASC ")
	public List<OperatingStatementDetails> getOperatingStatementDetailsByApplicationId(@Param("applicationId") Long applicationId, @Param("yearList") List<String> yearList, @Param("financialYearlyStatement") String financialYearlyStatement);
	
	@Query("select a.domesticSales ,a.interest , a.exportSales ,a.netProfitOrLoss, a.depreciation , a.provisionForDeferredTax ,a.opProfitBeforeIntrest from OperatingStatementDetails a where a.loanApplicationMaster.id= :applicationId  AND a.year=(SELECT  max(a.year) FROM OperatingStatementDetails a WHERE a.loanApplicationMaster.id =:applicationId AND a.applicationProposalMapping.proposalId = NULL AND a.isActive=true AND a.financialYearlyStatement =:financialYearlyStatement)")
	public List<Object[]> getCMADetail(@Param("applicationId") Long applicationId, @Param("financialYearlyStatement") String financialYearlyStatement );
	
	// for DATA API RELATED 
	@Query("select a from OperatingStatementDetails a where a.loanApplicationMaster.id= :applicationId  AND a.year=(SELECT  max(a.year) FROM OperatingStatementDetails a WHERE a.loanApplicationMaster.id =:applicationId AND a.applicationProposalMapping.proposalId = NULL AND a.isActive=true AND a.financialYearlyStatement =:financialYearlyStatement)")
	public List<OperatingStatementDetails> getCMADetailForAPI(@Param("applicationId") Long applicationId, @Param("financialYearlyStatement") String financialYearlyStatement );
	
	// for DATA API RELATED  min and max year
   @Query("select a from OperatingStatementDetails a where a.loanApplicationMaster.id= :applicationId  AND a.year=(SELECT  max(a.year-1) FROM OperatingStatementDetails a WHERE a.loanApplicationMaster.id =:applicationId AND a.applicationProposalMapping.proposalId = NULL AND a.isActive=true AND a.financialYearlyStatement =:financialYearlyStatement)")
	public List<OperatingStatementDetails> getCMADetailForAPIMaxAndMinYear(@Param("applicationId") Long applicationId, @Param("financialYearlyStatement") String financialYearlyStatement );
	 // ENDS HERE 

	public OperatingStatementDetails findByLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(Long applicationId , String year , String financialYearlyStatement , Boolean isActive);

	@Modifying
	@Transactional
	@Query("update OperatingStatementDetails o set o.isActive = false where o.loanApplicationMaster.id = :applicationId and o.financialYearlyStatement IN ('Estimated', 'Projected' ) and o.isActive = true")
	public int inActiveByAppIdAndFinancialYearlyStatementAndIsActive(@Param("applicationId") Long applicationId);
	
	@Modifying
	@Transactional
	@Query("update OperatingStatementDetails o set o.isActive = false where o.loanApplicationMaster.id = :applicationId and o.financialYearlyStatement IN ('Estimated', 'Projected' ) and o.applicationProposalMapping.proposalId =:proposalId and o.isActive = true")
	public int inActiveByAppIdAndProposalIdAndFinancialYearlyStatementAndIsActive(@Param("applicationId") Long applicationId , @Param("proposalId") Long proposalId);
	
	public List<OperatingStatementDetails> findByLoanApplicationMasterIdAndYearAndIsActive(Long applicationId , String year , Boolean isActive);
}
