package com.capitaworld.service.loans.service.fundprovider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitaworld.service.auth.model.UserRequest;
import com.capitaworld.service.loans.domain.fundprovider.CarLoanParameter;
import com.capitaworld.service.loans.domain.fundprovider.HomeLoanParameter;
import com.capitaworld.service.loans.domain.fundprovider.LapParameter;
import com.capitaworld.service.loans.domain.fundprovider.PersonalLoanParameter;
import com.capitaworld.service.loans.domain.fundprovider.ProductMaster;
import com.capitaworld.service.loans.domain.fundprovider.TermLoanParameter;
import com.capitaworld.service.loans.domain.fundprovider.WorkingCapitalParameter;
import com.capitaworld.service.loans.model.FpProductDetails;
import com.capitaworld.service.loans.model.MultipleFpPruductRequest;
import com.capitaworld.service.loans.model.ProductDetailsForSp;
import com.capitaworld.service.loans.model.ProductDetailsResponse;
import com.capitaworld.service.loans.model.ProductMasterRequest;
import com.capitaworld.service.loans.model.corporate.AddProductRequest;
import com.capitaworld.service.loans.model.corporate.CorporateProduct;
import com.capitaworld.service.loans.model.corporate.TermLoanParameterRequest;
import com.capitaworld.service.loans.model.corporate.WorkingCapitalParameterRequest;
import com.capitaworld.service.loans.model.retail.CarLoanParameterRequest;
import com.capitaworld.service.loans.model.retail.HomeLoanParameterRequest;
import com.capitaworld.service.loans.model.retail.LapParameterRequest;
import com.capitaworld.service.loans.model.retail.PersonalLoanParameterRequest;
import com.capitaworld.service.loans.model.retail.RetailProduct;
import com.capitaworld.service.loans.repository.fundprovider.CarLoanParameterRepository;
import com.capitaworld.service.loans.repository.fundprovider.GeographicalCountryRepository;
import com.capitaworld.service.loans.repository.fundprovider.HomeLoanParameterRepository;
import com.capitaworld.service.loans.repository.fundprovider.LapParameterRepository;
import com.capitaworld.service.loans.repository.fundprovider.LasParameterRepository;
import com.capitaworld.service.loans.repository.fundprovider.PersonalLoanParameterRepository;
import com.capitaworld.service.loans.repository.fundprovider.ProductMasterRepository;
import com.capitaworld.service.loans.repository.fundprovider.TermLoanParameterRepository;
import com.capitaworld.service.loans.repository.fundprovider.WorkingCapitalParameterRepository;
import com.capitaworld.service.loans.service.common.FundProviderSequenceService;
import com.capitaworld.service.loans.service.fundprovider.CarLoanParameterService;
import com.capitaworld.service.loans.service.fundprovider.HomeLoanParameterService;
import com.capitaworld.service.loans.service.fundprovider.LapLoanParameterService;
import com.capitaworld.service.loans.service.fundprovider.PersonalLoanParameterService;
import com.capitaworld.service.loans.service.fundprovider.ProductMasterService;
import com.capitaworld.service.loans.service.fundprovider.TermLoanParameterService;
import com.capitaworld.service.loans.service.fundprovider.WorkingCapitalParameterService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.enums.LoanType;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;

@Service
@Transactional
public class ProductMasterServiceImpl implements ProductMasterService {
	private static final Logger logger = LoggerFactory.getLogger(ProductMasterServiceImpl.class);
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
	private HomeLoanParameterRepository homeLoanParameterRepository;

	@Autowired
	private CarLoanParameterRepository carLoanParameterRepository;

	@Autowired
	private PersonalLoanParameterRepository personalLoanParameterRepository;

	@Autowired
	private LasParameterRepository lasParameterRepository;

	@Autowired
	private LapParameterRepository lapParameterRepository;

	@Autowired
	private FundProviderSequenceService fundProviderSequenceService;

	@Autowired
	private GeographicalCountryRepository geoCountry;

	@Autowired
	private WorkingCapitalParameterService workingCapitalParameterService;

	@Autowired
	private TermLoanParameterService termLoanParameterService;

	@Autowired
	private HomeLoanParameterService homeLoanParameterService;

	@Autowired
	private CarLoanParameterService carLoanParameterService;

	@Autowired
	private PersonalLoanParameterService personalLoanParameterService;

	@Autowired
	private LapLoanParameterService lapLoanParameterService;

	@Override
	public List<ProductMasterRequest> saveOrUpdate(AddProductRequest addProductRequest) {
		CommonDocumentUtils.startHook(logger, "saveOrUpdate");

		List<ProductMasterRequest> masterRequests = new ArrayList<>();
		List<ProductMaster> masters = new ArrayList<>();
		try {

			if (!CommonUtils.isObjectNullOrEmpty(addProductRequest.getProductMappingId())) {
				productMasterRepository.changeProductName(
						(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
								? addProductRequest.getUserId() : addProductRequest.getClientId()),addProductRequest.getProductMappingId(),
						addProductRequest.getName());
			} else {
				ProductMaster productMaster = null;
				LoanType loanType = LoanType.getById(Integer.parseInt(addProductRequest.getProductId().toString()));

				switch (loanType) {
				case WORKING_CAPITAL:
					productMaster = new WorkingCapitalParameter();
					break;
				case TERM_LOAN:
					productMaster = new TermLoanParameter();
					break;
				case HOME_LOAN:
					productMaster = new HomeLoanParameter();
					break;
				case CAR_LOAN:
					productMaster = new CarLoanParameter();
					break;
				case PERSONAL_LOAN:
					productMaster = new PersonalLoanParameter();
					break;
				case LOAN_AGAINST_PROPERTY:
					productMaster = new LapParameter();
					break;
				default:
					break;
				}
				productMaster.setProductId(addProductRequest.getProductId());
				productMaster.setIsMatched(false);
				productMaster.setName(addProductRequest.getName());
				productMaster.setFpName(addProductRequest.getFpName());
				productMaster.setUserId((CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
						? addProductRequest.getUserId() : addProductRequest.getClientId()));
				productMaster.setCreatedBy((CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
						? addProductRequest.getUserId() : addProductRequest.getClientId()));
				productMaster.setCreatedDate(new Date());
				productMaster.setModifiedBy((CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
						? addProductRequest.getUserId() : addProductRequest.getClientId()));
				productMaster.setIsParameterFilled(false);
				productMaster.setModifiedDate(new Date());
				productMaster.setIsActive(true);
				productMaster.setProductCode(
						fundProviderSequenceService.getFundProviderSequenceNumber(addProductRequest.getProductId()));
				productMasterRepository.save(productMaster);
			}

			masters = productMasterRepository
					.getUserProductListByProduct(
							(CommonUtils.isObjectNullOrEmpty(addProductRequest.getClientId())
									? addProductRequest.getUserId() : addProductRequest.getClientId()),
							addProductRequest.getProductId());

			if (!CommonUtils.isObjectListNull(masters)) {
				for (ProductMaster master : masters) {
					ProductMasterRequest masterRequest = new ProductMasterRequest();
					BeanUtils.copyProperties(master, masterRequest);
					masterRequests.add(masterRequest);
				}
			}
			CommonDocumentUtils.endHook(logger, "saveOrUpdate");
			return masterRequests;
		}

		catch (Exception e) {
			e.printStackTrace();
			logger.error("error while saveOrUpdate", e);
			return null;
		}
	}

	@Override
	public ProductMaster getProductMaster(Long id) {
		// TODO Auto-generated method stub
		return productMasterRepository.findOne(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject checkParameterIsFilled(Long productId) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "checkParameterIsFilled");
		ProductMaster productMaster = productMasterRepository.findOne(productId);
		JSONObject obj = new JSONObject();
		if (CommonUtils.isObjectNullOrEmpty(productMaster)) {
			obj.put("status", false);
			obj.put("message", "Product id is not valid");
			return obj;
		}
		if (!productMaster.getIsActive()) {
			obj.put("status", false);
			obj.put("message", "Requested User is In Active");
			return obj;
		}
		if (!productMaster.getIsParameterFilled()) {
			obj.put("status", false);
			obj.put("message", "Requested user has not filled parameter yet");
			return obj;
		}
		obj.put("status", true);
		obj.put("message", "Show teaser view");
		CommonDocumentUtils.endHook(logger, "checkParameterIsFilled");
		return obj;
	}

	@Override
	public List<ProductMasterRequest> getList(Long userId) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "getList");
		List<ProductMaster> results = productMasterRepository.getUserProductList(userId);
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
	public String getUserNameByApplicationId(Long productId, Long userId) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "getUserNameByApplicationId");
		ProductMaster productMaster = productMasterRepository.getUserProduct(productId, userId);
		if (productMaster != null) {
			CommonDocumentUtils.endHook(logger, "getUserNameByApplicationId");
			return productMaster.getFpName();
		}
		CommonDocumentUtils.endHook(logger, "getUserNameByApplicationId");
		return null;
	}

	@Override
	public Object[] getUserDetailsByPrductId(Long fpMappingId) {
		// TODO Auto-generated method stub
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
	public ProductDetailsResponse getProductDetailsResponse(Long userId) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "getProductDetailsResponse");
		UserResponse usrResponse = usersClient.getLastAccessApplicant(new UsersRequest(userId));
		ProductDetailsResponse productDetailsResponse = new ProductDetailsResponse();
		if(usrResponse != null && usrResponse.getStatus() == 200){
			Long fpMappingId = usrResponse.getId();
			if(fpMappingId != null){
				ProductMaster userProduct = productMasterRepository.getUserProduct(fpMappingId, userId);
				productDetailsResponse.setProductId(userProduct.getProductId());
				productDetailsResponse.setProductMappingId(fpMappingId);
				productDetailsResponse.setMessage("Proposal Details Sent");
				productDetailsResponse.setStatus(HttpStatus.OK.value());
			}else{
				productDetailsResponse.setMessage("Something went wrong");
				productDetailsResponse.setStatus(HttpStatus.BAD_REQUEST.value());	
			}
		}else{
			productDetailsResponse.setMessage("Something went wrong");
			productDetailsResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}
		
		return productDetailsResponse;
	}

	@Override
	public FpProductDetails getProductDetails(Long productMappingId) throws Exception {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "getProductDetails");
		ProductMaster productMaster = productMasterRepository.findOne(productMappingId);
		LoanType loanType = LoanType.getById(productMaster.getProductId());
		FpProductDetails fpProductDetails = new FpProductDetails();
		switch (loanType) {
		case WORKING_CAPITAL:
			productMaster = workingCapitalParameterRepository.findOne(productMappingId);
			break;
		case TERM_LOAN:
			productMaster = termLoanParameterRepository.findOne(productMappingId);
			break;
		case HOME_LOAN:
			productMaster = homeLoanParameterRepository.findOne(productMappingId);
			break;
		case CAR_LOAN:
			productMaster = carLoanParameterRepository.findOne(productMappingId);
			break;
		case PERSONAL_LOAN:
			productMaster = personalLoanParameterRepository.findOne(productMappingId);
			break;
		case LOAN_AGAINST_PROPERTY:
			productMaster = lapParameterRepository.findOne(productMappingId);
			break;
		case LOAN_AGAINST_SHARES_AND_SECUIRITIES:
			productMaster = lasParameterRepository.findOne(productMappingId);
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

	@Override
	public boolean isSelfView(Long fpProductId, Long userId) {
		return productMasterRepository.getUserProduct(fpProductId, userId) != null;
	}

	@Override
	public boolean isProductMatched(Long userId, MultipleFpPruductRequest multipleFpPruductRequest) throws IOException {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "isProductMatched");
		List<ProductDetailsForSp> productDetailsForSps = productMasterRepository.getMatchedAndActiveProduct(userId);
		if (CommonUtils.isListNullOrEmpty(productDetailsForSps)) {
			return false;
		}
		if (!CommonUtils.isObjectNullOrEmpty(multipleFpPruductRequest)) {
			for (Map<String, Object> obj : multipleFpPruductRequest.getDataList()) {
				ProductMasterRequest productMasterRequest = (ProductMasterRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, ProductMasterRequest.class);

				ProductMaster master = productMasterRepository.findOne(productMasterRequest.getId());
				if (!CommonUtils.isObjectNullOrEmpty(master)) {
					// if(master.getId())
					if (!productMasterRequest.getProductId().toString()
							.equals(productDetailsForSps.get(0).getProductId().toString())) {
						CommonDocumentUtils.endHook(logger, "isProductMatched");
						return true;
					}
				}
			}

		}
		CommonDocumentUtils.endHook(logger, "isProductMatched");
		return false;
	}

	@Override
	public int setIsMatchProduct(Long id, Long userId) {
		CommonDocumentUtils.startHook(logger, "setIsMatchProduct");
		// TODO Auto-generated method stub
		CommonDocumentUtils.endHook(logger, "setIsMatchProduct");
		return productMasterRepository.setIsMatchProduct(id, userId);

	}

	@Override
	public List<Object> getListByUserType(Long userId, Integer userType) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "getListByUserType");
		List<ProductMaster> results;
		List<Object> requests = new ArrayList<>();
		if (userType == 1) {
			results = productMasterRepository.getUserRetailProductList(userId);
			if (!CommonUtils.isListNullOrEmpty(results)) {
				for (ProductMaster master : results) {
					if (master.getProductId() == 3) {
						requests.add(homeLoanParameterService.getHomeLoanParameterRequest(master.getId()));
					} else if (master.getProductId() == 7) {
						requests.add(personalLoanParameterService.getPersonalLoanParameterRequest(master.getId()));
					} else if (master.getProductId() == 12) {
						requests.add(carLoanParameterService.getCarLoanParameterRequest(master.getId()));
					} else if (master.getProductId() == 13) {
						requests.add(lapLoanParameterService.getLapParameterRequest(master.getId()));
					}
				}
			}
		} else {

			results = productMasterRepository.getUserCorporateProductList(userId);
			if (!CommonUtils.isListNullOrEmpty(results)) {
				for (ProductMaster master : results) {
					if (master.getProductId() == 1) {
						requests.add(workingCapitalParameterService.getWorkingCapitalParameter(master.getId()));
					} else if (master.getProductId() == 2) {
						requests.add(termLoanParameterService.getTermLoanParameterRequest(master.getId()));
					}
				}
			}
		}

		/*
		 * if (CommonUtils.isListNullOrEmpty(results)) return null; for
		 * (ProductMaster master : results) { ProductMasterRequest request = new
		 * ProductMasterRequest(); BeanUtils.copyProperties(master, request);
		 * request.setIsMatched(productMasterRepository.
		 * getMatchedAndActiveProduct(userId).size() > 0 ? true : false);
		 * requests.add(request); }
		 */
		CommonDocumentUtils.endHook(logger, "getListByUserType");
		return requests;
	}

	@Override
	public Boolean changeStatus(Long fpProductId, Boolean status, Long userId) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "changeStatus");
		try {
			productMasterRepository.changeStatus(userId, fpProductId, status);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			logger.error("error while changeStatus", e);
		}
		CommonDocumentUtils.endHook(logger, "changeStatus");
		return null;
	}

	@Override
	public Boolean saveCorporate(CorporateProduct corporateProduct) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveCorporate");
		if (!CommonUtils.isObjectNullOrEmpty(corporateProduct)) {
			if (!CommonUtils.isObjectNullOrEmpty(corporateProduct.getProductId())) {
				if (corporateProduct.getProductId() == CommonUtils.LoanType.WORKING_CAPITAL.getValue()) {
					WorkingCapitalParameterRequest capitalParameterRequest = new WorkingCapitalParameterRequest();
					BeanUtils.copyProperties(corporateProduct, capitalParameterRequest);
					CommonDocumentUtils.endHook(logger, "saveCorporate");
					return workingCapitalParameterService.saveOrUpdate(capitalParameterRequest);
				} else if (corporateProduct.getProductId() == CommonUtils.LoanType.TERM_LOAN.getValue()) {
					TermLoanParameterRequest loanParameterRequest = new TermLoanParameterRequest();
					BeanUtils.copyProperties(corporateProduct, loanParameterRequest);
					CommonDocumentUtils.endHook(logger, "saveCorporate");
					return termLoanParameterService.saveOrUpdate(loanParameterRequest);
				}
			}
		}
		CommonDocumentUtils.endHook(logger, "saveCorporate");
		return false;
	}

	@Override
	public Boolean saveRetail(RetailProduct retailProduct) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "saveRetail");
		if (!CommonUtils.isObjectNullOrEmpty(retailProduct)) {
			if (!CommonUtils.isObjectNullOrEmpty(retailProduct.getProductId())) {
				if (retailProduct.getProductId() == CommonUtils.LoanType.CAR_LOAN.getValue()) {
					CarLoanParameterRequest carLoanParameterRequest= new CarLoanParameterRequest();
					BeanUtils.copyProperties(retailProduct, carLoanParameterRequest);
					CommonDocumentUtils.endHook(logger, "saveRetail");
					return carLoanParameterService.saveOrUpdate(carLoanParameterRequest);
				} else if (retailProduct.getProductId() == CommonUtils.LoanType.HOME_LOAN.getValue()) {
					HomeLoanParameterRequest homeLoanParameterRequest= new HomeLoanParameterRequest();
					BeanUtils.copyProperties(retailProduct, homeLoanParameterRequest);
					CommonDocumentUtils.endHook(logger, "saveRetail");
					return homeLoanParameterService.saveOrUpdate(homeLoanParameterRequest);
				}
				else if (retailProduct.getProductId() == CommonUtils.LoanType.PERSONAL_LOAN.getValue()) {
					PersonalLoanParameterRequest personalLoanParameterRequest= new PersonalLoanParameterRequest();
					BeanUtils.copyProperties(retailProduct, personalLoanParameterRequest);
					CommonDocumentUtils.endHook(logger, "saveRetail");
					return personalLoanParameterService.saveOrUpdate(personalLoanParameterRequest);
				}
				else if (retailProduct.getProductId() == CommonUtils.LoanType.LAP_LOAN.getValue()) {
					LapParameterRequest lapParameterRequest= new LapParameterRequest();
					BeanUtils.copyProperties(retailProduct, lapParameterRequest);
					CommonDocumentUtils.endHook(logger, "saveRetail");
					return lapLoanParameterService.saveOrUpdate(lapParameterRequest);
				}
			}
		}
		CommonDocumentUtils.endHook(logger, "saveRetail");
		return false;
	}

	@Override
	public ProductMasterRequest lastAccessedProduct(Long userId) {
		// TODO Auto-generated method stub
		CommonDocumentUtils.startHook(logger, "lastAccessedProduct");
		ProductMasterRequest productMasterRequest=new ProductMasterRequest();
		BeanUtils.copyProperties(productMasterRepository.getLastAccessedProduct(userId), productMasterRequest);
		CommonDocumentUtils.endHook(logger, "lastAccessedProduct");
		return  productMasterRequest;
	}

}