package com.twsela.service;

import com.twsela.domain.ChatMessage;
import com.twsela.domain.ChatRoom;
import com.twsela.domain.Shipment;
import com.twsela.domain.User;
import com.twsela.repository.ChatMessageRepository;
import com.twsela.repository.ChatRoomRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * خدمة المحادثات — إنشاء غرف وإرسال رسائل مع بث WebSocket.
 */
@Service
@Transactional
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       ShipmentRepository shipmentRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * إنشاء غرفة محادثة جديدة لشحنة.
     */
    public ChatRoom createRoom(Long shipmentId, ChatRoom.RoomType roomType, String participants) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        ChatRoom room = new ChatRoom();
        room.setShipment(shipment);
        room.setRoomType(roomType);
        room.setParticipants(participants);
        room.setStatus(ChatRoom.RoomStatus.ACTIVE);
        room.setCreatedAt(Instant.now());

        ChatRoom saved = chatRoomRepository.save(room);
        log.info("Chat room {} created for shipment {} (type: {})", saved.getId(), shipmentId, roomType);
        return saved;
    }

    /**
     * إرسال رسالة في غرفة محادثة.
     */
    public ChatMessage sendMessage(Long roomId, Long senderId, String content,
                                    ChatMessage.MessageType messageType) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));

        if (room.getStatus() != ChatRoom.RoomStatus.ACTIVE) {
            throw new IllegalStateException("Chat room is not active");
        }

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setSender(sender);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setSentAt(Instant.now());

        ChatMessage saved = chatMessageRepository.save(message);

        // Broadcast message via WebSocket
        broadcastMessage(room, saved, sender);

        log.debug("Message {} sent in room {} by user {}", saved.getId(), roomId, senderId);
        return saved;
    }

    /**
     * الحصول على رسائل غرفة المحادثة.
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(roomId);
    }

    /**
     * الحصول على غرف المحادثة لشحنة.
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getRoomsForShipment(Long shipmentId) {
        return chatRoomRepository.findByShipmentId(shipmentId);
    }

    /**
     * الحصول على غرف المحادثة لمستخدم.
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getRoomsForUser(Long userId) {
        return chatRoomRepository.findByParticipantsContaining(String.valueOf(userId));
    }

    /**
     * أرشفة غرفة محادثة.
     */
    public ChatRoom archiveRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));
        room.setStatus(ChatRoom.RoomStatus.ARCHIVED);
        log.info("Chat room {} archived", roomId);
        return chatRoomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public ChatRoom getRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));
    }

    private void broadcastMessage(ChatRoom room, ChatMessage message, User sender) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/chat/" + room.getId(),
                Map.of(
                    "messageId", message.getId(),
                    "roomId", room.getId(),
                    "senderId", sender.getId(),
                    "senderName", sender.getName(),
                    "content", message.getContent(),
                    "messageType", message.getMessageType().name(),
                    "sentAt", message.getSentAt().toString()
                )
            );
        } catch (Exception e) {
            log.warn("Failed to broadcast chat message {} in room {}: {}", message.getId(), room.getId(), e.getMessage());
        }
    }
}
