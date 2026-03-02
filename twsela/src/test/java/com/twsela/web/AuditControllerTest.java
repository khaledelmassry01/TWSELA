package com.twsela.web;

import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.security.JwtService;
import com.twsela.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuditController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(AuditControllerTest.TestMethodSecurityConfig.class)
class AuditControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private AuditService auditService;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    private Authentication createAuth(String roleName) {
        Role role = new Role(roleName);
        role.setId(1L);
        UserStatus activeStatus = new UserStatus("ACTIVE");
        activeStatus.setId(1L);
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setPhone("0501234567");
        user.setRole(role);
        user.setStatus(activeStatus);
        user.setIsDeleted(false);
        return new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }

    @Test
    @DisplayName("GET /api/audit/logs — يجب إرجاع سجلات المراجعة بنجاح")
    void getAuditLogs_success() throws Exception {
        when(auditService.getAuditLogsByDateRange(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/audit/logs").with(authentication(createAuth("OWNER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/audit/user/{userId} — يجب إرجاع سجلات مستخدم بنجاح")
    void getUserAuditLogs_success() throws Exception {
        when(auditService.getUserAuditLogs(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/audit/user/1").with(authentication(createAuth("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("GET /api/audit/logs — يجب رفض الوصول للتاجر")
    void getAuditLogs_forbidden_forMerchant() throws Exception {
        mockMvc.perform(get("/api/audit/logs").with(authentication(createAuth("MERCHANT"))))
                .andExpect(status().isForbidden());
    }
}
