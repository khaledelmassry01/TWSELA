package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * تنفيذ خطوة واحدة ضمن تنفيذ سلسلة عمل.
 */
@Entity
@Table(name = "workflow_step_executions", indexes = {
        @Index(name = "idx_wf_step_exec_exec", columnList = "workflow_execution_id"),
        @Index(name = "idx_wf_step_exec_status", columnList = "status")
})
public class WorkflowStepExecution {

    public enum StepExecutionStatus {
        PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_execution_id", nullable = false)
    private WorkflowExecution workflowExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStep workflowStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StepExecutionStatus status = StepExecutionStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkflowExecution getWorkflowExecution() { return workflowExecution; }
    public void setWorkflowExecution(WorkflowExecution workflowExecution) { this.workflowExecution = workflowExecution; }

    public WorkflowStep getWorkflowStep() { return workflowStep; }
    public void setWorkflowStep(WorkflowStep workflowStep) { this.workflowStep = workflowStep; }

    public StepExecutionStatus getStatus() { return status; }
    public void setStatus(StepExecutionStatus status) { this.status = status; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowStepExecution that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
