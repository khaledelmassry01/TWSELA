package com.twsela.repository;

import com.twsela.domain.PaymentTransaction;
import com.twsela.domain.PaymentTransaction.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    List<PaymentTransaction> findByInvoiceId(Long invoiceId);

    Optional<PaymentTransaction> findByExternalId(String externalId);

    List<PaymentTransaction> findByStatus(PaymentStatus status);
}
