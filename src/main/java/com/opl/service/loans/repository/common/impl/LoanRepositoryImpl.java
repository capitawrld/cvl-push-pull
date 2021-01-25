package com.opl.service.loans.repository.common.impl;

import java.math.BigInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.repository.common.LoanRepository;


@SuppressWarnings("unchecked")
@Repository
public class LoanRepositoryImpl implements LoanRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoanRepositoryImpl.class);

	@PersistenceContext
	private EntityManager entityManager;

	public Long getUserTypeByEmail(String email, String mobile) {
		try {
			BigInteger id = (BigInteger) entityManager.createNativeQuery("SELECT user_id FROM users.users WHERE email =:email and mobile =:mobile ")
					.setParameter("email", email)
					.setParameter("mobile", mobile)
					.getResultList()
					.stream().findFirst().orElse(null);
			return id != null ? id.longValue() : 0L;
		} catch (Exception e) {
			LOGGER.error(CommonUtils.EXCEPTION,e);
		}
		return 0L;
	}
	
}
