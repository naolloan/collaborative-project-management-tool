package com.collabpm.backend.comment;

import com.collabpm.backend.activity.ActivityService;
import com.collabpm.backend.comment.dto.CommentResponse;
import com.collabpm.backend.comment.dto.CreateCommentRequest;
import com.collabpm.backend.comment.model.Comment;
import com.collabpm.backend.comment.repository.CommentRepository;
import com.collabpm.backend.project.ProjectAccessService;
import com.collabpm.backend.task.model.Task;
import com.collabpm.backend.task.repository.TaskRepository;
import com.collabpm.backend.user.CurrentUserService;
import com.collabpm.backend.user.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final ActivityService activityService;
    private final ProjectAccessService projectAccessService;

    public CommentService(
        CommentRepository commentRepository,
        TaskRepository taskRepository,
        CurrentUserService currentUserService,
        ActivityService activityService,
        ProjectAccessService projectAccessService
    ) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.activityService = activityService;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listTaskComments(Long taskId, Authentication authentication) {
        Task task = ensureTaskExists(taskId);
        projectAccessService.ensureCanViewProject(task.getProject().getId(), authentication);
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public CommentResponse createComment(Long taskId, CreateCommentRequest request, Authentication authentication) {
        Task task = ensureTaskExists(taskId);
        projectAccessService.ensureCanViewProject(task.getProject().getId(), authentication);
        User author = currentUserService.getOrCreateCurrentUser(authentication);
        Comment comment = new Comment(task, author, request.content().trim());
        Comment savedComment = commentRepository.save(comment);
        activityService.recordCommentAdded(task, author);

        return toResponse(savedComment);
    }

    private Task ensureTaskExists(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getTask().getId(),
            comment.getAuthor().getFullName(),
            comment.getContent(),
            comment.getCreatedAt());
    }
}
