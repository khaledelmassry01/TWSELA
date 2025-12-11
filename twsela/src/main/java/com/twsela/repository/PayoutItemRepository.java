package com.twsela.repository;

import com.twsela.domain.PayoutItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PayoutItemRepository extends JpaRepository<PayoutItem, Long> {
    List<PayoutItem> findByPayoutId(Long payoutId);
    List<PayoutItem> findBySourceTypeAndSourceId(PayoutItem.SourceType sourceType, Long sourceId);
}

