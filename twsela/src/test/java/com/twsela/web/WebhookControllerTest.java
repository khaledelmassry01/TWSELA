package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.domain.WebhookEvent.DeliveryStatus;
import com.twsela.security.JwtService;
import com.twsela.service.WebhookService;
import com.twsela.web.dto.WebhookDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = WebhookController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000",
        "spring.profiles.active=test"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private WebhookService webhookService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    private User merchantUser;
    private WebhookSubscription sampleSub;

    @BeforeEach
    void setUp() {
        merchantUser = new User();
        merchantUser.setId(1L);
        merchantUser.setName("Test Merchant");

        sampleSub = new WebhookSubscription(merchantUser, "https://example.com/hook", "secret", "SHIPMENT_CREATED,STATUS_CHANGED");
        sampleSub.setId(10L);
        sampleSub.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("POST /api/webhooks — إنشاء اشتراك")
    void createSubscription_success() throws Exception {
        when(authHelper.getCurrentUser(any())).thenReturn(merchantUser);
        when(webhookService.subscribe(any(), eq("https://example.com/hook"), any()))
                .thenReturn(sampleSub);

        WebhookDTO.CreateWebhookRequest req = new WebhookDTO.CreateWebhookRequest();
        req.setUrl("https://example.com/hook");
        req.setEvents(List.of("SHIPMENT_CREATED", "STATUS_CHANGED"));

        mockMvc.perform(post("/api/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value("https://example.com/hook"));
    }

    @Test
    @DisplayName("GET /api/webhooks — قائمة الاشتراكات")
    void getSubscriptions_success() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(webhookService.getSubscriptions(1L)).thenReturn(List.of(sampleSub));

        mockMvc.perform(get("/api/webhooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    @DisplayName("GET /api/webhooks/{id} — تفاصيل الاشتراك")
    void getSubscription_success() throws Exception {
        when(webhookService.getSubscription(10L)).thenReturn(sampleSub);

        mockMvc.perform(get("/api/webhooks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("https://example.com/hook"));
    }

    @Test
    @DisplayName("DELETE /api/webhooks/{id} — إلغاء الاشتراك")
    void deleteSubscription_success() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        doNothing().when(webhookService).unsubscribe(10L, 1L);

        mockMvc.perform(delete("/api/webhooks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/webhooks/{id}/events — سجل الأحداث")
    void getEvents_success() throws Exception {
        WebhookEvent evt = new WebhookEvent(sampleSub, "SHIPMENT_CREATED", "{}");
        evt.setId(1L);
        evt.setStatus(DeliveryStatus.SENT);
        evt.setAttempts(1);
        evt.setResponseCode(200);
        evt.setCreatedAt(Instant.now());

        when(webhookService.getEvents(eq(10L), any())).thenReturn(new PageImpl<>(List.of(evt)));

        mockMvc.perform(get("/api/webhooks/10/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].eventType").value("SHIPMENT_CREATED"))
                .andExpect(jsonPath("$.data[0].status").value("SENT"));
    }

    @Test
    @DisplayName("POST /api/webhooks/{id}/test — إرسال اختبار")
    void sendTest_success() throws Exception {
        WebhookEvent testEvt = new WebhookEvent(sampleSub, "TEST", "{\"test\":true}");
        testEvt.setId(100L);
        testEvt.setStatus(DeliveryStatus.SENT);
        testEvt.setCreatedAt(Instant.now());

        when(webhookService.sendTestEvent(10L)).thenReturn(testEvt);

        mockMvc.perform(post("/api/webhooks/10/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventType").value("TEST"));
    }

    @Test
    @DisplayName("POST /api/webhooks/retry — إعادة المحاولة")
    void retryFailed_success() throws Exception {
        when(webhookService.retryFailed()).thenReturn(3);

        mockMvc.perform(post("/api/webhooks/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3));
    }
}
