package com.twsela.web;

import com.twsela.domain.Wallet;
import com.twsela.domain.WalletTransaction;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.WalletService;
import com.twsela.web.dto.WalletDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for wallet operations.
 */
@RestController
@RequestMapping("/api/wallet")
@Tag(name = "Wallet", description = "إدارة المحفظة المالية")
public class WalletController {

    private static final Logger log = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;
    private final AuthenticationHelper authHelper;

    public WalletController(WalletService walletService, AuthenticationHelper authHelper) {
        this.walletService = walletService;
        this.authHelper = authHelper;
    }

    /**
     * Get my wallet info.
     */
    @Operation(summary = "محفظتي", description = "الحصول على معلومات المحفظة الخاصة بي")
    @GetMapping
    public ResponseEntity<com.twsela.web.dto.ApiResponse<WalletDTO>> getMyWallet(Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        Wallet wallet = walletService.getWalletByUserId(userId);
        WalletDTO dto = toWalletDTO(wallet);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(dto));
    }

    /**
     * Get my wallet balance.
     */
    @Operation(summary = "رصيد المحفظة")
    @GetMapping("/balance")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<BigDecimal>> getBalance(Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(balance));
    }

    /**
     * Get paginated transaction history.
     */
    @Operation(summary = "سجل المعاملات", description = "سجل معاملات المحفظة (مُرقّم)")
    @GetMapping("/transactions")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<WalletDTO.TransactionDTO>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        Wallet wallet = walletService.getWalletByUserId(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransaction> txPage = walletService.getTransactions(wallet.getId(), pageable);

        List<WalletDTO.TransactionDTO> dtos = txPage.getContent().stream()
                .map(this::toTransactionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(dtos));
    }

    /**
     * Request a withdrawal.
     */
    @Operation(summary = "طلب سحب", description = "طلب سحب مبلغ من المحفظة")
    @PostMapping("/withdraw")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<WalletDTO.TransactionDTO>> requestWithdraw(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        Wallet wallet = walletService.getWalletByUserId(userId);
        WalletTransaction tx = walletService.debit(wallet.getId(), amount,
                WalletTransaction.TransactionReason.WITHDRAWAL, null, "طلب سحب");
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(toTransactionDTO(tx), "تم طلب السحب بنجاح"));
    }

    /**
     * Admin: get all wallets.
     */
    @Operation(summary = "كل المحافظ (مدير)")
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<WalletDTO>>> getAllWallets() {
        List<WalletDTO> dtos = walletService.getAllWallets().stream()
                .map(this::toWalletDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(dtos));
    }

    // ── Mapping helpers ─────────────────────────────────────────

    private WalletDTO toWalletDTO(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        dto.setId(wallet.getId());
        dto.setBalance(wallet.getBalance());
        dto.setCurrency(wallet.getCurrency());
        dto.setWalletType(wallet.getWalletType() != null ? wallet.getWalletType().name() : null);
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }

    private WalletDTO.TransactionDTO toTransactionDTO(WalletTransaction tx) {
        WalletDTO.TransactionDTO dto = new WalletDTO.TransactionDTO();
        dto.setId(tx.getId());
        dto.setType(tx.getType() != null ? tx.getType().name() : null);
        dto.setAmount(tx.getAmount());
        dto.setReason(tx.getReason() != null ? tx.getReason().name() : null);
        dto.setBalanceBefore(tx.getBalanceBefore());
        dto.setBalanceAfter(tx.getBalanceAfter());
        dto.setDescription(tx.getDescription());
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }
}
