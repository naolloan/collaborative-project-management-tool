package com.collabpm.backend.auth;

import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.config.SecurityConfig;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CurrentUserController.class)
@Import(SecurityConfig.class)
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsCurrentUserFromJwt() throws Exception {
        mockMvc.perform(get("/api/me")
                .with(jwt()
                    .jwt(token -> token
                        .subject("keycloak-user-id")
                        .claim("preferred_username", "manager@example.com")
                        .claim("email", "manager@example.com")
                        .claim("name", "Project Manager")
                        .claim("realm_access", Map.of("roles", List.of("PROJECT_MANAGER"))))
                    .authorities(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("keycloak-user-id"))
            .andExpect(jsonPath("$.username").value("manager@example.com"))
            .andExpect(jsonPath("$.email").value("manager@example.com"))
            .andExpect(jsonPath("$.fullName").value("Project Manager"))
            .andExpect(jsonPath("$.roles", contains("PROJECT_MANAGER")));
    }

    @Test
    void rejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized());
    }
}
