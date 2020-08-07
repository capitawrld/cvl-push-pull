package com.opl.service.loans.controller.fundseeker.corporate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.LoansResponse;
import com.opl.mudra.api.loans.model.corporate.FundSeekerInputRequestResponse;
import com.opl.mudra.api.loans.model.mobile.MobileApiResponse;
import com.opl.mudra.api.loans.model.mobile.MobileLoanRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.notification.utils.MobileCustomizeResponse;
import com.opl.service.loans.service.fundseeker.corporate.FundSeekerInputRequestService;


@RestController
@RequestMapping("/fundseeker_input_request_mobile")
public class FundSeekerInputRequestMobileController {

    private static final Logger logger = LoggerFactory.getLogger(FundSeekerInputRequestController.class);

    private static final String MOBILE_GET_ONE_FORM_USER_ID_IS_NULL_OR_EMPTY_MSG = "Mobile Get Oneform UserId is null or empty !! ";
    private static final String MOBILE_GET_ONE_FORM_APPLCATION_ID_IS_NULL_OR_EMPTY_MSG  = "Mobile Get Oneform ApplcationId is null or empty !! ";
    private static final String USER_ID_IS_NULL_OR_EMPTY_MSG = "UserId is null or empty !!";
    private static final String APPLCATION_ID_IS_NULL_OR_EMPTY_MSG = "ApplcationId is null or empty !!";

    @Autowired
    private FundSeekerInputRequestService fundSeekerInputRequestService;

    @RequestMapping(value = "/save_oneform", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MobileApiResponse> save(@RequestBody FundSeekerInputRequestResponse fundSeekerInputRequestResponse)
            throws LoansException

    {
        try {

            logger.info("ENTER IN SAVE FUNDSEEKER INPUT REQUEST----------------------------------->");

            if(fundSeekerInputRequestResponse.getUserId() == null) {
            	logger.info(MOBILE_GET_ONE_FORM_USER_ID_IS_NULL_OR_EMPTY_MSG);
            	return new ResponseEntity<MobileApiResponse>(
                        new MobileApiResponse(USER_ID_IS_NULL_OR_EMPTY_MSG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403),
                        HttpStatus.BAD_REQUEST);
            }
            
            if(fundSeekerInputRequestResponse.getApplicationId() == null) {
            	logger.info(MOBILE_GET_ONE_FORM_APPLCATION_ID_IS_NULL_OR_EMPTY_MSG);
            	return new ResponseEntity<MobileApiResponse>(
                        new MobileApiResponse(APPLCATION_ID_IS_NULL_OR_EMPTY_MSG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403),
                        HttpStatus.BAD_REQUEST);
            }           

            logger.info("GOING TO SAVE FUNDSEEKER INPUT REQUEST-------------USERID--->" + fundSeekerInputRequestResponse.getUserId() + "-------------APPLICATION ID --------------------->" + fundSeekerInputRequestResponse.getApplicationId());
            boolean result = fundSeekerInputRequestService.saveOrUpdate(fundSeekerInputRequestResponse);

            if(result){
                logger.info("FUNDSEEKER INPUT SAVED SUCCESSFULLY");
                return new ResponseEntity<MobileApiResponse>(
                        new MobileApiResponse("Oneform Saved Successfully","true", MobileCustomizeResponse.SUCCESS200),
                        HttpStatus.OK);
            } else {
                logger.info("FUNDSEEKER INPUT NOT SAVED");
                return new ResponseEntity<MobileApiResponse>(
                        new MobileApiResponse("Oneform Not Saved", CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR401),
                        HttpStatus.OK);
            }

        } catch (Exception e) {
            logger.error("Error while saving one form data : ",e);
            return new ResponseEntity<MobileApiResponse>(
                    new MobileApiResponse(CommonUtils.SOMETHING_WENT_WRONG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.INTERNALSERVERERROR407),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/get_oneform", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MobileApiResponse> get(@RequestBody FundSeekerInputRequestResponse fundSeekerInputRequestResponse)
            throws LoansException
    {
        try {
            if(fundSeekerInputRequestResponse.getUserId() == null) {
            	logger.info(MOBILE_GET_ONE_FORM_USER_ID_IS_NULL_OR_EMPTY_MSG);
            	return new ResponseEntity<MobileApiResponse>(
                        new MobileApiResponse(USER_ID_IS_NULL_OR_EMPTY_MSG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403),
                        HttpStatus.BAD_REQUEST);
            }
            
            if(fundSeekerInputRequestResponse.getApplicationId() == null) {
            	logger.info(MOBILE_GET_ONE_FORM_APPLCATION_ID_IS_NULL_OR_EMPTY_MSG);
            	return new ResponseEntity<MobileApiResponse>(
                        new MobileApiResponse(APPLCATION_ID_IS_NULL_OR_EMPTY_MSG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("Application Id for Getting one form for mobile============>{}",fundSeekerInputRequestResponse.getApplicationId());
            
            ResponseEntity<LoansResponse> responseEntity = fundSeekerInputRequestService.get(fundSeekerInputRequestResponse);
            LoansResponse loansResponse = responseEntity.getBody();
            if(!CommonUtils.isObjectNullOrEmpty(loansResponse.getData()) &&  (loansResponse.getStatus() == HttpStatus.OK.value())) {
            	return new ResponseEntity<MobileApiResponse>(new MobileApiResponse("Successfully get Data","true", loansResponse.getData(), MobileCustomizeResponse.SUCCESS200), HttpStatus.OK);	
            } else {
            	return new ResponseEntity<MobileApiResponse>(new MobileApiResponse("Data not found",CommonUtils.FALSE_LITERAL, null, MobileCustomizeResponse.SUCCESS204), HttpStatus.OK);
            }
            
        } catch (Exception e) {
            logger.error("Error while fetching one form data : ",e);
            return new ResponseEntity<MobileApiResponse>(
                    new MobileApiResponse(CommonUtils.SOMETHING_WENT_WRONG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.INTERNALSERVERERROR407),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @RequestMapping(value = "/get_director_detail", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MobileApiResponse> getDirectorDetail(@RequestBody FundSeekerInputRequestResponse fundSeekerInputRequestResponse)
            throws LoansException
    {
        try
        {
        	 if(fundSeekerInputRequestResponse.getUserId() == null) {
             	logger.info(MOBILE_GET_ONE_FORM_USER_ID_IS_NULL_OR_EMPTY_MSG);
             	return new ResponseEntity<MobileApiResponse>(
                         new MobileApiResponse(USER_ID_IS_NULL_OR_EMPTY_MSG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403),
                         HttpStatus.BAD_REQUEST);
             }
             
             if(fundSeekerInputRequestResponse.getApplicationId() == null) {
             	logger.info(MOBILE_GET_ONE_FORM_APPLCATION_ID_IS_NULL_OR_EMPTY_MSG);
             	return new ResponseEntity<MobileApiResponse>(
                         new MobileApiResponse(APPLCATION_ID_IS_NULL_OR_EMPTY_MSG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403),
                         HttpStatus.BAD_REQUEST);
             }
            logger.info("Application Id for Getting director detail============>{}",fundSeekerInputRequestResponse.getApplicationId());
            ResponseEntity<LoansResponse> directorDetail = fundSeekerInputRequestService.getDirectorDetail(fundSeekerInputRequestResponse);
            LoansResponse loansResponse = directorDetail.getBody();
            if(!CommonUtils.isObjectNullOrEmpty(loansResponse.getData()) &&  (loansResponse.getStatus() == HttpStatus.OK.value())) {
            	return new ResponseEntity<MobileApiResponse>(new MobileApiResponse("Successfully get Data","true", loansResponse.getData(), MobileCustomizeResponse.SUCCESS200), HttpStatus.OK);	
            } else {
            	return new ResponseEntity<MobileApiResponse>(new MobileApiResponse("Data not found",CommonUtils.FALSE_LITERAL, null, MobileCustomizeResponse.SUCCESS204), HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error("Error while fetching director detail : ",e);
            return new ResponseEntity<MobileApiResponse>(
                    new MobileApiResponse(CommonUtils.SOMETHING_WENT_WRONG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.INTERNALSERVERERROR407),
                    HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/save_director_detail", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MobileApiResponse> saveDirectorDetail(@RequestBody FundSeekerInputRequestResponse fundSeekerInputRequestResponse)
            throws LoansException
    {
        try {
        	 if(fundSeekerInputRequestResponse.getUserId() == null) {
             	logger.info(MOBILE_GET_ONE_FORM_USER_ID_IS_NULL_OR_EMPTY_MSG);
             	return new ResponseEntity<MobileApiResponse>(
                         new MobileApiResponse(USER_ID_IS_NULL_OR_EMPTY_MSG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403),
                         HttpStatus.BAD_REQUEST);
             }
             
             if(fundSeekerInputRequestResponse.getApplicationId() == null) {
             	logger.info(MOBILE_GET_ONE_FORM_APPLCATION_ID_IS_NULL_OR_EMPTY_MSG);
             	return new ResponseEntity<MobileApiResponse>(
                         new MobileApiResponse(APPLCATION_ID_IS_NULL_OR_EMPTY_MSG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403),
                         HttpStatus.BAD_REQUEST);
             }

            ResponseEntity<LoansResponse> saveOrUpdateDirectorDetail = fundSeekerInputRequestService.saveOrUpdateDirectorDetail(fundSeekerInputRequestResponse);
            LoansResponse loansResponse = saveOrUpdateDirectorDetail.getBody();
            return new ResponseEntity<MobileApiResponse>(new MobileApiResponse("Successfully saved data","true", loansResponse.getData(), MobileCustomizeResponse.SUCCESS200), HttpStatus.OK);	

        } catch (Exception e) {
            logger.error("Error while saving director detail : ",e);
            return new ResponseEntity<MobileApiResponse>(
                    new MobileApiResponse(CommonUtils.SOMETHING_WENT_WRONG, CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.INTERNALSERVERERROR407),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/match", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MobileApiResponse> callMatchengine(@RequestBody MobileLoanRequest mobileLoanRequest) throws LoansException {
        try {
            if(CommonUtils.isObjectNullOrEmpty(mobileLoanRequest.getUserId())) {
                return new ResponseEntity<MobileApiResponse>(
                        new MobileApiResponse("Invalid Request,UserID is null or Empty !!", CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403), HttpStatus.OK);
            }
            if(CommonUtils.isObjectNullOrEmpty(mobileLoanRequest.getApplicationId())) {
                return new ResponseEntity<MobileApiResponse>(
                        new MobileApiResponse("Invalid Request,ApplicationId is null or Empty !!", CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.ERROR403), HttpStatus.OK);
            }
            logger.info("Application Id for Getting============>{}",mobileLoanRequest.getApplicationId());

            LoansResponse callMatchEngineClient = fundSeekerInputRequestService.callMatchEngineClient(mobileLoanRequest.getApplicationId(),mobileLoanRequest.getUserId(),mobileLoanRequest.getBusinessTypeId(),mobileLoanRequest.getIsNbfcUser());
            logger.info("Response from Matchengine for mobile ==>{}",callMatchEngineClient.toString());
            {
                if(callMatchEngineClient.getStatus()== HttpStatus.BAD_REQUEST.value()){
                    return  new ResponseEntity<MobileApiResponse>(new MobileApiResponse(callMatchEngineClient.getMessage(),CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.SUCCESS200), HttpStatus.OK);
                } else if(callMatchEngineClient.getStatus()==HttpStatus.OK.value()){
                    return  new ResponseEntity<MobileApiResponse>(new MobileApiResponse(callMatchEngineClient.getMessage(),"true", MobileCustomizeResponse.SUCCESS200), HttpStatus.OK);
                } else {
                    return  new ResponseEntity<MobileApiResponse>(new MobileApiResponse(callMatchEngineClient.getMessage(),CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.INTERNALSERVERERROR407), HttpStatus.OK);
                }
            }
        } catch (Exception e) {
            logger.error("Error while Calling Connect Client after Oneform Submit : ",e);
            return  new ResponseEntity<MobileApiResponse>(new MobileApiResponse(CommonUtils.SOMETHING_WENT_WRONG,CommonUtils.FALSE_LITERAL, MobileCustomizeResponse.INTERNALSERVERERROR407), HttpStatus.OK);
        }
    }
}
