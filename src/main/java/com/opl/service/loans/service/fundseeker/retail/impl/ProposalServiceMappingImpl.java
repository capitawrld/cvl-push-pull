
package com.opl.service.loans.service.fundseeker.retail.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTimeComparator;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.opl.mudra.api.cibil.model.CibilRequest;
import com.opl.mudra.api.cibil.model.CibilScoreLogRequest;
import com.opl.mudra.api.connect.ConnectRequest;
import com.opl.mudra.api.connect.ConnectResponse;
import com.opl.mudra.api.connect.exception.ConnectException;
import com.opl.mudra.api.dms.exception.DocumentException;
import com.opl.mudra.api.dms.model.DocumentRequest;
import com.opl.mudra.api.dms.model.DocumentResponse;
import com.opl.mudra.api.dms.model.StorageDetailsResponse;
import com.opl.mudra.api.dms.utils.DocumentAlias;
import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.CorporateProposalDetails;
import com.opl.mudra.api.loans.model.FundProviderProposalDetails;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.model.ProposalDetailsAdminRequest;
import com.opl.mudra.api.loans.model.ProposalResponse;
import com.opl.mudra.api.loans.model.RetailProposalDetails;
import com.opl.mudra.api.loans.model.common.ProposalSearchResponse;
import com.opl.mudra.api.loans.model.common.ReportRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.CommonUtils.BusinessType;
import com.opl.mudra.api.loans.utils.CommonUtils.LoanType;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.mudra.api.matchengine.exception.MatchException;
import com.opl.mudra.api.matchengine.model.ConnectionResponse;
import com.opl.mudra.api.matchengine.model.DisbursementDetailsModel;
import com.opl.mudra.api.matchengine.model.DisbursementRequestModel;
import com.opl.mudra.api.matchengine.model.ProposalCountResponse;
import com.opl.mudra.api.matchengine.model.ProposalMappingRequest;
import com.opl.mudra.api.matchengine.model.ProposalMappingResponse;
import com.opl.mudra.api.matchengine.utils.MatchConstant;
import com.opl.mudra.api.matchengine.utils.MatchConstant.ProposalStatus;
import com.opl.mudra.api.notification.model.SchedulerDataMultipleBankRequest;
import com.opl.mudra.api.oneform.enums.Currency;
import com.opl.mudra.api.oneform.enums.Denomination;
import com.opl.mudra.api.oneform.enums.FundproviderType;
import com.opl.mudra.api.oneform.enums.WcRenewalType;
import com.opl.mudra.api.oneform.model.MasterResponse;
import com.opl.mudra.api.oneform.model.OneFormResponse;
import com.opl.mudra.api.oneform.model.SectorIndustryModel;
import com.opl.mudra.api.user.model.BranchBasicDetailsRequest;
import com.opl.mudra.api.user.model.CheckerDetailRequest;
import com.opl.mudra.api.user.model.FundProviderDetailsRequest;
import com.opl.mudra.api.user.model.LocationMasterResponse;
import com.opl.mudra.api.user.model.UserResponse;
import com.opl.mudra.api.user.model.UsersRequest;
import com.opl.mudra.client.cibil.CIBILClient;
import com.opl.mudra.client.connect.ConnectClient;
import com.opl.mudra.client.dms.DMSClient;
import com.opl.mudra.client.matchengine.MatchEngineClient;
import com.opl.mudra.client.matchengine.ProposalDetailsClient;
import com.opl.mudra.client.oneform.OneFormClient;
import com.opl.mudra.client.users.UsersClient;
import com.opl.service.loans.domain.fundprovider.ProductMaster;
import com.opl.service.loans.domain.fundprovider.ProposalDetails;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.IneligibleProposalDetails;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;
import com.opl.service.loans.domain.fundseeker.corporate.DirectorBackgroundDetail;
import com.opl.service.loans.domain.fundseeker.retail.RetailApplicantDetail;
import com.opl.service.loans.repository.OfflineProcessedAppRepository;
import com.opl.service.loans.repository.common.LoanRepository;
import com.opl.service.loans.repository.fundprovider.ProductMasterRepository;
import com.opl.service.loans.repository.fundprovider.ProposalDetailsRepository;
import com.opl.service.loans.repository.fundseeker.IneligibleProposalDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.ApplicationProposalMappingRepository;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateApplicantDetailRepository;
import com.opl.service.loans.repository.fundseeker.corporate.DirectorBackgroundDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.IndustrySectorRepository;
import com.opl.service.loans.repository.fundseeker.corporate.LoanApplicationRepository;
import com.opl.service.loans.repository.fundseeker.retail.RetailApplicantDetailRepository;
import com.opl.service.loans.repository.sanction.LoanDisbursementRepository;
import com.opl.service.loans.service.ProposalService;
import com.opl.service.loans.service.common.LogService;
import com.opl.service.loans.service.fundseeker.corporate.LoanApplicationService;
import com.opl.service.loans.utils.CommonDocumentUtils;
import com.opl.service.loans.utils.CommonUtility;

@Service
@Transactional
public class ProposalServiceMappingImpl implements ProposalService {

	@Autowired
	private OneFormClient oneFormClient;

	@Autowired
	private DMSClient dmsClient;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private ApplicationProposalMappingRepository applicationProposalMappingRepository;

	@Autowired
	private CorporateApplicantDetailRepository corporateApplicantDetailRepository;

	@Autowired
	private RetailApplicantDetailRepository retailApplicantDetailRepository;

	@Autowired
	private ProductMasterRepository productMasterRepository;

	@Autowired
	private IndustrySectorRepository industrySectorRepository;

	@Autowired
	private ProposalDetailsClient proposalDetailsClient;

	@Autowired
	private CIBILClient cibilClient;

	@Autowired
	private UsersClient usersClient;

	/*@Autowired
	private NotificationService notificationService;*/

	@Autowired
	private LoanApplicationService loanApplicationService;

	@Autowired
	private MatchEngineClient matchEngineClient;

	@Autowired
	private LogService logService;

	/*@Autowired
	private CorporateDirectorIncomeService corporateDirectorIncomeService;*/

	@Autowired
	private DirectorBackgroundDetailsRepository directorBackgroundDetailsRepository;

	@Autowired
	private ProposalDetailsRepository proposalDetailRepository;

	@Autowired
	private LoanDisbursementRepository loanDisbursementRepository;

	@Autowired
	private ConnectClient connectClient;

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private IneligibleProposalDetailsRepository ineligibleProposalDetailsRepository;

//	@Value("${cw.maxdays.recalculation}")
//	private String mxaDays;
//
//	@Value("${cw.daysdiff.recalculation}")
//	private String daysDiff;
//
//	@Value("${cw.maxdays.recalculation.offline}")
//	private String maxDaysForOffline;
//
//	@Value("${cw.interval.days.recalculation.offline}")
//	private String daysIntervalForOffline;
//
//	@Value("${cw.interval.start.recalculation.offline}")
//	private String startIntervalForOffline;

//	@Value("${cw.maxdays.recalculation.retail}")
//	private String maxDaysRetail;
//	
//	@Value("${cw.maxdays.recalculation.retail.hl}")
//	private String maxDaysRetailHL;
//	
//	@Value("${cw.maxdays.recalculation.retail.al}")
//	private String maxDaysRetailAL;
//
//	@Value("${cw.daysdiff.recalculation.retail}")
//	private String daysDiffRetail;
//
//	@Value("${cw.daysdiff.recalculation.retail.hl}")
//	private String daysDiffRetailHL;
//	
//	@Value("${cw.daysdiff.recalculation.retail.al}")
//	private String daysDiffRetailAL;
//	
//	@Value("${cw.maxdays.recalculation.offline.retail}")
//	private String maxDaysForOfflineRetail;
//
//	@Value("${cw.maxdays.recalculation.offline.retail.hl}")
//	private String maxDaysForOfflineRetailHL;
//	
//	@Value("${cw.maxdays.recalculation.offline.retail.al}")
//	private String maxDaysForOfflineRetailAL;
//	
//	@Value("${cw.interval.days.recalculation.offline.retail}")
//	private String daysIntervalForOfflineRetail;
//	
//	@Value("${cw.interval.days.recalculation.offline.retail.hl}")
//	private String daysIntervalForOfflineRetailHL;
//	
//	@Value("${cw.interval.days.recalculation.offline.retail.al}")
//	private String daysIntervalForOfflineRetailAL;
//	
//	@Value("${cw.interval.start.recalculation.offline.retail}")
//	private String startIntervalForOfflineRetail;
//
//	@Value("${cw.interval.start.recalculation.offline.retail.hl}")
//	private String startIntervalForOfflineRetailHL;
//	
//	@Value("${cw.interval.start.recalculation.offline.retail.al}")
//	private String startIntervalForOfflineRetailAL;

	DecimalFormat df = new DecimalFormat("#");

	private static final Logger logger = LoggerFactory.getLogger(ProposalServiceMappingImpl.class.getName());

	private static final String FOUND_BRANCH_ID_MSG = "Found Branch Id --> ";
	private static final String CURRENT_USER_ID_MSG = "Current user id --> ";
	private static final String ROLE_ID_MSG = "-- Role Id -->";
	private static final String BRANCH_ID_CAN_NOT_BE_FOUND_MSG = "Branch Id Can not be found";
	private static final String THROW_EXCEPTION_WHILE_GET_BRANCH_ID_FROM_USER_ID_MSG = "Throw Exception While Get Branch Id from UserId : ";
	private static final String YOU_DO_NOT_HAVE_RIGHTS_TO_TAKE_ACTION_FOR_THIS_PROPOSAL_IS_ALREADY_ASSIGNED_TO_ANOTHER_CHECKER_MSG = "The said proposal is already assigned to other Checker, hence you can not take any action on the same.";
	private static final String YOU_DO_NOT_HAVE_RIGHTS_TO_TAKE_ACTION_FOR_THIS_PROPOSAL_ASSIGN_PROPOAL_TO_UPPER_LEVEL_CHECKER_MSG = "You do not have rights to take action for this proposal. Kindly assign the proposal to your upper level checker.";
	/*private static final String USER_URL = "userURL";*/

	private String getMainDirectorName(Long appId) {
		DirectorBackgroundDetail dirBackDetails = directorBackgroundDetailsRepository
				.getMainDirectorByApplicationId(appId);
		if (!CommonUtils.isObjectNullOrEmpty(dirBackDetails)) {
			return dirBackDetails.getDirectorsName();
		}
		return "NA";

	}

	@Override
	public List fundproviderProposalByProposalId(ProposalMappingRequest request) {

		List proposalDetailsList = new ArrayList();

		try {

			try {
				// set branch id to proposal request
				UsersRequest usersRequest = new UsersRequest();
				usersRequest.setId(request.getUserId());
				logger.info(CURRENT_USER_ID_MSG + request.getUserId());
				UserResponse userResponse = usersClient.getBranchDetailsBYUserId(usersRequest);
				BranchBasicDetailsRequest basicDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) userResponse.getData(), BranchBasicDetailsRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(basicDetailsRequest)) {
					request.setUserRoleId(basicDetailsRequest.getRoleId());
					logger.info(FOUND_BRANCH_ID_MSG + basicDetailsRequest.getId() + ROLE_ID_MSG + basicDetailsRequest.getRoleId());
					if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.BO
							|| basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.FP_CHECKER) {
						logger.info("Current user is Branch officer or FP_CHECKER");
						request.setBranchId(basicDetailsRequest.getId());
					}
					else if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.SMECC) {
						logger.info("Current user is Branch officer or SMECC");
						request.setBranchIds(userResponse.getBranchList());
					}
				} else {
					logger.info(BRANCH_ID_CAN_NOT_BE_FOUND_MSG);
				}
			} catch (Exception e) {
				logger.error(THROW_EXCEPTION_WHILE_GET_BRANCH_ID_FROM_USER_ID_MSG,e);
			}

			// END set branch id to proposal request
			// calling MATCHENGINE for getting proposal list

			ProposalMappingResponse proposalDetailsResponse = proposalDetailsClient.proposalListOfFundProvider(request);



			for (int i = 0; i < proposalDetailsResponse.getDataList().size(); i++) {
				ProposalMappingRequest proposalrequest = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) proposalDetailsResponse.getDataList().get(i),
						ProposalMappingRequest.class);

				Long applicationId = proposalrequest.getApplicationId();
				Long proposalMappingId = proposalrequest.getId();
				ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.findByProposalIdAndIsActive(proposalrequest.getId(), true);
				if(CommonUtils.isObjectNullOrEmpty(applicationProposalMapping)){
					logger.info("Proposal not in application_proposal_mapping table "+proposalMappingId);
					continue;
				}
				//LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.findOne(applicationId);
				Integer bId = applicationProposalMapping.getBusinessTypeId();

				// getting the value of proposal's branch address city state
				// based on proposal applicationID
				// step 1 get proposal details by applicationID and user details
				// step2 get branch details by branch id available in
				// proposalDetails
				// step3 get branch state by state id available in location
				// Master
				// step4 get branch city by city id available in location Master

				UsersRequest usersRequestData = new UsersRequest();
				usersRequestData.setId(request.getUserId());
				BranchBasicDetailsRequest basicDetailsRequest = null;

				// step 1
				logger.info("application Id:" + proposalrequest.getApplicationId());
				if (!CommonUtils.isObjectNullOrEmpty(proposalrequest.getApplicationId())) {
					Object[] loanDeatils = loanApplicationService
							.getApplicationDetailsByProposalId(proposalrequest.getApplicationId(),proposalrequest.getId());
					logger.info("user id based on application Id:{}" , loanDeatils);
					long userId = loanDeatils[0] != null ? (long) loanDeatils[0] : 0;

					try {
//						step 2	get branch details by branch id available in proposalDetails	BRANCH-MASTER
						if (proposalrequest.getBranchId() != null) {
//						getlocation id available in branch then find city state location name based on location id
							UserResponse userResponse = usersClient.getBranchDetailById(proposalrequest.getBranchId());
							basicDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
									(LinkedHashMap<String, Object>) userResponse.getData(),
									BranchBasicDetailsRequest.class);
							logger.info(
									"--------------------------------------------------------------------------------");
							logger.info("Get BranchDetails By ID:" + userResponse.getData());
							logger.info("branch id By proposal:" + basicDetailsRequest.getBranchId());
							if (basicDetailsRequest.getLocationId() != null) {
								logger.info("location id by branchId:" + basicDetailsRequest.getLocationId());
								try {
									LocationMasterResponse locationDetails = usersClient
											.getLocationDetailByLocationId(basicDetailsRequest.getLocationId());
									logger.info("locationName:====>" + locationDetails.getLocationName());
									logger.info("cityName:====>" + locationDetails.getCity().getName());
									basicDetailsRequest.setLocationMasterResponse(locationDetails);
									logger.info("stateName:====>" + locationDetails.getState().getName());
								} catch (Exception e) {
									logger.info("Exception in getting location by id:" + e);
								}
							}
						}
					} catch (Exception e) {
						logger.info("Exception in getting value from location based on branch id:" + e);
					}
				}
				if (CommonUtils.isObjectNullOrEmpty(applicationProposalMapping)) {
					logger.info("loanApplicationMaster null ot empty !!");
					continue;
				}

				if (CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getIsActive())
						|| !applicationProposalMapping.getIsActive()) {
					logger.info("Application Id is InActive while get fundprovider proposals=====>" + applicationId);
					continue;
				}
				if (CommonUtils.UserMainType.CORPORATE == CommonUtils
						.getUserMainType(applicationProposalMapping.getProductId())) {

					CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
							.getByProposalId(proposalMappingId);

					if (corporateApplicantDetail == null)
						continue;

					// for get address city state country
					String address = "";
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCityId())) {
						address += CommonDocumentUtils.getCity(corporateApplicantDetail.getRegisteredCityId(),
								oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredStateId())) {
						address += CommonDocumentUtils.getState(
								corporateApplicantDetail.getRegisteredStateId().longValue(), oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCountryId())) {
						address += CommonDocumentUtils.getCountry(
								corporateApplicantDetail.getRegisteredCountryId().longValue(), oneFormClient);
					} else {
						address += "NA";
					}

					CorporateProposalDetails corporateProposalDetails = new CorporateProposalDetails();
					corporateProposalDetails.setBusinessTypeId(applicationProposalMapping.getBusinessTypeId());
					corporateProposalDetails.setAddress(address);

					// set Branch State and city and name
					try {
						if (basicDetailsRequest.getLocationId() != null) {
							corporateProposalDetails.setBranchLocationName(basicDetailsRequest.getName());
							corporateProposalDetails
									.setBranchCity(basicDetailsRequest.getLocationMasterResponse().getCity().getName());
							corporateProposalDetails.setBranchState(
									basicDetailsRequest.getLocationMasterResponse().getState().getName());
							corporateProposalDetails.setBranchLocationName(basicDetailsRequest.getName());
						}
					} catch (Exception e) {
						logger.info("Branch Id is null:");
					}
					if (CommonUtils.BusinessType.NEW_TO_BUSINESS.getId().equals(bId)) {

						corporateProposalDetails.setName(getMainDirectorName(applicationId));
					} else if (CommonUtils.BusinessType.EXISTING_BUSINESS.getId().equals(bId) || bId == null) {
						if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getOrganisationName()))
							corporateProposalDetails.setName("NA");
						else
							corporateProposalDetails.setName(corporateApplicantDetail.getOrganisationName());
					}

					corporateProposalDetails
							.setFsMainType(CommonUtils.getCorporateLoanType(applicationProposalMapping.getProductId()));
					LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.findOne(applicationId);
					corporateProposalDetails.setWcRenualNew(loanApplicationMaster.getWcRenewalStatus()!= null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue().toString() : "New");
					corporateProposalDetails.setApplicationCode(applicationProposalMapping.getApplicationCode()!= null ?  applicationProposalMapping.getApplicationCode() : "-");

					// for get industry id
					List<Long> listIndustryIds = industrySectorRepository.getIndustryByApplicationId(applicationId);
					if (listIndustryIds.size() > 0) {
						OneFormResponse formResponse = oneFormClient.getIndustryById(listIndustryIds);
						List<Map<String, Object>> loanResponseDatalist = (List<Map<String, Object>>) formResponse
								.getListData();
						String industry = "";
						if (loanResponseDatalist.size() > 0) {
							for (int k = 0; k < loanResponseDatalist.size(); k++) {
								MasterResponse masterResponse;
								masterResponse = MultipleJSONObjectHelper.getObjectFromMap(loanResponseDatalist.get(k),
										MasterResponse.class);
								industry += masterResponse.getValue() + " ,";
							}
							corporateProposalDetails.setIndustry(industry);
						} else {
							corporateProposalDetails.setIndustry("NA");
						}
					} else {
						corporateProposalDetails.setIndustry("NA");
					}

					List<Long> keyVerticalFundingId = new ArrayList<>();
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVericalFunding()))
						keyVerticalFundingId.add(corporateApplicantDetail.getKeyVericalFunding());
					if (!CommonUtils.isListNullOrEmpty(keyVerticalFundingId)) {
						try {
							OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
							List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
									.getListData();
							if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
								MasterResponse masterResponse = MultipleJSONObjectHelper
										.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
								corporateProposalDetails.setKeyVertical(masterResponse.getValue());
							} else {
								corporateProposalDetails.setKeyVertical("NA");
							}
						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}
					}

					// key vertical sector
					List<Long> keyVerticalSectorId = new ArrayList<>();
					// getting sector id from mapping
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSector()))
						keyVerticalSectorId.add(corporateApplicantDetail.getKeyVerticalSector());
					try {
						OneFormResponse formResponse = oneFormClient
								.getIndustrySecByMappingId(corporateApplicantDetail.getKeyVerticalSector());
						SectorIndustryModel sectorIndustryModel = MultipleJSONObjectHelper
								.getObjectFromMap((Map) formResponse.getData(), SectorIndustryModel.class);

						// get key vertical sector value
						OneFormResponse oneFormResponse = oneFormClient
								.getSectorById(Arrays.asList(sectorIndustryModel.getSectorId()));
						List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
								.getListData();
						if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
							MasterResponse masterResponse = MultipleJSONObjectHelper
									.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
							corporateProposalDetails.setSector(masterResponse.getValue());
						} else {
							corporateProposalDetails.setSector("NA");
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					// key vertical Subsector
					try {
						if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSubsector())) {
							OneFormResponse oneFormResponse = oneFormClient
									.getSubSecNameByMappingId(corporateApplicantDetail.getKeyVerticalSubsector());
							corporateProposalDetails.setSubSector((String) oneFormResponse.getData());
						}
					} catch (Exception e) {
						logger.error("error while getting key vertical sub-sector : ",e);
					}

					String amount = "";
					if (CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getLoanAmount()))
						amount += "NA";
					else
						amount += df.format(applicationProposalMapping.getLoanAmount());

					if (CommonUtils.isObjectNullOrEmpty(5))
						amount += " NA";
					else
						amount += " " + Denomination.getById(5).getValue();

					corporateProposalDetails.setAmount(amount);

					// calling DMS for getting fs corporate profile image path

					DocumentRequest documentRequest = new DocumentRequest();
					documentRequest.setApplicationId(applicationId);
					documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
					documentRequest.setProductDocumentMappingId(
							CommonDocumentUtils.getProductDocumentId(applicationProposalMapping.getProductId()));

					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					String imagePath = null;
					if (documentResponse != null && documentResponse.getStatus() == 200) {
						List<Map<String, Object>> list = documentResponse.getDataList();
						if (!CommonUtils.isListNullOrEmpty(list)) {
							StorageDetailsResponse response = null;

							response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
									StorageDetailsResponse.class);

							if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
								imagePath = response.getFilePath();
							else
								imagePath = null;
						}
					}
					corporateProposalDetails.setAssignDate(proposalrequest.getAssignDate());
					corporateProposalDetails.setImagePath(imagePath);
					corporateProposalDetails.setLastStatusActionDate(logService.getDateByLogType(
							proposalrequest.getApplicationId(), proposalrequest.getDateTypeMasterId()));
					corporateProposalDetails.setApplicationId(applicationId);
					corporateProposalDetails.setProposalMappingId(proposalrequest.getId());
					corporateProposalDetails.setFsType(CommonUtils.UserMainType.CORPORATE);
					corporateProposalDetails.setModifiedDate(applicationProposalMapping.getModifiedDate() != null ? applicationProposalMapping.getModifiedDate() : applicationProposalMapping.getCreatedDate());

					corporateProposalDetails.setProposalStatus(proposalrequest.getProposalStatusId());
					if(proposalrequest.getProposalStatusId() == ProposalStatus.HOLD || proposalrequest.getProposalStatusId() == ProposalStatus.DECLINE) {
						corporateProposalDetails.setLastStatusActionDate(proposalrequest.getModifiedDate());
					}

					if (!CommonUtils.isObjectNullOrEmpty(proposalrequest.getModifiedBy())) {
						UsersRequest usersRequest = getUserNameAndEmail(proposalrequest.getModifiedBy());
						if (!CommonUtils.isObjectNullOrEmpty(usersRequest)) {
							corporateProposalDetails.setModifiedBy(usersRequest.getName());
						}
					}

					/*
					 * if(!CommonUtils.isObjectNullOrEmpty(proposalrequest.
					 * getAssignBy())) { UsersRequest usersRequest =
					 * getUserNameAndEmail(proposalrequest.getAssignBy());
					 * if(!CommonUtils.isObjectNullOrEmpty(usersRequest)) {
					 * corporateProposalDetails.setAssignBy(usersRequest.getName
					 * ()); } }
					 */

					// city for fp
					UserResponse usrResponse = usersClient.getFPDetails(usersRequestData);
					if (!CommonUtils.isObjectNullOrEmpty(usrResponse)) {
						try {
							FundProviderDetailsRequest fundProviderDetailsRequest = MultipleJSONObjectHelper
									.getObjectFromMap((LinkedHashMap<String, Object>) usrResponse.getData(),
											FundProviderDetailsRequest.class);
							if (!CommonUtils.isObjectNullOrEmpty(fundProviderDetailsRequest)) {
								if (!CommonUtils.isObjectNullOrEmpty(fundProviderDetailsRequest.getCityId())) {
										corporateProposalDetails.setCity(CommonDocumentUtils.getCity(
												fundProviderDetailsRequest.getCityId().longValue(), oneFormClient));
								}
								if (!CommonUtils.isObjectNullOrEmpty(fundProviderDetailsRequest.getPincode())) {
									corporateProposalDetails.setPincode(fundProviderDetailsRequest.getPincode());
								}
							}

						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}

					}

					if (!CommonUtils.isObjectNullOrEmpty(proposalrequest.getAssignBranchTo())) {
						try {
							UserResponse userResponse = usersClient
									.getBranchNameById(proposalrequest.getAssignBranchTo());
							if (!CommonUtils.isObjectNullOrEmpty(userResponse)) {
								corporateProposalDetails.setAssignbranch((String) userResponse.getData());
							}
						} catch (Exception e) {
							logger.error("Throw Exception while get branch name by branch id--------->" + proposalrequest.getAssignBranchTo() + " :: ",e);
						}
						corporateProposalDetails.setIsAssignedToBranch(true);
					} else {
						corporateProposalDetails.setIsAssignedToBranch(false);
					}

					//checking whether proposal already sanction or not.
					ProposalDetails proposalDetails = proposalDetailRepository.getSanctionProposalByApplicationId(applicationId);
					if (!CommonUtils.isObjectNullOrEmpty(proposalDetails)) {
						if (!proposalDetails.getUserOrgId().toString().equals(request.getUserOrgId().toString())) {
							corporateProposalDetails.setIsSanction(true);
						} else {
							corporateProposalDetails.setIsSanction(false);
						}
					} else {
						corporateProposalDetails.setIsSanction(false);
					}
					//

					proposalDetailsList.add(corporateProposalDetails);
				} else {
					Long fpProductId = request.getFpProductId();

					RetailApplicantDetail retailApplicantDetail = retailApplicantDetailRepository
							.findByApplicationId(applicationId);

					if (retailApplicantDetail == null)
						continue;

					// for get address city state country
					String address = "";
					if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentCityId())) {
						address += CommonDocumentUtils.getCity(retailApplicantDetail.getPermanentCityId(),
								oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentStateId())) {
						address += CommonDocumentUtils.getState(retailApplicantDetail.getPermanentStateId().longValue(),
								oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentCountryId())) {
						address += CommonDocumentUtils
								.getCountry(retailApplicantDetail.getPermanentCountryId().longValue(), oneFormClient);
					} else {
						address += "NA";
					}

					RetailProposalDetails retailProposalDetails = new RetailProposalDetails();
					retailProposalDetails.setAddress(address);

					String name = "";

					if (CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getFirstName()))
						name += "NA";
					else
						name += retailApplicantDetail.getFirstName();

					if (CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getLastName()))
						name += " NA";
					else
						name += " " + retailApplicantDetail.getLastName();

					retailProposalDetails.setName(name);

					// set Branch State and city and name
					try {
						if (basicDetailsRequest.getLocationId() != null) {
							retailProposalDetails.setBranchLocationName(basicDetailsRequest.getName());
							retailProposalDetails
									.setBranchCity(basicDetailsRequest.getLocationMasterResponse().getCity().getName());
							retailProposalDetails.setBranchState(
									basicDetailsRequest.getLocationMasterResponse().getState().getName());
							retailProposalDetails.setBranchLocationName(basicDetailsRequest.getName());
						}
					} catch (Exception e) {
						logger.error("location id is null for this application:" + applicationId + "::" + e);
					}

					// calling DMS for getting fs retail profile image path

					DocumentRequest documentRequest = new DocumentRequest();
					documentRequest.setApplicationId(applicationId);
					documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
					documentRequest.setProductDocumentMappingId(
							CommonDocumentUtils.getProductDocumentId(applicationProposalMapping.getProductId()));
					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					String imagePath = null;
					if (documentResponse != null && documentResponse.getStatus() == 200) {
						List<Map<String, Object>> list = documentResponse.getDataList();
						if (!CommonUtils.isListNullOrEmpty(list)) {
							StorageDetailsResponse response = null;

							response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
									StorageDetailsResponse.class);

							if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
								imagePath = response.getFilePath();
							else
								imagePath = null;
						}
					}

					retailProposalDetails.setImagePath(imagePath);
					retailProposalDetails.setApplicationId(applicationId);
					retailProposalDetails.setProposalMappingId(proposalrequest.getId());
					retailProposalDetails.setFsType(CommonUtils.UserMainType.RETAIL);
					retailProposalDetails.setBusinessTypeId(applicationProposalMapping.getBusinessTypeId());
					retailProposalDetails.setFpProductid(fpProductId);
					retailProposalDetails.setProductId(applicationProposalMapping.getProductId()); 

					retailProposalDetails.setProposalStatus(proposalrequest.getProposalStatusId());
					if(proposalrequest.getProposalStatusId() == ProposalStatus.HOLD || proposalrequest.getProposalStatusId() == ProposalStatus.DECLINE) {
						retailProposalDetails.setLastStatusActionDate(proposalrequest.getModifiedDate());
					}
					// get retail loan amount

					String loanAmount = "";
					if (!CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getLoanAmount())) {
						loanAmount += df.format(applicationProposalMapping.getLoanAmount());
					} else {
						loanAmount += "NA";
					}

					if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getCurrencyId())) {
						loanAmount += " " + Currency.getById(retailApplicantDetail.getCurrencyId());
					} else {
						loanAmount += " NA";
					}

					retailProposalDetails.setAmount(loanAmount);
					try {
						CibilRequest cibilRequest = new CibilRequest();
						cibilRequest.setApplicationId(applicationId);
						cibilRequest.setUserId(request.getUserId());
						cibilRequest.setPan(retailApplicantDetail.getPan());
						CibilScoreLogRequest cibilResponse = cibilClient.getCibilScoreByPanCard(cibilRequest);
						if (!CommonUtils.isObjectNullOrEmpty(cibilResponse.getActualScore())) {
							if(cibilResponse.getActualScore().equals("000-1")) {
								retailProposalDetails.setCibilSCore("-1");
							} else {
								retailProposalDetails.setCibilSCore(cibilResponse.getActualScore());
							}
						}
//						if (!CibilUtils.isObjectNullOrEmpty(cibilResponse)) {
//							String response = (String) cibilResponse.getData();
//							if (!CibilUtils.isObjectNullOrEmpty(response)) {
//								JSONObject jsonObject = new JSONObject(response);
//								JSONObject asset = jsonObject.getJSONObject("Asset");
//								if (!CibilUtils.isObjectNullOrEmpty(asset)) {
//									JSONObject trueLinkCreditReport = asset.getJSONObject("ns4:TrueLinkCreditReport");
//									if (!CibilUtils.isObjectNullOrEmpty(trueLinkCreditReport)) {
//										JSONObject creditScore = trueLinkCreditReport.getJSONObject("ns4:Borrower")
//												.getJSONObject("ns4:CreditScore");
//										if (!CibilUtils.isObjectNullOrEmpty(creditScore)) {
//											String score = creditScore.get("riskScore").toString();
//											logger.info("Pan===>" + cibilRequest.getPan() + " ==> Score===>" + score);
//											retailProposalDetails.setCibilSCore(score);
//										} else {
//											logger.info("no data Found from key ns4:CreditScore");
//										}
//
//									} else {
//										logger.info("no data Found from key ns4:TrueLinkCreditReport");
//									}
//
//								} else {
//									logger.info("no data Found from key ns4:Asset");
//								}
//							} else {
//								logger.info("Cibil Actual data Response Found NULL from Loans for PAN ==>"
//										+ cibilRequest.getPan());
//							}
//						} else {
//							logger.info("CibilResponse Found NULL from Loans for PAN ==>" + cibilRequest.getPan());
//						}
					} catch (Exception e) {
						logger.error("Error while getting CIbilScore of User : ",e);
					}
					proposalDetailsList.add(retailProposalDetails);
				}
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return proposalDetailsList;
	}

	@Override
	public List fundproviderProposal(ProposalMappingRequest request) {

		List proposalDetailsList = new ArrayList();

		try {

			try {
				// set branch id to proposal request
				UsersRequest usersRequest = new UsersRequest();
				usersRequest.setId(request.getUserId());
				logger.info(CURRENT_USER_ID_MSG + request.getUserId());
				UserResponse userResponse = usersClient.getBranchDetailsBYUserId(usersRequest);
				BranchBasicDetailsRequest basicDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) userResponse.getData(), BranchBasicDetailsRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(basicDetailsRequest)) {
					request.setUserRoleId(basicDetailsRequest.getRoleId());
					logger.info(FOUND_BRANCH_ID_MSG + basicDetailsRequest.getId() + ROLE_ID_MSG + basicDetailsRequest.getRoleId());
					if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.BO
							|| basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.FP_CHECKER) {
						logger.info("Current user is Branch officer or FP_CHECKER");
						request.setBranchId(basicDetailsRequest.getId());
					}
					else if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.SMECC) {
						logger.info("Current user is Branch officer or SMECC");
						request.setBranchIds(userResponse.getBranchList());
					}
				} else {
					logger.info(BRANCH_ID_CAN_NOT_BE_FOUND_MSG);
				}
			} catch (Exception e) {
				logger.error(THROW_EXCEPTION_WHILE_GET_BRANCH_ID_FROM_USER_ID_MSG,e);
			}

			// END set branch id to proposal request
			// calling MATCHENGINE for getting proposal list

			ProposalMappingResponse proposalDetailsResponse = proposalDetailsClient.proposalListOfFundProvider(request);



			for (int i = 0; i < proposalDetailsResponse.getDataList().size(); i++) {
				ProposalMappingRequest proposalrequest = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) proposalDetailsResponse.getDataList().get(i),
						ProposalMappingRequest.class);

				Long applicationId = proposalrequest.getApplicationId();
				LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.findOne(applicationId);
				Integer bId = loanApplicationMaster.getBusinessTypeId();

				// getting the value of proposal's branch address city state
				// based on proposal applicationID
				// step 1 get proposal details by applicationID and user details
				// step2 get branch details by branch id available in
				// proposalDetails
				// step3 get branch state by state id available in location
				// Master
				// step4 get branch city by city id available in location Master

				UsersRequest usersRequestData = new UsersRequest();
				usersRequestData.setId(request.getUserId());
				BranchBasicDetailsRequest basicDetailsRequest = null;

				// step 1
				logger.info("application Id:" + proposalrequest.getApplicationId());
				if (!CommonUtils.isObjectNullOrEmpty(proposalrequest.getApplicationId())) {
					Object[] loanDeatils = loanApplicationService
							.getApplicationDetailsById(proposalrequest.getApplicationId());
					logger.info("user id based on application Id:" + Arrays.toString(loanDeatils));
					long userId = loanDeatils[0] != null ? (long) loanDeatils[0] : 0;

					try {
						// step 2 get branch details by branch id available in
						// proposalDetails BRANCH-MASTER
						if (proposalrequest.getBranchId() != null) {
							// getlocation id available in branch then find city
							// state location name based on location id
							UserResponse userResponse = usersClient.getBranchDetailById(proposalrequest.getBranchId());
							basicDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
									(LinkedHashMap<String, Object>) userResponse.getData(),
									BranchBasicDetailsRequest.class);
							logger.info(
									"--------------------------------------------------------------------------------");
							logger.info("Get BranchDetails By ID:" + userResponse.getData());
							logger.info("branch id By proposal:" + basicDetailsRequest.getBranchId());
							if (basicDetailsRequest.getLocationId() != null) {
								logger.info("location id by branchId:" + basicDetailsRequest.getLocationId());
								try {
									LocationMasterResponse locationDetails = usersClient
											.getLocationDetailByLocationId(basicDetailsRequest.getLocationId());
									logger.info("locationName:====>" + locationDetails.getLocationName());
									logger.info("cityName:====>" + locationDetails.getCity().getName());
									basicDetailsRequest.setLocationMasterResponse(locationDetails);
									logger.info("stateName:====>" + locationDetails.getState().getName());
								} catch (Exception e) {
									logger.info("Exception in getting location by id:" + e);
								}
							}
						}
					} catch (Exception e) {
						logger.info("Exception in getting value from location based on branch id:" + e);
					}
				}
				if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster)) {
					logger.info("loanApplicationMaster null ot empty !!");
					continue;
				}

				if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getIsActive())
						|| !loanApplicationMaster.getIsActive()) {
					logger.info("Application Id is InActive while get fundprovider proposals=====>" + applicationId);
					continue;
				}
				if (CommonUtils.UserMainType.CORPORATE == CommonUtils
						.getUserMainType(loanApplicationMaster.getProductId())) {

					CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
							.findOneByApplicationIdId(applicationId);

					if (corporateApplicantDetail == null)
						continue;

					// for get address city state country
					String address = "";
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCityId())) {
						address += CommonDocumentUtils.getCity(corporateApplicantDetail.getRegisteredCityId(),
								oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredStateId())) {
						address += CommonDocumentUtils.getState(
								corporateApplicantDetail.getRegisteredStateId().longValue(), oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCountryId())) {
						address += CommonDocumentUtils.getCountry(
								corporateApplicantDetail.getRegisteredCountryId().longValue(), oneFormClient);
					} else {
						address += "NA";
					}

					CorporateProposalDetails corporateProposalDetails = new CorporateProposalDetails();
					corporateProposalDetails.setBusinessTypeId(loanApplicationMaster.getBusinessTypeId());
					corporateProposalDetails.setAddress(address);

					// set Branch State and city and name
					try {
						if (basicDetailsRequest.getLocationId() != null) {
							corporateProposalDetails.setBranchLocationName(basicDetailsRequest.getName());
							corporateProposalDetails
									.setBranchCity(basicDetailsRequest.getLocationMasterResponse().getCity().getName());
							corporateProposalDetails.setBranchState(
									basicDetailsRequest.getLocationMasterResponse().getState().getName());
							corporateProposalDetails.setBranchLocationName(basicDetailsRequest.getName());
						}
					} catch (Exception e) {
						logger.info("Branch Id is null:");
					}
					if (CommonUtils.BusinessType.NEW_TO_BUSINESS.getId().equals(bId)) {

						corporateProposalDetails.setName(getMainDirectorName(applicationId));
					} else if (CommonUtils.BusinessType.EXISTING_BUSINESS.getId().equals(bId) || bId == null) {
						if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getOrganisationName()))
							corporateProposalDetails.setName("NA");
						else
							corporateProposalDetails.setName(corporateApplicantDetail.getOrganisationName());
					}

					corporateProposalDetails
							.setFsMainType(CommonUtils.getCorporateLoanType(loanApplicationMaster.getProductId()));
					corporateProposalDetails.setWcRenualNew(loanApplicationMaster.getWcRenewalStatus()!= null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue().toString() : "New");
					corporateProposalDetails.setApplicationCode(loanApplicationMaster.getApplicationCode()!= null ?  loanApplicationMaster.getApplicationCode() : "-");

					// for get industry id
					List<Long> listIndustryIds = industrySectorRepository.getIndustryByApplicationId(applicationId);
					if (listIndustryIds.size() > 0) {
						OneFormResponse formResponse = oneFormClient.getIndustryById(listIndustryIds);
						List<Map<String, Object>> loanResponseDatalist = (List<Map<String, Object>>) formResponse
								.getListData();
						String industry = "";
						if (loanResponseDatalist.size() > 0) {
							for (int k = 0; k < loanResponseDatalist.size(); k++) {
								MasterResponse masterResponse = new MasterResponse();
								masterResponse = MultipleJSONObjectHelper.getObjectFromMap(loanResponseDatalist.get(k),
										MasterResponse.class);
								industry += masterResponse.getValue() + " ,";
							}
							corporateProposalDetails.setIndustry(industry);
						} else {
							corporateProposalDetails.setIndustry("NA");
						}
					} else {
						corporateProposalDetails.setIndustry("NA");
					}

					List<Long> keyVerticalFundingId = new ArrayList<>();
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVericalFunding()))
						keyVerticalFundingId.add(corporateApplicantDetail.getKeyVericalFunding());
					if (!CommonUtils.isListNullOrEmpty(keyVerticalFundingId)) {
						try {
							OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
							List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
									.getListData();
							if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
								MasterResponse masterResponse = MultipleJSONObjectHelper
										.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
								corporateProposalDetails.setKeyVertical(masterResponse.getValue());
							} else {
								corporateProposalDetails.setKeyVertical("NA");
							}
						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}
					}

					// key vertical sector
					List<Long> keyVerticalSectorId = new ArrayList<>();
					// getting sector id from mapping
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSector()))
						keyVerticalSectorId.add(corporateApplicantDetail.getKeyVerticalSector());
					try {
						OneFormResponse formResponse = oneFormClient
								.getIndustrySecByMappingId(corporateApplicantDetail.getKeyVerticalSector());
						SectorIndustryModel sectorIndustryModel = MultipleJSONObjectHelper
								.getObjectFromMap((Map) formResponse.getData(), SectorIndustryModel.class);

						// get key vertical sector value
						OneFormResponse oneFormResponse = oneFormClient
								.getSectorById(Arrays.asList(sectorIndustryModel.getSectorId()));
						List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
								.getListData();
						if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
							MasterResponse masterResponse = MultipleJSONObjectHelper
									.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
							corporateProposalDetails.setSector(masterResponse.getValue());
						} else {
							corporateProposalDetails.setSector("NA");
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					// key vertical Subsector
					try {
						if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSubsector())) {
							OneFormResponse oneFormResponse = oneFormClient
									.getSubSecNameByMappingId(corporateApplicantDetail.getKeyVerticalSubsector());
							corporateProposalDetails.setSubSector((String) oneFormResponse.getData());
						}
					} catch (Exception e) {
						logger.error("error while getting key vertical sub-sector : ",e);
					}

					String amount = "";
					if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getAmount()))
						amount += "NA";
					else
						amount += df.format(loanApplicationMaster.getAmount());

					if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getDenominationId()))
						amount += " NA";
					else
						amount += " " + Denomination.getById(loanApplicationMaster.getDenominationId()).getValue();

					corporateProposalDetails.setAmount(amount);

					// calling DMS for getting fs corporate profile image path

					DocumentRequest documentRequest = new DocumentRequest();
					documentRequest.setApplicationId(applicationId);
					documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
					documentRequest.setProductDocumentMappingId(
							CommonDocumentUtils.getProductDocumentId(loanApplicationMaster.getProductId()));

					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					String imagePath = null;
					if (documentResponse != null && documentResponse.getStatus() == 200) {
						List<Map<String, Object>> list = documentResponse.getDataList();
						if (!CommonUtils.isListNullOrEmpty(list)) {
							StorageDetailsResponse response = null;

							response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
									StorageDetailsResponse.class);

							if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
								imagePath = response.getFilePath();
							else
								imagePath = null;
						}
					}
					corporateProposalDetails.setAssignDate(proposalrequest.getAssignDate());
					corporateProposalDetails.setImagePath(imagePath);
					corporateProposalDetails.setLastStatusActionDate(logService.getDateByLogType(
							proposalrequest.getApplicationId(), proposalrequest.getDateTypeMasterId()));
					corporateProposalDetails.setApplicationId(applicationId);
					corporateProposalDetails.setProposalMappingId(proposalrequest.getId());
					corporateProposalDetails.setFsType(CommonUtils.UserMainType.CORPORATE);
					corporateProposalDetails.setModifiedDate(loanApplicationMaster.getModifiedDate());

					corporateProposalDetails.setProposalStatus(proposalrequest.getProposalStatusId());
					if(proposalrequest.getProposalStatusId() == ProposalStatus.HOLD || proposalrequest.getProposalStatusId() == ProposalStatus.DECLINE) {
						corporateProposalDetails.setLastStatusActionDate(proposalrequest.getModifiedDate());
					}

					if (!CommonUtils.isObjectNullOrEmpty(proposalrequest.getModifiedBy())) {
						UsersRequest usersRequest = getUserNameAndEmail(proposalrequest.getModifiedBy());
						if (!CommonUtils.isObjectNullOrEmpty(usersRequest)) {
							corporateProposalDetails.setModifiedBy(usersRequest.getName());
						}
					}

					/*
					 * if(!CommonUtils.isObjectNullOrEmpty(proposalrequest.
					 * getAssignBy())) { UsersRequest usersRequest =
					 * getUserNameAndEmail(proposalrequest.getAssignBy());
					 * if(!CommonUtils.isObjectNullOrEmpty(usersRequest)) {
					 * corporateProposalDetails.setAssignBy(usersRequest.getName
					 * ()); } }
					 */

					// city for fp
					UserResponse usrResponse = usersClient.getFPDetails(usersRequestData);
					if (!CommonUtils.isObjectNullOrEmpty(usrResponse)) {
						try {
							FundProviderDetailsRequest fundProviderDetailsRequest = MultipleJSONObjectHelper
									.getObjectFromMap((LinkedHashMap<String, Object>) usrResponse.getData(),
											FundProviderDetailsRequest.class);
							if (!CommonUtils.isObjectNullOrEmpty(fundProviderDetailsRequest)) {
								if (!CommonUtils.isObjectNullOrEmpty(fundProviderDetailsRequest.getCityId())) {
										corporateProposalDetails.setCity(CommonDocumentUtils.getCity(
												fundProviderDetailsRequest.getCityId().longValue(), oneFormClient));
								}
								if (!CommonUtils.isObjectNullOrEmpty(fundProviderDetailsRequest.getPincode())) {
									corporateProposalDetails.setPincode(fundProviderDetailsRequest.getPincode());
								}
							}

						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}

					}

					if (!CommonUtils.isObjectNullOrEmpty(proposalrequest.getAssignBranchTo())) {
						try {
							UserResponse userResponse = usersClient
									.getBranchNameById(proposalrequest.getAssignBranchTo());
							if (!CommonUtils.isObjectNullOrEmpty(userResponse)) {
								corporateProposalDetails.setAssignbranch((String) userResponse.getData());
							}
						} catch (Exception e) {
							logger.error("Throw Exception while get branch name by branch id--------->" + proposalrequest.getAssignBranchTo() + " :: ",e);
						}
						corporateProposalDetails.setIsAssignedToBranch(true);
					} else {
						corporateProposalDetails.setIsAssignedToBranch(false);
					}
					proposalDetailsList.add(corporateProposalDetails);
				} else {
					Long fpProductId = request.getFpProductId();

					RetailApplicantDetail retailApplicantDetail = retailApplicantDetailRepository
							.findByApplicationId(applicationId);

					if (retailApplicantDetail == null)
						continue;

					// for get address city state country
					String address = "";
					if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentCityId())) {
						address += CommonDocumentUtils.getCity(retailApplicantDetail.getPermanentCityId(),
								oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentStateId())) {
						address += CommonDocumentUtils.getState(retailApplicantDetail.getPermanentStateId().longValue(),
								oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentCountryId())) {
						address += CommonDocumentUtils
								.getCountry(retailApplicantDetail.getPermanentCountryId().longValue(), oneFormClient);
					} else {
						address += "NA";
					}

					RetailProposalDetails retailProposalDetails = new RetailProposalDetails();
					retailProposalDetails.setAddress(address);

					String name = "";

					if (CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getFirstName()))
						name += "NA";
					else
						name += retailApplicantDetail.getFirstName();

					if (CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getLastName()))
						name += " NA";
					else
						name += " " + retailApplicantDetail.getLastName();

					retailProposalDetails.setName(name);

					// set Branch State and city and name
					try {
						if (basicDetailsRequest.getLocationId() != null) {
							retailProposalDetails.setBranchLocationName(basicDetailsRequest.getName());
							retailProposalDetails
									.setBranchCity(basicDetailsRequest.getLocationMasterResponse().getCity().getName());
							retailProposalDetails.setBranchState(
									basicDetailsRequest.getLocationMasterResponse().getState().getName());
							retailProposalDetails.setBranchLocationName(basicDetailsRequest.getName());
						}
					} catch (Exception e) {
						logger.error("location id is null for this application:" + applicationId + "::" + e);
					}

					// calling DMS for getting fs retail profile image path

					DocumentRequest documentRequest = new DocumentRequest();
					documentRequest.setApplicationId(applicationId);
					documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
					documentRequest.setProductDocumentMappingId(
							CommonDocumentUtils.getProductDocumentId(loanApplicationMaster.getProductId()));
					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					String imagePath = null;
					if (documentResponse != null && documentResponse.getStatus() == 200) {
						List<Map<String, Object>> list = documentResponse.getDataList();
						if (!CommonUtils.isListNullOrEmpty(list)) {
							StorageDetailsResponse response = null;

							response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
									StorageDetailsResponse.class);

							if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
								imagePath = response.getFilePath();
							else
								imagePath = null;
						}
					}

					retailProposalDetails.setImagePath(imagePath);
					retailProposalDetails.setApplicationId(applicationId);
					retailProposalDetails.setProposalMappingId(proposalrequest.getId());
					retailProposalDetails.setFsType(CommonUtils.UserMainType.RETAIL);
					retailProposalDetails.setBusinessTypeId(loanApplicationMaster.getBusinessTypeId());
					retailProposalDetails.setFpProductid(fpProductId);

					retailProposalDetails.setProposalStatus(proposalrequest.getProposalStatusId());
					if(proposalrequest.getProposalStatusId() == ProposalStatus.HOLD || proposalrequest.getProposalStatusId() == ProposalStatus.DECLINE) {
						retailProposalDetails.setLastStatusActionDate(proposalrequest.getModifiedDate());
					}
					// get retail loan amount

					String loanAmount = "";
					if (!CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getAmount())) {
						loanAmount += df.format(loanApplicationMaster.getAmount());
					} else {
						loanAmount += "NA";
					}

					if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getCurrencyId())) {
						loanAmount += " " + Currency.getById(retailApplicantDetail.getCurrencyId());
					} else {
						loanAmount += " NA";
					}

					retailProposalDetails.setAmount(loanAmount);

					try {
						CibilRequest cibilRequest = new CibilRequest();
						cibilRequest.setApplicationId(applicationId);
						cibilRequest.setUserId(request.getUserId());
						cibilRequest.setPan(retailApplicantDetail.getPan());
						CibilScoreLogRequest cibilResponse = cibilClient.getCibilScoreByPanCard(cibilRequest);
						if (!CommonUtils.isObjectNullOrEmpty(cibilResponse.getActualScore())) {
							if(cibilResponse.getActualScore().equals("000-1")) {
								retailProposalDetails.setCibilSCore("-1");
							} else {
								retailProposalDetails.setCibilSCore(cibilResponse.getActualScore());
							}
						}
//						CibilResponse cibilResponse = cibilClient.getCibilScore(cibilRequest);
//						if (!CibilUtils.isObjectNullOrEmpty(cibilResponse)) {
//							String response = (String) cibilResponse.getData();
//							if (!CibilUtils.isObjectNullOrEmpty(response)) {
//								JSONObject jsonObject = new JSONObject(response);
//								JSONObject asset = jsonObject.getJSONObject("Asset");
//								if (!CibilUtils.isObjectNullOrEmpty(asset)) {
//									JSONObject trueLinkCreditReport = asset.getJSONObject("ns4:TrueLinkCreditReport");
//									if (!CibilUtils.isObjectNullOrEmpty(trueLinkCreditReport)) {
//										JSONObject creditScore = trueLinkCreditReport.getJSONObject("ns4:Borrower")
//												.getJSONObject("ns4:CreditScore");
//										if (!CibilUtils.isObjectNullOrEmpty(creditScore)) {
//											String score = creditScore.get("riskScore").toString();
//											logger.info("Pan===>" + cibilRequest.getPan() + " ==> Score===>" + score);
//											retailProposalDetails.setCibilSCore(score);
//										} else {
//											logger.info("no data Found from key ns4:CreditScore");
//										}
//
//									} else {
//										logger.info("no data Found from key ns4:TrueLinkCreditReport");
//									}
//
//								} else {
//									logger.info("no data Found from key ns4:Asset");
//								}
//							} else {
//								logger.info("Cibil Actual data Response Found NULL from Loans for PAN ==>"
//										+ cibilRequest.getPan());
//							}
//						} else {
//							logger.info("CibilResponse Found NULL from Loans for PAN ==>" + cibilRequest.getPan());
//						}
					} catch (Exception e) {
						logger.error("Error while getting CIbilScore of User : ",e);
					}
					proposalDetailsList.add(retailProposalDetails);
				}

			}

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return proposalDetailsList;
	}

	private UsersRequest getUserNameAndEmail(Long userId) {
		try {
			UserResponse userResponse = usersClient.getEmailAndNameByUserId(userId);
			if (!CommonUtils.isObjectNullOrEmpty(userResponse.getData())) {
				UsersRequest request = MultipleJSONObjectHelper
						.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(), UsersRequest.class);
				if (!CommonUtils.isObjectNullOrEmpty(request)) {
					return request;
				}
			}
		} catch (Exception e) {
			logger.error("Throw exception while get name and email by userid : ",e);
		}
		return null;
	}

	public String getOfflineProposalList(Long applicationId) {
		try {
			return loanRepository.getOfflineDetailsByAppId(applicationId);
		} catch (Exception e) {
			logger.error("Error : ",e);
		}
		return Collections.emptyList().toString();
	}

	@Override
	public List<FundProviderProposalDetails> fundseekerProposal(ProposalMappingRequest request, Long userId) {

		List<FundProviderProposalDetails> proposalDetailsList = new ArrayList<FundProviderProposalDetails>();

		try {
			// calling MATCHENGINE for getting proposal list

			ProposalMappingResponse proposalDetailsResponse = proposalDetailsClient.proposalListOfFundSeeker(request);

			List<Object[]> disbursmentData = loanDisbursementRepository.getDisbursmentData(request.getApplicationId());

			// GET OFFLINE STATUS FOR SHOW IN MATCHED LIST
			String offlineStatus = loanRepository.getOfflineStatusByAppId(request.getApplicationId());

			for (int i = 0; i < proposalDetailsResponse.getDataList().size(); i++) {
				ProposalMappingRequest proposalrequest = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) proposalDetailsResponse.getDataList().get(i),
						ProposalMappingRequest.class);

				ProductMaster master = productMasterRepository.findOne(proposalrequest.getFpProductId());
				UsersRequest userRequest = new UsersRequest();
				userRequest.setId(master.getUserId());

				// calling USER for getting fp details
				UserResponse userResponse = usersClient.getFPDetails(userRequest);

				FundProviderDetailsRequest fundProviderDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
						(LinkedHashMap<String, Object>) userResponse.getData(), FundProviderDetailsRequest.class);
				FundProviderProposalDetails fundProviderProposalDetails = new FundProviderProposalDetails();

				Long productId = proposalrequest.getFpProductId();

				fundProviderProposalDetails.setName(fundProviderDetailsRequest.getOrganizationName());
				fundProviderProposalDetails.setWhoAreYou(
						FundproviderType.getById(fundProviderDetailsRequest.getBusinessTypeMaster()).getValue());
				fundProviderProposalDetails.setFpType("DEBT");

				fundProviderProposalDetails
						.setFpProductName(CommonUtils.isObjectNullOrEmpty(master.getName()) ? " " : master.getName());

				// calling DMS for getting fp profile image path

				DocumentRequest documentRequest = new DocumentRequest();
				documentRequest.setUserId(master.getUserId());
				documentRequest.setUserType("user");
				documentRequest.setUserDocumentMappingId(1L);

				DocumentResponse documentResponse = dmsClient.listUserDocument(documentRequest);
				String imagePath = "";
				if (documentResponse != null && documentResponse.getStatus() == 200) {
					List<Map<String, Object>> list = documentResponse.getDataList();
					if (!CommonUtils.isListNullOrEmpty(list)) {
						StorageDetailsResponse response = null;

						response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0), StorageDetailsResponse.class);
						if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
							imagePath = response.getFilePath();
						else
							imagePath = "";
					}
				}

				fundProviderProposalDetails.setImagePath(imagePath);
				fundProviderProposalDetails.setProductId(productId);
				fundProviderProposalDetails.setProposalMappingId(proposalrequest.getId());
				fundProviderProposalDetails.setElAmount(proposalrequest.getElAmount());
				fundProviderProposalDetails.setElRoi(proposalrequest.getElRoi());
				fundProviderProposalDetails.setElTenure(proposalrequest.getElTenure());
				fundProviderProposalDetails.setOfflineStatus(offlineStatus);
				// add disbursed amount logic

				fundProviderProposalDetails
						.setPartiallyDisburseAmt(disbursmentData != null ? (Double) (disbursmentData.get(0)[0]) : null);
				fundProviderProposalDetails.setLastDisbursmentDate(
						disbursmentData != null ? String.valueOf(disbursmentData.get(0)[1]) : null);

				proposalDetailsList.add(fundProviderProposalDetails);
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		return proposalDetailsList;
	}

	@Override
	public ProposalCountResponse fundProviderProposalCount(ProposalMappingRequest request) {
		ProposalCountResponse response = new ProposalCountResponse();

		try {
			response = proposalDetailsClient.proposalCountOfFundProvider(request);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return response;
	}

	@Override
	public ProposalCountResponse fundSeekerProposalCount(ProposalMappingRequest request) {
		ProposalCountResponse response = new ProposalCountResponse();
		try {
			response = proposalDetailsClient.proposalCountOfFundSeeker(request);
			try {
				logger.info("Application ID==========================================> " +request.getApplicationId());
				Long offlineCount = loanRepository.getOfflineCountByAppId(request.getApplicationId());
				logger.info("Offline Counts ==========================================> " + offlineCount);
				response.setOffline(offlineCount);
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION,e);
			}
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return response;
	}

	@Override
	public ProposalMappingResponse get(ProposalMappingRequest request) {
		ProposalMappingResponse response = new ProposalMappingResponse();
		ProposalMappingRequest proposalMappingRequest=null;
		try {
			response = proposalDetailsClient.getProposal(request);

			proposalMappingRequest = (ProposalMappingRequest) MultipleJSONObjectHelper.getObjectFromMap(
					(Map<String, Object>) response.getData(), ProposalMappingRequest.class);

			ProposalDetails proposalDetails = proposalDetailRepository.getSanctionProposalByApplicationId(proposalMappingRequest.getApplicationId());
			ProposalDetails proposalDetails1 = proposalDetailRepository.getByApplicationIdAndFPProductId(request.getApplicationId(), request.getFpProductId());

			Boolean isButtonDisplay=true;
			String messageOfButton=null;
			Boolean isNBFCProposal = false;

			proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
			ProposalDetails proposalSanctionDisbusedByNbfc = null;
			if (proposalDetails1 != null && proposalDetails1.getNbfcFlow() != null) {
				isNBFCProposal = true;
				if (proposalDetails1.getNbfcFlow() == 2) {
					proposalSanctionDisbusedByNbfc = proposalDetailRepository.getSanctionProposalByApplicationNBFCFlow(proposalMappingRequest.getApplicationId(), 1);
					if (proposalSanctionDisbusedByNbfc.getProposalStatusId().getId() == CommonUtils.ApplicationStatus.ASSIGNED) {
						proposalMappingRequest.setMessageOfButton("Sanction pending from NBFC");
						proposalMappingRequest.setIsButtonDisplay(false);
					} else if (proposalSanctionDisbusedByNbfc.getProposalStatusId().getId() == CommonUtils.ApplicationStatus.APPROVED) {
						proposalSanctionDisbusedByNbfc = proposalDetailRepository.getSanctionProposalByApplicationNBFCFlow(proposalMappingRequest.getApplicationId(), 2);
						if (proposalSanctionDisbusedByNbfc.getProposalStatusId().getId() == CommonUtils.ApplicationStatus.APPROVED) { // check only if bank sanctioned on not
							proposalMappingRequest.setMessageOfButton("Disbursement pending from NBFC");
							proposalMappingRequest.setIsButtonDisplay(false);
						}
					}
				} else if (proposalDetails1.getNbfcFlow() == 1) {
					ProposalDetails proposalSanctionDisbusedByBank = proposalDetailRepository.getSanctionProposalByApplicationBankFlow(proposalMappingRequest.getApplicationId(), 2);
					proposalSanctionDisbusedByNbfc = proposalDetailRepository.getSanctionProposalByApplicationBankFlow(proposalMappingRequest.getApplicationId(), 1);
					if (proposalSanctionDisbusedByNbfc != null && proposalSanctionDisbusedByBank == null) {
						messageOfButton = "Sanction pending from bank";
						isButtonDisplay = false;
						proposalMappingRequest.setMessageOfButton(messageOfButton);
						proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
					}
				}
			}

			if(!isNBFCProposal){
				if(!CommonUtils.isObjectNullOrEmpty(proposalDetails))
				{
					if((!proposalDetails.getUserOrgId().toString().equals(request.getUserOrgId().toString())) && CommonUtils.isObjectNullOrEmpty(proposalDetails.getNbfcFlow()))
					{
						if(ProposalStatus.APPROVED ==  proposalDetails.getProposalStatusId().getId())
							messageOfButton="This proposal has been Sanctioned by Other Bank.";
						else if(ProposalStatus.DISBURSED ==  proposalDetails.getProposalStatusId().getId())
							messageOfButton="This proposal has been Disbursed by Other Bank.";
						else if(ProposalStatus.PARTIALLY_DISBURSED ==  proposalDetails.getProposalStatusId().getId())
							messageOfButton="This proposal has been Partially Disbursed by Other Bank.";
						isButtonDisplay=false;


					proposalMappingRequest.setMessageOfButton(messageOfButton);
					proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
				}
				else
				{
					proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
				}
			}
			else
			{
				IneligibleProposalDetails ineligibleProposalDetails = ineligibleProposalDetailsRepository.getSanctionedByApplicationId(proposalMappingRequest.getApplicationId());
				if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails)){
					if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getUserOrgId())
							&& !ineligibleProposalDetails.getUserOrgId().equals(request.getUserOrgId().toString())){
						if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getIsDisbursed()) && ineligibleProposalDetails.getIsDisbursed() == true)
							messageOfButton="This proposal has been Disbursed by Other Bank.";
						else if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getIsSanctioned()) && ineligibleProposalDetails.getIsSanctioned() == true)
							messageOfButton="This proposal has been Sanctioned by Other Bank.";
						isButtonDisplay=false;

							proposalMappingRequest.setMessageOfButton(messageOfButton);
							proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
						}else{
							proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
						}
					}
					proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
				}
			}

			response.setData(proposalMappingRequest);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		return response;
	}

	@Override
	public ProposalMappingResponse getSanctionProposalByApplicationId(Long applicationId,Long userOrgId) {
		ProposalMappingResponse response = new ProposalMappingResponse();
		ProposalMappingRequest proposalMappingRequest=new ProposalMappingRequest();
		try {

			ProposalDetails proposalDetails = proposalDetailRepository.getSanctionProposalByApplicationId(applicationId);

			Boolean isButtonDisplay=true;
			String messageOfButton=null;
			if(!CommonUtils.isObjectNullOrEmpty(proposalDetails))
			{
				if(proposalDetails.getUserOrgId() != userOrgId)
				{
					if(ProposalStatus.APPROVED ==  proposalDetails.getProposalStatusId().getId())
						messageOfButton="This proposal has been Sanctioned by Other Bank.";
					else if(ProposalStatus.DISBURSED ==  proposalDetails.getProposalStatusId().getId())
						messageOfButton="This proposal has been Disbursed by Other Bank.";
					else if(ProposalStatus.PARTIALLY_DISBURSED ==  proposalDetails.getProposalStatusId().getId())
						messageOfButton="This proposal has been Partially Disbursed by Other Bank.";
					isButtonDisplay=false;

					proposalMappingRequest.setMessageOfButton(messageOfButton);
					proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
				}
				else
				{
					proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
				}
			}
			else{
				IneligibleProposalDetails ineligibleProposalDetails = ineligibleProposalDetailsRepository.getSanctionedByApplicationId(applicationId);
				if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails)){
					if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getUserOrgId())
							&& ineligibleProposalDetails.getUserOrgId() != userOrgId){
						if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getIsDisbursed()) && ineligibleProposalDetails.getIsDisbursed() == true)
							messageOfButton="This proposal has been Disbursed by Other Bank.";
						else if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getIsSanctioned()) && ineligibleProposalDetails.getIsSanctioned() == true)
							messageOfButton="This proposal has been Sanctioned by Other Bank.";
						isButtonDisplay=false;

						proposalMappingRequest.setMessageOfButton(messageOfButton);
						proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
					}else{
						proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
					}
				}
				proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
			}
			response.setData(proposalMappingRequest);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		return response;
	}

	@Override
	public ProposalMappingResponse changeStatus(ProposalMappingRequest request) {

		ProposalMappingResponse response = new ProposalMappingResponse();

		try {
			response = proposalDetailsClient.changeStatus(request);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return response;
	}

	@Override
	public ProposalMappingResponse listOfFundSeekerProposal(ProposalMappingRequest request) {

		ProposalMappingResponse response = new ProposalMappingResponse();
		try {
			response = proposalDetailsClient.listOfFundSeekerProposal(request);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return response;
	}

	@Override
	public ProposalResponse getConectionList(ProposalMappingRequest proposalMappingRequest) {

		ProposalResponse proposalResponse = new ProposalResponse();

		List proposalByMatches = new ArrayList();
		List proposalWithoutMatches = new ArrayList();
		// calling MATCHENGINE for getting connection list

		try {

			ProposalMappingResponse proposalDetailsResponse = proposalDetailsClient.connections(proposalMappingRequest);
			ConnectionResponse connectionResponse = (ConnectionResponse) MultipleJSONObjectHelper.getObjectFromMap(
					(Map<String, Object>) proposalDetailsResponse.getData(), ConnectionResponse.class);

			if (!(CommonUtils.UserType.FUND_SEEKER == proposalMappingRequest.getUserType())) {
				// for get suggetionListby matches (10)
				for (int i = 0; i < connectionResponse.getSuggetionByMatchesList().size(); i++) {
					try {
						BigInteger applicationId = (BigInteger) connectionResponse.getSuggetionByMatchesList().get(i);
						LoanApplicationMaster loanApplicationMaster = loanApplicationRepository
								.findOne(applicationId.longValue());
						Integer bId = loanApplicationMaster.getBusinessTypeId();
						if (CommonUtils.UserMainType.CORPORATE == CommonUtils
								.getUserMainType(loanApplicationMaster.getProductId())) {

							CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
									.findOneByApplicationIdId(applicationId.longValue());

							if (corporateApplicantDetail == null)
								continue;

							// for get address city state country
							String address = "";
							if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCityId())) {
								address += CommonDocumentUtils.getCity(corporateApplicantDetail.getRegisteredCityId(),
										oneFormClient) + ",";
							} else {
								address += "NA ,";
							}
							if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredStateId())) {
								address += CommonDocumentUtils.getState(
										corporateApplicantDetail.getRegisteredStateId().longValue(), oneFormClient)
										+ ",";
							} else {
								address += "NA ,";
							}
							if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCountryId())) {
								address += CommonDocumentUtils.getCountry(
										corporateApplicantDetail.getRegisteredCountryId().longValue(), oneFormClient);
							} else {
								address += "NA";
							}

							CorporateProposalDetails corporateProposalDetails = new CorporateProposalDetails();

							corporateProposalDetails.setAddress(address);

							if (CommonUtils.BusinessType.NEW_TO_BUSINESS.getId().equals(bId)) {

								corporateProposalDetails.setName(getMainDirectorName(applicationId.longValue()));

							} else if (CommonUtils.BusinessType.EXISTING_BUSINESS.getId().equals(bId) || bId == null) {
								if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getOrganisationName()))
									corporateProposalDetails.setName("NA");
								else
									corporateProposalDetails.setName(corporateApplicantDetail.getOrganisationName());
							}

							corporateProposalDetails.setFsMainType(
									CommonUtils.getCorporateLoanType(loanApplicationMaster.getProductId()));
							corporateProposalDetails.setWcRenualNew(loanApplicationMaster.getWcRenewalStatus()!= null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue().toString() : "New");
							corporateProposalDetails.setApplicationCode(loanApplicationMaster.getApplicationCode()!= null ?  loanApplicationMaster.getApplicationCode() : "-");

							String amount = "";
							if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getAmount()))
								amount += "NA";
							else
								amount += df.format(loanApplicationMaster.getAmount());

							if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getDenominationId()))
								amount += " NA";
							else
								amount += " "
										+ Denomination.getById(loanApplicationMaster.getDenominationId()).getValue();

							corporateProposalDetails.setAmount(amount);

							// calling DMS for getting fs corporate profile
							// image
							// path

							DocumentRequest documentRequest = new DocumentRequest();
							documentRequest.setApplicationId(applicationId.longValue());
							documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
							documentRequest.setProductDocumentMappingId(
									CommonDocumentUtils.getProductDocumentId(loanApplicationMaster.getProductId()));

							DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
							String imagePath = null;
							if (documentResponse != null && documentResponse.getStatus() == 200) {
								List<Map<String, Object>> list = documentResponse.getDataList();
								if (!CommonUtils.isListNullOrEmpty(list)) {
									StorageDetailsResponse response = null;

									response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
											StorageDetailsResponse.class);

									if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
										imagePath = response.getFilePath();
									else
										imagePath = null;
								}
							}

							corporateProposalDetails.setImagePath(imagePath);
							corporateProposalDetails.setApplicationId(applicationId.longValue());
							corporateProposalDetails.setFsType(CommonUtils.UserMainType.CORPORATE);
							proposalByMatches.add(corporateProposalDetails);
						} else {
							RetailApplicantDetail retailApplicantDetail = retailApplicantDetailRepository
									.findByApplicationId(applicationId.longValue());

							if (retailApplicantDetail == null)
								continue;

							// for get address city state country
							String address = "";
							if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentCityId())) {
								address += CommonDocumentUtils.getCity(retailApplicantDetail.getPermanentCityId(),
										oneFormClient) + ",";
							} else {
								address += "NA ,";
							}
							if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentStateId())) {
								address += CommonDocumentUtils.getState(
										retailApplicantDetail.getPermanentStateId().longValue(), oneFormClient) + ",";
							} else {
								address += "NA ,";
							}
							if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentCountryId())) {
								address += CommonDocumentUtils.getCountry(
										retailApplicantDetail.getPermanentCountryId().longValue(), oneFormClient);
							} else {
								address += "NA";
							}

							RetailProposalDetails retailProposalDetails = new RetailProposalDetails();
							retailProposalDetails.setAddress(address);

							String name = "";

							if (CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getFirstName()))
								name += "NA";
							else
								name += retailApplicantDetail.getFirstName();

							retailProposalDetails.setName(name);

							// calling DMS for getting fs retail profile image
							// path

							DocumentRequest documentRequest = new DocumentRequest();
							documentRequest.setApplicationId(applicationId.longValue());
							documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
							documentRequest.setProductDocumentMappingId(
									CommonDocumentUtils.getProductDocumentId(loanApplicationMaster.getProductId()));
							DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
							String imagePath = null;
							if (documentResponse != null && documentResponse.getStatus() == 200) {
								List<Map<String, Object>> list = documentResponse.getDataList();
								if (!CommonUtils.isListNullOrEmpty(list)) {
									StorageDetailsResponse response = null;

									response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
											StorageDetailsResponse.class);

									if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
										imagePath = response.getFilePath();
									else
										imagePath = null;
								}
							}

							retailProposalDetails.setImagePath(imagePath);
							retailProposalDetails.setApplicationId(applicationId.longValue());
							retailProposalDetails.setFsType(CommonUtils.UserMainType.RETAIL);
							proposalByMatches.add(retailProposalDetails);
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}

				}
				// for set connection without proposal 10

				for (int i = 0; i < connectionResponse.getSuggetionList().size(); i++) {
					try {
						BigInteger applicationId = (BigInteger) connectionResponse.getSuggetionList().get(i);
						LoanApplicationMaster loanApplicationMaster = loanApplicationRepository
								.findOne(applicationId.longValue());
						Integer bId = loanApplicationMaster.getBusinessTypeId();
						if (CommonUtils.UserMainType.CORPORATE == CommonUtils
								.getUserMainType(loanApplicationMaster.getProductId())) {

							CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
									.findOneByApplicationIdId(applicationId.longValue());

							if (corporateApplicantDetail == null)
								continue;

							// for get address city state country
							String address = "";
							if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCityId())) {
								address += CommonDocumentUtils.getCity(corporateApplicantDetail.getRegisteredCityId(),
										oneFormClient) + ",";
							} else {
								address += "NA ,";
							}
							if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredStateId())) {
								address += CommonDocumentUtils.getState(
										corporateApplicantDetail.getRegisteredStateId().longValue(), oneFormClient)
										+ ",";
							} else {
								address += "NA ,";
							}
							if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCountryId())) {
								address += CommonDocumentUtils.getCountry(
										corporateApplicantDetail.getRegisteredCountryId().longValue(), oneFormClient);
							} else {
								address += "NA";
							}

							CorporateProposalDetails corporateProposalDetails = new CorporateProposalDetails();

							corporateProposalDetails.setAddress(address);

							if (CommonUtils.BusinessType.NEW_TO_BUSINESS.getId().equals(bId)) {

								corporateProposalDetails.setName(getMainDirectorName(applicationId.longValue()));

							} else if (CommonUtils.BusinessType.EXISTING_BUSINESS.getId().equals(bId) || bId == null) {
								if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getOrganisationName()))
									corporateProposalDetails.setName("NA");
								else
									corporateProposalDetails.setName(corporateApplicantDetail.getOrganisationName());
							}

							corporateProposalDetails.setFsMainType(
									CommonUtils.getCorporateLoanType(loanApplicationMaster.getProductId()));
							corporateProposalDetails.setWcRenualNew(loanApplicationMaster.getWcRenewalStatus()!= null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue().toString() : "New");
							corporateProposalDetails.setApplicationCode(loanApplicationMaster.getApplicationCode()!= null ?  loanApplicationMaster.getApplicationCode() : "-");

							String amount = "";
							if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getAmount()))
								amount += "NA";
							else
								amount += df.format(loanApplicationMaster.getAmount());

							if (CommonUtils.isObjectNullOrEmpty(loanApplicationMaster.getDenominationId()))
								amount += " NA";
							else
								amount += " "
										+ Denomination.getById(loanApplicationMaster.getDenominationId()).getValue();

							corporateProposalDetails.setAmount(amount);

							// calling DMS for getting fs corporate profile
							// image
							// path

							DocumentRequest documentRequest = new DocumentRequest();
							documentRequest.setApplicationId(applicationId.longValue());
							documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
							documentRequest.setProductDocumentMappingId(
									CommonDocumentUtils.getProductDocumentId(loanApplicationMaster.getProductId()));

							DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
							String imagePath = null;
							if (documentResponse != null && documentResponse.getStatus() == 200) {
								List<Map<String, Object>> list = documentResponse.getDataList();
								if (!CommonUtils.isListNullOrEmpty(list)) {
									StorageDetailsResponse response = null;

									response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
											StorageDetailsResponse.class);

									if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
										imagePath = response.getFilePath();
									else
										imagePath = null;
								}
							}

							corporateProposalDetails.setImagePath(imagePath);
							corporateProposalDetails.setApplicationId(applicationId.longValue());
							corporateProposalDetails.setFsType(CommonUtils.UserMainType.CORPORATE);
							proposalWithoutMatches.add(corporateProposalDetails);
						} else {
							RetailApplicantDetail retailApplicantDetail = retailApplicantDetailRepository
									.findByApplicationId(applicationId.longValue());

							if (retailApplicantDetail == null)
								continue;

							// for get address city state country
							String address = "";
							if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentCityId())) {
								address += CommonDocumentUtils.getCity(retailApplicantDetail.getPermanentCityId(),
										oneFormClient) + ",";
							} else {
								address += "NA ,";
							}
							if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentStateId())) {
								address += CommonDocumentUtils.getState(
										retailApplicantDetail.getPermanentStateId().longValue(), oneFormClient) + ",";
							} else {
								address += "NA ,";
							}
							if (!CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getPermanentCountryId())) {
								address += CommonDocumentUtils.getCountry(
										retailApplicantDetail.getPermanentCountryId().longValue(), oneFormClient);
							} else {
								address += "NA";
							}

							RetailProposalDetails retailProposalDetails = new RetailProposalDetails();
							retailProposalDetails.setAddress(address);

							String name = "";

							if (CommonUtils.isObjectNullOrEmpty(retailApplicantDetail.getFirstName()))
								name += "NA";
							else
								name += retailApplicantDetail.getFirstName();

							retailProposalDetails.setName(name);

							// calling DMS for getting fs retail profile image
							// path

							DocumentRequest documentRequest = new DocumentRequest();
							documentRequest.setApplicationId(applicationId.longValue());
							documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
							documentRequest.setProductDocumentMappingId(
									CommonDocumentUtils.getProductDocumentId(loanApplicationMaster.getProductId()));
							DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
							String imagePath = null;
							if (documentResponse != null && documentResponse.getStatus() == 200) {
								List<Map<String, Object>> list = documentResponse.getDataList();
								if (!CommonUtils.isListNullOrEmpty(list)) {
									StorageDetailsResponse response = null;

									response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
											StorageDetailsResponse.class);

									if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
										imagePath = response.getFilePath();
									else
										imagePath = null;
								}
							}

							retailProposalDetails.setImagePath(imagePath);
							retailProposalDetails.setApplicationId(applicationId.longValue());
							retailProposalDetails.setFsType(CommonUtils.UserMainType.RETAIL);
							proposalWithoutMatches.add(retailProposalDetails);
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}

				}

			} else {

				List<Long> userOrgSuggetionByMatchesList = new ArrayList<>();
				logger.info("Total FP found for fs connection Suggetion By Matches list ---------------------> "
						+ connectionResponse.getSuggetionByMatchesList().size());
				for (int i = 0; i < connectionResponse.getSuggetionByMatchesList().size(); i++) {
					try {
						BigInteger fpProductId = BigInteger.class
								.cast(connectionResponse.getSuggetionByMatchesList().get(i));
						ProductMaster master = productMasterRepository.findOne(fpProductId.longValue());

						if (!CommonUtils.isObjectNullOrEmpty(master) && !CommonUtils.isObjectNullOrEmpty(master.getUserOrgId()) ) {
								if (userOrgSuggetionByMatchesList.contains(master.getUserOrgId())) {
									logger.info(
											"Found same user org id in connection suggestion by matches list ---------------"
													+ master.getId() + "--------------->" + master.getUserOrgId());
									continue;
								}
								userOrgSuggetionByMatchesList.add(master.getUserOrgId());
						}

						UsersRequest userRequest = new UsersRequest();
						userRequest.setId(master.getUserId());

						// calling USER for getting fp details
						UserResponse userResponse = usersClient.getFPDetails(userRequest);

						FundProviderDetailsRequest fundProviderDetailsRequest = MultipleJSONObjectHelper
								.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(),
										FundProviderDetailsRequest.class);
						FundProviderProposalDetails fundProviderProposalDetails = new FundProviderProposalDetails();

						fundProviderProposalDetails.setName(fundProviderDetailsRequest.getOrganizationName());
						fundProviderProposalDetails.setWhoAreYou(FundproviderType
								.getById(fundProviderDetailsRequest.getBusinessTypeMaster()).getValue());
						fundProviderProposalDetails.setFpType("DEBT");

						fundProviderProposalDetails.setFpProductName(
								CommonUtils.isObjectNullOrEmpty(master.getName()) ? " " : master.getName());

						// calling DMS for getting fp profile image path

						DocumentRequest documentRequest = new DocumentRequest();
						documentRequest.setUserId(master.getUserId());
						documentRequest.setUserType("user");
						documentRequest.setUserDocumentMappingId(1L);

						DocumentResponse documentResponse = dmsClient.listUserDocument(documentRequest);
						String imagePath = null;
						if (documentResponse != null && documentResponse.getStatus() == 200) {
							List<Map<String, Object>> list = documentResponse.getDataList();
							if (!CommonUtils.isListNullOrEmpty(list)) {
								StorageDetailsResponse response = null;

								response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
										StorageDetailsResponse.class);
								if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
									imagePath = response.getFilePath();
								else
									imagePath = null;
							}
						}

						fundProviderProposalDetails.setImagePath(imagePath);
						fundProviderProposalDetails.setProductId(fpProductId.longValue());
						proposalByMatches.add(fundProviderProposalDetails);
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}

				// set connection without matches
				List<Long> userOgList = new ArrayList<>();
				logger.info("Total FP found for fs connection Suggetion List ---------------------> "
						+ connectionResponse.getSuggetionList().size());
				for (int i = 0; i < connectionResponse.getSuggetionList().size(); i++) {
					try {

						BigInteger fpProductId = BigInteger.class.cast(connectionResponse.getSuggetionList().get(i));
						ProductMaster master = productMasterRepository.findOne(fpProductId.longValue());
						if (!CommonUtils.isObjectNullOrEmpty(master) && !CommonUtils.isObjectNullOrEmpty(master.getUserOrgId()) ) {
								if (userOgList.contains(master.getUserOrgId())) {
									logger.info("Found same user org id in connection suggestion list ---------------"
											+ master.getId() + "--------------->" + master.getUserOrgId());
									continue;
								}
								userOgList.add(master.getUserOrgId());
						}
						UsersRequest userRequest = new UsersRequest();
						userRequest.setId(master.getUserId());

						// calling USER for getting fp details
						UserResponse userResponse = usersClient.getFPDetails(userRequest);

						FundProviderDetailsRequest fundProviderDetailsRequest = MultipleJSONObjectHelper
								.getObjectFromMap((LinkedHashMap<String, Object>) userResponse.getData(),
										FundProviderDetailsRequest.class);
						FundProviderProposalDetails fundProviderProposalDetails = new FundProviderProposalDetails();

						fundProviderProposalDetails.setName(fundProviderDetailsRequest.getOrganizationName());
						fundProviderProposalDetails.setWhoAreYou(FundproviderType
								.getById(fundProviderDetailsRequest.getBusinessTypeMaster()).getValue());
						fundProviderProposalDetails.setFpType("DEBT");

						fundProviderProposalDetails.setFpProductName(
								CommonUtils.isObjectNullOrEmpty(master.getName()) ? " " : master.getName());

						// calling DMS for getting fp profile image path

						DocumentRequest documentRequest = new DocumentRequest();
						documentRequest.setUserId(master.getUserId());
						documentRequest.setUserType("user");
						documentRequest.setUserDocumentMappingId(1L);

						DocumentResponse documentResponse = dmsClient.listUserDocument(documentRequest);
						String imagePath = null;
						if (documentResponse != null && documentResponse.getStatus() == 200) {
							List<Map<String, Object>> list = documentResponse.getDataList();
							if (!CommonUtils.isListNullOrEmpty(list)) {
								StorageDetailsResponse response = null;

								response = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
										StorageDetailsResponse.class);
								if (!CommonUtils.isObjectNullOrEmpty(response.getFilePath()))
									imagePath = response.getFilePath();
								else
									imagePath = null;
							}
						}

						fundProviderProposalDetails.setImagePath(imagePath);
						fundProviderProposalDetails.setProductId(fpProductId.longValue());
						proposalWithoutMatches.add(fundProviderProposalDetails);
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
				}
			}

		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
			return null;
		}

		proposalResponse.setProposalByMatches(proposalByMatches);
		proposalResponse.setProposalWithoutMatches(proposalWithoutMatches);
		return proposalResponse;
	}

	@Override
	public ProposalMappingResponse sendRequest(ProposalMappingRequest request) {

		ProposalMappingResponse response = new ProposalMappingResponse();
		try {
			response = proposalDetailsClient.sendRequest(request);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return response;
	}

	@Override
	public Integer getPendingProposalCount(Long applicationId) {
		ProposalMappingResponse response = new ProposalMappingResponse();
		try {
			response = proposalDetailsClient.getPendingProposalCount(applicationId);
			return (Integer) response.getData();
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		return null;
	}

	@Override
	public ProposalMappingResponse updateAssignDetails(ProposalMappingRequest request) throws LoansException {
		try {
			return proposalDetailsClient.updateAssignDetails(request);
		} catch (Exception e) {
			logger.error("Throw Exception while updating assign issue : ",e);
			throw new LoansException("Somethig went wrong");
		}
	}

	@Override
	public List<?> fundproviderProposalByAssignBy(ProposalMappingRequest request) {

		try {
			// set branch id to proposal request
			UsersRequest usersRequest = new UsersRequest();
			usersRequest.setId(request.getUserId());
			logger.info(CURRENT_USER_ID_MSG + request.getUserId());
			UserResponse userResponse = usersClient.getBranchDetailsBYUserId(usersRequest);
			BranchBasicDetailsRequest basicDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
					(LinkedHashMap<String, Object>) userResponse.getData(), BranchBasicDetailsRequest.class);
			if (!CommonUtils.isObjectNullOrEmpty(basicDetailsRequest)) {
				logger.info(FOUND_BRANCH_ID_MSG + basicDetailsRequest.getId()
						+ ROLE_ID_MSG + basicDetailsRequest.getRoleId());
				if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.BO) {
					logger.info("Current user is Branch officer");
					request.setBranchId(basicDetailsRequest.getId());
				}
			} else {
				logger.info(BRANCH_ID_CAN_NOT_BE_FOUND_MSG);
			}
		} catch (Exception e) {
			logger.error(THROW_EXCEPTION_WHILE_GET_BRANCH_ID_FROM_USER_ID_MSG,e);
		}

		List proposalByMatches = new ArrayList();
		try {
			ProposalMappingResponse response = proposalDetailsClient.proposalListByAssignee(request);
			logger.info("Found total assigned proposal -------------------------->" + response.getDataList().size());
			// mappingRequests =response.getDataList();
			if (!CommonUtils.isListNullOrEmpty(response.getDataList())) {
				for (int i = 0; i < response.getDataList().size(); i++) {
					ProposalMappingRequest proposalrequest = MultipleJSONObjectHelper.getObjectFromMap(
							(LinkedHashMap<String, Object>) response.getDataList().get(i),
							ProposalMappingRequest.class);

					if (CommonUtils.isObjectNullOrEmpty(proposalrequest)) {
						logger.info("proposalrequest is null or empty");
						continue;
					}

					Long applicationId = proposalrequest.getApplicationId();
					LoanApplicationMaster loanApplicationMaster = loanApplicationRepository.findOne(applicationId);
					ApplicationProposalMapping applicationProposalMapping = applicationProposalMappingRepository.findByProposalIdAndIsActive(proposalrequest.getId(), true);
					if(CommonUtils.isObjectNullOrEmpty(applicationProposalMapping)){
						logger.info("Proposal not in application_proposal_mapping table "+applicationProposalMapping.getProposalId());
						continue;
					}
					Integer bId = applicationProposalMapping.getBusinessTypeId();
					if (!applicationProposalMapping.getIsActive()) {
						logger.info(
								"Application Id is InActive while get fundprovider proposals=====>" + applicationId);
						continue;
					}

					CorporateApplicantDetail corporateApplicantDetail = corporateApplicantDetailRepository
							.findOneByApplicationIdId(applicationId);

					if (corporateApplicantDetail == null)
						continue;

					// for get address city state country
					String address = "";
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCityId())) {
						address += CommonDocumentUtils.getCity(corporateApplicantDetail.getRegisteredCityId(),
								oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredStateId())) {
						address += CommonDocumentUtils.getState(
								corporateApplicantDetail.getRegisteredStateId().longValue(), oneFormClient) + ",";
					} else {
						address += "NA ,";
					}
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getRegisteredCountryId())) {
						address += CommonDocumentUtils.getCountry(
								corporateApplicantDetail.getRegisteredCountryId().longValue(), oneFormClient);
					} else {
						address += "NA";
					}

					CorporateProposalDetails corporateProposalDetails = new CorporateProposalDetails();

					corporateProposalDetails.setAddress(address);

					if (CommonUtils.BusinessType.NEW_TO_BUSINESS.getId().equals(bId)) {

						corporateProposalDetails.setName(getMainDirectorName(applicationId));

					} else if (CommonUtils.BusinessType.EXISTING_BUSINESS.getId().equals(bId) || bId == null) {
						if (CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getOrganisationName()))
							corporateProposalDetails.setName("NA");
						else
							corporateProposalDetails.setName(corporateApplicantDetail.getOrganisationName());
					}

					corporateProposalDetails
							.setFsMainType(CommonUtils.getCorporateLoanType(applicationProposalMapping.getProductId()));
					corporateProposalDetails.setWcRenualNew(loanApplicationMaster.getWcRenewalStatus()!= null ? WcRenewalType.getById(loanApplicationMaster.getWcRenewalStatus()).getValue().toString() : "New");
					corporateProposalDetails.setApplicationCode(applicationProposalMapping.getApplicationCode()!= null ?  applicationProposalMapping.getApplicationCode() : "-");

					// for get industry id
					List<Long> listIndustryIds = industrySectorRepository.getIndustryByApplicationId(applicationId);
					if (listIndustryIds.size() > 0) {
						OneFormResponse formResponse = oneFormClient.getIndustryById(listIndustryIds);
						List<Map<String, Object>> loanResponseDatalist = (List<Map<String, Object>>) formResponse
								.getListData();
						String industry = "";
						if (loanResponseDatalist.size() > 0) {
							for (int k = 0; k < loanResponseDatalist.size(); k++) {
								MasterResponse masterResponse;
								masterResponse = MultipleJSONObjectHelper.getObjectFromMap(loanResponseDatalist.get(k),
										MasterResponse.class);
								industry += masterResponse.getValue() + " ,";
							}
							corporateProposalDetails.setIndustry(industry);
						} else {
							corporateProposalDetails.setIndustry("NA");
						}
					} else {
						corporateProposalDetails.setIndustry("NA");
					}

					List<Long> keyVerticalFundingId = new ArrayList<>();
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVericalFunding()))
						keyVerticalFundingId.add(corporateApplicantDetail.getKeyVericalFunding());
					if (!CommonUtils.isListNullOrEmpty(keyVerticalFundingId)) {
						try {
							OneFormResponse oneFormResponse = oneFormClient.getIndustryById(keyVerticalFundingId);
							List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
									.getListData();
							if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
								MasterResponse masterResponse = MultipleJSONObjectHelper
										.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
								corporateProposalDetails.setKeyVertical(masterResponse.getValue());
							} else {
								corporateProposalDetails.setKeyVertical("NA");
							}
						} catch (Exception e) {
							logger.error(CommonUtils.EXCEPTION,e);
						}
					}

					// key vertical sector
					List<Long> keyVerticalSectorId = new ArrayList<>();
					// getting sector id from mapping
					if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSector()))
						keyVerticalSectorId.add(corporateApplicantDetail.getKeyVerticalSector());
					try {
						OneFormResponse formResponse = oneFormClient
								.getIndustrySecByMappingId(corporateApplicantDetail.getKeyVerticalSector());
						SectorIndustryModel sectorIndustryModel = MultipleJSONObjectHelper
								.getObjectFromMap((Map) formResponse.getData(), SectorIndustryModel.class);

						// get key vertical sector value
						OneFormResponse oneFormResponse = oneFormClient
								.getSectorById(Arrays.asList(sectorIndustryModel.getSectorId()));
						List<Map<String, Object>> oneResponseDataList = (List<Map<String, Object>>) oneFormResponse
								.getListData();
						if (oneResponseDataList != null && !oneResponseDataList.isEmpty()) {
							MasterResponse masterResponse = MultipleJSONObjectHelper
									.getObjectFromMap(oneResponseDataList.get(0), MasterResponse.class);
							corporateProposalDetails.setSector(masterResponse.getValue());
						} else {
							corporateProposalDetails.setSector("NA");
						}
					} catch (Exception e) {
						logger.error(CommonUtils.EXCEPTION,e);
					}
					// key vertical Subsector
					try {
						if (!CommonUtils.isObjectNullOrEmpty(corporateApplicantDetail.getKeyVerticalSubsector())) {
							OneFormResponse oneFormResponse = oneFormClient
									.getSubSecNameByMappingId(corporateApplicantDetail.getKeyVerticalSubsector());
							corporateProposalDetails.setSubSector((String) oneFormResponse.getData());
						}
					} catch (Exception e) {
						logger.error("error while getting key vertical sub-sector : ",e);
					}

					String amount = "";
					if (CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.getLoanAmount()))
						amount += "NA";
					else
						amount += df.format(applicationProposalMapping.getLoanAmount());

					/*if (CommonUtils.isObjectNullOrEmpty(applicationProposalMapping.get))
						amount += " NA";
					else
						amount += " " + Denomination.getById(loanApplicationMaster.getDenominationId()).getValue();*/

					corporateProposalDetails.setAmount(amount);

					// calling DMS for getting fs corporate profile image path

					DocumentRequest documentRequest = new DocumentRequest();
					documentRequest.setApplicationId(applicationId);
					documentRequest.setUserType(DocumentAlias.UERT_TYPE_APPLICANT);
					documentRequest.setProductDocumentMappingId(
							CommonDocumentUtils.getProductDocumentId(applicationProposalMapping.getProductId()));

					DocumentResponse documentResponse = dmsClient.listProductDocument(documentRequest);
					String imagePath = null;
					if (documentResponse != null && documentResponse.getStatus() == 200) {
						List<Map<String, Object>> list = documentResponse.getDataList();
						if (!CommonUtils.isListNullOrEmpty(list)) {
							StorageDetailsResponse storageDetailsResponse = null;

							storageDetailsResponse = MultipleJSONObjectHelper.getObjectFromMap(list.get(0),
									StorageDetailsResponse.class);

							if (!CommonUtils.isObjectNullOrEmpty(storageDetailsResponse.getFilePath()))
								imagePath = storageDetailsResponse.getFilePath();
							else
								imagePath = null;
						}
					}

					corporateProposalDetails.setImagePath(imagePath);
					corporateProposalDetails.setApplicationId(applicationId);
					corporateProposalDetails.setProposalMappingId(proposalrequest.getId());
					corporateProposalDetails.setFsType(CommonUtils.UserMainType.CORPORATE);
					corporateProposalDetails.setModifiedDate(applicationProposalMapping.getModifiedDate());
					corporateProposalDetails.setAssignDate(proposalrequest.getAssignDate());

					UsersRequest usersRequest = new UsersRequest();
					usersRequest.setId(request.getUserId());
					UserResponse usrResponse = usersClient.getFPDetails(usersRequest);
					FundProviderDetailsRequest fundProviderDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
							(LinkedHashMap<String, Object>) usrResponse.getData(), FundProviderDetailsRequest.class);
					if (!CommonUtils.isObjectNullOrEmpty(fundProviderDetailsRequest.getCityId())) {
							corporateProposalDetails.setCity(CommonDocumentUtils
									.getCity(fundProviderDetailsRequest.getCityId().longValue(), oneFormClient));
					}
					if (!CommonUtils.isObjectNullOrEmpty(fundProviderDetailsRequest.getPincode())) {
						corporateProposalDetails.setPincode(fundProviderDetailsRequest.getPincode());
					}

					/*
					 * if(!CommonUtils.isObjectNullOrEmpty(proposalrequest.
					 * getAssignBy())) { UsersRequest usersRequest =
					 * getUserNameAndEmail(proposalrequest.getAssignBy());
					 * if(!CommonUtils.isObjectNullOrEmpty(usersRequest)) {
					 * corporateProposalDetails.setAssignBy(usersRequest.getName
					 * ()); } }
					 */
					if (!CommonUtils.isObjectNullOrEmpty(proposalrequest.getAssignBranchTo())) {
						try {
							UserResponse userResponse = usersClient
									.getBranchNameById(proposalrequest.getAssignBranchTo());
							if (!CommonUtils.isObjectNullOrEmpty(userResponse)) {
								corporateProposalDetails.setAssignbranch((String) userResponse.getData());
							}
						} catch (Exception e) {
							logger.error("Throw Exception while get branch name by branch id--------->" + proposalrequest.getAssignBranchTo() + " :: ",e);
						}
						corporateProposalDetails.setIsAssignedToBranch(true);
					} else {
						corporateProposalDetails.setIsAssignedToBranch(false);
					}

					proposalByMatches.add(corporateProposalDetails);
				}
			}
		} catch (Exception e) {
			logger.error("Throw Exception while Get assign by proposal : ",e);
		}
		return proposalByMatches;

	}

	@Override
	public ProposalMappingResponse saveDisbursementDetails(DisbursementDetailsModel request, Long userId) {
		try {
			// set branch id to proposal request
			logger.info("DISBURSEMENT DETAILS IS ---------------------------------------------------> " + request.toString());

			Date connectlogModifiedDate = connectClient.getInprincipleDateByAppId(request.getApplicationId());
				if(!CommonUtils.isObjectNullOrEmpty(request.getDisbursementDate()))
				{
				request.getDisbursementDate().setHours(0);
				request.getDisbursementDate().setMinutes(0);
				request.getDisbursementDate().setSeconds(0);
				}
				if(!CommonUtils.isObjectNullOrEmpty(connectlogModifiedDate))
				{
				connectlogModifiedDate.setHours(0);
				connectlogModifiedDate.setMinutes(0);
				connectlogModifiedDate.setSeconds(0);
				}
				
			if (!CommonUtils.isObjectNullOrEmpty(connectlogModifiedDate)) {
				if (request.getDisbursementDate().compareTo(connectlogModifiedDate)<0 || request.getDisbursementDate().compareTo(new Date())>0) {
					return	new ProposalMappingResponse("Please insert valid disbursement date",
							HttpStatus.INTERNAL_SERVER_ERROR.value());
				}
			}
			DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
			//Comparing Date only
			if (!CommonUtils.isObjectNullOrEmpty(connectlogModifiedDate) && comparator.compare(request.getDisbursementDate(), connectlogModifiedDate) < 0) {
				return	new ProposalMappingResponse("Please insert valid disbursement date",
							HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
			return proposalDetailsClient.saveDisbursementDetails(request);

		} catch (Exception e) {
			logger.error("Throw Exception While saveDisbursementDetails : ",e);
			new ProposalMappingResponse("error while saving disbursement details",
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return null;
	}

	@Override
	public LoansResponse checkMinMaxAmount(UsersRequest userRequest,Long userOrgId) {
		LoansResponse loansResponse = new LoansResponse();

		try
		{
			loansResponse.setFlag(true);
            ApplicationProposalMapping applicationProposalMapping=applicationProposalMappingRepository.getByApplicationIdAndOrgId(userRequest.getApplicationId(), userOrgId);
			UserResponse userResponse = null;
			userRequest.setProductIdString(CommonUtility.encode("" + applicationProposalMapping.getProductId()));


			// check proposal is assigned and current user have not permission to access this proposal
			if (applicationProposalMapping.getNpUserId() != null && !(applicationProposalMapping.getNpUserId()).equals(userRequest.getId()) )
			{
				loansResponse.setFlag(false);
				loansResponse.setMessage(YOU_DO_NOT_HAVE_RIGHTS_TO_TAKE_ACTION_FOR_THIS_PROPOSAL_IS_ALREADY_ASSIGNED_TO_ANOTHER_CHECKER_MSG);
			}
			else //  check proposal is not assigned or current user has limit for this proposal or not
			{
				userResponse = usersClient.getMinMaxAmount(userRequest);

				CheckerDetailRequest checkerDetailRequest = null;
				if (userResponse != null && !CommonUtils.isObjectListNull(userResponse)
						&& !(CommonUtils.isObjectNullOrEmpty(userResponse.getData()))) {
					checkerDetailRequest = MultipleJSONObjectHelper.getObjectFromMap(
							(LinkedHashMap<String, Object>) userResponse.getData(), CheckerDetailRequest.class);
				}

				if (!CommonUtils.isObjectNullOrEmpty(checkerDetailRequest)) {
					// "+checkerDetailRequest.getMinAmount() + " getMaxAmount :
					// "+checkerDetailRequest.getMaxAmount());
					if (userRequest.getLoanAmount() != null && checkerDetailRequest != null && checkerDetailRequest.getMinAmount() != null
							&& checkerDetailRequest.getMaxAmount() != null
							&& !(userRequest.getLoanAmount() >= checkerDetailRequest.getMinAmount()
							&& userRequest.getLoanAmount() <= checkerDetailRequest.getMaxAmount())) {
						loansResponse.setFlag(false);
						loansResponse.setMessage(YOU_DO_NOT_HAVE_RIGHTS_TO_TAKE_ACTION_FOR_THIS_PROPOSAL_ASSIGN_PROPOAL_TO_UPPER_LEVEL_CHECKER_MSG);
					}
				} else {
					// You dont have Authorised for this Action
					loansResponse.setFlag(false);
					loansResponse.setMessage(YOU_DO_NOT_HAVE_RIGHTS_TO_TAKE_ACTION_FOR_THIS_PROPOSAL_ASSIGN_PROPOAL_TO_UPPER_LEVEL_CHECKER_MSG);
				}

			}
		} catch (Exception e) {

			logger.error("Error while Getting Min Max Loan Amount : ",e);
			loansResponse.setFlag(false);
			loansResponse.setMessage(CommonUtils.SOMETHING_WENT_WRONG);
		}
		return loansResponse;
	}

	@Override
	public Boolean checkAvailabilityForBankSelection(Long applicationId, Integer businessTypeId) {
		return checkMainLogicForMultiBankSelection(applicationId,businessTypeId,null);
	}

	@Override
	public Boolean checkMainLogicForMultiBankSelection(Long applicationId, Integer businessTypeId,List<ConnectRequest> filteredAppListList) {
		try {
			ProposalDetails ineligibleProposalDetails = proposalDetailRepository.getSanctionedByApplicationId(applicationId);
			if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails)){
				return Boolean.FALSE;
			}
			List<ProposalDetails> proposalDetailsList = proposalDetailRepository.findByApplicationIdAndIsActiveForMBOnline(applicationId,true,false);
			List<ProposalMappingRequest> inActivityProposalList = new ArrayList<ProposalMappingRequest>();
			for (int i = 0; i < proposalDetailsList.size(); i++) {
				ProposalDetails proposalDetail = proposalDetailsList.get(i);
				if(proposalDetail.getProposalStatusId().getId()== MatchConstant.ProposalStatus.ACCEPT
						|| proposalDetail.getProposalStatusId().getId()==MatchConstant.ProposalStatus.HOLD
						|| proposalDetail.getProposalStatusId().getId()==MatchConstant.ProposalStatus.DECLINE
						|| proposalDetail.getProposalStatusId().getId()== MatchConstant.ProposalStatus.CANCELED){
					ProposalMappingRequest proposalMappingRequest = new ProposalMappingRequest();
					BeanUtils.copyProperties(proposalDetail, proposalMappingRequest);
					inActivityProposalList.add(proposalMappingRequest);
				}else {
					inActivityProposalList = null;
					break;
				}
			}

			if(proposalDetailsList != null && proposalDetailsList.size() == 0){//for offline cases
				return checkLogicForOfflineMultiBankSelection(applicationId,proposalDetailsList,filteredAppListList);
			}
			if(inActivityProposalList != null && !inActivityProposalList.isEmpty()){
				ConnectRequest connectRequest = new ConnectRequest();
				connectRequest.setApplicationId(applicationId);
				connectRequest.setBusinessTypeId(businessTypeId);
				int connectListSize=0, days = 0;
				ConnectRequest connectRequest1 = new ConnectRequest();
				ConnectResponse connectResponse = new ConnectResponse();
				if(!CommonUtils.isListNullOrEmpty(filteredAppListList)){
					connectListSize = filteredAppListList.size();
					connectRequest1 = filteredAppListList.get(0);
				}else {
					connectResponse = connectClient.getApplicationList(applicationId);
					if(!CommonUtils.isObjectNullOrEmpty(connectResponse) && !CommonUtils.isListNullOrEmpty(connectResponse.getDataList())){
						connectListSize = connectResponse.getDataList().size();
						connectRequest1 = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponse.getDataList().get(0),ConnectRequest.class);
					}
				}
				if(!CommonUtils.isObjectNullOrEmpty(connectRequest1) && CommonUtils.isObjectNullOrEmpty(connectRequest1.getOrgId())){
					//ConnectRequest connectRequest1 = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponse.getDataList().get(0),ConnectRequest.class);
					if(!CommonUtils.isObjectNullOrEmpty(connectRequest1.getInPrincipleDate())){
						days = Days.daysBetween(new LocalDate(connectRequest1.getInPrincipleDate()),
								new LocalDate(new Date())).getDays();
					}else{
						days = Days.daysBetween(new LocalDate(connectRequest1.getModifiedDate()),
								new LocalDate(new Date())).getDays();
					}
					String maxDays = loanRepository.getCommonPropertiesValue("MAX_DAYS_RECALCULATION");
					if(connectRequest1.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId() &&
							days> Integer.parseInt(maxDays)){//take 22 from application.properties file
						return Boolean.FALSE;
					}else{
						
						String daysDiff = loanRepository.getCommonPropertiesValue("DAYS_DIFF_RECALCULATION");
						if(inActivityProposalList.size()<3 && connectListSize ==1) {
							if(connectRequest1.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId() &&
									days >= Integer.parseInt(daysDiff)) {//take 7 from application.properties file
								return Boolean.TRUE;
							}
						}else if(inActivityProposalList.size()<3 ){ //&& (connectListSize > 1 && connectListSize < 3)){
							if(connectListSize > 1 && connectListSize < 3){
								ConnectRequest connectReqObj = new ConnectRequest();
								if(!CommonUtils.isListNullOrEmpty(filteredAppListList)){
									connectReqObj =	filteredAppListList.get(connectListSize-1);
								}else {
									if(!CommonUtils.isObjectNullOrEmpty(connectResponse) && !CommonUtils.isListNullOrEmpty(connectResponse.getDataList())){
										connectReqObj = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponse.getDataList().get(connectListSize-1),ConnectRequest.class);
									}
								}
								if(!CommonUtils.isObjectNullOrEmpty(connectReqObj.getInPrincipleDate())){
									days = Days.daysBetween(new LocalDate(connectReqObj.getInPrincipleDate()),
											new LocalDate(new Date())).getDays();
								}else{
									days = Days.daysBetween(new LocalDate(connectReqObj.getModifiedDate()),
											new LocalDate(new Date())).getDays();
								}
								if(connectReqObj.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId() &&
										days >= Integer.parseInt(daysDiff)){//take 7 from application.properties file
									return Boolean.TRUE;
								}else {
									return Boolean.FALSE;
								}
							}else if(connectListSize >= 3){
								//List<ConnectRequest> connectRequestList = new ArrayList<>(connectListSize);
								int ineligibleCnt = 0,eligibleCnt=0,totalAttempt=0;
								for (int j = 0; j < connectListSize; j++) {

									ConnectRequest connectReq = null;
									if(!CommonUtils.isListNullOrEmpty(filteredAppListList)){
										connectReq = filteredAppListList.get(j);
									}else{
										connectReq = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponse.getDataList().get(j),ConnectRequest.class);
									}
									if((connectReq.getStageId().equals(4) || connectReq.getStageId().equals(207)) && connectReq.getStatus().equals(6)){
										ineligibleCnt++;
									}else if((connectReq.getStageId().equals(9) || connectReq.getStageId().equals(7) || connectReq.getStageId().equals(210) || connectReq.getStageId().equals(211)) && connectReq.getStatus().equals(3)){
										eligibleCnt++;
									}
									if(ineligibleCnt>0 && eligibleCnt == 0)
										ineligibleCnt = 1;
									//connectRequestList.add(connectReq);
								}
								totalAttempt = ineligibleCnt + eligibleCnt;
								if(totalAttempt < 3){
									ConnectRequest connectReqObj = new ConnectRequest();
									if(!CommonUtils.isListNullOrEmpty(filteredAppListList)){
										connectReqObj =	filteredAppListList.get(connectListSize-1);
									}else {
										if(!CommonUtils.isObjectNullOrEmpty(connectResponse) && !CommonUtils.isListNullOrEmpty(connectResponse.getDataList())){
											connectReqObj = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponse.getDataList().get(connectListSize-1),ConnectRequest.class);
										}
									}
									if(!CommonUtils.isObjectNullOrEmpty(connectReqObj.getInPrincipleDate())){
										days = Days.daysBetween(new LocalDate(connectReqObj.getInPrincipleDate()),
												new LocalDate(new Date())).getDays();
									}else{
										days = Days.daysBetween(new LocalDate(connectReqObj.getModifiedDate()),
												new LocalDate(new Date())).getDays();
									}
									if(connectReqObj.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId() &&
											eligibleCnt>=1 && days >= Integer.parseInt(daysDiff)){//take 7 from application.properties file
										return Boolean.TRUE;
									}else {
										return Boolean.FALSE;
									}
								}else {
									return Boolean.FALSE;
								}
							}
						}else {
							return Boolean.FALSE;
						}
					}
				}
			}
			return Boolean.FALSE;
		} catch (IOException io) {
			logger.error("Error while checking availability for bank selection...! == {}",applicationId," - ",io);
		} catch (Exception e) {
			logger.error("Error while checking availability for bank selection...! == {}",applicationId," - ",e);
		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean checkLogicForOfflineMultiBankSelection(Long applicationId,List<ProposalDetails> proposalDetailsList,List<ConnectRequest> filteredAppListList) {
		try {
			int days = 0,connectListSize = 0;
			if(proposalDetailsList.size() == 0){//for offline cases
				ConnectRequest connectRequestOffline = new ConnectRequest();
				ConnectResponse connectResponseOffline = new ConnectResponse();
				if(!CommonUtils.isObjectNullOrEmpty(filteredAppListList)){
					connectListSize = filteredAppListList.size();
					connectRequestOffline = filteredAppListList.get(0);
				}else{
					connectResponseOffline = connectClient.getApplicationList(applicationId);
					if(!CommonUtils.isObjectNullOrEmpty(connectResponseOffline) && !CommonUtils.isListNullOrEmpty(connectResponseOffline.getDataList())){
						connectListSize = connectResponseOffline.getDataList().size();
						connectRequestOffline = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponseOffline.getDataList().get(0),ConnectRequest.class);
					}
				}
				//ConnectRequest connectRequestOffline = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponseOffline.getDataList().get(0),ConnectRequest.class);
				if(!CommonUtils.isObjectNullOrEmpty(connectRequestOffline) && CommonUtils.isObjectNullOrEmpty(connectRequestOffline.getOrgId())){
					String maxDaysForOffline = loanRepository.getCommonPropertiesValue("MAX_DAYS_RECALCULATION_OFFLINE");
					
					days = Days.daysBetween(new LocalDate(connectRequestOffline.getModifiedDate()),
							new LocalDate(new Date())).getDays();
					if(connectRequestOffline.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId() &&
							days > Integer.parseInt(maxDaysForOffline)){
						return Boolean.FALSE;
					}else {
						//int offlineResponseListSize = connectResponseOffline.getDataList().size();
						ConnectRequest connectReqObj = new ConnectRequest();
						if(connectListSize == 1){
							connectReqObj = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponseOffline.getDataList().get(connectListSize-1),ConnectRequest.class);
							days = Days.daysBetween(new LocalDate(connectReqObj.getModifiedDate()),
									new LocalDate(new Date())).getDays();
							
							String daysIntervalForOffline = loanRepository.getCommonPropertiesValue("INTERVAL_DAYS_RECALCULATION_OFFLINE");
							if(connectReqObj.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId() &&
									days >= Integer.parseInt(daysIntervalForOffline)) {//take 1 from application.properties file
								return Boolean.TRUE;
								
							}
						}else if(connectListSize > 1){
							connectReqObj = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponseOffline.getDataList().get(connectListSize-1),ConnectRequest.class);
							days = Days.daysBetween(new LocalDate(connectReqObj.getModifiedDate()),
									new LocalDate(new Date())).getDays();
							String startIntervalForOffline = loanRepository.getCommonPropertiesValue("INTERVAL_START_RECALCULATION_OFFLINE");
							if(connectReqObj.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId() &&
									days >= Integer.parseInt(startIntervalForOffline)) {//take 15 from application.properties file
								return Boolean.TRUE;
							}
						}
					}
				}
				return Boolean.FALSE;
			}
			return Boolean.FALSE;
		} catch (IOException io) {
			logger.error("Error while checking availability for bank selection...! = {}",io);
		} catch (Exception e) {
			logger.error("Error while checking availability for bank selection...! = {}",e);
		}
		return Boolean.FALSE;
	}

	@Override
	public List<SchedulerDataMultipleBankRequest> getApplicationListForMultipleBank(){
		logger.info("entry in getApplicationListForMultipleBank()");
		try {
			//call connect client to get application list
			ConnectResponse connectResponse = connectClient.getInPrincipleApplicationList();
			List<ConnectRequest> connectRequestList = new ArrayList<>();
			if(!CommonUtils.isObjectNullOrEmpty(connectResponse) && !CommonUtils.isListNullOrEmpty(connectResponse.getDataList())) {
				for (int i=0; i<connectResponse.getDataList().size(); i++) {
					ConnectRequest connectRequest = MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponse.getDataList().get(i), ConnectRequest.class);
					connectRequestList.add(connectRequest);
				}
			}
			List<SchedulerDataMultipleBankRequest> schedulerDataMultipleBankRequestList = new ArrayList<>();
			if(!CommonUtils.isListNullOrEmpty(connectRequestList)) {
				for (int i=0; i<connectRequestList.size(); i++){
					ConnectRequest connectRequest = connectRequestList.get(i); //MultipleJSONObjectHelper.getObjectFromMap((LinkedHashMap<String, Object>) connectResponse.getDataList().get(i), ConnectRequest.class);

					List<ConnectRequest> filteredAppListList = connectRequestList.stream()
							.filter(connectReq -> connectRequest.getApplicationId().equals(connectReq.getApplicationId()))
							.collect(Collectors.toList());

					Boolean isMultiBankAllowed = checkMainLogicForMultiBankSelection(connectRequest.getApplicationId(),connectRequest.getBusinessTypeId(),filteredAppListList);
					if(isMultiBankAllowed){
						ConnectRequest connectRequest1 = filteredAppListList.get(filteredAppListList.size()-1);
						SchedulerDataMultipleBankRequest schedulerDataMultipleBankRequest = new SchedulerDataMultipleBankRequest();
						schedulerDataMultipleBankRequest.setUserId(connectRequest1.getUserId());
						schedulerDataMultipleBankRequest.setApplicationId(connectRequest1.getApplicationId());
						if((connectRequest1.getStageId().equals(4) || connectRequest1.getStageId().equals(207)) && connectRequest1.getStatus().equals(6)){
							schedulerDataMultipleBankRequest.setInpricipleDate(connectRequest1.getModifiedDate());
							if(connectRequest1.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId()){
								String daysIntervalForOffline = loanRepository.getCommonPropertiesValue("INTERVAL_DAYS_RECALCULATION_OFFLINE");
								schedulerDataMultipleBankRequest.setDayDiffrence(Integer.parseInt(daysIntervalForOffline));
							}
							//schedulerDataMultipleBankRequest.setDayDiffrence(Integer.parseInt(daysIntervalForOffline));
							//set offline
							schedulerDataMultipleBankRequest.setEmailType(2);//NotificationApiUtils.ApplicationType.Offline.getId());
							logger.info("appId:"+connectRequest1.getApplicationId());
								IneligibleProposalDetails ineligibleProposalDetails = ineligibleProposalDetailsRepository.findByApplicationIdAndIsActive(connectRequest1.getApplicationId(),true);
							if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails)
									&& !CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getUserOrgId())){
								schedulerDataMultipleBankRequest.setOrgId(ineligibleProposalDetails.getUserOrgId());
								logger.info("userOrgId:"+ineligibleProposalDetails.getUserOrgId());
							}else {
								continue;
							}
						}else{
							schedulerDataMultipleBankRequest.setProposalId(connectRequest1.getProposalId());
							if(!CommonUtils.isObjectNullOrEmpty(connectRequest1.getInPrincipleDate())){
								schedulerDataMultipleBankRequest.setInpricipleDate(connectRequest1.getInPrincipleDate());
							}else{
								schedulerDataMultipleBankRequest.setInpricipleDate(connectRequest1.getModifiedDate());
							}
							if(connectRequest1.getBusinessTypeId() == BusinessType.CVL_MUDRA_LOAN.getId()){
								String daysDiff = loanRepository.getCommonPropertiesValue("DAYS_DIFF_RECALCULATION");
								schedulerDataMultipleBankRequest.setDayDiffrence(Integer.parseInt(daysDiff));
							}
							
							//schedulerDataMultipleBankRequest.setDayDiffrence(Integer.parseInt(daysDiff));
							//set online
							schedulerDataMultipleBankRequest.setEmailType(1);//NotificationApiUtils.ApplicationType.Online.getId());
						}
						Long userOrgId = proposalDetailRepository.getOrgIdByProposalId(connectRequest1.getProposalId());
						if(!CommonUtils.isObjectNullOrEmpty(userOrgId)){
							schedulerDataMultipleBankRequest.setOrgId(userOrgId);
						}
						boolean idExists = schedulerDataMultipleBankRequestList.stream()
									.anyMatch(t -> t.getApplicationId().equals(connectRequest1.getApplicationId()));
						//logger.info("exist:"+idExists);
						if(!idExists){
							schedulerDataMultipleBankRequestList.add(schedulerDataMultipleBankRequest);
						}
					}
				}
			}
			logger.info("exit from getApplicationListForMultipleBank()");
			return schedulerDataMultipleBankRequestList;
		}catch (ConnectException e){
			logger.error("error in getApplicationListForMultipleBank()",e);
		}catch (Exception e){
			logger.error("error in getApplicationListForMultipleBank()",e);
		}
		return null;
	}

	/*public Boolean calculateAndGetNewMatches(Long applicationId, Integer businessTypeId){
		try {
			ConnectRequest connectRequest = new ConnectRequest();
			connectRequest.setApplicationId(applicationId);
			connectRequest.setBusinessTypeId(businessTypeId);
			connectClient.createForMultipleBank(connectRequest);

			//logger.info("=============> <=============");
			return Boolean.FALSE;
		} catch (MatchException e) {
			// TODO Auto-generated catch block
			logger.error("Error while checking availability for bank selection...!");
			e.printStackTrace();
		} catch (IOException io) {
			// TODO Auto-generated catch block
			logger.error("Error while checking availability for bank selection...!");
			io.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error while checking availability for bank selection...!");
			e.printStackTrace();
		}
		return Boolean.FALSE;
	}*/
	@Override
	public List<ProposalDetailsAdminRequest> getProposalsByOrgId(Long userOrgId, ProposalDetailsAdminRequest request,
			Long userId) {

		/* UserResponse userData = usersClient.getUserDetailsById(userId);

		   UsersRequest data = (UsersRequest) userData.getData();

		   userData.toString());

		   Long roleId = (Long) userData.get("roleId"); */
		List<Object[]> result;

		/* if(UsersRoles.HO.equals(roleId)) {
		 result =
		 proposalDetailRepository.getProposalDetailsByOrgId(userOrgId,
		 request.getFromDate(), request.getToDate());
		 } else if(UsersRoles.BO.equals(roleId)) { */
		result = proposalDetailRepository.getProposalDetailsByOrgId(userOrgId, request.getFromDate(),
				request.getToDate());
		// }

		List<ProposalDetailsAdminRequest> responseList = new ArrayList<>(result.size());

		for (Object[] obj : result) {
			ProposalDetailsAdminRequest proposal = new ProposalDetailsAdminRequest();
			proposal.setApplicationId(CommonUtils.convertLong(obj[0]));
			proposal.setUserId(CommonUtils.convertLong(obj[1]));
			proposal.setUserName(CommonUtils.convertString(obj[2]));
			proposal.setEmail(CommonUtils.convertString(obj[3]));
			proposal.setMobile(CommonUtils.convertString(obj[4]));
			proposal.setCreatedDate(CommonUtils.convertDate(obj[5]));
			proposal.setBranchId(CommonUtils.convertLong(obj[6]));
			proposal.setLoanAmount(CommonUtils.convertString(obj[7]));
			proposal.setTenure(CommonUtils.convertString(obj[8]));
			proposal.setRate(CommonUtils.convertString(obj[9]));
			proposal.setEmi(CommonUtils.convertDouble(obj[10]));
			proposal.setProcessingFee(CommonUtils.convertDouble(obj[11]));
			proposal.setBranchName(CommonUtils.convertString(obj[12]));
			proposal.setContactPersonName(CommonUtils.convertString(obj[13]));
			proposal.setTelephoneNo(CommonUtils.convertString(obj[14]));
			proposal.setContactPersonNumber(CommonUtils.convertString(obj[15]));
			proposal.setOrganizationName(CommonUtils.convertString(obj[16]));
			proposal.setApplicationCode(CommonUtils.convertString(obj[17]));
			proposal.setCode(CommonUtils.convertString(obj[18]));
			proposal.setStreetName(CommonUtils.convertString(obj[19]));
			proposal.setState(CommonUtils.convertString(obj[20]));
			proposal.setCity(CommonUtils.convertString(obj[21]));
			proposal.setPremisesNo(CommonUtils.convertString(obj[22]));
			proposal.setFpProductId(CommonUtils.convertLong(obj[23]));
			proposal.setContactPersonEmail(CommonUtils.convertString(obj[24]));
			proposal.setIsCampaignCustomer(CommonUtils.convertLong(obj[25]) > 0);
			proposal.setGstin(CommonUtils.convertString(obj[26]));

			responseList.add(proposal);
		}

		return responseList;
	}

	@Autowired
	private OfflineProcessedAppRepository offlineProcessedAppRepository;

	@Override
	public List<Object[]> getHomeCounterDetail() {
		logger.info(
				"========== Enter in getHomeCounter()  gettting no of fp , fs and total inprinciple and inprinciple amount======== ");
		List<Object[]> object = offlineProcessedAppRepository.getHomeCounterDetail();
		logger.info("========== Exit from  getHomeCounter() ======== " + object);
		return object;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<?> basicInfoForSearch(ProposalMappingRequest request) {
        UserResponse userResponse=null;
        BranchBasicDetailsRequest basicDetailsRequest=null;
		try {
			// set branch id to proposal request
			UsersRequest usersRequest = new UsersRequest();
			usersRequest.setId(request.getUserId());
			logger.info(CURRENT_USER_ID_MSG + request.getUserId());
			userResponse = usersClient.getBranchDetailsBYUserId(usersRequest);
			basicDetailsRequest = MultipleJSONObjectHelper.getObjectFromMap(
					(LinkedHashMap<String, Object>) userResponse.getData(), BranchBasicDetailsRequest.class);
			if (!CommonUtils.isObjectNullOrEmpty(basicDetailsRequest)) {
				logger.info(FOUND_BRANCH_ID_MSG + basicDetailsRequest.getId()
						+ ROLE_ID_MSG + basicDetailsRequest.getRoleId());
				if (basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.BO
						|| basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.FP_CHECKER) {
					logger.info("Current user is Branch officer or FP_CHECKER");
					request.setBranchId(basicDetailsRequest.getId());
				}
			} else {
				logger.info(BRANCH_ID_CAN_NOT_BE_FOUND_MSG);
			}
		} catch (Exception e) {
			logger.error(THROW_EXCEPTION_WHILE_GET_BRANCH_ID_FROM_USER_ID_MSG,e);
		}

        List<Object[]> result = null;
		if(basicDetailsRequest != null && basicDetailsRequest.getRoleId() == CommonUtils.UsersRoles.SMECC)
        {
            result = proposalDetailRepository.getAllProposalsForSearchWithBranch(request.getFpProductId(), request.getProposalStatusId(), userResponse.getBranchList());
        }
        else
        {
			if(request.getBranchId() != null){
				if ((!CommonUtils.isObjectNullOrEmpty(request.getIsSanctionByOtherBankReq())) && request.getIsSanctionByOtherBankReq())
					result = proposalDetailRepository.getSanctionByOtherBankProposalsForSearchWithBranch(request.getFpProductId(), request.getBranchId(), request.getUserOrgId());
				else
					result = proposalDetailRepository.getAllProposalsForSearchWithBranch(request.getFpProductId(), request.getProposalStatusId(), request.getBranchId());
			}else{

				if ((!CommonUtils.isObjectNullOrEmpty(request.getIsSanctionByOtherBankReq())) && request.getIsSanctionByOtherBankReq())
					result = proposalDetailRepository.getAllSanctionByOtherBankProposalsForSearch(request.getFpProductId(), request.getProposalStatusId());
				else
					result = proposalDetailRepository.getAllProposalsForSearch(request.getFpProductId(), request.getProposalStatusId());
			}
        }


		List<Map<String, Object>> finalList = new ArrayList<>(result.size());

		for(Object[] arr : result ){
			Map<String, Object> data = new HashMap<>();
			data.put("applicationId", arr[0]);
			data.put("organizationName", arr[1]);
			data.put("applicationCode", arr[2]);
			data.put("businessTypeId", CommonUtils.isObjectNullOrEmpty(arr[3]) ? BusinessType.EXISTING_BUSINESS.getId() : arr[3]);
			finalList.add(data);
		}
		result.clear();
		return finalList;
	}

	public List<ProposalSearchResponse> searchProposalByAppCode(Long loginUserId,Long loginOrgId,ReportRequest reportRequest,Long businessTypeId) {
		
		Object[] loggedUserDetailsList = loanRepository.getRoleIdAndBranchIdByUserId(loginUserId);
		Long roleId = CommonUtils.convertLong(loggedUserDetailsList[0]);
		//logger.info("Enter in Search Proposal Service ---------------------------->Role Id --------> " + roleId);
		Long branchId = CommonUtils.convertLong(loggedUserDetailsList[1]);
		if(CommonUtils.isObjectNullOrEmpty(roleId)) {
			return Collections.emptyList();
		}
		if (roleId == CommonUtils.UsersRoles.FP_CHECKER || roleId == CommonUtils.UsersRoles.SMECC || roleId == CommonUtils.UsersRoles.HO
				 || roleId == CommonUtils.UsersRoles.ZO || roleId == CommonUtils.UsersRoles.RO) {
			logger.info("GLOBAL SEARCH CALL SP -----> ORG ID --------> " + loginOrgId + "------UserId-----" + loginUserId + "------Value-----" + reportRequest.getValue() + "------Number-----" + reportRequest.getNumber() + "------businessTypeId-----" + businessTypeId + "------branchId-----" + branchId);
			List<Object[]> objList = loanRepository.getSerachProposalListByRoleSP(loginOrgId, reportRequest.getValue(), loginUserId, reportRequest.getNumber().longValue(), businessTypeId, branchId);
			if (objList.size() > 0) {
				return setValue(objList, true);
			}
		}
		return Collections.emptyList();
	}


	private List<ProposalSearchResponse> setValue(List<Object[]> objList,boolean setBranch) {
		List<ProposalSearchResponse> responseList = new ArrayList<>();
		ProposalSearchResponse response = null;
		for(Object[] obj : objList) {
			response = new ProposalSearchResponse();
			response.setApplicationId(CommonUtils.convertLong(obj[0]));
			response.setProposalId(CommonUtils.convertLong(obj[1]));
			response.setFpProductId(CommonUtils.convertLong(obj[2]));
			response.setApplicationCode(CommonUtils.convertString(obj[3]));
			response.setOrgName(CommonUtils.convertString(obj[4]));
			response.setElAmount(CommonUtils.convertDouble(obj[5]));
			response.setProductName(CommonUtils.convertString(obj[6]));
			response.setCreatedDate(CommonUtils.convertDate(obj[7]));
			response.setBusinessTypeId(CommonUtils.convertInteger(obj[8]));
			//if(response.getBusinessTypeId() == 3 || response.getBusinessTypeId() == 5 ) {
			response.setApplicantName(CommonUtils.convertString(obj[11]));
			//}
			response.setProposalStatusId(CommonUtils.convertLong(obj[9]));
			response.setProductId(CommonUtils.convertInteger(obj[10]));
			if(setBranch) {
				response.setBranchName(CommonUtils.convertString(obj[12]));
				response.setBranchCode(CommonUtils.convertString(obj[13]));
			}
			Integer count = CommonUtils.convertInteger(obj[14]);
			if(!CommonUtils.isObjectNullOrEmpty(count) && count > 0) {
				response.setIsSactionedFromOther(true);
			} else {
				response.setIsSactionedFromOther(false);
			}
			responseList.add(response);
		}
		return responseList;
	}

	public Map<String , Double> getFpDashBoardCount(Long loginUserId,Long loginOrgId,Long businessTypeId) {
		Object[] loggedUserDetailsList = loanRepository.getRoleIdAndBranchIdByUserId(loginUserId);
		Long roleId = CommonUtils.convertLong(loggedUserDetailsList[0]);
		Long branchId = CommonUtils.convertLong(loggedUserDetailsList[1]);
		if(CommonUtils.isObjectNullOrEmpty(roleId)) {
			return null;
		}
		Object[] count = null;
		count = loanRepository.fetchFpDashbordCountByRoleSP(loginOrgId, loginUserId,businessTypeId,branchId);
		if(count != null) {
			Map<String , Double> map = new HashMap<>();
			map.put("inPrincipleCount", CommonUtils.convertDouble(count[0]));
			map.put("holdBeforeCount", CommonUtils.convertDouble(count[1]));
			map.put("holdAfterCount", CommonUtils.convertDouble(count[2]));
			map.put("rejectBeforeCount", CommonUtils.convertDouble(count[3]));
			map.put("rejectAfterCount", CommonUtils.convertDouble(count[4]));
			map.put("sanctionedCount", CommonUtils.convertDouble(count[5]));
			map.put("disbursmentCount", CommonUtils.convertDouble(count[6]));
			return map;
		}
		return null;
	}

	@Override
	public Integer updateStatus(Long applicationId, Long fpProductId, Long status,String remarks) {
		return proposalDetailRepository.updateStatus(status, applicationId, fpProductId,remarks);
	}

	@Override
	public ProposalMappingResponse getProposalId(ProposalMappingRequest request) {
		ProposalMappingResponse response = new ProposalMappingResponse();
		ProposalMappingRequest proposalMappingRequest=null;
		try {
			response = proposalDetailsClient.getProposal(request);

			proposalMappingRequest = (ProposalMappingRequest) MultipleJSONObjectHelper.getObjectFromMap(
					(Map<String, Object>) response.getData(), ProposalMappingRequest.class);

			ProposalDetails proposalDetails = proposalDetailRepository.getProposalId(request.getApplicationId());

			Boolean isButtonDisplay=true;
			String messageOfButton=null;
			if(!CommonUtils.isObjectNullOrEmpty(proposalDetails))
			{
				if(!proposalDetails.getUserOrgId().toString().equals(request.getUserOrgId().toString()))
				{
					if(ProposalStatus.APPROVED ==  proposalDetails.getProposalStatusId().getId())
						messageOfButton="This proposal has been Sanctioned by Other Bank.";
					else if(ProposalStatus.DISBURSED ==  proposalDetails.getProposalStatusId().getId())
						messageOfButton="This proposal has been Disbursed by Other Bank.";
					else if(ProposalStatus.PARTIALLY_DISBURSED ==  proposalDetails.getProposalStatusId().getId())
						messageOfButton="This proposal has been Partially Disbursed by Other Bank.";
					isButtonDisplay=false;

					proposalMappingRequest.setMessageOfButton(messageOfButton);
					proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
				}
				else
				{
					proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
				}
			}
			else
			{
				IneligibleProposalDetails ineligibleProposalDetails = ineligibleProposalDetailsRepository.getSanctionedByApplicationId(request.getApplicationId());
				if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails)){
					if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getUserOrgId())
							&& !ineligibleProposalDetails.getUserOrgId().equals(request.getUserOrgId().toString())){
						if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getIsDisbursed()) && ineligibleProposalDetails.getIsDisbursed() == true)
							messageOfButton="This proposal has been Disbursed by Other Bank.";
						else if(!CommonUtils.isObjectNullOrEmpty(ineligibleProposalDetails.getIsSanctioned()) && ineligibleProposalDetails.getIsSanctioned() == true)
							messageOfButton="This proposal has been Sanctioned by Other Bank.";
						isButtonDisplay=false;

						proposalMappingRequest.setMessageOfButton(messageOfButton);
						proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
					}else{
						proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
					}
				}
				proposalMappingRequest.setIsButtonDisplay(isButtonDisplay);
			}
			response.setData(proposalMappingRequest);
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}

		return response;
	}

	@Override
	public String getDayDiffrenceForInprinciple(Integer loanType) {
		if(loanType == LoanType.WORKING_CAPITAL.getValue() ||loanType ==  LoanType.WCTL_LOAN.getValue() || loanType ==  LoanType.TERM_LOAN.getValue()) {
			return loanRepository.getCommonPropertiesValue("DAYS_DIFF_RECALCULATION");
		}
		return null;
	}

	@Override
	public ProposalMappingResponse getDisbursementRequestDetails(DisbursementRequestModel request) {
		logger.info("DISBURSEMENT request DETAILS IS ---------------------------------------------------> " + request.toString());
		if (CommonUtils.isObjectNullOrEmpty(request)) {
			return new ProposalMappingResponse("Invalid request",
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		try {
			return proposalDetailsClient.getDisbursementRequestDetails(request);
		} catch (MatchException e) {
			logger.error(CommonUtils.EXCEPTION,e);
		}
		return null;
	}

	@Override
	public ProposalMappingResponse saveDisbursementRequestDetails(MultipartFile[] multipartFiles, String userRequestString) {
		logger.info("saving DISBURSEMENT request DETAILS IS ---------------------------------------------------> " + userRequestString.toString());

		try {
			if (CommonUtils.isObjectNullOrEmpty(userRequestString)) {
				return new ProposalMappingResponse("Invalid request",
						HttpStatus.INTERNAL_SERVER_ERROR.value());
			}

			DisbursementRequestModel disbursementRequestModel = MultipleJSONObjectHelper.getObjectFromString(userRequestString, DisbursementRequestModel.class);
			if (com.opl.mudra.api.loans.utils.CommonUtils.isObjectNullOrEmpty(disbursementRequestModel) || com.opl.mudra.api.matchengine.utils.CommonUtils.isObjectNullOrEmpty(multipartFiles)) {
				return new ProposalMappingResponse("Error while uploading documents", HttpStatus.BAD_REQUEST.value());
			}
			try {
				return proposalDetailsClient.saveRequestDisbursementDetails(disbursementRequestModel);
			} catch (Exception e) {
				logger.error(CommonUtils.EXCEPTION, e);
			}
		}catch (Exception e){
			logger.error("Error while saving disbursement request");
		}
		return null;
	}

	private String uploadImageForMfi(MultipartFile multipartFile, Long userId, Integer productDocMappingId) {
		org.json.simple.JSONObject jsonObj = new org.json.simple.JSONObject();
		jsonObj.put("applicationId", userId);
		jsonObj.put("productDocumentMappingId", productDocMappingId);// this is productmappingid 593 for save in amazon
		// s3
		jsonObj.put("userType", DocumentAlias.UERT_TYPE_APPLICANT);
		jsonObj.put("originalFileName", multipartFile.getOriginalFilename());
		try {
			DocumentResponse documentResponse = dmsClient.uploadFile(jsonObj.toString(), multipartFile);
			logger.info("response {}", documentResponse.getStatus());
			StorageDetailsResponse response = null;
			Map<String, Object> list = (Map<String, Object>) documentResponse.getData();
			if (!com.opl.mudra.api.loans.utils.CommonUtils.isObjectListNull(list)) {
				try {
					response = com.opl.mudra.api.scoring.utils.MultipleJSONObjectHelper.getObjectFromMap(list, StorageDetailsResponse.class);
				} catch (IOException e) {
					logger.error("IO exception while upload file on DMS",e);
				}
			}

			if (response != null) {
				logger.debug("upload pre disbursedment () :: response is not null");
				if (!com.opl.mudra.api.loans.utils.CommonUtils.isObjectNullOrEmpty(response.getFilePath())) {
					return response.getId().toString();
				} else {
					logger.debug("uploadImageForMfi() :: error while upload Files response not 200");
					return null;
				}
			} else {
				logger.debug("uploadImageForMfi() :: response is null");
				return null;
			}
		} catch (DocumentException e) {
			logger.error("Document exception while upload file on DMS",e);
			return null;
		}
	}

}

