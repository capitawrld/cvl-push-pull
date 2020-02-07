package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.IndustrySectorDetail;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.AssetsDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.LiabilitiesDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.OperatingStatementDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PastFinancialEstimatesDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.SubsectorDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.RetailApplicantDetail;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.Address;
import com.capitaworld.service.loans.model.PaymentRequest;
import com.capitaworld.service.loans.model.common.GraphResponse;
import com.capitaworld.service.loans.model.common.LongitudeLatitudeRequest;
import com.capitaworld.service.loans.model.corporate.CorporateApplicantRequest;
import com.capitaworld.service.loans.model.corporate.CorporateCoApplicantRequest;
import com.capitaworld.service.loans.model.corporate.SubSectorListRequest;
import com.capitaworld.service.loans.repository.common.CommonRepository;
import com.capitaworld.service.loans.repository.common.LoanRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AssetsDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LiabilitiesDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.OperatingStatementDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PastFinancialEstimateDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SectorIndustryMappingRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SubSectorMappingRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SubSectorRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateCoApplicantService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;

@Service
@Transactional
public class CorporateApplicantServiceImpl implements CorporateApplicantService {
	private static final Logger logger = LoggerFactory.getLogger(CorporateApplicantService.class.getName());
	@Autowired
	private CorporateApplicantDetailRepository applicantRepository;

	@Autowired
	private IndustrySectorRepository industrySectorRepository;

	@Autowired
	private SubSectorRepository subSectorRepository;

	@Autowired
	private SectorIndustryMappingRepository sectorIndustryMappingRepository;

	@Autowired
	private SubSectorMappingRepository subSectorMappingRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private CorporateApplicantDetailRepository applicantDetailRepository;

	@Autowired
	private PastFinancialEstimateDetailsRepository pastFinancialEstimateDetailsRepository;

	@Autowired
	private CorporateCoApplicantService coApplicantService;

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;

    @Autowired
    private RetailApplicantDetailRepository retailApplicantDetailRepository;

    @Autowired
    private PrimaryCorporateDetailRepository primaryCorporateDetailRepository;

	@Autowired
	private UsersClient usersClient;
	
	@Autowired
	private CommonRepository commonRepository;
	
	@Autowired
	private AssetsDetailsRepository assetsDetailsRepository;
	
	//private static final String SIDBI_AMOUNT = "com.capitaworld.sidbi.amount";
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private LiabilitiesDetailsRepository liabilitiesDetailsRepository;
	
	@Autowired
	private OperatingStatementDetailsRepository operatingStatementDetailsRepository;
	
	@Autowired
	private LoanRepository loanRepository;   

	@Override
	public void saveITRMappingData (CorporateApplicantRequest applicantRequest) {
		//Updating OneForm Uniform Fields
		/*if(!CommonUtils.isObjectNullOrEmpty(applicantRequest.getTurnOverPrevFinYear()) || !CommonUtils.isObjectNullOrEmpty(applicantRequest.getTurnOverCurrFinYearTillMonth()) || !CommonUtils.isObjectNullOrEmpty(applicantRequest.getProfitCurrFinYear())){*/
			logger.info("TurnOverPrevFinYear===>{}TurnOverCurrFinYearTillMonth====>{}ProfitCurrFinYear===>{}====>GrossSales==>{}",applicantRequest.getTurnOverPrevFinYear(), applicantRequest.getTurnOverCurrFinYearTillMonth(),applicantRequest.getProfitCurrFinYear(),applicantRequest.getGrossSales());
			int count = primaryCorporateDetailRepository.updatedFinancialFieldsForUniformProduct(applicantRequest.getApplicationId(), applicantRequest.getTurnOverPrevFinYear(), applicantRequest.getTurnOverCurrFinYearTillMonth(),applicantRequest.getProfitCurrFinYear(),applicantRequest.getGrossSales());
			logger.info("Count in Updation===>{}",count);
		/*}*/

		CorporateApplicantDetail applicantDetail = applicantRepository.findByApplicationIdIdAndIsActive(applicantRequest.getApplicationId(),true);
		if(!CommonUtils.isObjectNullOrEmpty(applicantDetail)) {
			applicantDetail.setModifiedBy(applicantRequest.getUserId());
			applicantDetail.setModifiedDate(new Date());
		} else {
			applicantDetail = new CorporateApplicantDetail();
			applicantDetail.setCreatedBy(applicantRequest.getUserId());
			applicantDetail.setCreatedDate(new Date());
			applicantDetail.setIsActive(true);
			applicantDetail.setApplicationId(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		}
		applicantDetail.setEstablishmentMonth(applicantRequest.getEstablishmentMonth());
		applicantDetail.setEstablishmentYear(applicantRequest.getEstablishmentYear());
		copyAddressFromRequestToDomain(applicantRequest, applicantDetail);
		applicantRepository.save(applicantDetail);
		
		if(!CommonUtils.isObjectNullOrEmpty(applicantRequest.getCompanyCIN())) {
			logger.info("Company CIN number saving --------------------->" + applicantRequest.getCompanyCIN() + "-----------------------"  +applicantRequest.getApplicationId());
			
			LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.getById(applicantRequest.getApplicationId());
			if(!CommonUtils.isObjectNullOrEmpty(loanApplicationMaster)) {
				logger.info("LoanApplicationMaster is not null");
				loanApplicationMaster.setCompanyCinNumber(applicantRequest.getCompanyCIN());
				loanApplicationMaster.setModifiedDate(new Date());
				loanApplicationMaster.setModifiedBy(applicantRequest.getUserId());
				loanApplicationRepository.save(loanApplicationMaster);
			} else {
				logger.info("LoanApplicationMaster is null or empty");
			}
		} else {
			logger.info("Company CIN number null or empty --------------------->" );
		}
		
	}
	
	@Override
	public boolean save(CorporateApplicantRequest applicantRequest, Long userId) throws LoansException {
		try {
			// application id must not be null
			Long finalUserId = (CommonUtils.isObjectNullOrEmpty(applicantRequest.getClientId()) ? userId
					: applicantRequest.getClientId());
			CorporateApplicantDetail applicantDetail = applicantRepository.findByApplicationIdIdAndIsActive(applicantRequest.getApplicationId(),true);
			if (applicantDetail != null) {
				applicantDetail.setModifiedBy(userId);
				applicantDetail.setModifiedDate(new Date());
				// inactive previous before adding new Data
				int updatedRecords = industrySectorRepository
						.inActiveMappingByApplicationId(applicantDetail.getApplicationId().getId());
				logger.info("updated industrySector ==>" + updatedRecords);
				// inactive previous before adding new Data
				int subSecors = subSectorRepository
						.inActiveMappingByApplicationId(applicantDetail.getApplicationId().getId());
				logger.info("updated subSector==>" + subSecors);
			} else {
				applicantDetail = new CorporateApplicantDetail();
				applicantDetail.setCreatedBy(userId);
				applicantDetail.setCreatedDate(new Date());
				applicantDetail.setIsActive(true);
				applicantDetail.setApplicationId(new LoanApplicationMaster(applicantRequest.getApplicationId()));
			}

			BeanUtils.copyProperties(applicantRequest, applicantDetail, CommonUtils.IgnorableCopy.getCorporateFinal());
			applicantDetail.setModifiedBy(userId);
			applicantDetail.setModifiedDate(new Date());
			copyAddressFromRequestToDomain(applicantRequest, applicantDetail);
			applicantDetail = applicantRepository.save(applicantDetail);

			/*// save co-applicant details
			for (CorporateCoApplicantRequest request : applicantRequest.getCoApplicants()) {
				coApplicantService.save(request, applicantRequest.getApplicationId(), finalUserId);
			}*/

			// industry data save
			saveIndustry(applicantDetail.getApplicationId().getId(), applicantRequest.getIndustrylist());
			// Sector data save
			saveSector(applicantDetail.getApplicationId().getId(), applicantRequest.getSectorlist());
			// sub sector save
			saveSubSector(applicantDetail.getApplicationId().getId(), applicantRequest.getSubsectors());

			// Setting Flag to applicantDetailFilled or not
			loanApplicationRepository.setIsApplicantProfileMandatoryFilled(applicantDetail.getApplicationId().getId(),
					finalUserId, CommonUtils.isObjectNullOrEmpty(applicantRequest.getIsApplicantDetailsFilled()) ? false
							: applicantRequest.getIsApplicantDetailsFilled());

			// Updating Profile Filled Count
			loanApplicationRepository.setProfileFilledCount(applicantDetail.getApplicationId().getId(), finalUserId,
					applicantRequest.getDetailsFilledCount());
			return true;

		} catch (Exception e) {
			logger.error("Error while Saving Corporate Profile :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public CorporateApplicantRequest getCorporateApplicantByProposalId(Long userId, Long proposalId) throws Exception {
		try {
			// TODO Auto-generated method stub
			CorporateApplicantDetail applicantDetail = applicantRepository.getByProposalId(proposalId);

			if (applicantDetail == null) {
				return null;
			}
			CorporateApplicantRequest applicantRequest = new CorporateApplicantRequest();
			BeanUtils.copyProperties(applicantDetail, applicantRequest);
			copyAddressFromDomainToRequest(applicantDetail, applicantRequest);
			applicantRequest.setIndustrylist(industrySectorRepository.getIndustryByPorposalId(proposalId));
			applicantRequest.setSectorlist(industrySectorRepository.getSectorByProposalId(proposalId));
			applicantRequest.setSubsectors(subSectorRepository.getSubSectorByProposalId(proposalId));
			applicantRequest.setDetailsFilledCount(applicantDetail.getApplicationId().getDetailsFilledCount());
			try {

				UserResponse userResponse= usersClient.getEmailMobile(userId);
				@SuppressWarnings("unchecked")
				UsersRequest userRequest= MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				applicantRequest.setEmail(userRequest.getEmail());
				applicantRequest.setLandlineNo(userRequest.getMobile());
			}
			catch (Exception e){
				logger.warn("error while get user data = {}",e);
			}
			//applicantRequest.setCoApplicants(coApplicantService.getList(applicationId, userId));
			return applicantRequest;
		} catch (Exception e) {
			logger.error("Error while getting Corporate Profile:- {}",e);
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public CorporateApplicantRequest getCorporateApplicant(Long userId, Long applicationId) throws LoansException {
		try {

			CorporateApplicantDetail applicantDetail = applicantRepository.getByApplicationAndUserId(userId,
					applicationId);

			if (applicantDetail == null) {
				return null;
			}
			CorporateApplicantRequest applicantRequest = new CorporateApplicantRequest();
			BeanUtils.copyProperties(applicantDetail, applicantRequest);
			copyAddressFromDomainToRequest(applicantDetail, applicantRequest);
			applicantRequest.setIndustrylist(industrySectorRepository.getIndustryByApplicationId(applicationId));
			applicantRequest.setSectorlist(industrySectorRepository.getSectorByApplicationId(applicationId));
			applicantRequest.setSubsectors(subSectorRepository.getSubSectorByApplicationId(applicationId));
			applicantRequest.setDetailsFilledCount(applicantDetail.getApplicationId().getDetailsFilledCount());
			try {

				UserResponse userResponse= usersClient.getEmailMobile(userId);
				@SuppressWarnings("unchecked")
				UsersRequest userRequest= MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				applicantRequest.setEmail(userRequest.getEmail());
				applicantRequest.setLandlineNo(userRequest.getMobile());
			}
			catch (Exception e){
				logger.error("error while get user data : ",e);
			}
			//applicantRequest.setCoApplicants(coApplicantService.getList(applicationId, userId));
			return applicantRequest;
		} catch (Exception e) {
			logger.error("Error while getting Corporate Profile :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	/*
	 * @Override public void updateFinalCommonInformation(Long applicationId, Long
	 * userId, Boolean flag) throws Exception { try {
	 * loanApplicationRepository.setIsApplicantFinalMandatoryFilled( applicationId,
	 * userId, flag); } catch (Exception e) {
	 * logger.error("Error while updating final information flag : ",e);
	 * throw new Exception(CommonUtils.SOMETHING_WENT_WRONG); }
	 * }
	 */
	@Override
	public void saveIndustry(Long applicationId, List<Long> industrylist) {
		IndustrySectorDetail industrySectorDetail = null;
		for (Long id : industrylist) {
			
			List<Long> resultList = industrySectorRepository.findByIndustryIdAndApplicationIdAndIsActive(id, applicationId, true);
			if(resultList.size() > 0) {
				continue;
			}
			industrySectorDetail = new IndustrySectorDetail();
			industrySectorDetail.setApplicationId(applicationId);
			industrySectorDetail.setIndustryId(id);
			industrySectorDetail.setCreatedBy(applicationId);
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorRepository.save(industrySectorDetail);
		}
	}
	@Override
	public void saveSector(Long applicationId, List<Long> sectorlist) {
		// sector data save
		for (Long id : sectorlist) {
			
			List<Long> resultList = industrySectorRepository.findBySectorIdAndApplicationIdAndIsActive(id, applicationId, true);
			if(resultList.size() > 0) {
				continue;
			}
			IndustrySectorDetail industrySectorDetail = new IndustrySectorDetail();
			industrySectorDetail.setApplicationId(applicationId);
			industrySectorDetail.setSectorId(id);
			industrySectorDetail.setCreatedBy(applicationId);
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorRepository.save(industrySectorDetail);
		}
	}
	@Override
	public void saveSubSector(Long applicationId, List<Long> subSectorlist) {
		// sector data save
		for (Long id : subSectorlist) {

			List<Long> resultList = subSectorRepository.findBySectorSubsectorTransactionIdAndApplicationIdAndIsActive(id, applicationId, true);
			if(resultList.size() > 0) {
				continue;
			}
			
			SubsectorDetail subsectorDetail = new SubsectorDetail();
			subsectorDetail.setApplicationId(applicationId);
			subsectorDetail.setSectorSubsectorTransactionId(id);
			subsectorDetail.setCreatedBy(applicationId);
			subsectorDetail.setCreatedDate(new Date());
			subsectorDetail.setIsActive(true);
			// create by and update
			subSectorRepository.save(subsectorDetail);
		}
	}

	private static void copyAddressFromRequestToDomain(CorporateApplicantRequest from, CorporateApplicantDetail to) {
		// Setting Regsiterd Address
		if (from.getFirstAddress() != null) {
			to.setRegisteredPremiseNumber(from.getFirstAddress().getPremiseNumber());
			to.setRegisteredLandMark(from.getFirstAddress().getLandMark());
			to.setRegisteredStreetName(from.getFirstAddress().getStreetName());
			to.setRegisteredPincode(from.getFirstAddress().getPincode());
			to.setRegisteredCityId(from.getFirstAddress().getCityId());
			to.setRegisteredStateId(from.getFirstAddress().getStateId());
			to.setRegisteredCountryId(from.getFirstAddress().getCountryId());
		}

		/*// Setting Administrative Address
		if (from.getSameAs() != null && from.getSameAs().booleanValue()) {
			if (from.getFirstAddress() != null) {
				to.setAdministrativePremiseNumber(from.getFirstAddress().getPremiseNumber());
				to.setAdministrativeLandMark(from.getFirstAddress().getLandMark());
				to.setAdministrativeStreetName(from.getFirstAddress().getStreetName());
				to.setAdministrativePincode(from.getFirstAddress().getPincode());
				to.setAdministrativeCityId(from.getFirstAddress().getCityId());
				to.setAdministrativeStateId(from.getFirstAddress().getStateId());
				to.setAdministrativeCountryId(from.getFirstAddress().getCountryId());
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
			}
		}*/
	}

	private static void copyAddressFromDomainToRequest(CorporateApplicantDetail from, CorporateApplicantRequest to) {
		// Setting Regsiterd Address
		Address address = new Address();

		address.setPremiseNumber(from.getRegisteredPremiseNumber());
		address.setLandMark(from.getRegisteredLandMark());
		address.setStreetName(from.getRegisteredStreetName());
		address.setPincode(from.getRegisteredPincode());
		address.setCityId(from.getRegisteredCityId());
		address.setStateId(from.getRegisteredStateId());
		address.setCountryId(from.getRegisteredCountryId());
		to.setFirstAddress(address);
		/*if (from.getSameAs() != null && from.getSameAs()) {
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
			to.setSecondAddress(address);

		}*/

		// Setting Administrative Address
	}

	@Override
	public List<Long> getSectorListByIndustryId(List<Long> industryList) throws LoansException {
		return sectorIndustryMappingRepository.getSectorListByIndustryList(industryList);
	}

	@Override
	public List<SubSectorListRequest> getSubSectorList(List<Long> list) {

		List<SubSectorListRequest> subSectorListRequests = new ArrayList<SubSectorListRequest>(list.size());
		for (Long id : list) {
			SubSectorListRequest subSectorListRequest = new SubSectorListRequest();
			if (industrySectorRepository.findOneBySectorId(id) != null)
				subSectorListRequest.setIndustryId(industrySectorRepository.findOneBySectorId(id));

			if (sectorIndustryMappingRepository.findIndustryBySectorId(id) != null)
				subSectorListRequest.setIndustryId(sectorIndustryMappingRepository.findIndustryBySectorId(id));

			subSectorListRequest.setSectorId(id);
			subSectorListRequest.setSubSectorIdList(subSectorMappingRepository.getSectorListByIndustryList(id));
			subSectorListRequests.add(subSectorListRequest);

		}
		return subSectorListRequests;
	}

	@Override
	public GraphResponse getGraphs(Long applicationId, Long userId) {
		// For now code has been written as it was in spring old last release.
		// will improve later once i(Akshay) understands how graph data should
		// be.

		GraphResponse graphResponse = new GraphResponse();

		DecimalFormat decimalFormat = new DecimalFormat("#");
		DecimalFormat decimalFormat1 = new DecimalFormat("#.##");

		List<PastFinancialEstimatesDetail> pastEstimates = pastFinancialEstimateDetailsRepository
				.listPastFinancialEstimateDetailsFromAppId(applicationId);
		if (pastEstimates.size() > 4) {
			pastEstimates = pastEstimates.subList((pastEstimates.size() - 4), pastEstimates.size());
		}
		if (!CommonUtils.isListNullOrEmpty(pastEstimates) && pastEstimates.size() > 1) {
			graphResponse.setGraphAvailable(true);
		} else {
			return graphResponse;
		}

		List<Double> pats = new ArrayList<>(pastEstimates.size());
		List<Double> sales = new ArrayList<>(pastEstimates.size());
		List<Double> ebidta = new ArrayList<>(pastEstimates.size());
		List<Double> netWorth = new ArrayList<>(pastEstimates.size());
		List<Double> currentAsset = new ArrayList<>(pastEstimates.size());
		List<Double> currentLiabilities = new ArrayList<>(pastEstimates.size());
		List<Double> fixedAsset = new ArrayList<>(pastEstimates.size());
		List<Double> debt = new ArrayList<>(pastEstimates.size());

		List<String> financialYears = new ArrayList<>(pastEstimates.size());

		for (PastFinancialEstimatesDetail finEst : pastEstimates) {
			financialYears.add(finEst.getFinancialYear());

			pats.add(finEst.getPat());
			sales.add(finEst.getSales());
			ebidta.add(finEst.getEbitda());
			netWorth.add(finEst.getNetWorth());
			currentAsset.add(finEst.getCurrentAssets());
			currentLiabilities.add(finEst.getCurrentLiabilities());
			fixedAsset.add(finEst.getFixedAssets());
			debt.add(finEst.getDebt());
		}

		Double val;
		// calculate pat%

		List<Double> patsPercentage = new ArrayList<>(pats.size());
		for (int i = 0; i <= pats.size() - 1; i++) {
//			 logger.info(pats.get(i)+"-"+sales.get(i));
			val = (pats.get(i) / sales.get(i));
			val = val * 100;
			if (Double.isNaN(val)) {
				val = 0d;
			}
			patsPercentage.add(Double.valueOf(decimalFormat.format(val)));
		}

		List<Double> salesPercentage = new ArrayList<>(sales.size());
		salesPercentage.add(null);
		// calculate revenue % (Previous sales/sales)%
		for (int i = 0; i <= (sales.size() - 2); i++) {
			// logger.info(sales.get(i+1)+"-"+sales.get(i));
			val = (sales.get(i + 1) - sales.get(i));
			val = val / sales.get(i);
			val = val * 100;
			if (Double.isNaN(val)) {
				val = 0d;
			}
			salesPercentage.add(Double.valueOf(decimalFormat.format(val)));
		}
		// calculate Ebidta Percentage (Ebidta/sales)%
		List<Double> ebidtaPercentage = new ArrayList<>(sales.size());
		for (int i = 0; i <= (sales.size() - 1); i++) {
//			 logger.info(sales.get(i+1)+"-"+sales.get(i));
			val = (ebidta.get(i) / sales.get(i));
			val = val * 100;
			if (Double.isNaN(val)) {
				val = 0d;
			}
			ebidtaPercentage.add(Double.valueOf(decimalFormat.format(val)));
		}
		// calculate ROE(%) (pat/netWorth)%
		List<Double> roePercentage = new ArrayList<>(pats.size());
		for (int i = 0; i <= (pats.size() - 1); i++) {
//			logger.info(sales.get(i+1)+"-"+sales.get(i));
			val = (pats.get(i) / netWorth.get(i));
			val = val * 100;
			if (Double.isNaN(val)) {
				val = 0d;
			}
			roePercentage.add(Double.valueOf(decimalFormat.format(val)));
		}
		// calculate ROCE(%) (EBIDTA/CurrentAssets+FixsedAssets)%
		List<Double> rocePercentage = new ArrayList<>(ebidta.size());
		for (int i = 0; i <= (ebidta.size() - 1); i++) {
//			logger.info(sales.get(i+1)+"-"+sales.get(i));
			val = (ebidta.get(i) / (currentAsset.get(i) + fixedAsset.get(i)));
			val = val * 100;
			if (Double.isNaN(val)) {
				val = 0d;
			}
			rocePercentage.add(Double.valueOf(decimalFormat.format(val)));
		}

		List<Double> debtEquityPercentage = new ArrayList<>(debt.size());
		for (int i = 0; i <= (debt.size() - 1); i++) {
//			logger.info(sales.get(i+1)+"-"+sales.get(i));
			val = (debt.get(i) / netWorth.get(i));
			if (Double.isNaN(val)) {
				val = 0d;
			}
			debtEquityPercentage.add(Double.valueOf(decimalFormat1.format(val)));
		}

		// calculate current Ration (Current Assets/Current Liabilities)
		List<Double> currentRatio = new ArrayList<>(currentAsset.size());
		for (int i = 0; i <= (currentAsset.size() - 1); i++) {
//			logger.info(sales.get(i+1)+"-"+sales.get(i));
			val = (currentAsset.get(i) / (currentLiabilities.get(i)));
			if (Double.isNaN(val)) {
				val = 0d;
			}
			currentRatio.add(Double.valueOf(decimalFormat1.format(val)));
		}

		graphResponse.setxAxisOfPat(financialYears);
		graphResponse.setPats(pats);
		graphResponse.setSales(sales);
		graphResponse.setEbidta(ebidta);
		graphResponse.setNetWorth(netWorth);
		graphResponse.setCurrentAsset(currentAsset);
		graphResponse.setCurrentLiabilities(currentLiabilities);
		graphResponse.setFixedAsset(fixedAsset);
		graphResponse.setDebt(debt);

		graphResponse.setPatsPercentage(patsPercentage);
		graphResponse.setSalesPercentage(salesPercentage);
		graphResponse.setEbidtaPercentage(ebidtaPercentage);
		graphResponse.setRoePercentage(roePercentage);
		graphResponse.setRocePercentage(rocePercentage);
		graphResponse.setCurrentRatio(currentRatio);
		graphResponse.setDebtEquityPercentage(debtEquityPercentage);

		return graphResponse;

	}

	@Override
	public int updateLatLong(LongitudeLatitudeRequest request, Long userId) throws LoansException {
		try {
			Long finalUserId = !CommonUtils.isObjectNullOrEmpty(request.getClientId()) ? request.getClientId() : userId;

			int latLong = 1;
			long applicantCount = applicantDetailRepository.getApplicantCount(finalUserId, request.getId());
			if (applicantCount == 0) {
				CorporateApplicantDetail applicantDetail = new CorporateApplicantDetail();
				applicantDetail.setApplicationId(new LoanApplicationMaster(request.getId()));
				applicantDetail.setLongitude(request.getLongitude());
				applicantDetail.setLatitude(request.getLatitude());
				applicantDetail.setIsActive(true);
				applicantDetail.setCreatedBy(userId);
				applicantDetail.setModifiedBy(userId);
				applicantDetail.setCreatedDate(new Date());
				applicantDetail.setModifiedDate(new Date());
				applicantDetailRepository.save(applicantDetail);
				// One is Static Because First time only One Record will be
				// effect.even every time One record will affect.
				return latLong;
			} else {
				latLong = applicantDetailRepository.updateLatLong(request.getLatitude(), request.getLongitude(),
						request.getId());
			}
			return latLong;
		} catch (Exception e) {
			logger.error("Error While Updating Lat and Lon : ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public LongitudeLatitudeRequest getLatLonByApplicationAndUserId(Long applicationId, Long userId) throws LoansException {
		try {
			List<Object[]> latLons = applicantDetailRepository.getLatLonByApplicationAndUserId(applicationId, userId);
			if (CommonUtils.isListNullOrEmpty(latLons)) {
				return null;
			} else {
				LongitudeLatitudeRequest request = new LongitudeLatitudeRequest();
				Object[] objects = latLons.get(0);
				request.setLatitude(
						!CommonUtils.isObjectNullOrEmpty(objects[0]) ? Double.valueOf(objects[0].toString()) : null);
				request.setLongitude(
						!CommonUtils.isObjectNullOrEmpty(objects[1]) ? Double.valueOf(objects[1].toString()) : null);
				return request;
			}

		} catch (Exception e) {
			logger.error("Error While Updating Lat and Lon :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public Integer getCorporateEstablishmentYear(Long applicationId, Long userId) throws LoansException {
		try {
			return applicantDetailRepository.getApplicantEstablishmentYear(userId, applicationId);
		} catch (Exception e) {
			logger.error("Error while getting Establishment Year : ",e);
		}
		return null;
	}

	@Override
	public Integer getCorporateEstablishmentYearFromProposalId(Long proposalId) throws Exception {
		try {
			return applicantDetailRepository.getApplicantEstablishmentYearFromProposalId(proposalId);
		} catch (Exception e) {
			logger.error("Error while getting Establishment Year = {}",e);
		}
		return null;
	}

	@Override
	public List<CorporateCoApplicantRequest> getCoApplicants(Long userId, Long applicationId) throws LoansException {
		return coApplicantService.getList(applicationId, userId);
	}

/*	@Override
	public boolean updateIsMsmeScoreRequired(MsmeScoreRequest msmeScoreRequest) throws Exception {
		boolean msmeScoreRequired = false;
		LoanApplicationMaster loanApplicationMaster = loanApplicationRepository
				.findOne(msmeScoreRequest.getApplicationId());
		if (msmeScoreRequest.isMsmeScoreRequired()) {
			loanApplicationMaster.setIsMsmeScoreRequired(true);
			msmeScoreRequired = true;
		} else {
			loanApplicationMaster.setIsMsmeScoreRequired(false);
			msmeScoreRequired = false;
		}
		return msmeScoreRequired;
	} */

	/*
	 * @Override public CompanyDetails getCompanyDetails(Long applicationId, Long
	 * userId) throws Exception { CorporateApplicantDetail corp =
	 * corporateApplicantDetailRepository.findOneByApplicationIdId(applicationId);
	 * CompanyDetails companyDetails = new CompanyDetails();
	 * companyDetails.setCompanyName(corp.getOrganisationName());
	 * companyDetails.setPan(corp.getPanNo()); companyDetails.setUserId(userId);
	 * return companyDetails; }
	 */

/*	@Override
	public boolean getIsMsmeScoreRequired(Long applicationId) throws Exception {
		LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.findOne(applicationId);
		if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getIsMsmeScoreRequired()))
			return false;
		boolean msmeScoreRequired = loanApplicationMaster.getIsMsmeScoreRequired();
		return msmeScoreRequired;
	} */

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getCoapAndGuarIds(Long userId, Long applicationId) throws LoansException {
		try {
			List<Long> coAppIds = coApplicantService.getCoAppIds(applicationId, userId);

			JSONObject obj = new JSONObject();
			obj.put("coAppIds", coAppIds);
			return obj;
		} catch (Exception e) {
			logger.error("Error while getCoapIds :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public PaymentRequest getPaymentInfor(Long userId, Long applicationId) throws LoansException {
		try {
			LoanApplicationMaster loanApplicationMaster = loanApplicationRepository
					.findOne(applicationId);
			PaymentRequest paymentRequest = new PaymentRequest();
			paymentRequest.setPaymentAmount(loanApplicationMaster.getPaymentAmount());
			paymentRequest.setTypeOfPayment(loanApplicationMaster.getTypeOfPayment());
			paymentRequest.setAppointmentDate(loanApplicationMaster.getAppointmentDate());
			paymentRequest.setAppointmentTime(loanApplicationMaster.getAppointmentTime());
			paymentRequest.setIsAcceptConsent(loanApplicationMaster.getIsAcceptConsent());
            if (CommonUtils.BusinessType.RETAIL_PERSONAL_LOAN.getId().equals(loanApplicationMaster.getBusinessTypeId())) {

                RetailApplicantDetail retailApplicantDetail = retailApplicantDetailRepository.findByApplicationId(applicationId);
                if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail)) {

                    String firstName = retailApplicantDetail.getFirstName();
                    String lastName =retailApplicantDetail.getLastName();
                    String middleName =retailApplicantDetail.getMiddleName();

                    String fullName = firstName==null?"":firstName;
                    fullName += middleName==null?"":" "+ middleName;
                    fullName += lastName==null?"":" "+lastName;

                    paymentRequest.setNameOfEntity(fullName);
                    Address address = new Address();
                    address.setPremiseNumber(retailApplicantDetail.getAddressPremiseName());
                    address.setStreetName(retailApplicantDetail.getAddressStreetName());
                    address.setLandMark(retailApplicantDetail.getAddressLandmark());
                    address.setCountryId(retailApplicantDetail.getAddressCountry());
                    address.setStateId(retailApplicantDetail.getAddressState().intValue());
                    address.setCityId(retailApplicantDetail.getAddressCity());
                    address.setPincode(retailApplicantDetail.getAddressPincode());
                    paymentRequest.setAddress(address);
                }
            } else {

                CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
                        .findOneByApplicationIdId(applicationId);
                if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail)) {
                    paymentRequest.setNameOfEntity(corporateApplicantDetail.getOrganisationName());
                    Address address = new Address();
                    address.setPremiseNumber(corporateApplicantDetail.getRegisteredPremiseNumber());
                    address.setStreetName(corporateApplicantDetail.getRegisteredStreetName());
                    address.setLandMark(corporateApplicantDetail.getRegisteredLandMark());
                    address.setCountryId(corporateApplicantDetail.getRegisteredCountryId());
                    address.setStateId(corporateApplicantDetail.getRegisteredStateId());
                    address.setCityId(corporateApplicantDetail.getRegisteredCityId());
                    address.setPincode(corporateApplicantDetail.getRegisteredPincode());
                    paymentRequest.setAddress(address);
                }
            }
			try {
				UserResponse userResponse = usersClient.getEmailMobile(loanApplicationMaster.getUserId());
				if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
					@SuppressWarnings("unchecked")
					UsersRequest request = MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
					paymentRequest.setEmailAddress(request.getEmail());
					paymentRequest.setMobileNumber(request.getMobile());
				}
			}catch(Exception e) {
				logger.error("Exception in getPaymentInfor : ",e);
			}
			return paymentRequest;
		} catch (Exception e) {
			logger.error("Error while Getting Payment Related Info :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public CorporateApplicantRequest getCorporateApplicant(Long applicationId) {
		logger.info("Start Method getCorporateApplicant Only for Application Id:-=>{}",applicationId);
		CorporateApplicantDetail applicantDetail = applicantRepository.findByApplicationIdIdAndIsActive(applicationId,true);
		logger.info("After Query Executions:-=>");
		if (applicantDetail == null) {
			logger.info("If Acpplicant Details is NULL:-=>");
			return null;
		}
		logger.info("If Acpplicant Details is NOT NULL:-=>");
		CorporateApplicantRequest applicantRequest = new CorporateApplicantRequest();
		logger.info("CorporateApplicantRequest Object new Created:-=>");
		BeanUtils.copyProperties(applicantDetail, applicantRequest);
		copyAddressFromDomainToRequest(applicantDetail, applicantRequest);
		applicantRequest.setLoanApplicationCreatedDate(loanApplicationRepository.getCreatedDateById(applicationId));
		logger.info("CorporateApplicantRequest Object new Created applicantRequest:-=>{}",applicantRequest.toString());
		logger.info("Data===>:-=>{}",applicantRequest.getGstIn() + "==============>");
		logger.info("Copy Domain to Request=======================:-=>");
		logger.info("ENd Method getCorporateApplicant Only for Application Id:-=>{}",applicationId);
		return applicantRequest;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getOrgAndPanByAppId(Long applicationId) {
		logger.info("Start Method getOrgAndPanByAppId Only for Application Id:-=>{}",applicationId);
		JSONObject obj =  new JSONObject();
		CorporateApplicantDetail applicantDetail = applicantRepository.findOneByApplicationIdId(applicationId);
		if (!CommonUtils.isObjectListNull(applicantDetail)) {
			obj.put("entityName", applicantDetail.getOrganisationName());
			obj.put("panNo", corporateApplicantDetailRepository.getPanNoByApplicationId(applicationId));
		}
		obj.put("amount", commonRepository.getSidbiAmount() != null ? commonRepository.getSidbiAmount() : 1180);
		obj.put("gatewayProvider", commonRepository.getGatewayProvider());
		PrimaryCorporateDetail primaryCorporateDetail = primaryCorporateDetailRepository.getOne(applicationId);
		if(!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail)
				&& !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getPurposeOfLoanId())){
			obj.put("purposeOfLoanId",primaryCorporateDetail.getPurposeOfLoanId());
		}
		logger.info("ENd Method getOrgAndPanByAppId Only for Application Id:-=>{}",applicationId);
		return obj;
	}

	@Override
	public Map getOrganizationNameFromApplicationId(Long applicationId) {
		Integer purposeOfLoanId = primaryCorporateDetailRepository.getPurposeLoanId(applicationId);
		String organizationName = corporateApplicantDetailRepository.getOrganizationNameFromId(applicationId);
		Map map = new HashMap();
		map.put("organizationName",organizationName);
		map.put("purposeOfLoanId",purposeOfLoanId);
		return map;
	}

	@Override
	public boolean saveITRResponse(CorporateApplicantRequest applicantRequest) throws LoansException {
		try {
    		// save premises info
			
			CorporateApplicantDetail applicantDetails = corporateApplicantDetailRepository.findByApplicationIdIdAndIsActive(applicantRequest.getApplicationId(), true);
			if(!CommonUtils.isObjectNullOrEmpty(applicantDetails)) {
			applicantDetails.setApplicationId(new LoanApplicationMaster(applicantRequest.getApplicationId()));
			applicantDetails.setRegisteredCityId(Long.valueOf(applicantRequest.getCityId()));
			applicantDetails.setRegisteredCountryId(applicantRequest.getCountryId());
			applicantDetails.setRegisteredLandMark(applicantRequest.getLandmark());
			applicantDetails.setRegisteredPincode(Long.valueOf(applicantRequest.getPincode()));
			applicantDetails.setRegisteredPremiseNumber(applicantRequest.getPremiseNo());
			applicantDetails.setRegisteredStreetName(applicantRequest.getStreetName());
			applicantDetails.setOrganisationName(applicantRequest.getOrganisationName());
			applicantDetails.setRegisteredStateId(applicantRequest.getStateId());
			applicantDetails.setPanNo(applicantRequest.getPan());
			applicantDetails.setCreatedDate(new Date());
			applicantDetails.setIsActive(true);
			applicantDetails.setRegisteredDistMappingId(applicantRequest.getDistId());
			applicantDetails.setDob(applicantRequest.getDob());
			corporateApplicantDetailRepository.save(applicantDetails);
    		saveSalesITRResponse(applicantRequest);
			}
    		
			//SAVE INCOME DETAILS
//			applicantIncomeService.saveAll(applicantRequest.getIncomeDetailsList());
			return true;
		} catch (Exception e) {
			logger.error("error while saving mudra manaul itr form",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean saveSalesITRResponse(CorporateApplicantRequest applicantRequest) throws LoansException {
		try {
			Boolean isItrManualFilled = loanRepository.getIsItrManualFilled(applicantRequest.getApplicationId());
			
			logger.info("Enter in saveSalesITRResponse");
			Map<String, Object> incomeDetails = applicantRequest.getIncomeDetails();
			
			Map<String, Object> liability = (Map<String, Object>) incomeDetails.get("totalLiabilities");
			Map<String, Object> networth = (Map<String, Object>) incomeDetails.get("networth");
			Map<String, Object> creditors = (Map<String, Object>) incomeDetails.get("creditors");
			
			Map<String, Object> sales = (Map<String, Object>) incomeDetails.get("sales");
			Map<String, Object> profitAfterTax = (Map<String, Object>) incomeDetails.get("profitAfterTax");
			
			Map<String, Object> totalAssets = (Map<String, Object>) incomeDetails.get("totalAssets");
			Map<String, Object> inventory = (Map<String, Object>) incomeDetails.get("inventory");
			Map<String, Object> debtors = (Map<String, Object>) incomeDetails.get("debtors");
			Map<String, Object> investmentInPlantMachinery = (Map<String, Object>) incomeDetails.get("investmentInPlantMachinery");
			
			Year currentYear = Year.now();
			
			Map<String, Object>	 yearMapLiability  = (Map<String, Object>) liability.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearMapLiability.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
    			LiabilitiesDetails liabilitiesDetails = liabilitiesDetailsRepository.findByFsLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
    			if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails)) {
    				liabilitiesDetails = new LiabilitiesDetails();
    			}
		    	liabilitiesDetails.setYear(yearEntry.getKey());
		    	liabilitiesDetails.setTotalLiability(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
		    	liabilitiesDetails.setCreatedDate(new Date());
		    	liabilitiesDetails.setCreatedBy(applicantRequest.getUserId());
		    	liabilitiesDetails.setFsLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		    	liabilitiesDetails.setIsActive(true);
		    	//For Project Sales
	    		liabilitiesDetails.setFinancialYearlyStatement(finYearStmt);
		    	liabilitiesDetailsRepository.save(liabilitiesDetails); 
    		}
    		
    		Map<String, Object>	 yearMapSales  = (Map<String, Object>) sales.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearMapSales.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
		    	OperatingStatementDetails operatingStatementDetails = operatingStatementDetailsRepository.findByLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
		    	if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails)) {
		    		operatingStatementDetails = new OperatingStatementDetails();
		    	}
		    	Double salesValue = yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : 0.0d;
		    	operatingStatementDetails.setYear(yearEntry.getKey());
		    	operatingStatementDetails.setNetSales(salesValue);
		    	if(finYearStmt.equals("Projected")) {
		    		operatingStatementDetails.setDomesticSales(salesValue); // Discussed with gaurav bhai		    		
		    	}
		    	operatingStatementDetails.setCreatedDate(new Date());
		    	operatingStatementDetails.setCreatedBy(applicantRequest.getUserId());
		    	operatingStatementDetails.setLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		    	operatingStatementDetails.setFinancialYearlyStatement(finYearStmt);
		    	operatingStatementDetails.setIsActive(true);
		    	operatingStatementDetailsRepository.save(operatingStatementDetails); 
    		}
    		
//    		Map<String, Object>	 yearMapNetworth  = (Map<String, Object>) networth.get("year"); 
//    		for (Map.Entry<String,Object> yearEntry : yearMapNetworth.entrySet()) {
//    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
//    			LiabilitiesDetails liabilitiesDetails = liabilitiesDetailsRepository.findByFsLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
//    			if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails)) {
//    				liabilitiesDetails = new LiabilitiesDetails();
//    			}
//		    	liabilitiesDetails.setYear(yearEntry.getKey());
//		    	liabilitiesDetails.setNetWorth(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
//		    	liabilitiesDetails.setCreatedDate(new Date());
//		    	liabilitiesDetails.setCreatedBy(applicantRequest.getUserId());
//		    	liabilitiesDetails.setFsLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
//		    	liabilitiesDetails.setFinancialYearlyStatement(finYearStmt);
//		    	liabilitiesDetails.setIsActive(true);
//		    	liabilitiesDetailsRepository.save(liabilitiesDetails); 
//    		}
    		
    		Map<String, Object>	 yearMapProfitAfterTax  = (Map<String, Object>) profitAfterTax.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearMapProfitAfterTax.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
		    	OperatingStatementDetails operatingStatementDetails = operatingStatementDetailsRepository.findByLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
		    	if(CommonUtils.isObjectNullOrEmpty(operatingStatementDetails)) {
		    		operatingStatementDetails = new OperatingStatementDetails();
		    	}
		    	operatingStatementDetails.setYear(yearEntry.getKey());
		    	operatingStatementDetails.setNetProfitOrLoss(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
		    	operatingStatementDetails.setCreatedDate(new Date());
		    	operatingStatementDetails.setCreatedBy(applicantRequest.getUserId());
		    	operatingStatementDetails.setLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		    	operatingStatementDetails.setFinancialYearlyStatement(finYearStmt);
		    	operatingStatementDetails.setIsActive(true);
		    	operatingStatementDetailsRepository.save(operatingStatementDetails); 
    		}
    		
    		Map<String, Object>	 yearMapTotalAssets  = (Map<String, Object>) totalAssets.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearMapTotalAssets.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
    			AssetsDetails assetsDetails = assetsDetailsRepository.findByLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
    			if(CommonUtils.isObjectNullOrEmpty(assetsDetails)) {
    				assetsDetails = new AssetsDetails();
    			}
		    	assetsDetails.setYear(yearEntry.getKey());
		    	assetsDetails.setTotalAssets(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
		    	assetsDetails.setCreatedDate(new Date());
		    	assetsDetails.setCreatedBy(applicantRequest.getUserId());
		    	assetsDetails.setLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		    	assetsDetails.setFinancialYearlyStatement(finYearStmt);
		    	assetsDetails.setIsActive(true);
		    	assetsDetailsRepository.save(assetsDetails); 
    		}
    		
    		Map<String, Object>	 yearMapNetworth  = (Map<String, Object>) networth.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearMapNetworth.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
    			AssetsDetails assetsDetails = assetsDetailsRepository.findByLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
    			if(CommonUtils.isObjectNullOrEmpty(assetsDetails)) {
    				assetsDetails = new AssetsDetails();
    			}
		    	assetsDetails.setYear(yearEntry.getKey());
		    	assetsDetails.setTangibleNetWorth(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
		    	assetsDetails.setCreatedDate(new Date());
		    	assetsDetails.setCreatedBy(applicantRequest.getUserId());
		    	assetsDetails.setLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		    	assetsDetails.setFinancialYearlyStatement(finYearStmt);
		    	assetsDetails.setIsActive(true);
		    	assetsDetailsRepository.save(assetsDetails);
    		}
    		
    		
    		Map<String, Object>	 yearMapinventory  = (Map<String, Object>) inventory.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearMapinventory.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
    			AssetsDetails assetsDetails = assetsDetailsRepository.findByLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
    			if(CommonUtils.isObjectNullOrEmpty(assetsDetails)) {
    				assetsDetails = new AssetsDetails();
    			}
		    	assetsDetails.setYear(yearEntry.getKey());
		    	assetsDetails.setInventory(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
		    	assetsDetails.setCreatedDate(new Date());
		    	assetsDetails.setCreatedBy(applicantRequest.getUserId());
		    	assetsDetails.setLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		    	assetsDetails.setFinancialYearlyStatement(finYearStmt);
		    	assetsDetails.setIsActive(true);
		    	assetsDetailsRepository.save(assetsDetails); 
    		}
    		
    		Map<String, Object>	 yearMapdebtors  = (Map<String, Object>) debtors.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearMapdebtors.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
    			AssetsDetails assetsDetails = assetsDetailsRepository.findByLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
    			if(CommonUtils.isObjectNullOrEmpty(assetsDetails)) {
    				assetsDetails = new AssetsDetails();
    			}
		    	assetsDetails.setYear(yearEntry.getKey());
		    	assetsDetails.setDebtors(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
		    	assetsDetails.setCreatedDate(new Date());
		    	assetsDetails.setCreatedBy(applicantRequest.getUserId());
		    	assetsDetails.setLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		    	assetsDetails.setFinancialYearlyStatement(finYearStmt);
		    	assetsDetails.setIsActive(true);
		    	assetsDetailsRepository.save(assetsDetails); 
    		}
    		
    		Map<String, Object>	 yearInvestmentInPlantMachinery  = (Map<String, Object>) investmentInPlantMachinery.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearInvestmentInPlantMachinery.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
    			AssetsDetails assetsDetails = assetsDetailsRepository.findByLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
    			if(CommonUtils.isObjectNullOrEmpty(assetsDetails)) {
    				assetsDetails = new AssetsDetails();
    			}
    			assetsDetails.setYear(yearEntry.getKey());
    			assetsDetails.setInvestmentInPlantMachinery(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
    			assetsDetails.setCreatedDate(new Date());
    			assetsDetails.setCreatedBy(applicantRequest.getUserId());
    			assetsDetails.setLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
    			assetsDetails.setFinancialYearlyStatement(finYearStmt);
    			assetsDetails.setIsActive(true);
    			assetsDetailsRepository.save(assetsDetails); 
    		}
    		
    		Map<String, Object>	 yearMapcreditors  = (Map<String, Object>) creditors.get("year"); 
    		for (Map.Entry<String,Object> yearEntry : yearMapcreditors.entrySet()) {
    			String finYearStmt = yearEntry.getKey().equals(""+currentYear) ? "Projected":"Audited";
    			LiabilitiesDetails liabilitiesDetails = liabilitiesDetailsRepository.findByFsLoanApplicationMasterIdAndYearAndFinancialYearlyStatementAndIsActive(applicantRequest.getApplicationId(), yearEntry.getKey(), finYearStmt, true);
    			if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails)) {
    				liabilitiesDetails = new LiabilitiesDetails();
    			}
		    	liabilitiesDetails.setYear(yearEntry.getKey());
		    	liabilitiesDetails.setCreditors(yearEntry.getValue() != null ? Double.parseDouble(yearEntry.getValue().toString()) : null);
		    	liabilitiesDetails.setCreatedDate(new Date());
		    	liabilitiesDetails.setCreatedBy(applicantRequest.getUserId());
		    	liabilitiesDetails.setFsLoanApplicationMaster(new LoanApplicationMaster(applicantRequest.getApplicationId()));
		    	liabilitiesDetails.setFinancialYearlyStatement(finYearStmt);
		    	liabilitiesDetails.setIsActive(true);
		    	liabilitiesDetailsRepository.save(liabilitiesDetails);
    		}
			return true;
		} catch (Exception e) {
			logger.error("error while saving Projected Sales details mudra itr",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	@Override
	public CorporateApplicantRequest getCorporateApplicantDetails(Long applicationId) {
		
		CorporateApplicantDetail  applicantRequest =  corporateApplicantDetailRepository.findByApplicationIdIdAndIsActive(applicationId, true);
		CorporateApplicantRequest corporateApplicantRequest = new CorporateApplicantRequest();
		if(!CommonUtils.isObjectNullOrEmpty(applicantRequest)) {
			BeanUtils.copyProperties(applicantRequest, corporateApplicantRequest);
		}
		
		/******* GET INCOME DETAILS *******/
		Map<String, Object> incomeDetails = new HashMap<>();
		
		String sales = operatingStatementDetailsRepository.getNetSales(applicationId );
		incomeDetails.put("sales", sales);
		
		String profitAfterTax = operatingStatementDetailsRepository.getNetProfitOrLoss(applicationId);
		incomeDetails.put("profitAfterTax", profitAfterTax);

		String inventory = assetsDetailsRepository.getInventory(applicationId);
		incomeDetails.put("inventory", inventory);
		
		String debtors = assetsDetailsRepository.getDebtors(applicationId);
		incomeDetails.put("debtors", debtors);
		
		String creditors = liabilitiesDetailsRepository.getCreditors(applicationId);
		incomeDetails.put("creditors", creditors);

		String investmentInPlantMachinery = assetsDetailsRepository.getInvestmentInPlantMachinery(applicationId);
		incomeDetails.put("investmentInPlantMachinery", investmentInPlantMachinery);
		
		String networth = assetsDetailsRepository.getTangibleNetWorth(applicationId);
		incomeDetails.put("networth", networth);
		
		String totalAssets = assetsDetailsRepository.getTotalAssets(applicationId);
		incomeDetails.put("totalAssets", totalAssets);
		
		String liability = liabilitiesDetailsRepository.getTotalLiability(applicationId);
		incomeDetails.put("totalLiabilities", liability);
		
		corporateApplicantRequest.setIncomeDetails(incomeDetails);
		return corporateApplicantRequest;
	}
}
