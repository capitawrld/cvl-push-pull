package com.opl.service.loans.service.pushpull.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.users.UsersClient;
import com.opl.service.loans.model.pushpull.PushPullRequest;
import com.opl.service.loans.service.pushpull.PushPullApplicationService;

@Service
@Transactional
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
			return userResponse;
		}catch(Exception e) {
			logger.error("Error While saveUsersData: ", e);
			return null;
		}
	}


	


}
