package com.twsela.repository;

import com.twsela.domain.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findByPublishedFalse();

    List<OutboxMessage> findByPublishedFalseOrderByCreatedAtAsc();
}
