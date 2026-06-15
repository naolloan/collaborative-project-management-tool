package com.collabpm.backend.activity.dto;

import java.time.Instant;

public record ActivityResponse(
    Long id,
    Long projectId,
    Long sprintId,
    Long taskId,
    String subjectType,
    String subjectName,
    String actorName,
    String actionType,
    String oldValue,
    String newValue,
    String message,
    Instant createdAt
) {
}
