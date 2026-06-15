package com.collabpm.backend.sprint;

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
import com.collabpm.backend.sprint.dto.CreateSprintRequest;
import com.collabpm.backend.sprint.dto.SprintResponse;
import com.collabpm.backend.sprint.dto.UpdateSprintRequest;
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

@WebMvcTest(SprintController.class)
@Import(SecurityConfig.class)
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SprintService sprintService;

    @Test
    void listsProjectSprintsForAuthenticatedUsers() throws Exception {
        given(sprintService.listProjectSprints(eq(1L), any(Authentication.class))).willReturn(List.of(
            new SprintResponse(
                4L,
                1L,
                "Sprint 1",
                "Stand up the initial workflow surface",
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 29),
                "ACTIVE",
                8,
                3
            )));

        mockMvc.perform(get("/api/projects/1/sprints").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Sprint 1"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void createsSprintsForAuthenticatedUsers() throws Exception {
        CreateSprintRequest request = new CreateSprintRequest(
            "Sprint 2",
            "Move project execution into sprint planning",
            LocalDate.of(2026, 6, 30),
            LocalDate.of(2026, 7, 14),
            "PLANNED"
        );
        given(sprintService.createSprint(eq(1L), eq(request), any(Authentication.class))).willReturn(
            new SprintResponse(
                5L,
                1L,
                request.name(),
                request.goal(),
                request.startDate(),
                request.endDate(),
                request.status(),
                0,
                0
            ));

        mockMvc.perform(post("/api/projects/1/sprints")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Sprint 2",
                      "goal": "Move project execution into sprint planning",
                      "startDate": "2026-06-30",
                      "endDate": "2026-07-14",
                      "status": "PLANNED"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.name").value("Sprint 2"))
            .andExpect(jsonPath("$.status").value("PLANNED"));
    }

    @Test
    void updatesSprintsForAuthenticatedUsers() throws Exception {
        UpdateSprintRequest request = new UpdateSprintRequest(
            "Sprint 2",
            "Close the sprint planning loop",
            LocalDate.of(2026, 6, 30),
            LocalDate.of(2026, 7, 16),
            "ACTIVE"
        );
        given(sprintService.updateSprint(eq(5L), eq(request), any(Authentication.class))).willReturn(
            new SprintResponse(
                5L,
                1L,
                request.name(),
                request.goal(),
                request.startDate(),
                request.endDate(),
                request.status(),
                6,
                1
            ));

        mockMvc.perform(patch("/api/sprints/5")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Sprint 2",
                      "goal": "Close the sprint planning loop",
                      "startDate": "2026-06-30",
                      "endDate": "2026-07-16",
                      "status": "ACTIVE"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.totalTaskCount").value(6));
    }
}
