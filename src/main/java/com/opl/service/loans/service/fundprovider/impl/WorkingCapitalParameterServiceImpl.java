package com.opl.service.loans.service.fundprovider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.DataRequest;
import com.opl.mudra.api.loans.model.corporate.WorkingCapitalParameterRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.oneform.model.OneFormResponse;
import com.opl.mudra.api.workflow.model.WorkflowJobsTrackerRequest;
import com.opl.mudra.api.workflow.model.WorkflowResponse;
import com.opl.mudra.api.workflow.utils.WorkflowUtils;
import com.opl.mudra.client.oneform.OneFormClient;
import com.opl.mudra.client.workflow.WorkflowClient;
import com.opl.service.loans.domain.IndustrySectorDetail;
import com.opl.service.loans.domain.IndustrySectorDetailTemp;
import com.opl.service.loans.domain.fundprovider.ConstitutionMapping;
import com.opl.service.loans.domain.fundprovider.ConstitutionMappingTemp;
import com.opl.service.loans.domain.fundprovider.FpGstTypeMapping;
import com.opl.service.loans.domain.fundprovider.GeographicalCityDetail;
import com.opl.service.loans.domain.fundprovider.GeographicalCityDetailTemp;
import com.opl.service.loans.domain.fundprovider.GeographicalCountryDetail;
import com.opl.service.loans.domain.fundprovider.GeographicalCountryDetailTemp;
import com.opl.service.loans.domain.fundprovider.GeographicalStateDetail;
import com.opl.service.loans.domain.fundprovider.GeographicalStateDetailTemp;
import com.opl.service.loans.domain.fundprovider.LoanArrangementMapping;
import com.opl.service.loans.domain.fundprovider.LoanArrangementMappingTemp;
import com.opl.service.loans.domain.fundprovider.NegativeIndustry;
import com.opl.service.loans.domain.fundprovider.NegativeIndustryTemp;
import com.opl.service.loans.domain.fundprovider.WorkingCapitalParameter;
import com.opl.service.loans.domain.fundprovider.WorkingCapitalParameterTemp;
import com.opl.service.loans.repository.fundprovider.FpConstitutionMappingRepository;
import com.opl.service.loans.repository.fundprovider.FpConstitutionMappingTempRepository;
import com.opl.service.loans.repository.fundprovider.FpGstTypeMappingRepository;
import com.opl.service.loans.repository.fundprovider.FpGstTypeMappingTempRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalCityRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalCityTempRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalCountryRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalCountryTempRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalStateRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalStateTempRepository;
import com.opl.service.loans.repository.fundprovider.LoanArrangementMappingRepository;
import com.opl.service.loans.repository.fundprovider.LoanArrangementMappingTempRepository;
import com.opl.service.loans.repository.fundprovider.NegativeIndustryRepository;
import com.opl.service.loans.repository.fundprovider.NegativeIndustryTempRepository;
import com.opl.service.loans.repository.fundprovider.WorkingCapitalParameterRepository;
import com.opl.service.loans.repository.fundprovider.WorkingCapitalParameterTempRepository;
import com.opl.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.opl.service.loans.repository.fundseeker.corporate.IndustrySectorTempRepository;
import com.opl.service.loans.service.fundprovider.FPParameterMappingService;
import com.opl.service.loans.service.fundprovider.MsmeValueMappingService;
import com.opl.service.loans.service.fundprovider.WorkingCapitalParameterService;
import com.opl.service.loans.utils.CommonDocumentUtils;

@Service
@Transactional
public class WorkingCapitalParameterServiceImpl implements WorkingCapitalParameterService {
	private static final Logger logger = LoggerFactory.getLogger(WorkingCapitalParameterServiceImpl.class);
	@Autowired
	private WorkingCapitalParameterRepository workingCapitalParameterRepository;

	@Autowired
	private IndustrySectorRepository industrySectorRepository;

	@Autowired
	private GeographicalCountryRepository geographicalCountryRepository;

	@Autowired
	private GeographicalStateRepository geographicalStateRepository;

	@Autowired
	private GeographicalCityRepository geographicalCityRepository;

	@Autowired
	private IndustrySectorTempRepository industrySectorTempRepository;

	@Autowired
	private GeographicalCountryTempRepository geographicalCountryTempRepository;

	@Autowired
	private GeographicalStateTempRepository geographicalStateTempRepository;

	@Autowired
	private GeographicalCityTempRepository geographicalCityTempRepository;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private NegativeIndustryRepository negativeIndustryRepository;

	@Autowired
	private WorkingCapitalParameterTempRepository workingCapitalParameterTempRepository;

	@Autowired
	private NegativeIndustryTempRepository negativeIndustryTempRepository;
	
	@Autowired
	private LoanArrangementMappingRepository loanArrangementMappingRepository;
	
	@Autowired
	private LoanArrangementMappingTempRepository loanArrangementMappingTempRepository;

	@Autowired
	private WorkflowClient workflowClient;

	@Autowired
	private MsmeValueMappingService msmeValueMappingService;
	
    @Autowired
    private  FpGstTypeMappingRepository fpGstTypeMappingRepository;
    
    @Autowired
    private FpGstTypeMappingTempRepository fpGstTypeMappingTempRepository;
    
    @Autowired
	private FpConstitutionMappingRepository fpConstitutionMappingRepository;
	
	@Autowired
	private FpConstitutionMappingTempRepository fpConstitutionMappingTempRepository;
	
	@Autowired
	private FPParameterMappingService fPParameterMappingService;


	@Override
	public boolean saveOrUpdate(WorkingCapitalParameterRequest workingCapitalParameterRequest,Long mappingId) {
		logger.info("start saveOrUpdate");
		
		WorkingCapitalParameterTemp loanParameter = workingCapitalParameterTempRepository
				.getworkingCapitalParameterTempByFpProductId(mappingId);
		
		
		WorkingCapitalParameter workingCapitalParameter = null;

		if(loanParameter.getFpProductMappingId()!=null)
		{
		workingCapitalParameter = workingCapitalParameterRepository.findOne(loanParameter.getFpProductMappingId());
		}
		if (workingCapitalParameter == null) {
			workingCapitalParameter=new WorkingCapitalParameter();
			
		}
		
		
		loanParameter.setStatusId(CommonUtils.Status.APPROVED); 
		loanParameter.setIsDeleted(false);
		loanParameter.setIsEdit(false);
		loanParameter.setIsCopied(true);
		loanParameter.setIsApproved(true);
		loanParameter.setApprovalDate(new Date());
		loanParameter.setFpProductMappingId(workingCapitalParameter.getId());
		workingCapitalParameterTempRepository.save(loanParameter);

		if (!CommonUtils.isObjectListNull(workingCapitalParameterRequest.getMaxTenure()))
			workingCapitalParameterRequest.setMaxTenure(workingCapitalParameterRequest.getMaxTenure() * 12);
		if (!CommonUtils.isObjectListNull(workingCapitalParameterRequest.getMinTenure()))
			workingCapitalParameterRequest.setMinTenure(workingCapitalParameterRequest.getMinTenure() * 12);

		BeanUtils.copyProperties(workingCapitalParameterRequest, workingCapitalParameter,"id");
		
		workingCapitalParameter.setUserId(workingCapitalParameterRequest.getUserId()!=null?workingCapitalParameterRequest.getUserId():null);
		workingCapitalParameter.setProductId(workingCapitalParameterRequest.getProductId()!=null?workingCapitalParameterRequest.getProductId():null);
		
		workingCapitalParameter.setModifiedBy(workingCapitalParameterRequest.getUserId());
		workingCapitalParameter.setIsActive(true);
		workingCapitalParameter.setModifiedDate(new Date());
		workingCapitalParameter.setIsParameterFilled(true);
		workingCapitalParameter.setJobId(workingCapitalParameterRequest.getJobId());
		WorkingCapitalParameter workingCapitalParameter2=workingCapitalParameterRepository.save(workingCapitalParameter);
		workingCapitalParameterRequest.setId(workingCapitalParameter2.getId());
		industrySectorRepository.inActiveMappingByFpProductId(workingCapitalParameterRequest.getId());
		// industry data save
		saveIndustry(workingCapitalParameterRequest);
		// Sector data save
		saveSector(workingCapitalParameterRequest);
		geographicalCountryRepository.inActiveMappingByFpProductId(workingCapitalParameterRequest.getId());
		// country data save
		saveCountry(workingCapitalParameterRequest);
		// state data save
		geographicalStateRepository.inActiveMappingByFpProductId(workingCapitalParameterRequest.getId());
		saveState(workingCapitalParameterRequest);
		// city data save
		geographicalCityRepository.inActiveMappingByFpProductId(workingCapitalParameterRequest.getId());
		saveCity(workingCapitalParameterRequest);
		// negative industry save
		negativeIndustryRepository.inActiveMappingByFpProductMasterId(workingCapitalParameterRequest.getId());
		saveNegativeIndustry(workingCapitalParameterRequest);
		
		//loan arrangements
		loanArrangementMappingRepository.inActiveMasterByFpProductId(workingCapitalParameterRequest.getId());
		saveLoanArrangements(workingCapitalParameterRequest);
		

		//save constitution mapping
		fpConstitutionMappingRepository.inActiveMasterByFpProductId(workingCapitalParameterRequest.getId());
		saveConstitutionType(workingCapitalParameterRequest);
		
		//gst type 
		fpGstTypeMappingRepository.inActiveMasterByFpProductId(workingCapitalParameterRequest.getId());
		saveLoanGstType(workingCapitalParameterRequest);
		
		fPParameterMappingService.inactiveAndSave(workingCapitalParameter2.getId(),CommonUtils.ParameterTypes.BUREAU_SCORE, workingCapitalParameterRequest.getBureauScoreIds());
		fPParameterMappingService.inactiveAndSave(workingCapitalParameter2.getId(),CommonUtils.ParameterTypes.BUREAU_SCORE_MAIN_DIR, workingCapitalParameterRequest.getMainDirBureauScoreIds());
		fPParameterMappingService.inactiveAndSave(workingCapitalParameter2.getId(),CommonUtils.ParameterTypes.BANK_STATEMENT_OPTIONS, workingCapitalParameterRequest.getBankStatementOptions());
		fPParameterMappingService.inactiveAndSaveWithObject(workingCapitalParameter2.getId(), CommonUtils.ParameterTypes.RISK_BASE_LOAN_AMOUNT, workingCapitalParameterRequest.getRiskLoanAmountList());
		//Dhaval
		boolean isUpdate = msmeValueMappingService.updateMsmeValueMapping(false, mappingId,workingCapitalParameter2.getId());
		logger.info("updated = {}",isUpdate);

		logger.info("end saveOrUpdate");
		return true;
	}
	
	private void saveConstitutionType(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveConstitutionType");
		ConstitutionMapping constitutionMapping= null;
		for (Integer dataRequest : workingCapitalParameterRequest.getConstitutionIds()) {
			constitutionMapping = new ConstitutionMapping();
			constitutionMapping.setFpProductId(workingCapitalParameterRequest.getId());
			constitutionMapping.setConstitutionId(dataRequest);
			constitutionMapping.setCreatedBy(workingCapitalParameterRequest.getUserId());
			constitutionMapping.setModifiedBy(workingCapitalParameterRequest.getUserId());
			constitutionMapping.setCreatedDate(new Date());
			constitutionMapping.setModifiedDate(new Date());
			constitutionMapping.setIsActive(true);
			// create by and update
			fpConstitutionMappingRepository.save(constitutionMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveConstitutionType");
		
	}
	
	
	private void saveConstitutionTypeTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveConstitutionTypeTemp");
		ConstitutionMappingTemp constitutionMapping= null;
		for (Integer dataRequest : workingCapitalParameterRequest.getConstitutionIds()) {
			constitutionMapping = new ConstitutionMappingTemp();
			constitutionMapping.setFpProductId(workingCapitalParameterRequest.getId());
			constitutionMapping.setConstitutionId(dataRequest);
			constitutionMapping.setCreatedBy(workingCapitalParameterRequest.getUserId());
			constitutionMapping.setModifiedBy(workingCapitalParameterRequest.getUserId());
			constitutionMapping.setCreatedDate(new Date());
			constitutionMapping.setModifiedDate(new Date());
			constitutionMapping.setIsActive(true);
			// create by and update
			fpConstitutionMappingTempRepository.save(constitutionMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveConstitutionTypeTemp");
		
	}

	private void saveLoanGstType(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveGstTypeTemp");
		FpGstTypeMapping fpGstTypeMapping= null;
		for (Integer dataRequest : workingCapitalParameterRequest.getGstType()) {
			fpGstTypeMapping = new FpGstTypeMapping();
			fpGstTypeMapping.setFpProductId(workingCapitalParameterRequest.getId());
			fpGstTypeMapping.setGstTypeId(dataRequest);
			fpGstTypeMapping.setCreatedBy(workingCapitalParameterRequest.getUserId());
			fpGstTypeMapping.setModifiedBy(workingCapitalParameterRequest.getUserId());
			fpGstTypeMapping.setCreatedDate(new Date());
			fpGstTypeMapping.setModifiedDate(new Date());
			fpGstTypeMapping.setIsActive(true);
			// create by and update
			fpGstTypeMappingRepository.save(fpGstTypeMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveGstTypeTemp");
		
	}
	private void saveLoanArrangements(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveLoanArrangements");
		LoanArrangementMapping loanArrangementMapping= null;
		for (Integer dataRequest : workingCapitalParameterRequest.getLoanArrangementIds()) {
			loanArrangementMapping = new LoanArrangementMapping();
			loanArrangementMapping.setFpProductId(workingCapitalParameterRequest.getId());
			loanArrangementMapping.setLoanArrangementId(dataRequest);
			loanArrangementMapping.setCreatedBy(workingCapitalParameterRequest.getUserId());
			loanArrangementMapping.setModifiedBy(workingCapitalParameterRequest.getUserId());
			loanArrangementMapping.setCreatedDate(new Date());
			loanArrangementMapping.setModifiedDate(new Date());
			loanArrangementMapping.setIsActive(true);
			// create by and update
			loanArrangementMappingRepository.save(loanArrangementMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveLoanArrangements");
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public WorkingCapitalParameterRequest getWorkingCapitalParameter(Long id) {
		logger.info("start getWorkingCapitalParameter");
		WorkingCapitalParameterRequest workingCapitalParameterRequest = new WorkingCapitalParameterRequest();
		WorkingCapitalParameter loanParameter = workingCapitalParameterRepository.getByID(id);
		if (loanParameter == null)
			return null;
		BeanUtils.copyProperties(loanParameter, workingCapitalParameterRequest);

		if (!CommonUtils.isObjectListNull(workingCapitalParameterRequest.getMaxTenure()))
			workingCapitalParameterRequest.setMaxTenure(workingCapitalParameterRequest.getMaxTenure() / 12);
		if (!CommonUtils.isObjectListNull(workingCapitalParameterRequest.getMinTenure()))
			workingCapitalParameterRequest.setMinTenure(workingCapitalParameterRequest.getMinTenure() / 12);

		List<Long> industryList = industrySectorRepository
				.getIndustryByProductId(workingCapitalParameterRequest.getId());
		if (!industryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getIndustryById(industryList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				workingCapitalParameterRequest.setIndustrylist(dataRequests);
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> sectorList = industrySectorRepository.getSectorByProductId(workingCapitalParameterRequest.getId());
		if (!sectorList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getSectorById(sectorList);
				
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				workingCapitalParameterRequest.setSectorlist(dataRequests);

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> countryList = geographicalCountryRepository
				.getCountryByFpProductId(workingCapitalParameterRequest.getId());
		if (!countryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getCountryByCountryListId(countryList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				workingCapitalParameterRequest.setCountryList(dataRequests);

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> stateList = geographicalStateRepository
				.getStateByFpProductId(workingCapitalParameterRequest.getId());
		if (!stateList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getStateByStateListId(stateList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				workingCapitalParameterRequest.setStateList(dataRequests);

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> cityList = geographicalCityRepository.getCityByFpProductId(workingCapitalParameterRequest.getId());
		if (!cityList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getCityByCityListId(cityList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				workingCapitalParameterRequest.setCityList(dataRequests);

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> negativeIndustryList = negativeIndustryRepository
				.getIndustryByFpProductMasterId(workingCapitalParameterRequest.getId());
		if (!negativeIndustryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getIndustryById(negativeIndustryList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				workingCapitalParameterRequest
						.setUnInterestedIndustrylist(dataRequests);
			} catch (Exception e) {
				logger.error("error while getWCParameterRequest : ", e);
			}
		}
		workingCapitalParameterRequest.setMsmeFundingIds(msmeValueMappingService.getDataListFromFpProductId(2,id, workingCapitalParameterRequest.getUserId()));
		workingCapitalParameterRequest.setGstType(fpGstTypeMappingRepository.getIdsByFpProductId(workingCapitalParameterRequest.getId()));
		workingCapitalParameterRequest.setLoanArrangementIds(loanArrangementMappingRepository.getIdsByFpProductId(workingCapitalParameterRequest.getId()));
		workingCapitalParameterRequest.setConstitutionIds(fpConstitutionMappingRepository.getIdsByFpProductId(workingCapitalParameterRequest.getId()));
		workingCapitalParameterRequest.setBureauScoreIds(fPParameterMappingService.getParameters(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.BUREAU_SCORE));
		workingCapitalParameterRequest.setMainDirBureauScoreIds(fPParameterMappingService.getParameters(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.BUREAU_SCORE_MAIN_DIR));
		workingCapitalParameterRequest.setBankStatementOptions(fPParameterMappingService.getParameters(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.BANK_STATEMENT_OPTIONS));
		workingCapitalParameterRequest.setRiskLoanAmountList(fPParameterMappingService.getParametersWithObject(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.RISK_BASE_LOAN_AMOUNT));
		
		logger.info("end getWorkingCapitalParameter");
		return workingCapitalParameterRequest;
	}
	
	

	private void saveIndustry(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveIndustry");
		IndustrySectorDetail industrySectorDetail = null;
		logger.info(""+workingCapitalParameterRequest.getIndustrylist());
		for (DataRequest dataRequest : workingCapitalParameterRequest.getIndustrylist()) {
			industrySectorDetail = new IndustrySectorDetail();
			industrySectorDetail.setFpProductId(workingCapitalParameterRequest.getId());
			industrySectorDetail.setIndustryId(dataRequest.getId());
			industrySectorDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			industrySectorDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setModifiedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorRepository.save(industrySectorDetail);
		}
		logger.info("end saveIndustry");
	}

	private void saveSector(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveSector");
		IndustrySectorDetail industrySectorDetail = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getSectorlist()) {
			industrySectorDetail = new IndustrySectorDetail();
			industrySectorDetail.setFpProductId(workingCapitalParameterRequest.getId());
			industrySectorDetail.setSectorId(dataRequest.getId());
			industrySectorDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			industrySectorDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setModifiedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorRepository.save(industrySectorDetail);
		}
		logger.info("end saveSector");
	}

	private void saveCountry(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		logger.info("save saveCountry");
		GeographicalCountryDetail geographicalCountryDetail = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getCountryList()) {
			geographicalCountryDetail = new GeographicalCountryDetail();
			geographicalCountryDetail.setCountryId(dataRequest.getId());
			geographicalCountryDetail.setFpProductMaster(workingCapitalParameterRequest.getId());
			geographicalCountryDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			geographicalCountryDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			geographicalCountryDetail.setCreatedDate(new Date());
			geographicalCountryDetail.setModifiedDate(new Date());
			geographicalCountryDetail.setIsActive(true);
			// create by and update
			geographicalCountryRepository.save(geographicalCountryDetail);
		}
		logger.info("end saveCountry");
	}

	private void saveState(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveState");
		GeographicalStateDetail geographicalStateDetail = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getStateList()) {
			geographicalStateDetail = new GeographicalStateDetail();
			geographicalStateDetail.setStateId(dataRequest.getId());
			geographicalStateDetail.setFpProductMaster(workingCapitalParameterRequest.getId());
			geographicalStateDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			geographicalStateDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			geographicalStateDetail.setCreatedDate(new Date());
			geographicalStateDetail.setModifiedDate(new Date());
			geographicalStateDetail.setIsActive(true);
			// create by and update
			geographicalStateRepository.save(geographicalStateDetail);
		}
		logger.info("end saveState");
	}

	private void saveCity(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveCity");
		GeographicalCityDetail geographicalCityDetail = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getCityList()) {
			geographicalCityDetail = new GeographicalCityDetail();
			geographicalCityDetail.setCityId(dataRequest.getId());
			geographicalCityDetail.setFpProductMaster(workingCapitalParameterRequest.getId());
			geographicalCityDetail.setCreatedBy(workingCapitalParameterRequest.getUserId());
			geographicalCityDetail.setModifiedBy(workingCapitalParameterRequest.getUserId());
			geographicalCityDetail.setCreatedDate(new Date());
			geographicalCityDetail.setModifiedDate(new Date());
			geographicalCityDetail.setIsActive(true);
			// create by and update
			geographicalCityRepository.save(geographicalCityDetail);
		}
		logger.info("end saveCity");
	}

	@Override
	public List<WorkingCapitalParameterRequest> getWorkingCapitalParameterListByUserId(Long id) {
		return Collections.emptyList();
	}

	private void saveNegativeIndustry(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		CommonDocumentUtils.startHook(logger, "saveNegativeIndustry");
		NegativeIndustry negativeIndustry = null;
		for (DataRequest dataRequest : workingCapitalParameterRequest.getUnInterestedIndustrylist()) {
			negativeIndustry = new NegativeIndustry();
			negativeIndustry.setFpProductMasterId(workingCapitalParameterRequest.getId());
			negativeIndustry.setIndustryId(dataRequest.getId());
			negativeIndustry.setCreatedBy(workingCapitalParameterRequest.getUserId());
			negativeIndustry.setModifiedBy(workingCapitalParameterRequest.getUserId());
			negativeIndustry.setCreatedDate(new Date());
			negativeIndustry.setModifiedDate(new Date());
			negativeIndustry.setIsActive(true);
			// create by and update
			negativeIndustryRepository.save(negativeIndustry);
		}
		CommonDocumentUtils.endHook(logger, "saveNegativeIndustry");

	}

	public Boolean saveMasterFromTempWc(Long mappingId) throws LoansException {
		try {
			WorkingCapitalParameterRequest workingCapitalParameterRequest = getWorkingCapitalParameterTemp(mappingId,null,null);
			return saveOrUpdate(workingCapitalParameterRequest,mappingId);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public WorkingCapitalParameterRequest getWorkingCapitalParameterTemp(Long id,Long role,Long userId) {
		logger.info("start getWorkingCapitalParameterTemp");
		WorkingCapitalParameterRequest workingCapitalParameterRequest = new WorkingCapitalParameterRequest();
		WorkingCapitalParameterTemp loanParameter = workingCapitalParameterTempRepository
				.getworkingCapitalParameterTempByFpProductId(id);
		if (loanParameter == null)
			return null;
		BeanUtils.copyProperties(loanParameter, workingCapitalParameterRequest);

		if (!CommonUtils.isObjectListNull(workingCapitalParameterRequest.getMaxTenure()))
			workingCapitalParameterRequest.setMaxTenure(workingCapitalParameterRequest.getMaxTenure() / 12);
		if (!CommonUtils.isObjectListNull(workingCapitalParameterRequest.getMinTenure()))
			workingCapitalParameterRequest.setMinTenure(workingCapitalParameterRequest.getMinTenure() / 12);

		List<Long> industryList = industrySectorTempRepository
				.getIndustryByProductId(workingCapitalParameterRequest.getId());
		if (!industryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getIndustryById(industryList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				workingCapitalParameterRequest.setIndustrylist(dataRequests);
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> sectorList = industrySectorTempRepository
				.getSectorByProductId(workingCapitalParameterRequest.getId());
		if (!sectorList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getSectorById(sectorList);
				
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				workingCapitalParameterRequest.setSectorlist(dataRequests);

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> countryList = geographicalCountryTempRepository
				.getCountryByFpProductId(workingCapitalParameterRequest.getId());
		if (!countryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getCountryByCountryListId(countryList);
				
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				
				workingCapitalParameterRequest.setCountryList(dataRequests);

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> stateList = geographicalStateTempRepository
				.getStateByFpProductId(workingCapitalParameterRequest.getId());
		if (!stateList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getStateByStateListId(stateList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				
				workingCapitalParameterRequest.setStateList(dataRequests);

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> cityList = geographicalCityTempRepository
				.getCityByFpProductId(workingCapitalParameterRequest.getId());
		if (!cityList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getCityByCityListId(cityList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				
				workingCapitalParameterRequest.setCityList(dataRequests);

			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		}

		List<Long> negativeIndustryList = negativeIndustryTempRepository
				.getIndustryByFpProductMasterId(workingCapitalParameterRequest.getId());
		if (!negativeIndustryList.isEmpty()) {
			try {
				OneFormResponse formResponse = oneFormClient.getIndustryById(negativeIndustryList);
				List<DataRequest> dataRequests=new ArrayList<>(formResponse.getListData().size());
				for(Object object:formResponse.getListData())
				{
					DataRequest dataRequest=com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>)object, DataRequest.class);
					dataRequests.add(dataRequest);
				}
				
				workingCapitalParameterRequest
						.setUnInterestedIndustrylist(dataRequests);
			} catch (Exception e) {
				logger.error("error while getWCParameterRequest : ", e);
			}
		}
		workingCapitalParameterRequest.setJobId(loanParameter.getJobId());
		
		//set workflow buttons
		
		 if (!CommonUtils.isObjectNullOrEmpty(loanParameter.getJobId()) && !CommonUtils.isObjectNullOrEmpty(role)) {
             WorkflowResponse workflowResponse = workflowClient.getActiveStepForMaster(loanParameter.getJobId(),Arrays.asList(role), userId);
             if (!CommonUtils.isObjectNullOrEmpty(workflowResponse) && !CommonUtils.isObjectNullOrEmpty(workflowResponse.getData())) {
                 try {
                     WorkflowJobsTrackerRequest workflowJobsTrackerRequest = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) workflowResponse.getData(), WorkflowJobsTrackerRequest.class);
                     if (!CommonUtils.isObjectNullOrEmpty(workflowJobsTrackerRequest.getStep()) && !CommonUtils.isObjectNullOrEmpty(workflowJobsTrackerRequest.getStep().getStepActions())) {
                    	 workingCapitalParameterRequest.setWorkflowData(workflowJobsTrackerRequest.getStep().getStepActions());
                     } else {
                         logger.info("response from workflow NULL jobId = {} and roleId = {}", loanParameter.getJobId(), role);
                     }
                 } catch (IOException e) {
                     logger.error("Error While getting data from workflow {}", e);
                 }
             }
         } else {
             logger.info("you set jobId or list of roleId NULL for calling workflow");
         }

		workingCapitalParameterRequest.setMsmeFundingIds(msmeValueMappingService.getDataListFromFpProductId(1,id, userId));
		workingCapitalParameterRequest.setLoanArrangementIds(loanArrangementMappingTempRepository.getIdsByFpProductId(workingCapitalParameterRequest.getId()));
		workingCapitalParameterRequest.setGstType(fpGstTypeMappingTempRepository.getIdsByFpProductId(workingCapitalParameterRequest.getId()));
		workingCapitalParameterRequest.setConstitutionIds(fpConstitutionMappingTempRepository.getIdsByFpProductId(workingCapitalParameterRequest.getId()));
		workingCapitalParameterRequest.setBureauScoreIds(fPParameterMappingService.getParametersTemp(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.BUREAU_SCORE));
		workingCapitalParameterRequest.setMainDirBureauScoreIds(fPParameterMappingService.getParametersTemp(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.BUREAU_SCORE_MAIN_DIR));
		workingCapitalParameterRequest.setBankStatementOptions(fPParameterMappingService.getParametersTemp(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.BANK_STATEMENT_OPTIONS));
		workingCapitalParameterRequest.setRiskLoanAmountList(fPParameterMappingService.getParametersTempWithObject(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.RISK_BASE_LOAN_AMOUNT));
		logger.info("end getWorkingCapitalParameterTemp");
		return workingCapitalParameterRequest;
	}

	@Override
	public Boolean saveOrUpdateTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveOrUpdateTemp");
		WorkingCapitalParameterTemp workingCapitalParameter = null;

		if(workingCapitalParameterRequest.getAppstage() == 1){
			workingCapitalParameter = workingCapitalParameterTempRepository.findOne(workingCapitalParameterRequest.getId());
		}else{
			workingCapitalParameter = workingCapitalParameterTempRepository.getworkingCapitalParameterTempByFpProductMappingId(workingCapitalParameterRequest.getId());
		}
		
		if (workingCapitalParameter == null) {
			workingCapitalParameter=new WorkingCapitalParameterTemp();
		}

		if (!CommonUtils.isObjectListNull(workingCapitalParameterRequest.getMaxTenure())){
			workingCapitalParameterRequest.setMaxTenure(workingCapitalParameterRequest.getMaxTenure() * 12);			
		}
		
		if (!CommonUtils.isObjectListNull(workingCapitalParameterRequest.getMinTenure())){
			workingCapitalParameterRequest.setMinTenure(workingCapitalParameterRequest.getMinTenure() * 12);			
		}

		if(workingCapitalParameterRequest.getAppstage() != 1){
			workingCapitalParameter.setFpProductMappingId(workingCapitalParameterRequest.getId());
		}

		if(workingCapitalParameterRequest.getAppstage() == 1){
			BeanUtils.copyProperties(workingCapitalParameterRequest, workingCapitalParameter,"id");
		}else{
			BeanUtils.copyProperties(workingCapitalParameterRequest, workingCapitalParameter,"jobId","id");
		}
		
		workingCapitalParameter.setUserId(workingCapitalParameterRequest.getUserId()!=null?workingCapitalParameterRequest.getUserId():null);
		workingCapitalParameter.setProductId(workingCapitalParameterRequest.getProductId()!=null?workingCapitalParameterRequest.getProductId():null);
		workingCapitalParameter.setModifiedBy(workingCapitalParameterRequest.getUserId());
		workingCapitalParameter.setIsActive(true);
		workingCapitalParameter.setModifiedDate(new Date());
		workingCapitalParameter.setIsParameterFilled(true);
		workingCapitalParameter.setStatusId(CommonUtils.Status.OPEN);
		workingCapitalParameter.setIsApproved(false);
		workingCapitalParameter.setIsDeleted(false);
		workingCapitalParameter.setIsCopied(false);
		workingCapitalParameter.setApprovalDate(null);

		if (CommonUtils.isObjectNullOrEmpty(workingCapitalParameter.getJobId())) {
			WorkflowResponse workflowResponse = workflowClient.createJobForMasters(
					WorkflowUtils.Workflow.MASTER_DATA_APPROVAL_PROCESS, WorkflowUtils.Action.SEND_FOR_APPROVAL,
					workingCapitalParameterRequest.getUserId());
			Long jobId = null;
			if (!CommonUtils.isObjectNullOrEmpty(workflowResponse.getData())) {
				jobId = Long.valueOf(workflowResponse.getData().toString());
			}

			workingCapitalParameter.setJobId(jobId);
		}
		
		workingCapitalParameter = workingCapitalParameterTempRepository.save(workingCapitalParameter);
		workingCapitalParameterRequest.setId(workingCapitalParameter.getId());
		industrySectorTempRepository.inActiveMappingByFpProductId(workingCapitalParameter.getId());
		// industry data save
		saveIndustryTemp(workingCapitalParameterRequest);
		// Sector data save
		saveSectorTemp(workingCapitalParameterRequest);
		geographicalCountryTempRepository.inActiveMappingByFpProductId(workingCapitalParameter.getId());
		// country data save
		saveCountryTemp(workingCapitalParameterRequest);
		// state data save
		geographicalStateTempRepository.inActiveMappingByFpProductId(workingCapitalParameter.getId());
		saveStateTemp(workingCapitalParameterRequest);
		// city data save
		geographicalCityTempRepository.inActiveMappingByFpProductId(workingCapitalParameter.getId());
		saveCityTemp(workingCapitalParameterRequest);
		// negative industry save
		negativeIndustryTempRepository.inActiveMappingByFpProductMasterId(workingCapitalParameter.getId());
		saveNegativeIndustryTemp(workingCapitalParameterRequest);
		fPParameterMappingService.inactiveAndSaveTemp(workingCapitalParameterRequest.getId(),CommonUtils.ParameterTypes.BUREAU_SCORE, workingCapitalParameterRequest.getBureauScoreIds());
		fPParameterMappingService.inactiveAndSaveTemp(workingCapitalParameterRequest.getId(),CommonUtils.ParameterTypes.BUREAU_SCORE_MAIN_DIR, workingCapitalParameterRequest.getMainDirBureauScoreIds());
		fPParameterMappingService.inactiveAndSaveTempWithObject(workingCapitalParameterRequest.getId(), CommonUtils.ParameterTypes.RISK_BASE_LOAN_AMOUNT, workingCapitalParameterRequest.getRiskLoanAmountList());
		

		//save constitution mapping
		fpConstitutionMappingTempRepository.inActiveMasterByFpProductId(workingCapitalParameter.getId());
		saveConstitutionTypeTemp(workingCapitalParameterRequest);
		//Dhaval
		boolean isUpdate = msmeValueMappingService.updateMsmeValueMappingTemp(workingCapitalParameterRequest.getMsmeFundingIds(),workingCapitalParameterRequest.getId(), workingCapitalParameterRequest.getUserId());
		
		//loan arrangements
				loanArrangementMappingTempRepository.inActiveMasterByFpProductId(workingCapitalParameterRequest.getId());
				saveLoanArrangementsTemp(workingCapitalParameterRequest);
		logger.info("updated = {}",isUpdate);
		logger.info("end saveOrUpdateTemp");
		return true;
	}

	private void saveLoanArrangementsTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		// TODO Auto-generated method stub
		
		CommonDocumentUtils.startHook(logger, "saveLoanArrangementsTemp");
		LoanArrangementMappingTemp loanArrangementMapping= null;
		for (Integer dataRequest : workingCapitalParameterRequest.getLoanArrangementIds()) {
			loanArrangementMapping = new LoanArrangementMappingTemp();
			loanArrangementMapping.setFpProductId(workingCapitalParameterRequest.getId());
			loanArrangementMapping.setLoanArrangementId(dataRequest);
			loanArrangementMapping.setCreatedBy(workingCapitalParameterRequest.getUserId());
			loanArrangementMapping.setModifiedBy(workingCapitalParameterRequest.getUserId());
			loanArrangementMapping.setCreatedDate(new Date());
			loanArrangementMapping.setModifiedDate(new Date());
			loanArrangementMapping.setIsActive(true);
			// create by and update
			loanArrangementMappingTempRepository.save(loanArrangementMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveLoanArrangementsTemp");
		
	}

	private void saveIndustryTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
		logger.info("start saveIndustryTemp");
		IndustrySectorDetailTemp industrySectorDetail = null;
		logger.info(""+workingCapitalParameterRequest.getIndustrylist());
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

	private void saveSectorTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
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

	private void saveCountryTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
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

	private void saveStateTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
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

	private void saveCityTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
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

	private void saveNegativeIndustryTemp(WorkingCapitalParameterRequest workingCapitalParameterRequest) {
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
}
