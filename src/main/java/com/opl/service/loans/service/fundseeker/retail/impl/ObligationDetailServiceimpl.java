package com.opl.service.loans.service.fundseeker.retail.impl;

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
import com.opl.mudra.api.loans.model.retail.ObligationDetailRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.retail.ObligationDetail;
import com.opl.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.retail.ObligationDetailRepository;
import com.opl.service.loans.service.fundseeker.retail.ObligationDetailService;

/**
 * Created by ravina.panchal on 04-10-2018.
 */
@Service
@Transactional
public class ObligationDetailServiceimpl implements ObligationDetailService {


    private static final Logger logger = LoggerFactory.getLogger(OtherCurrentAssetDetailServiceImpl.class);

    @Autowired
    private ObligationDetailRepository obligationDetailRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    
    @Autowired
    private ApplicationProposalMappingRepository applicationProposalMappingRepository;
    
    @Override
    public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {
        try {
            for (Map<String, Object> obj : frameRequest.getDataList()) {
                ObligationDetailRequest obligationDetailRequest = (ObligationDetailRequest) MultipleJSONObjectHelper
                        .getObjectFromMap(obj, ObligationDetailRequest.class);
                ObligationDetail obligationDetail = new ObligationDetail();
                BeanUtils.copyProperties(obligationDetailRequest, obligationDetail);
                if (obligationDetailRequest.getId() == null) {
                    obligationDetail.setCreatedBy(frameRequest.getUserId());
                    obligationDetail.setCreatedDate(new Date());
                }
                switch(frameRequest.getApplicantType()) {
                    case CommonUtils.ApplicantType.APPLICANT:
                        obligationDetail.setApplicationId(loanApplicationRepository.findOne(frameRequest.getApplicationId()));
                        break;

                    default :
                        throw new LoansException();
                }

                ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.findByProposalIdAndIsActive(frameRequest.getProposalMappingId(), true);
                obligationDetail.setApplicationProposalMapping(applicationProposalMapping);
                obligationDetail.setModifiedBy(frameRequest.getUserId());
                obligationDetail.setModifiedDate(new Date());
                obligationDetailRepository.save(obligationDetail);
            }
            return true;
        }

        catch (Exception e) {
            logger.error("Exception  in save obligationDetail  :-",e);
            throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public List<ObligationDetailRequest> getObligationDetailList(Long id, int applicationType) throws LoansException {
        List<ObligationDetail> otherCurrentAssetDetails;
        switch (applicationType) {
            case CommonUtils.ApplicantType.APPLICANT:
                otherCurrentAssetDetails = obligationDetailRepository.listObligationDetailFromAppId(id);
                break;

            default:
                throw new LoansException();
        }

        List<ObligationDetailRequest> obligationDetailRequests = new ArrayList<ObligationDetailRequest>();

        for (ObligationDetail detail : otherCurrentAssetDetails) {
            ObligationDetailRequest obligationDetailRequest = new ObligationDetailRequest();
            obligationDetailRequest.setGrossAmountString(CommonUtils.convertValue(detail.getGrossAmount()));
            obligationDetailRequest.setNetAmountString(CommonUtils.convertValue(detail.getNetAmount()));
            obligationDetailRequest.setPeriodicityString(CommonUtils.convertValue(detail.getPeriodicity()));
            BeanUtils.copyProperties(detail, obligationDetailRequest);
            obligationDetailRequests.add(obligationDetailRequest);
        }
        return obligationDetailRequests;
    }

	@Override
	public List<ObligationDetailRequest> getObligationDetailsFromProposalId(Long proposalId, int applicationType)
			throws LoansException {
		List<ObligationDetail> otherCurrentAssetDetails;
		otherCurrentAssetDetails = obligationDetailRepository.listObligationDetailFromProposalId(proposalId);
		
//        switch (applicationType) {
//            case CommonUtils.ApplicantType.APPLICANT:
//                otherCurrentAssetDetails = obligationDetailRepository.listObligationDetailFromProposalId(proposalId);
//                break;
//
//            default:
//                throw new LoansException();
//        }

        List<ObligationDetailRequest> obligationDetailRequests = new ArrayList<ObligationDetailRequest>();

        for (ObligationDetail detail : otherCurrentAssetDetails) {
            ObligationDetailRequest obligationDetailRequest = new ObligationDetailRequest();
            obligationDetailRequest.setGrossAmountString(CommonUtils.convertValue(detail.getGrossAmount()));
            obligationDetailRequest.setNetAmountString(CommonUtils.convertValue(detail.getNetAmount()));
            obligationDetailRequest.setPeriodicityString(CommonUtils.convertValue(detail.getPeriodicity()));
            BeanUtils.copyProperties(detail, obligationDetailRequest);
            obligationDetailRequests.add(obligationDetailRequest);
        }
        return obligationDetailRequests;
    }
    
    

}
