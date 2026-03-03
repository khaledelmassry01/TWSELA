package com.twsela.repository;

import com.twsela.domain.PipelineExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PipelineExecutionRepository extends JpaRepository<PipelineExecution, Long> {
    List<PipelineExecution> findByPipelineConfigId(Long pipelineConfigId);
    List<PipelineExecution> findByStatus(String status);
    List<PipelineExecution> findByTenantId(Long tenantId);
}
