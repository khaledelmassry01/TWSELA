package com.twsela.web;

import com.twsela.domain.AccountLockout;
import com.twsela.domain.SecurityEvent;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.AccountLockoutService;
import com.twsela.service.SecurityAuditService;
import com.twsela.service.SecurityEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SecurityEventController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(SecurityEventControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة تحكم الأحداث الأمنية")
class SecurityEventControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private SecurityEventService securityEventService;
    @MockBean private AccountLockoutService accountLockoutService;
    @MockBean private SecurityAuditService securityAuditService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/security/events — قائمة التهديدات")
    void getEvents_threats() throws Exception {
        when(securityEventService.getActiveThreats()).thenReturn(List.of());

        mockMvc.perform(get("/api/security/events")
                        .with(user("01099999999").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/security/events/summary — ملخص")
    void getEventSummary_success() throws Exception {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("loginFailures", 5L);
        summary.put("bruteForceDetected", 1L);
        summary.put("ipBlocked", 0L);
        summary.put("criticalEvents", 2);
        when(securityEventService.getEventSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/security/events/summary")
                        .with(user("01099999999").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginFailures").value(5));
    }

    @Test
    @DisplayName("POST /api/security/lockouts/{userId}/unlock — فتح حساب")
    void unlockAccount_success() throws Exception {
        AccountLockout unlocked = new AccountLockout();
        unlocked.setId(1L);
        when(authHelper.getCurrentUserId(any())).thenReturn(99L);
        when(accountLockoutService.manualUnlock(1L, 99L)).thenReturn(unlocked);

        mockMvc.perform(post("/api/security/lockouts/1/unlock")
                        .with(user("01099999999").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/security/audit — تقرير تدقيق")
    void getSecurityAudit_success() throws Exception {
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("criticalEventsCount", 0);
        when(securityAuditService.generateSecurityAudit()).thenReturn(audit);

        mockMvc.perform(get("/api/security/audit")
                        .with(user("01099999999").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.criticalEventsCount").value(0));
    }

    @Test
    @DisplayName("GET /api/security/events — مستخدم غير مصرح — 403")
    void getEvents_forbidden() throws Exception {
        mockMvc.perform(get("/api/security/events")
                        .with(user("01012345678").roles("MERCHANT"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
