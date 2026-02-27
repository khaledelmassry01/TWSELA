package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "delivery_pricing", 
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_merchant_zone", columnNames = {"merchant_id", "zone_id"})
    },
    indexes = {
        @Index(name = "FK_dp_zone", columnList = "zone_id")
    }
)
public class DeliveryPricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private Instant updatedAt = Instant.now();

    // Constructors
    public DeliveryPricing() {}

    public DeliveryPricing(User merchant, Zone zone, BigDecimal deliveryFee) {
        this.merchant = merchant;
        this.zone = zone;
        this.deliveryFee = deliveryFee;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }
    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "DeliveryPricing{" +
                "id=" + id +
                ", deliveryFee=" + deliveryFee +
                ", isActive=" + isActive +
                ", merchant=" + (merchant != null ? merchant.getName() : "null") +
                ", zone=" + (zone != null ? zone.getName() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeliveryPricing)) return false;
        DeliveryPricing that = (DeliveryPricing) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}