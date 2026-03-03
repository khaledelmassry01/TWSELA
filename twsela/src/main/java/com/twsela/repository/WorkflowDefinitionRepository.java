package com.twsela.repository;

import com.twsela.domain.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    List<WorkflowDefinition> findByTenantIdAndIsActiveTrue(Long tenantId);

    List<WorkflowDefinition> findByTriggerEventAndIsActiveTrue(WorkflowDefinition.TriggerEvent triggerEvent);

    List<WorkflowDefinition> findByTenantId(Long tenantId);

    List<WorkflowDefinition> findByTenantIdAndTriggerEvent(Long tenantId, WorkflowDefinition.TriggerEvent triggerEvent);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
