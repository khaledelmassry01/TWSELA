package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_programs")
public class LoyaltyProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "merchant_id", nullable = false, unique = true)
    private Long merchantId;

    @NotNull
    @Min(0)
    @Column(name = "current_points", nullable = false)
    private Long currentPoints = 0L;

    @NotNull
    @Min(0)
    @Column(name = "lifetime_points", nullable = false)
    private Long lifetimePoints = 0L;

    @NotNull
    @Size(max = 20)
    @Column(name = "tier", nullable = false, length = 20)
    private String tier = "MEMBER";

    @Column(name = "tier_expires_at")
    private LocalDateTime tierExpiresAt;

    @Column(name = "points_expiring_at")
    private LocalDateTime pointsExpiringAt;

    @NotNull
    @Min(0)
    @Column(name = "points_expiring", nullable = false)
    private Integer pointsExpiring = 0;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(Long currentPoints) { this.currentPoints = currentPoints; }

    public Long getLifetimePoints() { return lifetimePoints; }
    public void setLifetimePoints(Long lifetimePoints) { this.lifetimePoints = lifetimePoints; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public LocalDateTime getTierExpiresAt() { return tierExpiresAt; }
    public void setTierExpiresAt(LocalDateTime tierExpiresAt) { this.tierExpiresAt = tierExpiresAt; }

    public LocalDateTime getPointsExpiringAt() { return pointsExpiringAt; }
    public void setPointsExpiringAt(LocalDateTime pointsExpiringAt) { this.pointsExpiringAt = pointsExpiringAt; }

    public Integer getPointsExpiring() { return pointsExpiring; }
    public void setPointsExpiring(Integer pointsExpiring) { this.pointsExpiring = pointsExpiring; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
