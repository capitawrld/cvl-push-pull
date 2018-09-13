package com.capitaworld.service.loans.model.corporate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.capitaworld.service.loans.model.DataRequest;
import com.capitaworld.service.loans.model.ProductMasterRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CorporateProduct  extends  ProductMasterRequest implements Serializable {
	private Integer currency;

	private Integer denomination;

	private Boolean isCollateralDisplay=false;

	private Boolean isCollateralMandatory=false;

	private Boolean isCreditRatingDisplay=false;

	private Boolean isCreditRatingMandatory=false;

	private Boolean isDebtEquityDisplay=false;

	private Boolean isDebtEquityMandatory=false;

	private Boolean isEstablishmentDisplay=false;

	private Boolean isEstablishmentMandatory=false;

	private Boolean isGeographicalDisplay=false;

	private Boolean isGeographicalMandatory=false;

	private Boolean isIndustrySectorDisplay=false;

	private Boolean isIndustrySectorMandatory=false;

	private Boolean isInvestmentSizeDisplay=false;

	private Boolean isInvestmentSizeMandatory=false;

	private Boolean isNetworthDisplay=false;

	private Boolean isNetworthMandatory=false;

	private Boolean isPastYearTurnoverDisplay=false;

	private Boolean isPastYearTurnoverMandatory=false;

	private Boolean isProfitabilityHistoryDisplay=false;

	private Boolean isProfitabilityHistoryMandatory=false;

	private Boolean isTenureDisplay=false;

	private Boolean isTenureMandatory=false;
	
	private Boolean isUnInterestedIndustryDisplay=false;

	private Boolean isUnInterestedIndustryMandatory=false;

	private Integer LongTermCreditRating;

	private Integer maxAgeEstablishment;

	private BigDecimal maxCollateral;

	private BigDecimal maxDebtEquity;

	private BigDecimal maxInvestSize;

	private BigDecimal maxNetworth;

	private BigDecimal maxPastTurnover;

	private BigDecimal maxTenure;

	private Integer minAgeEstablishment;

	private BigDecimal minCollateral;

	private BigDecimal minDebtEquity;

	private BigDecimal minInvestSize;

	private BigDecimal minNetworth;

	private BigDecimal minPastTurnover;

	private BigDecimal minTenure;
	
	private BigDecimal minTenureNtb;
	
	private BigDecimal maxTenureNtb;

	private Integer profitabilityHistory;

	private Integer shortTermCreditRating;
	
	private Long uninterestedIndustry;

	private BigDecimal minCurrentRatio;

	private BigDecimal maxCurrentRatio;

	private Boolean isCurrentRatioDisplay = false;

	private Boolean isCurrentRatioMandatory = false;

	private BigDecimal minInterestCoverage;

	private BigDecimal maxInterestCoverage;

	private Boolean isInterestCoverageDisplay = false;

	private Boolean isInterestCoverageMandatory = false;

	private BigDecimal minTolTnw;

	private BigDecimal maxTolTnw;

	private Boolean isTolTnwDisplay = false;

	private Boolean isTolTnwMandatory = false;

	private BigDecimal minTurnoverRatio;

	private BigDecimal maxTurnoverRatio;

	private Boolean isTurnoverRatioDisplay = false;

	private Boolean isTurnoverRatioMandatory = false;

	private BigDecimal minGrossCashAccuralsRatio;

	private BigDecimal maxGrossCashAccuralsRatio;

	private Boolean isGrossCashAccuralsRatioDisplay = false;

	private Boolean isGrossCashAccuralsRatioMandatory = false;

	private BigDecimal minCustomerConcentration;

	private BigDecimal maxCustomerConcentration;

	private Boolean isCustomerConcentrationDisplay = false;

	private Boolean isCustomerConcentrationMandatory =false;

	private Integer minRiskModelScore;

	private Integer maxRiskModelScore;

	private Boolean isRiskModelScoreDisplay = false;

	private Boolean isRiskModelScoreMandatory = false;

	private Integer netWorth;

	private Integer minChequeBounced;

	private Integer maxChequeBounced;

	private Boolean isChequeBouncedDisplay = false;

	private Boolean isChequeBouncedMandatory = false;

	private Integer minChequeBouncedLastSixMonths;

	private Integer maxChequeBouncedLastSixMonths;

	private Boolean isChequeBouncedLastSixMonthsDisplay = false;

	private Boolean isChequeBouncedLastSixMonthsMandatory = false;

	private Integer ddrFlow;

	private Integer individualCibil;

	private Boolean isIndividualCibilDisplay = false;

	private Boolean isIndividualCibilMandatory = false;

	private Integer commercialCibil;

	private Boolean isCommercialCibilDisplay = false;

	private Boolean isCommercialCibilMandatory = false;

	private Integer appstage;
	//-----------------------added eligibility method for product
	private Integer assessmentMethodId;
	
	private Long userOrgId;

	private BigDecimal minCgtmseCoverage;
	private BigDecimal maxCgtmseCoverage;
	private Boolean isCgtmseCoverageDisplay = false;
	private Boolean isCgtmseCoverageMandatory = false;
	private Boolean isMsmeFundingDisplay = false;
	private Boolean isMsmeFundingMandatory = false;
	private List<Integer> msmeFundingIds;
	
	private Integer cgtmseCoverage;
	
	private Integer cashMargin;
	
	private BigDecimal dscr;
	
	private Integer paybackPeriod;
	
/*ntb*/
	
	private BigDecimal minAvrgAge;

	private BigDecimal maxAvrgAge;
	
	private BigDecimal minAvrgYearlyIncome;
	
	private BigDecimal maxAvrgYearlyIncome;

	private BigDecimal mincgtmse;
	
	private BigDecimal maxcgtmse;

	private BigDecimal minLoanToAsset;
	
	private BigDecimal maxLoanToAsset;
	
	private BigDecimal minAvgWorkExp;
	
	private BigDecimal maxAvgWorkExp;
	
	private BigDecimal minCurrentFoir;
	
	private BigDecimal maxCurrentFoir;
	
	private Boolean isAvrgAgeDisplay = false;

	private Boolean isAvrgAgeMandatory = false;
	
	private Boolean isAvrgYearlyIncomeDisplay = false;

	private Boolean isAvrgYearlyIncomeMandatory = false;

	private Boolean iscgtmseDisplay = false;

	private Boolean iscgtmseMandatory = false;
	
	private Boolean isLoanToAssetDisplay = false;

	private Boolean isLoanToAssetMandatory = false;

	private Boolean isAvgWorkExpDisplay = false;

	private Boolean isAvgWorkExpMandatory = false;
	
	private Boolean isCurrentFoirDisplay = false;

	private Boolean isCurrentFoirMandatory = false;

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

	private List<DataRequest> industrylist = Collections.emptyList();

	private List<DataRequest> sectorlist = Collections.emptyList();

	private List<DataRequest> countryList = Collections.emptyList();

	private List<DataRequest> stateList = Collections.emptyList();

	private List<DataRequest> cityList = Collections.emptyList();
	
	private List<DataRequest> unInterestedIndustrylist = Collections.emptyList();

	public Integer getCurrency() {
		return currency;
	}

	public void setCurrency(Integer currency) {
		this.currency = currency;
	}

	public Integer getDenomination() {
		return denomination;
	}

	public void setDenomination(Integer denomination) {
		this.denomination = denomination;
	}

	public Boolean getIsCollateralDisplay() {
		return isCollateralDisplay;
	}

	public void setIsCollateralDisplay(Boolean isCollateralDisplay) {
		this.isCollateralDisplay = isCollateralDisplay;
	}

	public Boolean getIsCollateralMandatory() {
		return isCollateralMandatory;
	}

	public void setIsCollateralMandatory(Boolean isCollateralMandatory) {
		this.isCollateralMandatory = isCollateralMandatory;
	}

	public Boolean getIsCreditRatingDisplay() {
		return isCreditRatingDisplay;
	}

	public void setIsCreditRatingDisplay(Boolean isCreditRatingDisplay) {
		this.isCreditRatingDisplay = isCreditRatingDisplay;
	}

	public Boolean getIsCreditRatingMandatory() {
		return isCreditRatingMandatory;
	}

	public void setIsCreditRatingMandatory(Boolean isCreditRatingMandatory) {
		this.isCreditRatingMandatory = isCreditRatingMandatory;
	}

	public Boolean getIsDebtEquityDisplay() {
		return isDebtEquityDisplay;
	}

	public void setIsDebtEquityDisplay(Boolean isDebtEquityDisplay) {
		this.isDebtEquityDisplay = isDebtEquityDisplay;
	}

	public Boolean getIsDebtEquityMandatory() {
		return isDebtEquityMandatory;
	}

	public void setIsDebtEquityMandatory(Boolean isDebtEquityMandatory) {
		this.isDebtEquityMandatory = isDebtEquityMandatory;
	}

	public Boolean getIsEstablishmentDisplay() {
		return isEstablishmentDisplay;
	}

	public void setIsEstablishmentDisplay(Boolean isEstablishmentDisplay) {
		this.isEstablishmentDisplay = isEstablishmentDisplay;
	}

	public Boolean getIsEstablishmentMandatory() {
		return isEstablishmentMandatory;
	}

	public void setIsEstablishmentMandatory(Boolean isEstablishmentMandatory) {
		this.isEstablishmentMandatory = isEstablishmentMandatory;
	}

	public Boolean getIsGeographicalDisplay() {
		return isGeographicalDisplay;
	}

	public void setIsGeographicalDisplay(Boolean isGeographicalDisplay) {
		this.isGeographicalDisplay = isGeographicalDisplay;
	}

	public Boolean getIsGeographicalMandatory() {
		return isGeographicalMandatory;
	}

	public void setIsGeographicalMandatory(Boolean isGeographicalMandatory) {
		this.isGeographicalMandatory = isGeographicalMandatory;
	}

	public Boolean getIsIndustrySectorDisplay() {
		return isIndustrySectorDisplay;
	}

	public void setIsIndustrySectorDisplay(Boolean isIndustrySectorDisplay) {
		this.isIndustrySectorDisplay = isIndustrySectorDisplay;
	}

	public Boolean getIsIndustrySectorMandatory() {
		return isIndustrySectorMandatory;
	}

	public void setIsIndustrySectorMandatory(Boolean isIndustrySectorMandatory) {
		this.isIndustrySectorMandatory = isIndustrySectorMandatory;
	}

	public Boolean getIsInvestmentSizeDisplay() {
		return isInvestmentSizeDisplay;
	}

	public void setIsInvestmentSizeDisplay(Boolean isInvestmentSizeDisplay) {
		this.isInvestmentSizeDisplay = isInvestmentSizeDisplay;
	}

	public Boolean getIsInvestmentSizeMandatory() {
		return isInvestmentSizeMandatory;
	}

	public void setIsInvestmentSizeMandatory(Boolean isInvestmentSizeMandatory) {
		this.isInvestmentSizeMandatory = isInvestmentSizeMandatory;
	}

	public Boolean getIsNetworthDisplay() {
		return isNetworthDisplay;
	}

	public void setIsNetworthDisplay(Boolean isNetworthDisplay) {
		this.isNetworthDisplay = isNetworthDisplay;
	}

	public Boolean getIsNetworthMandatory() {
		return isNetworthMandatory;
	}

	public void setIsNetworthMandatory(Boolean isNetworthMandatory) {
		this.isNetworthMandatory = isNetworthMandatory;
	}

	public Boolean getIsPastYearTurnoverDisplay() {
		return isPastYearTurnoverDisplay;
	}

	public void setIsPastYearTurnoverDisplay(Boolean isPastYearTurnoverDisplay) {
		this.isPastYearTurnoverDisplay = isPastYearTurnoverDisplay;
	}

	public Boolean getIsPastYearTurnoverMandatory() {
		return isPastYearTurnoverMandatory;
	}

	public void setIsPastYearTurnoverMandatory(Boolean isPastYearTurnoverMandatory) {
		this.isPastYearTurnoverMandatory = isPastYearTurnoverMandatory;
	}

	public Boolean getIsProfitabilityHistoryDisplay() {
		return isProfitabilityHistoryDisplay;
	}

	public void setIsProfitabilityHistoryDisplay(Boolean isProfitabilityHistoryDisplay) {
		this.isProfitabilityHistoryDisplay = isProfitabilityHistoryDisplay;
	}

	public Boolean getIsProfitabilityHistoryMandatory() {
		return isProfitabilityHistoryMandatory;
	}

	public void setIsProfitabilityHistoryMandatory(Boolean isProfitabilityHistoryMandatory) {
		this.isProfitabilityHistoryMandatory = isProfitabilityHistoryMandatory;
	}

	public Boolean getIsTenureDisplay() {
		return isTenureDisplay;
	}

	public void setIsTenureDisplay(Boolean isTenureDisplay) {
		this.isTenureDisplay = isTenureDisplay;
	}

	public Boolean getIsTenureMandatory() {
		return isTenureMandatory;
	}

	public void setIsTenureMandatory(Boolean isTenureMandatory) {
		this.isTenureMandatory = isTenureMandatory;
	}

	public Integer getLongTermCreditRating() {
		return LongTermCreditRating;
	}

	public void setLongTermCreditRating(Integer longTermCreditRating) {
		LongTermCreditRating = longTermCreditRating;
	}

	public Integer getMaxAgeEstablishment() {
		return maxAgeEstablishment;
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

	public BigDecimal getMaxDebtEquity() {
		return maxDebtEquity;
	}

	public void setMaxDebtEquity(BigDecimal maxDebtEquity) {
		this.maxDebtEquity = maxDebtEquity;
	}

	public void setMinDebtEquity(BigDecimal minDebtEquity) {
		this.minDebtEquity = minDebtEquity;
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

	public Integer getMinAgeEstablishment() {
		return minAgeEstablishment;
	}

	public void setMinAgeEstablishment(Integer minAgeEstablishment) {
		this.minAgeEstablishment = minAgeEstablishment;
	}

	public BigDecimal getMinDebtEquity() {
		return minDebtEquity;
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

	public Integer getProfitabilityHistory() {
		return profitabilityHistory;
	}

	public void setProfitabilityHistory(Integer profitabilityHistory) {
		this.profitabilityHistory = profitabilityHistory;
	}

	public Integer getShortTermCreditRating() {
		return shortTermCreditRating;
	}

	public void setShortTermCreditRating(Integer shortTermCreditRating) {
		this.shortTermCreditRating = shortTermCreditRating;
	}

	public Long getUninterestedIndustry() {
		return uninterestedIndustry;
	}

	public void setUninterestedIndustry(Long uninterestedIndustry) {
		this.uninterestedIndustry = uninterestedIndustry;
	}

	public List<DataRequest> getIndustrylist() {
		return industrylist;
	}

	public void setIndustrylist(List<DataRequest> industrylist) {
		this.industrylist = industrylist;
	}

	public List<DataRequest> getSectorlist() {
		return sectorlist;
	}

	public void setSectorlist(List<DataRequest> sectorlist) {
		this.sectorlist = sectorlist;
	}

	public List<DataRequest> getCountryList() {
		return countryList;
	}

	public void setCountryList(List<DataRequest> countryList) {
		this.countryList = countryList;
	}

	public List<DataRequest> getStateList() {
		return stateList;
	}

	public void setStateList(List<DataRequest> stateList) {
		this.stateList = stateList;
	}

	public List<DataRequest> getCityList() {
		return cityList;
	}

	public void setCityList(List<DataRequest> cityList) {
		this.cityList = cityList;
	}

	public List<DataRequest> getUnInterestedIndustrylist() {
		return unInterestedIndustrylist;
	}

	public void setUnInterestedIndustrylist(List<DataRequest> unInterestedIndustrylist) {
		this.unInterestedIndustrylist = unInterestedIndustrylist;
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

	public Long getUserOrgId() {
		return userOrgId;
	}

	public void setUserOrgId(Long userOrgId) {
		this.userOrgId = userOrgId;
	}

	public Integer getAppstage() {
		return appstage;
	}

	public void setAppstage(Integer appstage) {
		this.appstage = appstage;
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


	public List<Integer> getMsmeFundingIds() {
		return msmeFundingIds;
	}

	public void setMsmeFundingIds(List<Integer> msmeFundingIds) {
		this.msmeFundingIds = msmeFundingIds;
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


	public BigDecimal getMinAvrgAge() {
		return minAvrgAge;
	}

	public void setMinAvrgAge(BigDecimal minAvrgAge) {
		this.minAvrgAge = minAvrgAge;
	}

	public BigDecimal getMaxAvrgAge() {
		return maxAvrgAge;
	}

	public void setMaxAvrgAge(BigDecimal maxAvrgAge) {
		this.maxAvrgAge = maxAvrgAge;
	}



	public BigDecimal getMinAvrgYearlyIncome() {
		return minAvrgYearlyIncome;
	}

	public void setMinAvrgYearlyIncome(BigDecimal minAvrgYearlyIncome) {
		this.minAvrgYearlyIncome = minAvrgYearlyIncome;
	}

	public BigDecimal getMaxAvrgYearlyIncome() {
		return maxAvrgYearlyIncome;
	}

	public void setMaxAvrgYearlyIncome(BigDecimal maxAvrgYearlyIncome) {
		this.maxAvrgYearlyIncome = maxAvrgYearlyIncome;
	}



	public BigDecimal getMincgtmse() {
		return mincgtmse;
	}

	public void setMincgtmse(BigDecimal mincgtmse) {
		this.mincgtmse = mincgtmse;
	}

	
	public BigDecimal getMaxcgtmse() {
		return maxcgtmse;
	}

	public void setMaxcgtmse(BigDecimal maxcgtmse) {
		this.maxcgtmse = maxcgtmse;
	}

	public BigDecimal getMinLoanToAsset() {
		return minLoanToAsset;
	}

	public void setMinLoanToAsset(BigDecimal minLoanToAsset) {
		this.minLoanToAsset = minLoanToAsset;
	}

	public BigDecimal getMaxLoanToAsset() {
		return maxLoanToAsset;
	}

	public void setMaxLoanToAsset(BigDecimal maxLoanToAsset) {
		this.maxLoanToAsset = maxLoanToAsset;
	}

	public BigDecimal getMinAvgWorkExp() {
		return minAvgWorkExp;
	}

	public void setMinAvgWorkExp(BigDecimal minAvgWorkExp) {
		this.minAvgWorkExp = minAvgWorkExp;
	}

	public BigDecimal getMaxAvgWorkExp() {
		return maxAvgWorkExp;
	}

	public void setMaxAvgWorkExp(BigDecimal maxAvgWorkExp) {
		this.maxAvgWorkExp = maxAvgWorkExp;
	}

	public BigDecimal getMinCurrentFoir() {
		return minCurrentFoir;
	}

	public void setMinCurrentFoir(BigDecimal minCurrentFoir) {
		this.minCurrentFoir = minCurrentFoir;
	}

	public BigDecimal getMaxCurrentFoir() {
		return maxCurrentFoir;
	}

	public void setMaxCurrentFoir(BigDecimal maxCurrentFoir) {
		this.maxCurrentFoir = maxCurrentFoir;
	}


	public Boolean getIsAvrgAgeDisplay() {
		return isAvrgAgeDisplay;
	}

	public void setIsAvrgAgeDisplay(Boolean isAvrgAgeDisplay) {
		this.isAvrgAgeDisplay = isAvrgAgeDisplay;
	}

	public Boolean getIsAvrgAgeMandatory() {
		return isAvrgAgeMandatory;
	}

	public void setIsAvrgAgeMandatory(Boolean isAvrgAgeMandatory) {
		this.isAvrgAgeMandatory = isAvrgAgeMandatory;
	}



	public Boolean getIsAvrgYearlyIncomeDisplay() {
		return isAvrgYearlyIncomeDisplay;
	}

	public void setIsAvrgYearlyIncomeDisplay(Boolean isAvrgYearlyIncomeDisplay) {
		this.isAvrgYearlyIncomeDisplay = isAvrgYearlyIncomeDisplay;
	}

	public Boolean getIsAvrgYearlyIncomeMandatory() {
		return isAvrgYearlyIncomeMandatory;
	}

	public void setIsAvrgYearlyIncomeMandatory(Boolean isAvrgYearlyIncomeMandatory) {
		this.isAvrgYearlyIncomeMandatory = isAvrgYearlyIncomeMandatory;
	}

	
	
	public Boolean getIscgtmseDisplay() {
		return iscgtmseDisplay;
	}

	public void setIscgtmseDisplay(Boolean iscgtmseDisplay) {
		this.iscgtmseDisplay = iscgtmseDisplay;
	}

	public Boolean getIscgtmseMandatory() {
		return iscgtmseMandatory;
	}

	public void setIscgtmseMandatory(Boolean iscgtmseMandatory) {
		this.iscgtmseMandatory = iscgtmseMandatory;
	}

	public Boolean getIsLoanToAssetDisplay() {
		return isLoanToAssetDisplay;
	}

	public void setIsLoanToAssetDisplay(Boolean isLoanToAssetDisplay) {
		this.isLoanToAssetDisplay = isLoanToAssetDisplay;
	}

	public Boolean getIsLoanToAssetMandatory() {
		return isLoanToAssetMandatory;
	}

	public void setIsLoanToAssetMandatory(Boolean isLoanToAssetMandatory) {
		this.isLoanToAssetMandatory = isLoanToAssetMandatory;
	}

	public Boolean getIsAvgWorkExpDisplay() {
		return isAvgWorkExpDisplay;
	}

	public void setIsAvgWorkExpDisplay(Boolean isAvgWorkExpDisplay) {
		this.isAvgWorkExpDisplay = isAvgWorkExpDisplay;
	}

	public Boolean getIsAvgWorkExpMandatory() {
		return isAvgWorkExpMandatory;
	}

	public void setIsAvgWorkExpMandatory(Boolean isAvgWorkExpMandatory) {
		this.isAvgWorkExpMandatory = isAvgWorkExpMandatory;
	}

	public Boolean getIsCurrentFoirDisplay() {
		return isCurrentFoirDisplay;
	}

	public void setIsCurrentFoirDisplay(Boolean isCurrentFoirDisplay) {
		this.isCurrentFoirDisplay = isCurrentFoirDisplay;
	}

	public Boolean getIsCurrentFoirMandatory() {
		return isCurrentFoirMandatory;
	}

	public void setIsCurrentFoirMandatory(Boolean isCurrentFoirMandatory) {
		this.isCurrentFoirMandatory = isCurrentFoirMandatory;
	}

	public BigDecimal getMinTenureNtb() {
		return minTenureNtb;
	}

	public void setMinTenureNtb(BigDecimal minTenureNtb) {
		this.minTenureNtb = minTenureNtb;
	}

	public BigDecimal getMaxTenureNtb() {
		return maxTenureNtb;
	}

	public void setMaxTenureNtb(BigDecimal maxTenureNtb) {
		this.maxTenureNtb = maxTenureNtb;
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



	
	
	
	
	
}
