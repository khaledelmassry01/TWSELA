package com.twsela.service;

import com.twsela.domain.DeviceToken;
import com.twsela.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Push notification service using Firebase Cloud Messaging (FCM).
 * Manages device tokens and sends push notifications.
 */
@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final DeviceTokenRepository deviceTokenRepository;

    public PushNotificationService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    /**
     * Send push notification to all active devices of a user.
     *
     * @return count of successfully sent notifications
     */
    public int sendPush(Long userId, String title, String body, Map<String, String> data) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.debug("No active device tokens for user {}", userId);
            return 0;
        }

        int sent = 0;
        for (DeviceToken dt : tokens) {
            try {
                // In production, use Firebase Admin SDK to send
                log.info("Sending push to device {} (platform={}) for user {}: {}",
                        dt.getToken().substring(0, Math.min(10, dt.getToken().length())) + "...",
                        dt.getPlatform(), userId, title);
                dt.setLastUsedAt(Instant.now());
                deviceTokenRepository.save(dt);
                sent++;
            } catch (Exception e) {
                log.warn("Failed to send push to device {}: {}", dt.getId(), e.getMessage());
                // If token is invalid, deactivate it
                if (isInvalidTokenError(e)) {
                    dt.setActive(false);
                    deviceTokenRepository.save(dt);
                    log.info("Deactivated invalid device token {}", dt.getId());
                }
            }
        }

        log.debug("Push sent to {}/{} devices for user {}", sent, tokens.size(), userId);
        return sent;
    }

    /**
     * Send a push notification to a topic (e.g., all couriers).
     */
    public void sendToTopic(String topic, String title, String body) {
        log.info("Sending push to topic '{}': {}", topic, title);
        // In production, use Firebase Admin SDK topic messaging
    }

    /**
     * Register a device token for a user.
     */
    public DeviceToken registerToken(Long userId, String token, DeviceToken.Platform platform,
                                      com.twsela.domain.User user) {
        return deviceTokenRepository.findByToken(token)
                .map(existing -> {
                    existing.setUser(user);
                    existing.setPlatform(platform);
                    existing.setActive(true);
                    existing.setLastUsedAt(Instant.now());
                    return deviceTokenRepository.save(existing);
                })
                .orElseGet(() -> {
                    DeviceToken dt = new DeviceToken(user, token, platform);
                    return deviceTokenRepository.save(dt);
                });
    }

    /**
     * Unregister a device token.
     */
    public void unregisterToken(Long userId, String token) {
        deviceTokenRepository.deleteByUserIdAndToken(userId, token);
        log.info("Device token unregistered for user {}", userId);
    }

    private boolean isInvalidTokenError(Exception e) {
        String msg = e.getMessage();
        return msg != null && (msg.contains("InvalidRegistration") || msg.contains("NotRegistered"));
    }
}
