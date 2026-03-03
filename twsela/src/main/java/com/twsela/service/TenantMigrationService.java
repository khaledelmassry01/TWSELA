package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * خدمة ترحيل البيانات من نظام أحادي المستأجر إلى متعدد المستأجرين.
 */
@Service
@Transactional
public class TenantMigrationService {

    private static final Logger log = LoggerFactory.getLogger(TenantMigrationService.class);

    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final TenantQuotaRepository quotaRepository;
    private final TenantBrandingRepository brandingRepository;
    private final UserRepository userRepository;

    public TenantMigrationService(TenantRepository tenantRepository,
                                   TenantUserRepository tenantUserRepository,
                                   TenantQuotaRepository quotaRepository,
                                   TenantBrandingRepository brandingRepository,
                                   UserRepository userRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.quotaRepository = quotaRepository;
        this.brandingRepository = brandingRepository;
        this.userRepository = userRepository;
    }

    /**
     * إنشاء مستأجر من بيانات موجودة.
     */
    public Tenant createTenantFromExistingData(String name, String slug, String contactName,
                                                String contactPhone, Tenant.TenantPlan plan) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("الرابط المختصر مستخدم بالفعل: " + slug);
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setSlug(slug);
        tenant.setContactName(contactName);
        tenant.setContactPhone(contactPhone);
        tenant.setPlan(plan);
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);

        tenant = tenantRepository.save(tenant);

        // Create default branding
        TenantBranding branding = new TenantBranding();
        branding.setTenant(tenant);
        branding.setCompanyNameAr(name);
        branding.setCompanyNameEn(name);
        branding.setPrimaryColor("#1a73e8");
        branding.setSecondaryColor("#ffffff");
        brandingRepository.save(branding);

        // Create default quotas based on plan
        createDefaultQuotas(tenant);

        log.info("Tenant created from existing data: {} ({})", name, slug);
        return tenant;
    }

    /**
     * ربط مستخدم موجود بمستأجر.
     */
    public TenantUser assignUserToTenant(Long userId, Long tenantId, TenantUser.TenantRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        if (tenantUserRepository.existsByUserIdAndTenantId(userId, tenantId)) {
            throw new BusinessRuleException("المستخدم مرتبط بالفعل بهذا المستأجر");
        }

        TenantUser tenantUser = new TenantUser();
        tenantUser.setUser(user);
        tenantUser.setTenant(tenant);
        tenantUser.setRole(role);
        tenantUser.setActive(true);
        tenantUser.setJoinedAt(Instant.now());

        log.info("User {} assigned to tenant {} as {}", userId, tenantId, role);
        return tenantUserRepository.save(tenantUser);
    }

    /**
     * ترحيل مجموعة مستخدمين إلى مستأجر.
     */
    public int migrateUsersToTenant(List<Long> userIds, Long tenantId, TenantUser.TenantRole role) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        int migratedCount = 0;
        for (Long userId : userIds) {
            if (!tenantUserRepository.existsByUserIdAndTenantId(userId, tenantId)) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    TenantUser tenantUser = new TenantUser();
                    tenantUser.setUser(user);
                    tenantUser.setTenant(tenant);
                    tenantUser.setRole(role);
                    tenantUser.setActive(true);
                    tenantUser.setJoinedAt(Instant.now());
                    tenantUserRepository.save(tenantUser);
                    migratedCount++;
                }
            }
        }

        log.info("Migrated {} users to tenant {}", migratedCount, tenantId);
        return migratedCount;
    }

    /**
     * جلب إحصائيات الترحيل.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMigrationStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTenants", tenantRepository.count());
        stats.put("totalTenantUsers", tenantUserRepository.count());
        stats.put("tenantsWithUsers", tenantRepository.findAll().stream()
                .filter(t -> !tenantUserRepository.findByTenantId(t.getId()).isEmpty())
                .count());
        return stats;
    }

    /**
     * إنشاء حصص افتراضية للمستأجر.
     */
    private void createDefaultQuotas(Tenant tenant) {
        Map<TenantQuota.QuotaType, Long> defaults = getDefaultQuotasForPlan(tenant.getPlan());

        for (Map.Entry<TenantQuota.QuotaType, Long> entry : defaults.entrySet()) {
            TenantQuota quota = new TenantQuota();
            quota.setTenant(tenant);
            quota.setQuotaType(entry.getKey());
            quota.setMaxValue(entry.getValue());
            quota.setCurrentValue(0);
            quota.setResetPeriod(getResetPeriod(entry.getKey()));
            quotaRepository.save(quota);
        }
    }

    private Map<TenantQuota.QuotaType, Long> getDefaultQuotasForPlan(Tenant.TenantPlan plan) {
        Map<TenantQuota.QuotaType, Long> quotas = new LinkedHashMap<>();
        switch (plan) {
            case FREE:
                quotas.put(TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY, 100L);
                quotas.put(TenantQuota.QuotaType.MAX_USERS, 5L);
                quotas.put(TenantQuota.QuotaType.MAX_API_CALLS, 1000L);
                quotas.put(TenantQuota.QuotaType.MAX_STORAGE_MB, 100L);
                quotas.put(TenantQuota.QuotaType.MAX_WEBHOOKS, 2L);
                break;
            case BASIC:
                quotas.put(TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY, 1000L);
                quotas.put(TenantQuota.QuotaType.MAX_USERS, 20L);
                quotas.put(TenantQuota.QuotaType.MAX_API_CALLS, 10000L);
                quotas.put(TenantQuota.QuotaType.MAX_STORAGE_MB, 500L);
                quotas.put(TenantQuota.QuotaType.MAX_WEBHOOKS, 5L);
                break;
            case PRO:
                quotas.put(TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY, 10000L);
                quotas.put(TenantQuota.QuotaType.MAX_USERS, 100L);
                quotas.put(TenantQuota.QuotaType.MAX_API_CALLS, 100000L);
                quotas.put(TenantQuota.QuotaType.MAX_STORAGE_MB, 5000L);
                quotas.put(TenantQuota.QuotaType.MAX_WEBHOOKS, 20L);
                break;
            case ENTERPRISE:
                quotas.put(TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY, 999999L);
                quotas.put(TenantQuota.QuotaType.MAX_USERS, 9999L);
                quotas.put(TenantQuota.QuotaType.MAX_API_CALLS, 999999L);
                quotas.put(TenantQuota.QuotaType.MAX_STORAGE_MB, 50000L);
                quotas.put(TenantQuota.QuotaType.MAX_WEBHOOKS, 100L);
                break;
        }
        return quotas;
    }

    private TenantQuota.ResetPeriod getResetPeriod(TenantQuota.QuotaType type) {
        switch (type) {
            case MAX_SHIPMENTS_MONTHLY:
            case MAX_API_CALLS:
                return TenantQuota.ResetPeriod.MONTHLY;
            default:
                return TenantQuota.ResetPeriod.NEVER;
        }
    }
}
