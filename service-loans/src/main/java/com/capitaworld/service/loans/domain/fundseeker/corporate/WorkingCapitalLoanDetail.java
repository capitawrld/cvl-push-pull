package com.capitaworld.service.loans.domain.fundseeker.corporate;

import java.io.Serializable;
import javax.persistence.*;

import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;

import java.util.Date;



/**
 * The persistent class for the fs_corporate_working_capital_loan_details database table.
 * 
 */
@Entity
@Table(name="fs_corporate_working_capital_loan_details")
@DiscriminatorValue("2")//2 for working capital

public class WorkingCapitalLoanDetail extends LoanApplicationMaster implements Serializable {
	private static final long serialVersionUID = 1L;

	
	@Column(name="accounting_systems_id")
	private Integer accountingSystemsId;

	@Column(name="brand_ambassador_id")
	private Integer brandAmbassadorId;

	@Column(name="competence_id")
	private Integer competenceId;

	@Column(name="created_by")
	private Long createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_date")
	private Date createdDate;

	@Column(name="credit_rating_id")
	private Long creditRatingId;

	@Column(name="currency_id")
	private Integer currencyId;

	@Column(name="denomination_id")
	private Integer denominationId;

	@Column(name="description_enhancement_limit")
	private String descriptionEnhancementLimit;

	@Column(name="distribution_and_marketing_tie_ups_id")
	private Integer distributionAndMarketingTieUpsId;

	@Column(name="environment_certification_id")
	private Integer environmentCertificationId;

	@Column(name="existing_share_holders_id")
	private Integer existingShareHoldersId;

	@Column(name="have_existing_limit")
	private String haveExistingLimit;

	@Column(name="india_distribution_network_id")
	private Integer indiaDistributionNetworkId;

	@Column(name="Integerernal_audit_id")
	private Integer IntegerernalAuditId;

	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_depends_majorly_on_government")
	private Boolean isDependsMajorlyOnGovernment;

	@Column(name="is_iso_certified")
	private Boolean isIsoCertified;

	@Column(name="market_position_id")
	private Integer marketPositionId;

	@Column(name="market_positioning_top_id")
	private Integer marketPositioningTopId;

	@Column(name="market_share_turnover_id")
	private Integer marketShareTurnoverId;

	@Column(name="marketing_positioning_id")
	private Integer marketingPositioningId;

	@Column(name="modified_by")
	private Long modifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="modified_date")
	private Date modifiedDate;

	@Column(name="product_services_perse_id")
	private Integer productServicesPerseId;

	@Lob
	@Column(name="project_brief")
	private String projectBrief;

	@Column(name="technology_patented_id")
	private Integer technologyPatentedId;

	@Column(name="technology_requires_upgradation_id")
	private Integer technologyRequiresUpgradationId;

	@Column(name="type_of_technology_corporate_id")
	private Boolean typeOfTechnologyCorporateId;

	@Column(name="is_technology_tied")
	private Boolean is_technology_tied;

	public WorkingCapitalLoanDetail() {
	}

	
	public Integer getAccountingSystemsId() {
		return this.accountingSystemsId;
	}

	public void setAccountingSystemsId(Integer accountingSystemsId) {
		this.accountingSystemsId = accountingSystemsId;
	}

	public Integer getBrandAmbassadorId() {
		return this.brandAmbassadorId;
	}

	public void setBrandAmbassadorId(Integer brandAmbassadorId) {
		this.brandAmbassadorId = brandAmbassadorId;
	}

	public Integer getCompetenceId() {
		return this.competenceId;
	}

	public void setCompetenceId(Integer competenceId) {
		this.competenceId = competenceId;
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

	public Long getCreditRatingId() {
		return this.creditRatingId;
	}

	public void setCreditRatingId(Long creditRatingId) {
		this.creditRatingId = creditRatingId;
	}

	public Integer getCurrencyId() {
		return this.currencyId;
	}

	public void setCurrencyId(Integer currencyId) {
		this.currencyId = currencyId;
	}

	public Integer getDenominationId() {
		return this.denominationId;
	}

	public void setDenominationId(Integer denominationId) {
		this.denominationId = denominationId;
	}

	public String getDescriptionEnhancementLimit() {
		return this.descriptionEnhancementLimit;
	}

	public void setDescriptionEnhancementLimit(String descriptionEnhancementLimit) {
		this.descriptionEnhancementLimit = descriptionEnhancementLimit;
	}

	public Integer getDistributionAndMarketingTieUpsId() {
		return this.distributionAndMarketingTieUpsId;
	}

	public void setDistributionAndMarketingTieUpsId(Integer distributionAndMarketingTieUpsId) {
		this.distributionAndMarketingTieUpsId = distributionAndMarketingTieUpsId;
	}

	public Integer getEnvironmentCertificationId() {
		return this.environmentCertificationId;
	}

	public void setEnvironmentCertificationId(Integer environmentCertificationId) {
		this.environmentCertificationId = environmentCertificationId;
	}

	public Integer getExistingShareHoldersId() {
		return this.existingShareHoldersId;
	}

	public void setExistingShareHoldersId(Integer existingShareHoldersId) {
		this.existingShareHoldersId = existingShareHoldersId;
	}

	public String getHaveExistingLimit() {
		return this.haveExistingLimit;
	}

	public void setHaveExistingLimit(String haveExistingLimit) {
		this.haveExistingLimit = haveExistingLimit;
	}

	public Integer getIndiaDistributionNetworkId() {
		return this.indiaDistributionNetworkId;
	}

	public void setIndiaDistributionNetworkId(Integer indiaDistributionNetworkId) {
		this.indiaDistributionNetworkId = indiaDistributionNetworkId;
	}

	public Integer getIntegerernalAuditId() {
		return this.IntegerernalAuditId;
	}

	public void setIntegerernalAuditId(Integer IntegerernalAuditId) {
		this.IntegerernalAuditId = IntegerernalAuditId;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsDependsMajorlyOnGovernment() {
		return this.isDependsMajorlyOnGovernment;
	}

	public void setIsDependsMajorlyOnGovernment(Boolean isDependsMajorlyOnGovernment) {
		this.isDependsMajorlyOnGovernment = isDependsMajorlyOnGovernment;
	}

	public Boolean getIsIsoCertified() {
		return this.isIsoCertified;
	}

	public void setIsIsoCertified(Boolean isIsoCertified) {
		this.isIsoCertified = isIsoCertified;
	}

	public Integer getMarketPositionId() {
		return this.marketPositionId;
	}

	public void setMarketPositionId(Integer marketPositionId) {
		this.marketPositionId = marketPositionId;
	}

	public Integer getMarketPositioningTopId() {
		return this.marketPositioningTopId;
	}

	public void setMarketPositioningTopId(Integer marketPositioningTopId) {
		this.marketPositioningTopId = marketPositioningTopId;
	}

	public Integer getMarketShareTurnoverId() {
		return this.marketShareTurnoverId;
	}

	public void setMarketShareTurnoverId(Integer marketShareTurnoverId) {
		this.marketShareTurnoverId = marketShareTurnoverId;
	}

	public Integer getMarketingPositioningId() {
		return this.marketingPositioningId;
	}

	public void setMarketingPositioningId(Integer marketingPositioningId) {
		this.marketingPositioningId = marketingPositioningId;
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

	public Integer getProductServicesPerseId() {
		return this.productServicesPerseId;
	}

	public void setProductServicesPerseId(Integer productServicesPerseId) {
		this.productServicesPerseId = productServicesPerseId;
	}

	public String getProjectBrief() {
		return this.projectBrief;
	}

	public void setProjectBrief(String projectBrief) {
		this.projectBrief = projectBrief;
	}

	public Integer getTechnologyPatentedId() {
		return this.technologyPatentedId;
	}

	public void setTechnologyPatentedId(Integer technologyPatentedId) {
		this.technologyPatentedId = technologyPatentedId;
	}

	public Integer getTechnologyRequiresUpgradationId() {
		return this.technologyRequiresUpgradationId;
	}

	public void setTechnologyRequiresUpgradationId(Integer technologyRequiresUpgradationId) {
		this.technologyRequiresUpgradationId = technologyRequiresUpgradationId;
	}

	public Boolean getTypeOfTechnologyCorporateId() {
		return this.typeOfTechnologyCorporateId;
	}

	public void setTypeOfTechnologyCorporateId(Boolean typeOfTechnologyCorporateId) {
		this.typeOfTechnologyCorporateId = typeOfTechnologyCorporateId;
	}

	public Boolean getWhetherTechnologyIsTied() {
		return this.is_technology_tied;
	}

	public void setWhetherTechnologyIsTied(Boolean whetherTechnologyIsTied) {
		this.is_technology_tied = whetherTechnologyIsTied;
	}

}