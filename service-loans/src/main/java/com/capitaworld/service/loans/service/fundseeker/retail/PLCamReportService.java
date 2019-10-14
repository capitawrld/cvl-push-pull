package com.capitaworld.service.loans.service.fundseeker.retail;

import java.util.Map;

public interface PLCamReportService {

	public Map<String, Object> getCamReportDetailsByProposalId(Long applicationId, Long productId, Long proposalId, boolean isFinalView);
	
	public Map<String, Object> getPLInEligibleCamReport(Long applicationId);

	public byte[] generateIneligibleCamReportFromMap(Long applicationId);
	
	public Map<String , Object> getDataForApplicationForm(Long applicationId, Long productId, Long proposalId);
}
