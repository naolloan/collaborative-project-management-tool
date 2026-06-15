package com.collabpm.backend.task.dto;

import java.time.LocalDate;

public record TaskResponse(
    Long id,
    Long projectId,
    String title,
    String description,
    String status,
    String priority,
    Long assigneeId,
    String assigneeName,
    String createdByName,
    LocalDate dueDate,
    Long sprintId,
    String sprintName
) {
}
