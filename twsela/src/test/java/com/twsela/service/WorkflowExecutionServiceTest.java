package com.twsela.service;

import com.twsela.domain.WorkflowDefinition;
import com.twsela.domain.WorkflowExecution;
import com.twsela.repository.WorkflowExecutionRepository;
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
class WorkflowExecutionServiceTest {

    @Mock
    private WorkflowExecutionRepository repository;

    @InjectMocks
    private WorkflowExecutionService service;

    private WorkflowExecution execution;
    private WorkflowDefinition definition;

    @BeforeEach
    void setUp() {
        definition = new WorkflowDefinition();
        definition.setId(1L);

        execution = new WorkflowExecution();
        execution.setId(1L);
        execution.setWorkflowDefinition(definition);
        execution.setTriggerEntityType("Shipment");
        execution.setTriggerEntityId(100L);
        execution.setStatus(WorkflowExecution.ExecutionStatus.RUNNING);
    }

    @Test
    @DisplayName("بدء تنفيذ سلسلة عمل")
    void start() {
        when(repository.save(any())).thenReturn(execution);
        WorkflowExecution result = service.start(execution);
        assertThat(result.getStatus()).isEqualTo(WorkflowExecution.ExecutionStatus.RUNNING);
    }

    @Test
    @DisplayName("إكمال تنفيذ")
    void complete() {
        when(repository.findById(1L)).thenReturn(Optional.of(execution));
        when(repository.save(any())).thenReturn(execution);
        service.complete(1L);
        assertThat(execution.getStatus()).isEqualTo(WorkflowExecution.ExecutionStatus.COMPLETED);
        assertThat(execution.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("رفض إكمال تنفيذ مكتمل")
    void complete_alreadyCompleted_throws() {
        execution.setStatus(WorkflowExecution.ExecutionStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> service.complete(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("فشل تنفيذ مع رسالة خطأ")
    void fail() {
        when(repository.findById(1L)).thenReturn(Optional.of(execution));
        when(repository.save(any())).thenReturn(execution);
        service.fail(1L, "خطأ في الخطوة الثالثة");
        assertThat(execution.getStatus()).isEqualTo(WorkflowExecution.ExecutionStatus.FAILED);
        assertThat(execution.getErrorMessage()).isEqualTo("خطأ في الخطوة الثالثة");
    }

    @Test
    @DisplayName("إلغاء تنفيذ")
    void cancel() {
        when(repository.findById(1L)).thenReturn(Optional.of(execution));
        when(repository.save(any())).thenReturn(execution);
        service.cancel(1L);
        assertThat(execution.getStatus()).isEqualTo(WorkflowExecution.ExecutionStatus.CANCELLED);
    }

    @Test
    @DisplayName("رفض إلغاء تنفيذ مكتمل")
    void cancel_completed_throws() {
        execution.setStatus(WorkflowExecution.ExecutionStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(execution));
        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("إيقاف مؤقت")
    void pause() {
        when(repository.findById(1L)).thenReturn(Optional.of(execution));
        when(repository.save(any())).thenReturn(execution);
        service.pause(1L);
        assertThat(execution.getStatus()).isEqualTo(WorkflowExecution.ExecutionStatus.PAUSED);
    }

    @Test
    @DisplayName("البحث بمعرّف غير موجود")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("جلب بالكيان")
    void findByEntity() {
        when(repository.findByTriggerEntityTypeAndTriggerEntityId("Shipment", 100L))
                .thenReturn(List.of(execution));
        List<WorkflowExecution> results = service.findByEntity("Shipment", 100L);
        assertThat(results).hasSize(1);
    }
}
