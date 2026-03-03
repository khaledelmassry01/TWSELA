package com.twsela.service;

import com.twsela.domain.Contract;
import com.twsela.domain.CustomPricingRule;
import com.twsela.domain.Zone;
import com.twsela.repository.ContractRepository;
import com.twsela.repository.CustomPricingRuleRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.ZoneRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Custom pricing service — resolves effective pricing for merchants with contracts.
 * Priority: Contract pricing > Volume discount > Default pricing.
 */
@Service
@Transactional
public class CustomPricingService {

    private static final Logger log = LoggerFactory.getLogger(CustomPricingService.class);

    private final ContractRepository contractRepository;
    private final CustomPricingRuleRepository pricingRuleRepository;
    private final ShipmentRepository shipmentRepository;
    private final ZoneRepository zoneRepository;

    public CustomPricingService(ContractRepository contractRepository,
                                 CustomPricingRuleRepository pricingRuleRepository,
                                 ShipmentRepository shipmentRepository,
                                 ZoneRepository zoneRepository) {
        this.contractRepository = contractRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.shipmentRepository = shipmentRepository;
        this.zoneRepository = zoneRepository;
    }

    /**
     * Calculate effective price for a merchant shipment.
     * Checks active contract → falls back to default pricing.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calculatePrice(Long merchantId, Long zoneFromId, Long zoneToId,
                                                double weightKg, BigDecimal codAmount) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. Check for active contract
        Optional<Contract> activeContract = contractRepository.findActiveByPartyId(merchantId);
        if (activeContract.isPresent()) {
            Contract contract = activeContract.get();
            List<CustomPricingRule> rules = pricingRuleRepository.findActiveByContractIdAndZones(
                    contract.getId(), zoneFromId, zoneToId);

            if (!rules.isEmpty()) {
                CustomPricingRule rule = rules.get(0); // Most specific rule

                BigDecimal base = rule.getBasePrice() != null ? rule.getBasePrice() : BigDecimal.ZERO;
                BigDecimal weightCharge = rule.getPerKgPrice() != null
                        ? rule.getPerKgPrice().multiply(BigDecimal.valueOf(weightKg))
                        : BigDecimal.ZERO;
                BigDecimal codFee = BigDecimal.ZERO;
                if (codAmount != null && rule.getCodFeePercent() != null) {
                    codFee = codAmount.multiply(rule.getCodFeePercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                BigDecimal subtotal = base.add(weightCharge).add(codFee);

                // Apply volume discount if eligible
                BigDecimal discount = BigDecimal.ZERO;
                if (rule.getDiscountPercent() != null && rule.getMinMonthlyShipments() > 0) {
                    long monthlyCount = shipmentRepository.countByMerchantIdAndCreatedAtBetween(
                            merchantId,
                            Instant.now().minus(30, ChronoUnit.DAYS),
                            Instant.now());
                    if (monthlyCount >= rule.getMinMonthlyShipments()) {
                        discount = subtotal.multiply(rule.getDiscountPercent())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    }
                }

                BigDecimal total = subtotal.subtract(discount);
                BigDecimal minimum = rule.getMinimumCharge() != null ? rule.getMinimumCharge() : BigDecimal.ZERO;
                total = total.max(minimum);

                result.put("basePrice", base);
                result.put("weightCharge", weightCharge);
                result.put("codFee", codFee);
                result.put("discount", discount);
                result.put("totalPrice", total);
                result.put("source", "CONTRACT");
                result.put("contractNumber", contract.getContractNumber());
                return result;
            }
        }

        // 2. Fallback to default pricing (simplified)
        BigDecimal defaultBase = new BigDecimal("25.00");
        BigDecimal defaultPerKg = new BigDecimal("2.00");
        BigDecimal weightCharge = defaultPerKg.multiply(BigDecimal.valueOf(weightKg));
        BigDecimal codFee = BigDecimal.ZERO;
        if (codAmount != null) {
            codFee = codAmount.multiply(new BigDecimal("0.02")); // 2% COD fee
        }
        BigDecimal total = defaultBase.add(weightCharge).add(codFee);

        result.put("basePrice", defaultBase);
        result.put("weightCharge", weightCharge);
        result.put("codFee", codFee);
        result.put("discount", BigDecimal.ZERO);
        result.put("totalPrice", total);
        result.put("source", "DEFAULT");
        return result;
    }

    /**
     * Add a pricing rule to a contract.
     */
    public CustomPricingRule addPricingRule(Long contractId, Long zoneFromId, Long zoneToId,
                                            String shipmentType, BigDecimal basePrice,
                                            BigDecimal perKgPrice, BigDecimal codFeePercent,
                                            BigDecimal minimumCharge, BigDecimal discountPercent,
                                            int minMonthlyShipments) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));

        CustomPricingRule rule = new CustomPricingRule();
        rule.setContract(contract);
        if (zoneFromId != null) {
            rule.setZoneFrom(zoneRepository.findById(zoneFromId)
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", zoneFromId)));
        }
        if (zoneToId != null) {
            rule.setZoneTo(zoneRepository.findById(zoneToId)
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", zoneToId)));
        }
        rule.setShipmentType(shipmentType);
        rule.setBasePrice(basePrice);
        rule.setPerKgPrice(perKgPrice);
        rule.setCodFeePercent(codFeePercent);
        rule.setMinimumCharge(minimumCharge);
        rule.setDiscountPercent(discountPercent);
        rule.setMinMonthlyShipments(minMonthlyShipments);

        rule = pricingRuleRepository.save(rule);
        log.info("Pricing rule {} added to contract {}", rule.getId(), contractId);
        return rule;
    }

    /**
     * Update a pricing rule.
     */
    public CustomPricingRule updatePricingRule(Long ruleId, BigDecimal basePrice,
                                                BigDecimal perKgPrice, BigDecimal codFeePercent,
                                                BigDecimal minimumCharge, BigDecimal discountPercent,
                                                int minMonthlyShipments, boolean active) {
        CustomPricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomPricingRule", "id", ruleId));

        if (basePrice != null) rule.setBasePrice(basePrice);
        if (perKgPrice != null) rule.setPerKgPrice(perKgPrice);
        if (codFeePercent != null) rule.setCodFeePercent(codFeePercent);
        if (minimumCharge != null) rule.setMinimumCharge(minimumCharge);
        if (discountPercent != null) rule.setDiscountPercent(discountPercent);
        rule.setMinMonthlyShipments(minMonthlyShipments);
        rule.setActive(active);
        rule.setUpdatedAt(Instant.now());

        return pricingRuleRepository.save(rule);
    }

    /**
     * Get the effective pricing rule for a merchant/zone combination.
     */
    @Transactional(readOnly = true)
    public Optional<CustomPricingRule> getEffectivePricing(Long merchantId, Long zoneFromId, Long zoneToId) {
        Optional<Contract> contract = contractRepository.findActiveByPartyId(merchantId);
        if (contract.isEmpty()) return Optional.empty();

        List<CustomPricingRule> rules = pricingRuleRepository.findActiveByContractIdAndZones(
                contract.get().getId(), zoneFromId, zoneToId);
        return rules.isEmpty() ? Optional.empty() : Optional.of(rules.get(0));
    }

    /**
     * Get all pricing rules for a contract.
     */
    @Transactional(readOnly = true)
    public List<CustomPricingRule> getPricingRules(Long contractId) {
        return pricingRuleRepository.findByContractId(contractId);
    }
}
