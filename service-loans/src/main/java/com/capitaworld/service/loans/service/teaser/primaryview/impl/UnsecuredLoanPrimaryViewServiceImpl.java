package com.capitaworld.service.loans.service.teaser.primaryview.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capitaworld.service.dms.client.DMSClient;
import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.model.DocumentRequest;
import com.capitaworld.service.dms.model.DocumentResponse;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryUnsecuredLoanDetail;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.AddressResponse;
import com.capitaworld.service.loans.model.DirectorBackgroundDetailRequest;
import com.capitaworld.service.loans.model.DirectorBackgroundDetailResponse;
import com.capitaworld.service.loans.model.FinancialArrangementsDetailRequest;
import com.capitaworld.service.loans.model.FinancialArrangementsDetailResponse;
import com.capitaworld.service.loans.model.teaser.primaryview.UnsecuredLoanPrimaryViewResponse;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryUnsecuredLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SubSectorRepository;
import com.capitaworld.service.loans.service.common.CommonService;
import com.capitaworld.service.loans.service.fundseeker.corporate.DirectorBackgroundDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.ExistingProductDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.ProposedProductDetailsService;
import com.capitaworld.service.loans.service.teaser.primaryview.UnsecuredLoanPrimaryViewService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.matchengine.MatchEngineClient;
import com.capitaworld.service.matchengine.model.MatchDisplayResponse;
import com.capitaworld.service.matchengine.model.MatchRequest;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.Constitution;
import com.capitaworld.service.oneform.enums.CreditRatingAvailable;
import com.capitaworld.service.oneform.enums.Currency;
import com.capitaworld.service.oneform.enums.Denomination;
import com.capitaworld.service.oneform.enums.EstablishmentMonths;
import com.capitaworld.service.oneform.enums.Gender;
import com.capitaworld.service.oneform.enums.LoanType;
import com.capitaworld.service.oneform.enums.RelationshipType;
import com.capitaworld.service.oneform.enums.Title;
import com.capitaworld.service.oneform.model.IndustrySectorSubSectorTeaserRequest;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;

@Service
public class UnsecuredLoanPrimaryViewServiceImpl implements UnsecuredLoanPrimaryViewService {

	private static final Logger logger = LoggerFactory.getLogger(UnsecuredLoanPrimaryViewServiceImpl.class);
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;

	@Autowired
	private PrimaryUnsecuredLoanDetailRepository primaryUnsecuredLoanLoanDetailRepository;

	@Autowired
	private ProposedProductDetailsService proposedProductDetailsService;

	@Autowired
	private ExistingProductDetailsService existingProductDetailsService;

	@Autowired
	private FinancialArrangementDetailsService financialArrangementDetailsService;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private IndustrySectorRepository industrySectorRepository;

	@Autowired
	private SubSectorRepository subSectorRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private MatchEngineClient matchEngineClient;

	@Autowired
	private UsersClient usersClient;

	@Autowired
	private DirectorBackgroundDetailsService directorBackgroundDetailsService;

	@Autowired
	private PrimaryCorporateDetailRepository primaryCorporateDetailRepository;
	
	@Autowired
	private CommonService commonService;

	@Override
	public UnsecuredLoanPrimaryViewResponse getUnsecuredLoanPrimaryViewDetails(Long toApplicationId, Integer userType,
																			   Long fundProviderUserId) throws LoansException {
		
		Long cityId = null ;
		Integer stateId = null;
		Integer countryId = null;
		String cityName = null;
		String stateName = null;
		String countryName = null;
		
		UnsecuredLoanPrimaryViewResponse unsecuredLoanPrimaryViewResponse = new UnsecuredLoanPrimaryViewResponse();

		if (userType != null && !(CommonUtils.UserType.FUND_SEEKER == userType) ) {
			     // teaser
				// view
				// viwed by
				// fund
				// provider
				Long fpProductMappingId = null;
				try {

					UsersRequest usersRequest = new UsersRequest();
					usersRequest.setId(fundProviderUserId);
					UserResponse userResponse= usersClient.getLastAccessApplicant(usersRequest);
					fpProductMappingId=userResponse.getId();
				} catch (Exception e) {
					logger.error("error while fetching last access fp rpduct id for fund provider while fetching matches in teaser view : ",e);
				}
				try {
					MatchRequest matchRequest = new MatchRequest();
					matchRequest.setApplicationId(toApplicationId);
					matchRequest.setProductId(fpProductMappingId);
					MatchDisplayResponse matchResponse = matchEngineClient.displayMatchesOfCorporate(matchRequest);
					unsecuredLoanPrimaryViewResponse.setMatchesList(matchResponse.getMatchDisplayObjectList());
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
		}

		LoanApplicationMaster applicationMaster = loanApplicationRepository.findOne(toApplicationId);
		Long userId = applicationMaster.getUserId();
		// get details of CorporateApplicantDetail
		CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
				.getByApplicationAndUserId(userId, toApplicationId);
		if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail)) {
			// set value to response
			if (corporateApplicantDetail != null)
				BeanUtils.copyProperties(corporateApplicantDetail, unsecuredLoanPrimaryViewResponse);
			if (corporateApplicantDetail.getConstitutionId() != null)
				unsecuredLoanPrimaryViewResponse
						.setConstitution(Constitution.getById(corporateApplicantDetail.getConstitutionId()).getValue());
			if (corporateApplicantDetail.getEstablishmentMonth() != null)
				unsecuredLoanPrimaryViewResponse.setEstablishmentMonth(
						EstablishmentMonths.getById(corporateApplicantDetail.getEstablishmentMonth()).getValue());

			//Set Registered Data
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCityId()))
				cityId = corporateApplicantDetail.getRegisteredCityId();
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredStateId()))
				stateId = corporateApplicantDetail.getRegisteredStateId();
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCountryId()))
				countryId = corporateApplicantDetail.getRegisteredCountryId();
			
			if(cityId != null || stateId != null || countryId != null) {
				Map<String ,Object> mapData = commonService.getCityStateCountryNameFromOneForm(cityId, stateId, countryId);
				if(mapData != null) {
					cityName = mapData.get(CommonUtils.CITY_NAME).toString();
					stateName = mapData.get(CommonUtils.STATE_NAME).toString();
					countryName = mapData.get(CommonUtils.COUNTRY_NAME).toString();
					
					//set City
					unsecuredLoanPrimaryViewResponse.setCity(cityName != null ? cityName : "NA");
					unsecuredLoanPrimaryViewResponse.setRegOfficeCity(cityName);
					
					//set State
					unsecuredLoanPrimaryViewResponse.setState(stateName != null ? stateName : "NA");
					unsecuredLoanPrimaryViewResponse.setRegOfficestate(stateName);
					
					//set Country
					unsecuredLoanPrimaryViewResponse.setCountry(countryName != null ? countryName : "NA");
					unsecuredLoanPrimaryViewResponse.setRegOfficecountry(countryName);
				}
			}
			
			// set city
			/**List<Long> cityList = new ArrayList<>();
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCityId()))
				cityList.add(corporateApplicantDetail.getRegisteredCityId());
			if(!CommonUtils.isListNullOrEmpty(cityList))
			{
				try {
					OneFormResponse oneFormResponse = oneFormClient.getCityByCityListId(cityList);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						unsecuredLoanPrimaryViewResponse.setCity(masterResponse.getValue());
						unsecuredLoanPrimaryViewResponse.setRegOfficeCity(masterResponse.getValue());
					} else {
						unsecuredLoanPrimaryViewResponse.setCity("NA");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}

			cityList.clear();
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeCityId()))
				cityList.add(corporateApplicantDetail.getAdministrativeCityId());
			if(!CommonUtils.isListNullOrEmpty(cityList))
			{
				try {
					OneFormResponse oneFormResponse = oneFormClient.getCityByCityListId(cityList);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						unsecuredLoanPrimaryViewResponse.setAddOfficeCity(masterResponse.getValue());

					} else {
						unsecuredLoanPrimaryViewResponse.setCity("NA");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}


			// set state
			List<Long> stateList = new ArrayList<>();
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredStateId()))
				stateList.add(Long.valueOf(corporateApplicantDetail.getRegisteredStateId()));
			if(!CommonUtils.isListNullOrEmpty(stateList))
			{
				try {
					OneFormResponse oneFormResponse = oneFormClient.getStateByStateListId(stateList);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						unsecuredLoanPrimaryViewResponse.setState(masterResponse.getValue());
						unsecuredLoanPrimaryViewResponse.setRegOfficestate(masterResponse.getValue());
					} else {
						unsecuredLoanPrimaryViewResponse.setState("NA");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}


			stateList.clear();
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeStateId()))
				stateList.add(Long.valueOf(corporateApplicantDetail.getAdministrativeStateId()));
			if(!CommonUtils.isListNullOrEmpty(stateList))
			{
				try {
					OneFormResponse oneFormResponse = oneFormClient.getStateByStateListId(stateList);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						unsecuredLoanPrimaryViewResponse.setAddOfficestate(masterResponse.getValue());
					} else {
						unsecuredLoanPrimaryViewResponse.setState("NA");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}
			// set country
			List<Long> countryList = new ArrayList<>();
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCountryId()))
				countryList.add(Long.valueOf(corporateApplicantDetail.getRegisteredCountryId()));
			if(!CommonUtils.isListNullOrEmpty(countryList))
			{
				try {
					OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						unsecuredLoanPrimaryViewResponse.setCountry(masterResponse.getValue());
						unsecuredLoanPrimaryViewResponse.setRegOfficecountry(masterResponse.getValue());
					} else {
						unsecuredLoanPrimaryViewResponse.setCountry("NA");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}

			countryList.clear();
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeCountryId()))
				countryList.add(Long.valueOf(corporateApplicantDetail.getAdministrativeCountryId()));
			if(!CommonUtils.isListNullOrEmpty(countryList))
			{
				try {
					OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						unsecuredLoanPrimaryViewResponse.setAddOfficecountry(masterResponse.getValue());
					} else {
						unsecuredLoanPrimaryViewResponse.setCountry("NA");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}*/


			List<Long> keyVerticalFundingId = new ArrayList<>();
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVericalFunding()))
				keyVerticalFundingId.add(corporateApplicantDetail.getKeyVericalFunding());
			try {
				OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
				List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
						.getListData();
				if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
					MasterResponse masterResponse = MultipleJSONObjectHelper
							.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
					unsecuredLoanPrimaryViewResponse.setKeyVericalFunding(masterResponse.getValue());
				} else {
					unsecuredLoanPrimaryViewResponse.setKeyVericalFunding(CommonUtils.NOT_APPLICABLE);
				}

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}

		}
		List<Long> industryList = industrySectorRepository.getIndustryByApplicationId(toApplicationId);
		List<Long> sectorList = industrySectorRepository.getSectorByApplicationId(toApplicationId);
		List<Long> subSectorList = subSectorRepository.getSubSectorByApplicationId(toApplicationId);

		IndustrySectorSubSectorTeaserRequest industrySectorSubSectorTeaserRequest = new IndustrySectorSubSectorTeaserRequest();
		industrySectorSubSectorTeaserRequest.setIndustryList(industryList);
		industrySectorSubSectorTeaserRequest.setSectorList(sectorList);
		industrySectorSubSectorTeaserRequest.setSubSectorList(subSectorList);
		if (industryList != null && !industryList.isEmpty()) {
			try {
				OneFormResponse oneFormResponse = oneFormClient
						.getIndustrySectorSubSector(industrySectorSubSectorTeaserRequest);
				unsecuredLoanPrimaryViewResponse.setIndustrySector(oneFormResponse.getListData());
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}
		// get value of Term Loan data
		PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetail = primaryUnsecuredLoanLoanDetailRepository
				.getByApplicationAndUserId(toApplicationId, userId);
		PrimaryCorporateDetail primaryCorporateDetail=primaryCorporateDetailRepository.getByApplicationAndUserId(userId, toApplicationId);

		// set value to response
		if (primaryUnsecuredLoanDetail != null) {
			BeanUtils.copyProperties(primaryUnsecuredLoanDetail, unsecuredLoanPrimaryViewResponse);
			unsecuredLoanPrimaryViewResponse.setTenure(primaryUnsecuredLoanDetail.getTenure() != null ? primaryUnsecuredLoanDetail.getTenure() / 12 : null);

			unsecuredLoanPrimaryViewResponse.setPurposeOfLoan(primaryUnsecuredLoanDetail.getPurposeOfLoan() != null ? primaryUnsecuredLoanDetail.getPurposeOfLoan() : null);
			/*unsecuredLoanPrimaryViewResponse.setSharePriceFace(primaryUnsecuredLoanDetail.getSharePriceFace());
			unsecuredLoanPrimaryViewResponse.setSharePriceMarket(primaryUnsecuredLoanDetail.getSharePriceMarket());*/
			unsecuredLoanPrimaryViewResponse.setGstin(corporateApplicantDetail.getGstIn() != null ? String.valueOf(corporateApplicantDetail.getGstIn()) : null);
			unsecuredLoanPrimaryViewResponse.setHaveCollateralSecurity(primaryCorporateDetail.getHaveCollateralSecurity() != null ? String.valueOf(primaryCorporateDetail.getHaveCollateralSecurity()) : null);
			unsecuredLoanPrimaryViewResponse.setCollateralSecurityAmount(primaryCorporateDetail.getCollateralSecurityAmount() != null ? String.valueOf(primaryCorporateDetail.getCollateralSecurityAmount()) : null);
			if (!CommonUtils.isObjectNullOrEmpty(primaryUnsecuredLoanDetail.getCurrencyId())&&!CommonUtils.isObjectNullOrEmpty(primaryUnsecuredLoanDetail.getDenominationId()))
				unsecuredLoanPrimaryViewResponse.setCurrencyDenomination(Currency.getById(primaryUnsecuredLoanDetail.getCurrencyId()).getValue() + " in " + Denomination.getById(primaryUnsecuredLoanDetail.getDenominationId()).getValue());
			if (primaryUnsecuredLoanDetail.getProductId() != null)
				unsecuredLoanPrimaryViewResponse.setLoanType(LoanType.getById(primaryUnsecuredLoanDetail.getProductId()).getValue());

			if (primaryUnsecuredLoanDetail.getModifiedDate() != null)
				unsecuredLoanPrimaryViewResponse
						.setDateOfProposal(simpleDateFormat.format(primaryUnsecuredLoanDetail.getModifiedDate()));
			unsecuredLoanPrimaryViewResponse.setIsCreditRatingAvailable(primaryUnsecuredLoanDetail.getCreditRatingId() != null
					? CreditRatingAvailable.getById(primaryUnsecuredLoanDetail.getCreditRatingId()).getValue() : null);
		}
		// get value of proposed product and set in response
		try {
			unsecuredLoanPrimaryViewResponse.setProposedProductDetailRequestList(
					proposedProductDetailsService.getProposedProductDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Proposed Product {}", e);
		}

		// get value of Existing product and set in response
		try {
			unsecuredLoanPrimaryViewResponse.setExistingProductDetailRequestList(
					existingProductDetailsService.getExistingProductDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Existing Product {}", e);
		}

		// get value of achievement details and set in response
/*		try {
			unsecuredLoanPrimaryViewResponse.setAchievementDetailList(
					achievmentDetailsService.getAchievementDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Achievement Details {}", e);
		}

		// get value of Credit Rating and set in response
		try {
			List<CreditRatingOrganizationDetailRequest> creditRatingOrganizationDetailRequestList = creditRatingOrganizationDetailsService
					.getcreditRatingOrganizationDetailsList(toApplicationId, userId);
			List<CreditRatingOrganizationDetailResponse> creditRatingOrganizationDetailResponseList = new ArrayList<>();
			for (CreditRatingOrganizationDetailRequest creditRatingOrganizationDetailRequest : creditRatingOrganizationDetailRequestList) {
				CreditRatingOrganizationDetailResponse creditRatingOrganizationDetailResponse = new CreditRatingOrganizationDetailResponse();
				creditRatingOrganizationDetailResponse.setAmount(creditRatingOrganizationDetailRequest.getAmount());
				creditRatingOrganizationDetailResponse.setCreditRatingFund(creditRatingOrganizationDetailRequest.getCreditRatingFundId() != null ? CreditRatingFund.getById(creditRatingOrganizationDetailRequest.getCreditRatingFundId()).getValue() : null);

				OneFormResponse oneFormResponse = oneFormClient.getRatingById(
						CommonUtils.isObjectNullOrEmpty(creditRatingOrganizationDetailRequest.getCreditRatingOptionId())
								? null : creditRatingOrganizationDetailRequest.getCreditRatingOptionId().longValue());
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) oneFormResponse.getData(), MasterResponse.class);
				if (masterResponse != null) {
					creditRatingOrganizationDetailResponse.setCreditRatingOption(masterResponse.getValue());
				} else {
					creditRatingOrganizationDetailResponse.setCreditRatingOption(CommonUtils.NOT_APPLICABLE);
				}

				if (creditRatingOrganizationDetailRequest.getCreditRatingTermId() != null)
					creditRatingOrganizationDetailResponse.setCreditRatingTerm(CreditRatingTerm
							.getById(creditRatingOrganizationDetailRequest.getCreditRatingTermId()).getValue());
				if (creditRatingOrganizationDetailRequest.getRatingAgencyId() != null)
					creditRatingOrganizationDetailResponse.setRatingAgency(
							RatingAgency.getById(creditRatingOrganizationDetailRequest.getRatingAgencyId()).getValue());
				creditRatingOrganizationDetailResponse
						.setFacilityName(creditRatingOrganizationDetailRequest.getFacilityName());
				creditRatingOrganizationDetailResponseList.add(creditRatingOrganizationDetailResponse);
				creditRatingOrganizationDetailResponse.setEntityName(creditRatingOrganizationDetailRequest.getEntityName());
				if (creditRatingOrganizationDetailRequest.getRatingDate() != null){
					creditRatingOrganizationDetailResponse.setRatingDate(creditRatingOrganizationDetailRequest.getRatingDate());
				}
			}
			unsecuredLoanPrimaryViewResponse
					.setCreditRatingOrganizationDetailResponse(creditRatingOrganizationDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Credit Rating {}", e);
		}

		// get value of Ownership Details and set in response
		try {
			List<OwnershipDetailRequest> ownershipDetailRequestsList = ownershipDetailsService
					.getOwnershipDetailList(toApplicationId, userId);
			List<OwnershipDetailResponse> ownershipDetailResponseList = new ArrayList<>();

			for (OwnershipDetailRequest ownershipDetailRequest : ownershipDetailRequestsList) {
				OwnershipDetailResponse ownershipDetailResponse = new OwnershipDetailResponse();
				BeanUtils.copyProperties(ownershipDetailRequest, ownershipDetailResponse);
				if (ownershipDetailRequest.getShareHoldingCategoryId() != null)
					ownershipDetailResponse.setShareHoldingCategory(ShareHoldingCategory
							.getById(ownershipDetailRequest.getShareHoldingCategoryId()).getValue());
				ownershipDetailResponseList.add(ownershipDetailResponse);
			}
			unsecuredLoanPrimaryViewResponse.setOwnershipDetailResponseList(ownershipDetailResponseList);

		} catch (Exception e) {
			logger.error("Problem to get Data of Ownership Details {}", e);
		}

		// get value of Promotor Background and set in response
		try {
			List<PromotorBackgroundDetailRequest> promotorBackgroundDetailRequestList = promotorBackgroundDetailsService.getPromotorBackgroundDetailList(toApplicationId, userId);
			List<PromotorBackgroundDetailResponse> promotorBackgroundDetailResponseList = new ArrayList<>();
			for (PromotorBackgroundDetailRequest promotorBackgroundDetailRequest : promotorBackgroundDetailRequestList) {
				PromotorBackgroundDetailResponse promotorBackgroundDetailResponse = new PromotorBackgroundDetailResponse();
				BeanUtils.copyProperties(promotorBackgroundDetailRequest, promotorBackgroundDetailResponse);
				promotorBackgroundDetailResponse.setAchievements(promotorBackgroundDetailRequest.getAchivements());
				String promotorName = "";
				if (promotorBackgroundDetailRequest.getSalutationId() != null){
					promotorName = Title.getById(promotorBackgroundDetailRequest.getSalutationId()).getValue();
				}
				promotorName += " "+promotorBackgroundDetailRequest.getPromotorsName();
				promotorBackgroundDetailResponse.setPromotorsName(promotorName);
				promotorBackgroundDetailResponse.setNetworth(promotorBackgroundDetailRequest.getNetworth());
				promotorBackgroundDetailResponseList.add(promotorBackgroundDetailResponse);
			}
			unsecuredLoanPrimaryViewResponse.setPromotorBackgroundDetailResponseList(promotorBackgroundDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Promotor Background {}", e);
		} */

		//get value of Director's Background and set in response

		try {
			List<DirectorBackgroundDetailRequest> directorBackgroundDetailRequestList = directorBackgroundDetailsService.getDirectorBackgroundDetailList(toApplicationId, userId);
			List<DirectorBackgroundDetailResponse> directorBackgroundDetailResponseList = new ArrayList<>();
			for (DirectorBackgroundDetailRequest directorBackgroundDetailRequest : directorBackgroundDetailRequestList) {
				DirectorBackgroundDetailResponse directorBackgroundDetailResponse = new DirectorBackgroundDetailResponse();
				//directorBackgroundDetailResponse.setAchivements(directorBackgroundDetailRequest.getAchivements());
				directorBackgroundDetailResponse.setAddress(directorBackgroundDetailRequest.getAddress());
				//directorBackgroundDetailResponse.setAge(directorBackgroundDetailRequest.getAge());
				directorBackgroundDetailResponse.setPanNo(directorBackgroundDetailRequest.getPanNo());
				directorBackgroundDetailResponse.setDirectorsName((directorBackgroundDetailRequest.getSalutationId() != null ? Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue() : null )+ " " + directorBackgroundDetailRequest.getDirectorsName());
				directorBackgroundDetailResponse.setPanNo(directorBackgroundDetailRequest.getPanNo().toUpperCase());
				String directorName = "";
				if (directorBackgroundDetailRequest.getSalutationId() != null){
					directorName = Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue();
				}
				directorName += " "+directorBackgroundDetailRequest.getDirectorsName();
				directorBackgroundDetailResponse.setDirectorsName(directorName);
				//directorBackgroundDetailResponse.setQualification(directorBackgroundDetailRequest.getQualification());
				directorBackgroundDetailResponse.setTotalExperience(directorBackgroundDetailRequest.getTotalExperience().toString());
				directorBackgroundDetailResponse.setNetworth(directorBackgroundDetailRequest.getNetworth().toString());
				directorBackgroundDetailResponse.setDesignation(directorBackgroundDetailRequest.getDesignation());
				directorBackgroundDetailResponse.setAppointmentDate(directorBackgroundDetailRequest.getAppointmentDate());
				directorBackgroundDetailResponse.setDin(directorBackgroundDetailRequest.getDin());
				directorBackgroundDetailResponse.setMobile(directorBackgroundDetailRequest.getMobile());
				directorBackgroundDetailResponse.setDob(directorBackgroundDetailRequest.getDob());
				directorBackgroundDetailResponse.setPincode(directorBackgroundDetailRequest.getPincode());
				directorBackgroundDetailResponse.setStateCode(directorBackgroundDetailRequest.getStateCode());
				directorBackgroundDetailResponse.setCity(directorBackgroundDetailRequest.getCity());
				directorBackgroundDetailResponse.setGender((directorBackgroundDetailRequest.getGender() != null ? Gender.getById(directorBackgroundDetailRequest.getGender()).getValue() : " " ));
				directorBackgroundDetailResponse.setRelationshipType((directorBackgroundDetailRequest.getRelationshipType() != null ? RelationshipType.getById(directorBackgroundDetailRequest.getRelationshipType()).getValue() : " " ));
				directorBackgroundDetailResponseList.add(directorBackgroundDetailResponse);

			}
			unsecuredLoanPrimaryViewResponse.setDirectorBackgroundDetailResponses(directorBackgroundDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Director's Background {}", e);
		}

		// get value of Past Financial and set in response
/*		try {
			unsecuredLoanPrimaryViewResponse.setPastFinancialEstimatesDetailRequestList(
					pastFinancialEstiamateDetailsService.getFinancialListData(userId, toApplicationId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Past Financial {}", e);
		}

		// get value of Future Projection and set in response
		try {
			unsecuredLoanPrimaryViewResponse
					.setFutureFinancialEstimatesDetailRequestList(futureFinancialEstimatesDetailsService
							.getFutureFinancialEstimateDetailsList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Future Projection {}", e);
		}

		// get value of Security and set in response
		try {
			unsecuredLoanPrimaryViewResponse.setSecurityCorporateDetailRequestList(
					securityCorporateDetailsService.getsecurityCorporateDetailsList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Security Details {}", e);
 		}  */

		// get value of Financial Arrangements and set in response
		try {
			List<FinancialArrangementsDetailRequest> financialArrangementsDetailRequestList = financialArrangementDetailsService
					.getFinancialArrangementDetailsList(toApplicationId, userId);
			List<FinancialArrangementsDetailResponse> financialArrangementsDetailResponseList = new ArrayList<>();

			for (FinancialArrangementsDetailRequest financialArrangementsDetailRequest : financialArrangementsDetailRequestList) {

				FinancialArrangementsDetailResponse financialArrangementsDetailResponse = new FinancialArrangementsDetailResponse();
				BeanUtils.copyProperties(financialArrangementsDetailRequest, financialArrangementsDetailResponse);
//				financialArrangementsDetailResponse.setRelationshipSince(financialArrangementsDetailRequest.getRelationshipSince());
				financialArrangementsDetailResponse.setOutstandingAmount(financialArrangementsDetailRequest.getOutstandingAmount());
				financialArrangementsDetailResponse.setSecurityDetails(financialArrangementsDetailRequest.getSecurityDetails());
				financialArrangementsDetailResponse.setAmount(financialArrangementsDetailRequest.getAmount());
				//	financialArrangementsDetailResponse.setLenderType(LenderType.getById(financialArrangementsDetailRequest.getLenderType()).getValue());
				financialArrangementsDetailResponse.setLoanDate(financialArrangementsDetailRequest.getLoanDate());
				financialArrangementsDetailResponse.setLoanType(financialArrangementsDetailRequest.getLoanType());
//				financialArrangementsDetailResponse.setFinancialInstitutionName(financialArrangementsDetailRequest.getFinancialInstitutionName());
//				financialArrangementsDetailResponse.setAddress(financialArrangementsDetailRequest.getAddress());
//				if (financialArrangementsDetailRequest.getFacilityNatureId() != null)
//					financialArrangementsDetailResponse.setFacilityNature(NatureFacility.getById(financialArrangementsDetailRequest.getFacilityNatureId()).getValue());
				financialArrangementsDetailResponseList.add(financialArrangementsDetailResponse);
			}
			unsecuredLoanPrimaryViewResponse
					.setFinancialArrangementsDetailResponseList(financialArrangementsDetailResponseList);

		} catch (Exception e) {
			logger.error("Problem to get Data of Financial Arrangements Details {}", e);
		}

		// get Finance Means Details and set in response
/*		try {
			List<FinanceMeansDetailRequest> financeMeansDetailRequestsList = financeMeansDetailsService
					.getMeansOfFinanceList(toApplicationId, userId);
			List<FinanceMeansDetailResponse> financeMeansDetailResponsesList = new ArrayList<FinanceMeansDetailResponse>();
			for (FinanceMeansDetailRequest financeMeansDetailRequest : financeMeansDetailRequestsList) {
				FinanceMeansDetailResponse detailResponse = new FinanceMeansDetailResponse();
				BeanUtils.copyProperties(financeMeansDetailRequest, detailResponse);

				if (financeMeansDetailRequest.getFinanceMeansCategoryId() != null)
					detailResponse.setFinanceMeansCategory(FinanceCategory
							.getById(Integer.parseInt(financeMeansDetailRequest.getFinanceMeansCategoryId().toString()))
							.getValue());
				financeMeansDetailResponsesList.add(detailResponse);
			}
			unsecuredLoanPrimaryViewResponse.setFinanceMeansDetailResponseList(financeMeansDetailResponsesList);
		} catch (Exception e1) {
			logger.error("Problem to get Data of Finance Means Details {}", e1);
		}

		// get Total cost of project and set in response
		try {
			List<TotalCostOfProjectRequest> costOfProjectsList = costOfProjectService
					.getCostOfProjectDetailList(toApplicationId, userId);
			List<TotalCostOfProjectResponse> costOfProjectResponses = new ArrayList<TotalCostOfProjectResponse>();
			for (TotalCostOfProjectRequest costOfProjectRequest : costOfProjectsList) {
				TotalCostOfProjectResponse costOfProjectResponse = new TotalCostOfProjectResponse();
				BeanUtils.copyProperties(costOfProjectRequest, costOfProjectResponse);
				if (costOfProjectRequest.getParticularsId() != null)
					costOfProjectResponse.setParticulars(Particular
							.getById(Integer.parseInt(costOfProjectRequest.getParticularsId().toString())).getValue());
				costOfProjectResponses.add(costOfProjectResponse);
			}
			unsecuredLoanPrimaryViewResponse.setTotalCostOfProjectResponseList(costOfProjectResponses);
		} catch (Exception e1) {
			logger.error("Problem to get Data of Total cost of project{}", e1);
		}

		//references
         List<ReferenceRetailDetailsRequest> referenceRetailDetailsRequestList = null;
		try {
			referenceRetailDetailsRequestList = referenceRetailDetailsService.getReferenceRetailDetailList(toApplicationId, userType);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		unsecuredLoanPrimaryViewResponse.setReferenceRetailDetailsRequests(referenceRetailDetailsRequestList); */

		// get list of Brochure
		DocumentRequest documentRequest = new DocumentRequest();
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.UNSECURED_LOAN_BROCHURE_OF_PROPOSED_ACTIVITIES);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			unsecuredLoanPrimaryViewResponse.setBrochureList(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// get list fo certificate
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.UNSECURED_LOAN_CERTIFICATE_OF_INCORPORATION);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			unsecuredLoanPrimaryViewResponse.setCertificateList(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// get list of pan card
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.UNSECURED_LOAN_COPY_OF_PAN_CARD);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			unsecuredLoanPrimaryViewResponse.setPanCardList(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// get profile pic
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.UNSECURED_LOAN_PROFIEL_PICTURE);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			unsecuredLoanPrimaryViewResponse.setProfilePic(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// set short term rating option
/*		try {
			List<String> shortTermValueList = new ArrayList<String>();
			List<Integer> shortTermIdList = creditRatingOrganizationDetailsService
					.getShortTermCreditRatingForTeaser(toApplicationId, userId);
			for (Integer shortTermId : shortTermIdList) {
				OneFormResponse oneFormResponse = oneFormClient
						.getRatingById(CommonUtils.isObjectNullOrEmpty(shortTermId) ? null : shortTermId.longValue());
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) oneFormResponse.getData(), MasterResponse.class);
				if (masterResponse != null) {
					shortTermValueList.add(masterResponse.getValue());
				} else {
					shortTermValueList.add(CommonUtils.NOT_APPLICABLE);
				}
				unsecuredLoanPrimaryViewResponse.setShortTermRating(shortTermValueList);
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// set long term rating option

		try {
			List<String> longTermValueList = new ArrayList<String>();
			List<Integer> longTermIdList = creditRatingOrganizationDetailsService
					.getLongTermCreditRatingForTeaser(toApplicationId, userId);
			for (Integer shortTermId : longTermIdList) {
				OneFormResponse oneFormResponse = oneFormClient
						.getRatingById(CommonUtils.isObjectNullOrEmpty(shortTermId) ? null : shortTermId.longValue());
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) oneFormResponse.getData(), MasterResponse.class);
				if (masterResponse != null) {
					longTermValueList.add(masterResponse.getValue());
				} else {
					longTermValueList.add(CommonUtils.NOT_APPLICABLE);
				}
			}
			unsecuredLoanPrimaryViewResponse.setLongTermRating(longTermValueList);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		//setting co-application details
        List<CorporateCoApplicantRequest> coApplicantResponse = null;
        try {
            coApplicantResponse = corporateCoApplicantService.getList(toApplicationId, userId);
        } catch (Exception e) {
            logger.error(CommonUtils.EXCEPTION,e);
        }
        unsecuredLoanPrimaryViewResponse.setCoApplicantList(coApplicantResponse);

		//Set Office Address
        AddressResponse officeAddress = new AddressResponse();
        try {
            List<Long> officeCity = new ArrayList<Long>(1);
            officeCity.add(corporateApplicantDetail.getAdministrativeCityId());
            OneFormResponse formResponse = oneFormClient.getCityByCityListId(officeCity);

            MasterResponse data = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponse.getListData().get(0), MasterResponse.class);
            officeAddress.setCity(data.getValue());
        } catch (Exception e) {
            logger.error(CommonUtils.EXCEPTION,e);
        }
        try {
            List<Long> officeCountry = new ArrayList<Long>(1);
            Long officeCountryLong = null;
            if (corporateApplicantDetail.getAdministrativeCountryId() != null) {
                officeCountryLong = Long.valueOf(corporateApplicantDetail.getAdministrativeCountryId().toString());

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
            if (corporateApplicantDetail.getAdministrativeStateId() != null) {
                officeStateLong = Long.valueOf(corporateApplicantDetail.getAdministrativeStateId().toString());

                officeState.add(officeStateLong);
                OneFormResponse state = oneFormClient.getStateByStateListId(officeState);
                MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) state.getListData().get(0), MasterResponse.class);
                officeAddress.setState(dataState.getValue());
            }
        } catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
        }
        officeAddress.setLandMark(corporateApplicantDetail.getAdministrativeLandMark());
        officeAddress.setPincode(corporateApplicantDetail.getAdministrativePincode() != null ? corporateApplicantDetail.getAdministrativePincode().toString() : null);
        officeAddress.setPremiseNumber(corporateApplicantDetail.getAdministrativePremiseNumber());
        officeAddress.setStreetName(corporateApplicantDetail.getAdministrativeStreetName());
        unsecuredLoanPrimaryViewResponse.setOfficeAddress(officeAddress); */

		//Set Permanent Address
		AddressResponse permanentAddress = new AddressResponse();
		cityId = null;
		stateId = null;
		countryId = null;
		if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeCityId()))
			cityId = corporateApplicantDetail.getAdministrativeCityId();
		if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeStateId()))
			stateId = corporateApplicantDetail.getAdministrativeStateId();
		if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeCountryId()))
			countryId = corporateApplicantDetail.getAdministrativeCountryId();
		
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
		
		permanentAddress.setLandMark(corporateApplicantDetail.getRegisteredLandMark());
		permanentAddress.setPincode(corporateApplicantDetail.getRegisteredPincode() != null ? corporateApplicantDetail.getRegisteredPincode().toString() : null);
		permanentAddress.setPremiseNumber(corporateApplicantDetail.getRegisteredPremiseNumber());
		permanentAddress.setStreetName(corporateApplicantDetail.getRegisteredStreetName());
		unsecuredLoanPrimaryViewResponse.setPermanentAddress(permanentAddress);
		
		/**AddressResponse permanentAddress = new AddressResponse();
		try {
			List<Long> permanentCity = new ArrayList<Long>(1);
			permanentCity.add(corporateApplicantDetail.getRegisteredCityId());
			OneFormResponse formResponse = oneFormClient.getCityByCityListId(permanentCity);

			MasterResponse data = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) formResponse.getListData().get(0), MasterResponse.class);
			permanentAddress.setCity(data.getValue());
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			List<Long> permanentCountry = new ArrayList<Long>(1);
			Long officeCountryLong = null;
			if (corporateApplicantDetail.getRegisteredCountryId() != null) {
				officeCountryLong = Long.valueOf(corporateApplicantDetail.getAdministrativeCountryId().toString());

				permanentCountry.add(officeCountryLong);
				OneFormResponse country = oneFormClient.getCountryByCountryListId(permanentCountry);
				MasterResponse dataCountry = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) country.getListData().get(0), MasterResponse.class);
				permanentAddress.setCountry(dataCountry.getValue());
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			List<Long> permanentState = new ArrayList<Long>(1);
			Long officeStateLong = null;
			if (corporateApplicantDetail.getRegisteredStateId() != null) {
				officeStateLong = Long.valueOf(corporateApplicantDetail.getAdministrativeStateId().toString());

				permanentState.add(officeStateLong);
				OneFormResponse state = oneFormClient.getStateByStateListId(permanentState);
				MasterResponse dataState = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) state.getListData().get(0), MasterResponse.class);
				permanentAddress.setState(dataState.getValue());
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}*/
		return unsecuredLoanPrimaryViewResponse;
	}
}
