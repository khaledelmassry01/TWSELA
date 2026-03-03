package com.twsela.web;

import com.twsela.domain.Tenant;
import com.twsela.domain.TenantBranding;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.service.TenantBrandingService;
import com.twsela.service.TenantService;
import com.twsela.security.TokenBlacklistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TenantBrandingController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(TenantBrandingControllerTest.TestMethodSecurityConfig.class)
class TenantBrandingControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private TenantBrandingService brandingService;
    @MockBean private TenantService tenantService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("جلب العلامة التجارية")
    void getBranding_success() throws Exception {
        TenantBranding branding = new TenantBranding();
        branding.setId(1L);
        branding.setPrimaryColor("#1a73e8");
        branding.setCompanyNameAr("شركة التوصيل");

        when(brandingService.getByTenantId(1L)).thenReturn(branding);

        mockMvc.perform(get("/api/tenants/1/branding")
                        .with(user("01000000000").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.primaryColor").value("#1a73e8"));
    }

    @Test
    @DisplayName("جلب الثيم العام بالـ slug")
    void getPublicBrandingCSS_success() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setSlug("delivery-co");

        when(tenantService.findBySlug("delivery-co")).thenReturn(tenant);
        when(brandingService.generateCSS(1L)).thenReturn(":root { --primary-color: #1a73e8; }");

        mockMvc.perform(get("/api/public/branding/delivery-co")
                        .with(user("01000000000").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"))
                .andExpect(content().string(":root { --primary-color: #1a73e8; }"));
    }

    @Test
    @DisplayName("رفض الوصول بدون صلاحية")
    void getBranding_forbidden() throws Exception {
        mockMvc.perform(get("/api/tenants/1/branding")
                        .with(user("01000000000").roles("COURIER")))
                .andExpect(status().isForbidden());
    }
}
