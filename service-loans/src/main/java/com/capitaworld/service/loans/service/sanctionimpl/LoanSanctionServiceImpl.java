package com.capitaworld.service.loans.service.sanctionimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.capitaworld.service.loans.config.FPAsyncComponent;
import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.sanction.LoanSanctionDomain;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.LoanSanctionRequest;
import com.capitaworld.service.loans.repository.OfflineProcessedAppRepository;
import com.capitaworld.service.loans.repository.fundprovider.ProposalDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.capitaworld.service.loans.repository.sanction.LoanSanctionRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.capitaworld.service.loans.service.sanction.LoanSanctionService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.notification.client.NotificationClient;
import com.capitaworld.service.notification.exceptions.NotificationException;
import com.capitaworld.service.notification.model.Notification;
import com.capitaworld.service.notification.model.NotificationRequest;
import com.capitaworld.service.notification.model.NotificationResponse;
import com.capitaworld.service.notification.utils.ContentType;
import com.capitaworld.service.notification.utils.EmailSubjectAlias;
import com.capitaworld.service.notification.utils.NotificationAlias;
import com.capitaworld.service.notification.utils.NotificationType;
import com.capitaworld.service.oneform.enums.SanctionedStatusMaster;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.BranchUserResponse;
import com.capitaworld.service.users.model.UserResponse;
/**
 * @author Ankit
 *
 */

@Service
@Transactional
public class LoanSanctionServiceImpl implements LoanSanctionService {

	private static final String SMS_SENDING_PROCESS_COMPLETE_STATUS_IS = "SMS sending process complete STATUS is :{}";
	private static final String ERROR_IN_SENDING_EMAIL_TO_AND_WHEN_BRANCH_TRANSFER = "-----Error in sending email to {}  and when Branch transfer------{}";
	private static final String EMAIL_SEND_TO_AND_WHEN_BRANCH_TRANSFER = "------Email send to {}  and  when Branch transfer-----{}";
	private static final String CHECKER_LITERAL = "Checker";
	private static final String LOGGER_PARAMETER_FP_NAME_CHEKER = "parameter fpName Cheker=====>{}";
	private static final String PARAM_SIR_MADAM = "Sir/Madam";
	private static final String LOGGER_PARAMETER_FP_NAME_HO = "parameter fpName HO=====>{}";
	private static final String LOGGER_MOBILE_NO = "Mobile No ====>{}";
	private static final String LOGGER_EMAIL_ID = "Email id ====>{}";
	private static final Logger logger = LoggerFactory.getLogger(LoanSanctionServiceImpl.class);
	private static final String LOGGER_SUBJECT = "Subject ====>{}";

	private static final String SUCCESS_LITERAL = "SUCCESS";
	private static final String ERROR_LITERAL = "error";
	private static final String USER_NAME = "userName";
	private static final String HO_NAME = "ho";
	private static final String HO="HO";
	private static final String CHECKER_NAME = "checkerName";
	private static final String BO_CHECKER = "bo";
	private static final String BO="BO";
	private static final String MAKER="Maker";
	private static final String MAKER_NAME= "makerName";
	private static final String CHECKER=CHECKER_LITERAL;
	private static final String EMAIL_ADDRESS_FROM="no-replay@onlinpsbloans.com";
	private static final String ISDYNAMIC="isDynamic";
	private static final String NAME_OF_ENTITY = "nameOfEntity";
	private static final String CLICK_HERE_TO_SEE_THE_PROPOSAL_DETAILS = "clickHereToSeeTheProposalDetails";
	private static final String APPLICATION_ID = "applicationId";

	@Autowired
	private LoanSanctionRepository loanSanctionRepository;
	
	@Autowired 
	private ProposalDetailsRepository proposalDetailsRepository;
	
	@Autowired
	private UsersClient userClient;
	
	@Value("${capitaworld.sidbi.integration.is_production}")
	private String isProduction; 
	
	@Value("${capitaworld.sidbi.integration.reverse_api_timeout}")
	private String reverseAPITimeOut; 

	@Autowired
	private FPAsyncComponent fpAsyncComponent;

	@Autowired
	private OfflineProcessedAppRepository offlineProcessedAppRepository;

	@Autowired
	private NotificationClient notiClient;

	@Autowired
	private LoanApplicationService loanApplicationService;

	@Autowired
	private ApplicationProposalMappingRepository applicationProposalMappingRepository;

	@Override
	public Boolean saveLoanSanctionDetail(LoanSanctionRequest loanSanctionRequest) throws LoansException {
		try {
		logger.info("Enter in saveLoanSanctionDetail() ----------------------->  LoanSanctionRequest==> "+ loanSanctionRequest);
		
		LoanSanctionDomain loanSanctionDomainOld = null;
		if(CommonUtils.isObjectNullOrEmpty(loanSanctionRequest.getNbfcFlow())){
			loanSanctionDomainOld = loanSanctionRepository.findByAppliationIdAndOrgId(loanSanctionRequest.getApplicationId(),loanSanctionRequest.getOrgId());
		}else{
			logger.info("NBFC flow....");
			loanSanctionDomainOld = loanSanctionRepository.findByAppliationIdAndNBFCFlow(loanSanctionRequest.getApplicationId(),loanSanctionRequest.getNbfcFlow());
		}
		if(CommonUtils.isObjectNullOrEmpty(loanSanctionDomainOld) ) {
			loanSanctionDomainOld = new LoanSanctionDomain();
			BeanUtils.copyProperties(loanSanctionRequest, loanSanctionDomainOld,"id");
			loanSanctionDomainOld.setOrgId(!CommonUtils.isObjectNullOrEmpty(loanSanctionRequest.getOrgId()) ? loanSanctionRequest.getOrgId() : null);
			if(loanSanctionRequest.getIsIneligibleProposal() != null && loanSanctionRequest.getIsIneligibleProposal() == true) {
				loanSanctionDomainOld.setIsSanctionedFrom(loanSanctionRequest.getIsSanctionedFrom());
				/*IneligibleProposalDetails ineligibleProposalDetails = (IneligibleProposalDetails) offlineProcessedAppRepository.findByAppliationId(loanSanctionRequest.getApplicationId());
				ineligibleProposalDetails.setIsSanctioned(true);*/
				//update sanctioned is true flag in ineligible proposal table
				Long userId = null;
				if(!CommonUtils.isObjectNullOrEmpty(loanSanctionRequest.getActionBy())) {
					userId = Long.valueOf(loanSanctionRequest.getActionBy());
				}
				offlineProcessedAppRepository.updateSanctionedFlag(loanSanctionRequest.getApplicationId(), loanSanctionRequest.getOrgId(), loanSanctionRequest.getBranch(), userId);
			} else if(CommonUtils.isObjectNullOrEmpty(loanSanctionRequest.getIsIneligibleProposal()) || loanSanctionRequest.getIsIneligibleProposal() == false) {
				loanSanctionDomainOld.setIsSanctionedFrom(CommonUtils.sanctionedFrom.ELIGIBLE_USERS);
			}
			loanSanctionDomainOld.setCreatedBy(loanSanctionRequest.getActionBy());
			loanSanctionDomainOld.setCreatedDate(new Date());
			loanSanctionDomainOld.setStatus(SanctionedStatusMaster.IN_PROGRES.getId());
			loanSanctionDomainOld.setIsActive(true);
		}else{
			//BeanUtils.copyProperties(loanSanctionRequest, loanSanctionDomainOld,"id");
			if(loanSanctionRequest.getIsIneligibleProposal() != null && loanSanctionRequest.getIsIneligibleProposal()) {
				loanSanctionDomainOld.setIsSanctionedFrom(loanSanctionRequest.getIsSanctionedFrom());
				/*IneligibleProposalDetails ineligibleProposalDetails = (IneligibleProposalDetails) offlineProcessedAppRepository.findByAppliationId(loanSanctionRequest.getApplicationId());
				ineligibleProposalDetails.setIsSanctioned(true);*/
				//update sanctioned is true flag in ineligible proposal table
				Long userId = null;
				if(!CommonUtils.isObjectNullOrEmpty(loanSanctionRequest.getActionBy())) {
					userId = Long.valueOf(loanSanctionRequest.getActionBy());
				}
				offlineProcessedAppRepository.updateSanctionedFlag(loanSanctionRequest.getApplicationId(), loanSanctionRequest.getOrgId(), loanSanctionRequest.getBranch(), userId);
			} else {
				loanSanctionDomainOld.setIsSanctionedFrom(CommonUtils.sanctionedFrom.ELIGIBLE_USERS);
			}
			loanSanctionDomainOld.setOrgId(!CommonUtils.isObjectNullOrEmpty(loanSanctionRequest.getOrgId()) ? loanSanctionRequest.getOrgId() : null);
			loanSanctionDomainOld.setSanctionAmount(loanSanctionRequest.getSanctionAmount());
			loanSanctionDomainOld.setSanctionDate(new Date());
			loanSanctionDomainOld.setTenure(loanSanctionRequest.getTenure());
			loanSanctionDomainOld.setRoi(loanSanctionRequest.getRoi());
			loanSanctionDomainOld.setProcessingFee(loanSanctionRequest.getProcessingFee());
			loanSanctionDomainOld.setRemark(loanSanctionRequest.getRemark());
			loanSanctionDomainOld.setNbfcFlow(loanSanctionRequest.getNbfcFlow());
			loanSanctionDomainOld.setModifiedBy(loanSanctionRequest.getActionBy());
			loanSanctionDomainOld.setModifiedDate(new Date());
			loanSanctionDomainOld.setStatus(SanctionedStatusMaster.IN_PROGRES.getId());
			/*loanSanctionDomainOld.setIsSanctionedFrom(1l);*/
		}

		if(loanSanctionRequest.getBusinessTypeId().intValue() != CommonUtils.BusinessType.MFI.getId())
		{
			//==================Sending Mail notification to Maker=============================
			try{
				fpAsyncComponent.sendEmailToFSWhenCheckerSanctionLoan(loanSanctionDomainOld);
			}catch(Exception e){
				logger.error("Exception : {}",e);
			}
			Integer count = proposalDetailsRepository.getCountOfProposalDetailsByApplicationId(loanSanctionDomainOld.getApplicationId());
			if(count > 1) {
				try {
					fpAsyncComponent.sendEmailToMakerHOBOWhenCheckerSanctionLoan(loanSanctionDomainOld);
					//sendMailToHOBOCheckerMakerForMultipleBanks(loanSanctionDomainOld.getApplicationId());
				}catch (IndexOutOfBoundsException e) {
					logger.info("Application not from multiple bank applicationid:{}",loanSanctionDomainOld.getApplicationId());
				}
			}else {
				try
				{
					fpAsyncComponent.sendEmailToMakerHOBOWhenCheckerSanctionLoan(loanSanctionDomainOld);
				}
				catch (Exception e)
				{
					logger.error("Exception : {}",e);
				}
			}
		}
		//=================================================================================
		return loanSanctionRepository.save(loanSanctionDomainOld) != null;
		}catch (Exception e) {
			logger.error("Error/Exception in saveLoanSanctionDetail() -----------------------> Message : ",e);
			throw new LoansException(e);
		}

	}


	public Boolean sendMailToHOBOCheckerMakerForMultipleBanks(Long applicationId) {
		logger.info("inside notification start for sanction In Multibank case of ApplicationId==>{}" ,applicationId);
		Boolean isSent = false;
		List<Object[]> proposalDetailByApplicationId = proposalDetailsRepository.findProposalDetailByApplicationId(applicationId);
		
		//check multibank condition if list is greater than 1
		if(proposalDetailByApplicationId != null && proposalDetailByApplicationId.size() > 1){
			
			String[] to = new String[] {};
			String toUserId = null;
			List<String> ccList = new ArrayList<String>();
//			String subject = "Intimation - Another Bank has Sanctioned the Proposal";
			Map<String, Object> parameters = new HashMap<>();
			Boolean result = false;
			
			for(Object[] arr : proposalDetailByApplicationId ){
				Integer proposalStatus = !CommonUtils.isObjectNullOrEmpty(arr[1]) ? Integer.valueOf(String.valueOf(arr[1])) : null;
				Long branchId = !CommonUtils.isObjectNullOrEmpty(arr[3]) ? Long.valueOf(String.valueOf(arr[3])) : null;
				if (proposalStatus != 5) {
					try {
						UserResponse userResponse = userClient.getBranchUsersListByBranchId(branchId);
						String organizationName = loanApplicationService.getFsApplicantName(applicationId);
						ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.getByApplicationId(applicationId);
						String applicationCode = applicationProposalMapping.getApplicationCode();
						if(!CommonUtils.isObjectNullOrEmpty(userResponse) && !CommonUtils.isObjectListNull(userResponse.getListData())){
							for(int i=0;i<userResponse.getListData().size();i++) {
								
								BranchUserResponse branchUserResponse = MultipleJSONObjectHelper.getObjectFromMap((Map) userResponse.getListData().get(i), BranchUserResponse.class);
								String smsTo = null;
								String fpName = "";
								logger.info("BranchUser=>{}",branchUserResponse );
								String userId = branchUserResponse.getUserId();
								fpName = branchUserResponse.getUserName();
								
								if (branchUserResponse.getUserRole().equals(HO) || branchUserResponse.getUserRole().equals(BO) || branchUserResponse.getUserRole().equals(CHECKER)) {
									parameters.put(NAME_OF_ENTITY, organizationName != null ? organizationName : "Fund Seeker");	
									if(to == null && !CommonUtils.isObjectNullOrEmpty(branchUserResponse.getEmail())) {
										to = new String[] {branchUserResponse.getEmail()};
										toUserId = branchUserResponse.getUserId();
										parameters.put(USER_NAME, fpName);
										parameters.put(APPLICATION_ID, applicationCode);
										parameters.put(CHECKER_NAME, !CommonUtils.isObjectNullOrEmpty(fpName) ? fpName :PARAM_SIR_MADAM);
									}else {
										if(!CommonUtils.isObjectNullOrEmpty(branchUserResponse.getEmail())) {
											ccList.add(branchUserResponse.getEmail());
										}
									}
									
									//send Sms
									smsTo = branchUserResponse.getMobile();
									Boolean smsStatus = createNotificationForSMS(smsTo,userId,parameters,NotificationAlias.SMS_SANCTION_HO_MULTIPLE_BANK);
									logger.info(SMS_SENDING_PROCESS_COMPLETE_STATUS_IS , smsStatus);
								}
								logger.info("{} ====> {} ===>{}  ====> {} ====> {}",userId,branchUserResponse.getUserRole(),fpName,organizationName,branchUserResponse.getMobile()  );
							}
						}
					} catch (Exception e) {
						logger.error("Exception",e);
					}
					
					String[] cc = {};
					if(!ccList.isEmpty()) {
						cc= ccList != null && !ccList.isEmpty() ? ccList.toArray(new String[ccList.size()]) : null;
					}
					
					result = sendEmail(to ,toUserId != null ? toUserId : "123",parameters, NotificationAlias.EMAIL_SANCTION_CHECKER_MULTIPLE_BANK,
							EmailSubjectAlias.EMAIL_SANCTION_CHECKER_MULTIPLE_BANK.getSubjectId() ,cc);
					if(result) {
						isSent = true;
						logger.info("Email send when When Sanction In MultiBank-----With To==>{} And CC==>{} and ApplicationId==>{}" , to != null?Arrays.asList(to):null,cc != null?Arrays.asList(cc):null ,applicationId);
					}else{
						logger.error("Error/Exception while sending Mail When Sanction In MultiBank to==>{}  with applicationId==>{}",to != null?Arrays.asList(to):null,applicationId);
					}
				}
			}
		}
	 
		logger.info("outside notification end for sanction Of Multibank of ApplicationId==>{}" ,applicationId);
		return isSent;
	}


	private Boolean sendEmail(String[] toNo,String userId,Map<String, Object> parameters,Long templateId,Object subject , String[] cc) {
		logger.info("inside email for {}" ,toNo != null?Arrays.asList(toNo):null);
		Boolean isSent = false;
		NotificationRequest notificationRequest=new NotificationRequest();
		try {
			notificationRequest.setClientRefId(userId);
			try{
				notificationRequest.setIsDynamic(((Boolean) parameters.get(ISDYNAMIC)).booleanValue());
			}catch (Exception e) {
				notificationRequest.setIsDynamic(false);
			}

			Notification notification = new Notification();
			notification.setContentType(ContentType.TEMPLATE);
			notification.setTemplateId(templateId);
			notification.setSubject(subject);
			notification.setTo(toNo);
			notification.setType(NotificationType.EMAIL);
			notification.setFrom(EMAIL_ADDRESS_FROM);
			notification.setParameters(parameters);
			notification.setIsDynamic(notificationRequest.getIsDynamic());
			if(cc != null) {
				notification.setCc(cc);
			}
			notificationRequest.addNotification(notification);

			Boolean status = sendNotification(notificationRequest);
			logger.info(" email status :  {}",status);


			isSent=status;
		} catch (NotificationException e) {
			logger.debug("Error in sending mail To {} for {}",notificationRequest.getNotifications().get(0).getTo(),notificationRequest.getAlias());
			logger.debug("Error :{}",e.getMessage());
			logger.error("Exception",e);
			isSent = false;
		}
		logger.info("outside email for {}",toNo != null?Arrays.asList(toNo):null);
		return isSent;
	}

	private Boolean createNotificationForSMS(String toNo,String toUserId,Map<String, Object> parameters,Long templateId) throws NotificationException {
		logger.info("inside sms for "+toNo);
		String to[] = { 91 + toNo };
		Notification notification = new Notification();
		NotificationRequest notificationRequest = new NotificationRequest();
		Boolean isSent = false;
		try {
			notification.setContentType(ContentType.TEMPLATE);
			notification.setTemplateId(templateId);
			notification.setTo(to);
			notification.setType(NotificationType.SMS);
			notification.setParameters(parameters);


			notificationRequest.addNotification(notification);
			notificationRequest.setClientRefId(toUserId);
			Boolean status = sendNotification(notificationRequest);

			isSent = status;
		}
		catch (NotificationException e){
			logger.info("Error in sending SMS To "+notificationRequest.getNotifications().get(0).getTo()+" for "+notificationRequest.getAlias());
			logger.info("Error :"+e.getMessage());
			logger.error("Exception",e);
			isSent = false;
		}
		logger.info("outside sms for "+toNo);
		return isSent;
	}

	private Boolean sendNotification(NotificationRequest notificationRequest) throws NotificationException {
		Boolean status=false;
		for (Notification noti : notificationRequest.getNotifications()) {
			if (noti.getTemplateId() != null && noti.getTemplateId() != 0) {


				NotificationResponse send = notiClient.send(notificationRequest);
				System.out.println("data = "+send.getMessage()+ "status :"+send.getStatus() );
				if(send.getStatus()==200L) {
					status = true;
				}else {
					status = false;
				}
			}
		}
		return status;
	}


	@Override
	public String sanctionRequestValidation( Long applicationId,Long orgId) throws LoansException {
		logger.info("Enter in requestValidation() ----------------------->  applicationId==> "+ applicationId);
	        try {        	
		 
	        	Long recCount = proposalDetailsRepository.getApplicationIdCountByOrgId(applicationId ,orgId);
	        	if(recCount != null && recCount  > 0) {
	        		return  SUCCESS_LITERAL;
	        	}else {
	        		return "Invalid ApplicationId ";
	        	}		 
	        }catch (Exception e) {
	        	logger.error("Error/Exception in requestValidation() ----------------------->  Message : ", e);
	        	throw new LoansException(e);
			}
	}

	@Override
	public Integer saveSanctionDetailFromPopup(LoanSanctionRequest loanSanctionRequest) throws LoansException {
		logger.info("Enter in saveSanctionDetailFromPopup() ----------------------------- sanctionRequest Data : "+ loanSanctionRequest.toString());
		try {

			if(loanSanctionRequest.getIsSanctionedFrom() == 2 && loanSanctionRequest.getBusinessTypeId() == 1){
				//FIRST CHECK IF CURRENT PROPOSAL IS ELIGIBL FOR SANCTIONED OR NOT
				Integer status = offlineProcessedAppRepository.checkBeforeOfflineSanctioned(loanSanctionRequest.getApplicationId());
				if(status == 4) {//OFFLINE
					loanSanctionRequest.setSanctionDate(new Date());
					Boolean result = saveLoanSanctionDetail(loanSanctionRequest);
					return !result ? 0 : 4;
				} else {
					return status;
				}
			} else {//OFFLINE
				loanSanctionRequest.setSanctionDate(loanSanctionRequest.getSanctionDate() != null ? loanSanctionRequest.getSanctionDate() :new Date());
				Boolean result = saveLoanSanctionDetail(loanSanctionRequest);
				return !result ? 0 : 4;
			}
			/*loanSanctionRequest.setSanctionDate(new Date());
			return saveLoanSanctionDetail(loanSanctionRequest);*/
			/*logger.info("going to fetch username/password");
			UserOrganisationRequest userOrganisationRequest = userClient.getByOrgId(loanSanctionRequest.getOrgId());
			if(CommonUtils.isObjectListNull( userOrganisationRequest, userOrganisationRequest.getUsername(),  userOrganisationRequest.getPassword() )){
				logger.warn("username/password found null ");
				return false;
			}
			loanSanctionRequest.setUserName(userOrganisationRequest.getUsername());
			loanSanctionRequest.setPassword(userOrganisationRequest.getPassword());*/
		} catch (Exception e) {
			logger.error("Error/Exception in saveSanctionDetailFromPopup() ----------------------->  Message : ",e);
			return 0;
		}
	}



	@Override
	public LoanSanctionRequest getSanctionDetail(Long applicationId) throws LoansException {
		LoanSanctionRequest loanSanctionRequest = null;
		
		LoanSanctionDomain loanSanctionDomain = loanSanctionRepository.findByAppliationId(applicationId);
		if(loanSanctionDomain != null) {
			loanSanctionRequest = new LoanSanctionRequest();
			loanSanctionRequest.setSanctionDate(loanSanctionDomain.getSanctionDate());
			loanSanctionRequest.setSanctionAmount(loanSanctionDomain.getSanctionAmount());
			loanSanctionRequest.setRoi(loanSanctionDomain.getRoi());
			loanSanctionRequest.setTenure(loanSanctionDomain.getTenure());
			loanSanctionRequest.setRemark(loanSanctionDomain.getRemark());
		}
		
		return loanSanctionRequest;
	}

}
