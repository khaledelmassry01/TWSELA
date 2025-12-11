package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Location Update Request DTO
 */
@Schema(description = "بيانات تحديث موقع عامل التوصيل")
public class LocationUpdateRequest {
    
    @NotNull(message = "خط العرض مطلوب")
    @Schema(description = "خط العرض", example = "30.0444", required = true)
    private Double latitude;
    
    @NotNull(message = "خط الطول مطلوب")
    @Schema(description = "خط الطول", example = "31.2357", required = true)
    private Double longitude;
    
    public LocationUpdateRequest() {}
    
    public LocationUpdateRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
