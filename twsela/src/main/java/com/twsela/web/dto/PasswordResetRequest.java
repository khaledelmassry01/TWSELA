package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Password Reset Request DTO
 */
@Schema(description = "بيانات إعادة تعيين كلمة المرور")
public class PasswordResetRequest {
    
    @NotBlank(message = "رقم الهاتف مطلوب")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "رقم الهاتف غير صحيح")
    @Schema(description = "رقم الهاتف", example = "+201234567890", required = true)
    private String phone;
    
    @Schema(description = "رمز التحقق", example = "123456")
    private String otp;
    
    @Schema(description = "كلمة المرور الجديدة", example = "newpassword123")
    private String newPassword;
    
    @Schema(description = "تأكيد كلمة المرور الجديدة", example = "newpassword123")
    private String confirmPassword;
    
    public PasswordResetRequest() {}
    
    public PasswordResetRequest(String phone, String otp, String newPassword, String confirmPassword) {
        this.phone = phone;
        this.otp = otp;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getOtp() {
        return otp;
    }
    
    public void setOtp(String otp) {
        this.otp = otp;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}