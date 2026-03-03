package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 100)
    @Column(name = "name_ar", length = 100)
    private String nameAr;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Size(max = 20)
    @Column(name = "campaign_type", nullable = false, length = 20)
    private String campaignType;

    @NotBlank
    @Size(max = 30)
    @Column(name = "target_audience", nullable = false, length = 30)
    private String targetAudience;

    @Column(name = "target_criteria", columnDefinition = "TEXT")
    private String targetCriteria;

    @Column(name = "promo_code_id")
    private Long promoCodeId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_ar", columnDefinition = "TEXT")
    private String messageAr;

    @NotBlank
    @Size(max = 10)
    @Column(name = "channel", nullable = false, length = 10)
    private String channel = "ALL";

    @NotBlank
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @NotNull
    @Min(0)
    @Column(name = "total_targets", nullable = false)
    private Integer totalTargets = 0;

    @NotNull
    @Min(0)
    @Column(name = "total_sent", nullable = false)
    private Integer totalSent = 0;

    @NotNull
    @Min(0)
    @Column(name = "total_opened", nullable = false)
    private Integer totalOpened = 0;

    @NotNull
    @Min(0)
    @Column(name = "total_converted", nullable = false)
    private Integer totalConverted = 0;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCampaignType() { return campaignType; }
    public void setCampaignType(String campaignType) { this.campaignType = campaignType; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getTargetCriteria() { return targetCriteria; }
    public void setTargetCriteria(String targetCriteria) { this.targetCriteria = targetCriteria; }

    public Long getPromoCodeId() { return promoCodeId; }
    public void setPromoCodeId(Long promoCodeId) { this.promoCodeId = promoCodeId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMessageAr() { return messageAr; }
    public void setMessageAr(String messageAr) { this.messageAr = messageAr; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getTotalTargets() { return totalTargets; }
    public void setTotalTargets(Integer totalTargets) { this.totalTargets = totalTargets; }

    public Integer getTotalSent() { return totalSent; }
    public void setTotalSent(Integer totalSent) { this.totalSent = totalSent; }

    public Integer getTotalOpened() { return totalOpened; }
    public void setTotalOpened(Integer totalOpened) { this.totalOpened = totalOpened; }

    public Integer getTotalConverted() { return totalConverted; }
    public void setTotalConverted(Integer totalConverted) { this.totalConverted = totalConverted; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
