package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * نقطة موقع GPS تُرسل من تطبيق المندوب أثناء جلسة التتبع.
 */
@Entity
@Table(name = "location_pings", indexes = {
        @Index(name = "idx_lp_session", columnList = "tracking_session_id"),
        @Index(name = "idx_lp_timestamp", columnList = "timestamp")
})
public class LocationPing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_session_id", nullable = false)
    private TrackingSession trackingSession;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "accuracy")
    private Float accuracy;

    @Column(name = "speed")
    private Float speed;

    @Column(name = "heading")
    private Float heading;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp = Instant.now();

    // ── Constructors ──
    public LocationPing() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TrackingSession getTrackingSession() { return trackingSession; }
    public void setTrackingSession(TrackingSession trackingSession) { this.trackingSession = trackingSession; }

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

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationPing that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
