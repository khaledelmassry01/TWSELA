package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.NotificationDeliveryLog.DeliveryStatus;
import com.twsela.domain.NotificationPreference.DigestMode;
import com.twsela.repository.NotificationDeliveryLogRepository;
import com.twsela.repository.NotificationPreferenceRepository;
import com.twsela.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Main notification dispatcher. Resolves user preferences, renders templates,
 * and routes notifications to the appropriate channel services.
 */
@Service
@Transactional
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationDeliveryLogRepository deliveryLogRepository;
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;
    private final NotificationService notificationService;
    private final EmailNotificationService emailService;
    private final PushNotificationService pushService;
    private final WhatsAppNotificationService whatsAppService;

    public NotificationDispatcher(NotificationPreferenceRepository preferenceRepository,
                                   NotificationDeliveryLogRepository deliveryLogRepository,
                                   UserRepository userRepository,
                                   TemplateEngine templateEngine,
                                   NotificationService notificationService,
                                   EmailNotificationService emailService,
                                   PushNotificationService pushService,
                                   WhatsAppNotificationService whatsAppService) {
        this.preferenceRepository = preferenceRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.userRepository = userRepository;
        this.templateEngine = templateEngine;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.pushService = pushService;
        this.whatsAppService = whatsAppService;
    }

    /**
     * Dispatch a notification to all enabled channels for a user.
     */
    public void dispatch(Long userId, NotificationType eventType, Map<String, String> templateVars) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot dispatch notification: user {} not found", userId);
            return;
        }

        // Check if user has paused notifications
        NotificationPreference pref = preferenceRepository.findByUserId(userId).orElse(null);
        if (pref != null && pref.getPausedUntil() != null && Instant.now().isBefore(pref.getPausedUntil())) {
            log.debug("Notifications paused for user {} until {}", userId, pref.getPausedUntil());
            return;
        }

        // Check quiet hours
        if (pref != null && isWithinQuietHours(pref)) {
            log.debug("Within quiet hours for user {}, skipping dispatch", userId);
            return;
        }

        // Determine channels
        List<NotificationChannel> channels = resolveChannels(pref, eventType);
        String locale = "ar"; // Default Arabic

        for (NotificationChannel channel : channels) {
            try {
                dispatchToChannel(user, eventType, channel, locale, templateVars);
            } catch (Exception e) {
                log.error("Failed to dispatch {} to {} for user {}: {}",
                        eventType, channel, userId, e.getMessage());
            }
        }
    }

    private void dispatchToChannel(User user, NotificationType eventType,
                                    NotificationChannel channel, String locale,
                                    Map<String, String> templateVars) {
        String[] rendered = templateEngine.renderForChannel(eventType, channel, locale, templateVars);
        String subject = rendered[0];
        String body = rendered[1];

        if (body == null || body.isBlank()) {
            log.warn("No template content for event={} channel={}", eventType, channel);
            return;
        }

        NotificationDeliveryLog logEntry = new NotificationDeliveryLog();
        logEntry.setChannel(channel);
        logEntry.setStatus(DeliveryStatus.PENDING);

        String externalId = null;
        switch (channel) {
            case IN_APP:
                Notification inApp = notificationService.send(user.getId(), eventType,
                        subject != null ? subject : eventType.name(), body);
                logEntry.setNotificationId(inApp.getId());
                logEntry.setRecipient("user:" + user.getId());
                logEntry.setStatus(DeliveryStatus.DELIVERED);
                break;

            case EMAIL:
                // Note: User entity uses phone-based identification.
                // Email address would come from merchant/courier details or preferences.
                String emailRecipient = user.getPhone() + "@twsela.app"; // placeholder
                externalId = emailService.sendEmail(emailRecipient, subject != null ? subject : "Twsela", body);
                logEntry.setRecipient(emailRecipient);
                logEntry.setExternalId(externalId);
                logEntry.setStatus(externalId != null ? DeliveryStatus.SENT : DeliveryStatus.FAILED);
                break;

            case SMS:
                String phone = user.getPhone();
                if (phone != null && !phone.isBlank()) {
                    // Delegate to existing TwilioSmsService or similar
                    log.info("SMS dispatch to {}: {}", phone, body);
                    logEntry.setRecipient(phone);
                    logEntry.setStatus(DeliveryStatus.SENT);
                } else {
                    logEntry.setRecipient("no-phone");
                    logEntry.setStatus(DeliveryStatus.FAILED);
                    logEntry.setErrorMessage("User has no phone number");
                }
                break;

            case PUSH:
                int sent = pushService.sendPush(user.getId(),
                        subject != null ? subject : eventType.name(), body, templateVars);
                logEntry.setRecipient("push:" + user.getId());
                logEntry.setStatus(sent > 0 ? DeliveryStatus.SENT : DeliveryStatus.FAILED);
                if (sent == 0) {
                    logEntry.setErrorMessage("No active device tokens");
                }
                break;

            case WHATSAPP:
                String waPhone = user.getPhone();
                if (waPhone != null && !waPhone.isBlank()) {
                    externalId = whatsAppService.sendWhatsApp(waPhone, eventType.name(), List.of(body));
                    logEntry.setRecipient(waPhone);
                    logEntry.setExternalId(externalId);
                    logEntry.setStatus(externalId != null ? DeliveryStatus.SENT : DeliveryStatus.FAILED);
                } else {
                    logEntry.setRecipient("no-phone");
                    logEntry.setStatus(DeliveryStatus.FAILED);
                }
                break;
        }

        logEntry.setSentAt(Instant.now());
        deliveryLogRepository.save(logEntry);
    }

    /**
     * Resolve which channels to use for a given event type.
     * Falls back to IN_APP if no preferences set.
     */
    private List<NotificationChannel> resolveChannels(NotificationPreference pref, NotificationType eventType) {
        if (pref == null || pref.getEnabledChannelsJson() == null || pref.getEnabledChannelsJson().isBlank()) {
            return List.of(NotificationChannel.IN_APP);
        }

        // Simple JSON parsing without external library
        // Format: {"EVENT_TYPE":["CHANNEL1","CHANNEL2"]}
        try {
            String json = pref.getEnabledChannelsJson();
            String eventKey = "\"" + eventType.name() + "\"";
            int idx = json.indexOf(eventKey);
            if (idx == -1) {
                return List.of(NotificationChannel.IN_APP);
            }

            int arrayStart = json.indexOf('[', idx);
            int arrayEnd = json.indexOf(']', arrayStart);
            if (arrayStart == -1 || arrayEnd == -1) {
                return List.of(NotificationChannel.IN_APP);
            }

            String arrayContent = json.substring(arrayStart + 1, arrayEnd);
            List<NotificationChannel> channels = new ArrayList<>();
            for (String part : arrayContent.split(",")) {
                String channelName = part.trim().replace("\"", "");
                if (!channelName.isBlank()) {
                    try {
                        channels.add(NotificationChannel.valueOf(channelName));
                    } catch (IllegalArgumentException ignored) {
                        log.warn("Unknown channel in preferences: {}", channelName);
                    }
                }
            }
            return channels.isEmpty() ? List.of(NotificationChannel.IN_APP) : channels;
        } catch (Exception e) {
            log.warn("Failed to parse channels JSON: {}", e.getMessage());
            return List.of(NotificationChannel.IN_APP);
        }
    }

    private boolean isWithinQuietHours(NotificationPreference pref) {
        if (pref.getQuietHoursStart() == null || pref.getQuietHoursEnd() == null) {
            return false;
        }
        LocalTime now = LocalTime.now(ZoneId.of("Africa/Cairo"));
        LocalTime start = pref.getQuietHoursStart();
        LocalTime end = pref.getQuietHoursEnd();

        if (start.isBefore(end)) {
            // e.g., 22:00 - 07:00 (crosses midnight)
            return now.isAfter(start) || now.isBefore(end);
        } else {
            // e.g., 23:00 - 06:00
            return now.isAfter(start) || now.isBefore(end);
        }
    }
}
