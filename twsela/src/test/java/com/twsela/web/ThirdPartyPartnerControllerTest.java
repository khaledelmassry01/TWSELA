package com.twsela.web;

import com.twsela.service.ThirdPartyPartnerService;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import com.twsela.web.dto.MultiCarrierDTO.*;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ThirdPartyPartnerController.class, properties = {
    "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
    "app.security.jwt.expiration-ms=3600000"
})
@Import(ThirdPartyPartnerControllerTest.TestMethodSecurityConfig.class)
class ThirdPartyPartnerControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private ThirdPartyPartnerService partnerService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authenticationHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void createPartner_shouldReturnCreated() throws Exception {
        var response = new ThirdPartyPartnerResponse(1L, "Partner Express", "01012345678",
                "Cairo,Giza", BigDecimal.valueOf(5.0), "ACTIVE", 1L, LocalDateTime.now());
        when(partnerService.createPartner(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/partners")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Partner Express","contactPhone":"01012345678","serviceArea":"Cairo,Giza"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Partner Express"));
    }

    @Test
    void createHandoff_shouldReturnCreated() throws Exception {
        var response = new PartnerHandoffResponse(1L, 100L, 1L, LocalDateTime.now(),
                "PENDING", "PTN-001", 1L, LocalDateTime.now());
        when(partnerService.createHandoff(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/partners/handoffs")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"shipmentId":100,"partnerId":1,"partnerTrackingNumber":"PTN-001"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getPartner_forbidden_forCourier() throws Exception {
        mockMvc.perform(get("/api/partners/1")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isForbidden());
    }
}
