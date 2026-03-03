package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Device token for push notifications (FCM).
 * Each user can have multiple devices registered.
 */
@Entity
@Table(name = "device_tokens", indexes = {
    @Index(name = "idx_dt_user", columnList = "user_id"),
    @Index(name = "idx_dt_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_dt_token", columnNames = {"token"})
})
public class DeviceToken {

    public enum Platform {
        ANDROID, IOS, WEB
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 10)
    private Platform platform;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    // Constructors
    public DeviceToken() {}

    public DeviceToken(User user, String token, Platform platform) {
        this.user = user;
        this.token = token;
        this.platform = platform;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
