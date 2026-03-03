package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Pickup schedule — a merchant requests a courier to pick up shipments
 * at a specified date, time slot, and address.
 */
@Entity
@Table(name = "pickup_schedules", indexes = {
        @Index(name = "idx_ps_merchant_status", columnList = "merchant_id, status"),
        @Index(name = "idx_ps_courier_date", columnList = "assigned_courier_id, pickup_date"),
        @Index(name = "idx_ps_status_date", columnList = "status, pickup_date")
})
public class PickupSchedule {

    public enum TimeSlot { MORNING_9_12, AFTERNOON_12_3, EVENING_3_6 }

    public enum PickupStatus { SCHEDULED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false, length = 20)
    private TimeSlot timeSlot;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "estimated_shipments", nullable = false)
    private int estimatedShipments;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_courier_id")
    private User assignedCourier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private PickupStatus status = PickupStatus.SCHEDULED;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    // ── Constructors ────────────────────────────────────────

    public PickupSchedule() {}

    // ── Getters / Setters ───────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public int getEstimatedShipments() { return estimatedShipments; }
    public void setEstimatedShipments(int estimatedShipments) { this.estimatedShipments = estimatedShipments; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getAssignedCourier() { return assignedCourier; }
    public void setAssignedCourier(User assignedCourier) { this.assignedCourier = assignedCourier; }

    public PickupStatus getStatus() { return status; }
    public void setStatus(PickupStatus status) { this.status = status; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PickupSchedule that = (PickupSchedule) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
