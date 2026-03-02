package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Stores the optimized delivery route for a courier.
 * Waypoints are stored as a JSON array.
 */
@Entity
@Table(name = "optimized_routes", indexes = {
    @Index(name = "idx_route_courier", columnList = "courier_id"),
    @Index(name = "idx_route_manifest", columnList = "manifest_id")
})
public class OptimizedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "courier_id", nullable = false)
    private Long courierId;

    @Column(name = "manifest_id")
    private Long manifestId;

    /** JSON array: [{shipmentId, lat, lng, order, estimatedArrival}] */
    @Column(name = "waypoints", columnDefinition = "TEXT")
    private String waypoints;

    @Column(name = "total_distance_km")
    private double totalDistanceKm;

    @Column(name = "estimated_duration_minutes")
    private int estimatedDurationMinutes;

    @Column(name = "optimized_at", nullable = false)
    private Instant optimizedAt = Instant.now();

    public OptimizedRoute() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }
    public Long getManifestId() { return manifestId; }
    public void setManifestId(Long manifestId) { this.manifestId = manifestId; }
    public String getWaypoints() { return waypoints; }
    public void setWaypoints(String waypoints) { this.waypoints = waypoints; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    public Instant getOptimizedAt() { return optimizedAt; }
    public void setOptimizedAt(Instant optimizedAt) { this.optimizedAt = optimizedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptimizedRoute that = (OptimizedRoute) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
