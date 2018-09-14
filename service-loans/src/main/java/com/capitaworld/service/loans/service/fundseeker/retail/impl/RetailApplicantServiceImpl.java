package com.capitaworld.service.loans.service.fundseeker.retail.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
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
import com.capitaworld.service.loans.domain.fundseeker.retail.RetailApplicantDetail;
import com.capitaworld.service.loans.model.Address;
import com.capitaworld.service.loans.model.common.AddressRequest;
import com.capitaworld.service.loans.model.common.CibilFullFillOfferRequest;
import com.capitaworld.service.loans.model.retail.CoApplicantRequest;
import com.capitaworld.service.loans.model.retail.FinalCommonRetailRequest;
import com.capitaworld.service.loans.model.retail.GuarantorRequest;
import com.capitaworld.service.loans.model.retail.RetailApplicantRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantIncomeService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.Gender;
import com.capitaworld.service.oneform.enums.Title;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;

@Service
@Transactional
public class RetailApplicantServiceImpl implements RetailApplicantService {

	private static final Logger logger = LoggerFactory.getLogger(RetailApplicantServiceImpl.class.getName());

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
	private UsersClient usersClient;
	
	@Autowired
	private Environment environment;
	
	private static final String SIDBI_AMOUNT = "com.capitaworld.sidbi.amount";

	@Override
	public boolean save(RetailApplicantRequest applicantRequest, Long userId) throws Exception {

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

			BeanUtils.copyProperties(applicantRequest, applicantDetail, CommonUtils.IgnorableCopy.RETAIL_FINAL);
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
			logger.error("Error while Saving Retail Profile:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	
	@Override
	public boolean saveITRResponse(RetailApplicantRequest applicantRequest) throws Exception {
		try {
			RetailApplicantDetail applicantDetail = applicantRepository.findOneByApplicationIdIdAndIsActive(applicantRequest.getApplicationId(), true);
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
			BeanUtils.copyProperties(applicantRequest, applicantDetail,CommonUtils.IgnorableCopy.RETAIL_FINAL_WITH_ID);
			Address address = applicantRequest.getFirstAddress();
			if(!CommonUtils.isObjectNullOrEmpty(address)) {
				applicantDetail.setAddressPremiseName(address.getPremiseNumber());
				applicantDetail.setAddressLandmark(address.getLandMark());
				applicantDetail.setAddressStreetName(address.getStreetName());
				applicantDetail.setAddressCountry(address.getCountryId());
				applicantDetail.setAddressState(!CommonUtils.isObjectNullOrEmpty(address.getStateId()) ? address.getStateId().longValue() : null);
				applicantDetail.setAddressCity(address.getCityId());
				applicantDetail.setAddressPincode(address.getPincode());
			}
			applicantDetail.setBirthDate(applicantRequest.getDob());
			applicantDetail = applicantRepository.save(applicantDetail);

			//SAVE INCOME DETAILS 
			applicantIncomeService.saveAll(applicantRequest.getIncomeDetailsList());
			return true;
		} catch (Exception e) {
			logger.error("Error while Saving Retail Profile:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getCoapAndGuarIds(Long userId, Long applicationId) throws Exception {
		try {
			List<Long> coAppIds = coApplicantService.getCoAppIds(userId, applicationId);
			List<Long> guarantorIds = guarantorService.getGuarantorIds(userId, applicationId);
			JSONObject obj = new JSONObject();
			obj.put("coAppIds", coAppIds);
			obj.put("guarantorIds", guarantorIds);
			return obj;
		} catch (Exception e) {
			logger.error("Error while getCoapAndGuarIds:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public RetailApplicantRequest get(Long userId, Long applicationId) throws Exception {
		try {
			RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(userId,
					applicationId);
			if (applicantDetail == null) {
				RetailApplicantRequest request = new RetailApplicantRequest();
				LoanApplicationMaster applicationMaster = loanApplicationRepository.getByIdAndUserId(applicationId,
						userId);
				request.setDetailsFilledCount(applicationMaster.getDetailsFilledCount());
				return request;
			}
			RetailApplicantRequest applicantRequest = new RetailApplicantRequest();
			BeanUtils.copyProperties(applicantDetail, applicantRequest);
			copyAddressFromDomainToRequest(applicantDetail, applicantRequest);
			applicantRequest.setCoApplicants(coApplicantService.getList(applicationId, userId));
			applicantRequest.setGuarantors(guarantorService.getList(applicationId, userId));
			Integer[] saperatedTime = CommonUtils.saperateDayMonthYearFromDate(applicantDetail.getBirthDate());
			applicantRequest.setDate(saperatedTime[0]);
			applicantRequest.setMonth(saperatedTime[1]);
			applicantRequest.setYear(saperatedTime[2]);
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
			return applicantRequest;
		} catch (Exception e) {
			logger.error("Error while Saving Retail Profile:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public FinalCommonRetailRequest getFinal(Long id, Long applicationId) throws Exception {
		try {
			RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(id, applicationId);
			if (applicantDetail == null) {
				throw new NullPointerException("RetailApplicantDetail Record of Final Portion not exists in DB of ID : "
						+ id + "  ApplicationId==>" + applicationId);
			}
			FinalCommonRetailRequest applicantRequest = new FinalCommonRetailRequest();
			BeanUtils.copyProperties(applicantDetail, applicantRequest, CommonUtils.IgnorableCopy.RETAIL_PROFILE);
			applicantRequest.setCurrencyValue(CommonDocumentUtils.getCurrency(applicantDetail.getCurrencyId()));
			applicantRequest.setFinalFilledCount(applicantDetail.getApplicationId().getFinalFilledCount());
			return applicantRequest;
		} catch (Exception e) {
			logger.error("Error while Saving Retail Profile:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public boolean saveFinal(FinalCommonRetailRequest applicantRequest, Long userId) throws Exception {
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
			BeanUtils.copyProperties(applicantRequest, applicantDetail, CommonUtils.IgnorableCopy.RETAIL_PROFILE);
			applicantRepository.save(applicantDetail);
			// Updating Final Flag
			loanApplicationRepository.setIsApplicantFinalMandatoryFilled(applicantRequest.getApplicationId(),
					finaluserId, applicantRequest.getIsApplicantFinalFilled());
			// Updating Final Count
			loanApplicationRepository.setFinalFilledCount(applicantRequest.getApplicationId(), finaluserId,
					applicantRequest.getFinalFilledCount());
			return true;
		} catch (Exception e) {
			logger.error("Error while Saving Retail Profile:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<CoApplicantRequest> getCoApplicants(Long userId, Long applicationId) throws Exception {
		// TODO Auto-generated method stub
		return coApplicantService.getList(applicationId, userId);
	}

	@Override
	public Integer getCurrency(Long applicationId, Long userId) throws Exception {
		try {
			return applicantRepository.getCurrency(userId, applicationId);
		} catch (Exception e) {
			logger.error("Error while Getting Currency:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<GuarantorRequest> getGuarantors(Long userId, Long applicationId) throws Exception {
		return guarantorService.getList(applicationId, userId);
	}

	@Override
	public CibilFullFillOfferRequest getProfile(Long userId, Long applicationId) throws Exception {
		try {
			logger.info("start getProfile() method");
			RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(userId,
					applicationId);
			if (applicantDetail == null) {
				return null;
			}
			CibilFullFillOfferRequest cibilFullFillOfferRequest = new CibilFullFillOfferRequest();
			cibilFullFillOfferRequest.setPan(applicantDetail.getPan());
			cibilFullFillOfferRequest.setAdhaar(applicantDetail.getAadharNumber());
			AddressRequest address = new AddressRequest();
			address.setAddressType("01"); // PermenantType
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
			UserResponse userResponse = usersClient.getEmailMobile(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				@SuppressWarnings("unchecked")
				UsersRequest request = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				cibilFullFillOfferRequest.setEmail(request.getEmail());
				cibilFullFillOfferRequest.setPhoneNumber(request.getMobile());
			}
			logger.info("End getProfile() method with Success Execution");
			return cibilFullFillOfferRequest;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while getting Basic profile for CIBIL.");
			logger.info("End getProfile() method with FAILURE Execution");
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
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
		RetailApplicantDetail applicantDetail = applicantRepository.findOneByApplicationIdId(applicationId);
		if(!CommonUtils.isObjectNullOrEmpty(applicantDetail)) {
			obj.put("name", applicantDetail.getFirstName() + " " + applicantDetail.getMiddleName() + " " + applicantDetail.getLastName());
			obj.put("pan", applicantDetail.getPan());
			obj.put("amount", environment.getProperty(SIDBI_AMOUNT));
		}
		return obj;
	}

}
