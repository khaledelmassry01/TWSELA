package com.twsela.repository;

import com.twsela.domain.TenantConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, Long> {

    Optional<TenantConfiguration> findByTenantIdAndConfigKey(Long tenantId, String configKey);

    List<TenantConfiguration> findByTenantIdAndCategory(Long tenantId, TenantConfiguration.ConfigCategory category);

    List<TenantConfiguration> findByTenantId(Long tenantId);
}
