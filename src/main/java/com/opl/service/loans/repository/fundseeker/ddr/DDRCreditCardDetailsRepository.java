package com.opl.service.loans.repository.fundseeker.ddr;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.ddr.DDRCreditCardDetails;

public interface DDRCreditCardDetailsRepository extends JpaRepository<DDRCreditCardDetails, Long>{

	@Query("select dd from DDRCreditCardDetails dd where dd.id =:id and dd.isActive = true")
	public DDRCreditCardDetails getByIdAndIsActive(@Param("id") Long id);
	
	@Query("select dd from DDRCreditCardDetails dd where dd.ddrFormId =:ddrFormId and dd.isActive = true")
	public List<DDRCreditCardDetails> getListByDDRFormId(@Param("ddrFormId") Long ddrFormId);
}
