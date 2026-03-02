package com.twsela.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for return shipment response.
 */
public class ReturnResponseDTO {

    private Long id;
    private Long originalShipmentId;
    private String originalTrackingNumber;
    private String status;
    private String reason;
    private String notes;
    private BigDecimal returnFee;
    private String assignedCourierName;
    private Instant createdAt;
    private Instant approvedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private String createdBy;

    public ReturnResponseDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOriginalShipmentId() { return originalShipmentId; }
    public void setOriginalShipmentId(Long originalShipmentId) { this.originalShipmentId = originalShipmentId; }

    public String getOriginalTrackingNumber() { return originalTrackingNumber; }
    public void setOriginalTrackingNumber(String originalTrackingNumber) { this.originalTrackingNumber = originalTrackingNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getReturnFee() { return returnFee; }
    public void setReturnFee(BigDecimal returnFee) { this.returnFee = returnFee; }

    public String getAssignedCourierName() { return assignedCourierName; }
    public void setAssignedCourierName(String assignedCourierName) { this.assignedCourierName = assignedCourierName; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public Instant getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(Instant pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
