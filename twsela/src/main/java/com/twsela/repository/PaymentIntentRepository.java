package com.twsela.repository;

import com.twsela.domain.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

    List<PaymentIntent> findByShipmentId(Long shipmentId);

    List<PaymentIntent> findByStatus(PaymentIntent.IntentStatus status);

    Optional<PaymentIntent> findByProviderRef(String providerRef);

    @Query("SELECT pi FROM PaymentIntent pi WHERE pi.status = 'PENDING' AND pi.expiresAt < :now")
    List<PaymentIntent> findExpired(@Param("now") Instant now);

    List<PaymentIntent> findByShipmentIdAndStatus(Long shipmentId, PaymentIntent.IntentStatus status);
}
