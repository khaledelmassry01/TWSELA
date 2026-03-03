package com.twsela.service;

import com.twsela.domain.WorkflowDefinition;
import com.twsela.domain.WorkflowStep;
import com.twsela.repository.WorkflowStepRepository;
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
class WorkflowStepServiceTest {

    @Mock
    private WorkflowStepRepository repository;

    @InjectMocks
    private WorkflowStepService service;

    private WorkflowStep step;
    private WorkflowDefinition definition;

    @BeforeEach
    void setUp() {
        definition = new WorkflowDefinition();
        definition.setId(1L);

        step = new WorkflowStep();
        step.setId(1L);
        step.setWorkflowDefinition(definition);
        step.setStepOrder(1);
        step.setStepType(WorkflowStep.StepType.ACTION);
        step.setConfiguration("{\"action\":\"send_sms\"}");
    }

    @Test
    @DisplayName("إنشاء خطوة بنجاح")
    void create() {
        when(repository.save(any())).thenReturn(step);
        WorkflowStep result = service.create(step);
        assertThat(result.getStepType()).isEqualTo(WorkflowStep.StepType.ACTION);
    }

    @Test
    @DisplayName("جلب خطوات التعريف مرتبة")
    void findByDefinitionId() {
        when(repository.findByWorkflowDefinitionIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step));
        List<WorkflowStep> results = service.findByDefinitionId(1L);
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("البحث بمعرّف غير موجود")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("تحديث خطوة")
    void update() {
        WorkflowStep updated = new WorkflowStep();
        updated.setStepOrder(2);
        updated.setStepType(WorkflowStep.StepType.CONDITION);
        updated.setConfiguration("{\"field\":\"status\"}");

        when(repository.findById(1L)).thenReturn(Optional.of(step));
        when(repository.save(any())).thenReturn(step);

        service.update(1L, updated);
        assertThat(step.getStepOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("حذف خطوات التعريف")
    void deleteByDefinitionId() {
        service.deleteByDefinitionId(1L);
        verify(repository).deleteByWorkflowDefinitionId(1L);
    }
}
