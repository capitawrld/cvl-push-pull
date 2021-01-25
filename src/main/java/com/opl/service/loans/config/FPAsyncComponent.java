package com.opl.service.loans.config;

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.notification.exception.NotificationException;
import com.opl.mudra.api.notification.model.Notification;
import com.opl.mudra.api.notification.model.NotificationRequest;
import com.opl.mudra.api.notification.model.NotificationResponse;
import com.opl.mudra.api.notification.utils.ContentType;
import com.opl.mudra.api.notification.utils.NotificationType;
import com.opl.mudra.client.gst.GstClient;
import com.opl.mudra.client.matchengine.ProposalDetailsClient;
import com.opl.mudra.client.notification.NotificationClient;
import com.opl.mudra.client.oneform.OneFormClient;
import com.opl.mudra.client.payment.GatewayClient;
import com.opl.mudra.client.users.UsersClient;

@SuppressWarnings("unchecked")
@Component
public class FPAsyncComponent {

	private static final String PRODUCT_ID = "productId";

	private static final String USER_NAME = "user_name";

	/**
	 * 
	 */
	private static final String LOAN_TYPE = "loan_type";

	private static final String SIR_MADAM = "Sir/Madam";

	private static final Logger logger = LoggerFactory.getLogger(FPAsyncComponent.class.getName());

//	private static final String SUBJECT_INTIMATION_NEW_PROPOSAL = "Intimation : New Proposal ";

	private static final String PARAMETERS_PRODUCT_TYPE = "product_type";
	private static final String PARAMETERS_INTEREST_RATE = "interest_rate";
	private static final String PARAMETERS_MOBILE_NO = "mobile_no";
	private static final String PARAMETERS_MAKER_NAME = "maker_name";
	private static final String PARAMETERS_ADMIN_CHECKER = "admin_checker";
	private static final String PARAMETERS_SIR_MADAM = SIR_MADAM;
	private static final String PARAMETERS_ADMIN_MAKER = "admin_maker";
	private static final String PARAMETERS_PRODUCT_NAME = "product_name";
//	private static final String PARAMETERS_BO_NAME = "bo_name";
//	private static final String PARAMETERS_HO_NAME = "ho_name";
	private static final String PARAMETERS_CHECKER_NAME = "checker_name";

	private static final String LITERAL_NULL = "null ";
	private static final String LITERAL_MAKER = "Maker";
	private static final String LITERAL_CHECKER = "Checker";

	private static final String BRANCH_ID = "branch_id";
	private static final String SOMETHING_WENT_WRONG_WHILE_CALLING_USERS_CLIENT = "Something went wrong while calling Users client===>{}";
	private static final String GOT_INPRINCIPLE_RESPONSE_FROM_PROPOSAL_DETAILS_CLIENT = "Got Inprinciple response from Proposal Details Client";
	private static final String CALLING_PROPOSAL_DETAILS_CLIENT_FOR_GETTING_BRANCH_ID = "Calling Proposal details client for getting Branch Id:-";

	private static final String ERROR_WHILE_FETCHING_FP_NAME = "error while fetching FP name : ";
	private static final String ERROR_CALLING_PROPOSAL_DETAILS_CLIENT_FOR_GETTING_BRANCH_ID = "Error calling Proposal Details Client for getting Branch Id:-";

	private static final String MSG_INTO_GETTING_FP_NAME = "Into getting FP Name======>";
//	private static final String MSG_NO_BO_FOUND = "No BO found=================>";
//	private static final String MSG_NO_HO_FOUND = "No HO found=================>";
	private static final String MSG_MOBILE_NO = "Mobile no:-";
	private static final String MSG_MAKER_ID = "Maker ID:---";
//	private static final String MSG_CHECKER_ID = "Checker ID:---";

	private static final String URL_WWW_PSBLOANS_COM = "https://www.psbloansin59minutes.com";
	private static final String DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy";

	/* By Maaz */
//    private static final String PROPOSAL_ID="proposalId";
	@Autowired
	private NotificationClient notificationClient;

	@Autowired
	private UsersClient userClient;

	@Autowired
	private ProposalDetailsClient proposalDetailsClient;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	public GstClient gstClient;

	@Autowired
	private GatewayClient gatewayClient;

	private static final String EMAIL_ADDRESS_FROM = "no-reply@capitaworld.com";

	@Value("${capitaworld.sidbi.mail.to.maker.checker}")
	private Boolean mailToMakerChecker;

	
	public void sendSMSNotification(String userId, Map<String, Object> parameters, Long templateId,Long domainId,Integer productId,Long userOrgId,Long masterId,String... to)
			throws NotificationException {
		logger.info("Inside send SMS===>{}",Arrays.toString(to));
		NotificationRequest req = new NotificationRequest();
		req.setClientRefId(userId);
		Notification notification = new Notification();
		notification.setContentType(ContentType.TEMPLATE);
		notification.setTemplateId(templateId);
		notification.setTo(to);
		notification.setMasterId(masterId);
		notification.setLoanTypeId(productId);
		notification.setUserOrgId(userOrgId);
		notification.setType(NotificationType.SMS);
		notification.setParameters(parameters);
		req.addNotification(notification);
		req.setDomainId(domainId);
		sendEmail(req);
		logger.info("Outside send SMS===>{}",Arrays.toString(to));

	}
	
	public void createNotificationForEmail(String toNo, String userId, Map<String, Object> mailParameters,
			Long templateId, Object emailSubject, Long domainId, String[] cc, Integer loanTypeId, Long userOrgId,
			Long masterId) throws NotificationException {
		logger.info("Inside send notification===>{}", toNo);

		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setClientRefId(userId);
		try {
			notificationRequest
					.setIsDynamic(((Boolean) mailParameters.get(CommonUtils.PARAMETERS_IS_DYNAMIC)).booleanValue());
		} catch (Exception e) {
			notificationRequest.setIsDynamic(false);
		}

		String[] to = { toNo };
		Notification notification = new Notification();
		notification.setContentType(ContentType.TEMPLATE);
		notification.setTemplateId(templateId);
		notification.setSubject(emailSubject);
		notification.setMasterId(masterId);
		notification.setLoanTypeId(loanTypeId);
		notification.setUserOrgId(userOrgId);
		notification.setTo(to);
		notification.setType(NotificationType.EMAIL);
		notification.setFrom(EMAIL_ADDRESS_FROM);
		if (cc != null) {
			notification.setCc(cc);
		}
		notification.setParameters(mailParameters);
		notification.setIsDynamic(notificationRequest.getIsDynamic());
		notificationRequest.setDomainId(domainId);
		notificationRequest.addNotification(notification);
		sendEmail(notificationRequest);
		logger.info("Outside send notification===>{}", toNo);
	}
	
	private void sendEmail(NotificationRequest notificationRequest) throws NotificationException {
		NotificationResponse send = notificationClient.send(notificationRequest);
		logger.info("Notification Sent status :{}",send.getResponse_code_message());
	}

}
