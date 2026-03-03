package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * رسالة داخل غرفة محادثة — نصية أو صورة أو موقع أو نظام.
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_cm_room", columnList = "chat_room_id"),
        @Index(name = "idx_cm_sent", columnList = "sent_at")
})
public class ChatMessage {

    public enum MessageType { TEXT, IMAGE, LOCATION, SYSTEM }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "read_by", columnDefinition = "TEXT")
    private String readBy;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();

    // ── Constructors ──
    public ChatMessage() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChatRoom getChatRoom() { return chatRoom; }
    public void setChatRoom(ChatRoom chatRoom) { this.chatRoom = chatRoom; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getReadBy() { return readBy; }
    public void setReadBy(String readBy) { this.readBy = readBy; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
