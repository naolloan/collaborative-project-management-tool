package com.collabpm.backend.project;

import com.collabpm.backend.organization.model.OrganizationalUnit;
import com.collabpm.backend.organization.model.OrganizationalUnitType;
import com.collabpm.backend.organization.repository.OrganizationalUnitRepository;
import com.collabpm.backend.project.dto.CreateProjectRequest;
import com.collabpm.backend.project.dto.ProjectResponse;
import com.collabpm.backend.project.dto.ProjectTeamSummaryResponse;
import com.collabpm.backend.project.dto.UpdateProjectRequest;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectRole;
import com.collabpm.backend.project.model.ProjectStatus;
import com.collabpm.backend.project.repository.ProjectRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final CurrentUserService currentUserService;
    private final ProjectAccessService projectAccessService;
    private final ProjectHealthService projectHealthService;

    public ProjectService(
        ProjectRepository projectRepository,
        OrganizationalUnitRepository organizationalUnitRepository,
        CurrentUserService currentUserService,
        ProjectAccessService projectAccessService,
        ProjectHealthService projectHealthService
    ) {
        this.projectRepository = projectRepository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.currentUserService = currentUserService;
        this.projectAccessService = projectAccessService;
        this.projectHealthService = projectHealthService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(Authentication authentication) {
        User currentUser = currentUserService.getOrCreateCurrentUser(authentication);
        List<Project> projects = currentUser.getSystemRole() == SystemRole.ADMINISTRATOR
            ? projectRepository.findAllByStatusNotOrderByCreatedAtDesc(ProjectStatus.ARCHIVED)
            : projectRepository.findDistinctByMembersUserIdAndStatusNotOrderByCreatedAtDesc(currentUser.getId(), ProjectStatus.ARCHIVED);

        return projects.stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Authentication authentication) {
        validateDates(request);

        User creator = currentUserService.getOrCreateCurrentUser(authentication);
        List<OrganizationalUnit> teams = resolveTeams(request.teamIds());
        Project project = new Project(
            request.name().trim(),
            normalizeDescription(request.description()),
            creator,
            primaryTeamReference(teams),
            request.startDate(),
            request.dueDate(),
            resolveProjectStatus(request.status()),
            "ON_TRACK");
        project.setTeams(teams);
        project.addMember(creator, ProjectRole.MANAGER);

        Project savedProject = projectRepository.save(project);
        savedProject.setHealth(projectHealthService.computeHealth(savedProject));
        return toResponse(savedProject);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Authentication authentication) {
        validateDates(request.startDate(), request.dueDate());
        projectAccessService.ensureCanManageProject(projectId, authentication);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        List<OrganizationalUnit> teams = resolveTeams(request.teamIds());

        project.setName(request.name().trim());
        project.setDescription(normalizeDescription(request.description()));
        project.setOrganizationalUnit(primaryTeamReference(teams));
        project.setTeams(teams);
        project.setStartDate(request.startDate());
        project.setDueDate(request.dueDate());
        project.setStatus(resolveProjectStatus(request.status()));
        project.setHealth(projectHealthService.computeHealth(project));

        return toResponse(project);
    }

    @Transactional
    public ProjectResponse archiveProject(Long projectId, Authentication authentication) {
        projectAccessService.ensureCanManageProject(projectId, authentication);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        project.setStatus(ProjectStatus.ARCHIVED);
        project.setHealth("ON_TRACK");

        return toResponse(project);
    }

    private void validateDates(CreateProjectRequest request) {
        validateDates(request.startDate(), request.dueDate());
    }

    private ProjectStatus resolveProjectStatus(String status) {
        if (status == null || status.isBlank()) {
            return ProjectStatus.PLANNED;
        }

        try {
            return ProjectStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project status is invalid");
        }
    }

    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate dueDate) {
        if (startDate != null && dueDate != null && dueDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project due date cannot be before the start date");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private List<OrganizationalUnit> resolveTeams(List<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return List.of();
        }

        List<OrganizationalUnit> teams = new ArrayList<>();
        for (Long teamId : new LinkedHashSet<>(teamIds)) {
            OrganizationalUnit team = organizationalUnitRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project team not found"));
            if (!team.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project team is inactive");
            }
            if (team.getType() != OrganizationalUnitType.TEAM) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only organizational units of type TEAM can be assigned to projects");
            }
            teams.add(team);
        }

        return teams;
    }

    private OrganizationalUnit primaryTeamReference(List<OrganizationalUnit> teams) {
        if (teams == null || teams.isEmpty()) {
            return null;
        }

        return teams.get(0);
    }

    private ProjectResponse toResponse(Project project) {
        OrganizationalUnit unit = project.getOrganizationalUnit();
        String computedHealth = projectHealthService.computeHealth(project);
        project.setHealth(computedHealth);
        List<ProjectTeamSummaryResponse> teamSummaries = project.getTeams().stream()
            .map((team) -> new ProjectTeamSummaryResponse(team.getId(), team.getName(), team.getType().name()))
            .toList();

        if (teamSummaries.isEmpty() && unit != null && unit.getType() == OrganizationalUnitType.TEAM) {
            teamSummaries = List.of(new ProjectTeamSummaryResponse(unit.getId(), unit.getName(), unit.getType().name()));
        }

        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            teamSummaries,
            project.getStartDate(),
            project.getDueDate(),
            project.getStatus().name(),
            computedHealth);
    }
}
