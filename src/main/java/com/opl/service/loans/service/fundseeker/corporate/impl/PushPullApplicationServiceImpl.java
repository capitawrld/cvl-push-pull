package com.opl.service.loans.service.fundseeker.corporate.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.AdminPanelLoanDetailsResponse;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.LoanApplicationDetailsForSp;
import com.opl.mudra.api.loans.model.LoanApplicationRequest;
import com.opl.mudra.api.loans.model.LoanPanCheckRequest;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.model.PaymentRequest;
import com.opl.mudra.api.loans.model.TutorialUploadManageRes;
import com.opl.mudra.api.loans.model.TutorialsViewAudits;
import com.opl.mudra.api.loans.model.api_model.LoantypeSelectionResponse;
import com.opl.mudra.api.loans.model.api_model.ProfileReqRes;
import com.opl.mudra.api.loans.model.common.BasicDetailFS;
import com.opl.mudra.api.loans.model.common.CGTMSECalcDataResponse;
import com.opl.mudra.api.loans.model.common.ChatDetails;
import com.opl.mudra.api.loans.model.common.DisbursementRequest;
import com.opl.mudra.api.loans.model.common.EkycRequest;
import com.opl.mudra.api.loans.model.common.EkycResponse;
import com.opl.mudra.api.loans.model.common.HunterRequestDataResponse;
import com.opl.mudra.api.loans.model.common.MinMaxProductDetailRequest;
import com.opl.mudra.api.loans.model.common.ProposalList;
import com.opl.mudra.api.loans.model.common.SanctioningDetailResponse;
import com.opl.mudra.api.loans.model.corporate.CorporateProduct;
import com.opl.mudra.api.loans.model.mobile.MLoanDetailsResponse;
import com.opl.mudra.api.loans.model.mobile.MobileLoanRequest;
import com.opl.mudra.api.loans.model.retail.BankRelationshipRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.scoring.model.scoringmodel.ScoringModelReqRes;
import com.opl.mudra.api.user.model.FpProfileBasicDetailRequest;
import com.opl.mudra.api.user.model.RegisteredUserResponse;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.users.UsersClient;
import com.opl.service.loans.domain.sidbi.PushPullRequest;
import com.opl.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.opl.service.loans.service.fundseeker.corporate.PushPullApplicationService;

@Service
@Transactional
@SuppressWarnings({"rawtypes","unchecked"})
public class PushPullApplicationServiceImpl implements PushPullApplicationService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private UsersClient userClient;
	
	private static final Logger logger = LoggerFactory.getLogger(PushPullApplicationServiceImpl.class.getName());

	@Override
	public LoansResponse saveOrUpdate(PushPullRequest pushPullRequest) throws LoansException {
		if (!CommonUtils.isObjectNullOrEmpty(pushPullRequest)) {
			UserResponse userResponse = saveUsersData(pushPullRequest);
		}
		return null;
	}
	
	private UserResponse saveUsersData(PushPullRequest pushPullRequest) {
		try {
			UsersRequest usersRequest = new UsersRequest();
			usersRequest.setEmail(pushPullRequest.getEmail());
			usersRequest.setMobile(pushPullRequest.getMobile());
			usersRequest.setPan(pushPullRequest.getPan());
			usersRequest.setGstin(pushPullRequest.getGstIn());
			UserResponse userResponse = userClient.saveCvlPushPull(usersRequest);
//			UserResponse userResponse = userClient.saveNewStructureDFSDealerUser(usersRequest);
			
			return userResponse;
		}catch(Exception e) {
			logger.error("Error While saveUsersData: ", e);
			return null;
		}
	}


	


}
