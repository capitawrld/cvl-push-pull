package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CorporateMcqDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.OverseasNetworkMappingDetail;
import com.capitaworld.service.loans.model.corporate.CorporateMcqRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CorporateMcqDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.OverseasNetworkRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateMcqService;
import com.capitaworld.service.loans.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CorporateMcqServiceImpl implements CorporateMcqService {

    private static final Logger logger = LoggerFactory.getLogger(CorporateMcqServiceImpl.class.getName());

    @Autowired
    private CorporateMcqDetailRepository corporateMcqDetailRepository;

    @Autowired
    private OverseasNetworkRepository networkRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private ApplicationProposalMappingRepository applicationProposalMappingRepository;

    @Override
    public boolean saveOrUpdate(CorporateMcqRequest corporateMcqRequest, Long userId) throws Exception {
        try {
            Long finalUserId = (CommonUtils.isObjectNullOrEmpty(corporateMcqRequest.getClientId()) ? userId : corporateMcqRequest.getClientId());
            CorporateMcqDetail corporateMcqDetail = corporateMcqDetailRepository.getByProposalIdAndUserId(corporateMcqRequest.getProposalMappingId());
            if (corporateMcqDetail != null) {
                // Inactive Previous Mapping
                //networkRepository.inActiveMappingByApplicationId(corporateMcqRequest.getApplicationId());
                corporateMcqDetail.setModifiedBy(userId);
                corporateMcqDetail.setModifiedDate(new Date());
            } else {
                corporateMcqDetail = new CorporateMcqDetail();
                corporateMcqDetail.setCreatedBy(userId);
                corporateMcqDetail.setCreatedDate(new Date());
                corporateMcqDetail.setActive(true);
                corporateMcqDetail.setApplicationId(new LoanApplicationMaster(corporateMcqRequest.getApplicationId()));
                corporateMcqDetail.setApplicationProposalMapping(new ApplicationProposalMapping(corporateMcqRequest.getProposalMappingId()));
            }
            BeanUtils.copyProperties(corporateMcqRequest, corporateMcqDetail, CommonUtils.IgnorableCopy.CORPORATE);
            corporateMcqDetail = corporateMcqDetailRepository.save(corporateMcqDetail);

            // saving Data
            /* saveOverseasNetworkMapping(corporateMcqRequest.getApplicationId(), userId, corporateMcqRequest.getOverseasNetworkIds());
            */
            //setting flag

            //loanApplicationRepository.setIsFinalMcqMandatoryFilled(corporateMcqRequest.getApplicationId(), finalUserId, CommonUtils.isObjectNullOrEmpty(corporateMcqRequest.getFinalMcqFilled()) ? false : corporateMcqRequest.getFinalMcqFilled());
            //loanApplicationRepository.setFinalFilledCount(corporateMcqRequest.getApplicationId(), finalUserId, corporateMcqRequest.getFinalFilledCount());

            applicationProposalMappingRepository.setIsFinalMcqMandatoryFilled(corporateMcqRequest.getProposalMappingId(), finalUserId, CommonUtils.isObjectNullOrEmpty(corporateMcqRequest.getFinalMcqFilled()) ? false : corporateMcqRequest.getFinalMcqFilled());
            return true;
        } catch (Exception e) {
            logger.error("Error while Saving Corporate final mcq Details:-");
            e.printStackTrace();
            throw new Exception("Something went Wrong !");
        }
    }

    private void saveOverseasNetworkMapping(Long applicationId, Long userId, List<Integer> overseasNetworkIds) {
        for (Integer networkId : overseasNetworkIds) {
            OverseasNetworkMappingDetail mappingDetail = new OverseasNetworkMappingDetail();
            mappingDetail.setApplicationId(applicationId);
            mappingDetail.setOverseasNetworkId(networkId);
            mappingDetail.setActive(true);
            mappingDetail.setCreatedDate(new Date());
            mappingDetail.setCreatedBy(userId);
            networkRepository.save(mappingDetail);
        }
    }

    @Override
    public CorporateMcqRequest get(Long proposalId) throws Exception {
        try {
            CorporateMcqDetail loanDetail = corporateMcqDetailRepository.getByProposalId(proposalId);
            if (loanDetail == null) {
                throw new NullPointerException("FinalTermLoanDetail not exist in DB with proposal Id==>" + proposalId);
            }
            CorporateMcqRequest corporateMcqRequest = new CorporateMcqRequest();
            BeanUtils.copyProperties(loanDetail, corporateMcqRequest);
            //corporateMcqRequest.setOverseasNetworkIds(networkRepository.getOverseasNetworkIds(applicationId));
            return corporateMcqRequest;
        } catch (Exception e) {
            logger.error("Error while getting Final Mcq Details:-");
            e.printStackTrace();
            throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
        }
    }
}
