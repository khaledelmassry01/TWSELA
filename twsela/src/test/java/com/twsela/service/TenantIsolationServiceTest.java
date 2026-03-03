package com.twsela.service;

import com.twsela.domain.Tenant;
import com.twsela.domain.TenantAuditLog;
import com.twsela.domain.TenantUser;
import com.twsela.repository.TenantAuditLogRepository;
import com.twsela.repository.TenantUserRepository;
import com.twsela.web.exception.BusinessRuleException;
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
class TenantIsolationServiceTest {

    @Mock private TenantContextService tenantContextService;
    @Mock private TenantUserRepository tenantUserRepository;
    @Mock private TenantAuditLogRepository auditLogRepository;
    @InjectMocks private TenantIsolationService isolationService;

    private Tenant tenant;
    private TenantUser tenantUser;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("شركة التوصيل");

        tenantUser = new TenantUser();
        tenantUser.setId(1L);
        tenantUser.setRole(TenantUser.TenantRole.TENANT_USER);
        tenantUser.setActive(true);
    }

    @Test
    @DisplayName("المستخدم ينتمي للمستأجر الحالي")
    void isUserInCurrentTenant_true() {
        when(tenantContextService.getCurrentTenantId()).thenReturn(1L);
        when(tenantUserRepository.existsByUserIdAndTenantId(10L, 1L)).thenReturn(true);

        boolean result = isolationService.isUserInCurrentTenant(10L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("لا يوجد سياق مستأجر - مسموح")
    void isUserInCurrentTenant_noContext() {
        when(tenantContextService.getCurrentTenantId()).thenReturn(null);

        boolean result = isolationService.isUserInCurrentTenant(10L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("تغيير دور مستخدم بنجاح")
    void changeUserRole_success() {
        when(tenantUserRepository.findByUserIdAndTenantId(10L, 1L)).thenReturn(Optional.of(tenantUser));
        when(tenantUserRepository.save(any(TenantUser.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantUser result = isolationService.changeUserRole(1L, 10L, TenantUser.TenantRole.TENANT_ADMIN);

        assertThat(result.getRole()).isEqualTo(TenantUser.TenantRole.TENANT_ADMIN);
    }

    @Test
    @DisplayName("رفض إزالة مالك المستأجر")
    void removeUser_owner_throwsException() {
        tenantUser.setRole(TenantUser.TenantRole.TENANT_OWNER);
        when(tenantUserRepository.findByUserIdAndTenantId(10L, 1L)).thenReturn(Optional.of(tenantUser));

        assertThatThrownBy(() -> isolationService.removeUserFromTenant(1L, 10L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("تسجيل عملية في سجل التدقيق")
    void logAuditEvent_success() {
        when(auditLogRepository.save(any(TenantAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        isolationService.logAuditEvent(1L, 10L, "CREATE", "Shipment", 123L,
                null, "{\"status\":\"CREATED\"}", "192.168.1.1");

        verify(auditLogRepository).save(any(TenantAuditLog.class));
    }
}
