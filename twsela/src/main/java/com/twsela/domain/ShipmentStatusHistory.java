package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "shipment_status_history", indexes = {
    @Index(name = "FK_ssh_shipment", columnList = "shipment_id"),
    @Index(name = "FK_ssh_status", columnList = "status_id"),
    @Index(name = "idx_ssh_created_at", columnList = "created_at"),
    @Index(name = "idx_ssh_shipment_created", columnList = "shipment_id, created_at")
})
public class ShipmentStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    @JsonBackReference
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private ShipmentStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt = Instant.now();

    // Constructors
    public ShipmentStatusHistory() {}

    public ShipmentStatusHistory(Shipment shipment, ShipmentStatus status, String notes) {
        this.shipment = shipment;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ShipmentStatusHistory{" +
                "id=" + id +
                ", status=" + (status != null ? status.getName() : "null") +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentStatusHistory)) return false;
        ShipmentStatusHistory that = (ShipmentStatusHistory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}