package com.capitaworld.service.loans.service.fundprovider.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.loans.domain.fundprovider.GeographicalCityDetail;
import com.capitaworld.service.loans.domain.fundprovider.GeographicalCountryDetail;
import com.capitaworld.service.loans.domain.fundprovider.GeographicalStateDetail;
import com.capitaworld.service.loans.domain.fundprovider.LasParameter;
import com.capitaworld.service.loans.model.DataRequest;
import com.capitaworld.service.loans.model.retail.LasParameterRequest;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalCityRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalCountryRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalStateRepository;
import com.capitaworld.service.loans.repository.fundprovider.LasParameterRepository;
import com.capitaworld.service.loans.service.fundprovider.LasLoanParameterService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.model.OneFormResponse;
@Transactional
@Service
public class LasLoanParameterServiceImpl implements LasLoanParameterService {

	private static final Logger logger = LoggerFactory.getLogger(LasLoanParameterServiceImpl.class);

	private static final String ERROR_WHILE_GET_LAS_PARAMETER_REQUEST_MSG = "error while getLasParameterRequest : ";

	@Autowired
	private LasParameterRepository lasParameterRepository;
	
	@Autowired 
	private GeographicalCountryRepository geographicalCountryRepository;
	
	@Autowired
	private GeographicalStateRepository geographicalStateRepository;
	
	@Autowired
	private GeographicalCityRepository geographicalCityRepository;
 	
	@Autowired
	private OneFormClient oneFormClient;
	
	@Override
	public boolean saveOrUpdate(LasParameterRequest lasParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveOrUpdate");

		LasParameter lasParameter= null;

		lasParameter = lasParameterRepository.findOne(lasParameterRequest.getId());
		if (lasParameter == null) {
			return false;
		}
		BeanUtils.copyProperties(lasParameterRequest, lasParameter, CommonUtils.IgnorableCopy.getFpProduct());
		lasParameter.setModifiedBy(lasParameterRequest.getUserId());
		lasParameter.setModifiedDate(new Date());
		lasParameterRepository.save(lasParameter);
		
		geographicalCountryRepository.inActiveMappingByFpProductId(lasParameterRequest.getId());
		//country data save
		saveCountry(lasParameterRequest);
		//state data save
		geographicalStateRepository.inActiveMappingByFpProductId(lasParameterRequest.getId());
		saveState(lasParameterRequest);
		//city data save
		geographicalCityRepository.inActiveMappingByFpProductId(lasParameterRequest.getId());
		saveCity(lasParameterRequest);
		CommonDocumentUtils.endHook(logger, "saveOrUpdate");
		return true;
	}

	@Override
	public LasParameterRequest getLasParameterRequest(Long id) {
		CommonDocumentUtils.startHook(logger, "getLasParameterRequest");

		LasParameterRequest lasParameterRequest= new LasParameterRequest();
		LasParameter lasParameter = lasParameterRepository.getByID(id);
		if(lasParameter==null)
			return null;
		BeanUtils.copyProperties(lasParameter, lasParameterRequest);
		
		List<Long> countryList=geographicalCountryRepository.getCountryByFpProductId(lasParameterRequest.getId());
		if(!countryList.isEmpty())
		{
		try {
			OneFormResponse formResponse = oneFormClient.getCountryByCountryListId(countryList);
			lasParameterRequest.setCountryList((List<DataRequest>) formResponse.getListData());
			 
			
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GET_LAS_PARAMETER_REQUEST_MSG,e);
		}
		}
		
		
		List<Long> stateList=geographicalStateRepository.getStateByFpProductId(lasParameterRequest.getId());
		if(!stateList.isEmpty())
		{
		try {
			OneFormResponse formResponse = oneFormClient.getStateByStateListId(stateList);
			lasParameterRequest.setStateList((List<DataRequest>) formResponse.getListData());
			 
			
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GET_LAS_PARAMETER_REQUEST_MSG,e);
		}
		}
		
		
		List<Long> cityList=geographicalCityRepository.getCityByFpProductId(lasParameterRequest.getId());
		if(!cityList.isEmpty())
		{
		try {
			OneFormResponse formResponse = oneFormClient.getCityByCityListId(cityList);
			lasParameterRequest.setCityList((List<DataRequest>) formResponse.getListData());
			 
			
		} catch (Exception e) {
			logger.error(ERROR_WHILE_GET_LAS_PARAMETER_REQUEST_MSG,e);
		}
		}
		CommonDocumentUtils.endHook(logger, "getLasParameterRequest");
		return lasParameterRequest;
	}
private void saveCountry(LasParameterRequest lasParameterRequest) {
	CommonDocumentUtils.startHook(logger, "saveCountry");
		GeographicalCountryDetail geographicalCountryDetail= null;
		for (DataRequest dataRequest : lasParameterRequest.getCountryList()) {
			geographicalCountryDetail = new GeographicalCountryDetail();
			geographicalCountryDetail.setCountryId(dataRequest.getId());
			geographicalCountryDetail.setFpProductMaster(lasParameterRequest.getId());
			geographicalCountryDetail.setCreatedBy(lasParameterRequest.getUserId());
			geographicalCountryDetail.setModifiedBy(lasParameterRequest.getUserId());
			geographicalCountryDetail.setCreatedDate(new Date());
			geographicalCountryDetail.setModifiedDate(new Date());
			geographicalCountryDetail.setIsActive(true);
			// create by and update
			geographicalCountryRepository.save(geographicalCountryDetail);
		}
		CommonDocumentUtils.endHook(logger, "saveCountry");
	}
	
	private void saveState(LasParameterRequest lasParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveState");
		GeographicalStateDetail geographicalStateDetail= null;
		for (DataRequest dataRequest : lasParameterRequest.getStateList()) {
			geographicalStateDetail = new GeographicalStateDetail();
			geographicalStateDetail.setStateId(dataRequest.getId());
			geographicalStateDetail.setFpProductMaster(lasParameterRequest.getId());
			geographicalStateDetail.setCreatedBy(lasParameterRequest.getUserId());
			geographicalStateDetail.setModifiedBy(lasParameterRequest.getUserId());
			geographicalStateDetail.setCreatedDate(new Date());
			geographicalStateDetail.setModifiedDate(new Date());
			geographicalStateDetail.setIsActive(true);
			// create by and update
			geographicalStateRepository.save(geographicalStateDetail);
		}
		CommonDocumentUtils.endHook(logger, "saveState");
	}
	
	private void saveCity(LasParameterRequest lasParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveCity");
		
		GeographicalCityDetail geographicalCityDetail= null;
		for (DataRequest dataRequest : lasParameterRequest.getCityList()) {
			geographicalCityDetail = new GeographicalCityDetail();
			geographicalCityDetail.setCityId(dataRequest.getId());
			geographicalCityDetail.setFpProductMaster(lasParameterRequest.getId());
			geographicalCityDetail.setCreatedBy(lasParameterRequest.getUserId());
			geographicalCityDetail.setModifiedBy(lasParameterRequest.getUserId());
			geographicalCityDetail.setCreatedDate(new Date());
			geographicalCityDetail.setModifiedDate(new Date());
			geographicalCityDetail.setIsActive(true);
			// create by and update
			geographicalCityRepository.save(geographicalCityDetail);
		}
		CommonDocumentUtils.endHook(logger, "saveCity");
	}
	
	
}
