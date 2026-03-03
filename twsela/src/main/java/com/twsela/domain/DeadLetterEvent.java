package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * حدث فاشل منقول إلى Dead Letter Queue.
 */
@Entity
@Table(name = "dead_letter_events", indexes = {
        @Index(name = "idx_dle_resolved", columnList = "resolved"),
        @Index(name = "idx_dle_next_retry", columnList = "next_retry_at"),
        @Index(name = "idx_dle_event", columnList = "original_event_id")
})
public class DeadLetterEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_event_id", nullable = false)
    private DomainEvent originalEvent;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "failure_count", nullable = false)
    private int failureCount = 1;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(nullable = false)
    private boolean resolved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DomainEvent getOriginalEvent() { return originalEvent; }
    public void setOriginalEvent(DomainEvent originalEvent) { this.originalEvent = originalEvent; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(Instant lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }

    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeadLetterEvent that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
