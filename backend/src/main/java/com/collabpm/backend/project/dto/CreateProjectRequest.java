package com.collabpm.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateProjectRequest(
    @NotBlank(message = "Project name is required")
    String name,
    String description,
    Long organizationalUnitId,
    LocalDate startDate,
    LocalDate dueDate
) {
}
