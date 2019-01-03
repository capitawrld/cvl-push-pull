package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.capitaworld.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.fundseeker.corporate.ProposedProductDetail;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.ProposedProductDetailRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ProposedProductDetailsRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.ProposedProductDetailsService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class ProposedProductDetailsServiceImpl implements ProposedProductDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(ProposedProductDetailsServiceImpl.class);

	@Autowired
	private ProposedProductDetailsRepository proposedProductDetailsRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private ApplicationProposalMappingRepository applicationProposalMappingRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws Exception {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				ProposedProductDetailRequest proposedProductDetailRequest = (ProposedProductDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, ProposedProductDetailRequest.class);
				ProposedProductDetail proposedProductDetail = new ProposedProductDetail();
				BeanUtils.copyProperties(proposedProductDetailRequest, proposedProductDetail);
				if (proposedProductDetailRequest.getId() == null) {
					proposedProductDetail.setCreatedBy(frameRequest.getUserId());
					proposedProductDetail.setCreatedDate(new Date());
				}
				proposedProductDetail
						.setApplicationId(loanApplicationRepository.findOne(frameRequest.getApplicationId()));
				proposedProductDetail
						.setProposalId(applicationProposalMappingRepository.findOne(frameRequest.getProposalMappingId()));
				proposedProductDetail.setModifiedBy(frameRequest.getUserId());
				proposedProductDetail.setModifiedDate(new Date());
				proposedProductDetailsRepository.save(proposedProductDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save proposedProductDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}


	@Override
	public List<ProposedProductDetailRequest> getProposedProductDetailListFromProposalId(Long proposalId,Long userId) throws Exception {
		try {
			List<ProposedProductDetail> proposedProductDetails = proposedProductDetailsRepository
					.findByProposalId(proposalId);
			List<ProposedProductDetailRequest> proposedProductDetailRequests = new ArrayList<>();

			for (ProposedProductDetail detail : proposedProductDetails) {
				ProposedProductDetailRequest proposedProductDetailRequest = new ProposedProductDetailRequest();
				BeanUtils.copyProperties(detail, proposedProductDetailRequest);
				ProposedProductDetailRequest.printFields(proposedProductDetailRequest);
				proposedProductDetailRequests.add(proposedProductDetailRequest);
			}
			return proposedProductDetailRequests;
		}

		catch (Exception e) {
			logger.info("Exception  in save proposedProductDetail  :-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<ProposedProductDetailRequest> getProposedProductDetailList(Long id,Long userId) throws Exception {
		try {
			List<ProposedProductDetail> proposedProductDetails = proposedProductDetailsRepository
					.findByApplicationIdIdAndIsActive(id,true);
			List<ProposedProductDetailRequest> proposedProductDetailRequests = new ArrayList<ProposedProductDetailRequest>();

			for (ProposedProductDetail detail : proposedProductDetails) {
				ProposedProductDetailRequest proposedProductDetailRequest = new ProposedProductDetailRequest();
				BeanUtils.copyProperties(detail, proposedProductDetailRequest);
				ProposedProductDetailRequest.printFields(proposedProductDetailRequest);
				proposedProductDetailRequests.add(proposedProductDetailRequest);
			}
			return proposedProductDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in save proposedProductDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

}
