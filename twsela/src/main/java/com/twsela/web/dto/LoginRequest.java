package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Login Request DTO
 */
@Schema(description = "بيانات تسجيل الدخول")
public class LoginRequest {
    
    @NotBlank(message = "رقم الهاتف مطلوب")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "رقم الهاتف غير صحيح")
    @Schema(description = "رقم الهاتف", example = "+201234567890", required = true)
    private String phone;
    
    @NotBlank(message = "كلمة المرور مطلوبة")
    @Size(min = 6, message = "كلمة المرور يجب أن تكون 6 أحرف على الأقل")
    @Schema(description = "كلمة المرور", example = "123456", required = true)
    private String password;
    
    public LoginRequest() {}
    
    public LoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}