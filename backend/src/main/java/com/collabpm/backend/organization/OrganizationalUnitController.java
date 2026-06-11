package com.collabpm.backend.organization;

import com.collabpm.backend.organization.dto.OrganizationalUnitRequest;
import com.collabpm.backend.organization.dto.OrganizationalUnitResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizational-units")
public class OrganizationalUnitController {

    private final OrganizationalUnitService organizationalUnitService;

    public OrganizationalUnitController(OrganizationalUnitService organizationalUnitService) {
        this.organizationalUnitService = organizationalUnitService;
    }

    @GetMapping
    public List<OrganizationalUnitResponse> listActiveUnits() {
        return organizationalUnitService.listActiveUnits();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationalUnitResponse createUnit(
        @Valid @RequestBody OrganizationalUnitRequest request,
        Authentication authentication
    ) {
        return organizationalUnitService.createUnit(request, authentication);
    }

    @PatchMapping("/{unitId}")
    public OrganizationalUnitResponse updateUnit(
        @PathVariable Long unitId,
        @Valid @RequestBody OrganizationalUnitRequest request,
        Authentication authentication
    ) {
        return organizationalUnitService.updateUnit(unitId, request, authentication);
    }

    @PatchMapping("/{unitId}/deactivate")
    public OrganizationalUnitResponse deactivateUnit(@PathVariable Long unitId, Authentication authentication) {
        return organizationalUnitService.deactivateUnit(unitId, authentication);
    }
}
