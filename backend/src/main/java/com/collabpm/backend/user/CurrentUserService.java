package com.collabpm.backend.user;

import com.collabpm.backend.user.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateCurrentUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakUserId = jwt.getSubject();

        return userRepository.findByKeycloakUserId(keycloakUserId)
            .orElseGet(() -> userRepository.save(new User(
                keycloakUserId,
                displayName(jwt),
                email(jwt),
                strongestRole(authentication))));
    }

    private String displayName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name;
        }

        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isBlank()) {
            return username;
        }

        return jwt.getSubject();
    }

    private String email(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isBlank()) {
            return username;
        }

        return jwt.getSubject() + "@keycloak.local";
    }

    private SystemRole strongestRole(Authentication authentication) {
        List<SystemRole> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring("ROLE_".length()))
            .map(this::toSystemRole)
            .sorted(Comparator.comparingInt(this::roleRank))
            .toList();

        return roles.isEmpty() ? SystemRole.TEAM_MEMBER : roles.get(0);
    }

    private SystemRole toSystemRole(String role) {
        try {
            return SystemRole.valueOf(role);
        } catch (IllegalArgumentException ex) {
            return SystemRole.TEAM_MEMBER;
        }
    }

    private int roleRank(SystemRole role) {
        return switch (role) {
            case ADMINISTRATOR -> 0;
            case PROJECT_MANAGER -> 1;
            case TEAM_MEMBER -> 2;
        };
    }
}
