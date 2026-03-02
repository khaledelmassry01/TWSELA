package com.twsela.service;

import com.twsela.domain.CourierLocationHistory;
import com.twsela.domain.Shipment;
import com.twsela.domain.User;
import com.twsela.repository.CourierLocationHistoryRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.dto.LocationDTO;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for courier location tracking, ETA calculation, and proximity queries.
 */
@Service
@Transactional
public class CourierLocationService {

    private static final Logger log = LoggerFactory.getLogger(CourierLocationService.class);

    /**
     * Average courier speed in km/h for ETA calculation (urban traffic).
     */
    private static final double AVERAGE_SPEED_KMH = 25.0;

    private final CourierLocationHistoryRepository locationRepository;
    private final UserRepository userRepository;

    public CourierLocationService(CourierLocationHistoryRepository locationRepository,
                                  UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a new location point for a courier.
     */
    public CourierLocationHistory saveLocation(Long courierId, BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude must not be null");
        }
        if (latitude.abs().compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude.abs().compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", courierId));

        CourierLocationHistory location = new CourierLocationHistory(courier, latitude, longitude);
        CourierLocationHistory saved = locationRepository.save(location);
        log.debug("Saved location for courier {}: ({}, {})", courierId, latitude, longitude);
        return saved;
    }

    /**
     * Get the most recent location for a courier.
     */
    @Transactional(readOnly = true)
    public Optional<LocationDTO> getLastLocation(Long courierId) {
        List<CourierLocationHistory> history = locationRepository.findByCourierIdOrderByTimestampDesc(courierId);
        if (history.isEmpty()) {
            return Optional.empty();
        }
        CourierLocationHistory latest = history.get(0);
        return Optional.of(new LocationDTO(latest.getLatitude(), latest.getLongitude(), latest.getTimestamp()));
    }

    /**
     * Get location history for a courier for today.
     */
    @Transactional(readOnly = true)
    public List<LocationDTO> getLocationHistory(Long courierId) {
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        List<CourierLocationHistory> history = locationRepository.findByCourierIdAndTimestampAfter(courierId, todayStart);
        return history.stream()
                .map(h -> new LocationDTO(h.getLatitude(), h.getLongitude(), h.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Calculate estimated minutes to delivery using straight-line distance.
     * Returns null if courier location or shipment destination is unknown.
     */
    @Transactional(readOnly = true)
    public Long calculateETA(Shipment shipment) {
        if (shipment == null || shipment.getDeliveryLatitude() == null || shipment.getDeliveryLongitude() == null) {
            return null;
        }
        User courier = shipment.getCourier();
        if (courier == null) {
            return null;
        }
        Optional<LocationDTO> lastLocation = getLastLocation(courier.getId());
        if (lastLocation.isEmpty()) {
            return null;
        }
        LocationDTO loc = lastLocation.get();
        double distanceKm = haversineDistance(
                loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue(),
                shipment.getDeliveryLatitude().doubleValue(), shipment.getDeliveryLongitude().doubleValue()
        );
        // Multiply by 1.3 for road factor (roads are not straight lines)
        double roadDistanceKm = distanceKm * 1.3;
        long minutes = Math.round((roadDistanceKm / AVERAGE_SPEED_KMH) * 60);
        return Math.max(minutes, 1); // At least 1 minute
    }

    /**
     * Find the nearest courier to given coordinates from a list of courier IDs.
     */
    @Transactional(readOnly = true)
    public Optional<Long> findNearestCourier(BigDecimal targetLat, BigDecimal targetLng, List<Long> courierIds) {
        if (courierIds == null || courierIds.isEmpty()) {
            return Optional.empty();
        }

        Long nearestId = null;
        double minDistance = Double.MAX_VALUE;

        for (Long courierId : courierIds) {
            Optional<LocationDTO> loc = getLastLocation(courierId);
            if (loc.isPresent()) {
                double dist = haversineDistance(
                        loc.get().getLatitude().doubleValue(), loc.get().getLongitude().doubleValue(),
                        targetLat.doubleValue(), targetLng.doubleValue()
                );
                if (dist < minDistance) {
                    minDistance = dist;
                    nearestId = courierId;
                }
            }
        }
        return Optional.ofNullable(nearestId);
    }

    /**
     * Haversine formula to calculate distance between two GPS coordinates in km.
     */
    static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                  * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
