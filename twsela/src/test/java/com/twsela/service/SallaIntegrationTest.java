package com.twsela.service;

import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("اختبارات تكامل سلة")
class SallaIntegrationTest {

    private SallaIntegration sallaIntegration;

    @BeforeEach
    void setUp() {
        sallaIntegration = new SallaIntegration();
    }

    @Test
    @DisplayName("يجب أن تكون المنصة سلة")
    void getPlatform() {
        assertThat(sallaIntegration.getPlatform()).isEqualTo(ECommercePlatform.SALLA);
    }

    @Test
    @DisplayName("يجب تحليل طلب سلة بنجاح")
    void parseOrder_success() {
        String payload = """
            {
                "event": "order.created",
                "data": {
                    "id": 98765,
                    "reference_id": "SL-2025-001",
                    "customer": {
                        "first_name": "محمد",
                        "last_name": "علي",
                        "phone": "0501234567"
                    },
                    "shipping": {
                        "address": {
                            "street": "شارع الملك فهد",
                            "city": "الرياض"
                        }
                    },
                    "amounts": {
                        "total": {"amount": 350.0}
                    },
                    "payment_method": "cod"
                }
            }
            """;

        Map<String, Object> result = sallaIntegration.parseOrder(payload);

        assertThat(result).isNotNull();
        assertThat(result.get("externalOrderId")).isEqualTo("98765");
        assertThat(result.get("recipientName")).isEqualTo("محمد علي");
    }

    @Test
    @DisplayName("يجب رفض توقيع فارغ")
    void validateWebhook_nullSignature() {
        boolean valid = sallaIntegration.validateWebhook("payload", null, "secret");
        assertThat(valid).isFalse();
    }
}
