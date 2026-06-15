package com.collabpm.backend.project;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.config.SecurityConfig;
import com.collabpm.backend.project.dto.CreateProjectTeamRequest;
import com.collabpm.backend.project.dto.ProjectTeamMemberResponse;
import com.collabpm.backend.project.dto.ProjectTeamResponse;
import com.collabpm.backend.project.dto.UpdateProjectTeamRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectTeamController.class)
@Import(SecurityConfig.class)
class ProjectTeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectTeamService projectTeamService;

    @Test
    void listsProjectTeamsForAuthenticatedUsers() throws Exception {
        given(projectTeamService.listProjectTeams(eq(1L), any(Authentication.class))).willReturn(List.of(new ProjectTeamResponse(
            9L,
            1L,
            "Payments Delivery",
            "Coordinates release planning and delivery",
            List.of(
                new ProjectTeamMemberResponse(21L, "Manager One", "manager@example.com", "MANAGER"),
                new ProjectTeamMemberResponse(22L, "Analyst Two", "analyst@example.com", "MEMBER")
            ),
            2,
            1
        )));

        mockMvc.perform(get("/api/projects/1/teams").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Payments Delivery"))
            .andExpect(jsonPath("$[0].memberCount").value(2))
            .andExpect(jsonPath("$[0].managerCount").value(1));
    }

    @Test
    void createsProjectTeamsForAuthenticatedUsers() throws Exception {
        CreateProjectTeamRequest request = new CreateProjectTeamRequest(
            "Payments Delivery",
            "Coordinates release planning and delivery",
            List.of(21L, 22L)
        );
        given(projectTeamService.createProjectTeam(eq(1L), eq(request), any(Authentication.class))).willReturn(new ProjectTeamResponse(
            9L,
            1L,
            request.name(),
            request.description(),
            List.of(
                new ProjectTeamMemberResponse(21L, "Manager One", "manager@example.com", "MANAGER"),
                new ProjectTeamMemberResponse(22L, "Analyst Two", "analyst@example.com", "MEMBER")
            ),
            2,
            1
        ));

        mockMvc.perform(post("/api/projects/1/teams")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Payments Delivery",
                      "description": "Coordinates release planning and delivery",
                      "memberUserIds": [21, 22]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(9))
            .andExpect(jsonPath("$.members", hasSize(2)))
            .andExpect(jsonPath("$.managerCount").value(1));
    }

    @Test
    void updatesProjectTeamsForAuthenticatedUsers() throws Exception {
        UpdateProjectTeamRequest request = new UpdateProjectTeamRequest(
            "Payments Delivery Core",
            "Updated delivery scope",
            List.of(21L)
        );
        given(projectTeamService.updateProjectTeam(eq(1L), eq(9L), eq(request), any(Authentication.class))).willReturn(new ProjectTeamResponse(
            9L,
            1L,
            request.name(),
            request.description(),
            List.of(new ProjectTeamMemberResponse(21L, "Manager One", "manager@example.com", "MANAGER")),
            1,
            1
        ));

        mockMvc.perform(patch("/api/projects/1/teams/9")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Payments Delivery Core",
                      "description": "Updated delivery scope",
                      "memberUserIds": [21]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Payments Delivery Core"))
            .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    void rejectsAnonymousProjectTeamRequests() throws Exception {
        mockMvc.perform(get("/api/projects/1/teams"))
            .andExpect(status().isUnauthorized());
    }
}
