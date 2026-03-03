package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "pick_waves", indexes = {
    @Index(name = "idx_pw_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_pw_status", columnList = "status"),
    @Index(name = "idx_pw_picker", columnList = "assigned_picker_id")
})
public class PickWave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "wave_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "رقم الموجة مطلوب")
    private String waveNumber;

    @Column(nullable = false, length = 20)
    private String status = "CREATED";

    @Column(nullable = false, length = 20)
    private String strategy = "SINGLE_ORDER";

    @Column(name = "total_orders", nullable = false)
    private int totalOrders;

    @Column(name = "completed_orders", nullable = false)
    private int completedOrders;

    @Column(name = "assigned_picker_id")
    private Long assignedPickerId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public PickWave() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWaveNumber() { return waveNumber; }
    public void setWaveNumber(String waveNumber) { this.waveNumber = waveNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
    public Long getAssignedPickerId() { return assignedPickerId; }
    public void setAssignedPickerId(Long assignedPickerId) { this.assignedPickerId = assignedPickerId; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PickWave that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
