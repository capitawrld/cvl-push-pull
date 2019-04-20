package com.capitaworld.service.loans.service.fundseeker.retail.impl;

import java.util.Date;

import com.capitaworld.service.loans.exceptions.LoansException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.FinalHomeLoanDetail;
import com.capitaworld.service.loans.model.retail.FinalHomeLoanDetailRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.FinalHomeLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.service.fundseeker.retail.FinalHomeLoanService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@Service
@Transactional
public class FinalHomeLoanServiceImpl implements FinalHomeLoanService {

	private static final Logger logger = LoggerFactory.getLogger(FinalHomeLoanServiceImpl.class);

	@Autowired
	private FinalHomeLoanDetailRepository finalHomeLoanDetailRepository;
	
	@Autowired
	private RetailApplicantDetailRepository retailApplicantDetailRepository;
	
	@Autowired
	private LoanApplicationRepository loanApplicationRepository;
	
	@Autowired
	private LoanApplicationService loanApplicationService;

	@Override
	public boolean saveOrUpdate(FinalHomeLoanDetailRequest finalHomeLoanDetailRequest, Long userId) throws LoansException {
		try {
			Long finalUserId = (CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetailRequest.getClientId()) ? userId : finalHomeLoanDetailRequest.getClientId());
			FinalHomeLoanDetail finalHomeLoanDetail = finalHomeLoanDetailRepository
					.getByApplicationAndUserId(finalHomeLoanDetailRequest.getApplicationId(), finalUserId);
			if (finalHomeLoanDetail == null) {
				finalHomeLoanDetail = new FinalHomeLoanDetail();
				finalHomeLoanDetail.setCreatedBy(userId);
				finalHomeLoanDetail.setCreatedDate(new Date());
				finalHomeLoanDetail.setIsActive(true);
				finalHomeLoanDetail
						.setApplicationId(new LoanApplicationMaster(finalHomeLoanDetailRequest.getApplicationId()));
			} else {
				finalHomeLoanDetail.setModifiedBy(userId);
				finalHomeLoanDetail.setModifiedDate(new Date());
			}
			String[] corporate = new String[CommonUtils.IgnorableCopy.getCORPORATE().length + 1];
			corporate[CommonUtils.IgnorableCopy.getCORPORATE().length] = CommonUtils.IgnorableCopy.ID;
			BeanUtils.copyProperties(finalHomeLoanDetailRequest, finalHomeLoanDetail,corporate);
			finalHomeLoanDetail = finalHomeLoanDetailRepository.save(finalHomeLoanDetail);

			if (finalHomeLoanDetail != null){
				logger.info("finalHomeLoanDetail is saved successfully");
			}

			//setting Flag to DB
			if(!CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetailRequest.getIsFinalInformationFilled())){
//				we are reusing this method and also same column in loanApplication master. it is actually using Corporate. 
				loanApplicationRepository.setIsFinalMcqMandatoryFilled(finalHomeLoanDetailRequest.getApplicationId(), finalUserId, finalHomeLoanDetailRequest.getIsFinalInformationFilled());
			}
			
			//Update Bowl Count Flag
			loanApplicationRepository.setFinalFilledCount(finalHomeLoanDetailRequest.getApplicationId(), finalUserId,finalHomeLoanDetailRequest.getFinalFilledCount());
			return true;
		} catch (Exception e) {
			logger.error("Error while Saving Final Home Loan Details:-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public FinalHomeLoanDetailRequest get(Long applicationId, Long userId) throws LoansException {
		try {
			FinalHomeLoanDetail loanDetail = finalHomeLoanDetailRepository.getByApplicationAndUserId(applicationId,
					userId);
			FinalHomeLoanDetailRequest finalHomeLoanDetailRequest = new FinalHomeLoanDetailRequest();
			if (loanDetail == null) {
				Integer currencyId = retailApplicantDetailRepository.getCurrency(userId, applicationId);
				JSONObject bowlCount = loanApplicationService.getBowlCount(applicationId, userId);
				finalHomeLoanDetailRequest.setCurrencyValue(CommonDocumentUtils.getCurrency(currencyId));
				if(!CommonUtils.isObjectNullOrEmpty(bowlCount.get("finalFilledCount"))){
					finalHomeLoanDetailRequest.setFinalFilledCount(bowlCount.get("finalFilledCount").toString());	
				}
				return finalHomeLoanDetailRequest;
			}
			BeanUtils.copyProperties(loanDetail, finalHomeLoanDetailRequest);
			Integer currencyId = retailApplicantDetailRepository.getCurrency(userId, applicationId);
			finalHomeLoanDetailRequest.setCurrencyValue(CommonDocumentUtils.getCurrency(currencyId));
			finalHomeLoanDetailRequest.setFinalFilledCount(loanDetail.getApplicationId().getFinalFilledCount());
			return finalHomeLoanDetailRequest;
		} catch (Exception e) {
			logger.error("Error while getting Final Home Loan Details:-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

}
