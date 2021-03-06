package com.opl.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.opl.service.loans.domain.fundseeker.corporate.BalanceSheetDetail;

public interface BalanceSheetDetailRepository extends JpaRepository<BalanceSheetDetail, Long>{

	

	@Modifying
	@Transactional
	@Query("update BalanceSheetDetail b set b.isActive = false where b.storageDetailsId= :sId")
	public void inActiveBalanceSheetDetail(@Param("sId") Long storageDetailsId);
	
	@Modifying
	@Transactional
	@Query("update BalanceSheetDetail b set b.isActive = false where b.applicationId.id = :applicationId and b.isActive = true")
	public void inActiveBalanceSheetDetailByAppId(@Param("applicationId") Long applicationId);
	
	@Query("from BalanceSheetDetail b where b.applicationId.id = :appId and b.year = :yr and b.isActive = true")
	public BalanceSheetDetail getBalanceSheetDetail(@Param("appId") Long applicationId, @Param("yr") String year);
	
	
	@Query("select o from BalanceSheetDetail o where o.applicationId.id = :applicationId and o.isActive = true")
	public List<BalanceSheetDetail> getByApplicationId(@Param("applicationId") Long applicationId);

	@Query("select o from BalanceSheetDetail o where o.applicationProposalMapping.proposalId = :proposalId and o.isActive = true")
	public List<BalanceSheetDetail> getByProposalId(@Param("proposalId") Long proposalId);

	@Query("select o from BalanceSheetDetail o where o.applicationId.id =:applicationId and o.applicationProposalMapping.proposalId = :proposalId and o.isActive = true")
	public List<BalanceSheetDetail> getByApplicationIdAndProposalId(@Param("applicationId") Long applicationId,@Param("proposalId") Long proposalId);

	@Query("select o from BalanceSheetDetail o where o.applicationId.id = :applicationId and o.isActive = true and o.year IN :yearList and o.financialYearlyStatement =:financialYearlyStatement ORDER BY o.year ASC" )
	public List<BalanceSheetDetail> getBalanceSheetDetailByApplicationId(@Param("applicationId") Long applicationId ,@Param("yearList") List<String> yearList, @Param("financialYearlyStatement") String financialYearlyStatement);

}
