package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "battery_optimization_configs")
public class BatteryOptimizationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer batteryThreshold = 20;

    @Column(nullable = false)
    private Integer locationIntervalSeconds = 30;

    @Column(nullable = false)
    private Integer pingIntervalSeconds = 60;

    @Column(nullable = false)
    private Integer syncIntervalSeconds = 300;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getBatteryThreshold() { return batteryThreshold; }
    public void setBatteryThreshold(Integer batteryThreshold) { this.batteryThreshold = batteryThreshold; }
    public Integer getLocationIntervalSeconds() { return locationIntervalSeconds; }
    public void setLocationIntervalSeconds(Integer locationIntervalSeconds) { this.locationIntervalSeconds = locationIntervalSeconds; }
    public Integer getPingIntervalSeconds() { return pingIntervalSeconds; }
    public void setPingIntervalSeconds(Integer pingIntervalSeconds) { this.pingIntervalSeconds = pingIntervalSeconds; }
    public Integer getSyncIntervalSeconds() { return syncIntervalSeconds; }
    public void setSyncIntervalSeconds(Integer syncIntervalSeconds) { this.syncIntervalSeconds = syncIntervalSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
