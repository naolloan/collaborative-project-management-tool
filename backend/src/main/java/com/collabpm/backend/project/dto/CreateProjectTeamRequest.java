package com.collabpm.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateProjectTeamRequest(
    @NotBlank(message = "Team name is required")
    @Size(max = 120)
    String name,

    @Size(max = 1000)
    String description,

    List<Long> memberUserIds
) {
}
