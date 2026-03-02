package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.ReturnShipment.ReturnStatusEnum;
import com.twsela.repository.ReturnShipmentRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

    @Mock private ReturnShipmentRepository returnRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReturnService returnService;

    private Shipment deliveredShipment;
    private Shipment pendingShipment;
    private User courier;
    private ReturnShipment sampleReturn;

    @BeforeEach
    void setUp() {
        ShipmentStatus deliveredStatus = new ShipmentStatus();
        deliveredStatus.setName("DELIVERED");
        deliveredShipment = new Shipment();
        deliveredShipment.setId(1L);
        deliveredShipment.setTrackingNumber("TS000001");
        deliveredShipment.setStatus(deliveredStatus);
        deliveredShipment.setDeliveryFee(new BigDecimal("50.00"));

        ShipmentStatus pendingStatus = new ShipmentStatus();
        pendingStatus.setName("PENDING");
        pendingShipment = new Shipment();
        pendingShipment.setId(2L);
        pendingShipment.setStatus(pendingStatus);

        courier = new User();
        courier.setId(10L);
        courier.setName("Test Courier");

        sampleReturn = new ReturnShipment(deliveredShipment, "damaged");
        sampleReturn.setId(100L);
        sampleReturn.setReturnFee(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("createReturn — delivered shipment succeeds")
    void createReturn_deliveredShipment_succeeds() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(deliveredShipment));
        when(returnRepository.existsByOriginalShipmentIdAndStatusNot(eq(1L), any())).thenReturn(false);
        when(returnRepository.save(any(ReturnShipment.class))).thenAnswer(inv -> {
            ReturnShipment r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        ReturnShipment result = returnService.createReturn(1L, "damaged", "notes", "admin");

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getReason()).isEqualTo("damaged");
        assertThat(result.getReturnFee()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(result.getStatus()).isEqualTo(ReturnStatusEnum.RETURN_REQUESTED);
    }

    @Test
    @DisplayName("createReturn — pending shipment throws BusinessRuleException")
    void createReturn_pendingShipment_throws() {
        when(shipmentRepository.findById(2L)).thenReturn(Optional.of(pendingShipment));

        assertThatThrownBy(() -> returnService.createReturn(2L, "reason", null, "user"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("createReturn — shipment not found throws")
    void createReturn_shipmentNotFound_throws() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> returnService.createReturn(999L, "reason", null, "user"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createReturn — active return already exists throws")
    void createReturn_duplicateReturn_throws() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(deliveredShipment));
        when(returnRepository.existsByOriginalShipmentIdAndStatusNot(eq(1L), any())).thenReturn(true);

        assertThatThrownBy(() -> returnService.createReturn(1L, "reason", null, "user"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("updateStatus — valid transition REQUESTED → APPROVED succeeds")
    void updateStatus_validTransition_succeeds() {
        sampleReturn.setStatus(ReturnStatusEnum.RETURN_REQUESTED);
        when(returnRepository.findById(100L)).thenReturn(Optional.of(sampleReturn));
        when(returnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReturnShipment result = returnService.updateStatus(100L, ReturnStatusEnum.RETURN_APPROVED);

        assertThat(result.getStatus()).isEqualTo(ReturnStatusEnum.RETURN_APPROVED);
        assertThat(result.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("updateStatus — invalid transition throws BusinessRuleException")
    void updateStatus_invalidTransition_throws() {
        sampleReturn.setStatus(ReturnStatusEnum.RETURN_REQUESTED);
        when(returnRepository.findById(100L)).thenReturn(Optional.of(sampleReturn));

        assertThatThrownBy(() -> returnService.updateStatus(100L, ReturnStatusEnum.RETURN_PICKED_UP))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("updateStatus — REJECTED is a valid terminal transition from REQUESTED")
    void updateStatus_rejectFromRequested_succeeds() {
        sampleReturn.setStatus(ReturnStatusEnum.RETURN_REQUESTED);
        when(returnRepository.findById(100L)).thenReturn(Optional.of(sampleReturn));
        when(returnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReturnShipment result = returnService.updateStatus(100L, ReturnStatusEnum.RETURN_REJECTED);
        assertThat(result.getStatus()).isEqualTo(ReturnStatusEnum.RETURN_REJECTED);
    }

    @Test
    @DisplayName("assignCourier — approved return assigns courier successfully")
    void assignCourier_approvedReturn_succeeds() {
        sampleReturn.setStatus(ReturnStatusEnum.RETURN_APPROVED);
        when(returnRepository.findById(100L)).thenReturn(Optional.of(sampleReturn));
        when(userRepository.findById(10L)).thenReturn(Optional.of(courier));
        when(returnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReturnShipment result = returnService.assignCourier(100L, 10L);

        assertThat(result.getStatus()).isEqualTo(ReturnStatusEnum.RETURN_PICKUP_ASSIGNED);
        assertThat(result.getAssignedCourier().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("assignCourier — non-approved return throws")
    void assignCourier_notApproved_throws() {
        sampleReturn.setStatus(ReturnStatusEnum.RETURN_REQUESTED);
        when(returnRepository.findById(100L)).thenReturn(Optional.of(sampleReturn));

        assertThatThrownBy(() -> returnService.assignCourier(100L, 10L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("getReturnById — existing return found")
    void getReturnById_found() {
        when(returnRepository.findById(100L)).thenReturn(Optional.of(sampleReturn));

        ReturnShipment result = returnService.getReturnById(100L);
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getReturnById — not found throws")
    void getReturnById_notFound_throws() {
        when(returnRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> returnService.getReturnById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
