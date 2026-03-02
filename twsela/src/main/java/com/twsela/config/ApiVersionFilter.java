package com.twsela.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Adds API version and security headers to all responses.
 * v1 is the current and only version; header allows clients to detect it.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiVersionFilter implements Filter {

    public static final String API_VERSION = "1.0";
    public static final String API_VERSION_HEADER = "X-API-Version";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // API version
        httpResponse.setHeader(API_VERSION_HEADER, API_VERSION);

        // Security headers (defense-in-depth, also set by nginx)
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Cache-Control", "no-store");
        httpResponse.setHeader("Pragma", "no-cache");

        chain.doFilter(request, response);
    }
}
