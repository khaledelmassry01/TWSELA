package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Create Payout Request DTO
 */
@Schema(description = "بيانات إنشاء دفعة مالية")
public class CreatePayoutRequest {
    
    @NotNull(message = "معرف المستخدم مطلوب")
    @Schema(description = "معرف المستخدم", example = "1", required = true)
    private Long userId;
    
    @NotBlank(message = "نوع الدفعة مطلوب")
    @Schema(description = "نوع الدفعة", example = "COURIER", allowableValues = {"COURIER", "MERCHANT"}, required = true)
    private String payoutType;
    
    @NotNull(message = "تاريخ البداية مطلوب")
    @Schema(description = "تاريخ بداية الفترة", example = "2024-01-01", required = true)
    private LocalDate startDate;
    
    @NotNull(message = "تاريخ النهاية مطلوب")
    @Schema(description = "تاريخ نهاية الفترة", example = "2024-01-31", required = true)
    private LocalDate endDate;
    
    public CreatePayoutRequest() {}
    
    public CreatePayoutRequest(Long userId, String payoutType, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.payoutType = payoutType;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getPayoutType() {
        return payoutType;
    }
    
    public void setPayoutType(String payoutType) {
        this.payoutType = payoutType;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
