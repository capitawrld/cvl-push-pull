package com.opl.service.loans.domain.fundseeker.ddr;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Created by : harshit
 * The persistent class for the fs_ddr_creditors_details database table.
 * 
 */
@Entity
@Table(name="fs_ddr_creditors_details")
@NamedQuery(name="DDRCreditorsDetails.findAll", query="SELECT a FROM DDRCreditorsDetails a")
public class DDRCreditorsDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "fs_ddr_form_id")
	private Long ddrFormId;

	private String name;
	
	private Double amount;
	
	@Column(name = "avg_creditor_turnover_period")
	private Double avgCreditorTurnoverPeriod;
	
	@Column(name = "comment")
	private String comment;
	
	@Column(name = "created_by")
	private Long createdBy;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_date")
	private Date createdDate;
	
	@Column(name = "modify_by")
	private Long modifyBy;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modify_date")
	private Date modifyDate;

	@Column(name = "is_active")
	private Boolean isActive;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDdrFormId() {
		return ddrFormId;
	}

	public void setDdrFormId(Long ddrFormId) {
		this.ddrFormId = ddrFormId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Long getModifyBy() {
		return modifyBy;
	}

	public void setModifyBy(Long modifyBy) {
		this.modifyBy = modifyBy;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Double getAvgCreditorTurnoverPeriod() {
		return avgCreditorTurnoverPeriod;
	}

	public void setAvgCreditorTurnoverPeriod(Double avgCreditorTurnoverPeriod) {
		this.avgCreditorTurnoverPeriod = avgCreditorTurnoverPeriod;
	}

	@Override
	public String toString() {
		return "DDRCreditorsDetails [id=" + id + ", ddrFormId=" + ddrFormId + ", name=" + name + ", amount=" + amount
				+ ", createdBy=" + createdBy + ", createdDate=" + createdDate + ", modifyBy=" + modifyBy
				+ ", modifyDate=" + modifyDate + ", isActive=" + isActive + "]";
	}


	
	

}
