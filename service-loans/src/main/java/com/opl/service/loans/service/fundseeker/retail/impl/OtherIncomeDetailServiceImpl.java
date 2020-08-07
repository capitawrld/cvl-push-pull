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
import com.opl.mudra.api.loans.model.retail.OtherIncomeDetailRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.oneform.enums.IncomeDetails;
import com.opl.service.loans.domain.fundseeker.retail.OtherIncomeDetail;
import com.opl.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.retail.CoApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.retail.GuarantorDetailsRepository;
import com.opl.service.loans.repository.fundseeker.retail.OtherIncomeDetailRepository;
import com.opl.service.loans.service.fundseeker.retail.OtherIncomeDetailService;

/**
 * @author Sanket
 *
 */

@Service
@Transactional
public class OtherIncomeDetailServiceImpl implements OtherIncomeDetailService {

	private static final Logger logger = LoggerFactory.getLogger(OtherIncomeDetailServiceImpl.class);

	@Autowired
	private OtherIncomeDetailRepository otherIncomeDetailRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private CoApplicantDetailRepository coApplicantDetailRepository;

	@Autowired
	private GuarantorDetailsRepository guarantorDetailsRepository;

	@Autowired
	private ApplicationProposalMappingRepository applicationProposalMappingRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				OtherIncomeDetailRequest otherIncomeDetailRequest = (OtherIncomeDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, OtherIncomeDetailRequest.class);
				OtherIncomeDetail otherIncomeDetail = new OtherIncomeDetail();
				BeanUtils.copyProperties(otherIncomeDetailRequest, otherIncomeDetail);
				if (otherIncomeDetailRequest.getId() == null) {
					otherIncomeDetail.setCreatedBy(frameRequest.getUserId());
					otherIncomeDetail.setCreatedDate(new Date());
				}
				switch (frameRequest.getApplicantType()) {
				case CommonUtils.ApplicantType.APPLICANT:
					otherIncomeDetail
							.setApplicationId(loanApplicationRepository.findOne(frameRequest.getApplicationId()));
					break;
				case CommonUtils.ApplicantType.COAPPLICANT:
					otherIncomeDetail.setCoApplicantDetailId(
							coApplicantDetailRepository.findOne(frameRequest.getApplicationId()));
					break;
				case CommonUtils.ApplicantType.GARRANTOR:
					otherIncomeDetail
							.setGuarantorDetailId(guarantorDetailsRepository.findOne(frameRequest.getApplicationId()));
					break;
				default:
					throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
				}

				otherIncomeDetail.setProposalId(applicationProposalMappingRepository.findOne(frameRequest.getProposalMappingId()));
				otherIncomeDetail.setModifiedBy(frameRequest.getUserId());
				otherIncomeDetail.setModifiedDate(new Date());
				otherIncomeDetailRepository.save(otherIncomeDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception in save otherIncomeDetail  :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public List<OtherIncomeDetailRequest> getOtherIncomeDetailListForCoApplicant(Long id, Long proposalId, Long coAppId) throws LoansException {
		try {
			List<OtherIncomeDetail> otherIncomeDetails;
			otherIncomeDetails = otherIncomeDetailRepository.listOtherIncomeFromProposalIdAndCoAppId(proposalId, coAppId);
			List<OtherIncomeDetailRequest> otherIncomeRequests = new ArrayList<OtherIncomeDetailRequest>();

			for (OtherIncomeDetail detail : otherIncomeDetails) {
				OtherIncomeDetailRequest otherIncomeRequest = new OtherIncomeDetailRequest();
				BeanUtils.copyProperties(detail, otherIncomeRequest);
				otherIncomeRequest.setNetIncomeString( !CommonUtils.isObjectNullOrEmpty(otherIncomeRequest.getNetIncome()) ? CommonUtils.convertValueWithoutDecimal(otherIncomeRequest.getNetIncome()) : null);
				otherIncomeRequest.setGrossIncomeString( !CommonUtils.isObjectNullOrEmpty(otherIncomeRequest.getGrossIncome()) ? CommonUtils.convertValueWithoutDecimal(otherIncomeRequest.getGrossIncome()) : null);
				otherIncomeRequest.setIncomeDetailsType(!CommonUtils.isObjectNullOrEmpty(otherIncomeRequest.getIncomeDetailsId()) ? IncomeDetails.getById(otherIncomeRequest.getIncomeDetailsId()).getValue() : "-" );
				otherIncomeRequests.add(otherIncomeRequest);
			}
			return otherIncomeRequests;
		} catch (Exception e) {
			logger.error("Exception in getting otherIncomeDetail :- ", e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<OtherIncomeDetailRequest> getOtherIncomeDetailList(Long id, int applicationType) throws LoansException {
		try {
			List<OtherIncomeDetail> otherIncomeDetails;
			otherIncomeDetails = otherIncomeDetailRepository.listOtherIncomeFromAppId(id);
			List<OtherIncomeDetailRequest> otherIncomeRequests = new ArrayList<OtherIncomeDetailRequest>();

			for (OtherIncomeDetail detail : otherIncomeDetails) {
				OtherIncomeDetailRequest otherIncomeRequest = new OtherIncomeDetailRequest();
				BeanUtils.copyProperties(detail, otherIncomeRequest);
				otherIncomeRequests.add(otherIncomeRequest);
			}
			return otherIncomeRequests;
		}

		catch (Exception e) {
			logger.error("Exception in getting otherIncomeDetail :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<OtherIncomeDetailRequest> getOtherIncomeDetailList(Long id, int applicationType,Long proposalId) throws LoansException {
		try {
			List<OtherIncomeDetail> otherIncomeDetails;
			otherIncomeDetails = otherIncomeDetailRepository.listOtherIncomeFromAppId(id,proposalId);
			List<OtherIncomeDetailRequest> otherIncomeRequests = new ArrayList<OtherIncomeDetailRequest>();

			for (OtherIncomeDetail detail : otherIncomeDetails) {
				OtherIncomeDetailRequest otherIncomeRequest = new OtherIncomeDetailRequest();
				BeanUtils.copyProperties(detail, otherIncomeRequest);
				otherIncomeRequest.setNetIncomeString( !CommonUtils.isObjectNullOrEmpty(otherIncomeRequest.getNetIncome()) ? CommonUtils.convertValueWithoutDecimal(otherIncomeRequest.getNetIncome()) : null);
				otherIncomeRequest.setGrossIncomeString( !CommonUtils.isObjectNullOrEmpty(otherIncomeRequest.getGrossIncome()) ? CommonUtils.convertValueWithoutDecimal(otherIncomeRequest.getGrossIncome()) : null);
				otherIncomeRequest.setIncomeDetailsType(!CommonUtils.isObjectNullOrEmpty(otherIncomeRequest.getIncomeDetailsId()) ? IncomeDetails.getById(otherIncomeRequest.getIncomeDetailsId()).getValue() : "-" ); 
				otherIncomeRequests.add(otherIncomeRequest);
			}
			return otherIncomeRequests;
		}

		catch (Exception e) {
			logger.error("Exception in getting otherIncomeDetail :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}


	@Override
	public Boolean saveOrUpdateCoApplicant(FrameRequest frameRequest) throws LoansException {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				OtherIncomeDetailRequest otherIncomeDetailRequest = (OtherIncomeDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, OtherIncomeDetailRequest.class);
				OtherIncomeDetail otherIncomeDetail = new OtherIncomeDetail();
				BeanUtils.copyProperties(otherIncomeDetailRequest, otherIncomeDetail);
				if (otherIncomeDetailRequest.getId() == null) {
					otherIncomeDetail.setCreatedBy(frameRequest.getUserId());
					otherIncomeDetail.setCreatedDate(new Date());
				}
				otherIncomeDetail
						.setApplicationId(loanApplicationRepository.findOne(frameRequest.getApplicationId()));
				otherIncomeDetail.setCoApplicantDetailId(
						coApplicantDetailRepository.findOne(frameRequest.getCoApplicantId()));

				otherIncomeDetail.setProposalId(applicationProposalMappingRepository.findOne(frameRequest.getProposalMappingId()));
				otherIncomeDetail.setModifiedBy(frameRequest.getUserId());
				otherIncomeDetail.setModifiedDate(new Date());
				otherIncomeDetailRepository.save(otherIncomeDetail);
			}
			return true;
		} catch (Exception e) {
			logger.error("Exception in save otherIncomeDetail  :- ", e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}
}
