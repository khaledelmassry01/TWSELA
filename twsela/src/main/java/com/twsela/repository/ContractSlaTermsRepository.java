package com.twsela.repository;

import com.twsela.domain.ContractSlaTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractSlaTermsRepository extends JpaRepository<ContractSlaTerms, Long> {

    Optional<ContractSlaTerms> findByContractId(Long contractId);
}
