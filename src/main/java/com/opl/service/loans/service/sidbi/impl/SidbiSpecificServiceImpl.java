package com.opl.service.loans.service.sidbi.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.common.CommonUtils;
import com.opl.mudra.api.common.MultipleJSONObjectHelper;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.FinancialArrangementsDetailRequest;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.model.corporate.TotalCostOfProjectRequest;
import com.opl.mudra.api.loans.model.sidbi.RawMaterialDetailsRequest;
import com.opl.mudra.api.loans.model.sidbi.SidbiBasicDetailRequest;
import com.opl.mudra.api.sidbi.enums.SidbiCurrencyRate;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.users.UsersClient;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.opl.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.opl.service.loans.domain.sidbi.FacilityDetails;
import com.opl.service.loans.domain.sidbi.SidbiBasicDetail;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.opl.service.loans.repository.sidbi.BasicDetailRepository;
import com.opl.service.loans.repository.sidbi.FacilityDetailsRepository;
import com.opl.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.opl.service.loans.service.sidbi.FacilityDetailsService;
import com.opl.service.loans.service.sidbi.MeansOfFinanceDetailService;
import com.opl.service.loans.service.sidbi.ProjectCostDetailService;
import com.opl.service.loans.service.sidbi.RawMaterialDetailsService;
import com.opl.service.loans.service.sidbi.SidbiSpecificService;

@Service
@Transactional
public class SidbiSpecificServiceImpl implements SidbiSpecificService {

	private static final Logger logger = LoggerFactory.getLogger(SidbiSpecificServiceImpl.class);
	
	@Autowired
	BasicDetailRepository basicDetailRepository;
	
	@Autowired
    CorporateApplicantDetailRepository corporateApplicantDetailRepository;
	
	@Autowired
	UsersClient usersClient;
	
	@Autowired
	private PrimaryCorporateDetailRepository primaryCorporateDetailRepository;
	
	@Autowired
    FinancialArrangementDetailsService financialArrangementDetailsService;
	
	@Autowired
    ProjectCostDetailService projectCostDetailService;
	
	@Autowired
    MeansOfFinanceDetailService meansOfFinanceDetailService;
	
	@Autowired
    FacilityDetailsService facilityDetailsService;
	
	@Autowired
    RawMaterialDetailsService rawMaterialDetailsService;
	
	@Autowired
    FacilityDetailsRepository facilityDetailsRepository;
	
	@Override
	public boolean saveOrUpdateAdditionalData(SidbiBasicDetailRequest sidbiBasicDetailRequest, Long userId) throws LoansException {

		try {
			SidbiBasicDetail sidbiBasicDetail = basicDetailRepository.getByApplicationAndUserId(userId, sidbiBasicDetailRequest.getApplicationId());
			
			if (sidbiBasicDetail == null) {
				sidbiBasicDetail = new SidbiBasicDetail();
				sidbiBasicDetail.setCreatedBy(userId);
				sidbiBasicDetail.setCreatedDate(new Date());
				sidbiBasicDetail.setIsActive(true);
			}
			
			BeanUtils.copyProperties(sidbiBasicDetailRequest, sidbiBasicDetail);

			sidbiBasicDetail.setModifiedBy(userId);
			sidbiBasicDetail.setModifiedDate(new Date());
			basicDetailRepository.save(sidbiBasicDetail);
			return true;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public SidbiBasicDetailRequest getAdditionalData(Long applicationId, Long userId) throws LoansException {
		
		SidbiBasicDetailRequest sidbiBasicDetailRequest = null;
		try {
			sidbiBasicDetailRequest = new SidbiBasicDetailRequest();
			SidbiBasicDetail sidbiBasicDetail = basicDetailRepository.getByApplicationAndUserId(userId, applicationId);
			if(sidbiBasicDetail == null) {
				
				sidbiBasicDetailRequest = setAutoFilledValue(applicationId,userId);
				
				
				sidbiBasicDetail = new SidbiBasicDetail();
				
				
				sidbiBasicDetailRequest.setId(null);								
				BeanUtils.copyProperties(sidbiBasicDetailRequest, sidbiBasicDetail);
				
				sidbiBasicDetail.setCreatedBy(userId);
				sidbiBasicDetail.setCreatedDate(new Date());
				sidbiBasicDetail.setIsActive(true);
				sidbiBasicDetail.setModifiedBy(userId);
				sidbiBasicDetail.setModifiedDate(new Date());
				sidbiBasicDetail.setAllAmountValuesIn(SidbiCurrencyRate.RUPEES_IN_LAKH.getId());
				sidbiBasicDetail.setIsLockedAllAmountValuesIn(true);
				sidbiBasicDetail.setApplicationId(applicationId);
				
				basicDetailRepository.save(sidbiBasicDetail);
				
				sidbiBasicDetailRequest = new SidbiBasicDetailRequest();
				BeanUtils.copyProperties(sidbiBasicDetail, sidbiBasicDetailRequest);
			}else {
				BeanUtils.copyProperties(sidbiBasicDetail, sidbiBasicDetailRequest);				
			}
			
			sidbiBasicDetailRequest.setLoanAmount(getLoanAmountByApplicationId(applicationId));			
			sidbiBasicDetailRequest.setLoanTypeId(primaryCorporateDetailRepository.getPurposeLoanId(applicationId));
			
		} catch (Exception e) {
			logger.error("Exception : ", e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
		
		return sidbiBasicDetailRequest;
	}
	
	public SidbiBasicDetailRequest setAutoFilledValue(Long applicationId, Long userId) {
		SidbiBasicDetailRequest sidbiBasicDetailRequest = null;
		try {
			CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository.getByApplicationIdAndIsAtive(applicationId);
			PrimaryCorporateDetail primaryCorpDetailObj = primaryCorporateDetailRepository.findOneByApplicationIdId(applicationId); 
			if(corporateApplicantDetail != null) {
				sidbiBasicDetailRequest = new SidbiBasicDetailRequest();
				BeanUtils.copyProperties(corporateApplicantDetail, sidbiBasicDetailRequest);
				if(primaryCorpDetailObj.getFactoryPremise()==4) {
					sidbiBasicDetailRequest.setFactoryPremise(3);
				}else {
					sidbiBasicDetailRequest.setFactoryPremise(primaryCorpDetailObj.getFactoryPremise());
				}
				sidbiBasicDetailRequest.setDateOfCommencementOfCommercialOperations(primaryCorpDetailObj.getCommercialOperationDate());
				sidbiBasicDetailRequest.setPremiseNumber(corporateApplicantDetail.getRegisteredPremiseNumber());
//				sidbiBasicDetailRequest.setIndustryId(corporateApplicantDetail.getKeyVericalFunding());
				sidbiBasicDetailRequest.setConstitutionId(corporateApplicantDetail.getConstitutionId());
				sidbiBasicDetailRequest.setStreetName(corporateApplicantDetail.getRegisteredStreetName());
				sidbiBasicDetailRequest.setLandMark(corporateApplicantDetail.getRegisteredLandMark());
				sidbiBasicDetailRequest.setPincode(corporateApplicantDetail.getRegisteredPincode());

				if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getMsmeRegistrationNumber()) && !CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAadhar())){

					sidbiBasicDetailRequest.setMsmeUamRegistrationNumber(corporateApplicantDetail.getMsmeRegistrationNumber());
				}
				else if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getMsmeRegistrationNumber()) && !CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAadhar())) {
					sidbiBasicDetailRequest.setMsmeUamRegistrationNumber(corporateApplicantDetail.getAadhar());
				}
				else if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getMsmeRegistrationNumber()) && CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAadhar())) {
					sidbiBasicDetailRequest.setMsmeUamRegistrationNumber(corporateApplicantDetail.getMsmeRegistrationNumber());
				}
				
				if(corporateApplicantDetail.getEstablishmentMonth()!=null && corporateApplicantDetail.getEstablishmentYear()!=null) {
					String str="01-"+corporateApplicantDetail.getEstablishmentMonth()+"-"+corporateApplicantDetail.getEstablishmentYear();
					Date date1=new SimpleDateFormat("dd-MM-yyyy").parse(str);  
					sidbiBasicDetailRequest.setEstablishmentDate(date1);
				}
				
			}
			UserResponse userResponse=usersClient.getUserBasicDetails(userId);
			if(!CommonUtils.isObjectNullOrEmpty(userResponse)) {
				UsersRequest usersRequest =MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if(!CommonUtils.isObjectNullOrEmpty(usersRequest)) {
					if(sidbiBasicDetailRequest==null) {
						sidbiBasicDetailRequest = new SidbiBasicDetailRequest();
					}
					sidbiBasicDetailRequest.setMobile(usersRequest.getMobile());
					sidbiBasicDetailRequest.setEmail(usersRequest.getEmail());
				}
			}
			
			
		} catch (Exception e) {
			logger.error("Exception while calling setAutoFilledValue :: ",e);
		}
		return sidbiBasicDetailRequest;
	}

	
	@Override
	public Double getLoanAmountByApplicationId(Long applicationId) throws LoansException {
		Double loanAmount=null;
		PrimaryCorporateDetail primaryCorpDetailObj = primaryCorporateDetailRepository.findOneByApplicationIdId(applicationId);
		
		if(primaryCorpDetailObj!=null) {
			if(primaryCorpDetailObj.getIsAllowSwitchExistingLender()!=null && primaryCorpDetailObj.getIsAllowSwitchExistingLender()) {
				if(primaryCorpDetailObj.getLoanAmount()==primaryCorpDetailObj.getAdditionalLoanAmount()) {
					loanAmount=primaryCorpDetailObj.getLoanAmount();
					FinancialArrangementsDetailRequest arrangementsDetailRequest = financialArrangementDetailsService.getTotalEmiAndSanctionAmountByApplicationId(applicationId);
					loanAmount+=arrangementsDetailRequest.getAmount();
				}else {
					loanAmount=primaryCorpDetailObj.getLoanAmount();
				}
	    	}else if(primaryCorpDetailObj.getAdditionalLoanAmount()!=null){
	    		loanAmount=primaryCorpDetailObj.getAdditionalLoanAmount();
	    	}else {
	    		loanAmount=primaryCorpDetailObj.getLoanAmount();
	    	}
		}
		return loanAmount;
	}

	@Override
	public LoansResponse validateSidbiForm(Long applicationId, Long userId) throws LoansException {
		Double totalAmt = 0.00;
		Double pTotalToBeIncurred = 0.00;
		Double pTotalAlreadyIncurred = 0.00;
		Double aTotalToBeIncurred = 0.00;
		Double aTotalAlreadyIncurred = 0.00;
		
		List<TotalCostOfProjectRequest> projectCostList = projectCostDetailService.getCostOfProjectDetailList(applicationId, userId);
		if(projectCostList == null || projectCostList.size() == 0) {
			return new LoansResponse("Please fill atleast one row in Project Cost Details", HttpStatus.INTERNAL_SERVER_ERROR.value(), "accCostOfProject");
		}else if(projectCostList != null) {
			totalAmt = 0.00;
			for(TotalCostOfProjectRequest projectCostDetail : projectCostList) {
				if(!"Plant & Machinery - For Manufacturing unit".contentEquals(projectCostDetail.getParticularName())) {
					totalAmt += projectCostDetail.getTotalCost() == null ? 0.00 : projectCostDetail.getTotalCost(); 
					pTotalToBeIncurred += projectCostDetail.getToBeIncurred() == null ? 0.00 : projectCostDetail.getToBeIncurred();
					pTotalAlreadyIncurred += projectCostDetail.getAlreadyIncurred() == null ? 0.00 : projectCostDetail.getAlreadyIncurred();				
				}
			}
			totalAmt=CommonUtils.convertTwoDecimal(totalAmt);
			pTotalToBeIncurred=CommonUtils.convertTwoDecimal(pTotalToBeIncurred);
			pTotalAlreadyIncurred=CommonUtils.convertTwoDecimal(pTotalAlreadyIncurred);
			if(totalAmt == null || totalAmt == 0.00) {
				return new LoansResponse("Please fill atleast one row in Project Cost Details", HttpStatus.INTERNAL_SERVER_ERROR.value(), "accCostOfProject");
			}
		}
		
		List<TotalCostOfProjectRequest> meansOfFinanceList = meansOfFinanceDetailService.getMeansOfFinanceList(applicationId, userId);
		
		if(meansOfFinanceList == null) {
			return new LoansResponse("Please fill atleast one row in Means of Finance Details", HttpStatus.INTERNAL_SERVER_ERROR.value(), "accCostOfProject");
		}else if(meansOfFinanceList != null){
			totalAmt = 0.00;
			for(TotalCostOfProjectRequest meansOfFinanceDetail : meansOfFinanceList) {
				totalAmt += meansOfFinanceDetail.getTotalCost() == null ? 0.00 : meansOfFinanceDetail.getTotalCost(); 
				aTotalToBeIncurred += meansOfFinanceDetail.getToBeIncurred() == null ? 0.00 : meansOfFinanceDetail.getToBeIncurred();
				aTotalAlreadyIncurred +=  meansOfFinanceDetail.getAlreadyIncurred() == null ? 0.00 : meansOfFinanceDetail.getAlreadyIncurred();
			}
			
			totalAmt=CommonUtils.convertTwoDecimal(totalAmt);
			aTotalToBeIncurred=CommonUtils.convertTwoDecimal(aTotalToBeIncurred);
			aTotalAlreadyIncurred=CommonUtils.convertTwoDecimal(aTotalAlreadyIncurred);
			if(totalAmt == null || totalAmt == 0.00) {
				return new LoansResponse("Please fill atleast one row in Means of Finance Details", HttpStatus.INTERNAL_SERVER_ERROR.value(), "accCostOfProject");
			}
		}
		
		if(pTotalToBeIncurred != aTotalToBeIncurred || pTotalAlreadyIncurred != aTotalAlreadyIncurred) {
			if(! pTotalAlreadyIncurred.equals(aTotalAlreadyIncurred)){
				return new LoansResponse("Please match total of Already Incurred of project cost detail data with Means of finance details", HttpStatus.INTERNAL_SERVER_ERROR.value(), "accCostOfProject");
			}else if(! pTotalToBeIncurred.equals(aTotalToBeIncurred)){
				return new LoansResponse("Please match total of To Be Incurred of project cost detail data with Means of finance details", HttpStatus.INTERNAL_SERVER_ERROR.value(), "accCostOfProject");
			}
		}
		
		List<FacilityDetails> facilityDetailsList = facilityDetailsRepository.getFacilityDetailsListAppId(applicationId);
		if(!CommonUtils.isObjectNullOrEmpty(facilityDetailsList) && facilityDetailsList.size() == 0){
			return new LoansResponse("Please fill atleast one row in Facility Details", HttpStatus.INTERNAL_SERVER_ERROR.value(), "accPropFacilities");
		}
		
		Integer loanType = primaryCorporateDetailRepository.getPurposeLoanId(applicationId);
		if(loanType==2) {
			List<RawMaterialDetailsRequest> rawMaterialDetailsRequests = rawMaterialDetailsService.getRawMaterialDetailsListAppId(applicationId);
			if(rawMaterialDetailsRequests == null || rawMaterialDetailsRequests.size() == 0) {
				return new LoansResponse("Please fill atleast one row in Details of Raw material components", HttpStatus.INTERNAL_SERVER_ERROR.value(), "accPropFacilities");
			}
		}
		
		
		return null;
	}

	
	@Override
	public SidbiCurrencyRate getValuesIn(Long applicationId) throws LoansException {
		SidbiBasicDetailRequest sidbiBasicDetailRequest = this.getSidbiBasicDetailByAppId(applicationId);
		return SidbiCurrencyRate.getById(sidbiBasicDetailRequest.getAllAmountValuesIn());
	}

	@Override
	public SidbiBasicDetailRequest getSidbiBasicDetailByAppId(Long applicationId) throws LoansException {
		SidbiBasicDetailRequest sidbiBasicDetailRequest = null;
		try {
			sidbiBasicDetailRequest = new SidbiBasicDetailRequest();
			SidbiBasicDetail sidbiBasicDetail = basicDetailRepository.getSidbiBasicDetailByAppId(applicationId);
			if(sidbiBasicDetail != null) {
				BeanUtils.copyProperties(sidbiBasicDetail, sidbiBasicDetailRequest);
			}
			
		} catch (Exception e) {
			logger.error("Exception : ", e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
		
		return sidbiBasicDetailRequest;
	}

	
}
