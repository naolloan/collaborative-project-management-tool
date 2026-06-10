package com.collabpm.backend.task.dto;

import com.collabpm.backend.task.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
    @NotNull(message = "Task status is required")
    TaskStatus status
) {
}
