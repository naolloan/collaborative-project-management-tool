package com.collabpm.backend.project.dto;

public record ProjectTeamMemberResponse(
    Long userId,
    String fullName,
    String email,
    String projectRole
) {
}
