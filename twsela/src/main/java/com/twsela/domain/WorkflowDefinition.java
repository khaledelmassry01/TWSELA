package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * تعريف سلسلة عمل — مجموعة خطوات تُنفَّذ عند حدث معيّن.
 */
@Entity
@Table(name = "workflow_definitions", indexes = {
        @Index(name = "idx_wf_def_trigger", columnList = "trigger_event"),
        @Index(name = "idx_wf_def_active", columnList = "is_active"),
        @Index(name = "idx_wf_def_tenant", columnList = "tenant_id"),
        @Index(name = "idx_wf_def_created_by", columnList = "created_by_id")
})
public class WorkflowDefinition {

    public enum TriggerEvent {
        SHIPMENT_CREATED, STATUS_CHANGED, PAYMENT_RECEIVED,
        DELIVERY_FAILED, RETURN_REQUESTED, SLA_BREACHED,
        RATING_SUBMITTED, DAILY_SCHEDULE
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
    private TriggerEvent triggerEvent;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private int priority = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

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

    public TriggerEvent getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(TriggerEvent triggerEvent) { this.triggerEvent = triggerEvent; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowDefinition that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
