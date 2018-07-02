package com.capitaworld.service.loans.service.sanctionimpl;

import java.util.Date;
import javax.transaction.Transactional;

import com.capitaworld.service.loans.domain.BankCWAuditTrailDomain;
import com.capitaworld.service.loans.model.common.SanctioningDetailResponse;
import com.capitaworld.service.users.model.UserOrganisationRequest;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capitaworld.service.loans.domain.sanction.LoanSanctionDomain;
import com.capitaworld.service.loans.model.LoanSanctionRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.repository.banktocw.BankToCWAuditTrailRepository;
import com.capitaworld.service.loans.repository.fundprovider.ProposalDetailsRepository;
import com.capitaworld.service.loans.repository.sanction.LoanSanctionRepository;
import com.capitaworld.service.loans.service.sanction.LoanSanctionService;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.loans.domain.BankCWAuditTrailDomain;
import com.capitaworld.service.loans.domain.sanction.LoanSanctionDomain;
import com.capitaworld.service.loans.model.common.SanctioningDetailResponse;
import com.capitaworld.service.users.model.UserOrganisationRequest;
import com.sun.org.apache.xpath.internal.operations.Bool;
/**
 * @author Ankit
 *
 */

@Service
@Transactional
public class LoanSanctionServiceImpl implements LoanSanctionService {
	private static final Logger logger = LoggerFactory.getLogger(LoanSanctionServiceImpl.class);

	@Autowired
	private LoanSanctionRepository loanSanctionRepository;
	
	@Autowired 
	private ProposalDetailsRepository proposalDetailsRepository;

	@Autowired
	private BankToCWAuditTrailRepository bankToCWAuditTrailRepository;

	@Autowired
	private UsersClient userClient;


	@Override
	public Boolean saveLoanSanctionDetail(LoanSanctionRequest loanSanctionRequest) throws Exception {
		try {
		logger.info("Enter in saveLoanSanctionDetail() ----------------------->  LoanSanctionRequest==> "+ loanSanctionRequest);
		
		LoanSanctionDomain loanSanctionDomainOld =loanSanctionRepository.findByAppliationId(loanSanctionRequest.getApplicationId());
		if(CommonUtils.isObjectNullOrEmpty(loanSanctionDomainOld) ) {
			loanSanctionDomainOld = new LoanSanctionDomain();
			BeanUtils.copyProperties(loanSanctionRequest, loanSanctionDomainOld,"id");
			loanSanctionDomainOld.setCreatedBy(loanSanctionRequest.getActionBy());
			loanSanctionDomainOld.setCreatedDate(new Date());
			loanSanctionDomainOld.setIsActive(true);
		}else{
			BeanUtils.copyProperties(loanSanctionRequest, loanSanctionDomainOld,"id");
			loanSanctionDomainOld.setModifiedBy(loanSanctionRequest.getActionBy());
			loanSanctionDomainOld.setModifiedDate(new Date());
		}
		logger.info("Exit saveLoanSanctionDetail() -----------------------> LoanSanctionDomain "+ loanSanctionDomainOld);
		return loanSanctionRepository.save(loanSanctionDomainOld) != null;
		}catch (Exception e) {
			logger.info("Error/Exception in saveLoanSanctionDetail() -----------------------> Message "+e.getMessage());
			e.printStackTrace();
			throw e;
		}

	}
	
	@Override
	public String requestValidation( Long applicationId,Long orgId) throws Exception {
		logger.info("Enter in requestValidation() ----------------------->  applicationId==> "+ applicationId);
	        try {        	
		 
		 if(orgId != null) {
			 Long recCount = proposalDetailsRepository.getApplicationIdCountByOrgId(applicationId ,orgId);
			if(recCount != null && recCount  > 0) {
					return  "SUCCESS";
			}else {
				return "Invalid ApplicationId ";
			}
		 }else {
			 return "Invalid Credential";
		 }
	        }catch (Exception e) {
	        	logger.info("Error/Exception in requestValidation() ----------------------->  Message "+ e.getMessage());
	        	throw e;
			}
	}



	@Override
	public void saveBankReqRes(LoanSanctionRequest loanSanctionRequest,Integer apiType,  LoansResponse loansResponse, String failureReason ,Long orgId) {
		logger.info("Enter in saveBankReqRes() -----------------------> LoanSanctionRequest ==>"+ loanSanctionRequest+ " orgId==> "+ orgId);
		try {
			BankCWAuditTrailDomain bankCWAuditTrailDomain = new BankCWAuditTrailDomain();
			bankCWAuditTrailDomain.setApplicationId(loanSanctionRequest !=null?loanSanctionRequest.getApplicationId():null);
			bankCWAuditTrailDomain.setOrgId(orgId);
			bankCWAuditTrailDomain.setBankRequest(MultipleJSONObjectHelper.getStringfromObject(loanSanctionRequest));
			bankCWAuditTrailDomain.setCwResponse(MultipleJSONObjectHelper.getStringfromObject(loansResponse.toString()));
			bankCWAuditTrailDomain.setFailureReason(failureReason);
			bankCWAuditTrailDomain.setIsActive(true);
			bankCWAuditTrailDomain.setCreatedDate(new Date());
			bankCWAuditTrailDomain.setApiType(apiType);
			if(loansResponse.getStatus()==200) {
				bankCWAuditTrailDomain.setStatus("SUCCESS");
			}else {
				bankCWAuditTrailDomain.setStatus("FAILURE");
			}
			bankToCWAuditTrailRepository.save(bankCWAuditTrailDomain);
		}catch (Exception e) {
			logger.info("Error/Exception in saveBankReqRes() ----------------------->  Message "+ e.getMessage());
			e.printStackTrace();
			/*throw e;*/
		}
	}

	@Override
	public Long getOrgIdByCredential(String userName, String pwd) {
		return userClient.getOrganisationDetailIdByCredential(userName, pwd);

	}

	@Override
	public Boolean saveSanctionDetailFromPopup(LoanSanctionRequest loanSanctionRequest) throws Exception {

		logger.info("Enter in saveSanctionDetailFromPopup() ----------------------------- sanctionRequest Data : "+ loanSanctionRequest.toString());
		try {


			logger.info("going to fetch username/password");
			UserOrganisationRequest userOrganisationRequest = userClient.getByOrgId(loanSanctionRequest.getOrgId());
			if(CommonUtils.isObjectListNull( userOrganisationRequest, userOrganisationRequest.getUsername(),  userOrganisationRequest.getPassword() )){
				logger.warn("username/password found null ");
				return false;
			}

			loanSanctionRequest.setUserName(userOrganisationRequest.getUsername());
			loanSanctionRequest.setPassword(userOrganisationRequest.getPassword());
			loanSanctionRequest.setSanctionDate(new Date());

			return saveLoanSanctionDetail(loanSanctionRequest);

		}catch (Exception e) {
			logger.info("Error/Exception in saveSanctionDetailFromPopup() ----------------------->  Message "+ e.getMessage());
			e.printStackTrace();
			return false;
		}

	}



}
