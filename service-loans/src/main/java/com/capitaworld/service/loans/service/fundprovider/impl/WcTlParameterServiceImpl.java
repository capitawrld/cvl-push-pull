package com.capitaworld.service.loans.service.fundprovider.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.capitaworld.service.loans.exceptions.LoansException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.api.workflow.model.WorkflowJobsTrackerRequest;
import com.capitaworld.api.workflow.model.WorkflowResponse;
import com.capitaworld.api.workflow.utility.MultipleJSONObjectHelper;
import com.capitaworld.api.workflow.utility.WorkflowUtils;
import com.capitaworld.client.workflow.WorkflowClient;
import com.capitaworld.service.loans.domain.IndustrySectorDetail;
import com.capitaworld.service.loans.domain.IndustrySectorDetailTemp;
import com.capitaworld.service.loans.domain.fundprovider.CoLendingRatio;
import com.capitaworld.service.loans.domain.fundprovider.FpGstTypeMapping;
import com.capitaworld.service.loans.domain.fundprovider.FpGstTypeMappingTemp;
import com.capitaworld.service.loans.domain.fundprovider.GeographicalCityDetail;
import com.capitaworld.service.loans.domain.fundprovider.GeographicalCityDetailTemp;
import com.capitaworld.service.loans.domain.fundprovider.GeographicalCountryDetail;
import com.capitaworld.service.loans.domain.fundprovider.GeographicalCountryDetailTemp;
import com.capitaworld.service.loans.domain.fundprovider.GeographicalStateDetail;
import com.capitaworld.service.loans.domain.fundprovider.GeographicalStateDetailTemp;
import com.capitaworld.service.loans.domain.fundprovider.LoanArrangementMapping;
import com.capitaworld.service.loans.domain.fundprovider.LoanArrangementMappingTemp;
import com.capitaworld.service.loans.domain.fundprovider.NbfcRatioMapping;
import com.capitaworld.service.loans.domain.fundprovider.NbfcRatioMappingTemp;
import com.capitaworld.service.loans.domain.fundprovider.NegativeIndustry;
import com.capitaworld.service.loans.domain.fundprovider.NegativeIndustryTemp;
import com.capitaworld.service.loans.domain.fundprovider.WcTlParameter;
import com.capitaworld.service.loans.domain.fundprovider.WcTlParameterTemp;
import com.capitaworld.service.loans.model.DataRequest;
import com.capitaworld.service.loans.model.corporate.TermLoanParameterRequest;
import com.capitaworld.service.loans.model.corporate.WcTlParameterRequest;
import com.capitaworld.service.loans.repository.fundprovider.CoLendingRatioRepository;
import com.capitaworld.service.loans.repository.fundprovider.FpGstTypeMappingRepository;
import com.capitaworld.service.loans.repository.fundprovider.FpGstTypeMappingTempRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalCityRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalCityTempRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalCountryRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalCountryTempRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalStateRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalStateTempRepository;
import com.capitaworld.service.loans.repository.fundprovider.LoanArrangementMappingRepository;
import com.capitaworld.service.loans.repository.fundprovider.LoanArrangementMappingTempRepository;
import com.capitaworld.service.loans.repository.fundprovider.NbfcRatioMappingRepository;
import com.capitaworld.service.loans.repository.fundprovider.NbfcRatioMappingTempRepository;
import com.capitaworld.service.loans.repository.fundprovider.NegativeIndustryRepository;
import com.capitaworld.service.loans.repository.fundprovider.NegativeIndustryTempRepository;
import com.capitaworld.service.loans.repository.fundprovider.WcTlLoanParameterRepository;
import com.capitaworld.service.loans.repository.fundprovider.WcTlParameterTempRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.IndustrySectorTempRepository;
import com.capitaworld.service.loans.service.fundprovider.MsmeValueMappingService;
import com.capitaworld.service.loans.service.fundprovider.WcTlParameterService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.model.OneFormResponse;

@Service
@Transactional
public class WcTlParameterServiceImpl implements WcTlParameterService {

	private static final Logger logger = LoggerFactory.getLogger(WcTlParameterServiceImpl.class);

	private static final String ERROR_WHILE_GET_TERM_LOAN_PARAMETER_REQUEST_MSG = "error while getTermLoanParameterRequest : ";
	private static final String ERROR_WHILE_GET_WCTL_REQUEST_TEMP_MSG = "error while getWcTlRequestTemp : ";

	@Autowired
	private WcTlLoanParameterRepository wcTlLoanParameterRepository;

	@Autowired
	private IndustrySectorRepository industrySectorRepository;

	@Autowired
	private GeographicalCountryRepository geographicalCountryRepository;

	@Autowired
	private GeographicalStateRepository geographicalStateRepository;

	@Autowired
	private GeographicalCityRepository geographicalCityRepository;

	@Autowired
	private NegativeIndustryRepository negativeIndustryRepository;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private IndustrySectorTempRepository industrySectorTempRepository;

	@Autowired
	private GeographicalCountryTempRepository geographicalCountryTempRepository;

	@Autowired
	private GeographicalStateTempRepository geographicalStateTempRepository;

	@Autowired
	private GeographicalCityTempRepository geographicalCityTempRepository;

	@Autowired
	private NegativeIndustryTempRepository negativeIndustryTempRepository;

	@Autowired
	private WcTlParameterTempRepository wcTlParameterTempRepository;

	@Autowired
	private WorkflowClient workflowClient;

	@Autowired
	private MsmeValueMappingService msmeValueMappingService;

	@Autowired
	private LoanArrangementMappingRepository loanArrangementMappingRepository;

	@Autowired
	private LoanArrangementMappingTempRepository loanArrangementMappingTempRepository;

	@Autowired
	private FpGstTypeMappingRepository fpGstTypeMappingRepository;

	@Autowired
	private FpGstTypeMappingTempRepository fpGstTypeMappingTempRepository;
	
	@Autowired
	private CoLendingRatioRepository coLendingRatioRepository;
	
	@Autowired
	private NbfcRatioMappingTempRepository nbfcRatioMappingTempRepository; 
	
	@Autowired
	private NbfcRatioMappingRepository nbfcRatioMappingRepository; 


	@Override
	public boolean saveOrUpdate(WcTlParameterRequest wcTlParameterRequest, Long mappingId) {
		CommonDocumentUtils.startHook(logger, "saveOrUpdate");

		WcTlParameterTemp loanParameter = wcTlParameterTempRepository.getWcTlParameterTempByFpProductId(mappingId);

		WcTlParameter WcTlParameter = null;

		if (loanParameter.getFpProductMappingId() != null) {
			WcTlParameter = wcTlLoanParameterRepository.findOne(loanParameter.getFpProductMappingId());
		}
		if (WcTlParameter == null) {
			WcTlParameter = new WcTlParameter();

		}

		loanParameter.setStatusId(CommonUtils.Status.APPROVED);
		loanParameter.setIsDeleted(false);
		loanParameter.setIsEdit(false);
		loanParameter.setIsCopied(true);
		loanParameter.setIsApproved(true);
		loanParameter.setApprovalDate(new Date());
		wcTlParameterTempRepository.save(loanParameter);

		if (!CommonUtils.isObjectListNull(wcTlParameterRequest.getMaxTenure()))
			wcTlParameterRequest.setMaxTenure(wcTlParameterRequest.getMaxTenure().multiply(new BigDecimal("12")));
		if (!CommonUtils.isObjectListNull(wcTlParameterRequest.getMinTenure()))
			wcTlParameterRequest.setMinTenure(wcTlParameterRequest.getMinTenure().multiply(new BigDecimal("12")));

		BeanUtils.copyProperties(wcTlParameterRequest, WcTlParameter, "id");
		WcTlParameter.setUserId(wcTlParameterRequest.getUserId() != null ? wcTlParameterRequest.getUserId() : null);
		WcTlParameter
				.setProductId(wcTlParameterRequest.getProductId() != null ? wcTlParameterRequest.getProductId() : null);
		WcTlParameter.setModifiedBy(wcTlParameterRequest.getUserId());
		WcTlParameter.setModifiedDate(new Date());
		WcTlParameter.setIsActive(true);
		WcTlParameter.setIsParameterFilled(true);
		WcTlParameter.setJobId(wcTlParameterRequest.getJobId());
		WcTlParameter wcTlParameter2 = wcTlLoanParameterRepository.save(WcTlParameter);
		wcTlParameterRequest.setId(wcTlParameter2.getId());
		industrySectorRepository.inActiveMappingByFpProductId(wcTlParameterRequest.getId());
		// industry data save
		saveIndustry(wcTlParameterRequest);
		// Sector data save
		saveSector(wcTlParameterRequest);
		geographicalCountryRepository.inActiveMappingByFpProductId(wcTlParameterRequest.getId());
		// country data save
		saveCountry(wcTlParameterRequest);
		// state data save
		geographicalStateRepository.inActiveMappingByFpProductId(wcTlParameterRequest.getId());
		saveState(wcTlParameterRequest);
		// city data save
		geographicalCityRepository.inActiveMappingByFpProductId(wcTlParameterRequest.getId());
		saveCity(wcTlParameterRequest);
		// negative industry save
		negativeIndustryRepository.inActiveMappingByFpProductMasterId(wcTlParameterRequest.getId());
		saveNegativeIndustry(wcTlParameterRequest);

		// loan arrangements
		loanArrangementMappingRepository.inActiveMasterByFpProductId(wcTlParameterRequest.getId());
		saveLoanArrangements(wcTlParameterRequest);

		// gst type
		fpGstTypeMappingRepository.inActiveMasterByFpProductId(wcTlParameterRequest.getId());
		saveLoanGstType(wcTlParameterRequest);
		
		//save nbfc ratio mapping
		nbfcRatioMappingRepository.inActiveByFpProductId(wcTlParameterRequest.getId());
		saveNbfcRatioMapping(wcTlParameterRequest);

		// Ravina
		boolean isUpdate = msmeValueMappingService.updateMsmeValueMapping(false, mappingId, wcTlParameter2.getId());
		logger.info("updated = {}", isUpdate);
		CommonDocumentUtils.endHook(logger, "saveOrUpdate");
		return true;

	}

	@Override
	public WcTlParameterRequest getWcTlRequest(Long id,Long role) {
		CommonDocumentUtils.startHook(logger, "getTermLoanParameterRequest");
		WcTlParameterRequest wcTlParameterRequest = new WcTlParameterRequest();
		WcTlParameter loanParameter = wcTlLoanParameterRepository.getById(id);
		if (loanParameter == null)
			return null;
		BeanUtils.copyProperties(loanParameter, wcTlParameterRequest);

		if (!CommonUtils.isObjectListNull(wcTlParameterRequest.getMaxTenure()))
			wcTlParameterRequest.setMaxTenure(
					wcTlParameterRequest.getMaxTenure().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP));
		if (!CommonUtils.isObjectListNull(wcTlParameterRequest.getMinTenure()))
			wcTlParameterRequest.setMinTenure(
					wcTlParameterRequest.getMinTenure().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP));

		List<Long> industryList = industrySectorRepository.getIndustryByProductId(wcTlParameterRequest.getId());
		if (!industryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getIndustryById(industryList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				wcTlParameterRequest.setIndustrylist(dataRequests);
			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_TERM_LOAN_PARAMETER_REQUEST_MSG, e);
			}
		}

		List<Long> sectorList = industrySectorRepository.getSectorByProductId(wcTlParameterRequest.getId());
		if (!sectorList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getSectorById(sectorList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}

				wcTlParameterRequest.setSectorlist(dataRequests);

			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_TERM_LOAN_PARAMETER_REQUEST_MSG, e);
			}
		}

		List<Long> countryList = geographicalCountryRepository.getCountryByFpProductId(wcTlParameterRequest.getId());
		if (!countryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getCountryByCountryListId(countryList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}

				wcTlParameterRequest.setCountryList(dataRequests);

			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_TERM_LOAN_PARAMETER_REQUEST_MSG, e);
			}
		}

		List<Long> stateList = geographicalStateRepository.getStateByFpProductId(wcTlParameterRequest.getId());
		if (!stateList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getStateByStateListId(stateList);

				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}

				wcTlParameterRequest.setStateList(dataRequests);

			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_TERM_LOAN_PARAMETER_REQUEST_MSG, e);
			}
		}

		List<Long> cityList = geographicalCityRepository.getCityByFpProductId(wcTlParameterRequest.getId());
		if (!cityList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getCityByCityListId(cityList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				wcTlParameterRequest.setCityList(dataRequests);

			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_TERM_LOAN_PARAMETER_REQUEST_MSG, e);
			}
		}

		List<Long> negativeIndustryList = negativeIndustryRepository
				.getIndustryByFpProductMasterId(wcTlParameterRequest.getId());
		if (!negativeIndustryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getIndustryById(negativeIndustryList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				wcTlParameterRequest.setNegativeIndustryList(dataRequests);
			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_TERM_LOAN_PARAMETER_REQUEST_MSG, e);
			}
		}
		List<DataRequest>  ratioMasterList;
		List<CoLendingRatio> listAllActiveByOrgkId;
		if(!CommonUtils.isObjectNullOrEmpty(role))
		{
		if(role.equals(WorkflowUtils.Role.NBFC_CHECKER) || role.equals(WorkflowUtils.Role.NBFC_MAKER))
		{
			listAllActiveByOrgkId =coLendingRatioRepository.listAllActiveProposalByOrgId(loanParameter.getUserOrgId());
			ratioMasterList=new ArrayList<>(listAllActiveByOrgkId.size());
			
		}
		else
		{
			listAllActiveByOrgkId = coLendingRatioRepository.listAllActiveByBankId(loanParameter.getUserOrgId());
			ratioMasterList=new ArrayList<>(listAllActiveByOrgkId.size());
		}
		
		for(CoLendingRatio coLendingRatio:listAllActiveByOrgkId)
		{
			DataRequest dataRequest=new DataRequest();
			dataRequest.setId(coLendingRatio.getId());
			dataRequest.setValue(coLendingRatio.getName());
			dataRequest.setTenure(coLendingRatio.getBankRatio());
			ratioMasterList.add(dataRequest);
		}
		
		wcTlParameterRequest.setNbfcRatioMasterList(ratioMasterList);
		}

		wcTlParameterRequest.setMsmeFundingIds(
				msmeValueMappingService.getDataListFromFpProductId(2, id, wcTlParameterRequest.getUserId()));
		wcTlParameterRequest.setGstType(fpGstTypeMappingRepository.getIdsByFpProductId(wcTlParameterRequest.getId()));
		wcTlParameterRequest.setLoanArrangementIds(
				loanArrangementMappingRepository.getIdsByFpProductId(wcTlParameterRequest.getId()));
		wcTlParameterRequest.setGstType(fpGstTypeMappingRepository.getIdsByFpProductId(wcTlParameterRequest.getId()));
		
		wcTlParameterRequest.setNbfcRatioIds(nbfcRatioMappingRepository.getIdsByFpProductId(wcTlParameterRequest.getId()));
		CommonDocumentUtils.endHook(logger, "getTermLoanParameterRequest");
		return wcTlParameterRequest;
	}

	private void saveIndustry(WcTlParameterRequest wcTlParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveIndustry");
		IndustrySectorDetail industrySectorDetail = null;
		for (DataRequest dataRequest : wcTlParameterRequest.getIndustrylist()) {
			industrySectorDetail = new IndustrySectorDetail();
			industrySectorDetail.setFpProductId(wcTlParameterRequest.getId());
			industrySectorDetail.setIndustryId(dataRequest.getId());
			industrySectorDetail.setCreatedBy(wcTlParameterRequest.getUserId());
			industrySectorDetail.setModifiedBy(wcTlParameterRequest.getUserId());
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setModifiedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorRepository.save(industrySectorDetail);
		}
		CommonDocumentUtils.endHook(logger, "saveIndustry");
	}

	private void saveSector(WcTlParameterRequest wcTlParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveSector");
		IndustrySectorDetail industrySectorDetail = null;
		for (DataRequest dataRequest : wcTlParameterRequest.getSectorlist()) {
			industrySectorDetail = new IndustrySectorDetail();
			industrySectorDetail.setFpProductId(wcTlParameterRequest.getId());
			industrySectorDetail.setSectorId(dataRequest.getId());
			industrySectorDetail.setCreatedBy(wcTlParameterRequest.getUserId());
			industrySectorDetail.setModifiedBy(wcTlParameterRequest.getUserId());
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setModifiedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorRepository.save(industrySectorDetail);
		}
		CommonDocumentUtils.endHook(logger, "saveSector");
	}

	private void saveCountry(WcTlParameterRequest wcTlParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveCountry");

		GeographicalCountryDetail geographicalCountryDetail = null;
		for (DataRequest dataRequest : wcTlParameterRequest.getCountryList()) {
			geographicalCountryDetail = new GeographicalCountryDetail();
			geographicalCountryDetail.setCountryId(dataRequest.getId());
			geographicalCountryDetail.setFpProductMaster(wcTlParameterRequest.getId());
			geographicalCountryDetail.setCreatedBy(wcTlParameterRequest.getUserId());
			geographicalCountryDetail.setModifiedBy(wcTlParameterRequest.getUserId());
			geographicalCountryDetail.setCreatedDate(new Date());
			geographicalCountryDetail.setModifiedDate(new Date());
			geographicalCountryDetail.setIsActive(true);
			// create by and update
			geographicalCountryRepository.save(geographicalCountryDetail);
		}
		CommonDocumentUtils.endHook(logger, "saveCountry");
	}

	private void saveState(WcTlParameterRequest wcTlParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveState");
		GeographicalStateDetail geographicalStateDetail = null;
		for (DataRequest dataRequest : wcTlParameterRequest.getStateList()) {
			geographicalStateDetail = new GeographicalStateDetail();
			geographicalStateDetail.setStateId(dataRequest.getId());
			geographicalStateDetail.setFpProductMaster(wcTlParameterRequest.getId());
			geographicalStateDetail.setCreatedBy(wcTlParameterRequest.getUserId());
			geographicalStateDetail.setModifiedBy(wcTlParameterRequest.getUserId());
			geographicalStateDetail.setCreatedDate(new Date());
			geographicalStateDetail.setModifiedDate(new Date());
			geographicalStateDetail.setIsActive(true);
			// create by and update
			geographicalStateRepository.save(geographicalStateDetail);
		}
		CommonDocumentUtils.endHook(logger, "saveState");
	}

	@Async
	private void saveCity(WcTlParameterRequest wcTlParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveCity");
		GeographicalCityDetail geographicalCityDetail = null;
		for (DataRequest dataRequest : wcTlParameterRequest.getCityList()) {
			geographicalCityDetail = new GeographicalCityDetail();
			geographicalCityDetail.setCityId(dataRequest.getId());
			geographicalCityDetail.setFpProductMaster(wcTlParameterRequest.getId());
			geographicalCityDetail.setCreatedBy(wcTlParameterRequest.getUserId());
			geographicalCityDetail.setModifiedBy(wcTlParameterRequest.getUserId());
			geographicalCityDetail.setCreatedDate(new Date());
			geographicalCityDetail.setModifiedDate(new Date());
			geographicalCityDetail.setIsActive(true);
			// create by and update
			geographicalCityRepository.save(geographicalCityDetail);
		}
		CommonDocumentUtils.endHook(logger, "saveCity");
	}

	@Async
	private void saveNegativeIndustry(WcTlParameterRequest wcTlParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveNegativeIndustry");
		NegativeIndustry negativeIndustry = null;
		for (DataRequest dataRequest : wcTlParameterRequest.getNegativeIndustryList()) {
			negativeIndustry = new NegativeIndustry();
			negativeIndustry.setFpProductMasterId(wcTlParameterRequest.getId());
			negativeIndustry.setIndustryId(dataRequest.getId());
			negativeIndustry.setCreatedBy(wcTlParameterRequest.getUserId());
			negativeIndustry.setModifiedBy(wcTlParameterRequest.getUserId());
			negativeIndustry.setCreatedDate(new Date());
			negativeIndustry.setModifiedDate(new Date());
			negativeIndustry.setIsActive(true);
			// create by and update
			negativeIndustryRepository.save(negativeIndustry);
		}
		CommonDocumentUtils.endHook(logger, "saveNegativeIndustry");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.capitaworld.service.loans.service.fundprovider.WcTlParameterService#
	 * saveMasterFromTempWcTl(java.lang.Long)
	 */
	@Override
	public Boolean saveMasterFromTempWcTl(Long mappingId) throws LoansException {
		try {
			WcTlParameterRequest temp = getWcTlRequestTemp(mappingId, null, null);

			return saveOrUpdate(temp, mappingId);

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION, e);
			return false;
		}
	}

	@Override
	public WcTlParameterRequest getWcTlRequestTemp(Long id, Long role, Long userId) {
		CommonDocumentUtils.startHook(logger, "getWcTlRequestTemp");

		WcTlParameterRequest wcTlParameterRequest = new WcTlParameterRequest();
		WcTlParameterTemp loanParameter = wcTlParameterTempRepository.getWcTlParameterTempByFpProductId(id);
		if (loanParameter == null)
			return null;
		BeanUtils.copyProperties(loanParameter, wcTlParameterRequest);

		if (!CommonUtils.isObjectListNull(wcTlParameterRequest.getMaxTenure()))
			wcTlParameterRequest.setMaxTenure(
					wcTlParameterRequest.getMaxTenure().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP));
		if (!CommonUtils.isObjectListNull(wcTlParameterRequest.getMinTenure()))
			wcTlParameterRequest.setMinTenure(
					wcTlParameterRequest.getMinTenure().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP));

		List<Long> industryList = industrySectorTempRepository.getIndustryByProductId(wcTlParameterRequest.getId());
		if (!industryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getIndustryById(industryList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				wcTlParameterRequest.setIndustrylist(dataRequests);
			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_WCTL_REQUEST_TEMP_MSG, e);
			}
		}

		List<Long> sectorList = industrySectorTempRepository.getSectorByProductId(wcTlParameterRequest.getId());
		if (!sectorList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getSectorById(sectorList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				wcTlParameterRequest.setSectorlist(dataRequests);

			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_WCTL_REQUEST_TEMP_MSG, e);
			}
		}

		List<Long> countryList = geographicalCountryTempRepository
				.getCountryByFpProductId(wcTlParameterRequest.getId());
		if (!countryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getCountryByCountryListId(countryList);

				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				wcTlParameterRequest.setCountryList(dataRequests);

			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_WCTL_REQUEST_TEMP_MSG, e);
			}
		}

		List<Long> stateList = geographicalStateTempRepository.getStateByFpProductId(wcTlParameterRequest.getId());
		if (!stateList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getStateByStateListId(stateList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				wcTlParameterRequest.setStateList(dataRequests);

			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_WCTL_REQUEST_TEMP_MSG, e);
			}
		}

		List<Long> cityList = geographicalCityTempRepository.getCityByFpProductId(wcTlParameterRequest.getId());
		if (!cityList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getCityByCityListId(cityList);
				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}

				wcTlParameterRequest.setCityList(dataRequests);

			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_WCTL_REQUEST_TEMP_MSG, e);
			}
		}

		List<Long> negativeIndustryList = negativeIndustryTempRepository
				.getIndustryByFpProductMasterId(wcTlParameterRequest.getId());
		if (!negativeIndustryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getIndustryById(negativeIndustryList);

				List<DataRequest> dataRequests = new ArrayList<>(formResponse.getListData().size());
				for (Object object : formResponse.getListData()) {
					DataRequest dataRequest = com.capitaworld.service.loans.utils.MultipleJSONObjectHelper
							.getObjectFromMap((LinkedHashMap<String, Object>) object, DataRequest.class);
					dataRequests.add(dataRequest);
				}

				wcTlParameterRequest.setNegativeIndustryList(dataRequests);
			} catch (Exception e) {
				logger.error(ERROR_WHILE_GET_WCTL_REQUEST_TEMP_MSG, e);
			}
		}
		
		List<DataRequest>  ratioMasterList;
		List<CoLendingRatio> listAllActiveByOrgkId;
		//add nbfc ratio list
		if(!CommonUtils.isObjectNullOrEmpty(role))
		{
		if(role.equals(WorkflowUtils.Role.NBFC_CHECKER) || role.equals(WorkflowUtils.Role.NBFC_MAKER))
		{
			listAllActiveByOrgkId =coLendingRatioRepository.listAllActiveProposalByOrgId(loanParameter.getUserOrgId());
			ratioMasterList=new ArrayList<>(listAllActiveByOrgkId.size());
			
		}
		else
		{
			listAllActiveByOrgkId = coLendingRatioRepository.listAllActiveByBankId(loanParameter.getUserOrgId());
			ratioMasterList=new ArrayList<>(listAllActiveByOrgkId.size());
		}
		
		for(CoLendingRatio coLendingRatio:listAllActiveByOrgkId)
		{
			DataRequest dataRequest=new DataRequest();
			dataRequest.setId(coLendingRatio.getId());
			dataRequest.setValue(coLendingRatio.getName());
			dataRequest.setTenure(coLendingRatio.getBankRatio());
			ratioMasterList.add(dataRequest);
		}
		
		wcTlParameterRequest.setNbfcRatioMasterList(ratioMasterList);
		}
		
		
		wcTlParameterRequest.setJobId(loanParameter.getJobId());
		// set workflow buttons

		if (!CommonUtils.isObjectNullOrEmpty(loanParameter.getJobId()) && !CommonUtils.isObjectNullOrEmpty(role)) {
			WorkflowResponse workflowResponse = workflowClient.getActiveStepForMaster(loanParameter.getJobId(),
					Arrays.asList(role), userId);
			if (!CommonUtils.isObjectNullOrEmpty(workflowResponse)
					&& !CommonUtils.isObjectNullOrEmpty(workflowResponse.getData())) {
				try {
					WorkflowJobsTrackerRequest workflowJobsTrackerRequest = MultipleJSONObjectHelper.getObjectFromMap(
							(LinkedHashMap<String, Object>) workflowResponse.getData(),
							WorkflowJobsTrackerRequest.class);
					if (!CommonUtils.isObjectNullOrEmpty(workflowJobsTrackerRequest.getStep()) && !CommonUtils
							.isObjectNullOrEmpty(workflowJobsTrackerRequest.getStep().getStepActions())) {
						wcTlParameterRequest.setWorkflowData(workflowJobsTrackerRequest.getStep().getStepActions());
					} else {
						logger.info("response from workflow NULL jobId = {} and roleId = {}", loanParameter.getJobId(),
								role);
					}
				} catch (IOException e) {
					logger.error("Error While getting data from workflow {}", e);
				}
			}
		} else {
			logger.info("you set jobId or list of roleId NULL for calling workflow");
		}
		wcTlParameterRequest.setMsmeFundingIds(msmeValueMappingService.getDataListFromFpProductId(1, id, userId));
		wcTlParameterRequest.setLoanArrangementIds(
				loanArrangementMappingTempRepository.getIdsByFpProductId(wcTlParameterRequest.getId()));
		wcTlParameterRequest.setGstType(fpGstTypeMappingTempRepository.getIdsByFpProductId(wcTlParameterRequest.getId()));
		wcTlParameterRequest.setNbfcRatioIds(nbfcRatioMappingTempRepository.getTempIdsByFpProductId(wcTlParameterRequest.getId()));
		CommonDocumentUtils.endHook(logger, "getWcTlRequestTemp");
		return wcTlParameterRequest;
	}

	@Override
	public Boolean saveOrUpdateTemp(WcTlParameterRequest wcTlParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveOrUpdateTemp");

		WcTlParameterTemp WcTlParameter = null;

		if (wcTlParameterRequest.getAppstage() == 1) {
			WcTlParameter = wcTlParameterTempRepository.findOne(wcTlParameterRequest.getId());
		} else {

			WcTlParameter = wcTlParameterTempRepository
					.getWcTlParameterTempByFpProductMappingId(wcTlParameterRequest.getId());

		}

		if (WcTlParameter == null) {
			WcTlParameter = new WcTlParameterTemp();
			WcTlParameter.setFpProductMappingId(wcTlParameterRequest.getId());
		}

		if (!CommonUtils.isObjectListNull(wcTlParameterRequest.getMaxTenure()))
			wcTlParameterRequest.setMaxTenure(wcTlParameterRequest.getMaxTenure().multiply(new BigDecimal("12")));
		if (!CommonUtils.isObjectListNull(wcTlParameterRequest.getMinTenure()))
			wcTlParameterRequest.setMinTenure(wcTlParameterRequest.getMinTenure().multiply(new BigDecimal("12")));

		if (wcTlParameterRequest.getAppstage() != 1) {
			WcTlParameter.setFpProductMappingId(wcTlParameterRequest.getId());
		}
		BeanUtils.copyProperties(wcTlParameterRequest, WcTlParameter, "id");
		WcTlParameter.setUserId(wcTlParameterRequest.getUserId() != null ? wcTlParameterRequest.getUserId() : null);
		WcTlParameter
				.setProductId(wcTlParameterRequest.getProductId() != null ? wcTlParameterRequest.getProductId() : null);
		WcTlParameter.setModifiedBy(wcTlParameterRequest.getUserId());
		WcTlParameter.setModifiedDate(new Date());
		WcTlParameter.setIsActive(true);
		WcTlParameter.setIsParameterFilled(true);
		WcTlParameter.setStatusId(CommonUtils.Status.OPEN);
		WcTlParameter.setIsApproved(false);
		WcTlParameter.setIsDeleted(false);
		WcTlParameter.setIsCopied(false);
		WcTlParameter.setApprovalDate(null);

		if (CommonUtils.isObjectNullOrEmpty(WcTlParameter.getJobId())) {
			WorkflowResponse workflowResponse = workflowClient.createJobForMasters(
					WorkflowUtils.Workflow.MASTER_DATA_APPROVAL_PROCESS, WorkflowUtils.Action.SEND_FOR_APPROVAL,
					wcTlParameterRequest.getUserId());
			Long jobId = null;
			if (!CommonUtils.isObjectNullOrEmpty(workflowResponse.getData())) {
				jobId = Long.valueOf(workflowResponse.getData().toString());
			}

			WcTlParameter.setJobId(jobId);
		}

		WcTlParameter = wcTlParameterTempRepository.save(WcTlParameter);
		wcTlParameterRequest.setId(WcTlParameter.getId());
		industrySectorTempRepository.inActiveMappingByFpProductId(WcTlParameter.getId());
		// industry data save
		saveIndustryTemp(wcTlParameterRequest);
		// Sector data save
		saveSectorTemp(wcTlParameterRequest);
		geographicalCountryTempRepository.inActiveMappingByFpProductId(WcTlParameter.getId());
		// country data save
		saveCountryTemp(wcTlParameterRequest);
		// state data save
		geographicalStateTempRepository.inActiveMappingByFpProductId(WcTlParameter.getId());
		saveStateTemp(wcTlParameterRequest);
		// city data save
		geographicalCityTempRepository.inActiveMappingByFpProductId(WcTlParameter.getId());
		saveCityTemp(wcTlParameterRequest);
		// negative industry save
		negativeIndustryTempRepository.inActiveMappingByFpProductMasterId(WcTlParameter.getId());
		saveNegativeIndustryTemp(wcTlParameterRequest);
		
		//save nbfc ratio mapping
		nbfcRatioMappingTempRepository.inActiveTempByFpProductId(WcTlParameter.getId());
		saveNbfcRatioMappingTemp(wcTlParameterRequest);

		// loan arrangements
		loanArrangementMappingTempRepository.inActiveMasterByFpProductId(wcTlParameterRequest.getId());
		saveLoanArrangementsTemp(wcTlParameterRequest);

		//save nbfc ratio mapping
		nbfcRatioMappingTempRepository.inActiveTempByFpProductId(wcTlParameterRequest.getId());
		saveNbfcRatioMappingTemp(wcTlParameterRequest);
		
		// vinita
		boolean isUpdate = msmeValueMappingService.updateMsmeValueMappingTemp(wcTlParameterRequest.getMsmeFundingIds(),
				wcTlParameterRequest.getId(), wcTlParameterRequest.getUserId());
		logger.info("updated = {}", isUpdate);
		CommonDocumentUtils.endHook(logger, "saveOrUpdateTemp");
		return true;

	}
	
	private void saveNbfcRatioMappingTemp(WcTlParameterRequest wcparam) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveNbfcRatioMappingTemp");
		NbfcRatioMappingTemp nbfc= null;
		for (Long dataRequest : wcparam.getNbfcRatioIds()) {
			nbfc = new NbfcRatioMappingTemp();
			nbfc.setFpProductId(wcparam.getId());
			nbfc.setRatioId(dataRequest);
			
			nbfc.setCreatedDate(new Date());
			nbfc.setModifiedDate(new Date());
			nbfc.setIsActive(true);
			// create by and update
			nbfcRatioMappingTempRepository.save(nbfc);
		}
		CommonDocumentUtils.endHook(logger, "saveNbfcRatioMappingTemp");
		
	}

	private void saveIndustryTemp(WcTlParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveIndustryTemp");
		IndustrySectorDetailTemp industrySectorDetail = null;
		logger.info("" + workingCapitalParameterRequest.getIndustrylist());
		for (DataRequest dataRequest : workingCapitalParameterRequest.getIndustrylist()) {
			industrySectorDetail = new IndustrySectorDetailTemp();
			industrySectorDetail.setFpProductId(workingCapitalParameterRequest.getId());
			industrySectorDetail.setIndustryId(dataRequest.getId());
			industrySectorDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			industrySectorDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setModifiedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorTempRepository.save(industrySectorDetail);
		}
		logger.info("end saveIndustryTemp");
	}
	
	private void saveNbfcRatioMapping(WcTlParameterRequest wcTlParameterRequest) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveNbfcRatioMapping");
		NbfcRatioMapping nbfc= null;
		for (Long dataRequest : wcTlParameterRequest.getNbfcRatioIds()) {
			nbfc = new NbfcRatioMapping();
			nbfc.setFpProductId(wcTlParameterRequest.getId());
			nbfc.setRatioId(dataRequest);
			
			nbfc.setCreatedDate(new Date());
			nbfc.setModifiedDate(new Date());
			nbfc.setIsActive(true);
			// create by and update
			nbfcRatioMappingRepository.save(nbfc);
		}
		CommonDocumentUtils.endHook(logger, "saveNbfcRatioMapping");
		
	}

	private void saveSectorTemp(WcTlParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveSectorTemp");
		IndustrySectorDetailTemp industrySectorDetail = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getSectorlist()) {
			industrySectorDetail = new IndustrySectorDetailTemp();
			industrySectorDetail.setFpProductId(workingCapitalParameterRequest.getId());
			industrySectorDetail.setSectorId(dataRequest.getId());
			industrySectorDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			industrySectorDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setModifiedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorTempRepository.save(industrySectorDetail);
		}
		logger.info("end saveSectorTemp");
	}

	private void saveCountryTemp(WcTlParameterRequest workingCapitalParameterRequest) {
		logger.info("save saveCountryTemp");
		GeographicalCountryDetailTemp geographicalCountryDetail = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getCountryList()) {
			geographicalCountryDetail = new GeographicalCountryDetailTemp();
			geographicalCountryDetail.setCountryId(dataRequest.getId());
			geographicalCountryDetail.setFpProductMaster(workingCapitalParameterRequest.getId());
			geographicalCountryDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			geographicalCountryDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			geographicalCountryDetail.setCreatedDate(new Date());
			geographicalCountryDetail.setModifiedDate(new Date());
			geographicalCountryDetail.setIsActive(true);
			// create by and update
			geographicalCountryTempRepository.save(geographicalCountryDetail);
		}
		logger.info("end saveCountryTemp");
	}

	private void saveStateTemp(WcTlParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveStateTemp");
		GeographicalStateDetailTemp geographicalStateDetail = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getStateList()) {
			geographicalStateDetail = new GeographicalStateDetailTemp();
			geographicalStateDetail.setStateId(dataRequest.getId());
			geographicalStateDetail.setFpProductMaster(workingCapitalParameterRequest.getId());
			geographicalStateDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			geographicalStateDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			geographicalStateDetail.setCreatedDate(new Date());
			geographicalStateDetail.setModifiedDate(new Date());
			geographicalStateDetail.setIsActive(true);
			// create by and update
			geographicalStateTempRepository.save(geographicalStateDetail);
		}
		logger.info("end saveStateTemp");
	}

	@Async
	private void saveCityTemp(WcTlParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveCityTemp");
		GeographicalCityDetailTemp geographicalCityDetail = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getCityList()) {
			geographicalCityDetail = new GeographicalCityDetailTemp();
			geographicalCityDetail.setCityId(dataRequest.getId());
			geographicalCityDetail.setFpProductMaster(workingCapitalParameterRequest.getId());
			geographicalCityDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			geographicalCityDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			geographicalCityDetail.setCreatedDate(new Date());
			geographicalCityDetail.setModifiedDate(new Date());
			geographicalCityDetail.setIsActive(true);
			// create by and update
			geographicalCityTempRepository.save(geographicalCityDetail);
		}
		logger.info("end saveCityTemp");
	}

	private void saveNegativeIndustryTemp(WcTlParameterRequest workingCapitalParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveNegativeIndustryTemp");
		NegativeIndustryTemp negativeIndustry = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getUnInterestedIndustrylist()) {
			negativeIndustry = new NegativeIndustryTemp();
			negativeIndustry.setFpProductMasterId(workingCapitalParameterRequest.getId());
			negativeIndustry.setIndustryId(dataRequest.getId());
			negativeIndustry.setCreatedBy(workingCapitalParameterRequest.getUserId());
			negativeIndustry.setModifiedBy(workingCapitalParameterRequest.getUserId());
			negativeIndustry.setCreatedDate(new Date());
			negativeIndustry.setModifiedDate(new Date());
			negativeIndustry.setIsActive(true);
			// create by and update
			negativeIndustryTempRepository.save(negativeIndustry);
		}
		CommonDocumentUtils.endHook(logger, "saveNegativeIndustryTemp");

	}

	private void saveLoanArrangements(WcTlParameterRequest wcTlParameterRequest) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveLoanArrangements");
		LoanArrangementMapping loanArrangementMapping = null;
		for (Integer dataRequest : wcTlParameterRequest.getLoanArrangementIds()) {
			loanArrangementMapping = new LoanArrangementMapping();
			loanArrangementMapping.setFpProductId(wcTlParameterRequest.getId());
			loanArrangementMapping.setLoanArrangementId(dataRequest);
			loanArrangementMapping.setCreatedBy(wcTlParameterRequest.getUserId());
			loanArrangementMapping.setModifiedBy(wcTlParameterRequest.getUserId());
			loanArrangementMapping.setCreatedDate(new Date());
			loanArrangementMapping.setModifiedDate(new Date());
			loanArrangementMapping.setIsActive(true);
			// create by and update
			loanArrangementMappingRepository.save(loanArrangementMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveLoanArrangements");

	}

	private void saveLoanArrangementsTemp(WcTlParameterRequest wcTlParameterRequest) {
		// TODO Auto-generated method stub

		CommonDocumentUtils.startHook(logger, "saveLoanArrangementsTemp");
		LoanArrangementMappingTemp loanArrangementMapping = null;
		for (Integer dataRequest : wcTlParameterRequest.getLoanArrangementIds()) {
			loanArrangementMapping = new LoanArrangementMappingTemp();
			loanArrangementMapping.setFpProductId(wcTlParameterRequest.getId());
			loanArrangementMapping.setLoanArrangementId(dataRequest);
			loanArrangementMapping.setCreatedBy(wcTlParameterRequest.getUserId());
			loanArrangementMapping.setModifiedBy(wcTlParameterRequest.getUserId());
			loanArrangementMapping.setCreatedDate(new Date());
			loanArrangementMapping.setModifiedDate(new Date());
			loanArrangementMapping.setIsActive(true);
			// create by and update
			loanArrangementMappingTempRepository.save(loanArrangementMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveLoanArrangementsTemp");

	}
	
	private void saveLoanGstType(WcTlParameterRequest wcTlParameterRequest) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveGstTypeTemp");
		FpGstTypeMapping fpGstTypeMapping= null;
		for (Integer dataRequest : wcTlParameterRequest.getGstType()) {
			fpGstTypeMapping = new FpGstTypeMapping();
			fpGstTypeMapping.setFpProductId(wcTlParameterRequest.getId());
			fpGstTypeMapping.setGstTypeId(dataRequest);
			fpGstTypeMapping.setCreatedBy(wcTlParameterRequest.getUserId());
			fpGstTypeMapping.setModifiedBy(wcTlParameterRequest.getUserId());
			fpGstTypeMapping.setCreatedDate(new Date());
			fpGstTypeMapping.setModifiedDate(new Date());
			fpGstTypeMapping.setIsActive(true);
			// create by and update
			fpGstTypeMappingRepository.save(fpGstTypeMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveGstTypeTemp");
		
	}
	
	private void saveLoanGstTypeTemp(WcTlParameterRequest wcTlParameterRequest) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveGstTypeTemp");
		FpGstTypeMappingTemp fpGstTypeMappingTemp= null;
		for (Integer dataRequest : wcTlParameterRequest.getGstType()) {
			fpGstTypeMappingTemp = new FpGstTypeMappingTemp();
			fpGstTypeMappingTemp.setFpProductId(wcTlParameterRequest.getId());
			fpGstTypeMappingTemp.setGstTypeId(dataRequest);
			fpGstTypeMappingTemp.setCreatedBy(wcTlParameterRequest.getUserId());
			fpGstTypeMappingTemp.setModifiedBy(wcTlParameterRequest.getUserId());
			fpGstTypeMappingTemp.setCreatedDate(new Date());
			fpGstTypeMappingTemp.setModifiedDate(new Date());
			fpGstTypeMappingTemp.setIsActive(true);
			// create by and update
			fpGstTypeMappingTempRepository.save(fpGstTypeMappingTemp);
		}
		CommonDocumentUtils.endHook(logger, "saveGstTypeTemp");
		
	}


}
