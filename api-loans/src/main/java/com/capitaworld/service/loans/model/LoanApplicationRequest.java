package com.capitaworld.service.loans.model;

import java.io.Serializable;

/**
 * The persistent class for the fs_loan_application_master database table.
 * 
 */
public class LoanApplicationRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	private Double amount;

	private String categoryCode;

	private String name;

	private Long productId;

	private Integer tenure;

	private Long userId;
	
	private Integer loanType;


	public LoanApplicationRequest() {
	}

	public LoanApplicationRequest(Long id) {
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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getProductId() {
		return this.productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Integer getTenure() {
		return tenure;
	}

	public void setTenure(Integer tenure) {
		this.tenure = tenure;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getLoanType() {
		return loanType;
	}

	public void setLoanType(Integer loanType) {
		this.loanType = loanType;
	}

		
	

}