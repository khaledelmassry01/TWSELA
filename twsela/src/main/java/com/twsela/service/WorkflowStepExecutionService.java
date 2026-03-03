package com.twsela.service;

import com.twsela.domain.WorkflowStepExecution;
import com.twsela.repository.WorkflowStepExecutionRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class WorkflowStepExecutionService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStepExecutionService.class);

    private final WorkflowStepExecutionRepository stepExecutionRepository;

    public WorkflowStepExecutionService(WorkflowStepExecutionRepository stepExecutionRepository) {
        this.stepExecutionRepository = stepExecutionRepository;
    }

    public WorkflowStepExecution create(WorkflowStepExecution stepExecution) {
        log.info("إنشاء تنفيذ خطوة — تنفيذ: {} خطوة: {}",
                stepExecution.getWorkflowExecution().getId(),
                stepExecution.getWorkflowStep().getId());
        return stepExecutionRepository.save(stepExecution);
    }

    @Transactional(readOnly = true)
    public WorkflowStepExecution findById(Long id) {
        return stepExecutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStepExecution", "id", id));
    }

    @Transactional(readOnly = true)
    public List<WorkflowStepExecution> findByExecutionId(Long executionId) {
        return stepExecutionRepository.findByWorkflowExecutionIdOrderByIdAsc(executionId);
    }

    public WorkflowStepExecution markRunning(Long id) {
        WorkflowStepExecution se = findById(id);
        se.setStatus(WorkflowStepExecution.StepExecutionStatus.RUNNING);
        se.setStartedAt(Instant.now());
        return stepExecutionRepository.save(se);
    }

    public WorkflowStepExecution markCompleted(Long id, String output) {
        WorkflowStepExecution se = findById(id);
        se.setStatus(WorkflowStepExecution.StepExecutionStatus.COMPLETED);
        se.setOutput(output);
        se.setCompletedAt(Instant.now());
        log.info("اكتمال تنفيذ خطوة: {}", id);
        return stepExecutionRepository.save(se);
    }

    public WorkflowStepExecution markFailed(Long id, String errorMessage) {
        WorkflowStepExecution se = findById(id);
        se.setStatus(WorkflowStepExecution.StepExecutionStatus.FAILED);
        se.setErrorMessage(errorMessage);
        se.setCompletedAt(Instant.now());
        se.setRetryCount(se.getRetryCount() + 1);
        log.error("فشل تنفيذ خطوة: {} — {}", id, errorMessage);
        return stepExecutionRepository.save(se);
    }

    public WorkflowStepExecution markSkipped(Long id) {
        WorkflowStepExecution se = findById(id);
        se.setStatus(WorkflowStepExecution.StepExecutionStatus.SKIPPED);
        se.setCompletedAt(Instant.now());
        return stepExecutionRepository.save(se);
    }
}
