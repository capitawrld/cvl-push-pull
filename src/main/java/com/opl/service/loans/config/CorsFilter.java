package com.opl.service.loans.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opl.utils.utility.CorsFilterUtils;
import org.springframework.stereotype.Component;

@Component
public class CorsFilter implements Filter {
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		boolean result = CorsFilterUtils.setHeader(response, request);
		if (!result) {
			chain.doFilter(req, res);
		}
	}

	public void init(FilterConfig filterConfig) {
		// Do nothing because of X and Y.
	}

	public void destroy() {
		// Do nothing because of X and Y.
	}

}
