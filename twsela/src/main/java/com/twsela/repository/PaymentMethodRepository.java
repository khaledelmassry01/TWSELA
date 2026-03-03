package com.twsela.repository;

import com.twsela.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByUserIdAndActiveTrue(Long userId);

    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<PaymentMethod> findByTokenizedRef(String tokenizedRef);

    List<PaymentMethod> findByUserId(Long userId);
}
