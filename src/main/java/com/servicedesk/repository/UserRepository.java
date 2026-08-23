package com.servicedesk.repository;

import com.servicedesk.domain.User;
import com.servicedesk.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndActiveTrue(Role role);
    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.role IN ('SUPPORT_ENGINEER', 'ADMIN', 'DEVELOPER') AND u.active = true")
    List<User> findAssignableUsers();
}
