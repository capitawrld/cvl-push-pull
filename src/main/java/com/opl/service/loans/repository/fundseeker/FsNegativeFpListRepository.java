package com.opl.service.loans.repository.fundseeker;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.FsNegativeFpList;

public interface FsNegativeFpListRepository extends JpaRepository<FsNegativeFpList, Long>{
	@Modifying
	@Query("update FsNegativeFpList fn set fn.isActive = false where fn.applicationId =:applicationId and fn.isActive = true")
	public int inActiveMappingByApplicationId(@Param("applicationId") Long applicationId);
	
	@Query("select fn.fpId from FsNegativeFpList fn where fn.applicationId = :applicationId and fn.isActive = true")
	public List<Long> getListByApplicationId(@Param("applicationId") Long applicationId);
	
	@Modifying
	@Query("update FsNegativeFpList pm set pm.isActive = false,pm.modifiedDate = NOW(),pm.modifiedBy =:userId where pm.applicationId =:applicationId and pm.isActive = true")
	public int inActive(@Param("userId") Long userId,@Param("applicationId") Long applicationId);


}
