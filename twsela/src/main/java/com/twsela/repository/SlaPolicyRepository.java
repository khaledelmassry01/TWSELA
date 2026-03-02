package com.twsela.repository;

import com.twsela.domain.SlaPolicy;
import com.twsela.domain.SupportTicket.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {

    Optional<SlaPolicy> findByPriorityAndActiveTrue(TicketPriority priority);
}
