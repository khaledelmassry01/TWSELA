package com.twsela.config;

import com.twsela.domain.ApiKey;
import com.twsela.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter that intercepts /api/v2/** requests and authenticates via API Key + Secret headers.
 * Skips if JWT authentication is already present in the SecurityContext.
 * ApiKeyService is optional — when absent (e.g. in @WebMvcTest), this filter is a no-op.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_SECRET_HEADER = "X-API-Secret";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthFilter(@Autowired(required = false) ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip entirely when ApiKeyService is not available (e.g. in test slices)
        if (apiKeyService == null) return true;
        // Only apply to /api/v2/** paths
        return !request.getRequestURI().startsWith("/api/v2/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        // Skip if already authenticated (e.g., JWT)
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKeyValue = request.getHeader(API_KEY_HEADER);
        String apiSecret = request.getHeader(API_SECRET_HEADER);

        if (apiKeyValue == null || apiSecret == null) {
            filterChain.doFilter(request, response);
            return;
        }

        ApiKey apiKey = apiKeyService.validateKey(apiKeyValue, apiSecret);
        if (apiKey == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid API key or secret\"}");
            return;
        }

        // Check rate limit
        if (!apiKeyService.enforceRateLimit(apiKey.getId())) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Rate limit exceeded\"}");
            return;
        }

        // Set authentication with MERCHANT role
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                apiKey.getMerchant().getPhone(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Log usage asynchronously would be better but for simplicity, log inline
        try {
            apiKeyService.recordUsage(apiKey.getId(), request.getRequestURI(),
                    request.getMethod(), 200, request.getRemoteAddr(),
                    request.getHeader("User-Agent"));
        } catch (Exception e) {
            log.warn("Failed to record API key usage: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
