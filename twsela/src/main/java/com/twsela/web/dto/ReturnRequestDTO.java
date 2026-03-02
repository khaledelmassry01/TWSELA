package com.twsela.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a return request.
 */
public class ReturnRequestDTO {

    @NotNull(message = "معرف الشحنة مطلوب")
    private Long shipmentId;

    @NotNull(message = "سبب الإرجاع مطلوب")
    @Size(min = 5, max = 500, message = "سبب الإرجاع يجب أن يكون بين 5 و 500 حرف")
    private String reason;

    private String notes;

    public ReturnRequestDTO() {}

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
