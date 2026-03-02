package com.twsela.web;

import com.twsela.domain.CourierLocationHistory;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.CourierLocationService;
import com.twsela.web.dto.LocationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for courier GPS location tracking.
 */
@RestController
@RequestMapping("/api/couriers")
@Tag(name = "Courier Location", description = "تتبع موقع المندوب GPS")
public class CourierLocationController {

    private static final Logger log = LoggerFactory.getLogger(CourierLocationController.class);

    private final CourierLocationService locationService;
    private final AuthenticationHelper authHelper;

    public CourierLocationController(CourierLocationService locationService,
                                     AuthenticationHelper authHelper) {
        this.locationService = locationService;
        this.authHelper = authHelper;
    }

    /**
     * Courier updates own location (latitude/longitude from JWT context).
     */
    @Operation(summary = "تحديث موقع المندوب", description = "المندوب يحدّث موقعه الحالي")
    @PostMapping("/location")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<LocationDTO>> updateMyLocation(
            @RequestBody Map<String, BigDecimal> body,
            Authentication authentication) {
        Long courierId = authHelper.getCurrentUserId(authentication);
        BigDecimal latitude = body.get("latitude");
        BigDecimal longitude = body.get("longitude");

        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest()
                    .body(com.twsela.web.dto.ApiResponse.error("latitude and longitude are required"));
        }

        CourierLocationHistory saved = locationService.saveLocation(courierId, latitude, longitude);
        LocationDTO dto = new LocationDTO(saved.getLatitude(), saved.getLongitude(), saved.getTimestamp());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(dto, "تم تحديث الموقع"));
    }

    /**
     * Get the last known location of a courier.
     */
    @Operation(summary = "آخر موقع مندوب", description = "الحصول على آخر موقع معروف للمندوب")
    @GetMapping("/{courierId}/location")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('COURIER')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<LocationDTO>> getLastLocation(
            @PathVariable Long courierId) {
        return locationService.getLastLocation(courierId)
                .map(loc -> ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(loc)))
                .orElse(ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(null, "لا يوجد موقع محفوظ")));
    }

    /**
     * Get today's location history for a courier.
     */
    @Operation(summary = "سجل مواقع المندوب", description = "الحصول على سجل مواقع المندوب لليوم")
    @GetMapping("/{courierId}/location/history")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('COURIER')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<LocationDTO>>> getLocationHistory(
            @PathVariable Long courierId) {
        List<LocationDTO> history = locationService.getLocationHistory(courierId);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(history));
    }
}
