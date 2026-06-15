package com.collabpm.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public record CreateProjectRequest(
    @NotBlank(message = "Project name is required")
    String name,
    String description,
    List<Long> teamIds,
    LocalDate startDate,
    LocalDate dueDate,
    String status
) {
}
