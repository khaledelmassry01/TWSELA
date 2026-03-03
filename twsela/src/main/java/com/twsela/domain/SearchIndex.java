package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "search_indexes")
public class SearchIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;

    @Column(columnDefinition = "TEXT")
    private String fields;

    @Column(nullable = false, length = 10)
    private String language = "BOTH";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_rebuilt_at")
    private Instant lastRebuiltAt;

    @Column(name = "document_count", nullable = false)
    private Long documentCount = 0L;

    @Column(name = "rebuild_cron_expression", length = 50)
    private String rebuildCronExpression;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getFields() { return fields; }
    public void setFields(String fields) { this.fields = fields; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Instant getLastRebuiltAt() { return lastRebuiltAt; }
    public void setLastRebuiltAt(Instant lastRebuiltAt) { this.lastRebuiltAt = lastRebuiltAt; }
    public Long getDocumentCount() { return documentCount; }
    public void setDocumentCount(Long documentCount) { this.documentCount = documentCount; }
    public String getRebuildCronExpression() { return rebuildCronExpression; }
    public void setRebuildCronExpression(String rebuildCronExpression) { this.rebuildCronExpression = rebuildCronExpression; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
