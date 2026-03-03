package com.twsela.web;

import com.twsela.service.RateLimitService;
import com.twsela.service.FeatureFlagService;
import com.twsela.web.dto.RateLimitFeatureFlagDTO.*;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlatformConfigController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(PlatformConfigControllerTest.TestMethodSecurityConfig.class)
class PlatformConfigControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private RateLimitService rateLimitService;
    @MockBean private FeatureFlagService featureFlagService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("يجب جلب سياسات تحديد المعدل")
    void getRateLimits_success() throws Exception {
        var p = new RateLimitPolicyResponse(1L, "API Policy", "GLOBAL", 100, 60, null, null, true, null, "سياسة عامة", Instant.now());
        when(rateLimitService.getAllPolicies()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/admin/rate-limits")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("API Policy"));
    }

    @Test
    @DisplayName("يجب إنشاء علم ميزة")
    void createFeatureFlag_success() throws Exception {
        var f = new FeatureFlagResponse(1L, "NEW_UI", "واجهة جديدة", null, false, 0, null, null, null, null, null, null, Instant.now());
        when(featureFlagService.createFlag(any(), isNull())).thenReturn(f);

        mockMvc.perform(post("/api/admin/feature-flags")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"featureKey\":\"NEW_UI\",\"name\":\"واجهة جديدة\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب جلب سياسات التخزين المؤقت")
    void getCachePolicies_success() throws Exception {
        var c = new CachePolicyResponse(1L, "Shipments", "SHIPMENT", 300, 1000, "LRU", true, null, Instant.now());
        when(rateLimitService.getActiveCachePolicies()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/admin/cache-policies")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].cacheRegion").value("SHIPMENT"));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير المصرح لهم")
    void getRateLimits_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/rate-limits")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isForbidden());
    }
}
