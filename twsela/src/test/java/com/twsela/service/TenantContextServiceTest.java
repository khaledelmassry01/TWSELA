package com.twsela.service;

import com.twsela.domain.Tenant;
import com.twsela.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantContextServiceTest {

    @Mock private TenantRepository tenantRepository;
    @InjectMocks private TenantContextService contextService;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("شركة التوصيل");
        tenant.setSlug("delivery-co");
        tenant.setTenantId("uuid-tenant-1");
        tenant.setDomain("delivery.example.com");
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        // Clear any residual thread-local
        contextService.clear();
    }

    @Test
    @DisplayName("تحديد المستأجر من النطاق الفرعي")
    void resolveFromSubdomain_success() {
        when(tenantRepository.findBySlug("delivery-co")).thenReturn(Optional.of(tenant));

        Optional<Tenant> result = contextService.resolveFromSubdomain("delivery-co.twsela.com");

        assertThat(result).isPresent();
        assertThat(result.get().getSlug()).isEqualTo("delivery-co");
    }

    @Test
    @DisplayName("تخطي www كنطاق فرعي")
    void resolveFromSubdomain_skipWww() {
        Optional<Tenant> result = contextService.resolveFromSubdomain("www.twsela.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("تحديد من نطاق مخصص")
    void resolveFromDomain_success() {
        when(tenantRepository.findByDomain("delivery.example.com")).thenReturn(Optional.of(tenant));

        Optional<Tenant> result = contextService.resolveFromDomain("delivery.example.com");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("تحديد من هيدر X-Tenant-ID")
    void resolveFromHeader_success() {
        when(tenantRepository.findByTenantId("uuid-tenant-1")).thenReturn(Optional.of(tenant));

        Optional<Tenant> result = contextService.resolveFromHeader("uuid-tenant-1");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("تعيين وجلب المستأجر الحالي عبر ThreadLocal")
    void setAndGetCurrentTenant() {
        contextService.setCurrentTenant(tenant);

        assertThat(contextService.getCurrentTenant()).isEqualTo(tenant);
        assertThat(contextService.getCurrentTenantId()).isEqualTo(1L);
        assertThat(contextService.hasTenantContext()).isTrue();

        contextService.clear();
        assertThat(contextService.hasTenantContext()).isFalse();
    }

    @Test
    @DisplayName("عدم وجود سياق مستأجر افتراضياً")
    void noTenantContext_byDefault() {
        assertThat(contextService.getCurrentTenant()).isNull();
        assertThat(contextService.getCurrentTenantId()).isNull();
        assertThat(contextService.hasTenantContext()).isFalse();
    }

    @Test
    @DisplayName("إرجاع فارغ عند النطاق الفارغ")
    void resolveFromSubdomain_emptyHost() {
        Optional<Tenant> result = contextService.resolveFromSubdomain("");

        assertThat(result).isEmpty();
    }
}
