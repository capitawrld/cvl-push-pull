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
import com.capitaworld.service.loans.domain.fundseeker.corporate.AssociatedConcernDetail;
import com.capitaworld.service.loans.model.AssociatedConcernDetailRequest;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AssociatedConcernDetailRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.AssociatedConcernDetailService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class AssociatedConcernDetailServiceImpl implements AssociatedConcernDetailService {

	private static final Logger logger = LoggerFactory.getLogger(AssociatedConcernDetailServiceImpl.class.getName());
	@Autowired
	private AssociatedConcernDetailRepository associatedConcernDetailRepository;
	
	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws Exception {
		try {
			CommonDocumentUtils.startHook(logger, "saveOrUpdate");
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				AssociatedConcernDetailRequest associatedConcernDetailRequest = (AssociatedConcernDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, AssociatedConcernDetailRequest.class);
				AssociatedConcernDetail associatedConcernDetail = null;
				if (associatedConcernDetailRequest.getId() != null) {
					associatedConcernDetail = associatedConcernDetailRepository.findOne(associatedConcernDetailRequest.getId());
				} else {
					associatedConcernDetail = new AssociatedConcernDetail();
					associatedConcernDetail.setCreatedBy(frameRequest.getUserId());
					associatedConcernDetail.setCreatedDate(new Date());
					associatedConcernDetail.setApplicationId(new LoanApplicationMaster(frameRequest.getApplicationId()));
					associatedConcernDetail.setApplicationProposalMapping(new ApplicationProposalMapping(frameRequest.getProposalMappingId()));
				}
				BeanUtils.copyProperties(associatedConcernDetailRequest, associatedConcernDetail);
				associatedConcernDetail.setModifiedBy(frameRequest.getUserId());
				associatedConcernDetail.setModifiedDate(new Date());
				associatedConcernDetailRepository.save(associatedConcernDetail);
			}
			CommonDocumentUtils.endHook(logger, "saveOrUpdate");
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save Associated Concern :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}


	@Override
	public List<AssociatedConcernDetailRequest> getAssociatedConcernsDetailListByProposalId(Long proposalId,Long userId) throws Exception {
		try {
			CommonDocumentUtils.startHook(logger, "getAssociatedConcernsDetailList");
			List<AssociatedConcernDetail> associatedConcernDetail = associatedConcernDetailRepository
					.listAssociatedConcernFromProposalId(proposalId);
			List<AssociatedConcernDetailRequest> associatedConcernDetailRequests = new ArrayList<AssociatedConcernDetailRequest>();

			for (AssociatedConcernDetail detail : associatedConcernDetail) {
				AssociatedConcernDetailRequest associatedConcernDetailRequest = new AssociatedConcernDetailRequest();
				associatedConcernDetailRequest.setProfitPastOneYearString(CommonUtils.convertValue(detail.getProfitPastOneYear()));
				associatedConcernDetailRequest.setProfitPastTwoYearString(CommonUtils.convertValue(detail.getProfitPastTwoYear()));
				associatedConcernDetailRequest.setProfitPastThreeYearString(CommonUtils.convertValue(detail.getProfitPastThreeYear()));
				associatedConcernDetailRequest.setTurnOverFirstYearString(CommonUtils.convertValue(detail.getTurnOverFirstYear()));
				associatedConcernDetailRequest.setTurnOverSecondYearString(CommonUtils.convertValue(detail.getTurnOverSecondYear()));
				associatedConcernDetailRequest.setTurnOverThirdYearString(CommonUtils.convertValue(detail.getTurnOverThirdYear()));
				BeanUtils.copyProperties(detail, associatedConcernDetailRequest);
				AssociatedConcernDetailRequest.printFields(associatedConcernDetailRequest);
				associatedConcernDetailRequests.add(associatedConcernDetailRequest);
			}
			CommonDocumentUtils.endHook(logger, "getAssociatedConcernsDetailList");
			return associatedConcernDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in get monthlyTurnoverDetail  :-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public Boolean saveOrUpdate(List<AssociatedConcernDetailRequest> requests,Long applicationId,Long userId) {
		// Inactivating Previous Details
		associatedConcernDetailRepository.inActive(userId, applicationId);
		AssociatedConcernDetail associatedConcernDetail = null;
		for (AssociatedConcernDetailRequest associatedConcernDetailRequest : requests) {
			associatedConcernDetail = new AssociatedConcernDetail();
			associatedConcernDetail.setCreatedBy(userId);
			associatedConcernDetail.setCreatedDate(new Date());
			associatedConcernDetail.setApplicationId(new LoanApplicationMaster(applicationId));
			BeanUtils.copyProperties(associatedConcernDetailRequest, associatedConcernDetail);
			associatedConcernDetail.setModifiedBy(userId);
			associatedConcernDetail.setModifiedDate(new Date());
			associatedConcernDetailRepository.save(associatedConcernDetail);
		}
		return true;
	}


	@Override
	public List<AssociatedConcernDetailRequest> getAssociatedConcernsDetailList(Long id,Long userId) throws Exception {
		try {
			CommonDocumentUtils.startHook(logger, "getAssociatedConcernsDetailList");
			List<AssociatedConcernDetail> associatedConcernDetail = associatedConcernDetailRepository.listAssociatedConcernFromAppId(id);
			List<AssociatedConcernDetailRequest> associatedConcernDetailRequests = new ArrayList<AssociatedConcernDetailRequest>(associatedConcernDetail.size());
			for (AssociatedConcernDetail detail : associatedConcernDetail) {
				AssociatedConcernDetailRequest associatedConcernDetailRequest = new AssociatedConcernDetailRequest();
				associatedConcernDetailRequest.setProfitPastOneYearString(CommonUtils.convertValue(detail.getProfitPastOneYear()));
				associatedConcernDetailRequest.setProfitPastTwoYearString(CommonUtils.convertValue(detail.getProfitPastTwoYear()));
				associatedConcernDetailRequest.setProfitPastThreeYearString(CommonUtils.convertValue(detail.getProfitPastThreeYear()));
				associatedConcernDetailRequest.setTurnOverFirstYearString(CommonUtils.convertValue(detail.getTurnOverFirstYear()));
				associatedConcernDetailRequest.setTurnOverSecondYearString(CommonUtils.convertValue(detail.getTurnOverSecondYear()));
				associatedConcernDetailRequest.setTurnOverThirdYearString(CommonUtils.convertValue(detail.getTurnOverThirdYear()));
				BeanUtils.copyProperties(detail, associatedConcernDetailRequest);
				AssociatedConcernDetailRequest.printFields(associatedConcernDetailRequest);
				associatedConcernDetailRequests.add(associatedConcernDetailRequest);
			}
			CommonDocumentUtils.endHook(logger, "getAssociatedConcernsDetailList");
			return associatedConcernDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in get monthlyTurnoverDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

}
