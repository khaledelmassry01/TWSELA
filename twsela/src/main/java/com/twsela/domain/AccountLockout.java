package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * قفل حساب بعد محاولات فاشلة متكررة.
 */
@Entity
@Table(name = "account_lockouts", indexes = {
        @Index(name = "idx_lockout_user", columnList = "user_id"),
        @Index(name = "idx_lockout_end", columnList = "lockout_end")
})
public class AccountLockout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "lockout_start")
    private Instant lockoutStart;

    @Column(name = "lockout_end")
    private Instant lockoutEnd;

    @Column(name = "lockout_reason", length = 500)
    private String lockoutReason;

    @Column(name = "auto_unlock_at")
    private Instant autoUnlockAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unlocked_by")
    private User unlockedBy;

    @Column(name = "unlocked_at")
    private Instant unlockedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public AccountLockout() {
        this.createdAt = Instant.now();
        this.failedAttempts = 0;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public Instant getLockoutStart() { return lockoutStart; }
    public void setLockoutStart(Instant lockoutStart) { this.lockoutStart = lockoutStart; }

    public Instant getLockoutEnd() { return lockoutEnd; }
    public void setLockoutEnd(Instant lockoutEnd) { this.lockoutEnd = lockoutEnd; }

    public String getLockoutReason() { return lockoutReason; }
    public void setLockoutReason(String lockoutReason) { this.lockoutReason = lockoutReason; }

    public Instant getAutoUnlockAt() { return autoUnlockAt; }
    public void setAutoUnlockAt(Instant autoUnlockAt) { this.autoUnlockAt = autoUnlockAt; }

    public User getUnlockedBy() { return unlockedBy; }
    public void setUnlockedBy(User unlockedBy) { this.unlockedBy = unlockedBy; }

    public Instant getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Instant unlockedAt) { this.unlockedAt = unlockedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountLockout that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
