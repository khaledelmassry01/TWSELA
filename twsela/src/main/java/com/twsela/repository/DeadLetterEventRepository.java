package com.twsela.repository;

import com.twsela.domain.DeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, Long> {

    @Query("SELECT d FROM DeadLetterEvent d WHERE d.resolved = false ORDER BY d.createdAt DESC")
    List<DeadLetterEvent> findUnresolved();

    List<DeadLetterEvent> findByOriginalEventEventType(String eventType);

    long countByResolved(boolean resolved);
}
