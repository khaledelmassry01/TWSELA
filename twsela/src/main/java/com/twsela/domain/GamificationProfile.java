package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "gamification_profiles")
public class GamificationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @NotNull
    @Min(1)
    @Column(name = "current_level", nullable = false)
    private Integer currentLevel = 1;

    @NotNull
    @Min(0)
    @Column(name = "total_xp", nullable = false)
    private Long totalXp = 0L;

    @NotNull
    @Min(0)
    @Column(name = "current_level_xp", nullable = false)
    private Long currentLevelXp = 0L;

    @NotNull
    @Min(0)
    @Column(name = "xp_to_next_level", nullable = false)
    private Long xpToNextLevel = 100L;

    @NotNull
    @Min(0)
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @NotNull
    @Min(0)
    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak = 0;

    @NotNull
    @Min(0)
    @Column(name = "total_deliveries", nullable = false)
    private Integer totalDeliveries = 0;

    @NotNull
    @Min(0)
    @Column(name = "perfect_deliveries", nullable = false)
    private Integer perfectDeliveries = 0;

    @NotNull
    @Size(max = 20)
    @Column(name = "tier", nullable = false, length = 20)
    private String tier = "BRONZE";

    @NotNull
    @Min(0)
    @Column(name = "monthly_xp", nullable = false)
    private Long monthlyXp = 0L;

    @NotNull
    @Min(0)
    @Column(name = "weekly_xp", nullable = false)
    private Long weeklyXp = 0L;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }

    public Long getTotalXp() { return totalXp; }
    public void setTotalXp(Long totalXp) { this.totalXp = totalXp; }

    public Long getCurrentLevelXp() { return currentLevelXp; }
    public void setCurrentLevelXp(Long currentLevelXp) { this.currentLevelXp = currentLevelXp; }

    public Long getXpToNextLevel() { return xpToNextLevel; }
    public void setXpToNextLevel(Long xpToNextLevel) { this.xpToNextLevel = xpToNextLevel; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }

    public Integer getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public Integer getPerfectDeliveries() { return perfectDeliveries; }
    public void setPerfectDeliveries(Integer perfectDeliveries) { this.perfectDeliveries = perfectDeliveries; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public Long getMonthlyXp() { return monthlyXp; }
    public void setMonthlyXp(Long monthlyXp) { this.monthlyXp = monthlyXp; }

    public Long getWeeklyXp() { return weeklyXp; }
    public void setWeeklyXp(Long weeklyXp) { this.weeklyXp = weeklyXp; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
