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
import com.collabpm.backend.project.dto.CreateProjectRequest;
import com.collabpm.backend.project.dto.ProjectResponse;
import com.collabpm.backend.project.dto.UpdateProjectRequest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    void listsProjectsForAuthenticatedUsers() throws Exception {
        given(projectService.listProjects(any(Authentication.class))).willReturn(List.of(new ProjectResponse(
            1L,
            "Internship Board",
            "Planning the project management tool",
            LocalDate.of(2026, 6, 9),
            null,
            "ACTIVE")));

        mockMvc.perform(get("/api/projects").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Internship Board"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void createsProjectForAuthenticatedUsers() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
            "Internship Board",
            "Planning the project management tool",
            LocalDate.of(2026, 6, 9),
            null);
        given(projectService.createProject(eq(request), any(Authentication.class))).willReturn(new ProjectResponse(
            1L,
            request.name(),
            request.description(),
            request.startDate(),
            request.dueDate(),
            "ACTIVE"));

        mockMvc.perform(post("/api/projects")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Internship Board",
                      "description": "Planning the project management tool",
                      "startDate": "2026-06-09",
                      "dueDate": null
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Internship Board"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updatesProjectsForAuthenticatedUsers() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest(
            "Updated Internship Board",
            "Updated planning notes",
            LocalDate.of(2026, 6, 10),
            LocalDate.of(2026, 7, 10));
        given(projectService.updateProject(eq(1L), eq(request), any(Authentication.class))).willReturn(new ProjectResponse(
            1L,
            request.name(),
            request.description(),
            request.startDate(),
            request.dueDate(),
            "ACTIVE"));

        mockMvc.perform(patch("/api/projects/1")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Updated Internship Board",
                      "description": "Updated planning notes",
                      "startDate": "2026-06-10",
                      "dueDate": "2026-07-10"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Updated Internship Board"));
    }

    @Test
    void archivesProjectsForAuthenticatedUsers() throws Exception {
        given(projectService.archiveProject(eq(1L), any(Authentication.class))).willReturn(new ProjectResponse(
            1L,
            "Internship Board",
            "Planning the project management tool",
            LocalDate.of(2026, 6, 9),
            null,
            "ARCHIVED"));

        mockMvc.perform(patch("/api/projects/1/archive").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    void rejectsAnonymousProjectRequests() throws Exception {
        mockMvc.perform(get("/api/projects"))
            .andExpect(status().isUnauthorized());
    }
}
