package com.opl.service.loans.repository.fundprovider;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundprovider.GeographicalCityDetailTemp;

public interface GeographicalCityTempRepository extends JpaRepository<GeographicalCityDetailTemp, Long>{
	@Modifying
	@Query("update GeographicalCityDetailTemp gc set gc.isActive = false where gc.fpProductMaster =:fpProductMaster and gc.isActive = true")
	public int inActiveMappingByFpProductId(@Param("fpProductMaster") Long fpProductMaster);
	
	@Query("select o.cityId from GeographicalCityDetailTemp o where o.fpProductMaster = :fpProductMaster and o.isActive = true")
	public List<Long> getCityByFpProductId(@Param("fpProductMaster")Long fpProductMaster);
}
