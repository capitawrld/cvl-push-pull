package com.capitaworld.service.loans.service;

import java.util.List;

import com.capitaworld.service.loans.model.FundProviderProposalDetails;
import com.capitaworld.service.loans.model.ProposalResponse;
import com.capitaworld.service.matchengine.model.ProposalCountResponse;
import com.capitaworld.service.matchengine.model.ProposalMappingRequest;
import com.capitaworld.service.matchengine.model.ProposalMappingResponse;

public interface ProposalService {

	public List<?>  fundproviderProposal(ProposalMappingRequest request);
	
	public List<FundProviderProposalDetails>  fundseekerProposal(ProposalMappingRequest request,Long userId);
	
	public ProposalCountResponse fundProviderProposalCount(ProposalMappingRequest request);
	
	public ProposalCountResponse fundSeekerProposalCount(ProposalMappingRequest request);
	
	public ProposalMappingResponse get(ProposalMappingRequest request);
	
	public ProposalMappingResponse changeStatus(ProposalMappingRequest request);
	
	public ProposalMappingResponse sendRequest(ProposalMappingRequest request);
	
	public ProposalMappingResponse listOfFundSeekerProposal(ProposalMappingRequest request);
	
	public ProposalResponse getConectionList(ProposalMappingRequest proposalMappingRequest);
	
	public Integer getPendingProposalCount(Long applicationId);
	
	public List getApprovedFs(ProposalMappingRequest request);
	
}
