package com.opl.service.loans.repository.common.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.domain.common.HomeLoanEligibilityCriteria;
import com.opl.service.loans.domain.common.LAPEligibilityCriteria;
import com.opl.service.loans.domain.common.PersonalLoanEligibilityCriteria;
import com.opl.service.loans.repository.common.LoanEligibilityCriteriaRepository;
import com.opl.service.loans.utils.CommonDocumentUtils;

@Repository
public class LoanEligibilityCriteriaRepositoryImpl implements LoanEligibilityCriteriaRepository {

	private static final Logger logger = LoggerFactory.getLogger(LoanEligibilityCriteriaRepositoryImpl.class);
	private static final String GIVEN_CRITERIA_DOES_NOT_MATCH_WITH_THE_DATABASE_RECORDS_MSG = "Given Criteria Does not match with the Database Records";
	private static final String GET_HOME_LOAN_BY_SALARY_SLAB = "getHomeLoanBySalarySlab";
	private static final String GET_PERSONAL_LOAN_BY_SALARY_SLAB = "getPersonalLoanBySalarySlab";
	private static final String GET_LAP_BY_SALARY_SLAB = "getLAPBySalarySlab";
	private static final String GET_HOME_LOAN_BY_SVMV = "getHomeLoanBySVMV";
	private static final String BANK_ID = "bankId";

	@PersistenceContext
	private EntityManager entityManager;

	// HomeLoan Starts
	@Override
	public HomeLoanEligibilityCriteria getHomeLoanBySalarySlab(Long income, Integer type, Integer bankId) {

		CommonDocumentUtils.startHook(logger, GET_HOME_LOAN_BY_SALARY_SLAB);
		String query = "select hl from HomeLoanEligibilityCriteria hl where hl.type =:type and hl.bankId =:bankId and hl.isActive =:isActive and "
				+ income + " >= hl.min and " + income + " <= hl.max";
		List<HomeLoanEligibilityCriteria> eligibility = entityManager
				.createQuery(query, HomeLoanEligibilityCriteria.class).setParameter("type", type)
				.setParameter(BANK_ID, bankId).setParameter(CommonUtils.IS_ACTIVE, true).getResultList();
		if (!CommonUtils.isListNullOrEmpty(eligibility)) {
			CommonDocumentUtils.endHook(logger, GET_HOME_LOAN_BY_SALARY_SLAB);
			return eligibility.get(0);
		}
		logger.warn(GIVEN_CRITERIA_DOES_NOT_MATCH_WITH_THE_DATABASE_RECORDS_MSG);
		CommonDocumentUtils.endHook(logger, GET_HOME_LOAN_BY_SALARY_SLAB);
		return null;
	}

	@Override
	public Float getHomeLoanBySV(Long sv,Integer bankId) {

		CommonDocumentUtils.startHook(logger, GET_HOME_LOAN_BY_SVMV);
//		hl.type =:type and
		String query = "select hl.saleDeedValue from HomeLoanEligibilityCriteria hl where hl.bankId =:bankId and hl.isActive =:isActive and ("
				+ sv + " >= hl.minPropertyAmount and " + sv + " <= hl.maxPropertyAmount) order by hl.id";
		List<Float> eligibility = entityManager
				.createQuery(query, Float.class)
				.setParameter(BANK_ID, bankId).setParameter(CommonUtils.IS_ACTIVE, true).getResultList();
		if (!CommonUtils.isListNullOrEmpty(eligibility)) {
			CommonDocumentUtils.endHook(logger, GET_HOME_LOAN_BY_SVMV);
			return eligibility.get(0);
		}
		logger.warn(GIVEN_CRITERIA_DOES_NOT_MATCH_WITH_THE_DATABASE_RECORDS_MSG);
		CommonDocumentUtils.endHook(logger, GET_HOME_LOAN_BY_SVMV);
		return null;
	}
	
	@Override
	public Float getHomeLoanByMV(Long mv, Integer bankId) {

				CommonDocumentUtils.startHook(logger, GET_HOME_LOAN_BY_SVMV);
//				hl.type =:type and
				String query = "select hl.marketValue from HomeLoanEligibilityCriteria hl where hl.bankId =:bankId and hl.isActive =:isActive and (" + mv
						+ " >= hl.minPropertyAmount and " + mv + " <= hl.maxPropertyAmount) order by hl.id";
				List<Float> eligibility = entityManager
						.createQuery(query, Float.class)
						.setParameter(BANK_ID, bankId).setParameter(CommonUtils.IS_ACTIVE, true).getResultList();
				if (!CommonUtils.isListNullOrEmpty(eligibility)) {
					CommonDocumentUtils.endHook(logger, GET_HOME_LOAN_BY_SVMV);
					return eligibility.get(0);
				}
				logger.warn(GIVEN_CRITERIA_DOES_NOT_MATCH_WITH_THE_DATABASE_RECORDS_MSG);
				CommonDocumentUtils.endHook(logger, GET_HOME_LOAN_BY_SVMV);
				return null;

	}

	@Override
	public Object[] getMinMaxRoiForHomeLoan(List<Integer> bankIds) {
		if (CommonUtils.isListNullOrEmpty(bankIds)) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Object[]> data = entityManager.createQuery(
				"select min(hl.roiLow),max(hl.roiHigh) from HomeLoanEligibilityCriteria hl where hl.isActive =:isActive and hl.bankId in (:ids)")
				.setParameter(CommonUtils.IS_ACTIVE, true).setParameter("ids", bankIds).getResultList();
		if (!CommonUtils.isListNullOrEmpty(data)) {
			return data.get(0);
		}
		return null;
	}
	// Home Loans Ends

	// Personal Loan Starts
	@Override
	public PersonalLoanEligibilityCriteria getPersonalLoanBySalarySlab(Long income, Integer type, Integer bankId) {
		CommonDocumentUtils.startHook(logger, GET_PERSONAL_LOAN_BY_SALARY_SLAB);
		String query = "select pl from PersonalLoanEligibilityCriteria pl where pl.type =:type and pl.bankId =:bankId and pl.isActive =:isActive and "
				+ income + " >= pl.min and " + income + " <= pl.max";
		List<PersonalLoanEligibilityCriteria> eligibility = entityManager
				.createQuery(query, PersonalLoanEligibilityCriteria.class).setParameter("type", type)
				.setParameter(BANK_ID, bankId).setParameter(CommonUtils.IS_ACTIVE, true).getResultList();
		if (!CommonUtils.isListNullOrEmpty(eligibility)) {
			CommonDocumentUtils.endHook(logger, GET_PERSONAL_LOAN_BY_SALARY_SLAB);
			return eligibility.get(0);
		}
		logger.warn(GIVEN_CRITERIA_DOES_NOT_MATCH_WITH_THE_DATABASE_RECORDS_MSG);
		CommonDocumentUtils.endHook(logger, GET_PERSONAL_LOAN_BY_SALARY_SLAB);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getMinMaxRoiForPersonalLoan(List<Integer> bankIds, Integer type) {
		if (CommonUtils.isListNullOrEmpty(bankIds)) {
			return null;
		}
		
		List<Object[]> data = entityManager.createQuery(
				"select min(pl.roiLow),max(pl.roiHigh) from PersonalLoanEligibilityCriteria pl where pl.isActive =:isActive and pl.type =:type and pl.bankId in (:ids)")
				.setParameter(CommonUtils.IS_ACTIVE, true)
				.setParameter("type", type)
				.setParameter("ids", bankIds)
				.getResultList();

		if (!CommonUtils.isListNullOrEmpty(data)) {
			return data.get(0);
		}
		return null;
	}

	// Personal Loan Ends

	// LAP Starts
	@Override
	public LAPEligibilityCriteria getLAPBySalarySlab(Long income, Integer type, Integer bankId, Integer propertyType) {
		CommonDocumentUtils.startHook(logger, GET_LAP_BY_SALARY_SLAB);
		String query = "select lap from LAPEligibilityCriteria lap where lap.type =:type and lap.bankId =:bankId and lap.isActive =:isActive and lap.propertyType =:propertyType and "
				+ income + " >= lap.min";
		List<LAPEligibilityCriteria> eligibility = entityManager.createQuery(query, LAPEligibilityCriteria.class)
				.setParameter("type", type).setParameter(BANK_ID, bankId).setParameter("propertyType", propertyType)
				.setParameter(CommonUtils.IS_ACTIVE, true).getResultList();
		if (!CommonUtils.isListNullOrEmpty(eligibility)) {
			CommonDocumentUtils.endHook(logger, GET_LAP_BY_SALARY_SLAB);
			return eligibility.get(0);
		}
		logger.warn(GIVEN_CRITERIA_DOES_NOT_MATCH_WITH_THE_DATABASE_RECORDS_MSG);
		CommonDocumentUtils.endHook(logger, GET_LAP_BY_SALARY_SLAB);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getMinMaxRoiForLAP(List<Integer> bankIds, Integer employementType, Integer propertyType) {
		if (CommonUtils.isListNullOrEmpty(bankIds)) {
			logger.warn("Bank Ids got Null or Empty");
			return null;
		}
		List<Object[]> data = entityManager.createQuery(
				"select min(lap.roiLow),max(lap.roiHigh) from LAPEligibilityCriteria lap where lap.isActive =:isActive and lap.bankId in (:ids) and lap.type =:employementType and lap.propertyType =:propertyType")
				.setParameter(CommonUtils.IS_ACTIVE, true).setParameter("ids", bankIds).setParameter("propertyType", propertyType)
				.setParameter("employementType", employementType).getResultList();
		if (!CommonUtils.isListNullOrEmpty(data)) {
			return data.get(0);
		}
		return null;
	}

	// LAP Ends

}
