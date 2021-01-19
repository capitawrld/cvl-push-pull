package com.opl.service.loans.service.fundseeker.corporate;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.service.loans.domain.sidbi.PushPullRequest;

public interface PushPullApplicationService {

	public LoansResponse saveOrUpdate(PushPullRequest pushPullRequest) throws LoansException;

}
