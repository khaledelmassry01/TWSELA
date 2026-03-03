package com.twsela.repository;

import com.twsela.domain.ApiKeyUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ApiKeyUsageLogRepository extends JpaRepository<ApiKeyUsageLog, Long> {

    long countByApiKeyIdAndRequestedAtBetween(Long apiKeyId, Instant from, Instant to);

    List<ApiKeyUsageLog> findByApiKeyIdOrderByRequestedAtDesc(Long apiKeyId);
}
