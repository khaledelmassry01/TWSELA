package com.twsela.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class GamificationLoyaltyDTO {

    private GamificationLoyaltyDTO() {}

    // === Gamification Profile ===
    public record GamificationProfileResponse(Long id, Long userId, Integer currentLevel, Long totalXp,
            Long currentLevelXp, Long xpToNextLevel, Integer currentStreak, Integer longestStreak,
            Integer totalDeliveries, Integer perfectDeliveries, String tier, Long monthlyXp, Long weeklyXp,
            Long tenantId, LocalDateTime createdAt) {}

    // === Achievement ===
    public record CreateAchievementRequest(
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 100) String nameAr,
            String description, String descriptionAr,
            @Size(max = 500) String iconUrl,
            @NotBlank @Size(max = 20) String category,
            @Min(0) Integer xpReward,
            String criteria,
            @Size(max = 20) String rarity) {}

    public record AchievementResponse(Long id, String code, String name, String nameAr,
            String description, String descriptionAr, String iconUrl, String category,
            Integer xpReward, String criteria, Boolean isActive, Integer sortOrder,
            String rarity, LocalDateTime createdAt) {}

    // === User Achievement ===
    public record UserAchievementResponse(Long id, Long userId, Long achievementId,
            LocalDateTime unlockedAt, Integer progress, Boolean isCompleted,
            Integer xpAwarded, LocalDateTime createdAt) {}

    // === Leaderboard ===
    public record LeaderboardEntryResponse(Long id, Long userId, String period, String periodKey,
            Integer rankPosition, Long score, Integer deliveryCount, BigDecimal avgRating,
            Long xpEarned, LocalDateTime calculatedAt) {}

    // === Loyalty Program ===
    public record LoyaltyProgramResponse(Long id, Long merchantId, Long currentPoints, Long lifetimePoints,
            String tier, LocalDateTime tierExpiresAt, LocalDateTime pointsExpiringAt,
            Integer pointsExpiring, LocalDateTime lastActivityAt, Long tenantId, LocalDateTime createdAt) {}

    public record CreateLoyaltyTransactionRequest(
            @NotNull Long loyaltyProgramId,
            @NotBlank @Size(max = 30) String transactionType,
            @NotNull Integer points,
            @Size(max = 50) String referenceType,
            Long referenceId,
            @Size(max = 500) String description) {}

    public record LoyaltyTransactionResponse(Long id, Long loyaltyProgramId, String transactionType,
            Integer points, Long balanceAfter, String referenceType, Long referenceId,
            String description, LocalDateTime expiresAt, LocalDateTime createdAt) {}

    // === Promo Code ===
    public record CreatePromoCodeRequest(
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 100) String nameAr,
            @NotBlank @Size(max = 20) String discountType,
            @NotNull BigDecimal discountValue,
            BigDecimal minOrderValue,
            BigDecimal maxDiscountAmount,
            Integer maxUsageTotal,
            Integer maxUsagePerUser,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            String applicableZones,
            String applicablePlans) {}

    public record PromoCodeResponse(Long id, String code, String name, String nameAr,
            String discountType, BigDecimal discountValue, BigDecimal minOrderValue,
            BigDecimal maxDiscountAmount, Integer maxUsageTotal, Integer maxUsagePerUser,
            Integer currentUsage, LocalDateTime validFrom, LocalDateTime validUntil,
            String applicableZones, String applicablePlans, Boolean isActive,
            Long createdById, Long tenantId, LocalDateTime createdAt) {}

    // === Campaign ===
    public record CreateCampaignRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 100) String nameAr,
            String description,
            @NotBlank @Size(max = 20) String campaignType,
            @NotBlank @Size(max = 30) String targetAudience,
            String targetCriteria,
            Long promoCodeId,
            String message,
            String messageAr,
            @Size(max = 10) String channel) {}

    public record CampaignResponse(Long id, String name, String nameAr, String description,
            String campaignType, String targetAudience, String targetCriteria, Long promoCodeId,
            String message, String messageAr, String channel, String status,
            LocalDateTime scheduledAt, LocalDateTime startedAt, LocalDateTime completedAt,
            Integer totalTargets, Integer totalSent, Integer totalOpened, Integer totalConverted,
            Long tenantId, LocalDateTime createdAt) {}
}
