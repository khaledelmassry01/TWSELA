package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Records the scoring breakdown when the smart engine evaluates a courier for a shipment.
 * Serves as an audit trail for assignment decisions.
 */
@Entity
@Table(name = "assignment_scores", indexes = {
    @Index(name = "idx_score_shipment", columnList = "shipment_id"),
    @Index(name = "idx_score_courier", columnList = "courier_id"),
    @Index(name = "idx_score_calculated", columnList = "calculated_at")
})
public class AssignmentScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "courier_id", nullable = false)
    private Long courierId;

    @Column(name = "total_score", nullable = false)
    private double totalScore;

    @Column(name = "distance_score")
    private double distanceScore;

    @Column(name = "load_score")
    private double loadScore;

    @Column(name = "rating_score")
    private double ratingScore;

    @Column(name = "zone_score")
    private double zoneScore;

    @Column(name = "vehicle_score")
    private double vehicleScore;

    @Column(name = "history_score")
    private double historyScore;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    public AssignmentScore() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    public double getDistanceScore() { return distanceScore; }
    public void setDistanceScore(double distanceScore) { this.distanceScore = distanceScore; }
    public double getLoadScore() { return loadScore; }
    public void setLoadScore(double loadScore) { this.loadScore = loadScore; }
    public double getRatingScore() { return ratingScore; }
    public void setRatingScore(double ratingScore) { this.ratingScore = ratingScore; }
    public double getZoneScore() { return zoneScore; }
    public void setZoneScore(double zoneScore) { this.zoneScore = zoneScore; }
    public double getVehicleScore() { return vehicleScore; }
    public void setVehicleScore(double vehicleScore) { this.vehicleScore = vehicleScore; }
    public double getHistoryScore() { return historyScore; }
    public void setHistoryScore(double historyScore) { this.historyScore = historyScore; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentScore that = (AssignmentScore) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
