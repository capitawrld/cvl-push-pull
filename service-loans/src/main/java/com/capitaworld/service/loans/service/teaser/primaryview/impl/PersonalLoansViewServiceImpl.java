package com.capitaworld.service.loans.service.teaser.primaryview.impl;

import com.capitaworld.service.dms.client.DMSClient;
import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.PrimaryPersonalLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.RetailApplicantDetail;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.AddressResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.PersonalLoanResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.RetailPrimaryViewResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.RetailProfileViewResponse;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.PrimaryPersonalLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.common.CommonService;
import com.capitaworld.service.loans.service.common.DocumentManagementService;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.teaser.primaryview.PersonalLoansViewService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class PersonalLoansViewServiceImpl implements PersonalLoansViewService {
	
	private static final Logger logger = LoggerFactory.getLogger(PersonalLoansViewServiceImpl.class);

	@Autowired
	private RetailApplicantDetailRepository applicantRepository;

	@Autowired
	private CoApplicantService coApplicantService;

	@Autowired
	private GuarantorService guarantorService;

	@Autowired
	private PrimaryPersonalLoanDetailRepository personalLoanDetailRepository;

	@Autowired
	private Environment environment;
	
	@Autowired
	private OneFormClient oneFormClient;
	
	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private DocumentManagementService documentManagementService;
	
	@Autowired
	private CommonService commonService;
	
	@Override
	public RetailPrimaryViewResponse getPersonalLoansPrimaryViewDetails(Long applicantId) throws LoansException {
		LoanApplicationMaster applicationMaster = loanApplicationRepository.findOne(applicantId);
		RetailPrimaryViewResponse retailPrimaryViewResponse = new RetailPrimaryViewResponse();
		PersonalLoanResponse personalLoanResponse = new PersonalLoanResponse();
		Long userId = applicationMaster.getUserId();
		//applicant
		try {
			RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(userId, applicantId);
			if (applicantDetail != null) {
				RetailProfileViewResponse profileViewPLResponse = new RetailProfileViewResponse();
				profileViewPLResponse.setCompanyName(applicantDetail.getCompanyName());
				personalLoanResponse.setDateOfProposal(CommonUtils.getStringDateFromDate(applicantDetail.getCreatedDate()));
				profileViewPLResponse.setFirstName(applicantDetail.getFirstName());
				try {
					profileViewPLResponse.setGender(Gender.getById(applicantDetail.getGenderId()).getValue());
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				profileViewPLResponse.setLastName(applicantDetail.getLastName());
				profileViewPLResponse.setMaritalStatus(applicantDetail.getStatusId() != null ? MaritalStatus.getById(applicantDetail.getStatusId()).getValue() : null);
				profileViewPLResponse.setMiddleName(applicantDetail.getMiddleName());
				profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());

				profileViewPLResponse.setBonusPerAnnum(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBonusPerAnnum()) ? applicantDetail.getBonusPerAnnum().toString() : "-");
				profileViewPLResponse.setIncentivePerAnnum(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getIncentivePerAnnum()) ? applicantDetail.getIncentivePerAnnum().toString() : "-");
				profileViewPLResponse.setOtherIncome(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOtherIncome()) ? applicantDetail.getOtherIncome().toString() : "-");
				profileViewPLResponse.setOtherInvestment(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOtherInvestment()) ? applicantDetail.getOtherInvestment().toString() : "-");
				profileViewPLResponse.setTaxPaid(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTaxPaidLastYear()) ? applicantDetail.getTaxPaidLastYear().toString() : "-");
				
				//Office Address Data
				AddressResponse officeAddress = new AddressResponse();
				Long cityId = null ;
    			Integer stateId = null;
    			Integer countryId = null;
    			String cityName = null;
    			String stateName = null;
    			String countryName = null;
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCityId()))
    				cityId = applicantDetail.getOfficeCityId();
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeStateId()))
    				stateId = applicantDetail.getOfficeStateId();
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCountryId()))
    				countryId = applicantDetail.getOfficeCountryId();
    			
    			if(cityId != null || stateId != null || countryId != null) {
    				Map<String ,Object> mapData = commonService.getCityStateCountryNameFromOneForm(cityId, stateId, countryId);
    				if(mapData != null) {
    					cityName = mapData.get(CommonUtils.CITY_NAME).toString();
    					stateName = mapData.get(CommonUtils.STATE_NAME).toString();
    					countryName = mapData.get(CommonUtils.COUNTRY_NAME).toString();
    					
    					//set City
    					officeAddress.setCity(cityName != null ? cityName : "-");
    					
    					//set State
    					officeAddress.setState(stateName != null ? stateName : "-");
    					
    					//set Country
    					officeAddress.setCountry(countryName != null ? countryName : "-");
    				}
    			}
    			
    			officeAddress.setLandMark(applicantDetail.getOfficeLandMark());
				officeAddress.setPincode(applicantDetail.getOfficePincode() != null ? applicantDetail.getOfficePincode().toString() : null);
				officeAddress.setPremiseNumber(applicantDetail.getOfficePremiseNumberName());
				officeAddress.setStreetName(applicantDetail.getOfficeStreetName());
				personalLoanResponse.setOfficeAddress(officeAddress);

				/**try {
					List<Long> officeCity = new ArrayList<Long>(1);
					officeCity.add(applicantDetail.getOfficeCityId());
					OneFormResponse formResponse = oneFormClient.getCityByCityListId(officeCity);

					MasterResponse data = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponse.getListData().get(0), MasterResponse.class);
					officeAddress.setCity(data.getValue());
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCountryId())) {
						List<Long> officeCountry = new ArrayList<Long>(1);
						officeCountry.add(applicantDetail.getOfficeCountryId().longValue());
						OneFormResponse country = oneFormClient.getCountryByCountryListId(officeCountry);
						MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) country.getListData().get(0), MasterResponse.class);
						officeAddress.setCountry(dataCountry.getValue());
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeStateId())) {
						List<Long> officeState = new ArrayList<Long>(1);
						officeState.add(applicantDetail.getOfficeStateId().longValue());
						OneFormResponse state = oneFormClient.getStateByStateListId(officeState);
						MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) state.getListData().get(0), MasterResponse.class);
						officeAddress.setState(dataState.getValue());
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}*/
				
				//Permanent Address Data
				AddressResponse permanentAddress = new AddressResponse();
				cityId = null;
    			stateId = null;
    			countryId = null;
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentCityId()))
    				cityId = applicantDetail.getPermanentCityId();
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentStateId()))
    				stateId = applicantDetail.getPermanentStateId();
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentCountryId()))
    				countryId = applicantDetail.getPermanentCountryId();
    			
    			if(cityId != null || stateId != null || countryId != null) {
    				Map<String ,Object> mapData = commonService.getCityStateCountryNameFromOneForm(cityId, stateId, countryId);
    				if(mapData != null) {
    					cityName = mapData.get(CommonUtils.CITY_NAME).toString();
    					stateName = mapData.get(CommonUtils.STATE_NAME).toString();
    					countryName = mapData.get(CommonUtils.COUNTRY_NAME).toString();
    					
    					//set City
    					permanentAddress.setCity(cityName != null ? cityName : "-");
    					
    					//set State
    					permanentAddress.setState(stateName != null ? stateName : "-");
    					
    					//set Country
    					permanentAddress.setCountry(countryName != null ? countryName : "-");
    				}
    			}
    			
    			permanentAddress.setLandMark(applicantDetail.getPermanentLandMark());
				permanentAddress.setPincode(applicantDetail.getPermanentPincode() != null ? applicantDetail.getPermanentPincode().toString() : null);
				permanentAddress.setPremiseNumber(applicantDetail.getPermanentPremiseNumberName());
				permanentAddress.setStreetName(applicantDetail.getPermanentStreetName());
				personalLoanResponse.setPermanentAddress(permanentAddress);

				/**try {
					List<Long> permanentCity = new ArrayList<Long>(1);
					permanentCity.add(applicantDetail.getPermanentCityId());
					OneFormResponse formResponsePermanentCity = oneFormClient.getCityByCityListId(permanentCity);
					MasterResponse dataCity = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponsePermanentCity.getListData().get(0), MasterResponse.class);
					permanentAddress.setCity(dataCity.getValue());
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentCountryId())) {
						List<Long> permanentCountry = new ArrayList<Long>(1);
						permanentCountry.add(applicantDetail.getPermanentCountryId().longValue());
						OneFormResponse countryPermanent = oneFormClient.getCountryByCountryListId(permanentCountry);
						MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) countryPermanent.getListData().get(0), MasterResponse.class);
						permanentAddress.setCountry(dataCountry.getValue());
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentStateId())) {
						List<Long> permanentState = new ArrayList<Long>(1);
						permanentState.add(applicantDetail.getPermanentStateId().longValue());
						OneFormResponse statePermanent = oneFormClient.getStateByStateListId(permanentState);
						MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) statePermanent.getListData().get(0), MasterResponse.class);
						permanentAddress.setState(dataState.getValue());

					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}*/

				profileViewPLResponse.setTitle(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTitleId()) ? Title.getById(applicantDetail.getTitleId()).getValue() : null);
				profileViewPLResponse.setAge(applicantDetail.getBirthDate() != null ? CommonUtils.getAgeFromBirthDate(applicantDetail.getBirthDate()).toString() : null);

				if (applicantDetail.getApplicationId() != null) {
					personalLoanResponse.setTenure(applicantDetail.getApplicationId().getTenure() != null ? String.valueOf((applicantDetail.getApplicationId().getTenure()/12)) : null);
					personalLoanResponse.setLoanType(applicantDetail.getApplicationId().getProductId() != null ? LoanType.getById(applicantDetail.getApplicationId().getProductId()).getValue() : null);
					personalLoanResponse.setLoanAmount(applicantDetail.getApplicationId().getAmount() != null ? CommonUtils.CurrencyFormat(applicantDetail.getApplicationId().getAmount().toString()) : null);
					personalLoanResponse.setCurrency(applicantDetail.getCurrencyId() != null ? Currency.getById(applicantDetail.getCurrencyId()).getValue() : null);
				}

				profileViewPLResponse.setNatureOfOccupationId(applicantDetail.getOccupationId());
				if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOccupationId())){
					switch (applicantDetail.getOccupationId().intValue()) {
					case 2 : //Salaried
						profileViewPLResponse.setCompanyName(applicantDetail.getCompanyName());
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getEmployedWithId())){
							if(applicantDetail.getEmployedWithId() != 8){
								profileViewPLResponse.setEmployeeWith(EmployeeWith.getById(applicantDetail.getEmployedWithId()).getValue());
							}else{
								profileViewPLResponse.setEmployeeWith(applicantDetail.getEmployedWithOther());
							}
						}
						profileViewPLResponse.setYearsInCurrentJob(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getCurrentJobYear()) ?  applicantDetail.getCurrentJobYear().toString() : "-");
						profileViewPLResponse.setMonthsInCurrentJob(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getCurrentJobMonth()) ?  applicantDetail.getCurrentJobMonth().toString() : "-");
						profileViewPLResponse.setTotalExperienceInMonths(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTotalExperienceMonth()) ?  applicantDetail.getTotalExperienceMonth().toString() : "-");
						profileViewPLResponse.setTotalExperienceInYears(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTotalExperienceYear()) ?  applicantDetail.getTotalExperienceYear().toString() : "-");
						profileViewPLResponse.setPreviousExperienceInMonths(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousJobMonth()) ?  applicantDetail.getPreviousJobMonth().toString() : "-");
						profileViewPLResponse.setPreviousExperienceInYears(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousJobYear()) ?  applicantDetail.getPreviousJobYear().toString() : "-");
						profileViewPLResponse.setPreviousEmployerName(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousEmployersName()) ?  applicantDetail.getPreviousEmployersName() : "-");
						profileViewPLResponse.setPreviousEmployerAddress(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousEmployersAddress()) ?  applicantDetail.getPreviousEmployersAddress() : "-");
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setModeOfReceipt(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getModeOfReceipt()) ? ModeOfRecipt.getById(applicantDetail.getModeOfReceipt()).getValue() : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getMonthlyIncome() != null ? applicantDetail.getMonthlyIncome().toString() : 0));

						break;
					case 3 : //Business
					case 4 : //Self Employed
						profileViewPLResponse.setEntityName(applicantDetail.getEntityName());
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getIndustryTypeId())){
							if(applicantDetail.getIndustryTypeId() != 16){
								profileViewPLResponse.setIndustryType(IndustryType.getById(applicantDetail.getIndustryTypeId()).getValue());
							}else{
								profileViewPLResponse.setIndustryType(applicantDetail.getIndustryTypeOther());
							}
						}
						profileViewPLResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
						profileViewPLResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
						profileViewPLResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
						profileViewPLResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
						profileViewPLResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
						profileViewPLResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
						profileViewPLResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getPatCurrentYear() != null ? applicantDetail.getPatCurrentYear().toString() : 0));

						break;
					case 5 ://Self Employed Professional
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getSelfEmployedOccupationId())){
							if(applicantDetail.getSelfEmployedOccupationId().intValue() != 10){
								profileViewPLResponse.setOccupation(Occupation.getById(applicantDetail.getSelfEmployedOccupationId()).getValue());
							}else{
								profileViewPLResponse.setOccupation(applicantDetail.getSelfEmployedOccupationOther());
							}
						}
						profileViewPLResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
						profileViewPLResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
						profileViewPLResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
						profileViewPLResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
						profileViewPLResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
						profileViewPLResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
						profileViewPLResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getPatCurrentYear() != null ? applicantDetail.getPatCurrentYear().toString() : 0));

						break;
					case 6://Agriculturist
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getLandSize())){
							profileViewPLResponse.setLandSize(LandSize.getById(applicantDetail.getLandSize().intValue()).getValue());
						}
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAlliedActivityId())){
							profileViewPLResponse.setAlliedActivity(AlliedActivity.getById(applicantDetail.getAlliedActivityId()).getValue());
						}
						profileViewPLResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
						profileViewPLResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
						profileViewPLResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
						profileViewPLResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
						profileViewPLResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
						profileViewPLResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
						profileViewPLResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getPatCurrentYear() != null ? applicantDetail.getPatCurrentYear().toString() : 0));

						break;
					case 7://Pensioner
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setModeOfReceipt(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getModeOfReceipt()) ? ModeOfRecipt.getById(applicantDetail.getModeOfReceipt()).getValue() : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getMonthlyIncome() != null ? applicantDetail.getMonthlyIncome().toString() : 0));
						break;
						default:
						break;
					}
				}

				//set pan car
				profileViewPLResponse.setPan(applicantDetail.getPan().toUpperCase());

				//get list of Pan Card
				try {
					profileViewPLResponse.setPanCardList(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_APPLICANT_SCANNED_COPY_OF_PAN_CARD));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}

				//get list of Aadhar Card
				try {
					profileViewPLResponse.setAadharCardList(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_APPLICANT_SCANNED_COPY_OF_AADHAR_CARD));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}

				//profile picture
				try {
					personalLoanResponse.setApplicantProfilePicture(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_PROFIEL_PICTURE));
				}catch (DocumentException e){
					logger.error(CommonUtils.EXCEPTION,e);
				}

				retailPrimaryViewResponse.setPersonalProfileRespoonse(profileViewPLResponse);
			} else {
				throw new LoansException("No Data found");
			}
		} catch (Exception e) {
			logger.error("Problem Occured while Fetching Retail Details : ",e);
			throw new LoansException("Problem Occured while Fetching Retail Details");
		}

		//set up loan specific details
		PrimaryPersonalLoanDetail loanDetail = personalLoanDetailRepository.getByApplicationAndUserId(applicantId, userId);
		if (loanDetail.getLoanPurpose() != 7 && loanDetail.getLoanPurpose() != null) {
			personalLoanResponse.setPurposeOfLoan(PersonalLoanPurpose.getById(Integer.valueOf(loanDetail.getLoanPurpose().toString())).getValue());
		} else {
			personalLoanResponse.setPurposeOfLoan(loanDetail.getLoanPurposeOther());
		}

		//setting co-application details
		List<RetailProfileViewResponse> coApplicantResponse = coApplicantService.getCoApplicantPLResponse(applicantId, userId,applicationMaster.getProductId());
		if (coApplicantResponse != null && !coApplicantResponse.isEmpty()) {
			retailPrimaryViewResponse.setCoApplicantResponse(coApplicantResponse);
		}

		//setting guarantor details
		List<RetailProfileViewResponse> garantorResponse = guarantorService.getGuarantorServiceResponse(applicantId, userId,applicationMaster.getProductId());
		if (garantorResponse != null && !garantorResponse.isEmpty()) {
			retailPrimaryViewResponse.setGarantorResponse(garantorResponse);
		}

		//setting Personal Loan Specific Data
		retailPrimaryViewResponse.setPersonalLoanResponse(personalLoanResponse);

		return retailPrimaryViewResponse;
	}

	@Override
	public RetailPrimaryViewResponse getHomeLoansPrimaryViewDetails(Long applicantId) throws LoansException {
		LoanApplicationMaster applicationMaster = loanApplicationRepository.findOne(applicantId);
		RetailPrimaryViewResponse retailPrimaryViewResponse = new RetailPrimaryViewResponse();
		PersonalLoanResponse personalLoanResponse = new PersonalLoanResponse();
		Long userId = applicationMaster.getUserId();
		//applicant
		RetailApplicantDetail applicantDetail = new RetailApplicantDetail();
		try {
			applicantDetail = applicantRepository.getByApplicationAndUserId(userId, applicantId);
			if (applicantDetail != null) {
				RetailProfileViewResponse profileViewPLResponse = new RetailProfileViewResponse();
				profileViewPLResponse.setCompanyName(applicantDetail.getCompanyName());
				personalLoanResponse.setDateOfProposal(CommonUtils.getStringDateFromDate(applicantDetail.getCreatedDate()));
				profileViewPLResponse.setFirstName(applicantDetail.getFirstName());
				try {
					profileViewPLResponse.setGender(Gender.getById(applicantDetail.getGenderId()).getValue());
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				profileViewPLResponse.setLastName(applicantDetail.getLastName());
				profileViewPLResponse.setMaritalStatus(applicantDetail.getStatusId() != null ? MaritalStatus.getById(applicantDetail.getStatusId()).getValue() : null);
				profileViewPLResponse.setMiddleName(applicantDetail.getMiddleName());
				if(applicantDetail.getOccupationId()!=null) {
					profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
				}
				profileViewPLResponse.setBonusPerAnnum(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBonusPerAnnum()) ? applicantDetail.getBonusPerAnnum().toString() : "-");
				profileViewPLResponse.setIncentivePerAnnum(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getIncentivePerAnnum()) ? applicantDetail.getIncentivePerAnnum().toString() : "-");
				profileViewPLResponse.setOtherIncome(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOtherIncome()) ? applicantDetail.getOtherIncome().toString() : "-");
				profileViewPLResponse.setOtherInvestment(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOtherInvestment()) ? applicantDetail.getOtherInvestment().toString() : "-");
				profileViewPLResponse.setTaxPaid(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTaxPaidLastYear()) ? applicantDetail.getTaxPaidLastYear().toString() : "-");
				
				//Office Address Data
				AddressResponse officeAddress = new AddressResponse();
				Long cityId = null ;
    			Integer stateId = null;
    			Integer countryId = null;
    			String cityName = null;
    			String stateName = null;
    			String countryName = null;
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCityId()))
    				cityId = applicantDetail.getOfficeCityId();
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeStateId()))
    				stateId = applicantDetail.getOfficeStateId();
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCountryId()))
    				countryId = applicantDetail.getOfficeCountryId();
    			
    			if(cityId != null || stateId != null || countryId != null) {
    				Map<String ,Object> mapData = commonService.getCityStateCountryNameFromOneForm(cityId, stateId, countryId);
    				if(mapData != null) {
    					cityName = mapData.get(CommonUtils.CITY_NAME).toString();
    					stateName = mapData.get(CommonUtils.STATE_NAME).toString();
    					countryName = mapData.get(CommonUtils.COUNTRY_NAME).toString();
    					
    					//set City
    					officeAddress.setCity(cityName != null ? cityName : "-");
    					
    					//set State
    					officeAddress.setState(stateName != null ? stateName : "-");
    					
    					//set Country
    					officeAddress.setCountry(countryName != null ? countryName : "-");
    				}
    			}
    			
    			officeAddress.setLandMark(applicantDetail.getOfficeLandMark());
				officeAddress.setPincode(applicantDetail.getOfficePincode() != null ? applicantDetail.getOfficePincode().toString() : null);
				officeAddress.setPremiseNumber(applicantDetail.getOfficePremiseNumberName());
				officeAddress.setStreetName(applicantDetail.getOfficeStreetName());
				personalLoanResponse.setOfficeAddress(officeAddress);

				/**try {
					List<Long> officeCity = new ArrayList<Long>(1);
					officeCity.add(applicantDetail.getOfficeCityId());
					OneFormResponse formResponse = oneFormClient.getCityByCityListId(officeCity);

					MasterResponse data = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponse.getListData().get(0), MasterResponse.class);
					officeAddress.setCity(data.getValue());
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCountryId())) {
						List<Long> officeCountry = new ArrayList<Long>(1);
						officeCountry.add(applicantDetail.getOfficeCountryId().longValue());
						OneFormResponse country = oneFormClient.getCountryByCountryListId(officeCountry);
						MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) country.getListData().get(0), MasterResponse.class);
						officeAddress.setCountry(dataCountry.getValue());
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeStateId())) {
						List<Long> officeState = new ArrayList<Long>(1);
						officeState.add(applicantDetail.getOfficeStateId().longValue());
						OneFormResponse state = oneFormClient.getStateByStateListId(officeState);
						MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) state.getListData().get(0), MasterResponse.class);
						officeAddress.setState(dataState.getValue());
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}*/
				
				//Permanent Address Data
				AddressResponse permanentAddress = new AddressResponse();
				cityId = null;
    			stateId = null;
    			countryId = null;
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentCityId()))
    				cityId = applicantDetail.getPermanentCityId();
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentStateId()))
    				stateId = applicantDetail.getPermanentStateId();
    			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentCountryId()))
    				countryId = applicantDetail.getPermanentCountryId();
    			
    			if(cityId != null || stateId != null || countryId != null) {
    				Map<String ,Object> mapData = commonService.getCityStateCountryNameFromOneForm(cityId, stateId, countryId);
    				if(mapData != null) {
    					cityName = mapData.get(CommonUtils.CITY_NAME).toString();
    					stateName = mapData.get(CommonUtils.STATE_NAME).toString();
    					countryName = mapData.get(CommonUtils.COUNTRY_NAME).toString();
    					
    					//set City
    					permanentAddress.setCity(cityName != null ? cityName : "-");
    					
    					//set State
    					permanentAddress.setState(stateName != null ? stateName : "-");
    					
    					//set Country
    					permanentAddress.setCountry(countryName != null ? countryName : "-");
    				}
    			}
    			
    			permanentAddress.setLandMark(applicantDetail.getPermanentLandMark());
				permanentAddress.setPincode(applicantDetail.getPermanentPincode() != null ? applicantDetail.getPermanentPincode().toString() : null);
				permanentAddress.setPremiseNumber(applicantDetail.getPermanentPremiseNumberName());
				permanentAddress.setStreetName(applicantDetail.getPermanentStreetName());
				personalLoanResponse.setPermanentAddress(permanentAddress);

				/**try {
					List<Long> permanentCity = new ArrayList<Long>(1);
					permanentCity.add(applicantDetail.getPermanentCityId());
					OneFormResponse formResponsePermanentCity = oneFormClient.getCityByCityListId(permanentCity);
					MasterResponse dataCity = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponsePermanentCity.getListData().get(0), MasterResponse.class);
					permanentAddress.setCity(dataCity.getValue());
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentCountryId())) {
						List<Long> permanentCountry = new ArrayList<Long>(1);
						permanentCountry.add(applicantDetail.getPermanentCountryId().longValue());
						OneFormResponse countryPermanent = oneFormClient.getCountryByCountryListId(permanentCountry);
						MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) countryPermanent.getListData().get(0), MasterResponse.class);
						permanentAddress.setCountry(dataCountry.getValue());
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentStateId())) {
						List<Long> permanentState = new ArrayList<Long>(1);
						permanentState.add(applicantDetail.getPermanentStateId().longValue());
						OneFormResponse statePermanent = oneFormClient.getStateByStateListId(permanentState);
						MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) statePermanent.getListData().get(0), MasterResponse.class);
						permanentAddress.setState(dataState.getValue());

					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}*/

				profileViewPLResponse.setTitle(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTitleId()) ? Title.getById(applicantDetail.getTitleId()).getValue() : null);
				profileViewPLResponse.setAge(applicantDetail.getBirthDate() != null ? CommonUtils.getAgeFromBirthDate(applicantDetail.getBirthDate()).toString() : null);

				if (applicantDetail.getApplicationId() != null) {
					personalLoanResponse.setTenure(applicantDetail.getApplicationId().getTenure() != null ? String.valueOf((applicantDetail.getApplicationId().getTenure()/12)) : null);
					personalLoanResponse.setLoanType(applicantDetail.getApplicationId().getProductId() != null ? LoanType.getById(applicantDetail.getApplicationId().getProductId()).getValue() : null);
					personalLoanResponse.setLoanAmount(applicantDetail.getApplicationId().getAmount() != null ? CommonUtils.CurrencyFormat(applicantDetail.getApplicationId().getAmount().toString()) : null);
					personalLoanResponse.setCurrency(applicantDetail.getCurrencyId() != null ? Currency.getById(applicantDetail.getCurrencyId()).getValue() : null);
				}

				profileViewPLResponse.setNatureOfOccupationId(applicantDetail.getOccupationId());
				if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOccupationId())){
					switch (applicantDetail.getOccupationId().intValue()) {
					case 2 : //Salaried
						profileViewPLResponse.setCompanyName(applicantDetail.getCompanyName());
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getEmployedWithId())){
							if(applicantDetail.getEmployedWithId() != 8){
								profileViewPLResponse.setEmployeeWith(EmployeeWith.getById(applicantDetail.getEmployedWithId()).getValue());
							}else{
								profileViewPLResponse.setEmployeeWith(applicantDetail.getEmployedWithOther());
							}
						}
						profileViewPLResponse.setYearsInCurrentJob(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getCurrentJobYear()) ?  applicantDetail.getCurrentJobYear().toString() : "-");
						profileViewPLResponse.setMonthsInCurrentJob(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getCurrentJobMonth()) ?  applicantDetail.getCurrentJobMonth().toString() : "-");
						profileViewPLResponse.setTotalExperienceInMonths(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTotalExperienceMonth()) ?  applicantDetail.getTotalExperienceMonth().toString() : "-");
						profileViewPLResponse.setTotalExperienceInYears(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTotalExperienceYear()) ?  applicantDetail.getTotalExperienceYear().toString() : "-");
						profileViewPLResponse.setPreviousExperienceInMonths(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousJobMonth()) ?  applicantDetail.getPreviousJobMonth().toString() : "-");
						profileViewPLResponse.setPreviousExperienceInYears(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousJobYear()) ?  applicantDetail.getPreviousJobYear().toString() : "-");
						profileViewPLResponse.setPreviousEmployerName(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousEmployersName()) ?  applicantDetail.getPreviousEmployersName() : "-");
						profileViewPLResponse.setPreviousEmployerAddress(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousEmployersAddress()) ?  applicantDetail.getPreviousEmployersAddress() : "-");
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setModeOfReceipt(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getModeOfReceipt()) ? ModeOfRecipt.getById(applicantDetail.getModeOfReceipt()).getValue() : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getMonthlyIncome() != null ? applicantDetail.getMonthlyIncome().toString() : 0));

						break;
					case 3 : //Business
					case 4 : //Self Employed
						profileViewPLResponse.setEntityName(applicantDetail.getEntityName());
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getIndustryTypeId())){
							if(applicantDetail.getIndustryTypeId() != 16){
								profileViewPLResponse.setIndustryType(IndustryType.getById(applicantDetail.getIndustryTypeId()).getValue());
							}else{
								profileViewPLResponse.setIndustryType(applicantDetail.getIndustryTypeOther());
							}
						}
						profileViewPLResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
						profileViewPLResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
						profileViewPLResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
						profileViewPLResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
						profileViewPLResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
						profileViewPLResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
						profileViewPLResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getPatCurrentYear() != null ? applicantDetail.getPatCurrentYear().toString() : 0));

						break;
					case 5 ://Self Employed Professional
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getSelfEmployedOccupationId())){
							if(applicantDetail.getSelfEmployedOccupationId().intValue() != 10){
								profileViewPLResponse.setOccupation(Occupation.getById(applicantDetail.getSelfEmployedOccupationId()).getValue());
							}else{
								profileViewPLResponse.setOccupation(applicantDetail.getSelfEmployedOccupationOther());
							}
						}
						profileViewPLResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
						profileViewPLResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
						profileViewPLResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
						profileViewPLResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
						profileViewPLResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
						profileViewPLResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
						profileViewPLResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getPatCurrentYear() != null ? applicantDetail.getPatCurrentYear().toString() : 0));

						break;
					case 6://Agriculturist
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getLandSize())){
							profileViewPLResponse.setLandSize(LandSize.getById(applicantDetail.getLandSize().intValue()).getValue());
						}
						if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAlliedActivityId())){
							profileViewPLResponse.setAlliedActivity(AlliedActivity.getById(applicantDetail.getAlliedActivityId()).getValue());
						}
						profileViewPLResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
						profileViewPLResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
						profileViewPLResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
						profileViewPLResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
						profileViewPLResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
						profileViewPLResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
						profileViewPLResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getPatCurrentYear() != null ? applicantDetail.getPatCurrentYear().toString() : 0));

						break;
					case 7://Pensioner
						profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
						profileViewPLResponse.setModeOfReceipt(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getModeOfReceipt()) ? ModeOfRecipt.getById(applicantDetail.getModeOfReceipt()).getValue() : "-");
						profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getMonthlyIncome() != null ? applicantDetail.getMonthlyIncome().toString() : 0));
						break;
						default:
						break;
					}
				}

				//set pan car
				profileViewPLResponse.setPan(applicantDetail.getPan().toUpperCase());

				//get list of Pan Card
				try {
					profileViewPLResponse.setPanCardList(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_APPLICANT_SCANNED_COPY_OF_PAN_CARD));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}

				//get list of Aadhar Card
				try {
					profileViewPLResponse.setAadharCardList(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_APPLICANT_SCANNED_COPY_OF_AADHAR_CARD));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}

				//profile picture
				try {
					personalLoanResponse.setApplicantProfilePicture(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_PROFIEL_PICTURE));
				}catch (DocumentException e){
					logger.error(CommonUtils.EXCEPTION,e);
				}

				retailPrimaryViewResponse.setPersonalProfileRespoonse(profileViewPLResponse);
			} else {
				throw new LoansException("No Data found");
			}
		} catch (Exception e) {
			logger.error("Problem Occured while Fetching Retail Details : ",e);
			throw new LoansException("Problem Occured while Fetching Retail Details");
		}

		//set up loan specific details
		if (applicantDetail.getLoanPurpose() != null && applicantDetail.getLoanPurpose() != 7) {
			personalLoanResponse.setPurposeOfLoan(PersonalLoanPurpose.getById(Integer.valueOf(applicantDetail.getLoanPurpose().toString())).getValue());
		} else {
			personalLoanResponse.setPurposeOfLoan(applicantDetail.getLoanPurposeOther());
		}

		//setting co-application details
		List<RetailProfileViewResponse> coApplicantResponse = coApplicantService.getCoApplicantPLResponse(applicantId, userId,applicationMaster.getProductId());
		if (coApplicantResponse != null && !coApplicantResponse.isEmpty()) {
			retailPrimaryViewResponse.setCoApplicantResponse(coApplicantResponse);
		}

		//setting guarantor details
		List<RetailProfileViewResponse> garantorResponse = guarantorService.getGuarantorServiceResponse(applicantId, userId,applicationMaster.getProductId());
		if (garantorResponse != null && !garantorResponse.isEmpty()) {
			retailPrimaryViewResponse.setGarantorResponse(garantorResponse);
		}

		//setting Personal Loan Specific Data
		retailPrimaryViewResponse.setPersonalLoanResponse(personalLoanResponse);

		return retailPrimaryViewResponse;
	}

}
