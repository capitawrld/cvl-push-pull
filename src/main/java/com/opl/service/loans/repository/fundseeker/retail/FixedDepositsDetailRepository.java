package com.opl.service.loans.repository.fundseeker.retail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.retail.FixedDepositsDetail;

/**
 * @author Sanket
 *
 */
public interface FixedDepositsDetailRepository extends JpaRepository<FixedDepositsDetail, Long> {

	@Query("select o from FixedDepositsDetail o where o.applicationId.id = :id and o.isActive = true")
	public List<FixedDepositsDetail> listFixedDepositsFromAppId(@Param("id")Long id);

	@Query("select o from FixedDepositsDetail o where o.coApplicantDetailId.id = :id and o.isActive = true")
	public List<FixedDepositsDetail> listFixedDepositsFromCoAppId(@Param("id")Long id);

	@Query("select o from FixedDepositsDetail o where o.guarantorDetailId.id = :id and o.isActive = true")
	public List<FixedDepositsDetail> listFixedDepositsFromGarrId(@Param("id")Long id);
	
	@Query("select o from FixedDepositsDetail o where o.applicationProposalMapping.proposalId = :proposalId and o.coApplicantDetailId=null and o.isActive = true")
	public List<FixedDepositsDetail> listFixedDepositsFromProposalId(@Param("proposalId") Long proposalId);

	@Query("select o from FixedDepositsDetail o where o.applicationProposalMapping.proposalId = :proposalId and o.coApplicantDetailId.id =:coAppId and o.isActive = true")
	public List<FixedDepositsDetail> listFixedDepositsFromProposalIdAndCoAppId(@Param("proposalId") Long proposalId,@Param("coAppId")Long coAppId);

}
