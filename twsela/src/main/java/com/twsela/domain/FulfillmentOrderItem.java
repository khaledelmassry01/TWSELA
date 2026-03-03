package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "fulfillment_order_items", indexes = {
    @Index(name = "idx_foi_order", columnList = "fulfillment_order_id")
})
public class FulfillmentOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fulfillment_order_id", nullable = false)
    private Long fulfillmentOrderId;

    @Column(name = "product_sku", length = 50)
    private String productSku;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "picked_quantity", nullable = false)
    private int pickedQuantity;

    @Column(name = "source_bin_id")
    private Long sourceBinId;

    @Column(name = "pick_sequence", nullable = false)
    private int pickSequence;

    @Column(name = "is_picked", nullable = false)
    private boolean picked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public FulfillmentOrderItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFulfillmentOrderId() { return fulfillmentOrderId; }
    public void setFulfillmentOrderId(Long fulfillmentOrderId) { this.fulfillmentOrderId = fulfillmentOrderId; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getPickedQuantity() { return pickedQuantity; }
    public void setPickedQuantity(int pickedQuantity) { this.pickedQuantity = pickedQuantity; }
    public Long getSourceBinId() { return sourceBinId; }
    public void setSourceBinId(Long sourceBinId) { this.sourceBinId = sourceBinId; }
    public int getPickSequence() { return pickSequence; }
    public void setPickSequence(int pickSequence) { this.pickSequence = pickSequence; }
    public boolean isPicked() { return picked; }
    public void setPicked(boolean picked) { this.picked = picked; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FulfillmentOrderItem that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
