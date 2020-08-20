
package com.opl.service.loans.domain.fundseeker;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the fs_loan_application_master database table.
 * 
 */
@Entity
@Table(name = "fs_loan_application_master")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQuery(name = "LoanApplicationMaster.findAll", query = "SELECT f FROM LoanApplicationMaster f")
public class LoanApplicationMaster implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "application_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Double amount;

	@Column(name = "category_code")
	private String categoryCode;

	@Column(name = "created_by")
	private Long createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", columnDefinition = "date default sysdate")
	private Date createdDate;

	@Column(name = "is_active")
	private Boolean isActive = true;

	@Column(name = "modified_by")
	private Long modifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_date")
	private Date modifiedDate;

	private String name;

	@Column(name = "application_code")
	private String applicationCode;

	@Column(name = "product_id")
	private Integer productId;

	private Double tenure;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "currency_id")
	private Integer currencyId;

	@Column(name = "denomination_id")
	private Integer denominationId;

	// Common Fields
	@Column(name = "is_applicant_details_filled")
	private Boolean isApplicantDetailsFilled;

	@Column(name = "is_applicant_primary_filled")
	private Boolean isApplicantPrimaryFilled;

	@Column(name = "is_applicant_final_filled")
	private Boolean isApplicantFinalFilled;

	// CoApps Fields
	@Column(name = "is_co_app1_details_filled")
	private Boolean isCoApp1DetailsFilled;

	@Column(name = "is_co_app1_final_filled")
	private Boolean isCoApp1FinalFilled;

	@Column(name = "is_co_app2_details_filled")
	private Boolean isCoApp2DetailsFilled;

	@Column(name = "is_co_app2_final_filled")
	private Boolean isCoApp2FinalFilled;

	// Guarantor Fields
	@Column(name = "is_guarantor1_details_filled")
	private Boolean isGuarantor1DetailsFilled;

	@Column(name = "is_guarantor1_final_filled")
	private Boolean isGuarantor1FinalFilled;

	@Column(name = "is_guarantor2_details_filled")
	private Boolean isGuarantor2DetailsFilled;

	@Column(name = "is_guarantor2_final_filled")
	private Boolean isGuarantor2FinalFilled;

	// Upload Fields
	@Column(name = "is_primary_upload_filled")
	private Boolean isPrimaryUploadFilled;

	@Column(name = "is_final_dpr_upload_filled")
	private Boolean isFinalDprUploadFilled;

	@Column(name = "is_final_upload_filled")
	private Boolean isFinalUploadFilled;

	@Column(name = "is_final_mcq_filled")
	private Boolean isFinalMcqFilled;

	// Locking Fields

	@Column(name = "is_primary_locked")
	private Boolean isPrimaryLocked;

	@Column(name = "is_final_locked")
	private Boolean isFinalLocked;

	// Filled Time

	@Column(name = "details_filled_time")
	private Boolean detailsFilledTime;

	@Column(name = "primary_filled_time")
	private Boolean primaryFilledTime;

	@Column(name = "final_filled_time")
	private Boolean finalFilledTime;

	// Filled Count

	@Column(name = "details_filled_count")
	private String detailsFilledCount;

	@Column(name = "primary_filled_count")
	private String primaryFilledCount;

	@Column(name = "final_filled_count")
	private String finalFilledCount;

	@Column(name = "mca_company_id")
	private String mcaCompanyId;

	@Column(name = "is_mca")
	private Boolean isMca;

	@Column(name = "isMsmeScoreRequired")
	private Boolean isMsmeScoreRequired;

	@Column(name = "campaign_code")
	private String campaignCode;

	@Column(name = "eligible_amnt")
	private Double eligibleAmnt;

	@Column(name = "np_user_id")
	private Long npUserId;

	@Column(name = "np_org_id")
	private Long npOrgId;

	@Column(name = "np_assignee_id")
	private Long npAssigneeId;

	@Column(name = "fp_maker_id")
	private Long fpMakerId;

	@Column(name = "ddr_status_id")
	private Long ddrStatusId;

	// payment related fields

	@Column(name = "type_of_payment")
	private String typeOfPayment;

	@Column(name = "appointment_date")
	private Date appointmentDate;

	@Column(name = "appointment_time")
	private String appointmentTime;

	@Column(name = "payment_amount")
	private Double paymentAmount;
	
	@Column(name = "is_accept_consent")
	private Boolean isAcceptConsent;
	
	@Column(name = "approved_date")
	private Date approvedDate;
	
	@Column(name="payment_status")
	private String paymentStatus;
	
	@Column(name = "business_type_id")
	private Integer businessTypeId;
	
	@Column(name = "company_cin_number")
	private String companyCinNumber;
	
	@Column(name = "wc_renewal_status")
	private Integer wcRenewalStatus;
	
	@Column(name = "is_mcq_skipped")
	private Boolean isMcqSkipped;
	
	@Column(name = "loan_campaign_code")
	private String loanCampaignCode;
	
	@Column(name = "connect_flow_type_id")
	private Integer connectFlowTypeId;

	@Column(name = "profile_mapping_id")
	private Long profileMappingId;

	public Long getFpMakerId() {
		return fpMakerId;
	}

	public void setFpMakerId(Long fpMakerId) {
		this.fpMakerId = fpMakerId;
	}

	/**
	 * @return the paymentStatus
	 */
	public String getPaymentStatus() {
		return paymentStatus;
	}

	/**
	 * @param paymentStatus the paymentStatus to set
	 */
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public Long getNpOrgId() {
		return npOrgId;
	}

	public void setNpOrgId(Long npOrgId) {
		this.npOrgId = npOrgId;
	}

	public Date getApprovedDate() {
		return approvedDate;
	}

	public void setApprovedDate(Date approvedDate) {
		this.approvedDate = approvedDate;
	}

	public Long getDdrStatusId() {
		return ddrStatusId;
	}

	public void setDdrStatusId(Long ddrStatusId) {
		this.ddrStatusId = ddrStatusId;
	}

	public Boolean getIsMsmeScoreRequired() {
		return isMsmeScoreRequired;
	}

	public void setIsMsmeScoreRequired(Boolean isMsmeScoreRequired) {
		this.isMsmeScoreRequired = isMsmeScoreRequired;
	}

	public Boolean getIsMca() {
		return isMca;
	}

	public void setIsMca(Boolean isMca) {
		this.isMca = isMca;
	}

	public String getMcaCompanyId() {
		return mcaCompanyId;
	}

	public void setMcaCompanyId(String mcaCompanyId) {
		this.mcaCompanyId = mcaCompanyId;
	}

	// bi-directional many-to-one association to ApplicationStatusMaster
	@ManyToOne
	@JoinColumn(name = "status")
	private ApplicationStatusMaster applicationStatusMaster;

	public LoanApplicationMaster() {
		// Do nothing because of X and Y.
	}

	public LoanApplicationMaster(Long id) {
		this.id = id;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getAmount() {
		return this.amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Long getModifiedBy() {
		return this.modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedDate() {
		return this.modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Double getTenure() {
		return tenure;
	}

	public void setTenure(Double tenure) {
		this.tenure = tenure;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public ApplicationStatusMaster getApplicationStatusMaster() {
		return this.applicationStatusMaster;
	}

	public void setApplicationStatusMaster(ApplicationStatusMaster applicationStatusMaster) {
		this.applicationStatusMaster = applicationStatusMaster;
	}

	public Integer getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(Integer currencyId) {
		this.currencyId = currencyId;
	}

	public Integer getDenominationId() {
		return denominationId;
	}

	public void setDenominationId(Integer denominationId) {
		this.denominationId = denominationId;
	}

	public Boolean getIsApplicantDetailsFilled() {
		return isApplicantDetailsFilled;
	}

	public void setIsApplicantDetailsFilled(Boolean isApplicantDetailsFilled) {
		this.isApplicantDetailsFilled = isApplicantDetailsFilled;
	}

	public Boolean getIsApplicantPrimaryFilled() {
		return isApplicantPrimaryFilled;
	}

	public void setIsApplicantPrimaryFilled(Boolean isApplicantPrimaryFilled) {
		this.isApplicantPrimaryFilled = isApplicantPrimaryFilled;
	}

	public Boolean getIsApplicantFinalFilled() {
		return isApplicantFinalFilled;
	}

	public void setIsApplicantFinalFilled(Boolean isApplicantFinalFilled) {
		this.isApplicantFinalFilled = isApplicantFinalFilled;
	}

	public Boolean getIsCoApp1DetailsFilled() {
		return isCoApp1DetailsFilled;
	}

	public void setIsCoApp1DetailsFilled(Boolean isCoApp1DetailsFilled) {
		this.isCoApp1DetailsFilled = isCoApp1DetailsFilled;
	}

	public Boolean getIsCoApp1FinalFilled() {
		return isCoApp1FinalFilled;
	}

	public void setIsCoApp1FinalFilled(Boolean isCoApp1FinalFilled) {
		this.isCoApp1FinalFilled = isCoApp1FinalFilled;
	}

	public Boolean getIsCoApp2DetailsFilled() {
		return isCoApp2DetailsFilled;
	}

	public void setIsCoApp2DetailsFilled(Boolean isCoApp2DetailsFilled) {
		this.isCoApp2DetailsFilled = isCoApp2DetailsFilled;
	}

	public Boolean getIsCoApp2FinalFilled() {
		return isCoApp2FinalFilled;
	}

	public void setIsCoApp2FinalFilled(Boolean isCoApp2FinalFilled) {
		this.isCoApp2FinalFilled = isCoApp2FinalFilled;
	}

	public Boolean getIsGuarantor1DetailsFilled() {
		return isGuarantor1DetailsFilled;
	}

	public void setIsGuarantor1DetailsFilled(Boolean isGuarantor1DetailsFilled) {
		this.isGuarantor1DetailsFilled = isGuarantor1DetailsFilled;
	}

	public Boolean getIsGuarantor1FinalFilled() {
		return isGuarantor1FinalFilled;
	}

	public void setIsGuarantor1FinalFilled(Boolean isGuarantor1FinalFilled) {
		this.isGuarantor1FinalFilled = isGuarantor1FinalFilled;
	}

	public Boolean getIsGuarantor2DetailsFilled() {
		return isGuarantor2DetailsFilled;
	}

	public void setIsGuarantor2DetailsFilled(Boolean isGuarantor2DetailsFilled) {
		this.isGuarantor2DetailsFilled = isGuarantor2DetailsFilled;
	}

	public Boolean getIsGuarantor2FinalFilled() {
		return isGuarantor2FinalFilled;
	}

	public void setIsGuarantor2FinalFilled(Boolean isGuarantor2FinalFilled) {
		this.isGuarantor2FinalFilled = isGuarantor2FinalFilled;
	}

	public Boolean getIsPrimaryUploadFilled() {
		return isPrimaryUploadFilled;
	}

	public void setIsPrimaryUploadFilled(Boolean isPrimaryUploadFilled) {
		this.isPrimaryUploadFilled = isPrimaryUploadFilled;
	}

	public Boolean getIsFinalDprUploadFilled() {
		return isFinalDprUploadFilled;
	}

	public void setIsFinalDprUploadFilled(Boolean isFinalDprUploadFilled) {
		this.isFinalDprUploadFilled = isFinalDprUploadFilled;
	}

	public Boolean getIsFinalUploadFilled() {
		return isFinalUploadFilled;
	}

	public void setIsFinalUploadFilled(Boolean isFinalUploadFilled) {
		this.isFinalUploadFilled = isFinalUploadFilled;
	}

	public Boolean getIsFinalMcqFilled() {
		return isFinalMcqFilled;
	}

	public void setIsFinalMcqFilled(Boolean isFinalMcqFilled) {
		this.isFinalMcqFilled = isFinalMcqFilled;
	}

	public Boolean getIsPrimaryLocked() {
		return isPrimaryLocked;
	}

	public void setIsPrimaryLocked(Boolean isPrimaryLocked) {
		this.isPrimaryLocked = isPrimaryLocked;
	}

	public Boolean getIsFinalLocked() {
		return isFinalLocked;
	}

	public void setIsFinalLocked(Boolean isFinalLocked) {
		this.isFinalLocked = isFinalLocked;
	}

	public Boolean getDetailsFilledTime() {
		return detailsFilledTime;
	}

	public void setDetailsFilledTime(Boolean detailsFilledTime) {
		this.detailsFilledTime = detailsFilledTime;
	}

	public Boolean getPrimaryFilledTime() {
		return primaryFilledTime;
	}

	public void setPrimaryFilledTime(Boolean primaryFilledTime) {
		this.primaryFilledTime = primaryFilledTime;
	}

	public Boolean getFinalFilledTime() {
		return finalFilledTime;
	}

	public void setFinalFilledTime(Boolean finalFilledTime) {
		this.finalFilledTime = finalFilledTime;
	}

	public String getPrimaryFilledCount() {
		return primaryFilledCount;
	}

	public void setPrimaryFilledCount(String primaryFilledCount) {
		this.primaryFilledCount = primaryFilledCount;
	}

	public String getApplicationCode() {
		return applicationCode;
	}

	public void setApplicationCode(String applicationCode) {
		this.applicationCode = applicationCode;
	}

	public String getDetailsFilledCount() {
		return detailsFilledCount;
	}

	public void setDetailsFilledCount(String detailsFilledCount) {
		this.detailsFilledCount = detailsFilledCount;
	}

	public String getFinalFilledCount() {
		return finalFilledCount;
	}

	public void setFinalFilledCount(String finalFilledCount) {
		this.finalFilledCount = finalFilledCount;
	}

	public String getCampaignCode() {
		return campaignCode;
	}

	public void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}

	public Double getEligibleAmnt() {
		return eligibleAmnt;
	}

	public void setEligibleAmnt(Double eligibleAmnt) {
		this.eligibleAmnt = eligibleAmnt;
	}

	public Long getNpAssigneeId() {
		return npAssigneeId;
	}

	public void setNpAssigneeId(Long npAssigneeId) {
		this.npAssigneeId = npAssigneeId;
	}

	public Long getNpUserId() {
		return npUserId;
	}

	public void setNpUserId(Long npUserId) {
		this.npUserId = npUserId;
	}
	
	public String getTypeOfPayment() {
		return typeOfPayment;
	}

	public void setTypeOfPayment(String typeOfPayment) {
		this.typeOfPayment = typeOfPayment;
	}

	public Date getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public String getAppointmentTime() {
		return appointmentTime;
	}

	public void setAppointmentTime(String appointmentTime) {
		this.appointmentTime = appointmentTime;
	}

	public Double getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(Double paymentAmount) {
		this.paymentAmount = paymentAmount;
	}
	
	public Boolean getIsAcceptConsent() {
		return isAcceptConsent;
	}

	public void setIsAcceptConsent(Boolean isAcceptConsent) {
		this.isAcceptConsent = isAcceptConsent;
	}
	
	public Integer getBusinessTypeId() {
		return businessTypeId;
	}

	public void setBusinessTypeId(Integer businessTypeId) {
		this.businessTypeId = businessTypeId;
	}
	
	

	/**
	 * @return the companyCinNumber
	 */
	public String getCompanyCinNumber() {
		return companyCinNumber;
	}

	/**
	 * @param companyCinNumber the companyCinNumber to set
	 */
	public void setCompanyCinNumber(String companyCinNumber) {
		this.companyCinNumber = companyCinNumber;
	}

	
	
	public Integer getWcRenewalStatus() {
		return wcRenewalStatus;
	}

	public void setWcRenewalStatus(Integer wcRenewalStatus) {
		this.wcRenewalStatus = wcRenewalStatus;
	}

	public Boolean getIsMcqSkipped() {
		return isMcqSkipped;
	}

	public void setIsMcqSkipped(Boolean isMcqSkipped) {
		this.isMcqSkipped = isMcqSkipped;
	}
	
	@Override
	public String toString() {
		return "LoanApplicationMaster [id=" + id + "]";
	}

	public String getLoanCampaignCode() {
		return loanCampaignCode;
	}

	public void setLoanCampaignCode(String loanCampaignCode) {
		this.loanCampaignCode = loanCampaignCode;
	}

	public Integer getConnectFlowTypeId() {
		return connectFlowTypeId;
	}

	public void setConnectFlowTypeId(Integer connectFlowTypeId) {
		this.connectFlowTypeId = connectFlowTypeId;
	}

	public Long getProfileMappingId() {
		return profileMappingId;
	}

	public void setProfileMappingId(Long profileMappingId) {
		this.profileMappingId = profileMappingId;
	}
	

}