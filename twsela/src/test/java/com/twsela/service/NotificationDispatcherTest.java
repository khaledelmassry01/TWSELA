package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.NotificationDeliveryLog.DeliveryStatus;
import com.twsela.repository.NotificationDeliveryLogRepository;
import com.twsela.repository.NotificationPreferenceRepository;
import com.twsela.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Mock private NotificationPreferenceRepository preferenceRepository;
    @Mock private NotificationDeliveryLogRepository deliveryLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private TemplateEngine templateEngine;
    @Mock private NotificationService notificationService;
    @Mock private EmailNotificationService emailService;
    @Mock private PushNotificationService pushService;
    @Mock private WhatsAppNotificationService whatsAppService;

    @InjectMocks
    private NotificationDispatcher dispatcher;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role role = new Role("MERCHANT");
        role.setId(1L);
        testUser = new User();
        testUser.setId(10L);
        testUser.setName("تاجر");
        testUser.setPhone("01234567890");
        testUser.setRole(role);
    }

    @Nested
    @DisplayName("dispatch — إرسال الإشعارات المتعددة القنوات")
    class DispatchTests {

        @Test
        @DisplayName("يجب إرسال إشعار IN_APP عند عدم وجود تفضيلات")
        void dispatch_defaultInApp() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
            when(preferenceRepository.findByUserId(10L)).thenReturn(Optional.empty());
            when(templateEngine.renderForChannel(any(), eq(NotificationChannel.IN_APP), anyString(), any()))
                    .thenReturn(new String[]{"عنوان", "محتوى"});
            when(notificationService.send(anyLong(), any(), anyString(), anyString()))
                    .thenReturn(new Notification(10L, NotificationType.SHIPMENT_CREATED, "عنوان", "محتوى"));

            dispatcher.dispatch(10L, NotificationType.SHIPMENT_CREATED, Map.of("trackingNumber", "TS001"));

            verify(notificationService).send(eq(10L), eq(NotificationType.SHIPMENT_CREATED), anyString(), anyString());
            verify(deliveryLogRepository).save(any(NotificationDeliveryLog.class));
        }

        @Test
        @DisplayName("يجب تخطي الإرسال عند إيقاف الإشعارات مؤقتاً")
        void dispatch_pausedUser() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
            NotificationPreference pref = new NotificationPreference();
            pref.setUser(testUser);
            pref.setPausedUntil(Instant.now().plusSeconds(3600)); // paused for 1 hour
            when(preferenceRepository.findByUserId(10L)).thenReturn(Optional.of(pref));

            dispatcher.dispatch(10L, NotificationType.SHIPMENT_CREATED, Map.of());

            verifyNoInteractions(templateEngine);
            verifyNoInteractions(deliveryLogRepository);
        }

        @Test
        @DisplayName("يجب تخطي الإرسال عند عدم وجود المستخدم")
        void dispatch_userNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            dispatcher.dispatch(999L, NotificationType.SHIPMENT_CREATED, Map.of());

            verifyNoInteractions(preferenceRepository);
        }

        @Test
        @DisplayName("يجب إرسال عبر قنوات متعددة حسب التفضيلات")
        void dispatch_multipleChannels() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));

            NotificationPreference pref = new NotificationPreference();
            pref.setUser(testUser);
            pref.setEnabledChannelsJson("{\"SHIPMENT_CREATED\":[\"IN_APP\",\"EMAIL\"]}");
            when(preferenceRepository.findByUserId(10L)).thenReturn(Optional.of(pref));

            when(templateEngine.renderForChannel(any(), any(), anyString(), any()))
                    .thenReturn(new String[]{"Subject", "Body"});
            when(notificationService.send(anyLong(), any(), anyString(), anyString()))
                    .thenReturn(new Notification(10L, NotificationType.SHIPMENT_CREATED, "S", "B"));
            when(emailService.sendEmail(anyString(), anyString(), anyString()))
                    .thenReturn("email-ext-123");

            dispatcher.dispatch(10L, NotificationType.SHIPMENT_CREATED, Map.of());

            verify(notificationService).send(eq(10L), any(), anyString(), anyString());
            verify(emailService).sendEmail(eq("01234567890@twsela.app"), anyString(), anyString());
            verify(deliveryLogRepository, times(2)).save(any(NotificationDeliveryLog.class));
        }

        @Test
        @DisplayName("يجب إرسال PUSH عند وجود تفضيل PUSH")
        void dispatch_pushChannel() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));

            NotificationPreference pref = new NotificationPreference();
            pref.setUser(testUser);
            pref.setEnabledChannelsJson("{\"STATUS_CHANGED\":[\"PUSH\"]}");
            when(preferenceRepository.findByUserId(10L)).thenReturn(Optional.of(pref));

            when(templateEngine.renderForChannel(any(), eq(NotificationChannel.PUSH), anyString(), any()))
                    .thenReturn(new String[]{"Title", "Body"});
            when(pushService.sendPush(eq(10L), anyString(), anyString(), any())).thenReturn(1);

            dispatcher.dispatch(10L, NotificationType.STATUS_CHANGED, Map.of());

            verify(pushService).sendPush(eq(10L), anyString(), anyString(), any());
        }
    }
}
