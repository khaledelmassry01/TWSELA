package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * جلسة تتبع حية لشحنة — تُحدث طوال مدة التوصيل وتنتهي عند الإتمام.
 */
@Entity
@Table(name = "tracking_sessions", indexes = {
        @Index(name = "idx_ts_shipment", columnList = "shipment_id"),
        @Index(name = "idx_ts_courier", columnList = "courier_id"),
        @Index(name = "idx_ts_status", columnList = "status")
})
public class TrackingSession {

    public enum SessionStatus { ACTIVE, PAUSED, ENDED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private User courier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "last_ping_at")
    private Instant lastPingAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "estimated_arrival")
    private Instant estimatedArrival;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "total_pings")
    private Integer totalPings = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ──

    public TrackingSession() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getLastPingAt() { return lastPingAt; }
    public void setLastPingAt(Instant lastPingAt) { this.lastPingAt = lastPingAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public Instant getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(Instant estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public Double getCurrentLat() { return currentLat; }
    public void setCurrentLat(Double currentLat) { this.currentLat = currentLat; }

    public Double getCurrentLng() { return currentLng; }
    public void setCurrentLng(Double currentLng) { this.currentLng = currentLng; }

    public Double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public Integer getTotalPings() { return totalPings; }
    public void setTotalPings(Integer totalPings) { this.totalPings = totalPings; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackingSession that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "TrackingSession{id=" + id + ", status=" + status + '}';
    }
}
