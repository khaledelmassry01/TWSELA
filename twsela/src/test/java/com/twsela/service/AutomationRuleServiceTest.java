package com.twsela.service;

import com.twsela.domain.AutomationRule;
import com.twsela.domain.Tenant;
import com.twsela.domain.WorkflowDefinition;
import com.twsela.repository.AutomationRuleRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomationRuleServiceTest {

    @Mock
    private AutomationRuleRepository repository;

    @InjectMocks
    private AutomationRuleService service;

    private AutomationRule rule;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        rule = new AutomationRule();
        rule.setId(1L);
        rule.setName("إرسال إشعار عند الإنشاء");
        rule.setTriggerEvent(WorkflowDefinition.TriggerEvent.SHIPMENT_CREATED);
        rule.setActionType(AutomationRule.ActionType.SEND_NOTIFICATION);
        rule.setActionConfig("{\"template\":\"new_shipment\"}");
        rule.setTenant(tenant);
    }

    @Test
    @DisplayName("إنشاء قاعدة أتمتة بنجاح")
    void create_success() {
        when(repository.existsByNameAndTenantId("إرسال إشعار عند الإنشاء", 1L)).thenReturn(false);
        when(repository.save(any())).thenReturn(rule);
        AutomationRule result = service.create(rule);
        assertThat(result.getName()).isEqualTo("إرسال إشعار عند الإنشاء");
    }

    @Test
    @DisplayName("رفض إنشاء قاعدة مكررة")
    void create_duplicate_throws() {
        when(repository.existsByNameAndTenantId("إرسال إشعار عند الإنشاء", 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.create(rule))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("تفعيل قاعدة")
    void activate() {
        rule.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(rule));
        when(repository.save(any())).thenReturn(rule);
        service.activate(1L);
        assertThat(rule.isActive()).isTrue();
    }

    @Test
    @DisplayName("تعطيل قاعدة")
    void deactivate() {
        when(repository.findById(1L)).thenReturn(Optional.of(rule));
        when(repository.save(any())).thenReturn(rule);
        service.deactivate(1L);
        assertThat(rule.isActive()).isFalse();
    }

    @Test
    @DisplayName("تسجيل تنفيذ قاعدة")
    void recordExecution() {
        rule.setExecutionCount(5);
        when(repository.findById(1L)).thenReturn(Optional.of(rule));
        when(repository.save(any())).thenReturn(rule);
        service.recordExecution(1L);
        assertThat(rule.getExecutionCount()).isEqualTo(6);
        assertThat(rule.getLastTriggeredAt()).isNotNull();
    }

    @Test
    @DisplayName("بحث بمعرّف غير موجود")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("جلب قواعد المستأجر")
    void findByTenantId() {
        when(repository.findByTenantId(1L)).thenReturn(List.of(rule));
        assertThat(service.findByTenantId(1L)).hasSize(1);
    }
}
