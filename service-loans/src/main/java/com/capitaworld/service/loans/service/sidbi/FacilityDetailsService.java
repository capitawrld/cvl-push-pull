package com.capitaworld.service.loans.service.sidbi;

import java.util.List;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.sidbi.FacilityDetailsRequest;

/**
 * Created by pooja.patel on 19-06-2019.
 */
public interface FacilityDetailsService {

    public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException;
    public List<FacilityDetailsRequest> getFacilityDetailsListAppId(Long applicationId,Long userId) throws LoansException;
}
