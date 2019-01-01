package com.capitaworld.service.loans.service.common.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.dms.client.DMSClient;
import com.capitaworld.service.dms.exception.DocumentException;
import com.capitaworld.service.dms.model.DocumentImportRequest;
import com.capitaworld.service.dms.model.DocumentRequest;
import com.capitaworld.service.dms.model.DocumentResponse;
import com.capitaworld.service.dms.model.StorageDetailsResponse;
import com.capitaworld.service.dms.util.DocumentAlias;
import com.capitaworld.service.loans.domain.fundseeker.FsNegativeFpList;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.AchievementDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.AssetsDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.AssociatedConcernDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.AvailabilityProposedPlantDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.BalanceSheetDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.BoardOfDirectorsDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CorporateCoApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CreditRatingOrganizationDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.DirectorBackgroundDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.DprUserDataDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.DriverForFutureGrowthDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.EmployeesCategoryBreaksDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.ExistingProductDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FinalTermLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FinalUnsecureLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FinalWorkingCapitalLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FinanceMeansDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FinancialArrangementsDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FutureFinancialEstimatesDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.GuarantorsCorporateDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.KeyManagementDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.LiabilitiesDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.MonthlyTurnoverDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.OperatingStatementDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.OverseasNetworkMappingDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.OwnershipDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PastFinancialEstimatesDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryTermLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryUnsecuredLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryWorkingCapitalLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.ProfitibilityStatementDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.ProjectImplementationScheduleDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PromotorBackgroundDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.ProposedProductDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.RequirementsAndAvailabilityRawMaterialsDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.RevenueAndOrderBookDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.ScotAnalysisDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.SecurityCorporateDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.StrategicAlliancesDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.TechnologyPositioningDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.TotalCostOfProject;
import com.capitaworld.service.loans.domain.fundseeker.retail.BankAccountHeldDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.CreditCardsDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.ReferencesRetailDetail;
import com.capitaworld.service.loans.model.common.AutoFillOneFormDetailRequest;
import com.capitaworld.service.loans.repository.fundseeker.FsNegativeFpListRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AchievementDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AssetsDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AssociatedConcernDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AvailabilityProposedPlantDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.BalanceSheetDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.BoardOfDirectorsDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CorporateCoApplicantRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CreditRatingOrganizationDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.DirectorBackgroundDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.DprUserDataDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.DriverForFutureGrowthDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.EmployeesCategoryBreaksDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ExistingProductDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FinalTermLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FinalUnsecuredLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FinalWorkingCapitalLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FinanceMeansDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FinancialArrangementDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FutureFinancialEstimatesDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.GuarantorsCorporateDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.KeyManagementDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LiabilitiesDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.MonthlyTurnoverDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.OperatingStatementDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.OverseasNetworkRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.OwnershipDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PastFinancialEstimateDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryTermLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryUnsecuredLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryWorkingCapitalLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ProfitibilityStatementDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ProjectImplementationScheduleDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PromotorBackgroundDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ProposedProductDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.RequirementsAndAvailabilityRawMaterialsDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.RevenueAndOrderBookDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ScotAnalysisDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SecurityCorporateDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.StrategicAlliancesDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SubSectorRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.TechnologyPositioningDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.TotalCostOfProjectRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.BankAccountHeldDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.CreditCardsDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.ReferenceRetailDetailsRepository;
import com.capitaworld.service.loans.service.common.AutoFillOneFormDetailService;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.CommonUtils.LoanType;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;

@Service
@Transactional
public class AutoFillOneFormDetailServiceImpl implements AutoFillOneFormDetailService {

	private static final String SUCESSFULLY_FINAL_UNSECURE_LOAN_DETAIL_SAVE_MSG = "Sucessfully FinalUnsecureLoanDetail save ==>  ";
	private static final String SUCESSFULLY_FINAL_WORKING_CAPITAL_LOAN_DETAIL_SAVE_MSG = "Sucessfully FinalWorkingCapitalLoanDetail save ==>  ";
	private static final String NEW_OBJECT_CREATED_EMPTY_DETAIL_IN_FINAL_UNSECURE_LOAN_DETAIL_MSG = "-- New Object Created, Empty detail in FinalUnsecureLoanDetail -->";
	private static final String NEW_OBJECT_CREATED_EMPTY_DETAIL_IN_FINAL_WORKING_CAPITAL_LOAN_DETAIL_MSG = "-- New Object Created, Empty detail in FinalWorkingCapitalLoanDetail -->";
	private static final String SUCESSFULLY_FINAL_TERM_LOAN_DETAIL_SAVE_MSG = "Sucessfully FinalTermLoanDetail save ==>  ";
	private static final String ENTER_IN_GET_AND_SAVE_OWNERSHIP_MSG = "===== Enter in getAndSaveOwnerShip() =====";
	private static final String WITH_USER_ID_MSG = " with user Id ==>";

	@Autowired
	private CorporateApplicantDetailRepository applicantRepository;
	@Autowired
	private PrimaryTermLoanDetailRepository primaryTLRepository;
	@Autowired
	private FsNegativeFpListRepository fsNegativeFpListRepository;
	@Autowired
	private AchievementDetailsRepository achievementDetailsRepository;
	@Autowired
	public ExistingProductDetailsRepository existingProductDetailsRepository;
	@Autowired
	private PromotorBackgroundDetailsRepository promotorBackgroundDetailsRepository;
	@Autowired
	private TotalCostOfProjectRepository totalCostOfProjectRepository;
	@Autowired
	private ProposedProductDetailsRepository proposedProductDetailsRepository;
	@Autowired
	private FinanceMeansDetailRepository financeMeansDetailRepository;
	@Autowired
	private PastFinancialEstimateDetailsRepository pastFinancialEstimateDetailsRepository;
	@Autowired
	private FutureFinancialEstimatesDetailsRepository futureFinancialEstimateDetailsRepository;
	@Autowired
	private FinancialArrangementDetailsRepository financialArrangementDetailsRepository;
	@Autowired
	private SecurityCorporateDetailsRepository securityCorporateDetailsRepository;
	@Autowired
	private PrimaryWorkingCapitalLoanDetailRepository primaryWCRepository;
	@Autowired
	private CorporateCoApplicantRepository coApplicantDetailRepository;

	@Autowired
	private IndustrySectorRepository industrySectorRepository;

	@Autowired
	private SubSectorRepository subSectorRepository;

	@Autowired
	private CorporateApplicantService corporateApplicantService;

	@Autowired
	private CreditRatingOrganizationDetailsRepository creditRatingOrganizationDetailsRepository;

	@Autowired
	private PrimaryUnsecuredLoanDetailRepository primaryUnsecuredLoanDetailRepository;

	@Autowired
	private OwnershipDetailsRepository ownershipDetailsRepository;

	@Autowired
	private FinalWorkingCapitalLoanDetailRepository finalWCRepository;

	@Autowired
	private FinalTermLoanDetailRepository finalTermLoanDetailRepository;

	@Autowired
	private FinalUnsecuredLoanDetailRepository finalUnsecuredLoanDetailRepository;

	@Autowired
	private OverseasNetworkRepository networkRepository;

	@Autowired
	private MonthlyTurnoverDetailRepository monthlyTurnoverDetailsRepository;

	@Autowired
	private GuarantorsCorporateDetailRepository guarantorsCorporateDetailRepository;

	@Autowired
	private AssociatedConcernDetailRepository associatedConcernDetailRepository;

	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private BankAccountHeldDetailRepository bankAccountHeldDetailRepository;

	@Autowired
	private ReferenceRetailDetailsRepository referenceRetailDetailsRepository;

	@Autowired
	private CreditCardsDetailRepository creditCardsDetailRepository;

	@Autowired
	private DirectorBackgroundDetailsRepository directorBackgroundDetailsRepository;

	@Autowired
	LoanApplicationRepository loanApplicationRepository;

	@Autowired
	LiabilitiesDetailsRepository liabilitiesDetailsRepository;

	@Autowired
	AssetsDetailsRepository assetsDetailsRepository;

	@Autowired
	OperatingStatementDetailsRepository operatingStatementDetailsRepository;

	@Autowired
	KeyManagementDetailRepository keyManagementDetailRepository;

	@Autowired
	EmployeesCategoryBreaksDetailRepository employeesCategoryBreaksDetailRepository;

	@Autowired
	BalanceSheetDetailRepository balanceSheetDetailRepository;

	@Autowired
	private BoardOfDirectorsDetailRepository boardOfDirectorsDetailRepository;

	@Autowired
	private StrategicAlliancesDetailRepository strategicAlliancesDetailRepository;

	@Autowired
	private ProfitibilityStatementDetailRepository profitibilityStatementDetailRepository;

	@Autowired
	private TechnologyPositioningDetailRepository technologyPositioningDetailRepository;

	@Autowired
	private RevenueAndOrderBookDetailRepository revenueAndOrderBookDetailRepository;

	@Autowired
	private DriverForFutureGrowthDetailRepository driverForFutureGrowthDetailRepository;

	@Autowired
	private ProjectImplementationScheduleDetailRepository projectImplementationScheduleDetailRepository;

	@Autowired
	private AvailabilityProposedPlantDetailRepository availabilityProposedPlantDetailRepository;

	@Autowired
	private RequirementsAndAvailabilityRawMaterialsDetailRepository requirementsAndAvailabilityRawMaterialsDetailRepository;

	@Autowired
	private ScotAnalysisDetailRepository scotAnalysisDetailRepository;

	@Autowired
	private DprUserDataDetailRepository dprUserDataDetailRepository;

	/* private Long userId = null; */

	private List<CorporateCoApplicantDetail> corporateCoApplicantDetailToList = new ArrayList<>(2);
	private static List<Long> prodDocMappingListCoApp = new ArrayList<Long>(10);
	private static String[] skipPrimaryData = { "id", "userId", CommonUtils.APPLICATION_ID, "productId", "categoryCode",
			"applicationCode", "isPrimaryLocked", "isFinalLocked", "createdBy", CommonUtils.CREATED_DATE, "isActive",
			"isMsmeScoreRequired", "eligibleAmnt", "npUserId", "npAssigneeId", "ddrStatusId", "typeOfPayment",
			"appointmentDate", "appointmentTime", "paymentAmount", "isApplicantDetailsFilled",
			"isApplicantPrimaryFilled", "isApplicantFinalFilled", "isCoApp1DetailsFilled", "isCoApp1FinalFilled",
			"isCoApp2DetailsFilled", "isCoApp2FinalFilled", "isGuarantor1DetailsFilled", "isGuarantor1FinalFilled",
			"isGuarantor2DetailsFilled", "isGuarantor2FinalFilled", "isPrimaryUploadFilled", "isFinalDprUploadFilled",
			"isFinalUploadFilled", "isFinalMcqFilled", "detailsFilledTime", "primaryFilledTime", "finalFilledTime",
			"detailsFilledCount", "primaryFilledCount", "finalFilledCount", "mcaCompanyId", "isMca", "campaignCode",
			"campaignCode" };
	private static String[] skipProfileData = { "id", CommonUtils.APPLICATION_ID, "organisationName",
			"administrativePremiseNumber", "administrativeStreetName", "administrativeLandMark",
			"administrativeCountryId", "administrativeStateId", "administrativeCityId", "administrativePincode",
			CommonUtils.CREATED_DATE, "createdBy", "isActive" };

	static {
		prodDocMappingListCoApp.add(
				DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_ADDRESS_PROOF_ELECTRICITY_BILL_ADHAR_CARD_VOTER_ID_CARD_ANY_1);
		prodDocMappingListCoApp.add(
				DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_AUDITEDUNAUDITED_BALANCE_SHEET_PROFIT_LOSS_STATEMENT_FOR_3_YEARS);
		prodDocMappingListCoApp.add(DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_FORM_16);
		prodDocMappingListCoApp.add(DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_ITR);
		prodDocMappingListCoApp.add(DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_SCANNED_COPY_OF_AADHAR_CARD);
		prodDocMappingListCoApp.add(DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_SCANNED_COPY_OF_PAN_CARD);
		prodDocMappingListCoApp
				.add(DocumentAlias.UNSECURED_LOAN_CO_APPLICANT_STATEMENT_OF_BANK_ACCOUNT_FOR_LAST_6_MONTHS);
	}

	private static final Logger logger = LoggerFactory.getLogger(AutoFillOneFormDetailServiceImpl.class);

	public void getAndSaveCorporateAutoFillOneFormDateils(Long userId,
			AutoFillOneFormDetailRequest autoFillOneFormDetailRequest)
			throws DocumentException, NullPointerException, IOException {
		logger.info("Entering in the getAndSaveCorporateAutoFillOneFormDateils ");
		logger.info("Getting all perameters " + " userId=====> " + userId + " AutoFillOneFromRequest ==========>"
				+ autoFillOneFormDetailRequest);

		// save profile
		CorporateApplicantDetail corporateApplicantDetailTo = saveProfile(autoFillOneFormDetailRequest, userId);

		// save primary
		LoanType type = CommonUtils.LoanType.getType(autoFillOneFormDetailRequest.getFromProductId().intValue());
		logger.info("From ApplicationId ====> " + autoFillOneFormDetailRequest.getFromApplicationId()
				+ " LoneType ====> " + type);
		List<Long> fromDocTypeProductMappingIDList = null;
		List<Long> fromOtherTypeProductMappingIDList = null;
		switch (type) {
		case WORKING_CAPITAL:
			// primary
			PrimaryWorkingCapitalLoanDetail loanDetailFrom = primaryWCRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
			if (loanDetailFrom == null) {
				logger.error("Throw NullPointerException in FromWCL ");
				throw new NullPointerException("PrimaryWorkingCapitalLoanDetail not exist in DB with ID=>"
						+ autoFillOneFormDetailRequest.getFromApplicationId() + WITH_USER_ID_MSG + userId);
			}

			logger.info("Get Detail  Final MCQ  FinalWorkingCapitalLoanDetail From ApplicatinId ====>"
					+ autoFillOneFormDetailRequest.getFromApplicationId());
			// final mcq
			FinalWorkingCapitalLoanDetail finalWorkingCapitalLoanDetailFrom = finalWCRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
			if (finalWorkingCapitalLoanDetailFrom == null) {
				logger.error("Error - Throw NullPointerException in FromWCL ");
				throw new NullPointerException("FinalWorkingCapitalLoanDetail not exist in DB with ID=>"
						+ autoFillOneFormDetailRequest.getFromApplicationId() + WITH_USER_ID_MSG + userId);
			}

			// IN FINAL getting list of product mappingid doc and other type
			fromDocTypeProductMappingIDList = getWCLExcelTypeProductMappingIDList();
			fromOtherTypeProductMappingIDList = getWCLOtherTypeProductMappingIDList();

			if (CommonUtils.LoanType.TERM_LOAN == CommonUtils.LoanType
					.getType(autoFillOneFormDetailRequest.getToProductId().intValue())) {
				logger.info("To ApplicationId ====> " + autoFillOneFormDetailRequest.getFromApplicationId()
						+ " LoneType ====> " + type);
				// fund requirement
				savePrimaryWCToTL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, loanDetailFrom, userId);

				// Comapny and Project Detail
				getAndSaveAchivements(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// financial
				getAndSaveFutureProjections(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// Security
				getAndSaveColletralDetails(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// final mcq
				saveFinalWCToTL(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						finalWorkingCapitalLoanDetailFrom, userId);
				// file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, getTLExcelTypeProductMappingIDList(), userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, getTLOtherTypeProductMappingIDList(), userId);

			} else if (CommonUtils.LoanType.UNSECURED_LOAN == CommonUtils.LoanType
					.getType(autoFillOneFormDetailRequest.getToProductId().intValue())) {
				// primary fund requirement
				savePrimaryWCToUSL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, loanDetailFrom, userId);
				// final mcq
				saveFinalWCToUSL(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						finalWorkingCapitalLoanDetailFrom, userId);
				// final file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, getUSLExcelTypeProductMappingIDList(), userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, getUSLOtherTypeProductMappingIDList(), userId);

			} else {
				// naegative list in all loan
				getAndSaveNagativeList(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// fund requirement
				savePrimaryWCToWC(autoFillOneFormDetailRequest, corporateApplicantDetailTo, loanDetailFrom, userId);

				// Comapny and Project Detail
				getAndSaveAchivements(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// financial
				getAndSaveFutureProjections(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// Security
				getAndSaveColletralDetails(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// final mcq
				saveFinalWCToWC(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						finalWorkingCapitalLoanDetailFrom, userId);

				// final information financial
				getAndSaveFinalInformationFinancial(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// finakl file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, fromDocTypeProductMappingIDList, userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, fromOtherTypeProductMappingIDList, userId);

			}
			break;

		case TERM_LOAN:
			PrimaryTermLoanDetail primaryTermLoanDetailFrom = primaryTLRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
			if (primaryTermLoanDetailFrom == null) {
				throw new NullPointerException("PrimaryTermLoanDetail not exist in DB with ID=>"
						+ autoFillOneFormDetailRequest.getFromApplicationId() + WITH_USER_ID_MSG + userId);
			}

			FinalTermLoanDetail finalTermLoanDetailFrom = finalTermLoanDetailRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
			if (finalTermLoanDetailFrom == null) {
				throw new NullPointerException("PrimaryTermLoanDetail not exist in DB with ID=>"
						+ autoFillOneFormDetailRequest.getFromApplicationId() + WITH_USER_ID_MSG + userId);
			}

			fromDocTypeProductMappingIDList = getTLExcelTypeProductMappingIDList();
			fromOtherTypeProductMappingIDList = getTLOtherTypeProductMappingIDList();
			if (CommonUtils.LoanType.WORKING_CAPITAL == CommonUtils.LoanType
					.getType(autoFillOneFormDetailRequest.getToProductId().intValue())) {
				// fund requirement
				savePrimaryTLtoWC(autoFillOneFormDetailRequest, corporateApplicantDetailTo, primaryTermLoanDetailFrom,
						userId);

				// Comapny and Project Detail
				getAndSaveAchivements(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// financial
				getAndSaveFutureProjections(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// Security
				getAndSaveColletralDetails(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
				// final mcq
				saveFinalTLToWL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, finalTermLoanDetailFrom,
						userId);

				// file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, getWCLExcelTypeProductMappingIDList(), userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, getWCLOtherTypeProductMappingIDList(), userId);

			} else if (CommonUtils.LoanType.UNSECURED_LOAN == CommonUtils.LoanType
					.getType(autoFillOneFormDetailRequest.getToProductId().intValue())) {
				// fund Requirement
				savePrimaryTLtoUSL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, primaryTermLoanDetailFrom,
						userId);

				// final mcq
				saveFinalTLToUSL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, finalTermLoanDetailFrom,
						userId);

				// file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, getUSLExcelTypeProductMappingIDList(), userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, getUSLOtherTypeProductMappingIDList(), userId);
				savePrimaryTLtoTL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, primaryTermLoanDetailFrom,
						userId);

			} else {

				// naegative list in all loan
				getAndSaveNagativeList(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// fund requirement
				savePrimaryTLtoTL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, primaryTermLoanDetailFrom,
						userId);

				// Comapny and Project Detail
				getAndSaveAchivements(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// financial
				getAndSaveCostEstimates(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
				getAndSaveMeanOfFinance(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
				getAndSaveFutureProjections(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
				// Security
				getAndSaveColletralDetails(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// fianl mcq
				saveFinalTLToTL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, finalTermLoanDetailFrom,
						userId);
				// file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, fromDocTypeProductMappingIDList, userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, fromOtherTypeProductMappingIDList, userId);
			}
			break;

		case UNSECURED_LOAN:

			PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetailFrom = primaryUnsecuredLoanDetailRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);

			FinalUnsecureLoanDetail finalUnsecureLoanDetailFrom = finalUnsecuredLoanDetailRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
			if (primaryUnsecuredLoanDetailFrom == null && finalUnsecureLoanDetailFrom == null) {
				throw new NullPointerException("PrimaryUnsecuredLoanDetail not exist in DB with ID=>"
						+ autoFillOneFormDetailRequest.getFromApplicationId() + WITH_USER_ID_MSG + userId);
			}
			fromDocTypeProductMappingIDList = getUSLExcelTypeProductMappingIDList();
			fromOtherTypeProductMappingIDList = getUSLOtherTypeProductMappingIDList();

			if (CommonUtils.LoanType.TERM_LOAN == CommonUtils.LoanType
					.getType(autoFillOneFormDetailRequest.getToProductId().intValue())) {
				// fund requirement
				savePrimaryUSLtoTL(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						primaryUnsecuredLoanDetailFrom, userId);

				// final mcq
				saveFinalUSLToTL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, finalUnsecureLoanDetailFrom,
						userId);
				// file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, getTLExcelTypeProductMappingIDList(), userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, getTLOtherTypeProductMappingIDList(), userId);

			} else if (CommonUtils.LoanType.WORKING_CAPITAL == CommonUtils.LoanType
					.getType(autoFillOneFormDetailRequest.getToProductId().intValue())) {
				// fund requirement
				savePrimaryUSLtoWC(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						primaryUnsecuredLoanDetailFrom, userId);

				// final mcq
				saveFinalUSLToWC(autoFillOneFormDetailRequest, corporateApplicantDetailTo, finalUnsecureLoanDetailFrom,
						userId);

				// file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, getWCLExcelTypeProductMappingIDList(), userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, getWCLOtherTypeProductMappingIDList(), userId);

			} else {
				// naegative list in all loan
				getAndSaveNagativeList(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

				// funs requiremnt
				savePrimaryUSLtoUSL(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						primaryUnsecuredLoanDetailFrom, userId);

				// final mcq
				saveFinalUSLToUSL(autoFillOneFormDetailRequest, corporateApplicantDetailTo, finalUnsecureLoanDetailFrom,
						userId);

				// final information
				getAndSaveFinalUSLCreditCard(autoFillOneFormDetailRequest, corporateApplicantDetailTo);
				getAndSaveFinalUSLBankAccountHeldDetail(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						userId);
				// getAndSaveFinalUSLAdditionalDetailRefrence();

				// final file upload of co applicant
				List<CorporateCoApplicantDetail> corporateCoApplicantDetailsList = coApplicantDetailRepository
						.getList(autoFillOneFormDetailRequest.getFromApplicationId(), userId);// hjhh

				if (corporateCoApplicantDetailsList != null) {
					try {
						getAndSaveFinalFileUploadUSLCoApplicant(autoFillOneFormDetailRequest,
								corporateApplicantDetailTo, corporateCoApplicantDetailsList, userId);
					} catch (DocumentException e) {
						logger.error("Error final upload ------------- co Applicant file uploding failed from USL To USl : ",e);
					}
				}
				// file upload
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromDocTypeProductMappingIDList, fromDocTypeProductMappingIDList, userId);
				getAndSaveFinalAllFileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo,
						fromOtherTypeProductMappingIDList, fromOtherTypeProductMappingIDList, userId);
			}
			break;
		default : break;
		}

		// Comapny and Project in all loan
		getAndSaveProductsExixting(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
		getAndSaveProductsProposed(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

		// promopter for all loan
		getAndSaveDirectorBackGround(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
		getAndSavePromotoresBackGround(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
		getAndSaveOwnerShip(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

		// financila for all loan
		getAndSavePastFinancials(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
		getAndSaveCurrentFinancial(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
		getAndSaveCreditRating(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

		// others
		getAndSaveReferenceDatailForPrimaryAndFinal(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

		// final mcq OverseasnetworksIs
		getAndSaveFinalMCQOverseasNetworkIds(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

		// final information other Guarantor
		getAndSaveFinalOtherGuarantor(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

		// final information other
		getAndSaveFinalOtherAssociatedConcern(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);

	}

	public CorporateApplicantDetail saveProfile(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			Long userId) {
		logger.info("================Entering in =====> saveProfile() of corporate loan========= ");

		// profile
		CorporateApplicantDetail corporateApplicantDetailFrom = applicantRepository
				.findOneByApplicationIdId(autoFillOneFormDetailRequest.getFromApplicationId());

		CorporateApplicantDetail corporateApplicantDetailTo = applicantRepository
				.findOneByApplicationIdId(autoFillOneFormDetailRequest.getToApplicationId());
		if (corporateApplicantDetailTo == null) {
			corporateApplicantDetailTo = new CorporateApplicantDetail();
			corporateApplicantDetailTo.setCreatedDate(new Date());
			corporateApplicantDetailTo.setCreatedBy(userId);
			corporateApplicantDetailTo.setIsActive(true);
			corporateApplicantDetailTo
					.setApplicationId(new LoanApplicationMaster(autoFillOneFormDetailRequest.getToApplicationId()));
		}
		BeanUtils.copyProperties(corporateApplicantDetailFrom, corporateApplicantDetailTo, skipProfileData);
		corporateApplicantDetailTo.setModifiedDate(new Date());
		corporateApplicantDetailTo.setModifiedBy(userId);
		corporateApplicantDetailTo = applicantRepository.save(corporateApplicantDetailTo);
		logger.info("Getting all industry and sectors and sub sector list");
		// lists
		List<Long> industrylist = industrySectorRepository
				.getIndustryByApplicationId(autoFillOneFormDetailRequest.getFromApplicationId());
		logger.info(industrylist.toString());
		List<Long> sectorlist = industrySectorRepository
				.getSectorByApplicationId(autoFillOneFormDetailRequest.getFromApplicationId());
		logger.info(sectorlist.toString());
		List<Long> subSectorlist = subSectorRepository
				.getSubSectorByApplicationId(autoFillOneFormDetailRequest.getFromApplicationId());
		logger.info(subSectorlist.toString());

		logger.info("InActiveMappingByApplicationId of Industry And Sectors========>"
				+ corporateApplicantDetailTo.getApplicationId().getId());
		industrySectorRepository.inActiveMappingByApplicationId(corporateApplicantDetailTo.getApplicationId().getId());
		// inactive previous before adding new Data
		logger.info("InActiveMappingByApplicationId of subSectore ========>"
				+ corporateApplicantDetailTo.getApplicationId().getId());
		subSectorRepository.inActiveMappingByApplicationId(corporateApplicantDetailTo.getApplicationId().getId());

		logger.info("Save All Industry And Sectors And SubSector List");
		corporateApplicantService.saveIndustry(corporateApplicantDetailTo.getApplicationId().getId(), industrylist);
		logger.info("Sucessfull  Industry List saved ");
		corporateApplicantService.saveSector(corporateApplicantDetailTo.getApplicationId().getId(), sectorlist);
		logger.info("Sucessfull  Sectors List saved ");
		corporateApplicantService.saveSubSector(corporateApplicantDetailTo.getApplicationId().getId(), subSectorlist);
		logger.info("Sucessfull  SubSectors List saved ");
		if (CommonUtils.LoanType.UNSECURED_LOAN == CommonUtils.LoanType
				.getType(autoFillOneFormDetailRequest.getFromProductId().intValue())
				&& CommonUtils.LoanType.UNSECURED_LOAN == CommonUtils.LoanType
						.getType(autoFillOneFormDetailRequest.getToProductId().intValue())) {

			List<CorporateCoApplicantDetail> corporateCoApplicantDetailsFromList = coApplicantDetailRepository
					.getList(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
			logger.info("OLD InActiveAllCoApplicant =======>ApplicationId " + corporateApplicantDetailTo.getId());
			// co applicant
			coApplicantDetailRepository.inactiveAllCoApplicant(corporateApplicantDetailTo.getApplicationId().getId());

			for (CorporateCoApplicantDetail corporateCoApplicantDetail : corporateCoApplicantDetailsFromList) {
				CorporateCoApplicantDetail coTo = new CorporateCoApplicantDetail();
				BeanUtils.copyProperties(corporateCoApplicantDetail, coTo, "id", CommonUtils.APPLICATION_ID);
				coTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
				coTo.setCreatedDate(new Date());
				corporateCoApplicantDetailToList.add(coApplicantDetailRepository.save(coTo));

			}
			logger.info("Save All New CoApplicant  =======>ApplicationId " + corporateApplicantDetailTo.getId());
		}
		logger.info("=================Exit From saveProfile()==================");
		return corporateApplicantDetailTo;
	}

	public void savePrimaryWCToWC(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			PrimaryWorkingCapitalLoanDetail workingCapitalLoanDetailFrom, Long userId) {
		logger.info("================Enter in savePrimaryWCToWC() ===========");
		try {

			PrimaryWorkingCapitalLoanDetail workingCapitalLoanDetailTo = primaryWCRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
			if (workingCapitalLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryWCToWC()");
				throw new NullPointerException();
			}
			BeanUtils.copyProperties(workingCapitalLoanDetailFrom, workingCapitalLoanDetailTo, skipPrimaryData);
			workingCapitalLoanDetailTo.setModifiedBy(userId);
			workingCapitalLoanDetailTo.setModifiedDate(new Date());
			workingCapitalLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());

			primaryWCRepository.save(workingCapitalLoanDetailTo);

		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryWCToWC()",e);
			throw e;
		}

		logger.info("================= Exit From savePrimaryWCToWC()================== ");
	}

	public void savePrimaryWCToTL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			PrimaryWorkingCapitalLoanDetail primaryWorkingCapitalLoanDetailFrom, Long userId) {
		logger.info("Enter in savePrimaryWCToTL() save detail from to To applicant ");
		try {
			PrimaryTermLoanDetail primaryTermLoanDetailTo = primaryTLRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
			logger.info("Get PrimaryTermLoanDetail of From application ====> "
					+ autoFillOneFormDetailRequest.getFromApplicationId());
			if (primaryTermLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryWCToTL()");

				throw new NullPointerException();
			}
			// build
			BeanUtils.copyProperties(primaryWorkingCapitalLoanDetailFrom, primaryTermLoanDetailTo, skipPrimaryData);

			primaryTermLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			primaryTermLoanDetailTo.setModifiedBy(userId);
			primaryTermLoanDetailTo.setModifiedDate(new Date());
			primaryTLRepository.save(primaryTermLoanDetailTo);
			logger.info("Sucessfully save PrimaryTermLoanDetail in To application Id"
					+ corporateApplicantDetailTo.getApplicationId());
		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryWCToTL()",e);
			throw e;
		}
		logger.info("================= Exit From savePrimaryWCToTL()================== ");
	}

	public void savePrimaryWCToUSL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			PrimaryWorkingCapitalLoanDetail primaryWorkingCapitalLoanDetailFrom, Long userId) {
		logger.info("Enter in savePrimaryWCToUSL() save detail from to To applicant ");
		try {
			PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetailTo = primaryUnsecuredLoanDetailRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
			if (primaryUnsecuredLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryWCToUSL()");
				throw new NullPointerException();
			}
			BeanUtils.copyProperties(primaryWorkingCapitalLoanDetailFrom, primaryUnsecuredLoanDetailTo,
					skipPrimaryData);
			primaryUnsecuredLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			primaryUnsecuredLoanDetailTo.setModifiedBy(userId);
			primaryUnsecuredLoanDetailTo.setModifiedDate(new Date());
			primaryUnsecuredLoanDetailRepository.save(primaryUnsecuredLoanDetailTo);

		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryWCToUSL()",e);
			throw e;

		}
		logger.info("================= Exit From savePrimaryWCToUSL()================== ");
	}

	public void savePrimaryTLtoTL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, PrimaryTermLoanDetail primaryTermLoanDetailFrom,
			Long userId) {
		logger.info("=================Enter in savePrimaryTLtoTL() save detail from to To applicant ============== ");

		PrimaryTermLoanDetail primaryTermLoanDetailTo = primaryTLRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		try {
			if (primaryTermLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryTLtoTL()");
				throw new NullPointerException();
			}
			BeanUtils.copyProperties(primaryTermLoanDetailFrom, primaryTermLoanDetailTo, skipPrimaryData);
			primaryTermLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			primaryTermLoanDetailTo.setModifiedBy(userId);
			primaryTermLoanDetailTo.setModifiedDate(new Date());
			primaryTLRepository.save(primaryTermLoanDetailTo);
		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryTLtoTL()",e);
			throw e;

		}

		logger.info("================= Exit From savePrimaryTLtoTL()================== ");
	}

	public void savePrimaryTLtoWC(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, PrimaryTermLoanDetail primaryTermLoanDetailFrom,
			Long userId) throws NullPointerException {
		logger.info("=================Enter in savePrimaryTLtoWC() save detail from to To applicant ============== ");

		PrimaryWorkingCapitalLoanDetail workingCapitalLoanDetailTo = primaryWCRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		try {
			if (workingCapitalLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryTLtoWC()");
				throw new NullPointerException();
			}
			BeanUtils.copyProperties(primaryTermLoanDetailFrom, workingCapitalLoanDetailTo, skipPrimaryData);
			workingCapitalLoanDetailTo.setModifiedBy(userId);
			workingCapitalLoanDetailTo.setModifiedDate(new Date());
			workingCapitalLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			primaryWCRepository.save(workingCapitalLoanDetailTo);
		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryTLtoWC()",e);
			throw e;

		}
		logger.info("================= Exit From savePrimaryTLtoWC()================== ");
	}

	public void savePrimaryTLtoUSL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, PrimaryTermLoanDetail primaryTermLoanDetailFrom,
			Long userId) {
		logger.info("=================Enter in savePrimaryTLtoUSL() save detail from to To applicant ============== ");

		PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetailTo = primaryUnsecuredLoanDetailRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		try {
			if (primaryUnsecuredLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryTLtoUSL()");
				throw new NullPointerException();
			}
			BeanUtils.copyProperties(primaryTermLoanDetailFrom, primaryUnsecuredLoanDetailTo, skipPrimaryData);
			primaryUnsecuredLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			primaryUnsecuredLoanDetailTo.setModifiedBy(userId);
			primaryUnsecuredLoanDetailTo.setModifiedDate(new Date());
			primaryUnsecuredLoanDetailRepository.save(primaryUnsecuredLoanDetailTo);
		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryTLtoUSL()",e);
			throw e;

		}

		logger.info("================= Exit From savePrimaryTLtoUSL()================== ");
	}

	public void savePrimaryUSLtoUSL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetailFrom, Long userId) {
		logger.info("=================Enter in savePrimaryUSLtoUSL() save detail from to To applicant ============== ");

		PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetailTo = primaryUnsecuredLoanDetailRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		try {
			if (primaryUnsecuredLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryUSLtoUSL()");
				throw new NullPointerException();
			}
			BeanUtils.copyProperties(primaryUnsecuredLoanDetailFrom, primaryUnsecuredLoanDetailTo, skipPrimaryData);
			primaryUnsecuredLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			primaryUnsecuredLoanDetailTo.setModifiedBy(userId);
			primaryUnsecuredLoanDetailTo.setModifiedDate(new Date());
			primaryUnsecuredLoanDetailRepository.save(primaryUnsecuredLoanDetailTo);
		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryUSLtoUSL()",e);
			throw e;

		}
		logger.info("================= Exit From savePrimaryUSLtoUSL()================== ");
	}

	public void savePrimaryUSLtoWC(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetailFrom, Long userId) {
		logger.info("=================Enter in savePrimaryUSLtoWC() save detail from to To applicant ============== ");

		PrimaryWorkingCapitalLoanDetail workingCapitalLoanDetailTo = primaryWCRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		try {
			if (workingCapitalLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryUSLtoWC()");
				throw new NullPointerException();
			}
			BeanUtils.copyProperties(primaryUnsecuredLoanDetailFrom, workingCapitalLoanDetailTo, skipPrimaryData);
			workingCapitalLoanDetailTo.setModifiedDate(new Date());
			workingCapitalLoanDetailTo.setModifiedBy(userId);
			workingCapitalLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			primaryWCRepository.save(workingCapitalLoanDetailTo);
		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryUSLtoWC()",e);
			throw e;

		}
		logger.info("================= Exit From savePrimaryUSLtoWC()================== ");
	}

	public void savePrimaryUSLtoTL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			PrimaryUnsecuredLoanDetail primaryUnsecuredLoanDetailFrom, Long userId) {
		logger.info("=================Enter in savePrimaryUSLtoTL() save detail from to To applicant ============== ");
		try {
			PrimaryTermLoanDetail primaryTermLoanDetailTo = primaryTLRepository
					.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
			if (primaryTermLoanDetailTo == null) {
				logger.error("NullPointer Exception in savePrimaryUSLtoTL()");

				throw new NullPointerException();
			}
			BeanUtils.copyProperties(primaryUnsecuredLoanDetailFrom, primaryTermLoanDetailTo, skipPrimaryData);
			primaryTermLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			primaryTermLoanDetailTo.setModifiedDate(new Date());
			primaryTermLoanDetailTo.setModifiedBy(userId);
			primaryTLRepository.save(primaryTermLoanDetailTo);

		} catch (NullPointerException e) {
			logger.error("NullPointer Exception in savePrimaryUSLtoTL()",e);
			throw e;
		}
		logger.info("================= Exit From savePrimaryUSLtoTL()================== ");
	}

	public void getAndSaveAchivements(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("=================Enter in getAndSaveAchivements() ============== ");
		List<AchievementDetail> achievementDetailsFromList = achievementDetailsRepository
				.listAchievementFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		logger.info("Get chievementDetailList from FromApplicationId");

		achievementDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		logger.info("InActive To ApplicationId");

		for (AchievementDetail achievementDetailFrom : achievementDetailsFromList) {
			AchievementDetail achievementDetailTo = new AchievementDetail();
			BeanUtils.copyProperties(achievementDetailFrom, achievementDetailTo, "id", CommonUtils.APPLICATION_ID);
			achievementDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			achievementDetailTo.setCreatedDate(new Date());
			achievementDetailsRepository.save(achievementDetailTo);

		}
		logger.info("Sucessfully save AchievementDetailList to To ApplicationId");
		logger.info("================= Exit From getAndSaveAchivements()================== ");
	}

	public void getAndSaveProductsExixting(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("=================Enter in getAndSaveProductsExixting() ============== ");
		// save AchievementDetail existing
		List<ExistingProductDetail> existingProductDetailsFromList = existingProductDetailsRepository
				.listExistingProductFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		existingProductDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (ExistingProductDetail existingProductDetailFrom : existingProductDetailsFromList) {
			ExistingProductDetail existingProductDetailTo = new ExistingProductDetail();
			BeanUtils.copyProperties(existingProductDetailFrom, existingProductDetailTo, "id", CommonUtils.APPLICATION_ID);
			existingProductDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			existingProductDetailTo.setCreatedDate(new Date());
			existingProductDetailsRepository.save(existingProductDetailTo);
		}
		logger.info("================= Exit From getAndSaveProductsExixting()================== ");
	}

	public void getAndSaveProductsProposed(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("=================Enter in getAndSaveProductsProposed() ============== ");
		// save AchievementDetail proposed
		List<ProposedProductDetail> proposedProductDetailsFromList = proposedProductDetailsRepository
				.listProposedProductFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		proposedProductDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (ProposedProductDetail proposedProductDetailFrom : proposedProductDetailsFromList) {
			ProposedProductDetail proposedProductDetailTo = new ProposedProductDetail();
			BeanUtils.copyProperties(proposedProductDetailFrom, proposedProductDetailTo, "id", CommonUtils.APPLICATION_ID);
			proposedProductDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());

			proposedProductDetailTo.setCreatedDate(new Date());
			proposedProductDetailsRepository.save(proposedProductDetailTo);
		}
		logger.info("================= Exit From getAndSaveProductsProposed()================== ");
	}

	public void getAndSavePromotoresBackGround(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info(ENTER_IN_GET_AND_SAVE_OWNERSHIP_MSG);
		// save director / promoter
		List<PromotorBackgroundDetail> promotorBackgroundDetailsFromList = promotorBackgroundDetailsRepository
				.listPromotorBackgroundFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);

		promotorBackgroundDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (PromotorBackgroundDetail promotorBackgroundDetailFrom : promotorBackgroundDetailsFromList) {
			PromotorBackgroundDetail promotorBackgroundDetailTo = new PromotorBackgroundDetail();
			BeanUtils.copyProperties(promotorBackgroundDetailFrom, promotorBackgroundDetailTo, "id", CommonUtils.APPLICATION_ID);
			promotorBackgroundDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			promotorBackgroundDetailTo.setCreatedBy(userId);
			promotorBackgroundDetailTo.setModifiedDate(new Date());
			promotorBackgroundDetailsRepository.save(promotorBackgroundDetailTo);
		}
		logger.info("================= Exit From getAndSaveBackGround()================== ");
	}

	public void getAndSaveOwnerShip(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info(ENTER_IN_GET_AND_SAVE_OWNERSHIP_MSG);
		// promoters
		List<OwnershipDetail> ownershipDetailsList = ownershipDetailsRepository
				.listOwnershipFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		ownershipDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());

		for (OwnershipDetail ownershipDetailFrom : ownershipDetailsList) {
			OwnershipDetail ownershipDetailTo = new OwnershipDetail();
			BeanUtils.copyProperties(ownershipDetailFrom, ownershipDetailTo, "id", CommonUtils.APPLICATION_ID);
			ownershipDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			ownershipDetailTo.setCreatedDate(new Date());
			ownershipDetailsRepository.save(ownershipDetailTo);
		}
		logger.info("================= Exit From getAndSaveOwnerShip()================== ");
	}

	public void getAndSaveDirectorBackGround(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info(ENTER_IN_GET_AND_SAVE_OWNERSHIP_MSG);
		// promoters
		List<DirectorBackgroundDetail> directorBackgroundDetailsList = directorBackgroundDetailsRepository
				.listPromotorBackgroundFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		directorBackgroundDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (DirectorBackgroundDetail directorBackgroundDetailFrom : directorBackgroundDetailsList) {
			DirectorBackgroundDetail directorBackgroundDetailTo = new DirectorBackgroundDetail();
			BeanUtils.copyProperties(directorBackgroundDetailFrom, directorBackgroundDetailTo, "id", CommonUtils.APPLICATION_ID);
			directorBackgroundDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			directorBackgroundDetailTo.setCreatedDate(new Date());
			directorBackgroundDetailsRepository.save(directorBackgroundDetailTo);
		}
		logger.info("================= Exit From getAndSaveOwnerShip()================== ");
	}

	public void getAndSaveCostEstimates(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================Enter in getAndSaveCostEstimates() ===========");
		// financial cost
		List<TotalCostOfProject> totalCostOfProjectRequestfromList = totalCostOfProjectRepository
				.listCostOfProjectFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		totalCostOfProjectRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());

		for (TotalCostOfProject totalCostOfProjectFrom : totalCostOfProjectRequestfromList) {
			TotalCostOfProject totalCostOfProjectTo = new TotalCostOfProject();
			BeanUtils.copyProperties(totalCostOfProjectFrom, totalCostOfProjectTo, "id", CommonUtils.APPLICATION_ID);
			totalCostOfProjectTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			totalCostOfProjectTo.setCreatedDate(new Date());
			totalCostOfProjectRepository.save(totalCostOfProjectTo);
		}
		logger.info("================= Exit From getAndSavePastFinancials()================== ");
	}

	public void getAndSaveMeanOfFinance(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================Enter in getAndSaveCostEstimates() ===========");
		// means of finence
		List<FinanceMeansDetail> financeMeansDetailsFromList = financeMeansDetailRepository
				.listFinanceMeansFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		financeMeansDetailRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (FinanceMeansDetail financeMeansDetailFrom : financeMeansDetailsFromList) {
			FinanceMeansDetail financeMeansDetailTo = new FinanceMeansDetail();
			BeanUtils.copyProperties(financeMeansDetailFrom, financeMeansDetailTo, "id", CommonUtils.APPLICATION_ID);
			financeMeansDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			financeMeansDetailTo.setCreatedDate(new Date());
			financeMeansDetailRepository.save(financeMeansDetailTo);
		}
		logger.info("================= Exit From getAndSaveMeanOfFinance()================== ");
	}

	public void getAndSavePastFinancials(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================Enter in getAndSavePastFinancials() ===========");
		// FINANCIAL past financial SAME (working and term and unsecure)
		List<PastFinancialEstimatesDetail> pastFinancialEstimateDetailsFromList = pastFinancialEstimateDetailsRepository
				.listPastFinancialEstimateDetailsFromAppId(autoFillOneFormDetailRequest.getFromApplicationId());
		pastFinancialEstimateDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (PastFinancialEstimatesDetail pastFinancialEstimatesDetailfrom : pastFinancialEstimateDetailsFromList) {
			PastFinancialEstimatesDetail pastFinancialEstimatesDetailTo = new PastFinancialEstimatesDetail();
			BeanUtils.copyProperties(pastFinancialEstimatesDetailfrom, pastFinancialEstimatesDetailTo, "id",
					CommonUtils.APPLICATION_ID);
			pastFinancialEstimatesDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			pastFinancialEstimatesDetailTo.setCreatedDate(new Date());
			pastFinancialEstimateDetailsRepository.save(pastFinancialEstimatesDetailTo);

		}
		logger.info("================= Exit From getAndSavePastFinancials()================== ");
	}

	public void getAndSaveFutureProjections(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================Enter in getAndSaveFutureProjections() ===========");
		// FINANCIAL future projection SAME (working and term)
		List<FutureFinancialEstimatesDetail> futureFinancialEstimateDetailsFormLsit = futureFinancialEstimateDetailsRepository
				.listFutureFinancialEstimateDetailsFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(),
						userId);
		logger.info("Get FutureFinancialEstimatesDetailList ====> " + futureFinancialEstimateDetailsFormLsit
				+ " FromApplicationId ===>" + autoFillOneFormDetailRequest.getFromApplicationId());
		futureFinancialEstimateDetailsRepository.inActive(userId,
				corporateApplicantDetailTo.getApplicationId().getId());
		logger.info("InActive FutureFinancialEstimatesDetail To ApplicationId");
		for (FutureFinancialEstimatesDetail futureFinancialEstimatesDetailFrom : futureFinancialEstimateDetailsFormLsit) {
			FutureFinancialEstimatesDetail futureFinancialEstimatesDetailTo = new FutureFinancialEstimatesDetail();
			BeanUtils.copyProperties(futureFinancialEstimatesDetailFrom, futureFinancialEstimatesDetailTo, "id",
					CommonUtils.APPLICATION_ID);
			futureFinancialEstimatesDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			futureFinancialEstimatesDetailTo.setCreatedDate(new Date());
			futureFinancialEstimateDetailsRepository.save(futureFinancialEstimatesDetailTo);
		}
		logger.info("================= Exit From getAndSaveFutureProjections()================== ");
	}

	public void getAndSaveCurrentFinancial(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================Enter in getAndSaveCurrentFinancial() ===========");
		// FINANCIAL current financial arrangements SAME (working and term and unsecure)
		List<FinancialArrangementsDetail> financialArrangementDetailsFromList = financialArrangementDetailsRepository
				.listSecurityCorporateDetailFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		financialArrangementDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (FinancialArrangementsDetail financialArrangementsDetailFrom : financialArrangementDetailsFromList) {
			FinancialArrangementsDetail financialArrangementsDetailTo = new FinancialArrangementsDetail();
			BeanUtils.copyProperties(financialArrangementsDetailFrom, financialArrangementsDetailTo, "id",
					CommonUtils.APPLICATION_ID);
			financialArrangementsDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			financialArrangementsDetailTo.setCreatedDate(new Date());
			financialArrangementDetailsRepository.save(financialArrangementsDetailTo);
		}
		logger.info("================= Exit From getAndSaveCurrentFinancial()================== ");
	}

	public void getAndSaveCreditRating(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================Enter in getAndSaveCreditRating() ===========");
		// FINANCIAL Credit rating SAME (working and term and unsecure)
		List<CreditRatingOrganizationDetail> creditRatingOrganizationDetailsFromList = creditRatingOrganizationDetailsRepository
				.listCreditRatingOrganizationDetailsFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(),
						userId);
		creditRatingOrganizationDetailsRepository.inActive(userId,
				corporateApplicantDetailTo.getApplicationId().getId());
		for (CreditRatingOrganizationDetail creditRatingOrganizationDetailFrom : creditRatingOrganizationDetailsFromList) {
			CreditRatingOrganizationDetail creditRatingOrganizationDetailTo = new CreditRatingOrganizationDetail();
			BeanUtils.copyProperties(creditRatingOrganizationDetailFrom, creditRatingOrganizationDetailTo, "id",
					CommonUtils.APPLICATION_ID);
			creditRatingOrganizationDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			creditRatingOrganizationDetailTo.setCreatedDate(new Date());
			creditRatingOrganizationDetailsRepository.save(creditRatingOrganizationDetailTo);
			logger.info("hii");
		}
		logger.info("inside getAndSaveCreditRating");
		logger.info("================= Exit From getAndSaveCreditRating()================== ");
	}

	public void getAndSaveColletralDetails(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================Enter in getAndSaveColletralDetails() ===========");
		// Colletral Security
		List<SecurityCorporateDetail> securityCorporateDetailsFromList = securityCorporateDetailsRepository
				.listSecurityCorporateDetailFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		securityCorporateDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (SecurityCorporateDetail securityCorporateDetailFrom : securityCorporateDetailsFromList) {
			SecurityCorporateDetail securityCorporateDetailTo = new SecurityCorporateDetail();
			BeanUtils.copyProperties(securityCorporateDetailFrom, securityCorporateDetailTo, "id", CommonUtils.APPLICATION_ID);
			securityCorporateDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			securityCorporateDetailTo.setCreatedDate(new Date());
			securityCorporateDetailsRepository.save(securityCorporateDetailTo);
		}
		logger.info("================= Exit From getAndSaveColletralDetails()================== ");

	}

	public void getAndSaveReferenceDatailForPrimaryAndFinal(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================Enter in getAndSaveReferenceDatail() ===========");
		List<ReferencesRetailDetail> referencesRetailDetailFromList = referenceRetailDetailsRepository
				.listReferencesRetailFromAppId(autoFillOneFormDetailRequest.getFromApplicationId());
		if (referencesRetailDetailFromList == null) {
			referenceRetailDetailsRepository
					.listReferencesRetailFromCoAppId(autoFillOneFormDetailRequest.getFromApplicationId());
			if (referencesRetailDetailFromList == null) {
				referencesRetailDetailFromList = referenceRetailDetailsRepository
						.listReferencesRetailFromGarrId(autoFillOneFormDetailRequest.getFromApplicationId());
			}
			logger.info("Get ReferencesRetailDetailfromList =====> " + referencesRetailDetailFromList
					+ "  FromApplicationId ===>" + autoFillOneFormDetailRequest.getFromApplicationId());
		}
		referenceRetailDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (ReferencesRetailDetail referencesRetailDetailFrom : referencesRetailDetailFromList) {
			ReferencesRetailDetail referencesRetailDetailTo = new ReferencesRetailDetail();
			BeanUtils.copyProperties(referencesRetailDetailFrom, referencesRetailDetailTo, "id", CommonUtils.APPLICATION_ID);
			referencesRetailDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			referencesRetailDetailTo.setModifiedBy(userId);
			referencesRetailDetailTo.setModifiedDate(new Date());
			referenceRetailDetailsRepository.save(referencesRetailDetailTo);
		}
		logger.info("================= Exit From getAndSaveReferenceDatail()================== ");
	}

	public void getAndSaveNagativeList(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("-----------Enter in getAndSaveNagativeList()--------------");
		List<Long> negativeList = fsNegativeFpListRepository
				.getListByApplicationId(autoFillOneFormDetailRequest.getFromApplicationId());
		fsNegativeFpListRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		// fsNegativeFpListRepository.getListByApplicationId(id)
		FsNegativeFpList fsNegativeFpList = null;
		if (negativeList == null) {
			logger.warn("Nagetive List not available " + negativeList
					+ " FromAplicationId ======> getAndSaveNagativeList()");
		}
		for (Long fpId : negativeList) {
			fsNegativeFpList = new FsNegativeFpList();
			fsNegativeFpList.setApplicationId(corporateApplicantDetailTo.getApplicationId().getId());
			fsNegativeFpList.setFpId(fpId);
			fsNegativeFpList.setCreatedBy(userId);
			fsNegativeFpList.setCreatedDate(new Date());
			fsNegativeFpList.setIsActive(true); // create by and update
			fsNegativeFpListRepository.save(fsNegativeFpList);
		}
		logger.info("---------------- Exit From getAndSaveColletralDetails()-------------");
	}

	public void saveFinalWCToWC(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			FinalWorkingCapitalLoanDetail finalWorkingCapitalLoanDetailFrom, Long userId) {
		logger.info("================Enter in saveFinalWCToWC() ===========");
		// FinalTermLoanD

		FinalWorkingCapitalLoanDetail finalWorkingCapitalLoanDetailTo = finalWCRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		if (finalWorkingCapitalLoanDetailTo == null) {
			logger.warn("Aplication Id not available in DB=====> saveFinalWCToWC()");
			logger.warn(NEW_OBJECT_CREATED_EMPTY_DETAIL_IN_FINAL_WORKING_CAPITAL_LOAN_DETAIL_MSG
					+ finalWorkingCapitalLoanDetailTo);
			finalWorkingCapitalLoanDetailTo = new FinalWorkingCapitalLoanDetail();

		}
		BeanUtils.copyProperties(finalWorkingCapitalLoanDetailFrom, finalWorkingCapitalLoanDetailTo, "id",
				CommonUtils.APPLICATION_ID);
		finalWorkingCapitalLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalWorkingCapitalLoanDetailTo.setCreatedDate(new Date());
		finalWorkingCapitalLoanDetailTo = finalWCRepository.save(finalWorkingCapitalLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_WORKING_CAPITAL_LOAN_DETAIL_SAVE_MSG + finalWorkingCapitalLoanDetailTo);
		logger.info("================= Exit From saveFinalWCToWC()================== ");
	}

	public void saveFinalWCToTL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			FinalWorkingCapitalLoanDetail finalWorkingCapitalLoanDetailFrom, Long userId) {
		logger.info("================ Enter in saveFinalWCToTL() ===========");
		// FinalTermLoanD
		FinalTermLoanDetail finalTermLoanDetailTo = finalTermLoanDetailRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		if (finalTermLoanDetailTo == null) {
			logger.warn("Aplication Id not available in DB=====> saveFinalWCToTL()");
			logger.warn("----------New Object Created,  Empty detail in FinalTermLoanDetail  --------> "
					+ finalTermLoanDetailTo);
			finalTermLoanDetailTo = new FinalTermLoanDetail();

		}
		BeanUtils.copyProperties(finalWorkingCapitalLoanDetailFrom, finalTermLoanDetailTo, "id", CommonUtils.APPLICATION_ID);
		finalTermLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalTermLoanDetailTo.setCreatedDate(new Date());
		finalTermLoanDetailTo = finalTermLoanDetailRepository.save(finalTermLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_TERM_LOAN_DETAIL_SAVE_MSG + finalTermLoanDetailTo);
		logger.info("================= Exit From saveFinalWCToTL()================== ");
	}

	public void saveFinalWCToUSL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			FinalWorkingCapitalLoanDetail finalWorkingCapitalLoanDetailFrom, Long userId) {
		logger.info("================ Enter in saveFinalWCToUSL() ===========");
		// FinalTermLoanD
		FinalUnsecureLoanDetail finalUnsecureLoanDetailTo = finalUnsecuredLoanDetailRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		if (finalUnsecureLoanDetailTo == null) {
			logger.warn("Aplication Id not available in DB =====> saveFinalWCToUSL()");
			logger.warn(NEW_OBJECT_CREATED_EMPTY_DETAIL_IN_FINAL_UNSECURE_LOAN_DETAIL_MSG
					+ finalUnsecureLoanDetailTo);
			finalUnsecureLoanDetailTo = new FinalUnsecureLoanDetail();
		}
		BeanUtils.copyProperties(finalWorkingCapitalLoanDetailFrom, finalUnsecureLoanDetailTo, "id", CommonUtils.APPLICATION_ID);
		finalUnsecureLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalUnsecureLoanDetailTo.setCreatedDate(new Date());
		finalUnsecureLoanDetailTo = finalUnsecuredLoanDetailRepository.save(finalUnsecureLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_UNSECURE_LOAN_DETAIL_SAVE_MSG + finalUnsecureLoanDetailTo);
		logger.info("================= Exit From saveFinalWCToUSL()================== ");
	}

	public void saveFinalTLToTL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, FinalTermLoanDetail finalTermLoanDetailFrom,
			Long userId) {
		logger.info("================ Enter in saveFinalTLToTL() ===========");
		// FinalTermLoanD
		FinalTermLoanDetail finalTermLoanDetailTo = finalTermLoanDetailRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		if (finalTermLoanDetailTo == null) {
			logger.warn("Aplication Id not available in DB  =====> saveFinalTLToTL()");
			logger.warn("----------New Object Created,  Empty detail in FinalTermLoanDetail  --------> "
					+ finalTermLoanDetailTo);
			finalTermLoanDetailTo = new FinalTermLoanDetail();
		}
		BeanUtils.copyProperties(finalTermLoanDetailFrom, finalTermLoanDetailTo, "id", CommonUtils.APPLICATION_ID);
		finalTermLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalTermLoanDetailTo.setCreatedDate(new Date());
		finalTermLoanDetailTo = finalTermLoanDetailRepository.save(finalTermLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_TERM_LOAN_DETAIL_SAVE_MSG + finalTermLoanDetailTo);
		logger.info("================= Exit From saveFinalTLToTL()================== ");
	}

	public void saveFinalTLToWL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, FinalTermLoanDetail finalTermLoanDetailFrom,
			Long userId) {
		logger.info("================ Enter in saveFinalTLToWL() ===========");
		// FinalTermLoanD
		FinalWorkingCapitalLoanDetail finalWorkingCapitalLoanDetailTo = finalWCRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		if (finalWorkingCapitalLoanDetailTo == null) {
			logger.warn(NEW_OBJECT_CREATED_EMPTY_DETAIL_IN_FINAL_WORKING_CAPITAL_LOAN_DETAIL_MSG
					+ finalWorkingCapitalLoanDetailTo);
			finalWorkingCapitalLoanDetailTo = new FinalWorkingCapitalLoanDetail();
		}
		BeanUtils.copyProperties(finalTermLoanDetailFrom, finalWorkingCapitalLoanDetailTo, "id", CommonUtils.APPLICATION_ID);
		finalWorkingCapitalLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalWorkingCapitalLoanDetailTo.setCreatedDate(new Date());
		finalWorkingCapitalLoanDetailTo = finalWCRepository.save(finalWorkingCapitalLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_WORKING_CAPITAL_LOAN_DETAIL_SAVE_MSG + finalWorkingCapitalLoanDetailTo);
		logger.info("================= Exit From saveFinalTLToWL()================== ");
	}

	public void saveFinalTLToUSL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, FinalTermLoanDetail finalTermLoanDetailFrom,
			Long userId) {
		logger.info("================ Enter in saveFinalTLToUSL() ===========");
		// FinalTermLoanD
		FinalUnsecureLoanDetail finalUnsecureLoanDetailTo = finalUnsecuredLoanDetailRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		if (finalUnsecureLoanDetailTo == null) {
			logger.warn("applicationId not  available in DB in saveFinalTLToUSL () ");
			logger.warn(NEW_OBJECT_CREATED_EMPTY_DETAIL_IN_FINAL_UNSECURE_LOAN_DETAIL_MSG
					+ finalUnsecureLoanDetailTo);
			finalUnsecureLoanDetailTo = new FinalUnsecureLoanDetail();
		}
		BeanUtils.copyProperties(finalTermLoanDetailFrom, finalUnsecureLoanDetailTo, "id", CommonUtils.APPLICATION_ID);
		finalUnsecureLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalUnsecureLoanDetailTo.setCreatedDate(new Date());
		finalUnsecureLoanDetailTo = finalUnsecuredLoanDetailRepository.save(finalUnsecureLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_UNSECURE_LOAN_DETAIL_SAVE_MSG + finalUnsecureLoanDetailTo);
		logger.info("================= Exit From saveFinalTLToUSL()================== ");
	}

	public void saveFinalUSLToUSL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, FinalUnsecureLoanDetail finalUnsecureLoanDetailfrom,
			Long userId) {
		logger.info("================ Enter in saveFinalUSLToUSL() ===========");
		// FinalTermLoanD
		FinalUnsecureLoanDetail finalUnsecureLoanDetailTo = finalUnsecuredLoanDetailRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		if (finalUnsecureLoanDetailTo == null) {
			logger.warn("applicationid not available in  DB saveFinalUSLToUSL ");
			logger.warn(NEW_OBJECT_CREATED_EMPTY_DETAIL_IN_FINAL_UNSECURE_LOAN_DETAIL_MSG
					+ finalUnsecureLoanDetailTo);
			finalUnsecureLoanDetailTo = new FinalUnsecureLoanDetail();
		}
		BeanUtils.copyProperties(finalUnsecureLoanDetailfrom, finalUnsecureLoanDetailTo, "id", CommonUtils.APPLICATION_ID);
		finalUnsecureLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalUnsecureLoanDetailTo.setCreatedDate(new Date());
		finalUnsecureLoanDetailTo = finalUnsecuredLoanDetailRepository.save(finalUnsecureLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_UNSECURE_LOAN_DETAIL_SAVE_MSG + finalUnsecureLoanDetailTo);
		logger.info("================= Exit From saveFinalUSLToUSL()================== ");
	}

	public void saveFinalUSLToWC(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, FinalUnsecureLoanDetail finalUnsecureLoanDetailfrom,
			Long userId) {
		logger.info("================ Enter in saveFinalUSLToWC() ===========");
		FinalWorkingCapitalLoanDetail finalWorkingCapitalLoanDetailTo = finalWCRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		if (finalWorkingCapitalLoanDetailTo == null) {
			logger.warn(NEW_OBJECT_CREATED_EMPTY_DETAIL_IN_FINAL_WORKING_CAPITAL_LOAN_DETAIL_MSG
					+ finalWorkingCapitalLoanDetailTo);
			finalWorkingCapitalLoanDetailTo = new FinalWorkingCapitalLoanDetail();

		}
		BeanUtils.copyProperties(finalUnsecureLoanDetailfrom, finalWorkingCapitalLoanDetailTo, "id", CommonUtils.APPLICATION_ID);
		finalWorkingCapitalLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalWorkingCapitalLoanDetailTo.setCreatedDate(new Date());
		finalWorkingCapitalLoanDetailTo = finalWCRepository.save(finalWorkingCapitalLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_WORKING_CAPITAL_LOAN_DETAIL_SAVE_MSG + finalWorkingCapitalLoanDetailTo);
		logger.info("================= Exit From saveFinalUSLToWC()================== ");
	}

	public void saveFinalUSLToTL(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, FinalUnsecureLoanDetail finalUnsecureLoanDetailfrom,
			Long userId) {
		logger.info("================ Enter in saveFinalUSLToTL() ===========");

		FinalTermLoanDetail finalTermLoanDetailTo = finalTermLoanDetailRepository
				.getByApplicationAndUserId(autoFillOneFormDetailRequest.getToApplicationId(), userId);
		logger.warn("---------- FinalTermLoanDetail --------> " + finalTermLoanDetailTo);
		if (finalTermLoanDetailTo == null) {
			finalTermLoanDetailTo = new FinalTermLoanDetail();
			logger.info("TL application id ont avialable");

		}
		BeanUtils.copyProperties(finalUnsecureLoanDetailfrom, finalTermLoanDetailTo, "id", CommonUtils.APPLICATION_ID);
		finalTermLoanDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
		finalTermLoanDetailTo.setCreatedDate(new Date());
		finalTermLoanDetailTo = finalTermLoanDetailRepository.save(finalTermLoanDetailTo);
		logger.info(SUCESSFULLY_FINAL_TERM_LOAN_DETAIL_SAVE_MSG + finalTermLoanDetailTo);
		logger.info("================= Exit From saveFinalUSLToTL()================== ");
	}

	public void getAndSaveFinalMCQOverseasNetworkIds(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================ Enter in getAndSaveFinalMCQOverseasNetworkIds() ===========");
		List<Integer> overseasNetworkIdsFrom = networkRepository
				.getOverseasNetworkIds(autoFillOneFormDetailRequest.getFromApplicationId());
		logger.warn("---------- overseasNetworkIdsFromList (Integer)  -----> " + overseasNetworkIdsFrom);
		if (overseasNetworkIdsFrom == null) {
			logger.error("Error in getAndSaveFinalMCQOverseasNetworkIds()");
			throw new NullPointerException();
		}
		networkRepository.inActiveMappingByApplicationId(corporateApplicantDetailTo.getApplicationId().getId());
		for (Integer networkIdsTo : overseasNetworkIdsFrom) {
			OverseasNetworkMappingDetail mappingDetailTo = new OverseasNetworkMappingDetail();
			mappingDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId().getId());
			mappingDetailTo.setOverseasNetworkId(networkIdsTo);
			mappingDetailTo.setActive(true);
			mappingDetailTo.setCreatedDate(new Date());
			mappingDetailTo.setCreatedBy(userId);
			networkRepository.save(mappingDetailTo);
		}
		logger.info("================= Exit From getAndSaveFinalMCQOverseasNetworkIds()================== ");
	}

	public void getAndSaveFinalInformationFinancial(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================ Enter in getAndSaveFinalInformationFinancial() ===========");

		List<MonthlyTurnoverDetail> monthlyTurnoverDetailsList = monthlyTurnoverDetailsRepository
				.listMonthlyTurnoverFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		logger.warn("---------- MonthlyTurnoverDetail  -----> " + monthlyTurnoverDetailsList);
		if (CommonUtils.isListNullOrEmpty(monthlyTurnoverDetailsList)) {
			logger.error("Exception in getAndSaveFinalInformationFinancial()");
			throw new NullPointerException();
		}
		monthlyTurnoverDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (MonthlyTurnoverDetail monthlyTurnoverDetailFrom : monthlyTurnoverDetailsList) {
			MonthlyTurnoverDetail monthlyTurnoverDetailTo = new MonthlyTurnoverDetail();
			BeanUtils.copyProperties(monthlyTurnoverDetailFrom, monthlyTurnoverDetailTo, "id", CommonUtils.APPLICATION_ID,
					CommonUtils.CREATED_DATE);
			monthlyTurnoverDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			monthlyTurnoverDetailTo.setCreatedDate(new Date());
			monthlyTurnoverDetailsRepository.save(monthlyTurnoverDetailTo);
		}

		logger.info("================= Exit From getAndSaveFinalInformationFinancial()================== ");
	}

	public void getAndSaveFinalOtherGuarantor(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================ Enter in getAndSaveFinalOtherGuarantor() ===========");
		List<GuarantorsCorporateDetail> guarantorsCorporateDetailList = guarantorsCorporateDetailRepository
				.listGuarantorsCorporateFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		logger.warn("---------- GuarantorsCorporateDetail  -----> " + guarantorsCorporateDetailList);
		guarantorsCorporateDetailRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (GuarantorsCorporateDetail guarantorsCorporateDetailFrom : guarantorsCorporateDetailList) {
			GuarantorsCorporateDetail guarantorsCorporateDetailTo = new GuarantorsCorporateDetail();
			BeanUtils.copyProperties(guarantorsCorporateDetailFrom, guarantorsCorporateDetailTo, "id", CommonUtils.APPLICATION_ID,
					CommonUtils.CREATED_DATE);
			guarantorsCorporateDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			guarantorsCorporateDetailTo.setCreatedDate(new Date());
			guarantorsCorporateDetailRepository.save(guarantorsCorporateDetailTo);
		}
		logger.info("================= Exit From getAndSaveFinalOtherGuarantor()================== ");
	}

	public void getAndSaveFinalOtherAssociatedConcern(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================ Enter in getAndSaveFinalOtherAssociatedConcern() ===========");

		List<AssociatedConcernDetail> associatedConcernDetailList = associatedConcernDetailRepository
				.listAssociatedConcernFromAppId(autoFillOneFormDetailRequest.getFromApplicationId(), userId);
		logger.warn("---------- AssociatedConcernDetail  -----> " + associatedConcernDetailList);
		associatedConcernDetailRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (AssociatedConcernDetail associatedConcernDetailFrom : associatedConcernDetailList) {
			AssociatedConcernDetail associatedConcernDetailTo = new AssociatedConcernDetail();
			BeanUtils.copyProperties(associatedConcernDetailFrom, associatedConcernDetailTo, "id", CommonUtils.APPLICATION_ID,
					CommonUtils.CREATED_DATE);
			associatedConcernDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			associatedConcernDetailTo.setCreatedDate(new Date());
			associatedConcernDetailRepository.save(associatedConcernDetailTo);
		}
		logger.info("================= Exit From getAndSaveFinalOtherAssociatedConcern()================== ");
	}

	public void getAndSaveFinalUSLAdditionalDetailRefrence(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================ Enter in getAndSaveFinalUSLAdditionalDetailRefrence() ===========");

		List<ReferencesRetailDetail> referencesRetailDetailList = referenceRetailDetailsRepository
				.listReferencesRetailFromAppId(autoFillOneFormDetailRequest.getFromApplicationId());
		logger.warn("---------- ReferencesRetailDetail  -----> " + referencesRetailDetailList);
		referenceRetailDetailsRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (ReferencesRetailDetail referencesRetailDetailFrom : referencesRetailDetailList) {
			ReferencesRetailDetail referencesRetailRequestTo = new ReferencesRetailDetail();
			BeanUtils.copyProperties(referencesRetailDetailFrom, referencesRetailRequestTo, "id", CommonUtils.APPLICATION_ID);
			referencesRetailRequestTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			referencesRetailRequestTo.setCreatedDate(new Date());
			referenceRetailDetailsRepository.save(referencesRetailRequestTo);
		}
		logger.info("================= Exit From getAndSaveFinalUSLAdditionalDetailRefrence()================== ");
	}

	public void getAndSaveFinalUSLBankAccountHeldDetail(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long userId) {
		logger.info("================ Enter in getAndSaveFinalUSLBankAccountHeldDetail() ===========");
		List<BankAccountHeldDetail> bankAccountHeldDetailsList = bankAccountHeldDetailRepository
				.listBankAccountHeldFromAppId(autoFillOneFormDetailRequest.getFromApplicationId());
		logger.warn("----------BankAccountHeldDetail ----------> " + bankAccountHeldDetailsList);
		bankAccountHeldDetailRepository.inActive(userId, corporateApplicantDetailTo.getApplicationId().getId());
		for (BankAccountHeldDetail bankAccountHeldDetailFrom : bankAccountHeldDetailsList) {
			BankAccountHeldDetail bankAccountHeldDetailTo = new BankAccountHeldDetail();
			BeanUtils.copyProperties(bankAccountHeldDetailFrom, bankAccountHeldDetailTo, "id", CommonUtils.APPLICATION_ID);
			bankAccountHeldDetailTo.setApplicantId(corporateApplicantDetailTo.getApplicationId());
			bankAccountHeldDetailTo.setCreatedDate(new Date());
			bankAccountHeldDetailRepository.save(bankAccountHeldDetailTo);
		}
		logger.info("================= Exit From getAndSaveFinalUSLBankAccountHeldDetail()================== ");
	}

	public void getAndSaveFinalUSLCreditCard(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo) {
		logger.info("================ Enter in getAndSaveFinalUSLCreditCard() ===========");
		List<CreditCardsDetail> creditCardsDetailsList = creditCardsDetailRepository
				.listCreditCardsFromAppId(autoFillOneFormDetailRequest.getFromApplicationId());
		logger.warn("----------CreditCardsDetailList ----------> " + creditCardsDetailsList);
		creditCardsDetailRepository.inactive(corporateApplicantDetailTo.getApplicationId().getId());
		for (CreditCardsDetail creditCardsDetailFrom : creditCardsDetailsList) {
			CreditCardsDetail creditCardsDetailTo = new CreditCardsDetail();
			BeanUtils.copyProperties(creditCardsDetailFrom, creditCardsDetailTo, "id", CommonUtils.APPLICATION_ID);
			creditCardsDetailTo.setApplicantionId(corporateApplicantDetailTo.getApplicationId());
			creditCardsDetailTo.setCreatedDate(new Date());
			creditCardsDetailRepository.save(creditCardsDetailTo);
		}
		logger.info("=================Sucessfully Exit From getAndSaveFinalUSLCreditCard()================== ");
	}

	public List<Long> getTLExcelTypeProductMappingIDList() throws DocumentException {
		logger.info(" =============== Enter in getTLExcelTypeProductMappingIDList() ============");
		List<Long> prodDocMappingList = new ArrayList<Long>(5);
		prodDocMappingList.add((long) DocumentAlias.TL_DPR_OUR_FORMAT);
		prodDocMappingList.add((long) DocumentAlias.TL_DPR_YOUR_FORMAT);
		prodDocMappingList.add((long) DocumentAlias.TL_CMA);
		prodDocMappingList.add((long) DocumentAlias.TL_COMPANY_ACT);
		// prodDocMappingList.add((long) DocumentAlias.TL_FINANCIAL_MODEL);
		logger.info("================= Exit From getTLExcelTypeProductMappingIDList()================== ");
		return prodDocMappingList;
	}

	public List<Long> getWCLExcelTypeProductMappingIDList() throws DocumentException {
		logger.info("================ Enter in getWCLExcelTypeProductMappingIDList() ===========");

		List<Long> prodDocMappingList = new ArrayList<Long>(5);
		prodDocMappingList.add((long) DocumentAlias.WC_DPR_OUR_FORMAT);
		prodDocMappingList.add((long) DocumentAlias.WC_DPR_YOUR_FORMAT);
		prodDocMappingList.add((long) DocumentAlias.WC_CMA);
		prodDocMappingList.add((long) DocumentAlias.WC_COMPANY_ACT);
		// prodDocMappingList.add((long) DocumentAlias.WC_FINANCIAL_MODEL);
		logger.info("================= Exit From getWCLExcelTypeProductMappingIDList()================== ");
		return prodDocMappingList;
	}

	public List<Long> getUSLExcelTypeProductMappingIDList() throws DocumentException {
		logger.info("================ Enter in getUSLExcelTypeProductMappingIDList() ===========");
		List<Long> prodDocMappingList = new ArrayList<Long>(5);
		prodDocMappingList.add(0, (long) 0);
		prodDocMappingList.add(1, (long) 0);
		prodDocMappingList.add(2, (long) DocumentAlias.USL_CMA);
		prodDocMappingList.add(3, (long) DocumentAlias.USL_COMPANY_ACT);
		prodDocMappingList.add(4, (long) 0);

		logger.info("================= Exit From getUSLExcelTypeProductMappingIDList()================== ");
		return prodDocMappingList;
	}

	public List<Long> getTLOtherTypeProductMappingIDList() throws DocumentException {
		logger.info("================ Enter in getTLOtherTypeProductMappingIDList() ===========");
		List<Long> prodDocMappingList = new ArrayList<Long>();

		prodDocMappingList.add(DocumentAlias.TERM_LOAN_LAST_AUDITED_ANNUAL_REPORT);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_SANCTION_LETTER_COPY);
		// new
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_BANK_STATEMENT);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_LAST_IT_RETURN);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_NET_WORTH_STATEMENT_OF_DIRECTORS);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_PROVISIONAL_FINANCIALS);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_BROCHURE_OF_PROPOSED_ACTIVITIES);

		// new
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_IT_RETURN_DIRECTOR);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_FINANCIALS_OF_SUBSIDIARIES);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_ASSESSMENT_ORDERS);

		prodDocMappingList.add(DocumentAlias.TERM_LOAN_CERTIFICATE_OF_INCORPORATION);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_COPY_OF_PAN_CARD);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_PAN_OF_DIRECTORS_CERTIFICATE_OF_INCORPORATION);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_DETAILED_LIST_OF_SHAREHOLDERS);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_PHOTO_OF_DIRECTORS);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_DIRECTOR_ADDRESS);
		prodDocMappingList.add(DocumentAlias.TERM_LOAN_DIRECTOR_RESOLUTION);

		prodDocMappingList.add(DocumentAlias.TL_MOM_AOA);
		prodDocMappingList.add(DocumentAlias.TL_DEBTORS_LIST);
		prodDocMappingList.add(DocumentAlias.TL_GST_APPLIED);
		prodDocMappingList.add(DocumentAlias.TL_LETTER_OF_INTENT);
		prodDocMappingList.add(DocumentAlias.TL_RELEVANT_LICENSE);
		prodDocMappingList.add(DocumentAlias.TL_SALES_TAX);
		prodDocMappingList.add(DocumentAlias.TL_LATEST_TAX);
		prodDocMappingList.add(DocumentAlias.TL_ENCUMBRANCE);
		prodDocMappingList.add(DocumentAlias.TL_COPIES_TRUST_DEEDS);
		prodDocMappingList.add(DocumentAlias.TL_MARKET_SURVEY_REPORT);
		prodDocMappingList.add(DocumentAlias.TL_CONTINGENT_LIABILITIES);

		logger.info("================= Exit From getTLOtherTypeProductMappingIDList()================== ");
		return prodDocMappingList;
	}

	public List<Long> getWCLOtherTypeProductMappingIDList() throws DocumentException {
		logger.info("================ Enter in getWCLOtherTypeProductMappingIDList() ===========");
		List<Long> prodDocMappingList = new ArrayList<Long>();
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_LAST_AUDITED_ANNUAL_REPORT);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_SANCTION_LETTER_COPY);
		// new
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_BANK_STATEMENT);

		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_LAST_IT_RETURN);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_NET_WORTH_STATEMENT_OF_DIRECTORS);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_PROVISIONAL_FINANCIALS);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_BROCHURE_OF_PROPOSED_ACTIVITIES);
		// new
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_IT_RETURN_DIRECTOR);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_FINANCIALS_OF_SUBSIDIARIES);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_ASSESSMENT_ORDERS);
		// kyc
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_CERTIFICATE_OF_INCORPORATION);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_COPY_OF_PAN_CARD);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_PAN_OF_DIRECTORS_CERTIFICATE_OF_INCORPORATION);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_DETAILED_LIST_OF_SHAREHOLDERS);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_PHOTO_OF_DIRECTORS);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_DIRECTOR_ADDRESS);
		prodDocMappingList.add(DocumentAlias.WORKING_CAPITAL_DIRECTOR_RESOLUTION);
		// other
		prodDocMappingList.add(DocumentAlias.WC_MOM_AOA);
		prodDocMappingList.add(DocumentAlias.WC_DEBTORS_LIST);
		prodDocMappingList.add(DocumentAlias.WC_GST_APPLIED);
		prodDocMappingList.add(DocumentAlias.WC_LETTER_OF_INTENT);
		prodDocMappingList.add(DocumentAlias.WC_RELEVANT_LICENSE);
		prodDocMappingList.add(DocumentAlias.WC_SALES_TAX);
		prodDocMappingList.add(DocumentAlias.WC_LATEST_TAX);
		prodDocMappingList.add(DocumentAlias.WC_ENCUMBRANCE);
		prodDocMappingList.add(DocumentAlias.WC_COPIES_TRUST_DEEDS);
		prodDocMappingList.add(DocumentAlias.WC_MARKET_SURVEY_REPORT);
		prodDocMappingList.add(DocumentAlias.WC_CONTINGENT_LIABILITIES);

		logger.info("================= Exit From getWCLOtherTypeProductMappingIDList()================== ");
		return prodDocMappingList;
	}

	public List<Long> getUSLOtherTypeProductMappingIDList() throws DocumentException {
		logger.info("================ Enter in getUSLOtherTypeProductMappingIDList() ===========");
		List<Long> prodDocMappingList = new ArrayList<Long>();
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_LAST_AUDITED_ANNUAL_REPORT);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_SANCTION_LETTER_COPY);
		// new
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_BANK_STATEMENT);

		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_LAST_IT_RETURN);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_NET_WORTH_STATEMENT_OF_DIRECTORS);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_PROVISIONAL_FINANCIALS);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_BROCHURE_OF_PROPOSED_ACTIVITIES);
		// new
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_IT_RETURN_DIRECTOR);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_FINANCIALS_OF_SUBSIDIARIES);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_ASSESSMENT_ORDERS);

		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_CERTIFICATE_OF_INCORPORATION);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_COPY_OF_PAN_CARD);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_PAN_OF_DIRECTORS_CERTIFICATE_OF_INCORPORATION);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_DETAILED_LIST_OF_SHAREHOLDERS);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_PHOTO_OF_DIRECTORS);
		// new
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_DIRECTOR_ADDRESS);
		prodDocMappingList.add(DocumentAlias.UNSECURED_LOAN_DIRECTOR_RESOLUTION);

		prodDocMappingList.add(DocumentAlias.USL_MOM_AOA);
		prodDocMappingList.add(DocumentAlias.USL_DEBTORS_LIST);
		prodDocMappingList.add(DocumentAlias.USL_GST_APPLIED);
		prodDocMappingList.add(DocumentAlias.USL_LETTER_OF_INTENT);
		prodDocMappingList.add(DocumentAlias.USL_RELEVANT_LICENSE);
		prodDocMappingList.add(DocumentAlias.USL_SALES_TAX);
		prodDocMappingList.add(DocumentAlias.USL_LATEST_TAX);
		prodDocMappingList.add(DocumentAlias.USL_ENCUMBRANCE);
		prodDocMappingList.add(DocumentAlias.USL_COPIES_TRUST_DEEDS);
		prodDocMappingList.add(DocumentAlias.USL_MARKET_SURVEY_REPORT);
		prodDocMappingList.add(DocumentAlias.USL_CONTINGENT_LIABILITIES);

		logger.info("================= Exit From getUSLOtherTypeProductMappingIDList()================== ");
		return prodDocMappingList;
	}

	/*
	 * @SuppressWarnings("unchecked") public void deleteFile(List<Long>
	 * prodDocMappingList, String userType) {
	 * logger.info("================ Enter in deleteFile() ===========");
	 * DocumentResponse documentResponse = null; for (Long productMapIDTo :
	 * prodDocMappingList) { DocumentRequest documentRequest = new
	 * DocumentRequest();
	 * documentRequest.setApplicationId(corporateApplicantDetailTo.getApplicationId(
	 * ).getId()); documentRequest.setUserType(userType);
	 * documentRequest.setProductDocumentMappingId(productMapIDTo);
	 * 
	 * try { documentResponse = dmsClient.listProductDocument(documentRequest);
	 * DocumentResponse response = null; for (Object obj :
	 * documentResponse.getDataList()) { StorageDetailsResponse res =
	 * MultipleJSONObjectHelper .getObjectFromMap((LinkedHashMap<String, Object>)
	 * obj, StorageDetailsResponse.class);
	 * documentRequest.setOriginalFileName(res.getOriginalFileName()); JSONObject
	 * json = new JSONObject(); json.put("id", res.getId()); response =
	 * dmsClient.deleteIrrDocument(json.toJSONString()); if (response != null &&
	 * response.getStatus() == 200) { if (productMapIDTo == DocumentAlias.TL_CMA ||
	 * productMapIDTo == DocumentAlias.WC_CMA) {
	 * assetsDetailsRepository.inActiveAssetsDetails(documentResponse.getStorageId()
	 * ); liabilitiesDetailsRepository.inActiveAssetsDetails(documentResponse.
	 * getStorageId());
	 * operatingStatementDetailsRepository.inActiveAssetsDetails(documentResponse.
	 * getStorageId());
	 * balanceSheetDetailRepository.inActiveBalanceSheetDetail(documentResponse.
	 * getStorageId());
	 * keyManagementDetailRepository.inActiveKeyManagementDetails(documentResponse.
	 * getStorageId()); employeesCategoryBreaksDetailRepository
	 * .inActiveemployeesCategoryBreaksDetails(documentResponse.getStorageId());
	 * 
	 * boardOfDirectorsDetailRepository
	 * .inActiveBoardOfDirectorsDetails(documentResponse.getStorageId());
	 * strategicAlliancesDetailRepository
	 * .inActiveStrategicAlliancesDetails(documentResponse.getStorageId());
	 * keyManagementDetailRepository.inActiveKeyManagementDetails(documentResponse.
	 * getStorageId()); employeesCategoryBreaksDetailRepository
	 * .inActiveemployeesCategoryBreaksDetails(documentResponse.getStorageId());
	 * 
	 * }
	 * 
	 * } } } catch (DocumentException | IOException e) {
	 * logger.error("Error - Failed to delect files : ",e); } }
	 * logger.info("================= Exit From deleteFile()================== "); }
	 */

	public void getAndSaveFinalAllFileUpload(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, List<Long> prodDocMappingFromList,
			List<Long> prodDocMappingToList, Long userId) throws DocumentException {
		logger.info("================ Enter in getAndSaveFinalAllFileUpload() ===========");
		DocumentRequest documentRequest = new DocumentRequest();
		DocumentResponse response = null;
		int i = 0;
		logger.warn("------ ProdDocMappingList -------> " + prodDocMappingFromList);
		for (Long productDocumentMappingIdFrom : prodDocMappingFromList) {
			documentRequest.setApplicationId(autoFillOneFormDetailRequest.getFromApplicationId());
			documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
			documentRequest.setProductDocumentMappingId(productDocumentMappingIdFrom);
			response = dmsClient.listProductDocument(documentRequest);
			if (response.getDataList().size() > 0 && prodDocMappingFromList.indexOf(i) != 0) {
				if(productDocumentMappingIdFrom==(long)DocumentAlias.TL_CMA || productDocumentMappingIdFrom==(long)DocumentAlias.TL_COMPANY_ACT || productDocumentMappingIdFrom== (long)DocumentAlias.WC_CMA ||productDocumentMappingIdFrom==(long)DocumentAlias.WC_COMPANY_ACT || productDocumentMappingIdFrom==(long)DocumentAlias.USL_CMA||productDocumentMappingIdFrom==(long)DocumentAlias.USL_COMPANY_ACT ) {
                   readAndSaveExcelData(autoFillOneFormDetailRequest, corporateApplicantDetailTo, prodDocMappingToList.get(i), userId);
				} else {
				  fileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo, response,
						prodDocMappingToList.get(i), userId);
				}
			}
			i++;
		}
		logger.info("================= Exit From getAndSaveFinalAllFileUpload()================== ");
	}

	public void getAndSaveFinalFileUploadUSLCoApplicant(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,
			List<CorporateCoApplicantDetail> corporateCoApplicantDetailFromList, Long userId) throws DocumentException {
		logger.info("================ Enter in getAndSaveFinalFileUploadUSLCoApplicant() ===========");
		DocumentRequest documentRequest = new DocumentRequest();
		DocumentResponse documentResponse = null;
		logger.warn("------ CorporateCoApplicantDetailList -------> " + corporateCoApplicantDetailFromList);
		for (CorporateCoApplicantDetail corporateCoApplicantDetailFrom : corporateCoApplicantDetailFromList) {
			for (Long productDocumentMappingId : prodDocMappingListCoApp) {
				documentRequest.setCoApplicantId(corporateCoApplicantDetailFrom.getId());
				documentRequest.setUserType(DocumentAlias.UERT_TYPE_CO_APPLICANT);
				documentRequest.setProductDocumentMappingId(productDocumentMappingId);
				documentResponse = dmsClient.listProductDocument(documentRequest);
				if (documentResponse.getDataList().size() > 0) {
					for (CorporateCoApplicantDetail coto : corporateCoApplicantDetailToList) {
						documentRequest.setApplicationId(corporateApplicantDetailTo.getApplicationId().getId());
						documentRequest.setCoApplicantId(coto.getId());

						fileUpload(autoFillOneFormDetailRequest, corporateApplicantDetailTo, documentResponse,
								productDocumentMappingId, userId);
					}
				}

			}
		}
		logger.info("================= Exit From getAndSaveFinalFileUploadUSLCoApplicant()================== ");
	}

	public void fileUpload(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, DocumentResponse response, Long productmappingId,
			Long userId) {
		logger.info("================ Enter in fileUpload() ===========");
		try {
			DocumentImportRequest documentImportRequest = new DocumentImportRequest();
			documentImportRequest.setToApplicationId(corporateApplicantDetailTo.getApplicationId().getId());

			for (Object obj : response.getDataList()) {
				StorageDetailsResponse res = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) obj, StorageDetailsResponse.class);
				documentImportRequest.setStorageId(res.getId());
				response = dmsClient.importDocument(documentImportRequest);

				if (response != null && (response.getStatus() == 200)) {
					logger.info("================= Sucessfull file uploaded ==================> ",response);               
				}
			}
		} catch (DocumentException | IOException e) {
			logger.error("-------- Exception in fileUpload  ---------> {}", e.getMessage());
		}
		logger.info("================= Exit From fileUpload()================== ");
	}

	@SuppressWarnings("unchecked")
	private void readAndSaveExcelData(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo, Long productDocumentMappingId, 
			 Long userId) throws DocumentException {
		logger.info("================ Enter in readAndSaveExcelData() ===========");
		// Code for read CMA BS and DPR
		Boolean flag = false;
		Long toApplicationId =corporateApplicantDetailTo.getApplicationId().getId();
		try {

			switch (productDocumentMappingId.intValue()) {

			/*
			 * case DocumentAlias.WC_FINANCIAL_MODEL: { logger.info(
			 * "Going to INactive DocumentAlias.WC_FINANCIAL_MODEL===>{}===>for Application Id==> for Storage Id==>{}"
			 * , DocumentAlias.WC_FINANCIAL_MODEL, toApplicationId, storageId);
			 * balanceSheetDetailRepository.inActiveBalanceSheetDetailByAppId(
			 * toApplicationId); profitibilityStatementDetailRepository.
			 * inActiveProfitibilityStatementDetailByAppId(toApplicationId);
			 * copyCoCMAData(storageId); flag = true; break; }
			 */

			case DocumentAlias.WC_DPR_OUR_FORMAT: {
				logger.info("Going to INactive DocumentAlias.WC_DPR_OUR_FORMAT===>{}===>{}",
						DocumentAlias.WC_DPR_OUR_FORMAT, toApplicationId);
				boardOfDirectorsDetailRepository.inActiveBoardOfDirectorsDetailsByAppId(toApplicationId);
				strategicAlliancesDetailRepository.inActiveStrategicAlliancesDetailsByAppId(toApplicationId);
				keyManagementDetailRepository.inActiveKeyManagementDetailsByAppId(toApplicationId);
				employeesCategoryBreaksDetailRepository.inActiveemployeesCategoryBreaksDetailsByAppId(toApplicationId);
				technologyPositioningDetailRepository.inActiveTechnologyPositioningDetailsByAppId(toApplicationId);
				revenueAndOrderBookDetailRepository.inActiveRevenueAndOrderBookDetailsByAppId(toApplicationId);
				driverForFutureGrowthDetailRepository.inActiveDriverForFutureGrowthDetailsByAppId(toApplicationId);
				projectImplementationScheduleDetailRepository
						.inActiveProjectImplementationScheduleDetailsByAppId(toApplicationId);
				availabilityProposedPlantDetailRepository
						.inActiveAvailabilityProposedPlantDetailsByAppId(toApplicationId);
				requirementsAndAvailabilityRawMaterialsDetailRepository
						.inActiveRequirementsAndAvailabilityRawMaterialsDetailsByAppId(toApplicationId);
				scotAnalysisDetailRepository.inActiveScotDetailsByAppId(toApplicationId);
				dprUserDataDetailRepository.inActiveDprUserDataDetailsByAppId(toApplicationId);
				copyDprData(autoFillOneFormDetailRequest, corporateApplicantDetailTo, userId);
				flag = true;
				break;
			}
			case DocumentAlias.WC_CMA: {
				logger.info(
						"Going to INactive DocumentAlias.WC_DPR_OUR_FORMAT===>{}===>for Application Id==> for Storage Id==>{}",
						DocumentAlias.WC_DPR_OUR_FORMAT, toApplicationId);
				assetsDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				liabilitiesDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				operatingStatementDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				copyCMAData(autoFillOneFormDetailRequest, corporateApplicantDetailTo,userId);
				flag = true;
				break;
			}
			case DocumentAlias.WC_COMPANY_ACT: {
				logger.info(
						"Going to INactive DocumentAlias.WC_COMPANY_ACT===>{}===>for Application Id==> for Storage Id==>{}",
						DocumentAlias.WC_COMPANY_ACT, toApplicationId);
				balanceSheetDetailRepository.inActiveBalanceSheetDetailByAppId(toApplicationId);
				profitibilityStatementDetailRepository.inActiveProfitibilityStatementDetailByAppId(toApplicationId);
				copyCoCMAData(autoFillOneFormDetailRequest, corporateApplicantDetailTo,userId);
				flag = true;
				break;
			}
			/*
			 * case DocumentAlias.TL_FINANCIAL_MODEL: { logger.info(
			 * "Going to INactive DocumentAlias.TL_FINANCIAL_MODEL===>{}===>for Application Id==> for Storage Id==>{}"
			 * , DocumentAlias.TL_FINANCIAL_MODEL, toApplicationId, storageId);
			 * balanceSheetDetailRepository.inActiveBalanceSheetDetailByAppId(
			 * toApplicationId); profitibilityStatementDetailRepository.
			 * inActiveProfitibilityStatementDetailByAppId(toApplicationId);
			 * copyCoCMAData(storageId); flag = true; break; }
			 */

			case DocumentAlias.TL_DPR_OUR_FORMAT: {
				logger.info(
						"Going to INactive DocumentAlias.TL_DPR_OUR_FORMAT===>{}===>for Application Id==> ",
						DocumentAlias.TL_DPR_OUR_FORMAT, toApplicationId);
				boardOfDirectorsDetailRepository.inActiveBoardOfDirectorsDetailsByAppId(toApplicationId);
				strategicAlliancesDetailRepository.inActiveStrategicAlliancesDetailsByAppId(toApplicationId);
				keyManagementDetailRepository.inActiveKeyManagementDetailsByAppId(toApplicationId);
				employeesCategoryBreaksDetailRepository.inActiveemployeesCategoryBreaksDetailsByAppId(toApplicationId);
				technologyPositioningDetailRepository.inActiveTechnologyPositioningDetailsByAppId(toApplicationId);
				revenueAndOrderBookDetailRepository.inActiveRevenueAndOrderBookDetailsByAppId(toApplicationId);
				driverForFutureGrowthDetailRepository.inActiveDriverForFutureGrowthDetailsByAppId(toApplicationId);
				projectImplementationScheduleDetailRepository
						.inActiveProjectImplementationScheduleDetailsByAppId(toApplicationId);
				availabilityProposedPlantDetailRepository
						.inActiveAvailabilityProposedPlantDetailsByAppId(toApplicationId);
				requirementsAndAvailabilityRawMaterialsDetailRepository
						.inActiveRequirementsAndAvailabilityRawMaterialsDetailsByAppId(toApplicationId);
				scotAnalysisDetailRepository.inActiveScotDetailsByAppId(toApplicationId);
				dprUserDataDetailRepository.inActiveDprUserDataDetailsByAppId(toApplicationId);
				copyDprData(autoFillOneFormDetailRequest, corporateApplicantDetailTo,userId);
				flag = true;
				break;
			}
			case DocumentAlias.TL_CMA: {
				logger.info("Going to INactive DocumentAlias.TL_CMA===>{}===>for Application Id==> for Storage Id==>{}",
						DocumentAlias.TL_CMA, toApplicationId);
				assetsDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				liabilitiesDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				operatingStatementDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				copyCMAData(autoFillOneFormDetailRequest, corporateApplicantDetailTo,userId);
				flag = true;
				break;
			}
			case DocumentAlias.TL_COMPANY_ACT: {
				logger.info(
						"Going to INactive DocumentAlias.TL_COMPANY_ACT===>{}===>for Application Id==> for Storage Id==>{}",
						DocumentAlias.TL_COMPANY_ACT, toApplicationId);
				balanceSheetDetailRepository.inActiveBalanceSheetDetailByAppId(toApplicationId);
				profitibilityStatementDetailRepository.inActiveProfitibilityStatementDetailByAppId(toApplicationId);
				copyCoCMAData(autoFillOneFormDetailRequest, corporateApplicantDetailTo,userId);
				flag = true;
				break;
			}
			case DocumentAlias.USL_CMA: {
				logger.info(
						"Going to INactive DocumentAlias.USL_CMA===>{}===>for Application Id==> for Storage Id==>{}",
						DocumentAlias.USL_CMA, toApplicationId);
				assetsDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				liabilitiesDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				operatingStatementDetailsRepository.inActiveAssetsDetailsByAppId(toApplicationId);
				copyCMAData(autoFillOneFormDetailRequest, corporateApplicantDetailTo,userId);
				flag = true;
				break;
			}
			case DocumentAlias.USL_COMPANY_ACT: {
				logger.info(
						"Going to INactive DocumentAlias.USL_COMPANY_ACT===>{}===>for Application Id==> for Storage Id==>{}",
						DocumentAlias.USL_COMPANY_ACT, toApplicationId);
				balanceSheetDetailRepository.inActiveBalanceSheetDetailByAppId(toApplicationId);
				profitibilityStatementDetailRepository.inActiveProfitibilityStatementDetailByAppId(toApplicationId);
				copyCoCMAData(autoFillOneFormDetailRequest, corporateApplicantDetailTo,userId);
				flag = true;
				break;
			}
			default : break;
			}

		} catch (Exception e) {
			logger.error("Error While Uploading Document in Autofill==>{}",e);
		}
		/*if (flag) {
			logger.info("File Uploaded SuccessFully in Autofill");
		} else {
			// code for inactive CMA BS and DPR recored
			JSONObject json = new JSONObject();
			json.put("id", storageId);
			dmsClient.deleteProductDocument(json.toJSONString());
			logger.error("Error While Uploading Document==>{}", json.toJSONString());
		}*/
		logger.info("================ Exit in readAndSaveExcelData() ===========");
	}

	public void copyCMAData(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,  Long userId) {
		logger.info("================ Enter in copyCMAData() ===========");
		Calendar calendar = Calendar.getInstance();
		Integer tillYear = calendar.get(Calendar.YEAR);

		Integer fromYear = tillYear;

		List<String> yearList = new ArrayList<String>();
		yearList.add(--fromYear + "");
		yearList.add(--fromYear + "");
		yearList.add(--fromYear + "");
		List<OperatingStatementDetails> operatingStatementDetailsList = operatingStatementDetailsRepository
				.getOperatingStatementDetailsByApplicationId(autoFillOneFormDetailRequest.getFromApplicationId(),
						yearList, CommonUtils.AUDITED);
		OperatingStatementDetails operatingStatementDetailsTo = null;
		for (OperatingStatementDetails operatingStatementDetailsFrom : operatingStatementDetailsList) {
			operatingStatementDetailsTo = new OperatingStatementDetails();
			BeanUtils.copyProperties(operatingStatementDetailsFrom, operatingStatementDetailsTo, "id", CommonUtils.APPLICATION_ID,
					CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			operatingStatementDetailsTo.setLoanApplicationMaster(corporateApplicantDetailTo.getApplicationId());
			operatingStatementDetailsTo.setStorageDetailsId(null);
			operatingStatementDetailsTo.setModifiedBy(userId);
			operatingStatementDetailsTo.setModifiedDate(new Date());
			operatingStatementDetailsRepository.save(operatingStatementDetailsTo);
		}
		List<LiabilitiesDetails> liabilitiesDetailsList = liabilitiesDetailsRepository
				.getLiabilitiesDetailsByApplicationId(autoFillOneFormDetailRequest.getFromApplicationId(), yearList,
						CommonUtils.AUDITED);
		LiabilitiesDetails liabilitiesDetailsTo = null;
		for (LiabilitiesDetails liabilitiesDetailsFrom : liabilitiesDetailsList) {
			liabilitiesDetailsTo = new LiabilitiesDetails();
			BeanUtils.copyProperties(liabilitiesDetailsFrom, liabilitiesDetailsTo, "id", CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY,
					CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			liabilitiesDetailsTo.setFsLoanApplicationMaster(corporateApplicantDetailTo.getApplicationId());
			liabilitiesDetailsTo.setStorageDetailsId(null);
			liabilitiesDetailsTo.setModifiedBy(userId);
			liabilitiesDetailsTo.setModifiedDate(new Date());
			liabilitiesDetailsRepository.save(liabilitiesDetailsTo);
		}
		List<AssetsDetails> assetsDetailsList = assetsDetailsRepository.getAssetsDetailsByApplicationId(
				autoFillOneFormDetailRequest.getFromApplicationId(), yearList, CommonUtils.AUDITED);
		AssetsDetails assetsDetailsTo = null;
		for (AssetsDetails assetsDetailsFrom : assetsDetailsList) {
			assetsDetailsTo = new AssetsDetails();
			BeanUtils.copyProperties(assetsDetailsFrom, assetsDetailsTo, "id", CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY,
					CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			assetsDetailsTo.setLoanApplicationMaster(corporateApplicantDetailTo.getApplicationId());
			assetsDetailsTo.setModifiedBy(userId);
			assetsDetailsTo.setStorageDetailsId(null);
			assetsDetailsTo.setModifiedDate(new Date());
			assetsDetailsRepository.save(assetsDetailsTo);
		}
		logger.info("================ Exit in copyCMAData() ===========");
	}

	public void copyCoCMAData(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,  Long userId) {
		logger.info("================ Enter in copyCoCMAData() ===========");
		Calendar calendar = Calendar.getInstance();
		Integer tillYear = calendar.get(Calendar.YEAR);

		Integer fromYear = tillYear;
		List<String> yearList = new ArrayList<String>();
		yearList.add(--fromYear + "");
		yearList.add(--fromYear + "");

		yearList.add(--fromYear + "");

		List<ProfitibilityStatementDetail> profitibilityStatementDetailsList = profitibilityStatementDetailRepository
				.getProfitibilityStatementDetailByApplicationId(autoFillOneFormDetailRequest.getFromApplicationId(),
						yearList, CommonUtils.AUDITED);
		ProfitibilityStatementDetail profitibilityStatementDetailTo = null;
		for (ProfitibilityStatementDetail profitibilityStatementDetailFrom : profitibilityStatementDetailsList) {
			profitibilityStatementDetailTo = new ProfitibilityStatementDetail();
			BeanUtils.copyProperties(profitibilityStatementDetailFrom, profitibilityStatementDetailTo, "id",
					CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			profitibilityStatementDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			profitibilityStatementDetailTo.setModifiedBy(userId);
			profitibilityStatementDetailTo.setModifiedDate(new Date());
			profitibilityStatementDetailTo.setStorageDetailsId(null);
			profitibilityStatementDetailRepository.save(profitibilityStatementDetailTo);
		}

		List<BalanceSheetDetail> balanceSheetDetailsList = balanceSheetDetailRepository
				.getBalanceSheetDetailByApplicationId(autoFillOneFormDetailRequest.getFromApplicationId(), yearList,
						CommonUtils.AUDITED);
		BalanceSheetDetail balanceSheetDetailTo = null;
		for (BalanceSheetDetail balanceSheetDetailFrom : balanceSheetDetailsList) {
			balanceSheetDetailTo = new BalanceSheetDetail();
			BeanUtils.copyProperties(balanceSheetDetailFrom, balanceSheetDetailTo, "id", CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY,
					CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			balanceSheetDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			balanceSheetDetailTo.setModifiedBy(userId);
			balanceSheetDetailTo.setModifiedDate(new Date());
			balanceSheetDetailTo.setStorageDetailsId(null);
			balanceSheetDetailRepository.save(balanceSheetDetailTo);
		}
		logger.info("================ Exit in copyCoCMAData() ===========");
	}

	public void copyDprData(AutoFillOneFormDetailRequest autoFillOneFormDetailRequest,
			CorporateApplicantDetail corporateApplicantDetailTo,  Long userId) {
		logger.info("================ Enter in copyDprData() ===========");
		List<BoardOfDirectorsDetail> boardOfDirectorsDetailList = boardOfDirectorsDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		BoardOfDirectorsDetail boardOfDirectorsDetailTo = null;
		for (BoardOfDirectorsDetail boardOfDirectorsDetailFrom : boardOfDirectorsDetailList) {
			boardOfDirectorsDetailTo = new BoardOfDirectorsDetail();
			BeanUtils.copyProperties(boardOfDirectorsDetailFrom, boardOfDirectorsDetailTo, "id", CommonUtils.APPLICATION_ID,
					CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			boardOfDirectorsDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			boardOfDirectorsDetailTo.setModifiedBy(userId);
			boardOfDirectorsDetailTo.setModifiedDate(new Date());
			boardOfDirectorsDetailTo.setStorageDetailsId(null);
			boardOfDirectorsDetailRepository.save(boardOfDirectorsDetailTo);
		}

		List<StrategicAlliancesDetail> strategicAlliancesDetailList = strategicAlliancesDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		StrategicAlliancesDetail strategicAlliancesDetailTo = null;
		for (StrategicAlliancesDetail strategicAlliancesDetailFrom : strategicAlliancesDetailList) {
			strategicAlliancesDetailTo = new StrategicAlliancesDetail();
			BeanUtils.copyProperties(strategicAlliancesDetailFrom, strategicAlliancesDetailTo, "id", CommonUtils.APPLICATION_ID,
					CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			strategicAlliancesDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			strategicAlliancesDetailTo.setModifiedBy(userId);
			strategicAlliancesDetailTo.setModifiedDate(new Date());
			strategicAlliancesDetailTo.setStorageDetailsId(null);
			strategicAlliancesDetailRepository.save(strategicAlliancesDetailTo);
		}

		List<KeyManagementDetail> keyManagementDetailList = keyManagementDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		KeyManagementDetail keyManagementDetailTo = null;
		for (KeyManagementDetail keyManagementDetailFrom : keyManagementDetailList) {
			keyManagementDetailTo = new KeyManagementDetail();
			BeanUtils.copyProperties(keyManagementDetailFrom, keyManagementDetailTo, "id", CommonUtils.APPLICATION_ID,
					CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			keyManagementDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			keyManagementDetailTo.setModifiedBy(userId);
			keyManagementDetailTo.setModifiedDate(new Date());
			keyManagementDetailTo.setStorageDetailsId(null);
			keyManagementDetailRepository.save(keyManagementDetailTo);
		}

		List<EmployeesCategoryBreaksDetail> employeesCategoryBreaksDetailList = employeesCategoryBreaksDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		EmployeesCategoryBreaksDetail employeesCategoryBreaksDetailTo = null;
		for (EmployeesCategoryBreaksDetail employeesCategoryBreaksDetailFrom : employeesCategoryBreaksDetailList) {

			employeesCategoryBreaksDetailTo = new EmployeesCategoryBreaksDetail();
			BeanUtils.copyProperties(employeesCategoryBreaksDetailFrom, employeesCategoryBreaksDetailTo, "id",
					CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			employeesCategoryBreaksDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			employeesCategoryBreaksDetailTo.setModifiedBy(userId);
			employeesCategoryBreaksDetailTo.setModifiedDate(new Date());
			employeesCategoryBreaksDetailTo.setStorageDetailsId(null);
			employeesCategoryBreaksDetailRepository.save(employeesCategoryBreaksDetailTo);
		}

		List<TechnologyPositioningDetail> technologyPositioningDetailList = technologyPositioningDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);

		TechnologyPositioningDetail technologyPositioningDetailTo = null;
		for (TechnologyPositioningDetail technologyPositioningDetailFrom : technologyPositioningDetailList) {

			technologyPositioningDetailTo = new TechnologyPositioningDetail();
			BeanUtils.copyProperties(technologyPositioningDetailFrom, technologyPositioningDetailTo, "id",
					CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			technologyPositioningDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			technologyPositioningDetailTo.setModifiedBy(userId);
			technologyPositioningDetailTo.setModifiedDate(new Date());
			technologyPositioningDetailTo.setStorageDetailsId(null);
			technologyPositioningDetailRepository.save(technologyPositioningDetailTo);

		}

		List<RevenueAndOrderBookDetail> revenueAndOrderBookDetailList = revenueAndOrderBookDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		RevenueAndOrderBookDetail revenueAndOrderBookDetailTo = null;
		for (RevenueAndOrderBookDetail revenueAndOrderBookDetailFrom : revenueAndOrderBookDetailList) {
			revenueAndOrderBookDetailTo = new RevenueAndOrderBookDetail();
			BeanUtils.copyProperties(revenueAndOrderBookDetailFrom, revenueAndOrderBookDetailTo, "id", CommonUtils.APPLICATION_ID,
					CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			revenueAndOrderBookDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			revenueAndOrderBookDetailTo.setModifiedBy(userId);
			revenueAndOrderBookDetailTo.setModifiedDate(new Date());
			revenueAndOrderBookDetailTo.setStorageDetailsId(null);
			revenueAndOrderBookDetailRepository.save(revenueAndOrderBookDetailTo);
		}

		List<DriverForFutureGrowthDetail> driverForFutureGrowthDetailList = driverForFutureGrowthDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		DriverForFutureGrowthDetail driverForFutureGrowthDetailTo = null;
		for (DriverForFutureGrowthDetail driverForFutureGrowthDetailFrom : driverForFutureGrowthDetailList) {
			driverForFutureGrowthDetailTo = new DriverForFutureGrowthDetail();
			BeanUtils.copyProperties(driverForFutureGrowthDetailFrom, driverForFutureGrowthDetailTo, "id",
					CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			driverForFutureGrowthDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			driverForFutureGrowthDetailTo.setModifiedBy(userId);
			driverForFutureGrowthDetailTo.setModifiedDate(new Date());
			driverForFutureGrowthDetailTo.setStorageDetailsId(null);
			driverForFutureGrowthDetailRepository.save(driverForFutureGrowthDetailTo);
		}

		List<AvailabilityProposedPlantDetail> availabilityProposedPlantDetailList = availabilityProposedPlantDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		AvailabilityProposedPlantDetail availabilityProposedPlantDetailTo = null;
		for (AvailabilityProposedPlantDetail availabilityProposedPlantDetailFrom : availabilityProposedPlantDetailList) {
			availabilityProposedPlantDetailTo = new AvailabilityProposedPlantDetail();
			BeanUtils.copyProperties(availabilityProposedPlantDetailFrom, availabilityProposedPlantDetailTo, "id",
					CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			availabilityProposedPlantDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			availabilityProposedPlantDetailTo.setModifiedBy(userId);
			availabilityProposedPlantDetailTo.setModifiedDate(new Date());
			availabilityProposedPlantDetailTo.setStorageDetailsId(null);
			availabilityProposedPlantDetailRepository.save(availabilityProposedPlantDetailTo);
		}

		List<ProjectImplementationScheduleDetail> projectImplementationScheduleDetailList = projectImplementationScheduleDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		ProjectImplementationScheduleDetail projectImplementationScheduleDetailTo = null;

		for (ProjectImplementationScheduleDetail projectImplementationScheduleDetailFrom : projectImplementationScheduleDetailList) {
			projectImplementationScheduleDetailTo = new ProjectImplementationScheduleDetail();
			BeanUtils.copyProperties(projectImplementationScheduleDetailFrom, projectImplementationScheduleDetailTo,
					"id", CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY, CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			projectImplementationScheduleDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			projectImplementationScheduleDetailTo.setModifiedBy(userId);
			projectImplementationScheduleDetailTo.setModifiedDate(new Date());
			projectImplementationScheduleDetailTo.setStorageDetailsId(null);
			projectImplementationScheduleDetailRepository.save(projectImplementationScheduleDetailTo);
		}

		List<RequirementsAndAvailabilityRawMaterialsDetail> requirementsAndAvailabilityRawMaterialsDetailList = requirementsAndAvailabilityRawMaterialsDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		RequirementsAndAvailabilityRawMaterialsDetail requirementsAndAvailabilityRawMaterialsDetailTo = null;
		for (RequirementsAndAvailabilityRawMaterialsDetail requirementsAndAvailabilityRawMaterialsDetailFrom : requirementsAndAvailabilityRawMaterialsDetailList) {
			requirementsAndAvailabilityRawMaterialsDetailTo = new RequirementsAndAvailabilityRawMaterialsDetail();
			BeanUtils.copyProperties(requirementsAndAvailabilityRawMaterialsDetailFrom,
					requirementsAndAvailabilityRawMaterialsDetailTo, "id", CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY,
					CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			requirementsAndAvailabilityRawMaterialsDetailTo
					.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			requirementsAndAvailabilityRawMaterialsDetailTo.setModifiedBy(userId);
			requirementsAndAvailabilityRawMaterialsDetailTo.setModifiedDate(new Date());
			requirementsAndAvailabilityRawMaterialsDetailTo.setStorageDetailsId(null);
			requirementsAndAvailabilityRawMaterialsDetailRepository
					.save(requirementsAndAvailabilityRawMaterialsDetailTo);
		}
		List<ScotAnalysisDetail> scotAnalysisDetailList = scotAnalysisDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		ScotAnalysisDetail scotAnalysisDetailTo = null;
		for (ScotAnalysisDetail scotAnalysisDetailFrom : scotAnalysisDetailList) {
			scotAnalysisDetailTo = new ScotAnalysisDetail();
			BeanUtils.copyProperties(scotAnalysisDetailFrom, scotAnalysisDetailTo, "id", CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY,
					CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			scotAnalysisDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			scotAnalysisDetailTo.setModifiedBy(userId);
			scotAnalysisDetailTo.setModifiedDate(new Date());
			scotAnalysisDetailTo.setStorageDetailsId(null);
			scotAnalysisDetailRepository.save(scotAnalysisDetailTo);
		}

		List<DprUserDataDetail> dprUserDataDetailList = dprUserDataDetailRepository
				.findByApplicationIdIdAndIsActive(autoFillOneFormDetailRequest.getFromApplicationId(), true);
		DprUserDataDetail dprUserDataDetailTo = null;
		for (DprUserDataDetail dprUserDataDetailFrom : dprUserDataDetailList) {
			dprUserDataDetailTo = new DprUserDataDetail();
			BeanUtils.copyProperties(dprUserDataDetailFrom, dprUserDataDetailTo, "id", CommonUtils.APPLICATION_ID, CommonUtils.MODIFIED_BY,
					CommonUtils.MODIFIED_DATE, CommonUtils.STORAGE_DETAILS_ID);
			dprUserDataDetailTo.setApplicationId(corporateApplicantDetailTo.getApplicationId());
			dprUserDataDetailTo.setStorageDetailsId(null);
			dprUserDataDetailTo.setModifiedBy(userId);
			dprUserDataDetailTo.setModifiedDate(new Date());
			dprUserDataDetailRepository.save(dprUserDataDetailTo);
		}
		logger.info("================ Exit in copyCMAData() ===========");
	}


}
