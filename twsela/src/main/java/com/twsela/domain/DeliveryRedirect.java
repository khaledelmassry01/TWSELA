package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "delivery_redirects")
public class DeliveryRedirect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "recipient_profile_id")
    private Long recipientProfileId;

    @NotBlank
    @Column(name = "redirect_type", nullable = false, length = 30)
    private String redirectType;

    @Column(name = "new_address_id")
    private Long newAddressId;

    @Column(name = "hold_until_date")
    private LocalDate holdUntilDate;

    @Column(name = "neighbor_name", length = 100)
    private String neighborName;

    @Column(name = "neighbor_phone", length = 20)
    private String neighborPhone;

    @Column(nullable = false, length = 20)
    private String status = "REQUESTED";

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "requested_at")
    private Instant requestedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "processed_by_id")
    private Long processedById;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
        if (requestedAt == null) requestedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getRecipientProfileId() { return recipientProfileId; }
    public void setRecipientProfileId(Long recipientProfileId) { this.recipientProfileId = recipientProfileId; }
    public String getRedirectType() { return redirectType; }
    public void setRedirectType(String redirectType) { this.redirectType = redirectType; }
    public Long getNewAddressId() { return newAddressId; }
    public void setNewAddressId(Long newAddressId) { this.newAddressId = newAddressId; }
    public LocalDate getHoldUntilDate() { return holdUntilDate; }
    public void setHoldUntilDate(LocalDate holdUntilDate) { this.holdUntilDate = holdUntilDate; }
    public String getNeighborName() { return neighborName; }
    public void setNeighborName(String neighborName) { this.neighborName = neighborName; }
    public String getNeighborPhone() { return neighborPhone; }
    public void setNeighborPhone(String neighborPhone) { this.neighborPhone = neighborPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    public Long getProcessedById() { return processedById; }
    public void setProcessedById(Long processedById) { this.processedById = processedById; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
