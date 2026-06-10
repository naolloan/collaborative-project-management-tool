package com.collabpm.backend.project.dto;

import java.time.LocalDate;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    LocalDate startDate,
    LocalDate dueDate,
    String status
) {
}
