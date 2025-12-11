package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "merchant_service_feedback")
public class MerchantServiceFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private User courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_shipment_id")
    private Shipment relatedShipment;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 stars

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // Constructors
    public MerchantServiceFeedback() {}

    public MerchantServiceFeedback(User merchant, User courier, Shipment relatedShipment, Integer rating, String comment) {
        this.merchant = merchant;
        this.courier = courier;
        this.relatedShipment = relatedShipment;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }

    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; }

    public Shipment getRelatedShipment() { return relatedShipment; }
    public void setRelatedShipment(Shipment relatedShipment) { this.relatedShipment = relatedShipment; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "MerchantServiceFeedback{" +
                "id=" + id +
                ", merchant=" + (merchant != null ? merchant.getId() : null) +
                ", courier=" + (courier != null ? courier.getId() : null) +
                ", relatedShipment=" + (relatedShipment != null ? relatedShipment.getId() : null) +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
