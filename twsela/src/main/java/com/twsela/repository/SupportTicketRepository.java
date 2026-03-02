package com.twsela.repository;

import com.twsela.domain.SupportTicket;
import com.twsela.domain.SupportTicket.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    Page<SupportTicket> findByReporterIdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);

    Page<SupportTicket> findByAssigneeIdOrderByCreatedAtDesc(Long assigneeId, Pageable pageable);

    List<SupportTicket> findByStatus(TicketStatus status);

    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

    @Query("SELECT t FROM SupportTicket t WHERE t.status IN :statuses AND t.priority = :priority ORDER BY t.createdAt ASC")
    List<SupportTicket> findOpenByPriority(@Param("statuses") List<TicketStatus> statuses,
                                            @Param("priority") TicketPriority priority);

    long countByStatus(TicketStatus status);
}
