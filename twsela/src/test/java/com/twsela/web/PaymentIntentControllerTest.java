package com.twsela.web;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.PaymentMethod;
import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import com.twsela.repository.PaymentMethodRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.PaymentIntentService;
import com.twsela.web.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentIntentController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(PaymentIntentControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة تحكم نوايا الدفع")
class PaymentIntentControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private PaymentIntentService paymentIntentService;
    @MockBean private PaymentMethodRepository paymentMethodRepository;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/payments/intents — إنشاء نية دفع جديدة")
    void createIntent_success() throws Exception {
        PaymentIntent intent = new PaymentIntent();
        intent.setId(1L);
        intent.setStatus(PaymentIntent.IntentStatus.PENDING);
        intent.setAmount(new BigDecimal("250.00"));
        intent.setCurrency("EGP");
        intent.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));

        when(paymentIntentService.createIntent(eq(1L), any(BigDecimal.class), eq("EGP"),
                eq(PaymentGatewayType.PAYMOB), isNull())).thenReturn(intent);

        mockMvc.perform(post("/api/payments/intents")
                        .with(user("01012345678").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "shipmentId": 1,
                                    "amount": 250.00,
                                    "currency": "EGP",
                                    "gateway": "PAYMOB"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.intentId").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/payments/intents/{id} — الحصول على نية دفع")
    void getIntent_success() throws Exception {
        PaymentIntent intent = new PaymentIntent();
        intent.setId(5L);
        intent.setStatus(PaymentIntent.IntentStatus.SUCCEEDED);
        intent.setAmount(new BigDecimal("100.00"));
        intent.setCurrency("EGP");
        intent.setProvider(PaymentGatewayType.STRIPE);
        intent.setProviderRef("pi_live_123");
        intent.setAttempts(1);
        intent.setConfirmedAt(Instant.now());

        when(paymentIntentService.getById(5L)).thenReturn(intent);

        mockMvc.perform(get("/api/payments/intents/5")
                        .with(user("01012345678").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.intentId").value(5))
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.data.providerRef").value("pi_live_123"));
    }

    @Test
    @DisplayName("POST /api/payments/intents/{id}/confirm — تأكيد نية دفع")
    void confirmIntent_success() throws Exception {
        PaymentIntent confirmed = new PaymentIntent();
        confirmed.setId(2L);
        confirmed.setStatus(PaymentIntent.IntentStatus.SUCCEEDED);
        confirmed.setProviderRef("PAYMOB-REF-001");

        when(paymentIntentService.confirmIntent(2L)).thenReturn(confirmed);

        mockMvc.perform(post("/api/payments/intents/2/confirm")
                        .with(user("01012345678").roles("MERCHANT"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.data.providerRef").value("PAYMOB-REF-001"));
    }

    @Test
    @DisplayName("POST /api/payments/methods — إضافة وسيلة دفع")
    void addMethod_success() throws Exception {
        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(10L);

        PaymentMethod saved = new PaymentMethod();
        saved.setId(3L);
        saved.setType(PaymentMethod.PaymentType.CARD);
        saved.setProvider(PaymentGatewayType.STRIPE);
        saved.setLast4("4242");

        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(saved);

        mockMvc.perform(post("/api/payments/methods")
                        .with(user("01012345678").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "CARD",
                                    "provider": "STRIPE",
                                    "last4": "4242",
                                    "brand": "Visa",
                                    "isDefault": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.methodId").value(3));
    }
}
