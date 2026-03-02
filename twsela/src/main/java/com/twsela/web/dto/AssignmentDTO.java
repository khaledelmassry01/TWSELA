package com.twsela.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for smart assignment, route optimization and demand prediction.
 */
public class AssignmentDTO {

    // ── Score DTOs ──────────────────────────────────────────

    public record ScoreBreakdownResponse(
            Long shipmentId, Long courierId, String courierName,
            double totalScore,
            double distanceScore, double loadScore, double ratingScore,
            double zoneScore, double vehicleScore, double historyScore,
            Instant calculatedAt
    ) {}

    public record SuggestionResponse(
            Long shipmentId,
            Long suggestedCourierId,
            String suggestedCourierName,
            double score,
            List<ScoreBreakdownResponse> topCandidates
    ) {}

    public record AutoAssignRequest(
            List<Long> shipmentIds
    ) {}

    public record AutoAssignResponse(
            int totalShipments,
            int assigned,
            int unassigned,
            List<AssignmentResultItem> results
    ) {}

    public record AssignmentResultItem(
            Long shipmentId,
            Long courierId,
            String courierName,
            double score,
            boolean assigned,
            String reason
    ) {}

    // ── Rule DTOs ───────────────────────────────────────────

    public record RuleResponse(
            Long id, String ruleKey, String ruleValue, String description, boolean active
    ) {}

    public record UpdateRuleRequest(
            String ruleKey, String ruleValue
    ) {}

    // ── Route DTOs ──────────────────────────────────────────

    public record OptimizedRouteResponse(
            Long routeId, Long courierId, Long manifestId,
            String waypoints,
            double totalDistanceKm, int estimatedDurationMinutes,
            Instant optimizedAt
    ) {}

    public record OptimizeRouteRequest(
            List<Long> shipmentIds
    ) {}

    // ── Demand DTOs ─────────────────────────────────────────

    public record DemandPredictionResponse(
            Long zoneId, String zoneName,
            LocalDate date,
            int predictedShipments,
            int recommendedCouriers
    ) {}

    public record DemandPatternResponse(
            Long zoneId, String zoneName,
            int dayOfWeek, String dayName,
            double averageShipments
    ) {}

    private AssignmentDTO() {}
}
