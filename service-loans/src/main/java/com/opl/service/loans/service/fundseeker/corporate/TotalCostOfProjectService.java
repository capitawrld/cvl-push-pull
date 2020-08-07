package com.opl.service.loans.service.fundseeker.corporate;

import java.util.List;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.corporate.TotalCostOfProjectRequest;

public interface TotalCostOfProjectService {

	public Boolean saveOrUpdate(FrameRequest  frameRequest) throws LoansException;
	
	
	public List<TotalCostOfProjectRequest> getCostOfProjectDetailList(Long applicationId,Long userId) throws LoansException;

	public List<TotalCostOfProjectRequest> getCostOfProjectDetailListByProposalId(Long proposalId,Long userId) throws Exception;
	public Double getCostOfProject(Long applicationId, Long userId);
	
	
}
