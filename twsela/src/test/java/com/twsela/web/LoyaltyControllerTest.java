package com.twsela.web;

import com.twsela.service.LoyaltyService;
import com.twsela.service.PromoCodeService;
import com.twsela.service.CampaignService;
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

@WebMvcTest(controllers = LoyaltyController.class, properties = {
    "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
    "app.security.jwt.expiration-ms=3600000"
})
@Import(LoyaltyControllerTest.TestMethodSecurityConfig.class)
class LoyaltyControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean private LoyaltyService loyaltyService;
    @MockBean private PromoCodeService promoCodeService;
    @MockBean private CampaignService campaignService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authenticationHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void getLoyaltyProgram_shouldReturnProgram() throws Exception {
        var response = new LoyaltyProgramResponse(1L, 5L, 1000L, 5000L, "GOLD",
                null, null, 0, LocalDateTime.now(), 1L, LocalDateTime.now());
        when(loyaltyService.getProgramByMerchantId(5L)).thenReturn(response);

        mockMvc.perform(get("/api/loyalty/programs/merchant/5")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tier").value("GOLD"));
    }

    @Test
    void createPromoCode_shouldReturnCreated() throws Exception {
        var response = new PromoCodeResponse(1L, "SUMMER2024", "Summer Sale", "تخفيضات الصيف",
                "PERCENTAGE", BigDecimal.valueOf(15), null, null, 100, 1, 0,
                null, null, null, null, true, 1L, 1L, LocalDateTime.now());
        when(promoCodeService.createPromoCode(any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/promo-codes")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"code":"SUMMER2024","name":"Summer Sale","discountType":"PERCENTAGE","discountValue":15}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("SUMMER2024"));
    }

    @Test
    void createCampaign_shouldReturnCreated() throws Exception {
        var response = new CampaignResponse(1L, "Eid Campaign", "حملة العيد", "Special Eid offers",
                "PROMOTIONAL", "ALL_MERCHANTS", null, null, null, null, "ALL", "DRAFT",
                null, null, null, 0, 0, 0, 0, 1L, LocalDateTime.now());
        when(campaignService.createCampaign(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/campaigns")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Eid Campaign","campaignType":"PROMOTIONAL","targetAudience":"ALL_MERCHANTS"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void launchCampaign_shouldReturnActive() throws Exception {
        var response = new CampaignResponse(1L, "Eid Campaign", null, null,
                "PROMOTIONAL", "ALL_MERCHANTS", null, null, null, null, "ALL", "ACTIVE",
                null, LocalDateTime.now(), null, 0, 0, 0, 0, 1L, LocalDateTime.now());
        when(campaignService.launchCampaign(1L)).thenReturn(response);

        mockMvc.perform(post("/api/campaigns/1/launch")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void getPromoCode_forbidden_forCourier() throws Exception {
        mockMvc.perform(get("/api/promo-codes/1")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isForbidden());
    }
}
