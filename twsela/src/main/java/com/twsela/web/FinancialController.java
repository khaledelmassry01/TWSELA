package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Unified Financial Controller for managing payouts and financial operations
 * Replaces role-specific financial endpoints with generic ones that filter by user role
 */
@RestController
@RequestMapping("/api/financial")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT') or hasRole('COURIER')")
public class FinancialController {

    @Autowired
    private FinancialService financialService;
    
    @Autowired
    private PayoutRepository payoutRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/payouts")
    public ResponseEntity<List<Payout>> getAllPayouts(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Payout> payouts;
        
        String role = currentUser.getRole().getName();
        
        switch (role) {
            case "OWNER":
            case "ADMIN":
                // OWNER and ADMIN can see all payouts
                payouts = payoutRepository.findAll();
                break;
            case "MERCHANT":
            case "COURIER":
                // MERCHANT and COURIER can only see their own payouts
                payouts = financialService.getPayoutsForUser(currentUser.getId());
                break;
            default:
                return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(payouts);
    }

    @PostMapping("/payouts")
    public ResponseEntity<Payout> createPayout(@RequestBody CreatePayoutRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER and ADMIN can create payouts
        if (!currentUser.getRole().getName().equals("OWNER") && !currentUser.getRole().getName().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        // Verify user exists
        User user = userRepository.findById(request.userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Create payout based on type
        Payout payout;
        if (request.payoutType.equals("COURIER")) {
            payout = financialService.createCourierPayout(
                request.userId, 
                request.startDate, 
                request.endDate
            );
        } else if (request.payoutType.equals("MERCHANT")) {
            payout = financialService.createMerchantPayout(
                request.userId, 
                request.startDate, 
                request.endDate
            );
        } else {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(payout);
    }

    @GetMapping("/payouts/{payoutId}")
    public ResponseEntity<Payout> getPayoutById(@PathVariable Long payoutId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Payout payout = financialService.getPayoutById(payoutId);
        
        if (payout == null) {
            return ResponseEntity.notFound().build();
        }
        
        String role = currentUser.getRole().getName();
        
        // Check access permissions
        if ((role.equals("MERCHANT") || role.equals("COURIER")) && 
            !payout.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(payout);
    }

    @PutMapping("/payouts/{payoutId}/status")
    public ResponseEntity<Payout> updatePayoutStatus(
            @PathVariable Long payoutId,
            @RequestParam String status,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER and ADMIN can update payout status
        if (!currentUser.getRole().getName().equals("OWNER") && !currentUser.getRole().getName().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        Payout payout = financialService.updatePayoutStatus(payoutId, status);
        if (payout == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(payout);
    }

    @GetMapping("/payouts/pending")
    public ResponseEntity<List<Payout>> getPendingPayouts(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER and ADMIN can see pending payouts
        if (!currentUser.getRole().getName().equals("OWNER") && !currentUser.getRole().getName().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        List<Payout> pendingPayouts = financialService.getPendingPayouts();
        return ResponseEntity.ok(pendingPayouts);
    }

    @GetMapping("/payouts/user/{userId}")
    public ResponseEntity<List<Payout>> getPayoutsForUser(@PathVariable Long userId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        String role = currentUser.getRole().getName();
        
        // Check access permissions
        if ((role.equals("MERCHANT") || role.equals("COURIER")) && !userId.equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        List<Payout> payouts = financialService.getPayoutsForUser(userId);
        return ResponseEntity.ok(payouts);
    }

    @GetMapping("/payouts/{payoutId}/items")
    public ResponseEntity<List<PayoutItem>> getPayoutItems(@PathVariable Long payoutId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Payout payout = financialService.getPayoutById(payoutId);
        
        if (payout == null) {
            return ResponseEntity.notFound().build();
        }
        
        String role = currentUser.getRole().getName();
        
        // Check access permissions
        if ((role.equals("MERCHANT") || role.equals("COURIER")) && 
            !payout.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        List<PayoutItem> items = financialService.getPayoutItems(payoutId);
        return ResponseEntity.ok(items);
    }

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    public static class CreatePayoutRequest {
        public Long userId;
        public String payoutType; // "COURIER" or "MERCHANT"
        public LocalDate startDate;
        public LocalDate endDate;
        
        public CreatePayoutRequest() {}
        
        public CreatePayoutRequest(Long userId, String payoutType, LocalDate startDate, LocalDate endDate) {
            this.userId = userId;
            this.payoutType = payoutType;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}