package com.twsela.repository;

import com.twsela.domain.TrackingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingSessionRepository extends JpaRepository<TrackingSession, Long> {

    List<TrackingSession> findByShipmentIdAndStatus(Long shipmentId, TrackingSession.SessionStatus status);

    List<TrackingSession> findByCourierIdAndStatus(Long courierId, TrackingSession.SessionStatus status);

    Optional<TrackingSession> findFirstByShipmentIdAndStatusOrderByStartedAtDesc(Long shipmentId,
                                                                                  TrackingSession.SessionStatus status);

    List<TrackingSession> findByCourierId(Long courierId);
}
