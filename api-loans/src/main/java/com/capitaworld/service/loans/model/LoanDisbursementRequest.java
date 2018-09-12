package com.capitaworld.service.loans.model;

import java.io.Serializable;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

import com.capitaworld.service.loans.model.common.AuditActivityRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanDisbursementRequest extends AuditActivityRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	private Long applicationId;
	private String referenceNo;
	private String transactionNo;
	private String accountNo;
	private Double disbursedAmount;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private Date disbursementDate;

	private String disbursementAuthority;
	
	@JsonProperty("paymentMode")
	private Long mode;
	
	private String remark;
	private String userName;
	private String password;
	private String actionBy;

	private Boolean isSaved = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getTransactionNo() {
		return transactionNo;
	}

	public void setTransactionNo(String transactionNo) {
		this.transactionNo = transactionNo;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public Double getDisbursedAmount() {
		return disbursedAmount;
	}

	public void setDisbursedAmount(Double disbursedAmount) {
		this.disbursedAmount = disbursedAmount;
	}

	public Date getDisbursementDate() {
		return disbursementDate;
	}

	public void setDisbursementDate(Date disbursementDate) {
		this.disbursementDate = disbursementDate;
	}

	public String getDisbursementAuthority() {
		return disbursementAuthority;
	}

	public void setDisbursementAuthority(String disbursementAuthority) {
		this.disbursementAuthority = disbursementAuthority;
	}

	public Long getMode() {
		return mode;
	}

	public void setMode(Long mode) {
		this.mode = mode;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getActionBy() {
		return actionBy;
	}

	public void setActionBy(String actionBy) {
		this.actionBy = actionBy;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

	@Override
	public String toString() {
		return "LoanDisbursementRequest [id=" + id + ", applicationId=" + applicationId + ", referenceNo=" + referenceNo
				+ ", transactionNo=" + transactionNo + ", accountNo=" + accountNo + ", disbursedAmount="
				+ disbursedAmount + ", disbursementDate=" + disbursementDate + ", disbursementAuthority="
				+ disbursementAuthority + ", mode=" + mode + ", remark=" + remark + ", userName=" + userName
				+ ", password=" + password + ", actionBy=" + actionBy + ", isSaved=" + isSaved + "]";
	}

}
