package com.twsela.service;

import com.twsela.domain.Notification;
import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationType;
import com.twsela.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private NotificationService notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = new Notification(1L, NotificationType.SHIPMENT_STATUS, "عنوان", "رسالة");
        sampleNotification.setId(10L);
        sampleNotification.setChannel(NotificationChannel.IN_APP);
        sampleNotification.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("send() saves notification and pushes via WebSocket")
    void send_savesAndPushes() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(10L);
            n.setCreatedAt(Instant.now());
            return n;
        });

        Notification result = notificationService.send(1L, NotificationType.SHIPMENT_STATUS, "عنوان", "رسالة", "/shipments/1");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/1"), any(Map.class));
    }

    @Test
    @DisplayName("send() without actionUrl defaults to null")
    void send_withoutActionUrl() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(11L);
            n.setCreatedAt(Instant.now());
            return n;
        });

        Notification result = notificationService.send(1L, NotificationType.WELCOME, "مرحبا", "أهلا بك");

        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("send() continues even if WebSocket push fails")
    void send_websocketFailure_stillSaves() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(12L);
            n.setCreatedAt(Instant.now());
            return n;
        });
        doThrow(new RuntimeException("ws error")).when(messagingTemplate)
                .convertAndSend(anyString(), any(Map.class));

        Notification result = notificationService.send(1L, NotificationType.SYSTEM_ALERT, "تنبيه", "خطأ");

        assertNotNull(result);
        assertEquals(12L, result.getId());
    }

    @Test
    @DisplayName("getAll() returns paginated results")
    void getAll_returnsPaginated() {
        Page<Notification> page = new PageImpl<>(List.of(sampleNotification), PageRequest.of(0, 20), 1);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any())).thenReturn(page);

        Page<Notification> result = notificationService.getAll(1L, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(sampleNotification, result.getContent().get(0));
    }

    @Test
    @DisplayName("getUnread() returns unread notifications")
    void getUnread_returnsUnread() {
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(sampleNotification));

        List<Notification> result = notificationService.getUnread(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getUnreadCount() returns count")
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(5L);

        assertEquals(5L, notificationService.getUnreadCount(1L));
    }

    @Test
    @DisplayName("markAsRead() marks notification as read if owned by user")
    void markAsRead_success() {
        sampleNotification.setRead(false);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = notificationService.markAsRead(10L, 1L);

        assertTrue(result);
        assertTrue(sampleNotification.isRead());
        assertNotNull(sampleNotification.getReadAt());
    }

    @Test
    @DisplayName("markAsRead() returns false for wrong user")
    void markAsRead_wrongUser() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(sampleNotification));

        boolean result = notificationService.markAsRead(10L, 999L);

        assertFalse(result);
    }

    @Test
    @DisplayName("markAsRead() returns false for non-existent notification")
    void markAsRead_notFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = notificationService.markAsRead(999L, 1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("markAllAsRead() delegates to repository")
    void markAllAsRead_delegatesToRepo() {
        when(notificationRepository.markAllAsReadByUserId(1L)).thenReturn(3);

        int result = notificationService.markAllAsRead(1L);

        assertEquals(3, result);
    }

    @Test
    @DisplayName("broadcastShipmentUpdate() sends to all relevant topics")
    void broadcastShipmentUpdate_sendsToAllTopics() {
        notificationService.broadcastShipmentUpdate(100L, "CREATED", "PICKED_UP", 2L, 3L);

        verify(messagingTemplate).convertAndSend(eq("/topic/shipment/100"), any(Map.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/dashboard/stats/2"), any(Map.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/courier/3"), any(Map.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/dashboard/stats/admin"), any(Map.class));
    }

    @Test
    @DisplayName("broadcastShipmentUpdate() skips null merchant/courier")
    void broadcastShipmentUpdate_skipsNull() {
        notificationService.broadcastShipmentUpdate(100L, "CREATED", "PICKED_UP", null, null);

        verify(messagingTemplate).convertAndSend(eq("/topic/shipment/100"), any(Map.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/dashboard/stats/admin"), any(Map.class));
        verify(messagingTemplate, never()).convertAndSend(startsWith("/topic/dashboard/stats/2"), any(Map.class));
        verify(messagingTemplate, never()).convertAndSend(startsWith("/topic/courier/"), any(Map.class));
    }

    @Test
    @DisplayName("broadcastDashboardUpdate() sends stats update to admin topic")
    void broadcastDashboardUpdate_sendsToAdmin() {
        notificationService.broadcastDashboardUpdate("totalShipments", 10, 11);

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/dashboard/stats/admin"), captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertEquals("STATS_UPDATE", payload.get("type"));
        assertEquals("totalShipments", payload.get("metric"));
    }
}
