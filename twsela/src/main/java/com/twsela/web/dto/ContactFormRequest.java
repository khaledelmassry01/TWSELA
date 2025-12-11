package com.twsela.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Contact Form Request DTO
 */
@Schema(description = "بيانات نموذج الاتصال")
public class ContactFormRequest {
    
    @NotBlank(message = "الاسم الأول مطلوب")
    @Size(max = 50, message = "الاسم الأول يجب أن يكون أقل من 50 حرف")
    @Schema(description = "الاسم الأول", example = "أحمد", required = true)
    private String firstName;
    
    @NotBlank(message = "الاسم الأخير مطلوب")
    @Size(max = 50, message = "الاسم الأخير يجب أن يكون أقل من 50 حرف")
    @Schema(description = "الاسم الأخير", example = "محمد", required = true)
    private String lastName;
    
    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "البريد الإلكتروني غير صحيح")
    @Schema(description = "البريد الإلكتروني", example = "ahmed@example.com", required = true)
    private String email;
    
    @NotBlank(message = "الموضوع مطلوب")
    @Size(max = 200, message = "الموضوع يجب أن يكون أقل من 200 حرف")
    @Schema(description = "موضوع الرسالة", example = "استفسار عن الخدمات", required = true)
    private String subject;
    
    @NotBlank(message = "نص الرسالة مطلوب")
    @Size(max = 1000, message = "نص الرسالة يجب أن يكون أقل من 1000 حرف")
    @Schema(description = "نص الرسالة", example = "أريد الاستفسار عن خدمات التوصيل", required = true)
    private String message;
    
    public ContactFormRequest() {}
    
    public ContactFormRequest(String firstName, String lastName, String email, String subject, String message) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
