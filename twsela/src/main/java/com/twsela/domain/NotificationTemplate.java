package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Template for notification messages. Each event type can have a template per channel.
 * Supports Arabic and English body templates with variable substitution.
 */
@Entity
@Table(name = "notification_templates", indexes = {
    @Index(name = "idx_nt_event_type", columnList = "event_type"),
    @Index(name = "idx_nt_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_nt_event_channel", columnNames = {"event_type", "channel"})
})
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private NotificationType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "subject_template", length = 255)
    private String subjectTemplate;

    @Column(name = "body_template_ar", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplateAr;

    @Column(name = "body_template_en", columnDefinition = "TEXT")
    private String bodyTemplateEn;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    // Constructors
    public NotificationTemplate() {}

    public NotificationTemplate(NotificationType eventType, NotificationChannel channel,
                                 String bodyTemplateAr, String bodyTemplateEn) {
        this.eventType = eventType;
        this.channel = channel;
        this.bodyTemplateAr = bodyTemplateAr;
        this.bodyTemplateEn = bodyTemplateEn;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NotificationType getEventType() { return eventType; }
    public void setEventType(NotificationType eventType) { this.eventType = eventType; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getSubjectTemplate() { return subjectTemplate; }
    public void setSubjectTemplate(String subjectTemplate) { this.subjectTemplate = subjectTemplate; }

    public String getBodyTemplateAr() { return bodyTemplateAr; }
    public void setBodyTemplateAr(String bodyTemplateAr) { this.bodyTemplateAr = bodyTemplateAr; }

    public String getBodyTemplateEn() { return bodyTemplateEn; }
    public void setBodyTemplateEn(String bodyTemplateEn) { this.bodyTemplateEn = bodyTemplateEn; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
