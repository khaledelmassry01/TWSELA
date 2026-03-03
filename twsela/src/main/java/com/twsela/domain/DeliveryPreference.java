package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "delivery_preferences")
public class DeliveryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_profile_id", nullable = false, unique = true)
    private Long recipientProfileId;

    @Column(name = "prefer_safe_place", nullable = false)
    private Boolean preferSafePlace = false;

    @Column(name = "safe_place_description", length = 255)
    private String safePlaceDescription;

    @Column(name = "allow_neighbor_delivery", nullable = false)
    private Boolean allowNeighborDelivery = false;

    @Column(name = "require_signature", nullable = false)
    private Boolean requireSignature = false;

    @Column(name = "require_otp", nullable = false)
    private Boolean requireOtp = false;

    @Column(name = "prefer_contactless", nullable = false)
    private Boolean preferContactless = false;

    @Column(name = "sms_before_delivery", nullable = false)
    private Boolean smsBeforeDelivery = true;

    @Column(name = "sms_minutes_before", nullable = false)
    private Integer smsMinutesBefore = 30;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecipientProfileId() { return recipientProfileId; }
    public void setRecipientProfileId(Long recipientProfileId) { this.recipientProfileId = recipientProfileId; }
    public Boolean getPreferSafePlace() { return preferSafePlace; }
    public void setPreferSafePlace(Boolean preferSafePlace) { this.preferSafePlace = preferSafePlace; }
    public String getSafePlaceDescription() { return safePlaceDescription; }
    public void setSafePlaceDescription(String safePlaceDescription) { this.safePlaceDescription = safePlaceDescription; }
    public Boolean getAllowNeighborDelivery() { return allowNeighborDelivery; }
    public void setAllowNeighborDelivery(Boolean allowNeighborDelivery) { this.allowNeighborDelivery = allowNeighborDelivery; }
    public Boolean getRequireSignature() { return requireSignature; }
    public void setRequireSignature(Boolean requireSignature) { this.requireSignature = requireSignature; }
    public Boolean getRequireOtp() { return requireOtp; }
    public void setRequireOtp(Boolean requireOtp) { this.requireOtp = requireOtp; }
    public Boolean getPreferContactless() { return preferContactless; }
    public void setPreferContactless(Boolean preferContactless) { this.preferContactless = preferContactless; }
    public Boolean getSmsBeforeDelivery() { return smsBeforeDelivery; }
    public void setSmsBeforeDelivery(Boolean smsBeforeDelivery) { this.smsBeforeDelivery = smsBeforeDelivery; }
    public Integer getSmsMinutesBefore() { return smsMinutesBefore; }
    public void setSmsMinutesBefore(Integer smsMinutesBefore) { this.smsMinutesBefore = smsMinutesBefore; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
