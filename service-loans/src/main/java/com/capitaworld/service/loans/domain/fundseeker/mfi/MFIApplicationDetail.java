package com.capitaworld.service.loans.domain.fundseeker.mfi;

import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author jaimin.darji
 */
@Entity
@Table(name = "fs_mfi_application_details")
public class MFIApplicationDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "application_id")
    private LoanApplicationMaster applicationId;

    @OneToOne
    @JoinColumn(name = "proposal_mapping_id")
    private ApplicationProposalMapping applicationProposalMapping;

    @Column(name = "aadhar_number")
    private String aadharNumber;

    @Column(name = "name_as_per_aadharCard")
    private String nameAsPerAadharCard;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "gender_id")
    private Integer genderId;

    @Column(name = "marital_status_id")
    private Integer maritalStatusId;

    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "address_same_as_aadhar")
    private Boolean addressSameAsAadhar;

    @Column(name = "address_pincode")
    private String addressPincode;

    @Column(name = "aadhar_address")
    private String aadharAddress;

    @Column(name = "aadhar_pincode")
    private String aadharPincode;

    @Column(name = "address_prof_type")
    private Integer addressProfType;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "mother_name")
    private String motherName;

    @Column(name = "spouse_name")
    private String spouseName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "spouse_birth_date")
    private Date spouseBirthDate;

    @Column(name = "spouse_mobile")
    private String spouseMobile;

    @Column(name = "no_dependent")
    private Integer noDependent;

    @Column(name = "nominee_name")
    private String nomineeName;

    @Column(name = "relation_with_nominee_id")
    private Integer relationWithNomineeId;

    @Column(name = "nominee_address")
    private String nomineeAddress;

    @Column(name = "nominee_pincode")
    private String nomineePincode;

    private Integer religion;

    @Column(name="education_qualification")
    private Integer educationQualification;

    @Column(name="land_holding")
    private Double landHolding;

    @Column(name="name_of_firm")
    private String nameOfFirm;

    @Column(name="business_type")
    private Integer businessType;

    @Column(name="house_type")
    private Integer houseType;

    @Column(name="loan_purpose")
    private Integer loanPurpose;

    @Column(name = "loan_amount_required")
    private Double loanAmountRequired;

    @Column(name = "cost_of_project")
    private Double costOfProject;

    @Column(name = "cost_of_equipment")
    private Double costOfEquipment;

    @Column(name = "working_cap_of_equipment")
    private Double workingCapOfEquipment;

    @Column(name = "total_cost_equipment")
    private Double totalCostEquipment;

    @Column(name = "promoter_contribution")
    private Double promoterContribution;

    @Column(name = "loan_required_from_sidbi")
    private Double loanRequiredFromSidbi;

    @Column(name = "total_mean_finance")
    private Double totalMeanFinance;

    @Column(name = "total_cash_flow")
    private Double totalCashFlow;

    @Column(name="repayment_frequency")
    private Integer repaymentFrequency;

    @Column(name="insurence_required")
    private Boolean insurenceRequired;

    @Column(name="insurence_company_name")
    private String insurenceCompanyName;

    @Column(name="insurence_premium")
    private Double insurencePremium;

    @Column(name = "created_by")
    private Long createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name="loan_type")
    private Integer loanType;

    private Integer type;

    private String remarks;

    @Column(name="is_personal_details_filled")
    private Boolean isPersonalDetailsFilled;

    @Column(name="is_family_details_filled")
    private Boolean isFamilyDetailsFilled;

    @Column(name="is_nominee_details_filled")
    private Boolean isNomineeDetailsFilled;

    @Column(name="is_acadamic_details_filled")
    private Boolean isAcadamicDetailsFilled;

    @Column(name="is_bank_details_filled")
    private Boolean isBankDetailsFilled;

    @Column(name="is_account_details_filled")
    private Boolean isAccountDetailsFilled;

    @Column(name="is_existing_loan_details_filled")
    private Boolean isExistingLoanDetailsFilled;

    @Column(name="is_income_details_filled")
    private Boolean isIncomeDetailsFilled;

    @Column(name="is_family_income_filled")
    private Boolean isFamilyIncomeFilled;

    @Column(name="is_family_expense_filled")
    private Boolean isFamilyExpenseFilled;

    @Column(name="is_expected_income_filled")
    private Boolean isExpectedIncomeFilled;

    @Column(name="is_ppi_filled")
    private Boolean isPPIFilled;

    @Column(name="is_project_details_filled")
    private Boolean isProjectDetailsFilled;

    @Column(name="is_apply_loan_filled")
    private Boolean isApplyLoanFilled;

    @Column(name="is_cost_project_filled")
    private Boolean isCostProjectFilled;

    @Column(name="is_mean_finance_filled")
    private Boolean isMeanFinanceFilled;

    @Column(name="is_cash_flow_details_filled")
    private Boolean isCashFlowDetailsFilled;

    @Column(name="is_assets_details_filled")
    private Boolean isAssetsDetailsFilled;

    @Column(name="is_current_assets_filled")
    private Boolean isCurrentAssetsFilled;

    @Column(name="is_fixed_assets_filled")
    private Boolean isFixedAssetsFilled;

    @Column(name="is_currnt_liability_filled")
    private Boolean isCurrntLiabilityFilled;

    @Column(name="is_repayment_details_filled")
    private Boolean isRepaymentDetailsFilled;

    @Column(name="is_consent_form_filled")
    private Boolean isConsentFormFilled;

    @Column(name="address_proof_type")
    private Integer addressProofType;

    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanApplicationMaster getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(LoanApplicationMaster applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationProposalMapping getApplicationProposalMapping() {
        return applicationProposalMapping;
    }

    public void setApplicationProposalMapping(ApplicationProposalMapping applicationProposalMapping) {
        this.applicationProposalMapping = applicationProposalMapping;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getNameAsPerAadharCard() {
        return nameAsPerAadharCard;
    }

    public void setNameAsPerAadharCard(String nameAsPerAadharCard) {
        this.nameAsPerAadharCard = nameAsPerAadharCard;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getGenderId() {
        return genderId;
    }

    public void setGenderId(Integer genderId) {
        this.genderId = genderId;
    }

    public Integer getMaritalStatusId() {
        return maritalStatusId;
    }

    public void setMaritalStatusId(Integer maritalStatusId) {
        this.maritalStatusId = maritalStatusId;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public Boolean getAddressSameAsAadhar() {
        return addressSameAsAadhar;
    }

    public void setAddressSameAsAadhar(Boolean addressSameAsAadhar) {
        this.addressSameAsAadhar = addressSameAsAadhar;
    }

    public String getAddressPincode() {
        return addressPincode;
    }

    public void setAddressPincode(String addressPincode) {
        this.addressPincode = addressPincode;
    }

    public String getAadharAddress() {
        return aadharAddress;
    }

    public void setAadharAddress(String aadharAddress) {
        this.aadharAddress = aadharAddress;
    }

    public String getAadharPincode() {
        return aadharPincode;
    }

    public void setAadharPincode(String aadharPincode) {
        this.aadharPincode = aadharPincode;
    }

    public Integer getAddressProfType() {
        return addressProfType;
    }

    public void setAddressProfType(Integer addressProfType) {
        this.addressProfType = addressProfType;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public Date getSpouseBirthDate() {
        return spouseBirthDate;
    }

    public void setSpouseBirthDate(Date spouseBirthDate) {
        this.spouseBirthDate = spouseBirthDate;
    }

    public String getSpouseMobile() {
        return spouseMobile;
    }

    public void setSpouseMobile(String spouseMobile) {
        this.spouseMobile = spouseMobile;
    }

    public Integer getNoDependent() {
        return noDependent;
    }

    public void setNoDependent(Integer noDependent) {
        this.noDependent = noDependent;
    }

    public String getNomineeName() {
        return nomineeName;
    }

    public void setNomineeName(String nomineeName) {
        this.nomineeName = nomineeName;
    }

    public Integer getRelationWithNomineeId() {
        return relationWithNomineeId;
    }

    public void setRelationWithNomineeId(Integer relationWithNomineeId) {
        this.relationWithNomineeId = relationWithNomineeId;
    }

    public String getNomineeAddress() {
        return nomineeAddress;
    }

    public void setNomineeAddress(String nomineeAddress) {
        this.nomineeAddress = nomineeAddress;
    }

    public String getNomineePincode() {
        return nomineePincode;
    }

    public void setNomineePincode(String nomineePincode) {
        this.nomineePincode = nomineePincode;
    }

    public Integer getReligion() {
        return religion;
    }

    public void setReligion(Integer religion) {
        this.religion = religion;
    }

    public Integer getEducationQualification() {
        return educationQualification;
    }

    public void setEducationQualification(Integer educationQualification) {
        this.educationQualification = educationQualification;
    }

    public Double getLandHolding() {
        return landHolding;
    }

    public void setLandHolding(Double landHolding) {
        this.landHolding = landHolding;
    }

    public String getNameOfFirm() {
        return nameOfFirm;
    }

    public void setNameOfFirm(String nameOfFirm) {
        this.nameOfFirm = nameOfFirm;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public Integer getHouseType() {
        return houseType;
    }

    public void setHouseType(Integer houseType) {
        this.houseType = houseType;
    }

    public Integer getLoanPurpose() {
        return loanPurpose;
    }

    public void setLoanPurpose(Integer loanPurpose) {
        this.loanPurpose = loanPurpose;
    }

    public Double getLoanAmountRequired() {
        return loanAmountRequired;
    }

    public void setLoanAmountRequired(Double loanAmountRequired) {
        this.loanAmountRequired = loanAmountRequired;
    }

    public Double getCostOfProject() {
        return costOfProject;
    }

    public void setCostOfProject(Double costOfProject) {
        this.costOfProject = costOfProject;
    }

    public Double getCostOfEquipment() {
        return costOfEquipment;
    }

    public void setCostOfEquipment(Double costOfEquipment) {
        this.costOfEquipment = costOfEquipment;
    }

    public Double getWorkingCapOfEquipment() {
        return workingCapOfEquipment;
    }

    public void setWorkingCapOfEquipment(Double workingCapOfEquipment) {
        this.workingCapOfEquipment = workingCapOfEquipment;
    }

    public Double getTotalCostEquipment() {
        return totalCostEquipment;
    }

    public void setTotalCostEquipment(Double totalCostEquipment) {
        this.totalCostEquipment = totalCostEquipment;
    }

    public Double getPromoterContribution() {
        return promoterContribution;
    }

    public void setPromoterContribution(Double promoterContribution) {
        this.promoterContribution = promoterContribution;
    }

    public Double getLoanRequiredFromSidbi() {
        return loanRequiredFromSidbi;
    }

    public void setLoanRequiredFromSidbi(Double loanRequiredFromSidbi) {
        this.loanRequiredFromSidbi = loanRequiredFromSidbi;
    }

    public Double getTotalMeanFinance() {
        return totalMeanFinance;
    }

    public void setTotalMeanFinance(Double totalMeanFinance) {
        this.totalMeanFinance = totalMeanFinance;
    }

    public Double getTotalCashFlow() {
        return totalCashFlow;
    }

    public void setTotalCashFlow(Double totalCashFlow) {
        this.totalCashFlow = totalCashFlow;
    }

    public Integer getRepaymentFrequency() {
        return repaymentFrequency;
    }

    public void setRepaymentFrequency(Integer repaymentFrequency) {
        this.repaymentFrequency = repaymentFrequency;
    }

    public Boolean getInsurenceRequired() {
        return insurenceRequired;
    }

    public void setInsurenceRequired(Boolean insurenceRequired) {
        this.insurenceRequired = insurenceRequired;
    }

    public String getInsurenceCompanyName() {
        return insurenceCompanyName;
    }

    public void setInsurenceCompanyName(String insurenceCompanyName) {
        this.insurenceCompanyName = insurenceCompanyName;
    }

    public Double getInsurencePremium() {
        return insurencePremium;
    }

    public void setInsurencePremium(Double insurencePremium) {
        this.insurencePremium = insurencePremium;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getLoanType() {
        return loanType;
    }

    public void setLoanType(Integer loanType) {
        this.loanType = loanType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getIsPersonalDetailsFilled() {
        return isPersonalDetailsFilled;
    }

    public void setIsPersonalDetailsFilled(Boolean isPersonalDetailsFilled) {
        this.isPersonalDetailsFilled = isPersonalDetailsFilled;
    }

    public Boolean getIsFamilyDetailsFilled() {
        return isFamilyDetailsFilled;
    }

    public void setIsFamilyDetailsFilled(Boolean isFamilyDetailsFilled) {
        this.isFamilyDetailsFilled = isFamilyDetailsFilled;
    }

    public Boolean getIsNomineeDetailsFilled() {
        return isNomineeDetailsFilled;
    }

    public void setIsNomineeDetailsFilled(Boolean isNomineeDetailsFilled) {
        this.isNomineeDetailsFilled = isNomineeDetailsFilled;
    }

    public Boolean getIsAcadamicDetailsFilled() {
        return isAcadamicDetailsFilled;
    }

    public void setIsAcadamicDetailsFilled(Boolean isAcadamicDetailsFilled) {
        this.isAcadamicDetailsFilled = isAcadamicDetailsFilled;
    }

    public Boolean getIsBankDetailsFilled() {
        return isBankDetailsFilled;
    }

    public void setIsBankDetailsFilled(Boolean isBankDetailsFilled) {
        this.isBankDetailsFilled = isBankDetailsFilled;
    }

    public Boolean getIsAccountDetailsFilled() {
        return isAccountDetailsFilled;
    }

    public void setIsAccountDetailsFilled(Boolean isAccountDetailsFilled) {
        this.isAccountDetailsFilled = isAccountDetailsFilled;
    }

    public Boolean getIsExistingLoanDetailsFilled() {
        return isExistingLoanDetailsFilled;
    }

    public void setIsExistingLoanDetailsFilled(Boolean isExistingLoanDetailsFilled) {
        this.isExistingLoanDetailsFilled = isExistingLoanDetailsFilled;
    }

    public Boolean getIsIncomeDetailsFilled() {
        return isIncomeDetailsFilled;
    }

    public void setIsIncomeDetailsFilled(Boolean isIncomeDetailsFilled) {
        this.isIncomeDetailsFilled = isIncomeDetailsFilled;
    }

    public Boolean getIsFamilyIncomeFilled() {
        return isFamilyIncomeFilled;
    }

    public void setIsFamilyIncomeFilled(Boolean isFamilyIncomeFilled) {
        this.isFamilyIncomeFilled = isFamilyIncomeFilled;
    }

    public Boolean getIsFamilyExpenseFilled() {
        return isFamilyExpenseFilled;
    }

    public void setIsFamilyExpenseFilled(Boolean isFamilyExpenseFilled) {
        this.isFamilyExpenseFilled = isFamilyExpenseFilled;
    }

    public Boolean getIsExpectedIncomeFilled() {
        return isExpectedIncomeFilled;
    }

    public void setIsExpectedIncomeFilled(Boolean isExpectedIncomeFilled) {
        this.isExpectedIncomeFilled = isExpectedIncomeFilled;
    }

    public Boolean getIsPPIFilled() {
        return isPPIFilled;
    }

    public void setIsPPIFilled(Boolean isPPIFilled) {
        this.isPPIFilled = isPPIFilled;
    }

    public Boolean getIsProjectDetailsFilled() {
        return isProjectDetailsFilled;
    }

    public void setIsProjectDetailsFilled(Boolean isProjectDetailsFilled) {
        this.isProjectDetailsFilled = isProjectDetailsFilled;
    }

    public Boolean getIsApplyLoanFilled() {
        return isApplyLoanFilled;
    }

    public void setIsApplyLoanFilled(Boolean isApplyLoanFilled) {
        this.isApplyLoanFilled = isApplyLoanFilled;
    }

    public Boolean getIsCostProjectFilled() {
        return isCostProjectFilled;
    }

    public void setIsCostProjectFilled(Boolean isCostProjectFilled) {
        this.isCostProjectFilled = isCostProjectFilled;
    }

    public Boolean getIsMeanFinanceFilled() {
        return isMeanFinanceFilled;
    }

    public void setIsMeanFinanceFilled(Boolean isMeanFinanceFilled) {
        this.isMeanFinanceFilled = isMeanFinanceFilled;
    }

    public Boolean getIsCashFlowDetailsFilled() {
        return isCashFlowDetailsFilled;
    }

    public void setIsCashFlowDetailsFilled(Boolean isCashFlowDetailsFilled) {
        this.isCashFlowDetailsFilled = isCashFlowDetailsFilled;
    }

    public Boolean getIsAssetsDetailsFilled() {
        return isAssetsDetailsFilled;
    }

    public void setIsAssetsDetailsFilled(Boolean isAssetsDetailsFilled) {
        this.isAssetsDetailsFilled = isAssetsDetailsFilled;
    }

    public Boolean getIsCurrentAssetsFilled() {
        return isCurrentAssetsFilled;
    }

    public void setIsCurrentAssetsFilled(Boolean isCurrentAssetsFilled) {
        this.isCurrentAssetsFilled = isCurrentAssetsFilled;
    }

    public Boolean getIsFixedAssetsFilled() {
        return isFixedAssetsFilled;
    }

    public void setIsFixedAssetsFilled(Boolean isFixedAssetsFilled) {
        this.isFixedAssetsFilled = isFixedAssetsFilled;
    }

    public Boolean getIsCurrntLiabilityFilled() {
        return isCurrntLiabilityFilled;
    }

    public void setIsCurrntLiabilityFilled(Boolean isCurrntLiabilityFilled) {
        this.isCurrntLiabilityFilled = isCurrntLiabilityFilled;
    }

    public Boolean getIsRepaymentDetailsFilled() {
        return isRepaymentDetailsFilled;
    }

    public void setIsRepaymentDetailsFilled(Boolean isRepaymentDetailsFilled) {
        this.isRepaymentDetailsFilled = isRepaymentDetailsFilled;
    }

    public Boolean getIsConsentFormFilled() {
        return isConsentFormFilled;
    }

    public void setIsConsentFormFilled(Boolean isConsentFormFilled) {
        this.isConsentFormFilled = isConsentFormFilled;
    }

    public Integer getAddressProofType() {
        return addressProofType;
    }

    public void setAddressProofType(Integer addressProofType) {
        this.addressProofType = addressProofType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MFIApplicantDetail{" +
                "id=" + id +
                ", applicationId=" + applicationId +
                ", applicationProposalMapping=" + applicationProposalMapping +
                ", aadharNumber='" + aadharNumber + '\'' +
                ", nameAsPerAadharCard='" + nameAsPerAadharCard + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", birthDate=" + birthDate +
                ", genderId=" + genderId +
                ", maritalStatusId=" + maritalStatusId +
                ", currentAddress='" + currentAddress + '\'' +
                ", addressSameAsAadhar=" + addressSameAsAadhar +
                ", addressPincode='" + addressPincode + '\'' +
                ", aadharAddress='" + aadharAddress + '\'' +
                ", aadharPincode='" + aadharPincode + '\'' +
                ", addressProfType=" + addressProfType +
                ", fatherName='" + fatherName + '\'' +
                ", motherName='" + motherName + '\'' +
                ", spouseName='" + spouseName + '\'' +
                ", spouseBirthDate=" + spouseBirthDate +
                ", spouseMobile='" + spouseMobile + '\'' +
                ", noDependent=" + noDependent +
                ", nomineeName='" + nomineeName + '\'' +
                ", relationWithNomineeId=" + relationWithNomineeId +
                ", nomineeAddress='" + nomineeAddress + '\'' +
                ", nomineePincode='" + nomineePincode + '\'' +
                ", religion=" + religion +
                ", educationQualification=" + educationQualification +
                ", landHolding=" + landHolding +
                ", nameOfFirm='" + nameOfFirm + '\'' +
                ", businessType=" + businessType +
                ", houseType=" + houseType +
                ", loanPurpose=" + loanPurpose +
                ", loanAmountRequired=" + loanAmountRequired +
                ", costOfProject=" + costOfProject +
                ", costOfEquipment=" + costOfEquipment +
                ", workingCapOfEquipment=" + workingCapOfEquipment +
                ", totalCostEquipment=" + totalCostEquipment +
                ", promoterContribution=" + promoterContribution +
                ", loanRequiredFromSidbi=" + loanRequiredFromSidbi +
                ", totalMeanFinance=" + totalMeanFinance +
                ", totalCashFlow=" + totalCashFlow +
                ", repaymentFrequency=" + repaymentFrequency +
                ", insurenceRequired=" + insurenceRequired +
                ", insurenceCompanyName='" + insurenceCompanyName + '\'' +
                ", insurencePremium=" + insurencePremium +
                ", createdBy=" + createdBy +
                ", createdDate=" + createdDate +
                ", isActive=" + isActive +
                ", modifiedBy=" + modifiedBy +
                ", modifiedDate=" + modifiedDate +
                ", loanType=" + loanType +
                ", type=" + type +
                ", remarks='" + remarks + '\'' +
                ", isPersonalDetailsFilled=" + isPersonalDetailsFilled +
                ", isFamilyDetailsFilled=" + isFamilyDetailsFilled +
                ", isNomineeDetailsFilled=" + isNomineeDetailsFilled +
                ", isAcadamicDetailsFilled=" + isAcadamicDetailsFilled +
                ", isBankDetailsFilled=" + isBankDetailsFilled +
                ", isAccountDetailsFilled=" + isAccountDetailsFilled +
                ", isExistingLoanDetailsFilled=" + isExistingLoanDetailsFilled +
                ", isIncomeDetailsFilled=" + isIncomeDetailsFilled +
                ", isFamilyIncomeFilled=" + isFamilyIncomeFilled +
                ", isFamilyExpenseFilled=" + isFamilyExpenseFilled +
                ", isExpectedIncomeFilled=" + isExpectedIncomeFilled +
                ", isPPIFilled=" + isPPIFilled +
                ", isProjectDetailsFilled=" + isProjectDetailsFilled +
                ", isApplyLoanFilled=" + isApplyLoanFilled +
                ", isCostProjectFilled=" + isCostProjectFilled +
                ", isMeanFinanceFilled=" + isMeanFinanceFilled +
                ", isCashFlowDetailsFilled=" + isCashFlowDetailsFilled +
                ", isAssetsDetailsFilled=" + isAssetsDetailsFilled +
                ", isCurrentAssetsFilled=" + isCurrentAssetsFilled +
                ", isFixedAssetsFilled=" + isFixedAssetsFilled +
                ", isCurrntLiabilityFilled=" + isCurrntLiabilityFilled +
                ", isRepaymentDetailsFilled=" + isRepaymentDetailsFilled +
                ", isConsentFormFilled=" + isConsentFormFilled +
                '}';
    }
}
