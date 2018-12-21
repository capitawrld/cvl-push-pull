package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.DirectorBackgroundDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FinancialArrangementsDetail;
import com.capitaworld.service.loans.model.FinancialArrangementsDetailRequest;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FinancialArrangementDetailsRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class FinancialArrangementDetailsServiceImpl implements FinancialArrangementDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(SecurityCorporateDetailsServiceImpl.class);

	@Autowired
	private FinancialArrangementDetailsRepository financialArrangementDetailsRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws Exception {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				FinancialArrangementsDetailRequest financialArrangementsDetailRequest = (FinancialArrangementsDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, FinancialArrangementsDetailRequest.class);
				FinancialArrangementsDetail financialArrangementsDetail = null;
				if (financialArrangementsDetailRequest.getId() != null) {
					financialArrangementsDetail = financialArrangementDetailsRepository
							.findOne(financialArrangementsDetailRequest.getId());
				} else {
					financialArrangementsDetail = new FinancialArrangementsDetail();
					financialArrangementsDetail.setCreatedBy(frameRequest.getUserId());
					financialArrangementsDetail.setCreatedDate(new Date());
				}
				BeanUtils.copyProperties(financialArrangementsDetailRequest, financialArrangementsDetail);
				financialArrangementsDetail
						.setApplicationId(new LoanApplicationMaster(frameRequest.getApplicationId()));
				financialArrangementsDetail.setModifiedBy(frameRequest.getUserId());
				financialArrangementsDetail.setModifiedDate(new Date());
				financialArrangementDetailsRepository.save(financialArrangementsDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save financialArrangementsDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<FinancialArrangementsDetailRequest> getFinancialArrangementDetailsList(Long id, Long userId)
			throws Exception {
		try {
			List<FinancialArrangementsDetail> financialArrangementDetails = financialArrangementDetailsRepository.listSecurityCorporateDetailFromAppId(id);
			List<FinancialArrangementsDetailRequest> financialArrangementDetailRequests = new ArrayList<FinancialArrangementsDetailRequest>();

			for (FinancialArrangementsDetail detail : financialArrangementDetails) {
				FinancialArrangementsDetailRequest financialArrangementDetailsRequest = new FinancialArrangementsDetailRequest();
				BeanUtils.copyProperties(detail, financialArrangementDetailsRequest);
				if(!CommonUtils.isObjectNullOrEmpty(detail.getDirectorBackgroundDetail())) {
					financialArrangementDetailsRequest.setDirectorId(detail.getDirectorBackgroundDetail().getId());					
				}
				financialArrangementDetailRequests.add(financialArrangementDetailsRequest);
				
			}
			return financialArrangementDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in save financialArrangementsDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	
	

	@Override
	public List<FinancialArrangementsDetailRequest> getManuallyAddedFinancialArrangementDetailsList(Long applicationId) {
		try {
			List<FinancialArrangementsDetail> financialArrangementDetails = financialArrangementDetailsRepository.getManuallyAddedFinancialDetail(applicationId);
			List<FinancialArrangementsDetailRequest> financialArrangementDetailRequests = new ArrayList<FinancialArrangementsDetailRequest>();

			for (FinancialArrangementsDetail detail : financialArrangementDetails) {
				FinancialArrangementsDetailRequest financialArrangementDetailsRequest = new FinancialArrangementsDetailRequest();
				BeanUtils.copyProperties(detail, financialArrangementDetailsRequest);
				if(!CommonUtils.isObjectNullOrEmpty(detail.getDirectorBackgroundDetail())) {
					financialArrangementDetailsRequest.setDirectorId(detail.getDirectorBackgroundDetail().getId());					
				}
				financialArrangementDetailRequests.add(financialArrangementDetailsRequest);
				
			}
			return financialArrangementDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in save financialArrangementsDetail  :-",e);
			return Collections.emptyList();
		}
	}

	@Override
	public Boolean saveOrUpdate(List<FinancialArrangementsDetailRequest> finArrDetailRequest,
			Long applicationId, Long userId) {
		financialArrangementDetailsRepository.inActive(userId, applicationId);
		for (FinancialArrangementsDetailRequest req : finArrDetailRequest) {
			FinancialArrangementsDetail arrangementsDetail = new FinancialArrangementsDetail();
			BeanUtils.copyProperties(req, arrangementsDetail,"id");
			arrangementsDetail.setApplicationId(new LoanApplicationMaster(applicationId));
			arrangementsDetail.setCreatedBy(userId);
			arrangementsDetail.setCreatedDate(new Date());
			arrangementsDetail.setIsActive(true);
			financialArrangementDetailsRepository.save(arrangementsDetail);
		}
		return true;
	}
	
	@Override
	public Boolean saveOrUpdateManuallyAddedLoans(List<FinancialArrangementsDetailRequest> finArrDetailRequest,Long applicationId,Long userId) {
		financialArrangementDetailsRepository.inActiveManuallyAddedLoans(userId, applicationId);
		for (FinancialArrangementsDetailRequest req : finArrDetailRequest) {
			FinancialArrangementsDetail arrangementsDetail = new FinancialArrangementsDetail();
			BeanUtils.copyProperties(req, arrangementsDetail,"id");
			arrangementsDetail.setApplicationId(new LoanApplicationMaster(applicationId));
			arrangementsDetail.setCreatedBy(userId);
			arrangementsDetail.setCreatedDate(new Date());
			arrangementsDetail.setIsActive(true);
			financialArrangementDetailsRepository.save(arrangementsDetail);
		}
		return true;
	}
	

	@Override
	public Boolean saveOrUpdate(List<FinancialArrangementsDetailRequest> existingLoanDetailRequest, Long applicationId,
			Long userId, Long directorId) {
		financialArrangementDetailsRepository.inActive(userId, applicationId,directorId);
		for (FinancialArrangementsDetailRequest req : existingLoanDetailRequest) {
			FinancialArrangementsDetail arrangementsDetail = new FinancialArrangementsDetail();
			BeanUtils.copyProperties(req, arrangementsDetail);
			arrangementsDetail.setApplicationId(new LoanApplicationMaster(applicationId));
			arrangementsDetail.setCreatedBy(userId);
			arrangementsDetail.setCreatedDate(new Date());
			arrangementsDetail.setIsActive(true);
			arrangementsDetail.setDirectorBackgroundDetail(new DirectorBackgroundDetail(directorId));
			financialArrangementDetailsRepository.save(arrangementsDetail);
		}
		return true;
	}

	@Override
	public FinancialArrangementsDetailRequest getTotalEmiAndSanctionAmountByApplicationId(Long applicationId) {
		Double totalEmi = financialArrangementDetailsRepository.getTotalEmiByApplicationId(applicationId);
		logger.info("getTotalOfEmiByApplicationId=====>" + totalEmi + " For Application Id=====>{}", applicationId);		
		List<String> loanTypes = Arrays.asList(new String[]{"cash credit","overdraft"});
		Double existingLimits = financialArrangementDetailsRepository.getExistingLimits(applicationId, loanTypes);
		logger.info("existingLimits=====>" + existingLimits + " For Application Id=====>{}", applicationId);
		FinancialArrangementsDetailRequest arrangementsDetailRequest = new FinancialArrangementsDetailRequest();
		arrangementsDetailRequest.setAmount(existingLimits);
		arrangementsDetailRequest.setEmi(totalEmi);
		return arrangementsDetailRequest;
	}

	@Override
	public Double getTotalOfEmiByApplicationIdAndDirectorId(Long applicationId, Long directorId) {
		Double totalEmi = financialArrangementDetailsRepository.getTotalEmiByApplicationIdAndDirectorId(applicationId,directorId);
		logger.info("getTotalOfEmiByApplicationIdAndDirectorId {} For Application Id = {} DirectorId = {}", totalEmi ,applicationId,directorId);
		return totalEmi;
	}

	/* (non-Javadoc)
	 * @see com.capitaworld.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService#getFinancialArrangementDetailsListDirId(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<FinancialArrangementsDetailRequest> getFinancialArrangementDetailsListDirId(Long dirId, Long id)
			throws Exception {
		try {
			List<FinancialArrangementsDetail> financialArrangementDetails = financialArrangementDetailsRepository.findByDirectorBackgroundDetailIdAndApplicationIdIdAndIsActive(dirId,id,true);
			List<FinancialArrangementsDetailRequest> financialArrangementDetailRequests = new ArrayList<FinancialArrangementsDetailRequest>(financialArrangementDetails.size());

			for (FinancialArrangementsDetail detail : financialArrangementDetails) {
				FinancialArrangementsDetailRequest financialArrangementDetailsRequest = new FinancialArrangementsDetailRequest();
				BeanUtils.copyProperties(detail, financialArrangementDetailsRequest);
				financialArrangementDetailRequests.add(financialArrangementDetailsRequest);
			}
			return financialArrangementDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception  in save financialArrangementsDetail  :-",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
}
