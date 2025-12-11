package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Return Request DTO
 */
@Schema(description = "بيانات طلب إرجاع شحنة")
public class ReturnRequest {
    
    @NotBlank(message = "سبب الإرجاع مطلوب")
    @Schema(description = "سبب الإرجاع", example = "المستلم غير متاح", required = true)
    private String reason;
    
    public ReturnRequest() {}
    
    public ReturnRequest(String reason) {
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
