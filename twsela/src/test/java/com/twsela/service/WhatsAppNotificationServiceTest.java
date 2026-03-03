package com.twsela.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppNotificationServiceTest {

    @InjectMocks
    private WhatsAppNotificationService whatsAppNotificationService;

    @Nested
    @DisplayName("sendWhatsApp — إرسال رسالة واتساب")
    class SendWhatsAppTests {

        @Test
        @DisplayName("يجب إرسال رسالة واتساب بنجاح")
        void sendWhatsApp_success() {
            String result = whatsAppNotificationService.sendWhatsApp(
                    "+201234567890", "shipment_update",
                    java.util.List.of("TS100001", "تم التسليم"));

            assertThat(result).isNotNull();
            assertThat(result).startsWith("wa-");
        }

        @Test
        @DisplayName("يجب إرجاع null مع رقم فارغ")
        void sendWhatsApp_emptyPhone() {
            String result = whatsAppNotificationService.sendWhatsApp(
                    "", "template", java.util.List.of());

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("يجب إرجاع null مع رقم null")
        void sendWhatsApp_nullPhone() {
            String result = whatsAppNotificationService.sendWhatsApp(
                    null, "template", java.util.List.of());

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("sendFreeFormMessage — إرسال رسالة حرة")
    class FreeFormTests {

        @Test
        @DisplayName("يجب إرسال رسالة حرة بنجاح")
        void sendFreeForm_success() {
            String result = whatsAppNotificationService.sendFreeFormMessage(
                    "+201234567890", "شحنتك في الطريق");

            assertThat(result).isNotNull();
            assertThat(result).startsWith("wa-ff-");
        }
    }
}
