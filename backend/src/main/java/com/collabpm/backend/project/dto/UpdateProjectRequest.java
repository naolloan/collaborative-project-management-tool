package com.collabpm.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record UpdateProjectRequest(
    @NotBlank(message = "Project name is required")
    String name,
    String description,
    LocalDate startDate,
    LocalDate dueDate
) {
}
