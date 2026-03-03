package com.twsela.security;

import com.twsela.domain.Tenant;
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
import java.util.Optional;

/**
 * فلتر لتحديد سياق المستأجر من كل طلب.
 * يحدد المستأجر من: هيدر X-Tenant-ID، النطاق الفرعي، أو النطاق المخصص.
 */
@Component
@Order(1)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Autowired(required = false)
    private TenantContextService tenantContextService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (tenantContextService == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Optional<Tenant> tenant = resolveTenant(request);
            tenant.ifPresent(t -> {
                if (t.getStatus() == Tenant.TenantStatus.ACTIVE || t.getStatus() == Tenant.TenantStatus.TRIAL) {
                    tenantContextService.setCurrentTenant(t);
                } else {
                    log.warn("Tenant {} is not active (status: {})", t.getSlug(), t.getStatus());
                }
            });

            filterChain.doFilter(request, response);
        } finally {
            if (tenantContextService != null) {
                tenantContextService.clear();
            }
        }
    }

    private Optional<Tenant> resolveTenant(HttpServletRequest request) {
        // Priority 1: X-Tenant-ID header
        String tenantHeader = request.getHeader(TENANT_HEADER);
        if (tenantHeader != null && !tenantHeader.isEmpty()) {
            Optional<Tenant> tenant = tenantContextService.resolveFromHeader(tenantHeader);
            if (tenant.isPresent()) {
                return tenant;
            }
        }

        // Priority 2: Subdomain
        String host = request.getServerName();
        if (host != null) {
            Optional<Tenant> tenant = tenantContextService.resolveFromSubdomain(host);
            if (tenant.isPresent()) {
                return tenant;
            }

            // Priority 3: Custom domain
            tenant = tenantContextService.resolveFromDomain(host);
            if (tenant.isPresent()) {
                return tenant;
            }
        }

        return Optional.empty();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip tenant resolution for public endpoints
        return path.startsWith("/actuator") || path.equals("/api/public/health");
    }
}
