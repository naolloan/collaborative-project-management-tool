package com.collabpm.backend.activity.dto;

import java.time.Instant;

public record ActivityResponse(
    Long id,
    Long taskId,
    String actorName,
    String actionType,
    String oldValue,
    String newValue,
    String message,
    Instant createdAt
) {
}
