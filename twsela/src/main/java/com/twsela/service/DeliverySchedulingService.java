package com.twsela.service;

import com.twsela.domain.DeliveryTimeSlot;
import com.twsela.domain.DeliveryBooking;
import com.twsela.repository.DeliveryTimeSlotRepository;
import com.twsela.repository.DeliveryBookingRepository;
import com.twsela.web.dto.RecipientExperienceDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeliverySchedulingService {

    private final DeliveryTimeSlotRepository slotRepo;
    private final DeliveryBookingRepository bookingRepo;

    public DeliverySchedulingService(DeliveryTimeSlotRepository slotRepo,
                                      DeliveryBookingRepository bookingRepo) {
        this.slotRepo = slotRepo;
        this.bookingRepo = bookingRepo;
    }

    @Transactional(readOnly = true)
    public List<DeliveryTimeSlotResponse> getSlotsByZone(Long zoneId) {
        return slotRepo.findByZoneIdAndIsActiveTrue(zoneId).stream().map(this::toSlotResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryTimeSlotResponse> getSlotsByZoneAndDay(Long zoneId, Integer dayOfWeek) {
        return slotRepo.findByZoneIdAndDayOfWeekAndIsActiveTrue(zoneId, dayOfWeek).stream()
                .map(this::toSlotResponse).toList();
    }

    public DeliveryTimeSlotResponse createSlot(CreateDeliveryTimeSlotRequest req) {
        var s = new DeliveryTimeSlot();
        s.setZoneId(req.zoneId());
        s.setDayOfWeek(req.dayOfWeek());
        s.setStartTime(req.startTime());
        s.setEndTime(req.endTime());
        if (req.maxCapacity() != null) s.setMaxCapacity(req.maxCapacity());
        if (req.surchargeAmount() != null) s.setSurchargeAmount(req.surchargeAmount());
        s.setDisplayNameAr(req.displayNameAr());
        return toSlotResponse(slotRepo.save(s));
    }

    // ── Bookings ──
    @Transactional(readOnly = true)
    public List<DeliveryBookingResponse> getBookingsByShipment(Long shipmentId) {
        return bookingRepo.findByShipmentId(shipmentId).stream().map(this::toBookingResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryBookingResponse> getBookingsByRecipient(Long recipientProfileId) {
        return bookingRepo.findByRecipientProfileIdOrderByCreatedAtDesc(recipientProfileId).stream()
                .map(this::toBookingResponse).toList();
    }

    public DeliveryBookingResponse createBooking(CreateDeliveryBookingRequest req) {
        var b = new DeliveryBooking();
        b.setShipmentId(req.shipmentId());
        b.setDeliveryTimeSlotId(req.deliveryTimeSlotId());
        b.setRecipientProfileId(req.recipientProfileId());
        b.setSelectedDate(req.selectedDate());
        var saved = bookingRepo.save(b);
        // increment slot bookings count
        slotRepo.findById(req.deliveryTimeSlotId()).ifPresent(slot -> {
            slot.setCurrentBookings(slot.getCurrentBookings() + 1);
            slotRepo.save(slot);
        });
        return toBookingResponse(saved);
    }

    public DeliveryBookingResponse updateBookingStatus(Long id, String status) {
        var b = bookingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الحجز غير موجود"));
        b.setStatus(status);
        return toBookingResponse(bookingRepo.save(b));
    }

    private DeliveryTimeSlotResponse toSlotResponse(DeliveryTimeSlot s) {
        return new DeliveryTimeSlotResponse(s.getId(), s.getZoneId(), s.getDayOfWeek(),
                s.getStartTime(), s.getEndTime(), s.getMaxCapacity(), s.getCurrentBookings(),
                s.getIsActive(), s.getSurchargeAmount(), s.getDisplayNameAr(), s.getCreatedAt());
    }

    private DeliveryBookingResponse toBookingResponse(DeliveryBooking b) {
        return new DeliveryBookingResponse(b.getId(), b.getShipmentId(), b.getDeliveryTimeSlotId(),
                b.getRecipientProfileId(), b.getSelectedDate(), b.getStatus(),
                b.getRescheduledFromId(), b.getRescheduledReason(), b.getBookedAt(), b.getCreatedAt());
    }
}
