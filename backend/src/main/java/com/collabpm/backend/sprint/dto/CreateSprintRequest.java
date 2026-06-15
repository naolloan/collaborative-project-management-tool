package com.collabpm.backend.sprint.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateSprintRequest(
    @NotBlank(message = "Sprint name is required")
    String name,
    String goal,
    LocalDate startDate,
    LocalDate endDate,
    String status
) {
}
