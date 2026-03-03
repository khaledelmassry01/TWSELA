package com.twsela.service;

import com.twsela.domain.Tenant;
import com.twsela.domain.WorkflowDefinition;
import com.twsela.repository.WorkflowDefinitionRepository;
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
class WorkflowDefinitionServiceTest {

    @Mock
    private WorkflowDefinitionRepository repository;

    @InjectMocks
    private WorkflowDefinitionService service;

    private WorkflowDefinition definition;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("شركة توصيلة");

        definition = new WorkflowDefinition();
        definition.setId(1L);
        definition.setName("سلسلة شحنات جديدة");
        definition.setDescription("تُنفَّذ عند إنشاء شحنة");
        definition.setTriggerEvent(WorkflowDefinition.TriggerEvent.SHIPMENT_CREATED);
        definition.setTenant(tenant);
        definition.setPriority(5);
    }

    @Test
    @DisplayName("إنشاء تعريف سلسلة عمل بنجاح")
    void create_success() {
        when(repository.existsByNameAndTenantId("سلسلة شحنات جديدة", 1L)).thenReturn(false);
        when(repository.save(any(WorkflowDefinition.class))).thenReturn(definition);

        WorkflowDefinition result = service.create(definition);

        assertThat(result.getName()).isEqualTo("سلسلة شحنات جديدة");
        verify(repository).save(definition);
    }

    @Test
    @DisplayName("رفض إنشاء تعريف بنفس الاسم")
    void create_duplicateName_throws() {
        when(repository.existsByNameAndTenantId("سلسلة شحنات جديدة", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(definition))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("البحث بالمعرّف بنجاح")
    void findById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(definition));

        WorkflowDefinition result = service.findById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("البحث بمعرّف غير موجود")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("جلب سلاسل عمل المستأجر")
    void findByTenantId() {
        when(repository.findByTenantId(1L)).thenReturn(List.of(definition));
        List<WorkflowDefinition> results = service.findByTenantId(1L);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("تفعيل سلسلة عمل")
    void activate() {
        when(repository.findById(1L)).thenReturn(Optional.of(definition));
        when(repository.save(any())).thenReturn(definition);
        service.activate(1L);
        assertThat(definition.isActive()).isTrue();
    }

    @Test
    @DisplayName("تعطيل سلسلة عمل")
    void deactivate() {
        definition.setActive(true);
        when(repository.findById(1L)).thenReturn(Optional.of(definition));
        when(repository.save(any())).thenReturn(definition);
        service.deactivate(1L);
        assertThat(definition.isActive()).isFalse();
    }

    @Test
    @DisplayName("تحديث تعريف سلسلة عمل")
    void update() {
        WorkflowDefinition updated = new WorkflowDefinition();
        updated.setName("اسم جديد");
        updated.setDescription("وصف جديد");
        updated.setTriggerEvent(WorkflowDefinition.TriggerEvent.STATUS_CHANGED);
        updated.setPriority(3);

        when(repository.findById(1L)).thenReturn(Optional.of(definition));
        when(repository.save(any())).thenReturn(definition);

        service.update(1L, updated);
        assertThat(definition.getName()).isEqualTo("اسم جديد");
        assertThat(definition.getVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("حذف تعريف سلسلة عمل")
    void delete_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(definition));
        service.delete(1L);
        verify(repository).delete(definition);
    }
}
