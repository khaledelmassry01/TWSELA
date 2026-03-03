package com.twsela.repository;

import com.twsela.domain.ECommerceConnection;
import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ECommerceConnectionRepository extends JpaRepository<ECommerceConnection, Long> {

    List<ECommerceConnection> findByMerchantId(Long merchantId);

    Optional<ECommerceConnection> findByMerchantIdAndPlatform(Long merchantId, ECommercePlatform platform);

    List<ECommerceConnection> findByActiveTrue();

    List<ECommerceConnection> findByPlatformAndActiveTrue(ECommercePlatform platform);
}
