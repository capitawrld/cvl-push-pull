package com.opl.service.loans.repository.fundprovider;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundprovider.CvlVehicleMultipleOption;

public interface CvlVehicleMultipleOptionRepo  extends JpaRepository<CvlVehicleMultipleOption, Long> {
    @Query("from CvlVehicleMultipleOption tp where tp.fpProductId=:fpProductId and isActive=true and typeId=:typeId")
    public CvlVehicleMultipleOption getByFpProductIdAndType(@Param("fpProductId") Long fpProductId,@Param("typeId") Long typeId);

    @Modifying
    @Query("UPDATE CvlVehicleMultipleOption msme SET msme.isActive=FALSE WHERE msme.fpProductId=:fpProductId and typeId=:typeId")
    public int inActiveMasterByFpProductIdAndType(@Param("fpProductId")Long fpProductId,@Param("typeId") Long typeId);

    List<CvlVehicleMultipleOption> findByFpProductIdAndIsActiveAndTypeId(Long fpProductId, boolean b,Long typeId);
    
    @Query("select masterId from CvlVehicleMultipleOption tp where tp.fpProductId=:fpProductId and isActive=true and typeId=:typeId")
    List<Long> finMasterIddByFpProductIdAndIsActiveAndTypeId(@Param("fpProductId") Long fpProductId,@Param("typeId") Long typeId);
    
}
