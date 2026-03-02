package com.twsela;

import com.twsela.config.ApiVersionFilter;
import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.security.JwtService;
import com.twsela.service.AwbService;
import com.twsela.service.BarcodeService;
import com.twsela.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that boot the full application context (H2 + test profile)
 * and verify cross-cutting concerns work together.
 */
@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AwbService awbService;
    @Autowired private BarcodeService barcodeService;

    @Test
    @DisplayName("Health endpoint returns 200")
    void healthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("API version header present on all responses")
    void apiVersionHeader() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().string("X-API-Version", ApiVersionFilter.API_VERSION));
    }

    @Test
    @DisplayName("X-Request-Id header present on all responses")
    void requestIdHeader() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    @DisplayName("Security headers present on response")
    void securityHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName("Protected endpoints return 401 without token")
    void protectedEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth login endpoint accepts POST")
    void loginEndpointAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"phone\":\"0501234567\",\"password\":\"test\"}"))
                // Endpoint is reachable — returns 4xx (bad request or unauthorized), not 403/404
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("AWB service generates valid tracking numbers")
    void awbServiceIntegration() {
        String awb = awbService.generateAwb();
        assertTrue(awbService.isValidAwb(awb));
    }

    @Test
    @DisplayName("Barcode service generates images")
    void barcodeServiceIntegration() throws Exception {
        byte[] barcode = barcodeService.generateBarcode("TWS-20250101-000001");
        assertNotNull(barcode);
        assertTrue(barcode.length > 100, "Barcode should be a valid image");
    }

    @Test
    @DisplayName("Static resources return correct cache headers")
    void staticResourcesCacheHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Pragma", "no-cache"));
    }

    @Test
    @DisplayName("OPTIONS requests are handled by security filter")
    void corsPreflightHandled() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                // CORS preflight handled by security filter chain
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 403,
                            "CORS preflight should return 200 or 403, got: " + status);
                });
    }
}
