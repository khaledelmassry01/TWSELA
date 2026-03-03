package com.twsela.web;

import com.twsela.domain.TrackingSession;
import com.twsela.domain.LocationPing;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.LiveTrackingService;
import com.twsela.service.TrackingSessionService;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LiveTrackingController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(LiveTrackingControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة تحكم التتبع الحي")
class LiveTrackingControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private TrackingSessionService trackingSessionService;
    @MockBean private LiveTrackingService liveTrackingService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    private TrackingSession createTestSession() {
        TrackingSession session = new TrackingSession();
        session.setId(100L);
        session.setStatus(TrackingSession.SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session.setTotalPings(5);
        session.setTotalDistanceKm(2.5);
        session.setCurrentLat(30.0);
        session.setCurrentLng(31.0);
        return session;
    }

    @Test
    @DisplayName("يجب بدء جلسة تتبع جديدة")
    void startSession() throws Exception {
        TrackingSession session = createTestSession();
        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(10L);
        when(trackingSessionService.startSession(1L, 10L)).thenReturn(session);

        mockMvc.perform(post("/api/tracking/sessions/start")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shipmentId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(100));
    }

    @Test
    @DisplayName("يجب إيقاف جلسة التتبع مؤقتاً")
    void pauseSession() throws Exception {
        TrackingSession session = createTestSession();
        session.setStatus(TrackingSession.SessionStatus.PAUSED);
        when(trackingSessionService.pauseSession(100L)).thenReturn(session);

        mockMvc.perform(post("/api/tracking/sessions/100/pause")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"));
    }

    @Test
    @DisplayName("يجب استئناف جلسة التتبع")
    void resumeSession() throws Exception {
        TrackingSession session = createTestSession();
        when(trackingSessionService.resumeSession(100L)).thenReturn(session);

        mockMvc.perform(post("/api/tracking/sessions/100/resume")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("يجب إنهاء جلسة التتبع")
    void endSession() throws Exception {
        TrackingSession session = createTestSession();
        session.setStatus(TrackingSession.SessionStatus.ENDED);
        session.setEndedAt(Instant.now());
        when(trackingSessionService.endSession(100L)).thenReturn(session);

        mockMvc.perform(post("/api/tracking/sessions/100/end")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ENDED"));
    }

    @Test
    @DisplayName("يجب إرسال نقطة موقع GPS")
    void sendPing() throws Exception {
        LocationPing ping = new LocationPing();
        ping.setId(1L);
        ping.setTimestamp(Instant.now());

        when(liveTrackingService.processPing(eq(100L), eq(30.0), eq(31.0),
                any(), any(), any(), any())).thenReturn(ping);

        mockMvc.perform(post("/api/tracking/ping")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\": 100, \"lat\": 30.0, \"lng\": 31.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pingId").value(1));
    }

    @Test
    @DisplayName("يجب عرض الجلسة النشطة لشحنة")
    void getActiveSession() throws Exception {
        TrackingSession session = createTestSession();
        session.setEstimatedArrival(Instant.now().plusSeconds(600));
        when(trackingSessionService.getActiveSession(1L)).thenReturn(Optional.of(session));

        mockMvc.perform(get("/api/tracking/sessions/shipment/1")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(100));
    }

    @Test
    @DisplayName("يجب رفض الوصول لمندوب غير مسجل")
    void startSession_forbidden() throws Exception {
        mockMvc.perform(post("/api/tracking/sessions/start")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shipmentId\": 1}"))
                .andExpect(status().isForbidden());
    }
}
