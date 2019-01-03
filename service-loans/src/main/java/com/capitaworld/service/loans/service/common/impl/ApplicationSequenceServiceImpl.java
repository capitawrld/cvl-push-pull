package com.capitaworld.service.loans.service.common.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.repository.ApplicationSequenceRepository;
import com.capitaworld.service.loans.service.common.ApplicationSequenceService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;

/**
 * Created by dhaval on 02-Jun-17.
 */
@Service
@Transactional
public class ApplicationSequenceServiceImpl implements ApplicationSequenceService {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationSequenceServiceImpl.class);

	private static final String GET_APPLICATION_SEQUENCE_NUMBER = "getApplicationSequenceNumber";
	
    @Autowired
    private ApplicationSequenceRepository applicationSequenceRepository;

    @Override
    public String getApplicationSequenceNumber(int productId) {
      CommonDocumentUtils.startHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
        Long sequenceNumber;
        switch (productId){
            case 1://WORKING CAPITAL
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-AWC-"+sequenceNumber;
            case 2://WORKING CAPITAL
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-ATL-"+sequenceNumber;
            case 3://HOME LOAN
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-AHL-"+sequenceNumber;
            case 7://PERSONAL LOAN
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-APL-"+sequenceNumber;
            case 12://CAR_LOAN
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-ACL-"+sequenceNumber;
            case 13://LOAN_AGAINST_PROPERTY
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-ALAP-"+sequenceNumber;
            case 14://LOAN_AGAINST_SHARES_AND_SECUIRITIES
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-ALAS-"+sequenceNumber;
            case 15://UNSECURED LOAN
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-AUSL-"+sequenceNumber;
            case 16://WORKING CAPITAL TERM LOAN
                sequenceNumber = applicationSequenceRepository.getApplicationSequenceNumber(Long.valueOf(productId));
                sequenceNumber+=1;
                applicationSequenceRepository.updateSequenceNumber(sequenceNumber, Long.valueOf(productId));
                CommonDocumentUtils.endHook(logger, GET_APPLICATION_SEQUENCE_NUMBER);
                return "CW-AWCTL-"+sequenceNumber;
            default:
                return null;
        }
    }
}
