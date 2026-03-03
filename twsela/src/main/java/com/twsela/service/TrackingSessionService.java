package com.twsela.service;

import com.twsela.domain.Shipment;
import com.twsela.domain.TrackingSession;
import com.twsela.domain.User;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.TrackingSessionRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * إدارة جلسات التتبع الحية — إنشاء وإيقاف وإنهاء.
 */
@Service
@Transactional
public class TrackingSessionService {

    private static final Logger log = LoggerFactory.getLogger(TrackingSessionService.class);

    private final TrackingSessionRepository trackingSessionRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public TrackingSessionService(TrackingSessionRepository trackingSessionRepository,
                                  ShipmentRepository shipmentRepository,
                                  UserRepository userRepository) {
        this.trackingSessionRepository = trackingSessionRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * بدء جلسة تتبع جديدة لشحنة معينة.
     */
    public TrackingSession startSession(Long shipmentId, Long courierId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", courierId));

        // End any existing active session for the same shipment
        List<TrackingSession> activeSessions = trackingSessionRepository
                .findByShipmentIdAndStatus(shipmentId, TrackingSession.SessionStatus.ACTIVE);
        for (TrackingSession existing : activeSessions) {
            existing.setStatus(TrackingSession.SessionStatus.ENDED);
            existing.setEndedAt(Instant.now());
            trackingSessionRepository.save(existing);
        }

        TrackingSession session = new TrackingSession();
        session.setShipment(shipment);
        session.setCourier(courier);
        session.setStatus(TrackingSession.SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session.setTotalPings(0);
        session.setTotalDistanceKm(0.0);

        TrackingSession saved = trackingSessionRepository.save(session);
        log.info("Tracking session {} started for shipment {} by courier {}", saved.getId(), shipmentId, courierId);
        return saved;
    }

    /**
     * إيقاف مؤقت لجلسة التتبع.
     */
    public TrackingSession pauseSession(Long sessionId) {
        TrackingSession session = trackingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "id", sessionId));
        if (session.getStatus() != TrackingSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }
        session.setStatus(TrackingSession.SessionStatus.PAUSED);
        log.info("Tracking session {} paused", sessionId);
        return trackingSessionRepository.save(session);
    }

    /**
     * استئناف جلسة تتبع متوقفة.
     */
    public TrackingSession resumeSession(Long sessionId) {
        TrackingSession session = trackingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "id", sessionId));
        if (session.getStatus() != TrackingSession.SessionStatus.PAUSED) {
            throw new IllegalStateException("Session is not paused");
        }
        session.setStatus(TrackingSession.SessionStatus.ACTIVE);
        log.info("Tracking session {} resumed", sessionId);
        return trackingSessionRepository.save(session);
    }

    /**
     * إنهاء جلسة التتبع.
     */
    public TrackingSession endSession(Long sessionId) {
        TrackingSession session = trackingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "id", sessionId));
        session.setStatus(TrackingSession.SessionStatus.ENDED);
        session.setEndedAt(Instant.now());
        log.info("Tracking session {} ended", sessionId);
        return trackingSessionRepository.save(session);
    }

    /**
     * الحصول على الجلسة النشطة لشحنة معينة.
     */
    @Transactional(readOnly = true)
    public Optional<TrackingSession> getActiveSession(Long shipmentId) {
        return trackingSessionRepository
                .findFirstByShipmentIdAndStatusOrderByStartedAtDesc(shipmentId, TrackingSession.SessionStatus.ACTIVE);
    }

    /**
     * جلسات المندوب النشطة.
     */
    @Transactional(readOnly = true)
    public List<TrackingSession> getActiveCourierSessions(Long courierId) {
        return trackingSessionRepository.findByCourierIdAndStatus(courierId, TrackingSession.SessionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public TrackingSession getById(Long sessionId) {
        return trackingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "id", sessionId));
    }

    @Transactional(readOnly = true)
    public List<TrackingSession> getByCourierId(Long courierId) {
        return trackingSessionRepository.findByCourierId(courierId);
    }
}
