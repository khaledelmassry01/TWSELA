package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import com.twsela.domain.ECommerceOrder.OrderStatus;
import com.twsela.repository.ECommerceConnectionRepository;
import com.twsela.repository.ECommerceOrderRepository;
import com.twsela.repository.UserRepository;
import com.twsela.repository.ZoneRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("اختبارات خدمة التجارة الإلكترونية")
class ECommerceServiceTest {

    @Mock private ECommerceConnectionRepository connectionRepository;
    @Mock private ECommerceOrderRepository orderRepository;
    @Mock private ECommerceIntegrationFactory integrationFactory;
    @Mock private UserRepository userRepository;
    @Mock private ZoneRepository zoneRepository;
    @Mock private ECommerceIntegration mockIntegration;

    @InjectMocks private ECommerceService eCommerceService;

    private User merchant;
    private ECommerceConnection connection;

    @BeforeEach
    void setUp() {
        merchant = new User();
        merchant.setId(1L);
        merchant.setName("تاجر تجريبي");
        merchant.setPhone("01012345678");

        connection = new ECommerceConnection();
        connection.setId(1L);
        connection.setMerchant(merchant);
        connection.setPlatform(ECommercePlatform.SHOPIFY);
        connection.setStoreName("متجري");
        connection.setStoreUrl("https://mystore.shopify.com");
        connection.setAccessToken("sk-test");
        connection.setWebhookSecret("secret123");
        connection.setActive(true);
        connection.setAutoCreateShipments(true);
        connection.setSyncErrors(0);
        connection.setCreatedAt(Instant.now());
    }

    @Nested
    @DisplayName("ربط المتجر")
    class ConnectStore {

        @Test
        @DisplayName("يجب ربط المتجر بنجاح")
        void connectStore_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
            when(connectionRepository.findByMerchantIdAndPlatform(1L, ECommercePlatform.SHOPIFY))
                    .thenReturn(Optional.empty());
            when(connectionRepository.save(any(ECommerceConnection.class))).thenAnswer(inv -> {
                ECommerceConnection c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            ECommerceConnection result = eCommerceService.connectStore(
                    1L, ECommercePlatform.SHOPIFY, "https://store.shopify.com",
                    "متجري", "sk-test", "secret123", null);

            assertThat(result).isNotNull();
            assertThat(result.getPlatform()).isEqualTo(ECommercePlatform.SHOPIFY);
            assertThat(result.getStoreName()).isEqualTo("متجري");
            verify(connectionRepository).save(any(ECommerceConnection.class));
        }

        @Test
        @DisplayName("يجب رفض الربط عند وجود اتصال نشط")
        void connectStore_duplicateActive() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
            when(connectionRepository.findByMerchantIdAndPlatform(1L, ECommercePlatform.SHOPIFY))
                    .thenReturn(Optional.of(connection));

            assertThatThrownBy(() -> eCommerceService.connectStore(
                    1L, ECommercePlatform.SHOPIFY, "https://store.shopify.com",
                    "متجري", "sk-test", "secret123", null))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("اتصال نشط");
        }

        @Test
        @DisplayName("يجب رفض الربط لتاجر غير موجود")
        void connectStore_merchantNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eCommerceService.connectStore(
                    99L, ECommercePlatform.SHOPIFY, "https://store.shopify.com",
                    "متجري", "sk-test", "secret123", null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("فصل المتجر")
    class DisconnectStore {

        @Test
        @DisplayName("يجب فصل المتجر بنجاح")
        void disconnectStore_success() {
            when(connectionRepository.findById(1L)).thenReturn(Optional.of(connection));
            when(connectionRepository.save(any(ECommerceConnection.class))).thenReturn(connection);

            eCommerceService.disconnectStore(1L);

            assertThat(connection.isActive()).isFalse();
            verify(connectionRepository).save(connection);
        }
    }

    @Nested
    @DisplayName("معالجة الطلبات الواردة")
    class ProcessIncomingOrder {

        @Test
        @DisplayName("يجب رفض Webhook من اتصال غير نشط")
        void processIncomingOrder_inactiveConnection() {
            connection.setActive(false);
            when(connectionRepository.findById(1L)).thenReturn(Optional.of(connection));

            assertThatThrownBy(() -> eCommerceService.processIncomingOrder(1L, "{}", "sig"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("غير نشط");
        }

        @Test
        @DisplayName("يجب رفض توقيع Webhook غير صالح")
        void processIncomingOrder_invalidSignature() {
            when(connectionRepository.findById(1L)).thenReturn(Optional.of(connection));
            when(integrationFactory.getIntegration(ECommercePlatform.SHOPIFY)).thenReturn(mockIntegration);
            when(mockIntegration.validateWebhook("{}", "bad-sig", "secret123")).thenReturn(false);

            assertThatThrownBy(() -> eCommerceService.processIncomingOrder(1L, "{}", "bad-sig"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("توقيع");
        }
    }

    @Nested
    @DisplayName("الاتصالات والإحصائيات")
    class ConnectionsAndStats {

        @Test
        @DisplayName("يجب جلب اتصالات التاجر")
        void getConnectionsByMerchant() {
            when(connectionRepository.findByMerchantId(1L)).thenReturn(List.of(connection));

            List<ECommerceConnection> result = eCommerceService.getConnectionsByMerchant(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPlatform()).isEqualTo(ECommercePlatform.SHOPIFY);
        }

        @Test
        @DisplayName("يجب جلب إحصائيات الاتصال")
        void getConnectionStats() {
            when(connectionRepository.findById(1L)).thenReturn(Optional.of(connection));
            when(orderRepository.countByConnectionIdAndStatus(1L, OrderStatus.RECEIVED)).thenReturn(5L);
            when(orderRepository.countByConnectionIdAndStatus(1L, OrderStatus.SHIPMENT_CREATED)).thenReturn(10L);
            when(orderRepository.countByConnectionIdAndStatus(1L, OrderStatus.FULFILLED)).thenReturn(20L);
            when(orderRepository.countByConnectionIdAndStatus(1L, OrderStatus.FAILED)).thenReturn(2L);

            Map<String, Object> stats = eCommerceService.getConnectionStats(1L);

            assertThat(stats).isNotNull();
            assertThat(stats.get("platform")).isEqualTo(ECommercePlatform.SHOPIFY);
        }
    }
}
