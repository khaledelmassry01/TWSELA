package com.twsela.web.dto;

import java.time.Instant;

/**
 * DTO for Notification responses.
 */
public class NotificationDTO {
    private Long id;
    private String type;
    private String channel;
    private String title;
    private String message;
    private String actionUrl;
    private boolean read;
    private Instant createdAt;
    private Instant readAt;

    public NotificationDTO() {}

    public NotificationDTO(Long id, String type, String channel, String title, String message,
                           String actionUrl, boolean read, Instant createdAt, Instant readAt) {
        this.id = id;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.actionUrl = actionUrl;
        this.read = read;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
}
