package com.opl.service.loans.service.teaser.primaryview.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.dms.exception.DocumentException;
import com.opl.mudra.api.dms.model.DocumentRequest;
import com.opl.mudra.api.dms.model.DocumentResponse;
import com.opl.mudra.api.dms.utils.DocumentAlias;
import com.opl.mudra.api.loans.model.DirectorBackgroundDetailRequest;
import com.opl.mudra.api.loans.model.DirectorBackgroundDetailResponse;
import com.opl.mudra.api.loans.model.FinancialArrangementsDetailRequest;
import com.opl.mudra.api.loans.model.FinancialArrangementsDetailResponse;
import com.opl.mudra.api.loans.model.teaser.primaryview.TermLoanPrimaryViewResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.matchengine.model.MatchDisplayResponse;
import com.opl.mudra.api.matchengine.model.MatchRequest;
import com.opl.mudra.api.oneform.enums.Constitution;
import com.opl.mudra.api.oneform.enums.Currency;
import com.opl.mudra.api.oneform.enums.Denomination;
import com.opl.mudra.api.oneform.enums.EstablishmentMonths;
import com.opl.mudra.api.oneform.enums.Gender;
import com.opl.mudra.api.oneform.enums.LoanType;
import com.opl.mudra.api.oneform.enums.RelationshipType;
import com.opl.mudra.api.oneform.enums.Title;
import com.opl.mudra.api.oneform.model.IndustrySectorSubSectorTeaserRequest;
import com.opl.mudra.api.oneform.model.MasterResponse;
import com.opl.mudra.api.oneform.model.OneFormResponse;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.dms.DMSClient;
import com.opl.mudra.client.matchengine.MatchEngineClient;
import com.opl.mudra.client.oneform.OneFormClient;
import com.opl.mudra.client.users.UsersClient;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.opl.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.opl.service.loans.domain.fundseeker.corporate.PrimaryTermLoanDetail;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryTermLoanDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.SubSectorRepository;
import com.opl.service.loans.service.common.CommonService;
import com.opl.service.loans.service.fundseeker.corporate.AchievmentDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.CreditRatingOrganizationDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.DirectorBackgroundDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.ExistingProductDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FinanceMeansDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FutureFinancialEstimatesDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.OwnershipDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.PromotorBackgroundDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.ProposedProductDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.SecurityCorporateDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.TotalCostOfProjectService;
import com.opl.service.loans.service.teaser.primaryview.TermLoanPrimaryViewService;

@Service
@Transactional
public class TermLoanPrimaryViewServiceImpl implements TermLoanPrimaryViewService {

	private static final Logger logger = LoggerFactory.getLogger(TermLoanPrimaryViewServiceImpl.class);
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;

	@Autowired
	private PrimaryTermLoanDetailRepository primaryTermLoanLoanDetailRepository;

	@Autowired
	private ProposedProductDetailsService proposedProductDetailsService;

	@Autowired
	private AchievmentDetailsService achievmentDetailsService;

	@Autowired
	private CreditRatingOrganizationDetailsService creditRatingOrganizationDetailsService;

	@Autowired
	private OwnershipDetailsService ownershipDetailsService;

	@Autowired
	private PromotorBackgroundDetailsService promotorBackgroundDetailsService;

	@Autowired
	private FutureFinancialEstimatesDetailsService futureFinancialEstimatesDetailsService;

	@Autowired
	private ExistingProductDetailsService existingProductDetailsService;

	@Autowired
	private SecurityCorporateDetailsService securityCorporateDetailsService;

	@Autowired
	private FinancialArrangementDetailsService financialArrangementDetailsService;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private IndustrySectorRepository industrySectorRepository;

	@Autowired
	private SubSectorRepository subSectorRepository;

	@Autowired
	private TotalCostOfProjectService costOfProjectService;

	@Autowired
	private FinanceMeansDetailsService financeMeansDetailsService;

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
	private PrimaryCorporateDetailRepository primaryCorporateRepository;

	@Autowired
	private CommonService commonService;
	
	@Override
	public TermLoanPrimaryViewResponse getTermLoanPrimaryViewDetails(Long toApplicationId, Integer userType,
																	 Long fundProviderUserId) {
		TermLoanPrimaryViewResponse termLoanPrimaryViewResponse = new TermLoanPrimaryViewResponse();

		if (userType != null && CommonUtils.UserType.FUND_SEEKER != userType) {
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
					termLoanPrimaryViewResponse.setMatchesList(matchResponse.getMatchDisplayObjectList());
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
				BeanUtils.copyProperties(corporateApplicantDetail, termLoanPrimaryViewResponse);
			if (corporateApplicantDetail.getConstitutionId() != null)
				termLoanPrimaryViewResponse
						.setConstitution(Constitution.getById(corporateApplicantDetail.getConstitutionId()).getValue());
			if (corporateApplicantDetail.getEstablishmentMonth() != null)
				termLoanPrimaryViewResponse.setEstablishmentMonth(
						EstablishmentMonths.getById(corporateApplicantDetail.getEstablishmentMonth()).getValue());


			//Set Registered Data
			Long cityId = null ;
			Integer stateId = null;
			Integer countryId = null;
			String cityName = null;
			String stateName = null;
			String countryName = null;
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
					termLoanPrimaryViewResponse.setCity(cityName != null ? cityName : "NA");
					termLoanPrimaryViewResponse.setRegOfficeCity(cityName);
					
					//set State
					termLoanPrimaryViewResponse.setState(stateName != null ? stateName : "NA");
					termLoanPrimaryViewResponse.setRegOfficestate(stateName);
					
					//set Country
					termLoanPrimaryViewResponse.setCountry(countryName != null ? countryName : "NA");
					termLoanPrimaryViewResponse.setRegOfficecountry(countryName);
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
						termLoanPrimaryViewResponse.setCity(masterResponse.getValue());
						termLoanPrimaryViewResponse.setRegOfficeCity(masterResponse.getValue());
					} else {
						termLoanPrimaryViewResponse.setCity("NA");
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
						termLoanPrimaryViewResponse.setAddOfficeCity(masterResponse.getValue());

					} else {
						termLoanPrimaryViewResponse.setCity("NA");
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
						termLoanPrimaryViewResponse.setState(masterResponse.getValue());
						termLoanPrimaryViewResponse.setRegOfficestate(masterResponse.getValue());
					} else {
						termLoanPrimaryViewResponse.setState("NA");
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
						termLoanPrimaryViewResponse.setAddOfficestate(masterResponse.getValue());
					} else {
						termLoanPrimaryViewResponse.setState("NA");
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
						termLoanPrimaryViewResponse.setCountry(masterResponse.getValue());
						termLoanPrimaryViewResponse.setRegOfficecountry(masterResponse.getValue());
					} else {
						termLoanPrimaryViewResponse.setCountry("NA");
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
						termLoanPrimaryViewResponse.setAddOfficecountry(masterResponse.getValue());
					} else {
						termLoanPrimaryViewResponse.setCountry("NA");
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
					termLoanPrimaryViewResponse.setKeyVericalFunding(masterResponse.getValue());
				} else {
					termLoanPrimaryViewResponse.setKeyVericalFunding(CommonUtils.NOT_APPLICABLE);
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
				termLoanPrimaryViewResponse.setIndustrySector(oneFormResponse.getListData());
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}
		// get value of Term Loan data
		PrimaryTermLoanDetail primaryTermLoanDetail = primaryTermLoanLoanDetailRepository
				.getByApplicationAndUserId(toApplicationId, userId);
		PrimaryCorporateDetail primaryCorporateDetail=primaryCorporateRepository.getByApplicationAndUserId(userId, toApplicationId);

		// set value to response
		if (primaryTermLoanDetail != null) {
			BeanUtils.copyProperties(primaryTermLoanDetail, termLoanPrimaryViewResponse);
			termLoanPrimaryViewResponse.setTenure(primaryTermLoanDetail.getTenure() != null ? primaryTermLoanDetail.getTenure() / 12 : null);
			//termLoanPrimaryViewResponse.setSharePriceFace(primaryTermLoanDetail.getSharePriceFace());
			//termLoanPrimaryViewResponse.setSharePriceMarket(primaryTermLoanDetail.getSharePriceMarket());
			if (!CommonUtils.isObjectNullOrEmpty(primaryTermLoanDetail.getCurrencyId())&&!CommonUtils.isObjectNullOrEmpty(primaryTermLoanDetail.getDenominationId()))
				termLoanPrimaryViewResponse.setCurrencyDenomination(Currency.getById(primaryTermLoanDetail.getCurrencyId()).getValue() + " in " + Denomination.getById(primaryTermLoanDetail.getDenominationId()).getValue());
			if (primaryTermLoanDetail.getProductId() != null)
				termLoanPrimaryViewResponse
						.setLoanType(LoanType.getById(primaryTermLoanDetail.getProductId()).getValue());

			termLoanPrimaryViewResponse.setGstin(corporateApplicantDetail.getGstIn() != null ? String.valueOf(corporateApplicantDetail.getGstIn()) : null);
			termLoanPrimaryViewResponse.setHaveCollateralSecurity(primaryCorporateDetail.getHaveCollateralSecurity() != null ? String.valueOf(primaryCorporateDetail.getHaveCollateralSecurity()) : null);
			termLoanPrimaryViewResponse.setCollateralSecurityAmount(primaryCorporateDetail.getCollateralSecurityAmount() != null ? String.valueOf(primaryCorporateDetail.getCollateralSecurityAmount()) : null);
			if (primaryTermLoanDetail.getModifiedDate() != null)
				termLoanPrimaryViewResponse
						.setDateOfProposal(simpleDateFormat.format(primaryTermLoanDetail.getModifiedDate()));
			//termLoanPrimaryViewResponse.setIsCreditRatingAvailable(primaryTermLoanDetail.getCreditRatingId() != null
			// ? CreditRatingAvailable.getById(primaryTermLoanDetail.getCreditRatingId()).getValue() : null);
		}
		// get value of proposed product and set in response
		try {
			termLoanPrimaryViewResponse.setProposedProductDetailRequestList(
					proposedProductDetailsService.getProposedProductDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Proposed Product {}", e);
		}

		// get value of Existing product and set in response
		try {
			termLoanPrimaryViewResponse.setExistingProductDetailRequestList(
					existingProductDetailsService.getExistingProductDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Existing Product {}", e);
		}

		// get value of achievement details and set in response
//		try {
//			termLoanPrimaryViewResponse.setAchievementDetailList(
//					achievmentDetailsService.getAchievementDetailList(toApplicationId, userId));
//		} catch (Exception e) {
//			logger.error("Problem to get Data of Achievement Details {}", e);
//		}

		/*// get value of Credit Rating and set in response
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
					creditRatingOrganizationDetailResponse.setCreditRatingTerm(CreditRatingTerm.getById(creditRatingOrganizationDetailRequest.getCreditRatingTermId()).getValue());
				if (creditRatingOrganizationDetailRequest.getRatingAgencyId() != null)
					creditRatingOrganizationDetailResponse.setRatingAgency(RatingAgency.getById(creditRatingOrganizationDetailRequest.getRatingAgencyId()).getValue());
				creditRatingOrganizationDetailResponse.setFacilityName(creditRatingOrganizationDetailRequest.getFacilityName());
				creditRatingOrganizationDetailResponseList.add(creditRatingOrganizationDetailResponse);
				creditRatingOrganizationDetailResponse.setEntityName(creditRatingOrganizationDetailRequest.getEntityName());
				if (creditRatingOrganizationDetailRequest.getRatingDate() != null){
					creditRatingOrganizationDetailResponse.setRatingDate(creditRatingOrganizationDetailRequest.getRatingDate());
				}


			}
			termLoanPrimaryViewResponse
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
			termLoanPrimaryViewResponse.setOwnershipDetailResponseList(ownershipDetailResponseList);

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
			termLoanPrimaryViewResponse.setPromotorBackgroundDetailResponseList(promotorBackgroundDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Promotor Background {}", e);
		}*/

		//get value of Director's Background and set in response

		try {
			List<DirectorBackgroundDetailRequest> directorBackgroundDetailRequestList = directorBackgroundDetailsService.getDirectorBackgroundDetailList(toApplicationId, userId);
			List<DirectorBackgroundDetailResponse> directorBackgroundDetailResponseList = new ArrayList<>();
			for (DirectorBackgroundDetailRequest directorBackgroundDetailRequest : directorBackgroundDetailRequestList) {
				DirectorBackgroundDetailResponse directorBackgroundDetailResponse = new DirectorBackgroundDetailResponse();
				directorBackgroundDetailResponse.setAddress(directorBackgroundDetailRequest.getAddress());
				//directorBackgroundDetailResponse.setPanNo(directorBackgroundDetailRequest.getPanNo());
				directorBackgroundDetailResponse.setDirectorsName((directorBackgroundDetailRequest.getSalutationId() != null ? Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue() : null )+ " " + directorBackgroundDetailRequest.getDirectorsName());
				directorBackgroundDetailResponse.setPanNo(directorBackgroundDetailRequest.getPanNo().toUpperCase());
						/*String directorName = "";
						if (directorBackgroundDetailRequest.getSalutationId() != null){
							directorName = Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue();
						}
						directorName += " "+directorBackgroundDetailRequest.getDirectorsName();
						directorBackgroundDetailResponse.setDirectorsName(directorName);*/
				directorBackgroundDetailResponse.setTotalExperience(directorBackgroundDetailRequest.getTotalExperience().toString());
				directorBackgroundDetailResponse.setNetworth(directorBackgroundDetailRequest.getNetworth().toString());
				directorBackgroundDetailResponse.setDesignation(directorBackgroundDetailRequest.getDesignation());
				directorBackgroundDetailResponse.setAppointmentDate(directorBackgroundDetailRequest.getAppointmentDate());
				directorBackgroundDetailResponse.setDin(directorBackgroundDetailRequest.getDin());
				directorBackgroundDetailResponse.setDob(directorBackgroundDetailRequest.getDob());
				directorBackgroundDetailResponse.setMobile(directorBackgroundDetailRequest.getMobile());
				directorBackgroundDetailResponse.setPincode(directorBackgroundDetailRequest.getPincode());
				directorBackgroundDetailResponse.setStateCode(directorBackgroundDetailRequest.getStateCode());
				directorBackgroundDetailResponse.setCity(directorBackgroundDetailRequest.getCity());
				directorBackgroundDetailResponse.setGender((directorBackgroundDetailRequest.getGender() != null ? Gender.getById(directorBackgroundDetailRequest.getGender()).getValue() : " " ));
				directorBackgroundDetailResponse.setRelationshipType((directorBackgroundDetailRequest.getRelationshipType() != null ? RelationshipType.getById(directorBackgroundDetailRequest.getRelationshipType()).getValue() : " " ));

				directorBackgroundDetailResponseList.add(directorBackgroundDetailResponse);
			}
			termLoanPrimaryViewResponse.setDirectorBackgroundDetailResponses(directorBackgroundDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Director's Background {}", e);
		}

		/*// get value of Past Financial and set in response
		try {
			List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequestList = pastFinancialEstimateDetailsRepository.listPastFinancialEstimateDetailsRequestFromAppId(toApplicationId);
			if (pastFinancialEstimatesDetailRequestList.size()>4){
				pastFinancialEstimatesDetailRequestList = pastFinancialEstimatesDetailRequestList.subList((pastFinancialEstimatesDetailRequestList.size()-4),pastFinancialEstimatesDetailRequestList.size());
			}
			termLoanPrimaryViewResponse.setPastFinancialEstimatesDetailRequestList(pastFinancialEstimatesDetailRequestList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Past Financial {}", e);
		}

		// get value of Future Projection and set in response
		try {
			termLoanPrimaryViewResponse
					.setFutureFinancialEstimatesDetailRequestList(futureFinancialEstimatesDetailsService
							.getFutureFinancialEstimateDetailsList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Future Projection {}", e);
		}

		// get value of Security and set in response
		try {
			termLoanPrimaryViewResponse.setSecurityCorporateDetailRequestList(
					securityCorporateDetailsService.getsecurityCorporateDetailsList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Security Details {}", e);
		}*/

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
//				if (financialArrangementsDetailRequest.getLenderType() != null){
//					financialArrangementsDetailResponse.setLenderType(LenderType.getById(financialArrangementsDetailRequest.getLenderType()).getValue());
//				}
				financialArrangementsDetailResponse.setLoanDate(financialArrangementsDetailRequest.getLoanDate());
				if (financialArrangementsDetailRequest.getLoanType() != null){
					financialArrangementsDetailResponse.setLoanType(financialArrangementsDetailRequest.getLoanType());
				}
				financialArrangementsDetailResponse.setFinancialInstitutionName(financialArrangementsDetailRequest.getFinancialInstitutionName());
				//	financialArrangementsDetailResponse.setAddress(financialArrangementsDetailRequest.getAddress());
//				if (financialArrangementsDetailRequest.getFacilityNatureId() != null)
//					financialArrangementsDetailResponse.setFacilityNature(NatureFacility.getById(financialArrangementsDetailRequest.getFacilityNatureId()).getValue());
				financialArrangementsDetailResponseList.add(financialArrangementsDetailResponse);
			}
			termLoanPrimaryViewResponse
					.setFinancialArrangementsDetailResponseList(financialArrangementsDetailResponseList);

		} catch (Exception e) {
			logger.error("Problem to get Data of Financial Arrangements Details {}", e);
		}

		/*// get Finance Means Details and set in response
		try {
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
			termLoanPrimaryViewResponse.setFinanceMeansDetailResponseList(financeMeansDetailResponsesList);
		} catch (Exception e1) {
			logger.error("Problem to get Data of Finance Means Details {}", e1);
		}
		//references
        List<ReferenceRetailDetailsRequest> referenceRetailDetailsRequestList = null;
		try {
			referenceRetailDetailsRequestList = referenceRetailDetailsService.getReferenceRetailDetailList(toApplicationId, userType);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		termLoanPrimaryViewResponse.setReferenceRetailDetailsRequests(referenceRetailDetailsRequestList);

		
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
			termLoanPrimaryViewResponse.setTotalCostOfProjectResponseList(costOfProjectResponses);
		} catch (Exception e1) {
			logger.error("Problem to get Data of Total cost of project{}", e1);
		}
*/
		// get list of Brochure
		DocumentRequest documentRequest = new DocumentRequest();
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.TERM_LOAN_BROCHURE_OF_PROPOSED_ACTIVITIES);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			termLoanPrimaryViewResponse.setBrochureList(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// get list fo certificate
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.TERM_LOAN_CERTIFICATE_OF_INCORPORATION);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			termLoanPrimaryViewResponse.setCertificateList(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// get list of pan card
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.TERM_LOAN_COPY_OF_PAN_CARD);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			termLoanPrimaryViewResponse.setPanCardList(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// get profile pic
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.TERM_LOAN_PROFIEL_PICTURE);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			termLoanPrimaryViewResponse.setProfilePic(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// set short term rating option
		try {
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
				termLoanPrimaryViewResponse.setShortTermRating(shortTermValueList);
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
			termLoanPrimaryViewResponse.setLongTermRating(longTermValueList);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		return termLoanPrimaryViewResponse;
	}

}