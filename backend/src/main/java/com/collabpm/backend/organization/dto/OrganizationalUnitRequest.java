package com.collabpm.backend.organization.dto;

import com.collabpm.backend.organization.model.OrganizationalUnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizationalUnitRequest(
    @NotBlank
    @Size(max = 120)
    String name,

    @NotNull
    OrganizationalUnitType type,

    @Size(max = 1000)
    String description
) {
}
