package com.twsela.repository;

import com.twsela.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {

    List<SettlementItem> findByBatchId(Long batchId);

    @Query("SELECT COALESCE(SUM(si.netAmount), 0) FROM SettlementItem si WHERE si.batch.id = :batchId")
    BigDecimal sumNetAmountByBatchId(@Param("batchId") Long batchId);

    List<SettlementItem> findByMerchantId(Long merchantId);

    List<SettlementItem> findByBatchIdAndType(Long batchId, SettlementItem.ItemType type);
}
