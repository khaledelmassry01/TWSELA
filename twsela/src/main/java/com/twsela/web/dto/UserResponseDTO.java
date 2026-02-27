package com.twsela.web.dto;

public class UserResponseDTO {
    private Long id;
    private String name;
    private String phone;
    private String role;
    private String status;
    private boolean active;

    public UserResponseDTO() {}
    public UserResponseDTO(Long id, String name, String phone, String role, String status, boolean active) {
        this.id = id; this.name = name; this.phone = phone; this.role = role; this.status = status; this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
