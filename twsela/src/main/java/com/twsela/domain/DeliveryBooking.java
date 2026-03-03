package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "delivery_bookings")
public class DeliveryBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id")
    private Long shipmentId;

    @NotNull
    @Column(name = "delivery_time_slot_id", nullable = false)
    private Long deliveryTimeSlotId;

    @Column(name = "recipient_profile_id")
    private Long recipientProfileId;

    @NotNull
    @Column(name = "selected_date", nullable = false)
    private LocalDate selectedDate;

    @Column(nullable = false, length = 20)
    private String status = "BOOKED";

    @Column(name = "rescheduled_from_id")
    private Long rescheduledFromId;

    @Column(name = "rescheduled_reason", length = 255)
    private String rescheduledReason;

    @Column(name = "booked_at")
    private Instant bookedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
        if (bookedAt == null) bookedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getDeliveryTimeSlotId() { return deliveryTimeSlotId; }
    public void setDeliveryTimeSlotId(Long deliveryTimeSlotId) { this.deliveryTimeSlotId = deliveryTimeSlotId; }
    public Long getRecipientProfileId() { return recipientProfileId; }
    public void setRecipientProfileId(Long recipientProfileId) { this.recipientProfileId = recipientProfileId; }
    public LocalDate getSelectedDate() { return selectedDate; }
    public void setSelectedDate(LocalDate selectedDate) { this.selectedDate = selectedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getRescheduledFromId() { return rescheduledFromId; }
    public void setRescheduledFromId(Long rescheduledFromId) { this.rescheduledFromId = rescheduledFromId; }
    public String getRescheduledReason() { return rescheduledReason; }
    public void setRescheduledReason(String rescheduledReason) { this.rescheduledReason = rescheduledReason; }
    public Instant getBookedAt() { return bookedAt; }
    public void setBookedAt(Instant bookedAt) { this.bookedAt = bookedAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
