
package com.capitaworld.service.loans.service;

import java.util.List;
import java.util.Map;

import com.capitaworld.service.loans.model.FundProviderProposalDetails;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.ProposalDetailsAdminRequest;
import com.capitaworld.service.loans.model.ProposalResponse;
import com.capitaworld.service.loans.model.common.ProposalSearchResponse;
import com.capitaworld.service.loans.model.common.ReportRequest;
import com.capitaworld.service.matchengine.model.DisbursementDetailsModel;
import com.capitaworld.service.matchengine.model.ProposalCountResponse;
import com.capitaworld.service.matchengine.model.ProposalMappingRequest;
import com.capitaworld.service.matchengine.model.ProposalMappingResponse;
import com.capitaworld.service.users.model.UsersRequest;

public interface ProposalService {

	public List<?>  fundproviderProposal(ProposalMappingRequest request);
	
	public List<?>  basicInfoForSearch(ProposalMappingRequest request);
	
	public List<?> fundproviderProposalByAssignBy(ProposalMappingRequest request);
	
	public List<FundProviderProposalDetails>  fundseekerProposal(ProposalMappingRequest request,Long userId);
	
	public ProposalCountResponse fundProviderProposalCount(ProposalMappingRequest request);
	
	public ProposalCountResponse fundSeekerProposalCount(ProposalMappingRequest request);
	
	public ProposalMappingResponse get(ProposalMappingRequest request);
	
	public ProposalMappingResponse changeStatus(ProposalMappingRequest request);
	
	public ProposalMappingResponse sendRequest(ProposalMappingRequest request);
	
	public ProposalMappingResponse listOfFundSeekerProposal(ProposalMappingRequest request);
	
	public ProposalResponse getConectionList(ProposalMappingRequest proposalMappingRequest);
	
	public Integer getPendingProposalCount(Long applicationId);
	
	public ProposalMappingResponse updateAssignDetails(ProposalMappingRequest request)  throws Exception;

	public ProposalMappingResponse saveDisbursementDetails(DisbursementDetailsModel request, Long userId);

	public LoansResponse checkMinMaxAmount(UsersRequest userRequest);
	
	public List<ProposalDetailsAdminRequest> getProposalsByOrgId(Long userOrgId, ProposalDetailsAdminRequest request, Long userId);
	
	public Object getHomeCounterDetail();
	
	public List<ProposalSearchResponse> searchProposalByAppCode(Long loginUserId,Long loginOrgId,ReportRequest reportRequest);
	
	public Map<String , Double> getFpDashBoardCount(Long loginUserId,Long loginOrgId);
}
