package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * مهمة مجدولة يحددها المستخدم — تعمل بتعبير Cron.
 */
@Entity
@Table(name = "scheduled_tasks", indexes = {
        @Index(name = "idx_sched_task_active", columnList = "is_active"),
        @Index(name = "idx_sched_task_next_run", columnList = "next_run_at"),
        @Index(name = "idx_sched_task_tenant", columnList = "tenant_id")
})
public class ScheduledTask {

    public enum TaskType {
        GENERATE_REPORT, EXPIRE_SHIPMENTS, SEND_REMINDERS,
        SYNC_INVENTORY, PROCESS_SETTLEMENTS, CLEANUP_DATA,
        CUSTOM_WEBHOOK
    }

    public enum TaskStatus {
        SUCCESS, FAILED, RUNNING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 30)
    private TaskType taskType;

    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression;

    @Column(columnDefinition = "TEXT")
    private String configuration;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_run_status", length = 20)
    private TaskStatus lastRunStatus;

    @Column(name = "last_run_duration_ms")
    private Long lastRunDurationMs;

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

    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public Instant getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(Instant lastRunAt) { this.lastRunAt = lastRunAt; }

    public Instant getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(Instant nextRunAt) { this.nextRunAt = nextRunAt; }

    public TaskStatus getLastRunStatus() { return lastRunStatus; }
    public void setLastRunStatus(TaskStatus lastRunStatus) { this.lastRunStatus = lastRunStatus; }

    public Long getLastRunDurationMs() { return lastRunDurationMs; }
    public void setLastRunDurationMs(Long lastRunDurationMs) { this.lastRunDurationMs = lastRunDurationMs; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledTask that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
