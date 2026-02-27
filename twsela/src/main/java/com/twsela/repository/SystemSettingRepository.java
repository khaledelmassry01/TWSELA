package com.twsela.repository;

import com.twsela.domain.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    List<SystemSetting> findByUserId(Long userId);

    @Query("SELECT s FROM SystemSetting s WHERE s.user.id = :userId AND s.settingKey = :key")
    Optional<SystemSetting> findByUserIdAndSettingKey(@Param("userId") Long userId, @Param("key") String key);

    void deleteByUserId(Long userId);
}
