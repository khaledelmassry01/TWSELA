package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Create Manifest Request DTO
 */
@Schema(description = "بيانات إنشاء مانيفست جديد")
public class CreateManifestRequest {
    
    @NotNull(message = "معرف عامل التوصيل مطلوب")
    @Schema(description = "معرف عامل التوصيل", example = "1", required = true)
    private Long courierId;
    
    public CreateManifestRequest() {}
    
    public CreateManifestRequest(Long courierId) {
        this.courierId = courierId;
    }
    
    public Long getCourierId() {
        return courierId;
    }
    
    public void setCourierId(Long courierId) {
        this.courierId = courierId;
    }
}