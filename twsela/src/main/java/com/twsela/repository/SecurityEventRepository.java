package com.twsela.repository;

import com.twsela.domain.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(e) FROM SecurityEvent e WHERE e.eventType = :type AND e.createdAt BETWEEN :start AND :end")
    long countByEventTypeAndCreatedAtBetween(@Param("type") SecurityEvent.EventType type,
                                              @Param("start") Instant start,
                                              @Param("end") Instant end);

    List<SecurityEvent> findByIpAddressAndEventType(String ipAddress, SecurityEvent.EventType eventType);

    List<SecurityEvent> findBySeverity(SecurityEvent.Severity severity);

    List<SecurityEvent> findBySeverityOrderByCreatedAtDesc(SecurityEvent.Severity severity);

    @Query("SELECT e FROM SecurityEvent e WHERE e.ipAddress = :ip AND e.eventType = :type AND e.createdAt > :since")
    List<SecurityEvent> findRecentByIpAndType(@Param("ip") String ipAddress,
                                               @Param("type") SecurityEvent.EventType type,
                                               @Param("since") Instant since);
}
