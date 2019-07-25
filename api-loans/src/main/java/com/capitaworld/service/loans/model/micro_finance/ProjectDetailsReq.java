package com.capitaworld.service.loans.model.micro_finance;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author harsukh.ghumaliya
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ProjectDetailsReq {

	private Long id;
	private Long applicationId;
	private Integer loanType;
	private String loanPurpose;
	private Integer purposeOfLoan;
	private Double loanAmountRequired;
	private Double costOfProject;
	private Double costOfEquipment;
	private Double workingCapOfEquipment;
	private Double totalCostEquipment;
	private Double promoterContribution;
	private Double loanRequiredFromSidbi;
	private Double totalMeanFinance;
	private Double totalCashFlow;
	private Integer repaymentFrequency;
	private Boolean insurenceRequired;
	private String insurenceCompanyName;
	private Double insurencePremium;
	private Boolean isProjectDetailsFilled;

	public ProjectDetailsReq() {
	}

	public ProjectDetailsReq(Long applicationId, Integer loanType, Integer purposeOfLoan, Double loanAmountRequired,
							 Double costOfProject, Double costOfEquipment, Double workingCapOfEquipment, Double totalCostEquipment,
							 Double promoterContribution, Double loanRequiredFromSidbi, Double totalMeanFinance, Double totalCashFlow,
							 Integer repaymentFrequency, Boolean insurenceRequired, String insurenceCompanyName, Double insurencePremium,
							 Boolean isProjectDetailsFilled) {
		super();
		this.applicationId = applicationId;
		this.loanType = loanType;
		this.purposeOfLoan = purposeOfLoan;
		this.loanAmountRequired = loanAmountRequired;
		this.costOfProject = costOfProject;
		this.costOfEquipment = costOfEquipment;
		this.workingCapOfEquipment = workingCapOfEquipment;
		this.totalCostEquipment = totalCostEquipment;
		this.promoterContribution = promoterContribution;
		this.loanRequiredFromSidbi = loanRequiredFromSidbi;
		this.totalMeanFinance = totalMeanFinance;
		this.totalCashFlow = totalCashFlow;
		this.repaymentFrequency = repaymentFrequency;
		this.insurenceRequired = insurenceRequired;
		this.insurenceCompanyName = insurenceCompanyName;
		this.insurencePremium = insurencePremium;
		this.isProjectDetailsFilled = isProjectDetailsFilled;
	}

	public Integer getLoanType() {
		return loanType;
	}

	public void setLoanType(Integer loanType) {
		this.loanType = loanType;
	}

	public Double getLoanAmountRequired() {
		return loanAmountRequired;
	}

	public void setLoanAmountRequired(Double loanAmountRequired) {
		this.loanAmountRequired = loanAmountRequired;
	}

	public String getLoanPurpose() {
		return loanPurpose;
	}

	public void setLoanPurpose(String loanPurpose) {
		this.loanPurpose = loanPurpose;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsProjectDetailsFilled() {
		return isProjectDetailsFilled;
	}

	public void setIsProjectDetailsFilled(Boolean isProjectDetailsFilled) {
		this.isProjectDetailsFilled = isProjectDetailsFilled;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public Integer getPurposeOfLoan() {
		return purposeOfLoan;
	}

	public void setPurposeOfLoan(Integer purposeOfLoan) {
		this.purposeOfLoan = purposeOfLoan;
	}

	@Override
	public String toString() {
		return "ProjectDetailsReq{" +
				"id=" + id +
				", applicationId=" + applicationId +
				", loanType=" + loanType +
				", loanPurpose='" + loanPurpose + '\'' +
				", purposeOfLoan='" + purposeOfLoan + '\'' +
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
				", isProjectDetailsFilled=" + isProjectDetailsFilled +
				'}';
	}
}
