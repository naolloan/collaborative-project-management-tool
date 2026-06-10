package com.collabpm.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
    @NotBlank(message = "Comment content is required")
    String content
) {
}
