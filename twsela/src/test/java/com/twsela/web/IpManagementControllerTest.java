package com.twsela.web;

import com.twsela.domain.IpBlacklist;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.IpBlockingService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IpManagementController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(IpManagementControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة تحكم إدارة IP")
class IpManagementControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private IpBlockingService ipBlockingService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/security/ip-blacklist — القائمة السوداء")
    void getBlacklist_success() throws Exception {
        when(ipBlockingService.getActiveBlacklist()).thenReturn(List.of());

        mockMvc.perform(get("/api/security/ip-blacklist")
                        .with(user("01099999999").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/security/ip-blacklist — حظر IP")
    void blockIp_success() throws Exception {
        IpBlacklist blocked = new IpBlacklist();
        blocked.setId(1L);
        blocked.setIpAddress("192.168.1.100");

        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(ipBlockingService.blockIp(eq("192.168.1.100"), eq("سبام"), eq(1L), eq(false)))
                .thenReturn(blocked);

        mockMvc.perform(post("/api/security/ip-blacklist")
                        .with(user("01099999999").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ipAddress\":\"192.168.1.100\",\"reason\":\"سبام\",\"permanent\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/security/ip-blacklist/{id} — رفع الحظر")
    void unblockIp_success() throws Exception {
        doNothing().when(ipBlockingService).unblockIp(1L);

        mockMvc.perform(delete("/api/security/ip-blacklist/1")
                        .with(user("01099999999").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/security/ip-blacklist — مستخدم غير مصرح — 403")
    void blockIp_forbidden() throws Exception {
        mockMvc.perform(post("/api/security/ip-blacklist")
                        .with(user("01012345678").roles("COURIER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ipAddress\":\"10.0.0.1\",\"reason\":\"test\",\"permanent\":false}"))
                .andExpect(status().isForbidden());
    }
}
