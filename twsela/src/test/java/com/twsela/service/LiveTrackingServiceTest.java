package com.twsela.service;

import com.twsela.domain.LocationPing;
import com.twsela.domain.Shipment;
import com.twsela.domain.TrackingSession;
import com.twsela.repository.LocationPingRepository;
import com.twsela.repository.TrackingSessionRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveTrackingServiceTest {

    @Mock private LocationPingRepository locationPingRepository;
    @Mock private TrackingSessionRepository trackingSessionRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ETACalculationService etaCalculationService;

    @InjectMocks private LiveTrackingService liveTrackingService;

    private TrackingSession activeSession;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        shipment = new Shipment();
        shipment.setId(1L);

        activeSession = new TrackingSession();
        activeSession.setId(100L);
        activeSession.setShipment(shipment);
        activeSession.setStatus(TrackingSession.SessionStatus.ACTIVE);
        activeSession.setTotalPings(0);
        activeSession.setTotalDistanceKm(0.0);
        activeSession.setStartedAt(Instant.now());
    }

    @Test
    @DisplayName("processPing() saves ping and updates session")
    void processPing_success() {
        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));
        when(locationPingRepository.save(any(LocationPing.class))).thenAnswer(inv -> {
            LocationPing p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(trackingSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(etaCalculationService.calculateETA(any(), any())).thenReturn(Instant.now().plusSeconds(600));

        LocationPing result = liveTrackingService.processPing(100L, 30.0, 31.0, 5.0f, 10.0f, 90.0f, 85);

        assertNotNull(result);
        assertEquals(30.0, result.getLat());
        assertEquals(31.0, result.getLng());
        assertEquals(1, activeSession.getTotalPings());
        assertEquals(30.0, activeSession.getCurrentLat());
        assertEquals(31.0, activeSession.getCurrentLng());
        verify(locationPingRepository).save(any(LocationPing.class));
        verify(trackingSessionRepository).save(activeSession);
        verify(messagingTemplate).convertAndSend(eq("/topic/tracking/1"), any(Map.class));
    }

    @Test
    @DisplayName("processPing() calculates distance from previous position")
    void processPing_calculatesDistance() {
        activeSession.setCurrentLat(30.0);
        activeSession.setCurrentLng(31.0);
        activeSession.setTotalDistanceKm(1.0);

        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));
        when(locationPingRepository.save(any())).thenAnswer(inv -> {
            LocationPing p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(trackingSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(etaCalculationService.calculateETA(any(), any())).thenReturn(null);

        liveTrackingService.processPing(100L, 30.01, 31.01, null, null, null, null);

        assertTrue(activeSession.getTotalDistanceKm() > 1.0);
    }

    @Test
    @DisplayName("processPing() throws for non-active session")
    void processPing_notActive() {
        activeSession.setStatus(TrackingSession.SessionStatus.ENDED);
        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));

        assertThrows(IllegalStateException.class,
                () -> liveTrackingService.processPing(100L, 30.0, 31.0, null, null, null, null));
    }

    @Test
    @DisplayName("processPing() throws for unknown session")
    void processPing_sessionNotFound() {
        when(trackingSessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> liveTrackingService.processPing(999L, 30.0, 31.0, null, null, null, null));
    }

    @Test
    @DisplayName("processPing() continues if WebSocket broadcast fails")
    void processPing_broadcastFails() {
        when(trackingSessionRepository.findById(100L)).thenReturn(Optional.of(activeSession));
        when(locationPingRepository.save(any())).thenAnswer(inv -> {
            LocationPing p = inv.getArgument(0);
            p.setId(3L);
            return p;
        });
        when(trackingSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(etaCalculationService.calculateETA(any(), any())).thenReturn(null);
        doThrow(new RuntimeException("ws error")).when(messagingTemplate)
                .convertAndSend(anyString(), any(Map.class));

        LocationPing result = liveTrackingService.processPing(100L, 30.0, 31.0, null, null, null, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getRecentPings() returns top 10 for small limit")
    void getRecentPings_small() {
        when(locationPingRepository.findTop10ByTrackingSessionIdOrderByTimestampDesc(100L))
                .thenReturn(List.of());

        List<LocationPing> result = liveTrackingService.getRecentPings(100L, 5);

        verify(locationPingRepository).findTop10ByTrackingSessionIdOrderByTimestampDesc(100L);
    }

    @Test
    @DisplayName("getRecentPings() returns all for large limit")
    void getRecentPings_large() {
        when(locationPingRepository.findByTrackingSessionIdOrderByTimestampDesc(100L))
                .thenReturn(List.of());

        liveTrackingService.getRecentPings(100L, 50);

        verify(locationPingRepository).findByTrackingSessionIdOrderByTimestampDesc(100L);
    }

    @Test
    @DisplayName("haversineDistance() calculates correctly")
    void haversineDistance_calculates() {
        // Cairo to Alexandria ~ 178 km
        double distance = liveTrackingService.haversineDistance(30.0444, 31.2357, 31.2001, 29.9187);
        assertTrue(distance > 170 && distance < 190, "Expected ~178km, got " + distance);
    }
}
