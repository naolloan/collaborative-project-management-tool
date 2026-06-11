package com.collabpm.backend.project;

import com.collabpm.backend.organization.model.OrganizationalUnit;
import com.collabpm.backend.organization.repository.OrganizationalUnitRepository;
import com.collabpm.backend.project.dto.CreateProjectRequest;
import com.collabpm.backend.project.dto.ProjectResponse;
import com.collabpm.backend.project.dto.UpdateProjectRequest;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectRole;
import com.collabpm.backend.project.model.ProjectStatus;
import com.collabpm.backend.project.repository.ProjectRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final CurrentUserService currentUserService;
    private final ProjectAccessService projectAccessService;

    public ProjectService(
        ProjectRepository projectRepository,
        OrganizationalUnitRepository organizationalUnitRepository,
        CurrentUserService currentUserService,
        ProjectAccessService projectAccessService
    ) {
        this.projectRepository = projectRepository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.currentUserService = currentUserService;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(Authentication authentication) {
        User currentUser = currentUserService.getOrCreateCurrentUser(authentication);
        List<Project> projects = currentUser.getSystemRole() == SystemRole.ADMINISTRATOR
            ? projectRepository.findAllByStatusOrderByCreatedAtDesc(ProjectStatus.ACTIVE)
            : projectRepository.findDistinctByMembersUserIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), ProjectStatus.ACTIVE);

        return projects.stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Authentication authentication) {
        validateDates(request);

        User creator = currentUserService.getOrCreateCurrentUser(authentication);
        OrganizationalUnit organizationalUnit = resolveOrganizationalUnit(request.organizationalUnitId());
        Project project = new Project(
            request.name().trim(),
            normalizeDescription(request.description()),
            creator,
            organizationalUnit,
            request.startDate(),
            request.dueDate());
        project.addMember(creator, ProjectRole.MANAGER);

        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Authentication authentication) {
        validateDates(request.startDate(), request.dueDate());
        projectAccessService.ensureCanManageProject(projectId, authentication);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        OrganizationalUnit organizationalUnit = resolveOrganizationalUnit(request.organizationalUnitId());

        project.setName(request.name().trim());
        project.setDescription(normalizeDescription(request.description()));
        project.setOrganizationalUnit(organizationalUnit);
        project.setStartDate(request.startDate());
        project.setDueDate(request.dueDate());

        return toResponse(project);
    }

    @Transactional
    public ProjectResponse archiveProject(Long projectId, Authentication authentication) {
        projectAccessService.ensureCanManageProject(projectId, authentication);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        project.setStatus(ProjectStatus.ARCHIVED);

        return toResponse(project);
    }

    private void validateDates(CreateProjectRequest request) {
        validateDates(request.startDate(), request.dueDate());
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

    private OrganizationalUnit resolveOrganizationalUnit(Long organizationalUnitId) {
        if (organizationalUnitId == null) {
            return null;
        }

        OrganizationalUnit unit = organizationalUnitRepository.findById(organizationalUnitId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organizational unit not found"));

        if (!unit.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organizational unit is inactive");
        }

        return unit;
    }

    private ProjectResponse toResponse(Project project) {
        OrganizationalUnit unit = project.getOrganizationalUnit();
        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            unit == null ? null : unit.getId(),
            unit == null ? null : unit.getName(),
            unit == null ? null : unit.getType().name(),
            project.getStartDate(),
            project.getDueDate(),
            project.getStatus().name());
    }
}
