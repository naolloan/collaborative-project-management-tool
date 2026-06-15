package com.collabpm.backend.sprint;

import com.collabpm.backend.project.ProjectAccessService;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.repository.ProjectRepository;
import com.collabpm.backend.sprint.dto.CreateSprintRequest;
import com.collabpm.backend.sprint.dto.SprintResponse;
import com.collabpm.backend.sprint.dto.UpdateSprintRequest;
import com.collabpm.backend.sprint.model.Sprint;
import com.collabpm.backend.sprint.model.SprintStatus;
import com.collabpm.backend.sprint.repository.SprintRepository;
import com.collabpm.backend.task.model.TaskStatus;
import com.collabpm.backend.task.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectAccessService projectAccessService;

    public SprintService(
        SprintRepository sprintRepository,
        ProjectRepository projectRepository,
        TaskRepository taskRepository,
        ProjectAccessService projectAccessService
    ) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> listProjectSprints(Long projectId, Authentication authentication) {
        ensureProjectExists(projectId);
        projectAccessService.ensureCanViewProject(projectId, authentication);
        return sprintRepository.findByProjectIdOrderByStartDateAscCreatedAtAsc(projectId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public SprintResponse createSprint(Long projectId, CreateSprintRequest request, Authentication authentication) {
        Project project = ensureProjectExists(projectId);
        projectAccessService.ensureCanManageProject(projectId, authentication);
        validateDates(request.startDate(), request.endDate());

        SprintStatus status = resolveStatus(request.status());
        ensureActiveSprintUniqueness(projectId, status, null);

        Sprint sprint = new Sprint(
            project,
            request.name().trim(),
            normalizeGoal(request.goal()),
            request.startDate(),
            request.endDate(),
            status
        );
        return toResponse(sprintRepository.save(sprint));
    }

    @Transactional
    public SprintResponse updateSprint(Long sprintId, UpdateSprintRequest request, Authentication authentication) {
        Sprint sprint = sprintRepository.findById(sprintId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));
        projectAccessService.ensureCanManageProject(sprint.getProject().getId(), authentication);
        validateDates(request.startDate(), request.endDate());

        SprintStatus status = resolveStatus(request.status());
        ensureActiveSprintUniqueness(sprint.getProject().getId(), status, sprintId);

        sprint.setName(request.name().trim());
        sprint.setGoal(normalizeGoal(request.goal()));
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setStatus(status);

        return toResponse(sprint);
    }

    private Project ensureProjectExists(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sprint end date cannot be before the start date");
        }
    }

    private SprintStatus resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return SprintStatus.PLANNED;
        }

        try {
            return SprintStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sprint status is invalid");
        }
    }

    private void ensureActiveSprintUniqueness(Long projectId, SprintStatus requestedStatus, Long currentSprintId) {
        if (requestedStatus != SprintStatus.ACTIVE) {
            return;
        }

        boolean anotherActiveSprintExists = sprintRepository.findByProjectIdOrderByStartDateAscCreatedAtAsc(projectId).stream()
            .anyMatch((sprint) -> sprint.getStatus() == SprintStatus.ACTIVE && !sprint.getId().equals(currentSprintId));

        if (anotherActiveSprintExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only one active sprint is allowed per project");
        }
    }

    private String normalizeGoal(String goal) {
        if (goal == null || goal.isBlank()) {
            return null;
        }

        return goal.trim();
    }

    private SprintResponse toResponse(Sprint sprint) {
        long totalTaskCount = taskRepository.countBySprintId(sprint.getId());
        long completedTaskCount = taskRepository.countBySprintIdAndStatus(sprint.getId(), TaskStatus.DONE);

        return new SprintResponse(
            sprint.getId(),
            sprint.getProject().getId(),
            sprint.getName(),
            sprint.getGoal(),
            sprint.getStartDate(),
            sprint.getEndDate(),
            sprint.getStatus().name(),
            totalTaskCount,
            completedTaskCount
        );
    }
}
