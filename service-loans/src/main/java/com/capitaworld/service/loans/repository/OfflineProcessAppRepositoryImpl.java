package com.capitaworld.service.loans.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

import com.capitaworld.service.loans.utils.CommonUtils;
import org.springframework.stereotype.Repository;
import com.capitaworld.service.loans.domain.fundseeker.IneligibleProposalDetails;


@Repository
public class OfflineProcessAppRepositoryImpl implements OfflineProcessedAppRepository{

	private static final String APP_ID = "appId";
	private static final String RESULT  = "result";
	private static final String ORG_ID = "orgId";
	private static final String FROM_DATE = "fromDate";
	private static final String TO_DATE = "toDate";
	private static final String CONNECT_FLOW_TYPE_ID = "connectFlowTypeId";

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public String getInEligibleRecordList(Long userId,Date fromDate,Date toDate) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchOfflinePendingProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(FROM_DATE,Date.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(TO_DATE,Date.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CONNECT_FLOW_TYPE_ID,ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		storedProcedureQuery.setParameter(FROM_DATE,fromDate);
		storedProcedureQuery.setParameter(TO_DATE,toDate);
		storedProcedureQuery.setParameter(CONNECT_FLOW_TYPE_ID,-1);
		return (String) storedProcedureQuery.getSingleResult();
	}
	
	@Override
	public IneligibleProposalDetails findByAppliationId(Long applicationId,Long orgId) {
		List<IneligibleProposalDetails> data = entityManager.createQuery("SELECT ipd FROM IneligibleProposalDetails ipd where ipd.applicationId =:applicationId and ipd.userOrgId =:orgId and ipd.isActive = true",IneligibleProposalDetails.class)
				.setParameter("applicationId", applicationId)
				.setParameter(ORG_ID, orgId)
				.getResultList();
		if(data != null && !data.isEmpty()) {
			return data.get(0);
		}
		return null;
	}

	public boolean updateSanctionedFlag(Long appId,Long orgId,Long branchId,Long userId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spUpdateOfflineSanctionedFlag");
		storedProcedureQuery.registerStoredProcedureParameter(APP_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(ORG_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter("branchId",Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter("userId",Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(RESULT,Boolean.class, ParameterMode.OUT);
		storedProcedureQuery.setParameter(APP_ID,appId);
		storedProcedureQuery.setParameter(ORG_ID,orgId);
		storedProcedureQuery.setParameter("branchId",branchId);
		storedProcedureQuery.setParameter("userId",userId);
		storedProcedureQuery.execute();
		return (Boolean) storedProcedureQuery.getOutputParameterValue(RESULT);
	}

	public Integer checkBeforeOfflineSanctioned(Long appId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spCheckBeforeOfflineSanctioned");
		storedProcedureQuery.registerStoredProcedureParameter(APP_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(RESULT,Integer.class, ParameterMode.OUT);
		storedProcedureQuery.setParameter(APP_ID,appId);
		storedProcedureQuery.execute();
		return (Integer) storedProcedureQuery.getOutputParameterValue(RESULT);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getSanctionedApplicationList(Long userId,Date fromDate,Date toDate) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchOfflineSanctionedProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(FROM_DATE,Date.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(TO_DATE,Date.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CONNECT_FLOW_TYPE_ID,ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		storedProcedureQuery.setParameter(FROM_DATE,fromDate);
		storedProcedureQuery.setParameter(TO_DATE,toDate);
		storedProcedureQuery.setParameter(CONNECT_FLOW_TYPE_ID,-1);
		return (String) storedProcedureQuery.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getDisbursedApplicationList(Long userId,Date fromDate,Date toDate) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchOfflineDisbursedProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(FROM_DATE,Date.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(TO_DATE,Date.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CONNECT_FLOW_TYPE_ID,ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		storedProcedureQuery.setParameter(FROM_DATE,fromDate);
		storedProcedureQuery.setParameter(TO_DATE,toDate);
		storedProcedureQuery.setParameter(CONNECT_FLOW_TYPE_ID,-1);
		return (String) storedProcedureQuery.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getRejectProposalsList(Long userId,Date fromDate,Date toDate) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchOfflineRejectProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(FROM_DATE,Date.class, ParameterMode.IN);
		storedProcedureQuery.registerStoredProcedureParameter(TO_DATE,Date.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CONNECT_FLOW_TYPE_ID,ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		storedProcedureQuery.setParameter(FROM_DATE,fromDate);
		storedProcedureQuery.setParameter(TO_DATE,toDate);
		storedProcedureQuery.setParameter(CONNECT_FLOW_TYPE_ID,-1);
		return (String) storedProcedureQuery.getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getOtherProposalsList(Long userId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchOfflineOtherProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		return (String) storedProcedureQuery.getSingleResult();
	}


	@SuppressWarnings("unchecked")
	@Override 
	public List<Object[]> getHomeCounterDetail(){
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spGetHomeCounter");
		return  (List<Object[]>) storedProcedureQuery.getResultList() ;
	 }

	@SuppressWarnings("unchecked")
	@Override
	public String getUniformApplications(Long userId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchUniformPendingProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		return (String) storedProcedureQuery.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getUniformSanctionedApplicationList(Long userId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchUniformSanctionedProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		return (String) storedProcedureQuery.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getUniformDisbursedApplicationList(Long userId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchUniformDisbursedProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		return (String) storedProcedureQuery.getSingleResult();
	}

	@Override
	public String getUniformRejectProposalsList(Long userId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchUniformRejectProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		return (String) storedProcedureQuery.getSingleResult();
	}

	@Override
	public String getUniformOtherProposalsList(Long userId) {
		StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("spFetchUniformOtherProposal");
		storedProcedureQuery.registerStoredProcedureParameter(CommonUtils.USER_ID,Long.class, ParameterMode.IN);
		storedProcedureQuery.setParameter(CommonUtils.USER_ID,userId);
		return (String) storedProcedureQuery.getSingleResult();
	}
}
