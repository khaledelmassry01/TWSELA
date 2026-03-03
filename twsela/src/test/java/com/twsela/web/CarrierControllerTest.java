package com.twsela.web;

import com.twsela.service.CarrierService;
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

@WebMvcTest(controllers = CarrierController.class, properties = {
    "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
    "app.security.jwt.expiration-ms=3600000"
})
@Import(CarrierControllerTest.TestMethodSecurityConfig.class)
class CarrierControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private CarrierService carrierService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authenticationHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void createCarrier_shouldReturnCreated() throws Exception {
        var response = new CarrierResponse(1L, "Aramex", "ARAMEX", "INTERNATIONAL",
                "https://api.aramex.com", "ACTIVE", "EG,SA", 1L, LocalDateTime.now());
        when(carrierService.createCarrier(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/carriers")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Aramex","code":"ARAMEX","type":"INTERNATIONAL"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("ARAMEX"));
    }

    @Test
    void getCarrier_shouldReturnCarrier() throws Exception {
        var response = new CarrierResponse(1L, "Aramex", "ARAMEX", "INTERNATIONAL",
                null, "ACTIVE", null, 1L, LocalDateTime.now());
        when(carrierService.getCarrierById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/carriers/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Aramex"));
    }

    @Test
    void createCarrierShipment_shouldReturnCreated() throws Exception {
        var response = new CarrierShipmentResponse(1L, 100L, 1L, "AWB123", null, null,
                BigDecimal.valueOf(50), 1L, LocalDateTime.now());
        when(carrierService.createCarrierShipment(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/carriers/shipments")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"shipmentId":100,"carrierId":1,"externalTrackingNumber":"AWB123"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.externalTrackingNumber").value("AWB123"));
    }

    @Test
    void createSelectionRule_shouldReturnCreated() throws Exception {
        var response = new CarrierSelectionRuleResponse(1L, 1, 5L, null, null,
                1L, 2L, null, true, 1L, LocalDateTime.now());
        when(carrierService.createSelectionRule(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/carriers/selection-rules")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"priority":1,"zoneId":5,"preferredCarrierId":1,"fallbackCarrierId":2}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value(1));
    }

    @Test
    void getCarrier_forbidden_forMerchant() throws Exception {
        mockMvc.perform(get("/api/carriers/1")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isForbidden());
    }
}
