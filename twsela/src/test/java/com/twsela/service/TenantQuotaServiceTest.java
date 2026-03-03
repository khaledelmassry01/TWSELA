package com.twsela.service;

import com.twsela.domain.TenantQuota;
import com.twsela.repository.TenantQuotaRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantQuotaServiceTest {

    @Mock private TenantQuotaRepository quotaRepository;
    @InjectMocks private TenantQuotaService quotaService;

    private TenantQuota quota;

    @BeforeEach
    void setUp() {
        quota = new TenantQuota();
        quota.setId(1L);
        quota.setQuotaType(TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY);
        quota.setMaxValue(1000);
        quota.setCurrentValue(500);
        quota.setResetPeriod(TenantQuota.ResetPeriod.MONTHLY);
    }

    @Test
    @DisplayName("التحقق من الحصة - غير متجاوزة")
    void checkQuota_notExceeded() {
        when(quotaRepository.findByTenantIdAndQuotaType(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY))
                .thenReturn(Optional.of(quota));

        boolean result = quotaService.checkQuota(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("التحقق من الحصة - متجاوزة")
    void checkQuota_exceeded() {
        quota.setCurrentValue(1001);
        when(quotaRepository.findByTenantIdAndQuotaType(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY))
                .thenReturn(Optional.of(quota));

        boolean result = quotaService.checkQuota(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("زيادة الاستهلاك")
    void incrementUsage_success() {
        when(quotaRepository.findByTenantIdAndQuotaType(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY))
                .thenReturn(Optional.of(quota));
        when(quotaRepository.save(any(TenantQuota.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantQuota result = quotaService.incrementUsage(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY);

        assertThat(result.getCurrentValue()).isEqualTo(501);
    }

    @Test
    @DisplayName("رفض الزيادة عند تجاوز الحصة")
    void incrementUsage_exceeded_throwsException() {
        quota.setCurrentValue(1001);
        when(quotaRepository.findByTenantIdAndQuotaType(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY))
                .thenReturn(Optional.of(quota));

        assertThatThrownBy(() ->
                quotaService.incrementUsage(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("تحديث الحد الأقصى")
    void updateMaxValue_success() {
        when(quotaRepository.findByTenantIdAndQuotaType(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY))
                .thenReturn(Optional.of(quota));
        when(quotaRepository.save(any(TenantQuota.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantQuota result = quotaService.updateMaxValue(1L, TenantQuota.QuotaType.MAX_SHIPMENTS_MONTHLY, 5000);

        assertThat(result.getMaxValue()).isEqualTo(5000);
    }

    @Test
    @DisplayName("إحصائيات الاستخدام")
    void getUsageStats_success() {
        when(quotaRepository.findByTenantId(1L)).thenReturn(List.of(quota));

        Map<String, Object> stats = quotaService.getUsageStats(1L);

        assertThat(stats).containsKey("totalQuotas");
        assertThat(stats.get("totalQuotas")).isEqualTo(1);
        assertThat(stats).containsKey("exceededQuotas");
    }
}
