package com.opl.service.loans.service.teaser.finalview.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.opl.mudra.api.dms.exception.DocumentException;
import com.opl.mudra.api.dms.utils.DocumentAlias;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.CreditRatingOrganizationDetailRequest;
import com.opl.mudra.api.loans.model.CreditRatingOrganizationDetailResponse;
import com.opl.mudra.api.loans.model.FinanceMeansDetailRequest;
import com.opl.mudra.api.loans.model.FinanceMeansDetailResponse;
import com.opl.mudra.api.loans.model.FinancialArrangementsDetailRequest;
import com.opl.mudra.api.loans.model.FinancialArrangementsDetailResponse;
import com.opl.mudra.api.loans.model.GuarantorsCorporateDetailRequest;
import com.opl.mudra.api.loans.model.OwnershipDetailRequest;
import com.opl.mudra.api.loans.model.OwnershipDetailResponse;
import com.opl.mudra.api.loans.model.PromotorBackgroundDetailRequest;
import com.opl.mudra.api.loans.model.PromotorBackgroundDetailResponse;
import com.opl.mudra.api.loans.model.TotalCostOfProjectResponse;
import com.opl.mudra.api.loans.model.corporate.CorporateCoApplicantRequest;
import com.opl.mudra.api.loans.model.corporate.FinalUnsecuredLoanRequest;
import com.opl.mudra.api.loans.model.corporate.TotalCostOfProjectRequest;
import com.opl.mudra.api.loans.model.retail.CreditCardsDetailRequest;
import com.opl.mudra.api.loans.model.retail.CreditCardsDetailResponse;
import com.opl.mudra.api.loans.model.retail.ReferenceRetailDetailsRequest;
import com.opl.mudra.api.loans.model.teaser.finalview.GuarantorsCorporateDetailResponse;
import com.opl.mudra.api.loans.model.teaser.finalview.UnsecuredLoanFinalViewResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.oneform.enums.AbilityRaiseFunds;
import com.opl.mudra.api.oneform.enums.AccountingQuality;
import com.opl.mudra.api.oneform.enums.AccountingSystems;
import com.opl.mudra.api.oneform.enums.BorrowerInvoked;
import com.opl.mudra.api.oneform.enums.BrandAmbassador;
import com.opl.mudra.api.oneform.enums.BusinessCommitment;
import com.opl.mudra.api.oneform.enums.BusinessExperience;
import com.opl.mudra.api.oneform.enums.ChequesReturned;
import com.opl.mudra.api.oneform.enums.CompanyConflicts;
import com.opl.mudra.api.oneform.enums.Competence;
import com.opl.mudra.api.oneform.enums.ComplianceConditions;
import com.opl.mudra.api.oneform.enums.Constitution;
import com.opl.mudra.api.oneform.enums.ConstructionContract;
import com.opl.mudra.api.oneform.enums.CreditRatingFund;
import com.opl.mudra.api.oneform.enums.CreditRatingTerm;
import com.opl.mudra.api.oneform.enums.CreditRecord;
import com.opl.mudra.api.oneform.enums.CumulativeOverdrawn;
import com.opl.mudra.api.oneform.enums.Currency;
import com.opl.mudra.api.oneform.enums.CustomerQuality;
import com.opl.mudra.api.oneform.enums.DelayInstalments;
import com.opl.mudra.api.oneform.enums.DelaySubmission;
import com.opl.mudra.api.oneform.enums.Denomination;
import com.opl.mudra.api.oneform.enums.DistributionMarketingTieUps;
import com.opl.mudra.api.oneform.enums.EnvironmentCertification;
import com.opl.mudra.api.oneform.enums.EnvironmentalImpact;
import com.opl.mudra.api.oneform.enums.EstablishmentMonths;
import com.opl.mudra.api.oneform.enums.ExistingShareholders;
import com.opl.mudra.api.oneform.enums.FinanceCategory;
import com.opl.mudra.api.oneform.enums.FinancialRestructuring;
import com.opl.mudra.api.oneform.enums.FinancialSupport;
import com.opl.mudra.api.oneform.enums.IndiaDistributionNetwork;
import com.opl.mudra.api.oneform.enums.IndustrialRelations;
import com.opl.mudra.api.oneform.enums.InfrastructureAvailability;
import com.opl.mudra.api.oneform.enums.Integrity;
import com.opl.mudra.api.oneform.enums.InternalAudit;
import com.opl.mudra.api.oneform.enums.InternalControl;
import com.opl.mudra.api.oneform.enums.InternalReturn;
import com.opl.mudra.api.oneform.enums.LimitOverdrawn;
import com.opl.mudra.api.oneform.enums.LoanType;
import com.opl.mudra.api.oneform.enums.ManagementCompetence;
import com.opl.mudra.api.oneform.enums.MarketPosition;
import com.opl.mudra.api.oneform.enums.MarketPositioningTop;
import com.opl.mudra.api.oneform.enums.MarketShareTurnover;
import com.opl.mudra.api.oneform.enums.MarketingPositioningNew;
import com.opl.mudra.api.oneform.enums.OperatingMargins;
import com.opl.mudra.api.oneform.enums.OrderBook;
import com.opl.mudra.api.oneform.enums.OverseasNetwork;
import com.opl.mudra.api.oneform.enums.Particular;
import com.opl.mudra.api.oneform.enums.ProductSeasonality;
import com.opl.mudra.api.oneform.enums.ProductServicesPerse;
import com.opl.mudra.api.oneform.enums.ProjectedRatio;
import com.opl.mudra.api.oneform.enums.RatingAgency;
import com.opl.mudra.api.oneform.enums.SensititivityAnalysis;
import com.opl.mudra.api.oneform.enums.ShareHoldingCategory;
import com.opl.mudra.api.oneform.enums.StatusClearances;
import com.opl.mudra.api.oneform.enums.StatusFinancialClosure;
import com.opl.mudra.api.oneform.enums.SubmissionReports;
import com.opl.mudra.api.oneform.enums.SuccessionPlanning;
import com.opl.mudra.api.oneform.enums.SupplierQuality;
import com.opl.mudra.api.oneform.enums.SustainabilityProduct;
import com.opl.mudra.api.oneform.enums.TechnologyPatented;
import com.opl.mudra.api.oneform.enums.TechnologyRequiresUpgradation;
import com.opl.mudra.api.oneform.enums.TechnologyRisk;
import com.opl.mudra.api.oneform.enums.Title;
import com.opl.mudra.api.oneform.enums.TypeTechnology;
import com.opl.mudra.api.oneform.enums.UnhedgedCurrency;
import com.opl.mudra.api.oneform.enums.VarianceSales;
import com.opl.mudra.api.oneform.model.IndustrySectorSubSectorTeaserRequest;
import com.opl.mudra.api.oneform.model.MasterResponse;
import com.opl.mudra.api.oneform.model.OneFormResponse;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.oneform.OneFormClient;
import com.opl.mudra.client.users.UsersClient;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateCoApplicantDetail;
import com.opl.service.loans.domain.fundseeker.corporate.PrimaryUnsecuredLoanDetail;
import com.opl.service.loans.repository.fundseeker.corporate.AvailabilityProposedPlantDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.BoardOfDirectorsDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.CapacityDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateCoApplicantRepository;
import com.opl.service.loans.repository.fundseeker.corporate.DprUserDataDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.DriverForFutureGrowthDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.EmployeesCategoryBreaksDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.opl.service.loans.repository.fundseeker.corporate.KeyManagementDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryUnsecuredLoanDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryWorkingCapitalLoanDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.ProjectImplementationScheduleDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.RequirementsAndAvailabilityRawMaterialsDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.RevenueAndOrderBookDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.ScotAnalysisDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.StrategicAlliancesDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.SubSectorRepository;
import com.opl.service.loans.repository.fundseeker.corporate.TechnologyPositioningDetailRepository;
import com.opl.service.loans.service.common.CommonService;
import com.opl.service.loans.service.common.DocumentManagementService;
import com.opl.service.loans.service.fundseeker.corporate.AchievmentDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.AssociatedConcernDetailService;
import com.opl.service.loans.service.fundseeker.corporate.CorporateCoApplicantService;
import com.opl.service.loans.service.fundseeker.corporate.CreditRatingOrganizationDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.ExistingProductDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FinalUnsecuredLoanService;
import com.opl.service.loans.service.fundseeker.corporate.FinanceMeansDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FutureFinancialEstimatesDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.GuarantorsCorporateDetailService;
import com.opl.service.loans.service.fundseeker.corporate.MonthlyTurnoverDetailService;
import com.opl.service.loans.service.fundseeker.corporate.OwnershipDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.PastFinancialEstiamateDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.PromotorBackgroundDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.ProposedProductDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.SecurityCorporateDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.TotalCostOfProjectService;
import com.opl.service.loans.service.teaser.finalview.UnsecuredLoanFinalViewService;

@Service
public class UnsecuredLoanFinalViewServiceImpl implements UnsecuredLoanFinalViewService{
	@Autowired
	private TotalCostOfProjectService costOfProjectService;

	@Autowired
	private FinanceMeansDetailsService financeMeansDetailsService;

	@Autowired
	private PrimaryUnsecuredLoanDetailRepository primaryUnsecuredLoanDetailRepository;

	@Autowired
	private BoardOfDirectorsDetailRepository boardOfDirectorsDetailRepository;

	@Autowired
	private StrategicAlliancesDetailRepository strategicAlliancesDetailRepository;

	@Autowired
	private KeyManagementDetailRepository keyManagementDetailRepository;

	@Autowired
	private EmployeesCategoryBreaksDetailRepository employeesCategoryBreaksDetailRepository;

	@Autowired
	private TechnologyPositioningDetailRepository technologyPositioningDetailRepository;

	@Autowired
	private RevenueAndOrderBookDetailRepository revenueAndOrderBookDetailRepository;

	@Autowired
	private CapacityDetailRepository capacityDetailRepository;

	@Autowired
	private AvailabilityProposedPlantDetailRepository availabilityProposedPlantDetailRepository;

	@Autowired
	private RequirementsAndAvailabilityRawMaterialsDetailRepository requirementsAndAvailabilityRawMaterialsDetailRepository;

	@Autowired
	private ScotAnalysisDetailRepository scotAnalysisDetailRepository;

	@Autowired
	private DprUserDataDetailRepository dprUserDataDetailRepository;

	@Autowired
	private DocumentManagementService documentManagementService;

	@Autowired
	private DriverForFutureGrowthDetailRepository driverForFutureGrowthDetailRepository;

	@Autowired
	private ProjectImplementationScheduleDetailRepository projectImplementationScheduleDetailRepository;

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;

	@Autowired
	private PrimaryWorkingCapitalLoanDetailRepository primaryWorkingCapitalLoanDetailRepository;

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
	private PastFinancialEstiamateDetailsService pastFinancialEstiamateDetailsService;

	@Autowired
	private FutureFinancialEstimatesDetailsService futureFinancialEstimatesDetailsService;

	@Autowired
	private ExistingProductDetailsService existingProductDetailsService;

	@Autowired
	private SecurityCorporateDetailsService securityCorporateDetailsService;

	@Autowired
	private FinancialArrangementDetailsService financialArrangementDetailsService;

	@Autowired
	private Environment environment;

	@Autowired
	private IndustrySectorRepository industrySectorRepository;

	@Autowired
	private SubSectorRepository subSectorRepository;

	@Autowired
	private AssociatedConcernDetailService associatedConcernDetailService;

	@Autowired
	private GuarantorsCorporateDetailService guarantorsCorporateDetailService;

	@Autowired
	private MonthlyTurnoverDetailService monthlyTurnoverDetailService;

	@Autowired
	private FinalUnsecuredLoanService finalUnsecuredLoanService;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private UsersClient usersClient;
	
	@Autowired
	private CorporateCoApplicantService corporateCoApplicantService;
	
	@Autowired
	private CorporateCoApplicantRepository corporateCoApplicantRepository;
	
	@Autowired
	private CommonService commonService;

	protected static final String ONE_FORM_URL = "oneForm";
	protected static final String USERS_URL = "userURL";
	protected static final String MATCHES_URL = "matchesURL";

	private static final Logger logger = LoggerFactory.getLogger(UnsecuredLoanFinalViewServiceImpl.class);
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public UnsecuredLoanFinalViewResponse getUnsecuredLoanFinalViewDetails(Long toApplicationId, Integer userType,
			Long fundProviderUserId) throws LoansException {
		LoanApplicationMaster applicationMaster = loanApplicationRepository.findOne(toApplicationId);
		Long userId = applicationMaster.getUserId();

		// create response object
		UnsecuredLoanFinalViewResponse response = new UnsecuredLoanFinalViewResponse();

		// getting data of uploads documents and getting profile picture
		try {
			response.setProfilePic(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_PROFIEL_PICTURE));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		/*
		 * FINAL UPLOADS
		 * */
		/* FINANCIAL */
		try{
			response.setLastAuditedAnnualReportList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_LAST_AUDITED_ANNUAL_REPORT));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setSanctionLetterCopyList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_SANCTION_LETTER_COPY));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setLastITReturnList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_LAST_IT_RETURN));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setBankStatementList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_BANK_STATEMENT));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setNetWorthStatementOfdirectorsList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_NET_WORTH_STATEMENT_OF_DIRECTORS));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setProvisionalFinancialsList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_PROVISIONAL_FINANCIALS));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setBrochureList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.UNSECURED_LOAN_BROCHURE_OF_PROPOSED_ACTIVITIES)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		try{
			response.setCmaList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_CMA)));
		}catch(DocumentException e){
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setBsFormatList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_COMPANY_ACT)));
		}catch(DocumentException e){
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setItReturnForFYOfAllDirectorsList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.UNSECURED_LOAN_IT_RETURN_DIRECTOR)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setFinSubsidiariesEntitiesList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.UNSECURED_LOAN_FINANCIALS_OF_SUBSIDIARIES)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setAssessOrderForLastThreeYearsList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.UNSECURED_LOAN_ASSESSMENT_ORDERS)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		/* KYC UPLOADS */
		try {
			response.setCertificateOfIncorpList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.UNSECURED_LOAN_CERTIFICATE_OF_INCORPORATION)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setDetailedListOfShareholdersList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_DETAILED_LIST_OF_SHAREHOLDERS));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setPanCardList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.UNSECURED_LOAN_COPY_OF_PAN_CARD)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setPhotoOfDirectorsList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, DocumentAlias.UNSECURED_LOAN_PHOTO_OF_DIRECTORS));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setPanOfDirectorsList(documentManagementService.getDocumentDetails(toApplicationId, DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.UNSECURED_LOAN_PAN_OF_DIRECTORS_CERTIFICATE_OF_INCORPORATION));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setResidenceAddProofList(documentManagementService.getDocumentDetails(toApplicationId, DocumentAlias.UERT_TYPE_APPLICANT,DocumentAlias.UNSECURED_LOAN_DIRECTOR_ADDRESS));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setResolutionForAdditionOfDirectorsList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.UNSECURED_LOAN_DIRECTOR_RESOLUTION)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		/* OTHERS UPLOADS */
		try {
			response.setMomAndAOAList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_MOM_AOA)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setDebtorsList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_DEBTORS_LIST)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setGstVATExciseList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_GST_APPLIED)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setLetterOfIntentFromFPList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_LETTER_OF_INTENT)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setCopiesOfRelevantLicenseList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_RELEVANT_LICENSE)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setSalesTaxReturnsList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_SALES_TAX)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setLatestTaxPaidCoyList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_LATEST_TAX)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setEncumbranceList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_ENCUMBRANCE)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setCopiesOfTrustDeedList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_COPIES_TRUST_DEEDS)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setMarketSurveyReportList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_MARKET_SURVEY_REPORT)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try {
			response.setDetailsOfContLiabilitiesList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_CONTINGENT_LIABILITIES)));
		} catch (DocumentException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		/*
		* CMA BSFormat
		* */
		try{
			response.setCmaList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_CMA)));
		}catch(DocumentException e){
			logger.error(CommonUtils.EXCEPTION,e);
		}
		try{
			response.setBsFormatList(documentManagementService.getDocumentDetails(toApplicationId,DocumentAlias.UERT_TYPE_APPLICANT, Long.valueOf(DocumentAlias.USL_COMPANY_ACT)));
		}catch(DocumentException e){
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		List<CorporateCoApplicantDetail> coApplicantDetails = corporateCoApplicantRepository.getList(toApplicationId, userId);
		//setting co-application details

        	response.setCoApplicantList(new ArrayList<CorporateCoApplicantRequest>());
		if (coApplicantDetails != null && !coApplicantDetails.isEmpty()) {
			for (CorporateCoApplicantDetail coApplicantDetail : coApplicantDetails) {

				CorporateCoApplicantRequest coApplicantRequest = new CorporateCoApplicantRequest();
				coApplicantDetail.setPanNo(coApplicantDetail.getPanNo() != null ? coApplicantDetail.getPanNo().toUpperCase() : null);
				BeanUtils.copyProperties(coApplicantDetail, coApplicantRequest);
			
				try {
					coApplicantRequest.setCoApplicant_BankACStatments(documentManagementService.getDocumentDetails(
							coApplicantDetail.getId(), DocumentAlias.UERT_TYPE_CO_APPLICANT,
							DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_STATEMENT_OF_BANK_ACCOUNT_FOR_LAST_6_MONTHS));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					coApplicantRequest.setCoApplicant_ItReturn(documentManagementService.getDocumentDetails(coApplicantDetail.getId(),DocumentAlias.UERT_TYPE_CO_APPLICANT, DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_ITR));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					coApplicantRequest.setCoApplicant_Form_16(documentManagementService.getDocumentDetails(coApplicantDetail.getId(),DocumentAlias.UERT_TYPE_CO_APPLICANT, DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_FORM_16));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					coApplicantRequest.setCoApplicant_BalanceSheet(documentManagementService.getDocumentDetails(coApplicantDetail.getId(),DocumentAlias.UERT_TYPE_CO_APPLICANT, DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_AUDITEDUNAUDITED_BALANCE_SHEET_PROFIT_LOSS_STATEMENT_FOR_3_YEARS));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					coApplicantRequest.setCoApplicant_AddressProof(documentManagementService.getDocumentDetails(coApplicantDetail.getId(),DocumentAlias.UERT_TYPE_CO_APPLICANT, DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_ADDRESS_PROOF_ELECTRICITY_BILL_ADHAR_CARD_VOTER_ID_CARD_ANY_1));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					coApplicantRequest.setCoApplicant_aadharCardList(documentManagementService.getDocumentDetails(coApplicantDetail.getId(),DocumentAlias.UERT_TYPE_CO_APPLICANT, DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_SCANNED_COPY_OF_AADHAR_CARD));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				try {
					coApplicantRequest.setCoApplicant_panCardList(documentManagementService.getDocumentDetails(coApplicantDetail.getId(),DocumentAlias.UERT_TYPE_CO_APPLICANT, DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_SCANNED_COPY_OF_PAN_CARD));
				} catch (DocumentException e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				response.addCoApplicantList(coApplicantRequest);
			}
		}
		//CO-APPLICANT
		

		// set final working capital information
		try {
			FinalUnsecuredLoanRequest finalUnsecuredLoanRequest = finalUnsecuredLoanService.get(userId, toApplicationId);

			response.setTechnologyType(finalUnsecuredLoanRequest.getTechnologyTypeId() != null ? TypeTechnology.getById(finalUnsecuredLoanRequest.getTechnologyTypeId()).getValue() : null);
			response.setTechnologyPatented(finalUnsecuredLoanRequest.getTechnologyPatentedId() != null ? TechnologyPatented.getById(finalUnsecuredLoanRequest.getTechnologyPatentedId()).getValue() : null);
			response.setTechnologyRequiresUpgradation(finalUnsecuredLoanRequest.getTechnologyRequiresUpgradationId() != null ? TechnologyRequiresUpgradation.getById(finalUnsecuredLoanRequest.getTechnologyRequiresUpgradationId()).getValue() : null);
			response.setMarketPosition(finalUnsecuredLoanRequest.getMarketPositionId() != null ? MarketPosition.getById(finalUnsecuredLoanRequest.getMarketPositionId()).getValue() : null);
			response.setMarketingPositioning(finalUnsecuredLoanRequest.getMarketingPositioningId() != null ? MarketingPositioningNew.getById(finalUnsecuredLoanRequest.getMarketingPositioningId()).getValue() : null);
			response.setMarketPositioningTop(finalUnsecuredLoanRequest.getMarketPositioningTopId() != null ? MarketPositioningTop.getById(finalUnsecuredLoanRequest.getMarketPositioningTopId()).getValue() : null);
			response.setMarketShareTurnover(finalUnsecuredLoanRequest.getMarketShareTurnoverId() != null ? MarketShareTurnover.getById(finalUnsecuredLoanRequest.getMarketShareTurnoverId()).getValue() : null);
			response.setIndiaDistributionNetwork(finalUnsecuredLoanRequest.getIndiaDistributionNetworkId() != null ? IndiaDistributionNetwork.getById(finalUnsecuredLoanRequest.getIndiaDistributionNetworkId()).getValue() : null);
			response.setDistributionAndTieUps(finalUnsecuredLoanRequest.getDistributionAndMarketingTieUpsId() != null ? DistributionMarketingTieUps.getById(finalUnsecuredLoanRequest.getDistributionAndMarketingTieUpsId()).getValue() : null);
			response.setBrandAmbassador(finalUnsecuredLoanRequest.getBrandAmbassadorId() != null ? BrandAmbassador.getById(finalUnsecuredLoanRequest.getBrandAmbassadorId()).getValue() : null);
			response.setProductServicesPerse(finalUnsecuredLoanRequest.getProductServicesPerseId() != null ? ProductServicesPerse.getById(finalUnsecuredLoanRequest.getProductServicesPerseId()).getValue() : null);
			response.setEnvironmentCertification(finalUnsecuredLoanRequest.getEnvironmentCertificationId() != null ? EnvironmentCertification.getById(finalUnsecuredLoanRequest.getEnvironmentCertificationId()).getValue() : null);
			response.setAccountingSystems(finalUnsecuredLoanRequest.getAccountingSystemsId() != null ? AccountingSystems.getById(finalUnsecuredLoanRequest.getAccountingSystemsId()).getValue() : null);
			response.setInternalAudit(finalUnsecuredLoanRequest.getInternalAuditId() != null ? InternalAudit.getById(finalUnsecuredLoanRequest.getInternalAuditId()).getValue() : null);
			response.setCompetence(finalUnsecuredLoanRequest.getCompetenceId() != null ? Competence.getById(finalUnsecuredLoanRequest.getCompetenceId()).getValue() : null);
			response.setExistingShareHolder(finalUnsecuredLoanRequest.getExistingShareHoldersId() != null ? ExistingShareholders.getById(finalUnsecuredLoanRequest.getExistingShareHoldersId()).getValue() : null);
			
			//NEW MCQ FOR NHBS
			response.setTechnologyRiskId(finalUnsecuredLoanRequest.getTechnologyRiskId() != null ? TechnologyRisk.getById(finalUnsecuredLoanRequest.getTechnologyRiskId()).getValue() : null);
			response.setCustomerQuality(finalUnsecuredLoanRequest.getCustomerQuality() != null ? CustomerQuality.getById(finalUnsecuredLoanRequest.getCustomerQuality()).getValue() : null);
			response.setSupplierQuality(finalUnsecuredLoanRequest.getSupplierQuality() != null ? SupplierQuality.getById(finalUnsecuredLoanRequest.getSupplierQuality()).getValue() : null);
			response.setSustainabilityProduct(finalUnsecuredLoanRequest.getSustainabilityProduct() != null ? SustainabilityProduct.getById(finalUnsecuredLoanRequest.getSustainabilityProduct()).getValue() : null);
			response.setEmployeeRelations(finalUnsecuredLoanRequest.getEmployeeRelations() != null ? IndustrialRelations.getById(finalUnsecuredLoanRequest.getEmployeeRelations()).getValue() : null);
			response.setProductSeasonality(finalUnsecuredLoanRequest.getProductSeasonality() != null ? ProductSeasonality.getById(finalUnsecuredLoanRequest.getProductSeasonality()).getValue() : null);
			response.setImpactOnOperatingMargins(finalUnsecuredLoanRequest.getImpactOnOperatingMargins() != null ? OperatingMargins.getById(finalUnsecuredLoanRequest.getImpactOnOperatingMargins()).getValue() : null);
			response.setOrderBookPosition(finalUnsecuredLoanRequest.getOrderBookPosition() != null ? OrderBook.getById(finalUnsecuredLoanRequest.getOrderBookPosition()).getValue() : null);
			response.setEnvironmentalImpact(finalUnsecuredLoanRequest.getEnvironmentalImpact() != null ? EnvironmentalImpact.getById(finalUnsecuredLoanRequest.getEnvironmentalImpact()).getValue() : null);
			response.setAccountingQuality(finalUnsecuredLoanRequest.getAccountingQuality() != null ? AccountingQuality.getById(finalUnsecuredLoanRequest.getAccountingQuality()).getValue() : null);
			response.setFinancialRestructuringHistory(finalUnsecuredLoanRequest.getFinancialRestructuringHistory() != null ? FinancialRestructuring.getById(finalUnsecuredLoanRequest.getFinancialRestructuringHistory()).getValue() : null);
			response.setIntegrity(finalUnsecuredLoanRequest.getIntegrity() != null ? Integrity.getById(finalUnsecuredLoanRequest.getIntegrity()).getValue() : null);
			response.setBusinessCommitment(finalUnsecuredLoanRequest.getBusinessCommitment() != null ? BusinessCommitment.getById(finalUnsecuredLoanRequest.getBusinessCommitment()).getValue() : null);
			response.setManagementCompetence(finalUnsecuredLoanRequest.getManagementCompetence() != null ? ManagementCompetence.getById(finalUnsecuredLoanRequest.getManagementCompetence()).getValue() : null);
			response.setBusinessExperience(finalUnsecuredLoanRequest.getBusinessExperience() != null ? BusinessExperience.getById(finalUnsecuredLoanRequest.getBusinessExperience()).getValue() : null);
			response.setSuccessionPlanning(finalUnsecuredLoanRequest.getSuccessionPlanning() != null ? SuccessionPlanning.getById(finalUnsecuredLoanRequest.getSuccessionPlanning()).getValue() : null);
			response.setFinancialStrength(finalUnsecuredLoanRequest.getFinancialStrength() != null ? FinancialSupport.getById(finalUnsecuredLoanRequest.getFinancialStrength()).getValue() : null);
			response.setAbilityToRaiseFunds(finalUnsecuredLoanRequest.getAbilityToRaiseFunds() != null ? AbilityRaiseFunds.getById(finalUnsecuredLoanRequest.getAbilityToRaiseFunds()).getValue() : null);
			response.setIntraCompany(finalUnsecuredLoanRequest.getIntraCompany() != null ? CompanyConflicts.getById(finalUnsecuredLoanRequest.getIntraCompany()).getValue() : null);
			response.setInternalControl(finalUnsecuredLoanRequest.getInternalControl() != null ? InternalControl.getById(finalUnsecuredLoanRequest.getInternalControl()).getValue() : null);
			response.setCreditTrackRecord(finalUnsecuredLoanRequest.getCreditTrackRecord() != null ? CreditRecord.getById(finalUnsecuredLoanRequest.getCreditTrackRecord()).getValue() : null);
			response.setStatusOfProjectClearances(finalUnsecuredLoanRequest.getStatusOfProjectClearances() != null ? StatusClearances.getById(finalUnsecuredLoanRequest.getStatusOfProjectClearances()).getValue() : null);
			response.setStatusOfFinancialClosure(finalUnsecuredLoanRequest.getStatusOfFinancialClosure() != null ? StatusFinancialClosure.getById(finalUnsecuredLoanRequest.getStatusOfFinancialClosure()).getValue() : null);
			response.setInfrastructureAvailability(finalUnsecuredLoanRequest.getInfrastructureAvailability() != null ? InfrastructureAvailability.getById(finalUnsecuredLoanRequest.getInfrastructureAvailability()).getValue() : null);
			response.setConstructionContract(finalUnsecuredLoanRequest.getConstructionContract() != null ? ConstructionContract.getById(finalUnsecuredLoanRequest.getConstructionContract()).getValue() : null);
			response.setNumberOfCheques(finalUnsecuredLoanRequest.getNumberOfCheques() != null ? ChequesReturned.getById(finalUnsecuredLoanRequest.getNumberOfCheques()).getValue() : null);
			response.setNumberOfTimesDp(finalUnsecuredLoanRequest.getNumberOfTimesDp() != null ? LimitOverdrawn.getById(finalUnsecuredLoanRequest.getNumberOfTimesDp()).getValue() : null);
			response.setCumulativeNoOfDaysDp(finalUnsecuredLoanRequest.getCumulativeNoOfDaysDp() != null ? CumulativeOverdrawn.getById(finalUnsecuredLoanRequest.getCumulativeNoOfDaysDp()).getValue() : null);
			response.setComplianceWithSanctioned(finalUnsecuredLoanRequest.getComplianceWithSanctioned() != null ? ComplianceConditions.getById(finalUnsecuredLoanRequest.getComplianceWithSanctioned()).getValue() : null);
			response.setProgressReports(finalUnsecuredLoanRequest.getProgressReports() != null ? SubmissionReports.getById(finalUnsecuredLoanRequest.getProgressReports()).getValue() : null);
			response.setDelayInReceipt(finalUnsecuredLoanRequest.getDelayInReceipt() != null ? DelayInstalments.getById(finalUnsecuredLoanRequest.getDelayInReceipt()).getValue() : null);
			response.setDelayInSubmission(finalUnsecuredLoanRequest.getDelayInSubmission() != null ? DelaySubmission.getById(finalUnsecuredLoanRequest.getDelayInSubmission()).getValue() : null);
			response.setNumberOfLc(finalUnsecuredLoanRequest.getNumberOfLc() != null ? BorrowerInvoked.getById(finalUnsecuredLoanRequest.getNumberOfLc()).getValue() : null);
			response.setUnhedgedForeignCurrency(finalUnsecuredLoanRequest.getUnhedgedForeignCurrency() != null ? UnhedgedCurrency.getById(finalUnsecuredLoanRequest.getUnhedgedForeignCurrency()).getValue() : null);
			response.setProjectedDebtService(finalUnsecuredLoanRequest.getProjectedDebtService() != null ? ProjectedRatio.getById(finalUnsecuredLoanRequest.getProjectedDebtService()).getValue() : null);
			response.setInternalRateReturn(finalUnsecuredLoanRequest.getInternalRateReturn() != null ? InternalReturn.getById(finalUnsecuredLoanRequest.getInternalRateReturn()).getValue() : null);
			response.setSensititivityAnalysis(finalUnsecuredLoanRequest.getSensititivityAnalysis() != null ? SensititivityAnalysis.getById(finalUnsecuredLoanRequest.getSensititivityAnalysis()).getValue() : null);
			response.setVarianceInProjectedSales(finalUnsecuredLoanRequest.getVarianceInProjectedSales() != null ? VarianceSales.getById(finalUnsecuredLoanRequest.getVarianceInProjectedSales()).getValue() : null);

			if (finalUnsecuredLoanRequest.getIsDependsMajorlyOnGovernment()) {
				response.setMajorlyOnGovernment("Yes");
			} else {
				response.setMajorlyOnGovernment("No");
			}

			if (finalUnsecuredLoanRequest.getIsIsoCertified()) {
				response.setIsIsoCertified("Yes");
			} else {
				response.setIsIsoCertified("No");
			}
			if (finalUnsecuredLoanRequest.isWhetherTechnologyIsTied()) {
				response.setWhetherTechnologyIsTied("Yes");
			} else {
				response.setWhetherTechnologyIsTied("No");
			}
			// set overseas
			List<Integer> overseasIds = finalUnsecuredLoanRequest.getOverseasNetworkIds();
			String overseasString = "";
			for (int id : overseasIds) {
				overseasString += OverseasNetwork.getById(id).getValue() + ",";
			}
			response.setOverseasNetwork(overseasString);

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		// set registered email address and registered contact number
		UserResponse userResponse = usersClient.getEmailMobile(userId);
		if (!CommonUtils.isObjectNullOrEmpty(userResponse)) {
			try {
				UsersRequest usersRequest = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if (usersRequest != null) {
					response.setRegisteredEmailAddress(usersRequest.getEmail());
					response.setRegisteredContactNumber(usersRequest.getMobile());
				}
			} catch (IOException e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		// get details of CorporateApplicantDetail
		CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
				.getByApplicationAndUserId(userId, toApplicationId);
		// set value to response
		if (corporateApplicantDetail != null) {
			BeanUtils.copyProperties(corporateApplicantDetail, response);
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getConstitutionId()))
				response.setConstitution(Constitution.getById(corporateApplicantDetail.getConstitutionId()).getValue());
			if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getEstablishmentMonth()))
				response.setEstablishmentMonth(EstablishmentMonths.getById(corporateApplicantDetail.getEstablishmentMonth()).getValue());
			// ESTABLISHMENT YEAR
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
						response.setEstablishmentYear(masterResponse.getValue());
					} else {
						response.setEstablishmentYear("NA");
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
					response.setCity(cityName != null ? cityName : "NA");
					response.setRegOfficeCity(cityName);
					
					//set State
					response.setState(stateName != null ? stateName : "NA");
					response.setRegOfficestate(stateName);
					
					//set Country
					response.setCountry(countryName != null ? countryName : "NA");
					response.setRegOfficecountry(countryName);
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
					response.setAddOfficeCity(cityName != null ? cityName : "NA");
					
					//set State
					response.setAddOfficestate(stateName != null ? stateName : "NA");
					
					//set Country
					response.setAddOfficecountry(countryName != null ? countryName : "NA");
				}
			}

			/**
			// set city
						List<Long> cityList = new ArrayList<>();
						if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCityId())) {
							cityList.add(corporateApplicantDetail.getRegisteredCityId());
						}
						if(!CommonUtils.isListNullOrEmpty(cityList))
						{
						try {
							OneFormResponse oneFormResponse = oneFormClient.getCityByCityListId(cityList);
							List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
									.getListData();
							if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
								MasterResponse masterResponse = MultipleJSONObjectHelper
										.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
								response.setCity(masterResponse.getValue());
								response.setRegOfficeCity(masterResponse.getValue());
							} else {
								response.setCity("NA");
							}
						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}
						}
						
						cityList.clear();
						if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeCityId())) {
							cityList.add(corporateApplicantDetail.getAdministrativeCityId());
						}
						if(!CommonUtils.isListNullOrEmpty(cityList))
						{
						try {
							OneFormResponse oneFormResponse = oneFormClient.getCityByCityListId(cityList);
							List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
									.getListData();
							if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
								MasterResponse masterResponse = MultipleJSONObjectHelper
										.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
								response.setAddOfficeCity(masterResponse.getValue());
								
							} else {
								response.setCity("NA");
							}
						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}
						}
						

						// set state
						List<Long> stateList = new ArrayList<>();
						if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredStateId()))
						{
							stateList.add(Long.valueOf(corporateApplicantDetail.getRegisteredStateId()));
						}
						if(!CommonUtils.isListNullOrEmpty(stateList))
						{
						try {
							OneFormResponse oneFormResponse = oneFormClient.getStateByStateListId(stateList);
							List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
									.getListData();
							if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
								MasterResponse masterResponse = MultipleJSONObjectHelper
										.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
								response.setState(masterResponse.getValue());
								response.setRegOfficestate(masterResponse.getValue());
							} else {
								response.setState("NA");
							}
						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}
						}
						
						
						stateList.clear();
						if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeStateId())) {
							stateList.add(Long.valueOf(corporateApplicantDetail.getAdministrativeStateId()));
						}
						if(!CommonUtils.isListNullOrEmpty(stateList))
						{
							try {
								OneFormResponse oneFormResponse = oneFormClient.getStateByStateListId(stateList);
								List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
										.getListData();
								if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
									MasterResponse masterResponse = MultipleJSONObjectHelper
											.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
									response.setAddOfficestate(masterResponse.getValue());
								} else {
									response.setState("NA");
								}
							} catch (Exception e) {
								logger.error(CommonUtils.EXCEPTION,e);
							}
						}
						// set country
						List<Long> countryList = new ArrayList<>();
						if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCountryId()))
						{
							countryList.add(Long.valueOf(corporateApplicantDetail.getRegisteredCountryId()));
						}
						if(!CommonUtils.isListNullOrEmpty(countryList))
						{
						try {
							OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
							List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
									.getListData();
							if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
								MasterResponse masterResponse = MultipleJSONObjectHelper
										.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
								response.setCountry(masterResponse.getValue());
								response.setRegOfficecountry(masterResponse.getValue());
							} else {
								response.setCountry("NA");
							}
						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}
						}
						
						countryList.clear();
						if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getAdministrativeCountryId())) {
							countryList.add(Long.valueOf(corporateApplicantDetail.getAdministrativeCountryId()));
						}
						if(!CommonUtils.isListNullOrEmpty(countryList))
						{
							try {
								OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
								List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
										.getListData();
								if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
									MasterResponse masterResponse = MultipleJSONObjectHelper
											.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
									response.setAddOfficecountry(masterResponse.getValue());
								} else {
									response.setCountry("NA");
								}
							} catch (Exception e) {
								logger.error(CommonUtils.EXCEPTION,e);
							}
						}
			*/				

			// set key vertical funding
			List<Long> keyVerticalFundingId = new ArrayList<>();
			keyVerticalFundingId.add(corporateApplicantDetail.getKeyVericalFunding());
			try {
				OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
				List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
						.getListData();
				if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
					MasterResponse masterResponse = MultipleJSONObjectHelper
							.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
					response.setKeyVericalFunding(masterResponse.getValue());
				} else {
					response.setKeyVericalFunding("NA");
				}

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		logger.trace("User Type :{} ",userType);

		Integer user_type = CommonUtils.ApplicantType.APPLICANT;
		// get industry sectors
		List<Long> industryList = industrySectorRepository.getIndustryByApplicationId(toApplicationId);
		List<Long> sectorList = industrySectorRepository.getSectorByApplicationId(toApplicationId);
		List<Long> subSectorList = subSectorRepository.getSubSectorByApplicationId(toApplicationId);
		IndustrySectorSubSectorTeaserRequest industrySectorSubSectorTeaserRequest = new IndustrySectorSubSectorTeaserRequest();
		industrySectorSubSectorTeaserRequest.setIndustryList(industryList);
		industrySectorSubSectorTeaserRequest.setSectorList(sectorList);
		industrySectorSubSectorTeaserRequest.setSubSectorList(subSectorList);
		try {
			OneFormResponse oneFormResponse = oneFormClient
					.getIndustrySectorSubSector(industrySectorSubSectorTeaserRequest);
			response.setIndustrySector(oneFormResponse.getListData());
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// get value of Unsecured Loan data
		PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetail = primaryUnsecuredLoanDetailRepository
				.getByApplicationAndUserId(toApplicationId, userId);
		// set value to response
		if (primaryUnsecuredLoanDetail != null) {
			BeanUtils.copyProperties(primaryUnsecuredLoanDetail, response);
			response.setTenure(primaryUnsecuredLoanDetail.getTenure() != null ? primaryUnsecuredLoanDetail.getTenure() / 12 : null);
		}
		if (primaryUnsecuredLoanDetail.getCurrencyId() != null && primaryUnsecuredLoanDetail.getDenominationId() != null) {
			response.setCurrencyDenomination(Currency.getById(primaryUnsecuredLoanDetail.getCurrencyId()).getValue() + " in "
					+ Denomination.getById(primaryUnsecuredLoanDetail.getDenominationId()).getValue());
		}
		if (primaryUnsecuredLoanDetail.getProductId() != null) {
			response.setLoanType(LoanType.getById(primaryUnsecuredLoanDetail.getProductId()).getValue());
		}
		if (primaryUnsecuredLoanDetail.getModifiedDate() != null) {
			response.setDateOfProposal(simpleDateFormat.format(primaryUnsecuredLoanDetail.getModifiedDate()));
		}
		if (primaryUnsecuredLoanDetail.getAmount() != null) {
			response.setLoanAmount(String.valueOf(primaryUnsecuredLoanDetail.getAmount()));
		}

		/**
		 * getting frame data
		 */
		// get value of proposed product and set in response
		try {
			response.setProposedProductDetailRequestList(
					proposedProductDetailsService.getProposedProductDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Proposed Product {}", e);
		}

		// get value of Existing product and set in response
		try {
			response.setExistingProductDetailRequestList(
					existingProductDetailsService.getExistingProductDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Existing Product {}", e);
		}

		// get value of achievement details and set in response
		try {
			response.setAchievementDetailList(
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
				// calling client for credit rating options
				OneFormResponse oneFormResponse = oneFormClient.getRatingById(
						CommonUtils.isObjectNullOrEmpty(creditRatingOrganizationDetailRequest.getCreditRatingOptionId())
								? null : creditRatingOrganizationDetailRequest.getCreditRatingOptionId().longValue());
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) oneFormResponse.getData(), MasterResponse.class);
				if (masterResponse != null) {
					creditRatingOrganizationDetailResponse.setCreditRatingOption(masterResponse.getValue());
				} else {
					response.setKeyVericalFunding("NA");
				}
				creditRatingOrganizationDetailResponse.setCreditRatingTerm(CreditRatingTerm
						.getById(creditRatingOrganizationDetailRequest.getCreditRatingTermId()).getValue());
				creditRatingOrganizationDetailResponse.setRatingAgency(
						RatingAgency.getById(creditRatingOrganizationDetailRequest.getRatingAgencyId()).getValue());
				creditRatingOrganizationDetailResponse
						.setFacilityName(creditRatingOrganizationDetailRequest.getFacilityName());
				creditRatingOrganizationDetailResponseList.add(creditRatingOrganizationDetailResponse);
			}
			response.setCreditRatingOrganizationDetailResponse(creditRatingOrganizationDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Credit Rating {}", e);
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
				response.setShortTermRating(shortTermValueList);
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
			response.setLongTermRating(longTermValueList);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		// get value of Ownership Details and set in response
		try {
			List<OwnershipDetailRequest> ownershipDetailRequestsList = ownershipDetailsService
					.getOwnershipDetailList(toApplicationId, userId);
			List<OwnershipDetailResponse> ownershipDetailResponseList = new ArrayList<>();
			for (OwnershipDetailRequest ownershipDetailRequest : ownershipDetailRequestsList) {
				OwnershipDetailResponse ownershipDetailResponse = new OwnershipDetailResponse();
				ownershipDetailResponse.setRemarks(ownershipDetailRequest.getRemarks());
				ownershipDetailResponse.setStackPercentage(ownershipDetailRequest.getStackPercentage());
				ownershipDetailResponse.setShareHoldingCategory(
						ShareHoldingCategory.getById(ownershipDetailRequest.getShareHoldingCategoryId()).getValue());
				ownershipDetailResponseList.add(ownershipDetailResponse);
			}
			response.setOwnershipDetailResponseList(ownershipDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Ownership Details {}", e);
		}

		// get value of Promotor Background and set in response
		try {
			List<PromotorBackgroundDetailRequest> promotorBackgroundDetailRequestList = promotorBackgroundDetailsService
					.getPromotorBackgroundDetailList(toApplicationId, userId);
			List<PromotorBackgroundDetailResponse> promotorBackgroundDetailResponseList = new ArrayList<>();
			for (PromotorBackgroundDetailRequest promotorBackgroundDetailRequest : promotorBackgroundDetailRequestList) {
				PromotorBackgroundDetailResponse promotorBackgroundDetailResponse = new PromotorBackgroundDetailResponse();
			//	promotorBackgroundDetailResponse.setAchievements(promotorBackgroundDetailRequest.getAchivements());
				promotorBackgroundDetailResponse.setAddress(promotorBackgroundDetailRequest.getAddress());
			//	promotorBackgroundDetailResponse.setAge(promotorBackgroundDetailRequest.getAge());
				promotorBackgroundDetailResponse.setPanNo(promotorBackgroundDetailRequest.getPanNo().toUpperCase());
				String promotorName = "";
				if (promotorBackgroundDetailRequest.getSalutationId() != null){
					promotorName = Title.getById(promotorBackgroundDetailRequest.getSalutationId()).getValue();
				}
				promotorName += " "+promotorBackgroundDetailRequest.getPromotorsName();
				promotorBackgroundDetailResponse.setPromotorsName(promotorName);
			//	promotorBackgroundDetailResponse.setQualification(promotorBackgroundDetailRequest.getQualification());
			//	promotorBackgroundDetailResponse
			//			.setTotalExperience(promotorBackgroundDetailRequest.getTotalExperience());
				promotorBackgroundDetailResponseList.add(promotorBackgroundDetailResponse);
			}
			response.setPromotorBackgroundDetailResponseList(promotorBackgroundDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Promotor Background {}", e);
		}

		// get value of Past Financial and set in response
		try {
			response.setPastFinancialEstimatesDetailRequestList(
					pastFinancialEstiamateDetailsService.getFinancialListData(userId, toApplicationId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Past Financial {}", e);
		}

		// get value of Future Projection and set in response
		try {
			response.setFutureFinancialEstimatesDetailRequestList(futureFinancialEstimatesDetailsService
					.getFutureFinancialEstimateDetailsList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Future Projection {}", e);
		}

		// get value of Security and set in response
		try {
			response.setSecurityCorporateDetailRequestList(
					securityCorporateDetailsService.getsecurityCorporateDetailsList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Security Details {}", e);
		}

		// get value of Financial Arrangements and set in response
		try {
			List<FinancialArrangementsDetailRequest> financialArrangementsDetailRequestList = financialArrangementDetailsService
					.getFinancialArrangementDetailsList(toApplicationId, userId);
			List<FinancialArrangementsDetailResponse> financialArrangementsDetailResponseList = new ArrayList<>();
			for (FinancialArrangementsDetailRequest financialArrangementsDetailRequest : financialArrangementsDetailRequestList) {
				FinancialArrangementsDetailResponse financialArrangementsDetailResponse = new FinancialArrangementsDetailResponse();
				financialArrangementsDetailResponse.setRelationshipSince(financialArrangementsDetailRequest.getRelationshipSince());
				financialArrangementsDetailResponse.setOutstandingAmount(financialArrangementsDetailRequest.getOutstandingAmount());
				financialArrangementsDetailResponse.setSecurityDetails(financialArrangementsDetailRequest.getSecurityDetails());
				financialArrangementsDetailResponse.setAmount(financialArrangementsDetailRequest.getAmount());
		//		financialArrangementsDetailResponse.setLenderType(LenderType.getById(financialArrangementsDetailRequest.getLenderType()).getValue());
				financialArrangementsDetailResponse.setLoanDate(financialArrangementsDetailRequest.getLoanDate());
				financialArrangementsDetailResponse.setLoanType(financialArrangementsDetailRequest.getLoanType());
				financialArrangementsDetailResponse.setFinancialInstitutionName(financialArrangementsDetailRequest.getFinancialInstitutionName());
				financialArrangementsDetailResponse.setAddress(financialArrangementsDetailRequest.getAddress());
				//if (financialArrangementsDetailRequest.getFacilityNatureId() != null)
				//	financialArrangementsDetailResponse.setFacilityNature(NatureFacility.getById(financialArrangementsDetailRequest.getFacilityNatureId()).getValue());
				financialArrangementsDetailResponseList.add(financialArrangementsDetailResponse);
			}
			response.setFinancialArrangementsDetailResponseList(financialArrangementsDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Financial Arrangements Details {}", e);
		}

		// get Finance Means Details and set in response
		try {
			List<FinanceMeansDetailRequest> financeMeansDetailRequestsList = financeMeansDetailsService
					.getMeansOfFinanceList(toApplicationId, userId);
			List<FinanceMeansDetailResponse> financeMeansDetailResponsesList = new ArrayList<FinanceMeansDetailResponse>();
			for (FinanceMeansDetailRequest financeMeansDetailRequest : financeMeansDetailRequestsList) {
				FinanceMeansDetailResponse detailResponse = new FinanceMeansDetailResponse();
				BeanUtils.copyProperties(financeMeansDetailRequest, detailResponse);
				if (financeMeansDetailRequest.getFinanceMeansCategoryId() != null) {
					detailResponse.setFinanceMeansCategory(FinanceCategory
							.getById(Integer.parseInt(financeMeansDetailRequest.getFinanceMeansCategoryId().toString()))
							.getValue());
				}
				financeMeansDetailResponsesList.add(detailResponse);
			}
			response.setFinanceMeansDetailResponseList(financeMeansDetailResponsesList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Finance Means Details {}", e);
		}

		// get Total cost of project and set in response
		try {
			List<TotalCostOfProjectRequest> costOfProjectsList = costOfProjectService
					.getCostOfProjectDetailList(toApplicationId, userId);
			List<TotalCostOfProjectResponse> costOfProjectResponses = new ArrayList<TotalCostOfProjectResponse>();
			for (TotalCostOfProjectRequest costOfProjectRequest : costOfProjectsList) {
				TotalCostOfProjectResponse costOfProjectResponse = new TotalCostOfProjectResponse();
				BeanUtils.copyProperties(costOfProjectRequest, costOfProjectResponse);
				if (costOfProjectRequest.getParticularsId() != null) {
					costOfProjectResponse.setParticulars(Particular
							.getById(Integer.parseInt(costOfProjectRequest.getParticularsId().toString())).getValue());
				}
				costOfProjectResponses.add(costOfProjectResponse);
			}
			response.setTotalCostOfProjectResponseList(costOfProjectResponses);
		} catch (Exception e) {
			logger.error("Problem to get Data of Total cost of project{}", e);
		}

		// get data of Associated Concern
		try {
			response.setAssociatedConcernDetailRequests(
					associatedConcernDetailService.getAssociatedConcernsDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Associated Concerns {}", e);
		}

		// get data of Details of Guarantors
		try {
            List<GuarantorsCorporateDetailRequest> guarantorsCorporateDetailRequestList= guarantorsCorporateDetailService
					.getGuarantorsCorporateDetailList(toApplicationId, userId);
			List<GuarantorsCorporateDetailResponse> guarantorsCorporateDetailResponseList = new ArrayList<>();
			for (GuarantorsCorporateDetailRequest guarantorsCorporateDetailRequest: guarantorsCorporateDetailRequestList) {
				GuarantorsCorporateDetailResponse guarantorsCorporateDetailResponse= new GuarantorsCorporateDetailResponse();
				BeanUtils.copyProperties(guarantorsCorporateDetailRequest, guarantorsCorporateDetailResponse);
				guarantorsCorporateDetailResponse.setPanNo(guarantorsCorporateDetailRequest.getPanNo()!=null? guarantorsCorporateDetailRequest.getPanNo().toUpperCase(): null);
				//set industry
				if(!CommonUtils.isObjectListNull(guarantorsCorporateDetailRequest.getIndustrylist()))
				{
				try {
					List<Long> industryId = new ArrayList<>();
					industryId.add((long)guarantorsCorporateDetailRequest.getIndustrylist());
					
					OneFormResponse oneFormResponse = oneFormClient.getIndustryById(industryId);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						guarantorsCorporateDetailResponse.setIndustrylist(masterResponse.getValue());
					} else {
						guarantorsCorporateDetailResponse.setIndustrylist("NA");
					}

				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				}
				
				//set industry
				if(!CommonUtils.isObjectListNull(guarantorsCorporateDetailRequest.getSectorlist()))
				{
				try {
					List<Long> sectorId = new ArrayList<>();
					sectorId.add((long)guarantorsCorporateDetailRequest.getSectorlist());
					
					OneFormResponse oneFormResponse = oneFormClient.getSectorById(sectorId);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						guarantorsCorporateDetailResponse.setSectorlist(masterResponse.getValue());
					} else {
						guarantorsCorporateDetailResponse.setSectorlist("NA");
					}

				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				}
				
				if(!CommonUtils.isObjectListNull(guarantorsCorporateDetailRequest.getConstitutionId()))
				{
					guarantorsCorporateDetailResponse.setConstitutionId(Constitution.getById(guarantorsCorporateDetailRequest.getConstitutionId()).getValue());
				}
				
				guarantorsCorporateDetailResponseList.add(guarantorsCorporateDetailResponse);
			}
			response.setGuarantorsCorporateDetailResponseList(guarantorsCorporateDetailResponseList);
		} catch (Exception e) {
			logger.error("Problem to get Data of Credit Rating {}", e);
		}
		// get data of Monthly Turnover
		try {
			response.setMonthlyTurnoverDetailRequestList(
					monthlyTurnoverDetailService.getMonthlyTurnoverDetailList(toApplicationId, userId));
		} catch (Exception e) {
			logger.error("Problem to get Data of Monthly Turnover {}", e);
		}
		
	
        //setting bank account details
        try {
//			response.setBankAccountHeldDetailsRequest(bankAccountHeldDetailService.getExistingLoanDetailList(toApplicationId, user_type));
		} catch (Exception e) {
			logger.error("Problem to get Data of Bank account {}", e);
		}
        
        //credit card
		List<CreditCardsDetailRequest> creditCardsDetailRequestList = null;
		try {
//			creditCardsDetailRequestList = creditCardsDetailService.getCreditCardDetailList(toApplicationId, user_type);
		} catch (Exception e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}
		List<CreditCardsDetailResponse> creditCardsDetailResponseList = new ArrayList<CreditCardsDetailResponse>();
		CreditCardsDetailResponse cardsDetailResponse = new CreditCardsDetailResponse();
		for(CreditCardsDetailRequest cardsDetailRequest:creditCardsDetailRequestList){
			//BeanUtils.copyProperties(cardsDetailRequest, cardsDetailResponse);
			cardsDetailResponse.setCardNumber(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getCardNumber()) ? cardsDetailRequest.getCardNumber() : "-");
			cardsDetailResponse.setIssuingBank(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getIssuingBank()) ? cardsDetailRequest.getIssuingBank() : "-");
			cardsDetailResponse.setYearOfIssue(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getYearOfIssue()) ?  cardsDetailRequest.getYearOfIssue().toString():"-");
			cardsDetailResponse.setYearOfExpiry(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getYearOfExpiry()) ?  cardsDetailRequest.getYearOfExpiry().toString():"-");
			cardsDetailResponse.setCardLimit(!CommonUtils.isObjectNullOrEmpty(cardsDetailRequest.getCardLimit()) ? cardsDetailRequest.getCardLimit().toString(): "-");
			creditCardsDetailResponseList.add(cardsDetailResponse);
		}
        
		response.setCreditCardsDetailResponse(creditCardsDetailResponseList);
        
        //references
        List<ReferenceRetailDetailsRequest> referenceRetailDetailsRequestList = null;
		try {
//			referenceRetailDetailsRequestList = referenceRetailDetailsService.getReferenceRetailDetailList(toApplicationId, user_type);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		response.setReferenceRetailDetailsRequest(referenceRetailDetailsRequestList);

		return response;
	}
		
}
