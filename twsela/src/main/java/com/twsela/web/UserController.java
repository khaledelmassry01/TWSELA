package com.twsela.web;

import com.twsela.domain.User;
import com.twsela.domain.Role;
import com.twsela.repository.UserRepository;
import com.twsela.repository.CourierLocationHistoryRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.UserService;
import com.twsela.web.dto.ApiPageResponse;
import com.twsela.web.dto.CreateUserRequest;
import com.twsela.web.dto.DtoMapper;
import com.twsela.web.dto.UpdateUserRequest;
import com.twsela.web.dto.UserResponseDTO;
import com.twsela.web.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Users", description = "إدارة المستخدمين والصلاحيات")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserRepository userRepository;
    private final CourierLocationHistoryRepository courierLocationHistoryRepository;
    private final AuthenticationHelper authHelper;

    public UserController(UserService userService,
                         UserRepository userRepository,
                         CourierLocationHistoryRepository courierLocationHistoryRepository,
                         AuthenticationHelper authHelper) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.courierLocationHistoryRepository = courierLocationHistoryRepository;
        this.authHelper = authHelper;
    }

    @Operation(summary = "الحصول على جميع المستخدمين")
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.listAll().stream()
                .map(DtoMapper::toUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(users, "Users retrieved successfully"));
    }

    @Operation(summary = "إنشاء مستخدم جديد")
    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        Role role = userService.getRoleByName(request.getRole())
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRole()));
        User user = userService.createUser(request.getName(), request.getPhone(), request.getPassword(), role);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(user), "User created successfully"));
    }

    @Operation(summary = "تحديث مستخدم")
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request.getName(), request.getPhone(), request.getActive(), request.getPassword());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(user), "User updated successfully"));
    }

    @Operation(summary = "حذف مستخدم")
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok("User deleted successfully"));
    }

    @Operation(summary = "تحديث الملف الشخصي")
    @PutMapping("/users/profile")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> updateProfile(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        User currentUser = authHelper.getCurrentUser(authentication);
        String name = request.get("name");
        String phone = request.get("phone");
        User updated = userService.updateUser(currentUser.getId(), name, phone, null, null);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(updated), "تم تحديث الملف الشخصي بنجاح"));
    }

    @Operation(summary = "الحصول على مندوب بالمعرف")
    @GetMapping("/couriers/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> getCourier(@PathVariable Long id) {
        User courier = userRepository.findById(id)
                .filter(u -> "COURIER".equals(u.getRole().getName()))
                .orElseThrow(() -> new ResourceNotFoundException("Courier", "id", id));
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(courier)));
    }

    @Operation(summary = "إنشاء مندوب جديد")
    @PostMapping("/couriers")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> createCourier(@Valid @RequestBody CreateUserRequest request) {
        Role role = userService.getRoleByName("COURIER").orElseThrow();
        User user = userService.createUser(request.getName(), request.getPhone(), request.getPassword(), role);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(user), "تم إنشاء المندوب بنجاح"));
    }

    @Operation(summary = "تحديث بيانات مندوب")
    @PutMapping("/couriers/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> updateCourier(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        userRepository.findById(id)
                .filter(u -> "COURIER".equals(u.getRole().getName()))
                .orElseThrow(() -> new ResourceNotFoundException("Courier", "id", id));
        User updated = userService.updateUser(id, request.getName(), request.getPhone(), request.getActive(), request.getPassword());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(updated), "تم تحديث المندوب بنجاح"));
    }

    @Operation(summary = "حذف مندوب")
    @DeleteMapping("/couriers/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Void>> deleteCourier(@PathVariable Long id) {
        userRepository.findById(id)
                .filter(u -> "COURIER".equals(u.getRole().getName()))
                .orElseThrow(() -> new ResourceNotFoundException("Courier", "id", id));
        userService.deleteUser(id);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok("تم حذف المندوب بنجاح"));
    }

    @Operation(summary = "الحصول على موقع المندوب")
    @GetMapping("/couriers/{id}/location")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'COURIER')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Object>> getCourierLocation(@PathVariable Long id) {
        var locations = courierLocationHistoryRepository.findByCourierIdOrderByTimestampDesc(id);
        if (locations.isEmpty()) {
            return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(null, "No location data available"));
        }
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(locations.get(0)));
    }

    @Operation(summary = "تحديث موقع المندوب")
    @PutMapping("/couriers/{id}/location")
    @PreAuthorize("hasAnyRole('COURIER')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Object>> updateCourierLocation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        User courier = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Courier", "id", id));
        java.math.BigDecimal latitude = new java.math.BigDecimal(request.get("latitude").toString());
        java.math.BigDecimal longitude = new java.math.BigDecimal(request.get("longitude").toString());
        var location = new com.twsela.domain.CourierLocationHistory(courier, latitude, longitude);
        courierLocationHistoryRepository.save(location);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(location, "تم تحديث الموقع بنجاح"));
    }

    @Operation(summary = "الحصول على تاجر بالمعرف")
    @GetMapping("/merchants/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> getMerchant(@PathVariable Long id) {
        User merchant = userRepository.findById(id)
                .filter(u -> "MERCHANT".equals(u.getRole().getName()))
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", id));
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(merchant)));
    }

    @Operation(summary = "إنشاء تاجر جديد")
    @PostMapping("/merchants")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> createMerchant(@Valid @RequestBody CreateUserRequest request) {
        Role role = userService.getRoleByName("MERCHANT").orElseThrow();
        User user = userService.createUser(request.getName(), request.getPhone(), request.getPassword(), role);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(user), "تم إنشاء التاجر بنجاح"));
    }

    @Operation(summary = "تحديث بيانات تاجر")
    @PutMapping("/merchants/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> updateMerchant(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        userRepository.findById(id)
                .filter(u -> "MERCHANT".equals(u.getRole().getName()))
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", id));
        User updated = userService.updateUser(id, request.getName(), request.getPhone(), request.getActive(), request.getPassword());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(updated), "تم تحديث التاجر بنجاح"));
    }

    @Operation(summary = "الحصول على موظف بالمعرف")
    @GetMapping("/employees/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> getEmployee(@PathVariable Long id) {
        User employee = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(employee)));
    }

    @Operation(summary = "تحديث بيانات موظف")
    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> updateEmployee(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        User updated = userService.updateUser(id, request.getName(), request.getPhone(), request.getActive(), request.getPassword());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(updated), "تم تحديث الموظف بنجاح"));
    }

    @Operation(summary = "الحصول على قائمة المناديب")
    @GetMapping("/couriers")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiPageResponse<UserResponseDTO>> getCouriers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<User> courierPage = userRepository.findByRoleName("COURIER", PageRequest.of(page, limit));
        Page<UserResponseDTO> dtoPage = courierPage.map(DtoMapper::toUserDTO);
        return ResponseEntity.ok(ApiPageResponse.of(dtoPage, "Couriers retrieved successfully"));
    }

    @Operation(summary = "الحصول على قائمة التجار")
    @GetMapping("/merchants")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiPageResponse<UserResponseDTO>> getMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<User> merchantPage = userRepository.findByRoleName("MERCHANT", PageRequest.of(page, limit));
        Page<UserResponseDTO> dtoPage = merchantPage.map(DtoMapper::toUserDTO);
        return ResponseEntity.ok(ApiPageResponse.of(dtoPage, "Merchants retrieved successfully"));
    }

    @Operation(summary = "الحصول على قائمة الموظفين")
    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiPageResponse<UserResponseDTO>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> employeePage = userRepository.findAllNonDeleted(PageRequest.of(page, size));
        Page<UserResponseDTO> dtoPage = employeePage.map(DtoMapper::toUserDTO);
        return ResponseEntity.ok(ApiPageResponse.of(dtoPage, "Employees retrieved successfully"));
    }

    @Operation(summary = "إنشاء موظف جديد")
    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> createEmployee(@Valid @RequestBody CreateUserRequest request) {
        Role role = userService.getRoleByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRole()));
        User user = userService.createUser(
            request.getName().trim(),
            request.getPhone().trim(),
            request.getPassword(),
            role
        );
        if (request.getActive() != null) {
            user = userService.updateUser(user.getId(), null, null, request.getActive(), null);
        }
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(user), "تم إنشاء الموظف بنجاح"));
    }
}