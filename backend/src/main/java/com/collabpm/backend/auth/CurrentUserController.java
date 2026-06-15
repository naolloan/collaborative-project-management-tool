package com.collabpm.backend.auth;

import com.collabpm.backend.auth.dto.CurrentUserResponse;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.User;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CurrentUserController {

    private final CurrentUserService currentUserService;

    public CurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        User currentUser = currentUserService.getOrCreateCurrentUser(authentication);
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring("ROLE_".length()))
            .sorted()
            .toList();

        return new CurrentUserResponse(
            currentUser.getId(),
            currentUser.getSystemRole().name(),
            jwt.getClaimAsString("preferred_username"),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString("name"),
            roles);
    }
}
