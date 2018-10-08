package com.capitaworld.service.loans.repository.fundprovider;

import com.capitaworld.service.loans.domain.fundprovider.ProposalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public interface ProposalDetailsRepository extends JpaRepository<ProposalDetails,Long>{

    @Query("SELECT pd.applicationId FROM ProposalDetails pd WHERE branchId =:branchId and fpProductId=:fpProductId and isActive = 1")
    public List<Long> getApplicationsBasedOnBranchIdAndFpProductId(@Param("branchId") Long branchId,@Param("fpProductId") Long fpProductId);

    @Query("SELECT pd.applicationId FROM ProposalDetails pd WHERE branchId =:branchId and isActive = 1")
    public List<Long> getApplicationsBasedOnBranchId(@Param("branchId") Long branchId);

    @Query("SELECT count(pd)  FROM ProposalDetails pd WHERE pd.userOrgId =:userOrgId and pd.applicationId =:applicationId  and isActive = 1")
    public Long getApplicationIdCountByOrgId(@Param("applicationId") Long applicationId,@Param("userOrgId") Long userOrgId);
    
    @Query(value = "SELECT pd.application_id, cl.user_id, fs.name, usr.email, usr.mobile, pd.created_date, pd.branch_id, \n" + 
    		"pd.el_amount, pd.el_tenure, pd.el_roi, pd.emi, pd.processing_fee, branch.name AS branchname, \n" + 
    		"branch.contact_person_name, branch.telephone_no, branch.contact_person_number, org.organisation_name, \n" + 
    		"lam.application_code, branch.code, branch.street_name, (SELECT state_name FROM `one_form`.`state` s \n" + 
    		"WHERE s.id = branch.state_id), (SELECT city_name FROM `one_form`.`city` c WHERE c.id = branch.city_id), branch.premises_no, \n" + 
    		"(SELECT product_id FROM `loan_application`.`fp_product_master` pm WHERE pm.fp_product_id = pd.fp_product_id), branch.contact_person_email, \n" + 
    		"(SELECT COUNT(id) FROM `users`.`campaign_details` cd WHERE cd.user_id = cl.user_id) \n" + 
    		"FROM  `loan_application`.`proposal_details` pd \n" + 
    		"LEFT JOIN `connect`.`connect_log` cl \n" + 
    		"ON cl.application_id = pd.application_id \n" + 
    		"LEFT JOIN `users`.`users` usr \n" + 
    		"ON usr.user_id = cl.user_id \n" + 
    		"LEFT JOIN `users`.`fund_seeker_details` fs \n" + 
    		"ON fs.user_id = usr.user_id \n" + 
    		"LEFT JOIN  `users`.`branch_master` branch \n" + 
    		"ON branch.id = pd.branch_id \n" + 
    		"LEFT JOIN `users`.`user_organisation_master` org \n" + 
    		"ON org.user_org_id = pd.user_org_id \n" + 
    		"LEFT JOIN `loan_application`.`fs_loan_application_master` lam \n" + 
    		"ON lam.application_id = pd.application_id \n" + 
    		"WHERE pd.user_org_id = :userOrgId AND usr.user_type_id = 1 AND pd.is_active = TRUE AND cl.stage_id > 6 AND cl.stage_id != 8 \n" + 
    		"and (pd.created_date BETWEEN :fromDate and :toDate) ORDER BY pd.id DESC;", nativeQuery = true)
    public List<Object[]> getProposalDetailsByOrgId(@Param("userOrgId")Long userOrgId,@Param("fromDate") Date fromDate,@Param("toDate") Date toDate);

}
