package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.DeliveryProofRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryProofServiceTest {

    @Mock private DeliveryProofRepository proofRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private DeliveryProofService deliveryProofService;

    private Shipment shipment;
    private User courier;
    private DeliveryProof proof;

    @BeforeEach
    void setUp() {
        shipment = new Shipment();
        shipment.setId(100L);
        shipment.setTrackingNumber("TS100001");

        Role courierRole = new Role("COURIER");
        courierRole.setId(2L);
        courier = new User();
        courier.setId(20L);
        courier.setName("مندوب اختبار");
        courier.setRole(courierRole);

        proof = new DeliveryProof();
        proof.setId(1L);
        proof.setShipment(shipment);
        proof.setLatitude(30.0444);
        proof.setLongitude(31.2357);
        proof.setRecipientName("أحمد محمد");
        proof.setDeliveredAt(Instant.now());
        proof.setCapturedBy(courier);
    }

    @Nested
    @DisplayName("submitProof — تقديم إثبات التسليم")
    class SubmitProofTests {

        @Test
        @DisplayName("يجب تسجيل إثبات التسليم بنجاح")
        void submitProof_success() throws IOException {
            when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
            when(userRepository.findById(20L)).thenReturn(Optional.of(courier));
            when(proofRepository.existsByShipmentId(100L)).thenReturn(false);
            when(proofRepository.save(any(DeliveryProof.class))).thenAnswer(inv -> {
                DeliveryProof p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            MultipartFile photo = mock(MultipartFile.class);
            when(photo.isEmpty()).thenReturn(false);
            when(fileStorageService.storeFile(eq(photo), eq("delivery-photos")))
                    .thenReturn("/uploads/delivery-photos/uuid.jpg");

            DeliveryProof result = deliveryProofService.submitProof(
                    100L, photo, null, 30.0444, 31.2357, "أحمد محمد", "ملاحظات", 20L);

            assertThat(result).isNotNull();
            assertThat(result.getShipment().getId()).isEqualTo(100L);
            assertThat(result.getRecipientName()).isEqualTo("أحمد محمد");
            assertThat(result.getPhotoUrl()).isEqualTo("/uploads/delivery-photos/uuid.jpg");
            verify(proofRepository).save(any(DeliveryProof.class));
        }

        @Test
        @DisplayName("يجب رفض إثبات مكرر لنفس الشحنة")
        void submitProof_duplicateThrows() {
            when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
            when(userRepository.findById(20L)).thenReturn(Optional.of(courier));
            when(proofRepository.existsByShipmentId(100L)).thenReturn(true);

            assertThatThrownBy(() -> deliveryProofService.submitProof(
                    100L, null, null, 30.0, 31.0, "test", null, 20L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("إثبات التسليم موجود مسبقاً");
        }

        @Test
        @DisplayName("يجب رفض شحنة غير موجودة")
        void submitProof_shipmentNotFound() {
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deliveryProofService.submitProof(
                    999L, null, null, 30.0, 31.0, "test", null, 20L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getProof — استرجاع إثبات التسليم")
    class GetProofTests {

        @Test
        @DisplayName("يجب استرجاع إثبات التسليم بنجاح")
        void getProof_success() {
            when(proofRepository.findByShipmentId(100L)).thenReturn(Optional.of(proof));

            DeliveryProof result = deliveryProofService.getProof(100L);

            assertThat(result).isNotNull();
            assertThat(result.getShipment().getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("يجب طرح استثناء عند عدم وجود إثبات")
        void getProof_notFound() {
            when(proofRepository.findByShipmentId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deliveryProofService.getProof(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("hasProof — التحقق من وجود إثبات")
    class HasProofTests {

        @Test
        @DisplayName("يجب إرجاع true عند وجود إثبات")
        void hasProof_true() {
            when(proofRepository.existsByShipmentId(100L)).thenReturn(true);
            assertThat(deliveryProofService.hasProof(100L)).isTrue();
        }

        @Test
        @DisplayName("يجب إرجاع false عند عدم وجود إثبات")
        void hasProof_false() {
            when(proofRepository.existsByShipmentId(100L)).thenReturn(false);
            assertThat(deliveryProofService.hasProof(100L)).isFalse();
        }
    }

    @Nested
    @DisplayName("validateProofLocation — التحقق من موقع التسليم")
    class ValidateLocationTests {

        @Test
        @DisplayName("يجب قبول موقع قريب (أقل من 2 كم)")
        void validateLocation_withinRange() {
            // Same location
            boolean valid = deliveryProofService.validateProofLocation(proof, 30.0444, 31.2357);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("يجب رفض موقع بعيد (أكثر من 2 كم)")
        void validateLocation_outOfRange() {
            // ~50 km away
            boolean valid = deliveryProofService.validateProofLocation(proof, 30.5, 31.7);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("يجب قبول الإثبات بدون إحداثيات GPS")
        void validateLocation_noGps() {
            proof.setLatitude(null);
            proof.setLongitude(null);
            boolean valid = deliveryProofService.validateProofLocation(proof, 30.0, 31.0);
            assertThat(valid).isTrue();
        }
    }
}
