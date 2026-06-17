package com.collabpm.backend.auth.dto;

public record EmployeeDirectoryEntryResponse(
    Long id,
    String fullName,
    String email,
    String systemRole
) {
}
