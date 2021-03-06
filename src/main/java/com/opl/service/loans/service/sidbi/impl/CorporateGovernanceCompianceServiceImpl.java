/**
 * 
 */
package com.opl.service.loans.service.sidbi.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.sidbi.CorporateGovernanceComplianceRequest;
import com.opl.service.loans.domain.sidbi.CorporateGovernanceCompliance;
import com.opl.service.loans.repository.sidbi.CorporateGovernanceCompianceRepository;
import com.opl.service.loans.service.sidbi.CorporateGovernanceCompianceService;

/**
 * @author mohammad.maaz
 *
 */
@Service
public class CorporateGovernanceCompianceServiceImpl implements CorporateGovernanceCompianceService{

	@Autowired
    CorporateGovernanceCompianceRepository corpoRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(CorporateGovernanceCompianceServiceImpl.class);
	
	/* (non-Javadoc)
	 * @see CorporateGovernanceCompianceService#saveCorporate(CorporateGovernanceCompliance)
	 */
	@Override
	@org.springframework.transaction.annotation.Transactional(rollbackFor=LoansException.class)
	public Boolean saveCorporate(List<CorporateGovernanceComplianceRequest> corporateGover) throws Exception {
		Date currentDate = new Date();
		for (CorporateGovernanceComplianceRequest corpoReq : corporateGover) {
			if(corpoReq.getApplicationId() == null) {
				logger.error("Requested Application id is null while saving Corporate governance details {}",corporateGover);
				throw new LoansException("Validation error kindly refresh");
			}
			if(corpoReq.getUserId() == null) {
				logger.error("Requested User id is null while saving Corporate governance details {}",corporateGover);
				throw new LoansException("Validation error kindly refresh");
			}
			if(corpoReq.getSelectedOption() == null) {
				logger.error("Validation error : Selected Option is null while saving Corporate governance details {}",corporateGover);
				throw new LoansException("Validation error : Select Option field");
			}
			
			CorporateGovernanceCompliance corpoDomain = corpoRepository.findByApplicationIdAndCorporateGovernanceId(corpoReq.getApplicationId(), corpoReq.getCorporateGovernanceId());
			if(corpoDomain==null) {
				corpoDomain=new CorporateGovernanceCompliance();
				BeanUtils.copyProperties(corpoReq, corpoDomain);
			}
			
			if(corpoDomain.getId() == null) {
				corpoDomain.setCreatedBy(corpoReq.getUserId());
				corpoDomain.setCreatedDate(currentDate);
			}else {
				
				corpoDomain.setCorporateGovernanceId(corpoReq.getCorporateGovernanceId());
				corpoDomain.setSelectedOption(corpoReq.getSelectedOption());
				corpoDomain.setUpdatedValue(corpoReq.getUpdatedValue());
				corpoDomain.setModifiedBy(corpoReq.getUserId());
				corpoDomain.setModifiedDate(currentDate);
				if(corpoDomain.getSelectedOption() == 2) {
					corpoDomain.setUpdatedValue(null);
				}
			}
			try {
				corpoRepository.save(corpoDomain);
			}catch (Exception e) {
				logger.info("Exception in sanving data {}",corpoDomain);
				logger.error("Exception in saving CorporateGovernance / Compliance :{}",e);
				throw new LoansException(e.getMessage());
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see CorporateGovernanceCompianceService#getCorporate(com.opl.msme.api.model.loans.sidbi.CorporateGovernanceComplianceRequest)
	 */
	@Override
	public List<CorporateGovernanceComplianceRequest> getCorporate(CorporateGovernanceComplianceRequest corporateGover) throws Exception {
		List<CorporateGovernanceCompliance> findLast5ByApplicationId = corpoRepository.findFirst5ByApplicationIdAndOrderByCreatedDateDesc(corporateGover.getApplicationId());
		logger.info("==================={}",findLast5ByApplicationId.toString());
		List<CorporateGovernanceComplianceRequest> requestList=new ArrayList<>();
		for (CorporateGovernanceCompliance domain : findLast5ByApplicationId) {
			CorporateGovernanceComplianceRequest request=new CorporateGovernanceComplianceRequest();
			BeanUtils.copyProperties(domain, request);
			request.setValue(com.opl.mudra.api.sidbi.enums.CorporateGovernanceCompliance.getById(domain.getCorporateGovernanceId()).getValue());
			requestList.add(request);
		}
		return requestList;
	}

	 
	

}
