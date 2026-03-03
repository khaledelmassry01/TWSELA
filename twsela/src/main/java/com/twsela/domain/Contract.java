package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Contract entity — formal agreement between Twsela and a merchant/courier.
 */
@Entity
@Table(name = "contracts")
public class Contract {

    public enum ContractType {
        MERCHANT_AGREEMENT, COURIER_AGREEMENT, PARTNER_AGREEMENT
    }

    public enum ContractStatus {
        DRAFT, PENDING_SIGNATURE, ACTIVE, EXPIRING_SOON, EXPIRED, TERMINATED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number", nullable = false, unique = true)
    private String contractNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private User party;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status;

    @Column(name = "auto_renew")
    private boolean autoRenew;

    @Column(name = "renewal_notice_days")
    private int renewalNoticeDays = 30;

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "signature_otp")
    private String signatureOtp;

    @Column(name = "terms_document", columnDefinition = "TEXT")
    private String termsDocument;

    @Column(name = "notes")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    public Contract() {}

    public Contract(String contractNumber, ContractType contractType, User party,
                    LocalDate startDate, LocalDate endDate) {
        this.contractNumber = contractNumber;
        this.contractType = contractType;
        this.party = party;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ContractStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public ContractType getContractType() { return contractType; }
    public void setContractType(ContractType contractType) { this.contractType = contractType; }

    public User getParty() { return party; }
    public void setParty(User party) { this.party = party; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }

    public boolean isAutoRenew() { return autoRenew; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }

    public int getRenewalNoticeDays() { return renewalNoticeDays; }
    public void setRenewalNoticeDays(int renewalNoticeDays) { this.renewalNoticeDays = renewalNoticeDays; }

    public Instant getSignedAt() { return signedAt; }
    public void setSignedAt(Instant signedAt) { this.signedAt = signedAt; }

    public String getSignatureOtp() { return signatureOtp; }
    public void setSignatureOtp(String signatureOtp) { this.signatureOtp = signatureOtp; }

    public String getTermsDocument() { return termsDocument; }
    public void setTermsDocument(String termsDocument) { this.termsDocument = termsDocument; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
