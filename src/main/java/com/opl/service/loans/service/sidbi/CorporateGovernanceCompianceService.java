/**
 * 
 */
package com.opl.service.loans.service.sidbi;

import java.util.List;

import com.opl.mudra.api.loans.model.sidbi.CorporateGovernanceComplianceRequest;

/**
 * @author mohammad.maaz
 *
 */
public interface CorporateGovernanceCompianceService {

	public Boolean saveCorporate(List<CorporateGovernanceComplianceRequest> corporateGover)throws Exception;
	
	public List<CorporateGovernanceComplianceRequest> getCorporate(CorporateGovernanceComplianceRequest corporateGover)throws Exception;
}
