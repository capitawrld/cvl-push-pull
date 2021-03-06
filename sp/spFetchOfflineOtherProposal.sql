DELIMITER $$

USE `loan_application`$$

DROP PROCEDURE IF EXISTS `spFetchOfflineOtherProposal`$$

CREATE DEFINER=`dbsidbi`@`%` PROCEDURE `spFetchOfflineOtherProposal`(IN userId INT)
BEGIN
    
	DECLARE vb_role_id INT;
	DECLARE vb_branch_id INT;
	DECLARE vb_org_id INT;
	DECLARE vb_business_type_id INT;
	SELECT `user_role_id`,`branch_id`,`user_org_id`,last_access_business_type_id INTO vb_role_id,vb_branch_id,vb_org_id,vb_business_type_id FROM users.`users`  WHERE `user_id` = userId;
	
	IF (vb_business_type_id = 1) THEN -- MSME
		SET @vb_query = CONCAT("SELECT CAST(JSON_ARRAYAGG(JSON) AS CHAR) FROM (SELECT JSON_OBJECT(
				'applicationId',ine.`application_id`,
				'organisationName',coapp.`organisation_name`,
				'pan',IF(coapp.`pan` IS NULL,NULL,(CAST((AES_DECRYPT(UNHEX(coapp.`pan`),'C@p!ta@W0rld#AES')) AS CHAR(100)))),
				'gstin',con.`gstin`,
				'userId',con.`user_id`,
				'loanAmount',coprim.`loan_amount`,
				'branchName',branch.`name`,
				'branchCode',branch.`code`,
				'branchAddress',branch.`street_name`,
				'reason',ine.`reason`,
				'modifiedDate',DATE_FORMAT(ine.`modified_date`, '%d/%m/%Y %H:%i:%S'),
				'campaignCode',camp.`code`,
				'isCampaignUser',IF(camp.id IS NULL,'Market Place','Bank Specific'),'branchId',branch.`id`,
				'mobile',u.`mobile`,
				'email',u.`email`,
				'status',ine.`status` ) AS JSON
				FROM loan_application.`ineligible_proposal_details` ine 
				LEFT JOIN loan_application.`fs_corporate_applicant_details` coapp ON coapp.`application_id` = ine.`application_id` and coapp.`proposal_mapping_id` is NULL
				LEFT JOIN loan_application.`fs_corporate_primary_details` coprim ON coprim.`application_id` = ine.`application_id`
				LEFT JOIN connect.`connect_log` con ON con.`application_id` = ine.`application_id` and con.`proposal_id` is NULL
				LEFT JOIN users.`campaign_details` camp ON camp.`user_id` = con.`user_id` AND camp.`is_active` = TRUE
				LEFT JOIN users.`branch_master` branch ON branch.`id` = ine.`branch_id`
				LEFT JOIN users.`users` u ON  u.`user_id` = con.`user_id`
				WHERE ine.`user_org_id` =", vb_org_id," AND ine.`status` IN (5,6,7,8,9) AND ine.business_type_id = 1 ");
		
		IF (vb_role_id = 6 OR vb_role_id=9) THEN -- BO (GET BASED ON BRANCH LEVEL)
			SET @vb_query = CONCAT(@vb_query," AND ine.`branch_id` = ",vb_branch_id, " GROUP BY ine.`application_id` ) AS main");
		ELSEIF (vb_role_id = 12) THEN -- SMECC
			SET @vb_query = CONCAT(@vb_query," AND ine.`branch_id` IN (SELECT `branch_master_id` FROM users.`user_branch_mapping` WHERE `user_id` = " , userId , " AND `is_active` = TRUE) GROUP BY ine.`application_id` ) AS main");
		ELSEIF (vb_role_id = 11 OR vb_role_id = 5) THEN -- AdminChecker and HO (GET BASED ON BANK LEVEL)
			SET @vb_query = CONCAT(@vb_query," GROUP BY ine.`application_id` ) AS main");
		ELSE 
			SET @vb_query = "select '' from dual";
		END IF; 
		-- select @vb_query from dual;
		PREPARE stmt1 FROM @vb_query;
		EXECUTE stmt1;
		
	ELSEIF(vb_business_type_id= 3) THEN -- HL 'pan',IF(reapp.`pan` IS NULL,NULL,(CAST((AES_DECRYPT(UNHEX(reapp.`pan`),'C@p!ta@W0rld#AES')) AS CHAR(100)))),

		SET @vb_query = CONCAT("SELECT CAST(JSON_ARRAYAGG(JSON) AS CHAR) FROM (SELECT JSON_OBJECT(
				'applicationId',reapp.`application_id`,
				'name',CONCAT(reapp.first_name,' ',reapp.middle_name,' ',reapp.last_name),
				'pan',reapp.`pan`,				
				'userId',con.`user_id`,
				'loanAmount',reapp.`loan_amount_required`,
				'branchName',branch.`name`,
				'branchCode',branch.`code`,
				'branchAddress',branch.`street_name`,
				'reason',ine.`reason`,
				'modifiedDate',DATE_FORMAT(ine.`modified_date`, '%d/%m/%Y %H:%i:%S'),
				'campaignCode',camp.`code`,
				'isCampaignUser',IF(camp.id IS NULL,'Market Place','Bank Specific'),'branchId',branch.`id`,
				'mobile',u.`mobile`,
				'email',u.`email`,
				'status',ine.`status` ) AS JSON
				FROM loan_application.`ineligible_proposal_details` ine 
				LEFT JOIN `loan_application`.`fs_retail_applicant_details` reapp ON reapp.application_id = ine.application_id 
				LEFT JOIN connect.`connect_log` con ON con.`application_id` = ine.`application_id` and con.`proposal_id` is NULL
				LEFT JOIN users.`campaign_details` camp ON camp.`user_id` = con.`user_id` AND camp.`is_active` = TRUE
				LEFT JOIN users.`branch_master` branch ON branch.`id` = ine.`branch_id`
				LEFT JOIN users.`users` u ON  u.`user_id` = con.`user_id`
				WHERE ine.`user_org_id` =", vb_org_id," AND ine.`status` IN (5,6,7,8,9) AND ine.business_type_id = 3 ");	
		
		IF (vb_role_id = 6 OR vb_role_id=9) THEN -- BO (GET BASED ON BRANCH LEVEL)
			SET @vb_query = CONCAT(@vb_query," AND ine.`branch_id` = ",vb_branch_id, " GROUP BY ine.`application_id` ) AS main");
		-- ELSEIF (vb_role_id = 12) THEN -- SMECC
		--      SET @vb_query = CONCAT(@vb_query," AND ine.`branch_id` IN (SELECT `branch_master_id` FROM users.`user_branch_mapping` WHERE `user_id` = " , userId , " AND `is_active` = TRUE) GROUP BY ine.`application_id` ) AS main");
		ELSEIF (vb_role_id = 11 OR vb_role_id = 5) THEN -- AdminChecker and HO (GET BASED ON BANK LEVEL)
			SET @vb_query = CONCAT(@vb_query," GROUP BY ine.`application_id` ) AS main");
		ELSE 
			SET @vb_query = "select '' from dual";
		END IF; 
		-- select @vb_query from dual;
		PREPARE stmt1 FROM @vb_query;
		EXECUTE stmt1;
	ELSEIF(vb_business_type_id= 5) THEN -- PL 'pan',IF(reapp.`pan` IS NULL,NULL,(CAST((AES_DECRYPT(UNHEX(reapp.`pan`),'C@p!ta@W0rld#AES')) AS CHAR(100)))),
	
		SET @vb_query = CONCAT("SELECT CAST(JSON_ARRAYAGG(JSON) AS CHAR) FROM (SELECT JSON_OBJECT(
				'applicationId',reapp.`application_id`,
				'name',CONCAT(reapp.first_name,' ',reapp.middle_name,' ',reapp.last_name),
				'pan',reapp.`pan`,
				'userId',con.`user_id`,
				'loanAmount',reapp.`loan_amount_required`,
				'branchName',branch.`name`,
				'branchCode',branch.`code`,
				'branchAddress',branch.`street_name`,
				'reason',ine.`reason`,
				'modifiedDate',DATE_FORMAT(ine.`modified_date`, '%d/%m/%Y %H:%i:%S'),
				'campaignCode',camp.`code`,
				'isCampaignUser',IF(camp.id IS NULL,'Market Place','Bank Specific'),'branchId',branch.`id`,
				'mobile',u.`mobile`,
				'loanTypeId',con.loan_type_id,
				'businessType',con.business_type_id,
				'coAppIds',(SELECT JSON_ARRAYAGG(id) FROM `loan_application`.`fs_retail_co_applicant_details` WHERE `application_id` = reapp.`application_id`),
				'email',u.`email`,
				'status',ine.`status` ) AS JSON
				FROM loan_application.`ineligible_proposal_details` ine 
				LEFT JOIN `loan_application`.`fs_retail_applicant_details` reapp ON reapp.application_id = ine.application_id 
				LEFT JOIN connect.`connect_log` con ON con.`application_id` = ine.`application_id` and con.`proposal_id` is NULL
				LEFT JOIN users.`campaign_details` camp ON camp.`user_id` = con.`user_id` AND camp.`is_active` = TRUE
				LEFT JOIN users.`branch_master` branch ON branch.`id` = ine.`branch_id`
				LEFT JOIN users.`users` u ON  u.`user_id` = con.`user_id`
				WHERE ine.`user_org_id` =", vb_org_id," AND ine.`status` IN (5,6,7,8,9) AND ine.business_type_id = 5 ");	
	
		IF (vb_role_id = 6 OR vb_role_id=9) THEN -- BO (GET BASED ON BRANCH LEVEL)
			SET @vb_query = CONCAT(@vb_query," AND ine.`branch_id` = ",vb_branch_id, " GROUP BY ine.`application_id` ) AS main");
		-- ELSEIF (vb_role_id = 12) THEN -- SMECC
		--      SET @vb_query = CONCAT(@vb_query," AND ine.`branch_id` IN (SELECT `branch_master_id` FROM users.`user_branch_mapping` WHERE `user_id` = " , userId , " AND `is_active` = TRUE) GROUP BY ine.`application_id` ) AS main");
		ELSEIF (vb_role_id = 11 OR vb_role_id = 5) THEN -- AdminChecker and HO (GET BASED ON BANK LEVEL)
			SET @vb_query = CONCAT(@vb_query," GROUP BY ine.`application_id` ) AS main");
		ELSE 
			SET @vb_query = "select '' from dual";
		END IF; 
		-- select @vb_query from dual;
		PREPARE stmt1 FROM @vb_query;
		EXECUTE stmt1;
	ELSE
		SET @vb_query = "select '' from dual";
	END IF;    	
    END$$

DELIMITER ;