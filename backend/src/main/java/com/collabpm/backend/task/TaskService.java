package com.collabpm.backend.task;

import com.collabpm.backend.activity.ActivityService;
import com.collabpm.backend.activity.repository.ActivityLogRepository;
import com.collabpm.backend.comment.repository.CommentRepository;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.ProjectAccessService;
import com.collabpm.backend.project.repository.ProjectMemberRepository;
import com.collabpm.backend.project.repository.ProjectRepository;
import com.collabpm.backend.task.dto.CreateTaskRequest;
import com.collabpm.backend.task.dto.TaskResponse;
import com.collabpm.backend.task.dto.UpdateTaskRequest;
import com.collabpm.backend.task.dto.UpdateTaskStatusRequest;
import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.task.model.TaskPriority;
import com.collabpm.backend.task.model.TaskStatus;
import com.collabpm.backend.task.repository.TaskRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.User;
import com.collabpm.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final CommentRepository commentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ProjectAccessService projectAccessService;

    public TaskService(
        TaskRepository taskRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        CurrentUserService currentUserService,
        UserRepository userRepository,
        ActivityService activityService,
        CommentRepository commentRepository,
        ActivityLogRepository activityLogRepository,
        ProjectAccessService projectAccessService
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.commentRepository = commentRepository;
        this.activityLogRepository = activityLogRepository;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listProjectTasks(Long projectId, Authentication authentication) {
        ensureProjectExists(projectId);
        projectAccessService.ensureCanViewProject(projectId, authentication);
        return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, Authentication authentication) {
        Project project = ensureProjectExists(projectId);
        User creator = currentUserService.getOrCreateCurrentUser(authentication);
        projectAccessService.ensureCanViewProject(projectId, authentication);
        User assignee = resolveAssignee(projectId, request.assigneeId(), creator);
        Task task = new Task(
            project,
            request.title().trim(),
            normalizeDescription(request.description()),
            assignee,
            creator,
            request.dueDate());
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        Task savedTask = taskRepository.save(task);
        activityService.recordTaskCreated(savedTask, creator);

        return toResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, Authentication authentication) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        projectAccessService.ensureCanManageProject(task.getProject().getId(), authentication);
        User actor = currentUserService.getOrCreateCurrentUser(authentication);
        User oldAssignee = task.getAssignee();
        User newAssignee = resolveNullableAssignee(task.getProject().getId(), request.assigneeId());

        task.setTitle(request.title().trim());
        task.setDescription(normalizeDescription(request.description()));
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        task.setAssignee(newAssignee);
        task.setDueDate(request.dueDate());

        if (!sameUser(oldAssignee, newAssignee)) {
            activityService.recordTaskAssigned(
                task,
                actor,
                oldAssignee == null ? null : oldAssignee.getFullName(),
                newAssignee == null ? null : newAssignee.getFullName());
        }
        activityService.recordTaskUpdated(task, actor);

        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, Authentication authentication) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        projectAccessService.ensureCanViewProject(task.getProject().getId(), authentication);
        User actor = currentUserService.getOrCreateCurrentUser(authentication);
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(request.status());
        activityService.recordTaskStatusChanged(task, actor, oldStatus, request.status());

        return toResponse(task);
    }

    @Transactional
    public void deleteTask(Long taskId, Authentication authentication) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        projectAccessService.ensureCanManageProject(task.getProject().getId(), authentication);

        commentRepository.deleteByTaskId(taskId);
        activityLogRepository.deleteByTaskId(taskId);
        taskRepository.delete(task);
    }

    private Project ensureProjectExists(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private User resolveAssignee(Long projectId, Long assigneeId, User fallbackAssignee) {
        if (assigneeId == null) {
            return fallbackAssignee;
        }

        User assignee = userRepository.findById(assigneeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));
        ensureUserIsProjectMember(projectId, assignee.getId());
        return assignee;
    }

    private User resolveNullableAssignee(Long projectId, Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }

        User assignee = userRepository.findById(assigneeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));
        ensureUserIsProjectMember(projectId, assignee.getId());
        return assignee;
    }

    private void ensureUserIsProjectMember(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must be a project member");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private boolean sameUser(User first, User second) {
        Long firstId = first == null ? null : first.getId();
        Long secondId = second == null ? null : second.getId();
        return Objects.equals(firstId, secondId);
    }

    private TaskResponse toResponse(Task task) {
        User assignee = task.getAssignee();
        User createdBy = task.getCreatedBy();

        return new TaskResponse(
            task.getId(),
            task.getProject().getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().name(),
            task.getPriority().name(),
            assignee == null ? null : assignee.getId(),
            assignee == null ? null : assignee.getFullName(),
            createdBy == null ? null : createdBy.getFullName(),
            task.getDueDate());
    }
}
