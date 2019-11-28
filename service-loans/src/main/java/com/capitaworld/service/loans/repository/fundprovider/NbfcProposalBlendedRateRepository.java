
package com.capitaworld.service.loans.repository.fundprovider;

import com.capitaworld.service.loans.domain.fundprovider.NbfcProposalBlendedRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NbfcProposalBlendedRateRepository extends JpaRepository<NbfcProposalBlendedRate,Long>{

	@Query(value = "select * from nbfc_proposal_blended_rate lm where lm.application_id =:applicationId and lm.is_active = true order by id desc limit 1",nativeQuery = true)
    public NbfcProposalBlendedRate getByApplicationIdOrderByIdDescLimit1(@Param("applicationId") Long applicationId);
	
}

