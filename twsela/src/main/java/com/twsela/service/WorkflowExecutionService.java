package com.twsela.service;

import com.twsela.domain.WorkflowExecution;
import com.twsela.repository.WorkflowExecutionRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class WorkflowExecutionService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutionService.class);

    private final WorkflowExecutionRepository executionRepository;

    public WorkflowExecutionService(WorkflowExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    public WorkflowExecution start(WorkflowExecution execution) {
        execution.setStatus(WorkflowExecution.ExecutionStatus.RUNNING);
        execution.setStartedAt(Instant.now());
        log.info("بدء تنفيذ سلسلة عمل — تعريف: {}", execution.getWorkflowDefinition().getId());
        return executionRepository.save(execution);
    }

    @Transactional(readOnly = true)
    public WorkflowExecution findById(Long id) {
        return executionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowExecution", "id", id));
    }

    @Transactional(readOnly = true)
    public List<WorkflowExecution> findByDefinitionId(Long definitionId) {
        return executionRepository.findByWorkflowDefinitionId(definitionId);
    }

    @Transactional(readOnly = true)
    public List<WorkflowExecution> findByStatus(WorkflowExecution.ExecutionStatus status) {
        return executionRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<WorkflowExecution> findByEntity(String entityType, Long entityId) {
        return executionRepository.findByTriggerEntityTypeAndTriggerEntityId(entityType, entityId);
    }

    public WorkflowExecution complete(Long id) {
        WorkflowExecution execution = findById(id);
        if (execution.getStatus() != WorkflowExecution.ExecutionStatus.RUNNING &&
            execution.getStatus() != WorkflowExecution.ExecutionStatus.PAUSED) {
            throw new BusinessRuleException("لا يمكن إكمال تنفيذ ليس في حالة تشغيل أو إيقاف مؤقت");
        }
        execution.setStatus(WorkflowExecution.ExecutionStatus.COMPLETED);
        execution.setCompletedAt(Instant.now());
        log.info("اكتمال تنفيذ سلسلة عمل: {}", id);
        return executionRepository.save(execution);
    }

    public WorkflowExecution fail(Long id, String errorMessage) {
        WorkflowExecution execution = findById(id);
        execution.setStatus(WorkflowExecution.ExecutionStatus.FAILED);
        execution.setErrorMessage(errorMessage);
        execution.setCompletedAt(Instant.now());
        log.error("فشل تنفيذ سلسلة عمل: {} — {}", id, errorMessage);
        return executionRepository.save(execution);
    }

    public WorkflowExecution cancel(Long id) {
        WorkflowExecution execution = findById(id);
        if (execution.getStatus() == WorkflowExecution.ExecutionStatus.COMPLETED) {
            throw new BusinessRuleException("لا يمكن إلغاء تنفيذ مكتمل");
        }
        execution.setStatus(WorkflowExecution.ExecutionStatus.CANCELLED);
        execution.setCompletedAt(Instant.now());
        log.info("إلغاء تنفيذ سلسلة عمل: {}", id);
        return executionRepository.save(execution);
    }

    public WorkflowExecution pause(Long id) {
        WorkflowExecution execution = findById(id);
        if (execution.getStatus() != WorkflowExecution.ExecutionStatus.RUNNING) {
            throw new BusinessRuleException("لا يمكن إيقاف تنفيذ ليس في حالة تشغيل");
        }
        execution.setStatus(WorkflowExecution.ExecutionStatus.PAUSED);
        log.info("إيقاف مؤقت لتنفيذ سلسلة عمل: {}", id);
        return executionRepository.save(execution);
    }
}
