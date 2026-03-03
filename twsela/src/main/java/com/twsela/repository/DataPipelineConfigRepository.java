package com.twsela.repository;

import com.twsela.domain.DataPipelineConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataPipelineConfigRepository extends JpaRepository<DataPipelineConfig, Long> {
    List<DataPipelineConfig> findByIsActiveTrue();
    List<DataPipelineConfig> findByTenantId(Long tenantId);
}
