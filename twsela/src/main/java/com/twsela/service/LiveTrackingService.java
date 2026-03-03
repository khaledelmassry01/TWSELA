package com.twsela.service;

import com.twsela.domain.LocationPing;
import com.twsela.domain.TrackingSession;
import com.twsela.repository.LocationPingRepository;
import com.twsela.repository.TrackingSessionRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * معالجة بيانات الموقع الحية من المندوب وبثها عبر WebSocket.
 */
@Service
@Transactional
public class LiveTrackingService {

    private static final Logger log = LoggerFactory.getLogger(LiveTrackingService.class);

    private final LocationPingRepository locationPingRepository;
    private final TrackingSessionRepository trackingSessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ETACalculationService etaCalculationService;

    public LiveTrackingService(LocationPingRepository locationPingRepository,
                               TrackingSessionRepository trackingSessionRepository,
                               SimpMessagingTemplate messagingTemplate,
                               ETACalculationService etaCalculationService) {
        this.locationPingRepository = locationPingRepository;
        this.trackingSessionRepository = trackingSessionRepository;
        this.messagingTemplate = messagingTemplate;
        this.etaCalculationService = etaCalculationService;
    }

    /**
     * استقبال نقطة موقع جديدة من المندوب.
     */
    public LocationPing processPing(Long sessionId, Double lat, Double lng,
                                     Float accuracy, Float speed, Float heading,
                                     Integer batteryLevel) {
        TrackingSession session = trackingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "id", sessionId));

        if (session.getStatus() != TrackingSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Tracking session is not active");
        }

        // Calculate distance from last known position
        double distance = 0.0;
        if (session.getCurrentLat() != null && session.getCurrentLng() != null) {
            distance = haversineDistance(session.getCurrentLat(), session.getCurrentLng(), lat, lng);
        }

        // Create the ping
        LocationPing ping = new LocationPing();
        ping.setTrackingSession(session);
        ping.setLat(lat);
        ping.setLng(lng);
        ping.setAccuracy(accuracy);
        ping.setSpeed(speed);
        ping.setHeading(heading);
        ping.setBatteryLevel(batteryLevel);
        ping.setTimestamp(Instant.now());
        LocationPing saved = locationPingRepository.save(ping);

        // Update session
        session.setCurrentLat(lat);
        session.setCurrentLng(lng);
        session.setLastPingAt(Instant.now());
        session.setTotalPings(session.getTotalPings() + 1);
        if (session.getTotalDistanceKm() == null) {
            session.setTotalDistanceKm(0.0);
        }
        session.setTotalDistanceKm(session.getTotalDistanceKm() + distance);

        // Calculate ETA
        Instant eta = etaCalculationService.calculateETA(session, speed);
        session.setEstimatedArrival(eta);
        trackingSessionRepository.save(session);

        // Broadcast location update via WebSocket
        broadcastLocationUpdate(session, lat, lng, speed, eta);

        log.debug("Ping #{} for session {}: lat={}, lng={}", session.getTotalPings(), sessionId, lat, lng);
        return saved;
    }

    /**
     * الحصول على آخر نقاط الموقع لجلسة تتبع.
     */
    @Transactional(readOnly = true)
    public List<LocationPing> getRecentPings(Long sessionId, int limit) {
        if (limit <= 10) {
            return locationPingRepository.findTop10ByTrackingSessionIdOrderByTimestampDesc(sessionId);
        }
        return locationPingRepository.findByTrackingSessionIdOrderByTimestampDesc(sessionId);
    }

    /**
     * الحصول على كل نقاط الموقع لجلسة تتبع.
     */
    @Transactional(readOnly = true)
    public List<LocationPing> getAllPings(Long sessionId) {
        return locationPingRepository.findByTrackingSessionIdOrderByTimestampDesc(sessionId);
    }

    private void broadcastLocationUpdate(TrackingSession session, Double lat, Double lng,
                                          Float speed, Instant eta) {
        try {
            Long shipmentId = session.getShipment().getId();
            messagingTemplate.convertAndSend(
                "/topic/tracking/" + shipmentId,
                Map.of(
                    "sessionId", session.getId(),
                    "shipmentId", shipmentId,
                    "lat", lat,
                    "lng", lng,
                    "speed", speed != null ? speed : 0,
                    "eta", eta != null ? eta.toString() : "",
                    "totalDistance", session.getTotalDistanceKm(),
                    "timestamp", Instant.now().toString()
                )
            );
        } catch (Exception e) {
            log.warn("Failed to broadcast location update for session {}: {}", session.getId(), e.getMessage());
        }
    }

    /**
     * حساب المسافة بين نقطتين بالكيلومترات (Haversine Formula).
     */
    double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
