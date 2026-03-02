package com.twsela.service;

import com.twsela.domain.Notification;
import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationType;
import com.twsela.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Manages in-app notifications with real-time WebSocket delivery.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Create and send an in-app notification.
     */
    @Transactional
    public Notification send(Long userId, NotificationType type, String title, String message, String actionUrl) {
        Notification notification = new Notification(userId, type, title, message);
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setActionUrl(actionUrl);
        notification = notificationRepository.save(notification);

        // Push via WebSocket
        try {
            messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                Map.of(
                    "id", notification.getId(),
                    "type", type.name(),
                    "title", title,
                    "message", message,
                    "actionUrl", actionUrl != null ? actionUrl : "",
                    "createdAt", notification.getCreatedAt().toString()
                )
            );
        } catch (Exception e) {
            log.warn("Failed to push WebSocket notification to user {}: {}", userId, e.getMessage());
        }

        log.debug("Notification sent to user {}: {} - {}", userId, type, title);
        return notification;
    }

    /**
     * Send notification without action URL.
     */
    @Transactional
    public Notification send(Long userId, NotificationType type, String title, String message) {
        return send(userId, type, title, message, null);
    }

    /**
     * Get all notifications for a user (paginated).
     */
    public Page<Notification> getAll(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Get unread notifications for a user.
     */
    public List<Notification> getUnread(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Count unread notifications.
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Mark a single notification as read.
     */
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .map(n -> {
                    n.setRead(true);
                    n.setReadAt(Instant.now());
                    notificationRepository.save(n);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Mark all notifications as read for a user.
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    /**
     * Broadcast a shipment status update via WebSocket.
     */
    public void broadcastShipmentUpdate(Long shipmentId, String oldStatus, String newStatus,
                                         Long merchantId, Long courierId) {
        Map<String, Object> payload = Map.of(
            "shipmentId", shipmentId,
            "oldStatus", oldStatus,
            "newStatus", newStatus,
            "timestamp", Instant.now().toString()
        );

        // Notify shipment subscribers
        messagingTemplate.convertAndSend("/topic/shipment/" + shipmentId, payload);

        // Notify merchant dashboard
        if (merchantId != null) {
            messagingTemplate.convertAndSend("/topic/dashboard/stats/" + merchantId, payload);
        }

        // Notify courier
        if (courierId != null) {
            messagingTemplate.convertAndSend("/topic/courier/" + courierId, payload);
        }

        // Notify admin dashboard
        messagingTemplate.convertAndSend("/topic/dashboard/stats/admin", payload);
    }

    /**
     * Broadcast dashboard stats update.
     */
    public void broadcastDashboardUpdate(String metric, Object oldValue, Object newValue) {
        Map<String, Object> payload = Map.of(
            "type", "STATS_UPDATE",
            "metric", metric,
            "oldValue", oldValue,
            "newValue", newValue,
            "timestamp", Instant.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/dashboard/stats/admin", payload);
    }
}
