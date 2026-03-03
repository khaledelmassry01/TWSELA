package com.twsela.repository;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationDeliveryLog;
import com.twsela.domain.NotificationDeliveryLog.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {

    List<NotificationDeliveryLog> findByNotificationId(Long notificationId);

    List<NotificationDeliveryLog> findByStatusAndNextRetryAtBefore(DeliveryStatus status, Instant now);

    @Query("SELECT COUNT(l) FROM NotificationDeliveryLog l " +
           "WHERE l.channel = :channel AND l.status = :status " +
           "AND l.sentAt BETWEEN :from AND :to")
    long countByChannelAndStatusAndSentAtBetween(
            @Param("channel") NotificationChannel channel,
            @Param("status") DeliveryStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to);

    List<NotificationDeliveryLog> findByRecipientAndSentAtBetween(String recipient, Instant from, Instant to);

    @Query("SELECT l.channel, l.status, COUNT(l) FROM NotificationDeliveryLog l " +
           "WHERE l.sentAt BETWEEN :from AND :to " +
           "GROUP BY l.channel, l.status ORDER BY l.channel, l.status")
    List<Object[]> getDeliveryStatsByChannelAndStatus(@Param("from") Instant from, @Param("to") Instant to);
}
