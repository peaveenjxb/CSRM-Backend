package com.csrm.repository;

import com.csrm.dto.Enums;
import com.csrm.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(Enums.Role role);
    long countByStatus(Enums.UserStatus status);
}
