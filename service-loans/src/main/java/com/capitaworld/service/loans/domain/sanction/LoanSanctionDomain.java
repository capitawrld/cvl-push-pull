package com.capitaworld.service.loans.domain.sanction;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "sanction_detail")
public class LoanSanctionDomain implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "account_no")
	private String accountNo;

	@Column(name = "sanction_amount")
	private Double sanctionAmount;

	private Double roi;

	@Column(name = "transaction_no")
	private String transactionNo;

	@Column(name = "sanction_date")
	private Date sanctionDate;

	private Long branch;

	@Column(name = "application_id")
	private Long applicationId;

	@Column(name = "reference_no")
	private String referenceNo;

	private Double tenure;

	@Column(name = "sanction_authority")
	private String sanctionAuthority;

	private String remark;

	@Column(name = "created_by")
	private String createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "modified_by")
	private String modifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_date")
	private Date modifiedDate;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "processing_fee")
	private Double processingFee;

	@Column(name = "bank_sanction_pk")
	private Long bankSanctionPrimaryKey;
	
	@Column(name = "is_sanctioned_from")
	private Long isSanctionedFrom;
	
	@Column(name = "org_id")
	private Long orgId;
	
	@Column(name = "is_partially_disbursed_offline")
	private Boolean isPartiallyDisbursedOffline;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public Double getSanctionAmount() {
		return sanctionAmount;
	}

	public void setSanctionAmount(Double sanctionAmount) {
		this.sanctionAmount = sanctionAmount;
	}

	public Double getRoi() {
		return roi;
	}

	public void setRoi(Double roi) {
		this.roi = roi;
	}

	public String getTransactionNo() {
		return transactionNo;
	}

	public void setTransactionNo(String transactionNo) {
		this.transactionNo = transactionNo;
	}

	public Date getSanctionDate() {
		return sanctionDate;
	}

	public void setSanctionDate(Date sanctionDate) {
		this.sanctionDate = sanctionDate;
	}

	public Long getBranch() {
		return branch;
	}

	public void setBranch(Long branch) {
		this.branch = branch;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public Double getTenure() {
		return tenure;
	}

	public void setTenure(Double tenure) {
		this.tenure = tenure;
	}

	public String getSanctionAuthority() {
		return sanctionAuthority;
	}

	public void setSanctionAuthority(String sanctionAuthority) {
		this.sanctionAuthority = sanctionAuthority;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Double getProcessingFee() {
		return processingFee;
	}

	public void setProcessingFee(Double processingFee) {
		this.processingFee = processingFee;
	}

	public Long getBankSanctionPrimaryKey() {
		return bankSanctionPrimaryKey;
	}

	public void setBankSanctionPrimaryKey(Long bankSanctionPrimaryKey) {
		this.bankSanctionPrimaryKey = bankSanctionPrimaryKey;
	}
	public Long getIsSanctionedFrom() {
		return isSanctionedFrom;
	}

	public void setIsSanctionedFrom(Long isSanctionedFrom) {
		this.isSanctionedFrom = isSanctionedFrom;
	}
	
	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	
	public Boolean getIsPartiallyDisbursedOffline() {
		return isPartiallyDisbursedOffline;
	}

	public void setIsPartiallyDisbursedOffline(Boolean isPartiallyDisbursedOffline) {
		this.isPartiallyDisbursedOffline = isPartiallyDisbursedOffline;
	}

	@Override
	public String toString() {
		return "LoanSanctionDomain [id=" + id + ", branch=" + branch + ", applicationId=" + applicationId + ", tenure="
				+ tenure + ", remark=" + remark + ", createdBy=" + createdBy + ", createdDate=" + createdDate
				+ ", modifiedBy=" + modifiedBy + ", modifiedDate=" + modifiedDate + ", isActive=" + isActive
				+ ", bankSanctionPrimaryKey=" + bankSanctionPrimaryKey + ", isSanctionedFrom=" + isSanctionedFrom
				+ ", orgId=" + orgId + ", isPartiallyDisbursedOffline=" + isPartiallyDisbursedOffline + "]";
	}

}
