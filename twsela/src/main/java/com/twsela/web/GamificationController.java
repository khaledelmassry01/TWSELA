package com.twsela.web;

import com.twsela.service.GamificationService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.GamificationLoyaltyDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    // === Profiles ===

    @GetMapping("/profiles/{userId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','COURIER')")
    public ResponseEntity<ApiResponse<GamificationProfileResponse>> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getProfileByUserId(userId)));
    }

    @PostMapping("/profiles/{userId}/init")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<GamificationProfileResponse>> initializeProfile(
            @PathVariable Long userId, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.initializeProfile(userId, tenantId),
                "تم تهيئة ملف التلعيب بنجاح"));
    }

    @PostMapping("/profiles/{userId}/xp")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<GamificationProfileResponse>> addXp(
            @PathVariable Long userId, @RequestParam long amount) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.addXp(userId, amount)));
    }

    @GetMapping("/profiles/top")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<GamificationProfileResponse>>> getTopProfiles(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getTopProfiles(tenantId)));
    }

    // === Achievements ===

    @PostMapping("/achievements")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<AchievementResponse>> createAchievement(
            @Valid @RequestBody CreateAchievementRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.createAchievement(request),
                "تم إنشاء الإنجاز بنجاح"));
    }

    @GetMapping("/achievements")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','COURIER')")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getActiveAchievements() {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getActiveAchievements()));
    }

    @GetMapping("/achievements/category/{category}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','COURIER')")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getAchievementsByCategory(category)));
    }

    // === User Achievements ===

    @GetMapping("/users/{userId}/achievements")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','COURIER')")
    public ResponseEntity<ApiResponse<List<UserAchievementResponse>>> getUserAchievements(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getUserAchievements(userId)));
    }

    @GetMapping("/users/{userId}/achievements/completed")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','COURIER')")
    public ResponseEntity<ApiResponse<List<UserAchievementResponse>>> getCompletedAchievements(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getCompletedAchievements(userId)));
    }

    // === Leaderboard ===

    @GetMapping("/leaderboard")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','COURIER')")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> getLeaderboard(
            @RequestParam String period, @RequestParam String periodKey) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getLeaderboard(period, periodKey)));
    }

    @GetMapping("/leaderboard/top")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','COURIER')")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> getTopLeaderboard(
            @RequestParam String period, @RequestParam String periodKey) {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getTopLeaderboard(period, periodKey)));
    }
}
