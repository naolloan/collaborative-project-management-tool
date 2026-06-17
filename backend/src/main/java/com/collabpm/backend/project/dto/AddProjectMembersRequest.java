package com.collabpm.backend.project.dto;

import com.collabpm.backend.project.model.ProjectRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddProjectMembersRequest(
    @NotEmpty List<Long> userIds,
    @NotNull ProjectRole projectRole
) {
}
