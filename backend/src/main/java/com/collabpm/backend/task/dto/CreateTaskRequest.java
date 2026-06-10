package com.collabpm.backend.task.dto;

import com.collabpm.backend.task.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateTaskRequest(
    @NotBlank(message = "Task title is required")
    String title,
    String description,
    TaskPriority priority,
    Long assigneeId,
    LocalDate dueDate
) {
}
