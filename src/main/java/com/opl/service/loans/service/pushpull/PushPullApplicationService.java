package com.opl.service.loans.service.pushpull;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.service.loans.model.pushpull.PushPullRequest;

public interface PushPullApplicationService {

	public LoansResponse saveOrUpdate(PushPullRequest pushPullRequest) throws LoansException;

}
