package com.opl.service.loans.service.fundseeker.corporate.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.opl.cvl.enums.cvl.VehicleBuildType;
import com.opl.cvl.enums.cvl.VehicleModelType;
import com.opl.cvl.enums.cvl.VehicleSegment;
import com.opl.cvl.enums.cvl.VehicleType;
import com.opl.mudra.api.eligibility.model.CVLEligibilityRequest;
import com.opl.mudra.api.loans.model.*;
import com.opl.service.loans.repository.VehicleOperatorDetailRepository;
import com.opl.service.loans.service.vehicledetails.VehicleOperatorService;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.tools.generic.NumberTool;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.capitaworld.service.fitchengine.model.manufacturing.FitchOutputManu;
import com.capitaworld.service.fitchengine.model.service.FitchOutputServ;
import com.capitaworld.service.fitchengine.model.trading.FitchOutputTrad;
import com.capitaworld.service.fitchengine.utils.CommonUtils.BusinessType;
import com.capitaworld.service.pennydrop.client.PennydropClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.opl.api.pennydrop.model.CommonResponse;
import com.opl.mudra.api.analyzer.model.common.AnalyzerResponse;
import com.opl.mudra.api.analyzer.model.common.Data;
import com.opl.mudra.api.analyzer.model.common.ManualBsReportRequest;
import com.opl.mudra.api.analyzer.model.common.ReportRequest;
import com.opl.mudra.api.cibil.model.CibilRequest;
import com.opl.mudra.api.cibil.model.CibilScoreLogRequest;
import com.opl.mudra.api.common.CommonUtils.LoanType;
import com.opl.mudra.api.connect.ConnectRequest;
import com.opl.mudra.api.connect.ConnectResponse;
import com.opl.mudra.api.eligibility.model.CalculationJSON;
import com.opl.mudra.api.eligibility.model.EligibililityRequest;
import com.opl.mudra.api.eligibility.model.EligibilityResponse;
import com.opl.mudra.api.fraudanalytics.model.AnalyticsResponse;
import com.opl.mudra.api.gst.model.GstCalculation;
import com.opl.mudra.api.gst.model.GstResponse;
import com.opl.mudra.api.gst.model.MomSales;
import com.opl.mudra.api.gst.model.MonthListSales;
import com.opl.mudra.api.gst.model.model.CAMGSTData;
import com.opl.mudra.api.gst.model.yuva.request.GSTR1Request;
import com.opl.mudra.api.itr.model.ITRConnectionResponse;
import com.opl.mudra.api.loans.model.CAM.AssetDetailsString;
import com.opl.mudra.api.loans.model.CAM.FinancialInputRequestDbl;
import com.opl.mudra.api.loans.model.CAM.FinancialInputRequestString;
import com.opl.mudra.api.loans.model.CAM.LiabilitiesDetailsString;
import com.opl.mudra.api.loans.model.CAM.OperatingStatementDetailsString;
import com.opl.mudra.api.loans.model.common.CGTMSECalcDataResponse;
import com.opl.mudra.api.loans.model.corporate.CorporateApplicantRequest;
import com.opl.mudra.api.loans.model.corporate.CorporateFinalInfoRequest;
import com.opl.mudra.api.loans.model.corporate.MachineDetailMudraLoanRequestResponse;
import com.opl.mudra.api.loans.model.corporate.PrimaryCorporateDetailMudraLoanReqRes;
import com.opl.mudra.api.loans.model.corporate.PrimaryCorporateRequest;
import com.opl.mudra.api.loans.model.corporate.TotalCostOfProjectRequest;
import com.opl.mudra.api.loans.model.retail.BankRelationshipRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.matchengine.model.MatchDisplayResponse;
import com.opl.mudra.api.matchengine.model.MatchRequest;
import com.opl.mudra.api.matchengine.model.ProposalMappingRequest;
import com.opl.mudra.api.matchengine.model.ProposalMappingRequestString;
import com.opl.mudra.api.matchengine.model.ProposalMappingResponse;
import com.opl.mudra.api.mca.McaResponse;
import com.opl.mudra.api.mca.verifyApi.VerifyAPIRequest;
import com.opl.mudra.api.oneform.enums.AssessedForITMst;
import com.opl.mudra.api.oneform.enums.CertificationCourseMst;
import com.opl.mudra.api.oneform.enums.CompetitionMst_SBI;
import com.opl.mudra.api.oneform.enums.Constitution;
import com.opl.mudra.api.oneform.enums.Denomination;
import com.opl.mudra.api.oneform.enums.DirectorRelationshipType;
import com.opl.mudra.api.oneform.enums.EducationalStatusMudraMst;
import com.opl.mudra.api.oneform.enums.EstablishmentMonths;
import com.opl.mudra.api.oneform.enums.FSParameterMst;
import com.opl.mudra.api.oneform.enums.FactoryPremiseMst;
import com.opl.mudra.api.oneform.enums.FinanceCategory;
import com.opl.mudra.api.oneform.enums.Gender;
import com.opl.mudra.api.oneform.enums.GovSchemesMst;
import com.opl.mudra.api.oneform.enums.HaveLIMst;
import com.opl.mudra.api.oneform.enums.IdProofMst;
import com.opl.mudra.api.oneform.enums.Industry;
import com.opl.mudra.api.oneform.enums.KnowHowMst;
import com.opl.mudra.api.oneform.enums.LCBG_Status_SBI;
import com.opl.mudra.api.oneform.enums.MaritalStatusMst;
import com.opl.mudra.api.oneform.enums.MrktArrFinishedGoodsList;
import com.opl.mudra.api.oneform.enums.MudraOwningHouseMst;
import com.opl.mudra.api.oneform.enums.NoOfEmployees;
import com.opl.mudra.api.oneform.enums.OngoingMudraLoan;
import com.opl.mudra.api.oneform.enums.Particular;
import com.opl.mudra.api.oneform.enums.PurposeOfLoan;
import com.opl.mudra.api.oneform.enums.RegistrationWithGovernmentAuthoritiesList;
import com.opl.mudra.api.oneform.enums.ResidentStatusMst;
import com.opl.mudra.api.oneform.enums.ShareHoldingCategory;
import com.opl.mudra.api.oneform.enums.SpouseDetailMst;
import com.opl.mudra.api.oneform.enums.Title;
import com.opl.mudra.api.oneform.enums.VisuallyImpairedMst;
import com.opl.mudra.api.oneform.enums.WcRenewalType;
import com.opl.mudra.api.oneform.model.MasterResponse;
import com.opl.mudra.api.oneform.model.OneFormResponse;
import com.opl.mudra.api.oneform.model.SectorIndustryModel;
import com.opl.mudra.api.rating.model.FinancialInputRequest;
import com.opl.mudra.api.rating.model.RatingResponse;
import com.opl.mudra.api.scoring.model.ProposalScoreDetailResponse;
import com.opl.mudra.api.scoring.model.ProposalScoreResponse;
import com.opl.mudra.api.scoring.model.ScoringRequest;
import com.opl.mudra.api.scoring.model.ScoringResponse;
import com.opl.mudra.api.thirdparty.model.CGTMSEDataResponse;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.api.workflow.model.WorkflowRequest;
import com.opl.mudra.api.workflow.model.WorkflowResponse;
import com.opl.mudra.api.workflow.utils.WorkflowUtils;
import com.opl.mudra.client.analyzer.AnalyzerClient;
import com.opl.mudra.client.cibil.CIBILClient;
import com.opl.mudra.client.connect.ConnectClient;
import com.opl.mudra.client.dms.DMSClient;
import com.opl.mudra.client.eligibility.EligibilityClient;
import com.opl.mudra.client.fraudanalytics.FraudAnalyticsClient;
import com.opl.mudra.client.gst.GstClient;
import com.opl.mudra.client.itr.ITRClient;
import com.opl.mudra.client.matchengine.MatchEngineClient;
import com.opl.mudra.client.matchengine.ProposalDetailsClient;
import com.opl.mudra.client.mca.McaClient;
import com.opl.mudra.client.oneform.OneFormClient;
import com.opl.mudra.client.reports.ReportsClient;
import com.opl.mudra.client.scoring.ScoringClient;
import com.opl.mudra.client.thirdparty.ThirdPartyClient;
import com.opl.mudra.client.users.UsersClient;
import com.opl.mudra.client.workflow.WorkflowClient;
import com.opl.service.loans.domain.fundprovider.ProposalDetails;
import com.opl.service.loans.domain.fundprovider.TermLoanParameter;
import com.opl.service.loans.domain.fundprovider.WcTlParameter;
import com.opl.service.loans.domain.fundprovider.WorkingCapitalParameter;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.IneligibleProposalDetails;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.AssetsDetails;
import com.opl.service.loans.domain.fundseeker.corporate.AssociatedConcernDetail;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.opl.service.loans.domain.fundseeker.corporate.LiabilitiesDetails;
import com.opl.service.loans.domain.fundseeker.corporate.MachineDetailMudraLoan;
import com.opl.service.loans.domain.fundseeker.corporate.OperatingStatementDetails;
import com.opl.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.opl.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetailMudraLoan;
import com.opl.service.loans.domain.fundseeker.retail.BankingRelation;
import com.opl.service.loans.repository.common.CommonRepository;
import com.opl.service.loans.repository.common.LoanRepository;
import com.opl.service.loans.repository.common.impl.LoanRepositoryImpl;
import com.opl.service.loans.repository.fundprovider.FSParameterMappingRepository;
import com.opl.service.loans.repository.fundprovider.ProductMasterRepository;
import com.opl.service.loans.repository.fundprovider.ProposalDetailsRepository;
import com.opl.service.loans.repository.fundprovider.TermLoanParameterRepository;
import com.opl.service.loans.repository.fundprovider.WcTlLoanParameterRepository;
import com.opl.service.loans.repository.fundprovider.WorkingCapitalParameterRepository;
import com.opl.service.loans.repository.fundseeker.IneligibleProposalDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.opl.service.loans.repository.fundseeker.corporate.AssetsDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.AssociatedConcernDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LiabilitiesDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.corporate.MachineDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.OperatingStatementDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailMudraLoanRepository;
import com.opl.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.opl.service.loans.repository.fundseeker.retail.BankingRelationlRepository;
import com.opl.service.loans.service.common.CommonService;
import com.opl.service.loans.service.common.PincodeDateService;
import com.opl.service.loans.service.fundseeker.corporate.AchievmentDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.AssociatedConcernDetailService;
import com.opl.service.loans.service.fundseeker.corporate.CamReportPdfDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.CollateralSecurityDetailService;
import com.opl.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.opl.service.loans.service.fundseeker.corporate.CorporateFinalInfoService;
import com.opl.service.loans.service.fundseeker.corporate.DirectorBackgroundDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.ExistingProductDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FinanceMeansDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.GuarantorsCorporateDetailService;
import com.opl.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.opl.service.loans.service.fundseeker.corporate.MonthlyTurnoverDetailService;
import com.opl.service.loans.service.fundseeker.corporate.OwnershipDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.PrimaryCorporateService;
import com.opl.service.loans.service.fundseeker.corporate.PromotorBackgroundDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.ProposedProductDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.SecurityCorporateDetailsService;
import com.opl.service.loans.service.fundseeker.corporate.TotalCostOfProjectService;
import com.opl.service.loans.service.irr.IrrService;
import com.opl.service.loans.service.scoring.ScoringService;
import com.opl.service.loans.service.teaser.primaryview.CorporatePrimaryViewService;
import com.opl.service.loans.utils.BanksEnumForReports;
import com.opl.service.loans.utils.DDRMultipart;

@Service
@Transactional
public class CamReportPdfDetailsServiceImpl implements CamReportPdfDetailsService{

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;
	
	@Autowired
	private ApplicationProposalMappingRepository applicationProposalMappingRepository;

	@Autowired
	private IrrService irrService; 
	
	@Autowired
	private MatchEngineClient matchEngineClient; 
	
	@Autowired
	private ScoringClient scoringClient;
	
	@Autowired
	private AnalyzerClient analyzerClient;
	
	@Autowired
	private CorporateApplicantService corporateApplicantService;
	
	@Autowired
	private PrimaryCorporateService primaryCorporateService;
	
	@Autowired
	private CorporateFinalInfoService corporateFinalInfoService;
	
	@Autowired
	private LoanApplicationService loanApplicationService;
	
	@Autowired
	private OneFormClient oneFormClient;
	
	@Autowired 
	private PromotorBackgroundDetailsService promotorBackgroundDetailsService;
	
	@Autowired
	private OwnershipDetailsService ownershipDetailsService;
	
	@Autowired
	private ProposedProductDetailsService proposedProductDetailsService; 
	
	@Autowired
	private ExistingProductDetailsService existingProductDetailsService;
	
	@Autowired
	private AssociatedConcernDetailService associatedConcernDetailService;
	
	@Autowired
	private FinancialArrangementDetailsService financialArrangementDetailsService;
	
	@Autowired
	private AchievmentDetailsService achievmentDetailsService;
	
	@Autowired
	private DirectorBackgroundDetailsService backgroundDetailsService;
	
	@Autowired
	private ProposalDetailsClient proposalDetailsClient;
	
	@Autowired
	private UsersClient usersClient;
	
	@Autowired
	private CIBILClient cibilClient;
	
	@Autowired
	private GstClient gstClient;
	
	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;
	
	@Autowired
	private GuarantorsCorporateDetailService guarantorsCorporateDetailService;

	@Autowired
	private MonthlyTurnoverDetailService monthlyTurnoverDetailService;
	
	@Autowired
	private TotalCostOfProjectService costOfProjectService; 
	
	@Autowired
	private FinanceMeansDetailsService financeMeansDetailsService;
	
	@Autowired
	private SecurityCorporateDetailsService securityCorporateDetailsService;
	
	@Autowired
	private ThirdPartyClient thirdPartyClient;
	
	@Autowired
	private OperatingStatementDetailsRepository operatingStatementDetailsRepository;
	
	@Autowired
	private LiabilitiesDetailsRepository liabilitiesDetailsRepository;
	
	@Autowired
	private AssetsDetailsRepository assetsDetailsRepository;
	
	@Autowired
	private EligibilityClient eligibilityClient;
	
	@Autowired
	private WorkflowClient workflowClient;
	
	@Autowired
	private ConnectClient connectClient;
	
	@Autowired
	private McaClient mcaClient;
	
	@Autowired
	private FraudAnalyticsClient fraudAnalyticsClient;
	
	@Autowired
	private TermLoanParameterRepository termLoanParameterRepository;
	
	@Autowired
	private WorkingCapitalParameterRepository workingCapitalParameterRepository;
	
	@Autowired
	private WcTlLoanParameterRepository wcTlLoanParameterRepository;
	
	@Autowired
	private ITRClient itrClient;
	
	@Autowired
	private ScoringService scoringService;
	
	@Autowired
	private PincodeDateService pincodeDateService;
	
	@Autowired
	private PrimaryCorporateDetailRepository primaryCorporateRepository;
	
	@Autowired
	private ProductMasterRepository productMasterRepository;
	
	@Autowired
	private CollateralSecurityDetailService collateralSecurityDetailService;

	@Autowired
	ProposalDetailsRepository proposalDetailsRepository;

	@Autowired
	IneligibleProposalDetailsRepository ineligibleProposalDetailsRepository;
	
	@Autowired
	CorporatePrimaryViewService corporatePrimaryViewService;
	
	@Autowired
	private CommonRepository commonRepository;
	
	@Autowired
	private PrimaryCorporateDetailMudraLoanRepository mudraLoanRepo ;
        
    @Autowired
    private FinancialArrangementDetailsService arrangementsDetailServiceImpl;
        
    @Autowired
    private LoanRepositoryImpl loanRepoImpl;
    
    @Autowired
    private FSParameterMappingRepository fsParameterMappingRepository;
    
    @Autowired
	private CorporateApplicantService applicantService;
    
    @Autowired
	MachineDetailsRepository machineDetailsRepo;
    
    @Autowired
	private CommonService commonService;
    
    @Autowired
	AssociatedConcernDetailRepository associatedConcernDetailRepository;
    
    @Autowired
	BankingRelationlRepository bankingRelationlRepository;
    
    @Autowired
    private PrimaryCorporateDetailMudraLoanRepository primaryCorporateDetailsMudra;
    
    @Autowired
    private PennydropClient pennyDropClient;
    
    @Autowired
	private ReportsClient reportsClient;
        
    @Autowired
    private DMSClient dmsClient;
    
    @Autowired
    private LoanRepository loanRepository;

	@Autowired
	VehicleOperatorDetailRepository vehicleOperatorDetailRepository;

	@Autowired
	VehicleOperatorService vehicleOperatorService;

	private static final Logger logger = LoggerFactory.getLogger(CamReportPdfDetailsServiceImpl.class);
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

	private static final String ASSESSMENT_ID = "assessmentId";
	private static final String FITCH_RESPONSE = "fitchResponse";
	private static final String FITCH_TITLE = "fitchTitle";

	@Override
	public Map<String, Object> getCamReportPrimaryDetails(Long applicationId, Long productId,Long proposalId, boolean isFinalView) {

		ProposalMappingRequestString proposalMappingRequestString = null;
		Object[] profileVersionDetails = loanRepository.getProfileVersionDetailsByApplicationId(applicationId);
		if(CommonUtils.isObjectNullOrEmpty(profileVersionDetails)) {
			logger.error("Profile not found for applicationId =======>"+applicationId);
			return null;
		}
		Long itrId = profileVersionDetails[0] != null ? Long.valueOf(profileVersionDetails[0].toString()) : null;
		Long gstId = profileVersionDetails[1] != null ? Long.valueOf(profileVersionDetails[1].toString()) : null;
		Long bsId =  profileVersionDetails[2] != null ? Long.valueOf(profileVersionDetails[2].toString()) : null;
		Map<String, Object> map = new HashMap<String, Object>();
		
		Long userOrgId = proposalDetailsRepository.getOrgIdByProposalId(proposalId);
		ProposalDetails proposalDetails = proposalDetailsRepository.getSanctionProposalByApplicationIdAndUserOrgId(applicationId, userOrgId);
		IneligibleProposalDetails ineligibleProposalDetails = ineligibleProposalDetailsRepository.getSanctionedByApplicationIdAndOrgId(applicationId, userOrgId);

		if (!CommonUtils.isObjectNullOrEmpty(proposalDetails) || !CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails)) {
			map.put("isSanctioned", "true");
		}

		DecimalFormat decim = new DecimalFormat("####");

        // CHANGES FOR NEW MULTIPLE BANKS----->
        ApplicationProposalMapping applicationProposalMapping = null;
        if(proposalId != null) {
        	applicationProposalMapping = applicationProposalMappingRepository.getByApplicationIdAndProposalId(applicationId, proposalId);
        }else {
        	applicationProposalMapping = applicationProposalMappingRepository.getByApplicationId(applicationId);
		}
       
        Long toApplicationId = applicationProposalMapping.getApplicationId();
        Long userId     =  applicationProposalMapping.getUserId();

        //Long userId = loanApplicationRepository.getUserIdByApplicationId(toApplicationId);  // PREVIOUS
        // ENDS HERE MULTIPLE BANK----->

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
						proposedVehicleRequest.toString().replaceAll("[\\[\\]]", "");
						proposedVehicleRequest.setVehicleMake(manufacturer);
					}
				}
			}
			map.put("vehicleOperateIn", vehicleOperatorRequest.getVehicleOperateIn().toString().replaceAll("[\\[\\]]", ""));
			map.put("vehicleOperatorDetails", vehicleOperatorRequest);
		}

		// Vehicle Operator Code ends here

        //new loan type based on proposal mapping
        map.put("loanType", !CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getProductId()) ? CommonUtils.LoanType.getType(applicationProposalMapping.getProductId()).getName() : " ");
        
        //CHANGES====>
        LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.getByIdAndUserId(toApplicationId, userId);
        if(applicationProposalMapping != null) {
            map.put("applicationCode", applicationProposalMapping.getApplicationCode());
            map.put("date",!CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getApprovedDate())? simpleDateFormat.format(applicationProposalMapping.getApprovedDate()):"-");
            map.put("isMcqSkipped", applicationProposalMapping.getIsMcqSkipped() != null ? applicationProposalMapping.getIsMcqSkipped() : false);
        }
        
        map.put("numberTool", new NumberTool());
   
        //Bank Details for MSME added.
        map.put("bankDetails", getBranchDetails(applicationId, userId, proposalId));
		//Bank Details added.
        
        CorporateApplicantRequest corporateApplicantRequest =corporateApplicantService.getCorporateApplicant(toApplicationId);
        UserResponse userResponse = usersClient.getEmailMobile(userId);
        if(userResponse != null) {
            LinkedHashMap<String, Object> lm = (LinkedHashMap<String, Object>)userResponse.getData();
            try {
                UsersRequest request = MultipleJSONObjectHelper.getObjectFromMap(lm,UsersRequest.class);
	                map.put("mobile", request.getMobile());
	                map.put("email", StringEscapeUtils.escapeXml(request.getEmail()));
                if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getPanNo()) && corporateApplicantRequest.getPanNo() != null) {
                	 BigInteger isEmailMobileFound = commonRepository.checkApplicationDisbursed(corporateApplicantRequest.getPanNo());
         			String msg;
         			if(Integer.parseInt(String.valueOf(isEmailMobileFound)) > 0){
         				msg="SIDBI has already disbursed to this Customer from PSB59 Platform.";
         			}else {
         				msg="SIDBI has not disbursed any funds to this Customer from PSB59 Platform.";
         			}
         			map.put("messageFromUsers", msg);
				}
               
            } catch (IOException e1) {
                logger.error("Error while getting registration details : ",e1);
            }
        }

        try {
			GSTR1Request gstr1Request = new GSTR1Request();
			gstr1Request.setApplicationId(gstId);
			gstr1Request.setUserId(userId);
			gstr1Request.setGstin(corporateApplicantRequest.getGstIn() != null ? corporateApplicantRequest.getGstIn() : commonRepository.getGSTInFromConnectWithApplicationIdAndProposalId(applicationId));
			GstResponse response = gstClient.getCalculations(gstr1Request);
			GstCalculation gstData = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)response.getData(),GstCalculation.class);
			int noOfCustomer = gstData.getNoOfCustomer().intValue();
			map.put("noOfCustomer", noOfCustomer);
			map.put("projectedSales", CommonUtils.convertValueIndianCurrency(gstData.getProjectedSales()));
			map.put("customerConcentration", CommonUtils.convertValue(gstData.getConcentration()));
		}catch(Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
     // MUDRA LOAN DETAILS
        try {
			PrimaryCorporateDetailMudraLoan mlDetail = 	mudraLoanRepo.findFirstByApplicationIdAndApplicationProposalMappingProposalIdOrderByIdDesc(applicationId, proposalId); 
			if (!CommonUtils.isObjectNullOrEmpty(mlDetail)) {			
				PrimaryCorporateDetailMudraLoanReqRes mlDetailsRes = new PrimaryCorporateDetailMudraLoanReqRes();
				BeanUtils.copyProperties(mlDetail, mlDetailsRes);
				if (!CommonUtils.isObjectNullOrEmpty(mlDetail.getMrktArragementFinishedGoods())) {					
					mlDetailsRes.setMrktArragementFinishedGoodsValue(MrktArrFinishedGoodsList.fromId(mlDetail.getMrktArragementFinishedGoods()).getValue());
				}
				//corporatePrimaryViewResponse.setMlDetail(mlDetailsRes);
				map.put("mlDetail", !CommonUtils.isObjectNullOrEmpty(mlDetailsRes) ? CommonUtils.printFields(mlDetailsRes , null) : Collections.EMPTY_LIST);
			}
        }catch (Exception e) {
			logger.error("Error/Exception mlDetail==>{}",e);
		}
        
        // GET MACHINE DETAILS
        try {
			List<MachineDetailMudraLoan> machineDetails = machineDetailsRepo.findByApplicationIdAndIsActive(toApplicationId, true);
			PrimaryCorporateDetailMudraLoanReqRes mlDetailsRes = new PrimaryCorporateDetailMudraLoanReqRes();
			if (!CommonUtils.isListNullOrEmpty(machineDetails)) {
				List<MachineDetailMudraLoanRequestResponse> machineDetailsRes =  new ArrayList<>(machineDetails.size());
				for (MachineDetailMudraLoan machineDetailMudraLoan : machineDetails) {
					MachineDetailMudraLoanRequestResponse machineDetail = new MachineDetailMudraLoanRequestResponse(); 
					BeanUtils.copyProperties(machineDetailMudraLoan, machineDetail);
					if(machineDetail != null && machineDetail.getCostOfMachinery() != null) {
						BigDecimal convertedVal = BigDecimal.valueOf(machineDetail.getCostOfMachinery()).setScale(2);
						machineDetail.setCostOfMachineryString(convertedVal.toString());
					}
					machineDetail.setNameOfSupplier(StringEscapeUtils.escapeXml(machineDetailMudraLoan.getNameOfSupplier()));
					machineDetailsRes.add(machineDetail);
				}				
				mlDetailsRes.setMachineDetails(machineDetailsRes);
			}
			map.put("machineDetailsMudra", CommonUtils.printFields(mlDetailsRes , null));
        }catch (Exception e) {
			logger.error("Error/Exception machineDetailsMudra==>{}",e);
		}
		
        map.putAll(getManualBankStatementData(applicationId , bsId));
        
		//get NO BS+ DATA
//		CommonResponse verificationrequestResponse = null;
//		try {
//			if(!CommonUtils.isObjectNullOrEmpty(applicationId)) {
//				verificationrequestResponse =  pennyDropClient.getAccountDetails(applicationId);
//				LinkedHashMap<String, Object> noBs = (LinkedHashMap<String, Object>) verificationrequestResponse.getData();	
//				String year = (String) noBs.get("sinceYear");
//				String months = (String) noBs.get("sinceMonth");
//				if (!CommonUtils.isObjectNullOrEmpty(year) || !CommonUtils.isObjectNullOrEmpty(months)) {
//					LocalDate today = LocalDate.now();
//					LocalDate since = LocalDate.of(Integer.parseInt(year), Integer.parseInt(months),1);
//					Period age = Period.between(since, today);
//					map.put("noBsSinceYear", age.getYears());
//					map.put("noBsSinceMonths", age.getMonths());
//					map.put("noBsSinceWhen", (!CommonUtils.isObjectNullOrEmpty(age.getYears()) ? age.getYears() +" year " : "") + " " +(!CommonUtils.isObjectNullOrEmpty(age.getMonths()) ? age.getMonths()+" months" :  "" ));
//				}
//				map.put("noBsData", verificationrequestResponse);
//			}
//			
//		} catch (Exception e) {
//			logger.error("Error while fetching Account data From PennyDrop : ",e);			
//		}
		
		// Product Name
		if(productId != null) {
			String productName = productMasterRepository.getFpProductName(productId);
			map.put("fpProductName",productName != null ? StringEscapeUtils.escapeXml(productName) : " ");
		}else {
			logger.info("fpProductMapping id is null..");
		}
		
		// application type
		map.put("applicationType", (loanApplicationMaster.getWcRenewalStatus() != null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue() : "New" ));

		//TIMELINE DATES  date of is now change again it is consider at the time of mcq page selection time ----
		map.put("dateOfProposal", !CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getCreatedDate())? CommonUtils.DATE_FORMAT.format(loanApplicationMaster.getCreatedDate()):"-");
		
		try {
			WorkflowRequest workflowRequest = new WorkflowRequest();
			workflowRequest.setApplicationId(toApplicationId);
			workflowRequest.setWorkflowId(WorkflowUtils.Workflow.DDR);
			workflowRequest.setUserId(userId);
			WorkflowResponse auditTrail = workflowClient.getAuditTrail(workflowRequest);
			if(!CommonUtils.isObjectNullOrEmpty(auditTrail)) {
				map.put("trailDates", CommonUtils.printFields(auditTrail.getData() , null));
			}
		} catch (Exception e2) {
			logger.error(CommonUtils.EXCEPTION,e2);
		}
		
		// FOR dateOfInPrincipalApproval  NEW FOR MULTIPLE BANK CONNECT MODIFIED DATE
		try {
			ConnectRequest response = null;
			ConnectResponse connectResponse = connectClient.getApplicationList(toApplicationId);
			if (!CommonUtils.isObjectNullOrEmpty(connectResponse) && !CommonUtils.isListNullOrEmpty(connectResponse.getDataList())) {
				List<LinkedHashMap<String, Object>> list = (List<LinkedHashMap<String, Object>>) connectResponse.getDataList();
				for (LinkedHashMap<String, Object> mp : list) {
					response = MultipleJSONObjectHelper.getObjectFromMap(mp, ConnectRequest.class);
					if (response.getProposalId().equals(proposalId)) {
						Date inPrincipleDate = response.getInPrincipleDate();
						map.put("dateOfInPrincipalApproval", !CommonUtils.isObjectNullOrEmpty(inPrincipleDate)? CommonUtils.DATE_FORMAT.format(inPrincipleDate) : "-");
					}
				}
			}
		} catch (Exception e2) {
			logger.error(CommonUtils.EXCEPTION,e2);
		}
			
		// Currently Commented  dateOfInPrincipalApproval from
		/**try { 
		//ConnectResponse connectResponse = connectClient.getByAppStageBusinessTypeId(applicationId, ConnectStage.COMPLETE.getId(), com.capitaworld.service.loans.utils.CommonUtils.BusinessType.EXISTING_BUSINESS.getId());
		Date InPrincipleDate=loanApplicationRepository.getModifiedDate(toApplicationId, ConnectStage.COMPLETE.getId(), com.capitaworld.service.loans.utils.CommonUtils.BusinessType.EXISTING_BUSINESS.getId());
		if(!CommonUtils.isObjectNullOrEmpty(InPrincipleDate)) {
			map.put("dateOfInPrincipalApproval",!CommonUtils.isObjectNullOrEmpty(InPrincipleDate)? CommonUtils.DATE_FORMAT.format(InPrincipleDate):"-");
		}
		} catch (Exception e2) {
			logger.error(CommonUtils.EXCEPTION,e2);
		}*/
		
		//COMMON GST DATA		
		map.put("gstDetailedResp" , getGstDetails(applicationId, userId , gstId));		
		
		//COMMON ONE-FORM DATA
		map.putAll(getOneFormData(applicationId,proposalId,userId,itrId));
		
		map.putAll(getMatchesAndEligiblityDetails(applicationId,productId,proposalId));
		
		try {
			PrimaryCorporateDetailMudraLoanReqRes primaryCorporateDetailMudraLoanReqRes = new PrimaryCorporateDetailMudraLoanReqRes(); 
			PrimaryCorporateDetailMudraLoan primaryCorporateDetailMudraLoan = primaryCorporateDetailsMudra.findFirstByApplicationIdAndApplicationProposalMappingProposalIdOrderByIdDesc(toApplicationId, proposalId);
			if(!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetailMudraLoan)) {
				BeanUtils.copyProperties(primaryCorporateDetailMudraLoan, primaryCorporateDetailMudraLoanReqRes);
				if (primaryCorporateDetailMudraLoanReqRes != null) {
					map.put("statutoryObligation", CommonUtils.printFields(primaryCorporateDetailMudraLoanReqRes , null));
				}				
			}
		}catch (Exception e) {
			logger.error("Error/Exception statutoryObligation==>{}",e);
		}
		//FINANCIALS AND NOTES TO ACCOUNTS
		try {
			//PrimaryCorporateRequest primaryCorporateRequest = primaryCorporateService.get(toApplicationId, userId);
			int currentYear = scoringService.getFinYear(itrId);
			map.put("currentYr",currentYear-1);
			// PENDING
			Long denominationValue = 1l;
			if(applicationProposalMapping.getDenominationId() != null) {
				denominationValue = Denomination.getById(applicationProposalMapping.getDenominationId()).getDigit();
			}
			
			Integer[] years = {currentYear-3, currentYear-2, currentYear-1};
			Map<Integer, Object[]> financials = new TreeMap<Integer, Object[]>(Collections.reverseOrder());
			for(Integer year : years) {
				//Object[] data = calculateFinancials(userId, applicationId, null, denominationValue, year); // PREVIOUS
				Object[] data = calculateFinancials(userId, toApplicationId, null, denominationValue,proposalId, year);//NEW BASED ON PROPOSAL ID
				financials.put(year, data);
			}
			
			calculateRatioAnalysis(financials, toApplicationId,itrId);
			map.put("financials", financials);
			Map<Integer, Object[]> projectedFin = new HashMap<Integer, Object[]>(applicationProposalMapping.getTenure().intValue());
			//if(primaryCorporateRequest.getProductId() == 1) { previous
			 if(applicationProposalMapping.getProductId() == 1){  // new
				//projectedFin.put(currentYear , calculateFinancials(userId, applicationId, null, denominationValue, currentYear)); // PREVIOUS
				projectedFin.put(currentYear , calculateFinancials(userId, toApplicationId, null, denominationValue,proposalId, currentYear));// NEW BASED ON PROPOSAL ID
				map.put("tenure", 1);
			}else {
				for(int i=0; i<=applicationProposalMapping.getTenure().intValue();i++) {
					//projectedFin.put(currentYear + i, calculateFinancials(userId, applicationId, null, denominationValue, currentYear + i));// PREVIOUS
					projectedFin.put(currentYear + i, calculateFinancials(userId, toApplicationId, null, denominationValue, proposalId,currentYear + i));// NEW BASED ON PROPOSAL ID
				}
				map.put("tenure", applicationProposalMapping.getTenure().intValue() +1);
			}
			map.put("projectedFinancials", projectedFin);
		}catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		
		//NOTE OF BORROWER FOR MSME
		try {
			String note = commonRepository.getNoteForHLCam(toApplicationId);
			map.put("noteOfBorrower", !CommonUtils.isObjectNullOrEmpty(note) ? note : null);
		}catch (Exception e) {
			logger.error("Error/Exception while getting note of borrower....Error==>{}", e);
		}

		//PROPOSAL RESPONSE
		try {
			ObjectMapper mapper = new ObjectMapper();
			ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
			proposalMappingRequest.setApplicationId(toApplicationId);
			proposalMappingRequest.setFpProductId(productId);
			ProposalMappingResponse proposalMappingResponse= proposalDetailsClient.getActiveProposalDetails(proposalMappingRequest);
			proposalMappingRequestString = mapper.convertValue(proposalMappingResponse.getData(), ProposalMappingRequestString.class);
			logger.info("============proposalMappingRequestId==>{}",proposalMappingRequestString.getId());
			map.put("proposalResponse", !CommonUtils.isObjectNullOrEmpty(proposalMappingResponse.getData()) ? CommonUtils.printFields(proposalMappingResponse.getData() ,null): " ");
			
			//For ROI Calculation
			Map<String , Object> roiData = new LinkedHashMap<String, Object>();
			if(!CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString)) {
				roiData.put("scoringBasedOn" , proposalMappingRequestString.getScoringModelBasedOn() != null && proposalMappingRequestString.getScoringModelBasedOn() == 2 ? "MCLR" : "EBLR");
				roiData.put("mclr", !CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString.getMclrRoi()) ? proposalMappingRequestString.getMclrRoi() : "-");
				roiData.put("spread", !CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString.getSpreadRoi()) ? proposalMappingRequestString.getSpreadRoi() : "-");
				roiData.put("effectiveRoi", !CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString.getElRoi()) ? proposalMappingRequestString.getElRoi() : "-");
			}
			map.put("roiData", !CommonUtils.isObjectNullOrEmpty(roiData) ? roiData : null);
		}		
		catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		//FOR NO-ITR DATA (MUDRA - CAM)
				CorporateApplicantRequest response = applicantService.getCorporateApplicantDetails(applicationId);
				LoansResponse loansResponse = new LoansResponse(CommonUtils.DATA_FOUND, HttpStatus.OK.value());
				response.getIncomeDetails().get("creditors");
				loansResponse.setData(response.getIncomeDetails());
				DateFormat dfYear = new SimpleDateFormat("yyyy");
				Date dateNoItr = new Date();
				dateNoItr = response.getDob();
				if (!CommonUtils.isObjectNullOrEmpty(dateNoItr)) {
//					int ii = (int) (dateNoItr.getTime()/1000);
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(response.getDob().getTime());
					map.put("noItrYear", dfYear.format(dateNoItr));
					
					String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
//					map.put("noItrMonth",dfMonth.format(response.getDob()));			
					map.put("noItrMonth",month);
				}else {
					map.put("noItrYear", "-");
					map.put("noItrMonth","-");			
				}
				
				
				try {
						//Map<String, Object> incomeDetails = response.getIncomeDetails();
						LinkedHashMap<String, Object> incomeDetails = response.getIncomeDetails();
						
						Map<String, Object> sales = MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("sales").toString(), Map.class) ;
						  sales.put("label", "Sales"); 
						  incomeDetails.put("sales", sales);
						
						  Map<String, Object> profitAfterTax = MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("profitAfterTax").toString(), Map.class) ;
						  profitAfterTax.put("label","Net Profit / Loss"); 
						  incomeDetails.put("profitAfterTax", profitAfterTax);	  
						  
						  Map<String, Object> inventory =  MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("inventory").toString(), Map.class) ;
						  inventory.put("label", "Inventory");
						  incomeDetails.put("inventory", inventory); 
						  
						  Map<String, Object> debtors = MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("debtors").toString(), Map.class) ;
						  debtors.put("label", "Debtors");
						  incomeDetails.put("debtors", debtors);
						
						Map<String, Object> creditor = MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("creditors").toString(), Map.class) ; 
						creditor.put("label", "Creditors");
						incomeDetails.put("creditors", creditor);
						
						 
						 Map<String, Object> investmentInPlantMachinery = MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("investmentInPlantMachinery").toString(), Map.class) ;
						  investmentInPlantMachinery.put("label","Investment in Plant and Machinery / Equipments");
						  incomeDetails.put("investmentInPlantMachinery", investmentInPlantMachinery);
						  
						  Map<String, Object> networth = MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("networth").toString(), Map.class) ;
						  networth.put("label", "Networth");
						  incomeDetails.put("networth", networth);
						  
						  Map<String, Object> totalAssets = MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("totalAssets").toString(), Map.class) ;
						  totalAssets.put("label", "Total Assets");
						  incomeDetails.put("totalAssets", totalAssets);
						 
						  
						  Map<String, Object> totalLiabilities = MultipleJSONObjectHelper.getObjectFromString(incomeDetails.get("totalLiabilities").toString(), Map.class) ;
						  totalLiabilities.put("label", "Total Liabilities");
						  incomeDetails.put("totalLiabilities", totalLiabilities);
 
						
						  
						map.put("noItrIncomeMudra", incomeDetails);

				} catch (Exception e) {
						// TODO: handle exception
					logger.error("Error while Getting No ITR Detail = >{}",e);
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
			try {
				map.put("associateConcern", CommonUtils.printFields(associatedConcernResList, null));
			} catch (Exception e) {
				logger.error("Error while Getting Associate Concern Details = >{}",e);
			}
				
		
		//SCORING DATA 
		try {
			ScoringRequest scoringRequest = new ScoringRequest();
			scoringRequest.setApplicationId(toApplicationId);
			scoringRequest.setFpProductId(productId);
			ScoringResponse scoringResponse = scoringClient.getScore(scoringRequest);
			DecimalFormat df = new DecimalFormat(".##");
			List<Map<String,Object>> scoreResponse = null;
			if(!CommonUtils.isListNullOrEmpty(scoringResponse.getDataList())) {
				scoreResponse = new ArrayList<>(scoringResponse.getDataList().size());
			}else {
				scoreResponse = new ArrayList<>();
			}
			Map<String,Object> mudraScoringMap = new HashMap<>();
			if(!CommonUtils.isObjectNullOrEmpty(scoringResponse.getDataObject())) {
				ProposalScoreResponse proposalScoreResponse =  (ProposalScoreResponse)MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)scoringResponse.getDataObject(),ProposalScoreResponse.class);
				mudraScoringMap.put("scoringDataObject",CommonUtils.printFields(proposalScoreResponse,null));
				map.put("scoringModelName", proposalScoreResponse.getScoringModelName() !=null ? CommonUtils.printFields(proposalScoreResponse.getScoringModelName(), null) : "-");
				if(!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse)) {
					map.put("managementRiskScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskScore()) ? proposalScoreResponse.getManagementRiskScore().intValue(): "-");
					map.put("managementRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskMaxTotalScore()) ?  proposalScoreResponse.getManagementRiskMaxTotalScore().intValue():"-");
					map.put("managementRiskWeightOfScoring",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskWeightOfScoring()) ? df.format(proposalScoreResponse.getManagementRiskWeightOfScoring()) :"-");
					map.put("managementRiskWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskWeight()) ? Math.round(proposalScoreResponse.getManagementRiskWeight()): "-");
					map.put("managementRiskMaxTotalWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskMaxTotalWeight()) ? Math.round(proposalScoreResponse.getManagementRiskMaxTotalWeight()): "-");
					
					map.put("financialRiskScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskScore()) ? proposalScoreResponse.getFinancialRiskScore().intValue() : "-");
					map.put("financialRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskMaxTotalScore()) ? proposalScoreResponse.getFinancialRiskMaxTotalScore().intValue():"-");
					map.put("financialRiskWeightOfScoring",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskWeightOfScoring()) ? df.format(proposalScoreResponse.getFinancialRiskWeightOfScoring()): "-");
					map.put("financialRiskWeight",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskWeight()) ? Math.round(proposalScoreResponse.getFinancialRiskWeight()): "-");
					map.put("financialRiskMaxTotalWeight",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskMaxTotalWeight()) ? CommonUtils.convertValueRound(proposalScoreResponse.getFinancialRiskMaxTotalWeight()) : "-");
					
					map.put("businessRiskScore", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskScore()) ? proposalScoreResponse.getBusinessRiskScore().intValue():"-");
					map.put("businessRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskMaxTotalScore()) ? proposalScoreResponse.getBusinessRiskMaxTotalScore().intValue():"-");
					map.put("businessRiskWeightOfScoring", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskWeightOfScoring()) ? df.format(proposalScoreResponse.getBusinessRiskWeightOfScoring()):"-");
					map.put("businessRiskWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskWeight()) ? Math.round(proposalScoreResponse.getBusinessRiskWeight()):"-");
					map.put("businessRiskMaxTotalWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskMaxTotalWeight()) ? CommonUtils.convertValueRound(proposalScoreResponse.getBusinessRiskMaxTotalWeight()):"-");
					
					map.put("totalActualScore", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskScore(), proposalScoreResponse.getFinancialRiskScore(), proposalScoreResponse.getBusinessRiskScore()).intValue());
					map.put("totalOutOfScore", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskMaxTotalScore(), proposalScoreResponse.getFinancialRiskMaxTotalScore(), proposalScoreResponse.getBusinessRiskMaxTotalScore()).intValue());
					map.put("totalWeight", df.format(CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskWeightOfScoring(), proposalScoreResponse.getFinancialRiskWeightOfScoring(), proposalScoreResponse.getBusinessRiskWeightOfScoring()).doubleValue()));
					
					if(proposalScoreResponse.getManagementRiskWeight()!=null && proposalScoreResponse.getFinancialRiskWeight() != null && proposalScoreResponse.getBusinessRiskWeight()!= null){
					map.put("totalRiskWeight", Math.addExact(Math.addExact(Math.round(proposalScoreResponse.getManagementRiskWeight()), Math.round(proposalScoreResponse.getFinancialRiskWeight())), Math.round(proposalScoreResponse.getBusinessRiskWeight())));
					}
					map.put("totalRiskMaxWeight", Math.round(CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskMaxTotalWeight(), proposalScoreResponse.getFinancialRiskMaxTotalWeight(), proposalScoreResponse.getBusinessRiskMaxTotalWeight()).intValue()));
					
					map.put("interpretation", StringEscapeUtils.escapeXml(proposalScoreResponse.getInterpretation()));
					map.put("proposalScoreResp", proposalScoreResponse);
				}				
			}
		
//			FOR FILTERING MUDRA LOANS SCORING PARAMETERS
			List<LinkedHashMap<String, Object>> mapList = Collections.emptyList();;
			if(!CommonUtils.isListNullOrEmpty(scoringResponse.getDataList())) {
				mapList = (List<LinkedHashMap<String, Object>>)scoringResponse.getDataList();
			}else {
				mapList = new ArrayList<>();
			}
			
			List<ProposalScoreDetailResponse> newMapList = new ArrayList<>(mapList.size());
			for(LinkedHashMap<String, Object> mp : mapList) {
				newMapList.add(MultipleJSONObjectHelper.getObjectFromMap(mp,ProposalScoreDetailResponse.class));
			}
			
			List<ProposalScoreDetailResponse> collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("CUSTOMER_ASSOCIATE_CONCERN_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("CUSTOMER_ASSOCIATE_CONCERN_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("AGE_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("AGE_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("CIBIL_TRANSUNION_SCORE_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("CIBIL_TRANSUNION_SCORE_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("OWNING_HOUSE_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("OWNING_HOUSE_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("ACADEMIC_QUALIFICATION_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("ACADEMIC_QUALIFICATION_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("EXPERIENCE_IN_THE_LINE_OF_TRADE_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("EXPERIENCE_IN_THE_LINE_OF_TRADE_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("ASSESSED_FOR_INCOME_TAX_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("ASSESSED_FOR_INCOME_TAX_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("PAYMENT_RECORDS_WITH_LENDERS_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("PAYMENT_RECORDS_WITH_LENDERS_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("ID_PROOF_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("ID_PROOF_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("NUMBER_OF_DEPENDENTS_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("NUMBER_OF_DEPENDENTS_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("RESIDING_AT_THE_SAME_ADDRESS_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("RESIDING_AT_THE_SAME_ADDRESS_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("CERTIFICATION_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("CERTIFICATION_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("MAIN_DIRECTOR_CATEGORY_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("MAIN_DIRECTOR_CATEGORY_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("COVERED_UNDER_DIFF_SCHEMES_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("COVERED_UNDER_DIFF_SCHEMES_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("OTHER_SOURCE_OF_INCOME_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("OTHER_SOURCE_OF_INCOME_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("TOL_TNW_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("TOL_TNW_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("TENURE_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("TENURE_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("PAST_YEAR_TURNOVER_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("PAST_YEAR_TURNOVER_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("PAT_NET_SALES_RATIO_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("PAT_NET_SALES_RATIO_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("NO_OF_CUSTOMER_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("NO_OF_CUSTOMER_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("FACTORY_PREMISES_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("FACTORY_PREMISES_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("YEARS_IN_BUSINESS_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("YEARS_IN_BUSINESS_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("NO_OF_CHEQUES_BOUNCED_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("NO_OF_CHEQUES_BOUNCED_ML", CommonUtils.printFields(collect.get(0),null));
			}			
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("NO_OF_CHEQUES_BOUNCED_LAST_SIX_MONTH_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("NO_OF_CHEQUES_BOUNCED_LAST_SIX_MONTH_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("UDYOG_AADHAR_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("UDYOG_AADHAR_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("ITR_RETURN_FILED_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("ITR_RETURN_FILED_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("TYPE_OF_ACTIVITY_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("TYPE_OF_ACTIVITY_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("RELATIONSHIP_WITH_BANK_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("RELATIONSHIP_WITH_BANK_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("MARKETING_ARRANGEMENT_FOR_FINISHED_GOODS_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("MARKETING_ARRANGEMENT_FOR_FINISHED_GOODS_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("REGISTRATION_WITH_GOVERNMENT_AUTHORITIES_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("REGISTRATION_WITH_GOVERNMENT_AUTHORITIES_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("BUSINESS_PROSPECTS_OF_THE_ACTIVITY_TO_BE_UNDERTAKEN_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("BUSINESS_PROSPECTS_OF_THE_ACTIVITY_TO_BE_UNDERTAKEN_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("ACCESS_TO_INPUTS_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("ACCESS_TO_INPUTS_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("CREDIT_SUMMATION_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("CREDIT_SUMMATION_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("DISTANCE_BETWEEN_WORKPLACE_AND_RESIDENCE_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("DISTANCE_BETWEEN_WORKPLACE_AND_RESIDENCE_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("EMPLOYMENT_GENERATION_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("EMPLOYMENT_GENERATION_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("PROMOTERS_CONTRIBUTION_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("PROMOTERS_CONTRIBUTION_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("REGISTRATION_WITH_GOVERNMENT_AUTHORITIES_COMBINED_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("REGISTRATION_WITH_GOVERNMENT_AUTHORITIES_COMBINED_ML", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("RELATIONSHIP_WITH_BANK_COMBINED_ML")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("RELATIONSHIP_WITH_BANK_COMBINED_ML", CommonUtils.printFields(collect.get(0),null));
			}

			// CVL Mudra Parameters
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("FLEET_STRENGTH_OWNED_BY_FLEET_OPERATOR")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("FLEET_STRENGTH_OWNED_BY_FLEET_OPERATOR", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("LOAN_FREE_VEHICLE")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("LOAN_FREE_VEHICLE", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("PROFITABILITY_HISTORY")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("PROFITABILITY_HISTORY", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("ASSURED_ORDER")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("ASSURED_ORDER", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("DEPOSIT_POSITION_POTENTIAL")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("DEPOSIT_POSITION_POTENTIAL", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("EXPERIENCE_IN_BUSINESS")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("EXPERIENCE_IN_BUSINESS", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("COLLATORAL_COVERAGE")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("COLLATORAL_COVERAGE", CommonUtils.printFields(collect.get(0),null));
			}
			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("CONSERVATIVE_DEBT_SERVICE_COVERAGE_RATIO")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("CONSERVATIVE_DEBT_SERVICE_COVERAGE_RATIO", CommonUtils.printFields(collect.get(0),null));
			}

			collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase("OPERATION_SUPPORTED_BY_FAMILY_MEMBERS_DIRECTLY")).collect(Collectors.toList());
			if(!CommonUtils.isListNullOrEmpty(collect)) {
				mudraScoringMap.put("OPERATION_SUPPORTED_BY_FAMILY_MEMBERS_DIRECTLY", CommonUtils.printFields(collect.get(0),null));
			}

			scoreResponse.add(mudraScoringMap);
			map.put("mudraScoringResp", scoreResponse);
		}
			catch (Exception e) {
				logger.error("Error while getting scoring data : ",e);
			}		
		
		try {
			VerifyAPIRequest verifyAPIRequest = new VerifyAPIRequest();
			verifyAPIRequest.setApplicationId(applicationId);
			McaResponse mcaResponse = mcaClient.getVerifyApiData(verifyAPIRequest);
			map.put("verifyApiData", !CommonUtils.isObjectNullOrEmpty(mcaResponse) && !CommonUtils.isObjectNullOrEmpty(mcaResponse.getData()) ? CommonUtils.printFields(mcaResponse.getData() ,null) : null);
		}catch (Exception e) {
			map.put("verifyApiData", null);
			logger.error("Error/Exception while getting Verify API Data of ApplicationId==>{} , e==>{}",applicationId ,e);
		}
		
		//CGTMSE DATA
		try {
			CGTMSEDataResponse cgtmseDataResponse = thirdPartyClient.getCalulation(toApplicationId,productId);
			if(!CommonUtils.isObjectNullOrEmpty(cgtmseDataResponse)) {
				map.put("cgtmseData", CommonUtils.printFields(cgtmseDataResponse,null));
				if(cgtmseDataResponse.getNatureOfEntity().equals("Manufacturer")) {
					map.put("industryType", "MANUFACTURING");
				}else {
					map.put("industryType", " ");
				}
				map.put("maxCgtmseCoverageAmount", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getMaxCgtmseCoverageAmount()));
				map.put("identityAmount", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getIdentityAmount()));
				map.put("gutAmt", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getGutAmt()));
				map.put("loanAmount", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getLoanAmount()));
				map.put("cgtmseCoverageAmount", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getCgtmseCoverageAmount()));
				map.put("amountOfColleteral", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getAmountOfColleteral()));
				map.put("totalColleteralAmount", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getTotalColleteralAmount()));
				map.put("gurAmtCarld", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getGurAmtCarld()));
				map.put("colleteralCoverage", CommonUtils.convertValue(cgtmseDataResponse.getColleteralCoverage()));
				map.put("percTerms", CommonUtils.convertValue(cgtmseDataResponse.getPercTerms()));
				map.put("assetAqu", CommonUtils.convertValueIndianCurrency(cgtmseDataResponse.getAssetAqusition()));
				if(!CommonUtils.isObjectNullOrEmpty(cgtmseDataResponse.getCgtmseResponse()) && !CommonUtils.isObjectNullOrEmpty(cgtmseDataResponse.getCgtmseResponse().getDetails())) {
					map.put("cgtmseBankWise", CommonUtils.printFields(cgtmseDataResponse.getCgtmseResponse().getDetails(),null));
				}
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		//ELIGIBILITY DATA (ASSESSMENT TO LIMITS)
		try {
			//PrimaryCorporateRequest primaryCorporateRequest = primaryCorporateService.get(applicationProposalMapping.getApplicationId(), userId); // previous
			//primaryCorporateRequest.getProductId(); // previous
			int loanId = applicationProposalMapping.getProductId(); // new
			switch (loanId) {
				case 1:
				WorkingCapitalParameter workingCapitalPara = workingCapitalParameterRepository.getByID(productId);
					if (workingCapitalPara.getAssessmentMethodId() != null) {
						Long assessmentId = workingCapitalPara.getAssessmentMethodId().longValue();
						map.put(ASSESSMENT_ID, assessmentId);
					}else {
						map.put(ASSESSMENT_ID, " ");
					}
					break;
				case 2:
				TermLoanParameter termLoanParameter = termLoanParameterRepository.getById(productId);
					if (termLoanParameter.getAssessmentMethodId() != null) {
						Long assessmentId = termLoanParameter.getAssessmentMethodId().longValue();
						map.put(ASSESSMENT_ID, assessmentId);
					} else {
						map.put(ASSESSMENT_ID, " ");
					}
					break;
				case 16:
				WcTlParameter wctlPara = wcTlLoanParameterRepository.getById(productId);
					if (wctlPara.getAssessmentMethodId() != null) {
						Long assessmentId = wctlPara.getAssessmentMethodId().longValue();
						map.put(ASSESSMENT_ID, assessmentId);
					}else {
						map.put(ASSESSMENT_ID, " ");
					}
					break;
				default:
					logger.info("Invalid Loan Id");
					break;
				}
		} catch (Exception e2) {
			logger.error(CommonUtils.EXCEPTION,e2);
		}
		
		try{
			EligibililityRequest eligibilityReq=new EligibililityRequest();
			eligibilityReq.setApplicationId(toApplicationId);
			eligibilityReq.setFpProductId(productId);
			EligibilityResponse eligibilityResp= eligibilityClient.corporateEligibilityData(eligibilityReq);

			if(!CommonUtils.isObjectListNull(eligibilityResp.getData())){
				CVLEligibilityRequest req= MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)eligibilityResp.getData(), CVLEligibilityRequest.class);
				DecimalFormat df = new DecimalFormat(".##");
				req.setAgreedPurchasePrice(!CommonUtils.isObjectNullOrEmpty(req.getAgreedPurchasePrice()) ? Double.valueOf(df.format(req.getAgreedPurchasePrice()).toString()) : 0.0);
				req.setOtherCost(!CommonUtils.isObjectNullOrEmpty(req.getOtherCost()) ? Double.valueOf(df.format(req.getOtherCost()).toString()) : 0.0);
				req.setFullyBuiltCost(!CommonUtils.isObjectNullOrEmpty(req.getFullyBuiltCost()) ? Double.valueOf(df.format(req.getFullyBuiltCost()).toString()) : 0.0);
				req.setChassisCost(!CommonUtils.isObjectNullOrEmpty(req.getChassisCost()) ? Double.valueOf(df.format(req.getChassisCost()).toString()) : 0.0);
				req.setBodyBuildingCost(!CommonUtils.isObjectNullOrEmpty(req.getBodyBuildingCost()) ? Double.valueOf(df.format(req.getBodyBuildingCost()).toString()) : 0.0);
				req.setTotalCostOfProposedVehicle(!CommonUtils.isObjectNullOrEmpty(req.getTotalCostOfProposedVehicle()) ? Double.valueOf(df.format(req.getTotalCostOfProposedVehicle()).toString()) : 0.0);
				req.setRatePerKm(!CommonUtils.isObjectNullOrEmpty(req.getRatePerKm()) ? Double.valueOf(df.format(req.getRatePerKm()).toString()) : 0.0);
				req.setOtherMonthlyIncome(!CommonUtils.isObjectNullOrEmpty(req.getOtherMonthlyIncome()) ? Double.valueOf(df.format(req.getOtherMonthlyIncome()).toString()) : 0.0);
				req.setAvgKmsPerLtr(!CommonUtils.isObjectNullOrEmpty(req.getAvgKmsPerLtr()) ? Double.valueOf(df.format(req.getAvgKmsPerLtr()).toString()) : 0.0);
				req.setIncomeTaxPaid(!CommonUtils.isObjectNullOrEmpty(req.getIncomeTaxPaid()) ? Double.valueOf(df.format(req.getIncomeTaxPaid()).toString()) : 0.0);
				req.setEligibleProjectCost(!CommonUtils.isObjectNullOrEmpty(req.getEligibleProjectCost()) ? Double.valueOf(df.format(req.getEligibleProjectCost()).toString()) : 0.0);
				req.setAvgMonthlyIncome(!CommonUtils.isObjectNullOrEmpty(req.getAvgMonthlyIncome()) ? Double.valueOf(df.format(req.getAvgMonthlyIncome()).toString()) : 0.0);
				req.setTotalMonthlyIncome(!CommonUtils.isObjectNullOrEmpty(req.getTotalMonthlyIncome()) ? Double.valueOf(df.format(req.getTotalMonthlyIncome()).toString()) : 0.0);
				req.setFuelCostPerMonth(!CommonUtils.isObjectNullOrEmpty(req.getFuelCostPerMonth()) ? Double.valueOf(df.format(req.getFuelCostPerMonth()).toString()) : 0.0);
				req.setTotalMonthlyExp(!CommonUtils.isObjectNullOrEmpty(req.getTotalMonthlyExp()) ? Double.valueOf(df.format(req.getTotalMonthlyExp()).toString()) : 0.0);
				req.setSurplusAmt(!CommonUtils.isObjectNullOrEmpty(req.getSurplusAmt()) ? Double.valueOf(df.format(req.getSurplusAmt()).toString()) : 0.0);
				req.setNetSurplusAmt(!CommonUtils.isObjectNullOrEmpty(req.getNetSurplusAmt()) ? Double.valueOf(df.format(req.getNetSurplusAmt()).toString()) : 0.0);
				req.setNetCashAccruals(!CommonUtils.isObjectNullOrEmpty(req.getNetCashAccruals()) ? Double.valueOf(df.format(req.getNetCashAccruals()).toString()) : 0.0);
				req.setMonthlyCashAccruals(!CommonUtils.isObjectNullOrEmpty(req.getMonthlyCashAccruals()) ? Double.valueOf(df.format(req.getMonthlyCashAccruals()).toString()) : 0.0);
				req.setPerLakhEmi(!CommonUtils.isObjectNullOrEmpty(req.getPerLakhEmi()) ? Double.valueOf(df.format(req.getPerLakhEmi()).toString()) : 0.0);
				req.setTermLoanCashFlow(!CommonUtils.isObjectNullOrEmpty(req.getTermLoanCashFlow()) ? Double.valueOf(df.format(req.getTermLoanCashFlow()).toString()) : 0.0);
				req.setNetEligibleTermLoan(!CommonUtils.isObjectNullOrEmpty(req.getNetEligibleTermLoan()) ? Double.valueOf(df.format(req.getNetEligibleTermLoan()).toString()) : 0.0);
				req.setEligibleLoanAmt(!CommonUtils.isObjectNullOrEmpty(req.getEligibleLoanAmt()) ? Double.valueOf(df.format(req.getEligibleLoanAmt()).toString()) : 0.0);
				req.setEligibleLoanEmi(!CommonUtils.isObjectNullOrEmpty(req.getEligibleLoanEmi()) ? Double.valueOf(df.format(req.getEligibleLoanEmi()).toString()) : 0.0);
				req.setDscr(!CommonUtils.isObjectNullOrEmpty(req.getDscr()) ? Double.valueOf(df.format(req.getDscr()).toString()) : 0.0);
				req.setCvlEliMotorTax(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliMotorTax()) ? Double.valueOf(df.format(req.getCvlEliMotorTax()).toString()) : 0.0);
				req.setCvlEliInsurancePremium(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliInsurancePremium()) ? Double.valueOf(df.format(req.getCvlEliInsurancePremium()).toString()) : 0.0);
				req.setCvlEliGarageRent(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliGarageRent()) ? Double.valueOf(df.format(req.getCvlEliGarageRent()).toString()) : 0.0);
				req.setCvlEliDepreciation(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliDepreciation()) ? Double.valueOf(df.format(req.getCvlEliDepreciation()).toString()) : 0.0);
				req.setCvlEliRepairExp(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliRepairExp()) ? Double.valueOf(df.format(req.getCvlEliRepairExp()).toString()) : 0.0);
				req.setCvlEliCostOil(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliCostOil()) ? Double.valueOf(df.format(req.getCvlEliCostOil()).toString()) : 0.0);
				req.setCvlEliStaffSal(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliStaffSal()) ? Double.valueOf(df.format(req.getCvlEliStaffSal()).toString()) : 0.0);
				req.setCvlEliDrawingExp(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliDrawingExp()) ? Double.valueOf(df.format(req.getCvlEliDrawingExp()).toString()) : 0.0);
				req.setCvlEliUnloadingCharges(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliUnloadingCharges()) ? Double.valueOf(df.format(req.getCvlEliUnloadingCharges()).toString()) : 0.0);
				req.setCvlEliIntOnBorrowing(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliIntOnBorrowing()) ? Double.valueOf(df.format(req.getCvlEliIntOnBorrowing()).toString()) : 0.0);
				req.setCvlEliOthers(!CommonUtils.isObjectNullOrEmpty(req.getCvlEliOthers()) ? Double.valueOf(df.format(req.getCvlEliOthers()).toString()) : 0.0);
				req.setCvlCostOfFullyBuildModel(!CommonUtils.isObjectNullOrEmpty(req.getCvlCostOfFullyBuildModel()) ? Double.valueOf(df.format(req.getCvlCostOfFullyBuildModel()).toString()) : 0.0);
				req.setCvlCostOfChasis(!CommonUtils.isObjectNullOrEmpty(req.getCvlCostOfChasis()) ? Double.valueOf(df.format(req.getCvlCostOfChasis()).toString()) : 0.0);
				req.setCvlCostOfBody(!CommonUtils.isObjectNullOrEmpty(req.getCvlCostOfBody()) ? Double.valueOf(df.format(req.getCvlCostOfBody()).toString()) : 0.0);
				req.setCvlOtherMarginExp(!CommonUtils.isObjectNullOrEmpty(req.getCvlOtherMarginExp()) ? Double.valueOf(df.format(req.getCvlOtherMarginExp()).toString()) : 0.0);
				req.setCvl2ndHandVehicle(!CommonUtils.isObjectNullOrEmpty(req.getCvl2ndHandVehicle()) ? Double.valueOf(df.format(req.getCvl2ndHandVehicle()).toString()) : 0.0);
				req.setExistingLimit(!CommonUtils.isObjectNullOrEmpty(req.getExistingLimit()) ? Double.valueOf(df.format(req.getExistingLimit()).toString()) : 0.0);
				req.setMonthlyLoanObligation(!CommonUtils.isObjectNullOrEmpty(req.getMonthlyLoanObligation()) ? Double.valueOf(df.format(req.getMonthlyLoanObligation()).toString()) : 0.0);


				map.put("assLimits",req);
			}
		}catch (Exception e) {
			logger.error("Error while getting Eligibility data : ",e);
		}

		/* eligibility financialCalculation year */
		map.put("eligibilityFinancialYear",CommonUtils.getFinancialYear());		
		
		//HUNTER API ANALYSIS
		
		
		try {
			UserResponse campaignUser=usersClient.isExists(userId,null);
			map.put("isCapaignUser", campaignUser != null && campaignUser.getData() != null ? campaignUser.getData() : "");
			if(campaignUser!= null && campaignUser.getData().equals(false)) {
				AnalyticsResponse hunterResp =fraudAnalyticsClient.getRuleAnalysisData(applicationId);
				if(!CommonUtils.isObjectListNull(hunterResp,hunterResp.getData())) {
					map.put("hunterResponse",  CommonUtils.printFields(hunterResp.getData(),null));
				}	
			}else {
				logger.info("user is campaign so hunter is skipped");
			}
			
		} catch (Exception e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}
		
		//ITR (CHECK IF UPLOADED USING XML OR ONLINE)
		try {
			ITRConnectionResponse itrConnectionResponse= itrClient.getIsUploadAndYearDetails(itrId);
			map.put("checkItr", itrConnectionResponse != null && itrConnectionResponse.getData() != null ? CommonUtils.printFields(itrConnectionResponse.getData(), null) : null);
		}catch(Exception e) {
			logger.error("Error while getting ITR data : ",e);
		}
		
		//gstRelatedParty Data Fetch
		try {
			Map<String , Object> gstRelatedPartyRequests = loanApplicationService.getGstRelatedPartyDetails(applicationId);
			map.put("gstPartyRelatedData", gstRelatedPartyRequests != null && !gstRelatedPartyRequests.isEmpty() ? CommonUtils.printFields(gstRelatedPartyRequests , null) : null);
		}catch (Exception e) {
			logger.error("Error/Exception while fetching list of gst Related Party List Data of APplicationId==>{}  ... Error==>{}",applicationId ,e);
		}
		
		map.put("gstEnable", "true");
		
		//GST Comparision by Maaz
		try{
			FinancialInputRequest finaForCam = finaForCam(applicationId,proposalId,itrId);
			map.put("gstComparision", corporatePrimaryViewService.gstVsItrVsBsComparision(applicationId, finaForCam ,gstId , itrId , bsId));
		}catch (Exception e) {
			logger.error("error in getting gst comparision data : {}",e);
		}
		
		//Get BankStatement
		try {
			map.putAll(getBankStatementDetails(toApplicationId, userId,bsId));
		}catch (Exception e) {
			logger.error("Error in getting Bank Statemnt from Perfios in MSME Cam Report with applicationId==>{}",applicationId);
		}
		
		// get ngbc final data
		

		/**ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(applicationId);
		reportRequest.setUserId(userId);
		List<Data> datas=new ArrayList<>();
		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			List<HashMap<String, Object>> hashMap=(List<HashMap<String, Object>>) analyzerResponse.getData();
			if(!CommonUtils.isListNullOrEmpty(hashMap))
			{
				for(HashMap<String,Object> rec:hashMap)
				{
					Data data = MultipleJSONObjectHelper.getObjectFromMap(rec, Data.class);
					datas.add(data);
					List<Object> customerInfo = new ArrayList<Object>();
					for(int i =0; i<hashMap.size(); i++) {
						customerInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getCustomerInfo()) ? CommonUtils.printFields(data.getCustomerInfo(),null) : " ");
					}
					map.put("customerInfo" , customerInfo);
				}
			}
		}catch (Exception e) {
			logger.error("Error while getting perfios data : ",e);
		}*/



/**********************************************FINAL DETAILS*****************************************************/
		
		if(isFinalView && (applicationProposalMapping.getIsMcqSkipped() == null || applicationProposalMapping.getIsMcqSkipped() == false)) {
			
			//FITCH DATA
			try {
				//RatingResponse ratingResponse = (RatingResponse) irrService.calculateIrrRating(applicationId, userId).getBody().getData();

				//RatingResponse ratingResponse = (RatingResponse) irrService.calculateIrrRating(proposalMappingRequestString.getId(), userId, proposalMappingRequestString.getId()).getBody().getData();// PREVIOUS
				RatingResponse ratingResponse = (RatingResponse) irrService.calculateIrrRating(toApplicationId, userId, applicationProposalMapping.getProposalId()).getBody().getData(); //NEW BASED ON PROPOSAL MAPPING ID
				if(!CommonUtils.isObjectNullOrEmpty(ratingResponse) && !CommonUtils.isObjectNullOrEmpty(ratingResponse.getBusinessTypeId())) {
					if(BusinessType.MANUFACTURING == ratingResponse.getBusinessTypeId()){
						FitchOutputManu fitchOutputManu= MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)ratingResponse.getData(),FitchOutputManu.class);
						if(!CommonUtils.isObjectNullOrEmpty(fitchOutputManu)) {
							map.put(FITCH_RESPONSE,CommonUtils.convertToDoubleForXml(fitchOutputManu,null));
							map.put("financialClosure",!CommonUtils.isObjectNullOrEmpty(fitchOutputManu.getFinancialClosureScore()) ? fitchOutputManu.getFinancialClosureScore() : "NA");
							map.put("intraCompany",!CommonUtils.isObjectNullOrEmpty(fitchOutputManu.getIntraCompanyScore()) ? fitchOutputManu.getIntraCompanyScore() : "NA");
							map.put("statusProjectClearance",!CommonUtils.isObjectNullOrEmpty(fitchOutputManu.getStatusProjectClearanceScore()) ? fitchOutputManu.getStatusProjectClearanceScore() : "NA");
							map.put("financialStrength",!CommonUtils.isObjectNullOrEmpty(fitchOutputManu.getFinancialStrengthScore()) ? fitchOutputManu.getFinancialStrengthScore() : "NA");
							map.put("infrastructureAvailability",!CommonUtils.isObjectNullOrEmpty(fitchOutputManu.getInfrastructureAvailabilityScore()) ? fitchOutputManu.getInfrastructureAvailabilityScore() : "NA");
							map.put("constructionContract",!CommonUtils.isObjectNullOrEmpty(fitchOutputManu.getConstructionContractScore()) ? fitchOutputManu.getConstructionContractScore() : "NA");
							map.put("forexRisk",!CommonUtils.isObjectNullOrEmpty(fitchOutputManu.getForexRiskScore()) ? fitchOutputManu.getForexRiskScore() : "NA");
							map.put("designTechnology",!CommonUtils.isObjectNullOrEmpty(fitchOutputManu.getDesignTechnologyRiskScore()) ? fitchOutputManu.getDesignTechnologyRiskScore() : "NA");
							map.put(FITCH_TITLE,"Manufacturing");
						}
					}
					
					if(BusinessType.TRADING == ratingResponse.getBusinessTypeId()){
						FitchOutputTrad fitchOutputTrad = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)ratingResponse.getData(),FitchOutputTrad.class);
						if(!CommonUtils.isObjectNullOrEmpty(fitchOutputTrad)) {
							map.put(FITCH_RESPONSE,CommonUtils.convertToDoubleForXml(fitchOutputTrad,null));
							map.put(FITCH_TITLE,"Trading");
						}
					}
					
					if(BusinessType.SERVICE == ratingResponse.getBusinessTypeId()){
						FitchOutputServ fitchOutputServ = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)ratingResponse.getData(),FitchOutputServ.class);
						if(!CommonUtils.isObjectNullOrEmpty(fitchOutputServ)) {
							map.put(FITCH_RESPONSE,CommonUtils.convertToDoubleForXml(fitchOutputServ,null));
							map.put(FITCH_TITLE,"Service");
						}
					}
				}
			}
			catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			
			//OWNERSHIP DETAILS :- 
			try {
				List<OwnershipDetailRequest> ownershipDetailRequestsList = ownershipDetailsService.getOwnershipDetailList(toApplicationId, userId,applicationProposalMapping.getProposalId());
				List<OwnershipDetailResponse> ownershipDetailResponseList = new ArrayList<>();
				for (OwnershipDetailRequest ownershipDetailRequest : ownershipDetailRequestsList) {
					OwnershipDetailResponse ownershipDetailResponse = new OwnershipDetailResponse();
					ownershipDetailResponse.setRemarks(ownershipDetailRequest.getRemarks());
					ownershipDetailResponse.setStackPercentage(ownershipDetailRequest.getStackPercentage());
					ownershipDetailResponse.setShareHoldingCategory(ShareHoldingCategory.getById(ownershipDetailRequest.getShareHoldingCategoryId()).getValue());
					ownershipDetailResponseList.add(ownershipDetailResponse);
				}
				map.put("ownership", CommonUtils.printFields(ownershipDetailResponseList,null));

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
		    }
			
			//PROMOTOR BACKGROUND DETAILS
			try {
			/*	List<PromotorBackgroundDetailRequest> promotorBackgroundDetailRequestList = promotorBackgroundDetailsService.getPromotorBackgroundDetailList(toApplicationId, userId);*/
				List<PromotorBackgroundDetailRequest> promotorBackgroundDetailRequestList = promotorBackgroundDetailsService.getPromotorBackgroundDetailListByProposalId(toApplicationId, applicationProposalMapping.getProposalId(),userId);
				List<PromotorBackgroundDetailResponse> promotorBackgroundDetailResponseList = new ArrayList<>();
				for (PromotorBackgroundDetailRequest promotorBackgroundDetailRequest : promotorBackgroundDetailRequestList) {
					PromotorBackgroundDetailResponse promotorBackgroundDetailResponse = new PromotorBackgroundDetailResponse();
					//promotorBackgroundDetailResponse.setAchievements(promotorBackgroundDetailRequest.getAchivements());
					String promotorName = "";
					if (promotorBackgroundDetailRequest.getSalutationId() != null){
						promotorName = Title.getById(promotorBackgroundDetailRequest.getSalutationId()).getValue();
					}
					promotorName += promotorBackgroundDetailRequest.getPromotorsName();
					promotorBackgroundDetailResponse.setPromotorsName(promotorName);
					promotorBackgroundDetailResponse.setPanNo(promotorBackgroundDetailRequest.getPanNo().toUpperCase());
					promotorBackgroundDetailResponse.setAddress(promotorBackgroundDetailRequest.getAddress());
					promotorBackgroundDetailResponse.setGender((promotorBackgroundDetailRequest.getGender() != null ? Gender.getById(promotorBackgroundDetailRequest.getGender()).getValue() : " " ));
					promotorBackgroundDetailResponse.setDin(!CommonUtils.isObjectNullOrEmpty(promotorBackgroundDetailRequest.getDin())?decim.format(promotorBackgroundDetailRequest.getDin()).toString() : "");
					promotorBackgroundDetailResponse.setTotalExperience(CommonUtils.convertValueWithoutDecimal(promotorBackgroundDetailRequest.getTotalExperience()));
					promotorBackgroundDetailResponse.setNetworth(CommonUtils.convertValue(promotorBackgroundDetailRequest.getNetworth()));
					promotorBackgroundDetailResponse.setAppointmentDate(promotorBackgroundDetailRequest.getAppointmentDate() != null ? CommonUtils.DATE_FORMAT.format(promotorBackgroundDetailRequest.getAppointmentDate()) : null);
					promotorBackgroundDetailResponse.setRelationshipType((promotorBackgroundDetailRequest.getRelationshipType() != null ? DirectorRelationshipType.getById(promotorBackgroundDetailRequest.getRelationshipType()).getValue() : " " ));
					promotorBackgroundDetailResponse.setDesignation(promotorBackgroundDetailRequest.getDesignation());
					promotorBackgroundDetailResponse.setMobile(promotorBackgroundDetailRequest.getMobile());
					promotorBackgroundDetailResponseList.add(promotorBackgroundDetailResponse);
				}
				map.put("promotorsbckgrnd", CommonUtils.printFields(promotorBackgroundDetailResponseList,null));
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			
			//ASSOCIATE ENTITY
			try {
				/*map.put("associatedConcerns",CommonUtils.printFields(associatedConcernDetailService.getAssociatedConcernsDetailList(toApplicationId, userId),null));*/
				map.put("associatedConcerns",CommonUtils.printFields(associatedConcernDetailService.getAssociatedConcernsDetailListByProposalId(applicationProposalMapping.getProposalId(), userId),null));
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			
			//DETAILS OF GUARANTER
			try {
				/*map.put("guaDetails", CommonUtils.printFields(guarantorsCorporateDetailService.getGuarantorsCorporateDetailList(toApplicationId, userId),null));*/
				map.put("guaDetails", CommonUtils.printFields(guarantorsCorporateDetailService.getGuarantorsCorporateDetailListByProposalId(applicationProposalMapping.getProposalId(), userId),null));
			} catch (Exception e) {
					logger.error("Problem to get Data of Details of Guarantor {}", e);
			}
			
		    //MONTHLY TURNOVER
			try {
				map.put("monthlyTurnOver", CommonUtils.printFields(monthlyTurnoverDetailService.getMonthlyTurnoverDetailListByProposalId(toApplicationId, userId,applicationProposalMapping.getProposalId()),null));
			} catch (Exception e) {
				logger.error("Problem to get Data of Monthly Turnover {}", e);
			}
			
			//COST ESTIMATES
			try {
/*				List<TotalCostOfProjectRequest> costOfProjectsList = costOfProjectService.getCostOfProjectDetailList(toApplicationId, userId);*/
				List<TotalCostOfProjectRequest> costOfProjectsList = costOfProjectService.getCostOfProjectDetailListByProposalId(applicationProposalMapping.getProposalId(), userId);
				List<TotalCostOfProjectResponse> costOfProjectResponses = new ArrayList<TotalCostOfProjectResponse>();
				for (TotalCostOfProjectRequest costOfProjectRequest : costOfProjectsList) {
					TotalCostOfProjectResponse costOfProjectResponse = new TotalCostOfProjectResponse();
					BeanUtils.copyProperties(costOfProjectRequest, costOfProjectResponse);
					if (costOfProjectRequest.getParticularsId() != null)
						costOfProjectResponse.setParticulars(Particular.getById(Integer.parseInt(costOfProjectRequest.getParticularsId().toString())).getValue());
					    costOfProjectResponses.add(costOfProjectResponse);
				}
				map.put("costEstimate", CommonUtils.printFields(costOfProjectResponses,null));
			} catch (Exception e1) {
				logger.error("Problem to get Data of Total cost of project{}", e1);
			}
			
			//MEANS OF FINANCE
			try {
				/*List<FinanceMeansDetailRequest> financeMeansDetailRequestsList = financeMeansDetailsService.getMeansOfFinanceList(toApplicationId, userId);*/
				List<FinanceMeansDetailRequest> financeMeansDetailRequestsList = financeMeansDetailsService.getMeansOfFinanceListByProposalId(applicationProposalMapping.getProposalId(), userId);
				List<FinanceMeansDetailResponse> financeMeansDetailResponsesList = new ArrayList<FinanceMeansDetailResponse>();
				for (FinanceMeansDetailRequest financeMeansDetailRequest : financeMeansDetailRequestsList) {
					FinanceMeansDetailResponse detailResponse = new FinanceMeansDetailResponse();
					BeanUtils.copyProperties(financeMeansDetailRequest, detailResponse);
					if (financeMeansDetailRequest.getFinanceMeansCategoryId() != null)
						detailResponse.setFinanceMeansCategory(FinanceCategory.getById(Integer.parseInt(financeMeansDetailRequest.getFinanceMeansCategoryId().toString())).getValue());
					financeMeansDetailResponsesList.add(detailResponse);
				}
				map.put("meansOfFinance", CommonUtils.printFields(financeMeansDetailResponsesList,null));
			} catch (Exception e1) {
				logger.error("Problem to get Data of Finance Means Details {}", e1);
			}
			
			//COLLATERAL SECURITY			
			try {
				/*map.put("collateralSecurity", CommonUtils.printFields(securityCorporateDetailsService.getsecurityCorporateDetailsList(toApplicationId, userId),null));*/
				map.put("collateralSecurity", CommonUtils.convertToDoubleForXmlIndianCurr(securityCorporateDetailsService.getSecurityCorporateDetailsListFromProposalId(applicationProposalMapping.getProposalId(), userId),null));
			} catch (Exception e) {
				logger.error("Problem to get Data of Security Details {}", e);
			}
		}
		
		try {
			/*map.put("proposedProduct",CommonUtils.printFields(proposedProductDetailsService.getProposedProductDetailList(applicationProposalMapping.getApplicationId(), userId),null));
			map.put("existingProduct",CommonUtils.printFields(existingProductDetailsService.getExistingProductDetailList(applicationProposalMapping.getApplicationId(), userId),null));
			map.put("achievementDetails",CommonUtils.printFields(achievmentDetailsService.getAchievementDetailList(applicationProposalMapping.getApplicationId(), userId),null));*/
						
			map.put("proposedProduct",CommonUtils.printFields(proposedProductDetailsService.getProposedProductDetailListFromProposalId(applicationProposalMapping.getProposalId(), userId),null));
			map.put("existingProduct",CommonUtils.printFields(existingProductDetailsService.getExistingProductDetailListByProposalId(applicationProposalMapping.getProposalId(), userId),null));
			map.put("achievementDetails",CommonUtils.printFields(achievmentDetailsService.getAchievementDetailListForMultipleBank(applicationProposalMapping.getProposalId()),null));
		}catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		//SHARE PRICE
		try {
			CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository.getByApplicationAndProposalIdAndUserId(userId, toApplicationId,applicationProposalMapping.getProposalId());
			if(corporateApplicantDetail != null) {
				map.put("sharePriceFace", CommonUtils.convertValue(corporateApplicantDetail.getSharePriceFace()));
				map.put("sharePriceMarket", CommonUtils.convertValue(corporateApplicantDetail.getSharePriceMarket()));
				map.put("castCategory", corporateApplicantDetail.getCastCategory());
				map.put("minorityCastCategory", corporateApplicantDetail.getMinorCastCategory());
			}
		}catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return map;
	}

	@Override
	public Map<String ,Object> getGstReportData(String panNo){
		Map <String ,Object> map = new HashMap<String, Object>();
		CAMGSTData resp = null;
		GSTR1Request gstr1Request = new GSTR1Request();
		gstr1Request.setPan(panNo);
		map.put("date", simpleDateFormat.format(new Date()));
		try {
			GstResponse response = gstClient.getGstDataByPan(gstr1Request);
			DecimalFormat df = new DecimalFormat(".##");
			if (!CommonUtils.isObjectNullOrEmpty(response) && response.getData() != null) {
				for (LinkedHashMap<String, Object> data : (List<LinkedHashMap<String, Object>>) response.getData()) {
					resp = MultipleJSONObjectHelper.getObjectFromMap(data,CAMGSTData.class);
					if(resp.getMomSales() != null) {
                        List<MomSales> momSalesResp1 = resp.getMomSales();
                        List<MomSales> responseMom= new ArrayList<>();
                        
                        for (MomSales sales1 : momSalesResp1) {
                        	StringBuilder str = new StringBuilder(sales1.getMonth());
                        	sales1.setMonth(str.insert(2, '-').toString());
                        	sales1.setValue((String)CommonUtils.convertValueIndianCurrency(Double.valueOf(sales1.getValue())));
                        	sales1.setIsManualEntry(sales1.getIsManualEntry());
                            responseMom.add(sales1);
                        }
                        data.put("monthWiseMomSales", responseMom);
                    }
					Double totalSales =0.0d;
					if(resp.getMomSales() != null) {
						List<MomSales> momSalesResp = resp.getMomSales();
						for (MomSales sales : momSalesResp) {
							
							totalSales += Double.valueOf(CommonUtils.convertStringCurrencyToDouble(sales.getValue()));
						}
						map.put("totalMomSales", df.format(totalSales));
					}
				}
				map.put("gstDetailedResp", (List<LinkedHashMap<String, Object>>) response.getData());
			}else {
				logger.info("Gst Data Null");
			}
			return map;
		}catch (Exception e) {
			logger.error("Error/Exception while getting gstData By Pan" ,e);
		}
		return null;
	}
	
	@Override
	public Map<String, Object> getBankStatementAnalysisReport(Long applicationId, Long productId) {
		Map<String, Object> map = new HashMap<String, Object>();
		Long userId = loanApplicationRepository.getUserIdByApplicationId(applicationId);
		CorporateApplicantRequest corporateApplicantRequest = corporateApplicantService.getCorporateApplicant(applicationId);
		try {
			if(corporateApplicantRequest != null) {
				map.put("orgName", CommonUtils.printFields(corporateApplicantRequest.getOrganisationName(), null));
			}
		} catch (Exception e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}
		
		// PERFIOS API DATA (BANK STATEMENT ANALYSIS)
		ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(applicationId);
		reportRequest.setUserId(userId);

		List<Data> datas = new ArrayList<>();
		List<Object> bankStatement = new ArrayList<Object>();
		List<Object> monthlyDetails = new ArrayList<Object>();
		List<Object> top5FundReceived = new ArrayList<Object>();
		List<Object> top5FundTransfered = new ArrayList<Object>();
		List<Object> bouncedChequeList = new ArrayList<Object>();
		List<Object> customerInfo = new ArrayList<Object>();
		List<Object> summaryInfo = new ArrayList<Object>();

		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			List<HashMap<String, Object>> hashMap = (List<HashMap<String, Object>>) analyzerResponse.getData();

			if (!CommonUtils.isListNullOrEmpty(hashMap)) {
				for (HashMap<String, Object> rec : hashMap) {
					Data data = MultipleJSONObjectHelper.getObjectFromMap(rec, Data.class);
					datas.add(data);
					bankStatement.add(!CommonUtils.isObjectNullOrEmpty(data.getXns()) ? CommonUtils.printFields(data.getXns().getXn(),null) : " ");
					monthlyDetails.add(!CommonUtils.isObjectNullOrEmpty(data.getMonthlyDetailList()) ? CommonUtils.printFields(data.getMonthlyDetailList(),null) : "");
					top5FundReceived.add(!CommonUtils.isObjectNullOrEmpty(data.getTop5FundReceivedList().getItem()) ? CommonUtils.printFields(data.getTop5FundReceivedList().getItem(),null) : "");
					top5FundTransfered.add(!CommonUtils.isObjectNullOrEmpty(data.getTop5FundTransferedList().getItem()) ? CommonUtils.printFields(data.getTop5FundTransferedList().getItem(),null) : "");
					bouncedChequeList.add(!CommonUtils.isObjectNullOrEmpty(data.getBouncedOrPenalXnList()) ? CommonUtils.printFields(data.getBouncedOrPenalXnList().getBouncedOrPenalXns(),null) : " ");
					customerInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getCustomerInfo()) ? CommonUtils.printFields(data.getCustomerInfo(),null) : " ");
					summaryInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getSummaryInfo()) ?CommonUtils.printFields(data.getSummaryInfo(),null) : " ");
				}

				//logger.info("bankStatement : "+bankStatement.size()+" monthlyDetails :"+monthlyDetails.size()+" top5FundReceived :"+top5FundReceived.size());
				//logger.info("top5FundTransfered : "+top5FundTransfered.size()+" bouncedChequeList :"+bouncedChequeList.size()+" customerInfo :"+customerInfo.size());
				//logger.info("summaryInfo : "+summaryInfo.size()+" bankStatementAnalysis :"+datas.size());
				map.put("bankStatement", bankStatement);
				map.put("monthlyDetails", monthlyDetails);
				map.put("top5FundReceived", top5FundReceived);
				map.put("top5FundTransfered", top5FundTransfered);
				map.put("bouncedChequeList", bouncedChequeList);
				map.put("customerInfo", customerInfo);
				map.put("summaryInfo", summaryInfo);
				map.put("bankStatementAnalysis", datas != null && !datas.isEmpty() ? CommonUtils.printFields(datas, null) : null);

				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				ow.writeValueAsString(monthlyDetails);
			}
		} catch (Exception e) {
			logger.error("Error while getting perfios data : ",e);
		}
		return map;
	}
	
	//FINANCIALS & NOTES TO ACCOUNTS
	public Object[] calculateFinancials(Long userId, Long applicationId, String industry, Long denomination, Long proposalId,Integer year) throws Exception {
		logger.info("USER ID==>{}  APPLICATION_ID==>{} AND PROPOSAL_ID==>{}  AND YEAR_DEMONITAION==>{}--------",userId , applicationId, proposalId , year);
		FinancialInputRequestDbl financialInputRequestDbl = new FinancialInputRequestDbl();
		FinancialInputRequestString financialInputRequestString = new FinancialInputRequestString();
		OperatingStatementDetailsString osDetailsString = new OperatingStatementDetailsString();
		LiabilitiesDetailsString liabilitiesDetailsString = new LiabilitiesDetailsString();
		AssetDetailsString assetDetailsString = new AssetDetailsString();
		//CorporateFinalInfoRequest  corporateFinalInfoRequest = corporateFinalInfoService.get(userId ,applicationId);// PREVIOUS
		CorporateFinalInfoRequest  corporateFinalInfoRequest = corporateFinalInfoService.getByProposalId(userId,proposalId);//NEW BASED ON PROPOSAL ID
		//logger.info("user id and application ID  fs corporate Applicatiion details======>"+corporateFinalInfoRequest.getSharePriceFace());
        //SET SHARE FACE VALUE
		Double shareFaceVal=1.00;
		// changes remaining---->
		//CorporateApplicantDetail corporateApplicantDetail=corporateApplicantDetailRepository.findOneByApplicationIdId(applicationId); // PREVIOUS
		CorporateApplicantDetail corporateApplicantDetail=corporateApplicantDetailRepository.getByApplicationIdAndProposalIdAndIsActive(applicationId,proposalId); // NEW BASED ON PROPOSAL ID
		logger.info("CMA DETAILS===========================>{}",corporateApplicantDetail);
		if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail)) {
			if(!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getSharePriceFace())) {
				shareFaceVal=corporateApplicantDetail.getSharePriceFace();
				financialInputRequestDbl.setShareFaceValue(shareFaceVal);
			}else{
				financialInputRequestDbl.setShareFaceValue(1.00);
			}
		} else{
			financialInputRequestDbl.setShareFaceValue(1.00);
		}
		
		financialInputRequestDbl.setNoOfMonth(12.0);
		
		/************************************************** OPERATING STATEMENT ***************************************************/
		//OperatingStatementDetails osDetails = operatingStatementDetailsRepository.getOperatingStatementDetails(applicationId, year+""); // PREVIOUS
		OperatingStatementDetails osDetails = operatingStatementDetailsRepository.getOperatingStatementDetails(applicationId,proposalId, year+""); // NEW BASED ON PROPOSAL ID
		if(CommonUtils.isObjectNullOrEmpty(osDetails)) {
			osDetails = new OperatingStatementDetails();
		}
		
		osDetailsString.setDomesticSales(CommonUtils.convertValueIndianCurrency(osDetails.getDomesticSales()).toString());
		osDetailsString.setExportSales(CommonUtils.convertValueIndianCurrency(osDetails.getExportSales()).toString());
		osDetailsString.setGrossSalesTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(osDetails.getDomesticSales(),osDetails.getExportSales())).toString());
		financialInputRequestDbl.setGrossSales((osDetails.getDomesticSales()+osDetails.getExportSales()) * denomination);
		financialInputRequestString.setGrossSales(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getGrossSales()).toString());
		
		osDetailsString.setLessExciseDuty(CommonUtils.convertValueIndianCurrency(osDetails.getLessExciseDuty()).toString());
		osDetailsString.setDeductOtherItems(CommonUtils.convertValueIndianCurrency(osDetails.getDeductOtherItems()).toString());
		osDetailsString.setExciseDutyTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(osDetails.getLessExciseDuty(),osDetails.getDeductOtherItems())).toString());
		financialInputRequestDbl.setLessExciseDuity((osDetails.getLessExciseDuty()+osDetails.getDeductOtherItems()) * denomination);
		financialInputRequestString.setLessExciseDuity(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getLessExciseDuity()).toString());
		
		osDetailsString.setAddOperatingStock(CommonUtils.convertValueIndianCurrency(osDetails.getAddOperatingStock()).toString());
		osDetailsString.setDeductStockInProcess(CommonUtils.convertValueIndianCurrency(osDetails.getDeductStockInProcess()).toString());
		osDetailsString.setAddOperatingStockFg(CommonUtils.convertValueIndianCurrency(osDetails.getAddOperatingStockFg()).toString());
		osDetailsString.setDeductClStockFg(CommonUtils.convertValueIndianCurrency(osDetails.getDeductClStockFg()).toString());
		osDetailsString.setIncreaseDecreaseTotal(CommonUtils.convertValueIndianCurrency((osDetails.getAddOperatingStock()-osDetails.getDeductStockInProcess()) + (osDetails.getAddOperatingStockFg()-osDetails.getDeductClStockFg()) * denomination).toString());
		financialInputRequestDbl.setIncreaseDecreaseStock(((osDetails.getAddOperatingStock()-osDetails.getDeductStockInProcess()) + (osDetails.getAddOperatingStockFg()-osDetails.getDeductClStockFg())) * denomination);
		financialInputRequestString.setIncreaseDecreaseStock(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getIncreaseDecreaseStock()).toString());
		
		osDetailsString.setRawMaterials(CommonUtils.convertValueIndianCurrency(osDetails.getRawMaterials()).toString());
		osDetailsString.setOtherSpares(CommonUtils.convertValueIndianCurrency(osDetails.getOtherSpares()).toString());
		osDetailsString.setRawMaterialsConsumedTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(osDetails.getRawMaterials(), osDetails.getOtherSpares())).toString());
		financialInputRequestDbl.setRawMaterialConsumed((osDetails.getRawMaterials()+osDetails.getOtherSpares()) * denomination);
		financialInputRequestString.setRawMaterialConsumed(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getRawMaterialConsumed()).toString());
		financialInputRequestDbl.setPowerAndFuelCost(osDetails.getPowerAndFuel()  * denomination);
		financialInputRequestString.setPowerAndFuelCost(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getPowerAndFuelCost()).toString());
		financialInputRequestDbl.setEmployeeCost(osDetails.getDirectLabour() * denomination);
		financialInputRequestString.setEmployeeCost(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getEmployeeCost()).toString());
		financialInputRequestDbl.setGeneralAndAdminExpe(osDetails.getSellingGenlAdmnExpenses() * denomination);
		financialInputRequestString.setGeneralAndAdminExpe(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getGeneralAndAdminExpe()).toString());
		financialInputRequestDbl.setSellingAndDistriExpe(osDetails.getSellingAndDistributionExpenses() * denomination);
		financialInputRequestString.setSellingAndDistriExpe(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getSellingAndDistriExpe()).toString());
		financialInputRequestDbl.setLessExpeCapita(osDetails.getExpensesAmortised() * denomination);
		financialInputRequestString.setLessExpeCapita(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getLessExpeCapita()).toString());
		financialInputRequestDbl.setMiscelExpe(osDetails.getOtherMfgExpenses() * denomination);
		financialInputRequestString.setMiscelExpe(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getMiscelExpe()).toString());
		financialInputRequestDbl.setOtherIncome(osDetails.getAddOtherRevenueIncome() * denomination);
		financialInputRequestString.setOtherIncome(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherIncome()).toString());
		financialInputRequestDbl.setInterest(osDetails.getInterest() * denomination);
		financialInputRequestString.setInterest(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getInterest()).toString());
		financialInputRequestDbl.setDepriciation(osDetails.getDepreciation() * denomination);
		financialInputRequestString.setDepriciation(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getDepriciation()).toString());
		financialInputRequestDbl.setExceptionalIncome(osDetails.getNetofNonOpIncomeOrExpenses() * denomination);
		financialInputRequestString.setExceptionalIncome(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getExceptionalIncome()).toString());
		
		osDetailsString.setOtherIncomeNeedTocCheckOp(CommonUtils.convertValueIndianCurrency(osDetails.getOtherIncomeNeedTocCheckOp()).toString());
		financialInputRequestDbl.setOtherIncomeNeedTocCheckOp(osDetails.getOtherIncomeNeedTocCheckOp() * denomination);
		financialInputRequestString.setOtherIncomeNeedTocCheckOp(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherIncomeNeedTocCheckOp()).toString());
		
		osDetailsString.setProvisionForTaxes(CommonUtils.convertValueIndianCurrency(osDetails.getProvisionForTaxes()).toString());
		osDetailsString.setProvisionForDeferredTax(CommonUtils.convertValueIndianCurrency(osDetails.getProvisionForDeferredTax()).toString());
		osDetailsString.setProvisionForTaxTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(osDetails.getProvisionForDeferredTax(), osDetails.getProvisionForTaxes())).toString());
		financialInputRequestDbl.setProvisionForTax((osDetails.getProvisionForTaxes() + osDetails.getProvisionForDeferredTax()) * denomination);
		financialInputRequestString.setProvisionForTax(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getProvisionForTax()).toString());
		financialInputRequestDbl.setDividendPayOut(osDetails.getEquityDeividendPaidAmt() * denomination);
		financialInputRequestString.setDividendPayOut(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getDividendPayOut()).toString());

		/************************************************ LIABILITIES DETAIL ***************************************************/
		//LiabilitiesDetails liabilitiesDetails = liabilitiesDetailsRepository.getLiabilitiesDetails(applicationId, year+""); // PREVIOUS
		LiabilitiesDetails liabilitiesDetails = liabilitiesDetailsRepository.getLiabilitiesDetailByProposal(proposalId,year+"");// NEW BASED ON PROPOSAL ID
		if(CommonUtils.isObjectNullOrEmpty(liabilitiesDetails)) {
			liabilitiesDetails = new LiabilitiesDetails();
		}
		liabilitiesDetailsString.setOrdinarySharesCapital(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOrdinarySharesCapital()).toString());
		liabilitiesDetailsString.setPreferencesShares(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getPreferencesShares()).toString());
		liabilitiesDetailsString.setShareCapitalTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(liabilitiesDetails.getOrdinarySharesCapital(), liabilitiesDetails.getPreferencesShares())).toString());
		financialInputRequestDbl.setShareCapital((liabilitiesDetails.getPreferencesShares() + liabilitiesDetails.getOrdinarySharesCapital()) * denomination);
		financialInputRequestString.setShareCapital(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getShareCapital()).toString());
		financialInputRequestDbl.setShareWarrantOutstandings((liabilitiesDetails.getShareWarrentsOutstanding()) * denomination);
		financialInputRequestString.setShareWarrantOutstandings(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getShareWarrantOutstandings()).toString());
		financialInputRequestDbl.setRevalationReserve((liabilitiesDetails.getRevaluationReservse()) * denomination);
		financialInputRequestString.setRevalationReserve(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getRevalationReserve()).toString());
		
		liabilitiesDetailsString.setGeneralReserve(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getGeneralReserve()).toString());
		liabilitiesDetailsString.setOtherReservse(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOtherReservse()).toString());
		liabilitiesDetailsString.setSurplusOrDeficit(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getSurplusOrDeficit()).toString());
		liabilitiesDetailsString.setOthers(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOthers()).toString());
		liabilitiesDetailsString.setOtherReservesTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(liabilitiesDetails.getGeneralReserve(),liabilitiesDetails.getOtherReservse(), liabilitiesDetails.getSurplusOrDeficit(),liabilitiesDetails.getOthers())).toString());
		financialInputRequestDbl.setOtherReserveAndSurplus((liabilitiesDetails.getGeneralReserve() + liabilitiesDetails.getOtherReservse() + liabilitiesDetails.getSurplusOrDeficit() + liabilitiesDetails.getOthers()) * denomination);
		financialInputRequestString.setOtherReserveAndSurplus(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherReserveAndSurplus()).toString());
		financialInputRequestDbl.setMinorityInterest(liabilitiesDetails.getMinorityInterest() * denomination);
		financialInputRequestString.setMinorityInterest(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getMinorityInterest()).toString());
		financialInputRequestDbl.setSecuredLoans(liabilitiesDetails.getTermLiabilitiesSecured() * denomination);
		financialInputRequestString.setSecuredLoans(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getSecuredLoans()).toString());
		financialInputRequestDbl.setUnsecuredLoansPromoters(liabilitiesDetails.getOtherNclUnsecuredLoansFromPromoters() * denomination);
		financialInputRequestString.setUnsecuredLoansPromoters(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getUnsecuredLoansPromoters()).toString());
		
		liabilitiesDetailsString.setTermLiabilitiesUnsecured(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getTermLiabilitiesUnsecured()).toString());
		liabilitiesDetailsString.setOtherNclUnsecuredLoansFromOther(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOtherNclUnsecuredLoansFromOther()).toString());
		liabilitiesDetailsString.setUnsecuredLoansOthersTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(liabilitiesDetails.getTermLiabilitiesUnsecured(), liabilitiesDetails.getOtherNclUnsecuredLoansFromOther())).toString());
		financialInputRequestDbl.setUnsecuredLoansOthers((liabilitiesDetails.getOtherNclUnsecuredLoansFromOther() + liabilitiesDetails.getTermLiabilitiesUnsecured()) * denomination);
		financialInputRequestString.setUnsecuredLoansOthers(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getUnsecuredLoansOthers()).toString());

		liabilitiesDetailsString.setSubTotalA(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getSubTotalA()).toString());
		liabilitiesDetailsString.setShortTermBorrowingFromOthers(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getShortTermBorrowingFromOthers()).toString());
		liabilitiesDetailsString.setOtherBorrowingsTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(liabilitiesDetails.getSubTotalA(), liabilitiesDetails.getShortTermBorrowingFromOthers())).toString());
		financialInputRequestDbl.setOtherBorrowing((liabilitiesDetails.getSubTotalA() + liabilitiesDetails.getShortTermBorrowingFromOthers()) * denomination);
		financialInputRequestString.setOtherBorrowing(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherBorrowing()).toString());
		financialInputRequestDbl.setDeferredTaxLiablities(liabilitiesDetails.getDeferredTaxLiability() * denomination);
		financialInputRequestString.setDeferredTaxLiablities(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getDeferredTaxLiablities()).toString());

		liabilitiesDetailsString.setOtherNcl(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOtherNcl()).toString());
		liabilitiesDetailsString.setDeferredPaymentsCredits(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getDeferredPaymentsCredits()).toString());
		liabilitiesDetailsString.setTermDeposits(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getTermDeposits()).toString());
		liabilitiesDetailsString.setDebentures(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getDebentures()).toString());
		liabilitiesDetailsString.setOtherTermLiabilies(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOtherTermLiabilies()).toString());
		liabilitiesDetailsString.setOtherLongTermLiabilitiesTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(liabilitiesDetails.getOtherNcl(),liabilitiesDetails.getDeferredPaymentsCredits(),liabilitiesDetails.getTermDeposits(), liabilitiesDetails.getDebentures(), liabilitiesDetails.getOtherTermLiabilies())).toString());
		financialInputRequestDbl.setOtherLongTermLiablities((liabilitiesDetails.getOtherNclOthers() + liabilitiesDetails.getDeferredPaymentsCredits() + liabilitiesDetails.getTermDeposits() + liabilitiesDetails.getDebentures() + liabilitiesDetails.getOtherTermLiabilies()) * denomination);
		financialInputRequestString.setOtherLongTermLiablities(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherLongTermLiablities()).toString());
		financialInputRequestDbl.setLongTermProvision(liabilitiesDetails.getOtherNclLongTermProvisions() * denomination);
		financialInputRequestString.setLongTermProvision(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getLongTermProvision()).toString());
		financialInputRequestDbl.setTradePayables(liabilitiesDetails.getSundryCreditors() * denomination);
		financialInputRequestString.setTradePayables(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getTradePayables()).toString());
		
		liabilitiesDetailsString.setAdvancePaymentsFromCustomers(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getAdvancePaymentsFromCustomers()).toString());
		liabilitiesDetailsString.setDividendPayable(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getDividendPayable()).toString());
		liabilitiesDetailsString.setOtherStatutoryLiability(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOtherStatutoryLiability()).toString());
		liabilitiesDetailsString.setDepositsOrInstalmentsOfTermLoans(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans()).toString());
		liabilitiesDetailsString.setOtherCurrentLiability(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOtherCurrentLiability()).toString());
		liabilitiesDetailsString.setOtherCurrentLiabilitiesTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(liabilitiesDetails.getAdvancePaymentsFromCustomers(),liabilitiesDetails.getDividendPayable(),liabilitiesDetails.getOtherStatutoryLiability(),liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans(),liabilitiesDetails.getOtherCurrentLiability())).toString());
		financialInputRequestDbl.setOtherCurruntLiablities((liabilitiesDetails.getAdvancePaymentsFromCustomers() + liabilitiesDetails.getDividendPayable() + liabilitiesDetails.getOtherStatutoryLiability() + liabilitiesDetails.getOtherCurrentLiability() + liabilitiesDetails.getDepositsOrInstalmentsOfTermLoans()) * denomination);
		financialInputRequestString.setOtherCurruntLiablities(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherCurruntLiablities()).toString());
		financialInputRequestDbl.setShortTermProvision(liabilitiesDetails.getProvisionalForTaxation() * denomination);
		financialInputRequestString.setShortTermProvision(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getShortTermProvision()).toString());
		
		liabilitiesDetailsString.setOtherIncomeNeedTocCheckLia(CommonUtils.convertValueIndianCurrency(liabilitiesDetails.getOtherIncomeNeedTocCheckLia()).toString());
		financialInputRequestDbl.setOtherIncomeNeedTocCheckLia(liabilitiesDetails.getOtherIncomeNeedTocCheckLia() * denomination);
		financialInputRequestString.setOtherIncomeNeedTocCheckLia(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherIncomeNeedTocCheckLia()).toString());
		
		/************************************************ ASSETS DETAIL ***************************************************/
		//AssetsDetails assetsDetails = assetsDetailsRepository.getAssetsDetails(applicationId, year+""); // PREVIOUS
		AssetsDetails assetsDetails = assetsDetailsRepository.getAssetsDetailByProposal(proposalId, year+"");// NEW BASED ON PROPOSAL ID
		if(CommonUtils.isObjectNullOrEmpty(assetsDetails)) {
			assetsDetails = new AssetsDetails();
		}
		
		financialInputRequestDbl.setGrossBlock(assetsDetails.getGrossBlock() * denomination);
		financialInputRequestString.setGrossBlock(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getGrossBlock()).toString());
		financialInputRequestDbl.setLessAccumulatedDepre(assetsDetails.getDepreciationToDate() * denomination);
		financialInputRequestString.setLessAccumulatedDepre(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getLessAccumulatedDepre()).toString());
		financialInputRequestDbl.setImpairmentofAsset(assetsDetails.getImpairmentAsset() * denomination);
		financialInputRequestString.setImpairmentofAsset(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getImpairmentofAsset()).toString());
		financialInputRequestDbl.setCapitalWorkInProgress(assetsDetails.getOtherNcaOtherCapitalWorkInprogress() * denomination);
		financialInputRequestString.setCapitalWorkInProgress(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getCapitalWorkInProgress()).toString());
		financialInputRequestDbl.setIntengibleAssets(assetsDetails.getIntangibleAssets() * denomination);
		financialInputRequestString.setIntengibleAssets(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getIntengibleAssets()).toString());
		financialInputRequestDbl.setPreOperativeExpe(assetsDetails.getOthersPreOperativeExpensesPending() * denomination);
		financialInputRequestString.setPreOperativeExpe(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getPreOperativeExpe()).toString());
		financialInputRequestDbl.setAssetInTransit(assetsDetails.getOthersAssetsInTransit() * denomination);
		financialInputRequestString.setAssetInTransit(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getAssetInTransit()).toString());
		financialInputRequestDbl.setInvestmentInSubsidiaries(assetsDetails.getInvestmentsInSubsidiary() * denomination);
		financialInputRequestString.setInvestmentInSubsidiaries(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getInvestmentInSubsidiaries()).toString());
		
		assetDetailsString.setInvestmentsOrBookDebtsString(CommonUtils.convertValueIndianCurrency(assetsDetails.getInvestmentsOrBookDebts()).toString());
		assetDetailsString.setDeferredReceviables(CommonUtils.convertValueIndianCurrency(assetsDetails.getDeferredReceviables()).toString());
		assetDetailsString.setOthers(CommonUtils.convertValueIndianCurrency(assetsDetails.getOthers()).toString());
		assetDetailsString.setOtherInvestmentsTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(assetsDetails.getInvestmentsOrBookDebts(), assetsDetails.getDeferredReceviables(),assetsDetails.getOthersOther())).toString());
		assetDetailsString.setOthersOther(CommonUtils.convertValueIndianCurrency(assetsDetails.getOthersOther()).toString());
		financialInputRequestDbl.setOtherInvestment((assetsDetails.getOthersOther() + assetsDetails.getDeferredReceviables() + assetsDetails.getOthers()) * denomination);
		financialInputRequestString.setOtherInvestment(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherInvestment()).toString());
		
		financialInputRequestDbl.setLongTermLoansAndAdva(assetsDetails.getAdvanceToSuppliersCapitalGoods() * denomination);
		financialInputRequestString.setLongTermLoansAndAdva(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getLongTermLoansAndAdva()).toString());
		
		assetDetailsString.setNonConsumableStoreAndSpares(assetsDetails.getNonConsumableStoreAndSpares() != null ? CommonUtils.convertValueIndianCurrency(assetsDetails.getNonConsumableStoreAndSpares()).toString() : null);
		assetDetailsString.setOtherNonCurrentAssets(CommonUtils.convertValueIndianCurrency(assetsDetails.getOtherNonCurrentAssets()).toString());
		assetDetailsString.setOtherNonCurrentAssestsTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(assetsDetails.getNonConsumableStoreAndSpares(),assetsDetails.getOtherNonCurrentAssets())).toString());
		financialInputRequestDbl.setOtheNonCurruntAsset((assetsDetails.getNonConsumableStoreAndSpares() + assetsDetails.getOtherNonCurrentAssets()) * denomination);
		financialInputRequestString.setOtheNonCurruntAsset(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtheNonCurruntAsset()).toString());
		financialInputRequestDbl.setInventories(assetsDetails.getInventory() * denomination);
		financialInputRequestString.setInventories(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getInventories()).toString());
		
		assetDetailsString.setReceivableOtherThanDefferred(CommonUtils.convertValueIndianCurrency(assetsDetails.getReceivableOtherThanDefferred()).toString());
		assetDetailsString.setExportReceivables(CommonUtils.convertValueIndianCurrency(assetsDetails.getExportReceivables()).toString());
		assetDetailsString.setSundryDebtorsTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(assetsDetails.getReceivableOtherThanDefferred(),assetsDetails.getExportReceivables())).toString());
		financialInputRequestDbl.setSundryDebtors((assetsDetails.getReceivableOtherThanDefferred() + assetsDetails.getExportReceivables()) * denomination);
		financialInputRequestString.setSundryDebtors(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getSundryDebtors()).toString());
		financialInputRequestDbl.setCashAndBank(assetsDetails.getCashAndBankBalance() * denomination);
		financialInputRequestString.setCashAndBank(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getCashAndBank()).toString());
		
		assetDetailsString.setInvestments(CommonUtils.convertValueIndianCurrency(assetsDetails.getInvestments()).toString());
		assetDetailsString.setInstalmentsDeferred(CommonUtils.convertValueIndianCurrency(assetsDetails.getInstalmentsDeferred()).toString());
		assetDetailsString.setOtherCurrentAssets(CommonUtils.convertValueIndianCurrency(assetsDetails.getOtherCurrentAssets()).toString());
		assetDetailsString.setOtherCurrentAssetsTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(assetsDetails.getInvestments(),assetsDetails.getInstalmentsDeferred(), assetsDetails.getOtherCurrentAssets())).toString());
		financialInputRequestDbl.setOtherCurruntAsset((assetsDetails.getInvestments() + assetsDetails.getInstalmentsDeferred() + assetsDetails.getOtherCurrentAssets()) * denomination);
		financialInputRequestString.setOtherCurruntAsset(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherCurruntAsset()).toString());
		
		assetDetailsString.setAdvanceToSupplierRawMaterials(CommonUtils.convertValueIndianCurrency(assetsDetails.getAdvanceToSupplierRawMaterials()).toString());
		assetDetailsString.setAdvancePaymentTaxes(CommonUtils.convertValueIndianCurrency(assetsDetails.getAdvancePaymentTaxes()).toString());
		assetDetailsString.setShortTermLoansAndAdvancesTotal(CommonUtils.convertValueIndianCurrency(CommonUtils.addNumbers(assetsDetails.getAdvanceToSupplierRawMaterials(),assetsDetails.getAdvancePaymentTaxes() )).toString());
		financialInputRequestDbl.setShortTermLoansAdvances((assetsDetails.getAdvanceToSupplierRawMaterials() + assetsDetails.getAdvancePaymentTaxes()) * denomination);
		financialInputRequestString.setShortTermLoansAdvances(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getShortTermLoansAdvances()).toString());
		if(corporateFinalInfoRequest == null) {
			financialInputRequestDbl.setContingentLiablities(null);
			financialInputRequestString.setContingentLiablities(null);
		}
		else {
			financialInputRequestDbl.setContingentLiablities(CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getContLiabilityFyAmt()) ? 0.0 : (corporateFinalInfoRequest.getContLiabilityFyAmt()* denomination));
			financialInputRequestString.setContingentLiablities(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getContingentLiablities()).toString());
		}
		
		assetDetailsString.setOtherIncomeNeedTocCheckAsset(CommonUtils.convertValueIndianCurrency(assetsDetails.getOtherIncomeNeedTocCheckAsset()).toString());
		financialInputRequestDbl.setOtherIncomeNeedTocCheckAsset(assetsDetails.getOtherIncomeNeedTocCheckAsset() * denomination);
		financialInputRequestString.setOtherIncomeNeedTocCheckAsset(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOtherIncomeNeedTocCheckAsset()).toString());
		assetDetailsString.setCurrentRatio(CommonUtils.convertValue(assetsDetails.getCurrentRatio()).toString());
		
		/************************************************** OTHER CALCULATIONS *******************************************************/ 
		//Profit & Loss Statement
        financialInputRequestDbl.setNetSale(CommonUtils.substractNumbers(financialInputRequestDbl.getGrossSales(), financialInputRequestDbl.getLessExciseDuity()));
      	financialInputRequestString.setNetSale(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getNetSale()).toString());
		financialInputRequestDbl.setTotalExpenditure(CommonUtils.substractNumbers(CommonUtils.addNumbers(financialInputRequestDbl.getIncreaseDecreaseStock(),financialInputRequestDbl.getRawMaterialConsumed(),financialInputRequestDbl.getPowerAndFuelCost(),financialInputRequestDbl.getEmployeeCost(), financialInputRequestDbl.getGeneralAndAdminExpe(),financialInputRequestDbl.getSellingAndDistriExpe(),financialInputRequestDbl.getMiscelExpe()), financialInputRequestDbl.getLessExpeCapita()));
		financialInputRequestString.setTotalExpenditure(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getTotalExpenditure()).toString());
		financialInputRequestDbl.setOperatingProfitExclOi(CommonUtils.substractNumbers(financialInputRequestDbl.getNetSale(),financialInputRequestDbl.getTotalExpenditure()));
		financialInputRequestString.setOperatingProfitExclOi(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOperatingProfitExclOi()).toString());
		financialInputRequestDbl.setOperatingProfitEbitadOi(CommonUtils.addNumbers(financialInputRequestDbl.getOperatingProfitExclOi(),financialInputRequestDbl.getOtherIncome()));
		financialInputRequestString.setOperatingProfitEbitadOi(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getOperatingProfitEbitadOi()).toString());
		financialInputRequestDbl.setPbdt(CommonUtils.substractNumbers(financialInputRequestDbl.getOperatingProfitEbitadOi(), financialInputRequestDbl.getInterest()));
		financialInputRequestString.setPbdt(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getPbdt()).toString());
		financialInputRequestDbl.setProfitBeforeTaxation(CommonUtils.substractNumbers(financialInputRequestDbl.getPbdt(), financialInputRequestDbl.getDepriciation()));
		financialInputRequestString.setProfitBeforeTaxation(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getProfitBeforeTaxation()).toString());
		financialInputRequestDbl.setProfitBeforeTax(CommonUtils.addNumbers(financialInputRequestDbl.getProfitBeforeTaxation(), financialInputRequestDbl.getExceptionalIncome()));
		financialInputRequestString.setProfitBeforeTax(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getProfitBeforeTax()).toString());
		financialInputRequestDbl.setProfitAfterTax(CommonUtils.substractNumbers(financialInputRequestDbl.getProfitBeforeTax(), financialInputRequestDbl.getProvisionForTax()) + financialInputRequestDbl.getOtherIncomeNeedTocCheckOp());
		financialInputRequestString.setProfitAfterTax(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getProfitAfterTax()).toString());
		if(financialInputRequestDbl.getDividendPayOut() == 0 || CommonUtils.isObjectNullOrEmpty(financialInputRequestDbl.getDividendPayOut()) || financialInputRequestDbl.getShareFaceValue() == 0 || CommonUtils.isObjectNullOrEmpty(financialInputRequestDbl.getShareFaceValue()) || financialInputRequestDbl.getShareCapital() == 0 || CommonUtils.isObjectNullOrEmpty(financialInputRequestDbl.getShareCapital()))
			financialInputRequestString.setEquityDividend("0");
		else
			financialInputRequestString.setEquityDividend(CommonUtils.convertValueIndianCurrency((financialInputRequestDbl.getDividendPayOut()*financialInputRequestDbl.getShareFaceValue()/financialInputRequestDbl.getShareCapital())).toString());
		
		if(financialInputRequestDbl.getShareFaceValue() !=0 && financialInputRequestDbl.getShareCapital() !=0) {
			double total = financialInputRequestDbl.getShareFaceValue()/financialInputRequestDbl.getShareCapital();
			if(!CommonUtils.isObjectNullOrEmpty(financialInputRequestDbl.getProfitAfterTax()) && financialInputRequestDbl.getProfitAfterTax() !=0) {
				financialInputRequestString.setEarningPerShare(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getProfitAfterTax() * total).toString());
			}
		}
		
		//Balance Sheet -Equities and Liabilities
		
		financialInputRequestDbl.setShareHolderFunds(CommonUtils.addNumbers(financialInputRequestDbl.getShareCapital(),financialInputRequestDbl.getShareWarrantOutstandings(),financialInputRequestDbl.getRevalationReserve(),financialInputRequestDbl.getOtherReserveAndSurplus()));
		financialInputRequestString.setShareHolderFunds(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getShareHolderFunds()).toString());
		financialInputRequestDbl.setTotalNonCurruntLiablities(CommonUtils.addNumbers(financialInputRequestDbl.getMinorityInterest(), financialInputRequestDbl.getSecuredLoans(), financialInputRequestDbl.getUnsecuredLoansOthers(),financialInputRequestDbl.getUnsecuredLoansPromoters(),financialInputRequestDbl.getDeferredTaxLiablities(),financialInputRequestDbl.getOtherLongTermLiablities(),financialInputRequestDbl.getOtherBorrowing(),financialInputRequestDbl.getLongTermProvision()));
		financialInputRequestString.setTotalNonCurruntLiablities(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getTotalNonCurruntLiablities()).toString());
		financialInputRequestDbl.setTotalCurruntLiablities(CommonUtils.addNumbers(financialInputRequestDbl.getTradePayables(), financialInputRequestDbl.getOtherCurruntLiablities(), financialInputRequestDbl.getShortTermProvision()));
		financialInputRequestString.setTotalCurruntLiablities(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getTotalCurruntLiablities()).toString());
		financialInputRequestDbl.setTotalLiablities(CommonUtils.addNumbers(financialInputRequestDbl.getShareHolderFunds(), financialInputRequestDbl.getTotalNonCurruntLiablities(), financialInputRequestDbl.getTotalCurruntLiablities(), financialInputRequestDbl.getOtherIncomeNeedTocCheckLia()));
		financialInputRequestString.setTotalLiablities(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getTotalLiablities()).toString());

		//Balance Sheet -ASSETS
		financialInputRequestDbl.setNetBlock(CommonUtils.substractThreeNumbers(financialInputRequestDbl.getGrossBlock(), financialInputRequestDbl.getLessAccumulatedDepre(),financialInputRequestDbl.getImpairmentofAsset()));
		financialInputRequestString.setNetBlock(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getNetBlock()).toString());
		financialInputRequestDbl.setTotalNonCurruntAsset(CommonUtils.addNumbers(financialInputRequestDbl.getCapitalWorkInProgress(), financialInputRequestDbl.getIntengibleAssets(), financialInputRequestDbl.getPreOperativeExpe(), financialInputRequestDbl.getAssetInTransit(), financialInputRequestDbl.getInvestmentInSubsidiaries(), financialInputRequestDbl.getOtherInvestment(), financialInputRequestDbl.getLongTermLoansAndAdva(), financialInputRequestDbl.getOtheNonCurruntAsset()));
		financialInputRequestString.setTotalNonCurruntAsset(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getTotalNonCurruntAsset()).toString());
		financialInputRequestDbl.setTotalCurruntAsset(CommonUtils.addNumbers(financialInputRequestDbl.getInventories(), financialInputRequestDbl.getSundryDebtors(), financialInputRequestDbl.getCashAndBank(), financialInputRequestDbl.getOtherCurruntAsset(), financialInputRequestDbl.getShortTermLoansAdvances()));
		financialInputRequestString.setTotalCurruntAsset(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getTotalCurruntAsset()).toString());
		financialInputRequestDbl.setTotalAsset(CommonUtils.addNumbers(financialInputRequestDbl.getNetBlock(), financialInputRequestDbl.getTotalCurruntAsset(), financialInputRequestDbl.getTotalNonCurruntAsset(), financialInputRequestDbl.getOtherIncomeNeedTocCheckAsset()));
		financialInputRequestString.setTotalAsset(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getTotalAsset()).toString());
		if(financialInputRequestDbl.getShareFaceValue() !=0 && financialInputRequestDbl.getShareCapital() !=0) {
			double total = financialInputRequestDbl.getShareCapital()/financialInputRequestDbl.getShareFaceValue();
			if(!CommonUtils.isObjectNullOrEmpty(financialInputRequestDbl.getShareHolderFunds()) && financialInputRequestDbl.getShareHolderFunds() !=0) {
				financialInputRequestString.setBookValue(CommonUtils.convertValueIndianCurrency(financialInputRequestDbl.getShareHolderFunds() / total).toString());
			}
		}
		
		return new Object[] {osDetailsString, liabilitiesDetailsString, assetDetailsString , financialInputRequestString , financialInputRequestDbl };
	}
	
	public void calculateRatioAnalysis(Map<Integer, Object[]>financials,Long toapplicationId,Long itrId) {
		int currentYear = scoringService.getFinYear(itrId);
		DecimalFormat decim = new DecimalFormat("###.##");
		Object[] curFinYear = financials.get(currentYear - 1);
		Object[] prevFinYear = financials.get(currentYear - 2);
		Object[] yrBeforePrevFinYear = financials.get(currentYear - 3);
		
		FinancialInputRequestString curFinYearString =  (FinancialInputRequestString)curFinYear[curFinYear.length - 2];
		FinancialInputRequestString prevFinYearString =  (FinancialInputRequestString)prevFinYear[prevFinYear.length - 2];
		FinancialInputRequestString yrBeforePrevFinYearString =  (FinancialInputRequestString)yrBeforePrevFinYear[yrBeforePrevFinYear.length - 2];
		
		FinancialInputRequestDbl curFinYearDouble =  (FinancialInputRequestDbl)curFinYear[curFinYear.length - 1];
		FinancialInputRequestDbl prevFinYearDouble =  (FinancialInputRequestDbl)prevFinYear[prevFinYear.length - 1];
		FinancialInputRequestDbl yrBeforePrevFinYearDouble =  (FinancialInputRequestDbl)yrBeforePrevFinYear[yrBeforePrevFinYear.length - 1];
		
		 //CASH FLOW 
		curFinYearDouble.setEbitda(curFinYearDouble.getOperatingProfitEbitadOi());
		prevFinYearDouble.setEbitda(prevFinYearDouble.getOperatingProfitEbitadOi());
		curFinYearString.setEbitda(CommonUtils.convertValue(curFinYearDouble.getEbitda()));
		prevFinYearString.setEbitda(CommonUtils.convertValue(prevFinYearDouble.getEbitda()));
		
		curFinYearDouble.setInterestPaid(curFinYearDouble.getInterest());
		prevFinYearDouble.setInterestPaid(prevFinYearDouble.getInterest());
		curFinYearString.setInterestPaid(CommonUtils.convertValue(curFinYearDouble.getInterestPaid()));
		prevFinYearString.setInterestPaid(CommonUtils.convertValue(prevFinYearDouble.getInterestPaid()));
		
		curFinYearDouble.setIncreaseWorkingCapital(curFinYearDouble.getInventories()+curFinYearDouble.getSundryDebtors()+curFinYearDouble.getOtherCurruntAsset()-prevFinYearDouble.getInventories()-prevFinYearDouble.getSundryDebtors()-prevFinYearDouble.getOtherCurruntAsset()+prevFinYearDouble.getTradePayables()+prevFinYearDouble.getOtherCurruntLiablities()+prevFinYearDouble.getShortTermProvision()-curFinYearDouble.getTradePayables()-curFinYearDouble.getOtherCurruntLiablities()-curFinYearDouble.getShortTermProvision());
		prevFinYearDouble.setIncreaseWorkingCapital(prevFinYearDouble.getInventories()+prevFinYearDouble.getSundryDebtors()+prevFinYearDouble.getOtherCurruntAsset()-yrBeforePrevFinYearDouble.getInventories()-yrBeforePrevFinYearDouble.getSundryDebtors()-yrBeforePrevFinYearDouble.getOtherCurruntAsset()+yrBeforePrevFinYearDouble.getTradePayables()+yrBeforePrevFinYearDouble.getOtherCurruntLiablities()+yrBeforePrevFinYearDouble.getShortTermProvision()-prevFinYearDouble.getTradePayables()-prevFinYearDouble.getOtherCurruntLiablities()-prevFinYearDouble.getShortTermProvision());
		curFinYearString.setIncreaseWorkingCapital(CommonUtils.convertValue(curFinYearDouble.getIncreaseWorkingCapital()));
		prevFinYearString.setIncreaseWorkingCapital(CommonUtils.convertValue(prevFinYearDouble.getIncreaseWorkingCapital()));
		
		curFinYearDouble.setTaxPaid(curFinYearDouble.getProvisionForTax());
		prevFinYearDouble.setTaxPaid(prevFinYearDouble.getProvisionForTax());
		curFinYearString.setTaxPaid(CommonUtils.convertValue(curFinYearDouble.getTaxPaid()));
		prevFinYearString.setTaxPaid(CommonUtils.convertValue(prevFinYearDouble.getTaxPaid()));
		
		curFinYearDouble.setCashFromOperating(curFinYearDouble.getEbitda()-curFinYearDouble.getInterestPaid()-curFinYearDouble.getIncreaseWorkingCapital()-curFinYearDouble.getTaxPaid());
		prevFinYearDouble.setCashFromOperating(prevFinYearDouble.getEbitda()-prevFinYearDouble.getInterestPaid()-prevFinYearDouble.getIncreaseWorkingCapital()-prevFinYearDouble.getTaxPaid());
		curFinYearString.setCashFromOperating(CommonUtils.convertValue(curFinYearDouble.getCashFromOperating()));
		prevFinYearString.setCashFromOperating(CommonUtils.convertValue(prevFinYearDouble.getCashFromOperating()));
		
		//RATIO ANALYSIS
		curFinYearString.setEbitadPercentage(CommonUtils.convertValue(CommonUtils.divideNumbers(curFinYearDouble.getOperatingProfitEbitadOi(), curFinYearDouble.getNetSale())* 100));
		prevFinYearString.setEbitadPercentage(CommonUtils.convertValue(CommonUtils.divideNumbers(prevFinYearDouble.getOperatingProfitEbitadOi(), prevFinYearDouble.getNetSale())* 100));
		yrBeforePrevFinYearString.setEbitadPercentage(CommonUtils.convertValue(CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getOperatingProfitEbitadOi(), yrBeforePrevFinYearDouble.getNetSale())* 100));
		
		curFinYearString.setPatm(CommonUtils.convertValue(CommonUtils.divideNumbers(curFinYearDouble.getProfitAfterTax(), curFinYearDouble.getNetSale())* 100));
		prevFinYearString.setPatm(CommonUtils.convertValue(CommonUtils.divideNumbers(prevFinYearDouble.getProfitAfterTax(), prevFinYearDouble.getNetSale())* 100));
		yrBeforePrevFinYearString.setPatm(CommonUtils.convertValue(CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getProfitAfterTax(), yrBeforePrevFinYearDouble.getNetSale())* 100));
		
		curFinYearString.setRoce(CommonUtils.convertValue(((curFinYearDouble.getOperatingProfitEbitadOi()*2/(CommonUtils.addNumbers(curFinYearDouble.getShareHolderFunds(), prevFinYearDouble.getShareHolderFunds(), curFinYearDouble.getTotalNonCurruntLiablities(), prevFinYearDouble.getTotalNonCurruntLiablities())))*12/curFinYearDouble.getNoOfMonth())* 100));
		prevFinYearString.setRoce(CommonUtils.convertValue(((prevFinYearDouble.getOperatingProfitEbitadOi()*2/(CommonUtils.addNumbers(prevFinYearDouble.getShareHolderFunds(), yrBeforePrevFinYearDouble.getShareHolderFunds(), prevFinYearDouble.getTotalNonCurruntLiablities(), yrBeforePrevFinYearDouble.getTotalNonCurruntLiablities())))*12/prevFinYearDouble.getNoOfMonth())* 100));
		yrBeforePrevFinYearString.setRoce("NA");
		
		curFinYearString.setAssetTurnOver(CommonUtils.convertValue(CommonUtils.divideNumbers(curFinYearDouble.getNetSale() * 12, (CommonUtils.multiplyNumbers(curFinYearDouble.getTotalAsset(), curFinYearDouble.getNoOfMonth())))));
		prevFinYearString.setAssetTurnOver(CommonUtils.convertValue(CommonUtils.divideNumbers(prevFinYearDouble.getNetSale() * 12, (CommonUtils.multiplyNumbers(prevFinYearDouble.getTotalAsset(), prevFinYearDouble.getNoOfMonth())))));
		yrBeforePrevFinYearString.setAssetTurnOver(CommonUtils.convertValue(CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getNetSale() * 12, (CommonUtils.multiplyNumbers(yrBeforePrevFinYearDouble.getTotalAsset(), yrBeforePrevFinYearDouble.getNoOfMonth())))));
		
		curFinYearString.setInventoryTurnOver(CommonUtils.convertValue(CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(curFinYearDouble.getTotalExpenditure()*12, (curFinYearDouble.getInventories()*curFinYearDouble.getNoOfMonth()))))));
		prevFinYearString.setInventoryTurnOver(CommonUtils.convertValue(CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(prevFinYearDouble.getTotalExpenditure()*12, (prevFinYearDouble.getInventories()*prevFinYearDouble.getNoOfMonth()))))));
		yrBeforePrevFinYearString.setInventoryTurnOver(CommonUtils.convertValue(CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getTotalExpenditure()*12, (yrBeforePrevFinYearDouble.getInventories()*yrBeforePrevFinYearDouble.getNoOfMonth()))))));
		
		curFinYearString.setDebtorsTurnover(CommonUtils.convertValue(CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(curFinYearDouble.getNetSale()*12, (curFinYearDouble.getSundryDebtors()*curFinYearDouble.getNoOfMonth()))))));
		prevFinYearString.setDebtorsTurnover(CommonUtils.convertValue(CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(prevFinYearDouble.getNetSale()*12, (prevFinYearDouble.getSundryDebtors()*prevFinYearDouble.getNoOfMonth()))))));
		yrBeforePrevFinYearString.setDebtorsTurnover(CommonUtils.convertValue(CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getNetSale()*12, (yrBeforePrevFinYearDouble.getSundryDebtors()*yrBeforePrevFinYearDouble.getNoOfMonth()))))));
		
		curFinYearString.setCreditorsTurnover(CommonUtils.convertValue((CommonUtils.divideNumbers(365.0, CommonUtils.divideNumbers((CommonUtils.addNumbers(curFinYearDouble.getRawMaterialConsumed(), curFinYearDouble.getPowerAndFuelCost())), curFinYearDouble.getTradePayables())))*12/curFinYearDouble.getNoOfMonth()));
		prevFinYearString.setCreditorsTurnover(CommonUtils.convertValue((CommonUtils.divideNumbers(365.0, CommonUtils.divideNumbers((CommonUtils.addNumbers(prevFinYearDouble.getRawMaterialConsumed(), prevFinYearDouble.getPowerAndFuelCost())), prevFinYearDouble.getTradePayables())))*12/prevFinYearDouble.getNoOfMonth()));
		yrBeforePrevFinYearString.setCreditorsTurnover(CommonUtils.convertValue((CommonUtils.divideNumbers(365.0, CommonUtils.divideNumbers((CommonUtils.addNumbers(yrBeforePrevFinYearDouble.getRawMaterialConsumed(), yrBeforePrevFinYearDouble.getPowerAndFuelCost())), yrBeforePrevFinYearDouble.getTradePayables())))*12/yrBeforePrevFinYearDouble.getNoOfMonth()));
		
		curFinYearString.setSalesWorkingCapital(CommonUtils.convertValue((CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(curFinYearDouble.getNetSale(), (CommonUtils.addNumbers(curFinYearDouble.getInventories(),curFinYearDouble.getSundryDebtors())-curFinYearDouble.getTradePayables())))))*12 /curFinYearDouble.getNoOfMonth()));
		prevFinYearString.setSalesWorkingCapital(CommonUtils.convertValue((CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(prevFinYearDouble.getNetSale(), (CommonUtils.addNumbers(prevFinYearDouble.getInventories(),prevFinYearDouble.getSundryDebtors())-prevFinYearDouble.getTradePayables())))))*12 /prevFinYearDouble.getNoOfMonth()));
		yrBeforePrevFinYearString.setSalesWorkingCapital(CommonUtils.convertValue((CommonUtils.divideNumbers(365.0, (CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getNetSale(), (CommonUtils.addNumbers(yrBeforePrevFinYearDouble.getInventories(),yrBeforePrevFinYearDouble.getSundryDebtors())-yrBeforePrevFinYearDouble.getTradePayables())))))*12 /yrBeforePrevFinYearDouble.getNoOfMonth()));
		
		curFinYearString.setNetSalesGrowthCapital(CommonUtils.convertValue((CommonUtils.divideNumbers(curFinYearDouble.getNetSale(),prevFinYearDouble.getNetSale())-1)* 100));
		prevFinYearString.setNetSalesGrowthCapital(CommonUtils.convertValue((CommonUtils.divideNumbers(prevFinYearDouble.getNetSale(),yrBeforePrevFinYearDouble.getNetSale())-1)* 100));
		yrBeforePrevFinYearString.setNetSalesGrowthCapital("NA");
		
		curFinYearString.setPatGrowthCapital(CommonUtils.convertValue((CommonUtils.divideNumbers(curFinYearDouble.getProfitAfterTax(),prevFinYearDouble.getProfitAfterTax())-1)* 100));
		prevFinYearString.setPatGrowthCapital(CommonUtils.convertValue((CommonUtils.divideNumbers(prevFinYearDouble.getProfitAfterTax(),yrBeforePrevFinYearDouble.getProfitAfterTax())-1)* 100));
		yrBeforePrevFinYearString.setPatGrowthCapital("NA");
		
		curFinYearDouble.setAdjustedTotalDebtEquity(Double.parseDouble(decim.format(CommonUtils.divideNumbers((CommonUtils.substractThreeNumbers(curFinYearDouble.getTotalNonCurruntLiablities(),curFinYearDouble.getLongTermProvision(),curFinYearDouble.getUnsecuredLoansPromoters())), (CommonUtils.addNumbers(curFinYearDouble.getShareHolderFunds(),curFinYearDouble.getUnsecuredLoansPromoters()))))));
		prevFinYearDouble.setAdjustedTotalDebtEquity(Double.parseDouble(decim.format(CommonUtils.divideNumbers((CommonUtils.substractThreeNumbers(prevFinYearDouble.getTotalNonCurruntLiablities(),prevFinYearDouble.getLongTermProvision(),prevFinYearDouble.getUnsecuredLoansPromoters())), (CommonUtils.addNumbers(prevFinYearDouble.getShareHolderFunds(),prevFinYearDouble.getUnsecuredLoansPromoters()))))));
		yrBeforePrevFinYearDouble.setAdjustedTotalDebtEquity(Double.parseDouble(decim.format(CommonUtils.divideNumbers((CommonUtils.substractThreeNumbers(yrBeforePrevFinYearDouble.getTotalNonCurruntLiablities(),yrBeforePrevFinYearDouble.getLongTermProvision(),yrBeforePrevFinYearDouble.getUnsecuredLoansPromoters())), (CommonUtils.addNumbers(yrBeforePrevFinYearDouble.getShareHolderFunds(),yrBeforePrevFinYearDouble.getUnsecuredLoansPromoters()))))));
		curFinYearString.setAdjustedTotalDebtEquity(CommonUtils.convertValue(curFinYearDouble.getAdjustedTotalDebtEquity()));
		prevFinYearString.setAdjustedTotalDebtEquity(CommonUtils.convertValue(prevFinYearDouble.getAdjustedTotalDebtEquity()));
		yrBeforePrevFinYearString.setAdjustedTotalDebtEquity(CommonUtils.convertValue(yrBeforePrevFinYearDouble.getAdjustedTotalDebtEquity()));
		
		curFinYearString.setGrowthDebtEquity(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractNumbers(curFinYearDouble.getAdjustedTotalDebtEquity(),prevFinYearDouble.getAdjustedTotalDebtEquity())), prevFinYearDouble.getAdjustedTotalDebtEquity())*100));
		prevFinYearString.setGrowthDebtEquity(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractNumbers(prevFinYearDouble.getAdjustedTotalDebtEquity(),yrBeforePrevFinYearDouble.getAdjustedTotalDebtEquity())), yrBeforePrevFinYearDouble.getAdjustedTotalDebtEquity())*100));
		yrBeforePrevFinYearString.setGrowthDebtEquity("NA");
		
		curFinYearString.setCurruntRatioX(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.addNumbers(curFinYearDouble.getInventories(),curFinYearDouble.getSundryDebtors())), curFinYearDouble.getTradePayables())));
		prevFinYearString.setCurruntRatioX(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.addNumbers(prevFinYearDouble.getInventories(),prevFinYearDouble.getSundryDebtors())), prevFinYearDouble.getTradePayables())));
		yrBeforePrevFinYearString.setCurruntRatioX(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.addNumbers(yrBeforePrevFinYearDouble.getInventories(),yrBeforePrevFinYearDouble.getSundryDebtors())), yrBeforePrevFinYearDouble.getTradePayables())));
		
		curFinYearString.setQuickRatioX(CommonUtils.convertValue(CommonUtils.divideNumbers(curFinYearDouble.getSundryDebtors(),curFinYearDouble.getTradePayables())));
		prevFinYearString.setQuickRatioX(CommonUtils.convertValue(CommonUtils.divideNumbers(prevFinYearDouble.getSundryDebtors(),prevFinYearDouble.getTradePayables())));
		yrBeforePrevFinYearString.setQuickRatioX(CommonUtils.convertValue(CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getSundryDebtors(),yrBeforePrevFinYearDouble.getTradePayables())));
		
		try {
			CGTMSECalcDataResponse response = loanApplicationService.getDataForCGTMSE(toapplicationId);
			if(!CommonUtils.isObjectNullOrEmpty(response)) {
				if(response.getSubSector() != null && response.getSubSector().equals("Manufacturer")) {
					curFinYearString.setCashInterestCover(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.addNumbers(curFinYearDouble.getCashFromOperating(),curFinYearDouble.getInterestPaid())), curFinYearDouble.getInterestPaid())));
					prevFinYearString.setCashInterestCover(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.addNumbers(prevFinYearDouble.getCashFromOperating(),prevFinYearDouble.getInterestPaid())), prevFinYearDouble.getInterestPaid())));
					yrBeforePrevFinYearString.setCashInterestCover("NA");
				}else {
					curFinYearString.setCashInterestCover(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractNumbers(curFinYearDouble.getOperatingProfitEbitadOi(),curFinYearDouble.getProvisionForTax())), curFinYearDouble.getInterest())));
					prevFinYearString.setCashInterestCover(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractNumbers(prevFinYearDouble.getOperatingProfitEbitadOi(),prevFinYearDouble.getProvisionForTax())), prevFinYearDouble.getInterest())));
					yrBeforePrevFinYearString.setCashInterestCover(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractNumbers(yrBeforePrevFinYearDouble.getOperatingProfitEbitadOi(),yrBeforePrevFinYearDouble.getProvisionForTax())), yrBeforePrevFinYearDouble.getInterest())));
				}
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		curFinYearString.setDebtEbitad(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractThreeNumbers(curFinYearDouble.getTotalNonCurruntLiablities(),curFinYearDouble.getUnsecuredLoansPromoters(),curFinYearDouble.getLongTermProvision())), (12*CommonUtils.divideNumbers(curFinYearDouble.getOperatingProfitEbitadOi(),curFinYearDouble.getNoOfMonth())))));
		prevFinYearString.setDebtEbitad(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractThreeNumbers(prevFinYearDouble.getTotalNonCurruntLiablities(),prevFinYearDouble.getUnsecuredLoansPromoters(),prevFinYearDouble.getLongTermProvision())), (12*CommonUtils.divideNumbers(prevFinYearDouble.getOperatingProfitEbitadOi(),prevFinYearDouble.getNoOfMonth())))));
		yrBeforePrevFinYearString.setDebtEbitad(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractThreeNumbers(yrBeforePrevFinYearDouble.getTotalNonCurruntLiablities(),yrBeforePrevFinYearDouble.getUnsecuredLoansPromoters(),yrBeforePrevFinYearDouble.getLongTermProvision())), (12*CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getOperatingProfitEbitadOi(),yrBeforePrevFinYearDouble.getNoOfMonth())))));
		
		curFinYearString.setFreeReserveEquity(CommonUtils.convertValue(CommonUtils.divideNumbers(curFinYearDouble.getOtherReserveAndSurplus(), (CommonUtils.addNumbers(curFinYearDouble.getShareCapital(),curFinYearDouble.getShareWarrantOutstandings())))));
		prevFinYearString.setFreeReserveEquity(CommonUtils.convertValue(CommonUtils.divideNumbers(prevFinYearDouble.getOtherReserveAndSurplus(), (CommonUtils.addNumbers(prevFinYearDouble.getShareCapital(),prevFinYearDouble.getShareWarrantOutstandings())))));
		yrBeforePrevFinYearString.setFreeReserveEquity(CommonUtils.convertValue(CommonUtils.divideNumbers(yrBeforePrevFinYearDouble.getOtherReserveAndSurplus(), (CommonUtils.addNumbers(yrBeforePrevFinYearDouble.getShareCapital(),yrBeforePrevFinYearDouble.getShareWarrantOutstandings())))));
		
		curFinYearDouble.setCfoMargine(CommonUtils.divideNumbers(curFinYearDouble.getCashFromOperating(),curFinYearDouble.getNetSale())*100);
		prevFinYearDouble.setCfoMargine(CommonUtils.divideNumbers(prevFinYearDouble.getCashFromOperating(),prevFinYearDouble.getNetSale())*100);
		curFinYearString.setCfoMargine(CommonUtils.convertValue(curFinYearDouble.getCfoMargine()));
		prevFinYearString.setCfoMargine(CommonUtils.convertValue(prevFinYearDouble.getCfoMargine()));
		yrBeforePrevFinYearString.setCfoMargine("NA");
		
		curFinYearString.setGrowthCfoMargine(CommonUtils.convertValue(CommonUtils.divideNumbers((CommonUtils.substractNumbers(curFinYearDouble.getCfoMargine(),prevFinYearDouble.getCfoMargine())), prevFinYearDouble.getCfoMargine()) *100));
		prevFinYearString.setGrowthCfoMargine("NA");
		yrBeforePrevFinYearString.setGrowthCfoMargine("NA");
		
		
		curFinYear[curFinYear.length - 2] = curFinYearString;
		prevFinYear[prevFinYear.length - 2] = prevFinYearString;
		yrBeforePrevFinYear[yrBeforePrevFinYear.length - 2] = yrBeforePrevFinYearString;
		
		financials.put((currentYear - 1), curFinYear);
		financials.put((currentYear - 2), prevFinYear);
		financials.put((currentYear - 3), yrBeforePrevFinYear);
		logger.info("financials========={}",financials.toString());
	}
	
	
	/*********************************************************CAM UTILS****************************************************************/
	
	
	
	@SuppressWarnings("unchecked")
	private String getCityName(Long cityId) {
		try {
			if(CommonUtils.isObjectNullOrEmpty(cityId)) {
				return null;
			}
			List<Long> cityList = new ArrayList<>(1);
			cityList.add(cityId);
			OneFormResponse oneFormResponse = oneFormClient.getCityByCityListId(cityList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
					.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper
						.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
				return masterResponse.getValue();
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	private String getStateName(Integer stateId) {
		try {
			if(CommonUtils.isObjectNullOrEmpty(stateId)) {
				return null;
			}
			List<Long> stateList = new ArrayList<>(1);
			stateList.add(stateId.longValue());
			OneFormResponse oneFormResponse = oneFormClient.getStateByStateListId(stateList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
					.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper
						.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
			    return StringEscapeUtils.escapeXml(masterResponse.getValue());

			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	private String getCountryName(Integer country) {
		try {
			if(CommonUtils.isObjectNullOrEmpty(country)) {
				return null;
			}
			List<Long> countryList = new ArrayList<>(1);
			countryList.add(country.longValue());
			OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
					.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper
						.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
				return masterResponse.getValue();
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return null;
	}

	@Override
	public FinancialInputRequest finaForCam(Long aplicationId,Long proposalId,Long itrId) {
		FinancialInputRequest financialInputRequest = new FinancialInputRequest();
		OperatingStatementDetails operatingStatementDetails;
		HashMap<String, Object> yearSalesPurchase = new HashMap<>();
		int currentYear = scoringService.getFinYear(itrId);
		List<Map<String, Object>> financialYearAndSalesAndPurchase = new ArrayList<>();
		try {
			if(proposalId != null) {
				operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByProposal(proposalId, currentYear-1+"");
			}else {
				operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByAppIdAndFinYear(aplicationId, currentYear-1+"");
			}
				if(operatingStatementDetails != null) {
					yearSalesPurchase.put("year",currentYear-1);
					yearSalesPurchase.put("itrSales",(operatingStatementDetails.getDomesticSales()+operatingStatementDetails.getExportSales()));
					yearSalesPurchase.put("rowMaterialIndigenous",operatingStatementDetails.getRawMaterialsIndigenous());
					financialYearAndSalesAndPurchase.add(yearSalesPurchase);
				}
		}catch (Exception e) {
			logger.info("Exception in getting financial fist year details {}",e);
		}
		try {
			yearSalesPurchase = new HashMap<>();
			if(proposalId != null) {
				operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByProposal(proposalId, currentYear-2+"");
			}else {
				operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByAppIdAndFinYear(aplicationId, currentYear-2+"");
			}
			if(operatingStatementDetails != null) {
				yearSalesPurchase.put("year",currentYear-2);
				yearSalesPurchase.put("itrSales",(operatingStatementDetails.getDomesticSales()+operatingStatementDetails.getExportSales()));
				yearSalesPurchase.put("rowMaterialIndigenous",operatingStatementDetails.getRawMaterialsIndigenous());
				financialYearAndSalesAndPurchase.add(yearSalesPurchase);
			}
		}catch (Exception e) {
			logger.info("Exception in getting financial fist year details {}",e);
		}
		
		try {
			yearSalesPurchase = new HashMap<>();
			if(proposalId != null) {
				operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByProposal(proposalId, currentYear-3+"");
			}else {
				operatingStatementDetails = operatingStatementDetailsRepository.getOperatingStatementDetailsByAppIdAndFinYear(aplicationId, currentYear-3+"");
			}
			if(operatingStatementDetails != null) {
				yearSalesPurchase.put("year",currentYear-3);
				yearSalesPurchase.put("itrSales",(operatingStatementDetails.getDomesticSales()+operatingStatementDetails.getExportSales()));
				yearSalesPurchase.put("rowMaterialIndigenous",operatingStatementDetails.getRawMaterialsIndigenous());
				financialYearAndSalesAndPurchase.add(yearSalesPurchase);
			}
		}catch (Exception e) {
			logger.info("Exception in getting financial fist year details {}",e);
		}
		financialInputRequest.setYearSalesPurchasList(financialYearAndSalesAndPurchase);
		return financialInputRequest;
	}
	
	public Map<String ,Object> getBranchDetails(Long applicationId ,Long userId ,Long proposalId){
		//Fetch Bank Details
		Map<String, Object> bankData = new HashMap<String, Object>();
		try {
			Long orgId = null;
			if(proposalId != null) {
				orgId = proposalDetailsRepository.getOrgIdByProposalId(proposalId);
			}else {
				orgId = ineligibleProposalDetailsRepository.getOrgId(applicationId);
			}
			List<Object[]> listBankData = commonRepository.getBankDetails(applicationId, orgId);
			if(!CommonUtils.isListNullOrEmpty(listBankData) && !CommonUtils.isObjectNullOrEmpty(listBankData.get(0))) {
				
				String bankAddress = (listBankData.get(0)[5] != null ? listBankData.get(0)[5] : "") + (listBankData.get(0)[6] != null ? " ," + listBankData.get(0)[6] : "") 
						+ (listBankData.get(0)[7] != null ? " ," +listBankData.get(0)[7] : "") + (listBankData.get(0)[8] != null ? " - " + listBankData.get(0)[8] : "");
				bankData.put("currentBankAddress", !CommonUtils.isObjectNullOrEmpty(bankAddress) ? StringEscapeUtils.escapeXml(bankAddress) : "-");
				bankData.put("bankName", listBankData.get(0)[9] != null ? listBankData.get(0)[9] : "-");
				if(listBankData.size() > 1 && !CommonUtils.isObjectNullOrEmpty(listBankData.get(1))) {
					String prevBankAddress = (listBankData.get(1)[5] != null ? listBankData.get(1)[5] : "") + (listBankData.get(1)[6] != null ? " ," + listBankData.get(1)[6] : "") 
							+ (listBankData.get(1)[7] != null ? " ," +listBankData.get(1)[7] : "") + (listBankData.get(1)[8] != null ? " - " + listBankData.get(1)[8] : "");
					bankData.put("previousBankAddress", !CommonUtils.isObjectNullOrEmpty(bankAddress) ? StringEscapeUtils.escapeXml(prevBankAddress) : "-");
				}
			}
			
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
					bankRelationshipRequest.setSinceWhen((bankRelationshipRequest.getSinceYear() != null ? bankRelationshipRequest.getSinceYear() +" year " : "") + " " +(bankRelationshipRequest.getSinceMonth() != null ? bankRelationshipRequest.getSinceMonth()+" months" :  "" ));
				}
            	bankRelationshipRequests.add(bankRelationshipRequest);
            }
            
            bankData.put("bankingRelationShip", bankRelationshipRequests);
			
			try {
				Object[] campaignDetails = loanRepoImpl.getApplicationCampaignDetails(applicationId);
				if(campaignDetails != null && campaignDetails.length > 0) {
					if(campaignDetails.length > 2 && campaignDetails[2] != null) { // loanCampCode
						bankData.put("typeOfUser", "Bank Specific");
					}else if (campaignDetails[3] != null && CommonUtils.convertString(campaignDetails[3]).equalsIgnoreCase("cii")) {
						bankData.put("typeOfUser", "Market Place");
					}else {
						bankData.put("typeOfUser", "Market Place");
					}
				}
	        } catch (Exception e2) {
	            logger.info("error while campaign user check ==>" , e2);
	        }
			
			return !bankData.isEmpty() ? bankData : null;
		}catch (Exception e) {
			logger.error("Error/Exception while getting Bank Details Of ApplicationId==>{}  ..Error==>{}",applicationId ,e);
		}
		return null;
	}
	
	//MATCHES COMMON METHOD
	public Map<String ,Object> getMatchesAndEligiblityDetails(Long applicationId ,Long productId, Long proposalId){
		//Fetch Matches Details
		Map<String, Object> map = new HashMap<String, Object>();
		
		//MATCHES RESPONSE FOR ONLINE CAM
		try {
			MatchRequest matchRequest = new MatchRequest();
			matchRequest.setApplicationId(applicationId);
			if (proposalId != null) {
				matchRequest.setProductId(productId);
			}
			MatchDisplayResponse matchResponse= matchEngineClient.displayMatchesOfCorporate(matchRequest);
			map.put("matchesResponse", !CommonUtils.isObjectNullOrEmpty(matchResponse.getMatchDisplayObjectMap()) ? CommonUtils.printFields(matchResponse.getMatchDisplayObjectMap(),null) : " ");

		}
		catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		try {
			if(proposalId != null) {
				EligibililityRequest eligibilityReq=new EligibililityRequest();
				eligibilityReq.setApplicationId(applicationId);
				eligibilityReq.setFpProductId(productId);
				EligibilityResponse eligibilityResp = eligibilityClient.corporateEligibilityData(eligibilityReq);
			
				if(!CommonUtils.isObjectListNull(eligibilityResp.getData())){
					CalculationJSON req= MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)eligibilityResp.getData(), CalculationJSON.class);
					
//					map.put("elProSales", req.getProjectedSales() != null ? CommonUtils.convertValueIndianCurrency(req.getProjectedSales())  : "-");
//					map.put("defaultHisSales", req.getDefaultHistoricSales() != null ? CommonUtils.convertValueIndianCurrency(req.getDefaultHistoricSales())  : "-");
					map.put("assLimits",CommonUtils.convertToDoubleForXmlIndianCurr(req, new HashMap<>()));
					map.put("financialYear",CommonUtils.getFinancialYear());
				}
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION, e);
		}
		
		
		return map;
	}
	
	public String getMonth(int month) {
	    return new DateFormatSymbols().getMonths()[month-1];
	}
	
	//GST COMMON METHOD
			@Override
			public List<CAMGSTData> getGstDetails(Long applicationId ,Long userId ,Long gstMasterId){
				//Fetch Bank Details
				//GST DATA
				
				CorporateApplicantRequest corporateApplicantRequest = corporateApplicantService.getCorporateApplicant(applicationId);
				if (corporateApplicantRequest != null) {			
					
					try {
						GSTR1Request req= new GSTR1Request();
						req.setApplicationId(gstMasterId);
						req.setUserId(userId);						
						req.setGstin(corporateApplicantRequest.getGstIn() != null ? corporateApplicantRequest.getGstIn() : commonRepository.getGSTInFromConnectWithApplicationIdAndProposalId(applicationId));	
						List<CAMGSTData> resp = new ArrayList<>();
						GstResponse response = gstClient.detailCalculation(req);
						
						DecimalFormat df = new DecimalFormat(".##");
						if (!CommonUtils.isObjectNullOrEmpty(response) && response.getData() != null) {
							for (LinkedHashMap<String, Object> data : (List<LinkedHashMap<String, Object>>) response.getData()) {
							 CAMGSTData	resp1 = MultipleJSONObjectHelper.getObjectFromMap(data,CAMGSTData.class);
								if(resp1.getMomSales() != null) {
			                        List<MomSales> momSalesResp1 = resp1.getMomSales();
			                        List<MomSales> responseMom= new ArrayList<>();
			                        
			                        for (MomSales sales1 : momSalesResp1) {
			                        	String str = sales1.getMonth();
			                        	sales1.setMonth(getMonth(Integer.valueOf(str.substring(0, 2)).intValue()) + " " +str.substring(2));
			                        	//sales1.setMonth(str.insert(2, '-').toString());
			                        	sales1.setValue((String)CommonUtils.convertValueIndianCurrency(Double.valueOf(sales1.getValue())));
			                        	sales1.setIsManualEntry(sales1.getIsManualEntry());
			                            responseMom.add(sales1);
			                        }
			                        /*data.put("monthWiseMomSales", responseMom);*/
			                        resp1.setMonthWiseMomSales(responseMom);
			                        // get only last 12 month gst data
			                        List<MomSales> last12momSales = responseMom.subList(responseMom.size() - 12 , responseMom.size());
			                        resp1.setLastTwelveMonthMomSales(last12momSales);
			                        
			                        // calculate last 12 month mom total
			                        Double last12totalSales =0.0d;
			                        for (MomSales sales : last12momSales) {
			                        	last12totalSales += Double.valueOf(CommonUtils.convertStringCurrencyToDouble(sales.getValue()));
									}
			                        resp1.setLast12totalSales(last12totalSales);
			                        if(!CommonUtils.isObjectNullOrEmpty(last12totalSales)) {
										Double last12totalSalesToConvert = (Double) last12totalSales;
										BigDecimal convertedLst12TotalSales = BigDecimal.valueOf(last12totalSalesToConvert).setScale(2);
										resp1.setLast12totalSalesInString(convertedLst12TotalSales.toString());
									}
			                        
			                    }
								Double totalSales =0.0d;
								if(resp1.getMomSales() != null) {
									List<MomSales> momSalesResp = resp1.getMomSales();
									for (MomSales sales : momSalesResp) {
										
										totalSales += Double.valueOf(CommonUtils.convertStringCurrencyToDouble(sales.getValue()));
									}
									//map.put("totalMomSales", df.format(totalSales));
									 resp1.setTotalMomSales(totalSales); 
									if(!CommonUtils.isObjectNullOrEmpty(totalSales)) {
										Double momSalesOrig = (Double) totalSales;
										BigDecimal convertedTotalMomSales = BigDecimal.valueOf(momSalesOrig).setScale(2);
										resp1.setTotalMomSalesInString(convertedTotalMomSales.toString());
									}							
								}
								Map<String, Object> convertExpVal = convertExpVal(resp1);
								resp1.getGstNotApplicable().setMomSalesBigDecimal(convertExpVal);
								resp.add(resp1);
							}		
							
		/*					List<LinkedHashMap<String, Object>> dataMapList =  (List<LinkedHashMap<String, Object>>) resp;*/
							
							
							return resp;
						}else {
							logger.info("Gst Data Null for==>"+applicationId);
						}		
					}catch(Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
				
				return null;
			}
			
			private Map<String,Object> convertExpVal(CAMGSTData dataMapList) {
				LinkedHashMap<String, Object> momSalesmodified = new LinkedHashMap<>();
				
					Map<Integer, Map<String, Double>> momSales = new HashMap<>();
				
					if(dataMapList.getGstNotApplicable().getMonthSalesDetails() != null && dataMapList.getGstNotApplicable().getMonthSalesDetails().size() > 0) {
						List<MonthListSales> listData = dataMapList.getGstNotApplicable().getMonthSalesDetails();
						Map<String, Double> mapData = new HashMap<>();
						for(MonthListSales data : listData) {
							mapData.put(data.getKey(), Double.valueOf(data.getValue()));
						}
						momSales.put(1 , mapData);
					}else {
						momSales = dataMapList.getGstNotApplicable().getMomSales();
					}
					
					for (Map<String,Double> momSalesMapValuesMap : momSales.values()) {
						for (Entry<String,Double> entry : momSalesMapValuesMap.entrySet()) {
							Double value = (Double) entry.getValue();
							BigDecimal convertedVal = BigDecimal.valueOf(value).setScale(2);
							String strKey = entry.getKey().toString();
							momSalesmodified.put(getMonth(Integer.valueOf(strKey.substring(0, 2)).intValue()) + " " +strKey.substring(2), convertedVal.toString());
						}
					}
				return momSalesmodified;
			}		
	
	//ONE_FORM COMMON
	public Map<String ,Object> getOneFormData(Long applicationId, Long proposalId,Long userId , Long itrMasterId){
		
		LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.getByIdAndUserId(applicationId, userId);
		
		ApplicationProposalMapping applicationProposalMapping = null;
        if(proposalId != null) {
        	applicationProposalMapping = applicationProposalMappingRepository.getByApplicationIdAndProposalId(applicationId, proposalId);
        }else {
        	applicationProposalMapping = applicationProposalMappingRepository.getByApplicationId(applicationId);
		}
		
		
		//Fetch Bank Details
		Map<String, Object> map = new HashMap<String, Object>();
		CorporateApplicantRequest corporateApplicantRequest = corporateApplicantService.getCorporateApplicant(applicationId);
		DecimalFormat decim = new DecimalFormat("####");
		
		/*
		 * Long userId =
		 * loanApplicationRepository.getUserIdByApplicationId(applicationId);
		 */
		String categoryType = "";
		PrimaryCorporateDetail primaryCorporateDetail = primaryCorporateRepository.getByApplicationAndUserId(applicationId, userId);
		if(!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail)) {
			map.put("loanAmtApplied", loanApplicationMaster != null && WcRenewalType.RENEWAL.getId().equals(loanApplicationMaster.getWcRenewalStatus()) ? (primaryCorporateDetail.getLoanAmount() != null ? CommonUtils.convertValueIndianCurrency(primaryCorporateDetail.getLoanAmount()) : 0)
					: (primaryCorporateDetail.getAdditionalLoanAmount() != null ? CommonUtils.convertValueIndianCurrency(primaryCorporateDetail.getAdditionalLoanAmount()) : 0));
			if (primaryCorporateDetail.getLoanAmount() != null) {
				if (primaryCorporateDetail.getLoanAmount() <= 50000) {
					categoryType = "Shishu"; 
//					map.put("categoryType", "Shishu");
				}
				if(primaryCorporateDetail.getLoanAmount() >= 50001 && primaryCorporateDetail.getLoanAmount() <= 500000){
					categoryType = "Kishor"; 
//					map.put("categoryType", "Kishor");
				}
				if(primaryCorporateDetail.getLoanAmount() >= 500001 && primaryCorporateDetail.getLoanAmount() <= 1000000){
					categoryType = "Tarun"; 
//					map.put("categoryType", "Tarun");
				}
			}	
			map.put("categoryType", categoryType);
			map.put("comercialOpDate",!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getCommercialOperationDate())? CommonUtils.DATE_FORMAT.format(primaryCorporateDetail.getCommercialOperationDate()):"-");
			map.put("factoryPremise", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getFactoryPremise())? StringEscapeUtils.escapeXml(FactoryPremiseMst.getById(primaryCorporateDetail.getFactoryPremise()).getValue()) : "-");
			map.put("knowHow", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getKnowHow())? StringEscapeUtils.escapeXml(KnowHowMst.getById(primaryCorporateDetail.getKnowHow()).getValue()) : "-");
			map.put("competition", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getCompetition())? StringEscapeUtils.escapeXml(CompetitionMst_SBI.getById(primaryCorporateDetail.getCompetition()).getValue()) : "-");
			map.put("productDesc", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getProductServiceDescription()) ? StringEscapeUtils.escapeXml(primaryCorporateDetail.getProductServiceDescription()) : null);
			map.put("incrementalTurnOver", primaryCorporateDetail.getIncrementalTurnover()!= null ? CommonUtils.convertValueIndianCurrency(primaryCorporateDetail.getIncrementalTurnover()) : 0);
			map.put("incrementalMarginMudra", primaryCorporateDetail.getIncrementalMargin()!= null ? CommonUtils.convertValueIndianCurrency(primaryCorporateDetail.getIncrementalMargin()) : 0);
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			map.put("commOperationDate", primaryCorporateDetail.getCommercialOperationDate() != null ? df.format(primaryCorporateDetail.getCommercialOperationDate()) : "-");
			map.put("totalCostOfMachineryForMudraLoan", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getCostOfMachinery())? primaryCorporateDetail.getCostOfMachinery() : "-");
			//map.put("additionalLimit", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getAdditionalLoanAmount()) ? primaryCorporateDetail.getAdditionalLoanAmount() : "-" );
			//map.put("costOfMachinery", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getCostOfMachinery()) ? primaryCorporateDetail.getCostOfMachinery() : "-" );
			
			//Scientific value removal
                        if (primaryCorporateDetail.getAdditionalLoanAmount() != null) {
                            Double addiLoanAmt = primaryCorporateDetail.getAdditionalLoanAmount();
				BigDecimal additionalLoanAmt = BigDecimal.valueOf(addiLoanAmt).setScale(0);
				map.put("additionalLimit", additionalLoanAmt.toString());    
                        } else {
                            map.put("additionalLimit", 0);    
                        }
                        
			if (primaryCorporateDetail.getIncrementalTurnover() != null) {
				Double increTurnover = primaryCorporateDetail.getIncrementalTurnover();
				BigDecimal incrementalTurnover = BigDecimal.valueOf(increTurnover).setScale(0);
				map.put("incrementalTurnover", incrementalTurnover.toString());
			}
			if (primaryCorporateDetail.getCostOfMachinery() != null) {
				Double machineCost = primaryCorporateDetail.getCostOfMachinery();
				BigDecimal costOfMachinery = BigDecimal.valueOf(machineCost).setScale(0);
				map.put("costOfMachinery", costOfMachinery.toString());
			}
			 
			//map.put("incrementalTurnover", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getIncrementalTurnover()) ? primaryCorporateDetail.getIncrementalTurnover() : "-");
			map.put("incrementalMargin", !CommonUtils.isObjectNullOrEmptyOrDash(primaryCorporateDetail.getIncrementalMargin()) ? primaryCorporateDetail.getIncrementalMargin() : "-");			
			map.put("borrowerDeclaredProjSales", !CommonUtils.isObjectNullOrEmptyOrDash(primaryCorporateDetail.getBorrowerDcldProjectedSales()) ? primaryCorporateDetail.getBorrowerDcldProjectedSales() : "-");			
                        FinancialArrangementsDetailRequest req = arrangementsDetailServiceImpl.getTotalEmiAndSanctionAmountByApplicationId(applicationId);
                        req.getAmount();
                        if (req.getAmount() != null) {
                            Double existingLim = req.getAmount();
				BigDecimal existingLimit = BigDecimal.valueOf(existingLim).setScale(0);
				map.put("existingLimit", existingLimit.toString());
                        }else{
                                map.put("existingLimit", 0);
                        }
                        //map.put("existingLimit", !CommonUtils.isObjectNullOrEmpty(req.getAmount()) ? req.getAmount() : "0");                       
			if (primaryCorporateDetail.getAdditionalLoanAmount() != null) {
                            if (req.getAmount() != null) {
                                Double totLimit = primaryCorporateDetail.getAdditionalLoanAmount() + req.getAmount();
                                BigDecimal totalLimit = BigDecimal.valueOf(totLimit).setScale(0);				
				map.put("totalLimit", totalLimit.toString());
                            } else {
                                Double totLimit = primaryCorporateDetail.getAdditionalLoanAmount() + 0;
                                BigDecimal totalLimit = BigDecimal.valueOf(totLimit).setScale(0);				
				map.put("totalLimit", totalLimit.toString());
                            }				
			} else {
				map.put("totalLimit", "-");
			}
                        
			Boolean isHaveCollateralSecu =  primaryCorporateDetail.getHaveCollateralSecurity();
			map.put("isHaveCollateralSecu", isHaveCollateralSecu == null ? "-" : isHaveCollateralSecu ? "Yes" : "No");
			
			Boolean isAllowSwitchExistingLender = primaryCorporateDetail.getIsAllowSwitchExistingLender();
			map.put("isAllowSwitchExistingLender", isAllowSwitchExistingLender == null ? "-" : isAllowSwitchExistingLender ? "Yes" : "No");
			
			Boolean isIsoCertified = primaryCorporateDetail.getIsIsoCertified();
			map.put("isIsoCertified", isIsoCertified == null ? "-" : isIsoCertified ? "Yes" : "No");
			
			if (proposalId != null) {
				map.put("loanType", !CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getProductId()) ? CommonUtils.LoanType.getType(applicationProposalMapping.getProductId()).getName() : " ");	
			} else {
				map.put("loanType", !CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getPurposeOfLoanId())
						? PurposeOfLoan.getById(primaryCorporateDetail.getPurposeOfLoanId()).getValue().toString()
						: " ");
			}
		}
		
		//ONE-FORM DATA			
		if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest)) {
			try {
	    		map.put("corporateApplicant", CommonUtils.printFields(corporateApplicantRequest ,null));
				map.put("orgName", corporateApplicantRequest.getOrganisationName());
				map.put("constitution", !CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getConstitutionId()) ? StringEscapeUtils.escapeXml(Constitution.getById(corporateApplicantRequest.getConstitutionId()).getValue()) : " ");
				
				
				String establishMentYear = !CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getEstablishmentMonth())? EstablishmentMonths.getById(corporateApplicantRequest.getEstablishmentMonth()).getValue(): "";
				if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getEstablishmentYear())) {
					try {
						OneFormResponse establishmentYearResponse = oneFormClient.getYearByYearId(CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getEstablishmentYear()) ? null: corporateApplicantRequest.getEstablishmentYear().longValue());
						List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) establishmentYearResponse.getListData();
						if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
							MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
							establishMentYear += " " + masterResponse.getValue();
						}
					} catch (Exception e) {
						logger.error("Error in getting establishment year : ", e);
					}
				}
				map.put("establishmentYr",!CommonUtils.isObjectNullOrEmpty(establishMentYear)? CommonUtils.printFields(establishMentYear, null): " ");

				
				//INDUSTRY DATA
				Integer industry = corporateApplicantRequest.getKeyVericalFunding().intValue();
				map.put("keyVerticalFunding", !CommonUtils.isObjectNullOrEmpty(industry) ? CommonUtils.printFields(Industry.getById(industry).getValue(),null) : " ");

				// key vertical sector
				List<Long> keyVerticalSectorId = new ArrayList<>();
				// getting sector id from mapping
				if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getKeyVerticalSector()))
					keyVerticalSectorId.add(corporateApplicantRequest.getKeyVerticalSector());

				try {
					OneFormResponse formResponse = oneFormClient
							.getIndustrySecByMappingId(corporateApplicantRequest.getKeyVerticalSector());
					// SectorIndustryModel oneResponseDataList = (SectorIndustryModel) formResponse
					// .getData();

					SectorIndustryModel sectorIndustryModel = MultipleJSONObjectHelper
							.getObjectFromMap((Map) formResponse.getData(), SectorIndustryModel.class);

					// get key vertical sector value
					OneFormResponse oneFormResponse = oneFormClient
							.getSectorById(Arrays.asList(sectorIndustryModel.getSectorId()));
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
							.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						map.put("keyVerticalSectorApp", masterResponse.getValue());
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}

				// key vertical Subsector
				try {
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getKeyVerticalSubsector())) {
						OneFormResponse oneFormResponse = oneFormClient
								.getSubSecNameByMappingId(corporateApplicantRequest.getKeyVerticalSubsector());
						map.put("keySubSectorApp", oneFormResponse.getData());
					}
				} catch (Exception e) {
					logger.error("error while getting key vertical sub-sector : ",e);
				}
			}catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
				}
		}
//                                Long orgId = null;
//		                if (proposalId != null) {
//                                orgId = proposalDetailsRepository.getOrgIdByProposalId(proposalId);
//                                } else {
//                                orgId = ineligibleProposalDetailsRepository.getOrgId(applicationId);
//                                }
//                                boolean isCibilCall = loanRepoImpl.getCibilBureauAPITrueOrFalse(orgId);
		//DIRECTOR'S BACKGROUND
				try {
					List<DirectorBackgroundDetailRequest> directorBackgroundDetailRequestList = backgroundDetailsService.getDirectorBackgroundDetailList(applicationId, userId);
					List<DirectorBackgroundDetailResponseString> directorBackgroundDetailResponseList = new ArrayList<>();
					if(!CommonUtils.isObjectListNull(directorBackgroundDetailRequestList)) {
						for (DirectorBackgroundDetailRequest directorBackgroundDetailRequest : directorBackgroundDetailRequestList) {
							DirectorBackgroundDetailResponseString directorBackgroundDetailResponse = new DirectorBackgroundDetailResponseString();
							//directorBackgroundDetailResponse.setAchivements(directorBackgroundDetailRequest.getAchivements());
							directorBackgroundDetailResponse.setAddress(directorBackgroundDetailRequest.getAddress());
							directorBackgroundDetailResponse.setPremiseNumber(directorBackgroundDetailRequest.getPremiseNumber());
							directorBackgroundDetailResponse.setStreetName(directorBackgroundDetailRequest.getStreetName());
							directorBackgroundDetailResponse.setLandmark(directorBackgroundDetailRequest.getLandmark());
							//directorBackgroundDetailResponse.setAge(directorBackgroundDetailRequest.getAge());
							directorBackgroundDetailResponse.setDirectorsName((directorBackgroundDetailRequest.getSalutationId() != null ? Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue() : null )+ " " + directorBackgroundDetailRequest.getDirectorsName());
							if(directorBackgroundDetailRequest.getPanNo() != null) {
								directorBackgroundDetailResponse.setPanNo(directorBackgroundDetailRequest.getPanNo().toUpperCase());
							}
	
							String directorName = "";
							if (directorBackgroundDetailRequest.getSalutationId() != null){
								directorName = Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue();
							}
							directorName += " "+directorBackgroundDetailRequest.getDirectorsName();
							directorBackgroundDetailResponse.setDirectorsName(directorName);
							//directorBackgroundDetailResponse.setQualification(directorBackgroundDetailRequest.getQualification());
							directorBackgroundDetailResponse.setTotalExperience(CommonUtils.convertValueWithoutDecimal(directorBackgroundDetailRequest.getTotalExperience()));
							directorBackgroundDetailResponse.setNetworth(CommonUtils.convertValueIndianCurrency(directorBackgroundDetailRequest.getNetworth()).toString());
							directorBackgroundDetailResponse.setDesignation(directorBackgroundDetailRequest.getDesignation());
							directorBackgroundDetailResponse.setAppointmentDate(directorBackgroundDetailRequest.getAppointmentDate());
							directorBackgroundDetailResponse.setDin(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDin())?decim.format(directorBackgroundDetailRequest.getDin()).toString() : "");
							directorBackgroundDetailResponse.setMobile(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getMobile())?directorBackgroundDetailRequest.getMobile(): " ");
							directorBackgroundDetailResponse.setDob(directorBackgroundDetailRequest.getDob());
							
							if(directorBackgroundDetailRequest.getPanNo().charAt(3) == 'H' ||directorBackgroundDetailRequest.getPanNo().charAt(3) == 'h') {
								directorBackgroundDetailResponse.setCibilScore("HUF");
							}else {                                                            
	                                                        try {
									CibilRequest cibilRequest = new CibilRequest();
									cibilRequest.setPan(directorBackgroundDetailRequest.getPanNo());
									cibilRequest.setApplicationId(applicationId);
	
									List<CibilScoreLogRequest> response = cibilClient.getMultipleProviderScoreByApplicationIdAndPan(cibilRequest);
									if(!CommonUtils.isListNullOrEmpty(response)) {
										for(CibilScoreLogRequest cibilScoreLogRequest : response) {
											String score = cibilScoreLogRequest.getActualScore();
											if("000-1".equalsIgnoreCase(cibilScoreLogRequest.getActualScore())) {
												score = "-1";
											}else {
												score = Integer.valueOf(cibilScoreLogRequest.getActualScore()).toString();								
											}
											if(cibilScoreLogRequest.getScoreName() != null && cibilScoreLogRequest.getScoreName().contains("CIBIL")) {
												directorBackgroundDetailResponse.setCibilScore(score);								
											}else if(cibilScoreLogRequest.getScoreName().contains("Experian")) {
												directorBackgroundDetailResponse.setExperianScore(score);								
											}else if(cibilScoreLogRequest.getScoreName().contains("HIGHMARK")) {
												directorBackgroundDetailResponse.setHighmarkScore(score);								
											}
											
										}
									}
								}catch(Exception e) {
									logger.error("Error while getting cibil details : ",e);
								}                                                    						
							}
							
							Double loanObligation = financialArrangementDetailsService.getTotalOfEmiByApplicationIdAndDirectorId(applicationId,directorBackgroundDetailRequest.getId());
							directorBackgroundDetailResponse.setLoanObligation(!CommonUtils.isObjectNullOrEmpty(loanObligation) ? loanObligation : 0);
							
							directorBackgroundDetailResponse.setPincode(directorBackgroundDetailRequest.getPincode());
							directorBackgroundDetailResponse.setPersonalId(directorBackgroundDetailRequest.getPersonalId());
							
							try {
								if (!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDistrictMappingId())) {
									directorBackgroundDetailResponse.setPinData(pincodeDateService.getById(directorBackgroundDetailRequest.getDistrictMappingId()));
								}
							} catch (Exception e) {
								logger.error(CommonUtils.EXCEPTION,e);
							}
							
							directorBackgroundDetailResponse.setStateCode(directorBackgroundDetailRequest.getStateCode());
							directorBackgroundDetailResponse.setCity(directorBackgroundDetailRequest.getCity());
							directorBackgroundDetailResponse.setGender((!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getGender()) ? Gender.getById(directorBackgroundDetailRequest.getGender()).getValue() : " "));
							directorBackgroundDetailResponse.setRelationshipType((!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getRelationshipType())  ? StringEscapeUtils.escapeXml(DirectorRelationshipType.getById(directorBackgroundDetailRequest.getRelationshipType()).getValue()) : " "));
							directorBackgroundDetailResponse.setIsMainDirector(directorBackgroundDetailRequest.getIsMainDirector());
							directorBackgroundDetailResponse.setAadhar(directorBackgroundDetailRequest.getAadhar());
							directorBackgroundDetailResponse.setFatherName(directorBackgroundDetailRequest.getFatherName());
							directorBackgroundDetailResponse.setEducationalStatus(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getEducationalStatus()) ? StringEscapeUtils.escapeXml(EducationalStatusMudraMst.getById(directorBackgroundDetailRequest.getEducationalStatus()).getValue().toString()) : "-");
							directorBackgroundDetailResponse.setVisuallyImpaired(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getVisuallyImpaired()) ? StringEscapeUtils.escapeXml(VisuallyImpairedMst.getById(directorBackgroundDetailRequest.getVisuallyImpaired()).getValue().toString()) : "-");
							directorBackgroundDetailResponse.setResidentStatus(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getResidentStatus()) ? StringEscapeUtils.escapeXml(ResidentStatusMst.getById(directorBackgroundDetailRequest.getResidentStatus()).getValue().toString()) : "-");
							directorBackgroundDetailResponse.setDirectorPersonalInfo(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest()) ? directorBackgroundDetailRequest.getDirectorPersonalDetailRequest() : " " );
							directorBackgroundDetailResponse.setIsWorkPlaceResidenceSamePlace(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest()) && !CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIsWorkAndResidenceSamePlace()) ? (directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIsWorkAndResidenceSamePlace() != 0 ? "Yes" : "No") : "No" );
							directorBackgroundDetailResponse.setIsPhysicallyhandicapped(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getPhysicallyHandicapped()) ? VisuallyImpairedMst.getById(directorBackgroundDetailRequest.getPhysicallyHandicapped()).toString() : "-");
							directorBackgroundDetailResponse.setHaveOwnCommercialProperty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getHaveOwnCommercialProperty());
						
							//NATIONALITY
							List<Long> countryList = new ArrayList<>();
							if (!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getNationality()))
								countryList.add(Long.valueOf(directorBackgroundDetailRequest.getNationality()));
							if (!CommonUtils.isListNullOrEmpty(countryList)) {
								try {
									OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
									List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
									if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
										MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
										map.put("dirCountry", StringEscapeUtils.escapeXml(masterResponse.getValue()));
										map.put("dirRegOfficeCountry", StringEscapeUtils.escapeXml(masterResponse.getValue()));
										directorBackgroundDetailResponse.setNationality(masterResponse.getValue());
									} else {
										directorBackgroundDetailResponse.setNationality("NA");
									}
								} catch (Exception e) {
									logger.error(CommonUtils.EXCEPTION,e);
								}
							}
							
							try {
								if(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getIsMainDirector()) && directorBackgroundDetailRequest.getIsMainDirector() == true) {
									DirectorPersonalDetailResponse directorPersonalDetailResponse= new DirectorPersonalDetailResponse();
									if(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest())) {	
										directorPersonalDetailResponse.setMaritalStatus(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getMaritalStatus()) ? StringEscapeUtils.escapeXml(MaritalStatusMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getMaritalStatus()).getValue().toString()) : "-");
										directorPersonalDetailResponse.setSpouseName(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getSpouseName()) ? StringEscapeUtils.escapeXml(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getSpouseName()) : "-" );
										directorPersonalDetailResponse.setSpouseDetail(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getSpouseDetail()) ? StringEscapeUtils.escapeXml(SpouseDetailMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getSpouseDetail()).getValue().toString()) : "-");
										directorPersonalDetailResponse.setIdProof(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIdProof()) ? IdProofMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIdProof()).getValue() : "-");
										directorPersonalDetailResponse.setAssessedForIt(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getAssessedForIt()) ? StringEscapeUtils.escapeXml(AssessedForITMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getAssessedForIt()).getValue().toString()) : "-");
										directorPersonalDetailResponse.setOwningHouse(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOwningHouse()) ? StringEscapeUtils.escapeXml(MudraOwningHouseMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOwningHouse()).getValue().toString()) : "-");
										directorPersonalDetailResponse.setNoOfChildren(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getNoOfChildren()) ? directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getNoOfChildren() : 0 );
										directorPersonalDetailResponse.setHaveLiPolicy(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getHaveLiPolicy()) ? StringEscapeUtils.escapeXml(HaveLIMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getHaveLiPolicy()).getValue().toString()) : "-");
	
										Boolean isSameAddIdProof = directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getIsSameAddIdProof(); 									
										directorPersonalDetailResponse.setIsSameAddIdProof((!CommonUtils.isObjectNullOrEmpty(isSameAddIdProof) ? (isSameAddIdProof ? "Yes" : "No") : "No")  + (isSameAddIdProof != null && isSameAddIdProof ? (!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getAddressYears()) ? " (" + directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getAddressYears() + " Years)" : "") : ""));
										
										directorPersonalDetailResponse.setCertificationCourse(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getCertificationCourse()) ? CertificationCourseMst.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getCertificationCourse()).getValue() : "-" );
										directorPersonalDetailResponse.setOtherIncomeSource(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOtherIncomeSource()) ? directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOtherIncomeSource() : 0 );
										map.put("onGoingMudraLoan", directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOngoingMudraLoan() != null ? OngoingMudraLoan.getById(directorBackgroundDetailRequest.getDirectorPersonalDetailRequest().getOngoingMudraLoan()).getValue() : "-"  );
										// COVERED IN GOV_SCHEMES
										List<Integer> govSchemes = fsParameterMappingRepository.getParametersByApplicationIdAndType(applicationId, FSParameterMst.GOV_SCHEMES.getId());
										if (!CommonUtils.isListNullOrEmpty(govSchemes)) {
											String govScheme  = ""; 
											for (int i = 0; i < govSchemes.size(); i++) {
												String authority = GovSchemesMst.getById(govSchemes.get(i)).getValue();
												govScheme = govScheme + ((i != 0) ? ", " : "" )+ authority;
											}
											directorPersonalDetailResponse.setGovScheme(govScheme);
											map.put("govtScheme", govScheme);
										}									
										
										directorBackgroundDetailResponse.setDirectorPersonalInfo(directorPersonalDetailResponse);
									}
								}
							}catch(Exception e) {
								logger.error("error while getting main directors details : ",e);
							}
							directorBackgroundDetailResponseList.add(directorBackgroundDetailResponse);
						}
					}
					map.put("dirBackgroundTitle", corporateApplicantRequest.getPanNo() != null ? (String.valueOf(corporateApplicantRequest.getPanNo().charAt(3)).toUpperCase().equals("P") ? "Proprietor" : String.valueOf(corporateApplicantRequest.getPanNo().charAt(3)).toUpperCase().equals("F") ? "Partners" : "Directors Background") : "Directors Background");
					map.put("dirBackground", !CommonUtils.isListNullOrEmpty(directorBackgroundDetailResponseList) ? CommonUtils.printFields(directorBackgroundDetailResponseList,null) : " ");
				}
				catch (Exception e) {
					logger.error("Error in getting directors background details : ",e);
				}
				
				// REGISTER WITH GOV AUTHORITIES
				List<Integer> govAuthorities = fsParameterMappingRepository.getParametersByApplicationIdAndType(applicationId, FSParameterMst.GOV_AUTHORITIES.getId());
				if (!CommonUtils.isListNullOrEmpty(govAuthorities)) {
					String govAuthValue  = ""; 
					for (int i = 0; i < govAuthorities.size(); i++) {
						String authority = 	RegistrationWithGovernmentAuthoritiesList.fromId(govAuthorities.get(i)).getValue();
						govAuthValue = govAuthValue + ((i != 0) ? ", " : "" )+ authority;
					}
					map.put("govtAuthority", govAuthValue);
				}
				
				
				/* cmr details cibil */
                                    try {
					String cmrScore= cibilClient.getCMRScore(applicationId);
					
					if (cmrScore != null && cmrScore.contains("EXP")) {
						map.put("msmeRankingTitle", "Experian");
					}else if (cmrScore != null && cmrScore.contains("CIBIL")) {
						map.put("msmeRankingTitle", "Cibil");
					}else {
						map.put("msmeRankingTitle", "MSME Ranking");
					}
					map.put("cibilCmrScore", cmrScore != null ? cmrScore : "Not Found");
				} catch (Exception e) {
					
					logger.error("error while getting cmr score : ",e);
                                    }    
								
				//FINANCIAL ARRANGEMENTS
				try {
					List<FinancialArrangementsDetailRequest> financialArrangementsDetailRequestList = financialArrangementDetailsService.getFinancialArrangementDetailsList(applicationId, userId);
		            List<FinancialArrangementDetailResponseString> financialArrangementsDetailResponseList = new ArrayList<>();
		            for (FinancialArrangementsDetailRequest financialArrangementsDetailRequest : financialArrangementsDetailRequestList) {
		            	FinancialArrangementDetailResponseString financialArrangementsDetailResponse = new FinancialArrangementDetailResponseString();
		     			//financialArrangementsDetailResponse.setRelationshipSince(financialArrangementsDetailRequest.getRelationshipSince());
                        if(!CommonUtils.isObjectNullOrEmpty(financialArrangementsDetailRequest.getAmount())) {
                        	Double finAmount = (Double) financialArrangementsDetailRequest.getAmount();
                            BigDecimal convertedfinAmount = BigDecimal.valueOf(finAmount).setScale(2);
                            financialArrangementsDetailRequest.setAmountInString(convertedfinAmount.toString());
                        }else {
                        	financialArrangementsDetailRequest.setAmount(0d);
                        }
                        
		            	
		            	financialArrangementsDetailResponse.setOutstandingAmount(CommonUtils.convertValueIndianCurrency(financialArrangementsDetailRequest.getOutstandingAmount()).toString());
		                financialArrangementsDetailResponse.setSecurityDetails(financialArrangementsDetailRequest.getSecurityDetails());
//		                financialArrangementsDetailResponse.setAmount(financialArrangementsDetailRequest.getAmount() != null ? CommonUtils.convertValueIndianCurrency(financialArrangementsDetailRequest.getAmount()).toString() : "-");
		                //financialArrangementsDetailResponse.setLenderType(LenderType.getById(financialArrangementsDetailRequest.getLenderType()).getValue());
		                financialArrangementsDetailResponse.setLoanDate(financialArrangementsDetailRequest.getLoanDate());
		                financialArrangementsDetailResponse.setLoanType(financialArrangementsDetailRequest.getLoanType());
		                financialArrangementsDetailResponse.setFinancialInstitutionName(StringEscapeUtils.escapeXml(financialArrangementsDetailRequest.getFinancialInstitutionName()));
		                //financialArrangementsDetailResponse.setFinancialInstitutionName(financialArrangementsDetailRequest.getFinancialInstitutionName());
		                //financialArrangementsDetailResponse.setFacilityNature(NatureFacility.getById(financialArrangementsDetailRequest.getFacilityNatureId()).getValue());
		                //financialArrangementsDetailResponse.setAddress(financialArrangementsDetailRequest.getAddress());
		                financialArrangementsDetailResponse.setLcbgStatus(!CommonUtils.isObjectNullOrEmpty(financialArrangementsDetailRequest.getLcBgStatus()) ? LCBG_Status_SBI.getById(financialArrangementsDetailRequest.getLcBgStatus()).getValue().toString() : "-");
		                financialArrangementsDetailResponse.setEmi(financialArrangementsDetailRequest.getEmi() != null ? CommonUtils.convertValueIndianCurrency(financialArrangementsDetailRequest.getEmi()).toString() : "-");
						financialArrangementsDetailResponse.setBuerauOutStandingStr(financialArrangementsDetailRequest.getBureauOutstandingAmount() != null ? CommonUtils.convertValueIndianCurrency(financialArrangementsDetailRequest.getBureauOutstandingAmount()).toString() : "-");
						financialArrangementsDetailResponse.setCollateralAmtStr(financialArrangementsDetailRequest.getCollateralSecurityAmount() != null ? CommonUtils.convertValueIndianCurrency(financialArrangementsDetailRequest.getCollateralSecurityAmount()).toString() : "-");
		                financialArrangementsDetailResponseList.add(financialArrangementsDetailResponse);
		             }
		            
		             map.put("financialArrangments",!CommonUtils.isListNullOrEmpty(financialArrangementsDetailResponseList) ? financialArrangementsDetailResponseList : " ");
				} catch (Exception e) {
		             logger.error("Problem to get Data of Financial Arrangements Details {}", e);
		        }
					
				/*get loan obligation of dir*/
				Double loanObligation=financialArrangementDetailsService.getTotalEmiOfAllDirByApplicationId(applicationId);
				map.put("loanObligation", loanObligation != null ? CommonUtils.CurrencyFormat(loanObligation.toString()) : 0);

				try {
					PrimaryCorporateRequest primaryCorporateRequest = primaryCorporateService.get(applicationId, userId);
					map.put("loanAmt", applicationProposalMapping != null && applicationProposalMapping.getLoanAmount() != null ? CommonUtils.convertValueIndianCurrency(applicationProposalMapping.getLoanAmount()) : "0");
					
					map.put("enhancementAmount", !CommonUtils.isObjectNullOrEmpty(primaryCorporateRequest.getEnhancementAmount()) ? CommonUtils.convertValueIndianCurrency(primaryCorporateRequest.getEnhancementAmount()) : " ");
					//map.put("loanType", !CommonUtils.isObjectNullOrEmpty(primaryCorporateRequest.getProductId()) ? CommonUtils.LoanType.getType(primaryCorporateRequest.getProductId()).getName() : " ");
					map.put("promotorsContribution", CommonUtils.convertValueIndianCurrency(primaryCorporateRequest.getPromoterContribution()));
					map.put("totalAmtPer", !CommonUtils.isObjectNullOrEmpty(primaryCorporateRequest.getTotalAmtPercentage()) ? " ("+CommonUtils.convertValue(primaryCorporateRequest.getTotalAmtPercentage())+"%)" : null);
					if(!CommonUtils.isObjectNullOrEmpty(primaryCorporateRequest.getPurposeOfLoanId())) {
						map.put("purpose", StringEscapeUtils.escapeXml(PurposeOfLoan.getById(primaryCorporateRequest.getPurposeOfLoanId()).getValue()));
					}else {
						map.put("purpose", "");
					}

					if(!CommonUtils.isObjectNullOrEmpty(primaryCorporateRequest.getHaveCollateralSecurity()) && primaryCorporateRequest.getHaveCollateralSecurity()) {
						map.put("collateralSecurityList", collateralSecurityDetailService.getData(applicationId));
						map.put("amtOfSecurity",!CommonUtils.isObjectNullOrEmpty(primaryCorporateRequest.getCollateralSecurityAmount()) ? CommonUtils.convertValueIndianCurrency(primaryCorporateRequest.getCollateralSecurityAmount()) : " ");
					}
				}catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				
				//NAME AS PER ITR
				try{
					ITRConnectionResponse itrResponse = itrClient.getITRBasicDetails(itrMasterId);
					logger.info("ITR RESPONSE===========>{}",itrResponse);
					map.put("nameAsPerItr", itrResponse != null && itrResponse.getData() != null ? CommonUtils.printFields(itrResponse.getData(),null) : null);
				}catch(Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			    			   
				
				
				if (proposalId != null) {
					CorporateFinalInfoRequest corporateFinalInfoRequest;
					try {
						//corporateFinalInfoRequest = corporateFinalInfoService.get(userId, applicationId);  PREVIOIS
						corporateFinalInfoRequest = corporateFinalInfoService.getByProposalId(userId, proposalId);//NEW

						if(!CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest)) {
							//ADMIN OFFICE ADDRESS
							if(!CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getSecondAddress())){
								map.put("adminAddPremise", !CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getSecondAddress().getPremiseNumber()) ? CommonUtils.printFields(corporateFinalInfoRequest.getSecondAddress().getPremiseNumber(),null) + "," : "");
								map.put("adminAddStreetName", !CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getSecondAddress().getStreetName()) ? CommonUtils.printFields(corporateFinalInfoRequest.getSecondAddress().getStreetName(),null) + " " : "");
								map.put("adminAddLandmark", !CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getSecondAddress().getLandMark()) ? CommonUtils.printFields(corporateFinalInfoRequest.getSecondAddress().getLandMark(),null) + " " : "");
								map.put("adminAddCountry", StringEscapeUtils.escapeXml(getCountryName(corporateFinalInfoRequest.getSecondAddress().getCountryId())));
								map.put("adminAddState", StringEscapeUtils.escapeXml(getStateName(corporateFinalInfoRequest.getSecondAddress().getStateId())));
								map.put("adminAddCity", StringEscapeUtils.escapeXml(getCityName(corporateFinalInfoRequest.getSecondAddress().getCityId())));
								map.put("adminAddPincode", !CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getSecondAddress().getPincode())?corporateFinalInfoRequest.getSecondAddress().getPincode() : "");			
								try {
									if(!CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getSecondAddress().getDistrictMappingId())) {
										map.put("adminAddressData",CommonUtils.printFields(pincodeDateService.getById(corporateFinalInfoRequest.getSecondAddress().getDistrictMappingId()),null));				
									}
								} catch (Exception e) {
									logger.error(CommonUtils.EXCEPTION,e);
								}
							}
							
							//REGISTERED OFFICE ADDRESS
							if(!CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFirstAddress())) {
								map.put("registeredAddPremise", !CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFirstAddress().getPremiseNumber()) ? CommonUtils.printFields(corporateFinalInfoRequest.getFirstAddress().getPremiseNumber(),null) + "," : "");
								map.put("registeredAddStreetName", !CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFirstAddress().getStreetName()) ? CommonUtils.printFields(corporateFinalInfoRequest.getFirstAddress().getStreetName(),null) + " " : "");
								map.put("registeredAddLandmark", !CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFirstAddress().getLandMark()) ? CommonUtils.printFields(corporateFinalInfoRequest.getFirstAddress().getLandMark(),null) + " " : "");
								map.put("registeredAddCountry", StringEscapeUtils.escapeXml(getCountryName(corporateFinalInfoRequest.getFirstAddress().getCountryId())));
								map.put("registeredAddState", StringEscapeUtils.escapeXml(getStateName(corporateFinalInfoRequest.getFirstAddress().getStateId())));
								map.put("registeredAddCity", StringEscapeUtils.escapeXml(getCityName(corporateFinalInfoRequest.getFirstAddress().getCityId())));
								map.put("registeredAddPincode", !CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFirstAddress().getPincode())?corporateFinalInfoRequest.getFirstAddress().getPincode() : "");
								try {
									if(!CommonUtils.isObjectNullOrEmpty(corporateFinalInfoRequest.getFirstAddress().getDistrictMappingId())) {
										map.put("registeredAddressData",CommonUtils.printFields(pincodeDateService.getById(corporateFinalInfoRequest.getFirstAddress().getDistrictMappingId()),null));				
									}
								} catch (Exception e) {
									logger.error(CommonUtils.EXCEPTION,e);
								}
							}
							map.put("corporateApplicantFinal",corporateFinalInfoRequest);
							map.put("aboutUs", StringEscapeUtils.escapeXml(corporateFinalInfoRequest.getAboutUs()));
						}
					} catch (Exception e1) {
						logger.error(CommonUtils.EXCEPTION,e1);
					}
				}
				
				map.put("applicationType", (loanApplicationMaster.getWcRenewalStatus() != null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue() : "New" ));
				//MCA DATA
				try {
					// CURRENTLY PENDING DATA-ID NOT EXISTS IN application_proposal_mapping-->applicationProposalMapping
					String companyId = loanApplicationMaster.getMcaCompanyId();
					
					if(companyId != null) {
						McaResponse mcaStatusResponse = mcaClient.mcaStatusCheck(String.valueOf(applicationId), companyId);
						if (mcaStatusResponse!= null && mcaStatusResponse.getData()!= null && mcaStatusResponse.getData().equals(true)) {
							McaResponse mcaResponse = mcaClient.getCompanyDetailedData(companyId);
							if(!CommonUtils.isObjectNullOrEmpty(mcaResponse.getData())) {
								map.put("mcaData", CommonUtils.printFields(mcaResponse.getData(),null));
							}
							McaResponse mcaFinancialAndDetailsRes=mcaClient.getCompanyFinancialCalcAndDetails(applicationId, companyId);
							if(mcaFinancialAndDetailsRes.getData()!=null){
								map.put("financialDetailResp", mcaFinancialAndDetailsRes.getData());
							}
						}
						map.put("mcaCheckStatus",mcaStatusResponse.getData()!= null ?  mcaStatusResponse.getData() : null);
						
					}else {
						map.put("mcaNotApplicable",true);
					}
				}catch(Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				
				//SHARE PRICE
				if (applicationProposalMapping != null) {
					try {
						CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository.getByApplicationAndProposalIdAndUserId(userId, applicationId,applicationProposalMapping.getProposalId());
						if(corporateApplicantDetail != null) {
							map.put("castCategory", corporateApplicantDetail.getCastCategory() != null ? corporateApplicantDetail.getCastCategory() : "-");
							map.put("minorityCastCategory", corporateApplicantDetail.getMinorCastCategory());
							map.put("noOfWorker", corporateApplicantDetail.getEmploymentGeneration() != null ? StringEscapeUtils.escapeXml(NoOfEmployees.getById(corporateApplicantDetail.getEmploymentGeneration()).getValue()) : "-");
							map.put("nameAsperGST", corporateApplicantDetail.getOrganisationName() != null ? StringEscapeUtils.escapeXml(corporateApplicantDetail.getOrganisationName()) : "-");
						}
					}catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
							   
				return map;
		}
	
	@Override
	public Map<String , Object> getManualBankStatementData(Long applicationId , Long bsMasterId) {
		
		Map<String,Object> map = new HashMap<String, Object>();
		try {
			ManualBsReportRequest manualBsReportRequest = new ManualBsReportRequest();
	    	manualBsReportRequest.setBsMasterId(bsMasterId);
	    	AnalyzerResponse analyzerResponse = analyzerClient.getManualBankAccDetails(manualBsReportRequest);
	    	if (!CommonUtils.isObjectNullOrEmpty(analyzerResponse) && !CommonUtils.isObjectNullOrEmpty(analyzerResponse.getData())) {
	    		LinkedHashMap<String, Object> bankStatementResponse = (LinkedHashMap<String, Object>)analyzerResponse.getData();
				Map<String,Object> map1 = new HashMap<String, Object>();
				Map<String,Object> data = new HashMap<String, Object>();
				data.put("accountName", bankStatementResponse.get("accountHolderName"));
				data.put("name", bankStatementResponse.get("accountHolderName"));
				data.put("accountNumber", bankStatementResponse.get("bankAccountNo"));
				data.put("ifsc", bankStatementResponse.get("ifscCode"));
				data.put("status", 1); // show as account verified
				String year = bankStatementResponse.get("sinceYear") != null ? String.valueOf(bankStatementResponse.get("sinceYear")) : null;
				String months = bankStatementResponse.get("sinceMonth") != null ? String.valueOf(bankStatementResponse.get("sinceMonth")) : null;
				if (!CommonUtils.isObjectNullOrEmpty(year) || !CommonUtils.isObjectNullOrEmpty(months)) {
					LocalDate today = LocalDate.now();
					LocalDate since = LocalDate.of(Integer.parseInt(year), Integer.parseInt(months),1);
					Period age = Period.between(since, today);
					map.put("noBsSinceYear", age.getYears());
					map.put("noBsSinceMonths", age.getMonths());
					map.put("noBsSinceWhen", (!CommonUtils.isObjectNullOrEmpty(age.getYears()) ? age.getYears() +" year " : "") + " " +(!CommonUtils.isObjectNullOrEmpty(age.getMonths()) ? age.getMonths()+" months" :  "" ));
				}
				
				map1.put("data", data);
				map.put("noBsData", map1);
	    	}
		}catch (Exception e) {
			logger.error("Error while fetching No Bank Statement Account data From Analyzer with BsMasterId==>{} with Error:{} ",bsMasterId , e);	
		}
		
		if(map.get("noBsData") == null) {
			try {
				if(!CommonUtils.isObjectNullOrEmpty(applicationId)) {
					CommonResponse verificationrequestResponse =  pennyDropClient.getAccountDetails(applicationId);
					if(!CommonUtils.isObjectNullOrEmpty(verificationrequestResponse) && !CommonUtils.isObjectNullOrEmpty(verificationrequestResponse.getData())) {
						LinkedHashMap<String, Object> noBs = (LinkedHashMap<String, Object>) verificationrequestResponse.getData();	
						String year = (String) noBs.get("sinceYear");
						String months = (String) noBs.get("sinceMonth");
						if (!CommonUtils.isObjectNullOrEmpty(year) || !CommonUtils.isObjectNullOrEmpty(months)) {
							LocalDate today = LocalDate.now();
							LocalDate since = LocalDate.of(Integer.parseInt(year), Integer.parseInt(months),1);
							Period age = Period.between(since, today);
							map.put("noBsSinceYear", age.getYears());
							map.put("noBsSinceMonths", age.getMonths());
							map.put("noBsSinceWhen", (!CommonUtils.isObjectNullOrEmpty(age.getYears()) ? age.getYears() +" year " : "") + " " +(!CommonUtils.isObjectNullOrEmpty(age.getMonths()) ? age.getMonths()+" months" :  "" ));
						}
					}
					map.put("noBsData", verificationrequestResponse);
				}
				
			} catch (Exception e) {
				logger.error("Error while fetching No Bank Statement Account data From PennyDrop for applicationId==>{}  with Error:{} ",applicationId ,e);			
			}
		}
		return map;
	}
	
	private void convertExpVal(List<LinkedHashMap<String, Object>> dataMapList) {
		for (LinkedHashMap<String, Object> dataMap : dataMapList) {
			Map gstNotApplicatbleMap = (Map) dataMap.get("gstNotApplicable");
			Map<String, Map<String,Object>> momSalesMap = (Map<String, Map<String,Object>>) gstNotApplicatbleMap.get("momSales");
			
			for (Map<String,Object> momSalesMapValuesMap : momSalesMap.values()) {
				for (Entry<String,Object> entry : momSalesMapValuesMap.entrySet()) {
					Double value = (Double) entry.getValue();
					BigDecimal convertedVal = BigDecimal.valueOf(value).setScale(2);
					entry.setValue(convertedVal.toString());
				}
			}
		}
	}
	
	public Map<String,Object> getBankStatementDetails(Long applicationId ,Long userId,Long bsId){
		//PERFIOS API DATA (BANK STATEMENT ANALYSIS)
		ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(applicationId);
		reportRequest.setBsMasterId(bsId);
		reportRequest.setUserId(userId);

		Map<String,Object> bankStatement = new HashMap<String, Object>();  
		
		List<Data> datas = new ArrayList<>();
		//List<Object> bankStatement = new ArrayList<Object>();
		List<Object> monthlyDetails = new ArrayList<Object>();
		List<Object> top5FundReceived = new ArrayList<Object>();
		List<Object> top5FundTransfered = new ArrayList<Object>();
		List<Object> bouncedChequeList = new ArrayList<Object>();
		List<Object> customerInfo = new ArrayList<Object>();
		List<Object> summaryInfo = new ArrayList<Object>();

		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			if(analyzerResponse.getData()!=null){
			List<HashMap<String, Object>> hashMap = (List<HashMap<String, Object>>) analyzerResponse.getData();

			if (!CommonUtils.isListNullOrEmpty(hashMap)) {
				for (HashMap<String, Object> rec : hashMap) {
					Data data = MultipleJSONObjectHelper.getObjectFromMap(rec, Data.class);
					datas.add(data);

					//bankStatement.add(!CommonUtils.isObjectNullOrEmpty(data.getXns()) ? CommonUtils.printFields(data.getXns().getXn(),null) : " ");
					monthlyDetails.add(!CommonUtils.isObjectNullOrEmpty(data.getMonthlyDetailList()) ? CommonUtils.printFields(data.getMonthlyDetailList(),null) : "");
					top5FundReceived.add(!CommonUtils.isObjectNullOrEmpty(data.getTop5FundReceivedList().getItem()) ? CommonUtils.printFields(data.getTop5FundReceivedList().getItem(),null) : "");
					top5FundTransfered.add(!CommonUtils.isObjectNullOrEmpty(data.getTop5FundTransferedList().getItem()) ? CommonUtils.printFields(data.getTop5FundTransferedList().getItem(),null) : "");
					bouncedChequeList.add(!CommonUtils.isObjectNullOrEmpty(data.getBouncedOrPenalXnList()) ? CommonUtils.printFields(data.getBouncedOrPenalXnList().getBouncedOrPenalXns(),null) : " ");
					customerInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getCustomerInfo()) ? CommonUtils.printFields(data.getCustomerInfo(),null) : " ");
					summaryInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getSummaryInfo()) ?CommonUtils.printFields(data.getSummaryInfo(),null) : " ");

				}

				//map.put("bankStatement", bankStatement);
				bankStatement.put("monthlyDetails", monthlyDetails);
				bankStatement.put("top5FundReceived", top5FundReceived);
				bankStatement.put("top5FundTransfered", top5FundTransfered);
				bankStatement.put("bouncedChequeList", bouncedChequeList);
				bankStatement.put("customerInfo", !CommonUtils.isObjectListNull(customerInfo) ? customerInfo : null);
				bankStatement.put("summaryInfo", summaryInfo);
				bankStatement.put("bankStatementAnalysis", datas != null && !datas.isEmpty() ? CommonUtils.printFields(datas, null) : null);
			}
			
		 }
			return bankStatement;
		} catch (Exception e) {
			logger.error("Error while getting perfios data : ",e);
			return null;
		}
		
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
	
	
	
	//For application Form
	@Override
	public Map < String, Object > getDetailsForApplicationForm(Long applicationId, Long productId, Long proposalId) {

		Object[] profileVersionDetails = loanRepository.getProfileVersionDetailsByApplicationId(applicationId);
		if(CommonUtils.isObjectNullOrEmpty(profileVersionDetails)) {
			logger.error("Profile not found for applicationId =======>"+applicationId);
			return null;
		}
		Long itrId = profileVersionDetails[0] != null ? Long.valueOf(profileVersionDetails[0].toString()) : null;
		Long gstId = profileVersionDetails[1] != null ? Long.valueOf(profileVersionDetails[1].toString()) : null;
		Long bsId =  profileVersionDetails[2] != null ? Long.valueOf(profileVersionDetails[2].toString()) : null;
	    Map < String, Object > map = new HashMap < String, Object > ();
	    DecimalFormat decim = new DecimalFormat("####");
	    //CGTMSE DATA
	
	    
	    Long userId = null;
	    if (proposalId != null) {
	    	ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.getByApplicationIdAndProposalId(applicationId, proposalId);
		    userId = applicationProposalMapping.getUserId();
		} else {
			userId = loanApplicationRepository.getUserIdByApplicationId(applicationId);
		}

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

			map.put("vehicleOperateIn", vehicleOperatorRequest.getVehicleOperateIn().toString().replaceAll("[\\[\\]]", ""));

			map.put("vehicleOperatorDetails", vehicleOperatorRequest);
		}

		// Vehicle Operator Code ends here

	    //GST COMMON DATA
	    map.put("gstDetailedResp",getGstDetails(applicationId, userId,gstId));
	        
	    CorporateApplicantRequest corporateApplicantRequest = corporateApplicantService.getCorporateApplicant(applicationId);
	    map.put("panNo", corporateApplicantRequest != null && !CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getPanNo()) ? corporateApplicantRequest.getPanNo() : " ");
	    
	    map.put("bankDetails", getBranchDetails(applicationId, userId, proposalId));
		Long orgId = null;
		if(proposalId != null) {
			orgId = proposalDetailsRepository.getOrgIdByProposalId(proposalId);
			// Product Name
			if(productId != null) {
				String productName = productMasterRepository.getFpProductName(productId);
				if(productName != null) {
					try {
						map.put("fpProductName",CommonUtils.printFields(productName, null));
					} catch (Exception e) {
						logger.info("error"+e);
					}
				}else {
					logger.info("product name is null..of productId==>{}", productId);
				}
			}else {
				logger.info("fpProductMapping id is null..");
			}
		}else {
			orgId = ineligibleProposalDetailsRepository.getOrgId(applicationId);
		}
		
		String[] str = BanksEnumForReports.getBankNameAndUrl(orgId);
		
		map.put("bankName", str != null && str.length > 0 && str[0] != null ? str[0] : " ");
		map.put("bankUrl", str != null && str.length > 1 && str[1] != null ? str[1] : null);
		map.put("bankFullName", str != null && str.length > 2 && str[2] != null ? str[2] : " ");
		
		map.putAll(getOneFormData(applicationId,proposalId,userId,itrId));
		
		//matches common
		map.putAll(getMatchesAndEligiblityDetails(applicationId,productId,proposalId));
		
		//bank Statement data
		map.putAll(getBankStatementDetails(applicationId, userId,bsId));
		
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
		try {
			map.put("associateConcern", CommonUtils.printFields(associatedConcernResList, null));
		} catch (Exception e) {
			logger.error("Error while Getting Associate Concern Details = >{}",e);
		}
		
		//PROPOSAL RESPONSE
		try {
			ObjectMapper mapper = new ObjectMapper();
			ProposalMappingRequestString proposalMappingRequestString = new ProposalMappingRequestString();
			ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
			proposalMappingRequest.setApplicationId(applicationId);
			if(productId != null) {
				proposalMappingRequest.setFpProductId(productId);
			}
			ProposalMappingResponse proposalMappingResponse= proposalDetailsClient.getActiveProposalDetails(proposalMappingRequest);
			proposalMappingRequestString = mapper.convertValue(proposalMappingResponse.getData(), ProposalMappingRequestString.class);
			logger.info("============proposalMappingRequestId==>{}",proposalMappingRequestString.getId());
			map.put("proposalResponse", !CommonUtils.isObjectNullOrEmpty(proposalMappingRequestString) ? CommonUtils.printFields(proposalMappingResponse.getData() ,null) : " ");
				
		}
		catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		String establishMentYear = !CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getEstablishmentMonth())? EstablishmentMonths.getById(corporateApplicantRequest.getEstablishmentMonth()).getValue(): "";
		if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getEstablishmentYear())) {
			try {
				OneFormResponse establishmentYearResponse = oneFormClient.getYearByYearId(CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest.getEstablishmentYear()) ? null: corporateApplicantRequest.getEstablishmentYear().longValue());
				List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) establishmentYearResponse.getListData();
				if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
					MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
					establishMentYear += " " + masterResponse.getValue();
				}
			} catch (Exception e) {
				logger.error("Error in getting establishment year : ", e);
			}
		}
		try {
			map.put("establishmentYr",!CommonUtils.isObjectNullOrEmpty(establishMentYear)? CommonUtils.printFields(establishMentYear, null): " ");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error while Getting Year Details from oneform = >{}",e);
		}
		
		//get NO BS+ DATA
		map.putAll(getManualBankStatementData(applicationId , bsId));
		
//		CommonResponse verificationrequestResponse = null;
//		try {
//			if(!CommonUtils.isObjectNullOrEmpty(applicationId)) {
//				verificationrequestResponse =  pennyDropClient.getAccountDetails(applicationId);
//				if(!CommonUtils.isObjectNullOrEmpty(verificationrequestResponse) && !CommonUtils.isObjectNullOrEmpty(verificationrequestResponse.getData())) {
//					LinkedHashMap<String, Object> noBs = (LinkedHashMap<String, Object>) verificationrequestResponse.getData();	
//					String year = (String) noBs.get("sinceYear");
//					String months = (String) noBs.get("sinceMonth");
//					if (!CommonUtils.isObjectNullOrEmpty(year) || !CommonUtils.isObjectNullOrEmpty(months)) {
//						LocalDate today = LocalDate.now();
//						LocalDate since = LocalDate.of(Integer.parseInt(year), Integer.parseInt(months),1);
//						Period age = Period.between(since, today);
//						map.put("noBsSinceYear", age.getYears());
//						map.put("noBsSinceMonths", age.getMonths());
//						map.put("noBsSinceWhen", (!CommonUtils.isObjectNullOrEmpty(age.getYears()) ? age.getYears() +" year " : "") + " " +(!CommonUtils.isObjectNullOrEmpty(age.getMonths()) ? age.getMonths()+" months" :  "" ));
//					}
//				}
//				map.put("noBsData", verificationrequestResponse);
//			}
//			
//		} catch (Exception e) {
//			logger.error("Error while fetching Account data : ",e);			
//		}
		
		//ITR (CHECK IF UPLOADED USING XML OR ONLINE)
		try {
			ITRConnectionResponse itrConnectionResponse= itrClient.getIsUploadAndYearDetails(itrId);
			if(!CommonUtils.isObjectNullOrEmpty(itrConnectionResponse)) {
				map.put("checkItr", itrConnectionResponse.getData());
			}
		}catch(Exception e) {
			logger.error("Error while getting ITR data : ",e);
		}
		
		//FOR NO-ITR DATA (MUDRA - CAM)
		CorporateApplicantRequest response = applicantService.getCorporateApplicantDetails(applicationId);
		DateFormat dfYear = new SimpleDateFormat("yyyy");
		Date dateNoItr = new Date();
		dateNoItr = response.getDob();
		if (!CommonUtils.isObjectNullOrEmpty(dateNoItr)) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(response.getDob().getTime());
			map.put("noItrYear", dfYear.format(dateNoItr));
			String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
			map.put("noItrMonth",month);
		}else {
			map.put("noItrYear", "-");
			map.put("noItrMonth","-");			
		}
		
		// MUDRA LOAN DETAILS
		try {
			PrimaryCorporateDetailMudraLoan mlDetail = 	mudraLoanRepo.findFirstByApplicationIdAndApplicationProposalMappingProposalIdOrderByIdDesc(applicationId, proposalId); 
			if (!CommonUtils.isObjectNullOrEmpty(mlDetail)) {			
				PrimaryCorporateDetailMudraLoanReqRes mlDetailsRes = new PrimaryCorporateDetailMudraLoanReqRes();
				BeanUtils.copyProperties(mlDetail, mlDetailsRes);
				if (!CommonUtils.isObjectNullOrEmpty(mlDetail.getMrktArragementFinishedGoods())) {					
					mlDetailsRes.setMrktArragementFinishedGoodsValue(MrktArrFinishedGoodsList.fromId(mlDetail.getMrktArragementFinishedGoods()).getValue());
				}
				//corporatePrimaryViewResponse.setMlDetail(mlDetailsRes);
				map.put("mlDetail", !CommonUtils.isObjectNullOrEmpty(mlDetailsRes) ? CommonUtils.printFields(mlDetailsRes , null) : Collections.EMPTY_LIST);
			}
		}
		catch (Exception e) {
			logger.error("Error/Exception while fetching mlDetails Error==>{}",e);
		}
		
		// GET MACHINE DETAILS
		List<MachineDetailMudraLoan> machineDetails = machineDetailsRepo.findByApplicationIdAndIsActive(applicationId, true);
		PrimaryCorporateDetailMudraLoanReqRes mlDetailsRes = new PrimaryCorporateDetailMudraLoanReqRes();
		if (!CommonUtils.isListNullOrEmpty(machineDetails)) {
			List<MachineDetailMudraLoanRequestResponse> machineDetailsRes =  new ArrayList<>(machineDetails.size());
			for (MachineDetailMudraLoan machineDetailMudraLoan : machineDetails) {
				MachineDetailMudraLoanRequestResponse machineDetail = new MachineDetailMudraLoanRequestResponse(); 
				BeanUtils.copyProperties(machineDetailMudraLoan, machineDetail);
				if(machineDetailMudraLoan != null && machineDetailMudraLoan.getCostOfMachinery() != null) {
					BigDecimal convertedVal = BigDecimal.valueOf(machineDetailMudraLoan.getCostOfMachinery()).setScale(2);
					machineDetail.setCostOfMachineryString(convertedVal.toString());
				}
				machineDetailsRes.add(machineDetail);
			}				
			mlDetailsRes.setMachineDetails(machineDetailsRes);
		}
		map.put("machineDetailsMudra", mlDetailsRes);
		
		
		//Get BankStatement
		try {
			map.putAll(getBankStatementDetails(applicationId, userId,bsId));
		}catch (Exception e) {
			logger.error("Error in getting Bank Statemnt from Perfios in MSME Cam Report with applicationId==>{}",applicationId);
		}
		
		try {
			PrimaryCorporateDetailMudraLoanReqRes primaryCorporateDetailMudraLoanReqRes = new PrimaryCorporateDetailMudraLoanReqRes(); 
			PrimaryCorporateDetailMudraLoan primaryCorporateDetailMudraLoan = primaryCorporateDetailsMudra.findFirstByApplicationIdAndApplicationProposalMappingProposalIdOrderByIdDesc(applicationId, proposalId);
			if(!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetailMudraLoan)) {
				BeanUtils.copyProperties(primaryCorporateDetailMudraLoan, primaryCorporateDetailMudraLoanReqRes);
				if (primaryCorporateDetailMudraLoanReqRes != null) {
					map.put("statutoryObligation", CommonUtils.printFields(primaryCorporateDetailMudraLoanReqRes,null));
				}
			}
		}catch (Exception e) {
			logger.error("Error/Exception statutoryObligation{}",e);
		}
	    return map;
	}
	
	@Override
	public String getCamVersionForBSStandalone(String type) {
		try {
		return commonRepository.getCamVersionForBSStandalone(type);
		}
		catch (Exception e) {
			logger.error("Error :{}",e);
			return type;
		}
	}
	private static final String PRODUCT_DOCUMENT_MAPPING_ID = "productDocumentMappingId";
	private static final String USER_TYPE = "userType";
	private static final String ORIGINAL_FILE_NAME = "originalFileName";
	private static final String ERROR_WHILE_GETTING_MAP_DETAILS = "Error while getting MAP Details==>";
	
	@SuppressWarnings("unchecked")
	@Override
	public byte[] getCamReportPrimaryDetailsByteArray(Long applicationId, Long productId, Long proposalId,boolean isFinalView,Integer loanTypeId ) {
		
		if(loanTypeId == null) {
			loanTypeId = 1;
		}
		Map<String, Object> response = new HashMap<String, Object>();		
		try {
			com.opl.mudra.api.reports.ReportRequest reportRequest = null;
			
			if(loanTypeId == LoanType.WORKING_CAPITAL.getValue() || loanTypeId == LoanType.TERM_LOAN.getValue() || loanTypeId == LoanType.WCTL_LOAN.getValue()) {
				logger.info("Fetching Cam Data of Mudra Loan by ApplicationId==>{} ProductMappingId==>{} ProposalId==>{}" ,applicationId ,productId, proposalId);
				response = getCamReportPrimaryDetails(applicationId,productId,proposalId,false);
				reportRequest = new com.opl.mudra.api.reports.ReportRequest();
				reportRequest.setParams(response);
				reportRequest.setTemplate("MUDRALOANPRIMARYCAM");
				reportRequest.setType("MUDRALOANPRIMARYCAM");
			}
			
			if(reportRequest != null && !response.isEmpty()) {
				byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
				if (byteArr != null && byteArr.length > 0) {
					try {
						if(loanTypeId == LoanType.WORKING_CAPITAL.getValue() || loanTypeId == LoanType.TERM_LOAN.getValue() || loanTypeId == LoanType.WCTL_LOAN.getValue()) {
							MultipartFile multipartFile = new DDRMultipart(byteArr);			  
							JSONObject jsonObj = new JSONObject();
							jsonObj.put(CommonUtils.APPLICATION_ID, applicationId);
							jsonObj.put(PRODUCT_DOCUMENT_MAPPING_ID, 355L);
							jsonObj.put(USER_TYPE, CommonUtils.UploadUserType.UERT_TYPE_APPLICANT);
							jsonObj.put(ORIGINAL_FILE_NAME, "MUDRALOANPRIMARYCAM"+proposalId +".pdf");
							
							dmsClient.uploadFile(jsonObj.toString(), multipartFile);
							logger.info("InPrinciple CAM Uploaded for ==>"+applicationId);
						}
					} catch (Exception e) {
						logger.info("Error while Generated InPrinciple Cam upload to dms for==>{} with Error...{}",applicationId , e);
					}
					
					return byteArr;
				}
			}else {
				logger.error("Error/Excpetion while fetching Cam Report with ApplicationId==>{} ,ProductId==>{} and ProposalId==>{} with LoanTypeId==>{}" , applicationId ,productId ,proposalId ,loanTypeId);
			}
		}catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
		}
		
		return null;
	}

	@Override
	public byte[] getApplicationForm(Long applicationId, Long productId, Long proposalId,Integer loanTypeId) {
		
		if(loanTypeId == null) {
			loanTypeId = 1;
		}
		
		Map<String, Object> response = new HashMap<String, Object>();		
		try {
			com.opl.mudra.api.reports.ReportRequest reportRequest = null;
			
			if(loanTypeId == LoanType.WORKING_CAPITAL.getValue() || loanTypeId == LoanType.TERM_LOAN.getValue() || loanTypeId == LoanType.WCTL_LOAN.getValue()) {
				logger.info("Fetching Data of Mudra Loan by ApplicationId==>{} ProductMappingId==>{} ProposalId==>{}" ,applicationId ,productId, proposalId);
				response = getDetailsForApplicationForm(applicationId, productId, proposalId);
				reportRequest = new com.opl.mudra.api.reports.ReportRequest();
				reportRequest.setParams(response);
				reportRequest.setTemplate("MUDRALOANAPPLICATIONFORM");
				reportRequest.setType("MUDRALOANAPPLICATIONFORM");
			}
			
			if(reportRequest != null && !response.isEmpty()) {
				byte[] byteArr = reportsClient.generatePDFFile(reportRequest);
				if (byteArr != null && byteArr.length > 0) {
					return byteArr;
				}
			}else {
				logger.error("Error/Excpetion while fetching data for Application Form for ApplicationId==>{} ,ProductId==>{} and ProposalId==>{} with LoanTypeId==>{}" , applicationId ,productId ,proposalId ,loanTypeId);
			}
		}catch (Exception e) {
			logger.error(ERROR_WHILE_GETTING_MAP_DETAILS, e);
		}
		
		return null;
	}
	
	
	
	
	
}
