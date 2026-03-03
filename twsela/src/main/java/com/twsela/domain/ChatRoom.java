package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * غرفة محادثة مرتبطة بشحنة — تجمع أطراف التوصيل.
 */
@Entity
@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_cr_shipment", columnList = "shipment_id"),
        @Index(name = "idx_cr_status", columnList = "status")
})
public class ChatRoom {

    public enum RoomType { MERCHANT_COURIER, RECIPIENT_COURIER, SUPPORT }
    public enum RoomStatus { ACTIVE, ARCHIVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "participants", columnDefinition = "TEXT")
    private String participants;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status = RoomStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ──
    public ChatRoom() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoom that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
