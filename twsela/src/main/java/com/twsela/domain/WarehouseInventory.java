package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "warehouse_inventory", indexes = {
    @Index(name = "idx_warehouse_status", columnList = "warehouse_id, status"),
    @Index(name = "FK_wi_receiver", columnList = "received_by_user_id")
})
public class WarehouseInventory {

    @Id
    @Column(name = "shipment_id")
    private Long shipmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "shipment_id")
    @NotNull(message = "Shipment is required")
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @NotNull(message = "Warehouse is required")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "received_by_user_id", nullable = false)
    @NotNull(message = "Received by user is required")
    private User receivedBy;

    @Column(name = "received_at", nullable = false)
    @NotNull(message = "Received at timestamp is required")
    private Instant receivedAt;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status is required")
    private InventoryStatus status = InventoryStatus.RECEIVED;

    public enum InventoryStatus {
        RECEIVED, IN_STORAGE, DISPATCHED, PICKED_UP
    }

    // Constructors
    public WarehouseInventory() {}

    public WarehouseInventory(Shipment shipment, Warehouse warehouse, User receivedBy) {
        this.shipment = shipment;
        this.warehouse = warehouse;
        this.receivedBy = receivedBy;
        this.receivedAt = Instant.now();
        this.status = InventoryStatus.RECEIVED;
    }

    public WarehouseInventory(Shipment shipment, Warehouse warehouse, User receivedBy, Instant receivedAt) {
        this.shipment = shipment;
        this.warehouse = warehouse;
        this.receivedBy = receivedBy;
        this.receivedAt = receivedAt;
        this.status = InventoryStatus.RECEIVED;
    }

    // Getters and Setters
    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public User getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(User receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(Instant dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }

    // Helper methods
    public void markAsInStorage() {
        this.status = InventoryStatus.IN_STORAGE;
    }

    public void markAsDispatched() {
        this.status = InventoryStatus.DISPATCHED;
        this.dispatchedAt = Instant.now();
    }

    public void markAsPickedUp() {
        this.status = InventoryStatus.PICKED_UP;
    }

    public boolean isReceived() {
        return status == InventoryStatus.RECEIVED;
    }

    public boolean isInStorage() {
        return status == InventoryStatus.IN_STORAGE;
    }

    public boolean isDispatched() {
        return status == InventoryStatus.DISPATCHED;
    }

    public boolean isPickedUp() {
        return status == InventoryStatus.PICKED_UP;
    }

    public boolean isActive() {
        return status == InventoryStatus.RECEIVED || status == InventoryStatus.IN_STORAGE;
    }

    @Override
    public String toString() {
        return "WarehouseInventory{" +
                "shipmentId=" + shipmentId +
                ", warehouse=" + (warehouse != null ? warehouse.getName() : "null") +
                ", receivedBy=" + (receivedBy != null ? receivedBy.getName() : "null") +
                ", receivedAt=" + receivedAt +
                ", dispatchedAt=" + dispatchedAt +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WarehouseInventory)) return false;
        WarehouseInventory that = (WarehouseInventory) o;
        return shipmentId != null && shipmentId.equals(that.shipmentId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
