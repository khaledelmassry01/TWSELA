package com.twsela.repository;

import com.twsela.domain.DeliveryTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryTimeSlotRepository extends JpaRepository<DeliveryTimeSlot, Long> {
    List<DeliveryTimeSlot> findByZoneIdAndIsActiveTrue(Long zoneId);
    List<DeliveryTimeSlot> findByZoneIdAndDayOfWeekAndIsActiveTrue(Long zoneId, Integer dayOfWeek);
}
