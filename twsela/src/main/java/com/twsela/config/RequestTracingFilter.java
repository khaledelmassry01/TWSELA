package com.twsela.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Assigns a unique request ID to each HTTP request for traceability.
 * The ID is added to MDC for logging and returned as a response header.
 */
@Component
@Order(1)
public class RequestTracingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestTracingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Use incoming header if present, otherwise generate
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(MDC_KEY, requestId);
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (httpRequest.getRequestURI().startsWith("/api/")) {
                log.info("{} {} {} {}ms", httpRequest.getMethod(), httpRequest.getRequestURI(),
                        httpResponse.getStatus(), duration);
            }
            MDC.remove(MDC_KEY);
        }
    }
}
