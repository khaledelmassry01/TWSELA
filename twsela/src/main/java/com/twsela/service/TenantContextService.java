package com.twsela.service;

import com.twsela.domain.Tenant;
import com.twsela.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * خدمة سياق المستأجر - تحديد المستأجر من الطلب.
 * يستخدم ThreadLocal لتخزين المستأجر الحالي.
 */
@Service
@Transactional(readOnly = true)
public class TenantContextService {

    private static final Logger log = LoggerFactory.getLogger(TenantContextService.class);

    private static final ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();

    private final TenantRepository tenantRepository;

    public TenantContextService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * تحديد المستأجر من النطاق الفرعي.
     */
    public Optional<Tenant> resolveFromSubdomain(String host) {
        if (host == null || host.isEmpty()) {
            return Optional.empty();
        }
        // Extract subdomain: e.g., "tenant1.twsela.com" -> "tenant1"
        String subdomain = host.split("\\.")[0];
        if ("www".equals(subdomain) || "api".equals(subdomain)) {
            return Optional.empty();
        }
        return tenantRepository.findBySlug(subdomain);
    }

    /**
     * تحديد المستأجر من النطاق المخصص.
     */
    public Optional<Tenant> resolveFromDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return Optional.empty();
        }
        return tenantRepository.findByDomain(domain);
    }

    /**
     * تحديد المستأجر من الهيدر X-Tenant-ID.
     */
    public Optional<Tenant> resolveFromHeader(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return Optional.empty();
        }
        return tenantRepository.findByTenantId(tenantId);
    }

    /**
     * تحديد المستأجر من الـ slug.
     */
    public Optional<Tenant> resolveFromSlug(String slug) {
        if (slug == null || slug.isEmpty()) {
            return Optional.empty();
        }
        return tenantRepository.findBySlug(slug);
    }

    /**
     * تعيين المستأجر الحالي في ThreadLocal.
     */
    public void setCurrentTenant(Tenant tenant) {
        currentTenant.set(tenant);
        if (tenant != null) {
            log.debug("Tenant context set: {} ({})", tenant.getName(), tenant.getTenantId());
        }
    }

    /**
     * جلب المستأجر الحالي.
     */
    public Tenant getCurrentTenant() {
        return currentTenant.get();
    }

    /**
     * جلب معرف المستأجر الحالي.
     */
    public Long getCurrentTenantId() {
        Tenant tenant = currentTenant.get();
        return tenant != null ? tenant.getId() : null;
    }

    /**
     * مسح سياق المستأجر.
     */
    public void clear() {
        currentTenant.remove();
    }

    /**
     * التحقق من وجود سياق مستأجر.
     */
    public boolean hasTenantContext() {
        return currentTenant.get() != null;
    }
}
