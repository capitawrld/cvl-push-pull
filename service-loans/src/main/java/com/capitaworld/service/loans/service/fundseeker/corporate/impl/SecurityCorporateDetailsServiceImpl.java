package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.SecurityCorporateDetail;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.SecurityCorporateDetailRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SecurityCorporateDetailsRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.SecurityCorporateDetailsService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class SecurityCorporateDetailsServiceImpl implements SecurityCorporateDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(SecurityCorporateDetailsServiceImpl.class);

	@Autowired
	private SecurityCorporateDetailsRepository securityCorporateDetailsRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws Exception {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				SecurityCorporateDetailRequest securityCorporateDetailRequest = (SecurityCorporateDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, SecurityCorporateDetailRequest.class);
				SecurityCorporateDetail securityCorporateDetail = null;
				if (securityCorporateDetailRequest.getId() != null) {
					securityCorporateDetail = securityCorporateDetailsRepository
							.findOne(securityCorporateDetailRequest.getId());
				} else {
					securityCorporateDetail = new SecurityCorporateDetail();
					securityCorporateDetail.setCreatedBy(frameRequest.getUserId());
					securityCorporateDetail.setCreatedDate(new Date());
				}
				BeanUtils.copyProperties(securityCorporateDetailRequest, securityCorporateDetail);
				securityCorporateDetail.setApplicationId(new LoanApplicationMaster(frameRequest.getApplicationId()));
				securityCorporateDetail.setProposalId(new ApplicationProposalMapping(frameRequest.getProposalMappingId()));
				securityCorporateDetail.setModifiedBy(frameRequest.getUserId());
				securityCorporateDetail.setModifiedDate(new Date());
				securityCorporateDetailsRepository.save(securityCorporateDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save securityCorporateDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<SecurityCorporateDetailRequest> getSecurityCorporateDetailsListFromProposalId(Long proposalId,Long userId) throws Exception {
		try {
			List<SecurityCorporateDetail> securityCorporateDetails = securityCorporateDetailsRepository
					.getSecurityCorporateDetailFromProposalId(proposalId);
			List<SecurityCorporateDetailRequest> securityCorporateDetailRequests = new ArrayList<SecurityCorporateDetailRequest>();

			for (SecurityCorporateDetail detail : securityCorporateDetails) {
				SecurityCorporateDetailRequest securityCorporateDetailsRequest = new SecurityCorporateDetailRequest();
				BeanUtils.copyProperties(detail, securityCorporateDetailsRequest);
				securityCorporateDetailsRequest.setAmountString(CommonUtils.checkString(detail.getAmount()));
				SecurityCorporateDetailRequest.printFields(securityCorporateDetailsRequest);
				securityCorporateDetailRequests.add(securityCorporateDetailsRequest);
			}
			return securityCorporateDetailRequests;
		}

		catch (Exception e) {
			logger.info("Exception  in save securityCorporateDetail  :-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<SecurityCorporateDetailRequest> getsecurityCorporateDetailsList(Long id,Long userId) throws Exception {
		try {
			List<SecurityCorporateDetail> securityCorporateDetails = securityCorporateDetailsRepository
					.getSecurityCorporateDetailFromAppId(id);
			List<SecurityCorporateDetailRequest> securityCorporateDetailRequests = new ArrayList<SecurityCorporateDetailRequest>();

			for (SecurityCorporateDetail detail : securityCorporateDetails) {
				SecurityCorporateDetailRequest securityCorporateDetailsRequest = new SecurityCorporateDetailRequest();
				BeanUtils.copyProperties(detail, securityCorporateDetailsRequest);
				securityCorporateDetailsRequest.setAmountString(CommonUtils.checkString(detail.getAmount()));
				SecurityCorporateDetailRequest.printFields(securityCorporateDetailsRequest);
				securityCorporateDetailRequests.add(securityCorporateDetailsRequest);
			}
			return securityCorporateDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in save securityCorporateDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
}
