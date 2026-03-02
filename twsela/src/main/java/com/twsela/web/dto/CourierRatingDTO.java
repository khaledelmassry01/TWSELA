package com.twsela.web.dto;

import java.time.Instant;

/**
 * DTO for courier rating responses
 */
public class CourierRatingDTO {

    private Long id;
    private Long courierId;
    private String courierName;
    private Long shipmentId;
    private String trackingNumber;
    private Integer rating;
    private String comment;
    private Instant createdAt;

    public CourierRatingDTO() {}

    public CourierRatingDTO(Long id, Long courierId, String courierName,
                            Long shipmentId, String trackingNumber,
                            Integer rating, String comment, Instant createdAt) {
        this.id = id;
        this.courierId = courierId;
        this.courierName = courierName;
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
