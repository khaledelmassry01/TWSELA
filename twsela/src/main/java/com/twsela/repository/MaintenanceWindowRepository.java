package com.twsela.repository;

import com.twsela.domain.MaintenanceWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindow, Long> {
    List<MaintenanceWindow> findByStatus(String status);
    List<MaintenanceWindow> findByTenantId(Long tenantId);
}
