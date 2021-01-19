package com.opl.service.loans.repository.common;

import java.util.List;

import com.opl.mudra.api.loans.model.TutorialsViewAudits;
import com.opl.service.loans.domain.fundseeker.retail.BankingRelation;

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

	public String getTutorialsById(Long id);

	public boolean saveTutorialsAudits(TutorialsViewAudits longLatrequest);

	public String getTutorialsAudit(TutorialsViewAudits request);

	public String getPrefillProfileStatus(Long fromLoanId,Long toLoanId);
	
	public String getApplicationListForPrefillProfile(Long userId);
	
	public Boolean retailPrefillData(String input);
	
	public String getApplicationCampaignCode(Long applicationId);
	
	public Object[] getApplicationCampaignDetails(Long applicationId);
	
	public Boolean isBankSpecificOn(Long applicationId);
	
	public Boolean isCampaignUser(Long userId);
	
	public String getCampaignUser(Long userId,Long campaignType);
	
	public String getAgriLoanApplicationsByOrgIdAndStatus(Integer orgId,Integer status,Integer fromLimit,Integer toLimit);

	public List<Object[]> getCoLendingRatio(Long fpProductId);
	
	public Object [] getBureauVersionIdById(Long scoringModelId);

	public Object[] getUserDetails(Long userId);

	public List<Object[]> getCoLendingAllRatio(Long applicationId);
	
	public String getScoringMinAndMaxRangeValue(List<Long> scoreModelId,List<Long> fieldMasterId);
	
	public Long getCampaignOrgIdByApplicationId(Long applicationId);
	
	public boolean getCibilBureauAPITrueOrFalse(Long orgId);
	
	public Boolean getIsItrManualFilled(Long applicationId);
	
	public Object[] getBankBureauFlags(Long orgId);
	
	public List<BankingRelation> listBankRelationAppId(Long id);
	
    public List<BankingRelation> listBankRelationAppId(Long id,Long applicantId);
    
    public Double getAllDirectorAverageBureauScore(Long applicationId);
    
    public Boolean isNoBankStatement(Long applicationId);
    
    public Integer getMinRelationshipInMonthByApplicationId(Long applicationId,String bankName);
    
    public Integer getMinRelationshipInMonthByApplicationId(Long applicationId);
    
    public Integer getMinRelationshipInMonthByApplicationIdAndNotGivenBank(Long applicationId,String bankName);
    
    public String getIFSCByApplicationId(Long applicationId);
    
    public String getBankNameByIFSC(String ifscPrefix);
    
    public boolean updateProfileVersIdInConnect(Long applicationId, Long profileVerMapId);
	
	public boolean updateProfileVersIdInLoanMaster(Long applicationId, Long profileVerMapId);

	public Integer getVersionFromOrgId(Long orgId);
	
	public Long getProfileMappingIdByApplicationId(Long applicationId);
	
	public Object[] getProfileVersionDetailsByProfileId(Long profileId);
	
	public Object[] getProfileVersionDetailsByApplicationId(Long applicationId);
	
	public Boolean isManualBs(Long bsId);

	public int checkApplicationStageforMultiBank(Long applicationId);

	public Long getUserTypeByEmail(String email);
}
