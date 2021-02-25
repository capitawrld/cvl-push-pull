package com.opl.service.loans.model.pushpull;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by pooja.patel on 19-06-2019.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PushPullRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Email
	private String email;
	
	@Pattern(regexp="(^$|[0-9]{10})")
	private String mobile;
	
	@Pattern(regexp="([A-Za-z]{5}\\d{4}[A-Za-z]{1})")
	private String pan;
	
	private String gstIn;
	
	private Long businessTypeId;
	
	private String username;
	
	private String financierId;
	
	private Long clientId;
	
	private Long offset;
	
	private Integer gstTypeId;
	
	private Long id;

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Long getBusinessTypeId() {
		return businessTypeId;
	}
	public void setBusinessTypeId(Long businessTypeId) {
		this.businessTypeId = businessTypeId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public String getGstIn() {
		return gstIn;
	}
	public void setGstIn(String gstIn) {
		this.gstIn = gstIn;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getFinancierId() {
		return financierId;
	}
	public void setFinancierId(String financierId) {
		this.financierId = financierId;
	}
	public Long getClientId() {
		return clientId;
	}
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	public Long getOffset() {
		return offset;
	}
	public void setOffset(Long offset) {
		this.offset = offset;
	}
	public Integer getGstTypeId() {
		return gstTypeId;
	}
	public void setGstTypeId(Integer gstTypeId) {
		this.gstTypeId = gstTypeId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
}
