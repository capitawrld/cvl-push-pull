package com.opl.service.loans.model.pushpull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TmlRootRequest {
	 @JsonProperty("offset") 
	    public int getOffset() { 
			 return this.offset; } 
	    public void setOffset(int offset) { 
			 this.offset = offset; } 
	    int offset;
	    @JsonProperty("result") 
	    public List<Result> getResult() { 
			 return this.result; } 
	    public void setResult(List<Result> result) { 
			 this.result = result; } 
	    List<Result> result;
	    @JsonProperty("total_records") 
	    public int getTotal_records() { 
			 return this.total_records; } 
	    public void setTotal_records(int total_records) { 
			 this.total_records = total_records; } 
	    int total_records;
	    
	    
	    
}
