package com.capitaworld.service.loans.service.fundseeker.corporate;

import org.springframework.http.ResponseEntity;

import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.NTBRequest;
import com.capitaworld.service.loans.model.corporate.FundSeekerInputRequestResponse;

public interface FundSeekerInputRequestService {

    public boolean saveOrUpdate(FundSeekerInputRequestResponse fundSeekerInputRequest) throws Exception;

    public ResponseEntity<LoansResponse> saveOrUpdateDirectorDetail(FundSeekerInputRequestResponse fundSeekerInputRequest);

    public ResponseEntity<LoansResponse> get(FundSeekerInputRequestResponse fundSeekerInputRequest);

    public ResponseEntity<LoansResponse> getDirectorDetail(FundSeekerInputRequestResponse fundSeekerInputRequest);
    
    public LoansResponse callMatchEngineClient(Long applicationId,Long userId,Integer businessTypeId);
    
    
    /**
     * Use to call after Director Details saved.
     * @param ntbRequest
     * @return
     */
    public LoansResponse postDirectorBackground(NTBRequest ntbRequest);

	/**
	 * @param fundSeekerInputRequestResponse
	 * @throws Exception 
	 */
	public LoansResponse invokeFraudAnalytics(FundSeekerInputRequestResponse fundSeekerInputRequestResponse) throws Exception;
    
}
