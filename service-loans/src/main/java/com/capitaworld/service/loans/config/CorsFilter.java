package com.capitaworld.service.loans.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CorsFilter implements Filter {
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "*");
		response.setHeader("Access-Control-Max-Age", "-1");
		response.setHeader("Access-Control-Allow-Headers",
			    "Content-Type, Access-Control-Allow-Headers, X-Requested-With,tk_ac,ur_cu,tk_rc,tk_lg,req_auth,*");
		response.setHeader("X-Frame-Options","ALLOW-FROM *.capitaworld.com");
		HttpServletRequest request = ((HttpServletRequest) req);
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else {
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
