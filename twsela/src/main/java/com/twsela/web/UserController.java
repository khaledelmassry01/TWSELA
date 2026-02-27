package com.twsela.web;

import com.twsela.domain.User;
import com.twsela.domain.Role;
import com.twsela.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Users", description = "إدارة المستخدمين والصلاحيات")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final com.twsela.repository.UserRepository userRepository;
    private final com.twsela.repository.CourierLocationHistoryRepository courierLocationHistoryRepository;

    public UserController(UserService userService, 
                         com.twsela.repository.UserRepository userRepository,
                         com.twsela.repository.CourierLocationHistoryRepository courierLocationHistoryRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.courierLocationHistoryRepository = courierLocationHistoryRepository;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
            List<User> users = userService.listAll();
            
            response.put("success", true);
            response.put("data", users);
            response.put("message", "Users retrieved successfully");
            response.put("count", users.size());
            
            return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
            
            Role role = userService.getRoleByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + request.getRole()));
            
            User user = userService.createUser(
                request.getName(),
                request.getPhone(),
                request.getPassword(),
                role
            );
            
            
            response.put("success", true);
            response.put("data", user);
            response.put("message", "User created successfully");
            
            return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
            
            User user = userService.updateUser(
                id,
                request.getName(),
                request.getPhone(),
                request.getActive(),
                request.getPassword()
            );
            
            
            response.put("success", true);
            response.put("data", user);
            response.put("message", "User updated successfully");
            
            return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
            
            userService.deleteUser(id);
            
            
            response.put("success", true);
            response.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(response);
    }

    @PutMapping("/users/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        com.twsela.domain.User currentUser = (com.twsela.domain.User) authentication.getPrincipal();
        
        String name = request.get("name");
        String phone = request.get("phone");
        
        com.twsela.domain.User updated = userService.updateUser(
            currentUser.getId(), name, phone, null, null);
        
        response.put("success", true);
        response.put("data", updated);
        response.put("message", "تم تحديث الملف الشخصي بنجاح");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/couriers/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCourier(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.User courier = userRepository.findById(id).orElse(null);
        if (courier == null || !"COURIER".equals(courier.getRole().getName())) {
            response.put("success", false);
            response.put("message", "Courier not found");
            return ResponseEntity.status(404).body(response);
        }
        response.put("success", true);
        response.put("data", courier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/couriers")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createCourier(@Valid @RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.Role role = userService.getRoleByName("COURIER").orElseThrow();
        com.twsela.domain.User user = userService.createUser(request.getName(), request.getPhone(), request.getPassword(), role);
        response.put("success", true);
        response.put("data", user);
        response.put("message", "تم إنشاء المندوب بنجاح");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/couriers/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCourier(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.User courier = userRepository.findById(id).orElse(null);
        if (courier == null || !"COURIER".equals(courier.getRole().getName())) {
            response.put("success", false);
            response.put("message", "Courier not found");
            return ResponseEntity.status(404).body(response);
        }
        com.twsela.domain.User updated = userService.updateUser(id, request.getName(), request.getPhone(), request.getActive(), request.getPassword());
        response.put("success", true);
        response.put("data", updated);
        response.put("message", "تم تحديث المندوب بنجاح");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/couriers/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCourier(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.User courier = userRepository.findById(id).orElse(null);
        if (courier == null || !"COURIER".equals(courier.getRole().getName())) {
            response.put("success", false);
            response.put("message", "Courier not found");
            return ResponseEntity.status(404).body(response);
        }
        userService.deleteUser(id);
        response.put("success", true);
        response.put("message", "تم حذف المندوب بنجاح");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/couriers/{id}/location")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'COURIER')")
    public ResponseEntity<Map<String, Object>> getCourierLocation(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        var locations = courierLocationHistoryRepository.findByCourierIdOrderByTimestampDesc(id);
        if (locations.isEmpty()) {
            response.put("success", true);
            response.put("data", null);
            response.put("message", "No location data available");
            return ResponseEntity.ok(response);
        }
        response.put("success", true);
        response.put("data", locations.get(0));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/couriers/{id}/location")
    @PreAuthorize("hasAnyRole('COURIER')")
    public ResponseEntity<Map<String, Object>> updateCourierLocation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.User courier = userRepository.findById(id).orElse(null);
        if (courier == null) {
            response.put("success", false);
            response.put("message", "Courier not found");
            return ResponseEntity.status(404).body(response);
        }
        java.math.BigDecimal latitude = new java.math.BigDecimal(request.get("latitude").toString());
        java.math.BigDecimal longitude = new java.math.BigDecimal(request.get("longitude").toString());
        var location = new com.twsela.domain.CourierLocationHistory(courier, latitude, longitude);
        courierLocationHistoryRepository.save(location);
        response.put("success", true);
        response.put("data", location);
        response.put("message", "تم تحديث الموقع بنجاح");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/merchants/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getMerchant(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.User merchant = userRepository.findById(id).orElse(null);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole().getName())) {
            response.put("success", false);
            response.put("message", "Merchant not found");
            return ResponseEntity.status(404).body(response);
        }
        response.put("success", true);
        response.put("data", merchant);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/merchants")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createMerchant(@Valid @RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.Role role = userService.getRoleByName("MERCHANT").orElseThrow();
        com.twsela.domain.User user = userService.createUser(request.getName(), request.getPhone(), request.getPassword(), role);
        response.put("success", true);
        response.put("data", user);
        response.put("message", "تم إنشاء التاجر بنجاح");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/merchants/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateMerchant(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.User merchant = userRepository.findById(id).orElse(null);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole().getName())) {
            response.put("success", false);
            response.put("message", "Merchant not found");
            return ResponseEntity.status(404).body(response);
        }
        com.twsela.domain.User updated = userService.updateUser(id, request.getName(), request.getPhone(), request.getActive(), request.getPassword());
        response.put("success", true);
        response.put("data", updated);
        response.put("message", "تم تحديث التاجر بنجاح");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getEmployee(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.User employee = userRepository.findById(id).orElse(null);
        if (employee == null) {
            response.put("success", false);
            response.put("message", "Employee not found");
            return ResponseEntity.status(404).body(response);
        }
        response.put("success", true);
        response.put("data", employee);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateEmployee(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        com.twsela.domain.User employee = userRepository.findById(id).orElse(null);
        if (employee == null) {
            response.put("success", false);
            response.put("message", "Employee not found");
            return ResponseEntity.status(404).body(response);
        }
        com.twsela.domain.User updated = userService.updateUser(id, request.getName(), request.getPhone(), request.getActive(), request.getPassword());
        response.put("success", true);
        response.put("data", updated);
        response.put("message", "تم تحديث الموظف بنجاح");
        return ResponseEntity.ok(response);
    }

    // DTOs for request/response
    public static class CreateUserRequest {
        @NotBlank(message = "الاسم مطلوب")
        private String name;
        @NotBlank(message = "رقم الهاتف مطلوب")
        private String phone;
        @NotBlank(message = "كلمة المرور مطلوبة")
        private String password;
        @NotBlank(message = "الدور مطلوب")
        private String role;
        private Boolean active;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class UpdateUserRequest {
        private String name;
        private String phone;
        private Boolean active;
        private String password;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @GetMapping("/couriers")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCouriers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> response = new HashMap<>();
            Page<User> courierPage = userRepository.findByRoleName("COURIER", PageRequest.of(page, limit));
            
            response.put("success", true);
            response.put("data", courierPage.getContent());
            response.put("message", "Couriers retrieved successfully");
            response.put("count", courierPage.getTotalElements());
            response.put("page", page);
            response.put("limit", limit);
            response.put("totalPages", courierPage.getTotalPages());
            
            return ResponseEntity.ok(response);
    }

    @GetMapping("/merchants")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> response = new HashMap<>();
            Page<User> merchantPage = userRepository.findByRoleName("MERCHANT", PageRequest.of(page, limit));
            
            response.put("success", true);
            response.put("data", merchantPage.getContent());
            response.put("message", "Merchants retrieved successfully");
            response.put("count", merchantPage.getTotalElements());
            response.put("page", page);
            response.put("limit", limit);
            response.put("totalPages", merchantPage.getTotalPages());
            
            return ResponseEntity.ok(response);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
            Page<User> employeePage = userRepository.findAllNonDeleted(PageRequest.of(page, size));
            
            response.put("success", true);
            response.put("data", employeePage.getContent());
            response.put("message", "Employees retrieved successfully");
            response.put("count", employeePage.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", employeePage.getTotalPages());
            
            return ResponseEntity.ok(response);
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createEmployee(@Valid @RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
            // Validate required fields
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "الاسم مطلوب");
                return ResponseEntity.badRequest().body(response);
            }
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "رقم الهاتف مطلوب");
                return ResponseEntity.badRequest().body(response);
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "كلمة المرور مطلوبة");
                return ResponseEntity.badRequest().body(response);
            }
            if (request.getRole() == null || request.getRole().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "الدور مطلوب");
                return ResponseEntity.badRequest().body(response);
            }

            // Get role
            Role role = userService.getRoleByName(request.getRole()).orElse(null);
            if (role == null) {
                response.put("success", false);
                response.put("message", "الدور غير صحيح");
                return ResponseEntity.badRequest().body(response);
            }

            // Create user
            User user = userService.createUser(
                request.getName().trim(),
                request.getPhone().trim(),
                request.getPassword(),
                role
            );

            // Set active status if provided
            if (request.getActive() != null) {
                user = userService.updateUser(user.getId(), null, null, request.getActive(), null);
            }

            response.put("success", true);
            response.put("message", "تم إنشاء الموظف بنجاح");
            response.put("data", user);
            
            return ResponseEntity.ok(response);
    }
}