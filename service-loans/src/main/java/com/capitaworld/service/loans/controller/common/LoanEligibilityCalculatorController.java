package com.capitaworld.service.loans.controller.common;

import org.json.simple.JSONObject;
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
import org.springframework.web.bind.annotation.RestController;

import com.capitaworld.service.loans.model.CMADetailResponse;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.common.HomeLoanEligibilityRequest;
import com.capitaworld.service.loans.model.common.LAPEligibilityRequest;
import com.capitaworld.service.loans.model.common.PersonalLoanEligibilityRequest;
import com.capitaworld.service.loans.service.common.LoanEligibilityCalculatorService;
import com.capitaworld.service.loans.utils.CommonDocumentUtils;
import com.capitaworld.service.loans.utils.CommonUtils;

@RestController
@RequestMapping("/loan_eligibility")
public class LoanEligibilityCalculatorController {

	private static final Logger logger = LoggerFactory.getLogger(LoanEligibilityCalculatorController.class);

	private static final String MSG_REQUEST_OBJECT = "Request Object ==>";
	private static final String INVALID_REQUEST = "Invalid Request";
	private static final String GET_ELIGIBLE_TENURE = "getEligibleTenure";
	private static final String GET_ELIGIBLE_TENURE_PL = "getEligibleTenurePL";
	private static final String GET_ELIGIBLE_TENURE_LAP = "getEligibleTenureLAP";

	@Autowired
	private LoanEligibilityCalculatorService loanEligibilityCalculatorService;

	/*@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public String getPing() {
		logger.info("Ping success");
		return "Ping Succeed";
	}*/

	// Home Loan Calculation Starts
	@RequestMapping(value = "${hl}/calc_min_max", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> calcMinMax(@RequestBody HomeLoanEligibilityRequest homeLoanRequest) {
		CommonDocumentUtils.startHook(logger, "calcMinMax");
		try {
			LoansResponse response = isHomeLoanRequestIsValid(homeLoanRequest, false);
			if (response.getStatus().equals(HttpStatus.BAD_REQUEST.value())) {
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			}

			JSONObject minMaxBySalarySlab = loanEligibilityCalculatorService.getMinMaxBySalarySlab(homeLoanRequest);
			if (minMaxBySalarySlab == null) {
				response.setMessage(CommonUtils.INVALID_AGE);
				response.setData(CommonUtils.YOU_ARE_NOT_ELIGIBLE_FOR_HOME_LOAN);
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else if (minMaxBySalarySlab.isEmpty()) {
				response.setMessage("Invalid");
				response.setData(CommonUtils.YOU_ARE_NOT_ELIGIBLE_FOR_HOME_LOAN);
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				response.setData(minMaxBySalarySlab);
				response.setStatus(HttpStatus.OK.value());
			}
			CommonDocumentUtils.endHook(logger, "calcMinMax");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while calculating Loan eligibility for Home Loans : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "${hl}/get_eligible_tenure", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getEligibleTenure(@RequestBody HomeLoanEligibilityRequest homeLoanRequest) {
		CommonDocumentUtils.startHook(logger, GET_ELIGIBLE_TENURE);
		try {
			LoansResponse response = isHomeLoanRequestIsValid(homeLoanRequest, false);
			if (response.getStatus().equals(HttpStatus.BAD_REQUEST.value())) {
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			}

			CommonDocumentUtils.endHook(logger, GET_ELIGIBLE_TENURE);
			Integer tenure = loanEligibilityCalculatorService.calculateTenure(homeLoanRequest,
					CommonUtils.LoanType.HOME_LOAN.getValue());
			if (tenure == null) {
				response.setMessage(CommonUtils.INVALID_AGE);
				response.setData(CommonUtils.YOU_ARE_NOT_ELIGIBLE_FOR_HOME_LOAN);
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				response.setData(tenure);
				response.setStatus(HttpStatus.OK.value());
			}
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);

		} catch (Exception e) {
			CommonDocumentUtils.endHook(logger, GET_ELIGIBLE_TENURE);
			logger.error("Error while calculating Eligible Tenure for Home Loans : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "${hl}/calc_home_loan_amount", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> calcHomeLoanAmount(@RequestBody HomeLoanEligibilityRequest homeLoanRequest) {
		CommonDocumentUtils.startHook(logger, "calcHomeLoanAmount");
		try {
			LoansResponse response = isHomeLoanRequestIsValid(homeLoanRequest, true);
			if (response.getStatus().equals(HttpStatus.BAD_REQUEST.value())) {
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			}

			JSONObject jsonObject = loanEligibilityCalculatorService.calcHomeLoanAmount(homeLoanRequest);
			if (jsonObject == null) {
				response.setMessage(CommonUtils.INVALID_AGE);
				response.setData(CommonUtils.YOU_ARE_NOT_ELIGIBLE_FOR_HOME_LOAN);
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				response.setData(jsonObject);
				response.setStatus(HttpStatus.OK.value());
			}
			CommonDocumentUtils.endHook(logger, "calcHomeLoanAmount");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while calculating Loan eligibility for Home Loans : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}
	// Home Loan Calculation Ends

	// Personal Loan Calculation Starts
	@RequestMapping(value = "${pl}/get_eligible_tenure", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getEligibleTenurePL(
			@RequestBody PersonalLoanEligibilityRequest eligibilityRequest) {
		CommonDocumentUtils.startHook(logger, GET_ELIGIBLE_TENURE_PL);
		try {
			LoansResponse response = isPersonalLoanRequestIsValid(eligibilityRequest);
			if (response.getStatus().equals(HttpStatus.BAD_REQUEST.value())) {
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			}

			CommonDocumentUtils.endHook(logger, GET_ELIGIBLE_TENURE_PL);
			Integer tenure = loanEligibilityCalculatorService.calculateTenure(eligibilityRequest,
					CommonUtils.LoanType.PERSONAL_LOAN.getValue());
			if (tenure == null) {
				response.setMessage(CommonUtils.INVALID_AGE);
				response.setData(CommonUtils.YOU_ARE_NOT_ELIGIBLE_FOR_PERSONAL_LOAN);
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				response.setData(tenure);
				response.setStatus(HttpStatus.OK.value());
			}
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);

		} catch (Exception e) {
			CommonDocumentUtils.endHook(logger, GET_ELIGIBLE_TENURE_PL);
			logger.error("Error while calculating Eligible Tenure for Personal Loans : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "${pl}/calc_min_max", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> calcMinMaxPL(@RequestBody PersonalLoanEligibilityRequest eligibilityRequest) {
		CommonDocumentUtils.startHook(logger, "calcMinMaxPL");
		try {
			LoansResponse response = isPersonalLoanRequestIsValid(eligibilityRequest);
			if (response.getStatus().equals(HttpStatus.BAD_REQUEST.value())) {
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			}

			JSONObject minMaxBySalarySlab = loanEligibilityCalculatorService
					.calcMinMaxForPersonalLoan(eligibilityRequest);
			if (minMaxBySalarySlab == null) {
				response = new LoansResponse(CommonUtils.INVALID_AGE);
				response.setData(CommonUtils.YOU_ARE_NOT_ELIGIBLE_FOR_PERSONAL_LOAN);
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				response = new LoansResponse(CommonUtils.SUCCESS);
				response.setData(minMaxBySalarySlab);
				response.setStatus(HttpStatus.OK.value());
			}
			CommonDocumentUtils.endHook(logger, "calcMinMaxPL");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while calculating Loan eligibility for Personal Loans : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	// Personal Loan Calculation Ends

	// LAP Calculation Starts
	@RequestMapping(value = "${lap}/get_eligible_tenure", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> getEligibleTenureLAP(@RequestBody LAPEligibilityRequest eligibilityRequest) {
		CommonDocumentUtils.startHook(logger, GET_ELIGIBLE_TENURE_LAP);
		try {
			LoansResponse response = isLAPRequestIsValid(eligibilityRequest);
			if (response.getStatus().equals(HttpStatus.BAD_REQUEST.value())) {
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			}

			CommonDocumentUtils.endHook(logger, GET_ELIGIBLE_TENURE_LAP);
			Integer tenure = loanEligibilityCalculatorService.calculateTenure(eligibilityRequest,
					CommonUtils.LoanType.LAP_LOAN.getValue());
			if (tenure == null) {
				response = new LoansResponse(CommonUtils.INVALID_AGE);
				response.setData("You are not eligible for Loan Against Properties.");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				response = new LoansResponse(CommonUtils.SUCCESS);
				response.setData(tenure);
				response.setStatus(HttpStatus.OK.value());
			}
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);

		} catch (Exception e) {
			CommonDocumentUtils.endHook(logger, GET_ELIGIBLE_TENURE_LAP);
			logger.error("Error while calculating Eligible Tenure for Loan Against Properties. : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "${lap}/calc_min_max", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> calcMinMaxLAP(@RequestBody LAPEligibilityRequest eligibilityRequest) {
		CommonDocumentUtils.startHook(logger, "calcMinMaxLAP");
		try {
			LoansResponse response = isLAPRequestIsValid(eligibilityRequest);
			if (response.getStatus().equals(HttpStatus.BAD_REQUEST.value())) {
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			}

			JSONObject minMaxBySalarySlab = loanEligibilityCalculatorService.calcMinMaxForLAP(eligibilityRequest);
			if (minMaxBySalarySlab == null) {
				response = new LoansResponse(CommonUtils.INVALID_AGE);
				response.setData("You are not eligible for Loan Against Properties.");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				response = new LoansResponse(CommonUtils.SUCCESS);
				response.setData(minMaxBySalarySlab);
				response.setStatus(HttpStatus.OK.value());
			}
			CommonDocumentUtils.endHook(logger, "calcMinMaxLAP");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while calculating Loan eligibility for LAP : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	@RequestMapping(value = "${lap}/calc_lap_amount", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoansResponse> calcLAP(@RequestBody LAPEligibilityRequest eligibilityRequest) {
		CommonDocumentUtils.startHook(logger, "calcLAP");
		try {
			LoansResponse response = isLAPRequestIsValid(eligibilityRequest);
			if (response.getStatus().equals(HttpStatus.BAD_REQUEST.value())) {
				return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
			}
			JSONObject jsonObject = loanEligibilityCalculatorService.calcLAPAmount(eligibilityRequest);
			if (jsonObject == null) {
				response = new LoansResponse(CommonUtils.INVALID_AGE);
				response.setData("You are not eligible for Loan Against Property");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				response = new LoansResponse(CommonUtils.SUCCESS);
				response.setData(jsonObject);
				response.setStatus(HttpStatus.OK.value());
			}
			CommonDocumentUtils.endHook(logger, "calcLAP");
			return new ResponseEntity<LoansResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error while calculating Loan eligibility for LAP : ",e);
			return new ResponseEntity<LoansResponse>(
					new LoansResponse(CommonUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.OK);
		}
	}

	private static boolean isValidRequest(Integer propertyType) {
		if (CommonUtils.isObjectNullOrEmpty(propertyType)) {
			return false;
		}
		if (!(propertyType.intValue() == CommonUtils.PropertyType.RESIDENTIAL)
				&& !(propertyType.intValue() == CommonUtils.PropertyType.COMMERCIAL)
				&& !(propertyType.intValue() == CommonUtils.PropertyType.INDUSTRIAL)
				&& !(propertyType.intValue() == CommonUtils.PropertyType.PLOT)) {
			return false;
		}
		return true;
	}

	private static LoansResponse isHomeLoanRequestIsValid(HomeLoanEligibilityRequest homeLoanRequest, boolean isMVSV) {
		final String MSG = CommonUtils.YOU_ARE_NOT_ELIGIBLE_FOR_HOME_LOAN;
		if (!CommonUtils.isObjectNullOrEmpty(homeLoanRequest)) {
			logger.info(MSG_REQUEST_OBJECT + homeLoanRequest.toString());
		}

		LoansResponse response = null;
		boolean isNull = CommonUtils.isObjectListNull(homeLoanRequest.getEmploymentType(), homeLoanRequest.getIncome(),
				homeLoanRequest.getDateOfBirth());
		if (isNull) {
			response = new LoansResponse(INVALID_REQUEST, HttpStatus.BAD_REQUEST.value());
			response.setData(MSG);
			return response;
		}

		if (!CommonUtils.isObjectNullOrEmpty(homeLoanRequest.getObligation()) && homeLoanRequest.getIncome() <= homeLoanRequest.getObligation() ) {
				response = new LoansResponse(CommonUtils.OBLIGATION_MUST_BE_LESS_THAN_INCOME, HttpStatus.BAD_REQUEST.value());
				response.setData(MSG);
				return response;
		}

		if (homeLoanRequest.getIncome() < 9000) {
			if (homeLoanRequest.getEmploymentType().intValue() == CommonUtils.EmployementType.SALARIED) {
				response = new LoansResponse("Minimum Salary should be 9000", HttpStatus.BAD_REQUEST.value());
			} else {
				response = new LoansResponse("Minimum Cash Profit should be 9000", HttpStatus.BAD_REQUEST.value());
			}
			response.setData(MSG);
			return response;
		}

		if (isMVSV) {
			isNull = CommonUtils.isObjectListNull(homeLoanRequest.getStampValue(), homeLoanRequest.getMarketValue());
			if (isNull) {
				response = new LoansResponse("Market Value and Stamp value must not be empty.",
						HttpStatus.BAD_REQUEST.value());
				response.setData(MSG);
				return response;
			}
		}
		return new LoansResponse(CommonUtils.SUCCESS, HttpStatus.OK.value());
	}

	private static LoansResponse isPersonalLoanRequestIsValid(PersonalLoanEligibilityRequest eligibilityRequest) {
		final String MSG = CommonUtils.YOU_ARE_NOT_ELIGIBLE_FOR_PERSONAL_LOAN;
		if (!CommonUtils.isObjectNullOrEmpty(eligibilityRequest)) {
			logger.info(MSG_REQUEST_OBJECT + eligibilityRequest.toString());
		}

		LoansResponse response = null;
		boolean isNull = CommonUtils.isObjectListNull(eligibilityRequest.getDateOfBirth(),
				eligibilityRequest.getIncome(), eligibilityRequest.getConstitution(),
				eligibilityRequest.getReceiptMode());
		if (isNull) {
			response = new LoansResponse(INVALID_REQUEST, HttpStatus.BAD_REQUEST.value());
			response.setData(MSG);
			return response;
		}
		if (!eligibilityRequest.getReceiptMode().equals(CommonUtils.ReceiptMode.BANK)) {
			response = new LoansResponse("only Bank is allowed as Receipt Mode.", HttpStatus.BAD_REQUEST.value());
			response.setData(MSG);
			return response;
		}
		if (!eligibilityRequest.getConstitution().equals(CommonUtils.EmployerConstitution.ANYOTHER)
				&& !eligibilityRequest.getConstitution()
						.equals(CommonUtils.EmployerConstitution.PARTNERSHIP_PROPRIETORSHIP)) {
			response = new LoansResponse("Constitution Must be ANYOTHER or PARTNERSHIP/PROPRIETORSHIP",
					HttpStatus.BAD_REQUEST.value());
			response.setData(MSG);
			return response;
		}

		if (!CommonUtils.isObjectNullOrEmpty(eligibilityRequest.getObligation())) {
			if (eligibilityRequest.getIncome() <= eligibilityRequest.getObligation()) {
				response = new LoansResponse(CommonUtils.OBLIGATION_MUST_BE_LESS_THAN_INCOME, HttpStatus.BAD_REQUEST.value());
				response.setData(MSG);
				return response;
			}
		}
		if (eligibilityRequest.getIncome() < 10000) {
			response = new LoansResponse("Minimum Salary should be 10000", HttpStatus.BAD_REQUEST.value());
			response.setData(MSG);
			return response;
		}
		return new LoansResponse(CommonUtils.SUCCESS, HttpStatus.OK.value());
	}

	private static LoansResponse isLAPRequestIsValid(LAPEligibilityRequest eligibilityRequest) {
		final String MSG = "You are not eligible for Loan Against Property";
		if (!CommonUtils.isObjectNullOrEmpty(eligibilityRequest)) {
			logger.info(MSG_REQUEST_OBJECT + eligibilityRequest.toString());
		}

		LoansResponse response = null;
		boolean isNull = CommonUtils.isObjectListNull(eligibilityRequest.getDateOfBirth(),
				eligibilityRequest.getIncome(), eligibilityRequest.getEmploymentType(),
				eligibilityRequest.getPropertyType());
		if (isNull) {
			response = new LoansResponse(INVALID_REQUEST, HttpStatus.BAD_REQUEST.value());
			response.setData(MSG);
			return response;
		}
		boolean validRequest = isValidRequest(eligibilityRequest.getPropertyType());
		if (!validRequest) {
			response = new LoansResponse("Invalid PropertyType", HttpStatus.BAD_REQUEST.value());
			response.setData(MSG);
		}
		if (!CommonUtils.isObjectNullOrEmpty(eligibilityRequest.getObligation())) {
			if (eligibilityRequest.getIncome() <= eligibilityRequest.getObligation()) {
				response = new LoansResponse(CommonUtils.OBLIGATION_MUST_BE_LESS_THAN_INCOME, HttpStatus.BAD_REQUEST.value());
				response.setData(MSG);
				return response;
			}
		}

		if (eligibilityRequest.getPropertyType().intValue() == CommonUtils.PropertyType.RESIDENTIAL
				&& eligibilityRequest.getEmploymentType().intValue() == CommonUtils.EmployementType.SALARIED) {
			if (eligibilityRequest.getIncome() < 12000) {
				response = new LoansResponse("Minimum Salary should be 12000", HttpStatus.BAD_REQUEST.value());
				response.setData(MSG);
				return response;
			}
		} else {
			if (eligibilityRequest.getIncome() < 16667) {
				response = new LoansResponse("Minimum Cash Profit should be 16667", HttpStatus.BAD_REQUEST.value());
				response.setData(MSG);
				return response;
			}
		}

		return new LoansResponse(CommonUtils.SUCCESS, HttpStatus.OK.value());
	}
	
	@RequestMapping(value="/getCMADetail/{applicationId}" ,method =RequestMethod.POST) 
	public CMADetailResponse getCmaDetail(@PathVariable("applicationId") Long applicationId) {
		logger.info("Enter in getCmaDetail()========>" + applicationId);
		CMADetailResponse cmaDetailResponse=null;
		try {
		cmaDetailResponse =loanEligibilityCalculatorService.getCMADetail(applicationId);
		} catch (NullPointerException e) {
			logger.error("Exception in getCmaDetail()========>",e);
		}
		logger.info("Exit from getCmaDetail()========>");
		return cmaDetailResponse;
	}
	
	@RequestMapping(value="/getCMADetailForEligibility/{applicationId}" ,method =RequestMethod.GET) 
	public CMADetailResponse getCmaDetailForEligibility(@PathVariable("applicationId") Long applicationId) {
		logger.info("Enter in getCmaDetail()========>" + applicationId);
		CMADetailResponse cmaDetailResponse=null;
		try {
			logger.info("==================================>1 in Controller");
		cmaDetailResponse =loanEligibilityCalculatorService.getCMADetail(applicationId);
		logger.info("cmaDetailResponse==================================>1 in Controlle=={}",cmaDetailResponse.toString());
		} catch (NullPointerException e) {
			logger.error("Exception in getCmaDetail()========>",e);
		}
		logger.info("Exit from getCmaDetail()========>");
		return cmaDetailResponse;
	}
	// LAP Calculation Ends

}
