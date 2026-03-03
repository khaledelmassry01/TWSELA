package com.twsela.repository;

import com.twsela.domain.ArchivedRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivedRecordRepository extends JpaRepository<ArchivedRecord, Long> {
    List<ArchivedRecord> findByOriginalTableAndOriginalId(String originalTable, Long originalId);
    List<ArchivedRecord> findByArchivePolicyId(Long archivePolicyId);
    List<ArchivedRecord> findByTenantId(Long tenantId);
}
