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
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.OwnershipDetailRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.OwnershipDetail;
import com.opl.service.loans.repository.fundseeker.corporate.OwnershipDetailsRepository;
import com.opl.service.loans.service.fundseeker.corporate.OwnershipDetailsService;
import com.opl.service.loans.utils.CommonDocumentUtils;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class OwnershipDetailsServiceImpl implements OwnershipDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(ExistingProductDetailsServiceImpl.class.getName());
	@Autowired
	public OwnershipDetailsRepository ownershipDetailsRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {

		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				OwnershipDetailRequest ownershipDetailRequest = (OwnershipDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, OwnershipDetailRequest.class);
				OwnershipDetail ownershipDetail = null;
				if (ownershipDetailRequest.getId() != null) {
					ownershipDetail = ownershipDetailsRepository.findOne(ownershipDetailRequest.getId());
				} else {
					ownershipDetail = new OwnershipDetail();
					ownershipDetail.setCreatedBy(frameRequest.getUserId());
					ownershipDetail.setCreatedDate(new Date());
				}
				BeanUtils.copyProperties(ownershipDetailRequest, ownershipDetail);
				ownershipDetail.setApplicationId(new LoanApplicationMaster(frameRequest.getApplicationId()));
				ownershipDetail.setProposalMapping(new ApplicationProposalMapping(frameRequest.getProposalMappingId()));
				ownershipDetail.setModifiedBy(frameRequest.getUserId());
				ownershipDetail.setModifiedDate(new Date());
				ownershipDetail.setProposalMapping(new ApplicationProposalMapping(frameRequest.getProposalMappingId()));
				ownershipDetailsRepository.save(ownershipDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save ownershipDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<OwnershipDetailRequest> getOwnershipDetailList(Long applicationId,Long userId) throws LoansException {
		try {
			List<OwnershipDetail> ownershipDetails = ownershipDetailsRepository.listOwnershipFromAppId(applicationId,userId);
			List<OwnershipDetailRequest> ownershipDetailRequests = new ArrayList<OwnershipDetailRequest>();

			for (OwnershipDetail detail : ownershipDetails) {
				OwnershipDetailRequest ownershipDetailRequest = new OwnershipDetailRequest();
				BeanUtils.copyProperties(detail, ownershipDetailRequest);
				ownershipDetailRequests.add(ownershipDetailRequest);
			}
			return ownershipDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in get ownershipDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<OwnershipDetailRequest> getOwnershipDetailList(Long applicationId,Long userId ,Long proposalId) throws Exception {
		try {
			List<OwnershipDetail> ownershipDetails = ownershipDetailsRepository.listOwnershipFromAppIdAndProposalId(applicationId,proposalId); // new
			List<OwnershipDetailRequest> ownershipDetailRequests = new ArrayList<OwnershipDetailRequest>();

			for (OwnershipDetail detail : ownershipDetails) {
				OwnershipDetailRequest ownershipDetailRequest = new OwnershipDetailRequest();
				BeanUtils.copyProperties(detail, ownershipDetailRequest);
				ownershipDetailRequests.add(ownershipDetailRequest);
			}
			return ownershipDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in get ownershipDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	/*multiple bank*/
	@Override
	public List<OwnershipDetailRequest> getOwnershipDetailListForMultipleBank(Long proposalId) throws Exception {
		try {
			CommonDocumentUtils.startHook(logger, "getOwnershipDetailList");
			List<OwnershipDetail> achievementDetails = ownershipDetailsRepository.listOwnershipFromProposalId(proposalId);
			List<OwnershipDetailRequest> ownershipDetailRequests = new ArrayList<>(achievementDetails.size());

			for (OwnershipDetail detail : achievementDetails) {
				OwnershipDetailRequest ownershipDetailRequest = new OwnershipDetailRequest();
				BeanUtils.copyProperties(detail, ownershipDetailRequest);
				ownershipDetailRequests.add(ownershipDetailRequest);
			}

			CommonDocumentUtils.endHook(logger, "getOwnershipDetailList");
			return ownershipDetailRequests;
		} catch (Exception e) {
			logger.error("Exception getting getOwnershipDetailList  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
}
