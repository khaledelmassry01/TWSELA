package com.twsela.service;

import com.twsela.domain.MerchantDetails;
import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.repository.MerchantDetailsRepository;
import com.twsela.repository.RoleRepository;
import com.twsela.repository.UserRepository;
import com.twsela.repository.UserStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MerchantDetailsRepository merchantDetailsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStatusRepository userStatusRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, MerchantDetailsRepository merchantDetailsRepository, PasswordEncoder passwordEncoder, UserStatusRepository userStatusRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.merchantDetailsRepository = merchantDetailsRepository;
        this.passwordEncoder = passwordEncoder;
        this.userStatusRepository = userStatusRepository;
    }

    @CacheEvict(value = {"users", "roles"}, allEntries = true)
    public User createUser(String name, String phone, String rawPassword, Role role) {
        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone already registered");
        }
        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        // Get ACTIVE status from database
        UserStatus activeStatus = userStatusRepository.findByName("ACTIVE").orElseThrow();
        user.setStatus(activeStatus);
        return userRepository.save(user);
    }

    public User createMerchantWithDetails(String name, String phone, String rawPassword, String businessName, String pickupAddress, String bankDetails) {
        Role merchantRole = roleRepository.findByName("MERCHANT").orElseThrow();
        User user = createUser(name, phone, rawPassword, merchantRole);
        MerchantDetails details = new MerchantDetails();
        details.setUser(user);
        details.setBusinessName(businessName);
        details.setPickupAddress(pickupAddress);
        details.setBankAccountDetails(bankDetails);
        merchantDetailsRepository.save(details);
        return user;
    }

    @CacheEvict(value = "users", allEntries = true)
    public User updateUser(Long id, String name, String phone, Boolean active, String rawPassword) {
        User user = userRepository.findById(id).orElseThrow();
        if (name != null) user.setName(name);
        if (phone != null && !phone.equals(user.getPhone())) {
            if (userRepository.existsByPhone(phone)) {
                throw new IllegalArgumentException("Phone already registered");
            }
            user.setPhone(phone);
        }
        if (active != null) {
            UserStatus status = active ? 
                userStatusRepository.findByName("ACTIVE").orElseThrow() : 
                userStatusRepository.findByName("INACTIVE").orElseThrow();
            user.setStatus(status);
        }
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        return userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<User> listByRole(Role role) {
        return userRepository.findAll().stream().filter(u -> u.getRole().equals(role)).toList();
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'all'")
    public List<User> listAll() {
        return userRepository.findAll();
    }
    
    @Cacheable(value = "roles", key = "#name")
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
    
    // ===== ROLE MANAGEMENT (merged from RoleService) =====
    
    @Cacheable(value = "roles", key = "'all'")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    public Role createRole(String name) {
        if (roleRepository.existsByName(name)) {
            throw new IllegalArgumentException("Role with name '" + name + "' already exists");
        }
        
        Role role = new Role(name);
        return roleRepository.save(role);
    }
    
    public Role updateRole(Long id, String name) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        
        if (!role.getName().equals(name) && roleRepository.existsByName(name)) {
            throw new IllegalArgumentException("Role with name '" + name + "' already exists");
        }
        
        role.setName(name);
        return roleRepository.save(role);
    }
    
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role not found with id: " + id);
        }
        
        roleRepository.deleteById(id);
    }
    
    public boolean roleExistsByName(String name) {
        return roleRepository.existsByName(name);
    }
}



