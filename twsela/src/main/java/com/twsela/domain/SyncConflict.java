package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_conflicts")
public class SyncConflict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long syncSessionId;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String entityType;

    @NotNull
    @Column(nullable = false)
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String localData;

    @Column(columnDefinition = "TEXT")
    private String serverData;

    @Size(max = 20)
    @Column(length = 20)
    private String resolution;

    private LocalDateTime resolvedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSyncSessionId() { return syncSessionId; }
    public void setSyncSessionId(Long syncSessionId) { this.syncSessionId = syncSessionId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getLocalData() { return localData; }
    public void setLocalData(String localData) { this.localData = localData; }
    public String getServerData() { return serverData; }
    public void setServerData(String serverData) { this.serverData = serverData; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
