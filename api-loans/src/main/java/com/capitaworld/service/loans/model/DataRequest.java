package com.capitaworld.service.loans.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataRequest implements Serializable{
	/**
	 * 
	 * @author harshit
	 */
	private Long id;
	private String value;
	private Long productId;
	private Long mappingId;
	private Double tenure;
	
	private static final long serialVersionUID = 1L;
	
	public DataRequest() {
		// Do nothing because of X and Y.
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getMappingId() {
		return mappingId;
	}

	public void setMappingId(Long mappingId) {
		this.mappingId = mappingId;
	}

	public Double getTenure() {
		return tenure;
	}

	public void setTenure(Double tenure) {
		this.tenure = tenure;
	}

	
	
	

}
