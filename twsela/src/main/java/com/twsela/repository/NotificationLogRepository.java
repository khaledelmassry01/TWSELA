package com.twsela.repository;

import com.twsela.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    
    List<NotificationLog> findByRecipientPhoneOrderBySentAtDesc(String recipientPhone);
    
    List<NotificationLog> findByMessageTypeOrderBySentAtDesc(String messageType);
    
    List<NotificationLog> findByStatusOrderBySentAtDesc(String status);
    
    @Query("SELECT n FROM NotificationLog n WHERE n.sentAt BETWEEN :startDate AND :endDate ORDER BY n.sentAt DESC")
    List<NotificationLog> findBySentAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
    
    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.recipientPhone = :phone AND n.messageType = :messageType AND n.sentAt >= :since")
    Long countByRecipientPhoneAndMessageTypeSince(@Param("phone") String phone, @Param("messageType") String messageType, @Param("since") Instant since);
}
