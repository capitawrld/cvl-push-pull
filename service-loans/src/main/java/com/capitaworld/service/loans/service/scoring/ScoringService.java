package com.capitaworld.service.loans.service.scoring;

import java.io.IOException;

import java.util.List;

import com.capitaworld.service.loans.domain.fundseeker.corporate.FinancialArrangementsDetail;
import com.capitaworld.service.loans.domain.fundseeker.corporate.PrimaryCorporateDetail;
import com.capitaworld.service.loans.domain.fundseeker.retail.BankingRelation;
import com.capitaworld.service.scoring.MCLRReqRes;
import com.capitaworld.service.scoring.exception.ScoringException;
import com.capitaworld.service.scoring.model.GenericCheckerReqRes;
import com.capitaworld.service.scoring.model.ScoringResponse;
import com.capitaworld.service.scoring.model.scoringmodel.ScoringModelReqRes;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.capitaworld.service.loans.exceptions.LoansException;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.score.ScoringRequestLoans;

public interface ScoringService {

    public ResponseEntity<LoansResponse> calculateScoring(ScoringRequestLoans scoringRequestLoans);

    public ResponseEntity<LoansResponse> calculateExistingBusinessScoring(ScoringRequestLoans scoringRequestLoans);

    public ResponseEntity<LoansResponse> calculateNTBScoring(ScoringRequestLoans scoringRequestLoans, PrimaryCorporateDetail primaryCorporateDetail);

    public ResponseEntity<LoansResponse> calculateExistingBusinessScoringList(List<ScoringRequestLoans> scoringRequestLoansList);

    public ResponseEntity<LoansResponse> calculateRetailPersonalLoanScoringList(List<ScoringRequestLoans> scoringRequestLoansList);
    
    public ResponseEntity<LoansResponse> calculateRetailHomeLoanScoringList(List<ScoringRequestLoans> scoringRequestLoansList);
    
    public ResponseEntity<LoansResponse> calculateRetailHomeLoanScoringListForCoApplicant(List<ScoringRequestLoans> scoringRequestLoansList);

    public ResponseEntity<LoansResponse> calculateMFILoanScoringList(List<ScoringRequestLoans> scoringRequestLoansList);

    //////////////

    public ResponseEntity<LoansResponse> calculateScoringTest(ScoringRequestLoans scoringRequestLoans);
    
	public Workbook  readScoringExcel(MultipartFile multipartFile ) throws IllegalStateException, InvalidFormatException,IOException , LoansException;
	
	public Workbook generateScoringExcel(List<LoansResponse> list) throws LoansException ;

	/////////

    public ScoringModelReqRes getScoringModelTempList(ScoringModelReqRes scoringModelReqRes);

    public ScoringModelReqRes saveScoringModelTemp(ScoringModelReqRes scoringModelReqRes);

    public ScoringModelReqRes getScoringModelTempDetail(ScoringModelReqRes scoringModelReqRes);

    public ScoringModelReqRes saveScoringModelTempDetail(ScoringModelReqRes scoringModelReqRes);
    
    public List<GenericCheckerReqRes> sendToChecker(List <GenericCheckerReqRes> genericCheckerReqRes , Long userId)  throws ScoringException ;
    
    public ScoringModelReqRes getScoringModelMasterList(ScoringModelReqRes scoringModelReqRes);
    
    public ScoringModelReqRes getScoringModelMasterDetail(ScoringModelReqRes scoringModelReqRes);

    public Integer getFinYear(Long applicationId);

    public ScoringResponse getMCLRHistoryDetail(MCLRReqRes mclrReqRes);

    public ScoringResponse getLatestMCLRDetails(MCLRReqRes mclrReqRes);

    public ScoringResponse getMCLRForChecker(MCLRReqRes mclrReqRes);

    public ScoringResponse getEffectiveMCLRDetails(MCLRReqRes mclrReqRes);

    public ScoringResponse createJob(MCLRReqRes mclrReqRes);

    public ScoringResponse saveMCLRDetails(MCLRReqRes mclrReqRes);

    public List<GenericCheckerReqRes> sendToCheckerMCLR(List <GenericCheckerReqRes> genericCheckerReqRes , Long userId)  throws ScoringException ;
    
    public Object [] getRetailConcessionDetails(ScoringRequestLoans scoringRequestLoans,List<String> bankStringsList,List<BankingRelation> bankingRelationList,List<FinancialArrangementsDetail> financialArrangementsDetailList);
    
}
