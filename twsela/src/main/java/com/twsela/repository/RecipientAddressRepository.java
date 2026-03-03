package com.twsela.repository;

import com.twsela.domain.RecipientAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecipientAddressRepository extends JpaRepository<RecipientAddress, Long> {
    List<RecipientAddress> findByRecipientProfileId(Long recipientProfileId);
    List<RecipientAddress> findByRecipientProfileIdAndIsDefaultTrue(Long recipientProfileId);
}
