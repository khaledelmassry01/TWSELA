package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Logs each API call made via an ApiKey for analytics and auditing.
 */
@Entity
@Table(name = "api_key_usage_log")
public class ApiKeyUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key_id", nullable = false)
    private Long apiKeyId;

    @Column(name = "endpoint", nullable = false, length = 255)
    private String endpoint;

    @Column(name = "method", nullable = false, length = 10)
    private String method;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(Long apiKeyId) { this.apiKeyId = apiKeyId; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public int getResponseStatus() { return responseStatus; }
    public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
}
