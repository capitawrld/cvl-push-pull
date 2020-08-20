package com.opl.service.loans.repository.fundseeker.retail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opl.service.loans.domain.fundseeker.retail.ObligationDetail;

/**
 * Created by ravina.panchal on 04-10-2018.
 */
public interface ObligationDetailRepository extends JpaRepository<ObligationDetail, Long> {

    @Query("select o from ObligationDetail o where o.applicationId.id = :id and o.isActive = true")
    public List<ObligationDetail> listObligationDetailFromAppId(@Param("id")Long id);
    
    @Query("select o from ObligationDetail o where o.applicationProposalMapping.proposalId = :proposalId and o.isActive = true")
    public List<ObligationDetail> listObligationDetailFromProposalId(@Param("proposalId") Long proposalId);

}
