package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Courier Rating - tracks delivery ratings from recipients
 */
@Entity
@Table(name = "courier_ratings", indexes = {
    @Index(name = "idx_cr_courier", columnList = "courier_id"),
    @Index(name = "idx_cr_shipment", columnList = "shipment_id"),
    @Index(name = "idx_cr_created", columnList = "created_at")
})
public class CourierRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private User courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "rated_by_phone", length = 20)
    private String ratedByPhone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public CourierRating() {}

    public CourierRating(User courier, Shipment shipment, Integer rating) {
        this.courier = courier;
        this.shipment = shipment;
        this.rating = rating;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getRatedByPhone() { return ratedByPhone; }
    public void setRatedByPhone(String ratedByPhone) { this.ratedByPhone = ratedByPhone; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
