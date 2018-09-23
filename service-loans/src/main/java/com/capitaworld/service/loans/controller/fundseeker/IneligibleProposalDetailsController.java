package com.capitaworld.service.loans.controller.fundseeker;

import com.capitaworld.service.loans.model.FrameRequest;
import com.capitaworld.service.loans.model.InEligibleProposalDetailsRequest;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.service.common.IneligibleProposalDetailsService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by KushalCW on 22-09-2018.
 */

@RestController
public class IneligibleProposalDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(IneligibleProposalDetailsController.class);

    @Autowired
    private IneligibleProposalDetailsService ineligibleProposalDetailsService;

    @RequestMapping(value = "/save/ineligible/proposal", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoansResponse> save(@RequestBody InEligibleProposalDetailsRequest inEligibleProposalDetailsRequest, HttpServletRequest request) {
        if(CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest) ||
                CommonUtils.isObjectNullOrEmpty(inEligibleProposalDetailsRequest.getApplicationId())){
            logger.warn("Requested data can not be empty.Invalid Request. ");
            return new ResponseEntity<LoansResponse>(
                    new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
        }

        Boolean isDetailsSaved = ineligibleProposalDetailsService.save(inEligibleProposalDetailsRequest);
        if(isDetailsSaved){
            return new ResponseEntity<LoansResponse>(
                    new LoansResponse("Data saved", HttpStatus.OK.value()), HttpStatus.OK);
        }else {
            return new ResponseEntity<LoansResponse>(
                    new LoansResponse("Data not saved", HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
        }
    }
}
