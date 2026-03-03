package com.twsela.service;

import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("اختبارات تكامل Shopify")
class ShopifyIntegrationTest {

    private ShopifyIntegration shopifyIntegration;

    @BeforeEach
    void setUp() {
        shopifyIntegration = new ShopifyIntegration();
    }

    @Test
    @DisplayName("يجب أن تكون المنصة Shopify")
    void getPlatform() {
        assertThat(shopifyIntegration.getPlatform()).isEqualTo(ECommercePlatform.SHOPIFY);
    }

    @Nested
    @DisplayName("تحليل الطلبات")
    class ParseOrder {

        @Test
        @DisplayName("يجب تحليل طلب Shopify بنجاح")
        void parseOrder_success() {
            String payload = """
                {
                    "id": 12345,
                    "order_number": "1001",
                    "shipping_address": {
                        "name": "أحمد محمد",
                        "phone": "01098765432",
                        "address1": "شارع النيل",
                        "city": "القاهرة",
                        "province": "القاهرة"
                    },
                    "line_items": [
                        {"grams": 1500, "title": "منتج تجريبي"}
                    ],
                    "total_price": "250.00",
                    "gateway": "cash_on_delivery"
                }
                """;

            Map<String, Object> result = shopifyIntegration.parseOrder(payload);

            assertThat(result).isNotNull();
            assertThat(result.get("externalOrderId")).isEqualTo("12345");
            assertThat(result.get("recipientName")).isEqualTo("أحمد محمد");
            assertThat(result.get("recipientPhone")).isEqualTo("01098765432");
            assertThat((Boolean) result.get("isCOD")).isTrue();
        }

        @Test
        @DisplayName("يجب التعامل مع طلب بدون بيانات شحن")
        void parseOrder_noShippingAddress() {
            String payload = """
                {
                    "id": 12346,
                    "order_number": "1002",
                    "line_items": [],
                    "total_price": "100.00",
                    "gateway": "credit_card"
                }
                """;

            Map<String, Object> result = shopifyIntegration.parseOrder(payload);

            assertThat(result).isNotNull();
            assertThat(result.get("externalOrderId")).isEqualTo("12346");
        }

        @Test
        @DisplayName("يجب رفض JSON غير صحيح")
        void parseOrder_invalidJson() {
            assertThatThrownBy(() -> shopifyIntegration.parseOrder("invalid json"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("التحقق من Webhook")
    class WebhookValidation {

        @Test
        @DisplayName("يجب رفض توقيع فارغ")
        void validateWebhook_nullSignature() {
            boolean valid = shopifyIntegration.validateWebhook("payload", null, "secret");
            assertThat(valid).isFalse();
        }
    }
}
