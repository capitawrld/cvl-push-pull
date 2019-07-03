package com.capitaworld.service.loans.domain.fundprovider;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the fp_product_master database table.
 * 
 */
@Entity
@Table(name = "fp_product_master")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ProductMaster implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "fp_product_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(name = "fp_name")
	private String fpName;
	
	@Column(name = "product_code")
	private String productCode;

	@Column(name = "product_id")
	private Integer productId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "created_by")
	private Long createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "modified_by")
	private Long modifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_date")
	private Date modifiedDate;
	
	@Column(name = "is_parameter_filled")
	private Boolean isParameterFilled = false;

	@Column(name = "is_active")
	private Boolean isActive = true;
	
	@Column(name = "is_matched")
	private Boolean isMatched = false;

	@Column(name = "user_org_id")
	private Long userOrgId;
	
	@Column(name = "score_model_id")
	private Long scoreModelId;
	
	@Column(name = "purpose_loan_model_id")
	private Long purposeLoanModelId;
	
	@Column(name = "score_model_id_coapp_id")
	private Long scoreModelIdCoAppId;
	
	
	@Column(name = "score_model_id_oth_thn_sal")
	private Long scoreModelIdOthThnSal;
	
	@Column(name = "score_model_id_coApp_id_oth_thn_sal")
	private Long scoreModelIdCoAppIdOthThnSal;
	
	@Column(name = "business_type_id")
	private Long businessTypeId;

	@Column(name = "wc_renewal_status")
	private Integer wcRenewalStatus;
	
	@Column(name="fin_type_id")
	private Integer finId;
	
	@Column(name="campaign_type")
	private Integer campaignCode;
	
	@Column(name="active_inactive_job_id")
    private Long activeInactiveJobId;
	
	@Column(name="action_for")
	private String actionFor;
	
	

	public ProductMaster() {
	}
	
	public ProductMaster(Long id) {
		this.id = id;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCreatedBy() {
		return this.createdBy;
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

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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

	public String getFpName() {
		return fpName;
	}

	public void setFpName(String fpName) {
		this.fpName = fpName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getProductId() {
		return this.productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Boolean getIsParameterFilled() {
		return isParameterFilled;
	}

	public void setIsParameterFilled(Boolean isParameterFilled) {
		this.isParameterFilled = isParameterFilled;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Boolean getIsMatched() {
		return isMatched;
	}

	public void setIsMatched(Boolean isMatched) {
		this.isMatched = isMatched;
	}

	public Long getUserOrgId() {
		return userOrgId;
	}

	public void setUserOrgId(Long userOrgId) {
		this.userOrgId = userOrgId;
	}

	public Long getScoreModelId() {
		return scoreModelId;
	}

	public void setScoreModelId(Long scoreModelId) {
		this.scoreModelId = scoreModelId;
	}

	public Long getBusinessTypeId() {
		return businessTypeId;
	}

	public void setBusinessTypeId(Long businessTypeId) {
		this.businessTypeId = businessTypeId;
	}

	public Integer getWcRenewalStatus() {
		return wcRenewalStatus;
	}

	public void setWcRenewalStatus(Integer wcRenewalStatus) {
		this.wcRenewalStatus = wcRenewalStatus;
	}

	public Integer getFinId() {
		return finId;
	}

	public void setFinId(Integer finId) {
		this.finId = finId;
	}

	public Integer getCampaignCode() {
		return campaignCode;
	}

	public void setCampaignCode(Integer campaignCode) {
		this.campaignCode = campaignCode;
	}

	public Long getActiveInactiveJobId() {
		return activeInactiveJobId;
	}

	public void setActiveInactiveJobId(Long activeInactiveJobId) {
		this.activeInactiveJobId = activeInactiveJobId;
	}

	public String getActionFor() {
		return actionFor;
	}

	public void setActionFor(String actionFor) {
		this.actionFor = actionFor;
	}

	public Long getPurposeLoanModelId() {
		return purposeLoanModelId;
	}

	public void setPurposeLoanModelId(Long purposeLoanModelId) {
		this.purposeLoanModelId = purposeLoanModelId;
	}

	public Long getScoreModelIdCoAppId() {
		return scoreModelIdCoAppId;
	}

	public void setScoreModelIdCoAppId(Long scoreModelIdCoAppId) {
		this.scoreModelIdCoAppId = scoreModelIdCoAppId;
	}

	public Long getScoreModelIdOthThnSal() {
		return scoreModelIdOthThnSal;
	}

	public void setScoreModelIdOthThnSal(Long scoreModelIdOthThnSal) {
		this.scoreModelIdOthThnSal = scoreModelIdOthThnSal;
	}

	public Long getScoreModelIdCoAppIdOthThnSal() {
		return scoreModelIdCoAppIdOthThnSal;
	}

	public void setScoreModelIdCoAppIdOthThnSal(Long scoreModelIdCoAppIdOthThnSal) {
		this.scoreModelIdCoAppIdOthThnSal = scoreModelIdCoAppIdOthThnSal;
	}
}