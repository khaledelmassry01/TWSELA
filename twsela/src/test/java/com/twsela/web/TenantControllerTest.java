package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.Tenant;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.service.TenantService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TenantController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(TenantControllerTest.TestMethodSecurityConfig.class)
class TenantControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private TenantService tenantService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("إنشاء مستأجر بنجاح")
    void createTenant_success() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("شركة جديدة");
        tenant.setSlug("new-co");
        tenant.setStatus(Tenant.TenantStatus.TRIAL);
        tenant.setPlan(Tenant.TenantPlan.BASIC);

        when(tenantService.createTenant(eq("شركة جديدة"), eq("new-co"), eq("أحمد"), eq("01000000001"), eq(Tenant.TenantPlan.BASIC)))
                .thenReturn(tenant);

        String json = "{\"name\":\"شركة جديدة\",\"slug\":\"new-co\",\"contactName\":\"أحمد\",\"contactPhone\":\"01000000001\",\"plan\":\"BASIC\"}";

        mockMvc.perform(post("/api/tenants")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("جلب كل المستأجرين")
    void getAllTenants_success() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("شركة");
        tenant.setSlug("co");
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        tenant.setPlan(Tenant.TenantPlan.PRO);

        when(tenantService.findAll()).thenReturn(List.of(tenant));

        mockMvc.perform(get("/api/tenants")
                        .with(user("01000000000").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("جلب مستأجر بالـ ID")
    void getTenant_success() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("شركة");
        tenant.setSlug("co");
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);

        when(tenantService.findById(1L)).thenReturn(tenant);

        mockMvc.perform(get("/api/tenants/1")
                        .with(user("01000000000").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("رفض الوصول بدون صلاحية")
    void createTenant_forbidden() throws Exception {
        String json = "{\"name\":\"شركة\",\"slug\":\"co\",\"contactName\":\"أحمد\",\"contactPhone\":\"01000000001\"}";

        mockMvc.perform(post("/api/tenants")
                        .with(user("01000000000").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }
}
