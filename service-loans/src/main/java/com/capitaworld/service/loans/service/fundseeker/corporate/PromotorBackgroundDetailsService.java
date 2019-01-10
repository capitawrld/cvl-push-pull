package com.capitaworld.service.loans.service.fundseeker.corporate;

import java.util.List;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.PromotorBackgroundDetailRequest;

/**
 * @author Sanket
 *
 */
public interface PromotorBackgroundDetailsService {

	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException;

	List<PromotorBackgroundDetailRequest> getPromotorBackgroundDetailList(Long applicationId,Long userId) throws LoansException;

}
