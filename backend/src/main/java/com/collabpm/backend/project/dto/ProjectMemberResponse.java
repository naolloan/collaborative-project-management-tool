package com.collabpm.backend.project.dto;

import java.time.Instant;

public record ProjectMemberResponse(
    Long id,
    Long userId,
    String fullName,
    String email,
    String projectRole,
    Instant joinedAt
) {
}
