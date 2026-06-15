package com.collabpm.backend.auth;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.config.SecurityConfig;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

@WebMvcTest(CurrentUserController.class)
@Import(SecurityConfig.class)
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrentUserService currentUserService;

    @Test
    void returnsCurrentUserFromJwt() throws Exception {
        User currentUser = new User("keycloak-user-id", "Project Manager", "manager@example.com", SystemRole.PROJECT_MANAGER);
        ReflectionTestUtils.setField(currentUser, "id", 1L);
        given(currentUserService.getOrCreateCurrentUser(any())).willReturn(currentUser);

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
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.systemRole").value("PROJECT_MANAGER"))
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
