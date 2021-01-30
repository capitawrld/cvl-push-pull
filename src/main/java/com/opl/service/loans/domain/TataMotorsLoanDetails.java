package com.opl.service.loans.domain;

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

@Entity
@Table(name = "tata_motors_loan_details")
public class TataMotorsLoanDetails implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "partydetails_maritalstatus")
	private String partydetailsMaritalstatus;
	
	@Column(name = "address_type")
	private String addressType;
	
	@Column(name = "date_of_birth")
	private String dateOfBirth;
	
	@Column(name = "religion")
	private String religion;
	
	@Column(name = "relationship_type")
	private String relationshipType;
	
	@Column(name = "father_mother_spouse_name")
	private String fatherMotherSpouseName;
	
	@Column(name = "id_type")
	private String idType;
	
	@Column(name = "id_description")
	private String idDescription;
	
	@Column(name = "id_issue_date")
	private String idIssueDate;
	
	@Column(name = "id_expiry_date")
	private String idExpiryDate;
	
	@Column(name = "fin_occupation")
	private String finOccupation;
	
	@Column(name = "fin_occupation_in_years")
	private String finOccupationInYears;
	
	@Column(name = "partydetails_annualincome")
	private String partydetailsAnnualincome;
	
	@Column(name = "form_sixty_flag")
	private String formSixtyFlag;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "mobile_no")
	private String mobileNo;
	
	@Column(name = "pan_no_indiviual")
	private String panNoIndiviual;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "taluka")
	private String taluka;
	
	@Column(name = "district")
	private String district;
	
	@Column(name = "city_town_village")
	private String cityTownVillage;
	
	@Column(name = "address1")
	private String address1;
	
	@Column(name = "address2")
	private String address2;
	
	@Column(name = "area")
	private String area;
	
	@Column(name = "pincode")
	private String pincode;
	
	@Column(name = "coapplicant_first_name")
	private String coapplicantFirstName;
	
	@Column(name = "coapplicant_last_name")
	private String coapplicantLastName;
	
	@Column(name = "coapplicant_mobile_no")
	private String coapplicantMobileNo;
	
	@Column(name = "coapplicant_pan_no_indiviual")
	private String coapplicantPanNoIndiviual;
	
	@Column(name = "coapplicant_date_of_birth")
	private String coapplicantDateOfBirth;
	
	@Column(name = "coapplicant_city_town_village")
	private String coapplicantCityTownVillage;
	
	@Column(name = "coapplicant_address1")
	private String coapplicantAddress1;
	
	@Column(name = "coapplicant_address2")
	private String coapplicantAddress2;
	
	@Column(name = "coapplicant_pincode")
	private String coapplicantPincode;
	
	@Column(name = "account_name")
	private String accountName;
	
	@Column(name = "account_site")
	private String accountSite;
	
	@Column(name = "account_number")
	private String accountNumber;
	
	@Column(name = "pan_no_company")
	private String panNoCompany;
	
	@Column(name = "account_type")
	private String accountType;
	
	@Column(name = "account_state")
	private String accountState;
	
	@Column(name = "account_tahsil_taluka")
	private String accountTahsilTaluka;
	
	@Column(name = "account_district")
	private String accountDistrict;
	
	@Column(name = "account_address1")
	private String accountAddress1;
	
	@Column(name = "account_address2")
	private String accountAddress2;
	
	@Column(name = "account_city_town_village")
	private String accountCityTownVillage;
	
	@Column(name = "account_pincode")
	private String accountPincode;
	
	@Column(name = "ex_showroom_price")
	private String exShowroomPrice;
	
	@Column(name = "intended_application")
	private String intendedApplication;
	
	@Column(name = "type_of_property")
	private String typeOfProperty;
	
	@Column(name = "customer_type")
	private String customerType;
	
	@Column(name = "opty_id")
	private String optyId;
	
	@Column(name = "on_road_price_total_amt")
	private String onRoadPriceTotalAmt;
	
	@Column(name = "customer_category")
	private String customerCategory;
	
	@Column(name = "customer_subcategory")
	private String customerSubcategory;
	
	@Column(name = "repayment_mode")
	private String repaymentMode;
	
	@Column(name = "indicative_loan_amt")
	private String indicativeLoanAmt;
	
	@Column(name = "loan_tenor")
	private String loanTenor;
	
	@Column(name = "ltv")
	private String ltv;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date")
	private Date createdDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_date")
	private Date modifiedDate;
	
	@Column(name = "is_active")
	private Boolean isActive;
	
	@Column(name = "offset")
	private String offset;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getPartydetailsMaritalstatus() {
		return partydetailsMaritalstatus;
	}

	public void setPartydetailsMaritalstatus(String partydetailsMaritalstatus) {
		this.partydetailsMaritalstatus = partydetailsMaritalstatus;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getReligion() {
		return religion;
	}

	public void setReligion(String religion) {
		this.religion = religion;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}

	public String getFatherMotherSpouseName() {
		return fatherMotherSpouseName;
	}

	public void setFatherMotherSpouseName(String fatherMotherSpouseName) {
		this.fatherMotherSpouseName = fatherMotherSpouseName;
	}

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
	}

	public String getIdDescription() {
		return idDescription;
	}

	public void setIdDescription(String idDescription) {
		this.idDescription = idDescription;
	}

	public String getIdIssueDate() {
		return idIssueDate;
	}

	public void setIdIssueDate(String idIssueDate) {
		this.idIssueDate = idIssueDate;
	}

	public String getIdExpiryDate() {
		return idExpiryDate;
	}

	public void setIdExpiryDate(String idExpiryDate) {
		this.idExpiryDate = idExpiryDate;
	}

	public String getFinOccupation() {
		return finOccupation;
	}

	public void setFinOccupation(String finOccupation) {
		this.finOccupation = finOccupation;
	}

	public String getFinOccupationInYears() {
		return finOccupationInYears;
	}

	public void setFinOccupationInYears(String finOccupationInYears) {
		this.finOccupationInYears = finOccupationInYears;
	}

	public String getPartydetailsAnnualincome() {
		return partydetailsAnnualincome;
	}

	public void setPartydetailsAnnualincome(String partydetailsAnnualincome) {
		this.partydetailsAnnualincome = partydetailsAnnualincome;
	}

	public String getFormSixtyFlag() {
		return formSixtyFlag;
	}

	public void setFormSixtyFlag(String formSixtyFlag) {
		this.formSixtyFlag = formSixtyFlag;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getPanNoIndiviual() {
		return panNoIndiviual;
	}

	public void setPanNoIndiviual(String panNoIndiviual) {
		this.panNoIndiviual = panNoIndiviual;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTaluka() {
		return taluka;
	}

	public void setTaluka(String taluka) {
		this.taluka = taluka;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getCityTownVillage() {
		return cityTownVillage;
	}

	public void setCityTownVillage(String cityTownVillage) {
		this.cityTownVillage = cityTownVillage;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getCoapplicantFirstName() {
		return coapplicantFirstName;
	}

	public void setCoapplicantFirstName(String coapplicantFirstName) {
		this.coapplicantFirstName = coapplicantFirstName;
	}

	public String getCoapplicantLastName() {
		return coapplicantLastName;
	}

	public void setCoapplicantLastName(String coapplicantLastName) {
		this.coapplicantLastName = coapplicantLastName;
	}

	public String getCoapplicantMobileNo() {
		return coapplicantMobileNo;
	}

	public void setCoapplicantMobileNo(String coapplicantMobileNo) {
		this.coapplicantMobileNo = coapplicantMobileNo;
	}

	public String getCoapplicantPanNoIndiviual() {
		return coapplicantPanNoIndiviual;
	}

	public void setCoapplicantPanNoIndiviual(String coapplicantPanNoIndiviual) {
		this.coapplicantPanNoIndiviual = coapplicantPanNoIndiviual;
	}

	public String getCoapplicantDateOfBirth() {
		return coapplicantDateOfBirth;
	}

	public void setCoapplicantDateOfBirth(String coapplicantDateOfBirth) {
		this.coapplicantDateOfBirth = coapplicantDateOfBirth;
	}

	public String getCoapplicantCityTownVillage() {
		return coapplicantCityTownVillage;
	}

	public void setCoapplicantCityTownVillage(String coapplicantCityTownVillage) {
		this.coapplicantCityTownVillage = coapplicantCityTownVillage;
	}

	public String getCoapplicantAddress1() {
		return coapplicantAddress1;
	}

	public void setCoapplicantAddress1(String coapplicantAddress1) {
		this.coapplicantAddress1 = coapplicantAddress1;
	}

	public String getCoapplicantAddress2() {
		return coapplicantAddress2;
	}

	public void setCoapplicantAddress2(String coapplicantAddress2) {
		this.coapplicantAddress2 = coapplicantAddress2;
	}

	public String getCoapplicantPincode() {
		return coapplicantPincode;
	}

	public void setCoapplicantPincode(String coapplicantPincode) {
		this.coapplicantPincode = coapplicantPincode;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountSite() {
		return accountSite;
	}

	public void setAccountSite(String accountSite) {
		this.accountSite = accountSite;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getPanNoCompany() {
		return panNoCompany;
	}

	public void setPanNoCompany(String panNoCompany) {
		this.panNoCompany = panNoCompany;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getAccountState() {
		return accountState;
	}

	public void setAccountState(String accountState) {
		this.accountState = accountState;
	}

	public String getAccountTahsilTaluka() {
		return accountTahsilTaluka;
	}

	public void setAccountTahsilTaluka(String accountTahsilTaluka) {
		this.accountTahsilTaluka = accountTahsilTaluka;
	}

	public String getAccountDistrict() {
		return accountDistrict;
	}

	public void setAccountDistrict(String accountDistrict) {
		this.accountDistrict = accountDistrict;
	}

	public String getAccountAddress1() {
		return accountAddress1;
	}

	public void setAccountAddress1(String accountAddress1) {
		this.accountAddress1 = accountAddress1;
	}

	public String getAccountAddress2() {
		return accountAddress2;
	}

	public void setAccountAddress2(String accountAddress2) {
		this.accountAddress2 = accountAddress2;
	}

	public String getAccountCityTownVillage() {
		return accountCityTownVillage;
	}

	public void setAccountCityTownVillage(String accountCityTownVillage) {
		this.accountCityTownVillage = accountCityTownVillage;
	}

	public String getAccountPincode() {
		return accountPincode;
	}

	public void setAccountPincode(String accountPincode) {
		this.accountPincode = accountPincode;
	}

	public String getExShowroomPrice() {
		return exShowroomPrice;
	}

	public void setExShowroomPrice(String exShowroomPrice) {
		this.exShowroomPrice = exShowroomPrice;
	}

	public String getIntendedApplication() {
		return intendedApplication;
	}

	public void setIntendedApplication(String intendedApplication) {
		this.intendedApplication = intendedApplication;
	}

	public String getTypeOfProperty() {
		return typeOfProperty;
	}

	public void setTypeOfProperty(String typeOfProperty) {
		this.typeOfProperty = typeOfProperty;
	}

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public String getOptyId() {
		return optyId;
	}

	public void setOptyId(String optyId) {
		this.optyId = optyId;
	}

	public String getOnRoadPriceTotalAmt() {
		return onRoadPriceTotalAmt;
	}

	public void setOnRoadPriceTotalAmt(String onRoadPriceTotalAmt) {
		this.onRoadPriceTotalAmt = onRoadPriceTotalAmt;
	}

	public String getCustomerCategory() {
		return customerCategory;
	}

	public void setCustomerCategory(String customerCategory) {
		this.customerCategory = customerCategory;
	}

	public String getCustomerSubcategory() {
		return customerSubcategory;
	}

	public void setCustomerSubcategory(String customerSubcategory) {
		this.customerSubcategory = customerSubcategory;
	}

	public String getRepaymentMode() {
		return repaymentMode;
	}

	public void setRepaymentMode(String repaymentMode) {
		this.repaymentMode = repaymentMode;
	}

	public String getIndicativeLoanAmt() {
		return indicativeLoanAmt;
	}

	public void setIndicativeLoanAmt(String indicativeLoanAmt) {
		this.indicativeLoanAmt = indicativeLoanAmt;
	}

	public String getLoanTenor() {
		return loanTenor;
	}

	public void setLoanTenor(String loanTenor) {
		this.loanTenor = loanTenor;
	}

	public String getLtv() {
		return ltv;
	}

	public void setLtv(String ltv) {
		this.ltv = ltv;
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

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}
	
}
