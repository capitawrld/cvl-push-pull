package com.capitaworld.service.loans.service.teaser.primaryview.impl;

import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.util.CommonUtil;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.PrimaryLapLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.RetailApplicantDetail;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.AddressResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.LapPrimaryViewResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.LapResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.RetailProfileViewResponse;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.PrimaryLapLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.common.CommonService;
import com.capitaworld.service.loans.service.common.DocumentManagementService;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.teaser.primaryview.LapPrimaryViewService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LapPrimaryViewServiceImpl implements LapPrimaryViewService{

	private static final Logger logger = LoggerFactory.getLogger(LapPrimaryViewServiceImpl.class);

	@Autowired
	private RetailApplicantDetailRepository applicantRepository;
	
	@Autowired
	private CoApplicantService coApplicantService;

	@Autowired
	private GuarantorService guarantorService;
	
	@Autowired
	private OneFormClient oneFormClient;
	
	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private PrimaryLapLoanDetailRepository primaryLapRepository;

    @Autowired
    private DocumentManagementService documentManagementService;
    
    @Autowired
    private CommonService commonService;

    protected static final String DMS_URL = "dmsURL";

	@Override
	public LapPrimaryViewResponse getLapPrimaryViewDetails(Long applicantId) throws LoansException {
		
		Long cityId = null ;
		Integer stateId = null;
		Integer countryId = null;
		String cityName = null;
		String stateName = null;
		String countryName = null;
		LapPrimaryViewResponse lapPrimaryViewResponse = new LapPrimaryViewResponse();
		LapResponse lapResponse = new LapResponse();
		//applicant
		LoanApplicationMaster applicationMaster = loanApplicationRepository.findOne(applicantId);
        try {
			RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(applicationMaster.getUserId(), applicantId);
			if (applicantDetail != null) {
				RetailProfileViewResponse profileViewLAPResponse = new RetailProfileViewResponse();
				lapResponse.setDateOfProposal(CommonUtils.getStringDateFromDate(applicantDetail.getCreatedDate()));
				if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getOccupationId())){
					profileViewLAPResponse.setNatureOfOccupationId(applicantDetail.getOccupationId());
					if (applicantDetail.getOccupationId() == 2){
						profileViewLAPResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getCompanyName())){
                        	profileViewLAPResponse.setCompanyName(applicantDetail.getCompanyName());
                        }else{
                        	profileViewLAPResponse.setCompanyName("-");
                        }
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getEmployedWithId())){
                            if (applicantDetail.getEmployedWithId() == 8){
                            	profileViewLAPResponse.setEmployeeWith(applicantDetail.getEmployedWithOther());
                            }else{
                            	profileViewLAPResponse.setEmployeeWith(EmployeeWith.getById(applicantDetail.getEmployedWithId()).getValue());
                            }
                        }else{
                        	profileViewLAPResponse.setEmployeeWith("-");
                        }
                        profileViewLAPResponse.setYearsInCurrentJob(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getCurrentJobYear()) ?  applicantDetail.getCurrentJobYear().toString() : "-");
                        profileViewLAPResponse.setMonthsInCurrentJob(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getCurrentJobMonth()) ?  applicantDetail.getCurrentJobMonth().toString() : "-");
                        profileViewLAPResponse.setTotalExperienceInMonths(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTotalExperienceMonth()) ?  applicantDetail.getTotalExperienceMonth().toString() : "-");
                        profileViewLAPResponse.setTotalExperienceInYears(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTotalExperienceYear()) ?  applicantDetail.getTotalExperienceYear().toString() : "-");
                        profileViewLAPResponse.setPreviousExperienceInMonths(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousJobMonth()) ?  applicantDetail.getPreviousJobMonth().toString() : "-");
                        profileViewLAPResponse.setPreviousExperienceInYears(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousJobYear()) ?  applicantDetail.getPreviousJobYear().toString() : "-");
                        profileViewLAPResponse.setPreviousEmployerName(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousEmployersName()) ?  applicantDetail.getPreviousEmployersName() : "-");
                        profileViewLAPResponse.setPreviousEmployerAddress(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPreviousEmployersAddress()) ?  applicantDetail.getPreviousEmployersAddress() : "-");
                        profileViewLAPResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
                        profileViewLAPResponse.setModeOfReceipt(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getModeOfReceipt()) ? ModeOfRecipt.getById(applicantDetail.getModeOfReceipt()).getValue() : "-");
                        profileViewLAPResponse.setMonthlyIncome((!CommonUtils.isObjectNullOrEmpty(String.valueOf(applicantDetail.getMonthlyIncome())) ? String.valueOf(applicantDetail.getMonthlyIncome()) : "0"));

                    }
                    else if (applicantDetail.getOccupationId() == 3 || applicantDetail.getOccupationId() == 4) {
                    	profileViewLAPResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getEntityName())){
                        	profileViewLAPResponse.setEntityName(applicantDetail.getEntityName());
                        }else{
                        	profileViewLAPResponse.setEntityName("-");
                        }
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getIndustryTypeId())){
                            if (applicantDetail.getIndustryTypeId()==16){
                            	profileViewLAPResponse.setIndustryType(applicantDetail.getIndustryTypeOther());
                            }else{
                            	profileViewLAPResponse.setIndustryType(IndustryType.getById(applicantDetail.getIndustryTypeId()).getValue());
                            }
                        }else{
                        	profileViewLAPResponse.setIndustryType("-");
                        }
                        profileViewLAPResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
                        profileViewLAPResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
                        profileViewLAPResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
                        profileViewLAPResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
                        profileViewLAPResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
                        profileViewLAPResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
                        profileViewLAPResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
                        profileViewLAPResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
                        profileViewLAPResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
                        profileViewLAPResponse.setMonthlyIncome((!CommonUtils.isObjectNullOrEmpty(String.valueOf(applicantDetail.getPatCurrentYear())) ? String.valueOf(applicantDetail.getPatCurrentYear()) : "0"));

                    }
                    else if(applicantDetail.getOccupationId()==5){
                    	profileViewLAPResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        if(!CommonUtil.isObjectNullOrEmpty(applicantDetail.getSelfEmployedOccupationId())){
                        	if (applicantDetail.getSelfEmployedOccupationId()==10){
                        		profileViewLAPResponse.setOccupation(applicantDetail.getSelfEmployedOccupationOther());
                            }else{
                            	profileViewLAPResponse.setOccupation(Occupation.getById(applicantDetail.getSelfEmployedOccupationId()).getValue());
                            }	
                        }else{
                        	profileViewLAPResponse.setOccupation("-");
                        }
                        profileViewLAPResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
                        profileViewLAPResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
                        profileViewLAPResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
                        profileViewLAPResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
                        profileViewLAPResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
                        profileViewLAPResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
                        profileViewLAPResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
                        profileViewLAPResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
                        profileViewLAPResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
                        profileViewLAPResponse.setMonthlyIncome((!CommonUtils.isObjectNullOrEmpty(String.valueOf(applicantDetail.getPatCurrentYear())) ? String.valueOf(applicantDetail.getPatCurrentYear()) : "0"));

                    }else if(applicantDetail.getOccupationId()==6){
                    	profileViewLAPResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getLandSize())){                          
                        	profileViewLAPResponse.setLandSize(LandSize.getById(applicantDetail.getLandSize().intValue()).getValue());
                        }else{
                        	profileViewLAPResponse.setLandSize("-");
                        }
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getAlliedActivityId())){
                        	profileViewLAPResponse.setAlliedActivity(AlliedActivity.getById(applicantDetail.getAlliedActivityId()).getValue());
                        }else{
                        	profileViewLAPResponse.setAlliedActivity("-");
                        }
                        profileViewLAPResponse.setAnnualTurnover(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAnnualTurnover()) ? CommonUtils.CurrencyFormat( applicantDetail.getAnnualTurnover().toString()) : "-");
                        profileViewLAPResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
                        profileViewLAPResponse.setPatPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatPreviousYear().toString()): "-");
                        profileViewLAPResponse.setPatCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPatCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getPatCurrentYear().toString()): "-");
                        profileViewLAPResponse.setDepreciationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationPreviousYear().toString()): "-");
                        profileViewLAPResponse.setDepreciationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getDepreciationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getDepreciationCurrentYear().toString()): "-");
                        profileViewLAPResponse.setRemunerationPreviousYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationPreviousYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationPreviousYear().toString()): "-");
                        profileViewLAPResponse.setRemunerationCurrentYear(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRemunerationCurrentYear()) ? CommonUtils.CurrencyFormat( applicantDetail.getRemunerationCurrentYear().toString()): "-");
                        profileViewLAPResponse.setBusinessExperience(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBusinessStartDate()) ? CommonUtils.calculateBusinessExperience(applicantDetail.getBusinessStartDate()) : "-");
                        profileViewLAPResponse.setMonthlyIncome((!CommonUtils.isObjectNullOrEmpty(String.valueOf(applicantDetail.getPatCurrentYear())) ? String.valueOf(applicantDetail.getPatCurrentYear()) : "0"));

                    }else if(applicantDetail.getOccupationId()==7){
                    	profileViewLAPResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        profileViewLAPResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
                        profileViewLAPResponse.setModeOfReceipt(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getModeOfReceipt()) ? ModeOfRecipt.getById(applicantDetail.getModeOfReceipt()).getValue() : "-");
                        profileViewLAPResponse.setMonthlyIncome((!CommonUtils.isObjectNullOrEmpty(String.valueOf(applicantDetail.getMonthlyIncome())) ? String.valueOf(applicantDetail.getMonthlyIncome()) : "0"));

                    }                   
                }else{
                	profileViewLAPResponse.setNatureOfOccupation("-");
                }
				profileViewLAPResponse.setFirstName((!CommonUtils.isObjectNullOrEmpty(applicantDetail.getFirstName()) ? applicantDetail.getFirstName() : null));
				profileViewLAPResponse.setMiddleName((!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMiddleName()) ? applicantDetail.getMiddleName() : null));
				profileViewLAPResponse.setLastName((!CommonUtils.isObjectNullOrEmpty(applicantDetail.getLastName()) ? applicantDetail.getLastName() : null));
				profileViewLAPResponse.setGender((!CommonUtils.isObjectNullOrEmpty(applicantDetail.getGenderId()) ? Gender.getById(applicantDetail.getGenderId()).getValue() : null));
				profileViewLAPResponse.setMaritalStatus((!CommonUtils.isObjectNullOrEmpty(applicantDetail.getStatusId()) ? MaritalStatus.getById(applicantDetail.getStatusId()).getValue() : null));

                profileViewLAPResponse.setBonusPerAnnum(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBonusPerAnnum()) ? applicantDetail.getBonusPerAnnum().toString() : "-");
                profileViewLAPResponse.setIncentivePerAnnum(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getIncentivePerAnnum()) ? applicantDetail.getIncentivePerAnnum().toString() : "-");
                profileViewLAPResponse.setOtherIncome(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOtherIncome()) ? applicantDetail.getOtherIncome().toString() : "-");
                profileViewLAPResponse.setOtherInvestment(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOtherInvestment()) ? applicantDetail.getOtherInvestment().toString() : "-");
                profileViewLAPResponse.setTaxPaid(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTaxPaidLastYear()) ? applicantDetail.getTaxPaidLastYear().toString() : "-");
                
				//set office address
                AddressResponse officeAddress = new AddressResponse();
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
                lapResponse.setOfficeAddress(officeAddress);

                /**try {
                    List<Long> officeCity = new ArrayList<Long>(1);
                    if(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCityId())){
                    	officeCity.add(applicantDetail.getOfficeCityId());
                        OneFormResponse formResponse = oneFormClient.getCityByCityListId(officeCity);
                        MasterResponse data = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponse.getListData().get(0), MasterResponse.class);
                        if(!CommonUtils.isObjectNullOrEmpty(data)){
                        	officeAddress.setCity(data.getValue());	
                        }else{
                        	officeAddress.setCity("-");
                        }	
                    }else{
                       	officeAddress.setCity("-");
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }
                try {
                    List<Long> officeCountry = new ArrayList<Long>(1);
                    Long officeCountryLong = null;
                    if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCountryId())) {
                        officeCountryLong = Long.valueOf(applicantDetail.getOfficeCountryId().toString());

                        officeCountry.add(officeCountryLong);
                        OneFormResponse country = oneFormClient.getCountryByCountryListId(officeCountry);
                        MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) country.getListData().get(0), MasterResponse.class);
                        if(!CommonUtils.isObjectNullOrEmpty(dataCountry.getValue())){
                        	officeAddress.setCountry(dataCountry.getValue());
                        }else{
                        	officeAddress.setCountry("-");
                        }
                    }else{
                    	officeAddress.setCountry("-");
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);

                }
                try {
                    List<Long> officeState = new ArrayList<Long>(1);
                    Long officeStateLong = null;
                    if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOfficeCountryId())) {
                        officeStateLong = Long.valueOf(applicantDetail.getOfficeStateId().toString());

                        officeState.add(officeStateLong);
                        OneFormResponse state = oneFormClient.getStateByStateListId(officeState);
                        MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) state.getListData().get(0), MasterResponse.class);
                        if(!CommonUtil.isObjectNullOrEmpty(dataState)){
                        	officeAddress.setState(dataState.getValue());	
                        }else{
                        	officeAddress.setState("-");
                        }
                    }else{
                    	officeAddress.setState("-");
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }*/
                
                //set permanent address
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
                lapResponse.setPermanentAddress(permanentAddress);
                
                /**try {
                    List<Long> permanentCity = new ArrayList<Long>(1);
                    if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentCityId())) {
                    	permanentCity.add(applicantDetail.getPermanentCityId());
                        OneFormResponse formResponsePermanentCity = oneFormClient.getCityByCityListId(permanentCity);
                        MasterResponse dataCity = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponsePermanentCity.getListData().get(0), MasterResponse.class);
                        if(!CommonUtils.isObjectNullOrEmpty(dataCity)){
                        	permanentAddress.setCity(dataCity.getValue());	
                        }else{
                        	permanentAddress.setCity("-");
                        }
                    }else{
                    	permanentAddress.setCity("-");
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }
                try {
                    List<Long> permanentCountry = new ArrayList<Long>(1);
                    Long permanentCountryLong = null;
                    if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentCountryId())) {
                        permanentCountryLong = Long.valueOf(applicantDetail.getPermanentCountryId().toString());
                        permanentCountry.add(permanentCountryLong);
                        OneFormResponse countryPermanent = oneFormClient.getCountryByCountryListId(permanentCountry);
                        MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) countryPermanent.getListData().get(0), MasterResponse.class);
                        if(!CommonUtils.isObjectNullOrEmpty(dataCountry)){
                        	permanentAddress.setCountry(dataCountry.getValue());	
                        }else{
                        	permanentAddress.setCountry("-");
                        }
                    }else{
                    	permanentAddress.setCountry("-");
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }
                try {
                    List<Long> permanentState = new ArrayList<Long>(1);
                    Long permanentStateLong = null;
                    if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getPermanentStateId())) {
                        permanentStateLong = Long.valueOf(applicantDetail.getPermanentStateId().toString());
                        permanentState.add(permanentStateLong);
                        OneFormResponse statePermanent = oneFormClient.getStateByStateListId(permanentState);
                        MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) statePermanent.getListData().get(0), MasterResponse.class);
                        if (!CommonUtils.isObjectNullOrEmpty(dataState)){
                        	permanentAddress.setState(dataState.getValue());	
                        }else{
                        	permanentAddress.setCountry("-");	
                        }
                    }else{
                    	permanentAddress.setCountry("-");
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }*/

				profileViewLAPResponse.setTitle(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTitleId()) ? Title.getById(applicantDetail.getTitleId()).getValue() : null);
				profileViewLAPResponse.setAge(applicantDetail.getBirthDate() != null ? CommonUtils.getAgeFromBirthDate(applicantDetail.getBirthDate()).toString() : null);
                lapResponse.setLoanType(applicationMaster.getProductId()!=null?LoanType.getById(applicationMaster.getProductId()).getValue():null);
				lapResponse.setCurrency(applicantDetail.getCurrencyId() != null ? Currency.getById(applicantDetail.getCurrencyId()).getValue() : null);

				profileViewLAPResponse.setEntityName(applicantDetail.getEntityName());

				//set pan 
				profileViewLAPResponse.setPan(applicantDetail.getPan());

				//applicant profile image
				try {
					lapResponse.setProfileImage(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.LAP_LOAN_PROFIEL_PICTURE));
				} catch (DocumentException e) {
                    logger.error(CommonUtils.EXCEPTION,e);
				}
				
				//get list of Pan Card
				try {
					profileViewLAPResponse.setPanCardList(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.LAP_LOAN_APPLICANT_SCANNED_COPY_OF_PAN_CARD));
				} catch (DocumentException e) {
                    logger.error(CommonUtils.EXCEPTION,e);
				}

				//get list of Aadhar Card
				try {
					profileViewLAPResponse.setAadharCardList(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.LAP_LOAN_APPLICANT_SCANNED_COPY_OF_AADHAR_CARD));
				} catch (DocumentException e) {
                    logger.error(CommonUtils.EXCEPTION,e);
				}
				lapPrimaryViewResponse.setApplicant(profileViewLAPResponse);
			} else {
				throw new LoansException("No Data found");
			}
		} catch (Exception e) {
            logger.error("Problem Occured while Fetching Retail Details : ",e);
			throw new LoansException("Problem Occured while Fetching Retail Details");
		}

		//set up loan specific details
		PrimaryLapLoanDetail loanDetail = primaryLapRepository.getByApplicationAndUserId(applicantId,applicationMaster.getUserId());
		
		if(!CommonUtils.isObjectNullOrEmpty(loanDetail.getPropertyType())){
			if(!CommonUtils.isObjectNullOrEmpty(loanDetail.getLoanPurpose())){
				lapResponse.setLoanPurpose(LoanPurpose.getById(loanDetail.getLoanPurpose()).getValue());
				if(loanDetail.getLoanPurpose() == 5){
					lapResponse.setLoanPurposeOther(!CommonUtils.isObjectNullOrEmpty(loanDetail.getLoanPurposeOther()) ? loanDetail.getLoanPurposeOther() : null);
				}
			}
			lapResponse.setTenure(!CommonUtils.isObjectNullOrEmpty(loanDetail.getTenure()) ? String.valueOf(loanDetail.getTenure()/12) : null);
			if(!CommonUtils.isObjectNullOrEmpty(loanDetail.getPropertyType())){
				lapResponse.setPropertyType(PropertyType.getById(loanDetail.getPropertyType()).getValue());
				if(loanDetail.getPropertyType() == 4){
					lapResponse.setPropertyTypeOther(!CommonUtils.isObjectNullOrEmpty(loanDetail.getPropertyTypeOther()) ? loanDetail.getPropertyTypeOther() : null);
				}
			}
			
			if(!CommonUtils.isObjectNullOrEmpty(loanDetail.getOccupationStatus())){
				lapResponse.setOccupationStatus(OccupationStatus.getById(loanDetail.getOccupationStatus()).getValue());
				if(loanDetail.getOccupationStatus() == 5){
					lapResponse.setOccupationStatusOther(!CommonUtils.isObjectNullOrEmpty(loanDetail.getOccupationStatusOther()) ? loanDetail.getOccupationStatusOther() : null);
				}
			}
			
			lapResponse.setPropertyAgeInMonths(!CommonUtils.isObjectNullOrEmpty(loanDetail.getPropertyAgeInMonth()) ? loanDetail.getPropertyAgeInMonth().toString() : null);
			lapResponse.setPropertyAgeInYears(!CommonUtils.isObjectNullOrEmpty(loanDetail.getPropertyAgeInYear()) ? loanDetail.getPropertyAgeInYear().toString() : null);
			lapResponse.setTotalArea(!CommonUtils.isObjectNullOrEmpty(loanDetail.getLandArea()) ? loanDetail.getLandArea().toString() : null);
			lapResponse.setBuiltUpArea(!CommonUtils.isObjectNullOrEmpty(loanDetail.getBuiltUpArea()) ? loanDetail.getBuiltUpArea().toString() : null);
			lapResponse.setPropertyValue(!CommonUtils.isObjectNullOrEmpty(loanDetail.getPropertyValue()) ? loanDetail.getPropertyValue().toString() : null);
			lapResponse.setPropertyOwnerName(!CommonUtils.isObjectNullOrEmpty(loanDetail.getOwnerName()) ? loanDetail.getOwnerName() : null);
			lapResponse.setPropertyPremiseNumber(!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressPremise()) ? loanDetail.getAddressPremise() : null);
			lapResponse.setPropertyStreetName(!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressStreet()) ? loanDetail.getAddressStreet() : null);
			lapResponse.setPropertyLandmark(!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressLandmark()) ? loanDetail.getAddressLandmark() : null);
			
			cityId = null;
			stateId = null;
			countryId = null;
			if (!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressCity()))
				cityId = loanDetail.getAddressCity().longValue();
			if (!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressState()))
				stateId = loanDetail.getAddressState();
			if (!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressCountry()))
				countryId = loanDetail.getAddressCountry();
			
			if(cityId != null || stateId != null || countryId != null) {
				Map<String ,Object> mapData = commonService.getCityStateCountryNameFromOneForm(cityId, stateId, countryId);
				if(mapData != null) {
					cityName = mapData.get(CommonUtils.CITY_NAME).toString();
					stateName = mapData.get(CommonUtils.STATE_NAME).toString();
					countryName = mapData.get(CommonUtils.COUNTRY_NAME).toString();
					
					//set City
					lapResponse.setPropertyCity(cityName != null ? cityName : "-");
					
					//set State
					lapResponse.setPropertyState(stateName != null ? stateName : "-");
					
					//set Country
					lapResponse.setPropertyCountry(countryName != null ? countryName : "-");
				}
			}
			
            /**try {
                List<Long> officeCity = new ArrayList<Long>(1);
                if(!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressCity())){
                	officeCity.add(Long.valueOf(loanDetail.getAddressCity().toString()));
                    OneFormResponse formResponse = oneFormClient.getCityByCityListId(officeCity);
                    MasterResponse data = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponse.getListData().get(0), MasterResponse.class);
                    if(!CommonUtils.isObjectNullOrEmpty(data)){
                    	lapResponse.setPropertyCity(data.getValue());
                    }else{
                    	lapResponse.setPropertyCity("-");
                    }	
                }else{
                	lapResponse.setPropertyCity("-");
                }
            } catch (Exception e) {
                logger.error(CommonUtils.EXCEPTION,e);
            }
            try {
                List<Long> officeCountry = new ArrayList<Long>(1);
                Long officeCountryLong = null;
                if (!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressCountry())) {
                    officeCountryLong = Long.valueOf(loanDetail.getAddressCountry().toString());

                    officeCountry.add(officeCountryLong);
                    OneFormResponse country = oneFormClient.getCountryByCountryListId(officeCountry);
                    MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) country.getListData().get(0), MasterResponse.class);
                    if(!CommonUtils.isObjectNullOrEmpty(dataCountry.getValue())){
                    	lapResponse.setPropertyCountry(dataCountry.getValue());
                    }else{
                    	lapResponse.setPropertyCountry("-");
                    }
                }else{
                	lapResponse.setPropertyCountry("-");
                }
            } catch (Exception e) {
                logger.error(CommonUtils.EXCEPTION,e);
            }
            try {
                List<Long> officeState = new ArrayList<Long>(1);
                Long officeStateLong = null;
                if (!CommonUtils.isObjectNullOrEmpty(loanDetail.getAddressState())) {
                    officeStateLong = Long.valueOf(loanDetail.getAddressState().toString());

                    officeState.add(officeStateLong);
                    OneFormResponse state = oneFormClient.getStateByStateListId(officeState);
                    MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) state.getListData().get(0), MasterResponse.class);
                    if(!CommonUtil.isObjectNullOrEmpty(dataState)){
                    	lapResponse.setPropertyState(dataState.getValue());
                    }else{
                    	lapResponse.setPropertyState("-");
                    }
                }else{
                	lapResponse.setPropertyState(null);
                }
            } catch (Exception e) {
                logger.error(CommonUtils.EXCEPTION,e);
            }*/
			
			lapResponse.setPropertyPincode(!CommonUtils.isObjectNullOrEmpty(loanDetail.getPincode()) ? loanDetail.getPincode().toString() : null);
		}
		
		lapResponse.setLoanAmount(!CommonUtils.isObjectNullOrEmpty(loanDetail.getAmount()) ? loanDetail.getAmount().toString() : null);
		lapPrimaryViewResponse.setLapResponse(lapResponse);
		
		//setting co-application details
		List<RetailProfileViewResponse> coApplicantResponse = null;
		try {
			coApplicantResponse = coApplicantService.getCoApplicantPLResponse(applicantId, applicationMaster.getUserId(),applicationMaster.getProductId());
		} catch (Exception e) {
            logger.error(CommonUtils.EXCEPTION,e);
		}
		if (coApplicantResponse != null && !coApplicantResponse.isEmpty()) {
            lapPrimaryViewResponse.setCoApplicantList(coApplicantResponse);
        }

		//setting guarantor details
		List<RetailProfileViewResponse> garantorResponse = null;
		try {
			garantorResponse = guarantorService.getGuarantorServiceResponse(applicantId, applicationMaster.getUserId(),applicationMaster.getProductId());
		} catch (Exception e) {
            logger.error(CommonUtils.EXCEPTION,e);
		}
		if (garantorResponse != null && !garantorResponse.isEmpty()) {
            lapPrimaryViewResponse.setGuarantorList(garantorResponse);
        }
		
		return lapPrimaryViewResponse;
	}
	
	
}
