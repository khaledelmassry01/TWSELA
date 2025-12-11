package com.twsela.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for monitoring application status
 * This endpoint is public and doesn't require authentication
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "فحص حالة التطبيق")
public class HealthController {

    @Operation(
        summary = "فحص حالة التطبيق",
        description = "فحص حالة التطبيق والتأكد من عمله بشكل صحيح"
    )
    @ApiResponse(
        responseCode = "200",
        description = "التطبيق يعمل بشكل طبيعي",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Map.class),
            examples = @ExampleObject(
                name = "Health Check Response",
                value = """
                {
                    "status": "UP",
                    "message": "Twsela application is running",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "version": "1.0.1",
                    "environment": "development"
                }
                """
            )
        )
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Twsela application is running");
        response.put("timestamp", java.time.Instant.now());
        response.put("version", "1.0.1");
        response.put("environment", "development");
        return ResponseEntity.ok(response);
    }
}
