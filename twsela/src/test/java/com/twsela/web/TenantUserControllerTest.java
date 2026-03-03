package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.TenantInvitation;
import com.twsela.domain.TenantUser;
import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.service.TenantInvitationService;
import com.twsela.service.TenantIsolationService;
import com.twsela.security.TokenBlacklistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TenantUserController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(TenantUserControllerTest.TestMethodSecurityConfig.class)
class TenantUserControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private TenantIsolationService isolationService;
    @MockBean private TenantInvitationService invitationService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("جلب مستخدمي المستأجر")
    void getTenantUsers_success() throws Exception {
        TenantUser tenantUser = new TenantUser();
        tenantUser.setId(1L);
        tenantUser.setRole(TenantUser.TenantRole.TENANT_ADMIN);
        tenantUser.setActive(true);
        tenantUser.setJoinedAt(Instant.now());
        User u = new User();
        u.setId(10L);
        u.setName("محمد");
        u.setPhone("01000000001");
        tenantUser.setUser(u);

        when(isolationService.getTenantUsers(1L)).thenReturn(List.of(tenantUser));

        mockMvc.perform(get("/api/tenants/1/users")
                        .with(user("01000000000").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("إرسال دعوة جديدة")
    void createInvitation_success() throws Exception {
        TenantInvitation invitation = new TenantInvitation();
        invitation.setId(1L);
        invitation.setPhone("01222222222");
        invitation.setRole(TenantUser.TenantRole.TENANT_USER);
        invitation.setStatus(TenantInvitation.InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().plusSeconds(604800));

        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(10L);
        when(invitationService.createInvitation(eq(1L), eq("01222222222"),
                eq(TenantUser.TenantRole.TENANT_USER), eq(10L)))
                .thenReturn(invitation);

        String json = "{\"phone\":\"01222222222\",\"role\":\"TENANT_USER\"}";

        mockMvc.perform(post("/api/tenants/1/invitations")
                        .with(user("01000000000").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("جلب دعوات المستأجر")
    void getInvitations_success() throws Exception {
        when(invitationService.getByTenantAndStatus(1L, TenantInvitation.InvitationStatus.PENDING))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/tenants/1/invitations")
                        .with(user("01000000000").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("رفض الوصول بدون صلاحية")
    void getTenantUsers_forbidden() throws Exception {
        mockMvc.perform(get("/api/tenants/1/users")
                        .with(user("01000000000").roles("COURIER")))
                .andExpect(status().isForbidden());
    }
}
