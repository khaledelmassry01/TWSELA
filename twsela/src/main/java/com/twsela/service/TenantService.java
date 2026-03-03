package com.twsela.service;

import com.twsela.domain.Tenant;
import com.twsela.domain.TenantBranding;
import com.twsela.repository.TenantBrandingRepository;
import com.twsela.repository.TenantRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * خدمة إدارة المستأجرين — CRUD + تفعيل/تعليق + ترقية/تخفيض.
 */
@Service
@Transactional
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final TenantBrandingRepository tenantBrandingRepository;

    public TenantService(TenantRepository tenantRepository,
                          TenantBrandingRepository tenantBrandingRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantBrandingRepository = tenantBrandingRepository;
    }

    /**
     * إنشاء مستأجر جديد مع علامة تجارية افتراضية.
     */
    public Tenant createTenant(String name, String slug, String contactName,
                                String contactPhone, Tenant.TenantPlan plan) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("الاسم المختصر '" + slug + "' مستخدم بالفعل");
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setSlug(slug);
        tenant.setContactName(contactName);
        tenant.setContactPhone(contactPhone);
        tenant.setPlan(plan);
        tenant.setStatus(Tenant.TenantStatus.TRIAL);

        Tenant saved = tenantRepository.save(tenant);

        // Create default branding
        TenantBranding branding = new TenantBranding();
        branding.setTenant(saved);
        branding.setCompanyNameAr(name);
        branding.setCompanyNameEn(name);
        branding.setPrimaryColor("#1E40AF");
        branding.setSecondaryColor("#3B82F6");
        branding.setAccentColor("#F59E0B");
        tenantBrandingRepository.save(branding);

        log.info("Tenant created: name={}, slug={}, plan={}", name, slug, plan);
        return saved;
    }

    /**
     * تفعيل مستأجر.
     */
    public Tenant activateTenant(Long tenantId) {
        Tenant tenant = findById(tenantId);
        if (tenant.getStatus() == Tenant.TenantStatus.ACTIVE) {
            throw new BusinessRuleException("المستأجر مفعل بالفعل");
        }
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        log.info("Tenant {} activated", tenantId);
        return tenantRepository.save(tenant);
    }

    /**
     * تعليق مستأجر.
     */
    public Tenant suspendTenant(Long tenantId) {
        Tenant tenant = findById(tenantId);
        if (tenant.getStatus() == Tenant.TenantStatus.SUSPENDED) {
            throw new BusinessRuleException("المستأجر معلق بالفعل");
        }
        tenant.setStatus(Tenant.TenantStatus.SUSPENDED);
        log.warn("Tenant {} suspended", tenantId);
        return tenantRepository.save(tenant);
    }

    /**
     * ترقية / تخفيض خطة المستأجر.
     */
    public Tenant updatePlan(Long tenantId, Tenant.TenantPlan plan) {
        Tenant tenant = findById(tenantId);
        Tenant.TenantPlan oldPlan = tenant.getPlan();
        tenant.setPlan(plan);
        log.info("Tenant {} plan changed: {} -> {}", tenantId, oldPlan, plan);
        return tenantRepository.save(tenant);
    }

    /**
     * تعديل بيانات المستأجر.
     */
    public Tenant updateTenant(Long tenantId, String name, String contactName,
                                String contactPhone, String contactEmail, String domain) {
        Tenant tenant = findById(tenantId);
        if (name != null) tenant.setName(name);
        if (contactName != null) tenant.setContactName(contactName);
        if (contactPhone != null) tenant.setContactPhone(contactPhone);
        if (contactEmail != null) tenant.setContactEmail(contactEmail);
        if (domain != null) tenant.setDomain(domain);
        return tenantRepository.save(tenant);
    }

    @Transactional(readOnly = true)
    public Tenant findById(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
    }

    @Transactional(readOnly = true)
    public Tenant findBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "slug", slug));
    }

    @Transactional(readOnly = true)
    public Tenant findByDomain(String domain) {
        return tenantRepository.findByDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "domain", domain));
    }

    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tenant> findByStatus(Tenant.TenantStatus status) {
        return tenantRepository.findByStatus(status);
    }
}
