package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a subscription plan that merchants can subscribe to.
 * Plans: FREE, BASIC, PRO, ENTERPRISE
 */
@Entity
@Table(name = "subscription_plans", indexes = {
    @Index(name = "idx_plan_name", columnList = "name", unique = true),
    @Index(name = "idx_plan_active", columnList = "is_active")
})
public class SubscriptionPlan {

    public enum PlanName {
        FREE, BASIC, PRO, ENTERPRISE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    @NotNull
    private PlanName name;

    @Column(name = "display_name_ar", nullable = false, length = 50)
    @NotBlank
    private String displayNameAr;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal monthlyPrice;

    @Column(name = "annual_price", nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal annualPrice;

    @Column(name = "max_shipments_per_month", nullable = false)
    private int maxShipmentsPerMonth;

    @Column(name = "max_webhooks", nullable = false)
    private int maxWebhooks;

    @Column(name = "features", columnDefinition = "TEXT")
    private String features;

    @Column(name = "api_rate_limit", nullable = false)
    private int apiRateLimit = 100;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // Constructors
    public SubscriptionPlan() {}

    public SubscriptionPlan(PlanName name, String displayNameAr, BigDecimal monthlyPrice,
                            BigDecimal annualPrice, int maxShipmentsPerMonth) {
        this.name = name;
        this.displayNameAr = displayNameAr;
        this.monthlyPrice = monthlyPrice;
        this.annualPrice = annualPrice;
        this.maxShipmentsPerMonth = maxShipmentsPerMonth;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlanName getName() { return name; }
    public void setName(PlanName name) { this.name = name; }

    public String getDisplayNameAr() { return displayNameAr; }
    public void setDisplayNameAr(String displayNameAr) { this.displayNameAr = displayNameAr; }

    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(BigDecimal monthlyPrice) { this.monthlyPrice = monthlyPrice; }

    public BigDecimal getAnnualPrice() { return annualPrice; }
    public void setAnnualPrice(BigDecimal annualPrice) { this.annualPrice = annualPrice; }

    public int getMaxShipmentsPerMonth() { return maxShipmentsPerMonth; }
    public void setMaxShipmentsPerMonth(int maxShipmentsPerMonth) { this.maxShipmentsPerMonth = maxShipmentsPerMonth; }

    public int getMaxWebhooks() { return maxWebhooks; }
    public void setMaxWebhooks(int maxWebhooks) { this.maxWebhooks = maxWebhooks; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    public int getApiRateLimit() { return apiRateLimit; }
    public void setApiRateLimit(int apiRateLimit) { this.apiRateLimit = apiRateLimit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionPlan)) return false;
        SubscriptionPlan that = (SubscriptionPlan) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
