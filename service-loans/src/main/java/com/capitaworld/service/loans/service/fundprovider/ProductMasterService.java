package com.capitaworld.service.loans.service.fundprovider;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONObject;

import com.capitaworld.service.loans.domain.fundprovider.ProductMaster;
import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.FpProductDetails;
import com.capitaworld.service.loans.model.MultipleFpPruductRequest;
import com.capitaworld.service.loans.model.ProductDetailsForSp;
import com.capitaworld.service.loans.model.ProductDetailsResponse;
import com.capitaworld.service.loans.model.ProductMasterRequest;
import com.capitaworld.service.loans.model.WorkflowData;
import com.capitaworld.service.loans.model.colending.FpProductRoiResponse;
import com.capitaworld.service.loans.model.common.ChatDetails;
import com.capitaworld.service.loans.model.corporate.AddProductRequest;
import com.capitaworld.service.loans.model.corporate.CorporateProduct;
public interface ProductMasterService {
	public Boolean saveOrUpdate(AddProductRequest addProductRequest, Long userOrgId);

	public ProductMaster getProductMaster(Long id);
	
	public Object getProductMasterWithAllData(Long id,Integer stage,Long role,Long userId);

	public List<ProductMasterRequest> getList(Long userId,Long userOrgId);
	
	public List<ProductMasterRequest> getActiveInActiveList(Long userId,Long userOrgId,Long businessTypeId);
//	public List<ProductMasterRequest> getActiveInActiveList(Long userId,Long userOrgId);

	public List<ProductMasterRequest> getListByUserType(Long userId,Integer userType,Integer stage,Long userOrgId,Integer productId);

	public String getUserNameByApplicationId(Long productId, Long userId);

	public List<ProductDetailsForSp> getProductDetailsByUserIdList(Long userId);

	public Object[] getUserDetailsByPrductId(Long fpMappingId);

	public ProductDetailsResponse getProductDetailsResponse(Long userId, Long userOrgId);

	public FpProductDetails getProductDetails(Long productMappingId) throws LoansException;

	public boolean isSelfView(Long fpProductId, Long userId);
	
	public Boolean changeStatus(Long fpProductId,Boolean status, Long userId,Integer stage);

	public boolean isProductMatched(Long userId, MultipleFpPruductRequest multipleFpPruductRequest) throws IOException;

	public int setIsMatchProduct(Long id, Long userId);

	public JSONObject checkParameterIsFilled(Long productId);
	
	public Boolean saveCorporate(CorporateProduct corporateProduct);
	
	public ProductMasterRequest lastAccessedProduct(Long userId);
	
	public List<ChatDetails> getChatListByFpMappingId(Long applicationId);
	
	public boolean isProductActive(Long productId);
	
	public List<ProductMasterRequest> getProductByOrgId(Long orgd);

	public Boolean saveCorporateMasterFromTemp(Long mappingId, Integer roleId) throws LoansException;

	/**
	 * @param corporateProduct
	 * @return
	 */
	public Boolean saveCorporateInTemp(CorporateProduct corporateProduct);
	
	public Boolean clickOnWorkFlowButton(WorkflowData workflowData);

	public List<ProductMasterRequest> getApprovedListByProductType(Long userId, Integer parseInt, Integer businessId,Long userOrgId);

	public Long createJobId(Long userId);
	
	public Boolean changeStatusWithWorkFlow(WorkflowData workflowData);

	public FpProductRoiResponse getMinMaxRoiFromFpProductId(Long fpProductId);
}
