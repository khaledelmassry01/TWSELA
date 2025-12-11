package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Assign Shipments to Manifest Request DTO
 */
@Schema(description = "بيانات تعيين شحنات للمانيفست")
public class AssignShipmentsRequest {
    
    @NotEmpty(message = "أرقام التتبع مطلوبة")
    @Schema(description = "قائمة أرقام التتبع", example = "[\"TS123456789\", \"TS987654321\"]", required = true)
    private List<String> trackingNumbers;
    
    public AssignShipmentsRequest() {}
    
    public AssignShipmentsRequest(List<String> trackingNumbers) {
        this.trackingNumbers = trackingNumbers;
    }
    
    public List<String> getTrackingNumbers() {
        return trackingNumbers;
    }
    
    public void setTrackingNumbers(List<String> trackingNumbers) {
        this.trackingNumbers = trackingNumbers;
    }
}
