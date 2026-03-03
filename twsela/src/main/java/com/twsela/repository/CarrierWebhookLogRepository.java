package com.twsela.repository;

import com.twsela.domain.CarrierWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarrierWebhookLogRepository extends JpaRepository<CarrierWebhookLog, Long> {
    List<CarrierWebhookLog> findByCarrierIdOrderByCreatedAtDesc(Long carrierId);
    List<CarrierWebhookLog> findByProcessedFalseOrderByCreatedAtAsc();
    List<CarrierWebhookLog> findByCarrierIdAndEventType(Long carrierId, String eventType);
}
