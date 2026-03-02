package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.twsela.web.validation.ValidPassword;

/**
 * Request DTO for changing the current user's password.
 */
public class ChangePasswordRequest {

    @NotBlank(message = "كلمة المرور الحالية مطلوبة")
    private String oldPassword;

    @NotBlank(message = "كلمة المرور الجديدة مطلوبة")
    @ValidPassword
    private String newPassword;

    public ChangePasswordRequest() {}

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
