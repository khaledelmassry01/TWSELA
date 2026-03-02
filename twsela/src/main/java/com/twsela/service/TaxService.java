package com.twsela.service;

import com.twsela.domain.TaxRule;
import com.twsela.web.exception.ResourceNotFoundException;
import com.twsela.repository.TaxRuleRepository;
import com.twsela.web.dto.CountryDTO.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TaxService {

    private final TaxRuleRepository taxRuleRepository;

    public TaxService(TaxRuleRepository taxRuleRepository) {
        this.taxRuleRepository = taxRuleRepository;
    }

    /**
     * Calculate total tax for an amount in a given country.
     */
    @Transactional(readOnly = true)
    public TaxCalculationResponse calculateTax(BigDecimal amount, String countryCode) {
        List<TaxRule> rules = taxRuleRepository.findApplicable(
                countryCode.toUpperCase(), LocalDate.now());

        if (rules.isEmpty()) {
            return new TaxCalculationResponse(amount, countryCode,
                    BigDecimal.ZERO, BigDecimal.ZERO, amount, "NONE");
        }

        // Use primary tax rule (first applicable — typically VAT)
        TaxRule rule = rules.get(0);
        BigDecimal taxAmount = amount.multiply(rule.getRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = amount.add(taxAmount);

        return new TaxCalculationResponse(amount, countryCode,
                rule.getRate(), taxAmount, total, rule.getTaxType().name());
    }

    @Transactional(readOnly = true)
    public List<TaxRuleResponse> getApplicableRules(String countryCode) {
        return taxRuleRepository.findByCountryCodeAndActiveTrue(countryCode.toUpperCase())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TaxRuleResponse> getAllRules() {
        return taxRuleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TaxRuleResponse createRule(CreateTaxRuleRequest req) {
        TaxRule rule = new TaxRule();
        rule.setCountryCode(req.countryCode().toUpperCase());
        rule.setTaxType(TaxRule.TaxType.valueOf(req.taxType()));
        rule.setRate(req.rate());
        rule.setExemptCategories(req.exemptCategories());
        rule.setValidFrom(req.validFrom());
        rule.setValidTo(req.validTo());
        rule.setActive(true);
        return toResponse(taxRuleRepository.save(rule));
    }

    public TaxRuleResponse updateRule(Long id, CreateTaxRuleRequest req) {
        TaxRule rule = taxRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("قاعدة ضريبية غير موجودة: " + id));
        rule.setCountryCode(req.countryCode().toUpperCase());
        rule.setTaxType(TaxRule.TaxType.valueOf(req.taxType()));
        rule.setRate(req.rate());
        rule.setExemptCategories(req.exemptCategories());
        rule.setValidFrom(req.validFrom());
        rule.setValidTo(req.validTo());
        return toResponse(taxRuleRepository.save(rule));
    }

    @Transactional(readOnly = true)
    public boolean isExempt(String countryCode, String category) {
        List<TaxRule> rules = taxRuleRepository.findApplicable(
                countryCode.toUpperCase(), LocalDate.now());
        return rules.stream().anyMatch(r -> {
            String exempt = r.getExemptCategories();
            return exempt != null && exempt.contains(category);
        });
    }

    private TaxRuleResponse toResponse(TaxRule r) {
        return new TaxRuleResponse(r.getId(), r.getCountryCode(),
                r.getTaxType().name(), r.getRate(), r.getExemptCategories(),
                r.getValidFrom(), r.getValidTo(), r.isActive());
    }
}
