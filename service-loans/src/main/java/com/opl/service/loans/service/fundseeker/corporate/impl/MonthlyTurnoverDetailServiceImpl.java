package com.opl.service.loans.service.fundseeker.corporate.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opl.mudra.api.loans.exception.LoansException;
import com.opl.mudra.api.loans.model.FrameRequest;
import com.opl.mudra.api.loans.model.MonthlyTurnoverDetailRequest;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.mudra.api.loans.utils.MultipleJSONObjectHelper;
import com.opl.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.opl.service.loans.domain.fundseeker.LoanApplicationMaster;
import com.opl.service.loans.domain.fundseeker.corporate.MonthlyTurnoverDetail;
import com.opl.service.loans.repository.fundseeker.corporate.MonthlyTurnoverDetailRepository;
import com.opl.service.loans.service.fundseeker.corporate.MonthlyTurnoverDetailService;

/**
 * @author Sanket
 *
 */
@Service
@Transactional
public class MonthlyTurnoverDetailServiceImpl implements MonthlyTurnoverDetailService {

	private static final Logger logger = LoggerFactory.getLogger(MonthlyTurnoverDetailServiceImpl.class.getName());
	@Autowired
	private MonthlyTurnoverDetailRepository monthlyTurnoverDetailsRepository;

	@Override
	public Boolean saveOrUpdate(FrameRequest frameRequest) throws LoansException {
		try {
			for (Map<String, Object> obj : frameRequest.getDataList()) {
				MonthlyTurnoverDetailRequest monthlyTurnoverDetailRequest = (MonthlyTurnoverDetailRequest) MultipleJSONObjectHelper
						.getObjectFromMap(obj, MonthlyTurnoverDetailRequest.class);
				MonthlyTurnoverDetail monthlyTurnoverDetail = null;

				if (monthlyTurnoverDetailRequest.getId() != null) {
					monthlyTurnoverDetail = monthlyTurnoverDetailsRepository
							.findOne(monthlyTurnoverDetailRequest.getId());
				} else {
					monthlyTurnoverDetail = new MonthlyTurnoverDetail();
					monthlyTurnoverDetail.setCreatedBy(frameRequest.getUserId());
					monthlyTurnoverDetail.setCreatedDate(new Date());
				}
				BeanUtils.copyProperties(monthlyTurnoverDetailRequest, monthlyTurnoverDetail);
				monthlyTurnoverDetail.setApplicationId(new LoanApplicationMaster(frameRequest.getApplicationId()));
				monthlyTurnoverDetail.setProposalMapping(new ApplicationProposalMapping(frameRequest.getProposalMappingId()));
				monthlyTurnoverDetail.setModifiedBy(frameRequest.getUserId());
				monthlyTurnoverDetail.setModifiedDate(new Date());
				monthlyTurnoverDetailsRepository.save(monthlyTurnoverDetail);
			}
			return true;
		}

		catch (Exception e) {
			logger.error("Exception in save monthlyTurnoverDetail :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}

	}

	@Override
	public List<MonthlyTurnoverDetailRequest> getMonthlyTurnoverDetailListByProposalId(Long id,Long userId,Long proposalId) throws LoansException {
		try {
			List<MonthlyTurnoverDetail> monthlyTurnoverDetails = monthlyTurnoverDetailsRepository
					.listMonthlyTurnoverFromAppIdAndProposalId(id,proposalId);

			if(CommonUtils.isListNullOrEmpty(monthlyTurnoverDetails)){
				return getList();
			}
		
			List<MonthlyTurnoverDetailRequest> monthlyTurnoverDetailRequests = new ArrayList<MonthlyTurnoverDetailRequest>();

			for (MonthlyTurnoverDetail detail : monthlyTurnoverDetails) {
				MonthlyTurnoverDetailRequest monthlyTurnoverDetailRequest = new MonthlyTurnoverDetailRequest();
				monthlyTurnoverDetailRequest.setAmountString(CommonUtils.convertValue(detail.getAmount()));
				BeanUtils.copyProperties(detail, monthlyTurnoverDetailRequest);
				monthlyTurnoverDetailRequests.add(monthlyTurnoverDetailRequest);
			}
			return monthlyTurnoverDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception in get List monthlyTurnoverDetail :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}
	
	@Override
	public List<MonthlyTurnoverDetailRequest> getMonthlyTurnoverDetailList(Long id,Long userId) throws LoansException {
		try {
			List<MonthlyTurnoverDetail> monthlyTurnoverDetails = monthlyTurnoverDetailsRepository
					.listMonthlyTurnoverFromAppId(id,userId);

			if(CommonUtils.isListNullOrEmpty(monthlyTurnoverDetails)){
				return getList();
			}
		
			List<MonthlyTurnoverDetailRequest> monthlyTurnoverDetailRequests = new ArrayList<MonthlyTurnoverDetailRequest>();

			for (MonthlyTurnoverDetail detail : monthlyTurnoverDetails) {
				MonthlyTurnoverDetailRequest monthlyTurnoverDetailRequest = new MonthlyTurnoverDetailRequest();
				monthlyTurnoverDetailRequest.setAmountString(CommonUtils.convertValue(detail.getAmount()));
				BeanUtils.copyProperties(detail, monthlyTurnoverDetailRequest);
				monthlyTurnoverDetailRequests.add(monthlyTurnoverDetailRequest);
			}
			return monthlyTurnoverDetailRequests;
		}

		catch (Exception e) {
			logger.error("Exception in get List monthlyTurnoverDetail :- ",e);
			throw new LoansException(CommonUtils.SOMETHING_WENT_WRONG);
		}
	}

	public List<MonthlyTurnoverDetailRequest> getList() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// get current date time with Calendar()
		Calendar cal = Calendar.getInstance();
		int currentYear = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		int currentDate = cal.get(Calendar.DATE);
		Date comparisonDate = null;
		Date todayDate = null;
		List<MonthlyTurnoverDetailRequest> yearList = new ArrayList<>();
		try {
			comparisonDate = dateFormat.parse(currentYear + "-" + currentMonth + "-15");
			todayDate = dateFormat.parse(dateFormat.format(cal.getTime()));
			if (todayDate.compareTo(comparisonDate) > 0) {
				int year = cal.get(Calendar.YEAR);
				cal.set(year, currentMonth - 1, currentDate);
				for (int i = 0; i < 12; i++) {
					cal.add(Calendar.MONTH, -1);
					yearList.add(new MonthlyTurnoverDetailRequest(
							cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) + " - "
									+ cal.get(Calendar.YEAR)));
				}
			} else if (todayDate.compareTo(comparisonDate) <= 0) {
				logger.info("Comparison Date is before Today's Date");
				int year = cal.get(Calendar.YEAR);
				cal.set(year, currentMonth - 2, currentDate);
				for (int i = 0; i < 12; i++) {
					cal.add(Calendar.MONTH, -1);
					yearList.add(new MonthlyTurnoverDetailRequest(
							cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) + " - "
									+ cal.get(Calendar.YEAR)));
				}
			}

		} catch (ParseException e) {
			logger.error("Exception in getList : ",e);
		}
		return yearList;
	}

}
