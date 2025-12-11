package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "courier_zones")
public class CourierZone {

    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "courier_id")
        private Long courierId;
        @Column(name = "zone_id")
        private Long zoneId;

        public Id() {}
        public Id(Long courierId, Long zoneId) { this.courierId = courierId; this.zoneId = zoneId; }
        public Long getCourierId() { return courierId; }
        public void setCourierId(Long courierId) { this.courierId = courierId; }
        public Long getZoneId() { return zoneId; }
        public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equals(courierId, id.courierId) && Objects.equals(zoneId, id.zoneId);
        }
        @Override public int hashCode() { return Objects.hash(courierId, zoneId); }
    }

    @EmbeddedId
    private Id id = new Id();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courierId")
    @JoinColumn(name = "courier_id")
    @JsonIgnore
    private User courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("zoneId")
    @JoinColumn(name = "zone_id")
    @JsonIgnore
    private Zone zone;

    // Constructors
    public CourierZone() {}

    public CourierZone(Long courierId, Long zoneId) {
        this.id = new Id(courierId, zoneId);
    }

    public CourierZone(User courier, Zone zone) {
        this.courier = courier;
        this.zone = zone;
        this.id = new Id(courier.getId(), zone.getId());
    }

    // Getters and Setters
    public Id getId() { return id; }
    public void setId(Id id) { this.id = id; }
    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; }
    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    @Override
    public String toString() {
        return "CourierZone{" +
                "courierId=" + id.getCourierId() +
                ", zoneId=" + id.getZoneId() +
                ", courier=" + (courier != null ? courier.getName() : "null") +
                ", zone=" + (zone != null ? zone.getName() : "null") +
                '}';
    }
}