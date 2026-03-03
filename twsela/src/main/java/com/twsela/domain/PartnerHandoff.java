package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_handoffs")
public class PartnerHandoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @NotNull
    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "handoff_date")
    private LocalDateTime handoffDate;

    @NotBlank
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Size(max = 100)
    @Column(name = "partner_tracking_number", length = 100)
    private String partnerTrackingNumber;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); handoffDate = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
    public LocalDateTime getHandoffDate() { return handoffDate; }
    public void setHandoffDate(LocalDateTime handoffDate) { this.handoffDate = handoffDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPartnerTrackingNumber() { return partnerTrackingNumber; }
    public void setPartnerTrackingNumber(String partnerTrackingNumber) { this.partnerTrackingNumber = partnerTrackingNumber; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
