package com.twsela.repository;

import com.twsela.domain.Invoice;
import com.twsela.domain.Invoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    Page<Invoice> findBySubscriptionMerchantIdOrderByCreatedAtDesc(Long merchantId, Pageable pageable);

    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'PENDING' AND i.dueDate < :now")
    List<Invoice> findOverdue(@Param("now") Instant now);

    boolean existsBySubscriptionIdAndStatus(Long subscriptionId, InvoiceStatus status);
}
