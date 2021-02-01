package com.opl.service.loans.repository.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.TataMotorsLoanDetails;


public interface TataMotorsLoanDetailsRepository extends JpaRepository<TataMotorsLoanDetails, Long>{
	
	
	public long countByMobileNo(String mobileNo);
	
	public TataMotorsLoanDetails  findByMobileNo(String mobileNo);
	
	@Query(value="SELECT * FROM users.users WHERE `mobile`=:mobileNo and is_active = true",nativeQuery = true)
	public List<Object[]>  getUserByMobileNo(@Param("mobileNo") String mobileNo);
	
	

}
