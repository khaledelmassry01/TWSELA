package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_usage_logs")
public class DataUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @Size(max = 100)
    @Column(length = 100)
    private String deviceId;

    @Size(max = 255)
    @Column(length = 255)
    private String endpoint;

    @Column(nullable = false)
    private Long bytesUp = 0L;

    @Column(nullable = false)
    private Long bytesDown = 0L;

    private LocalDateTime recordedAt;
    private Long tenantId;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public Long getBytesUp() { return bytesUp; }
    public void setBytesUp(Long bytesUp) { this.bytesUp = bytesUp; }
    public Long getBytesDown() { return bytesDown; }
    public void setBytesDown(Long bytesDown) { this.bytesDown = bytesDown; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
