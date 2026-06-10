package com.collabpm.backend.project;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.config.SecurityConfig;
import com.collabpm.backend.project.dto.AddProjectMemberRequest;
import com.collabpm.backend.project.dto.ProjectMemberResponse;
import com.collabpm.backend.project.model.ProjectRole;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectMemberController.class)
@Import(SecurityConfig.class)
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectMemberService projectMemberService;

    @Test
    void listsProjectMembersForAuthenticatedUsers() throws Exception {
        given(projectMemberService.listProjectMembers(eq(1L), any(Authentication.class))).willReturn(List.of(new ProjectMemberResponse(
            100L,
            20L,
            "Admin Admin",
            "admin@example.com",
            "MANAGER",
            Instant.parse("2026-06-09T14:00:00Z"))));

        mockMvc.perform(get("/api/projects/1/members").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].email").value("admin@example.com"))
            .andExpect(jsonPath("$[0].projectRole").value("MANAGER"));
    }

    @Test
    void addsProjectMembersForAuthenticatedUsers() throws Exception {
        AddProjectMemberRequest request = new AddProjectMemberRequest("member@example.com", ProjectRole.MEMBER);
        given(projectMemberService.addProjectMember(eq(1L), eq(request), any(Authentication.class))).willReturn(new ProjectMemberResponse(
            101L,
            21L,
            "Team Member",
            "member@example.com",
            "MEMBER",
            Instant.parse("2026-06-09T14:10:00Z")));

        mockMvc.perform(post("/api/projects/1/members")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "member@example.com",
                      "projectRole": "MEMBER"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(21))
            .andExpect(jsonPath("$.email").value("member@example.com"))
            .andExpect(jsonPath("$.projectRole").value("MEMBER"));
    }

    @Test
    void rejectsAnonymousMemberRequests() throws Exception {
        mockMvc.perform(get("/api/projects/1/members"))
            .andExpect(status().isUnauthorized());
    }
}
