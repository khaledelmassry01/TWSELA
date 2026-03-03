package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "receiving_orders", indexes = {
    @Index(name = "idx_ro_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_ro_merchant", columnList = "merchant_id"),
    @Index(name = "idx_ro_status", columnList = "status"),
    @Index(name = "idx_ro_refnum", columnList = "reference_number")
})
public class ReceivingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "رقم المرجع مطلوب")
    private String referenceNumber;

    @Column(nullable = false, length = 20)
    private String status = "EXPECTED";

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(name = "arrived_at")
    private Instant arrivedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "total_expected_items", nullable = false)
    private int totalExpectedItems;

    @Column(name = "total_received_items", nullable = false)
    private int totalReceivedItems;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "received_by_id")
    private Long receivedById;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ReceivingOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
    public Instant getArrivedAt() { return arrivedAt; }
    public void setArrivedAt(Instant arrivedAt) { this.arrivedAt = arrivedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public int getTotalExpectedItems() { return totalExpectedItems; }
    public void setTotalExpectedItems(int totalExpectedItems) { this.totalExpectedItems = totalExpectedItems; }
    public int getTotalReceivedItems() { return totalReceivedItems; }
    public void setTotalReceivedItems(int totalReceivedItems) { this.totalReceivedItems = totalReceivedItems; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getReceivedById() { return receivedById; }
    public void setReceivedById(Long receivedById) { this.receivedById = receivedById; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceivingOrder that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
