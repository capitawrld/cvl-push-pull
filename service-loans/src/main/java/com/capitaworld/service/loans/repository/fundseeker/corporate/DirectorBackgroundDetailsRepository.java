package com.capitaworld.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitaworld.service.loans.domain.fundseeker.corporate.DirectorBackgroundDetail;

public interface DirectorBackgroundDetailsRepository extends JpaRepository<DirectorBackgroundDetail, Long> {

	@Query("from DirectorBackgroundDetail o where o.applicationId.id = :id and o.applicationId.userId =:userId and isActive = true")
	public List<DirectorBackgroundDetail> listPromotorBackgroundFromAppId(@Param("id") Long id,@Param("userId")Long userId);

	@Modifying
	@Query("update DirectorBackgroundDetail pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId.id =:applicationId and pm.isActive = true")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId);

	@Query("select sum(o.networth) from DirectorBackgroundDetail o where o.applicationId.id = :applicationId and isActive = true")
	public Double getSumOfDirectorsNetworth(@Param("applicationId") Long applicationId);

}
