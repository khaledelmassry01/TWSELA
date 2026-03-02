package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Smart courier assignment engine.
 * <p>
 * Score(courier, shipment) = Σ(weight_i × factor_i)
 * <ul>
 *   <li>Distance Factor (40%): distance between courier and pickup</li>
 *   <li>Load Factor (25%): current load vs max capacity</li>
 *   <li>Rating Factor (15%): average courier rating</li>
 *   <li>Zone Factor (10%): zone match bonus</li>
 *   <li>Vehicle Factor (5%): vehicle suitability</li>
 *   <li>History Factor (5%): success rate in zone</li>
 * </ul>
 */
@Service
@Transactional
public class SmartAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(SmartAssignmentService.class);

    // Default weights
    static final double W_DISTANCE = 0.40;
    static final double W_LOAD     = 0.25;
    static final double W_RATING   = 0.15;
    static final double W_ZONE     = 0.10;
    static final double W_VEHICLE  = 0.05;
    static final double W_HISTORY  = 0.05;

    // Default rule values
    static final int    DEFAULT_MAX_LOAD      = 30;
    static final double DEFAULT_MAX_DISTANCE  = 50.0;
    static final double DEFAULT_MIN_RATING    = 3.0;
    static final boolean DEFAULT_ZONE_REQUIRED = true;

    private final AssignmentRuleRepository ruleRepository;
    private final AssignmentScoreRepository scoreRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final CourierLocationHistoryRepository locationRepository;
    private final CourierZoneRepository courierZoneRepository;
    private final CourierRatingRepository ratingRepository;
    private final VehicleAssignmentRepository vehicleAssignmentRepository;

    public SmartAssignmentService(AssignmentRuleRepository ruleRepository,
                                   AssignmentScoreRepository scoreRepository,
                                   ShipmentRepository shipmentRepository,
                                   UserRepository userRepository,
                                   CourierLocationHistoryRepository locationRepository,
                                   CourierZoneRepository courierZoneRepository,
                                   CourierRatingRepository ratingRepository,
                                   VehicleAssignmentRepository vehicleAssignmentRepository) {
        this.ruleRepository = ruleRepository;
        this.scoreRepository = scoreRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.courierZoneRepository = courierZoneRepository;
        this.ratingRepository = ratingRepository;
        this.vehicleAssignmentRepository = vehicleAssignmentRepository;
    }

    // ══════════════════════════════════════════════════════════
    // Rules
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<AssignmentRule> getActiveRules() {
        return ruleRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<AssignmentRule> getAllRules() {
        return ruleRepository.findAll();
    }

    public AssignmentRule updateRule(String ruleKey, String newValue) {
        AssignmentRule rule = ruleRepository.findByRuleKey(ruleKey)
                .orElseThrow(() -> new ResourceNotFoundException("AssignmentRule", "ruleKey", ruleKey));
        rule.setRuleValue(newValue);
        rule.setUpdatedAt(Instant.now());
        return ruleRepository.save(rule);
    }

    private double getRuleValue(String key, double defaultVal) {
        return ruleRepository.findByRuleKey(key)
                .filter(AssignmentRule::isActive)
                .map(r -> r.getNumericValue(defaultVal))
                .orElse(defaultVal);
    }

    private boolean getRuleBool(String key, boolean defaultVal) {
        return ruleRepository.findByRuleKey(key)
                .filter(AssignmentRule::isActive)
                .map(AssignmentRule::getBooleanValue)
                .orElse(defaultVal);
    }

    // ══════════════════════════════════════════════════════════
    // Scoring
    // ══════════════════════════════════════════════════════════

    /**
     * Calculate assignment score for a single courier-shipment pair.
     */
    public AssignmentScore calculateScore(Long courierId, Shipment shipment) {
        double maxDistance = getRuleValue("MAX_DISTANCE_KM", DEFAULT_MAX_DISTANCE);
        int maxLoad = (int) getRuleValue("MAX_LOAD_PER_COURIER", DEFAULT_MAX_LOAD);
        double minRating = getRuleValue("MIN_RATING", DEFAULT_MIN_RATING);

        // Distance score (0-1): closer = higher
        double distanceScore = calculateDistanceScore(courierId, shipment, maxDistance);

        // Load score (0-1): fewer shipments = higher
        double loadScore = calculateLoadScore(courierId, maxLoad);

        // Rating score (0-1): higher rating = higher
        double ratingScore = calculateRatingScore(courierId, minRating);

        // Zone score (0 or 1): matches zone = 1
        double zoneScore = calculateZoneScore(courierId, shipment);

        // Vehicle score (0 or 1): has active vehicle = 1
        double vehicleScore = calculateVehicleScore(courierId);

        // History score (0-1): success rate in the zone
        double historyScore = calculateHistoryScore(courierId, shipment);

        double total = W_DISTANCE * distanceScore
                     + W_LOAD     * loadScore
                     + W_RATING   * ratingScore
                     + W_ZONE     * zoneScore
                     + W_VEHICLE  * vehicleScore
                     + W_HISTORY  * historyScore;

        AssignmentScore score = new AssignmentScore();
        score.setShipmentId(shipment.getId());
        score.setCourierId(courierId);
        score.setTotalScore(total);
        score.setDistanceScore(distanceScore);
        score.setLoadScore(loadScore);
        score.setRatingScore(ratingScore);
        score.setZoneScore(zoneScore);
        score.setVehicleScore(vehicleScore);
        score.setHistoryScore(historyScore);
        score.setCalculatedAt(Instant.now());

        return scoreRepository.save(score);
    }

    /**
     * Find the best courier for a shipment among active couriers.
     * @return the highest-scoring courier's AssignmentScore, or empty if none qualifies.
     */
    public Optional<AssignmentScore> findBestCourier(Shipment shipment, List<Long> candidateCourierIds) {
        if (candidateCourierIds.isEmpty()) return Optional.empty();

        double minRating = getRuleValue("MIN_RATING", DEFAULT_MIN_RATING);
        int maxLoad = (int) getRuleValue("MAX_LOAD_PER_COURIER", DEFAULT_MAX_LOAD);
        boolean zoneRequired = getRuleBool("REQUIRE_ZONE_MATCH", DEFAULT_ZONE_REQUIRED);

        AssignmentScore best = null;
        for (Long courierId : candidateCourierIds) {
            // Pre-filter: rating
            Double avgRating = ratingRepository.getAverageRatingByCourierId(courierId);
            if (avgRating != null && avgRating < minRating) continue;

            // Pre-filter: load
            long currentLoad = shipmentRepository.countByCourierIdAndStatusName(courierId, "IN_TRANSIT");
            if (currentLoad >= maxLoad) continue;

            // Pre-filter: zone match (if required)
            if (zoneRequired && shipment.getZone() != null) {
                List<CourierZone> zones = courierZoneRepository.findByCourierIdAndZoneId(
                        courierId, shipment.getZone().getId());
                if (zones.isEmpty()) continue;
            }

            AssignmentScore score = calculateScore(courierId, shipment);
            if (best == null || score.getTotalScore() > best.getTotalScore()) {
                best = score;
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Get all scores for a given shipment.
     */
    @Transactional(readOnly = true)
    public List<AssignmentScore> getScoresForShipment(Long shipmentId) {
        return scoreRepository.findByShipmentIdOrderByTotalScoreDesc(shipmentId);
    }

    // ══════════════════════════════════════════════════════════
    // Individual Factor Calculations
    // ══════════════════════════════════════════════════════════

    double calculateDistanceScore(Long courierId, Shipment shipment, double maxDistanceKm) {
        if (shipment.getDeliveryLatitude() == null || shipment.getDeliveryLongitude() == null) {
            return 0.5; // neutral if no coordinates
        }

        // Get courier's latest location
        List<CourierLocationHistory> locations = locationRepository
                .findByCourierIdOrderByTimestampDesc(courierId);
        if (locations.isEmpty()) return 0.5;

        CourierLocationHistory latest = locations.get(0);
        double distance = haversineKm(
                latest.getLatitude().doubleValue(), latest.getLongitude().doubleValue(),
                shipment.getDeliveryLatitude().doubleValue(), shipment.getDeliveryLongitude().doubleValue());

        if (distance > maxDistanceKm) return 0.0;
        return 1.0 - (distance / maxDistanceKm);
    }

    double calculateLoadScore(Long courierId, int maxLoad) {
        long currentLoad = shipmentRepository.countByCourierIdAndStatusName(courierId, "IN_TRANSIT");
        if (currentLoad >= maxLoad) return 0.0;
        return 1.0 - ((double) currentLoad / maxLoad);
    }

    double calculateRatingScore(Long courierId, double minRating) {
        Double avg = ratingRepository.getAverageRatingByCourierId(courierId);
        if (avg == null) return 0.5; // new courier — neutral
        if (avg < minRating) return 0.0;
        return avg / 5.0; // normalize to 0-1
    }

    double calculateZoneScore(Long courierId, Shipment shipment) {
        if (shipment.getZone() == null) return 0.5;
        List<CourierZone> match = courierZoneRepository.findByCourierIdAndZoneId(
                courierId, shipment.getZone().getId());
        return match.isEmpty() ? 0.0 : 1.0;
    }

    double calculateVehicleScore(Long courierId) {
        boolean hasActiveVehicle = vehicleAssignmentRepository
                .existsByCourierIdAndStatus(courierId,
                        com.twsela.domain.VehicleAssignment.AssignmentStatus.ACTIVE);
        return hasActiveVehicle ? 1.0 : 0.0;
    }

    double calculateHistoryScore(Long courierId, Shipment shipment) {
        if (shipment.getZone() == null) return 0.5;

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        long total = shipmentRepository.countByCourierIdAndCreatedAtBetween(
                courierId, thirtyDaysAgo, Instant.now());
        if (total == 0) return 0.5;

        long delivered = shipmentRepository.countByCourierIdAndStatusName(courierId, "DELIVERED");
        return (double) delivered / total;
    }

    // ══════════════════════════════════════════════════════════
    // Haversine formula
    // ══════════════════════════════════════════════════════════

    static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
