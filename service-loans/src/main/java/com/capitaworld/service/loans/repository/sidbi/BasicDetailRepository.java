package com.capitaworld.service.loans.repository.sidbi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitaworld.service.loans.domain.sidbi.SidbiBasicDetail;

public interface BasicDetailRepository extends JpaRepository<SidbiBasicDetail, Long>{

	@Query("from SidbiBasicDetail cr where cr.applicationId =:applicationId and cr.createdBy =:userId and cr.isActive=true")
	public SidbiBasicDetail getByApplicationAndUserId(@Param("userId") Long userId, @Param("applicationId") Long applicationId);
	
	
}
