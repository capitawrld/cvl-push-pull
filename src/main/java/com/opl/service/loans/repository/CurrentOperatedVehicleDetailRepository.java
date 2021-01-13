package com.opl.service.loans.repository;

import com.opl.service.loans.domain.CurrentOperatedVehicleDetail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by pooja.patel on 24-11-2020.
 */
public interface CurrentOperatedVehicleDetailRepository extends JpaRepository<CurrentOperatedVehicleDetail,Long> {
	
	@Modifying
	@Query("DELETE FROM CurrentOperatedVehicleDetail lm where lm.applicationId =:applicationId")
	public void deleteByApplicationId(@Param("applicationId") Long applicationId);
	
	List<CurrentOperatedVehicleDetail> findByApplicationIdAndIsActive(Long applicationId, Boolean isActive);

	@Query("select sum(vehicleCost) FROM CurrentOperatedVehicleDetail lm where lm.applicationId =:applicationId and isActive=true")
	public Double getTotalCostOfExistingVehicle(@Param("applicationId") Long applicationId);
	
}
