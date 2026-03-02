package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "return_shipments", indexes = {
    @Index(name = "FK_rs_original", columnList = "original_shipment_id"),
    @Index(name = "FK_rs_return", columnList = "return_shipment_id"),
    @Index(name = "idx_rs_status", columnList = "status"),
    @Index(name = "idx_rs_assigned_courier", columnList = "assigned_courier_id")
})
public class ReturnShipment {

    /** Return lifecycle statuses */
    public enum ReturnStatusEnum {
        RETURN_REQUESTED,
        RETURN_APPROVED,
        RETURN_REJECTED,
        RETURN_PICKUP_ASSIGNED,
        RETURN_PICKED_UP,
        RETURN_IN_WAREHOUSE,
        RETURN_DELIVERED_TO_MERCHANT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_shipment_id", nullable = false)
    private Shipment originalShipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_shipment_id")
    private Shipment returnShipment;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ReturnStatusEnum status = ReturnStatusEnum.RETURN_REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_courier_id")
    private User assignedCourier;

    @Column(name = "return_fee", precision = 10, scale = 2)
    private BigDecimal returnFee;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // Constructors
    public ReturnShipment() {}

    public ReturnShipment(Shipment originalShipment, String reason) {
        this.originalShipment = originalShipment;
        this.reason = reason;
        this.status = ReturnStatusEnum.RETURN_REQUESTED;
        this.createdAt = Instant.now();
    }

    public ReturnShipment(Shipment originalShipment, Shipment returnShipment, String reason) {
        this.originalShipment = originalShipment;
        this.returnShipment = returnShipment;
        this.reason = reason;
        this.status = ReturnStatusEnum.RETURN_REQUESTED;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shipment getOriginalShipment() { return originalShipment; }
    public void setOriginalShipment(Shipment originalShipment) { this.originalShipment = originalShipment; }

    public Shipment getReturnShipment() { return returnShipment; }
    public void setReturnShipment(Shipment returnShipment) { this.returnShipment = returnShipment; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public ReturnStatusEnum getStatus() { return status; }
    public void setStatus(ReturnStatusEnum status) { this.status = status; }

    public User getAssignedCourier() { return assignedCourier; }
    public void setAssignedCourier(User assignedCourier) { this.assignedCourier = assignedCourier; }

    public BigDecimal getReturnFee() { return returnFee; }
    public void setReturnFee(BigDecimal returnFee) { this.returnFee = returnFee; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public Instant getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(Instant pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @Override
    public String toString() {
        return "ReturnShipment{" +
                "id=" + id +
                ", originalShipment=" + (originalShipment != null ? originalShipment.getId() : null) +
                ", returnShipment=" + (returnShipment != null ? returnShipment.getId() : null) +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnShipment)) return false;
        ReturnShipment that = (ReturnShipment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
