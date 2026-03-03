package com.twsela.service;

import com.twsela.domain.Contract;
import com.twsela.domain.ContractSlaTerms;
import com.twsela.repository.ContractSlaTermsRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for SLA compliance checking and penalty calculations.
 */
@Service
@Transactional
public class ContractSlaService {

    private static final Logger log = LoggerFactory.getLogger(ContractSlaService.class);

    private final ContractSlaTermsRepository slaRepository;
    private final ShipmentRepository shipmentRepository;
    private final ContractService contractService;

    public ContractSlaService(ContractSlaTermsRepository slaRepository,
                               ShipmentRepository shipmentRepository,
                               ContractService contractService) {
        this.slaRepository = slaRepository;
        this.shipmentRepository = shipmentRepository;
        this.contractService = contractService;
    }

    /**
     * Save or update SLA terms for a contract.
     */
    public ContractSlaTerms saveSlaTerms(Long contractId, double targetDeliveryRate,
                                          int maxDeliveryHours, BigDecimal latePenalty,
                                          BigDecimal lostPenalty,
                                          ContractSlaTerms.SlaReviewPeriod reviewPeriod) {
        Contract contract = contractService.findById(contractId);

        ContractSlaTerms sla = slaRepository.findByContractId(contractId)
                .orElseGet(() -> {
                    ContractSlaTerms newSla = new ContractSlaTerms();
                    newSla.setContract(contract);
                    return newSla;
                });

        sla.setTargetDeliveryRate(targetDeliveryRate);
        sla.setMaxDeliveryHours(maxDeliveryHours);
        sla.setLatePenaltyPerShipment(latePenalty);
        sla.setLostPenaltyFixed(lostPenalty);
        sla.setSlaReviewPeriod(reviewPeriod);
        sla.setUpdatedAt(Instant.now());

        sla = slaRepository.save(sla);
        log.info("SLA terms saved for contract {}", contractId);
        return sla;
    }

    /**
     * Get SLA terms for a contract.
     */
    @Transactional(readOnly = true)
    public Optional<ContractSlaTerms> getSlaTerms(Long contractId) {
        return slaRepository.findByContractId(contractId);
    }

    /**
     * Check SLA compliance for a contract over a date range.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkSlaCompliance(Long contractId, Instant from, Instant to) {
        Contract contract = contractService.findById(contractId);
        ContractSlaTerms sla = slaRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractSlaTerms", "contractId", contractId));

        Long partyId = contract.getParty().getId();

        // Total and delivered shipments for this merchant
        long totalShipments = shipmentRepository.countByMerchantIdAndCreatedAtBetween(partyId, from, to);
        long delivered = shipmentRepository.countByMerchantIdAndStatusNameAndCreatedAtBetween(
                partyId, "DELIVERED", from, to);

        double actualRate = totalShipments > 0 ? (double) delivered / totalShipments : 0;
        boolean isCompliant = actualRate >= sla.getTargetDeliveryRate();

        // Late shipments (delivered but over max hours)
        var allShipments = shipmentRepository.findByMerchantIdAndCreatedAtBetween(partyId, from, to);
        long lateShipments = allShipments.stream()
                .filter(s -> s.getStatus() != null && "DELIVERED".equals(s.getStatus().getName()))
                .filter(s -> s.getUpdatedAt() != null && s.getCreatedAt() != null)
                .filter(s -> Duration.between(s.getCreatedAt(), s.getUpdatedAt()).toHours() > sla.getMaxDeliveryHours())
                .count();

        // Lost shipments (RETURNED or LOST status)
        long lostShipments = shipmentRepository.countByMerchantIdAndStatusNameAndCreatedAtBetween(
                partyId, "RETURNED", from, to);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("contractId", contractId);
        report.put("contractNumber", contract.getContractNumber());
        report.put("targetRate", sla.getTargetDeliveryRate());
        report.put("actualRate", Math.round(actualRate * 10000.0) / 10000.0);
        report.put("isCompliant", isCompliant);
        report.put("totalShipments", totalShipments);
        report.put("deliveredShipments", delivered);
        report.put("lateShipments", lateShipments);
        report.put("lostShipments", lostShipments);
        return report;
    }

    /**
     * Calculate penalties for SLA violations.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calculatePenalties(Long contractId, Instant from, Instant to) {
        ContractSlaTerms sla = slaRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractSlaTerms", "contractId", contractId));

        Map<String, Object> compliance = checkSlaCompliance(contractId, from, to);

        long lateShipments = (long) compliance.get("lateShipments");
        long lostShipments = (long) compliance.get("lostShipments");

        BigDecimal latePenalties = sla.getLatePenaltyPerShipment() != null
                ? sla.getLatePenaltyPerShipment().multiply(BigDecimal.valueOf(lateShipments))
                : BigDecimal.ZERO;

        BigDecimal lostPenalties = sla.getLostPenaltyFixed() != null
                ? sla.getLostPenaltyFixed().multiply(BigDecimal.valueOf(lostShipments))
                : BigDecimal.ZERO;

        BigDecimal totalPenalties = latePenalties.add(lostPenalties);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contractId", contractId);
        result.put("lateShipments", lateShipments);
        result.put("latePenalties", latePenalties);
        result.put("lostShipments", lostShipments);
        result.put("lostPenalties", lostPenalties);
        result.put("totalPenalties", totalPenalties);
        return result;
    }
}
