package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * إشعار حي يُرسل عبر WebSocket ويُخزَّن لعرضه لاحقاً.
 */
@Entity
@Table(name = "live_notifications", indexes = {
        @Index(name = "idx_ln_user", columnList = "user_id"),
        @Index(name = "idx_ln_read", columnList = "is_read"),
        @Index(name = "idx_ln_created", columnList = "created_at")
})
public class LiveNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ──
    public LiveNotification() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiveNotification that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
