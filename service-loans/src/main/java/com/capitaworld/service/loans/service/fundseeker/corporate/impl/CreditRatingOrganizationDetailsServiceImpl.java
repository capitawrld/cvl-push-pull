package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CreditRatingOrganizationDetail;
import com.capitaworld.service.loans.model.CreditRatingOrganizationDetailRequest;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CreditRatingOrganizationDetailsRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.CreditRatingOrganizationDetailsService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class CreditRatingOrganizationDetailsServiceImpl implements CreditRatingOrganizationDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(CreditRatingOrganizationDetailsServiceImpl.class);

	@Autowired
	private CreditRatingOrganizationDetailsRepository creditRatingOrganizationDetailsRepository;

	@Autowired
	private OneFormClient oneFormClient;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				CreditRatingOrganizationDetailRequest creditRatingOrganizationsDetailRequest = (CreditRatingOrganizationDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, CreditRatingOrganizationDetailRequest.class);
				CreditRatingOrganizationDetail creditRatingOrganizationDetail;
				if (creditRatingOrganizationsDetailRequest.getId() != null) {
					creditRatingOrganizationDetail = creditRatingOrganizationDetailsRepository
							.findOne(creditRatingOrganizationsDetailRequest.getId());
				} else {
					creditRatingOrganizationDetail = new CreditRatingOrganizationDetail();
					creditRatingOrganizationDetail.setCreatedBy(frameRequest.getUserId());
					creditRatingOrganizationDetail.setCreatedDate(new Date());
				}
				BeanUtils.copyProperties(creditRatingOrganizationsDetailRequest, creditRatingOrganizationDetail);
				creditRatingOrganizationDetail
						.setApplicationId(new LoanApplicationMaster(frameRequest.getApplicationId()));
				creditRatingOrganizationDetail
						.setProposalId(new ApplicationProposalMapping(frameRequest.getProposalMappingId()));
				creditRatingOrganizationDetail.setModifiedBy(frameRequest.getUserId());
				creditRatingOrganizationDetail.setModifiedDate(new Date());
				creditRatingOrganizationDetailsRepository.save(creditRatingOrganizationDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save creditRatingOrganizationDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}


	@Override
	public List<CreditRatingOrganizationDetailRequest> getCreditRatingOrganizationDetailsListFromProposalId(Long proposalId, Long userId)
			throws Exception {
		try {
			List<CreditRatingOrganizationDetail> creditRatingOrganizationDetails = creditRatingOrganizationDetailsRepository
					.listCreditRatingOrganizationDetailsFromProposalId(proposalId);
			List<CreditRatingOrganizationDetailRequest> creditRatingOrganizationDetailRequests = new ArrayList<CreditRatingOrganizationDetailRequest>();

			for (CreditRatingOrganizationDetail detail : creditRatingOrganizationDetails) {
				CreditRatingOrganizationDetailRequest creditRatingOrganizationDetailsRequest = new CreditRatingOrganizationDetailRequest();
				BeanUtils.copyProperties(detail, creditRatingOrganizationDetailsRequest);
				if (!CommonUtils.isObjectNullOrEmpty(detail.getCreditRatingOptionId())) {
					OneFormResponse ratings = oneFormClient.getRatingById(detail.getCreditRatingOptionId().longValue());
					MasterResponse masterResponse = MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) ratings.getData(), MasterResponse.class);
					creditRatingOrganizationDetailsRequest.setRatingValue(masterResponse.getValue());
				}
				creditRatingOrganizationDetailRequests.add(creditRatingOrganizationDetailsRequest);
			}
			return creditRatingOrganizationDetailRequests;
		} catch (Exception e) {
			logger.error("Exception  in save creditRatingOrganizationDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<CreditRatingOrganizationDetailRequest> getcreditRatingOrganizationDetailsList(Long id, Long userId)
			throws LoansException {
		try {
			List<CreditRatingOrganizationDetail> creditRatingOrganizationDetails = creditRatingOrganizationDetailsRepository
					.listCreditRatingOrganizationDetailsFromAppId(id, userId);
			List<CreditRatingOrganizationDetailRequest> creditRatingOrganizationDetailRequests = new ArrayList<CreditRatingOrganizationDetailRequest>();

			for (CreditRatingOrganizationDetail detail : creditRatingOrganizationDetails) {
				CreditRatingOrganizationDetailRequest creditRatingOrganizationDetailsRequest = new CreditRatingOrganizationDetailRequest();
				BeanUtils.copyProperties(detail, creditRatingOrganizationDetailsRequest);
				if (!CommonUtils.isObjectNullOrEmpty(detail.getCreditRatingOptionId())) {
					OneFormResponse ratings = oneFormClient.getRatingById(detail.getCreditRatingOptionId().longValue());
					MasterResponse masterResponse = MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) ratings.getData(), MasterResponse.class);
					creditRatingOrganizationDetailsRequest.setRatingValue(masterResponse.getValue());
				}
				creditRatingOrganizationDetailRequests.add(creditRatingOrganizationDetailsRequest);
			}
			return creditRatingOrganizationDetailRequests;
		} catch (Exception e) {
			logger.error("Exception  in save creditRatingOrganizationDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	
	@Override
	public Boolean saveOrUpdateFromCibil(List<CreditRatingOrganizationDetailRequest> creditRatingList,
			Long applicationId, Long userId) {
		logger.info("Enter in saveOrUpdateFromCibil()");
		creditRatingOrganizationDetailsRepository.inActive(userId, applicationId);
		for (CreditRatingOrganizationDetailRequest req : creditRatingList) {
			CreditRatingOrganizationDetail arrangementsDetail = new CreditRatingOrganizationDetail();
			BeanUtils.copyProperties(req, arrangementsDetail);
			arrangementsDetail.setApplicationId(new LoanApplicationMaster(applicationId));
			arrangementsDetail.setCreatedBy(userId);
			arrangementsDetail.setIsActive(true);
			creditRatingOrganizationDetailsRepository.save(arrangementsDetail);
		}
		logger.info("Exit in saveOrUpdateFromCibil()");
		return true;
	}

	@Override
	public List<Integer> getShortTermCreditRatingForTeaser(Long id, Long userId) throws LoansException {
		return creditRatingOrganizationDetailsRepository.listShortCreditRatingOptionDetailsFromAppId(id, userId);
	}

	@Override
	public List<Integer> getLongTermCreditRatingForTeaser(Long id, Long userId) throws LoansException {
		return creditRatingOrganizationDetailsRepository.listLongCreditRatingOptionDetailsFromAppId(id, userId);
	}
}
