package com.capitaworld.service.loans.service.fundseeker.retail;

import java.util.List;

import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.retail.ExistingLoanDetailRequest;

/**
 * @author Sanket
 *
 */
public interface ExistingLoanDetailsService {

	public Boolean saveOrUpdate(FrameRequest frameRequest) throws Exception;

	public List<ExistingLoanDetailRequest> getExistingLoanDetailList(Long id, int applicationType) throws Exception;
	
	public Boolean saveOrUpdateFromCibil(List<ExistingLoanDetailRequest> existingLoanDetail,Long applicationId,Long userId,int applicantType) throws Exception;

}
