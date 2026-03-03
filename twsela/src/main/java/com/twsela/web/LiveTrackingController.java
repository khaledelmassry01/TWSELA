package com.twsela.web;

import com.twsela.domain.LocationPing;
import com.twsela.domain.TrackingSession;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.LiveTrackingService;
import com.twsela.service.TrackingSessionService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.LocationPingRequest;
import com.twsela.web.dto.StartTrackingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@Tag(name = "Live Tracking", description = "التتبع الحي للشحنات")
public class LiveTrackingController {

    private static final Logger log = LoggerFactory.getLogger(LiveTrackingController.class);

    private final TrackingSessionService trackingSessionService;
    private final LiveTrackingService liveTrackingService;
    private final AuthenticationHelper authenticationHelper;

    public LiveTrackingController(TrackingSessionService trackingSessionService,
                                  LiveTrackingService liveTrackingService,
                                  AuthenticationHelper authenticationHelper) {
        this.trackingSessionService = trackingSessionService;
        this.liveTrackingService = liveTrackingService;
        this.authenticationHelper = authenticationHelper;
    }

    @Operation(summary = "بدء جلسة تتبع حية لشحنة")
    @PostMapping("/sessions/start")
    @PreAuthorize("hasAnyRole('COURIER', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startSession(
            @Valid @RequestBody StartTrackingRequest request,
            Authentication authentication) {
        Long courierId = authenticationHelper.getCurrentUserId(authentication);
        TrackingSession session = trackingSessionService.startSession(request.getShipmentId(), courierId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "sessionId", session.getId(),
                "shipmentId", request.getShipmentId(),
                "status", session.getStatus().name(),
                "startedAt", session.getStartedAt().toString()
        ), "تم بدء جلسة التتبع"));
    }

    @Operation(summary = "إيقاف مؤقت لجلسة التتبع")
    @PostMapping("/sessions/{sessionId}/pause")
    @PreAuthorize("hasAnyRole('COURIER', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pauseSession(
            @PathVariable Long sessionId) {
        TrackingSession session = trackingSessionService.pauseSession(sessionId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "sessionId", session.getId(),
                "status", session.getStatus().name()
        ), "تم إيقاف جلسة التتبع مؤقتاً"));
    }

    @Operation(summary = "استئناف جلسة التتبع")
    @PostMapping("/sessions/{sessionId}/resume")
    @PreAuthorize("hasAnyRole('COURIER', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resumeSession(
            @PathVariable Long sessionId) {
        TrackingSession session = trackingSessionService.resumeSession(sessionId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "sessionId", session.getId(),
                "status", session.getStatus().name()
        ), "تم استئناف جلسة التتبع"));
    }

    @Operation(summary = "إنهاء جلسة التتبع")
    @PostMapping("/sessions/{sessionId}/end")
    @PreAuthorize("hasAnyRole('COURIER', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> endSession(
            @PathVariable Long sessionId) {
        TrackingSession session = trackingSessionService.endSession(sessionId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "sessionId", session.getId(),
                "status", session.getStatus().name(),
                "endedAt", session.getEndedAt().toString(),
                "totalDistance", session.getTotalDistanceKm(),
                "totalPings", session.getTotalPings()
        ), "تم إنهاء جلسة التتبع"));
    }

    @Operation(summary = "إرسال نقطة موقع GPS")
    @PostMapping("/ping")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendPing(
            @Valid @RequestBody LocationPingRequest request) {
        LocationPing ping = liveTrackingService.processPing(
                request.getSessionId(), request.getLat(), request.getLng(),
                request.getAccuracy(), request.getSpeed(), request.getHeading(),
                request.getBatteryLevel()
        );
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "pingId", ping.getId(),
                "timestamp", ping.getTimestamp().toString()
        ), "تم استقبال نقطة الموقع"));
    }

    @Operation(summary = "الحصول على الجلسة الحالية لشحنة")
    @GetMapping("/sessions/shipment/{shipmentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveSession(
            @PathVariable Long shipmentId) {
        return trackingSessionService.getActiveSession(shipmentId)
                .map(session -> {
                    Map<String, Object> data = new java.util.LinkedHashMap<>();
                    data.put("sessionId", session.getId());
                    data.put("status", session.getStatus().name());
                    data.put("currentLat", session.getCurrentLat() != null ? session.getCurrentLat() : 0);
                    data.put("currentLng", session.getCurrentLng() != null ? session.getCurrentLng() : 0);
                    data.put("totalDistance", session.getTotalDistanceKm() != null ? session.getTotalDistanceKm() : 0);
                    data.put("totalPings", session.getTotalPings() != null ? session.getTotalPings() : 0);
                    data.put("estimatedArrival", session.getEstimatedArrival() != null ? session.getEstimatedArrival().toString() : "");
                    return ResponseEntity.ok(ApiResponse.ok(data));
                })
                .orElse(ResponseEntity.ok(ApiResponse.ok(Map.of(), "لا توجد جلسة تتبع نشطة")));
    }

    @Operation(summary = "الحصول على جلسات المندوب النشطة")
    @GetMapping("/sessions/courier")
    @PreAuthorize("hasAnyRole('COURIER', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<TrackingSession>>> getCourierSessions(
            Authentication authentication) {
        Long courierId = authenticationHelper.getCurrentUserId(authentication);
        List<TrackingSession> sessions = trackingSessionService.getActiveCourierSessions(courierId);
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @Operation(summary = "الحصول على آخر نقاط الموقع لجلسة تتبع")
    @GetMapping("/sessions/{sessionId}/pings")
    public ResponseEntity<ApiResponse<List<LocationPing>>> getSessionPings(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LocationPing> pings = liveTrackingService.getRecentPings(sessionId, limit);
        return ResponseEntity.ok(ApiResponse.ok(pings));
    }
}
