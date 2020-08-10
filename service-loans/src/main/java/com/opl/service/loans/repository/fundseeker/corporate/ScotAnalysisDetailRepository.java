package com.opl.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.model.teaser.finalview.ScotAnalysisDetailResponse;
import com.opl.service.loans.domain.fundseeker.corporate.ScotAnalysisDetail;

/**
 * @author Sanket
 *
 */
public interface ScotAnalysisDetailRepository extends JpaRepository<ScotAnalysisDetail, Long> {

	@Modifying
	@Transactional
	@Query("update ScotAnalysisDetail a set a.isActive = false where a.storageDetailsId= :sId")
	public void inActiveScotDetails(@Param("sId")Long storageDetailsId);
	
	@Modifying
	@Transactional
	@Query("update ScotAnalysisDetail a set a.isActive = false where a.applicationId.id= :applicationId and a.isActive=true")
	public void inActiveScotDetailsByAppId(@Param("applicationId")Long applicationId);
	
	@Query("select new com.opl.mudra.api.loans.model.teaser.finalview.ScotAnalysisDetailResponse(a.	concernsDetails, a.concernsMeasure, a.opportunitiesDetials, a.strengthDetails, a.weaknessDetials, a.weaknessMeasure) from ScotAnalysisDetail a where a.applicationId.id= :applicationId and isActive=true")
	 public List<ScotAnalysisDetailResponse> listByApplicationId(@Param("applicationId")Long applicationId);
	
	public List<ScotAnalysisDetail> findByApplicationIdIdAndIsActive(Long applicationId, Boolean isActive);

}
