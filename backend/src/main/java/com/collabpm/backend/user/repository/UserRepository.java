package com.collabpm.backend.user.repository;

import com.collabpm.backend.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloakUserId(String keycloakUserId);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
}
