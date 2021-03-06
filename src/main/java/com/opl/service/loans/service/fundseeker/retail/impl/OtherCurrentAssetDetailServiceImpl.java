package com.opl.service.loans.service.fundseeker.retail.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.retail.OtherCurrentAssetDetailRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.oneform.enums.Assets;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.retail.OtherCurrentAssetDetail;
import com.opl.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.retail.CoApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.retail.GuarantorDetailsRepository;
import com.opl.service.loans.repository.fundseeker.retail.OtherCurrentAssetDetailRepository;
import com.opl.service.loans.service.fundseeker.retail.OtherCurrentAssetDetailService;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class OtherCurrentAssetDetailServiceImpl implements OtherCurrentAssetDetailService {
	
	private static final Logger logger = LoggerFactory.getLogger(OtherCurrentAssetDetailServiceImpl.class);
	
	@Autowired
	private OtherCurrentAssetDetailRepository otherCurrentAssetDetailRepository;
	
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
				OtherCurrentAssetDetailRequest otherCurrentAssetDetailRequest = (OtherCurrentAssetDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, OtherCurrentAssetDetailRequest.class);
				OtherCurrentAssetDetail otherCurrentAssetDetail = new OtherCurrentAssetDetail();
				BeanUtils.copyProperties(otherCurrentAssetDetailRequest, otherCurrentAssetDetail);
				if (otherCurrentAssetDetailRequest.getId() == null) {
					otherCurrentAssetDetail.setCreatedBy(frameRequest.getUserId());
					otherCurrentAssetDetail.setCreatedDate(new Date());
				}
				switch(frameRequest.getApplicantType()) {
				case CommonUtils.ApplicantType.APPLICANT:
					otherCurrentAssetDetail.setApplicationId(loanApplicationRepository.findOne(frameRequest.getApplicationId()));
					break;
				case CommonUtils.ApplicantType.COAPPLICANT:
					otherCurrentAssetDetail.setCoApplicantDetailId(coApplicantDetailRepository.findOne(frameRequest.getApplicationId()));
					break;
				case CommonUtils.ApplicantType.GARRANTOR:
					otherCurrentAssetDetail.setGuarantorDetailId(guarantorDetailsRepository.findOne(frameRequest.getApplicationId()));
					break;
				default :
					throw new LoansException();
				}
				
				ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.findByProposalIdAndIsActive(frameRequest.getProposalMappingId(), true);
				otherCurrentAssetDetail.setApplicationProposalMapping(applicationProposalMapping);
				otherCurrentAssetDetail.setModifiedBy(frameRequest.getUserId());
				otherCurrentAssetDetail.setModifiedDate(new Date());
				otherCurrentAssetDetailRepository.save(otherCurrentAssetDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save otherCurrentAssetDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public List<OtherCurrentAssetDetailRequest> getOtherCurrentAssetDetailList(Long id, int applicationType) throws LoansException {

		List<OtherCurrentAssetDetail> otherCurrentAssetDetails;
		switch (applicationType) {
		case CommonUtils.ApplicantType.APPLICANT:
			otherCurrentAssetDetails = otherCurrentAssetDetailRepository.listOtherCurrentAssetFromAppId(id);
			break;
		case CommonUtils.ApplicantType.COAPPLICANT:
			otherCurrentAssetDetails = otherCurrentAssetDetailRepository.listOtherCurrentAssetFromCoAppId(id);
			break;
		case CommonUtils.ApplicantType.GARRANTOR:
			otherCurrentAssetDetails = otherCurrentAssetDetailRepository.listOtherCurrentAssetFromGarrId(id);
			break;
		default:
			throw new LoansException();
		}
		
		List<OtherCurrentAssetDetailRequest> otherCurrentAssetRequests = new ArrayList<OtherCurrentAssetDetailRequest>();

		for (OtherCurrentAssetDetail detail : otherCurrentAssetDetails) {
			OtherCurrentAssetDetailRequest otherCurrentAssetRequest = new OtherCurrentAssetDetailRequest();
			otherCurrentAssetRequest.setAssetValueString(CommonUtils.convertValue(detail.getAssetValue()));
			otherCurrentAssetRequest.setAssetType(!CommonUtils.isObjectNullOrEmpty(detail.getAssetTypesId()) ? StringEscapeUtils.escapeXml(Assets.getById(detail.getAssetTypesId()).getValue()) : "");
			BeanUtils.copyProperties(detail, otherCurrentAssetRequest);
			otherCurrentAssetRequests.add(otherCurrentAssetRequest);
		}
		return otherCurrentAssetRequests;
	}

	@Override
	public List<OtherCurrentAssetDetailRequest> getOtherCurrentAssetDetailListByProposalIdAndCoAppId(Long proposalId,
																						   Long coAppId) throws LoansException {
		List<OtherCurrentAssetDetail> otherCurrentAssetDetails;

		otherCurrentAssetDetails = otherCurrentAssetDetailRepository.listOtherCurrentAssetFromProposalIdAndCoAppId(proposalId,coAppId);
		List<OtherCurrentAssetDetailRequest> otherCurrentAssetRequests = new ArrayList<OtherCurrentAssetDetailRequest>();

		for (OtherCurrentAssetDetail detail : otherCurrentAssetDetails) {
			OtherCurrentAssetDetailRequest otherCurrentAssetRequest = new OtherCurrentAssetDetailRequest();
			otherCurrentAssetRequest.setAssetValueString(!CommonUtils.isObjectNullOrEmpty(detail.getAssetValue()) ? CommonUtils.convertValueWithoutDecimal(detail.getAssetValue()) : null);
			otherCurrentAssetRequest.setAssetType(!CommonUtils.isObjectNullOrEmpty(detail.getAssetTypesId()) ? StringEscapeUtils.escapeXml(Assets.getById(detail.getAssetTypesId()).getValue()) : "");
			BeanUtils.copyProperties(detail, otherCurrentAssetRequest);
			otherCurrentAssetRequests.add(otherCurrentAssetRequest);
		}
		return otherCurrentAssetRequests;
	}

	@Override
	public List<OtherCurrentAssetDetailRequest> getOtherCurrentAssetDetailListByProposalId(Long proposalId,
			int applicationType) throws LoansException {
		List<OtherCurrentAssetDetail> otherCurrentAssetDetails;
//		switch (applicationType) {
//		case CommonUtils.ApplicantType.APPLICANT:
//			otherCurrentAssetDetails = otherCurrentAssetDetailRepository.listOtherCurrentAssetFromAppId(id);
//			break;
//		case CommonUtils.ApplicantType.COAPPLICANT:
//			otherCurrentAssetDetails = otherCurrentAssetDetailRepository.listOtherCurrentAssetFromCoAppId(id);
//			break;
//		case CommonUtils.ApplicantType.GARRANTOR:
//			otherCurrentAssetDetails = otherCurrentAssetDetailRepository.listOtherCurrentAssetFromGarrId(id);
//			break;
//		default:
//			throw new LoansException();
//		}
		
		otherCurrentAssetDetails = otherCurrentAssetDetailRepository.listOtherCurrentAssetFromProposalId(proposalId);
		List<OtherCurrentAssetDetailRequest> otherCurrentAssetRequests = new ArrayList<OtherCurrentAssetDetailRequest>();

		for (OtherCurrentAssetDetail detail : otherCurrentAssetDetails) {
			OtherCurrentAssetDetailRequest otherCurrentAssetRequest = new OtherCurrentAssetDetailRequest();
			otherCurrentAssetRequest.setAssetValueString(!CommonUtils.isObjectNullOrEmpty(detail.getAssetValue()) ? CommonUtils.convertValueWithoutDecimal(detail.getAssetValue()) : null);
			otherCurrentAssetRequest.setAssetType(!CommonUtils.isObjectNullOrEmpty(detail.getAssetTypesId()) ? StringEscapeUtils.escapeXml(Assets.getById(detail.getAssetTypesId()).getValue()) : "");
			BeanUtils.copyProperties(detail, otherCurrentAssetRequest);
			otherCurrentAssetRequests.add(otherCurrentAssetRequest);
		}
		return otherCurrentAssetRequests;
	}

	@Override
	public Boolean saveOrUpdateCoApplicant(FrameRequest frameRequest) throws LoansException {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				OtherCurrentAssetDetailRequest otherCurrentAssetDetailRequest = (OtherCurrentAssetDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, OtherCurrentAssetDetailRequest.class);
				OtherCurrentAssetDetail otherCurrentAssetDetail = new OtherCurrentAssetDetail();
				BeanUtils.copyProperties(otherCurrentAssetDetailRequest, otherCurrentAssetDetail);
				if (otherCurrentAssetDetailRequest.getId() == null) {
					otherCurrentAssetDetail.setCreatedBy(frameRequest.getUserId());
					otherCurrentAssetDetail.setCreatedDate(new Date());
				}
				otherCurrentAssetDetail.setApplicationId(loanApplicationRepository.findOne(frameRequest.getApplicationId()));
				otherCurrentAssetDetail.setCoApplicantDetailId(coApplicantDetailRepository.findOne(frameRequest.getCoApplicantId()));

				ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.findByProposalIdAndIsActive(frameRequest.getProposalMappingId(), true);
				otherCurrentAssetDetail.setApplicationProposalMapping(applicationProposalMapping);
				otherCurrentAssetDetail.setModifiedBy(frameRequest.getUserId());
				otherCurrentAssetDetail.setModifiedDate(new Date());
				otherCurrentAssetDetailRepository.save(otherCurrentAssetDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save otherCurrentAssetDetail  :-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

}
