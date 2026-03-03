package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "fulfillment_orders", indexes = {
    @Index(name = "idx_fo_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_fo_shipment", columnList = "shipment_id"),
    @Index(name = "idx_fo_status", columnList = "status"),
    @Index(name = "idx_fo_priority", columnList = "priority"),
    @Index(name = "idx_fo_picker", columnList = "assigned_picker_id")
})
public class FulfillmentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "shipment_id")
    private Long shipmentId;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "رقم الطلب مطلوب")
    private String orderNumber;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(nullable = false, length = 10)
    private String priority = "STANDARD";

    @Column(name = "assigned_picker_id")
    private Long assignedPickerId;

    @Column(name = "assigned_packer_id")
    private Long assignedPackerId;

    @Column(name = "picked_at")
    private Instant pickedAt;

    @Column(name = "packed_at")
    private Instant packedAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public FulfillmentOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Long getAssignedPickerId() { return assignedPickerId; }
    public void setAssignedPickerId(Long assignedPickerId) { this.assignedPickerId = assignedPickerId; }
    public Long getAssignedPackerId() { return assignedPackerId; }
    public void setAssignedPackerId(Long assignedPackerId) { this.assignedPackerId = assignedPackerId; }
    public Instant getPickedAt() { return pickedAt; }
    public void setPickedAt(Instant pickedAt) { this.pickedAt = pickedAt; }
    public Instant getPackedAt() { return packedAt; }
    public void setPackedAt(Instant packedAt) { this.packedAt = packedAt; }
    public Instant getShippedAt() { return shippedAt; }
    public void setShippedAt(Instant shippedAt) { this.shippedAt = shippedAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FulfillmentOrder that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
