package com.capitaworld.service.loans.service.fundseeker.corporate;

import java.util.List;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.GuarantorsCorporateDetailRequest;

/**
 * @author Sanket
 *
 */
public interface GuarantorsCorporateDetailService {

	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException;

	public List<GuarantorsCorporateDetailRequest> getGuarantorsCorporateDetailList(Long id,Long userId) throws LoansException;

}
