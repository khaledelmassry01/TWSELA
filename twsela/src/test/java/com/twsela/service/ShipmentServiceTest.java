package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ZoneRepository zoneRepository;
    @Mock private CourierZoneRepository courierZoneRepository;
    @Mock private ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    @Mock private ShipmentStatusRepository shipmentStatusRepository;
    @Mock private DeliveryPricingRepository deliveryPricingRepository;
    @Mock private RecipientDetailsRepository recipientDetailsRepository;
    @Mock private ShipmentManifestRepository shipmentManifestRepository;
    @Mock private TelemetrySettingsRepository telemetrySettingsRepository;
    @Mock private CourierLocationHistoryRepository courierLocationHistoryRepository;
    @Mock private ReturnShipmentRepository returnShipmentRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private User merchant;
    private User courier;
    private Zone zone;
    private ShipmentStatus pendingStatus;
    private ShipmentStatus pendingApprovalStatus;
    private ShipmentStatus deliveredStatus;
    private ShipmentStatus assignedStatus;
    private ShipmentStatus returnedToOriginStatus;
    private RecipientDetails recipientDetails;
    private Shipment sampleShipment;

    @BeforeEach
    void setUp() {
        Role merchantRole = new Role("MERCHANT");
        merchantRole.setId(1L);
        merchant = new User();
        merchant.setId(10L);
        merchant.setName("Test Merchant");
        merchant.setRole(merchantRole);

        Role courierRole = new Role("COURIER");
        courierRole.setId(2L);
        courier = new User();
        courier.setId(20L);
        courier.setName("Test Courier");
        courier.setRole(courierRole);

        zone = new Zone();
        zone.setId(1L);
        zone.setName("Cairo");
        zone.setDefaultFee(new BigDecimal("50.00"));

        pendingStatus = new ShipmentStatus("PENDING", "قيد الانتظار");
        pendingStatus.setId(1L);
        pendingApprovalStatus = new ShipmentStatus("PENDING_APPROVAL", "بانتظار الموافقة");
        pendingApprovalStatus.setId(5L);
        deliveredStatus = new ShipmentStatus("DELIVERED", "تم التسليم");
        deliveredStatus.setId(2L);
        assignedStatus = new ShipmentStatus("ASSIGNED_TO_COURIER", "مُعيّن للسائق");
        assignedStatus.setId(3L);
        returnedToOriginStatus = new ShipmentStatus("RETURNED_TO_ORIGIN", "مرتجع للأصل");
        returnedToOriginStatus.setId(4L);

        recipientDetails = new RecipientDetails("01012345678", "أحمد محمد", "القاهرة - مصر");

        sampleShipment = new Shipment();
        sampleShipment.setId(100L);
        sampleShipment.setTrackingNumber("TS100001");
        sampleShipment.setMerchant(merchant);
        sampleShipment.setZone(zone);
        sampleShipment.setStatus(pendingStatus);
        sampleShipment.setRecipientDetails(recipientDetails);
        sampleShipment.setItemValue(new BigDecimal("200.00"));
        sampleShipment.setCodAmount(new BigDecimal("200.00"));
        sampleShipment.setDeliveryFee(new BigDecimal("50.00"));
        sampleShipment.setCreatedAt(Instant.now());
        sampleShipment.setUpdatedAt(Instant.now());
    }

    // ======== createShipment(Shipment) ========

    @Test
    @DisplayName("createShipment — يجب إنشاء شحنة جديدة وتعيين رقم تتبع وحالة PENDING_APPROVAL")
    void createShipment_success() {
        Shipment input = new Shipment();

        // createShipment(Shipment) → createShipment(shipment, null, null)
        // It looks up PENDING_APPROVAL first, then PENDING as fallback
        // NOTE: .orElse() evaluates eagerly, so PENDING must also be stubbed
        when(shipmentStatusRepository.findByName("PENDING_APPROVAL"))
                .thenReturn(Optional.of(pendingApprovalStatus));
        when(shipmentStatusRepository.findByName("PENDING"))
                .thenReturn(Optional.of(pendingStatus));
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(inv -> {
                    Shipment s = inv.getArgument(0);
                    s.setId(1L);
                    return s;
                });
        when(shipmentStatusHistoryRepository.save(any(ShipmentStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Shipment result = shipmentService.createShipment(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTrackingNumber()).startsWith("TWS-");
        assertThat(result.getStatus().getName()).isEqualTo("PENDING_APPROVAL");
        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentStatusHistoryRepository).save(any(ShipmentStatusHistory.class));
    }

    @Test
    @DisplayName("createShipment — يجب استخدام PENDING إذا لم يوجد PENDING_APPROVAL")
    void createShipment_fallbackToPending() {
        Shipment input = new Shipment();

        when(shipmentStatusRepository.findByName("PENDING_APPROVAL"))
                .thenReturn(Optional.empty());
        when(shipmentStatusRepository.findByName("PENDING"))
                .thenReturn(Optional.of(pendingStatus));
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(inv -> {
                    Shipment s = inv.getArgument(0);
                    s.setId(2L);
                    return s;
                });
        when(shipmentStatusHistoryRepository.save(any(ShipmentStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Shipment result = shipmentService.createShipment(input);

        assertThat(result.getStatus().getName()).isEqualTo("PENDING");
    }

    // ======== getShipmentById ========

    @Test
    @DisplayName("getShipmentById — يجب إرجاع الشحنة عند وجود المعرف")
    void getShipmentById_found() {
        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(sampleShipment));

        Optional<Shipment> result = shipmentService.getShipmentById(100L);

        assertThat(result).isPresent();
        assertThat(result.get().getTrackingNumber()).isEqualTo("TS100001");
    }

    @Test
    @DisplayName("getShipmentById — يجب إرجاع empty عند عدم وجود الشحنة")
    void getShipmentById_notFound() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Shipment> result = shipmentService.getShipmentById(999L);

        assertThat(result).isEmpty();
    }

    // ======== findByTrackingNumber ========

    @Test
    @DisplayName("findByTrackingNumber — يجب إرجاع الشحنة عند وجود رقم التتبع")
    void findByTrackingNumber_found() {
        when(shipmentRepository.findByTrackingNumber("TS100001"))
                .thenReturn(Optional.of(sampleShipment));

        Shipment result = shipmentService.findByTrackingNumber("TS100001");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("findByTrackingNumber — يجب إرجاع null عند عدم وجود رقم التتبع")
    void findByTrackingNumber_notFound() {
        when(shipmentRepository.findByTrackingNumber("INVALID"))
                .thenReturn(Optional.empty());

        Shipment result = shipmentService.findByTrackingNumber("INVALID");

        assertThat(result).isNull();
    }

    // ======== findById ========

    @Test
    @DisplayName("findById — يجب إرجاع null عند عدم وجود الشحنة")
    void findById_notFound() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        Shipment result = shipmentService.findById(999L);

        assertThat(result).isNull();
    }

    // ======== updateStatus ========

    @Test
    @DisplayName("updateStatus — يجب تحديث الحالة وإضافة سجل في التاريخ")
    void updateStatus_success() {
        when(shipmentRepository.findByTrackingNumber("TS100001"))
                .thenReturn(Optional.of(sampleShipment));
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(shipmentStatusHistoryRepository.save(any(ShipmentStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Shipment result = shipmentService.updateStatus("TS100001", deliveredStatus, "تم التسليم للعميل");

        assertThat(result.getStatus().getName()).isEqualTo("DELIVERED");
        assertThat(result.getDeliveredAt()).isNotNull();
        verify(shipmentStatusHistoryRepository).save(any(ShipmentStatusHistory.class));
    }

    @Test
    @DisplayName("updateStatus — يجب تعيين deliveredAt عند حالة DELIVERED")
    void updateStatus_setsDeliveredAt() {
        when(shipmentRepository.findByTrackingNumber("TS100001"))
                .thenReturn(Optional.of(sampleShipment));
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(shipmentStatusHistoryRepository.save(any(ShipmentStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Shipment result = shipmentService.updateStatus("TS100001", deliveredStatus);

        assertThat(result.getDeliveredAt()).isNotNull();
    }

    @Test
    @DisplayName("updateStatus — يجب رمي IllegalArgumentException إذا لم يتم العثور على الشحنة")
    void updateStatus_shipmentNotFound() {
        when(shipmentRepository.findByTrackingNumber("BAD"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                shipmentService.updateStatus("BAD", deliveredStatus, "reason")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Shipment not found");
    }

    // ======== updateStatusWithReason ========

    @Test
    @DisplayName("updateStatusWithReason — يجب رمي IllegalArgumentException لحالة غير صالحة")
    void updateStatusWithReason_invalidStatus() {
        when(shipmentStatusRepository.findByName("INVALID_STATUS"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                shipmentService.updateStatusWithReason("TS100001", "INVALID_STATUS", "reason")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Invalid status");
    }

    // ======== getStatusByName ========

    @Test
    @DisplayName("getStatusByName — يجب إرجاع الحالة عند وجود الاسم")
    void getStatusByName_found() {
        when(shipmentStatusRepository.findByName("DELIVERED"))
                .thenReturn(Optional.of(deliveredStatus));

        Optional<ShipmentStatus> result = shipmentService.getStatusByName("DELIVERED");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("DELIVERED");
    }

    // ======== deleteShipment ========

    @Test
    @DisplayName("deleteShipment — يجب حذف الشحنة وسجلات الحالة بنجاح")
    void deleteShipment_success() {
        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(sampleShipment));
        doNothing().when(shipmentStatusHistoryRepository).deleteByShipment(sampleShipment);
        doNothing().when(shipmentRepository).delete(sampleShipment);

        shipmentService.deleteShipment(100L);

        verify(shipmentStatusHistoryRepository).deleteByShipment(sampleShipment);
        verify(shipmentRepository).delete(sampleShipment);
    }

    @Test
    @DisplayName("deleteShipment — يجب رمي IllegalArgumentException إذا لم يتم العثور على الشحنة")
    void deleteShipment_notFound() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.deleteShipment(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ======== createReturnShipment ========

    @Test
    @DisplayName("createReturnShipment — يجب إنشاء شحنة إرجاع وتحديث حالة الشحنة الأصلية")
    void createReturnShipment_success() {
        when(shipmentStatusRepository.findByName("RETURNED_TO_ORIGIN"))
                .thenReturn(Optional.of(returnedToOriginStatus));
        when(shipmentStatusRepository.findByName("PENDING_APPROVAL"))
                .thenReturn(Optional.of(pendingApprovalStatus));
        // NOTE: .orElse() evaluates eagerly, so PENDING must also be stubbed
        when(shipmentStatusRepository.findByName("PENDING"))
                .thenReturn(Optional.of(pendingStatus));
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(inv -> {
                    Shipment s = inv.getArgument(0);
                    if (s.getId() == null) s.setId(200L);
                    return s;
                });
        when(shipmentStatusHistoryRepository.save(any(ShipmentStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(returnShipmentRepository.save(any(ReturnShipment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Shipment returnShipment = shipmentService.createReturnShipment(sampleShipment, "damaged");

        assertThat(returnShipment).isNotNull();
        assertThat(returnShipment.getTrackingNumber()).startsWith("TWS-");
        assertThat(sampleShipment.getStatus().getName()).isEqualTo("RETURNED_TO_ORIGIN");
        verify(returnShipmentRepository).save(any(ReturnShipment.class));
        // original + return shipment = 2 saves
        verify(shipmentRepository, times(2)).save(any(Shipment.class));
    }

    // ======== updateCourierLocation ========

    @Test
    @DisplayName("updateCourierLocation — يجب حفظ موقع السائق الجديد")
    void updateCourierLocation_success() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(courier));
        when(courierLocationHistoryRepository.save(any(CourierLocationHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        shipmentService.updateCourierLocation(20L, 30.0444, 31.2357);

        verify(courierLocationHistoryRepository).save(argThat(loc ->
                loc.getLatitude().compareTo(BigDecimal.valueOf(30.0444)) == 0 &&
                loc.getLongitude().compareTo(BigDecimal.valueOf(31.2357)) == 0));
    }

    @Test
    @DisplayName("updateCourierLocation — يجب رمي استثناء لمستخدم غير سائق")
    void updateCourierLocation_notCourier() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(merchant));

        assertThatThrownBy(() -> shipmentService.updateCourierLocation(10L, 30.0, 31.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not a courier");
    }

    // ======== getAllStatuses ========

    @Test
    @DisplayName("getAllStatuses — يجب إرجاع قائمة بجميع حالات الشحنات")
    void getAllStatuses_returnsList() {
        when(shipmentStatusRepository.findAll())
                .thenReturn(List.of(pendingStatus, deliveredStatus, assignedStatus));

        List<ShipmentStatus> result = shipmentService.getAllStatuses();

        assertThat(result).hasSize(3);
    }

    // ======== statusExistsByName ========

    @Test
    @DisplayName("statusExistsByName — يجب إرجاع true إذا كانت الحالة موجودة")
    void statusExistsByName_true() {
        when(shipmentStatusRepository.existsByName("PENDING")).thenReturn(true);

        assertThat(shipmentService.statusExistsByName("PENDING")).isTrue();
    }

    @Test
    @DisplayName("statusExistsByName — يجب إرجاع false إذا لم تكن الحالة موجودة")
    void statusExistsByName_false() {
        when(shipmentStatusRepository.existsByName("NONEXISTENT")).thenReturn(false);

        assertThat(shipmentService.statusExistsByName("NONEXISTENT")).isFalse();
    }

    // ======== createStatus ========

    @Test
    @DisplayName("createStatus — يجب رمي IllegalArgumentException إذا كانت الحالة موجودة مسبقاً")
    void createStatus_duplicate() {
        when(shipmentStatusRepository.existsByName("PENDING")).thenReturn(true);

        assertThatThrownBy(() -> shipmentService.createStatus("PENDING", "desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("createStatus — يجب إنشاء حالة جديدة بنجاح")
    void createStatus_success() {
        when(shipmentStatusRepository.existsByName("NEW_STATUS")).thenReturn(false);
        when(shipmentStatusRepository.save(any(ShipmentStatus.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ShipmentStatus result = shipmentService.createStatus("NEW_STATUS", "حالة جديدة");

        assertThat(result.getName()).isEqualTo("NEW_STATUS");
        verify(shipmentStatusRepository).save(any(ShipmentStatus.class));
    }
}
