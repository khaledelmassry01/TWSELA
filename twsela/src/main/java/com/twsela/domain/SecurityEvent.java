package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * حدث أمني مسجل في النظام.
 */
@Entity
@Table(name = "security_events", indexes = {
        @Index(name = "idx_security_event_user", columnList = "user_id"),
        @Index(name = "idx_security_event_type", columnList = "event_type"),
        @Index(name = "idx_security_event_ip", columnList = "ip_address"),
        @Index(name = "idx_security_event_severity", columnList = "severity"),
        @Index(name = "idx_security_event_created", columnList = "created_at")
})
public class SecurityEvent {

    public enum EventType {
        LOGIN_SUCCESS, LOGIN_FAILURE, BRUTE_FORCE_DETECTED, TOKEN_REVOKED,
        PASSWORD_CHANGED, SUSPICIOUS_ACTIVITY, IP_BLOCKED, ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED, UNAUTHORIZED_ACCESS
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(name = "created_at")
    private Instant createdAt;

    public SecurityEvent() {
        this.createdAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityEvent that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
