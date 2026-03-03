package com.twsela.service;

import com.twsela.domain.Shipment;
import com.twsela.domain.TrackingSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ETACalculationServiceTest {

    private final ETACalculationService etaService = new ETACalculationService();

    @Test
    @DisplayName("calculateETA() returns ETA with valid speed")
    void calculateETA_withSpeed() {
        TrackingSession session = new TrackingSession();
        session.setShipment(new Shipment());
        session.setTotalDistanceKm(3.0);

        Instant eta = etaService.calculateETA(session, 10.0f); // 10 m/s = 36 km/h

        assertNotNull(eta);
        assertTrue(eta.isAfter(Instant.now()));
    }

    @Test
    @DisplayName("calculateETA() uses default speed when null")
    void calculateETA_nullSpeed() {
        TrackingSession session = new TrackingSession();
        session.setShipment(new Shipment());
        session.setTotalDistanceKm(2.0);

        Instant eta = etaService.calculateETA(session, null);

        assertNotNull(eta);
        assertTrue(eta.isAfter(Instant.now()));
    }

    @Test
    @DisplayName("calculateETA() uses minimum speed for very slow movement")
    void calculateETA_verySlowSpeed() {
        TrackingSession session = new TrackingSession();
        session.setShipment(new Shipment());
        session.setTotalDistanceKm(1.0);

        Instant eta = etaService.calculateETA(session, 0.5f); // 0.5 m/s = 1.8 km/h, below minimum

        assertNotNull(eta);
    }

    @Test
    @DisplayName("calculateETA() returns null for null session")
    void calculateETA_nullSession() {
        Instant eta = etaService.calculateETA(null, 10.0f);
        assertNull(eta);
    }

    @Test
    @DisplayName("estimateRemainingDistance() returns default for no travel")
    void estimateRemainingDistance_noTravel() {
        TrackingSession session = new TrackingSession();
        session.setTotalDistanceKm(null);

        Double remaining = etaService.estimateRemainingDistance(session);

        assertEquals(5.0, remaining);
    }

    @Test
    @DisplayName("estimateRemainingDistance() calculates based on traveled distance")
    void estimateRemainingDistance_withTravel() {
        TrackingSession session = new TrackingSession();
        session.setTotalDistanceKm(3.0);

        Double remaining = etaService.estimateRemainingDistance(session);

        assertEquals(7.0, remaining); // 10 - 3 = 7
    }

    @Test
    @DisplayName("estimateRemainingDistance() returns minimum for over-travel")
    void estimateRemainingDistance_overTravel() {
        TrackingSession session = new TrackingSession();
        session.setTotalDistanceKm(15.0);

        Double remaining = etaService.estimateRemainingDistance(session);

        assertEquals(0.5, remaining); // capped at minimum
    }
}
