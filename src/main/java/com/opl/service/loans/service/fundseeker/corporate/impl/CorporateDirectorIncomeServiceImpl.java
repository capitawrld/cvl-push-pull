package com.opl.service.loans.service.fundseeker.corporate.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.PincodeDataResponse;
import com.opl.mudra.api.loans.model.corporate.CorporateDirectorIncomeRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.oneform.enums.DirectorRelationshipType;
import com.opl.mudra.api.oneform.enums.EducationQualificationNTB;
import com.opl.mudra.api.oneform.enums.EmploymentStatusNTB;
import com.opl.mudra.api.oneform.enums.EmploymentWithNTB;
import com.opl.mudra.api.oneform.enums.Gender;
import com.opl.mudra.api.oneform.enums.MaritalStatus;
import com.opl.mudra.api.oneform.enums.OccupationNatureNTB;
import com.opl.mudra.api.oneform.enums.ResidenceStatusRetailMst;
import com.opl.service.loans.domain.fundseeker.corporate.CorporateDirectorIncomeDetails;
import com.opl.service.loans.domain.fundseeker.corporate.DirectorBackgroundDetail;
import com.opl.service.loans.repository.fundseeker.corporate.CorporateDirectorIncomeDetailsRepository;
import com.opl.service.loans.repository.fundseeker.corporate.DirectorBackgroundDetailsRepository;
import com.opl.service.loans.service.common.PincodeDateService;
import com.opl.service.loans.service.fundseeker.corporate.CorporateDirectorIncomeService;

@Service
@Transactional
public class CorporateDirectorIncomeServiceImpl implements CorporateDirectorIncomeService {

	
	@Autowired
	private CorporateDirectorIncomeDetailsRepository incomeDetailsRepository;
	
	@Autowired
	private DirectorBackgroundDetailsRepository backgroundDetailsRepository;
	
	@Autowired
    private PincodeDateService pincodeDateService;
	
	private static final Logger logger = LoggerFactory.getLogger(CorporateDirectorIncomeServiceImpl.class.getName());
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public Boolean saveOrUpdateIncomeDetails(List<CorporateDirectorIncomeRequest> corporateRequest) throws LoansException {

		try {
			logger.info("Entering into saveOrUpdateDirectorIncomeDetails=======================>");
			for(CorporateDirectorIncomeRequest corpoObj : corporateRequest) {
				if(CommonUtils.isObjectNullOrEmpty(corpoObj)) {
					continue;
				}
				if(CommonUtils.isObjectNullOrEmpty(corpoObj.getApplicationId()) || CommonUtils.isObjectNullOrEmpty(corpoObj.getDirectorId())
						|| CommonUtils.isObjectNullOrEmpty(corpoObj.getYear())) {
					continue;
				}
				CorporateDirectorIncomeDetails corpoDirReq = incomeDetailsRepository.findByApplicationIdAndDirectorIdAndYear(corpoObj.getApplicationId(), corpoObj.getDirectorId(), corpoObj.getYear());
				if(CommonUtils.isObjectNullOrEmpty(corpoDirReq)) {
					corpoDirReq = new CorporateDirectorIncomeDetails();
					BeanUtils.copyProperties(corpoObj, corpoDirReq);
					corpoDirReq.setCreatedDate(new Date());
					corpoDirReq.setCreatedBy(corpoObj.getUserId());
					corpoDirReq.setIsActive(true);
					incomeDetailsRepository.save(corpoDirReq);
				} else {
					BeanUtils.copyProperties(corpoObj, corpoDirReq,"id","isActive","createdDate","createdBy","modifiedBy","modifiedDate","applicationId","directorId","year");
					corpoDirReq.setModifiedDate(new Date());
					corpoDirReq.setModifiedBy(corpoObj.getUserId());
					incomeDetailsRepository.save(corpoDirReq);
				}
			}	
			return true;
		} catch (Exception e) {
			logger.error("Exception Occured in saveOrUpdateDirectorIncomeDetails=======================>",e);
		}
		return false;
		
	}

	@Override
	public List<CorporateDirectorIncomeRequest> getDirectorIncomeDetails(Long applicationId)
			throws LoansException {
		
		try {
			CorporateDirectorIncomeRequest incomeRequest = null;
			List<CorporateDirectorIncomeDetails> incomeDetails = null;
			List<CorporateDirectorIncomeRequest> incomeDetailsResponse = null;
			logger.info("Entering into getDirectorIncomeDetails=======================>");
			
			if(!(CommonUtils.isObjectNullOrEmpty(applicationId))){
				incomeDetails = incomeDetailsRepository.findByApplicationIdAndIsActive(applicationId, true);
				incomeDetailsResponse = new ArrayList<CorporateDirectorIncomeRequest>();
				if(!CommonUtils.isObjectNullOrEmpty(incomeDetails)){
					for(CorporateDirectorIncomeDetails corpObj:incomeDetails) {
						if(!CommonUtils.isObjectNullOrEmpty(corpObj)) {
							incomeRequest = new CorporateDirectorIncomeRequest();
							incomeRequest.setSalaryStr(CommonUtils.convertValue(corpObj.getSalary()));
							incomeRequest.setTotalIncomeStr(CommonUtils.convertValue(corpObj.getTotalIncome()));
							String directorName = backgroundDetailsRepository.getDirectorNamefromDirectorId(corpObj.getDirectorId());
							incomeRequest.setDirectorName(directorName);
							BeanUtils.copyProperties(corpObj, incomeRequest);
						}
						incomeDetailsResponse.add(incomeRequest);
				}
					logger.info("Successfully get DirectorIncomeDetails=======================>"+incomeDetailsResponse);
					return incomeDetailsResponse;	
			}
				
		}
		  return Collections.emptyList();
		} catch (Exception e) {
			logger.error("Exception Occured in gettingDirectorIncomeDetails=======================>",e);
		}
		  return Collections.emptyList();
	}
	
	@Override
	public List<CorporateDirectorIncomeRequest> getDirectorIncomeLatestYearDetails(Long applicationId)
			throws LoansException {
		try {
			CorporateDirectorIncomeRequest incomeRequest = null;
			List<CorporateDirectorIncomeDetails> incomeDetails = null;
			List<CorporateDirectorIncomeRequest> incomeDetailsResponse = null;
			logger.info("ENTER IN getDirectorIncomeLatestYearDetails---------->>>>");
			
			if(!(CommonUtils.isObjectNullOrEmpty(applicationId))){
				incomeDetails = incomeDetailsRepository.getLatestYearDetails(applicationId);
				incomeDetailsResponse = new ArrayList<CorporateDirectorIncomeRequest>();
				if(!CommonUtils.isObjectNullOrEmpty(incomeDetails)){
					for(CorporateDirectorIncomeDetails corpObj:incomeDetails) {
						if(!CommonUtils.isObjectNullOrEmpty(corpObj)) {
							incomeRequest = new CorporateDirectorIncomeRequest();
							BeanUtils.copyProperties(corpObj, incomeRequest);
							String directorName = backgroundDetailsRepository.getDirectorNamefromDirectorId(incomeRequest.getDirectorId());
							incomeRequest.setDirectorName(directorName);
						}
						incomeDetailsResponse.add(incomeRequest);
				}
					logger.info("Successfully get DirectorIncomeLatestYearDetails------------>"+incomeDetailsResponse);
					return incomeDetailsResponse;	
			}
				
		}
		  return Collections.emptyList();
		} catch (Exception e) {
			logger.error("Exception Occured in gettingDirectorLatestYearIncomeDetails------------->",e);
		}
		  return Collections.emptyList();
	}
	
	
	

	@Override
	public List<Map<String, Object>> getDirectorBackGroundDetails(Long applicationId) throws LoansException {
		
		try {
			List<DirectorBackgroundDetail> backgroundDetailsList = null;			
			Map<String, Object> map = null;
			List<Map<String, Object>> directorBackgroundlist = null;
			logger.info("Entering into getDirectorBackGroundAndEmployeeDetails=======================>");
			
			if(!(CommonUtils.isObjectNullOrEmpty(applicationId))){
				backgroundDetailsList = backgroundDetailsRepository.listPromotorBackgroundFromAppId(applicationId);
			   logger.info("Directors List==============>"+backgroundDetailsList);
				directorBackgroundlist = new ArrayList<Map<String, Object>>();
				if(!CommonUtils.isObjectNullOrEmpty(backgroundDetailsList)){
					for(DirectorBackgroundDetail corpObj:backgroundDetailsList) {
						if(!CommonUtils.isObjectNullOrEmpty(corpObj)) {
							map = new HashMap<String, Object>();					
							map.put("directorId", corpObj.getId());
							map.put("address", corpObj.getAddress());
							map.put("pincode", corpObj.getPincode());
							map.put("stateCode", corpObj.getStateCode());
							map.put("city", corpObj.getCity());
							map.put("din", corpObj.getDin());
							map.put("networth", CommonUtils.convertValue(corpObj.getNetworth()));
							map.put("applicationId", applicationId);
							map.put("appointmentDate", corpObj.getAppointmentDate());
							map.put("salutationId", corpObj.getSalutationId());
							map.put("panNo", corpObj.getPanNo());
							map.put("districtMappingId", corpObj.getDistrictMappingId());
							map.put("designation", corpObj.getDesignation());
							map.put("directorsName", corpObj.getDirectorsName());
							map.put("totalExperience", corpObj.getTotalExperience());
							map.put("dob", simpleDateFormat.format(corpObj.getDob()));
							map.put("mobile", corpObj.getMobile());
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getGender())) {
								Gender byIdGndr = Gender.getById(corpObj.getGender());
								map.put("gender", !CommonUtils.isObjectNullOrEmpty(byIdGndr) ? byIdGndr.getValue() : "");	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getGender())) {
								map.put("genderInt", corpObj.getGender());	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getRelationshipType())) {
								DirectorRelationshipType byIdRelation = DirectorRelationshipType.getById(corpObj.getRelationshipType());
								map.put("relationshipType", !CommonUtils.isObjectNullOrEmpty(byIdRelation) ? byIdRelation.getValue() : "");	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getRelationshipType())) {
								map.put("relationshipTypeInt", corpObj.getRelationshipType());	
							}
							map.put("firstName", corpObj.getFirstName());
							map.put("lastName", corpObj.getLastName());
							map.put("middleName", corpObj.getMiddleName());
							map.put("title", corpObj.getTitle());
							map.put("shareholding", corpObj.getShareholding());
							map.put("isItrCompleted", corpObj.getIsItrCompleted());
							map.put("isCibilCompleted", corpObj.getIsCibilCompleted());
							map.put("isBankStatementCompleted", corpObj.getIsBankStatementCompleted());
							map.put("isOneFormCompleted", corpObj.getIsOneFormCompleted());
							map.put("aadhar", corpObj.getAadhar());
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getMaritalStatus())) {
								MaritalStatus byIdMarital = MaritalStatus.getById(corpObj.getMaritalStatus());
								map.put("maritalStatus", !CommonUtils.isObjectNullOrEmpty(byIdMarital) ? byIdMarital.getValue() : "");	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getMaritalStatus())) {
								map.put("maritalStatusInt", corpObj.getMaritalStatus());	
							}
							map.put("noOfDependent", corpObj.getNoOfDependent());
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getResidenceType())) {
								ResidenceStatusRetailMst byIdResidentType = ResidenceStatusRetailMst.getById(corpObj.getResidenceType());
								map.put("residenceType", !CommonUtils.isObjectNullOrEmpty(byIdResidentType) ? byIdResidentType.getValue() : "");	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getResidenceType())) {
								map.put("residenceTypeInt", corpObj.getResidenceType());	
							}
							
							map.put("residenceSinceMonth", corpObj.getResidenceSinceMonth());
							map.put("residenceSinceYear", corpObj.getResidenceSinceYear());
							map.put("isFamilyMemberInBusiness", corpObj.getFamilyMemberInBusiness());
							map.put("stateId", corpObj.getStateId());
							map.put("cityId", corpObj.getCityId());
							map.put("premiseNumber", corpObj.getPremiseNumber());
							map.put("streetName", corpObj.getStreetName());
							map.put("landmark", corpObj.getLandmark());
							
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getQualificationId())) {
								EducationQualificationNTB byIdEduNtb = EducationQualificationNTB.getById(corpObj.getQualificationId());
								map.put("qualificationId", !CommonUtils.isObjectNullOrEmpty(byIdEduNtb) ? byIdEduNtb.getValue() : "");	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getQualificationId())) {
								map.put("qualificationIdInt", corpObj.getQualificationId());	
							}
							
							map.put("isMainDirector", corpObj.getIsMainDirector());
						
							// getting Employee Detail for each Director
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getEmploymentDetail())) {
							logger.info("Employment Detail======>"+corpObj.getEmploymentDetail());
							map.put("empId", corpObj.getEmploymentDetail().getId()); 
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getEmploymentDetail().getTypeOfEmployment())) {
								OccupationNatureNTB byIdOccupation = OccupationNatureNTB.getById(Integer.parseInt(corpObj.getEmploymentDetail().getTypeOfEmployment().toString()));
								map.put("typeOfEmployment", !CommonUtils.isObjectNullOrEmpty(byIdOccupation) ? byIdOccupation.getValue() : "");	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getEmploymentDetail().getTypeOfEmployment())) {
								OccupationNatureNTB byIdOccupation = OccupationNatureNTB.getById(Integer.parseInt(corpObj.getEmploymentDetail().getTypeOfEmployment().toString()));
								map.put("typeOfEmploymentInt", !CommonUtils.isObjectNullOrEmpty(byIdOccupation) ? byIdOccupation.getValue() : "");	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getEmploymentDetail().getEmploymentWith())) {
								EmploymentWithNTB byIdEmpWith = EmploymentWithNTB.getById(Integer.parseInt(corpObj.getEmploymentDetail().getEmploymentWith().toString()));
								map.put("employmentWith", !CommonUtils.isObjectNullOrEmpty(byIdEmpWith) ? byIdEmpWith.getValue() : "");	
							}
							if(!CommonUtils.isObjectNullOrEmpty(corpObj.getEmploymentDetail().getEmploymentStatus())) {
								EmploymentStatusNTB byIdEmpStatus = EmploymentStatusNTB.getById(Integer.parseInt(corpObj.getEmploymentDetail().getEmploymentStatus().toString()));
								map.put("employmentStatus", !CommonUtils.isObjectNullOrEmpty(byIdEmpStatus) ? byIdEmpStatus.getValue() : "");	
							}
							map.put("totalExperience", corpObj.getEmploymentDetail().getTotalExperience());
							map.put("nameOfEmployer", corpObj.getEmploymentDetail().getNameOfEmployer());
							map.put("salary", CommonUtils.convertValue(corpObj.getEmploymentDetail().getSalary()));
							}
							 try {
									if(!CommonUtils.isObjectNullOrEmpty(corpObj.getDistrictMappingId())) {
										PincodeDataResponse pinRes=(pincodeDateService.getById(Long.valueOf(String.valueOf(corpObj.getDistrictMappingId()))));
										map.put("pindata", pinRes);
										
									}
								} catch (Exception e) {
								 logger.error(CommonUtils.EXCEPTION,e);
								}
							 
							directorBackgroundlist.add(map);
						}
						
				}
					logger.info("Successfully get getDirectorBackGroundAndEmployeeDetails=======================>");
					return directorBackgroundlist;	
			}
				
		}
		  return Collections.emptyList();
		} catch (Exception e) {
			logger.error("Exception Occured in getDirectorBackGroundAndEmployeeDetails=======================>",e);
		}
		  return Collections.emptyList();
	}

}
