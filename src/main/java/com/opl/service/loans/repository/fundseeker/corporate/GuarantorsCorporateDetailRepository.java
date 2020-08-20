package com.opl.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.corporate.GuarantorsCorporateDetail;

/**
 * @author Sanket
 *
 */
public interface GuarantorsCorporateDetailRepository extends JpaRepository<GuarantorsCorporateDetail, Long> {

	@Query("select o from GuarantorsCorporateDetail o where o.applicationId.id =:id and o.applicationId.userId =:userId and o.isActive = true")
	public List<GuarantorsCorporateDetail> listGuarantorsCorporateFromAppId(@Param("id")Long id, @Param("userId") Long userId);


	@Query("select o from GuarantorsCorporateDetail o where o.applicationProposalMapping.proposalId =:proposalId and o.isActive = true")
	public List<GuarantorsCorporateDetail> listGuarantorsCorporateFromProposalId(@Param("proposalId")Long proposalId);
	
	@Modifying
	@Query("update GuarantorsCorporateDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId);
	
	@Query("select o from GuarantorsCorporateDetail o where o.applicationId.id =:id and o.isActive = true")
	public List<GuarantorsCorporateDetail> listGuarantorsCorporateFromAppId(@Param("id")Long id);


}
