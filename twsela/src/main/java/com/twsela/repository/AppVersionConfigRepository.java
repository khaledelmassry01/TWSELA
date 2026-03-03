package com.twsela.repository;

import com.twsela.domain.AppVersionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppVersionConfigRepository extends JpaRepository<AppVersionConfig, Long> {
    List<AppVersionConfig> findByPlatform(String platform);
}
