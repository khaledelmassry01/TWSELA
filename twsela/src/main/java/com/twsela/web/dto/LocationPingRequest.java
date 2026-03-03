package com.twsela.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * طلب إرسال نقطة موقع GPS من المندوب.
 */
public class LocationPingRequest {

    @NotNull(message = "sessionId is required")
    private Long sessionId;

    @NotNull(message = "lat is required")
    private Double lat;

    @NotNull(message = "lng is required")
    private Double lng;

    private Float accuracy;
    private Float speed;
    private Float heading;
    private Integer batteryLevel;

    public LocationPingRequest() {}

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Float getAccuracy() { return accuracy; }
    public void setAccuracy(Float accuracy) { this.accuracy = accuracy; }

    public Float getSpeed() { return speed; }
    public void setSpeed(Float speed) { this.speed = speed; }

    public Float getHeading() { return heading; }
    public void setHeading(Float heading) { this.heading = heading; }

    public Integer getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }
}
