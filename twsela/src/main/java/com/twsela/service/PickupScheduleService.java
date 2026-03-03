package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.PickupSchedule.PickupStatus;
import com.twsela.repository.PickupScheduleRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing merchant pickup schedules.
 */
@Service
@Transactional
public class PickupScheduleService {

    private static final Logger log = LoggerFactory.getLogger(PickupScheduleService.class);

    private final PickupScheduleRepository pickupRepository;
    private final UserRepository userRepository;

    public PickupScheduleService(PickupScheduleRepository pickupRepository,
                                  UserRepository userRepository) {
        this.pickupRepository = pickupRepository;
        this.userRepository = userRepository;
    }

    /**
     * Schedule a new pickup.
     */
    public PickupSchedule schedulePickup(Long merchantId, LocalDate pickupDate,
                                          PickupSchedule.TimeSlot timeSlot, String address,
                                          Double latitude, Double longitude,
                                          int estimatedShipments, String notes) {
        User merchant = userRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", merchantId));

        if (pickupDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("تاريخ الاستلام يجب أن يكون اليوم أو في المستقبل");
        }
        if (estimatedShipments <= 0) {
            throw new BusinessRuleException("عدد الشحنات المتوقع يجب أن يكون أكبر من صفر");
        }

        PickupSchedule pickup = new PickupSchedule();
        pickup.setMerchant(merchant);
        pickup.setPickupDate(pickupDate);
        pickup.setTimeSlot(timeSlot);
        pickup.setAddress(address);
        pickup.setLatitude(latitude);
        pickup.setLongitude(longitude);
        pickup.setEstimatedShipments(estimatedShipments);
        pickup.setNotes(notes);
        pickup.setStatus(PickupStatus.SCHEDULED);

        PickupSchedule saved = pickupRepository.save(pickup);
        log.info("Pickup #{} scheduled for merchant {} on {} ({})",
                saved.getId(), merchantId, pickupDate, timeSlot);
        return saved;
    }

    /**
     * Assign a courier to a pickup.
     */
    public PickupSchedule assignCourier(Long pickupId, Long courierId) {
        PickupSchedule pickup = getPickup(pickupId);

        if (pickup.getStatus() != PickupStatus.SCHEDULED) {
            throw new BusinessRuleException("لا يمكن تعيين مندوب — حالة الموعد: " + pickup.getStatus());
        }

        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", courierId));

        pickup.setAssignedCourier(courier);
        pickup.setStatus(PickupStatus.ASSIGNED);
        pickup.setUpdatedAt(Instant.now());

        log.info("Courier {} assigned to pickup #{}", courierId, pickupId);
        return pickupRepository.save(pickup);
    }

    /**
     * Start a pickup (courier is on the way / picking up).
     */
    public PickupSchedule startPickup(Long pickupId) {
        PickupSchedule pickup = getPickup(pickupId);
        if (pickup.getStatus() != PickupStatus.ASSIGNED) {
            throw new BusinessRuleException("لا يمكن بدء الاستلام — الحالة الحالية: " + pickup.getStatus());
        }

        pickup.setStatus(PickupStatus.IN_PROGRESS);
        pickup.setUpdatedAt(Instant.now());

        log.info("Pickup #{} started", pickupId);
        return pickupRepository.save(pickup);
    }

    /**
     * Complete a pickup.
     */
    public PickupSchedule completePickup(Long pickupId) {
        PickupSchedule pickup = getPickup(pickupId);
        if (pickup.getStatus() != PickupStatus.IN_PROGRESS) {
            throw new BusinessRuleException("لا يمكن إتمام الاستلام — الحالة الحالية: " + pickup.getStatus());
        }

        pickup.setStatus(PickupStatus.COMPLETED);
        pickup.setCompletedAt(Instant.now());
        pickup.setUpdatedAt(Instant.now());

        log.info("Pickup #{} completed", pickupId);
        return pickupRepository.save(pickup);
    }

    /**
     * Cancel a pickup.
     */
    public PickupSchedule cancelPickup(Long pickupId) {
        PickupSchedule pickup = getPickup(pickupId);
        if (pickup.getStatus() == PickupStatus.COMPLETED || pickup.getStatus() == PickupStatus.CANCELLED) {
            throw new BusinessRuleException("لا يمكن إلغاء الاستلام — الحالة الحالية: " + pickup.getStatus());
        }

        pickup.setStatus(PickupStatus.CANCELLED);
        pickup.setUpdatedAt(Instant.now());

        log.info("Pickup #{} cancelled", pickupId);
        return pickupRepository.save(pickup);
    }

    // ── Queries ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PickupSchedule getPickup(Long id) {
        return pickupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PickupSchedule", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<PickupSchedule> getMerchantPickups(Long merchantId, Pageable pageable) {
        return pickupRepository.findByMerchantIdOrderByPickupDateDesc(merchantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<PickupSchedule> getCourierPickups(Long courierId, LocalDate date) {
        return pickupRepository.findByAssignedCourierIdAndPickupDate(courierId, date);
    }

    @Transactional(readOnly = true)
    public List<PickupSchedule> getScheduledPickupsForDate(LocalDate date) {
        return pickupRepository.findByPickupDateAndStatus(date, PickupStatus.SCHEDULED);
    }

    @Transactional(readOnly = true)
    public List<PickupSchedule> getOverduePickups() {
        return pickupRepository.findOverdue(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Page<PickupSchedule> getPickupsByStatus(PickupStatus status, Pageable pageable) {
        return pickupRepository.findByStatusOrderByPickupDateAsc(status, pageable);
    }
}
