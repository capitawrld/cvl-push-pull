package com.capitaworld.service.loans.repository.common;

import com.capitaworld.service.loans.model.TutorialsViewAudits;

import java.util.List;

public interface LoanRepository{

	public Object[] getRoleIdAndBranchIdByUserId(Long userId);
	
	public String getMobileNumberByUserId(Long userId);	

	public List<Object[]> searchProposalForHO(Long orgId,String searchString,Long listLimit,Long businessTypeId);

	public List<Object[]> searchProposalForCheckerAndMaker(Long orgId,String searchString,Long branchId,Long listLimit,Long businessTypeId);

	public List<Object[]> searchProposalForSMECC(Long orgId,String searchString,Long userId,Long listLimit,Long businessTypeId);

	public List<Object[]> getSerachProposalListByRoleSP(Long orgId,String searchString,Long userId,Long listLimit,Long businessTypeId,Long branchId);

	public Object[] fpDashBoardCountByOrgId(Long orgId,Long businessTypeId);

	public Object[] fpDashBoardCountByOrgIdAndBranchId(Long orgId,Long branchId,Long businessTypeId);

	public Object[] fpDashBoardCountByOrgIdAndUserId(Long orgId,Long userId,Long businessTypeId);

	public Object[] fetchFpDashbordCountByRoleSP(Long orgId,Long userId,Long businessTypeId,Long branchId);

	public String getGSTINByAppId(Long applicationId);

	public String getCommonPropertiesValue(String key);
	
	public Long getOfflineCountByAppId(Long applicationId);
	
	public Double getRetailLoanAmountByApplicationId(Long applicationId);
	
	public Boolean isITRUploaded(Long applicationId);
	
	public Boolean isITRUploadedForCoApp(Long applicationId,Long coAppId);
	
	public Boolean isITRSkippedForCoApp(Long applicationId, Long coAppId);
	
	public Boolean isITRMannualForCoApp(Long applicationId, Long coAppId);
	
	public String getOfflineDetailsByAppId(Long applicationId);
	
	public String getOfflineStatusByAppId(Long applicationId);
	
	public List<Double> getIncomeOfItrOf3Years(Long applicationId);

	public List<Double> getIncomeOfItrOf3YearsOfCoApplicant(Long coAppId);

	//1/6/2019..............
	public List<Object[]>getTypeSelectionData();
	
	public List<Object[]>getTypeSelectionData(String userId);
	
	public String checkPanForAlreayInPrinciplOrNotEligible(Integer typeId,Integer selectedLoanTypeId,Long applicationId,String panNumber);

	public String getTutorialsByRoleId(Long userRoleId, Integer loanType);

	public boolean saveTutorialsAudits(TutorialsViewAudits longLatrequest);

	public String getTutorialsAudit(TutorialsViewAudits request);

	public String getPrefillProfileStatus(Long fromLoanId,Long toLoanId);
	
	public String getApplicationListForPrefillProfile(Long userId);
	
	public Boolean retailPrefillData(String input);
	
	public String getApplicationCampaignCode(Long applicationId);
	
	public Boolean isCampaignUser(Long userId);
	
	public String getCampaignUser(Long userId,Long campaignType);
	
	public String getAgriLoanApplicationsByOrgIdAndStatus(Integer orgId,Integer status,Integer fromLimit,Integer toLimit);

	public List<Object[]> getCoLendingRatio(Long fpProductId);
}
