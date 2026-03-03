package com.twsela.service;

import com.twsela.domain.LiveNotification;
import com.twsela.domain.User;
import com.twsela.repository.LiveNotificationRepository;
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
class LiveNotificationServiceTest {

    @Mock private LiveNotificationRepository liveNotificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private LiveNotificationService liveNotificationService;

    private User user;
    private LiveNotification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setPhone("01012345678");
        user.setName("مستخدم");

        notification = new LiveNotification();
        notification.setId(10L);
        notification.setUser(user);
        notification.setType("SHIPMENT_UPDATE");
        notification.setTitle("تحديث شحنة");
        notification.setBody("تم تسليم الشحنة");
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("send() saves notification and pushes via WebSocket")
    void send_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(liveNotificationRepository.save(any(LiveNotification.class))).thenAnswer(inv -> {
            LiveNotification n = inv.getArgument(0);
            n.setId(10L);
            n.setCreatedAt(Instant.now());
            return n;
        });

        LiveNotification result = liveNotificationService.send(1L, "SHIPMENT_UPDATE", "تحديث", "تم التسليم", null);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(liveNotificationRepository, atLeast(1)).save(any(LiveNotification.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/live-notifications/1"), any(Map.class));
    }

    @Test
    @DisplayName("send() throws for unknown user")
    void send_userNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> liveNotificationService.send(999L, "TYPE", "Title", "Body", null));
    }

    @Test
    @DisplayName("send() continues if WebSocket fails")
    void send_websocketFails() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(liveNotificationRepository.save(any())).thenAnswer(inv -> {
            LiveNotification n = inv.getArgument(0);
            n.setId(11L);
            n.setCreatedAt(Instant.now());
            return n;
        });
        doThrow(new RuntimeException("ws error")).when(messagingTemplate)
                .convertAndSend(anyString(), any(Map.class));

        LiveNotification result = liveNotificationService.send(1L, "TYPE", "Title", "Body", null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getUnread() returns unread notifications")
    void getUnread_returnsUnread() {
        when(liveNotificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<LiveNotification> result = liveNotificationService.getUnread(1L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    @DisplayName("getUnreadCount() returns count")
    void getUnreadCount_returnsCount() {
        when(liveNotificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(5L);

        assertEquals(5L, liveNotificationService.getUnreadCount(1L));
    }

    @Test
    @DisplayName("markAsRead() marks notification as read")
    void markAsRead_success() {
        when(liveNotificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(liveNotificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LiveNotification result = liveNotificationService.markAsRead(10L);

        assertTrue(result.isRead());
        assertNotNull(result.getReadAt());
    }

    @Test
    @DisplayName("markAsRead() throws for unknown notification")
    void markAsRead_notFound() {
        when(liveNotificationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> liveNotificationService.markAsRead(999L));
    }

    @Test
    @DisplayName("markAllAsRead() marks all unread notifications as read")
    void markAllAsRead_success() {
        LiveNotification n1 = new LiveNotification();
        n1.setId(1L);
        n1.setRead(false);
        LiveNotification n2 = new LiveNotification();
        n2.setId(2L);
        n2.setRead(false);

        when(liveNotificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n1, n2));

        liveNotificationService.markAllAsRead(1L);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(liveNotificationRepository).saveAll(anyList());
    }
}
