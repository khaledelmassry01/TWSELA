package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "storage_bins", indexes = {
    @Index(name = "idx_sb_zone", columnList = "warehouse_zone_id"),
    @Index(name = "idx_sb_code", columnList = "bin_code"),
    @Index(name = "idx_sb_occupied", columnList = "is_occupied"),
    @Index(name = "idx_sb_active", columnList = "is_active")
})
public class StorageBin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_zone_id", nullable = false)
    private Long warehouseZoneId;

    @Column(name = "bin_code", nullable = false, length = 30)
    @NotBlank(message = "رمز الحاوية مطلوب")
    @Size(max = 30)
    private String binCode;

    @Column(length = 10)
    private String aisle;

    @Column(length = 10)
    private String rack;

    @Column(length = 10)
    private String shelf;

    @Column(length = 10)
    private String position;

    @Column(name = "bin_type", nullable = false, length = 20)
    private String binType = "STANDARD";

    @Column(name = "max_weight", precision = 10, scale = 2)
    private BigDecimal maxWeight;

    @Column(name = "max_items", nullable = false)
    private int maxItems = 100;

    @Column(name = "current_items", nullable = false)
    private int currentItems;

    @Column(name = "is_occupied", nullable = false)
    private boolean occupied;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public StorageBin() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseZoneId() { return warehouseZoneId; }
    public void setWarehouseZoneId(Long warehouseZoneId) { this.warehouseZoneId = warehouseZoneId; }
    public String getBinCode() { return binCode; }
    public void setBinCode(String binCode) { this.binCode = binCode; }
    public String getAisle() { return aisle; }
    public void setAisle(String aisle) { this.aisle = aisle; }
    public String getRack() { return rack; }
    public void setRack(String rack) { this.rack = rack; }
    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getBinType() { return binType; }
    public void setBinType(String binType) { this.binType = binType; }
    public BigDecimal getMaxWeight() { return maxWeight; }
    public void setMaxWeight(BigDecimal maxWeight) { this.maxWeight = maxWeight; }
    public int getMaxItems() { return maxItems; }
    public void setMaxItems(int maxItems) { this.maxItems = maxItems; }
    public int getCurrentItems() { return currentItems; }
    public void setCurrentItems(int currentItems) { this.currentItems = currentItems; }
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorageBin that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
