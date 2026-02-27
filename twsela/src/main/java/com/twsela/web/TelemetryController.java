package com.twsela.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Telemetry Controller for collecting frontend error/performance telemetry.
 * Stores recent events in memory (production would persist to a time-series DB).
 */
@RestController
@RequestMapping("/api/telemetry")
@Tag(name = "Telemetry", description = "جمع بيانات الأداء والأخطاء")
public class TelemetryController {

    private static final Logger log = LoggerFactory.getLogger(TelemetryController.class);
    private static final int MAX_EVENTS = 1000;

    // In-memory circular buffer (production: persist to InfluxDB / Elasticsearch)
    private final Deque<Map<String, Object>> events = new ConcurrentLinkedDeque<>();

    @PostMapping
    public ResponseEntity<Map<String, Object>> ingestTelemetry(@RequestBody Map<String, Object> payload) {
        payload.put("receivedAt", Instant.now().toString());
        events.addFirst(payload);

        // Trim to max size
        while (events.size() > MAX_EVENTS) {
            events.removeLast();
        }

        log.debug("Telemetry event ingested: {}", payload.get("type"));
        return ResponseEntity.ok(Map.of("success", true, "message", "Telemetry received"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getTelemetry(
            @RequestParam(defaultValue = "50") int limit) {

        List<Map<String, Object>> recent = new ArrayList<>();
        int count = 0;
        for (Map<String, Object> event : events) {
            if (count++ >= limit) break;
            recent.add(event);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", recent);
        response.put("count", recent.size());
        response.put("total", events.size());
        return ResponseEntity.ok(response);
    }
}
