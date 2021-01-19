package com.opl.service.loans.repository.common.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.opl.mudra.api.loans.model.TutorialsViewAudits;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.domain.fundseeker.retail.BankingRelation;
import com.opl.service.loans.repository.common.LoanRepository;


@SuppressWarnings("unchecked")
@Repository
public class LoanRepositoryImpl implements LoanRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoanRepositoryImpl.class);

	private static final String ORG_ID = "orgId";
	private static final String BRANCH_ID = "branchId";
	private static final String LIST_LIMIT = "listLimit";
	private static final String SEARCH_STRING = "searchString";
	private static final String BUSI_TYPE_ID = "busiTypeId";
	
	@PersistenceContext
	private EntityManager entityManager;

	public Object[] getRoleIdAndBranchIdByUserId(Long userId) {
		try {
			return  (Object[]) entityManager
					.createNativeQuery("SELECT user_role_id,branch_id FROM users.`users` WHERE user_id =:userId")
					.setParameter(CommonUtils.USER_ID, userId)
					.getSingleResult();
		} catch (Exception e) {
			LOGGER.error(CommonUtils.EXCEPTION,e);
		}
		return null;
	}
	
	@Override
	public Boolean isCampaignUser(Long userId) {
		try {
			List<String> list =  (List<String>) entityManager
					.createNativeQuery("SELECT cam.loan_campaign_code FROM fs_loan_application_master cam WHERE cam.application_id =:applicationId")
					.setParameter(CommonUtils.APPLICATION_ID, userId)
					.getResultList();
			return !CommonUtils.isListNullOrEmpty(list);
		} catch (Exception e) {
			LOGGER.error(CommonUtils.EXCEPTION,e);
		}
		return null;
	}
	
	@Override
	public String getCampaignUser(Long userId,Long campaignType) {
		try {
			String code = (String) entityManager
					//.createNativeQuery("SELECT cam.code FROM `users`.`campaign_details` cam WHERE cam.user_id =:userId  AND cam.is_active = TRUE order by cam.id desc limit 1")
					.createNativeQuery("SELECT cam.code FROM `users`.`campaign_details` cam "
									+ "INNER JOIN users.`user_organisation_master` u ON u.organisation_code=cam.code AND (u.campaign_type=:campaignTypeBoth OR u.campaign_type=:campaignType) "
									+ "WHERE cam.user_id =:userId  AND cam.is_active = TRUE ORDER BY cam.id DESC LIMIT 1")
					.setParameter(CommonUtils.USER_ID, userId)
					.setParameter("campaignType", campaignType)
					.setParameter("campaignTypeBoth", com.opl.mudra.api.matchengine.utils.CommonUtils.CampaignLoanType.Both.getId())
					.getSingleResult();
			return !CommonUtils.isObjectNullOrEmpty(code) ? code : null;
		} catch (Exception e) {
			LOGGER.error(CommonUtils.EXCEPTION,e);
		}
		return null;
	}
	
	public String getMobileNumberByUserId(Long userId) {
		try {
			return  (String) entityManager
					.createNativeQuery("SELECT mobile FROM users.`users` WHERE user_id =:userId")
					.setParameter(CommonUtils.USER_ID, userId)
					.getSingleResult();
		} catch (Exception e) {
			LOGGER.error(CommonUtils.EXCEPTION,e);
		}
		return null;
	}

	public List<Object[]> searchProposalForHO(Long orgId,String searchString,Long listLimit,Long businessTypeId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchProposalsByOrgAndSearchString");
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(SEARCH_STRING,String.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(LIST_LIMIT,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BUSI_TYPE_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter(SEARCH_STRING,searchString);
		storedProcedureQuery.setParameter(LIST_LIMIT,listLimit);
		storedProcedureQuery.setParameter(BUSI_TYPE_ID,businessTypeId);
		return (List<Object[]>) storedProcedureQuery.getResultList();
	}

	public List<Object[]> searchProposalForCheckerAndMaker(Long orgId,String searchString,Long branchId,Long listLimit,Long businessTypeId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchProposalsByOrgAndBranchAndSearchString");
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(SEARCH_STRING,String.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BRANCH_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(LIST_LIMIT,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BUSI_TYPE_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter(SEARCH_STRING,searchString);
		storedProcedureQuery.setParameter(BRANCH_ID,branchId);
		storedProcedureQuery.setParameter(LIST_LIMIT,listLimit);
		storedProcedureQuery.setParameter(BUSI_TYPE_ID,businessTypeId);
		return (List<Object[]>) storedProcedureQuery.getResultList();
	}

	public List<Object[]> searchProposalForSMECC(Long orgId,String searchString,Long userId,Long listLimit,Long businessTypeId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchProposalsByOrgAndUserIdAndSearchString");
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(SEARCH_STRING,String.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(LIST_LIMIT,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BUSI_TYPE_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter(SEARCH_STRING,searchString);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		storedProcedureQuery.setParameter(LIST_LIMIT,listLimit);
		storedProcedureQuery.setParameter(BUSI_TYPE_ID,businessTypeId);
		return (List<Object[]>) storedProcedureQuery.getResultList();
	}

	@Override
	public List<Object[]> getSerachProposalListByRoleSP(Long orgId, String searchString, Long userId, Long listLimit, Long businessTypeId, Long branchId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("getSerachProposalListByRoleSP");
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(SEARCH_STRING,String.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(LIST_LIMIT,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BUSI_TYPE_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BRANCH_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter(SEARCH_STRING,searchString);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		storedProcedureQuery.setParameter(LIST_LIMIT,listLimit);
		storedProcedureQuery.setParameter(BUSI_TYPE_ID,businessTypeId);
		if(CommonUtils.isObjectNullOrEmpty(branchId))
			branchId=-1l;
		storedProcedureQuery.setParameter(BRANCH_ID,branchId);
		return (List<Object[]>) storedProcedureQuery.getResultList();
	}

	public Object[] fpDashBoardCountByOrgId(Long orgId,Long businessTypeId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchFpDashbordCountByOrgId");
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BUSI_TYPE_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter(BUSI_TYPE_ID,businessTypeId);
		return (Object[]) storedProcedureQuery.getSingleResult();
	}

	public Object[] fpDashBoardCountByOrgIdAndBranchId(Long orgId,Long branchId,Long businessTypeId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchFpDashbordCountByOrgIdAndBranchId");
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BRANCH_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BUSI_TYPE_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter(BRANCH_ID,branchId);
		storedProcedureQuery.setParameter(BUSI_TYPE_ID,businessTypeId);
		return (Object[]) storedProcedureQuery.getSingleResult();
	}

	public Object[] fpDashBoardCountByOrgIdAndUserId(Long orgId,Long userId,Long businessTypeId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchFpDashbordCountByOrgIdAndUserId");
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BUSI_TYPE_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		storedProcedureQuery.setParameter(BUSI_TYPE_ID,businessTypeId);
		return (Object[]) storedProcedureQuery.getSingleResult();
	}

	public Object[] fetchFpDashbordCountByRoleSP(Long orgId,Long userId,Long businessTypeId,Long branchId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("fetchFpDashbordCountByRoleSP");
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BUSI_TYPE_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(BRANCH_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		storedProcedureQuery.setParameter(BUSI_TYPE_ID,businessTypeId);
		if(CommonUtils.isObjectNullOrEmpty(branchId))
			branchId=-1l;
		storedProcedureQuery.setParameter(BRANCH_ID,branchId);
		return (Object[]) storedProcedureQuery.getSingleResult();
	}

	public String getGSTINByAppId(Long applicationId) {
		return  (String) entityManager
				.createNativeQuery("SELECT gstin FROM connect_cvl_mudra.`connect_log` WHERE application_id =:applicationId order by id desc limit 1")
						.setParameter("applicationId", applicationId).getSingleResult();
	}

	public String getCommonPropertiesValue(String key) {
		try {
			return (String) entityManager
					.createNativeQuery("SELECT `value` FROM `common_properties` WHERE `key` =:key")
							.setParameter("key", key).getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Exception while get common properties value ----->" ,e);
		}
		return null;
	}

	@Override
	public Long getOfflineCountByAppId(Long applicationId) {
		BigInteger count =  (BigInteger) entityManager
				.createNativeQuery("SELECT COUNT(*) FROM `ineligible_proposal_details` inl WHERE inl.`application_id` =:applicationId AND inl.`is_active` = TRUE AND inl.`addi_fields` = '1'")
						.setParameter("applicationId", applicationId).getSingleResult();
		return count != null ? count.longValue() : 0l;
	}
	
	@Override
	public String getOfflineDetailsByAppId(Long applicationId) {
		try {
			return  (String) entityManager
					.createNativeQuery("SELECT CAST(JSON_ARRAYAGG(JSON_OBJECT('applicationId',inl.`application_id`,\r\n" + 
							"'status',sts.`display_name`,'bankName',org.`organisation_name`,'branchName',brn.`name`,'branchCode', brn.`code`)) AS CHAR) AS JSON \r\n" + 
							"FROM `ineligible_proposal_details` inl \r\n" + 
							"LEFT JOIN `ineligible_proposal_status` sts ON sts.`id` = inl.`status` \r\n" + 
							"LEFT JOIN users.`user_organisation_master` org ON org.`user_org_id` = inl.`user_org_id` \r\n" + 
							"LEFT JOIN users.`branch_master` brn ON brn.`id` = inl.`branch_id` \r\n" + 
							"WHERE inl.`application_id` =:applicationId AND inl.`is_active` = TRUE AND inl.`addi_fields` = '1';")
							.setParameter("applicationId", applicationId).getSingleResult();	
		} catch (Exception e) {
			LOGGER.error("Exception while get offline details by application id ----->" ,e);
		}
		return null;
	}
	
	@Override
	public String getOfflineStatusByAppId(Long applicationId) {
		try {
			return  (String) entityManager
					.createNativeQuery("SELECT CAST(JSON_OBJECT('applicationId',inl.`application_id`,'status',sts.`display_name`,'bankName',org.`organisation_name`,'modifiedDate',inl.`modified_date`) AS CHAR ) \r\n" + 
							"FROM `ineligible_proposal_details` inl \r\n" + 
							"LEFT JOIN `ineligible_proposal_status` sts ON sts.`id` = inl.`status`\r\n" + 
							"LEFT JOIN users.`user_organisation_master` org ON org.`user_org_id` = inl.`user_org_id`\r\n" + 
							"WHERE inl.`application_id` =:applicationId AND inl.`is_active` = TRUE AND inl.`addi_fields` = '1'")
							.setParameter("applicationId", applicationId).getSingleResult();	
		} catch (Exception e) {
			LOGGER.error("Exception while get offline status  ----->" ,e);
		}
		return null;
		
	}

	
	@Override
	public List<Double> getIncomeOfItrOf3Years(Long applicationId) {

		return (List<Double>)entityManager.createNativeQuery("SELECT appInc.`salary_income` FROM `fs_retail_applicant_income_details` appInc WHERE appInc.`application_id` =:applicationId AND appInc.`proposal_mapping_id` IS NULL AND `appInc`.`salary_income` IS NOT NULL ORDER BY appInc.`year` DESC ").setParameter("applicationId", applicationId) .getResultList();
//		if(!CommonUtils.isListNullOrEmpty(resultList)) {
//			List<Double> finalList = new ArrayList<>(resultList.size());
//			for(Object o : resultList) {
//				finalList.add(Double.valueOf(o.toString()));
//			}
//			return finalList;
//		}else {
//			return Collections.emptyList();			
//		}
	}
	
	
	@Override
	public List<Double> getIncomeOfItrOf3YearsOfCoApplicant(Long coAppId) {

		return (List<Double>)entityManager.createNativeQuery("SELECT appInc.`salary_income` FROM `fs_retail_co_applicant_income_details` appInc WHERE appInc.`co_app_id` =:id AND appInc.`proposal_mapping_id` IS NULL AND `appInc`.`salary_income` IS NOT NULL ORDER BY appInc.`year` DESC ")
				.setParameter("id", coAppId)
				.getResultList();
	}
	
	
	@Override
	public Boolean isITRUploaded(Long applicationId) {
		List<Boolean> result = (List<Boolean>)entityManager.createNativeQuery("SELECT itr.`is_manual_filled` FROM `itr_api_msme`.`itr_tracking` itr WHERE itr.`application_id` =:applicationId AND itr.`is_active` = TRUE")
				.setParameter("applicationId", applicationId)
				.getResultList();
		if(!CommonUtils.isListNullOrEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	
	@Override
	public Boolean isITRUploadedForCoApp(Long applicationId, Long coAppId) {
		List<BigInteger> result = (List<BigInteger>)entityManager.createNativeQuery("SELECT COUNT(app.`id`) FROM `fs_retail_co_applicant_details` app WHERE app.`is_itr_completed` = TRUE AND app.`id` =:id AND app.`application_id` =:applicationId AND app.`is_active` = TRUE")
				.setParameter("applicationId", applicationId)
				.setParameter("id", coAppId)
				.getResultList();
		if(!CommonUtils.isListNullOrEmpty(result)) {
			return result.get(0) != null ? result.get(0).longValue() > 0 : false;
		}
		return null;
	}
	
	
	@Override
	public Boolean isITRSkippedForCoApp(Long applicationId, Long coAppId) {
		List<BigInteger> result = (List<BigInteger>)entityManager.createNativeQuery("SELECT COUNT(app.`id`) FROM `fs_retail_co_applicant_details` app WHERE app.`is_itr_skip` = TRUE AND app.`id` =:id AND app.`application_id` =:applicationId AND app.`is_active` = TRUE")
				.setParameter("applicationId", applicationId)
				.setParameter("id", coAppId)
				.getResultList();
		if(!CommonUtils.isListNullOrEmpty(result)) {
			return result.get(0) != null ? result.get(0).longValue() > 0 : false;
		}
		return null;
	}
	
	
	@Override
	public Boolean isITRMannualForCoApp(Long applicationId, Long coAppId) {
		List<BigInteger> result = (List<BigInteger>)entityManager.createNativeQuery("SELECT COUNT(app.`id`) FROM `fs_retail_co_applicant_details` app WHERE app.`is_itr_manual` = TRUE AND app.`id` =:id AND app.`application_id` =:applicationId AND app.`is_active` = TRUE")
				.setParameter("applicationId", applicationId)
				.setParameter("id", coAppId)
				.getResultList();
		if(!CommonUtils.isListNullOrEmpty(result)) {
			return result.get(0) != null ? result.get(0).longValue() > 0 : false;
		}
		return null;
	}

	
	@Override
	public Double getRetailLoanAmountByApplicationId(Long applicationId) {
		List<Double> result = (List<Double>)entityManager.createNativeQuery("SELECT app.`loan_amount_required` FROM `fs_retail_applicant_details` app WHERE app.`application_id` =:applicationId AND app.`is_active` = TRUE")
				.setParameter("applicationId", applicationId)
				.getResultList();
		if(!CommonUtils.isListNullOrEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	//1/6/2019...................
	
	@Override
	public List<Object[]> getTypeSelectionData() {
		return (List<Object[]>)entityManager.createNativeQuery("SELECT `type`,`description`,`business_type_id`,`img_path`, `id` ,`parent_id` FROM `fs_loan_type_selection` WHERE `is_active` = TRUE").getResultList();
	}
	
	/**
	 * @author vijay.chauhan
	 * @param userId
	 */	
	
	@Override
	public List<Object[]> getTypeSelectionData(String userId) {
		String query = "SELECT `type`,`description`,`business_type_id`,`img_path` FROM `fs_loan_type_selection` WHERE `is_active` = TRUE AND TYPE!='Retail'\n" + 
				"UNION ALL\n" + 
				"SELECT lts.`type`,lts.`description`,lts.`business_type_id`,lts.`img_path`\n" + 
				"FROM `fs_loan_type_selection` lts \n" + 
				"INNER JOIN `fs_loan_type_accessible_users` ltsu ON (lts.id=ltsu.loan_type_selection_id AND ltsu.is_active=TRUE)\n" + 
				"WHERE lts.TYPE='Retail' AND ltsu.user_id="+userId;
		return (List<Object[]>) entityManager.createNativeQuery(query).getResultList();
	}
	
	
	@Override
	public String checkPanForAlreayInPrinciplOrNotEligible(Integer typeId,Integer selectedLoanTypeId,Long applicationId,String panNumber) {
		try {
			StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spRetailCheckPANAlreadyExist");
			storedProcedureQuery.registerStoredProcedureParameter("typeId",Integer.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("selectedLoanTypeId",Integer.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("applicationId",Long.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("panNumber",String.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("loanType",Integer.class, ParameterMode.OUT);
			storedProcedureQuery.registerStoredProcedureParameter("message",String.class, ParameterMode.OUT);
			storedProcedureQuery.setParameter("typeId",typeId);
			storedProcedureQuery.setParameter("selectedLoanTypeId",selectedLoanTypeId);
			storedProcedureQuery.setParameter("applicationId",applicationId);
			storedProcedureQuery.setParameter("panNumber",panNumber);
			storedProcedureQuery.execute();
			Object result = storedProcedureQuery.getOutputParameterValue("loanType");
			if(!CommonUtils.isObjectNullOrEmpty(result)) {
				return (String) storedProcedureQuery.getOutputParameterValue("message");
			}
		} catch (Exception e) {
			LOGGER.error("EXCEPTION spRetailCheckPANAlreadyExist :=- ", e);
		}
		return null;
	}

	public String getTutorialsByRoleId(Long userRoleId, Integer loanType){
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchBankAdminTutorialList");
		storedProcedureQuery.registerStoredProcedureParameter("userRoleId",Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter("loanType",Integer.class, ParameterMode.IN);
		storedProcedureQuery.setParameter("userRoleId",userRoleId);
		storedProcedureQuery.setParameter("loanType",loanType);
		List<String> tutorials = storedProcedureQuery.getResultList();
		return !CommonUtils.isListNullOrEmpty(tutorials) ? !CommonUtils.isObjectNullOrEmpty(tutorials.get(0)) ? tutorials.get(0) : null :null;
	}

	public String getTutorialsById(Long id){
		String tutorials = (String) entityManager.createNativeQuery("select CAST(JSON_OBJECT('id',tu.id,'nameTutorial',tu.name_tutorial,'title',tu.title,'description',tu.description,'urlTutorial',tu.url_tutorial,'type',tu.type,'createdDate',tu.created_date,'urlThumbnail',tu.url_thumb,'externalLink',tu.external_link,'fileType',tu.extension,'viewCount',(SELECT COUNT(va.id) FROM `tutorial_view_audit` va WHERE va.tutorial_id = tu.id)) AS CHAR) from tutorial_upload_manage tu where tu.is_active = TRUE and tu.id =:tutorialId")
				.setParameter("tutorialId", id)
				.getSingleResult();
		return  !CommonUtils.isObjectNullOrEmpty(tutorials) ? tutorials : null;
	}
	
	@Override
	public String getPrefillProfileStatus(Long fromLoanId,Long toLoanId) {
		try {
			StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spPrefillProfileCheck");
			storedProcedureQuery.registerStoredProcedureParameter("fromLoanId",Long.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("toLoanId",Long.class, ParameterMode.IN);
			storedProcedureQuery.setParameter("fromLoanId",fromLoanId);
			storedProcedureQuery.setParameter("toLoanId",toLoanId);
			return (String) storedProcedureQuery.getSingleResult();
	    } catch (Exception e) {
	    	LOGGER.error("EXCEPTION spPrefillProfileCheck :=- ", e);
	    }
		return null;
	}
	
	@Override
	public String getApplicationListForPrefillProfile(Long userId) {
		try {
			return (String) entityManager
					.createNativeQuery("SELECT CAST(JSON_ARRAYAGG(JSON_OBJECT('applicationId',con.`application_id`,'name',CONCAT(IFNULL(fs.`first_name`,''),' ',IFNULL(fs.`last_name`,'')),\r\n" +  
							"'status',IF(con.`stage_id` = 207,'In-Eligible','In-Principle'),'applicationCode',\r\n" + 
							"IF(con.`stage_id` = 207,IF(con.`loan_type_id` = 3,'HomeLoan',IF(con.`loan_type_id` = 12,'Auto Loan','PersonalLoan')),CONCAT(IF(con.`loan_type_id` = 3,'HomeLoan',IF(con.`loan_type_id` = 12,'Auto Loan','PersonalLoan')),' - Date:', IF(con.stage_id = 207,DATE_FORMAT(con.modified_date, '%d-%m-%Y'),DATE_FORMAT(con.In_principle_date, '%d-%m-%Y')),' - Rs.',pp.`el_amount`))\r\n" + 
							")) AS CHAR ) \r\n" + 
							"FROM connect.`connect_log` con  \r\n" + 
							"LEFT JOIN `fs_retail_applicant_details` fs ON fs.`application_id` = con.`application_id` AND fs.`proposal_mapping_id` IS NULL \r\n" + 
							"LEFT JOIN `proposal_details` pp ON pp.`application_id` = con.`application_id` AND pp.`id` = con.`proposal_id`\r\n" + 
							"LEFT JOIN `application_proposal_mapping` ap ON ap.`application_id` = con.`application_id` AND ap.`proposal_id` = con.`proposal_id` \r\n" + 
							"WHERE con.`id` IN (SELECT cn.`id` FROM connect.`connect_log` cn WHERE cn.`user_id` =:userId AND ((cn.`stage_id` = 207 AND cn.`status` = 6) OR (cn.`stage_id` IN (210,211))) GROUP BY cn.`application_id`);")
							.setParameter("userId", userId).getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Exception while getApplicationListForPrefillProfile  ----->" ,e);
		}
		return null;
		
	}
	
	@Override
	public String getAgriLoanApplicationsByOrgIdAndStatus(Integer orgId,Integer status,Integer fromLimit,Integer toLimit) {
		try {
			StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spGetAgriApplicationsByOrgIdAndStatus");
			storedProcedureQuery.registerStoredProcedureParameter("orgId",Integer.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("stus",Integer.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("fromLimit",Integer.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("toLimit",Integer.class, ParameterMode.IN);
			storedProcedureQuery.registerStoredProcedureParameter("result",String.class, ParameterMode.OUT);
			if(status == null) {
				status = -1;
			}
			if(fromLimit == null) {
				fromLimit = -1;
			}
			if(toLimit == null) {
				toLimit = -1;
			}
			
			storedProcedureQuery.setParameter("toLimit",toLimit);				
			storedProcedureQuery.setParameter("fromLimit",fromLimit);
			storedProcedureQuery.setParameter("stus",status);
			storedProcedureQuery.setParameter("orgId",orgId);
			storedProcedureQuery.execute();
			return (String)storedProcedureQuery.getOutputParameterValue("result");
			
		} catch (Exception e) {
			LOGGER.error("EXCEPTION spGetAgriApplicationsByOrgIdAndStatus :=- {}", e);
		}
		return null;
	}
	
	@Override
	public Boolean retailPrefillData(String input) {
		try {
			StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spPrefillProfile");
			storedProcedureQuery.registerStoredProcedureParameter("input",String.class, ParameterMode.IN);
			storedProcedureQuery.setParameter("input",input);
			storedProcedureQuery.execute();
			return true;
	    } catch (Exception e) {
	    	LOGGER.error("EXCEPTION spPrefillProfile :=- ", e);
	    }
		return false;
	}
	
	@Override
	public String getApplicationCampaignCode(Long applicationId) {
		try {
			return (String) entityManager
					.createNativeQuery("SELECT fs.`loan_campaign_code` FROM `loan_application_cvl_mudra`.`fs_loan_application_master` fs WHERE fs.`application_id` =:applicationId")
							.setParameter("applicationId", applicationId).getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Exception while getApplicationCampaignCode  ----->" ,e);
		}
		return null;
		
	}
	
	@Override
	public Object[] getApplicationCampaignDetails(Long applicationId) {
		try {
			return (Object[]) entityManager
					.createNativeQuery("SELECT org.user_org_id,org.organisation_name,fs.`loan_campaign_code`,fs.campaign_code " + 
							"FROM `fs_loan_application_master` fs \r\n" + 
							"LEFT JOIN `users`.`user_organisation_master` org ON org.organisation_code = fs.loan_campaign_code\r\n" + 
							"WHERE fs.`application_id` =:applicationId")
							.setParameter("applicationId", applicationId).getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Exception while getApplicationCampaignDetails  ----->" ,e);
		}
		return null;
		
	}
	
	@Override
	public Boolean isBankSpecificOn(Long applicationId) {
		try {
			BigInteger count =  (BigInteger) entityManager.createNativeQuery("SELECT COUNT(org.`user_org_id`) FROM `users`.`user_organisation_master` org,`fp_offline_app_config` app,`fs_loan_application_master` cam \r\n" + 
					"WHERE cam.`application_id` =:applicationId AND cam.`loan_campaign_code` = org.`organisation_code` \r\n" + 
					"AND org.`user_org_id` = app.`org_id` AND org.`is_active` = TRUE \r\n" + 
					"AND app.`is_active` = TRUE AND app.`bank_specific` = '1' AND app.business_type_id = cam.business_type_id")
			.setParameter("applicationId", applicationId).getSingleResult();
			return count.longValue() > 0;			
		} catch (Exception e) {
			LOGGER.error("Exception while isBankSpecificOn  ----->" ,e);
		}
		return null;
	}

	public boolean saveTutorialsAudits(TutorialsViewAudits longLatrequest){
		BigInteger singleResult = (BigInteger) entityManager.createNativeQuery("SELECT COUNT(id) FROM `tutorial_view_audit` WHERE tutorial_id =:tutorialId AND user_id=:userId AND loan_type=:loanType")
				.setParameter("userId", longLatrequest.getUserId())
				.setParameter("loanType", longLatrequest.getLoanType())
				.setParameter("tutorialId", longLatrequest.getTutorialId())
				.getSingleResult();
		if(singleResult.longValue()  < 1l) {
			int saveTutorials = entityManager.createNativeQuery("INSERT INTO tutorial_view_audit(user_id,role_id,tutorial_id,loan_type,view_date) values (:userId,:roleId,:tutorialId,:loanType,NOW())")
					.setParameter("userId", longLatrequest.getUserId())
					.setParameter("roleId", longLatrequest.getRoleId())
					.setParameter("loanType", longLatrequest.getLoanType())
					.setParameter("tutorialId", longLatrequest.getTutorialId())
					.executeUpdate();
			return saveTutorials > 0;
		} else {
			int update = entityManager.createNativeQuery("update tutorial_view_audit set view_date =:date where user_id=:userId and loan_type=:loanType and tutorial_id=:tutorialId")
					.setParameter("date", new Date())
					.setParameter("userId", longLatrequest.getUserId())
					.setParameter("loanType", longLatrequest.getLoanType())
					.setParameter("tutorialId", longLatrequest.getTutorialId())
					.executeUpdate();
			return update > 0;

		}
	}

	
	public String getTutorialsAudit(TutorialsViewAudits request){
		List<String> tutorialViewAudit = entityManager.createNativeQuery("SELECT CAST(JSON_ARRAYAGG(JSON_OBJECT('id',a.id,'userName',u.email,'viewDate',a.view_date,'roleName',r.role_name,'branchName',b.name)) AS CHAR) FROM tutorial_view_audit a LEFT JOIN users.`users` u ON u.user_id = a.user_id LEFT JOIN users.`user_role_master` r ON r.role_id = a.role_id LEFT JOIN users.`branch_master` b ON u.branch_id=b.id\r\n" + 
				" WHERE a.tutorial_id =:tutorialId")
				.setParameter("tutorialId", request.getTutorialId())
				.setFirstResult(request.getPageIndex())
				.setMaxResults(request.getSize())
				.getResultList();
		System.out.println("request.getPageIndex()================>"+request.getPageIndex());
		System.out.println("=============>"+request.getSize());
			return CommonUtils.isListNullOrEmpty(tutorialViewAudit) ? null : CommonUtils.isObjectNullOrEmpty(tutorialViewAudit.get(0)) ? null : tutorialViewAudit.get(0);
	}

	public List<Object[]> getCoLendingRatio(Long fpProductId){
		List<Object[]> ratioList = (List<Object[]>)entityManager.createNativeQuery("SELECT m.ratio_id,fc.nbfc_ratio,fc.bank_ratio,fc.tenure " +
								"FROM nbfc_ratio_mapping m " +
								"INNER JOIN fp_co_lending_ratio fc ON fc.id=m.ratio_id AND fc.is_proposal_active=TRUE AND fc.is_active=TRUE " +
								"WHERE m.fp_product_id=:fpProductId AND m.is_active=TRUE")
								.setParameter("fpProductId",fpProductId).getResultList();
		return ratioList;
	}

	@Override
	public Object [] getBureauVersionIdById(Long scoringModelId) {
		Object [] object = null;
		try {
			object = (Object[]) entityManager.createNativeQuery("select md.cibil_bureau_version_concession,md.cibil_bureau_grad_version FROM scoring_mudra.scoring_model md where md.id =:id")
					.setParameter("id", scoringModelId)
					.getSingleResult();	
		}catch(Exception e) {
			object = new Object[2];
			object[0] = 1;
			object[1] = 2;
			LOGGER.error("Error while getting version from Scoring Model Id = >{}",scoringModelId);
		}
		return object;
	}

	@Override
	public Object[] getUserDetails(Long userId){
		try {
			Object[] obj = (Object[]) entityManager.createNativeQuery("select u.last_access_business_type_id,u.is_nbfc_user FROM users.users u where u.user_id =:userId")
					.setParameter("userId", userId)
					.getSingleResult();
			return obj;
		}catch (Exception e){
			LOGGER.error("Data not found for userId:",userId);
		}
		return null;
	}

	
	public List<Object[]> getCoLendingAllRatio(Long applicationId){
		List<Object[]> ratioList = (List<Object[]>)entityManager.createNativeQuery("SELECT m.ratio_id,fc.nbfc_ratio,fc.bank_ratio,fc.tenure,m.fp_product_id,fc.bank_id FROM nbfc_ratio_mapping m " +
				"INNER JOIN fp_co_lending_ratio fc ON fc.id=m.ratio_id AND fc.is_proposal_active=TRUE AND fc.is_active=TRUE " +
				"WHERE m.fp_product_id IN (SELECT fp_product_id FROM fp_product_master WHERE nbfc_product_type =2 AND user_org_id IN (SELECT bank_org_id FROM fs_co_lending_application_bank_mapping WHERE application_id=:applicationId)) " +
				"AND m.is_active=TRUE group by m.ratio_id")
				.setParameter("applicationId",applicationId).getResultList();
		return ratioList;
	}
	
	
	public String getScoringMinAndMaxRangeValue(List<Long> scoreModelId,List<Long> fieldMasterId) {
		try {
			return (String) entityManager.createNativeQuery("SELECT CAST(JSON_ARRAYAGG(JSON_OBJECT('scoreModelId',mp.scoring_model_id,'minRange',md.min_range,'maxRange',md.max_range,'score',md.score,'description',md.description,'fieldMasterId',mp.`field_master_id`)) AS CHAR) "
					+ "FROM `scoring_mudra`.`model_parameter_option` md "
					+ "INNER JOIN `scoring_mudra`.`model_parameter` mp ON mp.`id` = md.`model_parameter_id` "
					+ "WHERE mp.scoring_model_id IN (:scoreModelId) AND mp.`field_master_id` IN (:fieldMasterId) AND md.is_active = TRUE")
					.setParameter("scoreModelId",scoreModelId)
					.setParameter("fieldMasterId", fieldMasterId)
					.getSingleResult();	
		} catch (Exception e) {
			LOGGER.error("Exception while Get Scoring Min And Max Range Value",e);
		}
		return null;
	}
	
	@Override
	public Long getCampaignOrgIdByApplicationId(Long applicationId) {
		try {
			BigInteger orgId =  (BigInteger) entityManager.createNativeQuery("SELECT org.user_org_id FROM fs_loan_application_master ms\r\n" + 
					"INNER JOIN users.`user_organisation_master` org ON (org.organisation_code = ms.loan_campaign_code OR org.campaign_url_code = ms.loan_campaign_code)\r\n" + 
					"WHERE ms.application_id =:applicationId").setParameter("applicationId", applicationId).getSingleResult();
			return orgId != null ? orgId.longValue() : null;
		} catch (Exception e) {
			LOGGER.error("Exception while get campaign bank id from application id " + applicationId,e);
		}
		return null;
	}
	
	@Override
	public Object[] getBankBureauFlags(Long orgId){
		try{
			Object [] count = (Object [])entityManager
					.createNativeQuery("SELECT org.is_msme_api_active,org.is_score_enable,org.is_cmr_enable,org.is_main_dir_score_enable,org.is_loans_enable,org.total_emi_of_company_enable,org.existing_loan_collateral_amount_enable,org.total_existing_limit_enable,org.total_existing_limit_by_loan_type_enable,org.total_emi_of_director FROM cibil_mudra.`organisation_master` org WHERE org.id =:orgId")
					.setParameter("orgId", orgId).getSingleResult();
			return count;
		}catch (Exception e) {
			LOGGER.error("Error while Getting Bank Bureau Flags ==>{}",e);
			return null;
		}
	}
	
	@Override
	public boolean getCibilBureauAPITrueOrFalse(Long orgId) {
		try {
			BigInteger id =  (BigInteger) entityManager.createNativeQuery("SELECT cb.id FROM `cibil_mudra`.`organisation_master` cb WHERE cb.id =:orgId AND cb.`is_msme_api_active` IS TRUE")
					.setParameter("orgId", orgId).getSingleResult();
			return id != null ? true : false;
		} catch (Exception e) {
			LOGGER.error("Exception while get Cibil bureau API true or false by OrgId " + orgId,e.getMessage());
		}
		return false;
	}

	
	@Override
	public List<BankingRelation> listBankRelationAppId(Long id) {
		List<BankingRelation> bankingRelations = new ArrayList<BankingRelation>();
		bankingRelations = entityManager
				.createQuery("SELECT o FROM BankingRelation o WHERE o.applicationId=:id AND o.isActive = TRUE AND o.coApplicantId IS NULL")
				.setParameter("id", id).getResultList();
		return bankingRelations;
	}

	
	@Override
	public List<BankingRelation> listBankRelationAppId(Long id, Long applicantId) {
		List<BankingRelation> bankingRelations = new ArrayList<BankingRelation>();
		bankingRelations = entityManager
				.createNativeQuery("SELECT o FROM BankingRelation o WHERE o.applicationId=:id AND o.isActive = TRUE  AND o.coApplicantId =:coAppId")
				.setParameter("applicantId", applicantId)
				.setParameter("id", id).getResultList();
		return bankingRelations;
	}

	@Override
	
	public Boolean getIsItrManualFilled(Long applicationId) {
		List<Boolean> result = 	entityManager.createNativeQuery("SELECT is_manual_filled FROM `itr_api_msme`.`itr_tracking` WHERE application_id =:appId AND is_active = TRUE ORDER BY id DESC LIMIT 1")
				.setParameter("appId", applicationId)
				.getResultList(); 
		return !CommonUtils.isListNullOrEmpty(result) ? result.get(0) : null;
	}

	@Override
	public Double getAllDirectorAverageBureauScore(Long applicationId) {
		Double averageScore = null;
		try{
			averageScore = (Double)entityManager.createNativeQuery("SELECT AVG(MaxData) AS ScoreData FROM (SELECT CAST(MAX(adrr.actual_score) AS UNSIGNED) AS MaxData FROM cibil_mudra.cibil_score_log_details adrr,cibil_mudra.cibil_mstr ms WHERE ms.cibil_id = adrr.cibil_id AND ms.application_id =:applicationId AND ms.is_active = TRUE GROUP BY adrr.pan) AS a").setParameter("applicationId", applicationId).getSingleResult();
		}catch(Exception e){
			LOGGER.error("Error while getting all director Average score for ApplicationId = >{}====>{}",applicationId,e);
		}
		return averageScore;
	}

	@Override
	public Boolean isNoBankStatement(Long applicationId) {
		BigInteger count = null;
		try{
			count = (BigInteger)entityManager.createNativeQuery("SELECT COUNT(a.id) FROM `pennydrop`.`audit_log_details` a WHERE a.application_id =:applicationId AND a.status = 1 AND a.request_type = '1' ORDER BY a.id DESC LIMIT 1").setParameter("applicationId", applicationId).getSingleResult();
		}catch(Exception e){
			LOGGER.error("Error while getting count for pennydrop for ApplicationId = >{}====>{}",applicationId,e);
		}
		if(CommonUtils.isObjectNullOrEmpty(count)){
			return false;
		}
		
		return count.intValue() > 0;
	}
	
	@Override
	public Integer getMinRelationshipInMonthByApplicationId(Long applicationId,String bankName) {
		BigInteger relationInMonths = null;
		try {
			relationInMonths = (BigInteger)entityManager.createNativeQuery("SELECT TIMESTAMPDIFF(MONTH,STR_TO_DATE(CONCAT('01,',o.since_month,',',o.since_year),'%d,%m,%Y'),NOW()) AS res FROM pennydrop.account_details o WHERE o.application_id =:id and o.is_active = true and o.since_month IS NOT NULL and o.since_year IS NOT NULL and o.bank_name =:bankName order by o.id desc limit 1")
					.setParameter("id", applicationId)
					.setParameter("bankName", bankName).getSingleResult();
		}
		catch (Exception e) {
			LOGGER.error("Error While fetching months in relation with banks =====>{}======{}",applicationId,e);
		}
		if(CommonUtils.isObjectNullOrEmpty(relationInMonths)){
			return 0;
		}
		return relationInMonths.intValue();
	}
	
	
	

	@Override
	public Integer getMinRelationshipInMonthByApplicationId(Long applicationId) {
		BigInteger relationInMonths = null;
		try {
			relationInMonths = (BigInteger)entityManager.createNativeQuery("SELECT TIMESTAMPDIFF(MONTH,STR_TO_DATE(CONCAT('01,',o.since_month,',',o.since_year),'%d,%m,%Y'),NOW()) AS res FROM pennydrop.account_details o WHERE o.application_id =:id and o.is_active = true and o.since_month IS NOT NULL and o.since_year IS NOT NULL order by o.id desc limit 1")
					.setParameter("id", applicationId).getSingleResult();
		}
		catch (Exception e) {
			LOGGER.error("Error While fetching months in relation with banks =====>{}======{}",applicationId,e);
		}
		if(CommonUtils.isObjectNullOrEmpty(relationInMonths)){
			return 0;
		}
		return relationInMonths.intValue();
	}

	@Override
	public Integer getMinRelationshipInMonthByApplicationIdAndNotGivenBank(Long applicationId, String bankName) {
		BigInteger relationInMonths = null;
		try {
			relationInMonths = (BigInteger)entityManager.createNativeQuery("SELECT TIMESTAMPDIFF(MONTH,STR_TO_DATE(CONCAT('01,',o.since_month,',',o.since_year),'%d,%m,%Y'),NOW()) AS res FROM pennydrop.account_details o WHERE o.application_id =:id and o.is_active = true and o.since_month IS NOT NULL and o.since_year IS NOT NULL and o.bank_name !=:bankName order by o.id desc limit 1")
					.setParameter("id", applicationId)
					.setParameter("bankName", bankName).getSingleResult();
		}
		catch (Exception e) {
			LOGGER.error("Error While fetching months in relation with banks =====>{}======{}",applicationId,e);
		}
		if(CommonUtils.isObjectNullOrEmpty(relationInMonths)){
			return 0;
		}
		return relationInMonths.intValue();
	}

	@Override
	public String getIFSCByApplicationId(Long applicationId) {
		String ifsc = null;
		try {
			ifsc = (String)entityManager.createNativeQuery("SELECT o.ifsc FROM pennydrop.account_details o WHERE o.application_id =:id and o.is_active = true and o.since_month IS NOT NULL and o.since_year IS NOT NULL order by o.id desc limit 1").setParameter("id", applicationId).getSingleResult();
		}
		catch (Exception e) {
			LOGGER.error("Error While fetching IFSC by Application =====>{}======{}",applicationId,e);
		}
		return ifsc;
	}

	@Override
	public String getBankNameByIFSC(String ifscPrefix) {
		String bankName = null;
		try {
			bankName = (String)entityManager.createNativeQuery("SELECT sb.name FROM statement_analyzer_msme.banklist_data sb WHERE LOWER(sb.ifsc_prefix) = LOWER(:ifscPrefix) ORDER BY sb.id DESC LIMIT 1").setParameter("ifscPrefix", ifscPrefix).getSingleResult();
		}
		catch (Exception e) {
			LOGGER.error("Error While fetching Bank Name by IFSC =====>{}======{}",ifscPrefix,e);
		}
		return bankName;
	}

	@Override
	public boolean updateProfileVersIdInConnect(Long applicationId, Long profileVerMapId) {
		try {
			int update = entityManager.createNativeQuery("UPDATE connect_cvl_mudra.connect_log c SET c.profile_ver_map_id=:profileVerMapId WHERE c.application_id=:applicationId")
					.setParameter("applicationId", applicationId)
					.setParameter("profileVerMapId", profileVerMapId)
					.executeUpdate();
			return update > 0;	
		} catch (Exception e) {
			LOGGER.error("Excecption while Update Profile Vers ID in COneec By Application Id ===> " + applicationId, e);
			return false;
		}
	}
	
	@Override
	public boolean updateProfileVersIdInLoanMaster(Long applicationId, Long profileVerMapId) {
		try {
			int update = entityManager.createNativeQuery("UPDATE `fs_loan_application_master` SET `profile_mapping_id` =:profileVerMapId WHERE `application_id` =:applicationId")
					.setParameter("applicationId", applicationId)
					.setParameter("profileVerMapId", profileVerMapId)
					.executeUpdate();
			return update > 0;	
		} catch (Exception e) {
			LOGGER.error("Excecption while Update Profile Vers ID in Loan Application Master Application Id ===> " + applicationId, e);
			return false;
		}
	}
	
	@Override
	public Integer getVersionFromOrgId(Long orgId) {
		List<Integer> fpDetails = (List<Integer>) entityManager
				.createNativeQuery("SELECT version FROM fp_uniform_product_details uni,fp_product_master pm WHERE pm.fp_product_id = uni.fp_product_id and pm.user_org_id =:userOrgId and pm.is_active = true order by uni.fp_product_id desc limit 1")
				.setParameter("userOrgId", orgId).getResultList();
		if(CommonUtils.isListNullOrEmpty(fpDetails)){
			return null;
		}
		return fpDetails.get(0);
	}
	
	@Override
	public Long getProfileMappingIdByApplicationId(Long applicationId) {
		List<BigInteger> result = entityManager.createNativeQuery("SELECT profile_mapping_id FROM `loan_application_cvl_mudra`.`fs_loan_application_master` WHERE application_id =:applicationId")
				 .setParameter("applicationId",applicationId)
				 .getResultList();
		 return !CommonUtils.isListNullOrEmpty(result) ? result.get(0).longValue() : null; 
	}

	
	@Override
	public Object[] getProfileVersionDetailsByProfileId(Long profileId) {
		List<Object[]> result = entityManager.createNativeQuery("SELECT itr_id,gst_id,bs_id FROM `user_profile`.`profile_version_mapping` WHERE id =:profileId AND is_active = TRUE ORDER BY `version` DESC limit 1")
				 .setParameter("profileId",profileId)
				 .getResultList();
		 return !CommonUtils.isListNullOrEmpty(result) ? result.get(0) : null;
	}

	@Override
	public Object[] getProfileVersionDetailsByApplicationId(Long applicationId) {
		List<Object[]> result = entityManager.createNativeQuery("SELECT itr_id,gst_id,bs_id,id FROM `user_profile`.`profile_version_mapping` WHERE id = (SELECT profile_mapping_id FROM `loan_application_cvl_mudra`.`fs_loan_application_master` WHERE application_id =:applicationId) AND is_active = TRUE ORDER BY `version` DESC limit 1")
				 .setParameter("applicationId",applicationId)
				 .getResultList();
		 return !CommonUtils.isListNullOrEmpty(result) ? result.get(0) : null;
	}
	@Override
	public Boolean isManualBs(Long bsId) {
		try {
			Boolean isManual =  (Boolean) entityManager.createNativeQuery("SELECT is_manual_upload FROM `statement_analyzer_msme`.`bs_profile_master` WHERE id =:bsId")
					.setParameter("bsId", bsId).getSingleResult();
			return (isManual != null && isManual);
		} catch (Exception e) {
			LOGGER.error("Exception while get is Bank Statement Uploaded  or Manual Filled for BS Id : " + bsId,e.getMessage());
		}
		return false;
	}

	@Override
	public int checkApplicationStageforMultiBank(Long applicationId) {
		try {
			BigInteger cnt = (BigInteger) entityManager.createNativeQuery(" SELECT COUNT(*) FROM connect_cvl_mudra.connect_log  WHERE application_id=:applicationId AND is_active IS TRUE\n" +
					"  AND (stage_id=1006 OR stage_id=1010) ORDER BY id DESC LIMIT 1")
					.setParameter("applicationId", applicationId).getSingleResult();
			return cnt != null ? cnt.intValue() : null;
		} catch (Exception e) {
			LOGGER.error("Exception while get Count of checkApplicationStageforMultiBank from application id " + applicationId, e.getMessage());
		}
		return 0;
	}
	
	public Long getUserTypeByEmail(String email) {
		try {
			BigInteger id = (BigInteger) entityManager.createNativeQuery("SELECT user_id FROM users.users WHERE email =:email")
					.setParameter("email", email)
					.getResultList()
					.stream().findFirst().orElse(null);
			return id != null ? id.longValue() : 0L;
		} catch (Exception e) {
			LOGGER.error(CommonUtils.EXCEPTION,e);
		}
		return 0L;
	}
	
}
