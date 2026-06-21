package com.collabpm.backend.project;

import com.collabpm.backend.organization.model.OrganizationalUnit;
import com.collabpm.backend.organization.model.OrganizationalUnitType;
import com.collabpm.backend.organization.repository.OrganizationalUnitRepository;
import com.collabpm.backend.activity.ActivityService;
import com.collabpm.backend.project.dto.CreateProjectRequest;
import com.collabpm.backend.project.dto.ProjectResponse;
import com.collabpm.backend.project.dto.ProjectTeamSummaryResponse;
import com.collabpm.backend.project.dto.UpdateProjectRequest;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectRole;
import com.collabpm.backend.project.model.ProjectStatus;
import com.collabpm.backend.project.repository.ProjectRepository;
import com.collabpm.backend.sprint.model.Sprint;
import com.collabpm.backend.sprint.model.SprintPriority;
import com.collabpm.backend.sprint.repository.SprintRepository;
import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.task.model.TaskPriority;
import com.collabpm.backend.task.repository.TaskRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.SystemRole;
import com.collabpm.backend.user.User;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final ProjectAccessService projectAccessService;
    private final ProjectHealthService projectHealthService;
    private final ActivityService activityService;

    public ProjectService(
        ProjectRepository projectRepository,
        OrganizationalUnitRepository organizationalUnitRepository,
        SprintRepository sprintRepository,
        TaskRepository taskRepository,
        CurrentUserService currentUserService,
        ProjectAccessService projectAccessService,
        ProjectHealthService projectHealthService,
        ActivityService activityService
    ) {
        this.projectRepository = projectRepository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.sprintRepository = sprintRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.projectAccessService = projectAccessService;
        this.projectHealthService = projectHealthService;
        this.activityService = activityService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(Authentication authentication) {
        User currentUser = currentUserService.getOrCreateCurrentUser(authentication);
        List<Project> projects = currentUser.getSystemRole() == SystemRole.ADMINISTRATOR
            ? projectRepository.findAllByStatusNotOrderByCreatedAtDesc(ProjectStatus.ARCHIVED)
            : projectRepository.findDistinctByMembersUserIdAndStatusNotOrderByCreatedAtDesc(currentUser.getId(), ProjectStatus.ARCHIVED);

        Map<Long, Integer> progressByProjectId = computeProgressByProjectId(projects);

        return projects.stream()
            .map((project) -> toResponse(project, progressByProjectId.getOrDefault(project.getId(), 0)))
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
        activityService.recordProjectCreated(savedProject, creator);
        return toResponse(savedProject, computeProjectProgress(savedProject));
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
        User actor = projectAccessService.currentUser(authentication);
        activityService.recordProjectUpdated(project, actor);

        return toResponse(project, computeProjectProgress(project));
    }

    @Transactional
    public ProjectResponse archiveProject(Long projectId, Authentication authentication) {
        projectAccessService.ensureCanManageProject(projectId, authentication);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        project.setStatus(ProjectStatus.ARCHIVED);
        project.setHealth("ON_TRACK");
        User actor = projectAccessService.currentUser(authentication);
        activityService.recordProjectArchived(project, actor);

        return toResponse(project, computeProjectProgress(project));
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

    private ProjectResponse toResponse(Project project, int progressPercentage) {
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
            computedHealth,
            progressPercentage);
    }

    private Map<Long, Integer> computeProgressByProjectId(List<Project> projects) {
        if (projects.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<Sprint>> sprintsByProjectId = projects.stream()
            .collect(Collectors.toMap(
                Project::getId,
                (project) -> sprintRepository.findByProjectIdOrderByStartDateAscCreatedAtAsc(project.getId())
            ));

        Map<Long, List<Task>> tasksByProjectId = projects.stream()
            .collect(Collectors.toMap(
                Project::getId,
                (project) -> taskRepository.findByProjectId(project.getId())
            ));

        return projects.stream()
            .collect(Collectors.toMap(
                Project::getId,
                (project) -> computeProjectProgress(project, sprintsByProjectId.get(project.getId()), tasksByProjectId.get(project.getId()))
            ));
    }

    private int computeProjectProgress(Project project) {
        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByStartDateAscCreatedAtAsc(project.getId());
        List<Task> tasks = taskRepository.findByProjectId(project.getId());
        return computeProjectProgress(project, sprints, tasks);
    }

    private int computeProjectProgress(Project project, List<Sprint> sprints, List<Task> tasks) {
        List<Sprint> safeSprints = sprints == null ? List.of() : sprints;
        List<Task> safeTasks = tasks == null ? List.of() : tasks;

        if (project.getStatus() == ProjectStatus.COMPLETED || project.getStatus() == ProjectStatus.ARCHIVED) {
            return 100;
        }

        if (safeSprints.isEmpty()) {
            return 0;
        }

        double weightedCompleted = 0;
        double weightedTotal = 0;
        Map<Long, List<Task>> tasksBySprintId = safeTasks.stream()
            .filter((task) -> task.getSprint() != null && task.getSprint().getId() != null)
            .collect(Collectors.groupingBy((task) -> task.getSprint().getId()));

        for (Sprint sprint : safeSprints) {
            List<Task> sprintTasks = tasksBySprintId.getOrDefault(sprint.getId(), List.of());
            int sprintTaskWeight = totalTaskWeight(sprintTasks);
            if (sprintTaskWeight == 0) {
                continue;
            }

            int sprintPriorityWeight = sprintPriorityWeight(sprint.getPriority());
            weightedCompleted += completedTaskWeight(sprintTasks) * sprintPriorityWeight;
            weightedTotal += sprintTaskWeight * sprintPriorityWeight;
        }

        if (weightedTotal == 0) {
            return 0;
        }

        return (int) Math.round((weightedCompleted / weightedTotal) * 100.0);
    }

    private int completedTaskWeight(List<Task> tasks) {
        return tasks.stream()
            .filter((task) -> task.getStatus() == com.collabpm.backend.task.model.TaskStatus.DONE)
            .mapToInt((task) -> taskPriorityWeight(task.getPriority()))
            .sum();
    }

    private int totalTaskWeight(List<Task> tasks) {
        return tasks.stream()
            .mapToInt((task) -> taskPriorityWeight(task.getPriority()))
            .sum();
    }

    private int taskPriorityWeight(TaskPriority priority) {
        if (priority == null) {
            return 2;
        }

        return switch (priority) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }

    private int sprintPriorityWeight(SprintPriority priority) {
        if (priority == null) {
            return 2;
        }

        return switch (priority) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }
}
