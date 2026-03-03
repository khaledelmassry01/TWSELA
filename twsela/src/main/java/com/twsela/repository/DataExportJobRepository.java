package com.twsela.repository;

import com.twsela.domain.DataExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataExportJobRepository extends JpaRepository<DataExportJob, Long> {
    List<DataExportJob> findByStatus(String status);
    List<DataExportJob> findByRequestedById(Long requestedById);
    List<DataExportJob> findByTenantId(Long tenantId);
}
