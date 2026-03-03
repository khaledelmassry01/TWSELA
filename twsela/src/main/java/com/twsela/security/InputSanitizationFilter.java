package com.twsela.security;

import com.twsela.service.InputSanitizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

/**
 * فلتر تنظيف المدخلات من XSS وSQL Injection.
 */
@Component
public class InputSanitizationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InputSanitizationFilter.class);

    @Autowired(required = false)
    private InputSanitizationService sanitizationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {
        if (sanitizationService == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check query parameters
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] values = request.getParameterValues(paramName);
            if (values != null) {
                for (String value : values) {
                    if (!sanitizationService.isSafe(value)) {
                        log.warn("Potentially malicious input detected — param={}, ip={}", paramName, request.getRemoteAddr());
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"success\":false,\"message\":\"مدخلات غير آمنة تم اكتشافها\"}");
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
