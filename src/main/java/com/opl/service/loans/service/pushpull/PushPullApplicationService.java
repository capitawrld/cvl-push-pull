package com.opl.service.loans.service.pushpull;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.service.loans.domain.TataMotorsLoanDetails;
import com.opl.service.loans.model.pushpull.PushPullRequest;
import com.opl.service.loans.model.pushpull.TmlRootRequest;

public interface PushPullApplicationService {

	public LoansResponse saveOrUpdate(PushPullRequest pushPullRequest) throws LoansException;
	
	public LoansResponse saveTataMotorsLoanDetails(TmlRootRequest tmlRootRequest);
	
	public LoansResponse saveTataMotorsReqResDetails(TmlRootRequest tmlRootRequest);

	public TataMotorsLoanDetails getDataBYEmail(Long userId);
	

}
