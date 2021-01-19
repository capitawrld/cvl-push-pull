package com.opl.service.loans.model.pushpull;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by pooja.patel on 19-06-2019.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PushPullRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	private String email;
	private String mobile;
	private String pan;
	private String gstIn;
	
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
	
	
}
