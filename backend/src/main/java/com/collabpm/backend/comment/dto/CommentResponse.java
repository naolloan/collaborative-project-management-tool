package com.collabpm.backend.comment.dto;

import java.time.Instant;

public record CommentResponse(
    Long id,
    Long taskId,
    String authorName,
    String content,
    Instant createdAt
) {
}
