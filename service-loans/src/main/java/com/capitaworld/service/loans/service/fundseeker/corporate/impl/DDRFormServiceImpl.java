package com.capitaworld.service.loans.service.fundseeker.corporate.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.capitaworld.service.loans.domain.fundseeker.ddr.*;
import com.capitaworld.service.loans.model.ddr.*;
import com.capitaworld.service.loans.repository.fundseeker.ddr.*;
import com.capitaworld.service.oneform.enums.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.capitaworld.cibil.api.model.CibilRequest;
import com.capitaworld.cibil.api.model.CibilResponse;
import com.capitaworld.service.dms.client.DMSClient;
import com.capitaworld.service.dms.model.DocumentResponse;
import com.capitaworld.service.loans.domain.PincodeData;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.capitaworld.service.loans.domain.fundseeker.corporate.AssetsDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.AssociatedConcernDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.BalanceSheetDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.DirectorBackgroundDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.ExistingProductDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.FinancialArrangementsDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.LiabilitiesDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.OperatingStatementDetails;
import com.capitaworld.service.loans.domain.fundseeker.corporate.OwnershipDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.ProfitibilityStatementDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PromotorBackgroundDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.ProposedProductDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.SecurityCorporateDetail;
import com.capitaworld.service.loans.model.Address;
import com.capitaworld.service.loans.model.AssociatedConcernDetailRequest;
import com.capitaworld.service.loans.model.DirectorBackgroundDetailRequest;
import com.capitaworld.service.loans.model.DirectorBackgroundDetailResponse;
import com.capitaworld.service.loans.model.ExistingProductDetailRequest;
import com.capitaworld.service.loans.model.FinancialArrangementDetailResponseString;
import com.capitaworld.service.loans.model.OwnershipDetailRequest;
import com.capitaworld.service.loans.model.OwnershipDetailResponse;
import com.capitaworld.service.loans.model.PromotorBackgroundDetailRequest;
import com.capitaworld.service.loans.model.PromotorBackgroundDetailResponse;
import com.capitaworld.service.loans.model.ProposedProductDetailRequest;
import com.capitaworld.service.loans.model.SecurityCorporateDetailRequest;
import com.capitaworld.service.loans.model.common.DocumentUploadFlagRequest;
import com.capitaworld.service.loans.repository.PincodeDataRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AssetsDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.AssociatedConcernDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.BalanceSheetDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.DirectorBackgroundDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ExistingProductDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.FinancialArrangementDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LiabilitiesDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.OperatingStatementDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.OwnershipDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ProfitibilityStatementDetailRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.PromotorBackgroundDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.ProposedProductDetailsRepository;
import com.capitaworld.service.loans.repository.fundseeker.corporate.SecurityCorporateDetailsRepository;
import com.capitaworld.service.loans.service.fundseeker.corporate.AssociatedConcernDetailService;
import com.capitaworld.service.loans.service.fundseeker.corporate.DDRFormService;
import com.capitaworld.service.loans.service.fundseeker.corporate.ExistingProductDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.PromotorBackgroundDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.ProposedProductDetailsService;
import com.capitaworld.service.loans.service.fundseeker.corporate.SecurityCorporateDetailsService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import com.capitaworld.service.loans.utils.CommonUtils.DDRFinancialSummaryFields;
import com.capitaworld.service.loans.utils.CommonUtils.DDRFinancialSummaryToBeFields;
import com.capitaworld.service.loans.utils.CommonUtils.DDRFrames;
import com.capitaworld.service.loans.utils.MultipleJSONObjectHelper;
import com.capitaworld.service.oneform.client.OneFormClient;
import com.capitaworld.service.oneform.model.MasterResponse;
import com.capitaworld.service.oneform.model.OneFormResponse;
import com.capitaworld.service.users.client.UsersClient;
import com.capitaworld.service.users.model.UserResponse;
import com.capitaworld.service.users.model.UsersRequest;

@Service
@Transactional
public class DDRFormServiceImpl implements DDRFormService {

	private static final Logger logger = LoggerFactory.getLogger(DDRFormServiceImpl.class);

	@Autowired
	private DDRFormDetailsRepository ddrFormDetailsRepository;

	@Autowired
	private DDRAuthorizedSignDetailsRepository authorizedSignDetailsRepository;

	@Autowired
	private DDRCreditCardDetailsRepository cardDetailsRepository;

	@Autowired
	private DDRCreditorsDetailsRepository creditorsDetailsRepository;

	@Autowired
	private DDROtherBankLoanDetailsRepository bankLoanDetailsRepository;

	@Autowired
	private DDRRelWithDbsDetailsRepository dbsDetailsRepository;

	@Autowired
	private DDRVehiclesOwnedDetailsRepository vehiclesOwnedDetailsRepository;

	@Autowired
	private DDROfficeDetailsRepository ddrOfficeDetailsRepository;

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private UsersClient usersClient;

	@Autowired
	private PromotorBackgroundDetailsService promotorBackgroundDetailsService;
	@Autowired
	private PromotorBackgroundDetailsRepository promotorBackgroundDetailsRepository;

	@Autowired
	private DirectorBackgroundDetailsRepository directorBackgroundDetailsRepository;

	@Autowired
	private OwnershipDetailsRepository ownershipDetailsRepository;

	@Autowired
	private FinancialArrangementDetailsRepository financialArrangementDetailsRepository;

	@Autowired
	private ProposedProductDetailsService proposedProductDetailsService;
	@Autowired
	private ProposedProductDetailsRepository proposedProductDetailsRepository;

	@Autowired
	private ExistingProductDetailsService existingProductDetailsService;
	@Autowired
	private ExistingProductDetailsRepository existingProductDetailsRepository;

	@Autowired
	private DDRFinancialSummaryRepository financialSummaryRepository;

	@Autowired
	private AssociatedConcernDetailService associatedConcernDetailService;
	@Autowired
	private AssociatedConcernDetailRepository associatedConcernDetailRepository;

	@Autowired
	private DDRFamilyDirectorsDetailsRepository familyDirectorsDetailsRepository;

	@Autowired
	private OperatingStatementDetailsRepository operatingStatementDetailsRepository;

	@Autowired
	private ProfitibilityStatementDetailRepository profitibilityStatementDetailRepository;

	@Autowired
	private AssetsDetailsRepository assetsDetailsRepository;

	@Autowired
	private LiabilitiesDetailsRepository liabilitiesDetailsRepository;

	@Autowired
	private BalanceSheetDetailRepository balanceSheetDetailRepository;

	@Autowired
	private SecurityCorporateDetailsService securityCorporateDetailsService;
	@Autowired
	private SecurityCorporateDetailsRepository securityCorporateDetailsRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private DDRExistingBankerDetailsRepository ddrExistingBankerDetailsRepository;

	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private PincodeDataRepository pincodeDataRepository;
	
	@Value("${com.capitaworld.bob.api.url}")
	private String bobUrl;
	
	@Value("${com.capitaworld.bob.api.header.name}")
	private String headerName;
	
	@Value("${com.capitaworld.bob.api.header.auth.key}")
	private String headerAuthKey;
	

	@Override
	public DDRRequest getMergeDDR(Long appId, Long userId) {

		// SET DDR AUTO FIELD DATA
		DDRRequest dDRRequest = getCombinedOneFormDetails(userId, appId);
		if (CommonUtils.isObjectNullOrEmpty(dDRRequest)) {
			logger.info(
					"ONEFORM DETAILS NULL OR EMPTY ---->" + appId + "---------AND USERID ------------------>" + userId);
			return dDRRequest;
		}

		DDRFormDetails dDRFormDetails = ddrFormDetailsRepository.getByAppIdAndIsActive(appId);
		if (!CommonUtils.isObjectNullOrEmpty(dDRFormDetails)) {
			Long ddrFormId = dDRFormDetails.getId();
			BeanUtils.copyProperties(dDRFormDetails, dDRRequest);

			// SET TO BE FILED DATA
			dDRRequest.setOutsideLoansString(CommonUtils.checkString(dDRFormDetails.getOutsideLoans()));
			dDRRequest.setLoansFromFamilyMembersRelativeString(
					CommonUtils.checkString(dDRFormDetails.getLoansFromFamilyMembersRelative()));
			dDRRequest.setdDRAuthSignDetailsList(getAuthorizedSignDetails(ddrFormId));
			dDRRequest.setdDRCreditCardDetailsList(getCreditCardDetails(ddrFormId));
			dDRRequest.setdDRCreditorsDetailsList(getCreaditorsDetails(ddrFormId));
			dDRRequest.setdDROperatingOfficeList(getOfficeDetails(ddrFormId, DDRFrames.OPERATING_OFFICE.getValue()));
			dDRRequest.setdDRRegisteredOfficeList(getOfficeDetails(ddrFormId, DDRFrames.REGISTERED_OFFICE.getValue()));
			dDRRequest.setdDROtherBankLoanDetailsList(getOtherBankLoanDetails(ddrFormId));
			// dDRFormDetailsRequest.setdDRRelWithDbsDetailsList(getRelWithDBSDetails(ddrFormId));
			dDRRequest.setdDRVehiclesOwnedDetailsList(getVehiclesOwnedDetails(ddrFormId));
			dDRRequest.setdDRFinancialSummaryList(getFinancialSummary(ddrFormId));
			dDRRequest.setdDRFamilyDirectorsList(getFamilyDirectorsDetails(ddrFormId, appId, userId, true));
			dDRRequest.setExistingBankerDetailList(getExistingBankerDetails(ddrFormId, appId, userId, true));

			dDRRequest.setProvisionalTotalSales(getCMATotalSalesByAppIdAndYear(appId, "2018"));
			dDRRequest.setLastYearTotalSales(getCMATotalSalesByAppIdAndYear(appId, "2017"));
			dDRRequest.setLastToLastYearTotalSales(getCMATotalSalesByAppIdAndYear(appId, "2016"));
			dDRRequest.setCurrency(getCurrency(appId, userId));
		} else {
			dDRRequest.setdDRFamilyDirectorsList(getFamilyDirectorsDetails(null, appId, userId, true));
			dDRRequest.setExistingBankerDetailList(getExistingBankerDetails(null, appId, userId, true));
			dDRRequest.setdDRFinancialSummaryList(getFinancialSummary(null));
			dDRRequest.setCurrency(getCurrency(appId, userId));
		}
		return dDRRequest;
	}

	@Override
	public void saveMergeDDR(DDRRequest dDRRequest) throws Exception {
		Long userId = dDRRequest.getUserId();

		try {
			DDRFormDetails dDRFormDetails = ddrFormDetailsRepository
					.getByAppIdAndIsActive(dDRRequest.getApplicationId());
			if (CommonUtils.isObjectNullOrEmpty(dDRFormDetails)) {
				logger.info("DDR ===============> New DDR Form Saving ------------------------->");
				dDRFormDetails = new DDRFormDetails();
				BeanUtils.copyProperties(dDRRequest, dDRFormDetails, "id");
				dDRFormDetails.setIsActive(true);
				dDRFormDetails.setCreatedBy(userId);
				dDRFormDetails.setCreatedDate(new Date());
			} else {
				logger.info("DDR ===============> DDR Form Updating ------------------------->" + dDRRequest.getId());
				BeanUtils.copyProperties(dDRRequest, dDRFormDetails, "id", "applicationId", "userId", "isActive");
				dDRFormDetails.setModifyBy(userId);
				dDRFormDetails.setModifyDate(new Date());
			}
			dDRFormDetails = ddrFormDetailsRepository.save(dDRFormDetails);

			// SAVE AUTO FILEDS DATA
			if (CommonUtils.UsersRoles.MAKER.equals(dDRRequest.getRoleId())
					|| CommonUtils.UsersRoles.FP_MAKER.equals(dDRRequest.getRoleId())) {

				CorporateApplicantDetail applicantDetail = corporateApplicantDetailRepository
						.getByApplicationIdAndIsAtive(dDRRequest.getApplicationId());
				if (!CommonUtils.isObjectNullOrEmpty(applicantDetail) && !CommonUtils.isObjectNullOrEmpty(dDRRequest)) {
					if (!CommonUtils.isObjectNullOrEmpty(dDRRequest.getRegOfficeAddress())) {
						// regOfficeAddress
						Address regOfficeAdd = dDRRequest.getRegOfficeAddress();
						applicantDetail.setRegisteredPremiseNumber(regOfficeAdd.getPremiseNumber());
						applicantDetail.setRegisteredLandMark(regOfficeAdd.getLandMark());
						applicantDetail.setRegisteredStreetName(regOfficeAdd.getStreetName());
						applicantDetail.setRegisteredCountryId(regOfficeAdd.getCountryId());
						applicantDetail.setRegisteredStateId(regOfficeAdd.getStateId());
						applicantDetail.setRegisteredCityId(regOfficeAdd.getCityId());
						// corpOfficeAddress
						Address corpOfficeAdd = dDRRequest.getCorpOfficeAddress();
						applicantDetail.setAdministrativePremiseNumber(corpOfficeAdd.getPremiseNumber());
						applicantDetail.setAdministrativeLandMark(corpOfficeAdd.getLandMark());
						applicantDetail.setAdministrativeStreetName(corpOfficeAdd.getStreetName());
						applicantDetail.setAdministrativeCountryId(corpOfficeAdd.getCountryId());
						applicantDetail.setAdministrativeStateId(corpOfficeAdd.getStateId());
						applicantDetail.setAdministrativeCityId(corpOfficeAdd.getCityId());
						// aboutMe
						applicantDetail.setAboutUs(dDRRequest.getAboutMe());
						corporateApplicantDetailRepository.save(applicantDetail);
					}

					// Existing - Application proposedProductDetailList and
					// existingProductDetailList
					List<ProposedProductDetailRequest> proProductList = dDRRequest.getProposedProductDetailList();
					for (ProposedProductDetailRequest proProduct : proProductList) {
						ProposedProductDetail proposedProductDetail = null;
						if (!CommonUtils.isObjectNullOrEmpty(proProduct.getId())) {
							proposedProductDetail = proposedProductDetailsRepository
									.findByIdAndIsActive(proProduct.getId(), true);
						}
						if (CommonUtils.isObjectNullOrEmpty(proposedProductDetail)) {
							proposedProductDetail = new ProposedProductDetail();
							proposedProductDetail.setCreatedBy(userId);
							proposedProductDetail.setCreatedDate(new Date());
							proposedProductDetail.setIsActive(true);
							proposedProductDetail
									.setApplicationId(new LoanApplicationMaster(dDRRequest.getApplicationId()));
						} else {
							proposedProductDetail.setIsActive(proProduct.getIsActive());
							proposedProductDetail.setModifiedBy(userId);
							proposedProductDetail.setModifiedDate(new Date());
						}
						proposedProductDetail.setApplication(proProduct.getApplication());
						proposedProductDetail.setProduct(proProduct.getProduct());
						proposedProductDetailsRepository.save(proposedProductDetail);
					}

					if (!CommonUtils.isListNullOrEmpty(dDRRequest.getExistingProductDetailList())) {
						List<ExistingProductDetailRequest> existingProductDetailList = dDRRequest
								.getExistingProductDetailList();

						for (ExistingProductDetailRequest existingPro : existingProductDetailList) {
							ExistingProductDetail existingProductDetail = null;
							if (!CommonUtils.isObjectNullOrEmpty(existingPro.getId())) {
								existingProductDetail = existingProductDetailsRepository
										.findByIdAndIsActive(existingPro.getId(), true);
							}
							if (CommonUtils.isObjectNullOrEmpty(existingProductDetail)) {
								existingProductDetail = new ExistingProductDetail();
								existingProductDetail.setCreatedBy(userId);
								existingProductDetail.setCreatedDate(new Date());
								existingProductDetail.setIsActive(true);
								existingProductDetail
										.setApplicationId(new LoanApplicationMaster(dDRRequest.getApplicationId()));
							} else {
								existingProductDetail.setIsActive(existingPro.getIsActive());
								existingProductDetail.setModifiedBy(userId);
								existingProductDetail.setModifiedDate(new Date());
							}
							existingProductDetail.setApplication(existingPro.getApplication());
							existingProductDetail.setProduct(existingPro.getProduct());
							existingProductDetailsRepository.save(existingProductDetail);
						}
					}

					// promoBackRespList
					if (!CommonUtils.isListNullOrEmpty(dDRRequest.getPromoBackRespList())) {
						List<PromotorBackgroundDetailRequest> promoBackRespList = dDRRequest.getPromoBackRespList();

						for (PromotorBackgroundDetailRequest promoBackReq : promoBackRespList) {
							PromotorBackgroundDetail promBack = null;
							if (!CommonUtils.isObjectNullOrEmpty(promoBackReq.getId())) {
								promBack = promotorBackgroundDetailsRepository.findByIdAndIsActive(promoBackReq.getId(),
										true);
							}

							if (CommonUtils.isObjectNullOrEmpty(promBack)) {
								promBack = new PromotorBackgroundDetail();
								promBack.setCreatedBy(userId);
								promBack.setCreatedDate(new Date());
								promBack.setApplicationId(new LoanApplicationMaster(dDRRequest.getApplicationId()));
								promBack.setIsActive(true);
							} else {
								promBack.setIsActive(promoBackReq.getIsActive());
								promBack.setModifiedBy(userId);
								promBack.setModifiedDate(new Date());
							}
							promBack.setRelationshipType(promoBackReq.getRelationshipType());
							promBack.setAddress(promoBackReq.getAddress());
							promBack.setMobile(promoBackReq.getMobile());
							promBack.setDesignation(promoBackReq.getDesignation());
							promBack.setTotalExperience(promoBackReq.getTotalExperience());
							promBack.setNetworth(promoBackReq.getNetworth());
							promBack.setAppointmentDate(promoBackReq.getAppointmentDate());
							promotorBackgroundDetailsRepository.save(promBack);
						}
					}
					// dDRFamilyDirectorsList ---> directorBackReq
					if (!CommonUtils.isListNullOrEmpty(dDRRequest.getdDRFamilyDirectorsList())) {
						List<DDRFamilyDirectorsDetailsRequest> ddrFamilyDirReqList = dDRRequest
								.getdDRFamilyDirectorsList();
						for (DDRFamilyDirectorsDetailsRequest ddrFamilyDirReq : ddrFamilyDirReqList) {
							if (!CommonUtils.isObjectNullOrEmpty(ddrFamilyDirReq.getDirectorBackReq())) {
								DirectorBackgroundDetailRequest directorBackReq = ddrFamilyDirReq.getDirectorBackReq();// FIND
																														// DIRECTOR
																														// OBJECT

								DirectorBackgroundDetail dirBack = null;
								if (!CommonUtils.isObjectNullOrEmpty(directorBackReq.getId())) {
									dirBack = directorBackgroundDetailsRepository
											.findByIdAndIsActive(directorBackReq.getId(), true);
								}

								if (CommonUtils.isObjectNullOrEmpty(dirBack)) {
									dirBack = new DirectorBackgroundDetail();
									dirBack.setCreatedBy(userId);
									dirBack.setCreatedDate(new Date());
									dirBack.setApplicationId(new LoanApplicationMaster(dDRRequest.getApplicationId()));
									dirBack.setIsActive(true);
								} else {
									dirBack.setIsActive(directorBackReq.getIsActive());
									dirBack.setModifiedBy(userId);
									dirBack.setModifiedDate(new Date());
								}
								dirBack.setRelationshipType(directorBackReq.getRelationshipType());
								dirBack.setDesignation(directorBackReq.getDesignation());
								dirBack.setAddress(directorBackReq.getAddress());
								dirBack.setMobile(directorBackReq.getMobile());
								dirBack.setTotalExperience(directorBackReq.getTotalExperience());
								dirBack.setNetworth(directorBackReq.getNetworth());
								dirBack.setAppointmentDate(directorBackReq.getAppointmentDate());
								dirBack.setPremiseNumber(directorBackReq.getPremiseNumber());
								dirBack.setStreetName(directorBackReq.getStreetName());
								dirBack.setLandmark(directorBackReq.getLandmark());
								directorBackgroundDetailsRepository.save(dirBack);
							}
						}
					}

					// ownershipRespList
					if (!CommonUtils.isListNullOrEmpty(dDRRequest.getOwnershipReqList())) {
						List<OwnershipDetailRequest> ownershipReqList = dDRRequest.getOwnershipReqList();

						for (OwnershipDetailRequest ownershipReq : ownershipReqList) {
							OwnershipDetail ownership = null;
							if (!CommonUtils.isObjectNullOrEmpty(ownershipReq.getId())) {
								ownership = ownershipDetailsRepository.findByIdAndIsActive(ownershipReq.getId(), true);
							}

							if (CommonUtils.isObjectNullOrEmpty(ownership)) {
								ownership = new OwnershipDetail();
								ownership.setCreatedBy(userId);
								ownership.setCreatedDate(new Date());
								ownership.setApplicationId(new LoanApplicationMaster(dDRRequest.getApplicationId()));
								ownership.setIsActive(true);
							} else {
								ownership.setIsActive(ownershipReq.getIsActive());
								ownership.setModifiedBy(userId);
								ownership.setModifiedDate(new Date());
							}
							ownership.setStackPercentage(ownershipReq.getStackPercentage());
							ownershipDetailsRepository.save(ownership);
						}
					}

					// associatedConcernDetailList
					if (!CommonUtils.isListNullOrEmpty(dDRRequest.getAssociatedConcernDetailList())) {
						List<AssociatedConcernDetailRequest> assConcernDetailList = dDRRequest
								.getAssociatedConcernDetailList();

						for (AssociatedConcernDetailRequest assConcernDetailReq : assConcernDetailList) {
							AssociatedConcernDetail assConcernDetail = null;
							if (!CommonUtils.isObjectNullOrEmpty(assConcernDetailReq.getId())) {
								assConcernDetail = associatedConcernDetailRepository
										.findByIdAndIsActive(assConcernDetailReq.getId(), true);
							}

							if (CommonUtils.isObjectNullOrEmpty(assConcernDetail)) {
								assConcernDetail = new AssociatedConcernDetail();
								assConcernDetail.setCreatedBy(userId);
								assConcernDetail.setCreatedDate(new Date());
								assConcernDetail
										.setApplicationId(new LoanApplicationMaster(dDRRequest.getApplicationId()));
								assConcernDetail.setIsActive(true);
							} else {
								assConcernDetail.setIsActive(assConcernDetailReq.getIsActive());
								assConcernDetail.setModifiedBy(userId);
								assConcernDetail.setModifiedDate(new Date());
							}
							assConcernDetail.setNatureActivity(assConcernDetailReq.getNatureActivity());
							assConcernDetail.setInvestedAmount(assConcernDetailReq.getInvestedAmount());
							assConcernDetail.setNatureActivity(assConcernDetailReq.getNatureActivity());
							assConcernDetail.setNameOfDirector(assConcernDetailReq.getNameOfDirector());
							assConcernDetail.setBriefDescription(assConcernDetailReq.getBriefDescription());
							assConcernDetail.setTurnOverFirstYear(assConcernDetailReq.getTurnOverFirstYear());
							assConcernDetail.setTurnOverSecondYear(assConcernDetailReq.getTurnOverSecondYear());
							assConcernDetail.setTurnOverThirdYear(assConcernDetailReq.getTurnOverThirdYear());
							assConcernDetail.setProfitPastOneYear(assConcernDetailReq.getProfitPastOneYear());
							assConcernDetail.setProfitPastTwoYear(assConcernDetailReq.getProfitPastTwoYear());
							assConcernDetail.setProfitPastThreeYear(assConcernDetailReq.getProfitPastThreeYear());
							associatedConcernDetailRepository.save(assConcernDetail);
						}
					}

					// securityCorporateDetailList securityCorporateDetailsRepository
					if (!CommonUtils.isListNullOrEmpty(dDRRequest.getSecurityCorporateDetailList())) {
						List<SecurityCorporateDetailRequest> securityCorporateDetailList = dDRRequest
								.getSecurityCorporateDetailList();

						for (SecurityCorporateDetailRequest securityCorDetailReq : securityCorporateDetailList) {
							SecurityCorporateDetail securityCorporateDetail = null;
							if (!CommonUtils.isObjectNullOrEmpty(securityCorDetailReq.getId())) {
								securityCorporateDetail = securityCorporateDetailsRepository
										.findByIdAndIsActive(securityCorDetailReq.getId(), true);
							}

							if (CommonUtils.isObjectNullOrEmpty(securityCorporateDetail)) {
								securityCorporateDetail = new SecurityCorporateDetail();
								securityCorporateDetail.setCreatedBy(userId);
								securityCorporateDetail.setCreatedDate(new Date());
								securityCorporateDetail
										.setApplicationId(new LoanApplicationMaster(dDRRequest.getApplicationId()));
								securityCorporateDetail.setIsActive(true);
							} else {
								securityCorporateDetail.setIsActive(securityCorDetailReq.getIsActive());
								securityCorporateDetail.setModifiedBy(userId);
								securityCorporateDetail.setModifiedDate(new Date());
							}
							securityCorporateDetail.setAmount(securityCorDetailReq.getAmount());
							securityCorporateDetail
									.setPrimarySecurityName(securityCorDetailReq.getPrimarySecurityName());
							securityCorporateDetailsRepository.save(securityCorporateDetail);
						}
					}

				}

			} else {
				logger.info("ROLE ID NOT MATCHES --------------------------------------------------------->"
						+ dDRRequest.getRoleId());
			}

			// SAVE ALL LIST DATA
			saveAuthorizedSignDetails(dDRRequest.getdDRAuthSignDetailsList(), userId, dDRFormDetails.getId());
			saveCreaditorsDetails(dDRRequest.getdDRCreditorsDetailsList(), userId, dDRFormDetails.getId());
			saveCreditCardDetails(dDRRequest.getdDRCreditCardDetailsList(), userId, dDRFormDetails.getId());
			saveOfficeDetails(dDRRequest.getdDROperatingOfficeList(), userId, DDRFrames.OPERATING_OFFICE.getValue(),
					dDRFormDetails.getId());
			saveOfficeDetails(dDRRequest.getdDRRegisteredOfficeList(), userId, DDRFrames.REGISTERED_OFFICE.getValue(),
					dDRFormDetails.getId());
			saveOtherBankLoanDetails(dDRRequest.getdDROtherBankLoanDetailsList(), userId, dDRFormDetails.getId());
			// saveRelWithDBSDetails(ddrFormDetailsRequest.getdDRRelWithDbsDetailsList(),
			// userId,dDRFormDetails.getId());
			saveVehiclesOwnedDetails(dDRRequest.getdDRVehiclesOwnedDetailsList(), userId, dDRFormDetails.getId());
			saveFinancialSummary(dDRRequest.getdDRFinancialSummaryList(), userId, dDRFormDetails.getId());
			saveFamilyDirectorsDetails(dDRRequest.getdDRFamilyDirectorsList(), userId, dDRFormDetails.getId());
			saveExistingBankerDetails(dDRRequest.getExistingBankerDetailList(), userId, dDRFormDetails.getId());
			logger.info("DDR ===============> DDR Form Saved Successfully in Service-----------------> "
					+ dDRFormDetails.getId());
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception while saving ddr form");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	/**
	 * CHECK CUSTOMER DETAILS FILLED OR NOT IN TEASER VIEW WHILE APPROVE APPLICATION
	 * FOR BOB BANK
	 */
	@Override
	public DDRCustomerRequest checkCustomerDetailFilled(Long applicationId) {
		DDRCustomerRequest customerRequest = new DDRCustomerRequest();
		customerRequest.setApplicationId(applicationId);
		DDRFormDetails dDRFormDetails = ddrFormDetailsRepository.getByAppIdAndIsActive(applicationId);
		if (!CommonUtils.isObjectNullOrEmpty(dDRFormDetails)) {
			customerRequest.setDdrFormId(dDRFormDetails.getId());
			customerRequest.setCustomerId(dDRFormDetails.getCustomerId());
			customerRequest.setCustomerName(dDRFormDetails.getCustomerName());
			customerRequest.setIsFilled(CommonUtils.isObjectNullOrEmpty(dDRFormDetails.getCustomerId()));
		} else {
			customerRequest.setIsFilled(false);
		}
		return customerRequest;
	}

	/**
	 * SAVE CUSTOMER DETAILS IN TEASER VIEW WHILE APPROVE APPLICATION FOR BOB BANK
	 */
	@Override
	public Boolean saveCustomerDetailFilled(DDRCustomerRequest customerRequest) {
		DDRFormDetails dDRFormDetails = ddrFormDetailsRepository
				.getByAppIdAndIsActive(customerRequest.getApplicationId());
		if (!CommonUtils.isObjectNullOrEmpty(dDRFormDetails)) {
			dDRFormDetails.setCustomerId(customerRequest.getCustomerId());
			dDRFormDetails.setCustomerName(customerRequest.getCustomerName());
			dDRFormDetails.setModifyBy(customerRequest.getUserId());
			dDRFormDetails.setModifyDate(new Date());
			ddrFormDetailsRepository.save(dDRFormDetails);
			return true;
		}
		return false;
	}

	public DDRRequest getCombinedOneFormDetails(Long userId, Long applicationId) {

		logger.info("Enter in get one form details service");
		DDRRequest response = new DDRRequest();
		LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.getById(applicationId);

		if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster)) {
			logger.info("Data not found by this application id -------------------> " + applicationId);
			return null;
		}
		response.setApprovedDate(loanApplicationMaster.getApprovedDate());
		response.setDdrStatusId(loanApplicationMaster.getDdrStatusId());

		// ---------------------------------------------------PROFILE
		// ------------------------------------------------------------------------
		logger.info("Before Call Corporate Profile UserId is :- " + userId);
		CorporateApplicantDetail applicantDetail = corporateApplicantDetailRepository
				.getByApplicationIdAndIsAtive(applicationId);
		if (CommonUtils.isObjectNullOrEmpty(applicantDetail)) {
			logger.info("Corporate Profile Details NUll or Empty!! ----------------->" + applicationId);
			return response;
		}
		// GET ORGANIZATION TYPE
		try {
			Long orgId = loanApplicationMaster.getNpOrgId();
			if (!CommonUtils.isObjectNullOrEmpty(orgId)) {
				logger.info("OrgId", orgId);
				String orgName = CommonUtils.getOrganizationName(orgId);
				response.setOrgName(orgName);
				logger.info("Org name", orgName);
			} else {
				logger.info("No org Id found");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Error while getting user org id", e);
		}
		// ORGANIZATION NAME :- LINENO:6
		response.setNameOfBorrower(applicantDetail.getOrganisationName());
		response.setCurrency(getCurrency(applicationId, userId));
		// GET REGISTERED ADDRESS :- LINENO:7
		Address address = new Address();
		address.setPremiseNumber(applicantDetail.getRegisteredPremiseNumber());
		address.setStreetName(applicantDetail.getRegisteredStreetName());
		address.setLandMark(applicantDetail.getRegisteredLandMark());
		address.setCountryId(applicantDetail.getRegisteredCountryId());
		address.setStateId(applicantDetail.getRegisteredStateId());
		address.setCityId(applicantDetail.getRegisteredCityId());
		address.setPincode(applicantDetail.getRegisteredPincode());
		if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredDistMappingId())) {
			PincodeData pincodeData = pincodeDataRepository.findOne(applicantDetail.getRegisteredDistMappingId());
			if (!CommonUtils.isObjectNullOrEmpty(pincodeData)) {
				address.setVillage(pincodeData.getOfficeName());
				address.setDistrict(pincodeData.getDistrictName());
				address.setSubDistrict(pincodeData.getTaluka());
				address.setDistrictMappingId(applicantDetail.getRegisteredDistMappingId());
			}
		}
		response.setRegOfficeAddress(address);

		// GET ADMINISRATIVE (Corporate Office) ADDRESS :- LINENO:9
		address = new Address();
		address.setPremiseNumber(applicantDetail.getAdministrativePremiseNumber());
		address.setStreetName(applicantDetail.getAdministrativeStreetName());
		address.setLandMark(applicantDetail.getAdministrativeLandMark());
		address.setCountryId(applicantDetail.getAdministrativeCountryId());
		address.setStateId(applicantDetail.getAdministrativeStateId());
		address.setCityId(applicantDetail.getAdministrativeCityId());
		address.setPincode(applicantDetail.getAdministrativePincode());
		if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativeDistMappingId())) {
			PincodeData pincodeData = pincodeDataRepository.findOne(applicantDetail.getAdministrativeDistMappingId());
			if (!CommonUtils.isObjectNullOrEmpty(pincodeData)) {
				address.setVillage(pincodeData.getOfficeName());
				address.setDistrict(pincodeData.getDistrictName());
				address.setSubDistrict(pincodeData.getTaluka());
				address.setDistrictMappingId(applicantDetail.getAdministrativeDistMappingId());
			}
		}
		response.setCorpOfficeAddress(address);

		// GET RERGISTERED EMAIL ID :- LINENO:11
		try {
			UserResponse userResponse = usersClient.getEmailMobile(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				UsersRequest request = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(request)) {
					response.setRegEmailId(request.getEmail());
					// Contact Details :- LINENO:8
					response.setContactNo(request.getMobile());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// GET PROFILE CONSTITUTION :- LINENO:13
		response.setConstitution(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getConstitutionId())
				? Constitution.getById(applicantDetail.getConstitutionId()).getValue()
				: "NA");
		response.setConstitutionId(applicantDetail.getConstitutionId());

		String establishMentYear = !CommonUtils.isObjectNullOrEmpty(applicantDetail.getEstablishmentMonth())
				? EstablishmentMonths.getById(applicantDetail.getEstablishmentMonth()).getValue()
				: "";
		if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getEstablishmentYear())) {
			try {
				OneFormResponse establishmentYearResponse = oneFormClient
						.getYearByYearId(CommonUtils.isObjectNullOrEmpty(applicantDetail.getEstablishmentYear()) ? null
								: applicantDetail.getEstablishmentYear().longValue());
				List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) establishmentYearResponse
						.getListData();
				if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
					MasterResponse masterResponse = MultipleJSONObjectHelper
							.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
					establishMentYear += " " + masterResponse.getValue();
				}
			} catch (Exception e) {
				logger.info("Throw Exception while get establishment year in DDR OneForm");
				e.printStackTrace();
			}
		}
		// GET PROFILE ESTABLISHMENT YEAR :- LINENO:14
		response.setEstablishMentYear(!CommonUtils.isObjectNullOrEmpty(establishMentYear) ? establishMentYear : "NA");

		// ABOUT US :- LINENO:15
		response.setAboutMe(applicantDetail.getAboutUs());

		// ---------------------------------------------------PRIMARY
		// ------------------------------------------------------------------------

		// PROMOTOR BACKGROUND DETAILS :- LINENO:12
		try {
			response.setPromoBackRespList(
					promotorBackgroundDetailsService.getPromotorBackgroundDetailList(applicationId, null));
		} catch (Exception e) {
			logger.info("Throw Exception While Get Primary Promotor Background Details in DDR OneForm");
			e.printStackTrace();
		}

		// OWNERSHIP DETAILS :- LINENO:12
		try {
			List<OwnershipDetail> ownershipList = ownershipDetailsRepository.listOwnershipFromAppId(applicationId);
			List<OwnershipDetailRequest> ownershipRespList = new ArrayList<>(ownershipList.size());
			OwnershipDetailRequest ownershipReq = null;
			for (OwnershipDetail ownership : ownershipList) {
				if (CommonUtils.isObjectNullOrEmpty(ownership)) {
					continue;
				}
				ownershipReq = new OwnershipDetailRequest();
				BeanUtils.copyProperties(ownership, ownershipReq);
				ownershipRespList.add(ownershipReq);
			}
			response.setOwnershipReqList(ownershipRespList);
		} catch (Exception e) {
			logger.info("Throw Exception While Get Primary Ownership Details in DDR OneForm");
			e.printStackTrace();
		}

		// SECURITY DETAIL :- LINENO:12
		try {
			response.setSecurityCorporateDetailList(
					securityCorporateDetailsService.getsecurityCorporateDetailsList(applicationId, userId));
		} catch (Exception e) {
			logger.info("Throw Exception While Get Primary Security Details in DDR OneForm");
			e.printStackTrace();
		}

		// -----------------PRODUCT DETAILS PROPOSED AND EXISTING (Description of
		// Products) :- LINENO:111
		try {
			response.setProposedProductDetailList(
					proposedProductDetailsService.getProposedProductDetailList(applicationId, userId));
			response.setExistingProductDetailList(
					existingProductDetailsService.getExistingProductDetailList(applicationId, userId));
		} catch (Exception e) {
			logger.info("Throw Exception While Get Product Proposed and Existing details in DDR OneForm");
			e.printStackTrace();
		}

		// ASSOCIATES CONCERN :- LINENO:17
		try {
			response.setAssociatedConcernDetailList(
					associatedConcernDetailService.getAssociatedConcernsDetailList(applicationId, userId));
		} catch (Exception e) {
			logger.info("Throw Exception While Get associates concern in DDR OneForm");
			e.printStackTrace();
		}
		response.setdDRCMACalculationList(getCMAandCOActDetails(applicationId));

		/*
		 * try { List<ReferencesRetailDetail> referencesRetailList =
		 * referenceRetailDetailsRepository.listReferencesRetailFromAppId(applicationId)
		 * ; List<ReferenceRetailDetailsRequest> referencesResponseList = new
		 * ArrayList<>(referencesRetailList.size()); ReferenceRetailDetailsRequest
		 * referencesResponse = null; for(ReferencesRetailDetail referencesRetail :
		 * referencesRetailList) { referencesResponse = new
		 * ReferenceRetailDetailsRequest(); BeanUtils.copyProperties(referencesRetail,
		 * referencesResponse);
		 * ReferenceRetailDetailsRequest.printFields(referencesResponse);
		 * referencesResponseList.add(referencesResponse); }
		 * response.setReferencesResponseList(referencesResponseList); } catch
		 * (Exception e) {
		 * logger.info("Throw Exception While Get Reference Details in DDR OneForm");
		 * e.printStackTrace(); }
		 */
		return response;
	}

	DecimalFormat decim = new DecimalFormat("#,##0.00");
	/**
	 * SAVE DDR FORM DETAILS EXCPET FRAMES AND ONEFORM DETAILS
	 * 
	 * @throws Exception
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public void saveDDRForm(DDRFormDetailsRequest ddrFormDetailsRequest) throws Exception {

		Long userId = ddrFormDetailsRequest.getUserId();

		try {
			DDRFormDetails dDRFormDetails = ddrFormDetailsRepository.getByIdAndAppIdAndIsActive(
					ddrFormDetailsRequest.getId(), ddrFormDetailsRequest.getApplicationId());
			if (CommonUtils.isObjectNullOrEmpty(dDRFormDetails)) {
				logger.info("DDR ===============> New DDR Form Saving ------------------------->");
				dDRFormDetails = new DDRFormDetails();
				BeanUtils.copyProperties(ddrFormDetailsRequest, dDRFormDetails, "id");
				dDRFormDetails.setIsActive(true);
				dDRFormDetails.setCreatedBy(userId);
				dDRFormDetails.setCreatedDate(new Date());
			} else {
				logger.info("DDR ===============> DDR Form Updating ------------------------->"
						+ ddrFormDetailsRequest.getId());
				BeanUtils.copyProperties(ddrFormDetailsRequest, dDRFormDetails, "id", "applicationId", "userId",
						"isActive");
				dDRFormDetails.setModifyBy(userId);
				dDRFormDetails.setModifyDate(new Date());
			}
			dDRFormDetails = ddrFormDetailsRepository.save(dDRFormDetails);

			// SAVE ALL LIST DATA
			saveAuthorizedSignDetails(ddrFormDetailsRequest.getdDRAuthSignDetailsList(), userId,
					dDRFormDetails.getId());
			saveCreaditorsDetails(ddrFormDetailsRequest.getdDRCreditorsDetailsList(), userId, dDRFormDetails.getId());
			saveCreditCardDetails(ddrFormDetailsRequest.getdDRCreditCardDetailsList(), userId, dDRFormDetails.getId());
			saveOfficeDetails(ddrFormDetailsRequest.getdDROperatingOfficeList(), userId,
					DDRFrames.OPERATING_OFFICE.getValue(), dDRFormDetails.getId());
			saveOfficeDetails(ddrFormDetailsRequest.getdDRRegisteredOfficeList(), userId,
					DDRFrames.REGISTERED_OFFICE.getValue(), dDRFormDetails.getId());
			saveOtherBankLoanDetails(ddrFormDetailsRequest.getdDROtherBankLoanDetailsList(), userId,
					dDRFormDetails.getId());
			// saveRelWithDBSDetails(ddrFormDetailsRequest.getdDRRelWithDbsDetailsList(),
			// userId,dDRFormDetails.getId());
			saveVehiclesOwnedDetails(ddrFormDetailsRequest.getdDRVehiclesOwnedDetailsList(), userId,
					dDRFormDetails.getId());
			saveFinancialSummary(ddrFormDetailsRequest.getdDRFinancialSummaryList(), userId, dDRFormDetails.getId());
			saveFamilyDirectorsDetails(ddrFormDetailsRequest.getdDRFamilyDirectorsList(), userId,
					dDRFormDetails.getId());
			saveExistingBankerDetails(ddrFormDetailsRequest.getExistingBankerDetailList(), userId,
					dDRFormDetails.getId());
			logger.info("DDR ===============> DDR Form Saved Successfully in Service-----------------> "
					+ dDRFormDetails.getId());
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception while saving ddr form");
			e.printStackTrace();
			throw new Exception(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	/**
	 * 
	 * GET DDR FORM DETAILS EXCPET FRAMES AND ONEFORM DETAILS
	 */
	@Override
	public DDRFormDetailsRequest get(Long appId, Long userId) {
		DDRFormDetailsRequest dDRFormDetailsRequest = null;
		DDRFormDetails dDRFormDetails = ddrFormDetailsRepository.getByAppIdAndIsActive(appId);
		if (!CommonUtils.isObjectNullOrEmpty(dDRFormDetails)) {
			Long ddrFormId = dDRFormDetails.getId();
			dDRFormDetailsRequest = new DDRFormDetailsRequest();
			BeanUtils.copyProperties(dDRFormDetails, dDRFormDetailsRequest);
			dDRFormDetailsRequest.setOutsideLoansString(CommonUtils.checkString(dDRFormDetails.getOutsideLoans()));
			dDRFormDetailsRequest.setLoansFromFamilyMembersRelativeString(
					CommonUtils.checkString(dDRFormDetails.getLoansFromFamilyMembersRelative()));
			dDRFormDetailsRequest.setdDRAuthSignDetailsList(getAuthorizedSignDetails(ddrFormId));
			dDRFormDetailsRequest.setdDRCreditCardDetailsList(getCreditCardDetails(ddrFormId));
			dDRFormDetailsRequest.setdDRCreditorsDetailsList(getCreaditorsDetails(ddrFormId));
			dDRFormDetailsRequest
					.setdDROperatingOfficeList(getOfficeDetails(ddrFormId, DDRFrames.OPERATING_OFFICE.getValue()));
			dDRFormDetailsRequest
					.setdDRRegisteredOfficeList(getOfficeDetails(ddrFormId, DDRFrames.REGISTERED_OFFICE.getValue()));
			dDRFormDetailsRequest.setdDROtherBankLoanDetailsList(getOtherBankLoanDetails(ddrFormId));
			// dDRFormDetailsRequest.setdDRRelWithDbsDetailsList(getRelWithDBSDetails(ddrFormId));
			dDRFormDetailsRequest.setdDRVehiclesOwnedDetailsList(getVehiclesOwnedDetails(ddrFormId));
			dDRFormDetailsRequest.setdDRFinancialSummaryList(getFinancialSummary(ddrFormId));
			dDRFormDetailsRequest.setdDRFamilyDirectorsList(getFamilyDirectorsDetails(ddrFormId, appId, userId, false));
			dDRFormDetailsRequest
					.setExistingBankerDetailList(getExistingBankerDetails(ddrFormId, appId, userId, false));
			dDRFormDetailsRequest.setProvisionalTotalSales(getCMATotalSalesByAppIdAndYear(appId, "2018"));
			dDRFormDetailsRequest.setLastYearTotalSales(getCMATotalSalesByAppIdAndYear(appId, "2017"));
			dDRFormDetailsRequest.setLastToLastYearTotalSales(getCMATotalSalesByAppIdAndYear(appId, "2016"));
			dDRFormDetailsRequest.setCurrency(getCurrency(appId, userId));
		} else {
			dDRFormDetailsRequest = new DDRFormDetailsRequest();
			dDRFormDetailsRequest.setdDRFamilyDirectorsList(getFamilyDirectorsDetails(null, appId, userId, false));
			dDRFormDetailsRequest.setExistingBankerDetailList(getExistingBankerDetails(null, appId, userId, false));
			dDRFormDetailsRequest.setdDRFinancialSummaryList(getFinancialSummary(null));
			dDRFormDetailsRequest.setCurrency(getCurrency(appId, userId));
		}
		return dDRFormDetailsRequest;
	}

	@Override
	public com.capitaworld.sidbi.integration.model.ddr.DDRFormDetailsRequest getSIDBIDetails(Long appId, Long userId) {
		com.capitaworld.sidbi.integration.model.ddr.DDRFormDetailsRequest dDRFormDetailsRequest = new com.capitaworld.sidbi.integration.model.ddr.DDRFormDetailsRequest();

		DDRFormDetails dDRFormDetails = ddrFormDetailsRepository.getByAppIdAndIsActive(appId);
		if (CommonUtils.isObjectNullOrEmpty(dDRFormDetails)) {
			return dDRFormDetailsRequest;
		}
		BeanUtils.copyProperties(dDRFormDetails, dDRFormDetailsRequest);
		dDRFormDetailsRequest.setUserId(userId);
		dDRFormDetailsRequest.setApplicationId(appId);
		try {
			CorporateApplicantDetail applicantDetail = corporateApplicantDetailRepository
					.getByApplicationIdAndIsAtive(appId);
			if (!CommonUtils.isObjectNullOrEmpty(applicantDetail)) {
				// GET REGISTERED ADDRESS :- LINENO:7
				String regOfficeAdd = "";
				regOfficeAdd = !CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredPremiseNumber())
						? applicantDetail.getRegisteredPremiseNumber() + ", "
						: "";
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredStreetName())
						? applicantDetail.getRegisteredStreetName() + ", "
						: "";
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredLandMark())
						? applicantDetail.getRegisteredLandMark() + ", "
						: "";
				String countryName = getCountryName(applicantDetail.getRegisteredCountryId());
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(countryName) ? countryName + ", " : "";
				String stateName = getStateName(applicantDetail.getRegisteredStateId());
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(stateName) ? stateName + ", " : "";
				String cityName = getCityName(applicantDetail.getRegisteredCityId());
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(cityName) ? cityName : "";
				regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredPincode())
						? applicantDetail.getRegisteredPincode()
						: "";
				dDRFormDetailsRequest.setRegisteredOfficeAddressDetails(
						!CommonUtils.isObjectNullOrEmpty(regOfficeAdd) ? regOfficeAdd : "NA");

				// GET ADMINISRATIVE (Corporate Office) ADDRESS :- LINENO:9
				String admntOfficeAdd = "";
				admntOfficeAdd = !CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativePremiseNumber())
						? applicantDetail.getAdministrativePremiseNumber() + ", "
						: "";
				admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativeStreetName())
						? applicantDetail.getAdministrativeStreetName() + ", "
						: "";
				admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativeLandMark())
						? applicantDetail.getAdministrativeLandMark() + ", "
						: "";
				String admntCountryName = getCountryName(applicantDetail.getAdministrativeCountryId());
				admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(admntCountryName) ? admntCountryName + ", " : "";
				String admntStateName = getStateName(applicantDetail.getAdministrativeStateId());
				admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(admntStateName) ? admntStateName + ", " : "";
				String admntCityName = getCityName(applicantDetail.getAdministrativeCityId());
				admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(admntCityName) ? admntCityName : "";
				admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativePincode())
						? applicantDetail.getAdministrativePincode()
						: "";
				dDRFormDetailsRequest.setRegisteredOfficeAddressDetails(
						!CommonUtils.isObjectNullOrEmpty(admntOfficeAdd) ? admntOfficeAdd : "NA");
			}

		} catch (Exception e) {
			logger.info("Throw Exception While Get Corporate Details");
			e.printStackTrace();
		}

		Long ddrFormId = dDRFormDetails.getId();

		List<DDRAuthorizedSignDetails> listByDDRFormId = authorizedSignDetailsRepository.getListByDDRFormId(ddrFormId);
		List<com.capitaworld.sidbi.integration.model.ddr.DDRAuthorizedSignDetailsRequest> authorResponseList = new ArrayList<>(
				listByDDRFormId.size());
		if (!CommonUtils.isListNullOrEmpty(listByDDRFormId)) {
			for (DDRAuthorizedSignDetails authorizedSignDetails : listByDDRFormId) {
				com.capitaworld.sidbi.integration.model.ddr.DDRAuthorizedSignDetailsRequest response = new com.capitaworld.sidbi.integration.model.ddr.DDRAuthorizedSignDetailsRequest();
				BeanUtils.copyProperties(authorizedSignDetails, response);
				response.setApplicationId(appId);
				authorResponseList.add(response);
			}
		}
		dDRFormDetailsRequest.setdDRAuthSignDetailsList(authorResponseList);

		List<DDRCreditCardDetails> creditCardList = cardDetailsRepository.getListByDDRFormId(ddrFormId);
		List<com.capitaworld.sidbi.integration.model.ddr.DDRCreditCardDetailsRequest> creditResponseList = new ArrayList<>(
				creditCardList.size());
		if (!CommonUtils.isListNullOrEmpty(creditCardList)) {
			for (DDRCreditCardDetails obj : creditCardList) {
				com.capitaworld.sidbi.integration.model.ddr.DDRCreditCardDetailsRequest response = new com.capitaworld.sidbi.integration.model.ddr.DDRCreditCardDetailsRequest();
				BeanUtils.copyProperties(obj, response);
				response.setApplicationId(appId);
				creditResponseList.add(response);
			}
		}
		dDRFormDetailsRequest.setdDRCreditCardDetailsList(creditResponseList);

		List<DDRCreditorsDetails> creditorsDetailsList = creditorsDetailsRepository.getListByDDRFormId(ddrFormId);
		List<com.capitaworld.sidbi.integration.model.ddr.DDRCreditorsDetailsRequest> creditorsList = new ArrayList<>(
				creditorsDetailsList.size());
		if (!CommonUtils.isListNullOrEmpty(creditorsDetailsList)) {
			for (DDRCreditorsDetails obj : creditorsDetailsList) {
				com.capitaworld.sidbi.integration.model.ddr.DDRCreditorsDetailsRequest response = new com.capitaworld.sidbi.integration.model.ddr.DDRCreditorsDetailsRequest();
				BeanUtils.copyProperties(obj, response);
				response.setApplicationId(appId);
				creditorsList.add(response);
			}
		}
		dDRFormDetailsRequest.setdDRCreditorsDetailsList(creditorsList);

		List<DDROtherBankLoanDetails> otherBankLoanList = bankLoanDetailsRepository.getListByDDRFormId(ddrFormId);
		List<com.capitaworld.sidbi.integration.model.ddr.DDROtherBankLoanDetailsRequest> otherBankLoanReqList = new ArrayList<>(
				otherBankLoanList.size());
		if (!CommonUtils.isListNullOrEmpty(otherBankLoanList)) {
			for (DDROtherBankLoanDetails obj : otherBankLoanList) {
				com.capitaworld.sidbi.integration.model.ddr.DDROtherBankLoanDetailsRequest response = new com.capitaworld.sidbi.integration.model.ddr.DDROtherBankLoanDetailsRequest();
				BeanUtils.copyProperties(obj, response);
				response.setApplicationId(appId);
				otherBankLoanReqList.add(response);
			}
		}
		dDRFormDetailsRequest.setdDROtherBankLoanDetailsList(otherBankLoanReqList);

		List<DDRVehiclesOwnedDetails> vehiclesOwnedList = vehiclesOwnedDetailsRepository.getListByDDRFormId(ddrFormId);
		List<com.capitaworld.sidbi.integration.model.ddr.DDRVehiclesOwnedDetailsRequest> vehiclesList = new ArrayList<>(
				vehiclesOwnedList.size());
		if (!CommonUtils.isListNullOrEmpty(vehiclesOwnedList)) {
			for (DDRVehiclesOwnedDetails obj : vehiclesOwnedList) {
				com.capitaworld.sidbi.integration.model.ddr.DDRVehiclesOwnedDetailsRequest response = new com.capitaworld.sidbi.integration.model.ddr.DDRVehiclesOwnedDetailsRequest();
				BeanUtils.copyProperties(obj, response);
				response.setApplicationId(appId);
				vehiclesList.add(response);
			}
		}
		dDRFormDetailsRequest.setdDRVehiclesOwnedDetailsList(vehiclesList);

		List<com.capitaworld.sidbi.integration.model.ddr.DDRFinancialSummaryRequest> financialSummuryList = null;
		if (!CommonUtils.isObjectNullOrEmpty(ddrFormId)) {
			List<DDRFinancialSummary> objList = financialSummaryRepository.getListByDDRFormId(ddrFormId);
			if (!CommonUtils.isListNullOrEmpty(objList)) {
				financialSummuryList = new ArrayList<>(objList.size());
				for (DDRFinancialSummary obj : objList) {
					com.capitaworld.sidbi.integration.model.ddr.DDRFinancialSummaryRequest response = new com.capitaworld.sidbi.integration.model.ddr.DDRFinancialSummaryRequest();
					BeanUtils.copyProperties(obj, response);
					response.setDiffPfPrvsnlAndLastYear(CommonUtils.checkDouble(obj.getDiffPfPrvsnlAndLastYear()));
					response.setLastToLastYear(CommonUtils.checkDouble(obj.getLastToLastYear()));
					response.setLastYear(CommonUtils.checkDouble(obj.getLastYear()));
					response.setProvisionalYear(CommonUtils.checkDouble(obj.getProvisionalYear()));
					response.setApplicationId(appId);
					financialSummuryList.add(response);
				}
			}
		}
		dDRFormDetailsRequest.setdDRFinancialSummaryList(financialSummuryList);

		List<com.capitaworld.sidbi.integration.model.ddr.DDRFamilyDirectorsDetailsRequest> familyDirectorsList = null;
		if (!CommonUtils.isObjectNullOrEmpty(ddrFormId)) {
			List<DDRFamilyDirectorsDetails> familyDirectorList = familyDirectorsDetailsRepository
					.getListByDDRFormId(ddrFormId);
			if (!CommonUtils.isListNullOrEmpty(familyDirectorList)) {
				familyDirectorsList = new ArrayList<>(familyDirectorList.size());
				com.capitaworld.sidbi.integration.model.ddr.DDRFamilyDirectorsDetailsRequest response = null;
				for (DDRFamilyDirectorsDetails obj : familyDirectorList) {
					response = new com.capitaworld.sidbi.integration.model.ddr.DDRFamilyDirectorsDetailsRequest();
					BeanUtils.copyProperties(obj, response);
					if (!CommonUtils.isObjectNullOrEmpty(obj.getMaritalStatus())) {
						MaritalStatus maritalStatus = MaritalStatus.getById(obj.getMaritalStatus());
						response.setMaritalStatusName(
								!CommonUtils.isObjectNullOrEmpty(maritalStatus) ? maritalStatus.getValue() : null);
					}
					response.setApplicationId(appId);
					familyDirectorsList.add(response);
				}
			}
		}

		dDRFormDetailsRequest.setdDRFamilyDirectorsList(familyDirectorsList);

		List<com.capitaworld.sidbi.integration.model.ddr.DDRExistingBankerDetailRequest> existingBankerDetailsList = null;
		if (!CommonUtils.isObjectNullOrEmpty(ddrFormId)) {
			List<DDRExistingBankerDetails> existingBankList = ddrExistingBankerDetailsRepository
					.getListByDDRFormId(ddrFormId);
			if (!CommonUtils.isListNullOrEmpty(existingBankList)) {
				existingBankerDetailsList = new ArrayList<>(existingBankList.size());
				com.capitaworld.sidbi.integration.model.ddr.DDRExistingBankerDetailRequest response = null;
				for (DDRExistingBankerDetails obj : existingBankList) {
					response = new com.capitaworld.sidbi.integration.model.ddr.DDRExistingBankerDetailRequest();
					BeanUtils.copyProperties(obj, response);
					response.setApplicationId(appId);
					existingBankerDetailsList.add(response);
				}
			}
		}
		dDRFormDetailsRequest.setExistingBankerDetailList(existingBankerDetailsList);

		return dDRFormDetailsRequest;
	}

	/**
	 * GET AUTHORIZED SIGN DETAILS LIST BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @return
	 */
	public List<DDRAuthorizedSignDetailsRequest> getAuthorizedSignDetails(Long ddrFormId) {
		try {
			List<DDRAuthorizedSignDetails> listByDDRFormId = authorizedSignDetailsRepository
					.getListByDDRFormId(ddrFormId);
			List<DDRAuthorizedSignDetailsRequest> responseList = new ArrayList<>(listByDDRFormId.size());
			if (!CommonUtils.isListNullOrEmpty(listByDDRFormId)) {
				for (DDRAuthorizedSignDetails authorizedSignDetails : listByDDRFormId) {
					DDRAuthorizedSignDetailsRequest response = new DDRAuthorizedSignDetailsRequest();
					BeanUtils.copyProperties(authorizedSignDetails, response);
					DDRAuthorizedSignDetailsRequest.printFields(response);
					responseList.add(response);
				}
			}
			return responseList;
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception While Get Authorized Sign Details ------DDR FORM ID-->"
					+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();

	}

	public void saveAuthorizedSignDetails(List<DDRAuthorizedSignDetailsRequest> dDRAuthSignDetailsList, Long userId,
			Long ddrFormId) {
		try {
			for (DDRAuthorizedSignDetailsRequest dDRAuthSignDetails : dDRAuthSignDetailsList) {
				DDRAuthorizedSignDetails ddrAuthorizedSignDetails = null;
				if (!CommonUtils.isObjectNullOrEmpty(dDRAuthSignDetails.getId())) {
					ddrAuthorizedSignDetails = authorizedSignDetailsRepository
							.getByIdAndIsActive(dDRAuthSignDetails.getId());
				}
				if (CommonUtils.isObjectNullOrEmpty(ddrAuthorizedSignDetails)) {
					ddrAuthorizedSignDetails = new DDRAuthorizedSignDetails();
					BeanUtils.copyProperties(dDRAuthSignDetails, ddrAuthorizedSignDetails, "id", "createdBy",
							"createdDate", "modifyBy", "modifyDate", "ddrFormId", "isActive");
					ddrAuthorizedSignDetails.setCreatedBy(userId);
					ddrAuthorizedSignDetails.setCreatedDate(new Date());
					ddrAuthorizedSignDetails.setIsActive(true);
					ddrAuthorizedSignDetails.setDdrFormId(ddrFormId);
				} else {
					BeanUtils.copyProperties(dDRAuthSignDetails, ddrAuthorizedSignDetails, "id", "createdBy",
							"createdDate", "modifyBy", "modifyDate", "ddrFormId");
					ddrAuthorizedSignDetails.setModifyBy(userId);
					ddrAuthorizedSignDetails.setModifyDate(new Date());
				}
				authorizedSignDetailsRepository.save(ddrAuthorizedSignDetails);
			}
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Save Authorized Sign Details ------DDR FORM ID------->"
							+ ddrFormId);
			e.printStackTrace();
		}
	}

	/**
	 * GET CREDIT CARD DETAILS BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @return
	 */
	public List<DDRCreditCardDetailsRequest> getCreditCardDetails(Long ddrFormId) {
		try {
			List<DDRCreditCardDetails> objList = cardDetailsRepository.getListByDDRFormId(ddrFormId);
			List<DDRCreditCardDetailsRequest> responseList = new ArrayList<>(objList.size());
			if (!CommonUtils.isListNullOrEmpty(objList)) {
				for (DDRCreditCardDetails obj : objList) {
					DDRCreditCardDetailsRequest response = new DDRCreditCardDetailsRequest();
					DDRCreditCardDetailsRequest.printFields(response);
					BeanUtils.copyProperties(obj, response);
					responseList.add(response);
				}
			}
			return responseList;
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception While Get Credit Card Details ------DDR FORM ID-->"
					+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public void saveCreditCardDetails(List<DDRCreditCardDetailsRequest> requestList, Long userId, Long ddrFormId) {
		try {
			for (DDRCreditCardDetailsRequest reqObj : requestList) {
				DDRCreditCardDetails saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = cardDetailsRepository.getByIdAndIsActive(reqObj.getId());
				}
				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDRCreditCardDetails();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setIsActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				cardDetailsRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception While Save Credit Card Details ------DDR FORM ID-->"
					+ ddrFormId);
			e.printStackTrace();
		}
	}

	/**
	 * GET CREADITORS DETAILS BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @return
	 */
	public List<DDRCreditorsDetailsRequest> getCreaditorsDetails(Long ddrFormId) {
		try {
			List<DDRCreditorsDetails> objList = creditorsDetailsRepository.getListByDDRFormId(ddrFormId);
			List<DDRCreditorsDetailsRequest> responseList = new ArrayList<>(objList.size());
			if (!CommonUtils.isListNullOrEmpty(objList)) {
				for (DDRCreditorsDetails obj : objList) {
					DDRCreditorsDetailsRequest response = new DDRCreditorsDetailsRequest();
					BeanUtils.copyProperties(obj, response);
					DDRCreditorsDetailsRequest.printFields(response);
					responseList.add(response);
				}
			}
			return responseList;
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception While Get Creaditors Details ------DDR FORM ID-->"
					+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public void saveCreaditorsDetails(List<DDRCreditorsDetailsRequest> requestList, Long userId, Long ddrFormId) {
		try {
			for (DDRCreditorsDetailsRequest reqObj : requestList) {
				DDRCreditorsDetails saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = creditorsDetailsRepository.getByIdAndIsActive(reqObj.getId());
				}
				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDRCreditorsDetails();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setIsActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				creditorsDetailsRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception While Save Creaditors Details ------DDR FORM ID-->"
					+ ddrFormId);
			e.printStackTrace();
		}

	}

	/**
	 * GET OFFICE DETAILS BASE ON OFFICE TYPE BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @param officeType
	 *            :- Two Type First is REGISTERED OFFICE(ID: 4) and Second is
	 *            OPERATING OFFICE(ID: 4)
	 * @return
	 */
	public List<DDROfficeDetailsRequest> getOfficeDetails(Long ddrFormId, Integer officeType) {
		try {
			List<DDROfficeDetails> objList = ddrOfficeDetailsRepository.getListByDDRFormId(ddrFormId, officeType);
			List<DDROfficeDetailsRequest> responseList = new ArrayList<>(objList.size());
			if (!CommonUtils.isListNullOrEmpty(objList)) {
				for (DDROfficeDetails obj : objList) {
					DDROfficeDetailsRequest response = new DDROfficeDetailsRequest();
					BeanUtils.copyProperties(obj, response);
					DDROfficeDetailsRequest.printFields(response);
					responseList.add(response);
				}
			}
			return responseList;
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception While Get Office Details--------officeType------"
					+ officeType + "------DDR FORM ID-->" + ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public void saveOfficeDetails(List<DDROfficeDetailsRequest> requestList, Long userId, Integer officeType,
			Long ddrFormId) {
		try {
			for (DDROfficeDetailsRequest reqObj : requestList) {
				DDROfficeDetails saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = ddrOfficeDetailsRepository.getByIdAndIsActive(reqObj.getId());
				}

				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDROfficeDetails();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setOfficeType(officeType);
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setIsActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				ddrOfficeDetailsRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info("DDR ===============> Throw Exception While Save Office Details --------officeType------"
					+ officeType + "------DDR FORM ID-->" + ddrFormId);
			e.printStackTrace();
		}
	}

	/**
	 * GET OTHER BANK DETAILS BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @return
	 */
	public List<DDROtherBankLoanDetailsRequest> getOtherBankLoanDetails(Long ddrFormId) {
		try {
			List<DDROtherBankLoanDetails> objList = bankLoanDetailsRepository.getListByDDRFormId(ddrFormId);
			List<DDROtherBankLoanDetailsRequest> responseList = new ArrayList<>(objList.size());
			if (!CommonUtils.isListNullOrEmpty(objList)) {
				for (DDROtherBankLoanDetails obj : objList) {
					DDROtherBankLoanDetailsRequest response = new DDROtherBankLoanDetailsRequest();
					BeanUtils.copyProperties(obj, response);
					DDROtherBankLoanDetailsRequest.printFields(response);
					responseList.add(response);
				}
			}
			return responseList;
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Get Other Bank Loan Details--------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();

	}

	public void saveOtherBankLoanDetails(List<DDROtherBankLoanDetailsRequest> requestList, Long userId,
			Long ddrFormId) {
		try {
			for (DDROtherBankLoanDetailsRequest reqObj : requestList) {
				DDROtherBankLoanDetails saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = bankLoanDetailsRepository.getByIdAndIsActive(reqObj.getId());
				}

				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDROtherBankLoanDetails();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setIsActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				bankLoanDetailsRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Save Other Bank Loan Details -------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}

	}

	/**
	 * GET RELATION WITH DBS DETAILS BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @return
	 */
	public List<DDRRelWithDbsDetailsRequest> getRelWithDBSDetails(Long ddrFormId) {
		try {
			List<DDRRelWithDbsDetails> objList = dbsDetailsRepository.getListByDDRFormId(ddrFormId);
			List<DDRRelWithDbsDetailsRequest> responseList = new ArrayList<>(objList.size());
			if (!CommonUtils.isListNullOrEmpty(objList)) {
				for (DDRRelWithDbsDetails obj : objList) {
					DDRRelWithDbsDetailsRequest response = new DDRRelWithDbsDetailsRequest();
					BeanUtils.copyProperties(obj, response);
					DDRRelWithDbsDetailsRequest.printFields(response);
					responseList.add(response);
				}
			}
			return responseList;
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Get Rel With DBS Details--------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public void saveRelWithDBSDetails(List<DDRRelWithDbsDetailsRequest> requestList, Long userId, Long ddrFormId) {
		try {
			for (DDRRelWithDbsDetailsRequest reqObj : requestList) {
				DDRRelWithDbsDetails saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = dbsDetailsRepository.getByIdAndIsActive(reqObj.getId());
				}

				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDRRelWithDbsDetails();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setIsActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				dbsDetailsRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Save Rel With DBS Details -------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}

	}

	/**
	 * GET VEHICLES OWNED DETAILS BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @return
	 */
	public List<DDRVehiclesOwnedDetailsRequest> getVehiclesOwnedDetails(Long ddrFormId) {
		try {
			List<DDRVehiclesOwnedDetails> objList = vehiclesOwnedDetailsRepository.getListByDDRFormId(ddrFormId);
			List<DDRVehiclesOwnedDetailsRequest> responseList = new ArrayList<>(objList.size());
			if (!CommonUtils.isListNullOrEmpty(objList)) {
				for (DDRVehiclesOwnedDetails obj : objList) {
					DDRVehiclesOwnedDetailsRequest response = new DDRVehiclesOwnedDetailsRequest();
					BeanUtils.copyProperties(obj, response);
					DDRVehiclesOwnedDetailsRequest.printFields(response);
					responseList.add(response);
				}
			}
			return responseList;
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Get Vehicles Owned Details--------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();

	}

	public void saveVehiclesOwnedDetails(List<DDRVehiclesOwnedDetailsRequest> requestList, Long userId,
			Long ddrFormId) {
		try {
			for (DDRVehiclesOwnedDetailsRequest reqObj : requestList) {
				DDRVehiclesOwnedDetails saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = vehiclesOwnedDetailsRepository.getByIdAndIsActive(reqObj.getId());
				}

				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDRVehiclesOwnedDetails();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setIsActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				vehiclesOwnedDetailsRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Save Vehicles Owned Details -------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}

	}

	/**
	 * GET FINANCIAL SUMMARY DETAILS BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @return
	 */
	public List<DDRFinancialSummaryRequest> getFinancialSummary(Long ddrFormId) {
		try {
			List<DDRFinancialSummaryRequest> responseList = null;
			if (!CommonUtils.isObjectNullOrEmpty(ddrFormId)) {
				List<DDRFinancialSummary> objList = financialSummaryRepository.getListByDDRFormId(ddrFormId);
				if (!CommonUtils.isListNullOrEmpty(objList)) {
					responseList = new ArrayList<>(objList.size());
					for (DDRFinancialSummary obj : objList) {
						DDRFinancialSummaryRequest response = new DDRFinancialSummaryRequest();
						BeanUtils.copyProperties(obj, response);
						response.setDiffPfPrvsnlAndLastYear(CommonUtils.checkDouble(obj.getDiffPfPrvsnlAndLastYear()));
						response.setLastToLastYear(CommonUtils.checkDouble(obj.getLastToLastYear()));
						response.setLastYear(CommonUtils.checkDouble(obj.getLastYear()));
						response.setProvisionalYear(CommonUtils.checkDouble(obj.getProvisionalYear()));

						response.setDiffPfPrvsnlAndLastYearString(
								CommonUtils.checkString(obj.getDiffPfPrvsnlAndLastYear()));
						response.setLastToLastYearString(CommonUtils.checkString(obj.getLastToLastYear()));
						response.setLastYearString(CommonUtils.checkString(obj.getLastYear()));
						response.setProvisionalYearString(CommonUtils.checkString(obj.getProvisionalYear()));
						DDRFinancialSummaryRequest.printFields(response);
						responseList.add(response);
					}
					return responseList;
				}
			}
			DDRFinancialSummaryToBeFields[] values = DDRFinancialSummaryToBeFields.values();
			responseList = new ArrayList<>(values.length);
			DDRFinancialSummaryRequest response = null;
			for (int i = 0; i < values.length; i++) {
				response = new DDRFinancialSummaryRequest();
				response.setPerticularId(values[i].getId());
				response.setPerticularName(values[i].getValue());
				responseList.add(response);
			}
			return responseList;
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Get Financial Summary Details--------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();

	}

	public void saveFinancialSummary(List<DDRFinancialSummaryRequest> requestList, Long userId, Long ddrFormId) {
		try {
			for (DDRFinancialSummaryRequest reqObj : requestList) {
				DDRFinancialSummary saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = financialSummaryRepository.getByIdAndIsActive(reqObj.getId());
				}

				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDRFinancialSummary();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setPerticularName(
							DDRFinancialSummaryToBeFields.getType(reqObj.getPerticularId()).getValue());
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setIsActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setPerticularName(
							DDRFinancialSummaryToBeFields.getType(reqObj.getPerticularId()).getValue());
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				financialSummaryRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Save Financial Summary Details -------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}

	}

	/**
	 * GET FAMILY DIRECTOR DETAILS BY DDR FORM ID
	 * 
	 * @param ddrFormId
	 * @return
	 */
	public List<DDRFamilyDirectorsDetailsRequest> getFamilyDirectorsDetails(Long ddrFormId, Long appId, Long userId,
			boolean setExistingData) {
		try {
			List<DDRFamilyDirectorsDetailsRequest> responseList = null;
			if (!CommonUtils.isObjectNullOrEmpty(ddrFormId)) {
				List<DDRFamilyDirectorsDetails> objList = familyDirectorsDetailsRepository
						.getListByDDRFormId(ddrFormId);
				if (!CommonUtils.isListNullOrEmpty(objList)) {
					responseList = new ArrayList<>(objList.size());
					DDRFamilyDirectorsDetailsRequest response = null;
					for (DDRFamilyDirectorsDetails obj : objList) {
						response = new DDRFamilyDirectorsDetailsRequest();
						BeanUtils.copyProperties(obj, response);
						try {
							DDRFamilyDirectorsDetailsRequest.printFields(response);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (setExistingData && !CommonUtils.isObjectNullOrEmpty(obj.getBackgroundId())) {// SET DIRECTOR
																											// BACK
																											// DETAILS
																											// IN NEW
																											// DDR
																											// OBJECT
																											// FOR MERGE
																											// DDR
							try {
								DirectorBackgroundDetail dirBackDetails = directorBackgroundDetailsRepository
										.findByIdAndIsActive(obj.getBackgroundId(), true);
								if (!CommonUtils.isObjectNullOrEmpty(dirBackDetails)) {
									DirectorBackgroundDetailRequest dirRes = new DirectorBackgroundDetailRequest();
									BeanUtils.copyProperties(dirBackDetails, dirRes);
									SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy");
									dirRes.setDobString(sd.format(dirBackDetails.getDob()));
									if (!CommonUtils.isObjectNullOrEmpty(dirBackDetails.getDistrictMappingId())) {
										PincodeData pincodeData = pincodeDataRepository
												.findOne(dirBackDetails.getDistrictMappingId());
										if (!CommonUtils.isObjectNullOrEmpty(pincodeData)) {
											dirRes.setVillage(pincodeData.getOfficeName());
											dirRes.setDistrict(pincodeData.getDistrictName());
											dirRes.setSubDistrict(pincodeData.getTaluka());
										}
									}
									response.setDirectorBackReq(dirRes);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						responseList.add(response);
					}
					return responseList;
				}
			}

			try {
				List<DirectorBackgroundDetail> drDetailsList = directorBackgroundDetailsRepository
						.listPromotorBackgroundFromAppId(appId);
				DDRFamilyDirectorsDetailsRequest response = null;
				responseList = new ArrayList<>(drDetailsList.size());
				DirectorBackgroundDetailRequest dirRes = null;
				for (DirectorBackgroundDetail drDetails : drDetailsList) {
					response = new DDRFamilyDirectorsDetailsRequest();
					response.setBackgroundId(drDetails.getId());
					response.setName(drDetails.getDirectorsName());

					if (setExistingData) {
						dirRes = new DirectorBackgroundDetailRequest();
						BeanUtils.copyProperties(drDetails, dirRes);
						SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy");
						dirRes.setDobString(sd.format(dirRes.getDob()));
						if (!CommonUtils.isObjectNullOrEmpty(drDetails.getDistrictMappingId())) {
							PincodeData pincodeData = pincodeDataRepository.findOne(drDetails.getDistrictMappingId());
							if (!CommonUtils.isObjectNullOrEmpty(pincodeData)) {
								dirRes.setVillage(pincodeData.getOfficeName());
								dirRes.setDistrict(pincodeData.getDistrictName());
								dirRes.setSubDistrict(pincodeData.getTaluka());
							}
						}
						response.setDirectorBackReq(dirRes);
					}
					responseList.add(response);
				}
			} catch (Exception e) {
				logger.info("Throw Exception While Get Background Details");
				e.printStackTrace();
			}
			return responseList;
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Get Family Directors Details--------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public List<DDRExistingBankerDetailRequest> getExistingBankerDetails(Long ddrFormId, Long appId, Long userId,
			boolean setExistingData) {
		try {
			List<DDRExistingBankerDetailRequest> responseList = null;
			if (!CommonUtils.isObjectNullOrEmpty(ddrFormId)) {
				List<DDRExistingBankerDetails> objList = ddrExistingBankerDetailsRepository
						.getListByDDRFormId(ddrFormId);
				if (!CommonUtils.isListNullOrEmpty(objList)) {
					responseList = new ArrayList<>(objList.size());
					DDRExistingBankerDetailRequest response = null;
					FinancialArrangementDetailResponseString finArrRes = null;
					for (DDRExistingBankerDetails obj : objList) {
						response = new DDRExistingBankerDetailRequest();
						BeanUtils.copyProperties(obj, response);
						try {
							DDRExistingBankerDetailRequest.printFields(response);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (setExistingData && !CommonUtils.isObjectNullOrEmpty(obj.getFinancialArrangementId())) {
							// SET FINANCIAL ARRANGEMENT DETAILS IN NEW DDR OBJECT FOR MERGE DDR
							try {
								FinancialArrangementsDetail finArraDetails = financialArrangementDetailsRepository
										.findByIdAndIsActive(obj.getFinancialArrangementId(), true);
								if (!CommonUtils.isObjectNullOrEmpty(finArraDetails)) {
									finArrRes = new FinancialArrangementDetailResponseString();
									BeanUtils.copyProperties(finArraDetails, finArrRes);
									if (!CommonUtils.isObjectNullOrEmpty(finArraDetails.getRelationshipSince())) {
										finArrRes.setRelationshipSinceInYear(
												CommonUtils.isObjectNullOrEmpty(finArraDetails.getRelationshipSince())
														? null
														: finArraDetails.getRelationshipSince().toString());
									}
									finArrRes
											.setOutstandingAmount(convertDouble(finArraDetails.getOutstandingAmount()));
									response.setFinArraRes(finArrRes);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						responseList.add(response);
					}
					return responseList;
				}
			}

			try {
				List<FinancialArrangementsDetail> financialArrangementsList = financialArrangementDetailsRepository
						.listSecurityCorporateDetailFromAppId(appId);
				DDRExistingBankerDetailRequest response = null;
				responseList = new ArrayList<>(financialArrangementsList.size());
				FinancialArrangementDetailResponseString finArrRes = null;
				for (FinancialArrangementsDetail finDetail : financialArrangementsList) {
					response = new DDRExistingBankerDetailRequest();
					response.setFinancialArrangementId(finDetail.getId());
					response.setFinancialInstitutionName(finDetail.getFinancialInstitutionName());

					if (setExistingData) {
						finArrRes = new FinancialArrangementDetailResponseString();
						BeanUtils.copyProperties(finDetail, finArrRes);
						if (!CommonUtils.isObjectNullOrEmpty(finDetail.getRelationshipSince())) {
							finArrRes.setRelationshipSinceInYear(
									CommonUtils.isObjectNullOrEmpty(finDetail.getRelationshipSince()) ? null
											: finDetail.getRelationshipSince().toString());
						}
						finArrRes.setOutstandingAmount(convertDouble(finDetail.getOutstandingAmount()));
						response.setFinArraRes(finArrRes);
					}
					responseList.add(response);
				}
			} catch (Exception e) {
				logger.info("Throw Exception While Get DDRExistingBankerDetailRequest Details");
				e.printStackTrace();
			}
			return responseList;
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Get DDRExistingBankerDetailRequest--------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public void saveExistingBankerDetails(List<DDRExistingBankerDetailRequest> requestList, Long userId,
			Long ddrFormId) {
		try {
			for (DDRExistingBankerDetailRequest reqObj : requestList) {
				DDRExistingBankerDetails saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = ddrExistingBankerDetailsRepository.getByIdAndIsActive(reqObj.getId());
				}

				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDRExistingBankerDetails();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				ddrExistingBankerDetailsRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Save Existing Loan Details -------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}

	}

	public void saveFamilyDirectorsDetails(List<DDRFamilyDirectorsDetailsRequest> requestList, Long userId,
			Long ddrFormId) {
		try {
			for (DDRFamilyDirectorsDetailsRequest reqObj : requestList) {
				DDRFamilyDirectorsDetails saveObj = null;
				if (!CommonUtils.isObjectNullOrEmpty(reqObj.getId())) {
					saveObj = familyDirectorsDetailsRepository.getByIdAndIsActive(reqObj.getId());
				}

				if (CommonUtils.isObjectNullOrEmpty(saveObj)) {
					saveObj = new DDRFamilyDirectorsDetails();
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId", "isActive");
					saveObj.setDdrFormId(ddrFormId);
					saveObj.setCreatedBy(userId);
					saveObj.setCreatedDate(new Date());
					saveObj.setIsActive(true);
				} else {
					BeanUtils.copyProperties(reqObj, saveObj, "id", "createdBy", "createdDate", "modifyBy",
							"modifyDate", "ddrFormId");
					saveObj.setModifyBy(userId);
					saveObj.setModifyDate(new Date());
				}
				familyDirectorsDetailsRepository.save(saveObj);
			}
		} catch (Exception e) {
			logger.info(
					"DDR ===============> Throw Exception While Save Family Directors Details -------------DDR FORM ID-->"
							+ ddrFormId);
			e.printStackTrace();
		}

	}

	private String getCurrency(Long applicationId, Long userId) {
		try {
			Integer currencyId = loanApplicationRepository.getCurrencyId(applicationId);
			Integer denominationId = loanApplicationRepository.getDenominationId(applicationId);
			return CommonDocumentUtils.getCurrency(currencyId) + " in "
					+ CommonDocumentUtils.getDenomination(denominationId);
		} catch (Exception e) {
			logger.error("DDR ====================> Throw Excetion While get Currency by application id----------->"
					+ applicationId);
			e.printStackTrace();
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public DDROneFormResponse getOneFormDetails(Long userId, Long applicationId, boolean setExistingData) {

		logger.info("Enter in get one form details service");
		DDROneFormResponse response = new DDROneFormResponse();
		LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.getById(applicationId);

		if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster)) {
			logger.info("Data not found by this application id -------------------> " + applicationId);
			return null;
		}
		response.setApprovedDate(loanApplicationMaster.getApprovedDate());
		response.setDdrStatusId(loanApplicationMaster.getDdrStatusId());

		// ---------------------------------------------------PROFILE
		// ------------------------------------------------------------------------
		logger.info("Before Call Corporate Profile UserId is :- " + userId);
		CorporateApplicantDetail applicantDetail = corporateApplicantDetailRepository
				.getByApplicationIdAndIsAtive(applicationId);
		if (CommonUtils.isObjectNullOrEmpty(applicantDetail)) {
			logger.info("Corporate Profile Details NUll or Empty!! ----------------->" + applicationId);
			return response;
		}
		// GET ORGANIZATION TYPE
		try {
			Long orgId = loanApplicationMaster.getNpOrgId();
			if (!CommonUtils.isObjectNullOrEmpty(orgId)) {
				logger.info("OrgId", orgId);
				String orgName = CommonUtils.getOrganizationName(orgId);
				response.setOrgName(orgName);
				logger.info("Org name", orgName);
			} else {
				logger.info("No org Id found");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Error while getting user org id", e);
		}

		// ORGANIZATION NAME :- LINENO:6
		response.setNameOfBorrower(applicantDetail.getOrganisationName());
		response.setCurrency(getCurrency(applicationId, userId));
		// GET REGISTERED ADDRESS :- LINENO:7
		String regOfficeAdd = "";
		regOfficeAdd = !CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredPremiseNumber())
				? applicantDetail.getRegisteredPremiseNumber() + ", "
				: "";
		regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredStreetName())
				? applicantDetail.getRegisteredStreetName() + ", "
				: "";
		regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredLandMark())
				? applicantDetail.getRegisteredLandMark() + ", "
				: "";
		String countryName = getCountryName(applicantDetail.getRegisteredCountryId());
		regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(countryName) ? countryName + ", " : "";
		String stateName = getStateName(applicantDetail.getRegisteredStateId());
		regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(stateName) ? stateName + ", " : "";
		String cityName = getCityName(applicantDetail.getRegisteredCityId());
		regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(cityName) ? cityName : "";
		regOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getRegisteredPincode())
				? applicantDetail.getRegisteredPincode()
				: "";
		response.setRegOfficeAddress(!CommonUtils.isObjectNullOrEmpty(regOfficeAdd) ? regOfficeAdd : "NA");

		// GET ADMINISRATIVE (Corporate Office) ADDRESS :- LINENO:9
		String admntOfficeAdd = "";
		admntOfficeAdd = !CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativePremiseNumber())
				? applicantDetail.getAdministrativePremiseNumber() + ", "
				: "";
		admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativeStreetName())
				? applicantDetail.getAdministrativeStreetName() + ", "
				: "";
		admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativeLandMark())
				? applicantDetail.getAdministrativeLandMark() + ", "
				: "";
		String admntCountryName = getCountryName(applicantDetail.getAdministrativeCountryId());
		admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(admntCountryName) ? admntCountryName + ", " : "";
		String admntStateName = getStateName(applicantDetail.getAdministrativeStateId());
		admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(admntStateName) ? admntStateName + ", " : "";
		String admntCityName = getCityName(applicantDetail.getAdministrativeCityId());
		admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(admntCityName) ? admntCityName : "";
		admntOfficeAdd += !CommonUtils.isObjectNullOrEmpty(applicantDetail.getAdministrativePincode())
				? applicantDetail.getAdministrativePincode()
				: "";
		response.setCorpOfficeAddress(!CommonUtils.isObjectNullOrEmpty(admntOfficeAdd) ? admntOfficeAdd : "NA");

		// GET RERGISTERED EMAIL ID :- LINENO:11
		try {
			UserResponse userResponse = usersClient.getEmailMobile(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				UsersRequest request = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(request)) {
					response.setRegEmailId(request.getEmail());
					// Contact Details :- LINENO:8
					response.setContactNo(request.getMobile());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// GET PROFILE CONSTITUTION :- LINENO:13
		response.setConstitution(!CommonUtils.isObjectNullOrEmpty(applicantDetail.getConstitutionId())
				? Constitution.getById(applicantDetail.getConstitutionId()).getValue()
				: "NA");

		String establishMentYear = !CommonUtils.isObjectNullOrEmpty(applicantDetail.getEstablishmentMonth())
				? EstablishmentMonths.getById(applicantDetail.getEstablishmentMonth()).getValue()
				: "";
		if (!CommonUtils.isObjectNullOrEmpty(applicantDetail.getEstablishmentYear())) {
			try {
				OneFormResponse establishmentYearResponse = oneFormClient
						.getYearByYearId(CommonUtils.isObjectNullOrEmpty(applicantDetail.getEstablishmentYear()) ? null
								: applicantDetail.getEstablishmentYear().longValue());
				List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) establishmentYearResponse
						.getListData();
				if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
					MasterResponse masterResponse = MultipleJSONObjectHelper
							.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
					establishMentYear += " " + masterResponse.getValue();
				}
			} catch (Exception e) {
				logger.info("Throw Exception while get establishment year in DDR OneForm");
				e.printStackTrace();
			}
		}
		// GET PROFILE ESTABLISHMENT YEAR :- LINENO:14
		response.setEstablishMentYear(!CommonUtils.isObjectNullOrEmpty(establishMentYear) ? establishMentYear : "NA");

		// ABOUT US :- LINENO:15
		response.setAboutMe(applicantDetail.getAboutUs());

		// ---------------------------------------------------PRIMARY
		// ------------------------------------------------------------------------

		// PROMOTOR BACKGROUND DETAILS :- LINENO:12
		try {
			List<PromotorBackgroundDetailRequest> promoBackReqList = promotorBackgroundDetailsService
					.getPromotorBackgroundDetailList(applicationId, null);
			List<PromotorBackgroundDetailResponse> promoBackRespList = new ArrayList<>(promoBackReqList.size());
			PromotorBackgroundDetailResponse promoBackResp = null;
			for (PromotorBackgroundDetailRequest promBackReq : promoBackReqList) {
				promoBackResp = new PromotorBackgroundDetailResponse();
				BeanUtils.copyProperties(promBackReq, promoBackResp);
				// promoBackResp.setAchievements(promBackReq.getAchivements());
				promoBackResp.setPanNo(promBackReq.getPanNo().toUpperCase());
				promoBackResp.setGender(
						promBackReq.getGender() != null ? Gender.getById(promBackReq.getGender()).getValue() : null);
				promoBackResp.setRelationshipType(promBackReq.getRelationshipType() != null
						? DirectorRelationshipType.getById(promBackReq.getRelationshipType()).getValue()
						: null);
				promoBackResp.setDin(promBackReq.getDin() != null ? promBackReq.getDin().toString() : null);
				promoBackResp.setPromotorsName((promBackReq.getSalutationId() != null
						? Title.getById(promBackReq.getSalutationId()).getValue() + " "
						: "") + promBackReq.getPromotorsName());
				promoBackResp.setTotalExperience(
						promBackReq.getTotalExperience() != null ? promBackReq.getTotalExperience().toString() : null);
				promoBackResp.setDobDate(!CommonUtils.isObjectNullOrEmpty(promBackReq.getDob())
						? DATE_FORMAT.parse(DATE_FORMAT.format(promBackReq.getDob()))
						: null);
				promoBackResp.setAppointment(!CommonUtils.isObjectNullOrEmpty(promBackReq.getAppointmentDate())
						? DATE_FORMAT.parse(DATE_FORMAT.format(promBackReq.getAppointmentDate()))
						: null);
				promoBackResp
						.setNetworth(promBackReq.getNetworth() != null ? promBackReq.getNetworth().toString() : null);
				PromotorBackgroundDetailResponse.printFields(promoBackResp);
				promoBackRespList.add(promoBackResp);
			}
			response.setPromoBackRespList(promoBackRespList);
		} catch (Exception e) {
			logger.info("Throw Exception While Get Primary Promotor Background Details in DDR OneForm");
			e.printStackTrace();
		}

		// OWNERSHIP DETAILS :- LINENO:12
		try {
			List<OwnershipDetail> ownershipList = ownershipDetailsRepository.listOwnershipFromAppId(applicationId);
			List<OwnershipDetailResponse> ownershipRespList = new ArrayList<>(ownershipList.size());
			OwnershipDetailResponse ownershipResp = null;
			for (OwnershipDetail ownershipReq : ownershipList) {
				ownershipResp = new OwnershipDetailResponse();
				BeanUtils.copyProperties(ownershipReq, ownershipResp);
				ownershipResp.setShareHoldingCategory(
						ShareHoldingCategory.getById(ownershipReq.getShareHoldingCategoryId()).getValue());
				OwnershipDetailResponse.printFields(ownershipResp);
				ownershipRespList.add(ownershipResp);
			}
			response.setOwnershipRespList(ownershipRespList);
		} catch (Exception e) {
			logger.info("Throw Exception While Get Primary Ownership Details in DDR OneForm");
			e.printStackTrace();
		}

		// CURRENT FINANCIAL ARRANGEMENT DETAILS (Existing Banker(s) Details) :-
		// LINENO:21
		if (!setExistingData) {
			response.setFinancialArrangementsDetailResponseList(setFinancialArrangDetails(applicationId));
		}

		// SECURITY DETAIL :- LINENO:12
		try {
			response.setSecurityCorporateDetailList(
					securityCorporateDetailsService.getsecurityCorporateDetailsList(applicationId, userId));
		} catch (Exception e) {
			logger.info("Throw Exception While Get Primary Security Details in DDR OneForm");
			e.printStackTrace();
		}

		// ----------- DIRECTOR BACKGROUND DETAIL :- LINENO:12
		if (!setExistingData) {
			response.setDirectorBackgroundDetailResponses(setDirectorBackDetails(applicationId));
		}

		// -----------------PRODUCT DETAILS PROPOSED AND EXISTING (Description of
		// Products) :- LINENO:111
		try {
			response.setProposedProductDetailList(
					proposedProductDetailsService.getProposedProductDetailList(applicationId, userId));
			response.setExistingProductDetailList(
					existingProductDetailsService.getExistingProductDetailList(applicationId, userId));
		} catch (Exception e) {
			logger.info("Throw Exception While Get Product Proposed and Existing details in DDR OneForm");
			e.printStackTrace();
		}

		// ASSOCIATES CONCERN :- LINENO:17
		try {
			response.setAssociatedConcernDetailList(
					associatedConcernDetailService.getAssociatedConcernsDetailList(applicationId, userId));
		} catch (Exception e) {
			logger.info("Throw Exception While Get associates concern in DDR OneForm");
			e.printStackTrace();
		}
		response.setdDRCMACalculationList(getCMAandCOActDetails(applicationId));

		/*
		 * try { List<ReferencesRetailDetail> referencesRetailList =
		 * referenceRetailDetailsRepository.listReferencesRetailFromAppId(applicationId)
		 * ; List<ReferenceRetailDetailsRequest> referencesResponseList = new
		 * ArrayList<>(referencesRetailList.size()); ReferenceRetailDetailsRequest
		 * referencesResponse = null; for(ReferencesRetailDetail referencesRetail :
		 * referencesRetailList) { referencesResponse = new
		 * ReferenceRetailDetailsRequest(); BeanUtils.copyProperties(referencesRetail,
		 * referencesResponse);
		 * ReferenceRetailDetailsRequest.printFields(referencesResponse);
		 * referencesResponseList.add(referencesResponse); }
		 * response.setReferencesResponseList(referencesResponseList); } catch
		 * (Exception e) {
		 * logger.info("Throw Exception While Get Reference Details in DDR OneForm");
		 * e.printStackTrace(); }
		 */
		return response;
	}

	private List<FinancialArrangementDetailResponseString> setFinancialArrangDetails(Long applicationId) {
		try {
			List<FinancialArrangementsDetail> financialArrangementsList = financialArrangementDetailsRepository
					.listSecurityCorporateDetailFromAppId(applicationId);
			List<FinancialArrangementDetailResponseString> finArrDetailResList = new ArrayList<>(
					financialArrangementsList.size());
			FinancialArrangementDetailResponseString finArrDetailRes = null;
			for (FinancialArrangementsDetail finArrDetailReq : financialArrangementsList) {
				finArrDetailRes = new FinancialArrangementDetailResponseString();
				try {
					DDRExistingBankerDetails ddrExsBankerDetails = ddrExistingBankerDetailsRepository
							.findByFinancialArrangementIdAndIsActive(finArrDetailReq.getId(), true);
					if (!CommonUtils.isObjectNullOrEmpty(ddrExsBankerDetails)) {
						finArrDetailRes.setAddress(ddrExsBankerDetails.getAddress());
						finArrDetailRes.setRelationshipSince(ddrExsBankerDetails.getRelationshipSince());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (CommonUtils.isObjectNullOrEmpty(finArrDetailRes.getRelationshipSince())) {
					finArrDetailRes.setRelationshipSince(finArrDetailReq.getRelationshipSince());
				}
				finArrDetailRes.setOutstandingAmount(convertDouble(finArrDetailReq.getOutstandingAmount()));
				finArrDetailRes.setSecurityDetails(finArrDetailReq.getSecurityDetails());
				/*
				 * financialArrangementsDetailResponse.setAddress(
				 * financialArrangementsDetailRequest.getAddress());
				 */
				finArrDetailRes.setAmount(convertDouble(finArrDetailReq.getAmount()));
				// financialArrangementsDetailResponse.setAddress(financialArrangementsDetailRequest.getAddress());
				// financialArrangementsDetailResponse.setLenderType(LenderType.getById(financialArrangementsDetailRequest.getLenderType()).getValue());
				finArrDetailRes.setLoanDate(finArrDetailReq.getLoanDate());
				finArrDetailRes.setLoanType(finArrDetailReq.getLoanType());
				finArrDetailRes.setFinancialInstitutionName(finArrDetailReq.getFinancialInstitutionName());
				finArrDetailRes.setReportedDate(finArrDetailReq.getReportedDate());
				// financialArrangementsDetailResponse.setFacilityNature(NatureFacility.getById(financialArrangementsDetailRequest.getFacilityNatureId()).getValue());
				if (!CommonUtils.isObjectNullOrEmpty(finArrDetailReq.getRelationshipSince())) {
					finArrDetailRes.setRelationshipSinceInYear(
							CommonUtils.isObjectNullOrEmpty(finArrDetailReq.getRelationshipSince()) ? null
									: finArrDetailReq.getRelationshipSince().toString());
				}
				finArrDetailResList.add(finArrDetailRes);
			}
			return finArrDetailResList;
		} catch (Exception e) {
			logger.error("Problem to get DDR Financial Arrangement Detail Response {}", e);
		}
		return Collections.emptyList();
	}

	private List<DirectorBackgroundDetailResponse> setDirectorBackDetails(Long applicationId) {
		try {
			List<DirectorBackgroundDetail> directorBackgroundList = directorBackgroundDetailsRepository
					.listPromotorBackgroundFromAppId(applicationId);
			List<DirectorBackgroundDetailResponse> dirBackDetailResList = new ArrayList<>(
					directorBackgroundList.size());
			for (DirectorBackgroundDetail directorBackgroundDetailRequest : directorBackgroundList) {
				DirectorBackgroundDetailResponse dirBackDetailRes = new DirectorBackgroundDetailResponse();
				// directorBackgroundDetailResponse.setAchivements(directorBackgroundDetailRequest.getAchivements());
				dirBackDetailRes.setAddress(directorBackgroundDetailRequest.getAddress());
				// directorBackgroundDetailResponse.setAge(directorBackgroundDetailRequest.getAge());
				// directorBackgroundDetailResponse.setPanNo(directorBackgroundDetailRequest.getPanNo());
				// directorBackgroundDetailResponse.setDirectorsName((directorBackgroundDetailRequest.getSalutationId()
				// != null ?
				// Title.getById(directorBackgroundDetailRequest.getSalutationId()).getValue() :
				// null )+ " " + directorBackgroundDetailRequest.getDirectorsName());
				dirBackDetailRes.setPanNo(directorBackgroundDetailRequest.getPanNo().toUpperCase());
				String directorName = "";
				if (directorBackgroundDetailRequest.getDirectorsName() != null) {
					directorName += " " + directorBackgroundDetailRequest.getDirectorsName();
				} else {
					if (directorBackgroundDetailRequest.getTitle() != null)
						directorName += " " + directorBackgroundDetailRequest.getTitle();
					directorName += " " + directorBackgroundDetailRequest.getFirstName();
					directorName += " " + directorBackgroundDetailRequest.getMiddleName();
					directorName += " " + directorBackgroundDetailRequest.getLastName();
				}
				dirBackDetailRes.setDirectorsName(directorName);
				// directorBackgroundDetailResponse.setQualification(directorBackgroundDetailRequest.getQualification());
				dirBackDetailRes.setTotalExperience(directorBackgroundDetailRequest.getTotalExperience().toString());
				dirBackDetailRes.setNetworth(directorBackgroundDetailRequest.getNetworth().toString());
				dirBackDetailRes.setDesignation(directorBackgroundDetailRequest.getDesignation());
				dirBackDetailRes.setAppointmentDate(directorBackgroundDetailRequest.getAppointmentDate());
				dirBackDetailRes.setDin(directorBackgroundDetailRequest.getDin());
				dirBackDetailRes.setMobile(directorBackgroundDetailRequest.getMobile());
				dirBackDetailRes.setDob(!CommonUtils.isObjectNullOrEmpty(directorBackgroundDetailRequest.getDob())
						? DATE_FORMAT.parse(DATE_FORMAT.format(directorBackgroundDetailRequest.getDob()))
						: null);
				dirBackDetailRes.setPincode(directorBackgroundDetailRequest.getPincode());
				dirBackDetailRes.setStateCode(directorBackgroundDetailRequest.getStateCode());
				dirBackDetailRes.setCity(directorBackgroundDetailRequest.getCity());
				dirBackDetailRes.setGender((directorBackgroundDetailRequest.getGender() != null
						? Gender.getById(directorBackgroundDetailRequest.getGender()).getValue()
						: " "));
				dirBackDetailRes.setRelationshipType((directorBackgroundDetailRequest.getRelationshipType() != null
						? DirectorRelationshipType.getById(directorBackgroundDetailRequest.getRelationshipType())
								.getValue()
						: " "));
				dirBackDetailResList.add(dirBackDetailRes);
			}
			return dirBackDetailResList;
		} catch (Exception e) {
			logger.error("Problem to get Data of Director's Background {}", e);
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	private String getCityName(Long cityId) {
		try {
			if (CommonUtils.isObjectNullOrEmpty(cityId)) {
				return null;
			}
			List<Long> cityList = new ArrayList<>(1);
			cityList.add(cityId);
			OneFormResponse oneFormResponse = oneFormClient.getCityByCityListId(cityList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0),
						MasterResponse.class);
				return masterResponse.getValue();
			}
		} catch (Exception e) {
			logger.info("Throw Exception while get city name by city Id in DDR Onform");
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private String getStateName(Integer stateId) {
		try {
			if (CommonUtils.isObjectNullOrEmpty(stateId)) {
				return null;
			}
			List<Long> stateList = new ArrayList<>(1);
			stateList.add(stateId.longValue());
			OneFormResponse oneFormResponse = oneFormClient.getStateByStateListId(stateList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0),
						MasterResponse.class);
				return masterResponse.getValue();
			}
		} catch (Exception e) {
			logger.info("Throw Exception while get city name by city Id in DDR Onform");
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private String getCountryName(Integer country) {
		try {
			if (CommonUtils.isObjectNullOrEmpty(country)) {
				return null;
			}
			List<Long> countryList = new ArrayList<>(1);
			countryList.add(country.longValue());
			OneFormResponse oneFormResponse = oneFormClient.getCountryByCountryListId(countryList);
			List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse.getListData();
			if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
				MasterResponse masterResponse = MultipleJSONObjectHelper.getObjectFromMap(oneResponseDataList.get(0),
						MasterResponse.class);
				return masterResponse.getValue();
			}
		} catch (Exception e) {
			logger.info("Throw Exception while get country name by country Id in DDR Onform");
			e.printStackTrace();
		}
		return null;
	}

	private Double getCMATotalSalesByAppIdAndYear(Long applicationId, String year) {
		try {
			OperatingStatementDetails operatingStatementDetails = operatingStatementDetailsRepository
					.getOperatingStatementDetails(applicationId, year);
			if (CommonUtils.isObjectNullOrEmpty(operatingStatementDetails)) {
				ProfitibilityStatementDetail profitibilityStatementDetail = profitibilityStatementDetailRepository
						.getProfitibilityStatementDetail(applicationId, year);
				if (!CommonUtils.isObjectNullOrEmpty(profitibilityStatementDetail)) {
					return CommonUtils.checkDouble(profitibilityStatementDetail.getNetSales());
				}
			} else {
				if (!CommonUtils.isObjectNullOrEmpty(operatingStatementDetails.getNetSales())) {
					return CommonUtils.checkDouble(operatingStatementDetails.getNetSales());
				}
			}
		} catch (Exception e) {
			logger.info("Throw Exception While Get Total Sales From CMA or Company Act----- appId----->" + applicationId
					+ "-----year-------" + year);
			e.printStackTrace();
		}
		return 0.0;
	}

	public List<DDRCMACalculationResponse> getCMAandCOActDetails(Long applicationId) {
		List<DDRCMACalculationResponse> responseList = new ArrayList<>();

		try {
			boolean isCMAUpload = false;

			List<OperatingStatementDetails> operatingStatementDetails = operatingStatementDetailsRepository
					.getByApplicationId(applicationId);
			List<ProfitibilityStatementDetail> profitibilityStatementList = null;

			OperatingStatementDetails cma2018OSDetails = null;
			OperatingStatementDetails cma2017OSDetails = null;
			OperatingStatementDetails cma2016OSDetails = null;

			ProfitibilityStatementDetail coAct2018OSDetails = null;
			ProfitibilityStatementDetail coAct2017OSDetails = null;
			ProfitibilityStatementDetail coAct2016OSDetails = null;

			if (CommonUtils.isObjectListNull(operatingStatementDetails)) {
				profitibilityStatementList = profitibilityStatementDetailRepository.getByApplicationId(applicationId);
				if (CommonUtils.isObjectListNull(profitibilityStatementList)) {
					logger.info("User not filled CMA or CO Act Sheet");
					return responseList;
				}
				coAct2018OSDetails = profitibilityStatementList.stream()
						.filter(a -> "2018".equals(a.getYear()) || "2018.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(coAct2018OSDetails)) {
					coAct2018OSDetails = new ProfitibilityStatementDetail();
				}
				coAct2017OSDetails = profitibilityStatementList.stream()
						.filter(a -> "2017".equals(a.getYear()) || "2017.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(coAct2017OSDetails)) {
					coAct2017OSDetails = new ProfitibilityStatementDetail();
				}
				coAct2016OSDetails = profitibilityStatementList.stream()
						.filter(a -> "2016".equals(a.getYear()) || "2016.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(coAct2016OSDetails)) {
					coAct2016OSDetails = new ProfitibilityStatementDetail();
				}
			} else {
				isCMAUpload = true;
				cma2018OSDetails = operatingStatementDetails.stream()
						.filter(a -> "2018".equals(a.getYear()) || "2018.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2018OSDetails)) {
					cma2018OSDetails = new OperatingStatementDetails();
				}
				cma2017OSDetails = operatingStatementDetails.stream()
						.filter(a -> "2017".equals(a.getYear()) || "2017.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2017OSDetails)) {
					cma2017OSDetails = new OperatingStatementDetails();
				}
				cma2016OSDetails = operatingStatementDetails.stream()
						.filter(a -> "2016".equals(a.getYear()) || "2016.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2016OSDetails)) {
					cma2016OSDetails = new OperatingStatementDetails();
				}
			}

			List<AssetsDetails> cmaAssetsDetails = null;
			AssetsDetails cma2018AssetDetails = null;
			AssetsDetails cma2017AssetDetails = null;
			AssetsDetails cma2016AssetDetails = null;
			AssetsDetails cma2015AssetDetails = null;
			if (isCMAUpload) {
				cmaAssetsDetails = assetsDetailsRepository.getByApplicationId(applicationId);
				cma2018AssetDetails = cmaAssetsDetails.stream()
						.filter(a -> "2018".equals(a.getYear()) || "2018.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2018AssetDetails)) {
					cma2018AssetDetails = new AssetsDetails();
				}
				cma2017AssetDetails = cmaAssetsDetails.stream()
						.filter(a -> "2017".equals(a.getYear()) || "2017.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2017AssetDetails)) {
					cma2017AssetDetails = new AssetsDetails();
				}
				cma2016AssetDetails = cmaAssetsDetails.stream()
						.filter(a -> "2016".equals(a.getYear()) || "2016.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2016AssetDetails)) {
					cma2016AssetDetails = new AssetsDetails();
				}
				cma2015AssetDetails = cmaAssetsDetails.stream()
						.filter(a -> "2015".equals(a.getYear()) || "2015.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2015AssetDetails)) {
					cma2015AssetDetails = new AssetsDetails();
				}
			}

			List<LiabilitiesDetails> liabilitiesDetailsList = null;
			LiabilitiesDetails cma2018Liabilities = null;
			LiabilitiesDetails cma2017Liabilities = null;
			LiabilitiesDetails cma2016Liabilities = null;
			LiabilitiesDetails cma2015Liabilities = null;
			if (isCMAUpload) {
				liabilitiesDetailsList = liabilitiesDetailsRepository.getByApplicationId(applicationId);
				cma2018Liabilities = liabilitiesDetailsList.stream()
						.filter(a -> "2018".equals(a.getYear()) || "2018.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2018Liabilities)) {
					cma2018Liabilities = new LiabilitiesDetails();
				}
				cma2017Liabilities = liabilitiesDetailsList.stream()
						.filter(a -> "2017".equals(a.getYear()) || "2017.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2017Liabilities)) {
					cma2017Liabilities = new LiabilitiesDetails();
				}
				cma2016Liabilities = liabilitiesDetailsList.stream()
						.filter(a -> "2016".equals(a.getYear()) || "2016.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2016Liabilities)) {
					cma2016Liabilities = new LiabilitiesDetails();
				}
				cma2015Liabilities = liabilitiesDetailsList.stream()
						.filter(a -> "2015".equals(a.getYear()) || "2015.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(cma2015Liabilities)) {
					cma2015Liabilities = new LiabilitiesDetails();
				}
			}

			List<BalanceSheetDetail> balanceSheetDetailList = null;
			BalanceSheetDetail coAct2018BalanceSheet = null;
			BalanceSheetDetail coAct2017BalanceSheet = null;
			BalanceSheetDetail coAct2016BalanceSheet = null;
			BalanceSheetDetail coAct2015BalanceSheet = null;
			if (!isCMAUpload) {
				balanceSheetDetailList = balanceSheetDetailRepository.getByApplicationId(applicationId);
				coAct2018BalanceSheet = balanceSheetDetailList.stream()
						.filter(a -> "2018".equals(a.getYear()) || "2018.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(coAct2018BalanceSheet)) {
					coAct2018BalanceSheet = new BalanceSheetDetail();
				}
				coAct2017BalanceSheet = balanceSheetDetailList.stream()
						.filter(a -> "2017".equals(a.getYear()) || "2017.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(coAct2017BalanceSheet)) {
					coAct2017BalanceSheet = new BalanceSheetDetail();
				}
				coAct2016BalanceSheet = balanceSheetDetailList.stream()
						.filter(a -> "2016".equals(a.getYear()) || "2016.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(coAct2016BalanceSheet)) {
					coAct2016BalanceSheet = new BalanceSheetDetail();
				}
				coAct2015BalanceSheet = balanceSheetDetailList.stream()
						.filter(a -> "2015".equals(a.getYear()) || "2015.0".equals(a.getYear())).findFirst()
						.orElse(null);
				if (CommonUtils.isObjectNullOrEmpty(coAct2015BalanceSheet)) {
					coAct2015BalanceSheet = new BalanceSheetDetail();
				}
			}

			DDRCMACalculationResponse totalSalesResponse = new DDRCMACalculationResponse();
			totalSalesResponse.setKeyId(DDRFinancialSummaryFields.FIRST_TOTAL_SALES.getId());
			totalSalesResponse.setKeyName(DDRFinancialSummaryFields.FIRST_TOTAL_SALES.getValue());
			totalSalesResponse.setProvisionalYear(isCMAUpload ? CommonUtils.checkDouble(cma2018OSDetails.getNetSales())
					: CommonUtils.checkDouble(coAct2018OSDetails.getNetSales()));
			totalSalesResponse.setLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2017OSDetails.getNetSales())
					: CommonUtils.checkDouble(coAct2017OSDetails.getNetSales()));
			totalSalesResponse.setLastToLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2016OSDetails.getNetSales())
					: CommonUtils.checkDouble(coAct2016OSDetails.getNetSales()));
			totalSalesResponse.setDiffPvsnlAndLastYear(CommonUtils
					.checkDouble(((totalSalesResponse.getProvisionalYear() - totalSalesResponse.getLastYear())
							/ totalSalesResponse.getLastYear()) * 100));

			totalSalesResponse
					.setProvisionalYearString(isCMAUpload ? CommonUtils.checkString(cma2018OSDetails.getNetSales())
							: CommonUtils.checkString(coAct2018OSDetails.getNetSales()));
			totalSalesResponse.setLastYearString(isCMAUpload ? CommonUtils.checkString(cma2017OSDetails.getNetSales())
					: CommonUtils.checkString(coAct2017OSDetails.getNetSales()));
			totalSalesResponse
					.setLastToLastYearString(isCMAUpload ? CommonUtils.checkString(cma2016OSDetails.getNetSales())
							: CommonUtils.checkString(coAct2016OSDetails.getNetSales()));
			totalSalesResponse.setDiffPvsnlAndLastYearString(CommonUtils.checkString(CommonUtils
					.checkDouble(((totalSalesResponse.getProvisionalYear() - totalSalesResponse.getLastYear())
							/ totalSalesResponse.getLastYear()) * 100)));
			responseList.add(totalSalesResponse);

			DDRCMACalculationResponse interestCostResponse = new DDRCMACalculationResponse();
			interestCostResponse.setKeyId(DDRFinancialSummaryFields.INTEREST_COST.getId());
			interestCostResponse.setKeyName(DDRFinancialSummaryFields.INTEREST_COST.getValue());

			interestCostResponse
					.setProvisionalYear(isCMAUpload ? CommonUtils.checkDouble(cma2018OSDetails.getInterest())
							: CommonUtils.checkDouble(coAct2018OSDetails.getFinanceCost()));
			interestCostResponse.setLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2017OSDetails.getInterest())
					: CommonUtils.checkDouble(coAct2017OSDetails.getFinanceCost()));
			interestCostResponse.setLastToLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2016OSDetails.getInterest())
					: CommonUtils.checkDouble(coAct2016OSDetails.getFinanceCost()));
			interestCostResponse.setDiffPvsnlAndLastYear(calculateFinancialSummary(
					interestCostResponse.getProvisionalYear(), interestCostResponse.getLastYear()));

			interestCostResponse
					.setProvisionalYearString(isCMAUpload ? CommonUtils.checkString(cma2018OSDetails.getInterest())
							: CommonUtils.checkString(coAct2018OSDetails.getFinanceCost()));
			interestCostResponse.setLastYearString(isCMAUpload ? CommonUtils.checkString(cma2017OSDetails.getInterest())
					: CommonUtils.checkString(coAct2017OSDetails.getFinanceCost()));
			interestCostResponse
					.setLastToLastYearString(isCMAUpload ? CommonUtils.checkString(cma2016OSDetails.getInterest())
							: CommonUtils.checkString(coAct2016OSDetails.getFinanceCost()));
			interestCostResponse.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					interestCostResponse.getProvisionalYear(), interestCostResponse.getLastYear()));
			responseList.add(interestCostResponse);

			DDRCMACalculationResponse profitBeforeTaxResponse = new DDRCMACalculationResponse();
			profitBeforeTaxResponse.setKeyId(DDRFinancialSummaryFields.PROFIT_BEFORE_TAX.getId());
			profitBeforeTaxResponse.setKeyName(DDRFinancialSummaryFields.PROFIT_BEFORE_TAX.getValue());

			profitBeforeTaxResponse.setProvisionalYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2018OSDetails.getProfitBeforeTaxOrLoss())
							: CommonUtils.checkDouble(coAct2018OSDetails.getProfitBeforeTax()));
			profitBeforeTaxResponse
					.setLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2017OSDetails.getProfitBeforeTaxOrLoss())
							: CommonUtils.checkDouble(coAct2017OSDetails.getProfitBeforeTax()));
			profitBeforeTaxResponse.setLastToLastYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2016OSDetails.getProfitBeforeTaxOrLoss())
							: CommonUtils.checkDouble(coAct2016OSDetails.getProfitBeforeTax()));
			profitBeforeTaxResponse.setDiffPvsnlAndLastYear(calculateFinancialSummary(
					profitBeforeTaxResponse.getProvisionalYear(), profitBeforeTaxResponse.getLastYear()));

			profitBeforeTaxResponse.setProvisionalYearString(
					isCMAUpload ? CommonUtils.checkString(cma2018OSDetails.getProfitBeforeTaxOrLoss())
							: CommonUtils.checkString(coAct2018OSDetails.getProfitBeforeTax()));
			profitBeforeTaxResponse.setLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2017OSDetails.getProfitBeforeTaxOrLoss())
							: CommonUtils.checkString(coAct2017OSDetails.getProfitBeforeTax()));
			profitBeforeTaxResponse.setLastToLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2016OSDetails.getProfitBeforeTaxOrLoss())
							: CommonUtils.checkString(coAct2016OSDetails.getProfitBeforeTax()));
			profitBeforeTaxResponse.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					profitBeforeTaxResponse.getProvisionalYear(), profitBeforeTaxResponse.getLastYear()));
			responseList.add(profitBeforeTaxResponse);

			DDRCMACalculationResponse profitAfterTaxResponse = new DDRCMACalculationResponse();
			profitAfterTaxResponse.setKeyId(DDRFinancialSummaryFields.PROFIT_AFTER_TAX.getId());
			profitAfterTaxResponse.setKeyName(DDRFinancialSummaryFields.PROFIT_AFTER_TAX.getValue());

			profitAfterTaxResponse
					.setProvisionalYear(isCMAUpload ? CommonUtils.checkDouble(cma2018OSDetails.getNetProfitOrLoss())
							: CommonUtils.checkDouble(coAct2018OSDetails.getProfitAfterTax()));
			profitAfterTaxResponse
					.setLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2017OSDetails.getNetProfitOrLoss())
							: CommonUtils.checkDouble(coAct2017OSDetails.getProfitAfterTax()));
			profitAfterTaxResponse
					.setLastToLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2016OSDetails.getNetProfitOrLoss())
							: CommonUtils.checkDouble(coAct2016OSDetails.getProfitAfterTax()));
			profitAfterTaxResponse.setDiffPvsnlAndLastYear(calculateFinancialSummary(
					profitAfterTaxResponse.getProvisionalYear(), profitAfterTaxResponse.getLastYear()));

			profitAfterTaxResponse.setProvisionalYearString(
					isCMAUpload ? CommonUtils.checkString(cma2018OSDetails.getNetProfitOrLoss())
							: CommonUtils.checkString(coAct2018OSDetails.getProfitAfterTax()));
			profitAfterTaxResponse
					.setLastYearString(isCMAUpload ? CommonUtils.checkString(cma2017OSDetails.getNetProfitOrLoss())
							: CommonUtils.checkString(coAct2017OSDetails.getProfitAfterTax()));
			profitAfterTaxResponse.setLastToLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2016OSDetails.getNetProfitOrLoss())
							: CommonUtils.checkString(coAct2016OSDetails.getProfitAfterTax()));
			profitAfterTaxResponse.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					profitAfterTaxResponse.getProvisionalYear(), profitAfterTaxResponse.getLastYear()));
			responseList.add(profitAfterTaxResponse);

			DDRCMACalculationResponse netWorthResponse = new DDRCMACalculationResponse();
			netWorthResponse.setKeyId(DDRFinancialSummaryFields.NET_WORTH.getId());
			netWorthResponse.setKeyName(DDRFinancialSummaryFields.NET_WORTH.getValue());

			if (isCMAUpload) {
				netWorthResponse.setProvisionalYear(CommonUtils.checkDouble(cma2018AssetDetails.getTangibleNetWorth()));
				netWorthResponse.setLastYear(CommonUtils.checkDouble(cma2017AssetDetails.getTangibleNetWorth()));
				netWorthResponse.setLastToLastYear(CommonUtils.checkDouble(cma2016AssetDetails.getTangibleNetWorth()));
				netWorthResponse
						.setProvisionalYearString(CommonUtils.checkString(cma2018AssetDetails.getTangibleNetWorth()));
				netWorthResponse.setLastYearString(CommonUtils.checkString(cma2017AssetDetails.getTangibleNetWorth()));
				netWorthResponse
						.setLastToLastYearString(CommonUtils.checkString(cma2016AssetDetails.getTangibleNetWorth()));
			} else {
				double totalPrvsl2018Year = CommonUtils.checkDouble(coAct2018BalanceSheet.getGrandTotal())
						- CommonUtils.checkDouble(coAct2018BalanceSheet.getOtherNonCurrentLiability())
						- CommonUtils.checkDouble(coAct2018BalanceSheet.getOtherNonCurrentLiability())
						- CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentLiability())
						- CommonUtils.checkDouble(coAct2018BalanceSheet.getIntangibleAssets())
						- CommonUtils.checkDouble(coAct2018BalanceSheet.getRevaluationReserve());
				netWorthResponse.setProvisionalYear(totalPrvsl2018Year);
				netWorthResponse.setProvisionalYearString(CommonUtils.checkString(totalPrvsl2018Year));
				double totalPrvsl2017Year = CommonUtils.checkDouble(coAct2017BalanceSheet.getGrandTotal())
						- CommonUtils.checkDouble(coAct2017BalanceSheet.getOtherNonCurrentLiability())
						- CommonUtils.checkDouble(coAct2017BalanceSheet.getOtherNonCurrentLiability())
						- CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentLiability())
						- CommonUtils.checkDouble(coAct2017BalanceSheet.getIntangibleAssets())
						- CommonUtils.checkDouble(coAct2017BalanceSheet.getRevaluationReserve());
				netWorthResponse.setLastYear(totalPrvsl2017Year);
				netWorthResponse.setLastYearString(CommonUtils.checkString(totalPrvsl2017Year));
				double totalPrvsl2016Year = CommonUtils.checkDouble(coAct2016BalanceSheet.getGrandTotal())
						- CommonUtils.checkDouble(coAct2016BalanceSheet.getOtherNonCurrentLiability())
						- CommonUtils.checkDouble(coAct2016BalanceSheet.getOtherNonCurrentLiability())
						- CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentLiability())
						- CommonUtils.checkDouble(coAct2016BalanceSheet.getIntangibleAssets())
						- CommonUtils.checkDouble(coAct2016BalanceSheet.getRevaluationReserve());
				netWorthResponse.setLastToLastYear(totalPrvsl2016Year);
				netWorthResponse.setLastToLastYearString(CommonUtils.checkString(totalPrvsl2016Year));
			}
			netWorthResponse.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(netWorthResponse.getProvisionalYear(), netWorthResponse.getLastYear()));

			netWorthResponse.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					netWorthResponse.getProvisionalYear(), netWorthResponse.getLastYear()));
			responseList.add(netWorthResponse);

			DDRCMACalculationResponse adjustedNetWorth = new DDRCMACalculationResponse();
			adjustedNetWorth.setKeyId(DDRFinancialSummaryFields.ADJUSTED_NET_WORTH.getId());
			adjustedNetWorth.setKeyName(DDRFinancialSummaryFields.ADJUSTED_NET_WORTH.getValue());

			if (isCMAUpload) {
				adjustedNetWorth.setProvisionalYear(CommonUtils.checkDouble(netWorthResponse.getProvisionalYear())
						- CommonUtils.checkDouble(cma2018Liabilities.getOtherNclUnsecuredLoansFromPromoters()));
				adjustedNetWorth.setLastYear(CommonUtils.checkDouble(netWorthResponse.getLastYear())
						- CommonUtils.checkDouble(cma2017Liabilities.getOtherNclUnsecuredLoansFromPromoters()));
				adjustedNetWorth.setLastToLastYear(CommonUtils.checkDouble(netWorthResponse.getLastToLastYear())
						- CommonUtils.checkDouble(cma2016Liabilities.getOtherNclUnsecuredLoansFromPromoters()));

				adjustedNetWorth.setProvisionalYearString(CommonUtils
						.checkString(CommonUtils.checkDouble(netWorthResponse.getProvisionalYear()) - CommonUtils
								.checkDouble(cma2018Liabilities.getOtherNclUnsecuredLoansFromPromoters())));
				adjustedNetWorth.setLastYearString(
						CommonUtils.checkString(CommonUtils.checkDouble(netWorthResponse.getLastYear()) - CommonUtils
								.checkDouble(cma2017Liabilities.getOtherNclUnsecuredLoansFromPromoters())));
				adjustedNetWorth.setLastToLastYearString(CommonUtils
						.checkString(CommonUtils.checkDouble(netWorthResponse.getLastToLastYear()) - CommonUtils
								.checkDouble(cma2016Liabilities.getOtherNclUnsecuredLoansFromPromoters())));
			} else {
				adjustedNetWorth.setProvisionalYear(CommonUtils.checkDouble(netWorthResponse.getProvisionalYear())
						- CommonUtils.checkDouble(coAct2018BalanceSheet.getUnsecuredLoansFromPromoters()));
				adjustedNetWorth.setLastYear(CommonUtils.checkDouble(netWorthResponse.getLastYear())
						- CommonUtils.checkDouble(coAct2017BalanceSheet.getUnsecuredLoansFromPromoters()));
				adjustedNetWorth.setLastToLastYear(CommonUtils.checkDouble(netWorthResponse.getLastToLastYear())
						- CommonUtils.checkDouble(coAct2016BalanceSheet.getUnsecuredLoansFromPromoters()));

				adjustedNetWorth.setProvisionalYearString(
						CommonUtils.checkString(CommonUtils.checkDouble(netWorthResponse.getProvisionalYear())
								- CommonUtils.checkDouble(coAct2018BalanceSheet.getUnsecuredLoansFromPromoters())));
				adjustedNetWorth.setLastYearString(
						CommonUtils.checkString(CommonUtils.checkDouble(netWorthResponse.getLastYear())
								- CommonUtils.checkDouble(coAct2017BalanceSheet.getUnsecuredLoansFromPromoters())));
				adjustedNetWorth.setLastToLastYearString(
						CommonUtils.checkString(CommonUtils.checkDouble(netWorthResponse.getLastToLastYear())
								- CommonUtils.checkDouble(coAct2016BalanceSheet.getUnsecuredLoansFromPromoters())));
			}
			adjustedNetWorth.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(adjustedNetWorth.getProvisionalYear(), adjustedNetWorth.getLastYear()));
			adjustedNetWorth.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					adjustedNetWorth.getProvisionalYear(), adjustedNetWorth.getLastYear()));
			responseList.add(adjustedNetWorth);

			DDRCMACalculationResponse totalDebt = new DDRCMACalculationResponse();
			totalDebt.setKeyId(DDRFinancialSummaryFields.TOTAL_DEBT.getId());
			totalDebt.setKeyName(DDRFinancialSummaryFields.TOTAL_DEBT.getValue());

			if (isCMAUpload) {
				totalDebt.setProvisionalYear(CommonUtils.checkDouble(cma2018Liabilities.getTotalOutsideLiabilities()));
				totalDebt.setLastYear(CommonUtils.checkDouble(cma2017Liabilities.getTotalOutsideLiabilities()));
				totalDebt.setLastToLastYear(CommonUtils.checkDouble(cma2016Liabilities.getTotalOutsideLiabilities()));

				totalDebt.setProvisionalYearString(
						CommonUtils.checkString(cma2018Liabilities.getTotalOutsideLiabilities()));
				totalDebt.setLastYearString(CommonUtils.checkString(cma2017Liabilities.getTotalOutsideLiabilities()));
				totalDebt.setLastToLastYearString(
						CommonUtils.checkString(cma2016Liabilities.getTotalOutsideLiabilities()));
			} else {
				totalDebt
						.setProvisionalYear(CommonUtils.checkDouble(coAct2018BalanceSheet.getOtherNonCurrentLiability())
								+ CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentLiability()));
				totalDebt.setLastYear(CommonUtils.checkDouble(coAct2017BalanceSheet.getOtherNonCurrentLiability())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentLiability()));
				totalDebt.setLastToLastYear(CommonUtils.checkDouble(coAct2016BalanceSheet.getOtherNonCurrentLiability())
						+ CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentLiability()));

				totalDebt.setProvisionalYearString(CommonUtils
						.checkString(CommonUtils.checkDouble(coAct2018BalanceSheet.getOtherNonCurrentLiability())
								+ CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentLiability())));
				totalDebt.setLastYearString(CommonUtils
						.checkString(CommonUtils.checkDouble(coAct2017BalanceSheet.getOtherNonCurrentLiability())
								+ CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentLiability())));
				totalDebt.setLastToLastYearString(CommonUtils
						.checkString(CommonUtils.checkDouble(coAct2016BalanceSheet.getOtherNonCurrentLiability())
								+ CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentLiability())));
			}
			totalDebt.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(totalDebt.getProvisionalYear(), totalDebt.getLastYear()));
			totalDebt.setDiffPvsnlAndLastYearString(
					calculateFinancialSummaryString(totalDebt.getProvisionalYear(), totalDebt.getLastYear()));
			responseList.add(totalDebt);

			DDRCMACalculationResponse secureLoanResponse = new DDRCMACalculationResponse();
			secureLoanResponse.setKeyId(DDRFinancialSummaryFields.SECURE_LOAN.getId());
			secureLoanResponse.setKeyName(DDRFinancialSummaryFields.SECURE_LOAN.getValue());

			secureLoanResponse.setProvisionalYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2018Liabilities.getTermLiabilitiesSecured())
							: CommonUtils.checkDouble(coAct2018BalanceSheet.getTermLoansSecured()));
			secureLoanResponse
					.setLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2017Liabilities.getTermLiabilitiesSecured())
							: CommonUtils.checkDouble(coAct2017BalanceSheet.getTermLoansSecured()));
			secureLoanResponse.setLastToLastYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2016Liabilities.getTermLiabilitiesSecured())
							: CommonUtils.checkDouble(coAct2016BalanceSheet.getTermLoansSecured()));
			secureLoanResponse.setDiffPvsnlAndLastYear(calculateFinancialSummary(
					secureLoanResponse.getProvisionalYear(), secureLoanResponse.getLastYear()));

			secureLoanResponse.setProvisionalYearString(
					isCMAUpload ? CommonUtils.checkString(cma2018Liabilities.getTermLiabilitiesSecured())
							: CommonUtils.checkString(coAct2018BalanceSheet.getTermLoansSecured()));
			secureLoanResponse.setLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2017Liabilities.getTermLiabilitiesSecured())
							: CommonUtils.checkString(coAct2017BalanceSheet.getTermLoansSecured()));
			secureLoanResponse.setLastToLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2016Liabilities.getTermLiabilitiesSecured())
							: CommonUtils.checkString(coAct2016BalanceSheet.getTermLoansSecured()));
			secureLoanResponse.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					secureLoanResponse.getProvisionalYear(), secureLoanResponse.getLastYear()));
			responseList.add(secureLoanResponse);

			DDRCMACalculationResponse unsecureLoanResp = new DDRCMACalculationResponse();
			unsecureLoanResp.setKeyId(DDRFinancialSummaryFields.UNSECURE_LOAN.getId());
			unsecureLoanResp.setKeyName(DDRFinancialSummaryFields.UNSECURE_LOAN.getValue());

			if (isCMAUpload) {
				double provYear = CommonUtils.checkDouble(cma2018Liabilities.getTermLiabilitiesUnsecured())
						+ CommonUtils.checkDouble(cma2018Liabilities.getOtherNclUnsecuredLoansFromPromoters())
						+ CommonUtils.checkDouble(cma2018Liabilities.getOtherNclUnsecuredLoansFromOther());
				unsecureLoanResp.setProvisionalYear(provYear);

				double lastYear = CommonUtils.checkDouble(cma2017Liabilities.getTermLiabilitiesUnsecured())
						+ CommonUtils.checkDouble(cma2017Liabilities.getOtherNclUnsecuredLoansFromPromoters())
						+ CommonUtils.checkDouble(cma2017Liabilities.getOtherNclUnsecuredLoansFromOther());
				unsecureLoanResp.setLastYear(lastYear);

				double lastToLastYear = CommonUtils.checkDouble(cma2016Liabilities.getTermLiabilitiesUnsecured())
						+ CommonUtils.checkDouble(cma2016Liabilities.getOtherNclUnsecuredLoansFromPromoters())
						+ CommonUtils.checkDouble(cma2016Liabilities.getOtherNclUnsecuredLoansFromOther());
				unsecureLoanResp.setLastToLastYear(lastToLastYear);

				unsecureLoanResp.setProvisionalYearString(CommonUtils.checkString(provYear));
				unsecureLoanResp.setLastYearString(CommonUtils.checkString(lastYear));
				unsecureLoanResp.setLastToLastYearString(CommonUtils.checkString(lastToLastYear));
			} else {
				double provYear = CommonUtils.checkDouble(coAct2018BalanceSheet.getTermLoansUnsecured())
						+ CommonUtils.checkDouble(coAct2018BalanceSheet.getUnsecuredLoansFromPromoters())
						+ CommonUtils.checkDouble(coAct2018BalanceSheet.getUnsecuredLoansFromOthers());
				unsecureLoanResp.setProvisionalYear(provYear);

				double lastYear = CommonUtils.checkDouble(coAct2017BalanceSheet.getTermLoansUnsecured())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getUnsecuredLoansFromPromoters())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getUnsecuredLoansFromOthers());
				unsecureLoanResp.setLastYear(lastYear);

				double lastToLastYear = CommonUtils.checkDouble(coAct2016BalanceSheet.getTermLoansUnsecured())
						+ CommonUtils.checkDouble(coAct2016BalanceSheet.getUnsecuredLoansFromPromoters())
						+ CommonUtils.checkDouble(coAct2016BalanceSheet.getUnsecuredLoansFromOthers());
				unsecureLoanResp.setLastToLastYear(lastToLastYear);

				unsecureLoanResp.setProvisionalYearString(CommonUtils.checkString(provYear));
				unsecureLoanResp.setLastYearString(CommonUtils.checkString(lastYear));
				unsecureLoanResp.setLastToLastYearString(CommonUtils.checkString(lastToLastYear));
			}
			unsecureLoanResp.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(unsecureLoanResp.getProvisionalYear(), unsecureLoanResp.getLastYear()));
			unsecureLoanResp.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					unsecureLoanResp.getProvisionalYear(), unsecureLoanResp.getLastYear()));
			responseList.add(unsecureLoanResp);

			DDRCMACalculationResponse unSecureLoanFromFriends = new DDRCMACalculationResponse();
			unSecureLoanFromFriends.setKeyId(DDRFinancialSummaryFields.UNSECURE_LOAN_FROM_FRIEND.getId());
			unSecureLoanFromFriends.setKeyName(DDRFinancialSummaryFields.UNSECURE_LOAN_FROM_FRIEND.getValue());

			unSecureLoanFromFriends.setProvisionalYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2018Liabilities.getOtherNclUnsecuredLoansFromPromoters())
							: CommonUtils.checkDouble(coAct2018BalanceSheet.getUnsecuredLoansFromPromoters()));
			unSecureLoanFromFriends.setLastYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2017Liabilities.getOtherNclUnsecuredLoansFromPromoters())
							: CommonUtils.checkDouble(coAct2017BalanceSheet.getUnsecuredLoansFromPromoters()));
			unSecureLoanFromFriends.setLastToLastYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2016Liabilities.getOtherNclUnsecuredLoansFromPromoters())
							: CommonUtils.checkDouble(coAct2016BalanceSheet.getUnsecuredLoansFromPromoters()));
			unSecureLoanFromFriends.setDiffPvsnlAndLastYear(calculateFinancialSummary(
					unSecureLoanFromFriends.getProvisionalYear(), unSecureLoanFromFriends.getLastYear()));

			unSecureLoanFromFriends.setProvisionalYearString(
					isCMAUpload ? CommonUtils.checkString(cma2018Liabilities.getOtherNclUnsecuredLoansFromPromoters())
							: CommonUtils.checkString(coAct2018BalanceSheet.getUnsecuredLoansFromPromoters()));
			unSecureLoanFromFriends.setLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2017Liabilities.getOtherNclUnsecuredLoansFromPromoters())
							: CommonUtils.checkString(coAct2017BalanceSheet.getUnsecuredLoansFromPromoters()));
			unSecureLoanFromFriends.setLastToLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2016Liabilities.getOtherNclUnsecuredLoansFromPromoters())
							: CommonUtils.checkString(coAct2016BalanceSheet.getUnsecuredLoansFromPromoters()));
			unSecureLoanFromFriends.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					unSecureLoanFromFriends.getProvisionalYear(), unSecureLoanFromFriends.getLastYear()));
			responseList.add(unSecureLoanFromFriends);

			DDRCMACalculationResponse capitalResponse = new DDRCMACalculationResponse();
			capitalResponse.setKeyId(DDRFinancialSummaryFields.CAPITAL.getId());
			capitalResponse.setKeyName(DDRFinancialSummaryFields.CAPITAL.getValue());

			if (isCMAUpload) {
				capitalResponse
						.setProvisionalYear(CommonUtils.checkDouble(cma2018Liabilities.getOrdinarySharesCapital()));
				capitalResponse.setLastYear(CommonUtils.checkDouble(cma2017Liabilities.getOrdinarySharesCapital()));
				capitalResponse
						.setLastToLastYear(CommonUtils.checkDouble(cma2016Liabilities.getOrdinarySharesCapital()));

				capitalResponse.setProvisionalYearString(
						CommonUtils.checkString(cma2018Liabilities.getOrdinarySharesCapital()));
				capitalResponse
						.setLastYearString(CommonUtils.checkString(cma2017Liabilities.getOrdinarySharesCapital()));
				capitalResponse.setLastToLastYearString(
						CommonUtils.checkString(cma2016Liabilities.getOrdinarySharesCapital()));
			} else {
				capitalResponse
						.setProvisionalYear(CommonUtils.checkDouble(coAct2018BalanceSheet.getOrdinaryShareCapital())
								+ CommonUtils.checkDouble(coAct2018BalanceSheet.getPreferenceShareCapital())
								+ CommonUtils.checkDouble(coAct2018BalanceSheet.getShareApplicationPendingAllotment()));
				capitalResponse.setLastYear(CommonUtils.checkDouble(coAct2017BalanceSheet.getOrdinaryShareCapital())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getPreferenceShareCapital())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getShareApplicationPendingAllotment()));
				capitalResponse
						.setLastToLastYear(CommonUtils.checkDouble(coAct2016BalanceSheet.getOrdinaryShareCapital())
								+ CommonUtils.checkDouble(coAct2016BalanceSheet.getPreferenceShareCapital())
								+ CommonUtils.checkDouble(coAct2016BalanceSheet.getShareApplicationPendingAllotment()));

				capitalResponse.setProvisionalYearString(CommonUtils.checkString(CommonUtils
						.checkDouble(coAct2018BalanceSheet.getOrdinaryShareCapital())
						+ CommonUtils.checkDouble(coAct2018BalanceSheet.getPreferenceShareCapital())
						+ CommonUtils.checkDouble(coAct2018BalanceSheet.getShareApplicationPendingAllotment())));
				capitalResponse.setLastYearString(CommonUtils.checkString(CommonUtils
						.checkDouble(coAct2017BalanceSheet.getOrdinaryShareCapital())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getPreferenceShareCapital())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getShareApplicationPendingAllotment())));
				capitalResponse.setLastToLastYearString(CommonUtils.checkString(CommonUtils
						.checkDouble(coAct2016BalanceSheet.getOrdinaryShareCapital())
						+ CommonUtils.checkDouble(coAct2016BalanceSheet.getPreferenceShareCapital())
						+ CommonUtils.checkDouble(coAct2016BalanceSheet.getShareApplicationPendingAllotment())));
			}
			capitalResponse.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(capitalResponse.getProvisionalYear(), capitalResponse.getLastYear()));
			capitalResponse.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					capitalResponse.getProvisionalYear(), capitalResponse.getLastYear()));
			responseList.add(capitalResponse);

			DDRCMACalculationResponse totalCurrentAsset = new DDRCMACalculationResponse();
			totalCurrentAsset.setKeyId(DDRFinancialSummaryFields.TOTAL_CURRENT_ASSET.getId());
			totalCurrentAsset.setKeyName(DDRFinancialSummaryFields.TOTAL_CURRENT_ASSET.getValue());

			totalCurrentAsset.setProvisionalYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2018AssetDetails.getTotalCurrentAssets())
							: CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentAssets()));
			totalCurrentAsset
					.setLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2017AssetDetails.getTotalCurrentAssets())
							: CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentAssets()));
			totalCurrentAsset.setLastToLastYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2016AssetDetails.getTotalCurrentAssets())
							: CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentAssets()));
			totalCurrentAsset.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(totalCurrentAsset.getProvisionalYear(), totalCurrentAsset.getLastYear()));

			totalCurrentAsset.setProvisionalYearString(
					isCMAUpload ? CommonUtils.checkString(cma2018AssetDetails.getTotalCurrentAssets())
							: CommonUtils.checkString(coAct2018BalanceSheet.getOthersCurrentAssets()));
			totalCurrentAsset.setLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2017AssetDetails.getTotalCurrentAssets())
							: CommonUtils.checkString(coAct2017BalanceSheet.getOthersCurrentAssets()));
			totalCurrentAsset.setLastToLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2016AssetDetails.getTotalCurrentAssets())
							: CommonUtils.checkString(coAct2016BalanceSheet.getOthersCurrentAssets()));
			totalCurrentAsset.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					totalCurrentAsset.getProvisionalYear(), totalCurrentAsset.getLastYear()));
			responseList.add(totalCurrentAsset);

			DDRCMACalculationResponse totalCurrentLiability = new DDRCMACalculationResponse();
			totalCurrentLiability.setKeyId(DDRFinancialSummaryFields.TOTAL_CURRENT_LIABILITY.getId());
			totalCurrentLiability.setKeyName(DDRFinancialSummaryFields.TOTAL_CURRENT_LIABILITY.getValue());

			totalCurrentLiability.setProvisionalYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2018Liabilities.getTotalCurrentLiabilities())
							: CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentLiability()));
			totalCurrentLiability
					.setLastYear(isCMAUpload ? CommonUtils.checkDouble(cma2017Liabilities.getTotalCurrentLiabilities())
							: CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentLiability()));
			totalCurrentLiability.setLastToLastYear(
					isCMAUpload ? CommonUtils.checkDouble(cma2016Liabilities.getTotalCurrentLiabilities())
							: CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentLiability()));
			totalCurrentLiability.setDiffPvsnlAndLastYear(calculateFinancialSummary(
					totalCurrentLiability.getProvisionalYear(), totalCurrentLiability.getLastYear()));

			totalCurrentLiability.setProvisionalYearString(
					isCMAUpload ? CommonUtils.checkString(cma2018Liabilities.getTotalCurrentLiabilities())
							: CommonUtils.checkString(coAct2018BalanceSheet.getOthersCurrentLiability()));
			totalCurrentLiability.setLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2017Liabilities.getTotalCurrentLiabilities())
							: CommonUtils.checkString(coAct2017BalanceSheet.getOthersCurrentLiability()));
			totalCurrentLiability.setLastToLastYearString(
					isCMAUpload ? CommonUtils.checkString(cma2016Liabilities.getTotalCurrentLiabilities())
							: CommonUtils.checkString(coAct2016BalanceSheet.getOthersCurrentLiability()));
			totalCurrentLiability.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					totalCurrentLiability.getProvisionalYear(), totalCurrentLiability.getLastYear()));
			responseList.add(totalCurrentLiability);

			DDRCMACalculationResponse totalLiability = new DDRCMACalculationResponse();
			totalLiability.setKeyId(DDRFinancialSummaryFields.TOTAL_LIABILITY.getId());
			totalLiability.setKeyName(DDRFinancialSummaryFields.TOTAL_LIABILITY.getValue());

			totalLiability.setProvisionalYear(isCMAUpload
					? CommonUtils.checkDouble(cma2018Liabilities.getTotalOutsideLiabilities())
					: CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentLiability())
							+ CommonUtils.checkDouble(coAct2018BalanceSheet.getTotalCurrentAndNonCurrentLiability()));
			totalLiability.setLastYear(isCMAUpload
					? CommonUtils.checkDouble(cma2017Liabilities.getTotalOutsideLiabilities())
					: CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentLiability())
							+ CommonUtils.checkDouble(coAct2017BalanceSheet.getTotalCurrentAndNonCurrentLiability()));
			totalLiability.setLastToLastYear(isCMAUpload
					? CommonUtils.checkDouble(cma2016Liabilities.getTotalOutsideLiabilities())
					: CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentLiability())
							+ CommonUtils.checkDouble(coAct2016BalanceSheet.getTotalCurrentAndNonCurrentLiability()));
			totalLiability.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(totalLiability.getProvisionalYear(), totalLiability.getLastYear()));

			totalLiability.setProvisionalYearString(isCMAUpload
					? CommonUtils.checkString(cma2018Liabilities.getTotalOutsideLiabilities())
					: CommonUtils.checkString(CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentLiability())
							+ CommonUtils.checkDouble(coAct2018BalanceSheet.getTotalCurrentAndNonCurrentLiability())));
			totalLiability.setLastYearString(isCMAUpload
					? CommonUtils.checkString(cma2017Liabilities.getTotalOutsideLiabilities())
					: CommonUtils.checkString(CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentLiability())
							+ CommonUtils.checkDouble(coAct2017BalanceSheet.getTotalCurrentAndNonCurrentLiability())));
			totalLiability.setLastToLastYearString(isCMAUpload
					? CommonUtils.checkString(cma2016Liabilities.getTotalOutsideLiabilities())
					: CommonUtils.checkString(CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentLiability())
							+ CommonUtils.checkDouble(coAct2016BalanceSheet.getTotalCurrentAndNonCurrentLiability())));
			totalLiability.setDiffPvsnlAndLastYearString(
					calculateFinancialSummaryString(totalLiability.getProvisionalYear(), totalLiability.getLastYear()));
			responseList.add(totalLiability);

			DDRCMACalculationResponse leverage = new DDRCMACalculationResponse();
			leverage.setKeyId(DDRFinancialSummaryFields.LEVERAGE.getId());
			leverage.setKeyName(DDRFinancialSummaryFields.LEVERAGE.getValue());

			leverage.setProvisionalYear(CommonUtils.checkDouble(netWorthResponse.getProvisionalYear() > 0
					? totalLiability.getProvisionalYear() / netWorthResponse.getProvisionalYear()
					: 0.0));
			leverage.setLastYear(CommonUtils.checkDouble(
					netWorthResponse.getLastYear() > 0 ? totalLiability.getLastYear() / netWorthResponse.getLastYear()
							: 0.0));
			leverage.setLastToLastYear(CommonUtils.checkDouble(netWorthResponse.getLastToLastYear() > 0
					? totalLiability.getLastToLastYear() / netWorthResponse.getLastToLastYear()
					: 0.0));
			leverage.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(leverage.getProvisionalYear(), leverage.getLastYear()));

			leverage.setProvisionalYearString(CommonUtils.checkString(netWorthResponse.getProvisionalYear() > 0
					? totalLiability.getProvisionalYear() / netWorthResponse.getProvisionalYear()
					: 0.0));
			leverage.setLastYearString(CommonUtils.checkString(
					netWorthResponse.getLastYear() > 0 ? totalLiability.getLastYear() / netWorthResponse.getLastYear()
							: 0.0));
			leverage.setLastToLastYearString(CommonUtils.checkString(netWorthResponse.getLastToLastYear() > 0
					? totalLiability.getLastToLastYear() / netWorthResponse.getLastToLastYear()
					: 0.0));
			leverage.setDiffPvsnlAndLastYearString(
					calculateFinancialSummaryString(leverage.getProvisionalYear(), leverage.getLastYear()));
			responseList.add(leverage);

			DDRCMACalculationResponse adjustedLeverage = new DDRCMACalculationResponse();
			adjustedLeverage.setKeyId(DDRFinancialSummaryFields.ADJUSTED_LEVERAGE.getId());
			adjustedLeverage.setKeyName(DDRFinancialSummaryFields.ADJUSTED_LEVERAGE.getValue());

			adjustedLeverage.setProvisionalYear(CommonUtils.checkDouble(adjustedNetWorth.getProvisionalYear() > 0
					? totalLiability.getProvisionalYear() / adjustedNetWorth.getProvisionalYear()
					: 0.0));
			adjustedLeverage.setLastYear(CommonUtils.checkDouble(
					adjustedNetWorth.getLastYear() > 0 ? totalLiability.getLastYear() / adjustedNetWorth.getLastYear()
							: 0.0));
			adjustedLeverage.setLastToLastYear(CommonUtils.checkDouble(adjustedNetWorth.getLastToLastYear() > 0
					? totalLiability.getLastToLastYear() / adjustedNetWorth.getLastToLastYear()
					: 0.0));
			adjustedLeverage.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(adjustedLeverage.getProvisionalYear(), adjustedLeverage.getLastYear()));

			adjustedLeverage.setProvisionalYearString(CommonUtils.checkString(adjustedNetWorth.getProvisionalYear() > 0
					? totalLiability.getProvisionalYear() / adjustedNetWorth.getProvisionalYear()
					: 0.0));
			adjustedLeverage.setLastYearString(CommonUtils.checkString(
					adjustedNetWorth.getLastYear() > 0 ? totalLiability.getLastYear() / adjustedNetWorth.getLastYear()
							: 0.0));
			adjustedLeverage.setLastToLastYearString(CommonUtils.checkString(adjustedNetWorth.getLastToLastYear() > 0
					? totalLiability.getLastToLastYear() / adjustedNetWorth.getLastToLastYear()
					: 0.0));
			adjustedLeverage.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					adjustedLeverage.getProvisionalYear(), adjustedLeverage.getLastYear()));
			responseList.add(adjustedLeverage);

			DDRCMACalculationResponse capitalEmployed = new DDRCMACalculationResponse();
			capitalEmployed.setKeyId(DDRFinancialSummaryFields.CAPITAL_EMPLOYED.getId());
			capitalEmployed.setKeyName(DDRFinancialSummaryFields.CAPITAL_EMPLOYED.getValue());

			if (isCMAUpload) {
				capitalEmployed.setProvisionalYear(CommonUtils.checkDouble(cma2018AssetDetails.getTotalAssets())
						- CommonUtils.checkDouble(cma2018Liabilities.getTotalCurrentLiabilities()));
				capitalEmployed.setLastYear(CommonUtils.checkDouble(cma2017AssetDetails.getTotalAssets())
						- CommonUtils.checkDouble(cma2017Liabilities.getTotalCurrentLiabilities()));
				capitalEmployed.setLastToLastYear(CommonUtils.checkDouble(cma2016AssetDetails.getTotalAssets())
						- CommonUtils.checkDouble(cma2016Liabilities.getTotalCurrentLiabilities()));

				capitalEmployed.setProvisionalYearString(
						CommonUtils.checkString(CommonUtils.checkDouble(cma2018AssetDetails.getTotalAssets())
								- CommonUtils.checkDouble(cma2018Liabilities.getTotalCurrentLiabilities())));
				capitalEmployed.setLastYearString(
						CommonUtils.checkString(CommonUtils.checkDouble(cma2017AssetDetails.getTotalAssets())
								- CommonUtils.checkDouble(cma2017Liabilities.getTotalCurrentLiabilities())));
				capitalEmployed.setLastToLastYearString(
						CommonUtils.checkString(CommonUtils.checkDouble(cma2016AssetDetails.getTotalAssets())
								- CommonUtils.checkDouble(cma2016Liabilities.getTotalCurrentLiabilities())));
			} else {
				capitalEmployed
						.setProvisionalYear(CommonUtils.checkDouble(coAct2018BalanceSheet.getOrdinaryShareCapital())
								+ CommonUtils.checkDouble(coAct2018BalanceSheet.getPreferenceShareCapital())
								+ CommonUtils.checkDouble(coAct2018BalanceSheet.getShareApplicationPendingAllotment()));
				capitalEmployed.setLastYear(CommonUtils.checkDouble(coAct2017BalanceSheet.getOrdinaryShareCapital())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getPreferenceShareCapital())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getShareApplicationPendingAllotment()));
				capitalEmployed
						.setLastToLastYear(CommonUtils.checkDouble(coAct2016BalanceSheet.getOrdinaryShareCapital())
								+ CommonUtils.checkDouble(coAct2016BalanceSheet.getPreferenceShareCapital())
								+ CommonUtils.checkDouble(coAct2016BalanceSheet.getShareApplicationPendingAllotment()));

				capitalEmployed.setProvisionalYearString(CommonUtils.checkString(CommonUtils
						.checkDouble(coAct2018BalanceSheet.getOrdinaryShareCapital())
						+ CommonUtils.checkDouble(coAct2018BalanceSheet.getPreferenceShareCapital())
						+ CommonUtils.checkDouble(coAct2018BalanceSheet.getShareApplicationPendingAllotment())));
				capitalEmployed.setLastYearString(CommonUtils.checkString(CommonUtils
						.checkDouble(coAct2017BalanceSheet.getOrdinaryShareCapital())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getPreferenceShareCapital())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getShareApplicationPendingAllotment())));
				capitalEmployed.setLastToLastYearString(CommonUtils.checkString(CommonUtils
						.checkDouble(coAct2016BalanceSheet.getOrdinaryShareCapital())
						+ CommonUtils.checkDouble(coAct2016BalanceSheet.getPreferenceShareCapital())
						+ CommonUtils.checkDouble(coAct2016BalanceSheet.getShareApplicationPendingAllotment())));
			}
			capitalEmployed.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(capitalEmployed.getProvisionalYear(), capitalEmployed.getLastYear()));
			capitalEmployed.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					capitalEmployed.getProvisionalYear(), capitalEmployed.getLastYear()));
			responseList.add(capitalEmployed);

			DDRCMACalculationResponse gearingResp = new DDRCMACalculationResponse();
			gearingResp.setKeyId(DDRFinancialSummaryFields.GEARING.getId());
			gearingResp.setKeyName(DDRFinancialSummaryFields.GEARING.getValue());

			gearingResp.setProvisionalYear(CommonUtils.checkDouble(netWorthResponse.getProvisionalYear() > 0
					? totalDebt.getProvisionalYear() / netWorthResponse.getProvisionalYear()
					: 0.0));
			gearingResp.setLastYear(CommonUtils.checkDouble(
					netWorthResponse.getLastYear() > 0 ? totalDebt.getLastYear() / netWorthResponse.getLastYear()
							: 0.0));
			gearingResp.setLastToLastYear(CommonUtils.checkDouble(netWorthResponse.getLastToLastYear() > 0
					? totalDebt.getLastToLastYear() / netWorthResponse.getLastToLastYear()
					: 0.0));
			gearingResp.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(gearingResp.getProvisionalYear(), gearingResp.getLastYear()));

			gearingResp.setProvisionalYearString(CommonUtils.checkString(netWorthResponse.getProvisionalYear() > 0
					? totalDebt.getProvisionalYear() / netWorthResponse.getProvisionalYear()
					: 0.0));
			gearingResp.setLastYearString(CommonUtils.checkString(
					netWorthResponse.getLastYear() > 0 ? totalDebt.getLastYear() / netWorthResponse.getLastYear()
							: 0.0));
			gearingResp.setLastToLastYearString(CommonUtils.checkString(netWorthResponse.getLastToLastYear() > 0
					? totalDebt.getLastToLastYear() / netWorthResponse.getLastToLastYear()
					: 0.0));
			gearingResp.setDiffPvsnlAndLastYearString(
					calculateFinancialSummaryString(gearingResp.getProvisionalYear(), gearingResp.getLastYear()));
			responseList.add(gearingResp);

			DDRCMACalculationResponse adjustedGearingResp = new DDRCMACalculationResponse();
			adjustedGearingResp.setKeyId(DDRFinancialSummaryFields.ADJUSTED_GEARING.getId());
			adjustedGearingResp.setKeyName(DDRFinancialSummaryFields.ADJUSTED_GEARING.getValue());

			adjustedGearingResp.setProvisionalYear(CommonUtils.checkDouble(adjustedNetWorth.getProvisionalYear() > 0
					? totalDebt.getProvisionalYear() / adjustedNetWorth.getProvisionalYear()
					: 0.0));
			adjustedGearingResp.setLastYear(CommonUtils.checkDouble(
					adjustedNetWorth.getLastYear() > 0 ? totalDebt.getLastYear() / adjustedNetWorth.getLastYear()
							: 0.0));
			adjustedGearingResp.setLastToLastYear(CommonUtils.checkDouble(adjustedNetWorth.getLastToLastYear() > 0
					? totalDebt.getLastToLastYear() / adjustedNetWorth.getLastToLastYear()
					: 0.0));
			adjustedGearingResp.setDiffPvsnlAndLastYear(calculateFinancialSummary(
					adjustedGearingResp.getProvisionalYear(), adjustedGearingResp.getLastYear()));

			adjustedGearingResp
					.setProvisionalYearString(CommonUtils.checkString(adjustedNetWorth.getProvisionalYear() > 0
							? totalDebt.getProvisionalYear() / adjustedNetWorth.getProvisionalYear()
							: 0.0));
			adjustedGearingResp.setLastYearString(CommonUtils.checkString(
					adjustedNetWorth.getLastYear() > 0 ? totalDebt.getLastYear() / adjustedNetWorth.getLastYear()
							: 0.0));
			adjustedGearingResp.setLastToLastYearString(CommonUtils.checkString(adjustedNetWorth.getLastToLastYear() > 0
					? totalDebt.getLastToLastYear() / adjustedNetWorth.getLastToLastYear()
					: 0.0));
			adjustedGearingResp.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					adjustedGearingResp.getProvisionalYear(), adjustedGearingResp.getLastYear()));
			responseList.add(adjustedGearingResp);

			DDRCMACalculationResponse currentRatio = new DDRCMACalculationResponse();
			currentRatio.setKeyId(DDRFinancialSummaryFields.CURRENT_RATIO.getId());
			currentRatio.setKeyName(DDRFinancialSummaryFields.CURRENT_RATIO.getValue());

			if (isCMAUpload) {
				currentRatio.setProvisionalYear(
						CommonUtils.checkDouble(cma2018Liabilities.getTotalCurrentLiabilities()) > 0 ? CommonUtils
								.checkDouble(CommonUtils.checkDouble(cma2018AssetDetails.getTotalCurrentAssets())
										/ cma2018Liabilities.getTotalCurrentLiabilities())
								: 0.0);
				currentRatio.setLastYear(CommonUtils.checkDouble(cma2017Liabilities.getTotalCurrentLiabilities()) > 0
						? CommonUtils.checkDouble(CommonUtils.checkDouble(cma2017AssetDetails.getTotalCurrentAssets())
								/ cma2017Liabilities.getTotalCurrentLiabilities())
						: 0.0);
				currentRatio.setLastToLastYear(
						CommonUtils.checkDouble(cma2016Liabilities.getTotalCurrentLiabilities()) > 0 ? CommonUtils
								.checkDouble(CommonUtils.checkDouble(cma2016AssetDetails.getTotalCurrentAssets())
										/ cma2016Liabilities.getTotalCurrentLiabilities())
								: 0.0);

				currentRatio.setProvisionalYearString(
						CommonUtils.checkDouble(cma2018Liabilities.getTotalCurrentLiabilities()) > 0
								? CommonUtils.checkString(CommonUtils.checkDouble(
										CommonUtils.checkDouble(cma2018AssetDetails.getTotalCurrentAssets())
												/ cma2018Liabilities.getTotalCurrentLiabilities()))
								: "0.00");
				currentRatio.setLastYearString(
						CommonUtils.checkDouble(cma2017Liabilities.getTotalCurrentLiabilities()) > 0 ? CommonUtils
								.checkString(CommonUtils.checkDouble(cma2017AssetDetails.getTotalCurrentAssets())
										/ cma2017Liabilities.getTotalCurrentLiabilities())
								: "0.00");
				currentRatio.setLastToLastYearString(
						CommonUtils.checkDouble(cma2016Liabilities.getTotalCurrentLiabilities()) > 0 ? CommonUtils
								.checkString(CommonUtils.checkDouble(cma2016AssetDetails.getTotalCurrentAssets())
										/ cma2016Liabilities.getTotalCurrentLiabilities())
								: "0.00");
			} else {
				currentRatio.setProvisionalYear(
						CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentLiability()) > 0 ? CommonUtils
								.checkDouble(CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentAssets())
										/ coAct2018BalanceSheet.getOthersCurrentLiability())
								: 0.0);
				currentRatio.setLastYear(
						CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentLiability()) > 0 ? CommonUtils
								.checkDouble(CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentAssets())
										/ coAct2017BalanceSheet.getOthersCurrentLiability())
								: 0.0);
				currentRatio.setLastToLastYear(
						CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentLiability()) > 0 ? CommonUtils
								.checkDouble(CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentAssets())
										/ coAct2016BalanceSheet.getOthersCurrentLiability())
								: 0.0);

				currentRatio.setProvisionalYearString(
						CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentLiability()) > 0 ? CommonUtils
								.checkString(CommonUtils.checkDouble(coAct2018BalanceSheet.getOthersCurrentAssets())
										/ coAct2018BalanceSheet.getOthersCurrentLiability())
								: "0.00");
				currentRatio.setLastYearString(
						CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentLiability()) > 0 ? CommonUtils
								.checkString(CommonUtils.checkDouble(coAct2017BalanceSheet.getOthersCurrentAssets())
										/ coAct2017BalanceSheet.getOthersCurrentLiability())
								: "0.00");
				currentRatio.setLastToLastYearString(
						CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentLiability()) > 0 ? CommonUtils
								.checkString(CommonUtils.checkDouble(coAct2016BalanceSheet.getOthersCurrentAssets())
										/ coAct2016BalanceSheet.getOthersCurrentLiability())
								: "0.00");
			}
			currentRatio.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(currentRatio.getProvisionalYear(), currentRatio.getLastYear()));
			currentRatio.setDiffPvsnlAndLastYearString(
					calculateFinancialSummaryString(currentRatio.getProvisionalYear(), currentRatio.getLastYear()));
			responseList.add(currentRatio);

			DDRCMACalculationResponse inventoryTurnOver = new DDRCMACalculationResponse();
			inventoryTurnOver.setKeyId(DDRFinancialSummaryFields.INVENTORY_TURNOVER.getId());
			inventoryTurnOver.setKeyName(DDRFinancialSummaryFields.INVENTORY_TURNOVER.getValue());

			if (isCMAUpload) {
				double proPriviousCal = CommonUtils.checkDouble(cma2018AssetDetails.getInventory())
						+ CommonUtils.checkDouble(cma2017AssetDetails.getInventory());
				double provisionalYear = proPriviousCal > 0 ? totalSalesResponse.getProvisionalYear() / proPriviousCal
						: 0.0;
				inventoryTurnOver.setProvisionalYear(CommonUtils.checkDouble(provisionalYear / 2));
				inventoryTurnOver.setProvisionalYearString(CommonUtils.checkString(provisionalYear / 2));

				double lastPriviousCal = CommonUtils.checkDouble(cma2017AssetDetails.getInventory())
						+ CommonUtils.checkDouble(cma2016AssetDetails.getInventory());
				double lastYear = lastPriviousCal > 0 ? totalSalesResponse.getLastYear() / lastPriviousCal : 0.0;
				inventoryTurnOver.setLastYear(CommonUtils.checkDouble(lastYear / 2));
				inventoryTurnOver.setLastYearString(CommonUtils.checkString(lastYear / 2));

				double lastToLastPriviousCal = CommonUtils.checkDouble(cma2016AssetDetails.getInventory())
						+ CommonUtils.checkDouble(cma2015AssetDetails.getInventory());
				double lastToLastYear = lastToLastPriviousCal > 0
						? totalSalesResponse.getLastToLastYear() / lastToLastPriviousCal
						: 0.0;
				inventoryTurnOver.setLastToLastYear(CommonUtils.checkDouble(lastToLastYear / 2));
				inventoryTurnOver.setLastToLastYearString(CommonUtils.checkString(lastToLastYear / 2));
			} else {
				double proPriviousCal = CommonUtils.checkDouble(coAct2018BalanceSheet.getInventory())
						+ CommonUtils.checkDouble(coAct2017BalanceSheet.getInventory());
				double provisionalYear = proPriviousCal > 0 ? totalSalesResponse.getProvisionalYear() / proPriviousCal
						: 0.0;
				inventoryTurnOver.setProvisionalYear(CommonUtils.checkDouble(provisionalYear / 2));
				inventoryTurnOver.setProvisionalYearString(CommonUtils.checkString(provisionalYear / 2));

				double lastPriviousCal = CommonUtils.checkDouble(coAct2017BalanceSheet.getInventory())
						+ CommonUtils.checkDouble(coAct2016BalanceSheet.getInventory());
				double lastYear = lastPriviousCal > 0 ? totalSalesResponse.getLastYear() / lastPriviousCal : 0.0;
				inventoryTurnOver.setLastYear(CommonUtils.checkDouble(lastYear / 2));
				inventoryTurnOver.setLastYearString(CommonUtils.checkString(lastYear / 2));

				double lastToLastPriviousCal = CommonUtils.checkDouble(coAct2016BalanceSheet.getInventory())
						+ CommonUtils.checkDouble(coAct2015BalanceSheet.getInventory());
				double lastToLastYear = lastToLastPriviousCal > 0
						? totalSalesResponse.getLastToLastYear() / lastToLastPriviousCal
						: 0.0;
				inventoryTurnOver.setLastToLastYear(CommonUtils.checkDouble(lastToLastYear / 2));
				inventoryTurnOver.setLastToLastYearString(CommonUtils.checkString(lastToLastYear / 2));
			}
			inventoryTurnOver.setDiffPvsnlAndLastYear(
					calculateFinancialSummary(inventoryTurnOver.getProvisionalYear(), inventoryTurnOver.getLastYear()));
			inventoryTurnOver.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					inventoryTurnOver.getProvisionalYear(), inventoryTurnOver.getLastYear()));
			responseList.add(inventoryTurnOver);

			DDRCMACalculationResponse workingCapitalCycle = new DDRCMACalculationResponse();
			workingCapitalCycle.setKeyId(DDRFinancialSummaryFields.WORKING_CAPITAL_CYCLE.getId());
			workingCapitalCycle.setKeyName(DDRFinancialSummaryFields.WORKING_CAPITAL_CYCLE.getValue());

			double workingCapital2018 = totalCurrentAsset.getProvisionalYear()
					- totalCurrentLiability.getProvisionalYear();
			double workingCapital2017 = totalCurrentAsset.getLastYear() - totalCurrentLiability.getLastYear();
			double workingCapital2016 = totalCurrentAsset.getLastToLastYear()
					- totalCurrentLiability.getLastToLastYear();
			double totalCurrentAsset2015 = isCMAUpload
					? CommonUtils.checkDouble(cma2015AssetDetails.getTotalCurrentAssets())
					: CommonUtils.checkDouble(coAct2015BalanceSheet.getOthersCurrentAssets());
			double totalCurrentLiability2015 = isCMAUpload
					? CommonUtils.checkDouble(cma2015Liabilities.getTotalCurrentLiabilities())
					: CommonUtils.checkDouble(coAct2015BalanceSheet.getOthersCurrentLiability());
			double workingCapital2015 = totalCurrentAsset2015 - totalCurrentLiability2015;

			double avgWorkingCapital2018 = (workingCapital2018 + workingCapital2017) / 2;
			double avgWorkingCapital2017 = (workingCapital2017 + workingCapital2016) / 2;
			double avgWorkingCapital2016 = (workingCapital2016 + workingCapital2015) / 2;

			workingCapitalCycle.setProvisionalYear(totalSalesResponse.getProvisionalYear() > 0
					? CommonUtils.checkDouble((avgWorkingCapital2018 / totalSalesResponse.getProvisionalYear()) * 356)
					: 0.0);
			workingCapitalCycle.setLastYear(totalSalesResponse.getLastYear() > 0
					? CommonUtils.checkDouble((avgWorkingCapital2017 / totalSalesResponse.getLastYear()) * 356)
					: 0.0);
			workingCapitalCycle.setLastToLastYear(totalSalesResponse.getLastToLastYear() > 0
					? CommonUtils.checkDouble((avgWorkingCapital2016 / totalSalesResponse.getLastToLastYear()) * 356)
					: 0.0);
			workingCapitalCycle.setDiffPvsnlAndLastYear(calculateFinancialSummary(
					workingCapitalCycle.getProvisionalYear(), workingCapitalCycle.getLastYear()));

			workingCapitalCycle.setProvisionalYearString(totalSalesResponse.getProvisionalYear() > 0
					? CommonUtils.checkString(CommonUtils
							.checkDouble((avgWorkingCapital2018 / totalSalesResponse.getProvisionalYear()) * 356))
					: "0.00");
			workingCapitalCycle.setLastYearString(totalSalesResponse.getLastYear() > 0
					? CommonUtils.checkString(
							CommonUtils.checkDouble((avgWorkingCapital2017 / totalSalesResponse.getLastYear()) * 356))
					: "0.00");
			workingCapitalCycle.setLastToLastYearString(totalSalesResponse.getLastToLastYear() > 0
					? CommonUtils.checkString(CommonUtils
							.checkDouble((avgWorkingCapital2016 / totalSalesResponse.getLastToLastYear()) * 356))
					: "0.00");
			workingCapitalCycle.setDiffPvsnlAndLastYearString(calculateFinancialSummaryString(
					workingCapitalCycle.getProvisionalYear(), workingCapitalCycle.getLastYear()));
			DDRCMACalculationResponse.printFields(workingCapitalCycle);
			responseList.add(workingCapitalCycle);
		} catch (Exception e) {
			logger.error(
					"DDR ===================> Throw Exception While Get CO ACt and CMA Details -----application----->"
							+ applicationId);
			e.printStackTrace();
		}
		return responseList;

	}

	private double calculateFinancialSummary(Double provisinalYear, Double lastYear) {
		try {
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			if (!CommonUtils.isObjectNullOrEmpty(provisinalYear) && !CommonUtils.isObjectNullOrEmpty(lastYear)) {
				if ((provisinalYear > 0 && lastYear > 0) || (provisinalYear > 0 || lastYear > 0)) {
					return Double.valueOf(decimalFormat.format(((provisinalYear - lastYear) / lastYear) * 100));
				}
			}
			return 0.0;
		} catch (Exception e) {
			logger.info("DDR====================> Throw Excecption while calculateFinancialSummary");
			e.printStackTrace();
		}
		return 0.00;
	}

	private String calculateFinancialSummaryString(Double provisinalYear, Double lastYear) {
		try {
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			if (!CommonUtils.isObjectNullOrEmpty(provisinalYear) && !CommonUtils.isObjectNullOrEmpty(lastYear)) {
				if ((provisinalYear > 0 && lastYear > 0) || (provisinalYear > 0 || lastYear > 0)) {
					return decimalFormat.format(((provisinalYear - lastYear) / lastYear) * 100);
				}
			}
			return "0.00";
		} catch (Exception e) {
			logger.info("DDR====================> Throw Excecption while calculateFinancialSummaryString");
			e.printStackTrace();
		}
		return "0.00";
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getFinancialSummaryFieldsList() {
		List<JSONObject> responseList = new ArrayList<>();
		for (DDRFinancialSummaryFields dDRFinancialSummary : DDRFinancialSummaryFields.values()) {
			JSONObject obj = new JSONObject();
			obj.put("id", dDRFinancialSummary.getId());
			obj.put("value", dDRFinancialSummary.getValue());
			responseList.add(obj);
		}
		return responseList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getFinancialSummaryToBeFieldsList() {
		List<JSONObject> responseList = new ArrayList<>();
		for (DDRFinancialSummaryToBeFields dDRFinancialSummary : DDRFinancialSummaryToBeFields.values()) {
			JSONObject obj = new JSONObject();
			obj.put("id", dDRFinancialSummary.getId());
			obj.put("value", dDRFinancialSummary.getValue());
			responseList.add(obj);
		}
		return responseList;
	}

	@Override
	public Long saveDocumentFLag(DocumentUploadFlagRequest documentUploadFlagRequest) throws Exception {
		// DDRFormDetailsRequest
		try {
			DDRFormDetails dDRFormDetails = ddrFormDetailsRepository
					.getByAppIdAndIsActive(documentUploadFlagRequest.getApplicationId());
			if (CommonUtils.isObjectNullOrEmpty(dDRFormDetails)) {
				dDRFormDetails = new DDRFormDetails();
				dDRFormDetails.setApplicationId(documentUploadFlagRequest.getApplicationId());
				dDRFormDetails.setUserId(documentUploadFlagRequest.getUserId());
				dDRFormDetails.setCreatedBy(documentUploadFlagRequest.getUserId());
				dDRFormDetails.setCreatedDate(new Date());
				dDRFormDetails.setModifyBy(documentUploadFlagRequest.getUserId());
				dDRFormDetails.setModifyDate(new Date());
				dDRFormDetails.setIsActive(true);
			}

			final int switchCase = documentUploadFlagRequest.getDocumentMappingId().intValue();
			switch (switchCase) {
			// Working Capital

			case 501:// WC
			case 502:// TL
			case 503:// USL
			case 504:// WCTL
				dDRFormDetails.setFieldAuditReport("Yes");
				break;

			case 9:// WC
			case 36:// TL
			case 276:// USL
			case 375:// WCTL
				dDRFormDetails.setAuditedFinancialsForLast3years("Yes");
				break;

			case 280:// WC
			case 13:// TL
			case 40:// USL
			case 379:// WCTL
				dDRFormDetails.setProvisionalFinancialsForCurrentYear("Yes");
				break;

			case 11:// WC
			case 38:// TL
			case 278:// USL
			case 377:// WCTL
				dDRFormDetails.setItrForLast3years("Yes");
				break;

			case 10:// WC
			case 37:// TL
			case 277:// USL
			case 376:// WCTL
				dDRFormDetails.setSanctionLetter("Yes");
				break;

			case 505:// WC
			case 506:// TL
			case 507:// USL
			case 508:// WCTL
				dDRFormDetails.setBankStatementOfLast12months("Yes");
				break;

			case 308:// WC
			case 309:// TL
			case 310:// USL
			case 399:// WCTL
				dDRFormDetails.setDebtorsList("Yes");
				break;

			case 509:// WC
			case 510:// TL
			case 511:// USL
			case 512:// WCTL
				dDRFormDetails.setFinancialFigures("Yes");
				break;

			case 18:// WC
			case 45:// TL
			case 311:// USL
			case 384:// WCTL
				dDRFormDetails.setMoaOfTheCompany("Yes");
				break;

			case 3:// WC
			case 30:// TL
			case 283:// USL
			case 369:// WCTL
				dDRFormDetails.setPanCardOfTheCompany("Yes");
				break;

			case 305:// WC
			case 306:// TL
			case 307:// USL
			case 398:// WCTL
				dDRFormDetails.setResolutionAndForm32forAdditionOfDirector("Yes");
				break;

			case 513:// WC
			case 514:// TL
			case 515:// USL
			case 516:// WCTL
				dDRFormDetails.setCentralSalesTaxRegistrationOfCompany("Yes");
				break;

			case 517:// WC
			case 518:// TL
			case 519:// USL
			case 520:// WCTL
				dDRFormDetails.setCentralExciseRegistrationOfCompany("Yes");
				break;

			case 521:// WC
			case 522:// TL
			case 523:// USL
			case 524:// WCTL
				dDRFormDetails.setVatRegistrationOfCompany("Yes");
				break;

			case 315:// WC
			case 316:// TL
			case 317:// USL
			case 401:// WCTL
				dDRFormDetails.setLetterOfIntentFromFundProviders("Yes");
				break;

			case 14:// WC
			case 41:// TL
			case 284:// USL
			case 397:// WCTL
				dDRFormDetails.setPanCardAndResidenceAddProofOfDirectors("Yes");
				break;

			case 12:// WC
			case 39:// TL
			case 279:// USL
			case 378:// WCTL
				dDRFormDetails.setCaCertifiedNetworthStatement("Yes");
				break;

			case 297:// WC
			case 298:// TL
			case 299:// USL
			case 396:// WCTL
				dDRFormDetails.setIrrOfAllDirectorsForLast2years("Yes");
				break;

			case 525:// WC
			case 526:// TL
			case 527:// USL
			case 528:// WCTL
				dDRFormDetails.setListOfDirectors("Yes");
				break;

			case 15:// WC
			case 42:// TL
			case 285:// USL
			case 381:// WCTL
				dDRFormDetails.setListOfShareholdersAndShareHoldingPatter("Yes");
				break;

			case 535:// WC
			case 536:// TL
			case 537:// USL
			case 538:// WCTL
				dDRFormDetails.setProfilePicCompany("Yes");
				break;

			case 539:// WC
			case 540:// TL
			case 541:// USL
			case 542:// WCTL
				dDRFormDetails.setSiteOrPromotorsPhotos("Yes");
				break;

			default:
				break;
			}
			dDRFormDetails.setModifyBy(documentUploadFlagRequest.getUserId());
			dDRFormDetails.setModifyDate(new Date());
			ddrFormDetailsRepository.save(dDRFormDetails);
			return 1L;
		} catch (Exception e) {
			logger.info("DDR==============> Throw Exception while save document flag");
			e.printStackTrace();
		}

		return 0L;
	}

	@Override
	public boolean deleteDocument(DDRUploadRequest ddrUploadRequest) {
		try {
			JSONObject json = new JSONObject();
			json.put("id", ddrUploadRequest.getDocId());
			DocumentResponse docResponse = dmsClient.deleteProductDocument(json.toJSONString());
			if (!CommonUtils.isObjectNullOrEmpty(docResponse)
					&& docResponse.getStatus().equals(HttpStatus.OK.value())) {

				if (ddrUploadRequest.getTotalDocs() < 1) {
					DDRFormDetails dDRFormDetails = ddrFormDetailsRepository
							.getByAppIdAndIsActive(ddrUploadRequest.getApplicationId());

					switch (ddrUploadRequest.getModelName()) {
					case "fieldAuditReport":
						dDRFormDetails.setFieldAuditReport("No");
						break;
					case "auditedFinancialsForLast3years":
						dDRFormDetails.setAuditedFinancialsForLast3years("No");
						break;
					case "provisionalFinancialsForCurrentYear":
						dDRFormDetails.setProvisionalFinancialsForCurrentYear("No");
						break;
					case "itrForLast3years":
						dDRFormDetails.setItrForLast3years("No");
						break;
					case "sanctionLetter":
						dDRFormDetails.setSanctionLetter("No");
						break;
					case "bankStatementOfLast12months":
						dDRFormDetails.setBankStatementOfLast12months("No");
						break;
					case "debtorsList":
						dDRFormDetails.setDebtorsList("No");
						break;
					case "financialFigures":
						dDRFormDetails.setFinancialFigures("No");
						break;
					case "moaOfTheCompany":
						dDRFormDetails.setMoaOfTheCompany("No");
						break;
					case "panCardOfTheCompany":
						dDRFormDetails.setPanCardOfTheCompany("No");
						break;
					case "resolutionAndForm32forAdditionOfDirector":
						dDRFormDetails.setResolutionAndForm32forAdditionOfDirector("No");
						break;
					case "centralSalesTaxRegistrationOfCompany":
						dDRFormDetails.setCentralSalesTaxRegistrationOfCompany("No");
						break;
					case "centralExciseRegistrationOfCompany":
						dDRFormDetails.setCentralExciseRegistrationOfCompany("No");
						break;
					case "vatRegistrationOfCompany":
						dDRFormDetails.setVatRegistrationOfCompany("No");
						break;
					case "letterOfIntentFromFundProviders":
						dDRFormDetails.setLetterOfIntentFromFundProviders("No");
						break;
					case "panCardAndResidenceAddProofOfDirectors":
						dDRFormDetails.setPanCardAndResidenceAddProofOfDirectors("No");
						break;
					case "caCertifiedNetworthStatement":
						dDRFormDetails.setCaCertifiedNetworthStatement("No");
						break;
					case "irrOfAllDirectorsForLast2years":
						dDRFormDetails.setIrrOfAllDirectorsForLast2years("No");
						break;
					case "listOfDirectors":
						dDRFormDetails.setListOfDirectors("No");
						break;
					case "listOfShareholdersAndShareHoldingPatter":
						dDRFormDetails.setListOfShareholdersAndShareHoldingPatter("No");
						break;
					case "profilePicCompany":
						dDRFormDetails.setProfilePicCompany("No");
						break;
					case "siteOrPromotorsPhotos":
						dDRFormDetails.setSiteOrPromotorsPhotos("No");
						break;

					default:
						break;
					}
					ddrFormDetailsRepository.save(dDRFormDetails);
				}
			}
			return true;
		} catch (Exception e) {
			logger.info("Error WHile Delete Documents");
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.capitaworld.service.loans.service.fundseeker.corporate.DDRFormService#
	 * isDDRApproved(java.lang.Long, java.lang.Long)
	 */
	@Override
	public Boolean isDDRApproved(Long userId, Long applicationId) throws Exception {
		try {
			LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.getByIdAndUserId(applicationId,
					userId);

			if (loanApplicationMaster.getDdrStatusId() == CommonUtils.ApplicationStatus.APPROVED) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
	}

	public String convertValue(Integer value) {
		return !CommonUtils.isObjectNullOrEmpty(value) ? value.toString() : "0";
	}

	public String convertDouble(Double value) {
		return !CommonUtils.isObjectNullOrEmpty(value) ? decim.format(value) : "0";
	}

	@Override
	public DDRCustomerRequest getCustomerNameById(DDRCustomerRequest customerRequest) {
		try {
			logger.info("GET CUSTOMER NAME BY CUSTOMER ID =====>  "+ customerRequest.getCustomerId() + " ---->bobUrl==========>"+ bobUrl);
			RestTemplate restTemplate = new RestTemplate();
			String getResult = restTemplate.exchange(bobUrl, HttpMethod.POST, getHttpHeader(customerRequest.getCustomerId()), String.class).getBody();
			logger.info("Customer Name RESPOMSE==========>{}",getResult);
			if(!CommonUtils.isObjectNullOrEmpty(getResult)) {
				String[] split = getResult.split("\\|");
				if(split.length > 1) {
					String status = split[0];
					if("SUCCESS".equals(status)) {
						customerRequest.setCustomerName(split[1]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerRequest;
	}

	private HttpEntity<String> getHttpHeader(String customerId) {
		HttpHeaders headers = new HttpHeaders();
		logger.info("FOUND HAEDER KEY --------------->" + headerAuthKey + " and Header NAme ----------->" + headerName);
		String requestDataEnc = Base64.getEncoder().encodeToString(headerAuthKey.getBytes());
		headers.set(headerName, requestDataEnc);
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new HttpEntity<String>("NAMEINQ~" + customerId, headers);
	}
	
	@Override
	public DDRFormDetails getDDRDetailByApplicationId(Long applicationId) {
		return ddrFormDetailsRepository.getByAppIdAndIsActive(applicationId);
	}

}
