package com.twsela.service;

import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.repository.MerchantDetailsRepository;
import com.twsela.repository.RoleRepository;
import com.twsela.repository.UserRepository;
import com.twsela.repository.UserStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MerchantDetailsRepository merchantDetailsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserStatusRepository userStatusRepository;

    @InjectMocks
    private UserService userService;

    private Role merchantRole;
    private UserStatus activeStatus;

    @BeforeEach
    void setUp() {
        merchantRole = new Role("MERCHANT");
        merchantRole.setId(1L);

        activeStatus = new UserStatus();
        activeStatus.setId(1L);
        activeStatus.setName("ACTIVE");
    }

    @Test
    @DisplayName("createUser - creates user with encoded password")
    void createUser_Success() {
        when(userRepository.existsByPhone("0501234567")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$encodedPassword");
        when(userStatusRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeStatus));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User result = userService.createUser("Test User", "0501234567", "rawPassword", merchantRole);

        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getPhone()).isEqualTo("0501234567");
        assertThat(result.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(result.getRole()).isEqualTo(merchantRole);
        assertThat(result.getStatus()).isEqualTo(activeStatus);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - throws when phone already exists")
    void createUser_DuplicatePhone() {
        when(userRepository.existsByPhone("0501234567")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("Test", "0501234567", "pass", merchantRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phone already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - updates name and phone")
    void updateUser_Success() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setPhone("0501111111");
        existing.setRole(merchantRole);
        existing.setStatus(activeStatus);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByPhone("0502222222")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(1L, "New Name", "0502222222", null, null);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPhone()).isEqualTo("0502222222");
    }

    @Test
    @DisplayName("updateUser - throws when new phone already taken")
    void updateUser_DuplicatePhone() {
        User existing = new User();
        existing.setId(1L);
        existing.setPhone("0501111111");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByPhone("0502222222")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, null, "0502222222", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phone already registered");
    }

    @Test
    @DisplayName("updateUser - updates password when provided")
    void updateUser_WithPassword() {
        User existing = new User();
        existing.setId(1L);
        existing.setPhone("0501111111");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPass")).thenReturn("$2a$10$newEncoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(1L, null, null, null, "newPass");

        assertThat(result.getPassword()).isEqualTo("$2a$10$newEncoded");
    }

    @Test
    @DisplayName("listAll - returns all users")
    void listAll_ReturnsUsers() {
        User u1 = new User();
        u1.setId(1L);
        User u2 = new User();
        u2.setId(2L);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<User> result = userService.listAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getRoleByName - returns role when found")
    void getRoleByName_Found() {
        when(roleRepository.findByName("MERCHANT")).thenReturn(Optional.of(merchantRole));

        Optional<Role> result = userService.getRoleByName("MERCHANT");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("MERCHANT");
    }

    @Test
    @DisplayName("deleteUser - calls repository deleteById")
    void deleteUser_Success() {
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }
}
