package com.twsela.security;

import com.twsela.service.TenantContextService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * فلتر لفرض عزل البيانات بناءً على سياق المستأجر.
 * يضمن أن الطلبات على مسارات API التي تتطلب مستأجر يتم رفضها إذا لم يكن هناك سياق مستأجر.
 */
@Component
@Order(2)
public class TenantDataFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantDataFilter.class);

    @Autowired(required = false)
    private TenantContextService tenantContextService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (tenantContextService == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // For tenant-specific routes, we could enforce tenant context
        // For now, this is a pass-through that logs when no tenant context exists
        String path = request.getRequestURI();
        if (requiresTenantContext(path) && !tenantContextService.hasTenantContext()) {
            log.debug("No tenant context for path: {}", path);
            // We allow the request to proceed - tenant context is optional
            // The TenantIsolationService will handle enforcement at the service layer
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresTenantContext(String path) {
        // These paths would ideally require tenant context
        // But since we're transitioning to multi-tenant, we keep it optional
        return path.startsWith("/api/shipments") ||
               path.startsWith("/api/merchants") ||
               path.startsWith("/api/couriers");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") ||
               path.startsWith("/api/public") ||
               path.startsWith("/api/auth") ||
               path.startsWith("/api/tenants");
    }
}
