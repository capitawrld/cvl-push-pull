package com.opl.service.loans.repository.common;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opl.service.loans.domain.TataMotorsLoanDetails;


public interface TataMotorsLoanDetailsRepository extends JpaRepository<TataMotorsLoanDetails, Long>{
	
	
	public long countByMobileNo(String mobileNo);
	
	public TataMotorsLoanDetails  findByMobileNo(String mobileNo);
	
	

}
