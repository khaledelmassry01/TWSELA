package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * ربط مستخدم بمستأجر مع دوره.
 */
@Entity
@Table(name = "tenant_users", indexes = {
        @Index(name = "idx_tenant_user_user", columnList = "user_id"),
        @Index(name = "idx_tenant_user_tenant", columnList = "tenant_id"),
        @Index(name = "idx_tenant_user_unique", columnList = "user_id, tenant_id", unique = true)
})
public class TenantUser {

    public enum TenantRole {
        TENANT_OWNER, TENANT_ADMIN, TENANT_USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantRole role = TenantRole.TENANT_USER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (joinedAt == null) joinedAt = Instant.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public TenantRole getRole() { return role; }
    public void setRole(TenantRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantUser that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
