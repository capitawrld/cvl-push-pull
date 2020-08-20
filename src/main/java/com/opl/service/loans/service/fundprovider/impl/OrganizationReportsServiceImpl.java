package com.opl.service.loans.service.fundprovider.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.model.ReportResponse;
import com.opl.mudra.api.loans.model.reports.OrganizationPieChartResponse;
import com.opl.service.loans.service.fundprovider.OrganizationReportsService;

/**
 * Created by dhaval on 02-Dec-17.
 */
@Service
@Transactional
public class OrganizationReportsServiceImpl implements OrganizationReportsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private Environment environment;

    private static final Logger logger = LoggerFactory.getLogger(OrganizationReportsServiceImpl.class);
    private static final String PROPERTY_NAME_DATABASE_NAME = "capitaworld.loans.db.name";
    private static final String ORGANIZATION_ID = "organization_id";

    @Override
    public String getCountOfProposalInInboxAndPrimary(Long organization_id) {
        logger.info("Enter in getCountOfProposalInInboxAndPrimary() methods param",organization_id);
        String count = null;
        try {
            count = entityManager
                    .createNativeQuery("SELECT COUNT(*) FROM proposal_details WHERE proposal_details.fp_product_id IN " +
                            "(SELECT fp_product_master.fp_product_id FROM fp_product_master WHERE fp_product_master.user_org_id=:organization_id AND fp_product_master.is_active = TRUE) " +
                            "AND proposal_details.is_active = TRUE AND proposal_details.proposal_status_id IN (1,2); ")
                    .setParameter(ORGANIZATION_ID, organization_id).getSingleResult().toString();
        } catch (Exception e) {
            count = "0";
            logger.error("Error while getting data getCountOfProposalInInboxAndPrimary() methods ", e);
        }
        logger.info("response getCountOfProposalInInboxAndPrimary() methods ",count);
        return count;
    }

    @Override
    public String getCountOfAdvance(Long organization_id) {
        logger.info("Enter in getCountOfAdvance() methods param",organization_id);
        String count = null;
        try {
            count = entityManager
                    .createNativeQuery("SELECT COUNT(*) FROM proposal_details WHERE proposal_details.fp_product_id IN " +
                            "(SELECT fp_product_master.fp_product_id FROM fp_product_master WHERE fp_product_master.user_org_id=:organization_id AND fp_product_master.is_active = TRUE) " +
                            "AND proposal_details.is_active = TRUE AND proposal_details.proposal_status_id=5; ")
                    .setParameter(ORGANIZATION_ID, organization_id).getSingleResult().toString();
        } catch (Exception e) {
            count = "0";
            logger.error("Error while getting data getCountOfAdvance() methods ", e);
        }
        logger.info("response getCountOfAdvance() methods ",count);
        return count;
    }

    @Override
    public String getSumOfAmountProposalInInboxAndPrimary(Long organization_id) {
        logger.info("Enter in getSumOfAmountProposalInInboxAndPrimary() methods param",organization_id);
        String count = null;
        try {
            count = entityManager
                    .createNativeQuery("SELECT SUM(CASE fs_loan_application_master.denomination_id " +
                            "WHEN 1 THEN (fs_loan_application_master.amount*100000)" +
                            "WHEN 2 THEN (fs_loan_application_master.amount*1000000)" +
                            "WHEN 3 THEN (fs_loan_application_master.amount*10000000)" +
                            "WHEN 4 THEN (fs_loan_application_master.amount*100000000)" +
                            "WHEN 5 THEN (fs_loan_application_master.amount*1)" +
                            "ELSE fs_loan_application_master.amount END) AS sum_amount " +
                            "FROM fs_loan_application_master " +
                            "WHERE fs_loan_application_master.application_id IN " +
                            "(SELECT proposal_details.application_id FROM proposal_details WHERE proposal_details.fp_product_id " +
                            "IN (SELECT fp_product_master.fp_product_id FROM fp_product_master WHERE fp_product_master.user_org_id=:organization_id AND fp_product_master.is_active = TRUE) " +
                            "AND proposal_details.is_active = TRUE AND proposal_details.proposal_status_id IN (1,2));")
                    .setParameter(ORGANIZATION_ID, organization_id).getSingleResult().toString();
        } catch (Exception e) {
            count = "0";
            logger.error("Error while getting data getCountOfAdvance() methods ", e);
        }
        logger.info("response getCountOfAdvance() methods ",count);
        return count;
    }

    @Override
    public String getSumOfAmountProposalInAdvance(Long organization_id) {
        logger.info("Enter in getSumOfAmountProposalInAdvance() methods param",organization_id);
        String count = null;
        try {
            count = entityManager
                    .createNativeQuery("SELECT SUM(CASE fs_loan_application_master.denomination_id " +
                            "WHEN 1 THEN (fs_loan_application_master.amount*100000)" +
                            "WHEN 2 THEN (fs_loan_application_master.amount*1000000)" +
                            "WHEN 3 THEN (fs_loan_application_master.amount*10000000)" +
                            "WHEN 4 THEN (fs_loan_application_master.amount*100000000)" +
                            "WHEN 5 THEN (fs_loan_application_master.amount*1)" +
                            "ELSE fs_loan_application_master.amount END) AS sum_amount " +
                            "FROM fs_loan_application_master " +
                            "WHERE fs_loan_application_master.application_id IN " +
                            "(SELECT proposal_details.application_id FROM proposal_details WHERE proposal_details.fp_product_id " +
                            "IN (SELECT fp_product_master.fp_product_id FROM fp_product_master WHERE fp_product_master.user_org_id=:organization_id AND fp_product_master.is_active = TRUE) " +
                            "AND proposal_details.is_active = TRUE AND proposal_details.proposal_status_id = 5);")
                    .setParameter(ORGANIZATION_ID, organization_id).getSingleResult().toString();
        } catch (Exception e) {
            count = "0";
            logger.error("Error while getting data getSumOfAmountProposalInAdvance() methods ", e);
        }
        logger.info("response getSumOfAmountProposalInAdvance() methods ",count);
        return count;
    }

    @Override
    public String getBranchPieDetails(Long organizationId) {
        logger.info("Enter in getBranchPieDetails() methods param",organizationId);
        String count = "[";
        List<Objects[]> objectsList = null;
        try {
            objectsList = entityManager
                    .createNativeQuery("SELECT branch_name,COUNT(application_id) FROM org_branch_audit WHERE user_org_id=:organization_id AND is_active = TRUE GROUP BY branch_id;")
                    .setParameter(ORGANIZATION_ID, organizationId).getResultList();
            for (Object[] a : objectsList) {
                count +="{\"name\":\"" + a[0] + "\",\"y\":\"" + a[1]+"\"},";
            }
        } catch (Exception e) {
            count = "null";
            logger.error("Error while getting data getBranchPieDetails() methods ", e);
        }
        count = count.substring(0,(count.length()-1));
        count+="]";
        logger.info("response getBranchPieDetails() methods ",count);
        return count;
    }

    @Override
    public OrganizationPieChartResponse getDetails(Long organization_id) {
        OrganizationPieChartResponse organizationPieChartResponse = new OrganizationPieChartResponse();
        organizationPieChartResponse.setApprovedAmount(getSumOfAmountProposalInAdvance(organization_id));
        organizationPieChartResponse.setApprovedFiles(getCountOfAdvance(organization_id));
        organizationPieChartResponse.setUnderProcessFiles(getCountOfProposalInInboxAndPrimary(organization_id));
        organizationPieChartResponse.setUnderProcessAmount(getSumOfAmountProposalInInboxAndPrimary(organization_id));
        organizationPieChartResponse.setBranchPieResponce(getBranchPieDetails(organization_id));
        return organizationPieChartResponse;
    }

    @Override
    public List<List<Long>> getApplicationIdAndUserId() {
        String dbName = environment.getRequiredProperty(PROPERTY_NAME_DATABASE_NAME);
        List<Long> applicationId = new ArrayList<>();
        List<Long> userId = new ArrayList<>();
        List<List<Long>> master = new ArrayList<>();
        List<Objects[]> objectsList = null;
        try {
            objectsList = entityManager
                    .createNativeQuery("SELECT application_id,user_id FROM "+ dbName +".fs_loan_application_master WHERE fs_loan_application_master.application_id IN (SELECT application_id FROM "+ dbName +".proposal_details WHERE "+ dbName +".proposal_details.application_id IN (SELECT fs_loan_application_master.application_id FROM "+ dbName +".fs_loan_application_master WHERE fs_loan_application_master.user_id IN(SELECT campaign_details.user_id FROM users.campaign_details WHERE campaign_details.is_active = TRUE) AND fs_loan_application_master.is_active = TRUE) AND proposal_details.is_active = TRUE AND proposal_details.proposal_status_id=4 AND proposal_details.fp_product_id IN (SELECT fp_product_master.fp_product_id FROM "+ dbName +".fp_product_master WHERE fp_product_master.user_id IN (SELECT users.user_id FROM users.users WHERE users.user_type_id=2 AND users.user_org_id = 1 AND users.is_self_active = TRUE)))")
                    .getResultList();
            for (Object[] a : objectsList) {
                applicationId.add(Long.valueOf(a[0].toString()));
                userId.add(Long.valueOf(a[1].toString()));
            }
        } catch (Exception e) {
            master = null;
        }
        if (master != null) {
            master.add(applicationId);
            master.add(userId);
        }
        return master;
    }
    @Override
    public List<List<Long>> getApplicationIdAndUserIdForAdminPanel() {
        String dbName = environment.getRequiredProperty(PROPERTY_NAME_DATABASE_NAME);
        List<Long> applicationId = new ArrayList<>();
        List<Long> userId = new ArrayList<>();
        List<List<Long>> master = new ArrayList<>();
        List<Objects[]> objectsList = null;
        try {
            objectsList = entityManager
                    .createNativeQuery("SELECT application_id,user_id FROM "+ dbName +".fs_loan_application_master WHERE fs_loan_application_master.application_id IN (SELECT application_id FROM "+ dbName +".proposal_details WHERE "+ dbName +".proposal_details.application_id IN (SELECT fs_loan_application_master.application_id FROM "+ dbName +".fs_loan_application_master WHERE fs_loan_application_master.user_id IN(SELECT campaign_details.user_id FROM users.campaign_details WHERE campaign_details.is_active = TRUE) AND fs_loan_application_master.is_active = TRUE) AND proposal_details.is_active = TRUE AND proposal_details.proposal_status_id=5 AND proposal_details.fp_product_id IN (SELECT fp_product_master.fp_product_id FROM "+ dbName +".fp_product_master WHERE fp_product_master.user_id IN (SELECT users.user_id FROM users.users WHERE users.user_type_id=2 AND users.user_org_id = 1 AND users.is_self_active = TRUE)))")
                    .getResultList();
            for (Object[] a : objectsList) {
                applicationId.add(Long.valueOf(a[0].toString()));
                userId.add(Long.valueOf(a[1].toString()));
            }
        } catch (Exception e) {
            master = null;
        }
        if (master != null) {
            master.add(applicationId);
            master.add(userId);
        }
        return master;
    }

    @Override
    public List<ReportResponse> getFpProductMappingId() {
        String dbName = environment.getRequiredProperty(PROPERTY_NAME_DATABASE_NAME);
        List<ReportResponse> reportResponseList = new ArrayList<>();
        List<Objects[]> objectsList = null;
        try {
            objectsList = entityManager
                    .createNativeQuery("SELECT pd.fp_product_id,pd.application_id,la.user_id FROM (SELECT proposal_details.fp_product_id,proposal_details.application_id FROM "+ dbName +".proposal_details WHERE "+ dbName +".proposal_details.application_id IN (SELECT fs_loan_application_master.application_id FROM "+ dbName +".fs_loan_application_master WHERE fs_loan_application_master.user_id IN(SELECT campaign_details.user_id FROM users.campaign_details WHERE campaign_details.is_active = TRUE) AND fs_loan_application_master.is_active = TRUE) AND proposal_details.is_active = TRUE AND proposal_details.proposal_status_id=5 AND proposal_details.fp_product_id IN (SELECT fp_product_master.fp_product_id FROM "+ dbName +".fp_product_master WHERE fp_product_master.user_id IN (SELECT users.user_id FROM users.users WHERE users.user_type_id=2 AND users.user_org_id = 1 AND users.is_self_active = TRUE))) AS pd LEFT JOIN (SELECT application_id,user_id FROM "+ dbName +".fs_loan_application_master WHERE fs_loan_application_master.application_id IN (SELECT application_id FROM "+ dbName +".proposal_details WHERE "+ dbName +".proposal_details.application_id IN (SELECT fs_loan_application_master.application_id FROM "+ dbName +".fs_loan_application_master WHERE fs_loan_application_master.user_id IN(SELECT campaign_details.user_id FROM users.campaign_details WHERE campaign_details.is_active = TRUE) AND fs_loan_application_master.is_active = TRUE) AND proposal_details.is_active = TRUE AND proposal_details.proposal_status_id=5 AND proposal_details.fp_product_id IN (SELECT fp_product_master.fp_product_id FROM "+ dbName +".fp_product_master WHERE fp_product_master.user_id IN (SELECT users.user_id FROM users.users WHERE users.user_type_id=2 AND users.user_org_id = 1 AND users.is_self_active = TRUE)))) AS la ON pd.application_id = la.application_id")
                    .getResultList();
            for (Object[] a : objectsList) {
                ReportResponse reportResponse = new ReportResponse();
                reportResponse.setFpProductId(Long.valueOf(a[0].toString()));
                reportResponse.setApplicationId(Long.valueOf(a[1].toString()));
                reportResponse.setUserId(Long.valueOf(a[2].toString()));
                reportResponseList.add(reportResponse);
            }
        } catch (Exception e) {
            reportResponseList = null;
        }
        return reportResponseList;
    }
}