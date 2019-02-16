
package com.capitaworld.service.loans.service.fundseeker.corporate;

import java.io.IOException;

import java.util.List;
import java.util.Map;

import com.capitaworld.service.loans.domain.sanction.LoanSanctionDomain;
import com.capitaworld.service.loans.model.common.*;
import com.capitaworld.service.scoring.model.scoringmodel.ScoringModelReqRes;
import org.json.simple.JSONObject;

import com.capitaworld.service.gateway.model.GatewayRequest;
import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.AdminPanelLoanDetailsResponse;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.LoanApplicationDetailsForSp;
import com.capitaworld.service.loans.model.LoanApplicationRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.PaymentRequest;

import com.capitaworld.service.loans.model.common.CGTMSECalcDataResponse;
import com.capitaworld.service.loans.model.common.ChatDetails;
import com.capitaworld.service.loans.model.common.DisbursementRequest;
import com.capitaworld.service.loans.model.common.EkycRequest;
import com.capitaworld.service.loans.model.common.EkycResponse;
import com.capitaworld.service.loans.model.common.HunterRequestDataResponse;
import com.capitaworld.service.loans.model.common.ProposalList;
import com.capitaworld.service.loans.model.corporate.CorporateProduct;
import com.capitaworld.service.loans.model.mobile.MLoanDetailsResponse;
import com.capitaworld.service.loans.model.mobile.MobileLoanRequest;
import com.capitaworld.service.users.model.FpProfileBasicDetailRequest;
import com.capitaworld.service.users.model.RegisteredUserResponse;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.sidbi.integration.model.ProfileReqRes;

public interface LoanApplicationService {

	public boolean saveOrUpdate(FrameRequest commonRequest, Long userId) throws Exception;
	
	public boolean saveOrUpdateFromLoanEligibilty(FrameRequest commonRequest, Long userId) throws Exception;

	public LoanApplicationRequest get(Long id, Long userId,Long userOrdId) throws Exception;
	
	public Long getIrrByApplicationId(Long id) throws Exception;

	public LoanApplicationRequest inActive(Long id, Long userId) throws Exception;
	
	public int inActiveApplication(Long id, Long userId);

	public List<LoanApplicationRequest> getList(Long userId) throws Exception;

	public List<LoanApplicationDetailsForSp> getLoanDetailsByUserIdList(Long userId);

	public boolean lockPrimary(Long applicationId, Long userId,boolean flag) throws Exception;

	public LoanApplicationRequest lockFinal(Long applicationId, Long userId,boolean flag) throws Exception;

	public LoanApplicationRequest lockFinalByProposalId(Long applicationId,Long proposalId ,Long userId,boolean flag) throws Exception;

	public UserResponse setLastAccessApplication(Long applicationId,Long userId) throws Exception;
	
	public Integer getProductIdByApplicationId(Long applicationId,Long userId) throws Exception;

	public Object[] getApplicationDetailsByProposalId(Long applicationId,Long proposalMappingId) throws Exception;

	public Object[] getApplicationDetailsById(Long applicationId) throws Exception;

	public String getFsApplicantName(Long applicationId) throws Exception;
	
	public void updateFinalCommonInformation(Long applicationId, Long userId, Boolean flag,String finalFilledCount) throws Exception;
	
	public Boolean isProfileAndPrimaryDetailFilled(Long applicationId,Long userId) throws Exception;
	
	public Boolean isPrimaryLocked(Long applicationId, Long userId) throws Exception;

	public Boolean isPrimaryLockedByProposalId(Long proposalId, Long userId) throws Exception;

	public Boolean isApplicationIdActive(Long applicationId) throws Exception; // previous
	public Boolean getByProposalId(Long proposalId) throws Exception; // new 
	
	
	public Boolean isFinalDetailFilled(Long applicationId, Long userId) throws Exception;
	
	public Boolean isFinalLocked(Long applicationId, Long userId) throws Exception;

	public Boolean isFinalLockedByProposalId(Long proposalId, Long userId) throws Exception;

	public JSONObject getSelfViewAndPrimaryLocked(Long applicationId, Long userId) throws Exception;
	
	public Integer getCurrencyId(Long applicationId, Long userId) throws Exception;

	public JSONObject getCurrencyAndDenomination(Long applicationId, Long userId) throws Exception;
	
	public JSONObject isAllowToMoveAhead(Long applicationId, Long userId, Integer nextTabType,Long coAppllicantOrGuarantorId) throws Exception;

	public JSONObject isAllowToMoveAheadForMultiProposal(Long applicationId, Long proposalId, Long userId, Integer nextTabType, Long coAppllicantOrGuarantorId) throws Exception;

	public boolean hasAlreadyApplied(Long userId, Long applicationId,Integer productId);
	
	public JSONObject getBowlCount(Long applicationId, Long userId);

	public JSONObject getBowlCountByProposalId(Long applicationId, Long proposalId);
	
	public List<RegisteredUserResponse> getUsersRegisteredLoanDetails(MobileLoanRequest loanRequest);
	
	public List<AdminPanelLoanDetailsResponse> getLoanDetailsForAdminPanel(Integer type,MobileLoanRequest loanRequest) throws IOException, Exception;

	public List<AdminPanelLoanDetailsResponse> getPostLoginForAdminPanel(MobileLoanRequest loanRequest) throws IOException, Exception;

	public List<AdminPanelLoanDetailsResponse> getPostLoginForAdminPanelOfEligibility(MobileLoanRequest loanRequest) throws IOException, Exception;

	public List<AdminPanelLoanDetailsResponse> getPostLoginForAdminPanelOfNotEligibility(MobileLoanRequest loanRequest) throws IOException, Exception;

	public List<AdminPanelLoanDetailsResponse> getPostLoginForAdminPanelOfFinalLockedRejectedByUbi(MobileLoanRequest loanRequest) throws IOException, Exception;

	public List<AdminPanelLoanDetailsResponse> getPostLoginForAdminPanelOfApprovedByUbi(MobileLoanRequest loanRequest) throws IOException, Exception;

	public List<ChatDetails> getChatListByApplicationId(Long fpMappingId);

	public String getMcaCompanyId(Long applicationId, Long userId);
	
	public List<FpProfileBasicDetailRequest> getFpNegativeList(Long applicationId);
	
	public void saveSuggestionList(ProposalList  proposalList);	
	
	public List<MLoanDetailsResponse> getLoanListForMobile(Long userId);

	public void updateLoanApplication(LoanApplicationRequest loanRequest);
	
	public EkycResponse getDetailsForEkycAuthentication(EkycRequest ekycRequest);

	public Boolean isMca(Long applicationId, Long userId);
	
	public LoanApplicationRequest getLoanBasicDetails(Long id, Long userId);
	
	public Long getTotalUserApplication(Long userId);
	
	public Long getUserIdByApplicationId(Long applicationId); // previous
	
	public Long getApplicationIdByProposalId(Long proposalId); //NEW 
	
	public Long getUserIdByProposalId(Long proposalId); //NEW 
	
	public LoanApplicationRequest saveFromCampaign(Long userId, Long clientId, String campaignCode) throws Exception;
	
	public boolean isCampaignCodeExist(Long userId, Long clientId, String code) throws Exception;
	
	public String getCampaignCodeByApplicationId(Long applicationId) throws Exception;
	
	public Boolean isTermLoanLessThanLimit(Long applicationId);
	
	public Integer getIndustryIrrByApplication(Long applicationId);
	
	public Integer setEligibleLoanAmount(LoanApplicationRequest applicationRequest) throws Exception;
	
	public void updateFlow(Long applicationId,Long clientId,Long userId) throws Exception ;
	
	public Object updateLoanApplicationMaster(PaymentRequest paymentRequest, Long userId) throws Exception;
	
	public void updateSkipPayment(Long userId, Long applicationId, Long orgId,Long fprProductId) throws Exception;
	
	public void updateSkipPaymentWhiteLabel(Long userId, Long applicationId, Integer businessTypeId, Long orgId,Long fprProductId) throws Exception;
	
	public void sendInPrincipleForPersonalLoan(Long userId, Long applicationId, Integer businessTypeId, Long orgId,Long fprProductId) throws Exception;
	
	public LoanApplicationRequest updateLoanApplicationMasterPaymentStatus(PaymentRequest paymentRequest, Long userId)throws Exception;
	
	public GatewayRequest getPaymentStatus(PaymentRequest paymentRequest, Long userId, Long ClientId) throws Exception;
	
	public Long getDDRStatusId(Long applicationId);
	
	public Boolean updateDDRStatus(Long applicationId, Long userId , Long clientId, Long statusId) throws Exception;
	
	public Boolean updateDDRStatusByProposalId(Long applicationId, Long userId , Long proposalId, Long statusId) throws Exception;
	
	public LoanApplicationRequest getFromClient(Long id) throws Exception;

	public Boolean isApplicationEligibleForIrr(Long applicationId) throws Exception;
	
	public DisbursementRequest getDisbursementDetails(DisbursementRequest disbursementRequest);
	
	public Long createMsmeLoan(Long userId,Boolean isActive,Integer businessTypeId);

	public Long createRetailLoan(Long userId, Boolean isActive, Integer businessTypeId);

	public boolean updateProductDetails(LoanApplicationRequest loanApplicationRequest);
	
	public boolean savePhese1DataToSidbi(Long applicationId,Long userId,Long organizationId,Long fpProductMappingId);
	
	public boolean savePhese2DataToSidbi(Long applicationId,Long userId,Long organizationId,Long fpProductMappingId);
	
	public Map<String, Object> getFpDetailsByFpProductId(Long fpProductId) throws Exception;
	
	public CorporateProduct getFpDetailsByFpProductMappingId(Long fpProductId) throws Exception;
	
	public LoanApplicationRequest getLoanApplicationDetails(Long userId, Long applicationId);

	public ScoringModelReqRes getMinMaxMarginByApplicationId(Long applicationId,Integer businessTypeId);

	/**
	 * @param applicationId
	 * @return
	 */
	public CGTMSECalcDataResponse getDataForCGTMSE(Long applicationId) throws Exception;

	public LoanApplicationRequest getProposalDataFromApplicationId(Long applicationId);

	/**
	 * @param applicationId
	 * @return
	 */
	public HunterRequestDataResponse getDataForHunter(Long applicationId) throws Exception;


	public SanctioningDetailResponse getDetailsForSanction(DisbursementRequest disbursementRequest) throws Exception;

	
	public String saveDetailedInfo(ProfileReqRes profileReqRes)  throws LoansException, Exception;
	
	/*//Update Payment Status after redirection through Gateway for Mobile
	
	public Boolean updatePaymentStatusForMobile(PaymentRequest paymentRequest);
*/
	
	public String getMCACompanyIdById(Long applicationId);

	/**
	 * @param applicationId
	 * @return
	 * @throws Exception
	 */
	public HunterRequestDataResponse getDataForHunterForNTB(Long applicationId) throws Exception;
	
	public Boolean saveLoanWCRenewalType(Long applicationId,Integer wcRenewalType);
	public Integer getLoanWCRenewalType(Long applicationId);

	public Long getProposalId(Long applicationId, Long userOrgId);


}


