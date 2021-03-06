
CREATE TABLE `fs_mfi_applicant_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) DEFAULT NULL,
  `proposal_mapping_id` bigint(20) DEFAULT NULL,
  `aadhar_number` varchar(50) DEFAULT NULL,
  `name_as_per_aadharCard` varchar(50) DEFAULT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `middle_name` varchar(50) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `mobile` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `gender_id` int(11) DEFAULT NULL,
  `marital_status_id` int(11) DEFAULT NULL,
  `current_district` varchar(50) DEFAULT NULL,
  `aadhar_district` varchar(50) DEFAULT NULL,
  `current_house` varchar(50) DEFAULT NULL,
  `aadhar_house` varchar(50) DEFAULT NULL,
  `current_landmark` varchar(200) DEFAULT NULL,
  `aadhar_landmark` varchar(200) DEFAULT NULL,
  `current_location` varchar(50) DEFAULT NULL,
  `aadhar_location` varchar(50) DEFAULT NULL,
  `current_state` varchar(50) DEFAULT NULL,
  `aadhar_state` varchar(50) DEFAULT NULL,
  `current_street` varchar(50) DEFAULT NULL,
  `aadhar_street` varchar(50) DEFAULT NULL,
  `current_vtc` varchar(50) DEFAULT NULL,
  `aadhar_vtc` varchar(50) DEFAULT NULL,
  `aadhar_subdist` varchar(50) DEFAULT NULL,
  `current_subdist` varchar(50) DEFAULT NULL,
  `aadhar_po` varchar(50) DEFAULT NULL,
  `current_po` varchar(50) DEFAULT NULL,
  `aadhar_care_of` varchar(50) DEFAULT NULL,
  `address_pincode` varchar(50) DEFAULT NULL,
  `address_same_as_aadhar` bit(1) DEFAULT b'0',
  `aadhar_pincode` varchar(50) DEFAULT NULL,
  `address_proof_type` int(11) DEFAULT NULL,
  `father_name` varchar(50) DEFAULT NULL,
  `mother_name` varchar(50) DEFAULT NULL,
  `spouse_name` varbinary(50) DEFAULT NULL,
  `spouse_birth_date` date DEFAULT NULL,
  `spouse_mobile` varchar(50) DEFAULT NULL,
  `no_dependent` int(11) DEFAULT NULL,
  `nominee_name` varchar(50) DEFAULT NULL,
  `relation_with_nominee_id` int(11) DEFAULT NULL,
  `nominee_address` varchar(200) DEFAULT NULL,
  `nominee_pincode` varchar(50) DEFAULT NULL,
  `religion` int(11) DEFAULT NULL,
  `education_qualification` int(11) DEFAULT NULL,
  `land_holding` double(19,2) DEFAULT NULL,
  `name_of_firm` varchar(50) DEFAULT NULL,
  `business_type` int(11) DEFAULT NULL,
  `house_type` int(11) DEFAULT NULL,
  `loan_type` int(11) DEFAULT NULL,
  `loan_purpose` int(11) DEFAULT NULL,
  `loan_amount_required` double(19,2) DEFAULT NULL,
  `cost_of_project` double(19,2) DEFAULT NULL,
  `cost_of_equipment` double(19,2) DEFAULT NULL,
  `working_cap_of_equipment` double(19,2) DEFAULT NULL,
  `total_cost_equipment` double(19,2) DEFAULT NULL,
  `promoter_contribution` double(19,2) DEFAULT NULL,
  `loan_required_from_sidbi` double(19,2) DEFAULT NULL,
  `total_mean_finance` double(19,2) DEFAULT NULL,
  `total_cash_flow` double(19,2) DEFAULT NULL,
  `repayment_frequency` int(11) DEFAULT NULL,
  `insurence_required` bit(1) DEFAULT b'0',
  `insurence_company_name` varchar(50) DEFAULT NULL,
  `insurence_premium` double(19,2) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `modified_by` bigint(20) DEFAULT NULL,
  `modified_date` date DEFAULT NULL,
  `remarks` varchar(200) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `is_personal_details_filled` bit(1) DEFAULT b'0',
  `is_family_details_filled` bit(1) DEFAULT b'0',
  `is_nominee_details_filled` bit(1) DEFAULT b'0',
  `is_acadamic_details_filled` bit(1) DEFAULT b'0',
  `is_bank_details_filled` bit(1) DEFAULT b'0',
  `is_account_details_filled` bit(1) DEFAULT b'0',
  `is_existing_loan_details_filled` bit(1) DEFAULT b'0',
  `is_income_details_filled` bit(1) DEFAULT b'0',
  `is_family_income_filled` bit(1) DEFAULT b'0',
  `is_family_expense_filled` bit(1) DEFAULT b'0',
  `is_expected_income_filled` bit(1) DEFAULT b'0',
  `is_ppi_filled` bit(1) DEFAULT b'0',
  `is_project_details_filled` bit(1) DEFAULT b'0',
  `is_apply_loan_filled` bit(1) DEFAULT b'0',
  `is_cost_project_filled` bit(1) DEFAULT b'0',
  `is_mean_finance_filled` bit(1) DEFAULT b'0',
  `is_cash_flow_details_filled` bit(1) DEFAULT b'0',
  `is_assets_details_filled` bit(1) DEFAULT b'0',
  `is_current_assets_filled` bit(1) DEFAULT b'0',
  `is_fixed_assets_filled` bit(1) DEFAULT b'0',
  `is_currnt_liability_filled` bit(1) DEFAULT b'0',
  `is_repayment_details_filled` bit(1) DEFAULT b'0',
  `is_consent_form_filled` bit(1) DEFAULT b'0',
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
)

ALTER TABLE `loan_application`.`fs_retail_co_applicant_details` ADD COLUMN `is_owned_prop` BIT(1) DEFAULT b'0' NULL;
ALTER TABLE `loan_application`.`fs_loan_application_master` ADD COLUMN `is_owned_prop` BIT(1) DEFAULT b'0' NULL;