package com.twsela.service;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationDeliveryLog;
import com.twsela.domain.NotificationDeliveryLog.DeliveryStatus;
import com.twsela.repository.NotificationDeliveryLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRetryServiceTest {

    @Mock private NotificationDeliveryLogRepository deliveryLogRepository;
    @Mock private EmailNotificationService emailService;
    @Mock private PushNotificationService pushService;
    @Mock private WhatsAppNotificationService whatsAppService;

    @InjectMocks
    private NotificationRetryService notificationRetryService;

    @Nested
    @DisplayName("retryFailedNotifications — إعادة محاولة الإشعارات الفاشلة")
    class RetryTests {

        @Test
        @DisplayName("يجب إعادة إرسال بريد إلكتروني فاشل بنجاح")
        void retry_emailSuccess() {
            NotificationDeliveryLog logEntry = new NotificationDeliveryLog();
            logEntry.setId(1L);
            logEntry.setChannel(NotificationChannel.EMAIL);
            logEntry.setRecipient("test@example.com");
            logEntry.setStatus(DeliveryStatus.FAILED);
            logEntry.setRetryCount(1);
            logEntry.setNextRetryAt(Instant.now().minusSeconds(60));

            when(deliveryLogRepository.findByStatusAndNextRetryAtBefore(eq(DeliveryStatus.FAILED), any(Instant.class)))
                    .thenReturn(List.of(logEntry));
            when(emailService.sendEmail(anyString(), anyString(), anyString()))
                    .thenReturn("email-123");
            when(deliveryLogRepository.save(any(NotificationDeliveryLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            notificationRetryService.retryFailedNotifications();

            verify(deliveryLogRepository).save(argThat(saved ->
                    saved.getStatus() == DeliveryStatus.SENT));
        }

        @Test
        @DisplayName("يجب تمييز الإشعار كمرتد عند تجاوز الحد الأقصى")
        void retry_maxRetriesExceeded() {
            NotificationDeliveryLog logEntry = new NotificationDeliveryLog();
            logEntry.setId(2L);
            logEntry.setChannel(NotificationChannel.EMAIL);
            logEntry.setRecipient("fail@example.com");
            logEntry.setStatus(DeliveryStatus.FAILED);
            logEntry.setRetryCount(3);
            logEntry.setNextRetryAt(Instant.now().minusSeconds(60));

            when(deliveryLogRepository.findByStatusAndNextRetryAtBefore(eq(DeliveryStatus.FAILED), any(Instant.class)))
                    .thenReturn(List.of(logEntry));
            when(deliveryLogRepository.save(any(NotificationDeliveryLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            notificationRetryService.retryFailedNotifications();

            verify(deliveryLogRepository).save(argThat(saved ->
                    saved.getStatus() == DeliveryStatus.BOUNCED));
        }

        @Test
        @DisplayName("يجب عدم فعل شيء عند عدم وجود إشعارات فاشلة")
        void retry_noFailed() {
            when(deliveryLogRepository.findByStatusAndNextRetryAtBefore(eq(DeliveryStatus.FAILED), any(Instant.class)))
                    .thenReturn(List.of());

            notificationRetryService.retryFailedNotifications();

            verify(deliveryLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("يجب زيادة عدد المحاولات عند فشل إعادة المحاولة")
        void retry_incrementsCount() {
            NotificationDeliveryLog logEntry = new NotificationDeliveryLog();
            logEntry.setId(3L);
            logEntry.setChannel(NotificationChannel.WHATSAPP);
            logEntry.setRecipient("+201234567890");
            logEntry.setStatus(DeliveryStatus.FAILED);
            logEntry.setRetryCount(1);
            logEntry.setNextRetryAt(Instant.now().minusSeconds(60));

            when(deliveryLogRepository.findByStatusAndNextRetryAtBefore(eq(DeliveryStatus.FAILED), any(Instant.class)))
                    .thenReturn(List.of(logEntry));
            when(whatsAppService.sendWhatsApp(anyString(), anyString(), anyList()))
                    .thenReturn(null);
            when(deliveryLogRepository.save(any(NotificationDeliveryLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            notificationRetryService.retryFailedNotifications();

            verify(deliveryLogRepository).save(argThat(saved ->
                    saved.getRetryCount() == 2 && saved.getNextRetryAt() != null));
        }
    }
}
