package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Shipment Creation Request DTO
 */
@Schema(description = "بيانات إنشاء شحنة جديدة")
public class CreateShipmentRequest {
    
    @NotBlank(message = "اسم المستلم مطلوب")
    @Size(max = 100, message = "اسم المستلم يجب أن يكون أقل من 100 حرف")
    @Schema(description = "اسم المستلم", example = "أحمد محمد", required = true)
    private String recipientName;
    
    @NotBlank(message = "رقم هاتف المستلم مطلوب")
    @Schema(description = "رقم هاتف المستلم", example = "+201234567890", required = true)
    private String recipientPhone;
    
    @Schema(description = "رقم هاتف بديل للمستلم", example = "+201234567891")
    private String alternatePhone;
    
    @NotBlank(message = "عنوان المستلم مطلوب")
    @Size(max = 500, message = "العنوان يجب أن يكون أقل من 500 حرف")
    @Schema(description = "عنوان المستلم", example = "شارع التحرير، القاهرة", required = true)
    private String recipientAddress;
    
    @NotBlank(message = "وصف الطرد مطلوب")
    @Size(max = 200, message = "وصف الطرد يجب أن يكون أقل من 200 حرف")
    @Schema(description = "وصف الطرد", example = "ملابس", required = true)
    private String packageDescription;
    
    @NotNull(message = "وزن الطرد مطلوب")
    @Positive(message = "وزن الطرد يجب أن يكون أكبر من صفر")
    @Schema(description = "وزن الطرد بالكيلو", example = "2.5", required = true)
    private BigDecimal packageWeight;
    
    @Schema(description = "قيمة الطرد", example = "500.00")
    private BigDecimal itemValue;
    
    @Schema(description = "مبلغ الدفع عند الاستلام", example = "500.00")
    private BigDecimal codAmount;
    
    @NotNull(message = "معرف المنطقة مطلوب")
    @Schema(description = "معرف المنطقة", example = "1", required = true)
    private Long zoneId;
    
    @NotBlank(message = "أولوية الشحن مطلوبة")
    @Schema(description = "أولوية الشحن", example = "STANDARD", allowableValues = {"EXPRESS", "STANDARD", "ECONOMY"}, required = true)
    private String priority;
    
    @NotBlank(message = "من يدفع رسوم الشحن مطلوب")
    @Schema(description = "من يدفع رسوم الشحن", example = "MERCHANT", allowableValues = {"MERCHANT", "RECIPIENT"}, required = true)
    private String shippingFeePaidBy;
    
    @Schema(description = "تعليمات خاصة", example = "اتصل قبل التوصيل")
    private String specialInstructions;
    
    public CreateShipmentRequest() {}
    
    // Getters and Setters
    public String getRecipientName() {
        return recipientName;
    }
    
    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
    
    public String getRecipientPhone() {
        return recipientPhone;
    }
    
    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }
    
    public String getAlternatePhone() {
        return alternatePhone;
    }
    
    public void setAlternatePhone(String alternatePhone) {
        this.alternatePhone = alternatePhone;
    }
    
    public String getRecipientAddress() {
        return recipientAddress;
    }
    
    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }
    
    public String getPackageDescription() {
        return packageDescription;
    }
    
    public void setPackageDescription(String packageDescription) {
        this.packageDescription = packageDescription;
    }
    
    public BigDecimal getPackageWeight() {
        return packageWeight;
    }
    
    public void setPackageWeight(BigDecimal packageWeight) {
        this.packageWeight = packageWeight;
    }
    
    public BigDecimal getItemValue() {
        return itemValue;
    }
    
    public void setItemValue(BigDecimal itemValue) {
        this.itemValue = itemValue;
    }
    
    public BigDecimal getCodAmount() {
        return codAmount;
    }
    
    public void setCodAmount(BigDecimal codAmount) {
        this.codAmount = codAmount;
    }
    
    public Long getZoneId() {
        return zoneId;
    }
    
    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getShippingFeePaidBy() {
        return shippingFeePaidBy;
    }
    
    public void setShippingFeePaidBy(String shippingFeePaidBy) {
        this.shippingFeePaidBy = shippingFeePaidBy;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
}
