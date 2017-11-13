package com.capitaworld.service.loans.model.teaser.finalview;

import com.capitaworld.service.loans.model.*;
import com.capitaworld.service.loans.model.corporate.UnsecuredCoApplicantDetail;
import com.capitaworld.service.loans.model.retail.BankAccountHeldDetailsRequest;
import com.capitaworld.service.loans.model.retail.CreditCardsDetailRequest;
import com.capitaworld.service.loans.model.retail.CreditCardsDetailResponse;
import com.capitaworld.service.loans.model.retail.PastFinancialEstimatesDetailRequest;
import com.capitaworld.service.loans.model.retail.ReferenceRetailDetailsRequest;

import java.io.Serializable;
import java.util.List;

/**
 * Created by garima
 */
public class UnsecuredLoanFinalViewResponse implements Serializable{
    private static final long serialVersionUID = 1L;

    /**
     * Term Loan Primary and Profile Data Fields
     */
    private String organisationName;
    private String aboutUs;
    private String panNo;
    private String websiteAddress;
    private String city;
	private String regOfficeCity;
	private String addOfficeCity;
	private String country;
	private String regOfficecountry;
	private String addOfficecountry;
	private String state;
	private String regOfficestate;
	private String addOfficestate;
    private String constitution;
    private String establishmentMonth;
    private String establishmentYear;
    private String groupName;
    private String keyVericalFunding;
    private String projectBrief;
    private String currencyDenomination;
    private String LoanType;
    private String dateOfProposal;
    private Double tenure;
    private String loanAmount;
    private String landlineNo;
    private String registeredEmailAddress;
    private String registeredContactNumber;
    private Double totalCostOfEstimate;
    private List<?> industrySector;
    private List<Object> profilePic;
    private List<String> shortTermRating;
    private List<String> longTermRating;
    
    private List<UnsecuredCoApplicantDetail> coApplicantList;
    
    /**
     * Term Loan Primary Frame Data Fields
     */
    private Boolean isDprUploaded;
    private List<ProposedProductDetailRequest> proposedProductDetailRequestList;
    private List<AchievementDetailRequest> AchievementDetailList;
    private List<com.capitaworld.service.loans.model.CreditRatingOrganizationDetailResponse> CreditRatingOrganizationDetailResponse;
    private List<OwnershipDetailResponse> ownershipDetailResponseList;
    private List<PromotorBackgroundDetailResponse> promotorBackgroundDetailResponseList;
    private List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequestList;
    private List<FutureFinancialEstimatesDetailRequest> futureFinancialEstimatesDetailRequestList;
    private List<ExistingProductDetailRequest> existingProductDetailRequestList;
    private List<SecurityCorporateDetailRequest> securityCorporateDetailRequestList;
    private List<FinancialArrangementsDetailResponse> financialArrangementsDetailResponseList;
    private List<FinanceMeansDetailResponse> financeMeansDetailResponseList;
    private List<TotalCostOfProjectResponse> totalCostOfProjectResponseList;
    
    private List<BankAccountHeldDetailsRequest> bankAccountHeldDetailsRequest;
	private List<CreditCardsDetailRequest> creditCardsDetailResponse;
	private List<ReferenceRetailDetailsRequest> referenceRetailDetailsRequest;


    /**
     * DPR Data Fields
     */
    private List<BoardOfDirectorsResponse> boardOfDirectorsResponseList ;
    private List<StrategicAlliancesResponse> strategicAlliancesResponseList;
    private List<KeyManagementResponse> keyManagementResponseList;
    private List<EmployeesCategoryBreaksResponse> employeesCategoryBreaksResponseList;
    private List<TechnologyPositioningResponse> technologyPositioningResponseList;
    private List<RevenueAndOrderBookResponse> revenueAndOrderBookResponseList;
    private List<CapacityDetailResponse> capacityDetailResponses;
    private List<AvailabilityProposedPlantDetailResponse> availabilityProposedPlantDetailResponse;
    private List<RequirementsAndAvailabilityRawMaterialsDetailResponse> requirementsAndAvailabilityRawMaterialsDetailResponse;
    private ScotAnalysisDetailResponse scotAnalysisDetailResponses;
    private DprUserDataDetailResponse dprUserDataDetailResponses;
    private DriverForFutureGrowthResponse driverForFutureGrowthResponse;
    private List<ProjectImplementationScheduleResponse> projectImplementationScheduleResponseList;


    /**
     * Term Loan Final Frame Data Fields
     */
    private List<AssociatedConcernDetailRequest> associatedConcernDetailRequests;
    private List<UnsecuredGuarantorDetailRequest> guarantorsCorporateDetailRequestList;
    private List<MonthlyTurnoverDetailRequest> monthlyTurnoverDetailRequestList;

    /**
     * Term Loan Final Uploads Data Fields
     */
    private List<Object> lastAuditedAnnualReportList;
    private List<Object> sanctionLetterCopyList;
    private List<Object> lastITReturnList;
    private List<Object> netWorthStatementOfdirectorsList;
    private List<Object> provisionalFinancialsList;
    private List<Object> panOfDirectorsList;
    private List<Object> detailedListOfShareholdersList;
    private List<Object> photoOfDirectorsList;
    
    //PRIMARY UPLOADS MOVED TO FINAL UPLOADS
    private List<Object> brochureList;
	private List<Object> certificateList;
	private List<Object> panCardList;


    /**
     *Final Term Loan Information
     */
    private String technologyType;
    private String whetherTechnologyIsTied;
    private String technologyPatented;
    private String technologyRequiresUpgradation;
    private String marketPosition;
    private String marketPositioningTop;
    private String marketShareTurnover;
    public String getMajorlyOnGovernment() {
		return majorlyOnGovernment;
	}

	public void setMajorlyOnGovernment(String majorlyOnGovernment) {
		this.majorlyOnGovernment = majorlyOnGovernment;
	}

	public String getBrandAmbassador() {
		return brandAmbassador;
	}

	public void setBrandAmbassador(String brandAmbassador) {
		this.brandAmbassador = brandAmbassador;
	}

	public String getDistributionAndTieUps() {
		return distributionAndTieUps;
	}

	public void setDistributionAndTieUps(String distributionAndTieUps) {
		this.distributionAndTieUps = distributionAndTieUps;
	}

	public String getMarketPositioningProduct() {
		return marketPositioningProduct;
	}

	public void setMarketPositioningProduct(String marketPositioningProduct) {
		this.marketPositioningProduct = marketPositioningProduct;
	}

	public String getMarketingPositioning() {
		return marketingPositioning;
	}

	public void setMarketingPositioning(String marketingPositioning) {
		this.marketingPositioning = marketingPositioning;
	}

	private String overseasNetwork;
    private String indiaDistributionNetwork;
    private String environmentCertification;
    private String isIsoCertified;
    private String accountingSystems;
    private String internalAudit;
    private String competence;
    private String majorlyOnGovernment;
    private String brandAmbassador;
    private String distributionAndTieUps;
    private String marketPositioningProduct;
    private String marketingPositioning;
    private String existingShareHolder;
    private String productServicesPerse;

    /**
     *Working Capital DPR, CMA, BS Formate
     */
    private List<Object> dprList;
    private List<Object> cmaList;
    private List<Object> bsFormatList;
    private List<Object> financialModelList;
    private List<Object> dprYourFormatList;

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getAboutUs() {
        return aboutUs;
    }

    public void setAboutUs(String aboutUs) {
        this.aboutUs = aboutUs;
    }

    public String getPanNo() {
        return panNo;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public String getWebsiteAddress() {
        return websiteAddress;
    }

    public void setWebsiteAddress(String websiteAddress) {
        this.websiteAddress = websiteAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getConstitution() {
        return constitution;
    }

    public void setConstitution(String constitution) {
        this.constitution = constitution;
    }

    public String getEstablishmentMonth() {
        return establishmentMonth;
    }

    public void setEstablishmentMonth(String establishmentMonth) {
        this.establishmentMonth = establishmentMonth;
    }

    public String getEstablishmentYear() {
        return establishmentYear;
    }

    public void setEstablishmentYear(String establishmentYear) {
        this.establishmentYear = establishmentYear;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getKeyVericalFunding() {
        return keyVericalFunding;
    }

    public void setKeyVericalFunding(String keyVericalFunding) {
        this.keyVericalFunding = keyVericalFunding;
    }

    public String getProjectBrief() {
        return projectBrief;
    }

    public void setProjectBrief(String projectBrief) {
        this.projectBrief = projectBrief;
    }

    public String getCurrencyDenomination() {
        return currencyDenomination;
    }

    public void setCurrencyDenomination(String currencyDenomination) {
        this.currencyDenomination = currencyDenomination;
    }

    public String getLoanType() {
        return LoanType;
    }

    public void setLoanType(String loanType) {
        LoanType = loanType;
    }

    public String getDateOfProposal() {
        return dateOfProposal;
    }

    public void setDateOfProposal(String dateOfProposal) {
        this.dateOfProposal = dateOfProposal;
    }

    public Double getTenure() {
        return tenure;
    }

    public void setTenure(Double tenure) {
        this.tenure = tenure;
    }

    public String getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(String loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getLandlineNo() {
        return landlineNo;
    }

    public void setLandlineNo(String landlineNo) {
        this.landlineNo = landlineNo;
    }

    public String getRegisteredEmailAddress() {
        return registeredEmailAddress;
    }

    public void setRegisteredEmailAddress(String registeredEmailAddress) {
        this.registeredEmailAddress = registeredEmailAddress;
    }

    public String getRegisteredContactNumber() {
        return registeredContactNumber;
    }

    public void setRegisteredContactNumber(String registeredContactNumber) {
        this.registeredContactNumber = registeredContactNumber;
    }

    public List<?> getIndustrySector() {
        return industrySector;
    }

    public void setIndustrySector(List<?> industrySector) {
        this.industrySector = industrySector;
    }

    public List<Object> getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(List<Object> profilePic) {
        this.profilePic = profilePic;
    }

    public List<String> getShortTermRating() {
        return shortTermRating;
    }

    public void setShortTermRating(List<String> shortTermRating) {
        this.shortTermRating = shortTermRating;
    }

    public List<String> getLongTermRating() {
        return longTermRating;
    }

    public void setLongTermRating(List<String> longTermRating) {
        this.longTermRating = longTermRating;
    }


    public List<BoardOfDirectorsResponse> getBoardOfDirectorsResponseList() {
        return boardOfDirectorsResponseList;
    }

    public void setBoardOfDirectorsResponseList(List<BoardOfDirectorsResponse> boardOfDirectorsResponseList) {
        this.boardOfDirectorsResponseList = boardOfDirectorsResponseList;
    }

    public List<StrategicAlliancesResponse> getStrategicAlliancesResponseList() {
        return strategicAlliancesResponseList;
    }

    public void setStrategicAlliancesResponseList(List<StrategicAlliancesResponse> strategicAlliancesResponseList) {
        this.strategicAlliancesResponseList = strategicAlliancesResponseList;
    }

    public List<KeyManagementResponse> getKeyManagementResponseList() {
        return keyManagementResponseList;
    }

    public void setKeyManagementResponseList(List<KeyManagementResponse> keyManagementResponseList) {
        this.keyManagementResponseList = keyManagementResponseList;
    }

    public List<EmployeesCategoryBreaksResponse> getEmployeesCategoryBreaksResponseList() {
        return employeesCategoryBreaksResponseList;
    }

    public void setEmployeesCategoryBreaksResponseList(List<EmployeesCategoryBreaksResponse> employeesCategoryBreaksResponseList) {
        this.employeesCategoryBreaksResponseList = employeesCategoryBreaksResponseList;
    }

    public List<TechnologyPositioningResponse> getTechnologyPositioningResponseList() {
        return technologyPositioningResponseList;
    }

    public void setTechnologyPositioningResponseList(List<TechnologyPositioningResponse> technologyPositioningResponseList) {
        this.technologyPositioningResponseList = technologyPositioningResponseList;
    }

    public List<RevenueAndOrderBookResponse> getRevenueAndOrderBookResponseList() {
        return revenueAndOrderBookResponseList;
    }

    public void setRevenueAndOrderBookResponseList(List<RevenueAndOrderBookResponse> revenueAndOrderBookResponseList) {
        this.revenueAndOrderBookResponseList = revenueAndOrderBookResponseList;
    }

    public List<CapacityDetailResponse> getCapacityDetailResponses() {
        return capacityDetailResponses;
    }

    public void setCapacityDetailResponses(List<CapacityDetailResponse> capacityDetailResponses) {
        this.capacityDetailResponses = capacityDetailResponses;
    }

    public List<AvailabilityProposedPlantDetailResponse> getAvailabilityProposedPlantDetailResponse() {
        return availabilityProposedPlantDetailResponse;
    }

    public void setAvailabilityProposedPlantDetailResponse(List<AvailabilityProposedPlantDetailResponse> availabilityProposedPlantDetailResponse) {
        this.availabilityProposedPlantDetailResponse = availabilityProposedPlantDetailResponse;
    }

    public List<RequirementsAndAvailabilityRawMaterialsDetailResponse> getRequirementsAndAvailabilityRawMaterialsDetailResponse() {
        return requirementsAndAvailabilityRawMaterialsDetailResponse;
    }

    public void setRequirementsAndAvailabilityRawMaterialsDetailResponse(List<RequirementsAndAvailabilityRawMaterialsDetailResponse> requirementsAndAvailabilityRawMaterialsDetailResponse) {
        this.requirementsAndAvailabilityRawMaterialsDetailResponse = requirementsAndAvailabilityRawMaterialsDetailResponse;
    }

    public ScotAnalysisDetailResponse getScotAnalysisDetailResponses() {
        return scotAnalysisDetailResponses;
    }

    public void setScotAnalysisDetailResponses(ScotAnalysisDetailResponse scotAnalysisDetailResponses) {
        this.scotAnalysisDetailResponses = scotAnalysisDetailResponses;
    }

    public DprUserDataDetailResponse getDprUserDataDetailResponses() {
        return dprUserDataDetailResponses;
    }

    public void setDprUserDataDetailResponses(DprUserDataDetailResponse dprUserDataDetailResponses) {
        this.dprUserDataDetailResponses = dprUserDataDetailResponses;
    }

    public DriverForFutureGrowthResponse getDriverForFutureGrowthResponse() {
        return driverForFutureGrowthResponse;
    }

    public void setDriverForFutureGrowthResponse(DriverForFutureGrowthResponse driverForFutureGrowthResponse) {
        this.driverForFutureGrowthResponse = driverForFutureGrowthResponse;
    }

    public List<ProjectImplementationScheduleResponse> getProjectImplementationScheduleResponseList() {
        return projectImplementationScheduleResponseList;
    }

    public void setProjectImplementationScheduleResponseList(List<ProjectImplementationScheduleResponse> projectImplementationScheduleResponseList) {
        this.projectImplementationScheduleResponseList = projectImplementationScheduleResponseList;
    }

    
    
    public Boolean getIsDprUploaded() {
		return isDprUploaded;
	}

	public void setIsDprUploaded(Boolean isDprUploaded) {
		this.isDprUploaded = isDprUploaded;
	}

	public List<ProposedProductDetailRequest> getProposedProductDetailRequestList() {
        return proposedProductDetailRequestList;
    }

    public void setProposedProductDetailRequestList(List<ProposedProductDetailRequest> proposedProductDetailRequestList) {
        this.proposedProductDetailRequestList = proposedProductDetailRequestList;
    }

    public List<AchievementDetailRequest> getAchievementDetailList() {
        return AchievementDetailList;
    }

    public void setAchievementDetailList(List<AchievementDetailRequest> achievementDetailList) {
        AchievementDetailList = achievementDetailList;
    }

    public List<com.capitaworld.service.loans.model.CreditRatingOrganizationDetailResponse> getCreditRatingOrganizationDetailResponse() {
        return CreditRatingOrganizationDetailResponse;
    }

    public void setCreditRatingOrganizationDetailResponse(List<com.capitaworld.service.loans.model.CreditRatingOrganizationDetailResponse> creditRatingOrganizationDetailResponse) {
        CreditRatingOrganizationDetailResponse = creditRatingOrganizationDetailResponse;
    }

    public List<OwnershipDetailResponse> getOwnershipDetailResponseList() {
        return ownershipDetailResponseList;
    }

    public void setOwnershipDetailResponseList(List<OwnershipDetailResponse> ownershipDetailResponseList) {
        this.ownershipDetailResponseList = ownershipDetailResponseList;
    }

    public List<PromotorBackgroundDetailResponse> getPromotorBackgroundDetailResponseList() {
        return promotorBackgroundDetailResponseList;
    }

    public void setPromotorBackgroundDetailResponseList(List<PromotorBackgroundDetailResponse> promotorBackgroundDetailResponseList) {
        this.promotorBackgroundDetailResponseList = promotorBackgroundDetailResponseList;
    }

    public List<PastFinancialEstimatesDetailRequest> getPastFinancialEstimatesDetailRequestList() {
        return pastFinancialEstimatesDetailRequestList;
    }

    public void setPastFinancialEstimatesDetailRequestList(List<PastFinancialEstimatesDetailRequest> pastFinancialEstimatesDetailRequestList) {
        this.pastFinancialEstimatesDetailRequestList = pastFinancialEstimatesDetailRequestList;
    }

    public List<FutureFinancialEstimatesDetailRequest> getFutureFinancialEstimatesDetailRequestList() {
        return futureFinancialEstimatesDetailRequestList;
    }

    public void setFutureFinancialEstimatesDetailRequestList(List<FutureFinancialEstimatesDetailRequest> futureFinancialEstimatesDetailRequestList) {
        this.futureFinancialEstimatesDetailRequestList = futureFinancialEstimatesDetailRequestList;
    }

    public List<ExistingProductDetailRequest> getExistingProductDetailRequestList() {
        return existingProductDetailRequestList;
    }

    public void setExistingProductDetailRequestList(List<ExistingProductDetailRequest> existingProductDetailRequestList) {
        this.existingProductDetailRequestList = existingProductDetailRequestList;
    }

    public List<SecurityCorporateDetailRequest> getSecurityCorporateDetailRequestList() {
        return securityCorporateDetailRequestList;
    }

    public void setSecurityCorporateDetailRequestList(List<SecurityCorporateDetailRequest> securityCorporateDetailRequestList) {
        this.securityCorporateDetailRequestList = securityCorporateDetailRequestList;
    }

    public List<FinancialArrangementsDetailResponse> getFinancialArrangementsDetailResponseList() {
        return financialArrangementsDetailResponseList;
    }

    public void setFinancialArrangementsDetailResponseList(List<FinancialArrangementsDetailResponse> financialArrangementsDetailResponseList) {
        this.financialArrangementsDetailResponseList = financialArrangementsDetailResponseList;
    }

    public List<FinanceMeansDetailResponse> getFinanceMeansDetailResponseList() {
        return financeMeansDetailResponseList;
    }

    public void setFinanceMeansDetailResponseList(List<FinanceMeansDetailResponse> financeMeansDetailResponseList) {
        this.financeMeansDetailResponseList = financeMeansDetailResponseList;
    }

    public List<TotalCostOfProjectResponse> getTotalCostOfProjectResponseList() {
        return totalCostOfProjectResponseList;
    }

    public void setTotalCostOfProjectResponseList(List<TotalCostOfProjectResponse> totalCostOfProjectResponseList) {
        this.totalCostOfProjectResponseList = totalCostOfProjectResponseList;
    }

    public List<AssociatedConcernDetailRequest> getAssociatedConcernDetailRequests() {
        return associatedConcernDetailRequests;
    }

    public void setAssociatedConcernDetailRequests(List<AssociatedConcernDetailRequest> associatedConcernDetailRequests) {
        this.associatedConcernDetailRequests = associatedConcernDetailRequests;
    }

    public List<UnsecuredGuarantorDetailRequest> getGuarantorsCorporateDetailRequestList() {
        return guarantorsCorporateDetailRequestList;
    }

    public void setGuarantorsCorporateDetailRequestList(List<UnsecuredGuarantorDetailRequest> guarantorsCorporateDetailRequestList) {
        this.guarantorsCorporateDetailRequestList = guarantorsCorporateDetailRequestList;
    }

    public List<MonthlyTurnoverDetailRequest> getMonthlyTurnoverDetailRequestList() {
        return monthlyTurnoverDetailRequestList;
    }

    public void setMonthlyTurnoverDetailRequestList(List<MonthlyTurnoverDetailRequest> monthlyTurnoverDetailRequestList) {
        this.monthlyTurnoverDetailRequestList = monthlyTurnoverDetailRequestList;
    }

    public List<Object> getLastAuditedAnnualReportList() {
        return lastAuditedAnnualReportList;
    }

    public void setLastAuditedAnnualReportList(List<Object> lastAuditedAnnualReportList) {
        this.lastAuditedAnnualReportList = lastAuditedAnnualReportList;
    }

    public List<Object> getSanctionLetterCopyList() {
        return sanctionLetterCopyList;
    }

    public void setSanctionLetterCopyList(List<Object> sanctionLetterCopyList) {
        this.sanctionLetterCopyList = sanctionLetterCopyList;
    }

    public List<Object> getLastITReturnList() {
        return lastITReturnList;
    }

    public void setLastITReturnList(List<Object> lastITReturnList) {
        this.lastITReturnList = lastITReturnList;
    }

    public List<Object> getNetWorthStatementOfdirectorsList() {
        return netWorthStatementOfdirectorsList;
    }

    public void setNetWorthStatementOfdirectorsList(List<Object> netWorthStatementOfdirectorsList) {
        this.netWorthStatementOfdirectorsList = netWorthStatementOfdirectorsList;
    }

    public List<Object> getProvisionalFinancialsList() {
        return provisionalFinancialsList;
    }

    public void setProvisionalFinancialsList(List<Object> provisionalFinancialsList) {
        this.provisionalFinancialsList = provisionalFinancialsList;
    }

    public List<Object> getPanOfDirectorsList() {
        return panOfDirectorsList;
    }

    public void setPanOfDirectorsList(List<Object> panOfDirectorsList) {
        this.panOfDirectorsList = panOfDirectorsList;
    }

    public List<Object> getDetailedListOfShareholdersList() {
        return detailedListOfShareholdersList;
    }

    public void setDetailedListOfShareholdersList(List<Object> detailedListOfShareholdersList) {
        this.detailedListOfShareholdersList = detailedListOfShareholdersList;
    }

    public List<Object> getPhotoOfDirectorsList() {
        return photoOfDirectorsList;
    }

    public void setPhotoOfDirectorsList(List<Object> photoOfDirectorsList) {
        this.photoOfDirectorsList = photoOfDirectorsList;
    }

    public String getTechnologyType() {
        return technologyType;
    }

    public void setTechnologyType(String technologyType) {
        this.technologyType = technologyType;
    }

    public String getWhetherTechnologyIsTied() {
        return whetherTechnologyIsTied;
    }

    public void setWhetherTechnologyIsTied(String whetherTechnologyIsTied) {
        this.whetherTechnologyIsTied = whetherTechnologyIsTied;
    }

    public String getTechnologyPatented() {
        return technologyPatented;
    }

    public void setTechnologyPatented(String technologyPatented) {
        this.technologyPatented = technologyPatented;
    }

    public String getTechnologyRequiresUpgradation() {
        return technologyRequiresUpgradation;
    }

    public void setTechnologyRequiresUpgradation(String technologyRequiresUpgradation) {
        this.technologyRequiresUpgradation = technologyRequiresUpgradation;
    }

    public String getMarketPosition() {
        return marketPosition;
    }

    public void setMarketPosition(String marketPosition) {
        this.marketPosition = marketPosition;
    }

    public String getMarketPositioningTop() {
        return marketPositioningTop;
    }

    public void setMarketPositioningTop(String marketPositioningTop) {
        this.marketPositioningTop = marketPositioningTop;
    }

    public String getMarketShareTurnover() {
        return marketShareTurnover;
    }

    public void setMarketShareTurnover(String marketShareTurnover) {
        this.marketShareTurnover = marketShareTurnover;
    }

    public String getOverseasNetwork() {
        return overseasNetwork;
    }

    public void setOverseasNetwork(String overseasNetwork) {
        this.overseasNetwork = overseasNetwork;
    }

    public String getIndiaDistributionNetwork() {
        return indiaDistributionNetwork;
    }

    public void setIndiaDistributionNetwork(String indiaDistributionNetwork) {
        this.indiaDistributionNetwork = indiaDistributionNetwork;
    }

    public String getEnvironmentCertification() {
        return environmentCertification;
    }

    public void setEnvironmentCertification(String environmentCertification) {
        this.environmentCertification = environmentCertification;
    }

    public String getIsIsoCertified() {
        return isIsoCertified;
    }

    public void setIsIsoCertified(String isIsoCertified) {
        this.isIsoCertified = isIsoCertified;
    }

    public String getAccountingSystems() {
        return accountingSystems;
    }

    public void setAccountingSystems(String accountingSystems) {
        this.accountingSystems = accountingSystems;
    }

    public String getInternalAudit() {
        return internalAudit;
    }

    public void setInternalAudit(String internalAudit) {
        this.internalAudit = internalAudit;
    }

    public String getCompetence() {
        return competence;
    }

    public void setCompetence(String competence) {
        this.competence = competence;
    }

    public List<Object> getDprList() {
        return dprList;
    }

    public void setDprList(List<Object> dprList) {
        this.dprList = dprList;
    }

    public List<Object> getCmaList() {
        return cmaList;
    }

    public void setCmaList(List<Object> cmaList) {
        this.cmaList = cmaList;
    }

    public List<Object> getBsFormatList() {
        return bsFormatList;
    }

    public void setBsFormatList(List<Object> bsFormatList) {
        this.bsFormatList = bsFormatList;
    }

	public String getExistingShareHolder() {
		return existingShareHolder;
	}

	public void setExistingShareHolder(String existingShareHolder) {
		this.existingShareHolder = existingShareHolder;
	}

	public String getProductServicesPerse() {
		return productServicesPerse;
	}

	public void setProductServicesPerse(String productServicesPerse) {
		this.productServicesPerse = productServicesPerse;
	}

	public Double getTotalCostOfEstimate() {
		return totalCostOfEstimate;
	}

	public void setTotalCostOfEstimate(Double totalCostOfEstimate) {
		this.totalCostOfEstimate = totalCostOfEstimate;
	}

	public List<Object> getFinancialModelList() {
		return financialModelList;
	}

	public void setFinancialModelList(List<Object> financialModelList) {
		this.financialModelList = financialModelList;
	}

	public List<Object> getDprYourFormatList() {
		return dprYourFormatList;
	}

	public void setDprYourFormatList(List<Object> dprYourFormatList) {
		this.dprYourFormatList = dprYourFormatList;
	}

	public String getRegOfficeCity() {
		return regOfficeCity;
	}

	public void setRegOfficeCity(String regOfficeCity) {
		this.regOfficeCity = regOfficeCity;
	}

	public String getAddOfficeCity() {
		return addOfficeCity;
	}

	public void setAddOfficeCity(String addOfficeCity) {
		this.addOfficeCity = addOfficeCity;
	}

	public String getRegOfficecountry() {
		return regOfficecountry;
	}

	public void setRegOfficecountry(String regOfficecountry) {
		this.regOfficecountry = regOfficecountry;
	}

	public String getAddOfficecountry() {
		return addOfficecountry;
	}

	public void setAddOfficecountry(String addOfficecountry) {
		this.addOfficecountry = addOfficecountry;
	}

	public String getRegOfficestate() {
		return regOfficestate;
	}

	public void setRegOfficestate(String regOfficestate) {
		this.regOfficestate = regOfficestate;
	}

	public String getAddOfficestate() {
		return addOfficestate;
	}

	public void setAddOfficestate(String addOfficestate) {
		this.addOfficestate = addOfficestate;
	}

	public List<Object> getBrochureList() {
		return brochureList;
	}

	public void setBrochureList(List<Object> brochureList) {
		this.brochureList = brochureList;
	}

	public List<Object> getCertificateList() {
		return certificateList;
	}

	public void setCertificateList(List<Object> certificateList) {
		this.certificateList = certificateList;
	}

	public List<Object> getPanCardList() {
		return panCardList;
	}

	public void setPanCardList(List<Object> panCardList) {
		this.panCardList = panCardList;
	}

	public List<UnsecuredCoApplicantDetail> getCoApplicantList() {
		return coApplicantList;
	}

	public void setCoApplicantList(List<UnsecuredCoApplicantDetail> coApplicantList) {
		this.coApplicantList = coApplicantList;
	}

	public List<BankAccountHeldDetailsRequest> getBankAccountHeldDetailsRequest() {
		return bankAccountHeldDetailsRequest;
	}

	public void setBankAccountHeldDetailsRequest(List<BankAccountHeldDetailsRequest> bankAccountHeldDetailsRequest) {
		this.bankAccountHeldDetailsRequest = bankAccountHeldDetailsRequest;
	}

	public List<CreditCardsDetailRequest> getCreditCardsDetailResponse() {
		return creditCardsDetailResponse;
	}

	public void setCreditCardsDetailResponse(List<CreditCardsDetailRequest> creditCardsDetailResponse) {
		this.creditCardsDetailResponse = creditCardsDetailResponse;
	}

	public List<ReferenceRetailDetailsRequest> getReferenceRetailDetailsRequest() {
		return referenceRetailDetailsRequest;
	}

	public void setReferenceRetailDetailsRequest(List<ReferenceRetailDetailsRequest> referenceRetailDetailsRequest) {
		this.referenceRetailDetailsRequest = referenceRetailDetailsRequest;
	}
	
	

}
