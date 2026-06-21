package com.collabpm.backend.project.dto;

import java.time.LocalDate;
import java.util.List;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    List<ProjectTeamSummaryResponse> teams,
    LocalDate startDate,
    LocalDate dueDate,
    String status,
    String health,
    Integer progressPercentage
) {
}
