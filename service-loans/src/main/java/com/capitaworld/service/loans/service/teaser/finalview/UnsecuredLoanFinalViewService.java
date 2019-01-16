package com.capitaworld.service.loans.service.teaser.finalview;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.teaser.finalview.UnsecuredLoanFinalViewResponse;

public interface UnsecuredLoanFinalViewService {
	 public UnsecuredLoanFinalViewResponse getUnsecuredLoanFinalViewDetails(Long toApplicationId,Integer userType,Long fundProviderUserId) throws LoansException;
}
