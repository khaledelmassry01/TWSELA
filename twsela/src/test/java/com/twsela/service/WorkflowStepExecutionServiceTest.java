package com.twsela.service;

import com.twsela.domain.WorkflowExecution;
import com.twsela.domain.WorkflowStep;
import com.twsela.domain.WorkflowStepExecution;
import com.twsela.repository.WorkflowStepExecutionRepository;
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
class WorkflowStepExecutionServiceTest {

    @Mock
    private WorkflowStepExecutionRepository repository;

    @InjectMocks
    private WorkflowStepExecutionService service;

    private WorkflowStepExecution stepExecution;

    @BeforeEach
    void setUp() {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setId(1L);
        WorkflowStep step = new WorkflowStep();
        step.setId(1L);

        stepExecution = new WorkflowStepExecution();
        stepExecution.setId(1L);
        stepExecution.setWorkflowExecution(execution);
        stepExecution.setWorkflowStep(step);
        stepExecution.setStatus(WorkflowStepExecution.StepExecutionStatus.PENDING);
    }

    @Test
    @DisplayName("إنشاء تنفيذ خطوة")
    void create() {
        when(repository.save(any())).thenReturn(stepExecution);
        WorkflowStepExecution result = service.create(stepExecution);
        assertThat(result.getStatus()).isEqualTo(WorkflowStepExecution.StepExecutionStatus.PENDING);
    }

    @Test
    @DisplayName("تغيير حالة إلى تشغيل")
    void markRunning() {
        when(repository.findById(1L)).thenReturn(Optional.of(stepExecution));
        when(repository.save(any())).thenReturn(stepExecution);
        service.markRunning(1L);
        assertThat(stepExecution.getStatus()).isEqualTo(WorkflowStepExecution.StepExecutionStatus.RUNNING);
        assertThat(stepExecution.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("إكمال خطوة بنتيجة")
    void markCompleted() {
        when(repository.findById(1L)).thenReturn(Optional.of(stepExecution));
        when(repository.save(any())).thenReturn(stepExecution);
        service.markCompleted(1L, "{\"result\":\"ok\"}");
        assertThat(stepExecution.getStatus()).isEqualTo(WorkflowStepExecution.StepExecutionStatus.COMPLETED);
        assertThat(stepExecution.getOutput()).isEqualTo("{\"result\":\"ok\"}");
    }

    @Test
    @DisplayName("فشل خطوة")
    void markFailed() {
        when(repository.findById(1L)).thenReturn(Optional.of(stepExecution));
        when(repository.save(any())).thenReturn(stepExecution);
        service.markFailed(1L, "خطأ في التنفيذ");
        assertThat(stepExecution.getStatus()).isEqualTo(WorkflowStepExecution.StepExecutionStatus.FAILED);
        assertThat(stepExecution.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("تخطي خطوة")
    void markSkipped() {
        when(repository.findById(1L)).thenReturn(Optional.of(stepExecution));
        when(repository.save(any())).thenReturn(stepExecution);
        service.markSkipped(1L);
        assertThat(stepExecution.getStatus()).isEqualTo(WorkflowStepExecution.StepExecutionStatus.SKIPPED);
    }

    @Test
    @DisplayName("بحث بمعرّف غير موجود")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("جلب خطوات التنفيذ")
    void findByExecutionId() {
        when(repository.findByWorkflowExecutionIdOrderByIdAsc(1L)).thenReturn(List.of(stepExecution));
        List<WorkflowStepExecution> results = service.findByExecutionId(1L);
        assertThat(results).hasSize(1);
    }
}
