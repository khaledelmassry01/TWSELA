package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Tracks fuel consumption for vehicles.
 */
@Entity
@Table(name = "fuel_logs", indexes = {
        @Index(name = "idx_fl_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_fl_date", columnList = "fuel_date")
})
public class FuelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private User courier;

    @Column(name = "fuel_date", nullable = false)
    private LocalDate fuelDate;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal liters;

    @Column(name = "cost_per_liter", nullable = false, precision = 6, scale = 2)
    private BigDecimal costPerLiter;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "mileage_at_fill")
    private Integer mileageAtFill;

    @Column(name = "fuel_station", length = 100)
    private String fuelStation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Getters/Setters ─────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; }

    public LocalDate getFuelDate() { return fuelDate; }
    public void setFuelDate(LocalDate fuelDate) { this.fuelDate = fuelDate; }

    public BigDecimal getLiters() { return liters; }
    public void setLiters(BigDecimal liters) { this.liters = liters; }

    public BigDecimal getCostPerLiter() { return costPerLiter; }
    public void setCostPerLiter(BigDecimal costPerLiter) { this.costPerLiter = costPerLiter; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public Integer getMileageAtFill() { return mileageAtFill; }
    public void setMileageAtFill(Integer mileageAtFill) { this.mileageAtFill = mileageAtFill; }

    public String getFuelStation() { return fuelStation; }
    public void setFuelStation(String fuelStation) { this.fuelStation = fuelStation; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuelLog fuelLog = (FuelLog) o;
        return id != null && Objects.equals(id, fuelLog.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
