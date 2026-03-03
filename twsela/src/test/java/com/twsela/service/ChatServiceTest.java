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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private ChatService chatService;

    private Shipment shipment;
    private User sender;
    private ChatRoom activeRoom;

    @BeforeEach
    void setUp() {
        shipment = new Shipment();
        shipment.setId(1L);

        sender = new User();
        sender.setId(10L);
        sender.setPhone("01012345678");
        sender.setName("مرسل");

        activeRoom = new ChatRoom();
        activeRoom.setId(50L);
        activeRoom.setShipment(shipment);
        activeRoom.setRoomType(ChatRoom.RoomType.MERCHANT_COURIER);
        activeRoom.setStatus(ChatRoom.RoomStatus.ACTIVE);
        activeRoom.setParticipants("10,20");
        activeRoom.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("createRoom() creates a new chat room")
    void createRoom_success() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(inv -> {
            ChatRoom r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });

        ChatRoom result = chatService.createRoom(1L, ChatRoom.RoomType.MERCHANT_COURIER, "10,20");

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals(ChatRoom.RoomType.MERCHANT_COURIER, result.getRoomType());
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("createRoom() throws when shipment not found")
    void createRoom_shipmentNotFound() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> chatService.createRoom(999L, ChatRoom.RoomType.SUPPORT, "10"));
    }

    @Test
    @DisplayName("sendMessage() sends a message and broadcasts via WebSocket")
    void sendMessage_success() {
        when(chatRoomRepository.findById(50L)).thenReturn(Optional.of(activeRoom));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        ChatMessage result = chatService.sendMessage(50L, 10L, "مرحبا", ChatMessage.MessageType.TEXT);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("مرحبا", result.getContent());
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/50"), any(Map.class));
    }

    @Test
    @DisplayName("sendMessage() throws if room is archived")
    void sendMessage_roomArchived() {
        activeRoom.setStatus(ChatRoom.RoomStatus.ARCHIVED);
        when(chatRoomRepository.findById(50L)).thenReturn(Optional.of(activeRoom));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sender));

        assertThrows(IllegalStateException.class,
                () -> chatService.sendMessage(50L, 10L, "مرحبا", ChatMessage.MessageType.TEXT));
    }

    @Test
    @DisplayName("sendMessage() throws if room not found")
    void sendMessage_roomNotFound() {
        when(chatRoomRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> chatService.sendMessage(999L, 10L, "مرحبا", ChatMessage.MessageType.TEXT));
    }

    @Test
    @DisplayName("sendMessage() continues if WebSocket broadcast fails")
    void sendMessage_broadcastFails() {
        when(chatRoomRepository.findById(50L)).thenReturn(Optional.of(activeRoom));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any())).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setId(2L);
            return m;
        });
        doThrow(new RuntimeException("ws error")).when(messagingTemplate)
                .convertAndSend(anyString(), any(Map.class));

        ChatMessage result = chatService.sendMessage(50L, 10L, "رسالة", ChatMessage.MessageType.TEXT);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getMessages() returns messages in order")
    void getMessages_returnsOrdered() {
        ChatMessage m1 = new ChatMessage();
        m1.setId(1L);
        m1.setContent("أولى");
        ChatMessage m2 = new ChatMessage();
        m2.setId(2L);
        m2.setContent("ثانية");

        when(chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(50L)).thenReturn(List.of(m1, m2));

        List<ChatMessage> result = chatService.getMessages(50L);

        assertEquals(2, result.size());
        assertEquals("أولى", result.get(0).getContent());
    }

    @Test
    @DisplayName("getRoomsForShipment() returns rooms")
    void getRoomsForShipment_returnsRooms() {
        when(chatRoomRepository.findByShipmentId(1L)).thenReturn(List.of(activeRoom));

        List<ChatRoom> result = chatService.getRoomsForShipment(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("archiveRoom() sets status to ARCHIVED")
    void archiveRoom_success() {
        when(chatRoomRepository.findById(50L)).thenReturn(Optional.of(activeRoom));
        when(chatRoomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChatRoom result = chatService.archiveRoom(50L);

        assertEquals(ChatRoom.RoomStatus.ARCHIVED, result.getStatus());
    }

    @Test
    @DisplayName("archiveRoom() throws for unknown room")
    void archiveRoom_notFound() {
        when(chatRoomRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chatService.archiveRoom(999L));
    }
}
