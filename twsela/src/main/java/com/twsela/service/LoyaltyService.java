package com.twsela.service;

import com.twsela.domain.LoyaltyProgram;
import com.twsela.domain.LoyaltyTransaction;
import com.twsela.repository.LoyaltyProgramRepository;
import com.twsela.repository.LoyaltyTransactionRepository;
import com.twsela.web.dto.GamificationLoyaltyDTO.*;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LoyaltyService {

    private final LoyaltyProgramRepository programRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    public LoyaltyService(LoyaltyProgramRepository programRepository,
                          LoyaltyTransactionRepository transactionRepository) {
        this.programRepository = programRepository;
        this.transactionRepository = transactionRepository;
    }

    // === Loyalty Programs ===

    @Transactional(readOnly = true)
    public LoyaltyProgramResponse getProgramByMerchantId(Long merchantId) {
        LoyaltyProgram p = programRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty program not found for merchant: " + merchantId));
        return toProgramResponse(p);
    }

    public LoyaltyProgramResponse initializeProgram(Long merchantId, Long tenantId) {
        if (programRepository.existsByMerchantId(merchantId)) {
            throw new DuplicateResourceException("Loyalty program already exists for merchant: " + merchantId);
        }
        LoyaltyProgram p = new LoyaltyProgram();
        p.setMerchantId(merchantId);
        p.setTenantId(tenantId);
        return toProgramResponse(programRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<LoyaltyProgramResponse> getProgramsByTenant(Long tenantId) {
        return programRepository.findByTenantId(tenantId)
                .stream().map(this::toProgramResponse).toList();
    }

    // === Transactions ===

    public LoyaltyTransactionResponse createTransaction(CreateLoyaltyTransactionRequest request, Long tenantId) {
        LoyaltyProgram program = programRepository.findById(request.loyaltyProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty program not found: " + request.loyaltyProgramId()));

        LoyaltyTransaction t = new LoyaltyTransaction();
        t.setLoyaltyProgramId(request.loyaltyProgramId());
        t.setTransactionType(request.transactionType());
        t.setPoints(request.points());
        t.setReferenceType(request.referenceType());
        t.setReferenceId(request.referenceId());
        t.setDescription(request.description());
        t.setTenantId(tenantId);

        // Update program balance
        program.setCurrentPoints(program.getCurrentPoints() + request.points());
        if (request.points() > 0) {
            program.setLifetimePoints(program.getLifetimePoints() + request.points());
        }
        program.setLastActivityAt(LocalDateTime.now());
        programRepository.save(program);

        t.setBalanceAfter(program.getCurrentPoints());
        return toTransactionResponse(transactionRepository.save(t));
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> getTransactions(Long programId) {
        return transactionRepository.findByLoyaltyProgramIdOrderByCreatedAtDesc(programId)
                .stream().map(this::toTransactionResponse).toList();
    }

    // === Mappers ===

    private LoyaltyProgramResponse toProgramResponse(LoyaltyProgram p) {
        return new LoyaltyProgramResponse(p.getId(), p.getMerchantId(), p.getCurrentPoints(), p.getLifetimePoints(),
                p.getTier(), p.getTierExpiresAt(), p.getPointsExpiringAt(), p.getPointsExpiring(),
                p.getLastActivityAt(), p.getTenantId(), p.getCreatedAt());
    }

    private LoyaltyTransactionResponse toTransactionResponse(LoyaltyTransaction t) {
        return new LoyaltyTransactionResponse(t.getId(), t.getLoyaltyProgramId(), t.getTransactionType(),
                t.getPoints(), t.getBalanceAfter(), t.getReferenceType(), t.getReferenceId(),
                t.getDescription(), t.getExpiresAt(), t.getCreatedAt());
    }
}
