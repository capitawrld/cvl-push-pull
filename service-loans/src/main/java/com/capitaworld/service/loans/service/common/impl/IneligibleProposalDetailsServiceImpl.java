package com.capitaworld.service.loans.service.common.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.capitaworld.api.payment.gateway.exception.GatewayException;
import com.capitaworld.api.payment.gateway.model.GatewayResponse;
import com.capitaworld.api.reports.ReportRequest;
import com.capitaworld.api.reports.utils.JasperReportEnum;
import com.capitaworld.client.payment.gateway.GatewayClient;
import com.capitaworld.client.reports.ReportsClient;
import com.capitaworld.service.loans.model.*;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.InEligibleProposalCamReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.config.AsyncComponent;
import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.fundseeker.IneligibleProposalDetails;
import com.capitaworld.service.loans.domain.fundseeker.IneligibleProposalTransferHistory;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.capitaworld.service.loans.model.corporate.CorporateApplicantRequest;
import com.capitaworld.service.loans.model.retail.RetailApplicantRequest;
import com.capitaworld.service.loans.repository.common.CommonRepository;
import com.capitaworld.service.loans.repository.common.LoanRepository;
import com.capitaworld.service.loans.repository.fundseeker.IneligibleProposalDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.capitaworld.service.loans.repository.fundseeker.IneligibleProposalTransferHistoryRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PrimaryCorporateDetailRepository;
import com.capitaworld.service.loans.service.common.ApplicationSequenceService;
import com.capitaworld.service.loans.service.common.IneligibleProposalDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.ApplicationProposalMappingService;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.service.fundseeker.corporate.DirectorBackgroundDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.service.fundseeker.retail.HLIneligibleCamReportService;
import com.capitaworld.service.loans.service.fundseeker.retail.PLCamReportService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantService;
import com.capitaworld.service.loans.service.sidbi.SidbiSpecificService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.CommonUtils.InEligibleProposalStatus;
import com.capitaworld.service.loans.utils.CommonUtils.LoanType;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.matchengine.utils.MatchConstant;
import com.capitaworld.service.notification.client.NotificationClient;
import com.capitaworld.service.notification.exceptions.NotificationException;
import com.capitaworld.service.notification.model.ContentAttachment;
import com.capitaworld.service.notification.model.Notification;
import com.capitaworld.service.notification.model.NotificationRequest;
import com.capitaworld.service.notification.utils.ContentType;
import com.capitaworld.service.notification.utils.NotificationAlias;
import com.capitaworld.service.notification.utils.NotificationType;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.PurposeOfLoan;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.BranchBasicDetailsRequest;
import com.capitaworld.service.users.model.BranchUserResponse;
import com.capitaworld.service.users.model.UserOrganisationRequest;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;

import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.DiscoveryStrategy.Reiterating;

/**
 * Created by KushalCW on 22-09-2018.
 */

@Service
@Transactional
public class IneligibleProposalDetailsServiceImpl implements IneligibleProposalDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(IneligibleProposalDetailsServiceImpl.class);

	private static final String BRANCH_NAME_PARAMETERS = "branch_name";
	private static final String BRANCH_CODE_PARAMETERS = "branch_code";
	private static final String BRANCH_ADDRESS_PARAMETERS = "branch_address";
	private static final String BRANCH_CONTACT_PARAMETERS = "branch_contact";
	private static final String IFSC_CODE_PARAMETERS = "ifsc_code";
	private static final int PENDING_STATUS = 1;

	@Autowired
	private IneligibleProposalDetailsRepository ineligibleProposalDetailsRepository;
	@Autowired
	private IneligibleProposalTransferHistoryRepository historyRepository;

	@Autowired
	private UsersClient userClient;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private LoanApplicationService loanApplicationService;

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private NotificationClient notificationClient;


	@Autowired
	DirectorBackgroundDetailsService directorBackgroundDetailsService;

	@Autowired
	private CorporateApplicantService corporateApplicantService;

	@Autowired
	private RetailApplicantService retailApplicantSercive;

	@Autowired
	private PrimaryCorporateDetailRepository primaryCorporateDetailRepository;

	@Autowired
	private ReportsClient reportsClient;

	@Autowired
	private InEligibleProposalCamReportService inEligibleProposalCamReportService;

	@Autowired
	private CommonRepository commonRepository;

	@Autowired
	private AsyncComponent asyncComp;
	
    @Autowired
    private ApplicationProposalMappingRepository applicationRepository;

    @Autowired
    private GatewayClient gatewayClient;
    
    @Autowired
	private Environment environment;

    @Autowired
	private LoanApplicationRepository loanApplicationRepository;
    
    @Autowired
    private HLIneligibleCamReportService hlIneligibleService;
    
    @Autowired
	private PLCamReportService plCamService;

    @Autowired
    private SidbiSpecificService sidbiService;

    @Value("${isSIDBIFlowForIneligible}")
    private Boolean isSIDBIFlowForIneligible;
    
    @Value("${isSBIFlowForIneligible}")
    private Boolean isSBIFlowForIneligible;
    
	private static final String EMAIL_ADDRESS_FROM = "no-reply@capitaworld.com";

	
	@Override
	public Integer save(InEligibleProposalDetailsRequest inlPropReq) {
		try {
			String gstin = loanRepository.getGSTINByAppId(inlPropReq.getApplicationId());

			IneligibleProposalDetails inlProposalDetails = ineligibleProposalDetailsRepository.findByApplicationIdAndIsActive(inlPropReq.getApplicationId(), true);
			boolean isCreateNew = false;
			if(!CommonUtils.isObjectNullOrEmpty(inlProposalDetails)) {
				if(!CommonUtils.isObjectNullOrEmpty(inlProposalDetails.getIsSanctioned()) && inlProposalDetails.getIsSanctioned()) {//HANDLE MESSAGE
					// THIS APPLCATION IS ALREADY SANCTIONED
					return 1;
				}
				//IF ALREADY FOUND DATA WITH THIS APPLICATION ID THEN NEED TO COMPARE BANK ID WITH ALREADY EXISTS DATA
				if(inlProposalDetails.getUserOrgId() != inlPropReq.getUserOrgId()) {
					//IF NOT MATCHED WIH EXSTING BANK DATA THEN CURRENT OBJECT IS INACTIVE AND UPDATE STATUS
					inlProposalDetails.setIsActive(false);
					inlProposalDetails.setModifiedBy(inlPropReq.getUserId());
					inlProposalDetails.setModifiedDate(new Date());
					inlProposalDetails.setStatus(InEligibleProposalStatus.OTHER_BANK);
					ineligibleProposalDetailsRepository.save(inlProposalDetails);
					isCreateNew = true;
				} else if(inlProposalDetails.getBranchId() != inlPropReq.getBranchId()) {
					//IF NOT MATCHED WIH EXSTING BRANCH DATA THEN CURRENT OBJECT IS INACTIVE
					inlProposalDetails.setIsActive(false);
					inlProposalDetails.setModifiedBy(inlPropReq.getUserId());
					inlProposalDetails.setModifiedDate(new Date());
					inlProposalDetails.setStatus(InEligibleProposalStatus.OTHER_BRANCH);
					ineligibleProposalDetailsRepository.save(inlProposalDetails);
					isCreateNew = true;
				}
			} else {
				isCreateNew = true;
			}

			if(!CommonUtils.isObjectNullOrEmpty(gstin)) {
				//UPDARE STATUS FOR SAME GSTIN OLD APPLICATIONS
				List<IneligibleProposalDetails> inlProposalList = ineligibleProposalDetailsRepository.findByGstinPan(gstin.substring(2, 12));
				for(IneligibleProposalDetails inlProposal : inlProposalList) {
					//CHECK IF SAME BANK PROPOSAL AVAILABLE FOR THIS GSTIN
					if(inlProposal.getUserOrgId() == inlPropReq.getUserOrgId()) {
						// NEED TO CHECK IF ALREADY SANCTIONED OR NOT
						if(CommonUtils.isObjectNullOrEmpty(inlProposal.getIsSanctioned()) || !inlProposal.getIsSanctioned()) {
							// CHECK 60 DAY IN-PRINCIPLE VALIDITY
							long dateDiff = daysBetween(new Date(), inlProposal.getCreatedDate());

							String value = loanRepository.getCommonPropertiesValue(com.capitaworld.commons.lib.common.CommonUtils.COMMON_PROPERTIES.CONNECT_MSME_INPRINCIPLE_DATE_RANGE);
							Integer DAY_DIFFERENCE_FOR_INPRINCIPLE = 0;
							if(CommonUtils.isObjectNullOrEmpty(value)) {//IF NULL IN COMMON PROPERTIES THEN DEFAULT VALUE IS 60 DAYS
								DAY_DIFFERENCE_FOR_INPRINCIPLE = 60;
							} else {
								DAY_DIFFERENCE_FOR_INPRINCIPLE = Integer.valueOf(value);
							}
							if (dateDiff < DAY_DIFFERENCE_FOR_INPRINCIPLE) {
								inlProposal.setIsActive(false);
								inlProposal.setModifiedBy(inlPropReq.getUserId());
								inlProposal.setModifiedDate(new Date());
								inlProposal.setStatus(InEligibleProposalStatus.OTHER_BANK);
								ineligibleProposalDetailsRepository.save(inlProposal);
							}
						} else {
							continue;
						}
					}
				}
			}

			if(isCreateNew) {
				inlProposalDetails = new IneligibleProposalDetails();
				inlProposalDetails.setUserOrgId(inlPropReq.getUserOrgId());
				inlProposalDetails.setBranchId(inlPropReq.getBranchId());
				inlProposalDetails.setApplicationId(inlPropReq.getApplicationId());
				inlProposalDetails.setCreatedDate(new Date());
				inlProposalDetails.setCreatedBy(inlPropReq.getUserId());
				inlProposalDetails.setStatus(InEligibleProposalStatus.PENDING);
				inlProposalDetails.setBusinessTypeId(inlPropReq.getBusinessTypeId());
				try {
					//SET GSTIN
					inlProposalDetails.setGstin(loanRepository.getGSTINByAppId(inlPropReq.getApplicationId()));
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				inlProposalDetails.setIsActive(true);
			} else {
				if(!CommonUtils.isObjectNullOrEmpty(inlPropReq.getUserOrgId())) {
					inlProposalDetails.setUserOrgId(inlPropReq.getUserOrgId());
				}
				if(!CommonUtils.isObjectNullOrEmpty(inlPropReq.getBranchId())) {
					inlProposalDetails.setBranchId(inlPropReq.getBranchId());
				}
				if(!CommonUtils.isObjectNullOrEmpty(inlPropReq.getStatus())) {
					inlProposalDetails.setStatus(inlPropReq.getStatus());
				}
				if(!CommonUtils.isObjectNullOrEmpty(inlPropReq.getIsDisbursed())) {
					inlProposalDetails.setIsDisbursed(inlPropReq.getIsDisbursed());
				}
				if(!CommonUtils.isObjectNullOrEmpty(inlPropReq.getIsSanctioned())) {
					inlProposalDetails.setIsSanctioned(inlPropReq.getIsSanctioned());
				}
				if(!CommonUtils.isObjectNullOrEmpty(inlPropReq.getBusinessTypeId())) {
					inlProposalDetails.setBusinessTypeId(inlPropReq.getBusinessTypeId());
				}

				try {
					//SET GSTIN
					if(CommonUtils.isObjectNullOrEmpty(inlProposalDetails.getGstin())) {
						inlProposalDetails.setGstin(loanRepository.getGSTINByAppId(inlPropReq.getApplicationId()));
					}
				} catch (Exception e) {
					logger.error(CommonUtils.EXCEPTION,e);
				}
				inlProposalDetails.setModifiedDate(new Date());
				inlProposalDetails.setModifiedBy(inlPropReq.getUserId());
			}
			// Set Created Date.

			ineligibleProposalDetailsRepository.save(inlProposalDetails);
			return 2;
		} catch (Exception e) {
			logger.error("error while saving in eligible proposal : ",e);
		}
		return 0;
	}

	private static long daysBetween(Date one, Date two) {
		long difference = (one.getTime() - two.getTime()) / 86400000;
		return Math.abs(difference);
	}

	/**
	 * UPDATE REJECTION STATUS
	 * @param inEliProReq
	 * @return
	 */
	@Override
	public boolean updateStatus(InEligibleProposalDetailsRequest inEliProReq) {
		IneligibleProposalDetails ineligibleProposalDetails = null;
		try {
			ineligibleProposalDetails = ineligibleProposalDetailsRepository.findByApplicationIdAndUserOrgIdAndIsActive(inEliProReq.getApplicationId(), inEliProReq.getUserOrgId(), true);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return false;
		}
		if(CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails)) {
			return false;
		}
		ineligibleProposalDetails.setStatus(inEliProReq.getStatus());
		ineligibleProposalDetails.setReason(inEliProReq.getReason());
		ineligibleProposalDetails.setModifiedBy(inEliProReq.getUserId());
		ineligibleProposalDetails.setModifiedDate(new Date());
		ineligibleProposalDetailsRepository.save(ineligibleProposalDetails);
		return true;

	}

	@Override
	public Boolean sendMailToFsAndBankBranch(Long applicationId, Long branchId, Long userOrgId) {
		boolean isSent = false;
		if (applicationId != null && branchId != null && userOrgId != null) {
			try {
				Map<String, Object> notificationParams = new HashMap<>();
				
				// Sending mail to FS who become Ineligible
				// 1 Get Details of FS_NAME,Bank name, Branch name and Address based on application Id
//				ApplicationProposalMapping loanApplication = applicationRepository.getByApplicationIdAndOrgId(applicationId, userOrgId);

				LoanApplicationRequest applicationRequest = null;
				try {
					applicationRequest =loanApplicationService.getBasicInformation(applicationId);  // CHANGES FOR OFFLINE CAM REPORT PURPOSE NEW --->
							//loanApplicationService.getFromClient(loanApplication.getProposalId()); // OLD
				} catch (Exception e1) {
					logger.error("Exception in getting : {}" , e1);
				}
				notificationParams.put("app_code", applicationRequest.getApplicationCode());
				notificationParams.put("productId", applicationRequest.getProductId());
				// For getting Fund Seeker's Name
				if (applicationRequest != null) {
					notificationParams.putAll(getFsNameAndDetailsForAllProduct(applicationId, applicationRequest));
					notificationParams.putAll(getBankAndBranchDetails(userOrgId, branchId, notificationParams));

					UserResponse response = null;
					UsersRequest signUpUser = null;
					try {
						response = userClient.getEmailMobile(applicationRequest.getUserId());
					} catch (Exception e) {
						logger.error("Something went wrong while calling Users client from sending mail to fs===>{}",e);
					}
					if (!CommonUtils.isObjectNullOrEmpty(response)) {
						try {
							signUpUser = MultipleJSONObjectHelper
									.getObjectFromMap((Map<String, Object>) response.getData(), UsersRequest.class);
						} catch (Exception e) {
							logger.error("Exception getting signup user at Sending email to fs and bank branch : ",e);
						}
					}
					// ==================For getting Organisation========Name
					UserResponse userResponse = null;
					Map<String, Object> usersResp = null;
					UserOrganisationRequest organisationRequest = null;
					String organisationName = null;
					try {
						userResponse = userClient.getOrgNameByOrgId(Long.valueOf(userOrgId.toString()));
					} catch (Exception e) {
						logger.error("Exception occured while getting Organisation details by orgId : ",e);
					}
					try {
						if (!CommonUtils.isObjectNullOrEmpty(userResponse)) {
							usersResp = (Map<String, Object>) userResponse.getData();
							organisationRequest = MultipleJSONObjectHelper.getObjectFromMap(usersResp,
									UserOrganisationRequest.class);
							organisationName = organisationRequest.getOrganisationName();
						}
					} catch (Exception e) {
						logger.error("Exception occured while getting Organisation details by orgId : ",e);
					}

					// ===FS=============================================================================
					notificationParams.put("bank_name", organisationName);
 

					 String subject = "Manual Application";
	                    if (organisationName != null && applicationId!=null) {
	                        notificationParams.put(CommonUtils.PARAMETERS_IS_DYNAMIC, false);

						createNotificationForEmail(signUpUser.getEmail(), applicationRequest.getUserId().toString(),
								notificationParams, NotificationAlias.EMAIL_FS_WHEN_IN_ELIGIBLE, subject,applicationId,true,null,null,null);
					}
					// ===========================================================================================
					// 2nd email Step2 Get Details of Bank branch --- Sending mail to Branch
					// Checker/Maker/BO
					// ============================================================================================
					Map<String, Object> mailParameters = new HashMap<String, Object>();
					mailParameters.put("app_id", applicationId !=null ?applicationId : "NA");
					subject = "Manual Application";
					mailParameters.put(CommonUtils.PARAMETERS_FS_NAME,
							notificationParams.get(CommonUtils.PARAMETERS_FS_NAME) != null ? notificationParams.get(CommonUtils.PARAMETERS_FS_NAME) : "NA");
					mailParameters.put("mobile_no", signUpUser.getMobile() != null ? signUpUser.getMobile() : "NA");
					mailParameters.put(CommonUtils.PARAMETERS_ADDRESS,
							notificationParams.get(CommonUtils.PARAMETERS_ADDRESS) != null ? notificationParams.get(CommonUtils.PARAMETERS_ADDRESS) : "NA");
					if (applicationRequest.getBusinessTypeId() == CommonUtils.BusinessType.RETAIL_PERSONAL_LOAN.getId()) {
						//get loan amount and  loan type from loan applicationMaster
//						get loan_amount from retail applicant details
						mailParameters.put(CommonUtils.PARAMETERS_LOAN_TYPE, "Personal Loan");
						mailParameters.put(CommonUtils.PARAMETERS_LOAN_AMOUNT, notificationParams.get(CommonUtils.PARAMETERS_LOAN_AMOUNT));
					}else if (applicationRequest.getBusinessTypeId() == CommonUtils.BusinessType.RETAIL_HOME_LOAN.getId()) {
						mailParameters.put(CommonUtils.PARAMETERS_LOAN_TYPE, "Home Loan");
						mailParameters.put(CommonUtils.PARAMETERS_LOAN_AMOUNT, notificationParams.get(CommonUtils.PARAMETERS_LOAN_AMOUNT));
					} else {
						// Type ==For getting Loan=====For Existing and NTB====================
						PrimaryCorporateDetail primaryCorporateDetail = primaryCorporateDetailRepository
								.findOneByApplicationIdId(applicationId);
						if (!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail)) {
							if (!CommonUtils.isObjectNullOrEmpty(primaryCorporateDetail.getPurposeOfLoanId())) {
								String loanType = PurposeOfLoan.getById(primaryCorporateDetail.getPurposeOfLoanId())
										.getValue();
								if ("Asset Acquisition".equals(loanType)) {
									mailParameters.put(CommonUtils.PARAMETERS_LOAN_TYPE, "Term Loan");
								} else {
									mailParameters.put(CommonUtils.PARAMETERS_LOAN_TYPE, loanType != null ? loanType : "NA");
								}
							} else {
								mailParameters.put(CommonUtils.PARAMETERS_LOAN_TYPE, "NA");
							}
							mailParameters.put(CommonUtils.PARAMETERS_LOAN_AMOUNT,
									primaryCorporateDetail.getLoanAmount() != null
											? String.format("%.0f", primaryCorporateDetail.getLoanAmount()): "NA");
						} else {
							mailParameters.put(CommonUtils.PARAMETERS_LOAN_TYPE, "NA");
							mailParameters.put(CommonUtils.PARAMETERS_LOAN_AMOUNT, "NA");
						}
					}
					// ======send email to maker bo checker===========================
					if(branchId!=null && userOrgId!=null) {
					List<Map<String, Object>> usersRespList = null;
					String to = null;
					userResponse = userClient.getUserDetailByOrgRoleBranchId(userOrgId,
							com.capitaworld.service.users.utils.CommonUtils.UserRoles.FP_MAKER, branchId);
					usersRespList = (List<Map<String, Object>>) userResponse.getListData();
					if (!CommonUtils.isObjectNullOrEmpty(usersRespList)) {
						for (int i = 0; i < usersRespList.size(); i++) {
							UsersRequest userObj = MultipleJSONObjectHelper.getObjectFromMap(usersRespList.get(i),
									UsersRequest.class);
							if (!CommonUtils.isObjectNullOrEmpty(userObj.getEmail())) {
								to = userObj.getEmail();

								 mailParameters.put(CommonUtils.PARAMETERS_IS_DYNAMIC, false);
								String[] bcc=null;
								if(i==0)
								{
									bcc = new String[]{environment.getRequiredProperty("bccforcam")};
								}


								createNotificationForEmail(to, applicationRequest.getUserId().toString(),
										mailParameters, NotificationAlias.EMAIL_BRANCH_FS_WHEN_IN_ELIGIBLE, subject,applicationId,false,null,null,null);
							}
						}

					} else {
						logger.info("No Maker found=================>");
					}
					//add condition on branch id and orgId 
					userResponse = userClient.getUserDetailByOrgRoleBranchId(userOrgId,
							com.capitaworld.service.users.utils.CommonUtils.UserRoles.FP_CHECKER, branchId);
					usersRespList = (List<Map<String, Object>>) userResponse.getListData();
					if (!CommonUtils.isObjectNullOrEmpty(usersRespList)) {
						for (int i = 0; i < usersRespList.size(); i++) {
							UsersRequest userObj = MultipleJSONObjectHelper.getObjectFromMap(usersRespList.get(i),
									UsersRequest.class);
							if (!CommonUtils.isObjectNullOrEmpty(userObj.getEmail())) {
								to = userObj.getEmail();
								mailParameters.put(CommonUtils.PARAMETERS_IS_DYNAMIC, true);
								createNotificationForEmail(to, applicationRequest.getUserId().toString(),
										mailParameters, NotificationAlias.EMAIL_BRANCH_FS_WHEN_IN_ELIGIBLE, subject,applicationId,false,null,null,null);
							}
						}

					} else {
						logger.info("No Checker found=================>");
					}
					userResponse = userClient.getUserDetailByOrgRoleBranchId(userOrgId,
							com.capitaworld.service.users.utils.CommonUtils.UserRoles.BRANCH_OFFICER, branchId);
					usersRespList = (List<Map<String, Object>>) userResponse.getListData();
					if (!CommonUtils.isObjectNullOrEmpty(usersRespList)) {
						for (int i = 0; i < usersRespList.size(); i++) {
							UsersRequest userObj = MultipleJSONObjectHelper.getObjectFromMap(usersRespList.get(i),
									UsersRequest.class);
							if (!CommonUtils.isObjectNullOrEmpty(userObj.getEmail())) {
								to = userObj.getEmail();
								mailParameters.put(CommonUtils.PARAMETERS_IS_DYNAMIC, true);
								createNotificationForEmail(to, applicationRequest.getUserId().toString(),
										mailParameters, NotificationAlias.EMAIL_BRANCH_FS_WHEN_IN_ELIGIBLE, subject,applicationId,false,null,null,null);
							}
						}

					} else {
						logger.info("No BO found=================>");
					}
					}
					
					isSent = true;
				}

			} catch (Exception e) {
				logger.info(
						"Exception in sending email to fs and bank branch when ineligible from IneligibleProposalDetailsServiceImpl : "
								+ e);
				isSent = false;
			}
		} 
		
		return isSent;
	}

	private Map<String, Object> getBankAndBranchDetails(Long userOrgId, Long branchId,
			Map<String, Object> notificationParams) {

		UserResponse userResponse = null;
		String address = null;
		String premiseNo = null;
		String streetName = null;
		String landMark = null;
		String state = null;
		String city = null;
		String pinCode;
		try {
			if (!CommonUtils.isObjectNullOrEmpty(branchId)) {
				userResponse = userClient.getBranchDataBasedOnOrgAndBranchId(userOrgId, branchId);
				List<Map<String, Object>> usersRespList = null;
				if (!CommonUtils.isObjectNullOrEmpty(userResponse))
					usersRespList = (List<Map<String, Object>>) userResponse.getListData();
				if (!CommonUtils.isObjectListNull(usersRespList)) {
					for (int i = 0; i < usersRespList.size(); i++) {
						BranchBasicDetailsRequest resp = MultipleJSONObjectHelper.getObjectFromMap(usersRespList.get(i),
								BranchBasicDetailsRequest.class);
						if (!CommonUtils.isObjectNullOrEmpty(resp)) {
							notificationParams.put(BRANCH_NAME_PARAMETERS, resp.getName() != null ? resp.getName() : "-");
							notificationParams.put(BRANCH_CODE_PARAMETERS, resp.getCode() != null ? resp.getCode() : "-");

							premiseNo = resp.getPremisesNo() != null ? resp.getPremisesNo() : " ";
							streetName = resp.getStreetName() != null ? resp.getStreetName() : " ";
							landMark = resp.getLandMark() != null ? resp.getLandMark() : " ";

							if (!CommonUtils.isObjectNullOrEmpty(resp.getPincode())) {
								pinCode = ", " + Integer.toString(resp.getPincode());
							} else {
								pinCode = ", " + " ";
							}
							if (!CommonUtils.isObjectNullOrEmpty(resp.getStateId())) {
								try {
									state = CommonDocumentUtils.getState(Long.valueOf(resp.getStateId().toString()),
											oneFormClient);
								} catch (Exception e) {
									logger.error("Error while calling One form client for getting State : ",e);
									state = " ";
								}
								state = state != null ? state : " ";
							} else {
								state = " ";
							}
							if (!CommonUtils.isObjectNullOrEmpty(resp.getCityId())) {
								try {
									city = CommonDocumentUtils.getCity(Long.valueOf(resp.getCityId().toString()),
											oneFormClient);
								} catch (Exception e) {
									logger.error("Error while calling One form client for getting City : ",e);
									city = " ";
								}
								city = city != null ? city : " ";
							} else {
								city = " ";
							}
							address = premiseNo + ", " + streetName + ", " + landMark
									+ ", " + state + ", " + city;
							address = address + pinCode;
							notificationParams.put(BRANCH_ADDRESS_PARAMETERS, address != null ? address : "-");
							notificationParams.put(BRANCH_CONTACT_PARAMETERS,
									resp.getContactPersonNumber() != null ? resp.getContactPersonNumber() : "-");
							notificationParams.put("branch_contact_email",resp.getContactPersonEmail()!= null ? resp.getContactPersonEmail() : "-");
							// 16 is for SBI
							if(userOrgId.equals(Long.valueOf(16))) {
								notificationParams.put("smec_code", resp.getSmecCode()!= null?resp.getSmecCode():"NA");
								notificationParams.put("smec_name", resp.getSmecName()!= null?resp.getSmecName():"NA");
								notificationParams.put("smec_mobile", resp.getSmecMobile()!=null?resp.getSmecMobile():"NA");
							}
							
						} else {
							notificationParams.put(BRANCH_NAME_PARAMETERS, "-");
							notificationParams.put(BRANCH_CODE_PARAMETERS, "-");
							notificationParams.put(IFSC_CODE_PARAMETERS, "-");
							notificationParams.put(BRANCH_ADDRESS_PARAMETERS, "-");
							notificationParams.put(BRANCH_CONTACT_PARAMETERS, "-");
						}
					}
				}
			} else {
				notificationParams.put(BRANCH_NAME_PARAMETERS, "-");
				notificationParams.put(BRANCH_CODE_PARAMETERS, "-");
				notificationParams.put(IFSC_CODE_PARAMETERS, "-");
				notificationParams.put(BRANCH_ADDRESS_PARAMETERS, "-");
				notificationParams.put(BRANCH_CONTACT_PARAMETERS, "-");
			}
			return notificationParams;
		} catch (Exception e) {
			logger.error("Error while calling User's client for getting Branch Details : ",e);
			notificationParams.put(BRANCH_NAME_PARAMETERS, "-");
			notificationParams.put(BRANCH_CODE_PARAMETERS, "-");
			notificationParams.put(IFSC_CODE_PARAMETERS, "-");
			notificationParams.put(BRANCH_ADDRESS_PARAMETERS, "-");
			notificationParams.put(BRANCH_CONTACT_PARAMETERS, "-");
			return notificationParams;
		}
	}

	private Map<String, Object> getFsNameAndDetailsForAllProduct(Long applicationId,
			LoanApplicationRequest applicationRequest) {
		Map<String, Object> notificationParams = new HashMap();
		String fsName = null;
		String address = null;
		List<DirectorBackgroundDetailRequest> NTBResponse = null;
		if (applicationRequest.getBusinessTypeId() == CommonUtils.BusinessType.NEW_TO_BUSINESS.getId()) {
			try {
				NTBResponse = directorBackgroundDetailsService.getDirectorBasicDetailsListForNTB(applicationId);
			} catch (Exception e) {
				logger.error("Exception in  geting details of user in ntb: {}" , e);
			}
			if (!CommonUtils.isObjectNullOrEmpty(NTBResponse)) {
				int isMainDirector = 0;
				for (DirectorBackgroundDetailRequest director : NTBResponse) {
					if (!CommonUtils.isObjectNullOrEmpty(director) && director.getIsMainDirector()) {
						fsName = director.getDirectorsName() != null ? director.getDirectorsName() : "NA";
						notificationParams.put(CommonUtils.PARAMETERS_FS_NAME, fsName);
						notificationParams.put(CommonUtils.PARAMETERS_ADDRESS, director.getAddress() != null ? director.getAddress() : "NA");
						isMainDirector = 1;
					}
				}
				if (isMainDirector == 0) {
					fsName = NTBResponse == null ? "NA" : NTBResponse.get(0).getDirectorsName() != null ? NTBResponse.get(0).getDirectorsName()
							: "NA";
					notificationParams.put(CommonUtils.PARAMETERS_FS_NAME, fsName != null ? fsName : "NA");
					notificationParams.put(CommonUtils.PARAMETERS_ADDRESS,
							NTBResponse == null ? "NA" : NTBResponse.get(0).getAddress() != null ? NTBResponse.get(0).getAddress() : "NA");
				}
			} else {
				notificationParams.put(CommonUtils.PARAMETERS_FS_NAME, fsName != null ? fsName : "NA");
				notificationParams.put(CommonUtils.PARAMETERS_ADDRESS, "NA");
			}
			return notificationParams;
		} else if (applicationRequest.getBusinessTypeId() == CommonUtils.BusinessType.RETAIL_PERSONAL_LOAN.getId() || 
				applicationRequest.getBusinessTypeId() == CommonUtils.BusinessType.RETAIL_HOME_LOAN.getId()) {
			try {
				// for fs name and address only
				RetailApplicantRequest plRequest = retailApplicantSercive.get(applicationId);
				notificationParams.put(CommonUtils.PARAMETERS_LOAN_AMOUNT, plRequest.getLoanAmountRequired()!=null?plRequest.getLoanAmountRequired():" - ");
				
				if (plRequest != null) {
					notificationParams.put(CommonUtils.PARAMETERS_FS_NAME, plRequest.getFirstName());
					address = asyncComp.murgedAddress(plRequest.getAddressPremiseName(), plRequest.getAddressLandmark(), plRequest.getAddressStreetName(), plRequest.getAddressCity(), null, plRequest.getAddressState());
					
					/*String primiseName = plRequest.getAddressPremiseName() != "" ? plRequest.getAddressPremiseName()
							: "";
					String streetName = plRequest.getAddressStreetName() != "" ? plRequest.getAddressStreetName() : "";
					String landMark = plRequest.getAddressLandmark() != "" ? plRequest.getAddressLandmark() : "";
					address = null;
					if (primiseName != "" && primiseName != null){
						address = primiseName;
					}

					if (streetName != "" && streetName != null)
						if(address != null){
							address = address + "," + streetName;
						}else{
							address = streetName;
						}

					if (landMark != "" && landMark != null){
						if(address != null){
							address = address + "," + landMark;
						}else{
							address = landMark;
						}
					}

					String city = "";
					try {
						city = CommonDocumentUtils.getCity(Long.valueOf(plRequest.getAddressCity().toString()),
								oneFormClient);
						if (city != ""){
							if(address != null){
								address = address + "," + city;
							}else{
								address = city;
							}
						}

					} catch (Exception e) {
						logger.error("Error in getting city from city id" + e);
					}
					String state = "";
					try {
						state = CommonDocumentUtils.getState(Long.valueOf(plRequest.getAddressState().toString()),
								oneFormClient);
						if (state != "")
							address = address + "," + state;
					} catch (Exception e) {
						logger.error("Error in getting state from state id" + e);
					}
					logger.info("address is:" + address);*/

					notificationParams.put(CommonUtils.PARAMETERS_ADDRESS, address);
				}
			} catch (Exception e) {
				logger.error("Exception in Getting Fund seeker details for PL ineligible proposal details: {}" , e);
			}
			return notificationParams;
		} else {
			fsName = applicationRequest.getUserName() != null ? applicationRequest.getUserName() : "NA";
			notificationParams.put(CommonUtils.PARAMETERS_FS_NAME, fsName);
			if (applicationRequest.getBusinessTypeId() == CommonUtils.BusinessType.EXISTING_BUSINESS.getId()) {
				CorporateApplicantRequest applicantRequest = corporateApplicantService
						.getCorporateApplicant(applicationId);
				if (!CommonUtils.isObjectNullOrEmpty(applicantRequest)
						&& !CommonUtils.isObjectNullOrEmpty(applicantRequest.getFirstAddress())) {

					String premiseNumber = null;
					String streetName = null;
					String landMark = null;
					premiseNumber = applicantRequest.getFirstAddress().getPremiseNumber() != null
							? applicantRequest.getFirstAddress().getPremiseNumber()
							: "";
					streetName = applicantRequest.getFirstAddress().getStreetName() != null
							? applicantRequest.getFirstAddress().getStreetName()
							: "";
					landMark = applicantRequest.getFirstAddress().getLandMark() != null
							? applicantRequest.getFirstAddress().getLandMark()
							: "";
					address = premiseNumber + " " + streetName + " " + landMark;

					notificationParams.put(CommonUtils.PARAMETERS_ADDRESS, address != null ? address : "NA");
				}
			}
			return notificationParams;
		}
	}

	private void createNotificationForEmail(String toNo, String userId, Map<String, Object> mailParameters,
			Long templateId, String emailSubject,Long applicationId,Boolean isFundSeeker,String[] bcc,String[] cc,List<ContentAttachment> content) throws NotificationException {
		logger.info("Inside send notification===>{}" , toNo);
		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setClientRefId(userId);
		
		try{
			notificationRequest.setIsDynamic(((Boolean) mailParameters.get(CommonUtils.PARAMETERS_IS_DYNAMIC)).booleanValue());
		}catch (Exception e) {
			notificationRequest.setIsDynamic(false);
		}
		
		String[] to = { toNo };
		Notification notification = new Notification();
		notification.setContentType(ContentType.TEMPLATE);
		notification.setTemplateId(templateId);
		notification.setSubject(emailSubject);
		notification.setTo(to);
		notification.setType(NotificationType.EMAIL);
		notification.setFrom(EMAIL_ADDRESS_FROM);
		notification.setParameters(mailParameters);
		notification.setIsDynamic(notificationRequest.getIsDynamic());
		notification.setCc(cc);
		if(!CommonUtils.isObjectNullOrEmpty(bcc))
		{
			notification.setBcc(bcc);
		}

		// start attach CAM to Mail
		try {
			if(!isFundSeeker)
			{
				Integer productId = Integer.valueOf(mailParameters.get("productId").toString());
				byte[] camArr = getCamForNotification(applicationId, productId);
				
				notification.setFileName("CAM.pdf");
				notification.setContentInBytes(camArr);
				ContentAttachment contentAttach = new ContentAttachment();
				contentAttach.setFileName("CAM.pdf");
				content.add(contentAttach);
			}
		}catch (Exception e) {
			logger.error("Exception in getting cam for ineligible");
		}
		// end attach CAM to Mail
		if(content != null && !content.isEmpty()) {
			notification.setContentAttachments(content);
		}
		notificationRequest.addNotification(notification);
		sendEmail(notificationRequest);
		logger.info("Outside send notification===>{}" , toNo);
	}

	private byte[] getCamForNotification(Long applicationId, Integer productId) {
		ReportRequest reportRequest = new ReportRequest();
		Map<String,Object> response = new HashMap<>();
		
		if(productId == LoanType.WORKING_CAPITAL.getValue() || productId == LoanType.WCTL_LOAN.getValue() || productId == LoanType.TERM_LOAN.getValue()) {
			response = inEligibleProposalCamReportService.getInEligibleCamReport(applicationId);
			reportRequest.setParams(response);
			reportRequest.setTemplate("INELIGIBLECAMREPORT");
			reportRequest.setType("INELIGIBLECAMREPORT");
			try
			{
				return reportsClient.generatePDFFile(reportRequest);
			}
			catch (Exception e)
			{
				logger.error("error while attaching cam report : {}",e);
				return null;
			}
		}else if(productId == LoanType.PERSONAL_LOAN.getValue()) {
			try
			{
				return hlIneligibleService.generateIneligibleCamReportFromMap(applicationId);
			}
			catch (Exception e)
			{
				logger.error("error while attaching cam report : ",e);
				return null;
			}
		}else if(productId == LoanType.HOME_LOAN.getValue()) {
			try {
				return plCamService.generateIneligibleCamReportFromMap(applicationId);
			}catch (Exception e) {
				logger.error("error while attaching cam report : {}",e);
				return null;
			}
		}else {
			return null;
		}
	}

	private void sendEmail(NotificationRequest notificationRequest) throws NotificationException {
		notificationClient.send(notificationRequest);
	}

	@Override
	public List<ProposalDetailsAdminRequest> getOfflineProposals(Long userOrgId, Long userId,
			ProposalDetailsAdminRequest request) {

		List<Object[]> result;

		result = ineligibleProposalDetailsRepository.getOfflineProposalDetailsByOrgId(userOrgId, request.getFromDate(),
				request.getToDate());

		List<ProposalDetailsAdminRequest> responseList = new ArrayList<>(result.size());

		for (Object[] obj : result) {
			ProposalDetailsAdminRequest proposal = new ProposalDetailsAdminRequest();
			proposal.setApplicationId(CommonUtils.convertLong(obj[0]));
			proposal.setUserId(CommonUtils.convertLong(obj[1]));
			proposal.setUserName(CommonUtils.convertString(obj[2]));
			proposal.setEmail(CommonUtils.convertString(obj[3]));
			proposal.setMobile(CommonUtils.convertString(obj[4]));
			proposal.setCreatedDate(CommonUtils.convertDate(obj[5]));
			proposal.setBranchId(CommonUtils.convertLong(obj[6]));
			proposal.setBranchName(CommonUtils.convertString(obj[7]));
			proposal.setContactPersonName(CommonUtils.convertString(obj[8]));
			proposal.setTelephoneNo(CommonUtils.convertString(obj[9]));
			proposal.setContactPersonNumber(CommonUtils.convertString(obj[10]));
			proposal.setOrganizationName(CommonUtils.convertString(obj[11]));
			proposal.setApplicationCode(CommonUtils.convertString(obj[12]));
			proposal.setCode(CommonUtils.convertString(obj[13]));
			proposal.setStreetName(CommonUtils.convertString(obj[14]));
			proposal.setState(CommonUtils.convertString(obj[15]));
			proposal.setCity(CommonUtils.convertString(obj[16]));
			proposal.setPremisesNo(CommonUtils.convertString(obj[17]));
			proposal.setContactPersonEmail(CommonUtils.convertString(obj[18]));

			responseList.add(proposal);
		}

		return responseList;
	}
	
	
	@Override
	public Boolean checkIsExistOfflineProposalByApplicationId(Long applicationId) {
		return !CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetailsRepository.findByApplicationIdAndIsActive(applicationId, Boolean.TRUE));
	}

	@Override
	public boolean updateTransferBranchDetail(InEligibleProposalDetailsRequest inEliProReq) {
		try{
			//find entity by Id and update branch transfer details
			IneligibleProposalDetails proposalDetails = null;
			try {
				proposalDetails = ineligibleProposalDetailsRepository.findOne(inEliProReq.getIneligibleProposalId());
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
				return false;
			}
			if(CommonUtils.isObjectNullOrEmpty(proposalDetails)) {
				return false;
			}
			Long branchId = proposalDetails.getBranchId();
			proposalDetails.setBranchId(inEliProReq.getBranchId());
			proposalDetails.setModifiedBy(inEliProReq.getUserId());
			proposalDetails.setModifiedDate(new Date());
			ineligibleProposalDetailsRepository.save(proposalDetails);
			// save updated branch history in Transfer history table
			IneligibleProposalTransferHistory proposalTransferHistory = new IneligibleProposalTransferHistory();
			proposalTransferHistory.setIneligibleProposalid(proposalDetails.getId());
			proposalTransferHistory.setNewBranchId(inEliProReq.getBranchId());
			proposalTransferHistory.setOldBranchId(branchId);
			proposalTransferHistory.setReason(inEliProReq.getReason());
			proposalTransferHistory.setCreatedBy(inEliProReq.getUserId());
			proposalTransferHistory.setCreatedDate(new Date());
			proposalTransferHistory.setApplicationId(proposalDetails.getApplicationId());
			historyRepository.save(proposalTransferHistory);
			return true;
		} catch (Exception e) {
			logger.error("error while update ineligible proposal : ",e);
		}
			return false;
	}

	@Override
	public boolean updateReOpenProposalDetail(InEligibleProposalDetailsRequest inEliProReq) {
			//find entity by Id for update details of reopen status
			IneligibleProposalDetails proposalDetails = null;
			try {
				proposalDetails = ineligibleProposalDetailsRepository.findOne(inEliProReq.getIneligibleProposalId());
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
				return false;
			}
			if(CommonUtils.isObjectNullOrEmpty(proposalDetails)) {
				return false;
			}
			//reopen should be changed as pending status
			proposalDetails.setStatus(PENDING_STATUS);
			proposalDetails.setReopenReason(inEliProReq.getReOpenReason());
			proposalDetails.setModifiedBy(inEliProReq.getUserId());
			proposalDetails.setModifiedDate(new Date());
			ineligibleProposalDetailsRepository.save(proposalDetails);
			return true;
	}

	@Override
	public Boolean sendMailToFsAndBankBranchForSbiBankSpecific(Long applicationId,Long branchId,Long userOrgId) {
		Boolean status=false;
		Object[] user = {}; 
		try {
			user = commonRepository.getUserCampainCodeByApplicationId(applicationId);
		}catch (Exception e) {
			return status;
		}
		if(user!=null) {
			if((user[0].equals("sbi") && !CommonUtils.isObjectListNull(user[1]) && Integer.valueOf(user[1].toString()).equals(2)  && isSBIFlowForIneligible != null && isSBIFlowForIneligible) || (user[0].equals("sidbi") && isSIDBIFlowForIneligible != null && isSIDBIFlowForIneligible &&  Integer.valueOf(user[1].toString()).equals(1))) {
				String[] bcc = environment.getProperty("com.ineligible.email.bcc").split(",");
				Object[] emailData = commonRepository.getEmailDataByApplicationId(applicationId);
				if(emailData!=null) {
					Integer buisnessTypeId= Integer.valueOf(String.valueOf(emailData[14]));
					if(buisnessTypeId == CommonUtils.BusinessType.EXISTING_BUSINESS.getId()) {
						String fsEmail = String.valueOf(emailData[0]);
						String fsMobile = String.valueOf(emailData[1]);
						String fsPrimiseNo = emailData[3]!=null?String.valueOf(emailData[3]):"";
						String fsStreetName= emailData[4]!=null?String.valueOf(emailData[4]):"";
						String fsLandMark= emailData[5]!=null?String.valueOf(emailData[5]):"";
						Long fsCityId= Long.valueOf(String.valueOf(emailData[6]));
						Long fsStateId= Long.valueOf(String.valueOf(emailData[7]));
						String fsPincode= emailData[8]!=null?String.valueOf(emailData[8]):"";
						String fsName= String.valueOf(emailData[11]);
						Long userId= Long.valueOf(String.valueOf(emailData[12]));
						Integer proposOfLoanAmount= Integer.valueOf(String.valueOf(emailData[13]));
						String address ="";
						try {
							address = asyncComp.murgedAddress(fsPrimiseNo, fsLandMark, fsStreetName, fsCityId, Long.valueOf(fsPincode), fsStateId);
						} catch (Exception e) {
							logger.error("Exception in murging address",e);
						}
						Map<String, Object> param =new HashMap<>();
						try {
							param = getBankAndBranchDetails(userOrgId, branchId, param);
						}catch (Exception e) {
							logger.error("Exception while getting bank and bank and branch details :",e);
						}
						param.put("fs_name", fsName);
						param.put("noCode", true);
						param.put("address", address);
						if(user[0].equals("sbi")) {
							param.put("isSBI", "true");
						}
						
						String loanType="";
						if(proposOfLoanAmount == 1) {
							loanType = "Term Loan";
						}else if(proposOfLoanAmount == 2){
							loanType = "Working Capital";
						}
						param.put("loan_type", loanType);
						String bankLogo = "";
						try {
							 bankLogo = gatewayClient.getBankLogoUrlByOrgId(userOrgId);
							param.put("bank_url", bankLogo);
						} catch (GatewayException e) {
							logger.error("Exception while getting bank logo url for ineligible email",e); 
						}
						param.put("mobile_no", fsMobile);
						
						try {
							UserResponse orgName = userClient.getOrgNameByOrgId(userOrgId);
							UserOrganisationRequest request= MultipleJSONObjectHelper.getObjectFromMap((Map) orgName.getData(),UserOrganisationRequest.class);
							param.put("org_name", request.getOrganisationName());
						} catch (IOException e2) {
							logger.error("Exception in getting user organisation name",e2);
							param.put("org_name", "Bank");
						}
						if(fsEmail != null && fsMobile!=null && applicationId != null && !bankLogo.isEmpty()) {
							String subject="PSBLOANSIN59MINUTES | Thankyou For Completing Your Online Journey";
							String[] cc = {String.valueOf(param.get("branch_contact_email"))};
							List<ContentAttachment> documentList=new ArrayList<ContentAttachment>();
							if(user[0].equals("sidbi") && isSIDBIFlowForIneligible && Integer.valueOf(user[1].toString()).equals(1)) {
								try {
									DecimalFormat decim = new DecimalFormat("####");
									Double loanAmount = sidbiService.getLoanAmountByApplicationId(applicationId);
									param.put("loanAmount", decim.format(loanAmount));
									ReportRequest request=new ReportRequest();
									List<Map<String, Object>> dataList=new ArrayList<Map<String,Object>>();
									dataList.add(param);
									request.setTemplate(JasperReportEnum.SIDBI_SPECIFIC_DOCUMENT.getName());
									request.setIsStaticContent(false);
									request.setData(dataList);
									request.setParams(new HashMap<>());
									request.setDocumentName("SdibiDocumentList");
									documentList.add(new ContentAttachment("SidbiDocumentList.pdf", reportsClient.getReport(request)));
								}catch (Exception e) {
									logger.info("Error/Exception while getting document list for sidbi specific ==>{} ...Error==>{}",applicationId,e);
								}
							}
							
							try {
								createNotificationForEmail(fsEmail, String.valueOf(userId), param, NotificationAlias.EMAIL_OF_THANKYOU_BANKSPECIFIC_FS, subject, applicationId, true, bcc,cc,documentList);
							} catch (NotificationException e) {
								logger.error("Exception in sending thankyou email for ineligible prooposal:",e);
							}
						}
						/*Mail to branch*/
						UserResponse allBranchUsers = userClient.getAllBranchUsers(branchId);
						if(!allBranchUsers.getListData().isEmpty()) {
							List<Map<String,Object>> listData =(List<Map<String,Object>>) allBranchUsers.getListData();
							for (int i = 0; i < listData.size(); i++) {
								BranchUserResponse resp = new BranchUserResponse();
								try {
									resp = MultipleJSONObjectHelper.getObjectFromMap(listData.get(i), BranchUserResponse.class);
								} catch (IOException e1) {
									logger.error("Exception in getting branch user",e1);
								}
								if(resp.getUserRole().equals("9") || resp.getUserRole().equals("5") || resp.getUserRole().equals("6")) {
									param.put("bo_name", resp.getUserName()!=null?resp.getUserName():"Sir/Madam");
									String subject = "Intimation of new proposal";
									try {
										createNotificationForEmail(resp.getEmail(), String.valueOf(userId), param, NotificationAlias.EMAIL_OF_THANKYOU_BANKSPECIFIC_FP, subject, applicationId, false, bcc,null,null);
									} catch (NotificationException e) {
										logger.error("Exception in sending thankyou email for ineligible prooposal:",e);
									} 
								}
							}
						}
						status=true;
					}
				}
			}
		}else {
			logger.info("User is not from SBI bank specific and WC_renewal");
		}
		return status;
	}

	public Integer getBusinessTypeIdFromApplicationId(Long applicationId){
		return loanApplicationRepository.findOneBusinessTypeIdByIdAndIsActive(applicationId);
	}
}
