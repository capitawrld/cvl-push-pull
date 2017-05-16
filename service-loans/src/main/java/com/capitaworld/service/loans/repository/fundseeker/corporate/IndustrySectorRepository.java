package com.capitaworld.service.loans.repository.fundseeker.corporate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitaworld.service.loans.domain.IndustrySectorDetail;

public interface IndustrySectorRepository extends JpaRepository<IndustrySectorDetail, Long> {

	@Modifying
	@Query("update IndustrySectorDetail isd set isd.isActive = false where isd.applicationId =:applicationId and isd.isActive = true")
	public int inActiveMappingByApplicationId(@Param("applicationId") Long applicationId);

	@Query("select o.industryId from IndustrySectorDetail o where o.sectorId = :sectorId")
	public Long findOneBySectorId(@Param("sectorId")Long sectorId);
	
	@Query("select o.industryId from IndustrySectorDetail o where o.applicationId = :applicationId and o.isActive = true")
	public List<Long> getIndustryByApplicationId(@Param("applicationId")Long applicationId);
	
	@Query("select o.sectorId from IndustrySectorDetail o where o.applicationId = :applicationId and o.isActive = true")
	public List<Long> getSectorByApplicationId(@Param("applicationId")Long applicationId);

}
