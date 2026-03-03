package com.twsela.repository;

import com.twsela.domain.RecipientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecipientProfileRepository extends JpaRepository<RecipientProfile, Long> {
    Optional<RecipientProfile> findByPhone(String phone);
    boolean existsByPhone(String phone);
}
