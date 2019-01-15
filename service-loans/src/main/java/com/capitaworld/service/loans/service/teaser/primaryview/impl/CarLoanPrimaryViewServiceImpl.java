package com.capitaworld.service.loans.service.teaser.primaryview.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.oneform.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.util.CommonUtil;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.RetailApplicantDetail;
import com.capitaworld.service.loans.model.AddressResponse;
import com.capitaworld.service.loans.model.retail.PrimaryCarLoanDetailRequest;
import com.capitaworld.service.loans.model.teaser.primaryview.CarLoanPrimaryViewResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.CarLoanResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.RetailProfileViewResponse;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.common.DocumentManagementService;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.fundseeker.retail.PrimaryCarLoanService;
import com.capitaworld.service.loans.service.teaser.primaryview.CarLoanPrimaryViewService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;

@Service
@Transactional
/**
 * Created by dhaval on 23-May-17.
 */
public class CarLoanPrimaryViewServiceImpl implements CarLoanPrimaryViewService{

    private static final Logger logger = LoggerFactory.getLogger(CarLoanPrimaryViewServiceImpl.class);

    @Autowired
    private RetailApplicantDetailRepository applicantRepository;

    @Autowired
    private CoApplicantService coApplicantService;

    @Autowired
    private GuarantorService guarantorService;

    @Autowired
    private PrimaryCarLoanService primaryCarLoanService;

    @Autowired
    private OneFormClient oneFormClient;
    
    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private DocumentManagementService documentManagementService;

    protected static final String DMS_URL = "dmsURL";

    @Override
    public CarLoanPrimaryViewResponse getCarLoanPrimaryViewDetails(Long toApplicationId) {
        CarLoanPrimaryViewResponse carLoanPrimaryViewResponse = new CarLoanPrimaryViewResponse();
        CarLoanResponse carLoanResponse = new CarLoanResponse();
        LoanApplicationMaster applicationMaster = loanApplicationRepository.findOne(toApplicationId);
        Long userId = applicationMaster.getUserId();
        //applicant
        try {
            RetailApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(userId, toApplicationId);
            if (applicantDetail != null) {
                RetailProfileViewResponse profileViewPLResponse = new RetailProfileViewResponse();
                carLoanResponse.setDateOfProposal(CommonUtils.getStringDateFromDate(applicantDetail.getCreatedDate()));
                if (applicantDetail.getApplicationId() != null) {
                    carLoanResponse.setTenure(applicantDetail.getApplicationId().getTenure() != null ? String.valueOf(applicantDetail.getApplicationId().getTenure()/12) : null);
                    carLoanResponse.setLoanType(applicantDetail.getApplicationId().getProductId() != null ? LoanType.getById(applicantDetail.getApplicationId().getProductId()).getValue() : null);
                    carLoanResponse.setLoanAmount(applicantDetail.getApplicationId().getAmount() != null ? applicantDetail.getApplicationId().getAmount() : null);
                    carLoanResponse.setCurrency(applicantDetail.getCurrencyId() != null ? Currency.getById(applicantDetail.getCurrencyId()).getValue() : null);
                }
                if (applicantDetail.getOccupationId()!=null){
                    profileViewPLResponse.setNatureOfOccupationId(applicantDetail.getOccupationId());
                    if (applicantDetail.getOccupationId()==2){
                        profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getCompanyName())){
                            profileViewPLResponse.setCompanyName(applicantDetail.getCompanyName());
                        }
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getEmployedWithId())){
                            if (applicantDetail.getEmployedWithId()==8){
                                profileViewPLResponse.setEmployeeWith(applicantDetail.getEmployedWithOther());
                            }else{
                                profileViewPLResponse.setEmployeeWith(EmployeeWith.getById(applicantDetail.getEmployedWithId()).getValue());
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

                    }
                    else if (applicantDetail.getOccupationId() == 3 || applicantDetail.getOccupationId() == 4) {
                        profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getEntityName())){
                            profileViewPLResponse.setEntityName(applicantDetail.getEntityName());
                        }
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getIndustryTypeId())){
                            if (applicantDetail.getIndustryTypeId()==16){
                                profileViewPLResponse.setIndustryType(applicantDetail.getIndustryTypeOther());
                            }else{
                                profileViewPLResponse.setIndustryType(IndustryType.getById(applicantDetail.getIndustryTypeId()).getValue());
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

                    }
                    else if(applicantDetail.getOccupationId()==5){
                        profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        if (applicantDetail.getSelfEmployedOccupationId()==10){
                            profileViewPLResponse.setOccupation(applicantDetail.getSelfEmployedOccupationOther());
                        }else{
                            profileViewPLResponse.setOccupation(Occupation.getById(applicantDetail.getSelfEmployedOccupationId()).getValue());
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

                    }else if(applicantDetail.getOccupationId()==6){
                    	profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getLandSize())){                          
                            profileViewPLResponse.setLandSize(LandSize.getById(applicantDetail.getLandSize().intValue()).getValue());
                        }
                        if (!CommonUtil.isObjectNullOrEmpty(applicantDetail.getAlliedActivityId())){
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

                    }else if(applicantDetail.getOccupationId()==7){
                    	profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
                        profileViewPLResponse.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getMonthlyLoanObligation()) ? CommonUtils.CurrencyFormat( applicantDetail.getMonthlyLoanObligation().toString()): "-");
                        profileViewPLResponse.setModeOfReceipt(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getModeOfReceipt()) ? ModeOfRecipt.getById(applicantDetail.getModeOfReceipt()).getValue() : "-");
                        profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getMonthlyIncome() != null ? applicantDetail.getMonthlyIncome().toString() : 0));

                    }                   
                }

                //set pan car
                profileViewPLResponse.setPan(applicantDetail.getPan() != null ? applicantDetail.getPan().toUpperCase() : null);
                profileViewPLResponse.setTitle(applicantDetail.getTitleId() != null ? Title.getById(applicantDetail.getTitleId()).getValue() : null);
                profileViewPLResponse.setAge(applicantDetail.getBirthDate() != null ? CommonUtils.getAgeFromBirthDate(applicantDetail.getBirthDate()).toString() : null);
                profileViewPLResponse.setFirstName(applicantDetail.getFirstName() != null ? applicantDetail.getFirstName() : null);
                profileViewPLResponse.setGender(applicantDetail.getGenderId() != null ? Gender.getById(applicantDetail.getGenderId()).getValue() : null);
                profileViewPLResponse.setLastName(applicantDetail.getLastName() != null ? applicantDetail.getLastName() : null);
                profileViewPLResponse.setMaritalStatus(applicantDetail.getStatusId() != null ? MaritalStatus.getById(applicantDetail.getStatusId()).getValue() : null);
                profileViewPLResponse.setMiddleName(applicantDetail.getMiddleName() != null ? applicantDetail.getMiddleName() : null);

                profileViewPLResponse.setBonusPerAnnum(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getBonusPerAnnum()) ? applicantDetail.getBonusPerAnnum().toString() : "-");
                profileViewPLResponse.setIncentivePerAnnum(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getIncentivePerAnnum()) ? applicantDetail.getIncentivePerAnnum().toString() : "-");
                profileViewPLResponse.setOtherIncome(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOtherIncome()) ? applicantDetail.getOtherIncome().toString() : "-");
                profileViewPLResponse.setOtherInvestment(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getOtherInvestment()) ? applicantDetail.getOtherInvestment().toString() : "-");
                profileViewPLResponse.setTaxPaid(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getTaxPaidLastYear()) ? applicantDetail.getTaxPaidLastYear().toString() : "-");


                //set office address
                AddressResponse officeAddress = new AddressResponse();
                try {
                    List<Long> officeCity = new ArrayList<Long>(1);
                    officeCity.add(applicantDetail.getOfficeCityId());
                    OneFormResponse formResponse = oneFormClient.getCityByCityListId(officeCity);

                    MasterResponse data = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponse.getListData().get(0), MasterResponse.class);
                    officeAddress.setCity(data.getValue());
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }
                try {
                    List<Long> officeCountry = new ArrayList<Long>(1);
                    Long officeCountryLong = null;
                    if (applicantDetail.getOfficeCountryId() != null) {
                        officeCountryLong = Long.valueOf(applicantDetail.getOfficeCountryId().toString());

                        officeCountry.add(officeCountryLong);
                        OneFormResponse country = oneFormClient.getCountryByCountryListId(officeCountry);
                        MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) country.getListData().get(0), MasterResponse.class);
                        officeAddress.setCountry(dataCountry.getValue());
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);

                }
                try {
                    List<Long> officeState = new ArrayList<Long>(1);
                    Long officeStateLong = null;
                    if (applicantDetail.getOfficeCountryId() != null) {
                        officeStateLong = Long.valueOf(applicantDetail.getOfficeStateId().toString());

                        officeState.add(officeStateLong);
                        OneFormResponse state = oneFormClient.getStateByStateListId(officeState);
                        MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) state.getListData().get(0), MasterResponse.class);
                        officeAddress.setState(dataState.getValue());
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }
                officeAddress.setLandMark(applicantDetail.getOfficeLandMark());
                officeAddress.setPincode(applicantDetail.getOfficePincode() != null ? applicantDetail.getOfficePincode().toString() : null);
                officeAddress.setPremiseNumber(applicantDetail.getOfficePremiseNumberName());
                officeAddress.setStreetName(applicantDetail.getOfficeStreetName());
                carLoanResponse.setOfficeAddress(officeAddress);

                //set permanent address
                AddressResponse permanentAddress = new AddressResponse();
                try {
                    List<Long> permanentCity = new ArrayList<Long>(1);
                    permanentCity.add(applicantDetail.getPermanentCityId());
                    OneFormResponse formResponsePermanentCity = oneFormClient.getCityByCityListId(permanentCity);
                    MasterResponse dataCity = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponsePermanentCity.getListData().get(0), MasterResponse.class);
                    permanentAddress.setCity(dataCity.getValue());
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }
                try {
                    List<Long> permanentCountry = new ArrayList<Long>(1);
                    Long permanentCountryLong = null;
                    if (applicantDetail.getPermanentCountryId() != null) {
                        permanentCountryLong = Long.valueOf(applicantDetail.getPermanentCountryId().toString());
                        permanentCountry.add(permanentCountryLong);
                        OneFormResponse countryPermanent = oneFormClient.getCountryByCountryListId(permanentCountry);
                        MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) countryPermanent.getListData().get(0), MasterResponse.class);
                        permanentAddress.setCountry(dataCountry.getValue());
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }
                try {
                    List<Long> permanentState = new ArrayList<Long>(1);
                    Long permanentStateLong = null;
                    if (applicantDetail.getPermanentStateId() != null) {
                        permanentStateLong = Long.valueOf(applicantDetail.getPermanentStateId().toString());
                        permanentState.add(permanentStateLong);
                        OneFormResponse statePermanent = oneFormClient.getStateByStateListId(permanentState);
                        MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) statePermanent.getListData().get(0), MasterResponse.class);
                        permanentAddress.setState(dataState.getValue());
                    }
                } catch (Exception e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }
                permanentAddress.setLandMark(applicantDetail.getPermanentLandMark());
                permanentAddress.setPincode(applicantDetail.getPermanentPincode() != null ? applicantDetail.getPermanentPincode().toString() : null);
                permanentAddress.setPremiseNumber(applicantDetail.getPermanentPremiseNumberName());
                permanentAddress.setStreetName(applicantDetail.getPermanentStreetName());
                carLoanResponse.setPermanentAddress(permanentAddress);

                //get list of Pan Card
                try {
                    profileViewPLResponse.setPanCardList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.CAR_LOAN_APPLICANT_SCANNED_COPY_OF_PAN_CARD));
                } catch (DocumentException e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }

                //get list of Aadhar Card
                try {
                    profileViewPLResponse.setAadharCardList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.CAR_LOAN_APPLICANT_SCANNED_COPY_OF_AADHAR_CARD));
                } catch (DocumentException e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }

                //get profile picture
                try {
                    carLoanResponse.setApplicantProfilePicture(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.CAR_LOAN_PROFIEL_PICTURE));
                } catch (DocumentException e) {
                    logger.error(CommonUtils.EXCEPTION,e);
                }

                carLoanPrimaryViewResponse.setApplicant(profileViewPLResponse);
            } else {
                throw new LoansException("No Data found");
            }
        } catch (Exception e) {
            logger.error("Problem Occured while Fetching Retail Details : ",e);
           // throw new Exception("Problem Occured while Fetching Retail Details");
        }

        //setting co-application details
        List<RetailProfileViewResponse> coApplicantResponse = null;
        try {
            coApplicantResponse = coApplicantService.getCoApplicantPLResponse(toApplicationId, userId,applicationMaster.getProductId());
        } catch (Exception e) {
            logger.error(CommonUtils.EXCEPTION,e);
        }
        carLoanPrimaryViewResponse.setCoApplicantList(coApplicantResponse);

        //setting guarantor details
        List<RetailProfileViewResponse> guarantorResponse = null;
        try {
            guarantorResponse = guarantorService.getGuarantorServiceResponse(toApplicationId, userId,applicationMaster.getProductId());
        } catch (Exception e) {
            logger.error(CommonUtils.EXCEPTION,e);
        }
        if (guarantorResponse != null && !guarantorResponse.isEmpty()) {
            carLoanPrimaryViewResponse.setGuarantorList(guarantorResponse);
        }

        //setting Personal Loan Specific Data
        try {
            PrimaryCarLoanDetailRequest primaryCarLoanDetailRequest = primaryCarLoanService.get(toApplicationId, userId);
            BeanUtils.copyProperties(primaryCarLoanDetailRequest, carLoanResponse);
            if(!CommonUtils.isObjectNullOrEmpty(primaryCarLoanDetailRequest.getCertifiedDealer()) && primaryCarLoanDetailRequest.getCertifiedDealer().booleanValue()){
                carLoanResponse.setCertifiedDealer(primaryCarLoanDetailRequest.getCertifiedDealer().booleanValue() ? "Yes" : "No");
             }
            //carLoanResponse.setOnRoadCarPrice(!CommonUtils.isObjectNullOrEmpty(carLoanResponse.getOnRoadCarPrice()) ? CommonUtils.CurrencyFormat(carLoanResponse.getOnRoadCarPrice().toString()) : null);
            //carLoanResponse.setDownPayment(!CommonUtils.isObjectNullOrEmpty(carLoanResponse.getDownPayment()) ? CommonUtils.CurrencyFormat(carLoanResponse.getDownPayment().toString()) : null);
            carLoanResponse.setDeliveryDate(primaryCarLoanDetailRequest.getDeliveryDate() != null ? CommonUtils.DATE_FORMAT.format(primaryCarLoanDetailRequest.getDeliveryDate()) : null);
            carLoanResponse.setPurchasePreownedDate(primaryCarLoanDetailRequest.getPurchasePreownedDate() != null ? CommonUtils.DATE_FORMAT.format(primaryCarLoanDetailRequest.getPurchasePreownedDate()) : null);
            carLoanResponse.setPurchaseReimbursmentDate(primaryCarLoanDetailRequest.getPurchaseReimbursmentDate() != null ? CommonUtils.DATE_FORMAT.format(primaryCarLoanDetailRequest.getPurchaseReimbursmentDate()) : null);
            carLoanResponse.setCarType(primaryCarLoanDetailRequest.getCarType() != null ? CarType.getById(primaryCarLoanDetailRequest.getCarType()).getValue() : null);
            carLoanResponse.setNewCarPurchaseType(primaryCarLoanDetailRequest.getNewCarPurchaseType()!=null? CarPurchaseType.getById(primaryCarLoanDetailRequest.getNewCarPurchaseType()).getValue():null);
        } catch (Exception e) {
            logger.error(CommonUtils.EXCEPTION,e);
        }
        carLoanPrimaryViewResponse.setCarLoanResponse(carLoanResponse);
        return carLoanPrimaryViewResponse;
    }
}
