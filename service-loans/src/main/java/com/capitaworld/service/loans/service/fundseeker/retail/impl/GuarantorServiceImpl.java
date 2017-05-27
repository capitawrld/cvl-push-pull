package com.capitaworld.service.loans.service.fundseeker.retail.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.dms.util.CommonUtil;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.GuarantorDetails;
import com.capitaworld.service.loans.model.Address;
import com.capitaworld.service.loans.model.retail.BankAccountHeldDetailsRequest;
import com.capitaworld.service.loans.model.retail.CreditCardsDetailRequest;
import com.capitaworld.service.loans.model.retail.CreditCardsDetailResponse;
import com.capitaworld.service.loans.model.retail.ExistingLoanDetailRequest;
import com.capitaworld.service.loans.model.retail.FinalCommonRetailRequest;
import com.capitaworld.service.loans.model.retail.FixedDepositsDetailsRequest;
import com.capitaworld.service.loans.model.retail.GuarantorRequest;
import com.capitaworld.service.loans.model.retail.OtherCurrentAssetDetailRequest;
import com.capitaworld.service.loans.model.retail.OtherCurrentAssetDetailResponse;
import com.capitaworld.service.loans.model.retail.OtherIncomeDetailRequest;
import com.capitaworld.service.loans.model.retail.OtherIncomeDetailResponse;
import com.capitaworld.service.loans.model.retail.ReferenceRetailDetailsRequest;
import com.capitaworld.service.loans.model.teaser.finalview.RetailFinalViewCommonResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.RetailProfileViewResponse;
import com.capitaworld.service.loans.repository.fundseeker.retail.GuarantorDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.fundseeker.retail.BankAccountHeldDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.CreditCardsDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.ExistingLoanDetailsService;
import com.capitaworld.service.loans.service.fundseeker.retail.FixedDepositsDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.fundseeker.retail.OtherCurrentAssetDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.OtherIncomeDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.ReferenceRetailDetailsService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.oneform.enums.AlliedActivity;
import com.capitaworld.service.oneform.enums.Assets;
import com.capitaworld.service.oneform.enums.CastCategory;
import com.capitaworld.service.oneform.enums.EducationStatusRetailMst;
import com.capitaworld.service.oneform.enums.EmployeeWith;
import com.capitaworld.service.oneform.enums.EmploymentStatusRetailMst;
import com.capitaworld.service.oneform.enums.Gender;
import com.capitaworld.service.oneform.enums.IncomeDetails;
import com.capitaworld.service.oneform.enums.IndustryType;
import com.capitaworld.service.oneform.enums.LandSize;
import com.capitaworld.service.oneform.enums.MaritalStatus;
import com.capitaworld.service.oneform.enums.Occupation;
import com.capitaworld.service.oneform.enums.OccupationNature;
import com.capitaworld.service.oneform.enums.OfficeTypeRetailMst;
import com.capitaworld.service.oneform.enums.Options;
import com.capitaworld.service.oneform.enums.OwnershipTypeRetailMst;
import com.capitaworld.service.oneform.enums.ReligionRetailMst;
import com.capitaworld.service.oneform.enums.ResidenceStatusRetailMst;
import com.capitaworld.service.oneform.enums.Title;

@Service
@Transactional
public class GuarantorServiceImpl implements GuarantorService {

	private static final Logger logger = LoggerFactory.getLogger(GuarantorServiceImpl.class.getName());

	protected static final String DMS_URL = "dmsURL";

	@Autowired
	private GuarantorDetailsRepository guarantorDetailsRepository;

	@Autowired
	private RetailApplicantDetailRepository retailApplicantDetailRepository;


	@Autowired
	Environment environment;

	@Autowired
	private CreditCardsDetailService creditCardDetailsService; 
	
	@Autowired
	private ExistingLoanDetailsService existingLoanService;
	
	@Autowired
	private BankAccountHeldDetailService bankAccountsHeldService;
	
	@Autowired
	private FixedDepositsDetailService fixedDepositService;
	
	@Autowired
	private OtherCurrentAssetDetailService otherCurrentAssetService;
	
	@Autowired
	private OtherIncomeDetailService otherIncomeService;
	
	@Autowired
	private ReferenceRetailDetailsService referenceService;
	
	@Override
	public boolean save(GuarantorRequest guarantorRequest, Long applicationId, Long userId) throws Exception {
		try {
			GuarantorDetails guarantorDetails = guarantorDetailsRepository.get(applicationId, (CommonUtils.isObjectNullOrEmpty(guarantorRequest.getClientId()) ? userId : guarantorRequest.getClientId()),
					guarantorRequest.getId());
			if (guarantorDetails != null) {
				// throw new NullPointerException(
				// "CoApplicant Id Record not exists in DB : " +
				// guarantorRequest.getId());

				if (guarantorRequest.getIsActive() != null && !guarantorRequest.getIsActive().booleanValue()) {
					guarantorDetailsRepository.inactiveGuarantor(applicationId, guarantorRequest.getId());
					return true;
				}
				guarantorDetails.setModifiedBy(userId);
				guarantorDetails.setModifiedDate(new Date());
			} else {
				guarantorDetails = new GuarantorDetails();
				guarantorDetails.setFirstName(guarantorRequest.getFirstName());
				guarantorDetails.setCreatedBy(userId);
				guarantorDetails.setCreatedDate(new Date());
				guarantorDetails.setApplicationId(new LoanApplicationMaster(applicationId));
			}
			BeanUtils.copyProperties(guarantorRequest, guarantorDetails);
			copyAddressFromRequestToDomain(guarantorRequest, guarantorDetails);
			if (guarantorRequest.getDate() != null && guarantorRequest.getMonth() != null
					&& guarantorRequest.getYear() != null) {
				Date birthDate = CommonUtils.getDateByDateMonthYear(guarantorRequest.getDate(),
						guarantorRequest.getMonth(), guarantorRequest.getYear());
				guarantorDetails.setBirthDate(birthDate);
			}
			guarantorDetailsRepository.save(guarantorDetails);
			return true;

		} catch (Exception e) {
			logger.error("Error while Saving Guarantor Retail Profile:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public GuarantorRequest get(Long userId, Long applicationId, Long id) throws Exception {
		try {
			GuarantorDetails guarantorDetail = guarantorDetailsRepository.get(applicationId, userId, id);
			if (guarantorDetail == null) {
				throw new NullPointerException("GuarantorDetails Record not exists in DB of ID : " + id
						+ " and Application ID==>" + applicationId + " User Id ==>" + userId);
			}
			GuarantorRequest guaRequest = new GuarantorRequest();
			BeanUtils.copyProperties(guarantorDetail, guaRequest);
			copyAddressFromDomainToRequest(guarantorDetail, guaRequest);
			guaRequest.setCurrencyId(retailApplicantDetailRepository.getCurrency(userId, applicationId));
			Integer[] saperatedTime = CommonUtils.saperateDayMonthYearFromDate(guarantorDetail.getBirthDate());
			guaRequest.setDate(saperatedTime[0]);
			guaRequest.setMonth(saperatedTime[1]);
			guaRequest.setYear(saperatedTime[2]);
			return guaRequest;
		} catch (Exception e) {
			logger.error("Error while getting Guarantor Retail Profile:-");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public List<GuarantorRequest> getList(Long applicationId, Long userId) throws Exception {
		try {
			List<GuarantorDetails> details = guarantorDetailsRepository.getList(applicationId, userId);
			List<GuarantorRequest> requests = new ArrayList<>(details.size());
			for (GuarantorDetails detail : details) {
				GuarantorRequest request = new GuarantorRequest();
				BeanUtils.copyProperties(detail, request, CommonUtils.IgnorableCopy.RETAIL_FINAL);
				requests.add(request);
			}
			return requests;
		} catch (Exception e) {
			logger.error("Error while getting list of Guarantor Retail Profile:-");
			e.printStackTrace();
			throw new Exception("Something went Wrong !");
		}
	}

	@Override
	public boolean saveFinal(FinalCommonRetailRequest applicantRequest, Long userId) throws Exception {
		try {
			GuarantorDetails guaDetails = guarantorDetailsRepository.get(applicantRequest.getApplicationId(), (CommonUtils.isObjectNullOrEmpty(applicantRequest.getClientId()) ? userId : applicantRequest.getClientId()),
					applicantRequest.getId());
			if (guaDetails == null) {
				throw new NullPointerException("Guarantor Id Record not exists in DB : Application Id==>"
						+ applicantRequest.getApplicationId());
			}
			guaDetails.setModifiedBy(userId);
			guaDetails.setModifiedDate(new Date());
			BeanUtils.copyProperties(applicantRequest, guaDetails, CommonUtils.IgnorableCopy.RETAIL_PROFILE);
			guarantorDetailsRepository.save(guaDetails);
			return true;

		} catch (Exception e) {
			logger.error("Error while Saving final Guarantor Retail Profile:-");
			e.printStackTrace();
			throw new Exception("Something went Wrong !");
		}
	}

	@Override
	public FinalCommonRetailRequest getFinal(Long userId, Long applicationId, Long id) throws Exception {
		try {
			GuarantorDetails guaDetail = guarantorDetailsRepository.get(applicationId, userId, id);
			if (guaDetail == null) {
				throw new NullPointerException("GuarantorDetails Record of Final Portion not exists in DB of User ID : "
						+ userId + " and Application Id ==>" + applicationId);
			}
			FinalCommonRetailRequest applicantRequest = new FinalCommonRetailRequest();
			BeanUtils.copyProperties(guaDetail, applicantRequest, CommonUtils.IgnorableCopy.RETAIL_PROFILE);
			Integer currencyId = retailApplicantDetailRepository.getCurrency(userId, applicationId);
			applicantRequest.setCurrencyValue(CommonDocumentUtils.getCurrency(currencyId));
			return applicantRequest;
		} catch (Exception e) {
			logger.error("Error while getting final Guarantor Retail Profile:-");
			e.printStackTrace();
			throw new Exception("Something went Wrong !");
		}
	}

	public static void copyAddressFromRequestToDomain(GuarantorRequest from, GuarantorDetails to) {
		if (from.getFirstAddress() != null) {
			to.setPermanentPremiseNumberName(from.getFirstAddress().getPremiseNumber());
			to.setPermanentStreetName(from.getFirstAddress().getStreetName());
			to.setPermanentLandMark(from.getFirstAddress().getLandMark());
			if (from.getFirstAddress().getCityId() != null) {
				to.setPermanentCityId(from.getFirstAddress().getCityId().intValue());
			}
			to.setPermanentStateId(from.getFirstAddress().getStateId());
			to.setPermanentCountryId(from.getFirstAddress().getCountryId());
			if (from.getFirstAddress().getPincode() != null) {
				to.setPermanentPincode(from.getFirstAddress().getPincode().intValue());
			}

		}

		if (from.getAddressSameAs() != null && from.getAddressSameAs().booleanValue()) {
			if (from.getFirstAddress() != null) {
				to.setOfficePremiseNumberName(from.getFirstAddress().getPremiseNumber());
				to.setOfficeStreetName(from.getFirstAddress().getStreetName());
				to.setOfficeLandMark(from.getFirstAddress().getLandMark());
				if (from.getFirstAddress().getCityId() != null) {
					to.setOfficeCityId(from.getFirstAddress().getCityId().intValue());
				}
				to.setOfficeStateId(from.getFirstAddress().getStateId());
				to.setOfficeCountryId(from.getFirstAddress().getCountryId());
				if (from.getFirstAddress().getPincode() != null) {
					to.setOfficePincode(from.getFirstAddress().getPincode().intValue());
				}

			}
		} else {
			if (from.getSecondAddress() != null) {
				to.setOfficePremiseNumberName(from.getSecondAddress().getPremiseNumber());
				to.setOfficeStreetName(from.getSecondAddress().getStreetName());
				to.setOfficeLandMark(from.getSecondAddress().getLandMark());
				if (from.getSecondAddress().getCityId() != null) {
					to.setOfficeCityId(from.getSecondAddress().getCityId().intValue());
				}

				to.setOfficeStateId(from.getSecondAddress().getStateId());
				to.setOfficeCountryId(from.getSecondAddress().getCountryId());
				if (from.getSecondAddress().getPincode() != null) {
					to.setOfficePincode(from.getSecondAddress().getPincode().intValue());
				}

			}
		}

	}

	public static void copyAddressFromDomainToRequest(GuarantorDetails from, GuarantorRequest to) {
		Address address = new Address();
		address.setPremiseNumber(from.getPermanentPremiseNumberName());
		address.setLandMark(from.getPermanentLandMark());
		address.setStreetName(from.getPermanentStreetName());
		if (from.getPermanentCityId() != null) {
			address.setCityId(from.getPermanentCityId().longValue());
		}
		address.setStateId(from.getPermanentStateId());
		address.setCountryId(from.getPermanentCountryId());
		if (from.getPermanentPincode() != null) {
			address.setPincode(from.getPermanentPincode().longValue());
		}
		to.setFirstAddress(address);
		if (from.getAddressSameAs() != null && from.getAddressSameAs().booleanValue()) {
			to.setSecondAddress(address);
		} else {
			address = new Address();
			address.setPremiseNumber(from.getOfficePremiseNumberName());
			address.setLandMark(from.getOfficeLandMark());
			address.setStreetName(from.getOfficeStreetName());
			if (from.getOfficeCityId() != null) {
				address.setCityId(from.getOfficeCityId().longValue());
			}
			address.setStateId(from.getOfficeStateId());
			address.setCountryId(from.getOfficeCountryId());
			if (from.getOfficePincode() != null) {
				address.setPincode(from.getOfficePincode().longValue());
			}
			to.setSecondAddress(address);
		}
	}

	@Override
	public List<RetailProfileViewResponse> getGuarantorServiceResponse(Long applicantId, Long userId) throws Exception {
		try {
			List<GuarantorDetails> guarantorDetails = guarantorDetailsRepository.getList(applicantId, userId);
			if (guarantorDetails != null && !guarantorDetails.isEmpty()) {
				List<RetailProfileViewResponse> plResponses = new ArrayList<RetailProfileViewResponse>();
				for (GuarantorDetails guarantorDetail : guarantorDetails) {
					RetailProfileViewResponse profileViewPLResponse = new RetailProfileViewResponse();
					if (guarantorDetail.getOccupationId() != null) {
						if (guarantorDetail.getOccupationId() == 2) {
							profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(guarantorDetail.getOccupationId()).getValue());
							if (!CommonUtil.isObjectNullOrEmpty(guarantorDetail.getCompanyName())) {
								profileViewPLResponse.setCompanyName(guarantorDetail.getCompanyName());
							}
							if (!CommonUtil.isObjectNullOrEmpty(guarantorDetail.getEmployedWithId())) {
								if (guarantorDetail.getEmployedWithId() == 8) {
									profileViewPLResponse.setEmployeeWith(guarantorDetail.getEmployedWithOther());
								} else {
									profileViewPLResponse.setEmployeeWith(EmployeeWith.getById(guarantorDetail.getEmployedWithId()).getValue());
								}
							}
						} else if (guarantorDetail.getOccupationId() == 3 || guarantorDetail.getOccupationId() == 4) {
							profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(guarantorDetail.getOccupationId()).getValue());
							if (!CommonUtil.isObjectNullOrEmpty(guarantorDetail.getEntityName())) {
								profileViewPLResponse.setEntityName(guarantorDetail.getEntityName());
							}
							if (!CommonUtil.isObjectNullOrEmpty(guarantorDetail.getIndustryTypeId())) {
								if (guarantorDetail.getIndustryTypeId() == 16) {
									profileViewPLResponse.setIndustryType(guarantorDetail.getIndustryTypeOther());
								} else {
									profileViewPLResponse.setIndustryType(IndustryType.getById(guarantorDetail.getIndustryTypeId()).getValue());
								}
							}
						} else if (guarantorDetail.getOccupationId() == 5) {
							profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(guarantorDetail.getOccupationId()).getValue());
							if (guarantorDetail.getSelfEmployedOccupationId() == 10) {
								profileViewPLResponse.setOccupation(guarantorDetail.getSelfEmployedOccupationOther());
							} else {
								profileViewPLResponse.setOccupation(Occupation.getById(guarantorDetail.getSelfEmployedOccupationId()).getValue());
							}
						} else if (guarantorDetail.getOccupationId() == 6) {
							profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(guarantorDetail.getOccupationId()).getValue());
							if (!CommonUtil.isObjectNullOrEmpty(guarantorDetail.getLandSize())) {
								profileViewPLResponse.setLandSize(LandSize.getById(guarantorDetail.getLandSize().intValue()).getValue());
							}
							if (!CommonUtil.isObjectNullOrEmpty(guarantorDetail.getAlliedActivityId())) {
								profileViewPLResponse.setAlliedActivity(AlliedActivity.getById(guarantorDetail.getAlliedActivityId()).getValue());
							}
						} else if (guarantorDetail.getOccupationId() == 7) {
							profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(guarantorDetail.getOccupationId()).getValue());
						}
					}

					//set pan car
					profileViewPLResponse.setPan(guarantorDetail.getPan() != null ? guarantorDetail.getPan() : null);
					profileViewPLResponse.setTitle(guarantorDetail.getTitleId() != null ? Title.getById(guarantorDetail.getTitleId()).getValue() : null);
					profileViewPLResponse.setAge(guarantorDetail.getBirthDate() != null ? CommonUtils.getAgeFromBirthDate(guarantorDetail.getBirthDate()).toString() : null);
					profileViewPLResponse.setFirstName(guarantorDetail.getFirstName() != null ? guarantorDetail.getFirstName() : null);
					profileViewPLResponse.setGender(guarantorDetail.getGenderId() != null ? Gender.getById(guarantorDetail.getGenderId()).getValue() : null);
					profileViewPLResponse.setLastName(guarantorDetail.getLastName() != null ? guarantorDetail.getLastName() : null);
					profileViewPLResponse.setMaritalStatus(guarantorDetail.getStatusId() != null ? MaritalStatus.getById(guarantorDetail.getStatusId()).getValue() : null);
					profileViewPLResponse.setMiddleName(guarantorDetail.getMiddleName() != null ? guarantorDetail.getMiddleName() : null);
					profileViewPLResponse.setMonthlyIncome(String.valueOf(guarantorDetail.getMonthlyIncome() != null ? guarantorDetail.getMonthlyIncome() : 0));
					plResponses.add(profileViewPLResponse);
				}
				return plResponses;
			} else {
				throw new Exception("No Data found");
			}
		} catch (Exception e) {
			throw new Exception("Error Fetching Guarantor Details");
		}
	}
	
	@Override
	public List<RetailFinalViewCommonResponse> getGuarantorFinalViewResponse(Long applicantId, Long userId) throws Exception {
		try {
			List<GuarantorDetails> guarantorDetails = guarantorDetailsRepository.getList(applicantId, userId);
			if (guarantorDetails != null && !guarantorDetails.isEmpty()) {
				List<RetailFinalViewCommonResponse> finalCommonresponseList = new ArrayList<RetailFinalViewCommonResponse>();

				for (GuarantorDetails guarantorDetail : guarantorDetails) {
					RetailFinalViewCommonResponse finalViewResponse = new RetailFinalViewCommonResponse();
					
					if(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getCastId())){
						finalViewResponse.setCaste(CastCategory.getById(guarantorDetail.getCastId()).getValue());	
						if(guarantorDetail.getCastId() == 6){
							finalViewResponse.setCasteOther(guarantorDetail.getCastOther());
						}
					}else{
						finalViewResponse.setCaste("NA");
					}
					if(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getReligion())){
						finalViewResponse.setReligion(ReligionRetailMst.getById(guarantorDetail.getReligion()).getValue());	
						if(guarantorDetail.getReligion() == 8){
							finalViewResponse.setReligionOther(guarantorDetail.getReligionOther());
						}
					}else{
						finalViewResponse.setReligion("NA");
					}
					finalViewResponse.setBirthPlace(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getBirthPlace()) ? guarantorDetail.getBirthPlace() : "NA");
					finalViewResponse.setFatherFullName(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getFatherName()) ? guarantorDetail.getFatherName() : "NA");
					finalViewResponse.setMotherName(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getMotherName()) ? guarantorDetail.getMotherName() : "NA");
					if(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getStatusId())){
						if(guarantorDetail.getStatusId() == 2){
							finalViewResponse.setSpouseName(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getSpouseName()) ? guarantorDetail.getSpouseName() :"NA");
							finalViewResponse.setSpouseEmployed(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getIsSpouseEmployed()) ? Options.getById((guarantorDetail.getIsSpouseEmployed() ? 1 : 0)).getValue() :"NA");
							finalViewResponse.setNoOfChildren(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getNoChildren()) ? guarantorDetail.getNoChildren().toString() : "NA");
						}
					}
					finalViewResponse.setNoOfDependents(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getNoDependent()) ? guarantorDetail.getNoDependent().toString() : "NA");
					if(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getHighestQualification())){
						finalViewResponse.setHighestQualification(EducationStatusRetailMst.getById(guarantorDetail.getHighestQualification()).getValue());
						if(guarantorDetail.getHighestQualification() == 6){
							finalViewResponse.setHighestQualificationOther(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getHighestQualificationOther()) ? guarantorDetail.getHighestQualificationOther() : "NA");
						}
					}else{
						finalViewResponse.setHighestQualification("NA");	
					}
					finalViewResponse.setQualifyingYear(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getQualifyingYear()) ? guarantorDetail.getQualifyingYear().getMonth() +"/"+ guarantorDetail.getQualifyingYear().getYear() : "NA");
					finalViewResponse.setInstituteName(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getInstitute()) ? guarantorDetail.getInstitute() : "NA");
					if(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getResidenceType())){
						finalViewResponse.setResidenceType(ResidenceStatusRetailMst.getById(guarantorDetail.getResidenceType()).getValue());
						if(guarantorDetail.getResidenceType() == 2){
							finalViewResponse.setAnnualRent(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getAnnualRent()) ? guarantorDetail.getAnnualRent().toString() : "NA");
						}
					}else{
						finalViewResponse.setResidenceType("NA");
					}
					finalViewResponse.setYearAtCurrentResident(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getResidingYear()) ? guarantorDetail.getResidingYear().toString() : "NA");
					finalViewResponse.setMonthsAtCurrentResident(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getResidingMonth()) ? guarantorDetail.getResidingMonth().toString() : "NA");
					finalViewResponse.setWebsite(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getWebsiteAddress()) ? guarantorDetail.getWebsiteAddress() : "NA");
					if(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getOccupationId())){
						if(guarantorDetail.getOccupationId() == 2){//salaried
							finalViewResponse.setEmploymentStatus(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getEmploymentStatus()) ? EmploymentStatusRetailMst.getById(guarantorDetail.getEmploymentStatus()).getValue() : "NA");
							finalViewResponse.setCurrentIndustry(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getCurrentIndustry()) ?  guarantorDetail.getCurrentIndustry() : "NA");
							finalViewResponse.setCurrentDepartment(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getCurrentDepartment()) ?  guarantorDetail.getCurrentDepartment() : "NA");
							finalViewResponse.setCurrentDesignation(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getCurrentDesignation()) ?  guarantorDetail.getCurrentDesignation() : "NA");
							finalViewResponse.setYearsInCurrentJob(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getCurrentJobYear()) ?  guarantorDetail.getCurrentJobYear().toString() : "NA");
							finalViewResponse.setMonthsInCurrentJob(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getCurrentJobMonth()) ?  guarantorDetail.getCurrentJobMonth().toString() : "NA");
							finalViewResponse.setTotalExperienceInMonths(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getTotalExperienceMonth()) ?  guarantorDetail.getTotalExperienceMonth().toString() : "NA");
							finalViewResponse.setTotalExperienceInYears(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getTotalExperienceYear()) ?  guarantorDetail.getTotalExperienceYear().toString() : "NA");
							finalViewResponse.setPreviousExperienceInMonths(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getPreviousJobMonth()) ?  guarantorDetail.getPreviousJobMonth().toString() : "NA");
							finalViewResponse.setPreviousExperienceInYears(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getPreviousJobYear()) ?  guarantorDetail.getPreviousJobYear().toString() : "NA");
							finalViewResponse.setPreviousEmployerName(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getPreviousEmployersName()) ?  guarantorDetail.getPreviousEmployersName() : "NA");
							finalViewResponse.setPreviousEmployerAddress(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getPreviousEmployersAddress()) ?  guarantorDetail.getPreviousEmployersAddress() : "NA");
						}else if(guarantorDetail.getOccupationId() == 6){//agriculturist
							finalViewResponse.setTotalLandOwned(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getTotalLandOwned()) ?  guarantorDetail.getTotalLandOwned().toString() : "NA");
							finalViewResponse.setPresentlyIrrigated(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getPresentlyIrrigated()) ?  guarantorDetail.getPresentlyIrrigated() : "NA");
							finalViewResponse.setSeasonalIrrigated(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getSeasonalIrrigated()) ?  guarantorDetail.getSeasonalIrrigated() : "NA");
							finalViewResponse.setRainFed(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getRainFed()) ?  guarantorDetail.getRainFed() : "NA");
							finalViewResponse.setUnAttended(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getUnattended()) ?  guarantorDetail.getUnattended() : "NA");
						}else if(guarantorDetail.getOccupationId() == 3 || guarantorDetail.getOccupationId() == 4 || guarantorDetail.getOccupationId() == 5){//business/self employed prof/self employed
							finalViewResponse.setEntityName(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getNameOfEntity()) ?  guarantorDetail.getNameOfEntity() : "NA");
							finalViewResponse.setOwnershipType(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getOwnershipType()) ?  OwnershipTypeRetailMst.getById(guarantorDetail.getOwnershipType()).getValue() : "NA");
							finalViewResponse.setOfficeType(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getOfficeType()) ?  OfficeTypeRetailMst.getById(guarantorDetail.getOfficeType()).getValue() : "NA");
							finalViewResponse.setNoOfPartners(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getNoPartners()) ?  guarantorDetail.getNoPartners().toString() : "NA");
							finalViewResponse.setNameOfPartners(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getPartnersName()) ?  guarantorDetail.getPartnersName() : "NA");
							finalViewResponse.setBusinessEstablishmentYear(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getBusinessStartDate()) ?  guarantorDetail.getBusinessStartDate().getMonth()+"/"+guarantorDetail.getBusinessStartDate().getYear() : "NA");
							finalViewResponse.setShareHolding(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getShareholding()) ?  guarantorDetail.getShareholding() : "NA");
							finalViewResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getAnnualTurnover()) ?  guarantorDetail.getAnnualTurnover().toString() : "NA");
							finalViewResponse.setTradeLicenseNo(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getTradeLicenseNumber()) ?  guarantorDetail.getTradeLicenseNumber() : "NA");
							finalViewResponse.setTradeExpiryDate(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getTradeLicenseExpiryDate()) ?  guarantorDetail.getTradeLicenseExpiryDate().getMonth()+"/"+guarantorDetail.getTradeLicenseExpiryDate().getYear() : "NA");
							finalViewResponse.setNameOfPoaHolder(!CommonUtils.isObjectNullOrEmpty(guarantorDetail.getPoaHolderName()) ?  guarantorDetail.getPoaHolderName() : "NA");
						}
					}
					List<ExistingLoanDetailRequest> existingLoanDetailRequestList = existingLoanService.getExistingLoanDetailList(applicantId,CommonUtils.ApplicantType.COAPPLICANT);
					finalViewResponse.setExistingLoanDetailRequest(existingLoanDetailRequestList);
					
					List<BankAccountHeldDetailsRequest> accountHeldDetailsRequestList = bankAccountsHeldService.getExistingLoanDetailList(applicantId, CommonUtils.ApplicantType.COAPPLICANT);
					finalViewResponse.setBankAccountHeldDetailsRequest(accountHeldDetailsRequestList);
					
					List<CreditCardsDetailRequest> creditCardsDetailRequestList = creditCardDetailsService.getExistingLoanDetailList(applicantId, CommonUtils.ApplicantType.COAPPLICANT);
					List<CreditCardsDetailResponse> creditCardsDetailResponseList = new ArrayList<CreditCardsDetailResponse>();
					for(CreditCardsDetailRequest cardsDetailRequest:creditCardsDetailRequestList){
						CreditCardsDetailResponse cardsDetailResponse = new CreditCardsDetailResponse();
						cardsDetailResponse.setCardNumber(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getCardNumber()) ? cardsDetailRequest.getCardNumber()  : "NA");
						cardsDetailResponse.setIssuerName(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getIssuerName()) ? cardsDetailRequest.getIssuerName()  : "NA");
						/*cardsDetailResponse.setCreditCardTypes(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getCreditCardTypesId()) ? C  cardsDetailRequest.getIssuerName()  : "NA");*/
						cardsDetailResponse.setOutstandingBalance(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getOutstandingBalance()) ? cardsDetailRequest.getOutstandingBalance().toString() : "NA");
						creditCardsDetailResponseList.add(cardsDetailResponse);
					}
					finalViewResponse.setCreditCardsDetailResponse(creditCardsDetailResponseList);
					
					List<FixedDepositsDetailsRequest> depositsDetailsRequestList = fixedDepositService.getFixedDepositsDetailList(applicantId, CommonUtils.ApplicantType.COAPPLICANT);
					finalViewResponse.setFixedDepositsDetailsRequest(depositsDetailsRequestList);
					
					List<OtherCurrentAssetDetailRequest> otherCurrentAssetDetailRequestList = otherCurrentAssetService.getOtherCurrentAssetDetailList(applicantId, CommonUtils.ApplicantType.COAPPLICANT);
					List<OtherCurrentAssetDetailResponse> assetDetailResponseList = new ArrayList<OtherCurrentAssetDetailResponse>();
					for(OtherCurrentAssetDetailRequest assetDetailRequest:otherCurrentAssetDetailRequestList){
						OtherCurrentAssetDetailResponse assetDetailResponse = new OtherCurrentAssetDetailResponse();
						assetDetailResponse.setAssetType(!CommonUtils.isObjectNullOrEmpty(assetDetailRequest.getAssetTypesId()) ? Assets.getById(assetDetailRequest.getAssetTypesId()).getValue() : "NA");
						assetDetailResponse.setAssetDescription(!CommonUtils.isObjectNullOrEmpty(assetDetailRequest.getAssetDescription()) ? assetDetailRequest.getAssetDescription() : "NA");
						assetDetailResponse.setAssetValue(!CommonUtils.isObjectNullOrEmpty(assetDetailRequest.getAssetValue()) ? assetDetailRequest.getAssetValue().toString() : "NA");
						assetDetailResponseList.add(assetDetailResponse);
					}
					finalViewResponse.setAssetDetailResponseList(assetDetailResponseList);
					
					List<OtherIncomeDetailRequest> otherIncomeDetailRequestsList = otherIncomeService.getOtherIncomeDetailList(applicantId, CommonUtils.ApplicantType.COAPPLICANT);
					List<OtherIncomeDetailResponse> incomeDetailResponseList = new ArrayList<OtherIncomeDetailResponse>();
					for(OtherIncomeDetailRequest detailRequest : otherIncomeDetailRequestsList){
						OtherIncomeDetailResponse detailResponse = new OtherIncomeDetailResponse();
						detailResponse.setIncomeDetails(!CommonUtils.isObjectNullOrEmpty(detailRequest.getIncomeDetailsId()) ? IncomeDetails.getById(detailRequest.getIncomeDetailsId()).getValue() : "NA");
						detailResponse.setIncomeHead(!CommonUtils.isObjectNullOrEmpty(detailRequest.getIncomeHead()) ? detailRequest.getIncomeHead() : "NA");
						detailResponse.setGrossIncome(!CommonUtils.isObjectNullOrEmpty(detailRequest.getGrossIncome()) ? detailRequest.getGrossIncome().toString() : "NA");
						detailResponse.setNetIncome(!CommonUtils.isObjectNullOrEmpty(detailRequest.getNetIncome()) ? detailRequest.getNetIncome().toString() : "NA");
						incomeDetailResponseList.add(detailResponse);
					}
					finalViewResponse.setIncomeDetailResponseList(incomeDetailResponseList);
										
					List<ReferenceRetailDetailsRequest> referenceRetailDetailsRequestList = referenceService.getReferenceRetailDetailList(applicantId, CommonUtils.ApplicantType.COAPPLICANT);
					finalViewResponse.setReferenceRetailDetailsRequest(referenceRetailDetailsRequestList);
					finalCommonresponseList.add(finalViewResponse);
				}

				return finalCommonresponseList;
			} else {
				throw new Exception("No Data found");
			}
		} catch (Exception e) {
			throw new Exception("Error Fetching Guarantor Details");
		}
	}
}
