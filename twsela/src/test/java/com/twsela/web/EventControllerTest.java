package com.twsela.web;

import com.twsela.domain.DomainEvent;
import com.twsela.domain.EventSubscription;
import com.twsela.repository.EventSubscriptionRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(EventControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة التحكم بالأحداث")
class EventControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private EventPublisher eventPublisher;
    @MockBean private EventSubscriptionRepository eventSubscriptionRepository;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/events — قائمة الأحداث")
    void getEvents_shouldReturnList() throws Exception {
        DomainEvent event = new DomainEvent();
        event.setId(1L);
        event.setEventId("uuid-1");
        event.setEventType("SHIPMENT_STATUS_CHANGED");
        event.setAggregateType("Shipment");
        event.setAggregateId(10L);

        when(eventPublisher.getEventsByType("SHIPMENT_STATUS_CHANGED")).thenReturn(List.of(event));

        mockMvc.perform(get("/api/events")
                        .param("eventType", "SHIPMENT_STATUS_CHANGED")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventType").value("SHIPMENT_STATUS_CHANGED"));
    }

    @Test
    @DisplayName("GET /api/events/{eventId} — تفاصيل حدث")
    void getEvent_shouldReturnEvent() throws Exception {
        DomainEvent event = new DomainEvent();
        event.setId(1L);
        event.setEventId("uuid-detail");
        event.setEventType("PAYMENT_RECEIVED");
        event.setAggregateType("Payment");
        event.setAggregateId(5L);

        when(eventPublisher.getEventByEventId("uuid-detail")).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/uuid-detail")
                        .with(user("01000000000").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eventId").value("uuid-detail"));
    }

    @Test
    @DisplayName("GET /api/events/subscriptions — الاشتراكات النشطة")
    void getSubscriptions_shouldReturnList() throws Exception {
        EventSubscription sub = new EventSubscription();
        sub.setId(1L);
        sub.setSubscriberName("ShipmentHandler");
        sub.setEventType("SHIPMENT_STATUS_CHANGED");
        sub.setHandlerClass("com.twsela.service.ShipmentEventHandler");
        sub.setActive(true);

        when(eventSubscriptionRepository.findActiveSubscriptions()).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/events/subscriptions")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].subscriberName").value("ShipmentHandler"));
    }

    @Test
    @DisplayName("POST /api/events/subscriptions — إنشاء اشتراك جديد")
    void createSubscription_shouldCreate() throws Exception {
        EventSubscription saved = new EventSubscription();
        saved.setId(2L);
        saved.setSubscriberName("NewHandler");
        saved.setEventType("CUSTOM_EVENT");
        saved.setHandlerClass("com.twsela.service.CustomHandler");
        saved.setActive(true);

        when(eventSubscriptionRepository.save(any(EventSubscription.class))).thenReturn(saved);

        String body = "{\"subscriberName\":\"NewHandler\",\"eventType\":\"CUSTOM_EVENT\",\"handlerClass\":\"com.twsela.service.CustomHandler\"}";

        mockMvc.perform(post("/api/events/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subscriberName").value("NewHandler"));
    }
}
