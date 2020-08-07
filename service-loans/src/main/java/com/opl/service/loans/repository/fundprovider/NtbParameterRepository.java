package com.opl.service.loans.repository.fundprovider;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundprovider.NTBParameter;

public interface NtbParameterRepository extends JpaRepository<NTBParameter, Long>{
	@Query("from NTBParameter tp where tp.fpProductId.id =:id ")
	public NTBParameter getById(@Param("id") Long id);
	
	@Query("from NTBParameter tp where tp.userId =:userId and isActive=true")
	public List<NTBParameter> getByActiveUserId(@Param("userId") Long userId);
}
