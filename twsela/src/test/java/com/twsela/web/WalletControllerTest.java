package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.domain.Wallet.WalletType;
import com.twsela.domain.WalletTransaction.TransactionReason;
import com.twsela.domain.WalletTransaction.TransactionType;
import com.twsela.security.JwtService;
import com.twsela.service.WalletService;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = WalletController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000",
        "spring.profiles.active=test"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private WalletService walletService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    private User merchantUser;
    private Wallet merchantWallet;

    @BeforeEach
    void setUp() {
        merchantUser = new User();
        merchantUser.setId(1L);
        merchantUser.setName("Test Merchant");

        merchantWallet = new Wallet(merchantUser, WalletType.MERCHANT);
        merchantWallet.setId(10L);
        merchantWallet.setBalance(new BigDecimal("1000.00"));
        merchantWallet.setCurrency("EGP");
        merchantWallet.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("GET /api/wallet — الحصول على محفظتي")
    void getMyWallet_success() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(walletService.getWalletByUserId(1L)).thenReturn(merchantWallet);

        mockMvc.perform(get("/api/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(1000.00))
                .andExpect(jsonPath("$.data.currency").value("EGP"));
    }

    @Test
    @DisplayName("GET /api/wallet — محفظة غير موجودة")
    void getMyWallet_notFound() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(999L);
        when(walletService.getWalletByUserId(999L)).thenThrow(new ResourceNotFoundException("Wallet", "userId", 999L));

        mockMvc.perform(get("/api/wallet"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/wallet/balance — رصيد المحفظة")
    void getBalance_success() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(walletService.getBalance(1L)).thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/api/wallet/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1000.00));
    }

    @Test
    @DisplayName("GET /api/wallet/transactions — سجل المعاملات")
    void getTransactions_success() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(walletService.getWalletByUserId(1L)).thenReturn(merchantWallet);

        WalletTransaction tx = new WalletTransaction(merchantWallet, TransactionType.CREDIT,
                new BigDecimal("200.00"), TransactionReason.COD_COLLECTED, 1L, "COD test");
        tx.setId(100L);
        tx.setBalanceBefore(new BigDecimal("800.00"));
        tx.setBalanceAfter(new BigDecimal("1000.00"));
        tx.setCreatedAt(Instant.now());

        when(walletService.getTransactions(eq(10L), any())).thenReturn(new PageImpl<>(List.of(tx)));

        mockMvc.perform(get("/api/wallet/transactions").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].amount").value(200.00))
                .andExpect(jsonPath("$.data[0].type").value("CREDIT"));
    }

    @Test
    @DisplayName("POST /api/wallet/withdraw — سحب مبلغ")
    void requestWithdraw_success() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(walletService.getWalletByUserId(1L)).thenReturn(merchantWallet);

        WalletTransaction tx = new WalletTransaction(merchantWallet, TransactionType.DEBIT,
                new BigDecimal("100.00"), TransactionReason.WITHDRAWAL, null, "طلب سحب");
        tx.setId(200L);
        tx.setBalanceBefore(new BigDecimal("1000.00"));
        tx.setBalanceAfter(new BigDecimal("900.00"));
        tx.setCreatedAt(Instant.now());

        when(walletService.debit(eq(10L), any(BigDecimal.class), eq(TransactionReason.WITHDRAWAL), isNull(), anyString()))
                .thenReturn(tx);

        mockMvc.perform(post("/api/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", 100))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(100.00));
    }

    @Test
    @DisplayName("POST /api/wallet/withdraw — رصيد غير كافٍ")
    void requestWithdraw_insufficient() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(walletService.getWalletByUserId(1L)).thenReturn(merchantWallet);
        when(walletService.debit(eq(10L), any(), any(), any(), any()))
                .thenThrow(new BusinessRuleException("رصيد غير كافٍ"));

        mockMvc.perform(post("/api/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", 9999))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/wallet/admin/all — كل المحافظ")
    void getAllWallets_success() throws Exception {
        when(walletService.getAllWallets()).thenReturn(List.of(merchantWallet));

        mockMvc.perform(get("/api/wallet/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
