package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "shipment_manifests", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_manifest_number", columnNames = {"manifest_number"})
    },
    indexes = {
        @Index(name = "idx_courier_status", columnList = "courier_id, status")
    }
)
public class ShipmentManifest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "courier_id", nullable = false)
    private User courier;

    @Column(name = "manifest_number", nullable = false, unique = true, length = 50)
    private String manifestNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ManifestStatus status = ManifestStatus.CREATED;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt = Instant.now();

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Shipment> shipments = new java.util.HashSet<>();

    public enum ManifestStatus {
        CREATED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    // Constructors
    public ShipmentManifest() {}

    public ShipmentManifest(User courier, String manifestNumber) {
        this.courier = courier;
        this.manifestNumber = manifestNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; }
    public String getManifestNumber() { return manifestNumber; }
    public void setManifestNumber(String manifestNumber) { this.manifestNumber = manifestNumber; }
    public ManifestStatus getStatus() { return status; }
    public void setStatus(ManifestStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
    public Set<Shipment> getShipments() { return shipments; }
    public void setShipments(Set<Shipment> shipments) { this.shipments = shipments; }

    @Override
    public String toString() {
        return "ShipmentManifest{" +
                "id=" + id +
                ", manifestNumber='" + manifestNumber + '\'' +
                ", status=" + status +
                ", courier=" + (courier != null ? courier.getName() : "null") +
                '}';
    }
}

