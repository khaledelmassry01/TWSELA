package com.twsela.web.dto;

import java.time.Instant;
import java.util.List;

public class ManifestResponseDTO {
    private Long id;
    private String courierName;
    private String status;
    private int shipmentCount;
    private Instant createdAt;
    private List<String> trackingNumbers;

    public ManifestResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getShipmentCount() { return shipmentCount; }
    public void setShipmentCount(int shipmentCount) { this.shipmentCount = shipmentCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<String> getTrackingNumbers() { return trackingNumbers; }
    public void setTrackingNumbers(List<String> trackingNumbers) { this.trackingNumbers = trackingNumbers; }
}
