package com.capitaworld.service.loans.repository.fundprovider;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capitaworld.service.loans.domain.fundprovider.HomeLoanModelTemp;

public interface HomeLoanModelTempRepository extends JpaRepository<HomeLoanModelTemp, Long> {
	
	public HomeLoanModelTemp findById(Long id);
	
}
