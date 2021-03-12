package com.opl.service.loans.model.pushpull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {
	@JsonProperty("client_id")
	public String getClient_id() {
		return this.client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	String client_id;

	@JsonProperty("title")
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	String title;


	String fatherMotherSpouseName;

	@JsonProperty("father_mother_spouse_name")
	public String getFatherMotherSpouseName() {
		return fatherMotherSpouseName;
	}

	public void setFatherMotherSpouseName(String fatherMotherSpouseName) {
		this.fatherMotherSpouseName = fatherMotherSpouseName;
	}

	@JsonProperty("gender")
	public String getGender() {
		return this.gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	String gender;


	String firstName;

	@JsonProperty("first_name")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	String lastName;	

	@JsonProperty("last_name")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	String mobileNo;
	
	@JsonProperty("mobile_no")
	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	@JsonProperty("religion")
	public String getReligion() {
		return this.religion;
	}

	public void setReligion(String religion) {
		this.religion = religion;
	}

	String religion;

	String addressType;

	@JsonProperty("address_type")
	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	@JsonProperty("address1")
	public String getAddress1() {
		return this.address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	String address1;

	@JsonProperty("address2")
	public String getAddress2() {
		return this.address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	String address2;

	@JsonProperty("area")
	public String getArea() {
		return this.area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	String area;


	String cityTownVillage;

	@JsonProperty("city_town_village")
	public String getCityTownVillage() {
		return cityTownVillage;
	}

	public void setCityTownVillage(String cityTownVillage) {
		this.cityTownVillage = cityTownVillage;
	}

	@JsonProperty("state")
	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	String state;

	@JsonProperty("district")
	public String getDistrict() {
		return this.district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	String district;

	@JsonProperty("pincode")
	public String getPincode() {
		return this.pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	String pincode;


	String intendedApplication;

	@JsonProperty("intended_application")
	public String getIntendedApplication() {
		return intendedApplication;
	}

	public void setIntendedApplication(String intendedApplication) {
		this.intendedApplication = intendedApplication;
	}

	String accountType;

	@JsonProperty("account_type")
	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	String accountName;

	@JsonProperty("account_name")
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}


	String accountSite;
	
	@JsonProperty("account_site")
	public String getAccountSite() {
		return accountSite;
	}

	public void setAccountSite(String accountSite) {
		this.accountSite = accountSite;
	}


	String accountNumber;

	@JsonProperty("account_number")
	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}


	String accountAddress1;

	@JsonProperty("account_address1")
	public String getAccountAddress1() {
		return accountAddress1;
	}

	public void setAccountAddress1(String accountAddress1) {
		this.accountAddress1 = accountAddress1;
	}


	String accountAddress2;
	

	@JsonProperty("account_address2")
	public String getAccountAddress2() {
		return accountAddress2;
	}

	public void setAccountAddress2(String accountAddress2) {
		this.accountAddress2 = accountAddress2;
	}


	String accountCityTownVillage;

	@JsonProperty("account_city_town_village")
	public String getAccountCityTownVillage() {
		return accountCityTownVillage;
	}

	public void setAccountCityTownVillage(String accountCityTownVillage) {
		this.accountCityTownVillage = accountCityTownVillage;
	}

	String accountState;

	@JsonProperty("account_state")
	public String getAccountState() {
		return accountState;
	}

	public void setAccountState(String accountState) {
		this.accountState = accountState;
	}


	String accountDistrict;

	@JsonProperty("account_district")
	public String getAccountDistrict() {
		return accountDistrict;
	}

	public void setAccountDistrict(String accountDistrict) {
		this.accountDistrict = accountDistrict;
	}


	String accountPincode;

	@JsonProperty("account_pincode")
	public String getAccountPincode() {
		return accountPincode;
	}

	public void setAccountPincode(String accountPincode) {
		this.accountPincode = accountPincode;
	}


	String optyId;

	@JsonProperty("opty_id")
	public String getOptyId() {
		return optyId;
	}

	public void setOptyId(String optyId) {
		this.optyId = optyId;
	}

	@JsonProperty("opty_created_date")
	public Object getOpty_created_date() {
		return this.opty_created_date;
	}

	public void setOpty_created_date(Object opty_created_date) {
		this.opty_created_date = opty_created_date;
	}

	Object opty_created_date;


	String exShowroomPrice;

	@JsonProperty("ex_showroom_price")
	public String getExShowroomPrice() {
		return exShowroomPrice;
	}

	public void setExShowroomPrice(String exShowroomPrice) {
		this.exShowroomPrice = exShowroomPrice;
	}


	String onRoadPriceTotalAmt;

	@JsonProperty("on_road_price_total_amt")
	public String getOnRoadPriceTotalAmt() {
		return onRoadPriceTotalAmt;
	}

	public void setOnRoadPriceTotalAmt(String onRoadPriceTotalAmt) {
		this.onRoadPriceTotalAmt = onRoadPriceTotalAmt;
	}

	@JsonProperty("cust_loan_type")
	public Object getCust_loan_type() {
		return this.cust_loan_type;
	}

	public void setCust_loan_type(Object cust_loan_type) {
		this.cust_loan_type = cust_loan_type;
	}

	Object cust_loan_type;


	String formSixtyFlag;

	@JsonProperty("form_sixty_flag")
	public String getFormSixtyFlag() {
		return formSixtyFlag;
	}

	public void setFormSixtyFlag(String formSixtyFlag) {
		this.formSixtyFlag = formSixtyFlag;
	}

	@JsonProperty("express_deal")
	public Object getExpress_deal() {
		return this.express_deal;
	}

	public void setExpress_deal(Object express_deal) {
		this.express_deal = express_deal;
	}

	Object express_deal;

	@JsonProperty("sales_person_mobile_number")
	public Object getSales_person_mobile_number() {
		return this.sales_person_mobile_number;
	}

	public void setSales_person_mobile_number(Object sales_person_mobile_number) {
		this.sales_person_mobile_number = sales_person_mobile_number;
	}

	Object sales_person_mobile_number;


	String indicativeLoanAmt;

	@JsonProperty("indicative_loan_amt")
	public String getIndicativeLoanAmt() {
		return indicativeLoanAmt;
	}

	public void setIndicativeLoanAmt(String indicativeLoanAmt) {
		this.indicativeLoanAmt = indicativeLoanAmt;
	}


	String loanTenor;


	@JsonProperty("loan_tenor")
	public String getLoanTenor() {
		return loanTenor;
	}

	public void setLoanTenor(String loanTenor) {
		this.loanTenor = loanTenor;
	}

	String panNoIndiviual;
	
	@JsonProperty("pan_no_indiviual")
	public String getPanNoIndiviual() {
		return panNoIndiviual;
	}

	public void setPanNoIndiviual(String panNoIndiviual) {
		this.panNoIndiviual = panNoIndiviual;
	}


	String panNoCompany;
	
	@JsonProperty("pan_no_company")
	public String getPanNoCompany() {
		return panNoCompany;
	}

	public void setPanNoCompany(String panNoCompany) {
		this.panNoCompany = panNoCompany;
	}

	@JsonProperty("lob")
	public String getLob() {
		return this.lob;
	}

	public void setLob(String lob) {
		this.lob = lob;
	}

	String lob;

	@JsonProperty("ppl")
	public String getPpl() {
		return this.ppl;
	}

	public void setPpl(String ppl) {
		this.ppl = ppl;
	}

	String ppl;

	@JsonProperty("pl")
	public String getPl() {
		return this.pl;
	}

	public void setPl(String pl) {
		this.pl = pl;
	}

	String pl;

	@JsonProperty("usage")
	public String getUsage() {
		return this.usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	String usage;

	@JsonProperty("vehicle_class")
	public String getVehicle_class() {
		return this.vehicle_class;
	}

	public void setVehicle_class(String vehicle_class) {
		this.vehicle_class = vehicle_class;
	}

	String vehicle_class;

	@JsonProperty("vehicle_color")
	public Object getVehicle_color() {
		return this.vehicle_color;
	}

	public void setVehicle_color(Object vehicle_color) {
		this.vehicle_color = vehicle_color;
	}

	Object vehicle_color;

	@JsonProperty("emission_norms")
	public Object getEmission_norms() {
		return this.emission_norms;
	}

	public void setEmission_norms(Object emission_norms) {
		this.emission_norms = emission_norms;
	}

	Object emission_norms;

	@JsonProperty("organization")
	public String getOrganization() {
		return this.organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	String organization;

	@JsonProperty("sales_person_dse")
	public String getSales_person_dse() {
		return this.sales_person_dse;
	}

	public void setSales_person_dse(String sales_person_dse) {
		this.sales_person_dse = sales_person_dse;
	}

	String sales_person_dse;

	@JsonProperty("sale_person_dsm")
	public String getSale_person_dsm() {
		return this.sale_person_dsm;
	}

	public void setSale_person_dsm(String sale_person_dsm) {
		this.sale_person_dsm = sale_person_dsm;
	}

	String sale_person_dsm;


	String dateOfBirth;

	@JsonProperty("date_of_birth")
	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}


	String customerCategory;

	@JsonProperty("customer_category")
	public String getCustomerCategory() {
		return customerCategory;
	}

	public void setCustomerCategory(String customerCategory) {
		this.customerCategory = customerCategory;
	}


	String customerSubcategory;
	
	@JsonProperty("customer_subcategory")
	public String getCustomerSubcategory() {
		return customerSubcategory;
	}

	public void setCustomerSubcategory(String customerSubcategory) {
		this.customerSubcategory = customerSubcategory;
	}

	@JsonProperty("loandetails_repayable_in_months")
	public Object getLoandetails_repayable_in_months() {
		return this.loandetails_repayable_in_months;
	}

	public void setLoandetails_repayable_in_months(Object loandetails_repayable_in_months) {
		this.loandetails_repayable_in_months = loandetails_repayable_in_months;
	}

	Object loandetails_repayable_in_months;


	String repaymentMode;
	

	@JsonProperty("repayment_mode")
	public String getRepaymentMode() {
		return repaymentMode;
	}

	public void setRepaymentMode(String repaymentMode) {
		this.repaymentMode = repaymentMode;
	}

	String partydetailsMaritalstatus;

	@JsonProperty("partydetails_maritalstatus")
	public String getPartydetailsMaritalstatus() {
		return partydetailsMaritalstatus;
	}

	public void setPartydetailsMaritalstatus(String partydetailsMaritalstatus) {
		this.partydetailsMaritalstatus = partydetailsMaritalstatus;
	}


	String partydetailsAnnualincome;

	@JsonProperty("partydetails_annualincome")
	public String getPartydetailsAnnualincome() {
		return partydetailsAnnualincome;
	}

	public void setPartydetailsAnnualincome(String partydetailsAnnualincome) {
		this.partydetailsAnnualincome = partydetailsAnnualincome;
	}

	@JsonProperty("quantity")
	public String getQuantity() {
		return this.quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	String quantity;

	@JsonProperty("vc_number")
	public String getVc_number() {
		return this.vc_number;
	}

	public void setVc_number(String vc_number) {
		this.vc_number = vc_number;
	}

	String vc_number;

	@JsonProperty("product_id")
	public String getProduct_id() {
		return this.product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	String product_id;

	@JsonProperty("organization_code")
	public String getOrganization_code() {
		return this.organization_code;
	}

	public void setOrganization_code(String organization_code) {
		this.organization_code = organization_code;
	}

	String organization_code;

	@JsonProperty("division_id")
	public String getDivision_id() {
		return this.division_id;
	}

	public void setDivision_id(String division_id) {
		this.division_id = division_id;
	}

	String division_id;

	@JsonProperty("financier_id")
	public String getFinancier_id() {
		return this.financier_id;
	}

	public void setFinancier_id(String financier_id) {
		this.financier_id = financier_id;
	}

	String financier_id;

	@JsonProperty("sales_person_dse_id")
	public String getSales_person_dse_id() {
		return this.sales_person_dse_id;
	}

	public void setSales_person_dse_id(String sales_person_dse_id) {
		this.sales_person_dse_id = sales_person_dse_id;
	}

	String sales_person_dse_id;


	String idDescription;

	@JsonProperty("id_description")
	public String getIdDescription() {
		return idDescription;
	}

	public void setIdDescription(String idDescription) {
		this.idDescription = idDescription;
	}


	String idIssueDate;
	

	@JsonProperty("id_issue_date")
	public String getIdIssueDate() {
		return idIssueDate;
	}

	public void setIdIssueDate(String idIssueDate) {
		this.idIssueDate = idIssueDate;
	}



	String idExpiryDate;

	@JsonProperty("id_expiry_date")
	public String getIdExpiryDate() {
		return idExpiryDate;
	}

	public void setIdExpiryDate(String idExpiryDate) {
		this.idExpiryDate = idExpiryDate;
	}

	String idType;

	@JsonProperty("id_type")
	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
	}

	@JsonProperty("taluka")
	public String getTaluka() {
		return this.taluka;
	}

	public void setTaluka(String taluka) {
		this.taluka = taluka;
	}

	String taluka;


	String finOccupationInYears;

	@JsonProperty("fin_occupation_in_years")
	public String getFinOccupationInYears() {
		return finOccupationInYears;
	}

	public void setFinOccupationInYears(String finOccupationInYears) {
		this.finOccupationInYears = finOccupationInYears;
	}

	String finOccupation;

	@JsonProperty("fin_occupation")
	public String getFinOccupation() {
		return finOccupation;
	}

	public void setFinOccupation(String finOccupation) {
		this.finOccupation = finOccupation;
	}

	@JsonProperty("ltv")
	public String getLtv() {
		return this.ltv;
	}

	public void setLtv(String ltv) {
		this.ltv = ltv;
	}

	String ltv;


	String typeOfProperty;

	@JsonProperty("type_of_property")
	public String getTypeOfProperty() {
		return typeOfProperty;
	}

	public void setTypeOfProperty(String typeOfProperty) {
		this.typeOfProperty = typeOfProperty;
	}


	String customerType;


	@JsonProperty("customer_type")
	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	String accountTahsilTaluka;

	@JsonProperty("account_tahsil_taluka")
	public String getAccountTahsilTaluka() {
		return accountTahsilTaluka;
	}

	public void setAccountTahsilTaluka(String accountTahsilTaluka) {
		this.accountTahsilTaluka = accountTahsilTaluka;
	}

	String relationshipType;

	@JsonProperty("relationship_type")
	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}


	String coapplicantPanNoIndiviual;
	
	@JsonProperty("coapplicant_pan_no_indiviual")
	public String getCoapplicantPanNoIndiviual() {
		return coapplicantPanNoIndiviual;
	}

	public void setCoapplicantPanNoIndiviual(String coapplicantPanNoIndiviual) {
		this.coapplicantPanNoIndiviual = coapplicantPanNoIndiviual;
	}


	String coapplicantPincode;

	@JsonProperty("coapplicant_pincode")
	public String getCoapplicantPincode() {
		return coapplicantPincode;
	}

	public void setCoapplicantPincode(String coapplicantPincode) {
		this.coapplicantPincode = coapplicantPincode;
	}

	String coapplicantLastName;

	@JsonProperty("coapplicant_last_name")
	public String getCoapplicantLastName() {
		return coapplicantLastName;
	}

	public void setCoapplicantLastName(String coapplicantLastName) {
		this.coapplicantLastName = coapplicantLastName;
	}


	String coapplicantMobileNo;

	@JsonProperty("coapplicant_mobile_no")
	public String getCoapplicantMobileNo() {
		return coapplicantMobileNo;
	}

	public void setCoapplicantMobileNo(String coapplicantMobileNo) {
		this.coapplicantMobileNo = coapplicantMobileNo;
	}

	String coapplicantFirstName;
	

	@JsonProperty("coapplicant_first_name")
	public String getCoapplicantFirstName() {
		return coapplicantFirstName;
	}

	public void setCoapplicantFirstName(String coapplicantFirstName) {
		this.coapplicantFirstName = coapplicantFirstName;
	}


	String coapplicantAddress1;
	
	@JsonProperty("coapplicant_address1")
	public String getCoapplicantAddress1() {
		return coapplicantAddress1;
	}

	public void setCoapplicantAddress1(String coapplicantAddress1) {
		this.coapplicantAddress1 = coapplicantAddress1;
	}


	String coapplicantAddress2;

	@JsonProperty("coapplicant_address2")
	public String getCoapplicantAddress2() {
		return coapplicantAddress2;
	}

	public void setCoapplicantAddress2(String coapplicantAddress2) {
		this.coapplicantAddress2 = coapplicantAddress2;
	}

	String coapplicantCityTownVillage;
	

	@JsonProperty("coapplicant_city_town_village")
	public String getCoapplicantCityTownVillage() {
		return coapplicantCityTownVillage;
	}

	public void setCoapplicantCityTownVillage(String coapplicantCityTownVillage) {
		this.coapplicantCityTownVillage = coapplicantCityTownVillage;
	}

	String coapplicantDateOfBirth;
	
	@JsonProperty("coapplicant_date_of_birth")
	public String getCoapplicantDateOfBirth() {
		return coapplicantDateOfBirth;
	}

	public void setCoapplicantDateOfBirth(String coapplicantDateOfBirth) {
		this.coapplicantDateOfBirth = coapplicantDateOfBirth;
	}

	@JsonProperty("financier_case_status")
	public String getFinancier_case_status() {
		return this.financier_case_status;
	}

	public void setFinancier_case_status(String financier_case_status) {
		this.financier_case_status = financier_case_status;
	}

	String financier_case_status;

	@JsonProperty("financier_name")
	public String getFinancier_name() {
		return this.financier_name;
	}

	public void setFinancier_name(String financier_name) {
		this.financier_name = financier_name;
	}

	String financier_name;

	@JsonProperty("branch_name")
	public String getBranch_name() {
		return this.branch_name;
	}

	public void setBranch_name(String branch_name) {
		this.branch_name = branch_name;
	}

	String branch_name;

	@JsonProperty("branch_id")
	public String getBranch_id() {
		return this.branch_id;
	}

	public void setBranch_id(String branch_id) {
		this.branch_id = branch_id;
	}

	String branch_id;

	@JsonProperty("bdm_name")
	public String getBdm_name() {
		return this.bdm_name;
	}

	public void setBdm_name(String bdm_name) {
		this.bdm_name = bdm_name;
	}

	String bdm_name;

	@JsonProperty("bdm_mobile_no")
	public String getBdm_mobile_no() {
		return this.bdm_mobile_no;
	}

	public void setBdm_mobile_no(String bdm_mobile_no) {
		this.bdm_mobile_no = bdm_mobile_no;
	}

	String bdm_mobile_no;

	@JsonProperty("bdm_id")
	public String getBdm_id() {
		return this.bdm_id;
	}

	public void setBdm_id(String bdm_id) {
		this.bdm_id = bdm_id;
	}

	String bdm_id;

	@JsonProperty("financier_case_id")
	public int getFinancier_case_id() {
		return this.financier_case_id;
	}

	public void setFinancier_case_id(int financier_case_id) {
		this.financier_case_id = financier_case_id;
	}

	int financier_case_id;
}
