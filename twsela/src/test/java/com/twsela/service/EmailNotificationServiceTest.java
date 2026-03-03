package com.twsela.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @Nested
    @DisplayName("sendEmail — إرسال بريد إلكتروني")
    class SendEmailTests {

        @Test
        @DisplayName("يجب إرسال بريد إلكتروني بنجاح")
        void sendEmail_success() {
            String result = emailNotificationService.sendEmail("test@example.com", "موضوع", "<p>محتوى</p>");
            assertThat(result).isNotNull();
            assertThat(result).startsWith("email-");
        }

        @Test
        @DisplayName("يجب إرجاع معرف خارجي عند النجاح")
        void sendEmail_returnsExternalId() {
            String result = emailNotificationService.sendEmail("user@test.com", "عنوان", "<p>نص</p>");
            assertThat(result).isNotBlank();
        }
    }

    @Nested
    @DisplayName("sendBulkEmail — إرسال بريد جماعي")
    class SendBulkTests {

        @Test
        @DisplayName("يجب إرسال بريد جماعي لعدة مستلمين")
        void sendBulkEmail_success() {
            var recipients = java.util.List.of("a@test.com", "b@test.com", "c@test.com");
            int sent = emailNotificationService.sendBulkEmail(recipients, "تنبيه", "<p>رسالة</p>");
            assertThat(sent).isEqualTo(3);
        }

        @Test
        @DisplayName("يجب إرجاع صفر عند قائمة فارغة")
        void sendBulkEmail_emptyList() {
            int sent = emailNotificationService.sendBulkEmail(java.util.List.of(), "موضوع", "<p>محتوى</p>");
            assertThat(sent).isZero();
        }
    }
}
