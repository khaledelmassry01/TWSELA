package com.twsela.service;

import com.twsela.domain.AutomationRule;
import com.twsela.domain.WorkflowDefinition;
import com.twsela.repository.AutomationRuleRepository;
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
public class AutomationRuleService {

    private static final Logger log = LoggerFactory.getLogger(AutomationRuleService.class);

    private final AutomationRuleRepository automationRuleRepository;

    public AutomationRuleService(AutomationRuleRepository automationRuleRepository) {
        this.automationRuleRepository = automationRuleRepository;
    }

    public AutomationRule create(AutomationRule rule) {
        if (automationRuleRepository.existsByNameAndTenantId(rule.getName(),
                rule.getTenant() != null ? rule.getTenant().getId() : null)) {
            throw new BusinessRuleException("يوجد قاعدة أتمتة بنفس الاسم");
        }
        log.info("إنشاء قاعدة أتمتة: {}", rule.getName());
        return automationRuleRepository.save(rule);
    }

    @Transactional(readOnly = true)
    public AutomationRule findById(Long id) {
        return automationRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutomationRule", "id", id));
    }

    @Transactional(readOnly = true)
    public List<AutomationRule> findByTenantId(Long tenantId) {
        return automationRuleRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<AutomationRule> findActiveByTriggerEvent(WorkflowDefinition.TriggerEvent triggerEvent) {
        return automationRuleRepository.findByTriggerEventAndIsActiveTrue(triggerEvent);
    }

    public AutomationRule update(Long id, AutomationRule updated) {
        AutomationRule existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setTriggerEvent(updated.getTriggerEvent());
        existing.setConditionExpression(updated.getConditionExpression());
        existing.setActionType(updated.getActionType());
        existing.setActionConfig(updated.getActionConfig());
        log.info("تحديث قاعدة أتمتة: {}", id);
        return automationRuleRepository.save(existing);
    }

    public AutomationRule activate(Long id) {
        AutomationRule rule = findById(id);
        rule.setActive(true);
        log.info("تفعيل قاعدة أتمتة: {}", rule.getName());
        return automationRuleRepository.save(rule);
    }

    public AutomationRule deactivate(Long id) {
        AutomationRule rule = findById(id);
        rule.setActive(false);
        log.info("تعطيل قاعدة أتمتة: {}", rule.getName());
        return automationRuleRepository.save(rule);
    }

    public AutomationRule recordExecution(Long id) {
        AutomationRule rule = findById(id);
        rule.setExecutionCount(rule.getExecutionCount() + 1);
        rule.setLastTriggeredAt(Instant.now());
        return automationRuleRepository.save(rule);
    }

    public void delete(Long id) {
        AutomationRule rule = findById(id);
        log.info("حذف قاعدة أتمتة: {}", rule.getName());
        automationRuleRepository.delete(rule);
    }
}
