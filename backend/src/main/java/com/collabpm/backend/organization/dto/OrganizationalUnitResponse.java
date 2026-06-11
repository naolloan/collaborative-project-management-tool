package com.collabpm.backend.organization.dto;

public record OrganizationalUnitResponse(
    Long id,
    String name,
    String type,
    String description,
    boolean active
) {
}
