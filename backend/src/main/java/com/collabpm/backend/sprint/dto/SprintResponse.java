package com.collabpm.backend.sprint.dto;

import java.time.LocalDate;

public record SprintResponse(
    Long id,
    Long projectId,
    String name,
    String goal,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    String priority,
    long totalTaskCount,
    long completedTaskCount
) {
}
