package com.twsela.service;

import com.twsela.domain.TrackingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * حساب الوقت المتوقع للوصول بناءً على السرعة والمسافة المتبقية.
 */
@Service
public class ETACalculationService {

    private static final Logger log = LoggerFactory.getLogger(ETACalculationService.class);

    // Default average speed when no speed data is available (km/h)
    private static final double DEFAULT_SPEED_KMH = 30.0;
    // Minimum speed to use for ETA calculation (km/h)
    private static final double MIN_SPEED_KMH = 5.0;

    /**
     * حساب الوقت المتوقع للوصول.
     *
     * @param session   جلسة التتبع الحالية
     * @param currentSpeed السرعة الحالية بالمتر/ثانية (nullable)
     * @return الوقت المتوقع للوصول أو null إذا لم يكن هناك بيانات كافية
     */
    public Instant calculateETA(TrackingSession session, Float currentSpeed) {
        if (session == null || session.getShipment() == null) {
            return null;
        }

        // We'd normally use the shipment destination coordinates, but for now
        // we'll use a simple speed-based estimate if we have remaining distance estimation
        Double estimatedRemainingKm = estimateRemainingDistance(session);
        if (estimatedRemainingKm == null || estimatedRemainingKm <= 0) {
            return null;
        }

        double speedKmh;
        if (currentSpeed != null && currentSpeed > 0) {
            // Convert m/s to km/h
            speedKmh = currentSpeed * 3.6;
            if (speedKmh < MIN_SPEED_KMH) {
                speedKmh = MIN_SPEED_KMH;
            }
        } else {
            speedKmh = DEFAULT_SPEED_KMH;
        }

        // time = distance / speed  (in hours)
        double hoursRemaining = estimatedRemainingKm / speedKmh;
        long secondsRemaining = (long) (hoursRemaining * 3600);

        Instant eta = Instant.now().plusSeconds(secondsRemaining);
        log.debug("ETA calculated: {} km remaining at {} km/h = {} seconds, arrival at {}",
                estimatedRemainingKm, speedKmh, secondsRemaining, eta);
        return eta;
    }

    /**
     * تقدير المسافة المتبقية — في التطبيق الحقيقي يستخدم Google Maps API.
     * حالياً يُرجع قيمة افتراضية بسيطة.
     */
    Double estimateRemainingDistance(TrackingSession session) {
        // Placeholder: In production, this would call Google Maps Distance Matrix API
        // For now, return a simple estimate based on total distance traveled
        if (session.getTotalDistanceKm() != null && session.getTotalDistanceKm() > 0) {
            // Assume a typical delivery is ~10km total, subtract what's been traveled
            double estimatedTotal = 10.0;
            double remaining = estimatedTotal - session.getTotalDistanceKm();
            return remaining > 0 ? remaining : 0.5; // Minimum 0.5 km
        }
        return 5.0; // Default: 5km remaining
    }
}
