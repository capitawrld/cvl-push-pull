package com.opl.service.loans.utils.cma;

import java.util.Comparator;

import com.opl.service.loans.domain.fundseeker.corporate.LiabilitiesDetails;

public class LiabilityComparator implements Comparator<LiabilitiesDetails>{
	
	@Override
    public int compare(LiabilitiesDetails o1, LiabilitiesDetails o2) {
        return o1.getYear().compareTo(o2.getYear());
    }
}
