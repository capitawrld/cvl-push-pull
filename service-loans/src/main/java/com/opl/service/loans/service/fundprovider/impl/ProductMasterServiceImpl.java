
package com.opl.service.loans.service.fundprovider.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.BranchBasicDetailsRequest;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;
import com.opl.mudra.api.dms.exception.DocumentException;
import com.opl.mudra.api.dms.model.DocumentRequest;
import com.opl.mudra.api.dms.model.DocumentResponse;
import com.opl.mudra.api.dms.model.StorageDetailsResponse;
import com.opl.mudra.api.dms.utils.DocumentAlias;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.DataRequest;
import com.opl.mudra.api.loans.model.FpProductDetails;
import com.opl.mudra.api.loans.model.MultipleFpPruductRequest;
import com.opl.mudra.api.loans.model.ProductDetailsForSp;
import com.opl.mudra.api.loans.model.ProductDetailsResponse;
import com.opl.mudra.api.loans.model.ProductMasterRequest;
import com.opl.mudra.api.loans.model.WorkflowData;
import com.opl.mudra.api.loans.model.colending.FpProductRoiResponse;
import com.opl.mudra.api.loans.model.common.ChatDetails;
import com.opl.mudra.api.loans.model.corporate.AddProductRequest;
import com.opl.mudra.api.loans.model.corporate.CorporateProduct;
import com.opl.mudra.api.loans.model.corporate.TermLoanParameterRequest;
import com.opl.mudra.api.loans.model.corporate.UnsecuredLoanParameterRequest;
import com.opl.mudra.api.loans.model.corporate.WcTlParameterRequest;
import com.opl.mudra.api.loans.model.corporate.WorkingCapitalParameterRequest;
import com.opl.mudra.api.loans.model.teaser.primaryview.CommonRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.CommonUtils.LoanType;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.matchengine.exception.MatchException;
import com.opl.mudra.api.matchengine.model.ProposalMappingRequest;
import com.opl.mudra.api.oneform.model.MasterResponse;
import com.opl.mudra.api.oneform.model.OneFormResponse;
import com.opl.mudra.api.workflow.model.WorkflowRequest;
import com.opl.mudra.api.workflow.model.WorkflowResponse;
import com.opl.mudra.api.workflow.utils.WorkflowUtils;
import com.opl.mudra.client.dms.DMSClient;
import com.opl.mudra.client.matchengine.ProposalDetailsClient;
import com.opl.mudra.client.oneform.OneFormClient;
import com.opl.mudra.client.workflow.WorkflowClient;
import com.opl.service.loans.config.FPAsyncComponent;
import com.opl.service.loans.domain.IndustrySectorDetailTemp;
import com.opl.service.loans.domain.fundprovider.ConstitutionMappingTemp;
import com.opl.service.loans.domain.fundprovider.FpGstTypeMappingTemp;
import com.opl.service.loans.domain.fundprovider.GeographicalCityDetailTemp;
import com.opl.service.loans.domain.fundprovider.GeographicalCountryDetailTemp;
import com.opl.service.loans.domain.fundprovider.GeographicalStateDetailTemp;
import com.opl.service.loans.domain.fundprovider.MsmeValueMapping;
import com.opl.service.loans.domain.fundprovider.MsmeValueMappingTemp;
import com.opl.service.loans.domain.fundprovider.NegativeIndustryTemp;
import com.opl.service.loans.domain.fundprovider.ProductMaster;
import com.opl.service.loans.domain.fundprovider.ProductMasterTemp;
import com.opl.service.loans.domain.fundprovider.TermLoanParameterTemp;
import com.opl.service.loans.domain.fundprovider.WcTlParameterTemp;
import com.opl.service.loans.domain.fundprovider.WorkingCapitalParameterTemp;
import com.opl.service.loans.repository.common.CommonRepository;
import com.opl.service.loans.repository.common.LoanRepository;
import com.opl.service.loans.repository.fundprovider.FpConstitutionMappingTempRepository;
import com.opl.service.loans.repository.fundprovider.FpGstTypeMappingRepository;
import com.opl.service.loans.repository.fundprovider.FpGstTypeMappingTempRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalCityTempRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalCountryRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalCountryTempRepository;
import com.opl.service.loans.repository.fundprovider.GeographicalStateTempRepository;
import com.opl.service.loans.repository.fundprovider.MsmeValueMappingRepository;
import com.opl.service.loans.repository.fundprovider.MsmeValueMappingTempRepository;
import com.opl.service.loans.repository.fundprovider.NegativeIndustryTempRepository;
import com.opl.service.loans.repository.fundprovider.ProductMasterRepository;
import com.opl.service.loans.repository.fundprovider.ProductMasterTempRepository;
import com.opl.service.loans.repository.fundprovider.ProposalDetailsRepository;
import com.opl.service.loans.repository.fundprovider.TermLoanParameterRepository;
import com.opl.service.loans.repository.fundprovider.WorkingCapitalParameterRepository;
import com.opl.service.loans.repository.fundseeker.corporate.IndustrySectorTempRepository;
import com.opl.service.loans.service.common.FundProviderSequenceService;
import com.opl.service.loans.service.fundprovider.FPParameterMappingService;
import com.opl.service.loans.service.fundprovider.ProductMasterService;
import com.opl.service.loans.service.fundprovider.TermLoanParameterService;
import com.opl.service.loans.service.fundprovider.UnsecuredLoanParameterService;
import com.opl.service.loans.service.fundprovider.WcTlParameterService;
import com.opl.service.loans.service.fundprovider.WorkingCapitalParameterService;
import com.opl.service.loans.utils.CommonDocumentUtils;

@Service
@Transactional
@SuppressWarnings("unchecked")
public class ProductMasterServiceImpl implements ProductMasterService {
	private static final Logger logger = LoggerFactory.getLogger(ProductMasterServiceImpl.class);

	private static final String STATUS_LITERAL = "status";
	private static final String MESSAGE_LITERAL = "message";
	private static final String GET_USER_NAME_BY_APPLICATION_ID = "getUserNameByApplicationId";
	private static final String IS_PRODUCT_MATCHED = "isProductMatched";
	private static final String SAVE_CORPORATE = "saveCorporate";
	private static final String SAVE_CORPORATE_IN_TEMP = "saveCorporateInTemp";

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private UsersClient usersClient;

	@Autowired
	private ProductMasterRepository productMasterRepository;

	@Autowired
	private WorkingCapitalParameterRepository workingCapitalParameterRepository;

	@Autowired
	private TermLoanParameterRepository termLoanParameterRepository;

	@Autowired
	private FundProviderSequenceService fundProviderSequenceService;

	@Autowired
	private GeographicalCountryRepository geoCountry;

	@Autowired
	private WorkingCapitalParameterService workingCapitalParameterService;

	@Autowired
	private TermLoanParameterService termLoanParameterService;

	@Autowired
	private UnsecuredLoanParameterService unsecuredLoanParameterService;

	@Autowired
	private ProposalDetailsClient proposalDetailsClient;

	@Autowired
	private WcTlParameterService wcTlParameterService;
	
	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private ProductMasterTempRepository productMasterTempRepository;

	@Autowired
	private WorkflowClient workflowClient;
	
	private FPAsyncComponent fpAsyncComponent;
	
	@Lazy
	ProductMasterServiceImpl(FPAsyncComponent fpAsyncComponent){
        this.fpAsyncComponent=fpAsyncComponent;
    }
	
//	@Autowired
//	private FPAsyncComponent fpAsyncComponent;
	
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
    private MsmeValueMappingRepository masterRepository;

    @Autowired
    private MsmeValueMappingTempRepository tempRepository;

    @Autowired
    private ProposalDetailsRepository proposalDetailsRepository;
  
    @Autowired
    private  FpGstTypeMappingRepository fpGstTypeMappingRepository;
    
    @Autowired
    private FpGstTypeMappingTempRepository fpGstTypeMappingTempRepository;

	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private FpConstitutionMappingTempRepository fpConstitutionMappingTempRepository;
	
	@Autowired
	private FPParameterMappingService fPParameterMappingService;
	
	@Override
	public Boolean saveOrUpdate(AddProductRequest addProductRequest, Long userOrgId) {
		CommonDocumentUtils.startHook(logger, "saveOrUpdate");
		try {

			if (!CommonUtils.isObjectNullOrEmpty(addProductRequest.getProductMappingId())) {
				if (addProductRequest.getStage() == 2) {

					productMasterRepository.changeProductName(
							(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
									? addProductRequest.getUserId() : addProductRequest.getClientId()),
							addProductRequest.getProductMappingId(), addProductRequest.getName());
				} else {
					productMasterTempRepository.changeProductName(
							(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
									? addProductRequest.getUserId() : addProductRequest.getClientId()),
							addProductRequest.getProductMappingId(), addProductRequest.getName());
				}
				CommonDocumentUtils.endHook(logger, "saveOrUpdate");
				return true;
			} else {
				ProductMasterTemp productMasterTemp = null;
				LoanType loanType = LoanType.getById(Integer.parseInt(addProductRequest.getProductId().toString()));
				WorkflowResponse workflowResponse = workflowClient.createJobForMasters(
						WorkflowUtils.Workflow.MASTER_DATA_APPROVAL_PROCESS, WorkflowUtils.Action.SEND_FOR_APPROVAL,
						(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
								? addProductRequest.getUserId() : addProductRequest.getClientId()));
				Long jobId = null;

				switch (loanType) {
				case WORKING_CAPITAL:
					productMasterTemp = new WorkingCapitalParameterTemp();
					break;
				case TERM_LOAN:
					productMasterTemp = new TermLoanParameterTemp();
					break;
				case WCTL_LOAN:
					productMasterTemp = new WcTlParameterTemp();
					break;		

				default:
					break;
				}
				List<DataRequest> industrySecIdList = null,secIdList=null,geogaphicallyCountry=null,geogaphicallyState=null,geogaphicallyCity=null,negativeIndList=null;
				List<Integer> existingConstitutionIds = null;
				List<Integer> existingBureauIds = null;
				List<Integer> existingMainDirBureauIds = null;
				if(!CommonUtils.isObjectNullOrEmpty(addProductRequest.getLoanId()))
				{
					switch (loanType) {
					case WORKING_CAPITAL:
						WorkingCapitalParameterTemp workingCapitalParameterTemp = new WorkingCapitalParameterTemp();
						WorkingCapitalParameterRequest workingCapitalParameterRequest = workingCapitalParameterService.getWorkingCapitalParameter(addProductRequest.getLoanId());

						//set multiple value in temp
						industrySecIdList=workingCapitalParameterRequest.getIndustrylist();
						secIdList=workingCapitalParameterRequest.getSectorlist();
						geogaphicallyCountry=workingCapitalParameterRequest.getCountryList();
						geogaphicallyState=workingCapitalParameterRequest.getStateList();
						geogaphicallyCity=workingCapitalParameterRequest.getCityList();
						negativeIndList=workingCapitalParameterRequest.getUnInterestedIndustrylist();
						if(addProductRequest.getFinId()==null ||addProductRequest.getFinId()==4)
						{
							workingCapitalParameterRequest.setIsNewTolTnwCheck(false);
							workingCapitalParameterRequest.setNewTolTnw(null);
						}
						//END set multiple value in temp
						BeanUtils.copyProperties(workingCapitalParameterRequest, workingCapitalParameterTemp,"id");
						existingConstitutionIds = workingCapitalParameterRequest.getConstitutionIds();
						existingBureauIds = workingCapitalParameterRequest.getBureauScoreIds();
						existingMainDirBureauIds = workingCapitalParameterRequest.getMainDirBureauScoreIds();
						productMasterTemp = workingCapitalParameterTemp;
						productMasterTemp.setIsParameterFilled(true);
						break;
					case TERM_LOAN:
							TermLoanParameterTemp termLoanParameterTemp = new TermLoanParameterTemp();
							TermLoanParameterRequest termLoanParameterRequest = termLoanParameterService.getTermLoanParameterRequest(addProductRequest.getLoanId(),addProductRequest.getRoleId());

							//set multiple value in temp
							industrySecIdList=termLoanParameterRequest.getIndustrylist();
							secIdList=termLoanParameterRequest.getSectorlist();
							geogaphicallyCountry=termLoanParameterRequest.getCountryList();
							geogaphicallyState=termLoanParameterRequest.getStateList();
							geogaphicallyCity=termLoanParameterRequest.getCityList();
							negativeIndList=termLoanParameterRequest.getUnInterestedIndustrylist();
							//END set multiple value in temp
							if(addProductRequest.getFinId()==null || addProductRequest.getFinId()==4)
							{
								termLoanParameterRequest.setIsNewTolTnwCheck(false);
								termLoanParameterRequest.setNewTolTnw(null);
							}
							BeanUtils.copyProperties(termLoanParameterRequest, termLoanParameterTemp,"id");
							existingConstitutionIds = termLoanParameterRequest.getConstitutionIds();
							existingBureauIds = termLoanParameterRequest.getBureauScoreIds();
							existingMainDirBureauIds = termLoanParameterRequest.getMainDirBureauScoreIds();
							productMasterTemp = termLoanParameterTemp;
							productMasterTemp.setIsParameterFilled(true);
						break;
					case WCTL_LOAN:
						WcTlParameterTemp wcTlParameterTemp= new WcTlParameterTemp();
						WcTlParameterRequest wcTlParameterRequest = wcTlParameterService.getWcTlRequest(addProductRequest.getLoanId(),addProductRequest.getRoleId());

						//set multiple value in temp
						industrySecIdList=wcTlParameterRequest.getIndustrylist();
						secIdList=wcTlParameterRequest.getSectorlist();
						geogaphicallyCountry=wcTlParameterRequest.getCountryList();
						geogaphicallyState=wcTlParameterRequest.getStateList();
						geogaphicallyCity=wcTlParameterRequest.getCityList();
						negativeIndList=wcTlParameterRequest.getUnInterestedIndustrylist();
						//END set multiple value in temp
						if(addProductRequest.getFinId() == null || addProductRequest.getFinId() == 4)
						{
							wcTlParameterRequest.setIsNewTolTnwCheck(false);
							wcTlParameterRequest.setNewTolTnw(null);
						}
						BeanUtils.copyProperties(wcTlParameterRequest, wcTlParameterTemp,"id");
						productMasterTemp = wcTlParameterTemp;
						productMasterTemp.setIsParameterFilled(true);
						break;
					default:
						break;
					}
					
					
				}
				// productMaster.setJobId(null);
				if(workflowResponse != null){
					jobId = workflowResponse.getData() != null ? Long.valueOf(workflowResponse.getData().toString()) : null;
				}
				
				productMasterTemp.setJobId(jobId);

				productMasterTemp.setProductId(addProductRequest.getProductId());
				productMasterTemp.setIsMatched(false);
				productMasterTemp.setName(addProductRequest.getName());
				//productMasterTemp.setFpName(addProductRequest.getFpName());
				//get organisation name
				String orgName=proposalDetailsRepository.getOrgNameById(userOrgId);
				productMasterTemp.setFpName(CommonUtils.isObjectNullOrEmpty(orgName)?addProductRequest.getFpName():orgName);
				productMasterTemp.setUserId((CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
						? addProductRequest.getUserId() : addProductRequest.getClientId()));
				productMasterTemp.setCreatedBy((CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
						? addProductRequest.getUserId() : addProductRequest.getClientId()));
				productMasterTemp.setCreatedDate(new Date());
				productMasterTemp.setModifiedBy((CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
						? addProductRequest.getUserId() : addProductRequest.getClientId()));
				productMasterTemp.setIsParameterFilled(false);
				productMasterTemp.setModifiedDate(new Date());
				// set business type id
				productMasterTemp.setBusinessTypeId(addProductRequest.getBusinessTypeId());
				productMasterTemp.setWcRenewalStatus(addProductRequest.getWcRenewalStatus());
				productMasterTemp.setFinId(addProductRequest.getFinId());
//				productMasterTemp.setBankStatementOption(addProductRequest.getBankStatementOption());
				productMasterTemp.setIsCopied(false);
				productMasterTemp.setIsActive(true);
				productMasterTemp.setUserOrgId(userOrgId);
				productMasterTemp.setStatusId(1);
				productMasterTemp.setProductCode(
						fundProviderSequenceService.getFundProviderSequenceNumber(addProductRequest.getProductId()));
				productMasterTemp.setProductType(addProductRequest.getProductType());
				ProductMasterTemp productMaster2=productMasterTempRepository.save(productMasterTemp);
				
				//save gst type for only WC
				
				if(loanType==LoanType.WORKING_CAPITAL || loanType==LoanType.TERM_LOAN || loanType==LoanType.WCTL_LOAN)
				{
						// Copy Mapping Values
						if(!CommonUtils.isListNullOrEmpty(existingConstitutionIds)){
							saveConstitutionTypeTemp(existingConstitutionIds,productMaster2.getId(),productMaster2.getUserId());							
						}
						
						if(!CommonUtils.isListNullOrEmpty(existingBureauIds)){
							fPParameterMappingService.inactiveAndSaveTemp(productMaster2.getId(), CommonUtils.ParameterTypes.BUREAU_SCORE, existingBureauIds);
						}
						
						if(!CommonUtils.isListNullOrEmpty(existingMainDirBureauIds)){
							fPParameterMappingService.inactiveAndSaveTemp(productMaster2.getId(), CommonUtils.ParameterTypes.BUREAU_SCORE_MAIN_DIR, existingMainDirBureauIds);
						}
//					fPParameterMappingService.inactiveAndSaveTemp(productMaster2.getId(),CommonUtils.ParameterTypes.BANK_STATEMENT_OPTIONS, addProductRequest.getBankStatementOptions());
					
					fpGstTypeMappingTempRepository.inActiveMasterByFpProductId(productMaster2.getId());
					if(!CommonUtils.isListNullOrEmpty(addProductRequest.getGstType()))
						saveGstTypeTemp(productMaster2.getId(),addProductRequest.getGstType(),(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
							? addProductRequest.getUserId() : addProductRequest.getClientId()));

					industrySectorTempRepository.inActiveMappingByFpProductId(productMaster2.getId());
					// industry data save
					if(!CommonUtils.isListNullOrEmpty(industrySecIdList))
						saveIndustryTemp(productMaster2.getId(),industrySecIdList,(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
								? addProductRequest.getUserId() : addProductRequest.getClientId()));
					// Sector data save
					if(!CommonUtils.isListNullOrEmpty(secIdList))
						saveSectorTemp(productMaster2.getId(),secIdList,(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
								? addProductRequest.getUserId() : addProductRequest.getClientId()));
					geographicalCountryTempRepository.inActiveMappingByFpProductId(productMaster2.getId());
					// country data save
					if(!CommonUtils.isListNullOrEmpty(geogaphicallyCountry))
						saveCountryTemp(productMaster2.getId(),geogaphicallyCountry,(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
								? addProductRequest.getUserId() : addProductRequest.getClientId()));
					// state data save
					geographicalStateTempRepository.inActiveMappingByFpProductId(productMaster2.getId());
					if(!CommonUtils.isListNullOrEmpty(geogaphicallyState))
						saveStateTemp(productMaster2.getId(),geogaphicallyState,(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
								? addProductRequest.getUserId() : addProductRequest.getClientId()));
					// city data save
					geographicalCityTempRepository.inActiveMappingByFpProductId(productMaster2.getId());
					if(!CommonUtils.isListNullOrEmpty(geogaphicallyCity))
						saveCityTemp(productMaster2.getId(),geogaphicallyCity,(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
								? addProductRequest.getUserId() : addProductRequest.getClientId()));
					// negative industry save
					negativeIndustryTempRepository.inActiveMappingByFpProductMasterId(productMaster2.getId());
					if(!CommonUtils.isListNullOrEmpty(negativeIndList))
						saveNegativeIndustryTemp(productMaster2.getId(),negativeIndList,(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
								? addProductRequest.getUserId() : addProductRequest.getClientId()));

					fPParameterMappingService.inactiveAndSaveTemp(productMaster2.getId(),CommonUtils.ParameterTypes.BANK_STATEMENT_OPTIONS, addProductRequest.getBankStatementOptions());
					//msme value
					tempRepository.inActiveTempByFpProductId(productMaster2.getId());
					List<MsmeValueMapping> masterList = masterRepository.findByFpProductIdAndIsActive(addProductRequest.getLoanId(), true);

					if (!CommonUtils.isListNullOrEmpty(masterList)) {
						for (MsmeValueMapping master : masterList) {
							MsmeValueMappingTemp temp= new MsmeValueMappingTemp();
							BeanUtils.copyProperties(master,temp, "id");
							temp.setActive(true);
							temp.setFpProductId(productMaster2.getId());
							temp.setCreatedBy(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
									? addProductRequest.getUserId() : addProductRequest.getClientId());
							temp.setCreatedDate(new Date());
							temp.setModifiedBy(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
									? addProductRequest.getUserId() : addProductRequest.getClientId());
							temp.setModifiedDate(new Date());
							tempRepository.save(temp);
						}
					}
				}
				return true;
			}

		}

		catch (Exception e) {
			logger.error("error while saveOrUpdate : ", e);
			return false;
		}
	}

	private void saveGstTypeTemp(Long id, List<Integer> gstType, Long userId) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveGstTypeTemp");
		FpGstTypeMappingTemp fpGstTypeMappingTemp= null;
		for (Integer dataRequest : gstType) {
			fpGstTypeMappingTemp = new FpGstTypeMappingTemp();
			fpGstTypeMappingTemp.setFpProductId(id);
			fpGstTypeMappingTemp.setGstTypeId(dataRequest);
			fpGstTypeMappingTemp.setCreatedBy(userId);
			fpGstTypeMappingTemp.setModifiedBy(userId);
			fpGstTypeMappingTemp.setCreatedDate(new Date());
			fpGstTypeMappingTemp.setModifiedDate(new Date());
			fpGstTypeMappingTemp.setIsActive(true);
			// create by and update
			fpGstTypeMappingTempRepository.save(fpGstTypeMappingTemp);
		}
		CommonDocumentUtils.endHook(logger, "saveGstTypeTemp");
	}

	private void saveNegativeIndustryTemp(Long id, List<DataRequest> negativeIndList,Long userId) {
		CommonDocumentUtils.startHook(logger, "saveNegativeIndustryTemp");
		NegativeIndustryTemp negativeIndustry = null;
		for (DataRequest dataRequest : negativeIndList) {
			negativeIndustry = new NegativeIndustryTemp();
			negativeIndustry.setFpProductMasterId(id);
			negativeIndustry.setIndustryId(dataRequest.getId());
			negativeIndustry.setCreatedBy(userId);
			negativeIndustry.setModifiedBy(userId);
			negativeIndustry.setCreatedDate(new Date());
			negativeIndustry.setModifiedDate(new Date());
			negativeIndustry.setIsActive(true);
			// create by and update
			negativeIndustryTempRepository.save(negativeIndustry);
		}
		CommonDocumentUtils.endHook(logger, "saveNegativeIndustryTemp");
	}

	private void saveCityTemp(Long id, List<DataRequest> geogaphicallyCity,Long userId) {

		logger.info("start saveCity");
		GeographicalCityDetailTemp geographicalCityDetail = null;
		//List<GeographicalCityDetailTemp> geographicalCityDetailTemps=new ArrayList<>(geogaphicallyCity.size()); 
		for (DataRequest dataRequest : geogaphicallyCity) {
			geographicalCityDetail = new GeographicalCityDetailTemp();
			geographicalCityDetail.setCityId(dataRequest.getId());
			geographicalCityDetail.setFpProductMaster(id);
			geographicalCityDetail.setCreatedBy(userId);
			geographicalCityDetail.setModifiedBy(userId);
			geographicalCityDetail.setCreatedDate(new Date());
			geographicalCityDetail.setModifiedDate(new Date());
			geographicalCityDetail.setIsActive(true);
			// create by and update
			geographicalCityTempRepository.save(geographicalCityDetail);
		}
		
		 
		
		     
		/*EntityManagerFactory emf = Persistence.createEntityManagerFactory("TDEMSPU");
        entityManager = emf.createEntityManager();


        entityManager.getTransaction().begin(); 

      //  List<Enquiry> tempEnqList = tempEnqList();
        for (Iterator<GeographicalCityDetailTemp> it = geographicalCityDetailTemps.iterator(); it.hasNext();) {
        	GeographicalCityDetailTemp geographicalCityDetailTemp = it.next();

        	entityManager.persist(geographicalCityDetailTemp);
        	entityManager.flush();
        	entityManager.clear();
        }

        entityManager.getTransaction().commit();*/
		logger.info("end saveCity");
		
	}

	private void saveStateTemp(Long id, List<DataRequest> geogaphicallyState,Long userId) {
		 logger.info("start saveStateTemp");
		GeographicalStateDetailTemp geographicalStateDetail = null;
		for (DataRequest dataRequest : geogaphicallyState) {
			geographicalStateDetail = new GeographicalStateDetailTemp();
			geographicalStateDetail.setStateId(dataRequest.getId());
			geographicalStateDetail.setFpProductMaster(id);
			geographicalStateDetail.setCreatedBy(userId);
			geographicalStateDetail.setModifiedBy(userId);
			geographicalStateDetail.setCreatedDate(new Date());
			geographicalStateDetail.setModifiedDate(new Date());
			geographicalStateDetail.setIsActive(true);
			// create by and update
			geographicalStateTempRepository.save(geographicalStateDetail);
		}
		logger.info("end saveStateTemp");
		
	}

	private void saveConstitutionTypeTemp(List<Integer> constitutionIds,Long mappingId,Long userId) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveConstitutionTypeTemp");
		ConstitutionMappingTemp constitutionMapping= null;
		for (Integer dataRequest : constitutionIds) {
			constitutionMapping = new ConstitutionMappingTemp();
			constitutionMapping.setFpProductId(mappingId);
			constitutionMapping.setConstitutionId(dataRequest);
			constitutionMapping.setCreatedBy(userId);
			constitutionMapping.setModifiedBy(userId);
			constitutionMapping.setCreatedDate(new Date());
			constitutionMapping.setModifiedDate(new Date());
			constitutionMapping.setIsActive(true);
			// create by and update
			fpConstitutionMappingTempRepository.save(constitutionMapping);
		}
		CommonDocumentUtils.endHook(logger, "saveConstitutionTypeTemp");
		
	}
	
//	private void saveConstitutionTypeTemp(TermLoanParameterRequest termLoanParameterRequest) {
//		// TODO Auto-generated method stub
//		CommonDocumentUtils.startHook(logger, "saveConstitutionTypeTemp");
//		ConstitutionMappingTemp constitutionMapping= null;
//		for (Integer dataRequest : termLoanParameterRequest.getConstitutionIds()) {
//			constitutionMapping = new ConstitutionMappingTemp();
//			constitutionMapping.setFpProductId(termLoanParameterRequest.getId());
//			constitutionMapping.setConstitutionId(dataRequest);
//			constitutionMapping.setCreatedBy(termLoanParameterRequest.getUserId());
//			constitutionMapping.setModifiedBy(termLoanParameterRequest.getUserId());
//			constitutionMapping.setCreatedDate(new Date());
//			constitutionMapping.setModifiedDate(new Date());
//			constitutionMapping.setIsActive(true);
//			// create by and update
//			fpConstitutionMappingTempRepository.save(constitutionMapping);
//		}
//		CommonDocumentUtils.endHook(logger, "saveConstitutionTypeTemp");
//		
//	}
	
	private void saveCountryTemp(Long id, List<DataRequest> geogaphicallyCountry,Long userId) {

		logger.info("save saveCountryTemp");
		GeographicalCountryDetailTemp geographicalCountryDetail = null;
		for (DataRequest dataRequest : geogaphicallyCountry) {
			geographicalCountryDetail = new GeographicalCountryDetailTemp();
			geographicalCountryDetail.setCountryId(dataRequest.getId());
			geographicalCountryDetail.setFpProductMaster(id);
			geographicalCountryDetail.setCreatedBy(userId);
			geographicalCountryDetail.setModifiedBy(userId);
			geographicalCountryDetail.setCreatedDate(new Date());
			geographicalCountryDetail.setModifiedDate(new Date());
			geographicalCountryDetail.setIsActive(true);
			// create by and update
			geographicalCountryTempRepository.save(geographicalCountryDetail);
		}
		logger.info("end saveCountryTemp");
		
	}

	private void saveSectorTemp(Long id, List<DataRequest> secIdList,Long userId) {

		logger.info("start saveSectorTemp");
		IndustrySectorDetailTemp industrySectorDetail = null;
		for (DataRequest dataRequest : secIdList) {
			industrySectorDetail = new IndustrySectorDetailTemp();
			industrySectorDetail.setFpProductId(id);
			industrySectorDetail.setSectorId(dataRequest.getId());
			industrySectorDetail.setCreatedBy(userId);
			industrySectorDetail.setModifiedBy(userId);
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setModifiedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorTempRepository.save(industrySectorDetail);
		}
		logger.info("end saveSectorTemp");
		
	}

	private void saveIndustryTemp(Long id, List<DataRequest> industrySecIdList,Long userId) {

		logger.info("start saveIndustryTemp");
		IndustrySectorDetailTemp industrySectorDetail = null;
		for (DataRequest dataRequest : industrySecIdList) {
			industrySectorDetail = new IndustrySectorDetailTemp();
			industrySectorDetail.setFpProductId(id);
			industrySectorDetail.setIndustryId(dataRequest.getId());
			industrySectorDetail.setCreatedBy(userId);
			industrySectorDetail.setModifiedBy(userId);
			industrySectorDetail.setCreatedDate(new Date());
			industrySectorDetail.setModifiedDate(new Date());
			industrySectorDetail.setIsActive(true);
			// create by and update
			industrySectorTempRepository.save(industrySectorDetail);
		}
		logger.info("end saveIndustryTemp");
		
	}

	@Override
	public ProductMaster getProductMaster(Long id) {
		return productMasterRepository.findOne(id);
	}

	@Override
	public JSONObject checkParameterIsFilled(Long productId) {
		CommonDocumentUtils.startHook(logger, "checkParameterIsFilled");
		ProductMaster productMaster = productMasterRepository.findOne(productId);
		JSONObject obj = new JSONObject();
		if (CommonUtils.isObjectNullOrEmpty(productMaster)) {
			obj.put(STATUS_LITERAL, false);
			obj.put(MESSAGE_LITERAL, "Product id is not valid");
			return obj;
		}
		if (!productMaster.getIsActive()) {
			obj.put(STATUS_LITERAL, false);
			obj.put(MESSAGE_LITERAL, "Requested User is In Active");
			return obj;
		}
		if (!productMaster.getIsParameterFilled()) {
			obj.put(STATUS_LITERAL, false);
			obj.put(MESSAGE_LITERAL, "Requested user has not filled parameter yet");
			return obj;
		}
		obj.put(STATUS_LITERAL, true);
		obj.put(MESSAGE_LITERAL, "Show teaser view");
		CommonDocumentUtils.endHook(logger, "checkParameterIsFilled");
		return obj;
	}

	@Override
	public List<ProductMasterRequest> getList(Long userId, Long userOrgId) {
		CommonDocumentUtils.startHook(logger, "getList");
		Object[]  userObj= loanRepository.getUserDetails(userId);
		List<ProductMaster> results;
		if (!CommonUtils.isObjectNullOrEmpty(userOrgId)) {
			if(!CommonUtils.isObjectNullOrEmpty(userObj) && !CommonUtils.isObjectNullOrEmpty(userObj[0])){
				Long businessTypeId = Long.valueOf (userObj[0].toString());
				results = productMasterRepository.getUserProductListByOrgIdByBusinessTypeId(userOrgId,businessTypeId);
			}else
				results = productMasterRepository.getUserProductListByOrgId(userOrgId);
		} else {
			results = productMasterRepository.getUserProductList(userId);
		}
		List<ProductMasterRequest> requests = new ArrayList<>(results.size());
		for (ProductMaster master : results) {
			ProductMasterRequest request = new ProductMasterRequest();
			BeanUtils.copyProperties(master, request);
			request.setIsMatched(productMasterRepository.getMatchedAndActiveProduct(userId).size() > 0 ? true : false);
			requests.add(request);
		}
		CommonDocumentUtils.endHook(logger, "getList");
		return requests;
	}
	
	@Override
	public List<ProductMasterRequest> getActiveInActiveList(Long userId, Long userOrgId, Long businessTypeId) {
		CommonDocumentUtils.startHook(logger, "getActiveInActiveList");
		UserResponse userResponse=null;
		BranchBasicDetailsRequest basicDetailsRequest = null;
		try {
			UsersRequest usersRequest = new UsersRequest();
			usersRequest.setId(userId);
			logger.info("Current user id ---------------------------------------------------> " + userId);
			userResponse = usersClient.getBranchDetailsBYUserId(usersRequest);
			basicDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
					(LinkedHashMap<String, Object>) userResponse.getData(), BranchBasicDetailsRequest.class);
		} catch (Exception e) {
			logger.error("Throw Exception While Get Branch Id from UserId : ",e);
		}
		List<ProductMaster> results;
		if (!CommonUtils.isObjectNullOrEmpty(userOrgId)) {
			results = productMasterRepository.getUserProductActiveList(userOrgId,businessTypeId);
//			results = productMasterRepository.getUserProductActiveInActiveListByOrgId(userOrgId);
		} else {
			results = productMasterRepository.getUserProductList(userId);
		}
		List<ProductMasterRequest> requests = new ArrayList<>(results.size());
		List<ProductMasterRequest> activeProducts= new ArrayList<>();
		List<ProductMasterRequest> inActiveProducts= new ArrayList<>();

		Long matchCount = productMasterRepository.countByUserIdAndIsMatched(userId, true);
		
		for (ProductMaster master : results) {
			ProductMasterRequest request = new ProductMasterRequest();
			BeanUtils.copyProperties(master, request);
//			request.setIsMatched(productMasterRepository.getMatchedAndActiveInActiveProduct(userId).size() > 0 ? true : false);
			request.setIsMatched(matchCount > 0 ? true : false);
			Long count = null;
			if(basicDetailsRequest != null){
				if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.BO || basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.FP_CHECKER) {
					count = proposalDetailsRepository.getProposalCountByFpProductIdAndBranchId(master.getId(), basicDetailsRequest.getId());
				}else if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.HO) {
					count = proposalDetailsRepository.getProposalCountByFpProductId(master.getId());
				}
				else if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.SMECC) // Hiren
				{
					count = proposalDetailsRepository.getProposalCountByFpProductIdAndBranchId(master.getId(),userResponse.getBranchList());
				}
				/*else {
					logger.info("Branch Id Can't found,set by assignee");
					count = proposalDetailsRepository.countProposalListOfFundProviderByAssignId(master.getId(), userId);
				}*/
			}else{
				count = proposalDetailsRepository.getProposalCountByFpProductId(master.getId());
			}
			request.setProposalCount(count);

			if(request.getIsActive() == true) {
				activeProducts.add(request);
			}else {
				inActiveProducts.add(request);
			}

		}

		requests.addAll(activeProducts);
		requests.addAll(inActiveProducts);
		CommonDocumentUtils.endHook(logger, "getActiveInActiveList");
		return requests;
	}

	@Override
	public String getUserNameByApplicationId(Long productId, Long userId) {
		CommonDocumentUtils.startHook(logger, GET_USER_NAME_BY_APPLICATION_ID);
		ProductMaster productMaster = productMasterRepository.getUserProduct(productId, userId);
		if (productMaster != null) {
			CommonDocumentUtils.endHook(logger, GET_USER_NAME_BY_APPLICATION_ID);
			return productMaster.getFpName();
		}
		CommonDocumentUtils.endHook(logger, GET_USER_NAME_BY_APPLICATION_ID);
		return null;
	}

	@Override
	public Object[] getUserDetailsByPrductId(Long fpMappingId) {
		CommonDocumentUtils.startHook(logger, "getUserDetailsByPrductId");
		List<Object[]> pm = productMasterRepository.findById(fpMappingId);
		CommonDocumentUtils.endHook(logger, "getUserDetailsByPrductId");
		return (pm != null && !pm.isEmpty()) ? pm.get(0) : null;
	}

	@Override
	public List<ProductDetailsForSp> getProductDetailsByUserIdList(Long userId) {

		return productMasterRepository.getListByUserId(userId);

	}

	@Override
	public ProductDetailsResponse getProductDetailsResponse(Long userId, Long userOrgId) {
		CommonDocumentUtils.startHook(logger, "getProductDetailsResponse");
		UserResponse usrResponse = usersClient.getLastAccessApplicant(new UsersRequest(userId));
		ProductDetailsResponse productDetailsResponse = new ProductDetailsResponse();
		if (usrResponse != null && usrResponse.getStatus() == 200) {
			Long fpMappingId = usrResponse.getId();
			if (fpMappingId != null) {
				ProductMaster userProduct = null;
				if (!CommonUtils.isObjectNullOrEmpty(userOrgId)) {
//					userProduct = productMasterRepository.getUserProductByOrgId(fpMappingId, userOrgId);
					userProduct = productMasterRepository.getUserProductByOrgId(fpMappingId, userOrgId,usrResponse.getLastAccessBusinessTypeId());
				} else if (!CommonUtils.isObjectNullOrEmpty(userId)) {
//					userProduct = productMasterRepository.getUserProduct(fpMappingId, userId);
					userProduct = productMasterRepository.getUserProduct(fpMappingId, userId,usrResponse.getLastAccessBusinessTypeId());
				} else {
//					userProduct = productMasterRepository.findByIdAndIsActive(fpMappingId, true);
					userProduct = productMasterRepository.findByIdAndIsActiveAndBusinessTypeId(fpMappingId, true,usrResponse.getLastAccessBusinessTypeId());
				}
				if (CommonUtils.isObjectNullOrEmpty(userProduct)) {
					getProguctDetail(userId, userOrgId, usrResponse, productDetailsResponse);
				}else {
					productDetailsResponse.setProductId(userProduct.getProductId());
					productDetailsResponse.setProductMappingId(fpMappingId);
					productDetailsResponse.setMessage("Proposal Details Sent");
					productDetailsResponse.setStatus(HttpStatus.OK.value());
				}
			} else {
				getProguctDetail(userId, userOrgId, usrResponse, productDetailsResponse);
			}
		} else {
			productDetailsResponse.setMessage("Something went wrong");
			productDetailsResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return productDetailsResponse;
	}

	/**
	 * @author vijay.chauhan
	 * @param userId
	 * @param userOrgId
	 * @param usrResponse
	 * @param productDetailsResponse
	 */
	private void getProguctDetail(Long userId, Long userOrgId, UserResponse usrResponse,ProductDetailsResponse productDetailsResponse) {
		List<ProductMaster> userProductList = null;
		if (!CommonUtils.isObjectNullOrEmpty(userOrgId)) {
//					userProductList = productMasterRepository.getUserProductListByOrgId(userOrgId);
			userProductList = productMasterRepository.getUserProductListByOrgId(userOrgId,usrResponse.getLastAccessBusinessTypeId());
		} else {
//					userProductList = productMasterRepository.getUserProductList(userId);
			userProductList = productMasterRepository.getUserProductList(userId,usrResponse.getLastAccessBusinessTypeId());
		}
		if (!CommonUtils.isListNullOrEmpty(userProductList)) {
			ProductMaster productMaster = userProductList.get(0);
			productDetailsResponse.setProductId(productMaster.getProductId());
			productDetailsResponse.setProductMappingId(productMaster.getId());
			productDetailsResponse.setMessage("Proposal Details Sent");
			productDetailsResponse.setStatus(HttpStatus.OK.value());
			UsersRequest req = new UsersRequest();
			logger.info("-->>>>|||||||||||productMaster.getId()={}",productMaster.getId());
			req.setId(userId);
			req.setLastAccessApplicantId(productMaster.getId());
			usersClient.setLastAccessApplicant(req);
		} else {
			UsersRequest req = new UsersRequest();
			req.setId(userId);
			req.setLastAccessApplicantId(null);
			usersClient.setLastAccessApplicant(req);
			productDetailsResponse.setMessage("Something went wrong");
			productDetailsResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}
	}

	@Override
	public FpProductDetails getProductDetails(Long productMappingId) throws LoansException {
		try {
			CommonDocumentUtils.startHook(logger, "getProductDetails");
			ProductMaster productMaster = productMasterRepository.findOne(productMappingId);
			LoanType loanType = LoanType.getById(productMaster.getProductId());
			FpProductDetails fpProductDetails = new FpProductDetails();
			if (!CommonUtils.isObjectNullOrEmpty(productMaster.getName())) {
				fpProductDetails.setName(productMaster.getName());
			}
			switch (loanType) {
				case WORKING_CAPITAL:
					productMaster = workingCapitalParameterRepository.findOne(productMappingId);
					break;
				case TERM_LOAN:
					productMaster = termLoanParameterRepository.findOne(productMappingId);
					break;
				default:
					break;
			}
			fpProductDetails.setTypeOfInvestment(LoanType.getById(productMaster.getProductId()).getValue());
			List<String> countryname = new ArrayList<String>();
			List<Long> countryList = geoCountry.getCountryByFpProductId(productMappingId);
			if (!CommonUtils.isListNullOrEmpty(countryList)) {
				OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
				List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
				if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {

					for (int i = 0; i < oneResponseDataList.size(); i++) {
						MasterResponse masterResponse = MultipleJSONObjectHelper
								.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
						countryname.add(masterResponse.getValue());
					}
				}
				fpProductDetails.setGeographicalFocus(countryname);
			}

			// fp profile details
			fpProductDetails.setFpDashboard(usersClient.getFPDashboardDetails(productMaster.getUserId()));

			CommonDocumentUtils.endHook(logger, "getProductDetails");
			return fpProductDetails;
		}
		catch (Exception e){
			throw new LoansException(e);
		}

	}

	@Override
	public boolean isSelfView(Long fpProductId, Long userId) {
		return productMasterRepository.getUserProduct(fpProductId, userId) != null;
	}

	@Override
	public boolean isProductMatched(Long userId, MultipleFpPruductRequest multipleFpPruductRequest) throws IOException {
		CommonDocumentUtils.startHook(logger, IS_PRODUCT_MATCHED);
		List<ProductDetailsForSp> productDetailsForSps = productMasterRepository.getMatchedAndActiveProduct(userId);
		if (CommonUtils.isListNullOrEmpty(productDetailsForSps)) {
			return false;
		}
		if (!CommonUtils.isObjectNullOrEmpty(multipleFpPruductRequest)) {
			for (Map<String, Object> obj : multipleFpPruductRequest.getDataList()) {
				ProductMasterRequest productMasterRequest = (ProductMasterRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, ProductMasterRequest.class);

				ProductMaster master = productMasterRepository.findOne(productMasterRequest.getId());
				if (!CommonUtils.isObjectNullOrEmpty(master) && !productMasterRequest.getProductId().toString()
						.equals(productDetailsForSps.get(0).getProductId().toString())) {
					// if(master.getId())
						CommonDocumentUtils.endHook(logger, IS_PRODUCT_MATCHED);
						return true;
				}
			}

		}
		CommonDocumentUtils.endHook(logger, IS_PRODUCT_MATCHED);
		return false;
	}

	@Override
	public int setIsMatchProduct(Long id, Long userId) {
		CommonDocumentUtils.startHook(logger, "setIsMatchProduct");
		CommonDocumentUtils.endHook(logger, "setIsMatchProduct");
		return productMasterRepository.setIsMatchProduct(id, userId);

	}

	public static List<ProductMasterTemp> getFromObjectArray(List<Object[]> array){
		if(CommonUtils.isListNullOrEmpty(array)){
			return Collections.emptyList();
		}
		List<ProductMasterTemp> result = new ArrayList<>(array.size());
		ProductMasterTemp masterTemp = null;
		for(Object [] arr : array){
			masterTemp = new ProductMasterTemp();
			if(!CommonUtils.isObjectNullOrEmpty(arr[0])){
				masterTemp.setId(((BigInteger)arr[0]).longValue());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[1])){
				masterTemp.setProductId(((BigInteger)arr[1]).intValue());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[2])){
				masterTemp.setUserId(((BigInteger)arr[2]).longValue());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[3])){
				masterTemp.setFpName(arr[3].toString());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[4])){
				masterTemp.setName(arr[4].toString());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[5])){
				masterTemp.setIsParameterFilled((Boolean)arr[5]);
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[6])){
				masterTemp.setIsActive((Boolean)arr[6]);
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[7])){
				masterTemp.setProductCode(arr[7].toString());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[8])){
				masterTemp.setUserOrgId(((BigInteger)arr[8]).longValue());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[9])){
				masterTemp.setScoreModelId(((BigInteger)arr[9]).longValue());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[10])){
				masterTemp.setBusinessTypeId(((BigInteger)arr[10]).longValue());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[11])){
				masterTemp.setIsApproved((Boolean)arr[11]);
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[12])){
				masterTemp.setIsDeleted((Boolean)arr[12]);
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[13])){
				masterTemp.setIsCopied((Boolean)arr[13]);
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[14])){
				masterTemp.setIsEdit((Boolean)arr[14]);
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[15])){
				masterTemp.setStatusId(Integer.valueOf(arr[15].toString()));
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[16])){
				masterTemp.setJobId(((BigInteger)arr[16]).longValue());
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[17])){
				masterTemp.setFinId(Integer.valueOf(arr[17].toString()));
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[18])){
				masterTemp.setCampaignCode(Integer.valueOf(arr[18].toString()));
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[20])){
				masterTemp.setWcRenewalStatus(Integer.valueOf(arr[20].toString()));
			}
			if(!CommonUtils.isObjectNullOrEmpty(arr[21])){
				masterTemp.setCreatedDate((Date)arr[21]);
			}
			result.add(masterTemp);
		}
		return result;
	}
	
	@Override
	public List<ProductMasterRequest> getListByUserType(Long userId, Integer userType, Integer stage, Long userOrgId,Integer productId) {
		CommonDocumentUtils.startHook(logger, "getListByUserType");
		List<ProductMasterRequest> productMasterRequests = new ArrayList<>();
		if (!CommonUtils.isObjectNullOrEmpty(stage) && stage == 1) {
			List<ProductMasterTemp> results = null;
			Object[]  userObj= loanRepository.getUserDetails(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userOrgId)) {
				if(!CommonUtils.isObjectNullOrEmpty(userObj) && !CommonUtils.isObjectNullOrEmpty(userObj[0])) {
					Long businessTypeId = Long.valueOf(userObj[0].toString());
					List<Object[]> arr =  (List<Object[]>)productMasterTempRepository.getProductListByBusinessTypeId(userOrgId, businessTypeId);
					results = getFromObjectArray(arr);
				}else{
					results = productMasterTempRepository.getUserCorporateProductListByOrgId(userOrgId);
				}
			} else {
				results = productMasterTempRepository.getUserCorporateProductList(userId);
			}
			for (ProductMasterTemp productMaster : results) {
				ProductMasterRequest productMasterRequest = new ProductMasterRequest();
				BeanUtils.copyProperties(productMaster, productMasterRequest);
				List<Integer> gstTypes = fpGstTypeMappingTempRepository.getIdsByFpProductId(productMaster.getId());
				productMasterRequest.setBankStatementOptions(fPParameterMappingService.getParametersTemp(productMaster.getId(), CommonUtils.ParameterTypes.BANK_STATEMENT_OPTIONS));
				productMasterRequest.setGstType(gstTypes);
				productMasterRequests.add(productMasterRequest);
			}
		} else {
			List<ProductMaster> results = null;
				Object[]  userObj= loanRepository.getUserDetails(userId);
				if (!CommonUtils.isObjectNullOrEmpty(userOrgId)) {
					if(!CommonUtils.isObjectNullOrEmpty(userObj) && !CommonUtils.isObjectNullOrEmpty(userObj[0])) {
						Long businessTypeId = Long.valueOf(userObj[0].toString());
						results = productMasterRepository.getUserProductListByOrgIdByBusinessTypeId(userOrgId, businessTypeId);
					}else {
						results = productMasterRepository.getUserCorporateProductListByOrgId(userOrgId);
					}
				} else {
					results = productMasterRepository.getUserCorporateProductList(userId);
				}
			for (ProductMaster productMaster : results) {
				ProductMasterRequest productMasterRequest = new ProductMasterRequest();
				BeanUtils.copyProperties(productMaster, productMasterRequest);
				List<Integer> gstTypes = fpGstTypeMappingRepository.getIdsByFpProductId(productMaster.getId());
				productMasterRequest.setGstType(gstTypes);
				productMasterRequest.setBankStatementOptions(fPParameterMappingService.getParameters(productMaster.getId(), CommonUtils.ParameterTypes.BANK_STATEMENT_OPTIONS));
				productMasterRequests.add(productMasterRequest);
			}
		}
		
		CommonDocumentUtils.endHook(logger, "getListByUserType");

		return productMasterRequests;
	}

	@Override
	public Boolean changeStatus(Long fpProductId, Boolean status, Long userId, Integer stage) {
		CommonDocumentUtils.startHook(logger, "changeStatus");
		try {
			if (stage == 2) {
				productMasterRepository.changeStatus(userId, fpProductId, status);
			} else {
				productMasterTempRepository.changeStatus(userId, fpProductId, status);
			}
			return true;
		} catch (Exception e) {
			logger.error("error while changeStatus : ", e);
		}
		CommonDocumentUtils.endHook(logger, "changeStatus");
		return null;
	}

	@Override
	public Boolean saveCorporate(CorporateProduct corporateProduct) {
		CommonDocumentUtils.startHook(logger, SAVE_CORPORATE);
		if (!CommonUtils.isObjectNullOrEmpty(corporateProduct) && !CommonUtils.isObjectNullOrEmpty(corporateProduct.getProductId()) ) {
				if (corporateProduct.getProductId() == CommonUtils.LoanType.WORKING_CAPITAL.getValue()) {
					WorkingCapitalParameterRequest capitalParameterRequest = new WorkingCapitalParameterRequest();
					BeanUtils.copyProperties(corporateProduct, capitalParameterRequest);
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
					return workingCapitalParameterService.saveOrUpdate(capitalParameterRequest, null);
				} else if (corporateProduct.getProductId() == CommonUtils.LoanType.TERM_LOAN.getValue()) {
					TermLoanParameterRequest loanParameterRequest = new TermLoanParameterRequest();
					BeanUtils.copyProperties(corporateProduct, loanParameterRequest);
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
					return termLoanParameterService.saveOrUpdate(loanParameterRequest, null,null);
				} else if (corporateProduct.getProductId() == CommonUtils.LoanType.UNSECURED_LOAN.getValue()) {
					UnsecuredLoanParameterRequest unsecuredLoanParameterRequest = new UnsecuredLoanParameterRequest();
					BeanUtils.copyProperties(corporateProduct, unsecuredLoanParameterRequest);
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
					return unsecuredLoanParameterService.saveOrUpdate(unsecuredLoanParameterRequest);
				} else if (corporateProduct.getProductId() == CommonUtils.LoanType.WCTL_LOAN.getValue()) {
					WcTlParameterRequest wcTlParameterRequest = new WcTlParameterRequest();
					BeanUtils.copyProperties(corporateProduct, wcTlParameterRequest);
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
					return wcTlParameterService.saveOrUpdate(wcTlParameterRequest, null);
				}
		}
		CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
		return false;
	}

	@Override
	public ProductMasterRequest lastAccessedProduct(Long userId) {
		CommonDocumentUtils.startHook(logger, "lastAccessedProduct");
		ProductMasterRequest productMasterRequest = new ProductMasterRequest();
		BeanUtils.copyProperties(productMasterRepository.getLastAccessedProduct(userId), productMasterRequest);
		CommonDocumentUtils.endHook(logger, "lastAccessedProduct");
		return productMasterRequest;
	}

	@Override
	public List<ChatDetails> getChatListByFpMappingId(Long mappingId) {
		ProposalMappingRequest mappingRequest = new ProposalMappingRequest();
		mappingRequest.setApplicationId(mappingId);
		try {
			List<LinkedHashMap<String, Object>> mappingRequestList = (List<LinkedHashMap<String, Object>>) proposalDetailsClient
					.getFundSeekerChatList(mappingRequest).getDataList();
			if (!CommonUtils.isListNullOrEmpty(mappingRequestList)) {
				List<ChatDetails> chatDetailList = new ArrayList<ChatDetails>(mappingRequestList.size());
				for (LinkedHashMap<String, Object> linkedHashMap : mappingRequestList) {
					try {
						ChatDetails chatDetails = new ChatDetails();
						ProposalMappingRequest proposalMappingRequest = MultipleJSONObjectHelper.getObjectFromMap(
								(LinkedHashMap<String, Object>) linkedHashMap, ProposalMappingRequest.class);

						ProductMaster productMaster = productMasterRepository
								.findOne(proposalMappingRequest.getFpProductId());
						chatDetails.setProposalId(proposalMappingRequest.getId());
						chatDetails.setAppAndFpMappingId(proposalMappingRequest.getFpProductId());
						chatDetails.setIsAppFpProdActive(isProductActive(proposalMappingRequest.getFpProductId()));
						chatDetails.setName(productMaster.getFpName());

						// set profile pic
						DocumentRequest documentRequest = new DocumentRequest();
						documentRequest.setUserType(DocumentAlias.UERT_TYPE_USER);
						documentRequest.setUserId(productMaster.getUserId());
						documentRequest.setUserDocumentMappingId(DocumentAlias.FUND_PROVIDER_PROFIEL_PICTURE);
						try {
							DocumentResponse documentResponse = dmsClient.listUserDocument(documentRequest);
							if (!CommonUtils.isObjectNullOrEmpty(documentResponse) && !CommonUtils.isListNullOrEmpty(documentResponse.getDataList()) ) {
									StorageDetailsResponse storageDetailsResponse = MultipleJSONObjectHelper
											.getObjectFromMap((LinkedHashMap<String, Object>) documentResponse
													.getDataList().get(0), StorageDetailsResponse.class);
									if (!CommonUtils.isObjectNullOrEmpty(storageDetailsResponse)) {
										chatDetails.setProfile(storageDetailsResponse.getFilePath());
									}
							}
						} catch (DocumentException e) {
							logger.error(CommonUtils.EXCEPTION,e);
							throw new DocumentException(e.getMessage());
						}

						chatDetailList.add(chatDetails);
					} catch (IOException e) {
						logger.error(CommonUtils.EXCEPTION,e);
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
				return chatDetailList;
			}
		} catch (MatchException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return Collections.emptyList();
	}

	@Override
	public boolean isProductActive(Long productId) {
		Long count = productMasterRepository.getActiveProductsById(productId);
		if (!CommonUtils.isObjectNullOrEmpty(count) && (count > 0)) {
			return true;
		}
		return false;
	}

	@Override
	public List<ProductMasterRequest> getProductByOrgId(Long orgId) {
		logger.info("Start getProductByOrgId()");
		List<Integer> productIds = productMasterRepository.getProductsByOrgId(orgId);
		logger.info("Product Ids =={}======>Provided By====>{}", productIds, orgId);
		List<ProductMasterRequest> response = new ArrayList<>(productIds.size());
		for (Integer productId : productIds) {
			com.capitaworld.service.loans.utils.CommonUtils.LoanType type = CommonUtils.LoanType
					.getType(productId.intValue());
			if (CommonUtils.isObjectNullOrEmpty(type)) {
				continue;
			}
			ProductMasterRequest request = new ProductMasterRequest();
			request.setProductCode(type.getCode(false));
			request.setProductId(productId);
			request.setName(type.getName());
			response.add(request);
		}
		logger.info("End getProductByOrgId()");
		return response;
	}

	@Override
	public Object getProductMasterWithAllData(Long id, Integer stage, Long role, Long userId) {

		if (!CommonUtils.isObjectNullOrEmpty(stage) && stage == 1) {
			Integer productId = productMasterTempRepository.getProductIdById(id);
			if (productId == CommonUtils.LoanType.WORKING_CAPITAL.getValue()) {
				return workingCapitalParameterService.getWorkingCapitalParameterTemp(id, role, userId);
			} else if (productId == CommonUtils.LoanType.TERM_LOAN.getValue()) {
					return termLoanParameterService.getTermLoanParameterRequestTemp(id, role, userId);
			} else if (productId == CommonUtils.LoanType.WCTL_LOAN.getValue()) {
				return wcTlParameterService.getWcTlRequestTemp(id, role, userId);
			}
		} else {
			Integer productId = productMasterRepository.getProductIdById(id);
			if (productId == CommonUtils.LoanType.WORKING_CAPITAL.getValue()) {
				return workingCapitalParameterService.getWorkingCapitalParameter(id);
			} else if (productId == CommonUtils.LoanType.TERM_LOAN.getValue()) {
				return termLoanParameterService.getTermLoanParameterRequest(id,role);
			} else if (productId == CommonUtils.LoanType.UNSECURED_LOAN.getValue()) {
				return unsecuredLoanParameterService.getUnsecuredLoanParameterRequest(id);
			} else if (productId == CommonUtils.LoanType.WCTL_LOAN.getValue()) {
				return wcTlParameterService.getWcTlRequest(id,role);
			}			
		}
		return null;
	}

	@Override
	public Boolean saveCorporateMasterFromTemp(Long mappingId, Integer roleId) throws LoansException {

		ProductMasterTemp corporateProduct = productMasterTempRepository.getProductMasterTemp(mappingId);
		CommonDocumentUtils.startHook(logger, SAVE_CORPORATE);
		if (!CommonUtils.isObjectNullOrEmpty(corporateProduct) && !CommonUtils.isObjectNullOrEmpty(corporateProduct.getProductId()) ) {
				if (corporateProduct.getProductId() == CommonUtils.LoanType.WORKING_CAPITAL.getValue()) {
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
					return workingCapitalParameterService.saveMasterFromTempWc(mappingId);
				} else if (corporateProduct.getProductId() == CommonUtils.LoanType.TERM_LOAN.getValue()) {
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
					return termLoanParameterService.saveMasterFromTempTl(mappingId,roleId);
				} else if (corporateProduct.getProductId() == CommonUtils.LoanType.WCTL_LOAN.getValue()) {
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
					return wcTlParameterService.saveMasterFromTempWcTl(mappingId);
				}
		}
		CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
		return false;
	}

	@Override
	public Boolean saveCorporateInTemp(CorporateProduct corporateProduct) {

		CommonDocumentUtils.startHook(logger, SAVE_CORPORATE_IN_TEMP);
		if (!CommonUtils.isObjectNullOrEmpty(corporateProduct) && !CommonUtils.isObjectNullOrEmpty(corporateProduct.getProductId()) ) {
				if (corporateProduct.getProductId() == CommonUtils.LoanType.WORKING_CAPITAL.getValue()) {
					WorkingCapitalParameterRequest capitalParameterRequest = new WorkingCapitalParameterRequest();
					BeanUtils.copyProperties(corporateProduct, capitalParameterRequest);
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE_IN_TEMP);
					return workingCapitalParameterService.saveOrUpdateTemp(capitalParameterRequest);
				}
				else if (corporateProduct.getProductId() == CommonUtils.LoanType.TERM_LOAN.getValue()) {
						TermLoanParameterRequest loanParameterRequest = new TermLoanParameterRequest();
						BeanUtils.copyProperties(corporateProduct, loanParameterRequest);
						CommonDocumentUtils.endHook(logger, SAVE_CORPORATE_IN_TEMP);
						return termLoanParameterService.saveOrUpdateTemp(loanParameterRequest);
				} else if (corporateProduct.getProductId() == CommonUtils.LoanType.WCTL_LOAN.getValue()) {
					WcTlParameterRequest wcTlParameterRequest = new WcTlParameterRequest();
					BeanUtils.copyProperties(corporateProduct, wcTlParameterRequest);
					CommonDocumentUtils.endHook(logger, SAVE_CORPORATE_IN_TEMP);
					return wcTlParameterService.saveOrUpdateTemp(wcTlParameterRequest);
				}
		}
		CommonDocumentUtils.endHook(logger, SAVE_CORPORATE);
		return false;
	}

	@Autowired
	private CommonRepository commonRepository;
	
	@Override
	public Boolean clickOnWorkFlowButton(WorkflowData workflowData) {

		try {
			logger.info("Click on workflow Button with WorkFlow Data==>{}", workflowData.toString());
			WorkflowRequest request = new WorkflowRequest();
			request.setActionId(workflowData.getActionId());
			request.setCurrentStep(workflowData.getWorkflowStep());
			request.setToStep(workflowData.getNextworkflowStep());
			request.setJobId(workflowData.getJobId());
			request.setUserId(workflowData.getUserId());
			
			ProductMasterTemp productMasterTemp = productMasterTempRepository.findOne(workflowData.getFpProductId());
			Integer productStatus = null;
			String productType = null;
			if(!CommonUtils.isObjectNullOrEmpty(productMasterTemp) && 
			   !CommonUtils.isObjectNullOrEmpty(productMasterTemp.getStatusId())) {
				productStatus = productMasterTemp.getStatusId();
			}
			
			if(productMasterTemp.getProductId() !=null) {
			  productType = CommonUtils.LoanType.getType(productMasterTemp.getProductId()).getName();
			}

			if (workflowData.getActionId() == WorkflowUtils.Action.SEND_FOR_APPROVAL) {
				int rowUpdated = productMasterTempRepository.updateStatusToInProgress(workflowData.getFpProductId(), 2);
				WorkflowResponse workflowResponse = workflowClient.updateJob(request);
				if (rowUpdated > 0 && workflowResponse.getStatus() == 200) {
					if(!CommonUtils.isObjectNullOrEmpty(productMasterTemp)) {
						
						if(productStatus == CommonUtils.Status.REVERTED) {
							try {
								//	
							}
							catch(Exception e) {
								//logger.error("Exception occured while sending mail to Checker when Admin Maker resend product for Approval : ",e);
							}
						}
						else if(productStatus == CommonUtils.Status.OPEN){
							try {
								Long count = commonRepository.getCountOfJobId(workflowData.getJobId(), workflowData.getWorkflowStep(), workflowData.getActionId());
								if(count > 2) {
									logger.info("Inside sending mail to Checker when Admin Maker resend product for Approval");
									fpAsyncComponent.sendEmailToCheckerWhenAdminMakerResendProductForApproval(productMasterTemp,workflowData.getUserId(),productType);
								}else {
									logger.info("Inside sending mail to Checker when Admin Maker send product for Approval");
									fpAsyncComponent.sendEmailToCheckerWhenAdminMakerSendProductForApproval(productMasterTemp,workflowData.getUserId(),productType);
								}
							}
							catch(Exception e) {
								logger.error("Exception occured while sending mail to Checker when Admin Maker send product for Approval : ",e);
							}
						}
						
					}
					return true;
				} else {
					logger.info("could not updated in productMaster temp", workflowData.getJobId());
					return false;

				}
			} else if (workflowData.getActionId() == WorkflowUtils.Action.APPROVED) {
				Boolean result = saveCorporateMasterFromTemp(workflowData.getFpProductId(),workflowData.getRoleId());
				if (result) {
					WorkflowResponse workflowResponse = workflowClient.updateJob(request);
					if (workflowResponse.getStatus() == 200) {
						if(!CommonUtils.isObjectNullOrEmpty(productMasterTemp)) {
							try {
								logger.info("Inside sending mail to Maker when Admin Checker Approved Product");
								fpAsyncComponent.sendEmailToMakerWhenAdminCheckerApprovedProduct(productMasterTemp,workflowData.getUserId(),productType);	
							}
							catch(Exception e) {
								logger.error("Exception occured while sending mail to Maker when Admin Checker Approved Product : ",e);
							}
						}
						return true;
					}
				}
			} else if (workflowData.getActionId() == WorkflowUtils.Action.SEND_BACK) {
				int rowUpdated = productMasterTempRepository.updateStatusToInProgress(workflowData.getFpProductId(), 3);
				WorkflowResponse workflowResponse = workflowClient.updateJob(request);
				if (rowUpdated > 0 && workflowResponse.getStatus() == 200) {
					if(!CommonUtils.isObjectNullOrEmpty(productMasterTemp)) {
						try {
							logger.info("Inside sending mail to Maker when Admin Checker reverted Product");
							fpAsyncComponent.sendEmailToMakerWhenAdminCheckerRevertedProduct(productMasterTemp,workflowData.getUserId(),productType);	
						}
						catch(Exception e) {
							logger.error("Exception occured while sending mail to Maker when Admin Checker reverted Product : ",e);
						}
					}
					return true;
				} else {
					logger.info("could not updated in productMaster temp", workflowData.getJobId());
					return false;

				}

			}
			return false;
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return false;
		}
	}

	@Override
	public List<ProductMasterRequest> getApprovedListByProductType(Long userId, Integer productId, Integer businessId,Long userOrgId) {

		List<ProductMaster> results = null;
		if (!CommonUtils.isObjectNullOrEmpty(userOrgId)) {
			results = productMasterRepository.getUserCorporateProductListByOrgIdAndBusinessTypeIdAndProductId(userOrgId,businessId.longValue(),productId);
		} else {
			results = productMasterRepository.getUserCorporateProductListByBusinessTypeIdAndProductId(userId,businessId.longValue(),productId);
		}
		if(CommonUtils.isListNullOrEmpty(results)){
			return Collections.emptyList();
		}
		
		List<ProductMasterRequest> productMasterRequests = new ArrayList<>(results.size());
		for (ProductMaster productMaster : results) {
			ProductMasterRequest productMasterRequest = new ProductMasterRequest();
			BeanUtils.copyProperties(productMaster, productMasterRequest);
			List<Integer> gstTypes = fpGstTypeMappingRepository.getIdsByFpProductId(productMaster.getId());
			productMasterRequest.setGstType(gstTypes);
			productMasterRequest.setBankStatementOptions(fPParameterMappingService.getParameters(productMaster.getId(), CommonUtils.ParameterTypes.BANK_STATEMENT_OPTIONS));
			productMasterRequests.add(productMasterRequest);
		}
		return productMasterRequests;
	}

	@Override
	public Long createJobId(Long userId) {
		
		WorkflowResponse workflowResponse = workflowClient.createJobForMasters(
				WorkflowUtils.Workflow.MASTER_DATA_APPROVAL_PROCESS, WorkflowUtils.Action.SEND_FOR_APPROVAL, userId);
		return workflowResponse != null ? Long.valueOf(workflowResponse.getData().toString()) : null;
	}

	@Override
	public Boolean changeStatusWithWorkFlow(WorkflowData workflowData) {
		try {

			WorkflowRequest request = new WorkflowRequest();
			request.setActionId(workflowData.getActionId());
			request.setCurrentStep(workflowData.getWorkflowStep());
			request.setToStep(workflowData.getNextworkflowStep());
			request.setJobId(workflowData.getJobId());
			request.setUserId(workflowData.getUserId());
			
			ProductMasterTemp productMasterTemp = null;
			ProductMaster productMaster = null;
			Boolean status = null;
			
			if(workflowData.getStage() == 2) {
				productMaster = productMasterRepository.findOne(workflowData.getFpProductId());
			}else {
				productMasterTemp = productMasterTempRepository.findOne(workflowData.getFpProductId());
			}
			
			WorkflowResponse workflowResponse = workflowClient.updateJob(request);
			
			if (workflowData.getActionId() == WorkflowUtils.Action.SEND_FOR_APPROVAL && workflowResponse != null) {
				
				if(workflowData.getActionFor() == null) {
					return false;
				}
				
				if (workflowData.getStage() == 2 && productMaster != null) {
					productMaster.setActiveInactiveJobId(workflowData.getJobId());
					productMaster.setActionFor(workflowData.getActionFor());
					productMasterRepository.save(productMaster);
				} else {
					if (productMasterTemp != null) {
						productMasterTemp.setActiveInactiveJobId(workflowData.getJobId());
						productMasterTemp.setActionFor(workflowData.getActionFor());
						productMasterTempRepository.save(productMasterTemp);
					}
				}
				
				return true;
				
			} else if (workflowData.getActionId() == WorkflowUtils.Action.APPROVED  && workflowResponse != null) {
								
				if (workflowData.getStage() == 2 && productMaster != null) {
					if(productMaster.getActionFor() == null) {
						return false;
					}
					status = productMaster.getActionFor().equals("Active") ? true : false;
					productMasterRepository.changeStatusAndActiveInactiveJobId(workflowData.getUserId(), workflowData.getFpProductId(), status);
				} else {
					if (productMasterTemp != null) {
						if (productMasterTemp.getActionFor() == null) {
							return false;
						}
						status = productMasterTemp.getActionFor().equals("Active") ? true : false;
						productMasterTempRepository.changeStatusAndActiveInactiveJobId(workflowData.getUserId(), workflowData.getFpProductId(), status);
					}
				}
				return true;
				
			} else if (workflowData.getActionId() == WorkflowUtils.Action.SEND_BACK  && workflowResponse != null) {
				if (workflowData.getStage() == 2 && productMaster != null) {
					productMaster.setActiveInactiveJobId(null);
					productMasterRepository.save(productMaster);
				} else {
					if (productMasterTemp != null) {
						productMasterTemp.setActiveInactiveJobId(null);
						productMasterTempRepository.save(productMasterTemp);
					}
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return false;
		}
	}

	@Override
	public FpProductRoiResponse getMinMaxRoiFromFpProductId(Long fpProductId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get working capital product count
	 */
	@Override
	public Long getWCRenewalProductsCount(CommonRequest request) {
		return productMasterRepository.getWCRenewalProductsCount(CommonUtils.LoanType.WORKING_CAPITAL.getValue(), request.getUserOrgId(), request.getBusinessTypeId(), request.getWcRenewalStatus());
	}
	
	@Override
	public Boolean scoringCheckIsActive(Long fpProductId) {
	
		ProductMaster checkParameterIsactive = productMasterRepository.checkParameterIsactive(fpProductId);
		if(!CommonUtils.isObjectNullOrEmpty(checkParameterIsactive)) {
			List<Long> idList=new ArrayList<>();
			idList.add(checkParameterIsactive.getScoreModelId());
			idList.add(checkParameterIsactive.getScoreModelIdCoAppId());
			idList.add(checkParameterIsactive.getScoreModelIdCoAppIdOthThnSal());
			idList.add(checkParameterIsactive.getScoreModelIdOthThnSal());
			for(Long i:idList)
			{
				if(CommonUtils.isObjectListNull(i)){
					continue;
				}
	
		String checkParameterInScoringIsActive = productMasterRepository.checkParameterInScoringIsActive(i);
		if(!CommonUtils.isObjectNullOrEmpty(checkParameterInScoringIsActive)) {
			return true;
		}
		}
		}
		return false;
	}

	@Override
	public Boolean scoringCheckIsActiveforPanding(Long fpProductId) {
	
		ProductMasterTemp checkParameterIsactive = productMasterRepository.checkParameterIsactiveForPanding(fpProductId);
		if(!CommonUtils.isObjectNullOrEmpty(checkParameterIsactive)) {
		List<Long> idList=new ArrayList<>();
		idList.add(checkParameterIsactive.getScoreModelId());
		idList.add(checkParameterIsactive.getScoreModelIdCoAppId());
		idList.add(checkParameterIsactive.getScoreModelIdCoAppIdOthThnSal());
		idList.add(checkParameterIsactive.getScoreModelIdOthThnSal());
		
		for(Long i:idList)
		{
			if(CommonUtils.isObjectListNull(i)){
				continue;
			}
		String checkParameterInScoringIsActive = productMasterRepository.checkcoringIsActiveForPanding(i);
		if(!CommonUtils.isObjectNullOrEmpty(checkParameterInScoringIsActive)) {
			return true;
		}
		}
		}
		return false;
	}

}

