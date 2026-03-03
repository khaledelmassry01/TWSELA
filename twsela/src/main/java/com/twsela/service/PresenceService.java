package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * خدمة حالة الاتصال — تتبع المستخدمين المتصلين عبر WebSocket.
 */
@Service
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);

    private final SimpMessagingTemplate messagingTemplate;

    // In-memory presence map: userId → last seen timestamp
    private final Map<Long, Instant> onlineUsers = new ConcurrentHashMap<>();

    public PresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * تسجيل اتصال مستخدم.
     */
    public void userConnected(Long userId) {
        onlineUsers.put(userId, Instant.now());
        broadcastPresence(userId, true);
        log.debug("User {} connected", userId);
    }

    /**
     * تسجيل انقطاع اتصال مستخدم.
     */
    public void userDisconnected(Long userId) {
        onlineUsers.remove(userId);
        broadcastPresence(userId, false);
        log.debug("User {} disconnected", userId);
    }

    /**
     * هل المستخدم متصل حالياً؟
     */
    public boolean isOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    /**
     * الحصول على كل المستخدمين المتصلين.
     */
    public Set<Long> getOnlineUsers() {
        return onlineUsers.keySet();
    }

    /**
     * آخر وقت اتصال للمستخدم.
     */
    public Instant getLastSeen(Long userId) {
        return onlineUsers.get(userId);
    }

    /**
     * عدد المستخدمين المتصلين.
     */
    public int getOnlineCount() {
        return onlineUsers.size();
    }

    private void broadcastPresence(Long userId, boolean online) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/presence",
                Map.of(
                    "userId", userId,
                    "online", online,
                    "timestamp", Instant.now().toString()
                )
            );
        } catch (Exception e) {
            log.warn("Failed to broadcast presence for user {}: {}", userId, e.getMessage());
        }
    }
}
