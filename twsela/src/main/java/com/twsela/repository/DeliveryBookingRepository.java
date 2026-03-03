package com.twsela.repository;

import com.twsela.domain.DeliveryBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DeliveryBookingRepository extends JpaRepository<DeliveryBooking, Long> {
    List<DeliveryBooking> findByShipmentId(Long shipmentId);
    List<DeliveryBooking> findByRecipientProfileIdOrderByCreatedAtDesc(Long recipientProfileId);
    List<DeliveryBooking> findByDeliveryTimeSlotIdAndSelectedDate(Long slotId, LocalDate date);
}
