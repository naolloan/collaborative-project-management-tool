package com.collabpm.backend.task;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.config.SecurityConfig;
import com.collabpm.backend.task.dto.CreateTaskRequest;
import com.collabpm.backend.task.dto.TaskResponse;
import com.collabpm.backend.task.dto.UpdateTaskRequest;
import com.collabpm.backend.task.dto.UpdateTaskStatusRequest;
import com.collabpm.backend.task.model.TaskPriority;
import com.collabpm.backend.task.model.TaskStatus;
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

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void listsProjectTasksForAuthenticatedUsers() throws Exception {
        given(taskService.listProjectTasks(eq(1L), any(Authentication.class))).willReturn(List.of(new TaskResponse(
            10L,
            1L,
            "Write requirements",
            "Prepare initial requirements document",
            "TO_DO",
            "HIGH",
            20L,
            "Admin Admin",
            "Admin Admin",
            LocalDate.of(2026, 6, 12),
            4L,
            "Sprint 1")));

        mockMvc.perform(get("/api/projects/1/tasks").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Write requirements"))
            .andExpect(jsonPath("$[0].status").value("TO_DO"));
    }

    @Test
    void createsProjectTasksForAuthenticatedUsers() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
            "Build task board",
            "Create the first workflow board",
            TaskPriority.MEDIUM,
            20L,
            4L,
            null);
        given(taskService.createTask(eq(1L), eq(request), any(Authentication.class))).willReturn(new TaskResponse(
            11L,
            1L,
            request.title(),
            request.description(),
            "TO_DO",
            "MEDIUM",
            20L,
            "Admin Admin",
            "Admin Admin",
            null,
            4L,
            "Sprint 1"));

        mockMvc.perform(post("/api/projects/1/tasks")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Build task board",
                      "description": "Create the first workflow board",
                      "priority": "MEDIUM",
                      "assigneeId": 20,
                      "sprintId": 4,
                      "dueDate": null
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(11))
            .andExpect(jsonPath("$.title").value("Build task board"))
            .andExpect(jsonPath("$.sprintId").value(4))
            .andExpect(jsonPath("$.status").value("TO_DO"));
    }

    @Test
    void updatesTaskStatusForAuthenticatedUsers() throws Exception {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);
        given(taskService.updateTaskStatus(eq(11L), eq(request), any(Authentication.class))).willReturn(new TaskResponse(
            11L,
            1L,
            "Build task board",
            "Create the first workflow board",
            "IN_PROGRESS",
            "MEDIUM",
            20L,
            "Admin Admin",
            "Admin Admin",
            null,
            4L,
            "Sprint 1"));

        mockMvc.perform(patch("/api/tasks/11/status")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "IN_PROGRESS"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(11))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updatesTaskDetailsForAuthenticatedUsers() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest(
            "Build polished task board",
            "Refine the workflow board",
            TaskPriority.HIGH,
            21L,
            5L,
            LocalDate.of(2026, 6, 20));
        given(taskService.updateTask(eq(11L), eq(request), any(Authentication.class))).willReturn(new TaskResponse(
            11L,
            1L,
            request.title(),
            request.description(),
            "TO_DO",
            "HIGH",
            21L,
            "Team Member",
            "Admin Admin",
            request.dueDate(),
            5L,
            "Sprint 2"));

        mockMvc.perform(patch("/api/tasks/11")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Build polished task board",
                      "description": "Refine the workflow board",
                      "priority": "HIGH",
                      "assigneeId": 21,
                      "sprintId": 5,
                      "dueDate": "2026-06-20"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(11))
            .andExpect(jsonPath("$.title").value("Build polished task board"))
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.sprintId").value(5))
            .andExpect(jsonPath("$.assigneeId").value(21));
    }

    @Test
    void deletesTasksForAuthenticatedUsers() throws Exception {
        mockMvc.perform(delete("/api/tasks/11").with(jwt()))
            .andExpect(status().isNoContent());
    }

    @Test
    void rejectsAnonymousTaskRequests() throws Exception {
        mockMvc.perform(get("/api/projects/1/tasks"))
            .andExpect(status().isUnauthorized());
    }
}
