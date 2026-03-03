package com.twsela.repository;

import com.twsela.domain.LiveNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveNotificationRepository extends JpaRepository<LiveNotification, Long> {

    List<LiveNotification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    List<LiveNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
