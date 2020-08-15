package com.opl.service.loans.repository.fundseeker.ddr;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.ddr.DDRAuthorizedSignDetails;

public interface DDRAuthorizedSignDetailsRepository extends JpaRepository<DDRAuthorizedSignDetails,Long>{

	@Query("select dd from DDRAuthorizedSignDetails dd where dd.id =:id and dd.isActive = true")
	public DDRAuthorizedSignDetails getByIdAndIsActive(@Param("id") Long id);
	
	@Query("select dd from DDRAuthorizedSignDetails dd where dd.ddrFormId =:ddrFormId and dd.isActive = true")
	public List<DDRAuthorizedSignDetails> getListByDDRFormId(@Param("ddrFormId") Long ddrFormId);
}
