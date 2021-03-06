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
import com.opl.mudra.api.loans.model.common.CreditCardsDetailRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.retail.CoApplicantDetail;
import com.opl.service.loans.domain.fundseeker.retail.CreditCardsDetail;
import com.opl.service.loans.domain.fundseeker.retail.GuarantorDetails;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.retail.CoApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.retail.CreditCardsDetailRepository;
import com.opl.service.loans.repository.fundseeker.retail.GuarantorDetailsRepository;
import com.opl.service.loans.service.fundseeker.retail.CreditCardsDetailService;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class CreditCardsDetailServiceImpl implements CreditCardsDetailService {

	private static final Logger logger = LoggerFactory.getLogger(CreditCardsDetailServiceImpl.class);

	@Autowired
	private CreditCardsDetailRepository creditCardsDetailRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private CoApplicantDetailRepository coApplicantDetailRepository;

	@Autowired
	private GuarantorDetailsRepository guarantorDetailsRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				CreditCardsDetailRequest creditCardsDetailRequest = (CreditCardsDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, CreditCardsDetailRequest.class);
				CreditCardsDetail crediCardsDetail = new CreditCardsDetail();
				BeanUtils.copyProperties(creditCardsDetailRequest, crediCardsDetail);
				if (creditCardsDetailRequest.getId() == null) {
					crediCardsDetail.setCreatedBy(frameRequest.getUserId());
					crediCardsDetail.setCreatedDate(new Date());
				}
				switch (frameRequest.getApplicantType()) {
				case CommonUtils.ApplicantType.APPLICANT:
					crediCardsDetail
							.setApplicantionId(loanApplicationRepository.findOne(frameRequest.getApplicationId()));
					break;
				case CommonUtils.ApplicantType.COAPPLICANT:
					crediCardsDetail.setCoApplicantDetailId(
							coApplicantDetailRepository.findOne(frameRequest.getApplicationId()));
					break;
				case CommonUtils.ApplicantType.GARRANTOR:
					crediCardsDetail
							.setGuarantorDetailId(guarantorDetailsRepository.findOne(frameRequest.getApplicationId()));
					break;
				default:
					throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
				}

				crediCardsDetail.setModifiedBy(frameRequest.getUserId());
				crediCardsDetail.setModifiedDate(new Date());
				creditCardsDetailRepository.save(crediCardsDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save crediCardsDetail  :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public List<CreditCardsDetailRequest> getCreditCardDetailList(Long id, int applicationType) throws LoansException {

		List<CreditCardsDetail> creditCardsDetails;
		switch (applicationType) {
		case CommonUtils.ApplicantType.APPLICANT:
			creditCardsDetails = creditCardsDetailRepository.listCreditCardsFromAppId(id);
			break;
		case CommonUtils.ApplicantType.COAPPLICANT:
			creditCardsDetails = creditCardsDetailRepository.listCreditCardsFromCoAppId(id);
			break;
		case CommonUtils.ApplicantType.GARRANTOR:
			creditCardsDetails = creditCardsDetailRepository.listCreditCardsFromGarrId(id);
			break;
		default:
			throw new LoansException("Applcation Type Not Found in getCreditCardDetailList ");
		}

		List<CreditCardsDetailRequest> creditCardsRequests = new ArrayList<CreditCardsDetailRequest>();

		for (CreditCardsDetail detail : creditCardsDetails) {
			CreditCardsDetailRequest creditCardsRequest = new CreditCardsDetailRequest();
			BeanUtils.copyProperties(detail, creditCardsRequest);
			creditCardsRequests.add(creditCardsRequest);
		}
		return creditCardsRequests;
	}

	@Override
	public Boolean saveOrUpdateFromCibil(List<CreditCardsDetailRequest> creditCardDetail, Long applicationId,
			Long userId, int applicantType) throws LoansException {
		try {
			// Inactive Previous Loans Before Adding new
			CoApplicantDetail coApplicant = null;
			GuarantorDetails guarantor = null;
			switch (applicantType) {
				case CommonUtils.ApplicantType.APPLICANT:
					creditCardsDetailRepository.inactive(applicationId);
					break;
				case CommonUtils.ApplicantType.COAPPLICANT:
					creditCardsDetailRepository.inactiveByCoApplicant(applicationId);
					coApplicant = coApplicantDetailRepository.findOne(applicationId);
					break;
				case CommonUtils.ApplicantType.GARRANTOR:
					creditCardsDetailRepository.inactiveByGuarantor(applicationId);
					guarantor = guarantorDetailsRepository.findOne(applicationId);
					break;
				default:
					throw new LoansException("Invalid Applicant Type Accept : 1 2 and 3");
			}

			for (CreditCardsDetailRequest request : creditCardDetail) {
				CreditCardsDetail crediCardsDetail = new CreditCardsDetail();
				BeanUtils.copyProperties(request, crediCardsDetail);
				if (request.getId() == null) {
					crediCardsDetail.setCreatedBy(userId);
					crediCardsDetail.setCreatedDate(new Date());
				}
				switch (applicantType) {
				case CommonUtils.ApplicantType.APPLICANT:
					crediCardsDetail.setApplicantionId(new LoanApplicationMaster(applicationId));
					break;
				case CommonUtils.ApplicantType.COAPPLICANT:
					crediCardsDetail.setCoApplicantDetailId(coApplicant);
					crediCardsDetail.setCoApplicantId(applicationId);
					break;
				case CommonUtils.ApplicantType.GARRANTOR:
					crediCardsDetail.setGuarantorDetailId(guarantor);
					break;
				default:
					throw new LoansException("Invalid Applicant Type Accept : 1 2 and 3");
				}
				crediCardsDetail.setIsActive(true);
				crediCardsDetail.setModifiedBy(userId);
				crediCardsDetail.setModifiedDate(new Date());
				creditCardsDetailRepository.save(crediCardsDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception  in save CreditCard from CIBIL :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

}
