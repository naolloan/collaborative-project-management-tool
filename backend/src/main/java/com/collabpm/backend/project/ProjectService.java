package com.collabpm.backend.project;

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
    private final CurrentUserService currentUserService;
    private final ProjectAccessService projectAccessService;

    public ProjectService(
        ProjectRepository projectRepository,
        CurrentUserService currentUserService,
        ProjectAccessService projectAccessService
    ) {
        this.projectRepository = projectRepository;
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
        Project project = new Project(
            request.name().trim(),
            normalizeDescription(request.description()),
            creator,
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

        project.setName(request.name().trim());
        project.setDescription(normalizeDescription(request.description()));
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

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getStartDate(),
            project.getDueDate(),
            project.getStatus().name());
    }
}
