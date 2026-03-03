package com.twsela.repository;

import com.twsela.domain.DeliveryPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryPreferenceRepository extends JpaRepository<DeliveryPreference, Long> {
    Optional<DeliveryPreference> findByRecipientProfileId(Long recipientProfileId);
}
