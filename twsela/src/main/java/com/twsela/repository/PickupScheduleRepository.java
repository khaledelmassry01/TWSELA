package com.twsela.repository;

import com.twsela.domain.PickupSchedule;
import com.twsela.domain.PickupSchedule.PickupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PickupScheduleRepository extends JpaRepository<PickupSchedule, Long> {

    Page<PickupSchedule> findByMerchantIdOrderByPickupDateDesc(Long merchantId, Pageable pageable);

    Page<PickupSchedule> findByMerchantIdAndStatusOrderByPickupDateDesc(Long merchantId, PickupStatus status, Pageable pageable);

    List<PickupSchedule> findByAssignedCourierIdAndPickupDate(Long courierId, LocalDate date);

    List<PickupSchedule> findByPickupDateAndStatus(LocalDate date, PickupStatus status);

    @Query("SELECT p FROM PickupSchedule p WHERE p.status IN ('SCHEDULED','ASSIGNED') " +
           "AND p.pickupDate < :date ORDER BY p.pickupDate ASC")
    List<PickupSchedule> findOverdue(@Param("date") LocalDate date);

    Page<PickupSchedule> findByStatusOrderByPickupDateAsc(PickupStatus status, Pageable pageable);

    long countByMerchantId(Long merchantId);
}
