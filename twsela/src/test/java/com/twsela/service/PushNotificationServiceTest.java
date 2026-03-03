package com.twsela.service;

import com.twsela.domain.DeviceToken;
import com.twsela.domain.User;
import com.twsela.repository.DeviceTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock private DeviceTokenRepository deviceTokenRepository;

    @InjectMocks
    private PushNotificationService pushNotificationService;

    @Nested
    @DisplayName("sendPush — إرسال إشعار فوري")
    class SendPushTests {

        @Test
        @DisplayName("يجب إرسال إشعار لجميع أجهزة المستخدم")
        void sendPush_multipleDevices() {
            DeviceToken token1 = new DeviceToken();
            token1.setToken("token-11111");
            token1.setActive(true);
            token1.setPlatform(DeviceToken.Platform.ANDROID);

            DeviceToken token2 = new DeviceToken();
            token2.setToken("token-22222");
            token2.setActive(true);
            token2.setPlatform(DeviceToken.Platform.IOS);

            when(deviceTokenRepository.findByUserIdAndActiveTrue(1L))
                    .thenReturn(List.of(token1, token2));
            when(deviceTokenRepository.save(any(DeviceToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            int result = pushNotificationService.sendPush(1L, "عنوان", "محتوى", java.util.Map.of());

            assertThat(result).isEqualTo(2);
        }

        @Test
        @DisplayName("يجب إرجاع صفر عند عدم وجود أجهزة")
        void sendPush_noDevices() {
            when(deviceTokenRepository.findByUserIdAndActiveTrue(1L))
                    .thenReturn(List.of());

            int result = pushNotificationService.sendPush(1L, "عنوان", "محتوى", java.util.Map.of());

            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("registerToken — تسجيل جهاز جديد")
    class RegisterTests {

        @Test
        @DisplayName("يجب تسجيل جهاز جديد بنجاح")
        void registerToken_new() {
            User user = new User();
            user.setId(1L);

            when(deviceTokenRepository.findByToken("new-token"))
                    .thenReturn(Optional.empty());
            when(deviceTokenRepository.save(any(DeviceToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            DeviceToken result = pushNotificationService.registerToken(
                    1L, "new-token", DeviceToken.Platform.ANDROID, user);

            assertThat(result).isNotNull();
            verify(deviceTokenRepository).save(any(DeviceToken.class));
        }

        @Test
        @DisplayName("يجب تحديث جهاز موجود")
        void registerToken_existing() {
            User user = new User();
            user.setId(1L);

            DeviceToken existing = new DeviceToken();
            existing.setToken("existing-token");
            existing.setActive(false);

            when(deviceTokenRepository.findByToken("existing-token"))
                    .thenReturn(Optional.of(existing));
            when(deviceTokenRepository.save(any(DeviceToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            DeviceToken result = pushNotificationService.registerToken(
                    1L, "existing-token", DeviceToken.Platform.IOS, user);

            assertThat(result.isActive()).isTrue();
            verify(deviceTokenRepository).save(existing);
        }
    }

    @Nested
    @DisplayName("unregisterToken — إلغاء تسجيل جهاز")
    class UnregisterTests {

        @Test
        @DisplayName("يجب إلغاء تسجيل الجهاز بنجاح")
        void unregisterToken_success() {
            pushNotificationService.unregisterToken(1L, "token-to-remove");
            verify(deviceTokenRepository).deleteByUserIdAndToken(1L, "token-to-remove");
        }
    }
}
