package com.opl.service.loans.repository.fundprovider;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundprovider.EmpWithMappingDetailTemp;

public interface FpEmpWithTempRepository extends JpaRepository<EmpWithMappingDetailTemp, Long>{
	@Modifying
	@Query("update EmpWithMappingDetailTemp gc set gc.isActive = false where gc.fpProductId =:fpProductId and gc.isActive = true")
	public int inActiveEmpWithByFpProductId(@Param("fpProductId") Long fpProductMaster);
	
	@Query("select o.empTypeId from EmpWithMappingDetailTemp o where o.fpProductId = :fpProductId and o.isActive = true")
	public List<Integer> getEmpWithByFpProductId(@Param("fpProductId")Long fpProductMaster);
}
