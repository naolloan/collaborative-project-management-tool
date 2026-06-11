package com.collabpm.backend.organization;

import com.collabpm.backend.organization.dto.OrganizationalUnitRequest;
import com.collabpm.backend.organization.dto.OrganizationalUnitResponse;
import com.collabpm.backend.organization.model.OrganizationalUnit;
import com.collabpm.backend.organization.repository.OrganizationalUnitRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrganizationalUnitService {

    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final CurrentUserService currentUserService;

    public OrganizationalUnitService(
        OrganizationalUnitRepository organizationalUnitRepository,
        CurrentUserService currentUserService
    ) {
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<OrganizationalUnitResponse> listActiveUnits() {
        return organizationalUnitRepository.findAllByActiveTrueOrderByNameAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public OrganizationalUnitResponse createUnit(OrganizationalUnitRequest request, Authentication authentication) {
        ensureAdministrator(authentication);
        String name = normalizedName(request.name());

        if (organizationalUnitRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Organizational unit name already exists");
        }

        OrganizationalUnit unit = new OrganizationalUnit(name, request.type(), normalizeDescription(request.description()));
        return toResponse(organizationalUnitRepository.save(unit));
    }

    @Transactional
    public OrganizationalUnitResponse updateUnit(
        Long unitId,
        OrganizationalUnitRequest request,
        Authentication authentication
    ) {
        ensureAdministrator(authentication);
        OrganizationalUnit unit = findUnit(unitId);
        String name = normalizedName(request.name());

        if (!unit.getName().equalsIgnoreCase(name) && organizationalUnitRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Organizational unit name already exists");
        }

        unit.setName(name);
        unit.setType(request.type());
        unit.setDescription(normalizeDescription(request.description()));
        return toResponse(unit);
    }

    @Transactional
    public OrganizationalUnitResponse deactivateUnit(Long unitId, Authentication authentication) {
        ensureAdministrator(authentication);
        OrganizationalUnit unit = findUnit(unitId);
        unit.deactivate();
        return toResponse(unit);
    }

    private OrganizationalUnit findUnit(Long unitId) {
        return organizationalUnitRepository.findById(unitId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizational unit not found"));
    }

    private void ensureAdministrator(Authentication authentication) {
        User currentUser = currentUserService.getOrCreateCurrentUser(authentication);
        if (currentUser.getSystemRole() != SystemRole.ADMINISTRATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can manage organizational units");
        }
    }

    private String normalizedName(String name) {
        return name.trim();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private OrganizationalUnitResponse toResponse(OrganizationalUnit unit) {
        return new OrganizationalUnitResponse(
            unit.getId(),
            unit.getName(),
            unit.getType().name(),
            unit.getDescription(),
            unit.isActive());
    }
}
