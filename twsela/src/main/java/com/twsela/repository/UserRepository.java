package com.twsela.repository;

import com.twsela.domain.User;
import com.twsela.domain.Role;
import com.twsela.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    
    // Optimized query to load user with role and status for authentication
    @Query("SELECT u FROM User u JOIN FETCH u.role JOIN FETCH u.status WHERE u.phone = :phone AND u.isDeleted = false")
    Optional<User> findByPhoneWithRoleAndStatus(@Param("phone") String phone);
    
    boolean existsByPhone(String phone);
    
    // Paginated query excluding OWNER role (for ADMIN use)
    @Query("SELECT u FROM User u JOIN u.role r WHERE r.name <> 'OWNER' AND u.isDeleted = false")
    Page<User> findAllExcludingOwners(Pageable pageable);
    
    // Paginated query for all non-deleted users
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    Page<User> findAllNonDeleted(Pageable pageable);
    
    // Optimized queries with JOIN FETCH to avoid N+1 problems
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u JOIN FETCH u.role JOIN FETCH u.status WHERE u.role.name = :roleName AND u.isDeleted = false")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);
    
    @Query("SELECT u FROM User u JOIN FETCH u.role JOIN FETCH u.status WHERE u.role.name = :roleName AND u.status.name = 'ACTIVE' AND u.isDeleted = false")
    List<User> findActiveUsersByRole(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u JOIN FETCH u.role JOIN FETCH u.status WHERE u.isDeleted = false")
    List<User> findNonDeletedUsers();
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.status s WHERE u.isDeleted = false AND s.name = 'ACTIVE'")
    long countActiveUsers();
    
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.role != :role")
    void deleteByRoleNot(@Param("role") Role role);
    
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.isDeleted = false")
    List<User> findByStatusAndNotDeleted(@Param("status") UserStatus status);
    
    List<User> findByRoleAndStatus(Role role, UserStatus status);
}