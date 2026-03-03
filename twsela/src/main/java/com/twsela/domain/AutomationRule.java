package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * قاعدة أتمتة بسيطة (if/then) — عند حدث معيّن يتحقق شرط ويُنفَّذ فعل.
 */
@Entity
@Table(name = "automation_rules", indexes = {
        @Index(name = "idx_auto_rule_trigger", columnList = "trigger_event"),
        @Index(name = "idx_auto_rule_active", columnList = "is_active"),
        @Index(name = "idx_auto_rule_tenant", columnList = "tenant_id")
})
public class AutomationRule {

    public enum ActionType {
        SEND_NOTIFICATION, CHANGE_STATUS, ASSIGN_COURIER,
        UPDATE_FIELD, CALL_WEBHOOK, CREATE_TICKET,
        ADD_TAG, SEND_SMS, SEND_EMAIL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_event", nullable = false, length = 30)
    private WorkflowDefinition.TriggerEvent triggerEvent;

    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;

    @Column(name = "action_config", columnDefinition = "TEXT")
    private String actionConfig;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(name = "execution_count", nullable = false)
    private long executionCount = 0;

    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public WorkflowDefinition.TriggerEvent getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(WorkflowDefinition.TriggerEvent triggerEvent) { this.triggerEvent = triggerEvent; }

    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public String getActionConfig() { return actionConfig; }
    public void setActionConfig(String actionConfig) { this.actionConfig = actionConfig; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public long getExecutionCount() { return executionCount; }
    public void setExecutionCount(long executionCount) { this.executionCount = executionCount; }

    public Instant getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(Instant lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AutomationRule that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
