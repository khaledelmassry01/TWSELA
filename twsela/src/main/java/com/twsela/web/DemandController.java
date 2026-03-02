package com.twsela.web;

import com.twsela.domain.Zone;
import com.twsela.repository.ZoneRepository;
import com.twsela.service.DemandPredictionService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.AssignmentDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for demand prediction.
 */
@RestController
@RequestMapping("/api/demand")
@Tag(name = "Demand Prediction", description = "توقع الطلب واحتياج المناديب")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class DemandController {

    private final DemandPredictionService demandService;
    private final ZoneRepository zoneRepository;

    public DemandController(DemandPredictionService demandService, ZoneRepository zoneRepository) {
        this.demandService = demandService;
        this.zoneRepository = zoneRepository;
    }

    @Operation(summary = "توقع الطلب لمنطقة")
    @GetMapping("/predict")
    public ResponseEntity<ApiResponse<DemandPredictionResponse>> predictDemand(
            @RequestParam Long zoneId,
            @RequestParam LocalDate date) {
        Zone zone = zoneRepository.findById(zoneId).orElse(null);
        int predicted = demandService.predictDailyDemand(zoneId, date);
        int couriersNeeded = demandService.predictCourierNeed(zoneId, date);

        DemandPredictionResponse response = new DemandPredictionResponse(
                zoneId, zone != null ? zone.getName() : "Unknown",
                date, predicted, couriersNeeded);
        return ResponseEntity.ok(ApiResponse.ok(response, "توقع الطلب"));
    }

    @Operation(summary = "احتياج المناديب لمنطقة")
    @GetMapping("/courier-need")
    public ResponseEntity<ApiResponse<DemandPredictionResponse>> courierNeed(
            @RequestParam Long zoneId,
            @RequestParam LocalDate date) {
        Zone zone = zoneRepository.findById(zoneId).orElse(null);
        int couriersNeeded = demandService.predictCourierNeed(zoneId, date);

        DemandPredictionResponse response = new DemandPredictionResponse(
                zoneId, zone != null ? zone.getName() : "Unknown",
                date, 0, couriersNeeded);
        return ResponseEntity.ok(ApiResponse.ok(response, "احتياج المناديب"));
    }

    @Operation(summary = "الأنماط التاريخية لمنطقة")
    @GetMapping("/patterns")
    public ResponseEntity<ApiResponse<List<DemandPatternResponse>>> getPatterns(
            @RequestParam Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId).orElse(null);
        String zoneName = zone != null ? zone.getName() : "Unknown";

        List<DemandPatternResponse> patterns = new ArrayList<>();
        String[] dayNames = {"الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد"};

        for (DayOfWeek day : DayOfWeek.values()) {
            double avg = demandService.getHistoricalAverage(zoneId, day);
            patterns.add(new DemandPatternResponse(
                    zoneId, zoneName, day.getValue(), dayNames[day.getValue() - 1], avg));
        }
        return ResponseEntity.ok(ApiResponse.ok(patterns, "الأنماط التاريخية"));
    }
}
