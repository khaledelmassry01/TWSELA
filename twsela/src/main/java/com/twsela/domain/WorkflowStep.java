package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * خطوة ضمن سلسلة عمل.
 */
@Entity
@Table(name = "workflow_steps", indexes = {
        @Index(name = "idx_wf_step_def", columnList = "workflow_definition_id"),
        @Index(name = "idx_wf_step_order", columnList = "step_order")
})
public class WorkflowStep {

    public enum StepType {
        CONDITION, ACTION, DELAY, BRANCH, LOOP
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 20)
    private StepType stepType;

    @Column(columnDefinition = "TEXT")
    private String configuration;

    @Column(name = "next_step_on_success")
    private Integer nextStepOnSuccess;

    @Column(name = "next_step_on_failure")
    private Integer nextStepOnFailure;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkflowDefinition getWorkflowDefinition() { return workflowDefinition; }
    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) { this.workflowDefinition = workflowDefinition; }

    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }

    public StepType getStepType() { return stepType; }
    public void setStepType(StepType stepType) { this.stepType = stepType; }

    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }

    public Integer getNextStepOnSuccess() { return nextStepOnSuccess; }
    public void setNextStepOnSuccess(Integer nextStepOnSuccess) { this.nextStepOnSuccess = nextStepOnSuccess; }

    public Integer getNextStepOnFailure() { return nextStepOnFailure; }
    public void setNextStepOnFailure(Integer nextStepOnFailure) { this.nextStepOnFailure = nextStepOnFailure; }

    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowStep that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
