package com.capitaworld.service.loans.repository.fundprovider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitaworld.service.loans.domain.fundprovider.LapParameter;

public interface LapParameterRepository extends JpaRepository<LapParameter, Long>{
	@Query("from LapParameter lpp where lpp.fpProductId.id =:id and isActive=true")
	public LapParameter getByID(@Param("id") Long id);
}
