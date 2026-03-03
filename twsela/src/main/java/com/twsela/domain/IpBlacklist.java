package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * عنوان IP محظور.
 */
@Entity
@Table(name = "ip_blacklist", indexes = {
        @Index(name = "idx_ip_blacklist_address", columnList = "ip_address", unique = true),
        @Index(name = "idx_ip_blacklist_expires", columnList = "expires_at")
})
public class IpBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45, unique = true)
    private String ipAddress;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by")
    private User blockedBy;

    @Column(name = "blocked_at")
    private Instant blockedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "permanent")
    private boolean permanent;

    @Column(name = "created_at")
    private Instant createdAt;

    public IpBlacklist() {
        this.createdAt = Instant.now();
        this.blockedAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getBlockedBy() { return blockedBy; }
    public void setBlockedBy(User blockedBy) { this.blockedBy = blockedBy; }

    public Instant getBlockedAt() { return blockedAt; }
    public void setBlockedAt(Instant blockedAt) { this.blockedAt = blockedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isPermanent() { return permanent; }
    public void setPermanent(boolean permanent) { this.permanent = permanent; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        if (permanent) return false;
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IpBlacklist that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
