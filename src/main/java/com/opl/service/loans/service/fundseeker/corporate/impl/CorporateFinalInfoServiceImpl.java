package com.opl.service.loans.service.fundseeker.corporate.impl;

import java.util.Date;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.Address;
import com.opl.mudra.api.loans.model.corporate.CorporateFinalInfoRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.opl.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.service.fundseeker.corporate.CorporateFinalInfoService;
@Transactional
@Service
public class CorporateFinalInfoServiceImpl implements CorporateFinalInfoService {

    private static final Logger logger = LoggerFactory.getLogger(CorporateFinalInfoService.class.getName());
    @Autowired
    private CorporateApplicantDetailRepository applicantRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private ApplicationProposalMappingRepository applicationProposalMappingRepository;

    @Override
    public boolean saveOrUpdate(CorporateFinalInfoRequest corporateFinalInfoRequest, Long userId) throws LoansException {

        try{
            Long finalUserId = (CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getClientId()) ? userId
                    : corporateFinalInfoRequest.getClientId());
            CorporateApplicantDetail applicantDetail = applicantRepository.getByProposalId(corporateFinalInfoRequest.getProposalMappingId());

            if (applicantDetail != null) {
                applicantDetail.setModifiedBy(userId);
                applicantDetail.setModifiedDate(new Date());
                // inactive previous before adding new Data
            } else {
                applicantDetail = new CorporateApplicantDetail();
                applicantDetail.setCreatedBy(userId);
                applicantDetail.setCreatedDate(new Date());
                applicantDetail.setIsActive(true);
                applicantDetail.setApplicationId(new LoanApplicationMaster(corporateFinalInfoRequest.getApplicationId()));
                if (corporateFinalInfoRequest.getProposalMappingId() != null)
                    applicantDetail.setApplicationProposalMapping(new ApplicationProposalMapping(corporateFinalInfoRequest.getProposalMappingId()));
            }

            BeanUtils.copyProperties(corporateFinalInfoRequest, applicantDetail, CommonUtils.IgnorableCopy.getCorporateProfile()); //--------------------put check for Ignore properties
            applicantDetail.setModifiedBy(userId);
            applicantDetail.  setModifiedDate(new Date());
            copyAddressFromRequestToDomain(corporateFinalInfoRequest, applicantDetail); //--------------------put check for Ignore properties
            applicantDetail = applicantRepository.save(applicantDetail);


            // Setting Flag to applicantDetailFilled or not
           // loanApplicationRepository.setIsApplicantProfileMandatoryFilled(applicantDetail.getApplicationId().getId(),finalUserId, CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFinalDetailsFilled()) ? false : corporateFinalInfoRequest.getFinalDetailsFilled());
            if (corporateFinalInfoRequest.getProposalMappingId() == null) {
                loanApplicationRepository.setIsApplicantFinalMandatoryFilled(applicantDetail.getApplicationId().getId(), finalUserId, CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFinalDetailsFilled()) ? false : corporateFinalInfoRequest.getFinalDetailsFilled());
            }else {
                applicationProposalMappingRepository.setIsApplicantFinalMandatoryFilled(corporateFinalInfoRequest.getProposalMappingId(), applicantDetail.getApplicationId().getId(), finalUserId, CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFinalDetailsFilled()) ? false : corporateFinalInfoRequest.getFinalDetailsFilled());
            }
            // Updating Profile Filled Count
           /* loanApplicationRepository.setProfileFilledCount(applicantDetail.getApplicationId().getId(), finalUserId,
                    corporateFinalInfoRequest.getDetailsFilledCount());*/
            return true;

        }catch (Exception e){
            logger.error("Error while Saving Corporate Final Info:-",e);
            throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
        }

    }

    private static void copyAddressFromRequestToDomain(CorporateFinalInfoRequest from, CorporateApplicantDetail to) {
        // Setting Regsiterd Address
        if (from.getFirstAddress() != null) {
            to.setRegisteredPremiseNumber(from.getFirstAddress().getPremiseNumber());
            to.setRegisteredLandMark(from.getFirstAddress().getLandMark());
            to.setRegisteredStreetName(from.getFirstAddress().getStreetName());
            to.setRegisteredPincode(from.getFirstAddress().getPincode());
            to.setRegisteredCityId(from.getFirstAddress().getCityId());
            to.setRegisteredStateId(from.getFirstAddress().getStateId());
            to.setRegisteredCountryId(from.getFirstAddress().getCountryId());
            to.setRegisteredDistMappingId(from.getFirstAddress().getDistrictMappingId());
        }

		// Setting Administrative Address
		if (from.getSameAs() != null && from.getSameAs().booleanValue()) {
			if (from.getFirstAddress() != null) {
				to.setAdministrativePremiseNumber(from.getFirstAddress().getPremiseNumber());
				to.setAdministrativeLandMark(from.getFirstAddress().getLandMark());
				to.setAdministrativeStreetName(from.getFirstAddress().getStreetName());
				to.setAdministrativePincode(from.getFirstAddress().getPincode());
				to.setAdministrativeCityId(from.getFirstAddress().getCityId());
				to.setAdministrativeStateId(from.getFirstAddress().getStateId());
				to.setAdministrativeCountryId(from.getFirstAddress().getCountryId());
				to.setAdministrativeDistMappingId(from.getFirstAddress().getDistrictMappingId());
			}
		} else {
			if (from.getSecondAddress() != null) {
				to.setAdministrativePremiseNumber(from.getSecondAddress().getPremiseNumber());
				to.setAdministrativeLandMark(from.getSecondAddress().getLandMark());
				to.setAdministrativeStreetName(from.getSecondAddress().getStreetName());
				to.setAdministrativePincode(from.getSecondAddress().getPincode());
				to.setAdministrativeCityId(from.getSecondAddress().getCityId());
				to.setAdministrativeStateId(from.getSecondAddress().getStateId());
				to.setAdministrativeCountryId(from.getSecondAddress().getCountryId());
				to.setAdministrativeDistMappingId(from.getSecondAddress().getDistrictMappingId());
			}
		}
    }

    @Override
    public CorporateFinalInfoRequest getByProposalId(Long userId, Long proposalId) throws LoansException {
        try {
            CorporateApplicantDetail applicantDetail = applicantRepository.getByProposalId(proposalId);
            if (applicantDetail == null) {
                return null;
            }
            CorporateFinalInfoRequest corporateFinalInfoRequest = new CorporateFinalInfoRequest();
            BeanUtils.copyProperties(applicantDetail, corporateFinalInfoRequest, CommonUtils.IgnorableCopy.getCorporateProfile());
            copyAddressFromDomainToRequest(applicantDetail, corporateFinalInfoRequest);
            corporateFinalInfoRequest.setOrganisationName(corporateFinalInfoRequest.getOrganisationName());
            //applicantRequest.setDetailsFilledCount(applicantDetail.getApplicationId().getDetailsFilledCount());

            return corporateFinalInfoRequest;
        } catch (Exception e) {
            logger.error("Error while getting Corporate Profile:-",e);
            throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public CorporateFinalInfoRequest get(Long userId, Long applicationId) throws LoansException {
        try {
            CorporateApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(userId,
                    applicationId);
            if (applicantDetail == null) {
                return null;
            }
            CorporateFinalInfoRequest corporateFinalInfoRequest = new CorporateFinalInfoRequest();
            BeanUtils.copyProperties(applicantDetail, corporateFinalInfoRequest, CommonUtils.IgnorableCopy.getCorporateProfile());
            copyAddressFromDomainToRequest(applicantDetail, corporateFinalInfoRequest);

            //applicantRequest.setDetailsFilledCount(applicantDetail.getApplicationId().getDetailsFilledCount());

            return corporateFinalInfoRequest;
        } catch (Exception e) {
            logger.error("Error while getting Corporate Profile:-",e);
            throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public CorporateFinalInfoRequest getNTBDetails(Long userId, Long applicationId) throws LoansException {
        try {
            CorporateApplicantDetail applicantDetail = applicantRepository.getByApplicationIdAndIsAtive(applicationId);
            if (applicantDetail == null) {
                return null;
            }
            CorporateFinalInfoRequest corporateFinalInfoRequest = new CorporateFinalInfoRequest();
            BeanUtils.copyProperties(applicantDetail, corporateFinalInfoRequest, CommonUtils.IgnorableCopy.getNtbFinalExclusion());
            copyAddressFromDomainToRequest(applicantDetail, corporateFinalInfoRequest);

            //applicantRequest.setDetailsFilledCount(applicantDetail.getApplicationId().getDetailsFilledCount());

            return corporateFinalInfoRequest;
        } catch (Exception e) {
            logger.error("Error while getting Corporate Profile:-",e);
            throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public boolean saveOrUpdateNTBDetails(CorporateFinalInfoRequest corporateFinalInfoRequest, Long userId) throws LoansException {
        try{
            Long finalUserId = (CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getClientId()) ? userId
                    : corporateFinalInfoRequest.getClientId());
            CorporateApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(finalUserId,
                    corporateFinalInfoRequest.getApplicationId());

            if (applicantDetail != null) {
                applicantDetail.setModifiedBy(userId);
                applicantDetail.setModifiedDate(new Date());
                // inactive previous before adding new Data
            } else {
                applicantDetail = new CorporateApplicantDetail();
                applicantDetail.setCreatedBy(userId);
                applicantDetail.setCreatedDate(new Date());
                applicantDetail.setIsActive(true);
                applicantDetail.setApplicationId(new LoanApplicationMaster(corporateFinalInfoRequest.getApplicationId()));
            }

            BeanUtils.copyProperties(corporateFinalInfoRequest, applicantDetail, CommonUtils.IgnorableCopy.getNtbFinalExclusion()); //--------------------put check for Ignore properties
            applicantDetail.setModifiedBy(userId);
            applicantDetail.setModifiedDate(new Date());
            copyAddressFromRequestToDomain(corporateFinalInfoRequest, applicantDetail); //--------------------put check for Ignore properties
            applicantDetail = applicantRepository.save(applicantDetail);


            // Setting Flag to applicantDetailFilled or not
            // loanApplicationRepository.setIsApplicantProfileMandatoryFilled(applicantDetail.getApplicationId().getId(),finalUserId, CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFinalDetailsFilled()) ? false : corporateFinalInfoRequest.getFinalDetailsFilled());
            loanApplicationRepository.setIsApplicantFinalMandatoryFilled(applicantDetail.getApplicationId().getId(),finalUserId, CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFinalDetailsFilled()) ? false : corporateFinalInfoRequest.getFinalDetailsFilled());
            // Updating Profile Filled Count
           /* loanApplicationRepository.setProfileFilledCount(applicantDetail.getApplicationId().getId(), finalUserId,
                    corporateFinalInfoRequest.getDetailsFilledCount());*/
            return true;

        }catch (Exception e){
            logger.error("Error while Saving Corporate Final Info:-",e);
            throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
        }
    }


    private static void copyAddressFromDomainToRequest(CorporateApplicantDetail from, CorporateFinalInfoRequest to) {
        // Setting Regsiterd Address
        Address address = new Address();

        address.setPremiseNumber(from.getRegisteredPremiseNumber());
        address.setLandMark(from.getRegisteredLandMark());
        address.setStreetName(from.getRegisteredStreetName());
        address.setPincode(from.getRegisteredPincode());
        address.setCityId(from.getRegisteredCityId());
        address.setStateId(from.getRegisteredStateId());
        address.setCountryId(from.getRegisteredCountryId());
        address.setDistrictMappingId(from.getRegisteredDistMappingId());
        to.setFirstAddress(address);
		if (from.getSameAs() != null && from.getSameAs()) {
			to.setSecondAddress(address);
		} else {
			address = new Address();
			address.setPremiseNumber(from.getAdministrativePremiseNumber());
			address.setLandMark(from.getAdministrativeLandMark());
			address.setStreetName(from.getAdministrativeStreetName());
			address.setPincode(from.getAdministrativePincode());
			address.setCityId(from.getAdministrativeCityId());
			address.setStateId(from.getAdministrativeStateId());
			address.setCountryId(from.getAdministrativeCountryId());
			address.setDistrictMappingId(from.getAdministrativeDistMappingId());
			to.setSecondAddress(address);
		}

        // Setting Administrative Address
    }
}
