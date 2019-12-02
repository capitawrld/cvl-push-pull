package com.capitaworld.service.loans.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.capitaworld.service.loans.domain.fundseeker.IneligibleProposalDetails;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.LoanApplicationRequest;
import com.capitaworld.service.loans.model.PaymentRequest;
import com.capitaworld.service.loans.model.corporate.CorporateApplicantRequest;
import com.capitaworld.service.loans.model.retail.RetailApplicantRequest;
import com.capitaworld.service.loans.repository.common.CommonRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.capitaworld.service.loans.service.fundprovider.ProductMasterService;
import com.capitaworld.service.loans.service.fundseeker.corporate.CorporateApplicantService;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.service.fundseeker.retail.RetailApplicantService;
import com.capitaworld.service.loans.utils.CommonNotificationUtils.NotificationTemplate;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.CommonUtils.LoanType;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.matchengine.MatchEngineClient;
import com.capitaworld.service.matchengine.ProposalDetailsClient;
import com.capitaworld.service.matchengine.model.ConnectionResponse;
import com.capitaworld.service.matchengine.model.MatchDisplayResponse;
import com.capitaworld.service.matchengine.model.MatchRequest;
import com.capitaworld.service.matchengine.model.ProposalCountResponse;
import com.capitaworld.service.matchengine.model.ProposalMappingRequest;
import com.capitaworld.service.matchengine.model.ProposalMappingResponse;
import com.capitaworld.service.mca.client.McaClient;
import com.capitaworld.service.mca.exception.McaException;
import com.capitaworld.service.mca.model.verifyApi.VerifyAPIRequest;
import com.capitaworld.service.notification.client.NotificationClient;
import com.capitaworld.service.notification.exceptions.NotificationException;
import com.capitaworld.service.notification.model.Notification;
import com.capitaworld.service.notification.model.NotificationRequest;
import com.capitaworld.service.notification.utils.ContentType;
import com.capitaworld.service.notification.utils.EmailSubjectAlias;
import com.capitaworld.service.notification.utils.NotificationAlias;
import com.capitaworld.service.notification.utils.NotificationConstants;
import com.capitaworld.service.notification.utils.NotificationConstants.NotificationProperty.DomainValue;
import com.capitaworld.service.notification.utils.NotificationType;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;
import com.ibm.icu.text.SimpleDateFormat;

@Component
public class AsyncComponent {
	private static final Logger logger = LoggerFactory.getLogger(AsyncComponent.class.getName());

	@Autowired
	private Environment environment;

	@Autowired
	private UsersClient usersClient;

	@Autowired
	private LoanApplicationService loanApplicationService;

	@Autowired
	private NotificationClient notificationClient;

	@Autowired
	private MatchEngineClient matchEngineClient;

	@Autowired
	private ProposalDetailsClient proposalDetailsClient;

	@Autowired
	private ProductMasterService productMasterService;

	@Autowired
	private CorporateApplicantService corporateApplicantService;

	@Autowired
	private RetailApplicantService retailApplicantService;

	@Autowired
	private OneFormClient oneFormClient;
	
	@Autowired 
	private McaClient mcaClient;
	
	@Autowired
	private LoanApplicationRepository loanApplicationRepository;
	
    @Autowired
    private CommonRepository commonRepo;
    
    @Autowired
    private RetailApplicantDetailRepository retailApplicantDetailRepository;

	private static final String EMAIL_ADDRESS_FROM = "com.capitaworld.mail.url";
	private static final String PARAMETERS_TOTAL_MATCHES = "total_matches";

	private static final String THROW_EXCEPTION_WHILE_SENDING_MAIL_PRIMARY_COMPLETE = "Throw exception while sending mail, Primary Complete : ";
	private static final String ERROR_WHILE_GET_FUND_PROVIDER_NAME = "Error while get fund provider name : ";
	
	private static final String HOLD_REJECT_REASON_UNABLE_TO_CONTACT_THE_CLIENT = "Unable to Contact the Client";
	private static final String ISDYNAMIC ="isDynamic";

	/**
	 * FS Mail Number :- 4 Send Mail when Fund seeker login first time in our system
	 * and logout without selecting any application
	 * 
	 * @param userId :- FS Login UserId This Method Called From
	 *               LoanApplicationController
	 */
	@SuppressWarnings("unchecked")
	@Async
	public void sendMailWhenUserHasNoApplication(Long userId) {
		logger.info("Enter in sending mail when user has no application");
		try {
			Long totalApplication = loanApplicationService.getTotalUserApplication(userId);
			if (totalApplication > 0) {
				if (totalApplication == 1) {
					logger.info("Call method for sent mail if profile details filled or not ====>" + totalApplication);
					/*sentMailWhenUserLogoutWithoutFillingFirstProfileOrPrimaryData(userId);*/
					return;
				} else {
					logger.info("Exits,User has more then one application ====>" + totalApplication);
					return;
				}
			}
			Long domainId = DomainValue.MSME.getId();
			logger.info("Call user client for get email and name by user id");
			UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				UsersRequest request = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(request)) {
					Map<String, Object> parameters = new HashMap<String, Object>();
					parameters.put(CommonUtils.PARAMETERS_FS_NAME, request.getName());
					String[] toIds = { request.getEmail() };
					sendNotification(toIds, userId.toString(), parameters, NotificationTemplate.LOGOUT_IMMEDIATELY,
							null, false, null,domainId);
					logger.info(
							"Exits, Successfully sent mail when user has no application ---->" + request.getEmail());
					sendRemainderMailWhenUserHasNoApplication(userId, parameters, toIds,domainId);
				}
			} else {
				logger.info("User response null while getting email id and user type");
			}
		} catch (Exception e) {
			logger.error("Throw exception while sending mail, logout immediately : ",e);
		}
	}

	/**
	 * FS Mail Number :- 6 When user logout without filling first profile or primary
	 * details
	 * 
	 * @param userId :- FS Login UserId This Method Called From
	 *               sendMailWhenUserHasNoApplication method
	 */
	@Async
	public void sentMailWhenUserLogoutWithoutFillingFirstProfileOrPrimaryData(Long userId) {
		logger.info(
				"Start sent mail process for user logout withour filled first application profile or primary details");
		try {
			List<LoanApplicationRequest> loanApplicationRequestList = loanApplicationService.getList(userId);
			if (loanApplicationRequestList.size() > 1 || loanApplicationRequestList.isEmpty()) {
				logger.info("User has more one application or not application list========>"
						+ loanApplicationRequestList.size());
				return;
			}
			NotificationTemplate template = NotificationTemplate.LOGOUT_WITHOUT_FILLED_PROFILE_DETAILS;
			LoanApplicationRequest loanApplicationRequest = loanApplicationRequestList.get(0);
			Long domainId = DomainValue.MSME.getId();
			if(loanApplicationRequest.getLoanTypeMain().equals(CommonUtils.RETAIL)) {
				domainId = DomainValue.RETAIL.getId();
			}
			if (!CommonUtils.isObjectNullOrEmpty(loanApplicationRequest)) {

				if (!CommonUtils.isObjectNullOrEmpty(loanApplicationRequest.getIsApplicantDetailsFilled())) {
					if (loanApplicationRequest.getIsApplicantDetailsFilled()) {// CHECK USER HAS FILLED PROFILE DETAILS
						if (!CommonUtils.isObjectNullOrEmpty(loanApplicationRequest.getIsApplicantPrimaryFilled())) {
							if (loanApplicationRequest.getIsApplicantPrimaryFilled()) {// CHECK USER HAS FILLED PRIMARY
																						// DETAILS
								logger.info("User has filled profile and primary details ----> "
										+ loanApplicationRequest.getApplicationCode() + "======ID======="
										+ loanApplicationRequest.getId());
								return;
							} else {
								// SENT MAIL FOR PRIMARY DETAILS
								logger.info("Mail Template Ready for user has not filled primary details");
								template = NotificationTemplate.LOGOUT_WITHOUT_FILLED_PRIMARY_DETAILS;
							}
						} else {
							// SENT MAIL FOR PRIMARY DETAILS
							logger.info("Mail Template Ready for user has not filled primary details");
							template = NotificationTemplate.LOGOUT_WITHOUT_FILLED_PRIMARY_DETAILS;
						}
					} else {
						logger.info("Mail Template Ready for user has not filled profile details");
					}
				}
				UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
				if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
					@SuppressWarnings("unchecked")
					UsersRequest request = MultipleJSONObjectHelper.getObjectFromMap(
							(LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
					if (!CommonUtils.isObjectNullOrEmpty(request)) {
						Map<String, Object> parameters = new HashMap<String, Object>();
						if (template.getValue() == NotificationTemplate.LOGOUT_WITHOUT_FILLED_PROFILE_DETAILS
								.getValue()) {
							parameters.put(CommonUtils.PARAMETERS_FS_NAME, request.getName());
							parameters.put(CommonUtils.PARAMETERS_APPLICATION_ID,
									!CommonUtils.isObjectNullOrEmpty(loanApplicationRequest.getApplicationCode())
											? loanApplicationRequest.getApplicationCode()
											: "NA");
						} else if (template.getValue() == NotificationTemplate.LOGOUT_WITHOUT_FILLED_PRIMARY_DETAILS
								.getValue()) {
							String fsName = loanApplicationService.getFsApplicantName(loanApplicationRequest.getId());
							parameters.put(CommonUtils.PARAMETERS_FS_NAME,
									!CommonUtils.isObjectNullOrEmpty(fsName) ? fsName : request.getName());
							Integer totalCount = 0;
							try {
								UserResponse response = usersClient
										.getActiveUserCount(CommonUtils.UserType.FUND_PROVIDER);
								if (!CommonUtils.isObjectNullOrEmpty(response) && !CommonUtils.isObjectNullOrEmpty(response.getData()) ) {
										totalCount = (Integer) response.getData();
								}
							} catch (Exception e) {
								logger.error("Throw Excecption While Get Total Fp User Count : ",e);
							}
							parameters.put("total_fp_count", totalCount);
						}
						String[] toIds = { request.getEmail() };
						sendNotification(toIds, userId.toString(), parameters, template, null, false, null,domainId );
						logger.info(
								"Exits, Successfully sent mail when user not filled first profile or primary data ---->"
										+ request.getEmail() + "-----Subject----"
										+ NotificationTemplate.getSubjectName(template.getValue(), null));
					}
				} else {
					logger.info("User response null while getting email id and user type");
				}
			} else {
				logger.info("LoanAoplicationRequest object null or empty");
			}
		} catch (Exception e) {
			logger.info("Throw Exception while sent Mail When User Logout Without Filling First Profile or primary Data : ",e);
		}

	}

	/**
	 * FS Mail Number :- 12 Sent Mail After 3 hour from primary submit If user not
	 * filled final detail.
	 * 
	 * @param userId :- FS Login UserId This Method Called From
	 *               LoanApplicationController(lockPrimary) method
	 */
	@Async
	public void sentMailWhenUserLogoutWithoutFillingFinalData(Long userId, Long applicationId) {
		logger.info("Start Sent Mail Process When User not Fill Final Detail After 3 Hour From Primary Submit ------->"
				+ applicationId);
		try {
			new Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					try {
						Boolean finalDetailFilled = loanApplicationService.isFinalDetailFilled(applicationId, userId);
						if (finalDetailFilled) {
							logger.info("FS user filled final detail within 3 hour from primary submit------->"
									+ applicationId);
							return;
						}
						UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
						if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
							UsersRequest request = MultipleJSONObjectHelper.getObjectFromMap(
									(LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
							if (!CommonUtils.isObjectNullOrEmpty(request)) {
								Map<String, Object> parameters = new HashMap<String, Object>();
								String fsName = loanApplicationService.getFsApplicantName(applicationId);
								parameters.put(CommonUtils.PARAMETERS_FS_NAME,
										!CommonUtils.isObjectNullOrEmpty(fsName) ? fsName : request.getName());
								String[] toIds = { request.getEmail() };
								sendNotification(toIds, userId.toString(), parameters,
										NotificationTemplate.LOGOUT_WITHOUT_FILLED_FINAL_DETAILS, null, false, null,request.getDomainId());
								logger.info(
										"Exits, Successfully sent mail when User not Fill Final Detail After 3 Hour From Primary Submit---->"
												+ request.getEmail() + "------appID---" + applicationId);
							}
						} else {
							logger.info(
									"User response null while getting email id and user type,FS Mail Number :- 12----->"
											+ applicationId);
						}
					} catch (Exception e) {
						logger.error(
								"Error while sent mail when User not Fill Final Detail After 3 Hour From Primary Submit----->"
										+ applicationId);
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
			}, 10800000);
			// 10800000 ---> 3 Hour
		} catch (Exception e) {
			logger.error("Error while sent mail when User not Fill Final Detail After 3 Hour From Primary Submit----->"
					+ applicationId);
			logger.error(CommonUtils.EXCEPTION,e);
		}
	}

	/**
	 * FS Mail Number :- 5 When user logout without selecting any application after
	 * two days this mail sent for remainder
	 * 
	 * @param userId :- FS Login UserId This Method Called From
	 *               sendMailWhenUserHasNoApplication method
	 */
	@Async
	private void sendRemainderMailWhenUserHasNoApplication(Long userId, Map<String, Object> parameters,
			String[] toIds,Long domainId) {
		logger.info("start Sent remainder Mail when user not fill any application till 2 days ------->");
		try {
			new Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					try {
						Long totalApplication = loanApplicationService.getTotalUserApplication(userId);
						if (totalApplication > 0) {
							logger.info(
									"Logout Immediately remainder, Exits method when User has more then one application");
							return;
						}
						sendNotification(toIds, userId.toString(), parameters,
								NotificationTemplate.LOGOUT_IMMEDIATELY_REMAINDER, null, false, null,domainId);
						logger.info("Logout Immediately remainder,Successfully sent mail to this email ===>" + toIds);
					} catch (NotificationException e) {
						logger.error("Error while sent logout immediately reminder mail : ",e);
					}
				}
			}, 172800000);
		} catch (Exception e) {
			logger.error("Error while sent logout immediately reminder mail : ",e);
		}
	}

	/**
	 * FS Mail Number :- 8 Send Mail when Fund seeker submit profile-primary form
	 * and go to matches page
	 * 
	 * @param userId :- FS Login UserId This Method Called From MatchesController
	 */
	@SuppressWarnings("unchecked")
	@Async
	public void sendMailWhenUserCompletePrimaryForm(Long userId, Long applicationId) {
		logger.info("Enter in sending mail when user Complete Primary Form");
		try {
			UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				UsersRequest request = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(request)) {
					Map<String, Object> parameters = getFSMapData(userId, applicationId);
					String[] toIds = { request.getEmail() };
					sendNotification(toIds, userId.toString(), parameters, NotificationTemplate.PRIMARY_FILL_COMPLETE,
							null, false, null,request.getDomainId());
					logger.info("Exits, Successfully sent mail when user complete primary form ---->{}" , request.getEmail());
				}
			}
		} catch (Exception e) {
			logger.error(THROW_EXCEPTION_WHILE_SENDING_MAIL_PRIMARY_COMPLETE,e);
		}
	}

	private UsersRequest getUserNameAndEmail(Long userId) {
		try {
			UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				UsersRequest request = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(request)) {
					return request;
				}
			}
		} catch (Exception e) {
			logger.error("Throw exception while get name and email by userid : ",e);
		}
		return null;
	}

	/**
	 * FS Mail Number :- 9 Send Mail when Fund seeker submit profile-primary form
	 * and go to matches page
	 * 
	 * @param userId :- FS Login UserId This Method Called From
	 *               LoanApplicationController.java
	 */
	@SuppressWarnings("unchecked")
	@Async
	public void sendMailForFirstTimeUserViewMatches(Long applicationId, Long userId) {
		logger.info("Enter in sending mail when user go first time in matches page----"
				+ NotificationTemplate.FS_GO_MATCHES_PAGE.getValue());
		try {
			UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				UsersRequest request = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(request)) {
					Map<String, Object> parameters = getFSMapData(userId, applicationId);
					String[] toIds = { request.getEmail() };
					sendNotification(toIds, userId.toString(), parameters, NotificationTemplate.FS_GO_MATCHES_PAGE,
							null, true, 300000,DomainValue.MSME.getId());
					logger.info("Exits, Successfully sent mail when user go first time in matches page---->"
							+ request.getEmail());
				}
			}
		} catch (Exception e) {
			logger.error(THROW_EXCEPTION_WHILE_SENDING_MAIL_PRIMARY_COMPLETE,e);
		}
	}

	private Map<String, Object> getFSMapData(Long userId, Long applicationId) throws LoansException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		String fsName = loanApplicationService.getFsApplicantName(applicationId);
		parameters.put(CommonUtils.PARAMETERS_FS_NAME, !CommonUtils.isObjectNullOrEmpty(fsName) ? fsName : "NA");
		LoanApplicationRequest loanBasicDetails = loanApplicationService.getLoanBasicDetails(applicationId, userId);
		if (loanBasicDetails != null) {
			parameters.put(CommonUtils.PARAMETERS_APPLICATION_ID,
					!CommonUtils.isObjectNullOrEmpty(loanBasicDetails.getApplicationCode())
							? loanBasicDetails.getApplicationCode()
							: "NA");
			parameters.put("loan", LoanType.getType(loanBasicDetails.getProductId()).getName());
		} else {
			parameters.put(CommonUtils.PARAMETERS_APPLICATION_ID, "NA");
			parameters.put("loan", "NA");
		}

		try {
			logger.info("Stating get total match count");
			ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
			proposalMappingRequest.setApplicationId(applicationId);
			proposalMappingRequest.setUserType(Long.valueOf(CommonUtils.UserType.FUND_SEEKER));
			ProposalMappingResponse proposalDetailsResponse = proposalDetailsClient.connections(proposalMappingRequest);
			if (!CommonUtils.isObjectNullOrEmpty(proposalDetailsResponse)) {
				ConnectionResponse connectionResponse = (ConnectionResponse) MultipleJSONObjectHelper.getObjectFromMap(
						(Map<String, Object>) proposalDetailsResponse.getData(), ConnectionResponse.class);
				if (!CommonUtils.isObjectNullOrEmpty(connectionResponse)) {
					logger.info("successfully get total matches count suggestion list -----> "
							+ connectionResponse.getSuggetionList().size());
					logger.info("successfully get total matches count -----> "
							+ connectionResponse.getSuggetionByMatchesList().size());
					parameters.put(PARAMETERS_TOTAL_MATCHES, connectionResponse.getSuggetionByMatchesList().size());
				} else {
					logger.warn("ConnectionResponse null or empty whilte getting total matches count");
					parameters.put(PARAMETERS_TOTAL_MATCHES, 0);
				}
			} else {
				logger.warn("Something went wrong, Proposal service not available");
				parameters.put(PARAMETERS_TOTAL_MATCHES, 0);
			}

		} catch (Exception e) {
			logger.error("Error while get total suggestion matches list when primary locked mail sending : ",e);
			parameters.put(PARAMETERS_TOTAL_MATCHES, 0);
		}
		return parameters;
	}

	/**
	 * FS Mail Number :- 14 Send Mail when FP Click View More Details But FS not
	 * Complete Final Details
	 * 
	 * @param userId :- FP Login UserId This Method Called From
	 *               LoanApplicationController
	 */
	@SuppressWarnings("unchecked")
	@Async
	public void sendMailWhenUserNotCompleteFinalDetails(Long fpUserId, Long applicationId) {
		logger.info("Enter in sending mail when FP Click View More Details But FS not filled final details");
		try {
			Long lastFpProductId = getLastAccessId(fpUserId);
			if (CommonUtils.isObjectNullOrEmpty(lastFpProductId)) {
				logger.warn("Return, FP Product Id null or empty =========================>{}" , fpUserId);
				return;
			}
			Long userId = loanApplicationService.getUserIdByApplicationId(applicationId);
			UserResponse response = usersClient.checkUserUnderSp(userId);
			if (!CommonUtils.isObjectNullOrEmpty(response) && !(Boolean) response.getData() ) {
					UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
					if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
						UsersRequest request = MultipleJSONObjectHelper.getObjectFromMap(
								(LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
						if (!CommonUtils.isObjectNullOrEmpty(request)) {
							Map<String, Object> parameters = new HashMap<String, Object>();
							String fsName = loanApplicationService.getFsApplicantName(applicationId);
							parameters.put(CommonUtils.PARAMETERS_FS_NAME, !CommonUtils.isObjectNullOrEmpty(fsName) ? fsName : "Sir/Madam");
							LoanApplicationRequest loanBasicDetails = loanApplicationService .getLoanBasicDetails(applicationId, userId);
							if (loanBasicDetails != null) {
								parameters.put(CommonUtils.PARAMETERS_APPLICATION_ID,
										!CommonUtils.isObjectNullOrEmpty(loanBasicDetails.getApplicationCode())
												? loanBasicDetails.getApplicationCode() : "NA");
								parameters.put("loan", LoanType.getType(loanBasicDetails.getProductId()).getName());
							} else {
								parameters.put(CommonUtils.PARAMETERS_APPLICATION_ID, "NA");
								parameters.put("loan", "NA");
							}
							String fpName = "Fund Provider";
							Long domainId=DomainValue.MSME.getId();
							if(loanBasicDetails != null && loanBasicDetails.getProductId() != null &&
									(loanBasicDetails.getProductId() == LoanType.HOME_LOAN.getValue() || loanBasicDetails.getProductId() == LoanType.PERSONAL_LOAN.getValue())) {
								domainId = DomainValue.RETAIL.getId();
							}
							
							try {
//								here generating error 415
								logger.info("Start Getting Fp Name By Fp Product Id =======>{}" , lastFpProductId);
								ProposalMappingResponse activeProposal = proposalDetailsClient
										.getActiveProposalByApplicationID(applicationId);
								logger.info("Active Proposal: {}" , activeProposal.getData());
								ProposalMappingRequest active = MultipleJSONObjectHelper.getObjectFromMap(
										(LinkedHashMap<String, Object>) activeProposal.getData(),
										ProposalMappingRequest.class);
								logger.info("active proposal: {}", active);
								fpName = active.getFpProductName();
								logger.info("FP name is:==={}" , fpName);
								Object[] o = productMasterService.getUserDetailsByPrductId(active.getFpProductId());
								if (o != null) {
									fpName = o[1].toString();
									logger.info("Successfully get fo name------->{}" , fpName);
								} else {
									logger.info("Fund Provider name can't find using {} id" , lastFpProductId );
								}
								parameters.put(CommonUtils.PARAMETERS_FP_NAME, fpName);
							} catch (Exception e) {
								logger.error(ERROR_WHILE_GET_FUND_PROVIDER_NAME,e);
								parameters.put(CommonUtils.PARAMETERS_FP_NAME, fpName);
							}

							try {
								logger.info("Stating get total match count");
								ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
								proposalMappingRequest.setApplicationId(applicationId);
								ProposalCountResponse proposalCountResponse = proposalDetailsClient
										.proposalCountOfFundSeeker(proposalMappingRequest);
								if (!CommonUtils.isObjectNullOrEmpty(proposalCountResponse)) {
									logger.info("Successfully get total matches count ----> "
											+ proposalCountResponse.getMatches());
									parameters.put(PARAMETERS_TOTAL_MATCHES,
											!CommonUtils.isObjectNullOrEmpty(proposalCountResponse.getMatches())
													? proposalCountResponse.getMatches()
													: 0);
								} else {
									logger.info("Something went to wrong while get total matches count");
								}
							} catch (Exception e) {
								logger.error("Error while get total suggestion matches list when final details not filling mail sending : {}",e);
								parameters.put(PARAMETERS_TOTAL_MATCHES, 0);
							}
/*							String[] toIds = { request.getEmail() };
							if (request.getEmail() != null && fpName != null && fsName != null) {
								sendNotification(toIds, userId.toString(), parameters,
										NotificationTemplate.FP_VIEW_MORE_DETAILS, fpName, false, null);
							} else {
								logger.info("Email id is null when sending email from AsynchComponent.");
							} */

							try {
								// SMS
								UsersRequest resp = getEmailMobile(userId);
								if (resp != null && resp.getMobile() != null) {
									sendSMSNotification(String.valueOf(userId), parameters,
											NotificationAlias.SMS_VIEW_MORE_DETAILS,domainId, resp.getMobile());
									logger.info("Sms Sent for fp view more details request:{}" , resp.getMobile());
								}
							} catch (Exception e) {
								logger.error("mobile number is null when sending sms from AsynchComponent.:{}" , e);
							}
						}
					}
			}
		} catch (Exception e) {
			logger.error(THROW_EXCEPTION_WHILE_SENDING_MAIL_PRIMARY_COMPLETE,e);
		}
	}

	private void sendSMSNotification(String userId, Map<String, Object> parameters, Long templateId, Long domainId, String... to)
			throws NotificationException {
		NotificationRequest req = new NotificationRequest();
		req.setClientRefId(userId);
		Notification notification = new Notification();
		notification.setContentType(ContentType.TEMPLATE);
		notification.setTemplateId(templateId);
		notification.setTo(to);
		notification.setType(NotificationType.SMS);
		notification.setParameters(parameters);
		req.addNotification(notification);
		req.setDomainId(domainId);
		notificationClient.send(req);

	}
	
	/**
	 * @author nilay.darji
	 * @param req
	 * @throws McaException
	 * 
	 */
	private void callVerifyApiAsync(VerifyAPIRequest req) throws McaException {
		if(req != null) {
			logger.info("verify api async call ==>>"+req.getApplicationId());
			mcaClient.requestVerifyApi(req);
		}
	}
	@Async
	public void callVerify(VerifyAPIRequest req){
		if(req!= null) {
				try {
					callVerifyApiAsync(req);
				} catch (McaException e) {
					logger.info("Error While call Verify Api"+e); 
				}	
		}
		
	}

	/*@Async
	public void callCubictreeApi(CubictreeJobRegistrationRequest request){
		if(request != null){
			try {
				logger.info("Cubictree Api calling from loans");
				mcaClient.callForjobRegistrationApi(request);
			} catch (McaException e) {
				logger.error("Exception in calling cubictree api :{}",e);
			}
		}
	}*/

	private UsersRequest getEmailMobile(Long userId) throws IOException {
		if (CommonUtils.isObjectNullOrEmpty(userId)) {
			logger.warn("Usesr Id is NULL===>");
			return null;
		}
		UserResponse emailMobile = usersClient.getEmailMobile(userId);
		if (CommonUtils.isObjectListNull(emailMobile, emailMobile.getData())) {
			logger.warn("emailMobile or Data in emailMobile must not be null===>{}", emailMobile);
			return null;
		}

		return MultipleJSONObjectHelper
				.getObjectFromMap((LinkedHashMap<String, Object>) emailMobile.getData(), UsersRequest.class);
	}

	/**
	 * FS Mail Number :- 14 Send Mail when FP Send Direct request to fundseeker
	 * 
	 * @param userId :- FP Login UserId This Method Called From ProposalController
	 */
	@Async
	public void sentMailWhenFPSentFSDirectREquest(Long fpUserId, Long fpProductId, Long applicationId) {
		logger.info("Sent Mail When FundProvider sent direct matches request to Fundseeker");
		try {
			Long userId = loanApplicationService.getUserIdByApplicationId(applicationId);
			logger.info("FPSentDirectRequestToFS, Check FS User Under SP or Not (FS ID) ---->" + userId);
			UserResponse response = usersClient.checkUserUnderSp(userId);
			if (!CommonUtils.isObjectNullOrEmpty(response)) {
				if (!(Boolean) response.getData()) {
					logger.info("FPSentDirectRequestToFS, Get Email And Name By FS User ID ---->" + userId);
					UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
					if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
						UsersRequest request = MultipleJSONObjectHelper.getObjectFromMap(
								(LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
						if (!CommonUtils.isObjectNullOrEmpty(request)) {
							logger.info("FPSentDirectRequestToFS, Start Fill Parameters Details (ApplicationId) ---->"
									+ applicationId);
							Map<String, Object> parameters = new HashMap<String, Object>();
							String fsName = loanApplicationService.getFsApplicantName(applicationId);
							parameters.put(CommonUtils.PARAMETERS_FS_NAME,
									!CommonUtils.isObjectNullOrEmpty(fsName) ? fsName : request.getName());
							LoanApplicationRequest loanBasicDetails = loanApplicationService
									.getLoanBasicDetails(applicationId, userId);
							if (loanBasicDetails != null) {
								logger.info("FPSentDirectRequestToFS, Application Code ----->"
										+ loanBasicDetails.getApplicationCode());
								parameters.put(CommonUtils.PARAMETERS_APPLICATION_ID,
										!CommonUtils.isObjectNullOrEmpty(loanBasicDetails.getApplicationCode())
												? loanBasicDetails.getApplicationCode()
												: "NA");
								logger.info("FPSentDirectRequestToFS, Type of loan ----->"
										+ LoanType.getType(loanBasicDetails.getProductId()).getName());
								parameters.put("loan", LoanType.getType(loanBasicDetails.getProductId()).getName());
							} else {
								parameters.put(CommonUtils.PARAMETERS_APPLICATION_ID, "NA");
								parameters.put("loan", "NA");
							}
							logger.info("FPSentDirectRequestToFS, Start get fp name (fpProductId) ---->" + fpProductId);
							String fpName = "NA";
							try {
								logger.info("Start Getting Fp Name By Fp Product Id =======>" + fpProductId);
								Object[] o = productMasterService.getUserDetailsByPrductId(fpProductId);
								if (o != null) {
									fpName = o[1].toString();
									logger.info("Successfully get fo name------->" + fpName);
								} else {
									logger.info("Fund Provider name can't find using " + fpProductId + " id");
								}
								parameters.put(CommonUtils.PARAMETERS_FP_NAME, fpName);
							} catch (Exception e) {
								logger.error(ERROR_WHILE_GET_FUND_PROVIDER_NAME,e);
								parameters.put(CommonUtils.PARAMETERS_FP_NAME, "NA");
							}
							logger.info("FPSentDirectRequestToFS, End Parameter fill, And Start sending mail to ---->"
									+ request.getEmail());
							Long domainId = DomainValue.MSME.getId();
							if(loanBasicDetails != null && loanBasicDetails.getProductId() != null && (loanBasicDetails.getProductId() == LoanType.HOME_LOAN.getValue() || loanBasicDetails.getProductId() == LoanType.PERSONAL_LOAN.getValue())) {
								domainId = DomainValue.RETAIL.getId();
							}
							String[] toIds = { request.getEmail() };
							sendNotification(toIds, userId.toString(), parameters,NotificationTemplate.FP_DIRECT_SENT_REQUEST_TO_FP, fpName, false, null,domainId);
							logger.info(
									"Exits, Successfully sent mail when fp sent directly request to fs user (FP NAME)---->"
											+ fpName);
						}
					} else {
						logger.info(
								"FPSentDirectRequestToFS, User Data Null or Empty (usersClient.getEmailAndNameByUserId) ---->"
										+ userId);
					}
				} else {
					logger.info("FPSentDirectRequestToFS, FS User Under SP (FS ID) ---->" + userId);
				}
			} else {
				logger.info("FPSentDirectRequestToFS, UserResponse Null Or Empty (usersClient.checkUserUnderSp)---->"
						+ userId);
			}
		} catch (Exception e) {
			logger.error(THROW_EXCEPTION_WHILE_SENDING_MAIL_PRIMARY_COMPLETE,e);
		}
	}

	private Long getLastAccessId(Long userId) {
		try {
			logger.info("Start Getting get last application or fpProduct Id =======>" + userId);
			UserResponse userLastAppResponse = usersClient.getLastAccessApplicant(new UsersRequest(userId));
			if (!CommonUtils.isObjectNullOrEmpty(userLastAppResponse.getId())) {
				logger.info("Successfully get fp product id=======>" + userLastAppResponse.getId());
				return userLastAppResponse.getId();
			}
			return null;
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GET_FUND_PROVIDER_NAME,e);
			return null;
		}
	}

	private void sendNotification(String[] toIds, String userId, Map<String, Object> parameters,
			NotificationTemplate template, String fpName, boolean isTimerMail, Integer milisecond,Long domainId)
			throws NotificationException {
		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setClientRefId(userId);
		// MAKE NOTIFICATION OBJECT
		Notification notification = new Notification();
		notification.setTo(toIds);
		notification.setType(NotificationType.EMAIL);
		notification.setTemplateId(template.getValue());
		notification.setContentType(ContentType.TEMPLATE);
		notification.setParameters(parameters);
		notification.setFrom(environment.getRequiredProperty(EMAIL_ADDRESS_FROM));
		notification.setSubject(NotificationTemplate.getSubjectName(template.getValue(), fpName));
		notificationRequest.addNotification(notification);
		if(domainId != null)
			notificationRequest.setDomainId(domainId);
		else
			notificationRequest.setDomainId(DomainValue.MSME.getId());
		// SEND MAIL
		if (isTimerMail) {
			sendMailWithTimer(notificationRequest, milisecond,
					NotificationTemplate.getSubjectName(template.getValue(), fpName));
		} else {
			sendMail(notificationRequest);
		}
	}

	private void sendMail(NotificationRequest notificationRequest) throws NotificationException {
		notificationClient.send(notificationRequest);
	}

	private void sendMailWithTimer(NotificationRequest notificationRequest, Integer milisecond, String value) {
		logger.info("start Sent Mail with timer ------->" + milisecond + "<-------->" + value);
		try {
			new Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					try {
						logger.info("End Sent Mail Wth Timer------->" + milisecond + "<-------->" + value);
						sendMail(notificationRequest);
					} catch (NotificationException e) {
						logger.error("Error while send mail in notfication : ",e);
					}
				}
			}, milisecond);
		} catch (Exception e) {
			logger.error("Error while call timer method in notification : ",e);
		}
	}

	private String getFundSeekerName(Long applicationId, Long fsUserId) {
		try {
			logger.info("Starting get fund seeker name service");
			int fsProdId = loanApplicationService.getProductIdByApplicationId(applicationId, fsUserId);
			int fsType = CommonUtils.getUserMainType(fsProdId);
			if (CommonUtils.UserMainType.CORPORATE == fsType) {
				logger.info("In Corporate, Find fpProd Id by userid and applicationId");
				CorporateApplicantRequest corporateApplicantRequest = corporateApplicantService
						.getCorporateApplicant(fsUserId, applicationId);
				if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantRequest)) {
					logger.info("Successfully get fundseeker name =====> "
							+ corporateApplicantRequest.getOrganisationName());
					return corporateApplicantRequest.getOrganisationName();
				}
			} else if (CommonUtils.UserMainType.RETAIL == fsType) {
				logger.info("In Retails, Find fpProd Id by userid and applicationId");
				RetailApplicantRequest retailApplicantRequest = retailApplicantService.get(applicationId);
				if (!CommonUtils.isObjectNullOrEmpty(retailApplicantRequest)) {
					String fsName = (!CommonUtils.isObjectNullOrEmpty(retailApplicantRequest.getFirstName())
							? retailApplicantRequest.getFirstName()
							: "")
							+ " "
							+ (!CommonUtils.isObjectNullOrEmpty(retailApplicantRequest.getLastName())
									? retailApplicantRequest.getLastName()
									: "");
					logger.info("Successfully get fundseeker name =====> " + fsName);
					return fsName;
				}
			}
			return null;
		} catch (Exception e) {
			logger.error("Something went wrong while get fundseeker name : ",e);
			return null;
		}

	}

	@Async
	public void sendMailWhenFSSelectOnlinePayment(Long userId, PaymentRequest paymentInfo,
			NotificationTemplate emailNotificationTemplate, Long sysTemplateId) {
		try {
			Long domainId=DomainValue.MSME.getId();
			if (CommonUtils.isObjectNullOrEmpty(paymentInfo.getEmailAddress())) {
				logger.info("Email Address null or Empty while send mail when user select online payment");
			}
			Map<String, Object> parameters = new HashMap<>();
			SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			String fsName = loanApplicationService.getFsApplicantName(paymentInfo.getApplicationId());
			parameters.put(CommonUtils.PARAMETERS_FS_NAME, !CommonUtils.isObjectNullOrEmpty(fsName) ? fsName : "NA");
			parameters.put("entity_name",!CommonUtils.isObjectNullOrEmpty(paymentInfo.getNameOfEntity()) ? paymentInfo.getNameOfEntity(): "NA");
			parameters.put("mobile_number",!CommonUtils.isObjectNullOrEmpty(paymentInfo.getMobileNumber()) ? paymentInfo.getMobileNumber(): "NA");

			String regOfficeAdd = "";
			if (!CommonUtils.isObjectNullOrEmpty(paymentInfo.getAddress())) {
				regOfficeAdd = !CommonUtils.isObjectNullOrEmpty(paymentInfo.getAddress().getPremiseNumber())
						? paymentInfo.getAddress().getPremiseNumber() + ", ": "";
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(paymentInfo.getAddress().getStreetName())
						? paymentInfo.getAddress().getStreetName() + ", ": "";
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(paymentInfo.getAddress().getLandMark())
						? paymentInfo.getAddress().getLandMark() + ", ": "";
				String countryName = getCountryName(paymentInfo.getAddress().getCountryId());
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(countryName) ? countryName + ", " : "";
				String stateName = getStateName(paymentInfo.getAddress().getStateId());
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(stateName) ? stateName + ", " : "";
				String cityName = getCityName(paymentInfo.getAddress().getCityId());
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(cityName) ? cityName : "";
			}
			parameters.put("address", !CommonUtils.isObjectNullOrEmpty(regOfficeAdd) ? regOfficeAdd : "NA");
			parameters.put("appointment_date",
					!CommonUtils.isObjectNullOrEmpty(paymentInfo.getAppointmentDate())
							? dt.format(paymentInfo.getAppointmentDate())
							: "NA");
			parameters.put("appointment_time", paymentInfo.getAppointmentTime());

			String[] toIds = { paymentInfo.getEmailAddress() };
			sendNotification(toIds, userId.toString(), parameters, emailNotificationTemplate, null, false, null,domainId);
			if (!CommonUtils.isObjectNullOrEmpty(sysTemplateId)) {
				String[] toUserIds = { userId.toString() };
				synNotification(toUserIds, userId, sysTemplateId, parameters, paymentInfo.getApplicationId(), null,domainId);
				logger.info("Saved System Notification when FS select Online Payment--------------------------------->"
						+ paymentInfo.getEmailAddress());
			}
			logger.info("Send Mail when FS select Online Payment--------------------------------->"
					+ paymentInfo.getEmailAddress());

		} catch (Exception e) {
			logger.error("Throw Exception while send FS select online payment !!",e);
		}

	}

	/**
	 * SEND MAIL TO CHECKER WHEN MAKER LOCK FINAL DETAILS
	 * 
	 * @param checkerId
	 * @param makerId
	 * @param applicationCode
	 * @param productId
	 * @param fsName
	 */
	@Async
	public void sendEmailWhenMakerLockFinalDetails(Long checkerId, Long makerId, String applicationCode,
			Integer productId, String fsName, Long applicationId) {
		logger.info("Enter in send mail when aker has lock final details then send to checker ");
		try {
			Long domainId = DomainValue.MSME.getId();
			UsersRequest checkerUserName = getUserNameAndEmail(checkerId);
			UsersRequest makerUserName = getUserNameAndEmail(makerId);
			if (CommonUtils.isObjectNullOrEmpty(checkerUserName) || CommonUtils.isObjectNullOrEmpty(makerUserName)) {
				logger.info("Check request or maker request null or empty");
				return;
			}
			Map<String, Object> parameters = new HashMap<>();
			if (makerUserName != null && makerUserName.getName() != null) {
				parameters.put("maker_name", makerUserName.getName());
			}
			if (checkerUserName != null && checkerUserName.getName() != null) {
				parameters.put("checker_name", checkerUserName.getName());
			}
			parameters.put(CommonUtils.PARAMETERS_FS_NAME, fsName);
			parameters.put("lone_type", LoanType.getType(productId).getName());
			if(productId != null && (productId == LoanType.HOME_LOAN.getValue() && productId == LoanType.PERSONAL_LOAN.getValue())) {
				domainId = DomainValue.RETAIL.getId();
			}
			
/*			String[] toIds = { checkerUserName.getEmail() };
			String subject = makerUserName.getName() + " has lock final details for " + applicationCode;
			// STOP THIS MAIL RAHUL WRONG MAIL
			 sendNotification(toIds,checkerId.toString(),parameters,
			 NotificationTemplate.EMAIL_CKR_MKR_FINAL_LOCK,subject,false,null);
			logger.info("Successfully send mail ------------------>" + checkerUserName.getEmail()); */
			String[] toUserIds = { checkerId.toString() };
			synNotification(toUserIds, makerId, NotificationAlias.SYS_CKR_MKR_FINAL_LOCK, parameters, applicationId,null,domainId);
			logger.info("Successfully send system notification------------------>");
		} catch (Exception e) {
			logger.error("Throw exception while sending final lock mail : ",e);
		}

	}
	
	public Boolean sendNotificationToFsWhenProposalIneligibleInRetail(IneligibleProposalDetails inProp) {
        Boolean isSent=false;
        try {
            if((inProp != null) && (inProp.getStatus() == 4) && (inProp.getReason().equals(HOLD_REJECT_REASON_UNABLE_TO_CONTACT_THE_CLIENT))) {
                Map<String, Object> notiParam=new HashMap<String, Object>();
                LoanApplicationMaster lonaApplication = loanApplicationRepository.findOne(inProp.getApplicationId());
                if(lonaApplication.getProductId() != null && (lonaApplication.getProductId() == LoanType.HOME_LOAN.getValue()
                        || lonaApplication.getProductId() == LoanType.PERSONAL_LOAN.getValue()
                        || lonaApplication.getProductId() == LoanType.AUTO_LOAN.getValue() )) {
                    Long domainId = NotificationConstants.NotificationProperty.DomainValue.RETAIL.getId();
                    UsersRequest fsRequest = getUserNameAndEmail(inProp.getCreatedBy());
                   
                    Object[] checkerName = commonRepo.getLastCheckerNameByBranchId(inProp.getBranchId());
                    if(checkerName != null) {
                        String chkName=checkerName[0] != null ?
                                String.valueOf(checkerName[0]).concat(checkerName[1] != null ?" "+checkerName[1] :"")
                                :"Sir/Madam";
                        notiParam.put("fpName", chkName);       
                    }else {
                    	notiParam.put("fpName", "Sir/Madam");
                    }
                    
                    String fsName= null;
                    Object[] retailData = retailApplicantDetailRepository.getBasicDetailsByAppId(inProp.getApplicationId()); 
                    Object[] fsNameData = retailData != null && retailData[0] != null ? (Object[])retailData[0] : null;
                    if(fsNameData != null) {
                    	fsName = (fsNameData[0] != null ? fsNameData[0].toString() : "Sir/Madam") + " " +(fsNameData[1] != null ? fsNameData[1].toString() : "");
                    }else {
                    	fsName = "Sir/Madam";
                    }
                    
                    notiParam.put("fs_name", fsName);
                   
                    if(!CommonUtils.isObjectNullOrEmpty(fsRequest) && !CommonUtils.isObjectNullOrEmpty(fsRequest.getEmail())) {
                        String to = fsRequest.getEmail();   
                        if(to !=null) {
                        	createNotificationForEmail(to, fsRequest.getUserId() != null ? fsRequest.getUserId().toString() : "123", notiParam, NotificationAlias.EMAIL_FS_WHEN_PROPOSAL_REJECT_HOLD_FOR_SPECIFIC_REASON,EmailSubjectAlias.UNABLE_TO_REACH_HOLD_AND_REJECT_MAIL.getSubjectId(), domainId, null);
                            isSent = true;
                        }else {
                            logger.info("to and fpName is null");
                        }
                    }
                    if(!CommonUtils.isObjectNullOrEmpty(fsRequest.getMobile())) {
                        String to = "91"+fsRequest.getMobile();   
//                        sendSMSNotification(lonaApplication.getUserId().toString(), notiParam, null, domainId, inProp.getUserOrgId(),lonaApplication.getProductId(),
//                               NotificationMasterAlias.SMS_FS_REJECT_HOLD_FOR_UNABLE_CONTACT_CLIENT_REASEON.getMasterId(), to);
                        isSent = true;
                    }
                    if(!CommonUtils.isObjectNullOrEmpty(lonaApplication.getUserId())) {
//                        sendSYSNotification(inProp.getApplicationId(),lonaApplication.getUserId().toString(),
//                            notiParam, NotificationAlias.SYS_FS_CHECKER_REJECTS_PROPOSAL, lonaApplication.getUserId().toString(), domainId,inProp.getUserOrgId(),lonaApplication.getProductId(),
//                            NotificationMasterAlias.SYS_FS_REJECT_HOLD_FOR_UNABLE_CONTACT_CLIENT_REASEON.getMasterId(),lonaApplication.getId());
                    }
                   
                   
                }else {
                    return null;
                }
            }
        }catch (Exception e) {
            logger.error("Exception in sending email {}",e);
        }
        return isSent;
    }
	
	private void createNotificationForEmail(String toNo, String userId, Map<String, Object> mailParameters,
			Long templateId, Object subjectId ,Long domainId,String[] cc) throws NotificationException {
		logger.info("Inside send notification===>{}",toNo);
		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setDomainId(domainId);
		try{
			notificationRequest.setIsDynamic(( (Boolean) mailParameters.get(ISDYNAMIC)).booleanValue());
		}catch (Exception e) {
			notificationRequest.setIsDynamic(false);
		}
		notificationRequest.setClientRefId(userId);
		String[] to = { toNo };
		Notification notification = new Notification();
		notification.setContentType(ContentType.TEMPLATE);
		notification.setTemplateId(templateId);
		notification.setSubject(subjectId);
		notification.setTo(to);
		if(cc != null) {
			notification.setCc(cc);
		}
		notification.setType(NotificationType.EMAIL);
		notification.setFrom(EMAIL_ADDRESS_FROM);
		notification.setParameters(mailParameters);
		notification.setIsDynamic(notificationRequest.getIsDynamic());
		notificationRequest.addNotification(notification);
		logger.info("Outside send notification===>{} ==>  status{}",toNo,sendEmail(notificationRequest));
	}
	
	private Long sendEmail(NotificationRequest notificationRequest) throws NotificationException {
		return notificationClient.send(notificationRequest).getResponse_code();
	}

	/**
	 * 
	 * @param toIds         :- TO APPLICATION USER ID
	 * @param fromId        :- CURRENT USER ID
	 * @param templateId    :- NOTIFICATION TEMPLATE ID
	 * @param parameters    :- MAP
	 * @param applicationId :- CURRENT APPLICATION ID
	 * @param fpProductId   :- NON MANDATOY
	 * @param domainId   :- MANDATOY
	 * @return
	 */
	private void synNotification(String[] toIds, Long fromId, Long templateId, Map<String, Object> parameters,
			Long applicationId, Long fpProductId,Long domainId) {

		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setClientRefId(fromId.toString());

		Notification notification = new Notification();

		notification.setTo(toIds);
		notification.setType(NotificationType.SYSTEM);
		notification.setTemplateId(templateId);
		notification.setContentType(ContentType.TEMPLATE);
		notification.setParameters(parameters);
		notification.setFrom(fromId.toString());
		notification.setProductId(fpProductId);
		notification.setApplicationId(applicationId);
		notificationRequest.addNotification(notification);
		notificationRequest.setDomainId(domainId);
		try {
			notificationClient.send(notificationRequest);
		} catch (NotificationException e) {
			logger.error("Throw Exception While Send Sys Notication : ",e);
		}

	}

	@SuppressWarnings("unchecked")
	public String getCityName(Long cityId) {
		try {
			if (CommonUtils.isObjectNullOrEmpty(cityId)) {
				return null;
			}
			List<Long> cityList = new ArrayList<>(1);
			cityList.add(cityId);
			OneFormResponse oneFormResponse = oneFormClient.getCityByCityListId(cityList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0),
						MasterResponse.class);
				return masterResponse.getValue();
			}
		} catch (Exception e) {
			logger.error("Throw Exception while get city name by city Id in Asyn Mail Integation : ",e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public String getStateName(Integer stateId) {
		try {
			if (CommonUtils.isObjectNullOrEmpty(stateId)) {
				return null;
			}
			List<Long> stateList = new ArrayList<>(1);
			stateList.add(stateId.longValue());
			OneFormResponse oneFormResponse = oneFormClient.getStateByStateListId(stateList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0),
						MasterResponse.class);
				return masterResponse.getValue();
			}
		} catch (Exception e) {
			logger.error("Throw Exception while get city name by city Id in Asyn Mail Integation : ",e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public String getCountryName(Integer country) {
		try {
			if (CommonUtils.isObjectNullOrEmpty(country)) {
				return null;
			}
			List<Long> countryList = new ArrayList<>(1);
			countryList.add(country.longValue());
			OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0),
						MasterResponse.class);
				return masterResponse.getValue();
			}
		} catch (Exception e) {
			logger.error("Throw Exception while get country name by country Id in DDR Onform : ",e);
		}
		return null;
	}

	@Async
	public void saveOneformMapping(Long applicationId) {
		try {
			logger.info("ENTER IN SAVE MATCHES JSON WHILE SUBMIT ONEFROM DETAILS");
			MatchRequest req = new MatchRequest();
			req.setApplicationId(applicationId);
			req.setProductId(1l);
			MatchDisplayResponse response = matchEngineClient.displayMatchesOfCorporate(req);
			if (!CommonUtils.isObjectNullOrEmpty(response)) {
				logger.info("RESPONSE WHILE SAVE MATCHES JSON WHILE ONEFORM SUBMIT-----------> " + response.getStatus()
						+ "-----> " + response.getMessage());
			} else {
				logger.info("RESPONSE WHILE SAVE MATCHES JSON WHILE ONEFORM SUBMIT --------------> NULL");
			}
		} catch (Exception e) {
			logger.error("EXCEPTION THROW WHILE SAVE MATCHES JSON WHILE SUBMIT ONEFORM DETAILS : ",e);
		}

	}
	
	public String murgedAddress(String primiseNo,String landMark,String streetName,Long cityId,Long pincode,Long stateId) throws Exception {
		String address="";
		address=primiseNo;
		if(primiseNo==null) {
			address="";
		}
		address=!address.equals("") ? streetName!=null?address.concat(","+streetName):address.concat(""):address.concat(streetName);
		address=!address.equals("") ? landMark!=null?address.concat(","+landMark):address.concat(""):address.concat(landMark);
		String city = getCityName(cityId);	
		address=!address.equals("") ? city!=null?address.concat(","+city):address.concat(""):address.concat(city);
		String state = getStateName(Integer.valueOf(stateId.intValue()));
		address=!address.equals("")  ? state!=null?address.concat(","+state):address.concat(""):address.concat(state);
		address=!address.equals("")  ? pincode!=null?address.concat("-"+pincode):address.concat(""):address.concat(pincode.toString());
		return address;
	}

}
