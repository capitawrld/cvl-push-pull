package com.capitaworld.service.loans.service.fundseeker.retail.impl;

import java.util.Date;
import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.CoApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.RetailApplicantDetail;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.Address;
import com.capitaworld.service.loans.model.common.AddressRequest;
import com.capitaworld.service.loans.model.common.CibilFullFillOfferRequest;
import com.capitaworld.service.loans.model.retail.CoApplicantRequest;
import com.capitaworld.service.loans.model.retail.FinalCommonRetailRequestOld;
import com.capitaworld.service.loans.model.retail.GuarantorRequest;
import com.capitaworld.service.loans.model.retail.RetailApplicantRequest;
import com.capitaworld.service.loans.model.retail.RetailITRManualResponse;
import com.capitaworld.service.loans.repository.common.LoanRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.CoApplicantDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantIncomeService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.Gender;
import com.capitaworld.service.oneform.enums.Title;

@Service
@Transactional
public class RetailApplicantServiceImpl implements RetailApplicantService {

	private static final Logger logger = LoggerFactory.getLogger(RetailApplicantServiceImpl.class.getName());

	private static final String ERROR_WHILE_SAVING_RETAIL_PROFILE_MSG = "Error while Saving Retail Profile :- ";

	@Autowired
	private RetailApplicantDetailRepository applicantRepository;
	
	
	@Autowired
	private RetailApplicantIncomeService applicantIncomeService;

	@Autowired
	private CoApplicantService coApplicantService;

	@Autowired
	private GuarantorService guarantorService;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private Environment environment;
	
	@Autowired
	private CoApplicantDetailRepository coApplicantDetailRepository;
	
	@Autowired
	private LoanRepository loanRepository;
	
	private static final String SIDBI_AMOUNT = "com.capitaworld.sidbi.amount";

	@Override
	public boolean save(RetailApplicantRequest applicantRequest, Long userId) throws LoansException {

		try {
			Long finalUserId = (CommonUtils.isObjectNullOrEmpty(applicantRequest.getClientId()) ? userId
					: applicantRequest.getClientId());
			RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(finalUserId,
					applicantRequest.getApplicationId());
			if (applicantDetail != null) {
				applicantDetail.setModifiedBy(userId);
				applicantDetail.setModifiedDate(new Date());
			} else {
				applicantDetail = new RetailApplicantDetail();
				applicantDetail.setCreatedBy(userId);
				applicantDetail.setCreatedDate(new Date());
				applicantDetail.setIsActive(true);
				applicantDetail.setApplicationId(new LoanApplicationMaster(applicantRequest.getApplicationId()));
			}

			BeanUtils.copyProperties(applicantRequest, applicantDetail, CommonUtils.IgnorableCopy.getRetailFinal());
			copyAddressFromRequestToDomain(applicantRequest, applicantDetail);
			if (applicantRequest.getDate() != null && applicantRequest.getMonth() != null
					&& applicantRequest.getYear() != null) {
				Date birthDate = CommonUtils.getDateByDateMonthYear(applicantRequest.getDate(),
						applicantRequest.getMonth(), applicantRequest.getYear());
				applicantDetail.setBirthDate(birthDate);
			}
			if (applicantRequest.getQualifyingMonth() != null && applicantRequest.getQualifyingYear() != null) {
				Date qualifyingYear = CommonUtils.getDateByDateMonthYear(1,applicantRequest.getQualifyingMonth(), applicantRequest.getQualifyingYear());
				applicantDetail.setQualifyingYear(qualifyingYear);
			}
			if (applicantRequest.getBusinessStartMonth() != null && applicantRequest.getBusinessStartYear() != null) {
				Date businessStartDate = CommonUtils.getDateByDateMonthYear(1,applicantRequest.getBusinessStartMonth(), applicantRequest.getBusinessStartYear());
				applicantDetail.setBusinessStartDate(businessStartDate);
			}
			applicantDetail = applicantRepository.save(applicantDetail);

			if (applicantDetail != null){
				logger.info("applicantDetail is saved successfully");
			}

			for (CoApplicantRequest request : applicantRequest.getCoApplicants()) {
				coApplicantService.save(request, applicantRequest.getApplicationId(), finalUserId);
			}
			for (GuarantorRequest request : applicantRequest.getGuarantors()) {
				guarantorService.save(request, applicantRequest.getApplicationId(), finalUserId);
			}

			// Updating Flag
			loanApplicationRepository.setIsApplicantProfileMandatoryFilled(applicantRequest.getApplicationId(),
					finalUserId, applicantRequest.getIsApplicantDetailsFilled());

			// Updating Bowl Count
			loanApplicationRepository.setProfileFilledCount(applicantRequest.getApplicationId(), finalUserId,
					applicantRequest.getDetailsFilledCount());

			return true;

		} catch (Exception e) {
			logger.error(ERROR_WHILE_SAVING_RETAIL_PROFILE_MSG,e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	
	@Override
	public boolean saveITRResponse(RetailApplicantRequest applicantRequest) throws LoansException {
		try {
			RetailApplicantDetail applicantDetail = applicantRepository.findByApplicationId(applicantRequest.getApplicationId());
			if (applicantDetail != null) {
				applicantDetail.setModifiedBy(applicantRequest.getUserId());
				applicantDetail.setModifiedDate(new Date());
			} else {
				applicantDetail = new RetailApplicantDetail();
				applicantDetail.setCreatedBy(applicantRequest.getUserId());
				applicantDetail.setCreatedDate(new Date());
				applicantDetail.setIsActive(true);
				applicantDetail.setApplicationId(new LoanApplicationMaster(applicantRequest.getApplicationId()));
			}
			BeanUtils.copyProperties(applicantRequest, applicantDetail,CommonUtils.IgnorableCopy.getRetailFinalWithId());
			applicantDetail.setEmail(applicantRequest.getEmail());
			applicantDetail.setMobile(applicantRequest.getLanLineNo());
			Address address = applicantRequest.getFirstAddress();
			if(!CommonUtils.isObjectNullOrEmpty(address)) {
				applicantDetail.setAddressPremiseName(address.getPremiseNumber());
				applicantDetail.setAddressLandmark(address.getLandMark());
				applicantDetail.setAddressStreetName(address.getStreetName());
				applicantDetail.setAddressCountry(address.getCountryId());
				applicantDetail.setAddressState(!CommonUtils.isObjectNullOrEmpty(address.getStateId()) ? address.getStateId().longValue() : null);
				applicantDetail.setAddressCity(address.getCityId());
				applicantDetail.setAddressPincode(address.getPincode());
				applicantDetail.setAddressDistrictMappingId(address.getDistrictMappingId());
			}
			applicantDetail.setBirthDate(applicantRequest.getDob());
			applicantDetail = applicantRepository.save(applicantDetail);

			if (applicantDetail != null){
				logger.info("applicantDetail is saved successfully");
			}

			//SAVE INCOME DETAILS
			applicantIncomeService.saveAll(applicantRequest.getIncomeDetailsList());
			return true;
		} catch (Exception e) {
			logger.error(ERROR_WHILE_SAVING_RETAIL_PROFILE_MSG,e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getCoapAndGuarIds(Long userId, Long applicationId) throws LoansException {
		try {
			List<Long> coAppIds = coApplicantService.getCoAppIds(userId, applicationId);
			List<Long> guarantorIds = guarantorService.getGuarantorIds(userId, applicationId);
			JSONObject obj = new JSONObject();
			obj.put("coAppIds", coAppIds);
			obj.put("guarantorIds", guarantorIds);
			return obj;
		} catch (Exception e) {
			logger.error("Error while getCoapAndGuarIds:-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public RetailApplicantRequest get(Long applicationId) throws LoansException {
		try {
			RetailApplicantDetail applicantDetail = applicantRepository.findByApplicationId(applicationId);
			Integer productId = loanApplicationRepository.getProductIdByApplicationId(applicationId);
			RetailApplicantRequest applicantRequest = new RetailApplicantRequest();
			BeanUtils.copyProperties(applicantDetail, applicantRequest);
			copyAddressFromDomainToRequest(applicantDetail, applicantRequest);
			Integer[] saperatedTime = CommonUtils.saperateDayMonthYearFromDate(applicantDetail.getBirthDate());
			applicantRequest.setDob(applicantDetail.getBirthDate());
			applicantRequest.setDate(saperatedTime[0]);
			applicantRequest.setMonth(saperatedTime[1]);
			applicantRequest.setYear(saperatedTime[2]);
			applicantRequest.setGrossIncome(applicantDetail.getGrossMonthlyIncome());
			if(applicantDetail.getQualifyingYear() != null){
				Integer[] saperatedQualifyingYear = CommonUtils.saperateDayMonthYearFromDate(applicantDetail.getQualifyingYear());
				applicantRequest.setQualifyingMonth(saperatedQualifyingYear[1]);
				applicantRequest.setQualifyingYear(saperatedQualifyingYear[2]);
			}
			if(applicantDetail.getBusinessStartDate() != null){
				Integer[] saperatedBusinessStartDate = CommonUtils.saperateDayMonthYearFromDate(applicantDetail.getBusinessStartDate());
				applicantRequest.setBusinessStartMonth(saperatedBusinessStartDate[1]);
				applicantRequest.setBusinessStartYear(saperatedBusinessStartDate[2]);
			}			
			applicantRequest.setDetailsFilledCount(applicantDetail.getApplicationId().getDetailsFilledCount());
			applicantRequest.setProductId(productId);
			return applicantRequest;
		} catch (Exception e) {
			logger.error("Error while Getting Retail applicant details:-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public FinalCommonRetailRequestOld getFinal(Long id, Long applicationId) throws LoansException {
		try {
			RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(id, applicationId);
			if (applicantDetail == null) {
				throw new NullPointerException("RetailApplicantDetail Record of Final Portion not exists in DB of ID : "
						+ id + "  ApplicationId==>" + applicationId);
			}
			FinalCommonRetailRequestOld applicantRequest = new FinalCommonRetailRequestOld();
			BeanUtils.copyProperties(applicantDetail, applicantRequest, CommonUtils.IgnorableCopy.getRetailProfile());
			applicantRequest.setCurrencyValue(CommonDocumentUtils.getCurrency(applicantDetail.getCurrencyId()));
			applicantRequest.setFinalFilledCount(applicantDetail.getApplicationId().getFinalFilledCount());
			return applicantRequest;
		} catch (Exception e) {
			logger.error(ERROR_WHILE_SAVING_RETAIL_PROFILE_MSG,e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public boolean saveFinal(FinalCommonRetailRequestOld applicantRequest, Long userId) throws LoansException {
		try {
			if (applicantRequest.getApplicationId() == null) {
				throw new NullPointerException("Application Id and ID(Primary Key) must not be null=>Application ID==>"
						+ applicantRequest.getApplicationId() + " User Id (Primary Key)==>" + userId);
			}
			Long finaluserId = (CommonUtils.isObjectNullOrEmpty(applicantRequest.getClientId()) ? userId
					: applicantRequest.getClientId());
			RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(finaluserId,
					applicantRequest.getApplicationId());
			if (applicantDetail == null) {
				throw new NullPointerException(
						"Applicant ID and ID(Primary Key) does not match with the database==> Applicant ID==>"
								+ applicantRequest.getApplicationId() + "User ID==>" + userId);
			}
			applicantDetail.setModifiedBy(userId);
			applicantDetail.setModifiedDate(new Date());
			BeanUtils.copyProperties(applicantRequest, applicantDetail, CommonUtils.IgnorableCopy.getRetailProfile());
			applicantRepository.save(applicantDetail);
			// Updating Final Flag
			loanApplicationRepository.setIsApplicantFinalMandatoryFilled(applicantRequest.getApplicationId(),
					finaluserId, applicantRequest.getIsApplicantFinalFilled());
			// Updating Final Count
			loanApplicationRepository.setFinalFilledCount(applicantRequest.getApplicationId(), finaluserId,
					applicantRequest.getFinalFilledCount());
			return true;
		} catch (Exception e) {
			logger.error(ERROR_WHILE_SAVING_RETAIL_PROFILE_MSG,e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<CoApplicantRequest> getCoApplicants(Long userId, Long applicationId) throws LoansException {
		return coApplicantService.getList(applicationId, userId);
	}

	@Override
	public Integer getCurrency(Long applicationId, Long userId) throws LoansException {
		try {
			return applicantRepository.getCurrency(userId, applicationId);
		} catch (Exception e) {
			logger.error("Error while Getting Currency:-",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<GuarantorRequest> getGuarantors(Long userId, Long applicationId) throws LoansException {
		return guarantorService.getList(applicationId, userId);
	}

	@Override
	public CibilFullFillOfferRequest getProfile(Long userId, Long applicationId) throws LoansException {
		try {
			logger.info("start getProfile() method");
			RetailApplicantDetail applicantDetail = null;
			if(userId == null || userId <= 0){
				applicantDetail = applicantRepository.findByApplicationId(applicationId);
			}else{
				applicantDetail = applicantRepository.getByApplicationAndUserId(userId,applicationId);
			}


			if (applicantDetail == null) {
				return null;
			}
			CibilFullFillOfferRequest cibilFullFillOfferRequest = new CibilFullFillOfferRequest();
			cibilFullFillOfferRequest.setPan(applicantDetail.getPan());
			cibilFullFillOfferRequest.setAdhaar(applicantDetail.getAadharNumber());
			AddressRequest address = new AddressRequest();
			address.setAddressType("01"); // PermenantType
			if(CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentStateId())){
				address.setStreetAddress(applicantDetail.getAddressStreetName());
				address.setPremiseNo(applicantDetail.getAddressPremiseName());
				address.setLandMark(applicantDetail.getAddressLandmark());
				address.setCity(CommonDocumentUtils.getCity(applicantDetail.getAddressCity(), oneFormClient));
				if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAddressPincode())) {
					address.setPostalCode(applicantDetail.getAddressPincode().toString());
				}
				if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAddressState())) {
					address.setRegion(CommonDocumentUtils.getStateCode(applicantDetail.getAddressState().longValue(),
							oneFormClient));
				}
			}else{
				address.setStreetAddress(applicantDetail.getPermanentStreetName());
				address.setPremiseNo(applicantDetail.getPermanentPremiseNumberName());
				address.setLandMark(applicantDetail.getPermanentLandMark());
				address.setCity(CommonDocumentUtils.getCity(applicantDetail.getPermanentCityId(), oneFormClient));
				if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentPincode())) {
					address.setPostalCode(applicantDetail.getPermanentPincode().toString());
				}
				if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentStateId())) {
					address.setRegion(CommonDocumentUtils.getStateCode(applicantDetail.getPermanentStateId().longValue(),
							oneFormClient));
				}	
			}
			
			cibilFullFillOfferRequest.setAddress(address);
			cibilFullFillOfferRequest.setDateOfBirth(applicantDetail.getBirthDate());
			cibilFullFillOfferRequest.setForName(applicantDetail.getFirstName());
			cibilFullFillOfferRequest.setSurName(applicantDetail.getLastName());
			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTitleId())) {
				cibilFullFillOfferRequest.setTitle(Title.getById(applicantDetail.getTitleId()).getValue());
			}
			cibilFullFillOfferRequest.setPhoneNumber(applicantDetail.getContactNo());
			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getGenderId())) {
				cibilFullFillOfferRequest.setGender(Gender.getById(applicantDetail.getGenderId()).getValue());
			}
			// Email ID
/*			UserResponse userResponse = usersClient.getEmailMobile(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				@SuppressWarnings("unchecked")
				UsersRequest request = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class); */
				cibilFullFillOfferRequest.setEmail(applicantDetail.getEmail());
				cibilFullFillOfferRequest.setPhoneNumber(applicantDetail.getMobile());
//			}
			logger.info("End getProfile() method with Success Execution");
			return cibilFullFillOfferRequest;
		} catch (Exception e) {
			logger.error("Error while getting Basic profile for CIBIL : ",e);
			logger.info("End getProfile() method with FAILURE Execution");
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	public static void copyAddressFromRequestToDomain(RetailApplicantRequest from, RetailApplicantDetail to) {
		if (from.getFirstAddress() != null) {
			to.setPermanentPremiseNumberName(from.getFirstAddress().getPremiseNumber());
			to.setPermanentStreetName(from.getFirstAddress().getStreetName());
			to.setPermanentLandMark(from.getFirstAddress().getLandMark());
			to.setPermanentCityId(from.getFirstAddress().getCityId());
			to.setPermanentStateId(from.getFirstAddress().getStateId());
			to.setPermanentCountryId(from.getFirstAddress().getCountryId());
			to.setPermanentPincode(from.getFirstAddress().getPincode());
		}

		if (from.getAddressSameAs()) {
			if (from.getFirstAddress() != null) {
				to.setOfficePremiseNumberName(from.getFirstAddress().getPremiseNumber());
				to.setOfficeStreetName(from.getFirstAddress().getStreetName());
				to.setOfficeLandMark(from.getFirstAddress().getLandMark());
				to.setOfficeCityId(from.getFirstAddress().getCityId());
				to.setOfficeStateId(from.getFirstAddress().getStateId());
				to.setOfficeCountryId(from.getFirstAddress().getCountryId());
				to.setOfficePincode(from.getFirstAddress().getPincode());
			}
		} else {
			if (from.getSecondAddress() != null) {
				to.setOfficePremiseNumberName(from.getSecondAddress().getPremiseNumber());
				to.setOfficeStreetName(from.getSecondAddress().getStreetName());
				to.setOfficeLandMark(from.getSecondAddress().getLandMark());
				to.setOfficeCityId(from.getSecondAddress().getCityId());
				to.setOfficeStateId(from.getSecondAddress().getStateId());
				to.setOfficeCountryId(from.getSecondAddress().getCountryId());
				to.setOfficePincode(from.getSecondAddress().getPincode());
			}
		}

	}

	public static void copyAddressFromDomainToRequest(RetailApplicantDetail from, RetailApplicantRequest to) {
		Address address = new Address();
		address.setPremiseNumber(from.getPermanentPremiseNumberName());
		address.setLandMark(from.getPermanentLandMark());
		address.setStreetName(from.getPermanentStreetName());
		address.setCityId(from.getPermanentCityId());
		address.setStateId(from.getPermanentStateId());
		address.setCountryId(from.getPermanentCountryId());
		address.setPincode(from.getPermanentPincode());
		to.setFirstAddress(address);
		if (!CommonUtils.isObjectNullOrEmpty(from.getAddressSameAs())) {
			if (from.getAddressSameAs()) {
				to.setSecondAddress(address);
			} else {
				address = new Address();
				address.setPremiseNumber(from.getOfficePremiseNumberName());
				address.setLandMark(from.getOfficeLandMark());
				address.setStreetName(from.getOfficeStreetName());
				address.setCityId(from.getOfficeCityId());
				address.setStateId(from.getOfficeStateId());
				address.setCountryId(from.getOfficeCountryId());
				address.setPincode(from.getOfficePincode());
				to.setSecondAddress(address);
			}
		}
	}
	
	@Override
	public JSONObject getNameAndPanByAppId(Long applicationId) {
		JSONObject obj = new JSONObject();
		RetailApplicantDetail applicantDetail = applicantRepository.findByApplicationId(applicationId);
		if(!CommonUtils.isObjectNullOrEmpty(applicantDetail)) {
			obj.put("name", applicantDetail.getFirstName() + " " + applicantDetail.getMiddleName() + " " + applicantDetail.getLastName());
			obj.put("pan", applicantDetail.getPan());
			obj.put("amount", environment.getProperty(SIDBI_AMOUNT));
		}
		return obj;
	}
	
	public RetailITRManualResponse getITRManualFormData(Long applicationId,Long coAppId,Long userId) {
		
		if(!CommonUtils.isObjectNullOrEmpty(coAppId)) {
			CoApplicantDetail applicantDetail = coApplicantDetailRepository.findByIdAndIsActive(coAppId, true);
			if(!CommonUtils.isObjectNullOrEmpty(applicantDetail)) {
				RetailITRManualResponse res = new RetailITRManualResponse();
				BeanUtils.copyProperties(applicantDetail, res);
				res.setEmail(applicantDetail.getEmail());
				if(CommonUtils.isObjectNullOrEmpty(applicantDetail.getMobile())) {
					res.setTelephone(loanRepository.getMobileNumberByUserId(userId));					
				} else {
					res.setTelephone(applicantDetail.getMobile());
				}
				res.setPremiseNo(applicantDetail.getAddressPremiseName());
				res.setLandmark(applicantDetail.getAddressLandmark());
				res.setStreetName(applicantDetail.getAddressStreetName());
				res.setCountryId(applicantDetail.getAddressCountry());
				res.setStateId(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAddressState()) ? applicantDetail.getAddressState().intValue() : null);
				res.setCityId(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAddressCity()) ? applicantDetail.getAddressCity().longValue() : null);
				res.setPincode(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAddressPincode()) ? applicantDetail.getAddressPincode().longValue() : null);
				res.setDistId(applicantDetail.getAddressDistrictMappingId());
				res.setDob(applicantDetail.getBirthDate());
				return res;
			}
		} else {
			RetailApplicantDetail applicantDetail = applicantRepository.findByApplicationId(applicationId);
			if(!CommonUtils.isObjectNullOrEmpty(applicantDetail)) {
				RetailITRManualResponse res = new RetailITRManualResponse();
				BeanUtils.copyProperties(applicantDetail, res);
				res.setEmail(applicantDetail.getEmail());
				if(CommonUtils.isObjectNullOrEmpty(applicantDetail.getMobile())) {
					res.setTelephone(loanRepository.getMobileNumberByUserId(userId));					
				} else {
					res.setTelephone(applicantDetail.getMobile());
				}
				res.setPremiseNo(applicantDetail.getAddressPremiseName());
				res.setLandmark(applicantDetail.getAddressLandmark());
				res.setStreetName(applicantDetail.getAddressStreetName());
				res.setCountryId(applicantDetail.getAddressCountry());
				res.setStateId(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAddressState()) ? applicantDetail.getAddressState().intValue() : null);
				res.setCityId(applicantDetail.getAddressCity());
				res.setPincode(applicantDetail.getAddressPincode());
				res.setDistId(applicantDetail.getAddressDistrictMappingId());
				res.setDob(applicantDetail.getBirthDate());
				return res;
			}
		}
		return null;
	}

}
