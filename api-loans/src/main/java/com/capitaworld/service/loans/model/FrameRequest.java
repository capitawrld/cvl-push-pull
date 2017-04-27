package com.capitaworld.service.loans.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FrameRequest implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long applicationId;
	
	private List<Map<String, Object>> dataList;

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public List<Map<String, Object>> getDataList() {
		return dataList;
	}

	public void setDataList(List<Map<String, Object>> dataList) {
		this.dataList = dataList;
	}

	

	

}
