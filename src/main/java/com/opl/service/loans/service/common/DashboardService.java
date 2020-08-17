package com.opl.service.loans.service.common;

import com.capitaworld.service.users.model.UserResponse;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.DashboardProfileResponse;

public interface DashboardService {
	public DashboardProfileResponse getBasicProfileInfo(Long applicationId,Long userId,boolean isSP) throws LoansException ;
	
	public Integer getCount(int userType) throws LoansException;
	
	public UserResponse getFPBasicProfileInfo(Long userId) throws LoansException ;
}