package com.twsela.repository;

import com.twsela.domain.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCode(String code);

    boolean existsByCode(String code);

    List<PromoCode> findByIsActiveTrueOrderByCreatedAtDesc();

    List<PromoCode> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);

    List<PromoCode> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
