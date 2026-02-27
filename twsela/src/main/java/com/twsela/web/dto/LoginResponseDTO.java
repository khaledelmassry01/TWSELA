package com.twsela.web.dto;

/**
 * DTO for login response — exposes only safe user fields.
 * Never expose password hash, internal IDs, or sensitive data.
 */
public class LoginResponseDTO {
    
    private Long id;
    private String name;
    private String phone;
    private String role;
    private String status;
    
    public LoginResponseDTO() {}
    
    public LoginResponseDTO(Long id, String name, String phone, String role, String status) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }
    
    // Getters and Setters
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
}
