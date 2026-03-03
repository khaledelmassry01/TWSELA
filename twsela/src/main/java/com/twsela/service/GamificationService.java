package com.twsela.service;

import com.twsela.domain.Achievement;
import com.twsela.domain.GamificationProfile;
import com.twsela.domain.LeaderboardEntry;
import com.twsela.domain.UserAchievement;
import com.twsela.repository.AchievementRepository;
import com.twsela.repository.GamificationProfileRepository;
import com.twsela.repository.LeaderboardEntryRepository;
import com.twsela.repository.UserAchievementRepository;
import com.twsela.web.dto.GamificationLoyaltyDTO.*;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GamificationService {

    private final GamificationProfileRepository profileRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final LeaderboardEntryRepository leaderboardRepository;

    public GamificationService(GamificationProfileRepository profileRepository,
                               AchievementRepository achievementRepository,
                               UserAchievementRepository userAchievementRepository,
                               LeaderboardEntryRepository leaderboardRepository) {
        this.profileRepository = profileRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.leaderboardRepository = leaderboardRepository;
    }

    // === Gamification Profiles ===

    @Transactional(readOnly = true)
    public GamificationProfileResponse getProfileByUserId(Long userId) {
        GamificationProfile p = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gamification profile not found for user: " + userId));
        return toProfileResponse(p);
    }

    public GamificationProfileResponse initializeProfile(Long userId, Long tenantId) {
        if (profileRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("Gamification profile already exists for user: " + userId);
        }
        GamificationProfile p = new GamificationProfile();
        p.setUserId(userId);
        p.setTenantId(tenantId);
        return toProfileResponse(profileRepository.save(p));
    }

    public GamificationProfileResponse addXp(Long userId, long xpAmount) {
        GamificationProfile p = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gamification profile not found for user: " + userId));
        p.setTotalXp(p.getTotalXp() + xpAmount);
        p.setCurrentLevelXp(p.getCurrentLevelXp() + xpAmount);
        p.setMonthlyXp(p.getMonthlyXp() + xpAmount);
        p.setWeeklyXp(p.getWeeklyXp() + xpAmount);
        while (p.getCurrentLevelXp() >= p.getXpToNextLevel()) {
            p.setCurrentLevelXp(p.getCurrentLevelXp() - p.getXpToNextLevel());
            p.setCurrentLevel(p.getCurrentLevel() + 1);
            p.setXpToNextLevel((long) (p.getXpToNextLevel() * 1.5));
        }
        return toProfileResponse(profileRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<GamificationProfileResponse> getTopProfiles(Long tenantId) {
        return profileRepository.findTop10ByTenantIdOrderByTotalXpDesc(tenantId)
                .stream().map(this::toProfileResponse).toList();
    }

    // === Achievements ===

    public AchievementResponse createAchievement(CreateAchievementRequest request) {
        if (achievementRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Achievement already exists with code: " + request.code());
        }
        Achievement a = new Achievement();
        a.setCode(request.code());
        a.setName(request.name());
        a.setNameAr(request.nameAr());
        a.setDescription(request.description());
        a.setDescriptionAr(request.descriptionAr());
        a.setIconUrl(request.iconUrl());
        a.setCategory(request.category());
        a.setXpReward(request.xpReward() != null ? request.xpReward() : 0);
        a.setCriteria(request.criteria());
        a.setRarity(request.rarity() != null ? request.rarity() : "COMMON");
        return toAchievementResponse(achievementRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getActiveAchievements() {
        return achievementRepository.findByIsActiveTrueOrderBySortOrder()
                .stream().map(this::toAchievementResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAchievementsByCategory(String category) {
        return achievementRepository.findByCategoryAndIsActiveTrueOrderBySortOrder(category)
                .stream().map(this::toAchievementResponse).toList();
    }

    // === User Achievements ===

    @Transactional(readOnly = true)
    public List<UserAchievementResponse> getUserAchievements(Long userId) {
        return userAchievementRepository.findByUserId(userId)
                .stream().map(this::toUserAchievementResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserAchievementResponse> getCompletedAchievements(Long userId) {
        return userAchievementRepository.findByUserIdAndIsCompletedTrue(userId)
                .stream().map(this::toUserAchievementResponse).toList();
    }

    // === Leaderboard ===

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(String period, String periodKey) {
        return leaderboardRepository.findByPeriodAndPeriodKeyOrderByRankPositionAsc(period, periodKey)
                .stream().map(this::toLeaderboardResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getTopLeaderboard(String period, String periodKey) {
        return leaderboardRepository.findTop10ByPeriodAndPeriodKeyOrderByScoreDesc(period, periodKey)
                .stream().map(this::toLeaderboardResponse).toList();
    }

    // === Mappers ===

    private GamificationProfileResponse toProfileResponse(GamificationProfile p) {
        return new GamificationProfileResponse(p.getId(), p.getUserId(), p.getCurrentLevel(), p.getTotalXp(),
                p.getCurrentLevelXp(), p.getXpToNextLevel(), p.getCurrentStreak(), p.getLongestStreak(),
                p.getTotalDeliveries(), p.getPerfectDeliveries(), p.getTier(), p.getMonthlyXp(), p.getWeeklyXp(),
                p.getTenantId(), p.getCreatedAt());
    }

    private AchievementResponse toAchievementResponse(Achievement a) {
        return new AchievementResponse(a.getId(), a.getCode(), a.getName(), a.getNameAr(),
                a.getDescription(), a.getDescriptionAr(), a.getIconUrl(), a.getCategory(),
                a.getXpReward(), a.getCriteria(), a.getIsActive(), a.getSortOrder(),
                a.getRarity(), a.getCreatedAt());
    }

    private UserAchievementResponse toUserAchievementResponse(UserAchievement ua) {
        return new UserAchievementResponse(ua.getId(), ua.getUserId(), ua.getAchievementId(),
                ua.getUnlockedAt(), ua.getProgress(), ua.getIsCompleted(), ua.getXpAwarded(), ua.getCreatedAt());
    }

    private LeaderboardEntryResponse toLeaderboardResponse(LeaderboardEntry l) {
        return new LeaderboardEntryResponse(l.getId(), l.getUserId(), l.getPeriod(), l.getPeriodKey(),
                l.getRankPosition(), l.getScore(), l.getDeliveryCount(), l.getAvgRating(),
                l.getXpEarned(), l.getCalculatedAt());
    }
}
