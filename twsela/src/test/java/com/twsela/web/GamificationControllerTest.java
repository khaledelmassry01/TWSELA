package com.twsela.web;

import com.twsela.service.GamificationService;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.web.dto.GamificationLoyaltyDTO.*;
import com.twsela.security.AuthenticationHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GamificationController.class, properties = {
    "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
    "app.security.jwt.expiration-ms=3600000"
})
@Import(GamificationControllerTest.TestMethodSecurityConfig.class)
class GamificationControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean private GamificationService gamificationService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authenticationHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void getProfile_shouldReturnProfile() throws Exception {
        var response = new GamificationProfileResponse(1L, 10L, 5, 1500L, 200L, 300L,
                3, 10, 100, 90, "SILVER", 500L, 120L, 1L, LocalDateTime.now());
        when(gamificationService.getProfileByUserId(10L)).thenReturn(response);

        mockMvc.perform(get("/api/gamification/profiles/10")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tier").value("SILVER"));
    }

    @Test
    void createAchievement_shouldReturnCreated() throws Exception {
        var response = new AchievementResponse(1L, "FIRST_DELIVERY", "First Delivery", "أول توصيل",
                "Complete first delivery", null, null, "DELIVERY", 100, null, true, 0, "COMMON", LocalDateTime.now());
        when(gamificationService.createAchievement(any())).thenReturn(response);

        mockMvc.perform(post("/api/gamification/achievements")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"code":"FIRST_DELIVERY","name":"First Delivery","category":"DELIVERY","xpReward":100}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("FIRST_DELIVERY"));
    }

    @Test
    void getActiveAchievements_shouldReturnList() throws Exception {
        when(gamificationService.getActiveAchievements()).thenReturn(List.of());

        mockMvc.perform(get("/api/gamification/achievements")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getUserAchievements_shouldReturnList() throws Exception {
        when(gamificationService.getUserAchievements(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/gamification/users/10/achievements")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getLeaderboard_shouldReturnEntries() throws Exception {
        var entry = new LeaderboardEntryResponse(1L, 10L, "WEEKLY", "2024-W01",
                1, 5000L, 50, BigDecimal.valueOf(4.8), 2000L, LocalDateTime.now());
        when(gamificationService.getLeaderboard("WEEKLY", "2024-W01")).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/gamification/leaderboard")
                        .param("period", "WEEKLY")
                        .param("periodKey", "2024-W01")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rankPosition").value(1));
    }

    @Test
    void getProfile_forbidden_forMerchant() throws Exception {
        mockMvc.perform(get("/api/gamification/profiles/10")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isForbidden());
    }
}
