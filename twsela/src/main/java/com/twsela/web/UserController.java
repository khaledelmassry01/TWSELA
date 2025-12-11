package com.twsela.web;

import com.twsela.domain.User;
import com.twsela.domain.Role;
import com.twsela.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userService.listAll();
            
            response.put("success", true);
            response.put("data", users);
            response.put("message", "Users retrieved successfully");
            response.put("count", users.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ UserController: Error retrieving users: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Failed to retrieve users: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            
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
        } catch (Exception e) {
            System.err.println("❌ UserController: Error creating user: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Failed to create user: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            
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
        } catch (Exception e) {
            System.err.println("❌ UserController: Error updating user: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Failed to update user: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            
            userService.deleteUser(id);
            
            
            response.put("success", true);
            response.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ UserController: Error deleting user: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Failed to delete user: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // DTOs for request/response
    public static class CreateUserRequest {
        private String name;
        private String phone;
        private String password;
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
        try {
            Role courierRole = userService.getRoleByName("COURIER").orElseThrow();
            List<User> couriers = userService.listByRole(courierRole);
            
            // Apply pagination
            int start = page * limit;
            int end = Math.min(start + limit, couriers.size());
            List<User> paginatedCouriers = couriers.subList(start, end);
            
            response.put("success", true);
            response.put("data", paginatedCouriers);
            response.put("message", "Couriers retrieved successfully");
            response.put("count", couriers.size());
            response.put("page", page);
            response.put("limit", limit);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ UserController: Error retrieving couriers: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Failed to retrieve couriers: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/merchants")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            Role merchantRole = userService.getRoleByName("MERCHANT").orElseThrow();
            List<User> merchants = userService.listByRole(merchantRole);
            
            // Apply pagination
            int start = page * limit;
            int end = Math.min(start + limit, merchants.size());
            List<User> paginatedMerchants = merchants.subList(start, end);
            
            response.put("success", true);
            response.put("data", paginatedMerchants);
            response.put("message", "Merchants retrieved successfully");
            response.put("count", merchants.size());
            response.put("page", page);
            response.put("limit", limit);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ UserController: Error retrieving merchants: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Failed to retrieve merchants: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get all users (employees) - in this context, all users are considered employees
            List<User> allUsers = userService.listAll();
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, allUsers.size());
            List<User> paginatedUsers = allUsers.subList(start, end);
            
            response.put("success", true);
            response.put("data", paginatedUsers);
            response.put("message", "Employees retrieved successfully");
            response.put("count", allUsers.size());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) allUsers.size() / size));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ UserController: Error retrieving employees: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Failed to retrieve employees: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
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
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("❌ UserController: Error creating employee: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "فشل في إنشاء الموظف: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}