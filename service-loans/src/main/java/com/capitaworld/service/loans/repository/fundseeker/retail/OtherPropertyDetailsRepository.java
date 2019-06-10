package com.capitaworld.service.loans.repository.fundseeker.retail;

import com.capitaworld.service.loans.domain.fundseeker.retail.EmpAgriculturistType;
import com.capitaworld.service.loans.domain.fundseeker.retail.EmpSalariedType;
import com.capitaworld.service.loans.domain.fundseeker.retail.OtherPropertyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtherPropertyDetailsRepository extends JpaRepository<OtherPropertyDetails, Long> {

    @Query("select o from OtherPropertyDetails o where o.applicationId.id = :applicationId and o.isActive = true")
    List<OtherPropertyDetails> getListByApplicationId(@Param("applicationId")Long applicationId);

    @Query("select o from OtherPropertyDetails o where o.proposalId.proposalId = :proposalId and o.isActive = true")
    public List<OtherPropertyDetails> listPropertyFromPropsalIdAndType(@Param("proposalId")Long proposalId);
}
