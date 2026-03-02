package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import com.twsela.web.validation.ValidPassword;

/**
 * Request DTO for creating a new user (employee, courier, merchant).
 * Replaces UserController.CreateUserRequest inner class.
 */
public class CreateUserRequest {

    @NotBlank(message = "الاسم مطلوب")
    private String name;

    @NotBlank(message = "رقم الهاتف مطلوب")
    private String phone;

    @NotBlank(message = "كلمة المرور مطلوبة")
    @ValidPassword
    private String password;

    @NotBlank(message = "الدور مطلوب")
    private String role;

    private Boolean active;

    public CreateUserRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
