package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * قالب جاهز لسلسلة عمل — يمكن استنساخه لإنشاء سلسلة جديدة.
 */
@Entity
@Table(name = "workflow_templates", indexes = {
        @Index(name = "idx_wf_tmpl_category", columnList = "category"),
        @Index(name = "idx_wf_tmpl_system", columnList = "is_system")
})
public class WorkflowTemplate {

    public enum TemplateCategory {
        SHIPMENT, PAYMENT, NOTIFICATION, ASSIGNMENT, RETURN, SUPPORT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_ar", length = 100)
    private String nameAr;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateCategory category;

    @Column(name = "template_definition", columnDefinition = "TEXT")
    private String templateDefinition;

    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    @Column(name = "usage_count", nullable = false)
    private long usageCount = 0;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionAr() { return descriptionAr; }
    public void setDescriptionAr(String descriptionAr) { this.descriptionAr = descriptionAr; }

    public TemplateCategory getCategory() { return category; }
    public void setCategory(TemplateCategory category) { this.category = category; }

    public String getTemplateDefinition() { return templateDefinition; }
    public void setTemplateDefinition(String templateDefinition) { this.templateDefinition = templateDefinition; }

    public boolean isSystem() { return isSystem; }
    public void setSystem(boolean system) { isSystem = system; }

    public long getUsageCount() { return usageCount; }
    public void setUsageCount(long usageCount) { this.usageCount = usageCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowTemplate that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
