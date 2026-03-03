package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * تنفيذ فعلي لسلسلة عمل.
 */
@Entity
@Table(name = "workflow_executions", indexes = {
        @Index(name = "idx_wf_exec_def", columnList = "workflow_definition_id"),
        @Index(name = "idx_wf_exec_status", columnList = "status"),
        @Index(name = "idx_wf_exec_entity", columnList = "trigger_entity_type, trigger_entity_id")
})
public class WorkflowExecution {

    public enum ExecutionStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED, PAUSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "trigger_entity_type", length = 50)
    private String triggerEntityType;

    @Column(name = "trigger_entity_id")
    private Long triggerEntityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionStatus status = ExecutionStatus.RUNNING;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String context;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (startedAt == null) startedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkflowDefinition getWorkflowDefinition() { return workflowDefinition; }
    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) { this.workflowDefinition = workflowDefinition; }

    public String getTriggerEntityType() { return triggerEntityType; }
    public void setTriggerEntityType(String triggerEntityType) { this.triggerEntityType = triggerEntityType; }

    public Long getTriggerEntityId() { return triggerEntityId; }
    public void setTriggerEntityId(Long triggerEntityId) { this.triggerEntityId = triggerEntityId; }

    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowExecution that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
