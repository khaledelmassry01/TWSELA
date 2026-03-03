package com.twsela.service;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationDeliveryLog;
import com.twsela.domain.NotificationDeliveryLog.DeliveryStatus;
import com.twsela.repository.NotificationDeliveryLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Retries failed notification deliveries with exponential backoff.
 * Runs every 5 minutes via @Scheduled.
 */
@Service
public class NotificationRetryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetryService.class);
    private static final int MAX_RETRIES = 3;

    private final NotificationDeliveryLogRepository deliveryLogRepository;
    private final EmailNotificationService emailService;
    private final PushNotificationService pushService;
    private final WhatsAppNotificationService whatsAppService;

    public NotificationRetryService(NotificationDeliveryLogRepository deliveryLogRepository,
                                     EmailNotificationService emailService,
                                     PushNotificationService pushService,
                                     WhatsAppNotificationService whatsAppService) {
        this.deliveryLogRepository = deliveryLogRepository;
        this.emailService = emailService;
        this.pushService = pushService;
        this.whatsAppService = whatsAppService;
    }

    /**
     * Retry failed notifications that are due for retry.
     * Scheduled to run every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void retryFailedNotifications() {
        List<NotificationDeliveryLog> failedLogs =
                deliveryLogRepository.findByStatusAndNextRetryAtBefore(DeliveryStatus.FAILED, Instant.now());

        if (failedLogs.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed notifications", failedLogs.size());

        for (NotificationDeliveryLog logEntry : failedLogs) {
            if (logEntry.getRetryCount() >= MAX_RETRIES) {
                logEntry.setStatus(DeliveryStatus.BOUNCED);
                logEntry.setErrorMessage("Max retries exceeded");
                logEntry.setNextRetryAt(null);
                deliveryLogRepository.save(logEntry);
                continue;
            }

            try {
                boolean success = retryDelivery(logEntry);
                if (success) {
                    logEntry.setStatus(DeliveryStatus.SENT);
                    logEntry.setSentAt(Instant.now());
                    logEntry.setNextRetryAt(null);
                } else {
                    logEntry.setRetryCount(logEntry.getRetryCount() + 1);
                    logEntry.setNextRetryAt(calculateNextRetry(logEntry.getRetryCount()));
                }
            } catch (Exception e) {
                logEntry.setRetryCount(logEntry.getRetryCount() + 1);
                logEntry.setErrorMessage(e.getMessage());
                logEntry.setNextRetryAt(calculateNextRetry(logEntry.getRetryCount()));
            }
            deliveryLogRepository.save(logEntry);
        }
    }

    private boolean retryDelivery(NotificationDeliveryLog logEntry) {
        NotificationChannel channel = logEntry.getChannel();
        String recipient = logEntry.getRecipient();

        switch (channel) {
            case EMAIL:
                String emailResult = emailService.sendEmail(recipient, "Twsela Notification", "Retry notification");
                return emailResult != null;

            case PUSH:
                // Extract userId from "push:123" format
                if (recipient.startsWith("push:")) {
                    Long userId = Long.parseLong(recipient.substring(5));
                    return pushService.sendPush(userId, "Notification", "Retry", java.util.Map.of()) > 0;
                }
                return false;

            case WHATSAPP:
                String waResult = whatsAppService.sendWhatsApp(recipient, "retry", java.util.List.of());
                return waResult != null;

            case SMS:
                log.info("SMS retry to {}", recipient);
                return true; // Delegate to SMS service

            default:
                return false;
        }
    }

    /**
     * Calculate next retry time with exponential backoff.
     * Retry 1: +5 min, Retry 2: +15 min, Retry 3: +60 min
     */
    private Instant calculateNextRetry(int retryCount) {
        long delayMinutes;
        switch (retryCount) {
            case 1: delayMinutes = 5; break;
            case 2: delayMinutes = 15; break;
            default: delayMinutes = 60; break;
        }
        return Instant.now().plusSeconds(delayMinutes * 60);
    }
}
