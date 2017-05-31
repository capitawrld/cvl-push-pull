package com.capitaworld.service.loans.model.corporate;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalWorkingCapitalLoanRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long clientId;
	private Long applicationId;
	private Integer accountingSystemsId;
	private Integer brandAmbassadorId;
	private Integer competenceId;
	private Integer distributionAndMarketingTieUpsId;
	private Integer environmentCertificationId;
	private Integer existingShareHoldersId;
	private Integer indiaDistributionNetworkId;
	private Integer internalAuditId;
	private Boolean isDependsMajorlyOnGovernment;
	private Boolean isIsoCertified;
	private Integer marketPositionId;
	private Integer marketPositioningTopId;
	private Integer marketShareTurnoverId;
	private Integer marketingPositioningId;
	private Integer productServicesPerseId;
	private Integer technologyPatentedId;
	private Integer technologyRequiresUpgradationId;
	private Integer technologyTypeId;
	private Boolean whetherTechnologyIsTied;
	private List<Integer> overseasNetworkIds = Collections.emptyList();
	
	private Boolean isFinalMcqFilled;

	public FinalWorkingCapitalLoanRequest() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public Integer getAccountingSystemsId() {
		return accountingSystemsId;
	}

	public void setAccountingSystemsId(Integer accountingSystemsId) {
		this.accountingSystemsId = accountingSystemsId;
	}

	public Integer getBrandAmbassadorId() {
		return brandAmbassadorId;
	}

	public void setBrandAmbassadorId(Integer brandAmbassadorId) {
		this.brandAmbassadorId = brandAmbassadorId;
	}

	public Integer getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(Integer competenceId) {
		this.competenceId = competenceId;
	}

	public Integer getDistributionAndMarketingTieUpsId() {
		return distributionAndMarketingTieUpsId;
	}

	public void setDistributionAndMarketingTieUpsId(Integer distributionAndMarketingTieUpsId) {
		this.distributionAndMarketingTieUpsId = distributionAndMarketingTieUpsId;
	}

	public Integer getEnvironmentCertificationId() {
		return environmentCertificationId;
	}

	public void setEnvironmentCertificationId(Integer environmentCertificationId) {
		this.environmentCertificationId = environmentCertificationId;
	}

	public Integer getExistingShareHoldersId() {
		return existingShareHoldersId;
	}

	public void setExistingShareHoldersId(Integer existingShareHoldersId) {
		this.existingShareHoldersId = existingShareHoldersId;
	}

	public Integer getIndiaDistributionNetworkId() {
		return indiaDistributionNetworkId;
	}

	public void setIndiaDistributionNetworkId(Integer indiaDistributionNetworkId) {
		this.indiaDistributionNetworkId = indiaDistributionNetworkId;
	}

	public Integer getInternalAuditId() {
		return internalAuditId;
	}

	public void setInternalAuditId(Integer internalAuditId) {
		this.internalAuditId = internalAuditId;
	}

	public Boolean getIsDependsMajorlyOnGovernment() {
		return isDependsMajorlyOnGovernment;
	}

	public void setIsDependsMajorlyOnGovernment(Boolean isDependsMajorlyOnGovernment) {
		this.isDependsMajorlyOnGovernment = isDependsMajorlyOnGovernment;
	}

	public Boolean getIsIsoCertified() {
		return isIsoCertified;
	}

	public void setIsIsoCertified(Boolean isIsoCertified) {
		this.isIsoCertified = isIsoCertified;
	}

	public Integer getMarketPositionId() {
		return marketPositionId;
	}

	public void setMarketPositionId(Integer marketPositionId) {
		this.marketPositionId = marketPositionId;
	}

	public Integer getMarketPositioningTopId() {
		return marketPositioningTopId;
	}

	public void setMarketPositioningTopId(Integer marketPositioningTopId) {
		this.marketPositioningTopId = marketPositioningTopId;
	}

	public Integer getMarketShareTurnoverId() {
		return marketShareTurnoverId;
	}

	public void setMarketShareTurnoverId(Integer marketShareTurnoverId) {
		this.marketShareTurnoverId = marketShareTurnoverId;
	}

	public Integer getMarketingPositioningId() {
		return marketingPositioningId;
	}

	public void setMarketingPositioningId(Integer marketingPositioningId) {
		this.marketingPositioningId = marketingPositioningId;
	}

	public Integer getProductServicesPerseId() {
		return productServicesPerseId;
	}

	public void setProductServicesPerseId(Integer productServicesPerseId) {
		this.productServicesPerseId = productServicesPerseId;
	}

	public Integer getTechnologyPatentedId() {
		return technologyPatentedId;
	}

	public void setTechnologyPatentedId(Integer technologyPatentedId) {
		this.technologyPatentedId = technologyPatentedId;
	}

	public Integer getTechnologyRequiresUpgradationId() {
		return technologyRequiresUpgradationId;
	}

	public void setTechnologyRequiresUpgradationId(Integer technologyRequiresUpgradationId) {
		this.technologyRequiresUpgradationId = technologyRequiresUpgradationId;
	}

	public Integer getTechnologyTypeId() {
		return technologyTypeId;
	}

	public void setTechnologyTypeId(Integer technologyTypeId) {
		this.technologyTypeId = technologyTypeId;
	}

	public Boolean getWhetherTechnologyIsTied() {
		return whetherTechnologyIsTied;
	}

	public void setWhetherTechnologyIsTied(Boolean whetherTechnologyIsTied) {
		this.whetherTechnologyIsTied = whetherTechnologyIsTied;
	}

	public List<Integer> getOverseasNetworkIds() {
		return overseasNetworkIds;
	}

	public void setOverseasNetworkIds(List<Integer> overseasNetworkIds) {
		this.overseasNetworkIds = overseasNetworkIds;
	}

	public Boolean getIsFinalMcqFilled() {
		return isFinalMcqFilled;
	}

	public void setIsFinalMcqFilled(Boolean isFinalMcqFilled) {
		this.isFinalMcqFilled = isFinalMcqFilled;
	}
	

}