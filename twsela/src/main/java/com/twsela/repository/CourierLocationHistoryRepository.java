package com.twsela.repository;

import com.twsela.domain.CourierLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface CourierLocationHistoryRepository extends JpaRepository<CourierLocationHistory, Long> {
    List<CourierLocationHistory> findByCourierIdOrderByTimestampDesc(Long courierId);
    
    @Query("SELECT clh FROM CourierLocationHistory clh WHERE clh.courier.id = :courierId AND clh.timestamp >= :fromTime ORDER BY clh.timestamp DESC")
    List<CourierLocationHistory> findByCourierIdAndTimestampAfter(@Param("courierId") Long courierId, @Param("fromTime") Instant fromTime);
    
    @Query("SELECT clh FROM CourierLocationHistory clh WHERE clh.courier.id = :courierId AND clh.timestamp BETWEEN :startTime AND :endTime ORDER BY clh.timestamp DESC")
    List<CourierLocationHistory> findByCourierIdAndTimestampBetween(@Param("courierId") Long courierId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
}
