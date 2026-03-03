package com.twsela.repository;

import com.twsela.domain.SignatureRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignatureRequestRepository extends JpaRepository<SignatureRequest, Long> {
    Optional<SignatureRequest> findByToken(String token);
    List<SignatureRequest> findByDocumentId(Long documentId);
    List<SignatureRequest> findByStatus(String status);
    List<SignatureRequest> findByTenantId(Long tenantId);
}
