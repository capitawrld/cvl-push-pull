package com.opl.service.loans.service.teaser.primaryview.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opl.api.pennydrop.model.CommonResponse;
import com.opl.cvl.enums.cvl.VehicleBuildType;
import com.opl.cvl.enums.cvl.VehicleCarryType;
import com.opl.cvl.enums.cvl.VehicleModelType;
import com.opl.cvl.enums.cvl.VehicleSegment;
import com.opl.cvl.enums.cvl.VehicleType;
import com.opl.mudra.api.loans.model.*;
import com.opl.service.loans.repository.VehicleOperatorDetailRepository;
import com.opl.service.loans.service.vehicledetails.VehicleOperatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.pennydrop.client.PennydropClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opl.mudra.api.analyzer.model.common.AnalyzerResponse;
import com.opl.mudra.api.analyzer.model.common.Data;
import com.opl.mudra.api.analyzer.model.common.ManualBsReportRequest;
import com.opl.mudra.api.analyzer.model.common.ReportRequest;
import com.opl.mudra.api.dms.exception.DocumentException;
import com.opl.mudra.api.dms.model.DocumentRequest;
import com.opl.mudra.api.dms.model.DocumentResponse;
import com.opl.mudra.api.dms.utils.DocumentAlias;
import com.opl.mudra.api.eligibility.model.EligibililityRequest;
import com.opl.mudra.api.eligibility.model.EligibilityResponse;
import com.opl.mudra.api.fraudanalytics.model.AnalyticsResponse;
import com.opl.mudra.api.gst.model.GstResponse;
import com.opl.mudra.api.gst.model.MomSales;
import com.opl.mudra.api.gst.model.model.CAMGSTData;
import com.opl.mudra.api.gst.model.util.DateComparator2;
import com.opl.mudra.api.gst.model.yuva.request.GSTR1Request;
import com.opl.mudra.api.itr.model.ITRConnectionResponse;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.corporate.CollateralSecurityDetailRequest;
import com.opl.mudra.api.loans.model.corporate.CorporateFinalInfoRequest;
import com.opl.mudra.api.loans.model.corporate.MachineDetailMudraLoanRequestResponse;
import com.opl.mudra.api.loans.model.corporate.PrimaryCorporateDetailMudraLoanReqRes;
import com.opl.mudra.api.loans.model.retail.BankRelationshipRequest;
import com.opl.mudra.api.loans.model.teaser.primaryview.CorporatePrimaryViewResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.matchengine.exception.MatchException;
import com.opl.mudra.api.matchengine.model.MatchDisplayResponse;
import com.opl.mudra.api.matchengine.model.MatchRequest;
import com.opl.mudra.api.matchengine.model.ProposalMappingRequest;
import com.opl.mudra.api.matchengine.model.ProposalMappingRequestString;
import com.opl.mudra.api.matchengine.model.ProposalMappingResponse;
import com.opl.mudra.api.mca.McaResponse;
import com.opl.mudra.api.mca.verifyApi.VerifyAPIRequest;
import com.opl.mudra.api.oneform.enums.AccessToInputs;
import com.opl.mudra.api.oneform.enums.AssessedForITMst;
import com.opl.mudra.api.oneform.enums.AssessmentOptionForFS;
import com.opl.mudra.api.oneform.enums.BusinessProspects;
import com.opl.mudra.api.oneform.enums.CertificationCourseMst;
import com.opl.mudra.api.oneform.enums.CompetitionMst_SBI;
import com.opl.mudra.api.oneform.enums.Constitution;
import com.opl.mudra.api.oneform.enums.Currency;
import com.opl.mudra.api.oneform.enums.Denomination;
import com.opl.mudra.api.oneform.enums.DirectorRelationshipType;
import com.opl.mudra.api.oneform.enums.EducationalStatusMudraMst;
import com.opl.mudra.api.oneform.enums.EstablishmentMonths;
import com.opl.mudra.api.oneform.enums.FSParameterMst;
import com.opl.mudra.api.oneform.enums.FactoryPremiseMst;
import com.opl.mudra.api.oneform.enums.Gender;
import com.opl.mudra.api.oneform.enums.GovSchemesMst;
import com.opl.mudra.api.oneform.enums.HaveLIMst;
import com.opl.mudra.api.oneform.enums.IdProofMst;
import com.opl.mudra.api.oneform.enums.KnowHowMst;
import com.opl.mudra.api.oneform.enums.LCBG_Status_SBI;
import com.opl.mudra.api.oneform.enums.LoanType;
import com.opl.mudra.api.oneform.enums.MaritalStatusMst;
import com.opl.mudra.api.oneform.enums.MrktArrFinishedGoodsList;
import com.opl.mudra.api.oneform.enums.MudraOwningHouseMst;
import com.opl.mudra.api.oneform.enums.NoOfEmployees;
import com.opl.mudra.api.oneform.enums.OngoingMudraLoan;
import com.opl.mudra.api.oneform.enums.PurposeOfLoan;
import com.opl.mudra.api.oneform.enums.RegistrationWithGovernmentAuthoritiesList;
import com.opl.mudra.api.oneform.enums.ResidentStatusMst;
import com.opl.mudra.api.oneform.enums.SpouseDetailMst;
import com.opl.mudra.api.oneform.enums.Title;
import com.opl.mudra.api.oneform.enums.VisuallyImpairedMst;
import com.opl.mudra.api.oneform.enums.WcRenewalType;
import com.opl.mudra.api.oneform.model.MasterResponse;
import com.opl.mudra.api.oneform.model.OneFormResponse;
import com.opl.mudra.api.oneform.model.SectorIndustryModel;
import com.opl.mudra.api.rating.model.FinancialInputRequest;
import com.opl.mudra.api.scoring.exception.ScoringException;
import com.opl.mudra.api.scoring.model.ProposalScoreResponse;
import com.opl.mudra.api.scoring.model.ScoringRequest;
import com.opl.mudra.api.scoring.model.ScoringResponse;
import com.opl.mudra.api.thirdparty.model.CGTMSEDataResponse;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.analyzer.AnalyzerClient;
import com.opl.mudra.client.cibil.CIBILClient;
import com.opl.mudra.client.dms.DMSClient;
import com.opl.mudra.client.eligibility.EligibilityClient;
import com.opl.mudra.client.fraudanalytics.FraudAnalyticsClient;
import com.opl.mudra.client.gst.GstClient;
import com.opl.mudra.client.itr.ITRClient;
import com.opl.mudra.client.matchengine.MatchEngineClient;
import com.opl.mudra.client.matchengine.ProposalDetailsClient;
import com.opl.mudra.client.mca.McaClient;
import com.opl.mudra.client.oneform.OneFormClient;
import com.opl.mudra.client.scoring.ScoringClient;
import com.opl.mudra.client.thirdparty.ThirdPartyClient;
import com.opl.mudra.client.users.UsersClient;
import com.opl.profile.api.model.ProfileVerMapRequest;
import com.opl.profile.client.ProfileClient;
import com.opl.service.loans.domain.fundprovider.TermLoanParameter;
import com.opl.service.loans.domain.fundprovider.WcTlParameter;
import com.opl.service.loans.domain.fundprovider.WorkingCapitalParameter;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.AssociatedConcernDetail;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.opl.service.loans.domain.fundseeker.corporate.MachineDetailMudraLoan;
import com.opl.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.opl.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetailMudraLoan;
import com.opl.service.loans.domain.fundseeker.retail.BankingRelation;
import com.opl.service.loans.repository.common.CommonRepository;
import com.opl.service.loans.repository.common.LoanRepository;
import com.opl.service.loans.repository.fundprovider.FSParameterMappingRepository;
import com.opl.service.loans.repository.fundprovider.ProductMasterRepository;
import com.opl.service.loans.repository.fundprovider.ProposalDetailsRepository;
import com.opl.service.loans.repository.fundprovider.TermLoanParameterRepository;
import com.opl.service.loans.repository.fundprovider.WcTlLoanParameterRepository;
import com.opl.service.loans.repository.fundprovider.WorkingCapitalParameterRepository;
import com.opl.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.opl.service.loans.repository.fundseeker.corporate.AssociatedConcernDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.corporate.MachineDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailMudraLoanRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.opl.service.loans.repository.fundseeker.retail.BankingRelationlRepository;
import com.opl.service.loans.service.common.CommonService;
import com.opl.service.loans.service.common.PincodeDateService;
import com.opl.service.loans.service.fundseeker.corporate.CollateralSecurityDetailService;
import com.opl.service.loans.service.fundseeker.corporate.CorporateFinalInfoService;
import com.opl.service.loans.service.fundseeker.corporate.DirectorBackgroundDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.opl.service.loans.service.irr.IrrService;
import com.opl.service.loans.service.teaser.primaryview.CorporatePrimaryViewService;

@Service
@Transactional
public class CorporatePrimaryViewServiceImpl implements CorporatePrimaryViewService {

	private static final Logger logger = LoggerFactory.getLogger(CorporatePrimaryViewServiceImpl.class);

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private PrimaryCorporateDetailRepository primaryCorporateRepository;

	@Autowired
	private FinancialArrangementDetailsService financialArrangementDetailsService;

	@Autowired
	private DirectorBackgroundDetailsService directorBackgroundDetailsService;
	
	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private MatchEngineClient matchEngineClient;

	@Autowired
	private UsersClient usersClient;

	@Autowired
	private IrrService irrService;

	@Autowired
	private ScoringClient scoringClient;

	@Autowired
	private AnalyzerClient analyzerClient;

	@Autowired
	private EligibilityClient eligibilityClient;

	@Autowired
	private ThirdPartyClient thirdPartyClient;

	@Autowired
	private TermLoanParameterRepository termLoanParameterRepository;

	@Autowired
	private WorkingCapitalParameterRepository workingCapitalRepository;

	@Autowired
	private WcTlLoanParameterRepository wctlrepo;

	@Autowired
	private ITRClient itrClient;

	@Autowired
	private McaClient mcaClient;

	@Autowired
	private GstClient gstClient;

	@Autowired
	private PincodeDateService pincodeDateService;

	@Autowired
	private FraudAnalyticsClient fraudAnalyticsClient;
	
	@Autowired
	private CorporateFinalInfoService corporateFinalInfoService;

	@Autowired
	private ProductMasterRepository productMasterRepository;

	@Autowired
	private ApplicationProposalMappingRepository applicationProposalMappingRepository;
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private CollateralSecurityDetailService collateralSecurityDetailService;
	
	@Autowired
	private CIBILClient cibilClient;
	
	@Autowired
	private LoanApplicationService loanApplicationService;

	@Value("${capitaworld.gstdata.enable}")
	private Boolean gstCompRelFlag;
	
    @Autowired
    ProposalDetailsRepository proposalDetailsRepository;
    
	@Autowired
	private CommonRepository commonRepository;

	@Autowired
	private PrimaryCorporateDetailMudraLoanRepository mudraLoanRepo ;
	
	@Autowired
	FSParameterMappingRepository fsParameterMappingRepository ;
	
	@Autowired
	AssociatedConcernDetailRepository associatedConcernDetailRepository;
	
	@Autowired
	MachineDetailsRepository machineDetailsRepo;
	
	@Autowired
	BankingRelationlRepository bankingRelationlRepository;
	
	@Autowired
	private ProposalDetailsClient proposalDetailsClient;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private ProfileClient profileClient;

	@Autowired
	VehicleOperatorDetailRepository vehicleOperatorDetailRepository;

	@Autowired
	VehicleOperatorService vehicleOperatorService;
	
	@Autowired
	private PennydropClient pennyDropClient;

	DecimalFormat decim = new DecimalFormat("#,###.00");

	@SuppressWarnings("unchecked")
	@Override
	public CorporatePrimaryViewResponse getCorporatePrimaryViewDetails(Long applicationId,Long proposalId, Integer userType, Long fundProviderUserId) {

	CorporatePrimaryViewResponse corporatePrimaryViewResponse = new CorporatePrimaryViewResponse();

		// Vehicle Operator Code starts here

		VehicleOperatorRequest vehicleOperatorRequest = vehicleOperatorService.getByApplicationId(applicationId);

		if (!CommonUtils.isObjectNullOrEmpty(vehicleOperatorRequest)){
			List<Object[]> cityState = commonRepository.getStateAndCityNameById(vehicleOperatorRequest.getState(), vehicleOperatorRequest.getCity());
			if(cityState != null) {
				for (Object[] obj : cityState) {
					vehicleOperatorRequest.setCityName(CommonUtils.convertString(obj[0]));
					vehicleOperatorRequest.setStateName(CommonUtils.convertString(obj[1]));
				}
			}

			if (!CommonUtils.isListNullOrEmpty(vehicleOperatorRequest.getCurrentOperatedVehicleDetails())){
				for (CurrentOperatedVehicleRequest currentOperatedVehicleRequest : vehicleOperatorRequest.getCurrentOperatedVehicleDetails()){
					currentOperatedVehicleRequest.setTypeOfVehicle(!CommonUtils.isObjectNullOrEmpty(currentOperatedVehicleRequest.getVehicleType()) ? VehicleSegment.getById(currentOperatedVehicleRequest.getVehicleType()).getValue() : "-");
				}
			}

			if (!CommonUtils.isListNullOrEmpty(vehicleOperatorRequest.getProposedVehicleDetails())){
				for (ProposedVehicleRequest proposedVehicleRequest : vehicleOperatorRequest.getProposedVehicleDetails()){
					proposedVehicleRequest.setTypeOfVehicleObt(!CommonUtils.isObjectNullOrEmpty(proposedVehicleRequest.getVehicleType()) ? VehicleType.getById(proposedVehicleRequest.getVehicleType()).getValue() : "-");
					proposedVehicleRequest.setVehicleSeg(!CommonUtils.isObjectNullOrEmpty(proposedVehicleRequest.getVehicleSegment()) ? VehicleSegment.getById(proposedVehicleRequest.getVehicleSegment()).getValue() : "-");
					proposedVehicleRequest.setVehicleBuild(!CommonUtils.isObjectNullOrEmpty(proposedVehicleRequest.getVehicleIs()) ? VehicleBuildType.getById(proposedVehicleRequest.getVehicleIs()).getValue() : "-");
					if (!CommonUtils.isObjectNullOrEmpty(proposedVehicleRequest.getManufacturer())) {
						String manufacturer = commonRepository.getAutoManufacturer(proposedVehicleRequest.getManufacturer());
						proposedVehicleRequest.setVehicleMake(manufacturer);
					}
				}
			}

			if(vehicleOperatorRequest.getVehicleCapacity() != null) {
				vehicleOperatorRequest.setVehicleCapacityStr(VehicleCarryType.getById(vehicleOperatorRequest.getVehicleCapacity()).getValue());
			}
			
			corporatePrimaryViewResponse.setVehicleOperatorDetailsResponse(vehicleOperatorRequest);
		}

		// Vehicle Operator Code ends here

	//for NBFC and Code
		Object[] profileVersionDetails = loanRepository.getProfileVersionDetailsByApplicationId(applicationId);
		if(CommonUtils.isObjectNullOrEmpty(profileVersionDetails)) {
			logger.error("Profile not found for applicationId =======>"+applicationId);
			return corporatePrimaryViewResponse;
		}
		Long itrId = profileVersionDetails[0] != null ? Long.valueOf(profileVersionDetails[0].toString()) : null;
		Long gstId = profileVersionDetails[1] != null ? Long.valueOf(profileVersionDetails[1].toString()) : null;
		Long bsId =  profileVersionDetails[2] != null ? Long.valueOf(profileVersionDetails[2].toString()) : null;
		Long profileId =  profileVersionDetails[3] != null ? Long.valueOf(profileVersionDetails[3].toString()) : null;
		corporatePrimaryViewResponse.setProfileId(profileId);
		
		ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.findOne(proposalId); // NEW BASED ON PROPOSAL MAPPING ID
		logger.info("AppId===========>{}",applicationProposalMapping.getApplicationId());
		Long toApplicationId = applicationProposalMapping.getApplicationId(); // new
		Long toUserId = applicationProposalMapping.getUserId(); // new
		corporatePrimaryViewResponse.setCurrencyDenomination(applicationProposalMapping.getCurrencyId() != null ? Currency.getById(applicationProposalMapping.getCurrencyId()).getValue().toString() : "-");
		
		LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.findOne(toApplicationId);
		//Long userId = loanApplicationMaster.getUserId(); // previous

		//corporatePrimaryViewResponse.setProductId(loanApplicationMaster.getProductId()); //  previous
		
		corporatePrimaryViewResponse.setApplicationType(loanApplicationMaster.getWcRenewalStatus() != null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue().toString() : "New" );
		/* ========= Matches Data ========== */
		if (userType != null && CommonUtils.UserType.FUND_SEEKER != userType ) {
			     // teaser
				// view
				// viwed by
				// fund
				// provider
				Long fpProductMappingId = null;
				try {

					UsersRequest usersRequest = new UsersRequest();
					usersRequest.setId(fundProviderUserId);
					UserResponse userResponse = usersClient.getLastAccessApplicant(usersRequest);
					fpProductMappingId = userResponse.getId();

				} catch (Exception e) {
					logger.error("error while fetching last access fp rpduct id for fund provider while fetching matches in teaser view : ",e);
				}
				try {
					MatchRequest matchRequest = new MatchRequest();
					matchRequest.setApplicationId(toApplicationId);
					matchRequest.setProductId(fpProductMappingId);
					MatchDisplayResponse matchResponse = matchEngineClient.displayMatchesOfCorporate(matchRequest);
					corporatePrimaryViewResponse.setMatchesMap(matchResponse.getMatchDisplayObjectMap());
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
		}

		// get details of CorporateApplicantDetail PREVIOUS
	/*	CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
				.getByApplicationAndUserId(userId, toApplicationId);*/ // PREVIOUS

		CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository.getByApplicationAndProposalIdAndUserId(toUserId,toApplicationId,proposalId); //NEW BASED ON PROPOSAL MAPPING ID=======>
		
		//NOTE OF BORROWER FOR MSME
				try {
					String note = commonRepository.getNoteForHLCam(toApplicationId);
					corporatePrimaryViewResponse.setNoteOfBorrower(!CommonUtils.isObjectNullOrEmpty(note) ? note : null);
				}catch (Exception e) {
					logger.error("Error/Exception while getting note of borrower....Error==>{}", e);
				}
		
		// set value to response
		if (corporateApplicantDetail != null) {
			BeanUtils.copyProperties(corporateApplicantDetail, corporatePrimaryViewResponse);
			corporatePrimaryViewResponse.setComercialPanNo(corporateApplicantDetail.getPanNo() != null ? corporateApplicantDetail.getPanNo() : "-");
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getConstitutionId()))
				corporatePrimaryViewResponse
						.setConstitution(Constitution.getById(corporateApplicantDetail.getConstitutionId()).getValue());
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getEstablishmentMonth()))
				corporatePrimaryViewResponse.setEstablishmentMonth(
						EstablishmentMonths.getById(corporateApplicantDetail.getEstablishmentMonth()).getValue());
			// set Establishment year
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getEstablishmentYear())) {
				try {
					OneFormResponse establishmentYearResponse = oneFormClient.getYearByYearId(
							CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getEstablishmentYear()) ? null
									: corporateApplicantDetail.getEstablishmentYear().longValue());
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) establishmentYearResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						corporatePrimaryViewResponse.setEstablishmentYear(masterResponse.getValue());
					} else {
						corporatePrimaryViewResponse.setEstablishmentYear("NA");
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}

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
					corporatePrimaryViewResponse.setCity(cityName != null ? cityName : "NA");
					corporatePrimaryViewResponse.setRegOfficeCity(cityName);
					
					//set State
					corporatePrimaryViewResponse.setState(stateName != null ? stateName : "NA");
					corporatePrimaryViewResponse.setRegOfficestate(stateName);
					
					//set Country
					corporatePrimaryViewResponse.setCountry(countryName != null ? countryName : "NA");
					corporatePrimaryViewResponse.setRegOfficecountry(countryName);
				}
			}
			
			//set Administrative Data
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
					corporatePrimaryViewResponse.setAddOfficeCity(cityName != null ? cityName : "NA");
					
					//set State
					corporatePrimaryViewResponse.setAddOfficestate(stateName != null ? stateName : "NA");
					
					//set Country
					corporatePrimaryViewResponse.setAddOfficecountry(countryName != null ? countryName : "NA");
				}
			}
			
		

			List<Long> keyVerticalFundingId = new ArrayList<>();
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVericalFunding()))
				keyVerticalFundingId.add(corporateApplicantDetail.getKeyVericalFunding());
			if (!CommonUtils.isListNullOrEmpty(keyVerticalFundingId)) {
				try {
					OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						corporatePrimaryViewResponse.setKeyVericalFunding(masterResponse.getValue());
					} else {
						corporatePrimaryViewResponse.setKeyVericalFunding("NA");
					}

				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}

			// key vertical sector
			List<Long> keyVerticalSectorId = new ArrayList<>();
			// getting sector id from mapping
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSector()))
				keyVerticalSectorId.add(corporateApplicantDetail.getKeyVerticalSector());

			try {
				OneFormResponse formResponse = oneFormClient
						.getIndustrySecByMappingId(corporateApplicantDetail.getKeyVerticalSector());
				// SectorIndustryModel oneResponseDataList = (SectorIndustryModel) formResponse
				// .getData();

				SectorIndustryModel sectorIndustryModel = MultipleJSONObjectHelper
						.getObjectFromMap((Map<String,Object>) formResponse.getData(), SectorIndustryModel.class);

				// get key vertical sector value
				OneFormResponse oneFormResponse = oneFormClient
						.getSectorById(Arrays.asList(sectorIndustryModel.getSectorId()));
				List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
						.getListData();
				if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
					MasterResponse masterResponse = MultipleJSONObjectHelper
							.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
					corporatePrimaryViewResponse.setKeyVericalSector(masterResponse.getValue());
				} else {
					corporatePrimaryViewResponse.setKeyVericalSector("NA");
				}
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}

			// key vertical Subsector
			try {
				if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSubsector())) {
					OneFormResponse oneFormResponse = oneFormClient
							.getSubSecNameByMappingId(corporateApplicantDetail.getKeyVerticalSubsector());
					corporatePrimaryViewResponse.setKeyVericalSubsector((String) oneFormResponse.getData());
				}
			} catch (Exception e) {
				logger.error("error while getting key vertical sub-sector : ",e);
			}

		}
		// get value of working capital data
		PrimaryCorporateDetail primaryCorporateDetail = primaryCorporateRepository
				.getByApplicationAndUserId(toApplicationId, toUserId);

		if (primaryCorporateDetail != null) {
			// set value to response
			BeanUtils.copyProperties(primaryCorporateDetail, corporatePrimaryViewResponse);
			if (!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getCurrencyId())
					&& !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getDenominationId())) {
				corporatePrimaryViewResponse
						.setCurrencyDenomination(Currency.getById(primaryCorporateDetail.getCurrencyId()).getValue()
								+ " in " + Denomination.getById(primaryCorporateDetail.getDenominationId()).getValue());
			}
			/*corporatePrimaryViewResponse.setLoanType(primaryCorporateDetail.getProductId() != null
					? LoanType.getById(primaryCorporateDetail.getProductId()).getValue()
					: null);*/ // PREVIOUS
			corporatePrimaryViewResponse.setProductId(applicationProposalMapping.getProductId()); // new
			corporatePrimaryViewResponse.setLoanType(applicationProposalMapping.getProductId() != null
					? LoanType.getById(applicationProposalMapping.getProductId()).getValue()
					: null); // NEW BASED ON PROPOSAL MAPPING DATABASE

			/*corporatePrimaryViewResponse.setLoanAmount(
					primaryCorporateDetail.getAmount() != null ? String.valueOf(primaryCorporateDetail.getAmount())
							: null);*/ //PREVIOUS

			
			// corporatePrimaryViewResponse.setLoanAmount(applicationProposalMapping.getLoanAmount() != null ? String.valueOf(applicationProposalMapping.getLoanAmount()) : null); // NEW PROPOSAL MAPPING ID BASED
			
			// Set loan amount based on application (New or Renewal) 
			Double loanAmount = WcRenewalType.RENEWAL.getId().equals(loanApplicationMaster.getWcRenewalStatus()) ? primaryCorporateDetail.getLoanAmount() : primaryCorporateDetail.getAdditionalLoanAmount();  
			corporatePrimaryViewResponse.setLoanAmount(String.valueOf(loanAmount));

			corporatePrimaryViewResponse.setGstIn(
					corporateApplicantDetail.getGstIn() != null ? String.valueOf(corporateApplicantDetail.getGstIn())
							: null);
			
			if(primaryCorporateDetail.getAssessmentId()!=null) {
				corporatePrimaryViewResponse.setPurposeOfLoan(primaryCorporateDetail.getPurposeOfLoanId() != null && primaryCorporateDetail.getPurposeOfLoanId()==1 ? AssessmentOptionForFS.getById(primaryCorporateDetail.getAssessmentId()).getValue().toString() : PurposeOfLoan.getById(primaryCorporateDetail.getPurposeOfLoanId()).getValue().toString());	
			}else {	
				logger.warn("assesment id is null so considered PurposeOfLoan enum.....");
				corporatePrimaryViewResponse.setPurposeOfLoan(
						CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getPurposeOfLoanId()) ? null
								: PurposeOfLoan.getById(primaryCorporateDetail.getPurposeOfLoanId()).getValue().toString());
			}
			
			corporatePrimaryViewResponse
					.setHaveCollateralSecurity(primaryCorporateDetail.getHaveCollateralSecurity() != null
							? String.valueOf(primaryCorporateDetail.getHaveCollateralSecurity())
							: null);
			corporatePrimaryViewResponse
					.setCollateralSecurityAmount(primaryCorporateDetail.getCollateralSecurityAmount() != null
							? String.valueOf(primaryCorporateDetail.getCollateralSecurityAmount())
							: null);
			corporatePrimaryViewResponse.setProductServiceDesc(primaryCorporateDetail.getProductServiceDescription());
			
			
			List<CollateralSecurityDetailRequest> collateralSecurityDetails = collateralSecurityDetailService.getData(applicationId);
			corporatePrimaryViewResponse.setCollateralSecurityDetails(collateralSecurityDetails);
			corporatePrimaryViewResponse.setPromotersContribution(primaryCorporateDetail.getPromoterContribution());
			
			// add additional Details 
			
			corporatePrimaryViewResponse.setCostOfMachinery(primaryCorporateDetail.getCostOfMachinery());
			corporatePrimaryViewResponse.setIncrementalTurnover(primaryCorporateDetail.getIncrementalTurnover());
			corporatePrimaryViewResponse.setIncrementalMargin(primaryCorporateDetail.getIncrementalMargin());
			
			corporatePrimaryViewResponse
					.setPromotersContributionPer(primaryCorporateDetail.getTotalAmtPercentage() != null
							? " (" + convertValue(primaryCorporateDetail.getTotalAmtPercentage()) + "%)"
							: null);

			corporatePrimaryViewResponse.setNpOrgId(loanApplicationMaster.getNpOrgId());
			// workingCapitalPrimaryViewResponse.setSharePriceFace(primaryWorkingCapitalLoanDetail.getSharePriceFace());
			// workingCapitalPrimaryViewResponse.setSharePriceMarket(primaryWorkingCapitalLoanDetail.getSharePriceMarket());
			if (!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getModifiedDate()))
				corporatePrimaryViewResponse.setDateOfProposal(primaryCorporateDetail.getModifiedDate() != null
						? CommonUtils.DATE_FORMAT.format(primaryCorporateDetail.getModifiedDate())
						: null);
			
			// other Details			
			corporatePrimaryViewResponse.setComercialOpDate(primaryCorporateDetail.getCommercialOperationDate());
			corporatePrimaryViewResponse.setFactoryPremise(primaryCorporateDetail.getFactoryPremise() != null ? FactoryPremiseMst.getById(primaryCorporateDetail.getFactoryPremise()).getValue().toString() : "-");
			corporatePrimaryViewResponse.setKnoHow(primaryCorporateDetail.getKnowHow() != null ? KnowHowMst.getById(primaryCorporateDetail.getKnowHow()).getValue().toString() : "-");
			corporatePrimaryViewResponse.setCompetition(primaryCorporateDetail.getCompetition()  != null ? CompetitionMst_SBI.getById(primaryCorporateDetail.getCompetition()).getValue().toString() : "-");
			
			// MUDRA LOAN DETAILS
			PrimaryCorporateDetailMudraLoan mlDetail = 	mudraLoanRepo.findFirstByApplicationIdAndApplicationProposalMappingProposalIdOrderByIdDesc(applicationId, proposalId); 
			if (!CommonUtils.isObjectNullOrEmpty(mlDetail)) {			
				PrimaryCorporateDetailMudraLoanReqRes mlDetailsRes = new PrimaryCorporateDetailMudraLoanReqRes();
				BeanUtils.copyProperties(mlDetail, mlDetailsRes);
				if (!CommonUtils.isObjectNullOrEmpty(mlDetail.getMrktArragementFinishedGoods())) {					
					mlDetailsRes.setMrktArragementFinishedGoodsValue(MrktArrFinishedGoodsList.fromId(mlDetail.getMrktArragementFinishedGoods()).getValue());
				}
				
				// GET MACHINE DETAILS
				List<MachineDetailMudraLoan> machineDetails = machineDetailsRepo.findByApplicationIdAndIsActive(toApplicationId, true);
				if (!CommonUtils.isListNullOrEmpty(machineDetails)) {
					List<MachineDetailMudraLoanRequestResponse> machineDetailsRes =  new ArrayList<>(machineDetails.size());
					for (MachineDetailMudraLoan machineDetailMudraLoan : machineDetails) {
						MachineDetailMudraLoanRequestResponse machineDetail = new MachineDetailMudraLoanRequestResponse(); 
						BeanUtils.copyProperties(machineDetailMudraLoan, machineDetail);
						machineDetailsRes.add(machineDetail);
					}				
					mlDetailsRes.setMachineDetails(machineDetailsRes);
				}
				corporatePrimaryViewResponse.setMlDetail(mlDetailsRes);
			}
			
			//No of Employees/Workers
			corporatePrimaryViewResponse.setEmployeeGeneration(corporateApplicantDetail.getEmploymentGeneration() != null ?  (NoOfEmployees.getById(corporateApplicantDetail.getEmploymentGeneration()) != null ?  NoOfEmployees.getById(corporateApplicantDetail.getEmploymentGeneration()).getValue(): "-") : "-"  );
			corporatePrimaryViewResponse.setBusinessProspects(corporateApplicantDetail.getBusinessProspects() != null ? (BusinessProspects.getById(corporateApplicantDetail.getBusinessProspects()) != null ? BusinessProspects.getById(corporateApplicantDetail.getBusinessProspects()).getValue() : "-") : "-");
			corporatePrimaryViewResponse.setAccessInput(corporateApplicantDetail.getAccessInput() != null ? (AccessToInputs.getById(corporateApplicantDetail.getAccessInput()) != null ? AccessToInputs.getById(corporateApplicantDetail.getAccessInput()).getValue() : "-") : "-");

			
			// REGISTER WITH GOV AUTHORITIES
			List<Integer> govAuthorities = fsParameterMappingRepository.getParametersByApplicationIdAndType(applicationId, FSParameterMst.GOV_AUTHORITIES.getId());
			if (!CommonUtils.isListNullOrEmpty(govAuthorities)) {
				String govAuthValue  = ""; 
				for (int i = 0; i < govAuthorities.size(); i++) {
					String authority = 	RegistrationWithGovernmentAuthoritiesList.fromId(govAuthorities.get(i)).getValue();
					govAuthValue = govAuthValue + ((i != 0) ? ", " : "" )+ authority;
				}
				corporatePrimaryViewResponse.setRegiterWithGovAuthorities(govAuthValue);
			}
			
			// GET ASSOCIATE CONCERN DETAILS
			List<AssociatedConcernDetailRequest> associatedConcernResList = new ArrayList<>(); 
			List<AssociatedConcernDetail> associatedConcernDetailList =  associatedConcernDetailRepository.listAssociatedConcernFromAppId(applicationId);
			
			if (!CommonUtils.isListNullOrEmpty(associatedConcernDetailList)) {
				for (AssociatedConcernDetail associatedConcern : associatedConcernDetailList) {
					AssociatedConcernDetailRequest assoConcernDetailRes = new AssociatedConcernDetailRequest(); 
					BeanUtils.copyProperties(associatedConcern, assoConcernDetailRes);
					// SET ADDRESS
					setAssociateAddress(assoConcernDetailRes);
					associatedConcernResList.add(assoConcernDetailRes);
				}
			}
			corporatePrimaryViewResponse.setAssociatedConcernDetail(associatedConcernResList);
			
			// GET BANKING RELATIONSHIP DETAILS
			LocalDate today = LocalDate.now();
            List<BankRelationshipRequest> bankRelationshipRequests = new ArrayList<>();
            List<BankingRelation> bankingRelations = bankingRelationlRepository.listBankRelationAppId(applicationId);
            for(BankingRelation bankingRelation : bankingRelations) {
            	BankRelationshipRequest bankRelationshipRequest	= new BankRelationshipRequest();
            	BeanUtils.copyProperties(bankingRelation, bankRelationshipRequest);
            	if (bankingRelation.getSinceYear() != null && bankingRelation.getSinceMonth() != null) {
					LocalDate since = LocalDate.of(bankingRelation.getSinceYear(), bankingRelation.getSinceMonth(),1);
					Period age = Period.between(since, today);
					bankRelationshipRequest.setSinceYear(age.getYears());
					bankRelationshipRequest.setSinceMonth(age.getMonths());
					bankRelationshipRequest.setSinceWhen((bankRelationshipRequest.getSinceYear() != null ? bankRelationshipRequest.getSinceYear() +" year" : "") + " " +(bankRelationshipRequest.getSinceMonth() != null ? bankRelationshipRequest.getSinceMonth()+" months" :  "" ));
				}
            	bankRelationshipRequests.add(bankRelationshipRequest);
            }
            corporatePrimaryViewResponse.setBankingRelationshipList(bankRelationshipRequests);
		}

		try {
			List<DirectorBackgroundDetailRequest> directorBackgroundDetailRequestList = directorBackgroundDetailsService
					.getDirectorBackgroundDetailList(toApplicationId, toUserId);
			List<DirectorBackgroundDetailResponse> directorBackgroundDetailResponseList = new ArrayList<>();
			for (DirectorBackgroundDetailRequest directorBackgroundDetailRequest : directorBackgroundDetailRequestList) {
				DirectorBackgroundDetailResponse directorBackgroundDetailResponse = new DirectorBackgroundDetailResponse();
				// directorBackgroundDetailResponse.setAchivements(directorBackgroundDetailRequest.getAchivements());
				directorBackgroundDetailResponse.setAddress(directorBackgroundDetailRequest.getAddress());
				directorBackgroundDetailResponse.setPremiseNumber(directorBackgroundDetailRequest.getPremiseNumber());
				directorBackgroundDetailResponse.setStreetName(directorBackgroundDetailRequest.getStreetName());
				directorBackgroundDetailResponse.setLandmark(directorBackgroundDetailRequest.getLandmark());
				// directorBackgroundDetailResponse.setAge(directorBackgroundDetailRequest.getAge());
				// directorBackgroundDetailResponse.setPanNo(directorBackgroundDetailRequest.getPanNo());
				directorBackgroundDetailResponse
						.setDirectorsName((directorBackgroundDetailRequest.getSalutationId() != null
								? Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue()
								: null) + " " + directorBackgroundDetailRequest.getDirectorsName());
				directorBackgroundDetailResponse.setPanNo(directorBackgroundDetailRequest.getPanNo().toUpperCase());
				String directorName = "";
				if (directorBackgroundDetailRequest.getSalutationId() != null) {
					directorName = Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue();
				}
				directorName += " " + directorBackgroundDetailRequest.getDirectorsName();
				directorBackgroundDetailResponse.setDirectorsName(directorName);
				// directorBackgroundDetailResponse.setQualification(directorBackgroundDetailRequest.getQualification());
				directorBackgroundDetailResponse
						.setTotalExperience(directorBackgroundDetailRequest.getTotalExperience().toString() + " Years");
				directorBackgroundDetailResponse.setNetworth(directorBackgroundDetailRequest.getNetworth().toString());
				directorBackgroundDetailResponse.setDesignation(directorBackgroundDetailRequest.getDesignation());
				directorBackgroundDetailResponse
						.setAppointmentDate(directorBackgroundDetailRequest.getAppointmentDate());
				directorBackgroundDetailResponse.setDin(directorBackgroundDetailRequest.getDin());
				directorBackgroundDetailResponse.setMobile(directorBackgroundDetailRequest.getMobile());
				directorBackgroundDetailResponse.setDobN(directorBackgroundDetailRequest.getDob().toString());
				directorBackgroundDetailResponse.setPincode(directorBackgroundDetailRequest.getPincode());
				directorBackgroundDetailResponse.setPersonalId(directorBackgroundDetailRequest.getPersonalId());
                                
                                //Loan Obligation Added
                                Double loanObligation = financialArrangementDetailsService.getTotalOfEmiByApplicationIdAndDirectorId(applicationId,directorBackgroundDetailRequest.getId());
				directorBackgroundDetailResponse.setLoanObligation(!CommonUtils.isObjectNullOrEmpty(loanObligation) ? loanObligation : 0);
				try {
					if (!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDistrictMappingId())) {
						directorBackgroundDetailResponse.setPinData(
								pincodeDateService.getById(directorBackgroundDetailRequest.getDistrictMappingId()));
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}

				directorBackgroundDetailResponse.setStateCode(directorBackgroundDetailRequest.getStateCode());
				String cityName = commonRepository.getCityByCityId(Long.parseLong(directorBackgroundDetailRequest.getCityId().toString()));
				directorBackgroundDetailResponse.setCity(cityName);
				directorBackgroundDetailResponse.setGender((directorBackgroundDetailRequest.getGender() != null
						? Gender.getById(directorBackgroundDetailRequest.getGender()).getValue()
						: " "));
				directorBackgroundDetailResponse
						.setRelationshipType((directorBackgroundDetailRequest.getRelationshipType() != null
								? DirectorRelationshipType
										.getById(directorBackgroundDetailRequest.getRelationshipType()).getValue()
								: " "));
				
				// additional Details added as per req
				directorBackgroundDetailResponse.setPhysicallyHandicapped(directorBackgroundDetailRequest.getPhysicallyHandicapped() != null ? VisuallyImpairedMst.getById(directorBackgroundDetailRequest.getPhysicallyHandicapped()).getValue().toString() : "-");
				directorBackgroundDetailResponse.setIsMainDirector(directorBackgroundDetailRequest.getIsMainDirector());
				directorBackgroundDetailResponse.setFatherName(directorBackgroundDetailRequest.getFatherName());
				directorBackgroundDetailResponse.setEducationalStatus(directorBackgroundDetailRequest.getEducationalStatus() != null ? EducationalStatusMudraMst.getById(directorBackgroundDetailRequest.getEducationalStatus()).getValue().toString() : "-");
				directorBackgroundDetailResponse.setVisuallyImpaired(directorBackgroundDetailRequest.getVisuallyImpaired() != null ? VisuallyImpairedMst.getById(directorBackgroundDetailRequest.getVisuallyImpaired()).getValue().toString() : "-");
				directorBackgroundDetailResponse.setResidentStatus(directorBackgroundDetailRequest.getResidentStatus() != null ? ResidentStatusMst.getById(directorBackgroundDetailRequest.getResidentStatus()).getValue().toString() : "-");
				directorBackgroundDetailResponse.setDirectorPersonalInfo(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest() != null ? directorBackgroundDetailRequest.getDirectorPersonalDetailRequest() : " " );
				String stateName = commonRepository.getStateByStateCode(directorBackgroundDetailRequest.getStateId().longValue()); 
				directorBackgroundDetailResponse.setStateCode(stateName);
				
				//nationality
				
				List<Long> countryList = new ArrayList<>();
				if (!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getNationality()))
					countryList.add(Long.valueOf(directorBackgroundDetailRequest.getNationality()));
				if (!CommonUtils.isListNullOrEmpty(countryList)) {
					try {
						OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
						List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
								.getListData();
						if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
							MasterResponse masterResponse = MultipleJSONObjectHelper
									.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
							corporatePrimaryViewResponse.setCountry(masterResponse.getValue());
							corporatePrimaryViewResponse.setRegOfficecountry(masterResponse.getValue());
							
							directorBackgroundDetailResponse.setNationality(masterResponse.getValue());
							
						} else {
							directorBackgroundDetailResponse.setNationality("NA");
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
				
				
				directorBackgroundDetailResponseList.add(directorBackgroundDetailResponse);
				// is main director so fetch personal detail by personal id
				try {
					
					if(directorBackgroundDetailRequest.getIsMainDirector() != null && directorBackgroundDetailRequest.getIsMainDirector() == true){

						DirectorPersonalDetailResponse directorPersonalDetail= new DirectorPersonalDetailResponse();
						
						if(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest() != null) {
							directorPersonalDetail.setMaritalStatus(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getMaritalStatus() != null ? MaritalStatusMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getMaritalStatus()).getValue().toString() : "-");
							directorPersonalDetail.setSpouseName(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getSpouseName());
							directorPersonalDetail.setSpouseDetail(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getSpouseDetail() != null ? SpouseDetailMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getSpouseDetail()).getValue().toString() : "-");
							directorPersonalDetail.setAssessedForIt(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getAssessedForIt() != null ? AssessedForITMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getAssessedForIt()).getValue().toString() : "-");

							if(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOwningHouse() != null && MudraOwningHouseMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOwningHouse()) != null){
								directorPersonalDetail.setOwningHouse(MudraOwningHouseMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOwningHouse()).getValue().toString());
							}


							directorPersonalDetail.setNoOfChildren(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getNoOfChildren());
							directorPersonalDetail.setHaveLiPolicy(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getHaveLiPolicy()!= null ? HaveLIMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getHaveLiPolicy()).getValue().toString() : "-");
							directorPersonalDetail.setIdProof(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIdProof() != null ? IdProofMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIdProof()).getValue() : "-" );
							directorPersonalDetail.setDependent(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getDependent());
							Boolean isSameAddIdProof = directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIsSameAddIdProof() ; 
							directorPersonalDetail.setIsSameAddIdProof(!CommonUtils.isObjectNullOrEmpty(isSameAddIdProof) ? (isSameAddIdProof ? "Yes" : "No") : "No");
							directorPersonalDetail.setAddressYears(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getAddressYears());
							directorPersonalDetail.setOtherIncomeSource(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOtherIncomeSource());
							directorPersonalDetail.setCertificationCourse(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getCertificationCourse() != null ? CertificationCourseMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getCertificationCourse()).getValue() : "-"  );
							directorPersonalDetail.setWorkAndResidenceSamePlace(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIsWorkAndResidenceSamePlace() != null ?(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIsWorkAndResidenceSamePlace().equals(1) ? "Yes" : "No")  : "-"  );
							directorPersonalDetail.setHaveOwnCommercialProperty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getHaveOwnCommercialProperty() != null ?(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getHaveOwnCommercialProperty() ? "Yes" : "No")  : "-"  );
							String ongoingMudraLoan = directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOngoingMudraLoan() != null ? OngoingMudraLoan.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOngoingMudraLoan()).getValue() : "-"  ; 
							directorPersonalDetail.setOngoingMudraLoan(ongoingMudraLoan);
							corporatePrimaryViewResponse.setOngoingMudraLoan(ongoingMudraLoan);
							// COVERED IN GOV_SCHEMES
							List<Integer> govSchemes = fsParameterMappingRepository.getParametersByApplicationIdAndType(applicationId, FSParameterMst.GOV_SCHEMES.getId());
							if (!CommonUtils.isListNullOrEmpty(govSchemes)) {
								String govScheme  = ""; 
								for (int i = 0; i < govSchemes.size(); i++) {
									String authority = GovSchemesMst.getById(govSchemes.get(i)).getValue();
									govScheme = govScheme + ((i != 0) ? ", " : "" )+ authority;
								}
								directorPersonalDetail.setGovScheme(govScheme);
								corporatePrimaryViewResponse.setGovScheme(govScheme);
							}
						
						directorBackgroundDetailResponse.setDirectorPersonalInfo(directorPersonalDetail);
						}
						else {
							logger.warn("directorBackgroundDetailRequest.getDirectorPersonalDetailRequest() is null....");
						}
					}else {
						logger.warn("----:::::: is main director is null ::::::-----For-----AppId==>{}",toApplicationId);
					}
					
				} catch (Exception e) {
					logger.error("----:::::: error while get is main dir details ::::::-----For-----AppId==>{}  Error==>{}",toApplicationId , e);
				}
			}
			
			corporatePrimaryViewResponse.setDirectorBackgroundDetailResponses(directorBackgroundDetailResponseList);
		
		} catch (Exception e) {
			logger.error("Problem to get Data of Director's Background {}", e);
		}

		// get value of Financial Arrangements and set in response
		try {
			List<FinancialArrangementsDetailRequest> financialArrangementsDetailRequestList = financialArrangementDetailsService
					.getFinancialArrangementDetailsList(toApplicationId, toUserId);
			List<FinancialArrangementsDetailResponse> financialArrangementsDetailResponseList = new ArrayList<>();
			for (FinancialArrangementsDetailRequest financialArrangementsDetailRequest : financialArrangementsDetailRequestList) {
				FinancialArrangementsDetailResponse financialArrangementsDetailResponse = new FinancialArrangementsDetailResponse();
				// financialArrangementsDetailResponse.setRelationshipSince(financialArrangementsDetailRequest.getRelationshipSince());
				financialArrangementsDetailResponse
						.setOutstandingAmount(financialArrangementsDetailRequest.getOutstandingAmount());
				financialArrangementsDetailResponse
						.setSecurityDetails(financialArrangementsDetailRequest.getSecurityDetails());
				financialArrangementsDetailResponse.setAmount(financialArrangementsDetailRequest.getAmount());
				// financialArrangementsDetailResponse.setLenderType(LenderType.getById(financialArrangementsDetailRequest.getLenderType()).getValue());
				financialArrangementsDetailResponse.setLoanDate(financialArrangementsDetailRequest.getLoanDate());
				financialArrangementsDetailResponse.setLoanType(financialArrangementsDetailRequest.getLoanType());
				financialArrangementsDetailResponse
						.setFinancialInstitutionName(financialArrangementsDetailRequest.getFinancialInstitutionName());
				financialArrangementsDetailResponse.setLcbgStatus(financialArrangementsDetailRequest.getLcBgStatus() != null ? LCBG_Status_SBI.getById(financialArrangementsDetailRequest.getLcBgStatus()).getValue().toString() : "-");
				// financialArrangementsDetailResponse.setFacilityNature(NatureFacility.getById(financialArrangementsDetailRequest.getFacilityNatureId()).getValue());
				// financialArrangementsDetailResponse.setAddress(financialArrangementsDetailRequest.getAddress());
				//financialArrangementsDetailResponse.setBuerauEmi(financialArrangementsDetailRequest.getBureauOrCalculatedEmi());
				financialArrangementsDetailResponse.setEmi(financialArrangementsDetailRequest.getEmi());
				financialArrangementsDetailResponse.setBuerauOutStanding(financialArrangementsDetailRequest.getBureauOutstandingAmount());
				financialArrangementsDetailResponse.setCollateralAmt(financialArrangementsDetailRequest.getCollateralSecurityAmount());
				
				financialArrangementsDetailResponseList.add(financialArrangementsDetailResponse);
			}
			corporatePrimaryViewResponse
					.setFinancialArrangementsDetailResponseList(financialArrangementsDetailResponseList);

		} catch (Exception e) {
			logger.error("Problem to get Data of Financial Arrangements Details {}", e);
		}

		/*get personal loan obligation od dir*/

		Double loanObligation=financialArrangementDetailsService.getTotalEmiOfAllDirByApplicationId(toApplicationId);
		corporatePrimaryViewResponse.setLoanObligation(loanObligation!= null ? loanObligation : 0);


		Long denomination = Denomination.getById(primaryCorporateDetail.getDenominationId()).getDigit();

		try {
		//	FinancialInputRequest financialInputRequest = irrService.cmaIrrMappingService(userId, toApplicationId, null,enomination); //=======>>>>PREVIOUS
			FinancialInputRequest financialInputRequest = irrService.cmaIrrMappingService(toUserId, toApplicationId, null,
					denomination,proposalId,itrId); // CHANGES PROPOSAL ID NEW

			logger.info("financialInputRequest.getYear()===>>>{}" , financialInputRequest.getYear());
			// Profit & Loss Statement
			financialInputRequest.setNetSaleFy(CommonUtils.substractNumbers(financialInputRequest.getGrossSalesFy(),
					financialInputRequest.getLessExciseDuityFy()));
			financialInputRequest.setNetSaleSy(CommonUtils.substractNumbers(financialInputRequest.getGrossSalesSy(),
					financialInputRequest.getLessExciseDuitySy()));
			financialInputRequest.setNetSaleTy(CommonUtils.substractNumbers(financialInputRequest.getGrossSalesTy(),
					financialInputRequest.getLessExciseDuityTy()));
			financialInputRequest.setTotalExpenditureFy(CommonUtils.substractNumbers(
					CommonUtils.addNumbers(financialInputRequest.getIncreaseDecreaseStockFy(),
							financialInputRequest.getRawMaterialConsumedFy(),
							financialInputRequest.getPowerAndFuelCostFy(), financialInputRequest.getEmployeeCostFy(),
							financialInputRequest.getGeneralAndAdminExpeFy(),
							financialInputRequest.getSellingAndDistriExpeFy(), financialInputRequest.getMiscelExpeFy()),
					financialInputRequest.getLessExpeCapitaFy()));
			financialInputRequest.setTotalExpenditureSy(CommonUtils.substractNumbers(
					CommonUtils.addNumbers(financialInputRequest.getIncreaseDecreaseStockSy(),
							financialInputRequest.getRawMaterialConsumedSy(),
							financialInputRequest.getPowerAndFuelCostSy(), financialInputRequest.getEmployeeCostSy(),
							financialInputRequest.getGeneralAndAdminExpeSy(),
							financialInputRequest.getSellingAndDistriExpeSy(), financialInputRequest.getMiscelExpeSy()),
					financialInputRequest.getLessExpeCapitaSy()));
			financialInputRequest.setTotalExpenditureTy(CommonUtils.substractNumbers(
					CommonUtils.addNumbers(financialInputRequest.getIncreaseDecreaseStockTy(),
							financialInputRequest.getRawMaterialConsumedTy(),
							financialInputRequest.getPowerAndFuelCostTy(), financialInputRequest.getEmployeeCostTy(),
							financialInputRequest.getGeneralAndAdminExpeTy(),
							financialInputRequest.getSellingAndDistriExpeTy(), financialInputRequest.getMiscelExpeTy()),
					financialInputRequest.getLessExpeCapitaTy()));
			financialInputRequest.setOperatingProfitExclOiFy(CommonUtils.substractNumbers(
					financialInputRequest.getNetSaleFy(), financialInputRequest.getTotalExpenditureFy()));
			financialInputRequest.setOperatingProfitExclOiSy(CommonUtils.substractNumbers(
					financialInputRequest.getNetSaleSy(), financialInputRequest.getTotalExpenditureSy()));
			financialInputRequest.setOperatingProfitExclOiTy(CommonUtils.substractNumbers(
					financialInputRequest.getNetSaleTy(), financialInputRequest.getTotalExpenditureTy()));
			financialInputRequest.setOperatingProfitEbitadOiFy(CommonUtils.addNumbers(
					financialInputRequest.getOperatingProfitExclOiFy(), financialInputRequest.getOtherIncomeFy()));
			financialInputRequest.setOperatingProfitEbitadOiSy(CommonUtils.addNumbers(
					financialInputRequest.getOperatingProfitExclOiSy(), financialInputRequest.getOtherIncomeSy()));
			financialInputRequest.setOperatingProfitEbitadOiTy(CommonUtils.addNumbers(
					financialInputRequest.getOperatingProfitExclOiTy(), financialInputRequest.getOtherIncomeTy()));
			financialInputRequest.setPbdtFy(CommonUtils.substractNumbers(
					financialInputRequest.getOperatingProfitEbitadOiFy(), financialInputRequest.getInterestFy()));
			financialInputRequest.setPbdtSy(CommonUtils.substractNumbers(
					financialInputRequest.getOperatingProfitEbitadOiSy(), financialInputRequest.getInterestSy()));
			financialInputRequest.setPbdtTy(CommonUtils.substractNumbers(
					financialInputRequest.getOperatingProfitEbitadOiTy(), financialInputRequest.getInterestTy()));
			financialInputRequest.setProfitBeforeTaxationFy(CommonUtils
					.substractNumbers(financialInputRequest.getPbdtFy(), financialInputRequest.getDepriciationFy()));
			financialInputRequest.setProfitBeforeTaxationSy(CommonUtils
					.substractNumbers(financialInputRequest.getPbdtSy(), financialInputRequest.getDepriciationSy()));
			financialInputRequest.setProfitBeforeTaxationTy(CommonUtils
					.substractNumbers(financialInputRequest.getPbdtTy(), financialInputRequest.getDepriciationTy()));
			financialInputRequest.setProfitBeforeTaxFy(CommonUtils.addNumbers(
					financialInputRequest.getProfitBeforeTaxationFy(), financialInputRequest.getExceptionalIncomeFy()));
			financialInputRequest.setProfitBeforeTaxSy(CommonUtils.addNumbers(
					financialInputRequest.getProfitBeforeTaxationSy(), financialInputRequest.getExceptionalIncomeSy()));
			financialInputRequest.setProfitBeforeTaxTy(CommonUtils.addNumbers(
					financialInputRequest.getProfitBeforeTaxationTy(), financialInputRequest.getExceptionalIncomeTy()));
			financialInputRequest
					.setProfitAfterTaxFy(CommonUtils.substractNumbers(financialInputRequest.getProfitBeforeTaxFy(),
							financialInputRequest.getProvisionForTaxFy())
							+ financialInputRequest.getOtherIncomeNeedTocCheckOpFy());
			financialInputRequest
					.setProfitAfterTaxSy(CommonUtils.substractNumbers(financialInputRequest.getProfitBeforeTaxSy(),
							financialInputRequest.getProvisionForTaxSy())
							+ (financialInputRequest.getOtherIncomeNeedTocCheckOpSy() != null ? financialInputRequest.getOtherIncomeNeedTocCheckOpSy() :0.0));
			financialInputRequest
					.setProfitAfterTaxTy(CommonUtils.substractNumbers(financialInputRequest.getProfitBeforeTaxTy(),
							financialInputRequest.getProvisionForTaxTy())
							+ (financialInputRequest.getOtherIncomeNeedTocCheckOpTy() != null ? financialInputRequest.getOtherIncomeNeedTocCheckOpTy() :0.0));
			if (financialInputRequest.getDividendPayOutFy() == 0)
				financialInputRequest.setEquityDividendFy(0.0);
			else
				financialInputRequest.setEquityDividendFy(financialInputRequest.getDividendPayOutFy()
						* financialInputRequest.getShareFaceValue() / financialInputRequest.getShareCapitalFy());
			if(financialInputRequest.getDividendPayOutSy()!=null) {
				if (financialInputRequest.getDividendPayOutSy() == 0)
					financialInputRequest.setEquityDividendSy(0.0);
				else
					financialInputRequest.setEquityDividendSy(financialInputRequest.getDividendPayOutSy()
							* financialInputRequest.getShareFaceValue() / financialInputRequest.getShareCapitalSy());	
			}else {
				financialInputRequest.setEquityDividendSy(0.0);
			}
			
			if( financialInputRequest.getDividendPayOutTy() !=null) {
			
				if (financialInputRequest.getDividendPayOutTy() == 0)
					financialInputRequest.setEquityDividendTy(0.0);
				else
					financialInputRequest.setEquityDividendTy(financialInputRequest.getDividendPayOutTy()
							* financialInputRequest.getShareFaceValue() / financialInputRequest.getShareCapitalTy());
				
			}else {
				financialInputRequest.setEquityDividendTy(0.0);	
			}
			

			financialInputRequest.setEarningPerShareFy((financialInputRequest.getProfitAfterTaxFy())
					* financialInputRequest.getShareFaceValue() / financialInputRequest.getShareCapitalFy());
			financialInputRequest.setEarningPerShareSy((financialInputRequest.getProfitAfterTaxSy())
					* financialInputRequest.getShareFaceValue() / financialInputRequest.getShareCapitalSy());
			financialInputRequest.setEarningPerShareTy((financialInputRequest.getProfitAfterTaxTy())
					* financialInputRequest.getShareFaceValue() / financialInputRequest.getShareCapitalTy());

			// Balance Sheet -Equities and Liabilities
			financialInputRequest.setShareHolderFundsFy(CommonUtils.addNumbers(
					financialInputRequest.getShareCapitalFy(), financialInputRequest.getShareWarrantOutstandingsFy(),
					financialInputRequest.getRevalationReserveFy(),
					financialInputRequest.getOtherReserveAndSurplusFy()));
			financialInputRequest.setShareHolderFundsSy(CommonUtils.addNumbers(
					financialInputRequest.getShareCapitalSy(), financialInputRequest.getShareWarrantOutstandingsSy(),
					financialInputRequest.getRevalationReserveSy(),
					financialInputRequest.getOtherReserveAndSurplusSy()));
			financialInputRequest.setShareHolderFundsTy(CommonUtils.addNumbers(
					financialInputRequest.getShareCapitalTy(), financialInputRequest.getShareWarrantOutstandingsTy(),
					financialInputRequest.getRevalationReserveTy(),
					financialInputRequest.getOtherReserveAndSurplusTy()));
			financialInputRequest.setTotalNonCurruntLiablitiesFy(CommonUtils.addNumbers(
					financialInputRequest.getMinorityInterestFy(), financialInputRequest.getSecuredLoansFy(),
					financialInputRequest.getUnsecuredLoansOthersFy(),
					financialInputRequest.getUnsecuredLoansPromotersFy(),
					financialInputRequest.getDeferredTaxLiablitiesFy(),
					financialInputRequest.getOtherLongTermLiablitiesFy(), financialInputRequest.getOtherBorrowingFy(),
					financialInputRequest.getLongTermProvisionFy()));
			financialInputRequest.setTotalNonCurruntLiablitiesSy(CommonUtils.addNumbers(
					financialInputRequest.getMinorityInterestSy(), financialInputRequest.getSecuredLoansSy(),
					financialInputRequest.getUnsecuredLoansOthersSy(),
					financialInputRequest.getUnsecuredLoansPromotersSy(),
					financialInputRequest.getDeferredTaxLiablitiesSy(),
					financialInputRequest.getOtherLongTermLiablitiesSy(), financialInputRequest.getOtherBorrowingSy(),
					financialInputRequest.getLongTermProvisionSy()));
			financialInputRequest.setTotalNonCurruntLiablitiesTy(CommonUtils.addNumbers(
					financialInputRequest.getMinorityInterestTy(), financialInputRequest.getSecuredLoansTy(),
					financialInputRequest.getUnsecuredLoansOthersTy(),
					financialInputRequest.getUnsecuredLoansPromotersTy(),
					financialInputRequest.getDeferredTaxLiablitiesTy(),
					financialInputRequest.getOtherLongTermLiablitiesTy(), financialInputRequest.getOtherBorrowingTy(),
					financialInputRequest.getLongTermProvisionTy()));
			financialInputRequest.setTotalCurruntLiablitiesFy(CommonUtils.addNumbers(
					financialInputRequest.getTradePayablesFy(), financialInputRequest.getOtherCurruntLiablitiesFy(),
					financialInputRequest.getShortTermProvisionFy()));
			financialInputRequest.setTotalCurruntLiablitiesSy(CommonUtils.addNumbers(
					financialInputRequest.getTradePayablesSy(), financialInputRequest.getOtherCurruntLiablitiesSy(),
					financialInputRequest.getShortTermProvisionSy()));
			financialInputRequest.setTotalCurruntLiablitiesTy(CommonUtils.addNumbers(
					financialInputRequest.getTradePayablesTy(), financialInputRequest.getOtherCurruntLiablitiesTy(),
					financialInputRequest.getShortTermProvisionTy()));
			financialInputRequest
					.setTotalLiablitiesFy(CommonUtils.addNumbers(financialInputRequest.getShareHolderFundsFy(),
							financialInputRequest.getTotalNonCurruntLiablitiesFy(),
							financialInputRequest.getTotalCurruntLiablitiesFy(),
							financialInputRequest.getOtherIncomeNeedTocCheckLiaFy()));
			financialInputRequest
					.setTotalLiablitiesSy(CommonUtils.addNumbers(financialInputRequest.getShareHolderFundsSy(),
							financialInputRequest.getTotalNonCurruntLiablitiesSy(),
							financialInputRequest.getTotalCurruntLiablitiesSy(),
							financialInputRequest.getOtherIncomeNeedTocCheckLiaSy()));
			financialInputRequest
					.setTotalLiablitiesTy(CommonUtils.addNumbers(financialInputRequest.getShareHolderFundsTy(),
							financialInputRequest.getTotalNonCurruntLiablitiesTy(),
							financialInputRequest.getTotalCurruntLiablitiesTy(),
							financialInputRequest.getOtherIncomeNeedTocCheckLiaTy()));

			// Balance Sheet -ASSETS
			financialInputRequest.setNetBlockFy(CommonUtils.substractThreeNumbers(
					financialInputRequest.getGrossBlockFy(), financialInputRequest.getLessAccumulatedDepreFy(),
					financialInputRequest.getImpairmentofAssetFy()));
			financialInputRequest.setNetBlockSy(CommonUtils.substractThreeNumbers(
					financialInputRequest.getGrossBlockSy(), financialInputRequest.getLessAccumulatedDepreSy(),
					financialInputRequest.getImpairmentofAssetSy()));
			financialInputRequest.setNetBlockTy(CommonUtils.substractThreeNumbers(
					financialInputRequest.getGrossBlockTy(), financialInputRequest.getLessAccumulatedDepreTy(),
					financialInputRequest.getImpairmentofAssetTy()));
			financialInputRequest.setTotalNonCurruntAssetFy(CommonUtils.addNumbers(
					financialInputRequest.getCapitalWorkInProgressFy(), financialInputRequest.getIntengibleAssetsFy(),
					financialInputRequest.getPreOperativeExpeFy(), financialInputRequest.getAssetInTransitFy(),
					financialInputRequest.getInvestmentInSubsidiariesFy(), financialInputRequest.getOtherInvestmentFy(),
					financialInputRequest.getLongTermLoansAndAdvaFy(),
					financialInputRequest.getOtheNonCurruntAssetFy()));
			financialInputRequest.setTotalNonCurruntAssetSy(CommonUtils.addNumbers(
					financialInputRequest.getCapitalWorkInProgressSy(), financialInputRequest.getIntengibleAssetsSy(),
					financialInputRequest.getPreOperativeExpeSy(), financialInputRequest.getAssetInTransitSy(),
					financialInputRequest.getInvestmentInSubsidiariesSy(), financialInputRequest.getOtherInvestmentSy(),
					financialInputRequest.getLongTermLoansAndAdvaSy(),
					financialInputRequest.getOtheNonCurruntAssetSy()));
			financialInputRequest.setTotalNonCurruntAssetTy(CommonUtils.addNumbers(
					financialInputRequest.getCapitalWorkInProgressTy(), financialInputRequest.getIntengibleAssetsTy(),
					financialInputRequest.getPreOperativeExpeTy(), financialInputRequest.getAssetInTransitTy(),
					financialInputRequest.getInvestmentInSubsidiariesTy(), financialInputRequest.getOtherInvestmentTy(),
					financialInputRequest.getLongTermLoansAndAdvaTy(),
					financialInputRequest.getOtheNonCurruntAssetTy()));
			financialInputRequest.setTotalCurruntAssetFy(CommonUtils.addNumbers(
					financialInputRequest.getInventoriesFy(), financialInputRequest.getSundryDebtorsFy(),
					financialInputRequest.getCashAndBankFy(), financialInputRequest.getOtherCurruntAssetFy(),
					financialInputRequest.getShortTermLoansAdvancesFy()));
			financialInputRequest.setTotalCurruntAssetSy(CommonUtils.addNumbers(
					financialInputRequest.getInventoriesSy(), financialInputRequest.getSundryDebtorsSy(),
					financialInputRequest.getCashAndBankSy(), financialInputRequest.getOtherCurruntAssetSy(),
					financialInputRequest.getShortTermLoansAdvancesSy()));
			financialInputRequest.setTotalCurruntAssetTy(CommonUtils.addNumbers(
					financialInputRequest.getInventoriesTy(), financialInputRequest.getSundryDebtorsTy(),
					financialInputRequest.getCashAndBankTy(), financialInputRequest.getOtherCurruntAssetTy(),
					financialInputRequest.getShortTermLoansAdvancesTy()));
			financialInputRequest.setTotalAssetFy(CommonUtils.addNumbers(financialInputRequest.getNetBlockFy(),
					financialInputRequest.getTotalCurruntAssetFy(), financialInputRequest.getTotalNonCurruntAssetFy(),
					financialInputRequest.getOtherIncomeNeedTocCheckAssetFy()));
			financialInputRequest.setTotalAssetSy(CommonUtils.addNumbers(financialInputRequest.getNetBlockSy(),
					financialInputRequest.getTotalCurruntAssetSy(), financialInputRequest.getTotalNonCurruntAssetSy(),
					financialInputRequest.getOtherIncomeNeedTocCheckAssetSy()));
			financialInputRequest.setTotalAssetTy(CommonUtils.addNumbers(financialInputRequest.getNetBlockTy(),
					financialInputRequest.getTotalCurruntAssetTy(), financialInputRequest.getTotalNonCurruntAssetTy(),
					financialInputRequest.getOtherIncomeNeedTocCheckAssetTy()));
			financialInputRequest.setBookValueFy(financialInputRequest.getShareHolderFundsFy()
					/ (financialInputRequest.getShareCapitalFy() / financialInputRequest.getShareFaceValue()));
			financialInputRequest.setBookValueSy(financialInputRequest.getShareHolderFundsSy()
					/ (financialInputRequest.getShareCapitalSy() / financialInputRequest.getShareFaceValue()));
			financialInputRequest.setBookValueTy(financialInputRequest.getShareHolderFundsTy()
					/ (financialInputRequest.getShareCapitalTy() / financialInputRequest.getShareFaceValue()));
			corporatePrimaryViewResponse.setFinancialInputRequest(financialInputRequest);

		} catch (Exception e) {
			logger.error("Error From Irr Side While Calculate Financial data .....",e);
		}
		
		/*get cmr score cibil */
		
		try {
				String cmrScore= cibilClient.getCMRScore(applicationId);
				if (cmrScore != null && cmrScore.contains("EXP")) {
					corporatePrimaryViewResponse.setMsmeRankTitle("Experian");
				}else if (cmrScore != null && cmrScore.contains("CIBIL")) {
					corporatePrimaryViewResponse.setMsmeRankTitle("Cibil");
				}else {
					corporatePrimaryViewResponse.setMsmeRankTitle("MSME Ranking");
				}
				corporatePrimaryViewResponse.setCibilCmrScore(cmrScore != null ? cmrScore : "Not Found");	
			
		} catch (Exception e) {
			logger.info("Exception while get CIBIL CMR Score {}",e);
		}
		

		// itr xml isUpload or Online check
		try {
			ITRConnectionResponse itrConnectionResponse = itrClient.getIsUploadAndYearDetails(itrId);
			if (itrConnectionResponse != null) {
				corporatePrimaryViewResponse.setItrXmlIsUploaded(itrConnectionResponse.getData());
			} else {
				logger.info("itr Response is null");

			}

		} catch (Exception e) {
			logger.error("error while itr xml is uploaded or not check : ",e);
		}

		// bank statement data
		ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(toApplicationId);
		reportRequest.setBsMasterId(bsId);
		reportRequest.setUserId(toUserId);
		List<Data> datas = new ArrayList<>();
		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			List<HashMap<String, Object>> hashMaps = (List<HashMap<String, Object>>) analyzerResponse.getData();
			if (!CommonUtils.isListNullOrEmpty(hashMaps)) {
				for (HashMap<String, Object> hashMap : hashMaps) {
					Data data = MultipleJSONObjectHelper.getObjectFromMap(hashMap, Data.class);
					datas.add(data);
				}
			}
			corporatePrimaryViewResponse.setBankData(datas);
			// Data data = MultipleJSONObjectHelper.getObjectFromMap((HashMap<String,
			// Object>) analyzerResponse.getData(), Data.class);
			// corporatePrimaryViewResponse.setMonthlyDetailList(data.getMonthlyDetailList());
			// corporatePrimaryViewResponse.setTop5FundReceivedList(data.getTop5FundReceivedList());
			// corporatePrimaryViewResponse.setTop5FundTransferedList(data.getTop5FundTransferedList());
		} catch (Exception e) {
			logger.error("Error while getting perfios data : ",e);
		}

		// scoring Data
		Long fpProductMappingId = null;
		try {

			UsersRequest usersRequest = new UsersRequest();
			usersRequest.setId(fundProviderUserId);
			UserResponse userResponse = usersClient.getLastAccessApplicant(usersRequest);
			if(userResponse != null && userResponse.getId() != null) {

				fpProductMappingId = userResponse.getId();

			}else {

				logger.info("fpProduuct MappingId is Null..");
			}

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		ScoringRequest scoringRequest = new ScoringRequest();
		scoringRequest.setApplicationId(toApplicationId);
		scoringRequest.setFpProductId(fpProductMappingId);
		try {
			logger.info("Enter in get scoing data -----APPID-->{}  --FPProductId-->{}", toApplicationId ,fpProductMappingId);
			ScoringResponse scoringResponse = scoringClient.getScore(scoringRequest);
			if (!CommonUtils.isObjectNullOrEmpty(scoringResponse)
					&& !CommonUtils.isObjectNullOrEmpty(scoringResponse.getDataObject())) {
				logger.info("Find Data From Scoring ");
				ProposalScoreResponse proposalScoreResponse = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) scoringResponse.getDataObject(), ProposalScoreResponse.class);
				corporatePrimaryViewResponse.setDataList(scoringResponse.getDataList());
				corporatePrimaryViewResponse.setScoringModelName(proposalScoreResponse.getScoringModelName() != null ? proposalScoreResponse.getScoringModelName() : "-");
				corporatePrimaryViewResponse.setManagementRiskScore(proposalScoreResponse.getManagementRiskScore());
				corporatePrimaryViewResponse.setFinancialRiskScore(proposalScoreResponse.getFinancialRiskScore());
				corporatePrimaryViewResponse.setBuisnessRiskScore(proposalScoreResponse.getBusinessRiskScore());
				corporatePrimaryViewResponse
						.setManagementRiskScoreWeight(proposalScoreResponse.getManagementRiskWeight());
				corporatePrimaryViewResponse
						.setFinancialRiskScoreWeight(proposalScoreResponse.getFinancialRiskWeight());
				corporatePrimaryViewResponse.setBuisnessRiskScoreWeight(proposalScoreResponse.getBusinessRiskWeight());
				corporatePrimaryViewResponse.setScoreInterpretation(proposalScoreResponse.getInterpretation());
				corporatePrimaryViewResponse
						.setManagementRiskMaxTotalScore(proposalScoreResponse.getManagementRiskMaxTotalScore());
				corporatePrimaryViewResponse
						.setFinancialRiskMaxTotalScore(proposalScoreResponse.getFinancialRiskMaxTotalScore());
				corporatePrimaryViewResponse
						.setBusinessRiskMaxTotalScore(proposalScoreResponse.getBusinessRiskMaxTotalScore());
				corporatePrimaryViewResponse
						.setManagementRiskWeightOfScoring(proposalScoreResponse.getManagementRiskWeightOfScoring());
				corporatePrimaryViewResponse
						.setFinancialRiskWeightOfScoring(proposalScoreResponse.getFinancialRiskWeightOfScoring());
				corporatePrimaryViewResponse
						.setBusinessRiskWeightOfScoring(proposalScoreResponse.getBusinessRiskWeightOfScoring());

				// if true so show 3 col

				corporatePrimaryViewResponse.setWeightConsider(proposalScoreResponse.getWeightConsider());
				corporatePrimaryViewResponse
						.setManagementRiskMaxTotalWeight(proposalScoreResponse.getManagementRiskMaxTotalWeight());
				corporatePrimaryViewResponse
						.setFinancialRiskMaxTotalWeight(proposalScoreResponse.getFinancialRiskMaxTotalWeight());
				corporatePrimaryViewResponse
						.setBusinessRiskMaxTotalWeight(proposalScoreResponse.getBusinessRiskMaxTotalWeight());

				// if ture so show two col
				corporatePrimaryViewResponse
						.setIsProportionateScoreConsider(proposalScoreResponse.getIsProportionateScoreConsider());// Score(out
																													// of),
																													// proportionateScoreFS
				corporatePrimaryViewResponse.setProportionateScore(proposalScoreResponse.getProportionateScore());
				corporatePrimaryViewResponse.setProportionateScoreFS(proposalScoreResponse.getProportionateScoreFS());

				logger.info("complete");
			} else {
				logger.info("SCORING OBJECT NULL OR EMPTY -------------------->");
			} 
		} catch (ScoringException | IOException e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}

		// Eligibility Data


		try {

			//int loanId = primaryCorporateDetail.getProductId(); // previous
			int loanId = applicationProposalMapping.getProductId(); // NEW BASED ON PROPOAL MAPPING PRODUCT ID
			switch (loanId) {
			case 1:

				WorkingCapitalParameter workingCapitalPara = workingCapitalRepository.getByID(fpProductMappingId);
				if (workingCapitalPara != null && workingCapitalPara.getAssessmentMethodId() != null) {
					Long assessmentId = workingCapitalPara.getAssessmentMethodId().longValue();
					corporatePrimaryViewResponse.setAssesmentId(assessmentId);
				} else {
					logger.info("assesment id is null in wc");
				}
				break;
			case 2:

				TermLoanParameter termLoanParameter = termLoanParameterRepository.getById(fpProductMappingId);
				if (termLoanParameter != null && termLoanParameter.getAssessmentMethodId() != null) {
					Long assessmentId = termLoanParameter.getAssessmentMethodId().longValue();
					corporatePrimaryViewResponse.setAssesmentId(assessmentId);
				} else {
					logger.info("assesment id is null tl");
				}
				break;

			case 16:

				WcTlParameter wctlPara = wctlrepo.getById(fpProductMappingId);
				if (wctlPara !=null  && wctlPara.getAssessmentMethodId() != null) {
					Long assessmentId = wctlPara.getAssessmentMethodId().longValue();
					corporatePrimaryViewResponse.setAssesmentId(assessmentId);
				} else {
					logger.info("assesment id is null in wctl");
				}
				break;
			default:
				logger.info("invalid loan id");
				break;
			}

			EligibililityRequest eligibilityReq = new EligibililityRequest();
			eligibilityReq.setApplicationId(toApplicationId);
			// eligibilityReq.set
			eligibilityReq.setFpProductId(fpProductMappingId);
			logger.info(" for eligibility appid============>>{}" , toApplicationId);

			try {

				EligibilityResponse eligibilityResp = eligibilityClient.corporateEligibilityData(eligibilityReq);
				corporatePrimaryViewResponse.setEligibilityDataObject(eligibilityResp.getData());

			} catch (Exception e1) {
				logger.error(CommonUtils.EXCEPTION,e1);
			}
			/*loan eligibility financial year  */
            corporatePrimaryViewResponse.setEligibilityFinancialYear(CommonUtils.getFinancialYear());

            // CGTMSE
			try {

				CGTMSEDataResponse cgtmseDataResp = thirdPartyClient.getCalulation(toApplicationId,fpProductMappingId);

				corporatePrimaryViewResponse.setCgtmseData(cgtmseDataResp);

			} catch (Exception e) {
				logger.error("Error while calling CGTMSE data : ",e);
			}
			
		

		} catch (Exception e) {
			logger.error("FpProductId is null : ",e);
		}
		
		

		ObjectMapper mapper = new ObjectMapper();
		ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
		proposalMappingRequest.setApplicationId(toApplicationId);
		proposalMappingRequest.setFpProductId(fpProductMappingId);
		ProposalMappingResponse proposalMappingResponse = null;
		try {
			proposalMappingResponse = proposalDetailsClient.getActiveProposalDetails(proposalMappingRequest);
			ProposalMappingRequestString proposalMappingRequestString = mapper.convertValue(proposalMappingResponse.getData(), ProposalMappingRequestString.class);
			if(proposalMappingRequestString != null) {
				corporatePrimaryViewResponse.setScoringBasedOn(proposalMappingRequestString.getScoringModelBasedOn() != null && proposalMappingRequestString.getScoringModelBasedOn() == 2 ? "MCLR" : "EBLR");
				corporatePrimaryViewResponse.setMclrRoi(proposalMappingRequestString.getMclrRoi() != null ? proposalMappingRequestString.getMclrRoi().toString() : "-");
				corporatePrimaryViewResponse.setSpreadRoi(proposalMappingRequestString.getSpreadRoi() != null ? proposalMappingRequestString.getSpreadRoi().toString() : "-");
			    
			    if (!CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString.getMclrRoi()) && !CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString.getSpreadRoi())) {
			    	corporatePrimaryViewResponse.setEffectiveRoi(String.valueOf(Double.valueOf(proposalMappingRequestString.getMclrRoi()) + Double.valueOf(proposalMappingRequestString.getSpreadRoi())));		    	
				} else {
					corporatePrimaryViewResponse.setEffectiveRoi(proposalMappingRequestString.getMclrRoi() == null && proposalMappingRequestString.getSpreadRoi() == null ? "-" : proposalMappingRequestString.getMclrRoi() != null ? proposalMappingRequestString.getMclrRoi().toString() : proposalMappingRequestString.getSpreadRoi().toString());				
				}
			    corporatePrimaryViewResponse.setConcessionRoi(proposalMappingRequestString.getConsessionRoi() != null && proposalMappingRequestString.getConsessionRoi() != 0.0 && proposalMappingRequestString.getConsessionRoi() != 0 ? proposalMappingRequestString.getConsessionRoi().toString() : "-");
			    corporatePrimaryViewResponse.setConcessionRoiBased(proposalMappingRequestString.getConcessionBasedOnType() != null ? "- " + proposalMappingRequestString.getConcessionBasedOnType() : "No Concession");
			    if (corporatePrimaryViewResponse.getEffectiveRoi() != null) {
			    	corporatePrimaryViewResponse.setFinalRoi(proposalMappingRequestString.getConsessionRoi() != null ? String.valueOf(Double.valueOf(corporatePrimaryViewResponse.getEffectiveRoi()) - Double.valueOf(proposalMappingRequestString.getConsessionRoi())) : "-" );
				} else {
					corporatePrimaryViewResponse.setFinalRoi("-");
				}
			}
		    
		} catch (MatchException matchException) {
			logger.error("Calculation for Actual ROI Exception : ",matchException);
		}



		// MCA DATA
		try {
			String companyId = loanApplicationMaster.getMcaCompanyId();
			logger.info("mca comp id==>>{}" , companyId);
			VerifyAPIRequest verifyReq=new VerifyAPIRequest();
			verifyReq.setApplicationId(toApplicationId);
			McaResponse directorData = mcaClient.getVerifyApiData(verifyReq);
			if(directorData!= null) {
				corporatePrimaryViewResponse.setVerifyApiData(directorData.getData());
			}
			if (companyId != null) {
				corporatePrimaryViewResponse.setMcaNotApplicable(Boolean.FALSE);
				McaResponse mcaResponse = mcaClient.getCompanyDetailedData(companyId);
				McaResponse mcaStatusResponse = mcaClient.mcaStatusCheck(String.valueOf(toApplicationId), companyId);
				if (mcaStatusResponse != null) {
					corporatePrimaryViewResponse.setMcaCheckStatus(mcaStatusResponse.getData());
				} else {

					logger.warn("::::::=====MCA Check Status Data is Null====:::::::For:::::CompanyId==>{}" , companyId);
				}

				if (!CommonUtils.isObjectNullOrEmpty(mcaResponse.getData())) {

					corporatePrimaryViewResponse.setMcaData(mcaResponse);

				} else {

					logger.warn("::::::=====MCA Data is Null====:::::::For:::::CompanyId==>{}" , companyId);
				}

				McaResponse mcaFinancialAndDetailsRes=mcaClient.getCompanyFinancialCalcAndDetails(toApplicationId, companyId);
				if(mcaFinancialAndDetailsRes.getData()!=null) {
					corporatePrimaryViewResponse.setMcaFinancialAndDetailsResponse(mcaFinancialAndDetailsRes);
				}else {
					logger.info("::::::=====MCA Financial Data is Null====:::::::For:::::CompanyId==>{}  AppId==>{}", companyId ,toApplicationId);
				}
			} else {
				corporatePrimaryViewResponse.setMcaNotApplicable(Boolean.TRUE);
				logger.warn("Mca Company Id is Null");

			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// Name As Per

		try {
			ITRConnectionResponse resNameAsPerITR = itrClient.getITRBasicDetails(itrId);
			if (resNameAsPerITR != null) {

				corporatePrimaryViewResponse
						.setNameAsPerItr(resNameAsPerITR.getData() != null ? resNameAsPerITR.getData() : "NA");
			} else {

				logger.warn("-----------:::::::::::::: ItrResponse is null ::::::::::::---------");
			}

		} catch (Exception e) {
			logger.error(":::::::::::---------Error while fetching name as per itr----------:::::::::::",e);
		}

		// Gst Data

		try {

				/*if(corporateApplicantDetail.getGstIn()!= null) {*/
				CAMGSTData resp =null;
				GSTR1Request req= new GSTR1Request();
				req.setApplicationId(gstId);
				req.setUserId(toUserId);
				req.setGstin(corporateApplicantDetail.getGstIn());
				
				GstResponse response = gstClient.detailCalculation(req);
				
				if (response != null) {
					DecimalFormat df = new DecimalFormat(".##");
					if (!CommonUtils.isObjectNullOrEmpty(response)) {
						for (LinkedHashMap<String, Object> data : (List<LinkedHashMap<String, Object>>) response.getData()) {
							resp = MultipleJSONObjectHelper.getObjectFromMap(data,CAMGSTData.class);
							Double totalSales =0.0d;
							if(resp.getMomSales() != null) {
								List<MomSales> momSalesResp = resp.getMomSales();
								for (MomSales sales : momSalesResp) {
									totalSales += Double.valueOf(sales.getValue());
								}
								data.put("totalMomSales", df.format(totalSales));
							}
						}
					corporatePrimaryViewResponse.setGstData((List<LinkedHashMap<String, Object>>) response.getData());
					} else {
						logger.warn("----------:::::::: Gst Response is null :::::::---------");
					}
				}
			} catch (Exception e) {
				logger.error(":::::::------Error while calling gstData---:::::::",e);
			}

		// Fraud Detection Data

		try {
			UserResponse campaignUser=usersClient.isExists(toUserId,null);
			corporatePrimaryViewResponse.setIsCampaignUser(campaignUser!= null && campaignUser.getData()!= null ? (Boolean) campaignUser.getData() : null);
			if(campaignUser!= null && campaignUser.getData().equals(false)) {
				AnalyticsResponse hunterResp = fraudAnalyticsClient.getRuleAnalysisData(toApplicationId);
				if (!CommonUtils.isObjectListNull(hunterResp, hunterResp.getData())) {
					corporatePrimaryViewResponse.setFraudDetectionData(hunterResp);
				}else {
					logger.info("application is bank specific so fraud detection is skipped for===>"+applicationId);
				}	
			}
			
		} catch (Exception e1) {
			logger.error("------:::::...Error while fetching Fraud Detection Details...For..::::::-----" + toApplicationId + CommonUtils.EXCEPTION + e1);
		}
		
		try {
			ManualBsReportRequest manualBsReportRequest = new ManualBsReportRequest();
	    	manualBsReportRequest.setBsMasterId(bsId);
	    	AnalyzerResponse analyzerResponse = analyzerClient.getManualBankAccDetails(manualBsReportRequest);
	    	if (!CommonUtils.isObjectNullOrEmpty(analyzerResponse) && !CommonUtils.isObjectNullOrEmpty(analyzerResponse.getData())) {
	    		LinkedHashMap<String, Object> bankStatementResponse = (LinkedHashMap<String, Object>)analyzerResponse.getData();
			
	    		corporatePrimaryViewResponse.setName(bankStatementResponse.get("accountHolderName"));
	    		corporatePrimaryViewResponse.setAccountName(bankStatementResponse.get("accountHolderName"));
	    		corporatePrimaryViewResponse.setAccountNumber(bankStatementResponse.get("bankAccountNo"));
	    		corporatePrimaryViewResponse.setIfsc(bankStatementResponse.get("ifscCode"));
	    		corporatePrimaryViewResponse.setStatus(bankStatementResponse.get(1));
	    		String year = bankStatementResponse.get("sinceYear") != null ? String.valueOf(bankStatementResponse.get("sinceYear")) : null;
				String months = bankStatementResponse.get("sinceMonth") != null ? String.valueOf(bankStatementResponse.get("sinceMonth")) : null;
				if (!CommonUtils.isObjectNullOrEmpty(year) || !CommonUtils.isObjectNullOrEmpty(months)) {
					LocalDate today = LocalDate.now();
					LocalDate since = LocalDate.of(Integer.parseInt(year), Integer.parseInt(months),1);
					Period age = Period.between(since, today);
					corporatePrimaryViewResponse.setNoBsSinceYear(age.getYears());
					corporatePrimaryViewResponse.setNoBsSinceMonths(age.getMonths());
					corporatePrimaryViewResponse.setNoBsSinceYear((!CommonUtils.isObjectNullOrEmpty(age.getYears()) ? age.getYears() +" year " : "") + " " +(!CommonUtils.isObjectNullOrEmpty(age.getMonths()) ? age.getMonths()+" months" :  "" ));
				}
				
				
	    		corporatePrimaryViewResponse.setBankStatement((List<Object>) corporatePrimaryViewResponse);
	    	}
		}
		catch (Exception e) {
			logger.error("Error while fetching No Bank Statement Account data From Analyzer with BsMasterId==>{} with Error:{} ",bsId , e);	
		}
		
		// Product Name

		if(fpProductMappingId != null) {
			String productName = productMasterRepository.getFpProductName(fpProductMappingId);
			if(productName != null) {
				corporatePrimaryViewResponse.setFpProductName(productName);
			}else {
				logger.info("product name is null..");
			}
		}else {
			logger.info("fpProductMapping id is null..");
		}
		
		// address
		
		CorporateFinalInfoRequest corporateFinalInfoRequest;
		try {
			//corporateFinalInfoRequest = corporateFinalInfoService.get(userId,toApplicationId); // PREVIOUS CHANGES
			corporateFinalInfoRequest = corporateFinalInfoService.getByProposalId(toUserId,proposalId); //// NEW BASED ON PROPOSAL MAPPING ID

			//ADMIN OFFICE ADDRESS
			try {
				if(corporateFinalInfoRequest.getSecondAddress().getDistrictMappingId() != null) {
					
					PincodeDataResponse pindata=pincodeDateService.getById(corporateFinalInfoRequest.getSecondAddress().getDistrictMappingId());
					corporatePrimaryViewResponse.setAdminAddDist(pindata.getDistrictName());
					corporatePrimaryViewResponse.setAdminAddTaluko(pindata.getTaluka());
					pindata.getTaluka();
				}
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			if(!CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getSecondAddress())){
				
				corporatePrimaryViewResponse.setAdminAdd( (corporateFinalInfoRequest.getSecondAddress().getPremiseNumber()!=null ? (CommonUtils.commaReplace(corporateFinalInfoRequest.getSecondAddress().getPremiseNumber())) :"") + (corporateFinalInfoRequest.getSecondAddress().getStreetName() != null ? (CommonUtils.commaReplace(corporateFinalInfoRequest.getSecondAddress().getStreetName())) : "") + (corporateFinalInfoRequest.getSecondAddress().getLandMark() != null ? (CommonUtils.commaReplace(corporateFinalInfoRequest.getSecondAddress().getLandMark())) : "")+ (corporatePrimaryViewResponse.getAdminAddDist() != null ?(CommonUtils.commaReplace(corporatePrimaryViewResponse.getAdminAddDist())) :"")+ (corporatePrimaryViewResponse.getAdminAddTaluko() != null ? (CommonUtils.commaReplace(corporatePrimaryViewResponse.getAdminAddTaluko())) : "") + (corporateFinalInfoRequest.getSecondAddress().getPincode() != null ? (corporateFinalInfoRequest.getSecondAddress().getPincode()) : ""));
			}
		}
		catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			}	
			
		
		
		// address
		
				
				try {
					//corporateFinalInfoRequest = corporateFinalInfoService.get(userId,toApplicationId); // PREVIOUS
					corporateFinalInfoRequest = corporateFinalInfoService.getByProposalId(toUserId,proposalId); // NEW BASED ON PROPOSAL MAPPING ID
				//	logger.info("==================>"+corporateFinalInfoRequest.getFirstAddress());
					logger.info("==================>{}",corporateFinalInfoRequest.getSecondAddress().getDistrictMappingId());
					//Reg OFFICE ADDRESS
					try {
						if(corporateFinalInfoRequest.getSecondAddress().getDistrictMappingId() != null) {
							
							PincodeDataResponse pindata=pincodeDateService.getById(corporateFinalInfoRequest.getFirstAddress().getDistrictMappingId());
							corporatePrimaryViewResponse.setRegAddDist(pindata.getDistrictName());
							corporatePrimaryViewResponse.setRegAddTaluko(pindata.getTaluka());
							pindata.getTaluka();
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					if(!CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFirstAddress())){
						
						corporatePrimaryViewResponse.setRegAdd( (corporateFinalInfoRequest.getFirstAddress().getPremiseNumber()!=null ? (CommonUtils.commaReplace(corporateFinalInfoRequest.getFirstAddress().getPremiseNumber())) :"") + (corporateFinalInfoRequest.getFirstAddress().getStreetName() != null ? (CommonUtils.commaReplace(corporateFinalInfoRequest.getFirstAddress().getStreetName())) : "") + (corporateFinalInfoRequest.getFirstAddress().getLandMark() != null ? (CommonUtils.commaReplace(corporateFinalInfoRequest.getFirstAddress().getLandMark())) : "")+ (corporatePrimaryViewResponse.getRegAddDist() != null ?(CommonUtils.commaReplace(corporatePrimaryViewResponse.getRegAddDist())) :"")+ (corporatePrimaryViewResponse.getRegAddTaluko() != null ? (CommonUtils.commaReplace(corporatePrimaryViewResponse.getRegAddTaluko())) : "") + (corporateFinalInfoRequest.getFirstAddress().getPincode() != null ? (corporateFinalInfoRequest.getFirstAddress().getPincode()) : ""));
					}
				}
				catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
					}	
		

		// GET DOCUMENTS
		DocumentRequest documentRequest = new DocumentRequest();
		documentRequest.setApplicationId(toApplicationId);
		documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
		documentRequest.setProductDocumentMappingId(DocumentAlias.WORKING_CAPITAL_PROFIEL_PICTURE);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setProfilePic(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.WORKING_CAPITAL_BANK_STATEMENT);
		try {
			DocumentRequest documentRequest1 = new DocumentRequest();
			documentRequest1.setApplicationId(toApplicationId);
			documentRequest1.setUserType(DocumentAlias.USER_TYPE_APPLICANT_PROFILE);
			documentRequest1.setProfileId(profileId);
			//TO GET BANK STATEMENT
			List<Long> proMapIds=new ArrayList<Long>();
			proMapIds.add(DocumentAlias.WORKING_CAPITAL_BANK_STATEMENT);
			documentRequest1.setProMapIds(proMapIds);
			documentRequest1.setModuleMasterId(bsId);
			DocumentResponse documentResponse = dmsClient.listProDocByMultiProMapId(documentRequest1);
			//DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setBankStatement(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.CIBIL_REPORT_MSME_COMPANY);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setCibilReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.CIBIL_REPORT_HUF_COMMERCIAL);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setHufReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.CIBIL_REPORT_MSME_CONSUMER);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setCibilConsumerReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.CORPORATE_ITR_PDF);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setIrtPdfReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		// FOR NBFC RELATED CHANGES
		documentRequest.setProductDocumentMappingId(DocumentAlias.NBFC_PAN_UPLOAD);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setNbfcPANReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		documentRequest.setProductDocumentMappingId(DocumentAlias.NBFC_ADDRESS_PROOF);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setNbfcAddressProofReport(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		documentRequest.setProductDocumentMappingId(DocumentAlias.NBFC_PROJECTED_FINANCIALS);
		try {
			DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
			corporatePrimaryViewResponse.setNbfcProjectedFinancials(documentResponse.getDataList());
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		//ENDS HERE NBFC DOWNLOADS
		
		if(gstCompRelFlag != null && gstCompRelFlag) {
			LinkedHashMap<String, Object> gstVsItrVsBsComparision = gstVsItrVsBsComparision(applicationId, (FinancialInputRequest) corporatePrimaryViewResponse.getFinancialInputRequest() , gstId , itrId , bsId);
			corporatePrimaryViewResponse.setBankComparisionData(gstVsItrVsBsComparision);
				
			Map<String, Object> gstRelatedPartyDetails = loanApplicationService.getGstRelatedPartyDetails(applicationId);
			corporatePrimaryViewResponse.setGstRelatedParty(gstRelatedPartyDetails);
		}
		return corporatePrimaryViewResponse;
	}

//	STARTS HERE CO-ORIGIN CODE 
//	ENDS HERE CO-ORIGIN CODE 


	@SuppressWarnings("unchecked")
	@Override
	public LinkedHashMap<String,Object> gstVsItrVsBsComparision(Long applicationId,FinancialInputRequest financialInputRequest , Long gstMasterId , Long itrMasterId , Long bsMasterId) {
		LinkedHashMap<String,Object>comparisionData=new LinkedHashMap<>();
		GstResponse gstResp = null;
		Map<String,Object> bsMap=new HashMap<>();
		try {
			GSTR1Request request=new GSTR1Request();
			request.setApplicationId(gstMasterId);
			gstResp = gstClient.getbankComparisonData(request);
			ReportRequest requestReport = new ReportRequest(bsMasterId);
			bsMap  = (LinkedHashMap<String, Object>)analyzerClient.getDetailsByCategoryWise(requestReport).getData();
		}catch (Exception e) {
			logger.error("Exception in getting gst and BS data for teaserview {}",e);
		}
		SimpleDateFormat sdf=new SimpleDateFormat("MMyyyy");
		SimpleDateFormat sdf1=new SimpleDateFormat("MMMyy");
		SimpleDateFormat displayFormate=new SimpleDateFormat("MM-yyyy");
		// gst vs bank statement month wise purchase
		if( gstResp != null && gstResp.getData() != null) {
			LinkedHashMap<String,Object> gstData= (LinkedHashMap<String, Object>) gstResp.getData();
			if(bsMap != null && !bsMap.isEmpty()) {
				
				
				try {
						List<Map<String,Object>>bsData = new ArrayList<>();
						Double totalBsResipts = 0d;
						Double totalGstValue = 0d;
						Double bsValue= 0d;
						for (Map.Entry<String, Object> entry : bsMap.entrySet()) {
							Date parse = sdf1.parse(String.valueOf(entry.getKey()));
							HashMap<String,Object>gstSalesVsBankStatementMonthly = new HashMap<String,Object>();
	
							gstSalesVsBankStatementMonthly.put("month", displayFormate.format(parse));
							gstSalesVsBankStatementMonthly.put("gstValue", " - ");
							gstSalesVsBankStatementMonthly.put("bsDivededBygst", " - ");
							gstSalesVsBankStatementMonthly.put("bsValue", " - ");
							
							for (Map.Entry<String, Object> creditEntry : ((Map<String, Object>) entry.getValue()).entrySet()) {
								
								if(creditEntry != null && creditEntry.getValue() != null && creditEntry.getKey().equals("credit")) {
									 bsValue = Double.valueOf(String.valueOf(creditEntry.getValue()));
									 gstSalesVsBankStatementMonthly.put("bsValue", bsValue != null && bsValue != 0 ? CommonUtils.convertStringFormate(bsValue):"0");
									 totalBsResipts += bsValue;
									if(gstData != null && gstData.get("monthWiseTotalSales") != null) {
										for(Map.Entry<String, Object> monthWiseTotalSales: ((Map<String, Object>) gstData.get("monthWiseTotalSales")).entrySet()) {
											if(monthWiseTotalSales.getKey().equals(sdf.format(parse))) {
												 Double gstValue =Double.valueOf(String.valueOf(monthWiseTotalSales.getValue()));
												 gstSalesVsBankStatementMonthly.put("gstValue", monthWiseTotalSales.getValue() != null && gstValue  != 0 ? CommonUtils.convertValueIndianCurrency(gstValue):" - ");
												 totalGstValue += gstValue;
												 Double bsDividedByGst = gstValue != null && gstValue != 0 ?((bsValue/gstValue)*100):0;
												 gstSalesVsBankStatementMonthly.put("bsDivededBygst",bsDividedByGst != null && bsDividedByGst != 0 ?CommonUtils.convertValueIndianCurrency(bsDividedByGst).toString().concat(" %") : " - ");
											}
										}
									}
								}
							}
							bsData.add(gstSalesVsBankStatementMonthly);
						}
						comparisionData.put("gstVsBsSalesMonthly", !bsData.isEmpty()?bsData:null);
						comparisionData.put("totalGstValueForGstVsBsSalesMonthly", CommonUtils.convertValueIndianCurrency(totalGstValue));
						comparisionData.put("totalBsResiptsForGstVsBsSalesMonthly", CommonUtils.convertValueIndianCurrency(totalBsResipts));
						if(totalBsResipts != null && totalGstValue != 0 ) {
							Double totalGstVsBsTotalValues= totalBsResipts/totalGstValue*100;
							comparisionData.put("totalBsVsGstValueForGstVsBsSalesMonthly", totalGstVsBsTotalValues != null ?convertValue(totalGstVsBsTotalValues).concat(" %") : " - ");
						}else {
							comparisionData.put("totalBsVsGstValueForGstVsBsSalesMonthly", " - ");
						}
				}catch (Exception e) {
					logger.error("exception in getting the details of gstSalesVsBankStatementMonthly {}",e);
				}
			//	for monthly wise purchase gst vs bankstatement
				try {
					Double totalBsResipts = 0d;
					Double totalGstValue = 0d;
					List<LinkedHashMap<String,Object>>bsPurchaseData = new ArrayList<>();
					
					for (Map.Entry<String, Object> bsMapEntry : bsMap.entrySet()) {
						if(bsMapEntry != null) {
							Date parse = sdf1.parse(String.valueOf(bsMapEntry.getKey()));
							
							LinkedHashMap<String,Object>gstPurchaseVsBankStatementMonthly = new LinkedHashMap<>(); 
							
							gstPurchaseVsBankStatementMonthly.put("month", displayFormate.format(parse));
							gstPurchaseVsBankStatementMonthly.put("bsValue", " - ");
							for (Map.Entry<String, Object> debitEntry : ((Map<String, Object>) bsMapEntry.getValue()).entrySet()) {
								 if(debitEntry != null && debitEntry.getKey() != null  && debitEntry.getValue() != null && debitEntry.getKey().equals("debit")) {
									gstPurchaseVsBankStatementMonthly.put("bsDivededBygst", " - ");
									gstPurchaseVsBankStatementMonthly.put("gstValue", " - ");
			
									 Double bsValue = Double.valueOf(debitEntry.getValue().toString());
									 gstPurchaseVsBankStatementMonthly.put("bsValue", bsValue != null && bsValue != 0 ? CommonUtils.convertValueIndianCurrency(bsValue):"0");
									 totalBsResipts +=bsValue;
									 
									for (Map.Entry<String ,Object> entry: ((Map<String, Object>) gstData.get("monthWisePurchase")).entrySet()) {
										 if(bsMapEntry != null && entry.getKey().equals(sdf.format(parse))) {
											 Double gstValue =Double.valueOf(String.valueOf(entry.getValue()));
											gstPurchaseVsBankStatementMonthly.put("gstValue", gstValue != null && gstValue != 0 ? CommonUtils.convertValueIndianCurrency(gstValue):" - " );
											totalGstValue += gstValue;
									
											Double bsDividedByGst = gstValue != null && gstValue != 0 ?((bsValue/gstValue)*100):0;
											gstPurchaseVsBankStatementMonthly.put("bsDivededBygst",bsDividedByGst != 0 ?CommonUtils.convertValueIndianCurrency(bsDividedByGst).toString().concat(" %") : " - ");
										 }
									}
								 }
							 }
							bsPurchaseData.add(gstPurchaseVsBankStatementMonthly);
						}
					}
					comparisionData.put("gstVsBsPurchaseMonthly", !bsPurchaseData.isEmpty()?bsPurchaseData:null);
					comparisionData.put("gstVsBsPurchaseTotalGstValueMonthly", totalGstValue != null && totalGstValue !=0 ?CommonUtils.convertValueIndianCurrency(totalGstValue):" - ");
					comparisionData.put("gstVsBsPurchaseTotalBsResiptsMonthly", totalBsResipts != null && totalBsResipts != 0? CommonUtils.convertValueIndianCurrency(totalBsResipts) :" - ");
					if(totalGstValue != null && totalGstValue != 0) {
						Double total = totalBsResipts/totalGstValue*100;
						comparisionData.put("gstVsBsPurchaseTotalReciptsToSales", total != null && total != 0?CommonUtils.convertValueIndianCurrency(total).toString().concat(" %"):" - ");
					}else {
						comparisionData.put("gstVsBsPurchaseTotalReciptsToSales", " - ");
					}
				}catch (Exception e) {
					logger.error("Exception in getting gst vs bankstatement purchase : {}",e);
				}
				
			}else {
				logger.error("Exception in getting gst and bank statement data");
			}
			if(financialInputRequest != null && !financialInputRequest.getYearSalesPurchasList().isEmpty()) {
				
			// GSTVsITR Yearly Sales 
				try {
					if(gstData != null && gstData.get("yearWiseTotalSales") != null) {
					List<LinkedHashMap<String,Object>>itrSalesData = new ArrayList<>();
					Double totalOfGstSalesTotal = 0d;
					Double totalOfITRSalesTotal = 0d;
					Double totalOfGstToItr = 0d;
					
					for (Map.Entry<String, Object> yearWiseSales: ((Map<String, Object>) gstData.get("yearWiseTotalSales")).entrySet()) {
						if(yearWiseSales != null) {
					
						for (Map<String, Object> itrSales:  financialInputRequest.getYearSalesPurchasList()) {
							if(itrSales != null && !String.valueOf(Integer.valueOf(itrSales.get("year").toString())-1).equals("2017") && String.valueOf(Integer.valueOf(itrSales.get("year").toString())-1).equals(yearWiseSales.getKey().split("-")[0])) {
							
								LinkedHashMap<String,Object>gstPurchaseVsBankStatementMonthly = new LinkedHashMap<>(); 
								gstPurchaseVsBankStatementMonthly.put("year", yearWiseSales.getKey() != null ? yearWiseSales.getKey() : " - ");
								gstPurchaseVsBankStatementMonthly.put("gstSalesTotal",CommonUtils.convertValueIndianCurrency(yearWiseSales.getValue()));
								gstPurchaseVsBankStatementMonthly.put("gstToItr"," - ");
								Double totalOfGst=Double.valueOf(String.valueOf(yearWiseSales.getValue()));
								gstPurchaseVsBankStatementMonthly.put("itrSales", itrSales.get("itrSales") != null && Double.valueOf(String.valueOf(itrSales.get("itrSales"))) != 0 && !itrSales.get("itrSales").equals(" - ")?CommonUtils.convertValueIndianCurrency(itrSales.get("itrSales")):" - ");
								Double gstToItr = 0d;
								if(itrSales.get("itrSales") != null && Double.valueOf(itrSales.get("itrSales").toString()) != 0) {
									gstToItr = totalOfGst/Double.valueOf(itrSales.get("itrSales").toString()) * 100;
								}
								gstPurchaseVsBankStatementMonthly.put("gstToItr",gstToItr != 0?convertValue(gstToItr) + " %":" - ");
								
								// calculating total
								totalOfGstSalesTotal += totalOfGst; 
								totalOfITRSalesTotal += itrSales.get("itrSales") != null && !itrSales.get("itrSales").toString().equals("0") && !itrSales.get("itrSales").toString().equals("-")?Double.valueOf(itrSales.get("itrSales").toString()):0;
		
								if(!gstPurchaseVsBankStatementMonthly.isEmpty()) {
									itrSalesData.add(gstPurchaseVsBankStatementMonthly);
								}
							}
						}
					}
				
					if(totalOfITRSalesTotal != 0) {
						totalOfGstToItr = totalOfGstSalesTotal/totalOfITRSalesTotal*100;
					}
				 }
					comparisionData.put("gstVsItrYearlySales", !itrSalesData.isEmpty()?itrSalesData:null);
					comparisionData.put("gstVsItrYearlySalesTotalOfGstSalesTotal", totalOfGstSalesTotal !=0 ?CommonUtils.convertValueIndianCurrency(totalOfGstSalesTotal): " - ");
					comparisionData.put("gstVsItrYearlySalesTotalOfITRSalesTotal", totalOfITRSalesTotal!=0?CommonUtils.convertValueIndianCurrency(totalOfITRSalesTotal): " - ");
					comparisionData.put("gstVsItrYearlySalesTotalOfGstToItr", totalOfGstToItr!=0?CommonUtils.convertValueIndianCurrency(totalOfGstToItr).toString().concat(" %"): " - ");
				}
				}catch (Exception e) {
					logger.error("Exception in getting value of gstVsItrYearlySales {}",e);
				}
				// GSTVsITR Yearly Purchase 				
				if(gstData != null && gstData.get("yearWisePurchase") != null) {
					try{
						Double totalOfGstPurchase = 0d;
						Double totalOfITRPurchase = 0d;
						Double totalOfGstToItr = 0d;
					List<LinkedHashMap<String,Object>>bsPurchaseData = new ArrayList<>();
					((Map<String, Object>) gstData.get("yearWisePurchase")).entrySet().stream().sorted(new DateComparator2());
					for(Map.Entry<String, Object> yearWisePurchase:((Map<String, Object>) gstData.get("yearWisePurchase")).entrySet()) {
						
						if(financialInputRequest.getYearSalesPurchasList() !=null  && !financialInputRequest.getYearSalesPurchasList().isEmpty()) {
							financialInputRequest.getYearSalesPurchasList().stream().sorted(new DateComparator2());		
							for(Map<String, Object> fi: financialInputRequest.getYearSalesPurchasList()) {
								if(fi != null && !String.valueOf(Integer.valueOf(fi.get("year").toString())-1).equals("2017") 
									  && String.valueOf(Integer.valueOf(fi.get("year").toString())-1).split("-")[0].equals(yearWisePurchase.getKey().split("-")[0])) {
									
									LinkedHashMap<String,Object>gstPurchaseVsBankStatementMonthly = new LinkedHashMap<>();
									gstPurchaseVsBankStatementMonthly.put("year", yearWisePurchase.getKey() != null ?yearWisePurchase.getKey().toString() : " - ");
									gstPurchaseVsBankStatementMonthly.put("gstPurchase", yearWisePurchase.getValue() != null && yearWisePurchase.getValue().toString() != "0" ?CommonUtils.convertStringFormate(yearWisePurchase.getValue()).toString() : " - ");
									totalOfGstPurchase += Double.valueOf(yearWisePurchase.getValue() != null ?  yearWisePurchase.getValue().toString() : "0");
									
									gstPurchaseVsBankStatementMonthly.put("itrPurchase", fi.get("rowMaterialIndigenous") != null && Double.valueOf(fi.get("rowMaterialIndigenous").toString()) != 0?CommonUtils.convertStringFormate(fi.get("rowMaterialIndigenous").toString()):" - ");
									
									totalOfITRPurchase += fi.get("rowMaterialIndigenous") != null ?Double.valueOf(fi.get("rowMaterialIndigenous").toString()):0;
									Double gstToItr = 0d;
									if(fi.get("rowMaterialIndigenous") != null && yearWisePurchase.getValue() != null && Double.valueOf(fi.get("rowMaterialIndigenous").toString()) != 0) {
										gstToItr = Double.valueOf(yearWisePurchase.getValue().toString()) /Double.valueOf(fi.get("rowMaterialIndigenous").toString()) * 100;
									}
									gstPurchaseVsBankStatementMonthly.put("gstToItr",gstToItr != 0? convertValue(gstToItr) + " %":" - ");
									
									if(!gstPurchaseVsBankStatementMonthly.isEmpty()) {
										bsPurchaseData.add(gstPurchaseVsBankStatementMonthly);
									}
								}
							}
							
						}
						
					}
					if(totalOfITRPurchase != 0) {
						totalOfGstToItr = totalOfGstPurchase/totalOfITRPurchase*100;
					}
					comparisionData.put("gstVsItrYearlyPurchase", !bsPurchaseData.isEmpty()?bsPurchaseData:null);
					comparisionData.put("gstVsItrYearlyPurchaseTotalOfGstPurchase", totalOfGstPurchase!= 0?CommonUtils.convertValueIndianCurrency(totalOfGstPurchase): " - ");
					comparisionData.put("gstVsItrYearlyPurchaseTotalOfITRPurchase", totalOfITRPurchase !=0 ?CommonUtils.convertValueIndianCurrency(totalOfITRPurchase): " - ");
					comparisionData.put("gstVsItrYearlyPurchaseTotalOfGstToItr", totalOfGstToItr!=0?CommonUtils.convertValueIndianCurrency(totalOfGstToItr).toString().concat(" %"): " - ");
					}catch (Exception e) {
						logger.error("Exception in getting value of gstVsItrYearlyPurchase {}",e);
					}
				}
			}else {
				logger.error("Response not found in bankstatement  for bank comparision dispalay for :{}",applicationId);
			}
		}
		return comparisionData;
	}

	public String convertValue(Double value) {
		return !CommonUtils.isObjectNullOrEmpty(value) ? decim.format(value) : "0";
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public com.opl.mudra.api.loans.model.LoansResponse getCubictreeReport(Long applicationId) throws LoansException {
		com.opl.mudra.api.loans.model.LoansResponse response=new com.opl.mudra.api.loans.model.LoansResponse();
		try {
			Map<String,Object>reportResp=new HashMap<>();
			
			McaResponse cubictreeReport = mcaClient.getCubictreeReport(applicationId);
			List<Map<String,Object>>listData=((List<Map<String,Object>>)cubictreeReport.getData());
			
			if(cubictreeReport.getStatus().equals(HttpStatus.OK.value()) && cubictreeReport.getData() != null) {
				Map<String,Object> resp= (Map<String, Object>)listData.get(0);
				if(!resp.isEmpty()) {
					response.setData(reportResp);
				}
			}else {
				response.setMessage("Report not found");
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
		}catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("Exception in getting report :"+e);
			throw new LoansException("Exception in getting CubictreeReport",e);
		}
		return response;
	}
	
	public void setAssociateAddress(AssociatedConcernDetailRequest asso) {
		String address = "";
		
		if (!CommonUtils.isObjectListNull(asso.getPremiseNumber())) {
			address = address + asso.getPremiseNumber();
		}
		
		if (!CommonUtils.isObjectListNull(asso.getLandMark())) {
			address = address + ", " + asso.getLandMark();
		}
		
		Long cityId = asso.getCityId() ;
		Integer stateId = asso.getStateId();
		Integer countryId = asso.getCountryId();
		
		if(cityId != null || stateId != null || countryId != null) {
			Map<String ,Object> mapData = commonService.getCityStateCountryNameFromOneForm(cityId, stateId, countryId);
			
			if(mapData != null) {	
				String cityName = mapData.get(CommonUtils.CITY_NAME).toString();
				String stateName = mapData.get(CommonUtils.STATE_NAME).toString();
				String countryName = mapData.get(CommonUtils.COUNTRY_NAME).toString();
				
				if (cityName != null) {					
					address = address + ", " + cityName; 
				}
				if (stateName != null) {					
					address = address + ", " + stateName; 
				}
				if (countryName != null) {					
					address = address + ", " + countryName; 
				}
			}
		}
		asso.setAddress(address);
	}	
	
	@Override
	public List<Long> getAllStorageIds(Long profileId, Long applicationId) {
	
	Long profileMappingId = loanApplicationRepository.getProfileMappingId(applicationId);
	List<Long> comibinedStorageIds = new ArrayList<>();
	com.opl.profile.api.model.ProfileVerMapRequest profileObj = new com.opl.profile.api.model.ProfileVerMapRequest();
	if (!CommonUtils.isObjectNullOrEmpty(profileMappingId)) {
		logger.info("Inside CommonUtils.isObjectNullOrEmpty(profileMappingId)");
		com.opl.profile.api.model.CommonResponse profileRequest = profileClient.getProfileVersionDetailsByPrimaryId(profileMappingId);
		try {
			profileObj = MultipleJSONObjectHelper.getObjectFromMap(((Map)profileRequest.getData()), ProfileVerMapRequest.class) ;
			
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setBsMasterId(profileObj.getBsId());
			AnalyzerResponse bsStoarageIdsanalyzerResponse = analyzerClient.getBsStorageIds(reportRequest);
			List<HashMap<Object, Object>> bsStorageIdsMap = (List<HashMap<Object, Object>>) bsStoarageIdsanalyzerResponse.getData();
			List<Long> idsList = new ArrayList<>(); 
			if (!CommonUtils.isListNullOrEmpty(bsStorageIdsMap)) {
				String[] finalBsIds = null;
				for (Object bsIds : bsStorageIdsMap) {
					String ids = bsIds.toString().replaceAll(" ", "");
					String[] idsArray = ids.split(",");
					finalBsIds = idsArray;
					for (String id :  finalBsIds) { 
			            // Add each Storage ID of bs into the list 
						idsList.add(Long.parseLong(id)); 
			        }
			}
			}
			comibinedStorageIds.addAll(idsList);
			
			//CIBIL MASTER ID
			try {
			List<Long> proMapIds=new ArrayList<Long>();
			proMapIds.add(DocumentAlias.CORPORATE_ITR_PDF);
			List<BigInteger> itrStorageIds =  loanApplicationRepository.getItrStorageIds(profileObj.getItrId());
			if(itrStorageIds.contains(null) || itrStorageIds.contains("")) {
				comibinedStorageIds.addAll(Collections.EMPTY_LIST);
			}else {
				List<Long> newLongItrIds = new ArrayList<>(itrStorageIds.size());
				for (BigInteger bigInteger : itrStorageIds) {
					 newLongItrIds.add(bigInteger.longValue());
				}
				comibinedStorageIds.addAll(newLongItrIds);
			}
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}	
			
			DocumentRequest documentRequest = new DocumentRequest();
			documentRequest.setApplicationId(applicationId);
			documentRequest.setProfileId(profileObj.getProfileId());
			try {
				documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
				documentRequest.setProductDocumentMappingId(DocumentAlias.CIBIL_REPORT_MSME_COMPANY);
				DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
				List<HashMap<String, Object>> hashMap = (List<HashMap<String, Object>>) documentResponse.getDataList();
				for (HashMap<String, Object> rec : hashMap) {
					DocumentResponse data;
					try {
						data = MultipleJSONObjectHelper.getObjectFromMap(rec, DocumentResponse.class);
						System.out.println(data);
						comibinedStorageIds.add(data.getId().longValue());
					} catch (IOException e) {
						logger.error("Error While adding cibil storage idto combined storage id list");
					}
				}
			} catch (DocumentException e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}

			try {
				documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
				documentRequest.setProductDocumentMappingId(DocumentAlias.CIBIL_REPORT_HUF_COMMERCIAL);
				DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
				List<HashMap<String, Object>> hashMap = (List<HashMap<String, Object>>) documentResponse.getDataList();
				for (HashMap<String, Object> rec : hashMap) {
					DocumentResponse data;
					try {
						data = MultipleJSONObjectHelper.getObjectFromMap(rec, DocumentResponse.class);
						comibinedStorageIds.add(data.getId().longValue());
					} catch (IOException e) {
						logger.error("Error While adding cibil storage idto combined storage id list");
					}
				}
			} catch (DocumentException e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			
			try {
				documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
				documentRequest.setProductDocumentMappingId(DocumentAlias.CIBIL_REPORT_MSME_CONSUMER);
				DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
				List<HashMap<String, Object>> hashMap = (List<HashMap<String, Object>>) documentResponse.getDataList();
				for (HashMap<String, Object> rec : hashMap) {
					DocumentResponse data;
					try {
						data = MultipleJSONObjectHelper.getObjectFromMap(rec, DocumentResponse.class);
						comibinedStorageIds.add(data.getId().longValue());
					} catch (IOException e) {
						logger.error("Error While adding cibil storage idto combined storage id list");
					}
				}
			} catch (DocumentException e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			
			
			
		} catch (Exception e) {
			logger.error("Error while Getting Storage Ids ::",e);
		}

		}
		
		return  comibinedStorageIds;
	}
}
