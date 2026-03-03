package com.twsela.repository;

import com.twsela.domain.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long> {

    List<WorkflowExecution> findByWorkflowDefinitionId(Long workflowDefinitionId);

    List<WorkflowExecution> findByStatus(WorkflowExecution.ExecutionStatus status);

    List<WorkflowExecution> findByTriggerEntityTypeAndTriggerEntityId(String triggerEntityType, Long triggerEntityId);

    long countByWorkflowDefinitionIdAndStatus(Long workflowDefinitionId, WorkflowExecution.ExecutionStatus status);
}
