package com.capitaworld.service.loans.service.fundseeker.corporate;

import java.util.List;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.ProposedProductDetailRequest;

/**
 * @author Sanket
 *
 */
public interface ProposedProductDetailsService {

	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException;

	public List<ProposedProductDetailRequest> getProposedProductDetailList(Long id,Long userId) throws LoansException;

}
