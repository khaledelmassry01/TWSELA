package com.twsela.repository;

import com.twsela.domain.AssignmentScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentScoreRepository extends JpaRepository<AssignmentScore, Long> {

    List<AssignmentScore> findByShipmentIdOrderByTotalScoreDesc(Long shipmentId);

    Optional<AssignmentScore> findTopByShipmentIdOrderByTotalScoreDesc(Long shipmentId);

    List<AssignmentScore> findByShipmentIdAndCourierId(Long shipmentId, Long courierId);

    @Modifying
    @Query("DELETE FROM AssignmentScore s WHERE s.calculatedAt < :before")
    int deleteByCalculatedAtBefore(@Param("before") Instant before);
}
