package com.twsela.web;

import com.twsela.domain.AssignmentRule;
import com.twsela.domain.AssignmentScore;
import com.twsela.domain.Shipment;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.service.SmartAssignmentService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.AssignmentDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the smart courier assignment engine.
 */
@RestController
@RequestMapping("/api/assignment")
@Tag(name = "Smart Assignment", description = "محرك التوزيع الذكي للشحنات")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class SmartAssignmentController {

    private final SmartAssignmentService assignmentService;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public SmartAssignmentController(SmartAssignmentService assignmentService,
                                      ShipmentRepository shipmentRepository,
                                      UserRepository userRepository) {
        this.assignmentService = assignmentService;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "اقتراح أفضل مندوب لشحنة")
    @GetMapping("/suggest/{shipmentId}")
    public ResponseEntity<ApiResponse<SuggestionResponse>> suggestCourier(
            @PathVariable Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        // Get active couriers
        var couriers = userRepository.findByRoleName("COURIER");
        List<Long> courierIds = couriers.stream().map(u -> u.getId()).toList();

        var best = assignmentService.findBestCourier(shipment, courierIds);
        List<AssignmentScore> scores = assignmentService.getScoresForShipment(shipmentId);

        List<ScoreBreakdownResponse> topCandidates = scores.stream()
                .limit(5)
                .map(this::toScoreResponse)
                .toList();

        SuggestionResponse response = best.map(b -> new SuggestionResponse(
                shipmentId, b.getCourierId(),
                userRepository.findById(b.getCourierId()).map(u -> u.getName()).orElse("Unknown"),
                b.getTotalScore(), topCandidates
        )).orElse(new SuggestionResponse(shipmentId, null, null, 0, topCandidates));

        return ResponseEntity.ok(ApiResponse.ok(response, "اقتراح المندوب"));
    }

    @Operation(summary = "تفاصيل التقييم لشحنة")
    @GetMapping("/score/{shipmentId}")
    public ResponseEntity<ApiResponse<List<ScoreBreakdownResponse>>> getScores(
            @PathVariable Long shipmentId) {
        List<AssignmentScore> scores = assignmentService.getScoresForShipment(shipmentId);
        List<ScoreBreakdownResponse> response = scores.stream()
                .map(this::toScoreResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response, "تفاصيل التقييم"));
    }

    @Operation(summary = "جلب قواعد التعيين")
    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getRules() {
        List<AssignmentRule> rules = assignmentService.getAllRules();
        List<RuleResponse> response = rules.stream()
                .map(r -> new RuleResponse(r.getId(), r.getRuleKey(), r.getRuleValue(),
                        r.getDescription(), r.isActive()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response, "قواعد التعيين"));
    }

    @Operation(summary = "تعديل قاعدة تعيين")
    @PutMapping("/rules")
    public ResponseEntity<ApiResponse<RuleResponse>> updateRule(
            @RequestBody UpdateRuleRequest request) {
        AssignmentRule updated = assignmentService.updateRule(request.ruleKey(), request.ruleValue());
        RuleResponse response = new RuleResponse(updated.getId(), updated.getRuleKey(),
                updated.getRuleValue(), updated.getDescription(), updated.isActive());
        return ResponseEntity.ok(ApiResponse.ok(response, "تم تعديل القاعدة"));
    }

    private ScoreBreakdownResponse toScoreResponse(AssignmentScore s) {
        String name = userRepository.findById(s.getCourierId())
                .map(u -> u.getName()).orElse("Unknown");
        return new ScoreBreakdownResponse(
                s.getShipmentId(), s.getCourierId(), name,
                s.getTotalScore(), s.getDistanceScore(), s.getLoadScore(),
                s.getRatingScore(), s.getZoneScore(), s.getVehicleScore(),
                s.getHistoryScore(), s.getCalculatedAt());
    }
}
