package com.twsela.service;

import com.twsela.domain.WorkflowDefinition;
import com.twsela.repository.WorkflowDefinitionRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WorkflowDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowDefinitionService.class);

    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    public WorkflowDefinitionService(WorkflowDefinitionRepository workflowDefinitionRepository) {
        this.workflowDefinitionRepository = workflowDefinitionRepository;
    }

    public WorkflowDefinition create(WorkflowDefinition definition) {
        if (workflowDefinitionRepository.existsByNameAndTenantId(definition.getName(),
                definition.getTenant() != null ? definition.getTenant().getId() : null)) {
            throw new BusinessRuleException("يوجد تعريف سلسلة عمل بنفس الاسم");
        }
        log.info("إنشاء تعريف سلسلة عمل: {}", definition.getName());
        return workflowDefinitionRepository.save(definition);
    }

    @Transactional(readOnly = true)
    public WorkflowDefinition findById(Long id) {
        return workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowDefinition", "id", id));
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinition> findByTenantId(Long tenantId) {
        return workflowDefinitionRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinition> findActiveByTenantId(Long tenantId) {
        return workflowDefinitionRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinition> findByTriggerEvent(WorkflowDefinition.TriggerEvent triggerEvent) {
        return workflowDefinitionRepository.findByTriggerEventAndIsActiveTrue(triggerEvent);
    }

    public WorkflowDefinition update(Long id, WorkflowDefinition updated) {
        WorkflowDefinition existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setTriggerEvent(updated.getTriggerEvent());
        existing.setPriority(updated.getPriority());
        existing.setVersion(existing.getVersion() + 1);
        log.info("تحديث تعريف سلسلة عمل: {} v{}", existing.getName(), existing.getVersion());
        return workflowDefinitionRepository.save(existing);
    }

    public WorkflowDefinition activate(Long id) {
        WorkflowDefinition definition = findById(id);
        definition.setActive(true);
        log.info("تفعيل سلسلة عمل: {}", definition.getName());
        return workflowDefinitionRepository.save(definition);
    }

    public WorkflowDefinition deactivate(Long id) {
        WorkflowDefinition definition = findById(id);
        definition.setActive(false);
        log.info("تعطيل سلسلة عمل: {}", definition.getName());
        return workflowDefinitionRepository.save(definition);
    }

    public void delete(Long id) {
        WorkflowDefinition definition = findById(id);
        log.info("حذف تعريف سلسلة عمل: {}", definition.getName());
        workflowDefinitionRepository.delete(definition);
    }
}
