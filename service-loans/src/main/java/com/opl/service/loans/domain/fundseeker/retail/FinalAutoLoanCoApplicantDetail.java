package com.opl.service.loans.domain.fundseeker.retail;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;

/**
 * 
 * @author rohit.chaudhary
 *
 */
@Entity
@Table(name = "fs_retail_final_auto_loan_co_applicant_details")
public class FinalAutoLoanCoApplicantDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "application_id")
	private LoanApplicationMaster applicationId;

	@OneToOne
	@JoinColumn(name = "proposal_mapping_id")
	private ApplicationProposalMapping proposalId;

	@Column(name = "name")
	private String name;

	@Column(name = "father_full_name")
	private String fatherFullName;

	@Column(name = "mather_maiden_name")
	private String matherMaidenName;

	@Column(name = "name_of_spouse")
	private String nameOfSpouse;

	@Column(name = "cast")
	private Integer cast;

	@Column(name = "religion")
	private Integer religion;

	@Column(name = "place_of_birth")
	private String placeOfBirth;

	@Column(name = "no_of_children")
	private Integer noOfChildren;

	@Column(name = "permanent_premise_no")
	private String permanentPremiseNo;

	@Column(name = "permanent_street_name")
	private String permanentStreetName;

	@Column(name = "permanent_landmark")
	private String permanentLandmark;

	@Column(name = "permanent_pin_code")
	private Integer permanentPinCode;

	@Column(name = "permanent_city")
	private Integer permanentCity;

	@Column(name = "permanent_state")
	private Integer permanentState;

	@Column(name = "permanent_country")
	private Integer permanentCountry;

	@Column(name = "same_as_permanent_address")
	private Boolean isSameAsPermanentAddress;

	@Column(name = "correspondence_premise_no")
	private String correspondencePremiseNo;

	@Column(name = "correspondence_street_name")
	private String correspondenceStreetName;

	@Column(name = "correspondence_land_mark")
	private String correspondenceLandmark;

	@Column(name = "correspondence_pin_code")
	private Integer correspondencePinCode;

	@Column(name = "correspondence_city")
	private Integer correspondenceCity;

	@Column(name = "correspondence_state")
	private Integer correspondenceState;

	@Column(name = "correspondence_country")
	private Integer correspondenceCountry;

	@Column(name = "educational_qualification")
	private String educationalQualification;

	@Column(name = "year")
	private Integer year;

	@Column(name = "employeeType")
	private Integer employeeType;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "modified_date")
	private Date modifiedDate;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "modified_by")
	private Long modifiedBy;

	@Column(name = "co_applicant_id")
	private Long coApplicantId;

	@Column(name = "status_id")
	private Integer statusId;

	@Column(name = "remarks")
	private String remarks;

	public FinalAutoLoanCoApplicantDetail() {
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LoanApplicationMaster getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(LoanApplicationMaster applicationId) {
		this.applicationId = applicationId;
	}

	public ApplicationProposalMapping getProposalId() {
		return proposalId;
	}

	public void setProposalId(ApplicationProposalMapping proposalId) {
		this.proposalId = proposalId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFatherFullName() {
		return fatherFullName;
	}

	public void setFatherFullName(String fatherFullName) {
		this.fatherFullName = fatherFullName;
	}

	public String getMatherMaidenName() {
		return matherMaidenName;
	}

	public void setMatherMaidenName(String matherMaidenName) {
		this.matherMaidenName = matherMaidenName;
	}

	public String getNameOfSpouse() {
		return nameOfSpouse;
	}

	public void setNameOfSpouse(String nameOfSpouse) {
		this.nameOfSpouse = nameOfSpouse;
	}

	public Integer getCast() {
		return cast;
	}

	public void setCast(Integer cast) {
		this.cast = cast;
	}

	public Integer getReligion() {
		return religion;
	}

	public void setReligion(Integer religion) {
		this.religion = religion;
	}

	public String getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	public Integer getNoOfChildren() {
		return noOfChildren;
	}

	public void setNoOfChildren(Integer noOfChildren) {
		this.noOfChildren = noOfChildren;
	}

	public String getPermanentPremiseNo() {
		return permanentPremiseNo;
	}

	public void setPermanentPremiseNo(String permanentPremiseNo) {
		this.permanentPremiseNo = permanentPremiseNo;
	}

	public String getPermanentStreetName() {
		return permanentStreetName;
	}

	public void setPermanentStreetName(String permanentStreetName) {
		this.permanentStreetName = permanentStreetName;
	}

	public String getPermanentLandmark() {
		return permanentLandmark;
	}

	public void setPermanentLandmark(String permanentLandmark) {
		this.permanentLandmark = permanentLandmark;
	}

	public Integer getPermanentPinCode() {
		return permanentPinCode;
	}

	public void setPermanentPinCode(Integer permanentPinCode) {
		this.permanentPinCode = permanentPinCode;
	}

	public Integer getPermanentCity() {
		return permanentCity;
	}

	public void setPermanentCity(Integer permanentCity) {
		this.permanentCity = permanentCity;
	}

	public Integer getPermanentState() {
		return permanentState;
	}

	public void setPermanentState(Integer permanentState) {
		this.permanentState = permanentState;
	}

	public Integer getPermanentCountry() {
		return permanentCountry;
	}

	public void setPermanentCountry(Integer permanentCountry) {
		this.permanentCountry = permanentCountry;
	}

	public Boolean getIsSameAsPermanentAddress() {
		return isSameAsPermanentAddress;
	}

	public void setIsSameAsPermanentAddress(Boolean isSameAsPermanentAddress) {
		this.isSameAsPermanentAddress = isSameAsPermanentAddress;
	}

	public String getCorrespondencePremiseNo() {
		return correspondencePremiseNo;
	}

	public void setCorrespondencePremiseNo(String correspondencePremiseNo) {
		this.correspondencePremiseNo = correspondencePremiseNo;
	}

	public String getCorrespondenceStreetName() {
		return correspondenceStreetName;
	}

	public void setCorrespondenceStreetName(String correspondenceStreetName) {
		this.correspondenceStreetName = correspondenceStreetName;
	}

	public String getCorrespondenceLandmark() {
		return correspondenceLandmark;
	}

	public void setCorrespondenceLandmark(String correspondenceLandmark) {
		this.correspondenceLandmark = correspondenceLandmark;
	}

	public Integer getCorrespondencePinCode() {
		return correspondencePinCode;
	}

	public void setCorrespondencePinCode(Integer correspondencePinCode) {
		this.correspondencePinCode = correspondencePinCode;
	}

	public Integer getCorrespondenceCity() {
		return correspondenceCity;
	}

	public void setCorrespondenceCity(Integer correspondenceCity) {
		this.correspondenceCity = correspondenceCity;
	}

	public Integer getCorrespondenceState() {
		return correspondenceState;
	}

	public void setCorrespondenceState(Integer correspondenceState) {
		this.correspondenceState = correspondenceState;
	}

	public Integer getCorrespondenceCountry() {
		return correspondenceCountry;
	}

	public void setCorrespondenceCountry(Integer correspondenceCountry) {
		this.correspondenceCountry = correspondenceCountry;
	}

	public String getEducationalQualification() {
		return educationalQualification;
	}

	public void setEducationalQualification(String educationalQualification) {
		this.educationalQualification = educationalQualification;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getEmployeeType() {
		return employeeType;
	}

	public void setEmployeeType(Integer employeeType) {
		this.employeeType = employeeType;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
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

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Long getCoApplicantId() {
		return coApplicantId;
	}

	public void setCoApplicantId(Long coApplicantId) {
		this.coApplicantId = coApplicantId;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
