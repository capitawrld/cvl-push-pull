package com.opl.service.loans.service.fundseeker.corporate.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.FinanceMeansDetailRequest;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.FinanceMeansDetail;
import com.opl.service.loans.repository.fundseeker.corporate.FinanceMeansDetailRepository;
import com.opl.service.loans.service.fundseeker.corporate.FinanceMeansDetailsService;

@Service
@Transactional
public class FinanceMeansDetailServiceImpl implements FinanceMeansDetailsService {
	private static final Logger logger = LoggerFactory.getLogger(FinanceMeansDetailServiceImpl.class.getName());
	@Autowired
	private FinanceMeansDetailRepository financeMeansDetailRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				FinanceMeansDetailRequest financeMeansRequest = (FinanceMeansDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, FinanceMeansDetailRequest.class);
				FinanceMeansDetail financeMeansDetail = null;
				if (financeMeansRequest.getId() != null) {
					financeMeansDetail = financeMeansDetailRepository.findOne(financeMeansRequest.getId());
				} else {
					financeMeansDetail = new FinanceMeansDetail();
					financeMeansDetail.setCreatedBy(frameRequest.getUserId());
					financeMeansDetail.setCreatedDate(new Date());
				}

				BeanUtils.copyProperties(financeMeansRequest, financeMeansDetail);
				financeMeansDetail.setApplicationId(new LoanApplicationMaster(frameRequest.getApplicationId()));
				financeMeansDetail.setProposalId(new ApplicationProposalMapping(frameRequest.getProposalMappingId()));
				financeMeansDetail.setModifiedBy(frameRequest.getUserId());
				financeMeansDetail.setModifiedDate(new Date());
				financeMeansDetailRepository.save(financeMeansDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception in save totalCostOfProject :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public List<FinanceMeansDetailRequest> getMeansOfFinanceListByProposalId(Long proposalId, Long userId) throws Exception {
		try {
			List<FinanceMeansDetail> financeMeansDetails = financeMeansDetailRepository
					.listFinanceMeansFromProposalId(proposalId);
			List<FinanceMeansDetailRequest> financeMeansRequests = new ArrayList<FinanceMeansDetailRequest>(
					financeMeansDetails.size());

			for (FinanceMeansDetail detail : financeMeansDetails) {
				FinanceMeansDetailRequest financeMeansDetailRequest = new FinanceMeansDetailRequest();
				BeanUtils.copyProperties(detail, financeMeansDetailRequest);
				financeMeansRequests.add(financeMeansDetailRequest);
			}
			return financeMeansRequests;
		} catch (Exception e) {
			logger.error("Exception getting financeMeansDetail  :- {}",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<FinanceMeansDetailRequest> getMeansOfFinanceList(Long applicationId, Long userId) throws LoansException {
		try {
			List<FinanceMeansDetail> financeMeansDetails = financeMeansDetailRepository
					.listFinanceMeansFromAppId(applicationId, userId);
			List<FinanceMeansDetailRequest> financeMeansRequests = new ArrayList<FinanceMeansDetailRequest>(
					financeMeansDetails.size());

			for (FinanceMeansDetail detail : financeMeansDetails) {
				FinanceMeansDetailRequest financeMeansDetailRequest = new FinanceMeansDetailRequest();
				BeanUtils.copyProperties(detail, financeMeansDetailRequest);
				financeMeansRequests.add(financeMeansDetailRequest);
			}
			return financeMeansRequests;
		} catch (Exception e) {
			logger.error("Exception getting financeMeansDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

}
