package com.capitaworld.service.loans.domain.fundprovider;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the fp_term_loan_details database table.
 * 
 */
@Entity
@Table(name = "fp_term_loan_details_temp")
public class TermLoanParameterTemp extends ProductMasterTemp implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@OneToOne
	@JoinColumn(name = "fp_product_id")
	private ProductMasterTemp fpProductId;

	@Column(name = "created_by")
	private Long createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date")
	private Date createdDate;

	private Integer currency;

	private Integer denomination;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "is_collateral_display")
	private Boolean isCollateralDisplay = false;

	@Column(name = "is_collateral_mandatory")
	private Boolean isCollateralMandatory = false;

	@Column(name = "is_credit_rating_display")
	private Boolean isCreditRatingDisplay = false;

	@Column(name = "is_credit_rating_mandatory")
	private Boolean isCreditRatingMandatory = false;

	@Column(name = "is_debt_equity_display")
	private Boolean isDebtEquityDisplay = false;

	@Column(name = "is_debt_equity_mandatory")
	private Boolean isDebtEquityMandatory = false;

	@Column(name = "is_establishment_display")
	private Boolean isEstablishmentDisplay = false;

	@Column(name = "is_establishment_mandatory")
	private Boolean isEstablishmentMandatory = false;

	@Column(name = "is_geographical_display")
	private Boolean isGeographicalDisplay = false;

	@Column(name = "is_geographical_mandatory")
	private Boolean isGeographicalMandatory = false;

	@Column(name = "is_industry_sector_display")
	private Boolean isIndustrySectorDisplay = false;

	@Column(name = "is_industry_sector_mandatory")
	private Boolean isIndustrySectorMandatory = false;

	@Column(name = "is_investment_size_display")
	private Boolean isInvestmentSizeDisplay = false;

	@Column(name = "is_investment_size_mandatory")
	private Boolean isInvestmentSizeMandatory = false;

	@Column(name = "is_networth_display")
	private Boolean isNetworthDisplay = false;

	@Column(name = "is_networth_mandatory")
	private Boolean isNetworthMandatory = false;

	@Column(name = "is_past_year_turnover_display")
	private Boolean isPastYearTurnoverDisplay = false;

	@Column(name = "is_past_year_turnover_mandatory")
	private Boolean isPastYearTurnoverMandatory = false;

	@Column(name = "is_profitability_history_display")
	private Boolean isProfitabilityHistoryDisplay = false;

	@Column(name = "is_profitability_history_mandatory")
	private Boolean isProfitabilityHistoryMandatory = false;

	@Column(name = "is_tenure_display")
	private Boolean isTenureDisplay = false;

	@Column(name = "is_tenure_mandatory")
	private Boolean isTenureMandatory = false;

	@Column(name = "Long_term_credit_rating")
	private Integer LongTermCreditRating;

	@Column(name = "max_age_establishment")
	private Integer maxAgeEstablishment;

	@Column(name = "max_collateral")
	private BigDecimal maxCollateral;

	@Column(name = "max_debt_equity")
	private BigDecimal maxDebtEquity;

	@Column(name = "max_invest_size")
	private BigDecimal maxInvestSize;

	@Column(name = "max_networth")
	private BigDecimal maxNetworth;

	@Column(name = "max_past_turnover")
	private BigDecimal maxPastTurnover;

	@Column(name = "max_tenure")
	private BigDecimal maxTenure;

	@Column(name = "min_age_establishment")
	private Integer minAgeEstablishment;

	@Column(name = "min_collateral")
	private BigDecimal minCollateral;

	@Column(name = "min_debt_equity")
	private BigDecimal minDebtEquity;

	@Column(name = "min_invest_size")
	private BigDecimal minInvestSize;

	@Column(name = "min_networth")
	private BigDecimal minNetworth;

	@Column(name = "min_past_turnover")
	private BigDecimal minPastTurnover;

	@Column(name = "min_tenure")
	private BigDecimal minTenure;

	@Column(name = "modified_by")
	private Long modifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_date")
	private Date modifiedDate;

	@Column(name = "profitability_history")
	private Integer profitabilityHistory;

	@Column(name = "short_term_credit_rating")
	private Integer shortTermCreditRating;

	@Column(name = "uninterested_industry")
	private Long uninterestedIndustry;
	
	@Column(name="is_uninterested_industry_display")
	private Boolean isUnInterestedIndustryDisplay=false;

	@Column(name="is_uninterested_industry_mandatory")
	private Boolean isUnInterestedIndustryMandatory=false;

	@Column(name="min_current_ratio")
	private BigDecimal minCurrentRatio;

	@Column(name="max_current_ratio")
	private BigDecimal maxCurrentRatio;

	@Column(name="is_current_ratio_display")
	private Boolean isCurrentRatioDisplay = false;

	@Column(name="is_current_ratio_mandatory")
	private Boolean isCurrentRatioMandatory = false;

	@Column(name="min_interest_coverage")
	private BigDecimal minInterestCoverage;

	@Column(name="max_interest_coverage")
	private BigDecimal maxInterestCoverage;

	@Column(name="is_interest_coverage_display")
	private Boolean isInterestCoverageDisplay = false;

	@Column(name="is_interest_coverage_mandatory")
	private Boolean isInterestCoverageMandatory = false;

	@Column(name="min_tol_tnw")
	private BigDecimal minTolTnw;

	@Column(name="max_tol_tnw")
	private BigDecimal maxTolTnw;

	@Column(name="is_tol_tnw_display")
	private Boolean isTolTnwDisplay = false;

	@Column(name="is_tol_tnw_mandatory")
	private Boolean isTolTnwMandatory = false;

	@Column(name="min_turnover_ratio")
	private BigDecimal minTurnoverRatio;

	@Column(name="max_turnover_ratio")
	private BigDecimal maxTurnoverRatio;

	@Column(name="is_turnover_ratio_display")
	private Boolean isTurnoverRatioDisplay = false;

	@Column(name="is_turnover_ratio_mandatory")
	private Boolean isTurnoverRatioMandatory = false;

	@Column(name="min_gross_cash_accurals_ratio")
	private BigDecimal minGrossCashAccuralsRatio;

	@Column(name="max_gross_cash_accurals_ratio")
	private BigDecimal maxGrossCashAccuralsRatio;

	@Column(name="is_gross_cash_accurals_ratio_display")
	private Boolean isGrossCashAccuralsRatioDisplay = false;

	@Column(name="is_gross_cash_accurals_ratio_mandatory")
	private Boolean isGrossCashAccuralsRatioMandatory = false;

	@Column(name="min_customer_concentration")
	private BigDecimal minCustomerConcentration;

	@Column(name="max_customer_concentration")
	private BigDecimal maxCustomerConcentration;

	@Column(name="is_customer_concentration_display")
	private Boolean isCustomerConcentrationDisplay = false;

	@Column(name="is_customer_concentration_mandatory")
	private Boolean isCustomerConcentrationMandatory =false;

	@Column(name="min_risk_model_score")
	private Integer minRiskModelScore;

	@Column(name="max_risk_model_score")
	private Integer maxRiskModelScore;

	@Column(name="is_risk_model_score_display")
	private Boolean isRiskModelScoreDisplay = false;

	@Column(name="is_risk_model_score_mandatory")
	private Boolean isRiskModelScoreMandatory = false;

	@Column(name="net_worth")
	private Integer netWorth;

	@Column(name="min_cheque_bounced")
	private Integer minChequeBounced;

	@Column(name="max_cheque_bounced")
	private Integer maxChequeBounced;

	@Column(name="is_cheque_bounced_display")
	private Boolean isChequeBouncedDisplay = false;

	@Column(name="is_cheque_bounced_mandatory")
	private Boolean isChequeBouncedMandatory = false;

	@Column(name="min_cheque_bounced_last_six_months")
	private Integer minChequeBouncedLastSixMonths;

	@Column(name="max_cheque_bounced_last_six_months")
	private Integer maxChequeBouncedLastSixMonths;

	@Column(name="is_cheque_bounced_last_six_months_display")
	private Boolean isChequeBouncedLastSixMonthsDisplay = false;

	@Column(name="is_cheque_bounced_last_six_months_mandatory")
	private Boolean isChequeBouncedLastSixMonthsMandatory = false;

	@Column(name="ddr_flow")
	private Integer ddrFlow;

	@Column(name="individual_cibil")
	private Integer individualCibil;

	@Column(name="is_individual_cibil_display")
	private Boolean isIndividualCibilDisplay = false;

	@Column(name="is_individual_cibil_mandatory")
	private Boolean isIndividualCibilMandatory = false;

	@Column(name="commercial_cibil")
	private Integer commercialCibil;

	@Column(name="is_Commercial_cibil_display")
	private Boolean isCommercialCibilDisplay = false;

	@Column(name="is_Commercial_cibil_mandatory")
	private Boolean isCommercialCibilMandatory = false;

	/**
	 * @return the orgId
	 */
	@Column(name="min_cgtmse_coverage")
	private BigDecimal minCgtmseCoverage;

	@Column(name="max_cgtmse_coverage")
	private BigDecimal maxCgtmseCoverage;

	@Column(name="is_cgtmse_coverage_display")
	private Boolean isCgtmseCoverageDisplay = false;

	@Column(name="is_cgtmse_coverage_mandatory")
	private Boolean isCgtmseCoverageMandatory = false;

	@Column(name="is_msme_funding_display")
	private Boolean isMsmeFundingDisplay = false;

	@Column(name="is_msme_funding_mandatory")
	private Boolean isMsmeFundingMandatory = false;
	
	@Column(name="cgtmse_coverage")
	private Integer cgtmseCoverage;


	//-----------------------added eligibility method for product
	@Column(name="assessment_method_id")
	private Integer assessmentMethodId;
	
	@Column(name="cash_margin")
	private Integer cashMargin;
	
	@Column(name="dscr")
	private BigDecimal dscr;
	
	@Column(name="payback_period")
	private Integer paybackPeriod;
	
	@Column(name="max_drop_in_turnover")
	private BigDecimal maxDropInTurnover;

	@Column(name="is_max_drop_in_turnover_display")
	private Boolean isMaxDropInTurnoverDisplay = false;

	@Column(name="is_max_drop_in_turnover_mandatory")
	private Boolean isMaxDropInTurnoverMandatory = false;


	@Column(name="min_utilisation_percentage")
	private BigDecimal minUtilisationPercentage;

	@Column(name="max_utilisation_percentage")
	private BigDecimal maxUtilisationPercentage;

	@Column(name="is_utilisation_percentage_display")
	private Boolean isUtilisationPercentageDisplay = false;

	@Column(name="is_utilisation_percentage_mandatory")
	private Boolean isUtilisationPercentageMandatory = false;

	@Column(name="min_credit_summation")
	private BigDecimal minCreditSummation;

	@Column(name="max_credit_summation")
	private BigDecimal maxCreditSummation;

	@Column(name="is_credit_summation_display")
	private Boolean isCreditSummationDisplay = false;

	@Column(name="is_credit_summation_mandatory")
	private Boolean isCreditSummationMandatory = false;

	@Column(name="min_collateral_coverage")
	private BigDecimal minCollateralCoverage;

	@Column(name="max_collateral_coverage")
	private BigDecimal maxCollateralCoverage;

	@Column(name="is_collateral_coverage_display")
	private Boolean isCollateralCoverageDisplay = false;

	@Column(name="is_collateral_coverage_mandatory")
	private Boolean isCollateralCoverageMandatory = false;

	@Column(name="new_tol_tnw")
	private BigDecimal newTolTnw;

	@Column(name="new_collateral_coverage")
	private BigDecimal newCollateralCoverage;

	@Column(name="is_new_collateral_coverage")
	private Boolean isNewCollateralCoverageCheck = false;

	@Column(name="is_new_tol_tnw_check")
	private Boolean isNewTolTnwCheck = false;

	@Column(name="kotak_sub_parameter")
	private Integer kotakSubParameter ;
	
	@Column(name="is_wc_req_check")
	private Boolean wcReqCheck = false;
	@Column(name="is_new_dscr_check")
	private Boolean isNewDscrCheck;
	
	@Column(name="new_dscr_check")
	private BigDecimal newDscrCheck;

	// projected sales
	@Column(name = "is_new_historic_sales")
	private Boolean isNewHistoricSales = false;

	@Column(name = "new_historic_sales")
	private BigDecimal newHistoricSales;

	@Column(name = "is_historic_growth")
	private Boolean isHistoricGrowth = false;

	@Column(name = "new_historic_growth")
	private BigDecimal newHistoricGrowth;
	
	@Column(name="min_additional_loan")
	private BigDecimal minAdditionalLoan;

	@Column(name="max_additional_loan")
	private BigDecimal maxAdditionalLoan;
	
	@Column(name="min_total_loan")
	private BigDecimal minTotalLoan;

	@Column(name="max_total_loan")
	private BigDecimal maxTotalLoan;	

	@Column(name = "is_conservative_ratio_display")
	private Boolean isConservativeRatioDisplay=false;

	@Column(name = "is_conservative_ratio_mandatory")
	private Boolean isConservativeRatioMandatory=false;
	
	@Column(name = "max_conservative_ratio")
	private BigDecimal maxConservativeRatio;
	
	@Column(name = "min_conservative_ratio")
	private BigDecimal minConservativeRatio;
	
	@Column(name="main_individual_cibil")
	private Integer mainIndividualCibil;

	@Column(name="is_main_individual_cibil_display")
	private Boolean isMainIndividualCibilDisplay = false;

	@Column(name="is_main_individual_cibil_mandatory")
	private Boolean isMainIndividualCibilMandatory = false;

    @Column(name="commercial_cibil_for_3_month")
    private Integer commercialCibilFor3Month;

    @Column(name="is_Commercial_cibil_for_3_month_display")
    private Boolean isCommercialCibilFor3MonthDisplay = false;

    @Column(name="is_Commercial_cibil_for_3_month_mandatory")
    private Boolean isCommercialCibilFor3MonthMandatory = false;
    
	@Column(name="manufacturing")
	private BigDecimal manufacturing;
	
	@Column(name="service")
	private BigDecimal service;
	
	@Column(name="trading")
	private BigDecimal trading;
	/* ends here */
	
	@Column(name="promoter_contri")
	private BigDecimal promotorContri;
	
	//co-origination new parameter
    @Column(name = "is_min_no_credit_transaction_display")
	private Boolean isMinNoCreditTransactionDisplay=false;

	@Column(name = "is_min_no_credit_transaction_mandatory")
	private Boolean isMinNoCreditTransactionMandatory=false;
	
	@Column(name = "max_no_credit_transaction")
	private BigDecimal maxNoCreditTransaction;
	
	@Column(name = "min_no_credit_transaction")
	private BigDecimal minNoCreditTransaction;
	
	
	
	@Column(name = "is_min_no_overall_transaction_display")
	private Boolean isMinNoOverallTransactionDisplay=false;

	@Column(name = "is_min_no_overall_transaction_mandatory")
	private Boolean isMinNoOverallTransactionMandatory=false;
	
	@Column(name = "max_no_overall_transaction")
	private BigDecimal maxNoOverallTransaction;
	
	@Column(name = "min_no_overall_transaction")
	private BigDecimal minNoOverallTransaction;
	
	
	
	@Column(name = "is_min_sales_growth_display")
	private Boolean isMinSalesGrowthDisplay=false;

	@Column(name = "is_min_sales_growth_mandatory")
	private Boolean isMinSalesGrowthMandatory=false;
	
	@Column(name = "max_sales_growth")
	private BigDecimal maxSalesGrowth;
	
	@Column(name = "min_sales_growth")
	private BigDecimal minSalesGrowth ;
	
	
	
	
	@Column(name = "is_min_cash_profit_growth_display")
	private Boolean isMinCashProfitGrowthDisplay=false;

	@Column(name = "is_min_cash_profit_growth_mandatory")
	private Boolean isMinCashProfitGrowthMandatory=false;
	
	@Column(name = "max_cash_profit_growth")
	private BigDecimal maxCashProfitGrowth;
	
	@Column(name = "min_cash_profit_growth")
	private BigDecimal minCashProfitGrowth ;
	
	
	
	@Column(name = "is_min_monthly_avg_bal_display")
	private Boolean isMinMonthlyAvgBalDisplay =false;

	@Column(name = "is_min_monthly_avg_bal_mandatory")
	private Boolean isMinMonthlyAvgBalMandatory=false;
	
	@Column(name = "max_monthly_avg_bal")
	private BigDecimal maxMonthlyAvgBal;
	
	@Column(name = "min_monthly_avg_bal")
	private BigDecimal minMonthlyAvgBal ;
	
	
	@Column(name = "is_min_no_debit_transaction_display")
	private Boolean isMinNoDebitTransactionDisplay =false;

	@Column(name = "is_min_no_debit_transaction_mandatory")
	private Boolean isMinNoDebitTransactionMandatory=false;
	
	@Column(name = "max_no_debit_transaction")
	private BigDecimal maxNoDebitTransaction;
	
	@Column(name = "min_no_debit_transaction")
	private BigDecimal minNoDebitTransaction ;
	
	@Column(name = "is_constitution_display")
	private Boolean isConstitutionDisplay = false;

	@Column(name = "is_constitution_mandatory")
	private Boolean isConstitutionMandatory = false;
	
	
	

	public BigDecimal getPromotorContri() {
		return promotorContri;
	}

	public void setPromotorContri(BigDecimal promotorContri) {
		this.promotorContri = promotorContri;
	}

	public Integer getIndividualCibil() {
		return individualCibil;
	}

	public void setIndividualCibil(Integer individualCibil) {
		this.individualCibil = individualCibil;
	}

	public Boolean getIsIndividualCibilDisplay() {
		return isIndividualCibilDisplay;
	}

	public void setIsIndividualCibilDisplay(Boolean individualCibilDisplay) {
		isIndividualCibilDisplay = individualCibilDisplay;
	}

	public Boolean getIsIndividualCibilMandatory() {
		return isIndividualCibilMandatory;
	}

	public void setIsIndividualCibilMandatory(Boolean individualCibilMandatory) {
		isIndividualCibilMandatory = individualCibilMandatory;
	}

	public Integer getCommercialCibil() {
		return commercialCibil;
	}

	public void setCommercialCibil(Integer commercialCibil) {
		this.commercialCibil = commercialCibil;
	}

	public Boolean getIsCommercialCibilDisplay() {
		return isCommercialCibilDisplay;
	}

	public void setIsCommercialCibilDisplay(Boolean commercialCibilDisplay) {
		isCommercialCibilDisplay = commercialCibilDisplay;
	}

	public Boolean getIsCommercialCibilMandatory() {
		return isCommercialCibilMandatory;
	}

	public void setIsCommercialCibilMandatory(Boolean commercialCibilMandatory) {
		isCommercialCibilMandatory = commercialCibilMandatory;
	}

	public Integer getDdrFlow() {
		return ddrFlow;
	}

	public void setDdrFlow(Integer ddrFlow) {
		this.ddrFlow = ddrFlow;
	}

	public Integer getMinChequeBounced() {
		return minChequeBounced;
	}

	public void setMinChequeBounced(Integer minChequeBounced) {
		this.minChequeBounced = minChequeBounced;
	}

	public Integer getMaxChequeBounced() {
		return maxChequeBounced;
	}

	public void setMaxChequeBounced(Integer maxChequeBounced) {
		this.maxChequeBounced = maxChequeBounced;
	}

	public Boolean getIsChequeBouncedDisplay() {
		return isChequeBouncedDisplay;
	}

	public void setIsChequeBouncedDisplay(Boolean chequeBouncedDisplay) {
		isChequeBouncedDisplay = chequeBouncedDisplay;
	}

	public Boolean getIsChequeBouncedMandatory() {
		return isChequeBouncedMandatory;
	}

	public void setIsChequeBouncedMandatory(Boolean chequeBouncedMandatory) {
		isChequeBouncedMandatory = chequeBouncedMandatory;
	}

	public Integer getMinChequeBouncedLastSixMonths() {
		return minChequeBouncedLastSixMonths;
	}

	public void setMinChequeBouncedLastSixMonths(Integer minChequeBouncedLastSixMonths) {
		this.minChequeBouncedLastSixMonths = minChequeBouncedLastSixMonths;
	}

	public Integer getMaxChequeBouncedLastSixMonths() {
		return maxChequeBouncedLastSixMonths;
	}

	public void setMaxChequeBouncedLastSixMonths(Integer maxChequeBouncedLastSixMonths) {
		this.maxChequeBouncedLastSixMonths = maxChequeBouncedLastSixMonths;
	}

	public Boolean getIsChequeBouncedLastSixMonthsDisplay() {
		return isChequeBouncedLastSixMonthsDisplay;
	}

	public void setIsChequeBouncedLastSixMonthsDisplay(Boolean chequeBouncedLastSixMonthsDisplay) {
		isChequeBouncedLastSixMonthsDisplay = chequeBouncedLastSixMonthsDisplay;
	}

	public Boolean getIsChequeBouncedLastSixMonthsMandatory() {
		return isChequeBouncedLastSixMonthsMandatory;
	}

	public void setIsChequeBouncedLastSixMonthsMandatory(Boolean chequeBouncedLastSixMonthsMandatory) {
		isChequeBouncedLastSixMonthsMandatory = chequeBouncedLastSixMonthsMandatory;
	}

	public BigDecimal getMinCurrentRatio() {
		return minCurrentRatio;
	}

	public void setMinCurrentRatio(BigDecimal minCurrentRatio) {
		this.minCurrentRatio = minCurrentRatio;
	}

	public BigDecimal getMaxCurrentRatio() {
		return maxCurrentRatio;
	}

	public void setMaxCurrentRatio(BigDecimal maxCurrentRatio) {
		this.maxCurrentRatio = maxCurrentRatio;
	}

	public BigDecimal getMinInterestCoverage() {
		return minInterestCoverage;
	}

	public void setMinInterestCoverage(BigDecimal minInterestCoverage) {
		this.minInterestCoverage = minInterestCoverage;
	}

	public BigDecimal getMaxInterestCoverage() {
		return maxInterestCoverage;
	}

	public void setMaxInterestCoverage(BigDecimal maxInterestCoverage) {
		this.maxInterestCoverage = maxInterestCoverage;
	}

	public BigDecimal getMinTolTnw() {
		return minTolTnw;
	}

	public void setMinTolTnw(BigDecimal minTolTnw) {
		this.minTolTnw = minTolTnw;
	}

	public BigDecimal getMaxTolTnw() {
		return maxTolTnw;
	}

	public void setMaxTolTnw(BigDecimal maxTolTnw) {
		this.maxTolTnw = maxTolTnw;
	}

	public BigDecimal getMinTurnoverRatio() {
		return minTurnoverRatio;
	}

	public void setMinTurnoverRatio(BigDecimal minTurnoverRatio) {
		this.minTurnoverRatio = minTurnoverRatio;
	}

	public BigDecimal getMaxTurnoverRatio() {
		return maxTurnoverRatio;
	}

	public void setMaxTurnoverRatio(BigDecimal maxTurnoverRatio) {
		this.maxTurnoverRatio = maxTurnoverRatio;
	}

	public BigDecimal getMinGrossCashAccuralsRatio() {
		return minGrossCashAccuralsRatio;
	}

	public void setMinGrossCashAccuralsRatio(BigDecimal minGrossCashAccuralsRatio) {
		this.minGrossCashAccuralsRatio = minGrossCashAccuralsRatio;
	}

	public BigDecimal getMaxGrossCashAccuralsRatio() {
		return maxGrossCashAccuralsRatio;
	}

	public void setMaxGrossCashAccuralsRatio(BigDecimal maxGrossCashAccuralsRatio) {
		this.maxGrossCashAccuralsRatio = maxGrossCashAccuralsRatio;
	}

    public BigDecimal getMinCustomerConcentration() {
        return minCustomerConcentration;
    }

    public void setMinCustomerConcentration(BigDecimal minCustomerConcentration) {
        this.minCustomerConcentration = minCustomerConcentration;
    }

    public BigDecimal getMaxCustomerConcentration() {
        return maxCustomerConcentration;
    }

    public void setMaxCustomerConcentration(BigDecimal maxCustomerConcentration) {
        this.maxCustomerConcentration = maxCustomerConcentration;
    }

    public Integer getMinRiskModelScore() {
		return minRiskModelScore;
	}

	public void setMinRiskModelScore(Integer minRiskModelScore) {
		this.minRiskModelScore = minRiskModelScore;
	}

	public Integer getMaxRiskModelScore() {
		return maxRiskModelScore;
	}

	public void setMaxRiskModelScore(Integer maxRiskModelScore) {
		this.maxRiskModelScore = maxRiskModelScore;
	}

	public Integer getNetWorth() {
		return netWorth;
	}

	public void setNetWorth(Integer netWorth) {
		this.netWorth = netWorth;
	}

	public Boolean getIsCurrentRatioDisplay() {
		return isCurrentRatioDisplay;
	}

	public void setIsCurrentRatioDisplay(Boolean currentRatioDisplay) {
		isCurrentRatioDisplay = currentRatioDisplay;
	}

	public Boolean getIsCurrentRatioMandatory() {
		return isCurrentRatioMandatory;
	}

	public void setIsCurrentRatioMandatory(Boolean currentRatioMandatory) {
		isCurrentRatioMandatory = currentRatioMandatory;
	}

	public Boolean getIsInterestCoverageDisplay() {
		return isInterestCoverageDisplay;
	}

	public void setIsInterestCoverageDisplay(Boolean interestCoverageDisplay) {
		isInterestCoverageDisplay = interestCoverageDisplay;
	}

	public Boolean getIsInterestCoverageMandatory() {
		return isInterestCoverageMandatory;
	}

	public void setIsInterestCoverageMandatory(Boolean interestCoverageMandatory) {
		isInterestCoverageMandatory = interestCoverageMandatory;
	}

	public Boolean getIsTolTnwDisplay() {
		return isTolTnwDisplay;
	}

	public void setIsTolTnwDisplay(Boolean tolTnwDisplay) {
		isTolTnwDisplay = tolTnwDisplay;
	}

	public Boolean getIsTolTnwMandatory() {
		return isTolTnwMandatory;
	}

	public void setIsTolTnwMandatory(Boolean tolTnwMandatory) {
		isTolTnwMandatory = tolTnwMandatory;
	}

	public Boolean getIsTurnoverRatioDisplay() {
		return isTurnoverRatioDisplay;
	}

	public void setIsTurnoverRatioDisplay(Boolean turnoverRatioDisplay) {
		isTurnoverRatioDisplay = turnoverRatioDisplay;
	}

	public Boolean getIsTurnoverRatioMandatory() {
		return isTurnoverRatioMandatory;
	}

	public void setIsTurnoverRatioMandatory(Boolean turnoverRatioMandatory) {
		isTurnoverRatioMandatory = turnoverRatioMandatory;
	}

	public Boolean getIsGrossCashAccuralsRatioDisplay() {
		return isGrossCashAccuralsRatioDisplay;
	}

	public void setIsGrossCashAccuralsRatioDisplay(Boolean grossCashAccuralsRatioDisplay) {
		isGrossCashAccuralsRatioDisplay = grossCashAccuralsRatioDisplay;
	}

	public Boolean getIsGrossCashAccuralsRatioMandatory() {
		return isGrossCashAccuralsRatioMandatory;
	}

	public void setIsGrossCashAccuralsRatioMandatory(Boolean grossCashAccuralsRatioMandatory) {
		isGrossCashAccuralsRatioMandatory = grossCashAccuralsRatioMandatory;
	}

	public Boolean getIsCustomerConcentrationDisplay() {
		return isCustomerConcentrationDisplay;
	}

	public void setIsCustomerConcentrationDisplay(Boolean customerConcentrationDisplay) {
		isCustomerConcentrationDisplay = customerConcentrationDisplay;
	}

	public Boolean getIsCustomerConcentrationMandatory() {
		return isCustomerConcentrationMandatory;
	}

	public void setIsCustomerConcentrationMandatory(Boolean customerConcentrationMandatory) {
		isCustomerConcentrationMandatory = customerConcentrationMandatory;
	}

	public Boolean getIsRiskModelScoreDisplay() {
		return isRiskModelScoreDisplay;
	}

	public void setIsRiskModelScoreDisplay(Boolean riskModelScoreDisplay) {
		isRiskModelScoreDisplay = riskModelScoreDisplay;
	}

	public Boolean getIsRiskModelScoreMandatory() {
		return isRiskModelScoreMandatory;
	}

	public void setIsRiskModelScoreMandatory(Boolean riskModelScoreMandatory) {
		isRiskModelScoreMandatory = riskModelScoreMandatory;
	}

	public TermLoanParameterTemp() {
		// Do nothing because of X and Y.
	}

	public ProductMasterTemp getFpProductId() {
		return fpProductId;
	}

	public void setFpProductId(ProductMasterTemp fpProductId) {
		this.fpProductId = fpProductId;
	}

	public Long getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getCurrency() {
		return this.currency;
	}

	public void setCurrency(Integer currency) {
		this.currency = currency;
	}

	public Integer getDenomination() {
		return this.denomination;
	}

	public void setDenomination(Integer denomination) {
		this.denomination = denomination;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsCollateralDisplay() {
		return this.isCollateralDisplay;
	}

	public void setIsCollateralDisplay(Boolean isCollateralDisplay) {
		this.isCollateralDisplay = isCollateralDisplay;
	}

	public Boolean getIsCollateralMandatory() {
		return this.isCollateralMandatory;
	}

	public void setIsCollateralMandatory(Boolean isCollateralMandatory) {
		this.isCollateralMandatory = isCollateralMandatory;
	}

	public Boolean getIsCreditRatingDisplay() {
		return this.isCreditRatingDisplay;
	}

	public void setIsCreditRatingDisplay(Boolean isCreditRatingDisplay) {
		this.isCreditRatingDisplay = isCreditRatingDisplay;
	}

	public Boolean getIsCreditRatingMandatory() {
		return this.isCreditRatingMandatory;
	}

	public void setIsCreditRatingMandatory(Boolean isCreditRatingMandatory) {
		this.isCreditRatingMandatory = isCreditRatingMandatory;
	}

	public Boolean getIsDebtEquityDisplay() {
		return this.isDebtEquityDisplay;
	}

	public void setIsDebtEquityDisplay(Boolean isDebtEquityDisplay) {
		this.isDebtEquityDisplay = isDebtEquityDisplay;
	}

	public Boolean getIsDebtEquityMandatory() {
		return this.isDebtEquityMandatory;
	}

	public void setIsDebtEquityMandatory(Boolean isDebtEquityMandatory) {
		this.isDebtEquityMandatory = isDebtEquityMandatory;
	}

	public Boolean getIsEstablishmentDisplay() {
		return this.isEstablishmentDisplay;
	}

	public void setIsEstablishmentDisplay(Boolean isEstablishmentDisplay) {
		this.isEstablishmentDisplay = isEstablishmentDisplay;
	}

	public Boolean getIsEstablishmentMandatory() {
		return this.isEstablishmentMandatory;
	}

	public void setIsEstablishmentMandatory(Boolean isEstablishmentMandatory) {
		this.isEstablishmentMandatory = isEstablishmentMandatory;
	}

	public Boolean getIsGeographicalDisplay() {
		return this.isGeographicalDisplay;
	}

	public void setIsGeographicalDisplay(Boolean isGeographicalDisplay) {
		this.isGeographicalDisplay = isGeographicalDisplay;
	}

	public Boolean getIsGeographicalMandatory() {
		return this.isGeographicalMandatory;
	}

	public void setIsGeographicalMandatory(Boolean isGeographicalMandatory) {
		this.isGeographicalMandatory = isGeographicalMandatory;
	}

	public Boolean getIsIndustrySectorDisplay() {
		return this.isIndustrySectorDisplay;
	}

	public void setIsIndustrySectorDisplay(Boolean isIndustrySectorDisplay) {
		this.isIndustrySectorDisplay = isIndustrySectorDisplay;
	}

	public Boolean getIsIndustrySectorMandatory() {
		return this.isIndustrySectorMandatory;
	}

	public void setIsIndustrySectorMandatory(Boolean isIndustrySectorMandatory) {
		this.isIndustrySectorMandatory = isIndustrySectorMandatory;
	}

	public Boolean getIsInvestmentSizeDisplay() {
		return this.isInvestmentSizeDisplay;
	}

	public void setIsInvestmentSizeDisplay(Boolean isInvestmentSizeDisplay) {
		this.isInvestmentSizeDisplay = isInvestmentSizeDisplay;
	}

	public Boolean getIsInvestmentSizeMandatory() {
		return this.isInvestmentSizeMandatory;
	}

	public void setIsInvestmentSizeMandatory(Boolean isInvestmentSizeMandatory) {
		this.isInvestmentSizeMandatory = isInvestmentSizeMandatory;
	}

	public Boolean getIsNetworthDisplay() {
		return this.isNetworthDisplay;
	}

	public void setIsNetworthDisplay(Boolean isNetworthDisplay) {
		this.isNetworthDisplay = isNetworthDisplay;
	}

	public Boolean getIsNetworthMandatory() {
		return this.isNetworthMandatory;
	}

	public void setIsNetworthMandatory(Boolean isNetworthMandatory) {
		this.isNetworthMandatory = isNetworthMandatory;
	}

	public Boolean getIsPastYearTurnoverDisplay() {
		return this.isPastYearTurnoverDisplay;
	}

	public void setIsPastYearTurnoverDisplay(Boolean isPastYearTurnoverDisplay) {
		this.isPastYearTurnoverDisplay = isPastYearTurnoverDisplay;
	}

	public Boolean getIsPastYearTurnoverMandatory() {
		return this.isPastYearTurnoverMandatory;
	}

	public void setIsPastYearTurnoverMandatory(Boolean isPastYearTurnoverMandatory) {
		this.isPastYearTurnoverMandatory = isPastYearTurnoverMandatory;
	}

	public Boolean getIsProfitabilityHistoryDisplay() {
		return this.isProfitabilityHistoryDisplay;
	}

	public void setIsProfitabilityHistoryDisplay(Boolean isProfitabilityHistoryDisplay) {
		this.isProfitabilityHistoryDisplay = isProfitabilityHistoryDisplay;
	}

	public Boolean getIsProfitabilityHistoryMandatory() {
		return this.isProfitabilityHistoryMandatory;
	}

	public void setIsProfitabilityHistoryMandatory(Boolean isProfitabilityHistoryMandatory) {
		this.isProfitabilityHistoryMandatory = isProfitabilityHistoryMandatory;
	}

	public Boolean getIsTenureDisplay() {
		return this.isTenureDisplay;
	}

	public void setIsTenureDisplay(Boolean isTenureDisplay) {
		this.isTenureDisplay = isTenureDisplay;
	}

	public Boolean getIsTenureMandatory() {
		return this.isTenureMandatory;
	}

	public void setIsTenureMandatory(Boolean isTenureMandatory) {
		this.isTenureMandatory = isTenureMandatory;
	}

	public Integer getLongTermCreditRating() {
		return this.LongTermCreditRating;
	}

	public void setLongTermCreditRating(Integer LongTermCreditRating) {
		this.LongTermCreditRating = LongTermCreditRating;
	}

	public Integer getMaxAgeEstablishment() {
		return this.maxAgeEstablishment;
	}

	public void setMaxAgeEstablishment(Integer maxAgeEstablishment) {
		this.maxAgeEstablishment = maxAgeEstablishment;
	}

	public BigDecimal getMaxCollateral() {
		return maxCollateral;
	}

	public void setMaxCollateral(BigDecimal maxCollateral) {
		this.maxCollateral = maxCollateral;
	}

	public BigDecimal getMinCollateral() {
		return minCollateral;
	}

	public void setMinCollateral(BigDecimal minCollateral) {
		this.minCollateral = minCollateral;
	}

	public Integer getMinAgeEstablishment() {
		return this.minAgeEstablishment;
	}

	public void setMinAgeEstablishment(Integer minAgeEstablishment) {
		this.minAgeEstablishment = minAgeEstablishment;
	}

	public BigDecimal getMaxDebtEquity() {
		return maxDebtEquity;
	}

	public void setMaxDebtEquity(BigDecimal maxDebtEquity) {
		this.maxDebtEquity = maxDebtEquity;
	}

	public BigDecimal getMinDebtEquity() {
		return minDebtEquity;
	}

	public void setMinDebtEquity(BigDecimal minDebtEquity) {
		this.minDebtEquity = minDebtEquity;
	}

	public Long getModifiedBy() {
		return this.modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedDate() {
		return this.modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Integer getProfitabilityHistory() {
		return this.profitabilityHistory;
	}

	public void setProfitabilityHistory(Integer profitabilityHistory) {
		this.profitabilityHistory = profitabilityHistory;
	}

	public Integer getShortTermCreditRating() {
		return this.shortTermCreditRating;
	}

	public void setShortTermCreditRating(Integer shortTermCreditRating) {
		this.shortTermCreditRating = shortTermCreditRating;
	}

	public Long getUninterestedIndustry() {
		return this.uninterestedIndustry;
	}

	public void setUninterestedIndustry(Long uninterestedIndustry) {
		this.uninterestedIndustry = uninterestedIndustry;
	}

	public BigDecimal getMaxInvestSize() {
		return maxInvestSize;
	}

	public void setMaxInvestSize(BigDecimal maxInvestSize) {
		this.maxInvestSize = maxInvestSize;
	}

	public BigDecimal getMaxNetworth() {
		return maxNetworth;
	}

	public void setMaxNetworth(BigDecimal maxNetworth) {
		this.maxNetworth = maxNetworth;
	}

	public BigDecimal getMaxPastTurnover() {
		return maxPastTurnover;
	}

	public void setMaxPastTurnover(BigDecimal maxPastTurnover) {
		this.maxPastTurnover = maxPastTurnover;
	}

	public BigDecimal getMaxTenure() {
		return maxTenure;
	}

	public void setMaxTenure(BigDecimal maxTenure) {
		this.maxTenure = maxTenure;
	}

	public BigDecimal getMinInvestSize() {
		return minInvestSize;
	}

	public void setMinInvestSize(BigDecimal minInvestSize) {
		this.minInvestSize = minInvestSize;
	}

	public BigDecimal getMinNetworth() {
		return minNetworth;
	}

	public void setMinNetworth(BigDecimal minNetworth) {
		this.minNetworth = minNetworth;
	}

	public BigDecimal getMinPastTurnover() {
		return minPastTurnover;
	}

	public void setMinPastTurnover(BigDecimal minPastTurnover) {
		this.minPastTurnover = minPastTurnover;
	}

	public BigDecimal getMinTenure() {
		return minTenure;
	}

	public void setMinTenure(BigDecimal minTenure) {
		this.minTenure = minTenure;
	}

	public Boolean getIsUnInterestedIndustryDisplay() {
		return isUnInterestedIndustryDisplay;
	}

	public void setIsUnInterestedIndustryDisplay(Boolean isUnInterestedIndustryDisplay) {
		this.isUnInterestedIndustryDisplay = isUnInterestedIndustryDisplay;
	}

	public Boolean getIsUnInterestedIndustryMandatory() {
		return isUnInterestedIndustryMandatory;
	}

	public void setIsUnInterestedIndustryMandatory(Boolean isUnInterestedIndustryMandatory) {
		this.isUnInterestedIndustryMandatory = isUnInterestedIndustryMandatory;
	}

	public Integer getAssessmentMethodId() {
		return assessmentMethodId;
	}

	public void setAssessmentMethodId(Integer assessmentMethodId) {
		this.assessmentMethodId = assessmentMethodId;
	}

	public BigDecimal getMinCgtmseCoverage() {
		return minCgtmseCoverage;
	}

	public void setMinCgtmseCoverage(BigDecimal minCgtmseCoverage) {
		this.minCgtmseCoverage = minCgtmseCoverage;
	}

	public BigDecimal getMaxCgtmseCoverage() {
		return maxCgtmseCoverage;
	}

	public void setMaxCgtmseCoverage(BigDecimal maxCgtmseCoverage) {
		this.maxCgtmseCoverage = maxCgtmseCoverage;
	}
	public Boolean getIsCgtmseCoverageDisplay() {
		return isCgtmseCoverageDisplay;
	}

	public void setIsCgtmseCoverageDisplay(Boolean isCgtmseCoverageDisplay) {
		this.isCgtmseCoverageDisplay = isCgtmseCoverageDisplay;
	}

	public Boolean getIsCgtmseCoverageMandatory() {
		return isCgtmseCoverageMandatory;
	}

	public void setIsCgtmseCoverageMandatory(Boolean isCgtmseCoverageMandatory) {
		this.isCgtmseCoverageMandatory = isCgtmseCoverageMandatory;
	}

	public Boolean getIsMsmeFundingDisplay() {
		return isMsmeFundingDisplay;
	}

	public void setIsMsmeFundingDisplay(Boolean isMsmeFundingDisplay) {
		this.isMsmeFundingDisplay = isMsmeFundingDisplay;
	}

	public Boolean getIsMsmeFundingMandatory() {
		return isMsmeFundingMandatory;
	}

	public void setIsMsmeFundingMandatory(Boolean isMsmeFundingMandatory) {
		this.isMsmeFundingMandatory = isMsmeFundingMandatory;
	}

	public Integer getCgtmseCoverage() {
		return cgtmseCoverage;
	}

	public void setCgtmseCoverage(Integer cgtmseCoverage) {
		this.cgtmseCoverage = cgtmseCoverage;
	}

	public Integer getCashMargin() {
		return cashMargin;
	}

	public void setCashMargin(Integer cashMargin) {
		this.cashMargin = cashMargin;
	}

	public BigDecimal getDscr() {
		return dscr;
	}

	public void setDscr(BigDecimal dscr) {
		this.dscr = dscr;
	}

	public Integer getPaybackPeriod() {
		return paybackPeriod;
	}

	public void setPaybackPeriod(Integer paybackPeriod) {
		this.paybackPeriod = paybackPeriod;
	}

	public BigDecimal getMaxDropInTurnover() {
		return maxDropInTurnover;
	}

	public void setMaxDropInTurnover(BigDecimal maxDropInTurnover) {
		this.maxDropInTurnover = maxDropInTurnover;
	}

	public Boolean getIsMaxDropInTurnoverDisplay() {
		return isMaxDropInTurnoverDisplay;
	}

	public void setIsMaxDropInTurnoverDisplay(Boolean isMaxDropInTurnoverDisplay) {
		this.isMaxDropInTurnoverDisplay = isMaxDropInTurnoverDisplay;
	}

	public Boolean getIsMaxDropInTurnoverMandatory() {
		return isMaxDropInTurnoverMandatory;
	}

	public void setIsMaxDropInTurnoverMandatory(Boolean isMaxDropInTurnoverMandatory) {
		this.isMaxDropInTurnoverMandatory = isMaxDropInTurnoverMandatory;
	}

	public BigDecimal getMinUtilisationPercentage() {
		return minUtilisationPercentage;
	}

	public void setMinUtilisationPercentage(BigDecimal minUtilisationPercentage) {
		this.minUtilisationPercentage = minUtilisationPercentage;
	}

	public BigDecimal getMaxUtilisationPercentage() {
		return maxUtilisationPercentage;
	}

	public void setMaxUtilisationPercentage(BigDecimal maxUtilisationPercentage) {
		this.maxUtilisationPercentage = maxUtilisationPercentage;
	}

	public Boolean getIsUtilisationPercentageDisplay() {
		return isUtilisationPercentageDisplay;
	}

	public void setIsUtilisationPercentageDisplay(Boolean isUtilisationPercentageDisplay) {
		this.isUtilisationPercentageDisplay = isUtilisationPercentageDisplay;
	}

	public Boolean getIsUtilisationPercentageMandatory() {
		return isUtilisationPercentageMandatory;
	}

	public void setIsUtilisationPercentageMandatory(Boolean isUtilisationPercentageMandatory) {
		this.isUtilisationPercentageMandatory = isUtilisationPercentageMandatory;
	}

	public BigDecimal getMinCreditSummation() {
		return minCreditSummation;
	}

	public void setMinCreditSummation(BigDecimal minCreditSummation) {
		this.minCreditSummation = minCreditSummation;
	}

	public BigDecimal getMaxCreditSummation() {
		return maxCreditSummation;
	}

	public void setMaxCreditSummation(BigDecimal maxCreditSummation) {
		this.maxCreditSummation = maxCreditSummation;
	}

	public Boolean getIsCreditSummationDisplay() {
		return isCreditSummationDisplay;
	}

	public void setIsCreditSummationDisplay(Boolean isCreditSummationDisplay) {
		this.isCreditSummationDisplay = isCreditSummationDisplay;
	}

	public Boolean getIsCreditSummationMandatory() {
		return isCreditSummationMandatory;
	}

	public void setIsCreditSummationMandatory(Boolean isCreditSummationMandatory) {
		this.isCreditSummationMandatory = isCreditSummationMandatory;
	}

	public BigDecimal getMinCollateralCoverage() {
		return minCollateralCoverage;
	}

	public void setMinCollateralCoverage(BigDecimal minCollateralCoverage) {
		this.minCollateralCoverage = minCollateralCoverage;
	}

	public BigDecimal getMaxCollateralCoverage() {
		return maxCollateralCoverage;
	}

	public void setMaxCollateralCoverage(BigDecimal maxCollateralCoverage) {
		this.maxCollateralCoverage = maxCollateralCoverage;
	}

	public Boolean getIsCollateralCoverageDisplay() {
		return isCollateralCoverageDisplay;
	}

	public void setIsCollateralCoverageDisplay(Boolean isCollateralCoverageDisplay) {
		this.isCollateralCoverageDisplay = isCollateralCoverageDisplay;
	}

	public Boolean getIsCollateralCoverageMandatory() {
		return isCollateralCoverageMandatory;
	}

	public void setIsCollateralCoverageMandatory(Boolean isCollateralCoverageMandatory) {
		this.isCollateralCoverageMandatory = isCollateralCoverageMandatory;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public BigDecimal getNewTolTnw() {
		return newTolTnw;
	}

	public void setNewTolTnw(BigDecimal newTolTnw) {
		this.newTolTnw = newTolTnw;
	}

	public BigDecimal getNewCollateralCoverage() {
		return newCollateralCoverage;
	}

	public void setNewCollateralCoverage(BigDecimal newCollateralCoverage) {
		this.newCollateralCoverage = newCollateralCoverage;
	}

	public Boolean getIsNewCollateralCoverageCheck() {
		return isNewCollateralCoverageCheck;
	}

	public void setIsNewCollateralCoverageCheck(Boolean isNewCollateralCoverageCheck) {
		this.isNewCollateralCoverageCheck = isNewCollateralCoverageCheck;
	}

	public Boolean getIsNewTolTnwCheck() {
		return isNewTolTnwCheck;
	}

	public void setIsNewTolTnwCheck(Boolean isNewTolTnwCheck) {
		this.isNewTolTnwCheck = isNewTolTnwCheck;
	}

	public Integer getKotakSubParameter() {
		return kotakSubParameter;
	}

	public void setKotakSubParameter(Integer kotakSubParameter) {
		this.kotakSubParameter = kotakSubParameter;
	}

	public Boolean getWcReqCheck() {
		return wcReqCheck;
	}

	public void setWcReqCheck(Boolean wcReqCheck) {
		this.wcReqCheck = wcReqCheck;
	}

	
	public Boolean getIsNewDscrCheck() {
		return isNewDscrCheck;
	}

	public BigDecimal getNewDscrCheck() {
		return newDscrCheck;
	}

	public void setIsNewDscrCheck(Boolean isNewDscrCheck) {
		this.isNewDscrCheck = isNewDscrCheck;
	}

	public void setNewDscrCheck(BigDecimal newDscrCheck) {
		this.newDscrCheck = newDscrCheck;
	}

	public Boolean getIsNewHistoricSales() {
		return isNewHistoricSales;
	}

	public void setIsNewHistoricSales(Boolean isNewHistoricSales) {
		this.isNewHistoricSales = isNewHistoricSales;
	}

	public BigDecimal getNewHistoricSales() {
		return newHistoricSales;
	}

	public void setNewHistoricSales(BigDecimal newHistoricSales) {
		this.newHistoricSales = newHistoricSales;
	}

	public Boolean getIsHistoricGrowth() {
		return isHistoricGrowth;
	}

	public void setIsHistoricGrowth(Boolean isHistoricGrowth) {
		this.isHistoricGrowth = isHistoricGrowth;
	}

	public BigDecimal getNewHistoricGrowth() {
		return newHistoricGrowth;
	}

	public void setNewHistoricGrowth(BigDecimal newHistoricGrowth) {
		this.newHistoricGrowth = newHistoricGrowth;
	}

	public BigDecimal getMinAdditionalLoan() {
		return minAdditionalLoan;
	}

	public void setMinAdditionalLoan(BigDecimal minAdditionalLoan) {
		this.minAdditionalLoan = minAdditionalLoan;
	}

	public BigDecimal getMaxAdditionalLoan() {
		return maxAdditionalLoan;
	}

	public void setMaxAdditionalLoan(BigDecimal maxAdditionalLoan) {
		this.maxAdditionalLoan = maxAdditionalLoan;
	}

	public BigDecimal getMinTotalLoan() {
		return minTotalLoan;
	}

	public void setMinTotalLoan(BigDecimal minTotalLoan) {
		this.minTotalLoan = minTotalLoan;
	}

	public BigDecimal getMaxTotalLoan() {
		return maxTotalLoan;
	}

	public void setMaxTotalLoan(BigDecimal maxTotalLoan) {
		this.maxTotalLoan = maxTotalLoan;
	}

	public Boolean getIsConservativeRatioDisplay() {
		return isConservativeRatioDisplay;
	}

	public void setIsConservativeRatioDisplay(Boolean isConservativeRatioDisplay) {
		this.isConservativeRatioDisplay = isConservativeRatioDisplay;
	}

	public Boolean getIsConservativeRatioMandatory() {
		return isConservativeRatioMandatory;
	}

	public void setIsConservativeRatioMandatory(Boolean isConservativeRatioMandatory) {
		this.isConservativeRatioMandatory = isConservativeRatioMandatory;
	}

	public BigDecimal getMaxConservativeRatio() {
		return maxConservativeRatio;
	}

	public void setMaxConservativeRatio(BigDecimal maxConservativeRatio) {
		this.maxConservativeRatio = maxConservativeRatio;
	}

	public BigDecimal getMinConservativeRatio() {
		return minConservativeRatio;
	}

	public void setMinConservativeRatio(BigDecimal minConservativeRatio) {
		this.minConservativeRatio = minConservativeRatio;
	}

	public Integer getMainIndividualCibil() {
		return mainIndividualCibil;
	}

	public void setMainIndividualCibil(Integer mainIndividualCibil) {
		this.mainIndividualCibil = mainIndividualCibil;
	}

	public Boolean getIsMainIndividualCibilDisplay() {
		return isMainIndividualCibilDisplay;
	}

	public void setIsMainIndividualCibilDisplay(Boolean isMainIndividualCibilDisplay) {
		this.isMainIndividualCibilDisplay = isMainIndividualCibilDisplay;
	}

	public Boolean getIsMainIndividualCibilMandatory() {
		return isMainIndividualCibilMandatory;
	}

	public void setIsMainIndividualCibilMandatory(Boolean isMainIndividualCibilMandatory) {
		this.isMainIndividualCibilMandatory = isMainIndividualCibilMandatory;
	}

	public Integer getCommercialCibilFor3Month() {
		return commercialCibilFor3Month;
	}

	public void setCommercialCibilFor3Month(Integer commercialCibilFor3Month) {
		this.commercialCibilFor3Month = commercialCibilFor3Month;
	}

	public Boolean getIsCommercialCibilFor3MonthDisplay() {
		return isCommercialCibilFor3MonthDisplay;
	}

	public void setIsCommercialCibilFor3MonthDisplay(Boolean isCommercialCibilFor3MonthDisplay) {
		this.isCommercialCibilFor3MonthDisplay = isCommercialCibilFor3MonthDisplay;
	}

	public Boolean getIsCommercialCibilFor3MonthMandatory() {
		return isCommercialCibilFor3MonthMandatory;
	}

	public void setIsCommercialCibilFor3MonthMandatory(Boolean isCommercialCibilFor3MonthMandatory) {
		this.isCommercialCibilFor3MonthMandatory = isCommercialCibilFor3MonthMandatory;
	}

	public BigDecimal getManufacturing() {
		return manufacturing;
	}

	public void setManufacturing(BigDecimal manufacturing) {
		this.manufacturing = manufacturing;
	}

	public BigDecimal getService() {
		return service;
	}

	public void setService(BigDecimal service) {
		this.service = service;
	}

	public BigDecimal getTrading() {
		return trading;
	}

	public void setTrading(BigDecimal trading) {
		this.trading = trading;
	}

	public Boolean getIsMinNoCreditTransactionDisplay() {
		return isMinNoCreditTransactionDisplay;
	}

	public void setIsMinNoCreditTransactionDisplay(Boolean isMinNoCreditTransactionDisplay) {
		this.isMinNoCreditTransactionDisplay = isMinNoCreditTransactionDisplay;
	}

	public Boolean getIsMinNoCreditTransactionMandatory() {
		return isMinNoCreditTransactionMandatory;
	}

	public void setIsMinNoCreditTransactionMandatory(Boolean isMinNoCreditTransactionMandatory) {
		this.isMinNoCreditTransactionMandatory = isMinNoCreditTransactionMandatory;
	}

	public BigDecimal getMaxNoCreditTransaction() {
		return maxNoCreditTransaction;
	}

	public void setMaxNoCreditTransaction(BigDecimal maxNoCreditTransaction) {
		this.maxNoCreditTransaction = maxNoCreditTransaction;
	}

	public BigDecimal getMinNoCreditTransaction() {
		return minNoCreditTransaction;
	}

	public void setMinNoCreditTransaction(BigDecimal minNoCreditTransaction) {
		this.minNoCreditTransaction = minNoCreditTransaction;
	}

	public Boolean getIsMinNoOverallTransactionDisplay() {
		return isMinNoOverallTransactionDisplay;
	}

	public void setIsMinNoOverallTransactionDisplay(Boolean isMinNoOverallTransactionDisplay) {
		this.isMinNoOverallTransactionDisplay = isMinNoOverallTransactionDisplay;
	}

	public Boolean getIsMinNoOverallTransactionMandatory() {
		return isMinNoOverallTransactionMandatory;
	}

	public void setIsMinNoOverallTransactionMandatory(Boolean isMinNoOverallTransactionMandatory) {
		this.isMinNoOverallTransactionMandatory = isMinNoOverallTransactionMandatory;
	}

	public BigDecimal getMaxNoOverallTransaction() {
		return maxNoOverallTransaction;
	}

	public void setMaxNoOverallTransaction(BigDecimal maxNoOverallTransaction) {
		this.maxNoOverallTransaction = maxNoOverallTransaction;
	}

	public BigDecimal getMinNoOverallTransaction() {
		return minNoOverallTransaction;
	}

	public void setMinNoOverallTransaction(BigDecimal minNoOverallTransaction) {
		this.minNoOverallTransaction = minNoOverallTransaction;
	}

	public Boolean getIsMinSalesGrowthDisplay() {
		return isMinSalesGrowthDisplay;
	}

	public void setIsMinSalesGrowthDisplay(Boolean isMinSalesGrowthDisplay) {
		this.isMinSalesGrowthDisplay = isMinSalesGrowthDisplay;
	}

	public Boolean getIsMinSalesGrowthMandatory() {
		return isMinSalesGrowthMandatory;
	}

	public void setIsMinSalesGrowthMandatory(Boolean isMinSalesGrowthMandatory) {
		this.isMinSalesGrowthMandatory = isMinSalesGrowthMandatory;
	}

	public BigDecimal getMaxSalesGrowth() {
		return maxSalesGrowth;
	}

	public void setMaxSalesGrowth(BigDecimal maxSalesGrowth) {
		this.maxSalesGrowth = maxSalesGrowth;
	}

	public BigDecimal getMinSalesGrowth() {
		return minSalesGrowth;
	}

	public void setMinSalesGrowth(BigDecimal minSalesGrowth) {
		this.minSalesGrowth = minSalesGrowth;
	}

	public Boolean getIsMinCashProfitGrowthDisplay() {
		return isMinCashProfitGrowthDisplay;
	}

	public void setIsMinCashProfitGrowthDisplay(Boolean isMinCashProfitGrowthDisplay) {
		this.isMinCashProfitGrowthDisplay = isMinCashProfitGrowthDisplay;
	}

	public Boolean getIsMinCashProfitGrowthMandatory() {
		return isMinCashProfitGrowthMandatory;
	}

	public void setIsMinCashProfitGrowthMandatory(Boolean isMinCashProfitGrowthMandatory) {
		this.isMinCashProfitGrowthMandatory = isMinCashProfitGrowthMandatory;
	}

	public BigDecimal getMaxCashProfitGrowth() {
		return maxCashProfitGrowth;
	}

	public void setMaxCashProfitGrowth(BigDecimal maxCashProfitGrowth) {
		this.maxCashProfitGrowth = maxCashProfitGrowth;
	}

	public BigDecimal getMinCashProfitGrowth() {
		return minCashProfitGrowth;
	}

	public void setMinCashProfitGrowth(BigDecimal minCashProfitGrowth) {
		this.minCashProfitGrowth = minCashProfitGrowth;
	}

	public Boolean getIsMinMonthlyAvgBalDisplay() {
		return isMinMonthlyAvgBalDisplay;
	}

	public void setIsMinMonthlyAvgBalDisplay(Boolean isMinMonthlyAvgBalDisplay) {
		this.isMinMonthlyAvgBalDisplay = isMinMonthlyAvgBalDisplay;
	}

	public Boolean getIsMinMonthlyAvgBalMandatory() {
		return isMinMonthlyAvgBalMandatory;
	}

	public void setIsMinMonthlyAvgBalMandatory(Boolean isMinMonthlyAvgBalMandatory) {
		this.isMinMonthlyAvgBalMandatory = isMinMonthlyAvgBalMandatory;
	}

	public BigDecimal getMaxMonthlyAvgBal() {
		return maxMonthlyAvgBal;
	}

	public void setMaxMonthlyAvgBal(BigDecimal maxMonthlyAvgBal) {
		this.maxMonthlyAvgBal = maxMonthlyAvgBal;
	}

	public BigDecimal getMinMonthlyAvgBal() {
		return minMonthlyAvgBal;
	}

	public void setMinMonthlyAvgBal(BigDecimal minMonthlyAvgBal) {
		this.minMonthlyAvgBal = minMonthlyAvgBal;
	}

	public Boolean getIsMinNoDebitTransactionDisplay() {
		return isMinNoDebitTransactionDisplay;
	}

	public void setIsMinNoDebitTransactionDisplay(Boolean isMinNoDebitTransactionDisplay) {
		this.isMinNoDebitTransactionDisplay = isMinNoDebitTransactionDisplay;
	}

	public Boolean getIsMinNoDebitTransactionMandatory() {
		return isMinNoDebitTransactionMandatory;
	}

	public void setIsMinNoDebitTransactionMandatory(Boolean isMinNoDebitTransactionMandatory) {
		this.isMinNoDebitTransactionMandatory = isMinNoDebitTransactionMandatory;
	}

	public BigDecimal getMaxNoDebitTransaction() {
		return maxNoDebitTransaction;
	}

	public void setMaxNoDebitTransaction(BigDecimal maxNoDebitTransaction) {
		this.maxNoDebitTransaction = maxNoDebitTransaction;
	}

	public BigDecimal getMinNoDebitTransaction() {
		return minNoDebitTransaction;
	}

	public void setMinNoDebitTransaction(BigDecimal minNoDebitTransaction) {
		this.minNoDebitTransaction = minNoDebitTransaction;
	}

	public Boolean getIsConstitutionDisplay() {
		return isConstitutionDisplay;
	}

	public void setIsConstitutionDisplay(Boolean isConstitutionDisplay) {
		this.isConstitutionDisplay = isConstitutionDisplay;
	}

	public Boolean getIsConstitutionMandatory() {
		return isConstitutionMandatory;
	}

	public void setIsConstitutionMandatory(Boolean isConstitutionMandatory) {
		this.isConstitutionMandatory = isConstitutionMandatory;
	}
	
	


	
	
	

	

}

