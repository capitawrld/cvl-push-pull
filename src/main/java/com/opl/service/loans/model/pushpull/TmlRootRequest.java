package com.opl.service.loans.model.pushpull;

import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TmlRootRequest {

	String offset;

	@JsonProperty("offset")
	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	@JsonProperty("result")
	public List<Result> getResult() {
		return this.result;
	}

	public void setResult(List<Result> result) {
		this.result = result;
	}

	List<Result> result;

	@JsonProperty("total_records")
	public int getTotal_records() {
		return this.total_records;
	}

	public void setTotal_records(int total_records) {
		this.total_records = total_records;
	}

	int total_records;
	
	String responseBody;

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	
	JSONObject request;

	public JSONObject getRequest() {
		return request;
	}

	public void setRequest(JSONObject request) {
		this.request = request;
	}
	
	
	

}
