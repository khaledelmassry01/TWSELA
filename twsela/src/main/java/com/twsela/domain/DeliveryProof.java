package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Proof of Delivery — stores photo, digital signature, GPS coordinates,
 * and recipient information captured at the moment of delivery.
 */
@Entity
@Table(name = "delivery_proofs", indexes = {
        @Index(name = "idx_dp_shipment", columnList = "shipment_id", unique = true),
        @Index(name = "idx_dp_captured_by", columnList = "captured_by_user_id")
})
public class DeliveryProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "recipient_name", length = 150)
    private String recipientName;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "delivered_at", nullable = false)
    private Instant deliveredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captured_by_user_id", nullable = false)
    private User capturedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ────────────────────────────────────────

    public DeliveryProof() {}

    // ── Getters / Setters ───────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getSignatureUrl() { return signatureUrl; }
    public void setSignatureUrl(String signatureUrl) { this.signatureUrl = signatureUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public User getCapturedBy() { return capturedBy; }
    public void setCapturedBy(User capturedBy) { this.capturedBy = capturedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryProof that = (DeliveryProof) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
