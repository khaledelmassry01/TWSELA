package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Tracks assignment of vehicles to couriers.
 */
@Entity
@Table(name = "vehicle_assignments", indexes = {
        @Index(name = "idx_va_courier", columnList = "courier_id"),
        @Index(name = "idx_va_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_va_status", columnList = "status")
})
public class VehicleAssignment {

    public enum AssignmentStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private User courier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Column(name = "start_mileage")
    private Integer startMileage;

    @Column(name = "end_mileage")
    private Integer endMileage;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Getters/Setters ─────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }

    public LocalDate getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDate returnedDate) { this.returnedDate = returnedDate; }

    public Integer getStartMileage() { return startMileage; }
    public void setStartMileage(Integer startMileage) { this.startMileage = startMileage; }

    public Integer getEndMileage() { return endMileage; }
    public void setEndMileage(Integer endMileage) { this.endMileage = endMileage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleAssignment that = (VehicleAssignment) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
