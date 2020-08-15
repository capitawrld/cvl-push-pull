package com.opl.service.loans.repository.fundprovider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundprovider.PersonalLoanParameterTemp;

public interface PersonalLoanParameterTempRepository extends JpaRepository<PersonalLoanParameterTemp, Long>{
	@Query("select o from PersonalLoanParameterTemp o where o.fpProductId.id =:fpProductId")
	public PersonalLoanParameterTemp getPlParameterTempByFpProductId(@Param("fpProductId")Long fpProductId); 
	
	@Query("select o from PersonalLoanParameterTemp o where o.fpProductMappingId =:fpProductMappingId and isCopied=false")
	public PersonalLoanParameterTemp getPlParameterTempByFpProductMappingId(@Param("fpProductMappingId")Long fpProductId);
}
