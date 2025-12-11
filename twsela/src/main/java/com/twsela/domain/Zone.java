package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "zones")
public class Zone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "center_latitude", precision = 10, scale = 8)
    private BigDecimal centerLatitude;
    
    @Column(name = "center_longitude", precision = 10, scale = 8)
    private BigDecimal centerLongitude;
    
    @Column(name = "default_fee", precision = 8, scale = 2)
    private BigDecimal defaultFee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ZoneStatus status = ZoneStatus.ZONE_ACTIVE;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public Zone() {}
    
    public Zone(String name, String description, BigDecimal centerLatitude, BigDecimal centerLongitude) {
        this.name = name;
        this.description = description;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.status = ZoneStatus.ZONE_ACTIVE;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getCenterLatitude() {
        return centerLatitude;
    }
    
    public void setCenterLatitude(BigDecimal centerLatitude) {
        this.centerLatitude = centerLatitude;
    }
    
    public BigDecimal getCenterLongitude() {
        return centerLongitude;
    }
    
    public void setCenterLongitude(BigDecimal centerLongitude) {
        this.centerLongitude = centerLongitude;
    }
    
    public BigDecimal getDefaultFee() {
        return defaultFee;
    }
    
    public void setDefaultFee(BigDecimal defaultFee) {
        this.defaultFee = defaultFee;
    }
    
    
    public ZoneStatus getStatus() {
        return status;
    }
    
    public void setStatus(ZoneStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}