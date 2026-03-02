package com.twsela.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for courier location data.
 */
public class LocationDTO {

    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant timestamp;

    public LocationDTO() {}

    public LocationDTO(BigDecimal latitude, BigDecimal longitude, Instant timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
