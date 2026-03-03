package com.twsela.service;

import com.twsela.domain.Shipment;
import com.twsela.domain.TrackingSession;
import com.twsela.domain.User;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.TrackingSessionRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingSessionServiceTest {

    @Mock private TrackingSessionRepository trackingSessionRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TrackingSessionService trackingSessionService;

    private Shipment shipment;
    private User courier;
    private TrackingSession activeSession;

    @BeforeEach
    void setUp() {
        shipment = new Shipment();
        shipment.setId(1L);

        courier = new User();
        courier.setId(10L);
        courier.setPhone("01012345678");
        courier.setName("مندوب تجريبي");

        activeSession = new TrackingSession();
        activeSession.setId(100L);
        activeSession.setShipment(shipment);
        activeSession.setCourier(courier);
        activeSession.setStatus(TrackingSession.SessionStatus.ACTIVE);
        activeSession.setStartedAt(Instant.now());
        activeSession.setTotalPings(0);
        activeSession.setTotalDistanceKm(0.0);
    }

    @Test
    @DisplayName("startSession() creates a new tracking session")
    void startSession_success() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(userRepository.findById(10L)).thenReturn(Optional.of(courier));
        when(trackingSessionRepository.findByShipmentIdAndStatus(1L, TrackingSession.SessionStatus.ACTIVE))
                .thenReturn(List.of());
        when(trackingSessionRepository.save(any(TrackingSession.class))).thenAnswer(inv -> {
            TrackingSession s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        TrackingSession result = trackingSessionService.startSession(1L, 10L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(TrackingSession.SessionStatus.ACTIVE, result.getStatus());
        verify(trackingSessionRepository).save(any(TrackingSession.class));
    }

    @Test
    @DisplayName("startSession() ends existing active sessions")
    void startSession_endsExistingSessions() {
        TrackingSession existing = new TrackingSession();
        existing.setId(99L);
        existing.setStatus(TrackingSession.SessionStatus.ACTIVE);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(userRepository.findById(10L)).thenReturn(Optional.of(courier));
        when(trackingSessionRepository.findByShipmentIdAndStatus(1L, TrackingSession.SessionStatus.ACTIVE))
                .thenReturn(List.of(existing));
        when(trackingSessionRepository.save(any(TrackingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        trackingSessionService.startSession(1L, 10L);

        assertEquals(TrackingSession.SessionStatus.ENDED, existing.getStatus());
        assertNotNull(existing.getEndedAt());
        // save called for ending existing + creating new
        verify(trackingSessionRepository, atLeast(2)).save(any(TrackingSession.class));
    }

    @Test
    @DisplayName("startSession() throws when shipment not found")
    void startSession_shipmentNotFound() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trackingSessionService.startSession(999L, 10L));
    }

    @Test
    @DisplayName("startSession() throws when courier not found")
    void startSession_courierNotFound() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trackingSessionService.startSession(1L, 999L));
    }

    @Test
    @DisplayName("pauseSession() pauses an active session")
    void pauseSession_success() {
        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));
        when(trackingSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrackingSession result = trackingSessionService.pauseSession(100L);

        assertEquals(TrackingSession.SessionStatus.PAUSED, result.getStatus());
    }

    @Test
    @DisplayName("pauseSession() throws if session not active")
    void pauseSession_notActive() {
        activeSession.setStatus(TrackingSession.SessionStatus.ENDED);
        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));

        assertThrows(IllegalStateException.class, () -> trackingSessionService.pauseSession(100L));
    }

    @Test
    @DisplayName("resumeSession() resumes a paused session")
    void resumeSession_success() {
        activeSession.setStatus(TrackingSession.SessionStatus.PAUSED);
        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));
        when(trackingSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrackingSession result = trackingSessionService.resumeSession(100L);

        assertEquals(TrackingSession.SessionStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("resumeSession() throws if session not paused")
    void resumeSession_notPaused() {
        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));
        assertThrows(IllegalStateException.class, () -> trackingSessionService.resumeSession(100L));
    }

    @Test
    @DisplayName("endSession() ends a session")
    void endSession_success() {
        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));
        when(trackingSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrackingSession result = trackingSessionService.endSession(100L);

        assertEquals(TrackingSession.SessionStatus.ENDED, result.getStatus());
        assertNotNull(result.getEndedAt());
    }

    @Test
    @DisplayName("endSession() throws for non-existent session")
    void endSession_notFound() {
        when(trackingSessionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trackingSessionService.endSession(999L));
    }

    @Test
    @DisplayName("getActiveSession() returns optional session")
    void getActiveSession_returnsSession() {
        when(trackingSessionRepository.findFirstByShipmentIdAndStatusOrderByStartedAtDesc(
                1L, TrackingSession.SessionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));

        Optional<TrackingSession> result = trackingSessionService.getActiveSession(1L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getId());
    }

    @Test
    @DisplayName("getActiveCourierSessions() returns courier sessions")
    void getActiveCourierSessions_returnsSessions() {
        when(trackingSessionRepository.findByCourierIdAndStatus(10L, TrackingSession.SessionStatus.ACTIVE))
                .thenReturn(List.of(activeSession));

        List<TrackingSession> result = trackingSessionService.getActiveCourierSessions(10L);

        assertEquals(1, result.size());
    }
}
