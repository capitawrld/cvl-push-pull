package com.opl.service.loans.controller;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Harshit & Jaimin 
 * For Test ALL Micro Service is working or not !!
 */
@RestController
public class PingController {

	private static final Logger logger = LoggerFactory.getLogger(PingController.class);

	@SuppressWarnings("unchecked")
	@GetMapping(value = "/ping")
	public JSONObject ping() {
		logger.info("CHECK SERVICE STATUS  =====================>");
		JSONObject obj = new JSONObject();
		obj.put("status", 200);
		obj.put("message", "Service is working fine!!");
		return obj;
	}
}
