package com.twsela.repository;

import com.twsela.domain.SystemHealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemHealthCheckRepository extends JpaRepository<SystemHealthCheck, Long> {
    List<SystemHealthCheck> findByComponent(String component);
    List<SystemHealthCheck> findByStatus(String status);
}
