package com.twsela.service;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationTemplate;
import com.twsela.domain.NotificationType;
import com.twsela.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateEngineTest {

    @Mock private NotificationTemplateRepository templateRepository;

    @InjectMocks
    private TemplateEngine templateEngine;

    @Nested
    @DisplayName("render — استبدال المتغيرات في القوالب")
    class RenderTests {

        @Test
        @DisplayName("يجب استبدال المتغيرات في النص")
        void render_simpleVars() {
            String template = "شحنة {{trackingNumber}} تم تسليمها بواسطة {{courierName}}";
            Map<String, String> vars = Map.of("trackingNumber", "TS100001", "courierName", "أحمد");

            String result = templateEngine.render(template, vars);

            assertThat(result).isEqualTo("شحنة TS100001 تم تسليمها بواسطة أحمد");
        }

        @Test
        @DisplayName("يجب الإبقاء على المتغير عند عدم وجود قيمة")
        void render_missingVar() {
            String template = "المبلغ: {{amount}} {{currency}}";
            Map<String, String> vars = Map.of("amount", "100");

            String result = templateEngine.render(template, vars);

            assertThat(result).isEqualTo("المبلغ: 100 {{currency}}");
        }

        @Test
        @DisplayName("يجب إرجاع نص فارغ عند إدخال قالب فارغ")
        void render_nullTemplate() {
            String result = templateEngine.render(null, Map.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("يجب إرجاع النص كما هو بدون متغيرات")
        void render_noVars() {
            String template = "مرحباً بكم في توصيلة";
            String result = templateEngine.render(template, Map.of());
            assertThat(result).isEqualTo("مرحباً بكم في توصيلة");
        }
    }

    @Nested
    @DisplayName("renderForChannel — عرض قالب لقناة محددة")
    class RenderForChannelTests {

        @Test
        @DisplayName("يجب عرض القالب العربي عند locale=ar")
        void renderForChannel_arabic() {
            NotificationTemplate template = new NotificationTemplate();
            template.setActive(true);
            template.setSubjectTemplate("شحنة {{trackingNumber}}");
            template.setBodyTemplateAr("تم إنشاء شحنة {{trackingNumber}}");
            template.setBodyTemplateEn("Shipment {{trackingNumber}} created");

            when(templateRepository.findByEventTypeAndChannel(NotificationType.SHIPMENT_CREATED, NotificationChannel.EMAIL))
                    .thenReturn(Optional.of(template));

            String[] result = templateEngine.renderForChannel(
                    NotificationType.SHIPMENT_CREATED, NotificationChannel.EMAIL, "ar",
                    Map.of("trackingNumber", "TS001"));

            assertThat(result[0]).isEqualTo("شحنة TS001");
            assertThat(result[1]).isEqualTo("تم إنشاء شحنة TS001");
        }
    }
}
