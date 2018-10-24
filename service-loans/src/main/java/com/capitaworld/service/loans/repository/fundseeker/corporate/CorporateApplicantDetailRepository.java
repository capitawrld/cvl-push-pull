package com.capitaworld.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitaworld.service.loans.domain.fundseeker.corporate.CorporateApplicantDetail;

public interface CorporateApplicantDetailRepository extends JpaRepository<CorporateApplicantDetail, Long> {

	@Query("from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.applicationId.userId =:userId and cr.isActive=true")
	public CorporateApplicantDetail getByApplicationAndUserId(@Param("userId") Long userId, @Param("applicationId") Long applicationId);
	
	@Query("from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.applicationId.userId =:userId")
	public CorporateApplicantDetail getByApplicationAndUserIdForSP(@Param("userId") Long userId,
			@Param("applicationId") Long applicationId);
	
	@Query("select cr.organisationName,cr.modifiedDate from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.applicationId.userId =:userId and cr.isActive=true")
	public List<Object[]> getByNameAndLastUpdateDate(@Param("userId") Long userId,
			@Param("applicationId") Long applicationId);


	@Query("from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId")
	public CorporateApplicantDetail findOneByApplicationIdId(@Param("applicationId") Long applicationId);
	
	@Query("from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.isActive=true")
	public CorporateApplicantDetail getByApplicationIdAndIsAtive(@Param("applicationId") Long applicationId);
	
	public CorporateApplicantDetail findByApplicationIdIdAndIsActive(Long applicationId,Boolean isActive);

	@Query("select count(cr.applicationId.id) from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.applicationId.userId =:userId and cr.isActive=true and (cr.organisationName != NULL and cr.organisationName != '')")
	public Long hasAlreadyApplied(@Param("userId") Long userId,
			@Param("applicationId") Long applicationId);
	
	@Modifying
	@Query(value="update fs_corporate_applicant_details set latitude =:lat,longitude =:lon where application_id =:applicationId and is_active = 1",nativeQuery = true)
	public int updateLatLong(@Param("lat") Double lat,@Param("lon") Double lon, @Param("applicationId") Long applicationId);
	
	@Query("select cr.latitude,cr.longitude from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.applicationId.userId =:userId and cr.isActive=true")
	public List<Object[]> getLatLonByApplicationAndUserId(@Param("applicationId") Long applicationId,@Param("userId") Long userId);

	@Query("select count(cr.id) from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.applicationId.userId =:userId and cr.isActive=true")
	public long getApplicantCount(@Param("userId") Long userId,
			@Param("applicationId") Long applicationId);
	
	@Query("select cr.establishmentYear from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.applicationId.userId =:userId and cr.isActive=true")
	public Integer getApplicantEstablishmentYear(@Param("userId") Long userId,
			@Param("applicationId") Long applicationId);

	@Query("select cr.gstIn from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.isActive=true")
	public String getGstInByApplicationId(@Param("applicationId") Long applicationId);
	
	@Query("select cr.panNo from CorporateApplicantDetail cr where cr.applicationId.id =:applicationId and cr.isActive=true")
	public String getPanNoByApplicationId(@Param("applicationId") Long applicationId);
}
