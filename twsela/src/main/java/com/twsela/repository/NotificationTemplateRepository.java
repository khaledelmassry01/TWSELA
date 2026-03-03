package com.twsela.repository;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationTemplate;
import com.twsela.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByEventTypeAndChannel(NotificationType eventType, NotificationChannel channel);

    List<NotificationTemplate> findByEventType(NotificationType eventType);

    List<NotificationTemplate> findByActiveTrue();

    List<NotificationTemplate> findByEventTypeAndActiveTrue(NotificationType eventType);
}
