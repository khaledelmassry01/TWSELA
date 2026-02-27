package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "notification_log", indexes = {
    @Index(name = "idx_notif_recipient_phone", columnList = "recipient_phone"),
    @Index(name = "idx_notif_sent_at", columnList = "sent_at"),
    @Index(name = "idx_notif_type_status", columnList = "message_type, status")
})
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "message_type", nullable = false, length = 50)
    private String messageType;

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();

    @Column(name = "status", length = 20)
    private String status = "SENT";

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    // Constructors
    public NotificationLog() {}

    public NotificationLog(String recipientPhone, String messageType, String messageContent) {
        this.recipientPhone = recipientPhone;
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.sentAt = Instant.now();
        this.status = "SENT";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    @Override
    public String toString() {
        return "NotificationLog{" +
                "id=" + id +
                ", recipientPhone='" + recipientPhone + '\'' +
                ", messageType='" + messageType + '\'' +
                ", messageContent='" + messageContent + '\'' +
                ", sentAt=" + sentAt +
                ", status='" + status + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationLog)) return false;
        NotificationLog that = (NotificationLog) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
