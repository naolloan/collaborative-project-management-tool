package com.collabpm.backend.project.dto;

import java.time.LocalDate;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    Long organizationalUnitId,
    String organizationalUnitName,
    String organizationalUnitType,
    LocalDate startDate,
    LocalDate dueDate,
    String status
) {
}
