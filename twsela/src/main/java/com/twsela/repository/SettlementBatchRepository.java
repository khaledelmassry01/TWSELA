package com.twsela.repository;

import com.twsela.domain.SettlementBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> {

    List<SettlementBatch> findByStatus(SettlementBatch.BatchStatus status);

    Optional<SettlementBatch> findByPeriodAndStartDate(SettlementBatch.SettlementPeriod period, LocalDate startDate);

    List<SettlementBatch> findByStatusOrderByCreatedAtDesc(SettlementBatch.BatchStatus status);

    @Query("SELECT sb FROM SettlementBatch sb WHERE sb.startDate >= :start AND sb.endDate <= :end ORDER BY sb.createdAt DESC")
    List<SettlementBatch> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
