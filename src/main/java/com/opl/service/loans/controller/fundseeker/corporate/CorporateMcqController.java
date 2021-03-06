package com.opl.service.loans.controller.fundseeker.corporate;

import javax.servlet.http.HttpServletRequest;

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

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.model.corporate.CorporateMcqRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.service.fundseeker.corporate.CorporateMcqService;
import com.opl.service.loans.utils.CommonDocumentUtils;

@RestController
@RequestMapping("/corporate_mcq")
public class CorporateMcqController {

    private static final Logger logger = LoggerFactory.getLogger(CorporateMcqController.class);

    @Autowired
    private CorporateMcqService corporateMcqService;

    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoansResponse> save(@RequestBody CorporateMcqRequest corporateMcqRequest, HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) throws LoansException {
        try {
            CommonDocumentUtils.startHook(logger, "save");
            // request must not be null
            Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);
            if (userId == null) {
                logger.warn("userId can not be empty ==>" + corporateMcqRequest);
                return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
            }

            if (corporateMcqRequest.getProposalMappingId() == null) {
                logger.warn("Proposal ID can not be empty ==>" + corporateMcqRequest.getProposalMappingId());
                return new ResponseEntity<LoansResponse>(new LoansResponse("Proposal ID can not be empty.", HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
            }
            if (CommonDocumentUtils.isThisClientApplication(request)) {
                corporateMcqRequest.setClientId(clientId);
            }
            corporateMcqService.saveOrUpdate(corporateMcqRequest, userId);
            CommonDocumentUtils.endHook(logger, "save");
            return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while saving final corporate mcq : ",e);
            return new ResponseEntity<LoansResponse>(
                    new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value = "/skipMcq", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoansResponse> skipMcq(@RequestBody CorporateMcqRequest corporateMcqRequest,
                                              HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId)
            throws LoansException {
        try {
            CommonDocumentUtils.startHook(logger, "skipMcq");
            // request must not be null

            Long userId = (Long) request.getAttribute(CommonUtils.USER_ID);

            if (userId == null) {
                logger.warn("userId can not be empty ==>" + corporateMcqRequest);
                return new ResponseEntity<LoansResponse>(
                        new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
            }

            if (corporateMcqRequest.getApplicationId() == null) {
                logger.warn("Application ID can not be empty ==>" + corporateMcqRequest.getId());
                return new ResponseEntity<LoansResponse>(
                        new LoansResponse("Application ID can not be empty.", HttpStatus.BAD_REQUEST.value()),
                        HttpStatus.OK);
            }
            if (CommonDocumentUtils.isThisClientApplication(request)) {
                corporateMcqRequest.setClientId(clientId);
            }
            corporateMcqService.skipMcq(corporateMcqRequest, userId);
            CommonDocumentUtils.endHook(logger, "skipMcq");
            return new ResponseEntity<LoansResponse>(new LoansResponse("Successfully Saved.", HttpStatus.OK.value()),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while skipping final corporate mcq : ",e);
            return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value = "/get/{proposalId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoansResponse> get(@PathVariable("proposalId") Long proposalId, HttpServletRequest request, @RequestParam(value = "clientId", required = false) Long clientId) {
        try {
            try {
                CommonDocumentUtils.startHook(logger, "get");
                Long userId;
                if (CommonDocumentUtils.isThisClientApplication(request)) {
                    userId = clientId;
                } else {
                    userId = (Long) request.getAttribute(CommonUtils.USER_ID);
                }
                if (userId == null || proposalId == null) {
                    logger.warn("ID and ApplicationId Require to get Final  corporate mcq. ID==>" + userId + " and proposalId==>" + proposalId);
                    return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
                }
                CorporateMcqRequest response = corporateMcqService.get(proposalId);
                LoansResponse loansResponse = new LoansResponse("Data Found.", HttpStatus.OK.value());
                loansResponse.setData(response);
                CommonDocumentUtils.endHook(logger, "get");
                return new ResponseEntity<LoansResponse>(loansResponse, HttpStatus.OK);
            } catch (Exception e) {
                logger.error("Error while getting Final corporate mcq==>", e);
                return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error("Error while getting  final corporate mcq : ",e);
            return new ResponseEntity<LoansResponse>(new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.OK);
        }
    }
}
