package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_phone", columnNames = {"phone"})
    },
    indexes = {
        @Index(name = "idx_user_role_status", columnList = "role_id, status_id"),
        @Index(name = "idx_user_phone_active", columnList = "phone, is_deleted"),
        @Index(name = "idx_user_created_at", columnList = "created_at")
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private UserStatus status;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF\\s]+$", message = "Name must contain only letters and spaces")
    private String name;

    @Column(nullable = false)
    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp default current_timestamp")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt = Instant.now();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private MerchantDetails merchantDetails;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private CourierDetails courierDetails;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Shipment> createdShipments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Payout> payouts = new HashSet<>();

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public boolean isActive() { 
        return status != null && "ACTIVE".equals(status.getName()) && !isDeleted; 
    }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public MerchantDetails getMerchantDetails() { return merchantDetails; }
    public void setMerchantDetails(MerchantDetails merchantDetails) { this.merchantDetails = merchantDetails; }
    public CourierDetails getCourierDetails() { return courierDetails; }
    public void setCourierDetails(CourierDetails courierDetails) { this.courierDetails = courierDetails; }
    public Set<Shipment> getCreatedShipments() { return createdShipments; }
    public void setCreatedShipments(Set<Shipment> createdShipments) { this.createdShipments = createdShipments; }
    public Set<Payout> getPayouts() { return payouts; }
    public void setPayouts(Set<Payout> payouts) { this.payouts = payouts; }
    
    // Additional methods for testing
    public void setActive(boolean active) {
        // This method is for testing purposes only
        // In production, status should be managed through proper status management
    }
    
    public void setDeleted(boolean deleted) {
        // This method is for testing purposes only
        // In production, deletion should be managed through soft delete mechanism
    }
}