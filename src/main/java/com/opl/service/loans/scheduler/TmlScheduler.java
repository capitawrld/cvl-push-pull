package com.opl.service.loans.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.opl.mudra.api.loans.utils.CommonUtils;
import com.opl.service.loans.model.pushpull.PushPullRequest;
import com.opl.service.loans.service.pushpull.PushPullApplicationService;

@Component
public class TmlScheduler {

	private static final Logger logger = LoggerFactory.getLogger(TmlScheduler.class.getName());
	protected static final String TML_URL = "cw.tmlPushPull.scheduler.url";

	@Autowired
	private PushPullApplicationService pushPullApplicationService;

	@Autowired
	private Environment environment;

	private String urlDone = environment.getRequiredProperty(TML_URL);

	@Scheduled(fixedDelayString = "${cw.tmlPushPull.scheduler.timeout}")
	public void run() {
		logger.info("Entry ScheduledTasks");
		try {

			logger.info("Schedule Call................. ");

			PushPullRequest pushPullRequest = new PushPullRequest();

			pushPullRequest.setFinancierId("1-7DSGIBS");
			pushPullRequest.setClientId(93571l);
			pushPullRequest.setOffset(0l);

			List<NameValuePair> loginApiform = new ArrayList<>();
			loginApiform.add(new BasicNameValuePair("financierId", "1-7DSGIBS"));
			loginApiform.add(new BasicNameValuePair("clientId", "93571"));
			loginApiform.add(new BasicNameValuePair("offset", "0"));

			UrlEncodedFormEntity transactionComplete = new UrlEncodedFormEntity(loginApiform, Consts.UTF_8);

			HttpPost httpPostStatement = new HttpPost(environment.getProperty(TML_URL));
			httpPostStatement.setEntity(transactionComplete);
			
			  try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				  
				  HttpResponse responseStatement = httpclient.execute(httpPostStatement);
				  
			  }catch (Exception e) {
				// TODO: handle exception
			}
			
			

//			pushPullApplicationService.saveOrUpdate(pushPullRequest);

			logger.info("Exit ScheduledTasks");
		} catch (Exception e) {
			logger.error(CommonUtils.EXCEPTION, e);
		}
	}

}
