package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.ReturnShipment.ReturnStatusEnum;
import com.twsela.repository.ReturnShipmentRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.dto.ReturnResponseDTO;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing return shipments lifecycle.
 */
@Service
@Transactional
public class ReturnService {

    private static final Logger log = LoggerFactory.getLogger(ReturnService.class);

    /**
     * Default return fee as percentage of original delivery fee.
     */
    private static final BigDecimal RETURN_FEE_RATE = new BigDecimal("0.50");

    /**
     * Allowed status transitions for returns.
     */
    private static final Map<ReturnStatusEnum, Set<ReturnStatusEnum>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(ReturnStatusEnum.class);
        VALID_TRANSITIONS.put(ReturnStatusEnum.RETURN_REQUESTED,
                Set.of(ReturnStatusEnum.RETURN_APPROVED, ReturnStatusEnum.RETURN_REJECTED));
        VALID_TRANSITIONS.put(ReturnStatusEnum.RETURN_APPROVED,
                Set.of(ReturnStatusEnum.RETURN_PICKUP_ASSIGNED));
        VALID_TRANSITIONS.put(ReturnStatusEnum.RETURN_PICKUP_ASSIGNED,
                Set.of(ReturnStatusEnum.RETURN_PICKED_UP));
        VALID_TRANSITIONS.put(ReturnStatusEnum.RETURN_PICKED_UP,
                Set.of(ReturnStatusEnum.RETURN_IN_WAREHOUSE));
        VALID_TRANSITIONS.put(ReturnStatusEnum.RETURN_IN_WAREHOUSE,
                Set.of(ReturnStatusEnum.RETURN_DELIVERED_TO_MERCHANT));
    }

    /**
     * Statuses from which a shipment can be returned.
     */
    private static final Set<String> RETURNABLE_STATUSES = Set.of(
            ShipmentStatusConstants.DELIVERED,
            ShipmentStatusConstants.PARTIALLY_DELIVERED,
            ShipmentStatusConstants.FAILED_DELIVERY,
            ShipmentStatusConstants.FAILED_ATTEMPT
    );

    private final ReturnShipmentRepository returnRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public ReturnService(ReturnShipmentRepository returnRepository,
                         ShipmentRepository shipmentRepository,
                         UserRepository userRepository) {
        this.returnRepository = returnRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new return request.
     */
    public ReturnShipment createReturn(Long shipmentId, String reason, String notes, String createdBy) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        // Validate shipment status allows return
        String currentStatus = shipment.getStatus() != null ? shipment.getStatus().getName() : "";
        if (!RETURNABLE_STATUSES.contains(currentStatus)) {
            throw new BusinessRuleException("لا يمكن إرجاع شحنة بحالة: " + currentStatus);
        }

        // Check no active return already exists
        boolean activeReturnExists = returnRepository.existsByOriginalShipmentIdAndStatusNot(
                shipmentId, ReturnStatusEnum.RETURN_REJECTED);
        if (activeReturnExists) {
            throw new BusinessRuleException("يوجد طلب إرجاع نشط لهذه الشحنة بالفعل");
        }

        ReturnShipment returnShipment = new ReturnShipment(shipment, reason);
        returnShipment.setNotes(notes);
        returnShipment.setCreatedBy(createdBy);

        // Calculate return fee (50% of delivery fee)
        if (shipment.getDeliveryFee() != null) {
            returnShipment.setReturnFee(shipment.getDeliveryFee().multiply(RETURN_FEE_RATE));
        } else {
            returnShipment.setReturnFee(BigDecimal.ZERO);
        }

        ReturnShipment saved = returnRepository.save(returnShipment);
        log.info("Return request created: id={}, shipment={}, reason='{}'", saved.getId(), shipmentId, reason);
        return saved;
    }

    /**
     * Update return status with validation of allowed transitions.
     */
    public ReturnShipment updateStatus(Long returnId, ReturnStatusEnum newStatus) {
        ReturnShipment ret = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnShipment", "id", returnId));

        ReturnStatusEnum current = ret.getStatus();
        Set<ReturnStatusEnum> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessRuleException(
                    "لا يمكن الانتقال من " + current + " إلى " + newStatus);
        }

        ret.setStatus(newStatus);

        // Set timestamps for milestone statuses
        switch (newStatus) {
            case RETURN_APPROVED -> ret.setApprovedAt(Instant.now());
            case RETURN_PICKED_UP -> ret.setPickedUpAt(Instant.now());
            case RETURN_DELIVERED_TO_MERCHANT -> ret.setDeliveredAt(Instant.now());
            default -> { /* no timestamp for other statuses */ }
        }

        log.info("Return {} status changed: {} → {}", returnId, current, newStatus);
        return returnRepository.save(ret);
    }

    /**
     * Assign a courier to pick up the return.
     */
    public ReturnShipment assignCourier(Long returnId, Long courierId) {
        ReturnShipment ret = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnShipment", "id", returnId));

        if (ret.getStatus() != ReturnStatusEnum.RETURN_APPROVED) {
            throw new BusinessRuleException("يجب الموافقة على المرتجع قبل تعيين مندوب");
        }

        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", courierId));

        ret.setAssignedCourier(courier);
        ret.setStatus(ReturnStatusEnum.RETURN_PICKUP_ASSIGNED);
        log.info("Return {} assigned to courier {}", returnId, courierId);
        return returnRepository.save(ret);
    }

    /**
     * Get all returns (for admin/owner).
     */
    @Transactional(readOnly = true)
    public List<ReturnShipment> getAllReturns() {
        return returnRepository.findAll();
    }

    /**
     * Get returns for a specific merchant.
     */
    @Transactional(readOnly = true)
    public List<ReturnShipment> getReturnsByMerchant(Long merchantId) {
        return returnRepository.findByMerchantId(merchantId);
    }

    /**
     * Get returns assigned to a specific courier.
     */
    @Transactional(readOnly = true)
    public List<ReturnShipment> getReturnsByCourier(Long courierId) {
        return returnRepository.findByAssignedCourierId(courierId);
    }

    /**
     * Get a return by ID.
     */
    @Transactional(readOnly = true)
    public ReturnShipment getReturnById(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnShipment", "id", id));
    }

    /**
     * Map ReturnShipment entity to DTO.
     */
    public static ReturnResponseDTO toDTO(ReturnShipment ret) {
        ReturnResponseDTO dto = new ReturnResponseDTO();
        dto.setId(ret.getId());
        dto.setStatus(ret.getStatus() != null ? ret.getStatus().name() : null);
        dto.setReason(ret.getReason());
        dto.setNotes(ret.getNotes());
        dto.setReturnFee(ret.getReturnFee());
        dto.setCreatedAt(ret.getCreatedAt());
        dto.setApprovedAt(ret.getApprovedAt());
        dto.setPickedUpAt(ret.getPickedUpAt());
        dto.setDeliveredAt(ret.getDeliveredAt());
        dto.setCreatedBy(ret.getCreatedBy());

        if (ret.getOriginalShipment() != null) {
            dto.setOriginalShipmentId(ret.getOriginalShipment().getId());
            dto.setOriginalTrackingNumber(ret.getOriginalShipment().getTrackingNumber());
        }
        if (ret.getAssignedCourier() != null) {
            dto.setAssignedCourierName(ret.getAssignedCourier().getName());
        }
        return dto;
    }
}
