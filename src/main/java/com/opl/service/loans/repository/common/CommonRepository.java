package com.opl.service.loans.repository.common;

import java.math.BigInteger;
import java.util.List;


public interface CommonRepository {

	public Object[] getUserCampainCodeByApplicationId(Long applicationId);
	
	public Object[] getEmailDataByApplicationId(Long applicationId);
	
	public String getCoApplicatantNameFromITR(Long coAppId);

	public List<Object[]>  getBranchUserDetailsBasedOnRoleId(Long orgId,Integer roleId);
	
	public Object[] getFpFullName(Long userId); 
	
	public Object getMakerDate(Long applicationId);
	
	public Integer getViewedTeaser(String emailId);
	
	public String getEmailIdFromUsers(Long userId);
	
	public Object[] getEmailIdAndMobileForNBFCUser(Long userId);
	
	public String getNoteForHLCam(Long applicationId);
	
	public Object[] getInEligibleByApplicationId(Long applicationId);
	
	public List<Object[]> getBankDetails(Long applicationId, Long orgId);

	public Object[] getUserDetailsByApplicationId(Long applicationId) throws Exception;
	
	public List<String> getUserDetailsByUserOrgIdAndUserRoleIdAndBranchId(Long orgId ,Long roleId ,Long branchId);
	
	public Object getIsNBFCUser(Long applicationId);
	
	public Object[] fetchALDetailsOfManufacturerAssetsSupplier(Long manufacturerId , Long assetModelId, Integer supplierId) ;
	
	public BigInteger checkApplicationDisbursed(String pan);
	
	//Payment Common Properties
	
	public String getSidbiAmount();
	
	public String getGatewayProvider();
	
	public Object[] getLastCheckerNameByBranchId(Long branchId) throws Exception;
	
	public String getStateByStateCode(Long id);
	
	public Long getCountOfJobId(Long jobId , Long stepId , Long actionId);
	
	public Boolean checkUserForMudraLoanByUserId(Long userId);
	
}