package com.collabpm.backend.activity;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.activity.dto.ActivityResponse;
import com.collabpm.backend.config.SecurityConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ActivityController.class)
@Import(SecurityConfig.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

    @Test
    void listsTaskActivitiesForAuthenticatedUsers() throws Exception {
        given(activityService.listTaskActivities(eq(10L), any(Authentication.class))).willReturn(List.of(new ActivityResponse(
            1L,
            1L,
            4L,
            10L,
            "TASK",
            "Build polished task board",
            "Admin Admin",
            "TASK_STATUS_CHANGED",
            "TO_DO",
            "IN_PROGRESS",
            "Admin Admin moved task \"Build polished task board\" from To Do to In Progress",
            Instant.parse("2026-06-09T14:00:00Z"))));

        mockMvc.perform(get("/api/tasks/10/activities").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].actorName").value("Admin Admin"))
            .andExpect(jsonPath("$[0].actionType").value("TASK_STATUS_CHANGED"))
            .andExpect(jsonPath("$[0].message").value("Admin Admin moved task \"Build polished task board\" from To Do to In Progress"));
    }

    @Test
    void listsProjectActivitiesForAuthenticatedUsers() throws Exception {
        given(activityService.listProjectActivities(eq(1L), any(Authentication.class))).willReturn(List.of(new ActivityResponse(
            2L,
            1L,
            4L,
            null,
            "SPRINT",
            "Sprint 1",
            "Admin Admin",
            "SPRINT_CREATED",
            null,
            "ACTIVE",
            "Admin Admin created sprint \"Sprint 1\"",
            Instant.parse("2026-06-10T09:30:00Z"))));

        mockMvc.perform(get("/api/projects/1/activities").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].subjectType").value("SPRINT"))
            .andExpect(jsonPath("$[0].subjectName").value("Sprint 1"))
            .andExpect(jsonPath("$[0].actionType").value("SPRINT_CREATED"));
    }

    @Test
    void rejectsAnonymousActivityRequests() throws Exception {
        mockMvc.perform(get("/api/tasks/10/activities"))
            .andExpect(status().isUnauthorized());
    }
}
