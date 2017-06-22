package com.capitaworld.service.loans.service.teaser.primaryview.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.dms.client.DMSClient;
import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.PrimaryPersonalLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.RetailApplicantDetail;
import com.capitaworld.service.loans.model.AddressResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.PersonalLoanResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.RetailPrimaryViewResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.RetailProfileViewResponse;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.PrimaryPersonalLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.common.DocumentManagementService;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.GuarantorService;
import com.capitaworld.service.loans.service.teaser.primaryview.PersonalLoansViewService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.Currency;
import com.capitaworld.service.oneform.enums.EmployeeWith;
import com.capitaworld.service.oneform.enums.Gender;
import com.capitaworld.service.oneform.enums.IndustryType;
import com.capitaworld.service.oneform.enums.LoanType;
import com.capitaworld.service.oneform.enums.MaritalStatus;
import com.capitaworld.service.oneform.enums.OccupationNature;
import com.capitaworld.service.oneform.enums.PersonalLoanPurpose;
import com.capitaworld.service.oneform.enums.Title;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;

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
	
	@Override
	public RetailPrimaryViewResponse getPersonalLoansPrimaryViewDetails(Long applicantId) throws Exception {
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
				personalLoanResponse.setDateOfProposal(CommonUtils.getStringDateFromDate(applicantDetail.getModifiedDate()));
				try {
					if (applicantDetail.getEmployedWithId() != 8) {
						profileViewPLResponse.setEmployeeWith(EmployeeWith.getById(applicantDetail.getEmployedWithId()).getValue());
					} else {
						profileViewPLResponse.setEmployeeWith(applicantDetail.getEmployedWithOther());
					}
				} catch (Exception e) {

				}
				profileViewPLResponse.setFirstName(applicantDetail.getFirstName());
				try {
					profileViewPLResponse.setGender(Gender.getById(applicantDetail.getGenderId()).getValue());
				} catch (Exception e) {
				}
				profileViewPLResponse.setLastName(applicantDetail.getLastName());
				profileViewPLResponse.setMaritalStatus(applicantDetail.getStatusId() != null ? MaritalStatus.getById(applicantDetail.getStatusId()).getValue() : null);
				profileViewPLResponse.setMiddleName(applicantDetail.getMiddleName());
				profileViewPLResponse.setMonthlyIncome(String.valueOf(applicantDetail.getMonthlyIncome() != null ? applicantDetail.getMonthlyIncome() : 0));
				profileViewPLResponse.setNatureOfOccupation(OccupationNature.getById(applicantDetail.getOccupationId()).getValue());
				AddressResponse officeAddress = new AddressResponse();

				try {
					List<Long> officeCity = new ArrayList<Long>(1);
					officeCity.add(applicantDetail.getOfficeCityId());
					OneFormResponse formResponse = oneFormClient.getCityByCityListId(officeCity);

					MasterResponse data = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponse.getListData().get(0), MasterResponse.class);
					officeAddress.setCity(data.getValue());
				} catch (Exception e) {
					e.printStackTrace();
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
					e.printStackTrace();

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

				}
				officeAddress.setLandMark(applicantDetail.getOfficeLandMark());
				officeAddress.setPincode(applicantDetail.getOfficePincode() != null ? applicantDetail.getOfficePincode().toString() : null);
				officeAddress.setPremiseNumber(applicantDetail.getOfficePremiseNumberName());
				officeAddress.setStreetName(applicantDetail.getOfficeStreetName());
				personalLoanResponse.setOfficeAddress(officeAddress);

				AddressResponse permanentAddress = new AddressResponse();
				try {
					List<Long> permanentCity = new ArrayList<Long>(1);
					permanentCity.add(applicantDetail.getPermanentCityId());
					OneFormResponse formResponsePermanentCity = oneFormClient.getCityByCityListId(permanentCity);
					MasterResponse dataCity = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponsePermanentCity.getListData().get(0), MasterResponse.class);
					permanentAddress.setCity(dataCity.getValue());
				} catch (Exception e) {

				}
				try {
					List<Long> permanentCountry = new ArrayList<Long>(1);
					Long permanentCountryLong = null;
					if (applicantDetail.getOfficeCountryId() != null) {
						permanentCountryLong = Long.valueOf(applicantDetail.getPermanentCountryId().toString());

						permanentCountry.add(permanentCountryLong);
						OneFormResponse countryPermanent = oneFormClient.getCountryByCountryListId(permanentCountry);
						MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) countryPermanent.getListData().get(0), MasterResponse.class);
						officeAddress.setCountry(dataCountry.getValue());
					}
				} catch (Exception e) {

				}
				try {
					List<Long> permanentState = new ArrayList<Long>(1);

					Long permanentStateLong = null;
					if (applicantDetail.getOfficeCountryId() != null) {
						permanentStateLong = Long.valueOf(applicantDetail.getPermanentStateId().toString());

						permanentState.add(permanentStateLong);
						OneFormResponse statePermanent = oneFormClient.getStateByStateListId(permanentState);
						MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) statePermanent.getListData().get(0), MasterResponse.class);
						officeAddress.setState(dataState.getValue());

					}
				} catch (Exception e) {

				}
				permanentAddress.setLandMark(applicantDetail.getPermanentLandMark());
				permanentAddress.setPincode(applicantDetail.getPermanentPincode() != null ? applicantDetail.getPermanentPincode().toString() : null);
				permanentAddress.setPremiseNumber(applicantDetail.getPermanentPremiseNumberName());
				permanentAddress.setStreetName(applicantDetail.getPermanentStreetName());
				personalLoanResponse.setPermanentAddress(permanentAddress);


				profileViewPLResponse.setTitle(Title.getById(applicantDetail.getTitleId()).getValue());
				profileViewPLResponse.setAge(applicantDetail.getBirthDate() != null ? CommonUtils.getAgeFromBirthDate(applicantDetail.getBirthDate()).toString() : null);

				if (applicantDetail.getApplicationId() != null) {
					personalLoanResponse.setTenure(applicantDetail.getApplicationId().getTenure() != null ? applicantDetail.getApplicationId().getTenure().toString() : null);
					personalLoanResponse.setLoanType(applicantDetail.getApplicationId().getProductId() != null ? LoanType.getById(applicantDetail.getApplicationId().getProductId()).getValue() : null);
					personalLoanResponse.setLoanAmount(applicantDetail.getApplicationId().getAmount() != null ? applicantDetail.getApplicationId().getAmount().toString() : null);
					personalLoanResponse.setCurrency(applicantDetail.getApplicationId().getCurrencyId() != null ? Currency.getById(applicantDetail.getApplicationId().getCurrencyId()).getValue() : null);
				}


				profileViewPLResponse.setEntityName(applicantDetail.getEntityName());
				if (applicantDetail.getIndustryTypeId() != null && applicantDetail.getIndustryTypeId() != 16) {
					profileViewPLResponse.setIndustryType(IndustryType.getById(applicantDetail.getIndustryTypeId()).getValue());
				} else {
					profileViewPLResponse.setIndustryType(applicantDetail.getIndustryTypeOther());
				}

				//set pan car
				profileViewPLResponse.setPan(applicantDetail.getPan());

				//get list of Pan Card
				try {
					profileViewPLResponse.setPanCardList(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_APPLICANT_SCANNED_COPY_OF_PAN_CARD));
				} catch (DocumentException e) {
					e.printStackTrace();
				}

				//get list of Aadhar Card
				try {
					profileViewPLResponse.setAadharCardList(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_APPLICANT_SCANNED_COPY_OF_AADHAR_CARD));
				} catch (DocumentException e) {
					e.printStackTrace();
				}

				//profile picture
				try {
					personalLoanResponse.setApplicantProfilePicture(documentManagementService.getDocumentDetails(applicantId,DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.PERSONAL_LOAN_PROFIEL_PICTURE));
				}catch (DocumentException e){
					e.printStackTrace();
				}

				retailPrimaryViewResponse.setPersonalProfileRespoonse(profileViewPLResponse);
			} else {
				throw new Exception("No Data found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Problem Occured while Fetching Retail Details");
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
		retailPrimaryViewResponse.setCoApplicantResponse(coApplicantResponse);

		//setting guarantor details
		List<RetailProfileViewResponse> garantorResponse = guarantorService.getGuarantorServiceResponse(applicantId, userId,applicationMaster.getProductId());
		retailPrimaryViewResponse.setGarantorResponse(garantorResponse);

		//setting Personal Loan Specific Data
		retailPrimaryViewResponse.setPersonalLoanResponse(personalLoanResponse);

		return retailPrimaryViewResponse;
	}

}
