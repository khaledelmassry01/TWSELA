package com.twsela.repository;

import com.twsela.domain.PaymentWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentWebhookLogRepository extends JpaRepository<PaymentWebhookLog, Long> {

    List<PaymentWebhookLog> findByProviderAndEventType(String provider, String eventType);

    List<PaymentWebhookLog> findByProcessedFalseOrderByCreatedAtAsc();

    List<PaymentWebhookLog> findByProviderOrderByCreatedAtDesc(String provider);
}
