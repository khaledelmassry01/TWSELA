package com.twsela.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * طلب بدء جلسة تتبع حية.
 */
public class StartTrackingRequest {

    @NotNull(message = "shipmentId is required")
    private Long shipmentId;

    public StartTrackingRequest() {}

    public StartTrackingRequest(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
}
