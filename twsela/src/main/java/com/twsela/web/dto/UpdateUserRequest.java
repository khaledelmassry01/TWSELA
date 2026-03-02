package com.twsela.web.dto;

/**
 * Request DTO for updating an existing user.
 * Replaces UserController.UpdateUserRequest inner class.
 * All fields are optional — only provided fields are updated.
 */
public class UpdateUserRequest {

    private String name;
    private String phone;
    private Boolean active;
    private String password;

    public UpdateUserRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
