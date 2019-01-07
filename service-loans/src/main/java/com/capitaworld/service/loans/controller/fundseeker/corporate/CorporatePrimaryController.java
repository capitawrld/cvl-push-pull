package com.capitaworld.service.loans.controller.fundseeker.corporate;

import javax.servlet.http.HttpServletRequest;

import com.capitaworld.service.loans.exceptions.LoansException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.corporate.PrimaryCorporateRequest;
import com.capitaworld.service.loans.service.fundseeker.corporate.PrimaryCorporateService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/corporate_primary")
public class CorporatePrimaryController {
    private static final Logger logger = LoggerFactory.getLogger(CorporatePrimaryController.class);


    @Autowired
    private PrimaryCorporateService primaryCorporateService;

    /*@RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String getPing() {
        logger.info("Ping success");
        return "Ping Succeed";
    }*/

    @RequestMapping(value = "${primary}/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoansResponse> savePrimary(@RequestBody PrimaryCorporateRequest primaryCorporateRequest,
                                                     HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId)
            throws LoansException {
        try {
            CommonDocumentUtils.startHook(logger, "savePrimary");
            // request must not be null
            Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
            if (CommonDocumentUtils.isThisClientApplication(request)) {
                primaryCorporateRequest.setClientId(clientId);
            }

            if (userId == null) {
                logger.warn("userId can not be empty ==>" + primaryCorporateRequest);
                return new ResponseEntity<LoansResponse>(
                        new LoansResponse("Invalid Request", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
            }

            if (primaryCorporateRequest.getId() == null) {
                logger.warn("ID must not be empty ==>" + primaryCorporateRequest.getId());
                return new ResponseEntity<LoansResponse>(
                        new LoansResponse("ID must not be empty.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
            }

            primaryCorporateService.saveOrUpdate(primaryCorporateRequest, userId);
            CommonDocumentUtils.endHook(logger, "savePrimary");
            return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
                    HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error while saving Primary Details==>", e);
            return new ResponseEntity<LoansResponse>(
                    new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "${primary}/get/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoansResponse> getPrimary(@PathVariable("applicationId") Long applicationId,
                                                    HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
        try {
            CommonDocumentUtils.startHook(logger, "getPrimary");
            Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
            if (CommonDocumentUtils.isThisClientApplication(request)) {
                userId = clientId;
            } else {
                userId = (Long) request.getAttribute(CommonUtils.USER_ID);
            }

            if (applicationId == null || userId == null) {
                logger.warn("ID and User Id Require to get Primary Working Details ==>" + applicationId + "User ID ==>"
                        + userId);
                return new ResponseEntity<LoansResponse>(
                        new LoansResponse("Invalid Request", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
            }
            PrimaryCorporateRequest response = primaryCorporateService.get(applicationId, userId);
            LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
            loansResponse.setData(response);
            CommonDocumentUtils.endHook(logger, "getPrimary");
            return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while getting Primary Corporate Details==>", e);
            return new ResponseEntity<LoansResponse>(
                    new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.OK);
        }
    }
    
    @RequestMapping(value = "${primary}/get", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public PrimaryCorporateRequest getPrimaryForClient(@RequestBody Long applicationId) {
        try {
            CommonDocumentUtils.startHook(logger, "getPrimaryForClient");

            if (applicationId == null) {
                logger.warn("ID and User Id Require to get Primary Working Details ==>" + applicationId);
                return null;
            }
            CommonDocumentUtils.endHook(logger, "getPrimaryForClient");
            return primaryCorporateService.get(applicationId);
        } catch (Exception e) {
            logger.error("Error while getting Primary Corporate Details==>{}", e);
            return null;
        }
    }
}
