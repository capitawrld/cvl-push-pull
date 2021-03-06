CREATE TABLE `fs_mfi_current_financial_arrangements_details` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `application_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `financial_institution_name` VARCHAR(100) DEFAULT '',
  `amount` DOUBLE DEFAULT NULL,
  `created_date` DATETIME DEFAULT NULL,
  `modified_date` DATETIME DEFAULT NULL,
  `created_by` BIGINT(20) DEFAULT NULL,
  `modified_by` BIGINT(20) DEFAULT NULL,
  `is_active` BIT(1) DEFAULT NULL,
  `loan_date` DATETIME DEFAULT NULL,
  `loan_type` VARCHAR(255) DEFAULT NULL,
  `outstanding_amount` DOUBLE DEFAULT NULL,
  `bureau_outstanding_amount` DOUBLE DEFAULT NULL,
  `banker_outstanding_amount` DOUBLE DEFAULT NULL,
  `emi` DOUBLE(19,2) DEFAULT NULL,
  `is_bureau_emi` BIT(1) DEFAULT NULL,
  `bureau_or_calculated_emi` DOUBLE DEFAULT NULL,
  `reported_date` DATETIME DEFAULT NULL,    
  `is_manually_added` BIT(1) DEFAULT NULL,
  `provider` INT(10) DEFAULT NULL,
  `applicant_id` BIGINT(20) DEFAULT NULL,
  `is_loan_considered` BIT(1) DEFAULT NULL,
  `other_institution_name` VARCHAR(100),
  PRIMARY KEY (`id`),
  KEY `application_id` (`application_id`),
  CONSTRAINT `fs_mfi_current_financial_arrangements_details_ibfk_1` FOREIGN KEY (`application_id`) REFERENCES `fs_loan_application_master` (`application_id`)
);