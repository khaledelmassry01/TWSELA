package com.twsela.repository;

import com.twsela.domain.EInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface EInvoiceRepository extends JpaRepository<EInvoice, Long> {

    Optional<EInvoice> findByInvoiceId(Long invoiceId);

    List<EInvoice> findByStatus(EInvoice.EInvoiceStatus status);

    @Query("SELECT ei FROM EInvoice ei WHERE ei.countryCode = :country " +
           "AND ei.submittedAt BETWEEN :from AND :to")
    List<EInvoice> findByCountryAndSubmittedAtBetween(@Param("country") String countryCode,
                                                       @Param("from") Instant from,
                                                       @Param("to") Instant to);
}
