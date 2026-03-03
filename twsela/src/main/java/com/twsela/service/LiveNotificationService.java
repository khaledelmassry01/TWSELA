package com.twsela.service;

import com.twsela.domain.LiveNotification;
import com.twsela.domain.User;
import com.twsela.repository.LiveNotificationRepository;
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
 * خدمة الإشعارات الحية — تحفظ وتبث عبر WebSocket.
 */
@Service
@Transactional
public class LiveNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LiveNotificationService.class);

    private final LiveNotificationRepository liveNotificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public LiveNotificationService(LiveNotificationRepository liveNotificationRepository,
                                   UserRepository userRepository,
                                   SimpMessagingTemplate messagingTemplate) {
        this.liveNotificationRepository = liveNotificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * إرسال إشعار حي للمستخدم.
     */
    public LiveNotification send(Long userId, String type, String title, String body, String payload) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LiveNotification notification = new LiveNotification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setPayload(payload);
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());

        LiveNotification saved = liveNotificationRepository.save(notification);

        // Push via WebSocket
        try {
            messagingTemplate.convertAndSend(
                "/topic/live-notifications/" + userId,
                Map.of(
                    "id", saved.getId(),
                    "type", type,
                    "title", title,
                    "body", body != null ? body : "",
                    "payload", payload != null ? payload : "",
                    "createdAt", saved.getCreatedAt().toString()
                )
            );
            saved.setDeliveredAt(Instant.now());
            liveNotificationRepository.save(saved);
        } catch (Exception e) {
            log.warn("Failed to push live notification to user {}: {}", userId, e.getMessage());
        }

        log.debug("Live notification sent to user {}: [{}] {}", userId, type, title);
        return saved;
    }

    /**
     * الحصول على الإشعارات غير المقروءة.
     */
    @Transactional(readOnly = true)
    public List<LiveNotification> getUnread(Long userId) {
        return liveNotificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * عدد الإشعارات غير المقروءة.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return liveNotificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * الحصول على كل الإشعارات.
     */
    @Transactional(readOnly = true)
    public List<LiveNotification> getAll(Long userId) {
        return liveNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * تعليم إشعار كمقروء.
     */
    public LiveNotification markAsRead(Long notificationId) {
        LiveNotification notification = liveNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("LiveNotification", "id", notificationId));
        notification.setRead(true);
        notification.setReadAt(Instant.now());
        return liveNotificationRepository.save(notification);
    }

    /**
     * تعليم كل إشعارات المستخدم كمقروءة.
     */
    public void markAllAsRead(Long userId) {
        List<LiveNotification> unread = liveNotificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        Instant now = Instant.now();
        for (LiveNotification n : unread) {
            n.setRead(true);
            n.setReadAt(now);
        }
        liveNotificationRepository.saveAll(unread);
        log.info("Marked {} notifications as read for user {}", unread.size(), userId);
    }
}
