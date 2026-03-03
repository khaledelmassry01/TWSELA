package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "satisfaction_surveys")
public class SatisfactionSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id")
    private Long shipmentId;

    @Column(name = "recipient_profile_id")
    private Long recipientProfileId;

    @NotNull
    @Min(1) @Max(5)
    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    @Min(1) @Max(5)
    @Column(name = "delivery_speed_rating")
    private Integer deliverySpeedRating;

    @Min(1) @Max(5)
    @Column(name = "courier_behavior_rating")
    private Integer courierBehaviorRating;

    @Min(1) @Max(5)
    @Column(name = "packaging_rating")
    private Integer packagingRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "would_recommend")
    private Boolean wouldRecommend;

    @Column(name = "feedback_tags", length = 500)
    private String feedbackTags;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (submittedAt == null) submittedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getRecipientProfileId() { return recipientProfileId; }
    public void setRecipientProfileId(Long recipientProfileId) { this.recipientProfileId = recipientProfileId; }
    public Integer getOverallRating() { return overallRating; }
    public void setOverallRating(Integer overallRating) { this.overallRating = overallRating; }
    public Integer getDeliverySpeedRating() { return deliverySpeedRating; }
    public void setDeliverySpeedRating(Integer deliverySpeedRating) { this.deliverySpeedRating = deliverySpeedRating; }
    public Integer getCourierBehaviorRating() { return courierBehaviorRating; }
    public void setCourierBehaviorRating(Integer courierBehaviorRating) { this.courierBehaviorRating = courierBehaviorRating; }
    public Integer getPackagingRating() { return packagingRating; }
    public void setPackagingRating(Integer packagingRating) { this.packagingRating = packagingRating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Boolean getWouldRecommend() { return wouldRecommend; }
    public void setWouldRecommend(Boolean wouldRecommend) { this.wouldRecommend = wouldRecommend; }
    public String getFeedbackTags() { return feedbackTags; }
    public void setFeedbackTags(String feedbackTags) { this.feedbackTags = feedbackTags; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
