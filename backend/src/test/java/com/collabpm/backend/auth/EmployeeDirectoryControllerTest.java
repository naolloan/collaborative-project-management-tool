package com.collabpm.backend.auth;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.config.SecurityConfig;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import com.collabpm.backend.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeDirectoryController.class)
@Import(SecurityConfig.class)
class EmployeeDirectoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    void listsEmployeesForAuthenticatedUsers() throws Exception {
        User manager = new User("user-2", "Beta Manager", "beta.manager@example.com", SystemRole.PROJECT_MANAGER);
        User admin = new User("user-1", "Alpha Admin", "alpha.admin@example.com", SystemRole.ADMINISTRATOR);
        ReflectionTestUtils.setField(manager, "id", 2L);
        ReflectionTestUtils.setField(admin, "id", 1L);
        given(userRepository.findAll()).willReturn(List.of(manager, admin));

        mockMvc.perform(get("/api/employees").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].fullName").value("Alpha Admin"))
            .andExpect(jsonPath("$[0].email").value("alpha.admin@example.com"))
            .andExpect(jsonPath("$[0].systemRole").value("ADMINISTRATOR"))
            .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void rejectsAnonymousEmployeeDirectoryRequests() throws Exception {
        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isUnauthorized());
    }
}
