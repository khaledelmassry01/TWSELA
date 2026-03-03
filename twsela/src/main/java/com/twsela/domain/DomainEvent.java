package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * حدث نطاق مسجل في النظام — Event Sourcing.
 */
@Entity
@Table(name = "domain_events", indexes = {
        @Index(name = "idx_domain_event_type", columnList = "event_type"),
        @Index(name = "idx_domain_event_aggregate", columnList = "aggregate_type, aggregate_id"),
        @Index(name = "idx_domain_event_status", columnList = "status"),
        @Index(name = "idx_domain_event_published", columnList = "published_at")
})
public class DomainEvent {

    public enum EventStatus {
        PENDING, PUBLISHED, PROCESSED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private int version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.PENDING;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (eventId == null) eventId = UUID.randomUUID().toString();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public Long getAggregateId() { return aggregateId; }
    public void setAggregateId(Long aggregateId) { this.aggregateId = aggregateId; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainEvent that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
