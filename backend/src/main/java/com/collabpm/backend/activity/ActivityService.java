package com.collabpm.backend.activity;

import com.collabpm.backend.activity.dto.ActivityResponse;
import com.collabpm.backend.activity.model.ActivityLog;
import com.collabpm.backend.activity.model.ActivityType;
import com.collabpm.backend.activity.repository.ActivityLogRepository;
import com.collabpm.backend.project.ProjectAccessService;
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

    public void recordTaskCreated(Task task, User actor) {
        save(task, actor, ActivityType.TASK_CREATED, null, task.getTitle());
    }

    public void recordTaskStatusChanged(Task task, User actor, TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus == newStatus) {
            return;
        }

        save(task, actor, ActivityType.TASK_STATUS_CHANGED, oldStatus.name(), newStatus.name());
    }

    public void recordTaskUpdated(Task task, User actor) {
        save(task, actor, ActivityType.TASK_UPDATED, null, null);
    }

    public void recordTaskAssigned(Task task, User actor, String oldAssigneeName, String newAssigneeName) {
        save(task, actor, ActivityType.TASK_ASSIGNED, oldAssigneeName, newAssigneeName);
    }

    public void recordCommentAdded(Task task, User actor) {
        save(task, actor, ActivityType.COMMENT_ADDED, null, null);
    }

    private void save(Task task, User actor, ActivityType actionType, String oldValue, String newValue) {
        activityLogRepository.save(new ActivityLog(task, actor, actionType, oldValue, newValue));
    }

    private Task ensureTaskExists(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private ActivityResponse toResponse(ActivityLog activityLog) {
        return new ActivityResponse(
            activityLog.getId(),
            activityLog.getTask().getId(),
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
            case TASK_CREATED -> actorName + " created this task";
            case TASK_STATUS_CHANGED -> actorName + " moved this task from "
                + formatValue(activityLog.getOldValue()) + " to " + formatValue(activityLog.getNewValue());
            case COMMENT_ADDED -> actorName + " commented on this task";
            case TASK_ASSIGNED -> actorName + " changed the task assignee from "
                + formatValue(activityLog.getOldValue()) + " to " + formatValue(activityLog.getNewValue());
            case TASK_UPDATED -> actorName + " updated this task";
        };
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
