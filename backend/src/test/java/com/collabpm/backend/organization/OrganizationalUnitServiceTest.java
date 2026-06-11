package com.collabpm.backend.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.collabpm.backend.organization.dto.OrganizationalUnitRequest;
import com.collabpm.backend.organization.dto.OrganizationalUnitResponse;
import com.collabpm.backend.organization.model.OrganizationalUnit;
import com.collabpm.backend.organization.model.OrganizationalUnitType;
import com.collabpm.backend.organization.repository.OrganizationalUnitRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

class OrganizationalUnitServiceTest {

    private final OrganizationalUnitRepository organizationalUnitRepository = mock(OrganizationalUnitRepository.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final OrganizationalUnitService organizationalUnitService = new OrganizationalUnitService(
        organizationalUnitRepository,
        currentUserService);

    @Test
    void listsActiveUnitsOrderedByName() {
        given(organizationalUnitRepository.findAllByActiveTrueOrderByNameAsc()).willReturn(List.of(
            new OrganizationalUnit("Digital Banking Department", OrganizationalUnitType.DEPARTMENT, null)));

        List<OrganizationalUnitResponse> units = organizationalUnitService.listActiveUnits();

        assertThat(units).hasSize(1);
        assertThat(units.get(0).name()).isEqualTo("Digital Banking Department");
        assertThat(units.get(0).type()).isEqualTo("DEPARTMENT");
    }

    @Test
    void allowsAdministratorsToCreateUnits() {
        Authentication authentication = mock(Authentication.class);
        User administrator = mock(User.class);
        OrganizationalUnitRequest request = new OrganizationalUnitRequest(
            "  Digital Banking Department  ",
            OrganizationalUnitType.DEPARTMENT,
            "  Owns digital delivery  ");

        given(currentUserService.getOrCreateCurrentUser(authentication)).willReturn(administrator);
        given(administrator.getSystemRole()).willReturn(SystemRole.ADMINISTRATOR);
        given(organizationalUnitRepository.existsByNameIgnoreCase("Digital Banking Department")).willReturn(false);
        given(organizationalUnitRepository.save(any(OrganizationalUnit.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        OrganizationalUnitResponse response = organizationalUnitService.createUnit(request, authentication);

        assertThat(response.name()).isEqualTo("Digital Banking Department");
        assertThat(response.description()).isEqualTo("Owns digital delivery");
        verify(organizationalUnitRepository).save(any(OrganizationalUnit.class));
    }

    @Test
    void rejectsNonAdministratorsCreatingUnits() {
        Authentication authentication = mock(Authentication.class);
        User teamMember = mock(User.class);

        given(currentUserService.getOrCreateCurrentUser(authentication)).willReturn(teamMember);
        given(teamMember.getSystemRole()).willReturn(SystemRole.TEAM_MEMBER);

        assertThatThrownBy(() -> organizationalUnitService.createUnit(
            new OrganizationalUnitRequest("Adama Branch", OrganizationalUnitType.BRANCH, null),
            authentication))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Only administrators can manage organizational units");
    }

    @Test
    void rejectsDuplicateUnitNames() {
        Authentication authentication = mock(Authentication.class);
        User administrator = mock(User.class);

        given(currentUserService.getOrCreateCurrentUser(authentication)).willReturn(administrator);
        given(administrator.getSystemRole()).willReturn(SystemRole.ADMINISTRATOR);
        given(organizationalUnitRepository.existsByNameIgnoreCase("Adama Branch")).willReturn(true);

        assertThatThrownBy(() -> organizationalUnitService.createUnit(
            new OrganizationalUnitRequest("Adama Branch", OrganizationalUnitType.BRANCH, null),
            authentication))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Organizational unit name already exists");
    }

    @Test
    void deactivatesUnitsWithoutDeletingThem() {
        Authentication authentication = mock(Authentication.class);
        User administrator = mock(User.class);
        OrganizationalUnit unit = new OrganizationalUnit("Legacy Team", OrganizationalUnitType.TEAM, null);

        given(currentUserService.getOrCreateCurrentUser(authentication)).willReturn(administrator);
        given(administrator.getSystemRole()).willReturn(SystemRole.ADMINISTRATOR);
        given(organizationalUnitRepository.findById(1L)).willReturn(Optional.of(unit));

        OrganizationalUnitResponse response = organizationalUnitService.deactivateUnit(1L, authentication);

        assertThat(response.active()).isFalse();
    }
}
