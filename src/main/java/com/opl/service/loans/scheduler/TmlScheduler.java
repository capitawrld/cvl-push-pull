package com.opl.service.loans.scheduler;

import java.nio.charset.StandardCharsets;

import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.opl.mudra.api.common.MultipleJSONObjectHelper;
import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.model.pushpull.PushPullRequest;
import com.opl.service.loans.model.pushpull.TmlRootRequest;
import com.opl.service.loans.repository.common.TataMotorsLoanDetailsRepository;
import com.opl.service.loans.service.pushpull.PushPullApplicationService;

@Component
public class TmlScheduler {

	private static final Logger logger = LoggerFactory.getLogger(TmlScheduler.class.getName());
	protected static final String TML_URL = "cw.tmlPushPull.scheduler.url";

	@Autowired
	private PushPullApplicationService pushPullApplicationService;

	@Autowired
	private Environment environment;
	
	@Autowired
	private TataMotorsLoanDetailsRepository tataMotorsLoanDetailsRepository;

	@Scheduled(fixedDelayString = "${cw.tmlPushPull.scheduler.timeout}")
	public void run() {
		logger.info("Entry ScheduledTasks");
		try {
			String url = environment.getProperty(TML_URL);

			logger.info("Schedule Call................. ");

			PushPullRequest pushPullRequest = new PushPullRequest();

			pushPullRequest.setFinancierId("1-7DSGIBS");
			pushPullRequest.setClientId(93571l);
			pushPullRequest.setOffset(0l);

			
			//get last offset of tml data
			String offset=tataMotorsLoanDetailsRepository.getLastOffset();
			
			JSONObject json = new JSONObject();
			json.put("financier_id", "1-7DSGIBS");
			json.put("client_id", "93571");
			json.put("offset", CommonUtils.isObjectNullOrEmpty(offset)?"0":offset+1);
			//json.put("offset", "0");

			StringEntity params = new StringEntity(json.toString());
			
			TmlRootRequest tmlRootRequest = null;
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				final HttpPost httpPost = new HttpPost(url);
				httpPost.setEntity(params);
				// httpPost.addHeader("auth_key", "U0Gs3xVPjmBmZuwjV4lbt4S");
				httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
				httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Bearer U0Gs3xVPjmBmZuwjV4lbt4S");
				try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
					StatusLine statusLine = response.getStatusLine();
					System.out.println(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
					if(statusLine.getStatusCode() != 200) {
					return;	
					}
					String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//					System.out.println("Response body: " + responseBody);
					tmlRootRequest = MultipleJSONObjectHelper.getObjectFromString(responseBody,TmlRootRequest.class);
					tmlRootRequest.setResponseBody(responseBody);
					tmlRootRequest.setRequest(json);
					System.out.println("Response body: copied");
				}

			}

			Long id=pushPullApplicationService.saveTataMotorsReqResDetails(tmlRootRequest);
			tmlRootRequest.setId(id);
			pushPullApplicationService.saveTataMotorsLoanDetails(tmlRootRequest);
			

			logger.info("Exit ScheduledTasks");
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION, e);
		}
	}

}
