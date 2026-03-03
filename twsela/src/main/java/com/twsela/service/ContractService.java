package com.twsela.service;

import com.twsela.domain.Contract;
import com.twsela.domain.Contract.ContractStatus;
import com.twsela.domain.Contract.ContractType;
import com.twsela.domain.User;
import com.twsela.repository.ContractRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing contracts between Twsela and merchants/couriers.
 */
@Service
@Transactional
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;

    public ContractService(ContractRepository contractRepository,
                           UserRepository userRepository,
                           OtpService otpService) {
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.otpService = otpService;
    }

    /**
     * Create a new contract in DRAFT status.
     */
    public Contract createContract(ContractType type, Long partyId, LocalDate startDate,
                                    LocalDate endDate, boolean autoRenew, int renewalNoticeDays,
                                    String termsDocument, String notes, Long createdById) {
        User party = userRepository.findById(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", partyId));
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createdById));

        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException("تاريخ النهاية يجب أن يكون بعد تاريخ البداية");
        }

        String contractNumber = "TWS-CTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Contract contract = new Contract(contractNumber, type, party, startDate, endDate);
        contract.setAutoRenew(autoRenew);
        contract.setRenewalNoticeDays(renewalNoticeDays);
        contract.setTermsDocument(termsDocument);
        contract.setNotes(notes);
        contract.setCreatedBy(createdBy);

        contract = contractRepository.save(contract);
        log.info("Contract {} created for party {} (type: {})", contractNumber, partyId, type);
        return contract;
    }

    /**
     * Send contract for electronic signature via OTP.
     */
    public Contract sendForSignature(Long contractId) {
        Contract contract = findById(contractId);
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessRuleException("يمكن إرسال العقود المسودة فقط للتوقيع");
        }

        String otp = otpService.generateOtp(contract.getParty().getPhone());
        contract.setSignatureOtp(otp);
        contract.setStatus(ContractStatus.PENDING_SIGNATURE);
        contract.setUpdatedAt(Instant.now());

        contract = contractRepository.save(contract);
        log.info("Contract {} sent for signature to party {}", contract.getContractNumber(), contract.getParty().getId());
        return contract;
    }

    /**
     * Sign contract by verifying OTP.
     */
    public Contract signContract(Long contractId, String otp) {
        Contract contract = findById(contractId);
        if (contract.getStatus() != ContractStatus.PENDING_SIGNATURE) {
            throw new BusinessRuleException("العقد ليس في حالة انتظار التوقيع");
        }

        boolean valid = otpService.verifyOtp(contract.getParty().getPhone(), otp);
        if (!valid) {
            throw new BusinessRuleException("رمز التحقق غير صالح أو منتهي الصلاحية");
        }

        contract.setStatus(ContractStatus.ACTIVE);
        contract.setSignedAt(Instant.now());
        contract.setSignatureOtp(null);
        contract.setUpdatedAt(Instant.now());

        contract = contractRepository.save(contract);
        log.info("Contract {} signed by party {}", contract.getContractNumber(), contract.getParty().getId());
        return contract;
    }

    /**
     * Terminate an active contract.
     */
    public Contract terminateContract(Long contractId, String reason) {
        Contract contract = findById(contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.EXPIRING_SOON) {
            throw new BusinessRuleException("يمكن إنهاء العقود النشطة فقط");
        }

        contract.setStatus(ContractStatus.TERMINATED);
        contract.setNotes(contract.getNotes() != null
                ? contract.getNotes() + "\n[إنهاء] " + reason
                : "[إنهاء] " + reason);
        contract.setUpdatedAt(Instant.now());

        contract = contractRepository.save(contract);
        log.info("Contract {} terminated: {}", contract.getContractNumber(), reason);
        return contract;
    }

    /**
     * Manually renew a contract.
     */
    public Contract renewContract(Long contractId) {
        Contract contract = findById(contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.EXPIRING_SOON
                && contract.getStatus() != ContractStatus.EXPIRED) {
            throw new BusinessRuleException("لا يمكن تجديد هذا العقد في حالته الحالية");
        }

        // Extend by same duration
        long durationDays = contract.getStartDate().until(contract.getEndDate()).getDays();
        contract.setStartDate(contract.getEndDate());
        contract.setEndDate(contract.getEndDate().plusDays(durationDays));
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setUpdatedAt(Instant.now());

        contract = contractRepository.save(contract);
        log.info("Contract {} renewed until {}", contract.getContractNumber(), contract.getEndDate());
        return contract;
    }

    /**
     * Get contracts by party.
     */
    @Transactional(readOnly = true)
    public List<Contract> getContractsByParty(Long userId) {
        return contractRepository.findByPartyId(userId);
    }

    /**
     * Get contracts expiring within N days.
     */
    @Transactional(readOnly = true)
    public List<Contract> getExpiringContracts(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return contractRepository.findExpiringWithin(deadline);
    }

    /**
     * Process automatic renewals — daily scheduled task.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Africa/Cairo")
    public void processAutoRenewals() {
        LocalDate deadline = LocalDate.now().plusDays(7);
        List<Contract> autoRenewable = contractRepository.findAutoRenewableExpiring(deadline);

        for (Contract contract : autoRenewable) {
            try {
                renewContract(contract.getId());
                log.info("Auto-renewed contract {}", contract.getContractNumber());
            } catch (Exception ex) {
                log.error("Failed to auto-renew contract {}: {}", contract.getContractNumber(), ex.getMessage());
            }
        }

        log.info("Auto-renewal processed: {} contracts", autoRenewable.size());
    }

    /**
     * Send expiry reminder notifications — daily scheduled task.
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Africa/Cairo")
    public void sendExpiryReminders() {
        int[] remindDays = {30, 14, 7};
        for (int days : remindDays) {
            List<Contract> expiring = getExpiringContracts(days);
            for (Contract contract : expiring) {
                log.info("Expiry reminder: Contract {} expires in ~{} days ({})",
                        contract.getContractNumber(), days, contract.getEndDate());
            }
        }
    }

    /**
     * Get all contracts (admin).
     */
    @Transactional(readOnly = true)
    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    /**
     * Find contract by ID or throw ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public Contract findById(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
    }

    /**
     * Update a draft contract.
     */
    public Contract updateDraft(Long contractId, LocalDate startDate, LocalDate endDate,
                                 boolean autoRenew, int renewalNoticeDays,
                                 String termsDocument, String notes) {
        Contract contract = findById(contractId);
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessRuleException("يمكن تعديل العقود المسودة فقط");
        }

        if (startDate != null) contract.setStartDate(startDate);
        if (endDate != null) contract.setEndDate(endDate);
        contract.setAutoRenew(autoRenew);
        contract.setRenewalNoticeDays(renewalNoticeDays);
        if (termsDocument != null) contract.setTermsDocument(termsDocument);
        if (notes != null) contract.setNotes(notes);
        contract.setUpdatedAt(Instant.now());

        return contractRepository.save(contract);
    }
}
