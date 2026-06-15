package com.collabpm.backend.activity;

import com.collabpm.backend.activity.dto.ActivityResponse;
import com.collabpm.backend.activity.model.ActivityLog;
import com.collabpm.backend.activity.model.ActivitySubjectType;
import com.collabpm.backend.activity.model.ActivityType;
import com.collabpm.backend.activity.repository.ActivityLogRepository;
import com.collabpm.backend.project.ProjectAccessService;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectMember;
import com.collabpm.backend.sprint.model.Sprint;
import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.task.model.TaskStatus;
import com.collabpm.backend.task.repository.TaskRepository;
import com.collabpm.backend.user.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final TaskRepository taskRepository;
    private final ProjectAccessService projectAccessService;
    public ActivityService(
        ActivityLogRepository activityLogRepository,
        TaskRepository taskRepository,
        ProjectAccessService projectAccessService
    ) {
        this.activityLogRepository = activityLogRepository;
        this.taskRepository = taskRepository;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> listTaskActivities(Long taskId, Authentication authentication) {
        Task task = ensureTaskExists(taskId);
        projectAccessService.ensureCanViewProject(task.getProject().getId(), authentication);
        return activityLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> listProjectActivities(Long projectId, Authentication authentication) {
        projectAccessService.ensureCanViewProject(projectId, authentication);
        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(this::toResponse)
            .toList();
    }

    public void recordProjectCreated(Project project, User actor) {
        save(project, null, null, actor, ActivityType.PROJECT_CREATED, ActivitySubjectType.PROJECT, project.getName(), null, project.getName());
    }

    public void recordProjectUpdated(Project project, User actor) {
        save(project, null, null, actor, ActivityType.PROJECT_UPDATED, ActivitySubjectType.PROJECT, project.getName(), null, null);
    }

    public void recordProjectArchived(Project project, User actor) {
        save(project, null, null, actor, ActivityType.PROJECT_ARCHIVED, ActivitySubjectType.PROJECT, project.getName(), null, project.getStatus().name());
    }

    public void recordProjectMemberAdded(ProjectMember member, User actor) {
        save(
            member.getProject(),
            null,
            null,
            actor,
            ActivityType.PROJECT_MEMBER_ADDED,
            ActivitySubjectType.MEMBER,
            member.getUser().getFullName(),
            null,
            member.getProjectRole().name()
        );
    }

    public void recordSprintCreated(Sprint sprint, User actor) {
        save(sprint.getProject(), sprint, null, actor, ActivityType.SPRINT_CREATED, ActivitySubjectType.SPRINT, sprint.getName(), null, sprint.getStatus().name());
    }

    public void recordSprintUpdated(Sprint sprint, User actor) {
        save(sprint.getProject(), sprint, null, actor, ActivityType.SPRINT_UPDATED, ActivitySubjectType.SPRINT, sprint.getName(), null, null);
    }

    public void recordSprintStatusChanged(Sprint sprint, User actor, String oldStatus, String newStatus) {
        if (oldStatus != null && oldStatus.equals(newStatus)) {
            return;
        }

        save(sprint.getProject(), sprint, null, actor, ActivityType.SPRINT_STATUS_CHANGED, ActivitySubjectType.SPRINT, sprint.getName(), oldStatus, newStatus);
    }

    public void recordTaskCreated(Task task, User actor) {
        save(task.getProject(), task.getSprint(), task, actor, ActivityType.TASK_CREATED, ActivitySubjectType.TASK, task.getTitle(), null, task.getTitle());
    }

    public void recordTaskDeleted(Task task, User actor) {
        save(task.getProject(), task.getSprint(), null, actor, ActivityType.TASK_DELETED, ActivitySubjectType.TASK, task.getTitle(), null, task.getTitle());
    }

    public void recordTaskStatusChanged(Task task, User actor, TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus == newStatus) {
            return;
        }

        save(task.getProject(), task.getSprint(), task, actor, ActivityType.TASK_STATUS_CHANGED, ActivitySubjectType.TASK, task.getTitle(), oldStatus.name(), newStatus.name());
    }

    public void recordTaskUpdated(Task task, User actor) {
        save(task.getProject(), task.getSprint(), task, actor, ActivityType.TASK_UPDATED, ActivitySubjectType.TASK, task.getTitle(), null, null);
    }

    public void recordTaskAssigned(Task task, User actor, String oldAssigneeName, String newAssigneeName) {
        save(task.getProject(), task.getSprint(), task, actor, ActivityType.TASK_ASSIGNED, ActivitySubjectType.TASK, task.getTitle(), oldAssigneeName, newAssigneeName);
    }

    public void recordCommentAdded(Task task, User actor) {
        save(task.getProject(), task.getSprint(), task, actor, ActivityType.COMMENT_ADDED, ActivitySubjectType.TASK, task.getTitle(), null, null);
    }

    private void save(
        Project project,
        Sprint sprint,
        Task task,
        User actor,
        ActivityType actionType,
        ActivitySubjectType subjectType,
        String subjectName,
        String oldValue,
        String newValue
    ) {
        activityLogRepository.save(new ActivityLog(project, sprint, task, actor, actionType, subjectType, subjectName, oldValue, newValue));
    }

    private Task ensureTaskExists(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private ActivityResponse toResponse(ActivityLog activityLog) {
        return new ActivityResponse(
            activityLog.getId(),
            activityLog.getProject().getId(),
            activityLog.getSprint() == null ? null : activityLog.getSprint().getId(),
            activityLog.getTask() == null ? null : activityLog.getTask().getId(),
            activityLog.getSubjectType().name(),
            activityLog.getSubjectName(),
            activityLog.getActor().getFullName(),
            activityLog.getActionType().name(),
            activityLog.getOldValue(),
            activityLog.getNewValue(),
            toMessage(activityLog),
            activityLog.getCreatedAt());
    }

    private String toMessage(ActivityLog activityLog) {
        String actorName = activityLog.getActor().getFullName();
        return switch (activityLog.getActionType()) {
            case PROJECT_CREATED -> actorName + " created project " + quote(activityLog.getSubjectName());
            case PROJECT_UPDATED -> actorName + " updated project " + quote(activityLog.getSubjectName());
            case PROJECT_ARCHIVED -> actorName + " archived project " + quote(activityLog.getSubjectName());
            case PROJECT_MEMBER_ADDED -> actorName + " added " + quote(activityLog.getSubjectName())
                + " to the project as " + formatValue(activityLog.getNewValue());
            case SPRINT_CREATED -> actorName + " created sprint " + quote(activityLog.getSubjectName());
            case SPRINT_UPDATED -> actorName + " updated sprint " + quote(activityLog.getSubjectName());
            case SPRINT_STATUS_CHANGED -> actorName + " changed sprint " + quote(activityLog.getSubjectName()) + " from "
                + formatValue(activityLog.getOldValue()) + " to " + formatValue(activityLog.getNewValue());
            case TASK_CREATED -> actorName + " created task " + quote(activityLog.getSubjectName());
            case TASK_DELETED -> actorName + " deleted task " + quote(activityLog.getSubjectName());
            case TASK_STATUS_CHANGED -> actorName + " moved task " + quote(activityLog.getSubjectName()) + " from "
                + formatValue(activityLog.getOldValue()) + " to " + formatValue(activityLog.getNewValue());
            case COMMENT_ADDED -> actorName + " commented on task " + quote(activityLog.getSubjectName());
            case TASK_ASSIGNED -> actorName + " changed the assignee of task " + quote(activityLog.getSubjectName()) + " from "
                + formatValue(activityLog.getOldValue()) + " to " + formatValue(activityLog.getNewValue());
            case TASK_UPDATED -> actorName + " updated task " + quote(activityLog.getSubjectName());
        };
    }

    private String quote(String value) {
        if (value == null || value.isBlank()) {
            return "this item";
        }

        return "\"" + value + "\"";
    }

    private String formatValue(String value) {
        if (value == null || value.isBlank()) {
            return "not set";
        }

        String[] words = value.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!formatted.isEmpty()) {
                formatted.append(' ');
            }
            formatted.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return formatted.toString();
    }
}
