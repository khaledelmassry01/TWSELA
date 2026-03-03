package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;

/**
 * User preferences for notifications — channels per event, quiet hours, digest mode.
 */
@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_np_user", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_np_user", columnNames = {"user_id"})
})
public class NotificationPreference {

    public enum DigestMode {
        NONE, DAILY, WEEKLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * JSON map of event type → enabled channels.
     * e.g. {"SHIPMENT_CREATED": ["EMAIL","PUSH"], "STATUS_CHANGED": ["SMS","IN_APP"]}
     */
    @Column(name = "enabled_channels", columnDefinition = "TEXT")
    private String enabledChannelsJson;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "digest_mode", nullable = false, length = 10)
    private DigestMode digestMode = DigestMode.NONE;

    @Column(name = "paused_until")
    private Instant pausedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    // Constructors
    public NotificationPreference() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEnabledChannelsJson() { return enabledChannelsJson; }
    public void setEnabledChannelsJson(String enabledChannelsJson) { this.enabledChannelsJson = enabledChannelsJson; }

    public LocalTime getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }

    public LocalTime getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }

    public DigestMode getDigestMode() { return digestMode; }
    public void setDigestMode(DigestMode digestMode) { this.digestMode = digestMode; }

    public Instant getPausedUntil() { return pausedUntil; }
    public void setPausedUntil(Instant pausedUntil) { this.pausedUntil = pausedUntil; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
