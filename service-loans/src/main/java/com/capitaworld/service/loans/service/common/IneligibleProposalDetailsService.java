package com.capitaworld.service.loans.service.common;

import java.util.List;

import com.capitaworld.service.loans.model.InEligibleProposalDetailsRequest;
import com.capitaworld.service.loans.model.ProposalDetailsAdminRequest;

/**
 * Created by KushalCW on 22-09-2018.
 */
public interface IneligibleProposalDetailsService {

	public Integer save(InEligibleProposalDetailsRequest inEligibleProposalDetailsRequest);

	public Boolean sendMailToFsAndBankBranch(Long applicationId, Long branchId, Long userOrgId);

	public List<ProposalDetailsAdminRequest> getOfflineProposals(Long userOrgId, Long userId,
			ProposalDetailsAdminRequest request);
	
	public boolean updateStatus(InEligibleProposalDetailsRequest inEliProReq);
	
	//added by Jaimin Darji For transfer branch
	public boolean updateTransferBranchDetail(InEligibleProposalDetailsRequest inEliProReq);

}
