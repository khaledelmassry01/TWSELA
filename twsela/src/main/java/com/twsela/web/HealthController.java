package com.twsela.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health Check Controller for monitoring application status.
 * This endpoint is public and doesn't require authentication.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "فحص حالة التطبيق")
public class HealthController {

    @Value("${app.version:1.0.1}")
    private String appVersion;

    @Value("${spring.profiles.active:development}")
    private String activeProfile;

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    public HealthController(DataSource dataSource, @Nullable StringRedisTemplate redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

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
                    "environment": "development",
                    "components": {
                        "database": { "status": "UP" },
                        "redis":    { "status": "UP" }
                    }
                }
                """
            )
        )
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("message", "Twsela application is running");
        response.put("timestamp", java.time.Instant.now());
        response.put("version", appVersion);
        response.put("environment", activeProfile);

        Map<String, Object> components = new LinkedHashMap<>();
        components.put("database", checkDatabase());
        components.put("redis", checkRedis());
        response.put("components", components);

        return ResponseEntity.ok(response);
    }

    // ---- component checks ----

    private Map<String, Object> checkDatabase() {
        Map<String, Object> db = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            db.put("status", "UP");
        } catch (Exception e) {
            db.put("status", "DOWN");
            db.put("error", e.getMessage());
        }
        return db;
    }

    private Map<String, Object> checkRedis() {
        Map<String, Object> redis = new LinkedHashMap<>();
        if (redisTemplate == null || redisTemplate.getConnectionFactory() == null) {
            redis.put("status", "NOT_CONFIGURED");
            return redis;
        }
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            redis.put("status", "UP");
        } catch (Exception e) {
            redis.put("status", "DOWN");
            redis.put("error", e.getMessage());
        }
        return redis;
    }
}
