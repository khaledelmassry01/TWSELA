package com.twsela.service;

import com.twsela.domain.WorkflowStep;
import com.twsela.repository.WorkflowStepRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WorkflowStepService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStepService.class);

    private final WorkflowStepRepository workflowStepRepository;

    public WorkflowStepService(WorkflowStepRepository workflowStepRepository) {
        this.workflowStepRepository = workflowStepRepository;
    }

    public WorkflowStep create(WorkflowStep step) {
        log.info("إنشاء خطوة سلسلة عمل — نوع: {} ترتيب: {}", step.getStepType(), step.getStepOrder());
        return workflowStepRepository.save(step);
    }

    @Transactional(readOnly = true)
    public WorkflowStep findById(Long id) {
        return workflowStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStep", "id", id));
    }

    @Transactional(readOnly = true)
    public List<WorkflowStep> findByDefinitionId(Long definitionId) {
        return workflowStepRepository.findByWorkflowDefinitionIdOrderByStepOrderAsc(definitionId);
    }

    public WorkflowStep update(Long id, WorkflowStep updated) {
        WorkflowStep existing = findById(id);
        existing.setStepOrder(updated.getStepOrder());
        existing.setStepType(updated.getStepType());
        existing.setConfiguration(updated.getConfiguration());
        existing.setNextStepOnSuccess(updated.getNextStepOnSuccess());
        existing.setNextStepOnFailure(updated.getNextStepOnFailure());
        existing.setTimeoutSeconds(updated.getTimeoutSeconds());
        log.info("تحديث خطوة سلسلة عمل: {}", id);
        return workflowStepRepository.save(existing);
    }

    public void delete(Long id) {
        WorkflowStep step = findById(id);
        log.info("حذف خطوة سلسلة عمل: {}", id);
        workflowStepRepository.delete(step);
    }

    public void deleteByDefinitionId(Long definitionId) {
        log.info("حذف جميع خطوات التعريف: {}", definitionId);
        workflowStepRepository.deleteByWorkflowDefinitionId(definitionId);
    }
}
