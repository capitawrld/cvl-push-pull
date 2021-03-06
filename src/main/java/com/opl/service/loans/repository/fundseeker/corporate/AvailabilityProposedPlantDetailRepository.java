package com.opl.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.model.teaser.finalview.AvailabilityProposedPlantDetailResponse;
import com.opl.service.loans.domain.fundseeker.corporate.AvailabilityProposedPlantDetail;

/**
 * @author Sanket
 *
 */
public interface AvailabilityProposedPlantDetailRepository
		extends JpaRepository<AvailabilityProposedPlantDetail, Long> {

	@Modifying
	@Transactional
	@Query("update AvailabilityProposedPlantDetail a set a.isActive = false where a.storageDetailsId= :sId")
	public void inActiveAvailabilityProposedPlantDetails(@Param("sId")Long storageDetailsId);
	
	@Modifying
	@Transactional
	@Query("update AvailabilityProposedPlantDetail a set a.isActive = false where a.applicationId.id= :applicationId and a.isActive=true")
	public void inActiveAvailabilityProposedPlantDetailsByAppId(@Param("applicationId")Long applicationId);

	@Query("select new com.opl.mudra.api.loans.model.teaser.finalview.AvailabilityProposedPlantDetailResponse(a.descriptionPM, a.estimatedValue, a.importedOrIndigenous, a.supplier, a.useOrPurpose) from AvailabilityProposedPlantDetail a where a.applicationId.id= :applicationId and isActive=true")
	 public List<AvailabilityProposedPlantDetailResponse> listByApplicationId(@Param("applicationId")Long applicationId);

	public List<AvailabilityProposedPlantDetail> findByApplicationIdIdAndIsActive(Long applicationId, Boolean isActive);
}
