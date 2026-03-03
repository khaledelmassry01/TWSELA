package com.twsela.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private PresenceService presenceService;

    @Test
    @DisplayName("userConnected() marks user as online and broadcasts")
    void userConnected_success() {
        presenceService.userConnected(1L);

        assertTrue(presenceService.isOnline(1L));
        verify(messagingTemplate).convertAndSend(eq("/topic/presence"), any(Map.class));
    }

    @Test
    @DisplayName("userDisconnected() removes user from online list")
    void userDisconnected_success() {
        presenceService.userConnected(1L);
        presenceService.userDisconnected(1L);

        assertFalse(presenceService.isOnline(1L));
    }

    @Test
    @DisplayName("isOnline() returns false for unknown user")
    void isOnline_unknownUser() {
        assertFalse(presenceService.isOnline(999L));
    }

    @Test
    @DisplayName("getOnlineUsers() returns all connected users")
    void getOnlineUsers_returnsAll() {
        presenceService.userConnected(1L);
        presenceService.userConnected(2L);
        presenceService.userConnected(3L);

        Set<Long> online = presenceService.getOnlineUsers();

        assertEquals(3, online.size());
        assertTrue(online.contains(1L));
        assertTrue(online.contains(2L));
        assertTrue(online.contains(3L));
    }

    @Test
    @DisplayName("getOnlineCount() returns correct count")
    void getOnlineCount_returnsCount() {
        presenceService.userConnected(1L);
        presenceService.userConnected(2L);

        assertEquals(2, presenceService.getOnlineCount());
    }

    @Test
    @DisplayName("getLastSeen() returns timestamp for connected user")
    void getLastSeen_returnsTimestamp() {
        presenceService.userConnected(1L);

        Instant lastSeen = presenceService.getLastSeen(1L);

        assertNotNull(lastSeen);
    }

    @Test
    @DisplayName("getLastSeen() returns null for disconnected user")
    void getLastSeen_disconnected() {
        assertNull(presenceService.getLastSeen(999L));
    }
}
