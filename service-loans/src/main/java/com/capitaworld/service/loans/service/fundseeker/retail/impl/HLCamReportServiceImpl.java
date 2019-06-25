package com.capitaworld.service.loans.service.fundseeker.retail.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.capitaworld.service.oneform.enums.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.api.ekyc.model.EkycResponse;
import com.capitaworld.api.ekyc.model.epf.request.EmployerRequest;
import com.capitaworld.api.eligibility.model.EligibililityRequest;
import com.capitaworld.api.eligibility.model.EligibilityResponse;
import com.capitaworld.client.ekyc.EPFClient;
import com.capitaworld.client.eligibility.EligibilityClient;
import com.capitaworld.connect.api.ConnectStage;
import com.capitaworld.service.analyzer.client.AnalyzerClient;
import com.capitaworld.service.analyzer.model.common.AnalyzerResponse;
import com.capitaworld.service.analyzer.model.common.Data;
import com.capitaworld.service.analyzer.model.common.ReportRequest;
import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.BankingRelation;
import com.capitaworld.service.loans.domain.fundseeker.retail.CoApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.FinalHomeLoanCoApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.FinalHomeLoanDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.OtherPropertyDetails;
import com.capitaworld.service.loans.domain.fundseeker.retail.PurchasePropertyDetails;
import com.capitaworld.service.loans.domain.fundseeker.retail.ReferencesRetailDetail;
import com.capitaworld.service.loans.model.Address;
import com.capitaworld.service.loans.model.FinancialArrangementDetailResponseString;
import com.capitaworld.service.loans.model.FinancialArrangementsDetailRequest;
import com.capitaworld.service.loans.model.corporate.CorporateApplicantRequest;
import com.capitaworld.service.loans.model.retail.BankAccountHeldDetailsRequest;
import com.capitaworld.service.loans.model.retail.BankRelationshipRequest;
import com.capitaworld.service.loans.model.retail.CoApplicantRequest;
import com.capitaworld.service.loans.model.retail.EmpAgriculturistTypeRequest;
import com.capitaworld.service.loans.model.retail.EmpSalariedTypeRequest;
import com.capitaworld.service.loans.model.retail.EmpSelfEmployedTypeRequest;
import com.capitaworld.service.loans.model.retail.FixedDepositsDetailsRequest;
import com.capitaworld.service.loans.model.retail.HLOneformPrimaryRes;
import com.capitaworld.service.loans.model.retail.OtherCurrentAssetDetailRequest;
import com.capitaworld.service.loans.model.retail.OtherIncomeDetailRequest;
import com.capitaworld.service.loans.model.retail.PLRetailApplicantRequest;
import com.capitaworld.service.loans.model.retail.RetailApplicantIncomeRequest;
import com.capitaworld.service.loans.repository.common.CommonRepository;
import com.capitaworld.service.loans.repository.fundprovider.ProductMasterRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.BankingRelationlRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.FinalHomeLoanCoAppDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.FinalHomeLoanDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.OtherPropertyDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.PurchasePropertyDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.ReferenceRetailDetailsRepository;
import com.capitaworld.service.loans.service.common.PincodeDateService;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.impl.CamReportPdfDetailsServiceImpl;
import com.capitaworld.service.loans.service.fundseeker.retail.BankAccountHeldDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.EmpFinancialDetailsService;
import com.capitaworld.service.loans.service.fundseeker.retail.FixedDepositsDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.HLCamReportService;
import com.capitaworld.service.loans.service.fundseeker.retail.OtherCurrentAssetDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.OtherIncomeDetailService;
import com.capitaworld.service.loans.service.fundseeker.retail.PlRetailApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.PrimaryHomeLoanService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantIncomeService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.matchengine.MatchEngineClient;
import com.capitaworld.service.matchengine.ProposalDetailsClient;
import com.capitaworld.service.matchengine.model.MatchDisplayResponse;
import com.capitaworld.service.matchengine.model.MatchRequest;
import com.capitaworld.service.matchengine.model.ProposalMappingRequest;
import com.capitaworld.service.matchengine.model.ProposalMappingRequestString;
import com.capitaworld.service.matchengine.model.ProposalMappingResponse;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;
import com.capitaworld.service.oneform.model.SectorIndustryModel;
import com.capitaworld.service.scoring.ScoringClient;
import com.capitaworld.service.scoring.model.ProposalScoreDetailResponse;
import com.capitaworld.service.scoring.model.ProposalScoreResponse;
import com.capitaworld.service.scoring.model.ScoringRequest;
import com.capitaworld.service.scoring.model.ScoringResponse;
import com.capitaworld.service.scoring.utils.ScoreParameter.Retail;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class HLCamReportServiceImpl implements HLCamReportService{

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;
	
	@Autowired
	private PlRetailApplicantService plRetailApplicantService;
	
	@Autowired
	private FinalHomeLoanDetailRepository finalHomeLoanDetailRepository;
	
	@Autowired
	private BankAccountHeldDetailService bankAccountHeldDetailService;
	
	@Autowired
	private FixedDepositsDetailService fixedDepositsDetailService;
	
	@Autowired
	private OtherPropertyDetailsRepository otherPropertyDetailsRepository;
	
	@Autowired
	private OtherCurrentAssetDetailService otherCurrentAssetDetailService;
	
	@Autowired
	private OtherIncomeDetailService otherIncomeDetailService;
	
	@Autowired
	private FinalHomeLoanCoAppDetailRepository finalHomeLoanCoAppDetailRepository;
	
	@Autowired
	private EmpFinancialDetailsService empFinancialDetailsService;
	
	@Autowired
	private ReferenceRetailDetailsRepository referenceRetailDetailsRepository;
	
	@Autowired
	private PurchasePropertyDetailsRepository purchasePropertyDetailsRepository;
	
	@Autowired
	private OneFormClient oneFormClient;
	
	@Autowired
	private ApplicationProposalMappingRepository applicationMappingRepository;
	
	@Autowired 
	private PincodeDateService pincodeDateService;
	
	@Autowired
	private MatchEngineClient matchEngineClient;
	
	@Autowired
	private ProposalDetailsClient proposalDetailsClient;
	
	@Autowired 
	private RetailApplicantIncomeService retailApplicantIncomeService;
	
	@Autowired
	private FinancialArrangementDetailsService financialArrangementDetailsService;
	
	@Autowired
	private ScoringClient scoringClient;
	
	@Autowired
	private AnalyzerClient analyzerClient;
	
	@Autowired
	private EligibilityClient eligibilityClient;
	
	@Autowired
	private CoApplicantService coApplicantService;
	
	@Autowired
	private PrimaryHomeLoanService primaryHomeLoanService;
	
	@Autowired
	private ProductMasterRepository productMasterRepository;
	
	@Autowired
	private BankingRelationlRepository bankingRelationlRepository;
	
	@Autowired
	private CorporateApplicantService corporateApplicantService;
	
	@Autowired
	private CommonRepository commonRepository;
	
	@Autowired
	private EPFClient epfClient;
	
	private static final Logger logger = LoggerFactory.getLogger(CamReportPdfDetailsServiceImpl.class);
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	@Override
	public Map<String, Object> getCamReportDetailsByProposalId(Long applicationId, Long productId, Long proposalId, boolean isFinalView) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		Long userId = loanApplicationRepository.getUserIdByApplicationId(applicationId);
		ApplicationProposalMapping applicationProposalMapping = applicationMappingRepository.getByApplicationIdAndProposalId(applicationId, proposalId);
     	LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.getByIdAndUserId(applicationId, userId);
     	
     	if(loanApplicationMaster != null) {
     		map.put("applicationType", (loanApplicationMaster.getWcRenewalStatus() != null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue() : "New" ));
     		map.put("dateOfProposal", !CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getCreatedDate())? simpleDateFormat.format(loanApplicationMaster.getCreatedDate()):"-");
     	}
     	if(applicationProposalMapping != null) {
     		map.put("applicationCode", applicationProposalMapping.getApplicationCode() != null ? applicationProposalMapping.getApplicationCode() : "-");
     		map.put("loanType", !CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getProductId()) ? CommonUtils.LoanType.getType(applicationProposalMapping.getProductId()).getName() : " ");
     	}
     	
		try {
			PLRetailApplicantRequest plRetailApplicantRequest = plRetailApplicantService.getPrimaryByProposalId(userId, applicationId, proposalId);
			map.put("salutation", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getTitleId()) ? StringEscapeUtils.escapeXml(Title.getById(plRetailApplicantRequest.getTitleId()).getValue()):"");
			if(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest)) {
				map.put("registeredAddPremise", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAddressPremiseName()) ? CommonUtils.printFields(plRetailApplicantRequest.getAddressPremiseName(),null) + "," : "");
				map.put("registeredAddStreetName", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAddressStreetName()) ? CommonUtils.printFields(plRetailApplicantRequest.getAddressStreetName(),null) + "," : "");
				map.put("registeredAddLandmark", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAddressLandmark()) ? CommonUtils.printFields(plRetailApplicantRequest.getAddressLandmark(),null) + "," : "");
				map.put("registeredAddCountry", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAddressCountry()) ? StringEscapeUtils.escapeXml(getCountryName(plRetailApplicantRequest.getAddressCountry().intValue())) : "");
				map.put("registeredAddState", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAddressState()) ? StringEscapeUtils.escapeXml(getStateName(plRetailApplicantRequest.getAddressState().intValue())) : "");
				map.put("registeredAddCity", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAddressCity()) ? StringEscapeUtils.escapeXml(getCityName(plRetailApplicantRequest.getAddressCity())) : "");
				map.put("registeredAddPincode", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAddressPincode())?plRetailApplicantRequest.getAddressPincode() : "");
				try {
					if(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAddressDistrictMappingId())) {
						map.put("registeredAddressData",CommonUtils.printFields(pincodeDateService.getById(plRetailApplicantRequest.getAddressDistrictMappingId()),null));				
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}
			
			if(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getOfficeAddress())) {
				map.put("officeAddPremise", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getOfficeAddress().getPremiseNumber()) ? CommonUtils.printFields(plRetailApplicantRequest.getOfficeAddress().getPremiseNumber(),null) + "," : "");
				map.put("officeAddStreetName", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getOfficeAddress().getStreetName()) ? CommonUtils.printFields(plRetailApplicantRequest.getOfficeAddress().getStreetName(),null) + "," : "");
				map.put("officeAddLandmark", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getOfficeAddress().getLandMark()) ? CommonUtils.printFields(plRetailApplicantRequest.getOfficeAddress().getLandMark(),null) + "," : "");
				map.put("officeAddCountry", StringEscapeUtils.escapeXml(getCountryName(plRetailApplicantRequest.getOfficeAddress().getCountryId())));
				map.put("officeAddState", StringEscapeUtils.escapeXml(getStateName(plRetailApplicantRequest.getOfficeAddress().getStateId())));
				map.put("officeAddCity", StringEscapeUtils.escapeXml(getCityName(plRetailApplicantRequest.getOfficeAddress().getCityId())));
				map.put("officeAddPincode", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getOfficeAddress().getPincode())?plRetailApplicantRequest.getOfficeAddress().getPincode() : "");
				try {
					if(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getOfficeAddress().getDistrictMappingId())) {
						map.put("officeAddressData",CommonUtils.printFields(pincodeDateService.getById(plRetailApplicantRequest.getOfficeAddress().getDistrictMappingId()),null));				
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}
			
			LocalDate today = LocalDate.now();
			if(plRetailApplicantRequest.getBirthDate() != null ) {
				LocalDate birthday = LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(plRetailApplicantRequest.getBirthDate()));
				map.put("ageOfApplicant",(today.getYear() - birthday.getYear()) + " years");
			}
			
			if(plRetailApplicantRequest.getSalaryBankYear() != null && plRetailApplicantRequest.getSalaryBankMonth() != null) {
				LocalDate since = LocalDate.of(plRetailApplicantRequest.getSalaryBankYear(), plRetailApplicantRequest.getSalaryBankMonth(), 1);
				LocalDate now = LocalDate.now();
				Period sinceWhen = Period.between(since, now);
				int years = sinceWhen.getYears();
				int months = sinceWhen.getMonths();
				plRetailApplicantRequest.setSalaryBankYear(years);
				plRetailApplicantRequest.setSalaryBankMonth(months);
			}
			
			if(plRetailApplicantRequest.getResidenceSinceYear() != null && plRetailApplicantRequest.getResidenceSinceMonth() != null) {
				LocalDate since = LocalDate.of(plRetailApplicantRequest.getResidenceSinceYear(), plRetailApplicantRequest.getResidenceSinceMonth(), 1);
				LocalDate now = LocalDate.now();
				Period sinceWhen = Period.between(since, now);
				int years = sinceWhen.getYears();
				int months = sinceWhen.getMonths();
				map.put("residenceSinceYearMonths", (!CommonUtils.isObjectNullOrEmpty(years) ? years + " years" : "")+ " " +(!CommonUtils.isObjectNullOrEmpty(months) ? months+" months":""));
			}else {
				map.put("residenceSinceYearMonths", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getResidenceSinceMonth()) ? plRetailApplicantRequest.getResidenceSinceMonth()+" months":"");
			}
			
			String operatingBusinessSince = null;
			if(plRetailApplicantRequest.getBusinessStartDate() != null ) {
				LocalDate operatingBusinessDiff = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(plRetailApplicantRequest.getBusinessStartDate()));
				operatingBusinessSince = (today.getYear() - operatingBusinessDiff.getYear()) + " years";
			}
			
			map.put("loanPurposeType" ,!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getLoanPurposeQueType() != null) ? LoanPurposeQuestion.fromId(plRetailApplicantRequest.getLoanPurposeQueType()).getValue() : "-");
			map.put("loanPurposeValue", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getLoanPurposeQueValue() != null) ? plRetailApplicantRequest.getLoanPurposeQueValue() : "-");
			
			map.put("gender", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getGenderId()) ? Gender.getById(plRetailApplicantRequest.getGenderId()).getValue(): "-");
			map.put("birthDate",!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getBirthDate())? simpleDateFormat.format(plRetailApplicantRequest.getBirthDate()):"-");
			
			/*employment type*/
			if(plRetailApplicantRequest.getEmploymentType()!= null && plRetailApplicantRequest.getEmploymentType() == 2) {
				map.put("employmentWith" , plRetailApplicantRequest.getEmploymentWith() != null ? EmploymentWithPL.getById(plRetailApplicantRequest.getEmploymentWith()).getValue() : "-");
			}else if (plRetailApplicantRequest.getEmploymentType()!= null && plRetailApplicantRequest.getEmploymentType() == 5) {
				map.put("employmentWith" , plRetailApplicantRequest.getEmploymentWith() != null ? OccupationHL.getById(plRetailApplicantRequest.getEmploymentWith()).getValue() : "-");
			}else if (plRetailApplicantRequest.getEmploymentType()!= null && plRetailApplicantRequest.getEmploymentType() == 4) {
				map.put("employmentWith" , plRetailApplicantRequest.getEmploymentWith() != null ? EmploymentWithRetail.getById(plRetailApplicantRequest.getEmploymentWith()).getValue() : "-");
			}
			
			if(plRetailApplicantRequest.getEmploymentType()!= null && plRetailApplicantRequest.getEmploymentType() == 2) {
				map.put("totalExperience", (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getTotalExperienceYear()) ? plRetailApplicantRequest.getTotalExperienceYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getTotalExperienceMonth()) ? plRetailApplicantRequest.getTotalExperienceMonth() +" months" : ""));
			}else if(plRetailApplicantRequest.getEmploymentType()!= null && plRetailApplicantRequest.getEmploymentType() == 7) {
				map.put("totalExperience", null);
			}else {
				map.put("totalExperience",!CommonUtils.isObjectNullOrEmpty(operatingBusinessSince) ? operatingBusinessSince :"-");
			}
			
			String experienceInPresentJob = (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getCurrentJobYear()) ? plRetailApplicantRequest.getCurrentJobYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getCurrentJobMonth()) ? plRetailApplicantRequest.getCurrentJobMonth() +" months" : "");
			
			map.put("nameOfEmployer",plRetailApplicantRequest.getNameOfEmployer() != null ? plRetailApplicantRequest.getNameOfEmployer() : "-");
			
			//as per OccupationNature enum id
			switch (plRetailApplicantRequest.getEmploymentType() != null ? plRetailApplicantRequest.getEmploymentType() : 0) {
			
			case 2:
				//switch as per EmploymentWithPL id
				switch (plRetailApplicantRequest.getEmploymentWith() != null ? plRetailApplicantRequest.getEmploymentWith() :0) {
				
					case 1://central gov
						map.put("nameOfEmployer",oneFormClient.getMasterTableData(plRetailApplicantRequest.getCentralGovId().longValue(), GetStringFromIdForMasterData.CENTRAL_GOV.getValue()));
						break;
					case 2://state gov
						map.put("nameOfEmployer",oneFormClient.getMasterTableData(plRetailApplicantRequest.getStateGovId().longValue(), GetStringFromIdForMasterData.STATE_GOV.getValue()));
						break;
					case 3://psu
						map.put("nameOfEmployer",oneFormClient.getMasterTableData(plRetailApplicantRequest.getPsuId().longValue(), GetStringFromIdForMasterData.PSU.getValue()));
						break;
					case 4: //company
						map.put("nameOfEmployer",plRetailApplicantRequest.getNameOfEmployer());
						break;
					case 5://educational insitute
						map.put("nameOfEmployer",oneFormClient.getMasterTableData(plRetailApplicantRequest.getEduInstId().longValue(), GetStringFromIdForMasterData.INSITUTE.getValue()));
						break;
					case 8: //bank
						map.put("nameOfEmployer",oneFormClient.getMasterTableData(plRetailApplicantRequest.getBankNameId().longValue(), GetStringFromIdForMasterData.BANK.getValue()));
						break;
					case 9: //Insurance company
						map.put("nameOfEmployer",oneFormClient.getMasterTableData(plRetailApplicantRequest.getInsuranceNameId().longValue(), GetStringFromIdForMasterData.INSURANCE_COMP.getValue()));
						break;
	
					default:
						break;
				}
				break;
				
			}
			
			map.put("employmentType", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getEmploymentType()) ? OccupationNature.getById(plRetailApplicantRequest.getEmploymentType()).getValue() : "-");
			map.put("employmentStatus", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getEmploymentStatus()) ?EmploymentStatusRetailMst.getById(plRetailApplicantRequest.getEmploymentStatus()).getValue() : "-");
			map.put("sinceSalaryWhen", (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getSalaryBankYear()) ? plRetailApplicantRequest.getSalaryBankYear() + " years" : "")+" "+(plRetailApplicantRequest.getSalaryBankMonth() != null ? plRetailApplicantRequest.getSalaryBankMonth() +" months" : ""));
			map.put("retailApplicantProfile", CommonUtils.printFields(plRetailApplicantRequest, null));
			map.put("educationQualification", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getEducationQualification()) ? EducationStatusRetailMst.getById(plRetailApplicantRequest.getEducationQualification()).getValue() : "-");
			map.put("maritalStatus", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getStatusId()) ? MaritalStatusMst.getById(plRetailApplicantRequest.getStatusId()).getValue() : "-");
			map.put("residenceType", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getResidenceType()) ? ResidenceStatusRetailMst.getById(plRetailApplicantRequest.getResidenceType()).getValue() : "-");
			map.put("spouseEmployment", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getSpouseEmployment()) ? SpouseEmploymentList.getById(plRetailApplicantRequest.getSpouseEmployment()).getValue() : "-");
			map.put("designation", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getDesignation())? DesignationList.getById(plRetailApplicantRequest.getDesignation()).getValue() : "-");
			map.put("noOfDependent", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getNoOfDependent()) ? plRetailApplicantRequest.getNoOfDependent() : "-");
			map.put("nationality", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getResidentialStatus()) ? ResidentStatusMst.getById(plRetailApplicantRequest.getResidentialStatus()).getValue() : "-");
			map.put("annualIncomeOfSpouse", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAnnualIncomeOfSpouse()) ? CommonUtils.convertValueWithoutDecimal(plRetailApplicantRequest.getAnnualIncomeOfSpouse()) : "-");
			map.put("applicantNetWorth", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getNetworth()) ? CommonUtils.convertValueWithoutDecimal(plRetailApplicantRequest.getNetworth()) : "-");
			map.put("grossMonthlyIncome", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getGrossMonthlyIncome()) ? CommonUtils.convertValueWithoutDecimal(plRetailApplicantRequest.getGrossMonthlyIncome()) : null);
			map.put("netMonthlyIncome", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getMonthlyIncome()) ? CommonUtils.convertValueWithoutDecimal(plRetailApplicantRequest.getMonthlyIncome()) : null);
			//map.put("residenceSinceYearMonths", (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getResidenceSinceYear()) ? plRetailApplicantRequest.getResidenceSinceYear() + " years" : "")+ " " +(plRetailApplicantRequest.getResidenceSinceMonth() != null ? plRetailApplicantRequest.getResidenceSinceMonth()+" months":""));
			map.put("eligibleLoanAmount", !CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getLoanAmount()) ? CommonUtils.convertValueWithoutDecimal(applicationProposalMapping.getLoanAmount()): "-");
			map.put("eligibleTenure", !CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getTenure()) ? applicationProposalMapping.getTenure().longValue():"-");
			//map.put("operatingBusinessSince", !CommonUtils.isObjectNullOrEmpty(operatingBusinessSince) ? operatingBusinessSince :"-");
			map.put("applicantCategory", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getCategory()) ? CastCategory.getById(plRetailApplicantRequest.getCategory()).getValue() : "-");
			map.put("experienceInPresentJob", !CommonUtils.isObjectNullOrEmpty(experienceInPresentJob) ? experienceInPresentJob : "-");
			//map.put("totalExperience", (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getTotalExperienceYear()) ? plRetailApplicantRequest.getTotalExperienceYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getTotalExperienceMonth()) ? plRetailApplicantRequest.getTotalExperienceMonth() +" months" : ""));
			//KEY VERTICAL FUNDING
			List<Long> keyVerticalFundingId = new ArrayList<>();
			if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalFunding()))
				keyVerticalFundingId.add(plRetailApplicantRequest.getKeyVerticalFunding());
			if (!CommonUtils.isListNullOrEmpty(keyVerticalFundingId)) {
				try {
					OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
					List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
					if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
						MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						map.put("keyVerticalFunding", StringEscapeUtils.escapeXml(masterResponse.getValue()));
					} else {
						map.put("keyVerticalFunding", "-");
					}

				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}
			//KEY VERTICAL SECTOR
			List<Long> keyVerticalSectorId = new ArrayList<>();
			if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalSector()))
				keyVerticalSectorId.add(plRetailApplicantRequest.getKeyVerticalSector());
			try {
				OneFormResponse formResponse = oneFormClient.getIndustrySecByMappingId(plRetailApplicantRequest.getKeyVerticalSector());
				SectorIndustryModel sectorIndustryModel = MultipleJSONObjectHelper.getObjectFromMap((Map) formResponse.getData(), SectorIndustryModel.class);
				OneFormResponse oneFormResponse = oneFormClient.getSectorById(Arrays.asList(sectorIndustryModel.getSectorId()));
				List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
				if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
					MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
					map.put("keyVerticalSector", StringEscapeUtils.escapeXml(masterResponse.getValue()));
				} else {
					map.put("keyVerticalSector", "-");
				}
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
			//KEY VERTICAL SUBSECTOR
			try {
				if (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getKeyVerticalSubSector())) {
					OneFormResponse oneFormResponse = oneFormClient.getSubSecNameByMappingId(plRetailApplicantRequest.getKeyVerticalSubSector());
					map.put("keyVerticalSubSector",StringEscapeUtils.escapeXml(oneFormResponse.getData().toString()));
				}
			} catch (Exception e) {
				logger.error("error while getting key vertical sub-sector : ",e);
			}
			
		} catch (Exception e) {
			logger.error("Error while getting profile Details : ",e);
		}
		
		// Product Name
		if(productId != null) {
			String productName = productMasterRepository.getFpProductName(productId);
			if(productName != null) {
				map.put("fpProductName", productName);
			}else {
				logger.info("product name is null..of productId==>{}", productId);
			}
		}else {
			logger.info("fpProductMapping id is null..");
		}

		//  CHANGES FOR DATE OF PROPOSAL IN CAM REPORTS (NEW CODE)
		try {
			Date inPrincipleDate = loanApplicationRepository.getModifiedDate(applicationId, ConnectStage.RETAIL_COMPLETE.getId());
			if(!CommonUtils.isObjectNullOrEmpty(inPrincipleDate)) {
				map.put("dateOfInPrincipalApproval",!CommonUtils.isObjectNullOrEmpty(inPrincipleDate)? simpleDateFormat.format(inPrincipleDate):"-");
			}
		} catch (Exception e2) {
			logger.error(CommonUtils.EXCEPTION,e2);
		}
		// ENDS HERE===================>

		try {
			//Fetching CoApplicantDetails
			List<CoApplicantDetail> coApplicantDetails = coApplicantService.getCoApplicantList(applicationId);
			List<Map<String , Object>> listMap = new ArrayList<Map<String,Object>>();
			for(CoApplicantDetail coApplicantDetail : coApplicantDetails) {
				CoApplicantRequest coApplicantRequest = new CoApplicantRequest();
				Map<String, Object> coApp=new HashMap<>();
				coApp.put("salutation", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getTitleId()) ? StringEscapeUtils.escapeXml(Title.getById(coApplicantDetail.getTitleId()).getValue()):"");
				copyAddressFromDomainToRequestOfCoApplicant(coApplicantDetail, coApplicantRequest);
				if(!CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getFirstAddress())) {
					coApp.put("registeredAddPremise", !CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getFirstAddress().getPremiseNumber()) ? CommonUtils.printFields(coApplicantRequest.getFirstAddress().getPremiseNumber(),null) + "," : "");
					coApp.put("registeredAddStreetName", !CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getFirstAddress().getStreetName()) ? CommonUtils.printFields(coApplicantRequest.getFirstAddress().getStreetName(),null) + "," : "");
					coApp.put("registeredAddLandmark", !CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getFirstAddress().getLandMark()) ? CommonUtils.printFields(coApplicantRequest.getFirstAddress().getLandMark(),null) + "," : "");
					coApp.put("registeredAddCountry", StringEscapeUtils.escapeXml(getCountryName(coApplicantRequest.getFirstAddress().getCountryId())));
					coApp.put("registeredAddState", StringEscapeUtils.escapeXml(getStateName(coApplicantRequest.getFirstAddress().getStateId())));
					coApp.put("registeredAddCity", StringEscapeUtils.escapeXml(getCityName(coApplicantRequest.getFirstAddress().getCityId())));
					coApp.put("registeredAddPincode", !CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getFirstAddress().getPincode())?coApplicantRequest.getFirstAddress().getPincode() : "");
					try {
						if(!CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getFirstAddress().getDistrictMappingId())) {
							coApp.put("registeredAddressData",CommonUtils.printFields(pincodeDateService.getById(coApplicantRequest.getFirstAddress().getDistrictMappingId()),null));				
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
				
				if(!CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getSecondAddress())) {
					coApp.put("officeAddPremise", !CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getSecondAddress().getPremiseNumber()) ? CommonUtils.printFields(coApplicantRequest.getSecondAddress().getPremiseNumber(),null) + "," : "");
					coApp.put("officeAddStreetName", !CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getSecondAddress().getStreetName()) ? CommonUtils.printFields(coApplicantRequest.getSecondAddress().getStreetName(),null) + "," : "");
					coApp.put("officeAddLandmark", !CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getSecondAddress().getLandMark()) ? CommonUtils.printFields(coApplicantRequest.getSecondAddress().getLandMark(),null) + "," : "");
					coApp.put("officeAddCountry", StringEscapeUtils.escapeXml(getCountryName(coApplicantRequest.getSecondAddress().getCountryId())));
					coApp.put("officeAddState", StringEscapeUtils.escapeXml(getStateName(coApplicantRequest.getSecondAddress().getStateId())));
					coApp.put("officeAddCity", StringEscapeUtils.escapeXml(getCityName(coApplicantRequest.getSecondAddress().getCityId())));
					coApp.put("officeAddPincode", !CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getSecondAddress().getPincode())?coApplicantRequest.getSecondAddress().getPincode() : "");
					try {
						if(!CommonUtils.isObjectNullOrEmpty(coApplicantRequest.getSecondAddress().getDistrictMappingId())) {
							coApp.put("officeAddressData",CommonUtils.printFields(pincodeDateService.getById(coApplicantRequest.getSecondAddress().getDistrictMappingId()),null));				
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
				
				try {
					List<BankRelationshipRequest> bankRelationshipRequests = new ArrayList<>();
		            List<BankingRelation> bankingRelations = bankingRelationlRepository.listBankRelationAppId(applicationId , coApplicantDetail.getId());
	
		            BankRelationshipRequest bankRelationshipRequest = null;
		            for(BankingRelation bankingRelation : bankingRelations) {
		            	bankRelationshipRequest = new BankRelationshipRequest();
		            	BeanUtils.copyProperties(bankingRelation, bankRelationshipRequest);
		            	if(bankingRelation.getSinceYear() !=null && bankingRelation.getSinceMonth() != null) {
		            		LocalDate since = LocalDate.of(bankingRelation.getSinceYear(), bankingRelation.getSinceMonth(), 1);
		            		LocalDate today = LocalDate.now();
		            		Period age = Period.between(since, today);
		            		int years = age.getYears();
		            		int months = age.getMonths();
		            		bankRelationshipRequest.setSinceYear(years);
		            		bankRelationshipRequest.setSinceMonth(months);
		            		bankRelationshipRequest.setSinceWhen((bankRelationshipRequest.getSinceYear() != null ? bankRelationshipRequest.getSinceYear() +" year" : "") + " " +(bankRelationshipRequest.getSinceMonth() != null ? bankRelationshipRequest.getSinceMonth()+" months" :  "" ));
		            	}
		            	bankRelationshipRequests.add(bankRelationshipRequest);
		            }

		            coApp.put("bankingRelationship", !CommonUtils.isObjectListNull(bankRelationshipRequests) ? bankRelationshipRequests : null);
				}catch (Exception e) {
					logger.error("Error/Exception while fetching data for CoApplicant Banking Relationship");
				}
				
				BeanUtils.copyProperties(coApplicantDetail, coApplicantRequest);
				LocalDate today = LocalDate.now();
				if(coApplicantDetail.getBirthDate() != null ) {
					LocalDate birthday = LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(coApplicantDetail.getBirthDate()));
					coApp.put("ageOfApplicant",(today.getYear() - birthday.getYear()) + " years");
				}
				
				String operatingBusinessSince = null;
				if(coApplicantDetail.getBusinessStartDate() != null ) {
					LocalDate operatingBusinessDiff = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(coApplicantDetail.getBusinessStartDate()));
					operatingBusinessSince = (today.getYear() - operatingBusinessDiff.getYear()) + " years";
				}
				
				if(coApplicantDetail.getResidenceSinceYear() != null && coApplicantDetail.getResidenceSinceMonth() != null) {
					LocalDate since = LocalDate.of(coApplicantDetail.getResidenceSinceYear(), coApplicantDetail.getResidenceSinceMonth(), 1);
					LocalDate now = LocalDate.now();
					Period sinceWhen = Period.between(since, now);
					Integer years = sinceWhen.getYears();
					Integer months = sinceWhen.getMonths();
					
					coApp.put("residenceSinceYearMonths", (years!= null ?  years+ " years" : "")+ " " +(months != null ? months+" months":""));
				}else {
					coApp.put("residenceSinceYearMonths", coApplicantDetail.getResidenceSinceMonth() != null ? coApplicantDetail.getResidenceSinceMonth()+" months":"");
				}

				coApp.put("gender", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getGenderId()) ? Gender.getById(coApplicantDetail.getGenderId()).getValue(): "-");
				coApp.put("birthDate",!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getBirthDate())? simpleDateFormat.format(coApplicantDetail.getBirthDate()):"-");
				coApp.put("employmentType", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getEmploymentType()) ? OccupationNature.getById(coApplicantDetail.getEmploymentType()).getValue() : "-");
				
				/*employment type*/
				if(coApplicantDetail.getEmploymentType()!= null && coApplicantDetail.getEmploymentType() == 2) {
					coApp.put("employmentWith" , coApplicantDetail.getEmploymentWith() != null ? EmploymentWithPL.getById(coApplicantDetail.getEmploymentWith()).getValue() : "-");
				}else if (coApplicantDetail.getEmploymentType()!= null && coApplicantDetail.getEmploymentType() == 5) {
					coApp.put("employmentWith" , coApplicantDetail.getEmploymentWith() != null ? OccupationHL.getById(coApplicantDetail.getEmploymentWith()).getValue() : "-");
				}else if (coApplicantDetail.getEmploymentType()!= null && coApplicantDetail.getEmploymentType() == 4) {
					coApp.put("employmentWith" , coApplicantDetail.getEmploymentWith() != null ? EmploymentWithRetail.getById(coApplicantDetail.getEmploymentWith()).getValue() : "-");
				}
				
				if(coApplicantDetail.getEmploymentType()!= null && coApplicantDetail.getEmploymentType() == 2) {
					coApp.put("totalExperience", (!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getTotalExperienceYear()) ? coApplicantDetail.getTotalExperienceYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getTotalExperienceMonth()) ? coApplicantDetail.getTotalExperienceMonth() +" months" : ""));
				}else if(coApplicantDetail.getEmploymentType()!= null && coApplicantDetail.getEmploymentType() == 7) {
					coApp.put("totalExperience", null);
				}else {
					coApp.put("totalExperience",!CommonUtils.isObjectNullOrEmpty(operatingBusinessSince) ? operatingBusinessSince :"-");
				}
				
				coApp.put("nameOfEmployer", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getNameOfEmployer()) ? coApplicantDetail.getNameOfEmployer() : "-");
				
				//as per OccupationNature enum id
				switch (coApplicantDetail.getEmploymentType() != null ? coApplicantDetail.getEmploymentType() : 0) {
					
					case 2:
						//switch as per EmploymentWithPL id
						switch (coApplicantDetail.getEmploymentWith() != null ? coApplicantDetail.getEmploymentWith() :0) {
						
							case 1://central gov
								coApp.put("nameOfEmployer" ,oneFormClient.getMasterTableData(coApplicantDetail.getCentralGovId().longValue(), GetStringFromIdForMasterData.CENTRAL_GOV.getValue()));
								break;
							case 2://state gov
								coApp.put("nameOfEmployer" ,oneFormClient.getMasterTableData(coApplicantDetail.getStateGovId().longValue(), GetStringFromIdForMasterData.STATE_GOV.getValue()));
								break;
							case 3://psu
								coApp.put("nameOfEmployer" ,oneFormClient.getMasterTableData(coApplicantDetail.getPsuId().longValue(), GetStringFromIdForMasterData.PSU.getValue()));
								break;
							case 4: //company
								coApp.put("nameOfEmployer" ,coApplicantDetail.getNameOfEmployer());
								break;
							case 5://educational insitute
								coApp.put("nameOfEmployer" ,oneFormClient.getMasterTableData(coApplicantDetail.getEduInstId().longValue(), GetStringFromIdForMasterData.INSITUTE.getValue()));
								break;
							case 8: //bank
								coApp.put("nameOfEmployer" ,oneFormClient.getMasterTableData(coApplicantDetail.getBankNameId().longValue(), GetStringFromIdForMasterData.BANK.getValue()));
								break;
							case 9: //Insurance company
								coApp.put("nameOfEmployer" ,oneFormClient.getMasterTableData(coApplicantDetail.getInsuranceNameId().longValue(), GetStringFromIdForMasterData.INSURANCE_COMP.getValue()));
								break;
		
							default:
								break;
							}
						break;
					
				}

				String experienceInPresentJob = (!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getCurrentJobYear()) ? coApplicantDetail.getCurrentJobYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getCurrentJobMonth()) ? coApplicantDetail.getCurrentJobMonth() +" months" : "");
				
				coApp.put("employmentStatus", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getEmploymentStatus()) ? EmploymentStatusRetailMst.getById(coApplicantDetail.getEmploymentStatus()).getValue() : "-");
				coApp.put("relationshipWithApp", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getRelationshipWithApplicant()) ? RelationshipTypeHL.getById(coApplicantDetail.getRelationshipWithApplicant()).getValue() : "-");
				coApp.put("maritalStatus", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getStatusId()) ? MaritalStatusMst.getById(coApplicantDetail.getStatusId()).getValue() : "-");
				coApp.put("spouseEmployment", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getSpouseEmployment()) ? SpouseEmploymentList.getById(coApplicantDetail.getSpouseEmployment()).getValue() : "-");
				coApp.put("annualIncomeOfSpouse", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getAnnualIncomeOfSpouse()) ? CommonUtils.convertValueWithoutDecimal(coApplicantDetail.getAnnualIncomeOfSpouse()) : "-");
				coApp.put("residenceType", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getResidenceType()) ? ResidenceStatusRetailMst.getById(coApplicantDetail.getResidenceType()).getValue() : "-");
				coApp.put("noOfDependent", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getNoDependent()) ? coApplicantDetail.getNoDependent() : "-");
				coApp.put("designation", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getDesignation()) ? DesignationList.getById(coApplicantDetail.getDesignation()).getValue() : "-");
				coApp.put("educationQualification", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getEducationQualification()) ? EducationStatusRetailMst.getById(coApplicantDetail.getEducationQualification()).getValue() : "-");
				coApp.put("coApplicantNetWorth", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getNetworth()) ? CommonUtils.convertValueWithoutDecimal(coApplicantDetail.getNetworth()) : null);
				coApp.put("eligibleLoanAmount", !CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getLoanAmount()) ? CommonUtils.convertValueWithoutDecimal(applicationProposalMapping.getLoanAmount()): "-");
				coApp.put("eligibleTenure", !CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getTenure()) ? applicationProposalMapping.getTenure().longValue():"-");
				coApp.put("nationality", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getNationality()) ? ResidentStatusMst.getById(coApplicantDetail.getNationality()).getValue() : "-");
				coApp.put("grossMonthlyIncome", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getGrossMonthlyIncome()) ? CommonUtils.convertValueWithoutDecimal(coApplicantDetail.getGrossMonthlyIncome()) : null);
				coApp.put("netMonthlyIncome", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getMonthlyIncome()) ? CommonUtils.convertValueWithoutDecimal(coApplicantDetail.getMonthlyIncome()) : null);
				//coApp.put("operatingBusinessSince", !CommonUtils.isObjectNullOrEmpty(operatingBusinessSince) ? operatingBusinessSince : "-");
				coApp.put("coApplicantCategory", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getCategory()) ? CastCategory.getById(coApplicantDetail.getCategory()).getValue() : "-");
				coApp.put("experienceInPresentJob", !CommonUtils.isObjectNullOrEmpty(experienceInPresentJob) ? experienceInPresentJob : "-");
				//coApp.put("totalExperience", (!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getTotalExperienceYear()) ? coApplicantDetail.getTotalExperienceYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getTotalExperienceMonth()) ? coApplicantDetail.getTotalExperienceMonth() +" months" : ""));
				coApp.put("retailCoApplicantProfile", CommonUtils.printFields(coApplicantRequest, null));
				
				//Retail Final Co-App Detail
				if(isFinalView) {
					
					Map<String, Object> coAppData=new HashMap<>();
					try {
						//final CoApp Data
						FinalHomeLoanCoApplicantDetail finalCoApplicantDetail = finalHomeLoanCoAppDetailRepository.getByApplicationAndProposalIdAndCoAppId(applicationId, proposalId, coApplicantDetail.getId());
						
						if(!CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail)) {
							coAppData.put("permanentPremise", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getPermanentPremiseNo()) ? CommonUtils.printFields(finalCoApplicantDetail.getPermanentPremiseNo(),null) + "," : "");
							coAppData.put("permanentStreetName", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getPermanentStreetName()) ? CommonUtils.printFields(finalCoApplicantDetail.getPermanentStreetName(),null) + "," : "");
							coAppData.put("permanentLandmark", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getPermanentLandmark()) ? CommonUtils.printFields(finalCoApplicantDetail.getPermanentLandmark(),null) + "," : "");
							coAppData.put("permanentCountry", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getPermanentCountry()) ? StringEscapeUtils.escapeXml(getCountryName(finalCoApplicantDetail.getPermanentCountry())) : "");
							coAppData.put("permanentState", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getPermanentState()) ? StringEscapeUtils.escapeXml(getStateName(finalCoApplicantDetail.getPermanentState())) : "");
							coAppData.put("permanentCity", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getPermanentCity()) ? StringEscapeUtils.escapeXml(getCityName(finalCoApplicantDetail.getPermanentCity().longValue())) : "");
							coAppData.put("permanentPincode", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getPermanentPinCode()) ? finalCoApplicantDetail.getPermanentPinCode() : "");
							
							coAppData.put("correspondencePremise", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getCorrespondencePremiseNo()) ? CommonUtils.printFields(finalCoApplicantDetail.getCorrespondencePremiseNo(),null) + "," : "");
							coAppData.put("correspondenceStreetName", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getCorrespondenceStreetName()) ? CommonUtils.printFields(finalCoApplicantDetail.getCorrespondenceStreetName(),null) + "," : "");
							coAppData.put("correspondenceLandmark", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getCorrespondenceLandmark()) ? CommonUtils.printFields(finalCoApplicantDetail.getCorrespondenceLandmark(),null) + "," : "");
							coAppData.put("correspondenceCountry", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getCorrespondenceCountry()) ? StringEscapeUtils.escapeXml(getCountryName(finalCoApplicantDetail.getCorrespondenceCountry())) : "");
							coAppData.put("correspondenceState", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getCorrespondenceState()) ? StringEscapeUtils.escapeXml(getStateName(finalCoApplicantDetail.getCorrespondenceState())) : "");
							coAppData.put("correspondenceCity", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getCorrespondenceCity()) ? StringEscapeUtils.escapeXml(getCityName(finalCoApplicantDetail.getCorrespondenceCity().longValue())) : "");
							coAppData.put("correspondencePincode", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getCorrespondencePinCode()) ? finalCoApplicantDetail.getCorrespondencePinCode() : "");
							
							coAppData.put("mothersMaidenName", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getMatherMaidenName()) ? finalCoApplicantDetail.getMatherMaidenName() : "-");
							coAppData.put("qualificationYear", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getYear()) ? finalCoApplicantDetail.getYear() : "-");
							coAppData.put("nameOfSpouse", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getNameOfSpouse()) ? finalCoApplicantDetail.getNameOfSpouse() : "-");
							coAppData.put("noOfChildren", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getNoOfChildren()) ? finalCoApplicantDetail.getNoOfChildren() : "-");
							coAppData.put("caste", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getCast()) ? CastCategory.getById(finalCoApplicantDetail.getCast()) : "-");
							coAppData.put("religion", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getReligion()) ? ReligionRetailMst.getById(finalCoApplicantDetail.getReligion()) : "-");
							coAppData.put("birthPlace", !CommonUtils.isObjectNullOrEmpty(finalCoApplicantDetail.getPlaceOfBirth()) ? finalCoApplicantDetail.getPlaceOfBirth() :"-");
						}
					}
					catch (Exception e) {
						logger.error("Error/Exception while fetching final home loan detail Of Co-Applicant ..Error==>{}",e);
					}
					
					//Current Bank Account Detail
					try {
						List<BankAccountHeldDetailsRequest> bankAccountHeldDetails = bankAccountHeldDetailService.getExistingLoanDetailListByProposalIdCoAppId(proposalId, coApplicantDetail.getId());
						
						if(!CommonUtils.isObjectNullOrEmpty(bankAccountHeldDetails)) {
							coAppData.put("bankAccountHeldDetails", !CommonUtils.isObjectListNull(bankAccountHeldDetails) ? bankAccountHeldDetails : null);
						}
					}catch (Exception e) {
						logger.error("Error/Exception while fetching data of Co-Applicant Current Bank Account in home loan final CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
					}
					
					//Fixed Deposit Detail
					try {
						List<FixedDepositsDetailsRequest> fixedDepositsDetails = fixedDepositsDetailService.getFixedDepositsDetailByProposalIdAndCoAppId(proposalId, coApplicantDetail.getId());
						
						if(!CommonUtils.isObjectNullOrEmpty(fixedDepositsDetails)) {
							coAppData.put("fixedDepositDetails", !CommonUtils.isObjectListNull(fixedDepositsDetails) ? fixedDepositsDetails : null);
						}
					}catch (Exception e) {
						logger.error("Error/Exception while fetching data of Co-Applicant fixed deposit details in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
					}
					
					//Investment Detail
					try {
						List<OtherCurrentAssetDetailRequest> investmentDetails = otherCurrentAssetDetailService.getOtherCurrentAssetDetailListByProposalIdAndCoAppId(proposalId, coApplicantDetail.getId());
						
						if(!CommonUtils.isObjectNullOrEmpty(investmentDetails)) {
							coAppData.put("investmentDetails", !CommonUtils.isObjectListNull(investmentDetails) ? investmentDetails : null);
						}
					}catch (Exception e) {
						logger.error("Error/Exception while fetching data of Co-Applicant investment details in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
					}
					
					//Other Income Detail
					try {
						List<OtherIncomeDetailRequest> otherIncomeDetails = otherIncomeDetailService.getOtherIncomeDetailListForCoApplicant(applicationId, proposalId, coApplicantDetail.getId());
						
						if(!CommonUtils.isObjectNullOrEmpty(otherIncomeDetails)) {
							coAppData.put("otherIncomeDetails", !CommonUtils.isObjectListNull(otherIncomeDetails) ? otherIncomeDetails : null);
						}
					}catch (Exception e) {
						logger.error("Error/Exception while fetching data of Co-Applicant other income details in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
					}
					
					if(coApplicantDetail.getEmploymentType() != null) {
						
						//Emp Salaried Type
						if(coApplicantDetail.getEmploymentType() != null && coApplicantDetail.getEmploymentType() == OccupationNature.SALARIED.getId()) {
							try {
								List<EmpSalariedTypeRequest> empSalariedDetail = empFinancialDetailsService.getSalariedEmpFinDetailListByProposalIdCoAppId(proposalId, 0 ,coApplicantDetail.getId());
								
								if(!CommonUtils.isObjectNullOrEmpty(empSalariedDetail)) {
									coAppData.put("empSalariedDetails", !CommonUtils.isObjectListNull(empSalariedDetail) ? empSalariedDetail : null);
								}
							}catch (Exception e) {
								logger.error("Error/Exception while fetching data of Co-Applicant Emp Salaried Type in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
							}
						}
						
						//Emp SelfEmployed Type
						if(coApplicantDetail.getEmploymentType() != null && (coApplicantDetail.getEmploymentType() == OccupationNature.BUSINESS.getId() || coApplicantDetail.getEmploymentType() == OccupationNature.SELF_EMPLOYED.getId() || coApplicantDetail.getEmploymentType() == OccupationNature.SELF_EMPLOYED_PROFESSIONAL.getId())) {
							try {
								List<EmpSelfEmployedTypeRequest> empSelfEmployedTypeDetail = empFinancialDetailsService.getSelfEmpFinDetailListByProposalIdAndCoAppId(proposalId, 0 ,coApplicantDetail.getId());
								
								if(!CommonUtils.isObjectNullOrEmpty(empSelfEmployedTypeDetail)) {
									coAppData.put("empSelfEmployedTypeDetails", !CommonUtils.isObjectListNull(empSelfEmployedTypeDetail) ? empSelfEmployedTypeDetail : null);
								}
							}catch (Exception e) {
								logger.error("Error/Exception while fetching data of Co-Applicant Emp Self Employed Type in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
							}
						}
						
						//Emp Agriculturist Type
						if(coApplicantDetail.getEmploymentType() != null && coApplicantDetail.getEmploymentType() == OccupationNature.AGRICULTURIST.getId()) {
							try {
								List<EmpAgriculturistTypeRequest> empAgriculturistTypeDetail = empFinancialDetailsService.getAgriculturistEmpFinDetailListByProposalIdAndCoAppId(proposalId, 0 ,coApplicantDetail.getId());
								
								if(!CommonUtils.isObjectNullOrEmpty(empAgriculturistTypeDetail)) {
									coAppData.put("agriculturistDetails", !CommonUtils.isObjectListNull(empAgriculturistTypeDetail) ? empAgriculturistTypeDetail : null);
								}
							}catch (Exception e) {
								logger.error("Error/Exception while fetching data of Co-Applicant Agriculturist in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
							}
						}
					}
					
					coApp.put("finalData" ,coAppData != null ? coAppData : null);
				}
				
				listMap.add(coApp);
				
			}
			map.put("retailCoApplicantDetails", !CommonUtils.isObjectListNull(listMap) ? CommonUtils.printFields(listMap, null) : null);
		} catch (Exception e) {
			logger.error("Error while getting profile Details : ",e);
		}
		//DATE OF IN-PRINCIPLE APPROVAL (CONNECT CLIENT) EXISTING CODE
		/*try {
			ConnectResponse connectResponse = connectClient.getByAppStageBusinessTypeId(applicationId, ConnectStage.COMPLETE.getId(), com.capitaworld.service.loans.utils.CommonUtils.BusinessType.EXISTING_BUSINESS.getId());
			if(!CommonUtils.isObjectNullOrEmpty(connectResponse)) {
				map.put("dateOfInPrincipalApproval",!CommonUtils.isObjectNullOrEmpty(connectResponse.getData())? CommonUtils.DATE_FORMAT.format(connectResponse.getData()):"-");
			}
		} catch (Exception e2) {
			logger.info("Error while getting date of in-principal approval from connect client : ",e2);
		}*/
		
		//MATCHES RESPONSE
		try {
			MatchRequest matchRequest = new MatchRequest();
			matchRequest.setApplicationId(applicationId);
			matchRequest.setProductId(productId);
			matchRequest.setBusinessTypeId(applicationProposalMapping.getBusinessTypeId());
			MatchDisplayResponse matchResponse= matchEngineClient.displayMatchesOfRetail(matchRequest);
			logger.info("matchesResponse"+matchResponse);
			map.put("matchesResponse", !CommonUtils.isListNullOrEmpty(matchResponse.getMatchDisplayObjectList()) ? CommonUtils.printFields(matchResponse.getMatchDisplayObjectList(),null) : null);
		}
		catch (Exception e) {
			logger.error("Error while getting matches data : ",e);
		}
		//PROPOSAL RESPONSE
		try {
			ObjectMapper mapper = new ObjectMapper();
			ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
			proposalMappingRequest.setApplicationId(applicationId);
			proposalMappingRequest.setFpProductId(productId);
			ProposalMappingResponse proposalMappingResponse= proposalDetailsClient.getActiveProposalDetails(proposalMappingRequest);
			
			ProposalMappingRequestString proposalMappingRequestString = mapper.convertValue(proposalMappingResponse.getData(), ProposalMappingRequestString.class);
			//BeanUtils.copyProperties(proposalMappingResponse.getData(), proposalMappingRequestString);
			
			map.put("proposalDate", simpleDateFormat.format(proposalMappingRequestString.getModifiedDate()));
			map.put("proposalResponseEmi", !CommonUtils.isObjectNullOrEmpty(proposalMappingResponse.getData()) ? CommonUtils.convertValueWithoutDecimal((Double)((LinkedHashMap<String, Object>)proposalMappingResponse.getData()).get("emi")) : "-");
			map.put("proposalResponse", !CommonUtils.isObjectNullOrEmpty(proposalMappingResponse.getData()) ? proposalMappingResponse.getData() : null);
		}
		catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		
		//PRIMARY DATA (LOAN DETAILS)
		try {
			PLRetailApplicantRequest plRetailApplicantRequest = plRetailApplicantService.getPrimaryByProposalId(userId, applicationId, proposalId);
			map.put("loanPurpose", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getLoanPurpose()) ? StringEscapeUtils.escapeXml(HomeLoanPurpose.getById(plRetailApplicantRequest.getLoanPurpose()).getValue()): "-");
			map.put("retailApplicantPrimaryDetails", plRetailApplicantRequest);
		} catch (Exception e) {
			logger.error("Error while getting primary Details : ",e);
		}
		
		//Property Details
		try {
			Map<String ,Object> propertyDetails = new HashMap<String, Object>(); 
			HLOneformPrimaryRes response = primaryHomeLoanService.getOneformPrimaryDetails(applicationId);
			if(response != null) {
				propertyDetails.put("costOfProperty", !CommonUtils.isObjectNullOrEmpty(response.getCostOfProp()) ? CommonUtils.convertValueWithoutDecimal(response.getCostOfProp()) : "-");
				propertyDetails.put("propertyValue",!CommonUtils.isObjectNullOrEmpty(response.getMarketValProp()) ? CommonUtils.convertValueWithoutDecimal(response.getMarketValProp()) : "-");
				propertyDetails.put("propertyAge", !CommonUtils.isObjectNullOrEmpty(response.getOldPropYear()) ? response.getOldPropYear() : "-");
				propertyDetails.put("propertyPremise", !CommonUtils.isObjectNullOrEmpty(response.getPropPremiseName()) ? CommonUtils.printFields(response.getPropPremiseName(),null) + "," : "");
				propertyDetails.put("propertyStreetName", !CommonUtils.isObjectNullOrEmpty(response.getPropStreetName()) ? CommonUtils.printFields(response.getPropStreetName(),null) + "," : "");
				propertyDetails.put("propertyLandmark", !CommonUtils.isObjectNullOrEmpty(response.getPropLandmark()) ? CommonUtils.printFields(response.getPropLandmark(),null) + "," : "");
				propertyDetails.put("propertyCountry", !CommonUtils.isObjectNullOrEmpty(response.getPropCountry()) ? StringEscapeUtils.escapeXml(getCountryName(response.getPropCountry().intValue())) : "");
				propertyDetails.put("propertyState", !CommonUtils.isObjectNullOrEmpty(response.getPropState()) ? StringEscapeUtils.escapeXml(getStateName(response.getPropState().intValue())) : "");
				propertyDetails.put("propertyCity", !CommonUtils.isObjectNullOrEmpty(response.getPropCity()) ? StringEscapeUtils.escapeXml(getCityName(response.getPropCity())) : "");
				propertyDetails.put("propertyPincode", !CommonUtils.isObjectNullOrEmpty(response.getPropPincode()) ? response.getPropPincode() : "");
				try {
					if(!CommonUtils.isObjectNullOrEmpty(response.getPropdistrictMappingId())) {
						propertyDetails.put("propertyAddress",CommonUtils.printFields(pincodeDateService.getById(response.getPropdistrictMappingId()),null));				
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
			}
			map.put("propertyDetails", !CommonUtils.isObjectNullOrEmpty(propertyDetails) ? propertyDetails : null);
		} catch (Exception e) {
			logger.error("Error while getting property Details : ",e);
		}

		//INCOME DETAILS - NET INCOME
		try {
			List<RetailApplicantIncomeRequest> retailApplicantIncomeDetail = retailApplicantIncomeService.getAllByProposalId(applicationId, proposalId);
			
			if(!CommonUtils.isObjectNullOrEmpty(retailApplicantIncomeDetail)) {
				map.put("incomeDetails", retailApplicantIncomeDetail);
			}
		} catch (Exception e) {
			logger.error("Error while getting income details : ",e);
		}
		
		//FINANCIAL ARRANGEMENTS
		try {
            List<FinancialArrangementsDetailRequest> financialArrangementsDetailRequestList = financialArrangementDetailsService.getFinancialArrangementDetailsList(applicationId, userId);
            List<FinancialArrangementDetailResponseString> financialArrangementsDetailResponseList = new ArrayList<>();
            for (FinancialArrangementsDetailRequest financialArrangementsDetailRequest : financialArrangementsDetailRequestList) {
            	FinancialArrangementDetailResponseString financialArrangementsDetailResponse = new FinancialArrangementDetailResponseString();
                financialArrangementsDetailResponse.setOutstandingAmount(CommonUtils.convertValueWithoutDecimal(financialArrangementsDetailRequest.getOutstandingAmount()));
                financialArrangementsDetailResponse.setSecurityDetails(financialArrangementsDetailRequest.getSecurityDetails());
                financialArrangementsDetailResponse.setAmount(CommonUtils.convertValueWithoutDecimal(financialArrangementsDetailRequest.getAmount()));
                financialArrangementsDetailResponse.setLoanDate(financialArrangementsDetailRequest.getLoanDate());
                financialArrangementsDetailResponse.setLoanType(financialArrangementsDetailRequest.getLoanType());
                financialArrangementsDetailResponse.setFinancialInstitutionName(financialArrangementsDetailRequest.getFinancialInstitutionName());
                financialArrangementsDetailResponse.setEmi(CommonUtils.convertValueWithoutDecimal(financialArrangementsDetailRequest.getEmi()));
                //financialArrangementsDetailResponse.setLcbgStatus(!CommonUtils.isObjectNullOrEmpty(financialArrangementsDetailRequest.getLcBgStatus()) ? LCBG_Status_SBI.getById(financialArrangementsDetailRequest.getLcBgStatus()).getValue().toString() : "-");
                financialArrangementsDetailResponseList.add(financialArrangementsDetailResponse);
            }
            	map.put("financialArrangments",!CommonUtils.isListNullOrEmpty(financialArrangementsDetailResponseList) ? CommonUtils.printFields(financialArrangementsDetailResponseList,null) : null);
        } catch (Exception e) {
            logger.error("Problem to get Data of Financial Arrangements Details {}", e);
        }	
		
		//Co-Applicant FINANCIAL ARRANGEMENTS
		try {	
			List<CoApplicantDetail> coApplicantDetails = coApplicantService.getCoApplicantList(applicationId);
			List<Map<String , Object>> listMap = new ArrayList<Map<String,Object>>();
			for(CoApplicantDetail coApplicantDetail : coApplicantDetails) {
				CoApplicantRequest coApplicantRequest = new CoApplicantRequest();
				copyAddressFromDomainToRequestOfCoApplicant(coApplicantDetail, coApplicantRequest);
				BeanUtils.copyProperties(coApplicantDetail, coApplicantRequest);
				Map<String, Object> map1 = new HashMap<String, Object>();
				List<FinancialArrangementsDetailRequest> financialArrangementsDetailRequestList = financialArrangementDetailsService.getFinancialArrangementDetailsListDirId(coApplicantDetail.getId() , applicationId);	
				List<FinancialArrangementDetailResponseString> financialArrangementsDetailResponseList = new ArrayList<>();	
				for (FinancialArrangementsDetailRequest financialArrangementsDetailRequest : financialArrangementsDetailRequestList) {
					FinancialArrangementDetailResponseString financialArrangementsDetailResponse = new FinancialArrangementDetailResponseString();
					financialArrangementsDetailResponse.setOutstandingAmount(CommonUtils.convertValueWithoutDecimal(financialArrangementsDetailRequest.getOutstandingAmount()));	
					financialArrangementsDetailResponse.setSecurityDetails(financialArrangementsDetailRequest.getSecurityDetails());	
					financialArrangementsDetailResponse.setAmount(CommonUtils.convertValueWithoutDecimal(financialArrangementsDetailRequest.getAmount()));	
					financialArrangementsDetailResponse.setLoanDate(financialArrangementsDetailRequest.getLoanDate());	
					financialArrangementsDetailResponse.setLoanType(financialArrangementsDetailRequest.getLoanType());	
					financialArrangementsDetailResponse.setFinancialInstitutionName(financialArrangementsDetailRequest.getFinancialInstitutionName());	
					financialArrangementsDetailResponse.setEmi(CommonUtils.convertValueWithoutDecimal(financialArrangementsDetailRequest.getEmi()));	
					//financialArrangementsDetailResponse.setLcbgStatus(!CommonUtils.isObjectNullOrEmpty(financialArrangementsDetailRequest.getLcBgStatus()) ? LCBG_Status_SBI.getById(financialArrangementsDetailRequest.getLcBgStatus()).getValue().toString() : "-");	
					financialArrangementsDetailResponseList.add(financialArrangementsDetailResponse);	
				}
				map1.put("financialDetails", !CommonUtils.isListNullOrEmpty(financialArrangementsDetailResponseList) ? CommonUtils.printFields(financialArrangementsDetailResponseList,null) : null);
				map1.put("coAppDetail", CommonUtils.printFields(coApplicantRequest, null));
				listMap.add(map1);		
			}
			map.put("financialArrangmentsofCoApplicant",!CommonUtils.isListNullOrEmpty(listMap) ? CommonUtils.printFields(listMap,null) : null);
         } catch (Exception e) {	
            logger.error("Problem to get Data of Financial Arrangements Details {}", e);	
        }
		
		//SCORING for Applicant
		try {
			ScoringRequest scoringRequest = new ScoringRequest();
			scoringRequest.setApplicationId(applicationId);
			scoringRequest.setFpProductId(productId);
			ScoringResponse scoringResponse = scoringClient.getScore(scoringRequest);
			DecimalFormat df = new DecimalFormat(".##");
			List<Map<String,Object>> scoreResponse = new ArrayList<>(scoringResponse.getDataList().size());
			Map<String,Object> companyMap =new HashMap<>();
			ProposalScoreResponse proposalScoreResponse =  (ProposalScoreResponse)MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)scoringResponse.getDataObject(),ProposalScoreResponse.class);
			companyMap.put("scoringDataObject",CommonUtils.printFields(proposalScoreResponse,null));
			if(!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse)) {
				map.put("managementRiskScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskScore()) ? proposalScoreResponse.getManagementRiskScore().intValue(): "-");
				map.put("managementRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskMaxTotalScore()) ?  proposalScoreResponse.getManagementRiskMaxTotalScore().intValue():"-");
				map.put("managementRiskWeightOfScoring",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskWeightOfScoring()) ? proposalScoreResponse.getManagementRiskWeightOfScoring().intValue() :"-");
				map.put("managementRiskWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskWeight()) ? df.format((proposalScoreResponse.getManagementRiskWeight())): "-");
				map.put("managementRiskMaxTotalWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskMaxTotalWeight()) ? proposalScoreResponse.getManagementRiskMaxTotalWeight().intValue(): "-");
				
				map.put("financialRiskScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskScore()) ? proposalScoreResponse.getFinancialRiskScore().intValue() : "-");
				map.put("financialRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskMaxTotalScore()) ? proposalScoreResponse.getFinancialRiskMaxTotalScore().intValue():"-");
				map.put("financialRiskWeightOfScoring",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskWeightOfScoring()) ? proposalScoreResponse.getFinancialRiskWeightOfScoring().intValue(): "-");
				map.put("financialRiskWeight",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskWeight()) ? df.format((proposalScoreResponse.getFinancialRiskWeight())) : "-");
				map.put("financialRiskMaxTotalWeight",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskMaxTotalWeight()) ? proposalScoreResponse.getFinancialRiskMaxTotalWeight().intValue() : "-");
				
				map.put("businessRiskScore", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskScore()) ? proposalScoreResponse.getBusinessRiskScore().intValue():"-");
				map.put("businessRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskMaxTotalScore()) ? proposalScoreResponse.getBusinessRiskMaxTotalScore().intValue():"-");
				map.put("businessRiskWeightOfScoring", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskWeightOfScoring()) ? proposalScoreResponse.getBusinessRiskWeightOfScoring().intValue():"-");
				map.put("businessRiskWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskWeight()) ? df.format((proposalScoreResponse.getBusinessRiskWeight())):"-");
				map.put("businessRiskMaxTotalWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskMaxTotalWeight()) ? proposalScoreResponse.getBusinessRiskMaxTotalWeight().intValue():"-");
				
				map.put("totalActualScore", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskScore(), proposalScoreResponse.getFinancialRiskScore(), proposalScoreResponse.getBusinessRiskScore()).intValue());
				map.put("totalOutOfScore", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskMaxTotalScore(), proposalScoreResponse.getFinancialRiskMaxTotalScore(), proposalScoreResponse.getBusinessRiskMaxTotalScore()).intValue());
				map.put("totalWeight", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskWeightOfScoring(), proposalScoreResponse.getFinancialRiskWeightOfScoring(), proposalScoreResponse.getBusinessRiskWeightOfScoring()).intValue());
				map.put("totalRiskWeight", df.format(CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskWeight(), proposalScoreResponse.getFinancialRiskWeight(), proposalScoreResponse.getBusinessRiskWeight())));
				map.put("totalRiskMaxWeight", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskMaxTotalWeight(), proposalScoreResponse.getFinancialRiskMaxTotalWeight(), proposalScoreResponse.getBusinessRiskMaxTotalWeight()).intValue());
				
				map.put("interpretation", StringEscapeUtils.escapeXml(proposalScoreResponse.getInterpretation()));
				map.put("weightConsider", proposalScoreResponse.getWeightConsider() != null ? proposalScoreResponse.getWeightConsider() : false);
				map.put("isProposnate", proposalScoreResponse.getIsProportionateScoreConsider() != null ? proposalScoreResponse.getIsProportionateScoreConsider() : false);
				map.put("proposnateScoreFs", proposalScoreResponse.getProportionateScoreFS() != null ? proposalScoreResponse.getProportionateScoreFS() : false);
				map.put("proposnateScore", proposalScoreResponse.getProportionateScore() != null ? proposalScoreResponse.getProportionateScore() : false);
			}
			//Filter Parameters
			List<LinkedHashMap<String, Object>> mapList = (List<LinkedHashMap<String, Object>>)scoringResponse.getDataList();
			List<ProposalScoreDetailResponse> newMapList = new ArrayList<>(mapList.size());
			for(LinkedHashMap<String, Object> mp : mapList) {
				newMapList.add(MultipleJSONObjectHelper.getObjectFromMap(mp,ProposalScoreDetailResponse.class));
			}
			List<ProposalScoreDetailResponse> collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AGE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AGE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.CURRENT_JOB_EXP)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.CURRENT_JOB_EXP, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.TOTAL_WORK_EXP)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.TOTAL_WORK_EXP, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.RESIDENCE_TYPE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.RESIDENCE_TYPE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.NO_YEARS_STAY_CURR_LOC)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.NO_YEARS_STAY_CURR_LOC, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.BUREAU_SCORE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.BUREAU_SCORE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.MARITAL_STATUS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.MARITAL_STATUS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMPLOYMENT_TYPE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMPLOYMENT_TYPE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMPLOYMENT_CATEG_JOB)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMPLOYMENT_CATEG_JOB, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMPLOYMENT_CATEG_PROF_SELF_EMPLOYED)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMPLOYMENT_CATEG_PROF_SELF_EMPLOYED, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.CURRENT_EMPLOYMENT_STATUS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.CURRENT_EMPLOYMENT_STATUS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.MIN_BANKING_RELATIONSHIP)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.MIN_BANKING_RELATIONSHIP, CommonUtils.printFields(collect.get(0),null));
					} // new parameter Pl
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.SPOUSE_EMPLOYEMENT)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.SPOUSE_EMPLOYEMENT, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.NO_OF_DEPENDANTS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.NO_OF_DEPENDANTS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.DESIGNATION)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.DESIGNATION, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EDUCATION_QUALIFICATION)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EDUCATION_QUALIFICATION, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.NO_OF_APPLICANTS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.NO_OF_APPLICANTS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.ANNUAL_INCOME)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.ANNUAL_INCOME, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AVAILABLE_INCOME)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AVAILABLE_INCOME, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.ADDI_INCOME_SPOUSE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.ADDI_INCOME_SPOUSE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.MON_INCOME_DEPENDANT)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.MON_INCOME_DEPENDANT, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AVG_INCREASE_INCOME_REPORT_3_YEARS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AVG_INCREASE_INCOME_REPORT_3_YEARS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.APPLICANT_NW_TO_LOAN_AMOUNT)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.APPLICANT_NW_TO_LOAN_AMOUNT, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.TENURE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.TENURE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AGE_PROPERTY)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AGE_PROPERTY, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AVG_DEPOS_LAST_6_MONTH)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AVG_DEPOS_LAST_6_MONTH, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.CHECQUE_BOUNSE_LAST_1_MONTH)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.CHECQUE_BOUNSE_LAST_1_MONTH, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.CHECQUE_BOUNSE_LAST_6_MONTH)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.CHECQUE_BOUNSE_LAST_6_MONTH, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.DPD)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.DPD, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.LTV)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.LTV, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMI_NMI_RATIO)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMI_NMI_RATIO, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.LOAN_PURPOSE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.LOAN_PURPOSE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.INCOME_PROOF)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.INCOME_PROOF, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMI_NMI)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMI_NMI, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AVG_EOD_BALANCE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AVG_EOD_BALANCE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.LOAN_TO_INCOME_RATIO)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.LOAN_TO_INCOME_RATIO, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.INCOME_TO_INSTALLMENT_RATIO)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.INCOME_TO_INSTALLMENT_RATIO, CommonUtils.printFields(collect.get(0),null));
					}
					scoreResponse.add(companyMap);
					map.put("scoringResp", scoreResponse);
			}catch (Exception e) {
				logger.error("Error while getting scoring data : ",e);
			}
		
		//SCORING for Co-Applicant
		try {
			ScoringRequest scoringRequest = new ScoringRequest();
			scoringRequest.setApplicationId(applicationId);
			scoringRequest.setFpProductId(productId);
			List<CoApplicantDetail> coApplicantDetails = coApplicantService.getCoApplicantList(applicationId);
			List<List<Map<String,Object>>> coAppScoringData = new ArrayList<List<Map<String,Object>>>();
			
			if(coApplicantDetails != null) {
			for(CoApplicantDetail coApplicantDetail : coApplicantDetails) {
				DecimalFormat df = new DecimalFormat(".##");
				scoringRequest.setCoAppId(coApplicantDetail.getId());
				ScoringResponse scoringResponse = scoringClient.getScore(scoringRequest);
				List<Map<String,Object>> scoreResponse = new ArrayList<>(scoringResponse.getDataList().size());
				Map<String,Object> companyMap =new HashMap<>();
				ProposalScoreResponse proposalScoreResponse =  MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String,Object>)scoringResponse.getDataObject(),ProposalScoreResponse.class);
				companyMap.put("name",(coApplicantDetail.getFirstName() != null ? coApplicantDetail.getFirstName() : "") + " " +(coApplicantDetail.getMiddleName() != null ? coApplicantDetail.getMiddleName() : "") + " " + (coApplicantDetail.getLastName() != null ? coApplicantDetail.getLastName() : " ") );
				companyMap.put("scoringDataObject",CommonUtils.printFields(proposalScoreResponse,null));
			
				if(!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse)) {
					companyMap.put("managementRiskScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskScore()) ? proposalScoreResponse.getManagementRiskScore().intValue(): "-");
					companyMap.put("managementRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskMaxTotalScore()) ?  proposalScoreResponse.getManagementRiskMaxTotalScore().intValue():"-");
					companyMap.put("managementRiskWeightOfScoring",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskWeightOfScoring()) ? proposalScoreResponse.getManagementRiskWeightOfScoring().intValue() :"-");
					companyMap.put("managementRiskWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskWeight()) ? df.format((proposalScoreResponse.getManagementRiskWeight())): "-");
					companyMap.put("managementRiskMaxTotalWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getManagementRiskMaxTotalWeight()) ? proposalScoreResponse.getManagementRiskMaxTotalWeight().intValue(): "-");
					
					companyMap.put("financialRiskScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskScore()) ? proposalScoreResponse.getFinancialRiskScore().intValue() : "-");
					companyMap.put("financialRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskMaxTotalScore()) ? proposalScoreResponse.getFinancialRiskMaxTotalScore().intValue():"-");
					companyMap.put("financialRiskWeightOfScoring",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskWeightOfScoring()) ? proposalScoreResponse.getFinancialRiskWeightOfScoring().intValue(): "-");
					companyMap.put("financialRiskWeight",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskWeight()) ? df.format((proposalScoreResponse.getFinancialRiskWeight())) : "-");
					companyMap.put("financialRiskMaxTotalWeight",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getFinancialRiskMaxTotalWeight()) ? proposalScoreResponse.getFinancialRiskMaxTotalWeight().intValue() : "-");
					
					companyMap.put("businessRiskScore", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskScore()) ? proposalScoreResponse.getBusinessRiskScore().intValue():"-");
					companyMap.put("businessRiskMaxTotalScore",!CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskMaxTotalScore()) ? proposalScoreResponse.getBusinessRiskMaxTotalScore().intValue():"-");
					companyMap.put("businessRiskWeightOfScoring", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskWeightOfScoring()) ? proposalScoreResponse.getBusinessRiskWeightOfScoring().intValue():"-");
					companyMap.put("businessRiskWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskWeight()) ? df.format((proposalScoreResponse.getBusinessRiskWeight())):"-");
					companyMap.put("businessRiskMaxTotalWeight", !CommonUtils.isObjectNullOrEmpty(proposalScoreResponse.getBusinessRiskMaxTotalWeight()) ? proposalScoreResponse.getBusinessRiskMaxTotalWeight().intValue():"-");
					
					companyMap.put("totalActualScore", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskScore(), proposalScoreResponse.getFinancialRiskScore(), proposalScoreResponse.getBusinessRiskScore()).intValue());
					companyMap.put("totalOutOfScore", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskMaxTotalScore(), proposalScoreResponse.getFinancialRiskMaxTotalScore(), proposalScoreResponse.getBusinessRiskMaxTotalScore()).intValue());
					companyMap.put("totalWeight", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskWeightOfScoring(), proposalScoreResponse.getFinancialRiskWeightOfScoring(), proposalScoreResponse.getBusinessRiskWeightOfScoring()).intValue());
					companyMap.put("totalRiskWeight", df.format(CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskWeight(), proposalScoreResponse.getFinancialRiskWeight(), proposalScoreResponse.getBusinessRiskWeight())));
					companyMap.put("totalRiskMaxWeight", CommonUtils.addNumbers(proposalScoreResponse.getManagementRiskMaxTotalWeight(), proposalScoreResponse.getFinancialRiskMaxTotalWeight(), proposalScoreResponse.getBusinessRiskMaxTotalWeight()).intValue());
					
					companyMap.put("interpretation", StringEscapeUtils.escapeXml(proposalScoreResponse.getInterpretation()));
					companyMap.put("weightConsider", proposalScoreResponse.getWeightConsider() != null ? proposalScoreResponse.getWeightConsider() : false);
					companyMap.put("isProposnate", proposalScoreResponse.getIsProportionateScoreConsider() != null ? proposalScoreResponse.getIsProportionateScoreConsider() : false);
					companyMap.put("proposnateScoreFs", proposalScoreResponse.getProportionateScoreFS() != null ? proposalScoreResponse.getProportionateScoreFS() : false);
					companyMap.put("proposnateScore", proposalScoreResponse.getProportionateScore() != null ? proposalScoreResponse.getProportionateScore() : false);
				}
				
				//Filter Parameters
				List<LinkedHashMap<String, Object>> mapList = (List<LinkedHashMap<String, Object>>)scoringResponse.getDataList();
				List<ProposalScoreDetailResponse> newMapList = new ArrayList<>(mapList.size());
				for(LinkedHashMap<String, Object> mp : mapList) {
					newMapList.add(MultipleJSONObjectHelper.getObjectFromMap(mp,ProposalScoreDetailResponse.class));
				}
				
				List<ProposalScoreDetailResponse> collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AGE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AGE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.CURRENT_JOB_EXP)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.CURRENT_JOB_EXP, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.TOTAL_WORK_EXP)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.TOTAL_WORK_EXP, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.RESIDENCE_TYPE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.RESIDENCE_TYPE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.NO_YEARS_STAY_CURR_LOC)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.NO_YEARS_STAY_CURR_LOC, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.BUREAU_SCORE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.BUREAU_SCORE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.MARITAL_STATUS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.MARITAL_STATUS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMPLOYMENT_TYPE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMPLOYMENT_TYPE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMPLOYMENT_CATEG_JOB)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMPLOYMENT_CATEG_JOB, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMPLOYMENT_CATEG_PROF_SELF_EMPLOYED)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMPLOYMENT_CATEG_PROF_SELF_EMPLOYED, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.CURRENT_EMPLOYMENT_STATUS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.CURRENT_EMPLOYMENT_STATUS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.MIN_BANKING_RELATIONSHIP)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.MIN_BANKING_RELATIONSHIP, CommonUtils.printFields(collect.get(0),null));
					} // new parameter Pl
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.SPOUSE_EMPLOYEMENT)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.SPOUSE_EMPLOYEMENT, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.NO_OF_DEPENDANTS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.NO_OF_DEPENDANTS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.DESIGNATION)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.DESIGNATION, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EDUCATION_QUALIFICATION)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EDUCATION_QUALIFICATION, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.NO_OF_APPLICANTS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.NO_OF_APPLICANTS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.ANNUAL_INCOME)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.ANNUAL_INCOME, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AVAILABLE_INCOME)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AVAILABLE_INCOME, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.ADDI_INCOME_SPOUSE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.ADDI_INCOME_SPOUSE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.MON_INCOME_DEPENDANT)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.MON_INCOME_DEPENDANT, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AVG_INCREASE_INCOME_REPORT_3_YEARS)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AVG_INCREASE_INCOME_REPORT_3_YEARS, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.APPLICANT_NW_TO_LOAN_AMOUNT)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.APPLICANT_NW_TO_LOAN_AMOUNT, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.TENURE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.TENURE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AGE_PROPERTY)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AGE_PROPERTY, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AVG_DEPOS_LAST_6_MONTH)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AVG_DEPOS_LAST_6_MONTH, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.CHECQUE_BOUNSE_LAST_1_MONTH)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.CHECQUE_BOUNSE_LAST_1_MONTH, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.CHECQUE_BOUNSE_LAST_6_MONTH)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.CHECQUE_BOUNSE_LAST_6_MONTH, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.DPD)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.DPD, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.LTV)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.LTV, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMI_NMI_RATIO)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMI_NMI_RATIO, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.LOAN_PURPOSE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.LOAN_PURPOSE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.INCOME_PROOF)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.INCOME_PROOF, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.EMI_NMI)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.EMI_NMI, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.AVG_EOD_BALANCE)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.AVG_EOD_BALANCE, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.LOAN_TO_INCOME_RATIO)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.LOAN_TO_INCOME_RATIO, CommonUtils.printFields(collect.get(0),null));
					}
					collect = newMapList.stream().filter(m -> m.getParameterName().equalsIgnoreCase(Retail.HomeLoan.INCOME_TO_INSTALLMENT_RATIO)).collect(Collectors.toList());
					if(!CommonUtils.isListNullOrEmpty(collect)) {
						companyMap.put(Retail.HomeLoan.INCOME_TO_INSTALLMENT_RATIO, CommonUtils.printFields(collect.get(0),null));
					}
					scoreResponse.add(companyMap);
					coAppScoringData.add(scoreResponse);
			}
			map.put("scoringRespOfCoApp", !CommonUtils.isObjectListNull(coAppScoringData) ? coAppScoringData : null);
			}
				
		}catch (Exception e) {
			logger.error("Error while getting scoring data : ",e);
		}
		
		//PERFIOS API DATA (BANK STATEMENT ANALYSIS)
		ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(applicationId);
		reportRequest.setUserId(userId);
		List<Data> datas = new ArrayList<>();
	  //List<Object> bankStatement = new ArrayList<Object>();
	/**	List<Object> monthlyDetails = new ArrayList<Object>();
		List<Object> top5FundReceived = new ArrayList<Object>();
		List<Object> top5FundTransfered = new ArrayList<Object>();
		List<Object> bouncedChequeList = new ArrayList<Object>();
		List<Object> customerInfo = new ArrayList<Object>();
		List<Object> summaryInfo = new ArrayList<Object>();*/


		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			List<HashMap<String, Object>> listhashMap = (List<HashMap<String, Object>>) analyzerResponse.getData();
			//List<HashMap<String, Object>> bankDataDetails = new ArrayList<HashMap<String,Object>>(); 

			if (!CommonUtils.isListNullOrEmpty(listhashMap)) {	
				for (HashMap<String, Object> rec : listhashMap) {
					Data data = MultipleJSONObjectHelper.getObjectFromMap(rec, Data.class);
					datas.add(data);
							
					//bankStatement.add(!CommonUtils.isObjectNullOrEmpty(data.getXns()) ? CommonUtils.printFields(data.getXns().getXn(),null) : " ");
					/**monthlyDetails.add(!CommonUtils.isObjectNullOrEmpty(data.getMonthlyDetailList()) ? CommonUtils.printFields(data.getMonthlyDetailList(),null) : "");
					top5FundReceived.add(!CommonUtils.isObjectNullOrEmpty(data.getTop5FundReceivedList()) ? CommonUtils.printFields(data.getTop5FundReceivedList().getItem(),null) : "");
					top5FundTransfered.add(!CommonUtils.isObjectNullOrEmpty(data.getTop5FundTransferedList()) ? CommonUtils.printFields(data.getTop5FundTransferedList().getItem(),null) : "");
					bouncedChequeList.add(!CommonUtils.isObjectNullOrEmpty(data.getBouncedOrPenalXnList()) ? CommonUtils.printFields(data.getBouncedOrPenalXnList().getBouncedOrPenalXns(),null) : " ");
					customerInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getCustomerInfo()) ? CommonUtils.printFields(data.getCustomerInfo(),null) : " ");
					summaryInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getSummaryInfo()) ?CommonUtils.printFields(data.getSummaryInfo(),null) : " ");*/
						
				/**HashMap<String, Object>  bankData = new HashMap<>();
					bankData.put("monthlyDetails", monthlyDetails);
					bankData.put("top5FundReceived", top5FundReceived);
					bankData.put("top5FundTransfered", top5FundTransfered);
					bankData.put("bouncedChequeList", bouncedChequeList);
					bankData.put("customerInfo", customerInfo);
					bankData.put("summaryInfo", summaryInfo);
					bankData.put("bankStatementAnalysis", CommonUtils.printFields(datas, null));
					bankDataDetails.add(bankData);*/
				}
						
				map.put("bankRelatedData" , CommonUtils.printFields(datas, null));
				//map.put("bankStatement", bankStatement);
				/**map.put("monthlyDetails", monthlyDetails);
				map.put("top5FundReceived", top5FundReceived);
				map.put("top5FundTransfered", top5FundTransfered);
				map.put("bouncedChequeList", bouncedChequeList);
				map.put("customerInfo", customerInfo);
				map.put("summaryInfo", summaryInfo);
				map.put("bankStatementAnalysis", CommonUtils.printFields(datas, null));*/
			}
		} catch (Exception e) {
			logger.error("Error while getting perfios data : ",e);
		}

		//ELIGIBILITY DATA (ASSESSMENT TO LIMITS)
		try{
			EligibililityRequest eligibilityReq=new EligibililityRequest();
			eligibilityReq.setApplicationId(applicationId);
			eligibilityReq.setFpProductMappingId(productId);
			EligibilityResponse eligibilityResp= eligibilityClient.getHLLoanData(eligibilityReq);
			if(!CommonUtils.isObjectListNull(eligibilityResp,eligibilityResp.getData())){
				//mapData.put("assLimit", CommonUtils.convertToValueForXml(MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)eligibilityResp.getData(), RetailEligibilityRequest.class), new HashMap<>()));
				map.put("assLimits",CommonUtils.printFieldsForValue((LinkedHashMap<String, Object>)eligibilityResp.getData(),new HashMap<>()));
			}
		}catch (Exception e) {
			logger.error("Error while getting Eligibility data : ",e);
		}
		/*************************************************************FINAL DETAILS***********************************************************/
		
		//ekyc data
		try {
			EmployerRequest epfReq = new EmployerRequest();
			epfReq.setApplicationId(applicationId);
			EkycResponse epfRes = epfClient.getEpfData(epfReq);
			if(epfRes != null && epfRes.getData()!= null) {
				map.put("epfoData", epfRes.getData());
			}else {
				logger.info("eKYCData is null for ApplicationId==>{}"+applicationId);
			}
		} catch (Exception e) {
			logger.info("Error/Exception while fetching ekyc Data in HL Cam of ApplicationId==>{} Error:",applicationId ,e);
		}
		
		if(isFinalView) {
			
			Map<String , Object> retailMap = new HashMap<String, Object>();
			try {
				
			
			PLRetailApplicantRequest plRetailApplicantRequest = plRetailApplicantService.getPrimaryByProposalId(userId, applicationId, proposalId);
			if(plRetailApplicantRequest == null) {
				logger.error("Error/Exception while fetching primary details in final View of ApplicationId==>>{}",applicationId);
			}
			//PROPOSAL DATES
			/**try {
				List<Object[]> data = null;
				List<Date[]> data1 = null;
				data1 = loanDisbursementRepository.findDisbursementDateByApplicationId(applicationId);
				if(data1 != null && !data1.isEmpty()) {
					map.put("disbursmentDate", simpleDateFormat.format(data1.get(0)));
				}
				data1 = loanSanctionRepository.findSanctionDateByApplicationId(applicationId);
				if(data1 != null && !data1.isEmpty()) {
					map.put("sanctionDate", simpleDateFormat.format(data1.get(0)));
				}
				data = proposalDetailsRepository.findProposalDetailByApplicationId(applicationId);
				if(data != null && !data.isEmpty()) {
					String status = data.get(0) != null ? data.get(0)[1].toString() : "";
					if(status.equals("3")) {
						map.put("onHoldDate", data.get(0)[0]);
					}else if(status.equals("4")) {
						map.put("rejectedDate", data.get(0)[0]);
					}
				}
				} catch (Exception e) {
				logger.error("Error while getting PROPOSAL DATES data : ",e);
			}*/
			
			//Checker DATES
			try {
				if(applicationProposalMapping != null) {
					map.put("checkerDate" ,!CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getApprovedDate()) ? simpleDateFormat.format(applicationProposalMapping.getApprovedDate()) : "-");
				}
			}catch (Exception e) {
				logger.error("Error while getting PROPOSAL DATES data of ApplicationId==>{}   Error:{} ",applicationId ,e);
			}
			
			//Maker Date
			try {
				Object makerDate = commonRepository.getMakerDate(applicationId);
				if(makerDate != null) {
					map.put("makerDate", simpleDateFormat.format(makerDate));
				}
			}catch (Exception e) {
				logger.error("Error/Excpetion while getting maker Date from workFlow of ApplicationId==>{} Error:{}" ,applicationId ,e);
			}
			
			
			//RETAIL FINAL DETAILS
			try {
				FinalHomeLoanDetail finalHomeLoanDetail = finalHomeLoanDetailRepository.getByApplicationAndProposalId(applicationId, proposalId);
				if(!CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail)) {
					retailMap.put("motherMaidenName", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getMatherMaidenName()) ? StringEscapeUtils.escapeXml(finalHomeLoanDetail.getMatherMaidenName()) : "-");
					retailMap.put("nameOfSpouse", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getNameOfSpouse()) ? StringEscapeUtils.escapeXml(finalHomeLoanDetail.getNameOfSpouse()) : "-");
					retailMap.put("noOfChildren", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getNoOfChildren()) ? finalHomeLoanDetail.getNoOfChildren() : "-");
					retailMap.put("birthPlace", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getPlaceOfBirth()) ? StringEscapeUtils.escapeXml(finalHomeLoanDetail.getPlaceOfBirth()) : "-");
					retailMap.put("educationalQualificationYear", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getYear()) ? finalHomeLoanDetail.getYear() : "-");
					retailMap.put("religion", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getReligion()) ? ReligionRetailMst.getById(finalHomeLoanDetail.getReligion()).getValue() : "-");
					retailMap.put("caste", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getCast()) ? CastCategory.getById(finalHomeLoanDetail.getCast()).getValue() : "-");
								
					try {
						retailMap.put("permanantAddPremise", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getPermanentPremiseNo()) ? CommonUtils.printFields(finalHomeLoanDetail.getPermanentPremiseNo(),null) + "," : "");
						retailMap.put("permanantAddStreetName", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getPermanentStreetName()) ? CommonUtils.printFields(finalHomeLoanDetail.getPermanentStreetName(),null) + "," : "");
						retailMap.put("permanantAddLandmark", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getPermanentLandmark()) ? CommonUtils.printFields(finalHomeLoanDetail.getPermanentLandmark(),null) + "," : "");
						retailMap.put("permanantAddCountry", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getPermanentCountry()) ? StringEscapeUtils.escapeXml(getCountryName(finalHomeLoanDetail.getPermanentCountry())) + "," : "");
						retailMap.put("permanantAddState", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getPermanentState()) ? StringEscapeUtils.escapeXml(getStateName(finalHomeLoanDetail.getPermanentState()))  + ",": "");
						retailMap.put("permanantAddCity", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getPermanentCity()) ? StringEscapeUtils.escapeXml(getCityName(finalHomeLoanDetail.getPermanentCity().longValue())) + ",": "");
						retailMap.put("permanantAddPincode", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getPermanentPinCode()) ? finalHomeLoanDetail.getPermanentPinCode() : "");
			
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}

					try {	
						retailMap.put("correspondencePremise", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getCorrespondencePremiseNo()) ? CommonUtils.printFields(finalHomeLoanDetail.getCorrespondencePremiseNo(),null) + "," : "");
						retailMap.put("correspondenceStreetName", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getCorrespondenceStreetName()) ? CommonUtils.printFields(finalHomeLoanDetail.getCorrespondenceStreetName(),null) + "," : "");
						retailMap.put("correspondenceLandmark", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getCorrespondenceLandmark()) ? CommonUtils.printFields(finalHomeLoanDetail.getCorrespondenceLandmark(),null) + "," : "");
						retailMap.put("correspondenceCountry", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getCorrespondenceCountry()) ? StringEscapeUtils.escapeXml(getCountryName(finalHomeLoanDetail.getCorrespondenceCountry())) + ",": "");
						retailMap.put("correspondenceState",  !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getCorrespondenceState()) ? StringEscapeUtils.escapeXml(getStateName(finalHomeLoanDetail.getCorrespondenceState())) + "," : "");
						retailMap.put("correspondenceCity",  !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getCorrespondenceCity()) ? StringEscapeUtils.escapeXml(getCityName(finalHomeLoanDetail.getCorrespondenceCity().longValue())) + "," : "");
						retailMap.put("correspondencePincode", !CommonUtils.isObjectNullOrEmpty(finalHomeLoanDetail.getCorrespondencePinCode()) ? finalHomeLoanDetail.getCorrespondencePinCode() : "");
						
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					
					retailMap.put("nameOfSeller", finalHomeLoanDetail.getSellerName() != null ? finalHomeLoanDetail.getSellerName() : "-");
					retailMap.put("sellerAddress", finalHomeLoanDetail.getSellerAddress() != null ? finalHomeLoanDetail.getSellerAddress() : "-");
					retailMap.put("sellerPincode", finalHomeLoanDetail.getSellerPincode() != null ? finalHomeLoanDetail.getSellerPincode() : "-");
					retailMap.put("sellerCity", finalHomeLoanDetail.getSellerCity() != null ? StringEscapeUtils.escapeXml(getCityName(finalHomeLoanDetail.getSellerCity().longValue())) : "-");
					retailMap.put("sellerState", finalHomeLoanDetail.getSellerState() != null ? StringEscapeUtils.escapeXml(getStateName(finalHomeLoanDetail.getSellerState())) : "-");
					
					retailMap.put("dateOfExisLoanTaken", finalHomeLoanDetail.getDateOfExistingLoanTaken() != null ? simpleDateFormat.format(finalHomeLoanDetail.getDateOfExistingLoanTaken()) : "-");
					retailMap.put("originalValueOfProperty", finalHomeLoanDetail.getOriginalValueOfProperty() != null ? CommonUtils.convertValueWithoutDecimal(finalHomeLoanDetail.getOriginalValueOfProperty().doubleValue()) : "-");
				}
				
			} catch (Exception e) {
				logger.error("Error while getting Final Information : ",e);
			}
			
			//Current Bank Account Detail
			try {
				List<BankAccountHeldDetailsRequest> bankAccountHeldDetails = bankAccountHeldDetailService.getExistingLoanDetailListByProposalId(proposalId, 0);
				
				if(!CommonUtils.isObjectNullOrEmpty(bankAccountHeldDetails)) {
					retailMap.put("bankAccountHeldDetails", !CommonUtils.isObjectListNull(bankAccountHeldDetails) ? bankAccountHeldDetails : null);
				}
			}catch (Exception e) {
				logger.error("Error/Exception while fetching data of Current Bank Account in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
			}
			
			//Fixed Deposit Detail
			try {
				List<FixedDepositsDetailsRequest> fixedDepositsDetails = fixedDepositsDetailService.getFixedDepositsDetailByProposalId(proposalId, 0);
				
				if(!CommonUtils.isObjectNullOrEmpty(fixedDepositsDetails)) {
					retailMap.put("fixedDepositDetails", !CommonUtils.isObjectListNull(fixedDepositsDetails) ? fixedDepositsDetails : null);
				}
			}catch (Exception e) {
				logger.error("Error/Exception while fetching data of fixed deposit details in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
			}
			
			//Investment Detail
			try {
				List<OtherCurrentAssetDetailRequest> investmentDetails = otherCurrentAssetDetailService.getOtherCurrentAssetDetailListByProposalId(proposalId, 1);
				
				if(!CommonUtils.isObjectNullOrEmpty(investmentDetails)) {
					retailMap.put("investmentDetails", !CommonUtils.isObjectListNull(investmentDetails) ? investmentDetails : null);
				}
			}catch (Exception e) {
				logger.error("Error/Exception while fetching data of investment details in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
			}
			
			//Other Income Detail
			try {
				List<OtherIncomeDetailRequest> otherIncomeDetails = otherIncomeDetailService.getOtherIncomeDetailList(applicationId, 1, proposalId);
				
				if(!CommonUtils.isObjectNullOrEmpty(otherIncomeDetails)) {
					retailMap.put("otherIncomeDetails", !CommonUtils.isObjectListNull(otherIncomeDetails) ? otherIncomeDetails : null);
				}
			}catch (Exception e) {
				logger.error("Error/Exception while fetching data of other income details in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
			}
			
			if(plRetailApplicantRequest != null) {
				
				//Emp Salaried Type
				if(plRetailApplicantRequest.getEmploymentType() != null && plRetailApplicantRequest.getEmploymentType() == OccupationNature.SALARIED.getId()) {
					try {
						List<EmpSalariedTypeRequest> empSalariedDetail = empFinancialDetailsService.getSalariedEmpFinDetailListByProposalId(proposalId, 0);
						
						if(!CommonUtils.isObjectNullOrEmpty(empSalariedDetail)) {
							retailMap.put("empSalariedDetails", !CommonUtils.isObjectListNull(empSalariedDetail) ? empSalariedDetail : null);
						}
					}catch (Exception e) {
						logger.error("Error/Exception while fetching data of Emp Salaried Type in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
					}
				}
				
				//Emp SelfEmployed Type
				if(plRetailApplicantRequest.getEmploymentType() != null && (plRetailApplicantRequest.getEmploymentType() == OccupationNature.BUSINESS.getId() || plRetailApplicantRequest.getEmploymentType() == OccupationNature.SELF_EMPLOYED.getId() || plRetailApplicantRequest.getEmploymentType() == OccupationNature.SELF_EMPLOYED_PROFESSIONAL.getId())) {
					try {
						List<EmpSelfEmployedTypeRequest> empSelfEmployedTypeDetail = empFinancialDetailsService.getSelfEmpFinDetailListByProposalId(proposalId, 0);
						
						if(!CommonUtils.isObjectNullOrEmpty(empSelfEmployedTypeDetail)) {
							retailMap.put("empSelfEmployedTypeDetails", !CommonUtils.isObjectListNull(empSelfEmployedTypeDetail) ? empSelfEmployedTypeDetail : null);
						}
					}catch (Exception e) {
						logger.error("Error/Exception while fetching data of Emp Self Employed Type in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
					}
				}
				
				//Emp Agriculturist Type
				if(plRetailApplicantRequest.getEmploymentType() != null && plRetailApplicantRequest.getEmploymentType() == OccupationNature.AGRICULTURIST.getId()) {
					try {
						List<EmpAgriculturistTypeRequest> empAgriculturistTypeDetail = empFinancialDetailsService.getAgriculturistEmpFinDetailListByProposalId(proposalId, 0);
						
						if(!CommonUtils.isObjectNullOrEmpty(empAgriculturistTypeDetail)) {
							retailMap.put("agriculturistDetails", !CommonUtils.isObjectListNull(empAgriculturistTypeDetail) ? empAgriculturistTypeDetail : null);
						}
					}catch (Exception e) {
						logger.error("Error/Exception while fetching data of Agriculturist in home loan CAM of ApplicationId==>{} and ProposalId==>{} with Error==>{}" , applicationId ,proposalId ,e);
					}
				}
			}
			
			try {
				if(plRetailApplicantRequest != null) {
				
					if(plRetailApplicantRequest.getLoanPurpose() != null && plRetailApplicantRequest.getLoanPurpose() == HomeLoanPurpose.PURCHASE.getId()) {
						//Purchase Property Details
						try {
							List<Map<String, Object>> listDataOfProperty = new ArrayList<Map<String,Object>>(); 
							List<PurchasePropertyDetails> purchasePropertyDetails = purchasePropertyDetailsRepository.getListByApplicationId(applicationId);
							for(PurchasePropertyDetails purchasePropertyDetail : purchasePropertyDetails) {
								Map<String , Object> purchasePropertyDetailsReq = new HashMap<String, Object>();
								purchasePropertyDetailsReq.put("propertyName", purchasePropertyDetail.getPropertyName() != null ? purchasePropertyDetail.getPropertyName() : "-");
								purchasePropertyDetailsReq.put("cityName" ,purchasePropertyDetail.getCity() != null ? StringEscapeUtils.escapeXml(getCityName(purchasePropertyDetail.getCity().longValue())) : "-");
								purchasePropertyDetailsReq.put("stateName" ,purchasePropertyDetail.getState() != null ? StringEscapeUtils.escapeXml(getStateName(purchasePropertyDetail.getState())) : "-");
								purchasePropertyDetailsReq.put("buildUpArea" ,purchasePropertyDetail.getBuildUpArea() != null ? purchasePropertyDetail.getBuildUpArea() : "-");
								purchasePropertyDetailsReq.put("carpetArea" ,purchasePropertyDetail.getCarpetArea() != null ? purchasePropertyDetail.getCarpetArea() : "-");
								purchasePropertyDetailsReq.put("superBuildUpArea" ,purchasePropertyDetail.getSuperBuildUpArea() != null ? purchasePropertyDetail.getSuperBuildUpArea() : "-");
								purchasePropertyDetailsReq.put("totalPriceOfProperty" ,purchasePropertyDetail.getTotalPriceOfProperty() != null ? CommonUtils.convertValueWithoutDecimal(purchasePropertyDetail.getTotalPriceOfProperty().doubleValue()) : "-");
								listDataOfProperty.add(purchasePropertyDetailsReq);		
							}
							
							retailMap.put("purchasePropertyDetails", !CommonUtils.isObjectListNull(listDataOfProperty) ? listDataOfProperty : null);	
						}catch (Exception e) {
							logger.error("Error/Exception while fetching ListData of Property Details of ApplicationId==>{}" , applicationId);
						}
					}else if(plRetailApplicantRequest.getLoanPurpose() != null && plRetailApplicantRequest.getLoanPurpose() == HomeLoanPurpose.CONSTRUCTION_EXPANSION.getId()) {
						//Other Property Details
						try {
							List<Map<String, Object>> otherPropertyListData = new ArrayList<Map<String,Object>>();
							List<OtherPropertyDetails> otherPropertyDetails= otherPropertyDetailsRepository.getListByApplicationId(applicationId);
							for(OtherPropertyDetails otherPropertyDetail : otherPropertyDetails){
								if(otherPropertyDetail != null && otherPropertyDetail.getTotalCostOfLand() != null) {
									Map<String ,Object> constructionDetails = new HashMap<String, Object>();	
									constructionDetails.put("totalCostOfLand", otherPropertyDetail.getTotalCostOfLand() != null ? CommonUtils.convertValueWithoutDecimal(otherPropertyDetail.getTotalCostOfLand().doubleValue()) : "-");
									constructionDetails.put("totalCostOfConstruction", otherPropertyDetail.getTotalCostOfConstruction() != null ? CommonUtils.convertValueWithoutDecimal(otherPropertyDetail.getTotalCostOfConstruction().doubleValue()) : "-");
									constructionDetails.put("timeForCompletionConstruction" ,otherPropertyDetail.getTimeForCompletionConstruction() != null ? otherPropertyDetail.getTimeForCompletionConstruction() : "-");
									
									otherPropertyListData.add(!CommonUtils.isObjectListNull(constructionDetails) ? constructionDetails : null);
								}
							}
							
							retailMap.put("constructionDetails", !CommonUtils.isObjectListNull(otherPropertyListData) ? otherPropertyListData : null);
						}catch (Exception e) {
							logger.error("Error/Exception while fetching ListData of Property Other Details of ApplicationId==>{}",applicationId);
						}
					}else if(plRetailApplicantRequest.getLoanPurpose() != null && plRetailApplicantRequest.getLoanPurpose() == HomeLoanPurpose.REPAIRS_RENOVATIONS.getId()) {
						//Other Property Details
						try {
							List<Map<String, Object>> otherPropertyListData = new ArrayList<Map<String,Object>>();
							List<OtherPropertyDetails> otherPropertyDetails= otherPropertyDetailsRepository.getListByApplicationId(applicationId);
							for(OtherPropertyDetails otherPropertyDetail : otherPropertyDetails){
								if(otherPropertyDetail != null && otherPropertyDetail.getTotalCostOfRenovation() != null) {
									Map<String ,Object> repairDetails = new HashMap<String, Object>();
									repairDetails.put("typeOfRepairRenovation", otherPropertyDetail.getTypeOfRepairRenovation() != null ? otherPropertyDetail.getTypeOfRepairRenovation() : "-");
									repairDetails.put("totalCostOfRenovation", otherPropertyDetail.getTotalCostOfRenovation() != null ? CommonUtils.convertValueWithoutDecimal(otherPropertyDetail.getTotalCostOfRenovation().doubleValue()) : "-");
									repairDetails.put("timeForCompletionRenovation" ,otherPropertyDetail.getTimeForCompletionRenovation() != null ? otherPropertyDetail.getTimeForCompletionRenovation() : "-");
									
									otherPropertyListData.add(!CommonUtils.isObjectListNull(repairDetails) ? repairDetails : null);
								}
							}
							
							retailMap.put("repairDetails", !CommonUtils.isObjectListNull(otherPropertyListData) ? otherPropertyListData : null);
						}catch (Exception e) {
							logger.error("Error/Exception while fetching ListData of Property Other Details of ApplicationId==>{}",applicationId);
						}
					}
				}
			}catch (Exception e) {
				// TODO: handle exception
			}
			
			//References Details
			try {
				List<Map<String, Object>> referencesRetailsListData = new ArrayList<Map<String,Object>>();
				List<ReferencesRetailDetail> referencesRetailDetails = referenceRetailDetailsRepository.listReferencesRetailFromPropsalId(proposalId);
				for(ReferencesRetailDetail referencesRetailDetail : referencesRetailDetails) {
					Map<String , Object> referencesRetailsData = new HashMap<String, Object>();
					referencesRetailsData.put("referenceNo", referencesRetailDetail.getReferencesListId());
					referencesRetailsData.put("name", referencesRetailDetail.getName() != null ? referencesRetailDetail.getName() : "-");
					referencesRetailsData.put("address" ,referencesRetailDetail.getAddress() != null ? referencesRetailDetail.getAddress() : "-");
					referencesRetailsData.put("email" ,referencesRetailDetail.getEmail() != null ? referencesRetailDetail.getEmail() : "-");
					referencesRetailsData.put("tel" ,referencesRetailDetail.getTelephone() != null ? referencesRetailDetail.getTelephone() : "-");
					referencesRetailsData.put("mobile" , referencesRetailDetail.getMobile() != null ? referencesRetailDetail.getMobile() : "-");
					referencesRetailsListData.add(referencesRetailsData);
				}
				
				retailMap.put("referencesRetailData", !CommonUtils.isObjectListNull(referencesRetailsListData) ? referencesRetailsListData : null);
			}catch (Exception e) {
				logger.error("Error/Exception while fetching references Details in Final Cam of ApplicationId==>{}" ,applicationId);
			}
			
			map.put("retailData", retailMap);
			} catch (Exception e) {
				logger.error("Error/Exception while fetching final details of ApplicationId==>{}",applicationId);
			}
		}
		
		return map;
	}
	
	@Override
	public Map<String, Object> getHLBankStatementAnalysisReport(Long applicationId, Long productId) {
		Map<String, Object> map = new HashMap<String, Object>();
		Long userId = loanApplicationRepository.getUserIdByApplicationId(applicationId);
		CorporateApplicantRequest corporateApplicantRequest = corporateApplicantService.getCorporateApplicant(applicationId);
		try {
			if(corporateApplicantRequest != null) {
				map.put("orgName", StringEscapeUtils.escapeXml(corporateApplicantRequest.getOrganisationName()));
			}
		} catch (Exception e1) {
			logger.error(CommonUtils.EXCEPTION,e1);
		}
		
		//PERFIOS API DATA (BANK STATEMENT ANALYSIS)
		ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(applicationId);
		reportRequest.setUserId(userId);
		List<Data> datas = new ArrayList<>();
	  //List<Object> bankStatement = new ArrayList<Object>();
	/**	List<Object> monthlyDetails = new ArrayList<Object>();
		List<Object> top5FundReceived = new ArrayList<Object>();
		List<Object> top5FundTransfered = new ArrayList<Object>();
		List<Object> bouncedChequeList = new ArrayList<Object>();
		List<Object> customerInfo = new ArrayList<Object>();
		List<Object> summaryInfo = new ArrayList<Object>();*/


		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			List<HashMap<String, Object>> listhashMap = (List<HashMap<String, Object>>) analyzerResponse.getData();
			//List<HashMap<String, Object>> bankDataDetails = new ArrayList<HashMap<String,Object>>(); 

			if (!CommonUtils.isListNullOrEmpty(listhashMap)) {	
				for (HashMap<String, Object> rec : listhashMap) {
					Data data = MultipleJSONObjectHelper.getObjectFromMap(rec, Data.class);
					datas.add(data);
							
					//bankStatement.add(!CommonUtils.isObjectNullOrEmpty(data.getXns()) ? CommonUtils.printFields(data.getXns().getXn(),null) : " ");
					/**monthlyDetails.add(!CommonUtils.isObjectNullOrEmpty(data.getMonthlyDetailList()) ? CommonUtils.printFields(data.getMonthlyDetailList(),null) : "");
					top5FundReceived.add(!CommonUtils.isObjectNullOrEmpty(data.getTop5FundReceivedList()) ? CommonUtils.printFields(data.getTop5FundReceivedList().getItem(),null) : "");
					top5FundTransfered.add(!CommonUtils.isObjectNullOrEmpty(data.getTop5FundTransferedList()) ? CommonUtils.printFields(data.getTop5FundTransferedList().getItem(),null) : "");
					bouncedChequeList.add(!CommonUtils.isObjectNullOrEmpty(data.getBouncedOrPenalXnList()) ? CommonUtils.printFields(data.getBouncedOrPenalXnList().getBouncedOrPenalXns(),null) : " ");
					customerInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getCustomerInfo()) ? CommonUtils.printFields(data.getCustomerInfo(),null) : " ");
					summaryInfo.add(!CommonUtils.isObjectNullOrEmpty(data.getSummaryInfo()) ?CommonUtils.printFields(data.getSummaryInfo(),null) : " ");*/
						
				/**HashMap<String, Object>  bankData = new HashMap<>();
					bankData.put("monthlyDetails", monthlyDetails);
					bankData.put("top5FundReceived", top5FundReceived);
					bankData.put("top5FundTransfered", top5FundTransfered);
					bankData.put("bouncedChequeList", bouncedChequeList);
					bankData.put("customerInfo", customerInfo);
					bankData.put("summaryInfo", summaryInfo);
					bankData.put("bankStatementAnalysis", CommonUtils.printFields(datas, null));
					bankDataDetails.add(bankData);*/
				}
						
				map.put("bankRelatedData" , CommonUtils.printFields(datas, null));
				//map.put("bankStatement", bankStatement);
				/**map.put("monthlyDetails", monthlyDetails);
				map.put("top5FundReceived", top5FundReceived);
				map.put("top5FundTransfered", top5FundTransfered);
				map.put("bouncedChequeList", bouncedChequeList);
				map.put("customerInfo", customerInfo);
				map.put("summaryInfo", summaryInfo);
				map.put("bankStatementAnalysis", CommonUtils.printFields(datas, null));*/
			}
		} catch (Exception e) {
			logger.error("Error while getting perfios data : ",e);
		}
		return map;
	}
	
	public static void copyAddressFromDomainToRequestOfCoApplicant(CoApplicantDetail from, CoApplicantRequest to) {
	    Address address = new Address();
	    address.setPremiseNumber(from.getAddressPremiseName());
	    address.setLandMark(from.getAddressLandmark());
	    address.setStreetName(from.getAddressStreetName());
	    address.setCityId(from.getAddressCity() != null ? from.getAddressCity().longValue() : null);
	    address.setStateId(CommonUtils.isObjectNullOrEmpty(from.getAddressState()) ? null : from.getAddressState().intValue());
	    address.setCountryId(from.getAddressCountry());
	    address.setPincode(from.getAddressPincode() != null ? from.getAddressPincode().longValue() : null);
	    address.setDistrictMappingId(from.getAddressDistrictMappingId() != null ? from.getAddressDistrictMappingId() : null);
	    to.setFirstAddress(address);
	        
	    Address officeAddress = new Address();
	    officeAddress.setPremiseNumber(from.getOfficePremiseNumberName());
	    officeAddress.setLandMark(from.getOfficeLandMark());
	    officeAddress.setStreetName(from.getOfficeStreetName());
	    officeAddress.setCityId(from.getOfficeCityId() != null ? from.getOfficeCityId().longValue() : null);
	    officeAddress.setStateId(from.getOfficeStateId());
	    officeAddress.setCountryId(from.getOfficeCountryId());
	    officeAddress.setPincode(from.getOfficePincode() != null ? from.getOfficePincode().longValue() : null);
	    to.setSecondAddress(officeAddress);
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
				return masterResponse.getValue();
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

}
