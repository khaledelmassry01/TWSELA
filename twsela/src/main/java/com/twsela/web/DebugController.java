package com.twsela.web;

import com.twsela.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Debug Controller - FOR DEVELOPMENT ONLY
 * يجب حذف هذا الـ Controller في الإنتاج
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DebugController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Generate BCrypt hash for a password
     * GET /api/debug/generate-hash?password=YOUR_PASSWORD
     */
    @GetMapping("/generate-hash")
    public ResponseEntity<Map<String, Object>> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("password", password);
        response.put("hash", hash);
        response.put("message", "Hash generated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test if a password matches a hash
     * POST /api/debug/test-password
     * Body: { "password": "150620KkZz@#$", "hash": "$2a$10$..." }
     */
    @PostMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = request.get("hash");
        
        boolean matches = passwordEncoder.matches(password, hash);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("password", password);
        response.put("hash", hash);
        response.put("matches", matches);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reset all test user passwords
     * POST /api/debug/reset-test-passwords
     */
    @PostMapping("/reset-test-passwords")
    public ResponseEntity<Map<String, Object>> resetTestPasswords() {
        String newPassword = "150620KkZz@#$";
        String newHash = passwordEncoder.encode(newPassword);
        
        String[] phones = {
            "01023782584",  // OWNER
            "01023782585",  // MERCHANT  
            "01023782586",  // COURIER
            "01023782588",  // WAREHOUSE
            "01126538767"   // ADMIN
        };
        
        int updated = 0;
        for (String phone : phones) {
            var userOpt = userRepository.findByPhone(phone);
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                user.setPassword(newHash);
                userRepository.save(user);
                updated++;
                System.out.println("✅ Updated password for user: " + phone + " (" + user.getName() + ")");
            } else {
                System.out.println("⚠️  User not found: " + phone);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Test passwords updated");
        response.put("newPassword", newPassword);
        response.put("newHash", newHash);
        response.put("usersUpdated", updated);
        response.put("totalUsers", phones.length);
        
        return ResponseEntity.ok(response);
    }
}
