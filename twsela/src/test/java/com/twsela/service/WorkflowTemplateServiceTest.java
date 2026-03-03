package com.twsela.service;

import com.twsela.domain.WorkflowTemplate;
import com.twsela.repository.WorkflowTemplateRepository;
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
class WorkflowTemplateServiceTest {

    @Mock
    private WorkflowTemplateRepository repository;

    @InjectMocks
    private WorkflowTemplateService service;

    private WorkflowTemplate template;

    @BeforeEach
    void setUp() {
        template = new WorkflowTemplate();
        template.setId(1L);
        template.setName("قالب شحنة جديدة");
        template.setNameAr("قالب شحنة جديدة");
        template.setCategory(WorkflowTemplate.TemplateCategory.SHIPMENT);
        template.setTemplateDefinition("{\"steps\":[]}");
    }

    @Test
    @DisplayName("إنشاء قالب بنجاح")
    void create_success() {
        when(repository.existsByName("قالب شحنة جديدة")).thenReturn(false);
        when(repository.save(any())).thenReturn(template);
        WorkflowTemplate result = service.create(template);
        assertThat(result.getName()).isEqualTo("قالب شحنة جديدة");
    }

    @Test
    @DisplayName("رفض إنشاء قالب مكرر")
    void create_duplicate_throws() {
        when(repository.existsByName("قالب شحنة جديدة")).thenReturn(true);
        assertThatThrownBy(() -> service.create(template))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("جلب قوالب النظام")
    void findSystemTemplates() {
        template.setSystem(true);
        when(repository.findByIsSystemTrue()).thenReturn(List.of(template));
        assertThat(service.findSystemTemplates()).hasSize(1);
    }

    @Test
    @DisplayName("جلب بالتصنيف")
    void findByCategory() {
        when(repository.findByCategory(WorkflowTemplate.TemplateCategory.SHIPMENT))
                .thenReturn(List.of(template));
        assertThat(service.findByCategory(WorkflowTemplate.TemplateCategory.SHIPMENT)).hasSize(1);
    }

    @Test
    @DisplayName("رفض تعديل قالب نظام")
    void update_systemTemplate_throws() {
        template.setSystem(true);
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        assertThatThrownBy(() -> service.update(1L, new WorkflowTemplate()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("رفض حذف قالب نظام")
    void delete_systemTemplate_throws() {
        template.setSystem(true);
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("زيادة عدّاد الاستخدام")
    void incrementUsage() {
        template.setUsageCount(3);
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        when(repository.save(any())).thenReturn(template);
        service.incrementUsage(1L);
        assertThat(template.getUsageCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("بحث بمعرّف غير موجود")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
