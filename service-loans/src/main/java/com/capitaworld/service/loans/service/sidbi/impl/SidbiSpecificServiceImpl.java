package com.capitaworld.service.loans.service.sidbi.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.service.sidbi.SidbiSpecificService;

@Service
@Transactional
public class SidbiSpecificServiceImpl implements SidbiSpecificService{

	private static final Logger logger = LoggerFactory.getLogger(SidbiSpecificServiceImpl.class);
	
}
