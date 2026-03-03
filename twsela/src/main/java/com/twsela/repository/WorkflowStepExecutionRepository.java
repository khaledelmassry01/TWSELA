package com.twsela.repository;

import com.twsela.domain.WorkflowStepExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowStepExecutionRepository extends JpaRepository<WorkflowStepExecution, Long> {

    List<WorkflowStepExecution> findByWorkflowExecutionIdOrderByIdAsc(Long workflowExecutionId);

    List<WorkflowStepExecution> findByWorkflowExecutionIdAndStatus(Long workflowExecutionId, WorkflowStepExecution.StepExecutionStatus status);

    long countByWorkflowExecutionIdAndStatus(Long workflowExecutionId, WorkflowStepExecution.StepExecutionStatus status);
}
