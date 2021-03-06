package com.opl.service.loans.repository.sanction;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.sanction.LoanSanctionDomain;

/**
 * @author Ankit
 *
 */
public interface LoanSanctionRepository extends JpaRepository<LoanSanctionDomain, Long> {
															  
	@Query("SELECT lsd FROM LoanSanctionDomain lsd where lsd.applicationId =:applicationId AND lsd.isActive = true")
	public LoanSanctionDomain  findByAppliationId(@Param("applicationId") Long applicationId);
	
	@Query("SELECT lsd FROM LoanSanctionDomain lsd where lsd.applicationId =:applicationId AND lsd.isActive = true and lsd.orgId =:orgId")
	public LoanSanctionDomain  findByAppliationIdAndOrgId(@Param("applicationId") Long applicationId , @Param("orgId") Long orgId);

	@Query("SELECT lsd FROM LoanSanctionDomain lsd where lsd.applicationId =:applicationId AND lsd.nbfcFlow=:nbfcFlow AND lsd.isActive = true")
	public LoanSanctionDomain  findByAppliationIdAndNBFCFlow(@Param("applicationId") Long applicationId,@Param("nbfcFlow") Integer nbfcFlow);

	public LoanSanctionDomain  findByApplicationIdAndNbfcFlowAndIsActive(Long applicationId,Integer nbfcFlow,Boolean isActive);

	public LoanSanctionDomain  findByBankSanctionPrimaryKeyAndIsActiveAndApplicationId(Long id , Boolean isActive,Long applicationId);
	
	@Query(value="SELECT sanction_date FROM sanction_detail WHERE application_id =:applicationId AND is_active = TRUE ORDER BY id DESC", nativeQuery = true)
	public List<Date[]> findSanctionDateByApplicationId(@Param("applicationId") Long applicationId);
	
	@Query("SELECT lsd.tenure,lsd.roi FROM LoanSanctionDomain lsd where lsd.applicationId =:applicationId AND lsd.nbfcFlow=:nbfcFlow AND lsd.isActive = true")
	public Object[] getTenureAndRoiByAppIdAndNbfcFlow(@Param("applicationId") Long applicationId,@Param("nbfcFlow") Integer nbfcFlow);
	
	
}
