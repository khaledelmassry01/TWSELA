package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Daily snapshot of key performance indicators.
 * Pre-computed nightly to avoid expensive real-time queries.
 */
@Entity
@Table(name = "kpi_snapshots",
    uniqueConstraints = @UniqueConstraint(name = "uk_kpi_snapshot_date", columnNames = "snapshot_date"),
    indexes = {
        @Index(name = "idx_kpi_snapshot_date", columnList = "snapshot_date")
    }
)
public class KPISnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_shipments")
    private int totalShipments;

    @Column(name = "delivered_shipments")
    private int deliveredShipments;

    @Column(name = "returned_shipments")
    private int returnedShipments;

    @Column(name = "first_attempt_rate")
    private double firstAttemptRate;

    @Column(name = "avg_delivery_hours")
    private double avgDeliveryHours;

    @Column(name = "active_couriers")
    private int activeCouriers;

    @Column(name = "active_merchants")
    private int activeMerchants;

    @Column(name = "new_merchants")
    private int newMerchants;

    @Column(name = "sla_compliance_rate")
    private double slaComplianceRate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public KPISnapshot() {}

    public KPISnapshot(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getTotalShipments() { return totalShipments; }
    public void setTotalShipments(int totalShipments) { this.totalShipments = totalShipments; }

    public int getDeliveredShipments() { return deliveredShipments; }
    public void setDeliveredShipments(int deliveredShipments) { this.deliveredShipments = deliveredShipments; }

    public int getReturnedShipments() { return returnedShipments; }
    public void setReturnedShipments(int returnedShipments) { this.returnedShipments = returnedShipments; }

    public double getFirstAttemptRate() { return firstAttemptRate; }
    public void setFirstAttemptRate(double firstAttemptRate) { this.firstAttemptRate = firstAttemptRate; }

    public double getAvgDeliveryHours() { return avgDeliveryHours; }
    public void setAvgDeliveryHours(double avgDeliveryHours) { this.avgDeliveryHours = avgDeliveryHours; }

    public int getActiveCouriers() { return activeCouriers; }
    public void setActiveCouriers(int activeCouriers) { this.activeCouriers = activeCouriers; }

    public int getActiveMerchants() { return activeMerchants; }
    public void setActiveMerchants(int activeMerchants) { this.activeMerchants = activeMerchants; }

    public int getNewMerchants() { return newMerchants; }
    public void setNewMerchants(int newMerchants) { this.newMerchants = newMerchants; }

    public double getSlaComplianceRate() { return slaComplianceRate; }
    public void setSlaComplianceRate(double slaComplianceRate) { this.slaComplianceRate = slaComplianceRate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
