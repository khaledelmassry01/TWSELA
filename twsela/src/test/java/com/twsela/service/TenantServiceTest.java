package com.twsela.service;

import com.twsela.domain.Tenant;
import com.twsela.domain.TenantBranding;
import com.twsela.repository.TenantBrandingRepository;
import com.twsela.repository.TenantRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private TenantBrandingRepository brandingRepository;
    @InjectMocks private TenantService tenantService;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("شركة التوصيل السريع");
        tenant.setSlug("fast-delivery");
        tenant.setContactName("أحمد محمد");
        tenant.setContactPhone("01000000001");
        tenant.setPlan(Tenant.TenantPlan.BASIC);
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
    }

    @Test
    @DisplayName("إنشاء مستأجر جديد بنجاح")
    void createTenant_success() {
        when(tenantRepository.existsBySlug("fast-delivery")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> {
            Tenant t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(brandingRepository.save(any(TenantBranding.class))).thenAnswer(inv -> inv.getArgument(0));

        Tenant result = tenantService.createTenant("شركة التوصيل السريع", "fast-delivery", "أحمد", "01000000001", Tenant.TenantPlan.BASIC);

        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("fast-delivery");
        assertThat(result.getPlan()).isEqualTo(Tenant.TenantPlan.BASIC);
        assertThat(result.getStatus()).isEqualTo(Tenant.TenantStatus.TRIAL);
        verify(brandingRepository).save(any(TenantBranding.class));
    }

    @Test
    @DisplayName("رفض إنشاء مستأجر بـ slug مكرر")
    void createTenant_duplicateSlug_throwsException() {
        when(tenantRepository.existsBySlug("fast-delivery")).thenReturn(true);

        assertThatThrownBy(() ->
                tenantService.createTenant("شركة", "fast-delivery", "أحمد", "01000000001", Tenant.TenantPlan.FREE))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("تفعيل مستأجر")
    void activateTenant_success() {
        tenant.setStatus(Tenant.TenantStatus.SUSPENDED);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        Tenant result = tenantService.activateTenant(1L);

        assertThat(result.getStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
    }

    @Test
    @DisplayName("تعليق مستأجر")
    void suspendTenant_success() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        Tenant result = tenantService.suspendTenant(1L);

        assertThat(result.getStatus()).isEqualTo(Tenant.TenantStatus.SUSPENDED);
    }

    @Test
    @DisplayName("تحديث خطة المستأجر")
    void updatePlan_success() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        Tenant result = tenantService.updatePlan(1L, Tenant.TenantPlan.ENTERPRISE);

        assertThat(result.getPlan()).isEqualTo(Tenant.TenantPlan.ENTERPRISE);
    }

    @Test
    @DisplayName("تحديث بيانات المستأجر")
    void updateTenant_success() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        Tenant result = tenantService.updateTenant(1L, "اسم جديد", "محمد", "01111111111", "test@test.com", "custom.com");

        assertThat(result.getName()).isEqualTo("اسم جديد");
        assertThat(result.getContactName()).isEqualTo("محمد");
    }

    @Test
    @DisplayName("البحث بالـ slug")
    void findBySlug_success() {
        when(tenantRepository.findBySlug("fast-delivery")).thenReturn(Optional.of(tenant));

        Tenant result = tenantService.findBySlug("fast-delivery");

        assertThat(result.getName()).isEqualTo("شركة التوصيل السريع");
    }

    @Test
    @DisplayName("جلب جميع المستأجرين")
    void findAll_success() {
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));

        List<Tenant> result = tenantService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("خطأ عند البحث عن مستأجر غير موجود")
    void findById_notFound() {
        when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
