package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "shipments", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tracking_number", columnNames = {"tracking_number"})
    },
    indexes = {
        @Index(name = "idx_ship_status_date", columnList = "status_id, created_at"),
        @Index(name = "idx_ship_payout", columnList = "payout_id"),
        @Index(name = "idx_ship_merchant_created", columnList = "merchant_id, created_at"),
        @Index(name = "idx_ship_manifest", columnList = "manifest_id"),
        @Index(name = "idx_ship_recipient", columnList = "recipient_detail_id"),
        @Index(name = "idx_ship_zone", columnList = "zone_id")
    }
)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id")
    private ShipmentManifest manifest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private ShipmentStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_detail_id", nullable = false)
    private RecipientDetails recipientDetails;

    @Column(name = "delivery_latitude", precision = 10, scale = 8)
    private BigDecimal deliveryLatitude;

    @Column(name = "delivery_longitude", precision = 10, scale = 8)
    private BigDecimal deliveryLongitude;

    @Column(name = "item_value", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Item value is required")
    @DecimalMin(value = "0.0", message = "Item value must be positive")
    private BigDecimal itemValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_fee_paid_by", nullable = false)
    private ShippingFeePaidBy shippingFeePaidBy = ShippingFeePaidBy.MERCHANT;

    @Column(name = "cod_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "COD amount is required")
    @DecimalMin(value = "0.0", message = "COD amount must be positive")
    private BigDecimal codAmount = BigDecimal.ZERO;

    @Column(name = "delivery_fee", nullable = false, precision = 8, scale = 2)
    @NotNull(message = "Delivery fee is required")
    @DecimalMin(value = "0.0", message = "Delivery fee must be positive")
    private BigDecimal deliveryFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType = SourceType.MERCHANT;

    @Column(name = "external_tracking_number", length = 50)
    private String externalTrackingNumber;

    @Column(name = "cash_reconciled", nullable = false)
    private Boolean cashReconciled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_id")
    private Payout payout;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private Instant updatedAt = Instant.now();

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "pod_type")
    private PodType podType;

    @Column(name = "pod_data", length = 255)
    private String podData;

    @Column(name = "recipient_notes", columnDefinition = "TEXT")
    private String recipientNotes;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ShipmentStatusHistory> statusHistory = new java.util.HashSet<>();

    @OneToOne(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private ShipmentPackageDetails packageDetails;

    public enum PodType {
        OTP, PHOTO, SIGNATURE
    }

    public enum ShippingFeePaidBy {
        MERCHANT, RECIPIENT, PREPAID
    }

    public enum SourceType {
        MERCHANT("MERCHANT"), 
        THIRD_PARTY_LOGISTICS_PARTNER("3PL_PARTNER");
        
        private final String value;
        
        SourceType(String value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }
        
        @com.fasterxml.jackson.annotation.JsonCreator
        public static SourceType fromValue(String value) {
            for (SourceType type : SourceType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown SourceType: " + value);
        }
    }

    // Constructors
    public Shipment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }
    public ShipmentManifest getManifest() { return manifest; }
    public void setManifest(ShipmentManifest manifest) { this.manifest = manifest; }
    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public RecipientDetails getRecipientDetails() { return recipientDetails; }
    public void setRecipientDetails(RecipientDetails recipientDetails) { this.recipientDetails = recipientDetails; }
    public BigDecimal getDeliveryLatitude() { return deliveryLatitude; }
    public void setDeliveryLatitude(BigDecimal deliveryLatitude) { this.deliveryLatitude = deliveryLatitude; }
    public BigDecimal getDeliveryLongitude() { return deliveryLongitude; }
    public void setDeliveryLongitude(BigDecimal deliveryLongitude) { this.deliveryLongitude = deliveryLongitude; }
    public BigDecimal getItemValue() { return itemValue; }
    public void setItemValue(BigDecimal itemValue) { this.itemValue = itemValue; }
    public ShippingFeePaidBy getShippingFeePaidBy() { return shippingFeePaidBy; }
    public void setShippingFeePaidBy(ShippingFeePaidBy shippingFeePaidBy) { this.shippingFeePaidBy = shippingFeePaidBy; }
    public BigDecimal getCodAmount() { return codAmount; }
    public void setCodAmount(BigDecimal codAmount) { this.codAmount = codAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public String getExternalTrackingNumber() { return externalTrackingNumber; }
    public void setExternalTrackingNumber(String externalTrackingNumber) { this.externalTrackingNumber = externalTrackingNumber; }
    public Boolean getCashReconciled() { return cashReconciled; }
    public void setCashReconciled(Boolean cashReconciled) { this.cashReconciled = cashReconciled; }
    public Payout getPayout() { return payout; }
    public void setPayout(Payout payout) { this.payout = payout; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
    public PodType getPodType() { return podType; }
    public void setPodType(PodType podType) { this.podType = podType; }
    public String getPodData() { return podData; }
    public void setPodData(String podData) { this.podData = podData; }
    public String getRecipientNotes() { return recipientNotes; }
    public void setRecipientNotes(String recipientNotes) { this.recipientNotes = recipientNotes; }
    public Set<ShipmentStatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(Set<ShipmentStatusHistory> statusHistory) { this.statusHistory = statusHistory; }
    public ShipmentPackageDetails getPackageDetails() { return packageDetails; }
    public void setPackageDetails(ShipmentPackageDetails packageDetails) { this.packageDetails = packageDetails; }

    // Helper methods
    public User getCourier() {
        return manifest != null ? manifest.getCourier() : null;
    }

    public String getRecipientName() {
        return recipientDetails != null ? recipientDetails.getName() : null;
    }

    public String getRecipientPhone() {
        return recipientDetails != null ? recipientDetails.getPhone() : null;
    }

    public String getRecipientAddress() {
        return recipientDetails != null ? recipientDetails.getAddress() : null;
    }

    public void setRecipientName(String name) {
        if (recipientDetails != null) {
            recipientDetails.setName(name);
        }
    }

    public void setRecipientPhone(String phone) {
        if (recipientDetails != null) {
            recipientDetails.setPhone(phone);
        }
    }

    public void setRecipientAddress(String address) {
        if (recipientDetails != null) {
            recipientDetails.setAddress(address);
        }
    }

    public void setCourier(User courier) {
        // This method is kept for backward compatibility
        // In the new structure, courier is accessed through manifest
    }

    public boolean isCashReconciled() {
        return cashReconciled != null ? cashReconciled : false;
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "id=" + id +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", status=" + (status != null ? status.getName() : "null") +
                ", merchant=" + (merchant != null ? merchant.getName() : "null") +
                '}';
    }
}