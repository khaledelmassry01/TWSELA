package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "receiving_order_items", indexes = {
    @Index(name = "idx_roi_order", columnList = "receiving_order_id"),
    @Index(name = "idx_roi_status", columnList = "status")
})
public class ReceivingOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiving_order_id", nullable = false)
    private Long receivingOrderId;

    @Column(name = "product_sku", length = 50)
    private String productSku;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "expected_quantity", nullable = false)
    private int expectedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private int receivedQuantity;

    @Column(name = "damaged_quantity", nullable = false)
    private int damagedQuantity;

    @Column(name = "assigned_bin_id")
    private Long assignedBinId;

    @Column(name = "inspection_notes", columnDefinition = "TEXT")
    private String inspectionNotes;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ReceivingOrderItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReceivingOrderId() { return receivingOrderId; }
    public void setReceivingOrderId(Long receivingOrderId) { this.receivingOrderId = receivingOrderId; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getExpectedQuantity() { return expectedQuantity; }
    public void setExpectedQuantity(int expectedQuantity) { this.expectedQuantity = expectedQuantity; }
    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public int getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(int damagedQuantity) { this.damagedQuantity = damagedQuantity; }
    public Long getAssignedBinId() { return assignedBinId; }
    public void setAssignedBinId(Long assignedBinId) { this.assignedBinId = assignedBinId; }
    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceivingOrderItem that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
