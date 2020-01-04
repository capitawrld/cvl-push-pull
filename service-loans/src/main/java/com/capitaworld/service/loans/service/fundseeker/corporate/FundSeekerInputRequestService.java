package com.capitaworld.service.loans.service.fundseeker.corporate;

import java.util.List;

import com.capitaworld.service.loans.exceptions.LoansException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.capitaworld.connect.api.ConnectResponse;
import com.capitaworld.service.gst.GstResponse;
import com.capitaworld.service.loans.model.LoansResponse;
import com.capitaworld.service.loans.model.NTBRequest;
import com.capitaworld.service.loans.model.corporate.FundSeekerInputRequestResponse;
import com.capitaworld.service.loans.model.corporate.PrimaryCorporateDetailMudraLoanReqRes;

public interface FundSeekerInputRequestService {

    public boolean saveOrUpdate(FundSeekerInputRequestResponse fundSeekerInputRequest) throws LoansException;

    public ResponseEntity<LoansResponse> saveOrUpdateDirectorDetail(FundSeekerInputRequestResponse fundSeekerInputRequest);

    public ResponseEntity<LoansResponse> get(FundSeekerInputRequestResponse fundSeekerInputRequest);

    public ResponseEntity<LoansResponse> getDirectorDetail(FundSeekerInputRequestResponse fundSeekerInputRequest);
    
    public LoansResponse callMatchEngineClient(Long applicationId,Long userId,Integer businessTypeId,Boolean isNbfcUser);
    
    
    /**
     * Use to call after Director Details saved.
     * @param ntbRequest
     * @return
     */
    public LoansResponse postDirectorBackground(NTBRequest ntbRequest);

	/**
	 * @param fundSeekerInputRequestResponse
	 * @throws Exception 
	 */
	public LoansResponse invokeFraudAnalytics(FundSeekerInputRequestResponse fundSeekerInputRequestResponse) throws LoansException;

	/**
	 * Verify GST whether the GSTIN is Registered or Not.
	 * @param gstin
	 * @param applicationId
	 * @param userId
	 * @param uploadedFile
	 * @return
	 */
	public LoansResponse verifyGST(String gstin,Long applicationId,Long userId,MultipartFile[] uploadedFile);


	/**
	 * Getting Data for Uniform Product OneForm
	 * @param applicationId
	 * @return
	 */
	public LoansResponse getDataForOnePagerOneForm(Long applicationId);


	/**
	 * Update ITR Flag whether its is completed or not
	 * @param applicationId
	 * @param flag
	 * @param flagType
	 * @return
	 */
	public LoansResponse updateFlag(Long applicationId,Boolean flag,Integer flagType);

	/**
	 * Saving OneForm for Uniform Product
	 * @param fundSeekerInputRequest
	 * @return
	 */
	public LoansResponse saveOrUpdateForOnePagerEligibility(FundSeekerInputRequestResponse fundSeekerInputRequest);

	/**
	 * Deleting Document and Return the remaining list of Document for the givem mapping Id
	 * @param applicationId
	 * @param docIds
	 * @param mappingId
	 * @return
	 */
	public LoansResponse deleteDocument(Long applicationId,List<Long> docIds,Long mappingId);

	/**
	 * Inactive all the information for given applicationId for Uniform Product.
	 * @param connectResponse
	 * @return
	 */
	public LoansResponse resetUniformApplication(ConnectResponse connectResponse);
	
	/**
	 * Save Statutory Obligation info
	 * @param reqRes
	 * @return
	 * @throws LoansException
	 */
	public boolean saveOrUpdateStatutoryObligation(PrimaryCorporateDetailMudraLoanReqRes reqRes) throws LoansException;
	
	/**
	 * Get Statutory Obligation by applicationId
	 * @param reqRes
	 * @return
	 * @throws LoansException
	 */
	public PrimaryCorporateDetailMudraLoanReqRes getStatutoryObligationByApplicationId(Long applicationId) throws LoansException;
	
}
