package com.twsela.repository;

import com.twsela.domain.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {

    List<DeliveryAttempt> findByShipmentIdOrderByAttemptNumberAsc(Long shipmentId);

    List<DeliveryAttempt> findByShipmentIdOrderByAttemptNumberDesc(Long shipmentId);

    int countByShipmentId(Long shipmentId);

    @Query("SELECT a FROM DeliveryAttempt a WHERE a.courier.id = :courierId " +
           "AND a.attemptedAt BETWEEN :from AND :to ORDER BY a.attemptedAt DESC")
    List<DeliveryAttempt> findByCourierAndDateRange(@Param("courierId") Long courierId,
                                                     @Param("from") Instant from,
                                                     @Param("to") Instant to);

    @Query("SELECT a.failureReason, COUNT(a) FROM DeliveryAttempt a " +
           "WHERE a.status = com.twsela.domain.DeliveryAttempt$AttemptStatus.FAILED " +
           "AND a.attemptedAt BETWEEN :from AND :to GROUP BY a.failureReason")
    List<Object[]> countFailuresByReason(@Param("from") Instant from, @Param("to") Instant to);
}
