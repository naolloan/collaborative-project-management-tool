package com.collabpm.backend.task;

import com.collabpm.backend.task.dto.CreateTaskRequest;
import com.collabpm.backend.task.dto.TaskResponse;
import com.collabpm.backend.task.dto.UpdateTaskRequest;
import com.collabpm.backend.task.dto.UpdateTaskStatusRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> listProjectTasks(@PathVariable Long projectId, Authentication authentication) {
        return taskService.listProjectTasks(projectId, authentication);
    }

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
        @PathVariable Long projectId,
        @Valid @RequestBody CreateTaskRequest request,
        Authentication authentication
    ) {
        return taskService.createTask(projectId, request, authentication);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public TaskResponse updateTaskStatus(
        @PathVariable Long taskId,
        @Valid @RequestBody UpdateTaskStatusRequest request,
        Authentication authentication
    ) {
        return taskService.updateTaskStatus(taskId, request, authentication);
    }

    @PatchMapping("/tasks/{taskId}")
    public TaskResponse updateTask(
        @PathVariable Long taskId,
        @Valid @RequestBody UpdateTaskRequest request,
        Authentication authentication
    ) {
        return taskService.updateTask(taskId, request, authentication);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId, Authentication authentication) {
        taskService.deleteTask(taskId, authentication);
    }
}
