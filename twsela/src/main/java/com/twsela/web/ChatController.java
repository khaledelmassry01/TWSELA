package com.twsela.web;

import com.twsela.domain.ChatMessage;
import com.twsela.domain.ChatRoom;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.ChatService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.CreateChatRoomRequest;
import com.twsela.web.dto.SendChatMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "نظام المحادثات")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final AuthenticationHelper authenticationHelper;

    public ChatController(ChatService chatService,
                          AuthenticationHelper authenticationHelper) {
        this.chatService = chatService;
        this.authenticationHelper = authenticationHelper;
    }

    @Operation(summary = "إنشاء غرفة محادثة جديدة لشحنة")
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRoom(
            @Valid @RequestBody CreateChatRoomRequest request) {
        ChatRoom.RoomType roomType = ChatRoom.RoomType.valueOf(request.getRoomType());
        ChatRoom room = chatService.createRoom(request.getShipmentId(), roomType, request.getParticipants());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "roomId", room.getId(),
                "shipmentId", request.getShipmentId(),
                "roomType", room.getRoomType().name(),
                "status", room.getStatus().name()
        ), "تم إنشاء غرفة المحادثة"));
    }

    @Operation(summary = "إرسال رسالة في غرفة محادثة")
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendMessage(
            @Valid @RequestBody SendChatMessageRequest request,
            Authentication authentication) {
        Long senderId = authenticationHelper.getCurrentUserId(authentication);
        ChatMessage.MessageType type = ChatMessage.MessageType.valueOf(request.getMessageType());
        ChatMessage message = chatService.sendMessage(request.getRoomId(), senderId, request.getContent(), type);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "messageId", message.getId(),
                "roomId", request.getRoomId(),
                "sentAt", message.getSentAt().toString()
        ), "تم إرسال الرسالة"));
    }

    @Operation(summary = "الحصول على رسائل غرفة المحادثة")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages(
            @PathVariable Long roomId) {
        List<ChatMessage> messages = chatService.getMessages(roomId);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @Operation(summary = "الحصول على غرف المحادثة لشحنة")
    @GetMapping("/rooms/shipment/{shipmentId}")
    public ResponseEntity<ApiResponse<List<ChatRoom>>> getRoomsForShipment(
            @PathVariable Long shipmentId) {
        List<ChatRoom> rooms = chatService.getRoomsForShipment(shipmentId);
        return ResponseEntity.ok(ApiResponse.ok(rooms));
    }

    @Operation(summary = "الحصول على غرف المحادثة الخاصة بي")
    @GetMapping("/rooms/my")
    public ResponseEntity<ApiResponse<List<ChatRoom>>> getMyRooms(
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        List<ChatRoom> rooms = chatService.getRoomsForUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(rooms));
    }

    @Operation(summary = "أرشفة غرفة محادثة")
    @PostMapping("/rooms/{roomId}/archive")
    public ResponseEntity<ApiResponse<Map<String, Object>>> archiveRoom(
            @PathVariable Long roomId) {
        ChatRoom room = chatService.archiveRoom(roomId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "roomId", room.getId(),
                "status", room.getStatus().name()
        ), "تم أرشفة غرفة المحادثة"));
    }
}
