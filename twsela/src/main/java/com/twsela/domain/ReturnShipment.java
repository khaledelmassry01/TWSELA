package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "return_shipments", indexes = {
    @Index(name = "FK_rs_original", columnList = "original_shipment_id"),
    @Index(name = "FK_rs_return", columnList = "return_shipment_id")
})
public class ReturnShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_shipment_id", nullable = false)
    private Shipment originalShipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_shipment_id", nullable = false)
    private Shipment returnShipment;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // Constructors
    public ReturnShipment() {}

    public ReturnShipment(Shipment originalShipment, Shipment returnShipment, String reason) {
        this.originalShipment = originalShipment;
        this.returnShipment = returnShipment;
        this.reason = reason;
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
