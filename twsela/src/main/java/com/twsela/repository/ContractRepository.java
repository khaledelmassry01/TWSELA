package com.twsela.repository;

import com.twsela.domain.Contract;
import com.twsela.domain.Contract.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByPartyId(Long partyId);

    List<Contract> findByPartyIdAndStatus(Long partyId, ContractStatus status);

    @Query("SELECT c FROM Contract c WHERE c.party.id = :userId AND c.status = 'ACTIVE' " +
           "AND CURRENT_DATE BETWEEN c.startDate AND c.endDate")
    Optional<Contract> findActiveByPartyId(@Param("userId") Long userId);

    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate <= :deadline")
    List<Contract> findExpiringWithin(@Param("deadline") LocalDate deadline);

    List<Contract> findByStatus(ContractStatus status);

    Optional<Contract> findByContractNumber(String contractNumber);

    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.autoRenew = true " +
           "AND c.endDate <= :deadline")
    List<Contract> findAutoRenewableExpiring(@Param("deadline") LocalDate deadline);
}
