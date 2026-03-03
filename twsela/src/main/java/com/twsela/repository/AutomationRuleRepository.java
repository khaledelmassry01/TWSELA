package com.twsela.repository;

import com.twsela.domain.AutomationRule;
import com.twsela.domain.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    List<AutomationRule> findByTenantIdAndIsActiveTrue(Long tenantId);

    List<AutomationRule> findByTriggerEventAndIsActiveTrue(WorkflowDefinition.TriggerEvent triggerEvent);

    List<AutomationRule> findByTenantId(Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
