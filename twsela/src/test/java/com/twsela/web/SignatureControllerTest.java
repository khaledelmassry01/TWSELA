package com.twsela.web;

import com.twsela.service.SignatureService;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import com.twsela.web.dto.DocumentManagementDTO.*;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SignatureController.class, properties = {
    "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
    "app.security.jwt.expiration-ms=3600000"
})
@Import(SignatureControllerTest.TestMethodSecurityConfig.class)
class SignatureControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private SignatureService signatureService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authenticationHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void createSignatureRequest_shouldReturnCreated() throws Exception {
        var response = new SignatureRequestResponse(1L, 10L, "Ahmed", "01012345678",
                "PENDING", "tok-123", LocalDateTime.now().plusDays(7), 1L, LocalDateTime.now());
        when(signatureService.createSignatureRequest(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/signatures/requests")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"documentId":10,"signerName":"Ahmed","expiresAt":"2025-12-31T23:59:59"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signerName").value("Ahmed"));
    }

    @Test
    void createDigitalSignature_shouldReturnCreated() throws Exception {
        var response = new DigitalSignatureResponse(1L, 1L, "/sig/img.png",
                LocalDateTime.now(), "192.168.1.1", "Chrome", LocalDateTime.now());
        when(signatureService.createDigitalSignature(any())).thenReturn(response);

        mockMvc.perform(post("/api/signatures")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"signatureRequestId":1,"signatureImageUrl":"/sig/img.png","ipAddress":"192.168.1.1"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ipAddress").value("192.168.1.1"));
    }

    @Test
    void createCustomsDocument_shouldReturnCreated() throws Exception {
        var response = new CustomsDocumentResponse(1L, 100L, "COMMERCIAL_INVOICE",
                "1234.56", BigDecimal.valueOf(500), "EGP", "EG", "SA", 1L, LocalDateTime.now());
        when(signatureService.createCustomsDocument(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/signatures/customs")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"shipmentId":100,"documentType":"COMMERCIAL_INVOICE","hsCode":"1234.56","declaredValue":500}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentType").value("COMMERCIAL_INVOICE"));
    }

    @Test
    void createSignatureRequest_forbidden_forCourier() throws Exception {
        mockMvc.perform(post("/api/signatures/requests")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"documentId":10,"signerName":"Ahmed","expiresAt":"2025-12-31T23:59:59"}
                        """))
                .andExpect(status().isForbidden());
    }
}
