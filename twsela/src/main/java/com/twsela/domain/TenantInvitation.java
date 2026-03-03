package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * دعوة مستخدم للانضمام لمستأجر.
 */
@Entity
@Table(name = "tenant_invitations", indexes = {
        @Index(name = "idx_tenant_inv_token", columnList = "token", unique = true),
        @Index(name = "idx_tenant_inv_tenant", columnList = "tenant_id"),
        @Index(name = "idx_tenant_inv_status", columnList = "status")
})
public class TenantInvitation {

    public enum InvitationStatus {
        PENDING, ACCEPTED, EXPIRED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantUser.TenantRole role = TenantUser.TenantRole.TENANT_USER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (token == null) token = UUID.randomUUID().toString();
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public TenantUser.TenantRole getRole() { return role; }
    public void setRole(TenantUser.TenantRole role) { this.role = role; }

    public User getInvitedBy() { return invitedBy; }
    public void setInvitedBy(User invitedBy) { this.invitedBy = invitedBy; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantInvitation that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
