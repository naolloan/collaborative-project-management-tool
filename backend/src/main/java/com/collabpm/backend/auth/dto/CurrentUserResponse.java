package com.collabpm.backend.auth.dto;

import java.util.List;

public record CurrentUserResponse(
    Long id,
    String systemRole,
    String username,
    String email,
    String fullName,
    List<String> roles
) {
}
