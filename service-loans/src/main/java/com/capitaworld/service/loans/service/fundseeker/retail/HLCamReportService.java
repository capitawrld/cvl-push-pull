package com.capitaworld.service.loans.service.fundseeker.retail;

import java.util.Map;

public interface HLCamReportService {

	public Map<String, Object> getHLBankStatementAnalysisReport(Long applicationId, Long productId);
	
}
