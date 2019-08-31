DELIMITER $$

USE `loan_application`$$

DROP PROCEDURE IF EXISTS `listUserApplication`$$

CREATE DEFINER=`dbsidbi`@`%` PROCEDURE `listUserApplication`(IN userId BIGINT,OUT result LONGTEXT)
BEGIN
-- DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
	
	
	SET @userId := userId;
	
	SET @jsonMainString = '[';
	
	
	-- --------------------------------------------------------------
	
	SET @getApplicationIdCountQuery:= CONCAT('SELECT COUNT(DISTINCT(application_id)) INTO @applicationIdCount FROM `connect`.`connect_log` WHERE `user_id`= ',@userId,' AND is_active=TRUE');
	PREPARE stmt FROM @getApplicationIdCountQuery ;
	EXECUTE stmt ;
	
	
	DROP TEMPORARY TABLE IF EXISTS `application_temp_tbl`;
	CREATE TEMPORARY TABLE  application_temp_tbl(id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
						`applicationId` BIGINT											     
						);
						
	SET @k=0;
	WHILE @k<@applicationIdCount DO 				
		
		SET @getApplicationIdQuery:= CONCAT('SELECT DISTINCT(application_id) INTO @aId FROM `connect`.`connect_log` WHERE `user_id`= ',@userId,' AND is_active=TRUE ORDER BY created_date DESC LIMIT ',@k,',1');
		PREPARE stmt FROM @getApplicationIdQuery ;
		EXECUTE stmt ;
		
		INSERT INTO application_temp_tbl(applicationId) VALUES(@aId); 
		
		
		-- ----------------------------------------------------------------------------------------------------------------------------------	
		
		SET @jsonString := CONCAT('{ "applicationId" : ',@aId,', "loanList" :[');
	
	
		SET @getConnectCountQuery:= CONCAT('SELECT COUNT(*) INTO @n FROM `connect`.`connect_log` WHERE `user_id`= ',@userId,' AND `application_id`= ',@aId,' AND is_active=TRUE');
		PREPARE stmt FROM @getConnectCountQuery ;
		EXECUTE stmt ;
		
		
		
		
		DROP TEMPORARY TABLE IF EXISTS `connect_temp_tbl`;
		CREATE TEMPORARY TABLE  connect_temp_tbl (id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
							`applicationId` VARCHAR(100),
							`stageId` VARCHAR(100),
							`status` VARCHAR(100),
							`businessTypeId` INT,
							`companyName` VARCHAR(200),
							`gstIn` VARCHAR(100),
							`proposalId` VARCHAR(100),
							`statusString` VARCHAR(100),
							`createdDate` DATETIME,
							`modifiedDate` DATETIME,
							`proposalStage` INT,
							`tenure` INT,            
							`elLoanAmount` DOUBLE,       
							`productId` INT,        
							`applicationCode` VARCHAR(50),  
							`orgId` INT,            
							`reqLoanAmount`  DOUBLE,
							`sanctionAmount`  DOUBLE,
							`sanctionDate` DATETIME,
							`disbursedAmount` DOUBLE,
							`disCreatedDate` DATETIME,
							`offLoanAmt` DOUBLE,
							`offElAmount` DOUBLE,
							`offUorgId` INT,
							`offBranchId` INT,
							`offCrDate` DATETIME,
							`offMoDate` DATETIME,
							`offIsSanction` BOOLEAN,
							`offIsDisb` BOOLEAN,
							`offStatus` INT,
							`offReason` VARCHAR(200),
							`offReOpenRes` VARCHAR(200),
							`offLoanType` INT,
							`offReqLoanAmount`  DOUBLE,
							`applicationStage` VARCHAR(20),
							`purposeOfLoanId` INT,
							`OffproductId` INT
							);
		SET @notEligibleNSBCount = 0;
		SET @notEligibleSBCount = 0;
			
		SET @i=0;
			WHILE @i<@n DO 
			
			SET @statusString=NULL;
			SET @applicationStage= NULL;			
			
			SET @getConnectQuery:= CONCAT('SELECT application_id,business_type_id,stage_id,`status`,`gstin`,proposal_id,created_date,modified_date INTO @applicationId,@businessTypeId,@stageId,@status,@gstIn,@proposalId,@createdDate,@modifiedDate FROM `connect`.`connect_log` WHERE `user_id`=',@userId,'  AND `application_id`= ',@aId,' AND is_active=TRUE LIMIT ',@i,',1');
			PREPARE stmt FROM @getConnectQuery ;
			EXECUTE stmt ;
			-- SELECT @getConnectQuery;
			
			
			
			IF (@proposalId IS NOT NULL) THEN
			
				SET @companyNameQuery:= CONCAT('select organisation_name into @companyName from loan_application.fs_corporate_applicant_details where proposal_mapping_id=',@proposalId);
				PREPARE stmtCompanyName FROM @companyNameQuery;
				EXECUTE stmtCompanyName;
				
				IF (@businessTypeId = 3 OR @businessTypeId = 5 ) THEN
					SET @companyNameQuery:= CONCAT('select CONCAT(first_name," ",middle_name," ",last_name) into @companyName from loan_application.fs_retail_applicant_details where proposal_mapping_id=',@proposalId);
					PREPARE stmtCompanyName FROM @companyNameQuery;
					EXECUTE stmtCompanyName;
				END IF;
					
					
				-- SET @hasAlreadyApplied:= CONCAT('select count(application_id) into @hasAlApp from loan_application.fs_corporate_applicant_details  where application_id =',@applicationId,' and is_active=true',' and (organisation_name != NULL)');
				-- PREPARE stmtHasApp FROM @hasAlreadyApplied;
				-- EXECUTE stmtHasApp;
			
			END IF;
			
			
							     
			
			IF ((@stageId = 7 OR @stageId = 9 OR @stageId = 211 OR @stageId = 210 )  AND (@status = 3 OR @status = 7)) THEN
				IF (@status = 3) THEN
					SET @statusString = "InPrinciple";
				ELSEIF (@status = 7) THEN
					SET @statusString = "Expired";
				END IF;
				
				
				SET @proposalDetails:= CONCAT('SELECT proposal_status_id,`modified_date` INTO  @proposalStage,@proposalModifiedDate FROM loan_application.proposal_details WHERE id=',@proposalId);
				PREPARE stmtP FROM @proposalDetails;
				EXECUTE stmtP ;
				
					IF (@proposalStage = 2 OR @proposalStage = 3 OR @proposalStage = 4 OR @proposalStage = 5 OR @proposalStage = 11 OR @proposalStage = 13 ) THEN
				
						SET @inProgressAppData:=CONCAT('SELECT p.tenure,p.loan_amount,p.product_id,p.application_code,p.org_id,pr.loan_amount INTO @tenure,@elLoanAmt,@productId,@appCode,@orgId,@reqLoanAmt FROM loan_application.application_proposal_mapping p 
						RIGHT JOIN loan_application.fs_corporate_primary_details pr ON p.application_id=pr.application_id
						WHERE p.proposal_id=',@proposalId);
						PREPARE stmtInProgress FROM @inProgressAppData;
						EXECUTE stmtInProgress;
						
						IF (@businessTypeId = 3 OR @businessTypeId = 5) THEN
							SET @inProgressAppData:=CONCAT('SELECT DISTINCT p.tenure,p.loan_amount,p.product_id,p.application_code,p.org_id,pr.loan_amount_required INTO @tenure,@elLoanAmt,@productId,@appCode,@orgId,@reqLoanAmt FROM loan_application.application_proposal_mapping p 
							RIGHT JOIN loan_application.fs_retail_applicant_details pr ON p.application_id=pr.application_id WHERE p.proposal_id=',@proposalId);
							PREPARE stmtInProgress FROM @inProgressAppData;
							EXECUTE stmtInProgress;
						END IF;
						
					END IF;
					
					IF (@proposalStage = 5 OR @proposalStage = 11 OR @proposalStage = 13) THEN
				
						SET @sanctionAppData:=CONCAT('SELECT d.sanction_amount,d.created_date INTO @sanctionAmt,@sanctionDate FROM loan_application.sanction_detail d where d.is_active = true AND d.application_id=',@applicationId,' AND d.org_id=',@orgId);
					
						PREPARE stmtSanction FROM @sanctionAppData;
						EXECUTE stmtSanction;
						
					END IF;
					
					IF (@proposalStage = 11 OR @proposalStage = 13) THEN
				
					SET @disbursementAmountDetails:=CONCAT('SELECT SUM(d.`disbursed_amount`),d.created_date INTO @disAmt,@disDate FROM loan_application.`disbursement_detail` d where  d.is_active = true AND d.application_id=',@applicationId,' AND d.org_id=',@orgId);
					PREPARE stmtDisburse FROM @disbursementAmountDetails;
					EXECUTE stmtDisburse;
					
					END IF;
					
					
				
			-- inEligible
		
			ELSEIF ((@stageId = 4 AND (@status = 6 OR @status = 7)) OR (@stageId = 207 AND (@status = 6 OR @status = 7))) THEN
				
				SET @companyNameQuery:= CONCAT('select organisation_name into @companyName from loan_application.fs_corporate_applicant_details where application_id=',@applicationId,' limit 1');
				PREPARE stmtCompanyName FROM @companyNameQuery;
				EXECUTE stmtCompanyName;
				
				SET @offlinedataP:= CONCAT('SELECT purpose_of_loan_id,loan_amount INTO @OffproductId,@offLoanAmt FROM loan_application.fs_corporate_primary_details WHERE application_id=',@applicationId);
					PREPARE stmtOffLineP FROM @offlinedataP;
					EXECUTE stmtOffLineP;
				
				IF (@businessTypeId = 3 OR @businessTypeId = 5) THEN
					SET @companyNameQuery:= CONCAT('select CONCAT(first_name," ",middle_name," ",last_name),loan_amount_required into @companyName,@offReqLoanAmount from loan_application.fs_retail_applicant_details where application_id=',@applicationId,' limit 1');
					PREPARE stmtCompanyName FROM @companyNameQuery;
					EXECUTE stmtCompanyName;
					
					
					SET @offlinedataP:= CONCAT('SELECT fp_product_id,el_amount INTO @OffproductId,@offLoanAmt FROM loan_application.proposal_details WHERE application_id=',@applicationId);
					PREPARE stmtOffLineP FROM @offlinedataP;
					EXECUTE stmtOffLineP;
				END IF;
				
				
					
				
				SET @offlinedata:= CONCAT('SELECT `user_org_id`,`branch_id`,`loan_amount`,`created_date`,`modified_date`,`is_sanctioned`,`is_disbursed`,`status`,`reason`,`reopen_reason` INTO 
									@offUorgId,@offBranchId,@offElAmount,@offCrDate,@offMoDate,@offIsSanction,@offIsDisb,@offStatus,@offReason,@offReOpenRes	
									FROM loan_application.`ineligible_proposal_details` WHERE is_active = true and application_id=',@applicationId);
				PREPARE stmtOffLine FROM @offlinedata;
				EXECUTE stmtOffLine;
				
				
				IF (@offUorgId IS NULL AND @offBranchId IS NULL) THEN
						
					IF (@status = 7) THEN
						SET @statusString = "Expired";
					ELSE
						SET @statusString = "InEligibleNSB";
					END IF;	
					
					SET @notEligibleNSBCount= @notEligibleNSBCount + 1;
					
					SET @offlinedataP:= CONCAT('SELECT purpose_of_loan_id,loan_amount INTO @offLoanType,@offLoanAmt FROM loan_application.fs_corporate_primary_details WHERE application_id=',@applicationId);
					PREPARE stmtOffLineP FROM @offlinedataP;
					EXECUTE stmtOffLineP;
					
					IF (@businessTypeId = 3 OR @businessTypeId = 5) THEN
						SET @offlinedataP:= CONCAT('SELECT fp_product_id,el_amount INTO @OffproductId,@offLoanAmt FROM loan_application.proposal_details WHERE application_id=',@applicationId);
						PREPARE stmtOffLineP FROM @offlinedataP;
						EXECUTE stmtOffLineP;
					END IF;
					
				ELSE 
					IF (@status = 7) THEN
						SET @statusString = "Expired";
					ELSE
						SET @statusString = "InEligibleSB";
					END IF;	
					
					SET @notEligibleSBCount= @notEligibleSBCount + 1;
								
					SET @offlineSanctionAppData:=CONCAT('SELECT d.sanction_amount,d.created_date INTO @offlineSanctionAmt,@offlineSanctionDate FROM loan_application.sanction_detail d where d.is_active = true AND d.application_id=',@applicationId,' AND d.org_id=',@offUorgId);
					
					PREPARE offlineStmtSanction FROM @offlineSanctionAppData;
					EXECUTE offlineStmtSanction;
					
					SET @offlineDisbursementAmountDetails:=CONCAT('SELECT SUM(d.`disbursed_amount`),d.created_date INTO @offlineDisAmt,@offlineDisDate FROM loan_application.`disbursement_detail` d where  d.is_active = true AND d.application_id=',@applicationId);
					
					PREPARE offlineStmtDisburse FROM @offlineDisbursementAmountDetails;
					EXECUTE offlineStmtDisburse;
													
				END IF;				
				
			
			ELSEIF(((@stageId != 7 AND  @stageId != 9) OR (@stageId != 211 AND @stageId != 210)) AND (@status IN (1,2,3,4,7))) THEN
		
				IF (@status = 7) THEN
					SET @statusString = "Expired";
				ELSE 
					SET @statusString = "InProgress";
				END IF;
				
 				SET @inProgresAppData:= CONCAT('SELECT c.business_type_id,c.stage_id,c.status,c.proposal_id INTO @businessTypeId,@stageId,@statusId,@proposalId FROM connect.connect_log c 
								WHERE c.application_id=',@applicationId ,' and c.proposal_id IS NULL AND status IN (1,2,3,4) AND stage_id NOT IN (7,9)');
  				
  				PREPARE stmtInProgress FROM @inProgresAppData;
  				EXECUTE stmtInProgress;
  				
				
  				IF (@stageId = 8 OR @stageId = 201) THEN 
				SET @applicationStage= "Mcq Page";
				END IF;
  				
  				IF (@stageId = 0) THEN 
				SET @applicationStage= "GST";
				END IF;
				
				IF (@stageId = 202) THEN 
				SET @applicationStage= "AADHAR";
				END IF;
				
				
				IF (@stageId != 8 OR @stageId != 0 OR @stageId != 201 OR @stageId != 202) THEN
					
					SET @firstStageData:= CONCAT('select a.organisation_name,p.purpose_of_loan_id,p.loan_amount into @companyName,@purposeOfLoanId,@reqLoanAmount from loan_application.`fs_corporate_primary_details` p LEFT JOIN
					loan_application.`fs_corporate_applicant_details` a ON p.application_id=a.application_id where a.application_id=',@applicationId,' and a.proposal_mapping_id IS NULL limit 1');
					PREPARE stmtfirstStage FROM @firstStageData;
					EXECUTE stmtfirstStage;
					
					IF (@businessTypeId = 3 OR @businessTypeId = 5) THEN
						SET @firstStageData:= CONCAT('SELECT CONCAT(a.first_name," ",a.middle_name," ",a.last_name),p.fp_product_id,p.el_amount INTO @companyName,@purposeOfLoanId,@reqLoanAmount FROM loan_application.`proposal_details` p LEFT JOIN loan_application.`fs_retail_applicant_details` a ON p.application_id=a.application_id WHERE a.application_id=',@applicationId,' AND a.proposal_mapping_id IS NULL LIMIT 1');
						PREPARE stmtfirstStage FROM @firstStageData;
						EXECUTE stmtfirstStage;
					END IF;
					
					IF (@stageId = 1 OR @stageId = 203) THEN 
					SET @applicationStage= "ITR";
					END IF;
					
					IF (@stageId = 2 OR @stageId = 204) THEN 
					SET @applicationStage= "Bank Statement";
					END IF;
					
					IF (@stageId = 3 OR @stageId = 205) THEN 
					SET @applicationStage= "Directors Background";
					END IF;
					
					IF (@stageId = 4 OR @stageId = 207) THEN 
					SET @applicationStage= "One Form";
					END IF;
					
					IF (@stageId = 5 OR @stageId = 208) THEN
					   SET @applicationStage = "Matches";
					END IF;
					
					IF (@stageId = 6 OR @stageId = 209) THEN
					   SET @applicationStage = "Payment";
					END IF;
					
					IF (@stageId = 212) THEN
					   SET @applicationStage = "Loan Type Selection";
					END IF;
				END IF;
				
			END IF;
			
			IF (@applicationId IS NOT NULL AND @applicationId != '') THEN 
				INSERT INTO connect_temp_tbl(applicationId,stageId,`status`,gstIn,companyName,businessTypeId,proposalId,statusString,createdDate,modifiedDate,proposalStage,tenure,elLoanAmount,productId,applicationCode,orgId,reqLoanAmount,sanctionAmount,sanctionDate,`disbursedAmount`,`disCreatedDate`,offLoanAmt,offUorgId,offBranchId,offCrDate,offMoDate,offIsSanction,offIsDisb,offStatus,offReason,offReOpenRes,offLoanType,purposeOfLoanId,applicationStage,offElAmount,OffproductId,offReqLoanAmount) 
				VALUES(@applicationId,@stageId,@status,@gstIn,@companyName,@businessTypeId,@proposalId,@statusString,@createdDate,@modifiedDate,@proposalStage,@tenure,@elLoanAmt,@productId,@appCode,@orgId,@reqLoanAmt,@sanctionAmt,@sanctionDate,@disAmt,@disDate,@offLoanAmt,@offUorgId,@offBranchId,@offCrDate,@offMoDate,@offIsSanction,@offIsDisb,@offStatus,@offReason,@offReOpenRes,@offLoanType,@purposeOfLoanId,@applicationStage,@offElAmount,@OffproductId,@offReqLoanAmount); 
				
				SET @jsonObj = JSON_OBJECT('applicationId',@applicationId,'gstIn',@gstIn,'companyName',@companyName,'proposalId',@proposalId ,'stageId', @stageId, 'status', @status,'gstIn',@gstIn,'businessTypeId',@businessTypeId,'statusString',@statusString,'createdDate',@createdDate,'modifiedDate',@modifiedDate,'proposalStage',@proposalStage,'tenure',@tenure,'elLoanAmount',@elLoanAmt,'productId',@productId,'applicationCode',@appCode,'orgId',@orgId,'reqLoanAmount',@reqLoanAmt,'sanctionAmount',@sanctionAmt,'sanctionDate',@sanctionDate,'disbursedAmount',@disAmt,'disCreatedDate',@disDate,'offLoanAmt',@offLoanAmt,'offUorgId',@offUorgId,
				   'offBranchId',@offBranchId,'offCrDate',@offCrDate,'offMoDate',@offMoDate,'offIsSanction',@offIsSanction,'offIsDisb',@offIsDisb,'offStatus',@offStatus,'offReason',@offReason,'offReOpenRes',@offReOpenRes,'offLoanType',@offLoanType,
				   'offlineSancAmount',@offlineSanctionAmt,'offlineSancDate',@offlineSanctionDate,'offlineDisAmt',@offlineDisAmt,'offlineDisDate',@offlineDisDate,'proposalModifiedDate',@proposalModifiedDate,'purposeOfLoanId',@purposeOfLoanId,'applicationStage',@applicationStage,'offElAmount',@offElAmount,'OffproductId',@OffproductId,'offReqLoanAmount',@offReqLoanAmount);
					
				IF ( @i < (@n -1) ) THEN
					SET @jsonObj= CONCAT(@jsonObj,',');
				END IF;
			
				IF (@statusString = 'InEligibleNSB') THEN
				
					IF (@notEligibleNSBCount = 1) THEN 
						SET @jsonString:= CONCAT(@jsonString,@jsonObj);
					END IF;
						
				ELSEIF (@statusString = 'InEligibleSB') THEN 
					IF (@notEligibleSBCount = 1) THEN 
						SET @jsonString:= CONCAT(@jsonString,@jsonObj);
					END IF;
				ELSE
					SET @jsonString:= CONCAT(@jsonString,@jsonObj);
				END IF;
				
				
						
			END IF;
									
			SET @i = @i + 1;
			
			SET @sanctionAmt = NULL;
		SET @applicationId = NULL;
		SET @gstIn = NULL;
		SET @companyName = NULL;
		SET @proposalId = NULL;
		SET @stageId = NULL;
		SET @status = NULL;
		SET @gstIn = NULL;
		SET @businessTypeId = NULL;
		SET @statusString = NULL;
		SET @createdDate = NULL;
		SET @modifiedDate = NULL;
		SET @proposalStage = NULL;
		SET @tenure = NULL;
		SET @elLoanAmt = NULL;
		SET @productId = NULL;
		SET @appCode = NULL;
		SET @orgId = NULL;
		SET @reqLoanAmt = NULL;
		SET @sanctionAmt = NULL;
		SET @sanctionDate = NULL;
		SET @disAmt = NULL;
		SET @disDate = NULL;
		SET @offLoanAmt = NULL;
		SET @offUorgId = NULL;
		SET @offBranchId = NULL;
		SET @offCrDate = NULL;
		SET @offMoDate = NULL;
		SET @offIsSanction = NULL;
		SET @offIsDisb = NULL;
		SET @offStatus = NULL;
		SET @offReason = NULL;
		SET @offReOpenRes = NULL;
		SET @offLoanType = NULL;
		SET @offlineSanctionAmt = NULL;
		SET @offlineSanctionDate = NULL;
		SET @offlineDisAmt = NULL;
		SET @offlineDisDate = NULL;
		SET @proposalModifiedDate = NULL;
		SET @applicationStage = NULL;
		SET @purposeOfLoanId = NULL;
		SET @offElAmount = NULL;
		SET @OffproductId = NULL;
		SET @notEligibleNSBCount = 0;
		SET @notEligibleSBCount = 0;
			
			
		END WHILE;
		
		-- ----------------------------------------------------------------------------------------------------------------------------------
		
		SET @k = @k + 1;
		
		SET @jsonString:= CONCAT(@jsonString,']}');
		
		IF ( @k < @applicationIdCount ) THEN			
			SET @jsonString:= CONCAT(@jsonString,',');
		END IF;
			
		SET @jsonMainString := CONCAT(@jsonMainString,@jsonString);
		
		SET @aId = NULL;
		SET @applicationId = NULL;
		
		
	END WHILE;
	
	SET result := CONCAT(@jsonMainString,']');
	SET @jsonMainString = '';
	
	 SELECT * FROM 	application_temp_tbl;	
			
	
	-- SELECT * FROM connect_temp_tbl;
	
	 SELECT result;
	 SET @result = '';
	
    END$$

DELIMITER ;