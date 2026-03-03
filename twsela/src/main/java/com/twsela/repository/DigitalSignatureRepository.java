package com.twsela.repository;

import com.twsela.domain.DigitalSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, Long> {
    Optional<DigitalSignature> findBySignatureRequestId(Long signatureRequestId);
}
