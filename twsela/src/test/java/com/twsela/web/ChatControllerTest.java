package com.twsela.web;

import com.twsela.domain.ChatMessage;
import com.twsela.domain.ChatRoom;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ChatController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(ChatControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة تحكم المحادثات")
class ChatControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private ChatService chatService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("يجب إنشاء غرفة محادثة جديدة")
    void createRoom() throws Exception {
        ChatRoom room = new ChatRoom();
        room.setId(50L);
        room.setRoomType(ChatRoom.RoomType.MERCHANT_COURIER);
        room.setStatus(ChatRoom.RoomStatus.ACTIVE);

        when(chatService.createRoom(eq(1L), eq(ChatRoom.RoomType.MERCHANT_COURIER), eq("10,20")))
                .thenReturn(room);

        mockMvc.perform(post("/api/chat/rooms")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shipmentId\": 1, \"roomType\": \"MERCHANT_COURIER\", \"participants\": \"10,20\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomId").value(50));
    }

    @Test
    @DisplayName("يجب إرسال رسالة في غرفة محادثة")
    void sendMessage() throws Exception {
        ChatMessage message = new ChatMessage();
        message.setId(1L);
        message.setSentAt(Instant.now());

        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(10L);
        when(chatService.sendMessage(eq(50L), eq(10L), eq("مرحبا"), eq(ChatMessage.MessageType.TEXT)))
                .thenReturn(message);

        mockMvc.perform(post("/api/chat/messages")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\": 50, \"content\": \"مرحبا\", \"messageType\": \"TEXT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageId").value(1));
    }

    @Test
    @DisplayName("يجب عرض رسائل غرفة المحادثة")
    void getMessages() throws Exception {
        ChatMessage m1 = new ChatMessage();
        m1.setId(1L);
        m1.setContent("مرحبا");
        m1.setMessageType(ChatMessage.MessageType.TEXT);
        m1.setSentAt(Instant.now());

        when(chatService.getMessages(50L)).thenReturn(List.of(m1));

        mockMvc.perform(get("/api/chat/rooms/50/messages")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب عرض غرف المحادثة لشحنة")
    void getRoomsForShipment() throws Exception {
        ChatRoom room = new ChatRoom();
        room.setId(50L);
        room.setRoomType(ChatRoom.RoomType.MERCHANT_COURIER);
        room.setStatus(ChatRoom.RoomStatus.ACTIVE);
        room.setCreatedAt(Instant.now());

        when(chatService.getRoomsForShipment(1L)).thenReturn(List.of(room));

        mockMvc.perform(get("/api/chat/rooms/shipment/1")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب أرشفة غرفة محادثة")
    void archiveRoom() throws Exception {
        ChatRoom room = new ChatRoom();
        room.setId(50L);
        room.setStatus(ChatRoom.RoomStatus.ARCHIVED);

        when(chatService.archiveRoom(50L)).thenReturn(room);

        mockMvc.perform(post("/api/chat/rooms/50/archive")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }
}
