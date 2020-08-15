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
import com.opl.mudra.api.loans.model.AchievementDetailRequest;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.AchievementDetail;
import com.opl.service.loans.repository.fundseeker.corporate.AchievementDetailsRepository;
import com.opl.service.loans.service.fundseeker.corporate.AchievmentDetailsService;
import com.opl.service.loans.utils.CommonDocumentUtils;

@Service
@Transactional
public class AchievementDetailServiceImpl implements AchievmentDetailsService {
	private static final Logger logger = LoggerFactory.getLogger(AchievementDetailServiceImpl.class.getName());
	@Autowired
	private AchievementDetailsRepository achievementDetailsRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {
		CommonDocumentUtils.startHook(logger, "saveOrUpdate");
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				AchievementDetailRequest achievementDetailRequest = MultipleJSONObjectHelper.getObjectFromMap(obj, AchievementDetailRequest.class);
				AchievementDetail achievementDetail = new AchievementDetail();
				BeanUtils.copyProperties(achievementDetailRequest, achievementDetail);
				if (achievementDetailRequest.getId() == null) {
					achievementDetail.setCreatedBy(frameRequest.getUserId());
					achievementDetail.setCreatedDate(new Date());
				}
				achievementDetail.setApplicationId(new LoanApplicationMaster(frameRequest.getApplicationId()));
				achievementDetail.setProposalMapping(new ApplicationProposalMapping(frameRequest.getProposalMappingId()));
				achievementDetail.setModifiedBy(frameRequest.getUserId());
				achievementDetail.setModifiedDate(new Date());
				achievementDetailsRepository.save(achievementDetail);
			}
			CommonDocumentUtils.endHook(logger, "saveOrUpdate");
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save achievementDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public List<AchievementDetailRequest> getAchievementDetailList(Long applicationId, Long userId) throws LoansException {
		try {
			CommonDocumentUtils.startHook(logger, "getAchievementDetailList");
			List<AchievementDetail> achievementDetails = achievementDetailsRepository.listAchievementFromAppId(applicationId,userId);
			List<AchievementDetailRequest> achievementDetailRequests = new ArrayList<>(achievementDetails.size());

			for (AchievementDetail detail : achievementDetails) {
				AchievementDetailRequest achievementDetailRequest = new AchievementDetailRequest();
				BeanUtils.copyProperties(detail, achievementDetailRequest);
				achievementDetailRequests.add(achievementDetailRequest);
			}

			CommonDocumentUtils.endHook(logger, "getAchievementDetailList");
			return achievementDetailRequests;
		} catch (Exception e) {
			logger.error("Exception getting achievementDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	/*multiple bank*/
	@Override
	public List<AchievementDetailRequest> getAchievementDetailListForMultipleBank(Long proposalId) throws Exception {
		try {
			CommonDocumentUtils.startHook(logger, "getAchievementDetailList");
			List<AchievementDetail> achievementDetails = achievementDetailsRepository.listAchievementFromProposalId(proposalId);
			List<AchievementDetailRequest> achievementDetailRequests = new ArrayList<>(achievementDetails.size());

			for (AchievementDetail detail : achievementDetails) {
				AchievementDetailRequest achievementDetailRequest = new AchievementDetailRequest();
				BeanUtils.copyProperties(detail, achievementDetailRequest);
				achievementDetailRequests.add(achievementDetailRequest);
			}

			CommonDocumentUtils.endHook(logger, "getAchievementDetailList");
			return achievementDetailRequests;
		} catch (Exception e) {
			logger.error("Exception getting achievementDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
}
