package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carrier_rates")
public class CarrierRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "carrier_id", nullable = false)
    private Long carrierId;

    @Column(name = "carrier_zone_mapping_id")
    private Long carrierZoneMappingId;

    @NotNull
    @Column(name = "min_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal minWeight = BigDecimal.ZERO;

    @NotNull
    @Column(name = "max_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxWeight = new BigDecimal("999.99");

    @NotNull
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @NotNull
    @Column(name = "per_kg_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal perKgPrice = BigDecimal.ZERO;

    @NotNull
    @Size(max = 3)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EGP";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCarrierId() { return carrierId; }
    public void setCarrierId(Long carrierId) { this.carrierId = carrierId; }
    public Long getCarrierZoneMappingId() { return carrierZoneMappingId; }
    public void setCarrierZoneMappingId(Long carrierZoneMappingId) { this.carrierZoneMappingId = carrierZoneMappingId; }
    public BigDecimal getMinWeight() { return minWeight; }
    public void setMinWeight(BigDecimal minWeight) { this.minWeight = minWeight; }
    public BigDecimal getMaxWeight() { return maxWeight; }
    public void setMaxWeight(BigDecimal maxWeight) { this.maxWeight = maxWeight; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public BigDecimal getPerKgPrice() { return perKgPrice; }
    public void setPerKgPrice(BigDecimal perKgPrice) { this.perKgPrice = perKgPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
