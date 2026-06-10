package com.collabpm.backend.auth.dto;

import java.util.List;

public record CurrentUserResponse(
    String id,
    String username,
    String email,
    String fullName,
    List<String> roles
) {
}
