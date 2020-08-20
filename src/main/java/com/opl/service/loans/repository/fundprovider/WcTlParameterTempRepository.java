/**
 * 
 */
package com.opl.service.loans.repository.fundprovider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundprovider.WcTlParameterTemp;

/**
 * @author sanket
 *
 */
public interface WcTlParameterTempRepository extends JpaRepository<WcTlParameterTemp, Long> {
	
	@Query("select o from WcTlParameterTemp o where o.fpProductId.id =:fpProductId")
	public WcTlParameterTemp getWcTlParameterTempByFpProductId(@Param("fpProductId")Long fpProductId); 
	
	@Query("select o from WcTlParameterTemp o where o.fpProductMappingId =:fpProductMappingId and isCopied=false")
	public WcTlParameterTemp getWcTlParameterTempByFpProductMappingId(@Param("fpProductMappingId")Long fpProductId); 



}
