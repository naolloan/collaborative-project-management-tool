package com.collabpm.backend.project.dto;

import com.collabpm.backend.project.model.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddProjectMemberRequest(
    @NotBlank(message = "Member email is required")
    @Email(message = "Member email must be valid")
    String email,
    ProjectRole projectRole
) {
}
