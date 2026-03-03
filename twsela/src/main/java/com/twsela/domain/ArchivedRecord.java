package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "archived_records")
public class ArchivedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "original_table", nullable = false, length = 100)
    private String originalTable;

    @NotNull
    @Column(name = "original_id", nullable = false)
    private Long originalId;

    @NotBlank
    @Column(name = "archived_data", nullable = false, columnDefinition = "TEXT")
    private String archivedData;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "archive_policy_id")
    private Long archivePolicyId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.archivedAt == null) {
            this.archivedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOriginalTable() { return originalTable; }
    public void setOriginalTable(String originalTable) { this.originalTable = originalTable; }
    public Long getOriginalId() { return originalId; }
    public void setOriginalId(Long originalId) { this.originalId = originalId; }
    public String getArchivedData() { return archivedData; }
    public void setArchivedData(String archivedData) { this.archivedData = archivedData; }
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
    public Long getArchivePolicyId() { return archivePolicyId; }
    public void setArchivePolicyId(Long archivePolicyId) { this.archivePolicyId = archivePolicyId; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
