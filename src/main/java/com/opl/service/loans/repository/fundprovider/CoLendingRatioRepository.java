package com.opl.service.loans.repository.fundprovider;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.mudra.api.loans.model.corporate.CoLendingRequest;
import com.opl.service.loans.domain.fundprovider.CoLendingRatio;

public interface CoLendingRatioRepository extends JpaRepository<CoLendingRatio, Long>{
	@Modifying
	@Query("update CoLendingRatio clr set clr.isActive = false where clr.id=:id and clr.isActive = true")
	public int inActiveRatio(@Param("id") Long id);
	
	@Modifying
	@Query("update CoLendingRatio clr set clr.isProposalActive = false where clr.id=:id and clr.isActive = true")
	public int inActiveRatioAndProposal(@Param("id") Long id);
	
	@Modifying
	@Query("update CoLendingRatio clr set clr.isActive = true,clr.isProposalActive = true where clr.id=:id")
	public int activeRatioAndProposal(@Param("id") Long id);
	
	@Modifying
	@Query("update CoLendingRatio clr set clr.isProposalActive=:status where clr.jobId=:jobId and clr.isActive = true")
	public int updateActivatedRatio(@Param("jobId") Long jobId,@Param("status") Boolean status);

	@Query("from CoLendingRatio clr where clr.userOrgId =:userOrgId and isActive=true")
	public List<CoLendingRatio> listAllActiveByUserId(@Param("userOrgId")Long userId);

	@Query("from CoLendingRatio clr where clr.bankId =:bankId and isActive=true")
	public List<CoLendingRatio> listAllActiveByBankId(@Param("bankId")Long bankId);

	@Modifying
	@Query("update CoLendingRatio clr set clr.reason =:reason where clr.jobId=:jobId and clr.isActive = true")
	public int addReasonByJobId(@Param("reason")String reason,@Param("jobId")Long  jobId);

	@Query("SELECT DISTINCT new com.opl.mudra.api.loans.model.corporate.CoLendingRequest(clr.bankId) FROM CoLendingRatio clr WHERE clr.userOrgId =:userOrgId AND isActive = TRUE AND clr.isProposalActive = TRUE")
	public List<CoLendingRequest> listByOrgId(@Param("userOrgId") Long userOrgId);
	
	@Query("from CoLendingRatio clr where clr.userOrgId =:userOrgId and isActive=true and isProposalActive=true")
	public List<CoLendingRatio> listAllActiveProposalByOrgId(@Param("userOrgId")Long userOrgId);
	
	@Query("from CoLendingRatio clr where clr.bankId =:bankId and isActive=true and isProposalActive=true")
	public List<CoLendingRatio> listAllActiveProposalByBankId(@Param("bankId")Long bankId);
}
