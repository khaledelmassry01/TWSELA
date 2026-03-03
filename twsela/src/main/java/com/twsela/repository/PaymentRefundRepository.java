package com.twsela.repository;

import com.twsela.domain.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    List<PaymentRefund> findByPaymentIntentId(Long paymentIntentId);

    List<PaymentRefund> findByStatus(PaymentRefund.RefundStatus status);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM PaymentRefund r WHERE r.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentRefund.RefundStatus status);

    List<PaymentRefund> findByStatusOrderByCreatedAtDesc(PaymentRefund.RefundStatus status);
}
