package com.twsela.repository;

import com.twsela.domain.DomainEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DomainEventRepository extends JpaRepository<DomainEvent, Long> {

    List<DomainEvent> findByAggregateTypeAndAggregateIdOrderByVersionAsc(String aggregateType, Long aggregateId);

    List<DomainEvent> findByEventType(String eventType);

    List<DomainEvent> findByStatusOrderByCreatedAtAsc(DomainEvent.EventStatus status);

    @Query("SELECT e FROM DomainEvent e WHERE e.status = :status AND e.createdAt < :before ORDER BY e.createdAt ASC")
    List<DomainEvent> findByStatusAndCreatedAtBefore(@Param("status") DomainEvent.EventStatus status,
                                                      @Param("before") Instant before);

    Optional<DomainEvent> findByEventId(String eventId);

    @Query("SELECT MAX(e.version) FROM DomainEvent e WHERE e.aggregateType = :type AND e.aggregateId = :id")
    Integer findMaxVersionByAggregate(@Param("type") String aggregateType, @Param("id") Long aggregateId);
}
