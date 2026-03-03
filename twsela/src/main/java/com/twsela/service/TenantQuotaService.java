package com.twsela.service;

import com.twsela.domain.TenantQuota;
import com.twsela.repository.TenantQuotaRepository;
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
 * خدمة إدارة حصص المستأجر.
 */
@Service
@Transactional
public class TenantQuotaService {

    private static final Logger log = LoggerFactory.getLogger(TenantQuotaService.class);

    private final TenantQuotaRepository quotaRepository;

    public TenantQuotaService(TenantQuotaRepository quotaRepository) {
        this.quotaRepository = quotaRepository;
    }

    /**
     * التحقق من الحصة قبل العملية.
     */
    @Transactional(readOnly = true)
    public boolean checkQuota(Long tenantId, TenantQuota.QuotaType quotaType) {
        return quotaRepository.findByTenantIdAndQuotaType(tenantId, quotaType)
                .map(quota -> !quota.isExceeded())
                .orElse(true); // No quota = unlimited
    }

    /**
     * التحقق والرفض إذا تجاوز الحصة.
     */
    @Transactional(readOnly = true)
    public void enforceQuota(Long tenantId, TenantQuota.QuotaType quotaType) {
        if (!checkQuota(tenantId, quotaType)) {
            throw new BusinessRuleException("تم تجاوز الحد الأقصى للحصة: " + quotaType.name());
        }
    }

    /**
     * زيادة الاستهلاك.
     */
    public TenantQuota incrementUsage(Long tenantId, TenantQuota.QuotaType quotaType) {
        TenantQuota quota = quotaRepository.findByTenantIdAndQuotaType(tenantId, quotaType)
                .orElseThrow(() -> new ResourceNotFoundException("TenantQuota", "quotaType", quotaType));

        if (quota.isExceeded()) {
            throw new BusinessRuleException("تم تجاوز الحد الأقصى للحصة: " + quotaType.name());
        }

        quota.setCurrentValue(quota.getCurrentValue() + 1);
        log.info("Quota incremented for tenant {}: {} = {}/{}", tenantId, quotaType,
                quota.getCurrentValue(), quota.getMaxValue());
        return quotaRepository.save(quota);
    }

    /**
     * تقليل الاستهلاك.
     */
    public TenantQuota decrementUsage(Long tenantId, TenantQuota.QuotaType quotaType) {
        TenantQuota quota = quotaRepository.findByTenantIdAndQuotaType(tenantId, quotaType)
                .orElseThrow(() -> new ResourceNotFoundException("TenantQuota", "quotaType", quotaType));

        if (quota.getCurrentValue() > 0) {
            quota.setCurrentValue(quota.getCurrentValue() - 1);
        }
        return quotaRepository.save(quota);
    }

    /**
     * تحديث الحد الأقصى لحصة.
     */
    public TenantQuota updateMaxValue(Long tenantId, TenantQuota.QuotaType quotaType, long maxValue) {
        TenantQuota quota = quotaRepository.findByTenantIdAndQuotaType(tenantId, quotaType)
                .orElseThrow(() -> new ResourceNotFoundException("TenantQuota", "quotaType", quotaType));

        quota.setMaxValue(maxValue);
        log.info("Quota max updated for tenant {}: {} = {}", tenantId, quotaType, maxValue);
        return quotaRepository.save(quota);
    }

    /**
     * إعادة تعيين الحصص الدورية.
     */
    public int resetPeriodicQuotas(TenantQuota.ResetPeriod period) {
        // This would typically be called by a scheduled job
        List<TenantQuota> quotas = quotaRepository.findAll();
        int resetCount = 0;
        for (TenantQuota quota : quotas) {
            if (quota.getResetPeriod() == period) {
                quota.setCurrentValue(0);
                quota.setLastResetAt(Instant.now());
                quotaRepository.save(quota);
                resetCount++;
            }
        }
        log.info("Reset {} quotas with period {}", resetCount, period);
        return resetCount;
    }

    /**
     * جلب حصص المستأجر.
     */
    @Transactional(readOnly = true)
    public List<TenantQuota> getQuotas(Long tenantId) {
        return quotaRepository.findByTenantId(tenantId);
    }

    /**
     * جلب الحصص المتجاوزة.
     */
    @Transactional(readOnly = true)
    public List<TenantQuota> getExceededQuotas(Long tenantId) {
        return quotaRepository.findExceeded(tenantId);
    }

    /**
     * إحصائيات استخدام الحصص.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUsageStats(Long tenantId) {
        List<TenantQuota> quotas = quotaRepository.findByTenantId(tenantId);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalQuotas", quotas.size());
        stats.put("exceededQuotas", quotas.stream().filter(TenantQuota::isExceeded).count());

        Map<String, Object> usage = new LinkedHashMap<>();
        for (TenantQuota quota : quotas) {
            Map<String, Object> quotaInfo = new LinkedHashMap<>();
            quotaInfo.put("current", quota.getCurrentValue());
            quotaInfo.put("max", quota.getMaxValue());
            quotaInfo.put("exceeded", quota.isExceeded());
            usage.put(quota.getQuotaType().name(), quotaInfo);
        }
        stats.put("quotas", usage);
        return stats;
    }
}
