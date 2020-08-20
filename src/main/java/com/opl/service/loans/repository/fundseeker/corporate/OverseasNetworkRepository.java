package com.opl.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.corporate.OverseasNetworkMappingDetail;

public interface OverseasNetworkRepository extends JpaRepository<OverseasNetworkMappingDetail, Long>{
	
	@Modifying
	@Query("update OverseasNetworkMappingDetail sd set sd.isActive = false where sd.applicationId =:applicationId and sd.isActive = true")
	public int inActiveMappingByApplicationId(@Param("applicationId") Long applicationId);
	
	@Query("select sd.overseasNetworkId from OverseasNetworkMappingDetail sd where sd.applicationId =:applicationId and sd.isActive = true")
	public List<Integer> getOverseasNetworkIds(@Param("applicationId") Long applicationId);

}
