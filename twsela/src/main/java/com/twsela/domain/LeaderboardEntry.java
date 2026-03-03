package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_entries", uniqueConstraints = {
    @UniqueConstraint(name = "uk_lb_user_period", columnNames = {"user_id", "period", "period_key"})
})
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "period", nullable = false, length = 20)
    private String period;

    @NotBlank
    @Size(max = 20)
    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey;

    @NotNull
    @Min(0)
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition = 0;

    @NotNull
    @Min(0)
    @Column(name = "score", nullable = false)
    private Long score = 0L;

    @NotNull
    @Min(0)
    @Column(name = "delivery_count", nullable = false)
    private Integer deliveryCount = 0;

    @NotNull
    @Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal avgRating = BigDecimal.ZERO;

    @NotNull
    @Min(0)
    @Column(name = "xp_earned", nullable = false)
    private Long xpEarned = 0L;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getPeriodKey() { return periodKey; }
    public void setPeriodKey(String periodKey) { this.periodKey = periodKey; }

    public Integer getRankPosition() { return rankPosition; }
    public void setRankPosition(Integer rankPosition) { this.rankPosition = rankPosition; }

    public Long getScore() { return score; }
    public void setScore(Long score) { this.score = score; }

    public Integer getDeliveryCount() { return deliveryCount; }
    public void setDeliveryCount(Integer deliveryCount) { this.deliveryCount = deliveryCount; }

    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }

    public Long getXpEarned() { return xpEarned; }
    public void setXpEarned(Long xpEarned) { this.xpEarned = xpEarned; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
