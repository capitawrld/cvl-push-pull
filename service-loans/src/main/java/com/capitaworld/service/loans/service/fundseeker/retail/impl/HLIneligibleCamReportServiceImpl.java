package com.capitaworld.service.loans.service.fundseeker.retail.impl;

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

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.client.reports.ReportsClient;
import com.capitaworld.connect.api.ConnectStage;
import com.capitaworld.service.analyzer.client.AnalyzerClient;
import com.capitaworld.service.analyzer.model.common.AnalyzerResponse;
import com.capitaworld.service.analyzer.model.common.Data;
import com.capitaworld.service.analyzer.model.common.ReportRequest;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.retail.BankingRelation;
import com.capitaworld.service.loans.domain.fundseeker.retail.CoApplicantDetail;
import com.capitaworld.service.loans.model.Address;
import com.capitaworld.service.loans.model.FinancialArrangementDetailResponseString;
import com.capitaworld.service.loans.model.FinancialArrangementsDetailRequest;
import com.capitaworld.service.loans.model.retail.BankRelationshipRequest;
import com.capitaworld.service.loans.model.retail.CoApplicantRequest;
import com.capitaworld.service.loans.model.retail.HLOneformPrimaryRes;
import com.capitaworld.service.loans.model.retail.PLRetailApplicantRequest;
import com.capitaworld.service.loans.model.retail.RetailApplicantIncomeRequest;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.BankingRelationlRepository;
import com.capitaworld.service.loans.service.common.PincodeDateService;
import com.capitaworld.service.loans.service.fundseeker.corporate.FinancialArrangementDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.impl.CamReportPdfDetailsServiceImpl;
import com.capitaworld.service.loans.service.fundseeker.retail.CoApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.HLIneligibleCamReportService;
import com.capitaworld.service.loans.service.fundseeker.retail.PlRetailApplicantService;
import com.capitaworld.service.loans.service.fundseeker.retail.PrimaryHomeLoanService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantIncomeService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.matchengine.ProposalDetailsClient;
import com.capitaworld.service.matchengine.model.ProposalMappingRequestString;
import com.capitaworld.service.matchengine.model.ProposalMappingResponse;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.CastCategory;
import com.capitaworld.service.oneform.enums.DesignationList;
import com.capitaworld.service.oneform.enums.EducationStatusRetailMst;
import com.capitaworld.service.oneform.enums.EmploymentStatusRetailMst;
import com.capitaworld.service.oneform.enums.EmploymentWithPL;
import com.capitaworld.service.oneform.enums.EmploymentWithRetail;
import com.capitaworld.service.oneform.enums.Gender;
import com.capitaworld.service.oneform.enums.HomeLoanPurpose;
import com.capitaworld.service.oneform.enums.MaritalStatusMst;
import com.capitaworld.service.oneform.enums.OccupationHL;
import com.capitaworld.service.oneform.enums.OccupationNature;
import com.capitaworld.service.oneform.enums.RelationshipTypeHL;
import com.capitaworld.service.oneform.enums.ResidenceTypeHomeLoan;
import com.capitaworld.service.oneform.enums.ResidentStatusMst;
import com.capitaworld.service.oneform.enums.SpouseEmploymentList;
import com.capitaworld.service.oneform.enums.Title;
import com.capitaworld.service.oneform.enums.WcRenewalType;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;
import com.capitaworld.service.oneform.model.SectorIndustryModel;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class HLIneligibleCamReportServiceImpl implements HLIneligibleCamReportService{

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;
	
	@Autowired
	private PlRetailApplicantService plRetailApplicantService;
	
	@Autowired
	private OneFormClient oneFormClient;
	
	@Autowired 
	private PincodeDateService pincodeDateService;
	
	@Autowired
	private AnalyzerClient analyzerClient;
	
	@Autowired
	private CoApplicantService coApplicantService;
	
	@Autowired
	private PrimaryHomeLoanService primaryHomeLoanService;
	
	@Autowired
	private BankingRelationlRepository bankingRelationlRepository;
	
	@Autowired
	private ProposalDetailsClient proposalDetailsClient;
	
	@Autowired 
	private RetailApplicantIncomeService retailApplicantIncomeService;
	
	@Autowired
	private FinancialArrangementDetailsService financialArrangementDetailsService;

	@Autowired
	private ReportsClient reportsClient;
	
	@Autowired
	private HLIneligibleCamReportService hlIneligibleCamReportService;
	
	private static final Logger logger = LoggerFactory.getLogger(CamReportPdfDetailsServiceImpl.class);
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	private static final String HLINELIGIBLE_CAM_REPORT = "INELIGIBLEHLCAM";
	
	@Override
	public Map<String, Object> getHLInEligibleCamReport(Long applicationId){
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		Long userId = loanApplicationRepository.getUserIdByApplicationId(applicationId);
     	LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.getByIdAndUserId(applicationId, userId);
     	
     	if(loanApplicationMaster != null) {
     		map.put("applicationType", (loanApplicationMaster.getWcRenewalStatus() != null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue() : "New" ));
     		map.put("dateOfProposal", !CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getCreatedDate())? simpleDateFormat.format(loanApplicationMaster.getCreatedDate()):"-");
     		map.put("applicationCode", loanApplicationMaster.getApplicationCode() != null ? loanApplicationMaster.getApplicationCode() : "-");
     		map.put("loanType", !CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getProductId()) ? CommonUtils.LoanType.getType(loanApplicationMaster.getProductId()).getName() : " ");
     	}
     	
		try {
			PLRetailApplicantRequest plRetailApplicantRequest = plRetailApplicantService.getPrimary(userId, applicationId);
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
				plRetailApplicantRequest.setResidenceSinceYear(years);
			}
			
			String operatingBusinessSince = null;
			if(plRetailApplicantRequest.getBusinessStartDate() != null ) {
				LocalDate operatingBusinessDiff = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(plRetailApplicantRequest.getBusinessStartDate()));
				operatingBusinessSince = (today.getYear() - operatingBusinessDiff.getYear()) + " years";
			}
			
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
			
			map.put("employmentType", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getEmploymentType()) ? OccupationNature.getById(plRetailApplicantRequest.getEmploymentType()).getValue() : "-");
			map.put("employmentStatus", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getCurrentEmploymentStatus()) ?EmploymentStatusRetailMst.getById(plRetailApplicantRequest.getCurrentEmploymentStatus()).getValue() : "-");
			map.put("sinceSalaryWhen", (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getSalaryBankYear()) ? plRetailApplicantRequest.getSalaryBankYear() + " years" : "")+" "+(plRetailApplicantRequest.getSalaryBankMonth() != null ? plRetailApplicantRequest.getSalaryBankMonth() +" months" : ""));
			map.put("retailApplicantProfile", CommonUtils.printFields(plRetailApplicantRequest, null));
			map.put("educationQualification", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getEducationQualification()) ? EducationStatusRetailMst.getById(plRetailApplicantRequest.getEducationQualification()).getValue() : "-");
			map.put("maritalStatus", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getStatusId()) ? MaritalStatusMst.getById(plRetailApplicantRequest.getStatusId()).getValue() : "-");
			map.put("residenceType", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getResidenceType()) ? ResidenceTypeHomeLoan.getById(plRetailApplicantRequest.getResidenceType()).getValue() : "-");
			map.put("spouseEmployment", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getSpouseEmployment()) ? SpouseEmploymentList.getById(plRetailApplicantRequest.getSpouseEmployment()).getValue() : "-");
			map.put("designation", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getDesignation())? DesignationList.getById(plRetailApplicantRequest.getDesignation()).getValue() : "-");
			map.put("noOfDependent", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getNoOfDependent()) ? plRetailApplicantRequest.getNoOfDependent() : "-");
			map.put("nationality", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getResidentialStatus()) ? ResidentStatusMst.getById(plRetailApplicantRequest.getResidentialStatus()).getValue() : "-");
			map.put("annualIncomeOfSpouse", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getAnnualIncomeOfSpouse()) ? CommonUtils.convertValueWithoutDecimal(plRetailApplicantRequest.getAnnualIncomeOfSpouse()) : "-");
			map.put("applicantNetWorth", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getNetworth()) ? CommonUtils.convertValueWithoutDecimal(plRetailApplicantRequest.getNetworth()) : "-");
			map.put("grossMonthlyIncome", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getGrossMonthlyIncome()) ? CommonUtils.convertValueWithoutDecimal(plRetailApplicantRequest.getGrossMonthlyIncome()) : null);
			map.put("netMonthlyIncome", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getMonthlyIncome()) ? CommonUtils.convertValueWithoutDecimal(plRetailApplicantRequest.getMonthlyIncome()) : null);
			map.put("residenceSinceYearMonths", (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getResidenceSinceYear()) ? plRetailApplicantRequest.getResidenceSinceYear() + " years" : "")+ " " +(plRetailApplicantRequest.getResidenceSinceMonth() != null ? plRetailApplicantRequest.getResidenceSinceMonth()+" months":""));
			map.put("eligibleLoanAmount", !CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getAmount()) ? CommonUtils.convertValueWithoutDecimal(loanApplicationMaster.getAmount()): "-");
			map.put("eligibleTenure", !CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getTenure()) ? loanApplicationMaster.getTenure().longValue():"-");
			map.put("operatingBusinessSince", !CommonUtils.isObjectNullOrEmpty(operatingBusinessSince) ? operatingBusinessSince :"-");
			map.put("applicantCategory", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getCategory()) ? CastCategory.getById(plRetailApplicantRequest.getCategory()).getValue() : "-");
			map.put("experienceInPresentJob", (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getCurrentJobYear()) ? plRetailApplicantRequest.getCurrentJobYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getCurrentJobMonth()) ? plRetailApplicantRequest.getCurrentJobMonth() +" months" : ""));
			map.put("totalExperience", (!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getTotalExperienceYear()) ? plRetailApplicantRequest.getTotalExperienceYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getTotalExperienceMonth()) ? plRetailApplicantRequest.getTotalExperienceMonth() +" months" : ""));
			
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
				
				/*employment With*/
				if(coApplicantDetail.getEmploymentType()!= null && coApplicantDetail.getEmploymentType() == 2) {
					coApp.put("employmentWith" , coApplicantDetail.getEmployedWithId() != null ? EmploymentWithPL.getById(coApplicantDetail.getEmployedWithId()).getValue() : "-");
				}else if (coApplicantDetail.getEmploymentType()!= null && coApplicantDetail.getEmploymentType() == 5) {
					coApp.put("employmentWith" , coApplicantDetail.getEmployedWithId() != null ? OccupationHL.getById(coApplicantDetail.getEmployedWithId()).getValue() : "-");
				}else if (coApplicantDetail.getEmploymentType()!= null && coApplicantDetail.getEmploymentType() == 4) {
					coApp.put("employmentWith" , coApplicantDetail.getEmployedWithId() != null ? EmploymentWithRetail.getById(coApplicantDetail.getEmployedWithId()).getValue() : "-");
				}
				
				coApp.put("employmentStatus", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getCurrentEmploymentStatus()) ? EmploymentStatusRetailMst.getById(coApplicantDetail.getCurrentEmploymentStatus()).getValue() : "-");
				coApp.put("relationshipWithApp", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getRelationshipWithApplicant()) ? RelationshipTypeHL.getById(coApplicantDetail.getRelationshipWithApplicant()).getValue() : "-");
				coApp.put("maritalStatus", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getStatusId()) ? MaritalStatusMst.getById(coApplicantDetail.getStatusId()).getValue() : "-");
				coApp.put("nameOfEmployer", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getNameOfEmployer()) ? coApplicantDetail.getNameOfEmployer() : "-");
				coApp.put("spouseEmployment", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getSpouseEmployment()) ? SpouseEmploymentList.getById(coApplicantDetail.getSpouseEmployment()).getValue() : "-");
				coApp.put("annualIncomeOfSpouse", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getAnnualIncomeOfSpouse()) ? CommonUtils.convertValueWithoutDecimal(coApplicantDetail.getAnnualIncomeOfSpouse()) : "-");
				coApp.put("residenceType", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getResidenceType()) ? ResidenceTypeHomeLoan.getById(coApplicantDetail.getResidenceType()).getValue() : "-");
				coApp.put("noOfDependent", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getNoDependent()) ? coApplicantDetail.getNoDependent() : "-");
				coApp.put("designation", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getDesignation()) ? DesignationList.getById(coApplicantDetail.getDesignation()).getValue() : "-");
				coApp.put("educationQualification", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getEducationQualification()) ? EducationStatusRetailMst.getById(coApplicantDetail.getEducationQualification()).getValue() : "-");
				coApp.put("coApplicantNetWorth", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getNetworth()) ? CommonUtils.convertValueWithoutDecimal(coApplicantDetail.getNetworth()) : null);
				coApp.put("nationality", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getNationality()) ? ResidentStatusMst.getById(coApplicantDetail.getNationality()).getValue() : "-");
				coApp.put("grossMonthlyIncome", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getGrossMonthlyIncome()) ? CommonUtils.convertValueWithoutDecimal(coApplicantDetail.getGrossMonthlyIncome()) : null);
				coApp.put("netMonthlyIncome", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getMonthlyIncome()) ? CommonUtils.convertValueWithoutDecimal(coApplicantDetail.getMonthlyIncome()) : null);
				coApp.put("operatingBusinessSince", !CommonUtils.isObjectNullOrEmpty(operatingBusinessSince) ? operatingBusinessSince : "-");
				coApp.put("coApplicantCategory", !CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getCategory()) ? CastCategory.getById(coApplicantDetail.getCategory()).getValue() : "-");
				coApp.put("experienceInPresentJob", (!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getCurrentJobYear()) ? coApplicantDetail.getCurrentJobYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getCurrentJobMonth()) ? coApplicantDetail.getCurrentJobMonth() +" months" : ""));
				coApp.put("totalExperience", (!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getTotalExperienceYear()) ? coApplicantDetail.getTotalExperienceYear() + " years" :"")+" "+(!CommonUtils.isObjectNullOrEmpty(coApplicantDetail.getTotalExperienceMonth()) ? coApplicantDetail.getTotalExperienceMonth() +" months" : ""));
				coApp.put("retailCoApplicantProfile", CommonUtils.printFields(coApplicantRequest, null));
				listMap.add(coApp);		
			}
			map.put("retailCoApplicantDetails", CommonUtils.printFields(listMap, null));
		} catch (Exception e) {
			logger.error("Error while getting profile Details : ",e);
		}		
		
		//PROPOSAL RESPONSE
		try {
			ObjectMapper mapper = new ObjectMapper();
			ProposalMappingResponse proposalMappingResponse= proposalDetailsClient.getActiveProposalByApplicationID(applicationId);
			
			ProposalMappingRequestString proposalMappingRequestString = mapper.convertValue(proposalMappingResponse.getData(), ProposalMappingRequestString.class);
			
			map.put("proposalDate", simpleDateFormat.format(proposalMappingRequestString.getModifiedDate()));
			map.put("proposalResponseEmi", !CommonUtils.isObjectNullOrEmpty(proposalMappingResponse.getData()) ? CommonUtils.convertValueWithoutDecimal((Double)((LinkedHashMap<String, Object>)proposalMappingResponse.getData()).get("emi")) : "");
			map.put("proposalResponse", !CommonUtils.isObjectNullOrEmpty(proposalMappingResponse.getData()) ? proposalMappingResponse.getData() : " ");
		}
		catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		//PRIMARY DATA (LOAN DETAILS)
		try {
			PLRetailApplicantRequest plRetailApplicantRequest = plRetailApplicantService.getPrimary(userId, applicationId);
			map.put("loanPurpose", !CommonUtils.isObjectNullOrEmpty(plRetailApplicantRequest.getLoanPurpose()) ? StringEscapeUtils.escapeXml(HomeLoanPurpose.getById(plRetailApplicantRequest.getLoanPurpose()).getValue()): "");
			map.put("retailApplicantPrimaryDetails", plRetailApplicantRequest);
		} catch (Exception e) {
			logger.error("Error while getting primary Details : ",e);
		}
		
		//Property Details
		try {
			Map<String ,Object> propertyDetails = new HashMap<String, Object>(); 
			HLOneformPrimaryRes response = primaryHomeLoanService.getOneformPrimaryDetails(applicationId);
			propertyDetails.put("propertyValue",response.getMarketValProp() != null ? CommonUtils.convertValueWithoutDecimal(response.getMarketValProp()) : "");
			propertyDetails.put("propertyAge", response.getOldPropYear() != null ? response.getOldPropYear() : "");
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
			map.put("propertyDetails", propertyDetails);
		} catch (Exception e) {
			logger.error("Error while getting property Details : ",e);
		}

		//INCOME DETAILS - NET INCOME
		try {
			List<RetailApplicantIncomeRequest> retailApplicantIncomeDetail = retailApplicantIncomeService.getAll(applicationId);
			
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
		
		//PERFIOS API DATA (BANK STATEMENT ANALYSIS)
		ReportRequest reportRequest = new ReportRequest();
		reportRequest.setApplicationId(applicationId);
		reportRequest.setUserId(userId);
		List<Data> datas = new ArrayList<>();
		//List<Object> bankStatement = new ArrayList<Object>();

		try {
			AnalyzerResponse analyzerResponse = analyzerClient.getDetailsFromReportForCam(reportRequest);
			List<HashMap<String, Object>> listhashMap = (List<HashMap<String, Object>>) analyzerResponse.getData();
			//List<HashMap<String, Object>> bankDataDetails = new ArrayList<HashMap<String,Object>>(); 

			if (!CommonUtils.isListNullOrEmpty(listhashMap)) {	
				for (HashMap<String, Object> rec : listhashMap) {
					Data data = MultipleJSONObjectHelper.getObjectFromMap(rec, Data.class);
					datas.add(data);
							
					//bankStatement.add(!CommonUtils.isObjectNullOrEmpty(data.getXns()) ? CommonUtils.printFields(data.getXns().getXn(),null) : " ");
				}
						
				map.put("bankRelatedData" , CommonUtils.printFields(datas, null));
				//map.put("bankStatement", bankStatement);
		
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

	/* (non-Javadoc)
	 * @see com.capitaworld.service.loans.service.fundseeker.retail.HLIneligibleCamReportService#generateIneligibleCamReportFromMap(java.lang.Long)
	 */
	@Override
	public byte[] generateIneligibleCamReportFromMap(Long applicationId) {
		Map<String,Object> response =  hlIneligibleCamReportService.getHLInEligibleCamReport(applicationId);
		com.capitaworld.api.reports.ReportRequest reportRequest = new com.capitaworld.api.reports.ReportRequest();
		reportRequest.setParams(response);
		reportRequest.setTemplate(HLINELIGIBLE_CAM_REPORT);
		reportRequest.setType(HLINELIGIBLE_CAM_REPORT);
		return reportsClient.generatePDFFile(reportRequest);
	}
}
