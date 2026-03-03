package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Records each delivery attempt for a shipment, tracking reasons for failure,
 * GPS coordinates, and scheduling the next retry.
 */
@Entity
@Table(name = "delivery_attempts", indexes = {
        @Index(name = "idx_da_shipment", columnList = "shipment_id"),
        @Index(name = "idx_da_courier", columnList = "courier_id"),
        @Index(name = "idx_da_attempted_at", columnList = "attempted_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_da_shipment_attempt", columnNames = {"shipment_id", "attempt_number"})
})
public class DeliveryAttempt {

    public enum AttemptStatus { FAILED, SUCCESS }

    public enum FailureReason {
        CUSTOMER_ABSENT, WRONG_ADDRESS, REFUSED, PHONE_OFF, DAMAGED, OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private AttemptStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 20)
    private FailureReason failureReason;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(name = "next_attempt_date")
    private LocalDate nextAttemptDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private User courier;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ────────────────────────────────────────

    public DeliveryAttempt() {}

    // ── Getters / Setters ───────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public FailureReason getFailureReason() { return failureReason; }
    public void setFailureReason(FailureReason failureReason) { this.failureReason = failureReason; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(Instant attemptedAt) { this.attemptedAt = attemptedAt; }

    public LocalDate getNextAttemptDate() { return nextAttemptDate; }
    public void setNextAttemptDate(LocalDate nextAttemptDate) { this.nextAttemptDate = nextAttemptDate; }

    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryAttempt that = (DeliveryAttempt) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
