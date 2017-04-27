package com.capitaworld.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitaworld.service.loans.domain.fundseeker.corporate.AchievementDetail;

public interface AchievementDetailsRepository extends JpaRepository<AchievementDetail, Long> {
	
	
	@Query("from AchievementDetail  a where a.applicationId.id=:id AND a.isActive=true")
	public List<AchievementDetail> listAchievementFromAppId(@Param("id") Long id);
	
	@Modifying
	@Query("update AchievementDetail  a set a.isActive=false where a.id=:id")
	public int remove(@Param("id") Long id);
}
