package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Custom pricing rule within a contract — overrides default delivery pricing.
 */
@Entity
@Table(name = "custom_pricing_rules")
public class CustomPricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_from_id")
    private Zone zoneFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_to_id")
    private Zone zoneTo;

    @Column(name = "shipment_type")
    private String shipmentType;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "per_kg_price", precision = 10, scale = 2)
    private BigDecimal perKgPrice;

    @Column(name = "cod_fee_percent", precision = 5, scale = 2)
    private BigDecimal codFeePercent;

    @Column(name = "minimum_charge", precision = 10, scale = 2)
    private BigDecimal minimumCharge;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "min_monthly_shipments")
    private int minMonthlyShipments;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public CustomPricingRule() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public Zone getZoneFrom() { return zoneFrom; }
    public void setZoneFrom(Zone zoneFrom) { this.zoneFrom = zoneFrom; }

    public Zone getZoneTo() { return zoneTo; }
    public void setZoneTo(Zone zoneTo) { this.zoneTo = zoneTo; }

    public String getShipmentType() { return shipmentType; }
    public void setShipmentType(String shipmentType) { this.shipmentType = shipmentType; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getPerKgPrice() { return perKgPrice; }
    public void setPerKgPrice(BigDecimal perKgPrice) { this.perKgPrice = perKgPrice; }

    public BigDecimal getCodFeePercent() { return codFeePercent; }
    public void setCodFeePercent(BigDecimal codFeePercent) { this.codFeePercent = codFeePercent; }

    public BigDecimal getMinimumCharge() { return minimumCharge; }
    public void setMinimumCharge(BigDecimal minimumCharge) { this.minimumCharge = minimumCharge; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public int getMinMonthlyShipments() { return minMonthlyShipments; }
    public void setMinMonthlyShipments(int minMonthlyShipments) { this.minMonthlyShipments = minMonthlyShipments; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
