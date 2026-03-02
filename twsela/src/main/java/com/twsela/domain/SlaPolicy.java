package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * SLA policy defining response and resolution time targets.
 */
@Entity
@Table(name = "sla_policies", indexes = {
        @Index(name = "idx_sla_priority", columnList = "priority"),
        @Index(name = "idx_sla_active", columnList = "is_active")
})
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SupportTicket.TicketPriority priority;

    @Column(name = "first_response_hours", nullable = false)
    private int firstResponseHours;

    @Column(name = "resolution_hours", nullable = false)
    private int resolutionHours;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Getters/Setters ─────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SupportTicket.TicketPriority getPriority() { return priority; }
    public void setPriority(SupportTicket.TicketPriority priority) { this.priority = priority; }

    public int getFirstResponseHours() { return firstResponseHours; }
    public void setFirstResponseHours(int firstResponseHours) { this.firstResponseHours = firstResponseHours; }

    public int getResolutionHours() { return resolutionHours; }
    public void setResolutionHours(int resolutionHours) { this.resolutionHours = resolutionHours; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlaPolicy that = (SlaPolicy) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
