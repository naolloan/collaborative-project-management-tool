package com.collabpm.backend.project.dto;

import java.util.List;

public record ProjectTeamResponse(
    Long id,
    Long projectId,
    String name,
    String description,
    List<ProjectTeamMemberResponse> members,
    long memberCount,
    long managerCount
) {
}
