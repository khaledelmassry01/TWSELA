package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * اشتراك في نوع حدث معين.
 */
@Entity
@Table(name = "event_subscriptions", indexes = {
        @Index(name = "idx_event_sub_type", columnList = "event_type"),
        @Index(name = "idx_event_sub_active", columnList = "active"),
        @Index(name = "idx_event_sub_subscriber", columnList = "subscriber_name")
})
public class EventSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscriber_name", nullable = false, length = 100)
    private String subscriberName;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "handler_class", nullable = false, length = 255)
    private String handlerClass;

    @Column(name = "filter_expression", length = 500)
    private String filterExpression;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "retry_policy", columnDefinition = "TEXT")
    private String retryPolicy;

    @Column(name = "last_processed_at")
    private Instant lastProcessedAt;

    @Column(name = "failure_count", nullable = false)
    private int failureCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubscriberName() { return subscriberName; }
    public void setSubscriberName(String subscriberName) { this.subscriberName = subscriberName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getHandlerClass() { return handlerClass; }
    public void setHandlerClass(String handlerClass) { this.handlerClass = handlerClass; }

    public String getFilterExpression() { return filterExpression; }
    public void setFilterExpression(String filterExpression) { this.filterExpression = filterExpression; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }

    public Instant getLastProcessedAt() { return lastProcessedAt; }
    public void setLastProcessedAt(Instant lastProcessedAt) { this.lastProcessedAt = lastProcessedAt; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventSubscription that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
