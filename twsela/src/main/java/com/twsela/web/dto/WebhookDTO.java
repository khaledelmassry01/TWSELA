package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * DTOs for webhook management.
 */
public class WebhookDTO {

    private Long id;
    private String url;
    private List<String> events;
    private boolean active;
    private Instant createdAt;
    private long totalEvents;
    private long failedEvents;

    // ── Getters & Setters ───────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public long getTotalEvents() { return totalEvents; }
    public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }

    public long getFailedEvents() { return failedEvents; }
    public void setFailedEvents(long failedEvents) { this.failedEvents = failedEvents; }

    // ── Create request ──────────────────────────────────────────

    public static class CreateWebhookRequest {
        @NotBlank(message = "عنوان URL مطلوب")
        @Size(max = 500)
        private String url;

        @NotNull(message = "يجب تحديد الأحداث")
        private List<String> events;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public List<String> getEvents() { return events; }
        public void setEvents(List<String> events) { this.events = events; }
    }

    // ── Event DTO ───────────────────────────────────────────────

    public static class WebhookEventDTO {
        private Long id;
        private String eventType;
        private String status;
        private int attempts;
        private Integer responseCode;
        private Instant createdAt;
        private Instant lastAttemptAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }

        public Integer getResponseCode() { return responseCode; }
        public void setResponseCode(Integer responseCode) { this.responseCode = responseCode; }

        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

        public Instant getLastAttemptAt() { return lastAttemptAt; }
        public void setLastAttemptAt(Instant lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    }
}
